package models.ProjectModel;

import org.eclipse.jdt.core.IJavaProject;

import nullterminator.metamodel.entity.ProjectModel;
import ro.lrg.xcore.metametamodel.*;

@PropertyComputer
public class ToString implements IPropertyComputer<String, ProjectModel> {

	@Override
	public String compute(ProjectModel entity) {
		IJavaProject p = (IJavaProject)entity.getUnderlyingObject();
		return p.getElementName();
	}
}