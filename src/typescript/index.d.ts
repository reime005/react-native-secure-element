export interface ISecureElement {
  configure(opts: SecureElementOptions): Promise<void>;
  decrypt(key: string, value: string, opts: AndroidKeyGenOptions): Promise<string | null>;
  encrypt(key: string, value: string, opts: AndroidKeyGenOptions): Promise<string | null>;
  clearElement(key: string): Promise<void>;
  clearAll(): Promise<void>;
  isSecureDevice(): Promise<boolean>;
  getDeviceFeatures(): Promise<DeviceFeature[]>;
  performAuthentication(withFeature: DeviceFeature): Promise<boolean>;
}

export interface ISecureElementNativeModule {
  configure(opts: SecureElementOptions, callback: (errors: Error) => void): void;
  decrypt(
    key: string,
    value: string,
    opts: AndroidKeyGenOptions,
    callback: (errors: Error, result: string) => void
  ): void;
  encrypt(
    key: string,
    value: string,
    opts: AndroidKeyGenOptions,
    callback: (errors: Error, result: string) => void
  ): void;
  clearElement(key: string, callback: (errors: Error) => void): void;
  clearAll(callback: (errors: Error) => void): void;
  isSecureDevice(callback: (errors: Error, isSecureDevice: boolean) => void): void;
  getDeviceFeatures(callback: (errors: Error, deviceFeatures: [DeviceFeature]) => void): void;
  performAuthentication(
    withFeature: DeviceFeature,
    callback: (errors: Error, success: boolean) => void
  ): void;
}

export type DeviceFeature =
  | 'IOS_PASSCODELOCK'
  | 'IOS_TOUCHID'
  | 'IOS_FACEID'
  | 'IOS_BIOMETRICS'
  | 'ANDROID_FINGERPRINT'
  | 'ANDROID_DEVICE_SECURE';
export type AndroidKeyGenAlgorithm = 'RSA'; //TODO: [mr] see docs
export type AndroidKeyGenProvider = 'AndroidKeyStore'; //TODO: [mr] see docs
export type AndroidKeyGenPurpose = 'ENCRYPT' | 'DECRYPT' | 'SIGN' | 'VERIFY';
export type AndroidKeyGenBlockMode = 'ECB' | 'CBC' | 'CTR' | 'GCM';
export type AndroidKeyGenEncryptionPadding =
  | 'NoPadding'
  | 'PKCS7Padding'
  | 'PKCS1Padding'
  | 'OAEPPadding';

export type IOSSecAccessControlCreateFlags =
  | 'kSecAccessControlUserPresence'
  | 'kSecAccessControlBiometryAny'
  | 'kSecAccessControlTouchIDAny'
  | 'kSecAccessControlBiometryCurrentSet'
  | 'kSecAccessControlTouchIDCurrentSet'
  | 'kSecAccessControlDevicePasscode'
  | 'kSecAccessControlOr'
  | 'kSecAccessControlAnd'
  | 'kSecAccessControlPrivateKeyUsage'
  | 'kSecAccessControlApplicationPassword';

export type IOSSecAttrAccessible =
  | 'kSecAttrAccessibleWhenUnlocked'
  | 'kSecAttrAccessibleAfterFirstUnlock'
  | 'kSecAttrAccessibleAlways'
  | 'kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly'
  | 'kSecAttrAccessibleWhenUnlockedThisDeviceOnly'
  | 'kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly'
  | 'kSecAttrAccessibleAlwaysThisDeviceOnly';

export type IOSAlgorithm = 'SHA1' | 'SHA224' | 'SHA384' | 'SHA256' | 'SHA512';

export type IOSSecAttrType = 'EC' | 'ECSECPrimeRandom';

export interface AndroidKeyGenOptions {
  keyPairGeneratorAlgorithm: AndroidKeyGenAlgorithm;
  keyPairGeneratorProvider: AndroidKeyGenProvider;
  keyGenBlockMode: AndroidKeyGenBlockMode;
  keyGenEncryptionPadding: AndroidKeyGenEncryptionPadding;
  keyGenUserAuthenticationRequired: boolean;
  keyGenInvalidatedByBiometricEnrollment: boolean;
  userAuthenticationValidityDurationSeconds: number;
  purposes: AndroidKeyGenPurpose[];
}

export interface IOSKeyGenOptions {
  userPrompt: string;
  privateSACFlags: IOSSecAccessControlCreateFlags[] | [];
  publicSACFlags: IOSSecAccessControlCreateFlags[] | [];
  privateSACAccessible: IOSSecAttrAccessible | '';
  publicSACAccessible: IOSSecAttrAccessible | '';
  secAttrType: IOSSecAttrType;
  saveInSecureEnclaveIfPossible: boolean;
  algorithm?: IOSAlgorithm; // defaults to "SHA256"
  privateKeySizeInBits: number;
  publicKeyName: string;
  privateKeyName: string;
  touchIDAuthenticationAllowableReuseDuration?: number; // defaults to "300"
}

export interface SecureElementOptions {
  keystoreType: AndroidKeyGenProvider;
}
