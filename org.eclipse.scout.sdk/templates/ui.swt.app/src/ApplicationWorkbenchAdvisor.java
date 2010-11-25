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
package @@GROUP@@.ui.swt.app.core;

import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/** <h3>ApplicationWorkbenchAdvisor</h3>
 *  Used for getting the initial perspective.
*/
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {


  public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
  	return new ApplicationWorkbenchWindowAdvisor(configurer);
  }

	public String getInitialWindowPerspectiveId() {
		return "@@GROUP@@.ui.swt.core.perspective.Perspective";
	}
}
