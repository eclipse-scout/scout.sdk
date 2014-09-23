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
package org.eclipse.scout.sdk.compatibility.v45.internal;

import org.eclipse.scout.sdk.compatibility.internal.AbstractCompatibilityActivator;
import org.eclipse.scout.sdk.compatibility.internal.service.IJavadocHoverCompatService;
import org.eclipse.scout.sdk.compatibility.internal.service.IP2CompatService;
import org.eclipse.scout.sdk.compatibility.internal.service.ITargetPlatformCompatService;

public class CompatibilityActivator extends AbstractCompatibilityActivator {
  @Override
  public void start() throws Exception {
    registerService(IJavadocHoverCompatService.class, new JavadocHoverCompatService());
    registerService(ITargetPlatformCompatService.class, new TargetPlatformCompatService());
    registerService(IP2CompatService.class, new P2CompatService());
  }
}
