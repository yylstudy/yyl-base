<template>
  <a-drawer
    :title="form.id ? '编辑' : '添加'"
    :width="600"
    :open="visible"
    :body-style="{ paddingBottom: '80px' }"
    @close="onClose"
    destroyOnClose
  >
    <a-form
        :label-col="labelCol"
        :wrapper-col="wrapperCol"
        ref="formRef" :model="form" :rules="rules" layout="horizontal">
      <a-form-item label="姓名" name="username">
        <a-input v-model:value.trim="form.username" placeholder="请输入姓名" />
      </a-form-item>
      <a-form-item label="密码" name="password" v-if="!form.id">
        <a-input v-model:value.trim="form.password" type="password" placeholder="请输入密码" />
      </a-form-item>
      <a-form-item label="重复密码" name="confirmpassword"  v-if="!form.id">
        <a-input v-model:value.trim="form.confirmpassword" type="password" placeholder="请再次输入密码" />
      </a-form-item>
      <a-form-item label="手机号" name="phone">
        <a-input v-model:value.trim="form.phone" placeholder="请输入手机号" />
      </a-form-item>
      <a-form-item label="邮箱" name="email">
        <a-input v-model:value.trim="form.email" placeholder="请输入邮箱" />
      </a-form-item>
      <a-form-item label="部门" name="departId">
        <DepartmentTreeSelect ref="departmentTreeSelect" width="100%" :init="false" v-model:value="form.departId" />
      </a-form-item>
      <a-form-item label="性别" name="gender">
        <smart-enum-select style="width: 100%" v-model:value="form.gender" placeholder="请选择性别" enum-name="GENDER_ENUM" />
      </a-form-item>
      <a-form-item label="状态" name="disabledFlag">
        <a-select v-model:value="form.disabledFlag" placeholder="请选择状态">
          <a-select-option :value="0">启用</a-select-option>
          <a-select-option :value="1">禁用</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="角色" name="roleIdList">
        <a-select mode="multiple" v-model:value="form.roleIdList" optionFilterProp="title" placeholder="请选择角色">
          <a-select-option v-for="item in roleList" :key="item.id" :title="item.roleName">{{ item.roleName }}</a-select-option>
        </a-select>
      </a-form-item>
    </a-form>
    <div class="footer">
      <a-button style="margin-right: 8px" @click="onClose">取消</a-button>
      <a-button type="primary" style="margin-right: 8px" @click="onSubmit(false)">保存</a-button>
      <a-button v-if="!form.id" type="primary" @click="onSubmit(true)">保存并继续添加</a-button>
    </div>
  </a-drawer>
</template>
<script setup>
  import { message } from 'ant-design-vue';
  import _ from 'lodash';
  import { nextTick, reactive, ref } from 'vue';
  import { userApi } from '/@/api/system/user-api';
  import { roleApi } from '/@/api/system/role-api';
  import DepartmentTreeSelect from '/@/components/system/depart-tree-select/index.vue';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';
  import { GENDER_ENUM } from '/@/constants/common-const';
  import { regular } from '/@/constants/regular-const';
  import { SmartLoading } from '/@/components/framework/smart-loading';
import { smartSentry } from '/@/lib/smart-sentry';
  // ----------------------- 以下是字段定义 emits props ---------------------
  const departmentTreeSelect = ref();
  // emit
  const emit = defineEmits(['refresh', 'show-account']);

  // ----------------------- 显示/隐藏 ---------------------

  const visible = ref(false); // 是否展示抽屉
  // 隐藏
  function onClose() {
    reset();
    visible.value = false;
  }
  // 显示
  async function showDrawer(rowData) {
    Object.assign(form, formDefault);
    if (rowData && !_.isEmpty(rowData)) {
      Object.assign(form, rowData);
    }
    visible.value = true;
    nextTick(() => {
      queryAllRole();
    });
  }

  // ----------------------- 表单显示 ---------------------

  const roleList = ref([]); //角色列表
  async function queryAllRole() {
    let res = await roleApi.getCorpRole();
    roleList.value = res.data;
  }
  const labelCol = { style: { width: '100px' } };
  const wrapperCol = { span: 14 };
  const formRef = ref(); // 组件ref
  const formDefault = {
    id: undefined,
    username: undefined,
    departId: undefined,
    disabledFlag: 0,
    leaveFlag: 0,
    gender: GENDER_ENUM.MAN.value,
    password: undefined,
    confirmpassword: undefined,
    phone: undefined,
    email: undefined,
    roleIdList: undefined,
  };

  let form = reactive(_.cloneDeep(formDefault));
  function reset() {
    Object.assign(form, formDefault);
    formRef.value.resetFields();
  }

  // ----------------------- 表单提交 ---------------------
  // 表单规则
  const rules = {
    username: [
      { required: true, message: '姓名不能为空' },
      { max: 30, message: '姓名不能大于30个字符', trigger: 'blur' },
    ],
    phone: [
      { required: true, message: '手机号不能为空' },
      { pattern: regular.phone, message: '请输入正确的手机号码', trigger: 'blur' },
    ],
    email: [
      { required: true, message: '邮箱不能为空' },
      { pattern: regular.email, message: '请输入正确的邮箱', trigger: 'blur' },
    ],
    password: [
      { required: true, message: '密码不能为空' },
      { max: 30, message: '密码不能大于30个字符', trigger: 'blur' },
    ],
    confirmpassword: [
      { required: true, message: '密码不能为空' },
      { max: 30, message: '密码不能大于30个字符', trigger: 'blur' },
    ],
    gender: [{ required: true, message: '性别不能为空' }],
    departId: [{ required: false, message: '部门不能为空' }],
    disabledFlag: [{ required: true, message: '状态不能为空' }],
    leaveFlag: [{ required: true, message: '在职状态不能为空' }],
  };

  // 校验表单
  function validateForm(formRef) {
    return new Promise((resolve) => {
      formRef
        .validate()
        .then(() => {
          resolve(true);
        })
        .catch(() => {
          resolve(false);
        });
    });
  }

  // 提交数据
  async function onSubmit(keepAdding) {
    let validateFormRes = await validateForm(formRef.value);
    if (!validateFormRes) {
      message.error('参数验证错误，请仔细填写表单数据!');
      return;
    }
    SmartLoading.show();
    if (form.id) {
      await edit(keepAdding);
    } else {
      await add(keepAdding);
    }
  }

  async function add(keepAdding) {
    try {
      if(form.password!=form.confirmpassword){
        message.error('密码不一致');
        return;
      }
      let { data } = await userApi.add(form);
      message.success('添加成功');
      // emit('show-account', form.username, data);
      if (keepAdding) {
        reset();
      } else {
        onClose();
      }
      emit('refresh');
    } catch (error) {
      smartSentry.captureError(error);
    } finally {
      SmartLoading.hide();
    }
  }
  async function edit(keepAdding) {
    try {
      let result = await userApi.edit(form);
      message.success('更新成功');
      if (keepAdding) {
        reset();
      } else {
        onClose();
      }
      emit('refresh');
    } catch (error) {
      smartSentry.captureError(error);
    } finally {
      SmartLoading.hide();
    }
  }

  // ----------------------- 以下是暴露的方法内容 ----------------------------
  defineExpose({
    showDrawer,
  });
</script>
<style scoped lang="less">
  .footer {
    position: absolute;
    right: 0;
    bottom: 0;
    width: 100%;
    border-top: 1px solid #e9e9e9;
    padding: 10px 16px;
    background: #fff;
    text-align: right;
    z-index: 1;
  }
  .hint {
    margin-top: 5px;
    color: #bfbfbf;
  }
</style>
