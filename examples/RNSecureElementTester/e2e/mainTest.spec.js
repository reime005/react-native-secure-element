describe('Main component', () => {
  it('should should be visible', async () => {
    await element(by.id('test_root_view')).toBeVisible();
  });
});
