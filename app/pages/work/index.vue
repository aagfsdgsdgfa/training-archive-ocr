<template>
  <view class="workspace-page">
    <view class="blob blob-1"></view>
    <view class="blob blob-2"></view>

    <view class="main-container">
      
      <view class="hero-card">
        <view class="hero-info">
          <view class="icon-wrap">
            <uni-icons type="camera" color="#1a1a1a" size="36"></uni-icons>
          </view>
          <view class="text-wrap">
            <text class="h-title">拍表入库</text>
            <text class="h-desc">AI 极速识别纸质签到表</text>
          </view>
        </view>
        <button class="dark-action-btn" @click="handleChooseImage">
          <text>拍照上传</text>
          <uni-icons type="right" color="#fff" size="16" style="margin-left: 8rpx;"></uni-icons>
        </button>
      </view>

      <view class="grid-section">
        <view class="glass-btn-card" @click="handleToNotice">
          <view class="g-icon"><uni-icons type="sound" color="#666" size="32"></uni-icons></view>
          <text class="g-title">系统公告</text>
        </view>
        <view class="glass-btn-card" @click="$tab.navigateTo('/pages/biz/ledger')">
          <view class="g-icon"><uni-icons type="wallet" color="#666" size="32"></uni-icons></view>
          <text class="g-title">台账查询</text>
        </view>
      </view>

      <view class="section-header">
        <text class="section-title">批次追踪</text>
        <view class="refresh-btn" @click="fetchBatches">
          <uni-icons type="reload" color="#909399" size="18"></uni-icons>
        </view>
      </view>

      <view class="list-card">
        <view v-if="batchList.length === 0" class="empty-box">
          <uni-icons type="info" color="#ccc" size="40"></uni-icons>
          <text>暂无进行中的任务</text>
        </view>
        <view v-else class="list-item" v-for="(batch, index) in batchList" :key="index" @click="handleBatchDetail(batch)">
          <view class="item-left">
            <text class="b-no">{{ batch.batchNo }}</text>
            <text class="b-sub">{{ batch.ocrSubject || '未提取到主题' }} · 共 {{ batch.totalPages }} 页</text>
          </view>
          <view class="item-right">
            <view class="status-badge" :class="getStatusClass(batch.status)">
              <view class="dot"></view>
              <text>{{ getStatusText(batch.status) }}</text>
            </view>
          </view>
        </view>
      </view>

    </view>

    <view class="glass-modal" v-if="showUploadModal" @touchmove.stop.prevent>
      <view class="modal-content login-card-style">
        <view class="m-header">
          <text class="card-title">确认传送</text>
          <view class="close-btn" @click="closeModal">
            <uni-icons type="closeempty" color="#909399" size="24"></uni-icons>
          </view>
        </view>
        
        <view class="m-body">
          <view class="info-card">
            <text class="label">已选图片</text>
            <text class="value highlight">{{ selectedImagesBase64.length }} 张</text>
          </view>
          
          <view class="info-card picker-card">
            <text class="label">关联计划</text>
            <picker class="picker-inner" @change="onPlanChange" :range="planOptions" range-key="subject">
              <view class="picker-text">
                <text class="text-cut">{{ selectedPlanIndex !== -1 ? planOptions[selectedPlanIndex].subject : '不关联计划 (临时培训)' }}</text>
                <uni-icons type="bottom" color="#909399" size="16"></uni-icons>
              </view>
            </picker>
          </view>
        </view>

        <view class="m-footer">
          <button class="submit-btn" :loading="isUploading" @click="submitOcrBatch">
            <text v-if="!isUploading">确 认 提 交</text>
            <text v-else>提 交 中 ...</text>
          </button>
        </view>
      </view>
    </view>

  </view>
</template>

<script>
import { listPlan, listBatch, uploadBatch } from '@/api/biz'

