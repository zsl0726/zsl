<template>
  <div class="app-container">
    <div class="profile-container">
      <!-- 头部区域 -->
      <div class="header-card">
        <div class="header-content">
          <div class="header-left">
            <div class="title-section">
              <i class="el-icon-user-solid title-icon"></i>
              <h2 class="main-title">我的心理档案</h2>
              <el-tag 
                v-if="hasProfile && !loading && isVisibleToConsultant !== null"
                :type="visibilityType" 
                size="small"
                class="visibility-tag">
                {{ isVisibleToConsultant ? '咨询师可见' : '仅自己可见' }}
              </el-tag>
            </div>
            <p class="header-description">记录您的心理健康状态，为心理咨询提供参考</p>
          </div>
          <div class="header-right">
            <el-button-group class="action-group">
              <el-button 
                v-if="!hasProfile && !loading"
                type="primary" 
                icon="el-icon-plus"
                @click="handleCreate"
                class="create-btn">
                创建档案
              </el-button>
              <template v-else-if="hasProfile">
                <el-button 
                  v-if="isVisibleToConsultant !== null"
                  :type="isVisibleToConsultant ? 'success' : 'info'"
                  :loading="visibilityLoading"
                  @click="toggleVisibility"
                  class="visibility-btn"
                  :icon="isVisibleToConsultant ? 'el-icon-view' : 'el-icon-lock'">
                  {{ visibilityText }}
                </el-button>
                <el-button 
                  type="primary"
                  icon="el-icon-edit"
                  @click="handleEdit"
                  class="edit-btn">
                  编辑档案
                </el-button>
              </template>
            </el-button-group>
          </div>
        </div>
      </div>

      <!-- 可见性提示 -->
      <div v-if="hasProfile && !loading && isVisibleToConsultant !== null" class="visibility-alert-section">
        <el-alert 
          :title="visibilityAlert"
          :type="visibilityAlertType"
          :closable="false"
          show-icon
          :icon="isVisibleToConsultant ? 'el-icon-success' : 'el-icon-warning'"
          class="visibility-alert">
        </el-alert>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="loading-section">
        <div class="skeleton-grid">
          <el-skeleton animated class="skeleton-card" />
          <el-skeleton animated class="skeleton-card" />
          <el-skeleton animated class="skeleton-card" />
        </div>
      </div>

      <!-- 无档案状态 -->
      <div v-else-if="!hasProfile" class="empty-section">
        <div class="empty-content">
          <div class="empty-illustration">
            <i class="el-icon-user empty-icon"></i>
            <div class="empty-graphic">
              <div class="graphic-circle"></div>
              <div class="graphic-line"></div>
            </div>
          </div>
          <div class="empty-text">
            <h3 class="empty-title">尚未创建心理档案</h3>
            <p class="empty-desc">创建个人心理档案，获得更精准的心理咨询服务</p>
          </div>
          <el-button 
            type="primary" 
            icon="el-icon-edit"
            @click="handleCreate"
            size="medium"
            class="empty-action-btn">
            立即创建心理档案
          </el-button>
        </div>
      </div>

      <!-- 有档案内容 -->
      <div v-else class="content-section">
        <!-- 基本信息卡片 -->
        <div class="content-grid">
          <el-card shadow="hover" class="info-card basic-info-card">
            <div slot="header" class="card-header">
              <div class="card-title">
                <i class="el-icon-user card-icon"></i>
                <span>基本信息</span>
              </div>
              <el-tag type="info" size="mini" effect="plain">个人资料</el-tag>
            </div>
            <div class="card-content">
              <div class="info-grid">
                <div class="info-row">
                  <div class="info-item">
                    <div class="item-label">
                      <i class="el-icon-user item-icon"></i>
                      姓名
                    </div>
                    <div class="item-value">{{ profile.realName || '未填写' }}</div>
                  </div>
                  <div class="info-item">
                    <div class="item-label">
                      <i class="el-icon-female item-icon"></i>
                      性别
                    </div>
                    <div class="item-value">{{ getGenderName(profile.gender) }}</div>
                  </div>
                </div>
                <div class="info-row">
                  <div class="info-item">
                    <div class="item-label">
                      <i class="el-icon-time item-icon"></i>
                      年龄
                    </div>
                    <div class="item-value">{{ profile.age || '未填写' }}</div>
                  </div>
                  <div class="info-item">
                    <div class="item-label">
                      <i class="el-icon-office-building item-icon"></i>
                      职业
                    </div>
                    <div class="item-value">{{ profile.occupation || '未填写' }}</div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 健康指标卡片 -->
          <el-card shadow="hover" class="info-card health-metrics-card">
            <div slot="header" class="card-header">
              <div class="card-title">
                <i class="el-icon-data-line card-icon"></i>
                <span>健康指标</span>
              </div>
              <el-tag type="warning" size="mini" effect="plain">实时状态</el-tag>
            </div>
            <div class="card-content">
              <div class="metrics-grid">
                <div class="metric-item">
                  <div class="metric-title">
                    <i class="el-icon-chat-dot-round metric-icon"></i>
                    情绪状态
                  </div>
                  <div class="metric-content">
                    <div class="metric-value">
                      <el-rate
                        v-model="profile.emotionalState"
                        disabled
                        :max="5"
                        :colors="['#F56C6C', '#F7BA2A', '#67C23A']"
                        class="metric-rate"
                      />
                    </div>
                    <div class="metric-label">{{ getEmotionLabel(profile.emotionalState) }}</div>
                  </div>
                </div>
                <div class="metric-item">
                  <div class="metric-title">
                    <i class="el-icon-moon-night metric-icon"></i>
                    睡眠质量
                  </div>
                  <div class="metric-content">
                    <div class="metric-value">
                      <el-rate
                        v-model="profile.sleepQuality"
                        disabled
                        :max="5"
                        :colors="['#F56C6C', '#F7BA2A', '#409EFF']"
                        class="metric-rate"
                      />
                    </div>
                    <div class="metric-label">{{ getSleepLabel(profile.sleepQuality) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 压力水平卡片 -->
          <el-card shadow="hover" class="info-card stress-card">
            <div slot="header" class="card-header">
              <div class="card-title">
                <i class="el-icon-warning-outline card-icon"></i>
                <span>压力水平</span>
              </div>
              <el-tag :type="getStressTagType(profile.stressLevel)" size="mini" effect="light">
                {{ getStressLevelText(profile.stressLevel) }}
              </el-tag>
            </div>
            <div class="card-content">
              <div v-if="profile.stressLevel !== undefined && profile.stressLevel !== null" class="stress-content">
                <div class="stress-progress">
                  <el-progress 
                    :percentage="profile.stressLevel" 
                    :color="getStressColor(profile.stressLevel)"
                    :stroke-width="12"
                    :show-text="false"
                    class="stress-progress-bar">
                  </el-progress>
                  <div class="stress-info">
                    <span class="stress-value">{{ profile.stressLevel }}</span>
                    <span class="stress-label">/100</span>
                  </div>
                </div>
                <div class="stress-description">
                  <div class="stress-levels">
                    <div class="level-item" :class="{ active: profile.stressLevel < 30 }">
                      <div class="level-dot"></div>
                      <span class="level-text">轻度</span>
                    </div>
                    <div class="level-item" :class="{ active: profile.stressLevel >= 30 && profile.stressLevel < 60 }">
                      <div class="level-dot"></div>
                      <span class="level-text">中度</span>
                    </div>
                    <div class="level-item" :class="{ active: profile.stressLevel >= 60 && profile.stressLevel < 80 }">
                      <div class="level-dot"></div>
                      <span class="level-text">重度</span>
                    </div>
                    <div class="level-item" :class="{ active: profile.stressLevel >= 80 }">
                      <div class="level-dot"></div>
                      <span class="level-text">极高</span>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="empty-stress">
                <i class="el-icon-document-empty empty-icon"></i>
                <span class="empty-text">未评估</span>
              </div>
            </div>
          </el-card>

          <!-- 主要困扰卡片 -->
          <el-card shadow="hover" class="info-card concerns-card">
            <div slot="header" class="card-header">
              <div class="card-title">
                <i class="el-icon-question card-icon"></i>
                <span>主要困扰</span>
              </div>
              <el-tag type="warning" size="mini" effect="plain">关注点</el-tag>
            </div>
            <div class="card-content">
              <div v-if="profile.mainConcernsArray && profile.mainConcernsArray.length > 0" class="concerns-content">
                <el-tag 
                  v-for="(concern, index) in profile.mainConcernsArray" 
                  :key="index"
                  type="warning"
                  size="small"
                  class="concern-tag">
                  {{ concern }}
                </el-tag>
              </div>
              <div v-else class="empty-concerns">
                <i class="el-icon-document-empty empty-icon"></i>
                <span class="empty-text">暂未填写主要困扰</span>
              </div>
            </div>
          </el-card>

          <!-- 补充信息卡片 -->
          <el-card shadow="hover" class="info-card additional-card">
            <div slot="header" class="card-header">
              <div class="card-title">
                <i class="el-icon-info card-icon"></i>
                <span>补充信息</span>
              </div>
              <el-tag type="primary" size="mini" effect="plain">详细信息</el-tag>
            </div>
            <div class="card-content">
              <div class="additional-grid">
                <div class="additional-item">
                  <div class="additional-label">
                    <i class="el-icon-chat-line-square label-icon"></i>
                    心理咨询经历
                  </div>
                  <div class="additional-value">{{ profile.therapyExperience || '未填写' }}</div>
                </div>
                <div class="additional-item">
                  <div class="additional-label">
                    <i class="el-icon-aim label-icon"></i>
                    期望帮助
                  </div>
                  <div class="additional-value">{{ profile.expectations || '未填写' }}</div>
                </div>
                <div class="additional-item">
                  <div class="additional-label">
                    <i class="el-icon-chat-line-round label-icon"></i>
                    其他困扰
                  </div>
                  <div class="additional-value">{{ profile.otherConcerns || '未填写' }}</div>
                </div>
                <div class="additional-item">
                  <div class="additional-label">
                    <i class="el-icon-edit label-icon"></i>
                    补充说明
                  </div>
                  <div class="additional-value">{{ profile.additionalNotes || '未填写' }}</div>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { 
  getMyProfile, 
  checkProfile,
  updateProfileVisibility,
  getProfileVisibility 
} from "@/api/mental/profile";

export default {
  name: "MentalProfileIndex",
  data() {
    return {
      profile: {},
      loading: false,
      isVisibleToConsultant: null,
      visibilityLoading: false
    };
  },
  computed: {
    hasProfile() {
      return !!(this.profile && this.profile.id);
    },
    
    visibilityType() {
      if (this.isVisibleToConsultant === null) return 'info';
      return this.isVisibleToConsultant ? 'success' : 'info';
    },
    
    visibilityText() {
      if (this.isVisibleToConsultant === null) return '加载中...';
      return this.isVisibleToConsultant ? '设为私密' : '设为公开';
    },
    
    visibilityAlert() {
      if (this.isVisibleToConsultant === null) return '';
      return this.isVisibleToConsultant 
        ? '您的心理档案对咨询师可见，咨询师可以查看以提供更好的服务' 
        : '您的心理档案仅自己可见，咨询师无法查看';
    },
    
    visibilityAlertType() {
      if (this.isVisibleToConsultant === null) return 'info';
      return this.isVisibleToConsultant ? 'success' : 'warning';
    }
  },
  created() {
    this.loadProfile();
  },
  watch: {
    $route(to, from) {
      if (from.name === 'MentalProfileEdit' || to.query.refresh === 'true') {
        this.loadProfile();
        if (to.query.refresh === 'true') {
          this.$router.replace({ query: {} });
        }
      }
    }
  },
  methods: {
    async loadProfile() {
      this.loading = true;
      
      try {
        const [checkRes, profileRes] = await Promise.all([
          checkProfile(), 
          getMyProfile()
        ]);
        
        let hasProfile = false;
        
        if (checkRes.code === 200) {
          hasProfile = checkRes.data === true;
        }
        
        if (hasProfile || (profileRes.code === 200 && profileRes.data && profileRes.data.id)) {
          this.profile = profileRes.data || {};
          localStorage.setItem('hasMentalProfile', 'true');
          await this.loadVisibility();
        } else {
          this.profile = {};
          localStorage.setItem('hasMentalProfile', 'false');
          this.isVisibleToConsultant = null;
        }
        
      } catch (error) {
        console.error('加载失败:', error);
        this.$modal.msgError('加载失败，请稍后重试');
        this.profile = {};
        this.isVisibleToConsultant = null;
      } finally {
        this.loading = false;
      }
    },
    
    async loadVisibility() {
      try {
        const res = await getProfileVisibility();
        
        if (res && res.code === 200) {
          if (typeof res.data === 'boolean') {
            this.isVisibleToConsultant = res.data;
          } else if (res.data && typeof res.data.visible === 'boolean') {
            this.isVisibleToConsultant = res.data.visible;
          } else if (res.data && typeof res.data.isVisibleToConsultant === 'boolean') {
            this.isVisibleToConsultant = res.data.isVisibleToConsultant;
          } else if (res.data && data.isVisible !== undefined) {
            this.isVisibleToConsultant = data.isVisible;
          } else {
            this.isVisibleToConsultant = !!(this.profile && this.profile.id);
          }
        } else {
          this.isVisibleToConsultant = true;
        }
      } catch (error) {
        console.error('加载可见性设置失败:', error);
        this.isVisibleToConsultant = true;
      }
    },
    
    async toggleVisibility() {
      if (this.isVisibleToConsultant === null) return;
      
      this.visibilityLoading = true;
      try {
        const newVisibility = !this.isVisibleToConsultant;
        
        const res = await updateProfileVisibility(newVisibility);
        
        if (res.code === 200) {
          this.isVisibleToConsultant = newVisibility;
          this.$modal.msgSuccess(
            newVisibility 
              ? '已设置为对咨询师可见' 
              : '已设置为仅自己可见'
          );
        } else {
          this.$modal.msgError(res.msg || '操作失败');
        }
      } catch (error) {
        console.error('更新可见性失败:', error);
        this.$modal.msgError('操作失败，请稍后重试');
      } finally {
        this.visibilityLoading = false;
      }
    },
    
    handleCreate() {
      this.$router.push({ name: 'MentalProfileEdit' });
    },
    
    handleEdit() {
      this.$router.push({ name: 'MentalProfileEdit' });
    },
    
    getGenderName(gender) {
      if (gender === 0 || gender === '0') return '男';
      if (gender === 1 || gender === '1') return '女';
      if (gender === 2 || gender === '2') return '不愿透露';
      return '未填写';
    },
    
    getEmotionLabel(level) {
      const labels = ['非常差', '较差', '一般', '良好', '非常好'];
      return (level >= 1 && level <= 5) ? labels[level - 1] : '未填写';
    },
    
    getSleepLabel(level) {
      const labels = ['非常差', '较差', '一般', '良好', '非常好'];
      return (level >= 1 && level <= 5) ? labels[level - 1] : '未填写';
    },
    
    getStressColor(level) {
      if (level === undefined || level === null) return '#909399';
      if (level < 30) return '#67C23A';
      if (level < 60) return '#E6A23C';
      if (level < 80) return '#F56C6C';
      return '#909399';
    },
    
    getStressTagType(level) {
      if (level === undefined || level === null) return 'info';
      if (level < 30) return 'success';
      if (level < 60) return 'warning';
      if (level < 80) return 'danger';
      return 'info';
    },
    
    getStressLevelText(level) {
      if (level === undefined || level === null) return '未评估';
      if (level < 30) return '轻度压力';
      if (level < 60) return '中度压力';
      if (level < 80) return '重度压力';
      return '极高压力';
    }
  }
};
</script>

<style scoped>
.app-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  min-height: 100vh;
}

.profile-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 头部区域 */
.header-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px 32px;
  margin-bottom: 20px;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.header-left {
  flex: 1;
  min-width: 300px;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.title-icon {
  font-size: 28px;
  color: white;
}

.main-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: white;
}

.visibility-tag {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  font-weight: 500;
}

.header-description {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.action-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.create-btn,
.edit-btn,
.visibility-btn {
  border-radius: 8px;
  font-weight: 500;
  padding: 10px 20px;
  border: none;
}

.create-btn,
.edit-btn {
  background: white;
  color: #667eea;
}

.create-btn:hover,
.edit-btn:hover {
  background: rgba(255, 255, 255, 0.9);
  color: #667eea;
}

.visibility-btn {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.visibility-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}

/* 可见性提示 */
.visibility-alert-section {
  margin-bottom: 20px;
}

.visibility-alert {
  border-radius: 8px;
  border: none;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 加载状态 */
.loading-section {
  padding: 40px 0;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.skeleton-card {
  height: 200px;
  border-radius: 12px;
}

/* 空状态 */
.empty-section {
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.empty-content {
  text-align: center;
  max-width: 400px;
  margin: 0 auto;
}

.empty-illustration {
  position: relative;
  margin-bottom: 24px;
}

.empty-icon {
  font-size: 80px;
  color: #667eea;
  opacity: 0.8;
}

.empty-graphic {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 120px;
  height: 120px;
}

.graphic-circle {
  width: 120px;
  height: 120px;
  border: 2px dashed #667eea;
  border-radius: 50%;
  opacity: 0.3;
  animation: pulse 2s infinite;
}

.graphic-line {
  position: absolute;
  top: 0;
  left: 50%;
  width: 2px;
  height: 60px;
  background: linear-gradient(180deg, transparent, #667eea, transparent);
  opacity: 0.5;
}

@keyframes pulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.6; }
}

.empty-text {
  margin-bottom: 24px;
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #909399;
  line-height: 1.6;
}

.empty-action-btn {
  padding: 12px 32px;
  border-radius: 8px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.empty-action-btn:hover {
  opacity: 0.9;
}

/* 内容区域 */
.content-section {
  background: transparent;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 20px;
}

.info-card {
  border-radius: 12px;
  border: 1px solid #f0f2f5;
  background: white;
  transition: all 0.3s ease;
  height: 100%;
  min-height: 220px;
  display: flex;
  flex-direction: column;
}

.info-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f2f5;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #303133;
}

.card-icon {
  font-size: 18px;
  color: #667eea;
}

.card-content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 基本信息卡片 */
.basic-info-card .info-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.item-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

.item-icon {
  font-size: 14px;
  color: #667eea;
  opacity: 0.8;
}

.item-value {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 8px;
  min-height: 36px;
  display: flex;
  align-items: center;
}

/* 健康指标卡片 */
.health-metrics-card .metrics-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.metric-item {
  border-bottom: 1px solid #f0f2f5;
  padding-bottom: 16px;
}

.metric-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.metric-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 12px;
}

.metric-icon {
  font-size: 16px;
}

.metric-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metric-value {
  flex: 1;
}

.metric-rate {
  font-size: 18px;
}

.metric-label {
  font-size: 13px;
  font-weight: 500;
  color: #909399;
  margin-left: 16px;
  min-width: 60px;
  text-align: right;
}

/* 压力水平卡片 */
.stress-card .stress-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.stress-progress {
  margin-bottom: 20px;
}

.stress-progress-bar {
  margin-bottom: 12px;
}

.stress-info {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
}

.stress-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.stress-label {
  font-size: 16px;
  color: #909399;
}

.stress-description {
  margin-top: auto;
}

.stress-levels {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px;
}

.level-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.level-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #dcdfe6;
  transition: all 0.3s ease;
}

.level-item.active .level-dot {
  width: 12px;
  height: 12px;
  background: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.level-text {
  font-size: 12px;
  color: #909399;
  transition: all 0.3s ease;
}

.level-item.active .level-text {
  color: #667eea;
  font-weight: 500;
}

.empty-stress {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex: 1;
  color: #c0c4cc;
}

/* 主要困扰卡片 */
.concerns-card .card-content {
  min-height: 180px;
}

.concerns-content {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: flex-start;
}

.concern-tag {
  padding: 6px 12px;
  border-radius: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border: none;
  color: #92400e;
  margin: 0;
}

.empty-concerns {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex: 1;
  color: #c0c4cc;
}

.empty-concerns .empty-icon {
  font-size: 40px;
}

/* 补充信息卡片 */
.additional-card .additional-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.additional-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.additional-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.label-icon {
  font-size: 14px;
  color: #667eea;
  opacity: 0.8;
}

.additional-value {
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
  padding: 10px 12px;
  background: #f8fafc;
  border-radius: 8px;
  min-height: 50px;
  display: flex;
  align-items: center;
}

/* 空状态通用样式 */
.empty-icon {
  font-size: 32px;
  color: #c0c4cc;
  opacity: 0.6;
}

.empty-text {
  font-size: 14px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-container {
    padding: 16px;
  }
  
  .header-card {
    padding: 20px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-right {
    width: 100%;
    justify-content: flex-start;
  }
  
  .action-group {
    width: 100%;
  }
  
  .create-btn,
  .edit-btn,
  .visibility-btn {
    flex: 1;
    justify-content: center;
  }
  
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .info-row {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}

@media (max-width: 480px) {
  .main-title {
    font-size: 20px;
  }
  
  .header-description {
    font-size: 13px;
  }
  
  .card-header {
    padding: 14px 16px;
  }
  
  .card-content {
    padding: 16px;
  }
}
</style>