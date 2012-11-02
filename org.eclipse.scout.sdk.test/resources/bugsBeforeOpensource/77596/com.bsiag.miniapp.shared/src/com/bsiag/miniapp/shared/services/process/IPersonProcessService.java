/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.miniapp.shared.services.process;

import org.eclipse.scout.service.IService;
import com.bsiag.miniapp.shared.services.process.PersonFormData;
import org.eclipse.scout.commons.exception.ProcessingException;

public interface IPersonProcessService extends IService{

  PersonFormData prepareCreate(PersonFormData formData) throws ProcessingException;

  PersonFormData create(PersonFormData formData) throws ProcessingException;

  PersonFormData load(PersonFormData formData) throws ProcessingException;

  PersonFormData store(PersonFormData formData) throws ProcessingException;
}
