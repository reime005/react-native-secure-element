//
//  SecureElement.swift
//  RNSecureElement
//
//  Created by Marius Reimer on 28.10.19.
//  Copyright © 2019 Facebook. All rights reserved.
//

import Foundation
import EllipticCurveKeyPair
import LocalAuthentication

#if DEBUG
  func DebugLog(message: String) { NSLog("[SecureElement]: \(message)") }
#else
  func DebugLog(message: String) { }
#endif

@objc(RNSecureElement)
class RNSecureElement : NSObject {
  let serialQueue = DispatchQueue(label: "com.nect.SecureElement")
  
  // Export constants to use in your native module
  @objc
  func constantsToExport() -> [String : Any]! {
    return [:
      //"EXAMPLE_CONSTANT": "example"
    ]
  }

  static let operationPrompt = "Entschlüsselung deiner persönlichen Daten."
  private var transportKeyPairManager: EllipticCurveKeyPair.Manager?
  private var appKeyPairManager: EllipticCurveKeyPair.Manager?
  private var authKeyPairManager: EllipticCurveKeyPair.Manager?

  private static func securityFlags() -> SecAccessControlCreateFlags {
   //    if(devicePasscodeSet()){
//      return EllipticCurveKeyPair.Device.hasSecureEnclave ? [.userPresence, .privateKeyUsage] : [.userPresence]
//    } else {
    return[];//return EllipticCurveKeyPair.Device.hasSecureEnclave ? [.applicationPassword, .privateKeyUsage] : [.applicationPassword]
//      return EllipticCurveKeyPair.Device.hasSecureEnclave ? [] : []
//    }
  }

  var context: LAContext! = LAContext()

  private func createManagers(prompt: String){
    appKeyPairManager = {
      EllipticCurveKeyPair.logger = { print($0) }
      let publicAccessControl = EllipticCurveKeyPair.AccessControl(protection: kSecAttrAccessibleAlwaysThisDeviceOnly, flags: [])
      let privateAccessControl = EllipticCurveKeyPair.AccessControl(protection: kSecAttrAccessibleWhenUnlockedThisDeviceOnly, flags: RNSecureElement.securityFlags())
      let config = EllipticCurveKeyPair.Config(
        publicLabel: "nect.app.encryption.public",
        privateLabel: "nect.app.encryption.private",
        operationPrompt: prompt,
        publicKeyAccessControl: publicAccessControl,
        privateKeyAccessControl: privateAccessControl,
        token: .secureEnclaveIfAvailable)
      return EllipticCurveKeyPair.Manager(config: config)
    }()

    authKeyPairManager = {
      EllipticCurveKeyPair.logger = { print($0) }
      let publicAccessControl = EllipticCurveKeyPair.AccessControl(protection: kSecAttrAccessibleAlwaysThisDeviceOnly, flags: [])
      let privateAccessControl = EllipticCurveKeyPair.AccessControl(protection: kSecAttrAccessibleWhenUnlockedThisDeviceOnly, flags: [.applicationPassword])
      let config = EllipticCurveKeyPair.Config(
        publicLabel: "nect.auth.encryption.public",
        privateLabel: "nect.auth.encryption.private",
        operationPrompt: prompt,
        publicKeyAccessControl: publicAccessControl,
        privateKeyAccessControl: privateAccessControl,
        token: .secureEnclaveIfAvailable)
      return EllipticCurveKeyPair.Manager(config: config)
    }()

    transportKeyPairManager = {
      EllipticCurveKeyPair.logger = { print($0) }
      let publicAccessControl = EllipticCurveKeyPair.AccessControl(protection: kSecAttrAccessibleAlwaysThisDeviceOnly, flags: [])
      let privateAccessControl = EllipticCurveKeyPair.AccessControl(protection: kSecAttrAccessibleWhenUnlockedThisDeviceOnly, flags: RNSecureElement.securityFlags())
      let config = EllipticCurveKeyPair.Config(
        publicLabel: "nect.transport.encryption.public",
        privateLabel: "nect.transport.encryption.private",
        operationPrompt: prompt,
        publicKeyAccessControl: publicAccessControl,
        privateKeyAccessControl: privateAccessControl,
        token: .secureEnclaveIfAvailable)
      return EllipticCurveKeyPair.Manager(config: config)
    }()
  }

