/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.marker.commands;

public class MultipleGlobalBindingsCommand extends AbstractNonExecutableMarkerCommand {

  public MultipleGlobalBindingsCommand() {
    super("Multiple global binding definitions found");
    setSolutionDescription("Please note, that even though the binding is a global binding, it must be defined within schema scope in order to work. Thereby, the schema chosen is ignored and does not matter.");
  }
}
