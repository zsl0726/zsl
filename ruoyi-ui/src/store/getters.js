const getters = {
  sidebar: state => state.app.sidebar,
  size: state => state.app.size,
  device: state => state.app.device,
  dict: state => state.dict.dict,
  visitedViews: state => state.tagsView.visitedViews,
  cachedViews: state => state.tagsView.cachedViews,
  token: state => state.user.token,
  avatar: state => state.user.avatar,
  id: state => state.user.id,
  name: state => state.user.name,
  nickName: state => state.user.nickName,
  introduction: state => state.user.introduction,
  roles: state => {
    const roles = state.user.roles || []
    console.log('getters.roles 被调用:', roles)
    return roles
  },
  permissions: state => state.user.permissions,
  permission_routes: state => state.permission.routes,
  topbarRouters: state => state.permission.topbarRouters,
  defaultRoutes: state => state.permission.defaultRoutes,
  sidebarRouters: state => state.permission.sidebarRouters,
  hasMentalProfile: state => {
    console.log('检查心理档案状态 - Vuex状态:', state.user.hasMentalProfile, 
                'localStorage:', localStorage.getItem('hasMentalProfile'));
    
    // 1. 优先使用 Vuex 中的状态（已经在 user.js 中持久化到 localStorage）
    if (state.user.hasMentalProfile !== undefined) {
      return state.user.hasMentalProfile;
    }
    
    // 2. 直接检查 localStorage（确保即时更新）
    const localHasProfile = localStorage.getItem('hasMentalProfile');
    if (localHasProfile !== null) {
      return localHasProfile === 'true';
    }
    
    // 3. 原有的 state.profile 检查逻辑（保持兼容性）
    if (!state.profile) return false;
    const coreFields = ['id', 'realName', 'age', 'gender'];
    const hasCoreField = coreFields.some(field => {
      const value = state.profile[field];
      return value && value !== '' && value !== '未知' && value !== 0;
    });
    
    console.log('使用 state.profile 判断档案状态:', hasCoreField);
    return hasCoreField;
  }
}
export default getters;