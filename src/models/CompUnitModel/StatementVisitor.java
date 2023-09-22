package models.CompUnitModel;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;

public class StatementVisitor  extends ASTVisitor {

	private AST ast;
	private Statement statement;
	
	public StatementVisitor(AST ast) {
		this.ast = ast;
	}
	
	
	public Statement getStatemnt() {
		return statement;
	}
	
	public boolean visit(Block block) {
		statement = ast.newBlock();
		return true;
	}
	
}