  // Implement methods that you want to export to the native module
  @objc(encryptWithKeyPair:message:resolver:rejecter:)
  func encryptWithKeyPair(keyIdentifier: String, message: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
//    context.setCredential("passwordFromServer".data(using:String.Encoding.utf8)!, type: LACredentialType.applicationPassword)

    DispatchQueue.global(qos: .background).async {

    print("------ try_to_encrypt start ------");
      self.createManagers(prompt: RNSecureElement.operationPrompt)

    do {
      let encryptedMessage: Data;
      print("------ try_to_encrypt start ------");
      let toEncrypt = message.data(using: .utf8)!
      if(keyIdentifier == "transport"){
        encryptedMessage = try self.transportKeyPairManager!.encrypt(toEncrypt)
      } else if (keyIdentifier == "app") {
        encryptedMessage = try self.appKeyPairManager!.encrypt(toEncrypt)
      } else if (keyIdentifier == "auth") {
        encryptedMessage = try self.authKeyPairManager!.encrypt(toEncrypt)
      } else {
        reject("no such keypair", "no such keypair", nil)
        return;
      }

      print("------ try_to_encrypt end ------");
      resolve(encryptedMessage.base64EncodedString() as NSString)

      } catch EllipticCurveKeyPair.Error.underlying(_, let underlying) where underlying.code == errSecUnimplemented {
        reject("unsupported device", "unsupported device", underlying)
      } catch EllipticCurveKeyPair.Error.authentication(let authenticationError) where authenticationError.code == .userCancel {
        reject("authentication dismissed", "authentication dismissed", authenticationError)
      } catch {
        reject("encryption failed", "encryption failed", error)
      }
    }
  }

