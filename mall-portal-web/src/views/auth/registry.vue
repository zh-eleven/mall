<template>
  <div class="registration wb-form-shell">
    <nav-bar :title="$route.meta.title" />

    <div class="registration__panel wb-form-panel">
      <div class="registration__title wb-form-title">注册</div>
      <div class="registration__subtitle wb-form-subtitle">
        创建商城会员账号
      </div>

      <van-form
        class="form"
        validate-trigger="onSubmit"
        @submit="onSubmit"
      >
        <van-field
          v-model="form.username"
          required
          clearable
          name="username"
          label="用户名"
          placeholder="请输入4～32位用户名"
          :rules="[
            { required: true, message: '请输入用户名' },
            {
              validator: value =>
                value.length >= 4 && value.length <= 32,
              message: '用户名长度必须在4～32位'
            }
          ]"
        />

        <van-field
          v-model="form.phone"
          clearable
          name="phone"
          label="手机号"
          placeholder="选填"
        />

        <van-field
          v-model="form.email"
          type="email"
          clearable
          name="email"
          label="邮箱"
          placeholder="选填"
          :rules="[
            {
              validator: value =>
                !value || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value),
              message: '邮箱格式错误'
            }
          ]"
        />

        <van-field
          v-model="form.password"
          type="password"
          required
          clearable
          name="password"
          label="密码"
          placeholder="请输入6～32位密码"
          :rules="[
            { required: true, message: '请输入密码' },
            {
              validator: value =>
                value.length >= 6 && value.length <= 32,
              message: '密码长度必须在6～32位'
            }
          ]"
        />

        <van-field
          v-model="form.confirmPassword"
          type="password"
          required
          clearable
          name="confirmPassword"
          label="确认密码"
          placeholder="请再次输入密码"
          :rules="[
            { required: true, message: '请确认密码' }
          ]"
        />

        <div class="registration__submit">
          <van-button
            round
            block
            :loading="loading"
            type="primary"
            loading-text="注册中..."
            native-type="submit"
          >
            注册
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, toRefs } from 'vue'
import { useRouter } from 'vue-router'
import { showNotify, showToast } from 'vant'

import { setRegistry } from '@/api/user'

const router = useRouter()

const state = reactive({
  form: {
    username: '',
    phone: '',
    email: '',
    password: '',
    confirmPassword: ''
  },
  loading: false
})

const { form, loading } = toRefs(state)

const onSubmit = async () => {
  if (form.value.password !== form.value.confirmPassword) {
    showToast({
      type: 'fail',
      message: '两次输入的密码不一致'
    })
    return
  }

  loading.value = true

  try {
    await setRegistry(form.value)

    showNotify({
      type: 'success',
      message: '注册成功，请登录'
    })

    await router.replace({
      name: 'Login'
    })
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>
<style lang="scss" scoped>
.registration__panel {
  padding-bottom: 36px;
}

.registration__title {
  padding-bottom: 18px;
}

.registration__subtitle {
  padding-bottom: 40px;
}

.form {
  :deep(.van-cell) {
    padding: 24px 0;
    background: transparent;
    border-bottom: 1px solid rgba(29, 29, 31, 0.08);
  }

  :deep(.van-cell:after) {
    display: none;
  }
}

.registration__submit {
  margin-top: 36px;
}

.regist-code {
  height: 40px;

  img {
    vertical-align: middle;
    position: relative;
    top: -18.5px;
  }
}
</style>
