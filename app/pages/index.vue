<template>
  <view class="home-page">

    <view class="header-section">
      <view class="user-info">
        <text class="greeting">Hi, {{ userName }}</text>
        <text class="date-text">{{ currentDate }}</text>
      </view>
      <view class="avatar-wrap">
        <image :src="userAvatar" class="avatar" mode="aspectFill" />
      </view>
    </view>

    <view class="main-content">
      <view class="glass-card data-board">
        <view class="data-item" @click="$tab.navigateTo('/pages/biz/plan')">
          <text class="num">{{ stats.planCount }}</text>
          <text class="label">年度计划</text>
        </view>
        <view class="divider"></view>
        <view class="data-item" @click="$tab.navigateTo('/pages/biz/batch')">
          <text class="num">{{ stats.batchCount }}</text>
          <text class="label">扫表批次</text>
        </view>
        <view class="divider"></view>
        <view class="data-item" @click="$tab.navigateTo('/pages/biz/ledger')">
          <text class="num">{{ stats.personCount }}</text>
          <text class="label">参训人次</text>
        </view>
      </view>

      <view class="grid-nav">
        <view class="grid-item" @click="$tab.navigateTo('/pages/biz/ocr/camera')">
          <view class="icon-box bg-gray">
            <uni-icons type="camera-filled" color="#1890ff" size="28"></uni-icons>
          </view>
          <text>拍表入库</text>
        </view>
        <view class="grid-item" @click="gotoNotice">
          <view class="icon-box bg-gray">
            <uni-icons type="sound-filled" color="#1890ff" size="28"></uni-icons>
          </view>
          <text>系统公告</text>
        </view>
        <view class="grid-item" @click="$tab.navigateTo('/pages/biz/ledger')">
          <view class="icon-box bg-gray">
            <uni-icons type="wallet" color="#1890ff" size="28"></uni-icons>
          </view>
          <text>台账查询</text>
        </view>
        <view class="grid-item" @click="gotoarchive">
          <view class="icon-box bg-gray">
            <uni-icons type="person-filled" color="#1890ff" size="28"></uni-icons>
          </view>
          <text>个人档案</text>
        </view>
      </view>

      <view class="notice-bar glass-card" v-if="noticeList.length > 0" @click="gotoNotice">
        <uni-icons type="sound" color="#fa3534" size="20" style="margin-right: 16rpx;"></uni-icons>
        <swiper class="notice-swiper" vertical autoplay circular interval="3000">
          <swiper-item v-for="(notice, idx) in noticeList" :key="idx">
            <text class="notice-text">{{ notice.noticeTitle }}</text>
          </swiper-item>
        </swiper>
        <uni-icons type="right" color="#cccccc" size="16"></uni-icons>
      </view>

      <view class="section-title">
        <text class="title">计划执行预警</text>
        <view class="badge" v-if="pendingPlans.length > 0">{{ pendingPlans.length }} 待办</view>
      </view>
      
      <view class="glass-card alert-section">
        <view v-if="pendingPlans.length === 0" class="empty-state">
          <uni-icons type="checkbox-filled" color="#cccccc" size="60"></uni-icons>
          <text>当前无积压或逾期计划</text>
        </view>
        <view v-else class="alert-list">
          <view class="alert-item" v-for="(plan, index) in pendingPlans" :key="index" @click="$tab.navigateTo('/pages/biz/plan')">
            <view class="alert-indicator" :class="{ 'overdue': plan.isOverdue }"></view>
            <view class="alert-content-box">
              <view class="alert-head">
                <text class="subject">{{ plan.subject }}</text>
                <text class="tag" :class="plan.isOverdue ? 'tag-red' : 'tag-blue'">{{ plan.isOverdue ? '已逾期' : '本月待办' }}</text>
              </view>
              <view class="alert-meta">
                <text>{{ plan.planYear }}年{{ plan.planMonth }}月 · {{ plan.hostDeptName || '综合管理部' }}</text>
              </view>
            </view>
            <uni-icons type="right" color="#cccccc" size="18"></uni-icons>
          </view>
        </view>
      </view>

      <view class="section-title" style="margin-top: 40rpx;">
        <text class="title">最新台账入账</text>
      </view>
      <view class="glass-card dynamic-section">
        <view v-if="recentLedgers.length === 0" class="empty-state mini">暂无数据</view>
        <view class="dynamic-item" v-for="(ledger, idx) in recentLedgers" :key="idx" @click="$tab.navigateTo('/pages/biz/ledger')">
          <view class="d-icon bg-gray">
            <uni-icons type="calendar-filled" color="#1890ff" size="24"></uni-icons>
          </view>
          <view class="d-info">
            <text class="d-title">{{ ledger.subject }}</text>
            <text class="d-time">{{ ledger.trainDate }}</text>
          </view>
          <view class="d-val">实到 <text class="highlight">{{ ledger.actualCount }}</text> 人</view>
        </view>
      </view>

      <view class="bottom-spacer"></view>
    </view>
  </view>
