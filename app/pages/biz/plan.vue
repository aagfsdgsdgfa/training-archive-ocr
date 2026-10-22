<template>
  <view class="plan-page">
    <view class="blob blob-1"></view>
    <view class="blob blob-2"></view>

    <view class="custom-navbar glass-nav" :style="{ paddingTop: statusBarHeight + 'px', height: navBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" color="#1a1a1a" size="24"></uni-icons>
      </view>
      <view class="nav-center">
        <text class="nav-title">Plan</text>
      </view>
      <view class="nav-right"></view>
    </view>

    <view class="main-container" :style="{ paddingTop: (statusBarHeight + navBarHeight + 20) + 'px' }">
      
      <view class="search-section">
        <view class="search-box glass-panel">
          <uni-icons type="search" color="#909399" size="20"></uni-icons>
          <input 
            class="search-input" 
            v-model="queryParams.subject" 
            placeholder="搜索培训计划主题..." 
            confirm-type="search"
            @confirm="handleSearch"
          />
          <view class="clear-icon" v-if="queryParams.subject" @click="clearSearch">
            <uni-icons type="clear" color="#cccccc" size="20"></uni-icons>
          </view>
        </view>
      </view>

      <view class="filter-tabs">
        <view 
          class="tab-item" 
          v-for="(tab, index) in tabOptions" 
          :key="index"
          :class="{ 'active': queryParams.status === tab.value }"
          @click="switchTab(tab.value)"
        >
          <text>{{ tab.name }}</text>
        </view>
      </view>

      <view class="summary-header">
        <text class="summary-text">找到 <text class="highlight">{{ total }}</text> 个相关计划</text>
      </view>

      <view class="list-container">
        <view v-if="planList.length === 0 && !loading" class="empty-state">
          <uni-icons type="calendar" color="#cccccc" size="50"></uni-icons>
          <text>暂无相关培训计划</text>
        </view>

        <view v-else class="plan-list">
          <view class="plan-card glass-panel" v-for="(item, index) in planList" :key="index" @click="showDetail(item)">
            <view class="card-top">
              <view class="time-block">
                <uni-icons type="calendar-filled" color="#1a1a1a" size="16" style="margin-right: 8rpx;"></uni-icons>
                <text class="time-text">{{ item.planYear }}.{{ formatMonth(item.planMonth) }}</text>
              </view>
              <view class="status-badge" :class="item.uiState.class">
                <view class="dot"></view>
                <text>{{ item.uiState.text }}</text>
              </view>
            </view>
            
            <view class="card-body">
              <text class="subject-title">{{ item.subject }}</text>
            </view>

            <view class="card-bottom">
              <view class="meta-item">
                <uni-icons type="shop" color="#909399" size="16"></uni-icons>
                <text class="meta-text">{{ item.hostDeptName || '暂无部门' }}</text>
              </view>
              <view class="type-tag">
                <text>{{ item.planType === '1' ? '计划内' : '计划外' }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="load-status" v-if="planList.length > 0">
          <text v-if="loading">正在获取数据...</text>
          <text v-else-if="!hasMore">—— 列表已到底 ——</text>
        </view>
      </view>

    </view>
  </view>
</template>

<script>
import { listPlan } from '@/api/biz'

export default {
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,

      tabOptions: [
        { name: '全 部', value: undefined },
        { name: '待 执 行', value: '0' },
        { name: '已 完 成', value: '1' }
      ],

      queryParams: {
        pageNum: 1,
        pageSize: 10,
        subject: undefined,
        status: undefined
      },
      
      planList: [],
      total: 0,
      loading: false,
      hasMore: true
    }
  },
  onLoad() {
    this.initSystemInfo()
    this.fetchPlanList()
  },
  onPullDownRefresh() {
    this.queryParams.pageNum = 1
    this.hasMore = true
    this.fetchPlanList().then(() => {
      uni.stopPullDownRefresh()
    })
  },
  onReachBottom() {
    if (this.hasMore && !this.loading) {
      this.queryParams.pageNum++
      this.fetchPlanList(true)
    }
  },
  methods: {
	  
	  showDetail(item) {
	        if (!item.planId) return;
	        this.$tab.navigateTo(`/pages/biz/plan/detail?id=${item.planId}`)
	      },
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
        this.$tab.switchTab('/pages/index')
      }
    },

    switchTab(val) {
      if (this.queryParams.status === val) return
      this.queryParams.status = val
      this.handleSearch()
    },

    handleSearch() {
      this.queryParams.pageNum = 1
      this.hasMore = true
      this.planList = []
      this.fetchPlanList()
    },

    clearSearch() {
      this.queryParams.subject = undefined
      this.handleSearch()
    },

    formatMonth(month) {
      if (!month) return '01'
      return String(month).padStart(2, '0')
    },

    async fetchPlanList(isAppend = false) {
      this.loading = true
      try {
        const res = await listPlan(this.queryParams)
        
        // 智能加工：计算逾期状态
        const now = new Date()
        const currentYear = now.getFullYear()
        const currentMonth = now.getMonth() + 1

        const processedRows = res.rows.map(plan => {
          let uiState = { text: '未知', class: 'status-gray' }
          
          if (plan.status === '1') {
            uiState = { text: '已完成', class: 'status-black' }
          } else if (plan.status === '0') {
            // 判断是否逾期
            if (plan.planYear < currentYear || (plan.planYear === currentYear && plan.planMonth < currentMonth)) {
              uiState = { text: '已逾期', class: 'status-red' }
            } else {
              uiState = { text: '待执行', class: 'status-orange' }
            }
          }
          return { ...plan, uiState }
        })

        if (isAppend) {
          this.planList = this.planList.concat(processedRows)
        } else {
          this.planList = processedRows
        }
        
        this.total = res.total
        this.hasMore = this.planList.length < this.total
      } catch (error) {
        console.error('获取计划列表失败:', error)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.plan-page {
  width: 100vw;
  min-height: 100vh;
  background-color: #f8faff;
  position: relative;
  overflow-x: hidden;
  padding-bottom: 60rpx;

  /* 背景光晕 */
  .blob {
    position: absolute;
    width: 600rpx; height: 600rpx;
    background: linear-gradient(135deg, rgba(60, 156, 255, 0.1), rgba(122, 187, 255, 0.05));
    filter: blur(90px); border-radius: 50%; z-index: 0;
    animation: move 20s infinite alternate;
  }
  .blob-1 { top: -50rpx; right: -150rpx; }
  .blob-2 { top: 600rpx; left: -150rpx; background: rgba(166, 140, 227, 0.08); width: 400rpx; height: 400rpx; }

  @keyframes move {
    from { transform: translate(0, 0) scale(1); }
    to { transform: translate(40rpx, 80rpx) scale(1.05); }
  }

  /* ================= 自定义导航栏 ================= */
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

  /* 通用毛玻璃基类 */
  .glass-panel {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(25px);
    border: 1px solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 20rpx 50rpx rgba(0, 0, 0, 0.03);
  }

  .main-container {
    position: relative; z-index: 1; width: 88%; max-width: 680rpx; margin: 0 auto;
  }

  /* ================= 极简搜索栏 ================= */
  .search-section {
    margin-bottom: 30rpx;
    .search-box {
      border-radius: 24rpx; padding: 24rpx 30rpx;
      display: flex; align-items: center; transition: all 0.3s;
      box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.02);
      
      .search-input {
        flex: 1; margin: 0 20rpx; font-size: 28rpx; color: #1a1a1a; background: transparent; outline: none;
      }
      .clear-icon { padding: 4rpx; }
    }
  }

  /* ================= 胶囊分段控制器 ================= */
  .filter-tabs {
    display: flex;
    background: rgba(0, 0, 0, 0.04);
    border-radius: 24rpx;
    padding: 8rpx;
    margin-bottom: 40rpx;

    .tab-item {
      flex: 1;
      text-align: center;
      padding: 16rpx 0;
      border-radius: 18rpx;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      
      text { font-size: 26rpx; color: #909399; font-weight: 600; }

      &.active {
        background: #1a1a1a;
        box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
        text { color: #fff; font-weight: 800; }
      }
    }
  }

  /* ================= 数据摘要 ================= */
  .summary-header {
    margin-bottom: 30rpx; padding-left: 10rpx;
    .summary-text {
      font-size: 24rpx; color: #909399; font-weight: 500;
      .highlight { color: #1a1a1a; font-weight: 800; font-size: 28rpx; margin: 0 6rpx; }
    }
  }

  /* ================= 计划列表卡片 ================= */
  .list-container {
    .empty-state {
      display: flex; flex-direction: column; align-items: center; padding: 100rpx 0;
      text { font-size: 26rpx; color: #ccc; margin-top: 24rpx; }
    }

    .plan-list {
      display: flex; flex-direction: column; gap: 30rpx;
    }

    .plan-card {
      border-radius: 36rpx; padding: 40rpx; transition: transform 0.2s;
      &:active { transform: scale(0.98); background: rgba(255, 255, 255, 0.9); }

      .card-top {
        display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx;
        
        .time-block {
          display: flex; align-items: center;
          .time-text { font-size: 30rpx; color: #1a1a1a; font-family: monospace; font-weight: 900; letter-spacing: -0.5px; }
        }
        
        /* 智能状态指示器 */
        .status-badge {
          display: flex; align-items: center; background: rgba(255,255,255,0.8); padding: 8rpx 20rpx; border-radius: 12rpx; border: 1px solid rgba(0,0,0,0.03);
          .dot { width: 10rpx; height: 10rpx; border-radius: 50%; margin-right: 10rpx; }
          text { font-size: 22rpx; font-weight: 700; }
        }
        .status-black { background: rgba(26,26,26,0.05); .dot { background: #1a1a1a; } text { color: #1a1a1a; } }
        .status-orange { background: rgba(249,174,61,0.1); .dot { background: #f9ae3d; } text { color: #f9ae3d; } }
        .status-red { background: rgba(250,53,52,0.1); .dot { background: #fa3534; box-shadow: 0 0 8rpx rgba(250,53,52,0.4); } text { color: #fa3534; } }
      }

      .card-body {
        margin-bottom: 36rpx;
        .subject-title { display: block; font-size: 32rpx; font-weight: 800; color: #1a1a1a; line-height: 1.4; }
      }

      .card-bottom {
        display: flex; justify-content: space-between; align-items: center;
        border-top: 1px dashed rgba(0,0,0,0.06); padding-top: 24rpx;
        
        .meta-item {
          display: flex; align-items: center;
          .meta-text { font-size: 24rpx; color: #606266; margin-left: 10rpx; font-weight: 500; }
        }
        
        .type-tag {
          background: #f4f4f5; padding: 6rpx 16rpx; border-radius: 10rpx;
          text { font-size: 22rpx; color: #606266; font-weight: 600; }
        }
      }
    }
  }

  /* 底部加载状态 */
  .load-status {
    text-align: center; padding: 40rpx 0;
    text { font-size: 24rpx; color: #ccc; font-weight: 600; letter-spacing: 2px; }
  }
}
</style>