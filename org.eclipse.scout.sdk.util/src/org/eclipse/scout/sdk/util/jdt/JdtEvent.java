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
package org.eclipse.scout.sdk.util.jdt;

import java.util.EventObject;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.type.TypeUtility;

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

  @SuppressWarnings("null")
  public ITypeHierarchy getSuperTypeHierarchy() {
    if (m_superTypeHierarchy == null) {
      if (TypeUtility.exists(getElement())) {
        IType type = null;
        if (getElement().getElementType() == IJavaElement.TYPE) {
          type = (IType) getElement();
        }
        else if (getElement().getElementType() == IJavaElement.COMPILATION_UNIT) {
          try {
            IType[] types = ((ICompilationUnit) getElement()).getTypes();
            if (types.length > 0) {
              type = types[0];
            }
          }
          catch (JavaModelException ex) {
            SdkUtilActivator.logError(ex);
          }
        }
        if (TypeUtility.exists(type)) {
          try {
            m_superTypeHierarchy = type.newSupertypeHierarchy(null);
          }
          catch (JavaModelException e) {
            SdkUtilActivator.logError("could not create super type hierarchy for '" + getElement().getElementName() + "'.");
          }
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

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("JdtEvent <");
    switch (getEventType()) {
      case ADDED:
        builder.append("ADDED ");
        break;
      case CHANGED:
        builder.append("CHANGED ");
        break;
      case REMOVED:
        builder.append("REMOVED ");
        break;
    }
    builder.append(getElementType(getElement()) + " ");
    if (getElement() != null) {
      builder.append("'" + getElement().getElementName() + "' ");
    }
    else {
      builder.append("'NULL ELEMENT' ");
    }
    if (getDeclaringType() != null) {
      builder.append("declaringType='" + getDeclaringType().getElementName() + "' ");
    }
    else {
      builder.append("declaringType='NULL' ");
    }
    builder.append(">");
    return builder.toString();
  }

  private String getElementType(IJavaElement element) {
    switch (element.getElementType()) {
      case IJavaElement.JAVA_MODEL:
        return "JAVA_MODEL ";
      case IJavaElement.JAVA_PROJECT:
        return "JAVA_PROJECT ";
      case IJavaElement.PACKAGE_FRAGMENT_ROOT:
        return "PACKAGE_FRAGMENT_ROOT ";
      case IJavaElement.PACKAGE_FRAGMENT:
        return "PACKAGE_FRAGMENT ";
      case IJavaElement.COMPILATION_UNIT:
        return "COMPILATION_UNIT ";
      case IJavaElement.CLASS_FILE:
        return "CLASS_FILE ";
      case IJavaElement.TYPE:
        return "TYPE ";
      case IJavaElement.FIELD:
        return "FIELD ";
      case IJavaElement.METHOD:
        return "METHOD ";
      case IJavaElement.INITIALIZER:
        return "INITIALIZER ";
      case IJavaElement.PACKAGE_DECLARATION:
        return "PACKAGE_DECLARATION ";
      case IJavaElement.IMPORT_CONTAINER:
        return "IMPORT_CONTAINER ";
      case IJavaElement.IMPORT_DECLARATION:
        return "IMPORT_DECLARATION ";
      case IJavaElement.LOCAL_VARIABLE:
        return "LOCAL_VARIABLE ";
      case IJavaElement.TYPE_PARAMETER:
        return "TYPE_PARAMETER ";
      case IJavaElement.ANNOTATION:
        return "ANNOTATION ";
      default:
        return "???";
    }
  }
}
