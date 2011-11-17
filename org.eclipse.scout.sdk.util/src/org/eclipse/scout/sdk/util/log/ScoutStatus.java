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
package org.eclipse.scout.sdk.util.log;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;

public class ScoutStatus extends Status {

  public ScoutStatus(String message) {
    this(message, null);
  }

  public ScoutStatus(Throwable exception) {
    this(null, exception);
  }

  public ScoutStatus(String message, Throwable exception) {
    super(IStatus.ERROR, SdkUtilActivator.PLUGIN_ID, 0, message != null ? message : "", exception);
  }

  public ScoutStatus(IStatus status) {
    super(status.getSeverity(), status.getPlugin(), status.getMessage(), status.getException());
  }

  public ScoutStatus(int severity, String message, Throwable exception) {
    super(severity, SdkUtilActivator.PLUGIN_ID, 0, message, exception);
  }

}
