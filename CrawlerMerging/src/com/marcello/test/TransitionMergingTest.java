package com.marcello.test;

import static org.junit.Assert.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marcello.merge.TransitionMerging;

public class TransitionMergingTest {

	TransitionMerging test = null;
	String xmlTestFile = "/Users/Marcello/Dropbox/GEXF Traslator/Test Random/AardDict/Test1/files/1/guitree_new.xml";
	Document xmlTestDocument = null;
	
	@Test
	public void testTransitionMerging() {
		test = new TransitionMerging();
	}

	@Test
	public void testCalculate() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testMerge() {
		
		testTransitionMerging();
		
		xmlTestDocument = test.importXmlDocument(xmlTestFile);
		
		Element root = xmlTestDocument.getDocumentElement();
		
		NodeList nodeList = root.getElementsByTagName("TRANSITION");
		
		Hashtable<String, List<Element>> table = new Hashtable<String, List<Element>>();
		
		System.out.print("Merging events...");
		
		for(int i=0; i<nodeList.getLength(); i++){	//scorre tutti i nodi TRANSITION
			
			Element transition = (Element)nodeList.item(i);	//Per ogni nodo TRANSITION
			
			Element start_activity = (Element)transition.getElementsByTagName("START_ACTIVITY").item(0);	//Estrae il nodo start
			Element final_activity = (Element)transition.getElementsByTagName("FINAL_ACTIVITY").item(0);	//Estrae il nodo final
			Element event = (Element)transition.getElementsByTagName("EVENT").item(0);	//Estrae il nodo event

			String key = start_activity.getAttribute("id")+final_activity.getAttribute("id");	//crea la chiave composta dagli 'id' di start e final
			
			System.out.println("key: "+key);
			
			if(table.containsKey(key)){	//Se la tabella già contiene l'id ricavato
				Iterator<Element> iterator = table.get(key).iterator();	//ricava un iteratore sulle transition relative alla chiave
				boolean find = false;
				while(iterator.hasNext()&&find==false){	//scorre le transition relative alla chiave finchè ci sono elementi e non ha trovato quello cercato
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
		Set<String> keys = table.keySet();
		Hashtable<String, Integer> hash = new Hashtable<String, Integer>();
		Iterator<String> keyIterator = keys.iterator();
		while(keyIterator.hasNext()){
			String string = keyIterator.next();
			if(keys.contains(string))
			{
				if(hash.containsKey(string))
					hash.put(string, hash.get(string).intValue()+1);
				else{
					hash.put(string, new Integer(1));
					}
			}
		}
		Iterator<Integer> iter = hash.values().iterator();
		while(iter.hasNext()){
			int i=0;
			if(iter.next()==1)
				System.out.println(++i);			
		}
		System.out.println("Done correctly");

	}

	@Test
	public void testImportXmlDocument() {
		fail("Not yet implemented");
	}

	@Test
	public void testPrintGuiTreeOnXmlFile() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFilePath() {
		fail("Not yet implemented");
	}

	

}
