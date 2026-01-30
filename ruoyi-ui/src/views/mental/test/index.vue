<template>
  <div class="app-container">
    <div class="assessment-container">
      <!-- 头部区域 -->
      <div class="header-card">
        <div class="header-content">
          <div class="header-left">
            <div class="title-section">
              <i class="el-icon-s-data title-icon"></i>
              <h2 class="main-title">心理测评</h2>
            </div>
            <p class="header-description">专业心理测评工具，科学评估心理健康状态</p>
          </div>
          <div class="header-right">
          </div>
        </div>
      </div>

      <!-- 量表选择页面 -->
      <div v-if="!testing" class="scales-page">
        <!-- 量表列表 -->
        <div class="scales-grid">
          <el-card 
            v-for="scale in scales" 
            :key="scale.code" 
            class="scale-card" 
            shadow="hover"
            @click.native="startTest(scale.code)">
            <div class="scale-header">
              <div class="scale-icon" :style="{background: scale.color}">
                <i :class="scale.icon"></i>
              </div>
              <div class="scale-title">
                <h3>{{ scale.name }}</h3>
                <div class="scale-tags">
                  <el-tag size="mini" :type="scale.type" effect="plain">{{ scale.difficulty }}</el-tag>
                </div>
              </div>
            </div>
            <div class="scale-content">
              <p class="scale-desc">{{ scale.description }}</p>
              <div class="scale-meta">
                <div class="meta-item">
                  <i class="el-icon-time meta-icon"></i>
                  <span>{{ scale.time }}</span>
                </div>
                <div class="meta-item">
                  <i class="el-icon-document meta-icon"></i>
                  <span>{{ scale.questions }}题</span>
                </div>
              </div>
            </div>
            <div class="scale-footer">
              <el-button type="primary" size="small" plain class="start-btn">
                开始测评
                <i class="el-icon-right"></i>
              </el-button>
            </div>
          </el-card>
        </div>

        <!-- 测评说明 -->
        <el-card shadow="hover" class="notice-card">
          <div slot="header" class="notice-header">
            <div class="notice-title">
              <i class="el-icon-info notice-icon"></i>
              <span>测评说明</span>
            </div>
          </div>
          <div class="notice-content">
            <div class="notice-grid">
              <div class="notice-item">
                <div class="item-icon privacy">
                  <i class="el-icon-lock"></i>
                </div>
                <div class="item-content">
                  <h4>数据安全</h4>
                  <p>所有测评结果严格保密，仅用于您的个人参考</p>
                </div>
              </div>
              <div class="notice-item">
                <div class="item-icon truth">
                  <i class="el-icon-star"></i>
                </div>
                <div class="item-content">
                  <h4>如实回答</h4>
                  <p>请根据最近一周的真实感受进行选择</p>
                </div>
              </div>
              <div class="notice-item">
                <div class="item-icon reference">
                  <i class="el-icon-chat-line-square"></i>
                </div>
                <div class="item-content">
                  <h4>结果参考</h4>
                  <p>测评结果仅供参考，不能替代专业诊断</p>
                </div>
              </div>
              <div class="notice-item">
                <div class="item-icon help">
                  <i class="el-icon-help"></i>
                </div>
                <div class="item-content">
                  <h4>专业帮助</h4>
                  <p>如结果异常，建议及时寻求专业帮助</p>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 测评问卷页面 -->
      <div v-else class="test-page">
        <!-- 测评头部 -->
        <div class="test-header">
          <div class="test-title-section">
            <el-button 
              @click="cancelTest" 
              icon="el-icon-back" 
              size="small" 
              circle
              plain
              class="back-btn">
            </el-button>
            <div class="test-title">
              <h2>{{ currentScale.name }}</h2>
              <p class="test-desc">{{ currentScale.description }}</p>
            </div>
          </div>
          
          <!-- 进度条 -->
          <div class="test-progress">
            <div class="progress-info">
              <span class="progress-text">第 {{ currentQuestion + 1 }} 题 / 共 {{ questions.length }} 题</span>
              <span class="progress-percent">{{ progress }}%</span>
            </div>
            <el-progress 
              :percentage="progress" 
              :stroke-width="6"
              :color="currentScale.color"
              :show-text="false"
              class="progress-bar">
            </el-progress>
          </div>
        </div>

        <!-- 问题卡片 -->
        <el-card shadow="hover" class="question-card">
          <div class="question-header">
            <div class="question-number">
              <span class="current">{{ currentQuestion + 1 }}</span>
              <span class="separator">/</span>
              <span class="total">{{ questions.length }}</span>
            </div>
            <div class="question-text">
              <h3>{{ questions[currentQuestion] }}</h3>
            </div>
          </div>

          <div class="options-grid">
            <div 
              v-for="(option, index) in options" 
              :key="index" 
              class="option-card" 
              :class="{ 'selected': answers[currentQuestion] === index }"
              @click="selectOption(index)">
              <div class="option-index">{{ String.fromCharCode(65 + index) }}</div>
              <div class="option-content">
                <div class="option-text">{{ option }}</div>
                <div class="option-value" v-if="answers[currentQuestion] === index">已选择</div>
              </div>
            </div>
          </div>

          <div class="test-footer">
            <div class="answered-info">
              <el-tag type="success" size="small" class="answered-tag">
                <i class="el-icon-finished"></i>
                已答：{{ answeredCount }}题
              </el-tag>
            </div>
            
            <div class="action-buttons">
              <el-button 
                @click="prevQuestion" 
                :disabled="currentQuestion === 0"
                size="small"
                plain
                class="nav-btn prev-btn">
                <i class="el-icon-back"></i>
                上一题
              </el-button>
              
              <el-button 
                type="primary" 
                @click="nextQuestion"
                :disabled="!hasAnswer"
                size="small"
                class="nav-btn next-btn">
                <template v-if="currentQuestion === questions.length - 1">
                  <i class="el-icon-finished"></i>
                  提交测评
                </template>
                <template v-else>
                  <i class="el-icon-right"></i>
                  下一题
                </template>
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
import { submitAssessment } from '@/api/mental/record'

