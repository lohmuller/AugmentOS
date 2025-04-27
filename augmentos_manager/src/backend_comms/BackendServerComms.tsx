// backend_comms/BackendServerComms.ts
import axios, { AxiosRequestConfig } from 'axios';
import { Config } from 'react-native-config';
import GlobalEventEmitter from '../logic/GlobalEventEmitter';
import { log } from '../utils/logger';

interface Callback {
  onSuccess: (data: any) => void;
  onFailure: (errorCode: number) => void;
}

export default class BackendServerComms {
  private static instance: BackendServerComms;
  private TAG = 'MXT2_BackendServerComms';
  private serverUrl: string;
  private appStoreUrl: string;
  private coreToken: string | null = null;

  private constructor() {
    const secure = Config.AUGMENTOS_SECURE === 'true';
    const host = Config.AUGMENTOS_HOST;
    const port = Config.AUGMENTOS_PORT;
    const protocol = secure ? 'https' : 'http';
    this.serverUrl = `${protocol}://${host}:${port}`;
    this.appStoreUrl = `https://prod.augmentos.cloud`;
  }

  public getServerUrl(): string {
    return this.serverUrl;
  }

  public static getInstance(): BackendServerComms {
    if (!BackendServerComms.instance) {
      BackendServerComms.instance = new BackendServerComms();
    }
    return BackendServerComms.instance;
  }

  public setCoreToken(token: string | null): void {
    this.coreToken = token;
    log.core.info(`${this.TAG}: Core token ${token ? 'set' : 'cleared'}`);
  }

  public getCoreToken(): string | null {
    return this.coreToken;
  }


  public async restRequest(endpoint: string, data: any, callback: Callback): Promise<void> {
    try {
      const url = this.serverUrl + endpoint;

      // Axios request configuration
      const config: AxiosRequestConfig = {
        method: data ? 'POST' : 'GET',
        url: url,
        headers: {
          'Content-Type': 'application/json',
        },
        ...(data && { data }),
      };

      // Make the request
      const response = await axios(config);

      if (response.status === 200) {
        const responseData = response.data;
        if (responseData) {
          callback.onSuccess(responseData);
        } else {
          callback.onFailure(-1);
        }
      } else {
        log.core.error(`${this.TAG}: Error - ${response.statusText}`);
        callback.onFailure(response.status);
      }
    } catch (error: any) {
      log.app.error(`${this.TAG}: Network Error -`, error.message || error);
      callback.onFailure(-1);
    }
  }

