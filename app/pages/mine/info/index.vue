<template>
  <view class="profile-page">
    <view class="blob blob-1"></view>
    <view class="blob blob-2"></view>

    <view class="custom-navbar glass-nav" :style="{ paddingTop: statusBarHeight + 'px', height: navBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" color="#1a1a1a" size="24"></uni-icons>
      </view>
      <view class="nav-center">
        <text class="nav-title">Profile</text>
      </view>
      <view class="nav-right"></view>
    </view>

    <view class="main-container" :style="{ paddingTop: (statusBarHeight + navBarHeight + 20) + 'px' }">
      
      <view class="user-hero">
        <view class="avatar-ring">
          <image class="avatar-img" :src="userAvatar" mode="aspectFill"></image>
        </view>
        <text class="hero-name">{{ user.nickName || '加载中...' }}</text>
        <text class="hero-role">{{ roleGroup || '系统用户' }}</text>
      </view>

      <view class="info-card glass-panel">
        
        <view class="info-row">
          <view class="row-left">
            <view class="icon-wrap"><uni-icons type="person" color="#1a1a1a" size="18"></uni-icons></view>
            <text class="label">昵称</text>
          </view>
          <text class="value">{{ user.nickName || '未设置' }}</text>
        </view>

        <view class="info-row">
          <view class="row-left">
            <view class="icon-wrap"><uni-icons type="phone" color="#1a1a1a" size="18"></uni-icons></view>
            <text class="label">手机号码</text>
          </view>
          <text class="value number-font">{{ user.phonenumber || '未绑定' }}</text>
        </view>

        <view class="info-row">
          <view class="row-left">
            <view class="icon-wrap"><uni-icons type="email" color="#1a1a1a" size="18"></uni-icons></view>
            <text class="label">电子邮箱</text>
          </view>
          <text class="value number-font">{{ user.email || '未绑定' }}</text>
        </view>

        <view class="info-row">
          <view class="row-left">
            <view class="icon-wrap"><uni-icons type="staff" color="#1a1a1a" size="18"></uni-icons></view>
            <text class="label">所属岗位</text>
          </view>
          <text class="value">{{ postGroup || '暂无岗位' }}</text>
        </view>

        <view class="info-row">
          <view class="row-left">
            <view class="icon-wrap"><uni-icons type="vip" color="#1a1a1a" size="18"></uni-icons></view>
            <text class="label">系统角色</text>
          </view>
          <text class="value">{{ roleGroup || '暂无角色' }}</text>
        </view>

        <view class="info-row">
          <view class="row-left">
            <view class="icon-wrap"><uni-icons type="calendar" color="#1a1a1a" size="18"></uni-icons></view>
            <text class="label">加入日期</text>
          </view>
          <text class="value number-font">{{ formatCreateTime(user.createTime) }}</text>
        </view>

      </view>

    </view>
  </view>
</template>

<script>
import { getUserProfile } from "@/api/system/user"

export default {
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,

      user: {},
      roleGroup: "",
      postGroup: "",
      // 若依接口默认不一定直接返头像绝对路径，这里做个兜底
      defaultAvatar: '/static/images/profile.jpg' 
    }
  },
  computed: {
    userAvatar() {
      if (this.user && this.user.avatar && this.user.avatar !== '') {
        // 如果您的系统有配置 baseUrl，可能需要拼接
        return this.user.avatar;
      }
      return this.defaultAvatar;
    }
  },
  onLoad() {
    this.initSystemInfo()
    this.getUser()
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

    getUser() {
      uni.showLoading({ title: '加载中...' })
      getUserProfile().then(response => {
        this.user = response.data || {}
        this.roleGroup = response.roleGroup || ""
        this.postGroup = response.postGroup || ""
        uni.hideLoading()
      }).catch(() => {
        uni.hideLoading()
      })
    },

    // 格式化日期，让 UI 看起来更清爽 (截取年月日)
    formatCreateTime(time) {
      if (!time) return '-'
      return time.substring(0, 10).replace(/-/g, '.')
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-page {
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
  .blob-1 { top: -100rpx; right: -150rpx; }
  .blob-2 { bottom: 200rpx; left: -150rpx; background: rgba(166, 140, 227, 0.08); width: 400rpx; height: 400rpx; }

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
    position: relative;
    z-index: 1;
    width: 88%;
    max-width: 680rpx;
    margin: 0 auto;
  }

  /* 顶部人物聚焦区 */
  .user-hero {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 60rpx;
    padding-top: 40rpx;

    .avatar-ring {
      width: 180rpx; height: 180rpx; border-radius: 50rpx;
      padding: 8rpx; background: rgba(255,255,255,0.5);
      border: 1px solid rgba(255,255,255,0.8);
      box-shadow: 0 20rpx 40rpx rgba(0,0,0,0.06);
      margin-bottom: 30rpx;

      .avatar-img {
        width: 100%; height: 100%; border-radius: 42rpx; background-color: #f0f2f5;
      }
    }

    .hero-name {
      font-size: 48rpx; font-weight: 900; color: #1a1a1a; letter-spacing: 1px; margin-bottom: 10rpx;
    }
    .hero-role {
      font-size: 26rpx; color: #909399; font-weight: 500; background: rgba(0,0,0,0.04);
      padding: 6rpx 24rpx; border-radius: 20rpx;
    }
  }

  /* 通用毛玻璃基类 */
  .glass-panel {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(25px);
    border: 1px solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 30rpx 60rpx rgba(0, 0, 0, 0.03);
  }

  /* 资料信息卡片 */
  .info-card {
    border-radius: 48rpx;
    padding: 20rpx 40rpx;

    .info-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 40rpx 0;
      border-bottom: 1px solid rgba(0,0,0,0.03);

      &:last-child {
        border-bottom: none;
      }

      .row-left {
        display: flex;
        align-items: center;
        
        .icon-wrap {
          width: 56rpx; height: 56rpx; background: #f4f4f5; border-radius: 16rpx;
          display: flex; justify-content: center; align-items: center; margin-right: 24rpx;
        }
        
        .label {
          font-size: 28rpx; color: #606266; font-weight: 600;
        }
      }

      .value {
        font-size: 30rpx; color: #1a1a1a; font-weight: 800; max-width: 50%;
        white-space: nowrap; overflow: hidden; text-overflow: ellipsis; text-align: right;
      }

      /* 针对数字和字母采用等宽字体，提升专业感 */
      .number-font {
        font-family: monospace; letter-spacing: 0.5px; font-size: 32rpx;
      }
    }
  }
}
</style>