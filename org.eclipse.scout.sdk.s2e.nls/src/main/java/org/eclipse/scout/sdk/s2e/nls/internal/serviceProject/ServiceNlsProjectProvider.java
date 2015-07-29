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
package org.eclipse.scout.sdk.s2e.nls.internal.serviceProject;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.model.INlsProjectProvider;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

public class ServiceNlsProjectProvider implements INlsProjectProvider {

  /**
   * Gets the text provider service types ordered by @Order.
   *
   * @param projectFilter
   *          List of project names. Only services that belong to these projects are returned. if null is passed, all
   *          services are returned.
   * @return
   * @throws CoreException
   */
  private static Set<IType> getRegisteredTextProviderTypes(final Set<String> projectFilter) throws CoreException {

    final class TextProviderServiceDeclaration {
      private final IType svc;
      private final double prio;

      private TextProviderServiceDeclaration(IType s, double p) {
        svc = s;
        prio = p;
      }
    }

    Comparator<TextProviderServiceDeclaration> comparator = new Comparator<TextProviderServiceDeclaration>() {
      @Override
      public int compare(TextProviderServiceDeclaration o1, TextProviderServiceDeclaration o2) {
        if (o2.prio != o1.prio) {
          return Double.valueOf(o1.prio).compareTo(Double.valueOf(o2.prio));
        }

        if (o1.svc.isBinary() != o2.svc.isBinary()) {
          // prefer source types
          return Boolean.valueOf(o1.svc.isBinary()).compareTo(Boolean.valueOf(o2.svc.isBinary()));
        }

        return o1.svc.getElementName().compareTo(o2.svc.getElementName());
      }
    };

    Set<TextProviderServiceDeclaration> result = new TreeSet<>(comparator);
    Set<IType> baseTypes = JdtUtils.resolveJdtTypes("org.eclipse.scout.rt.shared.services.common.text.AbstractDynamicNlsTextProviderService");
    for (IType t : baseTypes) {
      ITypeHierarchy typeHierarchy = t.newTypeHierarchy(null);
      for (IType candidate : typeHierarchy.getAllSubtypes(t)) {
        try {
          if (!Flags.isAbstract(candidate.getFlags()) && SourceRange.isAvailable(candidate.getSourceRange()) && acceptsFilter(projectFilter, candidate)) {
            // only accept non-abstract types with source available and fulfills the given project filter
            TextProviderServiceDeclaration d = new TextProviderServiceDeclaration(candidate, getPriority(candidate));
            result.add(d);
          }
        }
        catch (JavaModelException e) {
          // this element seems to be corrupt -> ignore
          NlsCore.logWarning("Attempt to access source range of type '" + candidate.getFullyQualifiedName() + "' failed. Type will be skipped.", e);
        }
      }
    }

    // return the types of the services ordered by priority
    Set<IType> returnValueSorted = new LinkedHashSet<>(result.size());
    for (TextProviderServiceDeclaration d : result) {
      returnValueSorted.add(d.svc);
    }
    return returnValueSorted;
  }

  private static boolean acceptsFilter(Set<String> projects, IType candidate) {
    if (candidate == null) {
      return false;
    }
    if (candidate.isReadOnly()) {
      return true; // always include all text services from the platform
    }
    if (projects == null) {
      return true; // no project filter and doc filter is valid -> filter matches
    }

    // check project filter
    return projects.contains(candidate.getJavaProject().getProject().getName());
  }

  private static double getPriority(IType registration) {
    // check class annotation
    try {
      IAnnotation a = JdtUtils.getAnnotation(registration, "org.eclipse.scout.commons.annotations.Order");
      Double val = JdtUtils.getAnnotationValueNumeric(a, "value");
      if (val != null) {
        return val.doubleValue();
      }
    }
    catch (Exception e) {
      //nop
    }

    // if nothing defined: default ranking = 0
    return 0.0;
  }

  private static ServiceNlsProject getServiceNlsProject(IType serviceType) throws JavaModelException {
    if (serviceType == null) {
      NlsCore.logError("nls service type cannot be null.");
      return null;
    }
    if (!JdtUtils.exists(serviceType)) {
      NlsCore.logError("nls service type '" + serviceType.getFullyQualifiedName() + "' does not exist.");
      return null;
    }

    NlsServiceType type = new NlsServiceType(serviceType);
    if (type.getTranslationsFolderName() == null) {
      NlsCore.logWarning("The NLS Service for Type '" + serviceType.getFullyQualifiedName() + "' could not be parsed. Ensure that the method '" + NlsServiceType.DYNAMIC_NLS_BASE_NAME_GETTER + "' is available and returns a String literal like \"resources.texts.Texts\" directly.");
      return null;
    }

    return new ServiceNlsProject(type);
  }

  private static INlsProject getNlsProjectTreeProjectName(Set<String> projectFilter) throws CoreException {
    Set<IType> registeredTextProviderTypes = getRegisteredTextProviderTypes(projectFilter);
    if (registeredTextProviderTypes == null) {
      return null;
    }
    return getNlsProjectTree(registeredTextProviderTypes);
  }

