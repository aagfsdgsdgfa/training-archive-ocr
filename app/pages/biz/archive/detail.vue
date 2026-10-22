<template>
  <view class="archive-detail-page">
    <view class="blob blob-1"></view>
    <view class="blob blob-2"></view>

    <view class="custom-navbar glass-nav" :style="{ paddingTop: statusBarHeight + 'px', height: navBarHeight + 'px' }">
      <view class="nav-left" @click="goBack"><uni-icons type="left" color="#1a1a1a" size="24"></uni-icons></view>
      <view class="nav-center"><text class="nav-title">Record</text></view>
      <view class="nav-right"></view>
    </view>

    <view class="main-container" :style="{ paddingTop: (statusBarHeight + navBarHeight + 20) + 'px' }">
      
      <view class="hero-certificate glass-panel">
        <view class="cert-watermark">
          <uni-icons type="vip-filled" color="rgba(0,0,0,0.02)" size="120"></uni-icons>
        </view>
        
        <view class="hero-top">
          <view class="tag-row">
            <view class="status-tag">
              <view class="dot"></view><text>已入档</text>
            </view>
            <text class="seq-tag" v-if="archive.signSeq">签到序号: {{ archive.signSeq }}</text>
          </view>
        </view>

        <view class="hero-center">
          <view class="score-display" :class="getScoreColor(archive.score)">
            <text class="score-num">{{ formatScore(archive.score) }}</text>
            <text class="score-unit" v-if="isNumeric(archive.score)">PTS</text>
          </view>
          <text class="subject-title">{{ archive.subject || archive.trainContent || '加载中...' }}</text>
          <text class="user-name">{{ archive.userName || '员工' }} · {{ archive.deptName || archive.trainDept || '部门' }}</text>
        </view>
      </view>

      <view class="section-title"><text class="title">培训元数据</text></view>

      <view class="info-grid glass-panel">
        <view class="grid-item">
          <text class="g-label">授课人</text>
          <text class="g-value">{{ archive.speaker || '-' }}</text>
        </view>
        <view class="grid-item">
          <text class="g-label">认证学时</text>
          <text class="g-value num-font highlight">{{ archive.trainHours || '0' }} h</text>
        </view>
        <view class="grid-item">
          <text class="g-label">培训日期</text>
          <text class="g-value num-font">{{ formatDate(archive.trainDate) }}</text>
        </view>
        <view class="grid-item">
          <text class="g-label">主办部门</text>
          <text class="g-value">{{ archive.trainDept || '-' }}</text>
        </view>

        <view class="grid-item full-width border-top">
          <text class="g-label">地点</text>
          <text class="g-value">{{ archive.trainLocation || '未记录地点' }}</text>
        </view>
      </view>

      <view class="section-title" style="margin-top: 50rpx;"><text class="title">归档凭证</text></view>
      
      <view class="proof-card glass-panel">
        <view v-if="proofImages.length === 0" class="empty-text">未关联原始签到表图片</view>
        <view v-else class="proof-scroll-wrap">
          <scroll-view scroll-x class="proof-scroll" :show-scrollbar="false">
            <view class="proof-track">
              <view class="proof-img-wrap" v-for="(img, idx) in proofImages" :key="idx" @click="previewProof(idx)">
                <image :src="img" mode="aspectFill"></image>
                <view class="img-mask"><uni-icons type="search" color="#fff" size="20"></uni-icons></view>
              </view>
            </view>
          </scroll-view>
        </view>
      </view>

      <view class="content-card glass-panel" v-if="archive.remark">
        <view class="content-block">
          <text class="c-label">审计备注</text>
          <view class="remark-box">
            <text class="r-text">{{ archive.remark }}</text>
          </view>
        </view>
      </view>

      <view class="traceability-mark">
        <text>入档时间: {{ archive.createTime || '-' }}</text>
        <text>数字验真标识: {{ archive.archiveId ? 'SYS-ARC-' + archive.archiveId : 'UNVERIFIED' }}</text>
      </view>

    </view>
  </view>
</template>

<script>
import { getArchive } from '@/api/biz'
import config from '@/config'

