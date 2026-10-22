<template>
  <div class="app-container cloud-glass-container">
    <div class="glass-panel search-panel" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="90px">
        <el-form-item label="讲师姓名" prop="lecturerName">
          <el-input v-model="queryParams.lecturerName" placeholder="模糊搜索讲师姓名" clearable class="glass-input" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="讲师类型" prop="lecturerType">
          <el-select v-model="queryParams.lecturerType" placeholder="讲师类型" clearable class="glass-input" style="width: 150px;">
            <el-option label="内部讲师" value="0" />
            <el-option label="外部专家" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属机构" prop="companyName">
          <el-input v-model="queryParams.companyName" placeholder="部门或外部机构" clearable class="glass-input" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="擅长领域" prop="expertiseArea">
          <el-input v-model="queryParams.expertiseArea" placeholder="如: 安全、质量" clearable class="glass-input" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item class="search-btn-group">
          <el-button type="primary" icon="el-icon-search" size="mini" class="glass-btn primary" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" size="mini" class="glass-btn" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-panel table-panel">
      <el-row :gutter="10" class="mb8 toolbar-row">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="el-icon-plus" size="mini" class="glass-btn primary-plain" @click="handleAdd" v-hasPermi="['biz:lecturer:add']">新增讲师</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="el-icon-edit" size="mini" class="glass-btn success-plain" :disabled="single" @click="handleUpdate" v-hasPermi="['biz:lecturer:edit']">修改资质</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="el-icon-delete" size="mini" class="glass-btn danger-plain" :disabled="multiple" @click="handleDelete" v-hasPermi="['biz:lecturer:remove']">移除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" icon="el-icon-delete-solid" size="mini" class="glass-btn danger" @click="handleDeleteAll" v-hasPermi="['biz:lecturer:remove']">全部删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="warning" plain icon="el-icon-download" size="mini" class="glass-btn warning-plain" @click="handleExport" v-hasPermi="['biz:lecturer:export']">导出名册</el-button>
        </el-col>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="lecturerList" @selection-change="handleSelectionChange" class="glass-table">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="编号" align="center" prop="lecturerId" width="70" />
        
        <el-table-column label="讲师姓名" align="left" prop="lecturerName" min-width="120">
          <template slot-scope="scope">
            <span class="elegant-text" style="font-weight: 600; color: #409EFF;">{{ scope.row.lecturerName }}</span>
          </template>
        </el-table-column>

        <el-table-column label="讲师类型" align="center" prop="lecturerType" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.lecturerType === '0' ? 'primary' : 'warning'" size="small">
              {{ scope.row.lecturerType === '0' ? '内部讲师' : '外部专家' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="系统账号关联" align="center" prop="userId" width="120">
          <template slot-scope="scope">
            <span v-if="scope.row.userId" style="color: #67C23A; font-size: 12px;"><i class="el-icon-link"></i> 已绑定系统</span>
            <span v-else style="color: #ccc; font-size: 12px;">无关联</span>
          </template>
        </el-table-column>

        <el-table-column label="所属机构/部门" align="center" prop="companyName" width="150" show-overflow-tooltip />
        
        <el-table-column label="擅长领域" align="center" prop="expertiseArea" width="160" show-overflow-tooltip>
          <template slot-scope="scope">
             <span style="color: #606266; font-weight: 500;">{{ scope.row.expertiseArea || '--' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="资质凭证" align="center" prop="qualificationUrl" width="100">
          <template slot-scope="scope">
            <image-preview :src="scope.row.qualificationUrl" :width="45" :height="45" v-if="scope.row.qualificationUrl"/>
            <span v-else style="color: #ccc; font-size: 12px;">暂无凭证</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" align="center" prop="status" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" size="small" effect="dark" style="border-radius: 12px; padding: 0 10px;">
              {{ scope.row.status === '0' ? '活跃' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="简介" align="center" prop="remark" width="100">
          <template slot-scope="scope">
            <el-popover placement="left" title="讲师简介" width="300" trigger="hover" :content="scope.row.remark || '暂无简介'">
              <el-button slot="reference" type="text" size="mini" icon="el-icon-reading">查看</el-button>
            </el-popover>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:lecturer:edit']">编辑</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(scope.row)" v-hasPermi="['biz:lecturer:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" class="glass-pagination" />
    </div>

    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body custom-class="glass-dialog" :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="elegant-form">
        
        <el-divider content-position="left">身份与基础信息</el-divider>
        <el-row>
          <el-col :span="24">
            <el-form-item label="讲师类型" prop="lecturerType">
              <el-radio-group v-model="form.lecturerType" @change="handleTypeChange">
                <el-radio label="0">内部讲师 (本公司员工)</el-radio>
                <el-radio label="1">外部专家 (特聘/外包)</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="form.lecturerType === '0'">
          <el-col :span="24">
            <el-form-item label="绑定内部员工" prop="userId">
              <el-select 
                v-model="form.userId" 
                placeholder="搜索并选择系统内的员工，将自动带出姓名与部门" 
                filterable 
                clearable 
                style="width: 100%;"
                @change="handleUserChange"
              >
                <el-option v-for="user in userOptions" :key="user.userId" :label="user.nickName + ' ('+user.userName+')'" :value="user.userId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="讲师姓名" prop="lecturerName">
              <el-input v-model="form.lecturerName" placeholder="输入讲师真实姓名" :disabled="form.lecturerType === '0' && form.userId != null" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属机构" prop="companyName">
              <el-input v-model="form.companyName" placeholder="如: 质保部 或 XX培训机构" :disabled="form.lecturerType === '0' && form.userId != null" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">资质与特长</el-divider>
        <el-row>
          <el-col :span="24">
            <el-form-item label="擅长领域" prop="expertiseArea">
              <el-input v-model="form.expertiseArea" placeholder="请输入其擅长的业务领域或技术方向..." />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-form-item label="资质证明" prop="qualificationUrl">
              <image-upload v-model="form.qualificationUrl" :limit="2" />
              <div style="font-size: 12px; color: #909399; margin-top: 5px;">请上传资格证书、职称证明或聘书扫描件 (支持 JPG/PNG)</div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="合作状态" prop="status">
              <el-select v-model="form.status" style="width: 100%;">
                <el-option label="活跃/合作中" value="0" />
                <el-option label="停用/解约" value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="讲师简介" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请填写讲师的履历、过往授课经验等详细介绍..." />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="glass-btn primary" @click="submitForm">保 存 资 质</el-button>
        <el-button class="glass-btn" @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listLecturer, getLecturer, delLecturer, addLecturer, updateLecturer, deleteAllLecturer } from "@/api/biz/lecturer";
// 引入跨表用户资源
import { listUser } from "@/api/system/user";

export default {
  name: "Lecturer",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      lecturerList: [],
      title: "",
      open: false,
      
      // 全局用户库，用于内部讲师联动
      userOptions: [],

      queryParams: {
        pageNum: 1,
        pageSize: 10,
        lecturerName: null,
        lecturerType: null,
        userId: null,
        companyName: null,
        expertiseArea: null,
        status: null,
      },
      form: {
        lecturerType: '0',
        status: '0'
      },
      rules: {
        lecturerName: [{ required: true, message: "讲师姓名不能为空", trigger: "blur" }],
        lecturerType: [{ required: true, message: "必须选择讲师类型", trigger: "change" }]
      }
    };
  },
  created() {
    this.getList();
    this.getUserList();
  },
  methods: {
    getList() {
      this.loading = true;
      listLecturer(this.queryParams).then(response => {
        this.lecturerList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 获取系统用户字典 */
    getUserList() {
      listUser({ pageNum: 1, pageSize: 3000 }).then(response => {
        this.userOptions = response.rows;
      });
    },
    
    /** 【核心联动 1】：切换内外讲师时清洗数据 */
    handleTypeChange(val) {
      if (val === '1') {
        // 切换为外部，清空内部ID和部门联动
        this.form.userId = undefined;
        this.form.companyName = undefined;
        this.form.lecturerName = undefined;
      }
    },
    
    /** 【核心联动 2】：选中内部员工秒填姓名和机构 */
    handleUserChange(val) {
      if (val) {
        const user = this.userOptions.find(u => u.userId === val);
        if (user) {
          this.form.lecturerName = user.nickName;
          // 若依的用户接口如果带了 dept 对象，则直接取部门名称
          if (user.dept && user.dept.deptName) {
            this.form.companyName = user.dept.deptName;
          } else {
            this.form.companyName = "本公司内部";
          }
          this.$modal.msgSuccess("已智能提取该员工档案");
        }
      }
    },

    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        lecturerId: undefined,
        lecturerName: null,
        lecturerType: '0',
        userId: undefined,
        companyName: null,
        expertiseArea: null,
        qualificationUrl: null,
        status: '0',
        remark: null
      };
      this.resetForm("form");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.lecturerId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "录入培训讲师资质";
    },
    handleUpdate(row) {
      this.reset();
      const lecturerId = row.lecturerId || this.ids[0];
      getLecturer(lecturerId).then(response => {
        this.form = response.data;
        // 防御性赋值，锁定ID防止变新增
        this.$set(this.form, 'lecturerId', lecturerId);
        this.open = true;
        this.title = "修改讲师资质档案";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 如果是内部讲师但没有选人，给个弱提醒（但非强制）
          if (this.form.lecturerType === '0' && !this.form.userId) {
              this.$modal.msgWarning("建议为内部讲师绑定系统员工账号，以便后续考核与追溯");
          }

          if (this.form.lecturerId != null) {
            updateLecturer(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addLecturer(this.form).then(response => {
              this.$modal.msgSuccess("新增讲师成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDeleteAll() {
      this.$modal.confirm('警告！此操作将删除所有符合当前查询条件的讲师数据，且不可恢复！', '确认全部删除').then(() => {
        return deleteAllLecturer(this.queryParams);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("全部删除成功");
      }).catch(() => {});
    },
    handleDelete(row) {
      const lecturerIds = row.lecturerId || this.ids;
      this.$modal.confirm('是否确认移除选中的讲师资质信息？该操作不会删除系统员工表数据。').then(function() {
        return delLecturer(lecturerIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleExport() {
      this.download('biz/lecturer/export', {
        ...this.queryParams
      }, `培训讲师花名册_${new Date().getTime()}.xlsx`);
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
</style>