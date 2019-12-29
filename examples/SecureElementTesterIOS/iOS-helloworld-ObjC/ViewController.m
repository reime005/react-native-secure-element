//
//  ViewController.m
//  iOS-helloworld-ObjC
//
//  Created by James Grantham on 9/14/18.
//  Copyright © 2018 Navisens. All rights reserved.
//

/*
 * For complete documentation on Navisens SDK API
 * Please go to the following link:
 * https://github.com/navisens/NaviDocs/blob/master/API.iOS.md
 */

#import "ViewController.h"
#import <SecureElement/SecureElement.h>

@interface ViewController ()
@property (weak, nonatomic) IBOutlet UITextView *receiveMotionDnaTextField;
@property (weak, nonatomic) IBOutlet UITextView *receiveNetworkDataTextField;

@property (strong, nonatomic) NSMutableDictionary<NSString*, MotionDna*> *networkUsers;
@property (strong, nonatomic) NSMutableDictionary<NSString *, NSNumber*> *networkUsersTimestamps;

@end

NSString *motionTypeToNSString(MotionType motionType) {
    switch (motionType) {
        case STATIONARY:
            return @"STATIONARY";
            break;
        case FIDGETING:
            return @"FIDGETING";
            break;
        case FORWARD:
            return @"FORWARD";
            break;
        default:
            return @"UNKNOWN MOTION";
            break;
    }
    return nil;
}

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    _networkUsers = [NSMutableDictionary dictionary];
    _networkUsersTimestamps = [NSMutableDictionary dictionary];
  
    LAContext *context = [[LAContext alloc] init];
    
    SecureElement *secureElement = [[SecureElement alloc] init];
  

    dispatch_async(dispatch_get_main_queue(), ^{

      CFErrorRef error = NULL;
      struct KeyGenParameters params = [secureElement getDefaultKeyGenParameters];
      params.privateKeyName = @"secure.key.private";
      params.publicKeyName = @"secure.key.private";
      params.attrKeyType = kSecAttrKeyTypeECSECPrimeRandom;
      params.publicSACAccessible = kSecAttrAccessibleAlwaysThisDeviceOnly;
      params.privateSACAccessible = kSecAttrAccessibleWhenUnlockedThisDeviceOnly;
      params.algorithm = kSecKeyAlgorithmECIESEncryptionStandardX963SHA256AESGCM;
      
      NSString *res = [secureElement encrypt:@"foo" withParameters:&params error:&error];
      [self->_receiveMotionDnaTextField setText:res];
    });
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark MotionDna Callback Methods

//    This event receives the estimation results using a MotionDna object.
//    Check out the Getters section to learn how to read data out of this object.

- (void)receiveMotionDna:(MotionDna *)motionDna {
        Location location = [motionDna getLocation];
        XYZ localLocation = location.localLocation;
        GlobalLocation globalLocation = location.globalLocation;
        Motion motion = [motionDna getMotion];
        
        NSString *motionDnaLocalString = [NSString stringWithFormat:@"Local XYZ Coordinates (meters): \n(%.2f,%.2f,%.2f)",localLocation.x,localLocation.y,localLocation.z];
        NSString *motionDnaHeadingString = [NSString stringWithFormat:@"Current Heading: %.2f",location.heading];
        NSString *motionDnaGlobalString = [NSString stringWithFormat:@"Global Position: \n(Lat: %.6f, Lon: %.6f)",globalLocation.latitude,globalLocation.longitude];
        NSString *motionDnaMotionTypeString = [NSString stringWithFormat:@"Motion Type: %@",motionTypeToNSString(motion.motionType)];
        
    NSString *motionDnaString = [NSString stringWithFormat:@"MotionDna Location:\n%@\n%@\n%@\n%@",motionDnaLocalString,
                                     motionDnaHeadingString,
                                     motionDnaGlobalString,
                                     motionDnaMotionTypeString];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [self->_receiveMotionDnaTextField setText:motionDnaString];
    });
}

//    This event receives estimation results from other devices in the server room. In order
//    to receive anything, make sure you call startUDP to connect to a room. Again, it provides
//    access to a MotionDna object, which can be unpacked the same way as above.
//
//
//    If you aren't receiving anything, then the room may be full, or there may be an error in
//    your connection. See the reportError event below for more information.

