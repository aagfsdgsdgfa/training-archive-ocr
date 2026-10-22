<template>
  <view class="login-page">
    <view class="main-container">
      <view class="header">
        <image class="logo" :src="globalConfig.appInfo.logo" mode="aspectFit" />
        <view class="title-group">
          <text class="main-title">TrainArchive System</text>
          <text class="sub-title">让培训管理更简单、更高效</text>
        </view>
      </view>

      <view class="login-card">
        <view class="card-title">账号登录</view>
        
        <view class="input-section">
          <view class="glass-input" :class="{ 'active': activeField === 'user' }">
            <u-icon name="account" color="#666" size="36"></u-icon>
            <input 
              v-model="loginForm.username" 
              placeholder="Username" 
              @focus="activeField = 'user'" 
              @blur="activeField = ''" 
            />
          </view>

          <view class="glass-input" :class="{ 'active': activeField === 'pwd' }">
            <u-icon name="lock" color="#666" size="36"></u-icon>
            <input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="Password" 
              @focus="activeField = 'pwd'" 
              @blur="activeField = ''" 
            />
          </view>

          <view class="captcha-row" v-if="captchaEnabled">
            <view class="glass-input captcha-input" :class="{ 'active': activeField === 'code' }">
              <u-icon name="shield-check" color="#666" size="36"></u-icon>
              <input 
                v-model="loginForm.code" 
                placeholder="Code" 
                @focus="activeField = 'code'" 
                @blur="activeField = ''" 
              />
            </view>
            <view class="captcha-img-wrapper" @click="getCode">
              <image :src="codeUrl" mode="aspectFill" />
            </view>
          </view>
        </view>

        <button class="submit-btn" :loading="loading" @tap="handleLogin">
          <text>Sign In</text>
          <u-icon name="arrow-right-line" color="#fff" size="32"></u-icon>
        </button>

        <view class="register-link" v-if="register" @tap="handleUserRegister">
          New user? <text class="highlight">Create Account</text>
        </view>
      </view>

      
    </view>
    <view class="agreement">
      登录即视为同意 
      <text @tap="handleUserAgrement">用户协议</text> 与 
      <text @tap="handlePrivacy">隐私政策</text>
    </view>
  </view>
</template>

<script>
import { getCodeImg } from '@/api/login'
import { getToken } from '@/utils/auth' // 引入获取token的方法

