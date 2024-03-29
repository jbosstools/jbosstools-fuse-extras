/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.fusesource.ide.sap.ui.editor.provider;

import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.fusesource.ide.camel.editor.features.create.ext.CreateEndpointFigureFeature;
import org.fusesource.ide.camel.editor.utils.CamelUtils;

public class SapQueuedIDocDestinationPaletteEntry extends AbstractSAPPaletteEntry {
	
	private static final String PROTOCOL = "sap-qidoc-destination";
	
	public static final String COMPONENT_NAME = "SAP Queued IDoc Destination"; //$NON-NLS-1$ 
	public static final String COMPONENT_DESCRIPTION = "Creates an SAP Queued IDoc Destination endpoint..."; //$NON-NLS-1$
	public static final String COMPONENT_URL = PROTOCOL + ":destination:queue:idocType:idocTypeExtension:systemRelease:applicationRelease"; //$NON-NLS-1$

	@Override
	public ICreateFeature newCreateFeature(IFeatureProvider fp) {
		return new CreateEndpointFigureFeature(fp, COMPONENT_NAME, COMPONENT_DESCRIPTION, COMPONENT_URL, getRequiredDependencies(CamelUtils.getRuntimeProvider(fp))); 
	}

	/* (non-Javadoc)
	 * @see org.fusesource.ide.camel.editor.provider.ext.ICustomPaletteEntry#getProtocol()
	 */
	@Override
	public String getProtocol() {
		return PROTOCOL;
	}
	
	/* (non-Javadoc)
	 * @see org.fusesource.ide.camel.editor.provider.ext.ICustomPaletteEntry#providesProtocol(java.lang.String)
	 */
	@Override
	public boolean providesProtocol(String protocol) {
		return PROTOCOL.equalsIgnoreCase(protocol);
	}

}
