package com.marcello.merge;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import com.unina.tata.filemanager.FileManagerFSM;

public class Merge {

	
	public static void main(String[] args) {
				
		String guitree = args[0] + File.separator + "guitree.xml";
		
		String activities = args[0] + File.separator + "activities.xml";

		GuiTreeMerging a = new GuiTreeMerging(guitree,activities);

		TransitionMerging tm = new TransitionMerging();

		tm.calculate(a.getFilePath());

		GetDotFile(tm.getFilePath());

		//System.out.println("***FINISHED***");
	}
	
	
	public static void GetDotFile(String path){
		
		String[] string = new String[2];
				
		string[0] = path;
		string[1] = path.replace(path.split(File.separator)[path.split(File.separator).length-1], "guitree");
		
		try {
			FileManagerFSM.main(string);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

}
