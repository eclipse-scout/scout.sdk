/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.doc;

import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.typescript.model.api.INodeModule;
import org.eclipse.scout.sdk.core.typescript.model.api.IVariable;

public final class ObjectLiteralSample {

  private ObjectLiteralSample() {
  }

  public static void objectLiteralSample(INodeModule nodeModule) {
    // tag::objectLiteralSample[]
    var variableWithObjectLiteral = nodeModule
        .export("VariableWithObjectLiteral") // <1>
        .map(IVariable.class::cast) // <2>
        .orElseThrow();
    var objectLiteral = variableWithObjectLiteral
        .constantValue() // <3>
        .asObjectLiteral() // <4>
        .orElseThrow(); // <5>
    var valueOfB = objectLiteral
        .propertyAsObjectLiteral("nested").orElseThrow() // <6>
        .property("b").orElseThrow() // <7>
        .asBoolean().orElseThrow(); // <8>
    SdkLog.warning("Value of 'b': {}.", valueOfB); // <9>
    // end::objectLiteralSample[]
  }
}
