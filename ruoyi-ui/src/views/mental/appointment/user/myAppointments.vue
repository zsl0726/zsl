<template>
  <div class="app-my-appointments">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2><i class="el-icon-tickets"></i> 我的预约记录</h2>
        <p class="subtitle">查看和管理您的心理咨询预约</p>
      </div>
      <div class="header-right">
        <div class="filter-container">
          <span class="filter-label">状态筛选：</span>
          <el-select 
            v-model="statusFilter" 
            placeholder="全部状态" 
            @change="handleStatusChange" 
            size="medium"
            style="width: 150px;"
            clearable
          >
            <el-option label="全部" value=""></el-option>
            <el-option label="待处理" value="pending"></el-option>
            <el-option label="已接受" value="accepted"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="已取消" value="cancelled"></el-option>
            <el-option label="已拒绝" value="rejected"></el-option>
          </el-select>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <el-card class="appointment-card" shadow="hover">
        <!-- 表格区域 -->
        <div class="table-wrapper">
          <el-table 
            v-loading="loading" 
            :data="appointmentList" 
            @sort-change="sortChange" 
            style="width: 100%"
            size="medium"
          >
            <el-table-column label="预约ID" prop="id" width="80" align="center" />
            
            <el-table-column label="咨询师" prop="psychologistName" width="120" align="center">
              <template slot-scope="scope">
                <div class="psychologist-cell">
                  <el-avatar :size="32" :src="scope.row.psychologistAvatar" class="avatar">
                    {{ scope.row.psychologistName ? scope.row.psychologistName.charAt(0) : '心' }}
                  </el-avatar>
                  <span class="psychologist-name">{{ scope.row.psychologistName }}</span>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column label="预约日期" prop="appointmentDate" width="120" align="center" sortable="custom">
              <template slot-scope="scope">
                <div class="date-cell">
                  <span class="date">{{ parseTime(scope.row.appointmentDate, '{y}-{m}-{d}') }}</span>
                  <span class="weekday">{{ getWeekday(scope.row.appointmentDate) }}</span>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column label="时间段" prop="timeSlot" width="120" align="center">
              <template slot-scope="scope">
                <span class="time-slot">{{ scope.row.timeSlot }}</span>
              </template>
            </el-table-column>
            
            <el-table-column label="问题描述" prop="problemDescription" min-width="180" :show-overflow-tooltip="true">
              <template slot-scope="scope">
                <div class="problem-description">
                  {{ scope.row.problemDescription || '无描述' }}
                </div>
              </template>
            </el-table-column>
            
            <el-table-column label="紧急程度" prop="urgencyLevel" width="100" align="center">
              <template slot-scope="scope">
                <el-tag :type="getUrgencyType(scope.row.urgencyLevel)" size="small" effect="plain">
                  {{ scope.row.urgencyLevel }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column label="状态" prop="status" width="110" align="center">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.status)" size="small">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column label="评价状态" width="120" align="center">
              <template slot-scope="scope">
                <div v-if="scope.row.status === 'completed'">
                  <el-tag v-if="scope.row.ratingScore && scope.row.ratingScore > 0" type="success" size="small" class="rated-tag" @click="handleViewRating(scope.row)">
                    已评价 ({{ scope.row.ratingScore }}分)
                  </el-tag>
                  <el-tag v-else type="info" size="small" class="rate-tag" @click="handleRate(scope.row)">
                    去评价
                  </el-tag>
                </div>
                <span v-else class="no-rating">--</span>
              </template>
            </el-table-column>
            
            <el-table-column label="创建时间" prop="createTime" width="150" align="center" sortable="custom">
              <template slot-scope="scope">
                <span class="create-time">{{ parseTime(scope.row.createTime) }}</span>
              </template>
            </el-table-column>
            
            <el-table-column label="操作" align="center" width="220">
              <template slot-scope="scope">
                <div class="action-buttons">
                  <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" class="view-btn" title="查看详情"></el-button>
                  
                  <el-button v-if="scope.row.status === 'accepted' || scope.row.status === 'completed'" size="mini" type="text" icon="el-icon-document" @click="handleViewNotes(scope.row)" class="notes-btn" title="查看笔记"></el-button>
                  
                  <el-button v-if="scope.row.status === 'pending' && isUpcomingAppointment(scope.row)" size="mini" type="text" icon="el-icon-close" @click="handleCancel(scope.row)" class="cancel-btn" title="取消预约"></el-button>
                  
                  <el-button v-if="scope.row.status === 'completed' && (!scope.row.ratingScore || scope.row.ratingScore === 0)" size="mini" type="text" icon="el-icon-star" @click="handleRate(scope.row)" class="rate-btn" title="评价"></el-button>
                  
                  <el-button v-if="scope.row.status === 'completed' && scope.row.ratingScore && scope.row.ratingScore > 0" size="mini" type="text" icon="el-icon-star-on" @click="handleViewRating(scope.row)" class="view-rating-btn" title="查看评价"></el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 分页 -->
        <div v-if="total > 0" class="pagination-wrapper">
          <el-pagination
            :total="total"
            :current-page.sync="queryParams.pageNum"
            :page-size.sync="queryParams.pageSize"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            background
            class="pagination"
          />
        </div>
        
        <!-- 空状态 -->
        <div v-if="!loading && appointmentList.length === 0" class="empty-state">
          <div class="empty-content">
            <i class="el-icon-date empty-icon"></i>
            <div class="empty-text">
              <h3>暂无预约记录</h3>
              <p>您还没有预约心理咨询，快去预约吧</p>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 取消预约对话框 -->
    <el-dialog title="取消预约" :visible.sync="cancelVisible" width="500px" append-to-body>
      <div class="dialog-content">
        <div class="warning-icon">
          <i class="el-icon-warning"></i>
        </div>
        <div class="warning-text">
          <h3>确定要取消该预约吗？</h3>
          <p>取消后将无法恢复，请谨慎操作</p>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="cancelVisible = false">取 消</el-button>
        <el-button type="danger" @click="submitCancel" :loading="cancelLoading" class="confirm-btn">
          确 定
        </el-button>
      </span>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog :title="detailTitle" :visible.sync="detailVisible" width="650px" append-to-body>
      <div class="detail-content">
        <!-- 基本信息 -->
        <div class="detail-section">
          <h4><i class="el-icon-info"></i> 基本信息</h4>
          <el-row :gutter="20" class="basic-info">
            <el-col :span="12">
              <div class="info-item">
                <label class="info-label">预约ID：</label>
                <span class="info-value">{{ detailForm.id }}</span>
              </div>
              <div class="info-item">
                <label class="info-label">咨询师：</label>
                <div class="psychologist-info">
                  <el-avatar :size="36" :src="detailForm.psychologistAvatar" class="detail-avatar">
                    {{ detailForm.psychologistName ? detailForm.psychologistName.charAt(0) : '心' }}
                  </el-avatar>
                  <span class="psychologist-name">{{ detailForm.psychologistName }}</span>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label class="info-label">预约日期：</label>
                <span class="info-value">{{ parseTime(detailForm.appointmentDate, '{y}-{m}-{d}') }}</span>
              </div>
              <div class="info-item">
                <label class="info-label">时间段：</label>
                <span class="info-value">{{ detailForm.timeSlot }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
        
        <!-- 状态信息 -->
        <div class="detail-section">
          <h4><i class="el-icon-s-data"></i> 状态信息</h4>
          <div class="status-section">
            <div class="status-item">
              <label class="status-label">状态：</label>
              <el-tag :type="getStatusType(detailForm.status)" class="status-tag">
                {{ getStatusText(detailForm.status) }}
              </el-tag>
            </div>
            <div class="status-item">
              <label class="status-label">紧急程度：</label>
              <el-tag :type="getUrgencyType(detailForm.urgencyLevel)" class="urgency-tag">
                {{ detailForm.urgencyLevel }}
              </el-tag>
            </div>
            <div class="status-item">
              <label class="status-label">地点：</label>
              <span class="info-value">{{ detailForm.locationName || '心理咨询室' }}</span>
            </div>
          </div>
        </div>
        
        <!-- 问题描述 -->
        <div class="detail-section">
          <h4><i class="el-icon-chat-dot-round"></i> 问题描述</h4>
          <div class="problem-content">
            <div class="content-box">
              {{ detailForm.problemDescription || '无' }}
            </div>
          </div>
        </div>
        
        <!-- 咨询笔记 -->
        <div v-if="detailForm.counselingNotes" class="detail-section">
          <h4><i class="el-icon-document"></i> 咨询笔记</h4>
          <div class="notes-content">
            <div class="content-box notes-box">
              {{ detailForm.counselingNotes }}
            </div>
          </div>
        </div>
        
        <!-- 用户评价 -->
        <div v-if="detailForm.status === 'completed'" class="detail-section">
          <h4><i class="el-icon-star"></i> 用户评价</h4>
          <div class="rating-section">
            <div v-if="detailForm.ratingScore && detailForm.ratingScore > 0" class="rating-content">
              <div class="rating-score">
                <el-rate :value="detailForm.ratingScore" disabled :colors="['#99A9BF', '#F7BA2A', '#FF9900']" size="medium" />
                <span class="score-text">{{ detailForm.ratingScore }}分</span>
              </div>
              <div v-if="detailForm.ratingComment" class="rating-comment">
                <strong class="comment-label">评价内容：</strong>
                <div class="comment-box">
                  {{ detailForm.ratingComment }}
                </div>
              </div>
            </div>
            <div v-else class="no-rating">
              <el-button type="text" @click="handleRate(detailForm)" class="rate-now-btn">
                立即评价
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 时间信息 -->
        <div class="detail-section">
          <h4><i class="el-icon-time"></i> 时间信息</h4>
          <el-row :gutter="20" class="time-info">
            <el-col :span="12">
              <div class="time-item">
                <label class="time-label">创建时间：</label>
                <span class="time-value">{{ parseTime(detailForm.createTime) }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="time-item">
                <label class="time-label">更新时间：</label>
                <span class="time-value">{{ parseTime(detailForm.updateTime) }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false" class="close-btn">关 闭</el-button>
      </span>
    </el-dialog>

    <!-- 评价对话框 -->
    <el-dialog title="咨询评价" :visible.sync="rateVisible" width="550px" append-to-body @close="handleRateClose">
      <div class="rate-content">
        <div class="rate-header">
          <div class="appointment-info">
            <div class="psychologist-info">
              <el-avatar :size="48" :src="ratingAppointment && ratingAppointment.psychologistAvatar" class="rate-avatar">
                {{ ratingAppointment && ratingAppointment.psychologistName ? ratingAppointment.psychologistName.charAt(0) : '心' }}
              </el-avatar>
              <div class="info-text">
                <div class="psychologist-name">{{ ratingAppointment && ratingAppointment.psychologistName }}</div>
                <div class="appointment-time">
                  {{ ratingAppointment && parseTime(ratingAppointment.appointmentDate, '{y}-{m}-{d}') }} 
                  {{ ratingAppointment && ratingAppointment.timeSlot }}
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="rate-body">
          <div class="rate-title">请对本次咨询进行评价</div>
          
          <div class="rate-stars">
            <el-rate v-model="ratingForm.ratingScore" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" show-text text-color="#ff9900" size="large" />
            <div v-if="ratingForm.ratingScore" class="rate-text">
              {{ getRatingText(ratingForm.ratingScore) }}
            </div>
          </div>
          
          <div class="rate-comment">
            <el-input v-model="ratingForm.ratingComment" type="textarea" :rows="4" placeholder="请写下您的咨询感受和建议（可选）" maxlength="200" show-word-limit resize="none"></el-input>
          </div>
        </div>
      </div>
      
      <span slot="footer" class="dialog-footer">
        <el-button @click="rateVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitRating" :disabled="!ratingForm.ratingScore" :loading="ratingLoading" class="submit-rate-btn">
          提 交
        </el-button>
      </span>
    </el-dialog>

    <!-- 查看评价详情对话框 -->
    <el-dialog title="我的评价" :visible.sync="viewRatingVisible" width="550px" append-to-body>
      <div v-if="viewRatingDetail" class="view-rating-content">
        <div class="rating-header">
          <div class="psychologist-info">
            <el-avatar :size="60" :src="currentAppointment && currentAppointment.psychologistAvatar" class="view-rating-avatar">
              {{ currentAppointment && currentAppointment.psychologistName ? currentAppointment.psychologistName.charAt(0) : '心' }}
            </el-avatar>
            <div class="info-text">
              <div class="psychologist-name">{{ currentAppointment && currentAppointment.psychologistName }}</div>
              <div class="appointment-time">
                {{ currentAppointment && parseTime(currentAppointment.appointmentDate, '{y}-{m}-{d}') }} 
                {{ currentAppointment && currentAppointment.timeSlot }}
              </div>
            </div>
          </div>
        </div>
        
        <div class="rating-display">
          <div class="rating-score-display">
            <el-rate :value="viewRatingDetail.ratingScore" disabled :colors="['#99A9BF', '#F7BA2A', '#FF9900']" size="large" />
            <div class="score-number">{{ viewRatingDetail.ratingScore }}分</div>
          </div>
          
          <div v-if="viewRatingDetail.ratingComment" class="rating-comment-display">
            <div class="comment-label">评价内容：</div>
            <div class="comment-content">
              {{ viewRatingDetail.ratingComment }}
            </div>
          </div>
          <div v-else class="no-comment">
            您没有留下评价内容
          </div>
          
          <div class="rating-time">
            评价时间：{{ parseTime(viewRatingDetail.createTime) }}
          </div>
        </div>
      </div>
      
      <span slot="footer" class="dialog-footer">
        <el-button @click="viewRatingVisible = false" class="close-btn">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getMyAppointments, cancelAppointment, submitRating, getRatingByAppointmentId } from "@/api/mental/appointment";
import { parseTime } from "@/utils/ruoyi";

export default {
  name: "MyAppointments",
  data() {
    return {
      loading: true,
      total: 0,
      appointmentList: [],
      statusFilter: '',
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderByColumn: 'createTime',
        isAsc: 'desc'
      },
      cancelVisible: false,
      cancelLoading: false,
      cancelId: null,
      detailVisible: false,
      detailTitle: "预约详情",
      detailForm: {},
      rateVisible: false,
      ratingLoading: false,
      ratingAppointment: null,
      currentAppointment: null,
      ratingForm: {
        appointmentId: null,
        ratingScore: 0,
        ratingComment: ''
      },
      viewRatingVisible: false,
      viewRatingDetail: null,
      ratingTexts: {
        1: '很差',
        2: '较差',
        3: '一般',
        4: '满意',
        5: '非常满意'
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getWeekday(dateStr) {
      if (!dateStr) return '';
      try {
        const date = new Date(dateStr);
        const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
        return weekdays[date.getDay()];
      } catch (e) {
        return '';
      }
    },
    
    getStatusText(status) {
      const map = {
        'pending': '待处理',
        'accepted': '已接受',
        'completed': '已完成',
        'cancelled': '已取消',
        'rejected': '已拒绝'
      };
      return map[status] || status;
    },
    
    getStatusType(status) {
      const map = {
        'pending': 'warning',
        'accepted': 'success',
        'completed': 'info',
        'cancelled': 'danger',
        'rejected': 'danger'
      };
      return map[status] || 'info';
    },
    
    getUrgencyType(urgencyLevel) {
      const map = {
        '普通': 'info',
        '紧急': 'warning',
        '非常紧急': 'danger'
      };
      return map[urgencyLevel] || 'info';
    },
    
    getRatingText(score) {
      return this.ratingTexts[score] || '';
    },
    
    isUpcomingAppointment(row) {
      if (!row.appointmentDate) return false;
      const appointmentDate = new Date(row.appointmentDate);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return appointmentDate > today;
    },
    
    handleSizeChange(val) {
      this.queryParams.pageSize = val;
      this.getList();
    },
    
    handleCurrentChange(val) {
      this.queryParams.pageNum = val;
      this.getList();
    },
    
    handleStatusChange() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    
    async getList() {
      this.loading = true;
      const params = {
        ...this.queryParams,
        status: this.statusFilter
      };
      
      try {
        const response = await getMyAppointments(params);
        if (response.code === 200) {
          this.appointmentList = response.rows || [];
          this.total = response.total || 0;
        } else {
          this.$message.error(response.msg || '加载预约列表失败');
          this.appointmentList = [];
          this.total = 0;
        }
      } catch (error) {
        console.error('获取我的预约失败:', error);
        this.$message.error('获取预约列表失败');
        this.appointmentList = [];
        this.total = 0;
      } finally {
        this.loading = false;
      }
    },
    
    sortChange(column) {
      if (column.order === 'ascending') {
        this.queryParams.orderByColumn = column.prop;
        this.queryParams.isAsc = 'asc';
      } else if (column.order === 'descending') {
        this.queryParams.orderByColumn = column.prop;
        this.queryParams.isAsc = 'desc';
      } else {
        this.queryParams.orderByColumn = '';
        this.queryParams.isAsc = '';
      }
      this.getList();
    },
    
    handleCancel(row) {
      if (!this.isUpcomingAppointment(row)) {
        this.$message.warning('已过期的预约不能取消');
        return;
      }
      
      this.cancelId = row.id;
      this.currentAppointment = row;
      this.cancelVisible = true;
    },
    
    async submitCancel() {
      this.cancelLoading = true;
      try {
        const response = await cancelAppointment(this.cancelId);
        if (response.code === 200) {
          this.$modal.msgSuccess("取消成功");
          this.cancelVisible = false;
          this.getList();
        } else {
          this.$modal.msgError(response.msg || "取消失败");
        }
      } catch (error) {
        console.error('取消预约失败:', error);
        this.$modal.msgError("取消预约失败");
      } finally {
        this.cancelLoading = false;
      }
    },
    
    handleRate(row) {
      if (row.status !== 'completed') {
        this.$message.warning('只有已完成的预约才能评价');
        return;
      }
      
      if (row.ratingScore && row.ratingScore > 0) {
        this.$message.info('该预约已评价过');
        this.handleViewRating(row);
        return;
      }
      
      this.ratingAppointment = row;
      this.currentAppointment = row;
      this.ratingForm = {
        appointmentId: row.id,
        ratingScore: 0,
        ratingComment: ''
      };
      this.rateVisible = true;
    },
    
    async submitRating() {
      if (!this.ratingForm.ratingScore) {
        this.$message.warning('请先选择评分');
        return;
      }
      
      this.ratingLoading = true;
      try {
        const response = await submitRating(this.ratingForm);
        if (response.code === 200) {
          this.$modal.msgSuccess("评价成功");
          this.rateVisible = false;
          this.getList();
          
          if (this.detailVisible && this.detailForm.id === this.ratingForm.appointmentId) {
            this.detailForm.ratingScore = this.ratingForm.ratingScore;
            this.detailForm.ratingComment = this.ratingForm.ratingComment;
          }
        } else {
          this.$modal.msgError(response.msg || "评价失败");
        }
      } catch (error) {
        console.error('评价失败:', error);
        this.$modal.msgError("评价失败");
      } finally {
        this.ratingLoading = false;
      }
    },
    
    async handleViewRating(row) {
      this.currentAppointment = row;
      try {
        const response = await getRatingByAppointmentId(row.id);
        if (response.code === 200 && response.data) {
          this.viewRatingDetail = response.data;
          this.viewRatingVisible = true;
        } else {
          if (row.ratingScore && row.ratingScore > 0) {
            this.viewRatingDetail = {
              ratingScore: row.ratingScore,
              ratingComment: row.ratingComment || '',
              createTime: row.updateTime || row.createTime
            };
            this.viewRatingVisible = true;
          } else {
            this.$message.warning('该预约暂无评价');
          }
        }
      } catch (error) {
        console.error('查看评价详情失败:', error);
        if (row.ratingScore && row.ratingScore > 0) {
          this.viewRatingDetail = {
            ratingScore: row.ratingScore,
            ratingComment: row.ratingComment || '',
            createTime: row.updateTime || row.createTime
          };
          this.viewRatingVisible = true;
        } else {
          this.$message.error('查看评价详情失败');
        }
      }
    },
    
    handleRateClose() {
      this.ratingForm = {
        appointmentId: null,
        ratingScore: 0,
        ratingComment: ''
      };
    },
    
    handleViewNotes(row) {
      this.detailForm = Object.assign({}, row);
      this.detailTitle = "咨询笔记";
      this.detailVisible = true;
    },
    
    handleView(row) {
      this.detailForm = Object.assign({}, row);
      this.detailTitle = "预约详情";
      this.detailVisible = true;
    },
    
    parseTime
  }
};
</script>

<style scoped>
.app-my-appointments {
  padding: 20px;
  background-color: #f5f7fa;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-left h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
}

.header-left h2 i {
  margin-right: 10px;
  color: #409eff;
}

.header-left .subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-right .filter-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-label {
  color: #606266;
  font-size: 14px;
  white-space: nowrap;
}

.main-content .appointment-card {
  border: none;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.table-wrapper {
  margin-bottom: 20px;
}

.psychologist-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: bold;
}

.psychologist-name {
  font-weight: 500;
}

.date-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.date {
  font-weight: 500;
  color: #303133;
}

.weekday {
  font-size: 12px;
  color: #909399;
  background-color: #f2f3f5;
  padding: 2px 8px;
  border-radius: 10px;
}

.time-slot {
  color: #409eff;
  font-weight: 500;
  background-color: rgba(64, 158, 255, 0.08);
  padding: 4px 10px;
  border-radius: 12px;
  display: inline-block;
}

.problem-description {
  color: #606266;
  line-height: 1.5;
  max-height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.rated-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.rated-tag:hover {
  background-color: rgba(103, 194, 58, 0.15);
  transform: scale(1.05);
}

.rate-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.rate-tag:hover {
  background-color: rgba(144, 147, 153, 0.15);
  transform: scale(1.05);
}

.no-rating {
  color: #c0c4cc;
  font-style: italic;
  font-size: 13px;
}

.create-time {
  color: #909399;
  font-size: 13px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.action-buttons .el-button {
  width: 32px;
  height: 32px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: all 0.2s;
}

.action-buttons .el-button:hover {
  transform: scale(1.1);
}

.view-btn {
  color: #409eff;
  background-color: rgba(64, 158, 255, 0.08);
}

.view-btn:hover {
  background-color: rgba(64, 158, 255, 0.15);
}

.notes-btn {
  color: #67c23a;
  background-color: rgba(103, 194, 58, 0.08);
}

.notes-btn:hover {
  background-color: rgba(103, 194, 58, 0.15);
}

.cancel-btn {
  color: #f56c6c;
  background-color: rgba(245, 108, 108, 0.08);
}

.cancel-btn:hover {
  background-color: rgba(245, 108, 108, 0.15);
}

.rate-btn {
  color: #e6a23c;
  background-color: rgba(230, 162, 60, 0.08);
}

.rate-btn:hover {
  background-color: rgba(230, 162, 60, 0.15);
}

.view-rating-btn {
  color: #ff9900;
  background-color: rgba(255, 153, 0, 0.08);
}

.view-rating-btn:hover {
  background-color: rgba(255, 153, 0, 0.15);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.empty-content .empty-icon {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.empty-text h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #606266;
}

.empty-text p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

/* 对话框样式 */
.dialog-content {
  text-align: center;
}

.warning-icon {
  font-size: 56px;
  color: #f56c6c;
  margin-bottom: 20px;
}

.warning-text h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #303133;
}

.warning-text p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.confirm-btn {
  background-color: #f56c6c;
  border-color: #f56c6c;
  border-radius: 8px;
  padding: 10px 24px;
}

.confirm-btn:hover {
  background-color: #f78989;
  border-color: #f78989;
}

.detail-content .detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
}

.detail-section h4 i {
  margin-right: 8px;
  color: #409eff;
}

.basic-info .info-item {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.info-label {
  min-width: 70px;
  color: #606266;
  font-size: 14px;
}

.info-value {
  color: #303133;
  font-weight: 500;
}

.psychologist-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: bold;
}

.status-section {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.status-item {
  display: flex;
  align-items: center;
}

.status-label {
  min-width: 70px;
  color: #606266;
  font-size: 14px;
}

.content-box {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 8px;
  line-height: 1.6;
  color: #606266;
  min-height: 60px;
}

.notes-box {
  background-color: #f0f9eb;
  border-left: 4px solid #67c23a;
}

.rating-content .rating-score {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.score-text {
  color: #ff9900;
  font-size: 18px;
  font-weight: bold;
}

.rating-comment .comment-label {
  color: #303133;
  margin-bottom: 8px;
  display: block;
}

.comment-box {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 8px;
  line-height: 1.6;
  color: #606266;
}

.no-rating .rate-now-btn {
  color: #e6a23c;
  font-weight: 500;
}

.time-info .time-item {
  display: flex;
  align-items: center;
}

.time-label {
  min-width: 70px;
  color: #606266;
  font-size: 14px;
}

.time-value {
  color: #303133;
}

.close-btn {
  border-radius: 8px;
  padding: 10px 24px;
}

.rate-header .appointment-info {
  display: flex;
  align-items: center;
  justify-content: center;
}

.psychologist-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.rate-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: bold;
}

.info-text .psychologist-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.appointment-time {
  color: #606266;
  font-size: 14px;
}

.rate-body .rate-title {
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 20px;
}

.rate-stars {
  text-align: center;
  margin-bottom: 20px;
}

.rate-text {
  color: #ff9900;
  font-size: 16px;
  font-weight: 500;
}

.submit-rate-btn {
  border-radius: 8px;
  padding: 10px 24px;
  background-color: #409eff;
  border-color: #409eff;
}

.submit-rate-btn:disabled {
  background-color: #a0cfff;
  border-color: #a0cfff;
}

.view-rating-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: bold;
  border: 3px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.rating-display .rating-score-display {
  text-align: center;
  margin-bottom: 20px;
}

.score-number {
  color: #ff9900;
  font-size: 24px;
  font-weight: bold;
  margin-top: 8px;
}

.rating-comment-display {
  background-color: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.comment-label {
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  display: block;
}

.no-comment {
  text-align: center;
  color: #909399;
  font-style: italic;
  margin-bottom: 16px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.rating-time {
  text-align: center;
  color: #909399;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-my-appointments {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
    padding: 16px;
  }
  
  .table-wrapper {
    overflow-x: auto;
  }
  
  .action-buttons {
    flex-wrap: wrap;
    justify-content: flex-start;
  }
}
</style>