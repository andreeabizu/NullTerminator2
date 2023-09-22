package demo.Startup1;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import nullterminator.metamodel.factory.Factory;
import ro.lrg.insider.view.ToolRegistration.XEntityConverter;
import ro.lrg.xcore.metametamodel.XEntity;

public class MyXEntityConverter implements XEntityConverter {

	@Override
	public XEntity convert(Object element) {
	
		if(element instanceof IJavaProject)
			return Factory.getInstance().createProjectModel((IJavaProject)element);
		if(element instanceof MethodInvocation)
			return Factory.getInstance().createMethodInvocationModel((MethodInvocation)element);
		if(element instanceof SuperMethodInvocation)
			return Factory.getInstance().createMethodInvocationModel((SuperMethodInvocation)element);
		if(element instanceof SimpleName)
			return Factory.getInstance().createCheckedElementModel((SimpleName)element);
		if(element instanceof ICompilationUnit)
			return Factory.getInstance().createCompUnitModel((ICompilationUnit)element);
		return null;
		
	}
}