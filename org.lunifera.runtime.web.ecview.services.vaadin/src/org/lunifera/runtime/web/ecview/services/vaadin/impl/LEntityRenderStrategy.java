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

package org.lunifera.runtime.web.ecview.services.vaadin.impl;

import java.util.Map;

import org.eclipse.emf.ecp.ecview.common.context.IViewContext;
import org.eclipse.emf.ecp.ecview.common.model.core.YView;
import org.eclipse.emf.ecp.ecview.extension.model.extension.YGridLayout;
import org.eclipse.emf.ecp.ecview.extension.model.extension.util.SimpleExtensionModelFactory;
import org.lunifera.dsl.semantic.entity.LEntity;
import org.lunifera.runtime.web.ecview.services.vaadin.ILEntityRenderStrategy;
import org.osgi.service.component.annotations.Component;

@Component(property = { "service.ranking=100" })
public class LEntityRenderStrategy implements ILEntityRenderStrategy {

	private SimpleExtensionModelFactory factory = new SimpleExtensionModelFactory();

	@Override
	public YView render(LEntity lEntity, Class<?> entityClass,
			Object entityInstance, Map<String, Object> beanSlots) {

		YView yView = factory.createView();
		// create a bean slot for the bindings
		yView.addBeanSlot(IViewContext.ROOTBEAN_SELECTOR, entityClass);

		// create a main layout
		YGridLayout yLayout = factory.createGridLayout();
		yLayout.setColumns(4);
		yLayout.setFillHorizontal(true);
		yView.setContent(yLayout);

		// Use the entity renderer to build the UI model
		EntityModelRenderer renderer = new EntityModelRenderer();
		renderer.render(lEntity, yLayout, entityClass);

		return yView;
	}
}
