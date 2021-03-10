/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.util;

import static java.util.Collections.addAll;
import static java.util.Collections.emptySet;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IType;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.core.s.util.search.FileRange;
import org.eclipse.scout.sdk.core.s.util.search.IFileQueryResult;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.environment.EclipseEnvironment;
import org.eclipse.scout.sdk.s2e.util.EclipseWorkspaceWalker;
import org.eclipse.scout.sdk.s2e.util.EclipseWorkspaceWalker.WorkspaceFile;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.util.S2eScoutTier;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.search.internal.ui.text.FileSearchResult;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

/**
 * <h3>{@link S2eUiUtils}</h3>
 *
 * @since 5.2.0
 */
public final class S2eUiUtils {

  private S2eUiUtils() {
  }

  /**
   * Extracts the best matching client {@link IPackageFragment} and {@link IPackageFragmentRoot} from the given
   * {@link ISelection}.
   *
   * @param selection
   *          The selection from which the {@link PackageContainer} should be calculated.
   * @return The {@link PackageContainer} with the information about the given selection.
   */
  public static PackageContainer getClientPackageOfSelection(ISelection selection) {

    Comparator<IJavaElement> elementComparator = new Comparator<>() {

      @Override
      public int compare(IJavaElement o1, IJavaElement o2) {
        var result = Integer.compare(getRanking(o1), getRanking(o2));
        if (result != 0) {
          return result;
        }
        result = o1.getElementName().compareTo(o2.getElementName());
        if (result != 0) {
          return result;
        }
        return o1.toString().compareTo(o2.toString());
      }

      private int getRanking(IJavaElement element) {
        var tier = S2eScoutTier.valueOf(element);
        if (tier.isEmpty()) {
          return 100;
        }

        switch (tier.get().unwrap()) {
          case Client:
            return 5;
          case HtmlUi:
            return 10;
          default:
            return 15;
        }
      }
    };

    return getPackageOfSelection(selection, elementComparator, S2eScoutTier.wrap(ScoutTier.Client));
  }

  /**
   * Extracts the best matching shared {@link IPackageFragment} and {@link IPackageFragmentRoot} from the given
   * {@link ISelection}
   *
   * @param selection
   *          The selection from which the {@link PackageContainer} should be calculated.
   * @return The {@link PackageContainer} with the information about the given selection.
   */
  public static PackageContainer getSharedPackageOfSelection(ISelection selection) {
    var elementComparator = Comparator.comparing(IJavaElement::getElementName).thenComparing(Object::toString);
    return getPackageOfSelection(selection, elementComparator, S2eScoutTier.wrap(ScoutTier.Shared));
  }

  /**
   * Opens the given {@link IType} in the Java editor. Sets the focus in the editor to the given type.
   *
   * @param type
   *          The type to show.
   * @param activate
   *          {@code true} if the editor should be activated. {@code false} if not (e.g. if a modal dialog is open
   *          already).
   * @return returns the {@link IEditorPart} of the opened editor or {@code null} if the element could not be opened in
   *         the Java editor.
   */
  public static IEditorPart openInEditor(IType type, boolean activate) {
    if (type == null) {
      return null;
    }
    return openInEditor(EclipseEnvironment.toJdtType(type), activate);
  }

  /**
   * Opens the given {@link IJavaElement} in the Java editor. Sets the focus in the editor to the given element.
   *
   * @param je
   *          The element to show.
   * @param activate
   *          {@code true} if the editor should be activated. {@code false} if not (e.g. if a modal dialog is open
   *          already).
   * @return returns the {@link IEditorPart} of the opened editor or {@code null} if the element could not be opened in
   *         the Java editor.
   */
  public static IEditorPart openInEditor(IJavaElement je, boolean activate) {
    if (!JdtUtils.exists(je)) {
      return null;
    }

    try {
      return JavaUI.openInEditor(je, activate, true);
    }
    catch (PartInitException | JavaModelException ex) {
      SdkLog.info("Unable to open java editor for input '{}'.", je.getElementName(), ex);
    }
    return null;
  }

