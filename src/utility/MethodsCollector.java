package utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

public class MethodsCollector extends SearchRequestor
{

	private Set<IMethod> methods = new HashSet<IMethod>();

	public Set<IMethod> getMethods() 
	{
		return methods;
	}

	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException 
	{
		Object element = match.getElement();
		if (element instanceof IMethod) 
			methods.add((IMethod)element);		
	}

}