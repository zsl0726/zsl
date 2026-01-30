<template>
  <div class="app-user-appointment">
    <div class="page-header">
      <h2>预约心理咨询</h2>
      <p class="subtitle">请填写预约信息，选择专业咨询师为您服务</p>
    </div>
    
    <div class="content-container">
      <!-- 左侧预约表单 -->
      <el-card class="form-card" shadow="hover">
        <div slot="header" class="card-header">
          <h3><i class="el-icon-edit"></i> 预约信息</h3>
        </div>
        
        <el-form ref="form" :model="form" :rules="rules" label-width="100px">
          <!-- 咨询师选择 -->
          <el-form-item label="咨询师" prop="psychologistId" class="form-item">
            <el-select 
              v-model="form.psychologistId" 
              placeholder="请选择咨询师" 
              style="width: 100%"
              @change="handlePsychologistChange"
              :loading="psychologistLoading"
              filterable
              size="medium"
            >
              <el-option-group
                v-for="group in groupedPsychologists"
                :key="group.title"
                :label="group.title">
                <el-option
                  v-for="psychologist in group.list"
                  :key="psychologist.userId"
                  :label="psychologist.nickName"
                  :value="psychologist.userId">
                  <div class="psychologist-option">
                    <el-avatar 
                      :size="32" 
                      :src="psychologist.avatar" 
                      class="option-avatar"
                    >
                      {{ psychologist.nickName ? psychologist.nickName.charAt(0) : '心' }}
                    </el-avatar>
                    
                    <div class="option-info">
                      <div class="option-header">
                        <span class="option-name">{{ psychologist.nickName }}</span>
                        <el-tag size="mini" :type="getExperienceTagType(psychologist.experience)">
                          {{ psychologist.experience }}
                        </el-tag>
                      </div>
                      
                      <div class="option-details">
                        <div class="option-rating">
                          <el-rate
                            v-if="psychologist.averageRating > 0"
                            :value="psychologist.averageRating"
                            disabled
                            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                            size="mini"
                            class="rating-stars"
                          />
                          <span v-if="psychologist.averageRating > 0" class="rating-score">
                            {{ psychologist.averageRating.toFixed(1) }}
                          </span>
                          <span v-if="psychologist.totalRatings > 0" class="rating-count">
                            ({{ psychologist.totalRatings }})
                          </span>
                          <span v-else class="no-rating">暂无评价</span>
                        </div>
                        
                        <div class="option-specialty">
                          {{ psychologist.specialty }}
                        </div>
                      </div>
                    </div>
                  </div>
                </el-option>
              </el-option-group>
            </el-select>
          </el-form-item>
          
          <!-- 日期和时间 -->
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="预约日期" prop="appointmentDate">
                <el-date-picker
                  v-model="form.appointmentDate"
                  type="date"
                  placeholder="选择日期"
                  value-format="yyyy-MM-dd"
                  :picker-options="datePickerOptions"
                  @change="handleDateChange"
                  style="width: 100%"
                  size="medium"
                >
                </el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="时间段" prop="timeSlot">
                <el-select 
                  v-model="form.timeSlot" 
                  placeholder="请选择时间段" 
                  style="width: 100%"
                  :loading="timeSlotLoading"
                  @change="validateTimeSlot"
                  size="medium"
                >
                  <el-option
                    v-for="slot in availableTimeSlots"
                    :key="slot.value"
                    :label="slot.label"
                    :value="slot.value"
                    :disabled="slot.disabled">
                  </el-option>
                </el-select>
                <div v-if="timeSlotWarning" class="time-slot-warning">
                  <i class="el-icon-warning"></i> {{ timeSlotWarning }}
                </div>
              </el-form-item>
            </el-col>
          </el-row>
          
          <!-- 地点和紧急程度 -->
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="咨询地点" prop="locationId">
                <el-select 
                  v-model="form.locationId" 
                  placeholder="请选择地点" 
                  style="width: 100%" 
                  @change="handleLocationChange"
                  size="medium"
                >
                  <el-option
                    v-for="location in locations"
                    :key="location.id"
                    :label="location.name"
                    :value="location.id">
                    <div class="location-option">
                      <span class="location-name">{{ location.name }}</span>
                      <span class="location-address">{{ location.address }}</span>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="紧急程度" prop="urgencyLevel">
                <el-select v-model="form.urgencyLevel" placeholder="请选择紧急程度" style="width: 100%" size="medium">
                  <el-option label="普通" value="普通"></el-option>
                  <el-option label="紧急" value="紧急"></el-option>
                  <el-option label="非常紧急" value="非常紧急"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <!-- 地点名称 -->
          <el-form-item label="地点名称" prop="locationName">
            <el-input
              v-model="form.locationName"
              placeholder="自动填充地点名称"
              readonly
              size="medium"
            ></el-input>
          </el-form-item>
          
          <!-- 问题描述 -->
          <el-form-item label="问题描述" prop="problemDescription">
            <el-input
              v-model="form.problemDescription"
              type="textarea"
              :rows="4"
              placeholder="请简要描述您的问题或咨询需求（例如：工作压力大、人际关系困扰、情绪管理等）"
              maxlength="500"
              show-word-limit
              resize="none"
              size="medium"
            >
            </el-input>
            <div class="textarea-tips">
              <i class="el-icon-info"></i>
              详细描述有助于咨询师更好地理解您的问题
            </div>
          </el-form-item>
          
          <!-- 操作按钮 -->
          <el-form-item class="form-actions">
            <div class="button-group">
              <el-button type="primary" @click="submitForm" :loading="loading" class="submit-btn">
                <i class="el-icon-check"></i> 提交预约
              </el-button>
              <el-button @click="resetForm" class="reset-btn">
                <i class="el-icon-refresh-right"></i> 重置
              </el-button>
              <el-button type="text" @click="goToMyAppointments" class="view-btn">
                <i class="el-icon-tickets"></i> 我的预约
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
      
      <!-- 右侧咨询师信息 -->
      <div class="right-panel">
        <!-- 咨询师信息卡片 -->
        <el-card v-if="selectedPsychologistInfo" class="info-card" shadow="hover">
          <div slot="header" class="card-header">
            <h3><i class="el-icon-user"></i> 咨询师信息</h3>
          </div>
          
          <div class="psychologist-info">
            <div class="info-avatar">
              <el-avatar :size="80" :src="selectedPsychologistInfo.avatar" class="avatar">
                {{ selectedPsychologistInfo.nickName ? selectedPsychologistInfo.nickName.charAt(0) : '心' }}
              </el-avatar>
              <div class="avatar-badge">
                <i class="el-icon-medal"></i>
              </div>
            </div>
            
            <div class="info-content">
              <h3 class="psychologist-name">{{ selectedPsychologistInfo.nickName }}</h3>
              <p class="psychologist-title">专业心理咨询师</p>
              
              <!-- 评分信息 -->
              <div v-if="selectedPsychologistInfo.averageRating > 0" class="rating-info">
                <el-rate
                  :value="selectedPsychologistInfo.averageRating"
                  disabled
                  :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                  class="rating-stars"
                />
                <div class="rating-details">
                  <span class="rating-score">{{ selectedPsychologistInfo.averageRating.toFixed(1) }}</span>
                  <span class="rating-count">{{ selectedPsychologistInfo.totalRatings || 0 }}条评价</span>
                </div>
              </div>
              <div v-else class="no-rating">
                <span>暂无评分</span>
              </div>
              
              <!-- 详细信息 -->
              <div class="detail-info">
                <div class="detail-item">
                  <div class="detail-icon">
                    <i class="el-icon-star-on"></i>
                  </div>
                  <div class="detail-content">
                    <span class="detail-label">专业领域</span>
                    <span class="detail-value">{{ selectedPsychologistInfo.specialty }}</span>
                  </div>
                </div>
                
                <div class="detail-item">
                  <div class="detail-icon">
                    <i class="el-icon-trophy"></i>
                  </div>
                  <div class="detail-content">
                    <span class="detail-label">经验等级</span>
                    <span class="detail-value">{{ selectedPsychologistInfo.experience }}</span>
                  </div>
                </div>
                
                <div v-if="selectedPsychologistInfo.consultationCount" class="detail-item">
                  <div class="detail-icon">
                    <i class="el-icon-chat-line-square"></i>
                  </div>
                  <div class="detail-content">
                    <span class="detail-label">咨询次数</span>
                    <span class="detail-value">{{ selectedPsychologistInfo.consultationCount }}次</span>
                  </div>
                </div>
              </div>
              
              <!-- 简介 -->
              <div v-if="selectedPsychologistInfo.description" class="description">
                <div class="description-header">
                  <i class="el-icon-document"></i>
                  <span>专业简介</span>
                </div>
                <p class="description-content">{{ selectedPsychologistInfo.description }}</p>
              </div>
            </div>
          </div>
          
          <div class="card-footer">
            <i class="el-icon-success"></i>
            <span>选择专业咨询师，开启心灵疗愈之旅</span>
          </div>
        </el-card>
        
        <!-- 提示卡片 -->
        <el-card v-else class="info-card tip-card" shadow="hover">
          <div slot="header" class="card-header">
            <h3><i class="el-icon-info"></i> 温馨提示</h3>
          </div>
          
          <div class="tip-content">
            <div class="tip-item">
              <div class="tip-icon">
                <i class="el-icon-user"></i>
              </div>
              <div class="tip-text">
                <h4>请先选择咨询师</h4>
                <p>在左侧选择一位专业的心理咨询师</p>
              </div>
            </div>
            
            <div class="tip-item">
              <div class="tip-icon">
                <i class="el-icon-date"></i>
              </div>
              <div class="tip-text">
                <h4>选择合适时间</h4>
                <p>根据您的日程安排选择咨询时间</p>
              </div>
            </div>
            
            <div class="tip-item">
              <div class="tip-icon">
                <i class="el-icon-edit"></i>
              </div>
              <div class="tip-text">
                <h4>详细描述问题</h4>
                <p>帮助咨询师更好地了解您的情况</p>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
