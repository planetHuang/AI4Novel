<template>
  <div v-if="show" class="picker-overlay" @click.self="emit('cancel')">
    <div class="picker-panel">
      <div class="picker-title">选择项目文件</div>

      <div class="picker-tree">
        <div v-if="flatFiles.length === 0" class="picker-empty">暂无文件</div>
        <label
          v-for="item in flatFiles"
          :key="item.path"
          :class="['picker-item', { selected: selected.has(item.path) }]"
          :style="{ paddingLeft: (item.depth * 16 + 8) + 'px' }"
        >
          <input
            type="checkbox"
            :checked="selected.has(item.path)"
            @change="toggle(item.path)"
            class="picker-checkbox"
          />
          <span class="picker-name">{{ item.name }}</span>
        </label>
      </div>

      <div class="picker-actions">
        <button @click="emit('cancel')" class="picker-btn-cancel">取消</button>
        <button @click="confirmPick" class="picker-btn-confirm" :disabled="selected.size === 0">
          确认 (已选 {{ selected.size }})
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  show: Boolean,
  tree: { type: Object, default: null }
})

const emit = defineEmits(['confirm', 'cancel'])

const selected = ref(new Set())

const flatFiles = computed(() => {
  const entries = []
  function walk(node, depth) {
    if (node.type === 'directory') {
      // 也显示目录名作为分组，不提供选择
      if (node.path) {
        entries.push({ path: null, name: node.name + '/', depth, isDir: true })
      }
      if (node.children) {
        node.children.forEach(c => walk(c, depth + 1))
      }
    } else {
      entries.push({ path: node.path, name: node.name, depth, isDir: false })
    }
  }
  if (props.tree && props.tree.children) {
    props.tree.children.forEach(c => walk(c, 0))
  }
  return entries
})

watch(() => props.show, (visible) => {
  if (visible) {
    selected.value = new Set()
  }
})

function toggle(path) {
  const s = new Set(selected.value)
  if (s.has(path)) {
    s.delete(path)
  } else {
    s.add(path)
  }
  selected.value = s
}

function confirmPick() {
  const paths = Array.from(selected.value)
  emit('confirm', paths)
}
</script>
