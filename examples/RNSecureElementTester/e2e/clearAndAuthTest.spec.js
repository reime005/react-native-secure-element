async function justDecrypt() {
  const decryptButton = await element(by.id('test_button_user_auth_decrypt'));
  await expect(decryptButton).toBeVisible();
  await decryptButton.tap();
}

async function expectBasicAuthentication() {
  const encryptButton = await element(by.id('test_button_user_auth_encrypt'));
  await expect(encryptButton).toBeVisible();

  await encryptButton.tap();

  // if (Platform.OS === 'android') {
  //   //FIXME: [mr] Testing Android user auth does not work currently...
  //   const nativeBiometricPrompt = await element(by.type('ANDROID_TBD'));

  //   await expect(nativeBiometricPrompt).toBeVisible();

  //   await nativeBiometricPrompt.typeText('1111');
  //   await nativeBiometricPrompt.tapReturnKey();
  // }

  await justDecrypt();

  const decryptedResultText = await element(
    by.id('test_text_decrypted_user_result'),
  );
  await expect(decryptedResultText).toBeVisible();
  await expect(decryptedResultText).toHaveText('userAuthText');
}

describe('Clear and auth functions', () => {
  beforeEach(async () => {
    await device.reloadReactNative();
  });

  it('should handle user authenticated encryption/decryption', async () => {
    await expectBasicAuthentication();
  });

  it('should handle specific key removal', async () => {
    await expectBasicAuthentication();

    const clearElementButton = await element(by.id('test_button_clear_last'));
    await expect(clearElementButton).toBeVisible();

    await clearElementButton.tap();

    const decryptButton = await element(by.id('test_button_user_auth_decrypt'));

    await expect(decryptButton).toBeVisible();

    await justDecrypt();

    const decryptedResultText = await element(
      by.id('test_text_decrypted_user_result'),
    );
    await expect(decryptedResultText).toBeVisible();
    await expect(decryptedResultText).toHaveText('fail');
  });

  it('should handle clearAll keys call', async () => {
    const clearAllButton = await element(by.id('test_button_clear_all'));
    await expect(clearAllButton).toBeVisible();
    await clearAllButton.tap();

    // if an error pops up, the app should terminate, else there is still the root view (silent call)
    await expect(element(by.id('test_root_view'))).toBeVisible();
  });
});
