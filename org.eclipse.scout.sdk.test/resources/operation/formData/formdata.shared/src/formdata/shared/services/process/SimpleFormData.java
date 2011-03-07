package formdata.shared.services.process;

import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.AbstractComposerData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;

public class SimpleFormData extends AbstractFormData {
  private static final long serialVersionUID = 1L;

  public SimpleFormData() {
  }

  public Long getSimpleNr() {
    return getPropertyByClass(SimpleNrProperty.class).getValue();
  }

  public SimpleNrProperty getSimpleNrProperty() {
    return getPropertyByClass(SimpleNrProperty.class);
  }

  public void setSimpleNr(Long simpleNrProperty) {
    getSimpleNrProperty().setValue(simpleNrProperty);
  }

  public SampleComposer getSampleComposer() {
    return getFieldByClass(SampleComposer.class);
  }

  public SampleDouble getSampleDouble() {
    return getFieldByClass(SampleDouble.class);
  }

  public SampleSmart getSampleSmart() {
    return getFieldByClass(SampleSmart.class);
  }

  public SampleString getSampleString() {
    return getFieldByClass(SampleString.class);
  }

  public class SimpleNrProperty extends AbstractPropertyData<Long> {
    private static final long serialVersionUID = 1L;

    public SimpleNrProperty() {
    }

  }

  public class SampleComposer extends AbstractComposerData {
    private static final long serialVersionUID = 1L;

    public SampleComposer() {
    }

  }

  public class SampleDouble extends AbstractValueFieldData<Double> {
    private static final long serialVersionUID = 1L;

    public SampleDouble() {
    }

  }

  public class SampleSmart extends AbstractValueFieldData<Long> {
    private static final long serialVersionUID = 1L;

    public SampleSmart() {
    }

  }

  public class SampleString extends AbstractValueFieldData<String> {
    private static final long serialVersionUID = 1L;

    public SampleString() {
    }

  }
}
