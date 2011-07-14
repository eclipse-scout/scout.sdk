package formdata.shared.security;

import java.security.BasicPermission;

public class CreateSpecialAnnotationsPermission extends BasicPermission {

  private static final long serialVersionUID = 0L;

  public CreateSpecialAnnotationsPermission() {
  super("CreateSpecialAnnotations");
  }
}
