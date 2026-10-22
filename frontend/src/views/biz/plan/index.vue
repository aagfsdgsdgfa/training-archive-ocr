<template>
  <div class="app-container cloud-glass-container">
    <div class="glass-panel" style="padding: 20px 20px 5px 20px;">
      <el-row :gutter="20" class="mb20">
        <el-col :span="6">
          <div class="glass-metric-card">
            <div class="icon-wrapper bg-blue"><i class="el-icon-document-copy" /></div>
            <div class="metric-info">
              <div class="label">年度计划总数</div>
              <div class="value">{{ stats.total }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="glass-metric-card">
            <div class="icon-wrapper bg-blue"><i class="el-icon-circle-check" /></div>
            <div class="metric-info">
              <div class="label">已完成计划</div>
              <div class="value" style="color: #67C23A;">{{ stats.completed }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="glass-metric-card">
            <div class="icon-wrapper bg-orange"><i class="el-icon-time" /></div>
            <div class="metric-info">
              <div class="label">待执行计划</div>
              <div class="value" style="color: #E6A23C;">{{ stats.pending }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="glass-metric-card upload-card" @click="handleScan" v-hasPermi="['biz:batch:add']">
            <div class="upload-content">
              <i class="el-icon-camera cloud-icon"></i>
              <span>扫描计划表</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="glass-panel search-panel" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="80px">
        <el-form-item label="计划年份" prop="planYear">
          <el-date-picker
            v-model="queryParams.planYear"
            type="year"
            value-format="yyyy"
            placeholder="选择年份"
            class="glass-input"
            clearable
          />
        </el-form-item>
        <el-form-item label="计划月份" prop="planMonth">
          <el-select v-model="queryParams.planMonth" placeholder="选择月份" clearable class="glass-input">
            <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="培训主题" prop="subject">
          <el-input v-model="queryParams.subject" placeholder="模糊搜索培训主题" clearable class="glass-input" @keyup.enter.native="handleQuery"/>
        </el-form-item>
        <el-form-item label="培训部门" prop="hostDeptName">
          <el-input v-model="queryParams.hostDeptName" placeholder="模糊搜索培训部门名称" clearable class="glass-input" @keyup.enter.native="handleQuery"/>
        </el-form-item>
        <el-form-item label="完成情况" prop="completionStatus">
          <el-select v-model="queryParams.completionStatus" placeholder="选择完成情况" clearable class="glass-input" style="min-width: 110px;">
            <el-option label="已完成" value="completed" />
            <el-option label="未完成" value="incomplete" />
            <el-option label="逾期" value="overdue" />
          </el-select>
        </el-form-item>
        <el-form-item label="培训级别" prop="trainLevel">
          <el-select v-model="queryParams.trainLevel" placeholder="选择级别" clearable class="glass-input" style="min-width: 100px;">
            <el-option label="公司级" value="公司级" />
            <el-option label="部门级" value="部门级" />
          </el-select>
        </el-form-item>
        <el-form-item class="search-btn-group">
          <el-button type="primary" icon="el-icon-search" size="mini" class="glass-btn primary" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" class="glass-btn" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-panel table-panel">
      <el-row :gutter="10" class="mb8 toolbar-row">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="el-icon-plus" size="mini" class="glass-btn primary-plain" @click="handleAdd" v-hasPermi="['biz:plan:add']">新增计划</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="el-icon-edit" size="mini" class="glass-btn success-plain" :disabled="single" @click="handleUpdate" v-hasPermi="['biz:plan:edit']">修改</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="el-icon-delete" size="mini" class="glass-btn danger-plain" :disabled="multiple" @click="handleDelete" v-hasPermi="['biz:plan:remove']">删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" icon="el-icon-delete-solid" size="mini" class="glass-btn danger" @click="handleDeleteAll" v-hasPermi="['biz:plan:remove']">全部删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-dropdown trigger="click">
            <el-button type="primary" plain size="mini" class="glass-btn primary-plain" v-hasPermi="['biz:plan:export']">
              导出部门级 <i class="el-icon-arrow-down el-icon--right"></i>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <div style="padding: 10px 16px; min-width: 200px;">
                <p style="margin:0 0 8px 0; font-size:12px; color:#909399;">选择年份：</p>
                <el-select v-model="deptYear" placeholder="请选择年份" size="mini" style="width:100%;">
                  <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
                </el-select>
                <el-button type="primary" size="mini" style="width:100%; margin-top:8px;" @click="doExportDept">确认下载</el-button>
              </div>
            </el-dropdown-menu>
          </el-dropdown>
        </el-col>
        <el-col :span="1.5">
          <el-dropdown trigger="click">
            <el-button type="success" plain size="mini" class="glass-btn success-plain" v-hasPermi="['biz:plan:export']">
              导出公司级 <i class="el-icon-arrow-down el-icon--right"></i>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <div style="padding: 10px 16px; min-width: 200px;">
                <p style="margin:0 0 8px 0; font-size:12px; color:#909399;">选择年份：</p>
                <el-select v-model="companyYear" placeholder="请选择年份" size="mini" style="width:100%;">
                  <el-option v-for="y in yearOptions" :key="y" :label="y + '年'" :value="y" />
                </el-select>
                <el-button type="success" size="mini" style="width:100%; margin-top:8px;" @click="doExportCompany">确认下载</el-button>
              </div>
            </el-dropdown-menu>
          </el-dropdown>
        </el-col>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="planList" @selection-change="handleSelectionChange" class="glass-table">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="序号" align="center" width="60">
          <template slot-scope="scope">
            {{ (queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column label="年份" align="center" prop="planYear" width="80">
          <template slot-scope="scope">
            <span class="elegant-tag">{{ scope.row.planYear }}</span>
          </template>
        </el-table-column>
        <el-table-column label="月份" align="center" prop="planMonth" min-width="100">
          <template slot-scope="scope">{{ scope.row.planMonth || '-' }}</template>
        </el-table-column>
        <el-table-column label="计划类型" align="center" prop="planType" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.planType === '1' ? 'success' : 'warning'" size="small">
              {{ scope.row.planType === '1' ? '计划内' : '计划外' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="级别" align="center" prop="trainLevel" width="80" />
        <el-table-column label="培训课时" align="center" prop="projectCategory" width="120" />
        <el-table-column label="培训主题" align="left" prop="subject" show-overflow-tooltip min-width="180" />
        <el-table-column label="培训部门" align="center" prop="hostDeptName" width="130" />
        <el-table-column label="培训对象" align="left" width="200" class-name="audience-text-col">
          <template slot-scope="scope">
            <div class="audience-cell">{{ audienceLabel(scope.row) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="90">
          <template slot-scope="scope">
            <el-tag :type="statusTypeFilter(scope.row)" size="small">
              {{ statusLabelFilter(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="140" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:plan:edit']">修改</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(scope.row)" v-hasPermi="['biz:plan:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="onPaginationChange" class="glass-pagination" />
    </div>

    <el-dialog :title="title" :visible.sync="open" width="680px" append-to-body custom-class="glass-dialog" :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="elegant-form">
        <el-row>
          <el-col :span="12">
            <el-form-item label="计划年份" prop="planYear">
              <el-date-picker v-model="form.planYear" type="year" value-format="yyyy" placeholder="选择年份" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划月份" prop="planMonth">
              <el-input v-model="form.planMonth" placeholder="如 2026年6月 / 6月 / 不定时" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="计划类型" prop="planType">
              <el-select v-model="form.planType" placeholder="选择计划类型" style="width: 100%;">
                <el-option label="计划内" value="1" />
                <el-option label="计划外" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="培训级别" prop="trainLevel">
              <el-select v-model="form.trainLevel" placeholder="选择培训级别" style="width: 100%;">
                <el-option label="公司级" value="公司级" />
                <el-option label="部门级" value="部门级" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="培训课时" prop="projectCategory">
          <el-input v-model="form.projectCategory" placeholder="例如：2h、4学时" />
        </el-form-item>

        <el-form-item label="培训主题" prop="subject">
          <el-input v-model="form.subject" placeholder="请输入核心培训主题" />
        </el-form-item>
        
        <el-form-item label="培训部门" prop="hostDeptId">
          <div style="display: flex; gap: 8px; width: 100%;">
            <treeselect
              v-model="form.hostDeptId"
              :options="deptOptions"
              :show-count="true"
              placeholder="可选：从架构树中选择培训部门，或点右侧按钮扫描纸质表"
              :clearable="true"
              style="flex: 1;"
              @select="handleDeptSelect"
            />
            <el-button
              icon="el-icon-camera"
              size="small"
              :loading="deptOcrLoading"
              @click="triggerDeptScan"
            >扫描识别</el-button>
          </div>
          <input
            ref="deptOcrInput"
            type="file"
            accept="image/jpeg,image/png,image/jpg"
            style="display:none"
            @change="onDeptScanFilePicked"
          />
          <div v-if="deptOcrRawText" class="dept-ocr-hint" style="margin-top: 4px; font-size: 12px; color: #909399;">
            <i class="el-icon-info"></i>
            识别原文：<b>{{ deptOcrRawText }}</b>
            <span v-if="deptOcrMatchType === 'fuzzy'">（未精确匹配，请从下方候选中选择）</span>
            <span v-if="deptOcrMatchType === 'none'" style="color:#e6a23c;">（未匹配到部门，请手动选择或重新拍照）</span>
          </div>
        </el-form-item>

        <el-divider content-position="left">受众配置</el-divider>

        <el-form-item label="培训对象" prop="audienceScope">
          <el-radio-group v-model="form.audienceScope" @change="handleScopeChange">
            <el-radio label="0">全体员工</el-radio>
            <el-radio label="1">指定部门</el-radio>
            <el-radio label="2">指定人员</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="选择部门" prop="audienceIdsArray" v-if="form.audienceScope === '1'">
          <treeselect v-model="form.audienceIdsArray" :multiple="true" :options="deptOptions" :show-count="true" placeholder="请多选参与培训的部门" />
        </el-form-item>

        <el-form-item label="选择人员" prop="audienceIdsArray" v-if="form.audienceScope === '2'">
          <el-select v-model="form.audienceIdsArray" multiple filterable placeholder="支持拼音/汉字搜索并多选人员" style="width: 100%;">
            <el-option v-for="user in userOptions" :key="user.userId" :label="user.nickName + ' ('+user.userName+')'" :value="user.userId" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">附加信息</el-divider>

        <el-form-item label="计划状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">未完成</el-radio>
            <el-radio label="1">已完成</el-radio>
            <el-radio label="2">已取消</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注说明" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="可选填..." />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="glass-btn primary" @click="submitForm">确 定</el-button>
        <el-button class="glass-btn" @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 培训部门 OCR 候选弹窗（OCR 模糊匹配时弹出，让用户从候选中选一个） -->
    <el-dialog
      title="请选择识别到的部门"
      :visible.sync="deptOcrCandidatesDialogVisible"
      width="420px"
      append-to-body
    >
      <el-radio-group v-model="deptOcrSelectedCandidate" style="display:flex;flex-direction:column;gap:8px;">
        <el-radio v-for="c in deptOcrCandidates" :key="c.deptId" :label="c">
          {{ c.deptName }}
          <span style="color:#999;font-size:12px;">(编辑距离 {{ c.score }})</span>
        </el-radio>
      </el-radio-group>
      <div slot="footer">
        <el-button @click="deptOcrCandidatesDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCandidate">确认</el-button>
      </div>
    </el-dialog>

    <!-- 扫表弹窗 -->
    <el-dialog title="扫描培训计划表" :visible.sync="scanOpen" width="900px" append-to-body custom-class="glass-dialog" :close-on-click-modal="false" @opened="handleScanOpened" @close="handleScanClosed">
      <div style="margin-bottom: 12px; display: flex; align-items: center;">
        <span style="margin-right: 12px; color: #606266; font-size: 13px;">扫描类型：</span>
        <el-radio-group v-model="scanLevel" size="small">
          <el-radio-button label="dept">部门级（如采供部等单一部门）</el-radio-button>
          <el-radio-button label="company">公司级（集团培训总计划）</el-radio-button>
        </el-radio-group>
      </div>
      <el-tabs v-model="scanMethod" type="border-card" class="glass-tabs mb20">
        <el-tab-pane label="高拍仪连续拍摄" name="camera">
          <div class="scan-layout">
            <!-- 左侧：拍摄控制 -->
            <div class="scan-panel scan-panel-left">
              <div class="scan-panel-header">
                <i class="el-icon-video-camera-solid"></i>
                <span>实时拍摄</span>
              </div>
              <div class="camera-window">
                <camera-preview-panel
                  ref="cameraPanel"
                  :service-url="'http://127.0.0.1:38088'"
                  :rotation-angle="scanRotationAngle"
                  @ready="onCameraReady"
                  @stream-broken="onCameraBroken"
                  @launch-failed="onCameraLaunchFailed"
                />
              </div>
              <!-- 控制栏 -->
              <div class="camera-controls">
                <div class="control-row">
                  <span class="ctrl-label"><i class="el-icon-refresh"></i> 拍摄方向</span>
                  <el-button-group>
                    <el-button size="mini" icon="el-icon-refresh-left" @click="scanRotateLeft">左旋</el-button>
                    <el-button size="mini" @click="scanRotateRight">右旋 <i class="el-icon-refresh-right"></i></el-button>
                  </el-button-group>
                </div>
                <div class="control-row">
                  <span class="ctrl-label"><i class="el-icon-crop"></i> 裁切模式</span>
                  <el-radio-group v-model="cameraConfig.cutType" size="mini">
                    <el-radio-button label="0">不裁切</el-radio-button>
                    <el-radio-button label="1">自动切边</el-radio-button>
                  </el-radio-group>
                </div>
              </div>
              <div class="capture-btn-wrap">
                <el-button type="primary" icon="el-icon-camera-solid" class="capture-btn" :disabled="!cameraReady" @click="handleScanCapture">
                  拍 照 入 库
                </el-button>
              </div>
            </div>

            <!-- 右侧：已拍摄列表 -->
            <div class="scan-panel scan-panel-right">
              <div class="scan-panel-header">
                <i class="el-icon-files"></i>
                <span>已拍摄表单</span>
                <el-tag type="primary" size="mini" effect="plain" style="margin-left: auto;">{{ scanImages.length }} 页</el-tag>
              </div>
              <div class="gallery-window">
                <div v-if="scanImages.length === 0" class="empty-gallery">
                  <i class="el-icon-picture-outline" style="font-size: 48px;"></i>
                  <p>暂无照片</p>
                  <p style="font-size: 12px;">请点击左侧「拍照入库」</p>
                </div>
                <div v-else class="image-list">
                  <div class="img-item" v-for="(img, index) in scanImages" :key="index">
                    <img :src="img" />
                    <div class="img-overlay" @click="removeScanImage(index)">
                      <i class="el-icon-delete"></i>
                    </div>
                    <div class="page-badge">第 {{ index + 1 }} 页</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="本地文件选取" name="local">
           <el-upload
             ref="uploadRef"
             class="upload-area"
             drag
             action="#"
             :auto-upload="false"
             multiple
             :on-change="handleLocalFileChange"
             :on-remove="handleLocalFileRemove"
             :file-list="localFileList"
             accept=".jpg,.jpeg,.png,.xls,.xlsx"
             style="display: none;"
           />
           <!-- 拖拽上传区 -->
           <div class="upload-drop-zone" @click="$refs.uploadRef.$refs['upload-inner'].handleClick()">
             <i class="el-icon-upload"></i>
             <div class="upload-drop-text">将纸质表单图片或 Excel 文件拖到此处，或<em>点击选取</em></div>
             <div class="upload-drop-tip">支持 JPG/PNG 图片或 .xls/.xlsx Excel 文件</div>
           </div>
           <!-- 自定义文件卡片列表 -->
           <div class="file-card-grid" v-if="localFileList.length > 0">
             <div class="file-card" v-for="(file, index) in localFileList" :key="index">
               <div class="file-thumb" v-if="isImageFile(file)">
                 <img :src="getFileThumb(file)" />
               </div>
               <div class="file-thumb file-thumb-excel" v-else>
                 <i class="el-icon-document"></i>
                 <span>Excel</span>
               </div>
               <div class="file-info">
                 <div class="file-name">{{ file.name }}</div>
                 <div class="file-size">{{ formatFileSize(file.size) }}</div>
               </div>
               <div class="file-remove" @click="handleFileRemove(index)">
                 <i class="el-icon-close"></i>
               </div>
             </div>
           </div>
        </el-tab-pane>
      </el-tabs>
      <div slot="footer" class="dialog-footer" style="text-align: center;">
        <el-button type="primary" icon="el-icon-upload" style="width: 260px; height: 40px; font-size: 15px; font-weight: 600; letter-spacing: 2px; border-radius: 10px;" @click="submitScanUpload" :loading="isScanning" :disabled="isScanSubmitDisabled">提 交</el-button>
      </div>
    </el-dialog>

    <!-- Excel 校对工作台 -->
    <el-dialog title="年度培训计划 - 人工校对确认" :visible.sync="excelReviewOpen" fullscreen append-to-body custom-class="glass-dialog" top="0vh">
      <div class="excel-review-workspace">
        <div class="review-left glass-panel">
          <div class="view-header"><span><i class="el-icon-document"></i> Excel 文件信息</span></div>
          <div class="excel-file-info">
            <div class="excel-icon-wrapper">
              <i class="el-icon-document-copy"></i>
            </div>
            <div class="excel-file-name">{{ excelReviewFileName }}</div>
            <div class="excel-stats">
              <el-alert type="info" :closable="false" show-icon>
                <template slot="title">
                  <span>共识别 <b>{{ excelReviewPlans.length }}</b> 条培训计划</span>
                  <span style="margin-left: 20px; color: #67C23A;">公司级 <b>{{ excelReviewStats.companyCount }}</b> 条</span>
                  <span style="margin-left: 12px; color: #E6A23C;">部门级 <b>{{ excelReviewStats.deptCount }}</b> 条</span>
                </template>
              </el-alert>
            </div>
            <div class="review-tip">
              <i class="el-icon-info"></i>
              提示：请逐行核对计划信息。无效或重复的行可点击"作废"剔除，确认无误后点击底部"确认导入入库"。
            </div>
          </div>
        </div>

        <div class="review-right glass-panel">
          <div class="view-header">
            <span><i class="el-icon-s-grid"></i> 计划明细校对</span>
            <el-tag type="info" size="small" style="float: right; margin-top: 12px;">
              共 {{ excelReviewPlans.length }} 条
            </el-tag>
          </div>
          <div class="review-table-wrapper">
            <el-table :data="excelReviewPlans" height="100%" v-loading="excelReviewLoading" style="width: 100%;" class="glass-table" size="small">
              <el-table-column label="序号" width="60" align="center">
                <template slot-scope="scope">
                  <span :style="{ 'text-decoration': scope.row.uiDeleted ? 'line-through' : 'none', color: scope.row.uiDeleted ? '#ccc' : '#333' }">{{ reviewSeq(scope.$index) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="subject" label="培训主题" min-width="160" align="left">
                <template slot-scope="scope">
                  <el-input v-if="!scope.row.uiDeleted" v-model="scope.row.subject" size="mini" placeholder="培训主题" />
                  <span v-else :style="{ 'text-decoration': 'line-through', color: '#ccc' }">{{ scope.row.subject }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="planMonth" label="计划月份" width="140" align="center">
                <template slot-scope="scope">
                  <el-input v-if="!scope.row.uiDeleted" v-model="scope.row.planMonth" size="mini" placeholder="如 2026年6月" />
                  <span v-else :style="{ 'text-decoration': 'line-through', color: '#ccc' }">{{ scope.row.planMonth || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="trainLevel" label="级别" width="80" align="center">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.trainLevel === '公司级' ? 'success' : 'warning'" size="mini">{{ scope.row.trainLevel }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="hostDeptName" label="培训部门" min-width="130" align="center">
                <template slot-scope="scope">
                  <el-input v-if="!scope.row.uiDeleted" v-model="scope.row.hostDeptName" size="mini" placeholder="培训部门" />
                  <span v-else :style="{ 'text-decoration': 'line-through', color: '#ccc' }">{{ scope.row.hostDeptName }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="projectCategory" label="培训课时" min-width="110" align="center">
                <template slot-scope="scope">
                  <el-input v-if="!scope.row.uiDeleted" v-model="scope.row.projectCategory" size="mini" placeholder="分类" />
                  <span v-else :style="{ 'text-decoration': 'line-through', color: '#ccc' }">{{ scope.row.projectCategory }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="audienceScopeText" label="培训对象" width="90" align="center">
                <template slot-scope="scope">
                  <span v-if="!scope.row.uiDeleted">
                    <el-tooltip v-if="scope.row.audienceText" :content="scope.row.audienceText" placement="top" effect="light">
                      <el-tag size="mini" :type="audienceTagType(scope.row)">
                        {{ audienceLabel(scope.row) }}
                      </el-tag>
                    </el-tooltip>
                    <el-tag v-else size="mini" :type="audienceTagType(scope.row)">
                      {{ audienceLabel(scope.row) }}
                    </el-tag>
                  </span>
                  <span v-else :style="{ 'text-decoration': 'line-through', color: '#ccc' }">-</span>
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" min-width="100" align="center">
                <template slot-scope="scope">
                  <el-input v-if="!scope.row.uiDeleted" v-model="scope.row.remark" size="mini" placeholder="备注" />
                  <span v-else :style="{ 'text-decoration': 'line-through', color: '#ccc' }">{{ scope.row.remark }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" align="center" fixed="right">
                <template slot-scope="scope">
                  <el-button v-if="!scope.row.uiDeleted" type="text" style="color: #F56C6C;" icon="el-icon-delete" size="mini" @click="$set(scope.row, 'uiDeleted', true)">作废</el-button>
                  <el-button v-else type="text" style="color: #909399;" icon="el-icon-refresh-left" size="mini" @click="$set(scope.row, 'uiDeleted', false)">恢复</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="review-footer">
            <div class="review-tip-text">
              <i class="el-icon-info"></i> 已作废 {{ excelReviewPlans.filter(p => p.uiDeleted).length }} 条，确认导入将只入库有效记录
            </div>
            <div style="text-align: right; margin-top: 15px;">
              <el-button @click="excelReviewOpen = false" class="glass-btn">取 消</el-button>
              <el-button type="success" icon="el-icon-s-claim" style="font-size: 15px; font-weight: bold;" :loading="excelReviewLoading" @click="handleConfirmExcelImport">
                确认导入入库
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- OCR 扫表批次校对工作台 -->
    <el-dialog title="扫表批次 - 人工智能校对工作台" :visible.sync="detailOpen" fullscreen append-to-body custom-class="glass-dialog" top="0vh">
      <div class="workspace-container">
        <div class="left-view glass-panel">
          <div class="view-header"><span><i class="el-icon-picture"></i> 原始纸质凭证 (共 {{ logList.length }} 页)</span></div>
          <div class="carousel-wrapper">
            <el-carousel trigger="click" height="70vh" :autoplay="false" indicator-position="outside">
              <el-carousel-item v-for="log in logList" :key="log.logId">
                <el-image :src="getRealImageUrl(log.fileUrl)" fit="contain" style="width:100%; height:100%;" :preview-src-list="[getRealImageUrl(log.fileUrl)]">
                  <div slot="error" class="image-slot" style="display:flex; justify-content:center; align-items:center; height:100%; flex-direction:column; background:#f4f4f5; color:#909399;">
                    <i class="el-icon-picture-outline" style="font-size:40px; margin-bottom:10px;"></i>
                    <span>图片加载失败</span>
                    <span style="font-size:12px; margin-top:5px; color:#F56C6C; padding:0 20px; text-align:center; word-break:break-all;" :title="'数据库原始路径: ' + log.fileUrl">
                      实际请求路径：{{ getRealImageUrl(log.fileUrl) }}
                      <br><span style="color:#999; font-size:11px;">原始路径：{{ log.fileUrl }}</span>
                    </span>
                  </div>
                </el-image>
              </el-carousel-item>
            </el-carousel>
          </div>
        </div>

        <div class="right-view glass-panel">
          <div class="view-header">
            <span><i class="el-icon-document"></i> 基础培训信息校对</span>
            <el-tag v-if="currentBatch.status === '2'" type="success" effect="dark" size="small" style="float:right; margin-top:12px;">该批次已生成台账</el-tag>
            <el-tag v-else type="warning" effect="dark" size="small" style="float:right; margin-top:12px;">待核对修正</el-tag>
          </div>
          <div class="batch-meta-form">
            <el-form :model="currentBatch" size="mini" label-width="70px" :disabled="currentBatch.status !== '1'">
              <el-row :gutter="15">
                <el-col :span="12"><el-form-item label="培训主题"><el-input v-model="currentBatch.ocrSubject" placeholder="核对主题" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="培训部门"><el-input v-model="currentBatch.ocrHostDept" placeholder="核对培训部门" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="授课人"><el-input v-model="currentBatch.ocrSpeaker" placeholder="核对讲师" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="培训地点"><el-input v-model="currentBatch.ocrLocation" placeholder="核对地点" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="培训学时"><el-input v-model="currentBatch.ocrTrainHours" placeholder="如 2.0" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="原始日期"><el-input v-model="currentBatch.ocrDate" placeholder="纸质原填日期" /></el-form-item></el-col>
                <el-col :span="16"><el-form-item label="培训内容"><el-input v-model="currentBatch.ocrTrainContent" placeholder="核对提取出的培训大纲内容" /></el-form-item></el-col>
              </el-row>
            </el-form>
          </div>

          <div class="view-header" style="border-top:1px solid rgba(0,0,0,0.05); margin-top:5px;">
            <span><i class="el-icon-user-solid"></i> 人员明细校对 <el-tag type="info" size="mini" style="margin-left:10px;">共记录 {{ resultList.length }} 人</el-tag></span>
          </div>
          <div class="table-wrapper">
            <el-table :data="resultList" height="100%" v-loading="detailLoading" style="width:100%;" class="glass-table" size="small">
              <el-table-column prop="signSeq" label="签到序号" width="90" align="center">
                <template slot-scope="scope">
                  <el-input v-if="currentBatch.status === '1'" v-model="scope.row.signSeq" size="mini" />
                  <span v-else>{{ scope.row.signSeq }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="parsedName" label="识别姓名" min-width="100" align="center">
                <template slot-scope="scope">
                  <el-input v-if="currentBatch.status === '1' && !scope.row.uiDeleted" v-model="scope.row.parsedName" size="mini" />
                  <span v-else :style="{ 'text-decoration': scope.row.uiDeleted ? 'line-through' : 'none', color: scope.row.uiDeleted ? '#ccc' : '#333', 'font-weight': 'bold' }">{{ scope.row.parsedName }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="parsedDept" label="识别部门" min-width="140" align="center">
                <template slot-scope="scope">
                  <el-input v-if="currentBatch.status === '1' && !scope.row.uiDeleted" v-model="scope.row.parsedDept" size="mini" />
                  <span v-else :style="{ 'text-decoration': scope.row.uiDeleted ? 'line-through' : 'none', color: scope.row.uiDeleted ? '#ccc' : '#666' }">{{ scope.row.parsedDept }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="parsedScore" label="考核成绩/签到" min-width="110" align="center">
                <template slot-scope="scope">
                  <el-input v-if="currentBatch.status === '1' && !scope.row.uiDeleted" v-model="scope.row.parsedScore" size="mini" />
                  <span v-else><el-tag size="small" :type="scope.row.uiDeleted ? 'info' : 'success'">{{ scope.row.parsedScore }}</el-tag></span>
                </template>
              </el-table-column>
              <el-table-column prop="isNewUser" label="系统状态" width="90" align="center">
                <template slot-scope="scope">
                  <el-tooltip v-if="scope.row.isNewUser === '1'" content="系统查无此人，结算时将自动为其创建账号" placement="top">
                    <el-tag :type="scope.row.uiDeleted ? 'info' : 'danger'" size="mini">新人员</el-tag>
                  </el-tooltip>
                  <el-tag v-else :type="scope.row.uiDeleted ? 'info' : 'success'" size="mini">老员工</el-tag>
                </template>
              </el-table-column>
              <el-table-column v-if="currentBatch.status === '1'" label="操作" width="90" align="center" fixed="right">
                <template slot-scope="scope">
                  <el-button v-if="!scope.row.uiDeleted" type="text" style="color:#F56C6C;" icon="el-icon-delete" size="mini" @click="$set(scope.row, 'uiDeleted', true)">作废</el-button>
                  <el-button v-else type="text" style="color:#909399;" icon="el-icon-refresh-left" size="mini" @click="$set(scope.row, 'uiDeleted', false)">恢复</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="confirm-footer" v-if="currentBatch.status === '1'">
            <div class="tips">
              <i class="el-icon-info"></i> 提示：对照左侧原始凭证可修改明细。无关空数据请点击"作废"。
            </div>
            <div style="text-align:right; margin-top:15px;">
              <el-button @click="detailOpen = false" class="glass-btn">暂 存 退 出</el-button>
              <el-button type="success" icon="el-icon-s-claim" style="font-size:15px; font-weight:bold;" :loading="confirmLoading" @click="handleConfirmOcrImport">生成考勤与台账</el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- OCR 轮询等待遮罩 -->
    <div v-if="ocrPollLoading" class="ocr-poll-overlay">
      <div class="ocr-poll-box">
        <i class="el-icon-loading" style="font-size: 48px; color: #1890ff;"></i>
        <p class="ocr-poll-title">图片正在解析中...</p>
        <p class="ocr-poll-sub">解析完成后将自动打开校对工作台</p>
      </div>
    </div>
  </div>
</template>

<script>
import { listPlan, getPlan, delPlan, addPlan, updatePlan, importPlanExcel, previewExcel, confirmExcelImport, previewPlanImage, exportTemplate, exportDeptPlan, exportCompanyPlan, deleteAllPlan } from "@/api/biz/plan";
import { listUser, deptTreeSelect } from "@/api/system/user";
import { uploadBatch, getBatch, listBatchLog, listBatchResult, confirmAndGenerate, updateBatch } from "@/api/biz/batch";
import { recognizeHostDept } from "@/api/biz/ocr";
import axios from "axios";
import { blobValidate } from "@/utils/ruoyi";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";
import CameraPreviewPanel from '@/components/CameraPreviewPanel';

export default {
  name: "Plan",
  components: { CameraPreviewPanel },
  components: { Treeselect },
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      planList: [],
      title: "",
      open: false,

      // 扫表相关
      scanOpen: false,
      scanMethod: 'camera',
      isScanning: false,
      localFileList: [],
      cameraReady: false,
      scanRotationAngle: 0,
      cameraConfig: { cutType: '1', colorType: '0', jpgQuality: 80 },
      scanImages: [],
      // 扫描级别：dept=部门级（如采供部），company=公司级（集团总计划）
      scanLevel: 'dept',

      // 培训部门 OCR 快速识别
      deptOcrLoading: false,
      deptOcrRawText: '',
      deptOcrMatchType: '',
      deptOcrCandidatesDialogVisible: false,
      deptOcrCandidates: [],
      deptOcrSelectedCandidate: null,

      // Excel 校对工作台相关
      excelReviewOpen: false,
      excelReviewLoading: false,
      excelReviewPlans: [],
      excelReviewStats: { companyCount: 0, deptCount: 0 },
      excelReviewFileName: '',

      // OCR 扫表轮询 + 校对工作台
      ocrPollLoading: false,
      ocrPollBatchId: null,
      detailOpen: false,
      detailLoading: false,
      confirmLoading: false,
      currentBatch: {},
      logList: [],
      resultList: [],

      // 数据字典源
      stats: { total: 0, completed: 0, pending: 0 },
      deptOptions: [],
      userOptions: [],
      // 导出模板相关
      templateYear: new Date().getFullYear(),
      yearOptions: [],
      // 导出部门级/公司级相关
      deptYear: new Date().getFullYear(),
      companyYear: new Date().getFullYear(),

      queryParams: {
        pageNum: 1,
        pageSize: 10,
        planYear: null,
        planMonth: null,
        subject: null,
        hostDeptName: null,
        trainLevel: null,
        status: null,
        completionStatus: null,
      },
      // 完成情况=未完成/逾期 时，按 isOverdue 前端二次过滤；其他场景走后端分页
      needClientFilter: false,
      // 前端过滤后的全量数组（仅 needClientFilter=true 时使用）
      allFilteredRows: null,
      form: {
        audienceScope: '0',
        audienceIdsArray: [], // 中转数组：解决下拉多选与数据库逗号字符串的冲突
        planType: '1',
        status: '0'
      },
      rules: {
        planYear: [{ required: true, message: "计划年份不能为空", trigger: "change" }],
        planMonth: [{ required: true, message: "请输入计划月份（如 2026年6月）", trigger: "blur" }],
        subject: [{ required: true, message: "培训主题不能为空", trigger: "blur" }],
        audienceIdsArray: [{ type: 'array', required: true, message: "请至少选择一个受众目标", trigger: "change" }]
      }
    };
  },
  computed: {
    isScanSubmitDisabled() {
      if (this.scanMethod === 'camera') {
        return this.scanImages.length === 0;
      }
      return this.localFileList.length === 0;
    }
  },
  created() {
    this.getList();
    this.getTreeselect();
    this.getUserList();
    // 生成年份选项：近5年
    const cur = new Date().getFullYear();
    this.yearOptions = Array.from({ length: 6 }, (_, i) => cur - i);
    this.templateYear = cur;
    this.deptYear = cur;
    this.companyYear = cur;
  },
  watch: {
    scanMethod(val) {
      if (val === 'camera') {
        this.$nextTick(() => { this.initCamera(); });
      }
    }
  },
  methods: {
    /** 解析 plan_month 字符串为 {y, m}。支持 "2026年6月" / "2026年06月" / "6月" */
    parseYearMonth(s) {
      if (!s) return { y: null, m: null }
      const m1 = /^(\d{4})年(\d{1,2})月/.exec(s)
      if (m1) return { y: Number(m1[1]), m: Number(m1[2]) }
      const m2 = /^(\d{1,2})月$/.exec(s)
      if (m2) return { y: null, m: Number(m2[1]) }
      return { y: null, m: null }
    },
    /** 查询列表 */
    getList() {
      this.loading = true;
      listPlan(this.queryParams).then(response => {
        const now = new Date();
        const currentYear = now.getFullYear();
        const currentMonth = now.getMonth() + 1;
        // 与首页预警保持一致：status=0 且计划年月已过 => 已逾期
        let rows = (response.rows || []).map(r => {
          const ym = this.parseYearMonth(r.planMonth);
          return {
            ...r,
            isOverdue: r.status === '0' && (
              (ym.y != null && ym.y < currentYear) ||
              (ym.y === currentYear && ym.m != null && ym.m < currentMonth) ||
              (ym.y == null && ym.m != null && ym.m < currentMonth) // 旧数据"6月"按当月算
            )
          };
        });

        // 完成情况=未完成/逾期 时：前端按 isOverdue 二次过滤，并本地分页
        if (this.needClientFilter) {
          const cs = this.queryParams.completionStatus;
          this.allFilteredRows = rows.filter(r => cs === 'overdue' ? r.isOverdue : !r.isOverdue);
          this.total = this.allFilteredRows.length;
          this.applyLocalPagination();
        } else {
          this.allFilteredRows = null;
          this.total = response.total;
          this.planList = rows;
        }

        this.stats.total = this.total;
        this.stats.completed = rows.filter(i => i.status === '1').length;
        this.stats.pending = rows.filter(i => i.status === '0').length;
        this.loading = false;
      });
    },
    /** 本地分页（仅 needClientFilter=true 时使用）：
     *  按 isOverdue 过滤后的全量数组，按 pageNum/pageSize 切片 */
    applyLocalPagination() {
      if (!this.allFilteredRows) return;
      const start = (this.queryParams.pageNum - 1) * this.queryParams.pageSize;
      const end = start + this.queryParams.pageSize;
      this.planList = this.allFilteredRows.slice(start, end);
    },
    /** 分页事件：根据当前模式走本地分页或后端查询 */
    onPaginationChange() {
      if (this.needClientFilter) {
        this.applyLocalPagination();
      } else {
        this.getList();
      }
    },
    /** 查询部门下拉树结构 */
    getTreeselect() {
      // 核心修复点：调用 user.js 里的 deptTreeSelect 方法
      deptTreeSelect().then(response => {
        this.deptOptions = response.data;
      });
    },
    /** 查询人员列表(拉取全部用于前端下拉搜索) */
    getUserList() {
      listUser({ pageNum: 1, pageSize: 2000 }).then(response => {
        this.userOptions = response.rows;
      });
    },
    /** 培训部门选中事件：自动冗余存储部门名称，防止关联断裂 */
    handleDeptSelect(node) {
      this.form.hostDeptName = node.label;
    },
    /** 触发培训部门 OCR 文件选择 */
    triggerDeptScan() {
      const input = this.$refs.deptOcrInput
      if (input) input.click()
    },
    /** 培训部门 OCR 文件选中回调：调后端识别并按 matchType 分支处理 */
    async onDeptScanFilePicked(e) {
      const file = e && e.target && e.target.files && e.target.files[0]
      if (!file) return
      this.deptOcrLoading = true
      this.deptOcrRawText = ''
      this.deptOcrMatchType = ''
      try {
        const res = await recognizeHostDept(file)
        const data = (res && res.data) || {}
        this.deptOcrRawText = data.recognizedText || ''
        this.deptOcrMatchType = data.matchType
        if (data.matchType === 'exact' && data.deptId) {
          this.form.hostDeptId = data.deptId
          this.form.hostDeptName = data.deptName
          this.$modal.msgSuccess(`已识别培训部门：${data.deptName}`)
        } else if (data.matchType === 'fuzzy' && data.candidates && data.candidates.length) {
          this.deptOcrCandidates = data.candidates
          this.deptOcrSelectedCandidate = data.candidates[0]
          this.deptOcrCandidatesDialogVisible = true
        } else {
          this.$modal.alert(`未识别到部门，识别原文：${data.recognizedText || '（空）'}，请手动选择或重新拍照。`, '识别失败')
        }
      } catch (err) {
        const msg = (err && err.message) || '网络异常'
        this.$modal.msgError('OCR 识别失败：' + msg)
      } finally {
        this.deptOcrLoading = false
        // 清空 value 以允许重选同一文件
        if (e && e.target) e.target.value = ''
      }
    },
    /** 确认候选弹窗中的选择 */
    confirmCandidate() {
      const c = this.deptOcrSelectedCandidate
      if (c && c.deptId) {
        this.form.hostDeptId = c.deptId
        this.form.hostDeptName = c.deptName
        this.$modal.msgSuccess(`已选择：${c.deptName}`)
      }
      this.deptOcrCandidatesDialogVisible = false
    },
    /** 受众范围切换：清空已选的残留数据 */
    handleScopeChange(val) {
      this.form.audienceIdsArray = [];
    },
    // 状态 UI 渲染器（行级）：未完成且计划年月已过 => 已逾期（红色），其余按 status 渲染
    statusLabelFilter(row) {
      if (row && row.isOverdue) return '已逾期';
      const status = row && row.status;
      const map = { '0': '未完成', '1': '已完成', '2': '已取消' };
      return map[status] || '未知';
    },
    statusTypeFilter(row) {
      if (row && row.isOverdue) return 'danger';
      const status = row && row.status;
      const map = { '0': 'primary', '1': 'success', '2': 'info' };
      return map[status] || 'info';
    },
    // 受众范围统一展示：兼容字符串/数字/null，scope 留空时显示 audienceText 原文
    audienceLabel(row) {
      const scope = row.audienceScope;
      if (scope === '0' || scope === 0) return '全体员工';
      if (scope === '1' || scope === 1) return '指定部门';
      if (scope === '2' || scope === 2) return '指定人员';
      return row.audienceText == null ? '-' : row.audienceText;
    },
    // 受众范围对应的 el-tag 类型：scope 留空时降级为 info
    audienceTagType(row) {
      const scope = row.audienceScope;
      if (scope === '0' || scope === 0) return 'success';
      if (scope === '1' || scope === 1) return 'warning';
      if (scope === '2' || scope === 2) return 'primary';
      return 'info';
    },
    // 序号：删除/作废后自动重编号
    // 作废行不显示序号，未作废行从 1 开始顺次编号
    reviewSeq(index) {
      if (this.excelReviewPlans[index] && this.excelReviewPlans[index].uiDeleted) {
        return '-';
      }
      let count = 0;
      for (let i = 0; i <= index; i++) {
        if (!this.excelReviewPlans[i].uiDeleted) count++;
      }
      return count;
    },
    // 主列表序号：支持分页连续编号
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        planId: null,
        planYear: new Date().getFullYear().toString(),
        planMonth: new Date().getFullYear() + '年' + (new Date().getMonth() + 1) + '月', // 默认当前年月
        planType: '1',
        projectCategory: null,
        subject: null,
        trainLevel: '公司级',
        hostDeptId: null,
        hostDeptName: null,
        audienceScope: '0',
        audienceIds: null,
        audienceIdsArray: [], 
        status: '0',
        remark: null
      };
      this.resetForm("form");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      // 完成情况 → 派生 status + 决定是否前端按 isOverdue 再筛
      const cs = this.queryParams.completionStatus;
      if (cs === 'completed') {
        this.queryParams.status = '1';
        this.needClientFilter = false;
        this.queryParams.pageSize = 10;
      } else if (cs === 'incomplete' || cs === 'overdue') {
        this.queryParams.status = '0';
        this.needClientFilter = true;
        this.queryParams.pageSize = 9999; // 拉全部 status=0 的计划，前端按 isOverdue 筛
      } else {
        this.queryParams.status = null;
        this.needClientFilter = false;
        this.queryParams.pageSize = 10;
      }
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.queryParams.pageSize = 10;
      this.needClientFilter = false;
      this.allFilteredRows = null;
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.planId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新增年度培训计划";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const planId = row.planId || this.ids;
      getPlan(planId).then(response => {
        this.form = response.data;
        // 把后端传回的 int 强制转成 String
        this.form.planYear = String(this.form.planYear); 
        
        // 把数据库中 "101,102" 的字符串转成数字数组
        if (this.form.audienceIds && this.form.audienceScope !== '0') {
          this.form.audienceIdsArray = this.form.audienceIds.split(',').map(Number);
        } else {
          this.form.audienceIdsArray = [];
        }
        
        this.open = true;
        this.title = "修改年度培训计划";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 发给后端前：合并数组为字符串
          if (this.form.audienceScope !== '0' && this.form.audienceIdsArray.length > 0) {
            this.form.audienceIds = this.form.audienceIdsArray.join(',');
          } else {
            this.form.audienceIds = null;
          }

          // 发给后端前：转回年份数字
          this.form.planYear = Number(this.form.planYear);

          if (this.form.planId != null) {
            updatePlan(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addPlan(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDeleteAll() {
      this.$modal.confirm('警告！此操作将删除所有符合当前查询条件的数据，且不可恢复！', '确认全部删除').then(() => {
        return deleteAllPlan(this.queryParams);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("全部删除成功");
      }).catch(() => {});
    },
    handleDelete(row) {
      const planIds = row.planId || this.ids;
      this.$modal.confirm('确认要删除选中的培训计划吗？数据将不可恢复！').then(function() {
        return delPlan(planIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleExport() {
      this.download('biz/plan/export', { ...this.queryParams }, `年度培训计划_${new Date().getTime()}.xlsx`);
    },
    handleExportTemplate() {
      // 打开下拉即可选择年份，选择后触发 doExportTemplate
    },
    async doExportTemplate() {
      if (!this.templateYear) {
        this.$message.warning('请先选择年份');
        return;
      }
      try {
        const res = await exportTemplate(this.templateYear);
        const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `年度培训计划_${this.templateYear}年.xlsx`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.$message.success(`模板「${this.templateYear}年」下载成功`);
      } catch (e) {
        this.$message.error('模板下载失败');
      }
    },
    async doExportDept() {
      if (!this.deptYear) {
        this.$message.warning('请先选择年份');
        return;
      }
      try {
        const res = await exportDeptPlan(this.deptYear);
        // 检查返回是否为有效的blob（而非错误JSON）
        if (!blobValidate(res)) {
          const resText = await new Response(res).text();
          const rspObj = JSON.parse(resText);
          this.$message.error(rspObj.msg || '导出失败');
          return;
        }
        const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `年度部门培训计划_${this.deptYear}年.xlsx`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.$message.success(`部门级培训计划「${this.deptYear}年」下载成功`);
      } catch (e) {
        this.$message.error('部门级导出失败');
      }
    },
    async doExportCompany() {
      if (!this.companyYear) {
        this.$message.warning('请先选择年份');
        return;
      }
      try {
        const res = await exportCompanyPlan(this.companyYear);
        // 检查返回是否为有效的blob（而非错误JSON）
        if (!blobValidate(res)) {
          const resText = await new Response(res).text();
          const rspObj = JSON.parse(resText);
          this.$message.error(rspObj.msg || '导出失败');
          return;
        }
        const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `年度公司级培训计划_${this.companyYear}年.xlsx`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.$message.success(`公司级培训计划「${this.companyYear}年」下载成功`);
      } catch (e) {
        this.$message.error('公司级导出失败');
      }
    },

    // ======== 扫表相关方法 ========
    handleScan() {
      this.scanOpen = true;
      this.scanMethod = 'camera';
      this.scanImages = [];
      this.localFileList = [];
    },

    // 弹窗动画完成后初始化高拍仪（与 batch/index.vue 保持一致）
    handleScanOpened() {
      this.cameraReady = false;
      if (this.scanMethod === 'camera') {
        const panel = this.$refs.cameraPanel;
        if (panel) panel.start();
      }
    },

    handleScanClosed() {
      const panel = this.$refs.cameraPanel;
      if (panel) panel.stop();
      this.cameraReady = false;
    },

    onCameraReady() { this.cameraReady = true; },
    onCameraBroken() { this.cameraReady = false; },
    onCameraLaunchFailed() { this.cameraReady = false; },

    scanRotateLeft() {
      this.scanRotationAngle = (this.scanRotationAngle - 90 + 360) % 360;
    },
    scanRotateRight() {
      this.scanRotationAngle = (this.scanRotationAngle + 90) % 360;
    },
    launchService() {
      window.location.href = "cameraServicetis:0";
      // 等待 exe 启动 5 秒，并自动重试一次（exe 冷启动慢）
      let retried = false;
      const tryStart = () => {
        const panel = this.$refs.cameraPanel;
        if (!panel) return;
        panel.clearError();
        panel.start();
      };
      setTimeout(() => {
        tryStart();
        if (!retried) {
          retried = true;
          setTimeout(() => {
            const panel = this.$refs.cameraPanel;
            if (panel && !panel.isReady()) tryStart();
          }, 4000);
        }
      }, 5000);
    },

    // 拍照入库
    handleScanCapture() {
      const panel = this.$refs.cameraPanel;
      if (!panel || !panel.isReady()) {
        this.$message.warning('相机尚未就绪，请稍候');
        return;
      }
      const params = {
        camidx: "0", rotate: "0",
        image_process_info: { cut_type: this.cameraConfig.cutType, color_type: this.cameraConfig.colorType },
        image_storage_info: { jpg_quality: this.cameraConfig.jpgQuality.toString() }
      };
      axios.post('http://127.0.0.1:38088/video=grabimage', JSON.stringify(params), {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      }).then(async res => {
        if (res.data.code === "0" && res.data.images && res.data.images.length > 0) {
          let base64Data = res.data.images[0].base64;
          if (this.scanRotationAngle !== 0) {
            try {
              const rotatedData = await this.rotateBase64(base64Data, this.scanRotationAngle);
              this.scanImages.push(rotatedData);
            } catch (e) {
              this.scanImages.push("data:image/jpeg;base64," + base64Data);
            }
          } else {
            this.scanImages.push("data:image/jpeg;base64," + base64Data);
          }
          this.$message.success("拍摄成功，已加入暂存区");
        } else {
          if (res.data.code === "2" || (res.data.message && res.data.message.includes("占用"))) {
            panel.markOccupied();
            this.$message.warning("设备被占用，请关闭其他正在使用镜头的程序");
          } else {
            this.$message.error("拍摄失败");
          }
        }
      }).catch(err => {
        if (panel) panel.markConnectFail();
      });
    },

    removeScanImage(index) {
      this.scanImages.splice(index, 1);
    },

    rotateBase64(base64Data, angle) {
      return new Promise((resolve, reject) => {
        const img = new Image();
        img.src = "data:image/jpeg;base64," + base64Data;
        img.onload = () => {
          const canvas = document.createElement("canvas");
          const ctx = canvas.getContext("2d");
          if (angle === 90 || angle === 270) {
            canvas.width = img.height; canvas.height = img.width;
          } else {
            canvas.width = img.width; canvas.height = img.height;
          }
          ctx.translate(canvas.width / 2, canvas.height / 2);
          ctx.rotate((angle * Math.PI) / 180);
          ctx.drawImage(img, -img.width / 2, -img.height / 2);
          resolve(canvas.toDataURL("image/jpeg", 0.9));
        };
        img.onerror = reject;
      });
    },

    handleLocalFileChange(file, fileList) {
      this.localFileList = fileList;
    },
    handleLocalFileRemove(file, fileList) {
      this.localFileList = fileList;
    },
    handleFileRemove(index) {
      this.localFileList.splice(index, 1);
    },
    isImageFile(file) {
      const type = file.raw ? file.raw.type : file.type;
      return type && type.startsWith('image/');
    },
    getFileThumb(file) {
      return file.url || (file.raw ? (window.URL ? URL.createObjectURL(file.raw) : '') : '');
    },
    formatFileSize(size) {
      if (!size) return '';
      if (size < 1024) return size + ' B';
      if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB';
      return (size / (1024 * 1024)).toFixed(1) + ' MB';
    },

    fileToBase64(file) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
      });
    },

    async submitScanUpload() {
      // 区分 Excel 文件和图片
      const excelFiles = this.localFileList.filter(f => {
        const lower = f.name.toLowerCase();
        const isXlsx = lower.endsWith('.xlsx');
        const isXls  = lower.endsWith('.xls') && !isXlsx;
        return f.raw.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
            || f.raw.type === 'application/vnd.ms-excel'
            || isXlsx || isXls;
      });
      const imageFiles = this.localFileList.filter(f => f.raw.type && f.raw.type.startsWith('image/'));

      // Excel 文件：调用预览接口，进入校对工作台
      if (excelFiles.length > 0) {
        this.isScanning = true;
        const excelFile = excelFiles[0];
        previewExcel(excelFile.raw).then(res => {
          this.isScanning = false;
          // 附加 uiDeleted 软作废标记，供前端使用
          this.excelReviewPlans = (res.data.plans || []).map(p => ({ ...p, uiDeleted: false }));
          this.excelReviewStats = {
            companyCount: res.data.companyCount || 0,
            deptCount: res.data.deptCount || 0
          };
          this.excelReviewFileName = excelFile.name;
          this.excelReviewOpen = true;
        }).catch(() => {
          this.isScanning = false;
        });
        return;
      }

      // 拍照或本地图片：收集 base64
      let base64List = [];
      if (this.scanMethod === 'camera') {
        base64List = this.scanImages;
      } else {
        for (let file of imageFiles) {
          base64List.push(await this.fileToBase64(file.raw));
        }
      }

      if (base64List.length === 0) {
        this.$message.warning('请先拍摄或选择文件');
        return;
      }

      this.isScanning = true;
      previewPlanImage({ scanLevel: this.scanLevel, base64Images: base64List }).then(res => {
        this.isScanning = false;
        this.scanOpen = false;
        const data = res.data || {};
        this.excelReviewPlans = (data.plans || []).map(p => ({ ...p, uiDeleted: false }));
        this.excelReviewStats = {
          companyCount: data.companyCount || 0,
          deptCount: data.deptCount || 0
        };
        this.excelReviewFileName = '扫描识别';
        this.excelReviewOpen = true;
        if (data.warnings && data.warnings.length) {
          this.$modal.msgWarning(`已识别 ${data.plans.length} 条，部分页存在问题：${data.warnings.join('；')}`);
        }
        // 培训部门 OCR 结果反馈
        // exact：识别到 dept，已自动绑定 deptId + hostDeptName
        // fuzzy：原文预填进 hostDeptName，deptId 未绑定，需要用户从候选里挑或保留
        // none：原文预填进 hostDeptName，deptId 未绑定，可能是个人姓名或部门表里没建
        if (data.trainDept) {
          const td = data.trainDept;
          if (td.matchType === 'exact') {
            this.$modal.msgSuccess(`已自动识别培训部门：${td.deptName}（已绑定部门 ${td.deptId}）`);
          } else if (td.matchType === 'fuzzy') {
            const candNames = (td.candidates || []).map(c => c.deptName).join('、') || '（无候选）';
            this.$modal.msgWarning(`识别为：${td.recognizedText || '空'}，候选：${candNames}。已预填为文本，请在部门树下拉里挑一个或保留原文`);
          } else if (td.matchType === 'none') {
            const txt = td.recognizedText || '';
            if (txt) {
              this.$modal.msgWarning(`识别到"培训部门：${txt}"，但部门表里没找到。已预填为文本，你可以在部门树下拉里手动选一个，或就保留当前文本（例如这栏其实是个人姓名）`);
            } else {
              this.$modal.msgWarning('未识别到表头文字，请手动选择培训部门');
            }
          } else if (td.errorCode) {
            this.$modal.msgError(`百度OCR错误(${td.errorCode})：${td.errorMsg || '未知'}，请手动选择培训部门`);
          }
        }
      }).catch(err => {
        this.isScanning = false;
        const msg = (err && (err.msg || (err.response && err.response.data && err.response.data.msg))) || '未知错误';
        this.$modal.msgError('图片解析失败：' + msg);
      });
    },

    // 轮询等待 OCR 解析完成，状态变为待校对后自动打开校对工作台
    startOcrPoll(batchId) {
      this.ocrPollLoading = true;
      this.ocrPollBatchId = batchId;
      let elapsed = 0;
      const interval = setInterval(() => {
        elapsed += 2000;
        getBatch(batchId).then(res => {
          const batch = res.data;
          if (batch.status === '1') {
            clearInterval(interval);
            this.ocrPollLoading = false;
            this.openOcrReview(batch);
          } else if (batch.status === '3') {
            clearInterval(interval);
            this.ocrPollLoading = false;
            this.$modal.msgError('图片解析异常，请重新上传');
          } else if (elapsed >= 60000) {
            clearInterval(interval);
            this.ocrPollLoading = false;
            this.$modal.msgWarning('解析超时，请前往「扫表批次」页面查看');
          }
        });
      }, 2000);
    },

    openOcrReview(batch) {
      this.currentBatch = JSON.parse(JSON.stringify(batch));
      this.detailOpen = true;
      this.detailLoading = true;
      this.logList = [];
      this.resultList = [];
      listBatchLog({ batchId: batch.batchId }).then(res => { this.logList = res.rows; });
      listBatchResult({ batchId: batch.batchId, pageSize: 500 }).then(res => {
        this.resultList = res.rows.map(item => ({ ...item, uiDeleted: false }));
        this.detailLoading = false;
      }).catch(() => { this.detailLoading = false; });
    },

    handleConfirmOcrImport() {
      const editedList = this.resultList.filter(item => !item.uiDeleted);
      this.$modal.confirm('确认基础信息和人员明细无误，开始生成最终合规台账与考勤？').then(() => {
        this.confirmLoading = true;
        return updateBatch(this.currentBatch);
      }).then(() => {
        return confirmAndGenerate(this.currentBatch.batchId, editedList);
      }).then(() => {
        this.$modal.msgSuccess("极速结算成功！已同步生成台账。");
        this.detailOpen = false;
        this.confirmLoading = false;
        this.getList();
      }).catch(() => {
        this.confirmLoading = false;
      });
    },

    // 将服务器物理路径转为前端可访问的虚拟路径（与 batch/index.vue 保持一致）
    getRealImageUrl(url) {
      if (!url) return '';
      let path = url.replace(/\\/g, '/');
      if (path.startsWith('http://') || path.startsWith('https://')) return path;
      const origin = window.location.origin;
      const ocrIndex = path.indexOf('/ocr/');
      if (ocrIndex !== -1) {
        return origin + '/profile' + path.substring(ocrIndex);
      }
      if (path.indexOf('/profile') !== -1) {
        return origin + path.substring(path.indexOf('/profile'));
      }
      return origin + '/' + path.replace(/^\//, '');
    },

    // 确认导入 Excel 校对后的数据
    handleConfirmExcelImport() {
      const validPlans = this.excelReviewPlans.filter(p => !p.uiDeleted);
      if (validPlans.length === 0) {
        this.$message.warning('没有有效的计划可导入，请恢复或重新上传');
        return;
      }
      this.excelReviewLoading = true;
      // 删除前端临时字段，只提交后端需要的字段
      const plansToSave = validPlans.map(p => {
        const { uiDeleted, audienceScopeText, ...rest } = p;
        return rest;
      });
      confirmExcelImport(plansToSave).then(res => {
        this.excelReviewLoading = false;
        this.excelReviewOpen = false;
        this.scanOpen = false;
        this.$modal.msgSuccess(res.msg || '导入成功');
        this.getList();
      }).catch(() => {
        this.excelReviewLoading = false;
      });
    },

  }
};
</script>

<style scoped>
/* ================= 全局环境 ================= */
.cloud-glass-container {
  min-height: calc(100vh - 84px);
  background: #f0f2f5;
  position: relative;
  z-index: 1;
  padding: 20px;
}

/* ================= 毛玻璃卡片效果 ================= */
.glass-panel {
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  padding: 20px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}
.glass-panel:hover {
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.08);
}

/* ================= 统计大盘卡片 ================= */
.glass-metric-card {
  border: 1px solid rgba(255,255,255,0.9);
  border-radius: 12px;
  background: rgba(255,255,255,0.7);
  padding: 10px;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  height: 90px;
  transition: transform 0.3s;
}
.glass-metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}
.glass-metric-card .icon-wrapper {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 26px;
  margin-left: 15px;
  margin-right: 15px;
  color: #fff;
}
.glass-metric-card .metric-info .label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
  font-weight: bold;
}
.glass-metric-card .metric-info .value {
  font-size: 22px;
  font-weight: 900;
  color: #303133;
}
.glass-metric-card.upload-card {
  background: #1890ff;
  cursor: pointer;
  justify-content: center;
  color: white;
}
.glass-metric-card.upload-card .upload-content {
  text-align: center;
}
.glass-metric-card.upload-card .upload-content i {
  font-size: 30px;
  display: block;
  margin-bottom: 5px;
}
.glass-metric-card.upload-card .upload-content span {
  font-weight: bold;
  font-size: 15px;
  letter-spacing: 1px;
}
.bg-blue { background: #1890ff; }
.bg-green { background: #67c23a; }
.bg-orange { background: #e6a23c; }

/* ================= 搜索面板样式 ================= */
.search-panel { padding: 20px; }

.glass-btn {
  border-radius: 8px;
  font-weight: 500;
  border: none;
  transition: all 0.3s;
}
.glass-btn.primary {
  background: #1890ff;
  color: white;
}
.glass-btn.primary-plain {
  color: #1890ff;
  background: rgba(24, 144, 255, 0.08);
  border: 1px solid rgba(24, 144, 255, 0.2);
}
.glass-btn.success-plain {
  color: #67c23a;
  background: rgba(103, 194, 58, 0.08);
  border: 1px solid rgba(103, 194, 58, 0.2);
}
.glass-btn.warning-plain {
  color: #e6a23c;
  background: rgba(230, 162, 60, 0.08);
  border: 1px solid rgba(230, 162, 60, 0.2);
}
.glass-btn.danger-plain {
  color: #f56c6c;
  background: rgba(245, 108, 108, 0.08);
  border: 1px solid rgba(245, 108, 108, 0.2);
}
.glass-btn.info-plain {
  color: #909399;
  background: rgba(144, 147, 153, 0.08);
  border: 1px solid rgba(144, 147, 153, 0.2);
}
.glass-btn.info-plain:hover {
  color: #606266;
  background: rgba(144, 147, 153, 0.15);
}

::v-deep .glass-input .el-input__inner {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid rgba(200, 200, 200, 0.3) !important;
  border-radius: 8px !important;
}

/* ================= 表格美化 ================= */
.table-panel {
  padding: 0;
  overflow: hidden;
}

.toolbar-row {
  padding: 16px 20px;
  background: rgba(255,255,255,0.5);
  border-bottom: 1px solid rgba(0,0,0,0.05);
}

::v-deep .glass-table {
  background: #ffffff !important;
}
::v-deep .glass-table th,
::v-deep .glass-table tr,
::v-deep .glass-table td {
  background: transparent !important;
  border-bottom: 1px solid rgba(0,0,0,0.04) !important;
}
::v-deep .glass-table th {
  background: rgba(245, 247, 250, 0.4) !important;
  color: #333;
  font-weight: 600 !important;
}
::v-deep .glass-table tr:hover td {
  background: rgba(24, 144, 255, 0.05) !important;
}
::v-deep .glass-table::before {
  display: none;
}
::v-deep .el-table--border td,
::v-deep .el-table--border th {
  border-right: none !important;
}

/* 标签样式 */
.el-tag {
  border-radius: 20px !important;
  padding: 0 12px !important;
  font-weight: 500 !important;
}

/* ================= 弹窗美化 ================= */
::v-deep .glass-dialog .el-dialog__header {
  background: #1890ff;
  padding: 20px !important;
  margin: 0;
}
::v-deep .glass-dialog .el-dialog__title {
  color: white !important;
  font-weight: 600 !important;
}
::v-deep .glass-dialog .el-dialog__headerbtn .el-dialog__close {
  color: white !important;
}
::v-deep .glass-dialog .el-dialog__body {
  padding: 24px !important;
}
::v-deep .glass-dialog .el-dialog__footer {
  padding: 16px 24px !important;
  border-top: 1px solid rgba(0,0,0,0.05);
}

/* ================= 高拍仪扫描布局 ================= */
.scan-layout {
  display: flex;
  gap: 16px;
}
.scan-panel {
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.scan-panel-left { flex: 1.4; display: flex; flex-direction: column; }
.scan-panel-right { flex: 1; display: flex; flex-direction: column; }
.scan-panel-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f2f5;
}
.scan-panel-header i { color: #1890ff; font-size: 16px; }

/* 相机窗口 */
.camera-window {
  width: 100%;
  height: 300px;
  background: #1a1a1a;
  border-radius: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  box-shadow: inset 0 0 20px rgba(0,0,0,0.4);
  margin-bottom: 12px;
}
.camera-window .live-stream {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.camera-window .camera-placeholder {
  color: #909399;
  text-align: center;
}
.camera-window .camera-placeholder p { margin: 0; }

/* 控制栏 */
.camera-controls {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.control-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.ctrl-label {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
  white-space: nowrap;
}
.ctrl-label i { color: #1890ff; margin-right: 3px; }

/* 拍照按钮 */
.capture-btn-wrap { text-align: center; }
.capture-btn {
  width: 100%;
  height: 44px;
  font-size: 16px !important;
  font-weight: 600 !important;
  letter-spacing: 2px;
  background: #1890ff !important;
  border: none !important;
  border-radius: 10px !important;
}
.capture-btn:hover {
  background: #66b1ff !important;
}

/* 相册区 */
.gallery-window {
  flex: 1;
  min-height: 300px;
  background: #fafafa;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  overflow-y: auto;
  padding: 8px;
}
.gallery-window .empty-gallery {
  text-align: center;
  color: #c0c4cc;
  margin-top: 100px;
}
.gallery-window .empty-gallery i { font-size: 48px; color: #1890ff; opacity: 0.25; display: block; margin-bottom: 8px; }
.gallery-window .image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.gallery-window .image-list .img-item {
  width: calc(50% - 4px);
  height: 140px;
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.gallery-window .image-list .img-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.gallery-window .image-list .img-item .page-badge {
  position: absolute;
  top: 5px;
  left: 5px;
  background: #1890ff;
  color: white;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 20px;
  font-weight: 500;
}
.gallery-window .image-list .img-item .img-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.2s;
  cursor: pointer;
}
.gallery-window .image-list .img-item .img-overlay i {
  color: white;
  font-size: 24px;
}
.gallery-window .image-list .img-item:hover .img-overlay {
  opacity: 1;
}

::v-deep .glass-tabs .el-tabs__header {
  background: rgba(255,255,255,0.5);
  border-radius: 8px 8px 0 0;
}
::v-deep .glass-tabs .el-tabs__item.is-active {
  color: #1890ff !important;
  font-weight: 600;
}
::v-deep .glass-tabs .el-tabs__active-bar {
  background: #1890ff !important;
}

/* ================= 本地文件上传区 ================= */
.upload-drop-zone {
  border: 2px dashed rgba(24, 144, 255, 0.35);
  border-radius: 12px;
  background: rgba(24, 144, 255, 0.03);
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 16px;
}
.upload-drop-zone:hover {
  border-color: #1890ff;
  background: rgba(24, 144, 255, 0.06);
}
.upload-drop-zone i {
  font-size: 48px;
  color: #1890ff;
  margin-bottom: 12px;
}
.upload-drop-text {
  font-size: 15px;
  color: #303133;
  margin-bottom: 8px;
}
.upload-drop-text em {
  color: #1890ff;
  font-style: normal;
  font-weight: 600;
}
.upload-drop-tip {
  font-size: 12px;
  color: #909399;
}

/* 文件卡片网格 */
.file-card-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.file-card {
  width: calc(25% - 9px);
  min-width: 120px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: all 0.3s;
  position: relative;
}
.file-card:hover {
  box-shadow: 0 4px 16px rgba(24,144,255,0.15);
  border-color: rgba(24,144,255,0.3);
  transform: translateY(-2px);
}
.file-thumb {
  width: 100%;
  height: 100px;
  background: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}
.file-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.file-thumb-excel {
  flex-direction: column;
  color: #67c23a;
}
.file-thumb-excel i {
  font-size: 36px;
  margin-bottom: 4px;
}
.file-thumb-excel span {
  font-size: 11px;
  font-weight: bold;
  color: #67c23a;
}
.file-info {
  padding: 8px 10px;
}
.file-name {
  font-size: 12px;
  color: #303133;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}
.file-size {
  font-size: 11px;
  color: #c0c4cc;
}
.file-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  background: rgba(245, 108, 108, 0.9);
  border-radius: 50%;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
  font-size: 12px;
}
.file-card:hover .file-remove {
  opacity: 1;
}

/* ================= Excel 校对工作台 ================= */
.excel-review-workspace {
  display: flex;
  gap: 16px;
  height: calc(100vh - 120px);
}
.review-left {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  padding: 16px;
}
.review-left .view-header {
  font-size: 14px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(24, 144, 255, 0.15);
}
.excel-file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}
.excel-icon-wrapper {
  width: 80px;
  height: 80px;
  background: rgba(103, 194, 58, 0.1);
  border-radius: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.excel-icon-wrapper i {
  font-size: 40px;
  color: #67C23A;
}
.excel-file-name {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
  text-align: center;
  word-break: break-all;
}
.excel-stats {
  width: 100%;
}
.review-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
  padding: 10px;
  background: rgba(24, 144, 255, 0.04);
  border-radius: 8px;
  width: 100%;
}
.review-tip i {
  color: #1890ff;
  margin-right: 4px;
}

.review-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 16px;
}
.review-right .view-header {
  font-size: 14px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(24, 144, 255, 0.15);
}
.review-table-wrapper {
  flex: 1;
  overflow: hidden;
}
.review-footer {
  padding-top: 12px;
  border-top: 1px solid rgba(0,0,0,0.05);
}
.review-tip-text {
  font-size: 12px;
  color: #909399;
}
.review-tip-text i {
  color: #E6A23C;
  margin-right: 4px;
}

/* ================= OCR 扫表校对工作台 ================= */
.workspace-container {
  display: flex;
  gap: 16px;
  height: calc(100vh - 120px);
}
.left-view {
  width: 45%;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  padding: 16px;
}
.left-view .view-header {
  font-size: 14px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(24, 144, 255, 0.15);
}
.carousel-wrapper {
  flex: 1;
  overflow: hidden;
}
.batch-meta-form {
  margin-bottom: 10px;
}
.table-wrapper {
  flex: 1;
  overflow: hidden;
}
.confirm-footer {
  padding-top: 12px;
  border-top: 1px solid rgba(0,0,0,0.05);
}
.tips {
  font-size: 12px;
  color: #909399;
  background: rgba(24, 144, 255, 0.04);
  border-radius: 6px;
  padding: 8px 12px;
}
.tips i {
  color: #1890ff;
  margin-right: 4px;
}

.right-view {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 16px;
}
.right-view .view-header {
  font-size: 14px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(24, 144, 255, 0.15);
}

/* ================= OCR 轮询等待遮罩 ================= */
.ocr-poll-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 2000;
  display: flex;
  justify-content: center;
  align-items: center;
}
.ocr-poll-box {
  background: white;
  border-radius: 16px;
  padding: 40px 50px;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
}
.ocr-poll-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 16px 0 8px;
}
.ocr-poll-sub {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

/* ================= 受众范围列：完整显示、自动换行 ================= */
::v-deep .audience-text-col .cell {
  white-space: normal !important;
  padding: 8px 6px;
  line-height: 1.5;
  word-break: break-all;
  text-align: left;
}
.audience-cell {
  white-space: pre-line;
  word-break: break-all;
  line-height: 1.5;
  color: #303133;
  font-size: 13px;
}

/* ================= OCR 校对页 el-image 按 EXIF 自动旋转 ================= */
::v-deep .el-image > img {
  image-orientation: from-image;
}
</style>