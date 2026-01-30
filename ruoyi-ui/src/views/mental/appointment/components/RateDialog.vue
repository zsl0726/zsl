<template>
  <el-dialog
    title="咨询评价"
    :visible.sync="visible"
    width="400px"
    append-to-body
    @close="handleClose"
  >
    <div style="text-align: center;">
      <div style="margin-bottom: 20px; font-size: 16px;">
        请对本次咨询进行评价
      </div>
      
      <div style="margin-bottom: 30px;">
        <el-rate
          v-model="form.ratingScore"
          :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
          show-text
          text-color="#ff9900"
          size="large"
          style="margin-bottom: 10px;"
        />
        <div v-if="form.ratingScore" style="color: #ff9900; font-size: 14px;">
          {{ getRatingText(form.ratingScore) }}
        </div>
      </div>
      
      <el-input
        v-model="form.ratingComment"
        type="textarea"
        :rows="3"
        placeholder="请写下您的咨询感受和建议（可选）"
        style="margin-bottom: 15px;"
        maxlength="200"
        show-word-limit
      ></el-input>
    </div>
    
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取 消</el-button>
      <el-button 
        type="primary" 
        @click="handleSubmit" 
        :disabled="!form.ratingScore"
        :loading="loading"
      >
        提 交
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
import { submitRating } from '@/api/mental/appointment'

export default {
  name: 'RateDialog',
  props: {
    appointment: {
      type: Object,
      required: true
    },
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      form: {
        appointmentId: null,
        ratingScore: 0,
        ratingComment: ''
      },
      loading: false,
      ratingTexts: {
        1: '很差',
        2: '较差',
        3: '一般',
        4: '满意',
        5: '非常满意'
      }
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.form.appointmentId = this.appointment.id
        this.form.ratingScore = 0
        this.form.ratingComment = ''
      }
    }
  },
  methods: {
    getRatingText(score) {
      return this.ratingTexts[score] || ''
    },
    
    async handleSubmit() {
      if (!this.form.ratingScore) {
        this.$message.warning('请先选择评分')
        return
      }
      
      this.loading = true
      try {
        const response = await submitRating(this.form)
        if (response.code === 200) {
          this.$message.success('评价成功')
          this.$emit('success')
          this.visible = false
        } else {
          this.$message.error(response.msg || '评价失败')
        }
      } catch (error) {
        console.error('评价失败:', error)
        this.$message.error('评价失败')
      } finally {
        this.loading = false
      }
    },
    
    handleClose() {
      this.$emit('update:visible', false)
      this.$emit('close')
    }
  }
}
</script>