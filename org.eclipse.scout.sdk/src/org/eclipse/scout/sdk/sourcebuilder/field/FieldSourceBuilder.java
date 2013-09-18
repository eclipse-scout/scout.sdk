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
package org.eclipse.scout.sdk.sourcebuilder.field;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.sourcebuilder.AbstractAnnotatableSourceBuilder;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;

/**
 * <h3>{@link FieldSourceBuilder}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.10.0 07.03.2013
 */
public class FieldSourceBuilder extends AbstractAnnotatableSourceBuilder implements IFieldSourceBuilder {

  private String m_signature;
  private String m_value;

  /**
   * @param elementName
   */
  public FieldSourceBuilder(String fieldName) {
    super(fieldName);
  }

  @Override
  public void validate() throws IllegalArgumentException {
    super.validate();
    if (StringUtility.isNullOrEmpty(getSignature())) {
      throw new IllegalArgumentException("signature is null!");
    }
  }

  @Override
  public void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    super.createSource(source, lineDelimiter, ownerProject, validator);
    source.append(Flags.toString(getFlags())).append(" ");
    // field type
    source.append(SignatureUtility.getTypeReference(getSignature(), validator) + " ");
    source.append(getElementName()).append(" ");
    // init value
    createInitValue(source, lineDelimiter, ownerProject, validator);
    source.append(";");
  }

  /**
   * can be overridden to assign a specific value to the field.
   * Use {@link SignatureUtility#getTypeReference(String, IImportValidator)} to determ class references (fully
   * qualified vs. simple name).
   * 
   * @param sourceBuilder
   * @param lineDelimiter
   * @param ownerProject
   * @param validator
   *          * validator can be used to determ class references (fully qualified vs. simple name).
   * @throws JavaModelException
   */
  protected void createInitValue(StringBuilder sourceBuilder, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws JavaModelException {
    if (!StringUtility.isNullOrEmpty(getValue())) {
      sourceBuilder.append(" = ").append(getValue());
    }
  }

  @Override
  public void setSignature(String signature) {
    m_signature = signature;
  }

  @Override
  public String getSignature() {
    return m_signature;
  }

  @Override
  public void setValue(String value) {
    m_value = value;
  }

  @Override
  public String getValue() {
    return m_value;
  }
}
