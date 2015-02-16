package test.client;

/**
 *
 * <h3>{@link MethodBodyTest}</h3>
 * <b>NOTE: Tests using this class relay on source positions. Do not change any charakter of this class!</b>
 *
 * @author Scout Robot
 * @since 3.8.0 22.02.2013
 */
public class MethodBodyTest {

  public MethodBodyTest(){

  }
  public static final void doStaticStuff() {

  }

  public final void doFinalStuff() {

  }

  public void methodWithLongContent() {
    StringBuilder builder = new StringBuilder("");
    {
      builder.append(true);
    }
    if (builder.length() > 0) {
      builder.append("any String");
    }
    else if (builder.length() < 0) {
      builder.replace(0, 1, "");
    }
    else {
      builder.toString();
    }
    switch (builder.length()) {
      case 1:
        System.out.println("length is 1");
        break;

      default:
        System.out.println("length is not 1");
        break;
    }
  }

  public void methodWithException() throws NoSuchMethodException {
    // todo
  }

  public void methodWithComment()/*{} a comment*/{

  }

  public int returnAnInteger(){
    return 5;
  }

}
