/**
 *
 */
package sample.client.field.ext;

import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.AbstractTreeBox;

import sample.client.field.ext.AbstractCustomTreeBox.Tree;

/**
 * @author aho
 */
public abstract class AbstractCustomTreeBox extends AbstractTreeBox<Tree> {

  public class Tree extends AbstractTree {

  }
}
