package sample.shared.empty;

import java.security.BasicPermission;

public class CreateEmptyPermission extends BasicPermission {

  private static final long serialVersionUID = 0L;

  public CreateEmptyPermission() {
    super("CreateEmpty");
  }
}
