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
package org.eclipse.scout.sdk.ui.internal;

import java.util.Hashtable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.splash.BasicSplashHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class SdeSplashHandler extends BasicSplashHandler {
  private ServiceRegistration m_monitorReg;

  @Override
  public void init(Shell splash) {
    super.init(splash);
    Rectangle progressRect = new Rectangle(0, 280, 450, 10);
    setProgressRect(progressRect);
    Rectangle messageRect = new Rectangle(7, 260, 440, 20);
    setMessageRect(messageRect);
    setForeground(new RGB(0x88, 0x88, 0x88));
    IProgressMonitor mon = getBundleProgressMonitor();
    // register progress as osgi service
    BundleContext ctx = Platform.getProduct().getDefiningBundle().getBundleContext();
    m_monitorReg = ctx.registerService(IProgressMonitor.class.getName(), mon, new Hashtable());
  }

  @Override
  public void dispose() {
    if (m_monitorReg != null) {
      m_monitorReg.unregister();
      m_monitorReg = null;
    }
    super.dispose();
  }

}
