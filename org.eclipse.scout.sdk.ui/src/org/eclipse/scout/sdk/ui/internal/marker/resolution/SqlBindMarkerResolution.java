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
package org.eclipse.scout.sdk.ui.internal.marker.resolution;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.commons.annotations.SqlBindingIgnoreValidation;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.jdt.annotation.IgnoreSqlBindingAnnotationCreateOperation;
import org.eclipse.scout.sdk.sql.binding.SqlBindingMarkers;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.ui.IMarkerResolution;

/**
 * <h3>{@link SqlBindMarkerResolution}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 15.03.2011
 */
public class SqlBindMarkerResolution implements IMarkerResolution {

  private String[] m_bindings;

  public SqlBindMarkerResolution(String... bindings) {
    m_bindings = bindings;
  }

  @Override
  public String getLabel() {
    if (getBindings().length == 1) {
      return Texts.get("AddIgnoreAnnotation", getBindings()[0]);
    }
    else {
      return Texts.get("AddIgnoreAnnotationPlural");
    }
  }

  @Override
  public void run(IMarker marker) {
    String icuName = "";
    String bindVar = "";
    try {
      ICompilationUnit icu = (ICompilationUnit) JavaCore.create((IFile) marker.getResource());
      if (icu.getTypes().length > 0) {
        bindVar = (String) marker.getAttribute(SqlBindingMarkers.BIND_VARIABLE);
        icuName = icu.getElementName();
        Integer start = (Integer) marker.getAttribute(IMarker.CHAR_START);
        IJavaElement element = icu.getElementAt(start);
        if (TypeUtility.exists(element)) {
          if (element.getElementType() != IJavaElement.METHOD) {
            element = element.getAncestor(IJavaElement.METHOD);
          }
        }
        if (TypeUtility.exists(element) && element.getElementType() == IJavaElement.METHOD) {
          IgnoreSqlBindingAnnotationCreateOperation op = new IgnoreSqlBindingAnnotationCreateOperation((IMethod) element, SignatureCache.createTypeSignature(SqlBindingIgnoreValidation.class.getName()), m_bindings);
          OperationJob job = new OperationJob(op);
          job.schedule();
          job.join();
          UiUtility.showJavaElementInEditor(element, false);
        }
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not apply annotation resolution for bindvar '" + bindVar + "' in  '" + icuName + "'.");
    }
  }

  /**
   * @return the bindings
   */
  public String[] getBindings() {
    return m_bindings;
  }

}
