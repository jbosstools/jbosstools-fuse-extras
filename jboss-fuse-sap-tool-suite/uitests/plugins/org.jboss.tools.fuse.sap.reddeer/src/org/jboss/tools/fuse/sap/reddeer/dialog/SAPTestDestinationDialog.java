/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.fuse.sap.reddeer.dialog;

import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.swt.api.Text;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.text.DefaultText;

public class SAPTestDestinationDialog {

	public static final String TITLE = "Test Destination Connection";

	public SAPTestDestinationDialog activate() {
		new DefaultShell(TITLE);
		return this;
	}

	public String test() {
		activate();
		final String oldResult = getResultTXT().getText();
		new PushButton("Test").click();
		new WaitUntil(new ShellIsAvailable("Progress Information"), TimePeriod.DEFAULT, false);
		new WaitWhile(new ShellIsAvailable("Progress Information"), TimePeriod.LONG);
		return getResultTXT().getText().replaceFirst(oldResult, "").trim();
	}

	public void clear() {
		activate();
		new PushButton("Clear").click();
	}

	public void close() {
		activate();
		new PushButton("Close").click();
		new WaitWhile(new ShellIsAvailable(TITLE));
	}

	public Text getResultTXT() {
		activate();
		return new DefaultText();
	}
}
