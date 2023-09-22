package models.ProjectModel;


import nullterminator.metamodel.entity.CompUnitModel;
import nullterminator.metamodel.entity.CheckedElementModel;
import nullterminator.metamodel.entity.ProjectModel;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.IRelationBuilder;
import ro.lrg.xcore.metametamodel.RelationBuilder;

@RelationBuilder
public class CheckedElementModelGroupOptionalPattern implements IRelationBuilder<CheckedElementModel, ProjectModel> {

	@Override
	public Group<CheckedElementModel> buildGroup(ProjectModel arg0) {
		Group<CheckedElementModel> expr = new Group<>();
		Group<CompUnitModel> systeCompUnitModeles = arg0.compUnitModelGroup();
		for(CompUnitModel c: systeCompUnitModeles.getElements()) {
			expr.addAll(c.checkedElementModelGroupOptionalPattern().getElements());
		}
		return expr;
	}

}