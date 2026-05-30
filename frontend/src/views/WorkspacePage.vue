<template>
  <div class="workspace">
    <header class="workspace-header">
      <button @click="goBack" class="btn-back">&larr; 返回</button>
      <h2>{{ novel?.name || '加载中...' }}</h2>
    </header>
    <div class="workspace-body">
      <LeftSidebar
        class="panel-left"
        :tree="tree"
        :currentPath="currentFilePath"
        @select-file="openFile"
        @create-resource="createResource"
        @delete-resource="deleteResource"
        @refresh="loadTree"
      />
      <div class="panel-center">
        <ChapterList
          :tree="tree"
          :currentPath="currentFilePath"
          @select-file="openFile"
        />
        <ContentEditor
          :filePath="currentFilePath"
          :content="currentContent"
          :fileName="currentFileName"
          :dirty="isDirty"
          @update:content="onContentChange"
          @save="saveFile"
          @cursor-position="onCursorPosition"
        />
      </div>
      <RightSidebar
        class="panel-right"
        :novelId="id"
        :tree="tree"
        @send-to-editor="onSendToEditor"
      />
    </div>
    <SaveAsDialog
      :show="showSaveAsDialog"
      :tree="tree"
      :defaultName="currentFileName"
      @confirm="confirmSaveAs"
      @cancel="showSaveAsDialog = false"
    />
    <SendToEditorDialog
      :show="showSendToEditor"
      @confirm="handleSendToEditorConfirm"
      @cancel="showSendToEditor = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import LeftSidebar from '../components/LeftSidebar.vue'
import ChapterList from '../components/ChapterList.vue'
import ContentEditor from '../components/ContentEditor.vue'
import RightSidebar from '../components/RightSidebar.vue'
import SaveAsDialog from '../components/SaveAsDialog.vue'
import SendToEditorDialog from '../components/SendToEditorDialog.vue'

const props = defineProps({
  id: String
})

const router = useRouter()

const novel = ref(null)
const tree = ref(null)
const currentFilePath = ref('')
const currentContent = ref('')
const currentFileName = ref('')
const isDirty = ref(false)
const savedContent = ref('')
const showSaveAsDialog = ref(false)
const showSendToEditor = ref(false)
const pendingAiContent = ref('')
const cursorPosition = ref(0)

function goBack() {
  router.push({ name: 'Home' })
}

async function loadNovel() {
  try {
    const res = await fetch(`/api/novels/${props.id}`)
    const data = await res.json()
    if (data.code === 0) {
      novel.value = data.data
    }
  } catch (e) {
    console.error('获取小说信息失败', e)
  }
}

async function loadTree() {
  try {
    const res = await fetch(`/api/novels/${props.id}/tree`)
    const data = await res.json()
    if (data.code === 0) {
      tree.value = data.data
    }
  } catch (e) {
    console.error('获取文件树失败', e)
  }
}

async function openFile(path) {
  try {
    const res = await fetch(`/api/novels/${props.id}/file?path=${encodeURIComponent(path)}`)
    const data = await res.json()
    if (data.code === 0) {
      currentFilePath.value = data.data.path
      currentContent.value = data.data.content
      currentFileName.value = data.data.fileName
      savedContent.value = data.data.content
      isDirty.value = false
    }
  } catch (e) {
    console.error('读取文件失败', e)
  }
}

function onContentChange(content) {
  currentContent.value = content
  isDirty.value = content !== savedContent.value
}

async function saveFile() {
  if (!isDirty.value) return
  if (!currentFilePath.value) {
    showSaveAsDialog.value = true
    return
  }
  try {
    const res = await fetch(`/api/novels/${props.id}/file`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        path: currentFilePath.value,
        content: currentContent.value
      })
    })
    const data = await res.json()
    if (data.code === 0) {
      savedContent.value = currentContent.value
      isDirty.value = false
    }
  } catch (e) {
    console.error('保存失败', e)
  }
}

async function createResource({ parentPath, name, type }) {
  try {
    const res = await fetch(`/api/novels/${props.id}/resource`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ parentPath, name, type })
    })
    const data = await res.json()
    if (data.code === 0) {
      await loadTree()
    } else {
      alert(data.message)
    }
  } catch (e) {
    console.error('创建失败', e)
  }
}

async function deleteResource(path) {
  if (!confirm(`确定要删除「${path}」吗？`)) return
  try {
    const res = await fetch(`/api/novels/${props.id}/resource`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ path })
    })
    const data = await res.json()
    if (data.code === 0) {
      if (currentFilePath.value === path) {
        currentFilePath.value = ''
        currentContent.value = ''
        currentFileName.value = ''
        isDirty.value = false
      }
      await loadTree()
    }
  } catch (e) {
    console.error('删除失败', e)
  }
}

function onCursorPosition(pos) {
  cursorPosition.value = pos
}

function onSendToEditor(content) {
  pendingAiContent.value = content
  if (currentFilePath.value) {
    showSendToEditor.value = true
  } else {
    if (isDirty.value && currentContent.value) {
      if (!confirm('当前有未保存的修改，是否替换？')) return
    }
    currentFilePath.value = ''
    currentFileName.value = ''
    currentContent.value = content
    savedContent.value = ''
    isDirty.value = true
  }
}

async function handleSendToEditorConfirm(mode) {
  showSendToEditor.value = false
  const aiContent = pendingAiContent.value

  if (mode === 'cursor') {
    const pos = cursorPosition.value
    const old = currentContent.value
    currentContent.value = old.slice(0, pos) + aiContent + old.slice(pos)
    isDirty.value = true
  } else if (mode === 'append') {
    currentContent.value = currentContent.value + aiContent
    isDirty.value = true
  } else if (mode === 'new') {
    if (isDirty.value && currentFilePath.value) {
      await saveFile()
    }
    currentFilePath.value = ''
    currentFileName.value = ''
    currentContent.value = aiContent
    savedContent.value = ''
    isDirty.value = true
  }
}

async function confirmSaveAs({ parentPath, fileName }) {
  const filePath = parentPath ? parentPath + '/' + fileName : fileName
  try {
    const res = await fetch(`/api/novels/${props.id}/file`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ path: filePath, content: currentContent.value })
    })
    const data = await res.json()
    if (data.code === 0) {
      currentFilePath.value = filePath
      currentFileName.value = fileName
      savedContent.value = currentContent.value
      isDirty.value = false
      showSaveAsDialog.value = false
      await loadTree()
    } else {
      alert(data.message)
    }
  } catch (e) {
    console.error('保存失败', e)
  }
}

onMounted(() => {
  loadNovel()
  loadTree()
})
</script>
