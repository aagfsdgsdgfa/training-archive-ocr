<template>
  <div class="app-container cloud-glass-container">
    <div class="glass-panel search-panel" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="90px">
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="queryParams.deviceName" placeholder="模糊搜索设备名称" clearable class="glass-input" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="设备序列号" prop="deviceSn">
          <el-input v-model="queryParams.deviceSn" placeholder="SN或MAC地址" clearable class="glass-input" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="归属部门" prop="deptId">
          <treeselect 
            v-model="queryParams.deptId" 
            :options="deptOptions" 
            :normalizer="normalizer"
            :show-count="true" 
            placeholder="选择部门" 
            class="glass-treeselect" 
            style="width: 180px;" 
          />
        </el-form-item>
        <el-form-item label="部署地点" prop="locationId">
          <el-select v-model="queryParams.locationId" placeholder="搜索部署地点" filterable clearable class="glass-input" style="width: 160px;">
            <el-option v-for="loc in locationOptions" :key="loc.locationId" :label="loc.locationName" :value="loc.locationId" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="glass-input" style="width: 120px;">
            <el-option label="在线" value="0" />
            <el-option label="离线" value="1" />
            <el-option label="故障" value="2" />
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
          <el-button type="primary" plain icon="el-icon-plus" size="mini" class="glass-btn primary-plain" @click="handleAdd" v-hasPermi="['biz:device:add']">新增设备</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="el-icon-edit" size="mini" class="glass-btn success-plain" :disabled="single" @click="handleUpdate" v-hasPermi="['biz:device:edit']">修改信息</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="el-icon-delete" size="mini" class="glass-btn danger-plain" :disabled="multiple" @click="handleDelete" v-hasPermi="['biz:device:remove']">报废设备</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" icon="el-icon-delete-solid" size="mini" class="glass-btn danger" @click="handleDeleteAll" v-hasPermi="['biz:device:remove']">全部删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="warning" plain icon="el-icon-download" size="mini" class="glass-btn warning-plain" @click="handleExport" v-hasPermi="['biz:device:export']">导出台账</el-button>
        </el-col>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="deviceList" @selection-change="handleSelectionChange" class="glass-table">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="设备ID" align="center" prop="deviceId" width="70" />
        
        <el-table-column label="设备名称" align="left" prop="deviceName" min-width="150" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="elegant-text" style="font-weight: 500;">
               <i class="el-icon-camera" style="margin-right: 5px; color: #409EFF;" v-if="scope.row.deviceType === '高拍仪'"></i>
               <i class="el-icon-cpu" style="margin-right: 5px; color: #67C23A;" v-else></i>
               {{ scope.row.deviceName }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="设备类型" align="center" prop="deviceType" width="100">
           <template slot-scope="scope">
             <el-tag size="small" type="info">{{ scope.row.deviceType || '通用硬件' }}</el-tag>
           </template>
        </el-table-column>

        <el-table-column label="SN/MAC地址" align="center" prop="deviceSn" width="160">
           <template slot-scope="scope">
             <span style="font-family: monospace; color: #909399;">{{ scope.row.deviceSn }}</span>
           </template>
        </el-table-column>

        <el-table-column label="归属部门" align="center" prop="deptId" width="120" show-overflow-tooltip>
          <template slot-scope="scope">
            <span>{{ getDeptName(scope.row.deptId) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="部署地点" align="center" prop="locationId" width="140" show-overflow-tooltip>
          <template slot-scope="scope">
            <span>{{ getLocationName(scope.row.locationId) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="网络IP" align="center" prop="ipAddress" width="120" />

        <el-table-column label="状态" align="center" prop="status" width="80">
          <template slot-scope="scope">
             <el-tag :type="getStatusType(scope.row.status)" size="small" effect="dark" style="border-radius: 12px; padding: 0 10px;">
               {{ getStatusLabel(scope.row.status) }}
             </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="最后活跃时间" align="center" prop="lastActiveTime" width="150">
          <template slot-scope="scope">
            <span style="font-size: 12px; color: #909399;">{{ parseTime(scope.row.lastActiveTime, '{y}-{m}-{d} {h}:{i}') }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:device:edit']">设置</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(scope.row)" v-hasPermi="['biz:device:remove']">报废</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" class="glass-pagination" />
    </div>

    <el-dialog :title="title" :visible.sync="open" width="650px" append-to-body custom-class="glass-dialog" :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="elegant-form">
        
        <el-divider content-position="left">基础设备档案</el-divider>
        <el-row>
          <el-col :span="12">
            <el-form-item label="设备名称" prop="deviceName">
              <el-input v-model="form.deviceName" placeholder="如: 培训室1号高拍仪" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备类型" prop="deviceType">
              <el-select v-model="form.deviceType" placeholder="请选择类型" style="width: 100%;">
                <el-option label="高拍仪" value="高拍仪" />
                <el-option label="考勤机" value="考勤机" />
                <el-option label="扫码枪" value="扫码枪" />
                <el-option label="其他终端" value="其他终端" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24">
            <el-form-item label="序列号(SN)" prop="deviceSn">
              <el-input v-model="form.deviceSn" placeholder="请输入设备机身上的序列号或MAC地址用于唯一绑定" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">网络与部署配置</el-divider>
        <el-row>
          <el-col :span="12">
            <el-form-item label="归属部门" prop="deptId">
              <treeselect 
                v-model="form.deptId" 
                :options="deptOptions" 
                :normalizer="normalizer"
                :show-count="true" 
                placeholder="指派管理部门" 
                class="glass-treeselect" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部署地点" prop="locationId">
              <el-select v-model="form.locationId" placeholder="选择摆放位置" filterable clearable style="width: 100%;">
                <el-option v-for="loc in locationOptions" :key="loc.locationId" :label="loc.locationName" :value="loc.locationId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="绑定IP地址" prop="ipAddress">
              <el-input v-model="form.ipAddress" placeholder="如: 192.168.1.100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备状态" prop="status">
              <el-select v-model="form.status" style="width: 100%;">
                <el-option label="在线 (0)" value="0" />
                <el-option label="离线 (1)" value="1" />
                <el-option label="故障检修 (2)" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="维护说明" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="填写采购日期、资产编号或维修记录..." />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="glass-btn primary" @click="submitForm">确 定 注 册</el-button>
        <el-button class="glass-btn" @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDevice, getDevice, delDevice, addDevice, updateDevice, deleteAllDevice } from "@/api/biz/device";
import { listLocation } from "@/api/biz/location";
import { deptTreeSelect } from "@/api/system/user";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "Device",
  components: { Treeselect },
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      deviceList: [],
      title: "",
      open: false,
      
      // 下拉与树形数据源
      deptOptions: [],
      locationOptions: [],

      queryParams: {
        pageNum: 1,
        pageSize: 10,
        deviceName: null,
        deviceType: null,
        deviceSn: null,
        deptId: null,
        locationId: null,
        ipAddress: null,
        status: null,
      },
      form: {
        deviceId: undefined,
        deviceType: '高拍仪',
        status: '0'
      },
      rules: {
        deviceName: [{ required: true, message: "设备名称不能为空", trigger: "blur" }],
        deviceSn: [{ required: true, message: "序列号或MAC不能为空用于底层绑定", trigger: "blur" }],
        deptId: [{ required: true, message: "必须指派归属部门", trigger: "change" }]
      }
    };
  },
  created() {
    this.getList();
    this.getDictionaries();
  },
  methods: {
    getList() {
      this.loading = true;
      listDevice(this.queryParams).then(response => {
        this.deviceList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 并发获取字典数据
    getDictionaries() {
      deptTreeSelect().then(res => {
        this.deptOptions = res.data;
      });
      listLocation({ pageNum: 1, pageSize: 500 }).then(res => {
        this.locationOptions = res.rows;
      });
    },

    /** 【核心修复】：转换部门数据结构，掐断死循环的空 children 数组 */
    normalizer(node) {
      if (node.children && !node.children.length) {
        delete node.children;
      }
      return {
        id: node.id || node.deptId,
        label: node.label || node.deptName,
        children: node.children
      };
    },

    // 辅助方法：通过部门ID递归找部门名
    getDeptName(deptId) {
      if (!deptId || !this.deptOptions || this.deptOptions.length === 0) return "--";
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

    // 辅助方法：通过地点ID找地点名
    getLocationName(locId) {
       if (!locId || !this.locationOptions.length) return "--";
       const loc = this.locationOptions.find(item => item.locationId === locId);
       return loc ? loc.locationName : locId;
    },

    // 辅助方法：状态标签 UI
    getStatusType(status) {
        const map = { '0': 'success', '1': 'info', '2': 'danger' };
        return map[status] || 'info';
    },
    getStatusLabel(status) {
        const map = { '0': '在线', '1': '离线', '2': '故障' };
        return map[status] || '未知';
    },

    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        deviceId: undefined,
        deviceName: null,
        deviceType: '高拍仪',
        deviceSn: null,
        deptId: undefined, // 重置为 undefined 而不是 null 或 ''
        locationId: null,
        ipAddress: null,
        status: '0',
        lastActiveTime: null,
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
      this.ids = selection.map(item => item.deviceId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "设备入网注册";
    },
    handleUpdate(row) {
      this.reset();
      const deviceId = row.deviceId || this.ids[0];
      getDevice(deviceId).then(response => {
        this.form = response.data;
        this.$set(this.form, 'deviceId', deviceId);
        
        // 【核心修复】：更严谨的判空，防止 ID 为 0 的合法部门被错吞掉导致回显失败
        if (this.form.deptId !== undefined && this.form.deptId !== null && this.form.deptId !== '') {
          this.$set(this.form, 'deptId', Number(this.form.deptId));
        } else {
          this.$set(this.form, 'deptId', undefined);
        }

        if (this.form.locationId) {
           this.$set(this.form, 'locationId', Number(this.form.locationId));
        }
        this.open = true;
        this.title = "更新设备网络配置";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.deviceId !== undefined && this.form.deviceId != null) {
            updateDevice(this.form).then(response => {
              this.$modal.msgSuccess("配置更新成功");
              this.open = false;
              this.getList();
            });
          } else {
            addDevice(this.form).then(response => {
              this.$modal.msgSuccess("设备注册成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDeleteAll() {
      this.$modal.confirm('警告！此操作将删除所有符合当前查询条件的设备数据，且不可恢复！', '确认全部删除').then(() => {
        return deleteAllDevice(this.queryParams);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("全部删除成功");
      }).catch(() => {});
    },
    handleDelete(row) {
      const deviceIds = row.deviceId || this.ids;
      this.$modal.confirm('危险操作！是否确认将设备作废删除？将导致关联考勤不可用！').then(function() {
        return delDevice(deviceIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("作废成功");
      }).catch(() => {});
    },
    handleExport() {
      this.download('biz/device/export', {
        ...this.queryParams
      }, `硬件设备台账_${new Date().getTime()}.xlsx`);
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

/* ================= Treeselect 部门树强力修复层级遮盖问题 ================= */
::v-deep .vue-treeselect__menu-container {
  z-index: 99999 !important; /* 强制突破弹窗遮挡 */
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