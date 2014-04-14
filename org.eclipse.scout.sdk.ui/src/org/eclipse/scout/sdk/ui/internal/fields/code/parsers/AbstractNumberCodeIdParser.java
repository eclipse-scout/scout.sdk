package org.eclipse.scout.sdk.ui.internal.fields.code.parsers;

import org.eclipse.scout.sdk.ui.extensions.ICodeIdParser;

public abstract class AbstractNumberCodeIdParser implements ICodeIdParser {
  private final String m_numTypeSuffixLo;
  private final String m_numTypeSuffixUp;

  protected AbstractNumberCodeIdParser() {
    this(null);
  }

  protected AbstractNumberCodeIdParser(Character numTypeSuffix) {
    if (numTypeSuffix != null) {
      m_numTypeSuffixLo = (numTypeSuffix + "").toLowerCase();
      m_numTypeSuffixUp = (numTypeSuffix + "").toUpperCase();
    }
    else {
      m_numTypeSuffixLo = null;
      m_numTypeSuffixUp = null;
    }
  }

  protected abstract void parseNum(String val) throws NumberFormatException;

  @Override
  public boolean isValid(String val) {
    try {
      if (m_numTypeSuffixLo != null && m_numTypeSuffixUp != null) {
        val = val.replaceAll("[" + m_numTypeSuffixUp + m_numTypeSuffixLo + "]{0,1}$", "");
      }
      parseNum(val);
      return true;
    }
    catch (NumberFormatException e) {
    }
    return false;
  }

  @Override
  public String getSource(String val) {
    if (val == null) {
      return null;
    }
    else if (m_numTypeSuffixLo != null && val.toLowerCase().endsWith(m_numTypeSuffixLo)) {
      return val;
    }
    else {
      if (m_numTypeSuffixLo == null) {
        return val;
      }
      else {
        return val + m_numTypeSuffixUp;
      }
    }
  }
}