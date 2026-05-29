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
        />
      </div>
      <RightSidebar
        class="panel-right"
        :novelId="id"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import LeftSidebar from '../components/LeftSidebar.vue'
import ChapterList from '../components/ChapterList.vue'
import ContentEditor from '../components/ContentEditor.vue'
import RightSidebar from '../components/RightSidebar.vue'

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
  if (!currentFilePath.value || !isDirty.value) return
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

onMounted(() => {
  loadNovel()
  loadTree()
})
</script>
