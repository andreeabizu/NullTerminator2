package models.CheckedElementModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import models.MethodInvocationModel.ExpressionVisitor;
import models.MethodInvocationModel.StatementVisitor;
import nullterminator.metamodel.entity.CheckedElementModel;
import nullterminator.metamodel.entity.MethodInvocationModel;
import ro.lrg.xcore.metametamodel.ActionPerformer;
import ro.lrg.xcore.metametamodel.HListEmpty;
import ro.lrg.xcore.metametamodel.IActionPerformer;
import utility.UtilityClass;

@ActionPerformer
public class RefactorByIntroducingOptional implements IActionPerformer<Void, CheckedElementModel, HListEmpty> {

	private SimpleName name;
	private MethodInvocation methodI;
	private IfStatement ifStatement;
	private boolean result; 
	private IVariableBinding varBinding;
	
	
	@Override
	public Void performAction(CheckedElementModel arg0, HListEmpty arg1) {
	

		List<MethodInvocationModel> methodInvocationGroup = arg0.assignFinderOptionalPattern().getElements();
		MethodInvocationModel model = null;
		if(methodInvocationGroup.size()>0) {
		model = methodInvocationGroup.get(0);
		methodI = (MethodInvocation) model.getUnderlyingObject();
		//methodInvocation.refactoringByIntroducingOptional();
		}
	
		//methodInvocation = arg0.assignFinderOptionalPattern().getElements().get(0);
		name = (SimpleName)arg0.getUnderlyingObject();
	
		ifStatement = UtilityClass.getIfStatement(name);
		
		if(ifStatement==null) {
			return null;
		}
		System.out.println("if statement:  ---"+ifStatement);
		if(ifStatement.getElseStatement()!=null) {
			return null;
		}
		
		Statement thenStatement = ifStatement.getThenStatement();
		System.out.println("aici am ajuns");

		
		if(getOperator(ifStatement).equals("==")) {	
			doRefactoringForEqualCondition(thenStatement);			
		}if(getOperator(ifStatement).equals("!=")) {
			doRefactoringForNotEqualCondition(thenStatement);
		}
		
		model.refactoringByIntroducingOptionalInsideMethod();
		
				
		return null;
	}
	
    private void doRefactoringForEqualCondition(Statement statement) {
		
    	Statement thenStatement = statement;
		if(statement instanceof Block) {
			List statements = ((Block)thenStatement).statements();
			
			if(statements.size()!=1) {
				return;
			}	
			thenStatement = (Statement) statements.get(0);
		}
		
		if(thenStatement instanceof ExpressionStatement){
			CheckThatVarIsAssigned checkThatVarIsAssigned = new CheckThatVarIsAssigned(name);
			thenStatement.accept(checkThatVarIsAssigned);
			if(checkThatVarIsAssigned.isAssigned()) {
				
				if(name.resolveBinding() instanceof IMethodBinding) {
					return;
				}
				
				if(checkThatVarIsAssignedBetween()) {
					System.out.println(" ---- "+name.getStartPosition()+"---u-"+thenStatement.getStartPosition());
					return;
				}
				System.out.println("pattern 4: "+thenStatement);
				
				createOrElseGetAssign(ifStatement,thenStatement,checkThatVarIsAssigned.getValue());
			///	refact pattern 4
			}
				
		}else if(thenStatement instanceof  ThrowStatement){
			
			System.out.println("Throw statement-----");
			ASTVisitor checkVariableEffectivelyFinalInThrowS = new ASTVisitor() {
		
				public boolean visit(ThrowStatement throwStatement) {
					
					CheckVariableFinalOrEffectivelyFinalVisitor finalVisitor = new CheckVariableFinalOrEffectivelyFinalVisitor();
					throwStatement.accept(finalVisitor);
					if(!finalVisitor.areAllVarsFinalOrEffectivelyFinal()) {
						System.out.println("are not effectively final");
						result = false;
						return false;
					}System.out.println("areeffectively final");
					result = true;
					return false;
				}};
				
			thenStatement.accept(checkVariableEffectivelyFinalInThrowS);
			if(result) {
				Expression throwExpr = ((ThrowStatement) thenStatement).getExpression();
				IBinding binding = name.resolveBinding();
				if(binding instanceof IMethodBinding) {
					
					System.out.println("pattern 1: "+thenStatement);
					
					 createOrElseThrowCall(ifStatement,throwExpr);
					/// refactor pattern 1
				}else if(binding instanceof IVariableBinding) {
					//////// de aici
					
					if(checkThatVarIsAssignedBetween()) {
						System.out.println(" ---- "+name.getStartPosition()+"---u-"+thenStatement.getStartPosition());
						return;
					}
					//pana aici
					System.out.println("pattern 2: "+thenStatement);
					createOrElseThrowAssign(ifStatement,throwExpr);
					
					/// refactor pattern 2
				}
			}
		}
    }
    
