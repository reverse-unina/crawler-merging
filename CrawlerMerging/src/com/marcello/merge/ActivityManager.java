package com.marcello.merge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

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
import com.nofatclips.crawler.model.Comparator;
import com.nofatclips.crawler.strategy.comparator.ButtonComparator;
import com.nofatclips.crawler.strategy.comparator.CustomWidgetsComparator;
import com.nofatclips.crawler.strategy.comparator.CustomWidgetsDeepComparator;
import com.nofatclips.crawler.strategy.comparator.EditTextComparator;
import com.nofatclips.crawler.strategy.comparator.NameComparator;
import com.nofatclips.crawler.strategy.comparator.NullComparator;

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
		FileInputStream stream = null;

		while(doc==null){
			try {
				stream = new FileInputStream(filePath);
				doc = builder.parse(stream);
			} catch (SAXException e) {
				System.out.println("An error occured :" + e +"\nThe source file doesn't have the right format." +
						"\n\nTrying to modify the source file\n");
				doc = null;
				String newFilePath = fixFile(new File(filePath));
				try {
					stream = new FileInputStream(newFilePath);
				} catch (FileNotFoundException e1) {
					System.out.println("An error occured :" + e1);
				}
				//System.gc();
			} catch (IOException e) {
				System.out.println("An error occured :" + e);
			}
		}	  


		Element first = doc.getDocumentElement();

		NodeList list = first.getChildNodes();

		activities = new Vector<ActivityState>();

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

		return activities;
	}

	public List<ActivityState> ActivityExtractor(List<ActivityState> activity)
	{
		activities.addAll(activity);
		return activities;
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

	public void ActivityMerging(String file)
	{
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

	public List<ActivityState> getActivities()
	{
		if(activities!=null)
			return activities;
		else{
			System.out.println("Impossible to obtain the extracted Activities List. It is \"null\"");
			return null;
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

	public void printActivities()
	{
		printActivities(activities);
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
