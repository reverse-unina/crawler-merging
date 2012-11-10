package com.marcello.merge;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nofatclips.androidtesting.guitree.TestCaseActivity;
import com.nofatclips.androidtesting.guitree.TestCaseWidget;
import com.nofatclips.androidtesting.model.ActivityState;

public class ActivityManager {

	/* Internal fields */
	
	List<ActivityState> activities;
	String xmlFilePath;
	
	ActivityManager(){
		activities = null;
		xmlFilePath = null;
	}
	
	public List<ActivityState> ActivityExtractor(String filePath){

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
		InputStream()
		
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

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