    private void doRefactoringForNotEqualCondition(Statement thenStatement) {
  
    	CheckIfThereIsAReturnVisitor returnVisitor = new CheckIfThereIsAReturnVisitor();
    	
    	thenStatement.accept(returnVisitor);

    	if(returnVisitor.existReturn()) {
    		System.out.println("exista return");
    		return;
    	}
    	System.out.println("dupa return----");
    	
    	GetVariableDeclarationVisitor variableVisitor = new GetVariableDeclarationVisitor();
    	
    	thenStatement.accept(variableVisitor);
    	List<IVariableBinding> variables = variableVisitor.getVariables();
    	System.out.println("varsL "+variables);
    	CheckVariableFinalOrEffectivelyFinalVisitor finalOrEffectivelyFinalVisitor = new CheckVariableFinalOrEffectivelyFinalVisitor(variables);
    	thenStatement.accept(finalOrEffectivelyFinalVisitor);
    	
    	if(!finalOrEffectivelyFinalVisitor.areAllVarsFinalOrEffectivelyFinal()) {
    		return;
    	}
    	System.out.println("dupa all vars ar final----");
    	
    	if(finalOrEffectivelyFinalVisitor.containsAssignmentForVarsDeclaredOutside()) {
    		return;
    	}
    	System.out.println("dupa assgn----");
    	
    	IBinding binding = name.resolveBinding();
		if(binding instanceof IMethodBinding) {
			System.out.println("pattern 5: "+thenStatement);
		
			createIfPresentMethod(ifStatement,thenStatement,false);
			/// refactor pattern 5
		}else if(binding instanceof IVariableBinding) {
			if(checkThatVarIsAssignedBetween()) {
				System.out.println(" ---- "+name.getStartPosition()+"---u-"+thenStatement.getStartPosition());
				return;
			}
			varBinding = (IVariableBinding) binding;
			System.out.println("pattern 3: "+thenStatement);
			createIfPresentMethod(ifStatement,thenStatement,true);
			/// refactor pattern 3
		}
    }
    
   private String getOperator(IfStatement ifStatement){
		
    	InfixExpression expr = (InfixExpression)ifStatement.getExpression();
    	return expr.getOperator().toString();
		
	}
   
   private boolean checkThatVarIsAssignedBetween() {
	   System.out.println("checkThatVarIsAssignedBetween");
	    CheckThatVarIsAssignedBetween checkThatVarIsAssignedBetween = new CheckThatVarIsAssignedBetween(name,methodI.getStartPosition(),name.getStartPosition());
		UtilityClass.getCompilationUnit(methodI).accept(checkThatVarIsAssignedBetween);
		
		if(checkThatVarIsAssignedBetween.isAssigned()) {
			System.out.println(" ---- "+name.getStartPosition()+"---u-");
			return true;
		}
		return false;
   }
   
