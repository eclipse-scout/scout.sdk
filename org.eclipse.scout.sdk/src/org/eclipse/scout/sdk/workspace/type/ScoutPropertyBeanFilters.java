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

import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.type.IPropertyBean;
import org.eclipse.scout.sdk.util.type.IPropertyBeanFilter;
import org.eclipse.scout.sdk.util.type.TypeUtility;

/**
 * Convenience class for commonly used {@link IPropertyBeanFilter}.
 */
public final class ScoutPropertyBeanFilters {

  private ScoutPropertyBeanFilters() {
  }

  /**
   * Creates a new filter used to extract form properties that are part of the form data as well. Candidates have an
   * annotated getter and setter, respectively.
   *
   * @return Returns a new filter instance.
   */
  public static IPropertyBeanFilter getDtoPropertyFilter() {
    return new IPropertyBeanFilter() {
      @Override
      public boolean accept(IPropertyBean property) {
        // read and write method must exist
        boolean readAndWriteMethodsExist = TypeUtility.exists(property.getReadMethod()) && TypeUtility.exists(property.getWriteMethod());
        if (!readAndWriteMethodsExist) {
          return false;
        }

        // @FormData or @Data annotation must exist
        boolean isReadMethodDtoRelevant = JdtUtility.hasAnnotation(property.getReadMethod(), IRuntimeClasses.FormData) || JdtUtility.hasAnnotation(property.getReadMethod(), IRuntimeClasses.Data);
        if (!isReadMethodDtoRelevant) {
          return false;
        }
        boolean isWriteMethodDtoRelevant = JdtUtility.hasAnnotation(property.getWriteMethod(), IRuntimeClasses.FormData) || JdtUtility.hasAnnotation(property.getWriteMethod(), IRuntimeClasses.Data);
        if (!isWriteMethodDtoRelevant) {
          return false;
        }

        return true;
      }
    };
  }
}
