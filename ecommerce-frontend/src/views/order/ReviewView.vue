<template>
  <section class="review-view">
    <div class="review-container">
      <h2 class="page-title">商品评价</h2>

      <div v-if="loading" class="loading-block">加载中...</div>

      <template v-else-if="orderItems.length">
        <div
          v-for="(item, idx) in orderItems"
          :key="item.id"
          class="review-card"
        >
          <div class="goods-info">
            <el-image :src="item.productImage || fallbackImage" class="thumb" fit="cover" />
            <div class="meta">
              <p class="name">{{ item.productName }}</p>
              <p v-if="item.skuName" class="spec">规格：{{ item.skuName }}</p>
            </div>
          </div>

          <el-form
            :ref="el => setFormRef(el, idx)"
            :model="reviewForms[idx]"
            :rules="reviewRules"
            label-width="80px"
            class="review-form"
          >
            <el-form-item label="评分" prop="rating">
              <el-rate v-model="reviewForms[idx].rating" :texts="ratingTexts" show-text />
            </el-form-item>
            <el-form-item label="评价" prop="content">
              <el-input
                v-model="reviewForms[idx].content"
                type="textarea"
                :rows="3"
                :maxlength="500"
                show-word-limit
                placeholder="说说商品哪里好，或者哪里不满意"
              />
            </el-form-item>
            <el-form-item label="上传图片">
              <ImageUpload
                v-model="reviewForms[idx].imageList"
                multiple
                :limit="6"
              />
            </el-form-item>
          </el-form>
        </div>

        <div class="submit-bar">
          <el-button @click="$router.push('/orders')">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交评价</el-button>
        </div>
      </template>

      <Empty v-else size="large" description="没有可评价的商品">
        <el-button type="primary" @click="$router.push('/orders')">返回订单</el-button>
      </Empty>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail, submitProductReview } from '@/api'
import ImageUpload from '@/components/ImageUpload.vue'
import Empty from '@/components/Empty.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const orderItems = ref([])
const reviewForms = ref([])
const formRefs = ref([])
const fallbackImage = 'https://placehold.co/80x80/cccccc/666666?text=Product'

const ratingTexts = ['很差', '较差', '一般', '良好', '非常好']

const reviewRules = {
  rating: [{ required: true, type: 'number', min: 1, message: '请打分', trigger: 'change' }]
}

function setFormRef(el, idx) {
  if (el) formRefs.value[idx] = el
}

async function loadOrderItems() {
  const orderId = route.query.orderId
  if (!orderId) {
    ElMessage.warning('订单参数缺失')
    router.replace('/orders')
    return
  }
  loading.value = true
  try {
    const data = await getOrderDetail(orderId)
    orderItems.value = data?.items || []
    reviewForms.value = orderItems.value.map(item => ({
      productId: item.productId,
      orderItemId: item.id,
      rating: 5,
      content: '',
      imageList: []  // 字符串 URL 数组
    }))
  } catch (e) {
    orderItems.value = []
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  // validate all forms
  for (let i = 0; i < formRefs.value.length; i++) {
    const ref = formRefs.value[i]
    if (!ref) continue
    try {
      await ref.validate()
    } catch {
      ElMessage.warning(`第 ${i + 1} 件商品请完成评分`)
      return
    }
  }

  submitting.value = true
  try {
    for (let i = 0; i < reviewForms.value.length; i++) {
      const f = reviewForms.value[i]
      const images = (f.imageList || []).filter(Boolean)
      await submitProductReview({
        productId: f.productId,
        orderItemId: f.orderItemId,
        rating: f.rating,
        content: f.content,
        images: images.length ? JSON.stringify(images) : ''
      })
    }
    ElMessage.success('评价提交成功')
    router.replace('/orders')
  } catch (e) {} finally {
    submitting.value = false
  }
}

onMounted(loadOrderItems)
</script>

<style scoped lang="scss">
.review-view {
  background: #f5f7fa;
  padding: 16px 0 80px;
  min-height: 600px;
}
.review-container {
  width: min(900px, calc(100% - 32px));
  margin: 0 auto;
}
.page-title {
  margin: 0 0 16px;
  font-size: 22px;
}

.loading-block {
  text-align: center;
  padding: 80px 0;
  background: #fff;
  border-radius: 12px;
  color: #94a3b8;
}

.review-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 16px;
}

.goods-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #f1f5f9;
  margin-bottom: 16px;

  .thumb {
    width: 60px;
    height: 60px;
    border-radius: 4px;
    overflow: hidden;
    flex-shrink: 0;
    background: #f1f5f9;
  }
  .meta {
    .name { margin: 0 0 4px; font-size: 14px; }
    .spec { margin: 0; color: #94a3b8; font-size: 12px; }
  }
}

.review-form {
  max-width: 720px;
}

.submit-bar {
  background: #fff;
  border-radius: 12px;
  padding: 16px 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
