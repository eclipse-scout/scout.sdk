package formdata.shared.security;

import java.security.BasicPermission;

public class ReadSpecialAnnotationsPermission extends BasicPermission {

  private static final long serialVersionUID = 0L;

  public ReadSpecialAnnotationsPermission() {
  super("ReadSpecialAnnotations");
  }
}
