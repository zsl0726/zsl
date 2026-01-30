import router from '@/router'
import { MessageBox, } from 'element-ui'
import { login, logout, getInfo } from '@/api/login'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { isHttp, isEmpty } from "@/utils/validate"
import defAva from '@/assets/images/profile.jpg'

const user = {
  state: {
    token: getToken(),
    id: localStorage.getItem('userId') || '',
    name: '',
    nickName: '',
    avatar: '',
    roles: [],
    permissions: [],
    introduction: '',
    // 新增：心理档案状态（持久化存储）
    hasMentalProfile: localStorage.getItem('hasMentalProfile') === 'true' || false,
    // 新增：用户其他信息
    userInfo: null
  },

  mutations: {
    SET_TOKEN: (state, token) => {
      state.token = token
      setToken(token)
    },
    SET_ID: (state, id) => {
      state.id = id
      localStorage.setItem('userId', id)
    },
    SET_NAME: (state, name) => {
      state.name = name
    },
    SET_NICK_NAME: (state, nickName) => {
      state.nickName = nickName
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_INTRODUCTION: (state, introduction) => {
      state.introduction = introduction
    },
    SET_ROLES: (state, roles) => {
      // 确保roles是正确格式的数组
      if (Array.isArray(roles)) {
        state.roles = roles
      } else if (roles) {
        state.roles = [roles]
      } else {
        state.roles = []
      }
      console.log('SET_ROLES 设置的角色:', state.roles)
    },
    SET_PERMISSIONS: (state, permissions) => {
      state.permissions = permissions
    },
    // 新增：更新心理档案状态的mutation
    SET_HAS_PROFILE: (state, status) => {
      console.log('SET_HAS_PROFILE 更新档案状态:', status);
      state.hasMentalProfile = status;
      localStorage.setItem('hasMentalProfile', status.toString());
    },
    // 新增：存储完整的用户信息
    SET_USERINFO: (state, userInfo) => {
      state.userInfo = userInfo;
      // 如果用户信息中包含档案状态，同步更新
      if (userInfo && userInfo.hasMentalProfile !== undefined) {
        state.hasMentalProfile = userInfo.hasMentalProfile;
        localStorage.setItem('hasMentalProfile', userInfo.hasMentalProfile.toString());
      }
    },
    // 清除所有状态（用于登出）
    RESET_STATE: (state) => {
      state.token = ''
      state.id = ''
      state.name = ''
      state.nickName = ''
      state.avatar = ''
      state.roles = []
      state.permissions = []
      state.introduction = ''
      state.hasMentalProfile = false
      state.userInfo = null
    }
  },

  actions: {
    // 登录
    Login({ commit }, userInfo) {
      const username = userInfo.username.trim()
      const password = userInfo.password
      const code = userInfo.code
      const uuid = userInfo.uuid
      return new Promise((resolve, reject) => {
        login(username, password, code, uuid).then(res => {
          commit('SET_TOKEN', res.token)
          if (res.userId) {
            commit('SET_ID', res.userId)
          }
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 获取用户信息 - 增强版本，处理档案状态
    GetInfo({ commit, state }) {
      return new Promise((resolve, reject) => {
        getInfo().then(res => {
          console.log('GetInfo API返回数据:', res)
          
          const user = res.user
          let avatar = user.avatar || ""
          if (!isHttp(avatar)) {
            avatar = (isEmpty(avatar)) ? defAva : process.env.VUE_APP_BASE_API + avatar
          }
          
          // 处理角色数据
          let roles = []
          
          // 检查不同格式的角色数据
          if (res.roles && res.roles.length > 0) {
            roles = res.roles
          } else if (res.user && res.user.roles) {
            roles = res.user.roles
          } else if (res.role) {
            roles = Array.isArray(res.role) ? res.role : [res.role]
          }
          
          console.log('提取的原始角色数据:', roles)
          
          // 转换角色值为字符串格式并进行映射
          roles = roles.map(role => {
            if (typeof role === 'string') {
              // 角色映射：将 common 映射为 user
              if (role === 'common') return 'user'
              if (role === 'psychologist') return 'psychologist'
              if (role === 'admin') return 'admin'
              return role
            } else if (role && typeof role === 'object') {
              // 提取角色key
              if (role.roleKey) {
                // 角色映射
                if (role.roleKey === 'common') return 'user'
                return role.roleKey
              }
              if (role.roleName) return role.roleName
              if (role.name) return role.name
              if (role.key) return role.key
            }
            return String(role)
          }).filter(role => role && role !== 'ROLE_DEFAULT')
          
          console.log('处理后的角色数据（映射后）:', roles)
          
          // 如果没有角色，设置为默认用户角色
          if (roles.length === 0) {
            // 根据用户名判断默认角色
            if (user.userName === 'admin') {
              roles = ['admin']
            } else if (user.userName.includes('psych')) {
              roles = ['psychologist']
            } else {
              roles = ['user']
            }
          }
          
          // 保存角色
          commit('SET_ROLES', roles)
          
          // 保存权限
          if (res.permissions) {
            commit('SET_PERMISSIONS', res.permissions)
          }
          
          commit('SET_ID', user.userId)
          commit('SET_NAME', user.userName)
          commit('SET_NICK_NAME', user.nickName)
          commit('SET_AVATAR', avatar)

          // 保存用户信息到 localStorage，供首页显示用户名使用
          localStorage.setItem('health_user_name', user.nickName || user.userName || '用户')
          localStorage.setItem('health_user_info', JSON.stringify({
            nickname: user.nickName,
            username: user.userName,
            userId: user.userId
          }))

          // 设置简介（如果存在）
          if (user.introduction) {
            commit('SET_INTRODUCTION', user.introduction)
          }
          
          // 处理心理档案状态
          // 1. 检查后端是否返回了档案信息
          let hasMentalProfile = false;
          
          // 咨询师和管理员不需要档案
          if (roles.includes('psychologist') || roles.includes('admin')) {
            hasMentalProfile = true;
          } 
          // 检查后端是否返回了档案ID或其他标志
          else if (user.mentalProfileId) {
            hasMentalProfile = true;
          }
          // 检查后端是否显式返回了档案状态
          else if (res.hasMentalProfile !== undefined) {
            hasMentalProfile = res.hasMentalProfile;
          }
          // 检查user对象中是否有档案状态
          else if (user.hasMentalProfile !== undefined) {
            hasMentalProfile = user.hasMentalProfile;
          }
          
          console.log('计算出的心理档案状态:', hasMentalProfile);
          
          // 保存档案状态
          commit('SET_HAS_PROFILE', hasMentalProfile);
          
          // 保存完整的用户信息
          const userInfoData = {
            ...user,
            roles: roles,
            hasMentalProfile: hasMentalProfile
          };
          commit('SET_USERINFO', userInfoData);
          
          // 初始密码提示
          if (res.isDefaultModifyPwd) {
            MessageBox.confirm('您的密码还是初始密码，请修改密码！', '安全提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              router.push({ name: 'Profile', params: { activeTab: 'resetPwd' } })
            }).catch(() => {})
          }
          
          // 过期密码提示
          if (!res.isDefaultModifyPwd && res.isPasswordExpired) {
            MessageBox.confirm('您的密码已过期，请尽快修改密码！', '安全提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              router.push({ name: 'Profile', params: { activeTab: 'resetPwd' } })
            }).catch(() => {})
          }
          
          resolve(res)
        }).catch(error => {
          console.error('获取用户信息失败:', error)
          reject(error)
        })
      })
    },

    // 退出系统
    LogOut({ commit, state }) {
      return new Promise((resolve, reject) => {
        logout(state.token).then(() => {
          commit('RESET_STATE')
          removeToken()
          localStorage.removeItem('userId')
          localStorage.removeItem('hasMentalProfile')
          localStorage.removeItem('health_user_name')
          localStorage.removeItem('health_user_info')
          resolve()
        }).catch(error => {
          // 即使后端登出失败，也要清除前端状态
          commit('RESET_STATE')
          removeToken()
          localStorage.removeItem('userId')
          localStorage.removeItem('hasMentalProfile')
          localStorage.removeItem('health_user_name')
          localStorage.removeItem('health_user_info')
          reject(error)
        })
      })
    },

    // 前端登出（无请求）
    FedLogOut({ commit }) {
      return new Promise(resolve => {
        commit('RESET_STATE')
        removeToken()
        localStorage.removeItem('userId')
        localStorage.removeItem('hasMentalProfile')
        localStorage.removeItem('health_user_name')
        localStorage.removeItem('health_user_info')
        resolve()
      })
    },

    // 更新心理档案状态（完善档案后调用）
    UpdateProfileStatus({ commit }, status) {
      return new Promise((resolve) => {
        console.log('调用 UpdateProfileStatus，状态:', status);
        commit('SET_HAS_PROFILE', status);
        
        // 同时更新本地存储的其他相关数据，确保一致性
        if (status) {
          localStorage.setItem('profileGuideSkipped', 'true');
          console.log('档案状态已更新为true，同时设置profileGuideSkipped');
        }
        
        resolve();
      });
    },

    // 初始化档案状态（页面加载时调用）
    InitProfileStatus({ commit }) {
      const status = localStorage.getItem('hasMentalProfile') === 'true';
      console.log('初始化档案状态:', status);
      commit('SET_HAS_PROFILE', status);
    }
  }
}

export default user;