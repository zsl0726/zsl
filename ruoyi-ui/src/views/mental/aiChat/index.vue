<template>
  <div class="app-container">
    <!-- 移动端会话列表开关 -->
    <div class="mobile-session-toggle" v-if="isMobile && !showChat">
      <el-button type="primary" @click="toggleChatView" icon="el-icon-menu">
        会话列表
      </el-button>
    </div>

    <el-row :gutter="0" class="chat-layout">
      <!-- 左侧会话列表 -->
      <el-col 
        :xs="24" :sm="24" :md="8" :lg="6" :xl="5" 
        class="session-sidebar" 
        v-show="!isMobile || !showChat"
      >
        <div class="session-header">
          <div class="user-info">
            <div class="user-avatar">
              <i class="el-icon-user-solid"></i>
            </div>
            <div class="user-name">
              <h3>我的对话</h3>
            </div>
          </div>
          <el-button 
            type="primary" 
            class="new-session-btn"
            @click="startNewSession" 
            icon="el-icon-plus"
            circle
            size="small"
          ></el-button>
        </div>
        
        <div class="search-box">
          <el-input
            placeholder="搜索会话..."
            prefix-icon="el-icon-search"
            size="small"
            v-model="searchKeyword"
            @input="filterSessions"
          ></el-input>
        </div>
        
        <div class="session-list-wrapper">
          <div class="session-list" ref="sessionList">
            <div v-if="filteredSessions.length === 0" class="empty-session">
              <div class="empty-content">
                <i class="el-icon-chat-line-square empty-icon"></i>
                <p class="empty-text">暂无会话记录</p>
                <el-button 
                  type="text" 
                  @click="startNewSession"
                  class="start-chat-btn"
                >
                  开始新的对话
                </el-button>
              </div>
            </div>
            <div 
              v-for="session in filteredSessions" 
              :key="session.sessionId"
              :class="['session-item', { active: currentSessionId === session.sessionId }]"
              @click="selectSession(session)"
            >
              <div class="session-avatar">
                <div class="avatar-content">
                  <i class="el-icon-chat-dot-round"></i>
                </div>
                <div class="unread-badge" v-if="session.unreadCount">
                  {{ session.unreadCount }}
                </div>
              </div>
              <div class="session-info">
                <div class="session-header-line">
                  <span class="session-title">{{ session.title || '新对话' }}</span>
                  <span class="session-time">{{ formatTime(session.lastTime) }}</span>
                </div>
                <div class="session-preview">
                  {{ session.lastMessage || '暂无消息' }}
                </div>
              </div>
              <el-dropdown 
                @command="handleSessionCommand(session, $event)" 
                trigger="click"
                class="session-dropdown"
              >
                <span class="el-dropdown-link">
                  <i class="el-icon-more"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="rename">
                    <i class="el-icon-edit"></i>重命名
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" class="delete-item">
                    <i class="el-icon-delete"></i>删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧聊天区域 -->
      <el-col 
        :xs="24" :sm="24" :md="16" :lg="18" :xl="19" 
        class="chat-main" 
        v-show="!isMobile || showChat"
      >
        <div class="chat-container">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <div class="chat-header-left">
              <el-button 
                v-if="isMobile" 
                type="text" 
                @click="toggleChatView"
                class="back-btn"
                icon="el-icon-arrow-left"
              ></el-button>
              <div class="chat-user">
                <div class="chat-user-avatar">
                  <i class="el-icon-robot"></i>
                </div>
                <div class="chat-user-info">
                  <h3 class="chat-username">AI心理助手</h3>
                  <p class="chat-user-status">在线</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 聊天消息区域 -->
          <div class="chat-messages-wrapper" ref="messagesWrapper">
            <div class="chat-messages" ref="messagesContainer">
              <!-- 欢迎消息 -->
              <div v-if="messages.length === 0" class="welcome-message">
                <div class="welcome-content">
                  <div class="welcome-avatar">
                    <i class="el-icon-robot"></i>
                  </div>
                  <h3 class="welcome-title">AI心理助手</h3>
                  <p class="welcome-subtitle">我在这里倾听您的心声</p>
                  <div class="welcome-features">
                    <div class="feature-item">
                      <i class="el-icon-chat-line-round"></i>
                      <span>情绪疏导</span>
                    </div>
                    <div class="feature-item">
                      <i class="el-icon-moon"></i>
                      <span>压力管理</span>
                    </div>
                    <div class="feature-item">
                      <i class="el-icon-reading"></i>
                      <span>心理知识</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 消息列表 -->
              <div 
                v-for="(message, index) in messages" 
                :key="index"
                :class="['message-item', message.type]"
              >
                <!-- 显示日期分隔线 -->
                <div 
                  class="date-divider" 
                  v-if="shouldShowDateDivider(message, index)"
                >
                  <span>{{ formatDate(message.time) }}</span>
                </div>

                <!-- AI消息 -->
                <div v-if="message.type === 'ai'" class="message-ai">
                  <div class="message-avatar">
                    <div class="avatar-content">
                      <i class="el-icon-robot"></i>
                    </div>
                  </div>
                  <div class="message-content-wrapper">
                    <div class="message-bubble ai-bubble">
                      <div class="message-text" v-html="formatMessage(message.content)"></div>
                      <div class="message-time">{{ formatMessageTime(message.time) }}</div>
                    </div>
                    <div class="message-actions">
                      <el-button 
                        type="text" 
                        size="mini" 
                        class="copy-btn"
                        @click="copyMessage(message.content)"
                      >
                        <i class="el-icon-copy-document"></i> 复制
                      </el-button>
                    </div>
                  </div>
                </div>

                <!-- 用户消息 -->
                <div v-else class="message-user">
                  <div class="message-content-wrapper">
                    <div class="message-bubble user-bubble">
                      <div class="message-text">{{ message.content }}</div>
                      <div class="message-time">{{ formatMessageTime(message.time) }}</div>
                      <div class="message-status">
                        <i class="el-icon-check" v-if="message.status === 'sent'"></i>
                        <i class="el-icon-loading" v-if="message.status === 'sending'"></i>
                        <i class="el-icon-warning" v-if="message.status === 'failed'"></i>
                      </div>
                    </div>
                  </div>
                  <div class="message-avatar">
                    <div class="avatar-content">
                      <i class="el-icon-user-solid"></i>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 加载指示器 -->
              <div v-if="loading" class="loading-indicator">
                <div class="message-ai">
                  <div class="message-avatar">
                    <div class="avatar-content">
                      <i class="el-icon-robot"></i>
                    </div>
                  </div>
                  <div class="typing-indicator">
                    <div class="typing-dots">
                      <span></span>
                      <span></span>
                      <span></span>
                    </div>
                    <p class="typing-text">正在输入...</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 聊天输入区域 -->
          <div class="chat-input-area">
            <div class="input-main">
              <el-input
                ref="messageInput"
                v-model="inputMessage"
                type="textarea"
                :rows="1"
                :autosize="{ minRows: 1, maxRows: 4 }"
                placeholder="请输入消息..."
                @keydown.native="handleKeydown"
                resize="none"
                class="message-input"
                @focus="scrollToBottom"
              ></el-input>
              <el-button 
                type="success" 
                :loading="loading"
                @click="sendMessage"
                :disabled="!inputMessage.trim()"
                class="send-button"
              >
                发送
              </el-button>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { startNewConversation, sendMessage, getMySessions, getConversationHistory, deleteMySession } from "@/api/mental/aiConversation";

