<template>
  <div class="psychologist-schedule">
    <el-card class="schedule-card">
      <div slot="header" class="card-header">
        <div class="header-content">
          <i class="el-icon-date header-icon"></i>
          <span class="header-title">我的排班管理</span>
          <span class="header-subtitle">管理您的工作时间和可用时段</span>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="showMonthDialog" class="month-view-btn">
            <i class="el-icon-s-data"></i>
            <span>月视图</span>
          </el-button>
        </div>
      </div>
      
      <!-- 查询条件 -->
      <div class="query-section">
        <el-form :model="queryParams" ref="queryForm" :inline="true" class="query-form">
          <el-form-item label="排班日期" class="query-item">
            <el-date-picker
              v-model="queryParams.scheduleDate"
              type="date"
              placeholder="选择日期"
              value-format="yyyy-MM-dd"
              class="query-date-picker"
            >
            </el-date-picker>
          </el-form-item>
          <el-form-item label="状态" class="query-item">
            <el-select v-model="queryParams.isAvailable" placeholder="全部" class="query-select">
              <el-option label="全部" value=""></el-option>
              <el-option label="可用" value="1"></el-option>
              <el-option label="不可用" value="0"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item class="query-item">
            <el-button type="primary" icon="el-icon-search" @click="handleQuery" :loading="loading" class="query-btn">
              搜索
            </el-button>
            <el-button icon="el-icon-refresh" @click="resetQuery" class="reset-btn">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-section">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd" class="add-btn">
          新增排班
        </el-button>
        <el-button type="success" icon="el-icon-s-grid" @click="showBatchDialog" class="batch-btn">
          批量设置
        </el-button>
        <el-button 
          type="warning" 
          icon="el-icon-delete" 
          @click="handleDeleteSelected" 
          :disabled="multiple" 
          class="delete-btn"
        >
          批量删除
        </el-button>
      </div>
      
      <!-- 排班列表 -->
      <div class="table-section">
        <el-table 
          v-loading="loading" 
          :data="scheduleList" 
          @selection-change="handleSelectionChange"
          class="schedule-table"
          stripe
          border
        >
          <el-table-column type="selection" width="48" align="center" fixed="left" />
          <el-table-column label="排班日期" prop="scheduleDate" width="130" align="center" fixed="left">
            <template slot-scope="scope">
              <div class="date-display">
                <div class="date-number">{{ getDateNumber(scope.row.scheduleDate) }}</div>
                <div class="date-info">
                  <div class="date-month">{{ getDateMonth(scope.row.scheduleDate) }}</div>
                  <div class="date-weekday">{{ getDayOfWeek(scope.row.scheduleDate) }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="可用时段" min-width="280">
            <template slot-scope="scope">
              <div class="time-slots-display">
                <div v-if="scope.row.timeSlots && scope.row.timeSlots.length > 0" class="slots-grid">
                  <div 
                    v-for="slot in (scope.row.timeSlots || [])" 
                    :key="slot" 
                    class="time-slot-chip"
                  >
                    <i class="el-icon-time slot-icon"></i>
                    <span class="slot-text">{{ slot }}</span>
                  </div>
                </div>
                <div v-else class="no-slots">
                  <i class="el-icon-warning-outline"></i>
                  <span>暂无排班</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="预约量" width="110" align="center">
            <template slot-scope="scope">
              <div class="capacity-display">
                <div class="capacity-number">{{ scope.row.maxAppointments || 5 }}</div>
                <div class="capacity-label">个/时段</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template slot-scope="scope">
              <div class="status-display">
                <div 
                  :class="['status-badge', scope.row.isAvailable === '1' ? 'status-available' : 'status-unavailable']"
                >
                  {{ scope.row.isAvailable === '1' ? '可用' : '不可用' }}
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="180">
            <template slot-scope="scope">
              <div class="remarks-display" :title="scope.row.remarks">
                {{ scope.row.remarks || '无备注' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="160" align="center">
            <template slot-scope="scope">
              <div class="time-display">
                {{ formatDateTime(scope.row.createTime) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template slot-scope="scope">
              <div class="action-buttons">
                <el-button
                  size="small"
                  icon="el-icon-edit"
                  @click="handleUpdate(scope.row)"
                  class="edit-action-btn"
                >
                  编辑
                </el-button>
                <el-button
                  size="small"
                  icon="el-icon-copy-document"
                  @click="handleCopy(scope.row)"
                  class="copy-action-btn"
                >
                  复制
                </el-button>
                <el-button
                  size="small"
                  icon="el-icon-delete"
                  @click="handleDelete(scope.row)"
                  class="delete-action-btn"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-section">
        <el-pagination
          :total="total"
          :current-page.sync="queryParams.pageNum"
          :page-size.sync="queryParams.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="pagination"
        />
      </div>
    </el-card>
    
    <!-- 添加/编辑排班对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="720px"
      append-to-body
      @close="handleDialogClose"
      class="schedule-dialog"
    >
      <div class="dialog-content">
        <el-form ref="scheduleForm" :model="scheduleForm" :rules="scheduleRules" label-width="100px">
          <div class="form-section">
            <div class="form-row">
              <el-form-item label="排班日期" prop="scheduleDate" class="form-item">
                <div class="date-picker-wrapper">
                  <el-date-picker
                    v-model="scheduleForm.scheduleDate"
                    type="date"
                    placeholder="选择日期"
                    value-format="yyyy-MM-dd"
                    :picker-options="datePickerOptions"
                    class="date-picker"
                  >
                  </el-date-picker>
                  <div class="weekday-display">
                    {{ getDayOfWeek(scheduleForm.scheduleDate) }}
                  </div>
                </div>
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="状态设置" prop="isAvailable" class="form-item">
                <div class="availability-wrapper">
                  <el-switch
                    v-model="scheduleForm.isAvailableSwitch"
                    active-text="可用"
                    inactive-text="不可用"
                    active-color="#10b981"
                    inactive-color="#ef4444"
                    @change="handleAvailableChange"
                    class="availability-switch"
                  >
                  </el-switch>
                  <div v-if="!scheduleForm.isAvailableSwitch" class="availability-note">
                    <i class="el-icon-info"></i>
                    不可用时无法预约
                  </div>
                </div>
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="可用时段" prop="timeSlots" class="form-item">
                <div class="time-slots-wrapper">
                  <div class="slots-header">
                    <div class="slots-title">选择工作时段</div>
                    <div class="slots-actions">
                      <el-button size="small" @click="selectAllTimeSlots" class="slot-action-btn">
                        全选
                      </el-button>
                      <el-button size="small" @click="clearAllTimeSlots" class="slot-action-btn">
                        清空
                      </el-button>
                    </div>
                  </div>
                  <div class="slots-grid-wrapper">
                    <el-checkbox-group v-model="scheduleForm.timeSlots" class="time-slots-group">
                      <div 
                        v-for="option in timeSlotOptions" 
                        :key="option.value" 
                        :class="['slot-option', { 'disabled': !scheduleForm.isAvailableSwitch }]"
                      >
                        <el-checkbox 
                          :label="option.value"
                          :disabled="!scheduleForm.isAvailableSwitch"
                          class="slot-checkbox"
                        >
                          <div class="slot-content">
                            <i class="el-icon-time slot-option-icon"></i>
                            <span class="slot-option-text">{{ option.label }}</span>
                          </div>
                        </el-checkbox>
                      </div>
                    </el-checkbox-group>
                  </div>
                  <div class="slots-summary">
                    <div class="selected-count">
                      已选择 <span class="count-number">{{ scheduleForm.timeSlots.length }}</span> 个时段
                    </div>
                  </div>
                </div>
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="时段预约量" prop="maxAppointments" class="form-item">
                <div class="capacity-wrapper">
                  <el-input-number
                    v-model="scheduleForm.maxAppointments"
                    :min="1"
                    :max="20"
                    :step="1"
                    controls-position="right"
                    class="capacity-input"
                  >
                  </el-input-number>
                  <div class="capacity-note">
                    每个时段的最大预约数量
                  </div>
                </div>
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="备注信息" prop="remarks" class="form-item">
                <el-input
                  v-model="scheduleForm.remarks"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入备注信息，如特殊安排或注意事项等"
                  maxlength="500"
                  show-word-limit
                  class="remarks-input"
                >
                </el-input>
              </el-form-item>
            </div>
          </div>
        </el-form>
      </div>
      
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" size="medium" class="cancel-btn">
          取消
        </el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading" size="medium" class="submit-btn">
          确认保存
        </el-button>
      </span>
    </el-dialog>
    
    <!-- 批量设置对话框 -->
    <el-dialog
      title="批量设置排班"
      :visible.sync="batchDialogVisible"
      width="780px"
      append-to-body
      class="batch-dialog"
    >
      <div class="dialog-content">
        <el-form ref="batchForm" :model="batchForm" label-width="120px">
          <div class="form-section">
            <div class="form-row">
              <el-form-item label="日期范围" prop="dateRange" required class="form-item">
                <el-date-picker
                  v-model="batchForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="yyyy-MM-dd"
                  :picker-options="batchDatePickerOptions"
                  class="date-range-picker"
                >
                </el-date-picker>
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="适用日期" required class="form-item">
                <div class="weekdays-wrapper">
                  <div class="weekdays-grid">
                    <div 
                      v-for="day in weekdaysOptions" 
                      :key="day.value"
                      :class="['weekday-option', { 'selected': batchForm.weekdays.includes(day.value) }]"
                      @click="toggleWeekday(day.value)"
                    >
                      <div class="weekday-content">
                        <div class="weekday-text">{{ day.label }}</div>
                      </div>
                    </div>
                  </div>
                  <div class="weekdays-actions">
                    <el-button size="small" @click="selectWeekdays" class="weekday-action-btn">
                      工作日
                    </el-button>
                    <el-button size="small" @click="selectWeekend" class="weekday-action-btn">
                      周末
                    </el-button>
                    <el-button size="small" @click="selectAllWeekdays" class="weekday-action-btn">
                      全选
                    </el-button>
                  </div>
                </div>
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="可用时段" required class="form-item">
                <div class="batch-slots-wrapper">
                  <div class="slots-header">
                    <div class="slots-title">选择工作时段</div>
                    <div class="slots-actions">
                      <el-button size="small" @click="selectAllTimeSlotsBatch" class="slot-action-btn">
                        全选
                      </el-button>
                      <el-button size="small" @click="clearAllTimeSlotsBatch" class="slot-action-btn">
                        清空
                      </el-button>
                    </div>
                  </div>
                  <div class="slots-grid-wrapper">
                    <div 
                      v-for="option in timeSlotOptions" 
                      :key="option.value" 
                      :class="['batch-slot-option', { 'selected': batchForm.timeSlots.includes(option.value) }]"
                      @click="toggleBatchSlot(option.value)"
                    >
                      <div class="batch-slot-content">
                        <i class="el-icon-time batch-slot-icon"></i>
                        <span class="batch-slot-text">{{ option.label }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="时段预约量" class="form-item">
                <div class="batch-capacity-wrapper">
                  <el-input-number
                    v-model="batchForm.maxAppointments"
                    :min="1"
                    :max="20"
                    :step="1"
                    controls-position="right"
                    class="capacity-input"
                  >
                  </el-input-number>
                </div>
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="状态设置" class="form-item">
                <div class="batch-availability-wrapper">
                  <el-switch
                    v-model="batchForm.isAvailable"
                    active-text="可用"
                    inactive-text="不可用"
                    active-color="#10b981"
                    inactive-color="#ef4444"
                    class="availability-switch"
                  >
                  </el-switch>
                </div>
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="备注信息" class="form-item">
                <el-input
                  v-model="batchForm.remarks"
                  type="textarea"
                  :rows="2"
                  placeholder="备注信息"
                  class="remarks-input"
                >
                </el-input>
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="操作模式" class="form-item">
                <div class="mode-selector">
                  <div 
                    :class="['mode-option', { 'selected': batchForm.mode === 'add' }]"
                    @click="batchForm.mode = 'add'"
                  >
                    <div class="mode-content">
                      <i class="el-icon-plus"></i>
                      <div class="mode-text">新增排班</div>
                      <div class="mode-description">仅新增，不覆盖</div>
                    </div>
                  </div>
                  <div 
                    :class="['mode-option', { 'selected': batchForm.mode === 'update' }]"
                    @click="batchForm.mode = 'update'"
                  >
                    <div class="mode-content">
                      <i class="el-icon-refresh"></i>
                      <div class="mode-text">覆盖已有</div>
                      <div class="mode-description">更新已存在的排班</div>
                    </div>
                  </div>
                </div>
              </el-form-item>
            </div>
          </div>
        </el-form>
      </div>
      
      <span slot="footer" class="dialog-footer">
        <el-button @click="batchDialogVisible = false" size="medium" class="cancel-btn">
          取消
        </el-button>
        <el-button type="primary" @click="submitBatchForm" :loading="batchLoading" size="medium" class="submit-btn">
          批量设置
        </el-button>
      </span>
    </el-dialog>
    
    <!-- 月视图对话框 -->
    <el-dialog
      title="排班月视图"
      :visible.sync="monthDialogVisible"
      width="920px"
      append-to-body
      class="month-view-dialog"
    >
      <div class="month-view-container">
        <div class="month-navigation">
          <el-button icon="el-icon-arrow-left" circle @click="prevMonth" class="nav-btn">
          </el-button>
          <div class="month-title">{{ currentMonth }}</div>
          <el-button icon="el-icon-arrow-right" circle @click="nextMonth" class="nav-btn">
          </el-button>
        </div>
        
        <div class="calendar-container">
          <div class="calendar-header">
            <div class="day-header">周日</div>
            <div class="day-header">周一</div>
            <div class="day-header">周二</div>
            <div class="day-header">周三</div>
            <div class="day-header">周四</div>
            <div class="day-header">周五</div>
            <div class="day-header">周六</div>
          </div>
          
          <div class="calendar-grid">
            <div 
              v-for="(day, index) in calendarDays" 
              :key="index" 
              :class="['calendar-day', {
                'current-month': day.isCurrentMonth,
                'today': day.isToday,
                'has-schedule': day.hasSchedule,
                'weekend': day.isWeekend
              }]"
              @click="handleDayClick(day)"
            >
              <div class="day-content">
                <div class="day-header-content">
                  <div class="day-number">{{ day.day }}</div>
                  <div v-if="day.isToday" class="today-indicator"></div>
                </div>
                <div v-if="day.isCurrentMonth" class="day-body">
                  <div v-if="day.hasSchedule" class="schedule-indicators">
                    <div class="schedule-dots">
                      <div 
                        v-for="n in Math.min(day.scheduleCount, 3)" 
                        :key="n" 
                        class="schedule-dot"
                      ></div>
                    </div>
                    <div class="schedule-count">{{ day.scheduleCount }}时段</div>
                  </div>
                  <div v-else class="no-schedule">
                    未排班
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="calendar-legend">
          <div class="legend-item">
            <div class="legend-color today-color"></div>
            <span class="legend-text">今天</span>
          </div>
          <div class="legend-item">
            <div class="legend-color scheduled-color"></div>
            <span class="legend-text">已排班</span>
          </div>
          <div class="legend-item">
            <div class="legend-color weekend-color"></div>
            <span class="legend-text">周末</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getScheduleList, getScheduleByDate, saveSchedule, deleteSchedule, 
         batchSaveSchedule, getTimeSlotOptions, getScheduleCalendar } from "@/api/mental/appointment";

export default {
  name: "PsychologistSchedule",
  data() {
    return {
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        scheduleDate: null,
        isAvailable: ''
      },
      
      loading: false,
      submitLoading: false,
      batchLoading: false,
      
      scheduleList: [],
      total: 0,
      timeSlotOptions: [],
      ids: [],
      multiple: true,
      
      dialogVisible: false,
      batchDialogVisible: false,
      monthDialogVisible: false,
      dialogTitle: "",
      
      scheduleForm: {
        id: null,
        scheduleDate: null,
        timeSlots: [],
        maxAppointments: 5,
        isAvailable: '1',
        isAvailableSwitch: true,
        remarks: ''
      },
      
      batchForm: {
        dateRange: [],
        weekdays: ['1', '2', '3', '4', '5'],
        timeSlots: ['9:00-10:00', '14:00-15:00', '15:00-16:00'],
        maxAppointments: 5,
        isAvailable: true,
        remarks: '',
        mode: 'add'
      },
      
      currentMonth: '',
      calendarDays: [],
      scheduleMap: {},
      
      scheduleRules: {
        scheduleDate: [
          { required: true, message: '请选择排班日期', trigger: 'blur' }
        ],
        timeSlots: [
          { 
            type: 'array', 
            required: true, 
            message: '请选择至少一个时间段', 
            trigger: 'change',
            validator: (rule, value, callback) => {
              if (!value || value.length === 0) {
                callback(new Error('请选择至少一个时间段'));
              } else {
                callback();
              }
            }
          }
        ],
        maxAppointments: [
          { required: true, message: '请输入最大预约数', trigger: 'blur' }
        ]
      },
      
      datePickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 8.64e7;
        }
      },
      
      batchDatePickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 8.64e7;
        }
      },
      
      weekdaysOptions: [
        { label: '周一', value: '1' },
        { label: '周二', value: '2' },
        { label: '周三', value: '3' },
        { label: '周四', value: '4' },
        { label: '周五', value: '5' },
        { label: '周六', value: '6' },
        { label: '周日', value: '0' }
      ]
    };
  },
  created() {
    this.getList();
    this.loadTimeSlotOptions();
    this.initCurrentMonth();
  },
  methods: {
    // 获取日期数字
    getDateNumber(dateStr) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      return date.getDate().toString().padStart(2, '0');
    },
    
    // 获取月份
    getDateMonth(dateStr) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      const months = ['一月', '二月', '三月', '四月', '五月', '六月', 
                     '七月', '八月', '九月', '十月', '十一月', '十二月'];
      return months[date.getMonth()];
    },
    
    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return '';
      return this.parseTime(dateStr, '{y}-{m}-{d}');
    },
    
    // 格式化日期时间
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return '';
      return this.parseTime(dateTimeStr);
    },
    
    // 解析时间
    parseTime(time, pattern) {
      if (arguments.length === 0 || !time) {
        return null;
      }
      const format = pattern || '{y}-{m}-{d} {h}:{i}:{s}';
      let date;
      if (typeof time === 'object') {
        date = time;
      } else {
        if (typeof time === 'string' && /^\d+$/.test(time)) {
          time = parseInt(time);
        }
        if (typeof time === 'number' && time.toString().length === 10) {
          time = time * 1000;
        }
        date = new Date(time);
      }
      const formatObj = {
        y: date.getFullYear(),
        m: date.getMonth() + 1,
        d: date.getDate(),
        h: date.getHours(),
        i: date.getMinutes(),
        s: date.getSeconds(),
        a: date.getDay()
      };
      const time_str = format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
        let value = formatObj[key];
        if (key === 'a') {
          return ['日', '一', '二', '三', '四', '五', '六'][value];
        }
        if (result.length > 0 && value < 10) {
          value = '0' + value;
        }
        return value || 0;
      });
      return time_str;
    },
    
    // 获取星期几
    getDayOfWeek(dateStr) {
      if (!dateStr) return '';
      const date = new Date(dateStr);
      const days = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
      return days[date.getDay()];
    },
    
    // 加载时间段选项
    async loadTimeSlotOptions() {
      try {
        const response = await getTimeSlotOptions();
        if (response.code === 200 && response.data && response.data.length > 0) {
          this.timeSlotOptions = response.data;
        } else {
          this.timeSlotOptions = [
            { label: '09:00-10:00', value: '9:00-10:00' },
            { label: '10:00-11:00', value: '10:00-11:00' },
            { label: '11:00-12:00', value: '11:00-12:00' },
            { label: '14:00-15:00', value: '14:00-15:00' },
            { label: '15:00-16:00', value: '15:00-16:00' },
            { label: '16:00-17:00', value: '16:00-17:00' },
            { label: '17:00-18:00', value: '17:00-18:00' },
            { label: '18:00-19:00', value: '18:00-19:00' },
            { label: '19:00-20:00', value: '19:00-20:00' }
          ];
        }
      } catch (error) {
        console.error('加载时间段选项失败:', error);
        this.timeSlotOptions = [
          { label: '09:00-10:00', value: '9:00-10:00' },
          { label: '10:00-11:00', value: '10:00-11:00' },
          { label: '11:00-12:00', value: '11:00-12:00' },
          { label: '14:00-15:00', value: '14:00-15:00' },
          { label: '15:00-16:00', value: '15:00-16:00' },
          { label: '16:00-17:00', value: '16:00-17:00' },
          { label: '17:00-18:00', value: '17:00-18:00' },
          { label: '18:00-19:00', value: '18:00-19:00' },
          { label: '19:00-20:00', value: '19:00-20:00' }
        ];
      }
    },
    
    // 获取排班列表
    async getList() {
      this.loading = true;
      try {
        const response = await getScheduleList(this.queryParams);
        if (response.code === 200) {
          this.scheduleList = response.rows || [];
          this.total = response.total || 0;
          
          // 确保timeSlots是数组
          this.scheduleList.forEach(item => {
            if (item.timeSlots && typeof item.timeSlots === 'string') {
              try {
                item.timeSlots = JSON.parse(item.timeSlots);
              } catch (e) {
                // 如果解析失败，尝试按逗号分隔（兼容旧数据）
                item.timeSlots = item.timeSlots.split(',').filter(slot => slot.trim());
              }
            } else if (!item.timeSlots) {
              item.timeSlots = [];
            }
          });
        } else {
          this.$message.error(response.msg || '加载排班列表失败');
          this.scheduleList = [];
          this.total = 0;
        }
      } catch (error) {
        console.error('加载排班列表失败:', error);
        this.$message.error('加载排班列表失败');
        this.scheduleList = [];
        this.total = 0;
      } finally {
        this.loading = false;
      }
    },
    
    // 搜索
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    
    // 重置查询
    resetQuery() {
      this.resetForm("queryForm");
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        scheduleDate: null,
        isAvailable: ''
      };
      this.handleQuery();
    },
    
    // 表单重置
    resetForm(formName) {
      if (this.$refs[formName]) {
        this.$refs[formName].resetFields();
      }
    },
    
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.multiple = !selection.length;
    },
    
    // 新增排班
    handleAdd() {
      this.resetForm("scheduleForm");
      this.dialogTitle = "新增排班";
      this.scheduleForm = {
        id: null,
        scheduleDate: new Date().toISOString().split('T')[0],
        timeSlots: ['9:00-10:00', '14:00-15:00'],
        maxAppointments: 5,
        isAvailable: '1',
        isAvailableSwitch: true,
        remarks: ''
      };
      this.dialogVisible = true;
    },
    
    // 编辑排班
    handleUpdate(row) {
      this.resetForm("scheduleForm");
      this.dialogTitle = "编辑排班";

      // 确保timeSlots是数组
      let timeSlots = row.timeSlots;
      if (typeof timeSlots === 'string') {
        try {
          timeSlots = JSON.parse(timeSlots);
        } catch (e) {
          // 如果解析失败，尝试按逗号分隔（兼容旧数据）
          timeSlots = timeSlots.split(',').filter(slot => slot.trim());
        }
      } else if (!timeSlots) {
        timeSlots = [];
      }

      this.scheduleForm = {
        id: row.id,
        scheduleDate: this.formatDate(row.scheduleDate),
        timeSlots: timeSlots,
        maxAppointments: row.maxAppointments || 5,
        isAvailable: row.isAvailable || '1',
        isAvailableSwitch: row.isAvailable === '1',
        remarks: row.remarks || ''
      };
      this.dialogVisible = true;
    },
    
    // 复制排班
    handleCopy(row) {
      this.resetForm("scheduleForm");
      this.dialogTitle = "复制排班";

      let timeSlots = row.timeSlots;
      if (typeof timeSlots === 'string') {
        try {
          timeSlots = JSON.parse(timeSlots);
        } catch (e) {
          // 如果解析失败，尝试按逗号分隔（兼容旧数据）
          timeSlots = timeSlots.split(',').filter(slot => slot.trim());
        }
      } else if (!timeSlots) {
        timeSlots = [];
      }

      this.scheduleForm = {
        id: null,
        scheduleDate: new Date().toISOString().split('T')[0],
        timeSlots: [...timeSlots],
        maxAppointments: row.maxAppointments || 5,
        isAvailable: row.isAvailable || '1',
        isAvailableSwitch: row.isAvailable === '1',
        remarks: row.remarks || ''
      };
      this.dialogVisible = true;
    },
    
    // 删除排班
    handleDelete(row) {
      const scheduleDate = this.formatDate(row.scheduleDate);
      this.$confirm(`确定要删除 ${scheduleDate} 的排班吗？`, "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(async () => {
        try {
          const response = await deleteSchedule(row.id);
          if (response.code === 200) {
            this.$modal.msgSuccess("删除成功");
            this.getList();
          } else {
            this.$modal.msgError(response.msg || "删除失败");
          }
        } catch (error) {
          console.error('删除排班失败:', error);
          this.$modal.msgError("删除失败");
        }
      }).catch(() => {});
    },
    
    // 批量删除
    handleDeleteSelected() {
      if (this.ids.length === 0) {
        this.$modal.msgError("请选择要删除的排班");
        return;
      }
      
      this.$confirm(`确定要删除选中的 ${this.ids.length} 个排班吗？`, "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(async () => {
        try {
          const response = await deleteSchedule(this.ids.join(','));
          if (response.code === 200) {
            this.$modal.msgSuccess("删除成功");
            this.getList();
          } else {
            this.$modal.msgError(response.msg || "删除失败");
          }
        } catch (error) {
          console.error('批量删除失败:', error);
          this.$modal.msgError("删除失败");
        }
      }).catch(() => {});
    },
    
    // 对话框关闭
    handleDialogClose() {
      this.resetForm("scheduleForm");
    },
    
    // 是否可用切换
    handleAvailableChange(value) {
      this.scheduleForm.isAvailable = value ? '1' : '0';
      if (!value) {
        this.scheduleForm.timeSlots = [];
      }
    },
    
    // 全选时间段
    selectAllTimeSlots() {
      this.scheduleForm.timeSlots = this.timeSlotOptions.map(option => option.value);
    },
    
    // 清空时间段
    clearAllTimeSlots() {
      this.scheduleForm.timeSlots = [];
    },
    
    // 批量对话框全选时间段
    selectAllTimeSlotsBatch() {
      this.batchForm.timeSlots = this.timeSlotOptions.map(option => option.value);
    },
    
    // 批量对话框清空时间段
    clearAllTimeSlotsBatch() {
      this.batchForm.timeSlots = [];
    },
    
    // 切换工作日选择
    toggleWeekday(weekday) {
      const index = this.batchForm.weekdays.indexOf(weekday);
      if (index > -1) {
        this.batchForm.weekdays.splice(index, 1);
      } else {
        this.batchForm.weekdays.push(weekday);
      }
    },
    
    // 切换批量时间段
    toggleBatchSlot(slot) {
      const index = this.batchForm.timeSlots.indexOf(slot);
      if (index > -1) {
        this.batchForm.timeSlots.splice(index, 1);
      } else {
        this.batchForm.timeSlots.push(slot);
      }
    },
    
    // 选择工作日
    selectWeekdays() {
      this.batchForm.weekdays = ['1', '2', '3', '4', '5'];
    },
    
    // 选择周末
    selectWeekend() {
      this.batchForm.weekdays = ['0', '6'];
    },
    
    // 全选工作日
    selectAllWeekdays() {
      this.batchForm.weekdays = ['0', '1', '2', '3', '4', '5', '6'];
    },
    
    // 提交表单
    async submitForm() {
      this.$refs["scheduleForm"].validate(async (valid) => {
        if (valid) {
          if (this.scheduleForm.isAvailableSwitch && this.scheduleForm.timeSlots.length === 0) {
            this.$message.error('请选择至少一个时间段');
            return;
          }
          
          this.submitLoading = true;
          
          try {
            const formData = {
              ...this.scheduleForm,
              timeSlots: JSON.stringify(this.scheduleForm.timeSlots)
            };
            
            const response = await saveSchedule(formData);
            if (response.code === 200) {
              this.$modal.msgSuccess(this.dialogTitle === "新增排班" ? "新增成功" : "修改成功");
              this.dialogVisible = false;
              this.getList();
            } else {
              this.$modal.msgError(response.msg || "操作失败");
            }
          } catch (error) {
            console.error('保存排班失败:', error);
            this.$modal.msgError("操作失败");
          } finally {
            this.submitLoading = false;
          }
        }
      });
    },
    
    // 显示批量设置对话框
    showBatchDialog() {
      this.batchDialogVisible = true;
    },
    
    // 提交批量设置
    async submitBatchForm() {
      if (!this.batchForm.dateRange || this.batchForm.dateRange.length !== 2) {
        this.$modal.msgError("请选择日期范围");
        return;
      }
      
      if (this.batchForm.weekdays.length === 0) {
        this.$modal.msgError("请选择至少一个工作日");
        return;
      }
      
      if (this.batchForm.timeSlots.length === 0) {
        this.$modal.msgError("请选择至少一个时间段");
        return;
      }
      
      this.batchLoading = true;
      
      try {
        // 生成日期列表
        const startDate = new Date(this.batchForm.dateRange[0]);
        const endDate = new Date(this.batchForm.dateRange[1]);
        const scheduleList = [];
        
        let currentDate = new Date(startDate);
        while (currentDate <= endDate) {
          const weekday = currentDate.getDay().toString();
          if (this.batchForm.weekdays.includes(weekday)) {
            const scheduleDate = currentDate.toISOString().split('T')[0];
            scheduleList.push({
              scheduleDate: scheduleDate,
              timeSlots: JSON.stringify(this.batchForm.timeSlots),
              maxAppointments: this.batchForm.maxAppointments,
              isAvailable: this.batchForm.isAvailable ? '1' : '0',
              remarks: this.batchForm.remarks
            });
          }
          currentDate.setDate(currentDate.getDate() + 1);
        }
        
        if (scheduleList.length === 0) {
          this.$modal.msgError("在选定日期范围内没有符合条件的日期");
          this.batchLoading = false;
          return;
        }
        
        const response = await batchSaveSchedule(scheduleList);
        
        if (response.code === 200) {
          this.$modal.msgSuccess(`批量设置了 ${scheduleList.length} 个排班`);
          this.batchDialogVisible = false;
          this.getList();
        } else {
          this.$modal.msgError(response.msg || "批量设置失败");
        }
      } catch (error) {
        console.error('批量设置失败:', error);
        this.$modal.msgError("批量设置失败");
      } finally {
        this.batchLoading = false;
      }
    },
    
    // ==================== 月视图功能 ====================
    
    // 初始化当前月份
    initCurrentMonth() {
      const now = new Date();
      this.currentMonth = `${now.getFullYear()}年${(now.getMonth() + 1).toString().padStart(2, '0')}月`;
    },
    
    // 显示月视图
    showMonthDialog() {
      this.loadCalendarData();
      this.monthDialogVisible = true;
    },
    
    // 加载日历数据
    async loadCalendarData() {
      try {
        const monthStr = this.currentMonth.replace(/年|月/g, '-').slice(0, 7);
        const response = await getScheduleCalendar(monthStr);
        
        if (response.code === 200) {
          this.scheduleMap = response.data.scheduleMap || {};
        } else {
          this.scheduleMap = {};
        }
        this.generateCalendar();
      } catch (error) {
        console.error('加载日历数据失败:', error);
        this.scheduleMap = {};
        this.generateCalendar();
      }
    },
    
    // 生成日历
    generateCalendar() {
      const monthMatch = this.currentMonth.match(/(\d+)年(\d+)月/);
      if (!monthMatch) return;
      
      const year = parseInt(monthMatch[1]);
      const month = parseInt(monthMatch[2]);
      const firstDay = new Date(year, month - 1, 1);
      const lastDay = new Date(year, month, 0);
      const firstDayOfWeek = firstDay.getDay();
      const daysInMonth = lastDay.getDate();
      
      const today = new Date();
      const todayStr = today.toISOString().split('T')[0];
      
      this.calendarDays = [];
      
      // 上个月的日期
      const prevMonthLastDay = new Date(year, month - 1, 0).getDate();
      for (let i = 0; i < firstDayOfWeek; i++) {
        const day = prevMonthLastDay - firstDayOfWeek + i + 1;
        const date = new Date(year, month - 2, day);
        const weekday = date.getDay();
        
        this.calendarDays.push({
          day: day,
          isCurrentMonth: false,
          isToday: false,
          hasSchedule: false,
          isWeekend: weekday === 0 || weekday === 6,
          scheduleCount: 0,
          date: null
        });
      }
      
      // 当前月的日期
      for (let day = 1; day <= daysInMonth; day++) {
        const date = new Date(year, month - 1, day);
        const dateStr = date.toISOString().split('T')[0];
        const weekday = date.getDay();
        const schedule = this.scheduleMap[dateStr];
        const hasSchedule = schedule && schedule.timeSlots && schedule.timeSlots.length > 0;
        const scheduleCount = hasSchedule ? (Array.isArray(schedule.timeSlots) ? schedule.timeSlots.length : schedule.timeSlots.split(',').length) : 0;
        
        this.calendarDays.push({
          day: day,
          isCurrentMonth: true,
          isToday: dateStr === todayStr,
          hasSchedule: hasSchedule,
          isWeekend: weekday === 0 || weekday === 6,
          scheduleCount: scheduleCount,
          date: dateStr
        });
      }
      
      // 下个月的日期（补齐42个格子）
      const totalCells = 42;
      const remainingCells = totalCells - this.calendarDays.length;
      for (let i = 1; i <= remainingCells; i++) {
        const date = new Date(year, month, i);
        const weekday = date.getDay();
        
        this.calendarDays.push({
          day: i,
          isCurrentMonth: false,
          isToday: false,
          hasSchedule: false,
          isWeekend: weekday === 0 || weekday === 6,
          scheduleCount: 0,
          date: null
        });
      }
    },
    
    // 上个月
    prevMonth() {
      const monthMatch = this.currentMonth.match(/(\d+)年(\d+)月/);
      if (!monthMatch) return;
      
      let year = parseInt(monthMatch[1]);
      let month = parseInt(monthMatch[2]) - 1;
      if (month === 0) {
        month = 12;
        year = year - 1;
      }
      this.currentMonth = `${year}年${month.toString().padStart(2, '0')}月`;
      this.loadCalendarData();
    },
    
    // 下个月
    nextMonth() {
      const monthMatch = this.currentMonth.match(/(\d+)年(\d+)月/);
      if (!monthMatch) return;
      
      let year = parseInt(monthMatch[1]);
      let month = parseInt(monthMatch[2]) + 1;
      if (month === 13) {
        month = 1;
        year = year + 1;
      }
      this.currentMonth = `${year}年${month.toString().padStart(2, '0')}月`;
      this.loadCalendarData();
    },
    
    // 点击日期
    handleDayClick(day) {
      if (day.isCurrentMonth && day.date) {
        this.monthDialogVisible = false;
        
        // 查询该日期的排班
        this.getScheduleForDate(day.date);
      }
    },
    
    // 查询指定日期的排班
    async getScheduleForDate(date) {
      try {
        const response = await getScheduleByDate(date);
        if (response.code === 200 && response.data) {
          this.handleUpdate(response.data);
        } else {
          // 新增该日期的排班
          this.dialogTitle = "新增排班";
          this.scheduleForm = {
            id: null,
            scheduleDate: date,
            timeSlots: ['9:00-10:00', '14:00-15:00'],
            maxAppointments: 5,
            isAvailable: '1',
            isAvailableSwitch: true,
            remarks: ''
          };
          this.dialogVisible = true;
        }
      } catch (error) {
        console.error('获取排班详情失败:', error);
        this.dialogTitle = "新增排班";
        this.scheduleForm = {
          id: null,
          scheduleDate: date,
          timeSlots: ['9:00-10:00', '14:00-15:00'],
          maxAppointments: 5,
          isAvailable: '1',
          isAvailableSwitch: true,
          remarks: ''
        };
        this.dialogVisible = true;
      }
    }
  }
};
</script>

