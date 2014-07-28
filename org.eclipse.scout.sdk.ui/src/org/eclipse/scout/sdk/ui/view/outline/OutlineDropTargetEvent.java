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
package org.eclipse.scout.sdk.ui.view.outline;

import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Event;

public class OutlineDropTargetEvent extends Event {
  private TransferData m_transferData;
  private Object m_transferObject;
  private Object m_selectedObject;
  private Object m_currentTarget;
  private int m_currentLocation;
  private int m_operation;

  public OutlineDropTargetEvent() {
  }

  public void setSelectedObject(Object selectedObject) {
    m_selectedObject = selectedObject;
  }

  public Object getSelectedObject() {
    return m_selectedObject;
  }

  public void setCurrentTarget(Object currentTarget) {
    m_currentTarget = currentTarget;
  }

  public Object getCurrentTarget() {
    return m_currentTarget;
  }

  public void setCurrentLocation(int currentLocation) {
    m_currentLocation = currentLocation;
  }

  public int getCurrentLocation() {
    return m_currentLocation;
  }

  public void setOperation(int operation) {
    m_operation = operation;
  }

  public int getOperation() {
    return m_operation;
  }

  public TransferData getTransferData() {
    return m_transferData;
  }

  public void setTransferData(TransferData transferData) {
    m_transferData = transferData;
  }

  public Object getTransferObject() {
    return m_transferObject;
  }

  public void setTransferObject(Object transferObject) {
    m_transferObject = transferObject;
  }
}
