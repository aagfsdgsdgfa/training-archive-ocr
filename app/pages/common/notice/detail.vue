<template>
  <view class="notice-detail-page">
    <view class="blob blob-1"></view>
    <view class="blob blob-2"></view>

    <view class="custom-navbar glass-nav" :style="{ paddingTop: statusBarHeight + 'px', height: navBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" color="#1a1a1a" size="24"></uni-icons>
      </view>
      <view class="nav-center">
        <text class="nav-title">Read</text>
      </view>
      <view class="nav-right"></view>
    </view>

    <view class="main-container" :style="{ paddingTop: (statusBarHeight + navBarHeight + 20) + 'px' }">
      
      <view class="hero-header glass-panel">
        <view class="tag-row">
          <view class="type-tag" :class="notice.noticeType === '1' ? 'tag-notice' : 'tag-announce'">
            <view class="dot"></view>
            <text>{{ notice.noticeType === '1' ? '系统通知' : '公司公告' }}</text>
          </view>
        </view>
        
        <text class="notice-title">{{ notice.noticeTitle || '加载中...' }}</text>
        
        <view class="meta-row">
          <view class="meta-item">
            <uni-icons type="person" color="#909399" size="14"></uni-icons>
            <text class="m-text">{{ notice.createBy || '系统管理员' }}</text>
          </view>
          <view class="meta-item">
            <uni-icons type="calendar" color="#909399" size="14"></uni-icons>
            <text class="m-text num-font">{{ formatDate(notice.createTime) }}</text>
          </view>
        </view>
      </view>

      <view class="content-card glass-panel">
        <view v-if="!notice.noticeContent" class="empty-content">
          <text>正文内容为空</text>
        </view>
        <view v-else class="rich-text-wrapper">
          <rich-text :nodes="parsedContent"></rich-text>
        </view>
      </view>

      <view class="footer-mark">
        <text>END OF DOCUMENT</text>
      </view>

    </view>
  </view>
</template>

<script>
import { getNotice } from '@/api/biz'

export default {
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,
      noticeId: null,
      
      notice: {},
      parsedContent: '' // 清洗后的富文本内容
    }
  },
  onLoad(options) {
    this.initSystemInfo()
    if (options.noticeId) {
      this.noticeId = options.noticeId
      this.fetchNoticeDetail()
    } else {
      this.$modal.msgError("非法访问：缺少公告ID")
    }
  },
  methods: {
    initSystemInfo() {
      const systemInfo = uni.getSystemInfoSync()
      this.statusBarHeight = systemInfo.statusBarHeight || 20
      // #ifdef MP-WEIXIN
      let custom = uni.getMenuButtonBoundingClientRect()
      this.navBarHeight = custom.height + (custom.top - this.statusBarHeight) * 2
      // #endif
    },

    goBack() {
      const pages = getCurrentPages()
      if (pages.length > 1) {
        uni.navigateBack({ delta: 1 })
      } else {
        this.$tab.switchTab('/pages/common/notice/index')
      }
    },

    async fetchNoticeDetail() {
      uni.showLoading({ title: '加载正文...' })
      try {
        const res = await getNotice(this.noticeId)
        this.notice = res.data || {}
        
        // 核心：处理富文本中的图片和排版，防止在手机端溢出撑破页面
        if (this.notice.noticeContent) {
          this.parsedContent = this.formatRichText(this.notice.noticeContent)
        }
        
        uni.hideLoading()
      } catch (error) {
        uni.hideLoading()
        console.error('获取公告详情失败:', error)
      }
    },

    // 富文本清洗拦截器 (专门适配移动端阅读体验)
    formatRichText(html) {
      let newContent = html.replace(/<img[^>]*>/gi, function(match, capture) {
        match = match.replace(/style="[^"]+"/gi, '').replace(/style='[^']+'/gi, '');
        match = match.replace(/width="[^"]+"/gi, '').replace(/width='[^']+'/gi, '');
        match = match.replace(/height="[^"]+"/gi, '').replace(/height='[^']+'/gi, '');
        return match;
      });
      // 强制给图片加上宽度 100% 和圆角，适配高级卡片UI
      newContent = newContent.replace(/<img/gi, '<img style="max-width:100%;height:auto;display:block;margin:20px auto;border-radius:16px;"');
      // 修正段落行高
      newContent = newContent.replace(/<p/gi, '<p style="line-height:1.8;color:#333;font-size:16px;margin-bottom:16px;"');
      return newContent;
    },

    formatDate(dateStr) {
      if (!dateStr) return '未知时间'
      // 格式化为 2026.03.09 14:30
      return dateStr.substring(0, 16).replace(/-/g, '.')
    }
  }
}
</script>

