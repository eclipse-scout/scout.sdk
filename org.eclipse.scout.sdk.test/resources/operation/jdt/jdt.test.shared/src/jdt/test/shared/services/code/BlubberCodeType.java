package jdt.test.shared.services.code;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;

public class BlubberCodeType extends AbstractCodeType<Long> {

  private static final long serialVersionUID = 1L;
  public static final Long ID = 1362751349407L;

  public BlubberCodeType() throws ProcessingException {
    super();
  }

  @Override
  public Long getId() {
    return ID;
  }
}
