package models.ProjectModel;

import nullterminator.metamodel.entity.CompUnitModel;
import nullterminator.metamodel.entity.ProjectModel;
import ro.lrg.xcore.metametamodel.*;

@PropertyComputer
public class NumberOfLines implements IPropertyComputer<Long, ProjectModel> {

	@Override
	public Long compute(ProjectModel entity) {
		
		long noOfLines = 0;
		Group<CompUnitModel> comp = entity.compUnitModelGroup();
		for(CompUnitModel c : comp.getElements()) {
			noOfLines += c.numberOfLines();
		}
		 return noOfLines;
	}
}