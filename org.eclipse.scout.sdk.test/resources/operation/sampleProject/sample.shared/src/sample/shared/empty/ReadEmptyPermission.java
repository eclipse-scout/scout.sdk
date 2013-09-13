package sample.shared.empty;

import java.security.BasicPermission;

public class ReadEmptyPermission extends BasicPermission {

  private static final long serialVersionUID = 0L;

  public ReadEmptyPermission() {
    super("ReadEmpty");
  }
}
