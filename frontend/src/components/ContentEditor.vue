<template>
  <div class="content-editor">
    <div class="editor-header">
      <span class="editor-title">{{ filePath ? fileName : (content ? '新文件' : '未打开文件') }}</span>
      <span v-if="dirty" class="dirty-indicator">● 未保存</span>
      <button :disabled="!dirty" @click="$emit('save')" class="btn-save">{{ filePath ? '保存' : '另存为...' }}</button>
    </div>
    <textarea
      v-if="filePath || content"
      class="editor-textarea"
      :value="content"
      @input="$emit('update:content', $event.target.value)"
      placeholder="开始写作..."
    ></textarea>
    <div v-else class="editor-empty">
      请从左侧文件树或章节列表中选择一个文件开始编辑
    </div>
  </div>
</template>

<script setup>
defineProps({
  filePath: { type: String, default: '' },
  content: { type: String, default: '' },
  fileName: { type: String, default: '' },
  dirty: { type: Boolean, default: false }
})

defineEmits(['update:content', 'save'])
</script>
