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
package org.eclipse.scout.sdk.operation.library;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.ClasspathComputer;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.util.ScoutResourceFilters;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper;
import org.eclipse.scout.sdk.util.pde.PluginModelHelper.ManifestPart;
import org.eclipse.scout.sdk.util.pde.ProductFileModelHelper;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.type.JavaElementComparator;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.osgi.framework.Version;

/**
 * <h3>{@link LibraryBundleCreateOperation}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 02.03.2012
 */
@SuppressWarnings("restriction")
public class LibraryBundleCreateOperation implements IOperation {

  private String m_bundleName;
  private Set<File> m_libraryFiles;
  private boolean m_unpack;
  private String m_fragmentHost;
  private IProject m_createdProject;
  private Set<IScoutBundle> m_libraryUserBundles;

  @Override
  public String getOperationName() {
    return Texts.get("CreateLibraryBundleOperationName", getBundleName());
  }

  @Override
  public void validate() {
    IStatus nameStatus = ScoutUtility.validateNewBundleName(getBundleName());
    if (nameStatus.matches(IStatus.ERROR)) {
      throw new IllegalArgumentException(nameStatus.getMessage());
    }
    else if (nameStatus.matches(IStatus.WARNING)) {
      ScoutSdk.logWarning("Create a library bundle with warning bundle name status - " + nameStatus.getMessage());
    }
    if (getLibraryFiles() == null || getLibraryFiles().isEmpty()) {
      throw new IllegalArgumentException("Library files can not be null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    m_createdProject = createProject(monitor, workingCopyManager);
    processLibraryUserBundles(m_createdProject);
  }

  protected IProject createProject(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getBundleName());
    project.create(monitor);
    project.open(monitor);
    // set the natures of the project
    IProjectDescription description = project.getDescription();

    description.setNatureIds(new String[]{JavaCore.NATURE_ID, org.eclipse.pde.internal.core.natures.PDE.PLUGIN_NATURE, ScoutSdk.LIBRARY_NATURE_ID});
    project.setDescription(description, monitor);
    IFolder manifestFolder = project.getFolder("META-INF");
    manifestFolder.create(true, false, monitor);
    IFile manifestFile = manifestFolder.getFile("MANIFEST.MF");
    manifestFile.create(new ByteArrayInputStream(new byte[0]), true, monitor);

    IFile buildPropertiesFile = project.getFile("build.properties");
    buildPropertiesFile.create(new ByteArrayInputStream(new byte[0]), true, monitor);
    PluginModelHelper helper = new PluginModelHelper(project);
    helper.BuildProperties.addBinaryBuildEntry(manifestFolder);
    fillManifest(helper);
    // add libraries
    IFolder libFolder = project.getFolder("lib");
    libFolder.create(true, false, monitor);
    for (File libFile : getLibraryFiles()) {
      IFile file = libFolder.getFile(libFile.getName());
      try {
        file.create(new FileInputStream(libFile), IFile.FORCE, monitor);
        helper.BuildProperties.addBinaryBuildEntry(file);
        helper.Manifest.addClasspathEntry(file);
      }
      catch (IOException e) {
        ScoutSdk.logError("could not create '" + file.getName() + "' in '" + libFolder.getProjectRelativePath() + "'.", e);
      }
    }

    helper.save();
    IPluginModelBase model = PluginRegistry.findModel(project);
    ClasspathComputer.setClasspath(project, model);
    TreeSet<IPackageFragment> packages = new TreeSet<IPackageFragment>(new JavaElementComparator());
    collectPackages(project, packages);
    for (IPackageFragment f : packages) {
      helper.Manifest.addExportPackage(f);
    }
    helper.save();

    return project;
  }

  private void fillManifest(PluginModelHelper helper) {
    ManifestPart manifest = helper.Manifest;
    manifest.setEntryValue("Manifest-Version", "1.0");
    manifest.setEntryValue("Bundle-ManifestVersion", "2");
    manifest.setVersion(new Version(1, 0, 0, "qualifier"));
    manifest.setEntryValue("Bundle-SymbolicName", getBundleName());
    if (getFragmentHost() != null) {
      manifest.setEntryValue("Fragment-Host", getFragmentHost());
    }
    manifest.setEntryValue("Bundle-RequiredExecutionEnvironment", JdtUtility.getDefaultJvmExecutionEnvironment());
  }

  private void collectPackages(IProject project, Collection<IPackageFragment> collector) throws JavaModelException {
    IJavaProject jp = JavaCore.create(project);
    IPackageFragmentRoot[] roots = jp.getPackageFragmentRoots();
    HashSet<String> names = new HashSet<String>();
    for (int j = 0; j < roots.length; j++) {
      if (roots[j].getKind() == IPackageFragmentRoot.K_SOURCE || (roots[j].getKind() == IPackageFragmentRoot.K_BINARY && !roots[j].isExternal())) {
        IJavaElement[] children = roots[j].getChildren();
        for (int k = 0; k < children.length; k++) {
          IPackageFragment f = (IPackageFragment) children[k];
          String name = f.getElementName();
          if ("".equals(name)) {
            name = ".";
          }
          if ((f.hasChildren() || f.getNonJavaResources().length > 0) && names.add(name)) {
            collector.add(f);
          }
        }
      }
    }
  }

  protected void processLibraryUserBundles(IProject libraryBundle) throws CoreException {
    Set<IScoutBundle> libraryUserBundles = getLibraryUserBundles();
    if (libraryUserBundles != null) {
      for (IScoutBundle libraryUser : libraryUserBundles) {
        // add dependency to manifest
        PluginModelHelper helper = new PluginModelHelper(libraryUser.getProject());
        helper.Manifest.addDependency(libraryBundle.getName());
        helper.save();

        // add the dependencies to the product files
        // find all product files in the current scout project.
        for (IResource productFile : ResourceUtility.getAllResources(ScoutResourceFilters.getProductFileFilter(libraryUser))) {
          ProductFileModelHelper h = new ProductFileModelHelper((IFile) productFile);
          // add library bundle if there is already the library owner bundle in it.
          if (h.ProductFile.existsDependency(libraryUser.getSymbolicName())) {
            h.ProductFile.addDependency(libraryBundle.getName());
            h.save();
          }
        }

      }
    }
  }

  public String getBundleName() {
    return m_bundleName;
  }

  public void setBundleName(String bundleName) {
    m_bundleName = bundleName;
  }

  public boolean isUnpack() {
    return m_unpack;
  }

  public void setUnpack(boolean unpack) {
    m_unpack = unpack;
  }

  public Set<File> getLibraryFiles() {
    return m_libraryFiles;
  }

  public void setLibraryFiles(Set<File> libraryFiles) {
    m_libraryFiles = libraryFiles;
  }

  public String getFragmentHost() {
    return m_fragmentHost;
  }

  public void setFragmentHost(String fragmentHost) {
    m_fragmentHost = fragmentHost;
  }

  public Set<IScoutBundle> getLibraryUserBundles() {
    return m_libraryUserBundles;
  }

  public void setLibraryUserBundles(Set<IScoutBundle> libraryUserBundles) {
    m_libraryUserBundles = libraryUserBundles;
  }

  // out variables
  public IProject getCreatedProject() {
    return m_createdProject;
  }
}
