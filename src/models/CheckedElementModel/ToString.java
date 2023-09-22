package models.CheckedElementModel;


import org.eclipse.jdt.core.dom.SimpleName;

import nullterminator.metamodel.entity.CheckedElementModel;
import ro.lrg.xcore.metametamodel.IPropertyComputer;
import ro.lrg.xcore.metametamodel.PropertyComputer;

@PropertyComputer
public class ToString implements IPropertyComputer<String, CheckedElementModel>{

	@Override
	public String compute(CheckedElementModel arg0) {
		SimpleName m = (SimpleName)arg0.getUnderlyingObject();
		return m.toString();
	}
	
	
}
