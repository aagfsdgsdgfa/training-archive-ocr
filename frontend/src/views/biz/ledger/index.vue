<template>
  <div class="app-container cloud-glass-container">
    <div class="glass-panel search-panel" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="90px">
        <el-form-item label="溯源计划" prop="planId">
          <el-select v-model="queryParams.planId" placeholder="搜索并筛选计划" filterable clearable class="glass-input" style="width: 180px;">
            <el-option v-for="plan in planOptions" :key="plan.planId" :label="plan.subject" :value="plan.planId" />
          </el-select>
        </el-form-item>
        <el-form-item label="溯源场次" prop="sessionId">
          <el-select v-model="queryParams.sessionId" placeholder="搜索并筛选场次" filterable clearable class="glass-input" style="width: 180px;">
            <el-option v-for="session in sessionOptions" :key="session.sessionId" :label="session.subject" :value="session.sessionId" />
          </el-select>
        </el-form-item>
        <el-form-item label="培训主题" prop="subject">
          <el-input v-model="queryParams.subject" placeholder="模糊搜索培训主题" clearable class="glass-input" @keyup.enter.native="handleQuery"/>
        </el-form-item>
        <el-form-item label="培训部门" prop="hostDeptName">
          <el-input v-model="queryParams.hostDeptName" placeholder="输入部门名称" clearable class="glass-input" @keyup.enter.native="handleQuery"/>
        </el-form-item>
        <el-form-item label="培训日期" prop="trainDate">
          <el-date-picker
            v-model="queryParams.trainDate"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            @input="handleQuery"
            style="width: 240px;"
          />
        </el-form-item>
        <el-form-item label="计划类型" prop="planType">
          <el-select v-model="queryParams.planType" placeholder="请选择" clearable class="glass-input" style="width: 120px;">
            <el-option label="计划内" value="计划内" />
            <el-option label="计划外" value="计划外" />
          </el-select>
        </el-form-item>
        <el-form-item label="培训级别" prop="trainLevel">
          <el-select v-model="queryParams.trainLevel" placeholder="请选择" clearable class="glass-input" style="width: 120px;">
            <el-option label="公司级" value="公司级" />
            <el-option label="部门级" value="部门级" />
            <el-option label="班组级" value="班组级" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目分类" prop="projectCategory">
          <el-select v-model="queryParams.projectCategory" placeholder="请选择" clearable class="glass-input" style="width: 140px;">
            <el-option label="转岗培训" value="转岗培训" />
            <el-option label="岗前培训" value="岗前培训" />
            <el-option label="GMP文件培训" value="GMP文件培训" />
            <el-option label="规章制度培训" value="规章制度培训" />
            <el-option label="专业知识培训" value="专业知识培训" />
            <el-option label="新员工入职培训" value="新员工入职培训" />
            <el-option label="安全教育培训" value="安全教育培训" />
            <el-option label="法律法规培训" value="法律法规培训" />
            <el-option label="通用素质提升培训" value="通用素质提升培训" />
            <el-option label="心理健康讲座" value="心理健康讲座" />
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
          <el-button type="primary" plain icon="el-icon-plus" size="mini" class="glass-btn primary-plain" @click="handleAdd" v-hasPermi="['biz:ledger:add']">手工录入台账</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="el-icon-edit" size="mini" class="glass-btn success-plain" :disabled="single" @click="handleUpdate" v-hasPermi="['biz:ledger:edit']">修改</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="el-icon-delete" size="mini" class="glass-btn danger-plain" :disabled="multiple" @click="handleDelete" v-hasPermi="['biz:ledger:remove']">删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" icon="el-icon-delete-solid" size="mini" class="glass-btn danger" @click="handleDeleteAll" v-hasPermi="['biz:ledger:remove']">全部删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="warning" icon="el-icon-download" size="mini" class="glass-btn primary" style="background:#e6a23c;color:white;border:none;" @click="handleExportTemplate" v-hasPermi="['biz:ledger:export']">导出台账</el-button>
        </el-col>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="ledgerList" @selection-change="handleSelectionChange" class="glass-table">
        <el-table-column type="selection" width="50" align="center" />
        <!-- 1. 序号:跨页连续行号,按当前列表顺序(trainDate 序)自增 -->
        <el-table-column type="index" label="序号" align="center" width="50" :index="indexMethod" />
        <!-- 2. 培训部门 -->
        <el-table-column label="培训部门" align="center" prop="hostDeptName" width="120" show-overflow-tooltip />
        <!-- 3. 项目分类 -->
        <el-table-column label="项目分类" align="center" prop="projectCategory" width="120" show-overflow-tooltip>
          <template slot-scope="scope">
            <el-tag type="info" size="mini">{{ scope.row.projectCategory }}</el-tag>
          </template>
        </el-table-column>
        <!-- 4. 培训主题 (放开宽度,完整显示,不再截断) -->
        <el-table-column label="培训主题" align="left" prop="subject" min-width="280">
          <template slot-scope="scope">
            <span class="elegant-text">{{ scope.row.subject }}</span>
          </template>
        </el-table-column>
        <!-- 5. 培训日期 -->
        <el-table-column label="培训日期" align="center" prop="trainDate" width="200" show-overflow-tooltip>
          <template slot-scope="scope">
             <span>{{ formatOcrDate(scope.row.trainDate) || scope.row.trainDate || '' }}</span>
          </template>
        </el-table-column>
        <!-- 6. 培训学时 (原"学时") -->
        <el-table-column label="培训学时" align="center" prop="trainHours" width="60" />
        <!-- 7. 主讲人 (原"授课人") -->
        <el-table-column label="主讲人" align="center" prop="speaker" width="90" />
        <!-- 8. 培训地点 (新增) -->
        <el-table-column label="培训地点" align="center" prop="locationName" width="120" show-overflow-tooltip />
        <!-- 9. 参加部门/人员 (原"参加人员名单") -->
        <el-table-column label="参加部门/人员" align="center" prop="attendeeNames" width="150" show-overflow-tooltip>
          <template slot-scope="scope">
            <el-popover placement="left" title="参训人员明细" width="300" trigger="hover" :content="scope.row.attendeeNames || '无名单记录'">
              <el-button slot="reference" type="text" size="mini" icon="el-icon-user">查看名单</el-button>
            </el-popover>
          </template>
        </el-table-column>
        <!-- 10. 应参加培训人数 (从原"参训情况"拆分) -->
        <el-table-column label="应参加培训人数" align="center" prop="targetCount" width="90">
          <template slot-scope="scope">
            <span style="color: #909399;">{{ scope.row.targetCount || 0 }}</span> 人
          </template>
        </el-table-column>
        <!-- 11. 实际参加培训人数 (从原"参训情况"拆分) -->
        <el-table-column label="实际参加培训人数" align="center" prop="actualCount" width="90">
          <template slot-scope="scope">
            <span style="color: #67C23A; font-weight: bold;">{{ scope.row.actualCount || 0 }}</span> 人
          </template>
        </el-table-column>
        <!-- 12. 计划情况 -->
        <el-table-column label="计划情况" align="center" prop="planType" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.planType === '计划内' ? 'success' : 'warning'" size="mini">{{ scope.row.planType }}</el-tag>
          </template>
        </el-table-column>
        <!-- 13. 级别 -->
        <el-table-column label="级别" align="center" prop="trainLevel" width="80">
          <template slot-scope="scope">
            <el-tag type="info" size="mini">{{ scope.row.trainLevel }}</el-tag>
          </template>
        </el-table-column>
        <!-- 14. 备注 (新增,需先在后端域加 remark 字段) -->
        <el-table-column label="备注" align="left" prop="remark" min-width="100" show-overflow-tooltip />
        <!-- 凭证附件列已移除 (用户反馈:列表不展示附件,附件保留在编辑/详情里) -->
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="120">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:ledger:edit']">编辑</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(scope.row)" v-hasPermi="['biz:ledger:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" class="glass-pagination" />
    </div>

    <el-dialog :title="title" :visible.sync="open" width="680px" append-to-body custom-class="glass-dialog" :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="elegant-form">

        <el-form-item label="台账流号" v-if="form.serialNo">
          <el-input v-model="form.serialNo" readonly placeholder="系统自动生成" />
        </el-form-item>

        <el-form-item label="关联场次" prop="sessionId">
          <el-select v-model="form.sessionId" placeholder="搜索并选择已执行的场次，自动填充下方信息" filterable clearable style="width: 100%;" @change="handleSessionChange">
            <el-option v-for="session in sessionOptions" :key="session.sessionId" :label="session.subject" :value="session.sessionId" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">台账核心档案</el-divider>

        <el-row>
          <el-col :span="24">
            <el-form-item label="培训主题" prop="subject">
              <el-input v-model="form.subject" placeholder="填写本次培训的正式主题名称" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="主办部门" prop="hostDeptName">
              <el-input v-model="form.hostDeptName" placeholder="如：质保部" />
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
              <el-date-picker v-model="form.trainDate" type="date" value-format="yyyy-MM-dd" placeholder="选择日期" style="width: 100%;"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="培训学时" prop="trainHours">
              <el-input-number v-model="form.trainHours" :min="0.5" :step="0.5" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="8">
            <el-form-item label="项目分类" prop="projectCategory">
              <el-select v-model="form.projectCategory" placeholder="请选择" style="width: 100%;">
                <el-option label="转岗培训" value="转岗培训" />
                <el-option label="岗前培训" value="岗前培训" />
                <el-option label="GMP文件培训" value="GMP文件培训" />
                <el-option label="规章制度培训" value="规章制度培训" />
                <el-option label="专业知识培训" value="专业知识培训" />
                <el-option label="新员工入职培训" value="新员工入职培训" />
                <el-option label="安全教育培训" value="安全教育培训" />
                <el-option label="法律法规培训" value="法律法规培训" />
                <el-option label="通用素质提升培训" value="通用素质提升培训" />
                <el-option label="心理健康讲座" value="心理健康讲座" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="计划类型" prop="planType">
              <el-select v-model="form.planType" placeholder="请选择" style="width: 100%;">
                <el-option label="计划内" value="计划内" />
                <el-option label="计划外" value="计划外" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="培训级别" prop="trainLevel">
              <el-select v-model="form.trainLevel" placeholder="请选择" style="width: 100%;">
                <el-option label="公司级" value="公司级" />
                <el-option label="部门级" value="部门级" />
                <el-option label="班组级" value="班组级" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">参训统计</el-divider>

        <el-row>
          <el-col :span="12">
            <el-form-item label="应参加人数">
              <el-input-number v-model="form.targetCount" :min="0" :max="9999" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际到场">
              <el-input-number v-model="form.actualCount" :min="0" :max="9999" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="参训名单">
          <el-input v-model="form.attendeeNames" type="textarea" :rows="2" placeholder="由系统自动填充，可手动修正" />
        </el-form-item>

        <el-divider content-position="left">归档留痕</el-divider>

        <el-form-item label="归档凭证">
          <image-upload v-model="form.attachmentUrl" :limit="3" />
        </el-form-item>

        <el-form-item label="备注说明">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="附加说明（选填）" maxlength="200" show-word-limit />
        </el-form-item>

      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="glass-btn primary" @click="submitForm">确 定 归 档</el-button>
        <el-button class="glass-btn" @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listLedger, getLedger, delLedger, addLedger, updateLedger, deleteAllLedger } from "@/api/biz/ledger";
