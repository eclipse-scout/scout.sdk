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
package org.eclipse.scout.sdk.operation.form.util;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;

public final class FormDataUtility {

  private FormDataUtility() {
  }

  public static String getFieldIdWithoutSuffix(String s) {
    if (s.endsWith("Field")) {
      s = s.replaceAll("Field$", "");
    }
    else if (s.endsWith("Button")) {
      s = s.replaceAll("Button$", "");
    }
    else if (s.endsWith("Column")) {
      s = s.replaceAll("Column$", "");
    }
    return s;
  }

  public static String getBeanName(String name, boolean startWithUpperCase) {
    char firstChar = Character.toLowerCase(name.charAt(0));
    if (startWithUpperCase) {
      firstChar = Character.toUpperCase(name.charAt(0));
    }
    return firstChar + name.substring(1);
  }

  public static String indent(String s) {
    return ScoutIdeProperties.TAB + s.replace("\n", "\n" + ScoutIdeProperties.TAB);
  }

  /**
   * @return parsed annotation descriptor or null if there is no (and also no implicit) annotation
   */
  public static FormDataAnnotationDesc parseFormDataAnnotation(IType t, ITypeHierarchy formFieldHierarchy) throws JavaModelException {
    FormDataAnnotationDesc desc = null;
    // eval scout form data directive
    String formdataDirective = null;
    IAnnotation formDataAnnotation = null;
    IType tempType = t;
    while (tempType != null) {
      formDataAnnotation = TypeUtility.getAnnotation(tempType, RuntimeClasses.FormData);
      if (TypeUtility.exists(formDataAnnotation)) {
        IMemberValuePair[] memberValuePairs = formDataAnnotation.getMemberValuePairs();
        for (IMemberValuePair p : memberValuePairs) {
          if (p.getMemberName().equals("value")) {
            formdataDirective = (String) p.getValue();
            break;
          }
        }
        if (formdataDirective == null) {
          formdataDirective = "CREATE";
        }
        break;
      }
      tempType = formFieldHierarchy.getSuperclass(tempType);
    }

    // defaults based on data type
    if (formdataDirective == null) {
      if (formFieldHierarchy.isSubtype(ScoutSdk.getType(RuntimeClasses.IValueField), t)) {
        formdataDirective = "CREATE";
      }
      else if (formFieldHierarchy.isSubtype(ScoutSdk.getType(RuntimeClasses.ITableField), t)) {
        formdataDirective = "CREATE";
      }
      else if (formFieldHierarchy.isSubtype(ScoutSdk.getType(RuntimeClasses.IComposerField), t)) {
        formdataDirective = "CREATE";
      }
      else if (formFieldHierarchy.isSubtype(ScoutSdk.getType(RuntimeClasses.IComposerAttribute), t)) {
        formdataDirective = "CREATE";
      }
      else if (formFieldHierarchy.isSubtype(ScoutSdk.getType(RuntimeClasses.IComposerEntity), t)) {
        formdataDirective = "CREATE";
      }
      else {
        formdataDirective = "IGNORE";
      }
    }
    // parse
    for (String s : formdataDirective.split("\\s")) {
      if (s.equalsIgnoreCase("IGNORE")) {
        // nop
      }
      else if (s.equalsIgnoreCase("CREATE")) {
        if (desc == null) desc = new FormDataAnnotationDesc();
      }
      else if (s.equalsIgnoreCase("USING")) {
        if (desc == null) desc = new FormDataAnnotationDesc();
        desc.usingFlag = true;
      }
      else if (s.equalsIgnoreCase("EXTERNAL")) {
        if (desc == null) desc = new FormDataAnnotationDesc();
        desc.externalFlag = true;
      }
      else {
        if (desc == null) desc = new FormDataAnnotationDesc();
        desc.fullyQualifiedName = s;
      }
    }
    return desc;
  }

  public static class FormDataAnnotationDesc {
    public boolean usingFlag = false;
    public boolean externalFlag = false;
    public String fullyQualifiedName = null;
  }

}
