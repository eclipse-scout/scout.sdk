package org.eclipse.scout.sdk.extensions.codeid.parsers;

public class LongCodeIdParser extends AbstractNumberCodeIdParser {
  public LongCodeIdParser() {
    super('L');
  }

  @Override
  protected void parseNum(String val) throws NumberFormatException {
    Long.parseLong(val);
  }
}
