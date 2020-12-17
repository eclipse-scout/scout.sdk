/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.operation.jaxws;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;
import static org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment.toScoutProgress;
import static org.eclipse.scout.sdk.s2e.util.ApiHelper.scoutVersionOf;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.ISourceFolders;
import org.eclipse.scout.sdk.core.apidef.Api;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.s.apidef.IScoutApi;
import org.eclipse.scout.sdk.core.s.environment.IEnvironment;
import org.eclipse.scout.sdk.core.s.environment.IProgress;
import org.eclipse.scout.sdk.core.s.jaxws.AbstractWebServiceNewOperation;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.osgi.service.prefs.BackingStoreException;

/**
 * <h3>{@link WebServiceNewOperation}</h3>
 *
 * @since 7.0.0
 */
public class WebServiceNewOperation extends AbstractWebServiceNewOperation {

  public static final String FACTORY_PATH_FILE_NAME = ".factorypath";
  public static final String JDT_APT_SETTINGS_NODE = "org.eclipse.jdt.apt.core";
  public static final String APT_GEN_SRC_DIR_KEY = "org.eclipse.jdt.apt.genSrcDir";

  public static final String APT_RECONCILE_ENABLED_KEY = "org.eclipse.jdt.apt.reconcileEnabled";
  public static final boolean APT_RECONCILE_ENABLED_VALUE = true;

  public static final String APT_ENABLED_KEY = "org.eclipse.jdt.apt.aptEnabled";
  public static final boolean APT_ENABLED_VALUE = true;

  public static final String PROCESS_ANNOTATIONS_KEY = "org.eclipse.jdt.core.compiler.processAnnotations";
  public static final String PROCESS_ANNOTATIONS_VALUE = "enabled";

  private IJavaProject m_serverModule;
  private IJavaProject m_jaxWsProject;
  private String m_artifactId;

  @Override
  public void accept(IEnvironment env, IProgress progress) {
    var jaxWsProject = getJaxWsProject();
    if (JdtUtils.exists(jaxWsProject)) {
      setProjectRoot(jaxWsProject.getProject().getLocation().toFile().toPath());
      setSourceFolder(EclipseEnvironment.narrow(env)
          .toScoutJavaEnvironment(jaxWsProject)
          .primarySourceFolder()
          .orElseThrow(() -> newFail("Project {} does not contain a source folder.", jaxWsProject.getElementName())));
    }

    super.accept(env, progress);

    var postProcess = toScoutProgress(progress).newChild(2);
    if (isCreateNewModule()) {
      setIgnoreOptionalProblems(ISourceFolders.GENERATED_WS_IMPORT_SOURCE_FOLDER, postProcess.monitor());
    }
    else {
      S2eUtils.mavenUpdate(singleton(getJaxWsProject().getProject()), false, true, false, false, postProcess.monitor());
    }
  }

  @Override
  protected Path createNewJaxWsModule(IEnvironment env, IProgress progress) {
    Ensure.notNull(getServerModule(), "Target module pom file could not be found.");
    Ensure.notBlank(getArtifactId(), "ArtifactId cannot be empty when creating a new jaxws module.");

    var op = new JaxWsModuleNewOperation();
    op.setArtifactId(getArtifactId());
    op.setServerModule(getServerModule());
    op.accept(EclipseEnvironment.narrow(env), toScoutProgress(progress));

    var createdProject = op.getCreatedProject();
    setJaxWsProject(JavaCore.create(createdProject));

    return createdProject.getLocation().toFile().toPath();
  }

  @Override
  protected void createEntryPointDefinitions(IEnvironment env, IProgress progress) {
    enableApt(env, progress);
    super.createEntryPointDefinitions(env, progress);
  }

