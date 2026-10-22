<template>
  <div class="app-container cloud-glass-container">
    <el-row :gutter="20" class="mb20">
      <el-col :span="6">
        <div class="glass-metric-card">
          <div class="icon-wrapper bg-blue"><i class="el-icon-document-copy" /></div>
          <div class="metric-info">
            <div class="label">扫表批次总数</div>
            <div class="value">{{ total }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-metric-card">
          <div class="icon-wrapper bg-green"><i class="el-icon-circle-check" /></div>
          <div class="metric-info">
            <div class="label">已结算入库</div>
            <div class="value" style="color: #67C23A;">{{ stats.success }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-metric-card">
          <div class="icon-wrapper bg-orange"><i class="el-icon-view" /></div>
          <div class="metric-info">
            <div class="label">待人工校对</div>
            <div class="value" style="color: #E6A23C;">{{ stats.pending_confirm }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-metric-card upload-card" @click="handleAdd" v-hasPermi="['biz:batch:add']">
          <div class="upload-content">
            <i class="el-icon-camera cloud-icon"></i>
            <span>启动高拍仪扫表</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="glass-panel search-panel" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="90px">
        <el-form-item label="批次流水号" prop="batchNo">
          <el-input v-model="queryParams.batchNo" placeholder="搜索 OCR 批次号" clearable class="glass-input" @keyup.enter.native="handleQuery"/>
        </el-form-item>
        <el-form-item label="关联计划" prop="planId">
          <el-select v-model="queryParams.planId" placeholder="搜索计划" filterable clearable class="glass-input" style="width: 180px;">
            <el-option v-for="plan in planOptions" :key="plan.planId" :label="plan.subject" :value="plan.planId" />
          </el-select>
        </el-form-item>
        <el-form-item label="识别主题" prop="ocrSubject">
          <el-input v-model="queryParams.ocrSubject" placeholder="模糊搜索提取主题" clearable class="glass-input" @keyup.enter.native="handleQuery"/>
        </el-form-item>
        <el-form-item label="批次状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable class="glass-input" style="width: 140px">
            <el-option label="解析中" value="0" />
            <el-option label="待校对" value="1" />
            <el-option label="已结算" value="2" />
            <el-option label="解析异常" value="3" />
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
          <el-button type="primary" plain icon="el-icon-camera" size="mini" class="glass-btn primary-plain" @click="handleAdd" v-hasPermi="['biz:batch:add']">高拍仪扫表上传</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="warning" plain icon="el-icon-download" size="mini" class="glass-btn warning-plain" @click="handleExport" v-hasPermi="['biz:batch:export']">导出</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="el-icon-delete" size="mini" class="glass-btn danger-plain" :disabled="multiple" @click="handleDelete" v-hasPermi="['biz:batch:remove']">批量移除批次</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" icon="el-icon-delete-solid" size="mini" class="glass-btn danger" @click="handleDeleteAll" v-hasPermi="['biz:batch:remove']">全部删除</el-button>
        </el-col>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="batchList" @selection-change="handleSelectionChange" class="glass-table">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="批次流水号" align="center" prop="batchNo" width="180">
          <template slot-scope="scope">
             <span style="font-family: monospace; font-weight: bold; color: #409EFF;">{{ scope.row.batchNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="扫描页数" align="center" prop="totalPages" width="80">
          <template slot-scope="scope">
             <el-tag type="info" size="mini">{{ scope.row.totalPages }} 页</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="自动提取的主题" align="left" prop="ocrSubject" min-width="160" show-overflow-tooltip>
          <template slot-scope="scope">
             <span class="elegant-text">{{ scope.row.ocrSubject || '未提取到主题' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="提取日期" align="center" prop="ocrDate" width="130">
          <template slot-scope="scope">
             <span>{{ formatOcrDate(scope.row.ocrDate) || scope.row.ocrDate || '' }}</span>
             <el-tag v-if="scope.row.ocrDate && !formatOcrDate(scope.row.ocrDate)" type="danger" size="mini" effect="plain" style="margin-left: 4px;">待复核</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="关联年度计划" align="center" prop="planId" width="150" show-overflow-tooltip>
           <template slot-scope="scope">
              <span>{{ getPlanName(scope.row.planId) }}</span>
           </template>
        </el-table-column>

        <el-table-column label="批次状态" align="center" width="120">
          <template slot-scope="scope">
            <div v-if="scope.row.status === '0'" class="status-processing"><i class="el-icon-loading"></i> AI解析中</div>
            <el-tag v-else-if="scope.row.status === '1'" type="warning" effect="dark" size="small" style="border-radius: 12px;">待人工校对</el-tag>
            <el-tag v-else-if="scope.row.status === '2'" type="success" effect="dark" size="small" style="border-radius: 12px;">已结算入库</el-tag>
            <el-tag v-else-if="scope.row.status === '3'" type="danger" effect="dark" size="small" style="border-radius: 12px;">解析异常</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="备注说明" align="center" prop="remark" min-width="150" show-overflow-tooltip />

        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === '1'" size="mini" type="success" plain icon="el-icon-s-check" class="glass-btn" @click="handleReview(scope.row)">进行校对结算</el-button>
            <el-button v-if="scope.row.status === '2'" size="mini" type="text" icon="el-icon-view" @click="handleReview(scope.row)">查看明细</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(scope.row)" v-hasPermi="['biz:batch:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" class="glass-pagination" />
    </div>

    <el-dialog :title="title" :visible.sync="open" width="900px" append-to-body custom-class="glass-dialog" :close-on-click-modal="false" @opened="handleDialogOpen" @close="handleDialogClose">
      <el-form label-width="100px" class="elegant-form mb10">
         <el-form-item label="关联年度计划" required>
            <el-select v-model="uploadPlanId" placeholder="选填，将本次考勤核销到指定计划" filterable clearable style="width: 100%;">
              <el-option v-for="plan in planOptions" :key="plan.planId" :label="plan.subject" :value="plan.planId" />
            </el-select>
            <div style="font-size: 12px; color: #909399;">如果本次属于计划外临时培训，可以不选。</div>
         </el-form-item>
      </el-form>

      <el-tabs v-model="uploadMethod" type="border-card" class="glass-tabs mb20">
        <el-tab-pane label="高拍仪连续拍摄" name="camera">
          <div class="camera-box">
            <div class="toolbar-panel mb10">
              <div class="toolbar-row">
                 <span class="label">拍摄方向:</span>
                 <el-button-group>
                    <el-button size="mini" icon="el-icon-refresh-left" @click="rotateLeft">左旋</el-button>
                    <el-button size="mini" @click="rotateRight">右旋 <i class="el-icon-refresh-right"></i></el-button>
                 </el-button-group>
                 <span class="label ml15">裁切模式:</span>
                 <el-radio-group v-model="cameraConfig.cutType" size="mini">
                    <el-radio-button label="0">不裁切</el-radio-button>
                    <el-radio-button label="1">自动切边</el-radio-button>
                 </el-radio-group>
              </div>
            </div>

            <el-row :gutter="10">
              <el-col :span="14">
                <div class="preview-header">实时预览</div>
                <camera-preview-panel
                  ref="cameraPanel"
                  :service-url="cameraServiceUrl"
                  :rotation-angle="rotationAngle"
                  @ready="onCameraReady"
                  @stream-broken="onCameraBroken"
                  @launch-failed="onCameraLaunchFailed"
                />
                <div style="text-align: center; margin-top: 15px;">
                   <el-button type="primary" icon="el-icon-camera-solid" style="width: 200px; font-size: 16px;" @click="handleScan" :disabled="!cameraReady">拍 照 入 库</el-button>
                </div>
              </el-col>
              <el-col :span="10">
                 <div class="preview-header">已拍摄的纸质表单 ({{ scanImages.length }} 页)</div>
                 <div class="gallery-window">
                    <div v-if="scanImages.length === 0" class="empty-gallery">
                       <i class="el-icon-files"></i><p>暂无照片，请点击左侧拍照</p>
                    </div>
                    <div v-else class="image-list">
                       <div class="img-item" v-for="(img, index) in scanImages" :key="index">
                          <img :src="img" />
                          <div class="delete-mask" @click="removeScanImage(index)"><i class="el-icon-delete"></i> 移除</div>
                          <div class="page-badge">第 {{ index + 1 }} 页</div>
                       </div>
                    </div>
                 </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <el-tab-pane label="本地文件选取" name="local">
           <el-upload class="upload-demo" drag action="#" :auto-upload="false" multiple :on-change="handleLocalFileChange" :on-remove="handleLocalFileRemove" :file-list="localFileList" accept=".jpg,.jpeg,.png,.xlsx,.xls">
             <i class="el-icon-upload"></i>
             <div class="el-upload__text">将扫描图片或 Excel 表格拖到此处，或<em>点击选取</em></div>
             <div class="el-upload__tip" slot="tip">支持 JPG/PNG 图片或 Excel 表格（.xlsx / .xls），图片将走 OCR 识别，Excel 直接解析入库</div>
           </el-upload>
        </el-tab-pane>
      </el-tabs>

      <div slot="footer" class="dialog-footer" style="text-align: center;">
        <el-button type="primary" class="glass-btn primary" icon="el-icon-upload" style="width: 250px; font-size: 16px;" @click="submitBatchUpload" :loading="isUploading" :disabled="isSubmitDisabled">提 交 智 能 解 析</el-button>
      </div>
    </el-dialog>

    <el-dialog title="考勤台账 - 人工智能校对工作台" :visible.sync="detailOpen" fullscreen append-to-body custom-class="glass-dialog ocr-workspace" top="0vh">
      <div class="workspace-container">
        
        <div class="left-view glass-panel">
          <div class="view-header"><span><i class="el-icon-picture"></i> 原始纸质凭证 (共 {{ logList.length }} 页)</span></div>
          <div class="carousel-wrapper">
             <el-carousel trigger="click" height="70vh" :autoplay="false" indicator-position="outside">
                <el-carousel-item v-for="log in logList" :key="log.logId">
                   <!-- 图片文件：使用 el-image 预览 -->
                   <div v-if="isImageFile(log.fileUrl)" style="width:100%; height:100%; display:flex; align-items:center; justify-content:center; background:#1a1a1a;">
                     <el-image
                       :src="getRealImageUrl(log.fileUrl)"
                       fit="contain"
                       style="width:100%; height:100%"
                       :preview-src-list="[getRealImageUrl(log.fileUrl)]"
                     >
                       <div slot="error" class="image-slot" style="display:flex; justify-content:center; align-items:center; height:100%; flex-direction:column; background: #f4f4f5; color: #909399;">
                          <i class="el-icon-picture-outline" style="font-size: 40px; margin-bottom: 10px;"></i>
                          <span>图片加载失败</span>
                          <span style="font-size: 12px; margin-top: 5px; color: #F56C6C; padding: 0 20px; text-align: center; word-break: break-all;">
                             实际请求路径：{{ getRealImageUrl(log.fileUrl) }}
                             <br><span style="color:#999; font-size:11px;">原始路径：{{ log.fileUrl }}</span>
                          </span>
                       </div>
                     </el-image>
                   </div>
                   <!-- 非图片文件（Excel 等）：显示文件图标 + 下载按钮 -->
                   <div v-else style="width:100%; height:100%; display:flex; flex-direction:column; align-items:center; justify-content:center; background:#f4f4f5; color:#909399;">
                      <i class="el-icon-document" style="font-size: 80px; color:#67C23A; margin-bottom: 20px;"></i>
                      <span style="font-size: 18px; font-weight: bold; color:#303133; margin-bottom: 10px;">{{ getFileName(log.fileUrl) }}</span>
                      <span style="font-size: 13px; margin-bottom: 20px;">Excel 表格，已直接解析入库</span>
                      <el-button type="success" icon="el-icon-download" size="small" @click="downloadFile(log.fileUrl)">下载查看</el-button>
                   </div>
                </el-carousel-item>
             </el-carousel>
          </div>
        </div>

        <div class="right-view glass-panel">
          <!-- 1) 基础培训信息:可折叠,默认收起 -->
          <el-collapse v-model="activeSections" class="meta-collapse">
            <el-collapse-item name="meta">
              <template slot="title">
                <span style="font-weight: bold; color: #303133; display: flex; align-items: center; gap: 8px;">
                  <i class="el-icon-document"></i> 基础培训信息校对
                  <el-tag v-if="currentBatch.status === '2'" type="success" effect="dark" size="mini">该批次已生成台账</el-tag>
                  <el-tag v-else type="warning" effect="dark" size="mini">待核对修正</el-tag>
                </span>
              </template>
              <div class="batch-meta-form">
                <el-form :model="currentBatch" size="mini" label-width="78px" :disabled="currentBatch.status !== '1'">

                  <!-- Section 1: 基础信息 -->
                  <div class="form-section">
                    <div class="form-section-title">基础信息</div>
                    <el-row :gutter="12">
                      <el-col :span="14">
                        <el-form-item label="培训主题">
                          <el-input v-model="currentBatch.ocrSubject" placeholder="核对主题" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="10">
                        <el-form-item label="主办部门">
                          <el-input v-model="currentBatch.ocrHostDept" placeholder="核对主办部门" />
                        </el-form-item>
                      </el-col>
                    </el-row>
                  </div>

                  <!-- Section 2: 时间地点 -->
                  <div class="form-section">
                    <div class="form-section-title">时间地点</div>
                    <el-row :gutter="12">
                      <el-col :span="10">
                        <el-form-item label="培训日期">
                          <el-input v-model="displayOcrDate" placeholder="如 2026-05-11" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="6">
                        <el-form-item label="学时">
                          <el-input v-model="currentBatch.ocrTrainHours" placeholder="如 2.0" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="8">
                        <el-form-item label="培训地点">
                          <el-input v-model="currentBatch.ocrLocation" placeholder="核对地点" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="14">
                        <el-form-item label="授课人">
                          <el-input v-model="currentBatch.ocrSpeaker" placeholder="核对讲师" />
                        </el-form-item>
                      </el-col>
                    </el-row>
                  </div>

                  <!-- Section 3: 人员规模（高亮） -->
                  <div class="form-section form-section-highlight">
                    <div class="form-section-title"><i class="el-icon-user-solid" /> 人员规模</div>
                    <el-row :gutter="12" align="middle">
                      <el-col :span="8">
                        <el-form-item label="应到人数">
                          <el-input v-model.number="currentBatch.ocrTargetCount" placeholder="如 36" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="8">
                        <el-form-item label="实到人数">
                          <el-input v-model.number="currentBatch.ocrActualCount" placeholder="如 34" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="8">
                        <div class="count-info">
                          共记录 <strong>{{ resultList.length }}</strong> 人
                          <span v-if="currentBatch.ocrTargetCount && currentBatch.ocrActualCount != null"
                                :class="['count-diff', currentBatch.ocrTargetCount === currentBatch.ocrActualCount ? 'count-diff-ok' : 'count-diff-warn']">
                            {{ currentBatch.ocrTargetCount === currentBatch.ocrActualCount ? '✓ 一致' : '⚠ 缺席 ' + (currentBatch.ocrTargetCount - currentBatch.ocrActualCount) + ' 人' }}
                          </span>
                        </div>
                      </el-col>
                    </el-row>
                  </div>

                  <!-- Section 4: 培训内容 -->
                  <div class="form-section">
                    <div class="form-section-title">培训内容</div>
                    <el-row>
                      <el-col :span="24">
                        <el-form-item label-width="0">
                          <el-input type="textarea" :rows="2"
                                    v-model="currentBatch.ocrTrainContent"
                                    placeholder="核对提取出的培训大纲内容" />
                        </el-form-item>
                      </el-col>
                    </el-row>
                  </div>
                </el-form>
              </div>
            </el-collapse-item>
          </el-collapse>

          <!-- 2) 人员明细校对:默认收起 -->
          <el-collapse v-model="activeSections" class="meta-collapse">
            <el-collapse-item name="personnel" class="personnel-collapse-item">
              <template slot="title">
                <div class="personnel-header-inner">
                  <i class="el-icon-user-solid"></i>
                  <span class="personnel-title">人员明细校对</span>
                  <el-tag type="info" size="mini" class="personnel-stat-tag">
                    共 {{ resultList.length }} 人 / 有效 {{ validCount }} 人 / 缺席 {{ absentCount }} 人
                  </el-tag>
                  <div class="personnel-tools" @click.stop>
                    <el-button v-if="currentBatch.status === '1'"
                               icon="el-icon-refresh" size="mini" class="glass-btn"
                               :loading="reextractLoading" @click.stop="handleReextract">重新提取</el-button>
                  </div>
                </div>
              </template>
              <div class="card-grid-wrapper">
                <div v-loading="detailLoading" class="card-grid">
                  <div v-for="(row, idx) in resultList" :key="row.resultId || idx"
                       :class="['person-card', cardStateClass(row)]">
                    <!-- 左侧色条 -->
                    <div class="card-stripe"></div>

                    <!-- 卡片主体 -->
                    <div class="card-body">
                      <!-- 顶部:序号 + 姓名 + 右上角系统状态 tag -->
                      <div class="card-top">
                        <span class="card-seq">#{{ row.signSeq || (idx + 1) }}</span>
                        <el-input v-if="currentBatch.status === '1' && !row.uiDeleted"
                                  v-model="row.parsedName" size="mini"
                                  class="card-name-input" />
                        <span v-else class="card-name" :class="{ deleted: row.uiDeleted }">
                          {{ row.parsedName || '未识别' }}
                        </span>
                        <el-tag v-if="row.isNewUser === '1'" type="danger" size="mini" effect="plain">新人员</el-tag>
                        <el-tag v-else type="success" size="mini" effect="plain">老员工</el-tag>
                      </div>

                      <!-- 中部:三行字段 -->
                      <div class="card-fields">
                        <div class="field-row">
                          <span class="field-label">部门</span>
                          <el-input v-if="currentBatch.status === '1' && !row.uiDeleted"
                                    v-model="row.parsedDept" size="mini" />
                          <span v-else class="field-value" :class="{ deleted: row.uiDeleted }">
                            {{ row.parsedDept || '—' }}
                          </span>
                        </div>
                        <div class="field-row">
                          <span class="field-label">成绩</span>
                          <el-input v-if="currentBatch.status === '1' && !row.uiDeleted"
                                    v-model="row.parsedScore" size="mini" placeholder="空白=未参考" />
                          <span v-else class="field-value score"
                                :class="{ empty: !row.parsedScore, deleted: row.uiDeleted }">
                            {{ row.parsedScore || '空白' }}
                          </span>
                        </div>
                        <div class="field-row">
                          <span class="field-label">备注</span>
                          <el-input v-if="currentBatch.status === '1' && !row.uiDeleted"
                                    v-model="row.parsedRemark" size="mini" placeholder="如:请假半天" />
                          <span v-else-if="isAbsentRemark(row.parsedRemark)"
                                class="field-value remark-absent">
                            {{ row.parsedRemark }}<span class="absent-suffix"> ·不计入实到</span>
                          </span>
                          <span v-else class="field-value"
                                :class="{ empty: !row.parsedRemark, deleted: row.uiDeleted }">
                            {{ row.parsedRemark || '空白' }}
                          </span>
                        </div>
                      </div>

                      <!-- 底部:作废/恢复 -->
                      <div v-if="currentBatch.status === '1'" class="card-actions">
                        <el-button v-if="!row.uiDeleted" type="text" size="mini"
                                   icon="el-icon-delete" style="color:#F56C6C;"
                                   @click="$set(row, 'uiDeleted', true)">作废</el-button>
                        <el-button v-else type="text" size="mini"
                                   icon="el-icon-refresh-left" style="color:#909399;"
                                   @click="$set(row, 'uiDeleted', false)">恢复</el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>

          <!-- 3) 底部操作 -->
          <div class="confirm-footer" v-if="currentBatch.status === '1'">
             <div class="tips">
                 <i class="el-icon-info"></i> 提示：对照左侧原始凭证可修改明细。无关空数据请点击「作废」。
             </div>
             <div style="text-align: right; margin-top: 15px;">
                <el-button @click="detailOpen = false" class="glass-btn">暂 存 退 出</el-button>
                <el-button type="success" icon="el-icon-s-claim" class="glass-btn success-plain" style="font-size: 15px; font-weight: bold;" :loading="confirmLoading" @click="handleConfirmImport">生成考勤与台账</el-button>
             </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios';
import { listBatch, getBatch, delBatch, uploadBatch, confirmAndGenerate, listBatchLog, listBatchResult, updateBatch, deleteAllBatch, reextractBatch } from "@/api/biz/batch";
import { listPlan } from "@/api/biz/plan";
import { normalizeOcrDate, formatOcrDate } from "@/utils/ocr-date";
import CameraPreviewPanel from '@/components/CameraPreviewPanel';

export default {
  name: "OcrBatch",
  components: { CameraPreviewPanel },
  data() {
    return {
      // 表格与查询参数
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      batchList: [],
      planOptions: [],
      queryParams: {
        pageNum: 1, 
        pageSize: 10, 
        batchNo: null, 
        planId: null, 
        ocrSubject: null, 
        status: null 
      },
      stats: { success: 0, pending_confirm: 0 },

      // 上传弹窗相关
      open: false, 
      title: "", 
      uploadPlanId: null, 
      uploadMethod: 'camera', 
      isUploading: false,
      localFileList: [],

      // 高拍仪控制
      cameraServiceUrl: 'http://127.0.0.1:38088',
      cameraReady: false,
      rotationAngle: 0,
      cameraConfig: { cutType: '1', colorType: '0', jpgQuality: 80 },
      scanImages: [], 

      // 校对工作台相关
      detailOpen: false,
      detailLoading: false,
      confirmLoading: false,
      reextractLoading: false,
      currentBatch: {},
      logList: [],
      resultList: [],
      // 右侧 el-collapse 展开项:name='meta' 是「基础培训信息」(默认展开),name='personnel' 是「人员明细」(默认收起)
      activeSections: ['meta'],
    };
  },
  computed: {
    isSubmitDisabled() {
       if (this.uploadMethod === 'camera') {
           return this.scanImages.length === 0;
       } else {
           return this.localFileList.length === 0;
       }
    },
    displayOcrDate: {
      get() {
        const v = this.currentBatch.ocrDate;
        return formatOcrDate(v) || v || '';
      },
      set(val) {
        this.currentBatch.ocrDate = val;
      }
    },
    // 人员卡片顶部统计:有效 = 总数 - 已作废 - 缺席
    validCount() {
      return this.resultList.filter(r => !r.uiDeleted && !this.isAbsentRemark(r.parsedRemark)).length;
    },
    absentCount() {
      return this.resultList.filter(r => this.isAbsentRemark(r.parsedRemark)).length;
    }
  },
  created() {
    this.getList();
    this.getPlans();
  },
  watch: {
    uploadMethod(val) {
      if (val === 'camera') {
          this.initCamera();
      }
    }
  },
  methods: {
    // 把模块级 import 暴露到实例上,模板里才能直接调用
    normalizeOcrDate,
    formatOcrDate(raw) {
      // 闭包绑定模块导出,避开 splitChunks.commons 切分后的引用丢失
      return formatOcrDate(raw)
    },
    // 镜像后端 BizTrainOcrBatchServiceImpl.isAbsentRemark 的关键字(后端会把这类人跳过,不进档案/台账)
    isAbsentRemark(remark) {
      if (!remark) return false;
      const r = String(remark).trim();
      return r.indexOf('未参加') >= 0
          || r.indexOf('缺席') >= 0
          || r.indexOf('缺考') >= 0
          || r.indexOf('授课人') >= 0;
    },
    // 给人员卡片打状态类名,控制左侧色条颜色
    cardStateClass(row) {
      if (row.uiDeleted) return 'state-deleted';
      if (this.isAbsentRemark(row.parsedRemark)) return 'state-absent';
      if (row.isNewUser === '1') return 'state-new';
      return 'state-normal';
    },
    getList() {
        this.loading = true;
        return listBatch(this.queryParams).then(response => {
            // 待人工校对 (status='1') 全部置顶,其余保持后端 create_time DESC 原序
            const rows = (response.rows || []).slice();
            rows.sort((a, b) => {
                if (a.status === '1' && b.status !== '1') return -1;
                if (a.status !== '1' && b.status === '1') return 1;
                return 0;
            });
            this.batchList = rows;
            this.total = response.total;
            this.stats.success = this.batchList.filter(i => i.status == '2').length;
            this.stats.pending_confirm = this.batchList.filter(i => i.status == '1').length;
            this.loading = false;
        });
    },
    
    getPlans() { 
        listPlan({ pageNum: 1, pageSize: 1000 }).then(res => { 
            this.planOptions = res.rows; 
        }); 
    },
    
    getPlanName(planId) {
        if (!planId) return "线下临时补录";
        const plan = this.planOptions.find(p => p.planId === planId);
        return plan ? plan.subject : planId;
    },

    // Excel 日期序列号兼容已统一到 utils/ocr-date.js 的 normalizeOcrDate

    // ======== 经过真实验证的终极路径转换器 ========
    getRealImageUrl(url) {
      if (!url) return '';

      // 1. 将物理盘符的 Windows 反斜杠 \ 全部转换为 /
      let path = url.replace(/\\/g, '/');

      // 2. 若已经是 http 绝对路径则直接返回
      if (path.startsWith('http://') || path.startsWith('https://')) return path;

      // 3. 提取服务器 origin（自动适配 dev/prod）
      const origin = window.location.origin;

      // 4. 物理路径 → /profile/ 虚拟路径（如 /www/wwwroot/trainarchive/uploadPath/ocr/... → /profile/ocr/...）
      const ocrIndex = path.indexOf('/ocr/');
      if (ocrIndex !== -1) {
        return origin + '/profile' + path.substring(ocrIndex);
      }

      // 5. 已含 /profile 的路径，直接拼 origin
      if (path.indexOf('/profile') !== -1) {
        return origin + path.substring(path.indexOf('/profile'));
      }

      // 6. 其他情况（avatar 等），直接拼 origin
      return origin + '/' + path.replace(/^\//, '');
    },

    // ======== 文件类型判断 ========
    isImageFile(url) {
      if (!url) return false;
      const ext = url.split('.').pop().toLowerCase();
      return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext);
    },
    getFileName(url) {
      if (!url) return '未知文件';
      return url.split('/').pop();
    },
    downloadFile(url) {
      const fullUrl = this.getRealImageUrl(url);
      const a = document.createElement('a');
      a.href = fullUrl;
      a.download = this.getFileName(url);
      a.target = '_blank';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    },

    // ======== 高拍仪核心引擎 ========
    rotateLeft() { 
        this.rotationAngle = (this.rotationAngle - 90 + 360) % 360; 
    },
    rotateRight() { 
        this.rotationAngle = (this.rotationAngle + 90) % 360; 
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
    handleDialogOpen() {
      this.cameraReady = false;
      if (this.uploadMethod === 'camera') {
        const panel = this.$refs.cameraPanel;
        if (panel) panel.start();
      }
    },
    handleDialogClose() {
      const panel = this.$refs.cameraPanel;
      if (panel) panel.stop();
      this.cameraReady = false;
    },
    onCameraReady() {
      this.cameraReady = true;
    },
    onCameraBroken() {
      this.cameraReady = false;
    },
    onCameraLaunchFailed() {
      this.cameraReady = false;
    },
    handleScan() {
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

      axios.post(`${this.cameraServiceUrl}/video=grabimage`, JSON.stringify(params), {
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      })
      .then(async res => {
        if (res.data.code === "0" && res.data.images && res.data.images.length > 0) {
          let base64Data = res.data.images[0].base64;
          if (this.rotationAngle !== 0) {
             try {
                const rotatedData = await this.rotateBase64(base64Data, this.rotationAngle);
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
    fileToBase64(file) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
      });
    },

    // ======== 最终批量上传解析 ========
    async submitBatchUpload() {
       this.isUploading = true;
       let base64List = [];
       if (this.uploadMethod === 'camera') {
           base64List = this.scanImages;
       } else {
           for (let file of this.localFileList) {
               let base64 = await this.fileToBase64(file.raw);
               base64List.push(base64);
           }
       }

       uploadBatch({ planId: this.uploadPlanId, base64Images: base64List }).then(res => {
           this.$modal.msgSuccess("图片已送达神经中枢，系统正在异步解析...");
           this.isUploading = false;
           this.open = false;
           this.getList();
       }).catch(() => {
           this.isUploading = false;
       });
    },

    handleQuery() { 
        this.queryParams.pageNum = 1; 
        this.getList(); 
    },
    resetQuery() { 
        this.resetForm("queryForm"); 
        this.handleQuery(); 
    },
    handleSelectionChange(selection) { 
        this.ids = selection.map(item => item.batchId); 
        this.multiple = !selection.length; 
    },
    handleAdd() { 
        this.uploadPlanId = null;
        this.scanImages = [];
        this.localFileList = [];
        this.uploadMethod = 'camera';
        this.open = true; 
        this.title = "启动高拍仪流水线"; 
    },
    
    // ======== 校对工作台核心逻辑 ========
    handleReview(row) {
        this.currentBatch = JSON.parse(JSON.stringify(row));
        // 每次打开弹窗重置折叠状态:基础培训信息默认展开,人员明细默认收起
        // (否则关闭再开后,上次手动展开过的面板会保持展开)
        this.activeSections = ['meta'];
        this.detailOpen = true;
        this.loadBatchDetail(row.batchId);
    },

    loadBatchDetail(batchId) {
        this.detailLoading = true;
        this.logList = [];
        this.resultList = [];

        // 1. 重新拉取批次主表元数据（确保 重新提取 等操作后 currentBatch 同步）
        getBatch(batchId).then(res => {
            if (res.data) {
                this.currentBatch = res.data;
            }
        });

        // 2. 获取图片
        listBatchLog({ batchId: batchId }).then(res => {
            this.logList = res.rows;
        });

        // 3. 获取右侧下方人员解析明细 (附加 uiDeleted 实现前端软作废)
        listBatchResult({ batchId: batchId, pageSize: 500 }).then(res => {
            this.resultList = res.rows.map(item => {
               return { ...item, uiDeleted: false };
            });
            this.detailLoading = false;
        }).catch(() => {
            this.detailLoading = false;
        });
    },

    async handleConfirmImport() {
        // 过滤出未被标记作废的人员列表进行结算提交
        const editedList = this.resultList.filter(item => !item.uiDeleted);

        try {
            await this.$modal.confirm('确认基础信息和人员明细无误，开始生成最终合规台账与考勤？');
            this.confirmLoading = true;
            await updateBatch(this.currentBatch);
            await confirmAndGenerate(this.currentBatch.batchId, editedList);
            this.$modal.msgSuccess("极速结算成功！已同步生成台账。");
            this.detailOpen = false;
            this.confirmLoading = false;
            // 通知其他被 keep-alive 缓存的列表页（ledger/archive/attendance/session）下次激活时强制刷新
            sessionStorage.setItem('biz:dataChanged', JSON.stringify({ t: Date.now() }));
            // 等待外层批次列表拉取完成，确保状态变化可见
            await this.getList();
            // 兜底：再刷一次防止异步状态未及时落库
            setTimeout(() => { this.getList(); }, 600);
        } catch (e) {
            this.confirmLoading = false;
        }
    },

    handleReextract() {
        this.$modal.confirm('将基于当前批次的源文件重新 OCR 提取应到/实到/培训日期等元数据。\n注意：会覆盖当前已编辑的字段值，确认继续？', '重新提取').then(() => {
            this.reextractLoading = true;
            return reextractBatch(this.currentBatch.batchId);
        }).then(res => {
            if (res.code === 200) {
                this.$modal.msgSuccess(res.msg || '已更新元数据');
                this.loadBatchDetail(this.currentBatch.batchId);
            } else {
                this.$modal.msgError('重新提取失败：' + (res.msg || ''));
            }
            this.reextractLoading = false;
        }).catch(() => {
            this.reextractLoading = false;
        });
    },
    
    handleDeleteAll() {
      this.$modal.confirm('警告！此操作将删除所有符合当前查询条件的批次数据，且不可恢复！', '确认全部删除').then(() => {
        return deleteAllBatch(this.queryParams);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("全部删除成功");
      }).catch(() => {});
    },
    handleDelete(row) {
        const batchIds = row.batchId || this.ids;
        this.$modal.confirm('确认彻底删除选中的原始扫描批次？').then(function() {
            return delBatch(batchIds);
        }).then(() => {
            this.getList();
            this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
    },

    handleExport() {
      const query = { ...this.queryParams };
      this.download('biz/batch/export', query, `ocr_batch_${new Date().getTime()}.xlsx`);
    }
  }
};
</script>

<style lang="scss" scoped>
.cloud-glass-container {
  min-height: calc(100vh - 84px);
  background: #f0f2f5;
  position: relative;
  z-index: 1;
  padding: 20px;
}

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
.bg-orange { background: #e6a23c; color: white; }

/* ================= 状态动画 ================= */
.status-processing { 
  color: #409eff; 
  font-weight: bold; 
  font-size: 13px; 
}
.status-processing i { 
  animation: rotating 2s linear infinite; 
}
@keyframes rotating { 
  from { transform: rotate(0) } 
  to { transform: rotate(360deg) } 
}

/* ================= 按钮与表单 ================= */
::v-deep .glass-input .el-input__inner { 
  background: rgba(255, 255, 255, 0.5) !important; 
  border: 1px solid rgba(200, 200, 200, 0.3) !important; 
  border-radius: 8px; 
}
::v-deep .glass-table {
  background: #ffffff !important;
}
::v-deep .glass-table th, 
::v-deep .glass-table tr, 
::v-deep .glass-table td { 
  background: transparent !important; 
  border-bottom: 1px solid rgba(0, 0, 0, 0.04); 
}
::v-deep .glass-table th { 
  background: rgba(245, 247, 250, 0.4) !important; 
  color: #333; 
  font-weight: 600; 
}
::v-deep .glass-table::before { display: none; }
::v-deep .glass-tabs .el-tabs__content { background: rgba(255,255,255,0.5); }

.glass-btn { 
  border-radius: 8px; 
  font-weight: 500; 
  border: none; 
  background: rgba(255, 255, 255, 0.6); 
  color: #606266; 
}
.glass-btn.primary {
  background: #1890ff;
  color: white;
}
.glass-btn.success-plain {
  color: #67c23a;
  background: rgba(103, 194, 58, 0.06);
  border: 1px solid rgba(103, 194, 58, 0.2);
}
.glass-btn.warning-plain {
  color: #E6A23C;
  background: rgba(230, 162, 60, 0.06);
  border: 1px solid rgba(230, 162, 60, 0.2);
}
.glass-btn.danger-plain {
  color: #F56C6C;
  background: rgba(245, 108, 108, 0.06);
  border: 1px solid rgba(245, 108, 108, 0.2);
}

/* ================= 高拍仪区域 ================= */
.camera-box { background: transparent; }
.toolbar-panel { 
  padding: 10px; 
  background: rgba(255,255,255,0.8); 
  border-radius: 8px; 
}
.toolbar-row { 
  display: flex; 
  align-items: center; 
}
.toolbar-row .label { 
  font-size: 13px; 
  font-weight: bold; 
  margin-right: 8px; 
}
.ml15 { margin-left: 15px; }

.preview-header {
  font-size: 14px;
  font-weight: bold;
  color: #1890ff;
  margin-bottom: 10px;
  text-align: center;
}
.camera-window { 
  width: 100%; 
  height: 350px; 
  background: #1a1a1a; 
  border-radius: 12px; 
  display: flex; 
  justify-content: center; 
  align-items: center; 
  overflow: hidden; 
  box-shadow: inset 0 0 20px rgba(0,0,0,0.5); 
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
.camera-window .camera-placeholder i { 
  font-size: 40px; 
  margin-bottom: 10px; 
}
.camera-window .camera-placeholder p { margin: 0; }

.gallery-window { 
  width: 100%; 
  height: 350px; 
  background: rgba(255,255,255,0.6); 
  border: 2px dashed #dcdfe6; 
  border-radius: 12px; 
  overflow-y: auto; 
  padding: 10px; 
}
.gallery-window .empty-gallery { 
  text-align: center; 
  color: #ccc; 
  margin-top: 120px; 
}
.gallery-window .empty-gallery i { font-size: 40px; }
.gallery-window .image-list { 
  display: flex; 
  flex-wrap: wrap; 
  gap: 10px; 
}
.gallery-window .image-list .img-item { 
  width: 47%; 
  height: 160px; 
  position: relative; 
  border-radius: 8px; 
  overflow: hidden; 
  border: 1px solid #eee; 
  box-shadow: 0 2px 12px rgba(0,0,0,0.1); 
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
  background: rgba(0,0,0,0.6); 
  color: white; 
  font-size: 12px; 
  padding: 2px 6px; 
  border-radius: 4px; 
}
.gallery-window .image-list .img-item .delete-mask { 
  position: absolute; 
  bottom: 0; left: 0; width: 100%; 
  background: rgba(245, 108, 108, 0.9); 
  color: #fff; 
  text-align: center; 
  padding: 5px 0; 
  font-size: 13px; 
  cursor: pointer; 
  transform: translateY(100%); 
  transition: transform 0.3s; 
}
.gallery-window .image-list .img-item:hover .delete-mask { 
  transform: translateY(0); 
}

/* ================= 校对工作台全屏UI深度定制 ================= */
.ocr-workspace .el-dialog__body { 
  padding: 0 !important; 
  height: calc(100vh - 60px); 
}
.workspace-container { 
  display: flex; 
  height: 100%; 
  background: #f0f2f5; 
}
.workspace-container .left-view { 
  width: 45%; 
  border-right: 1px solid #dcdfe6; 
  display: flex; 
  flex-direction: column; 
  padding: 0; 
  margin: 15px; 
  border-radius: 12px; 
  overflow: hidden;
}
.workspace-container .right-view { 
  width: 55%; 
  display: flex; 
  flex-direction: column; 
  padding: 15px; 
  margin: 15px 15px 15px 0; 
  border-radius: 12px; 
}
.workspace-container .view-header { 
  height: 45px; 
  line-height: 45px; 
  padding: 0 20px; 
  background: rgba(245,247,250,0.8); 
  border-bottom: 1px solid #ebeef5; 
  font-weight: bold; 
  color: #303133; 
  font-size: 15px; 
}
.workspace-container .batch-meta-form {
  padding: 12px 15px;
  background: rgba(255,255,255,0.4);
  border-radius: 0 0 8px 8px;
  margin-bottom: 10px;
}
/* 表单分组排版 */
.workspace-container .batch-meta-form .form-section {
  background: #fafbfc;
  border-radius: 6px;
  padding: 6px 12px 4px;
  margin-bottom: 8px;
  border: 1px solid #ebeef5;
}
.workspace-container .batch-meta-form .form-section-highlight {
  background: linear-gradient(135deg, #fff7e6 0%, #fffbf0 100%);
  border: 1px solid #ffd591;
  box-shadow: 0 1px 3px rgba(230, 162, 60, 0.08);
}
.workspace-container .batch-meta-form .form-section-title {
  font-size: 11px;
  color: #909399;
  font-weight: bold;
  margin-bottom: 2px;
  letter-spacing: 1px;
  text-transform: uppercase;
}
.workspace-container .batch-meta-form .form-section-highlight .form-section-title {
  color: #d48806;
}
.workspace-container .batch-meta-form .count-info {
  font-size: 13px;
  color: #606266;
  line-height: 36px;
  text-align: center;
}
.workspace-container .batch-meta-form .count-info strong {
  color: #E6A23C;
  font-size: 18px;
  font-weight: bold;
  margin: 0 4px;
}
.workspace-container .batch-meta-form .count-diff {
  display: inline-block;
  margin-left: 8px;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: bold;
}
.workspace-container .batch-meta-form .count-diff-ok {
  background: #f0f9eb;
  color: #67C23A;
  border: 1px solid #c2e7b0;
}
.workspace-container .batch-meta-form .count-diff-warn {
  background: #fef0f0;
  color: #F56C6C;
  border: 1px solid #fbc4c4;
}
.workspace-container .batch-meta-form .el-form-item {
  margin-bottom: 8px;
}
// (旧 .table-wrapper 表格样式已废弃,改为下方卡片网格)

/* ============== 基础培训信息可折叠区 ============== */
.meta-collapse {
  background: transparent !important;
  border: none !important;
  margin-bottom: 10px;
}
::v-deep .meta-collapse .el-collapse-item {
  border: none !important;
}
::v-deep .meta-collapse .el-collapse-item__header {
  height: 44px;
  line-height: 44px;
  padding: 0 20px;
  background: rgba(245, 247, 250, 0.85) !important;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 0;
}
::v-deep .meta-collapse .el-collapse-item__wrap {
  border: none !important;
  background: transparent !important;
  box-shadow: none !important;
}
::v-deep .meta-collapse .el-collapse-item__content {
  padding: 12px 4px 0;
}

/* ============== 人员明细校对(卡片网格) ============== */
.personnel-collapse-item {
  margin-top: 8px;
}
.personnel-header-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  padding-right: 30px;
  font-weight: bold;
  color: #303133;
}
.personnel-header-inner .personnel-title {
  font-weight: bold;
}
.personnel-header-inner .personnel-stat-tag {
  font-weight: normal;
}
.personnel-tools {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}
::v-deep .personnel-collapse-item.is-active .el-collapse-item__header {
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
}

.card-grid-wrapper {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 4px 4px 4px 0;
  margin-top: 6px;
}
.card-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  padding: 4px;
}
.person-card {
  width: 300px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
  display: flex;
  overflow: hidden;
  transition: box-shadow .15s, transform .15s;
  position: relative;
}
.person-card:hover {
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}
.card-stripe {
  width: 5px;
  flex-shrink: 0;
  background: #67C23A; /* 默认 = 正常(绿) */
}
.person-card.state-absent .card-stripe {
  background: #F56C6C; /* 红 */
}
.person-card.state-new .card-stripe {
  background: #E6A23C; /* 橙 */
}
.person-card.state-deleted .card-stripe {
  background: #C0C4CC; /* 灰 */
}
.person-card.state-deleted {
  opacity: 0.55;
}

.card-body {
  flex: 1;
  padding: 12px 14px 8px;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.card-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px dashed rgba(0, 0, 0, 0.06);
}
.card-seq {
  font-size: 12px;
  color: #909399;
  font-weight: bold;
  flex-shrink: 0;
}
.card-name, .card-name-input {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  flex: 1;
  min-width: 0;
}
::v-deep .card-name-input .el-input__inner {
  font-size: 16px;
  font-weight: bold;
  height: 28px;
  line-height: 28px;
  padding: 0 6px;
}
.card-name.deleted, .field-value.deleted {
  text-decoration: line-through;
  color: #C0C4CC;
}

.card-fields {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
}
.field-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 26px;
}
.field-label {
  width: 36px;
  flex-shrink: 0;
  color: #909399;
  font-size: 12px;
}
.field-value {
  flex: 1;
  color: #303133;
  min-width: 0;
  word-break: break-all;
}
.field-value.score {
  font-weight: bold;
}
.field-value.empty {
  color: #c0c4cc;
  font-style: italic;
}
.field-value.remark-absent {
  color: #F56C6C;
  font-weight: 600;
}
.absent-suffix {
  color: #F56C6C;
  opacity: 0.7;
  font-size: 11px;
  margin-left: 4px;
}

.card-actions {
  margin-top: 8px;
  padding-top: 6px;
  border-top: 1px dashed rgba(0, 0, 0, 0.05);
  text-align: right;
}
.workspace-container .carousel-wrapper { 
  flex: 1; 
  background: #1a1a1a; 
}
.workspace-container .confirm-footer { 
  margin-top: 15px; 
  padding-top: 15px; 
  border-top: 1px dashed #ebeef5; 
}
.workspace-container .confirm-footer .tips { 
  color: #E6A23C; 
  font-size: 13px; 
}
</style>