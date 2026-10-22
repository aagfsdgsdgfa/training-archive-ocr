<template>
  <div class="app-container cloud-glass-container dashboard-wrapper">
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <div class="glass-metric-card" @click="$router.push('train/plan')">
          <div class="icon-wrapper bg-blue"><i class="el-icon-date"></i></div>
          <div class="metric-info">
            <div class="label">年度培训计划</div>
            <div class="value">{{ stats.planCount }} <span style="font-size:14px;color:#909399">个</span></div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="glass-metric-card" @click="$router.push('train/batch')">
          <div class="icon-wrapper bg-purple"><i class="el-icon-camera-solid"></i></div>
          <div class="metric-info">
            <div class="label">OCR 智能解析批次</div>
            <div class="value">{{ stats.batchCount }} <span style="font-size:14px;color:#909399">批</span></div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="glass-metric-card" @click="$router.push('train/ledger')">
          <div class="icon-wrapper bg-green"><i class="el-icon-s-data"></i></div>
          <div class="metric-info">
            <div class="label">培训台账记录</div>
            <div class="value">{{ stats.ledgerCount }} <span style="font-size:14px;color:#909399">次</span></div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="glass-metric-card" @click="$router.push('train/archive')">
          <div class="icon-wrapper bg-orange"><i class="el-icon-user-solid"></i></div>
          <div class="metric-info">
            <div class="label">个人培训档案</div>
            <div class="value">{{ stats.archiveCount }} <span style="font-size:14px;color:#909399">份</span></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-bottom: 28px;">

      <el-col :span="7">
        <div class="glass-panel alert-panel" style="height: 380px;">
          <div class="panel-header warning-header">
            <span><i class="el-icon-message-solid icon-shake"></i> 计划执行预警</span>
            <el-tag type="danger" effect="dark" size="mini" style="border-radius: 12px;">待处理</el-tag>
          </div>
          <div class="alert-content">
            <div v-if="pendingPlans.length === 0" class="empty-state">
              <i class="el-icon-circle-check text-green" style="font-size: 40px; margin-bottom: 10px;"></i>
              <p>当前无积压或逾期的培训计划</p>
            </div>
            <div v-else class="alert-list">
              <div class="alert-item" v-for="(plan, index) in pendingPlans" :key="index" @click="$router.push('train/plan')">
                <div class="alert-type" :style="{ color: plan.isOverdue ? '#F56C6C' : (plan.isCurrentMonth ? '#E6A23C' : '#1890ff') }">
                   <span class="pulse-dot" :style="{ backgroundColor: plan.isOverdue ? '#F56C6C' : (plan.isCurrentMonth ? '#E6A23C' : '#1890ff') }"></span> 
                   {{ plan.planMonth || '-' }}
                </div>
                <div class="alert-detail">
                  <div class="subject">{{ plan.subject }}</div>
                  <div class="meta">
                    <span><i class="el-icon-office-building"></i> {{ plan.hostDeptName || '暂无部门' }}</span>
                    <el-tag :type="plan.isOverdue ? 'danger' : (plan.isCurrentMonth ? 'warning' : 'info')" size="mini" style="margin-left: 8px; border-radius: 8px;">
                      {{ plan.tagLabel }}
                    </el-tag>
                  </div>
                </div>
                <div class="alert-action"><i class="el-icon-right"></i></div>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="10">
        <div class="glass-panel" style="height: 380px;">
          <div class="panel-header">
            <span><i class="el-icon-document-copy"></i> OCR 处理动态</span>
            <el-button type="text" size="mini" class="header-btn" @click="$router.push('train/batch')">去处理</el-button>
          </div>
          <el-table :data="recentBatches" class="glass-table no-border hover-scale" size="small" height="300">
            <el-table-column label="批次号" prop="batchNo" min-width="180" show-overflow-tooltip>
              <template slot-scope="scope"><div class="text-ellipsis mono-text">{{ scope.row.batchNo }}</div></template>
            </el-table-column>
            <el-table-column label="提取主题" prop="ocrSubject" show-overflow-tooltip />
            <el-table-column label="状态" width="100" align="right">
              <template slot-scope="scope">
                <div v-if="scope.row.status === '0'" class="status-badge processing"><i class="el-icon-loading"></i> 解析中</div>
                <div v-else-if="scope.row.status === '1'" class="status-badge pending"><i class="el-icon-warning-outline"></i> 待校对</div>
                <div v-else-if="scope.row.status === '2'" class="status-badge success"><i class="el-icon-circle-check"></i> 已结算</div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <el-col :span="7">
        <div class="glass-panel" style="height: 380px; display: flex; flex-direction: column;">
          <div class="panel-header">
            <span><i class="el-icon-pie-chart"></i> 台账业务分布</span>
          </div>
          <div id="categoryChart" class="echarts-box" style="height: 200px;"></div>
        </div>
      </el-col>

    </el-row>

    <!-- 快捷入口 -->
    <el-row :gutter="20" style="margin-bottom: 28px;">
      <el-col :span="6">
        <div class="glass-metric-card quick-card" @click="$router.push('train/plan')">
          <div class="quick-icon bg-blue"><i class="el-icon-document-add"></i></div>
          <div class="quick-info">
            <div class="quick-title">新增培训计划</div>
            <div class="quick-sub">制定年度培训安排</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-metric-card quick-card" @click="goToBatch">
          <div class="quick-icon bg-green"><i class="el-icon-camera-solid"></i></div>
          <div class="quick-info">
            <div class="quick-title">高拍仪扫表</div>
            <div class="quick-sub">快速识别培训凭证</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-metric-card quick-card" @click="goToImport">
          <div class="quick-icon bg-orange"><i class="el-icon-upload2"></i></div>
          <div class="quick-info">
            <div class="quick-title">Excel 导入</div>
            <div class="quick-sub">批量导入培训计划</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="glass-metric-card quick-card" @click="$router.push('train/ledger')">
          <div class="quick-icon" style="background: #8E44AD;"><i class="el-icon-s-grid"></i></div>
          <div class="quick-info">
            <div class="quick-title">台账管理</div>
            <div class="quick-sub">查看培训执行记录</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 月度完成进度 -->
    <el-row :gutter="20" style="margin-bottom: 28px;">
      <el-col :span="16">
        <div class="glass-panel">
          <div class="panel-header"><span><i class="el-icon-s-marketing"></i> 年度月度进度</span><span class="year-label">2026年</span></div>
          <div class="month-progress-grid">
            <div class="month-item" v-for="m in 12" :key="m">
              <div class="month-name">{{ m }}月</div>
              <div class="month-bar-wrap">
                <div class="month-bar" :style="{ width: getMonthPercent(m) + '%', background: getMonthColor(m) }"></div>
              </div>
              <div class="month-num">
                <span class="done">{{ getMonthDone(m) }}</span><span class="slash">/</span><span class="total">{{ getMonthTotal(m) }}</span>
              </div>
              <div class="month-status" :class="getMonthStatusClass(m)">
                <i :class="getMonthStatusIcon(m)"></i>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="glass-panel">
          <div class="panel-header"><span><i class="el-icon-data-analysis"></i> 年度总览</span></div>
          <div class="overview-stats">
            <div class="overview-item">
              <div class="ov-value text-blue">{{ stats.planCount }}</div>
              <div class="ov-label">年度计划</div>
            </div>
            <div class="overview-item">
              <div class="ov-value text-green">{{ stats.completedCount }}</div>
              <div class="ov-label">已完成</div>
            </div>
            <div class="overview-item">
              <div class="ov-value text-orange">{{ stats.pendingPlanCount }}</div>
              <div class="ov-label">待执行</div>
            </div>
            <div class="overview-item">
              <div class="ov-value text-red">{{ stats.overdueCount }}</div>
              <div class="ov-label">已逾期</div>
            </div>
          </div>
          <div class="overall-progress">
            <div class="progress-label">
              <span>年度完成率</span>
              <span class="progress-pct">{{ getOverallPercent() }}%</span>
            </div>
            <el-progress :percentage="getOverallPercent()" :stroke-width="10" :color="progressColor" style="margin-top: 8px;" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { listPlan } from "@/api/biz/plan";
