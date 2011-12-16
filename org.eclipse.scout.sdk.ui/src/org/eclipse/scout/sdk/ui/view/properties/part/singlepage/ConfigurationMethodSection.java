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
package org.eclipse.scout.sdk.ui.view.properties.part.singlepage;

import java.util.TreeSet;

import org.eclipse.scout.sdk.ui.view.properties.part.ISection;
import org.eclipse.scout.sdk.ui.view.properties.part.singlepage.PropertyViewConfig.Config;
import org.eclipse.scout.sdk.ui.view.properties.part.singlepage.PropertyViewConfig.ConfigCategory;
import org.eclipse.scout.sdk.ui.view.properties.part.singlepage.PropertyViewConfig.ConfigTypes;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyType;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A section used to represent configuration methods of certain types.
 */
public class ConfigurationMethodSection {

  private static final PropertyViewConfig CONFIG = new PropertyViewConfig();
  private static final FormToolkit TOOLKIT = new FormToolkit(Display.getDefault());

  private final ConfigurationMethodEx[] m_methods;

  private ISection m_section;
  private int m_numCategories;

  /**
   * Creates a section showing all presenters for methods that match the type parameters given to this constructor.<br>
   * The presenters are grouped by category as defined in the configuration xml file.
   * 
   * @param t
   *          The configuration methods of this type are rendered.
   * @param methodType
   *          One of ConfigurationMethod.PROPERTY_METHOD or ConfigurationMethod.OPERATION_METHOD
   * @param configurationType
   *          One of the <code>ConfigTypes</code>
   * @see ConfigTypes
   * @see ConfigurationMethod
   */
  public ConfigurationMethodSection(ConfigPropertyType t, int methodType, ConfigTypes configurationType) {
    m_methods = getConfigMethods(t, methodType, configurationType);
  }

  /**
   * creates the content of the section.
   * 
   * @param parent
   *          The section will be added to this PropertyPart
   * @param id
   *          The id of the section. Must be unique.
   * @param label
   *          The label of the section shown on top in the colored bar.
   * @param expanded
   *          specifies if the section should be expanded by default or collapsed. if expanded is false, the content of
   *          the section is created on demand when the section is expanded for the first time.
   */
  public ISection createContent(JdtTypePropertyPart parent, String id, String label, boolean expanded) {
    if (m_methods != null && m_methods.length > 0) {
      m_numCategories = getCategoryCount();
      m_section = createSection(parent, id, label, expanded);
    }
    return m_section;
  }

  private ISection createSection(final JdtTypePropertyPart part, String id, String label, boolean expanded) {
    final ISection section = part.addSection(id, label);
    section.setExpanded(expanded);

    final Composite clientSection = section.getSectionClient();
    GridLayout layout = new GridLayout(1, true);
    layout.horizontalSpacing = 0;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.verticalSpacing = 0;
    clientSection.setLayout(layout);

    if (expanded) {
      createCategories(clientSection, part, section);
    }
    else {
      // if collapsed by default: load content only on demand
      section.addExpansionListener(new ExpansionAdapter() {
        private boolean isInitialized = false;

        @Override
        public void expansionStateChanging(ExpansionEvent e) {
          if (!isInitialized && e.getState()) {
            createCategories(clientSection, part, section);
            isInitialized = true;
          }
        }
      });
    }
    return section;
  }

  /**
   * completely creates all categories that exist for this section.
   */
  private void createCategories(Composite parent, JdtTypePropertyPart part, ISection section) {
    ConfigCategory curCat = null;
    Composite curCatContainer = null;
    for (ConfigurationMethodEx m : m_methods) {
      if (!m.m_category.equals(curCat)) {
        // category changed: render horizontal line and create a new composite for the following presenters
        curCat = m.m_category;
        if (m_numCategories > 1) {
          // only show category heading, if there is more than one category
          createCategoryHeading(parent, curCat.getLabel());
        }
        curCatContainer = createCategory(parent, section);
      }

      if (m.m_configMethod.getMethodType() == ConfigurationMethod.PROPERTY_METHOD) {
        part.createConfigMethodPresenter(curCatContainer, m.m_configMethod);
      }
      else if (m.m_configMethod.getMethodType() == ConfigurationMethod.OPERATION_METHOD) {
        part.createOperationPresenter(curCatContainer, m.m_configMethod);
      }
    }
  }

