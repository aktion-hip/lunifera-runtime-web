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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.emf.databinding.EMFProperties;
import org.eclipse.emf.ecp.ecview.common.context.II18nService;
import org.eclipse.emf.ecp.ecview.common.editpart.IElementEditpart;
import org.eclipse.emf.ecp.ecview.common.model.core.YEmbeddableBindingEndpoint;
import org.eclipse.emf.ecp.ecview.common.model.core.YEmbeddableCollectionEndpoint;
import org.eclipse.emf.ecp.ecview.common.model.core.YEmbeddableMultiSelectionEndpoint;
import org.eclipse.emf.ecp.ecview.common.model.core.YEmbeddableSelectionEndpoint;
import org.eclipse.emf.ecp.ecview.databinding.emf.model.ECViewModelBindable;
import org.eclipse.emf.ecp.ecview.extension.model.extension.ExtensionModelPackage;
import org.eclipse.emf.ecp.ecview.extension.model.extension.YColumn;
import org.eclipse.emf.ecp.ecview.extension.model.extension.YFlatAlignment;
import org.eclipse.emf.ecp.ecview.extension.model.extension.YSelectionType;
import org.eclipse.emf.ecp.ecview.extension.model.extension.YTable;
import org.eclipse.emf.ecp.ecview.ui.core.editparts.extension.ITableEditpart;
import org.eclipse.emf.ecp.ecview.ui.core.editparts.extension.presentation.ITabPresentation;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;

/**
 * This presenter is responsible to render a table on the given layout.
 */
