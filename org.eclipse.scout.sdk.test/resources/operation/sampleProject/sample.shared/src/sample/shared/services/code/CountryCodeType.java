package sample.shared.services.code;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;

public class CountryCodeType extends AbstractCodeType<Long, Long> {

  private static final long serialVersionUID = 1L;
  public static final Long ID = 1365148677637L;

  public CountryCodeType() throws ProcessingException {
    super();
  }

  @Override
  public Long getId() {
    return ID;
  }

  @Order(10.0)
  public static class SwitzerlandCode extends AbstractCode<Long> {

    private static final long serialVersionUID = 1L;
    public static final Long ID = 1365148697761L;

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("Switzerland");
    }

    @Override
    public Long getId() {
      return ID;
    }
  }

  @Order(20.0)
  public static class GermanyCode extends AbstractCode<Long> {

    private static final long serialVersionUID = 1L;
    public static final Long ID = 1365148709404L;

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("Germany");
    }

    @Override
    public Long getId() {
      return ID;
    }
  }

  @Order(30.0)
  public static class AustriaCode extends AbstractCode<Long> {

    private static final long serialVersionUID = 1L;
    public static final Long ID = 1365148718089L;

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("Austria");
    }

    @Override
    public Long getId() {
      return ID;
    }
  }
}
