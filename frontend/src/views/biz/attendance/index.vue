<template>
  <div class="app-container cloud-glass-container">
    <div class="glass-panel search-panel" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="80px">
        <el-form-item label="关联场次" prop="sessionId">
          <el-select v-model="queryParams.sessionId" placeholder="搜索执行场次" filterable clearable class="glass-input" style="width: 180px;">
            <el-option v-for="session in sessionOptions" :key="session.sessionId" :label="session.subject" :value="session.sessionId" />
          </el-select>
        </el-form-item>
        <el-form-item label="打卡员工" prop="userId">
          <el-select v-model="queryParams.userId" placeholder="搜索员工" filterable clearable class="glass-input" style="width: 140px;">
            <el-option v-for="user in userOptions" :key="user.userId" :label="user.nickName + ' ('+user.userName+')'" :value="user.userId" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="签到序号" prop="signSeq">
          <el-input v-model="queryParams.signSeq" placeholder="搜索序号" clearable class="glass-input" style="width: 100px;" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="考核成绩" prop="scoreLevel">
          <el-select v-model="queryParams.scoreLevel" placeholder="等级筛选" clearable class="glass-input" style="width: 120px;">
            <el-option label="全部" value="" />
            <el-option label="80以上" value="above" />
            <el-option label="80以下" value="below" />
            <el-option label="空白" value="empty" />
          </el-select>
        </el-form-item>
        <el-form-item label="精确分数" prop="score">
          <el-input-number v-model="queryParams.score" controls-position="right" :min="0" :max="100" style="width: 100px;" />
        </el-form-item>

        <el-form-item label="打卡日期" prop="signTime">
          <el-date-picker v-model="queryParams.signTime" type="date" value-format="yyyy-MM-dd" placeholder="打卡日期" class="glass-input" clearable style="width: 140px;"></el-date-picker>
        </el-form-item>
        <el-form-item label="数据来源" prop="source">
          <el-select v-model="queryParams.source" placeholder="来源" clearable class="glass-input" style="width: 120px;">
            <el-option label="全部" value="" />
            <el-option label="线上打卡" value="online" />
            <el-option label="OCR扫表" value="ocr" />
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
          <el-button type="primary" plain icon="el-icon-plus" size="mini" class="glass-btn primary-plain" @click="handleAdd" v-hasPermi="['biz:attendance:add']">补录考勤</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="el-icon-edit" size="mini" class="glass-btn success-plain" :disabled="single" @click="handleUpdate" v-hasPermi="['biz:attendance:edit']">修改</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="el-icon-delete" size="mini" class="glass-btn danger-plain" :disabled="multiple" @click="handleDelete" v-hasPermi="['biz:attendance:remove']">删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" icon="el-icon-delete-solid" size="mini" class="glass-btn danger" @click="handleDeleteAll" v-hasPermi="['biz:attendance:remove']">全部删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="warning" plain icon="el-icon-download" size="mini" class="glass-btn warning-plain" @click="handleExport" v-hasPermi="['biz:attendance:export']">导出明细</el-button>
        </el-col>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="attendanceList" @selection-change="handleSelectionChange" class="glass-table">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="ID" align="center" prop="recordId" width="60" />
        
        <el-table-column label="所属培训场次" align="left" prop="sessionId" min-width="160" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="elegant-text">{{ getSessionName(scope.row.sessionId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="打卡员工" align="center" prop="userId" width="100">
          <template slot-scope="scope">
            <span style="font-weight: bold; color: #409EFF;">{{ getUserName(scope.row.userId) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="来源" align="center" prop="source" width="90">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.source === 'ocr'" type="warning" size="mini" effect="dark" style="border-radius: 12px;">OCR扫表</el-tag>
            <el-tag v-else-if="scope.row.source === 'online'" type="success" size="mini" style="border-radius: 12px;">线上打卡</el-tag>
            <span v-else style="color: #909399; font-size: 12px;">--</span>
          </template>
        </el-table-column>

        <el-table-column label="签到序号" align="center" prop="signSeq" width="80">
          <template slot-scope="scope">
            <span style="color: #909399; font-family: monospace;">{{ scope.row.signSeq || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="考核成绩" align="center" prop="score" width="110">
          <template slot-scope="scope">
            <!-- 显示实际分数，空值显示"空白" -->
            <span v-if="!scope.row.score && scope.row.score !== 0" style="color:#c0c4cc; font-style:italic;">空白</span>
            <span v-else style="font-weight:bold; color:#303133;">{{ scope.row.score }}</span>
          </template>
        </el-table-column>

        <el-table-column label="实际打卡时间" align="center" prop="signTime" width="150">
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.signTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="申诉进度" align="center" prop="appealStatus" width="100">
          <template slot-scope="scope">
            <el-tag :type="appealStatusType(scope.row.appealStatus)" size="small">
              {{ appealStatusLabel(scope.row.appealStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申诉原因" align="left" prop="appealReason" min-width="120" show-overflow-tooltip />
        <el-table-column label="佐证截图" align="center" prop="appealImage" width="90">
          <template slot-scope="scope">
            <image-preview :src="scope.row.appealImage" :width="40" :height="40" v-if="scope.row.appealImage"/>
            <span v-else style="color: #bbb; font-size: 12px;">无</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.source === 'ocr' && scope.row.sessionId && getSessionStatus(scope.row.sessionId) === '1'"
              size="mini"
              type="text"
              icon="el-icon-s-flag"
              style="color: #E6A23C; font-weight: bold;"
              @click="handleArchiveSession(scope.row)"
              v-hasPermi="['biz:attendance:edit']"
            >确认归档</el-button>

            <el-button
              v-if="scope.row.appealStatus === '1'"
              size="mini"
              type="text"
              icon="el-icon-s-check"
              style="color: #F56C6C; font-weight: bold;"
              @click="handleAudit(scope.row)"
              v-hasPermi="['biz:attendance:edit']"
            >审批处理</el-button>

            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:attendance:edit']">修改</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color:#C0C4CC" @click="handleDelete(scope.row)" v-hasPermi="['biz:attendance:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" class="glass-pagination" />
    </div>

    <el-dialog :title="title" :visible.sync="open" width="650px" append-to-body custom-class="glass-dialog" :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="elegant-form">
        
        <el-input v-model="form.recordId" v-show="false" />

        <el-row>
          <el-col :span="12">
            <el-form-item label="培训场次" prop="sessionId">
              <el-select v-model="form.sessionId" placeholder="搜索关联培训场次" filterable style="width: 100%;">
                <el-option v-for="session in sessionOptions" :key="session.sessionId" :label="session.subject" :value="session.sessionId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="打卡员工" prop="userId">
              <el-select v-model="form.userId" placeholder="搜索并选择员工" filterable style="width: 100%;">
                <el-option v-for="user in userOptions" :key="user.userId" :label="user.nickName + ' ('+user.userName+')'" :value="user.userId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">考核与成绩</el-divider>
        <el-row>
          <el-col :span="12">
            <el-form-item label="签到序号" prop="signSeq">
              <el-input v-model="form.signSeq" placeholder="如纸质表序号: 001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考核成绩" prop="score">
              <el-input v-model="form.score" placeholder="留空=未参考，填入实际分数如 85" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="打卡时间" prop="signTime">
              <el-date-picker v-model="form.signTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="选择打卡时间" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">申诉信息配置</el-divider>

        <el-form-item label="申诉状态" prop="appealStatus">
          <el-radio-group v-model="form.appealStatus">
            <el-radio label="0">无申诉</el-radio>
            <el-radio label="1">待审批</el-radio>
            <el-radio label="2">已通过(补卡)</el-radio>
            <el-radio label="3">已驳回</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="申诉原因" prop="appealReason">
          <el-input v-model="form.appealReason" type="textarea" :rows="2" placeholder="填写补卡原因说明..." />
        </el-form-item>

        <el-form-item label="佐证截图" prop="appealImage">
          <image-upload v-model="form.appealImage" :limit="1" />
        </el-form-item>

        <el-form-item label="备注说明" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="管理员批注..." />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="glass-btn primary" @click="submitForm">确 定</el-button>
        <el-button class="glass-btn" @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="考勤申诉审批" :visible.sync="auditOpen" width="500px" append-to-body custom-class="glass-dialog">
      <el-form label-width="90px" class="elegant-form">
        <el-form-item label="申请人：">
          <span style="font-weight: bold;">{{ getUserName(auditForm.userId) }}</span>
        </el-form-item>
        <el-form-item label="申诉原因：">
          <div style="background: rgba(245,247,250,0.5); padding: 10px; border-radius: 8px;">
            {{ auditForm.appealReason || '未填写原因' }}
          </div>
        </el-form-item>
        <el-form-item label="佐证截图：" v-if="auditForm.appealImage">
           <image-preview :src="auditForm.appealImage" :width="100" :height="100"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer" style="text-align: center;">
        <el-button type="success" icon="el-icon-check" @click="submitAudit(true)" style="width: 120px;">同意补卡</el-button>
        <el-button type="danger" icon="el-icon-close" @click="submitAudit(false)" style="width: 120px;">残忍驳回</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { listAttendance, getAttendance, delAttendance, addAttendance, updateAttendance, archiveSession, deleteAllAttendance } from "@/api/biz/attendance";
import { listSession } from "@/api/biz/session";
import { listUser } from "@/api/system/user";

export default {
  name: "Attendance",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      attendanceList: [],
      title: "",
      open: false,

      // 下拉库数据
      sessionOptions: [],
      userOptions: [],

      // 审批弹窗专用
      auditOpen: false,
      auditForm: {},

      queryParams: {
        pageNum: 1,
        pageSize: 10,
        sessionId: null,
        userId: null,
        // 【彻底补齐】：增加查询参数绑定
        signSeq: null,
        score: undefined,
        scoreLevel: null,
        signTime: null,
        signStatus: null,
        appealStatus: null,
      },
      form: {
        signStatus: '0',
        appealStatus: '0'
      },
      rules: {
        sessionId: [{ required: true, message: "关联执行场次不能为空", trigger: "change" }],
        userId: [{ required: true, message: "打卡员工不能为空", trigger: "change" }],
        signStatus: [{ required: true, message: "必须指定考勤状态", trigger: "change" }]
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
    getList() {
      this.loading = true;
      listAttendance(this.queryParams).then(response => {
        this.attendanceList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    getDictionaries() {
      listSession({ pageNum: 1, pageSize: 2000 }).then(response => {
        this.sessionOptions = response.rows;
      });
      listUser({ pageNum: 1, pageSize: 2000 }).then(response => {
        this.userOptions = response.rows;
      });
    },
    getSessionName(sessionId) {
      if (!sessionId) return "未知场次";
      const session = this.sessionOptions.find(item => item.sessionId === sessionId);
      return session ? session.subject : sessionId;
    },
    getUserName(userId) {
      if (!userId) return "未知员工";
      const user = this.userOptions.find(item => item.userId === userId);
      return user ? user.nickName : userId;
    },
    
    /** 【智能渲染】成绩彩色标签 */
    getScoreType(score) {
       if(!score) return 'info';
       const s = score.toString();
       if(s.includes('不') || s.includes('差')) return 'danger';
       if(s.includes('良') || s.includes('中')) return 'warning';
       return 'success';
    },
    getScoreTagType(score) {
       return this.getScoreType(score);
    },
    getScoreDisplay(score) {
       if(!score && score !== 0) return '空白';
       return score;
    },

    signStatusLabel(status) {
      const map = { '0': '正常', '1': '迟到', '2': '缺卡', '3': '免签', '4': '空白' };
      return map[status] || '未知';
    },
    signStatusType(status) {
      const map = { '0': 'success', '1': 'warning', '2': 'danger', '3': 'info', '4': 'info' };
      return map[status] || 'info';
    },
    appealStatusLabel(status) {
      const map = { '0': '无申诉', '1': '待审批', '2': '已通过', '3': '已驳回' };
      return map[status] || '未知';
    },
    appealStatusType(status) {
      const map = { '0': 'info', '1': 'warning', '2': 'success', '3': 'danger' };
      return map[status] || 'info';
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        recordId: undefined, // 保持undefined防止变新增
        sessionId: undefined,
        userId: undefined,
        // 【彻底补齐】：初始化表单加入这俩字段
        signSeq: null, 
        score: null,   
        signTime: null,
        signStatus: '0',
        appealStatus: '0',
        appealReason: null,
        appealImage: null,
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
      this.ids = selection.map(item => item.recordId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "后台补录考勤";
    },
    handleUpdate(row) {
      this.reset();
      // 【防御性修复】：确保取到准确的单行ID
      const recordId = row.recordId || this.ids[0];
      getAttendance(recordId).then(response => {
        this.form = response.data;
        
        // 【防御性修复】：强制类型转换并锁定ID
        this.$set(this.form, 'recordId', recordId);
        if (this.form.sessionId) this.$set(this.form, 'sessionId', Number(this.form.sessionId));
        if (this.form.userId) this.$set(this.form, 'userId', Number(this.form.userId));
        
        this.open = true;
        this.title = "修改考勤明细";
      });
    },
    handleAudit(row) {
      this.auditForm = Object.assign({}, row);
      this.auditOpen = true;
    },
    getSessionStatus(sessionId) {
      if (!sessionId) return null;
      const session = this.sessionOptions.find(item => item.sessionId === sessionId);
      return session ? session.status : null;
    },
    handleArchiveSession(row) {
      if (!row.sessionId) return;
      this.$modal.confirm('是否确认归档该场次？归档后将为每位员工生成培训档案并汇总到台账。')
        .then(() => {
          return archiveSession(row.sessionId);
        }).then(res => {
          this.$modal.msgSuccess(res.msg || '归档成功');
          this.getList();
        }).catch(() => {});
    },
    submitAudit(isPass) {
      if (isPass) {
        this.auditForm.appealStatus = '2';
        this.auditForm.signStatus = '0'; 
        this.auditForm.remark = "管理员于系统后台审批通过了补卡申诉。";
      } else {
        this.auditForm.appealStatus = '3';
        this.auditForm.remark = "管理员驳回了申诉申请。";
      }
      
      updateAttendance(this.auditForm).then(response => {
        this.$modal.msgSuccess(isPass ? "已同意补签请求！" : "已驳回该请求！");
        this.auditOpen = false;
        this.getList();
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 【防御性修复】：严格校验ID存在才走更新
          if (this.form.recordId !== undefined && this.form.recordId != null) {
            updateAttendance(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addAttendance(this.form).then(response => {
              this.$modal.msgSuccess("补录成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDeleteAll() {
      this.$modal.confirm('警告！此操作将删除所有符合当前查询条件的考勤数据，且不可恢复！', '确认全部删除').then(() => {
        return deleteAllAttendance(this.queryParams);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("全部删除成功");
      }).catch(() => {});
    },
    handleDelete(row) {
      const recordIds = row.recordId || this.ids;
      this.$modal.confirm('是否确认删除选中的考勤记录？').then(function() {
        return delAttendance(recordIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleExport() {
      this.download('biz/attendance/export', { ...this.queryParams }, `考勤明细_${new Date().getTime()}.xlsx`);
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
  background: #ffffff !important;
  border-color: #1890ff !important;
  box-shadow: 0 0 0 2px rgba(24, 128, 255, 0.1);
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