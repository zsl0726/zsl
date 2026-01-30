<template>
  <div class="app-psychologist-appointment">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2><i class="el-icon-s-order"></i> 咨询师工作台</h2>
        <p class="subtitle">管理您的心理咨询预约和评价</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="16">
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card total">
            <div class="stat-content">
              <div class="stat-icon">
                <i class="el-icon-s-order"></i>
              </div>
              <div class="stat-info">
                <div class="stat-title">总预约</div>
                <div class="stat-value">{{ statistics.total || 0 }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card pending">
            <div class="stat-content">
              <div class="stat-icon">
                <i class="el-icon-time"></i>
              </div>
              <div class="stat-info">
                <div class="stat-title">待处理</div>
                <div class="stat-value">{{ statistics.pending_count || 0 }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card accepted">
            <div class="stat-content">
              <div class="stat-icon">
                <i class="el-icon-circle-check"></i>
              </div>
              <div class="stat-info">
                <div class="stat-title">已接受</div>
                <div class="stat-value">{{ statistics.accepted_count || 0 }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="stat-card rating">
            <div class="stat-content">
              <div class="stat-icon">
                <i class="el-icon-star-on"></i>
              </div>
              <div class="stat-info">
                <div class="stat-title">综合评分</div>
                <div class="stat-value">
                  {{ ratingInfo.averageRating.toFixed(1) }}
                </div>
                <div class="stat-subtitle">
                  共 {{ ratingInfo.totalRatings || 0 }} 条评价
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 今日预约 -->
    <el-card class="today-card" shadow="hover">
      <div slot="header" class="card-header">
        <div class="header-left">
          <h3><i class="el-icon-sunny"></i> 今日预约</h3>
          <span class="card-subtitle">{{ todayAppointments.length }}个预约</span>
        </div>
        <div class="header-right">
          <div class="date-picker-container">
            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="选择日期"
              format="yyyy-MM-dd"
              value-format="yyyy-MM-dd"
              @change="loadTodayAppointments"
              size="small"
              style="width: 130px;"
            />
            <el-button 
              type="text" 
              icon="el-icon-refresh" 
              @click="refreshTodayAppointments"
              :loading="todayLoading"
              size="small"
              title="刷新"
            ></el-button>
          </div>
        </div>
      </div>
      <div class="table-container">
        <el-table 
          v-loading="todayLoading" 
          :data="todayAppointments" 
          :height="220"
          size="small"
          style="width: 100%"
          class="today-table"
        >
          <el-table-column label="用户" prop="userName" width="100" align="center">
            <template slot-scope="scope">
              <div class="user-cell">
                <el-avatar 
                  :size="32" 
                  :src="scope.row.userAvatar" 
                  v-if="scope.row.userAvatar"
                  class="user-avatar-sm"
                >
                  {{ scope.row.userName ? scope.row.userName.charAt(0) : '用' }}
                </el-avatar>
                <div class="user-info-sm">
                  <div class="user-name">{{ scope.row.userName }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="预约日期" prop="appointmentDate" width="120" align="center">
            <template slot-scope="scope">
              <div class="date-cell-sm">
                <div class="date-main">{{ parseTime(scope.row.appointmentDate, '{m}-{d}') }}</div>
                <div class="weekday-sm">{{ getWeekday(scope.row.appointmentDate) }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="时间段" prop="timeSlot" width="110" align="center">
            <template slot-scope="scope">
              <div class="time-slot-sm">
                {{ formatTimeSlot(scope.row.timeSlot) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="问题描述" prop="problemDescription" min-width="150">
            <template slot-scope="scope">
              <div class="problem-description-sm" :title="scope.row.problemDescription">
                {{ scope.row.problemDescription || '无描述' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="紧急程度" prop="urgencyLevel" width="100" align="center">
            <template slot-scope="scope">
              <div class="urgency-cell">
                <el-tag 
                  :type="getUrgencyType(scope.row.urgencyLevel)" 
                  size="mini"
                  effect="plain"
                  class="urgency-tag"
                >
                  {{ getUrgencyText(scope.row.urgencyLevel) }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="评分" width="130" align="center">
            <template slot-scope="scope">
              <div v-if="scope.row.ratingScore" class="rating-cell-sm">
                <div class="rating-stars-container">
                  <el-rate
                    :value="scope.row.ratingScore"
                    disabled
                    :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                    size="mini"
                    class="rating-stars-small"
                  />
                </div>
                <div class="rating-score">{{ scope.row.ratingScore }}分</div>
              </div>
              <span v-else class="no-rating">--</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="90" align="center">
            <template slot-scope="scope">
              <el-tag 
                :type="getStatusType(scope.row.status)" 
                size="mini"
                class="status-tag-sm"
              >
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="180" fixed="right">
            <template slot-scope="scope">
              <div class="action-buttons-sm">
                <el-button
                  v-if="scope.row.status === 'pending'"
                  size="mini"
                  type="success"
                  plain
                  @click="handleAccept(scope.row)"
                  class="accept-btn"
                >接受</el-button>
                <el-button
                  v-if="scope.row.status === 'pending'"
                  size="mini"
                  type="danger"
                  plain
                  @click="handleReject(scope.row)"
                  class="reject-btn"
                >拒绝</el-button>
                <el-button
                  v-if="scope.row.status === 'accepted'"
                  size="mini"
                  type="primary"
                  plain
                  @click="handleComplete(scope.row)"
                  class="complete-btn"
                >完成</el-button>
                <el-tag 
                  v-if="scope.row.status === 'completed'" 
                  size="mini" 
                  type="info"
                  class="completed-tag"
                >已完成</el-tag>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-if="todayAppointments.length === 0 && !todayLoading" class="empty-today">
        <i class="el-icon-date empty-icon"></i>
        <div class="empty-text">今日暂无预约</div>
      </div>
    </el-card>

    <!-- 预约管理 -->
    <el-card class="appointment-card" shadow="hover" style="margin-top: 16px;">
      <div slot="header" class="card-header">
        <div class="header-left">
          <h3><i class="el-icon-s-management"></i> 预约管理</h3>
          <div class="total-info">
            <span class="total-count">共 {{ total }} 条记录</span>
          </div>
        </div>
        <div class="header-right">
          <div class="filter-container">
            <span class="filter-label">状态：</span>
            <el-select 
              v-model="statusFilter" 
              placeholder="全部状态" 
              @change="handleStatusChange" 
              size="small"
              style="width: 120px; margin-right: 12px;"
              clearable
            >
              <el-option label="全部" value=""></el-option>
              <el-option label="待处理" value="pending"></el-option>
              <el-option label="已接受" value="accepted"></el-option>
              <el-option label="已完成" value="completed"></el-option>
              <el-option label="已取消" value="cancelled"></el-option>
              <el-option label="已拒绝" value="rejected"></el-option>
            </el-select>
            <el-button 
              type="text" 
              icon="el-icon-refresh" 
              @click="getList"
              :loading="loading"
              size="small"
              title="刷新"
              style="margin-left: 4px;"
            ></el-button>
          </div>
        </div>
      </div>
      
      <div class="table-container">
        <el-table 
          v-loading="loading" 
          :data="appointmentList" 
          style="width: 100%"
          size="medium"
          class="appointment-table"
          :row-class-name="tableRowClassName"
        >
          <el-table-column label="预约ID" prop="id" width="80" align="center" fixed="left">
            <template slot-scope="scope">
              <div class="appointment-id-cell">
                <span class="id-text">#{{ scope.row.id }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="用户信息" width="160" fixed="left">
            <template slot-scope="scope">
              <div class="user-info-card">
                <div class="user-avatar-wrapper">
                  <el-avatar 
                    :size="40" 
                    :src="scope.row.userAvatar" 
                    v-if="scope.row.userAvatar"
                    class="user-avatar"
                  >
                    {{ scope.row.userName ? scope.row.userName.charAt(0) : '用' }}
                  </el-avatar>
                  <el-avatar 
                    :size="40" 
                    v-else
                    class="user-avatar-default"
                  >
                    {{ scope.row.userName ? scope.row.userName.charAt(0) : '用' }}
                  </el-avatar>
                </div>
                <div class="user-details">
                  <div class="user-name" :title="scope.row.userName">{{ scope.row.userName }}</div>
                  <div class="user-meta">
                    <div v-if="scope.row.userId" class="user-id">ID: {{ scope.row.userId }}</div>
                    <div v-if="scope.row.userAge" class="user-age">年龄: {{ scope.row.userAge }}</div>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="预约时间" width="160">
            <template slot-scope="scope">
              <div class="appointment-time-info">
                <div class="date-row">
                  <i class="el-icon-date time-icon"></i>
                  <span class="date-text">{{ parseTime(scope.row.appointmentDate, '{y}-{m}-{d}') }}</span>
                </div>
                <div class="time-row">
                  <i class="el-icon-time time-icon"></i>
                  <span class="time-text">{{ formatTimeSlot(scope.row.timeSlot) }}</span>
                </div>
                <div class="weekday-row">
                  <span class="weekday-badge">{{ getWeekday(scope.row.appointmentDate) }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="问题描述" min-width="200">
            <template slot-scope="scope">
              <div class="problem-description-card">
                <div class="problem-header">
                  <span class="problem-label">问题描述：</span>
                  <el-tag 
                    v-if="scope.row.urgencyLevel"
                    :type="getUrgencyType(scope.row.urgencyLevel)" 
                    size="mini"
                    class="urgency-inline"
                  >
                    {{ getUrgencyText(scope.row.urgencyLevel) }}
                  </el-tag>
                </div>
                <div class="problem-content" :title="scope.row.problemDescription">
                  {{ scope.row.problemDescription || '用户未填写问题描述' }}
                </div>
                <div v-if="scope.row.counselingNotes" class="notes-preview">
                  <span class="notes-label">笔记：</span>
                  <span class="notes-text">{{ scope.row.counselingNotes }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态评分" width="180">
            <template slot-scope="scope">
              <div class="status-rating-card">
                <div class="status-section">
                  <el-tag 
                    :type="getStatusType(scope.row.status)" 
                    size="small"
                    class="status-tag-main"
                  >
                    {{ getStatusText(scope.row.status) }}
                  </el-tag>
                  <div class="create-time-sm">
                    创建: {{ parseTime(scope.row.createTime, '{m}-{d} {h}:{i}') }}
                  </div>
                </div>
                <div v-if="scope.row.ratingScore" class="rating-section">
                  <div class="rating-display">
                    <div class="rating-stars-wrapper">
                      <el-rate
                        :value="scope.row.ratingScore"
                        disabled
                        :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                        size="mini"
                        class="rating-stars-medium"
                      />
                    </div>
                    <span class="score-text">{{ scope.row.ratingScore }}分</span>
                  </div>
                  <div v-if="scope.row.ratingComment" class="comment-preview" :title="scope.row.ratingComment">
                    {{ scope.row.ratingComment.substring(0, 20) }}{{ scope.row.ratingComment.length > 20 ? '...' : '' }}
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="200" fixed="right">
            <template slot-scope="scope">
              <div class="action-buttons-card">
                <div class="action-buttons">
                  <el-button
                    v-if="scope.row.status === 'pending'"
                    size="mini"
                    type="success"
                    @click="handleAccept(scope.row)"
                    class="accept-action-btn"
                    title="接受预约"
                  >接受</el-button>
                  <el-button
                    v-if="scope.row.status === 'pending'"
                    size="mini"
                    type="danger"
                    @click="handleReject(scope.row)"
                    class="reject-action-btn"
                    title="拒绝预约"
                  >拒绝</el-button>
                  <el-button
                    v-if="scope.row.status === 'accepted'"
                    size="mini"
                    type="primary"
                    @click="handleComplete(scope.row)"
                    class="complete-action-btn"
                    title="完成预约"
                  >完成</el-button>
                  <el-button
                    v-if="['accepted', 'completed'].includes(scope.row.status)"
                    size="mini"
                    type="info"
                    plain
                    @click="handleNotes(scope.row)"
                    class="notes-btn"
                    title="咨询笔记"
                  >笔记</el-button>
                  <el-button
                    v-if="scope.row.status === 'completed' && scope.row.ratingId"
                    size="mini"
                    type="text"
                    @click="viewRatingDetail(scope.row)"
                    class="view-rating-btn"
                    title="查看评价"
                  >评价</el-button>
                </div>
                <div class="action-tips" v-if="scope.row.status === 'pending'">
                  <i class="el-icon-warning"></i> 请及时处理
                </div>
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
          <i class="el-icon-s-order empty-icon"></i>
          <div class="empty-text">
            <h3>暂无预约记录</h3>
            <p>您还没有收到任何预约</p>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 对话框保持不变 -->
    <el-dialog title="接受预约" :visible.sync="acceptVisible" width="500px" append-to-body>
      <span>确定要接受该预约吗？</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="acceptVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitAccept" :loading="acceptLoading">确 定</el-button>
      </span>
    </el-dialog>

    <el-dialog title="拒绝预约" :visible.sync="rejectVisible" width="500px" append-to-body>
      <span>确定要拒绝该预约吗？拒绝后用户会收到通知。</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="rejectVisible = false">取 消</el-button>
        <el-button type="danger" @click="submitReject" :loading="rejectLoading">确 定</el-button>
      </span>
    </el-dialog>

    <el-dialog title="完成预约" :visible.sync="completeVisible" width="500px" append-to-body>
      <el-form ref="completeForm" :model="completeForm" label-width="80px">
        <el-form-item label="咨询笔记">
          <el-input
            v-model="completeForm.counselingNotes"
            type="textarea"
            :rows="4"
            placeholder="请输入本次咨询的笔记（用户可见）"
            maxlength="1000"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="completeVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitComplete" :loading="completeLoading">完 成</el-button>
      </span>
    </el-dialog>

    <el-dialog title="咨询笔记" :visible.sync="notesVisible" width="600px" append-to-body>
      <el-form ref="notesForm" :model="notesForm" label-width="80px">
        <el-form-item label="用户信息">
          <div><strong>{{ notesForm.userName }}</strong> - {{ parseTime(notesForm.appointmentDate, '{y}-{m}-{d}') }} {{ formatTimeSlot(notesForm.timeSlot) }}</div>
        </el-form-item>
        <el-form-item label="问题描述">
          <div class="problem-content">{{ notesForm.problemDescription || '无' }}</div>
        </el-form-item>
        <el-form-item label="咨询笔记">
          <el-input
            v-model="notesForm.counselingNotes"
            type="textarea"
            :rows="6"
            placeholder="请记录本次咨询的情况、发现的问题、建议等"
            maxlength="2000"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="notesVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitNotes" :loading="notesLoading">保 存</el-button>
      </span>
    </el-dialog>

    <el-dialog title="评价详情" :visible.sync="ratingDetailVisible" width="500px" append-to-body>
      <div v-if="ratingDetail" class="rating-detail-content">
        <div class="rating-score-section">
          <el-rate
            :value="ratingDetail.ratingScore"
            disabled
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
            size="large"
          />
          <div class="score-number">{{ ratingDetail.ratingScore }}分</div>
        </div>
        <div v-if="ratingDetail.ratingComment" class="rating-comment-section">
          <div class="comment-label">评价内容：</div>
          <div class="comment-content">{{ ratingDetail.ratingComment }}</div>
        </div>
        <div v-else class="no-comment">
          <i class="el-icon-chat-line-square"></i> 用户没有留下评价内容
        </div>
        <div class="rating-time">
          <i class="el-icon-time"></i>
          评价时间：{{ parseTime(ratingDetail.createTime) }}
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="ratingDetailVisible = false">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getPsychologistAppointments, 
  acceptAppointment, 
  rejectAppointment, 
  completeAppointment, 
  updateAppointmentNotes, 
  getAppointmentStatistics, 
  getTodayAppointmentsForPsychologist,
  getPsychologistDashboard,
  getRatingByAppointmentId
} from "@/api/mental/appointment";
import { parseTime } from "@/utils/ruoyi";

export default {
  name: "PsychologistAppointment",
  data() {
    return {
      statistics: {},
      ratingInfo: {
        averageRating: 0,
        totalRatings: 0
      },
      todayAppointments: [],
      todayLoading: false,
      loading: true,
      total: 0,
      appointmentList: [],
      statusFilter: '',
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderByColumn: 'appointmentDate',
        isAsc: 'asc'
      },
      selectedDate: new Date().toISOString().split('T')[0],
      
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
      
      acceptVisible: false,
      acceptLoading: false,
      acceptId: null,
      rejectVisible: false,
      rejectLoading: false,
      rejectId: null,
      completeVisible: false,
      completeLoading: false,
      completeId: null,
      completeForm: {
        counselingNotes: ''
      },
      notesVisible: false,
      notesLoading: false,
      notesForm: {},
      ratingDetailVisible: false,
      ratingDetail: null
    };
  },
  created() {
    this.loadStatistics();
    this.loadRatingInfo();
    this.loadTodayAppointments();
    this.getList();
  },
  methods: {
    // 为表格行添加类名
    tableRowClassName({ row }) {
      if (row.urgencyLevel === 'high') {
        return 'urgent-row';
      }
      if (row.status === 'pending') {
        return 'pending-row';
      }
      if (row.status === 'accepted') {
        return 'accepted-row';
      }
      return '';
    },
    
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
    
    formatTimeSlot(timeSlot) {
      return this.timeSlots[timeSlot] || timeSlot;
    },
    
    getUrgencyText(urgencyLevel) {
      return this.urgencyLevels[urgencyLevel]?.text || urgencyLevel;
    },
    
    getUrgencyType(urgencyLevel) {
      return this.urgencyLevels[urgencyLevel]?.type || 'info';
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
    
    async loadStatistics() {
      try {
        const response = await getAppointmentStatistics();
        if (response.code === 200) {
          this.statistics = response.data || {};
        } else {
          this.$message.error(response.msg || '加载统计信息失败');
        }
      } catch (error) {
        console.error('加载统计信息失败:', error);
        this.$message.error('加载统计信息失败');
      }
    },
    
    async loadRatingInfo() {
      try {
        const response = await getPsychologistDashboard();
        if (response.code === 200) {
          const data = response.data || {};
          this.ratingInfo = {
            averageRating: data.averageRating || 0,
            totalRatings: data.totalRatings || 0
          };
        } else {
          console.warn('加载评分信息失败:', response.msg);
        }
      } catch (error) {
        console.error('加载评分信息失败:', error);
      }
    },
    
    handleSizeChange(val) {
      this.queryParams.pageSize = val;
      this.getList();
    },
    
    handleCurrentChange(val) {
      this.queryParams.pageNum = val;
      this.getList();
    },
    
    async loadTodayAppointments() {
      this.todayLoading = true;
      try {
        console.log('加载今日预约，日期:', this.selectedDate);
        
        const response = await getTodayAppointmentsForPsychologist(this.selectedDate);
        
        if (response.code === 200) {
          this.todayAppointments = response.rows || [];
          console.log('今日预约数据:', this.todayAppointments);
        } else {
          console.warn('加载今日预约失败:', response.msg);
          this.todayAppointments = [];
        }
      } catch (error) {
        console.error('加载今日预约失败:', error);
        this.$message.error('加载今日预约失败');
        this.todayAppointments = [];
      } finally {
        this.todayLoading = false;
      }
    },
    
    refreshTodayAppointments() {
      this.selectedDate = new Date().toISOString().split('T')[0];
      this.loadTodayAppointments();
    },
    
    async getList() {
      this.loading = true;
      const params = {
        ...this.queryParams,
        status: this.statusFilter
      };
      
      try {
        const response = await getPsychologistAppointments(params);
        if (response.code === 200) {
          this.appointmentList = response.rows || [];
          this.total = response.total || 0;
        } else {
          this.$message.error(response.msg || '加载预约列表失败');
          this.appointmentList = [];
          this.total = 0;
        }
      } catch (error) {
        console.error('获取预约列表失败:', error);
        this.$message.error('获取预约列表失败');
        this.appointmentList = [];
        this.total = 0;
      } finally {
        this.loading = false;
      }
    },
    
    handleStatusChange() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    
    handleAccept(row) {
      this.acceptId = row.id;
      this.acceptVisible = true;
    },
    
    async submitAccept() {
      this.acceptLoading = true;
      try {
        const response = await acceptAppointment(this.acceptId);
        if (response.code === 200) {
          this.$modal.msgSuccess("接受成功");
          this.acceptVisible = false;
          this.loadStatistics();
          this.loadRatingInfo();
          this.loadTodayAppointments();
          this.getList();
        } else {
          this.$modal.msgError(response.msg || "接受失败");
        }
      } catch (error) {
        console.error('接受预约失败:', error);
        this.$modal.msgError("接受预约失败");
      } finally {
        this.acceptLoading = false;
      }
    },
    
    handleReject(row) {
      this.rejectId = row.id;
      this.rejectVisible = true;
    },
    
    async submitReject() {
      this.rejectLoading = true;
      try {
        const response = await rejectAppointment(this.rejectId);
        if (response.code === 200) {
          this.$modal.msgSuccess("已拒绝");
          this.rejectVisible = false;
          this.loadStatistics();
          this.loadRatingInfo();
          this.loadTodayAppointments();
          this.getList();
        } else {
          this.$modal.msgError(response.msg || "拒绝失败");
        }
      } catch (error) {
        console.error('拒绝预约失败:', error);
        this.$modal.msgError("拒绝预约失败");
      } finally {
        this.rejectLoading = false;
      }
    },
    
    handleComplete(row) {
      this.completeId = row.id;
      this.completeForm.counselingNotes = row.counselingNotes || '';
      this.completeVisible = true;
    },
    
    async submitComplete() {
      this.completeLoading = true;
      try {
        const response = await completeAppointment(this.completeId, this.completeForm.counselingNotes);
        if (response.code === 200) {
          this.$modal.msgSuccess("已完成");
          this.completeVisible = false;
          this.loadStatistics();
          this.loadRatingInfo();
          this.loadTodayAppointments();
          this.getList();
        } else {
          this.$modal.msgError(response.msg || "完成失败");
        }
      } catch (error) {
        console.error('完成预约失败:', error);
        this.$modal.msgError("完成预约失败");
      } finally {
        this.completeLoading = false;
      }
    },
    
    handleNotes(row) {
      this.notesForm = Object.assign({}, row);
      this.notesVisible = true;
    },
    
    async submitNotes() {
      this.notesLoading = true;
      try {
        const response = await updateAppointmentNotes(this.notesForm.id, this.notesForm.counselingNotes);
        if (response.code === 200) {
          this.$modal.msgSuccess("笔记保存成功");
          this.notesVisible = false;
          this.loadTodayAppointments();
          this.getList();
        } else {
          this.$modal.msgError(response.msg || "保存失败");
        }
      } catch (error) {
        console.error('保存笔记失败:', error);
        this.$modal.msgError("保存笔记失败");
      } finally {
        this.notesLoading = false;
      }
    },
    
    async viewRatingDetail(row) {
      try {
        const response = await getRatingByAppointmentId(row.id);
        if (response.code === 200 && response.data) {
          this.ratingDetail = response.data;
          this.ratingDetailVisible = true;
        } else {
          if (row.ratingScore) {
            this.ratingDetail = {
              ratingScore: row.ratingScore,
              ratingComment: row.ratingComment || '',
              createTime: row.updateTime || row.createTime
            };
            this.ratingDetailVisible = true;
          } else {
            this.$message.warning('该预约暂无评价');
          }
        }
      } catch (error) {
        console.error('查看评价详情失败:', error);
        if (row.ratingScore) {
          this.ratingDetail = {
            ratingScore: row.ratingScore,
            ratingComment: row.ratingComment || '',
            createTime: row.updateTime || row.createTime
          };
          this.ratingDetailVisible = true;
        } else {
          this.$message.error('查看评价详情失败');
        }
      }
    },
    
    parseTime
  }
};
</script>

<style scoped lang="scss">
.app-psychologist-appointment {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
  
  .header-left {
    h2 {
      margin: 0 0 8px 0;
      font-size: 22px;
      font-weight: 600;
      color: #303133;
      display: flex;
      align-items: center;
      
      i {
        margin-right: 10px;
        color: #409eff;
      }
    }
    
    .subtitle {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }
}

.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  border: none;
  border-radius: 8px;
  transition: all 0.3s;
  cursor: pointer;
  height: 100%;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 16px;
}

.stat-icon {
  font-size: 36px;
  margin-right: 16px;
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .total & {
    background-color: rgba(64, 158, 255, 0.1);
    color: #409eff;
  }
  
  .pending & {
    background-color: rgba(230, 162, 60, 0.1);
    color: #e6a23c;
  }
  
  .accepted & {
    background-color: rgba(103, 194, 58, 0.1);
    color: #67c23a;
  }
  
  .rating & {
    background-color: rgba(255, 153, 0, 0.1);
    color: #ff9900;
  }
}

.stat-info {
  flex: 1;
  
  .stat-title {
    font-size: 14px;
    color: #909399;
    margin-bottom: 4px;
  }
  
  .stat-value {
    font-size: 24px;
    font-weight: bold;
    color: #303133;
    line-height: 1;
  }
  
  .stat-subtitle {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}

.today-card, .appointment-card {
  border: none;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      display: flex;
      align-items: center;
      
      i {
        margin-right: 8px;
        color: #409eff;
      }
    }
    
    .card-subtitle {
      color: #909399;
      font-size: 13px;
    }
    
    .total-info {
      .total-count {
        background: #f0f2f5;
        color: #606266;
        padding: 4px 12px;
        border-radius: 12px;
        font-size: 13px;
        font-weight: 500;
      }
    }
  }
  
  .header-right {
    .filter-container {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .filter-label {
        color: #606266;
        font-size: 14px;
        white-space: nowrap;
      }
    }
  }
}

// 今日预约表格样式
.today-table {
  :deep(.el-table__row) {
    transition: all 0.3s;
    
    &:hover {
      background-color: #f8f9fa;
      transform: scale(1.002);
    }
  }
}

.user-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  
  .user-avatar-sm {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    font-weight: bold;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  .user-info-sm {
    text-align: center;
    
    .user-name {
      font-weight: 500;
      font-size: 13px;
      color: #303133;
      max-width: 80px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

.date-cell-sm {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  
  .date-main {
    font-weight: 600;
    color: #303133;
    font-size: 14px;
  }
  
  .weekday-sm {
    color: #409eff;
    font-size: 12px;
    background: rgba(64, 158, 255, 0.1);
    padding: 2px 8px;
    border-radius: 10px;
  }
}

.time-slot-sm {
  color: #67c23a;
  font-weight: 500;
  font-size: 13px;
  background: rgba(103, 194, 58, 0.1);
  padding: 4px 8px;
  border-radius: 6px;
  display: inline-block;
}

.problem-description-sm {
  font-size: 13px;
  line-height: 1.4;
  color: #606266;
  max-height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  padding: 4px 0;
}

.urgency-cell {
  .urgency-tag {
    border-radius: 12px;
    padding: 4px 10px;
    
    &.el-tag--info {
      background: linear-gradient(135deg, #f0f2f5 0%, #e6e8eb 100%);
      color: #8c8c8c;
      border-color: #d9d9d9;
    }
    
    &.el-tag--warning {
      background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
      color: #d46b08;
      border-color: #ffd591;
    }
    
    &.el-tag--danger {
      background: linear-gradient(135deg, #fff2f0 0%, #ffccc7 100%);
      color: #cf1322;
      border-color: #ffa39e;
    }
  }
}

.rating-cell-sm {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 4px 0;
  min-height: 50px;
  width: 100%;
  
  .rating-stars-container {
    width: 100%;
    display: flex;
    justify-content: center;
    margin-bottom: 2px;
    
    :deep(.rating-stars-small) {
      display: inline-flex !important;
      justify-content: center !important;
      white-space: nowrap !important;
      width: auto !important;
      
      .el-rate {
        display: inline-flex !important;
        justify-content: center !important;
        white-space: nowrap !important;
      }
      
      .el-rate__item {
        flex-shrink: 0 !important;
        margin-right: 2px !important;
      }
      
      .el-rate__icon {
        font-size: 12px !important;
      }
    }
  }
  
  .rating-score {
    color: #ff9900;
    font-size: 12px;
    font-weight: 600;
    text-align: center;
    padding: 2px 6px;
    background: rgba(255, 153, 0, 0.1);
    border-radius: 4px;
    min-width: 40px;
  }
}

.no-rating {
  color: #c0c4cc;
  font-style: italic;
  font-size: 13px;
}

.status-tag-sm {
  font-weight: 500;
  border-radius: 12px;
  padding: 4px 12px;
  min-width: 60px;
  
  &.el-tag--warning {
    background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
    color: #fff;
    border: none;
  }
  
  &.el-tag--success {
    background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%);
    color: #1677ff;
    border: none;
  }
  
  &.el-tag--info {
    background: linear-gradient(135deg, #d4fc79 0%, #96e6a1 100%);
    color: #52c41a;
    border: none;
  }
}

.action-buttons-sm {
  display: flex;
  justify-content: center;
  gap: 6px;
  
  .el-button {
    padding: 5px 12px;
    font-size: 12px;
    border-radius: 6px;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
    
    &.accept-btn {
      background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
      border-color: #52c41a;
      color: white;
      
      &:hover {
        background: linear-gradient(135deg, #389e0d 0%, #52c41a 100%);
      }
    }
    
    &.reject-btn {
      background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
      border-color: #ff4d4f;
      color: white;
      
      &:hover {
        background: linear-gradient(135deg, #d9363e 0%, #ff4d4f 100%);
      }
    }
    
    &.complete-btn {
      background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
      border-color: #1677ff;
      color: white;
      
      &:hover {
        background: linear-gradient(135deg, #0958d9 0%, #1677ff 100%);
      }
    }
  }
  
  .completed-tag {
    background: linear-gradient(135deg, #f0f0f0 0%, #d9d9d9 100%);
    color: #8c8c8c;
    border: none;
    padding: 4px 12px;
    border-radius: 12px;
    font-weight: 500;
  }
}

// 预约管理表格样式
.appointment-table {
  :deep(.el-table__body) {
    .urgent-row {
      background: linear-gradient(90deg, rgba(255, 77, 79, 0.05) 0%, transparent 100%) !important;
      
      &:hover {
        background: linear-gradient(90deg, rgba(255, 77, 79, 0.1) 0%, transparent 100%) !important;
      }
    }
    
    .pending-row {
      background: linear-gradient(90deg, rgba(250, 173, 20, 0.05) 0%, transparent 100%) !important;
      
      &:hover {
        background: linear-gradient(90deg, rgba(250, 173, 20, 0.1) 0%, transparent 100%) !important;
      }
    }
    
    .accepted-row {
      background: linear-gradient(90deg, rgba(82, 196, 26, 0.05) 0%, transparent 100%) !important;
      
      &:hover {
        background: linear-gradient(90deg, rgba(82, 196, 26, 0.1) 0%, transparent 100%) !important;
      }
    }
  }
}

.appointment-id-cell {
  .id-text {
    font-weight: 600;
    color: #1677ff;
    font-size: 14px;
    background: rgba(22, 119, 255, 0.1);
    padding: 4px 8px;
    border-radius: 6px;
    display: inline-block;
  }
}

.user-info-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px;
  
  .user-avatar-wrapper {
    .user-avatar {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      font-weight: bold;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }
    
    .user-avatar-default {
      background: linear-gradient(135deg, #36d1dc 0%, #5b86e5 100%);
      color: white;
      font-weight: bold;
    }
  }
  
  .user-details {
    flex: 1;
    min-width: 0; // 防止内容溢出
    
    .user-name {
      font-weight: 600;
      color: #303133;
      font-size: 14px;
      margin-bottom: 4px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    
    .user-meta {
      .user-id,
      .user-age {
        color: #909399;
        font-size: 11px;
        line-height: 1.4;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }
}

.appointment-time-info {
  padding: 4px;
  
  .date-row,
  .time-row {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 6px;
    
    .time-icon {
      color: #409eff;
      font-size: 14px;
      flex-shrink: 0;
    }
    
    .date-text {
      font-weight: 500;
      color: #303133;
      font-size: 14px;
      white-space: nowrap;
    }
    
    .time-text {
      color: #67c23a;
      font-weight: 500;
      font-size: 13px;
      white-space: nowrap;
    }
  }
  
  .weekday-row {
    .weekday-badge {
      background: linear-gradient(135deg, #409eff 0%, #1677ff 100%);
      color: white;
      font-size: 11px;
      padding: 2px 8px;
      border-radius: 10px;
      display: inline-block;
    }
  }
}

.problem-description-card {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  
  .problem-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    
    .problem-label {
      color: #606266;
      font-size: 13px;
      font-weight: 500;
      white-space: nowrap;
    }
    
    .urgency-inline {
      font-size: 10px;
      padding: 1px 6px;
      border-radius: 8px;
      flex-shrink: 0;
    }
  }
  
  .problem-content {
    color: #303133;
    font-size: 13px;
    line-height: 1.5;
    margin-bottom: 8px;
    max-height: 60px;
    overflow-y: auto;
    word-break: break-word;
    padding-right: 4px;
    
    &::-webkit-scrollbar {
      width: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: #c0c4cc;
      border-radius: 2px;
    }
  }
  
  .notes-preview {
    background: rgba(144, 147, 153, 0.05);
    border-left: 2px solid #909399;
    padding: 6px 8px;
    border-radius: 4px;
    
    .notes-label {
      color: #909399;
      font-size: 12px;
      font-weight: 500;
      white-space: nowrap;
    }
    
    .notes-text {
      color: #606266;
      font-size: 12px;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }
}

.status-rating-card {
  padding: 8px;
  
  .status-section {
    margin-bottom: 12px;
    
    .status-tag-main {
      font-weight: 600;
      padding: 6px 12px;
      border-radius: 14px;
      border: none;
      margin-bottom: 6px;
      display: inline-block;
      white-space: nowrap;
      
      &.el-tag--warning {
        background: linear-gradient(135deg, #ffd666 0%, #ffc53d 100%);
        color: #d46b08;
      }
      
      &.el-tag--success {
        background: linear-gradient(135deg, #b7eb8f 0%, #95de64 100%);
        color: #389e0d;
      }
      
      &.el-tag--info {
        background: linear-gradient(135deg, #b5f5ec 0%, #87e8de 100%);
        color: #08979c;
      }
      
      &.el-tag--danger {
        background: linear-gradient(135deg, #ffccc7 0%, #ffa39e 100%);
        color: #cf1322;
      }
    }
    
    .create-time-sm {
      color: #8c8c8c;
      font-size: 11px;
      background: #f5f5f5;
      padding: 2px 6px;
      border-radius: 4px;
      display: inline-block;
      white-space: nowrap;
    }
  }
  
  .rating-section {
    .rating-display {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 6px;
      
      .rating-stars-wrapper {
        flex: 1;
        min-width: 80px;
        
        :deep(.rating-stars-medium) {
          display: inline-flex !important;
          white-space: nowrap !important;
          width: 100% !important;
          
          .el-rate {
            display: inline-flex !important;
            white-space: nowrap !important;
            width: 100% !important;
          }
          
          .el-rate__item {
            flex-shrink: 0 !important;
            margin-right: 2px !important;
          }
          
          .el-rate__icon {
            font-size: 12px !important;
          }
        }
      }
      
      .score-text {
        color: #ff9900;
        font-size: 14px;
        font-weight: 600;
        min-width: 35px;
        flex-shrink: 0;
        text-align: center;
        background: rgba(255, 153, 0, 0.1);
        padding: 2px 6px;
        border-radius: 4px;
      }
    }
    
    .comment-preview {
      color: #8c8c8c;
      font-size: 11px;
      font-style: italic;
      background: #fafafa;
      padding: 4px 6px;
      border-radius: 4px;
      border-left: 2px solid #ff9900;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

.action-buttons-card {
  .action-buttons {
    display: flex;
    justify-content: center;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 8px;
    
    .el-button {
      padding: 5px 10px;
      font-size: 12px;
      border-radius: 6px;
      min-width: 50px;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }
      
      &.accept-action-btn {
        background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
        border-color: #52c41a;
        color: white;
        
        &:hover {
          background: linear-gradient(135deg, #389e0d 0%, #52c41a 100%);
        }
      }
      
      &.reject-action-btn {
        background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
        border-color: #ff4d4f;
        color: white;
        
        &:hover {
          background: linear-gradient(135deg, #d9363e 0%, #ff4d4f 100%);
        }
      }
      
      &.complete-action-btn {
        background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
        border-color: #1677ff;
        color: white;
        
        &:hover {
          background: linear-gradient(135deg, #0958d9 0%, #1677ff 100%);
        }
      }
      
      &.notes-btn {
        background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
        border-color: #722ed1;
        color: white;
        
        &:hover {
          background: linear-gradient(135deg, #531dab 0%, #722ed1 100%);
        }
      }
      
      &.view-rating-btn {
        background: linear-gradient(135deg, #fa8c16 0%, #faad14 100%);
        border-color: #fa8c16;
        color: white;
        
        &:hover {
          background: linear-gradient(135deg, #d46b08 0%, #fa8c16 100%);
        }
      }
    }
  }
  
  .action-tips {
    color: #d46b08;
    font-size: 11px;
    text-align: center;
    background: #fff7e6;
    padding: 4px;
    border-radius: 4px;
    border: 1px solid #ffe7ba;
    white-space: nowrap;
    
    i {
      margin-right: 4px;
    }
  }
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.empty-today {
  padding: 40px 20px;
  text-align: center;
  
  .empty-icon {
    font-size: 36px;
    color: #c0c4cc;
    margin-bottom: 12px;
  }
  
  .empty-text {
    color: #909399;
    font-size: 14px;
  }
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
  
  .empty-content {
    .empty-icon {
      font-size: 48px;
      color: #c0c4cc;
      margin-bottom: 16px;
    }
    
    .empty-text {
      h3 {
        margin: 0 0 8px 0;
        font-size: 18px;
        color: #606266;
      }
      
      p {
        margin: 0;
        color: #909399;
        font-size: 14px;
      }
    }
  }
}

// 对话框样式
:deep(.el-dialog) {
  border-radius: 8px;
  
  .el-dialog__header {
    border-bottom: 1px solid #ebeef5;
    padding: 20px;
  }
  
  .el-dialog__body {
    padding: 20px;
  }
  
  .el-dialog__footer {
    border-top: 1px solid #ebeef5;
    padding: 20px;
  }
}

.rating-detail-content {
  text-align: center;
  
  .rating-score-section {
    margin-bottom: 20px;
    
    .score-number {
      color: #ff9900;
      font-size: 20px;
      font-weight: bold;
      margin-top: 8px;
    }
  }
  
  .rating-comment-section {
    background-color: #f5f7fa;
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 16px;
    text-align: left;
    
    .comment-label {
      font-weight: 500;
      color: #303133;
      margin-bottom: 8px;
    }
    
    .comment-content {
      color: #606266;
      line-height: 1.6;
    }
  }
  
  .no-comment {
    color: #909399;
    font-style: italic;
    margin-bottom: 16px;
    padding: 16px;
    background-color: #f5f7fa;
    border-radius: 8px;
    
    i {
      margin-right: 8px;
    }
  }
  
  .rating-time {
    color: #909399;
    font-size: 12px;
    
    i {
      margin-right: 4px;
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .appointment-table {
    :deep(.el-table__body-wrapper) {
      overflow-x: auto;
    }
  }
  
  .problem-description-card {
    min-width: 200px;
  }
  
  .status-rating-card {
    min-width: 150px;
  }
}

@media (max-width: 768px) {
  .app-psychologist-appointment {
    padding: 16px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    
    .header-left {
      width: 100%;
      justify-content: space-between;
    }
    
    .header-right {
      width: 100%;
      
      .filter-container {
        flex-wrap: wrap;
        justify-content: space-between;
      }
    }
  }
  
  .action-buttons {
    flex-wrap: wrap;
    
    .el-button {
      margin-bottom: 4px;
    }
  }
  
  .user-info-card {
    flex-direction: column;
    text-align: center;
    
    .user-details {
      .user-name {
        max-width: 100%;
      }
    }
  }
  
  .rating-cell-sm {
    .rating-stars-container {
      :deep(.rating-stars-small) {
        .el-rate__item {
          margin-right: 1px !important;
        }
      }
    }
  }
}
</style>