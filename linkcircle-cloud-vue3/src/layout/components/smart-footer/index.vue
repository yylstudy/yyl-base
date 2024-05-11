<template>
  <div class="version">
    <a target="_blank" href="javascript:void(0)"> @copyright 北京承启通科技有限公司 </a>
  </div>
</template>
<script setup>
  import dayjs from 'dayjs';

  const currentYear = dayjs().year();
</script>

<style lang="less" scoped>
  .version {
    font-size: 14px;
    color: rgba(0, 0, 0, 0.45);
    a {
      color: rgba(0, 0, 0, 0.45);
    }
    a:hover {
      color: @primary-color;
    }
  }
</style>
