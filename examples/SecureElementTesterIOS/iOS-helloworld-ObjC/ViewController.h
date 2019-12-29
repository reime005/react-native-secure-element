//
//  ViewController.h
//  iOS-helloworld-ObjC
//
//  Created by James Grantham on 9/14/18.
//  Copyright © 2018 Navisens. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MotionDnaManager.h"

@interface ViewController : UIViewController
@property (strong, nonatomic) MotionDnaManager *manager;
-(void)receiveMotionDna:(MotionDna*)motionDna;
-(void)receiveNetworkData:(MotionDna*)motionDna;
-(void)receiveNetworkData:(NetworkCode)opcode WithPayload:(NSDictionary*)payload;
@end