   private void createIfPresentMethod(IfStatement ifS,Statement thenStatement,boolean var){
	   
	   ICompilationUnit cUnit = UtilityClass.getICompilationUnit(name);
	   //ICompilationUnit cUnit = ((IMember) methodI.resolveMethodBinding().getJavaElement()).getCompilationUnit();
	   AST ast = AST.newAST(AST.JLS8);
	   final ASTRewrite rewriter=ASTRewrite.create(ast);
	   MethodInvocation newMethodInvocation = createMethodInvocationWithLambdaParam(ast,"ifPresent",thenStatement,1,var);
	   replaceNodeWithAnother(ast,ifS,newMethodInvocation,cUnit);
	   
	   if(var) {
		   ASTVisitor visitor = new ASTVisitor() {
			   public boolean visit(SimpleName n) {
				   System.out.println(name+"    GGGGGGGGTTTTTTTTTTTTT "+name.resolveBinding());
				   /*if(name.resolveBinding() instanceof IVariableBinding) {
					   System.out.println(name+"  GGGGGGGG"+ name.resolveBinding());
					   if(name.resolveBinding().isEqualTo(varBinding)) {
						   System.out.println(name+"  GGGGGG999999999GG");
						   //name.setIdentifier("iii");
						   AST ast = name.getAST();
						   final ASTRewrite rewriter=ASTRewrite.create(ast);
						   
						   SimpleName newName = ast.newSimpleName("varUsedInLambda");
						   rewriter.replace(name, newName, null);
						   replaceNodeWithAnother(ast,name,newName,cUnit);
					   }
				   }*/
				   System.out.println(name+"8888"+n);
				   if(n.toString().equals(name.toString())) {
					   System.out.println(n+" -----yyyyyy");
					   AST ast = n.getAST();
					   final ASTRewrite rewriter=ASTRewrite.create(ast);
					   SimpleName newName = ast.newSimpleName("varUsedInLambda");
					   rewriter.replace(n, newName, null);
					   replaceNodeWithAnother(ast,n,newName,cUnit);
				   }
				   return true;
			   }
		   };
		 // replaceNodeWithAnother(ast,ifS,newMethodInvocation,cUnit);
		  
		  ((LambdaExpression)newMethodInvocation.arguments().get(0)).getBody().accept(visitor);
	
		   
	   }
	   
	   
	   
	   //replaceNodeWithAnother(ast,ifS,newMethodInvocation,cUnit);

   }
   