export default {
  name: 'MentalTestPage',
  data() {
    return {
      testing: false,
      currentScale: {},
      questions: [],
      options: [
        '没有或很少时间',
        '小部分时间', 
        '相当多时间',
        '绝大部分或全部时间'
      ],
      answers: [],
      currentQuestion: 0,
      
      scales: [
        {
          code: 'SDS',
          name: '抑郁自评量表(SDS)',
          description: '评估抑郁症状的严重程度和变化',
          icon: 'el-icon-sad',
          color: '#409EFF',
          time: '约5分钟',
          questions: 20,
          difficulty: '简单',
          type: 'primary'
        },
        {
          code: 'SAS',
          name: '焦虑自评量表(SAS)',
          description: '评估焦虑症状的严重程度',
          icon: 'el-icon-warning',
          color: '#E6A23C',
          time: '约5分钟',
          questions: 20,
          difficulty: '简单',
          type: 'warning'
        }
      ],
      
      scaleQuestions: {
        SDS: [
          '我感到情绪沮丧，郁闷',
          '我感到早晨心情最好',
          '我要哭或想哭',
          '我夜间睡眠不好',
          '我吃饭像平时一样多',
          '我的性功能正常',
          '我感到体重减轻',
          '我为便秘烦恼',
          '我的心跳比平时快',
          '我无缘无故感到疲乏',
          '我的头脑像往常一样清楚',
          '我做事情像平时一样不感到困难',
          '我坐卧不安，难以保持平静',
          '我对未来感到有希望',
          '我比平时更容易激怒',
          '我觉得决定什么事很容易',
          '我感到自己是有用的和不可缺少的人',
          '我的生活很有意义',
          '假若我死了别人会过得更好',
          '我仍旧喜爱自己平时喜爱的东西'
        ],
        SAS: [
          '我感到比往常更加神经过敏和焦虑',
          '我无缘无故感到担心',
          '我容易心烦意乱或感到恐慌',
          '我感到我的身体好像被分成几块，支离破碎',
          '我感到事事都很顺利，不会有倒霉的事情发生',
          '我的四肢抖动和震颤',
          '我因头痛、颈痛和背痛而烦恼',
          '我感到无力且容易疲劳',
          '我感到很平静，能安静坐下来',
          '我感到我的心跳较快',
          '我因阵阵的眩晕而不舒服',
          '我有阵阵要昏倒的感觉',
          '我呼吸时进气和出气都不费力',
          '我的手指和脚趾感到麻木和刺痛',
          '我因胃痛和消化不良而苦恼',
          '我必须时常排尿',
          '我的手总是温暖而干燥',
          '我觉得脸发烧发红',
          '我容易入睡，晚上休息很好',
          '我做噩梦'
        ]
      }
    }
  },
  computed: {
    progress() {
      return Math.round((this.currentQuestion + 1) / this.questions.length * 100)
    },
    answeredCount() {
      return this.answers.filter(a => a !== null).length
    },
    hasAnswer() {
      return this.answers[this.currentQuestion] !== null
    }
  },
  methods: {
    startTest(scaleCode) {
      this.testing = true
      this.currentScale = this.scales.find(s => s.code === scaleCode) || {}
      this.questions = this.scaleQuestions[scaleCode] || []
      this.answers = new Array(this.questions.length).fill(null)
      this.currentQuestion = 0
      
      window.scrollTo(0, 0)
    },
    
    selectOption(index) {
      this.$set(this.answers, this.currentQuestion, index)
    },
    
    prevQuestion() {
      if (this.currentQuestion > 0) {
        this.currentQuestion--
      }
    },
    
    nextQuestion() {
      if (this.currentQuestion < this.questions.length - 1) {
        this.currentQuestion++
      } else {
        this.submitTest()
      }
    },
    
    cancelTest() {
      this.$confirm('确定要取消测评吗？当前进度将不会保存', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '继续测评',
        type: 'warning'
      }).then(() => {
        this.testing = false
        this.answers = []
        this.currentQuestion = 0
      }).catch(() => {})
    },
    
    async submitTest() {
      const unanswered = this.answers.filter(a => a === null).length
      
      if (unanswered > 0) {
        try {
          await this.$confirm(`还有${unanswered}题未回答，是否现在提交？`, '提示', {
            confirmButtonText: '提交测评',
            cancelButtonText: '继续完成',
            type: 'warning'
          })
        } catch {
          return
        }
      }
      
      try {
        const answersData = this.questions.map((question, index) => {
          const answer = this.answers[index]
          return {
            questionId: index + 1,
            question: question,
            answer: answer !== null ? answer + 1 : 0
          }
        })
        
        const assessmentData = {
          scaleCode: this.currentScale.code,
          scaleName: this.currentScale.name,
          answersJson: JSON.stringify(answersData)
        }
        
        const loading = this.$loading({
          lock: true,
          text: '正在提交测评结果...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        })
        
        const response = await submitAssessment(assessmentData)
        
        loading.close()
        
        this.showResult(response.data || response)
        
      } catch (error) {
        console.error('提交测评失败:', error)
        this.$loading().close()
        this.$message.error('提交失败: ' + (error.message || '请检查网络连接'))
      }
    },
    
    showResult(result) {
      const totalScore = result.totalScore || 0
      const resultLevel = result.resultLevel || '未评估'
      const suggestion = result.suggestion || '感谢您的参与！'
      const scaleName = result.scaleName || this.currentScale.name || '测评'
      
      this.$alert(`
        <div style="text-align: center;">
          <div style="font-size: 48px; color: #67C23A; margin-bottom: 10px;">
            <i class="el-icon-success"></i>
          </div>
          <h3 style="color: #303133; margin-bottom: 20px;">测评完成！</h3>
          <div style="text-align: left; padding: 20px; background: #f5f7fa; border-radius: 8px; margin-bottom: 15px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
              <span style="color: #909399;">测评量表</span>
              <span style="font-weight: 500;">${scaleName}</span>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
              <span style="color: #909399;">总分</span>
              <span style="color: ${this.getScoreColor(totalScore)}; font-weight: bold; font-size: 18px;">${totalScore}</span>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
              <span style="color: #909399;">评估等级</span>
              <span style="color: ${this.getLevelColor(resultLevel)}; font-weight: 500;">${resultLevel}</span>
            </div>
            <div style="border-top: 1px solid #e6ebf5; padding-top: 15px;">
              <div style="color: #909399; margin-bottom: 8px;">专业建议</div>
              <div style="color: #606266; line-height: 1.6; font-size: 14px;">${suggestion}</div>
            </div>
          </div>
        </div>
      `, '测评结果', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '查看报告',
        showCancelButton: true,
        cancelButtonText: '返回首页'
      }).then(() => {
        this.$router.push('/mental/report')
        this.testing = false
      }).catch(() => {
        this.$router.push('/')
        this.testing = false
      })
    },
    
    getScoreColor(score) {
      if (!score && score !== 0) return '#909399'
      const scoreNum = Number(score)
      if (isNaN(scoreNum)) return '#909399'
      if (scoreNum < 50) return '#67C23A'
      if (scoreNum < 60) return '#409EFF'
      if (scoreNum < 70) return '#E6A23C'
      return '#F56C6C'
    },
    
    getLevelColor(level) {
      if (!level) return '#909399'
      const levelStr = level.toString()
      if (levelStr.includes('正常')) return '#67C23A'
      if (levelStr.includes('轻度')) return '#409EFF'
      if (levelStr.includes('中度')) return '#E6A23C'
      if (levelStr.includes('重度')) return '#F56C6C'
      return '#909399'
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 16px;
  background: #f5f7fa;
  min-height: 100vh;
}

.assessment-container {
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
  margin-bottom: 6px;
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

.header-description {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  line-height: 1.4;
}

.header-right .breadcrumb {
  background: transparent;
  padding: 0;
}

:deep(.breadcrumb .el-breadcrumb__inner) {
  color: rgba(255, 255, 255, 0.8);
}

:deep(.breadcrumb .el-breadcrumb__separator) {
  color: rgba(255, 255, 255, 0.5);
}

/* 量表选择页面 */
.scales-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.scales-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.scale-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  min-height: 180px;
}

.scale-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-color: #409EFF;
}

.scale-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
}

