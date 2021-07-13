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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.PasteFromClipboardCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;
import org.fusesource.camel.component.sap.model.rfc.DestinationDataStore;
import org.fusesource.camel.component.sap.model.rfc.RfcPackage;
import org.fusesource.camel.component.sap.model.rfc.ServerDataStore;
import org.fusesource.camel.component.sap.model.rfc.impl.DestinationDataStoreEntryImpl;
import org.fusesource.camel.component.sap.model.rfc.impl.ServerDataStoreEntryImpl;
import org.fusesource.ide.sap.ui.Messages;

public class PasteHandler extends AbstractHandler {

	private boolean isServerPaste;
	private ServerDataStore serverDataStore;
	private ServerDataStoreEntryImpl serverDataStoreEntry;
	private DestinationDataStore destinationDataStore;
	private DestinationDataStoreEntryImpl destinationDataStoreEntry;
	private EditingDomain editingDomain;
	private CompoundCommand command;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (isServerPaste) {
			InputDialog inputDialog = new InputDialog(getShell(event), Messages.ServerDialog_shellPasteTitle, Messages.ServerDialog_message, serverDataStoreEntry.getKey(),
					new NewStoreValidator(serverDataStore.getEntries().keySet(), Messages.ServerDialog_serverAlreadyExists));
			if (inputDialog.open() == Window.OK) {
				String newName = inputDialog.getValue();
				serverDataStoreEntry.setKey(newName);
				editingDomain.getCommandStack().execute(command);
			}
		} else {
			InputDialog inputDialog = new InputDialog(getShell(event), Messages.DestinationDialog_shellPasteTitle, Messages.DestinationDialog_message,
					destinationDataStoreEntry.getKey(), new NewStoreValidator(destinationDataStore.getEntries().keySet(), Messages.DestinationDialog_destinationAlreadyExists));

			if (inputDialog.open() == Window.OK) {
				String newName = inputDialog.getValue();
				destinationDataStoreEntry.setKey(newName);
				editingDomain.getCommandStack().execute(command);
			}
		}
	    return null;
	}
	
	private Shell getShell(ExecutionEvent event) {
		return HandlerUtil.getActiveWorkbenchWindow(event).getShell();
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		Object obj = HandlerUtil.getVariable(evaluationContext, ISources.ACTIVE_CURRENT_SELECTION_NAME);
		if (obj instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) obj;
			if (selection.size() == 1) {
				obj = selection.getFirstElement();
				if (obj instanceof EObject) {
					EObject eObject = (EObject) obj;
					editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(eObject);
					if (editingDomain != null) {
						Command pasteFromClipboardCommand = PasteFromClipboardCommand.create(editingDomain, eObject, null);
						if (canPaste(selection)) {
							command = new CompoundCommand();
							command.append(createAddValueCommand(obj, editingDomain.getClipboard()));
							command.append(pasteFromClipboardCommand);
							setBaseEnabled(true);
							return;
						}
					}
				}
			}
		}
		setBaseEnabled(false);
	}
	
	protected Command createAddValueCommand(Object owner, Collection<Object> values) {
		if (owner instanceof ServerDataStore) {
			isServerPaste = true;
			serverDataStore = (ServerDataStore) owner;
			CompoundCommand command = new CompoundCommand();
			for (Object value: values) {
				ServerDataStoreEntryImpl entry = (ServerDataStoreEntryImpl) value;
				command.append(AddCommand.create(editingDomain, owner, RfcPackage.Literals.SERVER_DATA_STORE__SERVER_DATA, entry.getValue()));
			}
			return command;
		} else if (owner instanceof DestinationDataStore) {
			isServerPaste = false;
			destinationDataStore = (DestinationDataStore) owner;
			CompoundCommand command = new CompoundCommand();
			for (Object value: values) {
				DestinationDataStoreEntryImpl entry = (DestinationDataStoreEntryImpl) value;
				command.append(AddCommand.create(editingDomain, owner, RfcPackage.Literals.DESTINATION_DATA_STORE__DESTINATION_DATA, entry.getValue()));
			}
			return command; 
		}
		return null;
	}

	protected boolean canPaste(IStructuredSelection selection) {
		if (selection.size() == 1 && editingDomain.getClipboard() != null && editingDomain.getClipboard().size() == 1) {
			if (selection.getFirstElement() instanceof ServerDataStore) {
				for (Object obj : editingDomain.getClipboard()) {
					if (!(obj instanceof ServerDataStoreEntryImpl)) {
						return false;
					}
					serverDataStoreEntry = (ServerDataStoreEntryImpl) obj;
				}
				return true;
			} else if (selection.getFirstElement() instanceof DestinationDataStore) {
				for (Object obj : editingDomain.getClipboard()) {
					if (!(obj instanceof DestinationDataStoreEntryImpl)) {
						return false;
					}
					destinationDataStoreEntry = (DestinationDataStoreEntryImpl) obj;
				}
				return true;
			}
		}
		return false;
	}
}