   private void replaceNodeWithAnother(AST ast,ASTNode node, ASTNode other, ICompilationUnit cUnit) {

	   final AST ast1 = node.getAST();
	   final ASTRewrite rewriter=ASTRewrite.create(ast1);
		
	   ExpressionStatement newStatement = ast.newExpressionStatement((Expression)other);
	   rewriter.replace(node, newStatement, null);
	   rewriteCUnit(cUnit, rewriter);
	   try {
			cUnit.commitWorkingCopy(true, null);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   }
   
   private MethodInvocation createMethodInvocationWithLambdaParam(AST ast,String methodName,ASTNode body,int param,boolean var) {
	   
	   MethodInvocation newMethodInvocation = ast.newMethodInvocation();
	   ExpressionVisitor exprVisitor = new ExpressionVisitor(ast);
	   if(!var) {
		   if(UtilityClass.checkIfContainsAOptionalMethodCall(methodI, "orElse")) {
			   methodI = (MethodInvocation) methodI.getExpression();
			   System.out.println(methodI+" dupa ce ref");
		   }
		   methodI.accept(exprVisitor);
		   newMethodInvocation.setExpression(exprVisitor.getExpression());
	   }else {
		   newMethodInvocation.setExpression(ast.newSimpleName("varIntroducedByOptionalTool"));
	   }
	   newMethodInvocation.setName(ast.newSimpleName(methodName));
	   LambdaExpression lambda = ast.newLambdaExpression();
  	   
	   if(body instanceof Expression) {
		   body.accept(exprVisitor);
		   body = exprVisitor.getExpression();
	   }else if(body instanceof ExpressionStatement) {
		 ((ExpressionStatement) body).getExpression().accept(exprVisitor);
		  body = exprVisitor.getExpression();
	   }
	   else if(body instanceof Block) {
		   Block b = (Block)body;
		   
		   if(b.statements().size()==1) {
			   Statement s = (Statement) b.statements().get(0);
			   if(s instanceof ExpressionStatement) {
				   ((ExpressionStatement) s).getExpression().accept(exprVisitor);
				   body = exprVisitor.getExpression();
			   }
		   }else {
			   StatementVisitor statementVisitor = new StatementVisitor(ast);
			   body.accept(statementVisitor);
			   body = statementVisitor.getStatement();
		   }
	   }
	   lambda.setBody(body);
	   
	   List<Expression> newArgs = newMethodInvocation.arguments();
	   if(param==1) {
		  // newArgs.add(ast.newSimpleName(ame.toString()));  ??????????Adaugare parametri lambda
		   lambda.setParentheses(false);
		   VariableDeclarationFragment parameter = ast.newVariableDeclarationFragment();
		   parameter.setName(ast.newSimpleName("varUsedInLambda"));
		   
		   //parameter.setType(ast.newPrimitiveType());
		   
		   List params = lambda.parameters();
		
		   params.add(parameter);
		   
	   }
	   newArgs.add(lambda);

	   return newMethodInvocation;
   }
   
   private void createOrElseThrowCall(IfStatement ifS,Expression throwExpr) {
	   AST ast = AST.newAST(AST.JLS8);
	   MethodInvocation newMethod =  createMethodInvocationWithLambdaParam(ast,"orElseThrow",throwExpr,0,false);
	   ICompilationUnit cUnit = UtilityClass.getICompilationUnit(name);
	   //ICompilationUnit cUnit = ((IMember) methodI.resolveMethodBinding().getJavaElement()).getCompilationUnit();
	   replaceNodeWithAnother(ast,ifS,newMethod,cUnit);
   }
   
   private void createOrElseThrowAssign(IfStatement ifS,Expression throwExpr) {
	   AST ast = AST.newAST(AST.JLS8);
	   MethodInvocation statement = createMethodInvocationWithLambdaParam(ast,"orElseThrow",throwExpr,0,true);
	   Assignment assignment = createVariableAssignment(ast,statement);
	  // ICompilationUnit cUnit = ((IMember) methodI.resolveMethodBinding().getJavaElement()).getCompilationUnit();
	   ICompilationUnit cUnit = UtilityClass.getICompilationUnit(name);
			 //  ((IMember) methodI.resolveMethodBinding().getJavaElement()).getCompilationUnit();
	   replaceNodeWithAnother(ast,ifS,assignment,cUnit);
	   
   }
   
   private void createOrElseGetAssign(IfStatement ifS,Statement thenStatement,Expression expr) {
	   AST ast = AST.newAST(AST.JLS8);
	   ICompilationUnit cUnit = UtilityClass.getICompilationUnit(name);
	   //ICompilationUnit cUnit = ((IMember) methodI.resolveMethodBinding().getJavaElement()).getCompilationUnit();
	   //MethodInvocation newMethodInvocation = ast.newMethodInvocation();
	  
	   /*if(expr instanceof MethodInvocation) {
		   newMethodInvocation.setName(ast.newSimpleName("orElseGet"));
		   MethodInvocation methodParam = createMethodInvocationWithLambdaParam(ast,null,"orElseGet",);
	   }else {*/
	   //newMethodInvocation.setName(ast.newSimpleName("orElseGet"));
	  // ExpressionVisitor visitor = new ExpressionVisitor(ast);
	  // expr.accept(visitor);
	  // List<Expression> newArgs = newMethodInvocation.arguments();  
	   MethodInvocation newMethod = createMethodInvocationWithLambdaParam(ast,"orElseGet",expr,0,true);
	   newMethod.setExpression(ast.newSimpleName("varIntroducedByOptionalTool"));
	  // newArgs.add(createMethodInvocationWithLambdaParam(ast,"orElseGet"));
	   Assignment assignment = createVariableAssignment(ast,newMethod);
	   replaceNodeWithAnother(ast,ifS,assignment,cUnit);
   }
   
   private Assignment createVariableAssignment(AST ast,MethodInvocation expression) {
	   
	   Assignment assignment = ast.newAssignment();
	   assignment.setLeftHandSide(ast.newSimpleName(name.toString()));
	   assignment.setOperator(Assignment.Operator.ASSIGN);
	   assignment.setRightHandSide(expression);
	   
	   return assignment;
   }
	
	public void rewriteCUnit(final ICompilationUnit cUnit,ASTRewrite rewriter)
	{
		try
			{
			rewriter.rewriteAST();
			TextEdit textEdit;
			Document document=new Document(cUnit.getSource());
			System.out.println("cUnit: "+ cUnit+" source: "+ cUnit.getSource()+" document "+ document+" --"+ rewriter);
			textEdit=rewriter.rewriteAST(document,cUnit.getJavaProject().getOptions(true));
			textEdit.apply(document);
			String newSource=document.get();
			cUnit.getBuffer().setContents(newSource);
			File file=cUnit.getResource().getLocation().toFile();
			PrintWriter printWriter=new PrintWriter(new BufferedWriter(new FileWriter(file)));
			printWriter.write(newSource);
			printWriter.close();
			cUnit.getResource().refreshLocal(0,null);
			cUnit.close();
			}
		catch (Exception ex)
			{
			ex.printStackTrace();
			}
	}
	
}	


class CheckThatVarIsAssignedBetween extends ASTVisitor {
	
