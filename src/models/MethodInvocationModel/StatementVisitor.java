package models.MethodInvocationModel;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class StatementVisitor extends ASTVisitor{

	private Statement statement;
	private AST ast;
	
	public StatementVisitor(AST ast) {
		this.ast = ast;
	}
	
	public Statement getStatement() {
		return statement;
	}
	
	public boolean visit(Block block) {
		Block newBlock = ast.newBlock();
		StatementVisitor visitor = new StatementVisitor(ast);
		Statement st = null;
		
		List<Statement> newStatements = newBlock.statements();
		List<Statement> blockStatements = block.statements();
		
		for(Statement s: blockStatements) {
			s.accept(visitor);
			st = visitor.getStatement();
			newStatements.add(st);
		}
		
		statement = newBlock;
		return false;
	}
	
	public boolean visit(ExpressionStatement exprS) {
		
		ExpressionVisitor exprVisitor = new ExpressionVisitor(ast);
		exprS.getExpression().accept(exprVisitor);
		ExpressionStatement newExprS = ast.newExpressionStatement(exprVisitor.getExpression());
		
		statement = newExprS;
		return false;
	}
	
	public boolean visit(ThrowStatement throwS) {
		
		ThrowStatement newThrowS = ast.newThrowStatement();
		
		ExpressionVisitor exprVisitor = new ExpressionVisitor(ast);
		throwS.getExpression().accept(exprVisitor);
		
		newThrowS.setExpression(exprVisitor.getExpression());
		
		statement = newThrowS;
		return false;
	}
	
	public boolean visit(IfStatement ifS) {
		
		IfStatement newIfS = ast.newIfStatement();
		
		ExpressionVisitor exprVisitor = new ExpressionVisitor(ast);
		ifS.getExpression().accept(exprVisitor);
		newIfS.setExpression(exprVisitor.getExpression());
		
		StatementVisitor visitor = new StatementVisitor(ast);
		ifS.getThenStatement().accept(visitor);
		newIfS.setThenStatement(visitor.getStatement());
		
		ifS.getElseStatement().accept(visitor);
		newIfS.setElseStatement(visitor.getStatement());
		
		
		statement = newIfS;
		return false;
	}
	
	private VariableDeclarationFragment createVarDeclFragment(VariableDeclarationFragment varDecl) {
		
		VariableDeclarationFragment newVarDecl = null;
		ExpressionVisitor exprVisitor = new ExpressionVisitor(ast);
	
		newVarDecl = ast.newVariableDeclarationFragment();
		
		varDecl.getName().accept(exprVisitor);
		newVarDecl.setName((SimpleName) exprVisitor.getExpression());
		Expression initializer = varDecl.getInitializer();
		if(initializer!=null) {
			initializer.accept(exprVisitor);
			newVarDecl.setInitializer(exprVisitor.getExpression());
		}
		return newVarDecl;
	}
	
	public boolean visit(VariableDeclarationStatement varDeclS) {
		
		List<VariableDeclarationFragment> fragments = varDeclS.fragments();
		VariableDeclarationFragment fragm = createVarDeclFragment(fragments.get(0));
		
		VariableDeclarationStatement newVarDeclS = ast.newVariableDeclarationStatement(fragm);
		
		TypeVisitor typeVisitor = new TypeVisitor(ast);
		varDeclS.getType().accept(typeVisitor);
		newVarDeclS.setType(typeVisitor.getType());
		
		List<VariableDeclarationFragment> newFragments = newVarDeclS.fragments();
		
		for(int i=1;i<fragments.size();i++) {
			newFragments.add(createVarDeclFragment(fragments.get(i)));
		}
		
		statement = newVarDeclS;
		return false;
	}
}
