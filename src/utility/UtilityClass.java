package utility;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;

public class UtilityClass {

	private static Statement thenBlock;
	private static Statement elseBlock;
	
	public static MethodInvocation getMethodInvocation(ASTNode node) {
		ASTNode parent = node.getParent();
		if(parent != null) {
			if(parent instanceof MethodInvocation) {
				return (MethodInvocation) parent;
			}
			if(parent instanceof MethodDeclaration) {
				return null;
			}
		}
		return getMethodInvocation(parent);
	}
     public static ICompilationUnit getICompilationUnit(ASTNode node) {
		ASTNode parent = node.getParent();
		if(parent != null) {
			if(parent instanceof MethodDeclaration && ((MethodDeclaration) parent).resolveBinding()!=null) {
				IMethodBinding mb = ((MethodDeclaration) parent).resolveBinding();
				return ((IMember)mb.getJavaElement()).getCompilationUnit();
			}
			if(parent instanceof TypeDeclaration) {
				ITypeBinding type = ((TypeDeclaration)parent).resolveBinding();
				return ((IMember) type.getJavaElement()).getCompilationUnit();
			}
		}
		return getICompilationUnit(parent);
	}
     
    /* public static IType getNullCheckLocationClass(ASTNode node) {
    	 return (IType)UtilityClass.getNullCheckLocationMethod(node).getDeclaringClass().getJavaElement();
     }*/
     
 	public static boolean checkIfContainsAOptionalMethodCall(MethodInvocation method, String name) {
		if(method!=null) {
			System.out.println("Method ::::"+method);
	    	IMethodBinding methodBinding = method.resolveMethodBinding();
	    	System.out.println("Method2 ::::"+methodBinding);
	    	//if(methodBinding != null) {
	    		if(method.getName().toString().equals(name)) {
	    			System.out.println("true"+method);
	    			return true;
	    		}
	    	//}	
		}
	    return false;
	}
     
     public static IMethodBinding getNullCheckLocationMethod(ASTNode node) {
 		ASTNode p = node.getParent();
 		if(p != null && p instanceof MethodDeclaration) {
 			return  ((MethodDeclaration) p).resolveBinding();
 		}
 			return getNullCheckLocationMethod(p);
 	}
     
    public static IType getCheckedElementType(ASTNode node) {
    	ITypeBinding t = null;
 		if(node instanceof SimpleName) {
 			IVariableBinding var = (IVariableBinding) ((SimpleName)node).resolveBinding();
 			t = var.getType();
 		}else if(node instanceof MethodInvocation) {
 			IMethodBinding checkedMethodBinding=((MethodInvocation) node).resolveMethodBinding();
 			t = checkedMethodBinding.getReturnType();
 		}
 		
 		return (IType) t.getJavaElement();
 	}
   
    public static IfStatement getIfStatement(ASTNode node) {
    	ASTNode parent = node.getParent();
    	if(parent !=null && parent instanceof IfStatement) {
    		return (IfStatement) parent;
    	}
    	return getIfStatement(parent);
    }
     
 	public static Statement getThenBlock(ASTNode node) {
 		if(thenBlock==null) {
		IfStatement st = UtilityClass.getIfStatement(node);
		thenBlock = (Statement) st.getThenStatement();
		}
 		return thenBlock;
	}

	public static Statement getElseBlock(ASTNode node) {
		if(elseBlock==null) {
		IfStatement st = UtilityClass.getIfStatement(node);
		elseBlock = (Statement) st.getElseStatement();}
		
		return elseBlock;
	}
	
	public static InfixExpression.Operator getOperator(ASTNode node) {
		InfixExpression ex = UtilityClass.getInfixExpression(node);
		return ex.getOperator();
	}
	private static InfixExpression getInfixExpression(ASTNode node) {
		ASTNode parent = node.getParent();
    	if(parent !=null && parent instanceof InfixExpression) {
    		return (InfixExpression) parent;
    	}
    	return getInfixExpression(parent);
	}
/*	
	public static int getConditionLine(ASTNode node) {
		InfixExpression ex = UtilityClass.getInfixExpression(node);
		//CompilationUnit comp = (CompilationUnit) UtilityClass.getICompilationUnit(node);
		CompilationUnit comp = UtilityClass.getCompilationUnit(node);
		if(conditionLine==-1) {
			originalConditionLine =  comp.getLineNumber(node.getStartPosition());
			conditionLine = comp.getLineNumber(node.getStartPosition());}
		if (originalConditionLine ==-1) {
			originalConditionLine =  comp.getLineNumber(node.getStartPosition());
		}
		return conditionLine;
	}*/
	
	public static  CompilationUnit getCompilationUnit(ASTNode node) {
		ASTParser parser=ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(getICompilationUnit(node));
		parser.setResolveBindings(true);
		return (CompilationUnit)parser.createAST(null);
	}
/*	
	public static void setConditionLine(int newConditionLine) {
		conditionLine = newConditionLine;
	}

	public static int getOriginalConditionLine(ASTNode node) {
		if(originalConditionLine==-1) {
			getConditionLine(node);
		}
		return originalConditionLine;
	}
	
/*	public static void setThenBlock(Statement newThenBlock) {
		thenBlock = newThenBlock;
	}

	public static void setElseBlock(Statement newElseBlock) {
		elseBlock = newElseBlock;
	}
	*/
	public static ASTNode getAssignment(ASTNode expr) {
		if(expr instanceof Assignment) {
			return expr;
		}
		if(expr instanceof TypeDeclaration) {
			return null;
		}
		return getAssignment(expr.getParent());
	}
	
	public static ASTNode getVariableDeclaration(ASTNode expr) {
		if(expr instanceof VariableDeclaration) {
			return expr;
		}
		if(expr instanceof TypeDeclaration) {
			return null;
		}
		return getVariableDeclaration(expr.getParent());
	}
}