import { listLedger } from "@/api/biz/ledger";
import { listBatch } from "@/api/biz/batch";
import { listArchive } from "@/api/biz/archive"; 
import * as echarts from 'echarts';

export default {
  name: "Index",
  data() {
    return {
      stats: {
        planCount: 0,
        completedCount: 0,
        overdueCount: 0,
        pendingPlanCount: 0,
        batchCount: 0,
        pendingBatchCount: 0,
        ledgerCount: 0,
        personCount: 0,
        archiveCount: 0
      },
      currentYear: new Date().getFullYear(),
      allPlans: [],  // 存储全部计划用于月度统计
      
      pendingPlans: [],
      recentBatches: [],

      categoryChart: null,
    };
  },
  mounted() {
    this.fetchAllData();
    window.addEventListener('resize', this.resizeCharts);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts);
    if (this.categoryChart) this.categoryChart.dispose();
  },
  computed: {
    progressColor() {
      const pct = this.getOverallPercent();
      if (pct < 30) return '#F56C6C';
      if (pct < 70) return '#E6A23C';
      return '#67C23A';
    }
  },
  methods: {
    /** 解析 plan_month 字符串为 {y, m}。支持 "2026年6月" / "2026年06月" / "6月" */
    parseYearMonth(s) {
      if (!s) return { y: null, m: null }
      const m1 = /^(\d{4})年(\d{1,2})月/.exec(s)
      if (m1) return { y: Number(m1[1]), m: Number(m1[2]) }
      const m2 = /^(\d{1,2})月$/.exec(s)
      if (m2) return { y: null, m: Number(m2[1]) }
      return { y: null, m: null }
    },
    async fetchAllData() {
      try {
        // 1. 获取计划 (用于统计和智能预警)
        const planRes = await listPlan({ pageNum: 1, pageSize: 200 }); // 多拉一些数据来筛
        this.stats.planCount = planRes.total;
        
        // 【核心业务逻辑适配】：获取当前时间和月份，对应后端的 Calendar 逻辑
        const now = new Date();
        const currentYear = now.getFullYear();
        const currentMonth = now.getMonth() + 1;

        // 存储全部计划
        this.allPlans = planRes.rows;

        // 筛选出所有状态为 '0'（未完成）的计划
        const pendingAll = planRes.rows.filter(p => p.status === '0');
        this.stats.pendingPlanCount = pendingAll.length;
        this.stats.completedCount = planRes.rows.filter(p => p.status === '1').length;

        // 逾期数
        this.stats.overdueCount = pendingAll.filter(p => {
          const ym = this.parseYearMonth(p.planMonth);
          return (ym.y != null && ym.y < currentYear)
            || (ym.y === currentYear && ym.m != null && ym.m < currentMonth)
            || (ym.y == null && ym.m != null && ym.m < currentMonth);
        }).length;

        // 进一步加工数据：判断是逾期、本月待办、还是未来计划
        this.pendingPlans = pendingAll.map(plan => {
          let isOverdue = false;
          let isCurrentMonth = false;
          const ym = this.parseYearMonth(plan.planMonth);

          if ((ym.y != null && ym.y < currentYear)
              || (ym.y === currentYear && ym.m != null && ym.m < currentMonth)
              || (ym.y == null && ym.m != null && ym.m < currentMonth)) {
            isOverdue = true; // 过去的年月，已逾期
          } else if (ym.y === currentYear && ym.m === currentMonth) {
            isCurrentMonth = true; // 当前年月，本月待办
          }

          return {
            ...plan,
            isOverdue,
            isCurrentMonth,
            tagLabel: isOverdue ? '已逾期' : (isCurrentMonth ? '本月待办' : '未执行')
          };
        }).sort((a, b) => {
           // 排序优先级：已逾期(最高) > 本月待办 > 以后月份
           const aScore = a.isOverdue ? 3 : (a.isCurrentMonth ? 2 : 1);
           const bScore = b.isOverdue ? 3 : (b.isCurrentMonth ? 2 : 1);
           return bScore - aScore;
        }).slice(0, 4); // 取前4条最紧急的在首页展示

        // 2. 获取 OCR 批次
        const batchRes = await listBatch({ pageNum: 1, pageSize: 20 });
        this.stats.batchCount = batchRes.total;
        this.stats.pendingBatchCount = batchRes.rows.filter(b => b.status === '1').length;
        this.recentBatches = batchRes.rows.slice(0, 6);

        // 3. 获取台账
        const ledgerRes = await listLedger({ pageNum: 1, pageSize: 100 });
        this.stats.ledgerCount = ledgerRes.total;

        let totalPersons = 0;
        let categoryMap = {};
        ledgerRes.rows.forEach(item => {
          totalPersons += (item.actualCount || 0);
          const cat = (item.projectCategory && item.projectCategory.trim()) || '未分类';
          categoryMap[cat] = (categoryMap[cat] || 0) + 1;
        });
        this.stats.personCount = totalPersons;

        // 4. 获取档案数
        try {
            const archiveRes = await listArchive({ pageNum: 1, pageSize: 1 });
            this.stats.archiveCount = archiveRes.total;
        } catch (e) {
            this.stats.archiveCount = totalPersons; // 兜底
        }

        // 5. 渲染图表
        this.$nextTick(() => {
          this.initCategoryChart(categoryMap);
        });

      } catch (error) {
        console.error("首页数据拉取异常:", error);
      }
    },

    initCategoryChart(categoryMap) {
      const chartDom = document.getElementById('categoryChart');
      if (!chartDom) return;
      this.categoryChart = echarts.init(chartDom);
      
      const data = Object.keys(categoryMap).map(key => {
        return { name: key, value: categoryMap[key] };
      });
      if (data.length === 0) data.push({ name: '暂无台账', value: 0 });

      const option = {
        tooltip: { trigger: 'item', formatter: '{b}: {c} 条 ({d}%)' },
        legend: {
          orient: 'horizontal',
          bottom: 0,
          left: 'center',
          icon: 'circle',
          itemWidth: 8,
          itemHeight: 8,
          textStyle: { fontSize: 12 }
        },
        color: ['#1890ff', '#67C23A', '#E6A23C', '#F56C6C', '#8E44AD'],
        series: [
          {
            name: '培训分类',
            type: 'pie',
            radius: ['38%', '62%'],
            center: ['50%', '45%'],
            avoidLabelOverlap: true,
            itemStyle: {
              borderRadius: 8,
              borderColor: 'rgba(255,255,255,0.8)',
              borderWidth: 2
            },
            label: {
              show: true,
              formatter: '{b}\n{c}条 ({d}%)',
              fontSize: 11,
              lineHeight: 14
            },
            labelLine: { show: true, length: 8, length2: 6 },
            data: data
          }
        ]
      };
      this.categoryChart.setOption(option);
    },

    resizeCharts() {
      if (this.categoryChart) this.categoryChart.resize();
    },

    // 快捷跳转
    goToBatch() {
      this.$router.push('train/batch');
    },
    goToImport() {
      this.$router.push('train/plan');
      this.$nextTick(() => {
        this.$message.info('请在扫描计划表弹窗中选择「本地文件选取」上传 Excel');
      });
    },

    // 月度进度辅助
    getMonthTotal(month) {
      return this.allPlans.filter(p => {
        const ym = this.parseYearMonth(p.planMonth);
        return ym.m === month && (ym.y == null || String(ym.y) === String(this.currentYear));
      }).length;
    },
    getMonthDone(month) {
      return this.allPlans.filter(p => {
        const ym = this.parseYearMonth(p.planMonth);
        return ym.m === month && (ym.y == null || String(ym.y) === String(this.currentYear)) && p.status === '1';
      }).length;
    },
    getMonthPercent(month) {
      const total = this.getMonthTotal(month);
      if (total === 0) return 0;
      return Math.round((this.getMonthDone(month) / total) * 100);
    },
    getMonthColor(month) {
      const pct = this.getMonthPercent(month);
      if (pct === 0) return '#e4e7ed';
      if (pct < 50) return '#E6A23C';
      if (pct < 100) return '#1890ff';
      return '#67C23A';
    },
    getMonthStatusClass(month) {
      const total = this.getMonthTotal(month);
      if (total === 0) return 'status-none';
      const done = this.getMonthDone(month);
      const now = new Date();
      const isPast = (now.getFullYear() > this.currentYear || (now.getFullYear() === this.currentYear && now.getMonth() + 1 > month));
      if (done === total) return 'status-done';
      if (isPast && done < total) return 'status-overdue';
      return 'status-pending';
    },
    getMonthStatusIcon(month) {
      const cls = this.getMonthStatusClass(month);
      if (cls === 'status-done') return 'el-icon-check';
      if (cls === 'status-overdue') return 'el-icon-warning';
      if (cls === 'status-pending') return 'el-icon-time';
      return 'el-icon-minus';
    },

    // 年度总览
    getOverallPercent() {
      const total = this.stats.planCount;
      if (total === 0) return 0;
      return Math.round((this.stats.completedCount / total) * 100);
    },
  }
};
</script>

