package cachecatest;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

//This class written by Christine.

public class JavaCompletionProposals implements IJavaCompletionProposalComputer {

    public JavaCompletionProposals() {
        IPartListener2 openListener = new IPartListener2() {
            @Override
            public void partActivated(IWorkbenchPartReference partRef) {
            }

            @Override
            public void partBroughtToTop(IWorkbenchPartReference partRef) {
            }

            @Override
            public void partClosed(IWorkbenchPartReference partRef) {
            }

            @Override
            public void partDeactivated(IWorkbenchPartReference partRef) {
            }

            @Override
            public void partOpened(IWorkbenchPartReference partRef) {
            }

            @Override
            public void partHidden(IWorkbenchPartReference partRef) {
            }

            @Override
            public void partVisible(IWorkbenchPartReference partRef) {
                IEditorInput ip = partRef.getPage().getActiveEditor().getEditorInput();

                IPath path = ((FileEditorInput) ip).getPath();
                String p = path.toOSString();
                //DecalcaComputer.getInstance(p);
            }

            @Override
            public void partInputChanged(IWorkbenchPartReference partRef) {
            }
        };
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(openListener);
    }

    @Override
    public void sessionStarted() {
    }

    @Override
    public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
            IProgressMonitor monitor) {
        //get proposals
        EclipseIntegration compute = new EclipseIntegration();
        return compute.computeCompletionProposals(context, monitor);
    }

    @Override
    public List<IContextInformation> computeContextInformation(
            ContentAssistInvocationContext context, IProgressMonitor monitor) {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public void sessionEnded() {
    }

}