<style scoped lang="scss">
.psychologist-schedule {
  padding: 24px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  min-height: calc(100vh - 48px);
}

.schedule-card {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
  
  ::v-deep .el-card__header {
    background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
    border-bottom: 1px solid #e2e8f0;
    padding: 24px;
    border-radius: 16px 16px 0 0;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-content {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .header-icon {
      font-size: 24px;
      color: #3b82f6;
      margin-bottom: 4px;
    }
    
    .header-title {
      font-size: 20px;
      font-weight: 700;
      color: #1e293b;
      letter-spacing: -0.025em;
    }
    
    .header-subtitle {
      font-size: 14px;
      color: #64748b;
      font-weight: 400;
    }
  }
  
  .header-actions {
    .month-view-btn {
      background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
      border: none;
      border-radius: 12px;
      padding: 10px 20px;
      font-weight: 500;
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.25);
      
      &:hover {
        background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
        transform: translateY(-1px);
        box-shadow: 0 6px 16px rgba(59, 130, 246, 0.3);
      }
      
      i {
        margin-right: 6px;
      }
    }
  }
}

.query-section {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  margin: 0 24px 24px;
  border: 1px solid #e2e8f0;
}

.query-form {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  
  .query-item {
    margin: 0;
    
    ::v-deep .el-form-item__label {
      color: #475569;
      font-weight: 500;
      font-size: 14px;
    }
  }
}

