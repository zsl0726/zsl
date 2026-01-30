import router from './router'
import store from './store'
import { Message } from 'element-ui'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register', '/health/login', '/health/register', '/health/forgot-password']

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

// 心理咨询相关路径（咨询师专用）
const psychologistMentalHealthPaths = [
  '/mental-health',
  '/mental-health/appointment-manage',
  '/mental-health/my-schedule',
  '/mental-health/psychologist-consultation'
]

// 检查是否是心理咨询相关路径
const isPsychologistMentalHealthPath = (path) => {
  return psychologistMentalHealthPaths.some(
    mentalPath => path === mentalPath || path.startsWith(mentalPath + '/')
  )
}

// 获取咨询师在心理健康服务下的路由
const getPsychologistMentalHealthChildren = () => {
  return [
    {
      path: 'appointment-manage',
      component: () => import('@/views/mental/appointment/psychologist/index'),
      name: 'AppointmentManage',
      meta: { 
        title: '预约管理', 
        icon: 'setting',
        roles: ['psychologist', 'admin']
      }
    },
    {
      path: 'my-schedule',
      component: () => import('@/views/mental/appointment/psychologist/schedule'),
      name: 'MySchedule',
      meta: { 
        title: '我的排班', 
        icon: 'calendar',
        roles: ['psychologist', 'admin']
      }
    }
  ]
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  
  console.log('=== 路由守卫开始 ===');
  console.log('目标路径:', to.path, '来自路径:', from.path);
  console.log('是否有token:', !!getToken());
  
  // 设置页面标题
  if (to.meta && to.meta.title) {
    store.dispatch('settings/setTitle', to.meta.title)
  }
  
  if (getToken()) {
    /* 已登录 */
    console.log('用户已登录，开始检查权限');
    
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else if (isWhiteList(to.path)) {
      next()
    } else {
      if (store.getters.roles.length === 0) {
        // 用户信息未获取
        console.log('用户信息未获取，开始获取用户信息');
        isRelogin.show = true
        store.dispatch('GetInfo').then(() => {
          isRelogin.show = false
          console.log('用户信息获取完成，开始生成路由');
          
          const roles = store.getters.roles
          console.log('当前用户角色:', roles)
          
          // 特别处理咨询师角色
          if (roles.includes('psychologist')) {
            console.log('✅ 检测到咨询师角色，开始处理心理咨询相关权限')
            
            // 生成咨询师可访问的路由
            store.dispatch('GenerateRoutes', { isPsychologist: true }).then(accessRoutes => {
              console.log('✅ 咨询师路由生成完成，当前路径:', to.path);
              console.log('✅ 咨询师角色:', roles);
              console.log('✅ 生成的路由数量:', accessRoutes ? accessRoutes.length : 0);
              
              // 添加路由到路由器
              if (accessRoutes && accessRoutes.length > 0) {
                router.addRoutes(accessRoutes)
              }
              
              // 对于咨询师，检查心理咨询相关路径
              if (isPsychologistMentalHealthPath(to.path)) {
                console.log('✅ 咨询师访问心理咨询相关路径，放行:', to.path)
                next()
                NProgress.done()
                return
              }
              
              // 特殊处理：如果咨询师访问根路径或用户首页，重定向到咨询师首页
              if (to.path === '/' || to.path === '' || to.path === '/index') {
                console.log('✅ 咨询师访问根路径或用户首页，重定向到咨询师首页')
                next({ path: '/psychologist/index', replace: true })
                NProgress.done()
                return
              }
              
              // 检查权限
              if (checkRoutePermission(to, roles)) {
                console.log('✅ 咨询师有权限访问:', to.path)
                next({ ...to, replace: true })
              } else {
                console.log('❌ 咨询师没有权限访问:', to.path)
                // 尝试访问404前，先检查是否是心理咨询相关路径
                if (to.path.includes('mental-health')) {
                  console.log('⚠️ 咨询师访问心理健康相关路径但未找到，可能是路由配置问题')
                  // 尝试直接加载心理咨询相关路由
                  const mentalHealthRoute = findMentalHealthRoute(to.path, roles)
                  if (mentalHealthRoute) {
                    console.log('✅ 找到心理健康相关路由，重定向:', mentalHealthRoute.path)
                    next({ path: mentalHealthRoute.path, replace: true })
                  } else {
                    console.log('❌ 未找到相关心理健康路由，跳转404')
                    next('/404')
                  }
                } else {
                  next('/404')
                }
              }
            }).catch(err => {
              console.error('生成路由失败:', err)
              next('/404')
            })
          } else {
            // 非咨询师角色，原有逻辑
            store.dispatch('GenerateRoutes').then(accessRoutes => {
              console.log('路由生成完成，当前路径:', to.path);
              console.log('用户角色:', store.getters.roles);
              
              // 处理401/404页面
              if (to.path === '/401' || to.path === '/404') {
                console.log('访问401/404页面，直接放行');
                next()
                NProgress.done()
                return;
              }
              
              // 从根路径访问的重定向逻辑
              if (to.path === '/' || to.path === '') {
                console.log('从根路径访问，根据角色重定向');
                
                if (roles.includes('psychologist')) {
                  next({ path: '/psychologist/index', replace: true })
                  NProgress.done()
                  return
                } else if (roles.includes('admin')) {
                  next({ path: '/psychologist/index', replace: true })
                  NProgress.done()
                  return
                } else if (roles.includes('user')) {
                  next({ path: '/index', replace: true })
                  NProgress.done()
                  return
                }
              }
              
              // 咨询师访问用户首页，重定向到咨询师首页
              if ((to.path === '/index' || to.path === '/index/') && roles.includes('psychologist')) {
                console.log('咨询师访问用户首页，重定向到咨询师首页');
                next({ path: '/psychologist/index', replace: true })
                NProgress.done()
                return
              }
              
              // 用户访问咨询师首页，重定向到用户首页
              if ((to.path === '/psychologist/index' || to.path === '/psychologist/') && roles.includes('user')) {
                console.log('用户访问咨询师首页，重定向到用户首页');
                next({ path: '/index', replace: true })
                NProgress.done()
                return
              }
              
              // 首页权限检查
              if (to.path === '/index' || to.path === '/index/') {
                if (!roles.includes('user') && !roles.includes('admin')) {
                  console.log('用户尝试访问用户首页但没有权限，角色:', roles)
                  next('/401')
                  NProgress.done()
                  return
                }
              }
              
              if (to.path === '/psychologist/index' || to.path === '/psychologist/') {
                if (!roles.includes('psychologist') && !roles.includes('admin')) {
                  console.log('用户尝试访问咨询师首页但没有权限，角色:', roles)
                  next('/401')
                  NProgress.done()
                  return
                }
              }
              
              // 检查当前路由权限
              if (checkRoutePermission(to, store.getters.roles)) {
                console.log('有权限访问:', to.path)
                router.addRoutes(accessRoutes)
                next({ ...to, replace: true })
              } else {
                console.log('没有权限访问:', to.path, '用户角色:', store.getters.roles)
                next('/401')
              }
            }).catch(err => {
              console.error('获取用户信息失败:', err)
              store.dispatch('LogOut').then(() => {
                Message.error('用户信息验证失败')
                next({ path: '/' })
              })
            })
          }
        }).catch(err => {
          console.error('获取用户信息失败:', err)
          store.dispatch('LogOut').then(() => {
            Message.error('用户信息验证失败')
            next({ path: '/' })
          })
        })
      } else {
        // 用户信息已获取的情况
        const roles = store.getters.roles
        console.log('用户信息已获取，当前路径:', to.path, '用户角色:', roles)
        
        // 处理401/404页面
        if (to.path === '/401' || to.path === '/404') {
          console.log('访问401/404页面，直接放行');
          next()
          NProgress.done()
          return;
        }
        
        // 特别处理咨询师角色
        if (roles.includes('psychologist')) {
          console.log('✅ 咨询师已登录，检查心理咨询菜单访问')
          
          // 检查是否是心理咨询相关路径
          if (isPsychologistMentalHealthPath(to.path)) {
            console.log('✅ 咨询师访问心理咨询相关路径:', to.path)
            
            // 检查具体权限
            if (checkRoutePermission(to, roles)) {
              console.log('✅ 咨询师有权限访问心理咨询菜单')
              next()
            } else {
              console.log('❌ 咨询师没有心理咨询菜单权限')
              next('/401')
            }
            NProgress.done()
            return
          }
          
          // 咨询师访问根路径或用户首页，重定向到咨询师首页
          if (to.path === '/' || to.path === '' || to.path === '/index') {
            console.log('✅ 咨询师访问根路径或用户首页，重定向到咨询师首页')
            next({ path: '/psychologist/index', replace: true })
            NProgress.done()
            return
          }
        }
        
        // 从根路径访问的重定向逻辑
        if (to.path === '/' || to.path === '') {
          console.log('从根路径访问，根据角色重定向');
          
          if (roles.includes('psychologist')) {
            next({ path: '/psychologist/index', replace: true })
            NProgress.done()
            return
          } else if (roles.includes('admin')) {
            next({ path: '/psychologist/index', replace: true })
            NProgress.done()
            return
          } else if (roles.includes('user')) {
            next({ path: '/index', replace: true })
            NProgress.done()
            return
          }
        }
        
        // 咨询师访问用户首页，重定向到咨询师首页
        if ((to.path === '/index' || to.path === '/index/') && roles.includes('psychologist')) {
          console.log('咨询师访问用户首页，重定向到咨询师首页');
          next({ path: '/psychologist/index', replace: true })
          NProgress.done()
          return
        }
        
        // 用户访问咨询师首页，重定向到用户首页
        if ((to.path === '/psychologist/index' || to.path === '/psychologist/') && roles.includes('user')) {
          console.log('用户访问咨询师首页，重定向到用户首页');
          next({ path: '/index', replace: true })
          NProgress.done()
          return
        }
        
        // 首页权限检查
        if (to.path === '/index' || to.path === '/index/') {
          if (!roles.includes('user') && !roles.includes('admin')) {
            console.log('用户尝试访问用户首页但没有权限，角色:', roles)
            next('/401')
            NProgress.done()
            return
          }
        }
        
        if (to.path === '/psychologist/index' || to.path === '/psychologist/') {
          if (!roles.includes('psychologist') && !roles.includes('admin')) {
            console.log('用户尝试访问咨询师首页但没有权限，角色:', roles)
            next('/401')
            NProgress.done()
            return
          }
        }
        
        // 检查当前路由权限
        if (checkRoutePermission(to, roles)) {
          console.log('有权限访问:', to.path)
          next()
        } else {
          console.log('没有权限访问:', to.path, '用户角色:', roles)
          // 对于咨询师访问心理健康相关路径的特殊处理
          if (roles.includes('psychologist') && to.path.includes('mental-health')) {
            console.log('⚠️ 咨询师访问心理健康相关路径但权限检查失败，尝试修复')
            // 可能是路由未正确生成，尝试重新生成
            store.dispatch('GenerateRoutes', { isPsychologist: true }).then(() => {
              console.log('✅ 重新生成路由完成，重新检查')
              if (checkRoutePermission(to, roles)) {
                next()
              } else {
                next('/401')
              }
            }).catch(() => {
              next('/401')
            })
          } else {
            next('/401')
          }
        }
      }
    }
  } else {
    /* 未登录 */
    console.log('用户未登录，当前路径:', to.path);
    
    if (isWhiteList(to.path)) {
      console.log('在白名单中，放行');
      next()
    } else {
      console.log('不在白名单，重定向到登录页');
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})

// 检查是否有权限访问路由
function checkRoutePermission(route, userRoles) {
  // 如果路由没有定义权限，允许访问
  if (!route.meta || (!route.meta.roles && !route.meta.permissions)) {
    return true
  }
  
  // 特殊处理：咨询师访问心理咨询相关菜单
  if (userRoles.includes('psychologist')) {
    // 检查是否是心理健康服务路径
    const isMentalHealthPath = route.path && (
      route.path.startsWith('/mental-health/') ||
      route.path === '/mental-health'
    )
    
    if (isMentalHealthPath) {
      console.log(`🔍 咨询师检查心理健康路径 ${route.path}`)
      
      // 定义咨询师可以访问的心理健康子路径
      const psychologistAllowedMentalPaths = [
        'appointment-manage',
        'my-schedule',
        'psychologist-consultation'
      ]
      
      // 检查具体路径
      const pathParts = route.path.split('/')
      const lastPart = pathParts[pathParts.length - 1]
      
      if (psychologistAllowedMentalPaths.includes(lastPart)) {
        console.log(`✅ 咨询师允许访问心理健康子路径: ${lastPart}`)
        return true
      }
      
      // 对于心理健康服务根路径，咨询师也应该可以访问（作为菜单分组）
      if (route.path === '/mental-health' && route.meta && route.meta.roles) {
        const hasPermission = userRoles.some(role => route.meta.roles.includes(role))
        console.log(`咨询师检查心理健康根路径 ${route.path}: 需要的角色 ${route.meta.roles}, 结果 ${hasPermission}`)
        return hasPermission
      }
    }
  }
  
  // 检查角色权限
  if (route.meta.roles && route.meta.roles.length > 0) {
    const hasPermission = userRoles.some(role => route.meta.roles.includes(role))
    console.log(`权限检查: 路由 ${route.path} 需要的角色 ${route.meta.roles}, 用户角色 ${userRoles}, 结果: ${hasPermission}`)
    return hasPermission
  }
  
  // 检查权限标识
  if (route.meta.permissions && route.meta.permissions.length > 0) {
    // 这里可以添加权限检查逻辑
    console.log(`权限检查: 路由 ${route.path} 需要的权限 ${route.meta.permissions}`)
  }
  
  return true
}

// 查找心理健康相关路由（辅助函数）
function findMentalHealthRoute(path, roles) {
  // 尝试匹配心理健康相关路由
  const mentalHealthRoutes = [
    {
      path: '/mental-health/appointment-manage',
      meta: { roles: ['psychologist', 'admin'] }
    },
    {
      path: '/mental-health/my-schedule',
      meta: { roles: ['psychologist', 'admin'] }
    },
    {
      path: '/mental-health/psychologist-consultation',
      meta: { roles: ['psychologist', 'admin'] }
    }
  ]
  
  // 如果是咨询师，返回第一个可访问的路由
  if (roles.includes('psychologist')) {
    return mentalHealthRoutes[0]
  }
  
  return null
}

export default router