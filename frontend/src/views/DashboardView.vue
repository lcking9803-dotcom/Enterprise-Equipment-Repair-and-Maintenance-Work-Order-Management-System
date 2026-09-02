<script setup>
import { onMounted, ref, nextTick } from 'vue'
import * as echarts from 'echarts'
import { dashboardApi } from '../api'

const data = ref({ total:0, open:0, closed:0, slaComplianceRate:0, averageFirstResponseHours:0, mttrHours:0 })
const statusEl = ref(); const faultEl = ref(); const trendEl = ref()
const render = () => {
  echarts.init(statusEl.value).setOption({ tooltip:{trigger:'item'}, legend:{bottom:0}, series:[{type:'pie',radius:['42%','68%'],itemStyle:{borderRadius:5,borderColor:'#fff',borderWidth:3},data:data.value.statusDistribution || []}] })
  echarts.init(faultEl.value).setOption({ tooltip:{trigger:'axis'}, grid:{left:45,right:18,top:25,bottom:45}, xAxis:{type:'category',data:(data.value.faultRanking||[]).map(x=>x.name),axisLabel:{rotate:22}}, yAxis:{type:'value'}, series:[{type:'bar',data:(data.value.faultRanking||[]).map(x=>x.value),itemStyle:{color:'#1a9b84',borderRadius:[5,5,0,0]}}] })
  const trend = [...(data.value.monthlyTrend||[])].reverse()
  echarts.init(trendEl.value).setOption({ tooltip:{trigger:'axis'}, legend:{data:['新建工单','关闭工单']}, grid:{left:45,right:25,top:45,bottom:35}, xAxis:{type:'category',data:trend.map(x=>x.month)},yAxis:{type:'value'},series:[{name:'新建工单',type:'line',smooth:true,data:trend.map(x=>x.created_count),lineStyle:{color:'#245b78'},itemStyle:{color:'#245b78'}},{name:'关闭工单',type:'line',smooth:true,data:trend.map(x=>x.closed_count),lineStyle:{color:'#1a9b84'},itemStyle:{color:'#1a9b84'}}] })
}
onMounted(async()=>{ const res=await dashboardApi.summary(); data.value=res.data.data; await nextTick(); render() })
</script>

<template>
  <div class="page-head"><div><h2>运行看板</h2><p>聚焦响应效率、修复质量与设备风险</p></div><el-button @click="location.reload()"><el-icon><Refresh /></el-icon>刷新</el-button></div>
  <section class="stat-grid">
    <div class="stat-card accent"><small>累计工单</small><strong>{{ data.total }}</strong></div>
    <div class="stat-card"><small>处理中</small><strong>{{ data.open }}</strong></div>
    <div class="stat-card"><small>已关闭</small><strong>{{ data.closed }}</strong></div>
    <div class="stat-card"><small>SLA达标率</small><strong>{{ data.slaComplianceRate }}%</strong></div>
    <div class="stat-card"><small>平均响应</small><strong>{{ data.averageFirstResponseHours }}h</strong></div>
    <div class="stat-card"><small>平均修复 MTTR</small><strong>{{ data.mttrHours }}h</strong></div>
  </section>
  <section class="chart-grid">
    <div class="panel"><h3 class="section-title">工单状态分布</h3><div ref="statusEl" class="chart"></div></div>
    <div class="panel"><h3 class="section-title">高频故障类型</h3><div ref="faultEl" class="chart"></div></div>
    <div class="panel chart wide"><h3 class="section-title">月度工单趋势</h3><div ref="trendEl" style="height:240px"></div></div>
  </section>
</template>

