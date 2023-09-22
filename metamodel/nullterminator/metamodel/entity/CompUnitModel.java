package nullterminator.metamodel.entity;

public interface CompUnitModel extends ro.lrg.xcore.metametamodel.XEntity {

	@ro.lrg.xcore.metametamodel.ThisIsAProperty
	public java.lang.Long numberOfLines();

	@ro.lrg.xcore.metametamodel.ThisIsAProperty
	public java.lang.String toString();

	@ro.lrg.xcore.metametamodel.ThisIsAProperty
	public java.lang.String getPackage();

	/**

 syntax tree

	*/
	@ro.lrg.xcore.metametamodel.ThisIsAProperty
	public java.lang.String print();

	@ro.lrg.xcore.metametamodel.ThisIsARelationBuilder
	public ro.lrg.xcore.metametamodel.Group<CheckedElementModel> checkedElementModelGroupOptionalPattern();

	java.lang.Object getUnderlyingObject();
}
