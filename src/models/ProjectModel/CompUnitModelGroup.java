package models.ProjectModel;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;

import nullterminator.metamodel.entity.CompUnitModel;
import nullterminator.metamodel.entity.ProjectModel;
import nullterminator.metamodel.factory.Factory;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.IRelationBuilder;
import ro.lrg.xcore.metametamodel.RelationBuilder;


@RelationBuilder
public class CompUnitModelGroup implements IRelationBuilder<CompUnitModel, ProjectModel> {

	@Override
	public Group<CompUnitModel> buildGroup(ProjectModel arg0) {
		Group<CompUnitModel> systeCompUnitModeles = new Group<>();
		try {
			List<ICompilationUnit> classes = getClasses(arg0);
			for (ICompilationUnit i : classes) {
				CompUnitModel c = Factory.getInstance().createCompUnitModel(i);
				systeCompUnitModeles.add(c);
			}

		} catch (JavaModelException e) {
			System.err.println("CompUnitModel - ProjectModel:" + e.getMessage());
		}
		return systeCompUnitModeles;
	}

	private List<ICompilationUnit> getClasses(ProjectModel arg0) throws JavaModelException {
		List<ICompilationUnit> classes = new LinkedList<>();
		for (IPackageFragment i : ((IJavaProject) arg0.getUnderlyingObject()).getPackageFragments()) {
			for (IJavaElement j : i.getChildren()) {
				if (j.getElementType() == IJavaElement.COMPILATION_UNIT) {
						classes.add((ICompilationUnit) j);
				}
			}
		}
		return classes;
	}

}
