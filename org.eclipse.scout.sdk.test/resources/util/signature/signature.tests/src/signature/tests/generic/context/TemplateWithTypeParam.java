package signature.tests.generic.context;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;

public abstract class TemplateWithTypeParam<KEY> {
	public class InnerColumn extends AbstractColumn<KEY> {

	}
}
