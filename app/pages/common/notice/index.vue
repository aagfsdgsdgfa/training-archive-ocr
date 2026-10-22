<template>
  <view class="notice-page">
    <view class="blob blob-1"></view>
    <view class="blob blob-2"></view>

    <view class="custom-navbar glass-nav" :style="{ paddingTop: statusBarHeight + 'px', height: navBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" color="#1a1a1a" size="24"></uni-icons>
      </view>
      <view class="nav-center">
        <text class="nav-title">Notices</text>
      </view>
      <view class="nav-right"></view>
    </view>

    <view class="main-container" :style="{ paddingTop: (statusBarHeight + navBarHeight + 20) + 'px' }">
      
      <view class="search-section">
        <view class="search-box glass-panel">
          <uni-icons type="search" color="#909399" size="20"></uni-icons>
          <input 
            class="search-input" 
            v-model="queryParams.noticeTitle" 
            placeholder="搜索公告标题..." 
            confirm-type="search"
            @confirm="handleSearch"
          />
          <view class="clear-icon" v-if="queryParams.noticeTitle" @click="clearSearch">
            <uni-icons type="clear" color="#cccccc" size="20"></uni-icons>
          </view>
        </view>
      </view>

      <view class="filter-tabs">
        <view 
          class="tab-item" 
          v-for="(tab, index) in tabOptions" 
          :key="index"
          :class="{ 'active': queryParams.noticeType === tab.value }"
          @click="switchTab(tab.value)"
        >
          <text>{{ tab.name }}</text>
        </view>
      </view>

      <view class="list-container">
        <view v-if="noticeList.length === 0 && !loading" class="empty-state">
          <uni-icons type="sound" color="#cccccc" size="50"></uni-icons>
          <text>暂无相关公告信息</text>
        </view>

        <view v-else class="notice-list">
          <view class="notice-card glass-panel" v-for="(item, index) in noticeList" :key="index" @click="handleDetail(item)">
            <view class="card-top">
              <view class="type-tag" :class="item.noticeType === '1' ? 'tag-notice' : 'tag-announce'">
                <view class="dot"></view>
                <text>{{ item.noticeType === '1' ? '通知' : '公告' }}</text>
              </view>
              <text class="date-text">{{ formatDate(item.createTime) }}</text>
            </view>
            
            <view class="card-body">
              <text class="subject-title">{{ item.noticeTitle }}</text>
            </view>

            <view class="card-bottom">
              <view class="meta-item">
                <uni-icons type="person" color="#909399" size="16"></uni-icons>
                <text class="meta-text">{{ item.createBy || '系统管理员' }}</text>
              </view>
              <view class="action-btn">
                <text>查看详情</text>
                <uni-icons type="right" color="#1a1a1a" size="14"></uni-icons>
              </view>
            </view>
          </view>
        </view>

        <view class="load-status" v-if="noticeList.length > 0">
          <text v-if="loading">拉取信息中...</text>
          <text v-else-if="!hasMore">—— 已经到底啦 ——</text>
        </view>
      </view>

    </view>
  </view>
</template>

<script>
import { listNotice } from '@/api/biz'

export default {
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,

      tabOptions: [
        { name: '全 部', value: undefined },
        { name: '系 统 通 知', value: '1' },
        { name: '公 司 公 告', value: '2' }
      ],

      queryParams: {
        pageNum: 1,
        pageSize: 10,
        noticeType: undefined,
        noticeTitle: undefined // 新增搜索字段
      },
      
      noticeList: [],
      total: 0,
      loading: false,
      hasMore: true
    }
  },
  onLoad() {
    this.initSystemInfo()
    this.fetchNoticeList()
  },
  onPullDownRefresh() {
    this.queryParams.pageNum = 1
    this.hasMore = true
    this.fetchNoticeList().then(() => {
      uni.stopPullDownRefresh()
    })
  },
  onReachBottom() {
    if (this.hasMore && !this.loading) {
      this.queryParams.pageNum++
      this.fetchNoticeList(true)
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
        this.$tab.switchTab('/pages/index')
      }
    },

    switchTab(val) {
      if (this.queryParams.noticeType === val) return
      this.queryParams.noticeType = val
      this.handleSearch()
    },

    // 新增：搜索触发
    handleSearch() {
      this.queryParams.pageNum = 1
      this.hasMore = true
      this.noticeList = []
      this.fetchNoticeList()
    },

    // 新增：清空搜索框
    clearSearch() {
      this.queryParams.noticeTitle = undefined
      this.handleSearch()
    },

    async fetchNoticeList(isAppend = false) {
      this.loading = true
      try {
        const res = await listNotice(this.queryParams)
        if (isAppend) {
          this.noticeList = this.noticeList.concat(res.rows)
        } else {
          this.noticeList = res.rows
        }
        this.total = res.total
        this.hasMore = this.noticeList.length < this.total
      } catch (error) {
        console.error('获取公告列表失败:', error)
      } finally {
        this.loading = false
      }
    },

    handleDetail(item) {
      if (!item.noticeId) return
      this.$tab.navigateTo(`/pages/common/notice/detail?noticeId=${item.noticeId}`)
    },

    formatDate(dateStr) {
      if (!dateStr) return '-'
      // 转为极简格式: 2026.03.09 14:30
      return dateStr.substring(0, 16).replace(/-/g, '.')
    }
  }
}
</script>

