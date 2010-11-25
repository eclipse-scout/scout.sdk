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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.outline;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.server.service.ServiceOperationNodePage;
import org.eclipse.scout.sdk.ui.view.outline.pages.AbstractPage;

public class OutlineServiceOperationNodePage extends ServiceOperationNodePage {

  public OutlineServiceOperationNodePage(AbstractPage parent, IMethod method, IMethod implementationMethod) {
    super(parent, method, implementationMethod);
  }

}