export default {
  data() {
    return {
      activeField: '',
      loading: false, // 控制按钮的加载动画
      codeUrl: "",
      captchaEnabled: true,
      register: false,
      globalConfig: getApp().globalData.config,
      loginForm: {
        username: "", 
        password: "",
        code: "",
        uuid: ""
      }
    }
  },
  created() {
    this.getCode()
  },
  onLoad() {
    //#ifdef H5
    // H5环境下如果已经登录则直接跳转首页
    if (getToken()) {
      this.$tab.reLaunch('/pages/index')
    }
    //#endif
  },
  methods: {
    // 获取图形验证码
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = 'data:image/gif;base64,' + res.img
          this.loginForm.uuid = res.uuid
        }
      })
    },
    // 登录预校验
    async handleLogin() {
      if (this.loginForm.username === "") {
        this.$modal.msgError("请输入账号")
      } else if (this.loginForm.password === "") {
        this.$modal.msgError("请输入密码")
      } else if (this.loginForm.code === "" && this.captchaEnabled) {
        this.$modal.msgError("请输入验证码")
      } else {
        this.loading = true; // 开启按钮动画
        this.$modal.loading("登录中，请耐心等待...")
        this.pwdLogin()
      }
    },
    // 真正的请求登录逻辑
    async pwdLogin() {
      this.$store.dispatch('Login', this.loginForm).then(() => {
        this.$modal.closeLoading()
        this.loginSuccess()
      }).catch(() => {
        this.loading = false; // 失败则关闭按钮动画
        if (this.captchaEnabled) {
          this.getCode()
        }
      })
    },
    // 登录成功后的处理
    loginSuccess(result) {
      // 设置用户信息并跳转
      this.$store.dispatch('GetInfo').then(res => {
        this.$tab.reLaunch('/pages/index')
      })
    },
    // 跳转注册页
    handleUserRegister() { 
      this.$tab.redirectTo(`/pages/register`)
    },
    // 隐私协议
    handlePrivacy() { 
      let site = this.globalConfig.appInfo.agreements[0]
      this.$tab.navigateTo(`/pages/common/webview/index?title=${site.title}&url=${site.url}`)
    },
    // 用户协议
    handleUserAgrement() { 
      let site = this.globalConfig.appInfo.agreements[1]
      this.$tab.navigateTo(`/pages/common/webview/index?title=${site.title}&url=${site.url}`)
    }
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  width: 100vw;
  height: 100vh;
  background-color: #f0f2f5;
  overflow: hidden;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;

  .main-container {
    position: relative;
    z-index: 1;
    width: 85%;
    max-width: 600rpx;
  }

  /* 头部标题 */
  .header {
    margin-bottom: 80rpx;
    .logo {
      width: 100rpx;
      height: 96rpx;
      margin-bottom: 28rpx;
    }
    .title-group {
      .main-title {
        display: block;
        font-size: 56rpx;
        font-weight: 700;
        color: #303133;
        letter-spacing: 0;
      }
      .sub-title {
        font-size: 26rpx;
        color: #909399;
        margin-top: 10rpx;
        display: block;
      }
    }
  }

  /* 登录卡片 */
  .login-card {
    background: #ffffff;
    border-radius: 16rpx;
    padding: 48rpx;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    border: 1px solid #e4e7ed;

    .card-title {
      font-size: 34rpx;
      font-weight: 700;
      color: #303133;
      margin-bottom: 50rpx;
    }

    /* 输入框样式 */
    .glass-input {
      display: flex;
      align-items: center;
      height: 96rpx;
      background: #f5f7fa;
      border-radius: 12rpx;
      padding: 0 30rpx;
      margin-bottom: 30rpx;
      transition: all 0.3s;
      border: 1px solid #e4e7ed;

      input {
        flex: 1;
        margin-left: 20rpx;
        font-size: 28rpx;
        color: #303133;
      }

      &.active {
        background: #ffffff;
        border-color: #1890ff;
        box-shadow: 0 0 0 4rpx rgba(24, 128, 255, 0.1);
        
      }
    }

    /* 验证码布局 */
    .captcha-row {
      display: flex;
      gap: 20rpx;
      .captcha-input { flex: 1; }
      .captcha-img-wrapper {
        width: 180rpx;
        height: 96rpx;
        border-radius: 12rpx;
        overflow: hidden;
        background: #fff;
        image { width: 100%; height: 100%; }
      }
    }

    /* 按钮 */
    .submit-btn {
      width: 100%;
      height: 96rpx;
      background: #1890ff;
      border-radius: 12rpx;
      color: #fff;
      display: flex;
      justify-content: center;
      align-items: center;
      font-size: 30rpx;
      font-weight: 500;
      margin-top: 40rpx;
      transition: all 0.2s;
      border: none;

      &:active {
        background: #096dd9;
        transform: scale(0.98);
      }
      
      text { margin-right: 10rpx; }
    }

    .register-link {
      text-align: center;
      margin-top: 40rpx;
      font-size: 24rpx;
      color: #909399;
      .highlight {
        color: #1890ff;
        margin-left: 10rpx;
        font-weight: 600;
      }
    }
  }

  /* 底部协议 */
  .agreement {
    position: absolute;
    bottom: 60rpx;
    left: 0;
    right: 0;
    text-align: center;
    font-size: 22rpx;
    color: #c0c4cc;
    text {
      color: #606266;
      margin: 0 6rpx;
      text-decoration: underline;
    }
  }
}
</style>