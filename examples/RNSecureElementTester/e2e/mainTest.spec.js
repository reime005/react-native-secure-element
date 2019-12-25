describe('Main component', () => {
  it('should should be visible', async () => {
    await expect(element(by.id('test_root_view'))).toBeVisible();
  });
});
