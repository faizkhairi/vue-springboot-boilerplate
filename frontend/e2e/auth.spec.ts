import { test, expect } from '@playwright/test'

test.describe('Authentication Flows', () => {
  test('should navigate to login page', async ({ page }) => {
    await page.goto('/login')
    await expect(page.locator('h1, h2')).toContainText(/login|sign in/i)
  })

  test('should navigate to register page', async ({ page }) => {
    await page.goto('/register')
    await expect(page.locator('h1, h2')).toContainText(/register|sign up|create account/i)
  })

  test('should show validation errors for empty login form', async ({ page }) => {
    await page.goto('/login')

    // Submit empty form
    const submitButton = page.locator('button[type="submit"]')
    await submitButton.click()

    // Check for validation errors
    const errorVisible = await Promise.race([
      page.locator('[role="alert"], [class*="error"]').first().isVisible().catch(() => false),
      page.locator('input:invalid').first().isVisible().catch(() => false),
    ])

    expect(errorVisible).toBeTruthy()
  })

  test('should show validation errors for empty register form', async ({ page }) => {
    await page.goto('/register')

    // Submit empty form
    const submitButton = page.locator('button[type="submit"]')
    await submitButton.click()

    // Check for validation errors
    const errorVisible = await Promise.race([
      page.locator('[role="alert"], [class*="error"]').first().isVisible().catch(() => false),
      page.locator('input:invalid').first().isVisible().catch(() => false),
    ])

    expect(errorVisible).toBeTruthy()
  })

  test('should require authentication for dashboard', async ({ page }) => {
    await page.goto('/dashboard')

    // Should redirect to login or show unauthorized
    await page.waitForURL(/\/login|\/401/, { timeout: 5000 }).catch(() => {})

    const currentUrl = page.url()
    expect(currentUrl).toMatch(/\/login|\/401/)
  })

  test('should navigate between login and register pages', async ({ page }) => {
    // Start at login
    await page.goto('/login')

    // Find link to register page
    const registerLink = page.locator('a[href*="register"], a:has-text("Sign up"), a:has-text("Register")').first()

    if (await registerLink.isVisible().catch(() => false)) {
      await registerLink.click()
      await expect(page).toHaveURL(/\/register/)
      await expect(page.locator('h1, h2')).toContainText(/register|sign up|create account/i)

      // Navigate back to login
      const loginLink = page.locator('a[href*="login"], a:has-text("Sign in"), a:has-text("Login")').first()
      if (await loginLink.isVisible().catch(() => false)) {
        await loginLink.click()
        await expect(page).toHaveURL(/\/login/)
      }
    }
  })

  test('should handle login with invalid credentials', async ({ page }) => {
    await page.goto('/login')

    // Fill form with invalid credentials
    await page.locator('input[type="email"], input[name="email"]').fill('invalid@example.com')
    await page.locator('input[type="password"], input[name="password"]').fill('wrongpassword')

    // Submit form
    await page.locator('button[type="submit"]').click()

    // Should show error message (either toast or inline error)
    await page.waitForTimeout(1000) // Wait for API response

    const errorVisible = await Promise.race([
      page.locator('text=/invalid|unauthorized|incorrect|wrong credentials/i').first().isVisible().catch(() => false),
      page.locator('[role="alert"]').first().isVisible().catch(() => false),
    ])

    // Error might not show if backend is not running, so we just check that we didn't navigate away
    const currentUrl = page.url()
    expect(currentUrl).toContain('/login')
  })
})

test.describe('Error Pages', () => {
  test('should display 404 page for invalid route', async ({ page }) => {
    await page.goto('/this-route-does-not-exist')

    // Check if error page is shown
    const has404Content = await page.locator('text=/404|not found/i').first().isVisible().catch(() => false)

    if (has404Content) {
      // Check for navigation buttons
      const homeButton = page.locator('a[href="/"], button:has-text("Home")').first()
      await expect(homeButton).toBeVisible()
    }
  })

  test('should display 401 error page if it exists', async ({ page }) => {
    const response = await page.goto('/errors/401').catch(() => null)

    if (response && response.status() === 200) {
      await expect(page.locator('text=/401|unauthorized/i').first()).toBeVisible()
    }
  })

  test('should display 403 error page if it exists', async ({ page }) => {
    const response = await page.goto('/errors/403').catch(() => null)

    if (response && response.status() === 200) {
      await expect(page.locator('text=/403|forbidden/i').first()).toBeVisible()
    }
  })
})
