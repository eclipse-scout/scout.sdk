/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.scout.sdk.core.s.nls.ITranslationEntry;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.util.EventListenerList;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.TranslationInputValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class NlsTableCursor {

  private static final Pattern TEXT_EDIT_BEGIN_PAT = Pattern.compile("[a-zA-Z0-9]");
  private static final String COLOR_FOREGROUND = "scout.color_table_cursor_foreground";
  private static final String COLOR_FOREGROUND_INACTIVE = "scout.color_table_cursor_inactive_foreground";
  private static final String COLOR_BACKGROUND = "scout.color_table_cursor_background";
  private static final String COLOR_BACKGROUND_INACTIVE = "scout.color_table_cursor_inactive_background";

  private final TableCursor m_cursor;
  private final EventListenerList m_listeners;
  private final Map<String, RGB> m_colors;
  private final NlsTableController m_controller;

  private boolean m_renaming;
  private TableTextEditor m_editingText;

  public NlsTableCursor(Table parent, NlsTableController controller) {
    m_controller = controller;
    m_cursor = new TableCursor(parent, SWT.NONE);
    m_cursor.setBackgroundMode(SWT.INHERIT_FORCE);
    m_listeners = new EventListenerList();

    m_colors = new HashMap<>(4);
    m_colors.put(COLOR_FOREGROUND, new RGB(255, 255, 255));
    m_colors.put(COLOR_BACKGROUND, new RGB(13, 58, 161));
    m_colors.put(COLOR_BACKGROUND_INACTIVE, new RGB(255, 255, 255));
    m_colors.put(COLOR_FOREGROUND_INACTIVE, new RGB(0, 0, 0));

    m_cursor.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        getCursor().setForeground(getColor(COLOR_FOREGROUND));
        getCursor().setBackground(getColor(COLOR_BACKGROUND));
      }

      @Override
      public void focusLost(FocusEvent e) {
        getCursor().setBackground(getColor(COLOR_BACKGROUND_INACTIVE));
        getCursor().setForeground(getColor(COLOR_FOREGROUND_INACTIVE));
      }
    });
    m_cursor.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDoubleClick(MouseEvent e) {
        createEditableText();
      }
    });
    m_cursor.addKeyListener(new KeyAdapter() {
      @Override
      @SuppressWarnings("squid:SwitchLastCaseIsDefaultCheck")
      public void keyPressed(KeyEvent e) {
        switch (e.stateMask) {
          case SWT.CONTROL:
          case SWT.ALT:
            return;
        }

        if (e.keyCode == SWT.F2) {
          createEditableText();
        }
        else {
          CharSequence input = new String(new char[]{e.character});
          if (getCursor().getColumn() > NlsTableController.INDEX_COLUMN_REF_COUNT && TEXT_EDIT_BEGIN_PAT.matcher(input).matches()) {
            createEditableText(String.valueOf(e.character));
          }
        }
      }
    });
    m_cursor.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        TableItem row = ((TableCursor) e.getSource()).getRow();
        ITranslationEntry entry = NlsTableController.entryOfRow(row);
        if (entry == null) {
          return;
        }

        if (entry.store().isEditable()) {
          getCursor().setForeground(getColor(COLOR_FOREGROUND));
          getCursor().setBackground(getColor(COLOR_BACKGROUND));
        }
        else {
          getCursor().setForeground(getColor(COLOR_FOREGROUND_INACTIVE));
          getCursor().setBackground(getColor(COLOR_BACKGROUND_INACTIVE));
        }
      }

      // when the user hits "ENTER" in the TableCursor, pop up a text editor so that they can change the text of the cell
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        createEditableText();
      }
    });
  }

  public Optional<NlsTableCell> getSelection() {
    TableItem row = getCursor().getRow();
    if (row == null) {
      return Optional.empty();
    }

    int column = getCursor().getColumn();
    Language lang = m_controller.languageOfColumn(column);
    return Optional.of(new NlsTableCell(column, NlsTableController.entryOfRow(row), lang));
  }

  public void setVisible(boolean visible) {
    getCursor().setVisible(visible);
  }

  public void addKeyListener(KeyListener listener) {
    getCursor().addKeyListener(listener);
  }

  public void removeKeyListener(KeyListener listener) {
    getCursor().removeKeyListener(listener);
  }

  public void addCursorListener(INlsTableCursorListener listener) {
    m_listeners.add(listener);
  }

  public void removeCursorListener(INlsTableCursorListener listener) {
    m_listeners.remove(listener);
  }

  @FunctionalInterface
  public interface INlsTableCursorListener extends EventListener {
    void textStored(NlsTableCell selection, String text);
  }

  public void ensureFocus(TableItem row, int column) {
    getCursor().setSelection(row, column);
    getCursor().setVisible(true);
    getCursor().setFocus();
  }

  public void setEditableText(int rowIndex, int column) {
    getCursor().setSelection(rowIndex, column);
    createEditableText();
  }

  private Color getColor(String id) {
    ColorRegistry colorRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
    if (!colorRegistry.hasValueFor(id)) {
      RGB rgb = m_colors.get(id);
      if (rgb != null) {
        colorRegistry.put(id, rgb);
      }
    }
    return colorRegistry.get(id);
  }

  public void createEditableText() {
    createEditableText(null);
  }

  private void createEditableText(String input) {
    Optional<NlsTableCell> selection = getSelection();
    if (!selection.isPresent()) {
      return;
    }

    NlsTableCell cell = selection.get();
    if (!cell.store().isEditable()) {
      return;
    }
    if (cell.column() == NlsTableController.INDEX_COLUMN_REF_COUNT) {
      return;
    }

    createEditableTextInternal(input);
  }

  private IStatus validateEditingText() {
    int selectedColumn = getCursor().getColumn();
    if (selectedColumn == NlsTableController.INDEX_COLUMN_KEYS) {
      return TranslationInputValidator.validateNlsKey(m_controller.stack(), m_editingText.getText());
    }
    return Status.OK_STATUS;
  }

  private String getEditableTextContent(String inputText, boolean isKeyColumn) {
    if (inputText == null) {
      if (isKeyColumn) {
        return NlsTableController.entryOfRow(getCursor().getRow()).key();
      }
      Language lang = m_controller.languageOfColumn(getCursor().getColumn());
      return NlsTableController.entryOfRow(getCursor().getRow()).translation(lang).orElse("");
    }
    return inputText;
  }

  @SuppressWarnings("pmd:NPathComplexityContentAssistContextInfo.java")
  private void createEditableTextInternal(String defaultText) {
    if (m_editingText != null && !m_editingText.isDisposed()) {
      m_editingText.dispose();
    }

    boolean isKeyColumn = getCursor().getColumn() == NlsTableController.INDEX_COLUMN_KEYS;
    String input = getEditableTextContent(defaultText, isKeyColumn);
    m_editingText = new TableTextEditor(getCursor(), isKeyColumn ? SWT.NONE : SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
    m_editingText.setText(input);
    m_editingText.addModifyListener(e -> {
      if (validateEditingText().isOK()) {
        m_editingText.setForeground(null);
      }
      else {
        m_editingText.setForeground(m_editingText.getDisplay().getSystemColor(SWT.COLOR_RED));
      }
    });
    if (defaultText != null) {
      m_editingText.setSelection(input.length());
    }
    else {
      m_editingText.setSelection(0, input.length());
    }
    m_editingText.addKeyListener(new KeyAdapter() {
      private boolean m_altPressed;

      @Override
      public void keyReleased(KeyEvent e) {
        if (e.keyCode == SWT.ALT) {
          m_altPressed = false;
        }
      }

      @Override
      @SuppressWarnings("squid:SwitchLastCaseIsDefaultCheck")
      public void keyPressed(KeyEvent e) {
        switch (e.keyCode) {
          case SWT.ESC:
            disposeText();
            break;
          case SWT.ALT:
            m_altPressed = true;
            break;
          case SWT.CR:
            if (m_altPressed) {
              if (getCursor().getColumn() != NlsTableController.INDEX_COLUMN_KEYS) {
                addNewLine();
              }
            }
            else {
              storeText();
            }
            break;
        }
      }

      private void addNewLine() {
        if (m_editingText == null || m_editingText.isDisposed()) {
          return;
        }
        m_editingText.insertText("\n");
      }
    });
    m_editingText.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        if (m_renaming) {
          return;
        }
        storeText();
      }
    });
    m_editingText.open();
    m_editingText.setFocus();
  }

  private void disposeText() {
    m_editingText.dispose();
    m_editingText = null;
  }

  private void storeText() {
    if (m_editingText == null || m_editingText.isDisposed()) {
      return;
    }

    if (!validateEditingText().isOK()) {
      disposeText();
      return;
    }

    m_renaming = true;
    try {
      String text = m_editingText.getText();
      NlsTableCell selection = getSelection().get();
      for (INlsTableCursorListener listener : m_listeners.get(INlsTableCursorListener.class)) {
        listener.textStored(selection, text);
      }
    }
    finally {
      disposeText();
      m_renaming = false;
    }
  }

  public TableCursor getCursor() {
    return m_cursor;
  }
}
