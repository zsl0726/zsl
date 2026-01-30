import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

import Layout from '@/layout'

// 公共路由
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  
  // ========== 用户首页（user 和 admin 可见） ==========
  {
    path: '',
    component: Layout,
    redirect: 'index',
    meta: {
      roles: ['user', 'admin']  // 用户和管理员可见
    },
    children: [
      {
        path: 'index',
        component: () => import('@/views/index'),
        name: 'Index',
        meta: { 
          title: '用户首页', 
          icon: 'dashboard', 
          affix: true, 
          roles: ['user', 'admin']  // 用户和管理员可见
        }
      }
    ]
  },

  // ========== 咨询师工作台（psychologist 和 admin 可见） ==========
  {
    path: '/psychologist',
    component: Layout,
    redirect: '/psychologist/index',
    meta: {
      title: '咨询师工作台',
      icon: 'user',
      requiresAuth: true,
      roles: ['psychologist', 'admin']  // 咨询师和管理员可见
    },
    children: [
      {
        path: 'index',
        component: () => import('@/views/psychologist/index'),
        name: 'PsychologistIndex',
        meta: {
          title: '咨询师首页',
          icon: 'dashboard',
          requiresAuth: true,
          roles: ['psychologist', 'admin']  // 咨询师和管理员可见
        }
      }
    ]
  },

  // ========== 个人中心路由（所有角色可见） ==========
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { 
          title: '个人中心', 
          icon: 'user',
          roles: ['user', 'psychologist', 'admin']  // 所有角色可见
        }
      }
    ]
  },

  // ========== 心理健康服务路由（统一使用嵌套结构） ==========
  {
    path: '/mental-health',
    component: Layout,
    redirect: '/mental-health/assessment',
    hidden:true,
    meta: {
      title: '心理健康服务',
      icon: 'health',
      roles: ['user', 'psychologist', 'admin']  // 所有角色可见父菜单
    },
    children: [
      // ========== 心理评估（user 和 admin 可见） ==========
      {
        path: 'assessment',
        component: () => import('@/views/mental/test/index'),
        name: 'MentalAssessment',
        meta: { 
          title: '心理测评', 
          icon: 'edit',
          roles: ['user', 'admin']  // 用户和管理员可见
        }
      },
      {
        path: 'reports',
        component: () => import('@/views/mental/report/index'),
        name: 'MentalReports',
        meta: { 
          title: '我的报告', 
          icon: 'document',
          roles: ['user', 'admin']  // 用户和管理员可见
        }
      },
      {
        path: 'report-detail/:id',
        component: () => import('@/views/mental/record/index'),
        name: 'ReportDetail',
        meta: { 
          title: '报告详情', 
          icon: 'detail',
          roles: ['user', 'admin'],  // 用户和管理员可见
          activeMenu: '/mental-health/reports'
        },
        hidden: true
      },

      // ========== 心理咨询（所有角色可见，子菜单按角色控制） ==========
      {
        path: 'appointment',
        component: () => import('@/views/mental/appointment/user/index'),
        name: 'Appointment',
        meta: { 
          title: '咨询预约', 
          icon: 'schedule',
          roles: ['user', 'admin']  // 用户和管理员可见
        }
      },
      {
        path: 'my-appointments',
        component: () => import('@/views/mental/appointment/user/myAppointments'),
        name: 'MyAppointments',
        meta: { 
          title: '我的预约', 
          icon: 'list',
          roles: ['user', 'admin']  // 用户和管理员可见
        }
      },
      {
        path: 'appointment-manage',
        component: () => import('@/views/mental/appointment/psychologist/index'),
        name: 'AppointmentManage',
        meta: { 
          title: '预约管理', 
          icon: 'setting',
          roles: ['psychologist', 'admin']  // 咨询师和管理员可见
        }
      },
      {
        path: 'my-schedule',
        component: () => import('@/views/mental/appointment/psychologist/schedule'),
        name: 'MySchedule',
        meta: { 
          title: '我的排班', 
          icon: 'calendar',
          roles: ['psychologist', 'admin']  // 咨询师和管理员可见
        }
      },

      // ========== 智能疏导（user 和 admin 可见） ==========
      {
        path: 'ai-chat',
        component: () => import('@/views/mental/aiChat/index'),
        name: 'AiChat',
        meta: { 
          title: 'AI智能疏导', 
          icon: 'chat',
          roles: ['user', 'admin']  // 用户和管理员可见
        }
      }
    ]
  },

  // ========== 心理档案菜单（单独菜单，user 和 admin 可见） ==========
  {
    path: '/mental-profile',
    component: Layout,
    redirect: '/mental-profile/index',
    meta: {
      title: '心理档案',
      icon: 'user',
      roles: ['user', 'admin']  // 用户和管理员可见
    },
    children: [
      {
        path: 'index',
        component: () => import('@/views/mental/profile/index'),
        name: 'MentalProfile',
        meta: { 
          title: '我的心理档案', 
          icon: 'user',
          roles: ['user', 'admin']  // 用户和管理员可见
        }
      },
      {
        path: 'edit',
        component: () => import('@/views/mental/profile/edit'),
        name: 'MentalProfileEdit',
        meta: { 
          title: '修改心理档案', 
          icon: 'edit',
          roles: ['user', 'admin'],  // 用户和管理员可见
          activeMenu: '/mental-profile/index'
        },
        // hidden: true
      },
      {
        path: 'view/:id',
        component: () => import('@/views/mental/profile/view'),
        name: 'MentalProfileView',
        meta: { 
          title: '查看心理档案', 
          icon: 'eye',
          roles: ['admin'],  // 用户和管理员可见
          activeMenu: '/mental-profile/index'
        },
        // hidden: true
      }
    ]
  },

  // ========== 心理档案管理（咨询师功能，psychologist 和 admin 可见） ==========
  {
    path: '/profile-management',
    component: Layout,
    redirect: '/profile-management/list',
    meta: {
      title: '档案管理',
      icon: 'folder',
      roles: ['psychologist', 'admin']  // 咨询师和管理员可见
    },
    children: [
      {
        path: 'list',
        component: () => import('@/views/mental/profile/consultant/List'),
        name: 'ProfileManagementList',
        meta: { 
          title: '档案列表', 
          icon: 'list',
          roles: ['psychologist', 'admin']  // 咨询师和管理员可见
        }
      },
      {
        path: '',
        component: () => import('@/views/mental/profile/consultant/View'),
        name: 'ConsultantProfileView',
        meta: { 
          title: '档案详情', 
          icon: 'eye',
          roles: ['psychologist', 'admin'],  // 咨询师和管理员可见
          activeMenu: '/profile-management/list'
        },
        hidden: true
      }
    ]
  },

  // ========== 系统管理路由（仅 admin 可见） ==========
  {
    path: '/system',
    component: Layout,
    hidden:true,
    redirect: '/system/user',
    meta: {
      title: '系统管理',
      icon: 'system',
      roles: ['admin']  // 仅管理员可见
    },
    children: [
      {
        path: 'user',
        component: () => import('@/views/system/user/index'),
        name: 'User',
        meta: { 
          title: '用户管理', 
          icon: 'user',
          roles: ['admin']  // 仅管理员可见
        }
      },
      {
        path: 'role',
        component: () => import('@/views/system/role/index'),
        name: 'Role',
        meta: { 
          title: '角色管理', 
          icon: 'peoples',
          roles: ['admin']  // 仅管理员可见
        }
      },
      {
        path: 'menu',
        component: () => import('@/views/system/menu/index'),
        name: 'Menu',
        meta: { 
          title: '菜单管理', 
          icon: 'tree-table',
          roles: ['admin']  // 仅管理员可见
        }
      }
    ]
  },
  
  // 404兜底路由（必须放在最后）
  { path: '*', redirect: '/404', hidden: true }
]

