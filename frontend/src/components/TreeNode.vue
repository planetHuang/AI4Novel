<template>
  <div class="tree-node">
    <div
      :class="['tree-node-header', { active: isFile && currentPath === node.path }]"
      :style="{ '--depth': depth }"
      @click="onClick"
    >
      <span v-if="isDir" class="tree-toggle">{{ expanded ? '▾' : '▸' }}</span>
      <span v-else class="tree-toggle"></span>
      <span class="tree-icon">{{ isDir ? '📁' : '📄' }}</span>
      <span class="tree-name">{{ node.name }}</span>
      <button
        v-if="isFile"
        class="tree-delete"
        @click.stop="$emit('delete-resource', node.path)"
        title="删除"
      >×</button>
    </div>
    <div v-if="isDir && expanded">
      <TreeNode
        v-for="child in node.children"
        :key="child.path"
        :node="child"
        :currentPath="currentPath"
        :depth="depth + 1"
        @select-file="$emit('select-file', $event)"
        @delete-resource="$emit('delete-resource', $event)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  node: { type: Object, required: true },
  currentPath: { type: String, default: '' },
  depth: { type: Number, default: 0 }
})

const emit = defineEmits(['select-file', 'delete-resource'])

const expanded = ref(true)

const isDir = computed(() => props.node.type === 'directory')
const isFile = computed(() => props.node.type === 'file')

function onClick() {
  if (isDir.value) {
    expanded.value = !expanded.value
  } else {
    emit('select-file', props.node.path)
  }
}
</script>
