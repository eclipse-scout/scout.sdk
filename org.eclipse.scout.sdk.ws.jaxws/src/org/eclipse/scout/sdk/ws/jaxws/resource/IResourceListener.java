/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
/**
 *
 */
package org.eclipse.scout.sdk.ws.jaxws.resource;

public interface IResourceListener {

  public static final String ELEMENT_FILE = "";

  public static final int EVENT_UNKNOWN = 1 << 0;

  public static final int EVENT_SUNJAXWS_REPLACED = 1 << 1;
  public static final int EVENT_SUNJAXWS_ENTRY_ADDED = 1 << 2;
  public static final int EVENT_SUNJAXWS_ENTRY_REMOVED = 1 << 3;
  public static final int EVENT_SUNJAXWS_WSDL_CHANGED = 1 << 4;
  public static final int EVENT_SUNJAXWS_URL_PATTERN_CHANGED = 1 << 5;
  public static final int EVENT_SUNJAXWS_HANDLER_CHANGED = 1 << 9;

  public static final int EVENT_BUILDJAXWS_REPLACED = 1 << 1;
  public static final int EVENT_BUILDJAXWS_ENTRY_ADDED = 1 << 2;
  public static final int EVENT_BUILDJAXWS_PROPERTIES_CHANGED = 1 << 3;
  public static final int EVENT_BUILDJAXWS_WSDL_CHANGED = 1 << 4;

  public static final int EVENT_WSDL_REPLACED = 1 << 1;
  public static final int EVENT_STUB_REBUILT = 1 << 2;

  public static final int EVENT_MANIFEST_CLASSPATH = 1 << 1;

  /**
   * Notification about resource change
   * 
   * @param element
   *          the element that changed. This is a constant defined in {@link IResourceListener} and is never
   *          <code>null</code>.
   * @param event
   *          the event that describes the change. Multiple events are encoded by <em>bitwise OR</em>'ing them.
   */
  public void changed(String element, int event);
}
