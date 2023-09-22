package models.CompUnitModel;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import nullterminator.metamodel.entity.CompUnitModel;
import ro.lrg.xcore.metametamodel.IPropertyComputer;
import ro.lrg.xcore.metametamodel.PropertyComputer;

/**
 * syntax tree
 */

@PropertyComputer
public class Print implements IPropertyComputer<String, CompUnitModel>{

	@Override
	public String compute(CompUnitModel arg0) {

		
		ICompilationUnit unit = (ICompilationUnit) arg0.getUnderlyingObject();
		ASTParser parser = ASTParser.newParser(AST.JLS2);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		//CompilationUnit ast = (CompilationUnit)parser.createAST(null);
		ASTNode ast =parser.createAST(null);
		ASTPrinter printer = new ASTPrinter();
		ast.accept(printer);
		
		ASTVisitor v = new ASTVisitor() {
			
			public boolean visit(SingleVariableDeclaration d) {
				System.out.println("VariableDecl :::::::::::"+d+" "+d.getType());
				return true;
			}
			
			public boolean visit(LambdaExpression d) {
				System.out.println("VariableDecl :::::::::::"+d+" ");
				List par = d.parameters();
				System.out.println(par);
				System.out.println(par.get(0)+" 0000 "+par.get(0).getClass().getName());
				return true;
			}
		};
		ast.accept(v);
		//MessageDialog.openInformation(shell, "AST for: " + unit.getElementName(), printer.buffer.toString());
		return "hello";

	}
	
	 class ASTPrinter extends ASTVisitor {
	    //  StringBuffer buffer = new StringBuffer();
	      public void preVisit(ASTNode node) {
	         //write the name of the node being visited
	         printDepth(node);
	         String name = node.getClass().getName()+" : "+node.toString();
	         name = name.substring(name.lastIndexOf('.')+1);
	         name = name.replaceAll("\n", "   ");
	         System.out.print(name);
	        // buffer.append(name);
	         System.out.print(" {\r\n");
	        // buffer.append(" {\r\n");
	      }
	      public void postVisit(ASTNode node) {
	         //write a closing brace to indicate end of the node
	         printDepth(node);
	         System.out.print("}\r\n");
	      }
	      void printDepth(ASTNode node) {
	         //indent the current line to an appropriate depth
	         while (node != null) {
	            node = node.getParent();
	            System.out.print("  ");
	         }
	      }
	   }
}
