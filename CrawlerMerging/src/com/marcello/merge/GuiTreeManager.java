package com.marcello.merge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
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

	public void ReplaceActivitiesOnGuiTree(String guiTree, List<ActivityState> activities)
	{
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

		FileInputStream stream = null;

		doc= null;

		try {
			stream = new FileInputStream(guiTree);
			doc = builder.parse(stream);
			stream.close();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

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

	public void TransitionMerging(){
		
		NodeList nodeList = doc.getDocumentElement().getElementsByTagName("TRANSITION");
		
		Hashtable<String, List<Element>> table = new Hashtable<String, List<Element>>();

		System.out.print("Merging events...");

		for(int i=0; i<nodeList.getLength(); i++){	//scorre tutti i nodi TRANSITION

			Element transition = (Element)nodeList.item(i);	//Per ogni nodo TRANSITION

			Element start_activity = (Element)transition.getElementsByTagName("START_ACTIVITY").item(0);	//Estrae il nodo start
			Element final_activity = (Element)transition.getElementsByTagName("FINAL_ACTIVITY").item(0);	//Estrae il nodo final
			Element event = (Element)transition.getElementsByTagName("EVENT").item(0);	//Estrae il nodo event

			String key = start_activity.getAttribute("id")+final_activity.getAttribute("id");	//crea la chiave composta dagli 'id' di start e final

			//System.out.println("key: "+key);

			if(table.containsKey(key)){	//Se la tabella già contiene l'id ricavato
				Iterator<Element> iterator = table.get(key).iterator();	//ricava un iteratore sulle transition relative alla chiave
				boolean find = false;
				while(iterator.hasNext()&&find==false){	//scorre le transition relative alla chiave
					Element event_table = (Element)iterator.next().getElementsByTagName("EVENT").item(0);	//per ogni transition relativa alla chiave 
					if (event_table.getAttribute("type").equals(event.getAttribute("type"))){	//se il tipo di evento della transition nella tabella coincide con quello della transition in esame
						event.setAttribute("id", event_table.getAttribute("id"));	//modifica il campo id della transition in esame con quello della transition in tabella
						find=true;
					}
				}
				if(find==false)	//se nella tabella non è presente un evento con lo stesso tipo di quello in esame
					table.get(key).add(transition);	//inserisci la transition in tabella 
			}
			else{	//se la tabella non contiene l'id ricavato
				List<Element> list = new Vector<Element>();	//crea un vettore di Elementi
				list.add(transition);	//aggiungi al vettore la transition in esame
				table.put(key, list);	//associa il vettore alla tabella
			}
		}
		System.out.println("Done");
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
			manager.ReplaceActivitiesOnGuiTree(args[0], aManager.getActivities());
			manager.PrintGuiTreeOnXmlFile(manager.doc, args[0].replace(".xml", "_intermediate.xml"));
			manager.TransitionMerging();
			manager.PrintGuiTreeOnXmlFile(manager.doc, args[0].replace(".xml", "_merged.xml"));
			System.out.println("***FINISHED***");
		}

	}

}
