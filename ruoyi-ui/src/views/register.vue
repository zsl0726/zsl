<template>
  <div class="register">
    <el-form ref="registerForm" :model="registerForm" :rules="registerRules" class="register-form">
      <h3 class="title">{{title}}</h3>
      <p class="subtitle">{{ registerSubtitle }}</p>
      
      <el-form-item prop="username">
        <el-input v-model="registerForm.username" type="text" auto-complete="off" placeholder="账号(唯一且不能修改)">
          <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      
      <!-- 新增：手机号码输入框 -->
      <el-form-item prop="phonenumber">
        <el-input 
          v-model="registerForm.phonenumber" 
          type="text" 
          auto-complete="off" 
          placeholder="手机号码"
          maxlength="11"
        >
          <svg-icon slot="prefix" icon-class="phone" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      
      <el-form-item prop="password">
        <el-input
          v-model="registerForm.password"
          type="password"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter.native="handleRegister"
        >
          <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input
          v-model="registerForm.confirmPassword"
          type="password"
          auto-complete="off"
          placeholder="确认密码"
          @keyup.enter.native="handleRegister"
        >
          <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>
      
      <!-- 移除手动角色选择框 -->
      <el-form-item prop="code" v-if="captchaEnabled">
        <el-input
          v-model="registerForm.code"
          auto-complete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter.native="handleRegister"
          @blur="registerForm.code = registerForm.code.trim()"
        >
          <svg-icon slot="prefix" icon-class="validCode" class="el-input__icon input-icon" />
        </el-input>
        <div class="register-code">
          <img :src="codeUrl" @click="getCode" class="register-code-img" alt="验证码"/>
        </div>
      </el-form-item>
      
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="medium"
          type="primary"
          style="width:100%;"
          @click.native.prevent="handleRegister"
        >
          <span v-if="!loading">{{ registerButtonText }}</span>
          <span v-else>注 册 中...</span>
        </el-button>
        <div style="float: right; margin-top: 10px;">
          <router-link class="link-type" :to="'/login'">使用已有账户登录</router-link>
        </div>
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-register-footer">
      <span>Copyright © 2018-2025 ruoyi.vip All Rights Reserved.</span>
    </div>
  </div>
</template>

<script>
import { getCodeImg, register, register3 } from "@/api/login"

