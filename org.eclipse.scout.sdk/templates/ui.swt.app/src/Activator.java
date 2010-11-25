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
package @@GROUP@@.ui.swt.app.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


/** <h3>Activator</h3>
 *  ...
*/
public class Activator implements BundleActivator{
  public static final String PLUGIN_ID =  "@@GROUP@@.ui.swt.app.core";


  //The shared instance
  private static Activator plugin;

  public void start(BundleContext context) throws Exception{
    plugin = this;
  }

  public void stop(BundleContext context) throws Exception{
    plugin = null;
  }

  /**
   * Returns the shared instance
* @return the shared instance
   */
  public static Activator getDefault() {
    return plugin;
  }

}

