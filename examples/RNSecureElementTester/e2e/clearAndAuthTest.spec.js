const delay = n => new Promise(r => setTimeout(() => r(), n));

async function justDecrypt() {
  const decryptButton = await element(by.id('test_button_user_auth_decrypt'));
  await expect(decryptButton).toBeVisible();
  await decryptButton.tap();

  await unlockAndroidDevice();
}

async function justEncrypt() {
  const encryptButton = await element(by.id('test_button_user_auth_encrypt'));
  await expect(encryptButton).toBeVisible();
  await encryptButton.tap();

  await unlockAndroidDevice();
}

async function unlockAndroidDevice() {
  if (/android/i.test(process.env.configuration)) {
    await delay(2000);
    const { execWithRetriesAndLogs } = require('detox/src/utils/exec');

    await execWithRetriesAndLogs('adb shell input text 1111');
    await execWithRetriesAndLogs('adb shell input keyevent KEYCODE_ENTER');
  }
}

async function expectBasicAuthentication() {
  const clearAllButton = await element(by.id('test_button_clear_all'));
  await expect(clearAllButton).toBeVisible();
  await clearAllButton.tap();

  await justEncrypt();
  await justDecrypt();

  await waitFor(element(by.id('test_text_decrypted_user_result'))).toBeVisible().withTimeout(2000);

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

  // it('should handle user authenticated encryption/decryption', async () => {
  //   await expectBasicAuthentication();
  // });

  it('should handle specific key removal', async () => {
    await expectBasicAuthentication();

    const clearElementButton = await element(by.id('test_button_clear_last'));
    await expect(clearElementButton).toBeVisible();
    await clearElementButton.tap();

    await justDecrypt();

    await waitFor(element(by.id('test_text_decrypted_user_result'))).toBeVisible().withTimeout(2000);
    const decryptedResultText = await element(
      by.id('test_text_decrypted_user_result'),
    );
    await expect(decryptedResultText).toBeVisible();
    await expect(decryptedResultText).toHaveText('fail');
  });

  // it('should handle clearAll keys call', async () => {
  //   const clearAllButton = await element(by.id('test_button_clear_all'));
  //   await expect(clearAllButton).toBeVisible();
  //   await clearAllButton.tap();

  //   // if an error pops up, the app should terminate, else there is still the root view (silent call)
  //   await expect(element(by.id('test_root_view'))).toBeVisible();
  // });
});
