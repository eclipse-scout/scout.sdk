package org.eclipse.scout.sdk.util.pde;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.TargetPlatformHelper;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
import org.eclipse.pde.internal.core.product.ProductPlugin;
import org.osgi.framework.Version;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Helper class for basic product file operations.
 * 
 * @author mvi
 * @since 3.8.0
 */
@SuppressWarnings("restriction")
public final class ProductFileModelHelper {
  private final LazyProductFileModel m_model;

  /**
   * Class for operations executed on the configuration file of the product.
   */
  public final ConfigurationFilePart ConfigurationFile;

  /**
   * Class for operations executed on the product file.
   */
  public final ProductFilePart ProductFile;

  /**
   * Creates a new helper that operates on the given product file.
   * 
   * @param project
   *          The project in which the given path should be used.
   * @param projRelPathToProduct
   *          The path relative to the given project root pointing to the product file
   * @throws CoreException
   * @throws IllegalArgumentException
   *           when the given path is not valid or the file does not exist
   */
  public ProductFileModelHelper(IProject project, IPath projRelPathToProduct) throws CoreException {
    this(project.getFile(projRelPathToProduct));
  }

  /**
   * Creates a new helper that operates on the given product file.
   * 
   * @param project
   *          The project in which the given path should be used.
   * @param projRelPathToProduct
   *          The path relative to the given project root pointing to the product file
   * @throws CoreException
   * @throws IllegalArgumentException
   *           when the given path is not valid or the file does not exist
   */
  public ProductFileModelHelper(IProject project, String projRelPathToProduct) throws CoreException {
    this(project.getFile(projRelPathToProduct));
  }

  /**
   * Creates a new helper that operates on the given product file.
   * 
   * @param productFile
   *          The product file
   * @throws CoreException
   * @throws IllegalArgumentException
   *           when the given path is not valid or the file does not exist
   */
  public ProductFileModelHelper(IFile productFile) throws CoreException {
    m_model = new LazyProductFileModel(productFile);

    ConfigurationFile = new ConfigurationFilePart(m_model);
    ProductFile = new ProductFilePart(m_model);
  }

  public static class ProductFilePart {
    private final LazyProductFileModel m_model;

    private ProductFilePart(LazyProductFileModel model) {
      m_model = model;
    }

    /**
     * Adds the given plugin id to the dependencies of this product.<br>
     * If the given plugin is already in the list, this method does nothing.
     * 
     * @param pluginId
     *          The plugin id.
     * @throws CoreException
     */
    public void addDependency(String pluginId) throws CoreException {
      addDependency(pluginId, false);
    }

    /**
     * Adds the given plugin id to the dependencies of this product.<br>
     * If the given plugin is already in the list, this method does nothing.
     * 
     * @param pluginId
     *          The plugin id.
     * @param isFragment
     *          specifies if the given plugin id identifies a fragment.
     * @throws CoreException
     */
    public synchronized void addDependency(String pluginId, boolean isFragment) throws CoreException {
      if (!existsDependency(pluginId)) {
        P_ProductPlugin newPlugin = createPlugin(pluginId, isFragment);
        m_model.getWorkspaceProductModel().getProduct().addPlugins(new IProductPlugin[]{newPlugin});
      }
    }

    /**
     * TODO can be eliminated when <b>BUG 362398</b> is fixed.<br>
     * When the bug is fixed use m_model.getWorkspaceProductModel().getFactory().createPlugin() to create instances.
     * 
     * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=362398">Bugzilla #362398</a>
     */
    private P_ProductPlugin createPlugin(String pluginId, boolean isFragment) throws CoreException {
      return new P_ProductPlugin(m_model.getWorkspaceProductModel(), pluginId, isFragment);
    }

