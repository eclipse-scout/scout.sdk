package org.eclipse.scout.nls.sdk.pde;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.internal.core.ibundle.IManifestHeader;
import org.eclipse.pde.internal.core.text.bundle.ExportPackageHeader;
import org.osgi.framework.Constants;

/**
 * Helper class for basic plugin model modifications.
 * 
 * @author mvi
 * @since 3.7.1
 */
@SuppressWarnings("restriction")
public class PluginModelModificationHelper {

  protected final LazyPluginModel m_model;

  /**
   * Creates a new helper that operates on the given project.<br>
   * The plugin model classes are only created on first use.
   * 
   * @param project
   *          The project to modify.
   */
  public PluginModelModificationHelper(IProject project) {
    m_model = new LazyPluginModel(project);
  }

  /**
   * Adds the given plugin to the dependencies of the project associated with this helper.<br>
   * The dependency is not re-exported and marked as non-optional.<br>
   * If a dependency for the given plugin already exists (even if the existing dependency has different options), this
   * method does nothing.<br>
   * This method is thread safe.
   * 
   * @param pluginId
   *          The plugin ID
   * @throws CoreException
   */
  public void addDependency(String pluginId) throws CoreException {
    addDependency(pluginId, false);
  }

  /**
   * Adds the given plugin to the dependencies of the project associated with this helper.<br>
   * The dependency is marked as non-optional.<br>
   * If a dependency for the given plugin already exists (even if the existing dependency has different options), this
   * method does nothing.<br>
   * This method is thread safe.
   * 
   * @param pluginId
   *          The plugin ID
   * @param reexport
   *          If the dependency should be re-exported.
   * @throws CoreException
   */
  public void addDependency(String pluginId, boolean reexport) throws CoreException {
    addDependency(pluginId, false, false);
  }

  /**
   * Adds the given plugin to the dependencies of the project associated with this helper.<br>
   * If a dependency for the given plugin already exists (even if the existing dependency has different options), this
   * method does nothing.<br>
   * This method is thread safe.
   * 
   * @param pluginId
   *          The plugin ID
   * @param reexport
   *          If the dependency should be re-exported.
   * @param optional
   *          If the dependency should be optional
   * @return the created import or null is already exists.
   * @throws CoreException
   */
  public IPluginImport addDependency(String pluginId, boolean reexport, boolean optional) throws CoreException {
    synchronized (m_model.getProject()) { // synchronized on project level to ensure a dependency is not added twice.
      for (IPluginImport existing : m_model.getPluginBase().getImports()) {
        if (existing.getId().equals(pluginId)) {
          return null; // exists already
        }
      }

      IPluginImport imp = m_model.getBundlePluginModel().createImport(pluginId);
      imp.setReexported(reexport);
      imp.setOptional(optional);
      m_model.getPluginBase().add(imp);
      return imp;
    }
  }

  /**
   * saves all changes.
   */
  public void save() {
    m_model.getBundlePluginModel().save();
  }

  /**
   * Adds a simple extension to the project associated with this helper.<br>
   * If exactly the same extension already exists, it is added another time.<br>
   * A simple extension is of the following format:<br>
   * <code>
   * &lt;extension point="extensionPointId"&gt;<br>
   * &nbsp;&nbsp;&lt;elementName attribute1="value1" attribute2="value2"&gt;&lt;/elementName&gt;<br>
   * &lt;/extension&gt;<br>
   * </code>
   * 
   * @param extensionPointId
   *          The id of the extension point for which the extension should be added.
   * @param elementName
   *          The tag name of the extension.
   * @param attributes
   *          The attribute (name-value-pairs) that should be added to the element.
   * @throws CoreException
   */
  public void addSimpleExtension(String extensionPointId, String elementName, Map<String, String> attributes) throws CoreException {
    IPluginExtension pe = m_model.getBundlePluginModel().createExtension();
    pe.setPoint(extensionPointId);
    IPluginElement extension = m_model.getBundlePluginModel().createElement(pe);
    extension.setName(elementName);
    for (Map.Entry<String, String> entry : attributes.entrySet()) {
      extension.setAttribute(entry.getKey(), entry.getValue());
    }
    pe.add(extension);
    m_model.getBundlePluginModel().getExtensions().add(pe);
  }

  /**
   * Adds the given package to the exported packages of the project associated with this helper.<br>
   * If the given package is already in the exported list, this method does nothing.
   * 
   * @param packageName
   *          The fully qualified name of the package.
   * @throws CoreException
   */
  public void addExportPackage(String packageName) throws CoreException {
    IManifestHeader header = m_model.getBundle().getManifestHeader(Constants.EXPORT_PACKAGE);
    ExportPackageHeader expHeader = (ExportPackageHeader) m_model.getBundleModel().getFactory().createHeader(Constants.EXPORT_PACKAGE, header.getValue() == null ? "" : header.getValue());
    if (expHeader.getPackage(packageName) == null) {
      expHeader.addPackage(packageName);
      m_model.getBundle().setHeader(Constants.EXPORT_PACKAGE, expHeader.getValue());
    }
  }
}
