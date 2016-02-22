/*******************************************************************************
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.ScoutTier;
import org.eclipse.ui.IWorkingSet;

/**
 * <h3>{@link S2eUiUtils}</h3>
 *
 * @author Matthias Villiger
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

    Comparator<IJavaElement> elementComparator = new Comparator<IJavaElement>() {

      @Override
      public int compare(IJavaElement o1, IJavaElement o2) {
        int result = Integer.compare(getRanking(o1), getRanking(o2));
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
        try {
          ScoutTier tier = ScoutTier.valueOf(element);
          if (tier == null) {
            return 100;
          }

          switch (tier) {
            case Client:
              return 5;
            case HtmlUi:
              return 10;
            default:
              return 15;
          }
        }
        catch (JavaModelException ex) {
          throw new SdkException(ex);
        }
      }
    };

    return getPackageOfSelection(selection, elementComparator, ScoutTier.Client);
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
    Comparator<IJavaElement> elementComparator = new Comparator<IJavaElement>() {

      @Override
      public int compare(IJavaElement o1, IJavaElement o2) {
        int result = o1.getElementName().compareTo(o2.getElementName());
        if (result != 0) {
          return result;
        }
        return o1.toString().compareTo(o2.toString());
      }
    };

    return getPackageOfSelection(selection, elementComparator, ScoutTier.Shared);
  }

  private static PackageContainer getPackageOfSelection(ISelection selection, Comparator<IJavaElement> javaElementComparator, ScoutTier expected) {
    Set<IResource> selectedResources = getResourcesOfSelection(selection);
    PackageContainer result = new PackageContainer();
    if (selectedResources.isEmpty()) {
      return result;
    }

    try {
      List<IJavaElement> elements = new ArrayList<>(selectedResources.size());
      List<IJavaElement> unaccepted = new ArrayList<>(selectedResources.size());
      for (IResource r : selectedResources) {
        IJavaElement element = JavaCore.create(r);
        if (S2eUtils.exists(element)) {
          if (expected.equals(ScoutTier.valueOf(element))) {
            elements.add(element);
          }
          else {
            unaccepted.add(element);
          }
        }
      }

      if (elements.isEmpty() && !unaccepted.isEmpty()) {
        // try to convert from one tier to another
        PackageContainer unacceptedResult = new PackageContainer();
        fillContainer(unaccepted, unacceptedResult, javaElementComparator);
        if (S2eUtils.exists(unacceptedResult.getProject())) {
          ScoutTier foundTier = ScoutTier.valueOf(unacceptedResult.getProject());
          if (foundTier != null) {
            result.setProject(foundTier.convert(expected, unacceptedResult.getProject()));
            result.setSrcFolder(foundTier.convert(expected, unacceptedResult.getSrcFolder(), false));
            result.setPackage(foundTier.convert(expected, unacceptedResult.getPackage(), false));
          }
        }
      }
      else {
        fillContainer(elements, result, javaElementComparator);
      }
    }
    catch (Exception e) {
      SdkLog.warning("Unable to parse source folder of selection {}", selection, e);
    }

    return result;
  }

  private static void fillContainer(List<IJavaElement> candidates, PackageContainer result, Comparator<IJavaElement> javaElementComparator) throws JavaModelException {
    Collections.sort(candidates, javaElementComparator);
    IJavaElement element = candidates.get(0);

    // package
    result.setPackage((IPackageFragment) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT));

    // source folder
    IPackageFragmentRoot pckFragRoot = (IPackageFragmentRoot) element.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
    if (S2eUtils.exists(pckFragRoot) && pckFragRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
      result.setSrcFolder(pckFragRoot);
    }

    // java project
    IJavaProject javaProject = (IJavaProject) element.getAncestor(IJavaElement.JAVA_PROJECT);
    if (S2eUtils.exists(javaProject)) {
      result.setProject(javaProject);
      if (result.getSrcFolder() == null) {
        result.setSrcFolder(S2eUtils.getPrimarySourceFolder(javaProject));
      }
    }
  }

  /**
   * Gets the resources of the given {@link ISelection}. Only accessible resources are returned (
   * {@link IResource#isAccessible()}). If a {@link IWorkingSet} is selected, the resources of the working set are
   * returned.<br>
   * If the given selection is empty, the content of the currently active java editor is returned.
   *
   * @param selection
   *          The selection from which the {@link IResource}s should be extracted.
   * @return A {@link Set} with all selected {@link IResource}s. Never returns <code>null</code>.
   */
  public static Set<IResource> getResourcesOfSelection(ISelection selection) {
    if (!(selection instanceof IStructuredSelection) || selection.isEmpty()) {
      IJavaElement activeEditorJavaInput = EditorUtility.getActiveEditorJavaInput();
      if (!S2eUtils.exists(activeEditorJavaInput)) {
        return Collections.emptySet();
      }
      selection = new StructuredSelection(activeEditorJavaInput);
    }

    IStructuredSelection structSel = (IStructuredSelection) selection;
    Set<IResource> resourceSet = new LinkedHashSet<>(structSel.size());

    Iterator<?> iterator = structSel.iterator();
    while (iterator.hasNext()) {
      Object selElem = iterator.next();
      if (selElem instanceof IWorkingSet) {
        IWorkingSet workingSet = (IWorkingSet) selElem;
        if (workingSet.isEmpty() && workingSet.isAggregateWorkingSet()) {
          continue;
        }
        for (IAdaptable workingSetElement : workingSet.getElements()) {
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
    Object o = a.getAdapter(IResource.class);
    if (o instanceof IResource) {
      IResource resource = (IResource) o;
      if (resource.isAccessible()) {
        collector.add(resource);
      }
    }
  }
}