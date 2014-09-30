/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.util.typecache;

import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 * <h3>{@link TypeHierarchyConstraints}</h3> Specifies constraints for creating a project context type
 * hierarchy.
 *
 * @see IHierarchyCache#getProjectContextTypeHierarchy(TypeHierarchyConstraints)
 * @author Matthias Villiger
 * @since 4.0.0 13.05.2014
 */
public class TypeHierarchyConstraints {
  private final IType m_baseType;
  private IJavaProject m_classpath;
  private boolean m_includeBaseType;
  private int m_searchFor;
  private int m_setFlags;
  private int m_notSetFlags;

  private static final String NOT_NULL_MSG = "hierarchy base type may not be null!";

  /**
   * @param baseType
   *          The base type of the hierarchy result
   * @param classpath
   *          The project classpath context to search in
   */
  public TypeHierarchyConstraints(IType baseType, IJavaProject classpath) {
    if (!TypeUtility.exists(baseType)) {
      throw new IllegalArgumentException(NOT_NULL_MSG);
    }
    m_baseType = baseType;
    m_classpath = classpath;
    m_includeBaseType = true;
    m_searchFor = IJavaSearchConstants.TYPE;
    m_setFlags = 0;
    m_notSetFlags = 0;
  }

  /**
   * Specifies a flag filter. Only types that have all these flags are part of the {@link ITypeHierarchyResult}.
   *
   * @param mods
   *          The modifiers that the {@link IType}s must have.
   * @return this
   * @see Flags
   */
  public TypeHierarchyConstraints modifiersSet(int... mods) {
    if (mods != null && mods.length > 0) {
      for (int i : mods) {
        m_setFlags |= i;
      }
    }
    return this;
  }

  /**
   * Specifies a flag filter. Only types that have NONE of these flags are part of the {@link ITypeHierarchyResult}.
   *
   * @param mods
   *          The modifiers that the {@link IType}s may not have.
   * @return this
   * @see Flags
   */
  public TypeHierarchyConstraints modifiersNotSet(int... mods) {
    if (mods != null && mods.length > 0) {
      for (int i : mods) {
        m_notSetFlags |= i;
      }
    }
    return this;
  }

  /**
   * Checks if the given modifiers are accepted by this constraints.
   *
   * @param modifiers
   *          The modifiers
   * @return true if the given modifiers match the modifier constraints defined in this instance.
   * @see IType#getFlags()
   * @see Flags
   */
  public boolean modifiersAccepted(int modifiers) {
    return ((modifiers & m_setFlags) == m_setFlags || m_setFlags == 0)
        && ((modifiers & m_notSetFlags) == 0 || m_notSetFlags == 0);
  }