import { addAppointment } from "@/api/mental/appointment";
import { getPsychologists, getLocations, getAvailableTimeSlots, getPsychologistInfo } from "@/api/mental/appointment";

export default {
  name: "UserAppointment",
  data() {
    return {
      form: {
        psychologistId: undefined,
        psychologistName: '',
        appointmentDate: '',
        timeSlot: '',
        urgencyLevel: '普通',
        problemDescription: '',
        locationId: undefined,
        locationName: ''
      },
      psychologists: [],
      selectedPsychologistInfo: null,
      locations: [],
      availableTimeSlots: [],
      psychologistLoading: false,
      timeSlotLoading: false,
      loading: false,
      timeSlotWarning: '',
      timeSlotMap: {
        'morning_1': { label: '09:00-10:00', hour: 9 },
        'morning_2': { label: '10:00-11:00', hour: 10 },
        'morning_3': { label: '11:00-12:00', hour: 11 },
        'afternoon_1': { label: '14:00-15:00', hour: 14 },
        'afternoon_2': { label: '15:00-16:00', hour: 15 },
        'afternoon_3': { label: '16:00-17:00', hour: 16 },
        'evening_1': { label: '18:00-19:00', hour: 18 },
        'evening_2': { label: '19:00-20:00', hour: 19 }
      },
      rules: {
        psychologistId: [
          { required: true, message: '请选择咨询师', trigger: 'change' }
        ],
        appointmentDate: [
          { required: true, message: '请选择预约日期', trigger: 'change' }
        ],
        timeSlot: [
          { required: true, message: '请选择时间段', trigger: 'change' },
          { validator: this.validateTimeSlotRule, trigger: 'change' }
        ],
        locationId: [
          { required: true, message: '请选择咨询地点', trigger: 'change' }
        ],
        problemDescription: [
          { max: 500, message: '问题描述不能超过500个字符', trigger: 'blur' }
        ]
      },
      datePickerOptions: {
        disabledDate(time) {
          const today = new Date();
          today.setHours(0, 0, 0, 0);
          return time.getTime() < today.getTime();
        }
      }
    };
  },
  computed: {
    groupedPsychologists() {
      const groups = {
        资深咨询师: [],
        专业咨询师: [],
        新手咨询师: []
      };
      
      this.psychologists.forEach(psychologist => {
        const experience = psychologist.experience || '';
        if (experience.includes('资深') || experience.includes('高级') || experience.includes('专家')) {
          groups.资深咨询师.push(psychologist);
        } else if (experience.includes('专业') || experience.includes('中级')) {
          groups.专业咨询师.push(psychologist);
        } else {
          groups.新手咨询师.push(psychologist);
        }
      });
      
      // 过滤空组
      return Object.entries(groups)
        .filter(([title, list]) => list.length > 0)
        .map(([title, list]) => ({
          title,
          list
        }));
    }
  },
  created() {
    this.loadPsychologists();
    this.loadLocations();
  },
  watch: {
    'form.locationId': function(newVal) {
      this.handleLocationChange(newVal);
    },
    'form.timeSlot': function(newVal) {
      this.validateTimeSlot(newVal);
    }
  },
  methods: {
    getExperienceTagType(experience) {
      if (experience.includes('资深') || experience.includes('高级')) {
        return 'danger';
      } else if (experience.includes('专业') || experience.includes('中级')) {
        return 'warning';
      } else {
        return 'info';
      }
    },
    
    validateTimeSlotRule(rule, value, callback) {
      if (!value) {
        callback(new Error('请选择时间段'));
        return;
      }
      
      if (this.form.appointmentDate) {
        const today = new Date().toISOString().split('T')[0];
        if (this.form.appointmentDate === today) {
          const slotInfo = this.timeSlotMap[value];
          if (slotInfo) {
            const now = new Date();
            const currentHour = now.getHours();
            
            if (slotInfo.hour <= currentHour) {
              callback(new Error('不能选择今天已经过去的时间段'));
              return;
            }
          }
        }
      }
      
      callback();
    },
    
    validateTimeSlot(value) {
      if (!value || !this.form.appointmentDate) {
        this.timeSlotWarning = '';
        return;
      }
      
      const today = new Date().toISOString().split('T')[0];
      if (this.form.appointmentDate === today) {
        const slotInfo = this.timeSlotMap[value];
        if (slotInfo) {
          const now = new Date();
          const currentHour = now.getHours();
          
          if (slotInfo.hour <= currentHour) {
            this.timeSlotWarning = '注意：您选择的是今天已经过去的时间段，建议选择其他时间段';
          } else {
            this.timeSlotWarning = '';
          }
        }
      } else {
        this.timeSlotWarning = '';
      }
    },
    
    async loadPsychologists() {
      this.psychologistLoading = true;
      try {
        const response = await getPsychologists();
        console.log('咨询师列表响应:', response);
        
        if (response.code === 200) {
          let psychologists = response.data || [];
          
          if (psychologists.length === 0) {
            this.$message.warning('暂无可预约的咨询师');
            this.psychologists = [];
          } else {
            this.psychologists = psychologists.map(psychologist => ({
              userId: psychologist.userId || psychologist.id,
              nickName: psychologist.nickName || psychologist.userName,
              avatar: psychologist.avatar,
              specialty: psychologist.specialty || '心理咨询',
              experience: psychologist.experience || '专业咨询师',
              consultationCount: psychologist.consultationCount || 0,
              description: psychologist.description || '专业心理咨询师',
              averageRating: psychologist.averageRating || 0,
              totalRatings: psychologist.totalRatings || 0
            }));
            
            console.log('处理后的咨询师列表:', this.psychologists);
            
            // 移除自动选择第一个咨询师的逻辑
            // 现在需要用户手动选择
          }
        } else {
          this.$message.error(response.msg || '加载咨询师列表失败');
          this.psychologists = [];
        }
      } catch (error) {
        console.error('加载咨询师失败:', error);
        this.$message.error('加载咨询师列表失败，请检查网络连接');
        this.psychologists = [];
      } finally {
        this.psychologistLoading = false;
      }
    },
    
    async loadPsychologistInfo(psychologistId) {
      try {
        const response = await getPsychologistInfo(psychologistId);
        if (response.code === 200) {
          const data = response.data || {};
          this.selectedPsychologistInfo = {
            userId: data.userId,
            nickName: data.nickName,
            avatar: data.avatar,
            specialty: data.specialty || '心理咨询',
            experience: data.experience || '专业咨询师',
            consultationCount: data.consultationCount || 0,
            description: data.description || '专业心理咨询师',
            averageRating: data.averageRating || 0,
            totalRatings: data.totalRatings || 0
          };
        }
      } catch (error) {
        console.error('加载咨询师详情失败:', error);
        const psychologist = this.psychologists.find(p => p.userId === psychologistId);
        if (psychologist) {
          this.selectedPsychologistInfo = {
            ...psychologist,
            averageRating: psychologist.averageRating || 0,
            totalRatings: psychologist.totalRatings || 0
          };
        }
      }
    },
    
    async loadLocations() {
      try {
        const response = await getLocations();
        let locations = response.data || response || [];
        
        if (locations.length === 0) {
          locations = [
            { id: 1, name: '心理咨询室A', address: '主楼301室' },
            { id: 2, name: '心理咨询室B', address: '主楼302室' },
            { id: 3, name: '心理咨询室C', address: '主楼303室' }
          ];
        }
        
        this.locations = locations;
        // 移除自动选择第一个地点的逻辑
        // this.form.locationId = this.locations[0].id;
        // this.form.locationName = this.locations[0].name;
      } catch (error) {
        console.error('加载地点失败:', error);
        this.locations = [
          { id: 1, name: '心理咨询室A', address: '主楼301室' },
          { id: 2, name: '心理咨询室B', address: '主楼302室' }
        ];
      }
    },
    
    handlePsychologistChange(psychologistId) {
      if (!psychologistId) {
        this.selectedPsychologistInfo = null;
        this.form.psychologistName = '';
        return;
      }
      
      const selectedPsychologist = this.psychologists.find(p => p.userId === psychologistId);
      if (selectedPsychologist) {
        this.form.psychologistName = selectedPsychologist.nickName;
        this.loadPsychologistInfo(psychologistId);
      }
      this.loadAvailableTimeSlots();
    },
    
    handleDateChange() {
      this.form.timeSlot = '';
      this.timeSlotWarning = '';
      this.loadAvailableTimeSlots();
    },
    
    handleLocationChange(locationId) {
      const selectedLocation = this.locations.find(l => l.id === locationId);
      if (selectedLocation) {
        this.form.locationName = selectedLocation.name;
      }
    },
    
    async loadAvailableTimeSlots() {
      if (!this.form.psychologistId || !this.form.appointmentDate) {
        this.availableTimeSlots = [];
        return;
      }
      
      console.log('查询可用时间段参数:', {
        psychologistId: this.form.psychologistId,
        date: this.form.appointmentDate
      });
      
      this.timeSlotLoading = true;
      try {
        const response = await getAvailableTimeSlots(this.form.psychologistId, this.form.appointmentDate);
        console.log('可用时间段响应:', response);
        
        if (response.code === 200) {
          let timeSlots = response.data || [];

          if (typeof timeSlots === 'string') {
            try {
              // 尝试解析 JSON 字符串
              timeSlots = JSON.parse(timeSlots);
            } catch (e) {
              // 如果解析失败，尝试按逗号分隔（兼容旧数据）
              timeSlots = timeSlots.split(',').filter(slot => slot.trim());
            }
          }

          this.availableTimeSlots = this.processTimeSlots(timeSlots);
          
          if (this.availableTimeSlots.length === 0) {
            this.$message.warning('该日期暂无可用时间段，请选择其他日期或联系咨询师');
          } else {
            const availableCount = this.availableTimeSlots.filter(slot => !slot.disabled).length;
            if (availableCount > 0) {
              this.$message.success(`查询到 ${availableCount} 个可用时间段`);
            }
          }
        } else {
          this.$message.error(response.msg || '获取可用时间段失败');
          this.availableTimeSlots = [];
        }
      } catch (error) {
        console.error('加载时间段失败:', error);
        this.$message.error('获取可用时间段失败，请稍后重试');
        this.availableTimeSlots = [];
      } finally {
        this.timeSlotLoading = false;
      }
    },
    
    processTimeSlots(timeSlots) {
      const today = new Date().toISOString().split('T')[0];
      const now = new Date();
      const currentHour = now.getHours();
      
      return timeSlots.map(slot => {
        const slotInfo = this.timeSlotMap[slot];
        if (!slotInfo) {
          return {
            value: slot,
            label: slot,
            disabled: false
          };
        }
        
        let disabled = false;
        if (this.form.appointmentDate === today) {
          disabled = slotInfo.hour <= currentHour;
        }
        
        return {
          value: slot,
          label: slotInfo.label + (disabled ? ' (已过时)' : ''),
          disabled: disabled
        };
      });
    },
    
    async submitForm() {
      if (this.form.appointmentDate && this.form.timeSlot) {
        const today = new Date().toISOString().split('T')[0];
        if (this.form.appointmentDate === today) {
          const slotInfo = this.timeSlotMap[this.form.timeSlot];
          if (slotInfo) {
            const now = new Date();
            const currentHour = now.getHours();
            
            if (slotInfo.hour <= currentHour) {
              this.$modal.msgError('不能选择今天已经过去的时间段，请重新选择时间段');
              return;
            }
          }
        }
      }
      
      this.$refs["form"].validate(async (valid) => {
        if (valid) {
          this.loading = true;
          
          try {
            const response = await addAppointment(this.form);
            if (response.code === 200) {
              this.$modal.msgSuccess("预约成功！请等待咨询师确认");
              this.resetForm();
            } else {
              this.$modal.msgError(response.msg || "预约失败");
            }
          } catch (error) {
            console.error('预约失败:', error);
            this.$modal.msgError(error.msg || "预约失败，请稍后重试");
          } finally {
            this.loading = false;
          }
        }
      });
    },
    
    resetForm() {
      this.$refs["form"].resetFields();
      this.form = {
        psychologistId: undefined, // 不再自动选择第一个
        psychologistName: '',
        appointmentDate: '',
        timeSlot: '',
        urgencyLevel: '普通',
        problemDescription: '',
        locationId: undefined, // 不再自动选择第一个
        locationName: ''
      };
      this.availableTimeSlots = [];
      this.timeSlotWarning = '';
      this.selectedPsychologistInfo = null; // 清空咨询师信息
    },
    
    goToMyAppointments() {
      this.$router.push('/consultation/appointment/my');
    }
  }
};
</script>

