<template>
  <div class="app-container psychologist-dashboard">
    <!-- 欢迎区域 -->
    <el-card class="welcome-card" shadow="never">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>欢迎回来，{{ psychologistName }} </h2>
          <p class="welcome-subtitle">今天是 {{ formattedDate }}，当前时间 {{ currentTime }}</p>
        </div>
        <div class="welcome-stats">
          <div class="stat-item">
            <span class="stat-label">今日预约</span>
            <span class="stat-value">{{ statistics.todayAppointments || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">待处理</span>
            <span class="stat-value">{{ statistics.pendingAppointments || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">已完成</span>
            <span class="stat-value">{{ statistics.completedSessions || 0 }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 快捷功能卡片 -->
    <el-row :gutter="20" class="quick-cards">
      <el-col :xs="12" :sm="6" :md="6">
        <el-card class="quick-card" shadow="hover" @click.native="gotoSchedule">
          <div class="card-content">
            <div class="card-icon" style="background: linear-gradient(135deg, #409EFF, #66b1ff);">
              <i class="el-icon-date"></i>
            </div>
            <div class="card-text">
              <h4>排班管理</h4>
              <p>设置工作时间</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="6" :md="6">
        <el-card class="quick-card" shadow="hover" @click.native="gotoAppointments">
          <div class="card-content">
            <div class="card-icon" style="background: linear-gradient(135deg, #67C23A, #85ce61);">
              <i class="el-icon-tickets"></i>
            </div>
            <div class="card-text">
              <h4>预约管理</h4>
              <p>处理用户预约</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="6" :md="6">
        <el-card class="quick-card" shadow="hover" @click.native="gotoProfileManagement">
          <div class="card-content">
            <div class="card-icon" style="background: linear-gradient(135deg, #E6A23C, #ebb563);">
              <i class="el-icon-folder"></i>
            </div>
            <div class="card-text">
              <h4>档案管理</h4>
              <p>查看用户档案</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="6" :md="6">
        <el-card class="quick-card" shadow="hover" @click.native="gotoSettings">
          <div class="card-content">
            <div class="card-icon" style="background: linear-gradient(135deg, #F56C6C, #f78989);">
              <i class="el-icon-setting"></i>
            </div>
            <div class="card-text">
              <h4>个人设置</h4>
              <p>修改个人信息</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 今日工作区 -->
    <el-row :gutter="20" class="work-section">
      <!-- 今日预约列表 -->
      <el-col :lg="16" :md="24">
        <el-card class="today-appointments" shadow="hover">
          <div class="card-header">
            <div class="header-left">
              <i class="el-icon-date"></i>
              <span>今日预约列表</span>
              <el-date-picker
                v-model="selectedDate"
                type="date"
                placeholder="选择日期"
                format="yyyy-MM-dd"
                value-format="yyyy-MM-dd"
                @change="loadTodayAppointments"
                size="small"
                style="margin-left: 15px; width: 150px;"
              />
            </div>
            <el-button 
              type="primary" 
              icon="el-icon-refresh" 
              @click="refreshData"
              :loading="loading"
              size="small"
              circle
            ></el-button>
          </div>
          
          <div class="card-body">
            <div v-if="todayAppointments.length === 0" class="empty-state">
              <i class="el-icon-date"></i>
              <p>今日暂无预约</p>
            </div>
            
            <el-table 
              :data="todayAppointments" 
              v-loading="loading"
              style="width: 100%"
              :default-sort="{prop: 'timeSlot', order: 'ascending'}"
              height="400"
            >
              <el-table-column prop="timeSlot" label="时间段" width="120" sortable>
                <template slot-scope="scope">
                  <el-tag 
                    size="small" 
                    :type="getTimeSlotType(scope.row.timeSlot)"
                    effect="plain"
                  >
                    {{ formatTimeSlot(scope.row.timeSlot) }}
                  </el-tag>
                </template>
              </el-table-column>
              
              <el-table-column prop="userName" label="用户" width="100">
                <template slot-scope="scope">
                  <div class="user-info">
                    <el-avatar 
                      size="small" 
                      :src="scope.row.userAvatar"
                      style="margin-right: 8px;"
                    ></el-avatar>
                    <span>{{ scope.row.userName }}</span>
                  </div>
                </template>
              </el-table-column>
              
              <el-table-column prop="problemDescription" label="咨询问题" min-width="180">
                <template slot-scope="scope">
                  <div class="problem-description">
                    {{ scope.row.problemDescription || '未填写' }}
                  </div>
                </template>
              </el-table-column>
              
              <el-table-column prop="urgencyLevel" label="紧急程度" width="90">
                <template slot-scope="scope">
                  <el-tag 
                    :type="getUrgencyType(scope.row.urgencyLevel)" 
                    size="small"
                    effect="light"
                  >
                    {{ getUrgencyText(scope.row.urgencyLevel) }}
                  </el-tag>
                </template>
              </el-table-column>
              
              <el-table-column prop="status" label="状态" width="90">
                <template slot-scope="scope">
                  <el-tag 
                    :type="getStatusType(scope.row.status)" 
                    size="small"
                    :effect="scope.row.status === 'pending' ? 'dark' : 'light'"
                  >
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              
              <el-table-column label="操作" width="140" fixed="right">
                <template slot-scope="scope">
                  <div class="action-buttons">
                    <!-- <el-tooltip content="查看详情" placement="top">
                      <el-button 
                        size="mini" 
                        type="text"
                        @click="viewAppointment(scope.row)"
                        icon="el-icon-view"
                      ></el-button>
                    </el-tooltip> -->
                    
                    <el-tooltip content="接受预约" placement="top" v-if="scope.row.status === 'pending'">
                      <el-button 
                        size="mini" 
                        type="text"
                        @click="acceptAppointment(scope.row.id)"
                        icon="el-icon-check"
                        style="color: #67C23A;"
                      ></el-button>
                    </el-tooltip>
                    
                    <el-tooltip content="拒绝预约" placement="top" v-if="scope.row.status === 'pending'">
                      <el-button 
                        size="mini" 
                        type="text"
                        @click="rejectAppointment(scope.row.id)"
                        icon="el-icon-close"
                        style="color: #F56C6C;"
                      ></el-button>
                    </el-tooltip>
                    
                    <el-tooltip content="完成咨询" placement="top" v-if="scope.row.status === 'accepted'">
                      <el-button 
                        size="mini" 
                        type="text"
                        @click="completeAppointment(scope.row)"
                        icon="el-icon-finished"
                        style="color: #E6A23C;"
                      ></el-button>
                    </el-tooltip>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
      
      <!-- 统计概览 -->
      <el-col :lg="8" :md="24">
        <el-card class="statistics-overview" shadow="hover">
          <div class="card-header">
            <i class="el-icon-data-analysis"></i>
            <span>统计概览</span>
          </div>
          
          <div class="card-body">
            <div class="statistics-grid">
              <div class="stat-item-large">
                <div class="stat-icon-large" style="background: linear-gradient(135deg, #409EFF, #66b1ff);">
                  <i class="el-icon-date"></i>
                </div>
                <div class="stat-content-large">
                  <div class="stat-number-large">{{ statistics.todayAppointments || 0 }}</div>
                  <div class="stat-title-large">今日预约</div>
                  <div class="stat-change" v-if="statistics.todayAppointmentsChange">
                    <i :class="statistics.todayAppointmentsChange > 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
                    {{ Math.abs(statistics.todayAppointmentsChange) }}
                  </div>
                </div>
              </div>
              
              <div class="stat-item-large">
                <div class="stat-icon-large" style="background: linear-gradient(135deg, #E6A23C, #ebb563);">
                  <i class="el-icon-time"></i>
                </div>
                <div class="stat-content-large">
                  <div class="stat-number-large">{{ statistics.pendingAppointments || 0 }}</div>
                  <div class="stat-title-large">待处理</div>
                  <div class="stat-subtitle">需要尽快处理</div>
                </div>
              </div>
              
              <div class="stat-item-large">
                <div class="stat-icon-large" style="background: linear-gradient(135deg, #67C23A, #85ce61);">
                  <i class="el-icon-check"></i>
                </div>
                <div class="stat-content-large">
                  <div class="stat-number-large">{{ statistics.completedSessions || 0 }}</div>
                  <div class="stat-title-large">已完成</div>
                  <div class="stat-subtitle">本月累计</div>
                </div>
              </div>
              
              <div class="stat-item-large">
                <div class="stat-icon-large" style="background: linear-gradient(135deg, #F56C6C, #f78989);">
                  <i class="el-icon-star-on"></i>
                </div>
                <div class="stat-content-large">
                  <div class="stat-number-large">{{ statistics.averageRating || '4.8' }}</div>
                  <div class="stat-title-large">平均评分</div>
                  <div class="rating-stars">
                    <i class="el-icon-star-on" v-for="n in 5" :key="n"></i>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="upcoming-tips">
              <div class="tips-header">
                <i class="el-icon-light-rain"></i>
                <span>工作提示</span>
              </div>
              <ul class="tips-list">
                <li>请及时处理待确认的预约</li>
                <li>完成咨询后及时填写咨询记录</li>
                <li>保持咨询环境的安静与私密</li>
                <li>定期查看用户档案了解情况</li>
              </ul>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 完成咨询对话框 -->
    <el-dialog
      title="完成咨询"
      :visible.sync="completeDialogVisible"
      width="500px"
      center
    >
      <el-form :model="completeForm" ref="completeForm" label-width="80px">
        <el-form-item label="咨询记录" prop="counselingNotes" required>
          <el-input
            type="textarea"
            :rows="5"
            v-model="completeForm.counselingNotes"
            placeholder="请详细填写本次咨询的主要内容、分析和建议..."
            maxlength="500"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="咨询总结" prop="summary">
          <el-input
            v-model="completeForm.summary"
            placeholder="简要总结本次咨询的核心内容"
            maxlength="100"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="completeDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleComplete" :loading="completing">
          完成咨询
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getTodayAppointmentsForPsychologist, 
  getPendingAppointmentsForPsychologist,
  getPsychologistDashboard,
  acceptAppointment,
  rejectAppointment,
  completeAppointment
} from '@/api/mental/appointment'

export default {
  name: 'PsychologistIndex',
  data() {
    return {
      loading: false,
      selectedDate: new Date().toISOString().split('T')[0],
      psychologistName: '',
      currentTime: '',
      formattedDate: '',
      
      statistics: {
        todayAppointments: 0,
        pendingAppointments: 0,
        completedSessions: 0,
        averageRating: 4.8,
        todayAppointmentsChange: 2
      },
      
      todayAppointments: [],
      
      completeDialogVisible: false,
      completing: false,
      completeForm: {
        appointmentId: null,
        counselingNotes: '',
        summary: ''
      },
      
      timeSlots: {
        'morning_1': '09:00-10:00',
        'morning_2': '10:00-11:00',
        'morning_3': '11:00-12:00',
        'afternoon_1': '14:00-15:00',
        'afternoon_2': '15:00-16:00',
        'afternoon_3': '16:00-17:00',
        'evening_1': '18:00-19:00',
        'evening_2': '19:00-20:00'
      },
      
      urgencyLevels: {
        'low': { text: '一般', type: 'info' },
        'medium': { text: '中等', type: 'warning' },
        'high': { text: '紧急', type: 'danger' }
      },
      
      statusMap: {
        'pending': { text: '待确认', type: 'warning' },
        'accepted': { text: '已接受', type: 'success' },
        'rejected': { text: '已拒绝', type: 'danger' },
        'completed': { text: '已完成', type: 'info' },
        'cancelled': { text: '已取消', type: 'info' }
      }
    }
  },
  created() {
    this.loadData()
    this.updateDateTime()
    this.timeInterval = setInterval(this.updateDateTime, 1000)
    this.loadUserInfo()
  },
  beforeDestroy() {
    if (this.timeInterval) {
      clearInterval(this.timeInterval)
    }
  },
  methods: {
    loadUserInfo() {
      try {
        const userInfoStr = localStorage.getItem('health_user_info')
        if (userInfoStr) {
          const userInfo = JSON.parse(userInfoStr)
          this.psychologistName = userInfo.nickname || userInfo.username || '尊敬的咨询师'
        } else {
          this.psychologistName = '尊敬的咨询师'
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
        this.psychologistName = '尊敬的咨询师'
      }
    },
    
    async loadData() {
      this.loading = true
      try {
        await Promise.all([
          this.loadStatistics(),
          this.loadTodayAppointments()
        ])
      } catch (error) {
        console.error('加载数据失败:', error)
        this.$message.error('加载数据失败，请重试')
      } finally {
        this.loading = false
      }
    },
    
    async loadStatistics() {
      try {
        const response = await getPsychologistDashboard()
        if (response.code === 200) {
          this.statistics = response.data
        }
      } catch (error) {
        console.error('加载统计信息失败:', error)
      }
    },
    
    async loadTodayAppointments() {
      try {
        const response = await getTodayAppointmentsForPsychologist(this.selectedDate)
        if (response.code === 200) {
          this.todayAppointments = response.rows || []
        }
      } catch (error) {
        console.error('加载今日预约失败:', error)
      }
    },
    
    refreshData() {
      this.loadData()
    },
    
    updateDateTime() {
      const now = new Date()
      
      // 格式化日期
      const year = now.getFullYear()
      const month = (now.getMonth() + 1).toString().padStart(2, '0')
      const day = now.getDate().toString().padStart(2, '0')
      const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      const weekday = weekdays[now.getDay()]
      
      this.formattedDate = `${year}年${month}月${day}日 ${weekday}`
      
      // 格式化时间
      const hours = now.getHours().toString().padStart(2, '0')
      const minutes = now.getMinutes().toString().padStart(2, '0')
      const seconds = now.getSeconds().toString().padStart(2, '0')
      
      this.currentTime = `${hours}:${minutes}:${seconds}`
    },
    
    formatTimeSlot(timeSlot) {
      return this.timeSlots[timeSlot] || timeSlot
    },
    
    getTimeSlotType(timeSlot) {
      // 根据时间段返回不同的tag类型
      if (timeSlot.includes('morning')) return 'primary'
      if (timeSlot.includes('afternoon')) return 'success'
      if (timeSlot.includes('evening')) return 'warning'
      return 'info'
    },
    
    getUrgencyText(urgencyLevel) {
      return this.urgencyLevels[urgencyLevel]?.text || urgencyLevel
    },
    
    getUrgencyType(urgencyLevel) {
      return this.urgencyLevels[urgencyLevel]?.type || 'info'
    },
    
    getStatusText(status) {
      return this.statusMap[status]?.text || status
    },
    
    getStatusType(status) {
      return this.statusMap[status]?.type || 'info'
    },
    
    viewAppointment(appointment) {
      // 跳转到预约详情页面
      this.$router.push({
        path: '/mental/appointment/detail',
        query: { id: appointment.id }
      })
    },
    
    async acceptAppointment(id) {
      try {
        const response = await acceptAppointment(id)
        if (response.code === 200) {
          this.$message.success('预约已接受')
          this.refreshData()
        }
      } catch (error) {
        this.$message.error('操作失败')
      }
    },
    
    async rejectAppointment(id) {
      try {
        this.$confirm('确定要拒绝这个预约吗？', '确认操作', {
          type: 'warning',
          confirmButtonText: '确定拒绝',
          cancelButtonText: '取消'
        }).then(async () => {
          const response = await rejectAppointment(id)
          if (response.code === 200) {
            this.$message.success('预约已拒绝')
            this.refreshData()
          }
        }).catch(() => {})
      } catch (error) {
        this.$message.error('操作失败')
      }
    },
    
    completeAppointment(appointment) {
      this.completeForm.appointmentId = appointment.id
      this.completeForm.counselingNotes = appointment.counselingNotes || ''
      this.completeForm.summary = ''
      this.completeDialogVisible = true
    },
    
    async handleComplete() {
      if (!this.completeForm.counselingNotes.trim()) {
        this.$message.error('请填写咨询记录')
        return
      }
      
      this.completing = true
      try {
        const response = await completeAppointment(
          this.completeForm.appointmentId,
          {
            counselingNotes: this.completeForm.counselingNotes,
            summary: this.completeForm.summary
          }
        )
        if (response.code === 200) {
          this.$message.success('咨询已完成')
          this.completeDialogVisible = false
          this.refreshData()
        } else {
          this.$message.error(response.msg || '操作失败')
        }
      } catch (error) {
        console.error('完成咨询失败:', error)
        this.$message.error('操作失败')
      } finally {
        this.completing = false
      }
    },
    
    gotoSchedule() {
      // 跳转到排班管理
      this.$router.push('/mental-health/my-schedule')
    },
    
    gotoAppointments() {
      // 跳转到预约管理
      this.$router.push('/mental-health/appointment-manage')
    },
    
    gotoProfileManagement() {
      // 跳转到档案管理
      this.$router.push('/profile-management/list')
    },
    
    gotoSettings() {
      // 跳转到个人设置
      this.$router.push('/user/profile')
    }
  }
}
</script>

<style scoped lang="scss">
.psychologist-dashboard {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);
}

// 欢迎卡片
.welcome-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  
  ::v-deep .el-card__body {
    padding: 30px;
  }
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  @media (max-width: 768px) {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }
}

.welcome-text {
  h2 {
    margin: 0 0 10px 0;
    font-size: 28px;
    font-weight: bold;
  }
  
  .welcome-subtitle {
    margin: 0;
    opacity: 0.9;
    font-size: 16px;
  }
}

.welcome-stats {
  display: flex;
  gap: 30px;
  
  @media (max-width: 576px) {
    flex-direction: column;
    gap: 15px;
  }
}

.stat-item {
  text-align: center;
  
  .stat-label {
    display: block;
    font-size: 14px;
    opacity: 0.8;
    margin-bottom: 5px;
  }
  
  .stat-value {
    display: block;
    font-size: 32px;
    font-weight: bold;
  }
}

// 快捷功能卡片
.quick-cards {
  margin-bottom: 20px;
  
  .quick-card {
    height: 120px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15) !important;
    }
    
    ::v-deep .el-card__body {
      padding: 0;
      height: 100%;
    }
  }
}

.card-content {
  display: flex;
  align-items: center;
  padding: 25px 20px;
  height: 100%;
  
  .card-icon {
    width: 60px;
    height: 60px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20px;
    
    i {
      font-size: 28px;
      color: white;
    }
  }
  
  .card-text {
    flex: 1;
    
    h4 {
      margin: 0 0 5px 0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
    
    p {
      margin: 0;
      font-size: 13px;
      color: #909399;
    }
  }
}

// 工作区
.work-section {
  margin-bottom: 20px;
}

.today-appointments,
.statistics-overview {
  height: 100%;
  
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
      
      i {
        font-size: 18px;
        color: #409EFF;
        margin-right: 10px;
      }
      
      span {
        font-weight: 600;
        font-size: 16px;
        color: #303133;
      }
    }
  }
}

