/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {EnumObject, Widget} from '../../index';

export class FormField extends Widget {

  label: string;
  labelVisible: boolean;
  labelPosition: FormFieldLabelPosition;

  constructor() {
    super();
    this.label = null;
    this.labelVisible = true;
    this.labelPosition = FormField.LabelPosition.DEFAULT;
  }

  static LabelPosition = {
    DEFAULT: 0,
    LEFT: 1,
    ON_FIELD: 2,
    RIGHT: 3,
    TOP: 4,
    BOTTOM: 5
  } as const;
}

export type FormFieldLabelPosition = EnumObject<typeof FormField.LabelPosition>;
