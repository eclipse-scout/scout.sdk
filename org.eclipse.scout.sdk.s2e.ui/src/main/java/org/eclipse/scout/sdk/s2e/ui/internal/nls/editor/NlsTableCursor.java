/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor;

import static org.eclipse.scout.sdk.core.s.nls.TranslationValidator.isForbidden;
import static org.eclipse.scout.sdk.core.s.nls.TranslationValidator.validateKey;
import static org.eclipse.scout.sdk.s2e.ui.internal.nls.editor.NlsTableController.translationOfRow;

import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.core.s.nls.Language;
import org.eclipse.scout.sdk.core.s.nls.TranslationValidator;
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

public class NlsTableCursor {

  private static final Pattern TEXT_EDIT_BEGIN_PAT = Pattern.compile("[a-zA-Z\\d]");
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
          var input = String.valueOf(e.character);
          if (getCursor().getColumn() > NlsTableController.INDEX_COLUMN_REF_COUNT && TEXT_EDIT_BEGIN_PAT.matcher(input).matches()) {
            createEditableText(input);
          }
        }
      }
    });
    m_cursor.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        var cursor = (TableCursor) e.getSource();
        var row = cursor.getRow();
        var entry = translationOfRow(row);
        if (entry == null) {
          return;
        }

        var col = cursor.getColumn();
        var editable = col == NlsTableController.INDEX_COLUMN_KEYS ? entry.hasOnlyEditableStores() : entry.hasEditableStores();
        if (editable) {
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
    var row = getCursor().getRow();
    if (row == null) {
      return Optional.empty();
    }

    var column = getCursor().getColumn();
    var lang = m_controller.languageOfColumn(column);
    return Optional.of(new NlsTableCell(column, translationOfRow(row), lang));
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

  public void addCursorListener(@SuppressWarnings("TypeMayBeWeakened") INlsTableCursorListener listener) {
    m_listeners.add(listener);
  }

  public void removeCursorListener(@SuppressWarnings("TypeMayBeWeakened") INlsTableCursorListener listener) {
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
    var colorRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
    if (!colorRegistry.hasValueFor(id)) {
      var rgb = m_colors.get(id);
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
    var selection = getSelection();
    if (selection.isEmpty()) {
      return;
    }

    var cell = selection.orElseThrow();
    var isEditable = cell.column() == NlsTableController.INDEX_COLUMN_KEYS ? cell.translation().hasOnlyEditableStores() : cell.translation().hasEditableStores();
    if (!isEditable) {
      return;
    }
    if (cell.column() == NlsTableController.INDEX_COLUMN_REF_COUNT) {
      return;
    }

    createEditableTextInternal(input);
  }

  private IStatus validateEditingText() {
    var selectedColumn = getCursor().getColumn();
    var editingText = m_editingText.getText();
    if (selectedColumn == NlsTableController.INDEX_COLUMN_KEYS) {
      var validationResult = getSelection().orElseThrow().translation().stores()
          .mapToInt(store -> validateKey(m_controller.translationManager(), store, editingText, Collections.singleton(editingText)))
          .max().orElse(TranslationValidator.OK);
      return TranslationInputValidator.toStatus(validationResult);
    }
    var isDefaultLang = getSelection()
        .flatMap(NlsTableCell::language)
        .filter(Language.LANGUAGE_DEFAULT::equals)
        .isPresent();
    if (isDefaultLang) {
      return TranslationInputValidator.validateDefaultTranslation(editingText);
    }
    return Status.OK_STATUS;
  }

  private String getEditableTextContent(String inputText, boolean isKeyColumn) {
    if (inputText == null) {
      if (isKeyColumn) {
        return translationOfRow(getCursor().getRow()).key();
      }
      var lang = m_controller.languageOfColumn(getCursor().getColumn());
      return translationOfRow(getCursor().getRow()).text(lang).orElse("");
    }
    return inputText;
  }

  @SuppressWarnings("pmd:NPathComplexity")
  private void createEditableTextInternal(String defaultText) {
    if (m_editingText != null && !m_editingText.isDisposed()) {
      m_editingText.dispose();
    }

    var isKeyColumn = getCursor().getColumn() == NlsTableController.INDEX_COLUMN_KEYS;
    var input = getEditableTextContent(defaultText, isKeyColumn);
    m_editingText = new TableTextEditor(getCursor(), isKeyColumn ? SWT.NONE : SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
    m_editingText.setText(input);
    m_editingText.addModifyListener(e -> m_editingText.setErrorStatus(validateEditingText()));
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
        //noinspection HardcodedLineSeparator
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

    if (isForbidden(validateEditingText().getCode())) {
      disposeText();
      return;
    }

    m_renaming = true;
    try {
      var text = m_editingText.getText();
      var selection = getSelection().orElseThrow();
      for (var listener : m_listeners.get(INlsTableCursorListener.class)) {
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
