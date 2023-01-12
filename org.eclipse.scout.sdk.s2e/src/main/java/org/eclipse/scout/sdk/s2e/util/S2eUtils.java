/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.util;

import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.internal.project.ProjectConfigurationManager;
import org.eclipse.m2e.core.project.MavenUpdateRequest;
import org.eclipse.scout.sdk.core.builder.IBuilderContext;
import org.eclipse.scout.sdk.core.model.api.internal.JavaEnvironmentImplementor;
import org.eclipse.scout.sdk.core.s.IScoutSourceFolders;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.core.s.util.maven.IMavenConstants;
import org.eclipse.scout.sdk.core.util.PropertySupport;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.core.util.Xml;
import org.eclipse.scout.sdk.s2e.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.environment.AbstractJob;
import org.eclipse.scout.sdk.s2e.environment.model.ClasspathWithJdt;
import org.eclipse.scout.sdk.s2e.util.JdtUtils.PublicPrimaryTypeFilter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * <h3>{@link S2eUtils}</h3>
 * <p>
 * Contains utility methods for the Scout SDK Eclipse integration.
 *
 * @since 5.1.0
 */
public final class S2eUtils {

  private S2eUtils() {
  }

  /**
   * Waits until all JDT initializations have been executed.
   * <p>
   * See {@code org.eclipse.jdt.internal.ui.InitializeAfterLoadJob.RealJob}
   */
  public static void waitForJdt() {
    AbstractJob.waitForJobFamily("org.eclipse.jdt.ui");
  }

  /**
   * Creates a new {@link PropertySupport} containing the given {@link IJavaProject}.
   *
   * @param p
   *          The {@link IJavaProject} the map should contain or {@code null}.
   * @param targetPath
   *          The {@link Path} in which the generators source will be stored (as exact as possible)
   * @return The created {@link PropertySupport}. Never returns {@code null}.
   */
  public static PropertySupport propertyMap(IJavaProject p, Path targetPath) {
    var context = new PropertySupport(2);
    context.setProperty(IBuilderContext.PROPERTY_JAVA_MODULE, p);
    context.setProperty(IBuilderContext.PROPERTY_TARGET_PATH, targetPath);
    return context;
  }

  /**
   * Tries to ensure that the given {@link IResource}s can be written (not read-only).<br>
   *
   * @param resources
   *          The resources that should be written
   * @return An {@link IStatus} describing if the given resources can be written now. If {@link IStatus#isOK()} returns
   *         {@code true}, it is safe to continue the write operation. Otherwise, the {@link IStatus} contains the files
   *         and reasons why this is not possible. This may be the case if the file is still read-only or because it
   *         changed value in the meantime.
   */
  @SuppressWarnings("pmd:NPathComplexity")
  public static IStatus makeCommittable(Collection<IResource> resources) {
    if (resources == null || resources.isEmpty()) {
      return Status.OK_STATUS;
    }

    var existingReadOnlyFiles = resources.stream()
        .filter(Objects::nonNull)
        .filter(IResource::exists)
        .filter(r -> r.getType() == IResource.FILE)
        .filter(S2eUtils::isReadOnly)
        .map(r -> (IFile) r)
        .collect(toSet());
    if (existingReadOnlyFiles.isEmpty()) {
      return Status.OK_STATUS;
    }

    var oldTimeStamps = createModificationStampMap(existingReadOnlyFiles);
    var status = ResourcesPlugin.getWorkspace().validateEdit(existingReadOnlyFiles.toArray(new IFile[0]), IWorkspace.VALIDATE_PROMPT);
    if (!status.isOK()) {
      return status;
    }

    IStatus modified = null;
    // check if the resources can be written now
    for (var f : existingReadOnlyFiles) {
      if (isReadOnly(f)) {
        var message = "File '" + f.getFullPath() + "' is read only.";
        modified = addStatus(modified, message);
      }
    }
    // check for in between modifications
    var newTimeStamps = createModificationStampMap(existingReadOnlyFiles);
    for (var e : oldTimeStamps.entrySet()) {
      var file = e.getKey();
      if (!e.getValue().equals(newTimeStamps.get(file))) {
        var message = "File '" + file.getFullPath() + "' has been modified since the beginning of the operation.";
        modified = addStatus(modified, message);
      }
    }
    if (modified != null) {
      return modified;
    }
    return Status.OK_STATUS;
  }

  private static IStatus addStatus(IStatus status, String msg) {
    IStatus entry = new Status(IStatus.ERROR, S2ESdkActivator.PLUGIN_ID, msg);
    if (status == null) {
      return entry;
    }
    else if (status.isMultiStatus()) {
      ((MultiStatus) status).add(entry);
      return status;
    }
    else {
      var result = new MultiStatus(S2ESdkActivator.PLUGIN_ID, 0, msg, null);
      result.add(status);
      result.add(entry);
      return result;
    }
  }