  /**
   * Opens the given {@link IFile} in the standard editor associated with this file type.
   *
   * @param f
   *          The file to show in the corresponding standard editor.
   * @param activate
   *          {@code true} if the editor should be activated. {@code false} if not (e.g. if a modal dialog is open
   *          already).
   * @return The opened {@link IEditorPart} or {@code null} if the editor could not be opened.
   */
  public static IEditorPart openInEditor(IFile f, boolean activate) {
    return openInEditor(f, null, activate);
  }

  /**
   * Opens the given {@link IFile} in the editor with the given Id.
   *
   * @param f
   *          The file to show in the given editor.
   * @param editorId
   *          The id of the editor to use.
   * @param activate
   *          {@code true} if the editor should be activated. {@code false} if not (e.g. if a modal dialog is open
   *          already).
   * @return The opened {@link IEditorPart} or {@code null} if the editor could not be opened.
   */
  public static IEditorPart openInEditor(IFile f, String editorId, boolean activate) {
    return openInEditor(f, null, editorId, activate);
  }

  /**
   * Opens the given {@link IEditorInput} in the editor with the given Id
   *
   * @param input
   *          The {@link IEditorInput} to open.
   * @param editorId
   *          The id of the editor to open.
   * @param activate
   *          {@code true} if the editor should be activated. {@code false} if not (e.g. if a modal dialog is open
   *          already).
   * @return The opened {@link IEditorPart} or {@code null} if the editor could not be opened.
   */
  public static IEditorPart openInEditor(IEditorInput input, String editorId, boolean activate) {
    return openInEditor(null, input, editorId, activate);
  }

  /**
   * Opens the given {@link Path} in the standard editor associated with this file type.
   *
   * @param file
   *          The file to show in the corresponding standard editor.
   * @param activate
   *          {@code true} if the editor should be activated. {@code false} if not (e.g. if a modal dialog is open
   *          already).
   * @return The opened {@link IEditorPart} or {@code null} if the editor could not be opened.
   */
  public static IEditorPart openInEditor(Path file, boolean activate) {
    if (file == null) {
      return null;
    }

    return S2eUtils.findFileInWorkspace(file.toUri())
        .map(f -> openInEditor(f, activate))
        .orElse(null);
  }

  private static IEditorPart openInEditor(IFile f, IEditorInput input, String editorId, boolean activate) {
    var activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    if (activeWorkbenchWindow == null) {
      return null;
    }

    var activePage = activeWorkbenchWindow.getActivePage();
    if (activePage == null) {
      return null;
    }

    try {
      if (input != null) {
        return IDE.openEditor(activePage, input, editorId, activate);
      }

      if (f != null) {
        if (editorId != null) {
          return IDE.openEditor(activePage, f, editorId, activate);
        }
        return IDE.openEditor(activePage, f, activate);
      }
    }
    catch (PartInitException e) {
      SdkLog.info("Unable to open editor for input '{}'.", f == null ? input : f, e);
    }
    return null;
  }

  private static PackageContainer getPackageOfSelection(ISelection selection, Comparator<IJavaElement> javaElementComparator, S2eScoutTier expected) {
    var selectedResources = getResourcesOfSelection(selection);
    var result = new PackageContainer();
    if (selectedResources.isEmpty()) {
      return result;
    }

    try {
      List<IJavaElement> elements = new ArrayList<>(selectedResources.size());
      List<IJavaElement> unaccepted = new ArrayList<>(selectedResources.size());
      for (var r : selectedResources) {
        var element = JavaCore.create(r);
        if (JdtUtils.exists(element)) {
          if (expected.test(element)) {
            elements.add(element);
          }
          else {
            unaccepted.add(element);
          }
        }
      }

      if (elements.isEmpty() && !unaccepted.isEmpty()) {
        // try to convert from one tier to another
        var unacceptedResult = new PackageContainer();
        fillContainer(unaccepted, unacceptedResult, javaElementComparator);
        if (JdtUtils.exists(unacceptedResult.getProject())) {
          var foundTier = S2eScoutTier.valueOf(unacceptedResult.getProject());
          if (foundTier.isPresent()) {
            var expectedTier = expected.unwrap();
            result.setProject(foundTier.get().convert(expectedTier, unacceptedResult.getProject()).orElse(null));
            result.setSrcFolder(foundTier.get().convert(expectedTier, unacceptedResult.getSrcFolder()).orElse(null));
            result.setPackage(foundTier.get().convert(expectedTier, unacceptedResult.getPackage()).orElse(null));
          }
        }
      }
      else {
        fillContainer(elements, result, javaElementComparator);
      }
    }
    catch (JavaModelException e) {
      SdkLog.warning("Unable to parse source folder of selection {}", selection, e);
    }

    return result;
  }

