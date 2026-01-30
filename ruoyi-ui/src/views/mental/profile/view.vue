<template>
  <div class="app-container">
    <div class="detail-container">
      <!-- 头部区域 -->
      <div class="header-card">
        <div class="header-content">
          <div class="header-left">
            <div class="title-section">

              <h2 class="main-title">心理档案详情</h2>
            </div>
            <div class="header-info">
              <span class="info-item">
                <i class="el-icon-user info-icon"></i>
                <span>{{ profile.realName || '匿名用户' }}</span>
              </span>
              <span class="info-item">
                <i class="el-icon-time info-icon"></i>
                <span>{{ profile.createTime ? parseTime(profile.createTime) : '未知' }}</span>
              </span>
              <el-tag type="success" size="small" effect="plain" class="status-tag">
                正常档案
              </el-tag>
            </div>
          </div>
          <div class="header-right">
            <el-button 
              v-if="profile.userId === id"
              type="primary" 
              @click="handleEdit"
              size="small"
              icon="el-icon-edit"
              class="edit-btn">
              编辑档案
            </el-button>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-section">
        <div class="skeleton-grid">
          <el-skeleton animated class="skeleton-card" />
          <el-skeleton animated class="skeleton-card" />
          <el-skeleton animated class="skeleton-card" />
        </div>
      </div>

      <!-- 档案内容 -->
      <div v-else class="content-grid">
        <!-- 个人信息卡片 -->
        <el-card shadow="hover" class="info-card basic-card">
          <div slot="header" class="card-header">
            <div class="card-title">
              <i class="el-icon-user card-icon"></i>
              <h3>个人信息</h3>
            </div>
          </div>
          <div class="card-content">
            <div class="info-grid">
              <div class="info-item">
                <div class="item-label">姓名</div>
                <div class="item-value">{{ profile.realName || '未填写' }}</div>
              </div>
              <div class="info-item">
                <div class="item-label">性别</div>
                <div class="item-value">{{ getGenderName(profile.gender) }}</div>
              </div>
              <div class="info-item">
                <div class="item-label">年龄</div>
                <div class="item-value">{{ profile.age || '未填写' }}</div>
              </div>
              <div class="info-item">
                <div class="item-label">职业</div>
                <div class="item-value">{{ profile.occupation || '未填写' }}</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 健康状态卡片 -->
        <el-card shadow="hover" class="info-card health-card">
          <div slot="header" class="card-header">
            <div class="card-title">
              <i class="el-icon-data-line card-icon"></i>
              <h3>健康状态</h3>
            </div>
          </div>
          <div class="card-content">
            <div class="health-metrics">
              <!-- 情绪状态 -->
              <div class="metric-item">
                <div class="metric-header">
                  <div class="metric-title">
                    <i class="el-icon-chat-dot-round metric-icon"></i>
                    情绪状态
                  </div>
                  <div class="metric-label">{{ getEmotionLabel(profile.emotionalState) }}</div>
                </div>
                <div class="metric-content">
                  <el-rate
                    :value="profile.emotionalState"
                    :max="5"
                    disabled
                    :colors="['#F56C6C', '#F56C6C', '#E6A23C', '#67C23A', '#67C23A']"
                    class="metric-rate">
                  </el-rate>
                </div>
              </div>

              <!-- 睡眠质量 -->
              <div class="metric-item">
                <div class="metric-header">
                  <div class="metric-title">
                    <i class="el-icon-moon-night metric-icon"></i>
                    睡眠质量
                  </div>
                  <div class="metric-label">{{ getSleepLabel(profile.sleepQuality) }}</div>
                </div>
                <div class="metric-content">
                  <el-rate
                    :value="profile.sleepQuality"
                    :max="5"
                    disabled
                    :colors="['#F56C6C', '#F56C6C', '#E6A23C', '#67C23A', '#67C23A']"
                    class="metric-rate">
                  </el-rate>
                </div>
              </div>

              <!-- 压力水平 -->
              <div class="metric-item">
                <div class="metric-header">
                  <div class="metric-title">
                    <i class="el-icon-warning-outline metric-icon"></i>
                    压力水平
                  </div>
                  <div class="metric-label">{{ getStressLevelText(profile.stressLevel) }}</div>
                </div>
                <div class="metric-content">
                  <div class="stress-container">
                    <div class="stress-value">
                      <span class="value-number">{{ profile.stressLevel || 0 }}</span>
                      <span class="value-label">/100</span>
                    </div>
                    <div class="stress-bar">
                      <el-progress 
                        :percentage="profile.stressLevel || 0" 
                        :color="getStressColor(profile.stressLevel)"
                        :stroke-width="8"
                        :show-text="false"
                        class="stress-progress">
                      </el-progress>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 主要困扰卡片 -->
        <el-card shadow="hover" class="info-card concerns-card">
          <div slot="header" class="card-header">
            <div class="card-title">
              <i class="el-icon-question card-icon"></i>
              <h3>主要困扰</h3>
            </div>
          </div>
          <div class="card-content">
            <div class="concerns-container">
              <div v-if="profile.mainConcernsArray && profile.mainConcernsArray.length > 0">
                <div class="concerns-tags">
                  <el-tag 
                    v-for="(concern, index) in profile.mainConcernsArray" 
                    :key="index"
                    type="warning"
                    size="small"
                    class="concern-tag">
                    {{ concern }}
                  </el-tag>
                </div>
                <div v-if="profile.otherConcerns" class="other-concerns">
                  <div class="other-label">其他困扰：</div>
                  <div class="other-content">{{ profile.otherConcerns }}</div>
                </div>
              </div>
              <div v-else class="empty-content">
                <i class="el-icon-document-empty empty-icon"></i>
                <span class="empty-text">暂未填写主要困扰</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 期望帮助卡片 -->
        <el-card shadow="hover" class="info-card expectations-card">
          <div slot="header" class="card-header">
            <div class="card-title">
              <i class="el-icon-aim card-icon"></i>
              <h3>期望帮助</h3>
            </div>
          </div>
          <div class="card-content">
            <div v-if="profile.expectations" class="expectations-content">
              {{ profile.expectations }}
            </div>
            <div v-else class="empty-content">
              <i class="el-icon-chat-line-square empty-icon"></i>
              <span class="empty-text">暂未填写期望帮助</span>
            </div>
          </div>
        </el-card>

        <!-- 咨询经历卡片 -->
        <el-card shadow="hover" class="info-card experience-card">
          <div slot="header" class="card-header">
            <div class="card-title">
              <i class="el-icon-chat-line-round card-icon"></i>
              <h3>咨询经历</h3>
            </div>
          </div>
          <div class="card-content">
            <div class="experience-content">
              {{ profile.therapyExperience || '从未有过心理咨询经历' }}
            </div>
          </div>
        </el-card>

        <!-- 补充说明卡片 -->
        <el-card shadow="hover" class="info-card additional-card">
          <div slot="header" class="card-header">
            <div class="card-title">
              <i class="el-icon-info card-icon"></i>
              <h3>补充说明</h3>
            </div>
          </div>
          <div class="card-content">
            <div v-if="profile.additionalNotes" class="additional-content">
              {{ profile.additionalNotes }}
            </div>
            <div v-else class="empty-content">
              <i class="el-icon-edit-outline empty-icon"></i>
              <span class="empty-text">暂无补充说明</span>
            </div>
          </div>
        </el-card>

        <!-- 推荐服务卡片 -->
        <el-card v-if="profile.preference" shadow="hover" class="info-card recommendation-card">
          <div slot="header" class="card-header">
            <div class="card-title">
              <i class="el-icon-star card-icon"></i>
              <h3>推荐服务</h3>
            </div>
          </div>
          <div class="card-content">
            <div class="recommendation-tags">
              <el-tag 
                v-for="(service, index) in profile.preference.split(',')" 
                :key="index"
                type="success"
                size="small"
                class="service-tag">
                {{ service }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
import { getMyProfile } from "@/api/mental/profile";
import { parseTime } from "@/utils/ruoyi";
import { mapGetters } from 'vuex';

export default {
  name: "MentalProfileView",
  computed: {
    ...mapGetters(['id'])
  },
  data() {
    return {
      profile: {},
      loading: false
    };
  },
  created() {
    this.loadProfile();
  },
  methods: {
    loadProfile() {
      this.loading = true;
      getMyProfile()
        .then(response => {
          if (response.code === 200 && response.data && typeof response.data === 'object' && response.data.id) {
            this.profile = response.data;
          } else {
            this.$modal.msgWarning("未找到您的心理档案");
            this.$router.push({ name: 'MentalProfileIndex' });
          }
        })
        .catch(error => {
          console.error('加载档案失败:', error);
          this.$modal.msgError('加载失败：' + (error.response?.data?.msg || '系统异常'));
          this.$router.push({ name: 'MentalProfileIndex' });
        })
        .finally(() => {
          this.loading = false;
        });
    },
    
    handleEdit() {
      this.$router.push({ name: 'MentalProfileEdit' });
    },
    
    getGenderName(gender) {
      const genderMap = { '0': '男', '1': '女', '2': '不愿透露' };
      return genderMap[gender] || '未填写';
    },
    
    getEmotionLabel(level) {
      const labels = ['非常差', '较差', '一般', '良好', '非常好'];
      return level ? labels[level - 1] : '未填写';
    },
    
    getSleepLabel(level) {
      const labels = ['非常差', '较差', '一般', '良好', '非常好'];
      return level ? labels[level - 1] : '未填写';
    },
    
    getStressColor(level) {
      if (!level) return '#909399';
      if (level < 30) return '#67C23A';
      if (level < 60) return '#E6A23C';
      if (level < 80) return '#F56C6C';
      return '#909399';
    },
    
    getStressTagType(level) {
      if (!level) return 'info';
      if (level < 30) return 'success';
      if (level < 60) return 'warning';
      if (level < 80) return 'danger';
      return 'info';
    },
    
    getStressLevelText(level) {
      if (!level) return '未评估';
      if (level < 30) return '轻度压力';
      if (level < 60) return '中度压力';
      if (level < 80) return '重度压力';
      return '极高压力';
    },
    
    parseTime
  }
};
</script>

<style scoped>
.app-container {
  padding: 16px;
  background: #f5f7fa;
  min-height: 100vh;
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 头部区域 */
.header-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 16px;
  color: white;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  flex: 1;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.back-btn {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 8px;
  min-width: 32px;
}

.main-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: white;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
}

.info-icon {
  font-size: 14px;
  opacity: 0.8;
}

.status-tag {
  background: rgba(255, 255, 255, 0.15);
  border: none;
  color: white;
}

.header-right {
  display: flex;
  align-items: center;
}

.edit-btn {
  background: white;
  color: #667eea;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  padding: 8px 16px;
}

.edit-btn:hover {
  background: rgba(255, 255, 255, 0.9);
}

/* 加载状态 */
.loading-section {
  padding: 24px 0;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.skeleton-card {
  height: 180px;
  border-radius: 8px;
}

/* 内容网格布局 */
.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 16px;
}

.info-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
  transition: all 0.3s ease;
  height: 100%;
  min-height: 180px;
  display: flex;
  flex-direction: column;
}

