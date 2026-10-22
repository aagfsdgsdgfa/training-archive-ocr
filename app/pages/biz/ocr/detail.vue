<template>
  <view class="batch-review-page">
    <view class="blob blob-1"></view>
    <view class="blob blob-2"></view>

    <view class="custom-navbar glass-nav" :style="{ paddingTop: statusBarHeight + 'px', height: navBarHeight + 'px' }">
      <view class="nav-left" @click="goBack"><uni-icons type="left" color="#1a1a1a" size="24"></uni-icons></view>
      <view class="nav-center"><text class="nav-title">Review & Submit</text></view>
      <view class="nav-right"></view>
    </view>

    <view class="main-container" :style="{ paddingTop: (statusBarHeight + navBarHeight + 20) + 'px' }">
      
      <view class="status-header">
        <text class="b-no">{{ batch.batchNo || '加载中...' }}</text>
        <view class="badge" :class="getStatusClass(batch.status)">
          <view class="dot"></view><text>{{ getStatusText(batch.status) }}</text>
        </view>
      </view>

      <view class="gallery-card glass-panel">
        <view class="section-top">
          <text class="title">原始凭证影像</text>
          <text class="desc">点击放大核对</text>
        </view>
        
        <view v-if="imageUrls.length === 0" class="empty-state">
          <uni-icons type="image" color="#ccc" size="40"></uni-icons>
          <text style="display: block; font-size: 24rpx; color: #ccc; margin-top: 10rpx;">未拉取到有效的影像文件</text>
        </view>
        
        <scroll-view v-else scroll-x class="gallery-scroll" :show-scrollbar="false">
          <view class="gallery-track">
            <view class="img-wrap" v-for="(img, idx) in imageUrls" :key="idx" @click="previewImage(idx)">
              <image :src="img" mode="aspectFill"></image>
              <view class="page-tag">{{ idx + 1 }}</view>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="form-card glass-panel">
        <view class="section-top">
          <text class="title">基础信息提取结果</text>
        </view>
        <view class="form-group">
          <view class="form-item">
            <text class="label">培训主题</text>
            <input class="value-input" v-model="batch.ocrSubject" :disabled="!isEditable" placeholder="核对主题" />
          </view>
          <view class="form-item">
            <text class="label">主办部门</text>
            <input class="value-input" v-model="batch.ocrHostDept" :disabled="!isEditable" placeholder="核对部门" />
          </view>
          <view class="form-item">
            <text class="label">授课讲师</text>
            <input class="value-input" v-model="batch.ocrSpeaker" :disabled="!isEditable" placeholder="核对讲师" />
          </view>
          <view class="form-item">
            <text class="label">培训地点</text>
            <input class="value-input" v-model="batch.ocrLocation" :disabled="!isEditable" placeholder="核对地点" />
          </view>
          <view class="form-item">
            <text class="label">原始日期</text>
            <input class="value-input" v-model="batch.ocrDate" :disabled="!isEditable" placeholder="纸质原填日期" />
          </view>
          <view class="form-item no-border">
            <text class="label">培训学时</text>
            <input class="value-input" type="digit" v-model="batch.ocrTrainHours" :disabled="!isEditable" placeholder="如: 2.0" />
          </view>
        </view>
      </view>

      <view class="person-section">
        <view class="section-top" style="margin-bottom: 20rpx; padding-left: 10rpx;">
          <text class="title" style="font-size: 32rpx;">识别人员明细 ({{ activeResultList.length }}人)</text>
          <text class="desc" v-if="isEditable">发现错误可直接修改</text>
        </view>
        
        <view v-if="resultList.length === 0" class="empty-state" style="padding: 60rpx 0;">
          <text style="color: #ccc; font-size: 26rpx;">暂无人员解析结果</text>
        </view>
        
        <view class="person-list" v-else>
          <view class="person-card glass-panel" v-for="(item, index) in resultList" :key="index" v-if="item.status !== '1'">
            
            <view class="p-header">
              <view class="p-seq">No. <input class="inline-input" style="width: 60rpx;" v-model="item.signSeq" :disabled="!isEditable" /></view>
              <view class="p-actions" v-if="isEditable">
                <uni-icons type="trash" color="#fa3534" size="18" @click="removeItem(index)"></uni-icons>
              </view>
            </view>
            
            <view class="p-body">
              <view class="p-field">
                <text class="p-label">姓名</text>
                <input class="p-input font-bold" v-model="item.parsedName" :disabled="!isEditable" />
              </view>
              <view class="p-field">
                <text class="p-label">部门</text>
                <input class="p-input" v-model="item.parsedDept" :disabled="!isEditable" />
              </view>
              <view class="p-field">
                <text class="p-label">成绩/签到</text>
                <input class="p-input highlight-color" v-model="item.parsedScore" :disabled="!isEditable" />
              </view>
            </view>
            
            <view class="p-footer" v-if="item.isNewUser === '1'">
              <text class="new-user-tip">* 系统查无此人，结算将自动建号</text>
            </view>
          </view>
        </view>
      </view>

    </view>

    <view class="bottom-action" v-if="isEditable">
      <button class="dark-submit-btn" :loading="isSubmitting" @click="handleConfirmImport">
        <text v-if="!isSubmitting">极速结算生成台账</text>
        <text v-else>结算入库中...</text>
      </button>
      <view class="safe-area-bottom"></view>
    </view>
  </view>