  @objc(decryptWithKeyPair:encryptedMessage:authenticationTitle:authenticationDescription:resolver:rejecter:)
  func decryptWithKeyPair(keyIdentifier: String, encryptedMessage: String, authenticationTitle: String, authenticationDescription: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {

//    context = LAContext()
//    context.setCredential("passwordFromServer".data(using:String.Encoding.utf8)!, type: LACredentialType.applicationPassword)

//    self.context.touchIDAuthenticationAllowableReuseDuration = 10;

    DispatchQueue.global(qos: .background).async {

      print("------ try_to_decrypt start ------");

      self.createManagers(prompt: authenticationDescription);

      do {
        let decryptedMessage: Data;
        let encrypted = Data(base64Encoded: encryptedMessage)!
        if(keyIdentifier == "transport"){
          decryptedMessage = try self.transportKeyPairManager!.decrypt(encrypted, hash: .sha256, context: self.context)
        } else if (keyIdentifier == "app") {
          decryptedMessage = try self.appKeyPairManager!.decrypt(encrypted, hash: .sha256, context: self.context)
        } else if (keyIdentifier == "auth") {
          decryptedMessage = try self.authKeyPairManager!.decrypt(encrypted, hash: .sha256, context: self.context)
        } else {
          reject("no such keypair", "no such keypair", nil)
          return;
        }

        resolve(String(data: decryptedMessage, encoding: .utf8)! as NSString)

      } catch EllipticCurveKeyPair.Error.underlying(_, let underlying) where underlying.code == errSecUnimplemented {
        reject("unsupported device", "unsupported device", underlying)
      } catch EllipticCurveKeyPair.Error.authentication(let authenticationError) where authenticationError.code == .userCancel {
        reject("authentication dismissed", "authentication dismissed", authenticationError)
      } catch {
        NSLog("error: " + error.localizedDescription);
        reject("decryption failed", "decryption failed", error)
      }
    }
    print("------ try_to_decrypt end ------");
  }

  @objc(signWithKeyPair:message:authenticationTitle:authenticationDescription:resolver:rejecter:)
  func signWithKeyPair(keyIdentifier: String, message: String, authenticationTitle: String, authenticationDescription: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    self.createManagers(prompt: authenticationDescription);

    DispatchQueue.global(qos: .background).async {

      print("------ try_to_sign start ------");

      do {
        let signedMessage: Data;
        let messageToSign = message.data(using: .utf8)!
        if(keyIdentifier == "transport"){
          signedMessage = try self.transportKeyPairManager!.sign(messageToSign, hash: .sha256, context: self.context)
        } else if (keyIdentifier == "app") {
          signedMessage = try self.appKeyPairManager!.sign(messageToSign, hash: .sha256, context: self.context)
        } else {
          reject("no such keypair", "no such keypair", nil)
          return;
        }

        resolve(signedMessage.base64EncodedString() as NSString)

      } catch EllipticCurveKeyPair.Error.underlying(_, let underlying) where underlying.code == errSecUnimplemented {
        reject("unsupported device", "unsupported device", underlying)
      } catch EllipticCurveKeyPair.Error.authentication(let authenticationError) where authenticationError.code == .userCancel {
        reject("authentication dismissed", "authentication dismissed", authenticationError)
      } catch {
        reject("signing failed", "signing failed", error)
      }
    }
    print("------ try_to_sign end ------");
  }

  @objc(deleteKeyPairs:rejecter:)
  func deleteKeyPairs(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    print("---------- DELETE -----------")

    createManagers(prompt: RNSecureElement.operationPrompt)

    do {
      try self.transportKeyPairManager!.deleteKeyPair()
      try self.appKeyPairManager!.deleteKeyPair()
      resolve("success")
    } catch {
      reject("deletion failed", "deletion failed", error)
    }
  }

  @objc(hasSecureElement:rejecter:)
  func hasSecureElement(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    resolve(EllipticCurveKeyPair.Device.hasSecureEnclave)
  }

  @objc(getIsDeviceLocked:rejecter:)
  func getIsDeviceLocked(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    resolve(false)
  }

  @objc(getIsDeviceSecure:rejecter:)
  func getIsDeviceSecure(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    resolve(RNSecureElement.devicePasscodeSet())
  }

  @objc(isFaceIdDevice:rejecter:)
  func isFaceIdDevice(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    // face ID is only available from iOS 11
    if #available(iOS 11.0, *) {
      DebugLog(message: "Checking if Device has Face ID...")
      var error : NSError?
      
      do {
        try self.context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)

          // Call evaluatePolicy here
      } catch {
          print("Cannot evaluate policy, error: \(error)")
      }
      
      resolve(false)

//      serialQueue.sync {
//        self.context.canEvaluatePolicy(policy: kLAPolicyDeviceOwnerAuthenticationWithBiometrics, error: nil);
//        if (self.context.biometryType == LABiometryTypeFaceID) {
//          DebugLog(message: "Device has Face ID.")
//          resolve(true)
//        } else {
//          DebugLog(message: "Device has no Face ID.")
//          resolve(false)
//        }
//      }
    } else {
      DebugLog(message: "Device cannot have Face ID.")
      resolve(false)
    }
  }

  @objc(isTouchIdDevice:rejecter:)
  func isTouchIdDevice(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    resolve(false)
//    DebugLog(message: "Checking if Device has Touch ID...")
//    serialQueue.sync {
//      self.context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, error: nil);
//
//      if (self.context.biometryType == LABiometryTypeTouchID) {
//        DebugLog(message: "Device has Touch ID.")
//        resolve(true)
//      } else {
//        DebugLog(message: "Device has no Touch ID.")
//        resolve(false)
//      }
//    }
  }

  private static func devicePasscodeSet() -> Bool {
    //checks to see if devices (not apps) passcode has been set
    return LAContext().canEvaluatePolicy(.deviceOwnerAuthentication, error: nil)
  }
}
