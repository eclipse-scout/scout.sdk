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
package org.eclipse.scout.sdk.workspace.dto.formdata;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.ITypeResolver;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 * <h3>{@link ClientBundleUpdateFormDataOperation}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 20.07.2011
 */
public class ClientBundleUpdateFormDataOperation implements IOperation {

  private final IScoutBundle m_clientBundle;

  public ClientBundleUpdateFormDataOperation(IScoutBundle clientBundle) {
    m_clientBundle = clientBundle;
  }

  @Override
  public String getOperationName() {
    return "Update form data of '" + getClientBundle().getSymbolicName() + "'...";
  }

  @Override
  public void validate() {
    if (getClientBundle() == null) {
      throw new IllegalArgumentException("client bundle can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {

    if (getClientBundle().isBinary()) {
      return;
    }

    // collect types
    final Set<IType> types = new HashSet<>();

    // forms
    IType iForm = TypeUtility.getType(IRuntimeClasses.IForm);
    ICachedTypeHierarchy formHierarchy = TypeUtility.getPrimaryTypeHierarchy(iForm);
    types.addAll(formHierarchy.getAllSubtypes(iForm, ScoutTypeFilters.getInScoutBundles(getClientBundle())));

    // form field templates
    IType iFormField = TypeUtility.getType(IRuntimeClasses.IFormField);
    Set<IType> formFields = TypeUtility.getAbstractTypesOnClasspath(iFormField, getClientBundle().getJavaProject(), ScoutTypeFilters.getInScoutBundles(getClientBundle()));
    types.addAll(formFields);

    MultipleFormDataUpdateOperation updateOp = new MultipleFormDataUpdateOperation(new ITypeResolver() {
      @Override
      public Set<IType> getTypes() {
        return types;
      }
    });
    updateOp.validate();
    updateOp.run(monitor, workingCopyManager);
  }

  /**
   * @return the clientBundle
   */
  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

}
