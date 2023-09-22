package models.MethodInvocationModel;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import nullterminator.metamodel.entity.MethodInvocationModel;
import ro.lrg.xcore.metametamodel.ActionPerformer;
import ro.lrg.xcore.metametamodel.HListEmpty;
import ro.lrg.xcore.metametamodel.IActionPerformer;
import utility.UtilityClass;

@ActionPerformer
public class ShowInEditor implements IActionPerformer<Void, MethodInvocationModel, HListEmpty> {

	@Override
	public Void performAction(MethodInvocationModel arg0, HListEmpty arg1) {
		
		//MethodInvocation m = (MethodInvocation)arg0.getUnderlyingObject();
		Expression m = (Expression)arg0.getUnderlyingObject();
		ICompilationUnit iCompilationUnit = UtilityClass.getICompilationUnit(m);
		
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IPath path = iCompilationUnit.getPath();
		IFile file = FileBuffers.getWorkspaceFileAtLocation(path);
		if(file != null) {
			ITextEditor editor=null;
			try {
				editor = (ITextEditor) IDE.openEditor(page, file);
			} catch (PartInitException e) {
					
				e.printStackTrace();
			}
			editor.selectAndReveal(m.getStartPosition(),m.getLength());
		
		}
		return null;
	}
		

}