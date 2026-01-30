<template>
  <div class="app-container home">
    <!-- 顶部欢迎区域 -->
    <div class="welcome-section">
      <el-row :gutter="20">
        <el-col :span="24">
          <div class="welcome-card">
            <div class="welcome-content">
              <h1>欢迎{{ userName }}！</h1>
              <p class="welcome-message">今天的心情如何？我们来一起关注心灵健康</p>
              <p class="date-info">{{ currentDate }} · {{ currentTime }}</p>
            </div>
            <div class="welcome-actions">
              <el-button type="primary" @click="goToAssessment" size="medium">
                开始心理评估
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-card v-if="!hasMentalProfile" class="profile-reminder" shadow="hover" style="margin-top: 20px;">
        <div class="reminder-content">
          <i class="el-icon-warning"></i>
          <span class="reminder-text">您还未完善心理档案，完善档案可获得更精准的心理健康服务</span>
          <el-button type="primary" size="small" @click="goToMentalProfileEdit">
            立即完善
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 快速功能入口 -->
    <el-row :gutter="20" class="quick-access">
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <div class="quick-card" @click="goToAssessment">
          <div class="card-icon" style="background: linear-gradient(135deg, #409EFF, #66b1ff);">
            <i class="el-icon-document"></i>
          </div>
          <div class="card-text">心理评估</div>
          <div class="card-desc">专业量表评估</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <div class="quick-card" @click="goToAIChat">
          <div class="card-icon" style="background: linear-gradient(135deg, #67C23A, #85ce61);">
            <i class="el-icon-chat-dot-round"></i>
          </div>
          <div class="card-text">AI智能疏导</div>
          <div class="card-desc">24小时在线</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <div class="quick-card" @click="goToAppointment">
          <div class="card-icon" style="background: linear-gradient(135deg, #E6A23C, #ebb563);">
            <i class="el-icon-date"></i>
          </div>
          <div class="card-text">咨询预约</div>
          <div class="card-desc">专业心理咨询</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6" :md="6" :lg="6">
        <div class="quick-card" @click="goToMentalProfile">
          <div class="card-icon" style="background: linear-gradient(135deg, #F56C6C, #f78989);">
            <i class="el-icon-user"></i>
          </div>
          <div class="card-text">心理档案</div>
          <div class="card-desc">个人心理记录</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="main-cards">
      <!-- 心理健康状态卡片 -->
      <el-col :lg="12" :md="12" :sm="24" :xs="24">
        <el-card class="status-card mental-status" shadow="hover">
          <div class="card-header">
            <div class="header-left">
              <i class="el-icon-monitor"></i>
              <span>心理健康状态</span>
              <el-tooltip content="基于最近的评估报告" placement="top">
                <i class="el-icon-info"></i>
              </el-tooltip>
            </div>
            <el-button type="text" size="mini" @click="goToAssessment">
              立即评估
            </el-button>
          </div>
          
          <div class="card-body">
            <!-- 加载状态 -->
            <div v-if="loadingLatestReport" class="loading-state">
              <el-skeleton :rows="3" animated />
            </div>
            
            <!-- 空状态 -->
            <div v-else-if="!latestReport" class="empty-state">
              <i class="el-icon-document"></i>
              <p>暂无评估记录</p>
              <el-button type="primary" size="small" @click="goToAssessment">
                开始第一次评估
              </el-button>
            </div>
            
            <!-- 有最新报告 -->
            <div v-else class="report-status">
              <div class="status-main">
                <div class="status-level">
                  <div class="level-badge" :class="getLevelClass(latestReport.resultLevel)">
                    {{ getStatusText(latestReport.resultLevel) }}
                  </div>
                  <div class="level-tag">
                    <el-tag :type="getLevelType(latestReport.resultLevel)" size="medium">
                      {{ latestReport.resultLevel }}
                    </el-tag>
                  </div>
                </div>
                
                <div class="score-grid">
                  <div class="score-item">
                    <div class="score-label">量表名称</div>
                    <div class="score-value">{{ latestReport.scaleName || latestReport.scaleCode }}</div>
                  </div>
                  <div class="score-item">
                    <div class="score-label">原始总分</div>
                    <div class="score-value">{{ latestReport.totalScore || 0 }}</div>
                  </div>
                  <div class="score-item">
                    <div class="score-label">标准分</div>
                    <div class="score-value">{{ latestReport.standardScore ? latestReport.standardScore.toFixed(1) : '0.0' }}</div>
                  </div>
                  <div class="score-item">
                    <div class="score-label">评估时间</div>
                    <div class="score-value time">{{ formatDateTime(latestReport.assessmentTime) }}</div>
                  </div>
                </div>
                
                <div class="progress-section">
                  <div class="progress-header">
                    <span>心理健康进展</span>
                    <span class="risk-level" :class="getRiskLevelClass(latestReport.riskLevel)">
                      {{ latestReport.riskLevel || '低风险' }}
                    </span>
                  </div>
                  <el-progress 
                    :percentage="getProgressPercentage(latestReport.resultLevel)" 
                    :stroke-width="8"
                    :color="getProgressColor(latestReport.resultLevel)"
                    :show-text="false">
                  </el-progress>
                  <div class="progress-labels">
                    <span>正常</span>
                    <span>轻度</span>
                    <span>中度</span>
                    <span>重度</span>
                  </div>
                </div>
              </div>
              
              <div class="action-footer">
                <el-button type="text" @click="viewAssessmentHistory">
                  <i class="el-icon-tickets"></i>
                  查看完整报告
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 最近评估记录卡片 -->
      <el-col :lg="12" :md="12" :sm="24" :xs="24">
        <el-card class="status-card assessment-history" shadow="hover">
          <div class="card-header">
            <div class="header-left">
              <i class="el-icon-document-checked"></i>
              <span>最近评估记录</span>
            </div>
            <div class="header-right">
              <el-tooltip content="刷新数据" placement="top">
                <el-button 
                  type="text" 
                  size="mini" 
                  @click="refreshReports"
                  :loading="loadingReports"
                  circle
                >
                  <i class="el-icon-refresh"></i>
                </el-button>
              </el-tooltip>
              <el-button type="text" size="mini" @click="viewAllReports">
                查看全部
              </el-button>
            </div>
          </div>
          
          <div class="card-body">
            <!-- 加载状态 -->
            <div v-if="loadingReports" class="loading-state">
              <el-skeleton :rows="3" animated />
            </div>
            
            <!-- 空状态 -->
            <div v-else-if="recentReports.length === 0" class="empty-state">
              <i class="el-icon-document"></i>
              <p>暂无评估记录</p>
              <el-button type="primary" size="small" @click="goToAssessment">
                开始第一次评估
              </el-button>
            </div>
            
            <!-- 有评估记录 -->
            <div v-else class="assessment-list">
              <div 
                class="assessment-item" 
                v-for="report in recentReports" 
                :key="report.id" 
                @click="viewReportDetail(report)"
              >
                <div class="assessment-icon">
                  <i class="el-icon-document"></i>
                </div>
                <div class="assessment-content">
                  <div class="assessment-title">
                    {{ report.scaleName || report.scaleCode }}
                  </div>
                  <div class="assessment-meta">
                    <span class="time">{{ formatDateTime(report.assessmentTime) }}</span>
                    <span class="score">总分: {{ report.totalScore || 0 }}</span>
                    <span class="standard-score">标准分: {{ report.standardScore ? report.standardScore.toFixed(1) : '0.0' }}</span>
                  </div>
                </div>
                <div class="assessment-result">
                  <el-tag 
                    :type="getLevelType(report.resultLevel)" 
                    size="small"
                    effect="plain"
                  >
                    {{ report.resultLevel || '未评估' }}
                  </el-tag>
                </div>
              </div>
            </div>
            
            <!-- 更多记录提示 -->
            <div v-if="recentReports.length > 0 && totalReports > 3" class="more-records">
              <el-button type="text" @click="viewAllReports" size="small">
                查看更多记录 (共{{ totalReports }}条)
                <i class="el-icon-arrow-right"></i>
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 今日推荐区域 -->
    <el-row :gutter="20" class="recommendation-section">
      <el-col :span="24">
        <el-card shadow="hover" class="recommendation-card">
          <div class="card-header">
            <div class="header-left">
              <i class="el-icon-star-on"></i>
              <span>今日推荐 · 根据你的心理状态个性化推荐</span>
            </div>
            <div class="header-right">
              <el-tooltip content="刷新推荐" placement="top">
                <el-button 
                  type="text" 
                  size="mini" 
                  @click="refreshRecommendations"
                  circle
                >
                  <i class="el-icon-refresh"></i>
                </el-button>
              </el-tooltip>
              <!-- <el-button 
                type="text" 
                size="mini" 
                @click="goToMyCollections"
                v-if="hasCollections"
              >
                <i class="el-icon-star-on"></i>
                我的收藏
              </el-button> -->
            </div>
          </div>
          
          <div class="recommendation-content">
            <div v-if="personalizedRecommendations.length === 0" class="empty-recommendations">
              <i class="el-icon-document"></i>
              <p>正在加载推荐内容...</p>
            </div>
            
            <el-row :gutter="20" v-else>
              <el-col 
                :lg="8" 
                :md="8" 
                :sm="12" 
                :xs="24"
                v-for="item in personalizedRecommendations" 
                :key="item.id"
                class="recommendation-col"
              >
                <div class="recommendation-item">
                  <div class="item-image" @click="handleRecommendationClick(item)">
                    <img :src="item.imageUrl" alt="" class="recommendation-img">
                    <div class="image-overlay">
                      <i :class="getTypeIcon(item.type)"></i>
                    </div>
                    <div class="type-badge">
                      {{ getTypeName(item.type) }}
                    </div>
                  </div>
                  
                  <div class="item-content">
                    <div class="item-header">
                      <div class="item-category">
                        {{ item.category }}
                      </div>
                      <div class="interaction-stats" v-if="item.likes || item.collections">
                        <span v-if="item.likes" class="like-stat">
                          <i class="el-icon-thumb"></i> {{ item.likes }}
                        </span>
                        <span v-if="item.collections" class="collect-stat">
                          <i class="el-icon-star-off"></i> {{ item.collections }}
                        </span>
                      </div>
                    </div>
                    
                    <div class="item-title" @click="handleRecommendationClick(item)">
                      {{ item.title }}
                    </div>
                    <div class="item-desc">
                      {{ item.desc }}
                    </div>
                    
                    <div class="item-footer">
                      <div class="duration">
                        <i class="el-icon-time"></i>
                        {{ item.duration }}
                      </div>
                      
                      <div class="action-buttons">
                        <!-- 点赞按钮 -->
                        <el-tooltip :content="item.isLiked ? '取消点赞' : '点赞'" placement="top">
                          <el-button 
                            type="text" 
                            size="mini" 
                            class="like-btn"
                            :class="{ 'liked': item.isLiked }"
                            @click.stop="handleLike(item)"
                          >
                            <i :class="item.isLiked ? 'el-icon-thumb' : 'el-icon-thumb'"></i>
                            {{ item.isLiked ? '已赞' : '点赞' }}
                            <span v-if="item.likesCount > 0" class="count">{{ item.likesCount }}</span>
                          </el-button>
                        </el-tooltip>
                        
                        <!-- 收藏按钮 -->
                        <el-tooltip :content="item.isCollected ? '取消收藏' : '收藏'" placement="top">
                          <el-button 
                            type="text" 
                            size="mini" 
                            class="collect-btn"
                            :class="{ 'collected': item.isCollected }"
                            @click.stop="handleCollect(item)"
                          >
                            <i :class="item.isCollected ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
                            {{ item.isCollected ? '已收藏' : '收藏' }}
                          </el-button>
                        </el-tooltip>
                      </div>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部信息卡片 -->
    <el-row :gutter="20" class="info-cards">
      <el-col :lg="8" :md="8" :sm="24" :xs="24">
        <el-card class="info-card" shadow="hover">
          <div class="card-header">
            <i class="el-icon-info"></i>
            <span>系统信息</span>
          </div>
          <div class="card-body">
            <div class="info-item">
              <strong>平台名称：</strong>AI心理健康辅助平台
            </div>
            <div class="info-item">
              <strong>当前版本：</strong>v{{ version }}
            </div>
            <div class="info-item">
              <strong>服务宗旨：</strong>关注心灵健康，呵护美好生活
            </div>
            <div class="info-item">
              <strong>服务时间：</strong>7×24小时在线
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :lg="8" :md="8" :sm="24" :xs="24">
        <el-card class="info-card" shadow="hover">
          <div class="card-header">
            <i class="el-icon-service"></i>
            <span>紧急联系方式</span>
          </div>
          <div class="card-body">
            <div class="info-item">
              <i class="el-icon-phone"></i> 心理咨询热线：400-123-4567
            </div>
            <div class="info-item">
              <i class="el-icon-chat-line-round"></i> 在线咨询：24小时AI助手
            </div>
            <div class="info-item">
              <i class="el-icon-first-aid-kit"></i> 心理危机干预：800-123-4567
            </div>
            <div class="info-item">
              <i class="el-icon-s-home"></i> 校心理咨询中心：容二B1321
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :lg="8" :md="8" :sm="24" :xs="24">
        <el-card class="info-card" shadow="hover">
          <div class="card-header">
            <i class="el-icon-question"></i>
            <span>使用提示</span>
          </div>
          <div class="card-body">
            <div class="info-item">
              <i class="el-icon-light-rain"></i> 建议每周进行一次心理评估
            </div>
            <div class="info-item">
              <i class="el-icon-chat-line-square"></i> 遇到情绪困扰时，可以随时与AI助手交流
            </div>
            <div class="info-item">
              <i class="el-icon-notebook-2"></i> 保持心情日记，记录情绪变化
            </div>
            <div class="info-item">
              <i class="el-icon-user"></i> 如果需要专业帮助，请预约心理咨询师
            </div>
            <div class="info-item">
              <i class="el-icon-warning-outline"></i> 紧急情况请立即拨打心理危机干预热线
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部版权信息 -->
    <div class="footer-info">
      <p>© 2025 AI心理健康辅助平台 · 关注心理健康，呵护心灵成长</p>
      <p class="footer-sub">数据隐私保护 · 信息安全加密 · 专业心理支持</p>
    </div>

    <!-- 播放器弹窗 -->
    <el-dialog
      :title="playerTitle"
      :visible.sync="showPlayer"
      width="600px"
      @close="closePlayer"
      center
    >
      <!-- 音频播放器 -->
      <div v-if="playerType === 'audio'" style="text-align: center; padding: 20px;">
        <audio 
          ref="audioPlayer"
          :src="playerUrl"
          controls
          style="width: 100%;"
        >
          您的浏览器不支持 audio 标签。
        </audio>
        <p style="margin-top: 15px; color: #666;">正在播放冥想音频，建议在安静环境中聆听</p>
      </div>
      
      <!-- 视频播放器 -->
      <div v-if="playerType === 'video'" style="text-align: center;">
        <video 
          ref="videoPlayer"
          :src="playerUrl"
          controls
          style="width: 100%; border-radius: 8px;"
        >
          您的浏览器不支持 video 标签。
        </video>
      </div>
      
      <span slot="footer" class="dialog-footer">
        <el-button @click="closePlayer">关 闭</el-button>
      </span>
    </el-dialog>

    <!-- 心理档案引导弹窗 -->
    <MentalProfileGuide ref="profileGuide" />
  </div>
