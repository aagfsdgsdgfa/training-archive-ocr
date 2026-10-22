<template>
  <view class="plan-detail-page">
    <view class="blob blob-1"></view>
    <view class="blob blob-2"></view>

    <view class="custom-navbar glass-nav" :style="{ paddingTop: statusBarHeight + 'px', height: navBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" color="#1a1a1a" size="24"></uni-icons>
      </view>
      <view class="nav-center">
        <text class="nav-title">Plan Detail</text>
      </view>
      <view class="nav-right"></view>
    </view>

    <view class="main-container" :style="{ paddingTop: (statusBarHeight + navBarHeight + 20) + 'px' }">
      
      <view class="hero-section">
        <view class="tag-row">
          <view class="status-tag" :class="uiState.class">
            <view class="dot"></view>
            <text>{{ uiState.text }}</text>
          </view>
          
          <view class="type-tag">
            <text>{{ plan.planType === '1' ? '计划内' : '计划外' }}</text>
          </view>
        </view>

        <text class="hero-title">{{ plan.subject || '加载中...' }}</text>
        <text class="hero-date">计划实施月：<text class="num-font">{{ plan.planYear }}.{{ formatMonth(plan.planMonth) }}</text></text>
      </view>

      <view class="section-title">
        <text class="title">计划元数据</text>
      </view>

      <view class="info-grid glass-panel">
        <view class="grid-item">
          <text class="g-label">项目分类</text>
          <text class="g-value">{{ plan.projectCategory || '-' }}</text>
        </view>
        <view class="grid-item">
          <text class="g-label">培训级别</text>
          <text class="g-value">{{ plan.trainLevel || '-' }}</text>
        </view>
        <view class="grid-item">
          <text class="g-label">主办部门</text>
          <text class="g-value">{{ plan.hostDeptName || '-' }}</text>
        </view>
        <view class="grid-item">
          <text class="g-label">受训范围</text>
          <text class="g-value highlight">{{ getAudienceScope(plan.audienceScope) }}</text>
        </view>

        <view class="grid-item full-width border-top">
          <text class="g-label">计划立项时间</text>
          <text class="g-value num-font">{{ plan.createTime || '-' }}</text>
        </view>
      </view>

      <view v-if="plan.remark" class="section-title" style="margin-top: 50rpx;">
        <text class="title">项目备注</text>
      </view>
      
      <view v-if="plan.remark" class="remark-card glass-panel">
        <text class="remark-text">{{ plan.remark }}</text>
      </view>

      <view class="traceability-mark">
        <text>System Plan ID: {{ plan.planId || 'UNVERIFIED' }}</text>
      </view>

    </view>
  </view>
</template>

<script>
import { getPlan } from '@/api/biz'

export default {
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,
      planId: null,
      
      plan: {},
      uiState: { text: '未知', class: 'status-gray' } 
    }
  },
  onLoad(options) {
    this.initSystemInfo()
    if (options.id) {
      this.planId = options.id
      this.fetchPlanDetail()
    } else {
      this.$modal.msgError("非法访问：缺少计划ID")
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
      if (pages.length > 1) { 
        uni.navigateBack({ delta: 1 }) 
      } else { 
        uni.reLaunch({
        	url:"/pages/biz/plan"
        })
      }
    },

    async fetchPlanDetail() {
      uni.showLoading({ title: '拉取详情...' })
      try {
        const res = await getPlan(this.planId)
        this.plan = res.data || {}
        this.calculateUiState(this.plan)
        uni.hideLoading()
      } catch (error) {
        uni.hideLoading()
        console.error('获取计划详情失败:', error)
      }
    },

    // 智能加工：同列表页一样，在前端计算出计划是否逾期
    calculateUiState(plan) {
      const now = new Date()
      const currentYear = now.getFullYear()
      const currentMonth = now.getMonth() + 1

      if (plan.status === '1') {
        this.uiState = { text: '已完成', class: 'status-black' }
      } else if (plan.status === '0') {
        if (plan.planYear < currentYear || (plan.planYear === currentYear && plan.planMonth < currentMonth)) {
          this.uiState = { text: '已逾期', class: 'status-red' }
        } else {
          this.uiState = { text: '待执行', class: 'status-orange' }
        }
      }
    },

    formatMonth(month) {
      if (!month) return '01'
      return String(month).padStart(2, '0')
    },

    // 字典映射 (根据您的实际字典修改)
    getAudienceScope(scopeValue) {
      if (scopeValue === '0') return '全员'
      if (scopeValue === '1') return '部门级'
      if (scopeValue === '2') return '特定人员'
      return '未指定'
    }
  }
}
</script>

