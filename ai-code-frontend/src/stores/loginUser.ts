import { ref } from 'vue'
import { defineStore } from 'pinia'
import { info } from '@/api/userController.ts'

/**
 * 登录用户信息
 */
export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })
  async function fetchLoginUser() {
    const res = await info()
    if (res.data.code === 200 && res.data.data) {
      loginUser.value = res.data.data
    }
  }
  function setLoginUser(newLoginUser: API.LoginUserVO) {
    loginUser.value = newLoginUser
  }

  return { loginUser, fetchLoginUser, setLoginUser }
})
