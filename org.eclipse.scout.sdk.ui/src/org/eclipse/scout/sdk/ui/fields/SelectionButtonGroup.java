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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/***
 * Vertical Button group aligned to the default label positions.
 * 
 * @param <T>
 *          Value type associated with a button.
 */
public class SelectionButtonGroup<T> extends Composite {

  /**
   * Creates radio buttons
   */
  public final static int BUTTON_TYPE_RADIO = SWT.RADIO;

  /**
   * Creates check boxes
   */
  public final static int BUTTON_TYPE_CHECK = SWT.CHECK;

  private final HashMap<T, Button> m_buttons;
  private final int m_type;
  private final HashSet<T> m_selectedValues;
  private final HashSet<ModifyListener> m_modifyListeners;
  private Button m_lastButton;

  /**
   * @param parent
   *          the parent composite.
   * @param type
   *          one of the <code>SelectionButtonGroup</code> types
   * @see SelectionButtonGroup#BUTTON_TYPE_RADIO
   * @see SelectionButtonGroup#BUTTON_TYPE_CHECK
   */
  public SelectionButtonGroup(Composite parent, int type) {
    super(parent, SWT.NONE);
    if (type != BUTTON_TYPE_RADIO && type != BUTTON_TYPE_CHECK) throw new IllegalArgumentException("selection button type not valid");
    m_buttons = new HashMap<T, Button>();
    m_selectedValues = new HashSet<T>();
    m_modifyListeners = new HashSet<ModifyListener>();
    m_type = type;
    setLayout(new FormLayout());
  }

  /**
   * Adds the given listener to the notification list.
   * 
   * @param modifyListener
   */
  public void addModifyListener(ModifyListener modifyListener) {
    m_modifyListeners.add(modifyListener);
  }

  /**
   * Removes the given listener from the notification list.
   * 
   * @param listener
   */
  public void removeModifyListener(ModifyListener listener) {
    m_modifyListeners.remove(listener);
  }

  /**
   * adds a new button with given label and given value to the list.
   * 
   * @param label
   *          The label of the button.
   * @param value
   *          the value this button should represent. should be unique.
   * @return The created button instance.
   */
  public Button addButton(final String label, final T value) {
    final Button newButton = new Button(this, m_type);
    newButton.setText(label);
    newButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        if (newButton.getSelection()) {
          if (m_selectedValues.add(value)) {
            fireModify(newButton);
          }
        }
        else {
          if (m_selectedValues.remove(value)) {
            if (m_type == BUTTON_TYPE_CHECK) {
              fireModify(newButton);
            }
          }
        }
      }
    });

    // layout
    FormData formData = new FormData();
    if (m_lastButton != null) {
      formData.top = new FormAttachment(m_lastButton, 10);
    }
    formData.left = new FormAttachment(40, 5);
    newButton.setLayoutData(formData);

    // save instance
    m_lastButton = newButton;
    m_buttons.put(value, newButton);
    return newButton;
  }

  /**
   * Selects the button (if existent) that was created with the given value.
   * 
   * @param val
   *          the button value.
   */
  public void setValue(T val) {
    Button b = m_buttons.get(val);
    if (b != null) {
      b.setSelection(true);

      Event e = new Event();
      e.type = SWT.Selection;
      e.display = b.getDisplay();
      e.item = b;
      e.widget = this;

      for (Listener l : b.getListeners(SWT.Selection)) {
        l.handleEvent(e);
      }
      return;
    }
  }

  /**
   * Gets the first selected value. Convenience method for radio button style.
   * 
   * @return The first value of all selected values.
   */
  public T getValue() {
    if (m_selectedValues.size() > 0) {
      return m_selectedValues.iterator().next();
    }
    return null;
  }

  /**
   * Gets all selected values.
   * 
   * @return the selected values.
   */
  public Set<T> getValues() {
    return m_selectedValues;
  }

  private void fireModify(Button b) {
    Event e = new Event();
    e.type = SWT.Modify;
    e.display = b.getDisplay();
    e.item = b;
    e.widget = this;

    for (ModifyListener l : m_modifyListeners) {
      try {
        l.modifyText(new ModifyEvent(e));
      }
      catch (Throwable t) {
      }
    }
  }
}