export default {
  name: "AiChat",
  data() {
    return {
      sessions: [],
      filteredSessions: [],
      currentSessionId: null,
      currentSessionTitle: '',
      messages: [],
      inputMessage: '',
      loading: false,
      autoScroll: true,
      searchKeyword: '',
      showChat: true,
      isMobile: false,
    };
  },
  created() {
    this.checkMobile();
    this.loadSessions();
    window.addEventListener('resize', this.handleResize);
  },
  mounted() {
    this.initScrollListener();
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
  },
  watch: {
    messages() {
      this.$nextTick(() => {
        if (this.autoScroll) {
          this.scrollToBottom();
        }
      });
    },
    showChat(newVal) {
      if (newVal) {
        this.$nextTick(() => {
          this.scrollToBottom();
        });
      }
    }
  },
  methods: {
    checkMobile() {
      this.isMobile = window.innerWidth < 768;
    },
    
    handleResize() {
      this.checkMobile();
      this.$nextTick(() => {
        this.scrollToBottom();
      });
    },

    toggleChatView() {
      this.showChat = !this.showChat;
    },

    handleKeydown(event) {
      if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        this.sendMessage();
      }
    },

    initScrollListener() {
      const messagesContainer = this.$refs.messagesContainer;
      if (messagesContainer) {
        messagesContainer.addEventListener('scroll', () => {
          const container = messagesContainer;
          const isAtBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 50;
          this.autoScroll = isAtBottom;
        });
      }
    },

    async loadSessions() {
      try {
        const response = await getMySessions();
        this.sessions = response.data || [];
        this.filteredSessions = [...this.sessions];
        if (this.sessions.length > 0) {
          this.selectSession(this.sessions[0]);
          if (this.isMobile) {
            this.showChat = true;
          }
        }
      } catch (error) {
        console.error('加载会话列表失败:', error);
      }
    },

    filterSessions() {
      if (!this.searchKeyword.trim()) {
        this.filteredSessions = [...this.sessions];
        return;
      }
      
      const keyword = this.searchKeyword.toLowerCase();
      this.filteredSessions = this.sessions.filter(session => 
        session.title.toLowerCase().includes(keyword) ||
        (session.lastMessage && session.lastMessage.toLowerCase().includes(keyword))
      );
    },

    async startNewSession() {
      try {
        const response = await startNewConversation();
        if (response.code === 200) {
          const newSession = {
            sessionId: response.data.sessionId,
            title: '新对话',
            lastTime: new Date(),
            lastMessage: '',
            unreadCount: 0
          };
          this.sessions.unshift(newSession);
          this.filteredSessions = [...this.sessions];
          this.selectSession(newSession);
          this.messages = [];
          if (this.isMobile) {
            this.showChat = true;
          }
          this.focusInput();
        }
      } catch (error) {
        console.error('创建新会话失败:', error);
      }
    },

    async selectSession(session) {
      this.currentSessionId = session.sessionId;
      this.currentSessionTitle = session.title;
      this.loadMessages();
      this.autoScroll = true;
      this.$nextTick(() => {
        this.scrollToBottom();
        this.focusInput();
      });
    },

    async loadMessages() {
      if (!this.currentSessionId) return;
      
      try {
        const response = await getConversationHistory(this.currentSessionId);
        this.messages = response.data || [];
        this.scrollToBottom();
      } catch (error) {
        console.error('加载消息历史失败:', error);
      }
    },

    async sendMessage() {
      const message = this.inputMessage.trim();
      if (!message || !this.currentSessionId || this.loading) return;

      // 添加用户消息（临时状态）
      const userMessage = {
        type: 'user',
        content: message,
        time: new Date(),
        status: 'sending'
      };
      this.messages.push(userMessage);
      
      const messageIndex = this.messages.length - 1;
      this.inputMessage = '';
      this.scrollToBottom();

      this.loading = true;
      try {
        const response = await sendMessage({
          sessionId: this.currentSessionId,
          content: message
        });

        if (response.code === 200) {
          // 更新用户消息状态
          this.messages[messageIndex].status = 'sent';
          
          // 添加AI回复
          const aiMessage = {
            type: 'ai',
            content: response.data.content,
            time: new Date(),
            tokenUsage: response.data.tokenUsage
          };
          this.messages.push(aiMessage);
          this.scrollToBottom();
          
          // 更新会话列表中的最后消息
          const session = this.sessions.find(s => s.sessionId === this.currentSessionId);
          if (session) {
            session.lastMessage = message.length > 20 ? message.substring(0, 20) + '...' : message;
            session.lastTime = new Date();
            this.filterSessions();
          }
        }
      } catch (error) {
        console.error('发送消息失败:', error);
        this.messages[messageIndex].status = 'failed';
        this.$message.error('消息发送失败，请重试');
      } finally {
        this.loading = false;
        this.focusInput();
      }
    },

    // 处理会话操作
    handleSessionCommand(session, command) {
      if (command === 'delete') {
        this.deleteSession(session);
      } else if (command === 'rename') {
        this.renameSession(session);
      }
    },

    // 删除会话
    async deleteSession(session) {
      try {
        await this.$confirm('确定要删除此会话吗？删除后将无法恢复。', '提示', {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
          confirmButtonClass: 'el-button--danger'
        });
        
        await deleteMySession(session.sessionId);
        const index = this.sessions.findIndex(s => s.sessionId === session.sessionId);
        if (index > -1) {
          this.sessions.splice(index, 1);
          this.filterSessions();
          if (this.currentSessionId === session.sessionId) {
            if (this.sessions.length > 0) {
              this.selectSession(this.sessions[0]);
            } else {
              this.currentSessionId = null;
              this.currentSessionTitle = '';
              this.messages = [];
            }
          }
        }
        this.$message.success('删除成功');
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除会话失败:', error);
          this.$message.error('删除失败: ' + (error.message || '未知错误'));
        }
      }
    },

    // 重命名会话
    renameSession(session) {
      this.$prompt('请输入新的会话名称', '重命名', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: session.title,
        inputPattern: /.+/,
        inputErrorMessage: '名称不能为空'
      }).then(({ value }) => {
        session.title = value;
        if (session.sessionId === this.currentSessionId) {
          this.currentSessionTitle = value;
        }
        this.$message.success('重命名成功');
      });
    },

    focusInput() {
      this.$nextTick(() => {
        const input = this.$refs.messageInput;
        if (input && input.focus) {
          input.focus();
        }
      });
    },

    copyMessage(content) {
      navigator.clipboard.writeText(content.replace(/<[^>]*>/g, '')).then(() => {
        this.$message.success('已复制到剪贴板');
      });
    },

    formatMessage(content) {
      return content.replace(/\n/g, '<br>');
    },

    formatTime(time) {
      if (!time) return '';
      const date = new Date(time);
      const now = new Date();
      const diff = now - date;
      const diffMinutes = Math.floor(diff / (1000 * 60));
      const diffHours = Math.floor(diff / (1000 * 60 * 60));
      const diffDays = Math.floor(diff / (1000 * 60 * 60 * 24));

      if (diffMinutes < 1) return '刚刚';
      if (diffMinutes < 60) return `${diffMinutes}分钟前`;
      if (diffHours < 24) return `${diffHours}小时前`;
      if (diffDays < 1) return '今天';
      if (diffDays < 2) return '昨天';
      if (diffDays < 7) return `${diffDays}天前`;
      return date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' });
    },

    formatDate(time) {
      if (!time) return '';
      const date = new Date(time);
      const now = new Date();
      const diffDays = Math.floor((now - date) / (1000 * 60 * 60 * 24));
      
      if (diffDays < 1) return '今天';
      if (diffDays < 2) return '昨天';
      if (diffDays < 7) {
        const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
        return weekdays[date.getDay()];
      }
      return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' });
    },

    formatMessageTime(time) {
      if (!time) return '';
      const date = new Date(time);
      return date.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit',
        hour12: false
      });
    },

    shouldShowDateDivider(message, index) {
      if (index === 0) return false;
      const currentDate = new Date(message.time).toDateString();
      const prevDate = new Date(this.messages[index - 1].time).toDateString();
      return currentDate !== prevDate;
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer;
        if (container && this.autoScroll) {
          container.scrollTop = container.scrollHeight;
        }
      });
    }
  }
};
</script>

