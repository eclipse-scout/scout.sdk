package org.eclipse.scout.sdk.compatibility.internal;

import java.util.Hashtable;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

public class ScoutCompatibilityActivator extends Plugin {
  public final static String PLUGIN_ID = "org.eclipse.scout.sdk.compatibility";
  public final static String EXT_NAME = "activator";
  public final static String EXT_ATTRIB_CLASS_NAME = "class";
  public final static String EXT_ATTRIB_VERSION_NAME = "platformVersion";

  private static ScoutCompatibilityActivator instance;

  private BundleContext m_context;

  public static ScoutCompatibilityActivator getDefault() {
    return instance;
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    instance = this;
    m_context = context;

    IExtensionRegistry reg = Platform.getExtensionRegistry();
    IExtensionPoint xp = reg.getExtensionPoint(ScoutCompatibilityActivator.PLUGIN_ID, EXT_NAME);
    IExtension[] extensions = xp.getExtensions();
    for (IExtension extension : extensions) {
      IConfigurationElement[] elements = extension.getConfigurationElements();
      for (IConfigurationElement element : elements) {
        String versions = element.getAttribute(EXT_ATTRIB_VERSION_NAME);
        if (matchesAnyVersion(versions)) {
          ICompatibilityActivator o = (ICompatibilityActivator) element.createExecutableExtension(EXT_ATTRIB_CLASS_NAME);
          o.start();
        }
      }
    }
  }

  private static boolean matchesAnyVersion(String versions) {
    if (versions == null || versions.length() < 1) return false;
    String[] compatibleVersions = versions.split(",");
    for (String v : compatibleVersions) {
      if (matchesVersion(v)) {
        return true;
      }
    }
    return false;
  }

  private static boolean matchesVersion(String version) {
    if (version == null) return false;
    version = version.trim();
    if (version.length() == 0) return false;

    Version pv = PlatformVersionUtility.getPlatformVersion();
    String[] segments = version.split("\\.");
    if (segments.length == 0) return false;
    String[] platformVersion = new String[]{"" + pv.getMajor(), "" + pv.getMinor(), "" + pv.getMicro(), pv.getQualifier()};

    for (int i = 0; i < Math.min(segments.length, platformVersion.length); i++) {
      if (!platformVersion[i].equals(segments[i])) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    instance = null;
    m_context = null;
    super.stop(context);
  }

  public Object acquireService(String type) {
    ServiceReference reference = m_context.getServiceReference(type);
    try {
      if (reference == null) {
        return null;
      }
      Object service = m_context.getService(reference);
      return service;
    }
    finally {
      m_context.ungetService(reference);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Object> T acquireCompatibilityService(Class<T> type) {
    ServiceReference[] references = null;
    try {
      references = m_context.getServiceReferences(type.getName(), null);
    }
    catch (InvalidSyntaxException e) {
    }
    if (references == null || references.length < 1) return null;
    if (references.length != 1) {
      throw new RuntimeException("more than one service found for " + type.getName());
    }

    ServiceReference reference = references[0];
    Object service = m_context.getService(reference);
    if (service != null) {
      m_context.ungetService(reference);
    }
    return (T) service;
  }

  public <T extends Object> void registerService(Class<T> type, T service) {
    m_context.registerService(type.getName(), service, new Hashtable(0));
  }
}
