<template>
  <div v-if="show" class="sendto-overlay" @click.self="emit('cancel')">
    <div class="sendto-panel">
      <div class="sendto-title">发送到编辑器</div>
      <div class="sendto-options">
        <label
          v-for="opt in options"
          :key="opt.value"
          :class="['sendto-option', { selected: selected === opt.value }]"
        >
          <input type="radio" :value="opt.value" v-model="selected" />
          <span>{{ opt.label }}</span>
        </label>
      </div>
      <div class="sendto-actions">
        <button @click="emit('cancel')">取消</button>
        <button @click="confirmSelection">确认</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  show: Boolean
})

const emit = defineEmits(['confirm', 'cancel'])

const options = [
  { value: 'cursor', label: '插入到当前文件光标位置' },
  { value: 'append', label: '添加到当前文件末尾' },
  { value: 'new', label: '创建新文件' }
]

const selected = ref('cursor')

watch(() => props.show, (visible) => {
  if (visible) {
    selected.value = 'cursor'
  }
})

function confirmSelection() {
  emit('confirm', selected.value)
}
</script>