.scale-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
  flex-shrink: 0;
}

.scale-title {
  flex: 1;
}

.scale-title h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.scale-tags {
  display: flex;
  gap: 8px;
}

.scale-content {
  flex: 1;
  padding: 12px 0;
}

.scale-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin: 0 0 12px 0;
}

.scale-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
}

.meta-icon {
  font-size: 14px;
  opacity: 0.8;
}

.scale-footer {
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
}

.start-btn {
  width: 100%;
  border-radius: 6px;
}

/* 测评说明 */
.notice-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.notice-header {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f2f5;
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.notice-icon {
  font-size: 16px;
  color: #409EFF;
}

.notice-content {
  padding: 16px;
}

.notice-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 6px;
}

.item-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: white;
  flex-shrink: 0;
}

.privacy {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.truth {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.reference {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.help {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.item-content h4 {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px 0;
}

.item-content p {
  font-size: 12px;
  color: #606266;
  line-height: 1.4;
  margin: 0;
}

/* 测评页面 */
.test-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.test-header {
  background: white;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #ebeef5;
}

.test-title-section {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.back-btn {
  margin-top: 4px;
  border: 1px solid #dcdfe6;
}

.test-title {
  flex: 1;
}

.test-title h2 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.test-desc {
  font-size: 13px;
  color: #606266;
  margin: 0;
}

.test-progress {
  margin-top: 20px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-text {
  font-size: 13px;
  color: #606266;
}

.progress-percent {
  font-size: 14px;
  font-weight: 600;
  color: #409EFF;
}

.progress-bar {
  border-radius: 3px;
  overflow: hidden;
}

/* 问题卡片 */
.question-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
  padding: 20px;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f2f5;
}

.question-number {
  display: flex;
  align-items: baseline;
  gap: 2px;
  min-width: 80px;
}

.current {
  font-size: 28px;
  font-weight: 700;
  color: #409EFF;
  line-height: 1;
}

.separator {
  font-size: 16px;
  color: #c0c4cc;
}

.total {
  font-size: 16px;
  color: #909399;
}

.question-text h3 {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  line-height: 1.6;
  margin: 0;
}

.options-grid {
  display: grid;
  gap: 12px;
  margin-bottom: 30px;
}

.option-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: white;
}

.option-card:hover {
  border-color: #409EFF;
  background: #f5f9ff;
  transform: translateX(4px);
}

.option-card.selected {
  border-color: #409EFF;
  background: #f5f9ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.option-index {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  flex-shrink: 0;
}

.option-card.selected .option-index {
  background: #409EFF;
  color: white;
}

.option-content {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.option-text {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.option-value {
  font-size: 12px;
  color: #409EFF;
  font-weight: 500;
  padding: 2px 8px;
  background: rgba(64, 158, 255, 0.1);
  border-radius: 4px;
}

.test-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #f0f2f5;
}

.answered-info {
  display: flex;
  align-items: center;
}

.answered-tag {
  border: none;
  background: rgba(103, 194, 58, 0.1);
  color: #67C23A;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.nav-btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 500;
}

.prev-btn {
  border-color: #dcdfe6;
}

.next-btn {
  min-width: 100px;
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
  
  .breadcrumb {
    justify-content: flex-start;
  }
  
  .scales-grid {
    grid-template-columns: 1fr;
  }
  
  .notice-grid {
    grid-template-columns: 1fr;
  }
  
  .question-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .question-number {
    justify-content: center;
  }
  
  .test-footer {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .action-buttons {
    width: 100%;
  }
  
  .nav-btn {
    flex: 1;
  }
}

@media (max-width: 480px) {
  .main-title {
    font-size: 16px;
  }
  
  .header-description {
    font-size: 12px;
  }
  
  .scale-card {
    min-height: 160px;
  }
  
  .question-text h3 {
    font-size: 15px;
  }
  
  .option-text {
    font-size: 13px;
  }
}
</style>