<style scoped lang="scss">
.app-container {
  height: calc(100vh - 84px);
  background: #f0f0f0;
  overflow: hidden;
  padding: 0;
}

.mobile-session-toggle {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 1000;
  display: none;
  
  @media (max-width: 768px) {
    display: block;
  }
}

.chat-layout {
  height: 100%;
  margin: 0 !important;
}

/* ===== 左侧会话列表样式 ===== */
.session-sidebar {
  height: 100%;
  background: white;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e6e6e6;
  
  @media (max-width: 768px) {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 999;
  }
}

.session-header {
  padding: 20px;
  background: white;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .user-avatar {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      background: #07c160;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 20px;
    }
    
    .user-name {
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #333;
      }
    }
  }
  
  .new-session-btn {
    background: #07c160;
    border: none;
    color: white;
    
    &:hover {
      background: #05a049;
    }
  }
}

.search-box {
  padding: 15px 20px;
  background: white;
  border-bottom: 1px solid #e6e6e6;
  
  ::v-deep .el-input__inner {
    border-radius: 4px;
    background: #f5f5f5;
    border: none;
    height: 32px;
    font-size: 14px;
    
    &:focus {
      background: white;
    }
  }
}

.session-list-wrapper {
  flex: 1;
  overflow: hidden;
  background: white;
}

