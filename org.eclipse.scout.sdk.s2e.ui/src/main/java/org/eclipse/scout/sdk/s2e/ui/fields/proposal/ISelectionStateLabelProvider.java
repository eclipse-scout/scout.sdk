/*
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.fields.proposal;

import org.eclipse.swt.graphics.Image;

/**
 * <h3>{@link ISelectionStateLabelProvider}</h3> Feature interface for a custom styling of the proposal that is
 * currently selected. So this interface can be used if the currently selected proposal should look different than in
 * unselected state.
 *
 * @since 3.8.0 2012-02-07
 */
public interface ISelectionStateLabelProvider {

  Image getImageSelected(Object element);

  String getTextSelected(Object element);
}
