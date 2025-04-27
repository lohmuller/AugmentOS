import { NativeModule, NativeModules, NativeEventEmitter, Platform, PermissionsAndroid } from 'react-native';
import { log } from '../utils/logger';

interface NotificationServiceInterface extends NativeModule {
  isNotificationListenerEnabled: () => Promise<boolean>;
  startNotificationListenerService: () => Promise<string>;
  stopNotificationListenerService: () => Promise<string>;
  addListener: (eventName: string) => void;
  removeListeners: (count: number) => void;
}

// Access the native module
const { NotificationServiceUtils } = NativeModules;

// Typecast it to the defined interface
export const NotificationService = NotificationServiceUtils as NotificationServiceInterface;

// Export a properly scoped event emitter for notifications
export const NotificationEventEmitter = new NativeEventEmitter(NotificationService);

export const requestNotificationPermission = async (): Promise<boolean> => {
  const requestResult = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS);
  if (requestResult === PermissionsAndroid.RESULTS.GRANTED) {
    log.app.info('Notification permission granted.');
    return true;
  } else if (requestResult === PermissionsAndroid.RESULTS.DENIED) {
    log.app.warn('Notification permission denied.');
    return false;
  } else if (requestResult === PermissionsAndroid.RESULTS.NEVER_ASK_AGAIN) {
    log.app.warn('Notification permission set to never ask again.');
    return false;
  } else {
    return false;
  }
}

export const checkNotificationPermission = async (): Promise<boolean> => {
  if (Platform.OS === 'android' && Platform.Version >= 33) {
    try {
      const hasPermission = await PermissionsAndroid.check(PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS);
      if (hasPermission) {
        log.app.info('Notification permission already granted.');
        return true;
      } else {
        log.app.warn("We do not have the notification permission");
        return false;
      }
    } catch (error) {
      log.app.error('Error checking/requesting notification permission:', error);
      return false;
    }
  } else {
    log.app.info('Notification permissions are not required for this platform or version.');
    return true;
  }
  return false;
};
