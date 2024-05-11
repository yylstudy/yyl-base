<template>
  <div>
    <a-form class="smart-query-form">
      <a-row class="smart-query-form-row">
        <a-form-item label="参数Key" class="smart-query-form-item">
          <a-input style="width: 300px" v-model:value="queryForm.key" placeholder="请输入key" />
        </a-form-item>

        <a-form-item class="smart-query-form-item smart-margin-left10">
          <a-button type="primary" @click="onSearch" v-privilege="'system:config:query'">
            <template #icon>
              <ReloadOutlined />
            </template>
            查询
          </a-button>
          <a-button @click="resetQuery" v-privilege="'system:config:query'" class="smart-margin-left10">
            <template #icon>
              <SearchOutlined />
            </template>
            重置
          </a-button>

          <a-button @click="toEditOrAdd()" v-privilege="'system:config:add'" type="primary" class="smart-margin-left20">
            <template #icon>
              <PlusOutlined />
            </template>
            新建
          </a-button>
          <a-button
              @click="confirmBatchDelete"
              v-privilege="'system:config:delete'"
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
              <a-button @click="toEditOrAdd(record)" v-privilege="'system:config:update'" type="link">编辑</a-button>
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
    <ConfigFormModal ref="configFormModal" @reloadList="resetQuery" />
  </div>
</template>
<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import { configApi } from '/@/api/system/config-api';
  import ConfigFormModal from './config-form-modal.vue';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import {SmartLoading} from "/@/components/framework/smart-loading";
  import {dictApi} from "/@/api/system/dict-api";
  import {message, Modal} from "ant-design-vue";

  const columns = ref([
    {
      title: 'key',
      dataIndex: 'key',
      ellipsis: true,
    },
    {
      title: '参数名称',
      dataIndex: 'name',
      ellipsis: true,
    },
    {
      title: '参数值',
      dataIndex: 'value',
      ellipsis: true,
    },
    {
      title: '备注',
      dataIndex: 'remark',
      ellipsis: true,
      width: 150,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      width: 150,
    },
    {
      title: '修改时间',
      dataIndex: 'updateTime',
      width: 150,
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
      let responseModel = await configApi.queryList(queryForm);
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
      await configApi.batchDelete(selectedRowKeyList.value);
      message.success('删除成功');
      ajaxQuery();
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
    }
  };

  // ------------------------- 表单操作 弹窗 ------------------------------

  const configFormModal = ref();
  function toEditOrAdd(rowData) {
    configFormModal.value.showModal(rowData);
  }

  onMounted(ajaxQuery);
</script>
