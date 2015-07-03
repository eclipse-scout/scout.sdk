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
package org.eclipse.scout.sdk.core.model;

import org.eclipse.scout.sdk.core.util.CoreUtils;

/**
 * <h3>{@link IPropertyBean}</h3>
 * Description of a Java bean property.<br>
 * <br>
 * Use {@link CoreUtils#getPropertyBeans(IType, org.apache.commons.collections4.Predicate, java.util.Comparator)} to get
 * {@link IPropertyBean}s.
 *
 * @author Andreas Hoegger
 * @since 3.0.0
 * @see CoreUtils#getPropertyBeans(IType, org.apache.commons.collections4.Predicate, java.util.Comparator)
 */
public interface IPropertyBean {
  /**
   * @return The declaring type that is hosting this property bean.
   */
  IType getDeclaringType();

  /**
   * @return The property's getter method or <code>null</code> if it is write-only.
   */
  IMethod getReadMethod();

  /**
   * @return The property's setter method or <code>null</code> if it is read-only.
   */
  IMethod getWriteMethod();

  /**
   * @return The bean's name.
   */
  String getBeanName();

  /**
   * @return The data type of the bean.
   */
  IType getBeanType();
}
