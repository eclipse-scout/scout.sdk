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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.swt.AbstractSwtEnvironment;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironmentListener;
import org.eclipse.scout.rt.ui.swt.SwtEnvironmentEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import @@BUNDLE_SWT_NAME@@.editor.ScoutEditorPart;
import @@BUNDLE_SWT_NAME@@.views.CenterView;
import @@BUNDLE_SWT_NAME@@.views.DetailView;
import @@BUNDLE_SWT_NAME@@.views.EastView;
import @@BUNDLE_SWT_NAME@@.views.OutlineView;
import @@BUNDLE_SWT_NAME@@.views.TableView;
import @@BUNDLE_SWT_NAME@@.views.SearchView;

public class SwtEnvironment extends AbstractSwtEnvironment{

  public SwtEnvironment(Bundle bundle,String perspectiveId,Class<? extends AbstractClientSession> clientSessionClazz) {
    super(bundle, perspectiveId, clientSessionClazz);

    registerPart(IForm.VIEW_ID_OUTLINE, OutlineView.class.getName());
    registerPart(IForm.VIEW_ID_PAGE_DETAIL, DetailView.class.getName());
    registerPart(IForm.VIEW_ID_CENTER, CenterView.class.getName());
    registerPart(IForm.VIEW_ID_PAGE_TABLE, TableView.class.getName());
    registerPart(IForm.VIEW_ID_PAGE_SEARCH, SearchView.class.getName());
    registerPart(IForm.VIEW_ID_E, EastView.class.getName());
    registerPart(IForm.EDITOR_ID, ScoutEditorPart.class.getName());

    addEnvironmentListener(new ISwtEnvironmentListener() {
      @Override
      public void environmentChanged(SwtEnvironmentEvent e) {
        if (e.getType() == SwtEnvironmentEvent.STOPPED) {
          PlatformUI.getWorkbench().close();
        }
      }
    });
    addEnvironmentListener(new ISwtEnvironmentListener() {
      @Override
      public void environmentChanged(SwtEnvironmentEvent e) {
        if (e.getType() == SwtEnvironmentEvent.STARTED) {
          removeEnvironmentListener(this);
          IDesktop d = getClientSession().getDesktop();
          if (d != null) {
            setWindowTitle(d.getTitle());
            d.addPropertyChangeListener(IDesktop.PROP_TITLE, new PropertyChangeListener() {
              @Override
              public void propertyChange(PropertyChangeEvent evt) {
                setWindowTitle((String) evt.getNewValue());
              }
            });
          }
        }
      }
    });
  }

  private void setWindowTitle(final String title) {
    for (IWorkbenchWindow w : PlatformUI.getWorkbench().getWorkbenchWindows()) {
      final Shell s = w.getShell();
      if (!s.isDisposed()) {
        s.getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            s.setText(title);
          }
        });
      }
    }
  }
}
