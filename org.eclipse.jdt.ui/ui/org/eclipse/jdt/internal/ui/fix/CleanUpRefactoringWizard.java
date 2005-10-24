/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.fix;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.internal.corext.fix.CleanUpRefactoring;

import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementSorter;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;


public class CleanUpRefactoringWizard extends RefactoringWizard {

	private final boolean fShowCUPage;
	private final boolean fShowCleanUpPage;

	private class SelectCUPage extends UserInputWizardPage {

		private ContainerCheckedTreeViewer fTreeViewer;

		public SelectCUPage(String name) {
			super(name);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
		 */
		public void createControl(Composite parent) {
			Composite composite= new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout());
			
			createViewer(composite);
			setControl(composite);
			
			Dialog.applyDialogFont(composite);
		}
		
		private TreeViewer createViewer(Composite parent) {
			fTreeViewer= new ContainerCheckedTreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			GridData gd= new GridData(GridData.FILL_BOTH);
			gd.widthHint= convertWidthInCharsToPixels(40);
			gd.heightHint= convertHeightInCharsToPixels(15);
			fTreeViewer.getTree().setLayoutData(gd);
			fTreeViewer.setLabelProvider(new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_SMALL_ICONS));
			fTreeViewer.setContentProvider(new StandardJavaElementContentProvider());
			fTreeViewer.setSorter(new JavaElementSorter());
			fTreeViewer.addFilter(new ViewerFilter() {

				public boolean select(Viewer viewer, Object parentElement, Object element) {
					if (element instanceof IJavaElement) {
						IJavaElement jElement= (IJavaElement)element;
						return !jElement.isReadOnly();
					} else {
						return false;
					}
				}
				
			});
			IJavaModel create= JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
			fTreeViewer.setInput(create);
			checkElements(fTreeViewer, (CleanUpRefactoring)getRefactoring());
			return fTreeViewer;
		}
		
		private void checkElements(CheckboxTreeViewer treeViewer, CleanUpRefactoring refactoring) {
			ICompilationUnit[] compilationUnits= refactoring.getCompilationUnits();
			for (int i= 0; i < compilationUnits.length; i++) {
				ICompilationUnit compilationUnit= compilationUnits[i];
				treeViewer.expandToLevel(compilationUnit, 0);
				treeViewer.setChecked(compilationUnit, true);
			}
		}

		protected boolean performFinish() {
			initializeRefactoring();
			return super.performFinish();
		}
	
		public IWizardPage getNextPage() {
			initializeRefactoring();
			return super.getNextPage();
		}

