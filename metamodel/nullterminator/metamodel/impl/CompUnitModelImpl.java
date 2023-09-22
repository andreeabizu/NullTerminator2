package nullterminator.metamodel.impl;

import nullterminator.metamodel.entity.*;
import models.CompUnitModel.NumberOfLines;
import models.CompUnitModel.ToString;
import models.CompUnitModel.GetPackage;
import models.CompUnitModel.Print;
import models.CompUnitModel.CheckedElementModelGroupOptionalPattern;

public class CompUnitModelImpl implements CompUnitModel {

	private java.lang.Object underlyingObj_;

	private static final NumberOfLines NumberOfLines_INSTANCE = new NumberOfLines();
	private static final ToString ToString_INSTANCE = new ToString();
	private static final GetPackage GetPackage_INSTANCE = new GetPackage();
	private static final Print Print_INSTANCE = new Print();
	private static final CheckedElementModelGroupOptionalPattern CheckedElementModelGroupOptionalPattern_INSTANCE = new CheckedElementModelGroupOptionalPattern();

	public CompUnitModelImpl(java.lang.Object underlyingObj) {
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
	@ro.lrg.xcore.metametamodel.ThisIsAProperty
	public java.lang.String getPackage() {
		return GetPackage_INSTANCE.compute(this);
	}

	@Override
	/**

 syntax tree

	*/
	@ro.lrg.xcore.metametamodel.ThisIsAProperty
	public java.lang.String print() {
		return Print_INSTANCE.compute(this);
	}

	@Override
	@ro.lrg.xcore.metametamodel.ThisIsARelationBuilder
	public ro.lrg.xcore.metametamodel.Group<CheckedElementModel> checkedElementModelGroupOptionalPattern() {
		return CheckedElementModelGroupOptionalPattern_INSTANCE.buildGroup(this);
	}

	public boolean equals(Object obj) {
		if (null == obj || !(obj instanceof CompUnitModelImpl)) {
			return false;
		}
		CompUnitModelImpl iObj = (CompUnitModelImpl)obj;
		return underlyingObj_.equals(iObj.underlyingObj_);
	}

	public int hashCode() {
		return 97 * underlyingObj_.hashCode();
	}
}
