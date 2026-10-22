<template>
  <div class="login-page">
    <div class="blob blob-1"></div>
    <div class="blob blob-2"></div>

    <div class="main-container">
      <div class="brand-section">
        <div class="header">
          <img class="logo" src="@/assets/logo/logo.png" alt="logo" />
          <div class="title-group">
            <h1 class="main-title">培训档案云<br/>管理系统</h1>
            <p class="sub-title">让企业培训与考勤管理<br/>更简单、更高效。</p>
          </div>
        </div>
      </div>

      <div class="login-section">
        <div class="login-card">
          <div class="card-title">登录</div>
          
          <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
            <el-form-item prop="username">
              <div class="glass-input" :class="{ 'active': activeField === 'user' }">
                <i class="el-icon-user"></i>
                <input 
                  v-model="loginForm.username" 
                  type="text"
                  placeholder="账号"
                  @focus="activeField = 'user'" 
                  @blur="activeField = ''" 
                />
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <div class="glass-input" :class="{ 'active': activeField === 'pwd' }">
                <i class="el-icon-lock"></i>
                <input 
                  v-model="loginForm.password" 
                  type="password"
                  placeholder="密码"
                  @focus="activeField = 'pwd'" 
                  @blur="activeField = ''"
                  @keyup.enter="handleLogin"
                />
              </div>
            </el-form-item>

            <el-form-item prop="code" v-if="captchaEnabled">
              <div class="captcha-row">
                <div class="glass-input captcha-input" :class="{ 'active': activeField === 'code' }">
                  <i class="el-icon-key"></i>
                  <input 
                    v-model="loginForm.code" 
                    placeholder="验证码"
                    @focus="activeField = 'code'" 
                    @blur="activeField = ''"
                    @keyup.enter="handleLogin"
                  />
                </div>
                <div class="captcha-img-wrapper" @click="getCode">
                  <img :src="codeUrl" alt="验证码" />
                </div>
              </div>
            </el-form-item>

            <el-form-item class="remember-me-box">
              <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
            </el-form-item>

            <el-button 
              class="submit-btn" 
              :loading="loading" 
              @click.native.prevent="handleLogin"
            >
              <span v-if="!loading">登录</span>
              <span v-else>登录中...</span>
              <i class="el-icon-right" v-if="!loading" style="margin-left: 8px; font-weight: bold;"></i>
            </el-button>
          </el-form>
        </div>
      </div>
    </div>

    <div class="agreement">
      登录即视为同意 
      <span>用户协议</span> 与 
      <span>隐私政策</span>
      <div class="copyright">Copyright © 2026 zhizhiSheep All Rights Reserved.</div>
    </div>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login";
import Cookies from "js-cookie";
import { encrypt, decrypt } from '@/utils/jsencrypt';

