package org.eclipse.scout.sdk.doc;

import org.eclipse.scout.sdk.core.ISourceFolders;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.model.ecj.JavaEnvironmentWithEcjBuilder;

@SuppressWarnings({"MethodMayBeStatic", "UtilityClassWithoutPrivateConstructor"})
public class PrintConstantValue {

  public static void main(String[] args) {
    new PrintConstantValue().printConstantValue();
  }

  public void printConstantValue() {
    new JavaEnvironmentWithEcjBuilder<>()
        .withSourceFolder(ISourceFolders.MAIN_JAVA_SOURCE_FOLDER)
        .accept(this::printConstantValue);
  }

  // tag::printConstantValue[]
  public static final class Constants {
    public static final String NAME = "Austin Powers";
  }

  public void printConstantValue(IJavaEnvironment javaEnvironment) {
    var num = javaEnvironment
        .requireType(Constants.class.getName())
        .fields()
        .withName("NAME")
        .first().orElseThrow()
        .constantValue().orElseThrow();

    SdkLog.warning("type: {}", num.type()); // <1>
    SdkLog.warning("value: {}", num.as(String.class)); // <2>
  }
  // end::printConstantValue[]
}
