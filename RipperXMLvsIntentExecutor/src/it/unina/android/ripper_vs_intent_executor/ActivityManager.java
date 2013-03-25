package it.unina.android.ripper_vs_intent_executor;

import static com.nofatclips.androidtesting.model.SimpleType.BUTTON;
import static com.nofatclips.androidtesting.model.SimpleType.EDIT_TEXT;
import static com.nofatclips.androidtesting.model.SimpleType.IMAGE_VIEW;
import static com.nofatclips.androidtesting.model.SimpleType.LIST_VIEW;
import static com.nofatclips.androidtesting.model.SimpleType.MENU_VIEW;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

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
import org.w3c.dom.NamedNodeMap;
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

public class ActivityManager extends Thread{

	/* Internal fields */

	private List<ActivityState> activities;
	private String xmlFilePath;
	private Document doc;


	@Override
	public void run() {
		super.run();
		if(this.activities!=null)
			this.ActivityMerging();
		if(this.doc!=null&&this.activities!=null){
			this.updateDocument(this.doc, this.activities);
			this.PrintActivitiesOnXmlFile(this.doc, this.xmlFilePath.replace(".xml", "_merged.xml"));
		}
		else{
			System.out.println("ActivityManager class not initialized correctly. doc: "+this.doc+"; activities: "+this.activities);
		}
	}

	/***CONSTRUCTORS****/

	ActivityManager(){
		super();
		this.activities = null;
		this.xmlFilePath = null;
		this.doc = null;
	}

	ActivityManager(List<ActivityState> activities)
	{
		super();
		this.xmlFilePath = null;
		this.doc = null;
		this.activities.addAll(activities);
	}

	ActivityManager(String activitiesFilePath){
		super();
		this.activities = this.ActivityExtractor(activitiesFilePath);
		this.xmlFilePath = activitiesFilePath;
		setDocByXml();
	}

	/***UTILITY FUNCTIONS***/

