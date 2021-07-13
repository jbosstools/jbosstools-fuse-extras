/*******************************************************************************
* Copyright (c) 2014 Red Hat, Inc.
* Distributed under license by Red Hat, Inc. All rights reserved.
* This program is made available under the terms of the
* Eclipse Public License v1.0 which accompanies this distribution,
* and is available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Red Hat, Inc. - initial API and implementation
* William Collins punkhornsw@gmail.com
******************************************************************************/ 
package org.fusesource.ide.sap.ui.command;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.edit.command.CommandParameter;
import org.eclipse.emf.edit.command.CreateChildCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;
import org.fusesource.camel.component.sap.model.rfc.DestinationData;
import org.fusesource.camel.component.sap.model.rfc.DestinationDataStore;
import org.fusesource.camel.component.sap.model.rfc.RfcPackage;
import org.fusesource.camel.component.sap.model.rfc.impl.DestinationDataStoreEntryImpl;
import org.fusesource.ide.sap.ui.Messages;

public class NewDestinationHandler extends AbstractHandler {

	private EditingDomain editingDomain;
	private CompoundCommand command;
	private DestinationDataStore destinationDataStore;
	private DestinationDataStoreEntryImpl destinationDataStoreEntry;
	private DestinationData destinationData;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = getShell(event);

		InputDialog inputDialog = createDialogAskingForName(shell);

		if (inputDialog.open() == Window.OK) {
			String newName = inputDialog.getValue();
			destinationDataStoreEntry.setKey(newName);
			editingDomain.getCommandStack().execute(command);
		}
		return null;
	}

	/**
	 * @param shell
	 * @return
	 */
	private InputDialog createDialogAskingForName(Shell shell) {
		return new InputDialog(shell, Messages.DestinationDialog_shellCreateTitle, Messages.DestinationDialog_message, "",
				new NewStoreValidator(destinationDataStore.getEntries().keySet(), Messages.DestinationDialog_destinationAlreadyExists));
	}

	/**
	 * @param event
	 * @return
	 */
	private Shell getShell(ExecutionEvent event) {
		return HandlerUtil.getActiveWorkbenchWindow(event).getShell();
	}
	
	@Override
	public void setEnabled(Object evaluationContext) {
		Command createDestinationDataStoreEntryCommand = null;
		Command createDestinationDataCommand = null;
		Object obj = getActiveSelection(evaluationContext);
		if (obj instanceof IStructuredSelection) {
			obj = ((IStructuredSelection)obj).getFirstElement();
			if (obj instanceof DestinationDataStore){
				destinationDataStore = (DestinationDataStore) obj;
				editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(destinationDataStore);
				if (editingDomain != null) {
					Collection<?> descriptors = editingDomain.getNewChildDescriptors(destinationDataStore, null);
					for (Object descriptor: descriptors) {
						CommandParameter parameter = (CommandParameter) descriptor;
						if (parameter.getFeature() == RfcPackage.Literals.DESTINATION_DATA_STORE__ENTRIES) {
							destinationDataStoreEntry = (DestinationDataStoreEntryImpl) parameter.getValue();
							createDestinationDataStoreEntryCommand = CreateChildCommand.create(editingDomain, destinationDataStore, descriptor, Collections.singletonList(destinationDataStore));
							continue;
						} else if (parameter.getFeature() == RfcPackage.Literals.DESTINATION_DATA_STORE__DESTINATION_DATA) {
							destinationData = (DestinationData) parameter.getValue();
							createDestinationDataCommand = CreateChildCommand.create(editingDomain, destinationDataStore, descriptor, Collections.singletonList(destinationDataStore));
							continue;
						}

					}
					if(createDestinationDataStoreEntryCommand != null && createDestinationDataCommand != null) {
						command = new CompoundCommand("Create Destination Data Store Entry"); // $NON-NLS-N$
						command.append(createDestinationDataCommand);
						command.append(SetCommand.create(editingDomain, destinationDataStoreEntry, RfcPackage.Literals.DESTINATION_DATA_STORE_ENTRY__VALUE, destinationData));
						command.append(createDestinationDataStoreEntryCommand);
						setBaseEnabled(true);
						return;
					}
				}
				
			}
		}		
		setBaseEnabled(false);
	}

	/**
	 * @param evaluationContext
	 * @return
	 */
	Object getActiveSelection(Object evaluationContext) {
		return HandlerUtil.getVariable(evaluationContext, ISources.ACTIVE_CURRENT_SELECTION_NAME);
	}
	
}