  protected void enableApt(IEnvironment env, IProgress progress) {
    getJaxWsProject().setOption(PROCESS_ANNOTATIONS_KEY, PROCESS_ANNOTATIONS_VALUE);

    var aptPluginPreferenceNode = new ProjectScope(getJaxWsProject().getProject()).getNode(JDT_APT_SETTINGS_NODE);
    aptPluginPreferenceNode.putBoolean(APT_ENABLED_KEY, APT_ENABLED_VALUE);
    aptPluginPreferenceNode.put(APT_GEN_SRC_DIR_KEY, ISourceFolders.GENERATED_ANNOTATIONS_SOURCE_FOLDER);
    aptPluginPreferenceNode.putBoolean(APT_RECONCILE_ENABLED_KEY, APT_RECONCILE_ENABLED_VALUE);
    try {
      aptPluginPreferenceNode.flush();
    }
    catch (BackingStoreException e) {
      SdkLog.info("Unable to save the APT preferences of project '{}'.", getJaxWsProject().getElementName(), e);
    }
    createFactoryPath(env, progress);
  }

  protected void createFactoryPath(IEnvironment env, IProgress progress) {
    var factoryPathFile = getProjectRoot().resolve(FACTORY_PATH_FILE_NAME);
    var scoutVersion = scoutVersionOf(getJaxWsProject(), EclipseEnvironment.narrow(env))
        .orElseThrow(() -> newFail("JaxWs module '{}' has no Scout dependency.", getJaxWsProject().getElementName()));
    var scoutApi = Api.create(IScoutApi.class, scoutVersion);
    var generator = new FactoryPathGenerator()
        .withRtVersion(scoutVersion.asString())
        .withJaxWsConstants(scoutApi.JaxWsConstants());
    env.writeResource(generator, factoryPathFile, progress);
  }

  protected void setIgnoreOptionalProblems(String entryPath, IProgressMonitor monitor) {
    try {
      var jaxWsProject = getJaxWsProject();
      var rawClasspathEntries = jaxWsProject.getRawClasspath();
      List<IClasspathEntry> newEntries = new ArrayList<>(rawClasspathEntries.length);
      var entryPathToSearch = new org.eclipse.core.runtime.Path('/' + jaxWsProject.getElementName() + '/' + entryPath);
      for (var entry : rawClasspathEntries) {
        if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE && entry.getPath().equals(entryPathToSearch)) {
          var origAttributes = entry.getExtraAttributes();
          var newAttributes = Arrays.stream(origAttributes)
              .filter(attrib -> !IClasspathAttribute.IGNORE_OPTIONAL_PROBLEMS.equals(attrib.getName()))
              .collect(toList());
          //noinspection CallToNumericToString
          newAttributes.add(JavaCore.newClasspathAttribute(IClasspathAttribute.IGNORE_OPTIONAL_PROBLEMS, Boolean.TRUE.toString()));
          newEntries.add(JavaCore.newSourceEntry(entry.getPath(), entry.getInclusionPatterns(), entry.getExclusionPatterns(), entry.getOutputLocation(), newAttributes.toArray(new IClasspathAttribute[0])));
        }
        else {
          newEntries.add(entry);
        }
      }
      jaxWsProject.setRawClasspath(newEntries.toArray(new IClasspathEntry[0]), monitor);
    }
    catch (JavaModelException e) {
      SdkLog.warning("'Ignore Optional Problems' could not be enabled.", e);
    }
  }

  @Override
  protected void createDerivedResources(IEnvironment env, IProgress progress) {
    new RebuildArtifactsOperation(getJaxWsProject()).accept(env, progress);
  }

  public String getArtifactId() {
    return m_artifactId;
  }

  public void setArtifactId(String artifactId) {
    m_artifactId = artifactId;
  }

  public IJavaProject getServerModule() {
    return m_serverModule;
  }

  public void setServerModule(IJavaProject targetModule) {
    m_serverModule = targetModule;
  }

  public IJavaProject getJaxWsProject() {
    return m_jaxWsProject;
  }

  public void setJaxWsProject(IJavaProject jaxWsProject) {
    m_jaxWsProject = jaxWsProject;
  }
}
