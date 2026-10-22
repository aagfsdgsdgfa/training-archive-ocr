<template>
  <div class="app-container cloud-glass-container">
    <div class="glass-panel search-panel" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="100px">
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="queryParams.courseName" placeholder="模糊搜索课程名称" clearable class="glass-input" style="width: 200px" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="项目分类" prop="projectCategory">
          <el-select v-model="queryParams.projectCategory" placeholder="请选择或输入分类" filterable allow-create clearable class="glass-input" style="width: 180px">
            <el-option label="安全生产" value="安全生产" />
            <el-option label="质量体系" value="质量体系" />
            <el-option label="新员工入职" value="新员工入职" />
            <el-option label="技能提升" value="技能提升" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="课程状态" clearable class="glass-input" style="width: 120px;">
            <el-option label="启用" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
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
          <el-button type="primary" plain icon="el-icon-plus" size="mini" class="glass-btn primary-plain" @click="handleAdd" v-hasPermi="['biz:course:add']">建立新课程</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="el-icon-edit" size="mini" class="glass-btn success-plain" :disabled="single" @click="handleUpdate" v-hasPermi="['biz:course:edit']">修改</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="el-icon-delete" size="mini" class="glass-btn danger-plain" :disabled="multiple" @click="handleDelete" v-hasPermi="['biz:course:remove']">删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" icon="el-icon-delete-solid" size="mini" class="glass-btn danger" @click="handleDeleteAll" v-hasPermi="['biz:course:remove']">全部删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="warning" plain icon="el-icon-download" size="mini" class="glass-btn warning-plain" @click="handleExport" v-hasPermi="['biz:course:export']">导出目录</el-button>
        </el-col>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="courseList" @selection-change="handleSelectionChange" class="glass-table">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="课程编号" align="center" prop="courseId" width="80" />
        
        <el-table-column label="标准课程名称" align="left" prop="courseName" min-width="180" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="elegant-text" style="font-weight: bold; color: #409EFF;">{{ scope.row.courseName }}</span>
          </template>
        </el-table-column>

        <el-table-column label="项目分类" align="center" prop="projectCategory" width="130">
          <template slot-scope="scope">
            <el-tag size="small" type="info">{{ scope.row.projectCategory || '通用' }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="标准学时" align="center" prop="defaultHours" width="80">
           <template slot-scope="scope">
             <span style="font-weight: 600;">{{ scope.row.defaultHours }}</span> h
           </template>
        </el-table-column>

        <el-table-column label="大纲与内容介绍" align="center" prop="courseSyllabus" width="150">
          <template slot-scope="scope">
            <el-popover placement="left" title="课程大纲明细" width="350" trigger="hover" :content="scope.row.courseSyllabus || '暂无大纲说明'">
              <el-button slot="reference" type="text" size="mini" icon="el-icon-reading">查看大纲</el-button>
            </el-popover>
          </template>
        </el-table-column>

        <el-table-column label="课件/附件" align="center" prop="coursewareUrl" width="100">
          <template slot-scope="scope">
            <el-tooltip content="点击预览或下载课件" placement="top" v-if="scope.row.coursewareUrl">
              <el-link type="primary" :href="scope.row.coursewareUrl" target="_blank" :underline="false">
                <i class="el-icon-document" style="font-size: 18px;"></i>
              </el-link>
            </el-tooltip>
            <span v-else style="color: #ccc; font-size: 12px;">无附件</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" align="center" prop="status" width="90">
          <template slot-scope="scope">
             <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" size="small">
               {{ scope.row.status === '0' ? '启用中' : '已停用' }}
             </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:course:edit']">编辑</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(scope.row)" v-hasPermi="['biz:course:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" class="glass-pagination" />
    </div>

    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body custom-class="glass-dialog" :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="elegant-form">
        
        <el-row>
          <el-col :span="24">
            <el-form-item label="课程名称" prop="courseName">
              <el-input v-model="form.courseName" placeholder="必填，输入标准的课程名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="项目分类" prop="projectCategory">
              <el-select v-model="form.projectCategory" placeholder="请选择或输入" filterable allow-create clearable style="width: 100%;">
                <el-option label="安全生产" value="安全生产" />
                <el-option label="质量体系" value="质量体系" />
                <el-option label="新员工入职" value="新员工入职" />
                <el-option label="技能提升" value="技能提升" />
                <el-option label="企业文化" value="企业文化" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="默认学时" prop="defaultHours">
              <el-input-number v-model="form.defaultHours" :min="0.5" :step="0.5" placeholder="如: 2.0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="课程大纲" prop="courseSyllabus">
          <el-input v-model="form.courseSyllabus" type="textarea" :rows="4" placeholder="请填写该课程的核心内容介绍与大纲明细..." />
        </el-form-item>

        <el-row>
          <el-col :span="12">
            <el-form-item label="课程状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="0">正常启用</el-radio>
                <el-radio label="1">暂停使用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="课件附件" prop="coursewareUrl">
          <file-upload v-model="form.coursewareUrl" :limit="3" :fileType="['pdf', 'doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'zip', 'rar']" />
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">支持上传 PPT, PDF, Word 或视频压缩包等课件资料</div>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选填附加说明..." />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="glass-btn primary" @click="submitForm">确 定</el-button>
        <el-button class="glass-btn" @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCourse, getCourse, delCourse, addCourse, updateCourse, deleteAllCourse } from "@/api/biz/course";

export default {
  name: "Course",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      courseList: [],
      title: "",
      open: false,
      
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        courseName: null,
        projectCategory: null,
        status: null,
      },
      form: {
        defaultHours: 1,
        status: '0'
      },
      rules: {
        courseName: [{ required: true, message: "标准课程名称不能为空", trigger: "blur" }],
        projectCategory: [{ required: true, message: "请选择或输入项目分类", trigger: "change" }]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listCourse(this.queryParams).then(response => {
        this.courseList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        courseId: null,
        courseName: null,
        projectCategory: null,
        defaultHours: 1,
        courseSyllabus: null,
        coursewareUrl: null,
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
      this.ids = selection.map(item => item.courseId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "建立标准课程";
    },
    handleUpdate(row) {
      this.reset();
      const courseId = row.courseId || this.ids;
      getCourse(courseId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改标准课程";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.courseId != null) {
            updateCourse(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addCourse(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDeleteAll() {
      this.$modal.confirm('警告！此操作将删除所有符合当前查询条件的课程数据，且不可恢复！', '确认全部删除').then(() => {
        return deleteAllCourse(this.queryParams);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("全部删除成功");
      }).catch(() => {});
    },
    handleDelete(row) {
      const courseIds = row.courseId || this.ids;
      this.$modal.confirm('危险！是否确认删除选中的课程资料？此操作不可逆。').then(function() {
        return delCourse(courseIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleExport() {
      this.download('biz/course/export', {
        ...this.queryParams
      }, `标准课程库_${new Date().getTime()}.xlsx`);
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
</style>