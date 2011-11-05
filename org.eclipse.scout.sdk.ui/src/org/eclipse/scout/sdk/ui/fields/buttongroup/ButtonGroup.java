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
package org.eclipse.scout.sdk.ui.fields.buttongroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/***
 * Vertical Button group aligned to the default label positions.
 * 
 * @param <T>
 *          Value type associated with a button.
 */
public class ButtonGroup<T> extends Composite {

  /**
   * Creates radio buttons
   */
  public final static int BUTTON_TYPE_RADIO = SWT.RADIO;

  /**
   * Creates check boxes
   */
  public final static int BUTTON_TYPE_CHECK = SWT.CHECK;

  private final static String VALUE = "value";

  private List<Button> m_buttons;
  private final int m_type;
  private final EventListenerList m_eventListeners;
  private final P_ButtonSelectionListener m_internalListener = new P_ButtonSelectionListener();
  private final OptimisticLock m_notificationLock = new OptimisticLock();

  /**
   * @param parent
   *          the parent composite.
   * @param type
   *          one of the <code>SelectionButtonGroup</code> types
   * @see ButtonGroup#BUTTON_TYPE_RADIO
   * @see ButtonGroup#BUTTON_TYPE_CHECK
   */
  public ButtonGroup(Composite parent, int type) {
    super(parent, SWT.NONE);
    if (type != BUTTON_TYPE_RADIO && type != BUTTON_TYPE_CHECK) throw new IllegalArgumentException("selection button type not valid");
    m_eventListeners = new EventListenerList();
    m_buttons = new ArrayList<Button>();
    m_type = type;
    setLayout(new GridLayout(1, true));
  }

  public void addButtonGroupListener(ButtonGroupListener<T> listener) {
    m_eventListeners.add(ButtonGroupListener.class, listener);
  }

  public void removeButtonGroupListener(ButtonGroupListener<T> listener) {
    m_eventListeners.remove(ButtonGroupListener.class, listener);
  }

  public int getType() {
    return m_type;
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
  public Button createButton(final String label, final T value) {
    final Button newButton = new Button(this, m_type);
    newButton.setData(VALUE, value);
    newButton.setText(label);
    newButton.addSelectionListener(m_internalListener);
    m_buttons.add(newButton);

    // layout
    GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
    data.horizontalIndent = 40;

    newButton.setLayoutData(data);
    return newButton;
  }

  /**
   * Selects the button (if existent) that was created with the given value.
   * 
   * @param val
   *          the button value.
   */
  public void setValue(T... val) {
    HashSet<T> values = new HashSet<T>();
    if (val != null) {
      values.addAll(Arrays.asList(val));
    }
    if (getType() == BUTTON_TYPE_RADIO && values.size() > 1) {
      throw new IllegalArgumentException("BUTTON_TYPE_RADIO allows only one value to select.");
    }
    boolean dirty = false;
    try {
      m_notificationLock.acquire();
      for (Button b : m_buttons) {
        if (values.contains(b.getData(VALUE))) {
          if (!b.getSelection()) {
            b.setSelection(true);
            dirty = true;
          }
        }
        else {
          if (b.getSelection()) {
            b.setSelection(false);
            dirty = true;
          }
        }
      }
    }
    finally {
      m_notificationLock.release();
    }
    if (dirty) {
      fireSelectionChanged();
    }

  }

  /**
   * Gets the first selected value. Convenience method for radio button style.
   * 
   * @return The first value of all selected values.
   */
  public T getValue() {
    List<T> values = getValues();
    if (values.size() == 1) {
      return values.get(0);
    }
    else if (values.size() == 0) {
      return null;
    }
    else {
      ScoutSdkUi.logWarning("get selected value of a radio button group. Should use the method getValues().");
      return values.get(0);
    }
  }

  /**
   * Gets all selected values.
   * 
   * @return the selected values.
   */
  @SuppressWarnings("unchecked")
  public List<T> getValues() {
    ArrayList<T> selectedValues = new ArrayList<T>();
    for (Button b : m_buttons) {
      if (b.getSelection()) {
        selectedValues.add((T) b.getData(VALUE));
      }
    }
    return Collections.unmodifiableList(selectedValues);
  }

  /**
   *
   */
  private void fireSelectionChanged() {
    List<T> selection = getValues();
    for (ButtonGroupListener<T> l : m_eventListeners.getListeners(ButtonGroupListener.class)) {
      try {
        l.handleSelectionChanged(selection);
      }
      catch (Exception e) {
        ScoutSdkUi.logError("error during listener notification.", e);
      }
    }
  }

  private class P_ButtonSelectionListener extends SelectionAdapter {

    /**
     * to ensure an change event of type radio gets only fired once.
     */
    private int m_lastEvent = 0;

    @Override
    public void widgetSelected(SelectionEvent e) {
      try {
        if (m_notificationLock.acquire() && e.time != m_lastEvent) {
          m_lastEvent = e.time;
          fireSelectionChanged();
        }
      }
      finally {
        m_notificationLock.release();
      }
    }
  } // end class P_ButtonSelectionListener

}
