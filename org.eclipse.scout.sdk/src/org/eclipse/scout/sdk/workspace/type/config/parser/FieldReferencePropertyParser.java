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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.workspace.type.config.property.FieldProperty;

/**
 * <h3>{@link FieldReferencePropertyParser}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 27.02.2013
 */
public abstract class FieldReferencePropertyParser<T> implements IPropertySourceParser<FieldProperty<T>> {

  private final List<FieldProperty<T>> m_properties;

  public FieldReferencePropertyParser(List<FieldProperty<T>> properties) {
    m_properties = properties;

  }

  public List<FieldProperty<T>> getProperties() {
    return m_properties;
  }

  @Override
  public FieldProperty<T> parseSourceValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException {
    T parsedValue = getReturnValue(source, context, superTypeHierarchy);
    for (FieldProperty<T> prop : getProperties()) {
      if (prop.getSourceValue().equals(parsedValue)) {
        return prop;
      }
    }
    return null;
  }

  public abstract T getReturnValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException;

  @Override
  public String formatSourceValue(FieldProperty<T> value, String lineDelimiter, IImportValidator importValidator) throws CoreException {
    return value.getConstant().getElementName();
  }

}
