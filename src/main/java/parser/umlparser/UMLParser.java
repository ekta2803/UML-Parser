package parser.umlparser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.ModifierSet;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

/**
 * Hello world!
 *
 */
public class UMLParser 
{
	String propFile = "/Users/ekta2803/Documents/CMPE202/umlparser/src/main/java/parser/umlparser/parserProperties.properties";
	FileInputStream inFile = null;
	FileInputStream fileInput = null;
	InputStream in = null;
	File file = null;


	FieldDeclaration fd = null;
	List<TypeDeclaration> types;
	List<BodyDeclaration> body;
	List<VariableDeclarator> var = null;

	UMLParser(int i){


		try {
			fileInput = new FileInputStream(file);

			fileInput.close();
			String fileName = "inputFile"+1;



		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public static void main( String[] args )
	{
		String fileName,extension,path;
		CompilationUnit compileUnit = null;
		String filePath = args[0];
		File folder = new File(filePath);
		File[] fileList = folder.listFiles();
		ClassIdentifier ci = new ClassIdentifier();
		String className,interfaceName;
		Set<String> classNameList = new HashSet<String>();
		Set<String> interfaceList = new HashSet<String>();
		Set<String> javaFile = new HashSet<String>();
		for(File file : fileList){
			if(file.isFile()){
				fileName = file.getName();
				extension = fileName.substring(fileName.length()-4);
				if(extension.equals("java")){

					path = filePath + fileName;
					javaFile.add(path);
					try {
						FileInputStream in = new FileInputStream(path);
						compileUnit = JavaParser.parse(in);
						ci.visit(compileUnit, null);
						className = ci.getClassName();
						interfaceName = ci.getInterface();
						if(className!=null){
							classNameList.add(className);
						}
						if(interfaceName!=null){
							interfaceList.add(interfaceName);
						}
					} catch (FileNotFoundException e) {

						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		VisitorClass v = new VisitorClass(compileUnit,classNameList,interfaceList);
		for(String file : javaFile){
			FileInputStream in;
			try {
				in = new FileInputStream(file);
				compileUnit = JavaParser.parse(in);
				v.initialize();
				v.visit(compileUnit, null);
				v.buildStr();
			} catch (FileNotFoundException e) {

				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}

		}

		String umlString = v.createAssociation(v.getParsedString());

		URL url;
		try {
			url = new URL(umlString);
			InputStream in = new BufferedInputStream(url.openStream());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int n = 0;
			while (-1!=(n=in.read(buf)))
			{
				out.write(buf, 0, n);
			}
			out.close();
			in.close();
			byte[] response = out.toByteArray();
			String output = args[1];
			FileOutputStream fos = new FileOutputStream("./"+output);
			fos.write(response);
			System.out.println("File created successfully ");
			fos.close();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

