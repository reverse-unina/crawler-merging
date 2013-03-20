package com.marcello.merge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

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
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nofatclips.androidtesting.model.ActivityState;
import com.unina.tata.filemanager.FileManagerFSM;

public class GuiTreeManager extends Observable implements Runnable{

	private String xmlFilePath;
	private Document doc;
	private ActivityManager aManager;
	public static final int ACTIVITIES = 0;
	public static final int GUITREE = 1;

	@Override
	public void run() {
		this.mergeGuitree();
	}

	/***CONSTRUCTORS****/

	public GuiTreeManager() {
		super();
		this.xmlFilePath = null;
		this.doc = null;
		this.aManager = null;
	}

	public GuiTreeManager(Document doc, String filePath) {
		super();
		this.xmlFilePath = filePath;
		this.doc = doc;
		this.aManager = new ActivityManager(filePath.replace("guitree.xml", "activities.xml"));
	}

	public GuiTreeManager(Document doc) {
		super();
		this.xmlFilePath = null;
		this.doc = doc;
		this.aManager = null;
	}
	
	public GuiTreeManager(String filePath) {
		super();
		this.xmlFilePath = filePath;
		this.setDocByXml();
		this.aManager = new ActivityManager(filePath.replace("guitree.xml", "activities.xml"));
	}


	/***UTILITY FUNCTIONS***/

	public void mergeGuitree(){
		this.aManager.start();

		try {
			this.aManager.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(this.countObservers()!=0){
			this.setChanged();
			this.notifyObservers(GuiTreeManager.ACTIVITIES);}
		this.ReplaceActivitiesOnGuitree(this.doc.getDocumentElement(), aManager.getActivities());
		this.PrintGuiTreeOnXmlFile(this.doc, this.xmlFilePath.replace(".xml", "_intermediate.xml"));
		this.TransitionMerging();
		this.PrintGuiTreeOnXmlFile(this.doc, this.xmlFilePath.replace(".xml", "_merged.xml"));
		if(this.countObservers()!=0){
			this.setChanged();
			this.notifyObservers(GuiTreeManager.GUITREE);}
		this.GetDotFile(this.xmlFilePath.replace(".xml", "_merged.xml"));
	}

	//set this.doc depending on this.xmlFilePath
	public boolean setDocByXml(){
		if (doc==null){
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = null;
			try
			{
				factory.setValidating(Settings.validation);
				factory.setIgnoringElementContentWhitespace(true);
				builder = factory.newDocumentBuilder();			
			}
			catch (ParserConfigurationException e)
			{
				e.printStackTrace();
			}

			try {
				FileInputStream stream = new FileInputStream(this.xmlFilePath);
				doc = builder.parse(stream);
				stream.close();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	public void ReplaceActivitiesOnGuitree(Element guiTree, List<ActivityState> activities)
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

		//xmlFilePath = filename;
	}

	public void TransitionMerging(){

		NodeList nodeList = doc.getDocumentElement().getElementsByTagName("TRANSITION");

		Hashtable<String, List<Element>> table = new Hashtable<String, List<Element>>();

		for(int i=0; i<nodeList.getLength(); i++){	//scorre tutti i nodi TRANSITION

			Element transition = (Element)nodeList.item(i);	//Per ogni nodo TRANSITION

//			Element start_activity = (Element)transition.getElementsByTagName("START_ACTIVITY").item(0);	//Estrae il nodo start (spreco risorse)
//			Element final_activity = (Element)transition.getElementsByTagName("FINAL_ACTIVITY").item(0);	//Estrae il nodo final (spreco risorse)
			Element event = (Element)transition.getElementsByTagName("EVENT").item(0);	//Estrae il nodo event

			String key = ((Element) transition.getElementsByTagName("START_ACTIVITY").item(0)).getAttribute("id")+((Element) transition.getElementsByTagName("FINAL_ACTIVITY").item(0)).getAttribute("id");	//crea la chiave composta dagli 'id' di start e final

			//System.out.println("key: "+key);

			if(table.containsKey(key)){	//Se la tabella già contiene l'id ricavato
				Iterator<Element> iterator = table.get(key).iterator();	//ricava un iteratore sulle transition relative alla chiave
				boolean find = false;
				while(iterator.hasNext()&&find==false){	//scorre le transition relative alla chiave
					Element event_table = (Element)iterator.next();	//per ogni evento relativa alla chiave 
					if (event_table.getAttribute("type").equals(event.getAttribute("type"))){	//se il tipo di evento nella tabella coincide con quello in esame
						event.setAttribute("id", event_table.getAttribute("id"));	//modifica il campo id dell'evento in esame con quello dell'evento in tabella
						find=true;
					}
				}
				if(find==false)	//se nella tabella non è presente un evento con lo stesso tipo di quello in esame
					table.get(key).add(event);	//inserisci l'evento in tabella 
			}
			else{	//se la tabella non contiene l'id ricavato
				List<Element> list = new Vector<Element>();	//crea un vettore di Elementi
				list.add(event);	//aggiungi al vettore la transition in esame
				table.put(key, list);	//associa il vettore alla tabella
			}
		}
		System.out.println("Transitions merged");
	}

	public void GetDotFile(String path){

		String[] string = new String[2];

		string[0] = path;
		string[1] = path.replace(new File(path).getName(), "guitree");

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
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
		}

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


	/**
	 * @return the aManager
	 */
	public ActivityManager getaManager() {
		return aManager;
	}

	/***MAIN***/

	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();

		if(args[0].equals("help")||args==null)
			System.out.println("Parameter should be 'guitree.xml' 's path");
		else{
			GuiTreeManager manager = new GuiTreeManager(args[0]);
			manager.run();
		}
		System.out.println("Elaboration done. Time elapsed (sec): " + (int)Math.floor((System.currentTimeMillis() - startTime)/1000));

	}

}
