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
package org.eclipse.scout.sdk.ui.extensions.preferences;

import java.util.List;

import org.eclipse.swt.widgets.Composite;

/**
 * <h3>{@link IScoutProjectScrolledContent}</h3> ...
 * 
 * @author Matthias Villiger
 * @since 3.9.0 17.12.2012
 */
public interface IScoutProjectScrolledContent<T> {
  void save();

  void reset();

  void setVisible(boolean visible);

  void createContent(Composite parent);

  void setSearchPattern(String pattern);

  void reflow();

  void loadModel(List<T> entries, IModelLoadProgressObserver<T> observer);

  public interface IModelLoadProgressObserver<U> {
    void loaded(U justLoadedModel);
  }
}