.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f2f5;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.card-icon {
  font-size: 16px;
  color: #667eea;
}

.card-content {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 基本信息卡片 */
.basic-card .info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.item-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  padding: 6px 8px;
  background: #f8fafc;
  border-radius: 4px;
}

/* 健康状态卡片 */
.health-card .health-metrics {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.metric-item {
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
}

.metric-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.metric-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #606266;
}

.metric-icon {
  font-size: 14px;
  color: #667eea;
}

.metric-label {
  font-size: 12px;
  font-weight: 500;
  padding: 2px 8px;
  background: #f8fafc;
  border-radius: 4px;
  color: #909399;
}

.metric-content {
  margin-top: 4px;
}

.metric-rate {
  font-size: 16px;
}

.stress-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stress-value {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.value-number {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.value-label {
  font-size: 12px;
  color: #909399;
}

.stress-bar {
  width: 100%;
}

.stress-progress {
  margin: 0;
}

/* 主要困扰卡片 */
.concerns-card .card-content {
  min-height: 150px;
}

.concerns-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.concerns-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.concern-tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border: none;
  color: #92400e;
}

.other-concerns {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f2f5;
}

.other-label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
  margin-bottom: 6px;
}

.other-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

/* 期望帮助卡片 */
.expectations-card .card-content {
  min-height: 150px;
}

.expectations-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  flex: 1;
}

/* 咨询经历卡片 */
.experience-card .card-content {
  min-height: 120px;
}

.experience-content {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
  flex: 1;
  display: flex;
  align-items: center;
}

/* 补充说明卡片 */
.additional-card .card-content {
  min-height: 150px;
}

.additional-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  flex: 1;
}

/* 推荐服务卡片 */
.recommendation-card .card-content {
  min-height: 120px;
}

.recommendation-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.service-tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  border: none;
  color: #065f46;
}

/* 空状态 */
.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex: 1;
  color: #c0c4cc;
}

.empty-icon {
  font-size: 24px;
  opacity: 0.6;
}

.empty-text {
  font-size: 13px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-container {
    padding: 12px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .header-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .header-right {
    width: 100%;
    justify-content: flex-start;
  }
  
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .basic-card .info-grid {
    grid-template-columns: 1fr;
  }
  
  .skeleton-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .main-title {
    font-size: 16px;
  }
  
  .header-info {
    font-size: 12px;
  }
  
  .card-header {
    padding: 10px 12px;
  }
  
  .card-content {
    padding: 12px;
  }
  
  .card-title {
    font-size: 14px;
  }
}
</style>