</template>

<script>
import { getMyRecordList } from "@/api/mental/record";
import { mapGetters } from 'vuex';
import MentalProfileGuide from '@/views/mental/appointment/components/MentalProfileGuide';

export default {
  name: "HealthDashboard",
  components: {
    MentalProfileGuide
  },
  data() {
    return {
      version: "1.0.0",
      userName: localStorage.getItem('health_user_name') || '用户',
      currentDate: '',
      currentTime: '',
      loadingLatestReport: true,
      loadingReports: true,
      latestReport: null,
      recentReports: [],
      totalReports: 0,
      
      // 个性化推荐
      personalizedRecommendations: [],
      showPlayer: false,
      playerType: '',
      playerUrl: '',
      playerTitle: '',
      
      qiniuBaseUrl: 'http://t9fe8lv9h.hn-bkt.clouddn.com/',
      
      // 是否有收藏内容
      hasCollections: false,
      
      allRecommendations: {
        normal: [
          {
            id: 1,
            type: 'article',
            title: '保持心理健康的10个日常习惯',
            desc: '建立健康的生活习惯，预防心理问题的发生，提升生活质量。',
            duration: '10分钟',
            imageUrl: 'https://images.unsplash.com/photo-1493711662062-fa541adb3fc8?w=400&h=200&fit=crop',
            contentUrl: 'https://health.baidu.com/m/detail/ar_6038696102657825186',
            category: '预防保健',
            likes: 156,
            collections: 89,
            likesCount: 156,
            isLiked: false,
            isCollected: false
          },
          {
            id: 2,
            type: 'audio',
            title: '早晨积极心态冥想',
            desc: '每天早晨10分钟，培养积极心态，开启美好一天。',
            duration: '10分钟',
            audioUrl: 'meditation/basic-relaxation.mp3',
            imageUrl: 'https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=400&h=200&fit=crop',
            category: '冥想音频',
            likes: 203,
            collections: 124,
            likesCount: 203,
            isLiked: false,
            isCollected: false
          },
          {
            id: 3,
            type: 'video',
            title: '压力管理技巧讲座',
            desc: '心理学专家讲解如何有效管理日常压力。',
            duration: '25分钟',
            videoUrl: 'videos/stress-management.mp4',
            imageUrl: 'https://images.unsplash.com/photo-1551818255-e6e10975bc17?w=400&h=200&fit=crop',
            category: '心理讲座',
            likes: 98,
            collections: 67,
            likesCount: 98,
            isLiked: false,
            isCollected: false
          }
        ],
        mild: [
          {
            id: 4,
            type: 'article',
            title: '应对焦虑的实用方法',
            desc: '10个简单有效的技巧帮助你缓解轻度焦虑。',
            duration: '8分钟',
            imageUrl: 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=400&h=200&fit=crop',
            contentUrl: 'https://health.baidu.com/m/detail/ar_6428262560060016401',
            category: '焦虑应对',
            likes: 187,
            collections: 102,
            likesCount: 187,
            isLiked: false,
            isCollected: false
          },
          {
            id: 5,
            type: 'audio',
            title: '睡前放松冥想',
            desc: '帮助放松身心，改善睡眠质量，缓解压力。',
            duration: '15分钟',
            audioUrl: 'meditation/sleep-relaxation.mp3',
            imageUrl: 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&h=200&fit=crop',
            category: '助眠冥想',
            likes: 245,
            collections: 156,
            likesCount: 245,
            isLiked: false,
            isCollected: false
          },
          {
            id: 6,
            type: 'video',
            title: '情绪调节技巧教学',
            desc: '学习如何识别和调节负面情绪。',
            duration: '18分钟',
            videoUrl: 'videos/emotion-regulation.mp4',
            imageUrl: 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400&h=200&fit=crop',
            category: '情绪管理',
            likes: 112,
            collections: 78,
            likesCount: 112,
            isLiked: false,
            isCollected: false
          }
        ],
        moderate: [
          {
            id: 7,
            type: 'article',
            title: '专业心理咨询的重要性',
            desc: '了解何时需要寻求专业心理帮助。',
            duration: '6分钟',
            imageUrl: 'https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=400&h=200&fit=crop',
            contentUrl: 'https://m.baidu.com/bh/m/detail/ar_8389473972617351984',
            category: '专业指导',
            likes: 89,
            collections: 45,
            likesCount: 89,
            isLiked: false,
            isCollected: false
          },
          {
            id: 8,
            type: 'audio',
            title: '深度放松焦虑缓解',
            desc: '专业引导帮助缓解中度焦虑情绪。',
            duration: '20分钟',
            audioUrl: 'meditation/deep-anxiety-relief.mp3',
            imageUrl: 'https://images.unsplash.com/photo-1491349174775-aaafddd81942?w=400&h=200&fit=crop',
            category: '焦虑缓解',
            likes: 156,
            collections: 89,
            likesCount: 156,
            isLiked: false,
            isCollected: false
          },
          {
            id: 9,
            type: 'video',
            title: '认知行为疗法介绍',
            desc: '了解CBT如何帮助你改变负面思维模式。',
            duration: '30分钟',
            videoUrl: 'videos/cbt-introduction.mp4',
            imageUrl: 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400&h=200&fit=crop',
            category: '心理治疗',
            likes: 134,
            collections: 67,
            likesCount: 134,
            isLiked: false,
            isCollected: false
          }
        ],
        severe: [
          {
            id: 10,
            type: 'article',
            title: '心理危机干预指南',
            desc: '在紧急情况下如何获得帮助和支持。',
            duration: '5分钟',
            imageUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&h=200&fit=crop',
            contentUrl: 'https://www.zhihu.com/question/643620569',
            category: '紧急帮助',
            likes: 78,
            collections: 34,
            likesCount: 78,
            isLiked: false,
            isCollected: false
          },
          {
            id: 11,
            type: 'audio',
            title: '情绪舒缓引导',
            desc: '在情绪波动时帮助你平静下来。',
            duration: '12分钟',
            audioUrl: 'meditation/emotional-soothing.mp3',
            imageUrl: 'https://images.unsplash.com/photo-1474418397713-5b178681c522?w=400&h=200&fit=crop',
            category: '情绪舒缓',
            likes: 167,
            collections: 92,
            likesCount: 167,
            isLiked: false,
            isCollected: false
          },
          {
            id: 12,
            type: 'video',
            title: '心理健康紧急资源',
            desc: '了解可用的心理健康紧急服务和支持。',
            duration: '15分钟',
            videoUrl: 'videos/mental-health-resources.mp4',
            imageUrl: 'https://images.unsplash.com/photo-1515378791036-0648a3ef77b2?w=400&h=200&fit=crop',
            category: '紧急资源',
            likes: 95,
            collections: 56,
            likesCount: 95,
            isLiked: false,
            isCollected: false
          }
        ],
        default: [
          {
            id: 13,
            type: 'article',
            title: '心理健康基础知识',
            desc: '了解心理健康的重要性和基本概念。',
            duration: '8分钟',
            imageUrl: 'https://images.unsplash.com/photo-1559757148-5c350d0d3c56?w=400&h=200&fit=crop',
            contentUrl: 'https://www.zhihu.com/tardis/bd/art/652456216',
            category: '基础知识',
            likes: 145,
            collections: 78,
            likesCount: 145,
            isLiked: false,
            isCollected: false
          },
          {
            id: 14,
            type: 'audio',
            title: '基础放松冥想',
            desc: '适合初学者的简单放松练习。',
            duration: '10分钟',
            audioUrl: 'meditation/basic-relaxation.mp3',
            imageUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&h=200&fit=crop',
            category: '入门冥想',
            likes: 198,
            collections: 112,
            likesCount: 198,
            isLiked: false,
            isCollected: false
          },
          {
            id: 15,
            type: 'video',
            title: '心理健康自我关爱',
            desc: '学习如何照顾自己的心理健康。',
            duration: '20分钟',
            videoUrl: 'videos/self-care.mp4',
            imageUrl: 'https://images.unsplash.com/photo-1552058544-f2b08422138a?w=400&h=200&fit=crop',
            category: '自我关爱',
            likes: 123,
            collections: 89,
            likesCount: 123,
            isLiked: false,
            isCollected: false
          }
        ]
      }
    }
  },
  computed: {
    ...mapGetters(['hasMentalProfile'])
  },
  created() {
    this.updateDateTime();
    this.timer = setInterval(this.updateDateTime, 1000);
    
    this.loadUserInfo();
    this.loadReports();
    this.checkProfileGuide();
    this.loadPersonalizedRecommendations();
    this.loadUserInteractionData();
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  },
  methods: {
    // 点赞和收藏相关方法
    // 处理点赞
    handleLike(item) {
      if (item.isLiked) {
        // 取消点赞
        item.isLiked = false;
        item.likesCount -= 1;
        this.$message.success('已取消点赞');
        this.saveUserInteraction('unlike', item.id);
      } else {
        // 点赞
        item.isLiked = true;
        item.likesCount += 1;
        this.$message.success('点赞成功！');
        this.saveUserInteraction('like', item.id);
      }
    },
    
    // 处理收藏
    handleCollect(item) {
      if (item.isCollected) {
        // 取消收藏
        item.isCollected = false;
        this.$message.success('已取消收藏');
        this.saveUserInteraction('uncollect', item.id);
      } else {
        // 收藏
        item.isCollected = true;
        this.$message.success('收藏成功！');
        this.saveUserInteraction('collect', item.id);
      }
      
      // 更新是否有收藏内容的状态
      this.checkCollections();
    },
    
    // 保存用户互动数据到本地存储
    saveUserInteraction(action, itemId) {
      try {
        const userId = localStorage.getItem('user_id') || 'anonymous';
        const key = `user_interactions_${userId}`;
        
        let interactions = localStorage.getItem(key);
        interactions = interactions ? JSON.parse(interactions) : {
          likes: [],
          collections: []
        };
        
        if (action === 'like') {
          if (!interactions.likes.includes(itemId)) {
            interactions.likes.push(itemId);
          }
        } else if (action === 'unlike') {
          interactions.likes = interactions.likes.filter(id => id !== itemId);
        } else if (action === 'collect') {
          if (!interactions.collections.includes(itemId)) {
            interactions.collections.push(itemId);
          }
        } else if (action === 'uncollect') {
          interactions.collections = interactions.collections.filter(id => id !== itemId);
        }
        
        localStorage.setItem(key, JSON.stringify(interactions));
      } catch (error) {
        console.error('保存用户互动数据失败:', error);
      }
    },
    
    // 加载用户互动数据
    loadUserInteractionData() {
      try {
        const userId = localStorage.getItem('user_id') || 'anonymous';
        const key = `user_interactions_${userId}`;
        const interactions = localStorage.getItem(key);
        
        if (interactions) {
          const data = JSON.parse(interactions);
          
          // 更新个性化推荐中的点赞和收藏状态
          this.personalizedRecommendations.forEach(item => {
            if (data.likes && data.likes.includes(item.id)) {
              item.isLiked = true;
            }
            if (data.collections && data.collections.includes(item.id)) {
              item.isCollected = true;
            }
          });
          
          // 检查是否有收藏内容
          this.checkCollections();
        }
      } catch (error) {
        console.error('加载用户互动数据失败:', error);
      }
    },
    
    // 检查是否有收藏内容
    checkCollections() {
      this.hasCollections = this.personalizedRecommendations.some(item => item.isCollected);
    },
    
    // 跳转到我的收藏页面
    goToMyCollections() {
      this.$message.info('收藏功能正在开发中，敬请期待！');
      // 可以跳转到专门的收藏页面
      // this.$router.push('/mental/collections');
    },
    
    
    // 根据路由配置修改的跳转方法
    goToAssessment() {
      this.$router.push('/mental-health/assessment');
    },

    goToAIChat() {
      this.$router.push('/mental-health/ai-chat');
    },

    goToAppointment() {
      this.$router.push('/mental-health/appointment');
    },
    
    goToMentalProfile() {
      try {
        this.$router.push('/mental-profile/index');
      } catch (error) {
        console.error('跳转档案首页失败:', error);
        this.$message.warning('档案页面加载失败，请稍后重试');
      }
    },
    
    goToMentalProfileEdit() {
      try {
        this.$router.push({ name: 'MentalProfileEdit' });
      } catch (error) {
        try {
          this.$router.push('/mental-profile/edit');
        } catch (secondError) {
          console.error('跳转编辑页面失败:', secondError);
          this.$router.push('/mental-profile/index');
          this.$message.warning('编辑页面暂时无法访问，请从心理档案首页手动进入编辑');
        }
      }
    },
    
    viewAllReports() {
      this.$router.push('/mental-health/reports');
    },
    
    viewReportDetail(report) {
      this.$router.push({
        path: `/mental-health/report-detail/${report.id}`
      });
    },
    
    viewAssessmentHistory() {
      this.$router.push('/mental-health/reports');
    },

    // 其他方法
    updateDateTime() {
      const now = new Date();
      const year = now.getFullYear();
      const month = (now.getMonth() + 1).toString().padStart(2, '0');
      const day = now.getDate().toString().padStart(2, '0');
      const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
      const weekday = weekdays[now.getDay()];
      
      this.currentDate = `${year}年${month}月${day}日 ${weekday}`;
      
      const hours = now.getHours().toString().padStart(2, '0');
      const minutes = now.getMinutes().toString().padStart(2, '0');
      const seconds = now.getSeconds().toString().padStart(2, '0');
      
      this.currentTime = `${hours}:${minutes}:${seconds}`;
    },

    async loadReports() {
      try {
        this.loadingLatestReport = true;
        this.loadingReports = true;
        
        const response = await getMyRecordList({
          pageNum: 1,
          pageSize: 5
        });
        
        if (response.code === 200) {
          let reports = [];
          if (response.data && response.data.rows) {
            reports = response.data.rows;
            this.totalReports = response.data.total || 0;
          } else if (response.rows) {
            reports = response.rows;
            this.totalReports = response.total || 0;
          } else {
            reports = response.data || [];
            this.totalReports = reports.length;
          }
          
          if (reports.length > 0) {
            reports.sort((a, b) => new Date(b.assessmentTime) - new Date(a.assessmentTime));
            this.latestReport = reports[0];
            this.recentReports = reports.slice(0, 3);
          } else {
            this.latestReport = null;
            this.recentReports = [];
          }
        } else {
          this.$message.error(response.msg || '加载报告失败');
        }
      } catch (error) {
        console.error('加载报告失败:', error);
        this.$message.error('加载报告失败: ' + (error.message || '未知错误'));
      } finally {
        this.loadingLatestReport = false;
        this.loadingReports = false;
      }
    },

    formatDateTime(time) {
      if (!time) return '-';
      try {
        const date = new Date(time);
        const now = new Date();
        const diffTime = now - date;
        const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
        
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        
        if (diffDays === 0) {
          return `今天 ${hours}:${minutes}`;
        } else if (diffDays === 1) {
          return `昨天 ${hours}:${minutes}`;
        } else if (diffDays < 7) {
          return `${diffDays}天前`;
        } else {
          return `${month}月${day}日`;
        }
      } catch (error) {
        console.error('格式化时间失败:', error);
        return '-';
      }
    },

    getLevelType(level) {
      if (!level) return 'info';
      if (level.includes('正常')) return 'success';
      if (level.includes('轻度')) return 'warning';
      if (level.includes('中度')) return 'warning';
      if (level.includes('重度')) return 'danger';
      return 'info';
    },

    getStatusText(level) {
      if (!level) return '未知';
      if (level.includes('正常')) return '良好';
      if (level.includes('轻度')) return '需要注意';
      if (level.includes('中度')) return '建议关注';
      if (level.includes('重度')) return '需要关注';
      return '未知';
    },

    getLevelClass(level) {
      if (!level) return 'status-unknown';
      if (level.includes('正常')) return 'status-good';
      if (level.includes('轻度')) return 'status-mild';
      if (level.includes('中度')) return 'status-moderate';
      if (level.includes('重度')) return 'status-severe';
      return 'status-unknown';
    },

    getProgressPercentage(level) {
      if (!level) return 10;
      if (level.includes('正常')) return 10;
      if (level.includes('轻度')) return 40;
      if (level.includes('中度')) return 70;
      if (level.includes('重度')) return 90;
      return 10;
    },

    getProgressColor(level) {
      if (!level) return '#909399';
      if (level.includes('正常')) return '#67C23A';
      if (level.includes('轻度')) return '#E6A23C';
      if (level.includes('中度')) return '#F56C6C';
      if (level.includes('重度')) return '#F56C6C';
      return '#909399';
    },

    getRiskLevelClass(riskLevel) {
      if (!riskLevel) return 'risk-low';
      if (riskLevel.includes('正常') || riskLevel.includes('低风险')) return 'risk-low';
      if (riskLevel.includes('中低风险')) return 'risk-medium-low';
      if (riskLevel.includes('中高风险')) return 'risk-medium-high';
      if (riskLevel.includes('高风险')) return 'risk-high';
      return 'risk-unknown';
    },

    getTypeIcon(type) {
      return {
        'article': 'el-icon-document',
        'audio': 'el-icon-headset',
        'video': 'el-icon-video-play'
      }[type] || 'el-icon-document';
    },

    getTypeName(type) {
      return {
        'article': '心理文章',
        'audio': '冥想音频',
        'video': '心理视频'
      }[type] || '文章';
    },

    handleRecommendationClick(item) {
      if (item.type === 'article') {
        window.open(item.contentUrl, '_blank');
      } else if (item.type === 'audio') {
        this.playAudio(item);
      } else if (item.type === 'video') {
        this.playVideo(item);
      }
    },

    playAudio(item) {
      const fullAudioUrl = this.qiniuBaseUrl + item.audioUrl;
      this.playerType = 'audio';
      this.playerUrl = fullAudioUrl;
      this.playerTitle = item.title;
      this.showPlayer = true;
    },

    playVideo(item) {
      const fullVideoUrl = this.qiniuBaseUrl + item.videoUrl;
      this.playerType = 'video';
      this.playerUrl = fullVideoUrl;
      this.playerTitle = item.title;
      this.showPlayer = true;
    },

    closePlayer() {
      this.showPlayer = false;
      this.playerUrl = '';
      this.playerTitle = '';
      
      if (this.playerType === 'audio' && this.$refs.audioPlayer) {
        this.$refs.audioPlayer.pause();
        this.$refs.audioPlayer.currentTime = 0;
      } else if (this.playerType === 'video' && this.$refs.videoPlayer) {
        this.$refs.videoPlayer.pause();
        this.$refs.videoPlayer.currentTime = 0;
      }
    },

    refreshReports() {
      this.loadReports();
    },

    refreshRecommendations() {
      this.loadPersonalizedRecommendations();
      this.$message.success('推荐内容已刷新！');
    },

    loadUserInfo() {
      try {
        const userInfoStr = localStorage.getItem('health_user_info');
        if (userInfoStr) {
          const userInfo = JSON.parse(userInfoStr);
          this.userName = userInfo.nickname || userInfo.username || '用户';
        }
      } catch (error) {
        console.error('加载用户信息失败:', error);
      }
    },

    loadPersonalizedRecommendations() {
      setTimeout(() => {
        if (!this.latestReport) {
          this.personalizedRecommendations = [...this.allRecommendations.default];
        } else {
          const level = this.latestReport.resultLevel;
          if (level && level.includes('正常')) {
            this.personalizedRecommendations = [...this.allRecommendations.normal];
          } else if (level && level.includes('轻度')) {
            this.personalizedRecommendations = [...this.allRecommendations.mild];
          } else if (level && level.includes('中度')) {
            this.personalizedRecommendations = [...this.allRecommendations.moderate];
          } else if (level && level.includes('重度')) {
            this.personalizedRecommendations = [...this.allRecommendations.severe];
          } else {
            this.personalizedRecommendations = [...this.allRecommendations.default];
          }
        }
        
        // 加载用户互动数据
        this.loadUserInteractionData();
      }, 300);
    },

    checkProfileGuide() {
      const isAdmin = this.$store.getters.roles?.includes('admin');
      const hasSkipped = localStorage.getItem('profileGuideSkipped') === 'true';
      
      if (!isAdmin && !this.hasMentalProfile && !hasSkipped) {
        this.$nextTick(() => {
          this.$refs.profileGuide?.open();
        });
      }
    }
  }
};
</script>

