package formdata.shared.security;

import java.security.BasicPermission;

public class UpdateSpecialAnnotationsPermission extends BasicPermission {

  private static final long serialVersionUID = 0L;

  public UpdateSpecialAnnotationsPermission() {
  super("UpdateSpecialAnnotations");
  }
}
