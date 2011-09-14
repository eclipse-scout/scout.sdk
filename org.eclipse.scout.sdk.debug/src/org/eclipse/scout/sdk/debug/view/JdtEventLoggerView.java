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
package org.eclipse.scout.sdk.debug.view;

import java.util.ArrayList;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

/**
 * <h3>{@link JdtEventLoggerView}</h3> A view can be used to debug all occuring jdt events.
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 11.02.2011
 */
public class JdtEventLoggerView extends ViewPart {

  public static final String VIEW_ID = JdtEventLoggerView.class.getName();

  private Button m_startButton;
  private Button m_stopButton;
  private Button m_resetButton;
  private Text m_eventLogField;
  private Object writeLock = new Object();
  private ArrayList<String> m_events = new ArrayList<String>();
  private IElementChangedListener m_elementChangedListener;
  private FormToolkit m_toolkit;

  @Override
  public void createPartControl(Composite parent) {
    parent.addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        stopEventLogging();
      }
    });
    m_toolkit = new FormToolkit(parent.getDisplay());
    Form rootPane = m_toolkit.createForm(parent);
    Control buttonBar = createButtonBar(rootPane.getBody());
    m_eventLogField = m_toolkit.createText(rootPane.getBody(), "", SWT.MULTI | SWT.SHADOW_ETCHED_IN | SWT.V_SCROLL);

    // layout
    parent.setLayout(new FillLayout());
    rootPane.getBody().setLayout(new GridLayout(1, true));
    buttonBar.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_eventLogField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
  }

  protected Control createButtonBar(Composite parent) {
    Composite buttonBar = m_toolkit.createComposite(parent);
    m_startButton = m_toolkit.createButton(buttonBar, "start", SWT.PUSH);
    m_startButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        startEventLogging();
      }
    });
    m_stopButton = m_toolkit.createButton(buttonBar, "stop", SWT.PUSH);
    m_stopButton.setEnabled(false);
    m_stopButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        stopEventLogging();
      }
    });

    m_resetButton = m_toolkit.createButton(buttonBar, "reset", SWT.PUSH);
    m_resetButton.setText("reset");
    m_resetButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        resetLog();
      }
    });
    // layout
    buttonBar.setLayout(new GridLayout(3, true));
    return buttonBar;
  }

  protected void startEventLogging() {
    if (m_elementChangedListener == null) {
      m_elementChangedListener = new P_JdtEventListner();
      JavaCore.addElementChangedListener(m_elementChangedListener);
      m_startButton.setEnabled(false);
      m_stopButton.setEnabled(true);
    }

  }

  protected void stopEventLogging() {
    if (m_elementChangedListener != null) {
      JavaCore.removeElementChangedListener(m_elementChangedListener);
      m_elementChangedListener = null;
      m_startButton.setEnabled(true);
      m_stopButton.setEnabled(false);
    }
  }

  protected void resetLog() {
    synchronized (writeLock) {
      m_events.clear();
    }
    updateView();
  }

  protected void updateView() {
    String[] logs = null;
    synchronized (writeLock) {
      logs = m_events.toArray(new String[m_events.size()]);
    }
    final String[] finalLogs = logs;
    getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        StringBuilder allLogBuilder = new StringBuilder();
        for (String s : finalLogs) {
          allLogBuilder.append(s);
        }
        m_eventLogField.setText(allLogBuilder.toString());
        m_eventLogField.setSelection(allLogBuilder.length());
        m_eventLogField.showSelection();

      }
    });
  }

  @Override
  public void setFocus() {
    m_startButton.getParent().setFocus();
  }

  private class P_JdtEventListner implements IElementChangedListener {
    public static final int CHANGED_FLAG_MASK =
        IJavaElementDelta.F_CONTENT |
            IJavaElementDelta.F_MODIFIERS |
            IJavaElementDelta.F_MOVED_FROM |
            IJavaElementDelta.F_MOVED_TO |
            IJavaElementDelta.F_REORDER |
            IJavaElementDelta.F_SUPER_TYPES |
            IJavaElementDelta.F_OPENED |
            IJavaElementDelta.F_CLOSED |
            IJavaElementDelta.F_CATEGORIES |
            IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED |
            IJavaElementDelta.F_ANNOTATIONS;

    @Override
    public void elementChanged(ElementChangedEvent event) {
      StringBuilder eventLogBuilder = new StringBuilder();
      visitDelta(event.getDelta(), event.getType(), eventLogBuilder, 0);
      synchronized (writeLock) {
        m_events.add(eventLogBuilder.toString());
      }
      updateView();
    }

    private String getEventLogLine(IJavaElementDelta delta, int eventType, int indent) {
      StringBuilder text = new StringBuilder();
      for (int i = 0; i < indent; i++) {
        text.append("  ");
      }
      switch (delta.getKind()) {
        case IJavaElementDelta.ADDED:
          text.append("ADDED: ");
          break;
        case IJavaElementDelta.CHANGED:
          text.append("CHANGED: ");
          break;
        case IJavaElementDelta.REMOVED:
          text.append("REMOVED: ");
          break;
      }

      if (delta.getElement() != null) {
        switch (delta.getElement().getElementType()) {
          case IJavaElement.JAVA_MODEL:
            text.append("JAVA_MODEL ");
            break;
          case IJavaElement.JAVA_PROJECT:
            text.append("JAVA_PROJECT ");
            break;
          case IJavaElement.PACKAGE_FRAGMENT_ROOT:
            text.append("PACKAGE_FRAGMENT_ROOT ");
            break;
          case IJavaElement.PACKAGE_FRAGMENT:
            text.append("PACKAGE_FRAGMENT ");
            break;
          case IJavaElement.COMPILATION_UNIT:
            text.append("COMPILATION_UNIT ");
            break;
          case IJavaElement.CLASS_FILE:
            text.append("CLASS_FILE ");
            break;
          case IJavaElement.TYPE:
            text.append("TYPE ");
            break;
          case IJavaElement.FIELD:
            text.append("FIELD ");
            break;
          case IJavaElement.METHOD:
            text.append("METHOD ");
            break;
          case IJavaElement.INITIALIZER:
            text.append("INITIALIZER ");
            break;
          case IJavaElement.PACKAGE_DECLARATION:
            text.append("PACKAGE_DECLARATION ");
            break;
          case IJavaElement.IMPORT_CONTAINER:
            text.append("IMPORT_CONTAINER ");
            break;
          case IJavaElement.IMPORT_DECLARATION:
            text.append("IMPORT_DECLARATION ");
            break;

          case IJavaElement.LOCAL_VARIABLE:
            text.append("LOCAL_VARIABLE ");
            break;

          case IJavaElement.TYPE_PARAMETER:
            text.append("TYPE_PARAMETER ");
            break;

          case IJavaElement.ANNOTATION:
            text.append("ANNOTATION ");
            break;
        }
        text.append(delta.getElement().getElementName() + " ");
      }
      else {
        text.append("NULL element");
      }
      if ((delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0) {
        text.append(" children");
      }
      text.append("\n");
      return text.toString();
    }

    private void visitDelta(IJavaElementDelta delta, int eventType, StringBuilder eventLogBuilder, int indent) {
      int flags = delta.getFlags();
      eventType = delta.getKind();

      // children
      if ((flags & IJavaElementDelta.F_CHILDREN) != 0) {
        eventLogBuilder.append(getEventLogLine(delta, eventType, indent));
        IJavaElementDelta[] childDeltas = delta.getAffectedChildren();
        if (childDeltas != null && childDeltas.length > 0) {
          for (int i = 0; i < childDeltas.length; i++) {
            visitDelta(childDeltas[i], eventType, eventLogBuilder, indent + 1);
          }
        }
      }
      else {
        eventLogBuilder.append(getEventLogLine(delta, eventType, indent));
      }
      for (IJavaElementDelta annotationDelta : delta.getAnnotationDeltas()) {
        visitDelta(annotationDelta, eventType, eventLogBuilder, indent + 1);
      }
    }
  }

}
