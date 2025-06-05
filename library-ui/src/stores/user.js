import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // Attempt to load user and token from localStorage on store initialization
  const storedUser = localStorage.getItem('user');
  const user = ref(storedUser ? JSON.parse(storedUser) : null)
  const token = ref(localStorage.getItem('token'))

  const isLoggedIn = computed(() => !!token.value && !!user.value)

  // This function is now used for the full login success process
  function loginSuccess(userData, newToken) {
    user.value = userData;
    token.value = newToken;
    // Persist both user data and token
    localStorage.setItem('user', JSON.stringify(userData));
    localStorage.setItem('token', newToken);
  }

  // This might not be strictly needed anymore if loginSuccess is used, but keep for clarity or other potential uses
  function setUser(userData) {
    user.value = userData
     // Optional: also persist user here if setUser is called independently of setToken/loginSuccess
     // if (userData) {
     //    localStorage.setItem('user', JSON.stringify(userData));
     // } else {
     //    localStorage.removeItem('user');
     // }
  }

  function setToken(newToken) {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
      // When token is removed, user should also be cleared
      user.value = null;
      localStorage.removeItem('user');
    }
  }

  function logout() {
    user.value = null
    token.value = null
    localStorage.removeItem('user')
    localStorage.removeItem('token')
  }

  return {
    user,
    token,
    isLoggedIn,
    loginSuccess, // Expose the new login success function
    setUser, // Expose the setUser function
    // setToken, // You might not need to expose this if loginSuccess and logout are primary
    logout
  }
}) 