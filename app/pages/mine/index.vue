<template>
  <view class="mine-page">
    <view class="blob blob-1"></view>
    <view class="blob blob-2"></view>

    <view class="main-container">
      
      <view class="profile-card glass-panel">
        <view class="user-core">
          <view class="avatar-wrap">
            <image :src="userAvatar" mode="aspectFill"></image>
          </view>
          <view class="user-text">
            <text class="name">{{ displayName }}</text>
            <text class="role">{{ roleName }}</text>
          </view>
        </view>
      </view>

      <view class="menu-section">
        
        <text class="section-title">Preferences</text>
        <view class="menu-group glass-panel">
          <view class="menu-item" @click="handleToEditInfo">
            <view class="item-left">
              <view class="icon-box bg-gray"><uni-icons type="person" color="#1a1a1a" size="20"></uni-icons></view>
              <text class="item-text">个人资料</text>
            </view>
            <uni-icons type="right" color="#cccccc" size="16"></uni-icons>
          </view>
          
          <view class="menu-item" @click="handleToEditPwd">
            <view class="item-left">
              <view class="icon-box bg-gray"><uni-icons type="locked" color="#1a1a1a" size="20"></uni-icons></view>
              <text class="item-text">安全与密码</text>
            </view>
            <uni-icons type="right" color="#cccccc" size="16"></uni-icons>
          </view>
        </view>

        <text class="section-title">System</text>
        <view class="menu-group glass-panel">
          <view class="menu-item" @click="handleClearCache">
            <view class="item-left">
              <view class="icon-box bg-gray"><uni-icons type="trash" color="#1a1a1a" size="20"></uni-icons></view>
              <text class="item-text">清理缓存</text>
            </view>
            <text class="item-value">{{ cacheSize }}</text>
          </view>

          <view class="menu-item" @click="handleToAgreements">
            <view class="item-left">
              <view class="icon-box bg-gray"><uni-icons type="info" color="#1a1a1a" size="20"></uni-icons></view>
              <text class="item-text">隐私与协议</text>
            </view>
            <uni-icons type="right" color="#cccccc" size="16"></uni-icons>
          </view>

          <view class="menu-item" @click="handleAbout">
            <view class="item-left">
              <view class="icon-box bg-gray"><uni-icons type="settings" color="#1a1a1a" size="20"></uni-icons></view>
              <text class="item-text">关于系统</text>
            </view>
            <view class="version-tag">
              <text>zhizhiSheep v1.0</text>
            </view>
          </view>
        </view>
      </view>

      <view class="logout-section">
        <button class="dark-btn" @click="handleLogout">
          <text>Sign Out</text>
        </button>
      </view>

    </view>
  </view>
</template>

<script>
// 引入获取个人信息的接口 (请确保路径正确，通常是若依自带的)
import { getUserProfile } from "@/api/system/user"
import config from '@/config'

