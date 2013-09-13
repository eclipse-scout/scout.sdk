package sample.shared.person;

import java.security.BasicPermission;

public class ReadPersonPermission extends BasicPermission {

  private static final long serialVersionUID = 0L;

  public ReadPersonPermission() {
    super("ReadPerson");
  }
}
