export enum LogLevel {
    DEBUG = 'DEBUG',
    INFO = 'INFO',
    WARN = 'WARN',
    ERROR = 'ERROR'
}

type LogContext = 'APP' | 'CORE' | 'BLE' | 'CLOUD';

class Logger {
    private static instance: Logger;
    private currentLogLevel: LogLevel = __DEV__ ? LogLevel.DEBUG : LogLevel.INFO;
    private contexts: Record<LogContext, any> = {} as Record<LogContext, any>;

    private constructor() {
        // Create contexts dynamically
        const contexts: LogContext[] = ['APP', 'CORE', 'BLE', 'CLOUD'];
        contexts.forEach(context => {
            this.contexts[context] = {
                debug: (message: string) => this.log(LogLevel.DEBUG, message, context),
                info: (message: string) => this.log(LogLevel.INFO, message, context),
                warn: (message: string) => this.log(LogLevel.WARN, message, context),
                error: (message: string) => this.log(LogLevel.ERROR, message, context)
            };
        });
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

    private log(level: LogLevel, message: string, context: string) {
        if (!this.shouldLog(level)) return;

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

    // Base methods
    public debug(message: string) {
        this.log(LogLevel.DEBUG, message, 'DEBUG');
    }

    public info(message: string) {
        this.log(LogLevel.INFO, message, 'INFO');
    }

    public warn(message: string) {
        this.log(LogLevel.WARN, message, 'WARN');
    }

    public error(message: string) {
        this.log(LogLevel.ERROR, message, 'ERROR');
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