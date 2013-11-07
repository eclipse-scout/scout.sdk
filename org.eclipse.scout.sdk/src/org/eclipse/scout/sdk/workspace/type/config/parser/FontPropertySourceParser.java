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
package org.eclipse.scout.sdk.workspace.type.config.parser;

import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.scout.sdk.workspace.type.config.property.FontSpec;
import org.eclipse.swt.SWT;

/**
 * <h3>{@link FontPropertySourceParser}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.8.0 12.03.2013
 */
public class FontPropertySourceParser implements IPropertySourceParser<FontSpec> {

  @Override
  public FontSpec parseSourceValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException {
    String value = PropertyMethodSourceUtility.parseReturnParameterString(source, context, superTypeHierarchy);
    if (value == null) {
      value = "";
    }
    FontSpec fontSpec = new FontSpec();
    StringTokenizer tok = new StringTokenizer(value, "-_,/.;");
    while (tok.hasMoreTokens()) {
      String nextToken = tok.nextToken();
      String s = nextToken.toUpperCase();
      // styles
      if (s.equals("PLAIN")) {
        fontSpec.addStyle(SWT.NORMAL);
        // nop
      }
      else if (s.equals("BOLD")) {
        fontSpec.addStyle(SWT.BOLD);
      }
      else if (s.equals("ITALIC")) {
        fontSpec.addStyle(SWT.ITALIC);
      }
      else {
        // size or name
        try {

          // size
          int size = Integer.parseInt(s);
          fontSpec.setHeight(size);
        }
        catch (NumberFormatException nfe) {
          // name
          fontSpec.setName(nextToken);
        }
      }
    }
    return fontSpec;
  }

  @Override
  public String formatSourceValue(FontSpec value, String lineDelimiter, IImportValidator importValidator) throws CoreException {
    if (value == null || value.isDefault()) {
      return "null";
    }
    StringBuilder sourceBuilder = new StringBuilder();
    if (value.getStyle() != null) {
      if ((value.getStyle() & SWT.BOLD) != 0) {
        if (sourceBuilder.length() > 0) {
          sourceBuilder.append("-");
        }
        sourceBuilder.append("BOLD");
      }
      if ((value.getStyle() & SWT.ITALIC) != 0) {
        if (sourceBuilder.length() > 0) {
          sourceBuilder.append("-");
        }
        sourceBuilder.append("ITALIC");
      }
      if (sourceBuilder.length() == 0) {
        sourceBuilder.append("PLAIN");

      }
    }
    if (value.getHeight() != null) {
      if (sourceBuilder.length() > 0) {
        sourceBuilder.append("-");
      }

      sourceBuilder.append(value.getHeight());
    }
    if (value.getName() != null) {
      if (sourceBuilder.length() > 0) {
        sourceBuilder.append("-");
      }
      sourceBuilder.append(value.getName());
    }
    return "\"" + sourceBuilder.toString() + "\"";
  }

}
