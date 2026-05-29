<template>
  <div class="sidebar-right">
    <div class="chat-title">AI 写作助手</div>
    <div class="chat-messages" ref="chatContainer">
      <div v-for="(msg, i) in messages" :key="i" :class="['chat-msg', msg.role]">
        <div class="msg-role">{{ msg.role === 'user' ? '你' : 'AI' }}</div>
        <div class="msg-content">{{ msg.content }}</div>
      </div>
      <div v-if="sending" class="chat-msg assistant">
        <div class="msg-role">AI</div>
        <div class="msg-content">正在思考...</div>
      </div>
    </div>
    <div class="chat-input-area">
      <textarea
        v-model="inputText"
        @keyup.enter.exact="sendMessage"
        placeholder="输入消息，按 Enter 发送，Shift+Enter 换行..."
        rows="3"
      ></textarea>
      <button @click="sendMessage" :disabled="!inputText.trim() || sending">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'

const props = defineProps({
  novelId: { type: String, required: true }
})

const messages = ref([
  { role: 'assistant', content: '你好！我是AI写作助手，有什么可以帮助你的？' }
])
const inputText = ref('')
const sending = ref(false)
const chatContainer = ref(null)

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || sending.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  sending.value = true
  await scrollToBottom()

  try {
    const res = await fetch(`/api/novels/${props.novelId}/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ messages: messages.value })
    })
    const data = await res.json()
    if (data.code === 0) {
      messages.value.push({ role: 'assistant', content: data.data.reply })
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
