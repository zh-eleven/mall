<template>
  <div class="user-setting wb-page">
    <nav-bar :title="$route.meta.title" />

    <main class="user-setting__body">
      <section class="user-setting__profile">
        <div class="user-setting__avatar">
          <img
            v-if="userInfo.avatar"
            :src="userInfo.avatar"
            alt="用户头像"
          />
          <span v-else class="user-setting__avatar-fallback">
            {{ avatarFallback }}
          </span>
        </div>

        <div class="user-setting__profile__copy">
          <span class="user-setting__profile__label">头像</span>
          <strong class="user-setting__profile__name">
            {{ userInfo.nickname || userInfo.username || '未设置昵称' }}
          </strong>
        </div>

        <span class="user-setting__profile__action">
          暂不支持上传
        </span>
      </section>

      <section class="user-setting__panel">
        <van-cell-group>
          <van-cell
            title="昵称"
            to="/userSetting/nickname"
            :value="userInfo.nickname || '未设置'"
            is-link
          />

          <van-cell
            title="性别"
            :value="genderText"
            is-link
            @click="showGender = true"
          />

          <van-cell
            title="生日"
            :value="userInfo.birthday || '未设置'"
            is-link
            @click="showBirthday = true"
          />

          <van-cell
            title="手机号"
            to="/userSetting/mobile"
            :value="userInfo.phone || '未设置'"
            is-link
          />

          <van-cell
            title="邮箱"
            to="/userSetting/email"
            :value="userInfo.email || '未设置'"
            is-link
          />

          <van-cell
            title="登录密码"
            to="/userSetting/password"
            value="修改密码"
            is-link
          />
        </van-cell-group>
      </section>
    </main>

    <van-popup
      v-model:show="showBirthday"
      round
      position="bottom"
    >
      <van-date-picker
        v-model="currentDate"
        title="选择年月日"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="confirmBirthday"
        @cancel="showBirthday = false"
      />
    </van-popup>

    <van-popup
      v-model:show="showGender"
      round
      position="bottom"
    >
      <van-picker
        show-toolbar
        :columns="columns"
        @cancel="showGender = false"
        @confirm="onConfirmGender"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { computed, reactive, toRefs, watch } from 'vue'
import { useStore } from 'vuex'
import dayjs from 'dayjs'
import { showToast } from 'vant'

import { profile } from '@/api/user'

const store = useStore()

const state = reactive({
  showBirthday: false,
  showGender: false,
  columns: [
    {
      text: '男',
      value: 1
    },
    {
      text: '女',
      value: 2
    }
  ],
  minDate: new Date(1960, 0, 1),
  maxDate: new Date(),
  currentDate: []
})

const {
  showBirthday,
  showGender,
  columns,
  minDate,
  maxDate,
  currentDate
} = toRefs(state)

const userInfo = computed(() => {
  return store.getters.userInfo || {}
})

const avatarFallback = computed(() => {
  const name =
    userInfo.value.nickname ||
    userInfo.value.username ||
    'U'

  return String(name).slice(0, 1).toUpperCase()
})

const genderText = computed(() => {
  const gender = Number(userInfo.value.gender)

  if (gender === 1) {
    return '男'
  }

  if (gender === 2) {
    return '女'
  }

  return '未设置'
})

watch(
  () => userInfo.value.birthday,
  birthday => {
    let targetDate = dayjs()

    if (birthday) {
      const parsedDate = dayjs(birthday, 'YYYY-MM-DD')

      if (parsedDate.isValid()) {
        targetDate = parsedDate
      }
    }

    currentDate.value = [
      targetDate.format('YYYY'),
      targetDate.format('MM'),
      targetDate.format('DD')
    ]
  },
  {
    immediate: true
  }
)

const refreshUserInfo = async () => {
  await store.dispatch('user/getInfo')
}

const confirmBirthday = async ({ selectedValues }) => {
  const birthday = selectedValues.join('-')

  try {
    await profile({
      birthday
    })

    await refreshUserInfo()

    showToast({
      type: 'success',
      message: '生日修改成功'
    })

    showBirthday.value = false
  } catch (error) {
    showToast({
      type: 'fail',
      message: error?.message || '生日修改失败'
    })
  }
}

const onConfirmGender = async ({ selectedOptions = [] }) => {
  const gender = selectedOptions[0]?.value

  if (gender === undefined) {
    showGender.value = false
    return
  }

  try {
    await profile({
      gender
    })

    await refreshUserInfo()

    showToast({
      type: 'success',
      message: '性别修改成功'
    })

    showGender.value = false
  } catch (error) {
    showToast({
      type: 'fail',
      message: error?.message || '性别修改失败'
    })
  }
}
</script>

<style lang="scss" scoped>
.user-setting {
  min-height: 100vh;
  background: linear-gradient(
      180deg,
      #f5f5f7 0%,
      #ffffff 50%,
      #f5f5f7 100%
  );
}

.user-setting__body {
  width: 100%;
  max-width: var(--wb-content-width);
  margin: 0 auto;
  padding: 18px 18px 36px;
}

.user-setting__profile {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 18px;
  padding: 24px;
  border-radius: 30px;
  background: #ffffff;
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.06);
}

.user-setting__avatar {
  position: relative;
  width: 112px;
  height: 112px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 50%;
  background: rgba(0, 113, 227, 0.08);
  box-shadow: inset 0 0 0 1px rgba(29, 29, 31, 0.06);
}

.user-setting__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-setting__avatar-fallback {
  font-size: 40px;
  line-height: 1;
  font-weight: 600;
  color: #0071e3;
}

.user-setting__profile__copy {
  min-width: 0;
}

.user-setting__profile__label {
  display: block;
  font-size: 28px;
  line-height: 1.2;
  color: rgba(29, 29, 31, 0.5);
}

.user-setting__profile__name {
  display: block;
  margin-top: 8px;
  overflow: hidden;
  color: #1d1d1f;
  font-size: 36px;
  line-height: 1.18;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-setting__profile__action {
  flex: none;
  padding: 10px 18px;
  border-radius: 999px;
  background: #f5f5f7;
  color: rgba(29, 29, 31, 0.5);
  font-size: 24px;
  line-height: 1.2;
  font-weight: 600;
}

.user-setting__panel {
  margin-top: 18px;
  overflow: hidden;
  border-radius: 30px;
  background: #ffffff;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}

:deep(.van-cell-group) {
  background: transparent;
}

:deep(.van-cell) {
  align-items: center;
  padding: 26px 24px;
  background: #ffffff;
}

:deep(.van-cell::after) {
  right: 24px;
  left: 24px;
  border-color: rgba(29, 29, 31, 0.08);
}

:deep(.van-cell__title) {
  color: #1d1d1f;
  font-size: 30px;
  line-height: 1.25;
  font-weight: 600;
}

:deep(.van-cell__value) {
  color: rgba(29, 29, 31, 0.56);
  font-size: 28px;
  line-height: 1.25;
}

:deep(.van-cell__right-icon) {
  color: rgba(29, 29, 31, 0.34);
  font-size: 32px;
}

:deep(.van-popup) {
  background: #ffffff;
}

:deep(.van-picker__toolbar),
:deep(.van-picker-column__item),
:deep(.van-picker__confirm),
:deep(.van-picker__cancel) {
  font-size: 28px;
}

@media (max-width: 375px) {
  .user-setting__body {
    padding-right: 16px;
    padding-left: 16px;
  }

  .user-setting__profile {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .user-setting__profile__action {
    grid-column: 2;
    justify-self: start;
  }
}
</style>
