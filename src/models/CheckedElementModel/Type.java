package models.CheckedElementModel;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;

import nullterminator.metamodel.entity.CheckedElementModel;
import ro.lrg.xcore.metametamodel.IPropertyComputer;
import ro.lrg.xcore.metametamodel.PropertyComputer;

@PropertyComputer
public class Type implements IPropertyComputer<String, CheckedElementModel>{

	@Override
	public String compute(CheckedElementModel arg0) {
		SimpleName expr = (SimpleName)arg0.getUnderlyingObject();
		ITypeBinding type= expr.resolveTypeBinding();	
		
		return type.getName();
		
	}
}
