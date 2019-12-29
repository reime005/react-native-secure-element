//
//  SecureElement.m
//
//  Created by Marius Reimer on 15/20/2019.
//  Copyright © 2019-now Marius Reimer. All rights reserved.
//

#import "SecureElement.h"

#pragma mark - SecureElement

@implementation SecureElement

{
  LAContext* mContext;
}

- (id)initWithContext:(LAContext *)context
{
    if ((self = [super init])) {
      mContext = context;
    }

    return self;
}

- (void) changeContext:(LAContext *)context {
  mContext = context;
}

- (instancetype)init
{
  self = [super init];
  if (self) {
    mContext = [[LAContext alloc] init];
  }
  return self;
}

- (NSString *) encrypt:(NSString *)value
  withParameters:(struct KeyGenParameters *)params
           error:(CFErrorRef *)error
{
  // 1) look for availability of private key, create new pair if necessary
  SecKeyRef privateKeyRef = [self getOrCreateKeyWithOptions:params error:error];

  if (privateKeyRef == nil) {
   // error = CFErrorCreate(kCFAllocatorDefault, nil, nil, @{NSLocalizedDescriptionKey:@"Private key could not be retrieved."});
    return nil;
  }

  // 2) retrieve public key if available
  SecKeyRef publicKeyRef = SecKeyCopyPublicKey(privateKeyRef);

  if (publicKeyRef == nil) {
    error = CFErrorCreate(kCFAllocatorDefault, nil, nil, @{NSLocalizedDescriptionKey:@"Public key could not be retrieved."});
    return nil;
  }

  // 3) encrypt data
  NSString *encryptedData = [self encryptData:value withAlgorithm:params->algorithm publicKey:publicKeyRef error:error];

  return encryptedData;
}

- (NSString *) decrypt:(NSString *)value
  withParameters:(struct KeyGenParameters *)params
           error:(CFErrorRef *)error
{
  // 1) look for availability of private key
  SecKeyRef privateKeyRef = [self getOrCreateKeyWithOptions:params error:error];

  // lead to error if not available (not creating new keys here)
  if (privateKeyRef == nil) {
    //error = CFErrorCreate(kCFAllocatorDefault, nil, nil, @{NSLocalizedDescriptionKey:@"Private key could not be retrieved."});
    return nil;
  }

  // 3) decrypt data
  NSString *decryptedData = [self decryptData:value withAlgorithm:params->algorithm privateKey:privateKeyRef error:error];

  return decryptedData;
}

- (bool) deviceHasPassCode:(NSError *)error
{
  return [mContext canEvaluatePolicy:LAPolicyDeviceOwnerAuthentication error:&error];
}

- (bool) deviceHasBiometrics:(NSError *)error
{
  return [mContext canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error];
}

- (bool) deviceHasTouchID:(NSError *)error
{
  if ([mContext canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error]) {
    if (@available(iOS 11.0, *)) {
      return mContext.biometryType == LABiometryTypeTouchID;
    }
    return true;
  }

  return false;
}

- (bool) deviceHasFaceID:(NSError *)error
{
  if ([mContext canEvaluatePolicy:LAPolicyDeviceOwnerAuthenticationWithBiometrics error:&error]) {
    if (@available(iOS 11.0, *)) {
      return mContext.biometryType == LABiometryTypeFaceID;
    }
    return true;
  }

  return false;
}

- (void)clearElement:(NSString *)keyName error:(NSError *)error
{
  @try {
    [self deleteKey:keyName];
  } @catch (NSException *exception) {
    error = [NSError errorWithDomain:@"react.native.secure.element" code:0 userInfo:exception.userInfo];
  }
}

- (void) clearAll:(NSError *)error
{
  @try {
    NSArray *secItemClasses = @[(__bridge id)kSecClassKey];
    for (id secItemClass in secItemClasses) {
        NSDictionary *spec = @{(__bridge id)kSecClass: secItemClass};
        SecItemDelete((__bridge CFDictionaryRef)spec);
    }
  } @catch (NSException *exception) {
    error = [NSError errorWithDomain:@"react.native.secure.element" code:0 userInfo:exception.userInfo];
  }
}

#pragma mark - Encryption

