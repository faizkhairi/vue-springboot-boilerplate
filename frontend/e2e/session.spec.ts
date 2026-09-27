import { test, expect } from '@playwright/test'

// Needs the Spring Boot API on :8080 (the preview server proxies /api to it).
// CI starts it with Postgres; locally, run the backend before these specs.
// Registration happens once so the /api/auth rate limit (5 per minute per
// path) is not spent on setup.
test.describe.configure({ mode: 'serial' })

test.describe('Session', () => {
  const email = `e2e-${Date.now()}@example.com`
  const password = 'Password123!'

  test.beforeAll(async ({ request }) => {
    const response = await request.post('/api/auth/register', {
      data: { name: 'E2E User', email, password },
    })
    expect(response.status()).toBe(200)
  })

  test('signs in with valid credentials and signs out again', async ({ page }) => {
    await page.goto('/login')
    await page.getByLabel('Email').fill(email)
    await page.getByLabel('Password').fill(password)
    await page.getByRole('button', { name: 'Sign in', exact: true }).click()

    await expect(page).toHaveURL('/dashboard')
    await expect(page.getByRole('heading', { name: 'Welcome' })).toBeVisible()

    await page.getByRole('button', { name: 'Sign out' }).click()
    await expect(page).toHaveURL('/login')
  })

  test('rejects a wrong password', async ({ page }) => {
    await page.goto('/login')
    await page.getByLabel('Email').fill(email)
    await page.getByLabel('Password').fill('not-the-password')
    await page.getByRole('button', { name: 'Sign in', exact: true }).click()

    await expect(page.getByRole('alert')).toHaveText('Invalid email or password.')
    await expect(page).toHaveURL('/login')
  })

  test('redirects an anonymous visitor away from the dashboard', async ({ page }) => {
    await page.goto('/dashboard')
    await expect(page).toHaveURL(/\/login/)
  })
})
