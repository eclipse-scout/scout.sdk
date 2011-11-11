/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.util.JaxWsSdkUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class StringPresenter extends AbstractPropertyPresenter<String> {

  protected StyledText m_styledText;
  protected String m_prefix;
  protected String m_suffix;
  private VerifyListener m_verifyListener;
  private ModifyListener m_modifyListener;
  private boolean m_editable;
  private String m_tooltip;

  public StringPresenter(Composite parent, FormToolkit toolkit) {
    super(parent, toolkit, false);
    m_verifyListener = new P_VerifyListener();
    m_modifyListener = new P_ModifyListener();
    setAcceptNullValue(true);
    callInitializer();
  }

  public void setPrefix(String prefix) {
    m_prefix = prefix;
  }

  public void setSuffix(String suffix) {
    m_suffix = suffix;
  }

  public boolean isEditable() {
    return m_editable;
  }

  public void setEditable(boolean editable) {
    m_editable = editable;
    if (isControlCreated()) {
      if (editable) {
        m_styledText.setBackground(null);
      }
      else {
        m_styledText.setBackground(JaxWsSdkUtility.getColorLightGray());
      }
    }
  }

  public String getTooltip() {
    return m_tooltip;
  }

  @Override
  public void setTooltip(String tooltip) {
    m_tooltip = tooltip;
    if (isControlCreated()) {
      m_styledText.setToolTipText(m_tooltip);
    }
  }

  @Override
  protected Control createContent(Composite parent) {
    m_styledText = new StyledText(parent, SWT.SINGLE | SWT.BORDER);
    m_styledText.addLineStyleListener(new P_LineStyleListener());
    m_styledText.addSelectionListener(new P_SelectionListener());
    m_styledText.addKeyListener(new P_KeyListener());
    m_styledText.addFocusListener(new P_FocusListener());
    m_styledText.addModifyListener(m_modifyListener);
    m_styledText.addVerifyListener(m_verifyListener);
    m_styledText.setEditable(false);
    m_styledText.setToolTipText(m_tooltip);
    setEditable(m_editable);
    return m_styledText;
  }

  @Override
  protected void setInputInternal(String input) {
    m_styledText.removeVerifyListener(m_verifyListener);
    m_styledText.removeModifyListener(m_modifyListener);
    try {
      m_styledText.setText(StringUtility.nvl(input, StringUtility.join("", m_prefix, m_suffix)));
    }
    finally {
      m_styledText.addVerifyListener(m_verifyListener);
      m_styledText.addModifyListener(m_modifyListener);
    }
  }

  private final class P_LineStyleListener implements LineStyleListener {

    @Override
    public void lineGetStyle(LineStyleEvent event) {
      List<StyleRange> styleRanges = new LinkedList<StyleRange>();

      // color prefix
      if (m_prefix != null && m_styledText.getText().startsWith(m_prefix)) {
        StyleRange styleRange = new StyleRange();
        styleRange.start = 0;
        styleRange.length = m_prefix.length();
        styleRange.foreground = ScoutSdkUi.getDisplay().getSystemColor(SWT.COLOR_BLUE);
        styleRanges.add(styleRange);
      }

      // color suffix
      if (m_suffix != null && m_styledText.getText().endsWith(m_suffix)) {
        StyleRange styleRange = new StyleRange();
        styleRange.start = m_styledText.getText().length() - m_suffix.length();
        styleRange.length = m_styledText.getText().length();
        styleRange.foreground = ScoutSdkUi.getDisplay().getSystemColor(SWT.COLOR_BLUE);
        styleRanges.add(styleRange);
      }

      // Set the styles for the line
      event.styles = (styleRanges.toArray(new StyleRange[styleRanges.size()]));
    }
  }

  private final class P_SelectionListener extends SelectionAdapter {

    @Override
    public void widgetSelected(SelectionEvent e) {
      int start = Math.min(e.x, e.y);
      int end = Math.max(e.x, e.y);

      e.x = start;
      e.y = end;

      if (m_prefix != null && m_styledText.getText().startsWith(m_prefix) && e.x < m_prefix.length()) {
        e.x = m_prefix.length();
        e.doit = false;
      }
      if (m_suffix != null && m_styledText.getText().endsWith(m_suffix) && e.y > (m_styledText.getText().length() - m_suffix.length())) {
        e.y = m_styledText.getText().length() - m_suffix.length();
        e.doit = false;
      }
      if (e.y < e.x) {
        e.y = e.x;
      }

      // adjust selection
      if (!e.doit) {
        m_styledText.removeSelectionListener(this);
        try {
          m_styledText.setSelection(e.x, e.y);
        }
        finally {
          m_styledText.addSelectionListener(this);
        }
      }
    }
  }

  private final class P_KeyListener extends KeyAdapter {

    @Override
    public void keyReleased(KeyEvent e) {
      int key = e.character;
      if (key > 0 && key <= 26) {
        key += 'a' - 1;
      }

      e.doit = true;
      if (e.stateMask == SWT.MOD1 && key == 'a') { // CTRL-A
        selectAll();
      }
    }

    private void selectAll() {
      int start = 0;
      int end = m_styledText.getText().length();

      if (m_prefix != null && m_styledText.getText().startsWith(m_prefix)) {
        start = m_prefix.length();
      }
      if (m_suffix != null && m_styledText.getText().endsWith(m_suffix)) {
        end = m_styledText.getText().length() - m_suffix.length();
      }

      // adjust selection
      m_styledText.setSelection(start, end);
    }
  }

  private final class P_VerifyListener implements VerifyListener {

    @Override
    public void verifyText(VerifyEvent e) {
      // check prefix
      if (m_prefix != null && m_styledText.getText().startsWith(m_prefix) && e.start < m_prefix.length()) {
        e.doit = false;
        return;
      }

      // check suffix
      if (m_suffix != null && m_styledText.getText().endsWith(m_suffix) && e.end > m_styledText.getText().length() - m_suffix.length()) {
        e.doit = false;
        return;
      }
      e.doit = true;
    }
  }

  private final class P_FocusListener extends FocusAdapter {

    @Override
    public void focusLost(FocusEvent e) {
      String newValue = m_styledText.getText();
      if (isValid(newValue)) {
        setValueFromUI(newValue);
      }
      else {
        setInfo(IMarker.SEVERITY_ERROR, Texts.get("InvalidValue"));
      }
    }
  }

  private final class P_ModifyListener implements ModifyListener {

    @Override
    public void modifyText(ModifyEvent e) {
      String newValue = m_styledText.getText();
      if (isValid(newValue)) {
        clearInfo();
      }
      else {
        setInfo(IMarker.SEVERITY_ERROR, Texts.get("InvalidValue"));
      }
    }
  }

  private boolean isValid(String value) {
    if (!StringUtility.hasText(value) && !isAcceptNullValue()) {
      return false;
    }
    if (m_prefix != null && (!value.startsWith(m_prefix) || value.equals(m_prefix))) {
      return false;
    }
    if (m_suffix != null && (!value.endsWith(m_suffix) || value.equals(m_suffix))) {
      return false;
    }

    if (m_prefix != null && m_suffix != null && value.equals(m_prefix + m_suffix)) {
      return false;
    }
    return true;
  }
}
