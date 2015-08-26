/**
 * Copyright (c) 2005-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
/*
 * Created on 24/09/2005
 */
package com.python.pydev.codecompletion.simpleassist;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.python.pydev.core.IGrammarVersionProvider;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.MisconfigurationException;
import org.python.pydev.core.docutils.PySelection;
import org.python.pydev.editor.IPySyntaxHighlightingAndCodeCompletionEditor;
import org.python.pydev.editor.simpleassist.ISimpleAssistParticipant;
import org.python.pydev.editor.simpleassist.ISimpleAssistParticipant2;
import org.python.pydev.shared_ui.proposals.PyCompletionProposal;

import com.python.pydev.codecompletion.ui.CodeCompletionPreferencesPage;

/**
 * Auto completion for keywords:
 * 
 * import keyword
 * >>> for k in keyword.kwlist: print k
and
assert
break
class
continue
def
del
elif
else
except
exec
finally
for
from
global
if
import
in
is
lambda
not
or
pass
print
raise
return
try
while
yield
 * @author Fabio
 */
public class KeywordsSimpleAssist implements ISimpleAssistParticipant, ISimpleAssistParticipant2 {

    //    public KeywordsSimpleAssist() {
    //        IPartListener2 openListener = new IPartListener2() {
    //            @Override
    //            public void partActivated(IWorkbenchPartReference partRef) {
    //            }
    //
    //            @Override
    //            public void partBroughtToTop(IWorkbenchPartReference partRef) {
    //            }
    //
    //            @Override
    //            public void partClosed(IWorkbenchPartReference partRef) {
    //            }
    //
    //            @Override
    //            public void partDeactivated(IWorkbenchPartReference partRef) {
    //            }
    //
    //            @Override
    //            public void partOpened(IWorkbenchPartReference partRef) {
    //            }
    //
    //            @Override
    //            public void partHidden(IWorkbenchPartReference partRef) {
    //            }
    //
    //            @Override
    //            public void partVisible(IWorkbenchPartReference partRef) {
    //                IEditorInput ip = partRef.getPage().getActiveEditor().getEditorInput();
    //                IPath path = ((FileEditorInput) ip).getPath();
    //                String p = path.toOSString();
    //                CachecaComputer.getInstance(p);
    //            }
    //
    //            @Override
    //            public void partInputChanged(IWorkbenchPartReference partRef) {
    //            }
    //        };
    //        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(openListener);
    //    }

    public static String defaultKeywordsAsString() {

        String[] KEYWORDS = new String[] { "and", "assert", "break", "class", "continue", "def", "del",
                //                "elif", -- starting with 'e'
                //                "else:", -- starting with 'e'
                //                "except:",  -- ctrl+1 covers for try..except/ starting with 'e'
                //                "exec", -- starting with 'e'
                "finally:", "for", "from", "global",
                //                "if", --too small
                "import",
                //                "in", --too small
                //                "is", --too small
                "lambda", "not",
                //                "or", --too small
                "pass", "print", "raise", "return",
                //                "try:", -- ctrl+1 covers for try..except
                "while", "with", "yield",

                //the ones below were not in the initial list
                "self", "__init__",
                //                "as", --too small
                "False", "None", "object", "True" };
        return wordsAsString(KEYWORDS);
    }

    //very simple cache (this might be requested a lot).
    private static String cache;
    private static String[] cacheRet;

