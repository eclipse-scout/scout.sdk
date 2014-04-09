/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.operation.project;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.operation.util.LoadTargetPlatformOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link NewProjectLoadTargetOperation}</h3>
 * 
 * @author Matthias Villiger
 * @since 4.0.0 07.04.2014
 */
public class NewProjectLoadTargetOperation extends AbstractScoutProjectNewOperation {

  public static final String PROP_TARGET_PLATFORM_RELOAD_NECESSARY = "targetPlatformReloadRequired";

  private IFile m_targetFile;

  @Override
  public boolean isRelevant() {
    Boolean reloadNecessary = getProperties().getProperty(PROP_TARGET_PLATFORM_RELOAD_NECESSARY, Boolean.class);
    return reloadNecessary != null && reloadNecessary.booleanValue();
  }

  @Override
  public void init() {
    m_targetFile = getProperties().getProperty(CreateTargetProjectOperation.PROP_TARGET_FILE, IFile.class);
  }

  @Override
  public void validate() throws IllegalArgumentException {
    super.validate();
    if (m_targetFile == null) {
      throw new IllegalArgumentException("target file cannot be null.");
    }
  }

  @Override
  public String getOperationName() {
    return "Load target platform";
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    LoadTargetPlatformOperation o = new LoadTargetPlatformOperation(m_targetFile);
    o.validate();
    o.run(monitor, workingCopyManager);
  }
}
