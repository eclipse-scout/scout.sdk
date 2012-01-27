package org.eclipse.scout.nls.sdk.services.model.ws.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.scout.nls.sdk.extension.INlsProjectProvider;
import org.eclipse.scout.nls.sdk.internal.NlsCore;
import org.eclipse.scout.nls.sdk.internal.jdt.NlsJdtUtility;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.services.model.ws.NlsServiceType;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.internal.workspace.IScoutBundleConstantes;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.TypeCacheAccessor;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;

@SuppressWarnings("restriction")
public class ServiceNlsProjectProvider implements INlsProjectProvider {

  public ServiceNlsProjectProvider() {
  }

  /**
   * @return All text provider services in the workspace.
   * @throws JavaModelException
   */
  public static IType[] getRegisteredTextProviderTypes() throws JavaModelException {
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
  private static IType[] getRegisteredTextProviderTypes(Boolean returnDocServices, String[] projectFilter) throws JavaModelException {

    class TextProviderService {
      private final IType textProvider;
      private final IJavaProject project;

      private TextProviderService(IType t) {
        textProvider = t;
        project = t.getJavaProject();
      }

      @Override
      public int hashCode() {
        int ret = 1;
        ret = ret * 27 + textProvider.getFullyQualifiedName().hashCode();
        ret = ret * 19 + project.getElementName().hashCode();
        return ret;
      }

      @Override
      public boolean equals(Object obj) {
        if (obj instanceof TextProviderService) {
          TextProviderService o = (TextProviderService) obj;
          return textProvider.getFullyQualifiedName().equals(o.textProvider.getFullyQualifiedName()) &&
              project.getElementName().equals(o.project.getElementName());
        }
        else {
          return false;
        }
      }
    }

    class TextProviderServiceDeclaration {
      private final TextProviderService svc;
      private final float prio;

      private TextProviderServiceDeclaration(TextProviderService s, float p) {
        svc = s;
        prio = p;
      }
    }

    IType superType = TypeUtility.getType(RuntimeClasses.AbstractDynamicNlsTextProviderService);
    if (superType == null) return null;

    IType[] serviceImpls = TypeCacheAccessor.getHierarchyCache().getPrimaryTypeHierarchy(superType).getAllSubtypes(superType);
    HashMap<TextProviderService, TextProviderServiceDeclaration> result = new HashMap<TextProviderService, TextProviderServiceDeclaration>(serviceImpls.length);
    IExtension[] allServiceExtensions = PDECore.getDefault().getExtensionsRegistry().findExtensions(IScoutBundleConstantes.EXTENSION_POINT_SERVICES, true);
    for (IExtension e : allServiceExtensions) {
      for (IConfigurationElement c : e.getConfigurationElements()) {
        for (IType serviceType : serviceImpls) {
          if (acceptsFilter(returnDocServices, projectFilter, serviceType)) {
            if (IScoutBundleConstantes.EXTENSION_ELEMENT_SERVICE.equals(c.getName())) {
              String serviceClassDef = c.getAttribute("class");
              if (serviceClassDef != null && serviceClassDef.equals(serviceType.getFullyQualifiedName())) {
                TextProviderService s = new TextProviderService(serviceType);
                TextProviderServiceDeclaration d = new TextProviderServiceDeclaration(s, getPriority(serviceType, c));

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
      }
    }

    // we have ensured that every service is registered with its highest prio -> sort all services by prio
    TextProviderServiceDeclaration[] sortedArrayHighestPrioFirst = result.values().toArray(new TextProviderServiceDeclaration[result.size()]);
    Arrays.sort(sortedArrayHighestPrioFirst, new Comparator<TextProviderServiceDeclaration>() {
      @Override
      public int compare(TextProviderServiceDeclaration o1, TextProviderServiceDeclaration o2) {
        return new Float(o2.prio).compareTo(new Float(o1.prio));
      }
    });

    // return the types of the services ordered by priority
    IType[] returnValueSorted = new IType[sortedArrayHighestPrioFirst.length];
    for (int i = 0; i < returnValueSorted.length; i++) {
      returnValueSorted[i] = sortedArrayHighestPrioFirst[i].svc.textProvider;
    }
    return returnValueSorted;
  }

  private static boolean acceptsFilter(Boolean returnDocServices, String[] projects, IType candidate) throws JavaModelException {
    boolean acceptsDocPart = returnDocServices == null || returnDocServices == isDocsService(candidate);
    if (acceptsDocPart) {
      if (candidate.isReadOnly()) return true; // always include all text services from the platform
      if (projects == null) return true; // no project filter and doc filter is valid -> filter matches

      // check project filter
      for (String p : projects) {
        if (candidate.getJavaProject().getProject().getName().equals(p)) return true;
      }
    }
    return false;
  }

  private static boolean isDocsService(IType service) throws JavaModelException {
    String docsInterfaceClassName = RuntimeClasses.IDocumentationTextProviderService.substring(RuntimeClasses.IDocumentationTextProviderService.lastIndexOf('.') + 1);
    for (String ifs : service.getSuperInterfaceNames()) {
      if (docsInterfaceClassName.equals(ifs)) {
        return true;
      }
    }
    return false;
  }

  private static float getPriority(IType registration, IConfigurationElement config) {
    // first check plugin.xml definition for ranking
    String xmlRank = config.getAttribute(IScoutBundleConstantes.EXTENSION_SERVICE_RANKING);
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
      IAnnotation a = registration.getAnnotation("org.eclipse.scout.commons.annotations.Priority");
      if (a != null) {
        for (IMemberValuePair mvp : a.getMemberValuePairs()) {
          if ("value".equals(mvp.getMemberName()) && mvp.getValue() instanceof Float) {
            return ((Float) mvp.getValue()).floatValue();
          }
        }
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

  private INlsProject getNlsProjectTree(boolean returnDocServices, String[] projectFilter) throws CoreException {
    return getNlsProjectTree(getRegisteredTextProviderTypes(returnDocServices, projectFilter));
  }

  private INlsProject getNlsProjectTree(IType[] textProviderServices) throws CoreException {
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

  private static String[] getProjectNames(IScoutBundle[] scoutBundles) {
    if (scoutBundles == null || scoutBundles.length < 1) return null;

    String[] ret = new String[scoutBundles.length];
    for (int i = 0; i < scoutBundles.length; i++) {
      ret[i] = scoutBundles[i].getBundleName();
    }
    return ret;
  }

  private static void addBundlesRec(IScoutProject p, HashSet<IScoutBundle> collector) {
    for (IScoutBundle b : p.getAllScoutBundles()) {
      collector.add(b);
    }
    for (IScoutProject subP : p.getSubProjects()) {
      addBundlesRec(subP, collector);
    }
  }

  private static IScoutBundle[] getScoutBundlesForType(IType type) {
    IScoutBundle b = ScoutSdkCore.getScoutWorkspace().getScoutBundle(type.getJavaProject().getProject());
    if (b != null) {
      IScoutProject root = b.getScoutProject();
      return getScoutBundlesForProject(root);
    }
    else {
      return null;
    }
  }

  private static IScoutBundle[] getScoutBundlesForProject(IScoutProject p) {
    if (p == null) return null;
    HashSet<IScoutBundle> collector = new HashSet<IScoutBundle>();

    // find root scout project of the given project (may be a sub project).
    IScoutProject root = p;
    while (root.getParentProject() != null) {
      root = root.getParentProject();
    }

    // collect all bundles that belong to the root project that belongs to the project p.
    // assume that all these bundles together build the application so all text services of these projects should be available.
    addBundlesRec(root, collector);
    return collector.toArray(new IScoutBundle[collector.size()]);
  }

  private INlsProject getNlsProjectTree(IType type) throws CoreException {
    IType[] nlsProviders = getRegisteredTextProviderTypes(isDocsService(type), getProjectNames(getScoutBundlesForType(type)));
    if (nlsProviders == null) return null;

    String searchString = getTypeIdentifyer(type);
    ArrayList<IType> filtered = new ArrayList<IType>(nlsProviders.length);
    boolean minFound = false;
    for (IType t : nlsProviders) {
      if (getTypeIdentifyer(t).equals(searchString) && !minFound) {
        minFound = true;
      }

      if (minFound) {
        filtered.add(t);
      }
    }

    return getNlsProjectTree(filtered.toArray(new IType[filtered.size()]));
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
        boolean returnDocServices = !(RuntimeClasses.TEXTS.equals(t1.getFullyQualifiedName()));

        if (args[1] instanceof IScoutProject || args[1] == null) {
          // all text services in the given project with given kind (texts or normal)
          return getAllProjects(returnDocServices, getScoutBundlesForProject((IScoutProject) args[1]));
        }
        else if (args[1] instanceof IType) {
          // all text services with given kind available in the plugins defining the given type
          return getAllProjects(returnDocServices, getScoutBundlesForType((IType) args[1]));
        }
      }
    }
    return null;
  }

  private INlsProject getAllProjects(boolean returnDocServices, IScoutBundle[] wsBundles) {
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