export default {
  name: "Login",
  data() {
    return {
      activeField: '',
      codeUrl: "",
      loginForm: {
        username: "",
        password: "",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [{ required: true, trigger: "blur", message: "请输入账号" }],
        password: [{ required: true, trigger: "blur", message: "请输入密码" }],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      captchaEnabled: true,
      redirect: undefined
    };
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect;
      },
      immediate: true
    }
  },
  created() {
    this.getCode();
    this.getCookie();
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled;
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img;
          this.loginForm.uuid = res.uuid;
        }
      });
    },
    getCookie() {
      const username = Cookies.get("username");
      const password = Cookies.get("password");
      const rememberMe = Cookies.get("rememberMe");
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      };
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 });
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 });
            Cookies.set("rememberMe", this.loginForm.rememberMe, { expires: 30 });
          } else {
            Cookies.remove("username");
            Cookies.remove("password");
            Cookies.remove("rememberMe");
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || "/" }).catch(()=>{});
          }).catch(() => {
            this.loading = false;
            if (this.captchaEnabled) {
              this.getCode();
            }
          });
        }
      });
    }
  } // 修复点：这里补上了 methods 对象的闭合大括号
};
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

  /* PC 端分栏布局 */
  .main-container {
    position: relative;
    z-index: 1;
    width: 1000px;
    max-width: 90%;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  /* 左侧品牌区 */
  .brand-section {
    flex: 1;
    padding-right: 50px;
    
    .logo {
      width: 240px;
      height: auto;
      margin-bottom: 30px;
    }
    .main-title {
      font-size: 56px;
      font-weight: 800;
      color: #1a1a1a;
      line-height: 1.1;
      letter-spacing: -2px;
      margin: 0 0 20px 0;
    }
    .sub-title {
      font-size: 20px;
      color: #909399;
      line-height: 1.6;
      margin: 0;
      font-weight: 400;
    }
  }

  /* 右侧卡片区 */
  .login-section {
    width: 460px;
  }

  /* 登录卡片 */
  .login-card {
    background: #ffffff;
    border-radius: 8px;
    padding: 50px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
    border: 1px solid #e4e7ed;

    .card-title {
      font-size: 28px;
      font-weight: 800;
      color: #1a1a1a;
      margin-bottom: 40px;
      letter-spacing: -0.5px;
    }

    /* 消除原生 el-form-item 的 margin */
    ::v-deep .el-form-item {
      margin-bottom: 0;
    }
    ::v-deep .el-form-item__error {
      padding-top: 4px;
      margin-bottom: 10px;
      position: relative;
    }

    /* 极简自定义输入框 */
    .glass-input {
      display: flex;
      align-items: center;
      height: 56px;
      background: #f5f7fa;
      border-radius: 8px;
      padding: 0 16px;
      margin-bottom: 24px;
      transition: all 0.3s;
      border: 1px solid #e4e7ed;

      i {
        font-size: 18px;
        color: #909399;
        transition: color 0.3s;
      }

      input {
        flex: 1;
        margin-left: 12px;
        font-size: 14px;
        color: #333;
        border: none;
        background: transparent;
        outline: none;
        width: 100%;
        height: 100%;
      }
      input::placeholder { color: #c0c4cc; }

      &.active {
        background: #ffffff;
        border-color: #1890ff;
        box-shadow: 0 0 0 2px rgba(24, 128, 255, 0.1);
        i { color: #1890ff; }
      }
    }

    /* 验证码布局 */
    .captcha-row {
      display: flex;
      gap: 15px;
      margin-bottom: 24px;
      .captcha-input { flex: 1; margin-bottom: 0; }
      .captcha-img-wrapper {
        width: 120px;
        height: 38px;
        border-radius: 4px;
        overflow: hidden;
        background: #fff;
        border: 1px solid #e4e7ed;
        cursor: pointer;
        img { width: 100%; height: 100%; object-fit: cover; }
      }
    }

    /* 记住密码 */
    .remember-me-box {
      margin-bottom: 30px;
      ::v-deep .el-checkbox__label {
        color: #909399;
      }
    }

    /* 提交按钮 */
    .submit-btn {
      width: 100%;
      height: 48px;
      background: #1890ff;
      border-radius: 8px;
      color: #fff;
      font-size: 15px;
      font-weight: 500;
      transition: all 0.2s;
      border: none;
      padding: 0;
      display: flex;
      justify-content: center;
      align-items: center;

      &:hover {
        background: #40a9ff;
        box-shadow: 0 4px 12px rgba(24, 128, 255, 0.3);
      }
      &:active {
        background: #096dd9;
        transform: scale(0.98);
      }
    }
  }

  /* 底部协议 */
  .agreement {
    position: absolute;
    bottom: 40px;
    left: 0;
    right: 0;
    text-align: center;
    font-size: 13px;
    color: #c0c4cc;
    span {
      color: #606266;
      margin: 0 4px;
      cursor: pointer;
      transition: color 0.2s;
      &:hover { color: #3c9cff; }
    }
    .copyright {
      margin-top: 8px;
      font-size: 12px;
      font-family: Arial, Helvetica, sans-serif;
    }
  }
}
</style>