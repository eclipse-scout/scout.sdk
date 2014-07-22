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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;

/**
 * <h3>{@link NlsPropertySourceParser}</h3>
 * 
 * @author Andreas Hoegger
 * @since 3.8.0 27.02.2013
 */
public class NlsPropertySourceParser implements IPropertySourceParser<INlsEntry> {

  private final INlsProject m_nlsProject;

  public NlsPropertySourceParser(INlsProject nlsProject) {
    m_nlsProject = nlsProject;

  }

  public INlsProject getNlsProject() {
    return m_nlsProject;
  }

  @Override
  public String formatSourceValue(INlsEntry value, String lineDelimiter, IImportValidator importValidator) throws CoreException {
    StringBuilder sourceBuilder = new StringBuilder();
    if (value == null) {
      sourceBuilder.append("null");
    }
    else {
      String nlsTypeSig = SignatureCache.createTypeSignature(value.getProject().getNlsAccessorType().getFullyQualifiedName());
      sourceBuilder.append(SignatureUtility.getTypeReference(nlsTypeSig, importValidator));
      sourceBuilder.append(".get(\"").append(value.getKey()).append("\")");
    }
    return sourceBuilder.toString();
  }

  @Override
  public INlsEntry parseSourceValue(String source, IMethod context, ITypeHierarchy superTypeHierarchy) throws CoreException {
    String currentSourceValueKey = PropertyMethodSourceUtility.parseReturnParameterNlsKey(source);
    if (currentSourceValueKey == null) {
      return null;
    }
    INlsEntry entry = getNlsProject().getEntry(currentSourceValueKey);
    if (entry == null) {
      throw new CoreException(new ScoutStatus(Status.WARNING, "Key '" + currentSourceValueKey + "' not found!", null));
    }
    else {
      return entry;
    }
  }
}
