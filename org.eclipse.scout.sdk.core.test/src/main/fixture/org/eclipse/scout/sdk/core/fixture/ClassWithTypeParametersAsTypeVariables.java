/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.sdk.core.fixture;

public class ClassWithTypeParametersAsTypeVariables<A, B extends CharSequence/*these are the type variables*/> extends ClassWithTypeVariables<A, B/*these are the type params*/> {
}
