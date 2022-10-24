/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.doc;

import org.eclipse.scout.sdk.core.generator.ISourceGenerator;
import org.eclipse.scout.sdk.core.generator.field.FieldGenerator;
import org.eclipse.scout.sdk.core.generator.field.IFieldGenerator;
import org.eclipse.scout.sdk.core.generator.method.IMethodGenerator;
import org.eclipse.scout.sdk.core.generator.method.MethodOverrideGenerator;
import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IField;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.api.IMethod;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.transformer.DefaultWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer;
import org.eclipse.scout.sdk.core.transformer.IWorkingCopyTransformer.ITransformInput;
import org.eclipse.scout.sdk.core.transformer.SimpleWorkingCopyTransformerBuilder;

@SuppressWarnings("MethodMayBeStatic")
public class WorkingCopyTransformerSample {

  public static void main(String[] args) {
    new WorkingCopyTransformerSample().run();
  }

  public void run() {
    new EmptyJavaEnvironmentFactory().accept(this::transformExceptionClass);
    new EmptyJavaEnvironmentFactory().accept(this::transformAutoGeneration);
  }

  // tag::transformWorkingCopy[]
  protected void transformExceptionClass(IJavaEnvironment javaEnvironment) {
    var type = javaEnvironment.requireType(IllegalArgumentException.class.getName());
    var generator = type.toWorkingCopy(new SimpleWorkingCopyTransformerBuilder() // <1>
        .withFieldMapper(this::transformFields) // <2>
        .withMethodParameterMapper(IWorkingCopyTransformer::remove) // <3>
        .build());
    var source = generator.toJavaSource();
    SdkLog.warning(source);
  }

  protected IFieldGenerator<?> transformFields(ITransformInput<IField, IFieldGenerator<?>> input) {
    var defaultGenerator = input.requestDefaultWorkingCopy(); // <4>
    if (FieldGenerator.SERIAL_VERSION_UID.equals(defaultGenerator.elementName().orElse(""))) {
      // modify value of serialVersionUID field
      defaultGenerator.withValue(ISourceGenerator.raw("42L")); // <5>
    }
    return defaultGenerator;
  }
  // end::transformWorkingCopy[]

  public void transformAutoGeneration(IJavaEnvironment javaEnvironment) {
    // tag::transformAutoGeneration[]
    IWorkingCopyTransformer transformer = new DefaultWorkingCopyTransformer() { // <1>
      @Override
      public IMethodGenerator<?, ?> transformMethod(
          ITransformInput<IMethod, IMethodGenerator<?, ?>> in) {
        var templateMethod = in.model(); // <2>
        var defaultGenerator = in.requestDefaultWorkingCopy(); // <3>
        return switch (templateMethod.elementName()) {
          case "toString" -> defaultGenerator.withBody(b -> b.returnClause().stringLiteral("SampleCloseable class").semicolon()); // <4>
          case "close" -> defaultGenerator.withoutThrowable(f -> true); // <5>
          default -> defaultGenerator;
        };
      }
    };

    var generator = PrimaryTypeGenerator.create()
        .withElementName("SampleCloseable")
        .withInterface(AutoCloseable.class.getName()) // <6>
        .withMethod(MethodOverrideGenerator.createOverride(transformer) // <7>
            .withElementName("toString"))
        .withAllMethodsImplemented(transformer); // <8>
    SdkLog.warning(generator.toJavaSource(javaEnvironment).toString());
    // end::transformAutoGeneration[]
  }
}
