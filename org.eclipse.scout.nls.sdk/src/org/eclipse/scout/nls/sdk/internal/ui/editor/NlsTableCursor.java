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
package org.eclipse.scout.nls.sdk.internal.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.model.workspace.InheritedNlsEntry;
import org.eclipse.scout.nls.sdk.internal.ui.TableTextEditor;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.NlsEntry;
import org.eclipse.scout.nls.sdk.ui.INlsTableCursorManangerListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class NlsTableCursor {

  private Table m_table;
  private TableCursor m_cursor;
  private ControlEditor m_editor;
  private NlsTableInputValidator m_inputValidator;
  private List<INlsTableCursorManangerListener> m_listeners = new ArrayList<INlsTableCursorManangerListener>();
  private boolean m_renaming;

  public NlsTableCursor(Table table) {

    m_table = table;
    m_cursor = new TableCursor(m_table, SWT.NONE);
    m_cursor.setBackgroundMode(SWT.INHERIT_FORCE);
    m_cursor.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        m_cursor.setForeground(NlsCore.getColor(NlsCore.COLOR_TABLE_CURSOR_FOREGROUND));
        m_cursor.setBackground(NlsCore.getColor(NlsCore.COLOR_TABLE_CURSOR_BACKGROUND));
      }

      @Override
      public void focusLost(FocusEvent e) {
        m_cursor.setBackground(NlsCore.getColor(NlsCore.COLOR_TABLE_CURSOR_INACTIVE_BACKGROUND));
        m_cursor.setForeground(NlsCore.getColor(NlsCore.COLOR_TABLE_CURSOR_INACTIVE_FOREGROUND));
      }

    });

    m_editor = new ControlEditor(m_cursor);
    m_editor.grabHorizontal = true;
    m_editor.grabVertical = true;
    m_cursor.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseDoubleClick(MouseEvent e) {
        createEditableText();
      }
    });
    m_cursor.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        switch (e.stateMask) {
          case SWT.CONTROL:
                  case SWT.ALT:
                    return;
                }
                String input = new String(new char[]{e.character});
                String pattern = "[a-zA-Z0-9]";
                if (input.matches(pattern) && m_cursor.getColumn() > 1) {

                  createEditableText(new String(new char[]{e.character}));
                }
              }
    });

    m_cursor.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {

        Object rowData = ((TableCursor) e.getSource()).getRow().getData();
        if (rowData == null) {
          return;
        }
        if (rowData instanceof InheritedNlsEntry) {

          m_cursor.setForeground(NlsCore.getColor(NlsCore.COLOR_NLS_ROW_INACTIVE_FOREGROUND));

          m_cursor.setBackground(NlsCore.getColor(NlsCore.COLOR_TABLE_CURSOR_INACTIVE_BACKGROUND));
        }
        else {
          m_cursor.setForeground(NlsCore.getColor(NlsCore.COLOR_TABLE_CURSOR_FOREGROUND));
          m_cursor.setBackground(NlsCore.getColor(NlsCore.COLOR_TABLE_CURSOR_BACKGROUND));
        }
      }

      // when the user hits "ENTER" in the TableCursor, pop up a text
      // editor so that
      // they can change the text of the cell
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        createEditableText();
      }

    });

  }

  private void showInheritedWarning() {
    // TODO handle warning
  }

  /**
   * @return
   */
  public TableCursor getCursor() {
    return m_cursor;
  }

  /**
   * @param validator
   */
  public void setInputValidator(NlsTableInputValidator validator) {
    m_inputValidator = validator;

  }

  public NlsTableSelection getSelection() {
    NlsEntry row = (NlsEntry) m_cursor.getRow().getData();
    if (row == null) {
      return null;
    }
    else {
      return new NlsTableSelection(row, m_cursor.getColumn());
    }
  }

  /**
   * @param keyAdapter
   */
  public void addKeyListener(KeyListener listener) {
    m_cursor.addKeyListener(listener);
  }

  public void removeKeyListener(KeyListener listener) {
    m_cursor.removeKeyListener(listener);
  }

  public void addCursorMangerListener(INlsTableCursorManangerListener listener) {
    m_listeners.add(listener);
  }

  public void removeCursorMangerListener(INlsTableCursorManangerListener listener) {
    m_listeners.remove(listener);
  }

  /**
   * @param row
   */
  public void ensureFocus(TableItem row) {
    m_cursor.setSelection(row, NlsTable.INDEX_COLUMN_KEYS);
    m_cursor.setVisible(true);
    m_cursor.setFocus();

  }

  /**
   * @param rowIndex
   * @param column
   */
  public void setEditableText(int rowIndex, int column) {
    m_cursor.setSelection(rowIndex, column);
    createEditableText();
  }

  public void createEditableText() {
    createEditableText(null);
  }

  public void createEditableText(String input) {
    if (m_cursor.getRow().getData() instanceof InheritedNlsEntry) {
      showInheritedWarning();
      return;
    }
    // if (m_cursor.getColumn() < NlsTable.INDEX_COLUMN_KEYS + 1) {
    // return;
    // }
    createEditableTextInternal(input);
  }

  private TableTextEditor m_editingText;

  private void createEditableTextInternal(String defaultText) {
    if(m_editingText != null && ! m_editingText.isDisposed()){
      m_editingText.dispose();
    }
    m_editingText = new TableTextEditor(m_cursor);
    String input = m_cursor.getRow().getText(m_cursor.getColumn());
    if (defaultText != null) {
      input = defaultText;
    }
    m_editingText.setText(input);
    m_editingText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        if (m_inputValidator.validate(m_editingText.getText(), m_cursor.getColumn()).isOK()) {
          m_editingText.setForeground(null);
        }
        else {
          m_editingText.setForeground(m_editingText.getDisplay().getSystemColor(SWT.COLOR_RED));
        }
      }
    });
    if (defaultText != null) {
      m_editingText.setSelection(input.length());
    }
    else {
      m_editingText.setSelection(0, input.length());
    }
    m_editingText.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        switch (e.character) {
          case SWT.ESC:
                    disposeText();
                    break;
                  case SWT.CR:
                    storeText();

                  default:
                    break;
                }
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
    m_editor.setEditor(m_editingText);
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
    // TableItem row = m_cursor.getRow();
    final int column = m_cursor.getColumn();
    if (!m_inputValidator.validate(m_editingText.getText(), column).isOK()) {
      disposeText();
      return;
    }
    if (m_editingText.getText().equals(m_editingText)) {
      disposeText();
      return;
    }
    m_renaming = true;
    try {

      INlsEntry row = (NlsEntry) m_cursor.getRow().getData();
      for (INlsTableCursorManangerListener listener : m_listeners) {
        listener.textChangend(row, m_cursor.getColumn(), m_editingText.getText());
      }
      m_cursor.getRow().setText(m_cursor.getColumn(), m_editingText.getText());
    }
    finally {

      disposeText();
      m_renaming = false;
    }
  }

}
