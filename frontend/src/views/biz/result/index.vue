<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="所属批次ID" prop="batchId">
        <el-input
          v-model="queryParams.batchId"
          placeholder="请输入所属批次ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="来源于哪张图片(关联ocr_log)" prop="logId">
        <el-input
          v-model="queryParams.logId"
          placeholder="请输入来源于哪张图片(关联ocr_log)"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="OCR提取的姓名" prop="parsedName">
        <el-input
          v-model="queryParams.parsedName"
          placeholder="请输入OCR提取的姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="OCR提取的部门" prop="parsedDept">
        <el-input
          v-model="queryParams.parsedDept"
          placeholder="请输入OCR提取的部门"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="OCR提取的签到/考核成绩" prop="parsedScore">
        <el-input
          v-model="queryParams.parsedScore"
          placeholder="请输入OCR提取的签到/考核成绩"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="是否为系统未知人员" prop="isNewUser">
        <el-input
          v-model="queryParams.isNewUser"
          placeholder="请输入是否为系统未知人员"
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
          v-hasPermi="['biz:result:add']"
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
          v-hasPermi="['biz:result:edit']"
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
          v-hasPermi="['biz:result:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          icon="el-icon-delete-solid"
          size="mini"
          @click="handleDeleteAll"
          v-hasPermi="['biz:result:remove']"
        >全部删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['biz:result:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="resultList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="结果明细ID" align="center" prop="resultId" />
      <el-table-column label="所属批次ID" align="center" prop="batchId" />
      <el-table-column label="来源于哪张图片(关联ocr_log)" align="center" prop="logId" />
      <el-table-column label="OCR提取的姓名" align="center" prop="parsedName" />
      <el-table-column label="OCR提取的部门" align="center" prop="parsedDept" />
      <el-table-column label="OCR提取的签到/考核成绩" align="center" prop="parsedScore" />
      <el-table-column label="是否为系统未知人员" align="center" prop="isNewUser" />
      <el-table-column label="数据状态" align="center" prop="status" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['biz:result:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['biz:result:remove']"
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

    <!-- 添加或修改OCR识别结果暂存明细对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="所属批次ID" prop="batchId">
          <el-input v-model="form.batchId" placeholder="请输入所属批次ID" />
        </el-form-item>
        <el-form-item label="来源于哪张图片(关联ocr_log)" prop="logId">
          <el-input v-model="form.logId" placeholder="请输入来源于哪张图片(关联ocr_log)" />
        </el-form-item>
        <el-form-item label="OCR提取的姓名" prop="parsedName">
          <el-input v-model="form.parsedName" placeholder="请输入OCR提取的姓名" />
        </el-form-item>
        <el-form-item label="OCR提取的部门" prop="parsedDept">
          <el-input v-model="form.parsedDept" placeholder="请输入OCR提取的部门" />
        </el-form-item>
        <el-form-item label="OCR提取的签到/考核成绩" prop="parsedScore">
          <el-input v-model="form.parsedScore" placeholder="请输入OCR提取的签到/考核成绩" />
        </el-form-item>
        <el-form-item label="是否为系统未知人员" prop="isNewUser">
          <el-input v-model="form.isNewUser" placeholder="请输入是否为系统未知人员" />
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
import { listResult, getResult, delResult, addResult, updateResult, deleteAllResult } from "@/api/biz/result"

export default {
  name: "Result",
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
      // OCR识别结果暂存明细表格数据
      resultList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        batchId: null,
        logId: null,
        parsedName: null,
        parsedDept: null,
        parsedScore: null,
        isNewUser: null,
        status: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        batchId: [
          { required: true, message: "所属批次ID不能为空", trigger: "blur" }
        ],
        parsedName: [
          { required: true, message: "OCR提取的姓名不能为空", trigger: "blur" }
        ],
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询OCR识别结果暂存明细列表 */
    getList() {
      this.loading = true
      listResult(this.queryParams).then(response => {
        this.resultList = response.rows
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
        resultId: null,
        batchId: null,
        logId: null,
        parsedName: null,
        parsedDept: null,
        parsedScore: null,
        isNewUser: null,
        status: null
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
      this.ids = selection.map(item => item.resultId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加OCR识别结果暂存明细"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const resultId = row.resultId || this.ids
      getResult(resultId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改OCR识别结果暂存明细"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.resultId != null) {
            updateResult(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addResult(this.form).then(response => {
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
      this.$modal.confirm('警告！此操作将删除所有符合当前查询条件的OCR结果数据，且不可恢复！', '确认全部删除').then(() => {
        return deleteAllResult(this.queryParams);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("全部删除成功");
      }).catch(() => {});
    },
    handleDelete(row) {
      const resultIds = row.resultId || this.ids
      this.$modal.confirm('是否确认删除OCR识别结果暂存明细编号为"' + resultIds + '"的数据项？').then(function() {
        return delResult(resultIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('biz/result/export', {
        ...this.queryParams
      }, `result_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
