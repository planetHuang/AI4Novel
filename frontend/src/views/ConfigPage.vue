<template>
  <div class="config-page">
    <header class="header">
      <h1>⚙️ AI 配置</h1>
      <div class="header-actions">
        <button @click="router.push({ name: 'Home' })" class="btn-secondary">返回首页</button>
      </div>
    </header>

    <main class="main">
      <section class="create-section">
        <h2>添加新配置</h2>
        <form @submit.prevent="createConfig" class="create-form">
          <div class="form-group">
            <label for="name">配置名称</label>
            <input
              id="name"
              v-model="form.name"
              type="text"
              placeholder="例如：GPT-3.5"
              required
            />
          </div>
          <div class="form-group">
            <label for="apiUrl">API URL</label>
            <input
              id="apiUrl"
              v-model="form.apiUrl"
              type="text"
              placeholder="例如：https://api.openai.com/v1/chat/completions"
              required
            />
          </div>
          <div class="form-group">
            <label for="apiKey">API KEY</label>
            <input
              id="apiKey"
              v-model="form.apiKey"
              type="password"
              placeholder="请输入API KEY"
              required
            />
          </div>
          <div class="form-group">
            <label for="model">模型名称</label>
            <input
              id="model"
              v-model="form.model"
              type="text"
              placeholder="例如：deepseek-v4-pro / gpt-4"
              required
            />
          </div>
          <div class="form-group">
            <label for="thinkingEnabled">
              <input
                id="thinkingEnabled"
                v-model="form.thinkingEnabled"
                type="checkbox"
              />
              启用深度思考 (DeepSeek Thinking)
            </label>
          </div>
          <div class="form-group" v-if="form.thinkingEnabled">
            <label for="reasoningEffort">推理强度</label>
            <select id="reasoningEffort" v-model="form.reasoningEffort">
              <option value="low">低</option>
              <option value="medium">中</option>
              <option value="high">高</option>
            </select>
          </div>
          <button type="submit" :disabled="loading" class="btn-primary">
            {{ loading ? '添加中...' : '添加配置' }}
          </button>
        </form>
        <div v-if="message" :class="['message', messageType]">
          {{ message }}
        </div>
      </section>

      <section class="list-section">
        <h2>我的配置</h2>
        <div v-if="configs.length === 0" class="empty">暂无AI配置，请添加你的第一个配置</div>
        <div v-else class="config-list">
          <div v-for="config in configs" :key="config.id" class="config-card">
            <div class="config-info">
              <div class="config-name">
                {{ config.name }}
                <span v-if="config.isDefault" class="default-badge">默认</span>
              </div>
              <div class="config-url">{{ maskUrl(config.apiUrl) }}</div>
              <div class="config-meta">
                <span class="config-key">API KEY: {{ maskKey(config.apiKey) }}</span>
                <span>{{ formatTime(config.createTime) }}</span>
              </div>
            </div>
            <div class="config-actions">
              <button
                v-if="!config.isDefault"
                @click="setDefault(config.id)"
                class="btn-small btn-default"
              >设为默认</button>
              <button @click="editConfig(config)" class="btn-small btn-edit">编辑</button>
              <button @click="deleteConfig(config.id)" class="btn-small btn-delete">删除</button>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 编辑弹窗 -->
    <div v-if="editingConfig" class="modal-overlay" @click="closeEditModal">
      <div class="modal" @click.stop>
        <h3>编辑配置</h3>
        <form @submit.prevent="updateConfig">
          <div class="form-group">
            <label>配置名称</label>
            <input v-model="editForm.name" type="text" required />
          </div>
          <div class="form-group">
            <label>API URL</label>
            <input v-model="editForm.apiUrl" type="text" required />
          </div>
          <div class="form-group">
            <label>API KEY</label>
            <input v-model="editForm.apiKey" type="password" required />
          </div>
          <div class="form-group">
            <label>模型名称</label>
            <input v-model="editForm.model" type="text" placeholder="例如：deepseek-v4-pro" required />
          </div>
          <div class="form-group">
            <label>
              <input v-model="editForm.thinkingEnabled" type="checkbox" />
              启用深度思考 (DeepSeek Thinking)
            </label>
          </div>
          <div class="form-group" v-if="editForm.thinkingEnabled">
            <label>推理强度</label>
            <select v-model="editForm.reasoningEffort">
              <option value="low">低</option>
              <option value="medium">中</option>
              <option value="high">高</option>
            </select>
          </div>
          <div class="modal-actions">
            <button type="button" @click="closeEditModal" class="btn-secondary">取消</button>
            <button type="submit" :disabled="loading" class="btn-primary">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const configs = ref([])
const loading = ref(false)
const message = ref('')
const messageType = ref('success')
const editingConfig = ref(null)

const form = ref({
  name: '',
  apiUrl: '',
  apiKey: '',
  model: '',
  thinkingEnabled: false,
  reasoningEffort: 'high'
})

const editForm = ref({
  id: '',
  name: '',
  apiUrl: '',
  apiKey: '',
  model: '',
  thinkingEnabled: false,
  reasoningEffort: 'high'
})

