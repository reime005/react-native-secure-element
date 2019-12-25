async function expectBasicType(text) {
  const textInput = await element(by.id('test_text_input'));

  await expect(textInput).toBeVisible();

  await textInput.clearText();
  await textInput.typeText(text);
  await textInput.tapReturnKey();

  await expect(element(by.id('test_text_input_raw'))).toHaveText(text);
}

describe('Text input', () => {
  it('should display raw text', async () => {
    const inText = 'testStringRandom42';
    await expectBasicType(inText);
  });

  it('should display encrypt and decrypt raw text', async () => {
    const inText = 'testStringRandom42';
    await expectBasicType(inText);

    const encryptedTextElement = await element(
      by.id('test_text_input_encrypted'),
    );
    await expect(encryptedTextElement).toBeVisible();

    const decryptedTextElement = await element(
      by.id('test_text_input_decrypted'),
    );
    await expect(decryptedTextElement).toBeVisible();
    await expect(decryptedTextElement).toHaveText(inText);
  });
});