<style scoped lang="scss">
.home {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

// 欢迎区域（包含心理档案提醒）
.welcome-section {
  margin-bottom: 30px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 15px;
  padding: 30px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  @media (max-width: 768px) {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
}

.welcome-content {
  h1 {
    margin: 0 0 10px;
    font-size: 28px;
    font-weight: bold;
  }
  
  .welcome-message {
    margin: 0 0 10px;
    font-size: 16px;
    opacity: 0.9;
  }
  
  .date-info {
    margin: 0;
    font-size: 14px;
    opacity: 0.8;
  }
}

.welcome-actions {
  .el-button {
    background: rgba(255, 255, 255, 0.2);
    border-color: rgba(255, 255, 255, 0.3);
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

// 心理档案提醒样式（在欢迎区域内部）
.profile-reminder {
  margin-top: 20px;
  
  .reminder-content {
    display: flex;
    align-items: center;
    padding: 20px;
    background: linear-gradient(135deg, rgba(255, 193, 7, 0.1) 0%, rgba(255, 235, 59, 0.1) 100%);
    border-radius: 8px;
    
    .el-icon-warning {
      color: #E6A23C;
      font-size: 24px;
      margin-right: 15px;
    }
    
    .reminder-text {
      flex: 1;
      font-size: 14px;
      color: #606266;
    }
    
    @media (max-width: 768px) {
      flex-direction: column;
      align-items: flex-start;
      gap: 15px;
      
      .el-icon-warning {
        margin-right: 0;
        margin-bottom: 5px;
      }
    }
  }
}

// 快速入口
.quick-access {
  margin-bottom: 30px;
  
  .quick-card {
    background: white;
    border-radius: 12px;
    padding: 25px 15px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    height: 160px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border: 1px solid #ebeef5;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
      border-color: #409EFF;
    }
    
    .card-icon {
      width: 70px;
      height: 70px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 15px;
      
      i {
        font-size: 32px;
        color: white;
      }
    }
    
    .card-text {
      font-weight: 600;
      color: #303133;
      font-size: 16px;
      margin-bottom: 5px;
    }
    
    .card-desc {
      color: #909399;
      font-size: 13px;
    }
  }
}

// 主要卡片区域
.main-cards {
  margin-bottom: 30px;
  
  .status-card {
    height: 100%;
    min-height: 380px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 15px;
      border-bottom: 1px solid #ebeef5;
      
      .header-left {
        display: flex;
        align-items: center;
        gap: 10px;
        
        i {
          font-size: 18px;
          color: #409EFF;
        }
        
        span {
          font-weight: 600;
          color: #303133;
          font-size: 16px;
        }
        
        .el-icon-info {
          color: #C0C4CC;
          cursor: help;
        }
      }
      
      .header-right {
        display: flex;
        align-items: center;
        gap: 10px;
      }
    }
    
    .card-body {
      .loading-state {
        padding: 40px 0;
      }
      
      .empty-state {
        text-align: center;
        padding: 60px 0;
        
        i {
          font-size: 48px;
          color: #C0C4CC;
          margin-bottom: 15px;
          display: block;
        }
        
        p {
          color: #909399;
          margin-bottom: 20px;
          font-size: 14px;
        }
      }
    }
  }
  
  .mental-status {
    .report-status {
      .status-main {
        .status-level {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 25px;
          
          .level-badge {
            font-size: 32px;
            font-weight: bold;
            
            &.status-good {
              color: #67C23A;
            }
            
            &.status-mild {
              color: #E6A23C;
            }
            
            &.status-moderate {
              color: #F56C6C;
            }
            
            &.status-severe {
              color: #F56C6C;
            }
            
            &.status-unknown {
              color: #909399;
            }
          }
        }
        
        .score-grid {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 15px;
          margin-bottom: 25px;
          
          .score-item {
            background: #f8f9fa;
            border-radius: 8px;
            padding: 15px;
            
            .score-label {
              font-size: 12px;
              color: #909399;
              margin-bottom: 8px;
            }
            
            .score-value {
              font-size: 16px;
              font-weight: 600;
              color: #303133;
              
              &.time {
                font-size: 14px;
                color: #606266;
              }
            }
          }
        }
        
        .progress-section {
          margin-bottom: 20px;
          
          .progress-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
            
            span:first-child {
              font-size: 14px;
              color: #606266;
            }
            
            .risk-level {
              font-size: 12px;
              padding: 4px 8px;
              border-radius: 4px;
              
              &.risk-low {
                background: rgba(103, 194, 58, 0.1);
                color: #67C23A;
              }
              
              &.risk-medium-low {
                background: rgba(230, 162, 60, 0.1);
                color: #E6A23C;
              }
              
              &.risk-medium-high {
                background: rgba(245, 108, 108, 0.1);
                color: #F56C6C;
              }
              
              &.risk-high {
                background: rgba(245, 108, 108, 0.2);
                color: #F56C6C;
                font-weight: bold;
              }
              
              &.risk-unknown {
                background: rgba(144, 147, 153, 0.1);
                color: #909399;
              }
            }
          }
          
          .progress-labels {
            display: flex;
            justify-content: space-between;
            margin-top: 5px;
            font-size: 12px;
            color: #909399;
          }
        }
      }
      
      .action-footer {
        border-top: 1px solid #ebeef5;
        padding-top: 15px;
        text-align: center;
      }
    }
  }
  
  .assessment-history {
    .assessment-list {
      .assessment-item {
        display: flex;
        align-items: center;
        padding: 15px;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.3s;
        margin-bottom: 10px;
        border: 1px solid transparent;
        
        &:hover {
          background: #f8f9fa;
          border-color: #e4e7ed;
        }
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .assessment-icon {
          margin-right: 15px;
          
          i {
            font-size: 20px;
            color: #409EFF;
          }
        }
        
        .assessment-content {
          flex: 1;
          
          .assessment-title {
            font-weight: 600;
            color: #303133;
            margin-bottom: 5px;
            font-size: 14px;
          }
          
          .assessment-meta {
            display: flex;
            gap: 15px;
            font-size: 12px;
            color: #909399;
            
            span {
              display: flex;
              align-items: center;
              
              &::before {
                content: '·';
                margin-right: 5px;
                color: #C0C4CC;
              }
            }
          }
        }
        
        .assessment-result {
          margin-left: 15px;
        }
      }
    }
    
    .more-records {
      text-align: center;
      padding-top: 15px;
      margin-top: 15px;
      border-top: 1px solid #ebeef5;
    }
  }
}

// 今日推荐（添加了点赞收藏功能）
.recommendation-section {
  margin-bottom: 30px;
  
  .recommendation-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      
      .header-left {
        display: flex;
        align-items: center;
        gap: 10px;
        
        i {
          color: #E6A23C;
          font-size: 18px;
        }
        
        span {
          font-weight: 600;
          color: #303133;
          font-size: 16px;
        }
      }
      
      .header-right {
        display: flex;
        align-items: center;
        gap: 15px;
        
        .el-button {
          &:hover {
            color: #409EFF;
          }
          
          i {
            margin-right: 5px;
          }
        }
      }
    }
    
    .recommendation-content {
      .empty-recommendations {
        text-align: center;
        padding: 40px 0;
        
        i {
          font-size: 48px;
          color: #C0C4CC;
          margin-bottom: 15px;
          display: block;
        }
        
        p {
          color: #909399;
          font-size: 14px;
        }
      }
      
      .recommendation-col {
        margin-bottom: 20px;
      }
      
      .recommendation-item {
        border-radius: 12px;
        overflow: hidden;
        background: white;
        border: 1px solid #ebeef5;
        transition: all 0.3s;
        height: 100%;
        
        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
          border-color: #409EFF;
          
          .item-title {
            color: #409EFF;
          }
        }
        
        .item-image {
          position: relative;
          height: 140px;
          overflow: hidden;
          cursor: pointer;
          
          .recommendation-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.3s;
          }
          
          &:hover .recommendation-img {
            transform: scale(1.05);
          }
          
          .image-overlay {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0, 0, 0, 0.2);
            display: flex;
            align-items: center;
            justify-content: center;
            
            i {
              font-size: 32px;
              color: white;
            }
          }
          
          .type-badge {
            position: absolute;
            top: 10px;
            right: 10px;
            background: rgba(64, 158, 255, 0.9);
            color: white;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 12px;
          }
        }
        
        .item-content {
          padding: 20px;
          
          .item-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
            
            .item-category {
              font-size: 12px;
              color: #67C23A;
              font-weight: 500;
            }
            
            .interaction-stats {
              display: flex;
              gap: 10px;
              font-size: 11px;
              
              .like-stat, .collect-stat {
                display: flex;
                align-items: center;
                gap: 3px;
                color: #909399;
                
                i {
                  font-size: 12px;
                }
              }
              
              .like-stat i {
                color: #F56C6C;
              }
              
              .collect-stat i {
                color: #E6A23C;
              }
            }
          }
          
          .item-title {
            font-size: 16px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 10px;
            line-height: 1.4;
            height: 44px;
            overflow: hidden;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            -webkit-box-orient: vertical;
            cursor: pointer;
            transition: color 0.3s;
            
            &:hover {
              color: #409EFF;
            }
          }
          
          .item-desc {
            font-size: 13px;
            color: #606266;
            line-height: 1.5;
            margin-bottom: 15px;
            height: 40px;
            overflow: hidden;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            -webkit-box-orient: vertical;
          }
          
          .item-footer {
            display: flex;
            justify-content: space-between;
            align-items: center;
            
            .duration {
              font-size: 12px;
              color: #909399;
              
              i {
                margin-right: 5px;
              }
            }
            
            .action-buttons {
              display: flex;
              gap: 15px;
              
              .like-btn, .collect-btn {
                padding: 0;
                min-height: 24px;
                font-size: 12px;
                color: #909399;
                
                &:hover {
                  color: #409EFF;
                }
                
                i {
                  margin-right: 3px;
                  font-size: 14px;
                }
                
                .count {
                  margin-left: 3px;
                  font-weight: 500;
                }
              }
              
              .like-btn.liked {
                color: #F56C6C;
                
                i {
                  color: #F56C6C;
                }
                
                &:hover {
                  color: #F56C6C;
                }
              }
              
              .collect-btn.collected {
                color: #E6A23C;
                
                i {
                  color: #E6A23C;
                }
                
                &:hover {
                  color: #E6A23C;
                }
              }
            }
          }
        }
      }
    }
  }
}

