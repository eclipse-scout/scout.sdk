package org.eclipse.scout.sdk.ui.internal.fields.code.parsers;

public class LongCodeIdParser extends AbstractNumberCodeIdParser {
  public LongCodeIdParser() {
    super('L');
  }

  @Override
  protected void parseNum(String val) throws NumberFormatException {
    Long.parseLong(val);
  }
}