    public static String[] stringAsWords(String keywords) {
        if (cache != null && cache.equals(keywords)) {
            return cacheRet;
        }
        StringTokenizer tokenizer = new StringTokenizer(keywords);
        ArrayList<String> strs = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            strs.add(tokenizer.nextToken());
        }
        cache = keywords;
        cacheRet = strs.toArray(new String[0]);
        return cacheRet;
    }

    /**
     * @param keywords keywords to be gotten as string
     * @return a string with all the passed words separated by '\n'
     */
    public static String wordsAsString(String[] keywords) {
        StringBuffer buf = new StringBuffer();
        for (String string : keywords) {
            buf.append(string);
            buf.append("\n");
        }
        return buf.toString();
    }

    /**
     * @see ISimpleAssistParticipant
     */
    public Collection<ICompletionProposal> computeCompletionProposals(String activationToken, String qualifier,
            PySelection ps, IPySyntaxHighlightingAndCodeCompletionEditor edit, int offset) {
        boolean isPy3Syntax = false;
        if (CodeCompletionPreferencesPage.forcePy3kPrintOnPy2()) {
            isPy3Syntax = true;

        } else {
            try {
                IPythonNature nature = edit.getPythonNature();
                if (nature != null) {
                    isPy3Syntax = nature.getGrammarVersion() >= IGrammarVersionProvider.GRAMMAR_PYTHON_VERSION_3_0;
                }
            } catch (MisconfigurationException e) {
            }
        }
        return innerComputeProposals(activationToken, qualifier, offset, false, isPy3Syntax, null);
    }

    public Collection<ICompletionProposal> computeCompletionProposalsTest(String activationToken, String qualifier,
            PySelection ps, IPySyntaxHighlightingAndCodeCompletionEditor edit, int offset, ITextViewer viewer) {
        boolean isPy3Syntax = false;
        if (CodeCompletionPreferencesPage.forcePy3kPrintOnPy2()) {
            isPy3Syntax = true;

        } else {
            try {
                IPythonNature nature = edit.getPythonNature();
                if (nature != null) {
                    isPy3Syntax = nature.getGrammarVersion() >= IGrammarVersionProvider.GRAMMAR_PYTHON_VERSION_3_0;
                }
            } catch (MisconfigurationException e) {
            }
        }
        return innerComputeProposals(activationToken, qualifier, offset, false, isPy3Syntax, viewer);
    }

    /**
     * @see ISimpleAssistParticipant2
     */
    public Collection<ICompletionProposal> computeConsoleProposals(String activationToken, String qualifier, int offset
            ) {
        return innerComputeProposals(activationToken, qualifier, offset, true, false, null);
    }

    /**
     * Collects simple completions (keywords)
     * 
     * @param activationToken activation token used
     * @param qualifier qualifier used
     * @param offset offset at which the completion was requested
     * @param buildForConsole whether the completions should be built for the console or not
     * @param isPy3Syntax if py 3 syntax we'll treat print differently.
     * @return a list with the completions available.
     */
    private Collection<ICompletionProposal> innerComputeProposals(String activationToken, String qualifier, int offset,
            boolean buildForConsole, boolean isPy3Syntax, ITextViewer viewer) {

        //get offset
        int replacementLength = 0;
        int masterOffset = offset;
        IDocument doc = viewer.getDocument();
        System.out.println("offset:" + offset);
        System.out.println("activationToken:" + activationToken);
        System.out.println("qualifier:" + qualifier);
        try {
            offset = masterOffset;
            //if(offset < 0) return eclipseProposals;

            if (doc != null)
            {
                char c = doc.getChar(offset - 1);
                while (c != '.' && offset > 1) {
                    offset--;
                    replacementLength++;
                    c = doc.getChar(offset - 1);
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        //get prefix
        StringBuilder b = new StringBuilder();
        try {
            int numberOfSeparators = 0;
            offset = masterOffset;
            offset -= replacementLength;
            char c = doc.getChar(offset - 1);
            boolean prevCharWasWhitespace = false;
            while (numberOfSeparators < 10 && offset > 1) {
                if (!(Character.isWhitespace(doc.getChar(offset - 1)))) {
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
                c = doc.getChar(offset - 1);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        String pref = b.reverse().toString();
        System.out.println("pref:" + pref);

        //get inputs
        StringBuilder input = new StringBuilder();
        try {
            offset = masterOffset;
            char c = doc.getChar(offset - 1);
            while (c != '.' && offset > 1) {
                input.append(c);
                offset--;
                c = doc.getChar(offset - 1);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        String inp = input.reverse().toString().trim();
        //System.out.println("Pref:" + pref);
        System.out.println("inp:" + inp);
        // Get the currently selected file from the editor
        IWorkbenchPart workbenchPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .getActivePart();
        IFile file = (IFile) workbenchPart.getSite().getPage().getActiveEditor().getEditorInput()
                .getAdapter(IFile.class);
        if (file == null) {
            try {
                throw new FileNotFoundException();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        IPath path = file.getRawLocation();
        //IPath path = IResource.getRawLocation()
        String realPath = path.toOSString();
        System.out.println("realPath:" + realPath);
        List<ICompletionProposal> cachecaProposals = new ArrayList<ICompletionProposal>();

        CachecaComputer comp = CachecaComputer.getInstance(realPath);
        if (CachecaComputer.isInitialized() == true) {
            ArrayList<Word> p = comp.getCandidates(pref);
            int found = 0;

            for (int i = p.size() - 1; i >= 0 && found < 100; i--) {
                String token = p.get(i).mToken;
                if (token.length() < inp.length()) {
                    continue;
                }
                //System.out.println("token:" + token);
                if (inp.length() >= 1 ? p.get(i).mToken.substring(0, inp.length()).equals(inp) : true) {
                    // System.out.println("Replacement Offset:" + (masterOffset - replacementLength + 1));
                    //System.out.println("Token:" + token);
                    cachecaProposals.add(new PyCompletionProposal(token, masterOffset - replacementLength,
                            replacementLength, token.length(),
                            "Suggested by CACHECA with probability " + p.get(i).mProb,
                            PyCompletionProposal.PRIORITY_DEFAULT));
                    found++;
                }
            }
        }
        //        for (ICompletionProposal icp : cachecaProposals)
        //        {
        //            System.out.println(icp.getDisplayString() + icp.getAdditionalProposalInfo());
        //        }
        if (cachecaProposals.size() == 0)
        /* start of pydev code */
        {
            System.out.println("Cacheca proposal count 0");
            offset = masterOffset;
            List<ICompletionProposal> results = new ArrayList<ICompletionProposal>();
            //check if we have to use it
            if (!CodeCompletionPreferencesPage.useKeywordsCodeCompletion()) {
                cachecaProposals.addAll(results);
            }

            //get them
            int qlen = qualifier.length();
            if (activationToken.equals("") && qualifier.equals("") == false) {
                for (String keyw : CodeCompletionPreferencesPage.getKeywords()) {
                    if (keyw.startsWith(qualifier) && !keyw.equals(qualifier)) {
                        if (buildForConsole) {
                            //In the console, only show the simple completions without any special treatment
                            results.add(new PyCompletionProposal(keyw, offset - qlen, qlen, keyw.length(),
                                    "Suggested by Pydev", PyCompletionProposal.PRIORITY_DEFAULT));

                        } else {
                            //in the editor, we'll create a special proposal with more features
                            if (isPy3Syntax) {
                                if ("print".equals(keyw)) {
                                    keyw = "print()";//Handling print in py3k.
                                }
                            }
                            results.add(new SimpleAssistProposal(keyw, offset - qlen, qlen, keyw.length(),
                                    "Suggested by Pydev", PyCompletionProposal.PRIORITY_DEFAULT));
                        }
                    }
                }
            }
            cachecaProposals.addAll(results);
        }
        // return results;

        //end of pydev code 
        return cachecaProposals;

    }

}
