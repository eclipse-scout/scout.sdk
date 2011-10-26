package org.eclipse.scout.nls.sdk.services.model.ws.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.extension.INlsProjectProvider;
import org.eclipse.scout.nls.sdk.internal.jdt.NlsJdtUtility;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.nls.sdk.services.model.ws.NlsServiceType;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutStatus;
import org.eclipse.scout.sdk.internal.workspace.IScoutBundleConstantes;
import org.eclipse.scout.sdk.workspace.type.TypeUtility;

@SuppressWarnings("restriction")
public class ServiceNlsProjectProvider implements INlsProjectProvider {

  public ServiceNlsProjectProvider() {
  }

  public static IType[] getRegisteredTextProviderTypes() throws JavaModelException {
    return getRegisteredTextProviderTypes(null);
  }

  /**
   * Gets the registered (in plugin.xml) text provider service types ordered by priority.
   *
   * @param returnDocServices
   *          If true, only Docs text provider services (implementing marker interface
   *          <code>IDocumentationTextProviderService</code>) are returned. Otherwise only non-docs text provider
   *          services are
   *          returned. If the parameter is null, all text services are returned.
   * @return
   * @throws JavaModelException
   */
  public static IType[] getRegisteredTextProviderTypes(Boolean returnDocServices) throws JavaModelException {

    class TextProviderService {
      final IType textProvider;
      final IJavaProject project;

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
      final TextProviderService svc;
      final float prio;

      private TextProviderServiceDeclaration(TextProviderService s, float p) {
        svc = s;
        prio = p;
      }
    }

    IType superType = ScoutSdk.getType(RuntimeClasses.AbstractDynamicNlsTextProviderService);
    ITypeHierarchy nlsHierarchy = superType.newTypeHierarchy(new NullProgressMonitor());
    IType[] serviceImpls = nlsHierarchy.getAllSubtypes(superType);
    HashMap<TextProviderService, TextProviderServiceDeclaration> result = new HashMap<TextProviderService, TextProviderServiceDeclaration>(serviceImpls.length);
    IExtension[] allServiceExtensions = PDECore.getDefault().getExtensionsRegistry().findExtensions(IScoutBundleConstantes.EXTENSION_POINT_SERVICES, true);
    for (IExtension e : allServiceExtensions) {
      for (IConfigurationElement c : e.getConfigurationElements()) {
        for (IType serviceType : serviceImpls) {
          if (acceptsDocsFilter(returnDocServices, serviceType)) {
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

  private static boolean acceptsDocsFilter(Boolean returnDocServices, IType candidate) throws JavaModelException {
    if (returnDocServices == null) return true; // no filter

    return returnDocServices == isDocsService(candidate);
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

  private INlsProject getNlsProjectTree(boolean returnDocServices) throws CoreException {
    return getNlsProjectTree(getRegisteredTextProviderTypes(returnDocServices));
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
      } else if(root == null) {
    	  // first Type in the chain could not be parsed.
    	  // this is also the Type that e.g. the editor would show -> show error
    	  //NlsCore.logError("The NLS Service for Type " + type.getFullyQualifiedName() + " could not be parsed.");
    	  throw new CoreException(new ScoutStatus("The NLS Service for Type " + type.getFullyQualifiedName() + " could not be parsed."));
      }
    }
    return root;
  }

  private INlsProject getNlsProjectTree(IType type) throws CoreException {
    IType[] nlsProviders = getRegisteredTextProviderTypes(isDocsService(type));
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
    // this provider can handle:
    // - IFile: service class
    // - IType: service class
    // - IType: org.eclipse.scout.rt.shared.TEXTS class -> return all non-doc text services
    // - IType: org.eclipse.scout.rt.shared.services.common.text.IDocumentationTextProviderService -> return all doc text services
    if (args != null && args.length == 1) {
      if (args[0] instanceof IType) {
        IType t = (IType) args[0];
        if (RuntimeClasses.TEXTS.equals(t.getFullyQualifiedName())) {
          // expects the TEXTS class
          try {
            return getNlsProjectTree(false);
          }
          catch (CoreException e) {
            NlsCore.logWarning("Could not load full text provider service tree.", e);
          }
        }
        else if (RuntimeClasses.IDocumentationTextProviderService.equals(t.getFullyQualifiedName())) {
          // expects the IDocumentationTextProviderService class
          try {
            return getNlsProjectTree(true);
          }
          catch (CoreException e) {
            NlsCore.logWarning("Could not load full docs text provider service tree.", e);
          }
        }
        else {
          // expects the service class
          try {
            return getNlsProjectTree(t);
          }
          catch (CoreException e) {
            NlsCore.logWarning("Could not load text provider services for " + t.getFullyQualifiedName(), e);
          }
        }
      }
      else if (args[0] instanceof IFile) {
        IFile textProviderServiceFile = (IFile) args[0]; // expects the service class directly
        try {
          IType type = NlsJdtUtility.getITypeForFile(textProviderServiceFile);
          if (type != null) {
            return NlsCore.getNlsWorkspace().getNlsProject(new Object[]{type});
          }
        }
        catch (CoreException e) {
          NlsCore.logWarning("Could not load text provider services for file: " + textProviderServiceFile.getFullPath().toString(), e);
        }
      }
    }
    return null;
  }
}
