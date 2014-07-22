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
package org.eclipse.scout.sdk.testing;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.CompareUtility;
import org.junit.Assert;

/**
 * <h3>{@link ApiAssert}</h3>
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 22.05.2013
 */
public class ApiAssert extends Assert {

  public static void assertConstantValue(IField field, Object constantValue) throws JavaModelException {
    assertConstantValue(null, field, constantValue);
  }

  public static void assertConstantValue(String message, IField field, Object constantValue) throws JavaModelException {
    if (!CompareUtility.equals(field.getConstant(), constantValue)) {
      if (message == null) {
        StringBuilder messageBuilder = new StringBuilder("Field ");
        messageBuilder.append(field.getElementName()).append(" with value [").append(field.getConstant()).append("] is expected to have value [").append(constantValue).append("].");
        message = messageBuilder.toString();
      }
      fail(message);
    }
  }
}
