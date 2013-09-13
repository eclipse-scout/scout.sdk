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

import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.icon.IIconProvider;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;

/**
 * <h3>{@link IconSourcePropertyParser}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 06.03.2013
 */
public class IconSourcePropertyParser implements IPropertySourceParser<ScoutIconDesc> {

  private final static Pattern REGEX = Pattern.compile("^.*\\.([^\\.]+)$");
  private IIconProvider m_iconProvider;

  public IconSourcePropertyParser() {
  }

  @Override
  public ScoutIconDesc parseSourceValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException {
    String parsedString = PropertyMethodSourceUtility.parseReturnParameterIcon(source, context);
    if (getIconProvider() != null && !StringUtility.isNullOrEmpty(parsedString)) {
      String simpleIconName = REGEX.matcher(parsedString).replaceAll("$1");
      return getIconProvider().getIcon(simpleIconName);
    }
    return null;
  }

  @Override
  public String formatSourceValue(ScoutIconDesc value, String lineDelimiter, IImportValidator importValidator) throws CoreException {
    if (value != null) {
      String iconTypeSig = SignatureCache.createTypeSignature(value.getConstantField().getDeclaringType().getFullyQualifiedName());
      return importValidator.getTypeName(iconTypeSig) + "." + value.getConstantField().getElementName();
    }
    else {
      return "null";
    }
  }

  public IIconProvider getIconProvider() {
    return m_iconProvider;
  }

  public void setIconProvider(IIconProvider iconProvider) {
    m_iconProvider = iconProvider;
  }
}
