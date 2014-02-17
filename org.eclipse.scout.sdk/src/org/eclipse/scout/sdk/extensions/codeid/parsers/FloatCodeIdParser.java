package org.eclipse.scout.sdk.extensions.codeid.parsers;

public class FloatCodeIdParser extends AbstractNumberCodeIdParser {
  public FloatCodeIdParser() {
    super('F');
  }

  @Override
  protected void parseNum(String val) throws NumberFormatException {
    Float.parseFloat(val);
  }
}
