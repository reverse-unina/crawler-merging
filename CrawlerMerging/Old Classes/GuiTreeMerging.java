package com.marcello.merge;

import com.marcello.merge.StreamController;
import com.marcello.merge.ActivityMerging;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.nofatclips.androidtesting.guitree.TestCaseActivity;
import com.nofatclips.androidtesting.guitree.TestCaseWidget;
import com.nofatclips.androidtesting.model.ActivityState;

public class GuiTreeMerging {
	
	private String xmlFilePath;
	
	public GuiTreeMerging(){}
	
	public GuiTreeMerging(String guiTree, String activities)
	{
		StreamController stream = null;
		
		try{
			 stream = new StreamController(guiTree);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException e)
		{
			System.out.println("An exception occured: "+ e);
		}
				
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try
		{
			factory.setValidating(true);
			factory.setIgnoringElementContentWhitespace(true);
			builder = factory.newDocumentBuilder();			
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		 
		Document doc= null;
		  
		try {
			doc = builder.parse(stream.getStream());
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		  
		stream.closeStream();
		
		Element first = doc.getDocumentElement();
		
		ActivityMerging activityMerging = new ActivityMerging(activities);
		
		ReplaceEntries(first,activityMerging.getActivities());
		
		//PrintActivitiesOnFile(first, guiTree.split("guitree.xml")[0]+"guitree.txt");
		
		PrintGuiTreeOnXmlFile(doc, guiTree.split("guitree.xml")[0]+"guitree_new.xml");
		
	}

	public void ReplaceEntries(Element guiTree, List<ActivityState> activities)
	{
		NodeList startList = guiTree.getElementsByTagName("START_ACTIVITY");
		NodeList finalList = guiTree.getElementsByTagName("FINAL_ACTIVITY");
		
		
		for(int i=0; i<activities.size(); i++)
		{
			ActivityState activity = activities.get(i);
			String unique = activity.getUniqueId();
			String id = activity.getId();
			if(unique.equals(id))
			{
				continue;
			}
			for(int j=0; j<startList.getLength(); j++)
			{
				Element e = (Element)startList.item(j);
		
				if(e.getAttribute("unique_id").equals(unique))
				{
					e.setAttribute("id", id);
				}
			}
			for(int j=0; j<finalList.getLength(); j++)
			{
				Element e = (Element)finalList.item(j);
		
				if(e.getAttribute("unique_id").equals(unique))
				{
					e.setAttribute("id", id);
				}
			}
		}	
	}
	
	public void PrintActivities(Element guiTree){
		NodeList startList = guiTree.getElementsByTagName("START_ACTIVITY");
		NodeList finalList = guiTree.getElementsByTagName("FINAL_ACTIVITY");
		
		List<ActivityState> list = new Vector<ActivityState>();
		
		for(int i=0; i<startList.getLength(); i++)
		  {	
			  TestCaseActivity toAdd = new TestCaseActivity((Element)startList.item(i)); //put the XML Element(Activity) in a TestCaseActivity object
			  if(startList.item(i).getFirstChild().getNodeName()=="DESCRIPTION"&&startList.item(i).getFirstChild().hasChildNodes())	//check if XML Element has a Description (then some Widgets)
			  {
				  NodeList widgetList = startList.item(i).getFirstChild().getChildNodes(); //get all the widgets
				  for(int j=0; j<widgetList.getLength(); j++)	//iterate over the widgets list
				  {
					  TestCaseWidget widget = new TestCaseWidget((Element)widgetList.item(j));	//put the XML Element(Widget) in a TestCaseWidget object
					  toAdd.addWidget(widget);	//Add the widget to the Activity object
				  }
			  }				  
			  list.add(toAdd);	//Store the element in a vector
		  }
		
		for(int i=0; i<finalList.getLength(); i++)
		  {	
			  TestCaseActivity toAdd = new TestCaseActivity((Element)finalList.item(i)); //put the XML Element(Activity) in a TestCaseActivity object
			  if(finalList.item(i).getFirstChild().getNodeName()=="DESCRIPTION"&&finalList.item(i).getFirstChild().hasChildNodes())	//check if XML Element has a Description (then some Widgets)
			  {
				  NodeList widgetList = finalList.item(i).getFirstChild().getChildNodes(); //get all the widgets
				  for(int j=0; j<widgetList.getLength(); j++)	//iterate over the widgets list
				  {
					  TestCaseWidget widget = new TestCaseWidget((Element)widgetList.item(j));	//put the XML Element(Widget) in a TestCaseWidget object
					  toAdd.addWidget(widget);	//Add the widget to the Activity object
				  }
			  }				  
			  list.add(toAdd);	//Store the element in a vector
		  }
		
		new ActivityExtractor().printActivities(list);
		
	}
	
	public void PrintActivitiesOnFile(Element guiTree, String path){
		NodeList startList = guiTree.getElementsByTagName("START_ACTIVITY");
		NodeList finalList = guiTree.getElementsByTagName("FINAL_ACTIVITY");
		
		List<ActivityState> list = new Vector<ActivityState>();
		
		for(int i=0; i<startList.getLength(); i++)
		  {	
			  TestCaseActivity toAdd = new TestCaseActivity((Element)startList.item(i)); //put the XML Element(Activity) in a TestCaseActivity object
			  if(startList.item(i).getFirstChild().getNodeName()=="DESCRIPTION"&&startList.item(i).getFirstChild().hasChildNodes())	//check if XML Element has a Description (then some Widgets)
			  {
				  NodeList widgetList = startList.item(i).getFirstChild().getChildNodes(); //get all the widgets
				  for(int j=0; j<widgetList.getLength(); j++)	//iterate over the widgets list
				  {
					  TestCaseWidget widget = new TestCaseWidget((Element)widgetList.item(j));	//put the XML Element(Widget) in a TestCaseWidget object
					  toAdd.addWidget(widget);	//Add the widget to the Activity object
				  }
			  }				  
			  list.add(toAdd);	//Store the element in a vector
		  }
		
		for(int i=0; i<finalList.getLength(); i++)
		  {	
			  TestCaseActivity toAdd = new TestCaseActivity((Element)finalList.item(i)); //put the XML Element(Activity) in a TestCaseActivity object
			  if(finalList.item(i).getFirstChild().getNodeName()=="DESCRIPTION"&&finalList.item(i).getFirstChild().hasChildNodes())	//check if XML Element has a Description (then some Widgets)
			  {
				  NodeList widgetList = finalList.item(i).getFirstChild().getChildNodes(); //get all the widgets
				  for(int j=0; j<widgetList.getLength(); j++)	//iterate over the widgets list
				  {
					  TestCaseWidget widget = new TestCaseWidget((Element)widgetList.item(j));	//put the XML Element(Widget) in a TestCaseWidget object
					  toAdd.addWidget(widget);	//Add the widget to the Activity object
				  }
			  }				  
			  list.add(toAdd);	//Store the element in a vector
		  }
		
		new ActivityExtractor().printActivitiesOnFile(list, path);
		
	}

	public void PrintGuiTreeOnXmlFile(Document doc, String filename) {
	    try {
	    	// Prepare the DOM document for writing
	        Source source = new DOMSource(doc);

	        // Prepare the output file
	        File file = new File(filename);
	        Result result = new StreamResult(file);

	        // Write the DOM document to the file
	        Transformer xformer = TransformerFactory.newInstance().newTransformer();
	        xformer.setOutputProperty(javax.xml.transform.OutputKeys.DOCTYPE_PUBLIC, "SESSION");
	        xformer.setOutputProperty(javax.xml.transform.OutputKeys.DOCTYPE_SYSTEM, System.getProperty("user.dir")+File.separator+"guitree.dtd");
	        xformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
	        xformer.transform(source, result);
	    } catch (TransformerConfigurationException e) {
	    } catch (TransformerException e) {
	    }
	    
	    System.out.println("A new File created: "+filename);
	    
	    xmlFilePath = filename;
	}
	
	public String getFilePath(){
		return xmlFilePath;
	} 
}
