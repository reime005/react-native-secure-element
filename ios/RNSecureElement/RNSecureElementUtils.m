//
//  RNSecureElement.m
//
//  Created by Marius Reimer on 25.10.19.
//  Copyright © 2019-now Nect GmbH. All rights reserved.
//

#import "RNSecureElementUtils.h"

#pragma mark - RNSecureElementUtils

@implementation RNSecureElementUtils

+ (struct KeyGenParameters) getKeyGenParamsForDict:(NSDictionary *)opts
{
  struct  KeyGenParameters params;

  if (opts == nil || opts.allKeys == nil || opts.allKeys.count == 0) {
    //TODO: improve error handling
    return params;
  }

  params.algorithm = [self getAlgorithmForKey:opts[@"algorithm"]];
  params.attrKeyType = [self getSecAttrKeyType:opts[@"secAttrType"]];
  params.saveInSecureEnclaveIfPossible = opts[@"saveInSecureEnclaveIfPossible"] != nil ? kSecAttrTokenIDSecureEnclave : nil;
  params.privateKeyName = opts[@"privateKeyName"];
  params.publicKeyName = opts[@"publicKeyName"];
  params.userPrompt = opts[@"userPrompt"] != nil ? opts[@"userPrompt"] : @"";
  params.privateSACAccessible = [self getSACAccessibleConstant:opts[@"privateSACAccessible"]];
  params.publicSACAccessible = [self getSACAccessibleConstant:opts[@"publicSACAccessible"]];
  params.privateKeySACFlags = [self calculateSACFlags:opts[@"privateSACFlags"]];
  params.publicKeySACFlags = [self calculateSACFlags:opts[@"publicSACFlags"]];
  params.privateKeySizeInBits = [opts[@"privateKeySizeInBits"] intValue];

  return params;
}

+ (SecKeyAlgorithm) getSignatureMessage:(NSString *)key API_AVAILABLE(ios(10.0))
{
  if ([key isEqualToString:@"SHA1"]) {
    return kSecKeyAlgorithmECDSASignatureMessageX962SHA1;
  } else if ([key isEqualToString:@"SHA224"]) {
     return kSecKeyAlgorithmECDSASignatureMessageX962SHA224;
  } else if ([key isEqualToString:@"SHA384"]) {
    return kSecKeyAlgorithmECDSASignatureMessageX962SHA384;
  } else if ([key isEqualToString:@"SHA512"]) {
     return kSecKeyAlgorithmECDSASignatureMessageX962SHA512;
  }

  return kSecKeyAlgorithmECDSASignatureMessageX962SHA256;
}

+ (SecKeyAlgorithm) getAlgorithmForKey:(NSString *)key API_AVAILABLE(ios(10.0))
{
  if ([key isEqualToString:@"SHA1"]) {
    return kSecKeyAlgorithmECIESEncryptionStandardX963SHA1AESGCM;
  } else if ([key isEqualToString:@"SHA224"]) {
     return kSecKeyAlgorithmECIESEncryptionStandardX963SHA224AESGCM;
  } else if ([key isEqualToString:@"SHA384"]) {
    return kSecKeyAlgorithmECIESEncryptionStandardX963SHA384AESGCM;
  } else if ([key isEqualToString:@"SHA512"]) {
     return kSecKeyAlgorithmECIESEncryptionStandardX963SHA512AESGCM;
  }

  return kSecKeyAlgorithmECIESEncryptionStandardX963SHA256AESGCM;
}

+ (CFStringRef) getSecAttrKeyType:(NSString *)name
{
  if ([name isEqualToString:@"EC"]) {
    return kSecAttrKeyTypeEC;
  }

  return kSecAttrKeyTypeECSECPrimeRandom;
}

+ (CFStringRef) getSACAccessibleConstant:(NSString *)constant
{
  if ([constant isEqualToString:@"kSecAttrAccessibleWhenUnlocked"]) {
    return kSecAttrAccessibleWhenUnlocked;
  }

  if ([constant isEqualToString:@"kSecAttrAccessibleAfterFirstUnlock"]) {
    return kSecAttrAccessibleAfterFirstUnlock;
  }

  if ([constant isEqualToString:@"kSecAttrAccessibleAlways"]) {
    return kSecAttrAccessibleAlways;
  }

  if ([constant isEqualToString:@"kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly"]) {
    return kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly;
  }

  if ([constant isEqualToString:@"kSecAttrAccessibleWhenUnlockedThisDeviceOnly"]) {
    return kSecAttrAccessibleWhenUnlockedThisDeviceOnly;
  }

  if ([constant isEqualToString:@"kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly"]) {
    return kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly;
  }

  if ([constant isEqualToString:@"kSecAttrAccessibleAlwaysThisDeviceOnly"]) {
    return kSecAttrAccessibleAlwaysThisDeviceOnly;
  }

  return 0;
}

+ (SecAccessControlCreateFlags) calculateSACFlags:(NSArray *)array {
  SecAccessControlCreateFlags flags = 0;

  if ([array containsObject:@"kSecAccessControlUserPresence"]) {
    flags |= kSecAccessControlUserPresence;
  }

  if ([array containsObject:@"kSecAccessControlBiometryAny"]) {
    if (@available(iOS 11.3, *)) {
      flags |= kSecAccessControlBiometryAny;
    }
  }

  if ([array containsObject:@"kSecAccessControlTouchIDAny"]) {
    flags |= kSecAccessControlTouchIDAny;
  }

  if ([array containsObject:@"kSecAccessControlBiometryCurrentSet"]) {
    if (@available(iOS 11.3, *)) {
      flags |= kSecAccessControlBiometryCurrentSet;
    }
  }

  if ([array containsObject:@"kSecAccessControlTouchIDCurrentSet"]) {
    flags |= kSecAccessControlTouchIDCurrentSet;
  }

  if ([array containsObject:@"kSecAccessControlDevicePasscode"]) {
    flags |= kSecAccessControlDevicePasscode;
  }

  if ([array containsObject:@"kSecAccessControlOr"]) {
    flags |= kSecAccessControlOr;
  }

  if ([array containsObject:@"kSecAccessControlAnd"]) {
    flags |= kSecAccessControlAnd;
  }

  if ([array containsObject:@"kSecAccessControlPrivateKeyUsage"]) {
    flags |= kSecAccessControlPrivateKeyUsage;
  }

  if ([array containsObject:@"kSecAccessControlApplicationPassword"]) {
    flags |= kSecAccessControlApplicationPassword;
  }

  return flags;
}

@end