.query-date-picker {
  width: 200px;
  
  ::v-deep .el-input__inner {
    border-radius: 10px;
    border: 1px solid #cbd5e1;
    color: #1e293b;
    
    &:hover {
      border-color: #94a3b8;
    }
  }
}

.query-select {
  width: 140px;
  
  ::v-deep .el-input__inner {
    border-radius: 10px;
    border: 1px solid #cbd5e1;
    color: #1e293b;
  }
}

.query-btn {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 500;
  color: #ffffff;
  
  &:hover {
    background: linear-gradient(135deg, #059669 0%, #047857 100%);
  }
}

.reset-btn {
  background: #f1f5f9;
  border: 1px solid #cbd5e1;
  border-radius: 10px;
  padding: 10px 20px;
  color: #475569;
  font-weight: 500;
  
  &:hover {
    background: #e2e8f0;
    border-color: #94a3b8;
    color: #334155;
  }
}

.action-section {
  display: flex;
  gap: 12px;
  padding: 0 24px 24px;
  
  .add-btn {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    border: none;
    border-radius: 10px;
    padding: 10px 20px;
    font-weight: 500;
    color: #ffffff;
    
    &:hover {
      background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
    }
  }
  
  .batch-btn {
    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
    border: none;
    border-radius: 10px;
    padding: 10px 20px;
    font-weight: 500;
    color: #ffffff;
    
    &:hover {
      background: linear-gradient(135deg, #059669 0%, #047857 100%);
    }
  }
  
  .delete-btn {
    background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
    border: none;
    border-radius: 10px;
    padding: 10px 20px;
    font-weight: 500;
    color: #ffffff;
    
    &:hover:not(:disabled) {
      background: linear-gradient(135deg, #d97706 0%, #b45309 100%);
    }
    
    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }
}

.table-section {
  padding: 0 24px;
}

.schedule-table {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  
  ::v-deep .el-table__header {
    th {
      background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
      color: #334155;
      font-weight: 600;
      font-size: 14px;
      padding: 16px 0;
      border-bottom: 2px solid #e2e8f0;
    }
  }
  
  ::v-deep .el-table__row {
    transition: all 0.2s ease;
    
    &:hover {
      background-color: #f8fafc;
      transform: translateY(-1px);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    }
  }
  
  ::v-deep .el-table__body {
    tr:nth-child(even) {
      background-color: #f8fafc;
    }
  }
}

.date-display {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  
  .date-number {
    width: 36px;
    height: 36px;
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    color: #ffffff;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    font-weight: 700;
  }
  
  .date-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    
    .date-month {
      color: #475569;
      font-size: 12px;
      font-weight: 500;
    }
    
    .date-weekday {
      color: #64748b;
      font-size: 11px;
    }
  }
}

.time-slots-display {
  .slots-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    
    .time-slot-chip {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%);
      border: 1px solid #93c5fd;
      border-radius: 8px;
      padding: 6px 10px;
      transition: all 0.2s ease;
      
      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 2px 4px rgba(59, 130, 246, 0.2);
      }
      
      .slot-icon {
        color: #3b82f6;
        font-size: 12px;
      }
      
      .slot-text {
        color: #1e40af;
        font-size: 12px;
        font-weight: 500;
      }
    }
  }
  
  .no-slots {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #94a3b8;
    font-size: 13px;
    
    i {
      font-size: 14px;
    }
  }
}

