package sample.shared.services.code;

import java.io.File;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;

public class FileCodeType extends AbstractCodeType<File> {

  private static final long serialVersionUID = 1L;
  public static final File ID = null;//TODO [aho] Auto-generated value

  public FileCodeType() throws ProcessingException {
    super();
  }

  @Override
  public File getId() {
    return ID;
  }
}
