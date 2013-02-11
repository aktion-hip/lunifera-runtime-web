/**
 * Copyright (c) 2012 Committers of lunifera.org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Florian Pirchner - initial API and implementation
 */
package org.lunifera.runtime.web.http.tests.context;

import java.io.IOException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import junit.framework.Assert;

import org.eclipse.equinox.http.servlet.ExtendedHttpService;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletMapping;
import org.junit.Before;
import org.junit.Test;
import org.lunifera.runtime.web.http.Constants;
import org.lunifera.runtime.web.http.HttpApplication;
import org.lunifera.runtime.web.http.IHttpApplication;
import org.lunifera.runtime.web.http.internal.ServletContextHandler;
import org.lunifera.runtime.web.http.tests.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

@SuppressWarnings("restriction")
public class HttpApplicationTest {

	private Activator activator;
	private BundleContext context;
	private InternalHttpApplication application;

	/**
	 * Setup of the test.
	 * 
	 * @throws ConfigurationException
	 */
	@Before
	public void setup() throws ConfigurationException {
		activator = Activator.getInstance();
		context = Activator.context;
		application = new InternalHttpApplication("App1");
		application.setName("Application1");
		application.setContextPath("/test/app1");
	}

	/**
	 * Tests start and stop of application.
	 * 
	 * @throws ConfigurationException
	 */
	@Test
	public void test_start_stop() throws ConfigurationException {
		Assert.assertEquals(0, activator.getHttpServices().size());

		application.start();
		Assert.assertEquals(1, activator.getHttpServices().size());

		application.stop();
		Assert.assertEquals(0, activator.getHttpServices().size());
	}

	/**
	 * Tests if a http service is available.
	 * 
	 * @throws ConfigurationException
	 * @throws InvalidSyntaxException
	 */
	@Test
	public void test_HttpService_Available() throws ConfigurationException,
			InvalidSyntaxException {

		ServiceRegistration<IHttpApplication> registration = context
				.registerService(IHttpApplication.class, application,
						prepareDefaultProps());
		try {
			Collection<ServiceReference<HttpService>> refs = context
					.getServiceReferences(HttpService.class,
							"(lunifera.http.id=App1)");
			if (refs.size() != 0) {
				Assert.fail("Instance must not be available yet!");
			}

			application.start();

			refs = context.getServiceReferences(HttpService.class,
					"(lunifera.http.id=App1)");
			if (refs.size() != 1) {
				Assert.fail("Instance not found!");
			}

			application.stop();

			refs = context.getServiceReferences(HttpService.class,
					"(lunifera.http.id=App1)");

			if (refs.size() != 0) {
				Assert.fail("Instance must not be available yet!");
			}

		} finally {
			registration.unregister();
		}
	}

	/**
	 * Filters the http service by its id.
	 * 
	 * @throws ConfigurationException
	 * @throws InvalidSyntaxException
	 */
	@Test
	public void test_FilterHttpServiceById() throws ConfigurationException,
			InvalidSyntaxException {
		try {
			application.start();
			Collection<ServiceReference<HttpService>> refs = context
					.getServiceReferences(HttpService.class,
							"(lunifera.http.id=App1)");
			if (refs.size() != 1) {
				Assert.fail("Instance not found!");
			}
		} finally {
			application.stop();
		}
	}

	@Test
	public void test_FilterHttpServiceByName() throws ConfigurationException,
			InvalidSyntaxException {
		try {
			application.start();
			Collection<ServiceReference<HttpService>> refs = context
					.getServiceReferences(HttpService.class,
							"(lunifera.http.name=Application1)");
			if (refs.size() != 1) {
				Assert.fail("Instance not found!");
			}
		} finally {
			application.stop();
		}
	}

	/**
	 * Filters the http service by its context path.
	 * 
	 * @throws ConfigurationException
	 * @throws InvalidSyntaxException
	 */
	@Test
	public void test_FilterHttpServiceByContextPath()
			throws ConfigurationException, InvalidSyntaxException {

		try {
			application.start();
			Collection<ServiceReference<HttpService>> refs = context
					.getServiceReferences(HttpService.class,
							"(lunifera.http.contextPath=/test/app1)");
			if (refs.size() != 1) {
				Assert.fail("Instance not found!");
			}
		} finally {
			application.stop();
		}
	}

	/**
	 * Tests the registration of servlets.
	 * 
	 * @throws ConfigurationException
	 * @throws ServletException
	 * @throws NamespaceException
	 */
	@Test
	public void test_registerServlet() throws ConfigurationException,
			ServletException, NamespaceException {

		try {
			application.start();

			ServletContextHandler contexthandler = application
					.getServletContext();
			ServletMapping[] mappings = contexthandler.getServletHandler()
					.getServletMappings();
			Assert.assertNull(mappings);

			HttpService service = activator.getHttpServices().get(0);

			InternalServlet servlet = new InternalServlet();
			service.registerServlet("/test", servlet, null, null);

			mappings = contexthandler.getServletHandler().getServletMappings();
			Assert.assertEquals(1, mappings.length);
			Assert.assertEquals("/test", mappings[0].getPathSpecs()[0]);

			service.unregister("/test");
			mappings = contexthandler.getServletHandler().getServletMappings();
			Assert.assertEquals(0, mappings.length);
		} finally {
			application.stop();
		}

	}

