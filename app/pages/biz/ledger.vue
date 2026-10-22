<template>
  <view class="ledger-page">
    <view class="blob blob-1"></view>
    <view class="blob blob-2"></view>

    <view class="custom-navbar glass-nav" :style="{ paddingTop: statusBarHeight + 'px', height: navBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" color="#1a1a1a" size="24"></uni-icons>
      </view>
      <view class="nav-center">
        <text class="nav-title">Ledger</text>
      </view>
      <view class="nav-right"></view> </view>

    <view class="main-container" :style="{ paddingTop: (statusBarHeight + navBarHeight + 20) + 'px' }">
      
      <view class="search-section">
        <view class="search-box glass-panel">
          <uni-icons type="search" color="#909399" size="20"></uni-icons>
          <input 
            class="search-input" 
            v-model="queryParams.subject" 
            placeholder="搜索培训主题..." 
            confirm-type="search"
            @confirm="handleSearch"
          />
          <view class="clear-icon" v-if="queryParams.subject" @click="clearSearch">
            <uni-icons type="clear" color="#cccccc" size="20"></uni-icons>
          </view>
        </view>
      </view>

      <view class="summary-header">
        <text class="summary-text">共检索到 <text class="highlight">{{ total }}</text> 笔台账记录</text>
      </view>

      <view class="list-container">
        <view v-if="ledgerList.length === 0 && !loading" class="empty-state">
          <uni-icons type="info" color="#cccccc" size="40"></uni-icons>
          <text>暂无台账数据</text>
        </view>

        <view v-else class="ledger-list">
          <view class="ledger-card glass-panel" v-for="(item, index) in ledgerList" :key="index" @click="handleDetail(item)">
            <view class="card-top">
              <view class="category-tag">
                <view class="dot"></view>
                <text>{{ item.projectCategory || '常规培训' }}</text>
              </view>
              <text class="date-text">{{ formatDate(item.trainDate) }}</text>
            </view>
            
            <view class="card-body">
              <text class="subject-title">{{ item.subject }}</text>
            </view>

            <view class="card-bottom">
              <view class="meta-item">
                <uni-icons type="shop" color="#909399" size="16"></uni-icons>
                <text class="meta-text">{{ item.hostDeptName || '暂无部门' }}</text>
              </view>
              <view class="meta-item highlight-meta">
                <uni-icons type="person-filled" color="#1a1a1a" size="16"></uni-icons>
                <text class="meta-text">实到 <text class="num">{{ item.actualCount || 0 }}</text> 人</text>
              </view>
            </view>
          </view>
        </view>

        <view class="load-status" v-if="ledgerList.length > 0">
          <text v-if="loading">正在获取数据...</text>
          <text v-else-if="!hasMore">—— 已经到底啦 ——</text>
        </view>
      </view>

    </view>
  </view>
</template>

<script>
import { listLedger } from '@/api/biz'

export default {
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,

      queryParams: {
        pageNum: 1,
        pageSize: 10,
        subject: undefined
      },
      ledgerList: [],
      total: 0,
      loading: false,
      hasMore: true
    }
  },
  onLoad() {
    this.initSystemInfo()
    this.fetchLedgerList()
  },
  onPullDownRefresh() {
    this.queryParams.pageNum = 1
    this.hasMore = true
    this.fetchLedgerList().then(() => {
      uni.stopPullDownRefresh()
    })
  },
  onReachBottom() {
    if (this.hasMore && !this.loading) {
      this.queryParams.pageNum++
      this.fetchLedgerList(true)
    }
  },
  methods: {
    // 动态获取状态栏高度，适配刘海屏
    initSystemInfo() {
      const systemInfo = uni.getSystemInfoSync()
      this.statusBarHeight = systemInfo.statusBarHeight || 20
      // #ifdef MP-WEIXIN
      let custom = uni.getMenuButtonBoundingClientRect()
      this.navBarHeight = custom.height + (custom.top - this.statusBarHeight) * 2
      // #endif
    },

    goBack() {
      // 兼容直接分享打开的页面无上一页情况
      const pages = getCurrentPages()
      if (pages.length > 1) {
        uni.navigateBack({ delta: 1 })
      } else {
        this.$tab.switchTab('/pages/index') // 如果无历史记录，默认退回首页
      }
    },

    async fetchLedgerList(isAppend = false) {
      this.loading = true
      try {
        const res = await listLedger(this.queryParams)
        if (isAppend) {
          this.ledgerList = this.ledgerList.concat(res.rows)
        } else {
          this.ledgerList = res.rows
        }
        this.total = res.total
        this.hasMore = this.ledgerList.length < this.total
      } catch (error) {
        console.error('获取台账列表失败:', error)
      } finally {
        this.loading = false
      }
    },

    handleSearch() {
      this.queryParams.pageNum = 1
      this.hasMore = true
      this.ledgerList = []
      this.fetchLedgerList()
    },

    clearSearch() {
      this.queryParams.subject = undefined
      this.handleSearch()
    },

    handleDetail(item) {
       this.$tab.navigateTo(`/pages/biz/ledger/detail?id=${item.ledgerId}`)
      //this.$modal.msg('台账：' + item.subject)
    },

    formatDate(dateStr) {
      if (!dateStr) return '未定时间'
      // 转换为极简的 2026.03.07 格式
      return dateStr.substring(0, 10).replace(/-/g, '.')
    }
  }
}
</script>

<style lang="scss" scoped>
.ledger-page {
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
    margin-bottom: 40rpx;
    .search-box {
      border-radius: 24rpx; padding: 24rpx 30rpx;
      display: flex; align-items: center; transition: all 0.3s;
      /* 搜索框专属微光影 */
      box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.02);
      
      .search-input {
        flex: 1; margin: 0 20rpx; font-size: 28rpx; color: #1a1a1a; background: transparent; outline: none;
      }
      .clear-icon { padding: 4rpx; }
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

  /* ================= 台账列表卡片 ================= */
  .list-container {
    .empty-state {
      display: flex; flex-direction: column; align-items: center; padding: 100rpx 0;
      text { font-size: 26rpx; color: #ccc; margin-top: 24rpx; }
    }

    .ledger-list {
      display: flex; flex-direction: column; gap: 30rpx;
    }

    .ledger-card {
      border-radius: 36rpx; padding: 40rpx; transition: transform 0.2s;
      &:active { transform: scale(0.98); background: rgba(255, 255, 255, 0.9); }

      .card-top {
        display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx;
        
        .category-tag {
          display: flex; align-items: center; background: rgba(26,26,26,0.04);
          padding: 8rpx 20rpx; border-radius: 12rpx;
          .dot { width: 10rpx; height: 10rpx; background: #1a1a1a; border-radius: 50%; margin-right: 12rpx; }
          text { font-size: 22rpx; color: #1a1a1a; font-weight: 700; }
        }
        
        .date-text { font-size: 24rpx; color: #909399; font-family: monospace; font-weight: 600; }
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
        
        .highlight-meta {
          background: rgba(26,26,26,0.03); padding: 8rpx 20rpx; border-radius: 16rpx;
          .meta-text { color: #1a1a1a; }
          .num { font-weight: 900; font-size: 28rpx; font-family: monospace; margin: 0 4rpx; }
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