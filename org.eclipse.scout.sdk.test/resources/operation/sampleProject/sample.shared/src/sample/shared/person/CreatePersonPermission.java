package sample.shared.person;

import java.security.BasicPermission;

public class CreatePersonPermission extends BasicPermission {

  private static final long serialVersionUID = 0L;

  public CreatePersonPermission() {
    super("CreatePerson");
  }
}
