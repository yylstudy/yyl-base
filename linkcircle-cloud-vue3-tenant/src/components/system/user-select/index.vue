<template>
  <a-select
    v-model:value="selectValue"
    :style="`width: ${width}`"
    :placeholder="props.placeholder"
    :showSearch="true"
    :allowClear="true"
    :size="size"
    @change="onChange"
  >
    <a-select-option v-for="item in employeeList" :key="item.id" :value="item.id">
      {{ item.username }}
      <template v-if="item.departName"> （{{ item.departName }}） </template>
    </a-select-option>
  </a-select>
</template>

<script setup>
  import { onMounted, ref, watch } from 'vue';
  import { userApi } from '/src/api/system/user-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  // =========== 属性定义 和 事件方法暴露 =============

  const props = defineProps({
    value: [Number, Array],
    placeholder: {
      type: String,
      default: '请选择',
    },
    width: {
      type: String,
      default: '100%',
    },
    size: {
      type: String,
      default: 'default',
    },
    // 角色ID，可为空
    roleId: {
      type: Number,
      default: null,
    },
    // 禁用标识
    disabledFlag: {
      type: Number,
      default: null,
    },
  });

  const emit = defineEmits(['update:value', 'change']);

  // =========== 查询数据 =============

  //员工列表数据
  const employeeList = ref([]);
  async function query() {
    try {
      let params = {};
      if (props.roleId) {
        params = { roleId: props.roleId };
      }
      if (null != props.disabledFlag) {
        params.disabledFlag = props.disabledFlag;
      }
      let resp = await userApi.queryAll(params);
      employeeList.value = resp.data;
    } catch (e) {
      smartSentry.captureError(e);
    }
  }
  onMounted(query);

  // =========== 选择 监听、事件 =============
  
  const selectValue = ref(props.value);
  watch(
    () => props.value,
    (newValue) => {
      selectValue.value = newValue;
    }
  );

  function onChange(value) {
    emit('update:value', value);
    emit('change', value);
  }
</script>
