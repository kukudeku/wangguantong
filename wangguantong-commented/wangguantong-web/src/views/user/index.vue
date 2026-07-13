<template>
  <!-- 用户端入口目前只负责加载真正的自助终端组件，方便以后替换或拆分页面。 -->
  <UserTerminal />
</template>

<script setup>
// 把近千行的用户端界面放在独立组件中，保持路由入口简单。
import UserTerminal from './terminal.vue'
</script>
