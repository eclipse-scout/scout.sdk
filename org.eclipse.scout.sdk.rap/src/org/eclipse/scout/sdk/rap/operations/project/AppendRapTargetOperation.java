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
package org.eclipse.scout.sdk.rap.operations.project;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.sdk.compatibility.TargetPlatformUtility;
import org.eclipse.scout.sdk.operation.project.AbstractScoutProjectNewOperation;
import org.eclipse.scout.sdk.operation.project.CreateTargetProjectOperation;
import org.eclipse.scout.sdk.operation.project.NewProjectLoadTargetOperation;
import org.eclipse.scout.sdk.rap.var.RapTargetVariable;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link AppendRapTargetOperation}</h3>
 * 
 * @author Matthias Villiger
 * @since 4.0.0 07.04.2014
 */
public class AppendRapTargetOperation extends AbstractScoutProjectNewOperation {

  public static final String PROP_TARGET_STRATEGY = "propTargetStrategy";
  public static final String PROP_EXTRACT_TARGET_FOLDER = "propExtractTargetFolder";
  public static final String PROP_LOCAL_TARGET_FOLDER = "propLocalTargetFolder";

  public static final String SCOUT_RT_RAP_FEATURE = "org.eclipse.scout.rt.rap.source.feature.group";
  public static final String ECLIPSE_RT_RAP_FEATURE = "org.eclipse.rap.feature.feature.group";

  private static final String RAP_TARGET_VARIABLE = "${" + RapTargetVariable.RAP_TARGET_KEY + "}";

  public static enum TARGET_STRATEGY {
    STRATEGY_REMOTE,
    STRATEGY_LOCAL_EXISTING,
    STRATEGY_LOCAL_EXTRACT,
    STRATEGY_LATER
  }

  @Override
  public boolean isRelevant() {
    return isNodeChecked(CreateUiRapPluginOperation.BUNDLE_ID);
  }

  @Override
  public void init() {
  }

  @Override
  public String getOperationName() {
    return null;
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    if (getTargetStrategy() == TARGET_STRATEGY.STRATEGY_LATER) {
      // no target set
      return;
    }

    IFile targetFile = getProperties().getProperty(CreateTargetProjectOperation.PROP_TARGET_FILE, IFile.class);
    if (getTargetStrategy() == TARGET_STRATEGY.STRATEGY_LOCAL_EXISTING) {
      // set the environment variable
      RapTargetVariable.get().setValue(getLocalTargetFolder());

      // existing local RAP target
      TargetPlatformUtility.addDirectoryToTarget(targetFile, new String[]{RAP_TARGET_VARIABLE});
    }
    else if (getTargetStrategy() == TARGET_STRATEGY.STRATEGY_REMOTE) {
      // remote target using the update sites
      TargetPlatformUtility.addInstallableUnitToTarget(targetFile, SCOUT_RT_RAP_FEATURE, null, UPDATE_SITE_URL_LUNA, monitor);
      TargetPlatformUtility.addInstallableUnitToTarget(targetFile, ECLIPSE_RT_RAP_FEATURE, null, UPDATE_SITE_URL_LUNA, monitor);
    }
    else if (getTargetStrategy() == TARGET_STRATEGY.STRATEGY_LOCAL_EXTRACT) {
      // locally extracted, new target from rap.target plug-in
      ScoutRapTargetCreationOperation scoutRapTargetExtractOp = new ScoutRapTargetCreationOperation();
      scoutRapTargetExtractOp.setDestinationDirectory(new File(getExtractTargetFolder()));
      scoutRapTargetExtractOp.validate();
      scoutRapTargetExtractOp.run(monitor, workingCopyManager);

      // set the environment variable
      RapTargetVariable.get().setValue(getExtractTargetFolder());

      // new local extracted target
      TargetPlatformUtility.addDirectoryToTarget(targetFile, new String[]{RAP_TARGET_VARIABLE});
    }
    getProperties().setProperty(NewProjectLoadTargetOperation.PROP_TARGET_PLATFORM_RELOAD_NECESSARY, Boolean.valueOf(true));
  }

  protected String getLocalTargetFolder() {
    return getProperties().getProperty(PROP_LOCAL_TARGET_FOLDER, String.class);
  }

  protected String getExtractTargetFolder() {
    return getProperties().getProperty(PROP_EXTRACT_TARGET_FOLDER, String.class);
  }

  protected TARGET_STRATEGY getTargetStrategy() {
    return getProperties().getProperty(PROP_TARGET_STRATEGY, TARGET_STRATEGY.class);
  }
}
