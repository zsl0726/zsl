import auth from '@/plugins/auth'
import router, { constantRoutes, dynamicRoutes } from '@/router'
import { getRouters } from '@/api/menu'
import Layout from '@/layout/index'
import ParentView from '@/components/ParentView'
import InnerLink from '@/layout/components/InnerLink'

const permission = {
  state: {
    routes: [],
    addRoutes: [],
    defaultRoutes: [],
    topbarRouters: [],
    sidebarRouters: []
  },
  mutations: {
    SET_ROUTES: (state, routes) => {
      state.addRoutes = routes
      state.routes = constantRoutes.concat(routes)
    },
    SET_DEFAULT_ROUTES: (state, routes) => {
      state.defaultRoutes = constantRoutes.concat(routes)
    },
    SET_TOPBAR_ROUTES: (state, routes) => {
      state.topbarRouters = routes
    },
    SET_SIDEBAR_ROUTERS: (state, routes) => {
      state.sidebarRouters = routes
    },
  },
  actions: {
    // 生成路由
    GenerateRoutes({ commit, rootGetters }) {
      return new Promise(resolve => {
        // 向后端请求路由数据
        getRouters().then(res => {
          const sdata = JSON.parse(JSON.stringify(res.data))
          const rdata = JSON.parse(JSON.stringify(res.data))
          
          // 获取用户角色
          const userRoles = rootGetters.roles || []
          console.log('GenerateRoutes - 当前用户角色:', userRoles)
          
          // 根据角色过滤路由（前端路由 + 后端返回路由）
          const filteredConstantRoutes = filterRoutesByRole(constantRoutes, userRoles)
          const filteredSdata = filterRoutesByRole(sdata, userRoles)
          const filteredRdata = filterRoutesByRole(rdata, userRoles)
          
          console.log('GenerateRoutes - 过滤后的前端路由数量:', filteredConstantRoutes.length)
          console.log('GenerateRoutes - 过滤后的前端路由详情:', filteredConstantRoutes)
          
          const sidebarRoutes = filterAsyncRouter(filteredSdata)
          const rewriteRoutes = filterAsyncRouter(filteredRdata, false, true)
          
          // 合并前端路由和后端路由
          const finalSidebarRoutes = filteredConstantRoutes.concat(sidebarRoutes)
          const finalRewriteRoutes = filteredConstantRoutes.concat(rewriteRoutes)
          
          // 过滤动态路由
          const asyncRoutes = filterDynamicRoutes(dynamicRoutes, userRoles)
          
          finalRewriteRoutes.push({ path: '*', redirect: '/404', hidden: true })
          
          // 添加动态路由
          if (asyncRoutes.length > 0) {
            router.addRoutes(asyncRoutes)
          }
          
          commit('SET_ROUTES', finalRewriteRoutes)
          commit('SET_SIDEBAR_ROUTERS', finalSidebarRoutes)
          commit('SET_DEFAULT_ROUTES', sidebarRoutes)
          commit('SET_TOPBAR_ROUTES', finalSidebarRoutes)
          
          console.log('GenerateRoutes - 最终生成的路由数量:', finalRewriteRoutes.length)
          console.log('GenerateRoutes - 最终侧边栏路由数量:', finalSidebarRoutes.length)
          
          resolve(finalRewriteRoutes)
        }).catch(error => {
          console.error('获取路由数据失败:', error)
          // 如果后端获取路由失败，使用前端路由
          const userRoles = rootGetters.roles || []
          const filteredRoutes = filterRoutesByRole(constantRoutes, userRoles)
          
          console.log('使用前端路由，过滤后的路由数量:', filteredRoutes.length)
          
          commit('SET_ROUTES', [])
          commit('SET_SIDEBAR_ROUTERS', filteredRoutes)
          commit('SET_DEFAULT_ROUTES', filteredRoutes)
          commit('SET_TOPBAR_ROUTES', filteredRoutes)
          resolve([])
        })
      })
    }
  }
}

