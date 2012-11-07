package formfield.shared.services.code;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;

public class TestCodeType extends AbstractCodeType<Long> {

  private static final long serialVersionUID = 1L;
  public static final long ID = 106634L;

  public TestCodeType() throws ProcessingException {
    super();
  }

  @Override
  public Long getId() {
    return ID;
  }
}
