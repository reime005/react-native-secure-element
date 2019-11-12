//
//  RNSecureElement.m
//
//  Created by Marius Reimer on 25.10.19.
//  Copyright © 2019-now Nect GmbH. All rights reserved.
//

import EllipticCurveKeyPair
import Foundation
import LocalAuthentication

#if DEBUG
  func DebugLog(message: String) { NSLog("[SecureElement]: \(message)") }
#else
  func DebugLog(message _: String) {}
#endif

@objc(RNSecureElement)
class RNSecureElement: NSObject {
  let serialQueue = DispatchQueue(label: "com.nect.SecureElement")

  // Export constants to use in your native module
  @objc
  func constantsToExport() -> [String: Any]! {
    return [:
      // "EXAMPLE_CONSTANT": "example"
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
    return [] // return EllipticCurveKeyPair.Device.hasSecureEnclave ? [.applicationPassword, .privateKeyUsage] : [.applicationPassword]
//      return EllipticCurveKeyPair.Device.hasSecureEnclave ? [] : []
//    }
  }

  var context: LAContext! = LAContext()

  private func createManagers(prompt: String) {
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
        token: .secureEnclaveIfAvailable
      )
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
        token: .secureEnclaveIfAvailable
      )
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
        token: .secureEnclaveIfAvailable
      )
      return EllipticCurveKeyPair.Manager(config: config)
    }()
  }

  // Implement methods that you want to export to the native module
  @objc(encryptWithKeyPair:message:resolver:rejecter:)
  func encryptWithKeyPair(keyIdentifier: String, message: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
//    context.setCredential("passwordFromServer".data(using:String.Encoding.utf8)!, type: LACredentialType.applicationPassword)

    DispatchQueue.global(qos: .background).async {
      print("------ try_to_encrypt start ------")
      self.createManagers(prompt: RNSecureElement.operationPrompt)

      do {
        let encryptedMessage: Data
        print("------ try_to_encrypt start ------")
        let toEncrypt = message.data(using: .utf8)!
        if keyIdentifier == "transport" {
          encryptedMessage = try self.transportKeyPairManager!.encrypt(toEncrypt)
        } else if keyIdentifier == "app" {
          encryptedMessage = try self.appKeyPairManager!.encrypt(toEncrypt)
        } else if keyIdentifier == "auth" {
          encryptedMessage = try self.authKeyPairManager!.encrypt(toEncrypt)
        } else {
          reject("no such keypair", "no such keypair", nil)
          return
        }

        print("------ try_to_encrypt end ------")
        resolve(encryptedMessage.base64EncodedString() as NSString)

      } catch let EllipticCurveKeyPair.Error.underlying(_, underlying) where underlying.code == errSecUnimplemented {
        reject("unsupported device", "unsupported device", underlying)
      } catch let EllipticCurveKeyPair.Error.authentication(authenticationError) where authenticationError.code == .userCancel {
        reject("authentication dismissed", "authentication dismissed", authenticationError)
      } catch {
        reject("encryption failed", "encryption failed", error)
      }
    }
  }

  @objc(decryptWithKeyPair:encryptedMessage:authenticationTitle:authenticationDescription:resolver:rejecter:)
  func decryptWithKeyPair(keyIdentifier: String, encryptedMessage: String, authenticationTitle _: String, authenticationDescription: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
//    context = LAContext()
//    context.setCredential("passwordFromServer".data(using:String.Encoding.utf8)!, type: LACredentialType.applicationPassword)

//    self.context.touchIDAuthenticationAllowableReuseDuration = 10;

    DispatchQueue.global(qos: .background).async {
      print("------ try_to_decrypt start ------")

      self.createManagers(prompt: authenticationDescription)

      do {
        let decryptedMessage: Data
        let encrypted = Data(base64Encoded: encryptedMessage)!
        if keyIdentifier == "transport" {
          decryptedMessage = try self.transportKeyPairManager!.decrypt(encrypted, hash: .sha256, context: self.context)
        } else if keyIdentifier == "app" {
          decryptedMessage = try self.appKeyPairManager!.decrypt(encrypted, hash: .sha256, context: self.context)
        } else if keyIdentifier == "auth" {
          decryptedMessage = try self.authKeyPairManager!.decrypt(encrypted, hash: .sha256, context: self.context)
        } else {
          reject("no such keypair", "no such keypair", nil)
          return
        }

        resolve(String(data: decryptedMessage, encoding: .utf8)! as NSString)

      } catch let EllipticCurveKeyPair.Error.underlying(_, underlying) where underlying.code == errSecUnimplemented {
        reject("unsupported device", "unsupported device", underlying)
      } catch let EllipticCurveKeyPair.Error.authentication(authenticationError) where authenticationError.code == .userCancel {
        reject("authentication dismissed", "authentication dismissed", authenticationError)
      } catch {
        NSLog("error: " + error.localizedDescription)
        reject("decryption failed", "decryption failed", error)
      }
    }
    print("------ try_to_decrypt end ------")
  }

  @objc(signWithKeyPair:message:authenticationTitle:authenticationDescription:resolver:rejecter:)
  func signWithKeyPair(keyIdentifier: String, message: String, authenticationTitle _: String, authenticationDescription: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    createManagers(prompt: authenticationDescription)

    DispatchQueue.global(qos: .background).async {
      print("------ try_to_sign start ------")

      do {
        let signedMessage: Data
        let messageToSign = message.data(using: .utf8)!
        if keyIdentifier == "transport" {
          signedMessage = try self.transportKeyPairManager!.sign(messageToSign, hash: .sha256, context: self.context)
        } else if keyIdentifier == "app" {
          signedMessage = try self.appKeyPairManager!.sign(messageToSign, hash: .sha256, context: self.context)
        } else {
          reject("no such keypair", "no such keypair", nil)
          return
        }

        resolve(signedMessage.base64EncodedString() as NSString)

      } catch let EllipticCurveKeyPair.Error.underlying(_, underlying) where underlying.code == errSecUnimplemented {
        reject("unsupported device", "unsupported device", underlying)
      } catch let EllipticCurveKeyPair.Error.authentication(authenticationError) where authenticationError.code == .userCancel {
        reject("authentication dismissed", "authentication dismissed", authenticationError)
      } catch {
        reject("signing failed", "signing failed", error)
      }
    }
    print("------ try_to_sign end ------")
  }

  @objc(deleteKeyPairs:rejecter:)
  func deleteKeyPairs(resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    print("---------- DELETE -----------")

    createManagers(prompt: RNSecureElement.operationPrompt)

    do {
      try transportKeyPairManager!.deleteKeyPair()
      try appKeyPairManager!.deleteKeyPair()
      resolve("success")
    } catch {
      reject("deletion failed", "deletion failed", error)
    }
  }

  @objc(hasSecureElement:rejecter:)
  func hasSecureElement(resolve: @escaping RCTPromiseResolveBlock, reject _: @escaping RCTPromiseRejectBlock) {
    resolve(EllipticCurveKeyPair.Device.hasSecureEnclave)
  }

  @objc(getIsDeviceLocked:rejecter:)
  func getIsDeviceLocked(resolve: @escaping RCTPromiseResolveBlock, reject _: @escaping RCTPromiseRejectBlock) {
    resolve(false)
  }

  @objc(getIsDeviceSecure:rejecter:)
  func getIsDeviceSecure(resolve: @escaping RCTPromiseResolveBlock, reject _: @escaping RCTPromiseRejectBlock) {
    resolve(RNSecureElement.devicePasscodeSet())
  }

  @objc(authenticate:fallbackLabel:resolver:rejecter:)
  func authenticate(reason: String, fallbackLabel: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
    var warningMessage: String = String()

    if RNSecureElement.hasFaceId() {
      let usageDescription = Bundle.main.object(forInfoDictionaryKey: "NSFaceIDUsageDescription")

      if usageDescription == nil {
        warningMessage = "FaceID is available but has not been configured. To enable FaceID, provide `NSFaceIDUsageDescription`."
      }
    }

    let authContext: LAContext! = LAContext()

    if fallbackLabel != "" {
      authContext.localizedFallbackTitle = fallbackLabel
    }

    if #available(iOS 11.0, *) {
      authContext.interactionNotAllowed = false
    }

    let response = NSMutableDictionary()
    response.setValue(warningMessage, forKey: "warning")

    authContext.evaluatePolicy(LAPolicy.deviceOwnerAuthentication, localizedReason: reason) { success, error in
      response.setValue(success, forKey: "success")
      response.setValue(error?.localizedDescription, forKey: "error")
      if let convError = error {
        if #available(iOS 11.0, *) {
          if convError._code == LAError.biometryLockout.rawValue {
            reject("biometryLockout", convError.localizedDescription, convError)
            return
          } else if convError._code == LAError.biometryNotAvailable.rawValue {
            reject("biometryNotAvailable", convError.localizedDescription, convError)
            return
          }
        }

        switch convError._code {
        case LAError.userCancel.rawValue:
          reject("userCancel", convError.localizedDescription, convError)
        case LAError.appCancel.rawValue:
          reject("appCancel", convError.localizedDescription, convError)
        case LAError.systemCancel.rawValue:
          reject("systemCancel", convError.localizedDescription, convError)
        case LAError.touchIDLockout.rawValue:
          reject("touchIDLockout", convError.localizedDescription, convError)
        case LAError.touchIDNotAvailable.rawValue:
          reject("touchIDNotAvailable", convError.localizedDescription, convError)
        case LAError.touchIDNotEnrolled.rawValue:
          reject("touchIDNotEnrolled", convError.localizedDescription, convError)
        case LAError.userFallback.rawValue:
          reject("userFallback", convError.localizedDescription, convError)
        case LAError.authenticationFailed.rawValue:
          reject("authenticationFailed", convError.localizedDescription, convError)
        case LAError.invalidContext.rawValue:
          reject("invalidContext", convError.localizedDescription, convError)
        case LAError.notInteractive.rawValue:
          reject("notInteractive", convError.localizedDescription, convError)
        case LAError.passcodeNotSet.rawValue:
          reject("passcodeNotSet", convError.localizedDescription, convError)

        default:
          reject("unknown", convError.localizedDescription, convError)
        }
      } else if error != nil {
        // fallback if error cast did not work
        reject("unknown", error?.localizedDescription, error)
      } else {
        resolve(response)
      }
    }
  }

  @objc(isTouchIdDevice:rejecter:)
  func isTouchIdDevice(resolve: @escaping RCTPromiseResolveBlock, reject _: @escaping RCTPromiseRejectBlock) {
    resolve(RNSecureElement.hasTouchId())
  }

  @objc(isFaceIdDevice:rejecter:)
  func isFaceIdDevice(resolve: @escaping RCTPromiseResolveBlock, reject _: @escaping RCTPromiseRejectBlock) {
    resolve(RNSecureElement.hasFaceId())
  }

  private static func hasFaceId() -> Bool {
    let context: LAContext! = LAContext()
    var error: NSError?

    if #available(iOS 11.0, *) {
      do {
        try context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)
      } catch {
        return false
      }

      return context.biometryType == LABiometryType.faceID
    }

    return false
  }

  private static func hasTouchId() -> Bool {
    let context: LAContext! = LAContext()
    var error: NSError?

    if #available(iOS 11.0, *) {
      do {
        try context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)
        DebugLog(message: "has touch ID")
      } catch {
        return false
      }

      return context.biometryType == LABiometryType.touchID
    }

    return false
  }

  private static func convertErrorCode(error: LAError) -> String {
    if error == nil {
      return ""
    }

    switch error.code {
    //      case LAErrorSystemCancel:
    //        return "system_cancel";
    //      case LAErrorAppCancel:
    //        return "app_cancel";
    //      case LAErrorTouchIDLockout:
    //        return @"lockout";
    //      case LAErrorUserFallback:
    //        return @"user_fallback";
    //      case LAErrorUserCancel:
    //        return @"user_cancel";
    //      case LAErrorTouchIDNotAvailable:
    //        return @"not_available";
    //      case LAErrorInvalidContext:
    //        return @"invalid_context";
    //      case LAErrorTouchIDNotEnrolled:
    //        return @"not_enrolled";
    //      case LAErrorPasscodeNotSet:
    //        return @"passcode_not_set";
    //      case LAErrorAuthenticationFailed:
    //        return @"authentication_failed";
    default:
      return ""
    }
  }

  private static func devicePasscodeSet() -> Bool {
    return LAContext().canEvaluatePolicy(.deviceOwnerAuthentication, error: nil)
  }
}
