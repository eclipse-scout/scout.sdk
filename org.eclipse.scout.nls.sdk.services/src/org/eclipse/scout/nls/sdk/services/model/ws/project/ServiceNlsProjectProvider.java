package org.eclipse.scout.nls.sdk.services.model.ws.project;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.nls.sdk.extension.INlsProjectProvider;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.internal.jdt.NlsJdtUtility;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.services.model.ws.NlsServiceType;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

@SuppressWarnings("restriction")
public class ServiceNlsProjectProvider implements INlsProjectProvider {

  public ServiceNlsProjectProvider() {
  }

  /**
   * @return All text provider services in the workspace.
   * @throws JavaModelException
   */
  public static Set<IType> getRegisteredTextProviderTypes() throws JavaModelException {
    return getRegisteredTextProviderTypes(null, null);
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
  private static Set<IType> getRegisteredTextProviderTypes(Boolean returnDocServices, final Set<String> projectFilter) throws JavaModelException {

    class TextProviderService {
      private final IType textProvider;
      private final String contributingBundleName;

      private TextProviderService(IType t) {
        textProvider = t;
        IScoutBundle b = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(t);
        contributingBundleName = b == null ? "" : b.getSymbolicName();
      }

      @Override
      public int hashCode() {
        int ret = 1;
        ret = ret * 27 + textProvider.getFullyQualifiedName().hashCode();
        ret = ret * 19 + contributingBundleName.hashCode();
        return ret;
      }

      @Override
      public boolean equals(Object obj) {
        if (obj instanceof TextProviderService) {
          TextProviderService o = (TextProviderService) obj;
          return textProvider.getFullyQualifiedName().equals(o.textProvider.getFullyQualifiedName()) &&
              contributingBundleName.equals(o.contributingBundleName);
        }
        else {
          return false;
        }
      }
    }

    class TextProviderServiceDeclaration {
      private final TextProviderService svc;
      private final double prio;

      private TextProviderServiceDeclaration(TextProviderService s, double p) {
        svc = s;
        prio = p;
      }
    }

    IType superType = TypeUtility.getType(IRuntimeClasses.AbstractDynamicNlsTextProviderService);
    if (superType == null) return null;

    Set<IType> serviceImpls = ScoutSdkCore.getHierarchyCache().getPrimaryTypeHierarchy(superType).getAllSubtypes(superType, new ITypeFilter() {
      @Override
      public boolean accept(IType type) {
        try {
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
    HashMap<String, IType> typeMap = new HashMap<String, IType>(serviceImpls.size());
    for (IType t : serviceImpls) {
      typeMap.put(t.getFullyQualifiedName(), t);
    }

    HashMap<TextProviderService, TextProviderServiceDeclaration> result = new HashMap<TextProviderService, TextProviderServiceDeclaration>(serviceImpls.size());
    IExtension[] allServiceExtensions = PDECore.getDefault().getExtensionsRegistry().findExtensions(IRuntimeClasses.EXTENSION_POINT_SERVICES, true);
    for (IExtension e : allServiceExtensions) {
      for (IConfigurationElement c : e.getConfigurationElements()) {
        if (IRuntimeClasses.EXTENSION_ELEMENT_SERVICE.equals(c.getName())) {
          String serviceClassDef = c.getAttribute("class");
          IType serviceImpl = typeMap.get(serviceClassDef);
          if (acceptsFilter(returnDocServices, projectFilter, serviceImpl)) {
            TextProviderService s = new TextProviderService(serviceImpl);
            TextProviderServiceDeclaration d = new TextProviderServiceDeclaration(s, getPriority(serviceImpl, c));

            // if the same service is registered more than once with different priorities: use always the one with the higher priority
            // meaning: if we have the current service with a higher prio in the list already -> do not overwrite with the new one.
            TextProviderServiceDeclaration existing = result.get(s);
            if (existing == null || existing.prio < d.prio) {
              // we do not have this service or we have a service with lower prio -> overwrite
              result.put(s, d);
            }
          }
        }
      }
    }

    // we have ensured that every service is registered with its highest prio -> sort all services by prio
    // use the level distance as second criteria
    TextProviderServiceDeclaration[] sortedArrayHighestPrioFirst = result.values().toArray(new TextProviderServiceDeclaration[result.size()]);
    Arrays.sort(sortedArrayHighestPrioFirst, new Comparator<TextProviderServiceDeclaration>() {
      @Override
      public int compare(TextProviderServiceDeclaration o1, TextProviderServiceDeclaration o2) {
        if (o2.prio != o1.prio) {
          return Double.valueOf(o2.prio).compareTo(Double.valueOf(o1.prio));
        }

        int i1 = getIndexOf(o1.svc.contributingBundleName, projectFilter);
        int i2 = getIndexOf(o2.svc.contributingBundleName, projectFilter);
        if (i1 != i2) {
          return Integer.valueOf(i1).compareTo(Integer.valueOf(i2));
        }

        if (o1.svc.textProvider.isBinary() != o2.svc.textProvider.isBinary()) {
          // prefer source types
          return Boolean.valueOf(o1.svc.textProvider.isBinary()).compareTo(Boolean.valueOf(o2.svc.textProvider.isBinary()));
        }

        return o1.svc.textProvider.getElementName().compareTo(o2.svc.textProvider.getElementName());
      }
    });

    // return the types of the services ordered by priority
    Set<IType> returnValueSorted = new LinkedHashSet<IType>(sortedArrayHighestPrioFirst.length);
    for (int i = 0; i < sortedArrayHighestPrioFirst.length; i++) {
      returnValueSorted.add(sortedArrayHighestPrioFirst[i].svc.textProvider);
    }
    return returnValueSorted;
  }

  private static int getIndexOf(String searchEleemnt, Set<String> list) {
    if (list != null && list.size() > 0) {
      int i = 0;
      for (String name : list) {
        if (CompareUtility.equals(searchEleemnt, name)) {
          return i;
        }
        i++;
      }
    }
    return -1;
  }

  private static boolean acceptsFilter(Boolean returnDocServices, Set<String> projects, IType candidate) throws JavaModelException {
    if (candidate == null) return false;
    boolean acceptsDocPart = returnDocServices == null || returnDocServices == isDocsService(candidate);
    if (acceptsDocPart) {
      if (candidate.isReadOnly()) return true; // always include all text services from the platform
      if (projects == null) return true; // no project filter and doc filter is valid -> filter matches

      // check project filter
      return projects.contains(candidate.getJavaProject().getProject().getName());
    }
    return false;
  }

  private static boolean isDocsService(IType service) throws JavaModelException {
    ITypeHierarchy th = ScoutSdkCore.getHierarchyCache().getSuperHierarchy(service);
    for (IType ifs : th.getAllSuperInterfaces(service)) {
      if (IRuntimeClasses.IDocumentationTextProviderService.equals(ifs.getFullyQualifiedName())) {
        return true;
      }
    }
    return false;
  }

  private static float getPriority(IType registration, IConfigurationElement config) {
    // first check plugin.xml definition for ranking
    String xmlRank = config.getAttribute(IRuntimeClasses.EXTENSION_SERVICE_RANKING);
    if (xmlRank != null && xmlRank.length() > 0) {
      try {
        return Float.parseFloat(xmlRank);
      }
      catch (NumberFormatException e) {
        //nop
      }
    }

    // second check class annotation
    try {
      IAnnotation a = JdtUtility.getAnnotation(registration, IRuntimeClasses.Ranking);
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

  private INlsProject getNlsProjectTree(boolean returnDocServices, Set<String> projectFilter) throws CoreException {
    return getNlsProjectTree(getRegisteredTextProviderTypes(returnDocServices, projectFilter));
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
        //NlsCore.logError("The NLS Service for Type " + type.getFullyQualifiedName() + " could not be parsed.");
        throw new CoreException(new ScoutStatus("The NLS Service for Type " + type.getFullyQualifiedName() + " could not be parsed."));
      }
    }
    return root;
  }

  private static Set<String> getProjectNames(Set<IScoutBundle> scoutBundles) {
    if (scoutBundles == null || scoutBundles.size() < 1) return null;

    Set<String> names = new HashSet<String>(scoutBundles.size());
    for (IScoutBundle b : scoutBundles) {
      names.add(b.getSymbolicName());
    }
    return names;
  }

  private static Set<IScoutBundle> getScoutBundlesForType(IType type) {
    IScoutBundle b = ScoutTypeUtility.getScoutBundle(type.getJavaProject());
    return getParentSharedBundlesFor(b);
  }

  private static Set<IScoutBundle> getParentSharedBundlesFor(IScoutBundle b) {
    if (b != null) {
      return b.getParentBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), true);
    }
    else {
      return null;
    }
  }

  private INlsProject getNlsProjectTree(IType type) throws CoreException {
    Set<IType> nlsProviders = getRegisteredTextProviderTypes(isDocsService(type), getProjectNames(getScoutBundlesForType(type)));
    if (nlsProviders == null) return null;

    String searchString = getTypeIdentifyer(type);
    Set<IType> filtered = new LinkedHashSet<IType>(nlsProviders.size());
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
        IType t1 = (IType) args[0];
        boolean returnDocServices = !(IRuntimeClasses.TEXTS.equals(t1.getFullyQualifiedName()));

        if (args[1] instanceof IScoutBundle || args[1] == null) {
          // all text services in the given project with given kind (texts or normal)
          return getAllProjects(returnDocServices, getParentSharedBundlesFor((IScoutBundle) args[1]));
        }
        else if (args[1] instanceof IType) {
          // all text services with given kind available in the plugins defining the given type
          return getAllProjects(returnDocServices, getScoutBundlesForType((IType) args[1]));
        }
      }
    }
    return null;
  }

  private INlsProject getAllProjects(boolean returnDocServices, Set<IScoutBundle> wsBundles) {
    try {
      return getNlsProjectTree(returnDocServices, getProjectNames(wsBundles));
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
