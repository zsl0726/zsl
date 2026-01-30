<template>
  <div class="login">
    <!-- 顶部Logo区域 -->
    <div class="login-header">
      <div class="health-logo">
        <i class="el-icon-sunny"></i>
        <span class="platform-name">AI心理健康辅助平台</span>
      </div>
    </div>

    <!-- 用户类型选择 -->
    <div class="user-type-selector">
      <el-radio-group v-model="userType" @change="handleUserTypeChange">
        <el-radio-button label="user">
          <i class="el-icon-user"></i>
          <span>用户登录</span>
        </el-radio-button>
        <el-radio-button label="psychologist">
          <i class="el-icon-suitcase"></i>
          <span>咨询师登录</span>
        </el-radio-button>
      </el-radio-group>
    </div>

    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">{{ loginTitle }}</h3>
      <p class="subtitle">{{ loginSubtitle }}</p>
      
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          type="text"
          auto-complete="off"
          placeholder="账号/手机号"
          @keyup.enter.native="handleLogin"
        >
          <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          :type="showPassword ? 'text' : 'password'"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter.native="handleLogin"
        >
          <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
          <i 
            slot="suffix"
            :class="showPassword ? 'el-icon-view' : 'el-icon-view'" 
            class="password-eye"
            @click="showPassword = !showPassword"
          />
        </el-input>
      </el-form-item>
      
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input
          v-model="loginForm.code"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter.native="handleLogin"
        >
          <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" @click="getCode" class="login-code-img"/>
        </div>
      </el-form-item>
      
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
      
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="medium"
          :type="userType === 'user' ? 'primary' : 'success'"
          style="width:100%;"
          @click.native.prevent="handleLogin"
        >
          <span v-if="!loading">{{ loginButtonText }}</span>
          <span v-else>登 录 中...</span>
        </el-button>
        
        <div class="login-links">
          <!-- 统一使用若依自带的注册和忘记密码功能 -->
          <router-link class="link-type" :to="{ path: '/register', query: { userType: userType } }">立即注册</router-link>
        </div>
      </el-form-item>
    </el-form>
    
    <!-- 底部 -->
    <div class="el-login-footer">
      <span>© 2024 AI心理健康辅助平台 · 关注心理健康，呵护心灵成长</span>
      <span>服务热线：400-123-4567 | 技术支持：support@mentalhealth.com</span>
    </div>
  </div>
</template>

<script>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from '@/utils/jsencrypt'

export default {
  name: "Login",
  data() {
    return {
      title: process.env.VUE_APP_TITLE,
      codeUrl: "",
      // 用户类型：user（用户）或 psychologist（咨询师）
      userType: 'user',
      // 登录表单数据
      loginForm: {
        username: "admin",
        password: "admin123",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      // 登录验证规则
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入账号或手机号" }
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" }
        ],
        code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      showPassword: false,
      loading: false,
      // 验证码开关
      captchaEnabled: true,
      redirect: undefined
    }
  },
  computed: {
    // 动态标题
    loginTitle() {
      return this.userType === 'user' ? '用户登录' : '咨询师登录'
    },
    
    // 动态副标题
    loginSubtitle() {
      return this.userType === 'user' 
        ? '欢迎使用AI心理健康辅助平台' 
        : '专业心理咨询师工作台'
    },
    
    // 登录按钮文字
    loginButtonText() {
      return this.userType === 'user' ? '用户登录' : '咨询师登录'
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.getCode()
    this.getCookie()
  },
  methods: {
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img
          this.loginForm.uuid = res.uuid
        }
      })
    },
    
    getCookie() {
      // 根据用户类型获取不同的cookie
      const username = Cookies.get(`${this.userType}_username`)
      const password = Cookies.get(`${this.userType}_password`)
      const rememberMe = Cookies.get(`${this.userType}_rememberMe`)
      
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe),
        code: "",
        uuid: ""
      }
    },
    
    // 切换用户类型
    handleUserTypeChange(type) {
      // 重置表单
      this.$refs.loginForm.resetFields()
      
      // 根据用户类型设置默认值
      const username = Cookies.get(`${type}_username`)
      const password = Cookies.get(`${type}_password`)
      const rememberMe = Cookies.get(`${type}_rememberMe`)
      
      this.loginForm = {
        username: username === undefined ? "" : username,
        password: password === undefined ? "" : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe),
        code: "",
        uuid: ""
      }
      
      // 重新获取验证码
      this.getCode()
    },
    
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          
          // 保存登录信息到cookie（按用户类型分开存储）
          if (this.loginForm.rememberMe) {
            Cookies.set(`${this.userType}_username`, this.loginForm.username, { expires: 30 })
            Cookies.set(`${this.userType}_password`, encrypt(this.loginForm.password), { expires: 30 })
            Cookies.set(`${this.userType}_rememberMe`, this.loginForm.rememberMe, { expires: 30 })
          } else {
            Cookies.remove(`${this.userType}_username`)
            Cookies.remove(`${this.userType}_password`)
            Cookies.remove(`${this.userType}_rememberMe`)
          }
          
          // 统一使用若依的登录逻辑
          this.$store.dispatch("Login", this.loginForm).then(res => {
            // 登录成功后，保存用户类型信息到localStorage和store
            localStorage.setItem('loginUserType', this.userType)
            this.$store.commit('SET_USER_TYPE', this.userType)
            
            // 根据用户类型确定跳转路径
            let redirectPath = this.redirect
            
            // 如果没有重定向路径，则根据用户类型跳转到不同的首页
            if (!redirectPath) {
              if (this.userType === 'user') {
                // 用户跳转到用户首页 (views/index.vue)
                redirectPath = "/index"
              } else if (this.userType === 'psychologist') {
                // 咨询师跳转到咨询师首页
                redirectPath = "/psychologist/index"
              }
            }
            
            // 跳转到对应的页面
            this.$router.push({ path: redirectPath }).catch(() => {})
          }).catch(() => {
            this.loading = false
            if (this.captchaEnabled) {
              this.getCode()
            }
          })
        }
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../assets/images/login-background.jpg");
  background-size: cover;
  background-position: center;
  position: relative;
}

