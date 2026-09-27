# Standard Error Response Format

API error responses use a consistent JSON shape, defined in `backend/src/main/java/com/app/boilerplate/common/dto/ErrorResponse.java`.

## Format

```json
{
  "error": {
    "message": "Human-readable error message",
    "code": "ERROR_CODE",
    "details": {}
  }
}
```

`code` and `details` are omitted from the response when not set (`ErrorResponse` is annotated `@JsonInclude(JsonInclude.Include.NON_NULL)`).

## Common HTTP Status Codes

| Status | Meaning | Example Use Case |
|--------|---------|------------------|
| 400 | Bad Request | Invalid input, validation failed |
| 401 | Unauthorized | Missing or invalid authentication token |
| 403 | Forbidden | Authenticated but lacking required permissions |
| 404 | Not Found | Resource does not exist |
| 409 | Conflict | Resource already exists (for example, duplicate email) |
| 429 | Too Many Requests | Rate limit exceeded on `/api/auth/**` |
| 500 | Internal Server Error | Unexpected server error |

## Examples

These are real responses from `AuthController` (`backend/src/main/java/com/app/boilerplate/auth/AuthController.java`).

### Invalid credentials (login)

```json
{
  "error": {
    "message": "Invalid email or password",
    "code": "INVALID_CREDENTIALS"
  }
}
```

### Invalid or expired refresh token

```json
{
  "error": {
    "message": "Invalid refresh token",
    "code": "UNAUTHORIZED"
  }
}
```

### Registration failure (duplicate email)

```json
{
  "error": {
    "message": "Email already exists",
    "code": "VALIDATION_ERROR"
  }
}
```

### Rate limit error

This is the exact shape `RateLimitFilter` writes when `/api/auth/**` receives more than 5 requests per minute from the same client IP for the same path. The response also carries a `429` status and a `Retry-After` header (seconds until the window resets):

```json
{
  "error": {
    "message": "Too many authentication attempts. Please try again in a minute.",
    "code": "RATE_LIMIT_EXCEEDED"
  }
}
```

### Validation error

`GlobalExceptionHandler` (`backend/src/main/java/com/app/boilerplate/common/exception/GlobalExceptionHandler.java`) catches `MethodArgumentNotValidException` (a failed `@Valid` on a request body, for example a malformed email on `RegisterRequest`) and returns `400` with a flat field-to-message map, not the `error` envelope above. The messages are Hibernate Validator's default messages for the Bean Validation annotations on the request DTO:

```json
{
  "email": "must be a well-formed email address",
  "name": "must not be blank"
}
```

## Client-Side Handling

```typescript
try {
  const response = await fetch('/api/some-endpoint')
  const data = await response.json()

  if (!response.ok) {
    // The error envelope applies to most non-2xx responses.
    // A 400 from bean validation is the field-map shape shown above instead.
    const errorMessage = data.error?.message ?? 'An error occurred'
    const errorCode = data.error?.code

    if (response.status === 429) {
      const retryAfter = response.headers.get('Retry-After')
      // Surface retryAfter to the user, or back off and retry.
    }

    throw new Error(errorMessage)
  }

  return data
} catch (error) {
  // Handle error
}
```
