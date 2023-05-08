/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

export class Widget {
  // noinspection JSUnusedGlobalSymbols
  declare widgetMap: WidgetMap;

  enabled: boolean;
  visible: boolean;

  constructor() {
    this.enabled = true;
    this.visible = true;
  }

  // noinspection JSUnusedLocalSymbols
  widget<TId extends string & keyof WidgetMapOf<this>, T extends Widget>(widgetId: TId, type?: new() => T): WidgetMapOf<this>[TId] | T {
    return null;
  }
}

export type WidgetMap = {
  [type: string]: Widget;
};

export type WidgetMapOf<T> = T extends { widgetMap: infer TMap } ? TMap : object;
