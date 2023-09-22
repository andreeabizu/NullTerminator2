package models.CompUnitModel;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

import nullterminator.metamodel.entity.CompUnitModel;
import ro.lrg.xcore.metametamodel.IPropertyComputer;
import ro.lrg.xcore.metametamodel.PropertyComputer;

@PropertyComputer
public class NumberOfLines  implements IPropertyComputer<Long, CompUnitModel>{

	@Override
	public Long compute(CompUnitModel arg0) {
		long nr = 0;
		ICompilationUnit c = (ICompilationUnit)arg0.getUnderlyingObject();
		try {
			nr = c.getSource().split("\n").length;
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return nr;
	}

}



