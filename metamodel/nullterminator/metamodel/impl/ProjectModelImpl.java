package nullterminator.metamodel.impl;

import nullterminator.metamodel.entity.*;
import models.ProjectModel.NumberOfLines;
import models.ProjectModel.ToString;
import models.ProjectModel.CompUnitModelGroup;
import models.ProjectModel.CheckedElementModelGroupOptionalPattern;

public class ProjectModelImpl implements ProjectModel {

	private java.lang.Object underlyingObj_;

	private static final NumberOfLines NumberOfLines_INSTANCE = new NumberOfLines();
	private static final ToString ToString_INSTANCE = new ToString();
	private static final CompUnitModelGroup CompUnitModelGroup_INSTANCE = new CompUnitModelGroup();
	private static final CheckedElementModelGroupOptionalPattern CheckedElementModelGroupOptionalPattern_INSTANCE = new CheckedElementModelGroupOptionalPattern();

	public ProjectModelImpl(java.lang.Object underlyingObj) {
		underlyingObj_ = underlyingObj;
	}

	@Override
	public java.lang.Object getUnderlyingObject() {
		return underlyingObj_;
	}

	@Override
	@ro.lrg.xcore.metametamodel.ThisIsAProperty
	public java.lang.Long numberOfLines() {
		return NumberOfLines_INSTANCE.compute(this);
	}

	@Override
	@ro.lrg.xcore.metametamodel.ThisIsAProperty
	public java.lang.String toString() {
		return ToString_INSTANCE.compute(this);
	}

	@Override
	@ro.lrg.xcore.metametamodel.ThisIsARelationBuilder
	public ro.lrg.xcore.metametamodel.Group<CompUnitModel> compUnitModelGroup() {
		return CompUnitModelGroup_INSTANCE.buildGroup(this);
	}

	@Override
	@ro.lrg.xcore.metametamodel.ThisIsARelationBuilder
	public ro.lrg.xcore.metametamodel.Group<CheckedElementModel> checkedElementModelGroupOptionalPattern() {
		return CheckedElementModelGroupOptionalPattern_INSTANCE.buildGroup(this);
	}

	public boolean equals(Object obj) {
		if (null == obj || !(obj instanceof ProjectModelImpl)) {
			return false;
		}
		ProjectModelImpl iObj = (ProjectModelImpl)obj;
		return underlyingObj_.equals(iObj.underlyingObj_);
	}

	public int hashCode() {
		return 97 * underlyingObj_.hashCode();
	}
}
