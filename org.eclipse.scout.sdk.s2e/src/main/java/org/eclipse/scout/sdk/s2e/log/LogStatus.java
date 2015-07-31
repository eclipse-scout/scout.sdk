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
package org.eclipse.scout.sdk.s2e.log;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Status;

/**
 * <h3>{@link LogStatus}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 24.11.2010
 */
public class LogStatus extends Status {

  private StackTraceElement m_callerElement;

  public LogStatus(Class<?> wrapperClazz, int severity, String pluginId, int code, String message, Throwable exception) {
    super(severity, pluginId, code, message, exception);
    setWrapperClass(wrapperClazz);
  }

  public LogStatus(Class<?> wrapperClazz, int severity, String pluginId, String message, Throwable exception) {
    super(severity, pluginId, message, exception);
    setWrapperClass(wrapperClazz);
  }

  public LogStatus(Class<?> wrapperClazz, int severity, String pluginId, String message) {
    super(severity, pluginId, message);
    setWrapperClass(wrapperClazz);
  }

  @Override
  public String getMessage() {
    StringBuilder message = new StringBuilder();
    if (m_callerElement != null) {
      message.append(m_callerElement + "\n\t");
    }
    message.append(super.getMessage());
    return message.toString();
  }

  private void setWrapperClass(Class<?> wrapperClazz) {
    if (m_callerElement == null) {
      m_callerElement = getCallerLine(wrapperClazz);
    }
  }

  private static StackTraceElement getCallerLine(Class<?> wrapperClass) {
    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    int traceIndex = 0;
    int maxNumPrefixes = 4;
    Set<String> ignoredPackagePrefixes = new HashSet<>(maxNumPrefixes);
    ignoredPackagePrefixes.add(Thread.class.getName());
    ignoredPackagePrefixes.add(LogStatus.class.getName());
    ignoredPackagePrefixes.add(SdkLogManager.class.getName());
    if (wrapperClass != null) {
      ignoredPackagePrefixes.add(wrapperClass.getName());
    }

    while (traceIndex < trace.length) {
      boolean found = true;
      for (String prefix : ignoredPackagePrefixes) {
        if (trace[traceIndex].getClassName().startsWith(prefix)) {
          found = false;
          break;
        }
      }
      if (found) {
        break;
      }
      traceIndex++;
    }

    if (traceIndex >= trace.length) {
      traceIndex = trace.length - 1;
    }
    return trace[traceIndex];
  }
}
