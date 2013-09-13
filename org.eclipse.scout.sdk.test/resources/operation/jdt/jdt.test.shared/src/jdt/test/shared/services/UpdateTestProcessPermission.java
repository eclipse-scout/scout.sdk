package jdt.test.shared.services;

import java.security.BasicPermission;

public class UpdateTestProcessPermission extends BasicPermission{

  private static final long serialVersionUID = 0L;

  public UpdateTestProcessPermission() {
  super("UpdateTestProcess");
  }
}
