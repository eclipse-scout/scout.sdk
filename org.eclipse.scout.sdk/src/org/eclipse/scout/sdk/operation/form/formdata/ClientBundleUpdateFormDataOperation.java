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

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IPrimaryTypeTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

/**
 * <h3>{@link ClientBundleUpdateFormDataOperation}</h3> ...
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
    return "Update form data of '" + getClientBundle().getBundleName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getClientBundle() == null) {
      throw new IllegalArgumentException("client bundle can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // collect types
    ArrayList<IType> types = new ArrayList<IType>();
    IType iForm = TypeUtility.getType(RuntimeClasses.IForm);
    IPrimaryTypeTypeHierarchy formHierarchy = TypeUtility.getPrimaryTypeHierarchy(iForm);
    types.addAll(Arrays.asList(formHierarchy.getAllSubtypes(iForm, ScoutTypeFilters.getInScoutBundles(getClientBundle()))));

    IType iFormField = TypeUtility.getType(RuntimeClasses.IFormField);
    IPrimaryTypeTypeHierarchy formFieldHierarchy = TypeUtility.getPrimaryTypeHierarchy(iFormField);
    types.addAll(Arrays.asList(formFieldHierarchy.getAllSubtypes(iFormField, ScoutTypeFilters.getInScoutBundles(getClientBundle()))));

    MultipleFormDataUpdateOperation updateOp = new MultipleFormDataUpdateOperation(types.toArray(new IType[types.size()]));
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