export default [
  {
    path: '/mental',
    component: () => import('@/layout'),
    redirect: '/mental/test',
    hidden: true,
    meta: {
      title: '心理评估',
      icon: 'education',
      roles: ['admin', 'user']
    },
    children: [
      {
        path: 'test',
        component: () => import('@/views/mental/test/index'),
        name: 'MentalTestPage',
        meta: { title: '心理测评', icon: 'edit' }
      },
      {
        path: 'report',
        component: () => import('@/views/mental/report/index'),
        name: 'MentalReportPage',
        meta: { title: '我的报告', icon: 'document' }
      },
      {
        path: 'record',
        component: () => import('@/views/mental/record/index'),
        name: 'MentalRecordPage',
        meta: { title: '报告详情', icon: 'detail' }
      },
      {
        path: 'appointment',
        component: () => import('@/views/mental/appointment/user/index'),
        name: 'AppointmentPage',
        meta: { 
          title: '咨询预约', 
          icon: 'schedule',
          roles: ['admin', 'user']
        }
      },
      {
        path: 'aiChat',
        component: () => import('@/views/mental/aiChat/index'),
        name: 'AiChatPage',
        meta: { 
          title: 'AI智能疏导', 
          icon: 'chat',
          roles: ['admin', 'user'] 
        }
      }
    ]
  },
  {
    path: '/mental-profile',
    component: () => import('@/layout'),
    redirect: '/mental-profile/index',
    hidden: false,
    meta: {
      title: '心理档案',
      icon: 'people',
      roles: ['admin', 'user']
    },
    children: [
      {
        path: 'index',
        component: () => import('@/views/mental/profile/index'),
        name: 'MentalProfileIndex',
        meta: { 
          title: '我的心理档案', 
          icon: 'user',
          roles: ['admin', 'user'] 
        }
      },
      {
        path: 'edit',
        component: () => import('@/views/mental/profile/edit'),
        name: 'MentalProfileEdit',
        hidden: false,
        meta: { 
          title: '修改心理档案', 
          icon: 'edit',
          roles: ['admin', 'user'] 
        }
      },
      {
        path: 'view',
        component: () => import('@/views/mental/profile/view'),
        name: 'MentalProfileView',
        hidden: false,
        meta: { 
          title: '查看心理档案', 
          icon: 'eye',
          roles: ['admin', 'user'] 
        }
      }
    ]
  }
]

