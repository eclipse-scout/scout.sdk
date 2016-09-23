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
package org.eclipse.scout.sdk.s2e.nls.internal.serviceproject;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;
import org.eclipse.scout.sdk.core.s.ISdkProperties;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.nls.NlsCore;
import org.eclipse.scout.sdk.s2e.nls.internal.simpleproject.SimpleNlsProject;
import org.eclipse.scout.sdk.s2e.nls.model.INlsProjectProvider;
import org.eclipse.scout.sdk.s2e.nls.project.INlsProject;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;
import org.eclipse.scout.sdk.s2e.util.S2eUtils.PublicPrimaryTypeFilter;

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
  private static Set<IType> getRegisteredTextProviderTypes(IJavaProject javaProject) throws CoreException {

    final class TextProviderServiceDeclaration {
      private final IType m_svc;
      private final double m_prio;

      private TextProviderServiceDeclaration(IType s, double p) {
        m_svc = s;
        m_prio = p;
      }
    }

    final Set<String> duplicateOrders = new HashSet<>(0);

    Comparator<TextProviderServiceDeclaration> comparator = new Comparator<TextProviderServiceDeclaration>() {
      @Override
      public int compare(TextProviderServiceDeclaration o1, TextProviderServiceDeclaration o2) {
        if (o1 == o2) {
          return 0;
        }

        int compare = Double.compare(o1.m_prio, o2.m_prio);
        if (compare != 0) {
          return compare;
        }

        // log duplicate orders
        String[] duplicateOrdersFqn = new String[]{o1.m_svc.getFullyQualifiedName(), o2.m_svc.getFullyQualifiedName()};
        Arrays.sort(duplicateOrdersFqn);
        duplicateOrders.add(Arrays.toString(duplicateOrdersFqn));

        compare = Boolean.compare(o1.m_svc.isBinary(), o2.m_svc.isBinary());
        if (compare != 0) {
          // prefer source types
          return compare;
        }
        return o1.m_svc.getFullyQualifiedName().compareTo(o2.m_svc.getFullyQualifiedName());
      }
    };

    final Set<TextProviderServiceDeclaration> result = new TreeSet<>(comparator);
    Predicate<IType> filter = new PublicPrimaryTypeFilter() {
      @Override
      public boolean test(IType candidate) {
        boolean accept = super.test(candidate);
        if (!accept) {
          return false;
        }

        try {
          int flags = candidate.getFlags();
          if (Flags.isAbstract(flags)) {
            return false;
          }

          if (SourceRange.isAvailable(candidate.getSourceRange())) {
            // only accept non-abstract types with source available
            TextProviderServiceDeclaration d = new TextProviderServiceDeclaration(candidate, getOrder(candidate));
            result.add(d);
          }
        }
        catch (JavaModelException e) {
          // this element seems to be corrupt -> ignore
          SdkLog.warning("Attempt to access source range of type '{}' failed. Type will be skipped.", candidate.getFullyQualifiedName(), e);
        }
        return false;
      }
    };

    S2eUtils.findClassesInStrictHierarchy(javaProject, IScoutRuntimeTypes.AbstractDynamicNlsTextProviderService, null, filter);

    if (!duplicateOrders.isEmpty()) {
      SdkLog.warning("There are TextProviderServices with the same @Order value: {}", duplicateOrders);
    }

    // return the types of the services ordered by priority
    Set<IType> returnValueSorted = new LinkedHashSet<>(result.size());
    for (TextProviderServiceDeclaration d : result) {
      returnValueSorted.add(d.m_svc);
    }
    return returnValueSorted;
  }

  private static double getOrder(IType registration) {
    try {
      IAnnotation a = S2eUtils.getAnnotation(registration, IScoutRuntimeTypes.Order);
      if (S2eUtils.exists(a)) {
        BigDecimal val = S2eUtils.getAnnotationValueNumeric(a, "value");
        if (val != null) {
          return val.doubleValue();
        }
      }
    }
    catch (Exception e) {
      SdkLog.debug("Unable to parse order annotation value for type '{}'. Using default bean order instead.", registration.getFullyQualifiedName(), e);
    }

    // if nothing defined: default order
    return ISdkProperties.DEFAULT_BEAN_ORDER;
  }

  private static SimpleNlsProject getServiceNlsProject(IType serviceType) {
    if (serviceType == null) {
      SdkLog.error("nls service type cannot be null.");
      return null;
    }
    if (!S2eUtils.exists(serviceType)) {
      SdkLog.error("nls service type '{}' does not exist.", serviceType.getFullyQualifiedName());
      return null;
    }

    NlsServiceType type = new NlsServiceType(serviceType);
    if (type.getTranslationsFolderName() == null) {
      SdkLog.warning("The NLS Service for Type '{}' could not be parsed. Ensure that the method '{}' is available and returns a String literal like \"resources.texts.Texts\" directly.",
          serviceType.getFullyQualifiedName(), NlsServiceType.DYNAMIC_NLS_BASE_NAME_GETTER);
      return null;
    }

    return new SimpleNlsProject(type);
  }

  private static INlsProject getNlsProjectTree(IJavaProject projectFilter) throws CoreException {
    Set<IType> registeredTextProviderTypes = getRegisteredTextProviderTypes(projectFilter);
    return textProviderTypesToNlsProject(registeredTextProviderTypes);
  }

  private static INlsProject textProviderTypesToNlsProject(Set<IType> textProviderServices) throws CoreException {
    SimpleNlsProject previous = null;
    SimpleNlsProject root = null;
    for (IType type : textProviderServices) {
      SimpleNlsProject p = getServiceNlsProject(type);
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

  private static INlsProject getNlsProjectTree(IType type) throws CoreException {
    Set<IType> nlsProviders = getRegisteredTextProviderTypes(type.getJavaProject());
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

    return textProviderTypesToNlsProject(filtered);
  }

  private static String getTypeIdentifyer(IType t) {
    return t.getJavaProject().getProject().getName() + '/' + t.getFullyQualifiedName();
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
          return getAllProjects((IJavaProject) args[1]);
        }
        else if (args[1] instanceof IType) {
          // all text services with given kind available in the plugins defining the given type
          IType iType = (IType) args[1];
          return getAllProjects(iType.getJavaProject());
        }
      }
    }
    return null;
  }

  private static INlsProject getAllProjects(IJavaProject javaProject) {
    try {
      return getNlsProjectTree(javaProject);
    }
    catch (CoreException e) {
      SdkLog.warning("Could not load full text provider service tree.", e);
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
      SdkLog.warning("Could not load text provider services for file: {}", f.getFullPath().toString(), e);
    }
    return null;
  }

  private static IType getITypeForFile(IFile file) throws JavaModelException {
    IJavaElement element = JavaCore.create(file);
    if (S2eUtils.exists(element)) {
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
      SdkLog.warning("Could not load text provider services for {}", textservice.getFullyQualifiedName(), e);
      return null;
    }
  }
}