  /**
   * Gets if the given type fulfills the constraints defined by this instance.
   *
   * @param candidate
   *          The candidate to check if it fulfills the {@link ITypeHierarchyResult} constraints.
   * @param candidateSuperTypes
   *          All super types of the given candidate.
   * @return true if the given type fulfills the constraints of this {@link ITypeHierarchyResult} and would therefore be
   *         part of the hierarchy. False otherwise.
   */
  public boolean isTypeAccepted(IType t, Set<IType> superTypes) {
    if (!superTypes.contains(getBaseType())) {
      return false;
    }

    if (getClasspath() != null && !TypeUtility.isOnClasspath(t, getClasspath())) {
      return false;
    }

    if (getBaseType().equals(t) && !isIncludeBaseType()) {
      return false;
    }

    try {
      switch (getSearchFor()) {
        case IJavaSearchConstants.CLASS:
          if (!t.isClass()) {
            return false;
          }
        case IJavaSearchConstants.CLASS_AND_INTERFACE:
          if (!t.isClass() && !t.isInterface()) {
            return false;
          }
        case IJavaSearchConstants.CLASS_AND_ENUM:
          if (!t.isClass() && !t.isEnum()) {
            return false;
          }
        case IJavaSearchConstants.INTERFACE:
          if (!t.isInterface()) {
            return false;
          }
        case IJavaSearchConstants.INTERFACE_AND_ANNOTATION:
          if (!t.isInterface() && !t.isAnnotation()) {
            return false;
          }
        case IJavaSearchConstants.ENUM:
          if (!t.isEnum()) {
            return false;
          }
        case IJavaSearchConstants.ANNOTATION_TYPE:
          if (!t.isAnnotation()) {
            return false;
          }
      }

      if (!modifiersAccepted(t.getFlags())) {
        return false;
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logError("Error while checking for acceptance in hierarchy of base type '" + getBaseType().getFullyQualifiedName() + "'.", e);
      return false;
    }

    return true; // all constraints fulfilled
  }

  /**
   * @return Gets the base type of the hierarchy constraints.
   */
  public IType getBaseType() {
    return m_baseType;
  }

  /**
   * @return Gets the classpath constraint.
   */
  public IJavaProject getClasspath() {
    return m_classpath;
  }

  /**
   * Sets a new classpath constraint.
   */
  public TypeHierarchyConstraints setClasspath(IJavaProject classpath) {
    m_classpath = classpath;
    return this;
  }

  /**
   * @return Gets Specifies if the hierarchy base type should be included in the result or not.
   */
  public boolean isIncludeBaseType() {
    return m_includeBaseType;
  }

  /**
   * Specifies if the hierarchy base type should be included in the result.
   */
  public TypeHierarchyConstraints setIncludeBaseType(boolean includeBaseType) {
    m_includeBaseType = includeBaseType;
    return this;
  }

  /**
   * gets the nature of the searched elements.
   *
   * @return One of
   *         <ul>
   *         <li>{@link IJavaSearchConstants#CLASS}: only look for classes</li>
   *         <li>{@link IJavaSearchConstants#INTERFACE}: only look for interfaces</li>
   *         <li>{@link IJavaSearchConstants#ENUM}: only look for enumeration</li>
   *         <li>{@link IJavaSearchConstants#ANNOTATION_TYPE}: only look for annotation type</li>
   *         <li>{@link IJavaSearchConstants#CLASS_AND_ENUM}: only look for classes and enumerations</li>
   *         <li>{@link IJavaSearchConstants#CLASS_AND_INTERFACE}: only look for classes and interfaces</li>
   *         <li>{@link IJavaSearchConstants#TYPE}: look for all types (i.e. classes, interfaces, enum and annotation
   *         types)</li>
   *         </ul>
   */
  public int getSearchFor() {
    return m_searchFor;
  }

  /**
   * Sets the nature of the elements to search.
   *
   * @param searchFor
   *          One of
   *          <ul>
   *          <li>{@link IJavaSearchConstants#CLASS}: only look for classes</li>
   *          <li>{@link IJavaSearchConstants#INTERFACE}: only look for interfaces</li>
   *          <li>{@link IJavaSearchConstants#ENUM}: only look for enumeration</li>
   *          <li>{@link IJavaSearchConstants#ANNOTATION_TYPE}: only look for annotation type</li>
   *          <li>{@link IJavaSearchConstants#CLASS_AND_ENUM}: only look for classes and enumerations</li>
   *          <li>{@link IJavaSearchConstants#CLASS_AND_INTERFACE}: only look for classes and interfaces</li>
   *          <li>{@link IJavaSearchConstants#TYPE}: look for all types (i.e. classes, interfaces, enum and annotation
   *          types)</li>
   *          </ul>
   * @return this
   */
  public TypeHierarchyConstraints setSearchFor(int searchFor) {
    m_searchFor = searchFor;
    return this;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_baseType == null) ? 0 : m_baseType.hashCode());
    result = prime * result + ((m_classpath == null) ? 0 : m_classpath.hashCode());
    result = prime * result + (m_includeBaseType ? 1231 : 1237);
    result = prime * result + m_notSetFlags;
    result = prime * result + m_searchFor;
    result = prime * result + m_setFlags;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof TypeHierarchyConstraints)) {
      return false;
    }
    TypeHierarchyConstraints other = (TypeHierarchyConstraints) obj;
    if (m_baseType == null) {
      if (other.m_baseType != null) {
        return false;
      }
    }
    else if (!m_baseType.equals(other.m_baseType)) {
      return false;
    }
    if (m_classpath == null) {
      if (other.m_classpath != null) {
        return false;
      }
    }
    else if (!m_classpath.equals(other.m_classpath)) {
      return false;
    }
    if (m_includeBaseType != other.m_includeBaseType) {
      return false;
    }
    if (m_notSetFlags != other.m_notSetFlags) {
      return false;
    }
    if (m_searchFor != other.m_searchFor) {
      return false;
    }
    if (m_setFlags != other.m_setFlags) {
      return false;
    }
    return true;
  }
}
