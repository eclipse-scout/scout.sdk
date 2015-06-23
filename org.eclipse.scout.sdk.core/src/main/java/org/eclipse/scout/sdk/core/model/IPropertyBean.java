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

import java.beans.PropertyDescriptor;

/**
 * Description of a Java bean property. This is a dual class to Java's {@link PropertyDescriptor}.
 */
public interface IPropertyBean {
  /**
   * @return Returns the declaring type that is hosting this property bean.
   */
  IType getDeclaringType();

  /**
   * @return Returns the property's getter method or <code>null</code> if it is write-only.
   */
  IMethod getReadMethod();

  /**
   * @return Returns the property's setter method or <code>null</code> if it is read-only.
   */
  IMethod getWriteMethod();

  /**
   * @return Returns the bean's name.
   */
  String getBeanName();

  IType getBeanType();
}