	private SimpleName name;
	private int start, end;
	private boolean isAssigned = false;
	
	public CheckThatVarIsAssignedBetween(SimpleName name, int start, int end) {
		this.name = name;
		this.start = start;
		this.end = end;
	}
	
	public boolean visit(Assignment assignment) {
		Expression var = assignment.getLeftHandSide();
		System.out.println(var+" vari122");
		if(var instanceof SimpleName) {
			System.out.println(var+" vari");
			IBinding binding = ((SimpleName)var).resolveBinding();
			if(binding==null)
				return false;
			if(binding.isEqualTo(name.resolveBinding())) {
				System.out.println(var+" --oo--"+var.getStartPosition());
				int offset = var.getStartPosition();
				if(offset>start && offset<end) {
				isAssigned = true;
				return false;
				}
			}
		}
		return true;
	}
	
	public boolean isAssigned() {
		return isAssigned;
	}	
}




class CheckThatVarIsAssigned extends ASTVisitor {
	
	private SimpleName name;
	private Expression value;
	private boolean isAssigned = false;
	
	public CheckThatVarIsAssigned(SimpleName name) {
		this.name = name;
	}
	
	public boolean visit(Assignment assignment) {
		Expression var = assignment.getLeftHandSide();
		if(var instanceof SimpleName) {
			System.out.println(var+" vari");
			if((((SimpleName)var).resolveBinding().isEqualTo(name.resolveBinding()))) {
				System.out.println(var+" --oo--"+var.getStartPosition());
				value = assignment.getRightHandSide();
				isAssigned = true;
				return false;
			}
		}
		return true;
	}
	
	public Expression getValue() {
		return value;
	}
	public boolean isAssigned() {
		return isAssigned;
	}	
}

class CheckVariableFinalOrEffectivelyFinalVisitor extends ASTVisitor{
	
	
	private List<IVariableBinding> nonFinalOrEffectivelyFinalVariables = new ArrayList<>();
	private List<IVariableBinding> variablesDecl;
	private boolean containsAssignmentForVarsDeclaredOutside;
	
	public CheckVariableFinalOrEffectivelyFinalVisitor(List<IVariableBinding> variablesDecl) {
		this.variablesDecl = variablesDecl;
	}
	
	public CheckVariableFinalOrEffectivelyFinalVisitor() {
		this.variablesDecl = new ArrayList<>();
	}
	
	public boolean visit(SimpleName name) {
		IBinding binding = name.resolveBinding();
		
		if(binding instanceof IVariableBinding) {
			IVariableBinding varBinding  = (IVariableBinding)binding;
			System.out.println(variablesDecl +" "+ variablesDecl.contains(varBinding)+" "+name);
			if (varBinding != null && !varBinding.isField() && !variablesDecl.contains(varBinding)) { 
				if(!varBinding.isEffectivelyFinal() && varBinding.getModifiers()!=Modifier.FINAL) {
					System.out.println(variablesDecl +"is effectively "+ varBinding.isEffectivelyFinal()+" "+name);
					nonFinalOrEffectivelyFinalVariables.add(varBinding);
				}
			}
		}
		
		return true;
	}
	
	public boolean visit(Assignment assignment) {
		Expression var = assignment.getLeftHandSide();
		if(var instanceof SimpleName) {
			
			if(!variablesDecl.contains(((SimpleName)var).resolveBinding()))
				containsAssignmentForVarsDeclaredOutside=true;
				return true;
			}
		return true;
	}
	
	public boolean containsAssignmentForVarsDeclaredOutside() {
		return containsAssignmentForVarsDeclaredOutside;
	}
	
	public boolean areAllVarsFinalOrEffectivelyFinal() {
		return nonFinalOrEffectivelyFinalVariables.isEmpty();
	}
}

class CheckIfThereIsAReturnVisitor extends ASTVisitor{

	private boolean existReturn;
	
	public boolean visit(ReturnStatement returnS) {
		existReturn = true;
		return false;
	}
	
	public boolean existReturn() {
		return existReturn;
	}
}

class GetVariableDeclarationVisitor extends ASTVisitor{
	
	private List<IVariableBinding> variables = new ArrayList<>();
	
	public boolean visit(VariableDeclarationFragment variableDeclaration) {
		variables.add(variableDeclaration.resolveBinding());
		return true;
	}
	
	public List<IVariableBinding> getVariables(){
		return variables;
	}
}