<style scoped lang="scss">
.app-user-appointment {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
  
  h2 {
    margin: 0 0 8px 0;
    font-size: 24px;
    font-weight: 600;
    color: #2d3748;
  }
  
  .subtitle {
    margin: 0;
    color: #718096;
    font-size: 14px;
  }
}

.content-container {
  display: flex;
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
  
  @media (max-width: 992px) {
    flex-direction: column;
  }
}

.form-card {
  flex: 2;
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(31, 45, 61, 0.1);
  
  .card-header {
    border-bottom: 1px solid #eaeaea;
    padding: 16px 20px;
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: #2d3748;
      display: flex;
      align-items: center;
      
      i {
        margin-right: 8px;
        color: #409eff;
      }
    }
  }
  
  .el-form {
    padding: 20px;
  }
}

.form-item {
  margin-bottom: 20px;
}

.psychologist-option {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.option-avatar {
  margin-right: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: bold;
}

.option-info {
  flex: 1;
  min-width: 0;
}

.option-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
  
  .option-name {
    font-weight: 600;
    font-size: 14px;
    color: #2d3748;
  }
}

.option-details {
  .option-rating {
    display: flex;
    align-items: center;
    margin-bottom: 2px;
    
    .rating-stars {
      margin-right: 6px;
    }
    
    .rating-score {
      color: #ff9900;
      font-weight: bold;
      font-size: 12px;
      margin-right: 4px;
    }
    
    .rating-count {
      color: #718096;
      font-size: 11px;
    }
    
    .no-rating {
      color: #a0aec0;
      font-size: 11px;
    }
  }
  
  .option-specialty {
    color: #718096;
    font-size: 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.location-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  
  .location-name {
    font-size: 14px;
  }
  
  .location-address {
    color: #8492a6;
    font-size: 12px;
    max-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.time-slot-warning {
  color: #e6a23c;
  font-size: 12px;
  margin-top: 4px;
  display: flex;
  align-items: center;
  
  i {
    margin-right: 4px;
    font-size: 14px;
  }
}

.textarea-tips {
  color: #718096;
  font-size: 12px;
  margin-top: 4px;
  display: flex;
  align-items: center;
  
  i {
    margin-right: 4px;
    color: #409eff;
  }
}

.form-actions {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #eaeaea;
  
  .button-group {
    display: flex;
    gap: 12px;
    align-items: center;
  }
  
  .submit-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    border-radius: 8px;
    padding: 10px 24px;
    font-weight: 500;
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    }
  }
  
  .reset-btn {
    background: #f7fafc;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 10px 20px;
    color: #718096;
    
    &:hover {
      background: #edf2f7;
      border-color: #cbd5e0;
    }
  }
  
  .view-btn {
    color: #667eea;
    
    &:hover {
      color: #764ba2;
    }
  }
}

.right-panel {
  flex: 1;
  min-width: 300px;
}

.info-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 12px rgba(31, 45, 61, 0.1);
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  
  .card-header {
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    padding: 16px 20px;
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: white;
      display: flex;
      align-items: center;
      
      i {
        margin-right: 8px;
      }
    }
  }
  
  &.tip-card {
    background: white;
    color: #2d3748;
    
    .card-header {
      border-bottom: 1px solid #eaeaea;
      
      h3 {
        color: #2d3748;
      }
    }
  }
}

