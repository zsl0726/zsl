import store from '@/store'

/**
 * 字符权限校验
 * @param {Array} value 校验值
 * @returns {Boolean}
 */
export function checkPermi(value) {
  if (value && value instanceof Array && value.length > 0) {
    const permissions = store.getters && store.getters.permissions
    const permissionDatas = value
    const all_permission = "*:*:*"

    const hasPermission = permissions.some(permission => {
      return all_permission === permission || permissionDatas.includes(permission)
    })

    return hasPermission

  } else {
    console.error(`need roles! Like checkPermi="['system:user:add','system:user:edit']"`)
    return false
  }
}

/**
 * 角色权限校验
 * @param {Array} value 校验值
 * @returns {Boolean}
 */
export function checkRole(value) {
  if (value && value instanceof Array && value.length > 0) {
    const roles = store.getters && store.getters.roles
    const permissionRoles = value
    const super_admin = "admin"

    const hasRole = roles.some(role => {
      return super_admin === role || permissionRoles.includes(role)
    })

    return hasRole

  } else {
    console.error(`need roles! Like checkRole="['admin','editor']"`)
    return false
  }
}

/**
 * 检查用户是否有权限访问咨询师页面
 */
export function checkPsychologistPermission() {
  const userType = store.getters.userType
  if (userType !== 'psychologist') {
    // 如果不是咨询师，重定向到用户首页
    Message.error('您没有权限访问咨询师页面')
    router.push('/')
    return false
  }
  return true
}