  private static Map<IFile, Long> createModificationStampMap(Collection<IFile> files) {
    Map<IFile, Long> map = new HashMap<>(files.size());
    for (var f : files) {
      map.put(f, f.getModificationStamp());
    }
    return map;
  }

  /**
   * Checks if the given {@link IResource} is read-only.
   *
   * @param resource
   *          The {@link IResource} to check. Must not be {@code null}.
   * @return {@code true} if the resource is marked as read-only. {@code false} otherwise.
   */
  private static boolean isReadOnly(IResource resource) {
    var resourceAttributes = resource.getResourceAttributes();
    return resourceAttributes != null && resourceAttributes.isReadOnly();
  }

  /**
   * Tries to find a test {@link IPackageFragmentRoot} in the given {@link IJavaProject} or an associated test
   * {@link IJavaProject}.
   *
   * @param orig
   *          The {@link IJavaProject} for which the primary test source folder should be found.
   * @param fqnOfRequiredType
   *          Fully qualified name of a type that must be accessible in the resulting {@link IPackageFragmentRoot} to be
   *          returned. May be {@code null} which means every {@link IPackageFragmentRoot} is accepted.
   * @return The test source folder having the given fqnOfRequiredType on the classpath or {@code null} if no such
   *         source folder could be found.
   */
  public static IPackageFragmentRoot getTestSourceFolder(IJavaProject orig, String fqnOfRequiredType) {
    var sourceFolder = getTestSourceFolderInProject(orig, fqnOfRequiredType);
    if (JdtUtils.exists(sourceFolder)) {
      return sourceFolder;
    }

    // search for a test project
    var testProjectSuffixes = new String[]{".test", ".tests", ".testing"};
    @SuppressWarnings("squid:S2259") // NPE
    var javaModel = orig.getJavaModel();
    for (var suffix : testProjectSuffixes) {
      var testProject = javaModel.getJavaProject(orig.getElementName() + suffix);
      sourceFolder = getTestSourceFolderInProject(testProject, fqnOfRequiredType);
      if (JdtUtils.exists(sourceFolder)) {
        return sourceFolder;
      }
    }
    return null;
  }

  private static IPackageFragmentRoot getTestSourceFolderInProject(IJavaProject project, String fqnOfRequiredType) {
    if (!JdtUtils.exists(project)) {
      return null;
    }

    try {
      if (Strings.hasText(fqnOfRequiredType) && !JdtUtils.exists(project.findType(fqnOfRequiredType))) {
        // it is not a test project (no dependency to required class)
        return null;
      }
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }

    return sourceFoldersOrdered(project)
        .filter(element -> element.getPath().removeFirstSegments(1).toString().toLowerCase(Locale.US).contains("test"))
        .findAny()
        .orElse(null);
  }

  public static Optional<IFile> findFileInWorkspace(URI uri) {
    if (uri == null) {
      return Optional.empty();
    }

    var workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    var files = workspaceRoot.findFilesForLocationURI(uri);
    if (files.length < 1) {
      return Optional.empty();
    }
    return Optional.of(files[0]);
  }

  public static Optional<IPackageFragmentRoot> primarySourceFolder(IJavaProject project) {
    return sourceFoldersOrdered(project).findAny();
  }

  public static Stream<IPackageFragmentRoot> sourceFoldersOrdered(IJavaProject project) {
    if (!JdtUtils.exists(project)) {
      return Stream.empty();
    }

    try {
      return Arrays.stream(project.getPackageFragmentRoots())
          .filter(ClasspathWithJdt::isJavaSourceFolder)
          .sorted(comparingInt(root -> JavaEnvironmentImplementor.priorityOfSourceFolder(root.getResource().getLocation().toFile().toPath())));
    }
    catch (JavaModelException e) {
      throw new SdkException(e);
    }
  }

  /**
   * Gets the preferred source folder for DTOs created in the {@link IJavaProject} of the given source folder.
   *
   * @param selectedSourceFolder
   *          The default source folder.
   * @return The given selectedSourceFolder or the src/generated/java folder within the same {@link IJavaProject} if it
   *         exists.
   */
  public static IPackageFragmentRoot getDtoSourceFolder(IPackageFragmentRoot selectedSourceFolder) {
    if (!JdtUtils.exists(selectedSourceFolder)) {
      return selectedSourceFolder;
    }
    var targetProject = selectedSourceFolder.getJavaProject();
    if (!JdtUtils.exists(targetProject)) {
      return selectedSourceFolder;
    }
    var generatedFolder = targetProject.getProject().getFolder(IScoutSourceFolders.GENERATED_SOURCE_FOLDER);
    if (generatedFolder == null || !generatedFolder.exists()) {
      return selectedSourceFolder;
    }
    var generatedSourceFolder = targetProject.getPackageFragmentRoot(generatedFolder);
    if (!JdtUtils.exists(generatedSourceFolder)) {
      return selectedSourceFolder;
    }
    return generatedSourceFolder;
  }

