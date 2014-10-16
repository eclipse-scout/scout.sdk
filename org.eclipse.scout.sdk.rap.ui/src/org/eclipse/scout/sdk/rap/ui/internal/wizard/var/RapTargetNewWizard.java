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
package org.eclipse.scout.sdk.rap.ui.internal.wizard.var;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.rap.operations.project.AppendRapTargetOperation.TargetStrategy;
import org.eclipse.scout.sdk.rap.operations.project.ScoutRapTargetCreationOperation;
import org.eclipse.scout.sdk.rap.ui.internal.wizard.project.RapTargetPlatformWizardPage;
import org.eclipse.scout.sdk.rap.var.RapTargetVariable;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.ui.IWorkbench;

/**
 * <h3>{@link RapTargetNewWizard}</h3>
 *
 * @author Matthias Villiger
 * @since 3.9.0 16.01.2013
 */
public class RapTargetNewWizard extends AbstractWorkspaceWizard {
  private RapTargetPlatformWizardPage m_page1;

  private TargetStrategy m_strategy;
  private String m_localFolder;
  private String m_extractFolder;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    super.init(workbench, selection);

    setWindowTitle(Texts.get("SpecifyTheRAPTargetLocation"));

    m_page1 = new RapTargetPlatformWizardPage(new TargetStrategy[]{TargetStrategy.STRATEGY_LOCAL_EXISTING, TargetStrategy.STRATEGY_LOCAL_EXTRACT});
    m_page1.setTitle(Texts.get("SpecifyTheRAPTargetLocation"));
    addPage(m_page1);
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_strategy = m_page1.getTargetStrategy();
    m_localFolder = m_page1.getLocalTargetFolder();
    m_extractFolder = m_page1.getExtractTargetFolder();

    return super.beforeFinish();
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (TargetStrategy.STRATEGY_LOCAL_EXISTING.equals(m_strategy)) {
      // existing local target
      File f = new File(m_localFolder);
      RapTargetVariable.get().setValue(f.getAbsolutePath());
    }
    else {
      // locally extracted, new target from rap.target plug-in
      File f = new File(m_extractFolder);

      ScoutRapTargetCreationOperation scoutRapTargetExtractOp = new ScoutRapTargetCreationOperation();
      scoutRapTargetExtractOp.setDestinationDirectory(f);
      scoutRapTargetExtractOp.validate();
      scoutRapTargetExtractOp.run(monitor, workingCopyManager);

      // set the environment variable
      RapTargetVariable.get().setValue(f.getAbsolutePath());
    }
    return true;
  }
}