</template>

<script>
import { getBatch, listBatchLog, listBatchResult, updateBatch, confirmAndGenerate } from '@/api/biz'
import config from '@/config'

export default {
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,
      batchId: null,
      
      batch: {},
      imageUrls: [], 
      resultList: [],
      
      isSubmitting: false
    }
  },
  computed: {
    isEditable() {
      return this.batch.status === '1'
    },
    activeResultList() {
      return this.resultList.filter(item => item.status !== '1')
    }
  },
  onLoad(options) {
    this.initSystemInfo()
    if (options.id) {
      this.batchId = options.id
      this.fetchBatchDetail()
    } else {
      this.$modal.msgError("非法访问：缺少批次ID")
    }
  },
  methods: {
    initSystemInfo() {
      const sys = uni.getSystemInfoSync()
      this.statusBarHeight = sys.statusBarHeight || 20
      // #ifdef MP-WEIXIN
      let custom = uni.getMenuButtonBoundingClientRect()
      this.navBarHeight = custom.height + (custom.top - this.statusBarHeight) * 2
      // #endif
    },

    goBack() {
      uni.navigateBack({ delta: 1 })
    },

    // ======== 终极 URL 路径格式化工具 (抹平物理盘符与反斜杠) ========
    getRealImageUrl(url) {
      if (!url) return '';
      const baseURL = config.baseUrl || process.env.VUE_APP_BASE_API;
      
      let normalizedUrl = url.replace(/\\/g, '/');

      if (normalizedUrl.indexOf('/uploadPath/') !== -1) {
        let relative = normalizedUrl.substring(normalizedUrl.indexOf('/uploadPath/') + 11);
        return baseURL + '/profile' + relative;
      }

      if (normalizedUrl.indexOf('/ocr/') !== -1 && normalizedUrl.indexOf('/profile') === -1) {
         let relative = normalizedUrl.substring(normalizedUrl.indexOf('/ocr/'));
         return baseURL + '/profile' + relative;
      }


      if (normalizedUrl.indexOf('/profile') !== -1) {
        let relative = normalizedUrl.substring(normalizedUrl.indexOf('/profile'));
        return baseURL + relative;
      }

      return normalizedUrl.startsWith('http') ? normalizedUrl : baseURL + normalizedUrl;
    },

    async fetchBatchDetail() {
      uni.showLoading({ title: '加载工作台...' })
      try {
        // 1. 获取批次基础信息
        const res = await getBatch(this.batchId)
        this.batch = res.data || {}

        // 2. 获取真实的原始凭证图片 
        const logRes = await listBatchLog({ batchId: this.batchId })
        if (logRes.rows && logRes.rows.length > 0) {
           // 利用上方写好的神级正则方法转换图片 URL
           this.imageUrls = logRes.rows.map(log => this.getRealImageUrl(log.fileUrl)).filter(url => url !== '')
        } else {
           this.imageUrls = []
        }

        // 3. 获取人员解析明细
        const resultRes = await listBatchResult({ batchId: this.batchId, pageSize: 500 })
        this.resultList = resultRes.rows.filter(item => item.status !== '2')
        
        uni.hideLoading()
      } catch (error) {
        uni.hideLoading()
        this.$modal.msgError('获取数据失败')
      }
    },

    removeItem(index) {
      uni.showModal({
        title: '提示',
        content: '确定将该条识别记录作废剔除吗？',
        success: (res) => {
          if (res.confirm) {
            this.$set(this.resultList[index], 'status', '1')
          }
        }
      })
    },

    async handleConfirmImport() {
      uni.showModal({
        title: '最终确认',
        content: '确认基础信息和人员明细无误，立即生成最终台账？',
        success: async (res) => {
          if (res.confirm) {
            this.isSubmitting = true
            try {
              await updateBatch(this.batch)
              const editedList = this.resultList.filter(item => item.status !== '1')
              await confirmAndGenerate(this.batchId, editedList)
              
              this.isSubmitting = false
              uni.showToast({ title: '台账生成成功！', icon: 'success', duration: 2000 })
              
              setTimeout(() => { uni.navigateBack() }, 2000)
            } catch (error) {
              this.isSubmitting = false
              this.$modal.msgError('结算失败，请重试')
            }
          }
        }
      })
    },

    getStatusText(status) {
      const map = { '0': 'AI 解析中', '1': '待校对入库', '2': '已结算台账', '3': '解析异常' }
      return map[status] || '未知'
    },
    getStatusClass(status) {
      const map = { '0': 'status-blue', '1': 'status-orange', '2': 'status-black', '3': 'status-red' }
      return map[status] || 'status-gray'
    },
    previewImage(index) {
      if (this.imageUrls.length > 0) uni.previewImage({ urls: this.imageUrls, current: index })
    }
  }
}
</script>

