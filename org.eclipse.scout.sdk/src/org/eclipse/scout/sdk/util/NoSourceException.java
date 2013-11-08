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
package org.eclipse.scout.sdk.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.util.log.ScoutStatus;

/**
 * <h3>{@link NoSourceException}</h3> ...
 * 
 * @author Matthias Villiger
 * @since 3.8.0 26.11.2012
 */
public class NoSourceException extends JavaModelException {
  private static final long serialVersionUID = -9155111725402043100L;

  public NoSourceException(String typeName) {
    super(new CoreException(new ScoutStatus(Status.ERROR, "No source found for '" + typeName + "'", null)));
  }
}