- (void)receiveNetworkData:(MotionDna *)motionDna {
    [_networkUsers setObject:motionDna forKey:[motionDna getID]];
    double timeSinceBootSeconds = [[NSProcessInfo processInfo] systemUptime];
    [_networkUsersTimestamps setObject:@(timeSinceBootSeconds) forKey:[motionDna getID]];
    __block NSString *activeNetworkUsersString = [NSString string];
    NSMutableArray<NSString*> *toRemove = [NSMutableArray array];
    
    activeNetworkUsersString = [activeNetworkUsersString stringByAppendingString:@"Network Shared Devices:\n"];
    [_networkUsers enumerateKeysAndObjectsUsingBlock:^(NSString * _Nonnull key, MotionDna * _Nonnull user, BOOL * _Nonnull stop) {
        if (timeSinceBootSeconds - [[self->_networkUsersTimestamps objectForKey:[user getID]] doubleValue] > 2.0) {
            [toRemove addObject:[user getID]];
        } else {
            activeNetworkUsersString = [activeNetworkUsersString stringByAppendingString:[[[user getDeviceName] componentsSeparatedByString:@";"] lastObject]];
            XYZ location = [user getLocation].localLocation;
            activeNetworkUsersString = [activeNetworkUsersString stringByAppendingString:[NSString stringWithFormat:@" (%.2f, %.2f, %.2f)\n",location.x, location.y, location.z]];
        }
    }];
    for (NSString *key in toRemove) {
        [_networkUsers removeObjectForKey:key];
        [_networkUsersTimestamps removeObjectForKey:key];
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        self->_receiveNetworkDataTextField.text = activeNetworkUsersString;
    });
}

- (void)receiveNetworkData:(NetworkCode)opcode WithPayload:(NSDictionary *)payload {
    
}

- (void)startMotionDna {
    _manager = [[MotionDnaManager alloc] init];
    _manager.receiver = self;
    
    //    This functions starts up the SDK. You must pass in a valid developer's key in order for
    //    the SDK to function. IF the key has expired or there are other errors, you may receive
    //    those errors through the reportError() callback route.
    
    [_manager runMotionDna:@"<developer-key>"];
    
    //    Use our internal algorithm to automatically compute your location and heading by fusing
    //    inertial estimation with global location information. This is designed for outdoor use and
    //    will not compute a position when indoors. Solving location requires the user to be walking
    //    outdoors. Depending on the quality of the global location, this may only require as little
    //    as 10 meters of walking outdoors.
    
    [_manager setLocationNavisens];
    
    //   Set accuracy for GPS positioning, states :HIGH/LOW_ACCURACY/OFF, OFF consumes
    //   the least battery.
    
    [_manager setExternalPositioningState:LOW_ACCURACY];
    
    //    Manually sets the global latitude, longitude, and heading. This enables receiving a
    //    latitude and longitude instead of cartesian coordinates. Use this if you have other
    //    sources of information (for example, user-defined address), and need readings more
    //    accurate than GPS can provide.
//    [_manager setLocationLatitude:37.787582 Longitude:-122.396627 AndHeadingInDegrees:0.0];
    
    //    Set the power consumption mode to trade off accuracy of predictions for power saving.
    
    [_manager setPowerMode:PERFORMANCE];
    
    //    Connect to your own server and specify a room. Any other device connected to the same room
    //    and also under the same developer will receive any udp packets this device sends.
    
    [_manager startUDP];
    
    //    Allow our SDK to record data and use it to enhance our estimation system.
    //    Send this file to support@navisens.com if you have any issues with the estimation
    //    that you would like to have us analyze.
    
    [_manager setBinaryFileLoggingEnabled:YES];
    
    //    Tell our SDK how often to provide estimation results. Note that there is a limit on how
    //    fast our SDK can provide results, but usually setting a slower update rate improves results.
    //    Setting the rate to 0ms will output estimation results at our maximum rate.
    
    [_manager setCallbackUpdateRateInMs:500];
    
    //    When setLocationNavisens is enabled and setBackpropagationEnabled is called, once Navisens
    //    has initialized you will not only get the current position, but also a set of latitude
    //    longitude coordinates which lead back to the start position (where the SDK/App was started).
    //    This is useful to determine which building and even where inside a building the
    //    person started, or where the person exited a vehicle (e.g. the vehicle parking spot or the
    //    location of a drop-off).
    
    [_manager setBackpropagationEnabled:YES];
    
    //    If the user wants to see everything that happened before Navisens found an initial
    //    position, he can adjust the amount of the trajectory to see before the initial
    //    position was set automatically.
    
    [_manager setBackpropagationBufferSize:2000];
    
    //    Enables AR mode. AR mode publishes orientation quaternion at a higher rate.
    
//    [_manager setARModeEnabled:YES];
}

@end

