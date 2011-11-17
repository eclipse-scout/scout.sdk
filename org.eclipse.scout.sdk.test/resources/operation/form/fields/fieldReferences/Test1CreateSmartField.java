@Order(10.0)
    public class TestSmartField extends AbstractSmartField<Long> {

      @Override
      protected Class<? extends ICodeType<Long>> getConfiguredCodeType() {
        return TestCodeType.class;
      }
    }