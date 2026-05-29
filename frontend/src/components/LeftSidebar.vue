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
      <input v-model="newName" placeholder="输入名称" @keyup.enter="confirmCreate" />
      <select v-model="newType">
        <option value="file">文件</option>
        <option value="directory">文件夹</option>
      </select>
      <button @click="showCreate = false">取消</button>
      <button @click="confirmCreate">确定</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import TreeNode from './TreeNode.vue'

defineProps({
  tree: { type: Object, default: null },
  currentPath: { type: String, default: '' }
})

const emit = defineEmits(['select-file', 'create-resource', 'delete-resource', 'refresh'])

const showCreate = ref(false)
const newName = ref('')
const newType = ref('file')

function startCreate(type) {
  newType.value = type
  newName.value = ''
  showCreate.value = true
}

function confirmCreate() {
  const name = newName.value.trim()
  if (!name) return
  emit('create-resource', { parentPath: '', name, type: newType.value })
  showCreate.value = false
}
</script>
