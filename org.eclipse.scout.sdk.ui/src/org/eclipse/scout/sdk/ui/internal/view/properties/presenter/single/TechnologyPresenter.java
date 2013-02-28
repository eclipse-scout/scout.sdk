package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import java.util.LinkedList;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.extensions.TechnologyExtensionPoint;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.ITechnologyListener;
import org.eclipse.scout.sdk.ui.internal.extensions.technology.Technology;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.AbstractPresenter;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class TechnologyPresenter extends AbstractPresenter {

  private final IScoutBundle m_scoutProject;
  private final LinkedList<P_TechnologyUiModel> m_techModels;

  public TechnologyPresenter(PropertyViewFormToolkit toolkit, Composite parent, IScoutBundle scoutProject) {
    super(toolkit, parent);
    m_scoutProject = scoutProject;
    m_techModels = new LinkedList<P_TechnologyUiModel>();

    // define layout
    GridLayout layout = new GridLayout(1, false);
    layout.marginTop = 4;
    layout.marginBottom = 4;
    getContainer().setLayout(layout);
  }

  public void loadModel() {
    // sort all technologies
    TreeSet<Technology> technologies = new TreeSet<Technology>();
    for (Technology t : TechnologyExtensionPoint.getTechnologyExtensions()) {
      technologies.add(t);
    }

    // load the initial selection state
    m_techModels.clear();
    for (Technology t : technologies) {
      try {
        P_TechnologyUiModel uiModel = new P_TechnologyUiModel();
        uiModel.technology = t;
        uiModel.active = uiModel.technology.isActive(m_scoutProject);
        if (uiModel.active) {
          uiModel.selection = uiModel.technology.getSelection(m_scoutProject);
        }
        m_techModels.add(uiModel);
      }
      catch (Throwable tt) {
        ScoutSdkUi.logError("unable to load model for technology " + t.getId(), tt);
      }
    }
  }

  public void createContent() {
    String cat = null;
    for (P_TechnologyUiModel t : m_techModels) {
      final Technology tec = t.technology;
      if (t.active) {
        if (cat == null || !cat.equals(tec.getCategory())) {
          cat = tec.getCategory();
          createCategoryHeading(getContainer(), cat);
        }

        final Button checkbox = getToolkit().createButton(getContainer(), tec.getName(), SWT.CHECK);
        TriState initVal = t.selection;
        setCheckBoxVal(checkbox, initVal);
        checkbox.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseDown(MouseEvent e) {
            checkbox.setEnabled(false);
            boolean saved = false;
            try {
              saved = tec.setSelection(m_scoutProject, !checkbox.getSelection());
            }
            catch (CoreException e1) {
              ScoutSdkUi.logError("Unable to change technology selection.", e1);
            }
            if (!saved) {
              checkbox.setEnabled(true);
            }
          }
        });
        tec.addSelectionChangedListener(new ITechnologyListener() {
          @Override
          public void selectionChangeCompleted(boolean newSelection) {
            setCheckBoxVal(checkbox, TriState.parseTriState(newSelection));
            if (!checkbox.isDisposed()) {
              checkbox.setEnabled(true);
            }
          }
        });
      }
    }
  }

  private Composite createCategoryHeading(Composite parent, String label) {
    Composite body = getToolkit().createComposite(parent);
    GridLayout bodyLayout = new GridLayout(2, false);
    bodyLayout.horizontalSpacing = 4;
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

  private static void setCheckBoxVal(Button checkbox, TriState val) {
    if (checkbox == null || checkbox.isDisposed()) return;
    if (val == TriState.FALSE) {
      checkbox.setSelection(false);
      checkbox.setGrayed(false);
    }
    else {
      checkbox.setSelection(true);
      checkbox.setGrayed(val == TriState.UNDEFINED);
    }
  }

  private static class P_TechnologyUiModel {
    private Technology technology;
    private TriState selection;
    private boolean active;
  }
}
