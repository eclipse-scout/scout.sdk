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
package org.eclipse.scout.sdk.workspace.type;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.ScoutSdk;

/**
 *
 */
public class FieldFilters {

  public static IFieldFilter getFlagsFilter(final int flags) {
    return new IFieldFilter() {
      @Override
      public boolean accept(IField field) {
        try {
          return (flags & field.getFlags()) == flags;
        }
        catch (JavaModelException e) {
          ScoutSdk.logWarning("could not filter field '" + field.getElementName() + "' on '" + field.getDeclaringType().getFullyQualifiedName() + "'.", e);
        }
        return false;
      }
    };
  }

  public static IFieldFilter getNameRegexFilter(final String regex) {
    return new IFieldFilter() {
      @Override
      public boolean accept(IField field) {
        return field.getElementName().matches(regex);
      }
    };
  }

  public static IFieldFilter getPrivateNotStaticNotFinalNotAbstract() {
    return new IFieldFilter() {
      @Override
      public boolean accept(IField field) {
        try {
          int flags = field.getFlags();
          return Flags.isPrivate(flags) && !Flags.isStatic(flags) && !Flags.isFinal(flags) && !Flags.isAbstract(flags);
        }
        catch (JavaModelException e) {
          ScoutSdk.logWarning("could not filter field '" + field.getElementName() + "' on '" + field.getDeclaringType().getFullyQualifiedName() + "'.", e);
        }
        return false;
      }
    };
  }
}
