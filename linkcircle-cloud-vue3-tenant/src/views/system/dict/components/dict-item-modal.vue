<template>
  <a-drawer :width="800" :open="visible" :body-style="{ paddingBottom: '80px' }" title="字典值" @close="onClose">
    <a-card size="small" :bordered="false">
      <a-row class="smart-table-btn-block">
        <div class="smart-table-operate-block">
          <a-button @click="addOrUpdateValue" type="primary" size="small">
            <template #icon>
              <PlusOutlined />
            </template>
            新建
          </a-button>

          <a-button @click="confirmBatchDelete" type="text" danger size="small" :disabled="selectedRowKeyList.length == 0">
            <template #icon>
              <DeleteOutlined />
            </template>
            批量删除
          </a-button>
        </div>
        <div class="smart-table-setting-block"></div>
      </a-row>

      <a-table
        size="small"
        :dataSource="tableData"
        :columns="columns"
        rowKey="id"
        :pagination="false"
        :row-selection="{ selectedRowKeys: selectedRowKeyList, onChange: onSelectChange }"
        bordered
      >
        <template #bodyCell="{ record, column }">
          <template v-if="column.dataIndex === 'action'">
            <a-button @click="addOrUpdateValue(record)" type="link">编辑</a-button>
          </template>
        </template>
      </a-table>

      <div class="smart-query-table-page">
        <a-pagination
          showSizeChanger
          showQuickJumper
          show-less-items
          :pageSizeOptions="PAGE_SIZE_OPTIONS"
          :defaultPageSize="queryForm.pageSize"
          v-model:current="queryForm.pageNum"
          v-model:pageSize="queryForm.pageSize"
          :total="total"
          @change="ajaxQuery"
          @showSizeChange="ajaxQuery"
          :show-total="(total) => `共${total}条`"
        />
      </div>
    </a-card>
    <DictValueOperateModal ref="operateModal" @reloadList="ajaxQuery" />
  </a-drawer>
</template>
<script setup>
  import { reactive, ref } from 'vue';
  import DictValueOperateModal from './dict-item-operate-modal.vue';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { dictApi } from '/@/api/system/dict-api';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import { Modal } from 'ant-design-vue';
  import { message } from 'ant-design-vue';
import { smartSentry } from '/@/lib/smart-sentry';

  // 是否展示抽屉
  const visible = ref(false);
  const dictId = ref(undefined);

  function showModal(keyId) {
    dictId.value = keyId;
    visible.value = true;
    ajaxQuery();
  }

  function onClose() {
    visible.value = false;
    dictId.value = undefined;
  }

  const columns = reactive([
    // {
    //   title: 'ID',
    //   width: 80,
    //   dataIndex: 'id',
    // },
    {
      title: '编码',
      dataIndex: 'itemValue',
    },
    {
      title: '名称',
      dataIndex: 'itemText',
    },
    {
      title: '排序',
      width: 80,
      dataIndex: 'sort',
    },
    {
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
    },
  ]);

  // ----------------------- 表格 查询 ------------------------

  const queryFormState = {
    dictId: undefined,
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });
  const selectedRowKeyList = ref([]);
  const tableLoading = ref(false);
  const tableData = ref([]);
  const total = ref(0);

  function onSelectChange(selectedRowKeys) {
    selectedRowKeyList.value = selectedRowKeys;
  }

  function resetQuery() {
    Object.assign(queryForm, queryFormState);
    ajaxQuery();
  }
  async function ajaxQuery() {
    try {
      tableLoading.value = true;
      queryForm.dictId = dictId.value;
      let responseModel = await dictApi.itemQuery(queryForm);
      const list = responseModel.data.list;
      total.value = parseInt(responseModel.data.total);
      tableData.value = list;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  // ----------------------- 批量 删除 ------------------------

  function confirmBatchDelete() {
    Modal.confirm({
      title: '提示',
      content: '确定要删除选中值吗?',
      okText: '删除',
      okType: 'danger',
      onOk() {
        batchDelete();
      },
      cancelText: '取消',
      onCancel() {},
    });
  }

  const batchDelete = async () => {
    try {
      SmartLoading.show();
      await dictApi.itemDelete(dictId,selectedRowKeyList.value);
      message.success('删除成功');
      ajaxQuery();
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
    }
  };

  // ----------------------- 弹窗表单操作 ------------------------

  const operateModal = ref();
  function addOrUpdateValue(rowData) {
    operateModal.value.showModal(rowData, dictId.value);
  }

  defineExpose({
    showModal,
  });
</script>