.login-header {
  position: absolute;
  top: 40px;
  left: 40px;
  z-index: 2;
  
  .health-logo {
    display: flex;
    align-items: center;
    color: white;
    font-weight: bold;
    font-size: 18px;
    
    i {
      font-size: 28px;
      margin-right: 10px;
      color: #ffd700;
    }
    
    .platform-name {
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
    }
  }
}

.user-type-selector {
  position: absolute;
  top: 40px;
  right: 40px;
  z-index: 2;
  
  ::v-deep .el-radio-group {
    .el-radio-button {
      .el-radio-button__inner {
        background: rgba(255, 255, 255, 0.1);
        color: white;
        border: 1px solid rgba(255, 255, 255, 0.3);
        
        i {
          margin-right: 5px;
        }
        
        &:hover {
          background: rgba(255, 255, 255, 0.2);
        }
      }
      
      &.is-active {
        .el-radio-button__inner {
          background: rgba(255, 255, 255, 0.3);
          border-color: white;
          box-shadow: 0 2px 8px rgba(255, 255, 255, 0.3);
        }
      }
    }
  }
}

.title {
  margin: 0px auto 10px auto;
  text-align: center;
  color: #303133;
  font-size: 24px;
  font-weight: bold;
}

.subtitle {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.login-form {
  border-radius: 8px;
  background: #ffffff;
  width: 400px;
  padding: 30px 30px 15px 30px;
  z-index: 1;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  
  .el-input {
    height: 42px;
    input {
      height: 42px;
    }
  }
  
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 2px;
  }
}

.login-links {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
  
  .link-type {
    color: #409EFF;
    text-decoration: none;
    font-size: 13px;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

.password-eye {
  cursor: pointer;
  color: #999;
  font-size: 16px;
  line-height: 42px;
  padding: 0 10px;
  
  &:hover {
    color: #409EFF;
  }
}

.login-code {
  width: 33%;
  height: 42px;
  float: right;
  
  img {
    width: 100%;
    height: 100%;
    cursor: pointer;
    border-radius: 4px;
    border: 1px solid #dcdfe6;
  }
}

.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 0.5px;
  z-index: 2;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 0;
}

@media (max-width: 768px) {
  .login-header {
    top: 20px;
    left: 20px;
    
    .health-logo {
      font-size: 16px;
      
      i {
        font-size: 24px;
      }
    }
  }
  
  .user-type-selector {
    top: 20px;
    right: 20px;
  }
  
  .login-form {
    width: 90%;
    padding: 25px 20px 15px 20px;
  }
}
</style>