export default {
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,
      archiveId: null,
      
      archive: {},
      proofImages: []
    }
  },
  onLoad(options) {
    this.initSystemInfo()
    if (options.id) {
      this.archiveId = options.id
      this.fetchArchiveDetail()
    } else {
      this.$modal.msgError("非法访问：缺少档案ID")
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
      const pages = getCurrentPages()
      if (pages.length > 1) { uni.navigateBack({ delta: 1 }) } 
      else { this.$tab.switchTab('/pages/biz/archive/index') }
    },

    async fetchArchiveDetail() {
      uni.showLoading({ title: '提取履历...' })
      try {
        const res = await getArchive(this.archiveId)
        const data = res.data || {}
        this.archive = data

        // 处理附件图片：补全域名前缀
        const baseURL = config.baseUrl || process.env.VUE_APP_BASE_API
        if (data.attachmentUrl) {
          this.proofImages = data.attachmentUrl.split(',').map(url => {
            return url.startsWith('http') ? url : baseURL + url
          })
        } else {
          this.proofImages = []
        }
        uni.hideLoading()
      } catch (error) {
        uni.hideLoading()
        console.error(error)
      }
    },

    previewProof(index) {
      if (this.proofImages.length > 0) {
        uni.previewImage({ urls: this.proofImages, current: index })
      }
    },

    formatDate(dateStr) { return dateStr ? dateStr.substring(0, 10).replace(/-/g, '.') : '-' },
    isNumeric(val) { return !isNaN(parseFloat(val)) && isFinite(val) },
    formatScore(score) {
      if (!score || score === 'null') return 'PASS'
      if (score === '√' || score === '合格') return 'PASS'
      return score
    },
    getScoreColor(score) {
      const num = parseFloat(score)
      if (!isNaN(num) && num < 60) return 'text-danger'
      return 'text-black'
    }
  }
}
</script>

