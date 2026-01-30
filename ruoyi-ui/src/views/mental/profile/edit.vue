<template>
  <div class="app-container">
    <div class="edit-container">
      <!-- 头部区域 -->
      <div class="header-card">
        <div class="header-content">
          <div class="header-left">
            <div class="title-section">
              <i class="el-icon-user-solid title-icon"></i>
              <h2 class="main-title">{{ title }}</h2>
              <el-button 
                @click="$router.go(-1)" 
                icon="el-icon-back" 
                size="small" 
                class="back-btn">
                返回
              </el-button>
            </div>
            <p class="header-description">
              {{ isEdit ? '更新您的心理健康档案信息' : '填写个人心理健康档案' }}
            </p>
          </div>
        </div>
      </div>

      <!-- 表单内容 -->
      <div class="form-content">
        <el-card shadow="never" class="form-card">
          <!-- 个人信息区域 -->
          <div class="form-section">
            <div class="section-header">
              <h3 class="section-title">
                <i class="el-icon-user section-icon"></i>
                个人信息
              </h3>
            </div>
            <div class="section-content">
              <el-form 
                ref="formRef" 
                :model="form" 
                :rules="rules" 
                label-width="100px"
                :disabled="loading">
                
                <div class="form-row">
                  <!-- 真实姓名 -->
                  <div class="form-col">
                    <el-form-item label="真实姓名" prop="realName">
                      <el-input 
                        v-model="form.realName" 
                        placeholder="请输入真实姓名" 
                        maxlength="20"
                        show-word-limit
                        class="form-input"
                        size="small">
                      </el-input>
                    </el-form-item>
                  </div>
                  
                  <!-- 性别 -->
                  <div class="form-col">
                    <el-form-item label="性别" prop="gender">
                      <el-radio-group v-model="form.gender" size="small" class="gender-radio">
                        <el-radio label="0" class="radio-item">
                          <i class="el-icon-male radio-icon male"></i>
                          <span>男</span>
                        </el-radio>
                        <el-radio label="1" class="radio-item">
                          <i class="el-icon-female radio-icon female"></i>
                          <span>女</span>
                        </el-radio>
                        <el-radio label="2" class="radio-item">
                          <i class=" radio-icon other"></i>
                          <span>其他</span>
                        </el-radio>
                      </el-radio-group>
                    </el-form-item>
                  </div>
                  
                  <!-- 年龄 -->
                  <div class="form-col">
                    <el-form-item label="年龄" prop="age">
                      <el-input-number 
                        v-model="form.age" 
                        :min="1" 
                        :max="120" 
                        placeholder="年龄"
                        controls-position="right"
                        size="small"
                        class="age-input">
                      </el-input-number>
                    </el-form-item>
                  </div>
                  
                  <!-- 职业 -->
                  <div class="form-col full">
                    <el-form-item label="职业/身份" prop="occupation">
                      <el-input 
                        v-model="form.occupation" 
                        placeholder="如：学生、在职人员、自由职业等" 
                        maxlength="50"
                        show-word-limit
                        size="small"
                        class="form-input">
                      </el-input>
                    </el-form-item>
                  </div>
                </div>
              </el-form>
            </div>
          </div>

          <!-- 心理健康状态 -->
          <div class="form-section">
            <div class="section-header">
              <h3 class="section-title">
                <i class="el-icon-data-line section-icon"></i>
                心理健康状态
              </h3>
            </div>
            <div class="section-content">
              <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
                <div class="form-row">
                  <!-- 情绪状态 -->
                  <div class="form-col">
                    <el-form-item label="情绪状态" prop="emotionalState">
                      <div class="rate-container">
                        <el-rate
                          v-model="form.emotionalState"
                          :max="5"
                          size="small"
                          :colors="['#F56C6C', '#F56C6C', '#E6A23C', '#67C23A', '#67C23A']">
                        </el-rate>
                        <div class="rate-text">{{ emotionTexts[form.emotionalState - 1] || '请选择' }}</div>
                      </div>
                    </el-form-item>
                  </div>
                  
                  <!-- 睡眠质量 -->
                  <div class="form-col">
                    <el-form-item label="睡眠质量" prop="sleepQuality">
                      <div class="rate-container">
                        <el-rate
                          v-model="form.sleepQuality"
                          :max="5"
                          size="small"
                          :colors="['#F56C6C', '#F56C6C', '#E6A23C', '#67C23A', '#67C23A']">
                        </el-rate>
                        <div class="rate-text">{{ sleepTexts[form.sleepQuality - 1] || '请选择' }}</div>
                      </div>
                    </el-form-item>
                  </div>
                </div>
                
                <!-- 压力水平 -->
                <div class="form-row">
                  <div class="form-col full">
                    <el-form-item label="压力水平" prop="stressLevel">
                      <div class="stress-container">
                        <div class="slider-wrapper">
                          <el-slider 
                            v-model="form.stressLevel"
                            :min="0"
                            :max="100"
                            :step="5"
                            size="small"
                            show-stops
                            :show-tooltip="true"
                            :format-tooltip="val => `${val}%`"
                            class="stress-slider">
                          </el-slider>
                        </div>
                        <div class="stress-indicator">
                          <div class="indicator-item" :class="{ active: form.stressLevel < 30 }">
                            <div class="indicator-dot" :style="{backgroundColor: getStressColor(0)}"></div>
                            <span class="indicator-text">低</span>
                          </div>
                          <div class="indicator-item" :class="{ active: form.stressLevel >= 30 && form.stressLevel < 60 }">
                            <div class="indicator-dot" :style="{backgroundColor: getStressColor(50)}"></div>
                            <span class="indicator-text">中</span>
                          </div>
                          <div class="indicator-item" :class="{ active: form.stressLevel >= 60 && form.stressLevel < 80 }">
                            <div class="indicator-dot" :style="{backgroundColor: getStressColor(75)}"></div>
                            <span class="indicator-text">高</span>
                          </div>
                          <div class="indicator-item" :class="{ active: form.stressLevel >= 80 }">
                            <div class="indicator-dot" :style="{backgroundColor: getStressColor(100)}"></div>
                            <span class="indicator-text">极高</span>
                          </div>
                        </div>
                      </div>
                    </el-form-item>
                  </div>
                </div>
              </el-form>
            </div>
          </div>

          <!-- 主要困扰 -->
          <div class="form-section">
            <div class="section-header">
              <h3 class="section-title">
                <i class="el-icon-question section-icon"></i>
                主要困扰
              </h3>
            </div>
            <div class="section-content">
              <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
                <el-form-item label="主要困扰" prop="mainConcernsArray">
                  <div class="concerns-container">
                    <el-checkbox-group v-model="form.mainConcernsArray" size="small" class="concerns-checkbox-group">
                      <div class="concerns-grid">
                        <el-checkbox 
                          v-for="concern in concernsOptions" 
                          :key="concern.value" 
                          :label="concern.label"
                          class="concern-checkbox">
                          {{ concern.label }}
                        </el-checkbox>
                      </div>
                    </el-checkbox-group>
                    
                    <!-- 其他困扰 -->
                    <div class="other-concerns">
                      <div class="other-label">
                        <i class="el-icon-edit-outline label-icon"></i>
                        其他困扰（选填）
                      </div>
                      <el-input
                        v-model="form.otherConcerns"
                        type="textarea"
                        :rows="2"
                        placeholder="如有其他困扰，请在此描述..."
                        maxlength="500"
                        show-word-limit
                        size="small"
                        class="other-textarea">
                      </el-input>
                    </div>
                  </div>
                </el-form-item>
              </el-form>
            </div>
          </div>

          <!-- 期望与目标 -->
          <div class="form-section">
            <div class="section-header">
              <h3 class="section-title">
                <i class="el-icon-aim section-icon"></i>
                期望与目标
              </h3>
            </div>
            <div class="section-content">
              <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
                <!-- 期望帮助 -->
                <div class="form-row">
                  <div class="form-col full">
                    <el-form-item label="期望帮助" prop="expectations">
                      <el-input
                        v-model="form.expectations"
                        type="textarea"
                        :rows="3"
                        placeholder="请描述您希望通过心理咨询获得哪些帮助..."
                        maxlength="500"
                        show-word-limit
                        size="small"
                        class="expectations-textarea">
                      </el-input>
                    </el-form-item>
                  </div>
                </div>
                
                <!-- 咨询经历 -->
                <div class="form-row">
                  <div class="form-col">
                    <el-form-item label="咨询经历" prop="therapyExperience">
                      <el-select 
                        v-model="form.therapyExperience" 
                        placeholder="请选择"
                        size="small"
                        class="therapy-select">
                        <el-option label="从未有过" value="从未有过" />
                        <el-option label="有过但已结束" value="有过但已结束" />
                        <el-option label="正在接受" value="正在接受" />
                      </el-select>
                    </el-form-item>
                  </div>
                </div>
              </el-form>
            </div>
          </div>

          <!-- 补充说明 -->
          <div class="form-section">
            <div class="section-header">
              <h3 class="section-title">
                <i class="el-icon-info section-icon"></i>
                补充说明
              </h3>
            </div>
            <div class="section-content">
              <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
                <div class="form-row">
                  <div class="form-col full">
                    <el-form-item label="补充说明" prop="additionalNotes">
                      <el-input
                        v-model="form.additionalNotes"
                        type="textarea"
                        :rows="4"
                        placeholder="如有其他需要说明的情况...（选填）"
                        maxlength="1000"
                        show-word-limit
                        size="small"
                        class="additional-textarea">
                      </el-input>
                    </el-form-item>
                  </div>
                </div>
              </el-form>
            </div>
          </div>

          <!-- 表单操作 -->
          <div class="form-actions">
            <el-button 
              type="primary" 
              @click="submitForm" 
              :loading="loading"
              size="small"
              class="submit-btn">
              {{ isEdit ? '更新档案' : '提交档案' }}
            </el-button>
            <el-button 
              @click="$router.go(-1)"
              size="small"
              class="cancel-btn">
              取消
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
import { getMyProfile, createMyProfile, updateMyProfile } from "@/api/mental/profile";

