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
package org.eclipse.scout.sdk.util.pde;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.TargetPlatformHelper;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.core.iproduct.IProductFeature;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
import org.eclipse.pde.internal.core.product.ProductFeature;
import org.eclipse.pde.internal.core.product.ProductPlugin;
import org.eclipse.scout.sdk.compatibility.PlatformVersionUtility;
import org.osgi.framework.Version;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Helper class for basic product file operations.
 *
 * @author Matthias Villiger
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
   * @throws when
   *           the given path is not valid or the file does not exist
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
   * @throws when
   *           the given path is not valid or the file does not exist
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
   * @throws when
   *           the given path is not valid or the file does not exist
   */
  public ProductFileModelHelper(IFile productFile) throws CoreException {
    m_model = new LazyProductFileModel(productFile);

    ConfigurationFile = new ConfigurationFilePart(m_model);
    ProductFile = new ProductFilePart(m_model);
  }

  /**
   * <h3>{@link DependencyType}</h3>Specifies the type of dependency in a .product file.
   *
   * @author Matthias Villiger
   * @since 3.10.0 19.09.2013
   */
  public static enum DependencyType {
    /**
     * Specifies a normal plug-in dependency.
     */
    PLUGIN,

    /**
     * Specifies a fragment dependency.
     */
    FRAGMENT,

    /**
     * Specifies a dependency to a feature.
     */
    FEATURE
  }

  public static final class ProductFilePart {
    private final LazyProductFileModel m_model;

    private ProductFilePart(LazyProductFileModel model) {
      m_model = model;
    }

    /**
     * Adds the given plug-in id to the dependencies of this product.<br>
     * If the given plug-in is already in the list, this method does nothing.
     *
     * @param pluginId
     *          The plug-in id.
     * @throws CoreException
     */
    public void addDependency(String pluginId) throws CoreException {
      addDependency(pluginId, DependencyType.PLUGIN);
    }

    /**
     * Adds the given id to the dependencies of this product.<br>
     * If the given dependency already exists, this method does nothing.
     *
     * @param id
     *          The id to add (feature id, fragment or plug-in symbolic name).
     * @param type
     *          specifies the type of dependency.
     * @throws CoreException
     * @see DependencyType
     */
    public synchronized void addDependency(String id, DependencyType type) throws CoreException {
      if (!existsDependency(id)) {
        if (DependencyType.FEATURE.equals(type)) {
          ProductFeature pf = createFeature(id);
          m_model.getWorkspaceProductModel().getProduct().addFeatures(new IProductFeature[]{pf});
        }
        else {
          P_ProductPlugin newPlugin = createPlugin(id, DependencyType.FRAGMENT.equals(type));
          m_model.getWorkspaceProductModel().getProduct().addPlugins(new IProductPlugin[]{newPlugin});
        }
      }
    }

    private ProductFeature createFeature(String id) throws CoreException {
      ProductFeature pf = new ProductFeature(m_model.getWorkspaceProductModel());
      pf.setId(id);
      return pf;
    }

    /**
     * TODO can be eliminated when Eclipse 3.8 is the oldest version that the SDK supports (the bug was solved for
     * Eclipse 3.8. Must be verified though).<br>
     * Then use m_model.getWorkspaceProductModel().getFactory().createPlugin() to create instances.
     *
     * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=362398">Bugzilla #362398</a>
     */
    private P_ProductPlugin createPlugin(String pluginId, boolean isFragment) throws CoreException {
      return new P_ProductPlugin(m_model.getWorkspaceProductModel(), pluginId, isFragment);
    }

    /**
     * Checks if the given id is already in the dependencies of this product.<br>
     * The id can either be a feature id (for feature based products) or a bundle symbolic name (for plug-in based
     * products). If a plug-in id is given, this plug-in is also searched within the features of feature based products.
     *
     * @param pluginOrFeatureId
     *          The feature id or plug-in symbolic name
     * @return true if the given dependency is already in the product.
     * @throws CoreException
     */
    public boolean existsDependency(String pluginOrFeatureId) throws CoreException {
      // search in plug-ins
      for (IProductPlugin p : m_model.getWorkspaceProductModel().getProduct().getPlugins()) {
        if (p.getId().equals(pluginOrFeatureId)) {
          return true;
        }
      }

      IProductFeature[] features = m_model.getWorkspaceProductModel().getProduct().getFeatures();
      if (features != null && features.length > 0) {
        // search directly in features
        for (IProductFeature feature : features) {
          if (feature.getId().equals(pluginOrFeatureId)) {
            return true;
          }
        }

        // search plug-ins within features
        Map<String, ? extends Set<String>> allBundlesInFeatures = getBundlesInFeatures();
        for (IProductFeature f : features) {
          Set<String> bundlesInCurrentFeature = allBundlesInFeatures.get(f.getId());
          if (bundlesInCurrentFeature != null && bundlesInCurrentFeature.contains(pluginOrFeatureId)) {
            return true;
          }
        }
      }
      return false;
    }

    private Map<String /*feature id*/, ? extends Set<String /*bundle symbolic name*/>> getBundlesInFeatures() {
      IFeatureModel[] features = PDECore.getDefault().getFeatureModelManager().getModels();
      HashMap<String, HashSet<String>> allBundlesInFeatures = new HashMap<String, HashSet<String>>(features.length);
      for (IFeatureModel feature : features) {
        for (IFeaturePlugin bundle : feature.getFeature().getPlugins()) {
          HashSet<String> bundlesInFeature = allBundlesInFeatures.get(feature.getFeature().getId());
          if (bundlesInFeature == null) {
            bundlesInFeature = new HashSet<String>();
            allBundlesInFeatures.put(feature.getFeature().getId(), bundlesInFeature);
          }
          bundlesInFeature.add(bundle.getId());
        }
      }
      return allBundlesInFeatures;
    }

    /**
     * Removes the given dependency (feature id or plug-in symbolic name) from the dependency list of this product.
     *
     * @param id
     *          The feature id or plug-in symbolic name.
     * @throws CoreException
     */
    public void removeDependency(String id) throws CoreException {
      m_model.getWorkspaceProductModel().getProduct().removePlugins(new IProductPlugin[]{createPlugin(id, false)});
      m_model.getWorkspaceProductModel().getProduct().removeFeatures(new IProductFeature[]{createFeature(id)});
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
     * Gets the plug-in models of all plug-ins of the product associated with this helper.<br>
     * <br>
     * Plug-ins that exist in the product file but cannot be found in the current target platform
     * (marked red in the product file editor) will not be part of the result.<br>
     * Use {@link ProductFilePart#getPluginSymbolicNames()} to get the names of all plugins referenced in the product
     * file independent if they can be found or not.<br>
     * <br>
     * If the product file contains version constraints for the plug-ins, only models that fulfill the version
     * constraints are returned.<br>
     * <br>
     * If the product is feature based, this method returns an empty array.
     *
     * @return the plug-in models of all plug-ins this product is dependent of.
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
     * Gets the symbolic names of all plug-ins of the product associated with this helper.<br>
     * If the product is feature based, this method returns an empty array.
     *
     * @return The symbolic names of all plug-ins in the product.
     * @throws CoreException
     */
    public String[] getPluginSymbolicNames() throws CoreException {
      IProductPlugin[] plugins = m_model.getWorkspaceProductModel().getProduct().getPlugins();
      String[] result = new String[plugins.length];
      for (int i = 0; i < result.length; i++) {
        result[i] = plugins[i].getId();
      }
      return result;
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

  public static final class ConfigurationFilePart {
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
      return m_model.getConfigFileProperties().getProperty(key);
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
      return m_model.getConfigFileProperties().containsProperty(key);
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
      m_model.getConfigFileProperties().removeProperty(key);
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
      return m_model.getConfigFileProperties().getEntries();
    }
  }

  /**
   * TODO can be eliminated when Eclipse 3.8 is the oldest version that the SDK supports (the bug was solved for Eclipse
   * 3.8. Must be verified though).<br>
   * Then use m_model.getWorkspaceProductModel().getFactory().createPlugin() to create instances.
   *
   * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=362398">Bugzilla #362398</a>
   */
  private static final class P_ProductPlugin extends ProductPlugin {
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
      if (getVersion() != null && getVersion().length() > 0 && !PlatformVersionUtility.EMPTY_VERSION_STR.equals(getVersion())) {
        writer.print(" version=\"" + getVersion() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
      }

      IPluginModelBase modBase = PluginRegistry.findModel(getId());
      if (isFragment() || (modBase != null && modBase.isFragmentModel())) {
        writer.print(" fragment=\"true\""); //$NON-NLS-1$
      }
      writer.println("/>"); //$NON-NLS-1$
    }

    public boolean isFragment() {
      return m_fragment;
    }

    public void setFragment(boolean isFragment) {
      m_fragment = isFragment;
    }
  }
}
