<template>
  <div>
    <a-form class="smart-query-form">
      <a-row class="smart-query-form-row">
        <a-form-item label="企业编码" class="smart-query-form-item">
          <a-input style="width: 300px" v-model:value="queryForm.code" placeholder="请输入企业编码" />
        </a-form-item>

        <a-form-item class="smart-query-form-item smart-margin-left10">
          <a-button type="primary" @click="onSearch">
            <template #icon>
              <ReloadOutlined />
            </template>
            查询
          </a-button>
          <a-button @click="resetQuery" class="smart-margin-left10">
            <template #icon>
              <SearchOutlined />
            </template>
            重置
          </a-button>

          <a-button @click="toEditOrAdd()" type="primary" class="smart-margin-left20">
            <template #icon>
              <PlusOutlined />
            </template>
            新建
          </a-button>
          <a-button
              @click="confirmBatchDelete"
              type="text"
              danger
              size="small"
              :disabled="selectedRowKeyList.length === 0"
          >
            <template #icon>
              <DeleteOutlined />
            </template>
            批量删除
          </a-button>
        </a-form-item>
      </a-row>
    </a-form>

    <a-card size="small" :bordered="false" :hoverable="true">
      <a-table
          size="small"
          :loading="tableLoading"
          bordered
          :dataSource="tableData"
          :columns="columns"
          rowKey="id"
          :row-selection="{ selectedRowKeys: selectedRowKeyList, onChange: onSelectChange }"
          :pagination="false">
        <template #bodyCell="{ record, column }">
          <template v-if="column.dataIndex === 'action'">
            <div class="smart-table-operate">
              <a-button @click="toEditOrAdd(record)" type="link">编辑</a-button>
            </div>
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
    <CorpFormModal ref="corpFormModal" @reloadList="resetQuery" />
  </div>
</template>
<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { corpApi } from '/@/api/system/corp-api';
  import CorpFormModal from './corp-form-modal.vue';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import {SmartLoading} from "/@/components/framework/smart-loading";
  import {dictApi} from "/@/api/system/dict-api";
  import {message, Modal} from "ant-design-vue";

  const columns = ref([
    {
      title: '企业编码',
      dataIndex: 'id',
      ellipsis: true,
    },
    {
      title: '企业名称',
      dataIndex: 'name',
      ellipsis: true,
    },
    {
      title: '业务',
      dataIndex: 'businessStr',
      ellipsis: true,
    },
    {
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
      width: 60,
    },
  ]);

  // ---------------- 查询数据 -----------------------

  const queryFormState = {
    key: '',
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });
  const selectedRowKeyList = ref([]);
  const tableLoading = ref(false);
  const tableData = ref([]);
  const total = ref(0);

  function resetQuery() {
    Object.assign(queryForm, queryFormState);
    ajaxQuery();
  }

  function onSearch() {
    queryForm.pageNum = 1;
    ajaxQuery();
  }
  function onSelectChange(selectedRowKeys) {
    selectedRowKeyList.value = selectedRowKeys;
  }


  async function ajaxQuery() {
    try {
      tableLoading.value = true;
      let responseModel = await corpApi.queryList(queryForm);
      const list = responseModel.data.list;
      total.value = parseInt(responseModel.data.total);
      tableData.value = list;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  function confirmBatchDelete() {
    Modal.confirm({
      title: '提示',
      content: '确定要删除选中Key吗?',
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
      await corpApi.batchDelete(selectedRowKeyList.value);
      message.success('删除成功');
      ajaxQuery();
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
    }
  };

  // ------------------------- 表单操作 弹窗 ------------------------------

  const corpFormModal = ref();
  function toEditOrAdd(rowData) {
    corpFormModal.value.showModal(rowData);
  }

  onMounted(ajaxQuery);
</script>