<style scoped>
/* ================= 全局与面板 ================= */
.dashboard-wrapper {
  padding-top: 15px;
}
.cloud-glass-container {
  min-height: calc(100vh - 84px);
  background: #f0f2f5;
  position: relative;
  z-index: 1;
}
.glass-panel {
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  padding: 20px;
  transition: all 0.3s ease;
}
.glass-panel:hover { box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); }

.panel-header {
  font-size: 16px; font-weight: 600; color: #303133; margin-bottom: 15px;
  padding-bottom: 10px; border-bottom: 1px solid rgba(0,0,0,0.05);
  display: flex; justify-content: space-between; align-items: center;
}
.panel-header i { margin-right: 6px; color: #1890ff; font-size: 18px; }
.header-btn { padding: 0; font-weight: bold; }


/* ================= 预警列表区域 ================= */
.warning-header i { color: #F56C6C !important; }
.icon-shake { animation: shake 2s infinite; }
@keyframes shake {
  0%, 100% { transform: rotate(0); }
  10%, 30%, 50%, 70%, 90% { transform: rotate(-10deg); }
  20%, 40%, 60%, 80% { transform: rotate(10deg); }
}

.alert-list { display: flex; flex-direction: column; gap: 12px; }
.alert-item {
  display: flex; align-items: center; padding: 12px; background: rgba(255,255,255,0.5);
  border: 1px solid rgba(0, 0, 0, 0.05); border-radius: 10px; cursor: pointer;
  transition: all 0.2s;
}
.alert-item:hover { background: rgba(245, 108, 108, 0.05); border-color: rgba(245, 108, 108, 0.3); transform: translateX(3px); }
.alert-type { width: 80px; font-size: 13px; font-weight: bold; display: flex; align-items: center; }
.pulse-dot { width: 6px; height: 6px; border-radius: 50%; margin-right: 6px; animation: pulse 1.5s infinite; }
@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(0,0,0,0.4); }
  70% { box-shadow: 0 0 0 6px rgba(0,0,0,0); }
  100% { box-shadow: 0 0 0 0 rgba(0,0,0,0); }
}
.alert-detail { flex: 1; overflow: hidden; }
.alert-detail .subject { font-size: 14px; font-weight: bold; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 4px; }
.alert-detail .meta { font-size: 12px; color: #909399; }
.alert-action { color: #C0C4CC; }
.empty-state { text-align: center; color: #909399; margin-top: 60px; font-size: 14px; }

/* ================= OCR 批次动态表格 ================= */
::v-deep .glass-table { background: transparent !important; }
::v-deep .glass-table th, ::v-deep .glass-table tr, ::v-deep .glass-table td { background: transparent !important; border-bottom: 1px solid rgba(0, 0, 0, 0.03); }
::v-deep .glass-table th { background: rgba(245, 247, 250, 0.4) !important; color: #606266; font-weight: bold; }
::v-deep .glass-table::before { display: none; }
::v-deep .no-border td { border-bottom: none !important; }
::v-deep .hover-scale tr { transition: background-color 0.2s; cursor: pointer; }
::v-deep .hover-scale tr:hover > td { background-color: rgba(64,158,255,0.05) !important; }

.mono-text { font-family: monospace; color: #909399; }
.status-badge { display: inline-block; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: bold; }
.status-badge.processing { background: rgba(24, 128, 255, 0.1); color: #1890ff; }
.status-badge.processing i { animation: rotating 2s linear infinite; }
.status-badge.pending { background: rgba(230, 162, 60, 0.1); color: #E6A23C; }
.status-badge.success { background: rgba(103, 194, 58, 0.1); color: #67C23A; }
@keyframes rotating { from { transform: rotate(0) } to { transform: rotate(360deg) } }

/* ================= 右侧台账简报 ================= */
.mini-title { font-size: 13px; font-weight: bold; color: #909399; margin-bottom: 8px; padding-bottom: 5px; border-bottom: 1px dashed #ebeef5; }
.mini-item { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid rgba(0,0,0,0.02); min-width: 0; }
.mini-item:last-child { border-bottom: none; }
.mini-subject { font-size: 13px; color: #303133; min-width: 0; }
.text-ellipsis { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.mini-count { font-size: 12px; color: #909399; font-weight: bold; flex-shrink: 0; }

/* 文本颜色工具类 */
.text-blue { color: #1890ff !important; }
.text-orange { color: #E6A23C !important; }
.text-green { color: #67C23A !important; }
.text-red { color: #F56C6C !important; }

/* ================= 图标与按钮颜色 ================= */
.icon-wrapper {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 26px;
  margin-right: 14px;
  color: #fff;
  flex-shrink: 0;
}
.icon-wrapper i { color: #fff; }
.bg-blue { background: #1890ff; }
.bg-purple { background: #8E44AD; }
.bg-green { background: #67C23A; }
.bg-orange { background: #E6A23C; }

/* ================= 顶部统计卡片 ================= */
.glass-metric-card {
  border: 1px solid rgba(255,255,255,0.9);
  border-radius: 12px;
  background: rgba(255,255,255,0.7);
  padding: 18px 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  height: 90px;
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
}
.glass-metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}
.glass-metric-card .metric-info .label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
  font-weight: bold;
}
.glass-metric-card .metric-info .value {
  font-size: 22px;
  font-weight: 900;
  color: #303133;
}

/* ================= 快捷入口卡片 ================= */
.quick-card {
  cursor: pointer;
  height: 80px;
  padding: 0;
  overflow: hidden;
  align-items: stretch !important;
}
.quick-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.1) !important;
}
.quick-icon {
  width: 52px;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 26px;
  color: #fff;
  flex-shrink: 0;
  border-radius: 12px 0 0 12px;
}
.quick-icon i { color: #fff; }
.quick-info {
  flex: 1;
  padding: 16px 14px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.quick-title {
  font-size: 14px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}
.quick-sub {
  font-size: 12px;
  color: #909399;
}

/* ================= 月度进度面板 ================= */
.year-label {
  font-size: 13px;
  font-weight: normal;
  color: #909399;
  margin-left: 8px;
}
.month-progress-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 6px;
  margin-top: 5px;
}
.month-item {
  text-align: center;
  background: #fafafa;
  border-radius: 8px;
  padding: 8px 4px;
  border: 1px solid #f0f2f5;
  transition: all 0.2s;
}
.month-item:hover {
  background: #f0f7ff;
  border-color: rgba(24,144,255,0.2);
}
.month-name {
  font-size: 12px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 6px;
}
.month-bar-wrap {
  height: 6px;
  background: #f0f2f5;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 4px;
}
.month-bar {
  height: 100%;
  border-radius: 3px;
  transition: width 0.5s ease;
}
.month-num {
  font-size: 11px;
  color: #909399;
}
.month-num .done { font-weight: 700; color: #303133; }
.month-num .slash { margin: 0 1px; }
.month-num .total {}
.month-status {
  margin-top: 3px;
  font-size: 12px;
}
.month-status i { font-size: 12px; }
.status-done i { color: #67C23A; }
.status-overdue i { color: #F56C6C; }
.status-pending i { color: #E6A23C; }
.status-none i { color: #c0c4cc; }

/* 年度总览 */
.overview-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 16px;
}
.overview-item {
  text-align: center;
}
.ov-value {
  font-size: 28px;
  font-weight: 900;
  line-height: 1;
  margin-bottom: 4px;
}
.ov-label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}
.overall-progress {
  background: #fafafa;
  border-radius: 8px;
  padding: 12px;
}
.progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}
.progress-pct {
  font-weight: 700;
  color: #303133;
}

</style>