  /**
   * creates a new category composite that is able to change its columns dynamically based on the space available.
   */
  private Composite createCategory(final Composite parent, final ISection section) {
    final Composite category = TOOLKIT.createComposite(parent);
    final GridLayout catLayout = new GridLayout(1, true);
    catLayout.marginHeight = 0;
    catLayout.marginWidth = 0;
    catLayout.verticalSpacing = 0;
    category.setLayout(catLayout);
    category.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

    category.addControlListener(new ControlListener() {
      private final int m_columnWidth = 300;
      private int m_numCols = 1;

      private void layoutColumns() {
        try {
          category.setRedraw(false);
          parent.setRedraw(false);
          int numCols = Math.max(1, category.getBounds().width / m_columnWidth);
          if (numCols != m_numCols) {
            catLayout.numColumns = numCols;
            m_numCols = numCols;
            section.reflow();
          }
        }
        finally {
          category.setRedraw(true);
          parent.setRedraw(true);
        }
      }

      @Override
      public void controlResized(ControlEvent e) {
        layoutColumns();
      }

      @Override
      public void controlMoved(ControlEvent e) {
        layoutColumns();
      }
    });
    return category;
  }

  /**
   * creates a new category heading with a label and a horizontal line behind.
   * 
   * @param parent
   * @param label
   *          The text of the heading
   * @return
   */
  private Composite createCategoryHeading(Composite parent, String label) {
    Composite body = TOOLKIT.createComposite(parent);
    GridLayout bodyLayout = new GridLayout(2, false);
    bodyLayout.horizontalSpacing = 5;
    bodyLayout.marginHeight = 0;
    bodyLayout.marginWidth = 0;
    bodyLayout.verticalSpacing = 0;
    body.setLayout(bodyLayout);

    GridData bodyLayoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    body.setLayoutData(bodyLayoutData);

    Label l = new Label(body, SWT.NONE);
    l.setText(label);

    Label line = new Label(body, SWT.SEPARATOR | SWT.SHADOW_OUT | SWT.HORIZONTAL);
    line.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    return body;
  }

  /**
   * gets all configuration methods that match the given type filters ordered by category and order annotation of the
   * method.
   */
  private ConfigurationMethodEx[] getConfigMethods(ConfigPropertyType source, int methodType, ConfigTypes configurationType) {
    ConfigurationMethod[] allMethods = source.getConfigurationMethods(methodType);
    TreeSet<ConfigurationMethodEx> result = new TreeSet<ConfigurationMethodEx>();
    for (ConfigurationMethod m : allMethods) {
      ConfigurationMethodEx cme = new ConfigurationMethodEx(m);
      if (cme.m_configType.equals(configurationType)) {
        result.add(cme);
      }
    }
    return result.toArray(new ConfigurationMethodEx[result.size()]);
  }

  private int getCategoryCount() {
    ConfigCategory cat = m_methods[0].m_category;
    int ret = 1;
    for (ConfigurationMethodEx m : m_methods) {
      if (!m.m_category.equals(cat)) {
        cat = m.m_category;
        ret++;
      }
    }
    return ret;
  }

  /**
   * Helper class to hold additional meta data to a configuration method as configured in the xml file.
   */
  private static class ConfigurationMethodEx implements Comparable<ConfigurationMethodEx> {
    private final ConfigurationMethod m_configMethod;
    private final Double m_order; /* order annotation of the method in scout RT or as configured in the xml configuration */
    private final ConfigCategory m_category; /* category it belongs to according to the xml configuration */
    private final ConfigTypes m_configType; /* normal or advanced type according to the xml configuration */

    private ConfigurationMethodEx(ConfigurationMethod configMethod) {
      m_configMethod = configMethod;

      Config c = CONFIG.getConfiguration(configMethod);
      Double o = null;
      if (c != null && c.getOrder() != null) {
        // xml config overrides scout RT annotation
        o = c.getOrder();
      }
      else {
        o = configMethod.getOrder();
      }

      m_order = o == null ? Double.MAX_VALUE : o;
      m_category = c == null ? PropertyViewConfig.DEFAULT_CONFIG_CATEGORY : c.getCategory();
      m_configType = c == null ? PropertyViewConfig.DEFAULT_CONFIG_TYPE : c.getType();
    }

    @Override
    public int compareTo(ConfigurationMethodEx o) {
      // sort first by category
      int catComp = new Integer(m_category.getOrder()).compareTo(o.m_category.getOrder());
      if (catComp != 0) {
        return catComp;
      }
      else {
        // sort second by order of the method inside the category
        int orderComp = m_order.compareTo(o.m_order);
        if (orderComp != 0) {
          return orderComp;
        }
        else {
          // sort third by property name
          return m_configMethod.getMethodName().compareTo(o.m_configMethod.getMethodName());
        }
      }
    }
  }
}
