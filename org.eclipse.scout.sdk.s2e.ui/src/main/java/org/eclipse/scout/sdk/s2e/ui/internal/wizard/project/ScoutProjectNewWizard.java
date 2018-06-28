/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.internal.wizard.project;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.m2e.core.internal.lifecyclemapping.discovery.ILifecycleMappingRequirement;
import org.eclipse.m2e.core.internal.lifecyclemapping.discovery.IMavenDiscoveryProposal;
import org.eclipse.m2e.core.internal.lifecyclemapping.discovery.LifecycleMappingDiscoveryRequest;
import org.eclipse.m2e.core.internal.lifecyclemapping.discovery.MojoExecutionMappingConfiguration.MojoExecutionMappingRequirement;
import org.eclipse.m2e.core.ui.internal.wizards.MappingDiscoveryJob;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.job.ResourceBlockingOperationJob;
import org.eclipse.scout.sdk.s2e.operation.project.ScoutProjectNewOperation;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractWizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.osgi.framework.Version;

/**
 * <h3>{@link ScoutProjectNewWizard}</h3> Wizard that creates a new Scout project
 *
 * @author Matthias Villiger
 * @since 5.1.0
 */
public class ScoutProjectNewWizard extends AbstractWizard implements INewWizard {

  private static final String EXEC_ENV_PREFIX = "JavaSE-";
  private static final String MIN_JVM_VERSION = "1.8";

  private ScoutProjectNewWizardPage m_page1;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    setWindowTitle("New Scout Project");
    setHelpAvailable(true);
    setDefaultPageImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.ScoutProjectNewWizBanner));

    m_page1 = new ScoutProjectNewWizardPage();
    addPage(m_page1);
  }

  @Override
  public boolean performFinish() {
    if (!super.performFinish()) {
      return false;
    }

    // prepare operation
    final ScoutProjectNewOperation op = new ScoutProjectNewOperation();
    op.setDisplayName(m_page1.getDisplayName());
    op.setGroupId(m_page1.getGroupId());
    op.setArtifactId(m_page1.getArtifactId());
    op.setJavaVersion(getDefaultWorkspaceJavaVersion());
    op.setUseJsClient(m_page1.isUseJsClient());
    if (m_page1.isUseWorkspaceLocation()) {
      op.setTargetDirectory(ScoutProjectNewWizardPage.getWorkspaceLocation());
    }
    else {
      op.setTargetDirectory(m_page1.getTargetDirectory());
    }

    // remember folder
    String path = null;
    if (m_page1.getTargetDirectory() != null) {
      path = m_page1.getTargetDirectory().getAbsolutePath();
    }
    getDialogSettings().put(ScoutProjectNewWizardPage.SETTINGS_TARGET_DIR, path);

    // run operation
    final ResourceBlockingOperationJob projectCreationJob = new ResourceBlockingOperationJob(op);

    // append mapping discovery
    projectCreationJob.addJobChangeListener(new JobChangeAdapter() {
      @Override
      public void done(IJobChangeEvent event) {
        if (projectCreationJob.getResult().isOK()) {
          List<IProject> createdProjects = op.getCreatedProjects();
          if (createdProjects != null && !createdProjects.isEmpty()) {
            discoverMapping(createdProjects);
          }
        }
      }
    });
    projectCreationJob.schedule();

    return true;
  }

  protected void discoverMapping(List<IProject> projects) {
    P_MappingDiscoveryJob discoveryJob = new P_MappingDiscoveryJob(projects);
    discoveryJob.schedule();
  }

  protected static final class P_MappingDiscoveryJob extends MappingDiscoveryJob {

    public P_MappingDiscoveryJob(Collection<IProject> projects) {
      super(projects);
    }

    @Override
    protected void discoverProposals(LifecycleMappingDiscoveryRequest discoveryRequest, IProgressMonitor monitor) throws CoreException {
      super.discoverProposals(discoveryRequest, monitor);

      // by default remove all wrong proposals so that only one proposal by execution-id remains -> default selection can choose and is correct by default.
      for (Entry<ILifecycleMappingRequirement, List<IMavenDiscoveryProposal>> entry : discoveryRequest.getAllProposals().entrySet()) {
        if (entry.getKey() instanceof MojoExecutionMappingRequirement) {
          MojoExecutionMappingRequirement req = (MojoExecutionMappingRequirement) entry.getKey();
          if ("default-compile".equals(req.getExecutionId()) || "default-testCompile".equals(req.getExecutionId())) {
            List<IMavenDiscoveryProposal> proposals = entry.getValue();
            if (proposals != null && proposals.size() > 1) {
              Iterator<IMavenDiscoveryProposal> iterator = proposals.iterator();
              while (iterator.hasNext()) {
                IMavenDiscoveryProposal proposal = iterator.next();
                if (proposal == null || !proposal.toString().endsWith("Eclipse JDT Compiler")) {
                  iterator.remove();
                }
              }
            }
          }
        }
      }
    }
  }

  protected static String getDefaultWorkspaceJavaVersion() {
    return versionToString(computeDefaultWorkspaceJavaVersion());
  }

  /**
   * Converts the specified {@link Version} to a {@link String}. Only the major and minor parts are used. Trailing zeroes
   * are omitted.<br>
   *
   * @param version
   *          The {@link Version} to convert.
   * @return E.g. "1.8" or "9".
   */
  protected static String versionToString(final Version version) {
    final StringBuilder b = new StringBuilder(4);
    b.append(version.getMajor());
    if (version.getMinor() != 0) {
      b.append('.').append(version.getMinor());
    }
    return b.toString();
  }

  /**
   * Gets the default Java version supported by the current default JVM of the workspace.
   *
   * @return A {@link Version} like "1.8.0" or "9.0.0" with the latest version supported in the current default JVM.
   */
  protected static Version computeDefaultWorkspaceJavaVersion() {
    Version result = Version.parseVersion(MIN_JVM_VERSION);
    final IVMInstall defaultVm = JavaRuntime.getDefaultVMInstall();
    if (defaultVm == null) {
      return result;
    }

    for (final IExecutionEnvironment env : JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments()) {
      if (env.isStrictlyCompatible(defaultVm)) {
        final Version cur = execEnvironmentToVersion(env.getId());
        if (cur.compareTo(result) > 0) {
          result = cur; // take the newest
        }
      }
    }
    return result;
  }

  /**
   * Takes a Java execution environment (e.g. "JavaSE-1.8" or "JavaSE-9") and converts it to a {@link Version}.<br>
   * If an invalid value is passed, always 1.8 is returned as minimal version.<br>
   *
   * @param executionEnvId
   *          The execution environment of the form "JavaSE-1.8" or "JavaSE-9 to parse.
   * @return The {@link Version} holding the decimal equivalent value. E.g. {@code 1.8.0} or {@code 9.0.0}.
   */
  protected static Version execEnvironmentToVersion(final String executionEnvId) {
    if (executionEnvId != null && executionEnvId.startsWith(EXEC_ENV_PREFIX)) {
      String numPart = executionEnvId.substring(EXEC_ENV_PREFIX.length());
      if (StringUtils.isNotBlank(numPart)) {
        try {
          return Version.parseVersion(numPart);
        }
        catch (final IllegalArgumentException e) {
          SdkLog.warning("Invalid number part ({}) in execution environment {}.", numPart, executionEnvId, e);
        }
      }
    }
    return Version.parseVersion(MIN_JVM_VERSION);
  }
}
