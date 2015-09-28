/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.sdk.core.fixture;

import java.nio.CharBuffer;

public class ClassWithTypeParametersAsTypeVariables<A, B extends CharBuffer/*these are the type variables*/> extends ClassWithTypeVariables<A, B/*these are the type params*/> {
}
