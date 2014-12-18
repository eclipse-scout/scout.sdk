package signature.tests.generic.context;

import org.eclipse.scout.rt.client.ui.form.fields.listbox.AbstractListBox;

public class TemplateUse extends AbstractListBox<Long>  {
	public class ConcreteTemplateUse extends TemplateWithTypeParam<Number> {

	}
}
