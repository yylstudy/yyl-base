<template>
  <a-form class="smart-query-form" v-privilege="'system:operateLog:query'">
    <a-row class="smart-query-form-row">
      <a-form-item label="用户" class="smart-query-form-item">
        <a-input style="width: 300px" v-model:value="queryForm.username" placeholder="用户名称" />
      </a-form-item>

      <a-form-item label="登录时间" class="smart-query-form-item">
        <a-range-picker @change="changeCreateDate" v-model:value="createDateRange" :presets="defaultChooseTimeRange" style="width: 240px" />
      </a-form-item>

      <a-form-item class="smart-query-form-item smart-margin-left10">
        <a-button type="primary" @click="ajaxQuery" class="smart-margin-right10">
          <template #icon>
            <ReloadOutlined />
          </template>
          查询
        </a-button>
        <a-button @click="resetQuery">
          <template #icon>
            <SearchOutlined />
          </template>
          重置
        </a-button>
      </a-form-item>
    </a-row>
  </a-form>

  <a-card size="small" :bordered="false" :hoverable="true" style="height: 100%">
    <a-table size="small" :loading="tableLoading" :dataSource="tableData" :columns="columns" bordered rowKey="id" :pagination="false">
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'loginResult'">
          <a-tag v-if="text=='0'" :color="'success'">登录成功</a-tag>
          <a-tag v-if="text=='1'" :color="'error'">登录失败</a-tag>
          <a-tag v-if="text=='2'" :color="'success'">退出登录</a-tag>
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

    <LoginLogDetailModal ref="detailModal" />
  </a-card>
</template>
<script setup>
  import { onMounted, reactive, ref } from 'vue';
  import LoginLogDetailModal from './login-log-detail-modal.vue';
  import { loginLogApi } from '/@/api/system/login-log-api';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { defaultTimeRanges } from '/@/lib/default-time-ranges';
  import uaparser from 'ua-parser-js';
  import { smartSentry } from '/@/lib/smart-sentry';

  const columns = ref([
    {
      title: '用户',
      dataIndex: 'username',
      width: 150,
    },
    {
      title: '登录IP',
      dataIndex: 'loginIp',
      ellipsis: true,
    },
    {
      title: '登录结果',
      dataIndex: 'loginResult',
      ellipsis: true,
    },
    {
      title: '登录时间',
      dataIndex: 'createTime',
      ellipsis: true,
    },
  ]);

  const queryFormState = {
    username: '',
    loginResult: undefined,
    startDate: undefined,
    endDate: undefined,
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });
  const createDateRange = ref([]);
  const defaultChooseTimeRange = defaultTimeRanges;
  // 时间变动
  function changeCreateDate(dates, dateStrings) {
    queryForm.startDate = dateStrings[0];
    queryForm.endDate = dateStrings[1];
  }

  const tableLoading = ref(false);
  const tableData = ref([]);
  const total = ref(0);

  function resetQuery() {
    Object.assign(queryForm, queryFormState);
    createDateRange.value = [];
    ajaxQuery();
  }

  function onSearch() {
    queryForm.pageNum = 1;
    ajaxQuery();
  }

  async function ajaxQuery() {
    try {
      tableLoading.value = true;
      let responseModel = await loginLogApi.queryList(queryForm);
      const list = responseModel.data.list;
      total.value = parseInt(responseModel.data.total);
      tableData.value = list;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  onMounted(ajaxQuery);

  // ---------------------- 详情 ----------------------
  const detailModal = ref();
  function showDetail(id) {
    detailModal.value.show(id);
  }
</script>
