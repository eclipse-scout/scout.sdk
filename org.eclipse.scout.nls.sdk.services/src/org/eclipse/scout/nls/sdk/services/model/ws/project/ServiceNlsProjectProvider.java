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
package org.eclipse.scout.nls.sdk.services.model.ws.project;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.nls.sdk.extension.INlsProjectProvider;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.internal.jdt.NlsJdtUtility;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.services.model.ws.NlsServiceType;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeUtility;

public class ServiceNlsProjectProvider implements INlsProjectProvider {

  public ServiceNlsProjectProvider() {
  }

  /**
   * @return All text provider services in the workspace.
   * @throws JavaModelException
   */
  public static Set<IType> getRegisteredTextProviderTypes() throws JavaModelException {
    Set<IType> registeredTextProviderTypes = getRegisteredTextProviderTypes(null);
    if (registeredTextProviderTypes != null) {
      return registeredTextProviderTypes;
    }
    return CollectionUtility.emptyHashSet();
  }

  /**
   * Gets the registered (in plugin.xml) text provider service types ordered by priority.
   *
   * @param returnDocServices
   *          If true, only Docs text provider services (implementing marker interface
   *          <code>IDocumentationTextProviderService</code>) are returned. Otherwise only non-docs text provider
   *          services are
   *          returned. If the parameter is null, all text services are returned.
   * @param projectFilter
   *          List of project names. Only services that belong to these projects are returned. if null is passed, all
   *          services are returned.
   * @return
   * @throws JavaModelException
   */
  private static Set<IType> getRegisteredTextProviderTypes(final Set<String> projectFilter) throws JavaModelException {

    final class TextProviderServiceDeclaration {
      private final IType svc;
      private final double prio;

      private TextProviderServiceDeclaration(IType s, double p) {
        svc = s;
        prio = p;
      }
    }

    IType superType = TypeUtility.getType(IRuntimeClasses.AbstractDynamicNlsTextProviderService);
    if (superType == null) {
      return null;
    }

    Set<IType> serviceImpls = TypeUtility.getPrimaryTypeHierarchy(superType).getAllSubtypes(superType, new ITypeFilter() {
      @Override
      public boolean accept(IType type) {
        try {
          if (Flags.isAbstract(type.getFlags())) {
            return false;
          }

          // only accept nls providers where source code is available. otherwise we cannot parse it anyway.
          ISourceRange range = type.getSourceRange();
          return range != null && range.getOffset() != -1;
          // don't use SourceRange.isAvailable() because this method does not exist on eclipse 3.5
        }
        catch (JavaModelException e) {
          // this element seems to be corrupt -> ignore
          NlsCore.logWarning("Attempt to access source range of type '" + type.getFullyQualifiedName() + "' failed. Type will be skipped.", e);
          return false;
        }
      }
    });

    Map<IType, TextProviderServiceDeclaration> result = new HashMap<>(serviceImpls.size());
    for (IType serviceImpl : serviceImpls) {
      if (acceptsFilter(projectFilter, serviceImpl)) {
        TextProviderServiceDeclaration d = new TextProviderServiceDeclaration(serviceImpl, getPriority(serviceImpl));

        // if the same service is registered more than once with different priorities: use always the one with the higher priority
        // meaning: if we have the current service with a higher prio in the list already -> do not overwrite with the new one.
        TextProviderServiceDeclaration existing = result.get(serviceImpl);
        if (existing == null || existing.prio < d.prio) {
          // we do not have this service or we have a service with lower prio -> overwrite
          result.put(serviceImpl, d);
        }
      }
    }

    // we have ensured that every service is registered with its highest prio -> sort all services by prio
    TextProviderServiceDeclaration[] sortedArrayLowestOrderFirst = result.values().toArray(new TextProviderServiceDeclaration[result.size()]);
    Arrays.sort(sortedArrayLowestOrderFirst, new Comparator<TextProviderServiceDeclaration>() {
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
    });

    // return the types of the services ordered by priority
    Set<IType> returnValueSorted = new LinkedHashSet<>(sortedArrayLowestOrderFirst.length);
    for (int i = 0; i < sortedArrayLowestOrderFirst.length; i++) {
      returnValueSorted.add(sortedArrayLowestOrderFirst[i].svc);
    }
    return returnValueSorted;
  }

  private static boolean acceptsFilter(Set<String> projects, IType candidate) throws JavaModelException {
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

  private static float getPriority(IType registration) {
    // check class annotation
    try {
      IAnnotation a = JdtUtility.getAnnotation(registration, IRuntimeClasses.Order);
      Double val = JdtUtility.getAnnotationValueNumeric(a, "value");
      if (val != null) {
        return val.floatValue();
      }
    }
    catch (Exception e) {
      //nop
    }

    // if nothing defined: default ranking = 0
    return 0.0f;
  }

  private ServiceNlsProject getServiceNlsProject(IType serviceType) {
    if (!TypeUtility.exists(serviceType)) {
      NlsCore.logError("nls service type '" + serviceType.getFullyQualifiedName() + "' does not exist");
      return null;
    }

    NlsServiceType type = new NlsServiceType(serviceType);
    if (type.getTranslationsFolderName() == null) {
      NlsCore.logWarning("The NLS Service for Type '" + serviceType.getFullyQualifiedName() + "' could not be parsed. Ensure that the method '"
          + NlsServiceType.DYNAMIC_NLS_BASE_NAME_GETTER + "' is available and returns a String literal like \"resources.texts.Texts\" directly.");
      return null;
    }

    return new ServiceNlsProject(type);
  }

  private INlsProject getNlsProjectTreeProjectName(Set<String> projectFilter) throws CoreException {
    Set<IType> registeredTextProviderTypes = getRegisteredTextProviderTypes(projectFilter);
    if (registeredTextProviderTypes == null) {
      return null;
    }
    return getNlsProjectTree(registeredTextProviderTypes);
  }

  private INlsProject getNlsProjectTree(Set<IType> textProviderServices) throws CoreException {
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
        throw new CoreException(new ScoutStatus("The NLS Service for Type " + type.getFullyQualifiedName() + " could not be parsed."));
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

  private INlsProject getNlsProjectTree(IType type) throws CoreException {
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

  private INlsProject getAllProjects(Set<String> wsBundles) {
    try {
      return getNlsProjectTreeProjectName(wsBundles);
    }
    catch (CoreException e) {
      NlsCore.logWarning("Could not load full text provider service tree.", e);
      return null;
    }
  }

  private INlsProject getProjectByTextServiceFile(IFile f) {
    try {
      IType type = NlsJdtUtility.getITypeForFile(f);
      if (type != null) {
        return NlsCore.getNlsWorkspace().getNlsProject(new Object[]{type});
      }
    }
    catch (CoreException e) {
      NlsCore.logWarning("Could not load text provider services for file: " + f.getFullPath().toString(), e);
    }
    return null;
  }

  private INlsProject getProjectByTextServiceType(IType textservice) {
    try {
      return getNlsProjectTree(textservice);
    }
    catch (CoreException e) {
      NlsCore.logWarning("Could not load text provider services for " + textservice.getFullyQualifiedName(), e);
      return null;
    }
  }
}
