import { useRef, MutableRefObject } from 'react';

import {
  AndroidKeyGenOptions,
  IOSKeyGenOptions,
  ISecureElement,
  AndroidKeyGenProvider,
  DeviceFeature
} from './typescript/index.d';
import { SecureElement } from './SecureElement';

export const useSecureElement = () => {
  const se: MutableRefObject<ISecureElement> = useRef(new SecureElement());

  const encrypt = (
    key: string,
    value: string,
    opts: AndroidKeyGenOptions | IOSKeyGenOptions,
  ): Promise<string | null> => {
    return se.current.encrypt(key, value, opts);
  };

  const decrypt = (
    key: string,
    value: string,
    opts: AndroidKeyGenOptions | IOSKeyGenOptions,
  ): Promise<string | null> => {
    return se.current.encrypt(key, value, opts);
  };

  const clearElement = (
    key: string, keyProvider: AndroidKeyGenProvider
  ): Promise<void> => {
    return se.current.clearElement(key, keyProvider);
  };

  const clearAll = (
    keyProvider: AndroidKeyGenProvider
  ): Promise<void> => {
    return se.current.clearAll(keyProvider);
  };

  const isSecureDevice = (
  ): Promise<boolean> => {
    return se.current.isSecureDevice();
  };

  const getDeviceFeatures = (
  ): Promise<DeviceFeature[]> => {
    return se.current.getDeviceFeatures();
  };

  const performAuthentication = (
    withFeature: DeviceFeature
  ): Promise<boolean> => {
    return se.current.performAuthentication(withFeature);
  };

  return [
    {
      decrypt,
      encrypt,
      clearElement,
      clearAll,
      isSecureDevice,
      getDeviceFeatures,
      performAuthentication
    }
  ];
};