// 表格样式
::v-deep .el-table {
  .user-info {
    display: flex;
    align-items: center;
  }
  
  .problem-description {
    max-width: 300px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  
  .action-buttons {
    display: flex;
    gap: 5px;
    
    .el-button {
      padding: 5px;
      min-height: auto;
    }
  }
}

// 空状态
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
    margin: 0;
    font-size: 14px;
  }
}

// 统计概览
.statistics-grid {
  margin-bottom: 30px;
}

.stat-item-large {
  display: flex;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 15px;
  transition: all 0.3s;
  
  &:hover {
    background: #f0f2f5;
    transform: translateX(5px);
  }
}

.stat-icon-large {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  
  i {
    font-size: 22px;
    color: white;
  }
}

.stat-content-large {
  flex: 1;
  
  .stat-number-large {
    font-size: 24px;
    font-weight: bold;
    color: #303133;
    margin-bottom: 5px;
  }
  
  .stat-title-large {
    font-size: 14px;
    color: #606266;
    margin-bottom: 5px;
  }
  
  .stat-subtitle {
    font-size: 12px;
    color: #909399;
  }
  
  .stat-change {
    font-size: 12px;
    color: #67C23A;
    
    i {
      margin-right: 3px;
    }
    
    &.negative {
      color: #F56C6C;
    }
  }
  
  .rating-stars {
    i {
      color: #E6A23C;
      font-size: 14px;
      margin-right: 2px;
    }
  }
}

