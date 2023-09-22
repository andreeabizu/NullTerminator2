package models.MethodInvocationModel;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
//am modificat peste tot cu return false ->trb testat
public class ExpressionVisitor extends ASTVisitor{
	
	private Expression expr;
	private AST ast;
	
	public ExpressionVisitor(AST ast) {
		this.ast = ast;
	}
	
	public Expression getExpression() {
		return expr;
	}
	
	public boolean visit(SimpleName simpleName) {
		expr = ast.newSimpleName(simpleName.getIdentifier());
		return false;
	}
	
	public boolean visit(NumberLiteral numberLiteral) {
		NumberLiteral nl = ast.newNumberLiteral();
		nl.setToken(numberLiteral.getToken());
		expr = nl;
		return false;
	}
	
	
	
	public boolean visit(StringLiteral stringLiteral) {
		StringLiteral sl = ast.newStringLiteral();
		sl.setLiteralValue(stringLiteral.getLiteralValue());
		expr = sl;
		return false;
	}
	
	public boolean visit(BooleanLiteral booleanLiteral) {
		BooleanLiteral bl = ast.newBooleanLiteral(booleanLiteral.booleanValue());
		expr = bl;
		return false;
	}
	
	public boolean visit(NullLiteral nullLiteral) {
		expr = ast.newNullLiteral();
		return false;
	}
	
	public boolean visit(CharacterLiteral chLiteral) {
		CharacterLiteral cl = ast.newCharacterLiteral();
		cl.setCharValue(chLiteral.charValue());
		expr = cl;
		return false;
	}
	
	public boolean visit(QualifiedName qname) {
		ExpressionVisitor v = new ExpressionVisitor(ast);
		qname.getQualifier().accept(v);
		Name exp = (Name) v.getExpression();
		qname.getName().accept(v);
		SimpleName sn = (SimpleName) v.getExpression();
		QualifiedName qn = ast.newQualifiedName(exp,sn); 
		expr = qn;
		return false;
	}
	
	public boolean visit(ThisExpression thisE) {
		ThisExpression th = ast.newThisExpression();
		ExpressionVisitor v = new ExpressionVisitor(ast);
		if(thisE.getQualifier()!=null) {
		thisE.getQualifier().accept(v);
		th.setQualifier((Name) v.getExpression());}
		expr = th;
		return false;
	}
	
/*	public boolean visit(InfixExpression infixExpression) {
		InfixExpression newExpr = ast.newInfixExpression();
		System.out.println("OOOOOOOOO "+infixExpression.getOperator().toString());
		//InfixExpression.Operator op = InfixExpression.Operator.toOperator(InfixExpression.Operator.EQUALS);
		if(infixExpression.getOperator().toString().equals("==")) {
			newExpr.setOperator(InfixExpression.Operator.NOT_EQUALS);
		}else if(infixExpression.getOperator().toString().equals("!=")) {
			newExpr.setOperator(InfixExpression.Operator.EQUALS);
		}
		System.out.println("OOOOOOOOO5 "+InfixExpression.Operator.EQUALS);
		//newExpr.setOperator(InfixExpression.Operator.NOT_EQUALS);
		ExpressionVisitor v = new ExpressionVisitor(ast);
		if(infixExpression.getRightOperand() instanceof NullLiteral) {
			newExpr.setRightOperand(ast.newBooleanLiteral(true));
			Expression leftOperand = infixExpression.getLeftOperand();
			leftOperand.accept(v);
		}else if(infixExpression.getLeftOperand() instanceof NullLiteral) {
			newExpr.setLeftOperand(ast.newBooleanLiteral(true));
			Expression rightOperand = infixExpression.getLeftOperand();
			rightOperand.accept(v);
		}
		
		MethodInvocation newInvocation = ast.newMethodInvocation();
		newInvocation.setName(ast.newSimpleName("isPresent"));
		newInvocation.setExpression(v.getExpression());
		newExpr.setLeftOperand(newInvocation);
		//System.out.println("OPEE$$ "+newExpr.getOperator()+" "+newExpr.getRightOperand()+" "+newExpr.getLeftOperand() );
		expr = newExpr;
		return false;
	}
	
	*/
	public boolean visit(CastExpression castE) {
		CastExpression newCastE = ast.newCastExpression();
		ExpressionVisitor exprVisitor = new ExpressionVisitor(ast);
		TypeVisitor typeVisitor = new TypeVisitor(ast);
		castE.getExpression().accept(exprVisitor);
		castE.getType().accept(typeVisitor);
		newCastE.setExpression(exprVisitor.getExpression());
		newCastE.setType(typeVisitor.getType());
		expr = newCastE;
		return false;
	}

	
	public boolean visit(ParenthesizedExpression parenthesizedE) {
		ParenthesizedExpression newParenthesizedE = ast.newParenthesizedExpression();
		ExpressionVisitor exprVisitor = new ExpressionVisitor(ast);
		parenthesizedE.getExpression().accept(exprVisitor);
		newParenthesizedE.setExpression(exprVisitor.getExpression());
		expr = newParenthesizedE;
		return false;
	}
	
