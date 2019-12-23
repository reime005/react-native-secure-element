async function expectBasicType(text) {
  const textInput = await element(by.id('test_text_input'));

    expect(textInput).toBeVisible();

    await textInput.typeText(text);

    await element(by.text('test_text_input_raw')).toHaveText(text);
}

describe('Text input', () => {
  it('should display raw text', async () => {
    const inText = 'testStringRandom42';
    await expectBasicType(inText);
  });

  it('should display encrypt and decrypt raw text', async () => {
    const inText = 'testStringRandom42';
    await expectBasicType(inText);

    const encryptedTextElement = await element(by.id('test_text_encrypted_text'));
    expect(encryptedTextElement).toBeVisible();

    const decryptedTextElement = await element(by.id('test_text_decrypted_text'));
    expect(decryptedTextElement).toBeVisible();
    expect(decryptedTextElement).toHaveText(inText);
  });
});
