<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="report-header">
        <div class="header-content">
          <el-page-header 
            @back="goBack" 
            :content="reportData ? `${reportData.scaleName}评估报告详情` : '报告详情'"
          />
          <div class="header-actions">
            <el-button 
              type="success" 
              size="mini"
              :loading="exportLoading"
              @click="handleExport"
              :disabled="!reportData"
            >
              <i class="el-icon-download"></i> 导出报告
            </el-button>
            <el-button 
              type="danger" 
              size="mini"
              @click="handleDelete"
              :disabled="!reportData"
            >
              <i class="el-icon-delete"></i> 删除记录
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="6" animated />
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="!reportData && !loading" class="empty">
        <el-empty description="报告不存在或已删除">
          <el-button type="primary" @click="goBack">
            返回报告列表
          </el-button>
        </el-empty>
      </div>
      
      <!-- 报告详情内容 -->
      <div v-else-if="reportData" class="report-content">
        <!-- 报告基本信息 -->
        <div class="basic-info">
          <div class="info-card">
            <div class="info-header">
              <h3><i class="el-icon-document"></i> 报告基本信息</h3>
            </div>
            <div class="info-body">
              <div class="info-row">
                <div class="info-item">
                  <span class="info-label">评估用户：</span>
                  <span class="info-value">{{ reportData.userName }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">量表类型：</span>
                  <span class="info-value">{{ reportData.scaleName || reportData.scaleCode }}</span>
                </div>
              </div>
              <div class="info-row">
                <div class="info-item">
                  <span class="info-label">评估时间：</span>
                  <span class="info-value highlight">{{ formatTime(reportData.assessmentTime) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">报告编号：</span>
                  <span class="info-value">{{ reportData.id }}</span>
                </div>
              </div>
              <div class="info-row">
                <div class="info-item">
                  <span class="info-label">评估结果：</span>
                  <span class="info-value">
                    <el-tag :type="getLevelType(reportData.resultLevel)" size="medium">
                      {{ reportData.resultLevel || '未评估' }}
                    </el-tag>
                  </span>
                </div>
                <div class="info-item">
                  <span class="info-label">风险等级：</span>
                  <span class="info-value">
                    <el-tag :type="getRiskLevelType(reportData.riskLevel)" size="medium">
                      {{ reportData.riskLevel || '正常' }}
                    </el-tag>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 分数展示 -->
        <div class="score-section">
          <h3><i class="el-icon-data-analysis"></i> 评估分数详情</h3>
          <div class="score-cards">
            <div class="score-card total-score">
              <div class="score-icon">
                <i class="el-icon-finished"></i>
              </div>
              <div class="score-content">
                <div class="score-label">原始总分</div>
                <div class="score-value" :class="getScoreClass(reportData.totalScore)">
                  {{ reportData.totalScore || 0 }}
                </div>
                <div class="score-desc">问卷原始得分</div>
              </div>
            </div>
            
            <div class="score-card standard-score">
              <div class="score-icon">
                <i class="el-icon-scale-original"></i>
              </div>
              <div class="score-content">
                <div class="score-label">标准分</div>
                <div class="score-value" :class="getScoreClass(reportData.standardScore)">
                  {{ reportData.standardScore ? reportData.standardScore.toFixed(1) : '0.0' }}
                </div>
                <div class="score-desc">(原始分×1.25)</div>
              </div>
            </div>
            
            <div class="score-card result-card">
              <div class="score-icon">
                <i class="el-icon-medal"></i>
              </div>
              <div class="score-content">
                <div class="score-label">评估等级</div>
                <div class="result-level">
                  <el-tag 
                    :type="getLevelType(reportData.resultLevel)" 
                    size="large"
                    effect="dark"
                  >
                    {{ reportData.resultLevel || '未评估' }}
                  </el-tag>
                </div>
                <div class="score-desc">心理状态评估</div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 详细分析 -->
        <div class="analysis-section">
          <div class="analysis-card">
            <div class="analysis-header">
              <h3><i class="el-icon-reading"></i> 评估结果分析</h3>
            </div>
            <div class="analysis-body">
              <div class="analysis-item">
                <div class="analysis-title">
                  <i class="el-icon-s-opportunity" :style="{color: getAnalysisIconColor()}"></i>
                  <span>量表类型分析</span>
                </div>
                <div class="analysis-content">
                  <p><strong>{{ reportData.scaleName || reportData.scaleCode }}</strong> 是一份专业的心理评估量表，用于测量相关心理状态。</p>
                  <p>本次评估基于量表标准化的计分规则进行，确保了评估结果的科学性和准确性。</p>
                </div>
              </div>
              
              <div class="analysis-item">
                <div class="analysis-title">
                  <i class="el-icon-trophy" :style="{color: getAnalysisIconColor()}"></i>
                  <span>分数解读</span>
                </div>
                <div class="analysis-content">
                  <p>您的原始得分为 <strong>{{ reportData.totalScore }}</strong> 分，经过标准化转换后的标准分为 <strong>{{ reportData.standardScore ? reportData.standardScore.toFixed(1) : '0.0' }}</strong> 分。</p>
                  <p>根据量表的评分标准，您的评估等级为 <strong>{{ reportData.resultLevel }}</strong>，风险等级为 <strong>{{ reportData.riskLevel || '正常' }}</strong>。</p>
                </div>
              </div>
              
              <div class="analysis-item">
                <div class="analysis-title">
                  <i class="el-icon-s-flag" :style="{color: getAnalysisIconColor()}"></i>
                  <span>对比参考</span>
                </div>
                <div class="analysis-content">
                  <p>根据临床心理学标准，该量表的正常范围通常为标准分低于50分。</p>
                  <ul class="reference-list">
                    <li v-if="reportData.scaleCode === 'SDS'">
                      <span class="level-dot normal"></span> <strong>正常范围：</strong>标准分＜53分
                    </li>
                    <li v-if="reportData.scaleCode === 'SDS'">
                      <span class="level-dot mild"></span> <strong>轻度抑郁：</strong>53分≤标准分＜63分
                    </li>
                    <li v-if="reportData.scaleCode === 'SDS'">
                      <span class="level-dot moderate"></span> <strong>中度抑郁：</strong>63分≤标准分＜73分
                    </li>
                    <li v-if="reportData.scaleCode === 'SDS'">
                      <span class="level-dot severe"></span> <strong>重度抑郁：</strong>标准分≥73分
                    </li>
                    <li v-if="reportData.scaleCode === 'SAS'">
                      <span class="level-dot normal"></span> <strong>正常范围：</strong>标准分＜50分
                    </li>
                    <li v-if="reportData.scaleCode === 'SAS'">
                      <span class="level-dot mild"></span> <strong>轻度焦虑：</strong>50分≤标准分＜60分
                    </li>
                    <li v-if="reportData.scaleCode === 'SAS'">
                      <span class="level-dot moderate"></span> <strong>中度焦虑：</strong>60分≤标准分＜70分
                    </li>
                    <li v-if="reportData.scaleCode === 'SAS'">
                      <span class="level-dot severe"></span> <strong>重度焦虑：</strong>标准分≥70分
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 专业建议 -->
        <div class="suggestion-section">
          <div class="suggestion-card">
            <div class="suggestion-header">
              <h3><i class="el-icon-chat-dot-round"></i> 专业建议</h3>
            </div>
            <div class="suggestion-body">
              <div class="suggestion-main">
                <p>{{ reportData.suggestion || '暂无专业建议' }}</p>
              </div>
              
              <div class="suggestion-tips">
                <div class="tip-item" v-if="reportData.resultLevel && reportData.resultLevel.includes('正常')">
                  <i class="el-icon-success" style="color: #67C23A;"></i>
                  <div class="tip-content">
                    <strong>状态良好</strong>
                    <p>您的心理状态良好，请继续保持健康的生活习惯和积极的心态。</p>
                  </div>
                </div>
                
                <div class="tip-item" v-else-if="reportData.resultLevel && reportData.resultLevel.includes('轻度')">
                  <i class="el-icon-warning" style="color: #E6A23C;"></i>
                  <div class="tip-content">
                    <strong>需要关注</strong>
                    <p>建议适当进行自我调节，如运动、冥想、与朋友交流等。关注情绪变化，如有需要可寻求心理支持。</p>
                  </div>
                </div>
                
                <div class="tip-item" v-else-if="reportData.resultLevel && reportData.resultLevel.includes('中度')">
                  <i class="el-icon-warning" style="color: #E6A23C;"></i>
                  <div class="tip-content">
                    <strong>建议干预</strong>
                    <p>建议寻求心理咨询师的帮助，进行专业的心理疏导。定期进行心理评估，关注自身变化。</p>
                  </div>
                </div>
                
                <div class="tip-item" v-else-if="reportData.resultLevel && reportData.resultLevel.includes('重度')">
                  <i class="el-icon-error" style="color: #F56C6C;"></i>
                  <div class="tip-content">
                    <strong>需要专业帮助</strong>
                    <p>强烈建议立即寻求专业心理治疗，必要时请及时就医。您的心理健康非常重要。</p>
                  </div>
                </div>
                
                <div class="tip-item" v-else>
                  <i class="el-icon-info" style="color: #909399;"></i>
                  <div class="tip-content">
                    <strong>温馨提示</strong>
                    <p>请关注您的心理健康状态，如有需要请咨询专业人士。定期进行心理评估有助于保持心理健康。</p>
                  </div>
                </div>
              </div>
              
              <div class="suggestion-actions">
                <el-button type="primary" plain @click="$router.push('/mental/test')">
                  <i class="el-icon-refresh"></i> 重新测评
                </el-button>
                <el-button type="success" plain @click="$router.push('/mental/report')">
                  <i class="el-icon-tickets"></i> 查看其他报告
                </el-button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 答题详情（可展开） -->
        <div class="answers-section" v-if="reportData.answersJson && parsedAnswers.length > 0">
          <el-collapse v-model="activeCollapse">
            <el-collapse-item name="answers">
              <template slot="title">
                <h3 style="margin: 0; display: flex; align-items: center; gap: 8px;">
                  <i class="el-icon-tickets"></i> 详细答题记录
                </h3>
              </template>
              <div class="answers-content">
                <div class="answers-summary">
                  <div class="summary-item">
                    <span class="summary-label">总题数：</span>
                    <span class="summary-value">{{ parsedAnswers.length }}</span>
                  </div>
                  <div class="summary-item">
                    <span class="summary-label">答对题数：</span>
                    <span class="summary-value">{{ getCorrectAnswers() }}</span>
                  </div>
                </div>
                
                <div class="answers-list">
                  <div 
                    v-for="(answer, index) in parsedAnswers" 
                    :key="index"
                    class="answer-item"
                    :class="{'answer-item-odd': index % 2 === 0}"
                  >
                    <div class="answer-header">
                      <span class="question-number">第{{ index + 1 }}题</span>
                      <span class="question-score" :class="getAnswerScoreClass(answer.answer)">
                        得分：{{ answer.answer || 0 }}
                      </span>
                    </div>
                    <div class="question-text">{{ answer.question || `题目${index + 1}` }}</div>
                    <div class="answer-text">
                      <span class="answer-label">所选答案：</span>
                      <span class="answer-value">{{ getAnswerText(answer.answer) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
        
        <!-- 报告底部信息 -->
        <div class="report-footer">
          <div class="footer-content">
            <div class="footer-info">
              <p class="disclaimer">
                <i class="el-icon-info"></i>
                免责声明：本评估结果仅供参考，不能替代专业医疗诊断。如有需要，请咨询专业心理医生或精神科医生。
              </p>
              <p class="timestamp">
                <i class="el-icon-time"></i>
                报告生成时间：{{ formatTime(reportData.assessmentTime) }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getMyRecordDetail, deleteMyRecord } from "@/api/mental/record";
import { parseTime } from "@/utils/ruoyi";
import axios from 'axios';

export default {
  name: "ReportDetail",
  data() {
    return {
      loading: true,
      reportData: null,
      exportLoading: false,
      activeCollapse: [],
      parsedAnswers: [],
      scaleMap: {
        'SDS': '抑郁自评量表(SDS)',
        'SAS': '焦虑自评量表(SAS)',
        'SCL90': '症状自评量表(SCL-90)',
        'PHQ9': '抑郁症筛查量表(PHQ-9)',
        'GAD7': '广泛性焦虑量表(GAD-7)'
      }
    };
  },
  created() {
    this.loadReportDetail();
  },
  methods: {
    // 加载报告详情
    async loadReportDetail() {
      // 优先从query获取ID，如果没有再从params获取
      const recordId = this.$route.query.id || this.$route.params.id;
      
      console.log('=== 加载报告详情 ===');
      console.log('路由参数:', this.$route.params);
      console.log('路由查询:', this.$route.query);
      console.log('获取到的ID:', recordId);
      
      if (!recordId) {
        this.loading = false;
        this.$message.error('未找到报告ID');
        setTimeout(() => this.goBack(), 1500);
        return;
      }

      try {
        this.loading = true;
        console.log('开始调用API获取报告详情，ID:', recordId);
        
        const response = await getMyRecordDetail(recordId);
        console.log('API返回数据:', response);
        
        if (response.code === 200) {
          this.reportData = response.data;
          console.log('报告数据:', this.reportData);
          
          // 解析答案JSON
          if (this.reportData.answersJson) {
            this.parseAnswersJson();
          }
        } else {
          this.$message.error(response.msg || '加载报告失败');
        }
      } catch (error) {
        console.error('加载报告详情失败:', error);
        this.$message.error('加载报告详情失败: ' + (error.message || '未知错误'));
      } finally {
        this.loading = false;
      }
    },

    // 解析答案JSON
    parseAnswersJson() {
      try {
        const answersJson = this.reportData.answersJson;
        console.log('原始答案JSON:', answersJson);
        
        if (typeof answersJson === 'string') {
          this.parsedAnswers = JSON.parse(answersJson);
        } else {
          this.parsedAnswers = answersJson;
        }
        
        console.log('解析后的答案:', this.parsedAnswers);
      } catch (e) {
        console.error('解析答案数据失败:', e);
        this.parsedAnswers = [];
      }
    },

    // 获取答对题数
    getCorrectAnswers() {
      if (!this.parsedAnswers || this.parsedAnswers.length === 0) return 0;
      
      // 假设得分大于0的就是有效答案
      return this.parsedAnswers.filter(answer => answer.answer > 0).length;
    },

    // 获取答案文本
    getAnswerText(answerValue) {
      if (answerValue === 1) return '很少/没有';
      if (answerValue === 2) return '有时';
      if (answerValue === 3) return '经常';
      if (answerValue === 4) return '总是';
      return '未回答';
    },

    // 返回报告列表
    goBack() {
      this.$router.push('/mental/report');
    },

    // 处理导出 - 与列表页保持一致的实现方式
    async handleExport() {
      if (!this.reportData || !this.reportData.id) {
        this.$message.warning('无法导出，报告数据不完整');
        return;
      }

      this.$confirm(`确定要导出"${this.reportData.scaleName || this.reportData.scaleCode}"的评估报告吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          // 显示加载状态
          const loadingInstance = this.$loading({
            lock: true,
            text: '正在生成报告...',
            spinner: 'el-icon-loading',
            background: 'rgba(0, 0, 0, 0.7)'
          });
          
          this.exportLoading = true;
          
          // 生成文件名
          const fileName = this.generateExportFileName();
          
          // 获取token（用于身份验证）
          const token = this.$store.getters.token;
          
          // 使用axios直接下载文件
          const response = await axios({
            url: `${process.env.VUE_APP_BASE_API}/health/record/export/${this.reportData.id}`,
            method: 'GET',
            responseType: 'blob',
            headers: {
              'Authorization': 'Bearer ' + token,
              'Content-Type': 'application/json'
            },
            // 添加超时时间
            timeout: 60000
          });
          
          // 创建下载链接
          const blob = new Blob([response.data], { 
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
          });
          
          const downloadElement = document.createElement('a');
          const href = window.URL.createObjectURL(blob);
          
          downloadElement.href = href;
          downloadElement.download = fileName;
          document.body.appendChild(downloadElement);
          downloadElement.click();
          
          // 清理
          setTimeout(() => {
            document.body.removeChild(downloadElement);
            window.URL.revokeObjectURL(href);
            
            // 关闭加载状态
            loadingInstance.close();
            this.exportLoading = false;
            
            this.$message.success('报告导出成功！文件已开始下载');
          }, 100);
          
        } catch (error) {
          console.error('导出失败:', error);
          
          // 关闭加载状态
          if (this.$loading) {
            this.$loading.close();
          }
          this.exportLoading = false;
          
          let errorMessage = '导出失败：';
          if (error.response) {
            // 服务器返回了错误状态码
            if (error.response.status === 401) {
              errorMessage += '未登录或登录已过期，请重新登录';
            } else if (error.response.status === 403) {
              errorMessage += '没有权限导出此报告';
            } else if (error.response.status === 404) {
              errorMessage += '报告不存在或已被删除';
            } else {
              errorMessage += `服务器错误 (${error.response.status})`;
            }
          } else if (error.request) {
            // 请求已发出但没有收到响应
            errorMessage += '网络错误，请检查网络连接';
          } else {
            // 其他错误
            errorMessage += error.message || '未知错误';
          }
          
          this.$message.error(errorMessage);
        }
      }).catch(() => {
        // 用户取消
        console.log('用户取消导出');
      });
    },

    // 生成导出文件名
    generateExportFileName() {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const day = String(now.getDate()).padStart(2, '0');
      const hour = String(now.getHours()).padStart(2, '0');
      const minute = String(now.getMinutes()).padStart(2, '0');
      const second = String(now.getSeconds()).padStart(2, '0');
      
      // 获取量表名称
      let scaleName = this.reportData.scaleName || this.reportData.scaleCode || '心理评估';
      if (this.scaleMap[this.reportData.scaleCode]) {
        scaleName = this.scaleMap[this.reportData.scaleCode];
      }
      
      // 获取用户名
      const userName = this.reportData.userName || '用户';
      
      // 获取评估结果等级，用于文件名
      const resultLevel = this.reportData.resultLevel || '';
      
      // 生成文件名，移除特殊字符
      let safeFileName = `心理评估报告_${scaleName}_${userName}`;
      if (resultLevel) {
        safeFileName += `_${resultLevel}`;
      }
      safeFileName += `_${year}${month}${day}_${hour}${minute}${second}`;
      
      // 移除文件名中的非法字符
      safeFileName = safeFileName.replace(/[<>:"/\\|?*]/g, '_');
      
      return safeFileName + '.xlsx';
    },

    // 删除记录
    handleDelete() {
      if (!this.reportData || !this.reportData.id) return;
      
      this.$confirm('确定要删除这条评估记录吗？删除后无法恢复。', '警告', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger',
        cancelButtonClass: 'el-button--default'
      }).then(async () => {
        try {
          const response = await deleteMyRecord(this.reportData.id);
          if (response.code === 200) {
            this.$message.success('删除成功');
            this.goBack();
          } else {
            this.$message.error(response.msg || '删除失败');
          }
        } catch (error) {
          console.error('删除失败:', error);
          this.$message.error('删除失败: ' + (error.message || '未知错误'));
        }
      }).catch(() => {
        // 用户取消删除
      });
    },

    // 格式化时间
    formatTime(time) {
      if (!time) return '-';
      try {
        return parseTime(time, '{y}-{m}-{d} {h}:{i}:{s}');
      } catch (error) {
        console.error('格式化时间失败:', error);
        return '-';
      }
    },

    // 获取等级类型
    getLevelType(level) {
      if (!level) return 'info';
      if (level.includes('正常')) return 'success';
      if (level.includes('轻度')) return 'warning';
      if (level.includes('中度')) return 'warning';
      if (level.includes('重度')) return 'danger';
      return 'info';
    },

    // 获取风险等级类型
    getRiskLevelType(riskLevel) {
      if (!riskLevel) return 'info';
      if (riskLevel.includes('正常') || riskLevel.includes('低风险')) return 'success';
      if (riskLevel.includes('中低风险')) return 'warning';
      if (riskLevel.includes('中高风险')) return 'warning';
      if (riskLevel.includes('高风险')) return 'danger';
      return 'info';
    },

    // 获取分数样式类
    getScoreClass(score) {
      if (!score && score !== 0) return '';
      if (score < 50) return 'score-good';
      if (score < 60) return 'score-normal';
      if (score < 70) return 'score-warning';
      return 'score-danger';
    },

    // 获取答案分数样式类
    getAnswerScoreClass(score) {
      if (!score) return '';
      if (score === 1) return 'score-good';
      if (score === 2) return 'score-normal';
      if (score === 3) return 'score-warning';
      if (score === 4) return 'score-danger';
      return '';
    },

    // 获取分析图标颜色
    getAnalysisIconColor() {
      const level = this.reportData.resultLevel;
      if (level && level.includes('正常')) return '#67C23A';
      if (level && level.includes('轻度')) return '#E6A23C';
      if (level && level.includes('中度')) return '#F56C6C';
      if (level && level.includes('重度')) return '#F56C6C';
      return '#409EFF';
    }
  }
};
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.report-header {
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .header-actions {
    display: flex;
    gap: 10px;
  }
}

.loading-container {
  padding: 40px;
  text-align: center;
}

.empty {
  text-align: center;
  padding: 60px 0;
}

.report-content {
  animation: fadeIn 0.5s ease;
}

/* 基本信息 */
.basic-info {
  margin-bottom: 24px;
  
  .info-card {
    background: #fff;
    border-radius: 8px;
    border: 1px solid #e6ebf5;
    overflow: hidden;
    
    .info-header {
      background: linear-gradient(135deg, #f5f7fa, #f0f4ff);
      padding: 16px 20px;
      border-bottom: 1px solid #e6ebf5;
      
      h3 {
        margin: 0;
        color: #303133;
        font-size: 16px;
        display: flex;
        align-items: center;
        gap: 8px;
        
        i {
          color: #409EFF;
        }
      }
    }
    
    .info-body {
      padding: 20px;
    }
    
    .info-row {
      display: flex;
      margin-bottom: 16px;
      &:last-child {
        margin-bottom: 0;
      }
    }
    
    .info-item {
      flex: 1;
      min-width: 300px;
    }
    
    .info-label {
      color: #606266;
      font-weight: 500;
      margin-right: 8px;
    }
    
    .info-value {
      color: #303133;
      
      &.highlight {
        color: #409EFF;
        font-weight: 500;
      }
    }
  }
}

/* 分数展示 */
.score-section {
  margin-bottom: 24px;
  
  h3 {
    margin: 0 0 16px 0;
    color: #303133;
    font-size: 16px;
    display: flex;
    align-items: center;
    gap: 8px;
    
    i {
      color: #409EFF;
    }
  }
  
  .score-cards {
    display: flex;
    gap: 20px;
    flex-wrap: wrap;
    
    .score-card {
      flex: 1;
      min-width: 280px;
      background: #fff;
      border-radius: 8px;
      border: 1px solid #e6ebf5;
      padding: 24px;
      display: flex;
      align-items: center;
      gap: 20px;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
      }
      
      &.total-score {
        border-top: 4px solid #409EFF;
      }
      
      &.standard-score {
        border-top: 4px solid #67C23A;
      }
      
      &.result-card {
        border-top: 4px solid #E6A23C;
      }
      
      .score-icon {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
        color: #fff;
        
        .total-score & {
          background: linear-gradient(135deg, #409EFF, #66b1ff);
        }
        
        .standard-score & {
          background: linear-gradient(135deg, #67C23A, #85ce61);
        }
        
        .result-card & {
          background: linear-gradient(135deg, #E6A23C, #ebb563);
        }
      }
      
      .score-content {
        flex: 1;
        
        .score-label {
          color: #909399;
          font-size: 14px;
          margin-bottom: 8px;
        }
        
        .score-value {
          font-size: 36px;
          font-weight: bold;
          margin-bottom: 4px;
          
          &.score-good {
            color: #67C23A;
          }
          
          &.score-normal {
            color: #409EFF;
          }
          
          &.score-warning {
            color: #E6A23C;
          }
          
          &.score-danger {
            color: #F56C6C;
          }
        }
        
        .result-level {
          margin: 12px 0;
          
          :deep(.el-tag) {
            font-size: 16px;
            padding: 8px 16px;
          }
        }
        
        .score-desc {
          color: #909399;
          font-size: 12px;
        }
      }
    }
  }
}

/* 分析部分 */
.analysis-section {
  margin-bottom: 24px;
  
  .analysis-card {
    background: #fff;
    border-radius: 8px;
    border: 1px solid #e6ebf5;
    overflow: hidden;
    
    .analysis-header {
      background: linear-gradient(135deg, #f5f7fa, #f0f4ff);
      padding: 16px 20px;
      border-bottom: 1px solid #e6ebf5;
      
      h3 {
        margin: 0;
        color: #303133;
        font-size: 16px;
        display: flex;
        align-items: center;
        gap: 8px;
        
        i {
          color: #409EFF;
        }
      }
    }
    
    .analysis-body {
      padding: 20px;
    }
    
    .analysis-item {
      margin-bottom: 24px;
      &:last-child {
        margin-bottom: 0;
      }
    }
    
    .analysis-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;
      color: #303133;
      font-weight: 500;
      
      i {
        font-size: 18px;
      }
    }
    
    .analysis-content {
      color: #606266;
      line-height: 1.6;
      
      p {
        margin: 8px 0;
      }
      
      .reference-list {
        list-style: none;
        padding: 0;
        margin: 12px 0;
        
        li {
          margin: 8px 0;
          display: flex;
          align-items: center;
          gap: 8px;
          
          .level-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            
            &.normal {
              background: #67C23A;
            }
            
            &.mild {
              background: #E6A23C;
            }
            
            &.moderate {
              background: #F56C6C;
            }
            
            &.severe {
              background: #F56C6C;
              box-shadow: 0 0 0 2px rgba(245, 108, 108, 0.2);
            }
          }
        }
      }
    }
  }
}

/* 建议部分 */
.suggestion-section {
  margin-bottom: 24px;
  
  .suggestion-card {
    background: #fff;
    border-radius: 8px;
    border: 1px solid #e6ebf5;
    overflow: hidden;
    
    .suggestion-header {
      background: linear-gradient(135deg, #f5f7fa, #f0f4ff);
      padding: 16px 20px;
      border-bottom: 1px solid #e6ebf5;
      
      h3 {
        margin: 0;
        color: #303133;
        font-size: 16px;
        display: flex;
        align-items: center;
        gap: 8px;
        
        i {
          color: #409EFF;
        }
      }
    }
    
    .suggestion-body {
      padding: 20px;
    }
    
    .suggestion-main {
      background: linear-gradient(135deg, #fdfcfb, #e2d1c3);
      padding: 20px;
      border-radius: 6px;
      margin-bottom: 20px;
      border-left: 4px solid #409EFF;
      
      p {
        margin: 0;
        line-height: 1.8;
        color: #303133;
        font-size: 15px;
        text-indent: 2em;
      }
    }
    
    .suggestion-tips {
      .tip-item {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        padding: 16px;
        background: #f8f9fa;
        border-radius: 6px;
        margin-bottom: 12px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        i {
          font-size: 20px;
          margin-top: 2px;
        }
        
        .tip-content {
          flex: 1;
          
          strong {
            display: block;
            color: #303133;
            margin-bottom: 4px;
            font-size: 15px;
          }
          
          p {
            margin: 0;
            color: #606266;
            line-height: 1.6;
            font-size: 14px;
          }
        }
      }
    }
    
    .suggestion-actions {
      display: flex;
      justify-content: center;
      gap: 16px;
      margin-top: 24px;
      padding-top: 20px;
      border-top: 1px solid #e6ebf5;
      
      .el-button {
        min-width: 120px;
      }
    }
  }
}

/* 答题详情 */
.answers-section {
  margin-bottom: 24px;
  
  :deep(.el-collapse) {
    border: 1px solid #e6ebf5;
    border-radius: 8px;
    overflow: hidden;
    
    .el-collapse-item__header {
      background: linear-gradient(135deg, #f5f7fa, #f0f4ff);
      padding: 0 20px;
      height: 56px;
      font-weight: 500;
      border-bottom: 1px solid #e6ebf5;
    }
    
    .el-collapse-item__wrap {
      background: #fff;
    }
    
    .el-collapse-item__content {
      padding: 0;
    }
  }
  
  .answers-content {
    padding: 20px;
  }
  
  .answers-summary {
    display: flex;
    gap: 32px;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #e6ebf5;
    
    .summary-item {
      .summary-label {
        color: #909399;
        font-size: 14px;
        margin-right: 8px;
      }
      
      .summary-value {
        color: #303133;
        font-weight: 500;
        font-size: 18px;
      }
    }
  }
  
  .answers-list {
    max-height: 400px;
    overflow-y: auto;
    
    .answer-item {
      padding: 16px;
      border-radius: 6px;
      margin-bottom: 12px;
      background: #fff;
      border: 1px solid #e6ebf5;
      transition: all 0.3s ease;
      
      &.answer-item-odd {
        background: #fafafa;
      }
      
      &:hover {
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
        transform: translateX(4px);
      }
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .answer-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
        
        .question-number {
          color: #409EFF;
          font-weight: 500;
          font-size: 15px;
        }
        
        .question-score {
          padding: 2px 8px;
          border-radius: 4px;
          font-size: 13px;
          font-weight: 500;
          
          &.score-good {
            background: rgba(103, 194, 58, 0.1);
            color: #67C23A;
          }
          
          &.score-normal {
            background: rgba(64, 158, 255, 0.1);
            color: #409EFF;
          }
          
          &.score-warning {
            background: rgba(230, 162, 60, 0.1);
            color: #E6A23C;
          }
          
          &.score-danger {
            background: rgba(245, 108, 108, 0.1);
            color: #F56C6C;
          }
        }
      }
      
      .question-text {
        color: #303133;
        font-size: 15px;
        line-height: 1.6;
        margin-bottom: 12px;
        font-weight: 500;
      }
      
      .answer-text {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 14px;
        
        .answer-label {
          color: #909399;
        }
        
        .answer-value {
          color: #409EFF;
          font-weight: 500;
        }
      }
    }
  }
}

/* 报告底部 */
.report-footer {
  margin-top: 24px;
  
  .footer-content {
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    border: 1px solid #e6ebf5;
  }
  
  .footer-info {
    text-align: center;
    
    .disclaimer {
      color: #909399;
      font-size: 14px;
      line-height: 1.6;
      margin-bottom: 12px;
      
      i {
        margin-right: 8px;
        color: #E6A23C;
      }
    }
    
    .timestamp {
      color: #C0C4CC;
      font-size: 13px;
      
      i {
        margin-right: 4px;
      }
    }
  }
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-container {
    padding: 10px;
  }
  
  .report-header .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .score-cards {
    flex-direction: column;
  }
  
  .score-card {
    min-width: 100% !important;
  }
  
  .info-row {
    flex-direction: column;
    gap: 12px;
  }
  
  .info-item {
    min-width: 100% !important;
  }
  
  .suggestion-actions {
    flex-direction: column;
  }
  
  .suggestion-actions .el-button {
    width: 100%;
  }
}

@media (max-width: 992px) {
  .score-cards {
    flex-direction: column;
  }
  
  .score-card {
    min-width: 100%;
  }
}
</style>