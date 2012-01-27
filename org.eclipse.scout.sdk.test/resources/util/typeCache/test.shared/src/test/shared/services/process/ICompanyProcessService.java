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
package test.shared.services.process;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.service.IService;

public interface ICompanyProcessService extends IService {

  CompanyFormData prepareCreate(CompanyFormData formData) throws ProcessingException;

  CompanyFormData create(CompanyFormData formData) throws ProcessingException;

  CompanyFormData load(CompanyFormData formData) throws ProcessingException;

  CompanyFormData store(CompanyFormData formData) throws ProcessingException;
}
