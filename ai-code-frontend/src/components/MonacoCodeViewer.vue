<template>
  <div class="monaco-code-viewer">
    <div ref="containerRef" class="editor-root" />
    <div v-if="showPlaceholder" class="placeholder">
      {{ placeholder }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import * as monaco from 'monaco-editor'

type MonacoLanguage =
  | 'plaintext'
  | 'html'
  | 'css'
  | 'javascript'
  | 'typescript'
  | 'json'
  | 'markdown'
  | 'java'

const props = withDefaults(
  defineProps<{
    value: string
    language?: MonacoLanguage
    autoDetectLanguage?: boolean
    placeholder?: string
  }>(),
  {
    language: 'plaintext',
    autoDetectLanguage: true,
    placeholder: '代码将在这里实时输出…',
  },
)

const containerRef = ref<HTMLElement | null>(null)
let editor: monaco.editor.IStandaloneCodeEditor | null = null
let model: monaco.editor.ITextModel | null = null

// 自动滚动：只有当用户在底部附近时才跟随输出
let shouldAutoScroll = true
const autoScrollThresholdPx = 24

type Transformed = { value: string; language: MonacoLanguage }

function mapFenceLangToMonaco(lang: string | undefined): MonacoLanguage {
  const l = (lang || '').trim().toLowerCase()
  if (!l) return 'plaintext'
  if (l === 'js' || l === 'javascript' || l === 'node') return 'javascript'
  if (l === 'ts' || l === 'typescript') return 'typescript'
  if (l === 'json') return 'json'
  if (l === 'html' || l === 'htm') return 'html'
  if (l === 'css' || l === 'scss' || l === 'less') return 'css'
  if (l === 'md' || l === 'markdown') return 'markdown'
  if (l === 'java') return 'java'
  if (l === 'vue') return 'html'
  return 'plaintext'
}

/**
 * 提取首个 fenced code block（支持未闭合场景，适配流式输出）
 * 形如：```html\n...\n```  或  ```\n...\n
 */
function extractFirstFencedCodeBlock(text: string): { code: string; lang?: string } | null {
  const openIdx = text.indexOf('```')
  if (openIdx < 0) return null

  const afterOpen = text.slice(openIdx + 3)
  const firstLineBreak = afterOpen.indexOf('\n')
  if (firstLineBreak < 0) return null

  const infoLine = afterOpen.slice(0, firstLineBreak).trim()
  const lang = infoLine ? infoLine.split(/\s+/)[0] : undefined
  const codeStartIdx = openIdx + 3 + firstLineBreak + 1

  const closeIdx = text.indexOf('```', codeStartIdx)
  const code = closeIdx >= 0 ? text.slice(codeStartIdx, closeIdx) : text.slice(codeStartIdx)
  return { code, lang }
}

function transformInput(raw: string): Transformed {
  const fenced = extractFirstFencedCodeBlock(raw)
  if (fenced) {
    const lang = mapFenceLangToMonaco(fenced.lang)
    return { value: fenced.code, language: lang }
  }
  const lang = resolveLanguage(raw)
  return { value: raw, language: lang }
}

const transformed = computed(() => transformInput(props.value || ''))
const showPlaceholder = computed(() => !transformed.value.value)

function detectLanguageByContent(text: string): MonacoLanguage {
  const trimmed = text.trim()
  if (!trimmed) return 'plaintext'

  const looksLikeHtml = /^<!DOCTYPE\s+html>/i.test(trimmed) || /^<html[\s>]/i.test(trimmed) || /^<\w+[\s>]/.test(trimmed)
  if (looksLikeHtml) return 'html'

  const looksLikeJson = /^[\[{]/.test(trimmed) && /[\]}]\s*$/.test(trimmed)
  if (looksLikeJson) return 'json'

  const looksLikeTs = /\binterface\s+\w+|\btype\s+\w+\s*=|\bimport\s+.+\s+from\s+['"]/.test(trimmed)
  if (looksLikeTs) return 'typescript'

  const looksLikeJs = /\b(function|const|let|var|export|import|console\.log)\b/.test(trimmed)
  if (looksLikeJs) return 'javascript'

  const looksLikeJava = /\b(class|public|private|protected|static|void|System\.out\.println)\b/.test(trimmed)
  if (looksLikeJava) return 'java'

  const looksLikeMd = /^#{1,6}\s+/.test(trimmed) || /```/.test(trimmed)
  if (looksLikeMd) return 'markdown'

  const looksLikeCss = /^{?[\s\S]*?\{[\s\S]*?:[\s\S]*?;[\s\S]*?\}\s*$/.test(trimmed) || /\b(color|background|display|flex)\s*:/.test(trimmed)
  if (looksLikeCss) return 'css'

  return 'plaintext'
}

function resolveLanguage(text: string): MonacoLanguage {
  if (!props.autoDetectLanguage) return props.language
  if (props.language && props.language !== 'plaintext') return props.language
  return detectLanguageByContent(text)
}

function updateAutoScrollState() {
  if (!editor) return
  const scrollTop = editor.getScrollTop()
  const scrollHeight = editor.getScrollHeight()
  const height = editor.getLayoutInfo().height
  shouldAutoScroll = scrollTop + height >= scrollHeight - autoScrollThresholdPx
}

function scrollToBottom() {
  if (!editor || !model) return
  const lastLine = model.getLineCount()
  editor.revealLine(lastLine)
}

function setEditorValue(nextValue: string) {
  if (!model) return
  model.setValue(nextValue)
}

function appendEditorValue(appendText: string) {
  if (!model) return
  const endPos = model.getPositionAt(model.getValueLength())
  model.applyEdits([
    {
      range: new monaco.Range(endPos.lineNumber, endPos.column, endPos.lineNumber, endPos.column),
      text: appendText,
      forceMoveMarkers: true,
    },
  ])
}

onMounted(() => {
  if (!containerRef.value) return

  const initial = transformed.value
  model = monaco.editor.createModel(initial.value || '', initial.language)

  editor = monaco.editor.create(containerRef.value, {
    model,
    readOnly: true,
    theme: 'vs-dark',
    fontFamily:
      'ui-monospace, "Cascadia Mono", "Cascadia Code", SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace',
    fontSize: 13,
    lineHeight: 20,
    lineNumbers: 'on',
    minimap: { enabled: false },
    wordWrap: 'off',
    scrollBeyondLastLine: false,
    smoothScrolling: true,
    automaticLayout: true,
    renderLineHighlight: 'none',
    overviewRulerBorder: false,
    contextmenu: false,
    scrollbar: {
      verticalScrollbarSize: 10,
      horizontalScrollbarSize: 10,
    },
  })

  editor.onDidScrollChange(() => {
    updateAutoScrollState()
  })

  // 初次载入时，如果有内容，默认滚动到底部（更符合“流式输出”场景）
  if (transformed.value.value) {
    scrollToBottom()
  }
})

onUnmounted(() => {
  editor?.dispose()
  editor = null
  model?.dispose()
  model = null
})

watch(
  () => props.value,
  (next, prev) => {
    if (!model) return

    const nextT = transformInput(next || '')
    const prevT = transformInput(prev || '')

    // 语言同步（只在需要时切换）
    if (model.getLanguageId() !== nextT.language) {
      monaco.editor.setModelLanguage(model, nextT.language)
    }

    // 记录更新前是否在底部（避免 setValue/append 后“抢滚动”）
    updateAutoScrollState()
    const wasAtBottom = shouldAutoScroll

    // 流式输出：优先走“追加”减少抖动
    if (prevT.value && nextT.value.startsWith(prevT.value) && nextT.value.length >= prevT.value.length) {
      const appended = nextT.value.slice(prevT.value.length)
      if (appended) appendEditorValue(appended)
    } else {
      setEditorValue(nextT.value || '')
    }

    if (wasAtBottom) {
      scrollToBottom()
    }
  },
)
</script>

<style scoped>
.monaco-code-viewer {
  position: relative;
  width: 100%;
  height: 100%;
}

.editor-root {
  width: 100%;
  height: 100%;
}

.placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  padding: 16px 18px;
  color: rgba(230, 237, 243, 0.55);
  font-style: italic;
  pointer-events: none;
  white-space: pre-wrap;
}
</style>

