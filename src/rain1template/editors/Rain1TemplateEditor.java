package rain1template.editors;


import java.io.StringWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osgi.framework.debug.Debug;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.MultiPageEditorSite;
import org.eclipse.ui.ide.IDE;
import org.eclipse.wst.*;
import org.eclipse.wst.html.core.internal.provisional.contenttype.ContentTypeFamilyForHTML;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.core.internal.provisional.contenttype.ContentTypeIdForXML;
import org.eclipse.php.internal.core.documentModel.provisional.contenttype.ContentTypeIdForPHP;
import org.eclipse.php.internal.core.project.PHPNature;
import org.eclipse.php.internal.ui.folding.StructuredTextFoldingProviderPHP;

/**
 * An example showing how to create a multi-page editor.
 * This example has 3 pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
public class Rain1TemplateEditor extends MultiPageEditorPart implements IResourceChangeListener{
	private boolean firstTime = true;
	/** The text editor used in page 0. */
	private StructuredTextEditor editor;

	/** The text editor used in page 1. */
	private StructuredTextEditor templateEditor;
	
	/** The font chosen in pagex. */
	private Font font;

	/** The text widget used in page x. */
	private StyledText text;
	/**
	 * Creates a multi-page editor example.
	 */
	public Rain1TemplateEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	/**
	 * Creates page 0 of the multi-page editor,
	 * which contains a text editor.
	 */
	void createPage0() {
		try {
			editor = new StructuredTextEditor();
			int index = addPage(editor, getEditorInput());
			setPageText(index, editor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(
				getSite().getShell(),
				"Error creating nested text editor",
				null,
				e.getStatus());
		}
	}
	/**
	 * Creates page 1 of the multi-page editor,
	 * which allows you to change the font used in page 2.
	 */
	void createPage1_old() {

		Composite composite = new Composite(getContainer(), SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		layout.numColumns = 2;

		Button fontButton = new Button(composite, SWT.NONE);
		GridData gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 2;
		fontButton.setLayoutData(gd);
		fontButton.setText("Change Font...");
		
		fontButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				setFont();
			}
		});

		int index = addPage(composite);
		setPageText(index, "Properties");
	}
	/**
	 * Creates page 1 of the multi-page editor,
	 * which allows you to change the font used in page 2.
	 */
	void createPage1() {
		try {
			templateEditor = new StructuredTextEditor();
			int index = addPage(templateEditor, new StringEditorInput(""));
			convertSourceToTemplate();
			setPageText(index, "Rain1 Template");
			
		} catch (PartInitException e) {
			ErrorDialog.openError(
				getSite().getShell(),
				"Error creating nested text editor",
				null,
				e.getStatus());
		}
	}
	/**
	 * Creates page 2 of the multi-page editor,
	 * which shows the sorted text.
	 */
	void createPageTemplate() {
		Composite composite = new Composite(getContainer(), SWT.EMBEDDED);
		FillLayout layout = new FillLayout();
		composite.setLayout(layout);
		text = new StyledText(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		text.setEditable(true);

		int index = addPage(composite);
		setPageText(index, "Preview");
	}
	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {
		createPage0();
		createPage1();
		//createPage2();
	}
	/**
	 * The <code>MultiPageEditorPart</code> implementation of this 
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		convertTemplateToSource();
		getEditor(0).doSave(monitor);
		
	}
	/**
	 * Saves the multi-page editor's document as another file.
	 * Also updates the text for page 0's tab, and updates this multi-page editor's input
	 * to correspond to the nested editor's.
	 */
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}
	/* (non-Javadoc)
	 * Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}
	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput)
		throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
		
		// Setta il titolo del tab come il filename!!!!!!
		IFileEditorInput fileInput = (IFileEditorInput) editorInput;
		this.setPartName(fileInput.getName());
	}
	/* (non-Javadoc)
	 * Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}
	/**
	 * Calculates the contents of page 2 when the it is activated.
	 */
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if (newPageIndex == 1) {
			convertSourceToTemplate();
		}else if(newPageIndex == 0) {
			if(!firstTime) {
				convertTemplateToSource();
			}else {
				firstTime = false;
			}
		}
	}
	/**
	 * @see org.eclipse.ui.part.MultiPageEditorPart#createSite(org.eclipse.ui.IEditorPart)
	 */
	protected IEditorSite createSite(IEditorPart page) {
		IEditorSite site = null;
		if (page == editor) {
			site = new MultiPageEditorSite(this, page) {
				public String getId() {
					// Sets this ID so nested editor is configured for PHP source (Non funziona!!!)
					return org.eclipse.php.internal.core.documentModel.provisional.contenttype.ContentTypeIdForPHP.ContentTypeID_PHP;
				}
			};
		}
		else if(page == templateEditor) {
			site = new MultiPageEditorSite(this, page) {
				public String getId() {
					// Sets this ID so nested editor is configured for HTML source
					
					return ContentTypeFamilyForHTML.HTML_FAMILY + ".source"; //$NON-NLS-1$;

				}
			};
		}
		else {
			site = super.createSite(page);
		}
		return site;
		
	}
	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event){
		if(event.getType() == IResourceChangeEvent.PRE_CLOSE){
			Display.getDefault().asyncExec(new Runnable(){
				public void run(){
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i<pages.length; i++){
						if(((FileEditorInput)editor.getEditorInput()).getFile().getProject().equals(event.getResource())){
							IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
							pages[i].closeEditor(editorPart,true);
						}
					}
				}            
			});
		}
	}
	/**
	 * Sets the font related data to be applied to the text in page 2.
	 */
	void setFont() {
		FontDialog fontDialog = new FontDialog(getSite().getShell());
		fontDialog.setFontList(text.getFont().getFontData());
		FontData fontData = fontDialog.open();
		if (fontData != null) {
			if (font != null)
				font.dispose();
			font = new Font(text.getDisplay(), fontData);
			text.setFont(font);
		}
	}

	void convertSourceToTemplate() {
		String editorText =
				editor.getDocumentProvider().getDocument(editor.getEditorInput()).get();
		
//		text.setText(_decompile_code(editorText));
		templateEditor.getDocumentProvider().getDocument(templateEditor.getEditorInput()).set(_decompile_code(editorText));
	}
	void convertTemplateToSource() {
		String templateText =
				templateEditor.getDocumentProvider().getDocument(templateEditor.getEditorInput()).get();
//				text.getText();
		editor.getDocumentProvider().getDocument(editor.getEditorInput()).set(_compile_code(templateText));
	}
	String _compile_code(String tcode) {

			tcode = tcode.replaceAll("(\\\\})","|||R1T}|||").

		replaceAll(	"(\\\\\\{)"									,"|||R1T{|||"					).

		replaceAll(	"\\{(foreach|if) (.*?)\\}"			,"<"+"?php $1($2) : ?"+">"		).

		replaceAll(	"\\{\\/(foreach|if)\\}"				,"<"+"?php end$1; ?"+">"		).

		replaceAll(	"\\{f:(.*?)([^\\\\]{1})\\}"			,"<"+"?php echo $1$2; ?"+">"	).

		replaceAll(	"\\{(\\$.*?)\\}"				,"<"+"?php echo $1; ?"+">"		).

		replaceAll(	"\\{\\*(.*?)\\*\\}"						,"<"+"?php /*$1*/ ?"+">"		).

		

		replaceAll("\\|\\|\\|R1T(\\{|\\})\\|\\|\\|","$1");



		return tcode;

	}

	
	//<?php foreach($array as $k=>$v) : ?>
	 String _decompile_code(String tcode) {

		tcode = tcode.replaceAll(	"<\\?php (foreach|if)\\((.*?)\\) : \\?>"		,"{$1 $2}"			).

		replaceAll(	"<\\?php end(foreach|if); \\?>"			,"{/$1}"			).

		replaceAll(	"<\\?php \\/\\*(.*?)\\*\\/ \\?>"				,"{*$1*}"			).

		replaceAll(	"<\\?php echo ([^\\$]{1}.*?); \\?>"			,"{f:$1}"			).

		

		replaceAll(	"<\\?php echo (\\$)(.*?); \\?>"			,"{$1$2}"			);

		return tcode;

	}
	 
	 

}
