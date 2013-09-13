package jdt.test.shared.services;

import java.security.BasicPermission;

public class CreateTestProcessPermission extends BasicPermission{

  private static final long serialVersionUID = 0L;

  public CreateTestProcessPermission() {
  super("CreateTestProcess");
  }
}