public class TablePresentation extends AbstractFieldWidgetPresenter<Component>
		implements ITabPresentation<Component> {

	private final ModelAccess modelAccess;
	private CssLayout componentBase;
	private Table table;
	@SuppressWarnings("rawtypes")
	private ObjectProperty property;

	/**
	 * Constructor.
	 * 
	 * @param editpart
	 *            The editpart of that presenter
	 */
	public TablePresentation(IElementEditpart editpart) {
		super((ITableEditpart) editpart);
		this.modelAccess = new ModelAccess((YTable) editpart.getModel());
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
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
			
			associateWidget(componentBase, modelAccess.yTable);
			
			table = new Table();
			table.addStyleName(CSS_CLASS_CONTROL);
			table.setMultiSelect(modelAccess.yTable.getSelectionType() == YSelectionType.MULTI);
			table.setSelectable(true);
			table.setImmediate(true);
			
			associateWidget(table, modelAccess.yTable);

			if (table.isMultiSelect()) {
				property = new ObjectProperty(new HashSet(), Set.class);
			} else {
				if (modelAccess.yTable.getType() != null) {
					property = new ObjectProperty(null,
							modelAccess.yTable.getType());
				} else {
					property = new ObjectProperty(null, Object.class);
				}
			}
			table.setPropertyDataSource(property);

			if (modelAccess.yTable.getType() == String.class) {
				IndexedContainer datasource = new IndexedContainer();
				table.setContainerDataSource(datasource);
				table.setItemCaptionMode(ItemCaptionMode.ID);
			} else {
				if (modelAccess.yTable.getType() != null) {
					BeanItemContainer datasource = null;
					datasource = new BeanItemContainer(
							modelAccess.yTable.getType());
					table.setContainerDataSource(datasource);

					// applies the column properties
					applyColumns();

				} else {
					IndexedContainer container = new IndexedContainer();
					container.addContainerProperty("for", String.class, null);
					container.addContainerProperty("preview", String.class,
							null);
					container.addItem(new String[] { "Some value", "other" });
					table.setContainerDataSource(container);
				}
			}

			// creates the binding for the field
			createBindings(modelAccess.yTable, table);

			componentBase.addComponent(table);

			if (modelAccess.isCssClassValid()) {
				table.addStyleName(modelAccess.getCssClass());
			}

			applyCaptions();

			initializeField(table);
		}
		return componentBase;
	}

	/**
	 * Applies the column setting to the table.
	 */
	protected void applyColumns() {
		// set the visible columns and icons
		List<String> columns = new ArrayList<String>();
		Collection<?> propertyIds = table.getContainerDataSource()
				.getContainerPropertyIds();

		for (YColumn yColumn : modelAccess.yTable.getColumns()) {
			if (yColumn.isVisible() && propertyIds.contains(yColumn.getName())) {
				columns.add(yColumn.getName());
			}
		}

		table.setVisibleColumns(columns.toArray(new Object[columns.size()]));
		table.setColumnCollapsingAllowed(true);

		// traverse the columns again and set other properties
		for (YColumn yColumn : modelAccess.yTable.getColumns()) {
			if (yColumn.isVisible() && propertyIds.contains(yColumn.getName())) {
				table.setColumnAlignment(yColumn.getName(),
						toAlign(yColumn.getAlignment()));
				table.setColumnCollapsed(yColumn.getName(),
						yColumn.isCollapsed());
				table.setColumnCollapsible(yColumn.getName(),
						yColumn.isCollapsible());
				if (yColumn.getExpandRatio() >= 0) {
					table.setColumnExpandRatio(yColumn.getName(),
							yColumn.getExpandRatio());
				}
				if (yColumn.getIcon() != null && !yColumn.getIcon().equals("")) {
					table.setColumnIcon(yColumn.getName(), new ThemeResource(
							yColumn.getIcon()));
				}
			}
		}
	}

	private Align toAlign(YFlatAlignment alignment) {
		switch (alignment) {
		case LEFT:
			return Align.LEFT;
		case CENTER:
			return Align.CENTER;
		case RIGHT:
			return Align.RIGHT;
		}
		return Align.LEFT;
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
		return table;
	}

	@Override
	protected IObservable internalGetObservableEndpoint(
			YEmbeddableBindingEndpoint bindableValue) {
		if (bindableValue == null) {
			throw new IllegalArgumentException(
					"BindableValue must not be null!");
		}

		if (bindableValue instanceof YEmbeddableCollectionEndpoint) {
			return internalGetCollectionEndpoint();
		} else if (bindableValue instanceof YEmbeddableSelectionEndpoint) {
			return internalGetSelectionEndpoint((YEmbeddableSelectionEndpoint) bindableValue);
		} else if (bindableValue instanceof YEmbeddableMultiSelectionEndpoint) {
			return internalGetMultiSelectionEndpoint();
		}
		throw new IllegalArgumentException("Not a valid input: "
				+ bindableValue);
	}

	/**
	 * Returns the observable to observe the collection.
	 * 
	 * @return
	 */
	protected IObservableList internalGetCollectionEndpoint() {
		// return the observable value for text
		return EMFProperties.list(
				ExtensionModelPackage.Literals.YTABLE__COLLECTION).observe(
				getModel());
	}

	/**
	 * Returns the observable to observe the selection.
	 * 
	 * @return
	 */
	@SuppressWarnings("restriction")
	protected IObservableValue internalGetSelectionEndpoint(
			YEmbeddableSelectionEndpoint yEndpoint) {

		String attributePath = ECViewModelBindable.getAttributePath(
				ExtensionModelPackage.Literals.YTABLE__SELECTION,
				yEndpoint.getAttributePath());

		// return the observable value for text
		return ECViewModelBindable.observeValue(castEObject(getModel()),
				attributePath, modelAccess.yTable.getType(),
				modelAccess.yTable.getEmfNsURI());
	}

	/**
	 * Returns the observable to observe the selection.
	 * 
	 * @return
	 */
	protected IObservableList internalGetMultiSelectionEndpoint() {
		// return the observable value for text
		return EMFProperties.list(
				ExtensionModelPackage.Literals.YTABLE__MULTI_SELECTION)
				.observe(getModel());
	}

	/**
	 * Creates the bindings for the given values.
	 * 
	 * @param yField
	 * @param field
	 */
	protected void createBindings(YTable yField, Table field) {
		// create the model binding from ridget to ECView-model
		registerBinding(createBindings_ContainerContents(
				castEObject(getModel()),
				ExtensionModelPackage.Literals.YTABLE__COLLECTION, field,
				yField.getType()));

		// create the model binding from ridget to ECView-model
		if (modelAccess.yTable.getSelectionType() == YSelectionType.MULTI) {
			// create the model binding from ridget to ECView-model
			registerBinding(createBindingsMultiSelection(
					castEObject(getModel()),
					ExtensionModelPackage.Literals.YTABLE__MULTI_SELECTION,
					field, yField.getType()));
		} else {
			// create the model binding from ridget to ECView-model
			registerBinding(createBindingsSelection(castEObject(getModel()),
					ExtensionModelPackage.Literals.YTABLE__SELECTION, field,
					yField.getType()));

		}

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
			unassociateWidget(table);

			componentBase = null;
			table = null;
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
		private final YTable yTable;

		public ModelAccess(YTable yTable) {
			super();
			this.yTable = yTable;
		}

		/**
		 * @return
		 * @see org.eclipse.emf.ecp.ecview.ui.core.model.core.YCssAble#getCssClass()
		 */
		public String getCssClass() {
			return yTable.getCssClass();
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
			return yTable.getCssID();
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
			return yTable.getDatadescription() != null
					&& yTable.getDatadescription().getLabel() != null;
		}

		/**
		 * Returns the label.
		 * 
		 * @return
		 */
		public String getLabel() {
			return yTable.getDatadescription().getLabel();
		}

		/**
		 * Returns true, if the label is valid.
		 * 
		 * @return
		 */
		public boolean isLabelI18nKeyValid() {
			return yTable.getDatadescription() != null
					&& yTable.getDatadescription().getLabelI18nKey() != null;
		}

		/**
		 * Returns the label.
		 * 
		 * @return
		 */
		public String getLabelI18nKey() {
			return yTable.getDatadescription().getLabelI18nKey();
		}
	}
}
