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
package org.eclipse.scout.nls.sdk.internal.jdt;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.actions.FindReadReferencesAction;
import org.eclipse.jdt.ui.actions.FindReadReferencesInWorkingSetAction;
import org.eclipse.jdt.ui.actions.FindReferencesAction;
import org.eclipse.jdt.ui.actions.OpenNewClassWizardAction;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.internal.ui.dialog.NlsStatusDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkingSet;

@SuppressWarnings("restriction")
public final class NlsJdtUtility {

  private NlsJdtUtility() {
  }

  public static CompilationUnit getCompilationUnitASTForRead(ICompilationUnit icu) {
    if (icu == null) return null;

    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setSource(icu);
    return (CompilationUnit) parser.createAST(null);
  }

  public static List<IClasspathEntry> getSourceLocations(IJavaProject project) throws JavaModelException {
    List<IClasspathEntry> sourceLocations = new LinkedList<IClasspathEntry>();
    IClasspathEntry[] clEntries = project.getRawClasspath();
    for (IClasspathEntry entry : clEntries) {
      if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
        sourceLocations.add(entry);
      }
    }
    return sourceLocations;
  }

  public static boolean createFolder(IContainer folder, boolean recursively, IProgressMonitor monitor) throws CoreException {
    if (!folder.exists()) {
      createFolder(folder.getParent(), recursively, monitor);
      if (folder instanceof IFolder) {
        ((IFolder) folder).create(true, false, monitor);
      }
    }
    return true;
  }

  public static IType getITypeForFile(IFile file) {
    try {
      IProject project = file.getProject();
      if (project.hasNature(JavaCore.NATURE_ID)) {
        IJavaProject jp = JavaCore.create(project);
        List<IClasspathEntry> sourceLocs = getSourceLocations(jp);
        String filePath = file.getProjectRelativePath().toString();
        for (IClasspathEntry entry : sourceLocs) {
          String pref = entry.getPath().lastSegment();
          if (filePath.startsWith(pref)) {
            filePath = filePath.substring(pref.length() + 1);
            continue;
          }
        }
        String javaFileSuffix = ".java";
        filePath = filePath.replace("/", ".");
        if (filePath.toLowerCase().endsWith(javaFileSuffix)) {
          filePath = filePath.substring(0, filePath.length() - javaFileSuffix.length());
        }
        IType type = jp.findType(filePath);

        if (type == null) {
          NlsCore.logWarning("could not find an IType for: " + file.getName());
        }
        return type;
      }
    }
    catch (Exception e) {
      NlsCore.logWarning(e);
    }
    return null;
  }

  public static boolean isDescendant(IType clazz, Class<?> superclass) throws JavaModelException {
    return isDescendant(clazz, superclass, null);
  }

  /**
   * @param descendant
   *          ,
   *          the type that we are inspecting
   * @param ancestor
   *          ,
   *          the interface or type that we are checking against
   * @return true iff descendant implements/extends ancestor. there are some limitations to consider: if the
   *         extension/implementation is done by a class that is not in the current classpath, then this method returns
   *         false even though descendant does in fact extend/implement ancestor.
   * @throws JavaModelException
   */
  public static boolean isDescendant(IType clazz, Class<?> superclass, IProgressMonitor monitor)
      throws JavaModelException {
    IType[] types = clazz.newSupertypeHierarchy(monitor).getAllSupertypes(clazz);
    for (IType type : types) {
      if (superclass.getCanonicalName().equals(type.getFullyQualifiedName())) {
        return true;
      }
    }
    return false;
  }

  public static void checkJavaElement(IJavaElement element) {
    if (element == null || !element.exists()) {
      IllegalArgumentException e = new IllegalArgumentException("JavaElement must exist and not be null");

      throw e;
    }
  }

  /**
   * @param fullyQuallifiedField
   *          (e.g. com.bsiag.nls.JdNlsPlugin.PLUGIN_ID)
   * @param requestor
   * @param monitor
   * @throws CoreException
   */
  public static void findReferences(String fullyQuallifiedField, SearchRequestor requestor, IProgressMonitor monitor)
      throws CoreException {
    findReferences(fullyQuallifiedField, IJavaSearchConstants.FIELD, requestor, monitor);
  }

  /**
   * @param className
   * @param seachType
   *          one of {@value IJavaSearchConstants#FIELD}, {@value IJavaSearchConstants#PACKAGE},
   *          {@value IJavaSearchConstants#TYPE}, {@value IJavaSearchConstants#METHOD},
   *          {@value IJavaSearchConstants#CONSTRUCTOR}
   * @param collector
   * @param monitor
   */

  public static void findReferences(String fullyQuallifiedField, int searchType, SearchRequestor collector,
      IProgressMonitor monitor) throws CoreException {

    List<IJavaProject> jProjects = new LinkedList<IJavaProject>();
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    for (IProject project : projects) {
      if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID) && project.hasNature(PDE.PLUGIN_NATURE)) {
        jProjects.add(JavaCore.create(project));
      }
    }
    IJavaSearchScope scope = SearchEngine.createJavaSearchScope(jProjects.toArray(new IJavaElement[jProjects.size()]),
        true);

    SearchPattern pattern = SearchPattern.createPattern(fullyQuallifiedField, searchType,
        IJavaSearchConstants.REFERENCES, SearchPattern.R_EXACT_MATCH);
    SearchEngine engine = new SearchEngine();

    try {

      engine.search(pattern, new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()}, scope, collector,
          monitor);
    }
    catch (CoreException e) {
      NlsCore.logWarning(e);
    }
  }

  public static void findReferences(IJavaElement element, SearchRequestor requestor, IProgressMonitor monitor)
      throws CoreException {
    List<IJavaProject> jProjects = new LinkedList<IJavaProject>();
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    for (IProject project : projects) {
      if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID) && project.hasNature(PDE.PLUGIN_NATURE)) {
        jProjects.add(JavaCore.create(project));
      }
    }
    IJavaSearchScope scope = SearchEngine.createJavaSearchScope(jProjects.toArray(new IJavaElement[jProjects.size()]),
        true);

    SearchPattern pattern = SearchPattern.createPattern(element, IJavaSearchConstants.REFERENCES);
    SearchEngine engine = new SearchEngine();

    try {

      engine.search(pattern, new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()}, scope, requestor,
          monitor);
    }
    catch (CoreException e) {
      NlsCore.logWarning(e);
    }
  }

  // public static IStatus findHierarchy(IJavaElement element, SearchRequestor requestor, IProgressMonitor monitor)
  // throws CoreException{
  // SearchPattern pattern= SearchPattern.createPattern(element, IJavaSearchConstants.);
  // JavaSearchScopeFactory factory= JavaSearchScopeFactory.getInstance();
  // IJavaSearchScope scope= SearchEngine.createHierarchyScope(type);
  // String description= factory.getHierarchyScopeDescription(type);
  // return new ElementQuerySpecification(element, getLimitTo(), scope, description);
  // }
  //
  public static IStatus findReferencesForeground(IWorkbenchSite site, IJavaElement element,
      Collection<IProject> projects) {
    FindReferencesAction action = null;
    if (projects == null) {
      action = new FindReadReferencesAction(site);
    }
    else {
      action = new FindReadReferencesInWorkingSetAction(site, new IWorkingSet[]{new NlsWorkingSet(projects)});
    }

    action.run(element);
    return Status.OK_STATUS;
    // NewSearchUI.activateSearchResultView();
    // NewSearchUI.runQueryInForeground(PlatformUI.getWorkbench().getProgressService(), query)
  }

  public static IStatus renameField(IField field, String newName, Shell shell, IRunnableContext context) {
    RenameSupport support;
    try {
      support = RenameSupport.create(field, newName, RenameSupport.UPDATE_TEXTUAL_MATCHES | RenameSupport.UPDATE_REFERENCES);
      IStatus preCheckStatus = support.preCheck();
      if (preCheckStatus.getSeverity() == IStatus.OK) {
        support.perform(shell, context);
        return Status.OK_STATUS;
      }
      else {
        new NlsStatusDialog(shell, preCheckStatus).open();
        return preCheckStatus;
      }

    }
    catch (Exception e) {
      NlsCore.logWarning(e);
      NlsCore.logError("rename of: " + field.getElementName() + " to " + newName + " failed!", e);
      return new Status(IStatus.ERROR, NlsCore.PLUGIN_ID, IStatus.OK, "Rename Error", e);
    }
  }

  public static IStatus openOrcreateJavaFile(IProject project, String name) throws CoreException {
    if (project.hasNature(JavaCore.NATURE_ID)) {
      IJavaProject javaProject = JavaCore.create(project);
      IJavaElement result = null;
      if (name.length() > 0) result = javaProject.findType(name);
      if (result != null) JavaUI.openInEditor(result);
      else {
        OpenNewClassWizardAction action = new OpenNewClassWizardAction();
        action.run();
      }
      return Status.OK_STATUS;
    }
    // TODO create the correct status
    return Status.OK_STATUS;
  }

  public static List<IPackageFragment> getPluginPackages(IProject project) {
    IJavaProject jp = JavaCore.create(project);
    return getPluginPackages(jp);
  }

  /**
   * Returns all packages in any of the project's source folders.
   * 
   * @param jProject
   * @return
   */
  public static List<IPackageFragment> getPluginPackages(IJavaProject jProject) {
    List<IPackageFragment> proposals = new LinkedList<IPackageFragment>();
    try {
      for (IClasspathEntry entry : jProject.getRawClasspath()) {
        if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
          for (IPackageFragmentRoot fragRoot : jProject.findPackageFragmentRoots(entry)) {
            for (IJavaElement ele : fragRoot.getChildren()) {
              if (ele instanceof IPackageFragment) {
                proposals.add((IPackageFragment) ele);
              }
            }
          }
        }
      }
      return proposals;
    }
    catch (JavaModelException e) {
      NlsCore.logWarning(e);
      return new LinkedList<IPackageFragment>();
    }
  }
}
