import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    include: ['src/**/*.spec.ts', 'src/**/*.test.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'json-summary'],
      include: ['src/**/*.{ts,vue}'],
      exclude: [
        'src/main.ts',
        'src/**/*.d.ts',
        'src/**/*.gen.ts',
        'src/**/generated/**',
        'src/**/*.spec.ts',
        'src/**/*.test.ts',
      ],
      // Measured 2026-09-27: statements 30.49%, branches 15.44%, functions
      // 14.45%, lines 32.07%. Floors are rounded down to the nearest 5%.
      thresholds: {
        lines: 30,
        statements: 30,
        functions: 10,
        branches: 15,
      },
    },
  },
})