.session-list {
  height: 100%;
  overflow-y: auto;
  
  &::-webkit-scrollbar {
    display: none;
  }
  
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.empty-session {
  padding: 60px 20px;
  text-align: center;
  
  .empty-icon {
    font-size: 60px;
    color: #ddd;
    margin-bottom: 20px;
  }
  
  .empty-text {
    color: #999;
    font-size: 14px;
    margin-bottom: 20px;
  }
  
  .start-chat-btn {
    color: #07c160;
    font-size: 14px;
    
    &:hover {
      color: #05a049;
    }
  }
}

.session-item {
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f5f5f5;
  position: relative;
  
  &:hover {
    background: #f5f5f5;
    
    .session-dropdown {
      opacity: 1;
    }
  }
  
  &.active {
    background: #f0f9f4;
  }
}

.session-avatar {
  position: relative;
  
  .avatar-content {
    width: 44px;
    height: 44px;
    border-radius: 8px;
    background: #07c160;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 20px;
  }
  
  .unread-badge {
    position: absolute;
    top: -4px;
    right: -4px;
    min-width: 18px;
    height: 18px;
    padding: 0 4px;
    background: #f56c6c;
    color: white;
    font-size: 10px;
    border-radius: 9px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid white;
  }
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-header-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
  
  .session-title {
    font-weight: 600;
    color: #333;
    font-size: 15px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    flex: 1;
  }
  
  .session-time {
    font-size: 12px;
    color: #999;
    margin-left: 8px;
    flex-shrink: 0;
  }
}

.session-preview {
  font-size: 13px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-dropdown {
  opacity: 0;
  transition: opacity 0.2s;
  
  .el-dropdown-link {
    color: #999;
    padding: 4px;
    border-radius: 4px;
    
    &:hover {
      background: #f5f5f5;
      color: #07c160;
    }
  }
  
  ::v-deep .el-dropdown-menu {
    border-radius: 8px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    border: 1px solid #e6e6e6;
    
    .delete-item {
      color: #f56c6c;
      
      &:hover {
        background: #fef0f0;
      }
    }
  }
}

/* ===== 右侧聊天区域样式 ===== */
.chat-main {
  height: 100%;
  background: white;
  
  @media (max-width: 768px) {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 1000;
  }
}

.chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: white;
}

/* 聊天头部 */
.chat-header {
  padding: 0 20px;
  height: 60px;
  background: white;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  
  .chat-header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .back-btn {
      color: #07c160;
      font-size: 18px;
      display: none;
      
      @media (max-width: 768px) {
        display: block;
      }
    }
    
    .chat-user {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .chat-user-avatar {
        width: 40px;
        height: 40px;
        border-radius: 8px;
        background: #07c160;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 20px;
      }
      
      .chat-user-info {
        .chat-username {
          margin: 0;
          font-size: 16px;
          font-weight: 600;
          color: #333;
        }
        
        .chat-user-status {
          margin: 2px 0 0;
          font-size: 12px;
          color: #07c160;
        }
      }
    }
  }
}