.capacity-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  
  .capacity-number {
    width: 36px;
    height: 36px;
    background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
    color: #ffffff;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 700;
  }
  
  .capacity-label {
    color: #64748b;
    font-size: 11px;
    font-weight: 500;
  }
}

.status-display {
  .status-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 6px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    
    &.status-available {
      background: linear-gradient(135deg, #10b981 0%, #059669 100%);
      color: #ffffff;
    }
    
    &.status-unavailable {
      background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
      color: #ffffff;
    }
  }
}

.remarks-display {
  color: #475569;
  font-size: 13px;
  line-height: 1.5;
  max-height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.time-display {
  color: #64748b;
  font-size: 12px;
  font-weight: 400;
}

.action-buttons {
  display: flex;
  gap: 8px;
  
  .edit-action-btn {
    background: rgba(59, 130, 246, 0.1);
    border: 1px solid #93c5fd;
    color: #2563eb;
    border-radius: 8px;
    padding: 6px 12px;
    font-size: 12px;
    font-weight: 500;
    
    &:hover {
      background: rgba(59, 130, 246, 0.2);
      border-color: #2563eb;
    }
  }
  
  .copy-action-btn {
    background: rgba(139, 92, 246, 0.1);
    border: 1px solid #a78bfa;
    color: #7c3aed;
    border-radius: 8px;
    padding: 6px 12px;
    font-size: 12px;
    font-weight: 500;
    
    &:hover {
      background: rgba(139, 92, 246, 0.2);
      border-color: #7c3aed;
    }
  }
  
  .delete-action-btn {
    background: rgba(239, 68, 68, 0.1);
    border: 1px solid #fca5a5;
    color: #dc2626;
    border-radius: 8px;
    padding: 6px 12px;
    font-size: 12px;
    font-weight: 500;
    
    &:hover {
      background: rgba(239, 68, 68, 0.2);
      border-color: #dc2626;
    }
  }
}

.pagination-section {
  padding: 24px;
  border-top: 1px solid #e2e8f0;
}

// 对话框样式
.schedule-dialog, .batch-dialog, .month-view-dialog {
  ::v-deep .el-dialog {
    border-radius: 16px;
    box-shadow: 0 12px 48px rgba(0, 0, 0, 0.12);
    border: 1px solid #e2e8f0;
    
    .el-dialog__header {
      background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
      border-bottom: 1px solid #e2e8f0;
      padding: 24px;
      border-radius: 16px 16px 0 0;
      
      .el-dialog__title {
        color: #1e293b;
        font-size: 18px;
        font-weight: 700;
      }
    }
    
    .el-dialog__body {
      padding: 0;
    }
    
    .el-dialog__footer {
      background: #f8fafc;
      border-top: 1px solid #e2e8f0;
      padding: 24px;
      border-radius: 0 0 16px 16px;
    }
  }
}

.dialog-content {
  padding: 32px;
}

.form-section {
  .form-row {
    margin-bottom: 24px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
}

.form-item {
  margin: 0;
  
  ::v-deep .el-form-item__label {
    color: #334155;
    font-weight: 600;
    font-size: 14px;
  }
}

.date-picker-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .weekday-display {
    background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%);
    color: #2563eb;
    padding: 10px 16px;
    border-radius: 10px;
    font-size: 14px;
    font-weight: 500;
  }
}

