package com.ruoyi.biz.ocr;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ruoyi.common.exception.ServiceException;

/**
 * 百度 OCR 底层请求服务
 * 使用 OkHttp（连接池 + 自动重试）替代 HttpURLConnection
 *
 * 降级策略：
 *   Step 1: v1/table_vinner + 原图
 *   Step 2: v1/table_vinner + 灰度二值化预处理图
 *   Step 3: v1/table + 原图（最原始接口）
 *   Step 4: v1/general 通用文字识别（兜底，处理无线表格 / 密集小字 / 标题段等场景）
 */
@Service
public class BaiduOcrService {

    private static final Logger log = LoggerFactory.getLogger(BaiduOcrService.class);

    private static final String TABLE_VINNER_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/table";
    private static final String TABLE_V1_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/table";
    private static final String GENERAL_OCR_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/general";

    // OkHttp 连接池复用，连接/读取超时各 60s（服务器网络可能较慢）
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    // Token 缓存（有效期 30 天，不用每次请求都重新获取）
    private volatile String cachedToken = null;
    private volatile long tokenExpireTime = 0L;
    private static final long TOKEN_VALIDITY_MS = 29L * 24 * 3600 * 1000;

    @Value("${baidu.aip.api-key:你的默认AK}")
    private String apiKey;

    @Value("${baidu.aip.secret-key:你的默认SK}")
    private String secretKey;

