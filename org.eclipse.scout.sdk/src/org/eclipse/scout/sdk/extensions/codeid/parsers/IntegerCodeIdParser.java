package org.eclipse.scout.sdk.extensions.codeid.parsers;

public class IntegerCodeIdParser extends AbstractNumberCodeIdParser {
  public IntegerCodeIdParser() {
    super();
  }

  @Override
  protected void parseNum(String val) throws NumberFormatException {
    Integer.parseInt(val);
  }
}
