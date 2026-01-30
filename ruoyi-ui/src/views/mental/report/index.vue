<template>
  <div class="app-container">
    <el-card class="main-card">
      <div slot="header" class="report-header-container">
        <div class="header-content">
          <div class="title-section">
            <i class="el-icon-tickets header-icon"></i>
            <span class="header-title">我的心理评估报告</span>
            <span class="header-subtitle">全面了解您的心理健康状况</span>
          </div>
          <el-button 
            type="primary" 
            class="new-test-btn"
            @click="$router.push('/mental/test')"
          >
            <i class="el-icon-plus"></i> 开始新测评
          </el-button>
        </div>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-content">
          <el-skeleton :rows="5" animated />
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="reports.length === 0" class="empty-state">
        <div class="empty-content">
          <div class="empty-icon-container">
            <i class="el-icon-document-empty empty-icon"></i>
          </div>
          <h3 class="empty-title">暂无评估报告</h3>
          <p class="empty-desc">开始第一次心理测评，获取专业的评估报告</p>
          <el-button 
            type="primary" 
            class="empty-action-btn"
            @click="$router.push('/mental/test')"
          >
            <i class="el-icon-check"></i> 开始第一次测评
          </el-button>
        </div>
      </div>
      
      <div v-else class="report-content">
        <!-- 最近一次报告 -->
        <div class="latest-report-section">
          <div class="section-title">
            <div class="title-line"></div>
            <h3 class="title-text">最近一次评估报告</h3>
            <div class="title-badge">
              <i class="el-icon-star-on"></i> 最新
            </div>
          </div>
          
          <el-card class="latest-report-card" shadow="hover">
            <div class="report-header">
              <div class="report-meta">
                <div class="scale-info">
                  <span class="scale-icon">
                    <i class="el-icon-monitor"></i>
                  </span>
                  <div>
                    <h4 class="scale-name">{{ latestReport.scaleName || latestReport.scaleCode }}</h4>
                    <span class="report-date">
                      <i class="el-icon-time"></i>
                      {{ formatTime(latestReport.assessmentTime) }}
                    </span>
                  </div>
                </div>
                <el-tag 
                  :type="getLevelType(latestReport.resultLevel)"
                  class="level-tag"
                  effect="dark"
                >
                  {{ latestReport.resultLevel || '未评估' }}
                </el-tag>
              </div>
            </div>
            
            <div class="report-body">
              <div class="score-display-section">
                <div class="score-visual">
                  <div class="score-circle-container">
                    <div 
                      class="score-circle" 
                      :class="getScoreCircleClass(latestReport.totalScore)"
                    >
                      <div class="circle-inner">
                        <div class="score-value">{{ latestReport.totalScore || 0 }}</div>
                        <div class="score-label">总分</div>
                      </div>
                    </div>
                    <div class="score-indicator">
                      <div class="indicator-bar">
                        <div 
                          class="indicator-fill"
                          :style="{ width: getScorePercentage(latestReport.totalScore) + '%' }"
                        ></div>
                      </div>
                      <div class="indicator-labels">
                        <span>正常</span>
                        <span>需关注</span>
                      </div>
                    </div>
                  </div>
                  
                  <div class="score-details">
                    <div class="detail-item">
                      <span class="detail-label">
                        <i class="el-icon-document detail-icon"></i>
                        量表类型
                      </span>
                      <span class="detail-value">{{ getScaleName(latestReport.scaleCode) }}</span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">
                        <i class="el-icon-trophy detail-icon"></i>
                        评估等级
                      </span>
                      <span class="detail-value" :class="'level-' + getLevelType(latestReport.resultLevel)">
                        {{ latestReport.resultLevel || '未评估' }}
                      </span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">
                        <i class="el-icon-data-analysis detail-icon"></i>
                        标准分
                      </span>
                      <span class="detail-value score-number">
                        {{ latestReport.standardScore ? latestReport.standardScore.toFixed(1) : '-' }}
                      </span>
                    </div>
                    <div class="detail-item">
                      <span class="detail-label">
                        <i class="el-icon-date detail-icon"></i>
                        评估时间
                      </span>
                      <span class="detail-value">{{ formatTime(latestReport.assessmentTime) }}</span>
                    </div>
                  </div>
                </div>
                
                <div class="suggestion-section">
                  <div class="suggestion-header">
                    <i class="el-icon-chat-line-round suggestion-icon"></i>
                    <h4>专业建议</h4>
                  </div>
                  <div class="suggestion-content">
                    <p>{{ latestReport.suggestion || '暂无专业建议，请咨询专业心理医生获取个性化指导。' }}</p>
                  </div>
                </div>
              </div>
              
              <div class="report-actions">
                <el-button 
                  type="primary" 
                  class="action-btn"
                  @click="handleViewDetail(latestReport)"
                >
                  <i class="el-icon-view"></i>
                  <span>查看完整报告</span>
                </el-button>
                <el-button 
                  type="success" 
                  class="action-btn export-btn"
                  @click="handleExport(latestReport)"
                >
                  <i class="el-icon-download"></i>
                  <span>导出报告</span>
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
        
        <!-- 历史记录表格 -->
        <div class="history-section">
          <div class="section-title">
            <div class="title-line"></div>
            <h3 class="title-text">历史评估记录</h3>
            <div class="record-count">
              <span class="count-number">{{ total }}</span>
              <span class="count-label">条记录</span>
            </div>
          </div>
          
          <el-card class="history-table-card" shadow="never">
            <el-table 
              :data="reports" 
              class="history-table"
              :default-sort="{prop: 'assessmentTime', order: 'descending'}"
              style="width: 100%"
            >
              <el-table-column prop="assessmentTime" label="评估时间" width="180" sortable>
                <template slot-scope="scope">
                  <div class="time-cell">
                    <i class="el-icon-time time-icon"></i>
                    {{ formatTime(scope.row.assessmentTime) }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="scaleCode" label="量表类型" width="140">
                <template slot-scope="scope">
                  <div class="scale-cell">
                    <span class="scale-icon-small">
                      <i class="el-icon-monitor"></i>
                    </span>
                    {{ getScaleName(scope.row.scaleCode) }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="totalScore" label="总分" width="100" sortable>
                <template slot-scope="scope">
                  <div class="score-cell" :class="getScoreClass(scope.row.totalScore)">
                    {{ scope.row.totalScore }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="standardScore" label="标准分" width="100">
                <template slot-scope="scope">
                  <div class="standard-score">
                    {{ scope.row.standardScore ? scope.row.standardScore.toFixed(1) : '-' }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="resultLevel" label="评估结果" width="120">
                <template slot-scope="scope">
                  <el-tag 
                    :type="getLevelType(scope.row.resultLevel)" 
                    class="result-tag"
                    effect="light"
                  >
                    {{ scope.row.resultLevel || '未评估' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="280" fixed="right">
                <template slot-scope="scope">
                  <div class="action-buttons">
                    <el-button 
                      type="text" 
                      size="small" 
                      class="view-btn"
                      @click="handleViewDetail(scope.row)"
                    >
                      <i class="el-icon-view"></i>
                      查看
                    </el-button>
                    <el-button 
                      type="text" 
                      size="small" 
                      class="export-btn-table"
                      @click="handleExport(scope.row)"
                    >
                      <i class="el-icon-download"></i>
                      导出
                    </el-button>
                    <el-button 
                      type="text" 
                      size="small" 
                      class="delete-btn"
                      @click="handleDelete(scope.row.id)"
                    >
                      <i class="el-icon-delete"></i>
                      删除
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
            
            <!-- 分页 -->
            <pagination
              v-show="total>0"
              :total="total"
              :page.sync="queryParams.pageNum"
              :limit.sync="queryParams.pageSize"
              @pagination="fetchReports"
              class="table-pagination"
            />
          </el-card>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getMyRecordList, deleteMyRecord, exportReport } from '@/api/mental/record'
import { parseTime } from '@/utils/ruoyi'

export default {
  name: 'MentalReport',
  data() {
    return {
      loading: true,
      reports: [],
      latestReport: {},
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10
      },
      scaleMap: {
        'SDS': '抑郁自评量表(SDS)',
        'SAS': '焦虑自评量表(SAS)',
        'SCL90': '症状自评量表(SCL-90)',
        'PHQ9': '抑郁症筛查量表(PHQ-9)',
        'GAD7': '广泛性焦虑量表(GAD-7)'
      }
    }
  },
  mounted() {
    this.fetchReports()
  },
  methods: {
    async fetchReports() {
      this.loading = true
      try {
        const response = await getMyRecordList(this.queryParams)
        
        if (response.code === 200) {
          let actualData = response
          
          if (response.data && typeof response.data === 'object') {
            if (response.data.rows !== undefined || response.data.total !== undefined) {
              actualData = response.data
            }
          }
          
          this.reports = actualData.rows || actualData.list || actualData.data || []
          
          if (actualData.total !== undefined) {
            this.total = actualData.total
          } else if (response.total !== undefined) {
            this.total = response.total
          } else {
            this.total = this.reports.length
          }
          
          if (this.reports.length > 0) {
            const sortedReports = [...this.reports].sort((a, b) => 
              new Date(b.assessmentTime) - new Date(a.assessmentTime)
            )
            this.latestReport = sortedReports[0]
          }
        } else {
          this.$message.error(response.msg || '获取报告失败')
        }
      } catch (error) {
        console.error('获取报告失败:', error)
        this.$message.error('获取报告失败，请检查网络连接')
      } finally {
        this.loading = false
      }
    },
    
    getScaleName(code) {
      return this.scaleMap[code] || code || '未知量表'
    },
    
    formatTime(time) {
      if (!time) return '-'
      try {
        return parseTime(time, '{y}-{m}-{d} {h}:{i}')
      } catch (error) {
        console.error('格式化时间失败:', time, error)
        return '-'
      }
    },
    
    getLevelType(level) {
      if (!level) return 'info'
      if (level.includes('正常')) return 'success'
      if (level.includes('轻度')) return 'warning'
      if (level.includes('中度')) return 'warning'
      if (level.includes('重度')) return 'danger'
      return 'info'
    },
    
    getScoreClass(score) {
      if (!score && score !== 0) return ''
      if (score < 50) return 'score-good'
      if (score < 60) return 'score-normal'
      if (score < 70) return 'score-warning'
      return 'score-danger'
    },
    
    getScoreCircleClass(score) {
      if (!score && score !== 0) return 'score-circle-default'
      if (score < 50) return 'score-circle-good'
      if (score < 60) return 'score-circle-normal'
      if (score < 70) return 'score-circle-warning'
      return 'score-circle-danger'
    },
    
    getScorePercentage(score) {
      if (!score && score !== 0) return 0
      const maxScore = 100
      return Math.min((score / maxScore) * 100, 100)
    },
    
    handleExport(report) {
      this.$confirm(`确定要导出"${report.scaleName || report.scaleCode}"的评估报告吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          const loading = this.$loading({
            lock: true,
            text: '正在生成报告...',
            spinner: 'el-icon-loading',
            background: 'rgba(0, 0, 0, 0.7)'
          })
          
          const response = await exportReport(report.id)
          
          const blob = new Blob([response], { 
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
          })
          
          const downloadElement = document.createElement('a')
          const href = window.URL.createObjectURL(blob)
          const fileName = this.generateExportFileName(report)
          
          downloadElement.href = href
          downloadElement.download = fileName
          document.body.appendChild(downloadElement)
          downloadElement.click()
          
          setTimeout(() => {
            document.body.removeChild(downloadElement)
            window.URL.revokeObjectURL(href)
            loading.close()
            this.$message.success('报告导出成功！文件已开始下载')
          }, 100)
          
        } catch (error) {
          console.error('导出失败:', error)
          this.$message.error('导出失败：' + (error.message || '未知错误'))
          
          if (this.$loading) {
            this.$loading.close()
          }
        }
      }).catch(() => {
        console.log('用户取消导出')
      })
    },
    
    handleViewDetail(report) {
      console.log('=== 查看报告详情 ===')
      console.log('报告数据:', report)
      console.log('报告ID:', report.id)
      
      if (!report || !report.id) {
        this.$message.error('报告数据不完整，无法查看详情')
        return
      }
      
      this.$router.push({
        name: 'ReportDetail',
        params: { 
          id: report.id 
        },
        query: {
          from: 'report',
          t: new Date().getTime()
        }
      })
    },
    
    generateExportFileName(report) {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const hour = String(now.getHours()).padStart(2, '0')
      const minute = String(now.getMinutes()).padStart(2, '0')
      
      let scaleName = report.scaleName || report.scaleCode || '心理评估'
      if (this.scaleMap[report.scaleCode]) {
        scaleName = this.scaleMap[report.scaleCode]
      }
      
      const userName = report.userName || '用户'
      
      return `心理评估报告_${scaleName}_${userName}_${year}${month}${day}_${hour}${minute}.xlsx`
    },
    
    handleDelete(id) {
      this.$confirm('确定要删除这条评估记录吗？删除后无法恢复。', '提示', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }).then(async () => {
        try {
          await deleteMyRecord(id)
          this.$message.success('删除成功')
          this.fetchReports()
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败: ' + (error.message || '操作失败'))
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
/* ===== 基础样式 ===== */
.app-container {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f4ff 100%);
  min-height: calc(100vh - 84px);
}

.main-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.08);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  overflow: hidden;
}

.main-card :deep(.el-card__header) {
  border-bottom: 1px solid rgba(224, 230, 237, 0.8);
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  padding: 24px 32px;
}

/* ===== 头部样式 ===== */
.report-header-container {
  width: 100%;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  font-size: 28px;
  color: #667eea;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-title {
  font-size: 24px;
  font-weight: 700;
  color: #2d3748;
  letter-spacing: -0.5px;
}

.header-subtitle {
  font-size: 14px;
  color: #718096;
  background: #edf2f7;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 500;
}

.new-test-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 10px;
  padding: 10px 24px;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.new-test-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* ===== 加载状态 ===== */
.loading-container {
  padding: 80px 20px;
}

.loading-content {
  max-width: 800px;
  margin: 0 auto;
}

/* ===== 空状态 ===== */
.empty-state {
  padding: 80px 20px;
  text-align: center;
}

.empty-content {
  max-width: 480px;
  margin: 0 auto;
}

.empty-icon-container {
  width: 120px;
  height: 120px;
  margin: 0 auto 24px;
  background: linear-gradient(135deg, #f6f8ff 0%, #f0f4ff 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.1);
}

.empty-icon {
  font-size: 56px;
  color: #c3dafe;
}

.empty-title {
  font-size: 24px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 12px;
}

.empty-desc {
  color: #718096;
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 32px;
}

.empty-action-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 10px;
  padding: 12px 32px;
  font-weight: 600;
  font-size: 16px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.empty-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* ===== 报告内容 ===== */
.report-content {
  padding: 8px;
}

/* ===== 最近报告部分 ===== */
.latest-report-section {
  margin-bottom: 40px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.title-line {
  width: 4px;
  height: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

.title-text {
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
}

.title-badge {
  background: linear-gradient(135deg, #ffd666 0%, #ffa726 100%);
  color: #7a4f01;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.record-count {
  background: #edf2f7;
  padding: 4px 12px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: auto;
}

.count-number {
  font-weight: 700;
  color: #667eea;
  font-size: 16px;
}

.count-label {
  color: #718096;
  font-size: 12px;
}

/* ===== 最新报告卡片 ===== */
.latest-report-card {
  border: none;
  border-radius: 16px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 4px 20px rgba(31, 38, 135, 0.08);
  transition: all 0.3s ease;
}

.latest-report-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(31, 38, 135, 0.12);
}

.latest-report-card :deep(.el-card__body) {
  padding: 32px;
}

.report-header {
  margin-bottom: 24px;
}

.report-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.scale-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.scale-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.scale-name {
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 4px 0;
}

.report-date {
  font-size: 14px;
  color: #718096;
  display: flex;
  align-items: center;
  gap: 6px;
}

.level-tag {
  font-weight: 600;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  border: none;
}

/* ===== 分数显示 ===== */
.score-display-section {
  margin-bottom: 32px;
}

.score-visual {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 40px;
  align-items: center;
  margin-bottom: 32px;
}

@media (max-width: 768px) {
  .score-visual {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}

.score-circle-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.score-circle {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.score-circle::before {
  content: '';
  position: absolute;
  top: -6px;
  left: -6px;
  right: -6px;
  bottom: -6px;
  border-radius: 50%;
  background: inherit;
  filter: blur(12px);
  opacity: 0.6;
  z-index: -1;
}

.score-circle-default {
  background: linear-gradient(135deg, #a0aec0 0%, #718096 100%);
}

.score-circle-good {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
}

.score-circle-normal {
  background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);
}

.score-circle-warning {
  background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);
}

.score-circle-danger {
  background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
}

.circle-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.score-value {
  font-size: 48px;
  font-weight: 800;
  color: white;
  line-height: 1;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.score-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 8px;
  font-weight: 500;
  letter-spacing: 1px;
}

.score-indicator {
  width: 100%;
  max-width: 200px;
}

.indicator-bar {
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.indicator-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
  transition: width 1s ease-in-out;
}

.indicator-labels {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #718096;
}

/* ===== 分数详情 ===== */
.score-details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

@media (max-width: 992px) {
  .score-details {
    grid-template-columns: 1fr;
  }
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.detail-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.detail-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4a5568;
  font-weight: 500;
}

.detail-icon {
  color: #667eea;
  font-size: 16px;
}

.detail-value {
  font-weight: 600;
  color: #2d3748;
}

.level-success {
  color: #38a169;
}

.level-warning {
  color: #dd6b20;
}

.level-danger {
  color: #e53e3e;
}

.level-info {
  color: #718096;
}

.score-number {
  font-size: 20px;
  font-weight: 700;
}

/* ===== 建议部分 ===== */
.suggestion-section {
  background: linear-gradient(135deg, #f8fafc 0%, #edf2f7 100%);
  border-radius: 16px;
  padding: 24px;
  border-left: 4px solid #667eea;
}

.suggestion-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.suggestion-icon {
  font-size: 24px;
  color: #667eea;
}

.suggestion-header h4 {
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
}

.suggestion-content {
  color: #4a5568;
  line-height: 1.8;
  font-size: 15px;
}

/* ===== 操作按钮 ===== */
.report-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  padding-top: 32px;
  border-top: 1px solid #e2e8f0;
}

.action-btn {
  padding: 12px 32px;
  border-radius: 10px;
  font-weight: 600;
  border: none;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn:first-child {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.export-btn {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

/* ===== 历史记录表格 ===== */
.history-section {
  margin-top: 40px;
}

.history-table-card {
  border: none;
  border-radius: 16px;
  background: white;
  box-shadow: 0 4px 20px rgba(31, 38, 135, 0.08);
}

.history-table-card :deep(.el-card__body) {
  padding: 24px;
}

/* 表格样式 */
.history-table {
  border-radius: 12px;
  overflow: hidden;
}

.history-table :deep(.el-table__header) {
  background: linear-gradient(135deg, #f8fafc 0%, #edf2f7 100%);
}

.history-table :deep(.el-table th) {
  background: transparent;
  color: #4a5568;
  font-weight: 600;
  border-bottom: 2px solid #e2e8f0;
  font-size: 14px;
}

.history-table :deep(.el-table td) {
  border-bottom: 1px solid #edf2f7;
  padding: 16px 0;
}

.history-table :deep(.el-table tr:hover) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.04) 0%, rgba(118, 75, 162, 0.04) 100%);
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4a5568;
}

.time-icon {
  color: #a0aec0;
}

.scale-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #2d3748;
}

.scale-icon-small {
  color: #667eea;
}

.score-cell {
  font-weight: 700;
  font-size: 18px;
  text-align: center;
  padding: 8px;
  border-radius: 8px;
}

.score-good {
  background: rgba(72, 187, 120, 0.1);
  color: #38a169;
}

.score-normal {
  background: rgba(66, 153, 225, 0.1);
  color: #3182ce;
}

.score-warning {
  background: rgba(237, 137, 54, 0.1);
  color: #dd6b20;
}

.score-danger {
  background: rgba(245, 101, 101, 0.1);
  color: #e53e3e;
}

.standard-score {
  font-weight: 600;
  color: #4a5568;
  text-align: center;
}

.result-tag {
  font-weight: 600;
  border: none;
  border-radius: 20px;
  padding: 4px 12px;
  font-size: 12px;
}

/* 表格操作按钮 */
.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.view-btn {
  color: #667eea;
  font-weight: 500;
}

.view-btn:hover {
  color: #764ba2;
  background: rgba(102, 126, 234, 0.1);
}

.export-btn-table {
  color: #38a169;
  font-weight: 500;
}

.export-btn-table:hover {
  color: #2f855a;
  background: rgba(56, 161, 105, 0.1);
}

.delete-btn {
  color: #f56565;
  font-weight: 500;
}

.delete-btn:hover {
  color: #e53e3e;
  background: rgba(245, 101, 101, 0.1);
}

/* 分页样式 */
.table-pagination {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-container {
    padding: 12px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .new-test-btn {
    width: 100%;
  }
  
  .report-actions {
    flex-direction: column;
  }
  
  .action-btn {
    width: 100%;
    justify-content: center;
  }
  
  .action-buttons {
    flex-direction: column;
    align-items: stretch;
    gap: 4px;
  }
  
  .action-buttons .el-button {
    width: 100%;
    justify-content: center;
  }
  
  .latest-report-card :deep(.el-card__body),
  .history-table-card :deep(.el-card__body) {
    padding: 20px;
  }
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.latest-report-section {
  animation: fadeInUp 0.6s ease;
}

.history-section {
  animation: fadeInUp 0.8s ease;
}

/* 分数圆环动画 */
@keyframes scoreRing {
  0% {
    stroke-dashoffset: 100;
  }
  100% {
    stroke-dashoffset: 0;
  }
}

.score-circle {
  animation: pulse 2s infinite ease-in-out;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.02);
  }
  100% {
    transform: scale(1);
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}
</style>