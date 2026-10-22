<template>
  <view class="ledger-detail-page">
    <view class="blob blob-1"></view>
    <view class="blob blob-2"></view>

    <view class="custom-navbar glass-nav" :style="{ paddingTop: statusBarHeight + 'px', height: navBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" color="#1a1a1a" size="24"></uni-icons>
      </view>
      <view class="nav-center">
        <text class="nav-title">Detail</text>
      </view>
      <view class="nav-right"></view>
    </view>

    <view class="main-container" :style="{ paddingTop: (statusBarHeight + navBarHeight + 20) + 'px' }">
      
      <view class="hero-section">
        <view class="category-row">
          <view class="category-tag">
            <view class="dot"></view>
            <text>{{ ledger.projectCategory || '常规培训' }}</text>
          </view>
          <view class="level-tag" v-if="ledger.trainLevel">
            <text>{{ ledger.trainLevel }}</text>
          </view>
        </view>
        <text class="hero-title">{{ ledger.subject || '加载中...' }}</text>
        <text class="hero-date">{{ formatDate(ledger.trainDate) }}</text>
      </view>

      <view class="info-grid-card glass-panel">
        <view class="grid-item">
          <text class="g-label">培训讲师</text>
          <text class="g-value">{{ ledger.speaker || '-' }}</text>
        </view>
        <view class="grid-item">
          <text class="g-label">有效学时</text>
          <text class="g-value number-font">{{ ledger.trainHours || '0' }} h</text>
        </view>
        <view class="grid-item">
          <text class="g-label">主办部门</text>
          <text class="g-value">{{ ledger.hostDeptName || '-' }}</text>
        </view>
        <view class="grid-item">
          <text class="g-label">实到人数</text>
          <text class="g-value highlight number-font">{{ ledger.actualCount || 0 }} 人</text>
        </view>
        
        <view class="grid-item full-width border-top">
          <text class="g-label">培训地点</text>
          <text class="g-value">{{ ledger.locationName || '未记录地点' }}</text>
        </view>
        
        <view class="grid-item full-width border-top" v-if="ledger.remark">
          <text class="g-label">台账备注</text>
          <text class="g-value remark-text">{{ ledger.remark }}</text>
        </view>
      </view>

      <view class="section-title">
        <text class="title">参训人员</text>
        <text class="count-badge">{{ attendeeList.length }} 人已入账</text>
      </view>
      
      <view class="attendee-card glass-panel">
        <view v-if="attendeeList.length === 0" class="empty-text">暂无人员明细</view>
        <view v-else class="tags-wrapper">
          <view class="person-tag" v-for="(person, index) in attendeeList" :key="index">
            <text>{{ person }}</text>
          </view>
        </view>
      </view>

      <view class="section-title" style="margin-top: 60rpx;">
        <text class="title">原始数字凭证</text>
      </view>

      <view class="proof-card glass-panel">
        <view v-if="proofImages.length === 0" class="empty-text">未关联原始图片</view>
        <view v-else class="proof-scroll-wrap">
          <scroll-view scroll-x class="proof-scroll" :show-scrollbar="false">
            <view class="proof-track">
              <view class="proof-img-wrap" v-for="(img, idx) in proofImages" :key="idx" @click="previewProof(idx)">
                <image :src="img" mode="aspectFill"></image>
                <view class="img-mask">
                  <uni-icons type="search" color="#fff" size="20"></uni-icons>
                </view>
              </view>
            </view>
          </scroll-view>
        </view>
      </view>

    </view>
    <view class="bottom-spacer"></view>
  </view>
</template>

<script>
import { getLedger } from '@/api/biz'
import config from '@/config' // 引入配置获取 baseUrl

