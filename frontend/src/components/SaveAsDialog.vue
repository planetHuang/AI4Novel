<template>
  <div v-if="show" class="saveas-overlay" @click.self="emit('cancel')">
    <div class="saveas-panel">
      <div class="saveas-title">保存文件</div>

      <div class="saveas-section">
        <label class="saveas-label">选择目标文件夹</label>
        <div class="saveas-tree">
          <div
            v-for="entry in dirEntries"
            :key="entry.path"
            :class="['saveas-dir', { selected: selectedParentPath === entry.path }]"
            :style="{ paddingLeft: (entry.depth * 16 + 8) + 'px' }"
            @click="selectedParentPath = entry.path"
          >
            <span class="saveas-dir-name">{{ entry.name }}</span>
          </div>
        </div>
      </div>

      <div class="saveas-section">
        <label class="saveas-label">文件名</label>
        <div class="saveas-name-row">
          <input
            v-model="fileName"
            placeholder="输入文件名"
            @keyup.enter="confirmSave"
            class="saveas-name-input"
          />
          <div class="saveas-ext-row">
            <select v-model="fileExtension" class="saveas-ext-select">
              <option value=".txt">.txt</option>
              <option value=".docx">.docx</option>
              <option value="__custom__">自定义...</option>
            </select>
            <input
              v-if="fileExtension === '__custom__'"
              v-model="customExtension"
              placeholder="输入后缀，如 .md"
              class="saveas-ext-input"
              @keyup.enter="confirmSave"
            />
          </div>
        </div>
      </div>

      <div class="saveas-actions">
        <button @click="emit('cancel')">取消</button>
        <button @click="confirmSave">确认</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  show: Boolean,
  tree: { type: Object, default: null },
  defaultName: { type: String, default: '' }
})

const emit = defineEmits(['confirm', 'cancel'])

const selectedParentPath = ref('')
const fileName = ref('')
const fileExtension = ref('.txt')
const customExtension = ref('')

const dirEntries = computed(() => {
  const entries = []
  function walk(node, depth) {
    if (node.type === 'directory') {
      entries.push({ path: node.path, name: node.name, depth })
      if (node.children) {
        node.children.forEach(c => walk(c, depth + 1))
      }
    }
  }
  if (props.tree && props.tree.children) {
    // Root entry
    entries.push({ path: '', name: '项目根目录', depth: 0 })
    props.tree.children.forEach(c => walk(c, 1))
  }
  return entries
})

watch(() => props.show, (visible) => {
  if (visible) {
    selectedParentPath.value = ''
    fileName.value = props.defaultName || ''
    fileExtension.value = '.txt'
    customExtension.value = ''
  }
})

function confirmSave() {
  const name = fileName.value.trim()
  if (!name) return

  let ext = fileExtension.value
  if (ext === '__custom__') {
    ext = customExtension.value.trim()
    if (!ext) return
    if (!ext.startsWith('.')) ext = '.' + ext
  }

  let finalName = name
  const lastDot = finalName.lastIndexOf('.')
  if (lastDot > 0) {
    finalName = finalName.substring(0, lastDot)
  }
  finalName = finalName + ext

  emit('confirm', {
    parentPath: selectedParentPath.value,
    fileName: finalName
  })
}
</script>
