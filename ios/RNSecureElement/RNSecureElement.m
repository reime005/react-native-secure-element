//
//  RNSecureElement.m
//
//  Created by Marius Reimer on 25.10.19.
//  Copyright © 2019-now Nect GmbH. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTLog.h>
#import "RNSecureElement.h"

// import RCTBridgeModule
#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#elif __has_include(“RCTBridgeModule.h”)
#import “RCTBridgeModule.h”
#else
#import “React/RCTBridgeModule.h” // Required when used as a Pod in a Swift project
#endif

// import RCTEventEmitter
#if __has_include(<React/RCTEventEmitter.h>)
#import <React/RCTEventEmitter.h>
#elif __has_include(“RCTEventEmitter.h”)
#import “RCTEventEmitter.h”
#else
#import “React/RCTEventEmitter.h” // Required when used as a Pod in a Swift project
#endif

@interface RCT_EXTERN_MODULE(RNSecureElement, NSObject)

RCT_EXTERN_METHOD(encryptWithKeyPair:(NSString *)keyIdentifier message:(NSString *)message resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(decryptWithKeyPair:(NSString *)keyIdentifier encryptedMessage:(NSString *)encryptedMessage authenticationTitle:(NSString *)authenticationTitle authenticationDescription:(NSString *)authenticationDescription resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(signWithKeyPair:(NSString *)keyIdentifier message:(NSString *)message authenticationTitle:(NSString *)authenticationTitle authenticationDescription:(NSString *)authenticationDescription resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(deleteKeyPairs:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(hasSecureElement:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(getIsDeviceSecure:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(getIsDeviceLocked:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(isTouchIdDevice:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(isTouchIdDevice:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(isFaceIdDevice:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(authenticate:(NSString *)reason fallbackLabel:(NSString *)fallbackLabel resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)

@end
