package nullterminator.metamodel.impl;

import nullterminator.metamodel.entity.*;
import models.MethodInvocationModel.Type;
import models.MethodInvocationModel.Location;
import models.MethodInvocationModel.ToString;
import models.MethodInvocationModel.ShowInEditor;
import models.MethodInvocationModel.RefactoringByIntroducingOptionalInsideMethod;

public class MethodInvocationModelImpl implements MethodInvocationModel {

	private java.lang.Object underlyingObj_;

	private static final Type Type_INSTANCE = new Type();
	private static final Location Location_INSTANCE = new Location();
	private static final ToString ToString_INSTANCE = new ToString();
	private static final ShowInEditor ShowInEditor_INSTANCE = new ShowInEditor();
	private static final RefactoringByIntroducingOptionalInsideMethod RefactoringByIntroducingOptionalInsideMethod_INSTANCE = new RefactoringByIntroducingOptionalInsideMethod();

	public MethodInvocationModelImpl(java.lang.Object underlyingObj) {
		underlyingObj_ = underlyingObj;
	}

	@Override
	public java.lang.Object getUnderlyingObject() {
		return underlyingObj_;
	}

	@Override
	@ro.lrg.xcore.metametamodel.ThisIsAProperty
	public java.lang.String type() {
		return Type_INSTANCE.compute(this);
	}

	@Override
	@ro.lrg.xcore.metametamodel.ThisIsAProperty
	public java.lang.String location() {
		return Location_INSTANCE.compute(this);
	}

	@Override
	@ro.lrg.xcore.metametamodel.ThisIsAProperty
	public java.lang.String toString() {
		return ToString_INSTANCE.compute(this);
	}

	@Override
	@ro.lrg.xcore.metametamodel.ThisIsAnAction
	public void showInEditor() {
		 ShowInEditor_INSTANCE.performAction(this, ro.lrg.xcore.metametamodel.HListEmpty.getInstance());
	}

	@Override
	@ro.lrg.xcore.metametamodel.ThisIsAnAction
	public void refactoringByIntroducingOptionalInsideMethod() {
		 RefactoringByIntroducingOptionalInsideMethod_INSTANCE.performAction(this, ro.lrg.xcore.metametamodel.HListEmpty.getInstance());
	}

	public boolean equals(Object obj) {
		if (null == obj || !(obj instanceof MethodInvocationModelImpl)) {
			return false;
		}
		MethodInvocationModelImpl iObj = (MethodInvocationModelImpl)obj;
		return underlyingObj_.equals(iObj.underlyingObj_);
	}

	public int hashCode() {
		return 97 * underlyingObj_.hashCode();
	}
}
