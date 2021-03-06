/**
 * Copyright (c) 2011 - 2014, Lunifera GmbH (Gross Enzersdorf), Loetz KG (Heidelberg)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * 		Florian Pirchner - Initial implementation
 */
package org.lunifera.runtime.web.ecview.presentation.vaadin;

/**
 * Constants about the swt simple presentation.
 */
// BEGIN SUPRESS CATCH EXCEPTION
public interface IConstants {
	// END SUPRESS CATCH EXCEPTION
	/**
	 * This CSS class is applied to the base composite of presentations. The
	 * base composite is used to enable margins on a control and to allow a
	 * control to rebuild its SWT controls at the "base composite parent".
	 */
	String CSS_CLASS_CONTROL_BASE = "controlbase";

	/**
	 * This CSS class is applied to the control of a presentation as far as the
	 * model element does not specify its own CSS class.
	 */
	public static final String CSS_CLASS_CONTROL = "control";

	/**
	 * This CSS class marks the widget to show a margin.
	 */
	public static final String CSS_CLASS_MARGIN = "margin";

	/**
	 * This CSS class marks the widget to show a spacing.
	 */
	public static final String CSS_CLASS_SPACING = "spacing";

	/**
	 * This CSS class for master-detail master-base component.
	 */
	String CSS_CLASS_MASTER_BASE = "masterbase";

	/**
	 * This CSS class for master-detail detail-base component.
	 */
	String CSS_CLASS_DETAIL_BASE = "detailbase";
}