		private void initializeRefactoring() {
			CleanUpRefactoring refactoring= (CleanUpRefactoring)getRefactoring();
			refactoring.clearCompilationUnits();
			Object[] checkedElements= fTreeViewer.getCheckedElements();
			for (int i= 0; i < checkedElements.length; i++) {
				if (checkedElements[i] instanceof ICompilationUnit)
					refactoring.addCompilationUnit((ICompilationUnit)checkedElements[i]);
			}
		}

	}
	
	private class SelectSolverPage extends UserInputWizardPage {

		private static final String SERIAL_VERSION_IDS_SECTION_DESCRIPTION= "Serial version ids";

		private static final String CLEAN_UP_WIZARD_SETTINGS_SECTION_ID= "CleanUpWizard"; //$NON-NLS-1$
		
		private static final String CODE_STYLE_SECTION_DESCRIPTION= "Code style";
		private static final String J2SE_5_0_SECTION_DESCRIPTION= "J2SE 5.0";
		private static final String UNUSED_CODE_SECTION_DESCRIPTION= "Unused code";
		private static final String STRING_EXTERNALIZATION_SECTION_DESCRIPTION= "String externalization";
		
		private class NameFixTuple {

			private final IMultiFix fFix;
			private final String fName;

			public NameFixTuple(String name, IMultiFix fix) {
				fName= name;
				fFix= fix;
			}

			public IMultiFix getFix() {
				return fFix;
			}

			public String getName() {
				return fName;
			}
			
		}
		
		private NameFixTuple[] fMultiFixes;

		public SelectSolverPage(String name) {
			super(name);
		}
		
		public void createControl(Composite parent) {
			ScrolledComposite scrolled= new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
			scrolled.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			scrolled.setLayout(new GridLayout(1, false));
			scrolled.setExpandHorizontal(true);
			scrolled.setExpandVertical(true);

			
			Composite composite= new Composite(scrolled, SWT.NONE);
			composite.setLayout(new GridLayout(1, false));
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			createGroups(composite);
			scrolled.setContent(composite);
			
			scrolled.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	
			setControl(scrolled);
			
			Dialog.applyDialogFont(scrolled);
		}
		
		private void createGroups(Composite parent) {
			NameFixTuple[] multiFixes= getMultiFixes();
			for (int i= 0; i < multiFixes.length; i++) {
				NameFixTuple tuple= multiFixes[i];
				
				Group group= new Group(parent, SWT.NONE);
				group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
				group.setLayout(new GridLayout(1, true));
				group.setText(tuple.getName());
				
				tuple.getFix().createConfigurationControl(group);
			}
		}
		
		private NameFixTuple[] getMultiFixes() {
			if (fMultiFixes == null) {
				IDialogSettings settings= CleanUpRefactoringWizard.this.getDialogSettings();
				IDialogSettings section= settings.getSection(CLEAN_UP_WIZARD_SETTINGS_SECTION_ID);
				fMultiFixes= new NameFixTuple[4];
				if (section == null) {
					settings.addNewSection(CLEAN_UP_WIZARD_SETTINGS_SECTION_ID);
					fMultiFixes[0]= new NameFixTuple(CODE_STYLE_SECTION_DESCRIPTION, new CodeStyleMultiFix(true, true));
					fMultiFixes[1]= new NameFixTuple(UNUSED_CODE_SECTION_DESCRIPTION, new UnusedCodeMultiFix(true, true, true, true, true, true));	
					fMultiFixes[2]= new NameFixTuple(J2SE_5_0_SECTION_DESCRIPTION, new Java50MultiFix(true, true));
					fMultiFixes[3]= new NameFixTuple(STRING_EXTERNALIZATION_SECTION_DESCRIPTION, new StringMultiFix(false, true));
					storeSettings();
				} else {
					fMultiFixes[0]= new NameFixTuple(CODE_STYLE_SECTION_DESCRIPTION, new CodeStyleMultiFix(section));
					fMultiFixes[1]= new NameFixTuple(UNUSED_CODE_SECTION_DESCRIPTION, new UnusedCodeMultiFix(section));
					fMultiFixes[2]= new NameFixTuple(J2SE_5_0_SECTION_DESCRIPTION, new Java50MultiFix(section));
					fMultiFixes[3]= new NameFixTuple(STRING_EXTERNALIZATION_SECTION_DESCRIPTION, new StringMultiFix(section));
				}
			}
			return fMultiFixes;
		}
		
		protected boolean performFinish() {
			initializeRefactoring();
			storeSettings();
			return super.performFinish();
		}
	
		public IWizardPage getNextPage() {
			initializeRefactoring();
			storeSettings();
			return super.getNextPage();
		}
		
		private void storeSettings() {
			IDialogSettings settings= CleanUpRefactoringWizard.this.getDialogSettings().getSection(CLEAN_UP_WIZARD_SETTINGS_SECTION_ID);
			NameFixTuple[] fixes= getMultiFixes();
			for (int i= 0; i < fixes.length; i++) {
				fixes[i].getFix().saveSettings(settings);
			}
		}

		private void initializeRefactoring() {
			CleanUpRefactoring refactoring= (CleanUpRefactoring)getRefactoring();
			refactoring.clearProblemSolutions();
			NameFixTuple[] multiFixes= getMultiFixes();
			for (int i= 0; i < multiFixes.length; i++) {
				refactoring.addProblemSolution(multiFixes[i].getFix());
			}
		}	
	}
	
	public CleanUpRefactoringWizard(CleanUpRefactoring refactoring, int flags, boolean showCUPage, boolean showCleanUpPage) {
		super(refactoring, flags);
		fShowCUPage= showCUPage;
		fShowCleanUpPage= showCleanUpPage;
		setDefaultPageTitle("Clean up wizard");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.ui.refactoring.RefactoringWizard#addUserInputPages()
	 */
	protected void addUserInputPages() {
		if (fShowCUPage) {
			SelectCUPage selectCUPage= new SelectCUPage("Select Compilation units Page");
			selectCUPage.setMessage("Select compilation units to clean up.");
			addPage(selectCUPage);
		}
		
		if (fShowCleanUpPage){
			SelectSolverPage selectSolverPage= new SelectSolverPage("Select clean ups Page");
			selectSolverPage.setMessage("Select clean ups and set there options to applay to the selected compilation units.");
			addPage(selectSolverPage);
		}
	}

}