	public List<ActivityState> ActivityExtractor(String filePath){

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try
		{
			factory.setValidating(false);
			//factory.setIgnoringElementContentWhitespace(true);
			builder = factory.newDocumentBuilder();			
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}

		FileInputStream stream = null;
		Document doc = null;

		while(doc==null){
			try {
				if(new File(filePath.replace(".xml", "_fixed.xml")).exists()){ //activities file's fix has been already performed
					stream = new FileInputStream(filePath.replace(".xml", "_fixed.xml"));}
				else
					stream = new FileInputStream(filePath); //parsing will generate exception if file's format is not as expected

				doc = builder.parse(stream);

				stream.close();

			} catch (SAXException e) {
				System.out.println("\nThe source file doesn't have the right format." +
						"\n\nTrying to modify the source file\n");
				doc = null;

				filePath = fixFile(new File(filePath));

			} catch (IOException e) {
				System.out.println("An error occured :" + e);
				break;
			}
		}	  


		Element first = doc.getDocumentElement();

		NodeList list = first.getChildNodes();

		List<ActivityState> tempActivities = new Vector<ActivityState>();

		for(int i=0; i<list.getLength(); i++)
		{	

			TestCaseActivity toAdd = new TestCaseActivity((org.w3c.dom.Element)list.item(i)); //put the XML Element(Activity) in a TestCaseActivity object
			if(list.item(i).getFirstChild().getNodeName()=="DESCRIPTION"&&list.item(i).getFirstChild().hasChildNodes())	//check if XML Element has a Description (then some Widgets)
			{
				NodeList widgetList = list.item(i).getFirstChild().getChildNodes(); //get all the widgets
				for(int j=0; j<widgetList.getLength(); j++)	//iterate over the widgets list
				{
					TestCaseWidget widget = new TestCaseWidget((Element)widgetList.item(j));	//put the XML Element(Widget) in a TestCaseWidget object
					toAdd.addWidget(widget);	//Add the widget to the Activity object
				}
			}				  
			tempActivities.add(toAdd);	//Store the element in a vector
		}

		return tempActivities;
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
			path = file.getAbsolutePath().replace(".xml", "_fixed.xml");

		try {
			writer = new BufferedWriter(new FileWriter(path));
		} catch (IOException e) {
			System.out.println("An exception occurred: "+ e);
		}

		try {
			String[] parts = reader.readLine().split("\\?>");
			writer.write(parts[0] + "?><RADICE>" + parts[1]);
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

	public void ActivityMerging()
	{
		int count = activities.size();

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

		Object[] comparatorParamenters = getComparatorType(System.getProperty("user.dir") + File.separator +"files"+ File.separator +"merging_prefs.xml");
		int type = 0;

		try{
			type = (Integer)comparatorParamenters[0];
		}catch(NullPointerException e){
			System.out.println(e);
		}

		Comparator comparator = null;
		String[] parameters = new String[comparatorParamenters.length-1];

		switch(type){
		case 1:
			for(int i=1; i<comparatorParamenters.length;i++)
				parameters[i-1] = (String)comparatorParamenters[i];
			comparator = new CustomWidgetsDeepComparator(parameters);
			break;
		case 2:
			for(int i=1; i<comparatorParamenters.length;i++)
				parameters[i-1] = (String)comparatorParamenters[i];
			comparator = new CustomWidgetsComparator(parameters);
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

	private Object[] getComparatorType(String file) throws NullPointerException
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

		NodeList entries = first.getElementsByTagName("entry");

		String comparatorType = "Null";
		List<String> widgets = new Vector<String>();

		for(int i=0; i<entries.getLength();i++){

			NamedNodeMap attributes = entries.item(i).getAttributes();

			//memorizza il tipo di comparatore scelto
			if(attributes.getNamedItem("key").getNodeValue().equals("TYPE"))
				comparatorType = (String)attributes.getNamedItem("value").getNodeValue();
			//memorizza (se ce ne sono) i widget su cui fare il confronto
			else if(!attributes.getNamedItem("value").getNodeValue().equals(""))
				widgets.add((String)attributes.getNamedItem("value").getNodeValue());

		}

		Object[] toReturn;

		if (comparatorType.equals("CustomWidgetsDeepComparator"))
		{
			toReturn = new Object[widgets.size()+1];
			toReturn[0]=1;
			for(int i=1; i<=widgets.size();i++)
				toReturn[i] = widgets.get(i-1);
			return toReturn;
		}
		if (comparatorType.equals("CustomWidgetsComparator"))
		{
			toReturn = new Object[widgets.size()+1];
			toReturn[0]=2;
			for(int i=1; i<=widgets.size();i++)
				toReturn[i] = widgets.get(i-1);
			return toReturn;
		}
		if (comparatorType.equals("NameComparator"))
		{
			toReturn = new Object[1];
			toReturn[0]=3;
			return toReturn;
		}
		if (comparatorType.equals("ButtonComparator"))
		{
			toReturn = new Object[1];
			toReturn[0]=4;
			return toReturn;
		}
		if (comparatorType.equals("EditTextComparator"))
		{
			toReturn = new Object[1];
			toReturn[0]=5;
			return toReturn;
		}

		toReturn = new Object[1];
		toReturn[0]=0;
		return toReturn;
	}
	public boolean setDocByXml(){
		if (doc==null){
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

			String filePath = this.xmlFilePath;
			while(doc==null){
				try {
					FileInputStream stream = new FileInputStream(filePath);
					doc = builder.parse(stream);
					stream.close();
				} catch (SAXException e) {
					//e.printStackTrace();
					doc=null;
					filePath = filePath.replace(".xml", "_fixed.xml");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		return false;
	}

	public void updateDocument(Document _doc, List<ActivityState> _activities)
	{
		Element root = _doc.getDocumentElement();

		NodeList list = root.getChildNodes();

		for(int i=0; i<_activities.size();i++)
		{
			for(int j=0; j<list.getLength(); j++)
			{
				if(_activities.get(i).getUniqueId().equals(list.item(j).getAttributes().getNamedItem("unique_id").getLocalName()))
					list.item(j).getAttributes().getNamedItem("id").setNodeValue(_activities.get(i).getId());
			}
		}

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
			//xformer.setOutputProperty(javax.xml.transform.OutputKeys.DOCTYPE_PUBLIC, "SESSION");
			//xformer.setOutputProperty(javax.xml.transform.OutputKeys.DOCTYPE_SYSTEM, System.getProperty("user.dir")+File.separator+"guitree.dtd");
			xformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
			xformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
		} catch (TransformerException e) {
		}

		System.out.println("A new File created: "+filename);

		xmlFilePath = filename;
	}


	/***GETTERS AND SETTERS***/

	public List<ActivityState> getActivities()
	{
		if(activities!=null)
			return activities;
		else{
			System.out.println("Impossible to obtain the extracted Activities List. It is \"null\"");
			return null;
		}
	}

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

	public void setActivities(List<ActivityState> activities) {
		this.activities = activities;
	}


	/***MAIN***/	

	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();

		if(args[0].equals("help"))
			System.out.println("Parameter should be 'activities.xml' 's path");
		else{

			ActivityManager manager = new ActivityManager(args[0]);
			manager.start();
		}
		System.out.println("Elaboration done. Time elapsed (sec): " + (int)Math.floor((System.currentTimeMillis() - startTime)/1000));		
	}

}
