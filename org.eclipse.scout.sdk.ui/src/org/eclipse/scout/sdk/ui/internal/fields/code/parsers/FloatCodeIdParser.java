package org.eclipse.scout.sdk.ui.internal.fields.code.parsers;

public class FloatCodeIdParser extends AbstractNumberCodeIdParser {
  public FloatCodeIdParser() {
    super('F');
  }

  @Override
  protected void parseNum(String val) throws NumberFormatException {
    Float.parseFloat(val);
  }
}