    /**
     * 发起表格识别请求，智能降级重试
     */
    public JSONObject recognizeTable(String imagePath) {
        try {
            byte[] rawData = Files.readAllBytes(Paths.get(imagePath));
            String rawBase64 = Base64.getEncoder().encodeToString(rawData);
            String accessToken = getValidToken();

            // Step 1: v1/table_vinner + 原图
            log.info("OCR[vinner] 尝试识别: {}", imagePath);
            JSONObject result = doRequestWithRetry(TABLE_VINNER_URL, accessToken, rawBase64, 2);
            if (hasTableResult(result)) {
                log.info("OCR[vinner] 识别成功");
                return normalizeResponse(result);
            }
            logOcrFallbackDetail("vinner原图", result);

            // Step 2: v1/table_vinner + 预处理增强图
            BufferedImage original = readJpegImage(rawData);
            if (original != null) {
                log.info("OCR[vinner+预处理] 原图尺寸: {}x{}, 尝试识别", original.getWidth(), original.getHeight());
                byte[] enhanced = enhanceSimple(original);
                if (enhanced != null) {
                    String enhancedBase64 = Base64.getEncoder().encodeToString(enhanced);
                    result = doRequestWithRetry(TABLE_VINNER_URL, accessToken, enhancedBase64, 2);
                    if (hasTableResult(result)) {
                        log.info("OCR[vinner+预处理] 识别成功");
                        return normalizeResponse(result);
                    }
                    logOcrFallbackDetail("vinner+预处理", result);
                }
            } else {
                log.warn("OCR 图片无法被 ImageIO 解析，将尝试 v1/table");
            }

            // Step 3: v1/table + 原图（最原始接口，对标准图片效果好）
            log.info("OCR[v1] 降级尝试: {}", imagePath);
            result = doRequestWithRetry(TABLE_V1_URL, accessToken, rawBase64, 2);
            if (hasTableResult(result)) {
                log.info("OCR[v1] 识别成功");
                return normalizeResponse(result);
            }
            logOcrFallbackDetail("v1原图", result);

            // Step 4: v1/general 通用文字识别（兜底，对无线表格/密集小字版面也能拿到文字）
            log.info("OCR[general] 兜底尝试: {}", imagePath);
            JSONObject general = recognizeGeneral(rawBase64, accessToken);
            if (hasWordsResult(general)) {
                int wordsCount = countWords(general);
                log.info("OCR[general] 识别成功（{} 个文字块）", wordsCount);
                return normalizeResponse(general);
            }
            logOcrFallbackDetail("general原图", general);

            log.warn("OCR 全部方案均未识别到表格或文字");
            return normalizeResponse(general != null ? general : result);

        } catch (ServiceException e) {
            throw e;
        } catch (java.io.IOException e) {
            throw new ServiceException("OCR 图片文件读取失败，请检查文件是否存在: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ServiceException("OCR 图片格式异常，不是有效的图片文件: " + e.getMessage());
        } catch (Exception e) {
            throw new ServiceException("请求百度OCR接口失败，请检查网络: " + e.getMessage());
        }
    }

    /**
     * 获取有效 Token（有缓存，30 天有效）
     */
    private String getValidToken() {
        long now = System.currentTimeMillis();
        if (cachedToken != null && now < tokenExpireTime) {
            return cachedToken;
        }
        String newToken = getAuth(apiKey, secretKey);
        cachedToken = newToken;
        tokenExpireTime = now + TOKEN_VALIDITY_MS;
        log.info("百度 OCR Token 已刷新，有效期至: {}", new java.util.Date(tokenExpireTime));
        return newToken;
    }

    boolean hasTableResult(JSONObject result) {
        if (result == null) return false;
        // 标准 v1/table 接口返回 result 字段，内部有表格数据
        if (result.has("result")) {
            try {
                JSONObject res = result.getJSONObject("result");
                // 检查是否有表格结构数据
                return res.has("tables_result") && res.getJSONArray("tables_result").length() > 0
                    || res.has("forms_result") && res.getJSONArray("forms_result").length() > 0;
            } catch (Exception e) {
                return false;
            }
        }
        // 兼容旧格式
        if (!result.has("tables_result")) return false;
        try {
            return result.getJSONArray("tables_result").length() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 归一化百度 OCR 响应：把所有表格/文字数据统一包到 result 字段下，
     * 让下游 Controller 不用关心两种格式差异。
     *
     * 旧格式（v1/table）：{ result: { tables_result: [...] } }
     * 新格式（v1/table_vinner）：{ tables_result: [...], table_num:1, log_id:... }
     *
     * 输入：可能两种格式的 JSONObject
     * 输出：始终是 { result: { tables_result?: [...], forms_result?: [...], words_result?: [...] } }
     */
    JSONObject normalizeResponse(JSONObject raw) {
        if (raw == null) return null;
        if (raw.has("error_code")) return raw;  // 错误响应原样返回
        if (raw.has("result")) return raw;       // 已经是标准格式
        // 把顶层 tables_result / forms_result / words_result 包装到 result 里
        JSONObject res = new JSONObject();
        if (raw.has("tables_result")) res.put("tables_result", raw.getJSONArray("tables_result"));
        if (raw.has("forms_result")) res.put("forms_result", raw.getJSONArray("forms_result"));
        if (raw.has("words_result")) res.put("words_result", raw.getJSONArray("words_result"));
        if (raw.has("table_num")) res.put("table_num", raw.get("table_num"));
        if (raw.has("log_id")) res.put("log_id", raw.get("log_id"));
        if (res.length() == 0) return raw;       // 空响应原样返回
        return new JSONObject().put("result", res);
    }

    /**
     * 通用 OCR（v1/general）兜底识别：对无线表格、密集小字版面也能拿到 words_result。
     * 直接复用 doRequestWithRetry + 现有 Token 缓存。
     *
     * 返回结构：{ result: { words_result: [ { words, location{top,left,width,height} } ] }, error_code, error_msg }
     */
    JSONObject recognizeGeneral(String base64Image, String accessToken) {
        try {
            return doRequestWithRetry(GENERAL_OCR_URL, accessToken, base64Image, 2);
        } catch (Exception e) {
            log.warn("OCR[general] 调用异常: {}", e.getMessage());
            return new JSONObject();
        }
    }

    /**
     * 对单张图片直接走 v1/general 通用 OCR（不经过 table_vinner 降级链）。
     * 用于表格外的游离文字（如"培训部门：xxx"在纸张左上角游离于表格之外）。
     * 与 {@link #recognizeTable} 的区别：本方法永远返回 words_result 形式，
     * 不会因表格识别成功而提前返回。
     *
     * 返回结构同 {@link #recognizeGeneral}，再经 {@link #normalizeResponse} 归一化。
     */
    public JSONObject recognizeGeneralFromFile(String imagePath) {
        try {
            byte[] rawData = Files.readAllBytes(Paths.get(imagePath));
            String rawBase64 = Base64.getEncoder().encodeToString(rawData);
            String accessToken = getValidToken();
            log.info("OCR[general-direct] 直接走通用识别: {}", imagePath);
            JSONObject result = recognizeGeneral(rawBase64, accessToken);
            if (result.has("error_code") && result.optInt("error_code") != 0) {
                int errCode = result.optInt("error_code");
                String errMsg = result.optString("error_msg", "");
                log.error("OCR[general-direct] 百度业务错误: code={} msg={}", errCode, errMsg);
                JSONObject wrap = new JSONObject();
                wrap.put("error_code", errCode);
                wrap.put("error_msg", errMsg);
                wrap.put("result", new JSONObject().put("error", errMsg));
                return wrap;
            }
            if (hasWordsResult(result)) {
                int wordsCount = countWords(result);
                log.info("OCR[general-direct] 识别成功（{} 个文字块）", wordsCount);
            } else {
                logOcrFallbackDetail("general-direct", result);
            }
            return normalizeResponse(result);
        } catch (java.io.IOException e) {
            log.error("OCR[general-direct] 图片读取失败: {}", e.getMessage());
            return new JSONObject();
        } catch (Exception e) {
            log.error("OCR[general-direct] 调用异常: {}", e.getMessage());
            return new JSONObject();
        }
    }

    boolean hasWordsResult(JSONObject result) {
        if (result == null) return false;
        try {
            // 兼容两种格式：顶层 words_result（旧 v1/general）和嵌套 result.words_result
            if (result.has("result")) {
                JSONArray words = result.getJSONObject("result").optJSONArray("words_result");
                if (words != null && words.length() > 0) return true;
            }
            if (result.has("words_result")) {
                JSONArray words = result.getJSONArray("words_result");
                return words != null && words.length() > 0;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private int countWords(JSONObject result) {
        if (result == null || !result.has("result")) return 0;
        try {
            JSONArray words = result.getJSONObject("result").optJSONArray("words_result");
            return words == null ? 0 : words.length();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 打印单步降级的详细结果，便于排查"无线表格 / 标题页"等场景下百度接口的实际返回。
     */
    private void logOcrFallbackDetail(String stepName, JSONObject result) {
        if (result == null) {
            log.info("OCR[{}] 返回 null", stepName);
            return;
        }
        if (result.has("error_code")) {
            log.info("OCR[{}] 业务错误: error_code={}, error_msg={}",
                    stepName, result.optString("error_code"), result.optString("error_msg"));
            return;
        }
        if (!result.has("result")) {
            log.info("OCR[{}] 无 result 字段，完整响应: {}", stepName, result.toString());
            return;
        }
        JSONObject res = result.optJSONObject("result");
        if (res == null) {
            log.info("OCR[{}] result 不是 JSONObject: {}", stepName, result.opt("result"));
            return;
        }
        JSONArray tables = res.optJSONArray("tables_result");
        JSONArray forms = res.optJSONArray("forms_result");
        JSONArray words = res.optJSONArray("words_result");
        int tableLen = tables == null ? 0 : tables.length();
        int formLen = forms == null ? 0 : forms.length();
        int wordLen = words == null ? 0 : words.length();
        int bodyLen = 0;
        int headerLen = 0;
        if (tables != null && tableLen > 0) {
            JSONObject first = tables.optJSONObject(0);
            if (first != null) {
                bodyLen = first.optJSONArray("body") == null ? 0 : first.getJSONArray("body").length();
                headerLen = first.optJSONArray("header") == null ? 0 : first.getJSONArray("header").length();
            }
        }
        log.info("OCR[{}] tables_result={}, body={}, header={}, forms_result={}, words_result={}",
                stepName, tableLen, bodyLen, headerLen, formLen, wordLen);
    }

    /**
     * 带重试的 OCR 请求（OkHttp 自动处理连接失败重试）
     */
    private JSONObject doRequestWithRetry(String url, String token, String base64Image, int retries) {
        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                JSONObject result = doRequest(url, token, base64Image);
                if (result != null && !result.isNull("error_code")) {
                    int errCode = result.optInt("error_code");
                    String errorMsg = result.optString("error_msg", "");
                    // Token 无效或过期，强制刷新后重试
                    if (errCode == 110 || (errCode == 100 || errorMsg.contains("access token"))) {
                        log.warn("OCR Token 无效，刷新后重试第 {}/{} 次 (code={})", attempt, retries, errCode);
                        cachedToken = null;
                        token = getValidToken();
                        continue;
                    }
                }
                return result;
            } catch (Exception e) {
                if (attempt < retries) {
                    log.warn("OCR 请求失败，第 {}/{} 次重试: {}", attempt, retries, e.getMessage());
                    try { Thread.sleep(1000L * attempt); } catch (InterruptedException ignored) {}
                } else {
                    log.error("OCR 请求最终失败: {}", e.getMessage());
                    return new JSONObject();
                }
            }
        }
        return new JSONObject();
    }

    /**
     * 发送 OCR 请求（OkHttp）
     * 使用 application/x-www-form-urlencoded 格式，符合百度 OCR API 要求
     */
    private JSONObject doRequest(String url, String token, String base64Image) throws Exception {
        String fullUrl = url + "?access_token=" + token;

        // DEBUG: 记录请求信息
        log.debug("OCR请求URL: {}", fullUrl);
        log.debug("Base64长度: {}", base64Image.length());

        // 百度 OCR API 要求使用 application/x-www-form-urlencoded 格式
        // image 参数需要先 base64 编码，然后进行 URL encode
        String urlEncodedImage = URLEncoder.encode(base64Image, StandardCharsets.UTF_8.toString());
        String body = "image=" + urlEncodedImage;

        log.debug("请求Body长度: {}", body.length());

        Request request = new Request.Builder()
                .url(fullUrl)
                .post(RequestBody.create(body, MediaType.parse("application/x-www-form-urlencoded")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new ServiceException("百度OCR接口返回异常，HTTP状态码: " + response.code());
            }
            ResponseBody respBody = response.body();
            if (respBody == null) {
                throw new ServiceException("百度OCR接口返回空响应");
            }
            String bodyStr = respBody.string();
            // DEBUG: 记录响应（生产环境可关闭）
            log.debug("OCR响应: {}", bodyStr.length() > 300 ? bodyStr.substring(0, 300) + "..." : bodyStr);
            // STEP1-D: 完整记录响应（仅当 DEBUG 启用时输出），便于排查"无线表格"返回内容
            log.debug("OCR完整响应[url={}]: {}", url, bodyStr);
            return new JSONObject(bodyStr);
        }
    }

    private BufferedImage readJpegImage(byte[] data) {
        try {
            BufferedImage img = ImageIO.read(new java.io.ByteArrayInputStream(data));
            if (img != null) return img;
        } catch (java.io.IOException e) { /* ignore */ }

        try {
            javax.imageio.ImageReader reader =
                javax.imageio.ImageIO.getImageReadersByFormatName("jpeg").next();
            javax.imageio.stream.ImageInputStream iis =
                javax.imageio.ImageIO.createImageInputStream(new java.io.ByteArrayInputStream(data));
            reader.setInput(iis, true);
            BufferedImage img = reader.read(0);
            iis.close();
            if (img != null) {
                log.info("JpegReader 成功读取图片: {}x{}", img.getWidth(), img.getHeight());
                return img;
            }
        } catch (Exception e) {
            log.warn("JpegReader 读取失败: {}", e.getMessage());
        }

        return null;
    }

    private byte[] enhanceSimple(BufferedImage src) {
        try {
            int w = src.getWidth();
            int h = src.getHeight();

            long sum = 0;
            int[] gray = new int[w * h];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int rgb = src.getRGB(x, y);
                    int r = (rgb >> 16) & 0xff;
                    int g = (rgb >> 8) & 0xff;
                    int b = rgb & 0xff;
                    int gv = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                    gray[y * w + x] = gv;
                    sum += gv;
                }
            }
            int mean = (int) (sum / (w * h));

            double factor = 2.2;
            int[] stretched = new int[w * h];
            for (int i = 0; i < gray.length; i++) {
                int v = gray[i];
                int out = (int) ((v - mean) * factor + 128);
                stretched[i] = Math.max(0, Math.min(255, out));
            }

            int THRESHOLD = 150;
            BufferedImage binary = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int val = stretched[y * w + x] > THRESHOLD ? 255 : 0;
                    binary.setRGB(x, y, (val << 16) | (val << 8) | val);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(binary, "png", baos);
            log.info("图像预处理成功，输出 {}x{} PNG", w, h);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("图像预处理失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取百度鉴权 Token（使用 OkHttp）
     */
    private String getAuth(String ak, String sk) {
        String getAccessTokenUrl = "https://aip.baidubce.com/oauth/2.0/token?"
                + "grant_type=client_credentials&client_id=" + ak + "&client_secret=" + sk;
        try {
            Request request = new Request.Builder()
                    .url(getAccessTokenUrl)
                    .get()
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new ServiceException("获取百度Token失败，HTTP状态码: " + response.code());
                }
                ResponseBody body = response.body();
                if (body == null) {
                    throw new ServiceException("获取百度Token返回空响应");
                }
                return new JSONObject(body.string()).getString("access_token");
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("获取百度Token失败，请检查 AK/SK 配置是否正确: " + e.getMessage());
        }
    }
}
