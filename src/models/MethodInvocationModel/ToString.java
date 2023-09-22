package models.MethodInvocationModel;

import org.eclipse.jdt.core.dom.Expression;

import nullterminator.metamodel.entity.MethodInvocationModel;
import ro.lrg.xcore.metametamodel.IPropertyComputer;
import ro.lrg.xcore.metametamodel.PropertyComputer;

@PropertyComputer
public class ToString implements IPropertyComputer<String, MethodInvocationModel>{

	@Override
	public String compute(MethodInvocationModel arg0) {
		Expression m = (Expression)arg0.getUnderlyingObject();
		return m.toString();
	}
	
	
}