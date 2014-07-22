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
package org.eclipse.scout.sdk.ui.fields.javacode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>CodeField</h3>
 */
public class JavaCodeField extends Composite {

  private static final String REGEX_WORD = "([a-zA-Z._$]+)";

  private StyledText m_text;

  private JavaCodeFieldContentProvider m_contentProvider;
  private ContentProposalAdapter m_proposalAdapter;
  private HashMap<String, JavaCodeRange> m_ranges = new HashMap<String, JavaCodeRange>();

  public JavaCodeField(Composite parent, IJavaSearchScope scope) {
    super(parent, SWT.NONE);
    m_text = new StyledText(this, SWT.SINGLE | SWT.BORDER);
    m_text.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        handleTextModified(false);
      }
    });
    P_PopupListener popupListener = new P_PopupListener();
    m_text.addListener(SWT.MouseExit, popupListener);
    m_text.addListener(SWT.Dispose, popupListener);
    m_text.addListener(SWT.KeyDown, popupListener);
    m_text.addListener(SWT.MouseDown, popupListener);
    m_text.addListener(SWT.MouseMove, popupListener);
    m_text.addListener(SWT.MouseHover, popupListener);
    m_text.addListener(SWT.FocusOut, popupListener);

    ControlDecoration deco = new ControlDecoration(m_text, SWT.LEFT | SWT.TOP);
    deco.setImage(ScoutSdkUi.getImage(ScoutSdkUi.ContentAssist));
    m_contentProvider = new JavaCodeFieldContentProvider(scope);

    m_proposalAdapter = new ContentProposalAdapter(m_text, new JavaCodeFieldContentAdapter(), m_contentProvider, null, new char[]{' '});
    m_proposalAdapter.setLabelProvider(new JavaCodeFieldLabelProvider());
    m_proposalAdapter.addContentProposalListener(new IContentProposalListener() {
      /*
       * (non-Javadoc)
       * @see org.eclipse.jface.fieldassist.IContentProposalListener#proposalAccepted(org.eclipse.jface.fieldassist.IContentProposal)
       */
      @Override
      public void proposalAccepted(IContentProposal proposal) {
        JavaTypeProposal tp = (JavaTypeProposal) proposal;
        JavaCodeRange codeRange = new JavaCodeRange(tp.getContent());
        codeRange.setFullyQualifiedName(tp.getType().getFullyQualifiedName());
        codeRange.setType(JavaCodeRange.QUALIFIED_TYPE);
        m_ranges.put(proposal.getContent(), codeRange);
        handleTextModified(true);
      }
    });
    m_proposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
    setLayout(new FillLayout());
  }

  public String getText() {
    return m_text.getText();
  }

  public void setText(String type) {
    m_text.setText(type);
    handleTextModified(true);
  }

  @Override
  public void addFocusListener(FocusListener listener) {
    m_text.addFocusListener(listener);
  }

  @Override
  public void removeFocusListener(FocusListener listener) {
    m_text.removeFocusListener(listener);
  }

  public String[] getAllImports() {
    ArrayList<String> imports = new ArrayList<String>();
    for (JavaCodeRange range : m_ranges.values()) {
      if (range.getType() == JavaCodeRange.QUALIFIED_TYPE) {
        imports.add(range.getFullyQualifiedName());
      }
    }
    return imports.toArray(new String[imports.size()]);
  }

  private void handleTextModified(boolean fullInput) {
    // close popup
    int cursorPosition = m_text.getCaretOffset();

    HashMap<String, JavaCodeRange> oldRanges = new HashMap<String, JavaCodeRange>(m_ranges);
    m_ranges.clear();
    Matcher m = Pattern.compile(REGEX_WORD, Pattern.DOTALL).matcher(m_text.getText());
    while (m.find()) {
      String word = m.group(1);
      int offset = m.start(1);
      int length = m.end(1) - offset;

      if (fullInput || cursorPosition < offset || cursorPosition > (offset + length)) {
        JavaCodeRange range = oldRanges.get(word);
        if (range != null) {
          // update range
          range.offset = offset;
          range.length = length;
        }
        else {
          range = new JavaCodeRange(word);
          range.offset = offset;
          range.length = length;
          IContentProposal[] pros = m_contentProvider.findExactMatch(word);
          if (pros.length == 1) {
            // accept
            JavaTypeProposal proposal = (JavaTypeProposal) pros[0];
            if (!proposal.isPrimitive()) {
              range.setFullyQualifiedName(proposal.getType().getFullyQualifiedName());
              range.setType(JavaCodeRange.QUALIFIED_TYPE);
            }
            else {
              range.setType(JavaCodeRange.PRIMITIV_TYPE);
            }
          }
          else {
            // text layout unknown
            range.setType(JavaCodeRange.UNKNOWN);
          }
        }
        m_ranges.put(word, range);
      }
    }
    if (!oldRanges.equals(m_ranges)) {
      updateStyles();
    }
  }

  private void updateStyles() {
    TreeMap<CompositeObject, StyleRange> ranges = new TreeMap<CompositeObject, StyleRange>();
    for (JavaCodeRange range : m_ranges.values()) {
      StyleRange stRange = null;
      switch (range.getType()) {
        case JavaCodeRange.UNKNOWN:
          stRange = new StyleRange(range.offset, range.length, null, null);
          stRange.underlineColor = m_text.getDisplay().getSystemColor(SWT.COLOR_RED);
          stRange.underlineStyle = SWT.UNDERLINE_SQUIGGLE;
          stRange.underline = true;
          break;
        case JavaCodeRange.PRIMITIV_TYPE:
        case JavaCodeRange.QUALIFIED_TYPE:
          stRange = new StyleRange(range.offset, range.length, null, null);
          stRange.fontStyle = SWT.BOLD;
          break;
      }
      ranges.put(new CompositeObject(range.offset, range.offset + range.length, range.getText()), stRange);
    }
    m_text.setStyleRanges(ranges.values().toArray(new StyleRange[ranges.size()]));
  }

  private JavaCodeRange getCodeRangeFor(int x, int y) {
    String text = m_text.getText();
    try {
      int offset = m_text.getOffsetAtLocation(new Point(x, y));
      if (offset < text.length()) {
        String before = text.substring(0, offset);
        String after = text.substring(offset);
        Matcher m = Pattern.compile("([a-zA-Z0-9$.]*)$").matcher(before);
        if (m.find()) {
          before = m.group(1);
          m = Pattern.compile("^([a-zA-Z0-9$.]*)").matcher(after);
          if (m.find()) {
            return m_ranges.get(before + m.group(1));
          }
        }
      }
    }
    catch (IllegalArgumentException e) {
    }
    return null;
  }

  private P_Popup m_popup = new P_Popup();

  private class P_PopupListener implements Listener {
    @Override
    public void handleEvent(Event event) {
      switch (event.type) {
        case SWT.MouseHover:
          m_popup.showFor(getCodeRangeFor(event.x, event.y), event.x, event.y);
          break;
        case SWT.Dispose:
        case SWT.KeyDown:
        case SWT.MouseDown:
        case SWT.MouseMove:
          m_popup.close();
          break;
      }
    }
  } // end class P_PopupListener

  private class P_Popup {

    private Shell m_shell;
    private Object m_lock = new Object();
    private JavaCodeRange m_range;

    public void showFor(JavaCodeRange range, int x, int y) {
      if (range == null) {
        close();
      }
      else {
        if (!range.equals(m_range) || m_shell == null) {
          if (m_shell != null) {
            close();
          }
          synchronized (m_lock) {
            m_shell = new Shell(m_text.getShell(), SWT.ON_TOP | SWT.TOOL);
            m_shell.setBackground(m_text.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
            m_shell.setLayout(new FormLayout());
            Label l = new Label(m_shell, SWT.SINGLE);
            l.setBackground(m_shell.getBackground());
            l.setText(range.getFullyQualifiedName());
            FormData data = new FormData();
            data.top = new FormAttachment(0, 5);
            data.left = new FormAttachment(0, 5);
            data.right = new FormAttachment(100, -5);
            data.bottom = new FormAttachment(100, -5);
            l.setLayoutData(data);
            Rectangle rect = m_text.getDisplay().map(m_text, null, new Rectangle(x + 10, y, 0, 0));
            m_shell.setLocation(rect.x, rect.y);
            m_shell.pack();
            m_shell.open();
          }
        }
      }
      m_range = range;
    }

    public void close() {
      synchronized (m_lock) {
        if (m_shell != null && !m_shell.isDisposed()) {
          m_shell.close();
        }
        m_shell = null;
      }
    }
  }
}
