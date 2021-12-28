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
package org.eclipse.scout.sdk.core.testing.apidef;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

import org.eclipse.scout.sdk.core.apidef.Api;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.PreconditionViolationException;

public class ApiExecutionCondition implements ExecutionCondition {

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    var element = context.getElement();
    if (element.isEmpty()) {
      return enabled("Enabled by default");
    }
    return findAnnotation(element.orElseThrow(), EnabledFor.class)
        .map(ef -> checkCorrectApiAvailable(element.orElseThrow(), ef, context))
        .orElseGet(() -> enabled("Enabled by default"));
  }

  protected static ConditionEvaluationResult checkCorrectApiAvailable(AnnotatedElement owner, EnabledFor enabledFor, ExtensionContext context) {
    return Api.version(enabledFor.api(), getJavaEnvironment(owner, context))
        .map(v -> checkApiVersion(v.segments(), enabledFor.version(), enabledFor.require()))
        .orElseGet(() -> enabled("Enabled by default"));
  }

  protected static ConditionEvaluationResult checkApiVersion(int[] present, int[] required, ApiRequirement mode) {
    var msg = " because api '" + Arrays.toString(present) + "' is not " + mode + " '" + Arrays.toString(required) + "'.";
    if (mode.isFulfilled(present, required)) {
      return enabled("Enabled" + msg);
    }
    return disabled("Disabled" + msg);
  }

  protected static IJavaEnvironment getJavaEnvironment(AnnotatedElement owner, ExtensionContext context) {
    var env = new JavaEnvironmentExtension().getOrCreateContextFor(owner, context);
    if (env != null) {
      return env;
    }
    throw new PreconditionViolationException("If an api requirement is given (" + EnabledFor.class.getSimpleName() + "), " +
        "the @" + ExtendWithJavaEnvironmentFactory.class.getSimpleName() + " annotation is mandatory to declare the Java environment in which the api should be searched.");
  }
}
