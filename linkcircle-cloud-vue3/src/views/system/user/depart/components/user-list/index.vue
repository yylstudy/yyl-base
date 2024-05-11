<template>
  <a-card class="employee-container">
    <div class="header">
      <a-typography-title :level="5">部门人员</a-typography-title>
      <div class="query-operate">
        <a-radio-group v-model:value="params.disabledFlag" style="margin: 8px; flex-shrink: 0" @change="queryUserByUsername(false)">
          <a-radio-button :value="undefined">全部</a-radio-button>
          <a-radio-button :value="false">启用</a-radio-button>
          <a-radio-button :value="true">禁用</a-radio-button>
        </a-radio-group>
        <a-input-search v-model:value.trim="params.username" placeholder="登录名" @search="queryUserByUsername(true)">
          <template #enterButton>
            <a-button style="margin-left: 8px" type="primary">
              <template #icon>
                <SearchOutlined />
              </template>
              查询
            </a-button>
          </template>
        </a-input-search>
        <a-button @click="reset">
          <template #icon>
            <ReloadOutlined />
          </template>
          重置
        </a-button>
      </div>
    </div>
    <div class="btn-group">
      <a-button class="btn" type="primary" @click="showDrawer" v-privilege="'system:user:add'" size="small">新增</a-button>
      <a-button class="btn" size="small" @click="updateEmployeeDepartment" v-privilege="'system:user:depart:update'">调整部门</a-button>
      <a-button class="btn" size="small" @click="batchDelete" v-privilege="'system:user:delete'">批量删除</a-button>
    </div>

    <a-table
      :row-selection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
      size="small"
      :columns="columns"
      :data-source="tableData"
      :pagination="false"
      :loading="tableLoading"
      :scroll="{ x: 1200 }"
      row-key="id"
      bordered
    >
      <template #bodyCell="{ text, record, index, column }">
        <template v-if="column.dataIndex === 'disabledFlag'">
          <a-tag :color="text ? 'error' : 'processing'">{{ text ? '禁用' : '启用' }}</a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'gender'">
          <span>{{ $smartEnumPlugin.getDescByValue('GENDER_ENUM', text) }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'operate'">
          <div class="smart-table-operate">
            <a-button v-privilege="'system:user:update'" type="link" size="small" @click="showDrawer(record)">编辑</a-button>
            <a-button
              v-privilege="'system:user:password:reset'"
              type="link"
              size="small"
              @click="resetPassword(record.id, record.username)"
              >重置密码</a-button
            >
            <a-button v-privilege="'system:user:disabled'" type="link" @click="updateDisabled(record.id, record.disabledFlag)">{{
              record.disabledFlag ? '启用' : '禁用'
            }}</a-button>
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
        :defaultPageSize="params.pageSize"
        v-model:current="params.pageNum"
        v-model:pageSize="params.pageSize"
        :total="total"
        @change="queryUser"
        @showSizeChange="queryUser"
        :show-total="showTableTotal"
      />
    </div>
    <EmployeeFormModal ref="employeeFormModal" @refresh="queryUser" @show-account="showAccount" />
    <EmployeeDepartmentFormModal ref="employeeDepartmentFormModal" @refresh="queryUser" />
    <EmployeePasswordDialog ref="employeePasswordDialog" />
  </a-card>
</template>
<script setup lang="ts">
  import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
  import { message, Modal } from 'ant-design-vue';
  import _ from 'lodash';
  import { computed, createVNode, reactive, ref, watch } from 'vue';
  import { userApi } from '/@/api/system/user-api';
  import { PAGE_SIZE } from '/@/constants/common-const';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import EmployeeFormModal from '../user-form-modal/index.vue';
  import EmployeeDepartmentFormModal from '../user-depart-form-modal/index.vue';
  import EmployeePasswordDialog from '../user-password-dialog/index.vue';
  import { PAGE_SIZE_OPTIONS, showTableTotal } from '/@/constants/common-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import TableOperator from '/@/components/support/table-operator/list.vue';

  // ----------------------- 以下是字段定义 emits props ---------------------

  const props = defineProps({
    departId: String,
    breadcrumb: Array,
  });

  //-------------回显账号密码信息----------
  let employeePasswordDialog = ref();
  function showAccount(accountName, passWord) {
    employeePasswordDialog.value.showModal(accountName, passWord);
  }

  // ----------------------- 表格/列表/ 搜索 ---------------------
  //字段
  const columns = ref([
    {
      title: '姓名',
      dataIndex: 'realname',
      width: 85,
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      width: 80,
    },
    {
      title: '性别',
      dataIndex: 'gender',
      width: 40,
    },
    {
      title: '登录账号',
      dataIndex: 'username',
      width: 100,
    },
    {
      title: '状态',
      dataIndex: 'disabledFlag',
      width: 60,
    },
    {
      title: '角色',
      dataIndex: 'roleNameList',
      width: 100,
    },
    {
      title: '部门',
      dataIndex: 'departName',
      ellipsis: true,
      width: 200,
    },
    {
      title: '操作',
      dataIndex: 'operate',
      width: 120,
    },
  ]);
  const tableData = ref();

  let defaultParams = {
    departId: undefined,
    disabledFlag: false,
    username: undefined,
    searchCount: undefined,
    pageNum: 1,
    pageSize: PAGE_SIZE,
    sortItemList: undefined,
  };
  const params = reactive({ ...defaultParams });
  const total = ref(0);

  // 搜索重置
  function reset() {
    Object.assign(params, defaultParams);
    queryUser();
  }

  const tableLoading = ref(false);
  // 查询
  async function queryUser() {
    tableLoading.value = true;
    try {
      params.departId = props.departId;
      let res = await userApi.queryUser(params);
      for (const item of res.data.list) {
        item.roleNameList = _.join(item.roleNameList, ',');
      }
      tableData.value = res.data.list;
      total.value = parseInt(res.data.total);
      // 清除选中
      selectedRowKeys.value = [];
      selectedRows.value = [];
    } catch (error) {
      smartSentry.captureError(error);
    } finally {
      tableLoading.value = false;
    }
  }

  // 根据关键字 查询
  async function queryUserByUsername(allDepartment) {
    tableLoading.value = true;
    try {
      params.pageNum = 1;
      params.departId = allDepartment ? undefined : props.departId;
      let res = await userApi.queryUser(params);
      for (const item of res.data.list) {
        item.roleNameList = _.join(item.roleNameList, ',');
      }
      tableData.value = res.data.list;
      total.value = parseInt(res.data.total)
      // 清除选中
      selectedRowKeys.value = [];
      selectedRows.value = [];
    } catch (error) {
      smartSentry.captureError(error);
    } finally {
      tableLoading.value = false;
    }
  }

  watch(
    () => props.departId,
    () => {
      if (props.departId !== params.departId) {
        params.pageNum = 1;
        queryUser();
      }
    },
    { immediate: true }
  );

  // ----------------------- 多选操作 ---------------------

  let selectedRowKeys = ref([]);
  let selectedRows = ref([]);
  // 是否有选中：用于 批量操作按钮的禁用
  const hasSelected = computed(() => selectedRowKeys.value.length > 0);

  function onSelectChange(keyArray, selectRows) {
    selectedRowKeys.value = keyArray;
    selectedRows.value = selectRows;
  }

  // 批量删除员工
  function batchDelete() {
    if (!hasSelected.value) {
      message.warning('请选择要删除的员工');
      return;
    }
    const realnameArray = selectedRows.value.map((e) => e.realname);
    const userIdArray = selectedRows.value.map((e) => e.id);
    Modal.confirm({
      title: '确定要删除如下员工吗?',
      icon: createVNode(ExclamationCircleOutlined),
      content: _.join(realnameArray, ','),
      okText: '删除',
      okType: 'danger',
      async onOk() {
        SmartLoading.show();
        try {
          await userApi.batchDeleteEmployee(userIdArray);
          message.success('删除成功');
          queryUser();
          selectedRowKeys.value = [];
          selectedRows.value = [];
        } catch (error) {
          smartSentry.captureError(error);
        } finally {
          SmartLoading.hide();
        }
      },
      cancelText: '取消',
      onCancel() {},
    });
  }

  // 批量更新员工部门
  const employeeDepartmentFormModal = ref();

  function updateEmployeeDepartment() {
    if (!hasSelected.value) {
      message.warning('请选择要调整部门的员工');
      return;
    }
    const userIdArray = selectedRows.value.map((e) => e.id);
    employeeDepartmentFormModal.value.showModal(userIdArray);
  }

  // ----------------------- 添加、修改、禁用、重置密码 ------------------------------------

  const employeeFormModal = ref(); //组件

  // 展示编辑弹窗
  function showDrawer(rowData) {
    let params = {};
    if (rowData) {
      params = _.cloneDeep(rowData);
      params.disabledFlag = params.disabledFlag ? 1 : 0;
    } else if (props.departId) {
      params.departId = props.departId;
    }
    employeeFormModal.value.showDrawer(params);
  }

  // 重置密码
  function resetPassword(id, name) {
    Modal.confirm({
      title: '提醒',
      icon: createVNode(ExclamationCircleOutlined),
      content: '确定要重置密码吗?',
      okText: '确定',
      okType: 'danger',
      async onOk() {
        SmartLoading.show();
        try {
          let { data: passWord } = await userApi.resetPassword(id);
          message.success('重置成功');
          employeePasswordDialog.value.showModal(name, passWord);
          queryUser();
        } catch (error) {
          smartSentry.captureError(error);
        } finally {
          SmartLoading.hide();
        }
      },
      cancelText: '取消',
      onCancel() {},
    });
  }

  // 禁用 / 启用
  function updateDisabled(id, disabledFlag) {
    Modal.confirm({
      title: '提醒',
      icon: createVNode(ExclamationCircleOutlined),
      content: `确定要${disabledFlag ? '启用' : '禁用'}吗?`,
      okText: '确定',
      okType: 'danger',
      async onOk() {
        SmartLoading.show();
        try {
          await userApi.updateDisabled(id);
          message.success(`${disabledFlag ? '启用' : '禁用'}成功`);
          queryUser();
        } catch (error) {
          smartSentry.captureError(error);
        } finally {
          SmartLoading.hide();
        }
      },
      cancelText: '取消',
      onCancel() {},
    });
  }
</script>
<style scoped lang="less">
  .employee-container {
    height: 100%;
  }
  .header {
    display: flex;
    align-items: center;
  }
  .query-operate {
    margin-left: auto;
    display: flex;
    align-items: center;
  }

  .btn-group {
    margin: 10px 0;
    .btn {
      margin-right: 8px;
    }
  }
</style>
