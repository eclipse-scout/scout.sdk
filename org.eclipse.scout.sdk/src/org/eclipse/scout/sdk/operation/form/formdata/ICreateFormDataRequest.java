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
package org.eclipse.scout.sdk.operation.form.formdata;

import org.eclipse.jdt.core.IType;

/**
 * <h3>{@link ICreateFormDataRequest}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 17.02.2011
 */
public interface ICreateFormDataRequest {

  public boolean createFormData(IType type, String packageName, String simpleName);

  public int showQuestion(String title, String message, int buttons);
}