<style lang="scss" scoped>
.archive-detail-page {
  width: 100vw; min-height: 100vh; background-color: #f8faff; position: relative; overflow-x: hidden; padding-bottom: 80rpx;
  .blob { position: absolute; width: 600rpx; height: 600rpx; background: linear-gradient(135deg, rgba(60, 156, 255, 0.1), rgba(122, 187, 255, 0.05)); filter: blur(90px); border-radius: 50%; z-index: 0; animation: move 20s infinite alternate; }
  .blob-1 { top: -100rpx; right: -150rpx; }
  .blob-2 { bottom: 200rpx; left: -150rpx; background: rgba(166, 140, 227, 0.08); width: 400rpx; height: 400rpx; }
  @keyframes move { from { transform: translate(0, 0) scale(1); } to { transform: translate(40rpx, 80rpx) scale(1.05); } }

  .custom-navbar { position: fixed; top: 0; left: 0; right: 0; z-index: 999; display: flex; align-items: center; justify-content: space-between; padding: 0 30rpx; }
  .glass-nav { background: rgba(248, 250, 255, 0.85); backdrop-filter: blur(20px); border-bottom: 1px solid rgba(0, 0, 0, 0.02); }
  .nav-left, .nav-right { width: 80rpx; display: flex; align-items: center; }
  .nav-center { flex: 1; display: flex; justify-content: center; .nav-title { font-size: 34rpx; font-weight: 800; color: #1a1a1a; letter-spacing: 1px; text-transform: uppercase; } }

  .main-container { position: relative; z-index: 1; width: 88%; max-width: 680rpx; margin: 0 auto; }
  .glass-panel { background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(25px); border: 1px solid rgba(255, 255, 255, 0.8); box-shadow: 0 30rpx 60rpx rgba(0, 0, 0, 0.03); }

  .hero-certificate { border-radius: 40rpx; padding: 50rpx 40rpx; margin-bottom: 50rpx; position: relative; overflow: hidden;
    .cert-watermark { position: absolute; right: -30rpx; bottom: -30rpx; transform: rotate(-15deg); pointer-events: none; }
    .hero-top { margin-bottom: 40rpx; position: relative; z-index: 1;
      .tag-row { display: flex; justify-content: space-between; align-items: center;
        .status-tag { display: flex; align-items: center; background: #1a1a1a; padding: 8rpx 24rpx; border-radius: 12rpx; .dot { width: 10rpx; height: 10rpx; background: #fff; border-radius: 50%; margin-right: 12rpx; } text { font-size: 22rpx; color: #fff; font-weight: 700; letter-spacing: 1px; } }
        .seq-tag { font-size: 24rpx; color: #909399; font-family: monospace; font-weight: 600; }
      }
    }
    .hero-center { display: flex; flex-direction: column; align-items: center; position: relative; z-index: 1;
      .score-display { display: flex; align-items: baseline; margin-bottom: 20rpx; .score-num { font-size: 110rpx; font-weight: 900; line-height: 1; font-family: monospace; letter-spacing: -4px; } .score-unit { font-size: 26rpx; font-weight: 800; margin-left: 10rpx; padding-bottom: 20rpx; } }
      .text-black { color: #1a1a1a; } .text-danger { color: #fa3534; }
      .subject-title { font-size: 36rpx; font-weight: 900; color: #1a1a1a; text-align: center; margin-bottom: 12rpx; }
      .user-name { font-size: 26rpx; color: #606266; font-weight: 600; }
    }
  }

  .section-title { display: flex; align-items: center; margin-bottom: 30rpx; padding-left: 10rpx; .title { font-size: 26rpx; font-weight: 800; color: #909399; text-transform: uppercase; letter-spacing: 2px; } }

  .info-grid { border-radius: 36rpx; padding: 40rpx; display: flex; flex-wrap: wrap; gap: 40rpx 0; margin-bottom: 50rpx;
    .grid-item { width: 50%; display: flex; flex-direction: column; .g-label { font-size: 22rpx; color: #909399; font-weight: 600; margin-bottom: 8rpx; } .g-value { font-size: 28rpx; color: #1a1a1a; font-weight: 800; padding-right: 20rpx; }
      .num-font { font-family: monospace; font-size: 32rpx; }
      .highlight { color: #1a1a1a; background: rgba(0,0,0,0.04); padding: 4rpx 16rpx; border-radius: 10rpx; align-self: flex-start; }
    }
    .full-width { width: 100%; } .border-top { border-top: 1px dashed rgba(0,0,0,0.06); padding-top: 30rpx; }
  }

  .proof-card { padding: 30rpx; margin-bottom: 50rpx; border-radius: 36rpx;
    .empty-text { font-size: 26rpx; color: #ccc; text-align: center; padding: 40rpx 0; }
    .proof-scroll-wrap { width: 100%;
      .proof-scroll { white-space: nowrap; width: 100%; }
      .proof-track { display: inline-flex; gap: 24rpx; padding: 10rpx 0; }
      .proof-img-wrap { width: 220rpx; height: 300rpx; border-radius: 20rpx; overflow: hidden; position: relative; box-shadow: 0 10rpx 20rpx rgba(0,0,0,0.06); flex-shrink: 0; border: 1px solid rgba(0,0,0,0.02);
        image { width: 100%; height: 100%; display: block; background: #f0f2f5; }
        .img-mask { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.2); opacity: 0; transition: opacity 0.3s; display: flex; justify-content: center; align-items: center; }
        &:active .img-mask { opacity: 1; }
      }
    }
  }

  .content-card { border-radius: 36rpx; padding: 40rpx; margin-bottom: 60rpx;
    .content-block { display: flex; flex-direction: column; .c-label { font-size: 24rpx; color: #1a1a1a; font-weight: 800; margin-bottom: 16rpx; }
      .remark-box { background: rgba(250,200,88,0.1); border-left: 6rpx solid #e6a23c; padding: 20rpx 24rpx; border-radius: 0 16rpx 16rpx 0;
        .r-text { font-size: 26rpx; color: #e6a23c; font-weight: 600; line-height: 1.5; }
      }
    }
  }

  .traceability-mark { display: flex; flex-direction: column; align-items: center; opacity: 0.6; text { font-size: 20rpx; color: #909399; font-family: monospace; letter-spacing: 1px; margin-bottom: 6rpx; } }
}
</style>