.availability-wrapper {
  .availability-note {
    margin-top: 8px;
    color: #ef4444;
    font-size: 13px;
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.time-slots-wrapper {
  .slots-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .slots-title {
      color: #334155;
      font-weight: 600;
      font-size: 14px;
    }
  }
  
  .slots-grid-wrapper {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    margin-bottom: 16px;
    
    .slot-option {
      &.disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }
  }
  
  .slot-checkbox {
    width: 100%;
    
    ::v-deep .el-checkbox__label {
      width: 100%;
    }
  }
  
  .slot-content {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    background: #f8fafc;
    border-radius: 10px;
    border: 2px solid #e2e8f0;
    transition: all 0.2s ease;
    
    &:hover {
      border-color: #cbd5e1;
      background: #f1f5f9;
    }
    
    .slot-option-icon {
      color: #64748b;
      font-size: 14px;
    }
    
    .slot-option-text {
      color: #475569;
      font-size: 13px;
      font-weight: 500;
    }
  }
  
  ::v-deep .el-checkbox.is-checked {
    .slot-content {
      background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%);
      border-color: #3b82f6;
      
      .slot-option-icon, .slot-option-text {
        color: #2563eb;
      }
    }
  }
  
  .slots-summary {
    text-align: right;
    
    .selected-count {
      color: #64748b;
      font-size: 13px;
      
      .count-number {
        color: #10b981;
        font-weight: 700;
        font-size: 14px;
      }
    }
  }
}

