package com.marcello.merge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
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
		
		if(args.length==0||args[0].equals("-help")){
			System.out.println("Usage: TotalMerging.jar FileInput.zip" + System.getProperty("line.separator")+"(FileInput.zip contains random experiment's folders)");
			return;
		}
		if (args[0].endsWith(".zip")==false){
			System.out.println("file .zip not provided, the program will terminate.");
			return;
		}

		//Scompatta il file di input qualora non sia stato già fatto in precedenza
		unZipIt(args[0]);

		//Fa una lista di ciò che è contenuto nel file estratto
		File[] list = getFoldersList(args[0].replace(".zip", ""));

		//Per ogni CARTELLA (non file) applica l'algoritmo di Merging.
		for(int i=0; i<list.length; i++){
			File file = new File(list[i].getAbsolutePath());
			String[] mergeArgs = new String[1];
			mergeArgs[0] = file.getAbsolutePath();
			ExperimentMerging.main(mergeArgs);
		}

		activity_temp = new Vector<ActivityState>();

		System.out.print("Merging guitrees");

		ActivityManager aManager = null;
		GuiTreeManager gtManager = null;

		//Inizia il ciclo di confronto
		for(int i=0; i<list.length; i++){
			System.out.print(".");
			File file = new File(list[i].getAbsolutePath());
			if (file.isDirectory()==false)
				continue;

			if(guitree_temp == null&& new File(file.getAbsolutePath()+File.separator+"activities_merged.xml").exists()){
				aManager = new ActivityManager(file.getAbsolutePath()+File.separator+"activities_merged.xml");
				List<ActivityState> activities = aManager.getActivities();			

				try {
					guitree_temp = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file.getAbsolutePath()+File.separator+"guitree_merged.xml");
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}
				activity_temp.addAll(activities);
				aManager = null;
				continue;
			}

			if(new File(file.getAbsolutePath()+File.separator+"activities_fixed.xml").exists()){
				//estrae le activity dal file.
				aManager = new ActivityManager(file.getAbsolutePath()+File.separator+"activities_merged.xml");

				//confronto con le activity temporanee
				List<ActivityState> activities = compareActivities(activity_temp, aManager.getActivities());


				gtManager = new GuiTreeManager();
				gtManager.ReplaceActivitiesOnGuiTree(file.getAbsolutePath()+File.separator+"guitree_merged.xml", activities);

				NodeList trace = gtManager.getDoc().getDocumentElement().getElementsByTagName("TRACE");
				for(int j=0; j<trace.getLength(); j++){
					guitree_temp.getDocumentElement().appendChild(guitree_temp.importNode(trace.item(j), true));	
				}
				activity_temp.addAll(activities);
				gtManager = null;
				aManager = null;
			}
		}

		xmlFilePath = args[0].replace(".zip", File.separator + "guitree.xml" );

		gtManager = new GuiTreeManager(guitree_temp,xmlFilePath);
		
		System.out.println("Merging Events...");
		
		gtManager.TransitionMerging();

		gtManager.PrintGuiTreeOnXmlFile(gtManager.getDoc(),xmlFilePath);

		System.gc();

		ExperimentMerging.GetDotFile(xmlFilePath);

		System.out.println("Elaboration done. Time elapsed (sec): " + (int)Math.floor((System.currentTimeMillis() - startTime)/1000));


	}
	
	
	static File[] getFoldersList(String path)
	{
		File file = new File(path);
		
		Vector<File> vettore = new Vector<File>(0);
		
		File[] tempList = file.listFiles();
		
		for(int i=0; i<tempList.length;i++)
		{
			if(tempList[i].isDirectory()){
				File[] fileTempList = tempList[i].listFiles();
				boolean act = false, gui=false;
				
				for(int j=0; j<fileTempList.length;j++){
					if(fileTempList[j].getName().equals("activities.xml"))
						act=true;
					if(fileTempList[j].getName().equals("guitree.xml"))
						gui=true;					
				}
				if(act&&gui)
					vettore.add(tempList[i]);
				else{
					File[] temp = getFoldersList(tempList[i].getAbsolutePath());
					for (int h=0;h<temp.length;h++)
						vettore.add(temp[h]);
					}
			}
		}
		 tempList = new File[vettore.size()];
		 for(int k=0; k<vettore.size();k++)
			 tempList[k]=vettore.get(k);
		 return tempList;
	}

	static List<ActivityState> compareActivities(List<ActivityState> temp, List<ActivityState> current){

		ListIterator<ActivityState> iteratorTemp = temp.listIterator();
		ListIterator<ActivityState> iteratorCurrent = current.listIterator();

		//List<ActivityState> append_temp = new Vector<ActivityState>();

		Comparator comparator = new ActivityManager().selectComparator();

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
