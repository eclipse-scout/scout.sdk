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
/**
 * @version 2.x
 */
package org.eclipse.scout.nls.sdk.util.concurrent;

/**
 * <h4>UiRunnable</h4> Is used to TODO
 */
public abstract class UiRunnable implements Runnable {

  protected Object[] p_args;

  public UiRunnable(Object[] args) {
    p_args = args;
  }

}
