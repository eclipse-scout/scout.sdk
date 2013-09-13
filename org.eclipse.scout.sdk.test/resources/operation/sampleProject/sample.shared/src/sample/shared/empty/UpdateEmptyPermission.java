package sample.shared.empty;

import java.security.BasicPermission;

public class UpdateEmptyPermission extends BasicPermission {

  private static final long serialVersionUID = 0L;

  public UpdateEmptyPermission() {
    super("UpdateEmpty");
  }
}
