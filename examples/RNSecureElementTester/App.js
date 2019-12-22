/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, {useRef, useEffect, useState} from 'react';
import {
  SafeAreaView,
  StyleSheet,
  ScrollView,
  View,
  Text,
  StatusBar,
  Button,
  TextInput,
} from 'react-native';

import {
  Header,
  LearnMoreLinks,
  Colors,
  DebugInstructions,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';

import {
  SecureElement,
  IOSKeyGenOptions,
  AndroidKeyGenOptions,
} from 'react-native-secure-element/src';

const commonAndroidOptions: AndroidKeyGenOptions & IOSKeyGenOptions = {
  keyGenBlockMode: 'ECB',
  keyGenEncryptionPadding: 'PKCS1Padding',
  keyPairGeneratorAlgorithm: 'RSA',
  keyGenInvalidatedByBiometricEnrollment: false,
  keyGenUserAuthenticationRequired: false,
  keyPairGeneratorProvider: 'AndroidKeyStore',
  purposes: ['DECRYPT', 'ENCRYPT'],
  userAuthenticationValidityDurationSeconds: 10,
  userPromptTitle: 'Some title',
  userPromptDescription: 'Some description',
  privateSACFlags: [],
  publicSACFlags: [],
  privateSACAccessible: 'kSecAttrAccessibleWhenUnlockedThisDeviceOnly',
  publicSACAccessible: 'kSecAttrAccessibleAlwaysThisDeviceOnly',
};

const commonIOSOptions = {};

const App: () => React$Node = () => {
  const secureElement = useRef(new SecureElement());
  const [configured, setConfigured] = useState(false);
  const [plainText, setPlainText] = useState('');
  const [encryptedText, setEncryptedText] = useState('');
  const [decryptedText, setDecryptedText] = useState('');
  const [deviceFeatures, setDeviceFeatures] = useState('');

  const [encryptedUserAuthText, setEncryptedUserAuthText] = useState('');
  const [userAuthText, setUserAuthText] = useState('');

  useEffect(() => {
    if (!secureElement || !secureElement.current) {
      return;
    }

    secureElement.current
      .getDeviceFeatures()
      .then(features => setDeviceFeatures(features))
      .catch(e => setDeviceFeatures('fail'));

    secureElement.current
      .configure({
        keystoreType: 'AndroidKeyStore',
      })
      .then(() => setConfigured(true))
      .catch(() => setConfigured(false));
  }, [secureElement]);

  useEffect(() => {
    if (!secureElement || !secureElement.current) {
      return;
    }

    secureElement.current
      .encrypt('test.key.id.290', plainText, {
        ...commonAndroidOptions,
      })
      .then(text => setEncryptedText(text))
      .catch(() => setEncryptedText('fail'));
  }, [plainText]);

  useEffect(() => {
    if (!secureElement || !secureElement.current) {
      return;
    }

    secureElement.current
      .decrypt('test.key.id.290', encryptedText, {
        ...commonAndroidOptions,
      })
      .then(text => setDecryptedText(text))
      .catch(() => setDecryptedText('fail'));
  }, [encryptedText]);

  if (!configured) {
    return (
      <>
        <StatusBar barStyle="dark-content" />
        <SafeAreaView>
          <Text>react-native-secure-element has not been configured yet.</Text>
        </SafeAreaView>
      </>
    );
  }

  return (
    <>
      <StatusBar barStyle="dark-content" />
      <SafeAreaView style={styles.safeArea}>
        <TextInput
          style={{borderBottomColor: 'grey', borderBottomWidth: 2, margin: 10}}
          onChangeText={text => setPlainText(text)}
          placeholder="Enter some text"
        />

        <ScrollView
          contentInsetAdjustmentBehavior="automatic"
          style={styles.scrollView}>
          <Text style={styles.title}>Plain text:</Text>
          <Text style={styles.resultText}>{plainText}</Text>

          <Text style={styles.title}>Encrypted text:</Text>
          <Text style={styles.resultText}>{encryptedText}</Text>

          <Text style={styles.title}>Decrypted text:</Text>
          <Text style={styles.resultText}>{decryptedText}</Text>
          <Button
            title='encrypt "userAuthText" with user auth'
            onPress={() => {
              secureElement.current
                .encrypt('test.key.id.userAuth2', 'userAuthText', {
                  ...commonAndroidOptions,
                  userPrompt: 'iOS Prompt',
                  algorithm: 'SHA256',
                  privateKeySizeInBits: 256,
                  secAttrType: 'ECSECPrimeRandom',
                  saveInSecureEnclaveIfPossible: true,
                  keyGenUserAuthenticationRequired: true,
                  privateSACFlags: [
                    'kSecAccessControlPrivateKeyUsage',
                    'kSecAccessControlUserPresence',
                  ],
                })
                .then(userAuthEncrypted =>
                  setEncryptedUserAuthText(userAuthEncrypted),
                );
            }}
          />

          {!!encryptedUserAuthText && (
            <Text style={styles.title}>The encryped user auth text is:</Text>
          )}
          {!!encryptedUserAuthText && (
            <Text style={styles.resultText}>{encryptedUserAuthText}</Text>
          )}

          <Button
            title='decrypt "userAuthText" with user auth'
            onPress={() => {
              secureElement.current
                .decrypt('test.key.id.userAuth2', encryptedUserAuthText, {
                  ...commonAndroidOptions,
                  userPrompt: 'iOS Prompt',
                  algorithm: 'SHA256',
                  privateKeySizeInBits: 256,
                  secAttrType: 'ECSECPrimeRandom',
                  saveInSecureEnclaveIfPossible: true,
                  keyGenUserAuthenticationRequired: true,
                  privateSACFlags: [
                    'kSecAccessControlPrivateKeyUsage',
                    'kSecAccessControlUserPresence',
                  ],
                })
                .then(decryptedText => {
                  setUserAuthText(decryptedText);
                })
                .catch(e => {
                  setUserAuthText(e.message);
                });
            }}
          />

          <Text style={styles.title}>The user auth decrypted result is:</Text>
          <Text style={styles.resultText}>{userAuthText}</Text>

          <View style={{flex: 1}}>
            <Button
              title="clear only user auth key"
              onPress={() => {
                secureElement.current
                  .clearElement('test.key.id.userAuth2')
                  .then(() => console.warn('cleared'))
                  .catch(e => console.warn(e.message));
              }}
            />

            <Button
              title="clear all keys"
              onPress={() => {
                secureElement.current
                  .clearAll()
                  .then(() => console.warn('all clean'))
                  .catch(e => console.warn(e.message));
              }}
            />
          </View>

          <Text style={styles.title}>Device features:</Text>
          {Array.isArray(deviceFeatures) && (
            <Text>{deviceFeatures.join(', ')}</Text>
          )}
        </ScrollView>
      </SafeAreaView>
    </>
  );
};

const styles = StyleSheet.create({
  resultText: {
    backgroundColor: '#ccc',
    margin: 5,
    padding: 5,
  },
  title: {
    fontWeight: 'bold',
  },
  scrollView: {
    height: '100%',
    backgroundColor: Colors.lighter,
  },
  engine: {
    position: 'absolute',
    right: 0,
  },
  body: {
    backgroundColor: Colors.white,
  },
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
    color: Colors.black,
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
    color: Colors.dark,
  },
  highlight: {
    fontWeight: '700',
  },
  footer: {
    color: Colors.dark,
    fontSize: 12,
    fontWeight: '600',
    padding: 4,
    paddingRight: 12,
    textAlign: 'right',
  },
  safeArea: {
    padding: 15,
  },
});

export default App;
