<template>
  <section class="page-container">
    <el-card class="page-card">
      <template #header>管理后台</template>
      <el-row :gutter="16">
        <el-col :span="6">
          <el-statistic title="总用户数" :value="0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="总订单数" :value="0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="总销售额" :value="0" prefix="￥" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="今日订单" :value="0" />
        </el-col>
      </el-row>
    </el-card>
  </section>
</template>
