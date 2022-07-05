<template>
  <div>
    <!--
    <video :src="videoSrc" :poster="videoImg" :autoplay="playStatus" height="421" width="700" :muted="muteStatus">
      your browser does not support the video tag
    </video>
    <button @click="playClick" :class="{hide: isPlay}">点击播放</button> -->
    <!--class="video-js vjs-default-skin vjs-big-play-centered" -->
    <video    :preload="preload"
              :poster="videoImg" :height="height" :width="width" align="center" :controls="controls"  :autoplay="autoplay">
      <source :src="videoSrc" type="video/mp4">
    </video>
  </div>
</template>

<script>
    export default {
        name: ""
    }
</script>

<style scoped>

</style>
