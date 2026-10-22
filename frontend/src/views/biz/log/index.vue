<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="归属的批次ID" prop="batchId">
        <el-input
          v-model="queryParams.batchId"
          placeholder="请输入归属的批次ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="图片文件的MD5哈希值" prop="fileMd5">
        <el-input
          v-model="queryParams.fileMd5"
          placeholder="请输入图片文件的MD5哈希值"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="OCR提取的培训主题" prop="ocrSubject">
        <el-input
          v-model="queryParams.ocrSubject"
          placeholder="请输入OCR提取的培训主题"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="OCR提取的培训日期" prop="ocrDate">
        <el-input
          v-model="queryParams.ocrDate"
          placeholder="请输入OCR提取的培训日期"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="OCR识别出打勾/签字的实际人数" prop="ocrActualCount">
        <el-input
          v-model="queryParams.ocrActualCount"
          placeholder="请输入OCR识别出打勾/签字的实际人数"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['biz:log:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['biz:log:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['biz:log:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          icon="el-icon-delete-solid"
          size="mini"
          @click="handleDeleteAll"
          v-hasPermi="['biz:log:remove']"
        >全部删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['biz:log:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="日志记录ID" align="center" prop="logId" />
      <el-table-column label="归属的批次ID" align="center" prop="batchId" />
      <el-table-column label="图片文件的MD5哈希值" align="center" prop="fileMd5" />
      <el-table-column label="上传的图片存储地址" align="center" prop="fileUrl" />
      <el-table-column label="OCR提取的培训主题" align="center" prop="ocrSubject" />
      <el-table-column label="OCR提取的培训日期" align="center" prop="ocrDate" />
      <el-table-column label="OCR识别出打勾/签字的实际人数" align="center" prop="ocrActualCount" />
      <el-table-column label="处理状态" align="center" prop="status" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['biz:log:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['biz:log:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改OCR防重与拦截记录对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="归属的批次ID" prop="batchId">
          <el-input v-model="form.batchId" placeholder="请输入归属的批次ID" />
        </el-form-item>
        <el-form-item label="图片文件的MD5哈希值" prop="fileMd5">
          <el-input v-model="form.fileMd5" placeholder="请输入图片文件的MD5哈希值" />
        </el-form-item>
        <el-form-item label="上传的图片存储地址" prop="fileUrl">
          <el-input v-model="form.fileUrl" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="OCR提取的培训主题" prop="ocrSubject">
          <el-input v-model="form.ocrSubject" placeholder="请输入OCR提取的培训主题" />
        </el-form-item>
        <el-form-item label="OCR提取的培训日期" prop="ocrDate">
          <el-input v-model="form.ocrDate" placeholder="请输入OCR提取的培训日期" />
        </el-form-item>
        <el-form-item label="OCR识别出打勾/签字的实际人数" prop="ocrActualCount">
          <el-input v-model="form.ocrActualCount" placeholder="请输入OCR识别出打勾/签字的实际人数" />
        </el-form-item>
        <el-form-item label="删除标志" prop="delFlag">
          <el-input v-model="form.delFlag" placeholder="请输入删除标志" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listLog, getLog, delLog, addLog, updateLog, deleteAllLog } from "@/api/biz/log"

export default {
  name: "Log",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // OCR防重与拦截记录表格数据
      logList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        batchId: null,
        fileMd5: null,
        fileUrl: null,
        ocrSubject: null,
        ocrDate: null,
        ocrActualCount: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        fileMd5: [
          { required: true, message: "图片文件的MD5哈希值不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询OCR防重与拦截记录列表 */
    getList() {
      this.loading = true
      listLog(this.queryParams).then(response => {
        this.logList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        logId: null,
        batchId: null,
        fileMd5: null,
        fileUrl: null,
        ocrSubject: null,
        ocrDate: null,
        ocrActualCount: null,
        status: null,
        delFlag: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.logId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加OCR防重与拦截记录"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const logId = row.logId || this.ids
      getLog(logId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改OCR防重与拦截记录"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.logId != null) {
            updateLog(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addLog(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDeleteAll() {
      this.$modal.confirm('警告！此操作将删除所有符合当前查询条件的OCR日志数据，且不可恢复！', '确认全部删除').then(() => {
        return deleteAllLog(this.queryParams);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("全部删除成功");
      }).catch(() => {});
    },
    handleDelete(row) {
      const logIds = row.logId || this.ids
      this.$modal.confirm('是否确认删除OCR防重与拦截记录编号为"' + logIds + '"的数据项？').then(function() {
        return delLog(logIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('biz/log/export', {
        ...this.queryParams
      }, `log_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
