<template>
  <div class="login-page">
    <div class="login-page__sky" aria-hidden="true">
      <span class="login-page__cloud login-page__cloud--a" />
      <span class="login-page__cloud login-page__cloud--b" />
      <span class="login-page__cloud login-page__cloud--c" />
    </div>

    <div class="login-page__inner">
      <header class="login-page__header">
        <h1 class="login-page__title">欢迎回来</h1>
        <p class="login-page__subtitle">使用用户名和密码登录</p>
      </header>

      <div class="login-page__tabs">
        <button
          type="button"
          class="login-page__tab is-active"
        >
          账号登录
        </button>
      </div>

      <section class="login-card">
        <h2 class="login-card__title">会员登录</h2>

        <van-form class="login-form" @submit="onSubmit">
          <div class="login-field">
            <van-icon name="user-o" class="login-field__icon" />
            <van-field
              v-model="form.username"
              clearable
              name="username"
              placeholder="请输入用户名"
              autocomplete="username"
              :rules="[
                { required: true, message: '用户名不能为空' }
              ]"
            />
          </div>

          <div class="login-field">
            <van-icon name="lock" class="login-field__icon" />
            <van-field
              v-model="form.password"
              type="password"
              clearable
              name="password"
              placeholder="请输入密码"
              autocomplete="current-password"
              :rules="[
                { required: true, message: '密码不能为空' }
              ]"
            />
          </div>

          <van-button
            round
            block
            :loading="loading"
            type="primary"
            loading-text="登录中..."
            native-type="submit"
            class="login-submit"
          >
            登录
          </van-button>
        </van-form>

        <van-checkbox
          v-model="checked"
          checked-color="#0071e3"
          class="login-agreement"
          icon-size="16"
        >
          已阅读并同意
          <a class="login-agreement__link" @click.stop>《用户协议》</a>
          <a class="login-agreement__link" @click.stop>《隐私协议》</a>
        </van-checkbox>

        <div class="login-register">
          <span>还没有账号？</span>

          <button
            type="button"
            class="login-register__button"
            @click="router.push({ name: 'Registry' })"
          >
            立即注册
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, toRefs } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { showNotify, showToast } from 'vant'

const router = useRouter()
const store = useStore()

const state = reactive({
  form: {
    username: '',
    password: ''
  },
  loading: false,
  checked: false
})

const { form, loading, checked } = toRefs(state)

const onSubmit = async () => {
  if (!checked.value) {
    showToast({
      type: 'fail',
      message: '请先勾选同意用户协议及隐私政策'
    })
    return
  }

  loading.value = true

  try {
    await store.dispatch('user/login', form.value)

    showNotify({
      type: 'success',
      message: '登录成功',
      duration: 1000
    })

    await router.replace({ name: 'Home' })
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  padding: 120px 32px 48px;
  overflow: hidden;
  background: linear-gradient(180deg, #eaf1ff 0%, #f4f7ff 38%, #f5f5f7 100%);
  box-sizing: border-box;
}

.login-page__sky {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.login-page__cloud {
  position: absolute;
  display: block;
  border-radius: 50%;
  filter: blur(48px);
  opacity: 0.78;
}

.login-page__cloud--a {
  top: -120px;
  right: -80px;
  width: 420px;
  height: 420px;
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.92), rgba(198, 219, 255, 0.6) 60%, transparent 75%);
}

.login-page__cloud--b {
  top: 40px;
  left: -160px;
  width: 360px;
  height: 360px;
  background: radial-gradient(circle at 70% 40%, rgba(255, 255, 255, 0.7), rgba(186, 210, 255, 0.38) 60%, transparent 75%);
}

.login-page__cloud--c {
  top: 220px;
  right: -60px;
  width: 260px;
  height: 260px;
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.75), rgba(170, 198, 255, 0.3) 60%, transparent 75%);
}

.login-page__inner {
  position: relative;
  z-index: 1;
}

