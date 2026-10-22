<template>
  <view class="pwd-retrieve-page">
    <view class="blob blob-1"></view>
    <view class="blob blob-2"></view>

    <view class="custom-navbar glass-nav" :style="{ paddingTop: statusBarHeight + 'px', height: navBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" color="#1a1a1a" size="24"></uni-icons>
      </view>
      <view class="nav-center">
        <text class="nav-title">Security</text>
      </view>
      <view class="nav-right"></view>
    </view>

    <view class="main-container" :style="{ paddingTop: (statusBarHeight + navBarHeight + 20) + 'px' }">
      
      <view class="page-header">
        <text class="main-title">修改密码</text>
        <text class="sub-title">请定期更改密码，以确保您的系统账号安全</text>
      </view>

      <view class="form-card glass-panel">
        <uni-forms ref="form" :value="user" :rules="rules" label-position="top">
          
          <uni-forms-item name="oldPassword" label="当前密码">
            <uni-easyinput 
              type="password" 
              v-model="user.oldPassword" 
              placeholder="请输入当前使用的旧密码" 
              :placeholderStyle="placeholderStyle"
            />
          </uni-forms-item>
          
          <uni-forms-item name="newPassword" label="新密码">
            <uni-easyinput 
              type="password" 
              v-model="user.newPassword" 
              placeholder="请输入6-20位的新密码" 
              :placeholderStyle="placeholderStyle"
            />
          </uni-forms-item>
          
          <uni-forms-item name="confirmPassword" label="确认新密码">
            <uni-easyinput 
              type="password" 
              v-model="user.confirmPassword" 
              placeholder="请再次输入新密码" 
              :placeholderStyle="placeholderStyle"
            />
          </uni-forms-item>

        </uni-forms>
      </view>

      <view class="action-section">
        <button class="dark-btn" :loading="isSubmitting" @click="submit">
          <text v-if="!isSubmitting">确 认 修 改</text>
          <text v-else>提 交 中 ...</text>
        </button>
      </view>

    </view>
  </view>
</template>

<script>
import { updateUserPwd } from "@/api/system/user"

export default {
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,
      isSubmitting: false,
      placeholderStyle: "color: #c0c4cc; font-size: 28rpx;",
      
      user: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      rules: {
        oldPassword: {
          rules: [{ required: true, errorMessage: '旧密码不能为空' }]
        },
        newPassword: {
          rules: [
            { required: true, errorMessage: '新密码不能为空' },
            { minLength: 6, maxLength: 20, errorMessage: '长度在 6 到 20 个字符之间' }
          ]
        },
        confirmPassword: {
          rules: [
            { required: true, errorMessage: '确认密码不能为空' }, 
            { 
              validateFunction: (rule, value, data) => data.newPassword === value,
              errorMessage: '两次输入的密码不一致'
            }
          ]
        }
      }
    }
  },
  onLoad() {
    this.initSystemInfo()
  },
  onReady() {
    // 绑定验证规则
    this.$refs.form.setRules(this.rules)
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

    submit() {
      this.$refs.form.validate().then(res => {
        this.isSubmitting = true
        updateUserPwd(this.user.oldPassword, this.user.newPassword).then(response => {
          this.$modal.msgSuccess("密码修改成功")
          // 延迟返回，给用户看成功提示的时间
          setTimeout(() => {
            this.isSubmitting = false
            this.goBack()
          }, 1500)
        }).catch(() => {
          this.isSubmitting = false
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.pwd-retrieve-page {
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
    padding-bottom: 60rpx;
  }

  /* 页面引导文案 */
  .page-header {
    margin-bottom: 50rpx;
    .main-title {
      display: block;
      font-size: 56rpx;
      font-weight: 900;
      color: #1a1a1a;
      letter-spacing: -1px;
    }
    .sub-title {
      font-size: 26rpx;
      color: #909399;
      margin-top: 10rpx;
      display: block;
      line-height: 1.5;
    }
  }

  /* 表单卡片 (毛玻璃基类) */
  .glass-panel {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(25px);
    border: 1px solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 30rpx 60rpx rgba(0, 0, 0, 0.03);
    border-radius: 40rpx;
  }

  .form-card {
    padding: 50rpx 40rpx 20rpx;
    margin-bottom: 60rpx;

    /* ================= 深度重写 uni-forms 和 uni-easyinput 样式 ================= */
    ::v-deep .uni-forms-item {
      margin-bottom: 40rpx;
    }
    ::v-deep .uni-forms-item__label {
      font-size: 28rpx !important;
      color: #1a1a1a !important;
      font-weight: 700 !important;
      margin-bottom: 16rpx !important;
      padding: 0 !important;
    }
    
    ::v-deep .uni-easyinput__content {
      background-color: rgba(248, 250, 255, 0.8) !important;
      border: 1px solid rgba(0, 0, 0, 0.04) !important;
      border-radius: 20rpx !important;
      height: 96rpx;
      transition: all 0.3s;
    }
    ::v-deep .uni-easyinput__content:focus-within {
      background-color: #ffffff !important;
      border-color: #1a1a1a !important;
      box-shadow: 0 8rpx 20rpx rgba(26, 26, 26, 0.05);
    }
    ::v-deep .uni-easyinput__content-input {
      font-size: 30rpx !important;
      color: #1a1a1a !important;
      padding: 0 30rpx !important;
    }
    ::v-deep .uni-error-message {
      bottom: -36rpx;
      font-size: 22rpx;
      color: #fa3534;
    }
  }

  /* 底部黑键按钮 */
  .action-section {
    .dark-btn {
      width: 100%; height: 100rpx; background: #1a1a1a; border-radius: 28rpx;
      display: flex; justify-content: center; align-items: center;
      color: #fff; font-size: 30rpx; font-weight: 800; letter-spacing: 2px;
      border: none; transition: all 0.2s;
      box-shadow: 0 20rpx 40rpx rgba(26, 26, 26, 0.15);
      
      &::after { border: none; }
      &:active { transform: scale(0.97); box-shadow: 0 10rpx 20rpx rgba(26, 26, 26, 0.1); }
    }
  }
}
</style>