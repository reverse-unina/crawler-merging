package com.marcello.merge;

import com.marcello.merge.StreamController;

import com.nofatclips.androidtesting.model.ActivityState;
import com.nofatclips.crawler.model.Comparator;
import com.nofatclips.crawler.strategy.comparator.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

import org.w3c.dom.*;
import org.xml.sax.SAXException;


public class ActivityMerging {

	List<ActivityState> activities;
	private String xmlFilePath;

	public ActivityMerging(String file)
	{
		StreamController stream = null;

		try{
			stream = new StreamController(file);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException e)
		{
			System.out.println("An exception occured: "+ e);
		}

		ActivityExtractor extractor = null;

		extractor = new ActivityExtractor(stream);

		stream.closeStream();

		activities = extractor.getActivities();

		int count = activities.size();

		System.out.println(count + " activities.");

		Comparator comparator = selectComparator();

		System.out.println("Using a " + comparator.getClass().toString().split("comparator.")[1] + "\n");

		for(int i=0; i<count; i++)
		{
			ListIterator<ActivityState> iterator = activities.listIterator(i);
			ListIterator<ActivityState> iterator2 = activities.listIterator(i+1);
			if(iterator.hasNext())
			{
				ActivityState a = iterator.next();

				while(iterator2.hasNext())
				{
					ActivityState b = iterator2.next();
					if (comparator.compare(a,b))
					{
						b.setId(a.getId());
					}	
				}
			}
		}

	}

	public ActivityMerging(){}

	public Comparator selectComparator(){

		int type = 0;

		try{
			type = getComparatorType(System.getProperty("user.dir") + File.separator +"files"+ File.separator +"merging_prefs.xml");
		}catch(NullPointerException e){
			System.out.println(e);
		}

		Comparator comparator = null;

		switch(type){
		case 1:
			comparator = new CustomWidgetsDeepComparator();
			break;
		case 2:
			comparator = new CustomWidgetsComparator();
			break;
		case 3:
			comparator = new NameComparator();
			break;
		case 4:
			comparator = new ButtonComparator();
			break;
		case 5:
			comparator = new EditTextComparator();
			break;
		default:
			comparator = new NullComparator();
		}

		return comparator;
	}

	private int getComparatorType(String file) throws NullPointerException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;

		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}

		Document doc = null;


		try {
			doc = builder.parse(file);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("File "+ file + " not found. Create such a file that contains comparator preferences. Errore: "+e);
		}

		if (doc==null)
			throw new NullPointerException("In function getComparatorType the Document variable \"doc\" results null beacuse of the exception above. A NullComparator will be used.\n");

		Element first = doc.getDocumentElement();

		Element entry = (Element)first.getElementsByTagName("entry").item(0);

		String value = (entry.getAttribute("value"));

		if (value.equals("CustomWidgetsDeepComparator"))
			return 1;
		if (value.equals("CustomWidgetsComparator"))
			return 2;
		if (value.equals("NameComparator"))
			return 3;
		if (value.equals("ButtonComparator"))
			return 4;
		if (value.equals("EditTextComparator"))
			return 5;

		return 0;
	}

	public void PrintActivitiesOnXmlFile(Document doc, String filename) {
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

	public 	List<ActivityState> getActivities()
	{
		return activities;
	}

	public 	String getFilePath(){
		return xmlFilePath;
	}

}