export default {
  data() {
    return {
      displayName: '加载中...',
      roleName: '系统用户',
      userAvatar: '/static/images/profile.jpg',
      cacheSize: '2.4 MB'
    }
  },
  onLoad() {
    this.fetchUserInfo()
  },
  onShow() {
    // 每次页面展示时重新拉取，确保修改资料后能实时更新
    this.fetchUserInfo()
  },
  methods: {
    // 实时调用接口获取最新用户信息
    async fetchUserInfo() {
      try {
        const res = await getUserProfile()
        const user = res.data
        if (user) {
          // 优先使用 nickName，没有则用 userName
          this.displayName = user.nickName || user.userName || '未知用户'
          
          // 处理头像：如果有则拼接完整路径，没有则保留默认头像
          if (user.avatar && user.avatar !== '') {
            const baseURL = config.baseUrl || process.env.VUE_APP_BASE_API
            this.userAvatar = user.avatar.startsWith('http') ? user.avatar : baseURL + user.avatar
          }

          // 处理角色名称
          if (res.roleGroup) {
            this.roleName = res.roleGroup
          } else if (user.roles && user.roles.length > 0) {
            this.roleName = user.roles.map(r => r.roleName).join(' / ')
          }
        }
      } catch (error) {
        console.error("获取用户信息失败", error)
      }
    },

    handleToEditInfo() {
      this.$tab.navigateTo('/pages/mine/info/index')
    },

    handleToEditPwd() {
      this.$tab.navigateTo('/pages/mine/pwd/index')
    },

    handleToAgreements() {
      this.$modal.msg('协议页面暂未配置')
    },

    handleAbout() {
      this.$modal.showModal({
        title: '关于系统',
        content: '让业务开发更简单、更高效。',
        showCancel: false,
        confirmText: '确定'
      })
    },

    handleClearCache() {
      uni.showLoading({ title: '清理中...' })
      setTimeout(() => {
        uni.hideLoading()
        this.cacheSize = '0.0 MB'
        this.$modal.msgSuccess('缓存已清理')
      }, 800)
    },

    handleLogout() {
      this.$modal.confirm('确定要注销并退出系统吗？').then(() => {
        this.$store.dispatch('LogOut').then(() => {
          this.$tab.reLaunch('/pages/login')
        })
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.mine-page {
  width: 100vw;
  min-height: 100vh;
  background-color: #f8faff;
  position: relative;
  overflow-x: hidden;
  padding-bottom: 80rpx;

  /* 背景光晕 (统一全局视觉) */
  .blob {
    position: absolute;
    width: 600rpx; height: 600rpx;
    background: linear-gradient(135deg, rgba(60, 156, 255, 0.12), rgba(122, 187, 255, 0.05));
    filter: blur(90px); border-radius: 50%; z-index: 0;
    animation: move 20s infinite alternate;
  }
  .blob-1 { top: -100rpx; right: -150rpx; }
  .blob-2 { bottom: 200rpx; left: -150rpx; background: rgba(166, 140, 227, 0.08); width: 400rpx; height: 400rpx; }

  @keyframes move {
    from { transform: translate(0, 0) scale(1); }
    to { transform: translate(40rpx, 80rpx) scale(1.05); }
  }

  .main-container {
    position: relative;
    z-index: 1;
    width: 88%;
    max-width: 680rpx;
    margin: 0 auto;
    padding-top: 120rpx;
  }

  /* 通用毛玻璃基类 */
  .glass-panel {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(25px);
    border: 1px solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 30rpx 60rpx rgba(0, 0, 0, 0.03);
  }

  /* ================= 1. 顶部控制面板 ================= */
  .profile-card {
    border-radius: 48rpx;
    padding: 60rpx 50rpx;
    margin-bottom: 60rpx;

    .user-core {
      display: flex;
      align-items: center;

      .avatar-wrap {
        width: 130rpx; height: 130rpx; border-radius: 36rpx; overflow: hidden;
        background: #f0f2f5; margin-right: 40rpx; border: 4rpx solid #fff;
        box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.06);
        image { width: 100%; height: 100%; display: block; }
      }

      .user-text {
        display: flex; flex-direction: column; flex: 1;
        .name { font-size: 48rpx; font-weight: 900; color: #1a1a1a; letter-spacing: 1px; margin-bottom: 8rpx; }
        .role { font-size: 26rpx; color: #909399; font-weight: 500; }
      }
    }
  }

  /* ================= 2. 菜单矩阵 ================= */
  .menu-section {
    margin-bottom: 80rpx;

    .section-title {
      display: block;
      font-size: 24rpx;
      font-weight: 800;
      color: #909399;
      text-transform: uppercase;
      letter-spacing: 2px;
      margin-bottom: 20rpx;
      padding-left: 20rpx;
    }

    .menu-group {
      border-radius: 40rpx;
      padding: 10rpx 30rpx;
      margin-bottom: 50rpx;

      .menu-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 30rpx 10rpx;
        border-bottom: 1px solid rgba(0,0,0,0.03);
        transition: opacity 0.2s;

        &:active { opacity: 0.6; }
        &:last-child { border-bottom: none; }

        .item-left {
          display: flex;
          align-items: center;
          
          .icon-box {
            width: 56rpx; height: 56rpx; border-radius: 16rpx;
            display: flex; justify-content: center; align-items: center;
            margin-right: 24rpx;
          }
          .bg-gray { background: #f4f4f5; }
          
          .item-text { font-size: 30rpx; font-weight: 700; color: #1a1a1a; }
        }

        .item-value { font-size: 26rpx; color: #909399; font-weight: 600; font-family: monospace; }
        
        .version-tag {
          background: rgba(26,26,26,0.04);
          padding: 6rpx 16rpx;
          border-radius: 12rpx;
          text { font-size: 22rpx; font-weight: 800; color: #1a1a1a; }
        }
      }
    }
  }

  /* ================= 3. 极简退出按钮 ================= */
  .logout-section {
    .dark-btn {
      width: 100%; height: 110rpx; background: #1a1a1a; border-radius: 32rpx;
      display: flex; justify-content: center; align-items: center;
      color: #fff; font-size: 32rpx; font-weight: 800; letter-spacing: 1px;
      border: none; transition: all 0.2s;
      box-shadow: 0 20rpx 40rpx rgba(26, 26, 26, 0.1);
      
      &::after { border: none; }
      &:active { transform: scale(0.97); box-shadow: 0 10rpx 20rpx rgba(26, 26, 26, 0.08); }
    }
  }
}
</style>