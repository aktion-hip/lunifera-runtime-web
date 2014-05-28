/**
 * Copyright (c) 2012 Lunifera GmbH (Austria) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Florian Pirchner - initial API and implementation
 */
package org.lunifera.runtime.web.ecview.presentation.vaadin.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecp.ecview.common.context.IViewContext;
import org.eclipse.emf.ecp.ecview.common.disposal.AbstractDisposable;
import org.eclipse.emf.ecp.ecview.common.editpart.visibility.IVisibilityPropertiesEditpart;
import org.eclipse.emf.ecp.ecview.common.model.core.YEmbeddable;
import org.eclipse.emf.ecp.ecview.common.model.core.YEmbeddableBindingEndpoint;
import org.eclipse.emf.ecp.ecview.common.model.core.util.CoreModelUtil;
import org.eclipse.emf.ecp.ecview.common.notification.ILifecycleEvent;
import org.eclipse.emf.ecp.ecview.common.notification.ILifecycleService;
import org.eclipse.emf.ecp.ecview.common.notification.LifecycleEvent;
import org.eclipse.emf.ecp.ecview.common.presentation.IWidgetPresentation;
import org.eclipse.emf.ecp.ecview.ui.core.editparts.extension.ITabEditpart;
import org.lunifera.runtime.web.ecview.presentation.vaadin.IBindingManager;
import org.lunifera.runtime.web.ecview.presentation.vaadin.IConstants;

import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet.Tab;

/**
 * An abstract implementation of the {@link IWidgetPresentation}.
 */
public abstract class AbstractTabPresenter<A extends Component> extends
		AbstractDisposable implements IWidgetPresentation<A> {

	/**
	 * See {@link IConstants#CSS_CLASS__CONTROL_BASE}.
	 */
	public static final String CSS_CLASS__CONTROL_BASE = IConstants.CSS_CLASS__CONTROL_BASE;

	/**
	 * See {@link IConstants#CSS_CLASS__CONTROL}.
	 */
	public static final String CSS_CLASS__CONTROL = IConstants.CSS_CLASS__CONTROL;

	// a reference to viewContext is required for disposal. Otherwise the view
	// may not become accessed
	private IViewContext viewContext;

	private final ITabEditpart editpart;

	private IBindingManager bindingManger;
	private Set<Binding> bindings = new HashSet<Binding>();

	private IViewContext disposingTempViewContext;

	public AbstractTabPresenter(ITabEditpart editpart) {
		this.editpart = editpart;
		viewContext = editpart.getView().getContext();
	}

	/**
	 * Returns the editpart the presenter will render for.
	 * 
	 * @return the editpart
	 */
	protected ITabEditpart getEditpart() {
		return editpart;
	}

	@Override
	public Object getModel() {
		return getEditpart().getModel();
	}

	/**
	 * Returns the view context.
	 * 
	 * @return viewContext
	 */
	public IViewContext getViewContext() {
		return viewContext;
	}

	@Override
	public void apply(IVisibilityPropertiesEditpart properties) {

	}

	@Override
	public void resetVisibilityProperties() {

	}

	/**
	 * Applies the defaults to the ecview model. Transient values will be
	 * configured properly.
	 * 
	 * @param yEmbeddable
	 */
	protected void applyDefaults(YEmbeddable yEmbeddable) {
		// initialize the transient values
		//
		CoreModelUtil.initTransientValues(yEmbeddable);
	}

	/**
	 * Registers the given binding to be managed by the presenter. If the widget
	 * becomes disposed or unrendered, all the bindings will become disposed.
	 * 
	 * @param binding
	 */
	protected void registerBinding(Binding binding) {
		bindings.add(binding);
	}

	/**
	 * Unbinds all currently active bindings.
	 */
	protected void unbind() {
		for (Binding binding : bindings) {
			binding.dispose();
		}
		bindings.clear();
	}

	@Override
	public IObservable getObservableValue(Object model) {
		return internalGetObservableEndpoint((YEmbeddableBindingEndpoint) model);
	}

	/**
	 * Has to provide an instance of IObservable for the given bindableValue.
	 * 
	 * @param bindableValue
	 * @return
	 */
	protected IObservable internalGetObservableEndpoint(
			YEmbeddableBindingEndpoint bindableValue) {
		throw new UnsupportedOperationException("Must be overridden!");
	}

	protected EObject castEObject(Object model) {
		return (EObject) model;
	}

	@Override
	protected void internalDispose() {

	}

	@Override
	protected void notifyDisposeListeners() {
		super.notifyDisposeListeners();
		sendDisposedLifecycleEvent();
	}

	/**
	 * Send a dispose lifecycle event to all registered listeners.
	 */
	protected void sendDisposedLifecycleEvent() {
		ILifecycleService service = getViewContext().getService(
				ILifecycleService.class.getName());
		if (service != null) {
			service.notifyLifecycle(new LifecycleEvent(getEditpart(),
					ILifecycleEvent.TYPE_DISPOSED));
		}
	}

	/**
	 * Send a rendered lifecycle event to all registered listeners.
	 */
	protected void sendRenderedLifecycleEvent() {
		ILifecycleService service = getViewContext().getService(
				ILifecycleService.class.getName());
		if (service != null) {
			service.notifyLifecycle(new LifecycleEvent(getEditpart(),
					ILifecycleEvent.TYPE_RENDERED));
		}
	}

	/**
	 * Send a rendered lifecycle event to all registered listeners.
	 */
	protected void sendUnrenderedLifecycleEvent() {
		ILifecycleService service = getViewContext().getService(
				ILifecycleService.class.getName());
		if (service != null) {
			service.notifyLifecycle(new LifecycleEvent(getEditpart(),
					ILifecycleEvent.TYPE_UNRENDERED));
		}
	}

	/**
	 * For testing purposes.
	 * 
	 * @return
	 */
	public Set<Binding> getUIBindings() {
		return bindings;
	}

}