<style lang="scss" scoped>
.batch-review-page {
  width: 100vw; min-height: 100vh; background-color: #f8faff; position: relative; overflow-x: hidden; padding-bottom: 180rpx;
  
  .blob { position: absolute; width: 600rpx; height: 600rpx; background: linear-gradient(135deg, rgba(60, 156, 255, 0.1), rgba(122, 187, 255, 0.05)); filter: blur(90px); border-radius: 50%; z-index: 0; animation: move 20s infinite alternate; }
  .blob-1 { top: -100rpx; right: -150rpx; }
  .blob-2 { bottom: 200rpx; left: -150rpx; background: rgba(166, 140, 227, 0.08); width: 400rpx; height: 400rpx; }
  @keyframes move { from { transform: translate(0, 0) scale(1); } to { transform: translate(40rpx, 80rpx) scale(1.05); } }

  .custom-navbar { position: fixed; top: 0; left: 0; right: 0; z-index: 999; display: flex; align-items: center; justify-content: space-between; padding: 0 30rpx; }
  .glass-nav { background: rgba(248, 250, 255, 0.85); backdrop-filter: blur(20px); border-bottom: 1px solid rgba(0, 0, 0, 0.02); }
  .nav-left, .nav-right { width: 80rpx; display: flex; align-items: center; }
  .nav-center { flex: 1; display: flex; justify-content: center; .nav-title { font-size: 32rpx; font-weight: 800; color: #1a1a1a; letter-spacing: 0.5px; } }

  .main-container { position: relative; z-index: 1; width: 92%; max-width: 680rpx; margin: 0 auto; }
  .glass-panel { background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(25px); border: 1px solid rgba(255, 255, 255, 0.8); box-shadow: 0 20rpx 40rpx rgba(0, 0, 0, 0.03); }

  /* 顶部流水号与状态 */
  .status-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30rpx; padding: 0 10rpx;
    .b-no { font-size: 38rpx; font-weight: 900; color: #1a1a1a; font-family: monospace; }
    .badge { display: flex; align-items: center; padding: 6rpx 20rpx; border-radius: 12rpx; border: 1px solid rgba(0,0,0,0.03);
      .dot { width: 10rpx; height: 10rpx; border-radius: 50%; margin-right: 10rpx; } text { font-size: 22rpx; font-weight: 700; }
    }
    .status-blue { background: rgba(60,156,255,0.1); .dot { background: #3c9cff; } text { color: #3c9cff; } }
    .status-orange { background: rgba(249,174,61,0.1); .dot { background: #f9ae3d; } text { color: #f9ae3d; } }
    .status-black { background: rgba(26,26,26,0.05); .dot { background: #1a1a1a; } text { color: #1a1a1a; } }
  }

  .section-top { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 24rpx;
    .title { font-size: 28rpx; font-weight: 800; color: #1a1a1a; }
    .desc { font-size: 22rpx; color: #909399; }
  }

  /* 1. 画廊区 */
  .gallery-card { padding: 30rpx; margin-bottom: 30rpx; border-radius: 36rpx;
    .empty-state { text-align: center; padding: 40rpx 0; }
    .gallery-scroll { white-space: nowrap; width: 100%; }
    .gallery-track { display: inline-flex; gap: 20rpx; }
    .img-wrap { width: 180rpx; height: 250rpx; border-radius: 16rpx; overflow: hidden; position: relative; border: 1px solid rgba(0,0,0,0.05); background: #f0f2f5;
      image { width: 100%; height: 100%; display: block; }
      .page-tag { position: absolute; bottom: 8rpx; right: 8rpx; background: rgba(255,255,255,0.9); font-size: 20rpx; font-weight: 800; padding: 2rpx 10rpx; border-radius: 8rpx; color:#1a1a1a;}
    }
  }

  /* 2. 表单区 */
  .form-card { padding: 30rpx; margin-bottom: 40rpx; border-radius: 36rpx;
    .form-group { display: flex; flex-direction: column;
      .form-item { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 0; border-bottom: 1px dashed rgba(0,0,0,0.05);
        &.no-border { border-bottom: none; }
        .label { font-size: 26rpx; color: #606266; font-weight: 600; width: 160rpx; }
        .value-input { flex: 1; text-align: right; font-size: 28rpx; color: #1a1a1a; font-weight: 700; }
      }
    }
  }

  /* 3. 人员明细卡片流 */
  .person-section {
    .empty-state { text-align: center; padding: 60rpx 0; color: #ccc; font-size: 26rpx; }
    .person-list { display: flex; flex-direction: column; gap: 24rpx; }
    
    .person-card { padding: 30rpx; border-radius: 28rpx;
      .p-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; padding-bottom: 16rpx; border-bottom: 1px solid rgba(0,0,0,0.03);
        .p-seq { font-size: 24rpx; color: #909399; font-weight: 800; font-family: monospace; display: flex; align-items: center; 
          .inline-input { margin-left: 10rpx; background: rgba(0,0,0,0.03); padding: 2rpx 8rpx; border-radius: 8rpx; text-align: center;}
        }
      }
      .p-body { display: flex; justify-content: space-between;
        .p-field { display: flex; flex-direction: column; flex: 1;
          .p-label { font-size: 20rpx; color: #909399; margin-bottom: 8rpx; }
          .p-input { font-size: 26rpx; color: #333; }
          .font-bold { font-weight: 800; font-size: 28rpx; color: #1a1a1a; }
          .highlight-color { color: #3c9cff; font-weight: 700; }
        }
      }
      .p-footer { margin-top: 16rpx; .new-user-tip { font-size: 20rpx; color: #fa3534; font-weight: 600; } }
    }
  }

  /* 底部提交悬浮区 */
  .bottom-action { position: fixed; bottom: 0; left: 0; right: 0; padding: 30rpx 6%; background: linear-gradient(to top, rgba(248,250,255,1) 40%, rgba(248,250,255,0)); z-index: 99;
    .dark-submit-btn { width: 100%; height: 100rpx; background: #1a1a1a; border-radius: 28rpx; display: flex; justify-content: center; align-items: center; color: #fff; font-size: 30rpx; font-weight: 800; border: none; box-shadow: 0 20rpx 40rpx rgba(26, 26, 26, 0.15); transition: all 0.2s;
      &::after { border: none; } &:active { transform: scale(0.97); }
    }
    .safe-area-bottom { height: env(safe-area-inset-bottom); }
  }
}
</style>