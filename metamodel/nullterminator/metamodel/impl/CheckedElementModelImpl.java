package nullterminator.metamodel.impl;

import nullterminator.metamodel.entity.*;
import models.CheckedElementModel.Type;
import models.CheckedElementModel.Location;
import models.CheckedElementModel.ToString;
import models.CheckedElementModel.AssignFinderOptionalPattern;
import models.CheckedElementModel.ShowInEditor;
import models.CheckedElementModel.RefactorByIntroducingOptional;

public class CheckedElementModelImpl implements CheckedElementModel {

	private java.lang.Object underlyingObj_;

	private static final Type Type_INSTANCE = new Type();
	private static final Location Location_INSTANCE = new Location();
	private static final ToString ToString_INSTANCE = new ToString();
	private static final AssignFinderOptionalPattern AssignFinderOptionalPattern_INSTANCE = new AssignFinderOptionalPattern();
	private static final ShowInEditor ShowInEditor_INSTANCE = new ShowInEditor();
	private static final RefactorByIntroducingOptional RefactorByIntroducingOptional_INSTANCE = new RefactorByIntroducingOptional();

	public CheckedElementModelImpl(java.lang.Object underlyingObj) {
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
	@ro.lrg.xcore.metametamodel.ThisIsARelationBuilder
	public ro.lrg.xcore.metametamodel.Group<MethodInvocationModel> assignFinderOptionalPattern() {
		return AssignFinderOptionalPattern_INSTANCE.buildGroup(this);
	}

	@Override
	@ro.lrg.xcore.metametamodel.ThisIsAnAction
	public void showInEditor() {
		 ShowInEditor_INSTANCE.performAction(this, ro.lrg.xcore.metametamodel.HListEmpty.getInstance());
	}

	@Override
	@ro.lrg.xcore.metametamodel.ThisIsAnAction
	public void refactorByIntroducingOptional() {
		 RefactorByIntroducingOptional_INSTANCE.performAction(this, ro.lrg.xcore.metametamodel.HListEmpty.getInstance());
	}

	public boolean equals(Object obj) {
		if (null == obj || !(obj instanceof CheckedElementModelImpl)) {
			return false;
		}
		CheckedElementModelImpl iObj = (CheckedElementModelImpl)obj;
		return underlyingObj_.equals(iObj.underlyingObj_);
	}

	public int hashCode() {
		return 97 * underlyingObj_.hashCode();
	}
}