.login-page__header {
  margin-bottom: 36px;
}

.login-page__title {
  margin: 0;
  font-size: 60px;
  line-height: 1.08;
  font-weight: 700;
  letter-spacing: -0.6px;
  color: #1d1d1f;
}

.login-page__subtitle {
  margin: 20px 0 0;
  font-size: 28px;
  line-height: 1.45;
  color: rgba(29, 29, 31, 0.55);
}

.login-page__tabs {
  display: flex;
  gap: 14px;
  margin-bottom: 40px;
  padding: 0;
}

.login-page__tab {
  padding: 16px 28px;
  border: 0;
  background: transparent;
  font-size: 28px;
  line-height: 1;
  font-weight: 500;
  color: rgba(29, 29, 31, 0.52);
  border-radius: 999px;
  transition: background 160ms ease, color 160ms ease, box-shadow 160ms ease;
  cursor: pointer;
}

.login-page__tab.is-active {
  background: #ffffff;
  color: #0071e3;
  font-weight: 600;
  box-shadow: 0 8px 22px rgba(64, 124, 220, 0.14);
}

.login-card {
  position: relative;
  padding: 44px 32px 36px;
  border-radius: 36px;
  background: #ffffff;
  box-shadow: 0 24px 60px rgba(31, 65, 135, 0.12);
}

.login-card__title {
  margin: 0 0 30px;
  font-size: 36px;
  line-height: 1.1;
  font-weight: 700;
  color: #1d1d1f;
  letter-spacing: -0.2px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.login-field {
  position: relative;
  display: flex;
  align-items: center;
  background: #f3f5f9;
  border-radius: 24px;
  padding-left: 28px;
  transition: background 160ms ease, box-shadow 160ms ease;
}

.login-field:focus-within {
  background: #ffffff;
  box-shadow: inset 0 0 0 2px rgba(0, 113, 227, 0.35);
}

.login-field__icon {
  flex: 0 0 auto;
  font-size: 36px;
  color: rgba(29, 29, 31, 0.45);
  margin-right: 12px;
}

.login-field :deep(.van-cell) {
  flex: 1;
  padding: 28px 24px 28px 0;
  background: transparent;
  font-size: 30px;
  line-height: 1.2;
}

.login-field :deep(.van-cell:after) {
  display: none;
}

.login-field :deep(.van-field__control) {
  color: #1d1d1f;
}

.login-field :deep(.van-field__control::placeholder) {
  color: rgba(29, 29, 31, 0.4);
}

.login-field__code {
  padding: 0 4px;
  font-size: 26px;
  line-height: 1;
  font-weight: 600;
  color: #0071e3;
  white-space: nowrap;
  cursor: pointer;
}

.login-field__code.is-disabled {
  color: rgba(29, 29, 31, 0.4);
  cursor: default;
}

.login-submit {
  margin-top: 18px;
  height: 92px;
  font-size: 32px;
  font-weight: 600;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #0a84ff 0%, #0071e3 100%);
  border: none;
  box-shadow: 0 16px 36px rgba(10, 132, 255, 0.28);
}

.login-submit:active {
  opacity: 0.92;
}

.login-agreement {
  margin-top: 28px;
  padding: 0 4px;
  align-items: center;
}

.login-agreement :deep(.van-checkbox__label) {
  color: rgba(29, 29, 31, 0.58);
  font-size: 24px;
  line-height: 1.4;
  white-space: normal;
  margin-left: 8px;
}

.login-agreement__link {
  color: #0071e3;
}

@media (max-width: 360px) {
  .login-page {
    padding: 96px 24px 40px;
  }

  .login-page__title {
    font-size: 52px;
  }
}
.login-register {
  margin-top: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: rgba(29, 29, 31, 0.56);
  font-size: 28px;
}

.login-register__button {
  padding: 0;
  border: none;
  background: transparent;
  color: #0071e3;
  font-size: 28px;
  font-weight: 600;
}
</style>
