package cachecatest;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.python.pydev.editor.PyEdit;
import org.python.pydev.editor.codecompletion.revisited.javaintegration.AbstractWorkbenchTestCase;
import org.python.pydev.editorinput.PyOpenEditor;
import org.python.pydev.plugin.nature.PythonNature;

public class PydevTopRank {
    private static PyEdit editor;

    /**
     * Check many code-completion cases with the java integration.
     */
    public void testJavaClassModule() throws Throwable {
        try {

            checkCase2();

            //            goToManual();
        } catch (Throwable e) {
            //ok, I like errors to appear in stderr (and not only in the unit-test view)
            e.printStackTrace();
            throw e;
        }
    }

    protected IFile createIFile(IContainer sourceFolder, IProgressMonitor monitor)
            throws CoreException {
        IFile lastFile = null;
        if (sourceFolder == null) {
            return null;
        }
        IContainer parent = sourceFolder;

        IFolder folder = parent.getFolder(new Path("src/files"));
        if (!folder.exists()) {
            folder.create(true, true, monitor);
        }
        parent = folder;
        IFile file = parent.getFile(new Path("src/files/22.code.python.tokens"));
        if (!file.exists()) {
            file.create(new ByteArrayInputStream(new byte[0]), true, monitor);

            lastFile = file;
        }

        return lastFile;
    }

    protected void setFileContents(String contents) throws CoreException {
        NullProgressMonitor monitor = new NullProgressMonitor();
        // IWorkspace workspace = ResourcesPlugin.getWorkspace();
        File file = new File("22.code.python.tokens");
        // IPath location = Path.fromOSString(file.getAbsolutePath());
        // IFile ifile = workspace.getRoot().getFileForLocation(location);
        //IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("com.python.pydev.codecompletion");
        /*IFolder sourceFolder = ifile.getParent().getFolder(location);
        // IFile mod1 = createIFile(sourceFolder, monitor);
        if (!ifile.exists()) {
            ifile.create(new ByteArrayInputStream(contents.getBytes()), true, monitor);
        } else {
            ifile.setContents(new ByteArrayInputStream(contents.getBytes()), 0, monitor);
            ifile.refreshLocal(IResource.DEPTH_INFINITE, monitor);
        }*/

        /* new*/
        IProject project = AbstractWorkbenchTestCase.createProject(monitor, "pydev_testing");

        AbstractWorkbenchTestCase.setProjectReference(monitor, project, null);

        IFile initFile, mod1;

        IFolder sourceFolder = AbstractWorkbenchTestCase.createSourceFolder(monitor, project);

        initFile = AbstractWorkbenchTestCase.createPackageStructure(sourceFolder, "pack1.pack2", monitor);

        mod1 = initFile.getParent().getFile(new Path("mod1.py"));

        //OK, structure created, now, let's open mod1.py with a PyEdit so that the tests can begin...

        //create the contents and open the editor
        mod1.create(new ByteArrayInputStream(contents.getBytes()), true, monitor);

        PythonNature nature = PythonNature.getPythonNature(project);

        AbstractWorkbenchTestCase.waitForNatureToBeRecreated(nature);
        setEditor((PyEdit) PyOpenEditor.doOpenEditor(mod1));
    }

    public void checkCase2() throws CoreException {
        String mod1Contents = "";
        // File mod1 = new File ("src/22.code.python.tokens");
        setFileContents(mod1Contents);
        //ICompletionProposal[] proposals = this.requestProposals(mod1Contents, getEditor());

        //get prefix
        StringBuilder b = new StringBuilder();
        try {
            int numberOfSeparators = 0;
            int offset = mod1Contents.length();
            //offset -= replacementLength;
            char c = getEditor().getPySourceViewer().getDocument().getChar(offset);
            boolean prevCharWasWhitespace = false;
            while (numberOfSeparators < 10 && offset >= 0) {
                if (!(Character.isWhitespace(getEditor().getPySourceViewer().getDocument().getChar(offset)))) {
                    prevCharWasWhitespace = false;
                    b.append(c);
                }
                else {
                    if (prevCharWasWhitespace == false) {
                        b.append(" ");
                    }
                    prevCharWasWhitespace = true;
                }
                if (c == ' ' || c == '.' || c == '(' || c == ')' || c == '{' || c == '}' || c == ';' || c == '['
                        || c == ']' || c == '\n') {
                    numberOfSeparators++;
                }
                offset--;
                if (offset < 0) {
                    break;
                }
                c = getEditor().getPySourceViewer().getDocument().getChar(offset);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        String pref = b.reverse().toString();

        //get suffix
        StringBuilder suffix = new StringBuilder();
        try {
            int offset = mod1Contents.length() + 1;
            char c = getEditor().getPySourceViewer().getDocument().getChar(offset);
            while (c != '=' && c != '.' && c != '(' && c != ')' && c != '{' && c != '}' && c != ';' && c != '['
                    && c != ']' && c != '\n') {
                offset++;
                suffix.append(c);
                c = getEditor().getPySourceViewer().getDocument().getChar(offset);
            }
        } catch (BadLocationException e) {

        }
        String suf = suffix.toString();

        /**********************************************/

        String fileName = "/Users/siddhikacowlagi/Desktop/pydev_test" + "0" + "-output"; //$FILEOUTPUT
        PrintWriter w = null;
        try {
            w = new PrintWriter(new BufferedWriter(new FileWriter(fileName + ".txt", true)));
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        w.println("<bead>");

        w.print("<prefix>");
        w.print(pref);
        w.println("</prefix>");

        w.print("<reference>");
        w.print(suf);
        w.println("</reference>");
        // CodeCompletionTestsBase.assertContains("JavaClass - javamod1", proposals);
        // CodeCompletionTestsBase.assertContains("JavaClass2 - javamod1.javamod2", proposals);
    }

    public static PyEdit getEditor() {
        return editor;
    }

    public static void setEditor(PyEdit editor) {
        PydevTopRank.editor = editor;
    }

    public static void main(String args[])
    {
        PydevTopRank py = new PydevTopRank();
        try {
            py.testJavaClassModule();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
