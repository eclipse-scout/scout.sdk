package sample.shared.services.code;

import java.io.File;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;

public class EmptyCodeType extends AbstractCodeType<Long, File> {

  private static final long serialVersionUID = 1L;
  public static final Long ID = 1365148733309L;

  public EmptyCodeType() throws ProcessingException {
    super();
  }

  @Override
  public Long getId() {
    return ID;
  }
}
