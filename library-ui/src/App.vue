<script setup>
// App.vue is the root component, it should only contain the router-view
</script>

<template>
  <!-- Router view will render the component for the current route -->
  <router-view></router-view>
</template>

<style>
/* Global styles */
.main-content-container {
  width: 1400px;
  margin: 0 auto;
  padding: 20px 40px;
  min-height: calc(100vh - 40px);
  box-sizing: border-box;
  position: relative;
}

/* Optional: Add some basic body styles */
body {
  margin: 0;
  padding: 0;
  background-color: #f5f7fa;
  min-height: 100vh;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Common card styles */
.el-card {
  margin-bottom: 20px;
  width: 100%;
}

/* Common form styles */
.el-form {
  width: 100%;
  margin: 0 auto;
}

/* Common table styles */
.el-table {
  width: 100% !important;
  margin-bottom: 20px;
}

/* Common pagination styles */
.el-pagination {
  margin-top: 20px;
  justify-content: center;
}

/* Disable all transitions */
* {
  transition: none !important;
}
</style>
