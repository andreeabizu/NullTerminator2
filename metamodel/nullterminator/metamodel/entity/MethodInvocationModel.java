package nullterminator.metamodel.entity;

public interface MethodInvocationModel extends ro.lrg.xcore.metametamodel.XEntity {

	@ro.lrg.xcore.metametamodel.ThisIsAProperty
	public java.lang.String type();

	@ro.lrg.xcore.metametamodel.ThisIsAProperty
	public java.lang.String location();

	@ro.lrg.xcore.metametamodel.ThisIsAProperty
	public java.lang.String toString();

	@ro.lrg.xcore.metametamodel.ThisIsAnAction(numParams = 0) 
	public void showInEditor ();

	@ro.lrg.xcore.metametamodel.ThisIsAnAction(numParams = 0) 
	public void refactoringByIntroducingOptionalInsideMethod ();

	java.lang.Object getUnderlyingObject();
}
