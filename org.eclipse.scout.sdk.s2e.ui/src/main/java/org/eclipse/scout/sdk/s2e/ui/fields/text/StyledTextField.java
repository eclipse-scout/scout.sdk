/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.fields.text;

import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.util.OptimisticLock;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * <h3>{@link StyledTextField} with optional read-only suffix and prefix.</h3>
 *
 * @since 5.2.0
 */
public class StyledTextField extends TextField {

  private P_SuffixListener m_suffixListener;
  private String m_readOnlyPrefix;
  private String m_readOnlySuffix;
  private final OptimisticLock m_revealLock = new OptimisticLock();

  /**
   * Creates a new {@link StyledTextField} with a label and no image.
   */
  public StyledTextField(Composite parent) {
    super(parent);
  }

  /**
   * @see TextField#TextField(Composite, int)
   */
  public StyledTextField(Composite parent, int type) {
    super(parent, type);
  }

  /**
   * @see TextField#TextField(Composite, int, int)
   */
  public StyledTextField(Composite parent, int type, int labelWidth) {
    super(parent, type, labelWidth);
  }

  public String getReadOnlySuffix() {
    return m_readOnlySuffix;
  }

  public synchronized String getModifiableText() {
    var text = getText();
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
    if (Strings.isEmpty(getReadOnlyPrefix()) && Strings.isEmpty(getReadOnlySuffix())) {
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

  @Override
  public synchronized void setText(String text) {
    if (text == null) {
      text = "";
    }
    var prefix = "";
    var suffix = "";

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

    var start = 0;
    var end = text.length();
    var lowerText = text.toLowerCase(Locale.US);
    if (Strings.hasText(prefix) && lowerText.startsWith(prefix.toLowerCase(Locale.US))) {
      start = prefix.length();
    }
    if (Strings.hasText(suffix) && lowerText.endsWith(suffix.toLowerCase(Locale.US))) {
      end = text.length() - suffix.length();
    }

    var sb = new StringBuilder(end - start + prefix.length() + suffix.length());
    sb.append(prefix);
    sb.append(text, start, end);
    sb.append(suffix);
    super.setText(sb.toString());
  }

  private final class P_SuffixListener implements Listener, VerifyKeyListener {
    private static final String PRE_POST_FIX_REGEX = "^(#0#).*(#1#)$";

    private Pattern m_preSuffixPattern;
    private final StyleRange m_suffixStyleRange;
    private final StyleRange m_prefixStyleRange;
    private String m_suffixString = "";
    private String m_prefixString = "";

    private P_SuffixListener() {
      this("");
    }

    private P_SuffixListener(String suffix) {
      this(suffix, null);
    }

    private P_SuffixListener(String postfix, StyleRange style) {
      if (style == null) {
        style = new StyleRange(-1, -1, getDisplay().getSystemColor(SWT.COLOR_BLUE), null);
      }
      m_suffixStyleRange = new StyleRange(-1, -1, style.foreground, style.background, style.fontStyle);
      m_prefixStyleRange = new StyleRange(-1, -1, style.foreground, style.background, style.fontStyle);
      m_preSuffixPattern = Pattern.compile(replace(PRE_POST_FIX_REGEX, m_prefixString, m_suffixString));
      setSuffix(postfix);
    }

    /**
     * A regex may contain several placeholders marked as #0# ... #xx#. This method is used to replace the placeholders.
     *
     * @param regex
     *          the regex containing the exactly same amount of placeholders as the replacements contains items.
     * @param replacements
     *          the array of replacements
     * @return replaced regex expression.
     */
    private static String replace(String regex, String... replacements) {
      var sb = new StringBuilder(regex);
      for (var i = 0; i < replacements.length; i++) {
        var index = 0;
        var placeholder = "#" + i + '#';
        while ((index = sb.indexOf(placeholder, index)) >= 0) {
          sb.replace(index, index + placeholder.length(), replacements[i]);
        }
      }
      return sb.toString();
    }

    @Override
    public void verifyKey(VerifyEvent event) {
      var e = new Event();
      e.keyCode = event.keyCode;
      e.character = event.character;
      e.doit = event.doit;
      e.widget = event.widget;
      e.type = SWT.Verify;
      handleEvent(e);
      event.doit = e.doit;
    }

    @Override
    @SuppressWarnings({"squid:SwitchLastCaseIsDefaultCheck", "SuspiciousNameCombination"})
    public void handleEvent(Event event) {
      try {
        if (m_revealLock.acquire()) {
          var textSelection = getTextComponent().getSelection();
          var suffixMatcher = m_preSuffixPattern.matcher(getTextComponent().getText());
          if (suffixMatcher.find()) {
            switch (event.type) {
              case SWT.Verify -> {
                if (event.character == SWT.BS) {
                  event.doit = !(textSelection.x - 1 < suffixMatcher.end(1) && textSelection.x == textSelection.y);
                }
                if (event.character == SWT.DEL) {
                  event.doit = !(textSelection.x + 1 > suffixMatcher.start(2) && textSelection.x == textSelection.y);
                }
              }
              case SWT.Modify -> updateStyleRanges();
              case SWT.KeyDown, SWT.MouseDown, SWT.Selection -> {
                var selection = getTextComponent().getSelection();
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
              }
              case SWT.FocusIn -> {
                var x = getTextComponent().getSelection().x;
                if (x < suffixMatcher.end(1) || x > suffixMatcher.start(2)) {
                  getTextComponent().setSelection(suffixMatcher.end(1));
                }
              }
            }
          }

        }
      }
      finally {
        m_revealLock.release();
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
        if (m_revealLock.acquire()) {
          if (postfix == null) {
            postfix = "";
          }
          // remove old suffix
          StyledText text = getTextComponent();
          if (m_suffixString != null) {
            var suffixMatcher = m_preSuffixPattern.matcher(text.getText());
            if (suffixMatcher.find()) {
              var newText = text.getText().substring(0, suffixMatcher.start(2));
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
        m_revealLock.release();
      }

    }

    void setPrefix(String prefix) {
      try {
        if (m_revealLock.acquire()) {
          if (prefix == null) {
            prefix = "";
          }
          // remove old suffix
          StyledText text = getTextComponent();
          if (m_prefixString != null) {
            var suffixMatcher = m_preSuffixPattern.matcher(text.getText());
            if (suffixMatcher.find()) {
              var newText = text.getText().substring(suffixMatcher.end(1));
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
        m_revealLock.release();
      }

    }

    private void updateStyleRanges() {
      var preSuffixMatcher = m_preSuffixPattern.matcher(getTextComponent().getText());
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
