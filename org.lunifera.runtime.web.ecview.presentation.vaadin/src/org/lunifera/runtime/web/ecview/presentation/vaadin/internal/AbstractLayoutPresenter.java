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
package org.lunifera.runtime.web.ecview.presentation.vaadin.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecp.ecview.common.editpart.IEmbeddableEditpart;
import org.eclipse.emf.ecp.ecview.common.editpart.ILayoutEditpart;
import org.eclipse.emf.ecp.ecview.common.model.core.YLayout;
import org.eclipse.emf.ecp.ecview.common.presentation.ILayoutPresentation;

import com.vaadin.ui.ComponentContainer;

/**
 * An abstract base class implementing {@link ILayoutPresentation}.
 */
public abstract class AbstractLayoutPresenter<A extends ComponentContainer>
		extends AbstractVaadinWidgetPresenter<A> implements
		ILayoutPresentation<A> {

	private List<IEmbeddableEditpart> children;

	private boolean renderLock;

	public AbstractLayoutPresenter(ILayoutEditpart editpart) {
		super(editpart);
	}

	/**
	 * Returns the editpart the presenter will render for.
	 * 
	 * @return the editpart
	 */
	protected ILayoutEditpart getEditpart() {
		return (ILayoutEditpart) super.getEditpart();
	}

	@Override
	public YLayout getModel() {
		return (YLayout) getEditpart().getModel();
	}

	@Override
	public List<IEmbeddableEditpart> getChildren() {
		return children != null ? Collections.unmodifiableList(children)
				: Collections.<IEmbeddableEditpart> emptyList();
	}

	@Override
	public boolean contains(IEmbeddableEditpart presentation) {
		return children != null && children.contains(presentation);
	}

	@Override
	public void add(IEmbeddableEditpart presentation) {
		ensureChildren();

		if (!children.contains(presentation)) {
			children.add(presentation);

			if (!renderLock) {
				internalAdd(presentation);
			}
		}
	}

	/**
	 * This method is called after the editpart was successfully added to the
	 * children collection.<br>
	 * Subclasses should handle the add of the UI element in that method.
	 * 
	 * @param editpart
	 *            The editpart to be added
	 */
	protected void internalAdd(IEmbeddableEditpart presentation) {

	}

	@Override
	public void remove(IEmbeddableEditpart presentation) {
		if (children == null) {
			return;
		}

		if (children.remove(presentation) && !renderLock) {
			internalRemove(presentation);
		}
	}

	/**
	 * This method is called after the editpart was successfully removed from
	 * the children collection.<br>
	 * Subclasses should handle the removal of the UI element in that method.
	 * 
	 * @param presentation
	 *            The presentation to be removed
	 */
	protected void internalRemove(IEmbeddableEditpart presentation) {

	}

	@Override
	public void insert(IEmbeddableEditpart presentation, int index) {
		ensureChildren();

		int currentIndex = children.indexOf(presentation);
		if (currentIndex > -1 && currentIndex != index) {
			throw new RuntimeException(
					String.format(
							"Insert at index %d not possible since presentation already contained at index %d",
							index, currentIndex));
		}

		children.add(index, presentation);
		internalInsert(presentation, index);
	}

	/**
	 * This method is called after the editpart was successfully inserted to the
	 * children collection.<br>
	 * Subclasses should handle the insert of the UI element in that method.
	 * 
	 * @param editpart
	 *            The editpart to be inserted
	 * @param index
	 *            The index where the editpart should be inserted
	 */
	protected void internalInsert(IEmbeddableEditpart editpart, int index) {

	}

	@Override
	public void move(IEmbeddableEditpart presentation, int index) {
		if (children == null) {
			throw new RuntimeException(
					"Move not possible. No children present.");
		}

		if (!children.contains(presentation)) {
			throw new RuntimeException(
					String.format(
							"Move to index %d not possible since presentation not added yet!",
							index));
		}

		int currentIndex = children.indexOf(presentation);
		children.remove(presentation);
		children.add(index, presentation);

		if (!renderLock) {
			internalMove(presentation, currentIndex, index);
		}
	}

	/**
	 * This method is called after the presentation was successfully moved
	 * inside the children collection.<br>
	 * Subclasses should handle the move of the UI element in that method.
	 * 
	 * @param presentation
	 *            The presentation to be moved.
	 * @param oldIndex
	 *            The old index where the control was located.
	 * @param newIndex
	 *            The new index where the control should be located after the
	 *            move operation.
	 */
	protected void internalMove(IEmbeddableEditpart presentation, int oldIndex,
			int newIndex) {

	}

	/**
	 * Returns true, if rendering should not be done.
	 * 
	 * @return
	 */
	public boolean isRenderLock() {
		return renderLock;
	}

	/**
	 * True, if rendering should not be done.
	 * 
	 * @param renderLock
	 */
	public void setRenderLock(boolean renderLock) {
		this.renderLock = renderLock;
	}

	/**
	 * Ensures, that the children collection exists.
	 */
	protected void ensureChildren() {
		if (children == null) {
			children = new ArrayList<IEmbeddableEditpart>();
		}
	}

	@Override
	protected void internalDispose() {
		try {
			if (children != null) {
				children.clear();
				children = null;
			}
		} finally {

		}
	}
}
