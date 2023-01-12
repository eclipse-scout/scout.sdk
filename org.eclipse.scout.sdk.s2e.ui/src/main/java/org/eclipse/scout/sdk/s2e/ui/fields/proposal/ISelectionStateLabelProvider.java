/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
