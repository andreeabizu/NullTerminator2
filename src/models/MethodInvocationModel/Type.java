package models.MethodInvocationModel;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import nullterminator.metamodel.entity.MethodInvocationModel;
import ro.lrg.xcore.metametamodel.IPropertyComputer;
import ro.lrg.xcore.metametamodel.PropertyComputer;

@PropertyComputer
public class Type implements IPropertyComputer<String, MethodInvocationModel>{

	@Override
	public String compute(MethodInvocationModel arg0) {
		Expression expr = (Expression)arg0.getUnderlyingObject();
		ITypeBinding type= expr.resolveTypeBinding();	
		
		return type.getName();
		
	}
}