/* 聊天消息区域 */
.chat-messages-wrapper {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  background: #f0f0f0;
  position: relative;
}

.chat-messages {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20px;
  overflow-y: auto;
  
  &::-webkit-scrollbar {
    display: none;
  }
  
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 欢迎消息 */
.welcome-message {
  text-align: center;
  padding: 40px 20px;
  
  .welcome-content {
    .welcome-avatar {
      width: 80px;
      height: 80px;
      margin: 0 auto 20px;
      border-radius: 20px;
      background: #07c160;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 36px;
    }
    
    .welcome-title {
      margin: 0 0 8px 0;
      font-size: 24px;
      font-weight: 600;
      color: #333;
    }
    
    .welcome-subtitle {
      margin: 0 0 30px 0;
      color: #666;
      font-size: 16px;
    }
    
    .welcome-features {
      display: flex;
      justify-content: center;
      gap: 20px;
      max-width: 400px;
      margin: 0 auto;
      
      @media (max-width: 768px) {
        flex-direction: column;
        gap: 15px;
      }
      
      .feature-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        
        i {
          font-size: 24px;
          color: #07c160;
        }
        
        span {
          font-size: 14px;
          color: #666;
        }
      }
    }
  }
}

.date-divider {
  text-align: center;
  margin: 20px 0;
  
  span {
    display: inline-block;
    padding: 6px 16px;
    background: rgba(0, 0, 0, 0.08);
    color: #666;
    font-size: 12px;
    border-radius: 12px;
  }
}

