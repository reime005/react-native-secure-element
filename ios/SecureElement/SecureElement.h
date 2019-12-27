//
//  SecureElement.h
//
//  Created by Marius Reimer on 15/20/2019.
//  Copyright © 2019-now Marius Reimer. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <CommonCrypto/CommonCryptor.h>
#import <CommonCrypto/CommonDigest.h>

#import <LocalAuthentication/LocalAuthentication.h>

/**
 * Encrypting and decrypting elements securely, using on-device hardware security features.
 */
@interface SecureElement : NSObject

struct KeyGenParameters {
  SecAccessControlCreateFlags privateKeySACFlags;
  SecAccessControlCreateFlags publicKeySACFlags;
  CFStringRef privateSACAccessible;
  CFStringRef publicSACAccessible;
  
  NSString *privateKeyName;
  NSString *publicKeyName;
  int privateKeySizeInBits;
  CFStringRef attrKeyType;
  BOOL saveInSecureEnclaveIfPossible;
  NSString *userPrompt;
  CFStringRef algorithm;
};

- (id)initWithContext:(LAContext *)context;

- (NSString *) encrypt:(NSString *)value withParameters:(struct KeyGenParameters *)params error:(CFErrorRef *)error;
- (NSString *) decrypt:(NSString *)value withParameters:(struct KeyGenParameters *)params error:(CFErrorRef *)error;

- (bool) deviceHasPassCode:(NSError *)error;
- (bool) deviceHasBiometrics:(NSError *)error;
- (bool) deviceHasTouchID:(NSError *)error;
- (bool) deviceHasFaceID:(NSError *)error;

- (void) clearElement:(NSString *)keyName error:(NSError *)error;
- (void) clearAll:(NSError *)error;

- (void) changeContext:(LAContext *)context;

- (struct KeyGenParameters) getDefaultKeyGenParameters;

#define newCFDict CFDictionaryCreateMutable(kCFAllocatorDefault, 0, &kCFTypeDictionaryKeyCallBacks, &kCFTypeDictionaryValueCallBacks)

@end
