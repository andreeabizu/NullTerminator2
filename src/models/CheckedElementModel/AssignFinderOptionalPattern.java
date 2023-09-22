package models.CheckedElementModel;


import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import nullterminator.metamodel.entity.CheckedElementModel;
import nullterminator.metamodel.entity.MethodInvocationModel;
import nullterminator.metamodel.factory.Factory;
import ro.lrg.xcore.metametamodel.Group;
import ro.lrg.xcore.metametamodel.IRelationBuilder;
import ro.lrg.xcore.metametamodel.RelationBuilder;


@RelationBuilder
public class AssignFinderOptionalPattern  implements IRelationBuilder<MethodInvocationModel, CheckedElementModel>
{
	private Group<MethodInvocationModel> nullAssignments = null;
	private List<IVariableBinding> processedVariables;
	private Stack<IVariableBinding> interestingVariables = new Stack<IVariableBinding>();
	private int startPosition;

	@Override
	public Group<MethodInvocationModel> buildGroup(CheckedElementModel arg0) {
	
		nullAssignments = new Group<>();
		SimpleName expr = (SimpleName)arg0.getUnderlyingObject();
		startPosition = expr.getStartPosition();
		MethodInvocation m = null;
		processedVariables = new ArrayList<IVariableBinding>();
		
		if(expr.getParent() instanceof InfixExpression) {
			IVariableBinding variableBinding = (IVariableBinding) expr.resolveBinding();
			try {
				nullAssignments =  mainFinder(variableBinding);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(expr.getParent() instanceof MethodInvocation ) {
			//m = (MethodInvocation)arg0.getUnderlyingObject();
			m = (MethodInvocation) expr.getParent();
			try {
				nullAssignments = mainFinder(m);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else if(expr.getParent() instanceof SuperMethodInvocation) {
			SuperMethodInvocation sm = (SuperMethodInvocation) expr.getParent();
			try {
				nullAssignments = mainFinder(sm);
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		interestingVariables.clear();
		return nullAssignments;
	}
	
	public boolean myContains (IVariableBinding variable, Stack<IVariableBinding> stack)
	{
		List <IVariableBinding> list = new ArrayList<IVariableBinding> (stack);
		return myContains(variable, list);
	}
	
	public boolean myContains (IVariableBinding variable, List<IVariableBinding> list)
	{
		for (IVariableBinding v:list)
			if (v.isEqualTo(variable))
				return true;
		return false;
	}
	public Expression solveCast (Expression expression)
	{
		if (expression instanceof CastExpression)
			return ((CastExpression) expression).getExpression();
		return expression;
	}

	
	//var = null
	public void inspectAssignment (IVariableBinding variable, Expression expression)
	{
		if (expression instanceof NullLiteral)
		{
			System.out.println("STOP - Null assignment: " + variable.getName() + "=null");
		}
		else 
			inspectAssignment(expression);
	}
	

	//methodInvocation (expression)
		public void inspectAssignment (MethodInvocation methodInvocation, Expression expression)
		{

			if (expression instanceof NullLiteral)
			{
				System.out.println("STOP - Null sent as argument: " + methodInvocation.getName() + "=null");
			}
			else 
				inspectAssignment(expression);
		}
		
		

		//new Constructor (expression)
		public void inspectAssignment (ClassInstanceCreation instanceCreation, Expression expression)
		{
			System.out.println("Calling inspect assignment");
			if (expression!=null)
				System.out.println("Expr: " + expression.toString());
			if (expression instanceof NullLiteral)
			{
				System.out.println("STOP - Null sent as argument: " + instanceCreation.getType() + "=null");
			}
			else inspectAssignment(expression);
		}
		
		
		/*
		 * var = otherVar
		 * var = method()
		 * var = array[i]
		 * var = this.field
		 * var = super.field
		 */
		public void inspectAssignment (Expression expression)
		{
			expression = solveCast(expression);
			if (expression instanceof MethodInvocation)	//works also for chain invocation 
			{
				if(expression.getStartPosition() < startPosition) {
				MethodInvocationModel p= Factory.getInstance().createMethodInvocationModel((MethodInvocation)expression);
				nullAssignments.add(p);
				System.out.println("Added method invocation" + expression.toString());
				}
				}
			else 
			{
				IVariableBinding variableBinding = null;
				if (expression != null)
					System.out.println("Expression type: " + expression.getNodeType());
				if (expression instanceof SimpleName)
					variableBinding = (IVariableBinding) ((SimpleName)expression).resolveBinding();	
				else if (expression instanceof QualifiedName)
					variableBinding = (IVariableBinding) ((QualifiedName)expression).getName().resolveBinding();
				else if (expression instanceof ArrayAccess)
					inspectAssignment(((ArrayAccess) expression).getArray());
				else if (expression instanceof FieldAccess)
					variableBinding = (IVariableBinding) ((FieldAccess)expression).getName().resolveBinding();							
				else if (expression instanceof SuperFieldAccess)
					variableBinding = (IVariableBinding) ((SuperFieldAccess)expression).getName().resolveBinding();
				
				if (variableBinding != null)
					if (!myContains(variableBinding, interestingVariables))
						{
						System.out.println("Added new variable: " + variableBinding.getName());
						interestingVariables.push(variableBinding);
						}
			}
		}


		public void processAssignment (Expression leftSide, Expression rightSide, IVariableBinding variable)
		{
			if (leftSide instanceof SimpleName)
			{
				System.out.println ("left side is simpleName");
				IVariableBinding leftVariableBinding = (IVariableBinding) ((SimpleName)leftSide).resolveBinding();
				if (leftVariableBinding.isEqualTo(variable))
					inspectAssignment(leftVariableBinding, rightSide);
			}
			else if (leftSide instanceof QualifiedName)
			{
				System.out.println ("left side is qualifiedName");
				IVariableBinding leftVariableBinding = (IVariableBinding) ((QualifiedName)leftSide).getName().resolveBinding();
				if (leftVariableBinding.isEqualTo(variable))
					inspectAssignment(leftVariableBinding, rightSide);
			}
			else if (leftSide instanceof ArrayAccess)
			{
				Expression arrayVariable = ((ArrayAccess)leftSide).getArray();
				processAssignment(arrayVariable,rightSide,variable);
			}
			else if (leftSide instanceof FieldAccess)
			{
				System.out.println ("left side is fieldaccess");
				IVariableBinding leftVariableBinding = (IVariableBinding) ((FieldAccess)leftSide).getName().resolveBinding();				
				if (leftVariableBinding.isEqualTo(variable))
					inspectAssignment(leftVariableBinding, rightSide);
			}
			else if (leftSide instanceof SuperFieldAccess)
			{
				System.out.println ("left side is superfieldaccess");
				IVariableBinding leftVariableBinding = (IVariableBinding) ((SuperFieldAccess)leftSide).getName().resolveBinding();				
				if (leftVariableBinding.isEqualTo(variable))
					inspectAssignment(leftVariableBinding, rightSide);
			}		
			else System.out.println ("left side is " + leftSide.getNodeType());
		}


		public void findAssignment(final IMethodBinding methodBinding, final IVariableBinding variable)
		{
			if (methodBinding == null)
			{
				System.out.println("Method binding is null ");
				return;
			}
	
			IMethod method = (IMethod) methodBinding.getJavaElement();
			if (method == null)
				{
				System.out.println("Default constructor");
				return;	//covers the case when we try to access the default constructor
				}
			ICompilationUnit iCompilationUnit=method.getCompilationUnit();
			ASTParser parser=ASTParser.newParser(AST.JLS4);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setSource(iCompilationUnit);
			parser.setResolveBindings(true);
			final CompilationUnit unit = (CompilationUnit) parser.createAST(null);
			unit.accept(new ASTVisitor(){
			public boolean visit(MethodDeclaration node)
				{
				IMethodBinding localBinding = node.resolveBinding();
				if (localBinding == null)
					{
					System.out.println("Local binding is null");
					return false;
					}
				if (localBinding.isEqualTo(methodBinding))
					node.accept(new ASTVisitor(){
					
					public boolean visit(Assignment assignment)	//var = something;
						{
						processAssignment(assignment.getLeftHandSide(),assignment.getRightHandSide(),variable);
						return true;
						}
					
					public boolean visit(VariableDeclarationFragment declaration)  //Type var = something;
						{
						IVariableBinding variableBinding = (IVariableBinding)(declaration.getName()).resolveBinding();
						if (variableBinding == null)
							return false;
						if (variableBinding.isEqualTo(variable))
							inspectAssignment(variableBinding, declaration.getInitializer());
						return true;
						}
					});
				return true;
				}
			});
		}
		
		/*
		 * used to find uninitialized or null initialized fields
		 */
		public void findFieldDeclaration (ITypeBinding typeBinding, final IVariableBinding variable)
		{
			System.out.println("Looking for field declaration for field: " + variable.getName() + " in type: " + typeBinding.getName());
			ICompilationUnit iCompilationUnit=((IField)variable.getJavaElement()).getCompilationUnit();
			ASTParser parser=ASTParser.newParser(AST.JLS4);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setSource(iCompilationUnit);
			parser.setResolveBindings(true);
			final CompilationUnit unit = (CompilationUnit) parser.createAST(null);
			unit.accept(new ASTVisitor(){
				public boolean visit (VariableDeclarationFragment declaration)
				{
					IVariableBinding variableBinding = (IVariableBinding)(declaration.getName()).resolveBinding();
					if (variableBinding.isEqualTo(variable))
						//no need to do this for local variables since eclipse cries at compile time about this
						if (declaration.getInitializer() == null)
						{
							System.out.println("STOP - Uninitialized field" + variable.getName());
						
						}
						else {
							System.out.println("Initialized with something");
							inspectAssignment(variableBinding, declaration.getInitializer());
						}
					return true;	
				}
			});
		}


		public void inspectField (IVariableBinding variable) throws JavaModelException
		{
			System.out.println("Is field");
			ITypeBinding declaringClass = variable.getDeclaringClass();
			IMethodBinding[] methods = declaringClass.getDeclaredMethods();
			IField field = (IField) variable.getJavaElement();
		
			findFieldDeclaration(declaringClass, variable);
			if (Flags.isPrivate(field.getFlags()))
			{
				System.out.println("Private field");
				//check all methods in the field class that access the current field
				for (IMethodBinding method:methods)
				{
					System.out.println("Parsing method: " + method.getName());
					findAssignment(method, variable);
				}
			}
			else if (Flags.isProtected(field.getFlags()) || Flags.isPackageDefault(field.getFlags()))
			{
				System.out.println("Protected or package field");
				IPackageFragment packageFragment = (IPackageFragment) declaringClass.getPackage().getJavaElement();
				System.out.println("Package: " + packageFragment.getElementName());
				ICompilationUnit[] compilationUnits = packageFragment.getCompilationUnits();
				for (ICompilationUnit compilationUnit:compilationUnits)	//for every .java file
					{
						System.out.println("Unit: " + compilationUnit.getElementName());
						IType[] types = compilationUnit.getAllTypes();
						for (IType type:types)
						{
							System.out.println("Type: " + type.getElementName());
							ASTParser parser=ASTParser.newParser(AST.JLS4);
							parser.setKind(ASTParser.K_COMPILATION_UNIT);
							parser.setProject(packageFragment.getJavaProject());
							parser.setResolveBindings(true);
							IBinding[] methodsBinding = parser.createBindings(type.getMethods(), new NullProgressMonitor());
							System.out.println("methods bindings found: "+ methodsBinding.length);
							for (IBinding currentMethod:methodsBinding)
								{
								System.out.println("Searching assignment in method " + currentMethod.getName());
								findAssignment((IMethodBinding) currentMethod, variable);
								}
						}
					}
				if (Flags.isProtected(field.getFlags()))
				{
					System.out.println("Look for subclasses");
				}
			}
			else
			{
				System.out.println("Public field");
				IJavaProject javaProject = field.getJavaProject();
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
							for (ICompilationUnit compilationUnit:compilationUnits)	//for every .java file
								{
									IType[] types = compilationUnit.getAllTypes();
									for (IType type:types)
									{
										ASTParser parser=ASTParser.newParser(AST.JLS4);
										parser.setKind(ASTParser.K_COMPILATION_UNIT);
										parser.setProject(javaProject);
										parser.setResolveBindings(true);
										IBinding[] methodsBinding = parser.createBindings(type.getMethods(), new NullProgressMonitor());
										for (IBinding currentMethod:methodsBinding)
											findAssignment((IMethodBinding) currentMethod, variable);
									}
								}
							}
						}
			}
		}

		
		public void inspectParameter (IVariableBinding variable, final int position) throws JavaModelException
		{
			int i;
			ICompilationUnit[] compilationUnits;
			System.out.println("Is parameter");
			final IMethodBinding declaringMethod = variable.getDeclaringMethod();
			System.out.println("declaring method: " + declaringMethod.getName());
			//should search in entire project for calls of this method
			IJavaProject javaProject = declaringMethod.getJavaElement().getJavaProject();
			IPackageFragmentRoot[] packages=javaProject.getPackageFragmentRoots();
			for (IPackageFragmentRoot iPackageFragmentRoot:packages)
				if (!(iPackageFragmentRoot.isExternal()))
					{
					IJavaElement[] javaElements=iPackageFragmentRoot.getChildren();
					for (i=0;i<javaElements.length;i++)
						{
						IPackageFragment packageFragment=(IPackageFragment)javaElements[i];				
						compilationUnits=packageFragment.getCompilationUnits();
						for (ICompilationUnit compilationUnit:compilationUnits)	//for every .java file
							{		
							ASTParser parser=ASTParser.newParser(AST.JLS4);
							parser.setKind(ASTParser.K_COMPILATION_UNIT);
							parser.setSource(compilationUnit);
							parser.setResolveBindings(true);
							CompilationUnit cUnit = (CompilationUnit)parser.createAST(null);
							cUnit.accept(new ASTVisitor(){
								public boolean visit (MethodInvocation methodInvocation)
								{
									if (methodInvocation.resolveMethodBinding() != null)
									{
										if (methodInvocation.resolveMethodBinding().isEqualTo(declaringMethod))
										{
											System.out.println("Found invocation: " + methodInvocation.toString());
											List<Expression> list = methodInvocation.arguments();
											inspectAssignment(methodInvocation, list.get(position));
										}
									}
									return true;
								}
								
								public boolean visit (ClassInstanceCreation instanceCreation)
								{
									if (!declaringMethod.isConstructor())
										return false;
									ITypeBinding constructorBinding = instanceCreation.getType().resolveBinding();
									if (constructorBinding!=null)
										if (constructorBinding.getName().equals(declaringMethod.getDeclaringClass().getName()))
											{
											System.out.println("Class creation " + instanceCreation.toString() + "   " + 
													instanceCreation.getExpression() + " "+ instanceCreation.getType().toString());
		
											List<Expression> list = instanceCreation.arguments();
											//must check the size in case a class has more than 1 constructor
											if (position >= list.size())
												return false;
											inspectAssignment(instanceCreation, list.get(position));
											}
									return true;
								}
							});
							}
						}
					}
		}
		
		public Group<MethodInvocationModel> mainFinder (MethodInvocation invocation) throws JavaModelException
		{
			MethodInvocationModel p= Factory.getInstance().createMethodInvocationModel(invocation);
			nullAssignments.add(p);
			return nullAssignments;
		}
		
		public Group<MethodInvocationModel> mainFinder (SuperMethodInvocation invocation) throws JavaModelException
		{System.out.println("nu sunt aici!!");
			MethodInvocationModel p= Factory.getInstance().createMethodInvocationModel(invocation);
			nullAssignments.add(p);
			return nullAssignments;
		}
		
		public Group<MethodInvocationModel> mainFinder (IVariableBinding variable) throws JavaModelException
		{
			interestingVariables.clear();
			interestingVariables.push(variable);
			return mainFinder();
		}
		
		public Group<MethodInvocationModel> mainFinder () throws JavaModelException
		{
			IVariableBinding currentVariable;
			while (!interestingVariables.isEmpty())
			{		
				currentVariable = interestingVariables.pop();
				System.out.println("Inspecting " + currentVariable.getName());
				if (myContains(currentVariable, processedVariables))
					System.out.println("Already processed!");
				else
				{
					findAssignment(currentVariable.getDeclaringMethod(), currentVariable);
					processedVariables.add(currentVariable);
					if (currentVariable.isField())
						inspectField(currentVariable);
					else if (currentVariable.isParameter())
						{
						System.out.println("Param name: " + currentVariable.getName());
						System.out.println("method declaration: "+currentVariable.getDeclaringMethod().getName());
						ITypeBinding[] paramTypes = currentVariable.getDeclaringMethod().getParameterTypes();
						String[] paramNames = ((IMethod)currentVariable.getDeclaringMethod().getJavaElement()).getParameterNames();
						int i;
						for (i=0;i<paramTypes.length;i++)
						{
							System.out.println("argument " + paramTypes[i].getName() + " " + paramNames[i]);
							//try to get the actual name of the parameters
							if (paramTypes[i].isEqualTo(currentVariable.getType()) && 
								paramNames[i].equals(currentVariable.getName()))
							{
								System.out.println("Found argument position = "+i);
								inspectParameter(currentVariable,i);
								break;
							}
						}
					}
				}
			}
			
			return nullAssignments;
		}
	

}
