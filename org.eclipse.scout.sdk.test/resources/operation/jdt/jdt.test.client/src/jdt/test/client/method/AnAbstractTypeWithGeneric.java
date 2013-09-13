/**
 *
 */
package jdt.test.client.method;

import java.util.List;

/**
 * @author aho
 */
public abstract class AnAbstractTypeWithGeneric<T> {

  public List<T> getValue() {
    return null;
  }

  public void setValue(T value) {

  }
}