// 根据角色过滤路由（递归处理子路由）
function filterRoutesByRole(routes, userRoles) {
  if (!routes || !Array.isArray(routes)) return []
  
  const filteredRoutes = []
  
  routes.forEach(route => {
    const newRoute = { ...route }
    
    // 检查是否隐藏路由（由页面控制）
    if (newRoute.hidden) {
      // 不添加到菜单，但保留路由用于权限检查
      return
    }
    
    // 检查路由权限
    const hasPermission = checkRoutePermission(newRoute, userRoles)
    console.log(`路由 ${newRoute.path} 权限检查:`, {
      meta: newRoute.meta,
      userRoles,
      hasPermission
    })
    
    if (!hasPermission) {
      return
    }
    
    // 如果有子路由，递归过滤
    if (newRoute.children && newRoute.children.length > 0) {
      const filteredChildren = filterRoutesByRole(newRoute.children, userRoles)
      
      // 如果子路由为空，检查父路由是否应该显示
      if (filteredChildren.length === 0) {
        // 特别处理咨询师的心理健康服务菜单
        if (userRoles.includes('psychologist') && newRoute.path === '/mental-health') {
          // 咨询师的心理健康服务菜单需要特殊处理
          const psychologistMentalHealthRoutes = getPsychologistMentalHealthRoutes()
          if (psychologistMentalHealthRoutes.length > 0) {
            newRoute.children = psychologistMentalHealthRoutes
            filteredRoutes.push(newRoute)
          }
          return
        }
        
        // 其他情况，如果父路由本身没有component（只是一个菜单分组），不添加
        if (!newRoute.component) {
          return
        }
      } else {
        newRoute.children = filteredChildren
        filteredRoutes.push(newRoute)
      }
    } else {
      // 没有子路由，直接添加
      filteredRoutes.push(newRoute)
    }
  })
  
  return filteredRoutes
}

// 检查路由权限
function checkRoutePermission(route, userRoles) {
  // 如果路由没有定义权限，允许访问（公共路由）
  if (!route.meta || !route.meta.roles || route.meta.roles.length === 0) {
    return true
  }
  
  // 检查用户角色是否在路由允许的角色中
  const routeRoles = route.meta.roles
  const hasPermission = userRoles.some(role => routeRoles.includes(role))
  
  return hasPermission
}

// 获取咨询师在心理健康服务下的路由（硬编码，根据您的路由配置）
function getPsychologistMentalHealthRoutes() {
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

// 遍历后台传来的路由字符串，转换为组件对象
function filterAsyncRouter(asyncRouterMap, lastRouter = false, type = false) {
  return asyncRouterMap.filter(route => {
    if (type && route.children) {
      route.children = filterChildren(route.children)
    }
    if (route.component) {
      // Layout ParentView 组件特殊处理
      if (route.component === 'Layout') {
        route.component = Layout
      } else if (route.component === 'ParentView') {
        route.component = ParentView
      } else if (route.component === 'InnerLink') {
        route.component = InnerLink
      } else {
        route.component = loadView(route.component)
      }
    }
    if (route.children != null && route.children && route.children.length) {
      route.children = filterAsyncRouter(route.children, route, type)
    } else {
      delete route['children']
      delete route['redirect']
    }
    return true
  })
}

function filterChildren(childrenMap, lastRouter = false) {
  var children = []
  childrenMap.forEach(el => {
    el.path = lastRouter ? lastRouter.path + '/' + el.path : el.path
    if (el.children && el.children.length && el.component === 'ParentView') {
      children = children.concat(filterChildren(el.children, el))
    } else {
      children.push(el)
    }
  })
  return children
}

// 动态路由遍历，验证是否具备权限
export function filterDynamicRoutes(routes, userRoles = []) {
  const res = []
  routes.forEach(route => {
    // 检查角色权限
    if (route.roles && route.roles.length > 0) {
      const hasRole = userRoles.some(role => route.roles.includes(role))
      if (hasRole) {
        res.push(route)
      }
    } else if (route.permissions) {
      if (auth.hasPermiOr(route.permissions)) {
        res.push(route)
      }
    } else {
      // 没有权限限制的路由
      res.push(route)
    }
  })
  return res
}

export const loadView = (view) => {
  if (process.env.NODE_ENV === 'development') {
    return (resolve) => require([`@/views/${view}`], resolve)
  } else {
    // 使用 import 实现生产环境的路由懒加载
    return () => import(`@/views/${view}`)
  }
}

export default permission