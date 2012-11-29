package com.marcello.merge;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.nofatclips.androidtesting.model.*;
import com.nofatclips.androidtesting.guitree.*;

public class ActivityExtractor{

	public ActivityExtractor()
	{
	}
	
	public ActivityExtractor(StreamController stream) 
	{
		  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		  DocumentBuilder builder = null;
		  try
		  {
			  factory.setValidating(false);
			  factory.setIgnoringElementContentWhitespace(true);
			  builder = factory.newDocumentBuilder();			
		  }
		  catch (ParserConfigurationException e)
		  {
			e.printStackTrace();
		  }
		  
		  Document doc = null;
		  	
		  while(doc==null){
			  try {
				  doc = builder.parse(stream.getStream());
			  } catch (SAXException e) {
				  System.out.println("An error occured :" + e +"\nThe source file doesn't have the right format." +
				  		"\n\nTrying to modify the source file\n");
				  doc = null;
				  String newFilePath = fixFile(stream.getFile());
				  stream = new StreamController(newFilePath);
				  System.gc();
			  } catch (IOException e) {
				  System.out.println("An error occured :" + e);
			  }			  
		  }
		  
		  Element first = doc.getDocumentElement();
		  
		  NodeList list = first.getChildNodes();
		  
		  for(int i=0; i<list.getLength(); i++)
		  {	
			  TestCaseActivity toAdd = new TestCaseActivity((Element)list.item(i)); //put the XML Element(Activity) in a TestCaseActivity object
			  if(list.item(i).getFirstChild().getNodeName()=="DESCRIPTION"&&list.item(i).getFirstChild().hasChildNodes())	//check if XML Element has a Description (then some Widgets)
			  {
				  NodeList widgetList = list.item(i).getFirstChild().getChildNodes(); //get all the widgets
				  for(int j=0; j<widgetList.getLength(); j++)	//iterate over the widgets list
				  {
					  TestCaseWidget widget = new TestCaseWidget((Element)widgetList.item(j));	//put the XML Element(Widget) in a TestCaseWidget object
					  toAdd.addWidget(widget);	//Add the widget to the Activity object
				  }
			  }				  
			  activities.add(toAdd);	//Store the element in a vector
		  }
		  
	}

	private String fixFile(File file)
	{
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			System.out.println("An exception occurred: "+ e);
		}
		
		BufferedWriter writer = null;
		String path = null;
		
		if (file.getAbsolutePath().contains(".xml"))
				path = file.getAbsolutePath().replace(".xml", "_new.xml");
		
		try {
			writer = new BufferedWriter(new FileWriter(path));
		} catch (IOException e) {
			System.out.println("An exception occurred: "+ e);
		}
		
		try {
			String[] parts = reader.readLine().split("\\?>");
			writer.write(parts[0] + "?>\n<RADICE>" + parts[1]);
			writer.flush();
			while(reader.read()!=-1)
			{
				String stringa = reader.readLine();
				stringa = stringa.split("\\?>")[1];
				writer.write(stringa);
				writer.flush();
			}
			writer.write("</RADICE>");
			reader.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Source file modified: " + path + System.getProperty("line.separator"));

		return path;		
	}	
	
	public ActivityExtractor(List<ActivityState> activity)
	{
		activities.addAll(activity);		
	}
	
	public List<ActivityState> getActivities() {
		if(activities!=null)
			return activities;
		else{
			System.out.println("Impossible to obtain the extracted Activities List. It returns \"null\"");
			return null;
		}
	}
	
	public void printActivities()
	{
		Iterator<ActivityState> iterator = activities.iterator();
		
		while(iterator.hasNext())
		{
			ActivityState a = iterator.next();
			System.out.println("ID: " + a.getId());
			System.out.println("Name: " + a.getName());
			System.out.println("ScreenShoot: " + a.getScreenshot());
			System.out.println("Title: " + a.getTitle());
			System.out.println("Description id: " + a.getDescriptionId());
			System.out.println("Unique_id: " + a.getUniqueId() + "\n");		
		}
	}
	
	public void printActivities(List<ActivityState> list)
	{
		Iterator<ActivityState> iterator = list.iterator();
		
		while(iterator.hasNext())
		{
			ActivityState a = iterator.next();
			System.out.println("ID: " + a.getId());
			System.out.println("Name: " + a.getName());
			System.out.println("ScreenShoot: " + a.getScreenshot());
			System.out.println("Title: " + a.getTitle());
			System.out.println("Description id: " + a.getDescriptionId());
			System.out.println("Unique_id: " + a.getUniqueId() + "\n");		
		}		
	}

	public void printActivitiesOnFile(List<ActivityState> list, String path){
		
		File file = new File(path);
		BufferedWriter stream = null;
		try {
			stream = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			System.out.println("An error occurred: "+e);
		}
		
		Iterator<ActivityState> iterator = list.iterator();
		
		while(iterator.hasNext())
		{
			ActivityState a = iterator.next();
			
			try{
			stream.write("\nID: " + a.getId());
			stream.write("\nName: " + a.getName());
			stream.write("\nScreenShoot: " + a.getScreenshot());
			stream.write("\nTitle: " + a.getTitle());
			stream.write("\nDescription id: " + a.getDescriptionId());
			stream.write("\nUnique_id: " + a.getUniqueId() + "\n");
			}
			catch(IOException e){
				System.out.println("An error occurred: "+e);
			}
		}
		try {
			stream.close();
		} catch (IOException e) {
			System.out.println("An error occurred: "+e);
		}
		
		System.out.println("A new File created: " + path);
	}
	
	private List<ActivityState> activities = new Vector<ActivityState>();
}
