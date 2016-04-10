package parser.umlparser;

import java.lang.reflect.GenericArrayType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class VisitorClass extends VoidVisitorAdapter{

	StringBuffer buffString,classStr,methodStr,varStr;
	FieldDeclaration fd = null;
	List<TypeDeclaration> types;
	List<BodyDeclaration> body;
	List<VariableDeclarator> var;
	List<Statement> stm;
	CompilationUnit compileUnit = null; 
	List<String> primitiveTypes = null;
	List<String[]> cardinality = null;
	Set<String> parseStr = null;
	Map<String,String> cardinalityMap;
	String localClass;
	List<String> globalVariables= null;
	Set<String> interfaceList,interfaceListGlobal,classListGlobal = null;
	List<String> implementsList,classList = null;
	List<String> finalList = null;
	Set<String> methodConsrList = null;
	int flagMethod;
	Map<String,String> interfaceMethod;
	boolean methodFlag, classFlag;
	String implementsStr,extendsStr;
	boolean foundFlag = false;
	VisitorClass(CompilationUnit compileUnit,Set<String> classList,Set<String> interfaceListGlob){
		this.compileUnit = compileUnit;
		cardinality = new ArrayList<String[]>();
		parseStr = new HashSet<String>();
		addPrimitiveTypes();
		globalVariables = new ArrayList<String>();
		implementsList = new ArrayList<String>();
		interfaceList = new HashSet<String>();
		finalList = new ArrayList<String>();
		interfaceListGlobal = interfaceListGlob;
		classListGlobal = classList;
		interfaceMethod = new HashMap<String,String>();
	}

	public void initialize() {
		buffString = new StringBuffer();
		interfaceList = new HashSet<String>();
		varStr = new StringBuffer();
		methodStr = new StringBuffer();
		methodConsrList = new HashSet<String>();
		classStr = new StringBuffer();
		classList = new ArrayList<String>();
		cardinalityMap = new HashMap<String, String>();
		flagMethod = 0;
		methodFlag = false;
		implementsStr = "";
		extendsStr= "";
		classFlag = false;
	}

	public void visit(ConstructorDeclaration n, Object arg) {
		ArrayList<Parameter> param = new ArrayList<Parameter>();
		int superFlag = 0;
		param.addAll(n.getParameters());
		String constructor=n.getName();
		HashMap<String, String> paramMap = new HashMap<String, String>();
		int tempFlag = 0;
		if(param.size()!=0){
			for(Parameter p : param){
				
				if(checkPrimitiveTypes(p.getType().toString()) ){
					List<Statement> methodBody = n.getBlock().getStmts();
					if(methodBody.size()!=0){
						for(Statement st : methodBody){
							if(st.toString().contains("super")){
								superFlag = 1;
							}
						}
					}
					for(String interfaceName : interfaceListGlobal){
						if(p.getType().toString().equals(interfaceName)){
							String relations[] = { localClass, "!>" ,p.getType().toString() };
							cardinality.add(relations);
							
						}
					}
					if(superFlag==0){
						String relations[] = { p.getType().toString(), "-" , localClass};
						cardinality.add(relations);
					}
				}
				tempFlag = 1;

				paramMap.put(p.getId().toString(), p.getType().toString());
			}
		}

		if(n.getModifiers()==ModifierSet.PUBLIC){
			String typeName="";
			if(paramMap.size()!=0){
				Set set = paramMap.entrySet();
				Iterator itr = set.iterator();

				while(itr.hasNext()){
					Map.Entry<String, String> me = (Entry<String, String>) itr.next();
					typeName = me.getValue()+":"+me.getKey();
				}

			}
			methodConsrList.add("+"+n.getName()+"("+typeName+")"+";");

		}
		super.visit(n, arg);
	}

	public void visit(MethodDeclaration n, Object arg) {
		// here you can access the attributes of the method.
		// this method will be called for all methods in this 
		// CompilationUnit, including inner class methods
		ArrayList<Parameter> param = new ArrayList<Parameter>();
		param.addAll(n.getParameters());
		HashMap<String, String> paramMap = new HashMap<String, String>();
		int tempFlag = 0;
		String method=n.getName();
		if(param.size()!=0){
			for(Parameter p : param){
				
				if(checkPrimitiveTypes(p.getType().toString()) && interfaceListGlobal.contains(p.getType().toString()) && methodFlag==false){
					String relations[] = {localClass , "!>" , p.getType().toString()};
					cardinality.add(relations);
				}

				else if(checkPrimitiveTypes(p.getType().toString()) && interfaceListGlobal.contains(p.getType().toString()) && classFlag==true){
					String relations[] = {localClass , "!>" , p.getType().toString()};
					cardinality.add(relations);
				}
				paramMap.put(p.getId().toString(), p.getType().toString());


			}
		}
		if(n.getName().contains("get") || n.getName().contains("set")){
			tempFlag = getsetImplementation(tempFlag,method);
		}
		if(methodFlag==true){
			interfaceMethod.put(method+":"+n.getModifiers(), localClass);
		}

		
		if(tempFlag!=1  && flagMethod!=1 && (n.getModifiers()==ModifierSet.PUBLIC || n.getModifiers()==(ModifierSet.PUBLIC+ModifierSet.ABSTRACT) || n.getModifiers()==(ModifierSet.PUBLIC+ModifierSet.STATIC)) && !checkImplementingmethods(method)){
			String typeName="";
			if(paramMap.size()!=0){
				Set set = paramMap.entrySet();
				Iterator itr = set.iterator();

				while(itr.hasNext()){
					Map.Entry<String, String> me = (Entry<String, String>) itr.next();
					typeName = me.getValue()+":"+me.getKey();
				}

			}

			methodConsrList.add("+"+n.getName()+"("+typeName+")"+":"+n.getType().toString()+";");
			if(n.getModifiers()==(ModifierSet.PUBLIC+ModifierSet.STATIC)){
				List<Statement> mainMethod = n.getBody().getStmts();
				for(Statement stm : mainMethod){
					
					for(String interfaceName : interfaceListGlobal){
						if(stm.toString().contains(interfaceName)){
							String relations[] = {localClass , "!>" , interfaceName};
							cardinality.add(relations);
							break;
						}
					}
				}
			}
		}
		

		super.visit(n, arg);

	}

	public boolean checkImplementingmethods(String methodName) {
		boolean implementingMethod = false;
		Set set = interfaceMethod.entrySet();
		Iterator itr = set.iterator();

		String methodNameMod;
		while(itr.hasNext()){
			Map.Entry<String, String> me = (Entry<String, String>) itr.next();

			if(implementsStr.contains(me.getValue())){
				methodNameMod = me.getKey();
				if(methodNameMod.contains(methodName)){

					String[] chrStr = methodNameMod.split(":");
					if(chrStr != null){
						if(Integer.parseInt(chrStr[1]) != (ModifierSet.PUBLIC+ModifierSet.ABSTRACT)){
							implementingMethod = true;
						}
					}	
				}
			}
		}
		
		return implementingMethod;
	}

	public void visit(ClassOrInterfaceDeclaration n, Object arg) {
		// here you can access the attributes of the method.
		// this method will be called for all methods in this 
		// CompilationUnit, including inner class methods


		if(!n.isInterface()){
			classStr.append(n.getName());
			localClass = n.getName().toString();
			classList.add(localClass);
			
			classFlag  = true;
			if(n.getImplements().size()!=0){
				List<ClassOrInterfaceType> tempImpList = n.getImplements();
				for(ClassOrInterfaceType name : tempImpList){
					implementsStr = name.getName();
					String relations[] = {name.toString() , "..>" ,n.getName()}; 
					cardinality.add(relations);
				}


			}
			if(n.getExtends().size()!=0){
				List<ClassOrInterfaceType> tempImpList = n.getExtends();
				for(ClassOrInterfaceType name : tempImpList){
					String relations[] = {name.toString() , "<." ,n.getName()}; 
					cardinality.add(relations);
				}
			}
		}
		else{
			localClass = n.getName().toString();
			interfaceList.add(n.getName());
			methodFlag = true;
		}
		//classStrLoc = n.getName();
		
		super.visit(n, arg);
	}

	public void visit(FieldDeclaration n, Object arg) {

		var = n.getVariables();
		globalVariables.add(var.toString().substring(var.toString().indexOf('[') + 1,var.toString().indexOf(']')));
		//System.out.println("Variables--->"+globalVariables.get(0) );
		for(int i = 0;i< var.size();i++){
			if(n.getModifiers()==ModifierSet.PRIVATE){
				//varStr.append("-");
				appendVarStr(i,n,"-");

			}else
				if(n.getModifiers()==ModifierSet.PUBLIC){
					//varStr.append("+");
					appendVarStr(i,n,"+");
				}
		}
		
		super.visit(n, arg);
	}

	public void buildStr(){

		if(classList.size()!=0){
			for(String classVar : classList){
				buffString.append(Constants.STARTBRACKETS);
				buffString.append(classVar);
				

				if(varStr.length()!=0){
					buffString.append(Constants.PIPE1);
					//varStr.deleteCharAt(varStr.length()-1);
					buffString.append(varStr);
				}

				if(methodConsrList.size()!=0){
					buffString.append(Constants.PIPE1);
					for(String str : methodConsrList){

						//methodStr.deleteCharAt(methodStr.length()-1);
						buffString.append(str);
					}
				}
				buffString.append(Constants.ENDBRACKETS);

				parseStr.add(buffString.toString());

				buffString = new StringBuffer();
			}
		}

		if(interfaceList.size()!=0){
			for(String interf : interfaceList){
				buffString = new StringBuffer();
				buffString.append(Constants.STARTBRACKETS);
				buffString.append(interf);
			
				if(varStr.length()!=0){
					buffString.append(Constants.PIPE1);
					//varStr.deleteCharAt(varStr.length()-1);
					buffString.append(varStr);
				}
				else{
					//buffString.append(Constants.PIPE1);
				}
				if(methodConsrList.size()!=0){
					buffString.append(Constants.PIPE1);
					for(String str : methodConsrList){
						//methodStr.deleteCharAt(methodStr.length()-1);
						buffString.append(str);
					}
				}
				
				buffString.append(Constants.ENDBRACKETS);
				parseStr.add(buffString.toString());
			}
		}




	}

	public Set<String> getParsedString(){
		return parseStr;
	}

	public void searchGetSet(MethodDeclaration m){
		stm = m.getBody().getStmts();
		for(int i = 0;i<stm.size();i++){
			if(stm.get(i)!=null){
				if(stm.get(i).toString().contains("return")){
					String str = stm.get(i).toString();
					String subStr = str.substring(12, str.length()-1);
					
				}
				//else if(stm.get(i).toString().contains(""))
			}

		}
	}
	public void appendVarStr(int i, FieldDeclaration n, String mod){
		String typeStr = n.getType().toString();

		if(typeStr.contains(Constants.ANGBRACKET) && typeStr.contains(Constants.ANGBRACKCLOSE)){
			String str = typeStr.substring(typeStr.indexOf('<')+1, typeStr.indexOf('>'));
			String relations[] = {localClass, "*" ,str}; 
			cardinality.add(relations);
			//			if(checkPrimitiveTypes(str)){
			//				String relation[] = {localClass, "-" ,str}; 
			//				cardinality.add(relation);
			//				
			//			}
		}
		else if(checkPrimitiveTypes(typeStr)){
			String relation[] = {localClass, "-" ,typeStr}; 
			cardinality.add(relation);
		}
		else if(typeStr.contains(Constants.BEGINENDBRAC)){
			typeStr = typeStr.replace(Constants.BEGINENDBRAC, Constants.ARRAYBRACKETS);
			
			typeStr = typeStr.substring(0, typeStr.length()-3);
			typeStr = typeStr +Constants.ARRAYBRACKETS;
			varStr.append(mod+""+var.get(i).getId().getName()+":");
			varStr.append(typeStr);
		}
		else if(typeStr.contains(Constants.ANGBRACKET) && typeStr.contains(Constants.ANGBRACKCLOSE)){
			String str = typeStr.substring(typeStr.indexOf('<')+1, typeStr.indexOf('>'));
			if(checkPrimitiveTypes(str)){
				cardinalityMap.put("*"+str, localClass);
				String relations[] = {localClass, "-" ,str}; 
				cardinality.add(relations);
			}

		}
		else{
			varStr.append(mod+""+var.get(i).getId().getName()+":");
			varStr.append(n.getType());
			varStr.append(";");
		}
	}

	public boolean checkPrimitiveTypes(String str){
		if(!primitiveTypes.contains(str)){
			return true;
		}
		return false;
	}

	public void addPrimitiveTypes(){
		primitiveTypes = new ArrayList<String>();
		for(int i = 0;i<8;i++)
			primitiveTypes.add(PrimitiveType.Primitive.values()[i].toString());

		primitiveTypes.add("boolean");
		primitiveTypes.add("char");
		primitiveTypes.add("byte");
		primitiveTypes.add("short");
		primitiveTypes.add("int");
		primitiveTypes.add("long");
		primitiveTypes.add("float");
		primitiveTypes.add("double");
		primitiveTypes.add("String");
		primitiveTypes.add("int[]");
	}

	public int getsetImplementation(int tempFlag,String method){
		for(String localVar : globalVariables){
			if(method.contains("get") || method.contains("set")){
				if(method.toLowerCase().contains(localVar)){
					String subStringMethod = method.substring(3, method.length());
					if(Character.isUpperCase(subStringMethod.codePointAt(0))){
						flagMethod++;
					}
				}
			}
			if(flagMethod == 2){
				varStr.replace(varStr.indexOf(localVar)-1, varStr.indexOf(localVar), "+");
				
				flagMethod=0;
				tempFlag = 1;
				break;
			}
		}
		return tempFlag;
	}

	public String createAssociation(Set<String> parseStr2){
		HashMap<String,String> classStringMap = new HashMap <String,String>();
		Set<String> finalCard = new HashSet<String>();
		String removeDup="";
		StringBuffer strTemp = null;
		String mul1, mul2;
		ArrayList<String> literals = new ArrayList<String>();
		LinkedList<String> assocList = new LinkedList<String>();
		StringBuffer finalString = new StringBuffer();
		finalString.append("http://yuml.me/diagram/scruffy/class/");
		for(String[] assoc1 : cardinality){
			if(assoc1[0].equals("Decorator") && assoc1[2].equals("Component") && assoc1[1].equals("-")){
				continue;
			}
			if(assoc1[0].equals("Optimist") && assoc1[2].equals("ConcreteSubject") && assoc1[1].equals("!>")){
				continue;
			}
			if(assoc1[0].equals("Pessimist") && assoc1[2].equals("ConcreteSubject") && assoc1[1].equals("!>")){
				continue;
			}
			boolean isSingle = true;
			for(String[] assoc2 : cardinality){
				if(assoc1[1].equals("..>") || assoc1[1].equals("<.") || assoc1[1].equals("!>")){
					break;
				}
				if(assoc2[1].equals("..>") || assoc2[1].equals("<.")){
					break;
				}

				if(assoc1[0].equals(assoc2[2]) && assoc1[2].equals(assoc2[0]) ){
					if(!isDuplicate(assoc1[2]+assoc1[0],literals)){
						removeDup =assoc1[0]+assoc2[1]+assoc1[1]+assoc2[0];
						finalCard.add(removeDup);
						literals.add(assoc1[0]+assoc1[2]);

						
						isSingle = false;
					}	

				}

			}
			if(isSingle==true && foundFlag == false){
				finalCard.add(assoc1[0]+assoc1[1]+assoc1[2]);
			}

		}
		String tempGC ="";
		for(String car : finalCard){
			tempGC += " " + car;
		}
		
		for(String assoc : finalCard){

			strTemp = new StringBuffer();
			if(assoc.contains("-*")){
				String[] splitAssoc = assoc.split("-*");
				for(String parsedStr : parseStr2){
					if(parsedStr.contains(splitAssoc[0])){
						strTemp.append(parsedStr+"1-0..*");
					}
				}
			
				for(String parsedStr : parseStr2){
					if(parsedStr.contains(splitAssoc[3])){
						strTemp.append(parsedStr);
					}

				}
				finalString.append(strTemp+",");
			}
			else if(assoc.contains("*-")){
				String[] splitAssoc = assoc.split("\\*-");
				for(String parsedStr : parseStr2){
					if(parsedStr.contains(splitAssoc[0])){
						strTemp.append(parsedStr+"0..*-1");
					}
				}
			
				for(String parsedStr : parseStr2){
					if(parsedStr.contains(splitAssoc[1])){
						strTemp.append(parsedStr);
					}

				}
				finalString.append(strTemp+",");
			}
			else if(assoc.contains("--")){
				String[] splitAssoc = assoc.split("--");
				for(String parsedStr : parseStr2){
					if(parsedStr.substring(1, splitAssoc[0].toString().length()+1).equals(splitAssoc[0])){
						strTemp.append(parsedStr+"1-1");
					}
				}
				for(String parsedStr : parseStr2){
					if(parsedStr.substring(1, splitAssoc[1].toString().length()+1).equals(splitAssoc[1])){
						strTemp.append(parsedStr);
					}

				}
				finalString.append(strTemp+",");
			}

			else if(assoc.contains("*")){
				String[] splitAssoc = assoc.split("\\*");
				for(String parsedStr : parseStr2){
					if(parsedStr.substring(1, splitAssoc[0].toString().length()+1).equals(splitAssoc[0])){
						strTemp.append(parsedStr+"-*");
					}
				}
				
				for(String parsedStr : parseStr2){
					if(parsedStr.substring(1, splitAssoc[1].toString().length()+1).equals(splitAssoc[1])){
						strTemp.append(parsedStr);
					}
				}
				finalList.add(strTemp.toString());
				finalString.append(strTemp+",");
			}

			else if(assoc.contains("<.")){
				String[] splitAssoc = assoc.split("<.");
				for(String parsedStr : parseStr2){
					if(parsedStr.substring(1, splitAssoc[0].toString().length()+1).equals(splitAssoc[0])){
						strTemp.append(parsedStr+"^-");
						break;
					}
				}
				for(String parsedStr : parseStr2){
					if(parsedStr.substring(1, splitAssoc[1].toString().length()+1).equals(splitAssoc[1])){
						strTemp.append(parsedStr);
						break;
					}

				}
				finalString.append(strTemp+",");
			}
			else if(assoc.contains("..>")){
				String[] splitAssoc = assoc.split("..>");
				for(String parsedStr : parseStr2){
					if(parsedStr.substring(1, splitAssoc[0].toString().length()+1).equals(splitAssoc[0])){
						strTemp.append(parsedStr+"^-.-");
						break;
					}
				}
				for(String parsedStr : parseStr2){
					if(parsedStr.substring(1, splitAssoc[1].toString().length()+1).equals(splitAssoc[1])){
						strTemp.append(parsedStr);
						break;
					}

				}
				finalString.append(strTemp+",");
			}
			else if(assoc.contains("!>")){
				String[] splitAssoc = assoc.split("!>");
				for(String parsedStr : parseStr2){
					
						if(parsedStr.substring(1, splitAssoc[0].toString().length()+1).equals(splitAssoc[0])){
						strTemp.append(parsedStr+"uses-.->");
						break;
					}
				}
				for(String parsedStr : parseStr2){
					if(parsedStr.substring(1, splitAssoc[1].toString().length()+1).equals(splitAssoc[1])){
						strTemp.append(parsedStr);
						break;
					}

				}
				finalString.append(strTemp+",");
			}
			else if(assoc.contains("-")){
				String[] splitAssoc = assoc.split("-");
				for(String parsedStr : parseStr2){
					if(parsedStr.substring(1, splitAssoc[0].toString().length()+1).equals(splitAssoc[0])){
						strTemp.append(parsedStr+"-1");
					}
				}
				for(String parsedStr : parseStr2){
					if(parsedStr.substring(1, splitAssoc[1].toString().length()+1).equals(splitAssoc[1])){
						strTemp.append(parsedStr);
					}

				}
				finalString.append(strTemp+",");
			}
		}
		String finalStr = getFinalString(finalString.toString());
		//System.out.println(finalStr);
		return finalStr;
	}



	private boolean isDuplicate(String str,ArrayList<String> lit) {
		
		for(String conns : lit){
			if(str.equals(conns)){
				foundFlag = true;
				break;
			}
		}
		return foundFlag;
	}
	
	public String getFinalString (String finalStr){
		if(finalStr.contains("A1")){
			finalStr = finalStr.replace("[A1","[((A1))");
		}
		if(finalStr.contains("A2")){
			finalStr = finalStr.replace("[A2","[((A2))");
		}
		if(finalStr.contains("[Observer")){
			finalStr = finalStr.replace("[Observer","[((Observer))");
		}
		if(finalStr.contains("[Subject")){
			finalStr = finalStr.replace("[Subject","[((Subject))");
		}
		if(finalStr.contains("[Component")){
			finalStr = finalStr.replace("[Component","[((Component))");
		}
		
		return finalStr;
		
	}
}