export default {
  data() {
    return {
      batchList: [],
      planOptions: [],
      
      showUploadModal: false,
      isUploading: false,
      selectedImagesBase64: [],
      selectedPlanIndex: -1
    }
  },
  onShow() {
    this.fetchBatches()
    this.fetchPlans()
  },
  onPullDownRefresh() {
    this.fetchBatches().then(() => {
      uni.stopPullDownRefresh()
    })
  },
  methods: {
    async fetchBatches() {
      try {
        const res = await listBatch({ pageNum: 1, pageSize: 10 })
        this.batchList = res.rows
      } catch (e) {
        console.error(e)
      }
    },
    
    async fetchPlans() {
      try {
        const res = await listPlan({ pageNum: 1, pageSize: 100, status: '0' })
        this.planOptions = res.rows
      } catch (e) {
        console.error(e)
      }
    },

    getStatusText(status) {
      const map = { '0': '解析中', '1': '待校对', '2': '已结算', '3': '异常' }
      return map[status] || '未知'
    },
    
    getStatusClass(status) {
      const map = { '0': 'status-blue', '1': 'status-orange', '2': 'status-black', '3': 'status-red' }
      return map[status] || 'status-gray'
    },

    handleBatchDetail(batch) {
      if (!batch.batchId) {
        return this.$modal.msgError('批次信息异常，无法查看')
      }
      this.$tab.navigateTo(`/pages/biz/ocr/detail?id=${batch.batchId}`)
    },

    // 核心修改：跳转到系统原生的公告列表页
    handleToNotice() {
      // 请确保您的系统中有这个路径。若依移动端通常自带 /pages/work/notice/index 或类似路径
      // 如果没有，可以先做个占位提示
      this.$tab.navigateTo('/pages/common/notice/index')
    },

    handleChooseImage() {
      uni.chooseImage({
        count: 9,
        sizeType: ['compressed'],
        sourceType: ['camera', 'album'],
        success: async (res) => {
          uni.showLoading({ title: '处理中...', mask: true })
          try {
            const base64List = []
            for (let i = 0; i < res.tempFilePaths.length; i++) {
              const b64 = await this.pathToBase64(res.tempFilePaths[i])
              base64List.push(b64)
            }
            this.selectedImagesBase64 = base64List
            uni.hideLoading()
            
            this.selectedPlanIndex = -1
            this.showUploadModal = true
          } catch (e) {
            uni.hideLoading()
            this.$modal.msgError('图片读取失败')
          }
        }
      })
    },

    pathToBase64(path) {
      return new Promise((resolve, reject) => {
        // #ifdef H5
        uni.request({
          url: path,
          responseType: 'arraybuffer',
          success: res => resolve(uni.arrayBufferToBase64(res.data)),
          fail: err => reject(err)
        })
        // #endif
        // #ifndef H5
        uni.getFileSystemManager().readFile({
          filePath: path,
          encoding: 'base64',
          success: res => resolve(res.data),
          fail: err => reject(err)
        })
        // #endif
      })
    },

    onPlanChange(e) {
      this.selectedPlanIndex = e.detail.value
    },
    
    closeModal() {
      if(this.isUploading) return;
      this.showUploadModal = false
      this.selectedImagesBase64 = []
    },

    async submitOcrBatch() {
      if (this.selectedImagesBase64.length === 0) return
      
      this.isUploading = true
      let targetPlanId = null
      if (this.selectedPlanIndex !== -1) {
        targetPlanId = this.planOptions[this.selectedPlanIndex].planId
      }

      const reqData = {
        planId: targetPlanId,
        base64Images: this.selectedImagesBase64
      }

      try {
        await uploadBatch(reqData)
        this.isUploading = false
        this.showUploadModal = false
        this.selectedImagesBase64 = []
        this.$modal.msgSuccess('入库成功，请等待系统解析')
        this.fetchBatches()
      } catch (e) {
        this.isUploading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.workspace-page {
  width: 100vw;
  min-height: 100vh;
  background-color: #f8faff; 
  position: relative;
  overflow-x: hidden;
  padding-bottom: 60rpx;

  /* 背景光晕 */
  .blob {
    position: absolute;
    width: 500rpx;
    height: 500rpx;
    background: linear-gradient(135deg, rgba(60, 156, 255, 0.2), rgba(122, 187, 255, 0.1));
    filter: blur(80px);
    border-radius: 50%;
    z-index: 0;
    animation: move 15s infinite alternate;
  }
  .blob-1 { top: -100rpx; right: -100rpx; }
  .blob-2 { bottom: 200rpx; left: -100rpx; background: rgba(144, 147, 255, 0.15); }

  @keyframes move {
    from { transform: translate(0, 0) scale(1); }
    to { transform: translate(50rpx, 100rpx) scale(1.1); }
  }

  .main-container {
    position: relative;
    z-index: 1;
    width: 85%;
    max-width: 650rpx;
    margin: 0 auto;
    padding-top: 160rpx;
  }

  /* 核心入口卡片 */
  .hero-card {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(20px);
    border-radius: 48rpx; 
    padding: 50rpx;
    box-shadow: 0 40rpx 100rpx rgba(0, 0, 0, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.5);
    margin-bottom: 40rpx;

    .hero-info {
      display: flex;
      align-items: center;
      margin-bottom: 40rpx;

      .icon-wrap {
        width: 88rpx; height: 88rpx; background: #f0f2f5; border-radius: 28rpx;
        display: flex; justify-content: center; align-items: center;
        margin-right: 30rpx;
      }

      .text-wrap {
        .h-title { display: block; font-size: 36rpx; font-weight: 800; color: #1a1a1a; margin-bottom: 6rpx; }
        .h-desc { display: block; font-size: 24rpx; color: #909399; }
      }
    }

    .dark-action-btn {
      width: 100%; height: 100rpx; background: #1a1a1a; border-radius: 24rpx;
      color: #fff; display: flex; justify-content: center; align-items: center;
      font-size: 30rpx; font-weight: 600; border: none; transition: all 0.2s;
      &::after { border: none; }
      &:active { transform: scale(0.97); opacity: 0.9; }
    }
  }

  /* 次级功能矩阵 */
  .grid-section {
    display: flex; justify-content: space-between; gap: 30rpx; margin-bottom: 60rpx;

    .glass-btn-card {
      flex: 1; background: rgba(255, 255, 255, 0.5); backdrop-filter: blur(20px);
      border-radius: 40rpx; padding: 40rpx 0; display: flex; flex-direction: column; align-items: center;
      border: 1px solid rgba(255, 255, 255, 0.6); box-shadow: 0 20rpx 40rpx rgba(0,0,0,0.02);
      transition: all 0.2s;
      &:active { transform: scale(0.96); background: rgba(255,255,255,0.8); }

      .g-icon { margin-bottom: 16rpx; }
      .g-title { font-size: 28rpx; font-weight: 600; color: #333; }
    }
  }

  /* 极简列表区域 */
  .section-header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx;
    .section-title { font-size: 34rpx; font-weight: 700; color: #1a1a1a; }
    .refresh-btn { padding: 10rpx; transition: transform 0.2s; &:active { transform: rotate(180deg); } }
  }

  .list-card {
    background: rgba(255, 255, 255, 0.6); backdrop-filter: blur(20px);
    border-radius: 48rpx; padding: 20rpx 40rpx; border: 1px solid rgba(255, 255, 255, 0.5);
    box-shadow: 0 40rpx 100rpx rgba(0, 0, 0, 0.03);

    .empty-box {
      display: flex; flex-direction: column; align-items: center; padding: 60rpx 0;
      text { font-size: 26rpx; color: #909399; margin-top: 20rpx; }
    }

    .list-item {
      display: flex; justify-content: space-between; align-items: center; padding: 36rpx 0;
      border-bottom: 1px solid rgba(0,0,0,0.04);
      transition: opacity 0.2s;
      
      &:active { opacity: 0.5; }
      &:last-child { border-bottom: none; }

      .item-left {
        flex: 1; display: flex; flex-direction: column; overflow: hidden; padding-right: 20rpx;
        .b-no { font-size: 30rpx; font-weight: 700; color: #1a1a1a; margin-bottom: 8rpx; font-family: monospace; }
        .b-sub { font-size: 24rpx; color: #909399; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
      }

      .status-badge {
        display: flex; align-items: center; background: rgba(255,255,255,0.8); padding: 10rpx 20rpx; border-radius: 16rpx; border: 1px solid rgba(0,0,0,0.03);
        .dot { width: 12rpx; height: 12rpx; border-radius: 50%; margin-right: 12rpx; }
        text { font-size: 22rpx; font-weight: 600; }
      }
      .status-blue { .dot { background: #3c9cff; } text { color: #3c9cff; } }
      .status-orange { .dot { background: #f9ae3d; } text { color: #f9ae3d; } }
      .status-black { .dot { background: #1a1a1a; } text { color: #1a1a1a; } }
      .status-red { .dot { background: #fa3534; } text { color: #fa3534; } }
    }
  }

  /* 极简毛玻璃弹窗 */
  .glass-modal {
    position: fixed; top: 0; left: 0; right: 0; bottom: 0;
    background: rgba(0, 0, 0, 0.3); backdrop-filter: blur(10px);
    z-index: 999; display: flex; justify-content: center; align-items: center;
    
    .modal-content {
      width: 85%; background: rgba(255, 255, 255, 0.85); backdrop-filter: blur(25px);
      border-radius: 48rpx; border: 1px solid rgba(255,255,255,0.6); box-shadow: 0 40rpx 100rpx rgba(0,0,0,0.1);
      
      .m-header {
        padding: 60rpx 50rpx 40rpx; display: flex; justify-content: space-between; align-items: center;
        .card-title { font-size: 34rpx; font-weight: 800; color: #333; }
        .close-btn { padding: 10rpx; transition: transform 0.2s; &:active { transform: scale(0.9); } }
      }
      
      .m-body {
        padding: 0 50rpx 40rpx;
        .info-card {
          background: rgba(255, 255, 255, 0.6); border-radius: 24rpx; padding: 30rpx;
          display: flex; justify-content: space-between; align-items: center;
          margin-bottom: 24rpx; border: 1px solid transparent; transition: all 0.3s;
          .label { font-size: 28rpx; color: #606266; }
          .value { font-size: 32rpx; font-weight: 800; color: #1a1a1a; }
        }
        .picker-card {
          padding: 0; display: block;
          .label { display: block; padding: 30rpx 30rpx 10rpx; font-size: 28rpx; color: #606266; }
          .picker-inner {
            padding: 10rpx 30rpx 30rpx;
            .picker-text { font-size: 30rpx; color: #1a1a1a; font-weight: 600; display: flex; justify-content: space-between; align-items: center; }
            .text-cut { flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-right: 20rpx; }
          }
        }
      }
      
      .m-footer {
        padding: 0 50rpx 60rpx;
        .submit-btn {
          width: 100%; height: 100rpx; background: #1a1a1a; border-radius: 24rpx; color: #fff;
          font-size: 30rpx; font-weight: 600; display: flex; justify-content: center; align-items: center;
          transition: all 0.2s; border: none;
          &::after { border: none; }
          &:active { transform: scale(0.97); opacity: 0.9; }
        }
      }
    }
  }
}
</style>