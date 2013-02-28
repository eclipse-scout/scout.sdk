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

import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.type.IPropertyBean;
import org.eclipse.scout.sdk.util.type.IPropertyBeanFilter;

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
  public static IPropertyBeanFilter getFormDataPropertyFilter() {
    return new IPropertyBeanFilter() {
      @Override
      public boolean accept(IPropertyBean property) {
        if (property.getReadMethod() == null || JdtUtility.getAnnotation(property.getReadMethod(), RuntimeClasses.FormData) == null) {
          return false;
        }
        if (property.getWriteMethod() == null || JdtUtility.getAnnotation(property.getWriteMethod(), RuntimeClasses.FormData) == null) {
          return false;
        }
        return true;
      }
    };
  }
}
