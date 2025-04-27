import { log, LogLevel, AVAILABLE_CONTEXTS } from '../../src/utils/logger';

describe('Logger', () => {
    beforeEach(() => {
        log.setLogLevel(LogLevel.DEBUG);
        log.setAllowedContexts([...AVAILABLE_CONTEXTS]);
        // Clear all mocks before each test
        jest.clearAllMocks();
        // Spy on console methods
        jest.spyOn(console, 'debug').mockImplementation();
        jest.spyOn(console, 'info').mockImplementation();
        jest.spyOn(console, 'warn').mockImplementation();
        jest.spyOn(console, 'error').mockImplementation();
    });

    afterEach(() => {
        // Restore console methods after each test
        jest.restoreAllMocks();
    });

    describe('Context Getters', () => {
        it('should have getters for all contexts', () => {
            AVAILABLE_CONTEXTS.forEach((context: typeof AVAILABLE_CONTEXTS[number]) => {
                const contextKey = context.toLowerCase() as keyof typeof log;
                const contextGetter = log[contextKey];
                expect(contextGetter).toBeDefined();
                expect(contextGetter.debug).toBeDefined();
                expect(contextGetter.info).toBeDefined();
                expect(contextGetter.warn).toBeDefined();
                expect(contextGetter.error).toBeDefined();
            });
        });

        it('should have non-context properties', () => {
            expect(log.setLogLevel).toBeDefined();
            expect(log.setAllowedContexts).toBeDefined();
        });
    });

    describe('Basic Logging', () => {
        it('should log messages at all levels', () => {
            log.app.debug('Basic debug message');
            log.app.info('Basic info message');
            log.app.warn('Basic warning message');
            log.app.error('Basic error message');

            expect(console.debug).toHaveBeenCalled();
            expect(console.info).toHaveBeenCalled();
            expect(console.warn).toHaveBeenCalled();
            expect(console.error).toHaveBeenCalled();
        });
    });

    describe('Context-specific Logging', () => {
        it('should log messages with APP context', () => {
            log.app.debug('App debug message');
            log.app.info('App info message');
            log.app.warn('App warning message');
            log.app.error('App error message');

            expect(console.debug).toHaveBeenCalledWith(expect.stringContaining('[DEBUG] APP:'));
            expect(console.info).toHaveBeenCalledWith(expect.stringContaining('[INFO] APP:'));
            expect(console.warn).toHaveBeenCalledWith(expect.stringContaining('[WARN] APP:'));
            expect(console.error).toHaveBeenCalledWith(expect.stringContaining('[ERROR] APP:'));
        });

        it('should log messages with CORE context', () => {
            log.core.debug('Core debug message');
            log.core.info('Core info message');
            log.core.warn('Core warning message');
            log.core.error('Core error message');

            expect(console.debug).toHaveBeenCalledWith(expect.stringContaining('[DEBUG] CORE:'));
            expect(console.info).toHaveBeenCalledWith(expect.stringContaining('[INFO] CORE:'));
            expect(console.warn).toHaveBeenCalledWith(expect.stringContaining('[WARN] CORE:'));
            expect(console.error).toHaveBeenCalledWith(expect.stringContaining('[ERROR] CORE:'));
        });
    });

    describe('Log Levels', () => {
        it('should only show errors when log level is ERROR', () => {
            log.setLogLevel(LogLevel.ERROR);

            log.app.debug('Should not appear');
            log.app.info('Should not appear');
            log.app.warn('Should not appear');
            log.app.error('Should appear');

            expect(console.debug).not.toHaveBeenCalled();
            expect(console.info).not.toHaveBeenCalled();
            expect(console.warn).not.toHaveBeenCalled();
            expect(console.error).toHaveBeenCalled();
        });

        it('should show warnings and errors when log level is WARN', () => {
            log.setLogLevel(LogLevel.WARN);

            log.app.debug('Should not appear');
            log.app.info('Should not appear');
            log.app.warn('Should appear');
            log.app.error('Should appear');

            expect(console.debug).not.toHaveBeenCalled();
            expect(console.info).not.toHaveBeenCalled();
            expect(console.warn).toHaveBeenCalled();
            expect(console.error).toHaveBeenCalled();
        });

        it('should show info, warnings and errors when log level is INFO', () => {
            log.setLogLevel(LogLevel.INFO);

            log.app.debug('Should not appear');
            log.app.info('Should appear');
            log.app.warn('Should appear');
            log.app.error('Should appear');

            expect(console.debug).not.toHaveBeenCalled();
            expect(console.info).toHaveBeenCalled();
            expect(console.warn).toHaveBeenCalled();
            expect(console.error).toHaveBeenCalled();
        });

        it('should show all messages when log level is DEBUG', () => {
            log.setLogLevel(LogLevel.DEBUG);

            log.app.debug('Should appear');
            log.app.info('Should appear');
            log.app.warn('Should appear');
            log.app.error('Should appear');

            expect(console.debug).toHaveBeenCalled();
            expect(console.info).toHaveBeenCalled();
            expect(console.warn).toHaveBeenCalled();
            expect(console.error).toHaveBeenCalled();
        });
    });

    describe('Allowed Contexts', () => {
        it('should respect allowed contexts', () => {
            log.setAllowedContexts(['APP']);
            const consoleSpy = jest.spyOn(console, 'info');

            log.app.info('test');
            log.core.info('test');

            expect(consoleSpy).toHaveBeenCalledTimes(1);
        });

        it('should allow changing allowed contexts', () => {
            log.setAllowedContexts(['APP']);
            const consoleSpy = jest.spyOn(console, 'info');

            log.app.info('test');
            log.core.info('test');

            expect(consoleSpy).toHaveBeenCalledTimes(1);

            log.setAllowedContexts(['APP', 'CORE']);

            log.app.info('test');
            log.core.info('test');

            expect(consoleSpy).toHaveBeenCalledTimes(3);
        });
    });
}); 