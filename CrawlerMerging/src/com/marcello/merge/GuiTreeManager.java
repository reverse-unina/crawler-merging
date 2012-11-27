package com.marcello.merge;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nofatclips.androidtesting.model.ActivityState;

public class GuiTreeManager {

	private String xmlFilePath;
	private Document doc;

	/***CONSTRUCTOR****/

	public GuiTreeManager() {
		super();
		this.xmlFilePath = null;
		this.doc = null;
	}

	
	/***UTILITY FUNCTIONS***/
	
	public void GuiTreeMerging(String guiTree, List<ActivityState> activities)
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

		doc= null;

		try {
			doc = builder.parse(stream.getStream());
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		stream.closeStream();

		ReplaceEntries(doc.getDocumentElement(),activities);

	}

	private void ReplaceEntries(Element guiTree, List<ActivityState> activities)
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

	
	/***GETTERS AND SETTERS***/
	
	public String getXmlFilePath() {
		return xmlFilePath;
	}

	public void setXmlFilePath(String xmlFilePath) {
		this.xmlFilePath = xmlFilePath;
	}
	
	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}

	
	/***MAIN***/
	
	public static void main(String[] args) {
		if(args[0].equals("help"))
			System.out.println("Parameters should be guitree.xml and activities.xml");
		else{
			GuiTreeManager manager = new GuiTreeManager();
			ActivityManager aManager = new ActivityManager();
			aManager.ActivityExtractor(args[1]);
			aManager.ActivityMerging();
			aManager.updateDocument(aManager.getDoc(), aManager.getActivities());
			aManager.PrintActivitiesOnXmlFile(aManager.getDoc(), args[1].replace(".xml", "_merged.xml"));
			manager.GuiTreeMerging(args[0], aManager.getActivities());
			manager.PrintGuiTreeOnXmlFile(manager.doc, args[0].replace(".xml", "_merged.xml"));
			System.out.println("***FINISHED***");
		}

	}

}