    /**
     * Checks if the given plugin id is already in the dependencies of this product.
     * 
     * @param pluginId
     *          The plugin id.
     * @return true if the given plugin id is already in the list, false otherwise.
     * @throws CoreException
     */
    public boolean existsDependency(String pluginId) throws CoreException {
      for (IProductPlugin p : m_model.getWorkspaceProductModel().getProduct().getPlugins()) {
        if (p.getId().equals(pluginId)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Removes the given plugin id from the dependency list of this product.
     * 
     * @param pluginId
     *          The plugin id.
     * @throws CoreException
     */
    public void removeDependency(String pluginId) throws CoreException {
      m_model.getWorkspaceProductModel().getProduct().removePlugins(new IProductPlugin[]{createPlugin(pluginId, false)});
    }

    /**
     * Checks if the product is valid or not.
     * 
     * @return true if the product is valid, false otherwise.
     * @throws CoreException
     */
    public boolean isValid() throws CoreException {
      return m_model.getWorkspaceProductModel().isValid();
    }

    /**
     * Gets the plugin models of all plugins of the product associated with this helper.
     * 
     * @return the plugin models of all plugins this product is dependent of.
     * @throws CoreException
     */
    public BundleDescription[] getPluginModels() throws CoreException {
      ArrayList<BundleDescription> list = new ArrayList<BundleDescription>();
      State state = TargetPlatformHelper.getState();

      IProductPlugin[] plugins = m_model.getWorkspaceProductModel().getProduct().getPlugins();
      for (int i = 0; i < plugins.length; i++) {
        BundleDescription bundle = null;

        String v = plugins[i].getVersion();
        if (v != null && v.length() > 0) {
          bundle = state.getBundle(plugins[i].getId(), Version.parseVersion(v));
        }
        if (bundle == null) {
          bundle = state.getBundle(plugins[i].getId(), null);
        }
        if (bundle != null) {
          list.add(bundle);
        }
      }

      return list.toArray(new BundleDescription[list.size()]);
    }

    /**
     * Gets the {@link IProduct} model for this product.
     * 
     * @return the product model.
     * @throws CoreException
     */
    public IProduct getProduct() throws CoreException {
      return m_model.getWorkspaceProductModel().getProduct();
    }
  }

  /**
   * saves all changes of all underlying models that require saving.
   * 
   * @throws CoreException
   */
  public void save() throws CoreException {
    m_model.save();
  }

  public static class ConfigurationFilePart {
    private final LazyProductFileModel m_model;

    private ConfigurationFilePart(LazyProductFileModel model) {
      m_model = model;
    }

    /**
     * Gets the value of the config file entry with the given key.
     * 
     * @param key
     *          The key to search.
     * @return The value of the given key in the config file (or null if it does not exist).
     * @throws CoreException
     */
    public String getEntry(String key) throws CoreException {
      return m_model.getConfigFileProperties().getProperty(key, null);
    }

    /**
     * Checks if the given key exists in the config file.
     * 
     * @param key
     *          The key to search.
     * @return true if the key exists, false otherwise.
     * @throws CoreException
     */
    public boolean existsEntry(String key) throws CoreException {
      return m_model.getConfigFileProperties().containsKey(key);
    }

    /**
     * Sets the value of the entry with the given key.<br>
     * If the key does not exist, it is created.
     * 
     * @param key
     *          The key to create or update.
     * @param value
     *          The value associated with the given key.
     * @throws CoreException
     */
    public void setEntry(String key, String value) throws CoreException {
      m_model.getConfigFileProperties().setProperty(key, value);
    }

    /**
     * Removes the given key from the configuration file.
     * 
     * @param key
     *          The key to remove
     * @throws CoreException
     */
    public void removeEntry(String key) throws CoreException {
      m_model.getConfigFileProperties().remove(key);
    }

    /**
     * Gets the value of the "osgi.bundles" entry of the config file.
     * 
     * @return The value of the "osgi.bundles" entry.
     * @throws CoreException
     */
    public String getOsgiBundlesEntry() throws CoreException {
      return getEntry(EclipseStarter.PROP_BUNDLES);
    }

    /**
     * Sets the value of the "osgi.bundles" entry of the config file.
     * 
     * @param value
     *          The new value of the entry.
     * @throws CoreException
     */
    public void setOsgiBundlesEntry(String value) throws CoreException {
      setEntry(EclipseStarter.PROP_BUNDLES, value);
    }

    /**
     * Gets the configuration file of this product.
     * 
     * @return The config file or null if this product has no config file specified.
     * @throws CoreException
     */
    public IFile getFile() throws CoreException {
      return m_model.getConfigIniFile();
    }

    /**
     * Gets a map of all key-value-pairs that exist in the config file.<br>
     * Changes to the map have no effect to the config file.
     * 
     * @return A map with all key-value-pairs.
     * @throws CoreException
     */
    public Map<String, String> getEntries() throws CoreException {
      HashMap<String, String> entries = new HashMap<String, String>();
      for (Entry<Object, Object> entry : m_model.getConfigFileProperties().entrySet()) {
        if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
          entries.put((String) entry.getKey(), (String) entry.getValue());
        }
      }
      return entries;
    }
  }

  /**
   * TODO can be eliminated when <b>BUG 362398</b> is fixed.
   * 
   * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=362398">Bugzilla #362398</a>
   */
  private static class P_ProductPlugin extends ProductPlugin {
    private static final long serialVersionUID = 1L;
    private boolean m_fragment;

    private P_ProductPlugin(IProductModel model, String id) {
      this(model, id, false);
    }

    private P_ProductPlugin(IProductModel model, String id, boolean isFragment) {
      super(model);
      m_fragment = isFragment;
      setId(id);
    }

    @Override
    public void parse(Node node) {
      super.parse(node);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;
        setFragment(Boolean.parseBoolean(element.getAttribute("fragment")));
      }
    }

    @Override
    public void write(String indent, PrintWriter writer) {
      writer.print(indent + "<plugin id=\"" + getId() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
      if (getVersion() != null && getVersion().length() > 0 && !getVersion().equals("0.0.0")) { //$NON-NLS-1$
        writer.print(" version=\"" + getVersion() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
      }

      IPluginModelBase modBase = PluginRegistry.findModel(getId());
      if (isFragment() || (modBase != null && modBase.isFragmentModel())) {
        writer.print(" fragment=\"true\""); //$NON-NLS-1$
      }
      writer.println("/>"); //$NON-NLS-1$
    }

    private boolean isFragment() {
      return m_fragment;
    }

    private void setFragment(boolean isFragment) {
      m_fragment = isFragment;
    }
  }
}