  /**
   * Send error report to backend server
   * @param reportData The error report data
   * @returns Promise resolving to the response data, or rejecting with an error
   */
  public async sendErrorReport(reportData: any): Promise<any> {
    if (!this.coreToken) {
      throw new Error('No core token available for authentication');
    }

    const url = `${this.serverUrl}/app/error-report`;
    log.app.info('Sending error report to:', url);

    const config: AxiosRequestConfig = {
      method: 'POST',
      url,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.coreToken}`,
      },
      data: reportData,
    };

    try {
      const response = await axios(config);
      if (response.status === 200) {
        return response.data;
      } else {
        throw new Error(`Error sending report: ${response.statusText}`);
      }
    } catch (error: any) {
      log.app.error(`${this.TAG}: Error sending report -`, error.message || error);
      throw error;
    }
  }

  public async exchangeToken(supabaseToken: string): Promise<string> {
    const url = `${this.serverUrl}/auth/exchange-token`;
    const config: AxiosRequestConfig = {
      method: 'POST',
      url,
      headers: { 'Content-Type': 'application/json' },
      data: { supabaseToken },
    };

    try {
      const response = await axios(config);
      if (response.status === 200 && response.data) {
        log.app.info("GOT A RESPONSE!!!")
        log.app.info("\n\n");
        log.app.info(JSON.stringify(response.data));
        log.app.info("\n\n\n\n");
        // Store the token internally
        this.setCoreToken(response.data.coreToken);
        return response.data.coreToken;
      } else {
        throw new Error(`Bad response: ${response.statusText}`);
      }
    } catch (err) {
      throw err;
    }
  }

  public async getTpaSettings(tpaName: string): Promise<any> {
    if (!this.coreToken) {
      throw new Error('No core token available for authentication');
    }

    const url = `${this.serverUrl}/tpasettings/${tpaName}`;
    log.app.info('Fetching TPA settings from:', url);

    const config: AxiosRequestConfig = {
      method: 'GET',
      url,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.coreToken}`,
      },
    };

    try {
      const response = await axios(config);
      if (response.status === 200 && response.data) {
        log.app.info('Received TPA settings:', response.data);
        return response.data;
      } else {
        throw new Error(`Bad response: ${response.statusText}`);
      }
    } catch (error: any) {
      log.app.error('Error fetching TPA settings:', error.message || error);
      throw error;
    }
  }

  // New method to update a TPA setting on the server.
  public async updateTpaSetting(tpaName: string, update: { key: string; value: any }): Promise<any> {
    if (!this.coreToken) {
      throw new Error('No core token available for authentication');
    }

    const url = `${this.serverUrl}/tpasettings/${tpaName}`;
    log.app.info('Updating TPA settings via:', url);

    const config: AxiosRequestConfig = {
      method: 'POST',
      url,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.coreToken}`,
      },
      data: update,
    };

    try {
      const response = await axios(config);
      if (response.status === 200 && response.data) {
        log.app.info('Updated TPA settings:', response.data);
        return response.data;
      } else {
        throw new Error(`Bad response: ${response.statusText}`);
      }
    } catch (error: any) {
      log.app.error('Error updating TPA settings:', error.message || error);
      throw error;
    }
  }

  /**
 * Start an app using the REST API
 * @param packageName Package name of the app to start
 * @returns Response including app state
 */
  public async startApp(packageName: string): Promise<any> {
    if (!this.coreToken) {
      throw new Error('No core token available for authentication');
    }

    const url = `${this.serverUrl}/apps/${packageName}/start`;
    log.app.info('Starting app:', packageName);

    const config: AxiosRequestConfig = {
      method: 'POST',
      url,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.coreToken}`,
      },
    };

    try {
      const response = await axios(config);
      if (response.status === 200 && response.data) {
        log.app.info('App started successfully:', packageName);
        return response.data;
      } else {
        throw new Error(`Bad response: ${response.statusText}`);
      }
    } catch (error: any) {
      //console.error('Error starting app:', error.message || error);
      //GlobalEventEmitter.emit('SHOW_BANNER', { message: 'Error starting app: ' + error.message || error, type: 'error' })
      GlobalEventEmitter.emit('SHOW_BANNER', { message: `Could not connect to ${packageName}`, type: "error" });
      throw error;
    }
  }

  /**
   * Stop an app using the REST API
   * @param packageName Package name of the app to stop
   * @returns Response including app state
   */
  public async stopApp(packageName: string): Promise<any> {
    if (!this.coreToken) {
      throw new Error('No core token available for authentication');
    }

    const url = `${this.serverUrl}/apps/${packageName}/stop`;
    log.app.info('Stopping app:', packageName);

    const config: AxiosRequestConfig = {
      method: 'POST',
      url,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.coreToken}`,
      },
    };

    try {
      const response = await axios(config);
      if (response.status === 200 && response.data) {
        log.app.info('App stopped successfully:', packageName);
        return response.data;
      } else {
        throw new Error(`Bad response: ${response.statusText}`);
      }
    } catch (error: any) {
      log.app.error('Error stopping app:', error.message || error);
      throw error;
    }
  }

  /**
   * Uninstall an app using the REST API
   * @param packageName Package name of the app to uninstall
   * @returns Response including uninstallation status
   */
  public async uninstallApp(packageName: string): Promise<any> {
    if (!this.coreToken) {
      throw new Error('No core token available for authentication');
    }

    const url = `${this.appStoreUrl}/api/apps/uninstall/${packageName}`;
    log.app.info('Uninstalling app:', packageName);

    const config: AxiosRequestConfig = {
      method: 'POST',
      url,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.coreToken}`,
      },
    };

    try {
      const response = await axios(config);
      if (response.status === 200 && response.data) {
        log.app.info('App uninstalled successfully:', packageName);
        return response.data;
      } else {
        throw new Error(`Bad response: ${response.statusText}`);
      }
    } catch (error: any) {
      log.app.error('Error uninstalling app:', error.message || error);
      throw error;
    }
  }

  /**
   * Requests a temporary, single-use token for webview authentication.
   * @param packageName The package name of the TPA the token is for.
   * @returns Promise resolving to the temporary token string.
   * @throws Error if the request fails or no core token is available.
   */
  public async generateWebviewToken(packageName: string): Promise<string> {
    if (!this.coreToken) {
      throw new Error('Authentication required: No core token available.');
    }

    const url = `${this.serverUrl}/api/auth/generate-webview-token`;
    log.app.info('Requesting webview token for:', packageName, 'at URL:', url);

    const config: AxiosRequestConfig = {
      method: 'POST',
      url,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.coreToken}`, // Use the stored coreToken
      },
      data: { packageName }, // Send the target package name in the body
    };

    try {
      const response = await axios(config);
      if (response.status === 200 && response.data.success && response.data.token) {
        log.app.info(`Received temporary webview token for ${packageName}`);
        return response.data.token;
      } else {
        throw new Error(`Failed to generate webview token: ${response.data.error || response.statusText}`);
      }
    } catch (error: any) {
      log.app.error(`${this.TAG}: Error generating webview token -`, error.message || error);
      // Consider more specific error handling based on response status if available
      if (axios.isAxiosError(error) && error.response) {
        throw new Error(`Failed to generate webview token: ${error.response.data?.error || error.message}`);
      }
      throw error; // Re-throw the original error or a new one
    }
  }

  public async hashWithApiKey(stringToHash: string, packageName: string): Promise<string> {
    if (!this.coreToken) {
      throw new Error('No core token available for authentication');
    }

    const url = `${this.serverUrl}/api/auth/hash-with-api-key`;

    const config: AxiosRequestConfig = {
      method: 'POST',
      url,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.coreToken}`,
      },
      data: {
        stringToHash,
        packageName
      },
    };

    try {
      const response = await axios(config);
      if (response.status === 200 && response.data.success) {
        return response.data.hash;
      } else {
        throw new Error(`Failed to generate hash: ${response.data.error || response.statusText}`);
      }
    } catch (error: any) {
      log.app.error(`${this.TAG}: Error generating hash:`, error.message || error);
      throw error;
    }
  }
}
