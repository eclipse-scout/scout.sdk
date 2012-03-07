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

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.swt.AbstractSwtEnvironment;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironmentListener;
import org.eclipse.scout.rt.ui.swt.SwtEnvironmentEvent;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;


/** <h3>SwtEnvironment</h3>
 *  This class provides the possibility to write a own representation of any scout field.
 *  Furthermore the scout view id to swt view id mapping is done here. Ensure that each
 *  swt view id you are mapping to a certain scout view id is defined in the plugin.xml
 *  as a view extension.
 *  <br>
 *  e.g.
 *  <pre>
 *  public ISwtScoutSmartField createSmartField(Composite parent, ISmartField<?> model) {
 *    // create your own component
 *    ISwtScoutSmartField sf = ...
 *    return sf;
 *  }
 *  </pre>
*/
public class SwtEnvironment extends AbstractSwtEnvironment{

  public SwtEnvironment(Bundle bundle,String perspectiveId,Class<? extends AbstractClientSession> clientSessionClazz) {
    super(bundle, perspectiveId, clientSessionClazz);
    registerPart(IForm.VIEW_ID_CENTER, Activator.CENTER_VIEW_ID);
    registerPart(IForm.VIEW_ID_OUTLINE, Activator.OUTLINE_VIEW_ID);
    registerPart(IForm.VIEW_ID_PAGE_TABLE, Activator.TABLE_PAGE_VIEW_ID);
    registerPart(IForm.VIEW_ID_PAGE_SEARCH, Activator.SEAECH_VIEW_ID);

    addEnvironmentListener(new ISwtEnvironmentListener() {
      @Override
      public void environmentChanged(SwtEnvironmentEvent e) {
        if (e.getType() == SwtEnvironmentEvent.STOPPED) {
          PlatformUI.getWorkbench().close();
        }
      }
    });
  }
}
