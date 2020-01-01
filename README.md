<p>&nbsp;</p>
<p align="center">
  <img src="logo.png" width="350" title="hover text">
  <p align='center'>Secure Element for React Native</p>
</p>
<p>&nbsp;</p>

<p>

React Native Secure Element provides functionality to use on-device and hardware-based secure encryption and decryption.

Both native modules are also usable without React Native via gradle and cocoa pods. Thus, they also benefit from being tested by E2E tests.

The Android side uses the `android.security.keystore` API and requires a minimum SDK version of 23, due to availability of the hardware-backed security.

The iOS side uses the CommonCrypto and LocalAuthentication APIs. It saves the key pairs in the keychain or secure enclave if available.

</p>

<p>&nbsp;</p>

[![npm](https://img.shields.io/npm/v/react-native-secure-element.svg?style=flat-square)](http://npm.im/react-native-secure-element)
![GitHub](https://img.shields.io/github/license/reime005/react-native-secure-element.svg?style=flat-square)
![Android](https://github.com/reime005/react-native-secure-element/workflows/Android/badge.svg)
![iOS](https://github.com/reime005/react-native-secure-element/workflows/iOS/badge.svg)

<div style="font-size:10px; color: grey">Icons made by <a href="https://www.flaticon.com/authors/freepik" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a></div>

See examples in [src/examples](./src/examples).

```javascript
// React Hooks example
import { useSecureElement } from 'react-native-secure-element';

const Example = () => {
  const { encrypt } = useSecureElement();
  const [encryptedBase64Text, setEncryptedBase64Text] = useState('');

  useEffect(
    async () => {
      try {
        const val = await encrypt('someKey', 'toEncrypt');
        setEncryptedBase64Text(val);
      } catch (e) {
        console.warn(e);
        setEncryptedBase64Text(e.message);
      }
    },
    []
  )

  <View>
    <Text>{encryptedBase64Text}</Text>
  </View>
}
```

## Automated E2E (UI) Tests Preview

With Github Actions, each commit automatically triggers a full build cycle. This includes running End-to-End (E2E) or UI tests on an iOS Simulator and Android Emulator. This has the benefit of having only tested and not _breaking code_ merged into the *master* branch.

![E2E Tests](https://s5.gifyu.com/images/secure-element-min.gif)

## Features

  - Full TypeScript support
  - Automatically deployed and tested (CI/CD) via Github Actions
  - Extremely secure iOS encryption and decryption via secure enclave, keychain and elliptic curves. No third party dependencies
  - Very secure Android encryption and decryption via Android KeyStore
  - Natively (without React Native) available implementation

## Why / Purpose

The purpose of this repository is to provide a secure way to decrypt and encrypt values. Such values could consist of sensitive user data or authentication secrets (TOTP, ...).

## API

See types definition in [src/typescript](./src/typescript/index.d.ts).

<p>&nbsp;</p>

---

## Installation

To install react-native-secure-element, do either

```bash
npm install --save react-native-secure-element
```

or

```bash
yarn add react-native-secure-element
```

Note that this requires a react-native version of at least 0.60.0, to use its auto linking feature.

### Native Android Dependency

Via gradle/maven dependency:

```groovy
implementation 'com.android.secureelement:android:+'
```

### Native iOS Dependency

Via cocoapods dependency:

```ruby
pod 'SecureElement'
```

<p>&nbsp;</p>

---

## TODO

Sorted by priority (higher = higher).

* User authentication functionality (without encryption)
* Signing certificates
* React Hooks
* Error handling/formatting