import { listPlan } from "@/api/biz/plan";
import { listSession } from "@/api/biz/session";
import { listUser, deptTreeSelect } from "@/api/system/user";
import { formatOcrDate } from "@/utils/ocr-date";

export default {
  name: "Ledger",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      ledgerList: [],
      title: "",
      open: false,
      planOptions: [],
      sessionOptions: [],
      userOptions: [],
      deptOptions: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        planId: null,
        sessionId: null,
        hostDeptName: null,
        subject: null,
        trainDate: null,
        planType: null,
        trainLevel: null,
        projectCategory: null,
      },
      form: {
        targetCount: 0,
        actualCount: 0,
        trainHours: 1,
        planType: null,
        trainLevel: null
      },
      rules: {
        subject: [{ required: true, message: "培训主题不能为空", trigger: "blur" }],
        hostDeptName: [{ required: true, message: "培训部门不能为空", trigger: "blur" }]
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
      const params = {
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
        planId: this.queryParams.planId,
        sessionId: this.queryParams.sessionId,
        hostDeptName: this.queryParams.hostDeptName,
        subject: this.queryParams.subject,
        planType: this.queryParams.planType,
        trainLevel: this.queryParams.trainLevel,
        projectCategory: this.queryParams.projectCategory,
        beginTrainDate: this.queryParams.trainDate ? this.queryParams.trainDate[0] : undefined,
        endTrainDate: this.queryParams.trainDate ? this.queryParams.trainDate[1] : undefined
      };
      listLedger(params).then(response => {
        this.ledgerList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    getDictionaries() {
      listPlan({ pageNum: 1, pageSize: 2000 }).then(res => this.planOptions = res.rows);
      listSession({ pageNum: 1, pageSize: 2000 }).then(res => this.sessionOptions = res.rows);
      listUser({ pageNum: 1, pageSize: 3000 }).then(res => this.userOptions = res.rows);
      deptTreeSelect().then(res => this.deptOptions = res.data);
    },
    handleSessionChange(val) {
      if(val) {
        const session = this.sessionOptions.find(s => s.sessionId === val);
        if(session) {
          this.form.subject = session.subject;
          this.form.hostDeptName = session.hostDeptName;
          this.form.speaker = session.speaker;
          this.form.trainHours = session.trainHours;
          this.form.locationName = session.locationName;
          this.form.planId = session.planId;
          if (session.trainDate) {
            this.form.trainDate = String(session.trainDate).split(' ')[0];
          }
          
          if (session.planId) {
            const plan = this.planOptions.find(p => p.planId === session.planId);
            if (plan) {
                this.form.planType = plan.planType === '1' ? '计划内' : '计划外';
                this.form.projectCategory = plan.projectCategory;
                this.form.trainLevel = plan.trainLevel;
                
                this.form.attendeeNames = this.resolveAttendeeNames(plan);
                if (plan.audienceScope === '2' && plan.audienceIds) {
                    this.form.targetCount = plan.audienceIds.split(',').length;
                }
            }
          }
          this.$modal.msgSuccess("✨ 魔法注入！已从计划表中为您翻译出真实名单并填入表单。");
        }
      }
    },
    resolveAttendeeNames(plan) {
        if (!plan || !plan.audienceScope) return '未绑定受众计划';
        if (plan.audienceScope === '0') return '全体员工';
        if (!plan.audienceIds) return '无具体名单记录';

        const ids = plan.audienceIds.split(',');
        if (plan.audienceScope === '1') {
            const names = ids.map(id => this.getDeptName(Number(id))).filter(n => n);
            return '参与部门: ' + names.join('、');
        } else if (plan.audienceScope === '2') {
            const names = ids.map(id => this.getUserName(Number(id))).filter(n => n);
            return names.join('、');
        }
        return '';
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
    getUserName(userId) {
      if (!userId) return null;
      const user = this.userOptions.find(item => item.userId === userId);
      return user ? user.nickName : null;
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        ledgerId: null,
        serialNo: null,
        serialDate: null,
        planId: null,
        sessionId: null,
        hostDeptName: null,
        projectCategory: null,
        subject: null,
        trainDate: null,
        trainHours: 1,
        speaker: null,
        locationName: null,
        attendeeNames: null,
        targetCount: 0,
        actualCount: 0,
        planType: null,
        trainLevel: null,
        attachmentUrl: null,
        remark: null
      };
      this.resetForm("form");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      const params = {
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
        planId: this.queryParams.planId,
        sessionId: this.queryParams.sessionId,
        hostDeptName: this.queryParams.hostDeptName,
        subject: this.queryParams.subject,
        planType: this.queryParams.planType,
        trainLevel: this.queryParams.trainLevel,
        projectCategory: this.queryParams.projectCategory,
        beginTrainDate: this.queryParams.trainDate ? this.queryParams.trainDate[0] : undefined,
        endTrainDate: this.queryParams.trainDate ? this.queryParams.trainDate[1] : undefined
      };
      listLedger(params).then(response => {
        this.ledgerList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.ledgerId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    indexMethod(index) {
      return (this.queryParams.pageNum - 1) * this.queryParams.pageSize + index + 1;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "手工建立台账记录";
    },
    handleUpdate(row) {
      this.reset();
      const ledgerId = row.ledgerId || this.ids;
      getLedger(ledgerId).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "精修台账明细";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.ledgerId != null) {
            updateLedger(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addLedger(this.form).then(response => {
              const sn = response.serialNo || response.data && response.data.serialNo;
              this.$modal.msgSuccess(sn ? "台账落档成功，流水号：" + sn : "台账落档成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDeleteAll() {
      this.$modal.confirm('警告！此操作将删除所有符合当前查询条件的台账数据，且不可恢复！', '确认全部删除').then(() => {
        return deleteAllLedger(this.queryParams);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("全部删除成功");
      }).catch(() => {});
    },
    handleDelete(row) {
      const ledgerIds = row.ledgerId || this.ids;
      this.$modal.confirm('危险！是否确认删除选中的台账报表记录？').then(function() {
        return delLedger(ledgerIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleExportTemplate() {
      this.$modal.confirm('即将渲染并下载Excel报表，是否继续？').then(() => {
        const trainDateRange = this.queryParams.trainDate;
        const params = {
          planId: this.queryParams.planId || null,
          sessionId: this.queryParams.sessionId || null,
          hostDeptName: this.queryParams.hostDeptName || null,
          subject: this.queryParams.subject || null,
          planType: this.queryParams.planType || null,
          trainLevel: this.queryParams.trainLevel || null,
          beginTrainDate: trainDateRange && trainDateRange[0] ? trainDateRange[0] : null,
          endTrainDate: trainDateRange && trainDateRange[1] ? trainDateRange[1] : null,
          trainDate: null
        };
        this.download('biz/ledger/exportTemplate', params, `台账规范汇总_${new Date().getTime()}.xlsx`);
      }).catch(() => {});
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
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.05);
  padding: 20px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}
.glass-panel:hover {
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.08);
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
.elegant-text {
  font-weight: 500;
  color: #303133;
}
::v-deep .glass-dialog {
  background: rgba(255, 255, 255, 0.85) !important;
  border-radius: 16px !important;
  box-shadow: 0 15px 35px rgba(0,0,0,0.1) !important;
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