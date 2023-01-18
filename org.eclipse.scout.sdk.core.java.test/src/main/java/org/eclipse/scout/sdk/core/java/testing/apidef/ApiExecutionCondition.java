/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.java.testing.apidef;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import java.util.Arrays;
import java.util.Map.Entry;

import org.eclipse.scout.sdk.core.java.apidef.Api;
import org.eclipse.scout.sdk.core.java.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.java.testing.context.AbstractContextExtension;
import org.eclipse.scout.sdk.core.java.testing.context.ExtendWithJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.java.testing.context.JavaEnvironmentExtension;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.PreconditionViolationException;

public class ApiExecutionCondition implements ExecutionCondition {

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    return AbstractContextExtension.findAnnotationContext(context, EnabledFor.class)
        .map(Entry::getKey)
        .map(ef -> checkCorrectApiAvailable(ef, getJavaEnvironment(context)))
        .orElseGet(() -> enabled("Enabled by default"));
  }

  protected static ConditionEvaluationResult checkCorrectApiAvailable(EnabledFor enabledFor, IJavaEnvironment environment) {
    return Api.version(enabledFor.api(), environment)
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

  protected static IJavaEnvironment getJavaEnvironment(ExtensionContext context) {
    var env = new JavaEnvironmentExtension().getOrCreateContextFor(context);
    if (env != null) {
      return env;
    }
    throw new PreconditionViolationException("If an api requirement is given (" + EnabledFor.class.getSimpleName() + "), " +
        "the @" + ExtendWithJavaEnvironmentFactory.class.getSimpleName() + " annotation is mandatory to declare the Java environment in which the api should be searched.");
  }
}