  private static void fillContainer(List<IJavaElement> candidates, PackageContainer result, Comparator<IJavaElement> javaElementComparator) throws JavaModelException {
    if (candidates.isEmpty()) {
      return;
    }

    candidates.sort(javaElementComparator);
    var element = candidates.get(0);

    // package
    result.setPackage((IPackageFragment) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT));

    // source folder
    var pckFragRoot = JdtUtils.getSourceFolder(element);
    if (JdtUtils.exists(pckFragRoot) && pckFragRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
      result.setSrcFolder(pckFragRoot);
    }

    // java project
    var javaProject = (IJavaProject) element.getAncestor(IJavaElement.JAVA_PROJECT);
    if (JdtUtils.exists(javaProject)) {
      result.setProject(javaProject);
      if (result.getSrcFolder() == null) {
        result.setSrcFolder(S2eUtils.primarySourceFolder(javaProject).orElse(null));
      }
    }
  }

  /**
   * Gets all types in the selection of the given event.
   *
   * @param event
   *          The even. Must not be {@code null}.
   * @return All types found in the selection of the given even.
   */
  public static Set<org.eclipse.jdt.core.IType> getTypesInEventSelection(ExecutionEvent event) {
    var selection = HandlerUtil.getCurrentSelection(event);
    var resourcesFromSelection = S2eUiUtils.getResourcesOfSelection(selection);
    if (resourcesFromSelection.isEmpty()) {
      return emptySet();
    }

    var types = S2eUiUtils.resourcesToTypes(resourcesFromSelection);
    if (types.isEmpty()) {
      return emptySet();
    }
    return types;
  }

  /**
   * Gets the resources of the given {@link ISelection}. Only accessible resources are returned (
   * {@link IResource#isAccessible()}). If a {@link IWorkingSet} is selected, the resources of the working set are
   * returned.<br>
   * If the given selection is empty, the content of the currently active java editor is returned.
   *
   * @param selection
   *          The selection from which the {@link IResource}s should be extracted.
   * @return A {@link Set} with all selected {@link IResource}s. Never returns {@code null}.
   */
  public static Set<IResource> getResourcesOfSelection(ISelection selection) {
    if (!(selection instanceof IStructuredSelection) || selection.isEmpty()) {
      var activeEditorJavaInput = EditorUtility.getActiveEditorJavaInput();
      if (!JdtUtils.exists(activeEditorJavaInput)) {
        return emptySet();
      }
      selection = new StructuredSelection(activeEditorJavaInput);
    }

    var structSel = (IStructuredSelection) selection;
    Set<IResource> resourceSet = new LinkedHashSet<>(structSel.size());
    for (var selElem : structSel) {
      if (selElem instanceof IWorkingSet) {
        var workingSet = (IWorkingSet) selElem;
        if (workingSet.isEmpty() && workingSet.isAggregateWorkingSet()) {
          continue;
        }
        for (var workingSetElement : workingSet.getElements()) {
          addAdaptableResource(workingSetElement, resourceSet);
        }
      }
      else if (selElem instanceof IAdaptable) {
        addAdaptableResource((IAdaptable) selElem, resourceSet);
      }
    }
    return resourceSet;
  }

  private static void addAdaptableResource(IAdaptable a, Collection<IResource> collector) {
    var resource = a.getAdapter(IResource.class);
    if (resource != null && resource.isAccessible()) {
      collector.add(resource);
    }
  }

  /**
   * Gets all types in the given resources.
   *
   * @param resources
   *          The resources. Must not be {@code null}.
   * @return The types found.
   */
  public static Set<org.eclipse.jdt.core.IType> resourcesToTypes(Collection<IResource> resources) {
    Set<org.eclipse.jdt.core.IType> result = new HashSet<>(resources.size());
    for (var r : resources) {
      if (r != null && r.isAccessible()) {
        var element = JavaCore.create(r);
        if (JdtUtils.exists(element)) {
          if (element.getElementType() == IJavaElement.TYPE) {
            result.add((org.eclipse.jdt.core.IType) element);
          }
          else if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
            var icu = (ICompilationUnit) element;
            try {
              addAll(result, icu.getTypes());
            }
            catch (JavaModelException e) {
              SdkLog.warning("Unable to wellform types in compilation unit {}. Skipping.", icu.getElementName(), e);
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * @see S2eUtils#getTestSourceFolder(IJavaProject, String)
   */
  public static IPackageFragmentRoot getTestSourceFolder(IJavaElement element, String fqnOfRequiredType, String testName) {
    if (!JdtUtils.exists(element)) {
      return null;
    }

    if (Strings.isBlank(testName)) {
      testName = "tests";
    }
    var javaProject = element.getJavaProject();
    try {
      var testSourceFolder = S2eUtils.getTestSourceFolder(javaProject, fqnOfRequiredType);
      if (!JdtUtils.exists(testSourceFolder)) {
        // no result found. log message
        logNoTestSourceFolderFound(javaProject, fqnOfRequiredType, testName);
        return null;
      }
      return testSourceFolder;
    }
    catch (RuntimeException e) {
      SdkLog.warning("Unable to calculate test source folder for project {}. No {} will be generated.", javaProject.getElementName(), testName, e);
      return null;
    }
  }

  private static void logNoTestSourceFolderFound(IJavaProject javaProject, String fqnOfRequiredType, String testName) {
    // check if it is because if the class requirement
    try {
      var sourceFolderIgnoringRequiredType = S2eUtils.getTestSourceFolder(javaProject, null);
      if (JdtUtils.exists(sourceFolderIgnoringRequiredType)) {
        SdkLog.warning("Could not find a test source folder for project '{}' having access to class '{}'. No {} will be generated.", javaProject.getElementName(), fqnOfRequiredType, testName);
      }
      return;
    }
    catch (RuntimeException e) {
      SdkLog.debug("Unable to get source folders for project '{}'.", javaProject.getElementName(), e);
    }

    SdkLog.warning("No test source folder could be found for project '{}'. No {} will be generated.", javaProject.getElementName(), testName);
  }

  /**
   * Tries to open the given url in the system default browser.
   *
   * @param url
   *          the url to show
   */
  public static void showUrlInBrowser(String url) {
    try {
      PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(URIUtil.fromString(url).toURL());
    }
    catch (PartInitException | MalformedURLException | URISyntaxException | RuntimeException e) {
      SdkLog.warning("Could not open default web browser.", e);
    }
  }

  /**
   * Adds all {@link FileRange}s to the {@link FileSearchResult}. The {@link FileSearchResult} is cleared before.
   *
   * @param from
   *          The data source
   * @param to
   *          The target
   * @see IFileQueryResult
   * @see EclipseWorkspaceWalker
   */
  public static void queryResultToSearchResult(Stream<? extends FileRange> from, FileSearchResult to) {
    to.removeAll();
    from
        .flatMap(S2eUiUtils::toEclipseMatch)
        .forEach(to::addMatch);
  }

  static Stream<Match> toEclipseMatch(FileRange range) {
    return new WorkspaceFile(range.file(), range.module(), StandardCharsets.UTF_8)
        .inWorkspace().stream()
        .filter(IFile::exists)
        .map(iFile -> new Match(iFile, range.start(), range.length()));
  }
}
