import { log, LogLevel } from '../../src/utils/logger';

describe('Logger', () => {
    beforeEach(() => {
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

    describe('Basic Logging', () => {
        it('should log messages at all levels', () => {
            log.debug('Basic debug message');
            log.info('Basic info message');
            log.warn('Basic warning message');
            log.error('Basic error message');

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
}); 