/* 消息项 */
.message-item {
  margin-bottom: 20px;
  
  &:last-child {
    margin-bottom: 0;
  }
}

/* AI消息 - 左侧显示 */
.message-ai {
  display: flex;
  align-items: flex-start;
  max-width: 85%;
  
  .message-avatar {
    margin-right: 12px;
    flex-shrink: 0;
    
    .avatar-content {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      background: #07c160;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 18px;
    }
  }
  
  .message-content-wrapper {
    max-width: calc(100% - 52px);
  }
}

/* 用户消息 - 右侧显示 */
.message-user {
  display: flex;
  justify-content: flex-end;
  max-width: 85%;
  margin-left: auto;
  
  .message-content-wrapper {
    max-width: calc(100% - 52px);
  }
  
  .message-avatar {
    margin-left: 12px;
    flex-shrink: 0;
    
    .avatar-content {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      background: white;
      border: 1px solid #e6e6e6;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #07c160;
      font-size: 18px;
    }
  }
}

/* 消息气泡 */
.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  position: relative;
  max-width: 100%;
  word-break: break-word;
  
  /* AI气泡 - 白色 */
  &.ai-bubble {
    background: white;
    color: #333;
    border: 1px solid #e6e6e6;
    border-top-left-radius: 4px;
    
    /* 左侧小箭头 */
    &::before {
      content: '';
      position: absolute;
      left: -8px;
      top: 14px;
      width: 0;
      height: 0;
      border-top: 8px solid transparent;
      border-right: 8px solid #e6e6e6;
      border-bottom: 8px solid transparent;
    }
    
    &::after {
      content: '';
      position: absolute;
      left: -6px;
      top: 14px;
      width: 0;
      height: 0;
      border-top: 8px solid transparent;
      border-right: 8px solid white;
      border-bottom: 8px solid transparent;
    }
  }
  
  /* 用户气泡 - 绿色 */
  &.user-bubble {
    background: #07c160;
    color: white;
    border-top-right-radius: 4px;
    
    /* 右侧小箭头 */
    &::before {
      content: '';
      position: absolute;
      right: -8px;
      top: 14px;
      width: 0;
      height: 0;
      border-top: 8px solid transparent;
      border-left: 8px solid #07c160;
      border-bottom: 8px solid transparent;
    }
  }
}

.message-text {
  font-size: 15px;
  line-height: 1.6;
}

.message-time {
  font-size: 11px;
  margin-top: 6px;
  opacity: 0.7;
  
  .ai-bubble & {
    color: #999;
    text-align: right;
  }
  
  .user-bubble & {
    color: rgba(255, 255, 255, 0.8);
    text-align: right;
  }
}

