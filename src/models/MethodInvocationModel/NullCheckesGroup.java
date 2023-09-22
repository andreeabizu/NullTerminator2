package models.MethodInvocationModel;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import nullterminator.metamodel.entity.CheckedElementModel;
import nullterminator.metamodel.entity.MethodInvocationModel;
import nullterminator.metamodel.entity.ProjectModel;
import nullterminator.metamodel.factory.Factory;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.IRelationBuilder;
import ro.lrg.xcore.metametamodel.RelationBuilder;
import utility.UtilityClass;


public class NullCheckesGroup {


	public Set<ASTNode> buildGroup(MethodInvocationModel arg0) {
		Set<ASTNode> assignmentsForVariableInIf = new HashSet<>();
		Expression expr = (Expression) arg0.getUnderlyingObject();
		Assignment assignment = (Assignment) UtilityClass.getAssignment(expr);
		VariableDeclaration variableDecl = (VariableDeclaration) UtilityClass.getVariableDeclaration(expr);
		SimpleName variable = null;

		if(assignment != null) {
			variable = (SimpleName) assignment.getLeftHandSide();
		}else if(variableDecl != null) {
			variable = variableDecl.getName();
		}
		
		if(variable!=null) {
		IVariableBinding variableBinding = (IVariableBinding) variable.resolveBinding();
		ProjectModel projectModel = Factory.getInstance().createProjectModel(variableBinding.getJavaElement().getJavaProject());
		Group<CheckedElementModel> checkedElementGroup = projectModel.checkedElementModelGroupOptionalPattern();
		
		for(CheckedElementModel model: checkedElementGroup.getElements()) {
			ASTNode node = null;

			Assignment assignmentNew = (Assignment) UtilityClass.getAssignment(expr);
			VariableDeclaration variableDeclNew = (VariableDeclaration) UtilityClass.getVariableDeclaration(expr);
			SimpleName name = (SimpleName) model.getUnderlyingObject();
			if(assignmentNew != null) {
				node = assignmentNew;
			}else if(variableDeclNew != null) {
				node = variableDeclNew;
			}
			System.out.println("var "+ name+ "----"+node);
			if(name.resolveBinding().isEqualTo(variableBinding)) {
				//variableInIf.add(Factory.getInstance().createCheckedElementModel(name));
					assignmentsForVariableInIf.add(node);
				
			}
			}
		}
		return assignmentsForVariableInIf;
	}


}