	public boolean visit(MethodInvocation methodI) {
		
		ExpressionVisitor v = new ExpressionVisitor(ast);
		Expression exp;
		MethodInvocation m = ast.newMethodInvocation();
		m.setName(ast.newSimpleName(methodI.getName().toString()));
	
		if(methodI.getExpression()!=null) {
			methodI.getExpression().accept(v);
			exp = v.getExpression();
			m.setExpression(exp);
		}

		List<Expression> newArgs = m.arguments();
		List<Expression> args = methodI.arguments();

		
		for(Expression e:args) {
			e.accept(v);
			exp = v.getExpression();
			//System.out.println(exp+" "+exp.getClass());
			newArgs.add(exp);
			//System.out.println(exp+" "+exp.getClass());
		}
		
		expr = m;
		return false;
	}
	
	public boolean visit(ClassInstanceCreation instance) {
		ClassInstanceCreation newInstance = ast.newClassInstanceCreation();
		ExpressionVisitor v = new ExpressionVisitor(ast);
		TypeVisitor typeV =new TypeVisitor(ast);
		instance.getType().accept(typeV);
		newInstance.setType(typeV.getType());
		
		List<Expression> newArgs = newInstance.arguments();
		List<Expression> args = instance.arguments();
		
		for(Expression e:args) {
			e.accept(v);
			newArgs.add(v.getExpression());
		}
		
		expr = newInstance;
		return false;
	}

	
	public boolean visit(SuperMethodInvocation superMethod) {
		
		SuperMethodInvocation sm = ast.newSuperMethodInvocation();
		ExpressionVisitor v = new ExpressionVisitor(ast);
		Expression exp;
		sm.setName(ast.newSimpleName(superMethod.getName().toString()));
		if(superMethod.getQualifier()!=null) {
		superMethod.getQualifier().accept(v);
		sm.setQualifier((Name) v.getExpression());
		}
		List<Expression> newArgs = sm.arguments();
		List<Expression> args = superMethod.arguments();
		
		for(Expression e:args) {
			e.accept(v);
			exp = v.getExpression();
			newArgs.add(exp);
		}
		
		expr = sm;
		return false;

	}
	
	public boolean visit(Assignment assignment) {
		
		Assignment newAssign = ast.newAssignment();
		ExpressionVisitor v = new ExpressionVisitor(ast);
		assignment.getLeftHandSide().accept(v);
		newAssign.setLeftHandSide(v.getExpression());
		
		assignment.getRightHandSide().accept(v);
		newAssign.setRightHandSide(v.getExpression());
		newAssign.setOperator(assignment.getOperator());
		
		expr = newAssign;
		return false;
	}
	
	public boolean visit(FieldAccess fieldAccess) {
		
		FieldAccess newFieldAccess = ast.newFieldAccess();
		ExpressionVisitor v = new ExpressionVisitor(ast);
		
		fieldAccess.getName().accept(v);
		newFieldAccess.setName((SimpleName) v.getExpression());
		
		fieldAccess.getExpression().accept(v);
		newFieldAccess.setExpression(v.getExpression());
		
		expr = newFieldAccess;
		return false;
	}
	
	
	public boolean visit(InfixExpression infixExpr) {
		
		InfixExpression newInfixExpr = ast.newInfixExpression();
		
		ExpressionVisitor v = new ExpressionVisitor(ast);
		infixExpr.getLeftOperand().accept(v);
		newInfixExpr.setLeftOperand(v.getExpression());
		
		infixExpr.getRightOperand().accept(v);
		newInfixExpr.setRightOperand(v.getExpression());
		newInfixExpr.setOperator(infixExpr.getOperator());
		
		expr = newInfixExpr;
		return false;
	}
	
	
	public boolean visit(SuperFieldAccess superFieldAccess) {
		
		SuperFieldAccess newSuperFieldAccess = ast.newSuperFieldAccess();
		ExpressionVisitor v = new ExpressionVisitor(ast);
		
		superFieldAccess.getName().accept(v);
		newSuperFieldAccess.setName((SimpleName) v.getExpression());
		
		superFieldAccess.getQualifier().accept(v);
		newSuperFieldAccess.setQualifier((Name) v.getExpression());
		
		expr = newSuperFieldAccess;
		return false;
	}
}
