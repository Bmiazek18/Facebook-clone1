<template>
  <div class="code-block-gemini my-5 rounded-2xl overflow-hidden bg-[#1a1a1a] border border-white/[0.04] shadow-sm">
    <div class="flex items-center justify-between px-5 py-3 bg-[#1a1a1a] select-none">
      <span class="text-[#fff] text-sm font-medium tracking-wide font-sans">
        {{ displayLang }}
      </span>
      <div class="flex items-center gap-2 text-[#c4c7c5]">
        <button 
          @click="downloadCode" 
          class="flex items-center justify-center p-2 hover:bg-white/[0.06] text-[#c4c7c5] hover:text-white rounded-lg transition-all duration-150 cursor-pointer"
          :title="$t('metaAi.pobierzPlik')"
        >
          <Icon name="ph:file-arrow-down" class="w-5 h-5" />
        </button>
        
        <button 
          @click="copyCode" 
          class="flex items-center justify-center p-2 hover:bg-white/[0.06] rounded-lg transition-all duration-150 cursor-pointer"
          :class="copied ? 'text-green-400' : 'text-[#c4c7c5] hover:text-white'"
          :title="$t('metaAi.kopiujKod')"
        >
          <Icon :name="copied ? 'lucide:check' : 'lucide:copy'" class="w-4 h-4" />
          <span v-if="!copied" class="text-xs font-medium font-sans ml-2">{{ $t('metaAi.kopiujKod') }}</span>
          <span v-else class="text-xs font-medium font-sans ml-2 text-green-400">{{ $t('metaAi.skopiowano') }}</span>
        </button>
      </div>
    </div>

    <pre class="m-0 p-0 bg-transparent overflow-x-auto custom-code-scroll"><code class="block px-5 pb-5 text-[14px] text-[#e3e3e3] font-mono leading-relaxed select-text whitespace-pre" v-html="highlightedCode"></code></pre>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import hljs from 'highlight.js/lib/core'
import javascript from 'highlight.js/lib/languages/javascript'
import typescript from 'highlight.js/lib/languages/typescript'
import xml from 'highlight.js/lib/languages/xml'
import css from 'highlight.js/lib/languages/css'
import cpp from 'highlight.js/lib/languages/cpp'

hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('typescript', typescript)
hljs.registerLanguage('vue', xml)
hljs.registerLanguage('html', xml)
hljs.registerLanguage('css', css)
hljs.registerLanguage('cpp', cpp)

const props = defineProps<{
  code: string
  lang?: string
}>()

const copied = ref(false)

const displayLang = computed(() => {
  if (!props.lang) return 'Kod'
  const l = props.lang.toLowerCase()
  if (l === 'html') return 'HTML'
  if (l === 'vue') return 'Vue.js'
  if (l === 'cpp') return 'C++'
  if (l === 'js' || l === 'javascript') return 'JavaScript'
  if (l === 'ts' || l === 'typescript') return 'TypeScript'
  return props.lang.charAt(0).toUpperCase() + props.lang.slice(1)
})

const highlightedCode = computed(() => {
  let language = props.lang?.toLowerCase() || 'plaintext'
  if (language === 'vue') language = 'xml'

  if (hljs.getLanguage(language)) {
    return hljs.highlight(props.code, { language }).value
  }
  return hljs.highlightAuto(props.code).value
})

const langExtensions: Record<string, string> = {
  javascript: 'js', js: 'js',
  typescript: 'ts', ts: 'ts',
  vue: 'vue', html: 'html', css: 'css',
  json: 'json', python: 'py', cpp: 'cpp', bash: 'sh'
}

const downloadCode = () => {
  const langLower = props.lang?.toLowerCase() || 'txt'
  const extension = langExtensions[langLower] || langLower
  const fileName = `kod_${Date.now()}.${extension}`

  const blob = new Blob([props.code], { type: 'text/plain;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  
  document.body.appendChild(link);
  link.click();
  
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

const copyCode = () => {
  navigator.clipboard.writeText(props.code)
  copied.value = true
  setTimeout(() => (copied.value = false), 2000)
}
</script>

<style scoped>
code {
   font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
   font-weight: 400;
   font-size: 14px;
   line-height: 21px;
}

:deep(.hljs-keyword) {
  color: #969dff;
}

:deep(.hljs-keyword:is([class*="if"], [class*="return"], [class*="else"], [class*="import"])) {
  color: #c58af9;
}

:deep(.hljs-title.function_),
:deep(.hljs-title) {
  color: #e3e3e3; 
}

:deep(.hljs-params) {
  color: #e3e3e3;
}

:deep(.hljs-attr) {
  color: #ff96da;
}
:deep(.hljs-built_in){
  color: #ff5959;
}
:deep(.hljs-string) {
  color: #60d674;
}

:deep(.hljs-number),
:deep(.hljs-literal),
:deep(.hljs-property) {
  color: #7daaf4;
}

:deep(.hljs-comment) {
  color: #80868b;
  font-style: normal !important;
}

:deep(.hljs-operator),
:deep(.hljs-punctuation) {
  color: #e3e3e3;
}

:deep(.hljs-name) {
  color: #60d673;
}

.custom-code-scroll::-webkit-scrollbar {
  height: 8px;
}
.custom-code-scroll::-webkit-scrollbar-track {
  background: #1a1a1a;
}
.custom-code-scroll::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 99px;
}
.custom-code-scroll::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.18);
}
</style>
