<template>
  <a-card class="child-dept-container">
    <a-breadcrumb>
      <a-breadcrumb-item v-for="(item, index) in props.breadcrumb" :key="index">
        {{ item }}
      </a-breadcrumb-item>
    </a-breadcrumb>

    <a-list class="department-list" :data-source="props.selectedDepartmentChildren">
      <template #renderItem="{ item }">
        <a-list-item>
          <div class="department-item" @click="selectTree(item.id)">
            {{ item.name }}
            <RightOutlined />
          </div>
        </a-list-item>
      </template>
    </a-list>
  </a-card>
</template>
<script setup>
  import emitter from '../../depart-mitt';

  const props = defineProps({
    breadcrumb: Array,
    selectedDepartmentChildren: Array,
  });

  function selectTree(id) {
    emitter.emit('selectTree', id);
  }
</script>
<style scoped lang="less">
  :deep(.ant-list-item) {
    padding: 6px 0px;
  }
  .child-dept-container {
    .department-list-box {
      margin-top: 20px;
    }
    .department-list {
      height: 170px;
      overflow-y: auto;
    }
    .department-item {
      cursor: pointer;
    }
  }
</style>
