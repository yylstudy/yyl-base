<template>
  <a-modal :open="visible" :title="isAdd ? '添加' : '编辑'" ok-text="确认" cancel-text="取消" @ok="onSubmit" @cancel="onClose">
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
      <a-form-item label="企业编码" name="id">
        <a-input v-model:value="form.id" placeholder="请输入企业编码" />
      </a-form-item>
      <a-form-item label="企业名称" name="name">
        <a-input v-model:value="form.name" placeholder="请输入企业名称" />
      </a-form-item>
      <a-form-item label="业务" name="businessList">
        <a-select mode="multiple" v-model:value="form.businessList" optionFilterProp="title" placeholder="请选择业务">
          <a-select-option v-for="item in businessSelectList" :key="item.itemValue" :title="item.itemText">{{ item.itemText }}</a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script setup>
  import { message } from 'ant-design-vue';
  import { reactive, ref } from 'vue';
  import { corpApi } from '/@/api/system/corp-api';
  import { dictApi } from '/@/api/system/dict-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { SmartLoading } from '/@/components/framework/smart-loading';

  // emit
  const emit = defineEmits(['reloadList']);

  //  组件
  const formRef = ref();

  const formDefault = {
    id: undefined,
    name: '',
    businessList: [],
  };
  //业务列表
  const businessSelectList = ref([]);

  let form = reactive({ ...formDefault });
  const rules = {
    id: [{ required: true, message: '请输入企业编码' }],
    name: [{ required: true, message: '请输入企业名称' }],
    businessList: [{ required: true, message: '请选择业务' }],
  };
  // 是否展示
  const visible = ref(false);
  const isAdd = ref(true);
  function showModal(rowData) {
    console.log(rowData)
    if(rowData){
      isAdd.value = false;
    }else{
      isAdd.value = true;
    }
    queryBusinessList();
    Object.assign(form, formDefault);
    if (rowData) {
      Object.assign(form, rowData);
    }
    visible.value = true;
  }

  async function queryBusinessList() {
    let res = await dictApi.getItemByDictCode("business");
    businessSelectList.value = res.data;
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
          if (isAdd.value) {
            form.businessMenus = []
            for (let index = 0; index < form.businessList.length; index++) {
              form.businessMenus.push({business:form.businessList[index]})
            }
            await corpApi.add(form);
          } else {
            await corpApi.update(form);
          }
          message.success(`${isAdd.value ? '添加' : '修改'}成功`);
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