</template>

<script>
import { listPlan, listLedger, listBatch,listNotice } from '@/api/biz'
export default {
  data() {
    return {
      currentDate: '',
      userName: '管理员',
      userAvatar: '/static/images/profile.jpg',
      stats: {
        planCount: 0,
        batchCount: 0,
        personCount: 0
      },
      pendingPlans: [],
      recentLedgers: [],
      noticeList: [] // 存放公告数据
    }
  },
  onLoad() {
    this.formatCurrentDate()
    this.initUserInfo()
  },
  onShow() {
    this.fetchAllData()
  },
  onPullDownRefresh() {
    this.fetchAllData().then(() => {
      uni.stopPullDownRefresh()
    })
  },
  methods: {
    gotoarchive(){
      uni.reLaunch({
        url:"/pages/biz/archive"
      })
    },
    
    // 跳转到公告列表页 (若依移动端自带的组件或页面)
    gotoNotice(){
      // 请根据您实际的系统公告页面路径进行修改
      this.$tab.navigateTo('/pages/common/notice/index')
    },

    initUserInfo() {
      if (this.$store && this.$store.state && this.$store.state.user) {
        const user = this.$store.state.user;
        if (user.name) {
          this.userName = user.name;
        }
        if (user.avatar && user.avatar !== '') {
          this.userAvatar = user.avatar;
        }
      }
    },

    formatCurrentDate() {
      const now = new Date()
      const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
      this.currentDate = `${months[now.getMonth()]} ${now.getDate()}, ${now.getFullYear()}`
    },

    async fetchAllData() {
      try {
        // 1. 拉取公告数据 (专门接收定时任务发出的计划报警)
        const noticeRes = await listNotice({ pageNum: 1, pageSize: 5 })
        this.noticeList = noticeRes.rows || []

        // 2. 拉取台账动态
        const ledgerRes = await listLedger({ pageNum: 1, pageSize: 5 })
        this.recentLedgers = ledgerRes.rows
        
        // 3. 统计参训人次
        const allLedgerRes = await listLedger({ pageNum: 1, pageSize: 100 })
        let totalPersons = 0
        allLedgerRes.rows.forEach(item => {
          totalPersons += (item.actualCount || 0)
        })
        this.stats.personCount = totalPersons

        // 4. 统计批次
        const batchRes = await listBatch({ pageNum: 1, pageSize: 1 })
        this.stats.batchCount = batchRes.total

        // 5. 统计与解析计划
        const planRes = await listPlan({ pageNum: 1, pageSize: 100 })
        this.stats.planCount = planRes.total

        const now = new Date()
        const currentYear = now.getFullYear()
        const currentMonth = now.getMonth() + 1

        const pendingAll = planRes.rows.filter(p => p.status === '0')
        this.pendingPlans = pendingAll.map(plan => {
          let isOverdue = false
          if (plan.planYear < currentYear || (plan.planYear === currentYear && plan.planMonth < currentMonth)) {
            isOverdue = true
          }
          return { ...plan, isOverdue }
        }).sort((a, b) => {
          return (b.isOverdue ? 1 : 0) - (a.isOverdue ? 1 : 0)
        }).slice(0, 3)

      } catch (error) {
        console.error("加载首页数据失败", error)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  background-color: #f0f2f5;
  position: relative;

  .header-section {
    position: relative;
    z-index: 1;
    padding: 100rpx 40rpx 40rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .user-info {
      .greeting {
        display: block;
        font-size: 48rpx;
        font-weight: 800;
        color: #1a1a1a;
        margin-bottom: 10rpx;
      }
      .date-text {
        font-size: 26rpx;
        color: #909399;
        font-weight: 500;
      }
    }
    .avatar-wrap {
      width: 100rpx;
      height: 100rpx;
      border-radius: 30rpx;
      overflow: hidden;
      box-shadow: 0 10rpx 20rpx rgba(0,0,0,0.05);
      border: 4rpx solid #fff;
      background-color: #f0f2f5;
      .avatar { width: 100%; height: 100%; }
    }
  }

  .main-content {
    position: relative;
    z-index: 1;
    padding: 0 40rpx;
  }

  /* 通用卡片 */
  .glass-card {
    background: #ffffff;
    border-radius: 16rpx;
    padding: 32rpx;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
    border: 1px solid #e4e7ed;
    margin-bottom: 32rpx;
  }

  /* 顶部数据看板 */
  .data-board {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 32rpx 20rpx;

    .data-item {
      flex: 1;
      text-align: center;
      display: flex;
      flex-direction: column;
      .num { font-size: 44rpx; font-weight: 700; color: #303133; margin-bottom: 8rpx; }
      .label { font-size: 24rpx; color: #909399; }
    }
    .divider {
      width: 1px;
      height: 60rpx;
      background: #e4e7ed;
    }
  }

  /* 金刚区 */
  .grid-nav {
    display: flex;
    justify-content: space-between;
    margin-bottom: 32rpx;

    .grid-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .icon-box {
        width: 110rpx;
        height: 110rpx;
        border-radius: 16rpx;
        display: flex;
        justify-content: center;
        align-items: center;
        margin-bottom: 16rpx;
        background: #f5f7fa;
        transition: transform 0.2s;
        &:active { transform: scale(0.95); }
      }

      text { font-size: 24rpx; color: #606266; font-weight: 500; }
    }
  }

  /* 公告走马灯 */
  .notice-bar {
    display: flex; align-items: center; padding: 24rpx 30rpx; border-radius: 16rpx; margin-bottom: 32rpx;
    background: #ffffff;
    border: 1px solid #e4e7ed;

    .notice-swiper {
      flex: 1; height: 40rpx;
      swiper-item { display: flex; align-items: center; }
      .notice-text { font-size: 26rpx; font-weight: 600; color: #333; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    }
  }

  /* 标题公用类 */
  .section-title {
    display: flex;
    align-items: center;
    margin-bottom: 24rpx;
    .title { font-size: 34rpx; font-weight: 600; color: #303133; }
    .badge { margin-left: 16rpx; background: rgba(24, 128, 255, 0.1); color: #1890ff; font-size: 22rpx; padding: 4rpx 16rpx; border-radius: 20rpx; font-weight: 500; }
  }

  /* 预警列表 */
  .alert-section {
    padding: 30rpx;
    .alert-list {
      display: flex;
      flex-direction: column;
      gap: 20rpx;
    }
    .alert-item {
      display: flex;
      align-items: center;
      padding: 24rpx;
      background: #f5f7fa;
      border-radius: 12rpx;
      border: 1px solid #e4e7ed;

      .alert-indicator {
        width: 12rpx; height: 12rpx; border-radius: 50%; background: #909399; margin-right: 24rpx;
        &.overdue { background: #f56c6c; }
      }
      
      .alert-content-box {
        flex: 1;
        .alert-head {
          display: flex; align-items: center; margin-bottom: 8rpx;
          .subject { font-size: 30rpx; font-weight: 600; color: #333; max-width: 300rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
          .tag { font-size: 20rpx; padding: 2rpx 10rpx; border-radius: 8rpx; margin-left: 12rpx; font-weight: bold; }
          .tag-red { color: #f56c6c; background: rgba(245, 108, 108, 0.1); }
          .tag-blue { color: #1890ff; background: rgba(24, 128, 255, 0.1); }
        }
        .alert-meta { font-size: 24rpx; color: #909399; }
      }
    }
  }

  /* 动态列表 */
  .dynamic-section {
    padding: 10rpx 30rpx;
    .dynamic-item {
      display: flex; align-items: center; padding: 30rpx 0; border-bottom: 1px solid rgba(0,0,0,0.03);
      &:last-child { border-bottom: none; }
      
      .d-icon { width: 80rpx; height: 80rpx; border-radius: 16rpx; display: flex; justify-content: center; align-items: center; margin-right: 24rpx; }
      .bg-gray { background: #f5f7fa; }

      .d-info { flex: 1; display: flex; flex-direction: column; }
      .d-title { font-size: 28rpx; font-weight: 500; color: #303133; margin-bottom: 8rpx; max-width: 350rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
      .d-time { font-size: 22rpx; color: #909399; }
      .d-val { font-size: 26rpx; color: #606266; font-weight: 500; .highlight { color: #1890ff; font-weight: 700; font-size: 32rpx; margin: 0 4rpx; } }
    }
  }

  .empty-state {
    display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60rpx 0;
    text { font-size: 26rpx; color: #909399; margin-top: 20rpx; }
    &.mini { padding: 30rpx 0; font-size: 24rpx; color: #ccc; }
  }

  .bottom-spacer { height: 60rpx; }
}
</style>