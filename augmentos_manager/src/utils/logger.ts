export enum LogLevel {
    DEBUG = 'DEBUG',
    INFO = 'INFO',
    WARN = 'WARN',
    ERROR = 'ERROR'
}

// Define os contextos uma única vez
export const AVAILABLE_CONTEXTS = ['APP', 'CORE', 'BLE', 'CLOUD'] as const;
type LogContext = typeof AVAILABLE_CONTEXTS[number];

class Logger {
    private static instance: Logger;
    private currentLogLevel: LogLevel = __DEV__ ? LogLevel.DEBUG : LogLevel.INFO;
    private contexts: Record<LogContext, any> = {} as Record<LogContext, any>;
    private allowedContexts: LogContext[] = [...AVAILABLE_CONTEXTS];

    private constructor() {
        // Create contexts dynamically
        AVAILABLE_CONTEXTS.forEach((context: LogContext) => {
            this.contexts[context] = {
                debug: (message: string) => this.log(LogLevel.DEBUG, message, context),
                info: (message: string) => this.log(LogLevel.INFO, message, context),
                warn: (message: string) => this.log(LogLevel.WARN, message, context),
                error: (message: string) => this.log(LogLevel.ERROR, message, context)
            };
        });
    }

    public setAllowedContexts(contexts: LogContext[]) {
        this.allowedContexts = contexts;
    }

    public static getInstance(): Logger {
        if (!Logger.instance) {
            Logger.instance = new Logger();
        }
        return Logger.instance;
    }

    private formatMessage(level: LogLevel, message: string, context: string): string {
        const timestamp = new Date().toISOString();
        return `${timestamp} [${level}] ${context}: ${message}`;
    }

    private shouldLog(level: LogLevel): boolean {
        const levels = [LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR];
        return levels.indexOf(level) >= levels.indexOf(this.currentLogLevel);
    }

    private log(level: LogLevel, message: string, context: LogContext) {
        if (!this.shouldLog(level)) return;
        if (!this.allowedContexts.includes(context)) return;

        const formattedMessage = this.formatMessage(level, message, context);

        // Log to console based on level
        switch (level) {
            case LogLevel.DEBUG:
                console.debug(formattedMessage);
                break;
            case LogLevel.INFO:
                console.info(formattedMessage);
                break;
            case LogLevel.WARN:
                console.warn(formattedMessage);
                break;
            case LogLevel.ERROR:
                console.error(formattedMessage);
                break;
        }
    }

    // Context getters
    public get app() { return this.contexts.APP; }
    public get core() { return this.contexts.CORE; }
    public get ble() { return this.contexts.BLE; }
    public get cloud() { return this.contexts.CLOUD; }

    public setLogLevel(level: LogLevel) {
        this.currentLogLevel = level;
    }
}

export const log = Logger.getInstance(); 