// 信息卡片
.info-cards {
  margin-bottom: 30px;
  
  .info-card {
    height: 100%;
    min-height: 280px;
    
    .card-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 20px;
      padding-bottom: 15px;
      border-bottom: 1px solid #ebeef5;
      
      i {
        font-size: 18px;
        color: #409EFF;
      }
      
      span {
        font-weight: 600;
        color: #303133;
        font-size: 16px;
      }
    }
    
    .card-body {
      .info-item {
        margin-bottom: 12px;
        font-size: 14px;
        color: #606266;
        line-height: 1.6;
        padding-left: 5px;
        border-left: 3px solid transparent;
        transition: all 0.3s;
        
        &:hover {
          border-left-color: #409EFF;
          padding-left: 10px;
        }
        
        &:last-child {
          margin-bottom: 0;
        }
        
        i {
          color: #409EFF;
          margin-right: 8px;
          width: 16px;
          text-align: center;
        }
      }
    }
  }
}

// 底部信息
.footer-info {
  text-align: center;
  padding: 30px 0;
  color: #666;
  border-top: 1px solid #ebeef5;
  margin-top: 30px;
  
  p {
    margin: 5px 0;
  }
  
  .footer-sub {
    font-size: 12px;
    color: #909399;
  }
}

// 响应式调整
@media (max-width: 1200px) {
  .quick-card {
    height: 140px !important;
    padding: 20px 10px !important;
    
    .card-icon {
      width: 60px !important;
      height: 60px !important;
      margin-bottom: 12px !important;
      
      i {
        font-size: 28px !important;
      }
    }
    
    .card-text {
      font-size: 15px !important;
    }
    
    .card-desc {
      font-size: 12px !important;
    }
  }
}

