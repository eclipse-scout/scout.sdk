package jdt.test.shared.services;

import java.security.BasicPermission;

public class ReadTestProcessPermission extends BasicPermission{

  private static final long serialVersionUID = 0L;

  public ReadTestProcessPermission() {
  super("ReadTestProcess");
  }
}
