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
package org.eclipse.scout.sdk.compatibility.internal;

public abstract class AbstractCompatibilityActivator implements ICompatibilityActivator {
  protected <T extends Object> void registerService(Class<T> type, T service) {
    ScoutCompatibilityActivator.getDefault().registerService(type, service);
  }
}
