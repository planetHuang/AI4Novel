<template>
  <div class="home-page">
    <header class="header">
      <h1>AI4Novel - AI辅助小说写作</h1>
    </header>

    <main class="main">
      <section class="create-section">
        <h2>创建新小说</h2>
        <form @submit.prevent="createNovel" class="create-form">
          <div class="form-group">
            <label for="name">小说名称</label>
            <input
              id="name"
              v-model="form.name"
              type="text"
              placeholder="请输入小说名称"
              required
            />
          </div>
          <div class="form-group">
            <label for="desc">小说简介</label>
            <textarea
              id="desc"
              v-model="form.description"
              placeholder="请输入小说简介（可选）"
              rows="3"
            ></textarea>
          </div>
          <button type="submit" :disabled="loading" class="btn-primary">
            {{ loading ? '创建中...' : '创建小说' }}
          </button>
        </form>
        <div v-if="message" :class="['message', messageType]">
          {{ message }}
        </div>
      </section>

      <section class="list-section">
        <h2>我的小说</h2>
        <div v-if="novels.length === 0" class="empty">暂无小说，请创建你的第一本小说吧</div>
        <div v-else class="novel-grid">
          <div
            v-for="novel in novels"
            :key="novel.id"
            class="novel-card"
            @click="openNovel(novel.id)"
          >
            <div class="novel-name">{{ novel.name }}</div>
            <div class="novel-desc" v-if="novel.description">{{ novel.description }}</div>
            <div class="novel-meta">
              <span>{{ formatTime(novel.createTime) }}</span>
              <button @click.stop="deleteNovel(novel.id)" class="btn-delete">删除</button>
            </div>
            <div class="novel-folders">
              <span class="folder-tag" v-for="folder in subFolders" :key="folder">{{ folder }}</span>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const subFolders = ['人物设定', '剧情走向设定', '世界观设定', '伏笔', '章节']

const novels = ref([])
const loading = ref(false)
const message = ref('')
const messageType = ref('success')

const form = ref({
  name: '',
  description: ''
})

function openNovel(id) {
  router.push({ name: 'Workspace', params: { id } })
}

async function fetchNovels() {
  try {
    const res = await fetch('/api/novels')
    const data = await res.json()
    if (data.code === 0) {
      novels.value = data.data
    }
  } catch (e) {
    console.error('获取小说列表失败', e)
  }
}

async function createNovel() {
  if (!form.value.name.trim()) return
  loading.value = true
  message.value = ''
  try {
    const res = await fetch('/api/novels', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: form.value.name.trim(),
        description: form.value.description.trim()
      })
    })
    const data = await res.json()
    if (data.code === 0) {
      message.value = `小说「${data.data.name}」创建成功！文件夹已自动生成。`
      messageType.value = 'success'
      form.value = { name: '', description: '' }
      await fetchNovels()
    } else {
      message.value = data.message
      messageType.value = 'error'
    }
  } catch (e) {
    message.value = '请求失败，请检查后端服务是否启动'
    messageType.value = 'error'
  } finally {
    loading.value = false
  }
}

async function deleteNovel(id) {
  if (!confirm('确定要删除这本小说吗？文件夹中的内容也将被删除。')) return
  try {
    const res = await fetch(`/api/novels/${id}`, { method: 'DELETE' })
    const data = await res.json()
    if (data.code === 0) {
      await fetchNovels()
    }
  } catch (e) {
    console.error('删除失败', e)
  }
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchNovels()
})
</script>
