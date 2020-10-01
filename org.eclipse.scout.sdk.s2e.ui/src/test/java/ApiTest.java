import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;

import org.eclipse.jface.text.link.LinkedModeUI;
import org.junit.jupiter.api.Test;

public class ApiTest {

  @Test
  public void testLinkedModeUIApi() throws NoSuchMethodException {
    Method m = LinkedModeUI.class.getDeclaredMethod("triggerContentAssist");
    assertNotNull(m);
  }
}