<style lang="scss" scoped>
.notice-detail-page {
  width: 100vw;
  min-height: 100vh;
  background-color: #f8faff;
  position: relative;
  overflow-x: hidden;
  padding-bottom: 80rpx;

  /* 背景光晕 */
  .blob {
    position: absolute;
    width: 600rpx; height: 600rpx;
    background: linear-gradient(135deg, rgba(60, 156, 255, 0.1), rgba(122, 187, 255, 0.05));
    filter: blur(90px); border-radius: 50%; z-index: 0;
    animation: move 20s infinite alternate;
  }
  .blob-1 { top: -100rpx; right: -150rpx; }
  .blob-2 { bottom: 20%; left: -150rpx; background: rgba(166, 140, 227, 0.08); width: 400rpx; height: 400rpx; }

  @keyframes move {
    from { transform: translate(0, 0) scale(1); }
    to { transform: translate(40rpx, 80rpx) scale(1.05); }
  }

  /* 自定义沉浸式导航栏 */
  .custom-navbar {
    position: fixed; top: 0; left: 0; right: 0; z-index: 999;
    display: flex; align-items: center; justify-content: space-between; padding: 0 30rpx;
  }
  .glass-nav {
    background: rgba(248, 250, 255, 0.85); backdrop-filter: blur(20px);
    border-bottom: 1px solid rgba(0, 0, 0, 0.02);
  }
  .nav-left, .nav-right { width: 80rpx; display: flex; align-items: center; }
  .nav-center {
    flex: 1; display: flex; justify-content: center;
    .nav-title { font-size: 34rpx; font-weight: 800; color: #1a1a1a; letter-spacing: 1px; text-transform: uppercase; }
  }

  .main-container {
    position: relative; z-index: 1; width: 88%; max-width: 680rpx; margin: 0 auto;
  }

  /* 通用毛玻璃基类 */
  .glass-panel {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(25px);
    border: 1px solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 30rpx 60rpx rgba(0, 0, 0, 0.03);
  }

  /* ================= 1. 顶部标题与元数据区 ================= */
  .hero-header {
    border-radius: 40rpx;
    padding: 50rpx 40rpx;
    margin-bottom: 40rpx;
    display: flex;
    flex-direction: column;

    .tag-row {
      display: flex; margin-bottom: 24rpx;
      .type-tag {
        display: flex; align-items: center; padding: 6rpx 20rpx; border-radius: 12rpx; border: 1px solid rgba(0,0,0,0.03);
        .dot { width: 10rpx; height: 10rpx; border-radius: 50%; margin-right: 10rpx; }
        text { font-size: 22rpx; font-weight: 700; }
      }
      .tag-notice { background: rgba(250,53,52,0.08); .dot { background: #fa3534; } text { color: #fa3534; } }
      .tag-announce { background: rgba(26,26,26,0.05); .dot { background: #1a1a1a; } text { color: #1a1a1a; } }
    }

    .notice-title {
      font-size: 48rpx; font-weight: 900; color: #1a1a1a; line-height: 1.4; 
      letter-spacing: -0.5px; margin-bottom: 30rpx; word-break: break-all;
    }

    .meta-row {
      display: flex; align-items: center; gap: 30rpx;
      padding-top: 24rpx; border-top: 1px dashed rgba(0,0,0,0.06);
      
      .meta-item {
        display: flex; align-items: center;
        .m-text { font-size: 24rpx; color: #909399; font-weight: 600; margin-left: 8rpx; }
        .num-font { font-family: monospace; letter-spacing: 0.5px; }
      }
    }
  }

  /* ================= 2. 富文本正文阅读区 ================= */
  .content-card {
    border-radius: 40rpx;
    padding: 50rpx 40rpx;
    margin-bottom: 60rpx;

    .empty-content {
      text-align: center; padding: 60rpx 0;
      text { font-size: 26rpx; color: #ccc; }
    }

    /* 利用外层限制给富文本更好的间距 */
    .rich-text-wrapper {
      word-wrap: break-word;
      word-break: normal;
    }
  }

  /* ================= 底部收尾标识 ================= */
  .footer-mark {
    display: flex; justify-content: center; align-items: center; opacity: 0.3;
    text { font-size: 20rpx; font-weight: 800; color: #1a1a1a; letter-spacing: 4px; }
  }
}
</style>