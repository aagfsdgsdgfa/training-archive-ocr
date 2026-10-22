<template>
  <div class="app-container cloud-glass-container">
    <el-row :gutter="20">
      <splitpanes :horizontal="this.$store.getters.device === 'mobile'" class="default-theme glass-splitpanes">
        <pane size="18">
          <div class="glass-panel" style="height: 100%; padding: 15px;">
            <div class="head-container">
              <el-input v-model="deptName" placeholder="输入部门名称检索" clearable size="small" prefix-icon="el-icon-search" class="glass-input" style="margin-bottom: 20px" />
            </div>
            <div class="head-container">
              <el-tree 
                :data="deptOptions" 
                :props="defaultProps" 
                :expand-on-click-node="false" 
                :filter-node-method="filterNode" 
                ref="tree" 
                node-key="id" 
                default-expand-all 
                highlight-current 
                @node-click="handleNodeClick" 
                class="glass-tree"
              />
            </div>
          </div>
        </pane>
        
        <pane size="82">
          <div class="glass-panel" style="margin-left: 10px; height: 100%;">
            <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px" class="search-panel">
              <el-form-item label="用户名称" prop="userName">
                <el-input v-model="queryParams.userName" placeholder="请输入用户名称" clearable class="glass-input" style="width: 200px" @keyup.enter.native="handleQuery" />
              </el-form-item>
              <el-form-item label="手机号码" prop="phonenumber">
                <el-input v-model="queryParams.phonenumber" placeholder="请输入手机号码" clearable class="glass-input" style="width: 200px" @keyup.enter.native="handleQuery" />
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-select v-model="queryParams.status" placeholder="用户状态" clearable class="glass-input" style="width: 150px">
                  <el-option v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="创建时间">
                <el-date-picker v-model="dateRange" style="width: 240px" class="glass-input" value-format="yyyy-MM-dd" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"></el-date-picker>
              </el-form-item>
              <el-form-item class="search-btn-group">
                <el-button type="primary" icon="el-icon-search" size="mini" class="glass-btn primary" @click="handleQuery">搜索</el-button>
                <el-button icon="el-icon-refresh" size="mini" class="glass-btn" @click="resetQuery">重置</el-button>
              </el-form-item>
            </el-form>

            <el-row :gutter="10" class="mb8 toolbar-row">
              <el-col :span="1.5">
                <el-button type="primary" plain icon="el-icon-plus" size="mini" class="glass-btn primary-plain" @click="handleAdd" v-hasPermi="['system:user:add']">新增员工</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="success" plain icon="el-icon-edit" size="mini" class="glass-btn success-plain" :disabled="single" @click="handleUpdate" v-hasPermi="['system:user:edit']">修改</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="danger" plain icon="el-icon-delete" size="mini" class="glass-btn danger-plain" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:user:remove']">删除</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="info" plain icon="el-icon-upload2" size="mini" class="glass-btn" @click="handleImport" v-hasPermi="['system:user:import']">导入</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="warning" plain icon="el-icon-download" size="mini" class="glass-btn warning-plain" @click="handleExport" v-hasPermi="['system:user:export']">导出</el-button>
              </el-col>
              <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
            </el-row>

            <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange" class="glass-table">
              <el-table-column type="selection" width="50" align="center" />
              <el-table-column label="编号" align="center" key="userId" prop="userId" v-if="columns.userId.visible" width="60" />
              <el-table-column label="员工姓名" align="left" key="nickName" prop="nickName" v-if="columns.nickName.visible" :show-overflow-tooltip="true">
                <template slot-scope="scope">
                  <span class="elegant-text">{{ scope.row.nickName }}</span>
                </template>
              </el-table-column>
              <el-table-column label="登录账号" align="center" key="userName" prop="userName" v-if="columns.userName.visible" :show-overflow-tooltip="true" />
              <el-table-column label="归属部门" align="center" key="deptName" prop="dept.deptName" v-if="columns.deptName.visible" :show-overflow-tooltip="true" />
              
              <el-table-column label="学历" align="center" prop="education" width="90">
                <template slot-scope="scope">
                   <el-tag size="mini" type="info" v-if="scope.row.education">{{ scope.row.education }}</el-tag>
                   <span v-else style="color: #ccc; font-size: 12px;">--</span>
                </template>
              </el-table-column>
              <el-table-column label="入职时间" align="center" prop="joinDate" width="110">
                <template slot-scope="scope">
                  <span>{{ parseTime(scope.row.joinDate, '{y}-{m}-{d}') || '--' }}</span>
                </template>
              </el-table-column>

              <el-table-column label="手机号码" align="center" key="phonenumber" prop="phonenumber" v-if="columns.phonenumber.visible" width="120" />
              <el-table-column label="状态" align="center" key="status" v-if="columns.status.visible" width="80">
                <template slot-scope="scope">
                  <el-switch v-model="scope.row.status" active-value="0" inactive-value="1" @change="handleStatusChange(scope.row)"></el-switch>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width" fixed="right">
                <template slot-scope="scope" v-if="scope.row.userId !== 1">
                  <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:user:edit']">修改</el-button>
                  <el-button size="mini" type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(scope.row)" v-hasPermi="['system:user:remove']">删除</el-button>
                  <el-dropdown size="mini" @command="(command) => handleCommand(command, scope.row)" v-hasPermi="['system:user:resetPwd', 'system:user:edit']" style="margin-left: 10px;">
                    <el-button size="mini" type="text" icon="el-icon-d-arrow-right">更多</el-button>
                    <el-dropdown-menu slot="dropdown" class="glass-dropdown">
                      <el-dropdown-item command="handleResetPwd" icon="el-icon-key" v-hasPermi="['system:user:resetPwd']">重置密码</el-dropdown-item>
                      <el-dropdown-item command="handleAuthRole" icon="el-icon-user" v-hasPermi="['system:user:edit']">分配角色</el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </template>
              </el-table-column>
            </el-table>

            <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" class="glass-pagination" />
          </div>
        </pane>
      </splitpanes>
    </el-row>

    <el-dialog :title="title" :visible.sync="open" width="750px" append-to-body custom-class="glass-dialog" :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px" class="elegant-form">
        <el-divider content-position="left">基础账号信息</el-divider>
        <el-row>
          <el-col :span="12">
            <el-form-item label="员工姓名" prop="nickName">
              <el-input v-model="form.nickName" placeholder="真实姓名（如：张三）" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归属部门" prop="deptId">
              <treeselect v-model="form.deptId" :options="enabledDeptOptions" :show-count="true" placeholder="请选择归属部门" class="glass-treeselect" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="登录账号" prop="userName">
              <el-input v-model="form.userName" placeholder="用于登录系统（如：工号或拼音）" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="初始密码" prop="password">
              <el-input v-model="form.password" placeholder="请输入初始密码" type="password" maxlength="20" show-password />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">人事档案信息 (关键字段)</el-divider>
        <el-row>
          <el-col :span="8">
            <el-form-item label="出生年月" prop="birthday">
              <el-date-picker v-model="form.birthday" type="date" value-format="yyyy-MM-dd" placeholder="选择出生年月" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最高学历" prop="education">
              <el-select v-model="form.education" placeholder="请选择学历" style="width: 100%;" clearable allow-create filterable>
                <el-option label="博士" value="博士" />
                <el-option label="硕士" value="硕士" />
                <el-option label="本科" value="本科" />
                <el-option label="大专" value="大专" />
                <el-option label="高中及以下" value="高中及以下" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="入职时间" prop="joinDate">
              <el-date-picker v-model="form.joinDate" type="date" value-format="yyyy-MM-dd" placeholder="选择进公司时间" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="手机号码" prop="phonenumber">
              <el-input v-model="form.phonenumber" placeholder="请输入联系方式" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户性别">
              <el-select v-model="form.sex" placeholder="请选择性别" style="width: 100%;">
                <el-option v-for="dict in dict.type.sys_user_sex" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">权限与配置</el-divider>
        <el-row>
          <el-col :span="12">
            <el-form-item label="分配岗位">
              <el-select v-model="form.postIds" multiple placeholder="请指定岗位" style="width: 100%;">
                <el-option v-for="item in postOptions" :key="item.postId" :label="item.postName" :value="item.postId" :disabled="item.status == 1" ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="系统角色">
              <el-select v-model="form.roleIds" multiple placeholder="分配系统权限角色" style="width: 100%;">
                <el-option v-for="item in roleOptions" :key="item.roleId" :label="item.roleName" :value="item.roleId" :disabled="item.status == 1"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row>
          <el-col :span="12">
            <el-form-item label="账号状态">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="选填邮箱" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-form-item label="备注说明">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="填写人事相关附加信息..."></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="glass-btn primary" @click="submitForm">确 定</el-button>
        <el-button class="glass-btn" @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="upload.title" :visible.sync="upload.open" width="400px" append-to-body custom-class="glass-dialog">
      <el-upload ref="upload" :limit="1" accept=".xlsx, .xls" :headers="upload.headers" :action="upload.url + '?updateSupport=' + upload.updateSupport" :disabled="upload.isUploading" :on-progress="handleFileUploadProgress" :on-success="handleFileSuccess" :auto-upload="false" drag>
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip text-center" slot="tip">
          <div class="el-upload__tip" slot="tip">
            <el-checkbox v-model="upload.updateSupport" />是否更新已经存在的用户数据
          </div>
          <span>仅允许导入xls、xlsx格式文件。</span>
          <el-link type="primary" :underline="false" style="font-size: 12px; vertical-align: baseline" @click="importTemplate">下载模板</el-link>
        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="glass-btn primary" @click="submitFileForm">确 定</el-button>
        <el-button class="glass-btn" @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listUser, getUser, delUser, addUser, updateUser, resetUserPwd, changeUserStatus, deptTreeSelect } from "@/api/system/user";
