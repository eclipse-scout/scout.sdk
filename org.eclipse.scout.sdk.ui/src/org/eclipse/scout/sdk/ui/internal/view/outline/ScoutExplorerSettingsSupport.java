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
package org.eclipse.scout.sdk.ui.internal.view.outline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkingSet;

/**
 * <h3>{@link ScoutExplorerSettingsSupport}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.9.0 20.03.2013
 */
@SuppressWarnings("restriction")
public final class ScoutExplorerSettingsSupport {

  private final static ScoutExplorerSettingsSupport INSTANCE = new ScoutExplorerSettingsSupport();

  public static enum BundlePresentation {
    Grouped,
    Hierarchical,
    Flat,
    WorkingSet,
    FlatGroups,
  }

  public final static String SCOUT_WOKRING_SET_ID = "org.eclipse.scout.sdk.ui.workingSet";

  public final static String PREF_BUNDLE_DISPLAY_STYLE_KEY = "org.eclipse.scout.sdk.ui.view.scoutExplorer.bundleDisplayStyle";
  public final static String PREF_SHOW_FRAGMENTS_KEY = "org.eclipse.scout.sdk.view.ui.scoutExplorer.showFragments";
  public final static String PREF_SHOW_BINARY_BUNDLES_KEY = "org.eclipse.scout.sdk.ui.view.scoutExplorer.showBinaryBundles";
  public final static String PREF_HIDDEN_BUNDLES_TYPES = "org.eclipse.scout.sdk.ui.view.scoutExplorer.hiddenBundleTypes";
  public final static String PREF_HIDDEN_WORKING_SETS = "org.eclipse.scout.sdk.ui.view.scoutExplorer.hiddenWorkingSets";
  public final static String PREF_WORKING_SETS_ORDER = "org.eclipse.scout.sdk.ui.view.scoutExplorer.workingSetsOrder";

  private final static String DISPLAY_STYLE_GROUPED = "grouped"; // default
  private final static String DISPLAY_STYLE_HIERARCHICAL = "hierarchical";
  private final static String DISPLAY_STYLE_FLAT = "flat";
  private final static String DISPLAY_STYLE_WORKING_SET = "workingSet";
  private final static String DISPLAY_STYLE_FLAT_GROUPS = "flatGroups";

  private final static String SHOW_FRAGMENTS_ENABLED = "true";
  private final static String SHOW_FRAGMENTS_DISABLED = "false"; // default

  private final static String SHOW_BINARY_BUNDLES_ENABLED = "true"; // default
  private final static String SHOW_BINARY_BUNDLES_DISABLED = "false";

  public final static char DELIMITER = ',';
  public final static String OTHER_PROJECTS_WORKING_SET_NAME = "Other Projects";

  private BundlePresentation m_bundlePresentation;
  private boolean m_showFragments;
  private boolean m_showBinaryBundles;
  private Set<String> m_hiddenBundleTypes;
  private Set<String> m_hiddenWorkingSets;
  private String[] m_workingSetsOrder;

  public static ScoutExplorerSettingsSupport get() {
    return INSTANCE;
  }