.message-status {
  position: absolute;
  right: -20px;
  bottom: 0;
  
  .el-icon-check {
    color: white;
    font-size: 12px;
  }
  
  .el-icon-loading {
    color: #999;
    font-size: 12px;
    animation: loading 1s linear infinite;
  }
  
  .el-icon-warning {
    color: #f56c6c;
    font-size: 12px;
  }
}

@keyframes loading {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.message-actions {
  margin-top: 8px;
  
  .copy-btn {
    font-size: 12px;
    color: #999;
    padding: 2px 8px;
    
    i {
      margin-right: 4px;
    }
    
    &:hover {
      color: #07c160;
    }
  }
}

/* 加载指示器 */
.loading-indicator {
  margin: 10px 0;
  
  .typing-indicator {
    background: white;
    padding: 12px 16px;
    border-radius: 16px;
    border: 1px solid #e6e6e6;
    border-top-left-radius: 4px;
    display: inline-flex;
    align-items: center;
    gap: 12px;
    position: relative;
    
    &::before {
      content: '';
      position: absolute;
      left: -8px;
      top: 14px;
      width: 0;
      height: 0;
      border-top: 8px solid transparent;
      border-right: 8px solid #e6e6e6;
      border-bottom: 8px solid transparent;
    }
    
    &::after {
      content: '';
      position: absolute;
      left: -6px;
      top: 14px;
      width: 0;
      height: 0;
      border-top: 8px solid transparent;
      border-right: 8px solid white;
      border-bottom: 8px solid transparent;
    }
  }
  
  .typing-dots {
    display: flex;
    align-items: center;
    gap: 4px;
    
    span {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #999;
      animation: typing 1.4s ease-in-out infinite;
      
      &:nth-child(1) { animation-delay: -0.32s; }
      &:nth-child(2) { animation-delay: -0.16s; }
      &:nth-child(3) { animation-delay: 0s; }
    }
  }
  
  @keyframes typing {
    0%, 60%, 100% { 
      transform: translateY(0);
      opacity: 0.6;
    }
    30% { 
      transform: translateY(-4px);
      opacity: 1;
    }
  }
  
  .typing-text {
    margin: 0;
    font-size: 12px;
    color: #999;
  }
}

/* 聊天输入区域 */
.chat-input-area {
  padding: 16px 20px;
  background: white;
  border-top: 1px solid #e6e6e6;
  flex-shrink: 0;
}

.input-main {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  
  .message-input {
    flex: 1;
    
    ::v-deep .el-textarea__inner {
      border: 1px solid #e6e6e6;
      border-radius: 8px;
      font-size: 15px;
      line-height: 1.5;
      resize: none;
      padding: 12px 16px;
      background: white;
      transition: all 0.3s;
      
      &:focus {
        border-color: #07c160;
      }
      
      /* 移除输入框滚动条 */
      &::-webkit-scrollbar {
        display: none;
      }
      
      & {
        -ms-overflow-style: none;
        scrollbar-width: none;
      }
    }
  }
  
  .send-button {
    width: 80px;
    height: 40px;
    border: none;
    border-radius: 8px;
    background: #07c160;
    color: white;
    font-size: 15px;
    font-weight: 500;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s;
    flex-shrink: 0;
    
    &:hover:not(:disabled) {
      background: #05a049;
    }
    
    &:disabled {
      background: #c0c4cc;
      cursor: not-allowed;
      opacity: 0.6;
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-container {
    height: calc(100vh - 60px);
  }
  
  .mobile-session-toggle {
    display: block;
  }
  
  .chat-header {
    padding: 0 15px;
    
    .back-btn {
      display: block !important;
    }
  }
  
  .chat-messages {
    padding: 15px;
  }
  
  .chat-input-area {
    padding: 12px 15px;
  }
  
  .message-ai,
  .message-user {
    max-width: 90%;
  }
  
  .welcome-content {
    .welcome-features {
      flex-direction: column;
      gap: 15px;
    }
  }
}
</style>