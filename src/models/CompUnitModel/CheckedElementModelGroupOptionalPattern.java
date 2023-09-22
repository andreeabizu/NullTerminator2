package models.CompUnitModel;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import nullterminator.metamodel.entity.CheckedElementModel;
import nullterminator.metamodel.entity.CompUnitModel;
import nullterminator.metamodel.entity.MethodInvocationModel;
import nullterminator.metamodel.factory.Factory;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.IRelationBuilder;
import ro.lrg.xcore.metametamodel.RelationBuilder;
import utility.UtilityClass;

@RelationBuilder
public class CheckedElementModelGroupOptionalPattern implements IRelationBuilder<CheckedElementModel, CompUnitModel> {
	
	
	@Override
	public Group<CheckedElementModel> buildGroup(CompUnitModel arg0) {
		
		ICompilationUnit compilationUnit = (ICompilationUnit) arg0.getUnderlyingObject();
		ASTParser parser=ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(compilationUnit);
		parser.setResolveBindings(true);
		
		
		Group<CheckedElementModel> vars = new Group<>();
		ASTNode node =parser.createAST(null);
		
	
		ASTVisitor v = new ASTVisitor() {	
			
			private IMethodBinding nullCheckLocationMethod;	
			
			public boolean visit(MethodDeclaration method)
			{
				if (method.resolveBinding()!=null)
				{
					nullCheckLocationMethod= method.resolveBinding();
					
				}
				return true;
			}

			public boolean visit (final IfStatement node)
			{
				node.getExpression().accept(new ASTVisitor(){
				public boolean visit(InfixExpression ex)
					{
					Expression left,right;
					InfixExpression.Operator operator;
					left=ex.getLeftOperand();
					right=ex.getRightOperand();
					operator=ex.getOperator();
					System.out.println(left+" "+left.getClass()+" "+left.resolveTypeBinding());
					try
						{
						if (left instanceof NullLiteral)
							{
							if (right instanceof SimpleName)
								processVariable((SimpleName)right,node, operator);
							else if (right instanceof QualifiedName)
								processVariable(((QualifiedName)right).getName(),node, operator);
							else if (right instanceof FieldAccess)
								processVariable(((FieldAccess)right).getName(), node, operator);
							else if (right instanceof SuperFieldAccess)
								processVariable(((SuperFieldAccess)right).getName(), node, operator);
							else if (right instanceof MethodInvocation)
								processMethod((MethodInvocation)right, node, operator);
							else if (right instanceof SuperMethodInvocation)
								processMethod((SuperMethodInvocation)right, node, operator);
							}
						else if (right instanceof NullLiteral)
							{
							if (left instanceof SimpleName)
								processVariable((SimpleName)left,node, operator);
							else if (left instanceof QualifiedName)
								{
								processVariable(((QualifiedName)left).getName(),node, operator);
								}
							else if (left instanceof FieldAccess)
								processVariable(((FieldAccess)left).getName(), node, operator);
							else if (left instanceof SuperFieldAccess)
								processVariable(((SuperFieldAccess)left).getName(), node, operator);
							else if (left instanceof MethodInvocation)
								processMethod((MethodInvocation)left, node, operator);
							else if (left instanceof SuperMethodInvocation)
								processMethod((SuperMethodInvocation)left, node, operator);
							}
						
						}
					catch (Exception e)
						{
						}
					return true;
					}
				});
				return true;
			}
			
			private void processVariable(SimpleName var, IfStatement node, InfixExpression.Operator op) throws JavaModelException
			{
				IVariableBinding variableBinding = (IVariableBinding) ((SimpleName)var).resolveBinding();
				System.out.println(UtilityClass.getThenBlock(var)+ " "+ UtilityClass.getCheckedElementType(var)+ " "+UtilityClass.getElseBlock(var)+" "+UtilityClass.getICompilationUnit(var)+" "+UtilityClass.getOperator(var)+" "+UtilityClass.getNullCheckLocationMethod(var));
				if (variableBinding!=null)
					{
					if(variableBinding.getKind()==IBinding.VARIABLE && !variableBinding.isField() && !variableBinding.isParameter()) { //local variable !!!
					ITypeBinding variableType=variableBinding.getType(); 
					if (variableType != null && canBeRefactored(node))
						{
							Group<MethodInvocationModel> group = new Group<MethodInvocationModel>();
					    	CheckedElementModel p= Factory.getInstance().createCheckedElementModel(var);
					    	group = p.assignFinderOptionalPattern();
					    	if(group.getElements().size()!=0) {
					    		vars.add(p);
					    	}	
							
						}
					}
					}
			}
			
			private void processMethod(MethodInvocation invocation, IfStatement node, InfixExpression.Operator op) throws JavaModelException
			{
				IMethodBinding checkedMethodBinding=invocation.resolveMethodBinding();
				processInvocation(checkedMethodBinding,node,op,invocation.getName());
			}
			
			private void processMethod(SuperMethodInvocation invocation, IfStatement node, InfixExpression.Operator op) throws JavaModelException
			{
				IMethodBinding checkedMethodBinding=invocation.resolveMethodBinding();
				processInvocation(checkedMethodBinding,node,op,invocation.getName());
				
			}
			
			private void processInvocation(IMethodBinding checkedMethodBinding,IfStatement node, InfixExpression.Operator op, SimpleName elementName)throws JavaModelException{
				if (checkedMethodBinding!=null)
				{
					IType retType=null;
					if (checkedMethodBinding.getReturnType().getJavaElement() instanceof IType)
						retType=(IType) checkedMethodBinding.getReturnType().getJavaElement();
					ITypeBinding declaringType=null;
					
					if (checkedMethodBinding.getDeclaringClass().getJavaElement() instanceof IType)
						declaringType = checkedMethodBinding.getDeclaringClass();
					
					if (retType!=null && declaringType!=null)
					{
						if(canBeRefactored(node)) { 
							Group<MethodInvocationModel> group = new Group<MethodInvocationModel>();
							CheckedElementModel p= Factory.getInstance().createCheckedElementModel(elementName);
							group = p.assignFinderOptionalPattern();
					    	if(group.getElements().size()!=0) {
					    		vars.add(p);
					    	}	
						}
						
					}	
				}	
				else 
					System.out.println("checkedMethodBinding is null");
				
			}
			
			private boolean canBeRefactored(IfStatement node) 
			{
				if (node.getExpression().toString().contains("&&") || node.getExpression().toString().contains("||"))
					return false;
				return true;
			}
			
		};
		
		node.accept(v);
		
		return vars;

	}
}