- (NSString *) encryptData:(NSString *)value
             withAlgorithm:(CFStringRef)algorithm
                 publicKey:(SecKeyRef)publicKey
                     error:(CFErrorRef *)error
{
  NSData *data = [value dataUsingEncoding: NSUnicodeStringEncoding];

  CFDataRef encrypted = SecKeyCreateEncryptedData(publicKey, algorithm, (__bridge CFDataRef)data, error);

  return [encrypted base64EncodedStringWithOptions:0];
}

#pragma mark - Decryption

- (NSString *) decryptData:(NSString *)value
             withAlgorithm:(CFStringRef)algorithm
                privateKey:(SecKeyRef)privateKey
                     error:(CFErrorRef *)error
{
  NSData *data = [[NSData alloc] initWithBase64EncodedString:value options:0];

  CFDataRef decrypted = SecKeyCreateDecryptedData(privateKey, algorithm, (__bridge CFDataRef)data, error);

  return [[NSString alloc] initWithData:decrypted encoding:NSUnicodeStringEncoding];
}

#pragma mark - Helpers

- (SecKeyRef) getOrCreateKeyWithOptions:(struct KeyGenParameters *)params
                                  error:(CFErrorRef *)error
{
  SecKeyRef key = NULL;

  @try {
    key = [self getPrivateKeyForName:params->privateKeyName withPrompt:params->userPrompt withKeySizeInBits:params->privateKeySizeInBits];
  } @catch (NSException *exception) {
    [self deleteKey:params->publicKeyName];
    [self deleteKey:params->privateKeyName];
  }

  if (key == nil) {
    key = [self generateKeyPairWithParams:params error:error];
  }

  return key;
}

- (SecKeyRef) getPrivateKeyForName:(NSString *)keyName
                        withPrompt:(NSString *)userPrompt
                 withKeySizeInBits:(int)keySize
{
  SecKeyRef key = NULL;

  CFMutableDictionaryRef getPrivateKeyQuery = newCFDict;
  CFDictionarySetValue(getPrivateKeyQuery, kSecClass, kSecClassKey);
  CFDictionarySetValue(getPrivateKeyQuery, kSecAttrLabel, (__bridge const void *)(keyName));
  CFDictionarySetValue(getPrivateKeyQuery, kSecAttrApplicationTag, (__bridge const void *)(keyName));
  CFDictionarySetValue(getPrivateKeyQuery, kSecReturnRef, kCFBooleanTrue);
  CFDictionarySetValue(getPrivateKeyQuery, kSecUseOperationPrompt, userPrompt);
  CFDictionarySetValue(getPrivateKeyQuery, kSecUseAuthenticationContext, mContext);

  if (keySize != 0 && keySize % 2 == 0) {
    CFDictionarySetValue(getPrivateKeyQuery, kSecAttrKeySizeInBits, (__bridge const void *)([NSNumber numberWithInt:keySize]));
  } else {
    CFDictionarySetValue(getPrivateKeyQuery, kSecAttrKeySizeInBits, (__bridge const void *)([NSNumber numberWithInt:256]));
  }

  OSStatus status = SecItemCopyMatching(getPrivateKeyQuery, (CFTypeRef *)&key);
  if (status == errSecSuccess)
    return (SecKeyRef)key;

  return nil;
}

