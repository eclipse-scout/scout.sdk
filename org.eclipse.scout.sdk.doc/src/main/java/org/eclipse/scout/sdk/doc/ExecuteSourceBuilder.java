package org.eclipse.scout.sdk.doc;

import static java.lang.System.lineSeparator;

import java.nio.file.Paths;

import org.eclipse.scout.sdk.core.builder.BuilderContext;
import org.eclipse.scout.sdk.core.builder.IBuilderContext;
import org.eclipse.scout.sdk.core.builder.MemorySourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.JavaSourceBuilder;
import org.eclipse.scout.sdk.core.builder.java.expression.ExpressionBuilder;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;

@SuppressWarnings({"MethodMayBeStatic", "TypeMayBeWeakened"})
public class ExecuteSourceBuilder {

  public static void main(String[] args) {
    new ExecuteSourceBuilder().executeBuilders();
  }

  public void executeBuilders() {
    execSimple();
    new EmptyJavaEnvironmentFactory().accept(this::execComplex);
  }

  public void execSimple() {
    // tag::builderSimple[]
    var builder = MemorySourceBuilder.create(); // <1>
    builder.appendLine("Hello World!"); // <2>
    SdkLog.warning(builder.toString()); // <3>
    // end::builderSimple[]
  }

  public void execComplex(IJavaEnvironment javaEnvironment) {
    // tag::builderComplex[]
    var workingDir = Paths.get("").toAbsolutePath();

    var context = new BuilderContext(lineSeparator()); // <1>
    context.properties().setProperty(IBuilderContext.PROPERTY_TARGET_PATH, workingDir); // <2>

    var rootBuilder = MemorySourceBuilder.create(context); // <3>
    var javaBuilder = JavaSourceBuilder.create(rootBuilder, javaEnvironment); // <4>
    var exprBuilder = ExpressionBuilder.create(javaBuilder); // <5>

    var source = exprBuilder
        .stringLiteralArray("1", "2", "3") // <6>
        .semicolon() // <7>
        .toString();
    SdkLog.warning(source); // <8>
    // end::builderComplex[]
  }
}