<style lang="scss" scoped>
.plan-detail-page {
  width: 100vw; min-height: 100vh; background-color: #f8faff; position: relative; overflow-x: hidden; padding-bottom: 80rpx;
  
  /* 背景光晕 */
  .blob { position: absolute; width: 600rpx; height: 600rpx; background: linear-gradient(135deg, rgba(60, 156, 255, 0.1), rgba(122, 187, 255, 0.05)); filter: blur(90px); border-radius: 50%; z-index: 0; animation: move 20s infinite alternate; }
  .blob-1 { top: -100rpx; right: -150rpx; }
  .blob-2 { bottom: 200rpx; left: -150rpx; background: rgba(166, 140, 227, 0.08); width: 400rpx; height: 400rpx; }
  @keyframes move { from { transform: translate(0, 0) scale(1); } to { transform: translate(40rpx, 80rpx) scale(1.05); } }

  /* 自定义导航栏 */
  .custom-navbar { position: fixed; top: 0; left: 0; right: 0; z-index: 999; display: flex; align-items: center; justify-content: space-between; padding: 0 30rpx; }
  .glass-nav { background: rgba(248, 250, 255, 0.85); backdrop-filter: blur(20px); border-bottom: 1px solid rgba(0, 0, 0, 0.02); }
  .nav-left, .nav-right { width: 80rpx; display: flex; align-items: center; }
  .nav-center { flex: 1; display: flex; justify-content: center; .nav-title { font-size: 34rpx; font-weight: 800; color: #1a1a1a; letter-spacing: 0.5px; } }

  .main-container { position: relative; z-index: 1; width: 88%; max-width: 680rpx; margin: 0 auto; }
  .glass-panel { background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(25px); border: 1px solid rgba(255, 255, 255, 0.8); box-shadow: 0 30rpx 60rpx rgba(0, 0, 0, 0.03); }

  /* ================= 1. 顶部 Hero 区 ================= */
  .hero-section {
    padding: 20rpx 0 50rpx; display: flex; flex-direction: column; align-items: flex-start;

    .tag-row { display: flex; gap: 20rpx; margin-bottom: 30rpx; align-items: center;
      .status-tag { display: flex; align-items: center; padding: 8rpx 20rpx; border-radius: 12rpx; border: 1px solid rgba(0,0,0,0.03);
        .dot { width: 10rpx; height: 10rpx; border-radius: 50%; margin-right: 10rpx; }
        text { font-size: 22rpx; font-weight: 700; }
      }
      .status-black { background: rgba(26,26,26,0.05); .dot { background: #1a1a1a; } text { color: #1a1a1a; } }
      .status-orange { background: rgba(249,174,61,0.1); .dot { background: #f9ae3d; } text { color: #f9ae3d; } }
      .status-red { background: rgba(250,53,52,0.1); .dot { background: #fa3534; box-shadow: 0 0 8rpx rgba(250,53,52,0.4); } text { color: #fa3534; } }

      .type-tag { background: rgba(0,0,0,0.04); padding: 8rpx 20rpx; border-radius: 12rpx; text { font-size: 22rpx; color: #1a1a1a; font-weight: 700; } }
    }

    .hero-title { font-size: 48rpx; font-weight: 900; color: #1a1a1a; line-height: 1.3; letter-spacing: -1px; margin-bottom: 20rpx; }
    .hero-date { font-size: 28rpx; color: #909399; font-weight: 600; .num-font { font-family: monospace; font-size: 32rpx; color: #1a1a1a; margin-left: 10rpx; } }
  }

  /* ================= 标题公用类 ================= */
  .section-title { display: flex; align-items: center; margin-bottom: 30rpx; padding-left: 10rpx; .title { font-size: 24rpx; font-weight: 800; color: #909399; text-transform: uppercase; letter-spacing: 2px; } }

  /* ================= 2. 核心信息网格 ================= */
  .info-grid {
    border-radius: 36rpx; padding: 40rpx; display: flex; flex-wrap: wrap; gap: 40rpx 0; margin-bottom: 40rpx;
    
    .grid-item { width: 50%; display: flex; flex-direction: column; .g-label { font-size: 22rpx; color: #909399; font-weight: 600; margin-bottom: 8rpx; } .g-value { font-size: 28rpx; color: #1a1a1a; font-weight: 800; padding-right: 20rpx; }
      .num-font { font-family: monospace; font-size: 30rpx; }
      .highlight { color: #1a1a1a; background: rgba(0,0,0,0.04); padding: 4rpx 16rpx; border-radius: 10rpx; align-self: flex-start; }
    }
    
    .full-width { width: 100%; } .border-top { border-top: 1px dashed rgba(0,0,0,0.06); padding-top: 30rpx; }
  }

  /* ================= 3. 备注块 ================= */
  .remark-card {
    border-radius: 32rpx; padding: 40rpx; margin-bottom: 60rpx;
    .remark-text { font-size: 28rpx; color: #606266; line-height: 1.6; font-weight: 500; }
  }

  /* 底部溯源标记 */
  .traceability-mark { display: flex; flex-direction: column; align-items: center; opacity: 0.6; text { font-size: 20rpx; color: #909399; font-family: monospace; letter-spacing: 1px; margin-bottom: 6rpx; } }
}
</style>