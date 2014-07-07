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

import java.util.regex.Pattern;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;

/**
 * Contains predefined field filters
 */
public final class FieldFilters {

  private FieldFilters() {
  }

  public static IFieldFilter getNameFilter(final String name) {
    return new IFieldFilter() {
      @Override
      public boolean accept(IField field) {
        return CompareUtility.equals(field.getElementName(), name);
      }
    };
  }

  public static IFieldFilter getFlagsFilter(final int flags) {
    return new IFieldFilter() {
      @Override
      public boolean accept(IField field) {
        try {
          return (flags & field.getFlags()) == flags;
        }
        catch (JavaModelException e) {
          SdkUtilActivator.logWarning("could not filter field '" + field.getElementName() + "' on '" + field.getDeclaringType().getFullyQualifiedName() + "'.", e);
        }
        return false;
      }
    };
  }

  public static IFieldFilter getNameRegexFilter(final Pattern regex) {
    return new IFieldFilter() {
      @Override
      public boolean accept(IField field) {
        return regex.matcher(field.getElementName()).matches();
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
          SdkUtilActivator.logWarning("could not filter field '" + field.getElementName() + "' on '" + field.getDeclaringType().getFullyQualifiedName() + "'.", e);
        }
        return false;
      }
    };
  }

  public static IFieldFilter getCompositeFilter(final IFieldFilter... filters) {
    return new IFieldFilter() {
      @Override
      public boolean accept(IField field) {
        for (IFieldFilter f : filters) {
          if (!f.accept(field)) {
            return false;
          }
        }
        return true;
      }
    };
  }
}
