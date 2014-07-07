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
package org.eclipse.scout.nls.sdk.internal.ui.smartfield;

import java.util.List;

import org.eclipse.swt.graphics.Image;

public interface ISmartFieldModel {

  List<Object> getProposals(String pattern);

  String getText(Object item);

  Image getImage(Object item);

}
