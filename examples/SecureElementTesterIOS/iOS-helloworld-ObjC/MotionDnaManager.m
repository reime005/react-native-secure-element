//
//  MotionDnaManager.m
//  iOS-helloworld-ObjC
//
//  Created by James Grantham on 9/14/18.
//  Copyright © 2018 Navisens. All rights reserved.
//

#import "MotionDnaManager.h"
#import "ViewController.h"

@implementation MotionDnaManager
- (void)receiveMotionDna:(MotionDna *)motionDna {
    if (_receiver != nil) {
        [_receiver receiveMotionDna:motionDna];
    }
}

//    Report any errors of the estimation or internal SDK

- (void)reportError:(ErrorCode)error WithMessage:(NSString *)message {
    switch (error) {
        case SENSOR_TIMING:
            NSLog(@"Error: Sensor Timing %@", message);
            break;
        case AUTHENTICATION_FAILED:
            NSLog(@"Error: Authentication Failed %@", message);
            break;
        case SENSOR_MISSING:
            NSLog(@"Error: Sensor Missing %@", message);
            break;
        case SDK_EXPIRED:
            NSLog(@"Error: SDK Expired %@", message);
            break;
        case WRONG_FLOOR_INPUT:
            NSLog(@"Error: Wrong Floor Input %@", message);
            break;
        default:
            NSLog(@"Error: Unknown Cause");
    };
}

- (void)receiveNetworkData:(MotionDna *)motionDna {
    if (_receiver != nil) {
        [_receiver receiveNetworkData:motionDna];
    }
}

- (void)receiveNetworkData:(NetworkCode)opcode WithPayload:(NSDictionary *)payload {
    if (_receiver != nil) {
        [_receiver receiveNetworkData:opcode WithPayload:payload];
    }
}
@end
