import request from '@/utils/request'

// 上传纸质表图片，OCR 识别"培训部门"字段并匹配 sys_dept 部门树
// 返回值（data 字段）：
//   {
//     recognizedText: '财务部',
//     matchType: 'exact' | 'fuzzy' | 'none',
//     deptId?: Long,
//     deptName?: string,
//     candidates: [{ deptId, deptName, score }]
//   }
export function recognizeHostDept(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/biz/ocr/hostDept',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}