  /**
   * Gets the best scout session type on the classpath of the given {@link IJavaProject}.
   *
   * @param project
   *          The {@link IJavaProject} for which the accessible session should be searched.
   * @param tier
   *          The type of session.
   * @param monitor
   *          The progress monitor to use
   * @return The session {@link IType} or {@code null} if no session could be found.
   */
  public static Optional<IType> getSession(IJavaProject project, ScoutTier tier, IProgressMonitor monitor) {
    if (!JdtUtils.exists(project)) {
      return Optional.empty();
    }

    Predicate<IType> filter = new PublicPrimaryTypeFilter() {
      @Override
      public boolean test(IType candidate) {
        if (!super.test(candidate)) {
          return false;
        }
        try {
          return candidate.isClass();
        }
        catch (JavaModelException e) {
          throw new SdkException("Unable to check for flags in type '" + candidate.getFullyQualifiedName() + "'.", e);
        }
      }
    };

    var scoutApi = ApiHelper.scoutApiFor(project);
    if (scoutApi.isEmpty()) {
      return Optional.empty();
    }

    var sessionToFind = switch (tier) {
      case Server -> scoutApi.orElseThrow().IServerSession().fqn();
      case Client, HtmlUi -> scoutApi.orElseThrow().IClientSession().fqn();
      default -> scoutApi.orElseThrow().ISession().fqn();
    };
    var sessions = JdtUtils.findTypesInStrictHierarchy(project, sessionToFind, monitor, filter);

    if (sessions.isEmpty()) {
      return Optional.empty();
    }
    else if (sessions.size() == 1) {
      return Optional.of(sessions.iterator().next());
    }
    else {
      return Optional.ofNullable(findMostSpecific(sessions));
    }
  }

  private static IType findMostSpecific(Iterable<IType> candidates) {
    ITypeHierarchy superHierarchy = null;
    for (var t : candidates) {
      if (superHierarchy == null || !superHierarchy.contains(t)) {
        try {
          superHierarchy = t.newSupertypeHierarchy(null);
        }
        catch (JavaModelException e) {
          throw new SdkException("Cannot calculate supertype hierarchy for type {}.", t, e);
        }
      }
    }
    if (superHierarchy == null) {
      return null;
    }
    return superHierarchy.getType();
  }

  /**
   * Gets the content of the pom.xml file of the given {@link IProject} as {@link Document}.
   *
   * @param p
   *          The {@link IProject} for which the pom should be returned.
   * @return The {@link Document} holding the pom.xml contents.
   */
  public static Document getPomDocument(IProject p) {
    var pom = p.getFile(IMavenConstants.POM);
    return readXmlDocument(pom);
  }

  /**
   * Reads the given {@link IFile} into an XML {@link Document}.
   *
   * @param file
   *          The {@link IFile} that should be loaded. Must be an XML file!
   * @return a {@link Document} holding the contents of the {@link IFile} or {@code null} if the given {@link IFile}
   *         does not exist.
   */
  public static Document readXmlDocument(IFile file) {
    if (!file.exists()) {
      return null;
    }

    try {
      var docBuilder = Xml.createDocumentBuilder();
      //noinspection NestedTryStatement
      try (var in = file.getContents()) {
        return docBuilder.parse(in);
      }
    }
    catch (IOException | ParserConfigurationException | CoreException | SAXException e) {
      throw new SdkException(e);
    }
  }

  /**
   * Execute a Maven update on the given {@link IProject}s.
   *
   * @param projects
   *          The projects for which a Maven update should be performed.
   * @param updateSnapshots
   *          Specifies if an update of snapshot dependencies should be enforced.
   * @param updateConfig
   *          Specifies if the Eclipse project configuration should be updated based on the pom.xml
   * @param cleanProject
   *          Specifies if the project should be cleaned.
   * @param refreshFromDisk
   *          Specifies if the project should be refreshed from disk.
   * @param monitor
   *          The {@link IProgressMonitor} for the update operation.
   * @return A {@link Map} containing the project name as key and an {@link IStatus} describing the update result for
   *         the corresponding project.
   */
  public static Map<String, IStatus> mavenUpdate(Set<IProject> projects, boolean updateSnapshots, boolean updateConfig, boolean cleanProject, boolean refreshFromDisk, IProgressMonitor monitor) {
    if (projects == null || projects.isEmpty()) {
      return emptyMap();
    }
    var mavenPlugin = MavenPluginActivator.getDefault();
    if (mavenPlugin == null) {
      return emptyMap();
    }
    var configurationManager = (ProjectConfigurationManager) mavenPlugin.getProjectConfigurationManager();
    if (configurationManager == null) {
      return emptyMap();
    }
    var request = new MavenUpdateRequest(projects, false, updateSnapshots);
    if (monitor != null && monitor.isCanceled()) {
      return emptyMap();
    }
    return configurationManager.updateProjectConfiguration(request, updateConfig, cleanProject, refreshFromDisk, monitor);
  }
}
