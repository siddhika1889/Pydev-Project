package cachecatest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.java.JavaAllCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.python.pydev.editor.PyEdit;
import org.python.pydev.editor.codecompletion.revisited.javaintegration.AbstractWorkbenchTestCase;
import org.python.pydev.editor.simpleassist.SimpleAssistProcessor;

@SuppressWarnings("restriction")
public class EclipseIntegration extends JavaAllCompletionProposalComputer {

    public static int version;

    @Override
    public java.util.List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
            IProgressMonitor monitor) {
        java.util.List<ICompletionProposal> defaultProposals = super.computeCompletionProposals(context, monitor);
        JavaContentAssistInvocationContext ctx = (JavaContentAssistInvocationContext) context;

        int masterOffset = ctx.getInvocationOffset() - 1;

        /******************* TEST CODE BLOCK *********************/
        String fileName = "/Users/siddhikacowlagi/Desktop/pydev_test" + version + "-output"; //$FILEOUTPUT
        PrintWriter w = null;
        try {
            w = new PrintWriter(new BufferedWriter(new FileWriter(fileName + ".txt", true)));
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        w.println("<bead>");

        /******************* END TEST CODE BLOCK ****************/

        /******************************************************** TEST CODE BLOCK*/

        CompletionProposalCollector collector = new CompletionProposalCollector(ctx.getCompilationUnit());
        collector.setInvocationContext(ctx);

        try {
            ctx.getCompilationUnit().codeComplete(ctx.getInvocationOffset(), collector, new NullProgressMonitor());
        } catch (JavaModelException e2) {
            e2.printStackTrace();
        }

        //  IJavaCompletionProposal[] jProps = collector.getJavaCompletionProposals();

        /*  Arrays.sort(jProps, new Comparator<IJavaCompletionProposal>() {
              @Override
              public int compare(IJavaCompletionProposal one, IJavaCompletionProposal two) {
                  if (one.getRelevance() < two.getRelevance()) {
                      return 1;
                  } else if (one.getRelevance() > two.getRelevance()) {
                      return -1;
                  } else {
                      return one.getDisplayString().compareTo(two.getDisplayString());
                  }
              }
          });*/

        /*********************************************************/

        //prepare for eclipse

        defaultProposals.clear();
        int proposalFractionFromEclipse = 5;

        /* java.util.List<ICompletionProposal> dp = new ArrayList<ICompletionProposal>();

         int proposalFractionFromEclipse = 5;
         if (jProps.length < proposalFractionFromEclipse) {
             proposalFractionFromEclipse = jProps.length;
         }

         int j = 0;
         while (j < proposalFractionFromEclipse) {
             dp.add(jProps[j]);
             j++;
         }*/

        //custom completions

        //get offset
        int replacementLength = 0;
        try {
            int offset = masterOffset;
            if (offset < 0) {
                return defaultProposals;
            }
            char c = ctx.getDocument().getChar(offset);
            while (c != '=' && c != ' ' && c != '.' && c != '(' && c != ')' && c != '{' && c != '}' && c != ';'
                    && c != '[' && c != ']' && c != '\n') {
                offset--;
                replacementLength++;
                c = ctx.getDocument().getChar(offset);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        //get prefix
        StringBuilder b = new StringBuilder();
        try {
            int numberOfSeparators = 0;
            int offset = masterOffset;
            offset -= replacementLength;
            char c = ctx.getDocument().getChar(offset);
            boolean prevCharWasWhitespace = false;
            while (numberOfSeparators < 10 && offset >= 0) {
                if (!(Character.isWhitespace(ctx.getDocument().getChar(offset)))) {
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
                c = ctx.getDocument().getChar(offset);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        String pref = b.reverse().toString();

        //get suffix
        StringBuilder suffix = new StringBuilder();
        try {
            int offset = masterOffset + 1;
            char c = ctx.getDocument().getChar(offset);
            while (c != '=' && c != '.' && c != '(' && c != ')' && c != '{' && c != '}' && c != ';' && c != '['
                    && c != ']' && c != '\n') {
                offset++;
                suffix.append(c);
                c = ctx.getDocument().getChar(offset);
            }
        } catch (BadLocationException e) {

        }
        String suf = suffix.toString();

        /**********************************************/

        w.print("<prefix>");
        w.print(pref);
        w.println("</prefix>");

        w.print("<reference>");
        w.print(suf);
        w.println("</reference>");

        w.println("<Pydev>");

        String mod1Contents = pref;
        PyEdit editor = AbstractWorkbenchTestCase.getEditor();
        editor.setSelection(mod1Contents.length(), 0);
        IContentAssistant contentAssistant = editor.getEditConfiguration().getContentAssistant(
                editor.getPySourceViewer());
        SimpleAssistProcessor processor = (SimpleAssistProcessor) contentAssistant
                .getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
        processor.doCycle(); //we want to show the default completions in this case (not the simple ones)
        ICompletionProposal[] props = processor.computeCompletionProposals(editor.getPySourceViewer(),
                mod1Contents.length());

        for (int k = 0; k < props.length && k < proposalFractionFromEclipse; k += 1) {
            w.println("<cand>" + props[k].getDisplayString() + " ||| " + "</cand>");
        }

        w.println("</Pydev>");

        /*********************************************/

        //get inputs ------------------
        /* StringBuilder input = new StringBuilder();
         try {
             int offset = masterOffset;
             char c = ctx.getDocument().getChar(offset);
             while (c != '.') {
                 input.append(c);
                 offset--;
                 c = ctx.getDocument().getChar(offset);
             }
         } catch (BadLocationException e) {
             e.printStackTrace();
         }
         String inp = input.reverse().toString().trim();

         IPath path = ctx.getCompilationUnit().getResource().getRawLocation();
         String realPath = path.toOSString();

         w.println("<decalca>");

         DecalcaComputer comp = DecalcaComputer.getInstance(realPath);
         if (DecalcaComputer.isInitialized() == true) {
             ArrayList<Word> p = comp.getCandidates(pref, suf);
             int found = 0;

             for (int i = p.size() - 1; i > 0 && found < 5; i--) {
                 String token = p.get(i).mToken;
                 if (token.length() < inp.length()) {
                     continue;
                 }
                 if (inp.length() >= 1 ? p.get(i).mToken.substring(0, inp.length()).equals(inp) : true) {
                     defaultProposals.add(new CompletionProposal(token, masterOffset - replacementLength + 1,
                             replacementLength, token.length(), null,
                             token, null, "Suggested by DECALCA with probability " + p.get(i).mProb));
                     found++;
                     w.println("<cand>" + token + " ||| " + p.get(i).mProb + "</cand>");
                 }
             }
         }

         w.println("</decalca>");
        -----------------------*/
        //add in eclipse 
        for (int i = 0; i < proposalFractionFromEclipse; i++) {
            defaultProposals.add(props[i]);
        }

        w.println("</bead>");
        w.println();

        if (w != null) {
            w.close();
        }

        return defaultProposals;
    }

}
