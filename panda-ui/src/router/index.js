import Vue from 'vue'
import VueRouter from 'vue-router'
import { Message }from 'element-ui'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

import Login from '../views/Login';
import Welcome from "../views/Welcome";
import Home from '../views/Home.vue';
import CinemaInfo from "../views/cinema/CinemaInfo";
import MovieInfo from "../views/movie/MovieInfo";
import MovieCategory from "../views/movie/MovieCategory";
import UserInfo from "../views/user/UserInfo";
import RoleInfo from "../views/role/RoleInfo";
import ResourceInfo from "../views/role/ResourceInfo";
import Error404 from "../views/Error404";

import test from "../views/movie/test";

Vue.use(VueRouter)

const routes = [
  {
    //默认重定向到login组件
    path: '/',
    component: Login,
    redirect: '/login'
  },
  {
    path: '/login',
    component: Login
  },
  {
    path: '/home',
    component: Home,
    redirect: '/welcome',
    children: [
      { path: '/test', component: test},
      { path: '/welcome', component: Welcome },

      { path: '/cinema', component: CinemaInfo},
      { path: '/movie', component: MovieInfo},
      { path: '/movieCategory', component: MovieCategory},

      { path: '/user', component: UserInfo},

      { path: '/role', component: RoleInfo},
      { path: '/resource', component: ResourceInfo}
    ]
  },
  {
    path: '/*',
    component: Error404
  }
]

const router = new VueRouter({
  routes,
  mode: 'history',
})

//挂载路由导航守卫
router.beforeEach((to, from, next) =>{
  //to 将要访问的路径
  //from 从哪个页面来
  //next 一个放行函数

  if(to.path === '/login' || to.path === '/404') return next();

  //顶部进度条
  NProgress.start()
  //获取token
  const token = window.sessionStorage.getItem("token")
  if(!token){
    Message.error('抱歉，请先登录')
    return next('/login');
  }
  next();
})

router.afterEach(() => {
  NProgress.done()
})

const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push(location){
  return originalPush.call(this, location).catch(err => err)
}


export default router
