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
package org.eclipse.scout.sdk.ui.action;

import org.eclipse.scout.sdk.Texts;

/**
 * <h3>{@link FormDataSqlBindingValidateAction}</h3>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 24.02.2011
 */
public class FormDataSqlBindingValidateAction extends AbstractScoutHandler {

  public FormDataSqlBindingValidateAction() {
    super(Texts.get("ValidateSqlBindings"), null, null, false, Category.DELETE);
  }
}