.capacity-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .capacity-note {
    color: #64748b;
    font-size: 13px;
  }
}

.remarks-input {
  ::v-deep textarea {
    border-radius: 10px;
    border: 1px solid #cbd5e1;
    color: #475569;
    
    &:focus {
      border-color: #3b82f6;
      box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
    }
  }
}

.dialog-footer {
  .cancel-btn {
    background: #ffffff;
    border: 1px solid #cbd5e1;
    color: #475569;
    border-radius: 10px;
    padding: 10px 24px;
    font-weight: 500;
    
    &:hover {
      background: #f1f5f9;
      border-color: #94a3b8;
      color: #334155;
    }
  }
  
  .submit-btn {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    border: none;
    border-radius: 10px;
    padding: 10px 24px;
    font-weight: 500;
    color: #ffffff;
    
    &:hover {
      background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
    }
  }
}

// 批量对话框特殊样式
.weekdays-wrapper {
  .weekdays-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 8px;
    margin-bottom: 16px;
    
    .weekday-option {
      background: #f8fafc;
      border: 2px solid #e2e8f0;
      border-radius: 10px;
      padding: 12px 8px;
      text-align: center;
      cursor: pointer;
      transition: all 0.2s ease;
      
      &:hover {
        border-color: #cbd5e1;
        background: #f1f5f9;
      }
      
      &.selected {
        background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%);
        border-color: #3b82f6;
        
        .weekday-content {
          .weekday-text {
            color: #2563eb;
            font-weight: 600;
          }
        }
      }
      
      .weekday-content {
        .weekday-text {
          color: #475569;
          font-size: 13px;
          font-weight: 500;
        }
      }
    }
  }
}

