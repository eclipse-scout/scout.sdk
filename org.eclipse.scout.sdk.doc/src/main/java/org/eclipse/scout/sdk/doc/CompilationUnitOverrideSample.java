package org.eclipse.scout.sdk.doc;

import org.eclipse.scout.sdk.core.generator.type.PrimaryTypeGenerator;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentFactories.EmptyJavaEnvironmentFactory;
import org.eclipse.scout.sdk.core.util.JavaTypes;

@SuppressWarnings("MethodMayBeStatic")
public class CompilationUnitOverrideSample {

  public static void main(String[] args) {
    new CompilationUnitOverrideSample().run();
  }

  public void run() {
    new EmptyJavaEnvironmentFactory().accept(this::generatorToModel);
  }

  protected void generatorToModel(IJavaEnvironment javaEnvironment) {
    // tag::generatorToModel[]
    var packageName = "org.eclipse.scout.sdk.doc";
    var className = "EmptyCloseable";

    var source = PrimaryTypeGenerator.create() // <1>
        .withPackageName(packageName)
        .asPublic()
        .withInterface(AutoCloseable.class.getName())
        .withElementName(className)
        .withAllMethodsImplemented()
        .toJavaSource(javaEnvironment);
    var requiresReload = javaEnvironment.registerCompilationUnitOverride(packageName,
        className + JavaTypes.JAVA_FILE_SUFFIX, source); // <2>
    if (requiresReload) {
      javaEnvironment.reload(); // <3>
    }

    var closeMethodSource = javaEnvironment.requireType(packageName + '.' + className)
        .methods()
        .withName("close")
        .first().get()
        .source().get()
        .asCharSequence().toString(); // <4>
    SdkLog.warning(closeMethodSource);
    // end::generatorToModel[]
  }
}
