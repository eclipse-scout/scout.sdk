/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import java.awt.*;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCode;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;

@SuppressWarnings("ALL")
@ClassId("8e558241-1d69-4a17-8f3a-afe9f6a081b3")
public class TestCodeType extends AbstractCodeType<Long, Color> {

  private static final long serialVersionUID = 1L;
  public static final Long ID = 20000L;

  @Override
  protected String getConfiguredText() {
    return TEXTS.get("Colors");
  }

  @Override
  public Long getId() {
    return ID;
  }

  @Order(1000)
  @ClassId("815af799-0eb0-454d-b6d3-ce67289cc05b")
  public static class BlackCode extends AbstractCode<Color> {
    private static final long serialVersionUID = 1L;
    public static final Color ID = Color.BLACK;

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("Black");
    }

    @Override
    public Color getId() {
      return ID;
    }
  }

  Code<caret>
  
  @Order(2000)
  @ClassId("dd2d969e-46fd-48a6-9d5e-04c0c51ccca3")
  public static class BlueCode extends AbstractCode<Color> {
    private static final long serialVersionUID = 1L;
    public static final Color ID = Color.BLUE;

    @Override
    protected String getConfiguredText() {
      return TEXTS.get("Blue");
    }

    @Override
    public Color getId() {
      return ID;
    }
  }
}
