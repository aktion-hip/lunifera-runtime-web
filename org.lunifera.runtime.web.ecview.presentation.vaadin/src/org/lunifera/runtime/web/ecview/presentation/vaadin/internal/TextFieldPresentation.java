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

import java.util.Locale;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.databinding.EMFObservables;
import org.eclipse.emf.ecp.ecview.common.context.II18nService;
import org.eclipse.emf.ecp.ecview.common.editpart.IElementEditpart;
import org.eclipse.emf.ecp.ecview.common.model.core.YEmbeddableBindingEndpoint;
import org.eclipse.emf.ecp.ecview.common.model.core.YEmbeddableValueEndpoint;
import org.eclipse.emf.ecp.ecview.extension.model.extension.ExtensionModelPackage;
import org.eclipse.emf.ecp.ecview.extension.model.extension.YTextField;
import org.eclipse.emf.ecp.ecview.ui.core.editparts.extension.ITextFieldEditpart;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;

/**
 * This presenter is responsible to render a text field on the given layout.
 */
public class TextFieldPresentation extends
		AbstractFieldWidgetPresenter<Component> {

	private final ModelAccess modelAccess;
	private CssLayout componentBase;
	private TextField text;
	private ObjectProperty<String> property;

	/**
	 * Constructor.
	 * 
	 * @param editpart
	 *            The editpart of that presenter
	 */
	public TextFieldPresentation(IElementEditpart editpart) {
		super((ITextFieldEditpart) editpart);
		this.modelAccess = new ModelAccess((YTextField) editpart.getModel());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Component doCreateWidget(Object parent) {
		if (componentBase == null) {
			componentBase = new CssLayout();
			componentBase.addStyleName(CSS_CLASS_CONTROL_BASE);
			if (modelAccess.isCssIdValid()) {
				componentBase.setId(modelAccess.getCssID());
			} else {
				componentBase.setId(getEditpart().getId());
			}

			associateWidget(componentBase, modelAccess.yText);

			text = new TextField();
			text.addStyleName(CSS_CLASS_CONTROL);
			text.setNullRepresentation("");
			text.setImmediate(true);

			associateWidget(text, modelAccess.yText);

			property = new ObjectProperty<String>(null, String.class);
			text.setPropertyDataSource(property);
			
			// creates the binding for the field
			createBindings(modelAccess.yText, text);

			componentBase.addComponent(text);

			if (modelAccess.isCssClassValid()) {
				text.addStyleName(modelAccess.getCssClass());
			}

			applyCaptions();

			initializeField(text);
		}
		return componentBase;
	}

	@Override
	protected void doUpdateLocale(Locale locale) {
		// no need to set the locale to the ui elements. Is handled by vaadin
		// internally.

		// update the captions
		applyCaptions();
	}

	/**
	 * Applies the labels to the widgets.
	 */
	protected void applyCaptions() {
		II18nService service = getI18nService();
		if (service != null && modelAccess.isLabelI18nKeyValid()) {
			componentBase.setCaption(service.getValue(
					modelAccess.getLabelI18nKey(), getLocale()));
		} else {
			if (modelAccess.isLabelValid()) {
				componentBase.setCaption(modelAccess.getLabel());
			}
		}
	}

	@Override
	protected Field<?> doGetField() {
		return text;
	}

	@Override
	protected IObservable internalGetObservableEndpoint(
			YEmbeddableBindingEndpoint bindableValue) {
		if (bindableValue == null) {
			throw new IllegalArgumentException(
					"BindableValue must not be null!");
		}

		if (bindableValue instanceof YEmbeddableValueEndpoint) {
			return internalGetValueEndpoint();
		}
		throw new IllegalArgumentException("Not a valid input: "
				+ bindableValue);
	}

	/**
	 * Returns the observable to observe value.
	 * 
	 * @return
	 */
	protected IObservableValue internalGetValueEndpoint() {
		// return the observable value for text
		return EMFObservables.observeValue(castEObject(getModel()),
				ExtensionModelPackage.Literals.YTEXT_FIELD__VALUE);
	}

	/**
	 * Creates the bindings for the given values.
	 * 
	 * @param yField
	 * @param field
	 */
	protected void createBindings(YTextField yField, TextField field) {
		// create the model binding from ridget to ECView-model
		registerBinding(createBindings_Value(castEObject(getModel()),
				ExtensionModelPackage.Literals.YTEXT_FIELD__VALUE, text));

		super.createBindings(yField, field, componentBase);
	}

	@Override
	public Component getWidget() {
		return componentBase;
	}

	@Override
	public boolean isRendered() {
		return componentBase != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doUnrender() {
		if (componentBase != null) {

			// unbind all active bindings
			unbind();

			ComponentContainer parent = ((ComponentContainer) componentBase
					.getParent());
			if (parent != null) {
				parent.removeComponent(componentBase);
			}

			// remove assocations
			unassociateWidget(componentBase);
			unassociateWidget(text);

			componentBase = null;
			text = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalDispose() {
		try {
			unrender();
		} finally {
			super.internalDispose();
		}
	}

	/**
	 * A helper class.
	 */
	private static class ModelAccess {
		private final YTextField yText;

		public ModelAccess(YTextField yText) {
			super();
			this.yText = yText;
		}

		/**
		 * @return
		 * @see org.eclipse.emf.ecp.ecview.ui.core.model.core.YCssAble#getCssClass()
		 */
		public String getCssClass() {
			return yText.getCssClass();
		}

		/**
		 * Returns true, if the css class is not null and not empty.
		 * 
		 * @return
		 */
		public boolean isCssClassValid() {
			return getCssClass() != null && !getCssClass().equals("");
		}

		/**
		 * @return
		 * @see org.eclipse.emf.ecp.ecview.ui.core.model.core.YCssAble#getCssID()
		 */
		public String getCssID() {
			return yText.getCssID();
		}

		/**
		 * Returns true, if the css id is not null and not empty.
		 * 
		 * @return
		 */
		public boolean isCssIdValid() {
			return getCssID() != null && !getCssID().equals("");
		}

		/**
		 * Returns true, if the label is valid.
		 * 
		 * @return
		 */
		public boolean isLabelValid() {
			return yText.getDatadescription() != null
					&& yText.getDatadescription().getLabel() != null;
		}

		/**
		 * Returns the label.
		 * 
		 * @return
		 */
		public String getLabel() {
			return yText.getDatadescription().getLabel();
		}

		/**
		 * Returns true, if the label is valid.
		 * 
		 * @return
		 */
		public boolean isLabelI18nKeyValid() {
			return yText.getDatadescription() != null
					&& yText.getDatadescription().getLabelI18nKey() != null;
		}

		/**
		 * Returns the label.
		 * 
		 * @return
		 */
		public String getLabelI18nKey() {
			return yText.getDatadescription().getLabelI18nKey();
		}
	}
}