import { getToken } from "@/utils/auth";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";
import { Splitpanes, Pane } from "splitpanes";
import "splitpanes/dist/splitpanes.css";

export default {
  name: "User",
  dicts: ['sys_normal_disable', 'sys_user_sex'],
  components: { Treeselect, Splitpanes, Pane },
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      userList: null,
      title: "",
      deptOptions: [],
      enabledDeptOptions: [],
      open: false,
      deptName: undefined,
      initPassword: undefined,
      dateRange: [],
      postOptions: [],
      roleOptions: [],
      form: {
        deptId: null,
      },
      defaultProps: {
        children: "children",
        label: "label"
      },
      upload: {
        open: false,
        title: "",
        isUploading: false,
        updateSupport: 0,
        headers: { Authorization: "Bearer " + getToken() },
        url: process.env.VUE_APP_BASE_API + "/system/user/importData"
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: undefined,
        phonenumber: undefined,
        status: undefined,
        deptId: undefined
      },
      columns: {
        userId: { label: '用户编号', visible: true },
        userName: { label: '用户名称', visible: true },
        nickName: { label: '用户昵称', visible: true },
        deptName: { label: '部门', visible: true },
        phonenumber: { label: '手机号码', visible: true },
        status: { label: '状态', visible: true },
        createTime: { label: '创建时间', visible: true }
      },
      rules: {
        userName: [
          { required: true, message: "登录账号不能为空", trigger: "blur" },
          { min: 2, max: 20, message: '登录账号长度必须介于 2 和 20 之间', trigger: 'blur' }
        ],
        nickName: [
          { required: true, message: "员工姓名不能为空", trigger: "blur" }
        ],
        deptId: [
          { required: true, message: "必须指派归属部门", trigger: "change" }
        ],
        password: [
          { required: true, message: "初始密码不能为空", trigger: "blur" },
          { min: 5, max: 20, message: '用户密码长度必须介于 5 和 20 之间', trigger: 'blur' },
          { pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符：< > \" ' \\\ |", trigger: "blur" }
        ],
        email: [
          { type: "email", message: "请输入正确的邮箱地址", trigger: ["blur", "change"] }
        ],
        phonenumber: [
          { pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输入正确的手机号码", trigger: "blur" }
        ]
      }
    };
  },
  watch: {
    deptName(val) {
      this.$refs.tree.filter(val);
    }
  },
  created() {
    this.getList();
    this.getDeptTree();
    this.getConfigKey("sys.user.initPassword").then(response => {
      this.initPassword = response.msg;
    });
  },
  methods: {
    getList() {
      this.loading = true;
      listUser(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
          this.userList = response.rows;
          this.total = response.total;
          this.loading = false;
        }
      );
    },
    getDeptTree() {
      deptTreeSelect().then(response => {
        const data = response?.data || [];
        this.$set(this, 'deptOptions', data);
        this.$set(this, 'enabledDeptOptions', data);
      });
    },
    filterDisabledDept(deptList) {
      return deptList.filter(dept => {
        if (dept.disabled) {
          return false;
        }
        if (dept.children && dept.children.length) {
          dept.children = this.filterDisabledDept(dept.children);
        }
        return true;
      });
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    handleNodeClick(data) {
      this.queryParams.deptId = data.id;
      this.handleQuery();
    },
    handleStatusChange(row) {
      let text = row.status === "0" ? "启用" : "停用";
      this.$modal.confirm('确认要"' + text + '""' + row.userName + '"用户吗？').then(function() {
        return changeUserStatus(row.userId, row.status);
      }).then(() => {
        this.$modal.msgSuccess(text + "成功");
      }).catch(function() {
        row.status = row.status === "0" ? "1" : "0";
      });
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        userId: undefined,
        deptId: undefined,
        userName: undefined,
        nickName: undefined,
        password: undefined,
        phonenumber: undefined,
        email: undefined,
        sex: undefined,
        // 新增的三个核心人事字段初始化
        birthday: undefined,
        education: undefined,
        joinDate: undefined,
        status: "0",
        remark: undefined,
        postIds: [],
        roleIds: []
      };
      this.resetForm("form");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.queryParams.deptId = undefined;
      this.$refs.tree.setCurrentKey(null);
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.userId);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
    handleCommand(command, row) {
      switch (command) {
        case "handleResetPwd":
          this.handleResetPwd(row);
          break;
        case "handleAuthRole":
          this.handleAuthRole(row);
          break;
        default:
          break;
      }
    },
    handleAdd() {
      this.reset();
      this.getDeptTree();
      // 获取当前登录用户信息，用于设置新增人员部门默认值
      getUser().then(response => {
        this.postOptions = response.posts || [];
        this.roleOptions = response.roles || [];
        // 设置部门默认值为当前用户所在部门
        if (response.data && response.data.deptId) {
          this.$set(this.form, 'deptId', response.data.deptId);
        }
        this.open = true;
        this.title = "办理员工入职档案";
        this.form.password = this.initPassword;
      }).catch(() => {
        this.postOptions = [];
        this.roleOptions = [];
        this.open = true;
        this.title = "办理员工入职档案";
        this.form.password = this.initPassword;
      });
    },
    handleUpdate(row) {
      this.reset();
      const userId = row.userId || this.ids;
      getUser(userId).then(response => {
        this.form = response.data;
        this.postOptions = response.posts;
        this.roleOptions = response.roles;
        this.$set(this.form, "postIds", response.postIds);
        this.$set(this.form, "roleIds", response.roleIds);
        this.open = true;
        this.title = "修改员工人事档案";
        this.form.password = "";
      });
    },
    handleResetPwd(row) {
      this.$prompt('请输入"' + row.userName + '"的新密码', "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        closeOnClickModal: false,
        inputPattern: /^.{5,20}$/,
        inputErrorMessage: "用户密码长度必须介于 5 和 20 之间",
        inputValidator: (value) => {
          if (/<|>|"|'|\||\\/.test(value)) {
            return "不能包含非法字符：< > \" ' \\\ |";
          }
        },
      }).then(({ value }) => {
          resetUserPwd(row.userId, value).then(response => {
            this.$modal.msgSuccess("修改成功，新密码是：" + value);
          });
        }).catch(() => {});
    },
    handleAuthRole: function(row) {
      const userId = row.userId;
      this.$router.push("/system/user-auth/role/" + userId);
    },
    submitForm: function() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.userId != undefined) {
            updateUser(this.form).then(response => {
              this.$modal.msgSuccess("资料更新成功");
              this.open = false;
              this.getList();
            });
          } else {
            addUser(this.form).then(response => {
              this.$modal.msgSuccess("入职办理成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      const userIds = row.userId || this.ids;
      this.$modal.confirm('是否确认离职/删除员工编号为"' + userIds + '"的数据项？').then(function() {
        return delUser(userIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleExport() {
      this.download('system/user/export', {
        ...this.queryParams
      }, `user_${new Date().getTime()}.xlsx`);
    },
    handleImport() {
      this.upload.title = "员工人事档案导入";
      this.upload.open = true;
    },
    importTemplate() {
      this.download('system/user/importTemplate', {
      }, `user_template_${new Date().getTime()}.xlsx`);
    },
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true;
    },
    handleFileSuccess(response, file, fileList) {
      this.upload.open = false;
      this.upload.isUploading = false;
      this.$refs.upload.clearFiles();
      this.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", { dangerouslyUseHTMLString: true });
      this.getList();
    },
    submitFileForm() {
      const file = this.$refs.upload.uploadFiles;
      if (!file || file.length === 0 || !file[0].name.toLowerCase().endsWith('.xls') && !file[0].name.toLowerCase().endsWith('.xlsx')) {
        this.$modal.msgError("请选择后缀为 “xls”或“xlsx”的文件。");
        return;
      }
      this.$refs.upload.submit();
    }
  }
};
</script>

<style scoped>
/* ================= 全局环境：流云质感背景 ================= */
.cloud-glass-container {
  min-height: calc(100vh - 84px);
  background: #f0f2f5;
  position: relative;
  z-index: 1;
  padding: 20px;
}


/* ================= Splitpanes 透明分割面板修复 ================= */
.glass-splitpanes.default-theme .splitpanes__pane {
  background: transparent !important;
}
.glass-splitpanes.default-theme .splitpanes__splitter {
  background-color: transparent !important;
  border-left: 1px solid rgba(0,0,0,0.05);
  width: 10px;
}
.glass-splitpanes.default-theme .splitpanes__splitter:before {
  background-color: rgba(64, 158, 255, 0.3) !important;
}

/* ================= 部门树玻璃化 ================= */
.glass-tree {
  background: transparent !important;
  color: #606266;
}
::v-deep .glass-tree .el-tree-node__content:hover {
  background-color: rgba(64, 158, 255, 0.1) !important;
  border-radius: 4px;
}
::v-deep .glass-tree .el-tree-node:focus > .el-tree-node__content {
  background-color: rgba(64, 158, 255, 0.15) !important;
}

/* ================= 卡片基类 ================= */
.glass-panel {
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  padding: 20px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}
.glass-panel:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* ================= 搜索组件美化 ================= */
.search-panel .el-form-item {
  margin-bottom: 15px;
}
::v-deep .glass-input .el-input__inner {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid rgba(200, 200, 200, 0.3) !important;
  border-radius: 8px;
  transition: all 0.3s;
}
::v-deep .glass-input .el-input__inner:focus {
  background: rgba(255, 255, 255, 0.9) !important;
  border-color: #1890ff !important;
  box-shadow: 0 0 8px rgba(64, 158, 255, 0.2);
}

/* ================= Treeselect 部门树样式穿透与高度对齐 ================= */
::v-deep .glass-treeselect .vue-treeselect__control {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid rgba(200, 200, 200, 0.3) !important;
  border-radius: 8px !important;
  height: 32px !important;
  line-height: 30px !important;
}
::v-deep .glass-treeselect .vue-treeselect__placeholder, 
::v-deep .glass-treeselect .vue-treeselect__single-value {
  line-height: 30px !important;
  color: #606266;
}
::v-deep .vue-treeselect__menu {
  background: #ffffff !important;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12) !important;
}

/* ================= 按钮高级定制 ================= */
.glass-btn {
  border-radius: 8px;
  font-weight: 500;
  border: none;
  background: rgba(255, 255, 255, 0.6);
  color: #606266;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.glass-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.9);
}
.glass-btn.primary { background: #1890ff; color: white; }
.glass-btn.primary:hover { background: #40a9ff; box-shadow: 0 2px 8px rgba(24, 128, 255, 0.3); }

.glass-btn.primary-plain { color: #1890ff; background: rgba(24, 128, 255, 0.06); border: 1px solid rgba(24, 128, 255, 0.2); }
.glass-btn.success-plain { color: #67C23A; background: rgba(103, 194, 58, 0.1); border: 1px solid rgba(103, 194, 58, 0.3); }
.glass-btn.warning-plain { color: #E6A23C; background: rgba(230, 162, 60, 0.1); border: 1px solid rgba(230, 162, 60, 0.3); }
.glass-btn.danger-plain { color: #F56C6C; background: rgba(245, 108, 108, 0.1); border: 1px solid rgba(245, 108, 108, 0.3); }

/* ================= 表格透明毛玻璃化 ================= */
::v-deep .glass-table {
  background: #ffffff !important;
}
::v-deep .glass-table th, 
::v-deep .glass-table tr, 
::v-deep .glass-table td {
  background: transparent !important;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}
::v-deep .glass-table th {
  background: rgba(245, 247, 250, 0.4) !important;
  color: #333;
  font-weight: 600;
}
::v-deep .glass-table::before {
  display: none; 
}

/* ================= 文本质感 ================= */
.elegant-text {
  font-weight: 500;
  color: #303133;
}

/* ================= 弹窗高级美化 ================= */
::v-deep .glass-dialog {
  background: #ffffff !important;
  border-radius: 8px !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12) !important;
}
::v-deep .glass-dialog .el-dialog__header {
  border-bottom: 1px solid rgba(0,0,0,0.05);
  padding-bottom: 20px;
}
::v-deep .glass-dialog .el-dialog__title {
  font-weight: 600;
  color: #303133;
}
::v-deep .glass-dialog .el-dialog__footer {
  border-top: 1px solid rgba(0,0,0,0.05);
}

/* 下拉菜单美化 */
::v-deep .glass-dropdown {
  background: rgba(255, 255, 255, 0.9) !important;

  border-radius: 8px;
  border: 1px solid rgba(200,200,200,0.2);
}
</style>