async function fetchConfigs() {
  try {
    const res = await fetch('/api/ai-configs')
    const data = await res.json()
    if (data.code === 0) {
      configs.value = data.data
    }
  } catch (e) {
    console.error('获取配置列表失败', e)
  }
}

async function createConfig() {
  if (!form.value.name.trim() || !form.value.apiUrl.trim() || !form.value.apiKey.trim() || !form.value.model.trim()) return
  loading.value = true
  message.value = ''
  try {
    const res = await fetch('/api/ai-configs', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: form.value.name.trim(),
        apiUrl: form.value.apiUrl.trim(),
        apiKey: form.value.apiKey.trim(),
        model: form.value.model.trim(),
        thinkingEnabled: form.value.thinkingEnabled ? 'true' : 'false',
        reasoningEffort: form.value.thinkingEnabled ? form.value.reasoningEffort : ''
      })
    })
    const data = await res.json()
    if (data.code === 0) {
      message.value = `配置「${data.data.name}」创建成功！`
      messageType.value = 'success'
      form.value = { name: '', apiUrl: '', apiKey: '', model: '', thinkingEnabled: false, reasoningEffort: 'high' }
      await fetchConfigs()
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

function editConfig(config) {
  editingConfig.value = config
  editForm.value = {
    id: config.id,
    name: config.name,
    apiUrl: config.apiUrl,
    apiKey: config.apiKey,
    model: config.model || '',
    thinkingEnabled: config.thinkingEnabled || false,
    reasoningEffort: config.reasoningEffort || 'high'
  }
}

function closeEditModal() {
  editingConfig.value = null
  editForm.value = { id: '', name: '', apiUrl: '', apiKey: '', model: '', thinkingEnabled: false, reasoningEffort: 'high' }
}

async function updateConfig() {
  if (!editForm.value.name.trim() || !editForm.value.apiUrl.trim() || !editForm.value.apiKey.trim() || !editForm.value.model.trim()) return
  loading.value = true
  try {
    const res = await fetch(`/api/ai-configs/${editForm.value.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name: editForm.value.name.trim(),
        apiUrl: editForm.value.apiUrl.trim(),
        apiKey: editForm.value.apiKey.trim(),
        model: editForm.value.model.trim(),
        thinkingEnabled: editForm.value.thinkingEnabled ? 'true' : 'false',
        reasoningEffort: editForm.value.thinkingEnabled ? editForm.value.reasoningEffort : ''
      })
    })
    const data = await res.json()
    if (data.code === 0) {
      closeEditModal()
      await fetchConfigs()
    } else {
      alert(data.message)
    }
  } catch (e) {
    alert('请求失败')
  } finally {
    loading.value = false
  }
}

async function setDefault(id) {
  try {
    const res = await fetch(`/api/ai-configs/${id}/set-default`, {
      method: 'PUT'
    })
    const data = await res.json()
    if (data.code === 0) {
      await fetchConfigs()
    } else {
      alert(data.message)
    }
  } catch (e) {
    alert('请求失败')
  }
}

async function deleteConfig(id) {
  if (!confirm('确定要删除这个配置吗？')) return
  try {
    const res = await fetch(`/api/ai-configs/${id}`, { method: 'DELETE' })
    const data = await res.json()
    if (data.code === 0) {
      await fetchConfigs()
    } else {
      alert(data.message)
    }
  } catch (e) {
    alert('删除失败')
  }
}

function maskUrl(url) {
  if (url.length > 50) {
    return url.substring(0, 50) + '...'
  }
  return url
}

function maskKey(key) {
  if (key.length <= 8) {
    return '********'
  }
  return key.substring(0, 4) + '***' + key.substring(key.length - 4)
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchConfigs()
})
</script>

<style scoped>
.config-page {
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

.header-actions button {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
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

.form-group input {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.form-group select {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  background: white;
  width: 100%;
}

.form-group select:focus {
  outline: none;
  border-color: #667eea;
}

.form-group input[type="checkbox"] {
  width: auto;
  padding: 0;
  margin-right: 0.5rem;
  cursor: pointer;
}

.form-group label:has(input[type="checkbox"]) {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
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

.btn-secondary {
  background: #f0f0f0;
  color: #333;
  border: 1px solid #ddd;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
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

.config-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.config-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: #fafafa;
}

.config-info {
  flex: 1;
}

.config-name {
  font-weight: 600;
  font-size: 1.1rem;
  color: #333;
  margin-bottom: 0.25rem;
}

.default-badge {
  background: #4caf50;
  color: white;
  padding: 0.15rem 0.5rem;
  border-radius: 3px;
  font-size: 0.8rem;
  margin-left: 0.5rem;
}

.config-url {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 0.25rem;
}

.config-meta {
  color: #999;
  font-size: 0.85rem;
}

.config-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-small {
  padding: 0.4rem 0.8rem;
  border-radius: 4px;
  font-size: 0.85rem;
  cursor: pointer;
}

.btn-default {
  background: #4caf50;
  color: white;
  border: none;
}

.btn-edit {
  background: #2196f3;
  color: white;
  border: none;
}

.btn-delete {
  background: #f44336;
  color: white;
  border: none;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  width: 400px;
  max-width: 90%;
}

.modal h3 {
  margin-top: 0;
}

.modal form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 1rem;
}
</style>