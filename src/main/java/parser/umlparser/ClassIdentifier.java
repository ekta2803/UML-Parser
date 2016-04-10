package parser.umlparser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ClassIdentifier extends VoidVisitorAdapter {
	String interfaceList,interfaceName,className = "";
	public ClassIdentifier(){
		
	}
	public void visit(ClassOrInterfaceDeclaration n, Object arg) {
		// here you can access the attributes of the method.
		// this method will be called for all methods in this 
		// CompilationUnit, including inner class methods


		if(!n.isInterface()){
			String localClass = n.getName().toString();
			className = localClass;
		}
		else{
			String intrface = n.getName().toString();
			//interfaceList.add(n.getName());
			interfaceName = intrface;
		}
		super.visit(n, arg);
	}
	
	public String getClassName(){
		return className;
	}
	
	public String getInterface(){
		return interfaceName;
	}
	
}
