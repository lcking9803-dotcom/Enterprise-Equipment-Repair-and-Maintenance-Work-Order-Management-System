<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { equipmentApi } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const rows=ref([]), total=ref(0), loading=ref(false), dialog=ref(false), editing=ref(null)
const role=localStorage.getItem('roleCode'); const canEdit=computed(()=>['ADMIN','DISPATCHER'].includes(role))
const query=reactive({page:1,size:10,keyword:'',status:''})
const empty=()=>({equipmentCode:'',name:'',category:'生产设备',location:'',department:'',responsiblePerson:'',status:'RUNNING',maintenanceCycleDays:90,lastMaintenanceDate:null,description:''})
const form=reactive(empty())
const statusMap={RUNNING:'运行中',FAULT:'故障',MAINTENANCE:'保养中',DISABLED:'停用'}
const statusType={RUNNING:'success',FAULT:'danger',MAINTENANCE:'warning',DISABLED:'info'}
const load=async()=>{loading.value=true;try{const r=await equipmentApi.page(query);rows.value=r.data.data.records;total.value=r.data.data.total}finally{loading.value=false}}
const openCreate=()=>{editing.value=null;Object.assign(form,empty());dialog.value=true}
const openEdit=row=>{editing.value=row.id;Object.assign(form,row);dialog.value=true}
const save=async()=>{editing.value?await equipmentApi.update(editing.value,form):await equipmentApi.create(form);ElMessage.success('保存成功');dialog.value=false;load()}
const remove=async row=>{await ElMessageBox.confirm(`确认删除设备“${row.name}”？`,'删除确认',{type:'warning'});await equipmentApi.remove(row.id);ElMessage.success('已删除');load()}
onMounted(load)
</script>

<template>
  <div class="page-head"><div><h2>设备台账</h2><p>统一维护设备责任、位置、状态和保养计划</p></div><el-button v-if="canEdit" type="primary" @click="openCreate"><el-icon><Plus /></el-icon>新增设备</el-button></div>
  <div class="panel">
    <div class="filters"><el-input v-model="query.keyword" clearable placeholder="编号、名称或位置" @keyup.enter="load"/><el-select v-model="query.status" clearable placeholder="设备状态"><el-option v-for="(v,k) in statusMap" :key="k" :label="v" :value="k"/></el-select><el-button @click="query.page=1;load()">查询</el-button></div>
    <el-table v-loading="loading" :data="rows" stripe><el-table-column prop="equipmentCode" label="设备编号" width="145"/><el-table-column prop="name" label="设备名称" min-width="150"/><el-table-column prop="category" label="分类" width="110"/><el-table-column prop="location" label="位置" min-width="150"/><el-table-column prop="department" label="责任部门" width="125"/><el-table-column prop="responsiblePerson" label="责任人" width="90"/><el-table-column label="状态" width="100"><template #default="s"><el-tag :type="statusType[s.row.status]">{{statusMap[s.row.status]}}</el-tag></template></el-table-column><el-table-column prop="nextMaintenanceDate" label="下次保养" width="120"/><el-table-column v-if="canEdit" label="操作" width="125" fixed="right"><template #default="s"><el-button link type="primary" @click="openEdit(s.row)">编辑</el-button><el-button v-if="role==='ADMIN'" link type="danger" @click="remove(s.row)">删除</el-button></template></el-table-column></el-table>
    <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" layout="total, prev, pager, next" :total="total" @current-change="load"/>
  </div>
  <el-dialog v-model="dialog" :title="editing?'编辑设备':'新增设备'" width="680px"><el-form :model="form" label-width="92px"><el-row :gutter="16"><el-col :span="12"><el-form-item label="设备编号"><el-input v-model="form.equipmentCode"/></el-form-item></el-col><el-col :span="12"><el-form-item label="设备名称"><el-input v-model="form.name"/></el-form-item></el-col><el-col :span="12"><el-form-item label="设备分类"><el-input v-model="form.category"/></el-form-item></el-col><el-col :span="12"><el-form-item label="设备状态"><el-select v-model="form.status" style="width:100%"><el-option v-for="(v,k) in statusMap" :key="k" :label="v" :value="k"/></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="设备位置"><el-input v-model="form.location"/></el-form-item></el-col><el-col :span="12"><el-form-item label="责任部门"><el-input v-model="form.department"/></el-form-item></el-col><el-col :span="12"><el-form-item label="责任人"><el-input v-model="form.responsiblePerson"/></el-form-item></el-col><el-col :span="12"><el-form-item label="保养周期"><el-input-number v-model="form.maintenanceCycleDays" :min="1" :max="3650"/></el-form-item></el-col><el-col :span="12"><el-form-item label="上次保养"><el-date-picker v-model="form.lastMaintenanceDate" value-format="YYYY-MM-DD"/></el-form-item></el-col><el-col :span="24"><el-form-item label="设备说明"><el-input v-model="form.description" type="textarea"/></el-form-item></el-col></el-row></el-form><template #footer><el-button @click="dialog=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog>
</template>

