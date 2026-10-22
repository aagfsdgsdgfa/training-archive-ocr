<template>
  <div class="app-container cloud-glass-container">
    <div class="glass-panel search-panel" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="100px">
        <el-form-item label="关联年度计划" prop="planId">
          <el-select
            v-model="queryParams.planId"
            placeholder="搜索年度计划"
            filterable
            clearable
            class="glass-input"
            style="width: 200px;"
          >
            <el-option
              v-for="plan in planOptions"
              :key="plan.planId"
              :label="formatPlanLabel(plan)"
              :value="plan.planId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="培训主题" prop="subject">
          <el-input v-model="queryParams.subject" placeholder="模糊搜索培训主题" clearable class="glass-input" @keyup.enter.native="handleQuery"/>
        </el-form-item>
        <el-form-item label="主办部门" prop="hostDeptId">
          <treeselect
            v-model="queryParams.hostDeptId"
            :options="deptOptions"
            :show-count="true"
            placeholder="请选择主办部门"
            class="glass-treeselect"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="培训日期" prop="trainDate">
          <el-date-picker 
            v-model="queryParams.trainDate" 
            type="date" 
            value-format="yyyy-MM-dd" 
            placeholder="选择培训日期" 
            class="glass-input" 
            clearable 
          />
        </el-form-item>
        <el-form-item label="场次状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable class="glass-input" style="width: 150px;">
            <el-option label="未开始" value="0" />
            <el-option label="进行中" value="1" />
            <el-option label="待审归档" value="2" />
            <el-option label="已归档" value="3" />
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
          <el-button type="primary" plain icon="el-icon-plus" size="mini" class="glass-btn primary-plain" @click="handleAdd" v-hasPermi="['biz:session:add']">新增场次</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="el-icon-edit" size="mini" class="glass-btn success-plain" :disabled="single" @click="handleUpdate" v-hasPermi="['biz:session:edit']">修改</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="el-icon-delete" size="mini" class="glass-btn danger-plain" :disabled="multiple" @click="handleDelete" v-hasPermi="['biz:session:remove']">删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" icon="el-icon-delete-solid" size="mini" class="glass-btn danger" @click="handleDeleteAll" v-hasPermi="['biz:session:remove']">全部删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="warning" plain icon="el-icon-download" size="mini" class="glass-btn warning-plain" @click="handleExport" v-hasPermi="['biz:session:export']">导出</el-button>
        </el-col>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="sessionList" @selection-change="handleSelectionChange" class="glass-table">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="编号" align="center" prop="sessionId" width="60" />
        <el-table-column label="培训主题" align="left" prop="subject" min-width="160" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="elegant-text">{{ scope.row.subject }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="培训内容大纲" align="left" prop="trainContent" min-width="180" show-overflow-tooltip />

        <el-table-column label="主办部门" align="center" prop="hostDeptName" width="130">
          <template slot-scope="scope">
            <span>{{ scope.row.hostDeptName || getDeptName(scope.row.hostDeptId) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="授课人" align="center" prop="speaker" width="90" />
        <el-table-column label="培训日期" align="center" prop="trainDate" width="200" show-overflow-tooltip>
          <template slot-scope="scope">
            <span>{{ formatOcrDate(scope.row.trainDate) || scope.row.trainDate || '' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="学时" align="center" prop="trainHours" width="60">
          <template slot-scope="scope">
            <el-tag type="info" size="mini">{{ scope.row.trainHours }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="防飞检凭证" align="center" prop="attachmentUrl" width="100">
          <template slot-scope="scope">
            <image-preview :src="scope.row.attachmentUrl" :width="45" :height="45" v-if="scope.row.attachmentUrl"/>
            <span v-else style="color: #999; font-size: 12px;">无凭证</span>
          </template>
        </el-table-column>

        <el-table-column label="考勤方式" align="center" prop="signType" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.signType === '0' ? 'primary' : 'success'" size="small">
              {{ scope.row.signType === '0' ? '线上打卡' : '纸质扫表' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" prop="status" width="90">
          <template slot-scope="scope">
            <el-tag :type="statusTypeFilter(scope.row.status)" size="small">
              {{ statusLabelFilter(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="190" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:session:edit']">修改</el-button>
            <el-button 
              v-if="scope.row.status === '2'"
              size="mini" 
              type="text" 
              icon="el-icon-folder-checked" 
              style="color: #E6A23C; font-weight: bold;" 
              @click="handleArchive(scope.row)" 
              v-hasPermi="['biz:session:archive']"
            >归档结算</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(scope.row)" v-hasPermi="['biz:session:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" class="glass-pagination" />
    </div>

    <el-dialog :title="title" :visible.sync="open" width="750px" append-to-body custom-class="glass-dialog" :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="elegant-form">
        
        <el-form-item label="关联年度计划" prop="planId">
          <el-select
            v-model="form.planId"
            placeholder="支持搜索绑定的年度计划（选择后自动带入信息）"
            filterable
            clearable
            style="width: 100%;"
            @change="handlePlanChange"
          >
            <el-option
              v-for="plan in planOptions"
              :key="plan.planId"
              :label="formatPlanLabel(plan)"
              :value="plan.planId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="培训主题" prop="subject">
          <el-input v-model="form.subject" placeholder="请输入培训场次具体主题（标题）" />
        </el-form-item>

        <el-form-item label="培训内容" prop="trainContent">
          <el-input v-model="form.trainContent" type="textarea" :rows="3" placeholder="请输入详细的培训内容大纲，此数据将同步至个人档案和台账..." />
        </el-form-item>

        <el-row>
          <el-col :span="12">
            <el-form-item label="主办部门" prop="hostDeptId">
              <treeselect 
                v-model="form.hostDeptId" 
                :options="deptOptions" 
                :show-count="true" 
                :append-to-body="true" 
                placeholder="请选择主办部门" 
                class="glass-treeselect" 
                @select="handleDeptSelect" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="授课人" prop="speaker">
              <el-input v-model="form.speaker" placeholder="请输入授课人姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="培训地点" prop="locationName">
              <el-select v-model="form.locationName" placeholder="请选择或输入地点" filterable allow-create style="width: 100%;">
                <el-option v-for="loc in locationOptions" :key="loc.locationId" :label="loc.locationName" :value="loc.locationName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="培训学时" prop="trainHours">
              <el-input-number v-model="form.trainHours" :min="0.5" :step="0.5" placeholder="学时" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="8">
            <el-form-item label="培训日期" prop="trainDate" label-width="90px">
              <el-date-picker v-model="form.trainDate" type="date" value-format="yyyy-MM-dd" placeholder="培训日期" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="开始时间" prop="startTime" label-width="90px">
              <el-date-picker v-model="form.startTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" format="HH:mm" placeholder="开始时间" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="结束时间" prop="endTime" label-width="90px">
              <el-date-picker v-model="form.endTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" format="HH:mm" placeholder="结束时间" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="考勤方式" prop="signType">
              <el-radio-group v-model="form.signType">
                <el-radio label="0">线上手机打卡</el-radio>
                <el-radio label="1">纸质扫表OCR</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="场次状态" prop="status">
              <el-select v-model="form.status" style="width: 100%;">
                <el-option label="0 - 未开始" value="0" />
                <el-option label="1 - 进行中" value="1" />
                <el-option label="2 - 待审归档" value="2" />
                <el-option label="3 - 已归档 (锁表)" value="3" disabled />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="防飞检凭证" prop="attachmentUrl">
          <image-upload v-model="form.attachmentUrl" :limit="1" />
        </el-form-item>

        <el-form-item label="备注说明" prop="remark">
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
import { listSession, getSession, delSession, addSession, updateSession, archiveSession, deleteAllSession } from "@/api/biz/session";
import { listPlan } from "@/api/biz/plan";
import { listLocation } from "@/api/biz/location";
import { deptTreeSelect } from "@/api/system/user";
import { formatOcrDate } from "@/utils/ocr-date";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "Session",
  components: { Treeselect },
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      sessionList: [],
      title: "",
      open: false,

      planOptions: [],
      deptOptions: [],
      locationOptions: [],

      queryParams: {
        pageNum: 1,
        pageSize: 10,
        planId: null,
        subject: null,
        hostDeptId: null,
        status: null,
      },
      form: {
        signType: '0',
        status: '0',
        trainHours: 1
      },
      rules: {
        subject: [{ required: true, message: "培训主题不能为空", trigger: "blur" }],
        trainContent: [{ required: true, message: "培训内容不能为空", trigger: "blur" }],
        hostDeptId: [{ required: true, message: "主办部门不能为空", trigger: "change" }],
        trainDate: [{ required: true, message: "培训日期不能为空", trigger: "change" }]
      }
    };
  },
  created() {
    this.getList();
    this.getDictionaries();
  },
  activated() {
    // 缓存页面切回时无条件刷新,确保拿到最新数据
    this.getList();
  },
  methods: {
    formatOcrDate,
    getList() {
      this.loading = true;
      listSession(this.queryParams).then(response => {
        this.sessionList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    getDictionaries() {
      deptTreeSelect().then(response => {
        this.deptOptions = response.data;
      });
      listPlan({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.planOptions = response.rows || [];
      });
      listLocation({ pageNum: 1, pageSize: 500 }).then(response => {
        this.locationOptions = response.rows;
      });
    },
    formatPlanLabel(plan) {
      if (!plan) return '';
      const subject = plan.subject || ('#' + plan.planId);
      if (!plan.planMonth) return subject;
      const dateStr = /^\d{4}年/.test(plan.planMonth)
        ? plan.planMonth
        : (plan.planYear ? plan.planYear + '年' + plan.planMonth : plan.planMonth);
      return subject + ' (' + dateStr + ')';
    },
    getDeptName(deptId) {
      if (!deptId || !this.deptOptions || this.deptOptions.length === 0) return deptId;
      let deptName = deptId;
      const searchTree = (nodes) => {
        for (let node of nodes) {
          if (node.id === deptId) {
            deptName = node.label;
            return true;
          }
          if (node.children && searchTree(node.children)) return true;
        }
        return false;
      };
      searchTree(this.deptOptions);
      return deptName;
    },
    handlePlanChange(val) {
      if(val) {
        const selectedPlan = this.planOptions.find(p => p.planId === val);
        if(selectedPlan) {
          this.form.subject = selectedPlan.subject;
          this.form.hostDeptId = Number(selectedPlan.hostDeptId);
          this.form.hostDeptName = selectedPlan.hostDeptName;
          this.$modal.msgSuccess("已自动为您填入计划相关信息");
        }
      }
    },
    handleDeptSelect(node) {
      this.form.hostDeptName = node.label;
    },
    handleArchive(row) {
      const sessionId = row.sessionId;
      const subject = row.subject;
      this.$modal.confirm('确认要将培训场次《' + subject + '》进行一键归档吗？系统将自动汇总考勤生成台账和个人档案！').then(function() {
        return archiveSession(sessionId);
      }).then(() => {
        this.getList(); 
        this.$modal.msgSuccess("归档结算成功！已生成档案与台账。");
      }).catch(() => {});
    },
    statusLabelFilter(status) {
      const map = { '0': '未开始', '1': '进行中', '2': '待审归档', '3': '已归档' };
      return map[status] || '未知';
    },
    statusTypeFilter(status) {
      const map = { '0': 'info', '1': 'primary', '2': 'warning', '3': 'success' };
      return map[status] || 'info';
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        sessionId: null,
        planId: null,
        subject: null,
        trainContent: null,
        hostDeptId: null,
        hostDeptName: null,
        speaker: null,
        trainHours: 1,
        locationName: null,
        trainDate: null,
        startTime: null,
        endTime: null,
        signType: '0',
        attachmentUrl: null,
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
      this.ids = selection.map(item => item.sessionId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新增培训执行场次";
    },
    handleUpdate(row) {
      this.reset();
      const sessionId = row.sessionId || this.ids[0];
      getSession(sessionId).then(response => {
        this.form = response.data;
        this.form.sessionId = sessionId;
        if (this.form.hostDeptId) {
          this.form.hostDeptId = Number(this.form.hostDeptId);
        } else {
          this.form.hostDeptId = undefined;
        }
        this.open = true;
        this.title = "修改培训执行场次";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.sessionId != null) {
            updateSession(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addSession(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDeleteAll() {
      this.$modal.confirm('警告！此操作将删除所有符合当前查询条件的场次数据，且不可恢复！', '确认全部删除').then(() => {
        return deleteAllSession(this.queryParams);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("全部删除成功");
      }).catch(() => {});
    },
    handleDelete(row) {
      const sessionIds = row.sessionId || this.ids;
      this.$modal.confirm('是否确认删除该培训场次？').then(function() {
        return delSession(sessionIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleExport() {
      this.download('biz/session/export', { ...this.queryParams }, `session_${new Date().getTime()}.xlsx`);
    }
  }
};
</script>

<style scoped>
.cloud-glass-container {
  min-height: calc(100vh - 84px);
  background: #f0f2f5;
  position: relative;
  z-index: 1;
  padding: 20px;
}

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