  private ScoutExplorerSettingsSupport() {
    IPreferenceStore preferenceStore = getStore();
    preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent event) {
        if (PREF_BUNDLE_DISPLAY_STYLE_KEY.equals(event.getProperty()) ||
            PREF_SHOW_FRAGMENTS_KEY.equals(event.getProperty()) ||
            PREF_SHOW_BINARY_BUNDLES_KEY.equals(event.getProperty()) ||
            PREF_HIDDEN_BUNDLES_TYPES.equals(event.getProperty()) ||
            PREF_HIDDEN_WORKING_SETS.equals(event.getProperty()) ||
            PREF_WORKING_SETS_ORDER.equals(event.getProperty())) {
          readFromStore();
        }
      }
    });
    readFromStore();
  }

  private synchronized void readFromStore() {
    IPreferenceStore preferenceStore = getStore();

    String displayStyle = preferenceStore.getString(PREF_BUNDLE_DISPLAY_STYLE_KEY);
    setBundlePresentation(parseBundlePresentation(displayStyle));

    String showFragments = preferenceStore.getString(PREF_SHOW_FRAGMENTS_KEY);
    setShowFragments(SHOW_FRAGMENTS_ENABLED.equals(showFragments));

    String showBinaryBundles = preferenceStore.getString(PREF_SHOW_BINARY_BUNDLES_KEY);
    setShowBinaryBundles(SHOW_BINARY_BUNDLES_ENABLED.equals(showBinaryBundles));

    m_hiddenBundleTypes = parseListProperty(PREF_HIDDEN_BUNDLES_TYPES);

    m_hiddenWorkingSets = parseListProperty(PREF_HIDDEN_WORKING_SETS);

    Set<String> list = parseListProperty(PREF_WORKING_SETS_ORDER);
    m_workingSetsOrder = list.toArray(new String[list.size()]);
  }

  private static Set<String> parseListProperty(String propertyName) {
    String prop = getStore().getString(propertyName);
    String[] tokens = null;
    if (StringUtility.hasText(prop)) {
      tokens = prop.split("" + DELIMITER);
    }
    return toSet(tokens);
  }

  private static Set<String> toSet(String[] elements) {
    Set<String> items = new LinkedHashSet<String>();
    if (elements != null && elements.length > 0) {
      for (String s : elements) {
        if (StringUtility.hasText(s)) {
          items.add(s.trim());
        }
      }
    }
    return items;
  }

  private static void storeListProperty(Iterable<String> oldItems, Iterable<String> newItems, String propertyName) {
    if (CompareUtility.equals(oldItems, newItems)) {
      return;
    }

    StringBuilder sb = new StringBuilder();
    for (String s : newItems) {
      if (StringUtility.hasText(s)) {
        sb.append(s.trim());
        sb.append(DELIMITER);
      }
    }

    // remove ending delimiter if existing
    if (sb.length() > 1) {
      int lastPos = sb.length() - 1;
      if (DELIMITER == sb.charAt(lastPos)) {
        sb.deleteCharAt(lastPos);
      }
    }

    persist(propertyName, sb.toString(), oldItems, newItems);
  }

  private static BundlePresentation parseBundlePresentation(String input) {
    if (DISPLAY_STYLE_FLAT.equals(input)) {
      return BundlePresentation.Flat;
    }
    else if (DISPLAY_STYLE_HIERARCHICAL.equals(input)) {
      return BundlePresentation.Hierarchical;
    }
    else if (DISPLAY_STYLE_WORKING_SET.equals(input)) {
      return BundlePresentation.WorkingSet;
    }
    else if (DISPLAY_STYLE_FLAT_GROUPS.equals(input)) {
      return BundlePresentation.FlatGroups;
    }
    return BundlePresentation.Grouped;
  }

  private static String getBundlePresentationString(BundlePresentation input) {
    if (BundlePresentation.Flat.equals(input)) {
      return DISPLAY_STYLE_FLAT;
    }
    else if (BundlePresentation.Hierarchical.equals(input)) {
      return DISPLAY_STYLE_HIERARCHICAL;
    }
    else if (BundlePresentation.WorkingSet.equals(input)) {
      return DISPLAY_STYLE_WORKING_SET;
    }
    else if (BundlePresentation.FlatGroups.equals(input)) {
      return DISPLAY_STYLE_FLAT_GROUPS;
    }
    return DISPLAY_STYLE_GROUPED;
  }

  private static IPreferenceStore getStore() {
    return ScoutSdkUi.getDefault().getPreferenceStore();
  }

  private static void persist(String key, String value, Object oldVal, Object newVal) {
    IPreferenceStore preferenceStore = getStore();
    preferenceStore.putValue(key, value);
    preferenceStore.firePropertyChangeEvent(key, oldVal, newVal);
    // the store is automatically saved on workspace shutdown
  }

  public synchronized BundlePresentation getBundlePresentation() {
    return m_bundlePresentation;
  }

  public synchronized void setBundlePresentation(BundlePresentation bundlePresentation) {
    BundlePresentation old = m_bundlePresentation;
    m_bundlePresentation = bundlePresentation;

    if (old != m_bundlePresentation) {
      persist(PREF_BUNDLE_DISPLAY_STYLE_KEY, getBundlePresentationString(m_bundlePresentation), old, m_bundlePresentation);
    }
  }

  public synchronized boolean isShowFragments() {
    return m_showFragments;
  }

  public synchronized void setShowFragments(boolean showFragments) {
    boolean old = m_showFragments;
    m_showFragments = showFragments;

    if (old != m_showFragments) {
      persist(PREF_SHOW_FRAGMENTS_KEY, showFragments ? SHOW_FRAGMENTS_ENABLED : SHOW_FRAGMENTS_DISABLED, old, m_showFragments);
    }
  }

  public synchronized boolean isShowBinaryBundles() {
    return m_showBinaryBundles;
  }

  public synchronized void setShowBinaryBundles(boolean showBinaryBundles) {
    boolean old = m_showBinaryBundles;
    m_showBinaryBundles = showBinaryBundles;

    if (old != m_showBinaryBundles) {
      persist(PREF_SHOW_BINARY_BUNDLES_KEY, m_showBinaryBundles ? SHOW_BINARY_BUNDLES_ENABLED : SHOW_BINARY_BUNDLES_DISABLED, old, m_showBinaryBundles);
    }
  }

  public synchronized boolean isBundleTypeHidden(String type) {
    return m_hiddenBundleTypes.contains(type);
  }

  public synchronized void addHiddenBundleType(String type) {
    Set<String> old = new HashSet<String>(m_hiddenBundleTypes);
    boolean added = m_hiddenBundleTypes.add(type);
    if (added) {
      storeListProperty(old, m_hiddenBundleTypes, PREF_HIDDEN_BUNDLES_TYPES);
    }
  }

  public synchronized void removeHiddenBundleType(String type) {
    Set<String> old = new HashSet<String>(m_hiddenBundleTypes);
    boolean removed = m_hiddenBundleTypes.remove(type);
    if (removed) {
      storeListProperty(old, m_hiddenBundleTypes, PREF_HIDDEN_BUNDLES_TYPES);
    }
  }

  public synchronized void setWorkingSetsOrder(IWorkingSet[] order) {
    String[] oldOrder = m_workingSetsOrder;
    String[] newOrder = new String[order.length];
    for (int i = 0; i < newOrder.length; i++) {
      newOrder[i] = order[i].getName();
    }

    if (CompareUtility.notEquals(oldOrder, newOrder)) {
      m_workingSetsOrder = newOrder;
      storeListProperty(toList(oldOrder), toList(m_workingSetsOrder), PREF_WORKING_SETS_ORDER);
    }
  }

  private static <T> List<T> toList(T[] elements) {
    ArrayList<T> result = new ArrayList<T>();
    if (elements != null && elements.length > 0) {
      for (T e : elements) {
        result.add(e);
      }
    }
    return result;
  }

  private static int getOrder(String workingSetName, String[] setsOrder) {
    for (int i = 0; i < setsOrder.length; i++) {
      if (CompareUtility.equals(setsOrder[i], workingSetName)) {
        return i;
      }
    }
    return Integer.MAX_VALUE; // if undefined, put them to the end
  }

  public synchronized void setHiddenScoutWorkingSets(IWorkingSet[] hiddenSets) {
    Set<String> old = new HashSet<String>(m_hiddenWorkingSets);
    Set<String> newHidden = new HashSet<String>(hiddenSets.length);
    for (IWorkingSet s : hiddenSets) {
      newHidden.add(s.getName());
    }

    if (CompareUtility.notEquals(old, newHidden)) {
      m_hiddenWorkingSets = newHidden;
      storeListProperty(old, m_hiddenWorkingSets, PREF_HIDDEN_WORKING_SETS);
    }
  }

  public synchronized IWorkingSet[] getScoutWorkingSets(boolean includeHidden) {
    IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
    IWorkingSet[] allWorkingSets = workingSetManager.getAllWorkingSets();
    HashSet<IWorkingSet> result = new HashSet<IWorkingSet>(allWorkingSets.length + 1);
    HashSet<IScoutBundle> bundlesThatBelongToASet = new HashSet<IScoutBundle>();
    for (IWorkingSet ws : allWorkingSets) {
      IWorkingSet fws = filterWorkingSet(ws, includeHidden);
      if (fws != null) {
        result.add(fws);
      }

      // remember the bundles used
      for (IAdaptable a : ws.getElements()) {
        IScoutBundle b = (IScoutBundle) a.getAdapter(IScoutBundle.class);
        if (b != null) {
          bundlesThatBelongToASet.add(b);
        }
      }
    }

    // others working set
    IScoutBundle[] unAssignedBundles = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundles(ScoutBundleFilters.getNotInListFilter(bundlesThatBelongToASet));
    IWorkingSet others = new WorkingSet(OTHER_PROJECTS_WORKING_SET_NAME, OTHER_PROJECTS_WORKING_SET_NAME, unAssignedBundles);
    others.setId(SCOUT_WOKRING_SET_ID);
    others = filterWorkingSet(others, includeHidden);
    if (others != null) {
      result.add(others);
    }

    IWorkingSet[] array = result.toArray(new IWorkingSet[result.size()]);
    Arrays.sort(array, new Comparator<IWorkingSet>() {
      @Override
      public int compare(IWorkingSet o1, IWorkingSet o2) {
        int order1 = getOrder(o1.getName(), m_workingSetsOrder);
        int order2 = getOrder(o2.getName(), m_workingSetsOrder);
        return order1 - order2;
      }
    });
    return array;
  }

  private IWorkingSet filterWorkingSet(IWorkingSet ws, boolean includeHidden) {
    if (ws.isVisible() && SCOUT_WOKRING_SET_ID.equals(ws.getId())) {
      if (includeHidden || !m_hiddenWorkingSets.contains(ws.getName())) {
        return ws;
      }
    }
    return null;
  }
}
