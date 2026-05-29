<template>
  <div class="ai-selector">
    <div class="selector-header">
      <span class="selector-label">当前AI：</span>
      <select
        v-model="selectedConfigId"
        @change="handleSelect"
        class="ai-select"
      >
        <option v-for="config in configs" :key="config.id" :value="config.id">
          {{ config.name }}{{ config.isDefault ? ' (默认)' : '' }}
        </option>
      </select>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue', 'change'])

const configs = ref([])
const selectedConfigId = ref(props.modelValue)

watch(() => props.modelValue, (newVal) => {
  selectedConfigId.value = newVal
})

async function fetchConfigs() {
  try {
    const res = await fetch('/api/ai-configs')
    const data = await res.json()
    if (data.code === 0) {
      configs.value = data.data
      // 如果没有选中项且有默认配置，自动选中默认配置
      if (!selectedConfigId.value && configs.value.length > 0) {
        const defaultConfig = configs.value.find(c => c.isDefault)
        if (defaultConfig) {
          selectedConfigId.value = defaultConfig.id
          emit('update:modelValue', defaultConfig.id)
          emit('change', defaultConfig)
        }
      }
    }
  } catch (e) {
    console.error('获取AI配置失败', e)
  }
}

function handleSelect() {
  const selected = configs.value.find(c => c.id === selectedConfigId.value)
  emit('update:modelValue', selectedConfigId.value)
  emit('change', selected)
}

onMounted(() => {
  fetchConfigs()
})
</script>

<style scoped>
.ai-selector {
  padding: 0.5rem 1rem;
  background: #f0f0f0;
  border-bottom: 1px solid #e0e0e0;
}

.selector-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.selector-label {
  font-size: 0.9rem;
  color: #666;
  white-space: nowrap;
}

.ai-select {
  flex: 1;
  padding: 0.4rem 0.6rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9rem;
  background: white;
  cursor: pointer;
}

.ai-select:focus {
  outline: none;
  border-color: #667eea;
}
</style>