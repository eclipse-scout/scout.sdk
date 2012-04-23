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
package @@BUNDLE_SWT_NAME@@;

import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
import @@BUNDLE_CLIENT_NAME@@.ClientSession;
import @@BUNDLE_SWT_NAME@@.perspective.Perspective;
import @@BUNDLE_SWT_NAME@@.views.CenterView;
import @@BUNDLE_SWT_NAME@@.views.DetailView;
import @@BUNDLE_SWT_NAME@@.views.EastView;
import @@BUNDLE_SWT_NAME@@.views.OutlineView;
import @@BUNDLE_SWT_NAME@@.views.SearchView;
import @@BUNDLE_SWT_NAME@@.views.TableView;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator{

  // the plugin id
  public static final String BUNDLE_ID = "@@BUNDLE_SWT_NAME@@";

  // all view ID's commodity to access.
  public static final String CENTER_VIEW_ID = CenterView.class.getName();
  public static final String DETAIL_VIEW_ID = DetailView.class.getName();
  public static final String EAST_VIEW_ID = EastView.class.getName();
  public static final String OUTLINE_VIEW_ID = OutlineView.class.getName();
  public static final String TABLE_VIEW_ID = TableView.class.getName();
  public static final String SEARCH_VIEW_ID = SearchView.class.getName();

  private ISwtEnvironment m_environment;

  // the shared instance
  private static Activator m_bundle;

  @Override
  public void start(BundleContext context) throws Exception {
    m_bundle = this;
    m_environment = new SwtEnvironment(context.getBundle(), Perspective.ID, ClientSession.class);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    m_bundle = null;
  }

  public static Activator getDefault() {
    return m_bundle;
  }

  public ISwtEnvironment getEnvironment() {
    return m_environment;
  }
}

