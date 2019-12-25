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
  Platform,
} from 'react-native';

import {Colors} from 'react-native/Libraries/NewAppScreen';

import {
  SecureElement,
  IOSKeyGenOptions,
  AndroidKeyGenOptions,
} from 'react-native-secure-element/src';

const commonOptions = Platform.select({
  android: commonAndroidOptions,
  ios: commonIOSOptions,
});

const commonAndroidOptions: AndroidKeyGenOptions = {
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
  privateSACAccessible: '',
  publicSACAccessible: '',
};

const commonIOSOptions: IOSKeyGenOptions = {
  userPrompt: 'The test app requires TouchID access',
  privateSACFlags: [],
  publicSACFlags: [],
  privateSACAccessible: 'kSecAttrAccessibleWhenUnlockedThisDeviceOnly',
  publicSACAccessible: 'kSecAttrAccessibleAlwaysThisDeviceOnly',
  secAttrType: 'ECSECPrimeRandom',
  saveInSecureEnclaveIfPossible: true,
  algorithm: 'SHA256',
  privateKeySizeInBits: 256,
  publicKeyName: 'secure.element.key',
  privateKeyName: 'secure.element.key',
  touchIDAuthenticationAllowableReuseDuration: 60,
};

const App: () => React$Node = () => {
  const secureElement = useRef(new SecureElement());
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
  }, [secureElement]);

  useEffect(() => {
    if (!secureElement || !secureElement.current) {
      return;
    }

    secureElement.current
      .encrypt('test.key.id.42', plainText, {
        ...commonOptions,
      })
      .then(text => setEncryptedText(text))
      .catch(() => setEncryptedText('fail'));
  }, [plainText]);

  useEffect(() => {
    if (!secureElement || !secureElement.current) {
      return;
    }

    secureElement.current
      .decrypt('test.key.id.42', encryptedText, {
        ...commonOptions,
      })
      .then(text => setDecryptedText(text))
      .catch(() => setDecryptedText('fail'));
  }, [encryptedText]);

  return (
    <>
      <StatusBar barStyle="dark-content" />
      <SafeAreaView style={styles.safeArea} testID="test_root_view">
        <TextInput
          testID="test_text_input"
          style={{borderBottomColor: 'green', borderBottomWidth: 3, margin: 10}}
          onChangeText={text => setPlainText(text)}
          placeholder="Enter some text"
        />

        <ScrollView
          testID="test_scroll_view"
          contentInsetAdjustmentBehavior="automatic"
          style={styles.scrollView}>
          <Text style={styles.title}>Plain text:</Text>
          <Text style={styles.resultText} testID="test_text_input_raw">
            {plainText}
          </Text>

          <Text style={styles.title}>Encrypted text:</Text>
          <Text style={styles.resultText} testID="test_text_input_encrypted">
            {encryptedText}
          </Text>

          <Text style={styles.title}>Decrypted text:</Text>
          <Text style={styles.resultText} testID="test_text_input_decrypted">
            {decryptedText}
          </Text>

          <Button
            testID="test_button_user_auth_encrypt"
            title='encrypt "userAuthText" with user auth'
            onPress={() => {
              secureElement.current
                .encrypt('test.key.id.userAuth', 'userAuthText', {
                  ...commonOptions,
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
          <Button
            testID="test_button_user_auth_decrypt"
            title='decrypt "userAuthText" with user auth'
            onPress={() => {
              secureElement.current
                .decrypt('test.key.id.userAuth', encryptedUserAuthText, {
                  ...commonOptions,
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
          <Text
            style={styles.resultText}
            testID="test_text_decrypted_user_result">
            {userAuthText}
          </Text>

          <View style={{flex: 1}}>
            <Button
              testID="test_button_clear_last"
              title="clear only user auth key"
              onPress={() => {
                secureElement.current
                  .clearElement('test.key.id.userAuth', 'AndroidKeyStore')
                  .then(() => console.warn('cleared'))
                  .catch(e => console.warn(e.message));
              }}
            />

            <Button
              testID="test_button_clear_all"
              title="clear all keys"
              onPress={() => {
                secureElement.current
                  .clearAll('AndroidKeyStore')
                  .then(() => console.warn('all clean'))
                  .catch(e => console.error(e.message));
              }}
            />
          </View>

          <Text style={styles.title}>Device features:</Text>
          {Array.isArray(deviceFeatures) && (
            <Text testID="test_text_device_features_available">
              {deviceFeatures.join(', ')}
            </Text>
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
