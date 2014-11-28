/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.fields;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class StyledTextField extends TextField {

  private P_SuffixListener m_suffixListener = null;
  private String m_readOnlyPrefix;
  private String m_readOnlySuffix;
  private final OptimisticLock m_revalLock = new OptimisticLock();

  /**
   * @param parent
   * @param style
   *          of the text field
   */
  public StyledTextField(Composite parent) {
    super(parent);
  }

  public StyledTextField(Composite parent, String labelName) {
    super(parent, labelName);
  }

  public StyledTextField(Composite parent, String labelName, int labelPercentage) {
    super(parent, labelName, labelPercentage);
  }

  public String getReadOnlySuffix() {
    return m_readOnlySuffix;
  }

  public synchronized String getModifiableText() {
    String text = getText();
    if (m_suffixListener != null && text.length() >= m_suffixListener.getSuffix().length()) {
      text = text.substring(0, text.length() - m_suffixListener.getSuffix().length());
    }
    return text;
  }

  public void setReadOnlySuffix(String suffix) {
    m_readOnlySuffix = suffix;
    updateListeners();
  }

  public void setReadOnlyPrefix(String readOnlyPrefix) {
    m_readOnlyPrefix = readOnlyPrefix;
    updateListeners();
  }

  public String getReadOnlyPrefix() {
    return m_readOnlyPrefix;
  }

  private synchronized void updateListeners() {
    StyledText textComp = getTextComponent();
    if (StringUtility.isNullOrEmpty(getReadOnlyPrefix()) && StringUtility.isNullOrEmpty(getReadOnlySuffix())) {
      // uninstall
      if (m_suffixListener != null) {
        textComp.removeListener(SWT.Selection, m_suffixListener);
        textComp.removeListener(SWT.MouseDown, m_suffixListener);
        textComp.removeListener(SWT.Modify, m_suffixListener);
        textComp.removeVerifyKeyListener(m_suffixListener);
        textComp.removeListener(SWT.KeyDown, m_suffixListener);
        textComp.removeListener(SWT.FocusIn, m_suffixListener);
        m_suffixListener = null;
      }
    }
    else {
      if (m_suffixListener == null) {
        m_suffixListener = new P_SuffixListener();
      }
      m_suffixListener.setPrefix(getReadOnlyPrefix());
      m_suffixListener.setSuffix(getReadOnlySuffix());
      textComp.addListener(SWT.Selection, m_suffixListener);
      textComp.addListener(SWT.MouseDown, m_suffixListener);
      textComp.addListener(SWT.Modify, m_suffixListener);
      textComp.addVerifyKeyListener(m_suffixListener);
      textComp.addListener(SWT.KeyDown, m_suffixListener);
      textComp.addListener(SWT.FocusIn, m_suffixListener);

    }
  }

  /**
   * A regex may contain several placeholders marked as #0# ... #xx#. This method is used to replace
   * the placeholders.
   *
   * @param regex
   *          the regex containing the exactly same amount of placeholders as the replacements conatins items.
   * @param replacements
   *          the array of replacements
   * @return replaced regex expression.
   */
  private static String replace(String regex, String... replacements) {
    StringBuilder sb = new StringBuilder(regex);
    for (int i = 0; i < replacements.length; i++) {
      int index = 0;
      String placeholder = "#" + i + "#";
      while ((index = sb.indexOf(placeholder, index)) >= 0) {
        sb.replace(index, index + placeholder.length(), replacements[i]);
      }
    }
    return sb.toString();
  }

  @Override
  public synchronized void setText(String text) {
    if (text == null) {
      text = "";
    }
    String prefix = "";
    String suffix = "";

    if (m_suffixListener != null) {
      prefix = m_suffixListener.getPrefix();
      suffix = m_suffixListener.getSuffix();
      if (prefix == null) {
        prefix = "";
      }
      if (suffix == null) {
        suffix = "";
      }
    }

    int start = 0;
    int end = text.length();
    String lowerText = text.toLowerCase();
    if (StringUtility.hasText(prefix) && lowerText.startsWith(prefix.toLowerCase())) {
      start = prefix.length();
    }
    if (StringUtility.hasText(suffix) && lowerText.endsWith(suffix.toLowerCase())) {
      end = text.length() - suffix.length();
    }

    StringBuilder sb = new StringBuilder(end - start + prefix.length() + suffix.length());
    sb.append(prefix);
    sb.append(text.substring(start, end));
    sb.append(suffix);
    super.setText(sb.toString());
  }

  private class P_SuffixListener implements Listener, VerifyKeyListener {
    private static final String PRE_POST_FIX_REGEX = "^(#0#).*(#1#)$";

    private Pattern m_preSuffixPattern;
    private StyleRange m_suffixStyleRange;
    private StyleRange m_prefixStyleRange;
    private String m_suffixString = "";
    private String m_prefixString = "";

    public P_SuffixListener() {
      this("");
    }

    public P_SuffixListener(String suffix) {
      this(suffix, null);
    }

    public P_SuffixListener(String postfix, StyleRange style) {
      if (style == null) {
        style = new StyleRange(-1, -1, getDisplay().getSystemColor(SWT.COLOR_BLUE), null);
      }
      m_suffixStyleRange = new StyleRange(-1, -1, style.foreground, style.background, style.fontStyle);
      m_prefixStyleRange = new StyleRange(-1, -1, style.foreground, style.background, style.fontStyle);
      m_preSuffixPattern = Pattern.compile(replace(PRE_POST_FIX_REGEX, m_prefixString, m_suffixString));
      setSuffix(postfix);
    }

    @Override
    public void verifyKey(VerifyEvent event) {
      Event e = new Event();
      e.keyCode = event.keyCode;
      e.character = event.character;
      e.doit = event.doit;
      e.widget = event.widget;
      e.type = SWT.Verify;
      handleEvent(e);
      event.doit = e.doit;
    }

    @Override
    public void handleEvent(Event event) {
      try {
        if (m_revalLock.acquire()) {
          Point textSelection = getTextComponent().getSelection();
          Matcher suffixMatcher = m_preSuffixPattern.matcher(getTextComponent().getText());
          if (suffixMatcher.find()) {
            switch (event.type) {
              case SWT.Verify: {
                if (event.character == SWT.BS) {
                  event.doit = !(textSelection.x - 1 < suffixMatcher.end(1) && textSelection.x == textSelection.y);
                }
                if (event.character == SWT.DEL) {
                  event.doit = !(textSelection.x + 1 > suffixMatcher.start(2) && textSelection.x == textSelection.y);
                }
                break;
              }
              case SWT.Modify: {
                updateStyleRanges();
                break;
              }
              case SWT.KeyDown:
              case SWT.MouseDown:
              case SWT.Selection:
                Point selection = getTextComponent().getSelection();
                if (selection.x < suffixMatcher.end(1)) {
                  selection.x = suffixMatcher.end(1);
                  if (selection.x > selection.y) {
                    selection.y = selection.x;
                  }
                  event.doit = false;
                }
                if (selection.y > suffixMatcher.start(2)) {
                  selection.y = suffixMatcher.start(2);
                  if (selection.y < selection.x) {
                    selection.x = selection.y;
                  }
                  event.doit = false;
                }
                if (!event.doit) {
                  getTextComponent().setSelection(selection);
                }
                break;
              case SWT.FocusIn:
                int x = getTextComponent().getSelection().x;
                if (x < suffixMatcher.end(1) || x > suffixMatcher.start(2)) {
                  getTextComponent().setSelection(suffixMatcher.end(1));
                }
                break;
            }
          }

        }
      }
      finally {
        m_revalLock.release();
      }

    }

    String getPrefix() {
      return m_prefixString;
    }

    String getSuffix() {
      return m_suffixString;
    }

    void setSuffix(String postfix) {
      try {
        if (m_revalLock.acquire()) {
          if (postfix == null) {
            postfix = "";
          }
          // remove old suffix
          StyledText text = getTextComponent();
          if (m_suffixString != null) {
            Matcher suffixMatcher = m_preSuffixPattern.matcher(text.getText());
            if (suffixMatcher.find()) {
              String newText = text.getText().substring(0, suffixMatcher.start(2));
              text.setText(newText);
            }
          }
          m_suffixString = postfix;
          m_preSuffixPattern = Pattern.compile(replace(PRE_POST_FIX_REGEX, m_prefixString, m_suffixString));
          if (!text.getText().endsWith(m_suffixString)) {
            text.setText(text.getText() + m_suffixString);
          }
          updateStyleRanges();
        }
      }
      finally {
        m_revalLock.release();
      }

    }

    void setPrefix(String prefix) {
      try {
        if (m_revalLock.acquire()) {
          if (prefix == null) {
            prefix = "";
          }
          // remove old suffix
          StyledText text = getTextComponent();
          if (m_prefixString != null) {
            Matcher suffixMatcher = m_preSuffixPattern.matcher(text.getText());
            if (suffixMatcher.find()) {
              String newText = text.getText().substring(suffixMatcher.end(1));
              text.setText(newText);
            }
          }
          m_prefixString = prefix;
          m_preSuffixPattern = Pattern.compile(replace(PRE_POST_FIX_REGEX, m_prefixString, m_suffixString));
          if (!text.getText().startsWith(m_prefixString)) {
            text.setText(m_prefixString + text.getText());
          }
          updateStyleRanges();
        }
      }
      finally {
        m_revalLock.release();
      }

    }

    private void updateStyleRanges() {
      Matcher preSuffixMatcher = m_preSuffixPattern.matcher(getTextComponent().getText());
      if (preSuffixMatcher.find()) {
        m_suffixStyleRange.start = preSuffixMatcher.start(2);
        m_suffixStyleRange.length = preSuffixMatcher.end(2) - preSuffixMatcher.start(2);
        m_prefixStyleRange.start = preSuffixMatcher.start(1);
        m_prefixStyleRange.length = preSuffixMatcher.end(1) - preSuffixMatcher.start(1);
        getTextComponent().setStyleRanges(new StyleRange[]{m_prefixStyleRange, m_suffixStyleRange});
      }
      else {
        getTextComponent().setStyleRanges(null);
      }
    }
  } // end class P_SuffixListener
}
