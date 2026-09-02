<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { workOrderApi } from '../api'
import { ElMessage } from 'element-plus'

const route=useRoute(),detail=ref({workOrder:{},logs:[],attachments:[]}),stage=ref('REPORT')
const statuses={PENDING_ACCEPTANCE:'待受理',PENDING_DISPATCH:'待派单',IN_REPAIR:'维修中',PENDING_INSPECTION:'待验收',CLOSED:'已关闭',CANCELLED:'已取消'}
const actions={CREATE:'创建报修',ACCEPT:'受理',DISPATCH:'派单',SUBMIT_REPAIR:'提交维修',INSPECTION_PASS:'验收通过',INSPECTION_REJECT:'验收驳回'}
const load=async()=>{const r=await workOrderApi.detail(route.params.id);detail.value=r.data.data}
const upload=async file=>{await workOrderApi.upload(route.params.id,stage.value,file.raw);ElMessage.success('附件上传成功');load()}
onMounted(load)
</script>

<template>
  <div class="page-head"><div><h2>工单详情</h2><p>{{detail.workOrder.orderNo}}</p></div><el-button @click="$router.back()">返回列表</el-button></div>
  <section class="detail-grid"><div class="panel"><h3 class="section-title">故障与处理信息</h3><dl class="kv"><dt>工单标题</dt><dd>{{detail.workOrder.title}}</dd><dt>当前状态</dt><dd><el-tag>{{statuses[detail.workOrder.status]}}</el-tag></dd><dt>设备ID</dt><dd>{{detail.workOrder.equipmentId}}</dd><dt>故障类型</dt><dd>{{detail.workOrder.faultType}}</dd><dt>优先级</dt><dd>{{detail.workOrder.priority}}</dd><dt>SLA截止</dt><dd>{{detail.workOrder.slaDeadline}}</dd><dt>故障描述</dt><dd>{{detail.workOrder.faultDescription}}</dd><dt>维修说明</dt><dd>{{detail.workOrder.repairDescription || '尚未提交'}}</dd><dt>维修费用</dt><dd>{{detail.workOrder.repairCost ?? '—'}}</dd><dt>驳回原因</dt><dd>{{detail.workOrder.rejectionReason || '—'}}</dd></dl><el-divider/><h3 class="section-title">附件材料</h3><div class="action-row"><el-select v-model="stage" style="width:140px"><el-option label="报修材料" value="REPORT"/><el-option label="维修材料" value="REPAIR"/><el-option label="验收材料" value="INSPECTION"/></el-select><el-upload :show-file-list="false" :auto-upload="false" :on-change="upload" accept="image/jpeg,image/png,image/webp,application/pdf"><el-button>选择并上传</el-button></el-upload></div><el-table :data="detail.attachments" size="small" style="margin-top:12px"><el-table-column prop="stage" label="阶段" width="100"/><el-table-column prop="originalName" label="文件"/><el-table-column prop="sizeBytes" label="大小(B)" width="100"/><el-table-column label="查看" width="70"><template #default="s"><el-link :href="`/api/files/${s.row.id}`" target="_blank" type="primary">打开</el-link></template></el-table-column></el-table></div>
    <div class="panel"><h3 class="section-title">状态流转审计</h3><el-timeline><el-timeline-item v-for="log in detail.logs" :key="log.id" :timestamp="log.createdAt" placement="top" :type="log.action==='INSPECTION_REJECT'?'danger':'primary'"><div class="timeline-title">{{actions[log.action] || log.action}}</div><div class="timeline-meta">{{log.operatorName}} · {{log.fromStatus ? statuses[log.fromStatus]+' → ' : ''}}{{statuses[log.toStatus]}}</div><p v-if="log.remark">{{log.remark}}</p></el-timeline-item></el-timeline></div></section>
</template>
