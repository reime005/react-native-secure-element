//
//  RNSecureElement.h
//
//  Created by Marius Reimer on 25.10.19.
//  Copyright © 2019-now Marius Reimer. All rights reserved.
//

//#import <CommonCrypto/CommonCryptor.h>
//#import <CommonCrypto/CommonDigest.h>

#import <LocalAuthentication/LocalAuthentication.h>

#import <Foundation/Foundation.h>
#import "SecureElement.h"

/**
 * Helper class for converting values from JavaScript to iOS/ObjC land.
 */
@interface RNSecureElementUtils : NSObject

+ (SecKeyAlgorithm) getSignatureMessage:(NSString *)key API_AVAILABLE(ios(10.0));
+ (SecKeyAlgorithm) getAlgorithmForKey:(NSString *)key API_AVAILABLE(ios(10.0));
+ (CFStringRef) getSACAccessibleConstant:(NSString *)constant;
+ (SecAccessControlCreateFlags) calculateSACFlags:(NSArray *)array;
+ (struct KeyGenParameters) getKeyGenParamsForDict:(NSDictionary *)opts;
@end

