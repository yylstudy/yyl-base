<template>
  <div>
    <a-select
      v-model:value="selectValue"
      :style="`width: ${width}`"
      :placeholder="props.placeholder"
      :allowClear="true"
      :size="size"
      :mode="mode"
      @change="onChange"
      :disabled="disabled"
    >
      <a-select-option v-for="item in dictValueList" :key="item.valueCode" :value="item.valueCode">
        {{ item.valueName }}
      </a-select-option>
    </a-select>
  </div>
</template>

<script setup>
  import { computed, onMounted, ref, watch } from 'vue';
  import { dictApi } from '/src/api/support/dict-api';

  const props = defineProps({
    dictCode: String,
    value: [Array, String],
    mode: {
      type: String,
      default: 'combobox',
    },
    width: {
      type: String,
      default: '200px',
    },
    placeholder: {
      type: String,
      default: '请选择',
    },
    size: {
      type: String,
      default: 'default',
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  });

  // -------------------------- 查询 字典数据 --------------------------

  const dictValueList = ref([]);
  async function queryDict() {
    let res = await dictApi.getItemByDictCode(props.dictCode);
    dictValueList.value = res.data;
  }

  const values = computed(() => {
    if (!props.value) {
      return [];
    }
    if (!Array.isArray(props.value)) {
      console.error('valueList is not array!!!');
      return [];
    }
    let res = [];
    if (props.value && props.value.length > 0) {
      props.value.forEach((element) => {
        res.push(element.valueCode);
      });
      return res;
    }
    return res;
  });

  onMounted(queryDict);

  // -------------------------- 选中 相关、事件 --------------------------

  const selectValue = ref(props.value);
  watch(
    () => props.value,
    (value) => {
      selectValue.value = value;
    }
  );

  const emit = defineEmits(['update:value', 'change']);
  function onChange(value) {
    let selected = [];
    if (!value) {
      emit('update:value', selected);
      emit('change', selected);
      return selected;
    }
    if (Array.isArray(props.value)) {
      let valueList = dictValueList.value.filter((e) => value.includes(e.valueCode));
      valueList = valueList.map((e) => e.valueCode);
      emit('update:value', valueList);
      emit('change', valueList);
    } else {
      let findValue = dictValueList.value.find((e) => e.valueCode == value);
      emit('update:value', findValue.valueCode);
      emit('change', findValue.valueCode);
    }
  }
</script>
