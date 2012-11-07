package org.eclipse.scout.sdk.ui.internal.fields.code.parsers;

public class DoubleCodeIdParser extends AbstractNumberCodeIdParser {
  public DoubleCodeIdParser() {
    super('D');
  }

  @Override
  protected void parseNum(String val) throws NumberFormatException {
    Double.parseDouble(val);
  }
}