.psychologist-info {
  padding: 20px;
}

.info-avatar {
  position: relative;
  text-align: center;
  margin-bottom: 16px;
  
  .avatar {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    font-weight: bold;
    font-size: 24px;
    border: 3px solid white;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  }
  
  .avatar-badge {
    position: absolute;
    bottom: 0;
    right: calc(50% - 40px);
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: #ff9900;
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid white;
    font-size: 12px;
  }
}

.info-content {
  .psychologist-name {
    margin: 0 0 4px 0;
    font-size: 20px;
    font-weight: 600;
    text-align: center;
  }
  
  .psychologist-title {
    margin: 0 0 16px 0;
    text-align: center;
    color: rgba(255, 255, 255, 0.8);
    font-size: 14px;
  }
}

.rating-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 20px;
  
  .rating-stars {
    :deep(.el-rate__icon) {
      font-size: 16px;
    }
  }
  
  .rating-details {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .rating-score {
      font-size: 20px;
      font-weight: bold;
      color: #ff9900;
    }
    
    .rating-count {
      font-size: 12px;
      color: rgba(255, 255, 255, 0.8);
      margin-top: 2px;
    }
  }
}

.no-rating {
  text-align: center;
  margin-bottom: 20px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

.detail-info {
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  
  &:last-child {
    border-bottom: none;
  }
  
  .detail-icon {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.1);
    display: flex;
    align-items: center;
    justify-content: center;
    
    i {
      font-size: 16px;
      color: white;
    }
  }
  
  .detail-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    
    .detail-label {
      font-size: 12px;
      color: rgba(255, 255, 255, 0.8);
      margin-bottom: 2px;
    }
    
    .detail-value {
      font-size: 14px;
      font-weight: 500;
    }
  }
}

