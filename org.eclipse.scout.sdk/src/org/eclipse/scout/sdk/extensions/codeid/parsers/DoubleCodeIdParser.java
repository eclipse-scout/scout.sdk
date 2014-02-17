package org.eclipse.scout.sdk.extensions.codeid.parsers;

public class DoubleCodeIdParser extends AbstractNumberCodeIdParser {
  public DoubleCodeIdParser() {
    super('D');
  }

  @Override
  protected void parseNum(String val) throws NumberFormatException {
    Double.parseDouble(val);
  }
}
