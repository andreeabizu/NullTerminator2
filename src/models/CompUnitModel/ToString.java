package models.CompUnitModel;

import org.eclipse.jdt.core.ICompilationUnit;

import nullterminator.metamodel.entity.CompUnitModel;
import ro.lrg.xcore.metametamodel.IPropertyComputer;
import ro.lrg.xcore.metametamodel.PropertyComputer;

@PropertyComputer
public class ToString  implements IPropertyComputer<String, CompUnitModel>{

	@Override
	public String compute(CompUnitModel arg0) {
		return ((ICompilationUnit)arg0.getUnderlyingObject()).getElementName();
	}

}



