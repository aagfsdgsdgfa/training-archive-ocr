<template>
  <view class="camera-page">
    <view class="blob blob-1"></view>
    <view class="blob blob-2"></view>

    <view class="custom-navbar glass-nav" :style="{ paddingTop: statusBarHeight + 'px', height: navBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" color="#1a1a1a" size="24"></uni-icons>
      </view>
      <view class="nav-center">
        <text class="nav-title">Scanner</text>
      </view>
      <view class="nav-right"></view>
    </view>

    <view class="main-container" :style="{ paddingTop: (statusBarHeight + navBarHeight + 20) + 'px' }">
      
      <view class="upload-section">
        <view class="image-grid">
          <view class="image-item" v-for="(img, index) in imageList" :key="index">
            <image :src="img" mode="aspectFill" @click="previewImage(index)"></image>
            <view class="delete-btn" @click.stop="removeImage(index)">
              <uni-icons type="closeempty" color="#fff" size="14"></uni-icons>
            </view>
            <view class="page-num">{{ index + 1 }}</view>
          </view>
          
          <view class="add-btn" @click="chooseImage" v-if="imageList.length < 9">
            <uni-icons type="camera-filled" color="#909399" size="36"></uni-icons>
          </view>
        </view>
      </view>

      <view class="form-section glass-panel">
        <view class="form-row">
          <view class="row-header">
            <uni-icons type="paperclip" color="#1a1a1a" size="20"></uni-icons>
            <text class="label">归属培训计划</text>
          </view>
          <picker class="custom-picker" @change="onPlanChange" :range="planOptions" range-key="subject">
            <view class="picker-value">
              <text class="text-cut" :class="{ 'placeholder': selectedPlanIndex === -1 }">
                {{ selectedPlanIndex !== -1 ? planOptions[selectedPlanIndex].subject : '不关联计划 (临时培训)' }}
              </text>
              <uni-icons type="bottom" color="#909399" size="16"></uni-icons>
            </view>
          </picker>
        </view>
      </view>
    </view>

    <view class="bottom-action">
      <button class="dark-submit-btn" :loading="isUploading" @click="submitBatch">
        <text v-if="!isUploading">确 认 传 送</text>
        <text v-else>解 析 中 ...</text>
      </button>
      <view class="safe-area-bottom"></view>
    </view>
  </view>
</template>

<script>
// 这里一定要导入我们在 api/biz.js 中写好的方法
import { listPlan, uploadBatch } from '@/api/biz'

