<template>
  <div class="app-container">
    <div class="management-container">
      <!-- 头部区域 -->
      <div class="header-card">
        <div class="header-content">
          <div class="header-left">
            <div class="title-section">
              <i class="el-icon-files title-icon"></i>
              <h2 class="main-title">心理档案管理</h2>
            </div>
            <p class="header-description">管理用户心理档案，查看心理健康状态</p>
          </div>
          <div class="header-right">
            <el-button 
              type="primary" 
              @click="refreshList"
              :loading="loading"
              size="small"
              icon="el-icon-refresh"
              class="refresh-btn">
              刷新
            </el-button>
          </div>
        </div>
      </div>

      <!-- 搜索栏 -->
      <el-card shadow="never" class="search-card">
        <el-form :inline="true" :model="searchForm" class="search-form" size="small">
          <el-form-item label="用户姓名" class="search-item">
            <el-input 
              v-model="searchForm.realName" 
              placeholder="请输入用户姓名"
              clearable
              prefix-icon="el-icon-user"
              class="search-input">
            </el-input>
          </el-form-item>
          
          <el-form-item label="档案状态" class="search-item">
            <el-select 
              v-model="searchForm.hasProfile" 
              placeholder="请选择"
              clearable
              class="search-select">
              <el-option label="全部" :value="null" />
              <el-option label="已填写" :value="true" />
              <el-option label="未填写" :value="false" />
            </el-select>
          </el-form-item>
          
          <el-form-item class="search-item">
            <el-button 
              type="primary" 
              @click="handleSearch"
              icon="el-icon-search"
              class="search-btn">
              查询
            </el-button>
            <el-button 
              @click="resetSearch"
              icon="el-icon-refresh-left"
              class="reset-btn">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 数据卡片 -->
      <div class="stats-cards">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="stats-icon total-icon">
              <i class="el-icon-document"></i>
            </div>
            <div class="stats-info">
              <div class="stats-label">档案总数</div>
              <div class="stats-value">{{ total }}</div>
            </div>
          </div>
        </el-card>
        
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="stats-icon visible-icon">
              <i class="el-icon-view"></i>
            </div>
            <div class="stats-info">
              <div class="stats-label">可见档案</div>
              <div class="stats-value">{{ visibleCount }}</div>
            </div>
          </div>
        </el-card>
        
        <el-card shadow="hover" class="stats-card">
          <div class="stats-content">
            <div class="stats-icon stress-icon">
              <i class="el-icon-warning"></i>
            </div>
            <div class="stats-info">
              <div class="stats-label">高压力用户</div>
              <div class="stats-value">{{ highStressCount }}</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 档案列表 -->
      <el-card shadow="never" class="list-card">
        <el-table 
          v-loading="loading"
          :data="profileList"
          style="width: 100%"
          row-key="id"
          size="small"
          class="profile-table"
          @sort-change="handleSortChange">
          
          <el-table-column label="用户信息" min-width="180" sortable prop="realName">
            <template slot-scope="scope">
              <div class="user-info">
                <div class="user-avatar">
                  <i class="el-icon-user avatar-icon"></i>
                </div>
                <div class="user-details">
                  <div class="user-name">{{ scope.row.realName || '匿名用户' }}</div>
                  <div class="user-tags">
                    <el-tag size="mini" type="info" class="gender-tag">
                      {{ getGenderName(scope.row.gender) }}
                    </el-tag>
                    <el-tag size="mini" class="age-tag" v-if="scope.row.age">
                      {{ scope.row.age }}岁
                    </el-tag>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="职业" min-width="120" prop="occupation">
            <template slot-scope="scope">
              <div class="occupation-cell">
                <i class="el-icon-office-building cell-icon"></i>
                <span>{{ scope.row.occupation || '未填写' }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="情绪状态" width="140">
            <template slot-scope="scope">
              <div class="emotion-cell">
                <div class="emotion-rate">
                  <el-rate
                    v-model="scope.row.emotionalState"
                    disabled
                    :max="5"
                    :colors="['#F56C6C', '#F56C6C', '#E6A23C', '#67C23A', '#67C23A']"
                    :allow-half="true"
                    size="small">
                  </el-rate>
                </div>
                <div class="emotion-label">
                  {{ getEmotionLabel(scope.row.emotionalState) }}
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="压力水平" width="160">
            <template slot-scope="scope">
              <div class="stress-cell">
                <div class="stress-info">
                  <div class="stress-level">
                    <span class="level-value">{{ scope.row.stressLevel || 0 }}</span>
                    <span class="level-unit">/100</span>
                  </div>
                  <div class="stress-indicator">
                    <div 
                      class="indicator-bar" 
                      :style="{width: (scope.row.stressLevel || 0) + '%', background: getStressColor(scope.row.stressLevel)}">
                    </div>
                  </div>
                </div>
                <div class="stress-tag">
                  <el-tag 
                    :type="getStressTagType(scope.row.stressLevel)"
                    size="mini"
                    effect="plain">
                    {{ getStressLevelText(scope.row.stressLevel) }}
                  </el-tag>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="睡眠质量" width="120">
            <template slot-scope="scope">
              <div class="sleep-cell">
                <div class="sleep-rate">
                  <el-rate
                    v-model="scope.row.sleepQuality"
                    disabled
                    :max="5"
                    :colors="['#F56C6C', '#F56C6C', '#E6A23C', '#409EFF', '#409EFF']"
                    size="small">
                  </el-rate>
                </div>
                <div class="sleep-label">
                  {{ getSleepLabel(scope.row.sleepQuality) }}
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="创建时间" width="150" sortable prop="createTime">
            <template slot-scope="scope">
              <div class="time-cell">
                <i class="el-icon-time cell-icon"></i>
                <span>{{ formatDate(scope.row.createTime) }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="可见性" width="90">
            <template slot-scope="scope">
              <div class="visibility-cell">
                <el-tag 
                  :type="getVisibilityType(scope.row.isVisibleToConsultant)"
                  size="mini"
                  effect="light"
                  class="visibility-tag">
                  {{ getVisibilityText(scope.row.isVisibleToConsultant) }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="100" fixed="right" align="center">
            <template slot-scope="scope">
              <el-button 
                type="text" 
                size="mini"
                @click="viewProfile(scope.row)"
                :disabled="!scope.row.id"
                icon="el-icon-view"
                class="view-btn">
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-show="total > 0"
          :current-page="searchForm.pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="searchForm.pageSize"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          size="small">
        </el-pagination>
      </div>
    </div>
  </div>
</template>

<script>
import { getConsultantProfileList } from "@/api/mental/profile";

export default {
  name: "ConsultantProfileList",
  data() {
    return {
      searchForm: {
        realName: '',
        hasProfile: null,
        pageNum: 1,
        pageSize: 10,
        orderByColumn: 'createTime',
        isAsc: 'desc'
      },
      
      profileList: [],
      total: 0,
      loading: false,
      visibleCount: 0,
      highStressCount: 0
    };
  },
  created() {
    this.getList();
  },
  methods: {
    async getList() {
      this.loading = true;
      try {
        const res = await getConsultantProfileList(this.searchForm);
        if (res.code === 200) {
          this.profileList = res.rows || [];
          this.total = res.total || 0;
          
          // 计算统计信息
          this.calculateStats();
        } else {
          this.$modal.msgError(res.msg || '获取列表失败');
        }
      } catch (error) {
        console.error('获取列表失败:', error);
        
        if (error.response && error.response.status === 403) {
          this.$modal.msgError('权限不足，请联系管理员分配咨询师角色');
        } else {
          this.$modal.msgError('获取列表失败');
        }
      } finally {
        this.loading = false;
      }
    },
    
    calculateStats() {
      // 计算可见档案数量
      this.visibleCount = this.profileList.filter(item => 
        item.isVisibleToConsultant === 1 || item.isVisibleToConsultant === true
      ).length;
      
      // 计算高压力用户数量（压力水平 >= 60）
      this.highStressCount = this.profileList.filter(item => 
        item.stressLevel && item.stressLevel >= 60
      ).length;
    },
    
    handleSearch() {
      this.searchForm.pageNum = 1;
      this.getList();
    },
    
    resetSearch() {
      this.searchForm = {
        realName: '',
        hasProfile: null,
        pageNum: 1,
        pageSize: 10,
        orderByColumn: 'createTime',
        isAsc: 'desc'
      };
      this.getList();
    },
    
    refreshList() {
      this.getList();
    },
    
    handleSizeChange(val) {
      this.searchForm.pageSize = val;
      this.getList();
    },
    
    handleCurrentChange(val) {
      this.searchForm.pageNum = val;
      this.getList();
    },
    
    handleSortChange({ column, prop, order }) {
      if (prop) {
        this.searchForm.orderByColumn = prop;
        this.searchForm.isAsc = order === 'ascending' ? 'asc' : 'desc';
        this.getList();
      }
    },
    
    viewProfile(row) {
      if (!row.id) {
        this.$modal.msgWarning('该用户尚未填写心理档案');
        return;
      }
      
      this.$router.push({
        name: 'ConsultantProfileView',
        query: { userId: row.userId }
      });
    },
    
    getGenderName(gender) {
      if (gender === 0 || gender === '0') return '男';
      if (gender === 1 || gender === '1') return '女';
      if (gender === 2 || gender === '2') return '其他';
      return '未填写';
    },
    
    getEmotionLabel(level) {
      const labels = ['非常差', '较差', '一般', '良好', '非常好'];
      return (level >= 1 && level <= 5) ? labels[level - 1] : '未评估';
    },
    
    getSleepLabel(level) {
      const labels = ['非常差', '较差', '一般', '良好', '非常好'];
      return (level >= 1 && level <= 5) ? labels[level - 1] : '未评估';
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
      if (level < 30) return '轻度';
      if (level < 60) return '中度';
      if (level < 80) return '重度';
      return '极高';
    },
    
    getVisibilityType(visible) {
      if (visible === null || visible === undefined) return 'info';
      return visible === 1 || visible === true ? 'success' : 'info';
    },
    
    getVisibilityText(visible) {
      if (visible === null || visible === undefined) return '未知';
      return visible === 1 || visible === true ? '可见' : '隐藏';
    },
    
    formatDate(date) {
      if (!date) return '';
      
      try {
        const dateObj = new Date(date);
        if (isNaN(dateObj.getTime())) return '';
        
        const now = new Date();
        const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
        const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000);
        
        if (dateObj >= today) {
          const hours = String(dateObj.getHours()).padStart(2, '0');
          const minutes = String(dateObj.getMinutes()).padStart(2, '0');
          return `今天 ${hours}:${minutes}`;
        } else if (dateObj >= yesterday) {
          const hours = String(dateObj.getHours()).padStart(2, '0');
          const minutes = String(dateObj.getMinutes()).padStart(2, '0');
          return `昨天 ${hours}:${minutes}`;
        } else {
          const month = String(dateObj.getMonth() + 1).padStart(2, '0');
          const day = String(dateObj.getDate()).padStart(2, '0');
          return `${month}-${day}`;
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

.management-container {
  max-width: 1400px;
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

.header-right {
  display: flex;
  align-items: center;
}

.refresh-btn {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 6px;
  font-weight: 500;
}

.refresh-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}

/* 搜索栏 */
.search-card {
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.search-item {
  margin-bottom: 0 !important;
}

.search-input {
  width: 180px;
}

.search-select {
  width: 120px;
}

.search-btn,
.reset-btn {
  padding: 8px 16px;
}

/* 数据卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.stats-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
  transition: all 0.3s ease;
  cursor: pointer;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stats-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stats-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.total-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.visible-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
}

.stress-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
}

.stats-info {
  flex: 1;
}

.stats-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.stats-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

/* 列表卡片 */
.list-card {
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
}

.profile-table {
  border-radius: 6px;
  overflow: hidden;
}

/* 用户信息列 */
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-icon {
  font-size: 16px;
  color: white;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.user-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.gender-tag,
.age-tag {
  border: none;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 10px;
}

.age-tag {
  background: #f0f2f5;
  color: #606266;
}

/* 职业列 */
.occupation-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.cell-icon {
  font-size: 14px;
  color: #667eea;
  opacity: 0.8;
}

/* 情绪状态列 */
.emotion-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.emotion-rate {
  line-height: 1;
}

.emotion-label {
  font-size: 12px;
  color: #909399;
  text-align: center;
}

/* 压力水平列 */
.stress-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stress-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stress-level {
  display: flex;
  align-items: baseline;
  gap: 2px;
  min-width: 40px;
}

.level-value {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.level-unit {
  font-size: 12px;
  color: #909399;
}

.stress-indicator {
  flex: 1;
  height: 4px;
  background: #f0f2f5;
  border-radius: 2px;
  overflow: hidden;
}

.indicator-bar {
  height: 100%;
  border-radius: 2px;
  transition: all 0.3s ease;
}

.stress-tag {
  text-align: center;
}

/* 睡眠质量列 */
.sleep-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sleep-rate {
  line-height: 1;
}

.sleep-label {
  font-size: 12px;
  color: #909399;
  text-align: center;
}

/* 时间列 */
.time-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 12px;
}

/* 可见性列 */
.visibility-cell {
  text-align: center;
}

.visibility-tag {
  border: none;
  font-weight: 500;
}

/* 操作列 */
.view-btn {
  padding: 4px 8px;
  color: #667eea;
  font-size: 12px;
}

.view-btn:hover {
  background: rgba(102, 126, 234, 0.1);
}

.view-btn.is-disabled {
  color: #c0c4cc;
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 12px 0;
}

:deep(.el-pagination__total) {
  font-size: 12px;
}

:deep(.el-pagination__sizes .el-input__inner),
:deep(.el-pagination__jump .el-input__inner) {
  font-size: 12px;
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
    justify-content: flex-start;
  }
  
  .search-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-item {
    width: 100%;
  }
  
  .search-input,
  .search-select {
    width: 100%;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .stats-card {
    margin-bottom: 0;
  }
  
  .user-info {
    align-items: flex-start;
  }
  
  .user-details {
    min-width: 0;
  }
  
  .profile-table {
    font-size: 12px;
  }
  
  .el-table__row .cell {
    padding: 8px 4px;
  }
}

@media (max-width: 480px) {
  .main-title {
    font-size: 16px;
  }
  
  .header-description {
    font-size: 12px;
  }
  
  .stats-value {
    font-size: 20px;
  }
  
  .pagination-container {
    justify-content: center;
  }
  
  :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>