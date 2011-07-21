/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.osgi.tests.resource;

import java.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.resolver.ResolverHook;
import org.osgi.framework.hooks.resolver.ResolverHookFactory;
import org.osgi.framework.wiring.*;

public class ResolverHookTests extends AbstractResourceTest {

	public static Test suite() {
		return new TestSuite(ResolverHookTests.class);
	}

	public ResolverHookTests(String name) {
		super(name);
	}

	public void testSingletonIdentity() throws Exception {
		final RuntimeException error[] = {null};
		final boolean called[] = {false};
		ResolverHookFactory resolverHookFactory = new ResolverHookFactory() {
			public ResolverHook begin(Collection triggers) {
				return new ResolverHook() {

					public void filterSingletonCollisions(BundleCapability singleton, Collection collisionCandidates) {
						if (error[0] != null)
							return;
						called[0] = true;
						try {
							assertEquals("Wrong namespace", ResourceConstants.IDENTITY_NAMESPACE, singleton.getNamespace());
							assertEquals("Wrong singleton directive", "true", singleton.getDirectives().get(ResourceConstants.IDENTITY_SINGLETON_DIRECTIVE));
							String symbolicName = (String) singleton.getAttributes().get(ResourceConstants.IDENTITY_NAMESPACE);
							for (Iterator iCandidates = collisionCandidates.iterator(); iCandidates.hasNext();) {
								BundleCapability candidate = (BundleCapability) iCandidates.next();
								assertEquals("Wrong namespace", ResourceConstants.IDENTITY_NAMESPACE, candidate.getNamespace());
								assertEquals("Wrong singleton directive", "true", candidate.getDirectives().get(ResourceConstants.IDENTITY_SINGLETON_DIRECTIVE));
								assertEquals("Wrong symbolic name", symbolicName, (String) candidate.getAttributes().get(ResourceConstants.IDENTITY_NAMESPACE));
							}
						} catch (RuntimeException e) {
							error[0] = e;
						}
					}

					public void filterResolvable(Collection candidates) {
						// nothing
					}

					public void filterMatches(BundleRequirement requirement, Collection candidates) {
						// nothing
					}

					public void end() {
						// nothing
					}
				};
			}
		};

		ServiceRegistration hookReg = getContext().registerService(ResolverHookFactory.class, resolverHookFactory, null);

		try {
			Bundle tb1v1 = installer.installBundle("singleton.tb1v1");
			Bundle tb1v2 = installer.installBundle("singleton.tb1v2");
			assertFalse(((FrameworkWiring) getContext().getBundle(0).adapt(FrameworkWiring.class)).resolveBundles(Arrays.asList(new Bundle[] {tb1v1, tb1v2})));
			assertTrue("ResolverHook was not called", called[0]);
			if (error[0] != null)
				throw error[0];
		} finally {
			hookReg.unregister();
		}
	}

}