.upcoming-tips {
  padding: 20px;
  background: #fdfcfb;
  border-radius: 8px;
  border-left: 4px solid #409EFF;
  
  .tips-header {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    
    i {
      color: #409EFF;
      font-size: 18px;
      margin-right: 10px;
    }
    
    span {
      font-weight: 600;
      color: #303133;
      font-size: 16px;
    }
  }
  
  .tips-list {
    margin: 0;
    padding-left: 20px;
    
    li {
      margin-bottom: 8px;
      color: #606266;
      font-size: 14px;
      line-height: 1.5;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

// 响应式调整
@media (max-width: 1200px) {
  .work-section {
    .el-col-lg-8, .el-col-lg-16 {
      margin-bottom: 20px;
    }
  }
}

@media (max-width: 768px) {
  .psychologist-dashboard {
    padding: 15px;
  }
  
  .welcome-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }
  
  .welcome-stats {
    width: 100%;
    justify-content: space-between;
  }
  
  .card-content {
    padding: 20px 15px;
    
    .card-icon {
      width: 50px;
      height: 50px;
      margin-right: 15px;
      
      i {
        font-size: 24px;
      }
    }
  }
}

@media (max-width: 576px) {
  .welcome-stats {
    flex-direction: column;
    gap: 15px;
  }
  
  .quick-cards {
    .el-col-xs-12 {
      margin-bottom: 15px;
    }
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start !important;
    gap: 10px;
    
    .header-left {
      width: 100%;
      flex-direction: column;
      align-items: flex-start !important;
      gap: 10px;
    }
  }
}
</style>