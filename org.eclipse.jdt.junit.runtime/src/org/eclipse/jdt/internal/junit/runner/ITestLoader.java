/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     David Saff (saff@mit.edu) - initial API and implementation
 *             (bug 102632: [JUnit] Support for JUnit 4.)
 *******************************************************************************/

package org.eclipse.jdt.internal.junit.runner;

public interface ITestLoader {
	/**
	 * @param testClasses classes to be run
	 * @param testName individual method to be run
	 * @param failureNames may want to run these first, since they failed
	 * @param packages packages containing tests to run
	 * @param includeExcludeTags tags to be included and excluded in the test run
	 * @param uniqueId unique ID of the test to run
	 * @param listener to be notified if tests could not be loaded
	 * @return the loaded test references
	 */
	public abstract ITestReference[] loadTests(Class[] testClasses, String testName, String[] failureNames, String[] packages, String[][] includeExcludeTags, String uniqueId, RemoteTestRunner listener);
}

