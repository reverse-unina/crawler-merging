package com.marcello.merge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nofatclips.androidtesting.model.ActivityState;
import com.nofatclips.crawler.model.Comparator;

public class TotalMerging {

	/**
	 * @param args :
	 * Experiment's zip file (usually named "TestX.zip") path.
	 */

	static Document activity;
	static Document guitree_temp;
	static List<ActivityState> activity_temp;
	static String xmlFilePath;

	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();

		if(args[0].equals("-help")){
			System.out.println("Usage: TotalMerging.jar FileInput.zip" + System.getProperty("line.separator")+"FileInput.zip contains random experiment's folders");
			return;
		}
		if (args[0].endsWith(".zip")==false){
			System.out.println("file .zip not provided, the program will terminate.");
			return;
		}

		//Scompatta il file di input qualora non sia stato già fatto in precedenza
		unZipIt(args[0]);

		//Fa una lista di ciò che è contenuto nel file estratto
		File[] list = new File(args[0].replace(".zip", "/files")).listFiles();
		
		//Per ogni CARTELLA (non file) applica l'algoritmo di Merging.
		for(int i=0; i<list.length; i++){
			File file = new File(list[i].getAbsolutePath());
			if (file.isDirectory()==false)
				continue;
			String[] mergeArgs = new String[1];
			mergeArgs[0] = file.getAbsolutePath();
			ExperimentMerging.main(mergeArgs);

		}

		activity_temp = new Vector<ActivityState>();

		System.out.print("Merging guitrees");

		//Inizia il ciclo di confronto
		for(int i=0; i<list.length; i++){
			System.out.print(".");
			File file = new File(list[i].getAbsolutePath());
			if (file.isDirectory()==false)
				continue;

			if(guitree_temp == null&& new File(file.getAbsolutePath()+File.separator+"activities_new.xml").exists()){
				ActivityMerging merging = new ActivityMerging(file.getAbsolutePath()+File.separator+"activities_new.xml");
				List<ActivityState> activities = merging.getActivities();			

				try {
					guitree_temp = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file.getAbsolutePath()+File.separator+"guitree_new_final.xml");
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}
				activity_temp.addAll(activities);
				continue;
			}

			if(new File(file.getAbsolutePath()+File.separator+"activities_new.xml").exists()){
				//ri-elabora il merging delle activity. DA MODIFICARE CON REEINGINEERING DI ACTIVITYMERGING.JAVA
				ActivityMerging merging = new ActivityMerging(file.getAbsolutePath()+File.separator+"activities_new.xml");

				//confronto con le activity temporanee
				List<ActivityState> activities = compareActivities(activity_temp, merging.getActivities());

				Element guiTreeRoot = null;

				try {
					guiTreeRoot = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file.getAbsolutePath()+File.separator+"guitree_new_final.xml").getDocumentElement();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}

				System.gc();

				new GuiTreeMerging().ReplaceEntries(guiTreeRoot, activities);

				NodeList trace = guiTreeRoot.getElementsByTagName("TRACE");
				for(int j=0; j<trace.getLength(); j++){
					guitree_temp.getDocumentElement().appendChild(guitree_temp.importNode(trace.item(j), true));	
				}
				activity_temp.addAll(activities);
			}
		}

		System.out.println("Done");

		xmlFilePath = args[0].replace(".zip", File.separator + "files" + File.separator + "guitree.xml" );

		new GuiTreeMerging().PrintGuiTreeOnXmlFile(guitree_temp,xmlFilePath);

		System.gc();

		TransitionMerging tm = new TransitionMerging();

		tm.calculate(xmlFilePath);

		ExperimentMerging.GetDotFile(tm.getFilePath());


		System.out.println("Elaboration done. Time elapsed (sec): " + (int)Math.floor((System.currentTimeMillis() - startTime)/1000));


	}

	static List<ActivityState> compareActivities(List<ActivityState> temp, List<ActivityState> current){

		ListIterator<ActivityState> iteratorTemp = temp.listIterator();
		ListIterator<ActivityState> iteratorCurrent = current.listIterator();

		//List<ActivityState> append_temp = new Vector<ActivityState>();

		Comparator comparator = new ActivityMerging().selectComparator();

		//per ogni activity presente nella lista temporanea
		while(iteratorTemp.hasNext())
		{

			ActivityState a = iteratorTemp.next();

			//per ogni activity del file in esame
			while(iteratorCurrent.hasNext())
			{
				ActivityState b = iteratorCurrent.next();

				//se il risultato è positivo si cambia l'id dell'activity in esame in quello
				//dell'activity della lista temporanea
				if (comparator.compare(a,b))
				{
					b.setId(a.getId());
					//append_temp.add(b);
				}
				//altrimenti, se l'id coincide ma non sono uguali, si cambia id all'activity in esame
				//(diventa "_id")
				else{
					if(b.getId().equals(a.getId())){
						b.setId("_"+b.getId());}
				}
			}
		}
		return current;
	}


	public static void unZipIt(String zipFile){

		String outputFolder = zipFile.split(".zip")[0]+File.separator;


		byte[] buffer = new byte[1024];

		try{

			//create output directory is not exists
			File folder = new File(outputFolder);
			if(!folder.exists()){
				folder.mkdir();
			}
			else{
				System.out.println(zipFile+" extraction not performed: it has been already perfromed.");
				return;
			}

			//get the zip file content
			ZipInputStream zis = 
					new ZipInputStream(new FileInputStream(zipFile));
			//get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();
			System.out.print("Unzipping file");

			while(ze!=null){

				if(ze.isDirectory()){
					ze = zis.getNextEntry();
					continue;
				}
				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator + fileName);

				System.out.print(".");

				//create all non exists folders
				//else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);             

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();   
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

			System.out.println("Done");

		}catch(IOException ex){
			ex.printStackTrace();
		}

	}
}
