package signature.tests;

import signature.tests.SignatureRefType.InnerType01;
import signature.tests.SignatureRefType.InnerType02;


/**
 * <h3>{@link SignatureTest}</h3> ...
 *
 * @author aho
 * @since 3.8.0 12.02.2013
 */
public class SignatureTest {

  public void setAnInnerType01(InnerType01 type) {

  }

  public void setAnInnerType02(SignatureRefType.InnerType01 type) {

  }

  public void setAnInnerType03(signature.tests.SignatureRefType.InnerType02 type) {

  }

  public void setAnInnerType04(InnerType02.InnerType03 type) {

  }

  public void setAnInnerType05(SignatureRefType.InnerType02.InnerType03 type) {

  }

  public void setAnInnerType06(signature.tests.SignatureRefType.InnerType02.InnerType03 type) {

  }

}