<style lang="scss" scoped>
.notice-page {
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
  .blob-2 { top: 500rpx; left: -150rpx; background: rgba(166, 140, 227, 0.08); width: 400rpx; height: 400rpx; }

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
    box-shadow: 0 20rpx 50rpx rgba(0, 0, 0, 0.03);
  }

  /* ================= 极简搜索栏 (新引入) ================= */
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

  /* 胶囊分段控制器 */
  .filter-tabs {
    display: flex; background: rgba(0, 0, 0, 0.04); border-radius: 24rpx; padding: 8rpx; margin-bottom: 40rpx;
    .tab-item {
      flex: 1; text-align: center; padding: 18rpx 0; border-radius: 18rpx; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      text { font-size: 26rpx; color: #909399; font-weight: 600; }
      &.active {
        background: #1a1a1a; box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
        text { color: #fff; font-weight: 800; }
      }
    }
  }

  /* 公告列表卡片 */
  .list-container {
    .empty-state {
      display: flex; flex-direction: column; align-items: center; padding: 100rpx 0;
      text { font-size: 26rpx; color: #ccc; margin-top: 24rpx; }
    }

    .notice-list { display: flex; flex-direction: column; gap: 30rpx; }

    .notice-card {
      border-radius: 36rpx; padding: 40rpx; transition: transform 0.2s;
      &:active { transform: scale(0.98); background: rgba(255, 255, 255, 0.9); }

      .card-top {
        display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx;
        
        .type-tag {
          display: flex; align-items: center; padding: 6rpx 20rpx; border-radius: 12rpx; border: 1px solid rgba(0,0,0,0.03);
          .dot { width: 10rpx; height: 10rpx; border-radius: 50%; margin-right: 10rpx; }
          text { font-size: 22rpx; font-weight: 700; }
        }
        .tag-notice { background: rgba(250,53,52,0.08); .dot { background: #fa3534; } text { color: #fa3534; } }
        .tag-announce { background: rgba(26,26,26,0.05); .dot { background: #1a1a1a; } text { color: #1a1a1a; } }
        
        .date-text { font-size: 24rpx; color: #909399; font-family: monospace; font-weight: 600; }
      }

      .card-body {
        margin-bottom: 30rpx;
        .subject-title { display: block; font-size: 32rpx; font-weight: 800; color: #1a1a1a; line-height: 1.5; word-break: break-all; }
      }

      .card-bottom {
        display: flex; justify-content: space-between; align-items: center;
        border-top: 1px dashed rgba(0,0,0,0.06); padding-top: 24rpx;
        
        .meta-item {
          display: flex; align-items: center;
          .meta-text { font-size: 24rpx; color: #606266; margin-left: 10rpx; font-weight: 500; }
        }
        
        .action-btn {
          display: flex; align-items: center; background: rgba(26,26,26,0.03); padding: 8rpx 20rpx; border-radius: 14rpx;
          text { font-size: 22rpx; font-weight: 700; color: #1a1a1a; margin-right: 4rpx; }
        }
      }
    }
  }

  .load-status {
    text-align: center; padding: 40rpx 0;
    text { font-size: 24rpx; color: #ccc; font-weight: 600; letter-spacing: 2px; }
  }
}
</style>