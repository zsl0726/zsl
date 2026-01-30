<template>
  <div class="app-container">
    <div class="detail-container">
      <!-- 头部区域 -->
      <div class="header-card">
        <div class="header-content">
          <div class="header-left">
            <div class="title-section">
              <el-button 
                @click="$router.go(-1)" 
                icon="el-icon-back" 
                size="small" 
                class="back-btn"
                circle>
              </el-button>
              <h2 class="main-title">心理档案详情</h2>
            </div>
            <div class="header-info">
              <span class="user-info">
                <i class="el-icon-user info-icon"></i>
                <span>{{ profile.realName || '匿名用户' }}</span>
              </span>
              <el-tag :type="profile.id ? 'success' : 'info'" size="small" effect="light" class="status-tag">
                {{ profile.id ? '已填写' : '未填写' }}
              </el-tag>
            </div>
          </div>
          <div class="header-right">
            <el-button-group class="action-group">
              <el-button 
                type="primary" 
                @click="addNote"
                :loading="noteLoading"
                size="small"
                icon="el-icon-edit"
                class="note-btn">
                添加备注
              </el-button>
              <el-button 
                type="success" 
                @click="downloadReport"
                size="small"
                icon="el-icon-download"
                class="export-btn">
                导出报告
              </el-button>
            </el-button-group>
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
      <div v-else>
        <!-- 用户基本信息卡片 -->
        <div class="content-grid">
          <el-card shadow="hover" class="info-card user-card">
            <div slot="header" class="card-header">
              <div class="card-title">
                <i class="el-icon-user card-icon"></i>
                <h3>用户信息</h3>
              </div>
            </div>
            <div class="card-content">
              <div class="user-grid">
                <div class="user-item">
                  <div class="item-label">用户ID</div>
                  <div class="item-value">{{ profile.userId }}</div>
                </div>
                <div class="user-item">
                  <div class="item-label">姓名</div>
                  <div class="item-value">{{ profile.realName || '未填写' }}</div>
                </div>
                <div class="user-item">
                  <div class="item-label">性别</div>
                  <div class="item-value">{{ getGenderName(profile.gender) }}</div>
                </div>
                <div class="user-item">
                  <div class="item-label">年龄</div>
                  <div class="item-value">{{ profile.age || '未填写' }}</div>
                </div>
                <div class="user-item">
                  <div class="item-label">职业</div>
                  <div class="item-value">{{ profile.occupation || '未填写' }}</div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 健康状态评估卡片 -->
          <el-card v-if="profile.id" shadow="hover" class="info-card health-card">
            <div slot="header" class="card-header">
              <div class="card-title">
                <i class="el-icon-data-line card-icon"></i>
                <h3>健康状态评估</h3>
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
                      v-model="profile.emotionalState"
                      disabled
                      :max="5"
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
                      v-model="profile.sleepQuality"
                      disabled
                      :max="5"
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
                      <div class="stress-progress">
                        <el-progress 
                          :percentage="profile.stressLevel || 0" 
                          :color="getStressColor(profile.stressLevel)"
                          :stroke-width="8"
                          :show-text="false"
                          class="stress-progress-bar">
                        </el-progress>
                      </div>
                      <div class="stress-value">
                        <span class="value-number">{{ profile.stressLevel || 0 }}</span>
                        <span class="value-label">/100</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 主要困扰卡片 -->
          <el-card v-if="profile.id" shadow="hover" class="info-card concerns-card">
            <div slot="header" class="card-header">
              <div class="card-title">
                <i class="el-icon-question card-icon"></i>
                <h3>主要困扰</h3>
              </div>
            </div>
            <div class="card-content">
              <div v-if="profile.mainConcernsArray && profile.mainConcernsArray.length > 0" class="concerns-container">
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
          </el-card>

          <!-- 咨询经历与期望卡片 -->
          <div v-if="profile.id" class="info-grid">
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
          </div>

          <!-- 补充说明卡片 -->
          <el-card v-if="profile.id" shadow="hover" class="info-card additional-card">
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

          <!-- 无档案提示 -->
          <el-card v-if="!profile.id" shadow="hover" class="info-card empty-card">
            <div class="empty-state">
              <i class="el-icon-user empty-main-icon"></i>
              <h3 class="empty-title">尚未创建心理档案</h3>
              <p class="empty-desc">该用户尚未填写心理档案</p>
            </div>
          </el-card>
        </div>

        <!-- 咨询师备注区域 -->
        <div class="notes-section">
          <el-card shadow="hover" class="notes-card">
            <div slot="header" class="card-header">
              <div class="card-title">
                <i class="el-icon-edit-outline card-icon"></i>
                <h3>咨询师备注</h3>
                <span class="notes-count">({{ notes.length }}条)</span>
              </div>
            </div>
            <div class="card-content">
              <div v-if="notes.length > 0" class="notes-list">
                <div 
                  v-for="(note, index) in notes" 
                  :key="index"
                  class="note-item">
                  <div class="note-header">
                    <div class="note-meta">
                      <span class="consultant-name">
                        <i class="el-icon-user consultant-icon"></i>
                        {{ note.consultantName || '咨询师' }}
                      </span>
                      <span class="note-time">{{ formatDate(note.createTime) }}</span>
                    </div>
                  </div>
                  <div class="note-content">{{ note.content }}</div>
                </div>
              </div>
              <div v-else class="empty-notes">
                <i class="el-icon-chat-line-square empty-icon"></i>
                <span class="empty-text">暂无备注记录</span>
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
  getConsultantProfileDetail, 
  getProfileNotes,
  addProfileNote 
} from "@/api/mental/profile";

