/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package dataobject;

import java.util.List;

import org.eclipse.scout.rt.dataobject.DoValue;

public interface DataObjectTestInterface {
  DoValue<Boolean> enabled();

  List<Long> getVersions();

  String getStringAttribute();
}
