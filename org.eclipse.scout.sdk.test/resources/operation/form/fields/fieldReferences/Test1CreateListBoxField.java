@Order(10.0)
    public class TestListboxField extends AbstractListBox<Long> {

      @Override
      protected Class<? extends ICodeType> getConfiguredCodeType() {
        return TestCodeType.class;
      }
    }