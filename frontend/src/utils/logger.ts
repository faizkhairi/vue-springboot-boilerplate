/**
 * Structured logging utility for Vue frontend
 *
 * In production, logs can be sent to:
 * - Backend logging endpoint
 * - Third-party logging services
 * - Browser console (dev mode only)
 */

type LogLevel = 'debug' | 'info' | 'warn' | 'error'

interface LogContext {
  [key: string]: any
}

class Logger {
  private isDev = import.meta.env.DEV

  private formatMessage(level: LogLevel, message: string, context?: LogContext): string {
    const timestamp = new Date().toISOString()
    const contextStr = context ? ` | ${JSON.stringify(context)}` : ''
    return `[${timestamp}] [${level.toUpperCase()}] ${message}${contextStr}`
  }

  debug(message: string, context?: LogContext) {
    if (this.isDev) {
      console.log(this.formatMessage('debug', message, context))
    }
  }

  info(message: string, context?: LogContext) {
    console.log(this.formatMessage('info', message, context))
  }

  warn(message: string, context?: LogContext) {
    console.warn(this.formatMessage('warn', message, context))
  }

  error(message: string, error?: Error, context?: LogContext) {
    const errorContext = error
      ? {
          ...context,
          error: {
            message: error.message,
            stack: error.stack,
            name: error.name,
          },
        }
      : context

    console.error(this.formatMessage('error', message, errorContext))
  }

  /**
   * Log audit events (auth, security, etc.)
   */
  audit(event: string, data: Record<string, any>) {
    this.info(`[AUDIT] ${event}`, { audit: true, event, ...data })
  }
}

export const logger = new Logger()

/**
 * Create a child logger with additional context
 */
export function createLogger(context: LogContext) {
  return {
    debug: (message: string, additionalContext?: LogContext) =>
      logger.debug(message, { ...context, ...additionalContext }),
    info: (message: string, additionalContext?: LogContext) =>
      logger.info(message, { ...context, ...additionalContext }),
    warn: (message: string, additionalContext?: LogContext) =>
      logger.warn(message, { ...context, ...additionalContext }),
    error: (message: string, error?: Error, additionalContext?: LogContext) =>
      logger.error(message, error, { ...context, ...additionalContext }),
    audit: (event: string, data: Record<string, any>) =>
      logger.audit(event, { ...context, ...data }),
  }
}
