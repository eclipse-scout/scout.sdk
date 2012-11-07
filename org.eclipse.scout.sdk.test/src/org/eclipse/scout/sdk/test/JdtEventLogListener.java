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
package org.eclipse.scout.sdk.test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.scout.sdk.test.Event.EventGroup;
import org.eclipse.scout.sdk.test.Event.Type;

/**
 *
 */
public class JdtEventLogListener implements IElementChangedListener {

  private List<P_Flag> m_flagsToLog = new ArrayList<JdtEventLogListener.P_Flag>();

  public void addLogFlag(int flag, String text) {
    m_flagsToLog.add(new P_Flag(flag, text));
  }

  @Override
  public void elementChanged(ElementChangedEvent event) {
    Event logEvent = new Event(getEventType(event), "", "");
    logEvent.setEventGroup(EventGroup.JDT_EVENT);
    visitDelta(event.getDelta(), event.getType(), logEvent);

    printLogEvent(logEvent, 0);
  }

  private void printLogEvent(Event e, int indent) {
    StringBuilder line = new StringBuilder();
    for (int i = 0; i < indent; i++) {
      line.append(" ");
    }
    String indentString = line.toString();
    line.append(e.getEventType()).append(" ").append(e.getElementType()).append(" ").append(e.getElement()).append("\n");
    line.append(indentString).append("-> ").append(e.getCustomText());
    System.out.println(line.toString());
    for (Event ce : e.getChildren()) {
      printLogEvent(ce, indent + 1);
    }
  }

  private void visitDelta(IJavaElementDelta delta, int eventType, Event parentEvent) {
    Event newLogEvent = new Event(getEventType(delta), getElementType(delta), delta.getElement().getElementName());
    newLogEvent.setEventGroup(EventGroup.JDT_EVENT);
    int flags = delta.getFlags();
    StringBuilder flagBuilder = new StringBuilder("flags[");
    for (P_Flag f : m_flagsToLog) {
      flagBuilder.append("(").append(f.text).append(" ").append((flags & f.flag) != 0).append(")");
    }
    flagBuilder.append("]");
    newLogEvent.setCustomText(flagBuilder.toString());
    parentEvent.addChildEvent(newLogEvent);
    if ((flags & IJavaElementDelta.F_CHILDREN) != 0) {
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

  private class P_Flag {
    public int flag;
    public String text;

    public P_Flag(int flag, String text) {
      this.flag = flag;
      this.text = text;
    }
  }
}
