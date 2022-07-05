<template>
    <div class="">

<!--      <a href="https://www.sdsrty.com/index.php/vod/play/id/260964/sid/1/nid/1.html">电影链接+{{this.movieId}}</a>-->

      <video class="el-icon-video-camera"   controls="controls" autoplay="" name="media">
<!--        <source src="../../assets/movie/B.mp4" type="video/mp4">-->
        <source  :src="this.publicPath+this.movieId+'.mp4'" type="video/mp4">
<!--        <source  :src="getImgUrl(item)" type="video/mp4">-->
      </video>

    </div>
</template>

<script>
  export default {
    data() {
      return{
        movieId: this.$route.params.movieId,

        publicPath: process.env.BASE_URL
      }

    },
    created(){
      this.init()
    },
    methods: {
      init(){

      },
      // //获取接口代码块没写 如果有接口 还需调用接口
      // getImgUrl(item){
      //   //  this.GLOBAL.IMGS 是公共的图片路径
      //   //  item.avatar 接口获取到的路径数据
      //   // var url = this.GLOBAL.IMGS + item.avatar;
      //   // var url ="http://localhost:8081/media/testHuawei.c4367207.mp4"
      //   // var url ="http://localhost:8081/media/B.f6c00ff6.mp4"
      //   // http://localhost:8081/media/testHuawei2.c4367207.mp4
      //   var url = "http://localhost:8181/images/movieTV/testHuawei"+".mp4"
      //   return url;
      // }
    }
  }



</script>

<style scoped>
.el-icon-video-camera{
  width:100%;
  height:100%;
  object-fit: fill
}


</style>
