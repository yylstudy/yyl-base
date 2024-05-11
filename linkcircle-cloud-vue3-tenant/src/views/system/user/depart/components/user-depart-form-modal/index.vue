<template>
  <a-modal v-model:open="visible" title="调整部门" :footer="null" destroyOnClose>
    <DepartmentTree ref="departmentTree" :height="400" :showMenu="false" />
    <div class="footer">
      <a-button style="margin-right: 8px" @click="closeModal">取消</a-button>
      <a-button type="primary" @click="handleOk">提交</a-button>
    </div>
  </a-modal>
</template>
<script setup lang="ts">
  import { message } from 'ant-design-vue';
  import _ from 'lodash';
  import { ref } from 'vue';
  import DepartmentTree from '../depart-tree/index.vue';
  import { userApi } from '/@/api/system/user-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { SmartLoading } from '/@/components/framework/smart-loading';

  // ----------------------- 以下是字段定义 emits props ---------------------

  const emit = defineEmits(['refresh']);

  // ----------------------- 显示/隐藏 ------------------------

  const departmentTree = ref();
  const visible = ref(false);
  const userIdList = ref([]);

  //显示
  async function showModal(selectUserId) {
    userIdList.value = selectUserId;
    visible.value = true;
  }

  //隐藏
  function closeModal() {
    visible.value = false;
  }

  // ----------------------- form操作 ---------------------------------
  async function handleOk() {
    SmartLoading.show();
    try {
      if (_.isEmpty(userIdList.value)) {
        message.warning('请选择要调整的员工');
        return;
      }
      if (_.isEmpty(departmentTree.value.selectedKeys)) {
        message.warning('请选择要调整的部门');
        return;
      }
      let departId = departmentTree.value.selectedKeys[0];
      let params = {
        userIdList: userIdList.value,
        departId: departId,
      };
      await userApi.batchUpdateDepartmentEmployee(params);
      message.success('操作成功');
      emit('refresh');
      closeModal();
    } catch (error) {
      smartSentry.captureError(error);
    } finally {
      SmartLoading.hide();
    }
  }

  // ----------------------- 以下是暴露的方法内容 ----------------------------
  defineExpose({
    showModal,
  });
</script>
<style scoped lang="less">
  .footer {
    position: absolute;
    right: 0;
    bottom: 0;
    width: 100%;
    border-top: 1px solid #e9e9e9;
    padding: 10px 16px;
    background: #fff;
    text-align: right;
    z-index: 1;
  }
</style>
