<template>
  <div class="sidebar-right">
    <div class="chat-title">AI 写作助手</div>

    <!-- 对话标题 -->
    <div class="conv-title-bar">
      <template v-if="!editingTitle">
        <span class="conv-title-text" @click="startRename" :title="currentConversationId ? '点击编辑标题' : ''">
          {{ currentConversationTitle || '暂无对话' }}
        </span>
        <button v-if="currentConversationId" @click="startRename" title="编辑标题" class="btn-conv-edit">✎</button>
      </template>
      <template v-else>
        <input
          v-model="newTitle"
          @keyup.enter="confirmRename"
          @keyup.escape="cancelRename"
          class="conv-title-input"
          placeholder="输入对话标题"
          ref="titleInput"
        />
        <button @click="confirmRename" class="btn-conv-ok">✓</button>
        <button @click="cancelRename" class="btn-conv-cancel">✗</button>
      </template>
    </div>

    <!-- 对话切换操作 -->
    <div class="conv-actions">
      <select v-model="currentConversationId" @change="switchConversation" class="conv-select">
        <option v-for="conv in conversations" :key="conv.id" :value="conv.id">{{ conv.title }}</option>
      </select>
      <button @click="createNewConversation" title="新建对话" class="btn-conv-new">+ 新建</button>
      <button @click="deleteCurrentConversation" title="删除当前对话" class="btn-conv-del" :disabled="!currentConversationId">删除</button>
    </div>

    <AiSelector v-model="selectedAiId" @change="handleAiChange" />

    <div class="chat-messages" ref="chatContainer">
      <div v-if="messages.length === 0 && !sending" class="chat-empty">
        点击「+」新建对话，开始与AI交流
      </div>
      <div v-for="(msg, i) in messages" :key="i" :class="['chat-msg', msg.role]">
        <div class="msg-role">{{ msg.role === 'user' ? '你' : 'AI' }}</div>
        <div class="msg-content">{{ msg.content }}</div>
        <button
          v-if="msg.role === 'assistant' && !sending"
          class="btn-send-to-editor"
          @click="emit('send-to-editor', msg.content)"
        >发送到编辑器</button>
      </div>
      <div v-if="sending" class="chat-msg assistant">
        <div class="msg-role">AI</div>
        <div class="msg-content">正在思考...</div>
      </div>
    </div>
    <div class="chat-input-area">
      <div class="file-tags" v-if="selectedFiles.length > 0">
        <span v-for="(f, idx) in selectedFiles" :key="f" class="file-tag">
          {{ f }}
          <button class="file-tag-remove" @click="removeFile(idx)" title="移除">&times;</button>
        </span>
      </div>
      <textarea
        v-model="inputText"
        @keyup.enter.exact="sendMessage"
        placeholder="输入消息，Enter 发送 | 支持 '阅读 章节/xxx.txt' 引用项目文件"
        rows="3"
      ></textarea>
      <div class="chat-input-actions">
        <button @click="showFilePicker = true" title="选择项目文件" class="btn-pick-file">选择文件</button>
        <button @click="sendMessage" :disabled="(!inputText.trim() && selectedFiles.length === 0) || sending">发送</button>
      </div>
    </div>
    <FilePickerDialog
      :show="showFilePicker"
      :tree="tree"
      @confirm="onFilesPicked"
      @cancel="showFilePicker = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import AiSelector from './AiSelector.vue'
import FilePickerDialog from './FilePickerDialog.vue'

const emit = defineEmits(['send-to-editor'])

const props = defineProps({
  novelId: { type: String, required: true },
  tree: { type: Object, default: null }
})

const selectedAiId = ref('')

const messages = ref([])
const conversations = ref([])
const currentConversationId = ref('')
const inputText = ref('')
const sending = ref(false)
const chatContainer = ref(null)
const showFilePicker = ref(false)
const selectedFiles = ref([])
const editingTitle = ref(false)
const newTitle = ref('')
const titleInput = ref(null)

const currentConversationTitle = computed(() => {
  const conv = conversations.value.find(c => c.id === currentConversationId.value)
  return conv ? conv.title : ''
})

onMounted(async () => {
  await loadConversations()
})