export default {
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,
      ledgerId: null,
      
      ledger: {},
      attendeeList: [],
      proofImages: []
    }
  },
  onLoad(options) {
    this.initSystemInfo()
    if (options.id) {
      this.ledgerId = options.id
      this.fetchLedgerDetail()
    } else {
      // 本地测试兜底
      this.ledgerId = 102
      this.fetchLedgerDetail()
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
        this.$tab.switchTab('/pages/biz/ledger/index')
      }
    },

    async fetchLedgerDetail() {
      uni.showLoading({ title: '获取卷宗...' })
      try {
        const res = await getLedger(this.ledgerId)
        const data = res.data || {}
        this.ledger = data
        
        // 1. 处理人员名单 (将 "杨桑,阿bin" 转为数组)
        if (data.attendeeNames) {
          this.attendeeList = data.attendeeNames.split(',').filter(name => name.trim() !== '')
        } else {
          this.attendeeList = []
        }
        
        // 2. 处理图片路径 (拼接全路径)
        const baseURL = config.baseUrl || process.env.VUE_APP_BASE_API
        if (data.attachmentUrl) {
          // 如果有多张图片逗号分隔，也予以支持
          this.proofImages = data.attachmentUrl.split(',').map(url => {
            return url.startsWith('http') ? url : baseURL + url
          })
        } else {
          this.proofImages = []
        }
        
        uni.hideLoading()
      } catch (error) {
        uni.hideLoading()
        console.error('获取台账详情失败:', error)
      }
    },

    formatDate(dateStr) {
      if (!dateStr) return ''
      return dateStr.substring(0, 10).replace(/-/g, '.')
    },

    previewProof(index) {
      if (!this.proofImages || this.proofImages.length === 0) return
      uni.previewImage({
        urls: this.proofImages,
        current: index
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.ledger-detail-page {
  width: 100vw;
  min-height: 100vh;
  background-color: #f8faff;
  position: relative;
  overflow-x: hidden;

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

  /* 自定义导航栏 */
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
    .nav-title { font-size: 34rpx; font-weight: 800; color: #1a1a1a; letter-spacing: 0.5px; }
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
    border-radius: 40rpx;
  }

  /* ================= 1. 顶部 Hero 概览区 ================= */
  .hero-section {
    padding: 20rpx 0 50rpx;
    display: flex;
    flex-direction: column;
    align-items: flex-start;

    .category-row {
      display: flex; align-items: center; margin-bottom: 30rpx; gap: 20rpx;
      
      .category-tag {
        display: flex; align-items: center; background: rgba(26,26,26,0.04);
        padding: 10rpx 24rpx; border-radius: 14rpx; 
        .dot { width: 12rpx; height: 12rpx; background: #1a1a1a; border-radius: 50%; margin-right: 14rpx; }
        text { font-size: 24rpx; color: #1a1a1a; font-weight: 700; }
      }

      .level-tag {
        background: #f4f4f5; padding: 10rpx 24rpx; border-radius: 14rpx;
        text { font-size: 24rpx; color: #606266; font-weight: 600; }
      }
    }

    .hero-title {
      font-size: 52rpx; font-weight: 900; color: #1a1a1a; line-height: 1.3;
      letter-spacing: -1px; margin-bottom: 20rpx;
    }

    .hero-date {
      font-size: 28rpx; color: #909399; font-weight: 600; font-family: monospace;
    }
  }

  /* ================= 2. 核心信息网格卡片 ================= */
  .info-grid-card {
    padding: 40rpx;
    display: flex;
    flex-wrap: wrap;
    gap: 40rpx 0;
    margin-bottom: 60rpx;

    .grid-item {
      width: 50%;
      display: flex;
      flex-direction: column;

      .g-label { font-size: 24rpx; color: #909399; font-weight: 600; margin-bottom: 12rpx; }
      .g-value { font-size: 30rpx; color: #1a1a1a; font-weight: 800; padding-right: 20rpx; }
      
      .number-font { font-family: monospace; font-size: 32rpx; }
      .highlight { color: #3c9cff; }
    }

    .full-width { width: 100%; }
    .border-top { border-top: 1px dashed rgba(0,0,0,0.06); padding-top: 30rpx; }
    
    .remark-text {
      font-size: 26rpx !important; color: #606266 !important; font-weight: 500 !important; 
      line-height: 1.6; background: rgba(0,0,0,0.02); padding: 20rpx; border-radius: 16rpx; margin-top: 10rpx;
    }
  }

  /* ================= 标题公用类 ================= */
  .section-title {
    display: flex; align-items: center; justify-content: space-between; margin-bottom: 30rpx;
    .title { font-size: 34rpx; font-weight: 800; color: #1a1a1a; }
    .count-badge { background: #f4f4f5; color: #606266; font-size: 22rpx; font-weight: 600; padding: 6rpx 20rpx; border-radius: 12rpx; }
  }

  /* ================= 3. 参训人员卡片 ================= */
  .attendee-card {
    padding: 40rpx;
    margin-bottom: 60rpx;

    .empty-text { font-size: 26rpx; color: #ccc; text-align: center; padding: 20rpx 0; }

    .tags-wrapper {
      display: flex; flex-wrap: wrap; gap: 20rpx;
      
      .person-tag {
        background: rgba(255,255,255,0.8);
        border: 1px solid rgba(0,0,0,0.04);
        padding: 12rpx 28rpx;
        border-radius: 20rpx;
        transition: transform 0.2s;
        box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.01);

        &:active { transform: scale(0.95); background: #f4f4f5; }
        text { font-size: 26rpx; color: #333; font-weight: 600; }
      }
    }
  }

  /* ================= 4. 数字凭证缩略图 ================= */
  .proof-card {
    padding: 30rpx;
    
    .empty-text { font-size: 26rpx; color: #ccc; text-align: center; padding: 40rpx 0; }

    .proof-scroll-wrap {
      width: 100%;
      
      .proof-scroll {
        white-space: nowrap; width: 100%;
      }

      .proof-track {
        display: inline-flex; gap: 24rpx; padding: 10rpx 0;
      }

      .proof-img-wrap {
        width: 220rpx; height: 300rpx;
        border-radius: 20rpx; overflow: hidden; position: relative;
        box-shadow: 0 10rpx 20rpx rgba(0,0,0,0.06);
        flex-shrink: 0; border: 1px solid rgba(0,0,0,0.02);

        image { width: 100%; height: 100%; display: block; background: #f0f2f5; }

        .img-mask {
          position: absolute; top: 0; left: 0; right: 0; bottom: 0;
          background: rgba(0,0,0,0.2); opacity: 0; transition: opacity 0.3s;
          display: flex; justify-content: center; align-items: center;
        }

        &:active .img-mask { opacity: 1; }
      }
    }
  }

  .bottom-spacer { height: 100rpx; }
}
</style>