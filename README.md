<h1>Currently in beta</h1>

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

```javascript
/* EXAMPLE will follow */
```

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

To install react-native-camera-hooks, do either

```bash
npm install --save react-native-camera-hooks
```

or

```bash
yarn add react-native-camera-hooks
```

Note that this requires a react-native version of at least 0.60.0, to use its auto linking feature.

<p>&nbsp;</p>

---

## TODO

Sorted by priority (higher = higher).

* User authentication functionality (without encryption)
* Signing certificates
* React Hooks
* Error handling/formatting
