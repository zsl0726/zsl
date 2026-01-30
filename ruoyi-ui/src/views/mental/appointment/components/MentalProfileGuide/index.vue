<template>
  <el-dialog
    title="完善心理档案"
    :visible.sync="visible"
    width="500px"
    :close-on-click-modal="false"
    :show-close="false"
    center
  >
    <div class="guide-content">
      <div class="guide-icon">🧠</div>
      <div class="guide-title">欢迎使用心理健康服务</div>
      <div class="guide-desc">
        为了给你提供更精准的心理健康服务，建议你先完善个人心理档案。
        这份档案将帮助我们了解你的基本情况，提供个性化的心理疏导和建议。
      </div>
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button @click="skip">稍后完善</el-button>
      <el-button type="primary" @click="goToProfile">立即完善</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'MentalProfileGuide',
  data() {
    return {
      visible: false
    }
  },
  methods: {
    open() {
      this.visible = true
    },
    skip() {
      this.visible = false
      // 记录已跳过，避免重复弹窗
      localStorage.setItem('profileGuideSkipped', 'true')
    },
    goToProfile() {
      this.visible = false
      this.$router.push('/mental/profile/edit')
    }
  }
}
</script>

<style scoped>
.guide-content {
  text-align: center;
  padding: 20px 0;
}
.guide-icon {
  font-size: 60px;
  margin-bottom: 20px;
}
.guide-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 15px;
}
.guide-desc {
  color: #666;
  line-height: 1.6;
  text-align: left;
}
.dialog-footer {
  text-align: center;
}
</style>