<template>
  <a-modal :open="visible" :title="form.id ? '编辑' : '添加'" ok-text="确认" cancel-text="取消" @ok="onSubmit" @cancel="onClose">
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }" :wrapper-col="{ span: 12 }">
      <a-form-item label="编码" name="itemValue">
        <a-input v-model:value="form.itemValue" placeholder="请输入编码" />
      </a-form-item>
      <a-form-item label="名称" name="itemText">
        <a-input v-model:value="form.itemText" placeholder="请输入名称" />
      </a-form-item>
      <a-form-item label="排序" name="sort">
        <a-input-number v-model:value="form.sort" :min="0" :max="1000" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script setup>
  import { ref, reactive } from 'vue';
  import { message } from 'ant-design-vue';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import { dictApi } from '/@/api/system/dict-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  // emit
  const emit = defineEmits(['reloadList']);

  //  组件
  const formRef = ref();

  const formDefault = {
    id: undefined,
    dictId: undefined,
    sort: 1,
    itemValue: '',
    itemText: '',
  };
  let form = reactive({ ...formDefault });
  const rules = {
    itemValue: [{ required: true, message: '请输入编码' }],
    itemText: [{ required: true, message: '请输入名称' }],
    sort: [{ required: true, message: '请输入排序' }],
  };
  // 是否展示
  const visible = ref(false);

  function showModal(rowData, dictId) {
    Object.assign(form, formDefault);
    if (rowData) {
      Object.assign(form, rowData);
    }
    form.dictId = dictId;
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
            await dictApi.itemEdit(form);
          } else {
            await dictApi.itemAdd(form);
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
