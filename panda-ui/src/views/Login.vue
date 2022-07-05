<template>
  <div class="login_container">
    <div class="login_box">
      <div class="title_box">
        <p>熊猫影院管理登录</p>
      </div>
      <!-- 登录表单区域 -->
<!--   loginForm双向绑定   -->
      <el-form class="login_form" :model="loginForm" :rules="loginFormRules" ref="loginFormRef">
<!--        在vue中ref可以以属性的形式添加给标签或者组件-->
        <!-- 用户名 -->
        <el-form-item prop="userName">
          <el-input v-model="loginForm.userName" placeholder="请输入用户名" clearable
                    prefix-icon="iconfont icon-user"></el-input>
        </el-form-item>
        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password
                    prefix-icon="iconfont icon-lock"></el-input>
        </el-form-item>
        <!-- 按扭区域 -->
        <el-form-item class="btns">
          <el-button size="medium" :round="true" type="primary" @click="login">点击登录</el-button>
          <el-button size="medium" :round="true" type="info" @click="resetLoginForm">恢复默认</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      //登录表单数据对象
      //设立初始值，方便调试
      loginForm: {
        userName: 'admin',
        password: '123456'
      },
      //表单验证规则
      loginFormRules: {
        //验证用户名
        userName: [
          { required: true, message: "请输入用户名称", trigger: "blur"},
          //trigger: ‘blur’:失去焦点时触发
          { min:2, max: 20, message: "长度在2到20个字符之间", trigger: "blur"}
        ],
        //验证密码
        password: [
          { required: true, message: "请输入密码", trigger: "blur"},
          { min:6, max: 16, message: "长度在6到16个字符之间", trigger: "blur"}
        ]
      }
    }
  },
  methods:{
    success(params) {
      console.log(params);
      this.login()
    },
    //点击重置按钮，重置表单
    resetLoginForm(){
      console.log(this.$refs)
      //只是将查询条件初始化，所以在初始化时绑定什么值就还是什么值。用于清空表单
      this.$refs.loginFormRef.resetFields();
    },
    login() {
      this.$refs.loginFormRef.validate(async valid => {
        //用于表单的验证
        if(!valid) return;
        axios.defaults.headers.post['Content-Type'] = 'application/json'
        //JSON.stringify() 方法用于将 JavaScript 值转换为 JSON 字符串。
        const { data: res} = await axios.post('sysUser/login', JSON.stringify(this.loginForm));
        //登录失败
        if(res.code !== 200) return this.$message.error(res.msg);
        //控制登录权限
        if(res.data.sysUser.sysRole.children === null || res.data.sysUser.sysRole.children[0] === null) {
          this.$message.error("抱歉，您没有权限登录，请联系管理员获取权限")
          return
        }
        this.$message.success("登录成功")
        // console.log(res.data);
        //保存token，放入sessionStorage,浏览器关闭就会取消
        window.sessionStorage.setItem("token", res.data.token)
        window.sessionStorage.setItem("loginUser", JSON.stringify({sysUser : res.data.sysUser, cinemaId : res.data.cinemaId, cinemaName : res.data.cinemaName}));
        // window.sessionStorage.setItem("btnPermission", res.data.sysUser.sysRole.roleId === 1 ? "admin" : "normal")
        window.sessionStorage.setItem("btnPermission", res.data.sysUser.sysRole.roleId === 1 ? "admin" : "admin")
        //导航跳转到首页
        await this.$router.push('/welcome');
      })
    }
  }
}
</script>

<style scoped>
.login_container{
  /*背景图片*/
  background-image: url("../assets/login-background1.jpg");
  height: 100%;
}

.login_box{
  width: 450px;
  height: 300px;
  background-color: #fff;
  border-radius: 3px;
  position: absolute;
  left: 50%;
  top: 50%;
  /*一般用于居中，这个其实就是一个位移的属性，translatex在x轴方向上进行移动，反之translatey实在y轴方向，而translate括号里的两个参数是先x后y的。*/
  transform: translate(-50%, -50%);
}

.avatar_box{
  height: 130px;
  width: 130px;
  border: 1px solid #eee;
  border-radius: 50%;
  padding: 10px;
  box-shadow: 0 0 10px #ddd;
  position: absolute;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: #fff;
}

.avatar_box > img{
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background-color: #eee;
}

.title_box{
  text-align: center;
  font-size: 200%;
}

.login_form{
  position: absolute;
  bottom: 0;
  width: 100%;
  padding: 0 20px;
  box-sizing: border-box;
}

.btns{
  display: flex;
  justify-content: center;
}
</style>
