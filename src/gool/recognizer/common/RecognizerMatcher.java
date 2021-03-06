/*
 * Copyright 2010 Pablo Arrighi, Alex Concha, Miguel Lezama for version 1.
 * Copyright 2013 Pablo Arrighi, Miguel Lezama, Kevin Mazet for version 2.    
 *
 * This file is part of GOOL.
 *
 * GOOL is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, version 3.
 *
 * GOOL is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License along with GOOL,
 * in the file COPYING.txt.  If not, see <http://www.gnu.org/licenses/>.
 */

package gool.recognizer.common;

import gool.Settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;


public class RecognizerMatcher {

	//private static Platform InputLang;
	private static String InputLang;
	private static String DirOutPut;
	private static HashMap<String, ArrayList<String>> ClassMatchTable;
	private static HashMap<String, ArrayList<String>> MethodMatchTable;

	static public void init(/*Platform inputLang*/ String inputLang) {
		// Initialization of data structures
		InputLang = inputLang;
		ClassMatchTable = new HashMap<String, ArrayList<String>>();
		MethodMatchTable = new HashMap<String, ArrayList<String>>();
		
		DirOutPut = Settings.get(InputLang+"_in_dir_tmp");
		File tmpFolder = new File(DirOutPut);
		tmpFolder.mkdir();

		// Enabling the recognition of default libraries of the input language
		ArrayList<String> defaultGoolClasses = getGoolClassesFromImport("default");
		for (String defaultGoolClass : defaultGoolClasses)
			enableRecognition(defaultGoolClass);
	}

	public static ArrayList<String> matchImportAdd(String inputLangImport) {
		ArrayList<String> imports = getImportChange(inputLangImport);
		ArrayList<String> toReturn = new ArrayList<String>();
		if(imports.isEmpty())
			return imports ;
		System.out.print("[RecognizerMatcher] Enabling the recognition of the import : "
				+ inputLangImport );
		File tmpFolder = new File(DirOutPut);
		if(!tmpFolder.exists()){
			tmpFolder.mkdir();
		}
		for (String importName : imports){
			System.out.println(" and change to "+ importName);
			File tmpFile = new File(DirOutPut+"/"+importName);
			System.out.println(
					copyFile(new File(importName),tmpFile ));
			toReturn.add(DirOutPut+"/"+importName);
		}
		return toReturn;
	}
	
	public static boolean matchImport(String inputLangImport) {
		ArrayList<String> goolClasses = getGoolClassesFromImport(inputLangImport);
		for (String goolClass : goolClasses)
			enableRecognition(goolClass);
		return !goolClasses.isEmpty();
	}

	public static String matchClass(String inputLangClass) {
		for (String goolClass : ClassMatchTable.keySet())
		if (ClassMatchTable.get(goolClass).contains(inputLangClass))
			return goolClass;
		return null;
	}

	public static String matchMethod(String inputLangMethodSignature) {
			for (String goolMethod : MethodMatchTable.keySet())
			if (MethodMatchTable.get(goolMethod).contains(
					inputLangMethodSignature))
				return goolMethod;
		return null;
	}

	
	
