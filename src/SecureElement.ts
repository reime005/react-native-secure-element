import { AndroidKeyGenProvider } from './../../../../../src/typescript/index.d';
import { NativeModules } from 'react-native';

import {
  ISecureElement,
  ISecureElementNativeModule,
  SecureElementOptions,
  DeviceFeature,
  AndroidKeyGenOptions,
} from './typescript/index.d';

export class SecureElement implements ISecureElement {
  private readonly _secureElementModule: ISecureElementNativeModule;

  constructor() {
    this._secureElementModule = NativeModules.RNSecureElementModule;

    if (!this._secureElementModule) {
      throw new Error('[react-native-secure-element]: NativeModule: RNSecureElement is null.');
    }
  }

  decrypt(key: string, value: string, opts: AndroidKeyGenOptions) {
    return new Promise<string | null>((resolve, reject) => {
      this._secureElementModule.decrypt(key, value, opts, (error: Error, result: string) => {
        console.warn({ error, result });

        if (error) {
          reject(error);
        } else {
          resolve(result);
        }
      });
    });
  }

  encrypt(key: string, value: string, opts: AndroidKeyGenOptions) {
    return new Promise<string | null>((resolve, reject) => {
      this._secureElementModule.encrypt(key, value, opts, (error: Error, result: string) => {
        if (error) {
          reject(error);
        } else {
          resolve(result);
        }
      });
    });
  }

  clearElement(key: string, keyProvider: AndroidKeyGenProvider) {
    return new Promise<void>((resolve, reject) => {
      this._secureElementModule.clearElement(key, keyProvider, (error: Error) => {
        if (error) {
          reject(error);
        } else {
          resolve();
        }
      });
    });
  }

  clearAll(keyProvider: AndroidKeyGenProvider) {
    return new Promise<void>((resolve, reject) => {
      this._secureElementModule.clearAll(keyProvider, (error: Error) => {
        if (error) {
          reject(error);
        } else {
          resolve();
        }
      });
    });
  }

  isSecureDevice() {
    return new Promise<boolean>((resolve, reject) => {
      this._secureElementModule.isSecureDevice((error: Error, isSecureDevice: boolean) => {
        if (error) {
          reject(error);
        } else {
          resolve(isSecureDevice);
        }
      });
    });
  }

  getDeviceFeatures() {
    return new Promise<[DeviceFeature]>((resolve, reject) => {
      this._secureElementModule.getDeviceFeatures(
        (error: Error, deviceFeatures: [DeviceFeature]) => {
          if (error) {
            reject(error);
          } else {
            resolve(deviceFeatures);
          }
        }
      );
    });
  }

  performAuthentication(withFeature: DeviceFeature) {
    return new Promise<boolean>((resolve, reject) => {
      this._secureElementModule.performAuthentication(
        withFeature,
        (error: Error, success: boolean) => {
          if (error) {
            reject(error);
          } else {
            resolve(success);
          }
        }
      );
    });
  }
}
