<template>
  <div class="app-container cloud-glass-container">
    <div class="glass-panel search-panel" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="90px">
        <el-form-item label="归属员工" prop="userId">
          <el-select v-model="queryParams.userId" placeholder="支持姓名/工号搜索" filterable clearable class="glass-input" style="width: 180px;">
            <el-option v-for="user in userOptions" :key="user.userId" :label="user.nickName + ' ('+user.userName+')'" :value="user.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="归属部门" prop="deptId">
          <treeselect
            v-model="queryParams.deptId"
            :options="deptOptions"
            :show-count="true"
            :append-to-body="true"
            placeholder="选择部门"
            class="glass-treeselect"
            style="width: 180px;"
            :normalizer="deptNormalizer"
          />
        </el-form-item>
        <el-form-item label="关联计划" prop="planId">
          <el-select v-model="queryParams.planId" placeholder="搜索关联计划" filterable clearable class="glass-input" style="width: 160px;">
            <el-option v-for="plan in planOptions" :key="plan.planId" :label="plan.subject" :value="plan.planId" />
          </el-select>
        </el-form-item>
        <el-form-item label="培训主题" prop="subject">
          <el-input v-model="queryParams.subject" placeholder="模糊搜索培训主题" clearable class="glass-input" @keyup.enter.native="handleQuery"/>
        </el-form-item>
        <el-form-item label="培训日期" prop="trainDateRange">
          <el-date-picker
            v-model="queryParams.trainDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            class="glass-input"
            clearable
            style="width: 260px;"
          />
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
          <el-button type="primary" plain icon="el-icon-plus" size="mini" class="glass-btn primary-plain" @click="handleAdd" v-hasPermi="['biz:archive:add']">手工补录档案</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="el-icon-edit" size="mini" class="glass-btn success-plain" :disabled="single" @click="handleUpdate" v-hasPermi="['biz:archive:edit']">修改</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="el-icon-delete" size="mini" class="glass-btn danger-plain" :disabled="multiple" @click="handleDelete" v-hasPermi="['biz:archive:remove']">删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" icon="el-icon-delete-solid" size="mini" class="glass-btn danger" @click="handleDeleteAll" v-hasPermi="['biz:archive:remove']">全部删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="warning" icon="el-icon-printer" size="mini" class="glass-btn primary" style="background:#e6a23c;color:white;border:none;" @click="handleExportUserArchive" v-hasPermi="['biz:archive:export']">打印员工个人档案 (Excel)</el-button>
        </el-col>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="archiveList" @selection-change="handleSelectionChange" class="glass-table">
        <el-table-column type="selection" width="50" align="center" />
        <!-- 1. 员工姓名 -->
        <el-table-column label="员工姓名" align="center" prop="userName" width="100">
          <template slot-scope="scope">
            <span style="font-weight: bold; color: #409EFF;">{{ scope.row.userName }}</span>
          </template>
        </el-table-column>
        <!-- 2. 培训日期 -->
        <el-table-column label="培训日期" align="center" prop="trainDate" width="200" show-overflow-tooltip>
          <template slot-scope="scope">
             <span>{{ displayTrainDate(scope.row.trainDate) || scope.row.trainDate || '' }}</span>
             <el-tag v-if="scope.row.remark && /日期无法识别/.test(scope.row.remark)" type="danger" size="mini" effect="plain" style="margin-left: 4px;">待复核</el-tag>
          </template>
        </el-table-column>
        <!-- 3. 培训部门 -->
        <el-table-column label="培训部门" align="center" prop="trainDept" width="120" show-overflow-tooltip />
        <!-- 4. 培训地点 -->
        <el-table-column label="培训地点" align="center" prop="trainLocation" width="180" show-overflow-tooltip />
        <!-- 5. 培训内容 -->
        <el-table-column label="培训内容" align="left" prop="subject" min-width="180" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="elegant-text">{{ scope.row.subject }}</span>
          </template>
        </el-table-column>
        <!-- 6. 签到序号 -->
        <el-table-column label="签到序号" align="center" prop="signSeq" width="80" />
        <!-- 7. 授课人 -->
        <el-table-column label="授课人" align="center" prop="speaker" width="90" />
        <!-- 8. 成绩 -->
        <el-table-column label="成绩" align="center" prop="score" width="100">
          <template slot-scope="scope">
            <el-tag :type="getScoreType(scope.row.score)" size="mini" effect="dark" style="border-radius: 12px; padding: 0 10px;">
              {{ scope.row.score || '暂无' }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 9. 培训学时 -->
        <el-table-column label="培训学时" align="center" prop="trainHours" width="80" />
        <!-- 10. 备注 -->
        <el-table-column label="备注" align="left" prop="remark" min-width="160" show-overflow-tooltip />
        <!-- 操作列固定右侧 -->
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:archive:edit']">编辑</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(scope.row)" v-hasPermi="['biz:archive:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" class="glass-pagination" />
    </div>

    <el-dialog :title="title" :visible.sync="open" width="800px" append-to-body custom-class="glass-dialog" :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="elegant-form">
        
        <el-divider content-position="left" style="color: #409EFF;">① 员工身份信息锁定</el-divider>
        <el-row>
          <el-col :span="12">
            <el-form-item label="归属员工" prop="userId">
              <el-select v-model="form.userId" placeholder="支持搜索并选定员工" filterable clearable style="width: 100%;" @change="handleUserChange">
                <el-option v-for="user in userOptions" :key="user.userId" :label="user.nickName + ' ('+user.userName+')'" :value="user.userId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="员工姓名" prop="userName">
              <el-input v-model="form.userName" disabled placeholder="选择员工后自动带出" style="background-color: #f5f7fa;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="归属部门" prop="deptId">
              <treeselect
                v-model="form.deptId"
                :options="deptOptions"
                :show-count="true"
                :append-to-body="true"
                placeholder="请选择部门"
                class="glass-treeselect"
                :normalizer="deptNormalizer"
                @select="handleDeptSelect"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门名称" prop="deptName">
              <el-input v-model="form.deptName" disabled placeholder="选择部门后自动带出" style="background-color: #f5f7fa;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">② 培训溯源与内容自动带出</el-divider>
        <el-row>
          <el-col :span="12">
            <el-form-item label="关联年度计划" prop="planId">
              <el-select v-model="form.planId" placeholder="选填，可溯源至年度计划" filterable clearable style="width: 100%;" @change="handlePlanChange">
                <el-option v-for="plan in planOptions" :key="plan.planId" :label="plan.subject" :value="plan.planId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联场次" prop="sessionId">
              <el-select v-model="form.sessionId" placeholder="选择场次可秒填下方信息！" filterable clearable style="width: 100%;" @change="handleSessionChange">
                <el-option v-for="session in sessionOptions" :key="session.sessionId" :label="session.subject" :value="session.sessionId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-form-item label="培训主题" prop="subject">
              <el-input v-model="form.subject" placeholder="必填，本次培训的简短核心主题" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="培训内容大纲" prop="trainContent">
              <el-input v-model="form.trainContent" type="textarea" :rows="3" placeholder="请填写详细的培训内容大纲..." />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="培训部门" prop="trainDept">
              <el-input v-model="form.trainDept" placeholder="本次培训主办方" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="授课人" prop="speaker">
              <el-input v-model="form.speaker" placeholder="授课人姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="培训日期" prop="trainDate">
              <el-date-picker v-model="form.trainDate" type="date" value-format="yyyy-MM-dd" placeholder="落档日期" style="width: 100%;"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="培训地点" prop="trainLocation">
              <el-input v-model="form.trainLocation" placeholder="如: 一楼会议室" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">③ 考核与变动</el-divider>
        <el-row>
          <el-col :span="8">
            <el-form-item label="培训学时" prop="trainHours">
              <el-input-number v-model="form.trainHours" :min="0" :step="0.5" placeholder="如: 2.0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="考核成绩" prop="score">
              <el-input v-model="form.score" placeholder="分数或合格" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="签到序号" prop="signSeq">
              <el-input v-model="form.signSeq" placeholder="如: 001" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="岗位变化" prop="jobChange">
          <el-input v-model="form.jobChange" placeholder="若培训导致岗位晋升或调动，请在此说明..." />
        </el-form-item>

        <el-form-item label="备注说明" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="附加说明..." />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="glass-btn primary" @click="submitForm">保 存 档 案</el-button>
        <el-button class="glass-btn" @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="选择需要打印档案的员工" :visible.sync="exportOpen" width="500px" append-to-body custom-class="glass-dialog">
      <div style="text-align: center; margin-bottom: 20px; color: #909399; font-size: 13px;">
        系统将自动提取该员工历年所有的培训记录，<br/>并生成按年份划分 Sheet 的标准个人培训档案。
      </div>
      <el-form label-width="100px">
        <el-form-item label="指定员工">
          <el-select v-model="exportUserId" placeholder="支持姓名/工号搜索" filterable style="width: 100%;">
            <el-option v-for="user in userOptions" :key="user.userId" :label="user.nickName + ' ('+user.userName+')'" :value="user.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="培训日期范围">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-date-picker
              v-model="exportDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="yyyy-MM-dd"
              style="flex: 1;"
            />
            <el-link
              :type="exportDateRange === null ? 'success' : 'primary'"
              :underline="false"
              :icon="exportDateRange === null ? 'el-icon-success' : 'el-icon-refresh-left'"
              @click="exportDateRange = null"
              style="font-size: 13px; white-space: nowrap;">
              {{ exportDateRange === null ? '全部日期' : '还原为全部' }}
            </el-link>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer" style="text-align: center;">
        <el-button type="primary" icon="el-icon-printer" style="width:150px;background:#1890ff;border:none;" @click="submitExportTemplate">生 成 并 下 载</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { listArchive, getArchive, delArchive, addArchive, updateArchive, deleteAllArchive } from "@/api/biz/archive";
import { listPlan } from "@/api/biz/plan";
import { listSession } from "@/api/biz/session";
import { listUser } from "@/api/system/user";
import { listDept } from "@/api/system/dept";
import { normalizeOcrDate, formatOcrDate } from "@/utils/ocr-date";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "Archive",
  components: { Treeselect },
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      archiveList: [],
      title: "",
      open: false,

      // 导出相关变量
      exportOpen: false,
      exportUserId: null,
      exportDateRange: null,

      // 超级字典库
      planOptions: [],
      sessionOptions: [],
      userOptions: [],
      deptOptions: [],

      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        deptId: null,
        userName: null,
        deptName: null,
        planId: null,
        subject: null,
        beginTrainDate: null,
        endTrainDate: null,
      },
      form: {
        trainHours: 1
      },
      rules: {
        userId: [{ required: true, message: "必须指定归属员工", trigger: "change" }],
        subject: [{ required: true, message: "培训主题不能为空", trigger: "blur" }]
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
  computed: {
    displayTrainDate() {
      return (raw) => {
        return formatOcrDate(raw) || '';
      };
    }
  },
  methods: {
    getList() {
      this.loading = true;
      listArchive(this.queryParams).then(response => {
        this.archiveList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    getDictionaries() {
      listPlan({ pageNum: 1, pageSize: 2000 }).then(res => this.planOptions = res.rows);
      listSession({ pageNum: 1, pageSize: 2000 }).then(res => this.sessionOptions = res.rows);
      listUser({ pageNum: 1, pageSize: 3000 }).then(res => this.userOptions = res.rows);
      listDept().then(response => {
        this.deptOptions = this.handleTree(response.data, 'deptId');
      });
    },

    handleUserChange(val) {
      if (val) {
        const user = this.userOptions.find(u => u.userId === val);
        if (user) {
          this.form.userName = user.nickName;
          if(user.deptId) {
             this.form.deptId = user.deptId;
             this.form.deptName = user.dept ? user.dept.deptName : this.getDeptName(user.deptId);
          }
        }
      }
    },
    handleDeptSelect(node) {
      this.form.deptName = node.label;
    },
    
    handlePlanChange(val) {
      if (val) {
        const plan = this.planOptions.find(p => p.planId === val);
        if (plan) {
          if(!this.form.subject) this.form.subject = plan.subject;
        }
      }
    },

    /** 【智能分离版】：场次联动时，主题和内容各回各家 */
    handleSessionChange(val) {
      if(val) {
        const session = this.sessionOptions.find(s => s.sessionId === val);
        if(session) {
          // 主题赋值给 subject
          this.form.subject = session.subject;
          // 内容赋值给 trainContent
          this.form.trainContent = session.trainContent;
          
          this.form.trainDept = session.hostDeptName;
          this.form.speaker = session.speaker;
          this.form.trainHours = session.trainHours;
          this.form.trainLocation = session.locationName;
          
          if (session.planId) {
             this.form.planId = session.planId;
          }
          if (session.trainDate) {
            this.form.trainDate = String(session.trainDate).split(' ')[0];
          }
        }
      }
    },

    getDeptName(deptId) {
      if (!deptId || !this.deptOptions || this.deptOptions.length === 0) return "";
      let deptName = "";
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

    /** 部门树字段转换：deptId → id, deptName → label */
    deptNormalizer(node) {
      if (node.children && !node.children.length) {
        delete node.children;
      }
      return {
        id: node.deptId,
        label: node.deptName,
        children: node.children
      };
    },

    getScoreType(score) {
       if(!score) return 'info';
       const s = score.toString();
       if(s.includes('不') || s.includes('差')) return 'danger';
       if(s.includes('良') || s.includes('中')) return 'warning';
       return 'success';
    },

    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        archiveId: null,
        userId: null,
        deptId: null,
        userName: null,
        deptName: null,
        planId: null,
        ledgerId: null,
        trainDate: null,
        trainDept: null,
        trainLocation: null,
        subject: null,
        trainContent: null, // 初始化内容
        signSeq: null,      // 初始化签到序号
        speaker: null,
        score: null,
        trainHours: 1,
        jobChange: null,
        remark: null
      };
      this.resetForm("form");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      if (this.queryParams.trainDateRange && this.queryParams.trainDateRange.length === 2) {
        this.queryParams.beginTrainDate = this.queryParams.trainDateRange[0];
        this.queryParams.endTrainDate = this.queryParams.trainDateRange[1];
      } else {
        this.queryParams.beginTrainDate = null;
        this.queryParams.endTrainDate = null;
      }
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.queryParams.trainDateRange = null;
      this.queryParams.beginTrainDate = null;
      this.queryParams.endTrainDate = null;
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.archiveId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "手工建立个人培训档案";
    },
    handleUpdate(row) {
      this.reset();
      const archiveId = row.archiveId || this.ids;
      getArchive(archiveId).then(response => {
        this.form = response.data;
        // 如果deptId有值但deptName为空，尝试获取部门名称
        if (this.form.deptId && !this.form.deptName) {
          this.form.deptName = this.getDeptName(this.form.deptId);
        }
        this.open = true;
        this.title = "精修个人培训档案";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.archiveId != null) {
            updateArchive(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addArchive(this.form).then(response => {
              this.$modal.msgSuccess("个人档案落档成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDeleteAll() {
      this.$modal.confirm('警告！此操作将删除所有符合当前查询条件的档案数据，且不可恢复！', '确认全部删除').then(() => {
        return deleteAllArchive(this.queryParams);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("全部删除成功");
      }).catch(() => {});
    },
    handleDelete(row) {
      const archiveIds = row.archiveId || this.ids;
      this.$modal.confirm('危险！是否确认删除选中的个人培训档案？').then(function() {
        return delArchive(archiveIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    
    handleExportUserArchive() {
       if(this.queryParams.userId) {
           this.exportUserId = this.queryParams.userId;
       } else {
           this.exportUserId = null;
       }
       this.exportOpen = true;
    },
    
    submitExportTemplate() {
       if(!this.exportUserId) {
           this.$modal.msgError("请先选择一位需要导出档案的员工");
           return;
       }
       const params = {
         userId: this.exportUserId
       };
       if (this.exportDateRange && this.exportDateRange.length === 2) {
         params.beginTrainDate = this.exportDateRange[0];
         params.endTrainDate = this.exportDateRange[1];
       }
       this.$modal.confirm('即将查询该员工历史记录，并生成按年份划分Sheet的规范档案，是否继续？').then(() => {
        this.download('biz/archive/exportTemplate', params, `个人培训档案_${new Date().getTime()}.xlsx`);
        this.exportOpen = false;
      }).catch(() => {});
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
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.9);
}
.glass-btn.primary { background: #1890ff; color: white; }
.glass-btn.primary:hover { background: #40a9ff; box-shadow: 0 2px 8px rgba(24, 128, 255, 0.3); }

.glass-btn.primary-plain { color: #1890ff; background: rgba(24, 128, 255, 0.06); border: 1px solid rgba(24, 128, 255, 0.2); }
.glass-btn.success-plain { color: #67C23A; background: rgba(103, 194, 58, 0.06); border: 1px solid rgba(103, 194, 58, 0.2); }
.glass-btn.warning-plain { color: #E6A23C; background: rgba(230, 162, 60, 0.06); border: 1px solid rgba(230, 162, 60, 0.2); }
.glass-btn.danger-plain { color: #F56C6C; background: rgba(245, 108, 108, 0.06); border: 1px solid rgba(245, 108, 108, 0.2); }

/* ================= 表格透明毛玻璃化 ================= */
::v-deep .glass-table {
  background: transparent !important;
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