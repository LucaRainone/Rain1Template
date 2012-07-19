package rain1template.editors;

import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;


public class StringEditorInput implements IStorageEditorInput {



    private final String inputString;


    public StringEditorInput(String inputString) {

        this.inputString = inputString;

    }



    public boolean exists() {

        return false;

    }



    public ImageDescriptor getImageDescriptor() {

        return null;

    }



    public IPersistableElement getPersistable() {

        return null;

    }



    public Object getAdapter(Class adapter) {

        return null;

    }



    public String getName() {

        return "input name";

    }



    public String getToolTipText() {

        return "tool tip";

    }



    public IStorage getStorage() throws CoreException {

        return new IStorage() {



            public InputStream getContents() throws CoreException {

                return new StringBufferInputStream(inputString);

            }



            public IPath getFullPath() {

                return null;

            }



            public String getName() {

                return StringEditorInput.this.getName();

            }



            public boolean isReadOnly() {

                return false;

            }



            public Object getAdapter(Class adapter) {

                return null;

            }



        };

    }



}