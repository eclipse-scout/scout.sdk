package org.eclipse.scout.sdk.util.pde;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.pde.core.build.IBuildEntry;
import org.eclipse.pde.core.plugin.IPluginAttribute;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.internal.core.ibundle.IManifestHeader;
import org.eclipse.pde.internal.core.text.bundle.BundleClasspathHeader;
import org.eclipse.pde.internal.core.text.bundle.ExportPackageHeader;
import org.eclipse.pde.internal.core.text.bundle.RequireBundleHeader;
import org.eclipse.scout.commons.CompareUtility;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * Helper class for basic plugin model operations.
 * 
 * @author mvi
 * @since 3.8.0
 */
@SuppressWarnings("restriction")
public class PluginModelHelper {

  /**
   * Class for operations executed on the MANIFEST.MF part of a plugin model.
   */
  public final ManifestPart Manifest;

  /**
   * Class for operations executed on the plugin.xml part of a plugin model.
   */
  public final PluginXmlPart PluginXml;

  /**
   * Class for operations executed on the build.properties part of a plugin model.
   */
  public final BuildPropertiesPart BuildProperties;

  /**
   * Creates a new helper that operates on the workspace project with the same name as denoted by the given model base.
   * 
   * @param pluginModelBase
   *          The model base that defines the project name.
   * @throws IllegalArgumentException
   *           when no project with the same name as denoted by the given model description can be found in the
   *           workspace or is not a valid plugin project.
   */
  public PluginModelHelper(IPluginModelBase pluginModelBase) {
    this(pluginModelBase.getBundleDescription().getName());
  }