.batch-slots-wrapper {
  .slots-grid-wrapper {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    margin-bottom: 16px;
    
    .batch-slot-option {
      background: #f8fafc;
      border: 2px solid #e2e8f0;
      border-radius: 10px;
      padding: 12px;
      cursor: pointer;
      transition: all 0.2s ease;
      
      &:hover {
        border-color: #cbd5e1;
        background: #f1f5f9;
      }
      
      &.selected {
        background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%);
        border-color: #3b82f6;
        
        .batch-slot-content {
          .batch-slot-icon, .batch-slot-text {
            color: #2563eb;
          }
        }
      }
      
      .batch-slot-content {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .batch-slot-icon {
          color: #64748b;
          font-size: 14px;
        }
        
        .batch-slot-text {
          color: #475569;
          font-size: 13px;
          font-weight: 500;
        }
      }
    }
  }
}

.mode-selector {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  
  .mode-option {
    background: #f8fafc;
    border: 2px solid #e2e8f0;
    border-radius: 12px;
    padding: 20px;
    cursor: pointer;
    transition: all 0.2s ease;
    
    &:hover {
      border-color: #cbd5e1;
      background: #f1f5f9;
    }
    
    &.selected {
      background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%);
      border-color: #3b82f6;
    }
    
    .mode-content {
      text-align: center;
      
      i {
        font-size: 24px;
        color: #3b82f6;
        margin-bottom: 8px;
      }
      
      .mode-text {
        color: #334155;
        font-size: 14px;
        font-weight: 600;
        margin-bottom: 4px;
      }
      
      .mode-description {
        color: #64748b;
        font-size: 12px;
      }
    }
  }
}

