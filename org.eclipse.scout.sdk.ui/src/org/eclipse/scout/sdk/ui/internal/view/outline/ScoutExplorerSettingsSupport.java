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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

/**
 * <h3>{@link ScoutExplorerSettingsSupport}</h3>
 * 
 * @author mvi
 * @since 3.9.0 20.03.2013
 */
public final class ScoutExplorerSettingsSupport {

  private final static ScoutExplorerSettingsSupport INSTANCE = new ScoutExplorerSettingsSupport();

  public static enum BundlePresentation {
    Grouped,
    Hierarchical,
    Flat
  }

  public final static String PREF_BUNDLE_DISPLAY_STYLE_KEY = "org.eclipse.scout.sdk.ui.view.scoutExplorer.bundleDisplayStyle";
  public final static String PREF_SHOW_FRAGMENTS_KEY = "org.eclipse.scout.sdk.view.ui.scoutExplorer.showFragments";
  public final static String PREF_SHOW_BINARY_BUNDLES_KEY = "org.eclipse.scout.sdk.ui.view.scoutExplorer.showBinaryBundles";

  private final static String DISPLAY_STYLE_GROUPED = "grouped"; // default
  private final static String DISPLAY_STYLE_HIERARCHICAL = "hierarchical";
  private final static String DISPLAY_STYLE_FLAT = "flat";

  private final static String SHOW_FRAGMENTS_ENABLED = "true";
  private final static String SHOW_FRAGMENTS_DISABLED = "false"; // default

  private final static String SHOW_BINARY_BUNDLES_ENABLED = "true"; // default
  private final static String SHOW_BINARY_BUNDLES_DISABLED = "false";

  private BundlePresentation m_bundlePresentation;
  private boolean m_showFragments;
  private boolean m_showBinaryBundles;

  public static ScoutExplorerSettingsSupport get() {
    return INSTANCE;
  }

  private ScoutExplorerSettingsSupport() {
    IPreferenceStore preferenceStore = ScoutSdkUi.getDefault().getPreferenceStore();
    preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent event) {
        if (PREF_BUNDLE_DISPLAY_STYLE_KEY.equals(event.getProperty()) ||
            PREF_SHOW_FRAGMENTS_KEY.equals(event.getProperty()) ||
            PREF_SHOW_BINARY_BUNDLES_KEY.equals(event.getProperty())) {
          readFromStore();
        }
      }
    });
    readFromStore();
  }

  private void readFromStore() {
    IPreferenceStore preferenceStore = ScoutSdkUi.getDefault().getPreferenceStore();

    String displayStyle = preferenceStore.getString(PREF_BUNDLE_DISPLAY_STYLE_KEY);
    setBundlePresentation(parseBundlePresentation(displayStyle));

    String showFragments = preferenceStore.getString(PREF_SHOW_FRAGMENTS_KEY);
    setShowFragments(SHOW_FRAGMENTS_ENABLED.equals(showFragments));

    String showBinaryBundles = preferenceStore.getString(PREF_SHOW_BINARY_BUNDLES_KEY);
    setShowBinaryBundles(!SHOW_BINARY_BUNDLES_DISABLED.equals(showBinaryBundles));
  }

  private static BundlePresentation parseBundlePresentation(String input) {
    if (DISPLAY_STYLE_FLAT.equals(input)) {
      return BundlePresentation.Flat;
    }
    else if (DISPLAY_STYLE_HIERARCHICAL.equals(input)) {
      return BundlePresentation.Hierarchical;
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
    return DISPLAY_STYLE_GROUPED;
  }

  public BundlePresentation getBundlePresentation() {
    return m_bundlePresentation;
  }

  public void setBundlePresentation(BundlePresentation bundlePresentation) {
    BundlePresentation old = m_bundlePresentation;
    m_bundlePresentation = bundlePresentation;

    if (old != m_bundlePresentation) {
      IPreferenceStore preferenceStore = ScoutSdkUi.getDefault().getPreferenceStore();
      preferenceStore.putValue(PREF_BUNDLE_DISPLAY_STYLE_KEY, getBundlePresentationString(m_bundlePresentation));
      preferenceStore.firePropertyChangeEvent(PREF_BUNDLE_DISPLAY_STYLE_KEY, old, m_bundlePresentation);
      // the store is automatically saved on workspace shutdown
    }
  }

  public boolean isShowFragments() {
    return m_showFragments;
  }

  public void setShowFragments(boolean showFragments) {
    boolean old = m_showFragments;
    m_showFragments = showFragments;

    if (old != m_showFragments) {
      IPreferenceStore preferenceStore = ScoutSdkUi.getDefault().getPreferenceStore();
      preferenceStore.putValue(PREF_SHOW_FRAGMENTS_KEY, showFragments ? SHOW_FRAGMENTS_ENABLED : SHOW_FRAGMENTS_DISABLED);
      preferenceStore.firePropertyChangeEvent(PREF_SHOW_FRAGMENTS_KEY, old, m_showFragments);
      // the store is automatically saved on workspace shutdown
    }
  }

  public boolean isShowBinaryBundles() {
    return m_showBinaryBundles;
  }

  public void setShowBinaryBundles(boolean showBinaryBundles) {
    boolean old = m_showBinaryBundles;
    m_showBinaryBundles = showBinaryBundles;

    if (old != m_showBinaryBundles) {
      IPreferenceStore preferenceStore = ScoutSdkUi.getDefault().getPreferenceStore();
      preferenceStore.putValue(PREF_SHOW_BINARY_BUNDLES_KEY, m_showBinaryBundles ? SHOW_BINARY_BUNDLES_ENABLED : SHOW_BINARY_BUNDLES_DISABLED);
      preferenceStore.firePropertyChangeEvent(PREF_SHOW_BINARY_BUNDLES_KEY, old, m_showBinaryBundles);
      // the store is automatically saved on workspace shutdown
    }
  }
}