export default {
  name: "MentalProfileEdit",
  data() {
    return {
      form: {
        id: '',
        realName: '',
        gender: '',
        age: null,
        occupation: '',
        emotionalState: null,
        sleepQuality: null,
        stressLevel: 50,
        mainConcernsArray: [],
        otherConcerns: '',
        expectations: '',
        therapyExperience: '',
        additionalNotes: '',
        userId: ''
      },
      
      rules: {
        realName: [
          { required: true, message: '请输入真实姓名', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        gender: [
          { required: true, message: '请选择性别', trigger: 'change' }
        ],
        age: [
          { required: true, message: '请输入年龄', trigger: 'blur' },
          { type: 'number', min: 1, max: 120, message: '年龄在 1 到 120 之间', trigger: 'blur' }
        ],
        emotionalState: [
          { required: true, message: '请评估情绪状态', trigger: 'change' }
        ],
        sleepQuality: [
          { required: true, message: '请评估睡眠质量', trigger: 'change' }
        ],
        stressLevel: [
          { required: true, message: '请选择压力水平', trigger: 'change' }
        ],
        mainConcernsArray: [
          { required: true, message: '请选择至少一项主要困扰', trigger: 'change' }
        ]
      },
      
      concernsOptions: [
        { label: '学业压力', value: '学业压力' },
        { label: '工作压力', value: '工作压力' },
        { label: '人际关系', value: '人际关系' },
        { label: '情绪管理', value: '情绪管理' },
        { label: '自我认知', value: '自我认知' },
        { label: '家庭问题', value: '家庭问题' },
        { label: '恋爱关系', value: '恋爱关系' },
        { label: '职业发展', value: '职业发展' },
        { label: '睡眠问题', value: '睡眠问题' },
        { label: '焦虑情绪', value: '焦虑情绪' },
        { label: '抑郁情绪', value: '抑郁情绪' }
      ],
      
      emotionTexts: ['非常差', '较差', '一般', '良好', '非常好'],
      sleepTexts: ['非常差', '较差', '一般', '良好', '非常好'],
      
      loading: false,
      isEdit: false,
      title: '填写心理档案'
    };
  },
  created() {
    this.initData();
  },
  methods: {
    initData() {
      this.loading = true;
      getMyProfile()
        .then(response => {
          if (response.code === 200 && response.data && typeof response.data === 'object') {
            this.isEdit = true;
            this.title = '修改心理档案';
            this.form = {
              id: response.data.id || '',
              realName: response.data.realName || '',
              gender: response.data.gender || '',
              age: response.data.age || null,
              occupation: response.data.occupation || '',
              emotionalState: response.data.emotionalState || null,
              sleepQuality: response.data.sleepQuality || null,
              stressLevel: response.data.stressLevel || 50,
              mainConcernsArray: response.data.mainConcernsArray || [],
              otherConcerns: response.data.otherConcerns || '',
              expectations: response.data.expectations || '',
              therapyExperience: response.data.therapyExperience || '',
              additionalNotes: response.data.additionalNotes || '',
              userId: response.data.userId || ''
            };
          } else {
            this.isEdit = false;
            this.title = '填写心理档案';
          }
        })
        .catch(error => {
          console.error('初始化档案失败:', error);
          this.$modal.msgError('初始化失败：' + (error.response?.data?.msg || '网络异常'));
        })
        .finally(() => {
          this.loading = false;
        });
    },
    
    submitForm() {
      this.$refs.formRef.validate(valid => {
        if (!valid) return;
        
        this.loading = true;
        const submitApi = this.isEdit ? updateMyProfile : createMyProfile;
        
        submitApi(this.form)
          .then(response => {
            if (response.code === 200) {
              this.$modal.msgSuccess(this.isEdit ? '档案更新成功' : '档案创建成功');
              localStorage.setItem('hasMentalProfile', 'true');
              setTimeout(() => {
                this.$router.push({ 
                  name: 'MentalProfileIndex',
                  query: { refresh: 'true' }
                });
              }, 300);
            } else {
              this.$modal.msgError(response.msg || '操作失败');
            }
          })
          .catch(error => {
            console.error('提交失败:', error);
            this.$modal.msgError('提交失败');
          })
          .finally(() => {
            this.loading = false;
          });
      });
    },
    
    getStressColor(value) {
      if (value < 30) return '#67C23A';
      if (value < 60) return '#E6A23C';
      if (value < 80) return '#F56C6C';
      return '#909399';
    }
  }
};
</script>

<style scoped>
.app-container {
  padding: 16px;
  background: #f5f7fa;
  min-height: 100vh;
}

.edit-container {
  max-width: 1000px;
  margin: 0 auto;
}

/* 头部区域 */
.header-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  padding: 20px 24px;
  margin-bottom: 16px;
  color: white;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-left {
  flex: 1;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.title-icon {
  font-size: 20px;
  color: white;
}

.main-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: white;
}

.back-btn {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 12px;
  height: auto;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}

.header-description {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
  margin: 4px 0 0;
  line-height: 1.4;
}

/* 表单内容 */
.form-content {
  background: transparent;
}

.form-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

/* 表单区域 */
.form-section {
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
}

.form-section:last-child {
  border-bottom: none;
}

.section-header {
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.section-icon {
  font-size: 16px;
  color: #667eea;
}

.section-content {
  padding: 0;
}

/* 表单布局 */
.form-row {
  display: flex;
  flex-wrap: wrap;
  margin: 0 -8px;
}

.form-col {
  padding: 0 8px;
  flex: 1;
  min-width: 200px;
  margin-bottom: 8px;
}

.form-col.full {
  flex: 0 0 100%;
  min-width: 100%;
}

/* 表单元素样式 */
.form-input,
.age-input,
.therapy-select {
  width: 100%;
}

.age-input {
  max-width: 120px;
}

:deep(.el-input-number--small) {
  width: 100%;
}

/* 性别单选按钮 */
.gender-radio {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: all 0.2s ease;
  font-size: 12px;
}

.radio-item:hover {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.radio-item :deep(.el-radio__label) {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.radio-icon {
  font-size: 14px;
}

.male {
  color: #409EFF;
}

.female {
  color: #F56C6C;
}

.other {
  color: #909399;
}

.radio-item.is-checked {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

/* 评分组件 */
.rate-container {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.rate-text {
  font-size: 12px;
  color: #667eea;
  font-weight: 500;
  padding: 2px 8px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 4px;
  display: inline-block;
}

/* 压力滑块 */
.stress-container {
  width: 100%;
}

.slider-wrapper {
  margin-bottom: 12px;
}

.stress-slider {
  width: 100%;
}

.stress-slider :deep(.el-slider__bar) {
  background: linear-gradient(90deg, #67C23A, #E6A23C, #F56C6C);
  height: 4px;
}

.stress-slider :deep(.el-slider__button) {
  border: 2px solid #667eea;
  width: 14px;
  height: 14px;
}

.stress-indicator {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding: 0 10px;
}

.indicator-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex: 1;
  opacity: 0.5;
  transition: all 0.2s ease;
}

.indicator-item.active {
  opacity: 1;
}

.indicator-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.indicator-text {
  font-size: 11px;
  color: #606266;
  font-weight: 500;
}

.indicator-item.active .indicator-text {
  color: #303133;
  font-weight: 600;
}

/* 主要困扰 */
.concerns-container {
  width: 100%;
}

.concerns-checkbox-group {
  width: 100%;
}

.concerns-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 8px;
  margin-bottom: 12px;
}

.concern-checkbox {
  margin: 0 !important;
}

.concern-checkbox :deep(.el-checkbox__label) {
  font-size: 12px;
  padding-left: 6px;
}

.other-concerns {
  margin-top: 16px;
}

.other-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
  font-weight: 500;
  margin-bottom: 8px;
}

.label-icon {
  color: #667eea;
  font-size: 14px;
}

.other-textarea {
  width: 100%;
}

/* 文本框样式 */
.expectations-textarea,
.additional-textarea {
  width: 100%;
}

:deep(.el-textarea__inner) {
  font-size: 12px;
  line-height: 1.4;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: all 0.2s ease;
}

:deep(.el-textarea__inner):focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

/* 咨询经历选择器 */
.therapy-select {
  max-width: 200px;
}

/* 表单操作按钮 */
.form-actions {
  padding: 20px 24px;
  text-align: center;
  border-top: 1px solid #ebeef5;
}

.submit-btn {
  padding: 8px 24px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.submit-btn:hover {
  opacity: 0.9;
}

.cancel-btn {
  padding: 8px 24px;
  font-size: 14px;
  border-radius: 6px;
  margin-left: 12px;
  border: 1px solid #dcdfe6;
}

.cancel-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-container {
    padding: 12px;
  }
  
  .header-card {
    padding: 16px 20px;
  }
  
  .form-section {
    padding: 16px 20px;
  }
  
  .form-col {
    min-width: 100%;
  }
  
  .concerns-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 6px;
  }
  
  .gender-radio {
    flex-direction: column;
    gap: 8px;
  }
  
  .radio-item {
    width: 100%;
    justify-content: center;
  }
  
  .stress-indicator {
    padding: 0 5px;
  }
  
  .indicator-text {
    font-size: 10px;
  }
  
  .form-actions {
    padding: 16px 20px;
  }
  
  .submit-btn,
  .cancel-btn {
    width: 100%;
    margin: 0 0 8px 0;
  }
  
  .submit-btn {
    margin-bottom: 8px;
  }
}

@media (max-width: 480px) {
  .main-title {
    font-size: 16px;
  }
  
  .header-description {
    font-size: 12px;
  }
  
  .section-title {
    font-size: 14px;
  }
  
  :deep(.el-form-item__label) {
    font-size: 13px;
  }
  
  .concerns-grid {
    grid-template-columns: 1fr;
  }
  
  .therapy-select {
    max-width: 100%;
  }
  
  .age-input {
    max-width: 100%;
  }
}
</style>