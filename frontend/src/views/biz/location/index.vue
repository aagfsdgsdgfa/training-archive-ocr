<template>
  <div class="app-container cloud-glass-container">
    <div class="glass-panel search-panel" v-show="showSearch">
      <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="90px">
        <el-form-item label="地点名称" prop="locationName">
          <el-input v-model="queryParams.locationName" placeholder="模糊搜索会议室/地点" clearable class="glass-input" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="容纳人数 >=" prop="capacity">
          <el-input-number v-model="queryParams.capacity" :min="1" placeholder="最小人数要求" clearable class="glass-input" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="会议室状态" clearable class="glass-input" style="width: 120px;">
            <el-option label="正常可用" value="0" />
            <el-option label="维护停用" value="1" />
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
          <el-button type="primary" plain icon="el-icon-plus" size="mini" class="glass-btn primary-plain" @click="handleAdd" v-hasPermi="['biz:location:add']">新增会议室</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="el-icon-edit" size="mini" class="glass-btn success-plain" :disabled="single" @click="handleUpdate" v-hasPermi="['biz:location:edit']">修改配置</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="el-icon-delete" size="mini" class="glass-btn danger-plain" :disabled="multiple" @click="handleDelete" v-hasPermi="['biz:location:remove']">删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" icon="el-icon-delete-solid" size="mini" class="glass-btn danger" @click="handleDeleteAll" v-hasPermi="['biz:location:remove']">全部删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="warning" plain icon="el-icon-download" size="mini" class="glass-btn warning-plain" @click="handleExport" v-hasPermi="['biz:location:export']">导出明细</el-button>
        </el-col>
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-row>

      <el-table v-loading="loading" :data="locationList" @selection-change="handleSelectionChange" class="glass-table">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="地点ID" align="center" prop="locationId" width="80" />
        
        <el-table-column label="地点/会议室名称" align="left" prop="locationName" min-width="180" show-overflow-tooltip>
          <template slot-scope="scope">
             <i class="el-icon-office-building" style="margin-right: 5px; color: #409EFF;"></i>
             <span class="elegant-text" style="font-weight: 600;">{{ scope.row.locationName }}</span>
          </template>
        </el-table-column>

        <el-table-column label="最大容纳人数" align="center" prop="capacity" width="120">
          <template slot-scope="scope">
             <span style="font-weight: bold; color: #67C23A; font-size: 15px;">{{ scope.row.capacity || '--' }}</span> 人
          </template>
        </el-table-column>

        <el-table-column label="状态" align="center" prop="status" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" size="small" effect="dark" style="border-radius: 12px; padding: 0 10px;">
              {{ scope.row.status === '0' ? '正常可用' : '维护停用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="地点描述/备注" align="left" prop="remark" min-width="150" show-overflow-tooltip>
          <template slot-scope="scope">
             <span style="color: #909399; font-size: 13px;">{{ scope.row.remark || '暂无说明' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="130" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:location:edit']">设置</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(scope.row)" v-hasPermi="['biz:location:remove']">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" class="glass-pagination" />
    </div>

    <el-dialog :title="title" :visible.sync="open" width="550px" append-to-body custom-class="glass-dialog" :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="elegant-form">
        
        <el-form-item label="会议室名称" prop="locationName">
          <el-input v-model="form.locationName" placeholder="如: 第一会议室 或 总部大礼堂" />
        </el-form-item>

        <el-row>
          <el-col :span="12">
            <el-form-item label="最大容纳人数" prop="capacity">
              <el-input-number v-model="form.capacity" :min="1" :step="10" placeholder="容量" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前状态" prop="status">
              <el-select v-model="form.status" style="width: 100%;">
                <el-option label="正常可用" value="0" />
                <el-option label="维护停用" value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="配置与描述" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="说明该地点的内部配置，如：有投影仪、白板、独立网络等..." />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" class="glass-btn primary" @click="submitForm">保 存 配 置</el-button>
        <el-button class="glass-btn" @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listLocation, getLocation, delLocation, addLocation, updateLocation, deleteAllLocation } from "@/api/biz/location";

export default {
  name: "Location",
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      locationList: [],
      title: "",
      open: false,
      
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        locationName: null,
        capacity: null,
        status: null,
      },
      form: {
        capacity: 50,
        status: '0'
      },
      rules: {
        locationName: [{ required: true, message: "地点/会议室名称不能为空", trigger: "blur" }]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listLocation(this.queryParams).then(response => {
        this.locationList = response.rows;
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
        locationId: undefined,
        locationName: null,
        capacity: 50,
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
      this.ids = selection.map(item => item.locationId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "新增培训会议室";
    },
    handleUpdate(row) {
      this.reset();
      const locationId = row.locationId || this.ids[0];
      getLocation(locationId).then(response => {
        this.form = response.data;
        // 核心防御：锁定ID防止修改变新增
        this.$set(this.form, 'locationId', locationId);
        this.open = true;
        this.title = "修改会议室配置";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.locationId !== undefined && this.form.locationId != null) {
            updateLocation(this.form).then(response => {
              this.$modal.msgSuccess("配置修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addLocation(this.form).then(response => {
              this.$modal.msgSuccess("新增会议室成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDeleteAll() {
      this.$modal.confirm('警告！此操作将删除所有符合当前查询条件的地点数据，且不可恢复！', '确认全部删除').then(() => {
        return deleteAllLocation(this.queryParams);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("全部删除成功");
      }).catch(() => {});
    },
    handleDelete(row) {
      const locationIds = row.locationId || this.ids;
      this.$modal.confirm('危险！是否确认删除选中的会议室？若该地点已被关联使用，建议选择"修改状态"将其停用。').then(function() {
        return delLocation(locationIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    handleExport() {
      this.download('biz/location/export', {
        ...this.queryParams
      }, `培训会议室资源册_${new Date().getTime()}.xlsx`);
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