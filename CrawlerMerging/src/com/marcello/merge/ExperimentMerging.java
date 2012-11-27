package com.marcello.merge;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import com.unina.tata.filemanager.FileManagerFSM;

public class ExperimentMerging {

	
	public static void main(String[] args) {
				
		long startTime = System.currentTimeMillis();
		
		String activities = args[0] + File.separator + "activities.xml";
		
		ActivityManager aManager = new ActivityManager();
		
		aManager.ActivityExtractor(activities);
		aManager.ActivityMerging();
		aManager.updateDocument(aManager.getDoc(), aManager.getActivities());
		aManager.PrintActivitiesOnXmlFile(aManager.getDoc(), activities.replace(".xml", "_merged.xml"));
		
		String guitree = args[0] + File.separator + "guitree.xml";
				
		GuiTreeManager gtManager = new GuiTreeManager();
		
		gtManager.ReplaceActivitiesOnGuiTree(guitree, aManager.getActivities());
		gtManager.PrintGuiTreeOnXmlFile(gtManager.getDoc(), guitree.replace(".xml", "_intermediate.xml"));
		gtManager.TransitionMerging();
		gtManager.PrintGuiTreeOnXmlFile(gtManager.getDoc(), guitree.replace(".xml", "_merged.xml"));

		GetDotFile(gtManager.getXmlFilePath());

		System.out.println("Elaboration done. Time elapsed (sec): " + (int)Math.floor((System.currentTimeMillis() - startTime)/1000));		
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