export default {
  data() {
    return {
      statusBarHeight: 0, 
      navBarHeight: 44,   
      
      imageList: [], 
      planOptions: [],
      selectedPlanIndex: -1,
      isUploading: false
    }
  },
  onLoad() {
    this.initSystemInfo()
    this.fetchPlans()
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
      uni.navigateBack({ delta: 1 })
    },

    async fetchPlans() {
      try {
        const res = await listPlan({ pageNum: 1, pageSize: 100, status: '0' })
        this.planOptions = res.rows
      } catch (e) {
        console.error("计划拉取失败", e)
      }
    },

    chooseImage() {
      const maxCount = 9 - this.imageList.length
      if (maxCount <= 0) return

      uni.chooseImage({
        count: maxCount,
        sizeType: ['compressed'],
        sourceType: ['camera', 'album'],
        success: (res) => {
          this.imageList = this.imageList.concat(res.tempFilePaths)
        }
      })
    },

    previewImage(index) {
      uni.previewImage({
        urls: this.imageList,
        current: index
      })
    },

    removeImage(index) {
      this.imageList.splice(index, 1)
    },

    onPlanChange(e) {
      this.selectedPlanIndex = e.detail.value
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

    async submitBatch() {
      if (this.imageList.length === 0) {
        return this.$modal.msgError('请先拍摄或选择签到表图片')
      }

      this.isUploading = true
      uni.showLoading({ title: 'AI 引擎启动中...', mask: true })

      try {
        const base64Images = []
        for (let i = 0; i < this.imageList.length; i++) {
          const b64 = await this.pathToBase64(this.imageList[i])
          base64Images.push(b64)
        }

        let targetPlanId = null
        if (this.selectedPlanIndex !== -1) {
          targetPlanId = this.planOptions[this.selectedPlanIndex].planId
        }

        const reqData = {
          planId: targetPlanId,
          base64Images: base64Images
        }

        // 调用上传接口
        await uploadBatch(reqData)
        
        uni.hideLoading()
        this.isUploading = false
        this.$modal.msgSuccess('传送成功，已进入系统解析队列')
        
        this.imageList = []
        this.selectedPlanIndex = -1
        
        // 延时返回，让用户看清成功提示
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)

      } catch (error) {
        uni.hideLoading()
        this.isUploading = false
        console.error("上传错误详情：", error)
        this.$modal.msgError('文件处理失败，请重试')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.camera-page {
  width: 100vw;
  min-height: 100vh;
  background-color: #f8faff;
  position: relative;
  overflow-x: hidden;
  padding-bottom: 200rpx; 

  /* 背景光晕 */
  .blob {
    position: absolute;
    width: 600rpx; height: 600rpx;
    background: linear-gradient(135deg, rgba(60, 156, 255, 0.12), rgba(122, 187, 255, 0.05));
    filter: blur(90px); border-radius: 50%; z-index: 0;
    animation: move 20s infinite alternate;
  }
  .blob-1 { top: -100rpx; right: -150rpx; }
  .blob-2 { bottom: 400rpx; left: -150rpx; background: rgba(166, 140, 227, 0.08); width: 400rpx; height: 400rpx; }

  @keyframes move {
    from { transform: translate(0, 0) scale(1); }
    to { transform: translate(40rpx, 80rpx) scale(1.05); }
  }

  /* 自定义沉浸式导航栏 */
  .custom-navbar {
    position: fixed;
    top: 0; left: 0; right: 0;
    z-index: 999;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 30rpx;
    transition: background-color 0.3s;
  }
  .glass-nav {
    background: rgba(248, 250, 255, 0.85);
    backdrop-filter: blur(20px);
    border-bottom: 1px solid rgba(0, 0, 0, 0.02);
  }
  
  .nav-left, .nav-right { width: 80rpx; display: flex; align-items: center; }
  .nav-center {
    flex: 1; display: flex; justify-content: center;
    .nav-title { font-size: 32rpx; font-weight: 800; color: #1a1a1a; letter-spacing: 1px; text-transform: uppercase; }
  }

  /* 主容器 */
  .main-container {
    position: relative; z-index: 1; width: 88%; max-width: 680rpx; margin: 0 auto;
  }

  /* ================= 图片采集矩阵 ================= */
  .upload-section {
    margin-bottom: 60rpx;

    .image-grid {
      display: flex; flex-wrap: wrap; gap: 20rpx;

      /* 统一比例，类似 A4 纸纵向 */
      .image-item, .add-btn { width: calc((100% - 40rpx) / 3); height: 280rpx; border-radius: 24rpx; position: relative; overflow: hidden; }

      .image-item {
        box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.06); border: 1px solid rgba(0,0,0,0.02);
        image { width: 100%; height: 100%; display: block; }
        
        .delete-btn {
          position: absolute; top: 10rpx; right: 10rpx; width: 40rpx; height: 40rpx;
          background: rgba(0,0,0,0.5); backdrop-filter: blur(4px); border-radius: 50%;
          display: flex; justify-content: center; align-items: center; transition: background 0.2s;
          &:active { background: rgba(250,53,52,0.8); }
        }

        .page-num {
          position: absolute; bottom: 10rpx; left: 10rpx; background: rgba(255,255,255,0.8);
          backdrop-filter: blur(4px); padding: 2rpx 12rpx; border-radius: 10rpx;
          font-size: 20rpx; font-weight: 800; color: #1a1a1a;
        }
      }

      .add-btn {
        background: rgba(255, 255, 255, 0.6); backdrop-filter: blur(20px); border: 1px dashed rgba(0, 0, 0, 0.15);
        display: flex; justify-content: center; align-items: center; transition: background 0.2s;
        &:active { background: rgba(0,0,0,0.02); }
      }
    }
  }

  /* ================= 表单设置区 ================= */
  .glass-panel {
    background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(25px);
    border: 1px solid rgba(255, 255, 255, 0.8); box-shadow: 0 30rpx 60rpx rgba(0, 0, 0, 0.03);
    border-radius: 36rpx;
  }

  .form-section {
    padding: 10rpx 40rpx;

    .form-row {
      padding: 30rpx 0;
      .row-header {
        display: flex; align-items: center; margin-bottom: 20rpx;
        .label { font-size: 28rpx; font-weight: 700; color: #1a1a1a; margin-left: 12rpx; }
      }

      .custom-picker {
        background: rgba(255,255,255,0.5); border-radius: 20rpx; padding: 24rpx 30rpx; border: 1px solid rgba(0,0,0,0.03);
        .picker-value {
          display: flex; justify-content: space-between; align-items: center;
          .text-cut { flex: 1; font-size: 28rpx; color: #1a1a1a; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-right: 20rpx; }
          .placeholder { color: #909399; font-weight: 500; }
        }
      }
    }
  }

  /* ================= 底部悬浮操作区 ================= */
  .bottom-action {
    position: fixed; bottom: 0; left: 0; right: 0; padding: 30rpx 6%;
    background: linear-gradient(to top, rgba(248,250,255,1) 40%, rgba(248,250,255,0)); z-index: 99;

    .dark-submit-btn {
      width: 100%; height: 110rpx; background: #1a1a1a; border-radius: 32rpx;
      display: flex; justify-content: center; align-items: center;
      color: #fff; font-size: 32rpx; font-weight: 800; letter-spacing: 2px;
      border: none; transition: transform 0.2s, box-shadow 0.2s; box-shadow: 0 20rpx 40rpx rgba(26, 26, 26, 0.15);
      
      &::after { border: none; }
      &:active { transform: scale(0.97); box-shadow: 0 10rpx 20rpx rgba(26, 26, 26, 0.08); }
    }
    
    .safe-area-bottom { height: env(safe-area-inset-bottom); }
  }
}
</style>