export default {
  name: "Register",
  data() {
    // 手机号验证规则
    const validatePhone = (rule, value, callback) => {
      if (!value) {
        // 如果手机号不是必填，可以改为 callback()
        callback(new Error("请输入手机号码"));
        return;
      }
      
      // 手机号正则验证（11位数字）
      const phoneRegex = /^1[3-9]\d{9}$/;
      if (!phoneRegex.test(value)) {
        callback(new Error("请输入正确的手机号码"));
      } else {
        callback();
      }
    };

    const equalToPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error("请再次输入您的密码"));
        return;
      }
      if (this.registerForm.password !== value) {
        callback(new Error("两次输入的密码不一致"));
      } else {
        callback();
      }
    };

    return {
      title: process.env.VUE_APP_TITLE || "AI心理健康辅助平台",
      codeUrl: "",
      userType: 'user',
      registerForm: {
        username: "",
        phonenumber: "", // 新增：手机号码字段
        password: "",
        confirmPassword: "",
        code: "",
        uuid: ""
      },
      registerRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" },
          { min: 2, max: 20, message: '用户账号长度必须介于 2 和 20 之间', trigger: 'blur' }
        ],
        phonenumber: [
          { required: true, trigger: "blur", validator: validatePhone } // 新增：手机号验证
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" },
          { min: 5, max: 20, message: "用户密码长度必须介于 5 和 20 之间", trigger: "blur" },
          { pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符：< > \" ' \\\ |", trigger: "blur" }
        ],
        confirmPassword: [
          { required: true, validator: equalToPassword, trigger: "blur" }
        ],
      },
      loading: false,
      captchaEnabled: true
    };
  },
  computed: {
    registerSubtitle() {
      return this.userType === 'user' 
        ? '用户注册 - AI心理健康辅助平台' 
        : '咨询师注册 - AI心理健康辅助平台';
    },
    registerButtonText() {
      return this.userType === 'user' ? '用户注册' : '咨询师注册';
    }
  },
  watch: {
    $route: {
      handler(route) {
        if (route.query && route.query.userType) {
          this.userType = route.query.userType;
        }
      },
      immediate: true
    }
  },
  created() {
    if (this.$route.query && this.$route.query.userType) {
      this.userType = this.$route.query.userType;
    }
    this.getCode().catch(err => {
      console.error("获取验证码失败：", err);
      this.captchaEnabled = false;
    });
  },
  methods: {
    getCode() {
      return getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled ?? true;
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img;
          this.registerForm.uuid = res.uuid || "";
        }
      });
    },
    handleRegister() {
  this.$refs.registerForm.validate(valid => {
    if (valid) {
      this.loading = true;
      
      // 调试：打印注册数据
      console.log("=== 前端注册数据 ===");
      console.log("用户名:", this.registerForm.username);
      console.log("手机号:", this.registerForm.phonenumber);
      console.log("密码:", this.registerForm.password);
      console.log("验证码:", this.registerForm.code);
      console.log("uuid:", this.registerForm.uuid);
      console.log("用户类型:", this.userType);
      
      const registerApi = this.userType === 'user' ? register : register3;
      if (typeof registerApi !== 'function') {
        this.$message.error("注册接口初始化失败，请刷新页面重试");
        this.loading = false;
        return;
      }

      // 构建注册数据对象（与后端 RegisterBody 类字段一致）
      const registerData = {
        username: this.registerForm.username.trim(),
        password: this.registerForm.password,
        phonenumber: this.registerForm.phonenumber ? this.registerForm.phonenumber.trim() : null,
        code: this.registerForm.code ? this.registerForm.code.trim() : '',
        uuid: this.registerForm.uuid || ''
      };

      console.log("=== 发送到后端的注册数据 ===", JSON.stringify(registerData));

      registerApi(registerData).then(res => {
        console.log("注册成功响应:", res);
        const username = this.registerForm.username;
        const roleText = this.userType === 'user' ? '用户' : '咨询师';
        this.$alert(
          `<font color='green'>恭喜你，您的${roleText}账号 ${username} 注册成功！</font>`, 
          '注册成功', 
          {
            dangerouslyUseHTMLString: true,
            type: 'success'
          }
        ).then(() => {
          this.$router.push({ 
            path: "/login", 
            query: { userType: this.userType } 
          });
        });
      }).catch(err => {
        console.error("注册失败详情：", err);
        console.error("错误响应:", err.response);
        this.loading = false;
        if (this.captchaEnabled) {
          this.getCode();
        }
        const errorMsg = err.response?.data?.msg || err.message || "注册失败，请稍后重试";
        this.$message.error(errorMsg);
      });
    }
  });
}
  }
};
</script>

<style scoped>
.register {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh; /* 优化：使用vh确保全屏 */
  background-image: url("../assets/images/login-background.jpg");
  background-size: cover;
  background-position: center;
  margin: 0;
  padding: 0;
}

.title {
  margin: 0px auto 10px auto;
  text-align: center;
  color: #707070;
  font-size: 24px;
  font-weight: bold;
}

.subtitle {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.register-form {
  border-radius: 8px; /* 优化：圆角更美观 */
  background: #ffffff;
  width: 400px;
  padding: 30px 25px 15px 25px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1); /* 优化：增加阴影 */
}

.register-form .el-input {
  height: 38px;
}

.register-form .el-input input {
  height: 38px;
}

.input-icon {
  height: 39px;
  width: 14px;
  margin-left: 2px;
}

.register-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}

.register-code {
  width: 33%;
  height: 38px;
  float: right;
  display: flex;
  justify-content: center;
  align-items: center;
}

.register-code-img {
  height: 38px;
  cursor: pointer;
  border-radius: 4px; /* 优化：验证码图片圆角 */
}

.el-register-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
  background: rgba(0, 0, 0, 0.1); /* 优化：底部文字背景 */
}

.link-type {
  color: #409EFF;
  text-decoration: none;
  font-size: 13px;
}

.link-type:hover {
  text-decoration: underline;
}
</style>