  /**
   * Creates a new helper that operates on the workspace project with given name.
   * 
   * @param projectName
   *          The name of the project.
   * @throws IllegalArgumentException
   *           when no project with the given name can be found in the workspace or is not a valid plugin project.
   */
  public PluginModelHelper(String projectName) {
    this(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName));
  }

  /**
   * Creates a new helper that operates on the given project.
   * 
   * @param project
   *          The project to modify.
   * @throws IllegalArgumentException
   *           when the project is null or is not a valid plugin project.
   */
  public PluginModelHelper(IProject project) {
    LazyPluginModel model = new LazyPluginModel(project);
    Manifest = new ManifestPart(model);
    PluginXml = new PluginXmlPart(model);
    BuildProperties = new BuildPropertiesPart(model);
  }

  /**
   * Gets the project associated with this helper.
   * 
   * @return The project this helper was created with.
   */
  public IProject getProject() {
    return Manifest.m_model.getProject();
  }

  private static String getProjectRelativeResourcePath(IResource r) {
    if (r == null) return null;
    String entry = r.getProjectRelativePath().toString();
    if (r.getType() == IResource.FOLDER) {
      if (!entry.endsWith("/")) {
        entry = entry + "/";
      }
    }
    return entry;
  }

  public static class ManifestPart {

    private final LazyPluginModel m_model;

    private ManifestPart(LazyPluginModel m) {
      m_model = m;
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
     * If the given plugin is null, empty or a dependency for the given plugin already exists
     * (even if the existing dependency has different options), this method does nothing.<br>
     * This method is thread safe.
     * 
     * @param pluginId
     *          The plugin ID
     * @param reexport
     *          If the dependency should be re-exported.
     * @param optional
     *          If the dependency should be optional
     * @return the new created import for the given pluginId if it did not exist or the already existing import.
     * @throws CoreException
     */
    public void addDependency(String pluginId, boolean reexport, boolean optional) throws CoreException {
      if (pluginId == null || pluginId.length() < 1) return;
      synchronized (m_model.getProject()) {
        if (!existsDependency(pluginId)) {
          IPluginImport imp = m_model.getBundlePluginModel().createImport(pluginId);
          imp.setReexported(reexport);
          imp.setOptional(optional);
          m_model.getPluginBase().add(imp);
        }
      }
    }

    /**
     * Removes the given plugin from the dependency list of the project associated with this helper.<br>
     * 
     * @param pluginId
     *          The plugin id to remove.
     * @throws CoreException
     */
    public void removeDependency(String pluginId) throws CoreException {
      synchronized (m_model.getProject()) {
        IPluginImport existing = getDependency(pluginId);
        if (existing != null) {
          m_model.getPluginBase().remove(existing);

          RequireBundleHeader reqHeader = getRequireBundleHeader();
          reqHeader.removeBundle(pluginId);
          setEntryValue(Constants.REQUIRE_BUNDLE, reqHeader.getValue());
        }
      }
    }

    private RequireBundleHeader getRequireBundleHeader() {
      IManifestHeader header = m_model.getBundle().getManifestHeader(Constants.REQUIRE_BUNDLE);
      return (RequireBundleHeader) m_model.getBundleModel().getFactory().createHeader(Constants.REQUIRE_BUNDLE, header.getValue() == null ? "" : header.getValue());
    }

    /**
     * Checks whether the given plugin id is already in the dependency list of the project associated with this helper.
     * 
     * @param pluginId
     *          The plugin id to check
     * @return true if the given plugin is already in the dependency list, false otherwise.
     */
    public boolean existsDependency(String pluginId) {
      IPluginImport existing = getDependency(pluginId);
      return existing != null;
    }

    /**
     * Returns all dependencies defined in the plug-in project associated with this helper.
     * 
     * @return an array of import objects
     */
    public IPluginImport[] getAllDependencies() {
      return m_model.getPluginBase().getImports();
    }

    private IPluginImport getDependency(String pluginId) {
      if (pluginId == null || pluginId.length() < 1) return null;
      for (IPluginImport existing : m_model.getPluginBase().getImports()) {
        if (existing.getId().equals(pluginId)) {
          return existing;
        }
      }
      return null;
    }

    /**
     * Checks whether the given package is already exported.
     * 
     * @param pck
     *          The package name to check.
     * @return true, if the given package is exported, false otherwise.
     */
    public boolean existsExportPackage(IPackageFragment pck) {
      if (pck == null) return false;
      return existsExportPackage(pck.getElementName());
    }

    /**
     * Checks whether the given package name is already exported.
     * 
     * @param packageName
     *          The package name to check.
     * @return true, if the given package is exported, false otherwise.
     */
    public boolean existsExportPackage(String packageName) {
      if (packageName == null || packageName.length() < 1) return false;
      ExportPackageHeader expHeader = getExportPackageHeader();
      return existsExportPackage(packageName, expHeader);
    }

    private boolean existsExportPackage(String packageName, ExportPackageHeader expHeader) {
      return expHeader.getPackage(packageName) != null;
    }

    /**
     * Adds the given package to the exported packages of the project associated with this helper.<br>
     * If the given package is null, empty or already in the exported list, this method does nothing.<br>
     * This method is thread safe.
     * 
     * @param packageName
     *          The fully qualified name of the package.
     */
    public void addExportPackage(String packageName) {
      if (packageName == null || packageName.length() < 1) return;
      synchronized (m_model.getProject()) {
        ExportPackageHeader expHeader = getExportPackageHeader();
        if (!existsExportPackage(packageName, expHeader)) {
          expHeader.addPackage(packageName);
          setEntryValue(Constants.EXPORT_PACKAGE, expHeader.getValue());
        }
      }
    }

    /**
     * Adds the given package to the exported packages of the project associated with this helper.<br>
     * If the given package is already in the exported list, this method does nothing.
     * 
     * @param pck
     *          The package to add
     */
    public void addExportPackage(IPackageFragment pck) {
      addExportPackage(pck.getElementName());
    }

    /**
     * Removes the given package from the exported packages list of the project associated with this helper.<br>
     * This method does nothing if the give package is null.
     * 
     * @param pck
     *          The package to remove
     */
    public void removeExportPackage(IPackageFragment pck) {
      if (pck == null) return;
      removeExportPackage(pck.getElementName());
    }

    /**
     * Removes the given package from the exported packages list of the project associated with this helper.<br>
     * This method does nothing if the give package is null or empty.
     * 
     * @param packageName
     *          The fully qualified name of the package
     */
    public void removeExportPackage(String packageName) {
      if (packageName == null || packageName.length() < 1) return;
      ExportPackageHeader expHeader = getExportPackageHeader();
      expHeader.removePackage(packageName);
      setEntryValue(Constants.EXPORT_PACKAGE, expHeader.getValue());
    }

    private ExportPackageHeader getExportPackageHeader() {
      IManifestHeader header = m_model.getBundle().getManifestHeader(Constants.EXPORT_PACKAGE);
      return (ExportPackageHeader) m_model.getBundleModel().getFactory().createHeader(Constants.EXPORT_PACKAGE, header.getValue() == null ? "" : header.getValue());
    }

    private BundleClasspathHeader getBundleClasspathHeader() {
      IManifestHeader header = m_model.getBundle().getManifestHeader(Constants.BUNDLE_CLASSPATH);
      return (BundleClasspathHeader) m_model.getBundleModel().getFactory().createHeader(Constants.BUNDLE_CLASSPATH, header.getValue() == null ? "" : header.getValue());
    }

    /**
     * Creates or updates the given manifest header key to the given value.<br>
     * This method does nothing if the given key is null or empty.
     * 
     * @param key
     *          The key. Will be created if it does not exist.
     * @param value
     *          The value that should be set for the given key. if value is null, the key is removed from the manifest.
     */
    public void setEntryValue(String key, String value) {
      if (key == null || key.length() < 1) return;
      m_model.getBundle().setHeader(key, value);
    }

    /**
     * Removes the given key from the manifest.<br>
     * This method does nothing if the given key is null or empty.
     * 
     * @param key
     *          The key to remove.
     */
    public void removeEntry(String key) {
      setEntryValue(key, null);
    }

    /**
     * Gets the value of the given manifest header key.
     * 
     * @param key
     *          The property name.
     * @return The value (or null if it does not exist) of the given key.
     */
    public String getEntry(String key) {
      return m_model.getBundle().getHeader(key);
    }

    /**
     * Gets all classpath entries of the project associated with this helper.
     * 
     * @return an array with all entries.
     */
    @SuppressWarnings("unchecked")
    public String[] getClasspathEntries() {
      BundleClasspathHeader h = getBundleClasspathHeader();
      Vector<String> names = h.getElementNames();
      return names.toArray(new String[names.size()]);
    }

    /**
     * Removes the given entry from the classpath.<br>
     * If a null resource is passed, this method does nothing.
     * 
     * @param resource
     *          The entry to remove.
     */
    public void removeClasspathEntry(IResource resource) {
      if (resource == null) return;
      removeClasspathEntry(getProjectRelativeResourcePath(resource));
    }

    /**
     * Removes the given entry from the classpath.<br>
     * If a null entry or empty entry is passed, this method does nothing.
     * 
     * @param entry
     *          The entry to remove.
     */
    public void removeClasspathEntry(String entry) {
      if (entry == null || entry.length() < 1) return;

      BundleClasspathHeader h = getBundleClasspathHeader();
      h.removeLibrary(entry);
      setEntryValue(Constants.BUNDLE_CLASSPATH, h.getValue());
    }

    /**
     * Adds the default classpath entry (".") to the classpath list.<br>
     * If the default entry does already exist, this method does nothing.
     */
    public void addClasspathDefaultEntry() {
      addClasspathEntry(".");
    }

    /**
     * Checks whether the given entry is already in the classpath entries of the project associated with this helper.
     * 
     * @param resource
     *          The entry to search
     * @return false if the resource is null or does not exist, true otherwise.
     */
    public boolean existsClasspathEntry(IResource resource) {
      if (resource == null) return false;
      return existsClasspathEntry(getProjectRelativeResourcePath(resource));
    }

    /**
     * Checks whether the given entry is already in the classpath entries of the project associated with this helper.
     * 
     * @param entry
     *          The entry to search.
     * @return true if the given entry already exists, false otherwise.
     */
    public boolean existsClasspathEntry(String entry) {
      if (entry == null || entry.length() < 1) return false;
      for (String e : getClasspathEntries()) {
        if (e.equals(entry)) return true;
      }
      return false;
    }

    /**
     * Adds the given classpath entry to the classpath of the project associated with this helper.<br>
     * If the resource is null, does not exist in the project or is already in the classpath, this method does nothing.<br>
     * This method is thread safe.
     * 
     * @param resource
     *          The classpath entry to add.
     */
    public void addClasspathEntry(IResource resource) {
      if (resource == null) return;
      if (!resource.exists()) return;
      addClasspathEntry(getProjectRelativeResourcePath(resource));
    }

    /**
     * Adds the given classpath entry to the classpath of the project associated with this helper.<br>
     * If the given entry is null, an empty string or already exists in the classpath, this method does nothing.<br>
     * This method is thread safe.
     * 
     * @param entry
     *          The classpath entry to add.
     */
    public void addClasspathEntry(String entry) {
      if (entry == null || entry.length() < 1) return;
      synchronized (m_model.getProject()) {
        if (!existsClasspathEntry(entry)) {
          BundleClasspathHeader h = getBundleClasspathHeader();
          h.addLibrary(entry);
          setEntryValue(Constants.BUNDLE_CLASSPATH, h.getValue());
        }
      }
    }

    /**
     * Sets the version of the plugin to the given value.<br>
     * If the given value is null, this method does nothing.
     * 
     * @param newVersion
     *          the new version
     */
    public void setVersion(Version newVersion) {
      if (newVersion == null) return;
      setVersion(newVersion.toString());
    }

    /**
     * Sets the version of the plugin to the given value.<br>
     * If the given value is empty or null, this method does nothing.
     * 
     * @param newVersion
     *          the new version
     */
    public void setVersion(String newVersion) {
      if (newVersion == null || newVersion.length() < 1) return;
      setEntryValue(Constants.BUNDLE_VERSION, newVersion);
    }

    /**
     * Gets the version of the plugin.
     * 
     * @return The version of this plugin.
     */
    public Version getVersion() {
      return Version.parseVersion(getVersionAsString());
    }

    /**
     * Gets the version of the plugin.
     * 
     * @return The version of the plugin.
     */
    public String getVersionAsString() {
      return getEntry(Constants.BUNDLE_VERSION);
    }

    /**
     * Gets the MANIFEST.MF file of the plugin.
     * 
     * @return
     */
    public IFile getFile() {
      return m_model.getManifestFile();
    }
  }

  /**
   * saves all changes of all underlying models that require saving.
   */
  public void save() {
    // same lazy model instance for all parts -> does not matter which one is saved.
    Manifest.m_model.save();
  }

  public static class PluginXmlPart {
    private final LazyPluginModel m_model;

    private PluginXmlPart(LazyPluginModel m) {
      m_model = m;
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
     *          The full id of the extension point for which the extension should be added.
     * @param elementName
     *          The tag name of the extension.
     * @param attributes
     *          The attribute (name-value-pairs) that should be added to the element.
     * @throws CoreException
     */
    public void addSimpleExtension(String extensionPointId, String elementName, Map<String, String> attributes) throws CoreException {
      IPluginExtension pe = null;
      // find existing extension
      for (IPluginExtension existing : m_model.getExtensionsModel().getExtensions().getExtensions()) {
        if (existing.getPoint().equals(extensionPointId)) {
          pe = existing;
          break;
        }
      }
      if (pe == null) {
        // no extension for given extension point exists: create new
        pe = m_model.getExtensionsModel().createExtension();
        pe.setPoint(extensionPointId);
        m_model.getExtensionsModel().getExtensions().add(pe);
      }

      IPluginElement extension = m_model.getExtensionsModel().createElement(pe);
      extension.setName(elementName);
      if (attributes != null && attributes.size() > 0) {
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
          extension.setAttribute(entry.getKey(), entry.getValue());
        }
      }
      pe.add(extension);
    }

    /**
     * Adds a simple extension to the project associated with this helper.<br>
     * If exactly the same extension already exists, it is added another time.<br>
     * A simple extension is of the following format:<br>
     * <code>
     * &lt;extension point="extensionPointId"&gt;<br>
     * &nbsp;&nbsp;&lt;elementName&gt;&lt;/elementName&gt;<br>
     * &lt;/extension&gt;<br>
     * </code>
     * 
     * @param extensionPointId
     *          The full id of the extension point for which the extension should be added.
     * @param elementName
     *          The tag name of the extension.
     * @throws CoreException
     */
    public void addSimpleExtension(String extensionPointId, String elementName) throws CoreException {
      addSimpleExtension(extensionPointId, elementName, null);
    }

    /**
     * Removes the simple extension from this plugin. An existing extension must match in <code>extensionPointId</code>,
     * <code>elementName</code> and all <code>attributes</code> given.<br>
     * A simple extension is of the following format:<br>
     * <code>
     * &lt;extension point="extensionPointId"&gt;<br>
     * &nbsp;&nbsp;&lt;elementName attribute1="value1" attribute2="value2"&gt;&lt;/elementName&gt;<br>
     * &lt;/extension&gt;<br>
     * </code>
     * 
     * @param extensionPointId
     *          The full extension point id.
     * @param elementName
     *          the tag name
     * @param attributes
     *          key-value-pairs of the attributes.
     * @throws CoreException
     */
    public void removeSimpleExtension(String extensionPointId, String elementName, Map<String, String> attributes) throws CoreException {
      IPluginElement[] toDeleteList = getSimpleExtensions(extensionPointId, elementName, attributes);
      for (IPluginElement toDelete : toDeleteList) {
        if (toDelete.getParent() instanceof IPluginExtension) {
          ((IPluginExtension) toDelete.getParent()).remove(toDelete);
        }
      }
    }

    /**
     * Removes the simple extension from this plugin. An existing extension must match in <code>extensionPointId</code>
     * and <code>elementName</code>.<br>
     * A simple extension is of the following format:<br>
     * <code>
     * &lt;extension point="extensionPointId"&gt;<br>
     * &nbsp;&nbsp;&lt;elementName&gt;&lt;/elementName&gt;<br>
     * &lt;/extension&gt;<br>
     * </code>
     * 
     * @param extensionPointId
     *          The full extension point id.
     * @param elementName
     * @throws CoreException
     */
    public void removeSimpleExtension(String extensionPointId, String elementName) throws CoreException {
      removeSimpleExtension(extensionPointId, elementName, null);
    }

    /**
     * Gets all simple extensions with given <code>extensionPointId</code>, <code>elementName</code> and
     * <code>attributes</code>.<br>
     * A simple extension is of the following format:<br>
     * <code>
     * &lt;extension point="extensionPointId"&gt;<br>
     * &nbsp;&nbsp;&lt;elementName attribute1="value1" attribute2="value2"&gt;&lt;/elementName&gt;<br>
     * &lt;/extension&gt;<br>
     * </code>
     * 
     * @param extensionPointId
     *          The full extension point id.
     * @param elementName
     *          The tag name
     * @param attributes
     *          the attributes that must match.
     * @return an array containing all extensions that match the given criteria.
     */
    public IPluginElement[] getSimpleExtensions(String extensionPointId, String elementName, Map<String, String> attributes) {
      List<IPluginElement> candidates = getPluginExtensions(extensionPointId);
      Iterator<IPluginElement> it = candidates.iterator();
      while (it.hasNext()) {
        IPluginElement candidate = it.next();
        if (!elementName.equals(candidate.getName())) {
          it.remove(); // element name does not match -> no candidate (remove from list)
        }
        else if (attributes != null && attributes.size() > 0) {
          // name matches. check also for the attributes
          for (Entry<String, String> entry : attributes.entrySet()) {
            IPluginAttribute a = candidate.getAttribute(entry.getKey());
            if (a == null) {
              it.remove(); // search attribute does not exist -> no candidate (remove from list)
              break;
            }
            else if (CompareUtility.notEquals(entry.getValue(), a.getValue())) {
              it.remove(); // search attribute does not contain the requested value -> no candidate (remove from list)
              break;
            }
          }
        }
      }
      return candidates.toArray(new IPluginElement[candidates.size()]);
    }

    /**
     * Gets all simple extensions with given <code>extensionPointId</code> and <code>elementName</code>.<br>
     * A simple extension is of the following format:<br>
     * <code>
     * &lt;extension point="extensionPointId"&gt;<br>
     * &nbsp;&nbsp;&lt;elementName&gt;&lt;/elementName&gt;<br>
     * &lt;/extension&gt;<br>
     * </code>
     * 
     * @param extensionPointId
     *          The full extension point id.
     * @param elementName
     *          The tag name
     * @return an array containing all extensions that match the given criteria.
     */
    public IPluginElement[] getSimpleExtensions(String extensionPointId, String elementName) {
      return getSimpleExtensions(extensionPointId, elementName, null);
    }

    private List<IPluginElement> getPluginExtensions(String extensionPointId) {
      LinkedList<IPluginElement> result = new LinkedList<IPluginElement>();
      for (IPluginExtension extPoint : m_model.getExtensionsModel().getExtensions().getExtensions()) {
        if (extPoint.getPoint().equals(extensionPointId)) {
          for (IPluginObject element : extPoint.getChildren()) {
            if (element instanceof IPluginElement) {
              result.add((IPluginElement) element);
            }
          }
        }
      }
      return result;
    }

    /**
     * Gets the first simple extension with given <code>extensionPointId</code>, <code>elementName</code> and
     * <code>attributes</code>.<br>
     * A simple extension is of the following format:<br>
     * <code>
     * &lt;extension point="extensionPointId"&gt;<br>
     * &nbsp;&nbsp;&lt;elementName attribute1="value1" attribute2="value2"&gt;&lt;/elementName&gt;<br>
     * &lt;/extension&gt;<br>
     * </code>
     * 
     * @param extensionPointId
     *          The full extension point id
     * @param elementName
     *          The tag name
     * @param attributes
     *          The attributes
     * @return the first extension that matches all criteria.
     */
    public IPluginElement getSimpleExtension(String extensionPointId, String elementName, Map<String, String> attributes) {
      IPluginElement[] matches = getSimpleExtensions(extensionPointId, elementName, attributes);
      if (matches != null && matches.length > 0) return matches[0];
      return null;
    }

    /**
     * Gets the first simple extension with given <code>extensionPointId</code> and <code>elementName</code>.<br>
     * A simple extension is of the following format:<br>
     * <code>
     * &lt;extension point="extensionPointId"&gt;<br>
     * &nbsp;&nbsp;&lt;elementName&gt;&lt;/elementName&gt;<br>
     * &lt;/extension&gt;<br>
     * </code>
     * 
     * @param extensionPointId
     *          The full extension point id
     * @param elementName
     *          The tag name
     * @return the first extension that matches all criteria.
     */
    public IPluginElement getSimpleExtension(String extensionPointId, String elementName) {
      return getSimpleExtension(extensionPointId, elementName, null);
    }

    /**
     * Checks if a simple extension with given <code>extensionPointId</code>, <code>elementName</code> and
     * <code>attributes</code> already exists.<br>
     * A simple extension is of the following format:<br>
     * <code>
     * &lt;extension point="extensionPointId"&gt;<br>
     * &nbsp;&nbsp;&lt;elementName attribute1="value1" attribute2="value2"&gt;&lt;/elementName&gt;<br>
     * &lt;/extension&gt;<br>
     * </code>
     * 
     * @param extensionPointId
     *          The full extension point id
     * @param elementName
     *          The tag name
     * @param attributes
     *          The attributes
     * @return true if such an extension exists, false otherwise.
     */
    public boolean existsSimpleExtension(String extensionPointId, String elementName, Map<String, String> attributes) {
      IPluginElement[] matches = getSimpleExtensions(extensionPointId, elementName, attributes);
      return matches != null && matches.length > 0;
    }

    /**
     * Checks if a simple extension with given <code>extensionPointId</code> and <code>elementName</code> already
     * exists.<br>
     * A simple extension is of the following format:<br>
     * <code>
     * &lt;extension point="extensionPointId"&gt;<br>
     * &nbsp;&nbsp;&lt;elementName&gt;&lt;/elementName&gt;<br>
     * &lt;/extension&gt;<br>
     * </code>
     * 
     * @param extensionPointId
     *          The full extension point id
     * @param elementName
     *          The tag name
     * @return true if such an extension exists, false otherwise.
     */
    public boolean existsSimpleExtension(String extensionPointId, String elementName) {
      return existsSimpleExtension(extensionPointId, elementName, null);
    }

    /**
     * Gets the plugin.xml file of the plugin.
     * 
     * @return
     */
    public IFile getFile() {
      return m_model.getPluginXmlFile();
    }
  }

  public static class BuildPropertiesPart {
    private final static String BINARY_BUILD_INCLUDES = "bin.includes";

    private final LazyPluginModel m_model;

    private BuildPropertiesPart(LazyPluginModel m) {
      m_model = m;
    }

    /**
     * Removes the given resource from the binary build includes list.<br>
     * If the resource is null, this method does nothing.
     * 
     * @param resource
     *          The resource to remove.
     * @throws CoreException
     */
    public void removeBinaryBuildEntry(IResource resource) throws CoreException {
      if (resource == null) return;
      removeBinaryBuildEntry(getProjectRelativeResourcePath(resource));
    }

    /**
     * Removes the given token from the binary build includes list.<br>
     * If the token is null or empty, this method does nothing.
     * 
     * @param token
     *          The token to remove.
     * @throws CoreException
     */
    public void removeBinaryBuildEntry(String token) throws CoreException {
      if (token == null || token.length() < 1) return;
      IBuildEntry entry = m_model.getBuildModel().getBuild().getEntry(BINARY_BUILD_INCLUDES);
      if (entry != null) {
        entry.removeToken(token);
      }
    }

    /**
     * Gets all binary build includes.
     * 
     * @return An array containing all binary build includes of the plugin.
     */
    public String[] getBinaryBuildEntries() {
      IBuildEntry entry = m_model.getBuildModel().getBuild().getEntry(BINARY_BUILD_INCLUDES);
      if (entry != null) {
        return entry.getTokens();
      }
      return new String[]{};
    }

    private IBuildEntry getBuildEntry(String name) throws CoreException {
      IBuildEntry entry = m_model.getBuildModel().getBuild().getEntry(name);
      if (entry == null) {
        entry = m_model.getBuildModel().getFactory().createEntry(name);
        m_model.getBuildModel().getBuild().add(entry);
      }
      return entry;
    }

    /**
     * Checks whether the given resource exists in the binary build includes list of the project associated with this
     * helper.
     * 
     * @param resource
     *          The resource to search.
     * @return true if the given resource is already in the binary build includes list, false otherwise.
     * @throws CoreException
     */
    public boolean existsBinaryBuildEntry(IResource resource) throws CoreException {
      if (resource == null) return false;
      return existsBinaryBuildEntry(getProjectRelativeResourcePath(resource));
    }

    /**
     * Checks whether the given token exists in the binary build includes list of the project associated with this
     * helper.
     * 
     * @param token
     *          The token to search.
     * @return true if the given token is already in the binary build includes list, false otherwise.
     * @throws CoreException
     */
    public boolean existsBinaryBuildEntry(String token) throws CoreException {
      if (token == null || token.length() < 1) return false;
      IBuildEntry entry = getBuildEntry(BINARY_BUILD_INCLUDES);
      return entry.contains(token);
    }

    /**
     * Adds the given token to the binary build includes.<br>
     * If no "bin.includes" exists, it is created.<br>
     * If the token is null, does not exist or is already in the list, this method does nothing.<br>
     * This method is thread safe.
     * 
     * @param resource
     *          The resource to add.
     * @throws CoreException
     */
    public void addBinaryBuildEntry(IResource resource) throws CoreException {
      if (resource == null) return;
      if (!resource.exists()) return;
      addBinaryBuildEntry(getProjectRelativeResourcePath(resource));
    }

    /**
     * Adds the given token to the binary build includes.<br>
     * If no "bin.includes" exists, it is created.<br>
     * If the token is null, empty or is already in the list, this method does nothing.<br>
     * This method is thread safe.
     * 
     * @param token
     *          The token to add.
     * @throws CoreException
     */
    public void addBinaryBuildEntry(String token) throws CoreException {
      if (token == null || token.length() < 1) return;
      synchronized (m_model.getProject()) {
        IBuildEntry entry = getBuildEntry(BINARY_BUILD_INCLUDES);
        if (!entry.contains(token)) {
          entry.addToken(token);
        }
      }
    }

    /**
     * Gets the build.properties file of the plugin.
     * 
     * @return
     */
    public IFile getFile() {
      return m_model.getBuildPropertiesFile();
    }
  }
}
