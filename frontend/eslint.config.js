import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript'
import pluginVue from 'eslint-plugin-vue'

export default defineConfigWithVueTs(
  {
    name: 'app/files-to-lint',
    files: ['**/*.{ts,mts,tsx,vue}'],
  },
  {
    name: 'app/files-to-ignore',
    ignores: ['**/dist/**', '**/coverage/**', '**/playwright-report/**', '**/test-results/**'],
  },
  pluginVue.configs['flat/essential'],
  vueTsConfigs.recommended,
  {
    name: 'app/rules',
    rules: {
      // shadcn-vue UI primitives and numeric error-code views intentionally
      // use single-word filenames to match upstream/status-code conventions.
      'vue/multi-word-component-names': [
        'error',
        { ignores: ['button', 'card', 'input', 'label', '401', '403', '404', '500'] },
      ],
    },
  },
)
