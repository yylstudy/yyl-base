<template>
  <div class="user-header">
    <a-page-header :title="welcomeSentence">
    </a-page-header>
  </div>
</template>
<script setup>
  import { computed } from 'vue';
  import { useUserStore } from '/@/store/modules/system/user';
  import uaparser from 'ua-parser-js';
  import { Solar, Lunar } from 'lunar-javascript';
  import _ from 'lodash';
  import heartSentenceArray from './heart-sentence';

  const userStore = useUserStore();

  const departName = computed(() => userStore.departName);

  // 欢迎语
  const welcomeSentence = computed(() => {
    let sentence = '';
    let now = new Date().getHours();
    if (now > 0 && now <= 6) {
      sentence = '午夜好，';
    } else if (now > 6 && now <= 11) {
      sentence = '早上好，';
    } else if (now > 11 && now <= 14) {
      sentence = '中午好，';
    } else if (now > 14 && now <= 18) {
      sentence = '下午好，';
    } else {
      sentence = '晚上好，';
    }
    return sentence + userStore.$state.username;
  });

  //日期、节日、节气
  const dayInfo = computed(() => {
    //阳历
    let solar = Solar.fromDate(new Date());
    let day = solar.toString();
    let week = solar.getWeekInChinese();
    //阴历
    let lunar = Lunar.fromDate(new Date());
    let lunarMonth = lunar.getMonthInChinese();
    let lunarDay = lunar.getDayInChinese();
    //节气
    let jieqi = lunar.getPrevJieQi().getName();
    let next = lunar.getNextJieQi();
    let nextJieqi = next.getName() + ' ' + next.getSolar().toYmd();

    return `${day} 星期${week}，农历${lunarMonth}${lunarDay}（当前${jieqi}，${nextJieqi} ）`;
  });

  // 毒鸡汤
  const heartSentence = computed(() => {
    return heartSentenceArray[_.random(0, heartSentenceArray.length - 1)];
  });
</script>
<style scoped lang="less">
  .user-header {
    width: 100%;
    background-color: #fff;
    margin-bottom: 10px;

    .left-content {
      width: calc(100% - 420px);
      h3 {
        color: rgba(0, 0, 0, 0.75);
      }
    }

    .content {
      display: flex;
      justify-content: space-between;
      .weather {
        width: 400px;
      }
    }

    .last-login-info {
      font-size: 13px;
      color: #333;
      overflow-wrap: break-word;
      padding: 0;
      margin: 1px 0 0 0;
    }

    .sentence {
      display: block;
      font-size: 12px;
      color: #acacac;
      overflow-wrap: break-word;
      padding: 5px 0 0 0;
      margin: 6px 0 0 0;
    }
    .sentence:hover {
      cursor: pointer;
      text-decoration: underline;
    }
  }
</style>