.description {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 12px;
  margin-top: 16px;
  
  .description-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    font-size: 14px;
    font-weight: 500;
    
    i {
      font-size: 16px;
    }
  }
  
  .description-content {
    margin: 0;
    font-size: 13px;
    line-height: 1.5;
    color: rgba(255, 255, 255, 0.9);
    max-height: 80px;
    overflow: auto;
  }
}

.card-footer {
  padding: 16px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  
  i {
    color: #ff9900;
    font-size: 16px;
  }
}

.tip-content {
  padding: 20px;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  
  &:not(:last-child) {
    border-bottom: 1px solid #eaeaea;
  }
  
  .tip-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    
    i {
      font-size: 18px;
      color: #667eea;
    }
  }
  
  .tip-text {
    flex: 1;
    
    h4 {
      margin: 0 0 4px 0;
      font-size: 14px;
      font-weight: 600;
      color: #2d3748;
    }
    
    p {
      margin: 0;
      color: #718096;
      font-size: 13px;
    }
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .app-user-appointment {
    padding: 16px;
  }
  
  .content-container {
    gap: 16px;
  }
  
  .form-card,
  .info-card {
    .card-header {
      padding: 14px 16px;
    }
    
    .el-form,
    .psychologist-info,
    .tip-content {
      padding: 16px;
    }
  }
  
  .form-actions {
    .button-group {
      flex-direction: column;
      align-items: stretch;
    }
    
    .submit-btn,
    .reset-btn {
      width: 100%;
    }
  }
}
</style>