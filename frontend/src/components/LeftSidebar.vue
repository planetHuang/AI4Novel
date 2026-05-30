<template>
  <div class="sidebar-left">
    <div class="sidebar-title">项目文件</div>
    <div class="tree-container" v-if="tree">
      <TreeNode
        v-for="child in tree.children"
        :key="child.path"
        :node="child"
        :currentPath="currentPath"
        :depth="0"
        @select-file="$emit('select-file', $event)"
        @delete-resource="$emit('delete-resource', $event)"
      />
    </div>
    <div class="sidebar-actions">
      <button @click="startCreate('file')">+ 新建文件</button>
      <button @click="startCreate('directory')">+ 新建文件夹</button>
    </div>
    <div v-if="showCreate" class="create-dialog">
      <div class="create-tree-label">目标文件夹</div>
      <div class="create-tree">
        <div
          v-for="entry in dirEntries"
          :key="entry.path"
          :class="['create-dir', { selected: selectedParentPath === entry.path }]"
          :style="{ paddingLeft: (entry.depth * 12 + 8) + 'px' }"
          @click="selectedParentPath = entry.path"
        >{{ entry.name }}</div>
      </div>
      <input v-model="newName" placeholder="输入名称" @keyup.enter="confirmCreate" />
      <select v-model="newType">
        <option value="file">文件</option>
        <option value="directory">文件夹</option>
      </select>
      <div v-if="newType === 'file'" class="ext-row">
        <select v-model="newExtension" class="ext-select">
          <option value=".txt">.txt</option>
          <option value=".docx">.docx</option>
          <option value="__custom__">自定义...</option>
        </select>
        <input
          v-if="newExtension === '__custom__'"
          v-model="customExtension"
          placeholder="输入后缀，如 .md"
          class="ext-input"
          @keyup.enter="confirmCreate"
        />
      </div>
      <button @click="showCreate = false">取消</button>
      <button @click="confirmCreate">确定</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import TreeNode from './TreeNode.vue'

defineProps({
  tree: { type: Object, default: null },
  currentPath: { type: String, default: '' }
})

const emit = defineEmits(['select-file', 'create-resource', 'delete-resource', 'refresh'])

const showCreate = ref(false)
const newName = ref('')
const newType = ref('file')
const newExtension = ref('.txt')
const customExtension = ref('')
const selectedParentPath = ref('')

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
    entries.push({ path: '', name: '项目根目录', depth: 0 })
    props.tree.children.forEach(c => walk(c, 1))
  }
  return entries
})

function startCreate(type) {
  newType.value = type
  newName.value = ''
  newExtension.value = '.txt'
  customExtension.value = ''
  selectedParentPath.value = ''
  showCreate.value = true
}

function confirmCreate() {
  const name = newName.value.trim()
  if (!name) return

  let finalName = name
  if (newType.value === 'file') {
    let ext = newExtension.value
    if (ext === '__custom__') {
      ext = customExtension.value.trim()
      if (!ext) {
        alert('请输入自定义文件后缀')
        return
      }
      if (!ext.startsWith('.')) ext = '.' + ext
    }
    const lastDot = finalName.lastIndexOf('.')
    if (lastDot > 0) {
      finalName = finalName.substring(0, lastDot)
    }
    finalName = finalName + ext
  }

  emit('create-resource', { parentPath: selectedParentPath.value, name: finalName, type: newType.value })
  showCreate.value = false
}
</script>
