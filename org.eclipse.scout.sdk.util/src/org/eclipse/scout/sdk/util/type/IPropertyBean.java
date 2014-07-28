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
package org.eclipse.scout.sdk.util.type;

import java.beans.PropertyDescriptor;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

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
   * Property inspectors try to determine a bean property's field. Since the JavaBeans specification defines bean
   * properties by their access methods it is possible, that a property is not based on a field at all. This method
   * returns <code>null</code> in such a case.
   *
   * @return The field this property is based on or <code>null</code> if it can not be determined.
   */
  IField getField();

  /**
   * @return the read, write method and the field if not null.
   */
  IMember[] getAllMembers();

  /**
   * @return Returns the bean's name.
   */
  String getBeanName();

  String getBeanSignature();
}
