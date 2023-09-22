package nullterminator.metamodel.entity;

public interface ProjectModel extends ro.lrg.xcore.metametamodel.XEntity {

	@ro.lrg.xcore.metametamodel.ThisIsAProperty
	public java.lang.Long numberOfLines();

	@ro.lrg.xcore.metametamodel.ThisIsAProperty
	public java.lang.String toString();

	@ro.lrg.xcore.metametamodel.ThisIsARelationBuilder
	public ro.lrg.xcore.metametamodel.Group<CompUnitModel> compUnitModelGroup();

	@ro.lrg.xcore.metametamodel.ThisIsARelationBuilder
	public ro.lrg.xcore.metametamodel.Group<CheckedElementModel> checkedElementModelGroupOptionalPattern();

	java.lang.Object getUnderlyingObject();
}
