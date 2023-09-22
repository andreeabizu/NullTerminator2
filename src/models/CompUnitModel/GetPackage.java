package models.CompUnitModel;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaModelException;

import nullterminator.metamodel.entity.CompUnitModel;
import ro.lrg.xcore.metametamodel.IPropertyComputer;
import ro.lrg.xcore.metametamodel.PropertyComputer;

@PropertyComputer
public class GetPackage  implements IPropertyComputer<String, CompUnitModel>{

	@Override
	public String compute(CompUnitModel arg0) {
		try {
			IPackageDeclaration[] pack = ((ICompilationUnit)arg0.getUnderlyingObject()).getPackageDeclarations();
			if(pack.length==1)
			return ((ICompilationUnit)arg0.getUnderlyingObject()).getPackageDeclarations()[0].toString();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

}