@media (max-width: 992px) {
  .main-cards {
    .el-col {
      margin-bottom: 20px;
    }
  }
  
  .info-cards {
    .el-col {
      margin-bottom: 20px;
    }
  }
}

@media (max-width: 768px) {
  .home {
    padding: 15px;
  }
  
  .welcome-card {
    padding: 20px !important;
    
    h1 {
      font-size: 24px !important;
    }
  }
  
  .quick-card {
    margin-bottom: 15px;
    height: 130px !important;
  }
  
  .recommendation-item {
    margin-bottom: 20px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 10px !important;
    
    .like-btn, .collect-btn {
      width: 100%;
      justify-content: center;
    }
  }
}

@media (max-width: 576px) {
  .score-grid {
    grid-template-columns: 1fr !important;
  }
  
  .assessment-meta {
    flex-direction: column;
    gap: 5px !important;
  }
  
  .quick-card {
    height: 120px !important;
    padding: 15px 8px !important;
    
    .card-icon {
      width: 50px !important;
      height: 50px !important;
      
      i {
        font-size: 24px !important;
      }
    }
  }
  
  .item-header {
    flex-direction: column;
    align-items: flex-start !important;
    gap: 5px;
  }
  
  .item-footer {
    flex-direction: column;
    align-items: flex-start !important;
    gap: 10px;
  }
}
</style>