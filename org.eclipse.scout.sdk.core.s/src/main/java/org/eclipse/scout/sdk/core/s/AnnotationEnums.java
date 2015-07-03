/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.core.s;

/**
 * Enums used in annotations of the scout runtime.
 */
public final class AnnotationEnums {
  private AnnotationEnums() {
  }

  public static enum SdkCommand {
    CREATE, USE, IGNORE, DEFAULT
  }

  public static enum DefaultSubtypeSdkCommand {
    CREATE, IGNORE, DEFAULT
  }

  public static enum SdkColumnCommand {
    CREATE, IGNORE
  }
}
