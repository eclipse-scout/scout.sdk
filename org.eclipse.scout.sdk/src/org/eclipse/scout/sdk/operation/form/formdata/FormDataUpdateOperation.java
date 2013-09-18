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
package org.eclipse.scout.sdk.operation.form.formdata;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.internal.workspace.dto.FormDataUtility;
import org.eclipse.scout.sdk.operation.jdt.icu.CompilationUnitUpdateOperation;
import org.eclipse.scout.sdk.operation.util.OrganizeImportOperation;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link FormDataUpdateOperation}</h3>
 * 
 * @author aho
 * @since 3.8.0 14.01.2013
 */
public class FormDataUpdateOperation extends CompilationUnitUpdateOperation {

  private final IType m_formDataDefinitionType;
  private IType m_createdFormData;

  /**
   * @param typeName
   */
  public FormDataUpdateOperation(IType formDataDefinitionType, ICompilationUnit formDataIcu) {
    super(formDataIcu);
    m_formDataDefinitionType = formDataDefinitionType;
    setFormatSource(true);
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (!TypeUtility.exists(getFormDataDefinitionType())) {
      throw new IllegalArgumentException("form data definition type '" + getFormDataDefinitionType() + "' does not exist.");
    }
    super.validate();
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    FormDataAnnotation formDataAnnotation = ScoutTypeUtility.findFormDataAnnotation(getFormDataDefinitionType(), TypeUtility.getSuperTypeHierarchy(getFormDataDefinitionType()));
    ITypeSourceBuilder typeOp = FormDataUtility.createFormDataSourceBuilder(getFormDataDefinitionType(), formDataAnnotation);
    addTypeSourceBuilder(typeOp);

    super.run(monitor, workingCopyManager);

    // organize import required to:
    // 1. ensure the JDT settings for the imports are applied
    // 2. resolve references e.g. in validation rules to classes holding constants.
    //    see /org.eclipse.scout.sdk.test/resources/operation/formData/formdata.client/src/formdata/client/ui/template/formfield/AbstractLimitedStringField.java for an example.
    OrganizeImportOperation o = new OrganizeImportOperation(getCompilationUnit());
    o.validate();
    o.run(monitor, workingCopyManager);

    m_createdFormData = getCompilationUnit().getType(typeOp.getElementName());
  }

  public IType getFormDataDefinitionType() {
    return m_formDataDefinitionType;
  }

  public IType getCreatedFormData() {
    return m_createdFormData;
  }

}
