<template>
  <div class="login-container">
    <div class="login-left">
      <div class="login-left-content">
        <div class="logo-section">
          <el-icon class="logo-icon" :size="48"><Box /></el-icon>
          <h1 class="system-title">连锁零售门店耗材进销存管控系统</h1>
        </div>
        <div class="features">
          <div class="feature-item">
            <el-icon :size="24"><DataAnalysis /></el-icon>
            <span>智能库存预警</span>
          </div>
          <div class="feature-item">
            <el-icon :size="24"><TrendCharts /></el-icon>
            <span>实时数据分析</span>
          </div>
          <div class="feature-item">
            <el-icon :size="24"><Shop /></el-icon>
            <span>多门店协同管理</span>
          </div>
          <div class="feature-item">
            <el-icon :size="24"><Document /></el-icon>
            <span>自动化报表生成</span>
          </div>
        </div>
      </div>
    </div>
    <div class="login-right">
      <div class="login-box">
        <h2 class="title">欢迎登录</h2>
        <p class="subtitle">请输入您的账号信息</p>
        <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              size="large"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item prop="captcha">
            <div class="captcha-row">
              <el-input
                v-model="form.captcha"
                placeholder="请输入验证码"
                prefix-icon="Key"
                size="large"
                clearable
                @keyup.enter="handleLogin"
              />
              <div class="captcha-img" @click="refreshCaptcha">
                <canvas ref="captchaCanvas" width="120" height="40"></canvas>
              </div>
            </div>
          </el-form-item>
          <el-form-item>
            <div class="form-options">
              <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
              <el-link type="primary" :underline="false">忘记密码？</el-link>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>
        <div class="login-footer">
          <p>© 2024 连锁零售门店耗材进销存管控系统</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { authApi, transformLoginResult } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const captchaCanvas = ref<HTMLCanvasElement>()
const loading = ref(false)
const rememberMe = ref(false)
const captchaCode = ref('')

const form = reactive({
  username: '',
  password: '',
  captcha: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为4位字符', trigger: 'blur' }
  ]
}

const generateCaptcha = () => {
  const canvas = captchaCanvas.value
  if (!canvas) return
  
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789'
  let code = ''
  
  ctx.fillStyle = '#f0f2f5'
  ctx.fillRect(0, 0, 120, 40)
  
  for (let i = 0; i < 4; i++) {
    const char = chars[Math.floor(Math.random() * chars.length)]
    code += char
    
    ctx.font = `bold ${20 + Math.random() * 8}px Arial`
    ctx.fillStyle = `rgb(${Math.floor(Math.random() * 100)}, ${Math.floor(Math.random() * 100)}, ${Math.floor(Math.random() * 100)})`
    
    const x = 15 + i * 25
    const y = 25 + Math.random() * 10
    const rotate = (Math.random() - 0.5) * 0.4
    
    ctx.save()
    ctx.translate(x, y)
    ctx.rotate(rotate)
    ctx.fillText(char, 0, 0)
    ctx.restore()
  }
  
  for (let i = 0; i < 6; i++) {
    ctx.strokeStyle = `rgb(${Math.floor(Math.random() * 200)}, ${Math.floor(Math.random() * 200)}, ${Math.floor(Math.random() * 200)})`
    ctx.beginPath()
    ctx.moveTo(Math.random() * 120, Math.random() * 40)
    ctx.lineTo(Math.random() * 120, Math.random() * 40)
    ctx.stroke()
  }
  
  for (let i = 0; i < 30; i++) {
    ctx.fillStyle = `rgb(${Math.floor(Math.random() * 255)}, ${Math.floor(Math.random() * 255)}, ${Math.floor(Math.random() * 255)})`
    ctx.beginPath()
    ctx.arc(Math.random() * 120, Math.random() * 40, 1, 0, 2 * Math.PI)
    ctx.fill()
  }
  
  captchaCode.value = code.toLowerCase()
}

const refreshCaptcha = () => {
  generateCaptcha()
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      // if (form.captcha.toLowerCase() !== captchaCode.value) {
      //   ElMessage.error('验证码错误')
      //   refreshCaptcha()
      //   return
      // }
      
      loading.value = true
      try {
        const res = await authApi.login({
          username: form.username,
          password: form.password
        })
        
        const { token, userInfo } = transformLoginResult(res.data)
        userStore.setToken(token)
        userStore.setUserInfo(userInfo)
        
        await userStore.fetchUserInfo()
        
        if (rememberMe.value) {
          localStorage.setItem('rememberedUsername', form.username)
        } else {
          localStorage.removeItem('rememberedUsername')
        }
        
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error: any) {
        ElMessage.error(error.message || '登录失败，请检查用户名和密码')
        refreshCaptcha()
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  generateCaptcha()
  
  const rememberedUsername = localStorage.getItem('rememberedUsername')
  if (rememberedUsername) {
    form.username = rememberedUsername
    rememberMe.value = true
  }
})
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  min-height: 100vh;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1E3A8A 0%, #1E40AF 50%, #3B82F6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  
  @media (max-width: 992px) {
    display: none;
  }
}

.login-left-content {
  color: #fff;
  max-width: 500px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 60px;
}

.logo-icon {
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
  padding: 12px;
  border-radius: 12px;
}

.system-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0;
  line-height: 1.3;
}

.features {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  font-size: 15px;
  
  .el-icon {
    color: #60A5FA;
  }
}

.login-right {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #fff;
  
  @media (max-width: 576px) {
    width: 100%;
    padding: 20px;
  }
}

.login-box {
  width: 100%;
  max-width: 400px;
}

.title {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 600;
  color: #1E3A8A;
}

.subtitle {
  margin: 0 0 32px;
  font-size: 14px;
  color: #6B7280;
}

.login-form {
  .captcha-row {
    display: flex;
    gap: 12px;
    width: 100%;
    
    .el-input {
      flex: 1;
    }
    
    .captcha-img {
      cursor: pointer;
      border-radius: 6px;
      overflow: hidden;
      border: 1px solid #E5E7EB;
      transition: border-color 0.2s;
      
      &:hover {
        border-color: #1E3A8A;
      }
    }
  }
  
  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }
  
  .login-btn {
    width: 100%;
    height: 44px;
    font-size: 16px;
    font-weight: 500;
    border-radius: 8px;
  }
}

.login-footer {
  margin-top: 40px;
  text-align: center;
  
  p {
    margin: 0;
    font-size: 12px;
    color: #9CA3AF;
  }
}

:deep(.el-input__wrapper) {
  padding: 0 15px;
  height: 44px;
  border-radius: 8px;
}

:deep(.el-checkbox__label) {
  color: #6B7280;
}

:deep(.el-link--primary) {
  color: #1E3A8A;
  
  &:hover {
    color: #3B5998;
  }
}
</style>