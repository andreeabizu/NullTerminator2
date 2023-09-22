package models.MethodInvocationModel;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.Expression;

import nullterminator.metamodel.entity.MethodInvocationModel;
import ro.lrg.xcore.metametamodel.IPropertyComputer;
import ro.lrg.xcore.metametamodel.PropertyComputer;
import utility.UtilityClass;

@PropertyComputer
public class Location implements IPropertyComputer<String, MethodInvocationModel>{

	@Override
	public String compute(MethodInvocationModel arg0) {
		Expression m = (Expression)arg0.getUnderlyingObject();
		ICompilationUnit iCompilationUnit = UtilityClass.getICompilationUnit(m);
		
		return iCompilationUnit.getElementName();
	}

	
}