	private static void enableRecognition(String goolClass) {
		if(ClassMatchTable.keySet().contains(goolClass))
			return;
		System.out
				.println("[RecognizerMatcher] Enabling the recognition of the GOOL class: "
						+ goolClass + ".");
		try {
			// matching the GOOL class with the input language classes
			ArrayList<String> inputClasses = new ArrayList<String>();
			InputStream ips = new FileInputStream(
					getPathOfInputClassMatchFile(goolClass));
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;
			while ((line = br.readLine()) != null) {
				line = removeSpaces(line);
				if (isInputMatchLine(line)) {
					String currentGoolClass = getLeftPartOfInputMatchLine(line);
					ArrayList<String> currentInputClasses = parseCommaSeparatedValues(getRightPartOfInputMatchLine(line));
					if (currentGoolClass.equals(goolClass))
						inputClasses.addAll(currentInputClasses);
				}
			}
			br.close();
			ClassMatchTable.put(goolClass, inputClasses);

			// matching the GOOL methods of the GOOL class with the input
			// language methods
			ArrayList<String> goolMethods = getGoolMethodsFromGoolClass(goolClass);
			for (String goolMethod : goolMethods) {
				ArrayList<String> inputMethodSignatures = new ArrayList<String>();
				ips = new FileInputStream(
						getPathOfInputMethodMatchFile(goolMethod));
				ipsr = new InputStreamReader(ips);
				br = new BufferedReader(ipsr);
				while ((line = br.readLine()) != null) {
					line = removeSpaces(line);
					if (isInputMatchLine(line)) {
						String currentGoolMethod = getLeftPartOfInputMatchLine(line);
						String currentInputMethodSignature = getRightPartOfInputMatchLine(line);
						if (currentGoolMethod.equals(goolMethod))
							inputMethodSignatures
									.add(currentInputMethodSignature);
					}
				}
				br.close();
				MethodMatchTable.put(goolMethod, inputMethodSignatures);
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	
	static private ArrayList<String> getImportChange(
			String inputLangImport) {
		ArrayList<String> imports = new ArrayList<String>();
		
		try {
			InputStream ips = new FileInputStream(
					getPathOfInputImportMatchFile());
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;
			while ((line = br.readLine()) != null) {
				line = removeSpaces(line);
				if (isInputMatchLine(line)) {
					String currentImport = getLeftPartOfInputMatchLine(line);
					ArrayList<String> currentInputImports = parseCommaSeparatedValues(getRightPartOfInputMatchLine(line));
					if(isImportAddName(currentImport)){
						String importName = getImportAddName(currentImport);
						if(importName.compareTo(inputLangImport) == 0){
							// Recognized import changed :
							imports.addAll(currentInputImports);
						}
					}
				}
			}
			br.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return imports;
	}
	
	static private ArrayList<String> getGoolClassesFromImport(
			String inputLangImport) {
		ArrayList<String> goolClasses = new ArrayList<String>();
		
		try {
			InputStream ips = new FileInputStream(
					getPathOfInputImportMatchFile());
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;
			while ((line = br.readLine()) != null) {
				line = removeSpaces(line);
				if (isInputMatchLine(line)) {
					String currentGoolClass = getLeftPartOfInputMatchLine(line);
					ArrayList<String> currentInputImports = parseCommaSeparatedValues(getRightPartOfInputMatchLine(line));
					if (currentInputImports.contains(inputLangImport))
						goolClasses.add(currentGoolClass);
				}
			}
			br.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return goolClasses;
	}

	static private ArrayList<String> getGoolMethodsFromGoolClass(
			String goolClass) {
		ArrayList<String> goolMethods = new ArrayList<String>();
		try {
			InputStream ips = new FileInputStream(
					getPathOfInputMethodMatchFile(goolClass + "."));
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;
			while ((line = br.readLine()) != null) {
				line = removeSpaces(line);
				if (isInputMatchLine(line)) {
					String currentGoolMethod = getLeftPartOfInputMatchLine(line);
					if (currentGoolMethod.contains(goolClass)) {
						goolMethods.add(currentGoolMethod);
					}
				}
			}
			br.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return goolMethods;
	}

	
	
	
	
	
	
	/*
	 * methods used by the GoolMatcher to parse each line of a match file
	 */
	static private String removeSpaces(String line) {
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == ' ' || line.charAt(i) == '\t') {
				line = line.substring(0, i) + line.substring(i + 1);
				i -= 1;
			}
		}
		return line;
	}

	static private boolean isCommentLine(String line) {
		return line.startsWith("#");
	}

	static private boolean isInputMatchLine(String line) {
		return !isCommentLine(line) && line.contains("<-");
	}

	static private String getLeftPartOfInputMatchLine(String InputMatchLine) {
		return InputMatchLine.substring(0, InputMatchLine.indexOf("<-"));
	}

	static private String getRightPartOfInputMatchLine(String InputMatchLine) {
		return InputMatchLine.substring(InputMatchLine.indexOf("<-") + 2);
	}
	
	static private boolean isImportAddName(String importName) {
		return importName.startsWith("+");
	}
	
	static private String getImportAddName(String importName) {
		return importName.substring(1);
	}
	
	static private ArrayList<String> parseCommaSeparatedValues(String csv) {
		ArrayList<String> parsedValues = new ArrayList<String>();
		csv+=";";
		while (!csv.isEmpty()) {
			int ind1 = csv.indexOf(",");
			int ind2 = csv.indexOf(";");
			if (ind1 != -1) {
				parsedValues.add(csv.substring(0, ind1));
				csv = csv.substring(ind1 + 1);
			} else {
				parsedValues.add(csv.substring(0, ind2));
				csv = csv.substring(ind2 + 1);
			}
		}
		return parsedValues;
	}

	/*
	 * methods used by the GoolMatcher to compute the path to match files
	 */
	static private String getPathOfInputMatchDir(String goolClass) {
		String goolPackageName = goolClass.substring(0,
				goolClass.lastIndexOf("."));
		goolPackageName = goolPackageName.replace('.', '/');
		return "src/gool/recognizer/" + InputLang.toString().toLowerCase()
				+ "/matching/" + goolPackageName + "/";
	}

	static private String getPathOfInputImportMatchFile() {
		return "src/gool/recognizer/" + InputLang.toString().toLowerCase()
				+ "/matching/ImportMatching.properties";
	}

	static private String getPathOfInputClassMatchFile(String goolClass) {
		return getPathOfInputMatchDir(goolClass) + "ClassMatching.properties";
	}

	static private String getPathOfInputMethodMatchFile(String goolMethod) {
		String goolClassName = goolMethod.substring(0,
				goolMethod.lastIndexOf("."));
		return getPathOfInputMatchDir(goolClassName)
				+ "MethodMatching.properties";
	}

	// The following method may be used for debugging issues: it prints the
	// content of the match tables.
	public static void printMatchTables() {
		System.out.println("[RecognizerMatcher] Printing ClassMatchTable...");
		for (String goolClass : ClassMatchTable.keySet()) {
			System.out.print("[RecognizerMatcher] -- " + goolClass + " <- ");
			for (String inputLangClass : ClassMatchTable.get(goolClass))
				System.out.print(inputLangClass + " ");
			System.out.println();
		}
		System.out.println("[RecognizerMatcher] Printing MethodMatchTable...");
		for (String goolMethod : MethodMatchTable.keySet()) {
			System.out.print("[RecognizerMatcher] -- " + goolMethod + " <- ");
			for (String inputLangMethodSignature : MethodMatchTable
					.get(goolMethod))
				System.out.print(inputLangMethodSignature + " ");
			System.out.println();
		}
	}

	public static boolean copyFile(File source, File dest){
		try{
			// Declaration et ouverture des flux
			java.io.FileInputStream sourceFile = new java.io.FileInputStream(source);
	 
			try{
				java.io.FileOutputStream destinationFile = null;
	 
				try{
					destinationFile = new FileOutputStream(dest);
	 
					// Lecture par segment de 0.5Mo 
					byte buffer[] = new byte[512 * 1024];
					int nbLecture;
	 
					while ((nbLecture = sourceFile.read(buffer)) != -1){
						destinationFile.write(buffer, 0, nbLecture);
					}
				} finally {
					destinationFile.close();
				}
			} finally {
				sourceFile.close();
			}
		} catch (IOException e){
			e.printStackTrace();
			return false; // Erreur
		}
	 
		return true; // Résultat OK  
	}
}