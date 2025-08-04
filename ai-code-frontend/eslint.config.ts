import { globalIgnores } from 'eslint/config'
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript'
import pluginVue from 'eslint-plugin-vue'
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting'

// To allow more languages other than `ts` in `.vue` files, uncomment the following lines:
// import { configureVueProject } from '@vue/eslint-config-typescript'
// configureVueProject({ scriptLangs: ['ts', 'tsx'] })
// More info at https://github.com/vuejs/eslint-config-typescript/#advanced-setup

export default defineConfigWithVueTs(
  {
    name: 'app/files-to-lint',
    files: ['**/*.{ts,mts,tsx,vue}'],
  },

  globalIgnores(['**/dist/**', '**/dist-ssr/**', '**/coverage/**']),

  // Vue 3 组合式 API 推荐配置
  pluginVue.configs['flat/essential'],
  pluginVue.configs['flat/recommended'],
  {
    rules: {
      // 强制使用 Vue 3 语法
      'vue/no-deprecated-destroyed-lifecycle': 'error',
      'vue/no-deprecated-dollar-listeners-api': 'error',
      'vue/no-deprecated-dollar-scopedslots-api': 'error',
      'vue/no-deprecated-events-api': 'error',
      'vue/no-deprecated-filter': 'error',
      'vue/no-deprecated-functional-template': 'error',
      'vue/no-deprecated-html-element-is': 'error',
      'vue/no-deprecated-props-default-this': 'error',
      'vue/no-deprecated-scope-attribute': 'error',
      'vue/no-deprecated-slot-attribute': 'error',
      'vue/no-deprecated-slot-scope-attribute': 'error',
      'vue/no-deprecated-v-bind-sync': 'error',
      'vue/no-deprecated-v-on-native-modifier': 'error',
      'vue/no-deprecated-v-on-number-modifiers': 'error',
      'vue/no-deprecated-v-on-object-syntax': 'error',
      'vue/no-deprecated-v-once-directive': 'error',
      'vue/no-deprecated-v-text-v-html': 'error',
      'vue/no-deprecated-vue-config-keycodes': 'error',
      
      // 组合式 API 最佳实践
      'vue/component-api-style': ['error', ['script-setup', 'composition']],
      'vue/define-macros-order': ['error', {
        order: ['defineProps', 'defineEmits', 'defineExpose', 'withDefaults']
      }],
      'vue/no-setup-props-destructure': 'error',
      
      // TypeScript 相关
      'vue/valid-define-props': 'error',
      'vue/valid-define-emits': 'error',
      'vue/valid-define-expose': 'error',
    }
  },
  vueTsConfigs.recommended,
  skipFormatting,
)
