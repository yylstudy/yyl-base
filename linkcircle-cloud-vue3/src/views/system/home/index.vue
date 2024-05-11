<template>
  <!--  顶部用户信息-->
  <a-row>
    <HomeHeader />
  </a-row>
  <!--下方左右布局-->
  <a-row :gutter="[10, 10]">
    <!--左侧-->
    <a-col :span="16">
      <a-row :gutter="[10, 10]">
        <a-col :span="12">
          <Pie />
        </a-col>
      </a-row>
    </a-col>
    <!--右侧-->
    <a-col :span="8">
      <a-row :gutter="[10, 10]">
        <!--快捷入口-->
        <!-- <a-col :span="24">
          <HomeQuickEntry />
        </a-col> -->
      </a-row>
    </a-col>
  </a-row>
</template>
<script setup>
  import { computed } from 'vue';
  import HomeHeader from './home-header.vue';
  import HomeNotice from './home-notice.vue';
  import HomeQuickEntry from './components/quick-entry/home-quick-entry.vue';
  import Gauge from './components/echarts/gauge.vue';
  import Pie from './components/echarts/pie.vue';
  import { Modal } from 'ant-design-vue';

  // 业绩完成百分比
  const saleTargetPercent = computed(() => {
    return 75;
  });

</script>
<style lang="less" scoped>
  @import './index.less';
</style>
