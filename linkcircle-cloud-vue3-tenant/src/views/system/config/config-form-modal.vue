<template>
  <a-modal :open="visible" :title="form.id ? '编辑' : '添加'" ok-text="确认" cancel-text="取消" @ok="onSubmit" @cancel="onClose">
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
      <a-form-item label="参数Key" name="key">
        <a-input v-model:value="form.key" placeholder="请输入参数Key" />
      </a-form-item>
      <a-form-item label="参数名称" name="name">
        <a-input v-model:value="form.name" placeholder="请输入参数名称" />
      </a-form-item>
      <a-form-item label="参数值" name="value">
        <a-input v-model:value="form.value" placeholder="请输入参数值" />
      </a-form-item>
      <a-form-item label="备注" name="remark">
        <textarea v-model="form.remark" style="width: 100%; height: 100px; outline: none"></textarea>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script setup>
  import { message } from 'ant-design-vue';
  import { reactive, ref } from 'vue';
  import { configApi } from '/@/api/system/config-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { SmartLoading } from '/@/components/framework/smart-loading';

  // emit
  const emit = defineEmits(['reloadList']);

  //  组件
  const formRef = ref();

  const formDefault = {
    id: undefined,
    key: '',
    name: '',
    value: '',
    remark: '',
  };
  let form = reactive({ ...formDefault });
  const rules = {
    key: [{ required: true, message: '请输入参数key' }],
    name: [{ required: true, message: '请输入参数名称' }],
    value: [{ required: true, message: '请输入参数值' }],
  };
  // 是否展示
  const visible = ref(false);

  function showModal(rowData) {
    Object.assign(form, formDefault);
    if (rowData) {
      Object.assign(form, rowData);
    }
    visible.value = true;
  }

  function onClose() {
    Object.assign(form, formDefault);
    visible.value = false;
  }

  function onSubmit() {
    formRef.value
      .validate()
      .then(async () => {
        SmartLoading.show();
        try {
          if (form.id) {
            await configApi.updateConfig(form);
          } else {
            await configApi.addConfig(form);
          }
          message.success(`${form.id ? '修改' : '添加'}成功`);
          emit('reloadList');
          onClose();
        } catch (error) {
          smartSentry.captureError(error);
        } finally {
          SmartLoading.hide();
        }
      })
      .catch((error) => {
        console.log('error', error);
        message.error('参数验证错误，请仔细填写表单数据!');
      });
  }

  defineExpose({
    showModal,
  });
</script>
