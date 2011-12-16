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
package org.eclipse.scout.sdk.operation.annotation;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.scout.commons.annotations.SqlBindingIgnoreValidation;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link IgnoreSqlBindingAnnotationCreateOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 02.03.2011
 */
public class IgnoreSqlBindingAnnotationCreateOperation extends AnnotationCreateOperation {

  private HashSet<String> m_ignoredBindings;

  /**
   * @param annotationOwner
   * @param signature
   */
  public IgnoreSqlBindingAnnotationCreateOperation(IMember annotationOwner, String signature, String[] bindVariable) {
    super(annotationOwner, signature);
    m_ignoredBindings = new HashSet<String>();
    m_ignoredBindings.addAll(Arrays.asList(bindVariable));
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    IAnnotation existingAnnotation = JdtUtility.getAnnotation((IAnnotatable) getAnnotationOwner(), SqlBindingIgnoreValidation.class.getName());
    if (TypeUtility.exists(existingAnnotation)) {
      if (existingAnnotation.getSource().startsWith("@")) {
        for (IMemberValuePair p : existingAnnotation.getMemberValuePairs()) {
          String memberName = p.getMemberName();
          Object value = p.getValue();
          if ("value".equals(memberName) && p.getValueKind() == IMemberValuePair.K_STRING) {
            if (value instanceof Object[]) {
              Object[] values = (Object[]) value;
              for (Object v : values) {
                m_ignoredBindings.add((String) v);
              }
            }
            else {
              m_ignoredBindings.add((String) value);
            }
          }
        }
      }
    }
    // create parameter
    String[] bindVars = m_ignoredBindings.toArray(new String[m_ignoredBindings.size()]);
    StringBuilder parameterBuilder = new StringBuilder();
    if (bindVars.length == 1) {
      parameterBuilder.append("\"" + bindVars[0] + "\"");
    }
    else if (bindVars.length > 1) {
      parameterBuilder.append("{");
      for (int i = 0; i < bindVars.length; i++) {
        parameterBuilder.append("\"" + bindVars[i] + "\"");
        if (i < bindVars.length - 1) {
          parameterBuilder.append(", ");
        }
      }
      parameterBuilder.append("}");
    }
    addParameter(parameterBuilder.toString());
    super.run(monitor, workingCopyManager);
  }
}