export default {
  name: "ConsultantProfileView",
  data() {
    return {
      profile: {},
      loading: false,
      notes: [],
      noteLoading: false,
      userId: null
    };
  },
  computed: {
    pageTitle() {
      if (this.profile.realName) {
        return `${this.profile.realName}的心理档案`;
      }
      return '心理档案详情';
    }
  },
  created() {
    this.userId = this.$route.query.userId;
    if (this.userId) {
      this.loadProfile();
      this.loadNotes();
    } else {
      this.$modal.msgError('用户ID参数缺失');
      this.$router.go(-1);
    }
  },
  methods: {
    async loadProfile() {
      this.loading = true;
      try {
        const res = await getConsultantProfileDetail(this.userId);
        if (res.code === 200) {
          this.profile = res.data || {};
        } else {
          this.$modal.msgError(res.msg || '加载失败');
        }
      } catch (error) {
        console.error('加载档案失败:', error);
        this.$modal.msgError('加载失败');
      } finally {
        this.loading = false;
      }
    },
    
    async loadNotes() {
      try {
        const res = await getProfileNotes(this.userId);
        if (res.code === 200) {
          this.notes = res.data || [];
        }
      } catch (error) {
        console.error('加载备注失败:', error);
      }
    },
    
    async addNote() {
      this.$prompt('请输入备注内容', '添加备注', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPattern: /.+/,
        inputErrorMessage: '备注内容不能为空',
        inputPlaceholder: '请输入您对用户的观察、分析或建议...'
      }).then(async ({ value }) => {
        this.noteLoading = true;
        try {
          const res = await addProfileNote({
            userId: this.userId,
            content: value
          });
          
          if (res.code === 200) {
            this.$modal.msgSuccess('备注添加成功');
            this.loadNotes();
          } else {
            this.$modal.msgError(res.msg || '添加失败');
          }
        } catch (error) {
          this.$modal.msgError('添加失败');
        } finally {
          this.noteLoading = false;
        }
      }).catch(() => {});
    },
    
    downloadReport() {
      this.$modal.msgInfo('导出功能开发中，敬请期待');
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
    },
    
    formatDate(date) {
      if (!date) return '';
      
      try {
        const dateObj = new Date(date);
        if (isNaN(dateObj.getTime())) return '';
        
        const now = new Date();
        const diff = now.getTime() - dateObj.getTime();
        const diffDays = Math.floor(diff / (1000 * 60 * 60 * 24));
        
        if (diffDays === 0) {
          // 今天
          const hours = String(dateObj.getHours()).padStart(2, '0');
          const minutes = String(dateObj.getMinutes()).padStart(2, '0');
          return `今天 ${hours}:${minutes}`;
        } else if (diffDays === 1) {
          // 昨天
          const hours = String(dateObj.getHours()).padStart(2, '0');
          const minutes = String(dateObj.getMinutes()).padStart(2, '0');
          return `昨天 ${hours}:${minutes}`;
        } else if (diffDays < 7) {
          // 一周内
          const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
          const day = days[dateObj.getDay()];
          const hours = String(dateObj.getHours()).padStart(2, '0');
          const minutes = String(dateObj.getMinutes()).padStart(2, '0');
          return `${day} ${hours}:${minutes}`;
        } else {
          // 一周前
          const month = String(dateObj.getMonth() + 1).padStart(2, '0');
          const day = String(dateObj.getDate()).padStart(2, '0');
          const hours = String(dateObj.getHours()).padStart(2, '0');
          const minutes = String(dateObj.getMinutes()).padStart(2, '0');
          return `${month}-${day} ${hours}:${minutes}`;
        }
      } catch (error) {
        console.error('日期格式化错误:', error);
        return '';
      }
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
  align-items: flex-start;
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

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
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

.action-group {
  display: flex;
  gap: 8px;
}

.note-btn,
.export-btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 500;
}

.note-btn {
  background: white;
  color: #667eea;
  border: none;
}

.note-btn:hover {
  background: rgba(255, 255, 255, 0.9);
}

.export-btn {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.export-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}

/* 加载状态 */
.loading-section {
  padding: 40px 0;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.skeleton-card {
  height: 200px;
  border-radius: 8px;
}

/* 内容网格布局 */
.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
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

/* 用户信息卡片 */
.user-card .user-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.user-item {
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
  min-height: 32px;
  display: flex;
  align-items: center;
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
  align-items: center;
  gap: 12px;
}

.stress-progress {
  flex: 1;
}

.stress-progress-bar {
  margin: 0;
}

.stress-value {
  display: flex;
  align-items: baseline;
  gap: 2px;
  min-width: 60px;
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

/* 咨询经历与期望网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
  grid-column: 1 / -1;
}

.experience-card .card-content,
.expectations-card .card-content {
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

.expectations-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  flex: 1;
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

/* 无档案卡片 */
.empty-card {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.empty-main-icon {
  font-size: 60px;
  color: #c0c4cc;
  opacity: 0.6;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: #606266;
  margin: 0;
}

.empty-desc {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 空状态通用样式 */
.empty-content,
.empty-notes {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex: 1;
  color: #c0c4cc;
}

.empty-icon {
  font-size: 32px;
  opacity: 0.6;
}

.empty-text {
  font-size: 13px;
  color: #909399;
}

/* 备注区域 */
.notes-section {
  margin-top: 24px;
}

.notes-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.notes-count {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
  margin-left: 8px;
}

.notes-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.note-item {
  padding: 16px;
  background: #f8fafc;
  border-radius: 6px;
  border-left: 4px solid #667eea;
}

.note-header {
  margin-bottom: 8px;
}

.note-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.consultant-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #409EFF;
}

.consultant-icon {
  font-size: 12px;
}

.note-time {
  font-size: 12px;
  color: #909399;
}

.note-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.empty-notes {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #c0c4cc;
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
  
  .header-right {
    width: 100%;
  }
  
  .action-group {
    width: 100%;
  }
  
  .note-btn,
  .export-btn {
    flex: 1;
    justify-content: center;
  }
  
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .user-grid {
    grid-template-columns: 1fr;
  }
  
  .info-grid {
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
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .user-info {
    font-size: 13px;
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
  
  .note-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>