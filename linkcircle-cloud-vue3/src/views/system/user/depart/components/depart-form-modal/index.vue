<template>
  <a-modal v-model:open="visible" :title="formState.id ? '编辑部门' : '添加部门'" @ok="handleOk" destroyOnClose>
    <a-form
        :label-col="labelCol"
        :wrapper-col="wrapperCol"
        ref="formRef" :model="formState" :rules="rules" layout="horizontal">
      <a-form-item label="上级部门" name="parentId" v-if="formState.parentId != 0">
        <DepartmentTreeSelect ref="departmentTreeSelect" v-model:value="formState.parentId" :defaultValueFlag="false" width="100%" />
      </a-form-item>
      <a-form-item label="部门名称" name="name">
        <a-input v-model:value.trim="formState.name" placeholder="请输入部门名称" />
      </a-form-item>
      <a-form-item label="部门排序" name="sort">
        <a-input-number style="width: 100%" v-model:value="formState.sort" :min="0" placeholder="请输入部门名称" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script setup lang="ts">
  import message from 'ant-design-vue/lib/message';
  import { reactive, ref } from 'vue';
  import { departApi } from '/src/api/system/depart-api';
  import DepartmentTreeSelect from '/@/components/system/depart-tree-select/index.vue';
  import EmployeeSelect from '/@/components/system/user-select/index.vue';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { SmartLoading } from '/@/components/framework/smart-loading';

  // ----------------------- 对外暴漏 ---------------------

  defineExpose({
    showModal,
  });

  // ----------------------- modal 的显示与隐藏 ---------------------
  const emits = defineEmits(['refresh']);

  const visible = ref(false);
  function showModal(data) {
    visible.value = true;
    updateFormData(data);
  }
  function closeModal() {
    visible.value = false;
    resetFormData();
  }

  // ----------------------- form 表单操作 ---------------------
  const formRef = ref();
  const departmentTreeSelect = ref();
  const labelCol = { style: { width: '100px' } };
  const wrapperCol = { span: 14 };
  const defaultDepartmentForm = {
    id: undefined,
    managerId: undefined, //部门负责人
    name: undefined,
    parentId: undefined,
    sort: 0,
  };
  const employeeSelect = ref();

  let formState = reactive({
    ...defaultDepartmentForm,
  });
  // 表单校验规则
  const rules = {
    parentId: [{ required: true, message: '上级部门不能为空' }],
    name: [
      { required: true, message: '部门名称不能为空' },
      { max: 50, message: '部门名称不能大于20个字符', trigger: 'blur' },
    ],
    managerId: [{ required: true, message: '部门负责人不能为空' }],
  };
  // 更新表单数据
  function updateFormData(data) {
    Object.assign(formState, defaultDepartmentForm);
    if (data) {
      Object.assign(formState, data);
    }
    visible.value = true;
  }
  // 重置表单数据
  function resetFormData() {
    Object.assign(formState, defaultDepartmentForm);
  }

  async function handleOk() {
    try {
      await formRef.value.validate();
      if (formState.id) {
        updateDepart();
      } else {
        addDepart();
      }
    } catch (error) {
      message.error('参数验证错误，请仔细填写表单数据!');
    }
  }

  // ----------------------- form 表单  ajax 操作 ---------------------
  //添加部门ajax请求
  async function addDepart() {
    SmartLoading.show();
    try {
      await departApi.addDepart(formState);
      emits('refresh');
      closeModal();
    } catch (error) {
      smartSentry.captureError(error);
    } finally {
      SmartLoading.hide();
    }
  }

  //更新部门ajax请求
  async function updateDepart() {
    SmartLoading.show();
    try {
      if (formState.parentId == formState.id) {
        message.warning('上级菜单不能为自己');
        return;
      }
      await departApi.updateDepart(formState);
      emits('refresh');
      closeModal();
    } catch (error) {
      smartSentry.captureError(error);
    } finally {
      SmartLoading.hide();
    }
  }
</script>
