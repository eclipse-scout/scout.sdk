package @@BUNDLE_SWT_NAME@@;


import org.eclipse.scout.rt.client.AbstractClientSession;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.swt.AbstractSwtEnvironment;
import org.eclipse.scout.rt.ui.swt.ISwtEnvironmentListener;
import org.eclipse.scout.rt.ui.swt.SwtEnvironmentEvent;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import @@BUNDLE_SWT_NAME@@.editor.ScoutEditorPart;
import @@BUNDLE_SWT_NAME@@.views.CenterView;
import @@BUNDLE_SWT_NAME@@.views.EastView;
import @@BUNDLE_SWT_NAME@@.views.NorthEastView;
import @@BUNDLE_SWT_NAME@@.views.NorthWestView;
import @@BUNDLE_SWT_NAME@@.views.SouthWestView;
import @@BUNDLE_SWT_NAME@@.views.SouthView;
import @@BUNDLE_SWT_NAME@@.views.WestView;
import @@BUNDLE_SWT_NAME@@.views.SouthEastView;

public class SwtEnvironment extends AbstractSwtEnvironment{


  public SwtEnvironment(Bundle bundle,String perspectiveId,Class<? extends AbstractClientSession> clientSessionClazz) {
    super(bundle, perspectiveId, clientSessionClazz);

    registerPart(IForm.VIEW_ID_OUTLINE, NorthWestView.class.getName());
    registerPart(IForm.VIEW_ID_OUTLINE_SELECTOR, SouthWestView.class.getName());
    registerPart(IForm.VIEW_ID_CENTER, CenterView.class.getName());
    registerPart(IForm.VIEW_ID_PAGE_TABLE, CenterView.class.getName());
    registerPart(IForm.VIEW_ID_PAGE_DETAIL, NorthView.class.getName());
    registerPart(IForm.VIEW_ID_PAGE_SEARCH, SouthView.class.getName());
    registerPart(IForm.VIEW_ID_N, NorthView.class.getName());
    registerPart(IForm.VIEW_ID_NW, NorthWestView.class.getName());
    registerPart(IForm.VIEW_ID_W, WestView.class.getName());
    registerPart(IForm.VIEW_ID_SW, SouthWestView.class.getName());
    registerPart(IForm.VIEW_ID_S, SouthView.class.getName());
    registerPart(IForm.VIEW_ID_SE, SouthEastView.class.getName());
    registerPart(IForm.VIEW_ID_E, EastView.class.getName());
    registerPart(IForm.VIEW_ID_NE, NorthEastView.class.getName());

    registerPart(IForm.EDITOR_ID, ScoutEditorPart.class.getName());

    addEnvironmentListener(new ISwtEnvironmentListener() {
      @Override
      public void environmentChanged(SwtEnvironmentEvent e) {
        if (e.getType() == SwtEnvironmentEvent.STOPPED) {
          if (!PlatformUI.getWorkbench().isClosing()) {
            PlatformUI.getWorkbench().close();
          }
        }
      }
    });

  }
}