- (SecKeyRef) generateKeyPairWithParams:(struct KeyGenParameters *)params
                                  error:(CFErrorRef *)error
{
  CFErrorRef t1 = NULL;
  SecAccessControlRef privateSAC = SecAccessControlCreateWithFlags(kCFAllocatorDefault,
                                                                   params->privateSACAccessible,
                                                                   params->privateKeySACFlags,
                                                                   &t1);

  CFErrorRef t2 = NULL;
  SecAccessControlRef publicSAC = SecAccessControlCreateWithFlags(kCFAllocatorDefault,
                                                                  params->publicSACAccessible,
                                                                  params->publicKeySACFlags,
                                                                  &t2);

  if (privateSAC == nil || publicSAC == nil) {
    return nil;
  }

  // create dict of private key info
  CFMutableDictionaryRef publicAccessControlDict = newCFDict;
  CFDictionaryAddValue(publicAccessControlDict, kSecAttrAccessControl, publicSAC);
  CFDictionaryAddValue(publicAccessControlDict, kSecAttrLabel, params->publicKeyName);
  CFDictionarySetValue(publicAccessControlDict, kSecAttrApplicationTag, params->publicKeyName);

  CFMutableDictionaryRef privateAccessControlDict = newCFDict;
  CFDictionaryAddValue(privateAccessControlDict, kSecAttrAccessControl, privateSAC);
  CFDictionaryAddValue(privateAccessControlDict, kSecAttrIsPermanent, kCFBooleanTrue); /* SAVE KEY */
  CFDictionaryAddValue(privateAccessControlDict, kSecAttrLabel, params->privateKeyName);
  CFDictionarySetValue(privateAccessControlDict, kSecAttrApplicationTag, params->privateKeyName);

  CFDictionarySetValue(privateAccessControlDict, kSecUseAuthenticationContext, mContext);

  // create dict which actually saves key into keychain
  CFMutableDictionaryRef generatePairRef = newCFDict;

  if (TARGET_OS_SIMULATOR == 0 && [self deviceHasPassCode:nil]) {
    CFDictionaryAddValue(generatePairRef, kSecAttrTokenID, kSecAttrTokenIDSecureEnclave);
  }

  CFDictionaryAddValue(generatePairRef, kSecAttrKeyType, params->attrKeyType);

  if (params->privateKeySizeInBits != 0 && params->privateKeySizeInBits % 2 == 0) {
    CFDictionaryAddValue(generatePairRef, kSecAttrKeySizeInBits, (__bridge const void *)([NSNumber numberWithInt:params->privateKeySizeInBits]));
  } else {
    CFDictionaryAddValue(generatePairRef, kSecAttrKeySizeInBits, (__bridge const void *)([NSNumber numberWithInt:256]));
  }

  CFDictionaryAddValue(generatePairRef, kSecPrivateKeyAttrs, privateAccessControlDict);
  CFDictionaryAddValue(generatePairRef, kSecPublicKeyAttrs, publicAccessControlDict);

  /* GENERATE KEYPAIR */

  SecKeyRef privateKey = SecKeyCreateRandomKey(generatePairRef, error);

  CFMutableDictionaryRef saveDict = newCFDict;
  CFDictionarySetValue(saveDict, kSecValueRef, privateKey);
  CFDictionarySetValue(saveDict, kSecClass, kSecClassKey);
  CFDictionarySetValue(saveDict, kSecAttrApplicationTag, params->privateKeyName);
  CFDictionarySetValue(saveDict, kSecAttrLabel, params->privateKeyName);

  OSStatus saveStatus = SecItemAdd(saveDict, nil);

  if (saveStatus == errSecDuplicateItem) {
    SecItemDelete(saveDict);
    saveStatus = SecItemAdd(saveDict, nil);
  }

  if (saveStatus == errSecSuccess)
    return privateKey;
  else
    return nil;
}

- (bool) deleteKey:(NSString *)keyName
{
  CFMutableDictionaryRef savePublicKeyDict = newCFDict;
  CFDictionaryAddValue(savePublicKeyDict, kSecClass, kSecClassKey);
  CFDictionaryAddValue(savePublicKeyDict, kSecAttrApplicationTag, keyName);

  OSStatus err = SecItemDelete(savePublicKeyDict);
  while (err == errSecDuplicateItem)
  {
    err = SecItemDelete(savePublicKeyDict);
  }
  return true;
}

- (struct KeyGenParameters) getDefaultKeyGenParameters
{
  struct KeyGenParameters params;

  params.algorithm = kSecKeyAlgorithmECIESEncryptionStandardX963SHA256AESGCM;
  params.attrKeyType = kSecAttrKeyTypeECSECPrimeRandom;
  params.saveInSecureEnclaveIfPossible = true;
  params.privateKeyName = @"defaultPrivateKeyName";
  params.publicKeyName = @"defaultPublicKeyName";
  params.userPrompt = @"default Prompt";
  params.privateSACAccessible = kSecAttrAccessibleWhenUnlockedThisDeviceOnly;
  params.publicSACAccessible = kSecAttrAccessibleAlwaysThisDeviceOnly;
  params.privateKeySACFlags = 0;
  params.publicKeySACFlags = 0;
  params.privateKeySizeInBits = 256;

  return params;
}

@end
