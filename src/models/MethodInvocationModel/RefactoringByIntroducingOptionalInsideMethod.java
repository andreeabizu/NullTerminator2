package models.MethodInvocationModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import nullterminator.metamodel.entity.CheckedElementModel;
import nullterminator.metamodel.entity.CompUnitModel;
import nullterminator.metamodel.entity.MethodInvocationModel;
import nullterminator.metamodel.factory.Factory;
import ro.lrg.xcore.metametamodel.ActionPerformer;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.HListEmpty;
import ro.lrg.xcore.metametamodel.IActionPerformer;
import utility.MethodsCollector;
import utility.UtilityClass;


@ActionPerformer
public class RefactoringByIntroducingOptionalInsideMethod implements IActionPerformer<Void, MethodInvocationModel, HListEmpty> {

	private IProgressMonitor monitor = new NullProgressMonitor();
	private MethodDeclaration newMethodDeclaration;
	private VariableDeclarationStatement variableDeclarationStatement;
	private Assignment assignment;
	private List<CheckedElementModel> checkedElementModel = new ArrayList<CheckedElementModel>(); 
	private Group<CheckedElementModel> checkModel ;
	
	private ICompilationUnit icomp;
	@Override
	public Void performAction(MethodInvocationModel arg0, HListEmpty arg1) {
		
		Expression m = (Expression)arg0.getUnderlyingObject();
		IMethodBinding methodBinding = null;
		icomp = UtilityClass.getICompilationUnit(m);
		if(m instanceof MethodInvocation) {
			methodBinding = ((MethodInvocation) m).resolveMethodBinding();
		}else if(m instanceof SuperMethodInvocation) {
			methodBinding = ((SuperMethodInvocation) m).resolveMethodBinding();
		}
		
		try {
			findMethodDeclaration(methodBinding);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}  
		
		/*for(Expression expr: variableRefactorList) {
			refactorVariable(expr);
		}*/
		refactorVariable(m);
	
		return null;
		
	}
	

	public void refactorMethodInvocation(Expression m, ASTRewrite rewriter,AST ast) {
			/***IfStatement ifStatement = getIfStatementParent(m);
			
			if(ifStatement!=null && canBeRefactored(ifStatement)) {
				if(ifStatement.getExpression() instanceof InfixExpression) {
					InfixExpression expression = (InfixExpression) ifStatement.getExpression();
					InfixExpression newExpr = null;
					//ExpressionVisitor v = new ExpressionVisitor(ast);
					//expression.accept(v);
					//newExpr=(InfixExpression) v.getExpression();
					newExpr = (InfixExpression) createNewInfixExpression(expression, ast);
					rewriter.replace(expression, newExpr, null);
				}
			}else {***/
		  
		 /*   MethodInvocation parentMethodInvocation = UtilityClass.getMethodInvocation(m);
		    if(parentMethodInvocation!=null) {
		    	IMethodBinding methodBinding = parentMethodInvocation.resolveMethodBinding();
		    	if(methodBinding == null) {
		    	if(parentMethodInvocation.getName().toString().equals("orElseThrow"))
		    		return;
		    	}
		    }*/
		
			MethodInvocation parentMethodInvocation = UtilityClass.getMethodInvocation(m);
			 if(parentMethodInvocation!=null) {
				 if(UtilityClass.checkIfContainsAOptionalMethodCall(parentMethodInvocation,"orElseThrow") || UtilityClass.checkIfContainsAOptionalMethodCall(parentMethodInvocation,"ifPresent") ) {
					return;
				}
			}
		
			MethodInvocation newMeth = ast.newMethodInvocation();
			newMeth.setName(ast.newSimpleName("orElse"));
			List<Expression> newArgs = newMeth.arguments();
			newArgs.add(ast.newNullLiteral());
			ExpressionVisitor v = new ExpressionVisitor(ast);
			m.accept(v);
			newMeth.setExpression(v.getExpression());
			rewriter.replace(m, newMeth, null);
			
			/*Assignment assignment = (Assignment) UtilityClass.getAssignment(m);
			VariableDeclaration variableDecl = (VariableDeclaration) UtilityClass.getVariableDeclaration(m);
			if(assignment!=null || variableDecl!=null) {
				variableRefactorList.add(m);
				//refactorVariable(m);
			}*/
			//}
	}
	

	
	public IfStatement getIfStatementParent(ASTNode node) {
		if(node instanceof InfixExpression && node.getParent() instanceof IfStatement) {
			return (IfStatement) node.getParent();
		}
		if(node instanceof TypeDeclaration) {
			return null;
		}
		return getIfStatementParent(node.getParent());
	}
	
