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
package org.eclipse.scout.sdk;

/**
 * Service for getting and providing feedback from and to the user, respectively.
 * 
 * @since 3.10.0-M1
 */
public interface IMessageBoxService {

  public enum YesNo {
    YES,
    NO;
  }

  /**
   * Shows the given message to the user as well as some means for <em>Yes</em> and <em>No</em>. If the interaction with
   * the user is not possible, the default answer is returned.
   * 
   * @param title
   * @param message
   * @param defaultAnswer
   * @return
   */
  YesNo showYesNoQuestion(String title, String message, YesNo defaultAnswer);

  /**
   * Shows the given warning.
   * 
   * @param title
   * @param message
   */
  void showWarning(String title, String message);
}
