<template>
  <div class="sidebar-right">
    <div class="chat-title">AI 写作助手</div>
    <AiSelector v-model="selectedAiId" @change="handleAiChange" />
    <div class="chat-messages" ref="chatContainer">
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
import { ref, nextTick } from 'vue'
import AiSelector from './AiSelector.vue'
import FilePickerDialog from './FilePickerDialog.vue'

const emit = defineEmits(['send-to-editor'])

const props = defineProps({
  novelId: { type: String, required: true },
  tree: { type: Object, default: null }
})

const selectedAiId = ref('')

const messages = ref([
  { role: 'assistant', content: '你好！我是AI写作助手，有什么可以帮助你的？' }
])
const inputText = ref('')
const sending = ref(false)
const chatContainer = ref(null)
const showFilePicker = ref(false)
const selectedFiles = ref([])

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

function handleAiChange(config) {
  // 可以在这里添加切换AI时的逻辑，比如清空聊天记录
  // messages.value = [{ role: 'assistant', content: `已切换到 ${config.name}，有什么可以帮助你的？` }]
}

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
      aiConfigId: selectedAiId.value
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
