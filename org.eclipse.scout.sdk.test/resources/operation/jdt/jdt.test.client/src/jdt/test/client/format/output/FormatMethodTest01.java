/**
 *
 */
package jdt.test.client.format.output;

import java.io.File;
import java.util.ArrayList;

/**
 * @author aho
 */
public class FormatMethodTest01 {

  private String m_input;

  public FormatMethodTest01(String input) {
    m_input = input;
  }

  public static void toBeFormatted(ArrayList<String> input01, File file) throws Exception {
    boolean notTrue = false;
    if (notTrue) {
      file = new File("");
    }
    else {
      input01.add("");
    }

  }
}
