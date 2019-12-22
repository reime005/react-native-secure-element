//
//  RNSecureElement.m
//
//  Created by Marius Reimer on 12/20/2019.
//  Copyright © 2019-now Marius Reimer. All rights reserved.
//

#import "RNSecureElement.h"

#pragma mark - Static helper functions

#pragma mark - RNSecureElement

@implementation RNSecureElementModule

{
  SecureElement *secureElement;
  LAContext* context;
}

- (instancetype)init
{
  self = [super init];
  if (self) {
    secureElement = [[SecureElement alloc] init];
    context = [[LAContext alloc ] init];
  }
  return self;
}

+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

RCT_EXPORT_MODULE()

#pragma mark - Exported JS Functions

RCT_EXPORT_METHOD(configure:
                  (NSDictionary *)opts
                  callback:(RCTResponseSenderBlock)callback)
{
  if (opts[@"touchIDAuthenticationAllowableReuseDuration"] != nil) {
    context.touchIDAuthenticationAllowableReuseDuration = [opts[@"touchIDAuthenticationAllowableReuseDuration"] doubleValue];
    [secureElement changeContext:context];
  }
  
  callback(@[(id)kCFNull, (id)kCFNull]);
}

RCT_EXPORT_METHOD(decrypt:
                  (NSString *)key
                  value:(NSString *)value
                  opts:(NSDictionary *)opts
                  callback:(RCTResponseSenderBlock)callback)
{
  if (value == nil) {
    value = @"";
  }
  
  struct KeyGenParameters params = [secureElement getDefaultKeyGenParameters];
  params = [RNSecureElementUtils getKeyGenParamsForDict:opts];
  params.publicKeyName = key;
  params.privateKeyName = key;
  
  CFErrorRef decryptionError = NULL;
  NSString *decryptedResult = [secureElement decrypt:value withParameters:&params error:&decryptionError];
  
  NSDictionary *anyError = nil;
  if (decryptionError != NULL) {
    anyError = RCTJSErrorFromNSError((__bridge NSError *)decryptionError);
  }
  
  callback(@[RCTNullIfNil(anyError), RCTNullIfNil(decryptedResult)]);
}

RCT_EXPORT_METHOD(encrypt:
                  (NSString *)key
                  value:(NSString *)value
                  opts:(NSDictionary *)opts
                  callback:(RCTResponseSenderBlock)callback)
{
  if (value == nil) {
    value = @"";
  }
  
  CFErrorRef encryptionError = NULL;
  struct KeyGenParameters params = [secureElement getDefaultKeyGenParameters];
  params = [RNSecureElementUtils getKeyGenParamsForDict:opts];
  params.publicKeyName = key;
  params.privateKeyName = key;
  
  NSString *encryptedResult = [secureElement encrypt:value withParameters:&params error:&encryptionError];
  
  NSDictionary *anyError = nil;
  if (encryptionError != NULL) {
    anyError = RCTJSErrorFromNSError((__bridge NSError *)encryptionError);
  }
  
  callback(@[RCTNullIfNil(anyError), RCTNullIfNil(encryptedResult)]);
}

RCT_EXPORT_METHOD(clearElement:
                  (NSString *)key
                  callback:(RCTResponseSenderBlock)callback)
{
  NSError *error = nil;

  [secureElement clearElement:key error:error];

  NSDictionary *anyError = nil;

  if (error != NULL) {
    anyError = RCTJSErrorFromNSError(error);
  }

  callback(@[RCTNullIfNil(anyError)]);
}

RCT_EXPORT_METHOD(clearAll:
                  (RCTResponseSenderBlock)callback)
{
  NSError *error = nil;

  [secureElement clearAll:error];

  NSDictionary *anyError = nil;

  if (error != NULL) {
    anyError = RCTJSErrorFromNSError(error);
  }

  callback(@[RCTNullIfNil(anyError), (id)kCFNull]);
}

RCT_EXPORT_METHOD(isSecureDevice:
                  (RCTResponseSenderBlock)callback)
{
  NSError *error = nil;
  
  BOOL result = [secureElement deviceHasPassCode:error];
  
  NSDictionary *anyError = nil;

  if (error != NULL) {
    anyError = RCTJSErrorFromNSError(error);
  }

  callback(@[RCTNullIfNil(anyError), @(result)]);
}

RCT_EXPORT_METHOD(getDeviceFeatures:
                  (RCTResponseSenderBlock)callback)
{
  NSMutableArray *features = [[NSMutableArray alloc] init];

  NSError *error = nil;

  @try {
    if ([secureElement deviceHasBiometrics:error]) {
      [features addObject:@"IOS_BIOMETRICS"];
    }

    if ([secureElement deviceHasFaceID:error]) {
      [features addObject:@"IOS_FACEID"];
    }

    if ([secureElement deviceHasTouchID:error]) {
      [features addObject:@"IOS_TOUCHID"];
    }

    if ([secureElement deviceHasPassCode:error]) {
      [features addObject:@"IOS_PASSCODELOCK"];
    }
  } @catch (NSException *exception) {
    error = [NSError errorWithDomain:@"react.native.secure.element" code:0 userInfo:exception.userInfo];
  }
  
  NSDictionary *anyError = nil;

  if (error != NULL) {
    anyError = RCTJSErrorFromNSError(error);
  }

  callback(@[RCTNullIfNil(anyError), RCTNullIfNil(features)]);
}

RCT_EXPORT_METHOD(performAuthentication:
                  (NSString *)withFeature
                  callback:(RCTResponseSenderBlock)callback)
{
  //NOT YET IMPLEMENTED
}

@end