// 动态路由
export const dynamicRoutes = [
  {
    path: '/system/user-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:user:edit'],
    roles: ['admin'],  // 仅管理员可见
    children: [
      {
        path: 'role/:userId(\\d+)',
        component: () => import('@/views/system/user/authRole'),
        name: 'AuthRole',
        meta: { 
          title: '分配角色', 
          activeMenu: '/system/user',
          roles: ['admin']  // 仅管理员可见
        }
      }
    ]
  },
  {
    path: '/system/role-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:role:edit'],
    roles: ['admin'],  // 仅管理员可见
    children: [
      {
        path: 'user/:roleId(\\d+)',
        component: () => import('@/views/system/role/authUser'),
        name: 'AuthUser',
        meta: { 
          title: '分配用户', 
          activeMenu: '/system/role',
          roles: ['admin']  // 仅管理员可见
        }
      }
    ]
  },
  {
    path: '/system/dict-data',
    component: Layout,
    hidden: true,
    permissions: ['system:dict:list'],
    roles: ['admin'],  // 仅管理员可见
    children: [
      {
        path: 'index/:dictId(\\d+)',
        component: () => import('@/views/system/dict/data'),
        name: 'Data',
        meta: { 
          title: '字典数据', 
          activeMenu: '/system/dict',
          roles: ['admin']  // 仅管理员可见
        }
      }
    ]
  },
  {
    path: '/monitor/job-log',
    component: Layout,
    hidden: true,
    permissions: ['monitor:job:list'],
    roles: ['admin'],  // 仅管理员可见
    children: [
      {
        path: 'index/:jobId(\\d+)',
        component: () => import('@/views/monitor/job/log'),
        name: 'JobLog',
        meta: { 
          title: '调度日志', 
          activeMenu: '/monitor/job',
          roles: ['admin']  // 仅管理员可见
        }
      }
    ]
  },
  {
    path: '/tool/gen-edit',
    component: Layout,
    hidden: true,
    permissions: ['tool:gen:edit'],
    roles: ['admin'],  // 仅管理员可见
    children: [
      {
        path: 'index/:tableId(\\d+)',
        component: () => import('@/views/tool/gen/editTable'),
        name: 'GenEdit',
        meta: { 
          title: '修改生成配置', 
          activeMenu: '/tool/gen',
          roles: ['admin']  // 仅管理员可见
        }
      }
    ]
  }
]

// 防止连续点击多次路由报错
let routerPush = Router.prototype.push
let routerReplace = Router.prototype.replace
Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(err => err)
}
Router.prototype.replace = function replace(location) {
  return routerReplace.call(this, location).catch(err => err)
}

// 初始化路由
export default new Router({
  mode: 'history',
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})