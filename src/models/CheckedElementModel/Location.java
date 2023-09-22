package models.CheckedElementModel;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;

import nullterminator.metamodel.entity.CheckedElementModel;
import ro.lrg.xcore.metametamodel.IPropertyComputer;
import ro.lrg.xcore.metametamodel.PropertyComputer;
import utility.UtilityClass;


@PropertyComputer
public class Location implements IPropertyComputer<String, CheckedElementModel>{

	@Override
	public String compute(CheckedElementModel arg0) {
		SimpleName m = (SimpleName)arg0.getUnderlyingObject();
		ICompilationUnit iCompilationUnit = UtilityClass.getICompilationUnit(m);
		
		return iCompilationUnit.getElementName();
	}

	
}