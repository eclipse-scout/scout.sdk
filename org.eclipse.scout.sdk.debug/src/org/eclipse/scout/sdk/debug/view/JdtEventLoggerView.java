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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.scout.sdk.debug.internal.eventlogger.Event;
import org.eclipse.scout.sdk.debug.internal.eventlogger.Event.EventGroup;
import org.eclipse.scout.sdk.debug.internal.eventlogger.Event.Type;
import org.eclipse.scout.sdk.debug.internal.eventlogger.EventLoggerContentProvider;
import org.eclipse.scout.sdk.ui.fields.table.ColumnViewerFilter;
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
import org.eclipse.swt.widgets.TreeColumn;
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
  private Object writeLock = new Object();
  private ArrayList<String> m_events = new ArrayList<String>();
  private IElementChangedListener m_elementChangedListener;
  private IResourceChangeListener m_resourceChangeListener;
  private FormToolkit m_toolkit;

  private Button m_jdtEventCheckbox;
  private TreeViewer m_treeViewer;
  private Event m_invisibleRoot;

  private Button m_resourceEventCheckbox;

  public JdtEventLoggerView() {
    m_invisibleRoot = new Event();
  }

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
    ColumnViewerFilter filter = new ColumnViewerFilter(rootPane.getBody());
    m_treeViewer = new TreeViewer(rootPane.getBody(), SWT.FULL_SELECTION);
    m_treeViewer.getTree().setHeaderVisible(true);
    TreeColumn eventTypeColumn = new TreeColumn(m_treeViewer.getTree(), SWT.LEFT);
    eventTypeColumn.setText("Event Type");
    eventTypeColumn.setWidth(200);
    TreeColumn elementTypeColumn = new TreeColumn(m_treeViewer.getTree(), SWT.LEFT);
    elementTypeColumn.setText("Element Type");
    elementTypeColumn.setWidth(200);
    TreeColumn elementColumn = new TreeColumn(m_treeViewer.getTree(), SWT.LEFT);
    elementColumn.setText("Element");
    elementColumn.setWidth(200);
    TreeColumn eventDumpColumn = new TreeColumn(m_treeViewer.getTree(), SWT.LEFT);
    eventDumpColumn.setText("Dump");
    eventDumpColumn.setWidth(600);

    filter.setViewer(m_treeViewer);
    EventLoggerContentProvider viewerProvider = new EventLoggerContentProvider();
    m_treeViewer.setContentProvider(viewerProvider);
    m_treeViewer.setLabelProvider(viewerProvider);
    m_treeViewer.setInput(m_invisibleRoot);

    // layout
    parent.setLayout(new FillLayout());
    rootPane.getBody().setLayout(new GridLayout(1, true));
    buttonBar.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    filter.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_treeViewer.getTree().setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
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
    m_resetButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        resetLog();
      }
    });
    m_jdtEventCheckbox = m_toolkit.createButton(buttonBar, "JDT Events", SWT.CHECK);
    m_jdtEventCheckbox.setSelection(true);
    m_resourceEventCheckbox = m_toolkit.createButton(buttonBar, "Resource Events", SWT.CHECK);

    // layout
    buttonBar.setLayout(new GridLayout(5, true));
    return buttonBar;
  }

  protected void startEventLogging() {
    if (m_elementChangedListener == null) {
      if (m_jdtEventCheckbox.getSelection()) {
        m_elementChangedListener = new P_JdtEventListner();
        JavaCore.addElementChangedListener(m_elementChangedListener);
      }
    }
    if (m_resourceChangeListener == null) {
      if (m_resourceEventCheckbox.getSelection()) {
        m_resourceChangeListener = new P_ResourceChangeListener();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(m_resourceChangeListener);
      }
    }
    m_startButton.setEnabled(false);
    m_stopButton.setEnabled(true);
    m_jdtEventCheckbox.setEnabled(false);
    m_resourceEventCheckbox.setEnabled(false);
  }

  protected void stopEventLogging() {
    if (m_elementChangedListener != null) {
      JavaCore.removeElementChangedListener(m_elementChangedListener);
      m_elementChangedListener = null;
    }
    if (m_resourceChangeListener != null) {
      ResourcesPlugin.getWorkspace().removeResourceChangeListener(m_resourceChangeListener);
      m_resourceChangeListener = null;
    }
    m_startButton.setEnabled(true);
    m_stopButton.setEnabled(false);
    m_jdtEventCheckbox.setEnabled(true);
    m_resourceEventCheckbox.setEnabled(true);
  }

  protected void resetLog() {
    synchronized (writeLock) {
      m_invisibleRoot = new Event();
      m_treeViewer.setInput(m_invisibleRoot);
    }
  }

  private Job m_updateViewJob;
  private Object m_updateViewLock = new Object();

  protected void updateView() {
    synchronized (m_updateViewLock) {
      m_updateViewJob = new P_UpdateViewJob();
      m_updateViewJob.schedule(300);
    }
  }

  @Override
  public void setFocus() {
    m_startButton.getParent().setFocus();
  }

  private class P_UpdateViewJob extends Job {

    public P_UpdateViewJob() {
      super("");
      setSystem(true);
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      synchronized (m_updateViewLock) {
        if (m_updateViewJob == this) {
          if (getViewSite().getShell() != null && !getViewSite().getShell().isDisposed()) {
            getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
              @Override
              public void run() {
                m_treeViewer.refresh();
                m_treeViewer.expandAll();
              }
            });
          }
        }
      }
      return Status.OK_STATUS;
    }
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
      Event logEvent = new Event(getEventType(event), "", "");
      logEvent.setEventGroup(EventGroup.JDT_EVENT);
      visitDelta(event.getDelta(), event.getType(), logEvent);
      synchronized (writeLock) {
        m_invisibleRoot.addChildEvent(logEvent);
      }
      updateView();
    }

    private void visitDelta(IJavaElementDelta delta, int eventType, Event parentEvent) {
      Event newLogEvent = new Event(getEventType(delta), getElementType(delta), delta.getElement().getElementName() + " [hasChildren='" + ((delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0) + "']");
      newLogEvent.setEventGroup(EventGroup.JDT_EVENT);
      newLogEvent.setEventDump(eventDump(delta));
      parentEvent.addChildEvent(newLogEvent);
      if ((delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0) {
        IJavaElementDelta[] childDeltas = delta.getAffectedChildren();
        if (childDeltas != null && childDeltas.length > 0) {
          for (int i = 0; i < childDeltas.length; i++) {
            visitDelta(childDeltas[i], eventType, newLogEvent);
          }
        }
      }
      for (IJavaElementDelta annotationDelta : delta.getAnnotationDeltas()) {
        visitDelta(annotationDelta, eventType, newLogEvent);
      }
    }

    private String eventDump(IJavaElementDelta delta) {
      StringBuilder b = new StringBuilder();
      if (delta.getElement() != null) {
        ICompilationUnit icu = (ICompilationUnit) delta.getElement().getAncestor(IJavaElement.COMPILATION_UNIT);
        if (icu != null) {
          b.append("[isWorkingCopy=").append(icu.isWorkingCopy()).append("] ");
          try {
            b.append("[changed=").append(icu.hasUnsavedChanges()).append("] ");
          }
          catch (JavaModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }

      }
      int flags = delta.getFlags();
      b.append("[F_CONTENT=").append((flags & IJavaElementDelta.F_CONTENT) != 0).append("] ");
//      b.append("[F_MODIFIERS=").append((flags & IJavaElementDelta.F_MODIFIERS) != 0).append("] ");
      b.append("[F_CHILDREN=").append((flags & IJavaElementDelta.F_CHILDREN) != 0).append("] ");
//      b.append("[F_MOVED_FROM=").append((flags & IJavaElementDelta.F_MOVED_FROM) != 0).append("] ");
//      b.append("[F_MOVED_TO=").append((flags & IJavaElementDelta.F_MOVED_TO) != 0).append("] ");
//      b.append("[F_ADDED_TO_CLASSPATH=").append((flags & IJavaElementDelta.F_ADDED_TO_CLASSPATH) != 0).append("] ");
//      b.append("[F_REMOVED_FROM_CLASSPATH=").append((flags & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0).append("] ");
//      b.append("[F_CLASSPATH_REORDER=").append((flags & IJavaElementDelta.F_CLASSPATH_REORDER) != 0).append("] ");
//      b.append("[F_REORDER=").append((flags & IJavaElementDelta.F_REORDER) != 0).append("] ");
      b.append("[F_OPENED=").append((flags & IJavaElementDelta.F_OPENED) != 0).append("] ");
      b.append("[F_CLOSED=").append((flags & IJavaElementDelta.F_CLOSED) != 0).append("] ");
//      b.append("[F_SUPER_TYPES=").append((flags & IJavaElementDelta.F_SUPER_TYPES) != 0).append("] ");
//      b.append("[F_SOURCEATTACHED=").append((flags & IJavaElementDelta.F_SOURCEATTACHED) != 0).append("] ");
//      b.append("[F_SOURCEDETACHED=").append((flags & IJavaElementDelta.F_SOURCEDETACHED) != 0).append("] ");
//      b.append("[F_FINE_GRAINED=").append((flags & IJavaElementDelta.F_FINE_GRAINED) != 0).append("] ");
//      b.append("[F_ARCHIVE_CONTENT_CHANGED=").append((flags & IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED) != 0).append("] ");
      b.append("[F_PRIMARY_WORKING_COPY=").append((flags & IJavaElementDelta.F_PRIMARY_WORKING_COPY) != 0).append("] ");
//      b.append("[F_CLASSPATH_CHANGED=").append((flags & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0).append("] ");
      b.append("[F_PRIMARY_RESOURCE=").append((flags & IJavaElementDelta.F_PRIMARY_RESOURCE) != 0).append("] ");
//      b.append("[F_AST_AFFECTED=").append((flags & IJavaElementDelta.F_AST_AFFECTED) != 0).append("] ");
//      b.append("[F_CATEGORIES=").append((flags & IJavaElementDelta.F_CATEGORIES) != 0).append("] ");
//      b.append("[F_RESOLVED_CLASSPATH_CHANGED=").append((flags & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0).append("] ");
//      b.append("[F_ANNOTATIONS=").append((flags & IJavaElementDelta.F_ANNOTATIONS) != 0).append("]");

      return b.toString();
    }

    @SuppressWarnings("deprecation")
    private Type getEventType(ElementChangedEvent event) {
      switch (event.getType()) {
        case ElementChangedEvent.POST_CHANGE:
          return Type.POST_CHANGE;

        case ElementChangedEvent.POST_RECONCILE:
          return Type.POST_RECONCILE;
        case ElementChangedEvent.PRE_AUTO_BUILD:
          return Type.PRE_AUTO_BUILD;
        default:
          return Type.UNDEFINED;
      }
    }

    private Type getEventType(IJavaElementDelta delta) {
      switch (delta.getKind()) {
        case IJavaElementDelta.ADDED:
          return Type.ADDED;
        case IJavaElementDelta.CHANGED:
          return Type.CHANGED;
        case IJavaElementDelta.REMOVED:
          return Type.REMOVED;
        default:
          return Type.UNDEFINED;
      }
    }

    private String getElementType(IJavaElementDelta delta) {
      switch (delta.getElement().getElementType()) {
        case IJavaElement.JAVA_MODEL:
          return "JAVA_MODEL ";
        case IJavaElement.JAVA_PROJECT:
          return "JAVA_PROJECT ";
        case IJavaElement.PACKAGE_FRAGMENT_ROOT:
          return "PACKAGE_FRAGMENT_ROOT ";
        case IJavaElement.PACKAGE_FRAGMENT:
          return "PACKAGE_FRAGMENT ";
        case IJavaElement.COMPILATION_UNIT:
          return "COMPILATION_UNIT ";
        case IJavaElement.CLASS_FILE:
          return "CLASS_FILE ";
        case IJavaElement.TYPE:
          return "TYPE ";
        case IJavaElement.FIELD:
          return "FIELD ";
        case IJavaElement.METHOD:
          return "METHOD ";
        case IJavaElement.INITIALIZER:
          return "INITIALIZER ";
        case IJavaElement.PACKAGE_DECLARATION:
          return "PACKAGE_DECLARATION ";
        case IJavaElement.IMPORT_CONTAINER:
          return "IMPORT_CONTAINER ";
        case IJavaElement.IMPORT_DECLARATION:
          return "IMPORT_DECLARATION ";
        case IJavaElement.LOCAL_VARIABLE:
          return "LOCAL_VARIABLE ";
        case IJavaElement.TYPE_PARAMETER:
          return "TYPE_PARAMETER ";
        case IJavaElement.ANNOTATION:
          return "ANNOTATION ";
        default:
          return "???";
      }
    }

  } // end P_JdtEventListner

  private class P_ResourceChangeListener implements IResourceChangeListener {

    @Override
    public final void resourceChanged(IResourceChangeEvent e) {
      Event logEvent = new Event(getEventType(e), "", "");
      logEvent.setEventGroup(EventGroup.RESOURCE_EVENT);
      visitDelta(e.getDelta(), logEvent);
      synchronized (writeLock) {
        m_invisibleRoot.addChildEvent(logEvent);
      }
      updateView();
    }

    private void visitDelta(IResourceDelta delta, Event parentEvent) {
      if (delta == null) {
        return;
      }
      IResource resource = delta.getResource();
      String resourceName = "NULL RESOURCE";
      if (resource != null) {
        resourceName = resource.getName() + " [" + resource.exists() + "]";
      }

      Event newLogEvent = new Event(getEventType(delta), getResourceType(delta), resourceName);
      newLogEvent.setEventGroup(EventGroup.RESOURCE_EVENT);
      parentEvent.addChildEvent(newLogEvent);
      IResourceDelta[] children = delta.getAffectedChildren();
      if (children != null && children.length > 0) {
        for (int i = 0; i < children.length; i++) {
          visitDelta(children[i], newLogEvent);
        }
      }
    }

    private Type getEventType(IResourceChangeEvent event) {
      switch (event.getType()) {
        case IResourceChangeEvent.POST_CHANGE:
          return Type.POST_CHANGE;
        case IResourceChangeEvent.POST_BUILD:
          return Type.POST_BUILD;
        case IResourceChangeEvent.PRE_BUILD:
          return Type.PRE_BUILD;
        case IResourceChangeEvent.PRE_CLOSE:
          return Type.PRE_CLOSE;
        case IResourceChangeEvent.PRE_DELETE:
          return Type.PRE_DELETE;
        case IResourceChangeEvent.PRE_REFRESH:
          return Type.PRE_REFRESH;
        default:
          return Type.UNDEFINED;
      }
    }

    private Type getEventType(IResourceDelta delta) {
      switch (delta.getKind()) {
        case IResourceDelta.ADDED:
          return Type.ADDED;
        case IResourceDelta.REMOVED:
          return Type.REMOVED;
        case IResourceDelta.CHANGED:
          return Type.CHANGED;
        case IResourceDelta.ADDED_PHANTOM:
          return Type.ADDED_PHANTOM;
        case IResourceDelta.REMOVED_PHANTOM:
          return Type.REMOVED_PHANTOM;
        default:
          return Type.UNDEFINED;
      }
    }

    private String getResourceType(IResourceDelta delta) {
      switch (delta.getResource().getType()) {
        case IResource.FILE:
          return "file";
        case IResource.FOLDER:
          return "folder";
        case IResource.PROJECT:
          return "project";
        case IResource.ROOT:
          return "root";
        default:
          return "undefined";
      }
    }

  }
}
