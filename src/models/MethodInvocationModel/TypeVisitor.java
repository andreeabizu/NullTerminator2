package models.MethodInvocationModel;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;

public class TypeVisitor extends ASTVisitor{
	
	private org.eclipse.jdt.core.dom.Type type;
	private AST ast;
	
	public TypeVisitor(AST ast) {
		this.ast = ast;
	}
	
	public org.eclipse.jdt.core.dom.Type getType() {
		return type;
	}

	public boolean visit(SimpleType simpleType) {
		type = ast.newSimpleType(ast.newSimpleName(simpleType.toString()));
		return false;
	}
	
	public boolean visit(ArrayType arrayType) {
		TypeVisitor t = new TypeVisitor(ast);
		arrayType.getElementType().accept(t);
		type = ast.newArrayType(t.getType(), arrayType.getDimensions());
		return false;
	}
	
	public boolean visit(QualifiedType qType) {
		TypeVisitor v = new TypeVisitor(ast);
		qType.getQualifier().accept(v);
		type = ast.newQualifiedType(v.getType(), ast.newSimpleName(qType.getName().getIdentifier()));
		return false;
	}
	
	public boolean visit(ParameterizedType pType) {
		TypeVisitor v = new TypeVisitor(ast);
		pType.getType().accept(v);
		type = ast.newParameterizedType(v.getType());
		List<org.eclipse.jdt.core.dom.Type> newArgs = ((ParameterizedType) type).typeArguments();
		List<org.eclipse.jdt.core.dom.Type> args = pType.typeArguments();

		
		for(org.eclipse.jdt.core.dom.Type t:args) {
			t.accept(v);
			newArgs.add(v.getType());
		}
		return false;
	}
}
