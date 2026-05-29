<template>
  <div class="chapter-list">
    <div class="panel-title">章节列表</div>
    <div v-if="!chapters.length" class="empty-hint">暂无章节</div>
    <div
      v-for="ch in chapters"
      :key="ch.path"
      :class="['chapter-item', { active: ch.path === currentPath }]"
      @click="$emit('select-file', ch.path)"
    >
      {{ displayName(ch.name) }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  tree: { type: Object, default: null },
  currentPath: { type: String, default: '' }
})

defineEmits(['select-file'])

const chapters = computed(() => {
  if (!props.tree || !props.tree.children) return []
  const chapterDir = props.tree.children.find(c => c.name === '章节')
  if (!chapterDir || !chapterDir.children) return []
  return chapterDir.children.filter(c => c.type === 'file')
})

function displayName(name) {
  return name.replace(/\.(txt|md)$/, '')
}
</script>