	/**
	 * Tests what happens if servlet are registered twice.
	 * 
	 * @throws ConfigurationException
	 * @throws ServletException
	 * @throws NamespaceException
	 */
	@Test
	public void test_registerServlet_twice() throws ConfigurationException,
			ServletException, NamespaceException {
		try {
			application.start();

			HttpService service = activator.getHttpServices().get(0);
			InternalServlet servlet = new InternalServlet();
			service.registerServlet("/test", servlet, null, null);
			try {
				service.registerServlet("/test", servlet, null, null);
				Assert.fail();
			} catch (NamespaceException e) {
				// expected
			}

		} finally {
			application.stop();
		}

	}

	/**
	 * Tests what happens if servlets are unregistered twice.
	 * 
	 * @throws ConfigurationException
	 * @throws ServletException
	 * @throws NamespaceException
	 */
	@Test
	public void test_unregisterServlet_twice() throws ConfigurationException,
			ServletException, NamespaceException {
		try {
			application.start();

			HttpService service = activator.getHttpServices().get(0);
			InternalServlet servlet = new InternalServlet();
			service.registerServlet("/test", servlet, null, null);
			try {
				service.unregister("/test");
				service.unregister("/test");
				Assert.fail();
			} catch (IllegalArgumentException e) {
				// expected
				Assert.assertEquals("Alias /test was not registered",
						e.getMessage());
			}

		} finally {
			application.stop();
		}
	}

	/**
	 * Test the registration of filters.
	 * 
	 * @throws ConfigurationException
	 * @throws ServletException
	 * @throws NamespaceException
	 */
	@Test
	public void test_registerFilter() throws ConfigurationException,
			ServletException, NamespaceException {
		try {
			application.start();
			ServletContextHandler contexthandler = application
					.getServletContext();
			FilterMapping[] mappings = contexthandler.getServletHandler()
					.getFilterMappings();
			Assert.assertNull(mappings);

			ExtendedHttpService service = activator.getHttpServices().get(0);

			InternalFilter filter = new InternalFilter();
			service.registerFilter("/test", filter, null, null);

			mappings = contexthandler.getServletHandler().getFilterMappings();
			Assert.assertEquals(1, mappings.length);
			Assert.assertEquals("/test", mappings[0].getPathSpecs()[0]);

			service.unregisterFilter(filter);
			mappings = contexthandler.getServletHandler().getFilterMappings();
			Assert.assertEquals(0, mappings.length);
		} finally {
			application.stop();
		}
	}

	/**
	 * Tests what happens if a filter becomes registered twice.
	 * 
	 * @throws ConfigurationException
	 * @throws ServletException
	 * @throws NamespaceException
	 */
	@Test
	public void test_registerFilter_twice() throws ConfigurationException,
			ServletException, NamespaceException {

		try {
			application.start();

			ExtendedHttpService service = activator.getHttpServices().get(0);
			InternalFilter filter = new InternalFilter();
			service.registerFilter("/test", filter, null, null);
			service.registerFilter("/test", filter, null, null);

		} finally {
			application.stop();
		}
	}

	/**
	 * Tests what happens if a filter becomes unregistered twice.
	 * 
	 * @throws ConfigurationException
	 * @throws ServletException
	 * @throws NamespaceException
	 */
	@Test
	public void test_unregisterFilter_twice() throws ConfigurationException,
			ServletException, NamespaceException {
		try {
			application.start();

			ExtendedHttpService service = activator.getHttpServices().get(0);
			InternalFilter filter = new InternalFilter();
			InternalServlet servlet = new InternalServlet();
			service.registerServlet("/test", servlet, null, null);
			service.registerFilter("/test", filter, null, null);
			try {
				service.unregisterFilter(filter);
				service.unregisterFilter(filter);
				Assert.fail();
			} catch (IllegalArgumentException e) {
				// expected
				Assert.assertEquals("Alias /test was not registered",
						e.getMessage());
			}
		} finally {
			application.stop();
		}
	}

	/**
	 * Creates default properties for the tests.
	 * 
	 * @return
	 */
	public Dictionary<String, Object> prepareDefaultProps() {
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put(Constants.OSGI__APPLICATION_NAME, "Application1");
		props.put(Constants.OSGI__APPLICATION_CONTEXT_PATH, "/test/app1");
		return props;
	}

	/**
	 * An internal helper application.
	 */
	public static class InternalHttpApplication extends HttpApplication {

		public InternalHttpApplication() {
			this("App1");
		}

		public InternalHttpApplication(String id) {
			super(id, Activator.context);
		}

	}

	/**
	 * An internal helper servlet.
	 */
	@SuppressWarnings("serial")
	private static class InternalServlet extends HttpServlet {

		@SuppressWarnings("unused")
		private boolean initCalled;

		@Override
		public void init() throws ServletException {
			initCalled = true;
			super.init();
		}
	}

	/**
	 * An internal helper filter.
	 */
	private static class InternalFilter implements Filter {

		@Override
		public void init(FilterConfig filterConfig) throws ServletException {

		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response,
				FilterChain chain) throws IOException, ServletException {

		}

		@Override
		public void destroy() {

		}
	}
}