async function loadConversations() {
  try {
    const res = await fetch(`/api/novels/${props.novelId}/conversations`)
    const data = await res.json()
    if (data.code === 0) {
      conversations.value = data.data
      if (data.data.length > 0) {
        currentConversationId.value = data.data[0].id
        await loadMessages(data.data[0].id)
      }
    }
  } catch (e) {
    console.error('加载对话列表失败', e)
  }
}

async function loadMessages(convId) {
  try {
    const res = await fetch(`/api/novels/${props.novelId}/conversations/${convId}`)
    const data = await res.json()
    if (data.code === 0) {
      messages.value = data.data.messages.map(m => ({
        role: m.role,
        content: m.content
      }))
    }
  } catch (e) {
    console.error('加载消息失败', e)
  }
}

async function switchConversation() {
  if (currentConversationId.value) {
    await loadMessages(currentConversationId.value)
  }
}

async function createNewConversation() {
  try {
    const res = await fetch(`/api/novels/${props.novelId}/conversations`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title: '新对话' })
    })
    const data = await res.json()
    if (data.code === 0) {
      conversations.value.unshift(data.data)
      currentConversationId.value = data.data.id
      messages.value = []
    }
  } catch (e) {
    console.error('创建对话失败', e)
  }
}

async function deleteCurrentConversation() {
  if (!currentConversationId.value) return
  if (!confirm('确定要删除当前对话吗？')) return
  try {
    const res = await fetch(`/api/novels/${props.novelId}/conversations/${currentConversationId.value}`, {
      method: 'DELETE'
    })
    const data = await res.json()
    if (data.code === 0) {
      conversations.value = conversations.value.filter(c => c.id !== currentConversationId.value)
      if (conversations.value.length > 0) {
        currentConversationId.value = conversations.value[0].id
        await loadMessages(conversations.value[0].id)
      } else {
        currentConversationId.value = ''
        messages.value = []
      }
    }
  } catch (e) {
    console.error('删除对话失败', e)
  }
}

function onFilesPicked(paths) {
  showFilePicker.value = false
  for (const p of paths) {
    if (!selectedFiles.value.includes(p)) {
      selectedFiles.value.push(p)
    }
  }
}

function removeFile(idx) {
  selectedFiles.value.splice(idx, 1)
}

async function startRename() {
  if (!currentConversationId.value) return
  const conv = conversations.value.find(c => c.id === currentConversationId.value)
  newTitle.value = conv ? conv.title : ''
  editingTitle.value = true
  await nextTick()
  if (titleInput.value) titleInput.value.focus()
}

async function confirmRename() {
  const title = newTitle.value.trim()
  if (!title || !currentConversationId.value) return
  try {
    const res = await fetch(`/api/novels/${props.novelId}/conversations/${currentConversationId.value}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title })
    })
    const data = await res.json()
    if (data.code === 0) {
      const conv = conversations.value.find(c => c.id === currentConversationId.value)
      if (conv) conv.title = title
    }
  } catch (e) {
    console.error('重命名失败', e)
  }
  editingTitle.value = false
}

function cancelRename() {
  editingTitle.value = false
}

function handleAiChange(config) {}

async function sendMessage() {
  const text = inputText.value.trim()
  if ((!text && selectedFiles.value.length === 0) || sending.value) return

  const displayText = text || (selectedFiles.value.length > 0 ? '请根据引用的文件内容回答' : '')
  messages.value.push({ role: 'user', content: displayText })
  const filePathsToSend = [...selectedFiles.value]
  inputText.value = ''
  selectedFiles.value = []
  sending.value = true
  await scrollToBottom()

  try {
    const body = {
      messages: messages.value,
      aiConfigId: selectedAiId.value,
      conversationId: currentConversationId.value || undefined
    }
    if (filePathsToSend.length > 0) {
      body.filePaths = filePathsToSend
    }
    const res = await fetch(`/api/novels/${props.novelId}/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
    const data = await res.json()
    if (data.code === 0) {
      messages.value.push({ role: 'assistant', content: data.data.reply })
      // 如果是新创建的对话，刷新列表并更新ID
      if (data.data.conversationId && !currentConversationId.value) {
        currentConversationId.value = data.data.conversationId
        await loadConversations()
      }
    } else {
      messages.value.push({ role: 'assistant', content: '错误: ' + data.message })
    }
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '请求失败，请检查后端服务。' })
  } finally {
    sending.value = false
    await scrollToBottom()
  }
}

async function scrollToBottom() {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}
</script>
