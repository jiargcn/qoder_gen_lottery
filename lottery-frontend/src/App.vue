<template>
  <div class="app-wrapper">
    <header class="global-header">
      <div class="company-name">
        {{ companyName }}
      </div>
    </header>
    <router-view />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const companyName = ref('年会抽奖系统')

const syncCompanyName = () => {
  const tenantName = localStorage.getItem('tenantName')
  if (tenantName) {
    companyName.value = tenantName
  } else {
    companyName.value = '年会抽奖系统'
  }
}

onMounted(() => {
  syncCompanyName()
  window.addEventListener('storage', syncCompanyName)
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: "Microsoft YaHei", Arial, sans-serif;
}

#app {
  min-height: 100vh;
}

.app-wrapper {
  min-height: 100vh;
}

.global-header {
  width: 100%;
  height: 50px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: #20232a;
  color: #fff;
  font-size: 18px;
}

.company-name {
  font-weight: bold;
}
</style>