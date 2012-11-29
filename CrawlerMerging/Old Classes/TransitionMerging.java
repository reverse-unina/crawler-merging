package com.marcello.merge;

import java.io.File;
import java.io.IOException;

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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TransitionMerging {

	private Document xmlDocument;
	private String xmlFilePath;
	
	public TransitionMerging(){
		xmlDocument=null;
	}
	
	public void calculate(String file){
		
		xmlDocument = importXmlDocument(file);
		
		Element root = xmlDocument.getDocumentElement();
		
		NodeList nodeList = root.getElementsByTagName("TRANSITION");
		
		merge(nodeList);
		
		PrintGuiTreeOnXmlFile(xmlDocument, file.replace(".xml", "_final.xml"));
		
	}
	
	public Document importXmlDocument(String file){
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		Document doc = null;
		try {
			doc = builder.parse(file);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return doc;
	}
	
	public void merge(NodeList nodeList){
		
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
	
	public void PrintGuiTreeOnXmlFile(Document doc, String filename) {
	    try {
	    	String separator = System.getProperty("file.separator");
	        // Prepare the DOM document for writing
	        Source source = new DOMSource(doc);

	        // Prepare the output file
	        File file = new File(filename);
	        Result result = new StreamResult(file);

	        // Write the DOM document to the file
	        Transformer xformer = TransformerFactory.newInstance().newTransformer();
	        xformer.setOutputProperty(javax.xml.transform.OutputKeys.DOCTYPE_PUBLIC, "SESSION");
	        xformer.setOutputProperty(javax.xml.transform.OutputKeys.DOCTYPE_SYSTEM, System.getProperty("user.dir")+separator+"guitree.dtd");
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
