// ManagerApp/src/IntentSender.ts

import { NativeModules } from 'react-native';
import { log } from './utils/logger';

const { IntentSender } = NativeModules;

interface ProtocolData {
  version: string;
  action: string;
  request_id?: string;
  parameters?: {
    app_id?: string;
    setting_key?: string;
    setting_value?: string | number | boolean;
  };
}

const sendProtocolIntent = async (protocolData: ProtocolData): Promise<void> => {
  try {
    const jsonPayload = JSON.stringify(protocolData);
    IntentSender.sendIntent(jsonPayload);
    log.app.info('Intent sent successfully');
  } catch (error) {
    log.app.error('Error sending intent:', error);
  }
};

export default sendProtocolIntent;