// 月视图样式
.month-view-container {
  .month-navigation {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 24px;
    margin-bottom: 32px;
    
    .nav-btn {
      width: 40px;
      height: 40px;
      background: #ffffff;
      border: 1px solid #e2e8f0;
      color: #64748b;
      
      &:hover {
        background: #f1f5f9;
        border-color: #cbd5e1;
        color: #475569;
      }
    }
    
    .month-title {
      color: #1e293b;
      font-size: 20px;
      font-weight: 700;
      min-width: 140px;
      text-align: center;
    }
  }
  
  .calendar-container {
    .calendar-header {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      margin-bottom: 8px;
      
      .day-header {
        text-align: center;
        color: #64748b;
        font-size: 14px;
        font-weight: 600;
        padding: 12px 0;
      }
    }
    
    .calendar-grid {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      gap: 1px;
      background: #e2e8f0;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      overflow: hidden;
      
      .calendar-day {
        background: #ffffff;
        min-height: 120px;
        padding: 12px;
        position: relative;
        cursor: pointer;
        transition: all 0.2s ease;
        
        &:hover {
          background: #f8fafc;
          z-index: 1;
          transform: scale(1.02);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        &.current-month {
          .day-content {
            opacity: 1;
          }
        }
        
        &:not(.current-month) {
          .day-content {
            opacity: 0.5;
          }
        }
        
        &.today {
          background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%);
          
          .day-header-content {
            .today-indicator {
              display: block;
            }
          }
        }
        
        &.has-schedule {
          .schedule-indicators {
            .schedule-dots {
              .schedule-dot {
                background: #10b981;
              }
            }
            
            .schedule-count {
              color: #10b981;
              font-weight: 600;
            }
          }
        }
        
        &.weekend {
          background: #fef2f2;
          
          .day-number {
            color: #ef4444;
          }
        }
        
        .day-content {
          height: 100%;
          display: flex;
          flex-direction: column;
          
          .day-header-content {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;
            
            .day-number {
              color: #334155;
              font-size: 16px;
              font-weight: 700;
            }
            
            .today-indicator {
              display: none;
              width: 8px;
              height: 8px;
              background: #3b82f6;
              border-radius: 50%;
            }
          }
          
          .day-body {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: center;
            
            .schedule-indicators {
              text-align: center;
              
              .schedule-dots {
                display: flex;
                justify-content: center;
                gap: 4px;
                margin-bottom: 4px;
                
                .schedule-dot {
                  width: 6px;
                  height: 6px;
                  background: #94a3b8;
                  border-radius: 50%;
                }
              }
              
              .schedule-count {
                color: #64748b;
                font-size: 11px;
              }
            }
            
            .no-schedule {
              color: #94a3b8;
              font-size: 12px;
              font-style: italic;
              text-align: center;
              margin-top: 8px;
            }
          }
        }
      }
    }
    
    .calendar-legend {
      display: flex;
      justify-content: center;
      gap: 24px;
      margin-top: 24px;
      padding: 16px;
      background: #f8fafc;
      border-radius: 12px;
      
      .legend-item {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .legend-color {
          width: 16px;
          height: 16px;
          border-radius: 4px;
          
          &.today-color {
            background: linear-gradient(135deg, #dbeafe 0%, #e0e7ff 100%);
            border: 1px solid #3b82f6;
          }
          
          &.scheduled-color {
            background: #10b981;
          }
          
          &.weekend-color {
            background: #fef2f2;
            border: 1px solid #fecaca;
          }
        }
        
        .legend-text {
          color: #475569;
          font-size: 13px;
          font-weight: 500;
        }
      }
    }
  }
}

// 分页样式
.pagination {
  display: flex;
  justify-content: center;
}

::v-deep .el-pagination {
  .btn-prev, .btn-next, .el-pager li {
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    color: #475569;
    font-weight: 500;
    
    &:hover {
      color: #2563eb;
      border-color: #93c5fd;
    }
  }
  
  .el-pager li.active {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    border-color: #3b82f6;
    color: #ffffff;
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .slots-grid-wrapper {
    grid-template-columns: repeat(2, 1fr) !important;
  }
  
  .batch-slots-wrapper .slots-grid-wrapper {
    grid-template-columns: repeat(2, 1fr) !important;
  }
}

@media (max-width: 768px) {
  .psychologist-schedule {
    padding: 16px;
  }
  
  .query-form {
    flex-direction: column;
    gap: 16px;
  }
  
  .query-date-picker,
  .query-select {
    width: 100%;
  }
  
  .action-section {
    flex-wrap: wrap;
  }
  
  .slots-grid-wrapper,
  .batch-slots-wrapper .slots-grid-wrapper {
    grid-template-columns: 1fr !important;
  }
  
  .mode-selector {
    grid-template-columns: 1fr !important;
  }
  
  .calendar-day {
    min-height: 100px !important;
    padding: 8px !important;
  }
}
</style>