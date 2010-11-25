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
package org.eclipse.scout.sdk.jdt;

import java.util.EventObject;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

/**
 * <h3>{@link JdtEvent}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 24.11.2010
 */
public class JdtEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  public static final int ADDED = IJavaElementDelta.ADDED;
  public static final int REMOVED = IJavaElementDelta.REMOVED;
  public static final int CHANGED = IJavaElementDelta.CHANGED;
  /**
   * event type indicating any working copy is created and is not anymore sync with the underlying resource.
   * Note that the {@link JdtEvent#BUFFER_SYNC} event is alternately with the {@link JdtEvent#BUFFER_DIRTY}.
   */
  public static final int BUFFER_DIRTY = 10;
  /**
   * event type indicating any working copy is sync with the underlying resource.
   * Note that the {@link JdtEvent#BUFFER_SYNC} event is alternately with the {@link JdtEvent#BUFFER_DIRTY}.
   */
  public static final int BUFFER_SYNC = 11;

  /**
   * one of {@link JdtEvent#ADDED}, {@link JdtEvent#REMOVED}, {@link JdtEvent#CHANGED}, {@link JdtEvent#BUFFER_DIRTY},
   * {@link JdtEvent#BUFFER_SYNC}
   */
  private final int m_eventType;
  private final IJavaElement m_element;
  private IType m_declaringType;

  private ITypeHierarchy m_superTypeHierarchy;

  public JdtEvent(Object source, int eventType, IJavaElement element) {
    super(source);
    m_eventType = eventType;
    m_element = element;
  }

  public int getEventType() {
    return m_eventType;
  }

  public IJavaElement getElement() {
    return m_element;
  }

  public int getElementType() {
    if (getElement() == null) {
      return -1;
    }
    return m_element.getElementType();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof JdtEvent)) {
      return false;
    }
    JdtEvent event = (JdtEvent) obj;
    if (!CompareUtility.equals(event.getElement(), m_element)) {
      return false;
    }
    if (event.getEventType() != m_eventType) {
      return false;
    }
    if (!CompareUtility.equals(event.getSource(), getSource())) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = m_eventType;
    if (getSource() != null) {
      hash ^= getSource().hashCode();
    }
    if (getElement() != null) {
      hash ^= getElement().hashCode();
    }
    return hash;
  }

  public ITypeHierarchy getSuperTypeHierarchy() {
    if (m_superTypeHierarchy == null) {
      if (TypeUtility.exists(getElement()) && getElement().getElementType() == IJavaElement.TYPE) {
        try {
          m_superTypeHierarchy = ((IType) getElement()).newSupertypeHierarchy(null);
        }
        catch (JavaModelException e) {
          ScoutSdk.logError("could not create super type hierarchy for '" + getElement().getElementName() + "'.");
        }
      }
    }
    return m_superTypeHierarchy;
  }

  public IType getDeclaringType() {
    if (m_declaringType == null) {
      if (getElement() != null) {
        if (getElementType() == IJavaElement.TYPE) {
          m_declaringType = ((IType) getElement()).getDeclaringType();
        }
        else {
          m_declaringType = (IType) getElement().getAncestor(IJavaElement.TYPE);
        }
      }
    }
    return m_declaringType;
  }
}
