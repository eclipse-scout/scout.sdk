package org.eclipse.scout.sdk.ui.internal.fields.code.parsers;

public class IntegerCodeIdParser extends AbstractNumberCodeIdParser {
  public IntegerCodeIdParser() {
    super();
  }

  @Override
  protected void parseNum(String val) throws NumberFormatException {
    Integer.parseInt(val);
  }
}