  private static INlsProject getNlsProjectTree(Set<IType> textProviderServices) throws CoreException {
    ServiceNlsProject previous = null;
    ServiceNlsProject root = null;
    for (IType type : textProviderServices) {
      ServiceNlsProject p = getServiceNlsProject(type);
      if (p != null) {
        // remember the first project (the one with the highest prio)
        if (root == null) {
          root = p;
        }

        // set the current as parent of the previous instance -> build up tree
        if (previous != null) {
          previous.setParent(p);
        }

        // remember the previous provider for the next iteration
        previous = p;
      }
      else if (root == null) {
        // first Type in the chain could not be parsed.
        // this is also the Type that e.g. the editor would show -> show error
        throw new CoreException(new Status(IStatus.ERROR, NlsCore.PLUGIN_ID, "The NLS Service for Type " + type.getFullyQualifiedName() + " could not be parsed."));
      }
    }
    return root;
  }

  private static Set<String> getScoutBundleNamesForProject(IJavaProject p) throws JavaModelException {
    String[] requiredProjectNames = p.getRequiredProjectNames();
    Set<String> result = new HashSet<>(requiredProjectNames.length + 1);
    result.add(p.getElementName());
    for (String s : requiredProjectNames) {
      result.add(s);
    }
    return result;
  }

  private static Set<String> getScoutBundleNamesForType(IType type) throws JavaModelException {
    IJavaProject javaProject = type.getJavaProject();
    return getScoutBundleNamesForProject(javaProject);
  }

  private static INlsProject getNlsProjectTree(IType type) throws CoreException {
    Set<IType> nlsProviders = getRegisteredTextProviderTypes(getScoutBundleNamesForType(type));
    if (nlsProviders == null) {
      return null;
    }

    String searchString = getTypeIdentifyer(type);
    Set<IType> filtered = new LinkedHashSet<>(nlsProviders.size());
    boolean minFound = false;
    for (IType t : nlsProviders) {
      if (getTypeIdentifyer(t).equals(searchString) && !minFound) {
        minFound = true;
      }

      if (minFound) {
        filtered.add(t);
      }
    }

    return getNlsProjectTree(filtered);
  }

  private static String getTypeIdentifyer(IType t) {
    return t.getJavaProject().getProject().getName() + "/" + t.getFullyQualifiedName();
  }

  @Override
  public INlsProject getProject(Object[] args) {
    if (args != null) {
      if (args.length == 1) {
        if (args[0] instanceof IType) {
          // text service type
          return getProjectByTextServiceType((IType) args[0]);
        }
        else if (args[0] instanceof IFile) {
          // text service file
          return getProjectByTextServiceFile((IFile) args[0]);
        }
      }
      else if (args.length == 2 && args[0] instanceof IType) {
        if (args[1] instanceof IJavaProject || args[1] == null) {
          // all text services in the given project with given kind (texts or normal)
          try {
            return getAllProjects(getScoutBundleNamesForProject((IJavaProject) args[1]));
          }
          catch (CoreException e) {
            NlsCore.logWarning("Could not load text provider services.", e);
          }
        }
        else if (args[1] instanceof IType) {
          try {
            // all text services with given kind available in the plugins defining the given type
            return getAllProjects(getScoutBundleNamesForType((IType) args[1]));
          }
          catch (CoreException e) {
            NlsCore.logWarning("Could not load text provider services for type '" + args[1].toString() + "'.", e);
          }
        }
      }
    }
    return null;
  }

  private static INlsProject getAllProjects(Set<String> wsBundles) {
    try {
      return getNlsProjectTreeProjectName(wsBundles);
    }
    catch (CoreException e) {
      NlsCore.logWarning("Could not load full text provider service tree.", e);
      return null;
    }
  }

  private static INlsProject getProjectByTextServiceFile(IFile f) {
    try {
      IType type = getITypeForFile(f);
      if (type != null) {
        return NlsCore.getNlsWorkspace().getNlsProject(new Object[]{type});
      }
    }
    catch (CoreException e) {
      NlsCore.logWarning("Could not load text provider services for file: " + f.getFullPath().toString(), e);
    }
    return null;
  }

  private static IType getITypeForFile(IFile file) throws JavaModelException {
    IJavaElement element = JavaCore.create(file);
    if (JdtUtils.exists(element)) {
      if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
        ICompilationUnit icu = (ICompilationUnit) element;
        IType[] types = icu.getTypes();
        if (types.length > 0) {
          return types[0];
        }
      }
      else if (element.getElementType() == IJavaElement.TYPE) {
        return (IType) element;
      }
    }
    return null;
  }

  private static INlsProject getProjectByTextServiceType(IType textservice) {
    try {
      return getNlsProjectTree(textservice);
    }
    catch (CoreException e) {
      NlsCore.logWarning("Could not load text provider services for " + textservice.getFullyQualifiedName(), e);
      return null;
    }
  }
}
