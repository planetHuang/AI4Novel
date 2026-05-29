<template>
  <div class="home-page">
    <header class="header">
      <h1>AI4Novel - AI辅助小说写作</h1>
      <div class="header-actions">
        <button @click="router.push({ name: 'Settings' })" class="btn-secondary">⚙️ AI设置</button>
      </div>
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

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.3);
}

.main {
  max-width: 900px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.create-section, .list-section {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.create-section h2, .list-section h2 {
  margin-top: 0;
  color: #333;
}

.create-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 500;
  color: #555;
}

.form-group input, .form-group textarea {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-group textarea {
  resize: vertical;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.message {
  padding: 0.75rem;
  border-radius: 4px;
  margin-top: 1rem;
}

.message.success {
  background: #d4edda;
  color: #155724;
}

.message.error {
  background: #f8d7da;
  color: #721c24;
}

.empty {
  text-align: center;
  color: #999;
  padding: 2rem;
}

.novel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1rem;
}

.novel-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 1rem;
  cursor: pointer;
  transition: all 0.2s;
  background: #fafafa;
}

.novel-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.novel-name {
  font-weight: 600;
  font-size: 1.1rem;
  color: #333;
  margin-bottom: 0.5rem;
}

.novel-desc {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 1rem;
  line-height: 1.4;
}

.novel-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
  font-size: 0.85rem;
  color: #999;
}

.btn-delete {
  background: #f44336;
  color: white;
  border: none;
  padding: 0.25rem 0.5rem;
  border-radius: 3px;
  cursor: pointer;
  font-size: 0.8rem;
}

.novel-folders {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.folder-tag {
  background: #e3f2fd;
  color: #1976d2;
  padding: 0.2rem 0.5rem;
  border-radius: 3px;
  font-size: 0.75rem;
}
</style>