	private boolean canBeRefactored(IfStatement node) 
	{
		if (node.getExpression().toString().contains("&&") || node.getExpression().toString().contains("||"))
			return false;
		return true;
	}
	
	public Expression createNewInfixExpression(InfixExpression infixExpression, AST ast) {
		InfixExpression newExpr = ast.newInfixExpression();
		if(infixExpression.getOperator().toString().equals("==")) {
			newExpr.setOperator(InfixExpression.Operator.NOT_EQUALS);
		}else if(infixExpression.getOperator().toString().equals("!=")) {
			newExpr.setOperator(InfixExpression.Operator.EQUALS);
		}

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

		return newExpr;
	}


	public void changeReturnExpression(MethodDeclaration methDecl) {
		final AST ast=methDecl.getParent().getAST();
		final ASTRewrite rewriter=ASTRewrite.create(ast);
		
		methDecl.accept(new ASTVisitor() {
			
			public boolean visit(ReturnStatement returnStatement) {
				Expression returnExpression = returnStatement.getExpression();
				MethodInvocation newInvocation = ast.newMethodInvocation();
				QualifiedName packageName = ast.newQualifiedName(ast.newSimpleName("java"), ast.newSimpleName("util"));
				QualifiedName optionalName = ast.newQualifiedName(packageName, ast.newSimpleName("Optional"));
				if(returnExpression instanceof NullLiteral) {
					newInvocation.setName(ast.newSimpleName("empty"));
					newInvocation.setExpression(optionalName);//ast.newSimpleName("Optional"));
				}
				else {	
				/*if(returnExpression instanceof SuperMethodInvocation) {
					
					SuperMethodInvocation superMeth = (SuperMethodInvocation)returnExpression; 
					boolean k1 = superMeth.resolveMethodBinding().toString().equals(methDecl.resolveBinding().toString());
					boolean k2 = superMeth.resolveMethodBinding().getReturnType().getName().startsWith("Optional<");
					if(k1 || k2)
						return true;
					
				}/*else if(returnExpression instanceof MethodInvocation) {
					MethodInvocation method = (MethodInvocation)returnExpression;
					boolean k1 =  method.resolveMethodBinding().toString().equals(methDecl.resolveBinding().toString());
					boolean k2 =  method.resolveMethodBinding().getReturnType().getName().startsWith("Optional<");
					if(k1 || k2)
						return true;
				}*/
				newInvocation.setName(ast.newSimpleName("ofNullable"));
				newInvocation.setExpression(optionalName);//ast.newSimpleName("Optional"));
				List arg = newInvocation.arguments();
				Expression expr;
				ExpressionVisitor v = new ExpressionVisitor(ast);
				returnExpression.accept(v);
				expr = v.getExpression();
				arg.add(expr);
				
				//rewriteCUnit(((IMethod) methDecl.resolveBinding().getJavaElement()).getCompilationUnit(), rewriter);
				}
				rewriter.replace(returnStatement.getExpression(), newInvocation, null);
				return true;
				}
			});
		rewriteCUnit(((IMethod) methDecl.resolveBinding().getJavaElement()).getCompilationUnit(), rewriter);
		/*try {
			((IMethod) methDecl.resolveBinding().getJavaElement()).getCompilationUnit().commitWorkingCopy(true, null);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} am comentat*/
		
	}
	
	
	public void changeReturnType(MethodDeclaration methDecl) {
		
		final AST ast=methDecl.getParent().getAST();
		final ASTRewrite rewriter=ASTRewrite.create(ast);
		org.eclipse.jdt.core.dom.Type t = methDecl.getReturnType2();
		QualifiedName packageName = ast.newQualifiedName(ast.newSimpleName("java"), ast.newSimpleName("util"));
		QualifiedName optionalName = ast.newQualifiedName(packageName, ast.newSimpleName("Optional"));
		ParameterizedType newReturnType = ast.newParameterizedType(ast.newSimpleType(optionalName));
		TypeVisitor v = new TypeVisitor(ast);
		t.accept(v);
		org.eclipse.jdt.core.dom.Type type = v.getType();
		List arg = newReturnType.typeArguments();
		arg.add(type);
		rewriter.replace(t, newReturnType, null);
		rewriteCUnit(((IMethod) methDecl.resolveBinding().getJavaElement()).getCompilationUnit(), rewriter);//}
	
		/*try {
			((IMethod) methDecl.resolveBinding().getJavaElement()).getCompilationUnit().commitWorkingCopy(true, null);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} am comentat
		*/
	}

	

	
	public void rewriteCUnit(final ICompilationUnit cUnit,ASTRewrite rewriter) 
	{
		try
			{
			System.out.println("aici inainte de eroare"+ rewriter.getAST()+" --"+rewriter);
			try{
				rewriter.rewriteAST();
			}catch(IllegalArgumentException e) {
			
				e.printStackTrace();
			}
			System.out.println("yyyyyyy uuiud");
			TextEdit textEdit;
			Document document=new Document(cUnit.getSource());
			System.out.println("document doc" + document );
			System.out.println("document cunti" + cUnit );
			textEdit=rewriter.rewriteAST(document,cUnit.getJavaProject().getOptions(true));
			System.out.println("text edit"+textEdit);
			textEdit.apply(document);
			System.out.println("text edit2");
			String newSource=document.get();
			System.out.println("text edit source"+newSource);
			cUnit.getBuffer().setContents(newSource);
			System.out.println("text edit set cont");
			File file=cUnit.getResource().getLocation().toFile();
			System.out.println("fille "+file);
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

	
	public void getMethodInvocation(MethodDeclaration meth) throws JavaModelException {
		IMethodBinding m = meth.resolveBinding();
		IJavaElement je = m.getJavaElement();
		IJavaProject javaProject = je.getJavaProject();
		int i;
		IPackageFragmentRoot[] packages=javaProject.getPackageFragmentRoots();
		for (IPackageFragmentRoot iPackageFragmentRoot:packages)
			if (!(iPackageFragmentRoot.isExternal()))
				{
				IJavaElement[] javaElements=iPackageFragmentRoot.getChildren();
				for (i=0;i<javaElements.length;i++)
					{
					IPackageFragment packageFragment=(IPackageFragment)javaElements[i];				
					ICompilationUnit[] compilationUnits = packageFragment.getCompilationUnits();
					  for (ICompilationUnit compilationUnit:compilationUnits)	
						{
							
						ASTParser parser=ASTParser.newParser(AST.JLS4);
						parser.setKind(ASTParser.K_COMPILATION_UNIT);
						parser.setSource(compilationUnit);
						parser.setResolveBindings(true);
						CompilationUnit cUnit = (CompilationUnit)parser.createAST(null);
						
						final AST ast= cUnit.getAST();
						final ASTRewrite rewriter=ASTRewrite.create(ast);
						cUnit.accept(new ASTVisitor(){
							public boolean visit (MethodInvocation methodInvocation)
							{
								if (methodInvocation.resolveMethodBinding() != null)
								{
								 
									if (methodInvocation.resolveMethodBinding().isEqualTo(meth.resolveBinding()))
									{
										CompUnitModel compUnit = Factory.getInstance().createCompUnitModel(UtilityClass.getICompilationUnit(methodInvocation));
									
										System.out.println("************ Found invocation: " + methodInvocation.toString()+" in file "+compUnit.toString()+ " in "+compUnit.getPackage());
										refactorMethodInvocation(methodInvocation,rewriter,ast);
									}
								}
								return true;
							}
							
							public boolean visit (SuperMethodInvocation methodInvocation)
							{
								if (methodInvocation.resolveMethodBinding() != null)
								{
								 
									if (methodInvocation.resolveMethodBinding().isEqualTo(meth.resolveBinding()))
									{
										CompUnitModel compUnit = Factory.getInstance().createCompUnitModel(UtilityClass.getICompilationUnit(methodInvocation));
									
										System.out.println("************ Found invocation: " + methodInvocation.toString()+" in file "+compUnit.toString()+ " in "+compUnit.getPackage());
										refactorMethodInvocation(methodInvocation,rewriter,ast);
									}
								}
								return true;
							}
							
						});
						rewriteCUnit(compilationUnit, rewriter);
						/*try {
							compilationUnit.commitWorkingCopy(true, null);
						} catch (JavaModelException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}am comentat*/
						} 
		            }
				}
		}
	




	public void findMethodDeclaration(IMethodBinding methodBinding) throws JavaModelException{
			int i;
			if (!methodBinding.getDeclaringClass().isFromSource())
			{
				System.out.println("Source not accesible");
				//inspectImpossibleMethod (invocation);
				return;
			}
			SearchPattern pattern = SearchPattern.createPattern((IMethod)methodBinding.getJavaElement(), IJavaSearchConstants.DECLARATIONS);
			SearchParticipant[] participants = new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
			MethodsCollector collector = new MethodsCollector();
			IJavaSearchScope scope = null;
			try 
			{
				scope = SearchEngine.createHierarchyScope(((IMethod)methodBinding.getJavaElement()).getDeclaringType());
				new SearchEngine().search(pattern, participants, scope, collector, new NullProgressMonitor());
			} 
			catch (CoreException e) 
			{
				e.printStackTrace();
			}
	
			Set<IMethod> result = collector.getMethods();
			
			IBinding[] foundBindings = null;
			System.out.println(result +"resultsss1");
			
			//-----------------------------
			IMethod method = (IMethod)methodBinding.getJavaElement();
			IJavaProject javaProject = method.getJavaProject();
			IType type = method.getDeclaringType();
			IMethod superMethod = method;
			ITypeHierarchy typeHierarchy = type.newTypeHierarchy(javaProject, null);
			IType[] supertypes = typeHierarchy.getAllSupertypes(type);
			for (IType supertype : supertypes) {
				if(!supertype.isBinary()) {
			  IMethod[] methods = supertype.getMethods();
			  for (IMethod m : methods) {
				  if(m.getElementName().equals(superMethod.getElementName()) && m.getNumberOfParameters()==superMethod.getNumberOfParameters()) {
					  List p = Arrays.asList(m.getParameterTypes());
					  List n = Arrays.asList(superMethod.getParameterTypes());
					  if(p.equals(n)) {
						  result.add(m);
					  }
				  }
			  }}
			}
			
			IType[] subtypes = typeHierarchy.getAllSubtypes(type);
			for (IType subtype : subtypes) {
				if(!subtype.isBinary()) {
			  IMethod[] methods = subtype.getMethods();
			  for (IMethod m : methods) {
				  if(m.getElementName().equals(superMethod.getElementName()) && m.getNumberOfParameters()==superMethod.getNumberOfParameters()) {
					  List p = Arrays.asList(m.getParameterTypes());
					  List n = Arrays.asList(superMethod.getParameterTypes());
					  if(p.equals(n)) {
						  result.add(m);
					  }
				  }
			  }}
			}System.out.println(result +"resultsss2");
			 //-----------------------------
			
			IMethod[] resultsArray = result.toArray(new IMethod[result.size()]);
			ASTParser parser=ASTParser.newParser(AST.JLS4);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setProject(((IJavaElement) result.toArray()[0]).getJavaProject());
			parser.setResolveBindings(true);
			foundBindings = (IBinding[]) parser.createBindings(resultsArray,new NullProgressMonitor());
			System.out.println("RESULT SIZE = " + result.size());
		
			for (i=0;i<result.size();i++)
			{	
				if (foundBindings[i] instanceof IMethodBinding)
					{
					final IMethodBinding currentBinding = (IMethodBinding) foundBindings[i];
					
					ICompilationUnit cUnit=((IMethod)foundBindings[i].getJavaElement()).getCompilationUnit();
					ASTParser parser1=ASTParser.newParser(AST.JLS4);
					parser1.setKind(ASTParser.K_COMPILATION_UNIT);
					parser1.setSource(cUnit);
					parser1.setResolveBindings(true);
					final CompilationUnit unit = (CompilationUnit) parser1.createAST(null);
					unit.accept(new ASTVisitor(){
						public boolean visit (MethodDeclaration declaration)
						{
							final IMethodBinding localBinding = declaration.resolveBinding();
							if (localBinding == null)
								return false;
							if (localBinding.isEqualTo(currentBinding))
							{
								if(localBinding.getReturnType().getName().startsWith("Optional<")) {
									System.out.println(" Has already Optional type");
									return true;
								}
								try {
									getMethodInvocation(declaration);
								} catch (JavaModelException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								 CompUnitModel compUnit = Factory.getInstance().createCompUnitModel(UtilityClass.getICompilationUnit(declaration));
								 System.out.println("^^^^^^^^^^^^^^^^^^^		 Declaration:  "+declaration + " in file "+compUnit.toString()+ " in "+compUnit.getPackage()); 
								 declaration = refindMethodDeclaration(declaration);
								 changeReturnExpression(declaration);
						     	 changeReturnType(declaration);
						     	
						     	
							 	 return true;
							}
							return true;
						}
					});
				}
			}
		}
	
	
	public MethodDeclaration refindMethodDeclaration(MethodDeclaration declaration) {
		
		final ICompilationUnit cUnit= UtilityClass.getICompilationUnit(declaration);
		ASTParser parser = ASTParser.newParser(AST.JLS4); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(cUnit);
		parser.setResolveBindings(true);
		final CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		unit.accept(new ASTVisitor(){
		
			public boolean visit(MethodDeclaration node)
			{
					final IMethodBinding localBinding = node.resolveBinding();
					if (localBinding == null)
						return false;
					if (localBinding.isEqualTo(declaration.resolveBinding()))
					{
						newMethodDeclaration = node;
					}
					
				return true;
			}
		});
		return newMethodDeclaration;
	}
	
	public VariableDeclarationStatement refindVariableDeclaration(VariableDeclaration variableDecl) {
		final ICompilationUnit cUnit= UtilityClass.getICompilationUnit(variableDecl);
		ASTParser parser = ASTParser.newParser(AST.JLS4); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(cUnit);
		parser.setResolveBindings(true);

		final CompilationUnit unit = (CompilationUnit) parser.createAST(null); 
		unit.accept(new ASTVisitor(){
		
			public boolean visit(VariableDeclarationStatement node)
			{
				VariableDeclarationFragment fragment = (VariableDeclarationFragment)node.fragments().get(0);
				if(variableDecl.resolveBinding().isEqualTo(fragment.resolveBinding())) {
					
					
					Expression exp= fragment.getInitializer();
					if(exp instanceof CastExpression)
						exp = ((CastExpression)exp).getExpression();
					final AST ast= fragment.getAST();
					final ASTRewrite rewriter=ASTRewrite.create(ast);
					SimpleName simpleName = ast.newSimpleName("varIntroducedByOptionalTool");
					System.out.println(fragment.getInitializer()+"yup "+fragment.getInitializer().getClass());
					MethodInvocation inv = (MethodInvocation) exp;
					//final AST ast= inv.getExpression().getAST();
					//final ASTRewrite rewriter=ASTRewrite.create(ast);
					rewriter.replace(inv.getExpression(), simpleName, null);
					//rewriteCUnit(cUnit, rewriter);
					
					rewriteCUnit(UtilityClass.getICompilationUnit(inv.getExpression()), rewriter);
				System.out.println(exp+" after ref");
					variableDeclarationStatement = node;
					
				}
				return true;
			}
		});
		return variableDeclarationStatement;
	}
	
	public Assignment refindAssignment(Assignment assign) {
		final ICompilationUnit cUnit= UtilityClass.getICompilationUnit(assign);
		ASTParser parser = ASTParser.newParser(AST.JLS4); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(cUnit);
		parser.setResolveBindings(true);

		final CompilationUnit unit = (CompilationUnit) parser.createAST(null); 
		unit.accept(new ASTVisitor(){
		
			public boolean visit(Assignment node)
			{ 
				Expression expr = node.getLeftHandSide();
				expr.accept(new ASTVisitor() {
					public boolean visit(SimpleName name) {
						if(name.resolveBinding().isEqualTo(((SimpleName)assign.getLeftHandSide()).resolveBinding())){
							System.out.println(name +" "+ node);
							assignment = node;
							final AST ast= node.getAST();
							final ASTRewrite rewriter=ASTRewrite.create(ast);
							SimpleName simpleName = ast.newSimpleName("varIntroducedByOptionalTool");
							MethodInvocation inv = (MethodInvocation) node.getRightHandSide();
							rewriter.replace(inv.getExpression(), simpleName, null);
							rewriteCUnit(cUnit, rewriter);//}
							
						}
						return true;
					}
				});
					
		
				return true;
			}
		});
		return assignment;
	}
	
	
	
	public void refactorVariable(Expression expr) {
		
		//Expression expr = (Expression) arg0.getUnderlyingObject();
		ICompilationUnit  cUnit = UtilityClass.getICompilationUnit((ASTNode)expr);

		Assignment assignment = (Assignment) UtilityClass.getAssignment(expr);
		VariableDeclaration variableDecl = (VariableDeclaration) UtilityClass.getVariableDeclaration(expr);
		
		ASTNode nextNode = null; 
		
		if(assignment != null) {
			//System.out.println(refindAssignment(assignment)+"(((");
			assignment = refindAssignment(assignment);
			System.out.println(assignment+"varrr  88");
			nextNode = assignment.getParent();
			System.out.println(nextNode+"varrr nes 88");
			if(assignment.getRightHandSide() instanceof MethodInvocation) {
				MethodInvocation m = (MethodInvocation) assignment.getRightHandSide();
				if(checkIfAssignmentIsAlreadyRefactor(m)) {
					return;
				}
			}
		}else if(variableDecl != null) {
			nextNode = refindVariableDeclaration(variableDecl);
			System.out.println(nextNode+":node:"+variableDecl+"varrr next  88"+variableDecl.getClass());
			VariableDeclarationStatement statement = (VariableDeclarationStatement)nextNode;
			
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
			System.out.println(fragment+" fragm varrr  88");
			if(fragment.getInitializer() instanceof MethodInvocation) {
				MethodInvocation m = (MethodInvocation)fragment.getInitializer();
				if(checkIfAssignmentIsAlreadyRefactor(m)) {
					return;
				}
			}
		}
		
		if(nextNode!=null) {
			//try {
				/****insertAssignment(cUnit,ifStatement,name.getFullyQualifiedName());**/
				/****refactorIfStatement(ifStatement);**/
				MethodInvocation method = (MethodInvocation)expr;
				System.out.println("sunt in try aici");
				if(UtilityClass.checkIfContainsAOptionalMethodCall(method,"orElse")) {
					method = (MethodInvocation) method.getExpression();
					System.out.println("a intray in if");
				}
				insertVariableDeclaration(cUnit,nextNode,getType(expr),method);
			/*} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
	}
	
	public boolean checkIfAssignmentIsAlreadyRefactor(MethodInvocation m) {
		
	   if(m.getExpression().toString().equals("varIntroducedByOptionalTool") && UtilityClass.checkIfContainsAOptionalMethodCall(m,"orElse")) {
			return true;
	   }
		return false;
	}
	
	public ITypeBinding getType(Expression expr) {
	/*	Assignment assignment = (Assignment) UtilityClass.getAssignment(expr);
		VariableDeclaration variableDecl = (VariableDeclaration) UtilityClass.getVariableDeclaration(expr);
		String type = "";
		if(assignment != null) {
			Expression leftHandSide = assignment.getLeftHandSide();
			if (leftHandSide instanceof SimpleName) {
				IBinding binding = ((SimpleName) leftHandSide).resolveBinding();
				if (binding instanceof IVariableBinding) {
					IVariableBinding variableBinding = (IVariableBinding) binding;
					type = variableBinding.getType().getName();
				}
			}
		}else if(variableDecl != null) {
			type = variableDecl.resolveBinding().getType().getName();
		}
		return type;*/
		
		if(expr instanceof MethodInvocation) {
			ITypeBinding type = ((MethodInvocation)expr).resolveTypeBinding();
			return type;
		}else if(expr instanceof SuperMethodInvocation) {
			ITypeBinding type = ((SuperMethodInvocation)expr).resolveTypeBinding();
			return type;
		}
		return null;
	}
	/********
	public void refactorIfStatement(IfStatement ifStatement ) {
		final AST ast=ifStatement.getAST();
		final ASTRewrite rewriter=ASTRewrite.create(ast);
		ICompilationUnit cUnit = UtilityClass.getICompilationUnit((ASTNode)ifStatement);
		if(ifStatement!=null && canBeRefactored(ifStatement)) {
			if(ifStatement.getExpression() instanceof InfixExpression) {
				InfixExpression expression = (InfixExpression)ifStatement.getExpression();
				InfixExpression newExpr = null;
				expression.setLeftOperand(ast.newSimpleName("varIntroducedByOptionalTool"));
				newExpr = (InfixExpression) createNewInfixExpression(expression, ast);
				rewriter.replace(expression, newExpr, null);
				rewriteCUnit(cUnit, rewriter);
				try {
					cUnit.commitWorkingCopy(true, null);
					cUnit.save(monitor, true);
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}******/
	
	
	/*public Assignment insertAssignment (ICompilationUnit cUnit, ASTNode node, String name) throws JavaModelException
	{
		final AST ast=node.getAST();
		final ASTRewrite rewriter=ASTRewrite.create(ast); 
		Statement thenStatement = ((IfStatement)node).getThenStatement();

		//Creates an unparented variable declaration fragment node owned by this AST.
		final Assignment assignment=ast.newAssignment();
		assignment.setOperator(Assignment.Operator.ASSIGN);
	    
	    MethodInvocation methodInvocation = ast.newMethodInvocation();
	    methodInvocation.setName(ast.newSimpleName("get"));
	    methodInvocation.setExpression(ast.newSimpleName("varIntroducedByOptionalTool"));
	    assignment.setRightHandSide(methodInvocation);
	    assignment.setLeftHandSide(ast.newSimpleName(name));
	    System.out.println(assignment+"assignmnet");
	    ExpressionStatement expressionStatement = ast.newExpressionStatement(assignment);
	    ListRewrite listRewrite = rewriter.getListRewrite(thenStatement, Block.STATEMENTS_PROPERTY);
	    listRewrite.insertFirst(expressionStatement, null);
	    rewriteCUnit(cUnit, rewriter);
	    try {
			cUnit.commitWorkingCopy(true, null);
			cUnit.save(monitor, true);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return assignment;
	}*/
	
	/*****
	//assignment-ul din interiorul if-ului
	public Assignment insertAssignment (ICompilationUnit cUnit, ASTNode node, String name) throws JavaModelException
	{
		
		final AST ast=node.getAST();
		final ASTRewrite rewriter=ASTRewrite.create(ast); 
		IfStatement ifStatement = (IfStatement)node;
		InfixExpression expr = (InfixExpression) ifStatement.getExpression();
		Statement statement;
		if(expr.getOperator().toString().equals("!=")) {
			statement = ifStatement.getThenStatement();
		}else {
			statement = ifStatement.getElseStatement();
		}

		//Creates an unparented variable declaration fragment node owned by this AST.
		final Assignment assignment=ast.newAssignment();
		assignment.setOperator(Assignment.Operator.ASSIGN);
	    
	    MethodInvocation methodInvocation = ast.newMethodInvocation();
	    methodInvocation.setName(ast.newSimpleName("get"));
	    methodInvocation.setExpression(ast.newSimpleName("varIntroducedByOptionalTool"));
	    assignment.setRightHandSide(methodInvocation);
	    assignment.setLeftHandSide(ast.newSimpleName(name));
	    System.out.println(assignment+"assignment");
	    ExpressionStatement expressionStatement = ast.newExpressionStatement(assignment);
	    ListRewrite listRewrite = rewriter.getListRewrite(statement, Block.STATEMENTS_PROPERTY);
	    listRewrite.insertFirst(expressionStatement, null);
	    rewriteCUnit(cUnit, rewriter);
	    try {
			cUnit.commitWorkingCopy(true, null);
			cUnit.save(monitor, true);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return assignment;
	}
	******/
	public VariableDeclarationStatement insertVariableDeclaration (ICompilationUnit cUnit, ASTNode nextNode, ITypeBinding type, Expression initializer) //throws JavaModelException
	{
		final AST ast= nextNode.getAST();
		final ASTRewrite rewriter=ASTRewrite.create(ast);
		//Creates an unparented variable declaration fragment node owned by this AST.
		final VariableDeclarationFragment retTypeVarDeclFragment=ast.newVariableDeclarationFragment();
		ExpressionVisitor v = new ExpressionVisitor(ast);
		initializer.accept(v);
	    retTypeVarDeclFragment.setInitializer(v.getExpression());
		retTypeVarDeclFragment.setName(ast.newSimpleName("varIntroducedByOptionalTool"));
		VariableDeclarationStatement declarationStatement=ast.newVariableDeclarationStatement(retTypeVarDeclFragment);
		QualifiedName packageName = ast.newQualifiedName(ast.newSimpleName("java"), ast.newSimpleName("util"));
		QualifiedName optionalName = ast.newQualifiedName(packageName, ast.newSimpleName("Optional"));
		ParameterizedType paramType = ast.newParameterizedType(ast.newSimpleType(optionalName));
		List<org.eclipse.jdt.core.dom.Type> newArgs = paramType.typeArguments();
		if(type.isParameterizedType()) {
			TypeVisitor typeV = new TypeVisitor(ast);
			ParameterizedType parameterizedType = ast.newParameterizedType(ast.newSimpleType(ast.newName(type.getTypeDeclaration().getName())));
			for (ITypeBinding typeArgument : type.getTypeArguments()) {
				ast.newSimpleType(ast.newSimpleName(typeArgument.getName())).accept(typeV);
				System.out.println(type.getTypeDeclaration().getName()+" here");
				System.out.println(typeV.getType()+" *** "+typeV.getType().getClass());
		        parameterizedType.typeArguments().add(typeV.getType());
		    }
			newArgs.add(parameterizedType);
		}
		else {
			newArgs.add(ast.newSimpleType(ast.newSimpleName(type.getName())));
			}
		declarationStatement.setType(paramType);
		if (nextNode.getParent() instanceof Block)
		{
			System.out.println("insertVariableDeclaration Is block");
			ListRewrite lrw = rewriter.getListRewrite(nextNode.getParent(),Block.STATEMENTS_PROPERTY);
			System.out.println(declarationStatement+"decl stattt" + nextNode.getParent()+" parrent "+nextNode.getParent().getClass());
			System.out.println(nextNode+"neee");
			lrw.insertBefore(declarationStatement, nextNode, null);
			System.out.println("icop: "+icomp+" uuuu ");
			System.out.println("icopR: "+rewriter.getAST());
			rewriteCUnit(cUnit, rewriter);
			/*try {
				cUnit.commitWorkingCopy(true, null);
				cUnit.save(monitor, true);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		else
		{
			System.out.println("insertVariableDeclaration Is not block: " + nextNode.getParent().getNodeType());
			Block block = ast.newBlock();
			block.statements().add(declarationStatement);
			block.statements().add(rewriter.createCopyTarget(nextNode));
			rewriter.replace(nextNode, block, null);
			rewriteCUnit(cUnit, rewriter);	
			/*try {
				cUnit.commitWorkingCopy(true, null);
				cUnit.save(monitor, true);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} am comentat*/
		}
		return declarationStatement;
	}
	
}