package org.eclipse.scout.sdk.ui.view.properties.part.singlepage;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument;
import org.eclipse.scout.commons.xmlparser.ScoutXmlDocument.ScoutXmlElement;
import org.eclipse.scout.commons.xmlparser.ScoutXmlParser;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;

public class PropertyViewConfig {

  private final static String TAG_TYPE = "type";
  private final static String TAG_CONFIG = "config";
  private final static String PROP_TYPE_NAME = "name";
  private final static String PROP_CONFIG_NAME = "name";
  private final static String PROP_CONFIG_TYPE = "type";
  private final static String PROP_CONFIG_CATEGORY = "category";
  private final static String PROP_CONFIG_ORDER = "order";

  private final HashMap<String, HashMap<String, Config>> m_typeConfigs;

  public final static ConfigTypes DEFAULT_CONFIG_TYPE = ConfigTypes.Advanced;
  public final static ConfigCategory DEFAULT_CONFIG_CATEGORY = ConfigCategory.Misc;

  /**
   * Method priority.
   */
  public enum ConfigTypes {
    /**
     * Important method. Shown by default.
     */
    Normal,

    /**
     * Advanced method. Collapsed by default.
     */
    Advanced
  }

  /**
   * Method cateogry. Defines to which category a method belongs.
   */
  public enum ConfigCategory {
    /**
     * Used for properties or operations that influence how the item looks like.<br>
     * Examples:<br>
     * Label text, icons, titles, colors, fonts, numeric fraction-digits, ...
     */
    Appearance(100 /* defines in which order the categories appear in a section */),

    /**
     * Used for properties that influence how the item is positioned.<br>
     * Examples:<br>
     * alignments, widths, heights, label-visibilities, positions (x,y), display view hints, ...
     */
    Layout(200),

    /**
     * Used for properties that influence how the item behaves at runtime.<br>
     * Examples:<br>
     * type of buttons, column displayable, editable, sortings, modality of dialogs, enabled, ...
     */
    Behavior(300),

    /**
     * Used for properties that influence how the item behaves at runtime.<br>
     * Examples:<br>
     * min/max values, lookup calls, code types, master fields, max lengths, most service properties, data filters, load
     * table data, ...
     */
    Data(400),

    /**
     * Used for most operations that have no influence on the appearance of an item.
     */
    BusinessLogic(500),

    /**
     * Undefined or various other operations and properties. Default for methods that have nothing configured.
     */
    Misc(Integer.MAX_VALUE);

    private final int m_order;
    private final String m_name;

    private ConfigCategory(int order) {
      m_order = order;
      m_name = Texts.get("PropertyViewConfig.Categories." + toString());
    }

    public int getOrder() {
      return m_order;
    }

    public String getLabel() {
      return m_name;
    }
  }

  /**
   * complete meta data for a {@link ConfigurationMethod}.
   */
  public static class Config {
    private ConfigTypes type;
    private ConfigCategory category;
    private Double order;

    /**
     * The order is defined by the {@link Order} annotation of the method in the Scout Runtime.<br>
     * It can be overridden by the xml configuration file using the optional "order" attribute on the "config" tag.
     * 
     * @return Gets the order of the method inside a category.
     */
    public Double getOrder() {
      return order;
    }

    /**
     * If not defined in the config file, {@link PropertyViewConfig}<code>.DEFAULT_CONFIG_TYPE</code> is returned.
     * 
     * @return gets the type of the method (normal or advanced)
     */
    public ConfigTypes getType() {
      return type;
    }

    /**
     * If not defined in the config file, {@link PropertyViewConfig}<code>.DEFAULT_CONFIG_CATEGORY</code> is returned.
     * 
     * @return Gets the category the method belongs to.
     */
    public ConfigCategory getCategory() {
      return category;
    }
  }

  public PropertyViewConfig() {
    m_typeConfigs = new HashMap<String, HashMap<String, Config>>(100);
    load();
  }

  private void load() {
    try {
      URL url = FileLocator.find(ScoutSdkUi.getDefault().getBundle(), new Path("resources/sdkPropertyViewConfig.xml"), null);
      ScoutXmlParser parser = new ScoutXmlParser();
      InputStream is = null;
      try {
        is = url.openStream();
        ScoutXmlDocument xmlDoc = parser.parse(is);
        for (ScoutXmlElement o : xmlDoc.getRoot().getChildren(TAG_TYPE)) {
          loadType(o);
        }
      }
      finally {
        if (is != null) {
          try {
            is.close();
          }
          catch (Exception e) {
          }
        }
      }
    }
    catch (Exception e) {
      ScoutSdkUi.logError("unable to load property view configuration. ", e);
    }
  }

  /**
   * Gets the complete meta data for the given {@link ConfigurationMethod} as defined in the xml config file or null if
   * nothing is configured.
   * 
   * @param m
   * @return
   */
  public Config getConfiguration(ConfigurationMethod m) {
    IType[] superTypes = m.getSuperTypeHierarchy().getAllSupertypes(m.getType());
    for (IType superType : superTypes) {
      HashMap<String, Config> tc = m_typeConfigs.get(superType.getFullyQualifiedName());
      if (tc != null) {
        Config c = tc.get(m.getMethodName());
        if (c != null) {
          return c;
        }
      }
    }
    return null;
  }

  private void loadType(ScoutXmlElement type) {
    if (type == null) return;
    String name = type.getAttribute(PROP_TYPE_NAME);
    if (name == null || name.trim().length() < 1) return;

    HashMap<String, Config> c = new HashMap<String, Config>(20);
    m_typeConfigs.put(name, c);

    for (ScoutXmlElement o : type.getChildren(TAG_CONFIG)) {
      loadConfig(c, o);
    }
  }

  private ConfigTypes parseConfigType(String type) {
    try {
      return ConfigTypes.valueOf(type);
    }
    catch (Exception e) {
      return DEFAULT_CONFIG_TYPE;
    }
  }

  private ConfigCategory parseConfigCategory(String cat) {
    try {
      return ConfigCategory.valueOf(cat);
    }
    catch (Exception e) {
      return DEFAULT_CONFIG_CATEGORY;
    }
  }

  private Double parseOrder(String order) {
    try {
      if (order == null || order.trim().length() < 1) return null;
      else return Double.parseDouble(order);
    }
    catch (Exception e) {
      return null;
    }
  }

  private void loadConfig(HashMap<String, Config> tc, ScoutXmlElement config) {
    if (config == null) return;
    String name = config.getAttribute(PROP_CONFIG_NAME);
    String type = config.getAttribute(PROP_CONFIG_TYPE, ConfigTypes.Advanced.toString());
    String category = config.getAttribute(PROP_CONFIG_CATEGORY, ConfigCategory.Misc.toString());
    String order = config.getAttribute(PROP_CONFIG_ORDER, null);

    Config c = new Config();
    c.category = parseConfigCategory(category);
    c.type = parseConfigType(type);
    c.order = parseOrder(order);

    tc.put(name, c);
  }
}
