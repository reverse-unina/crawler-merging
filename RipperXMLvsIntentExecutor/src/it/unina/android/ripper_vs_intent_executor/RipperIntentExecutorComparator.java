package it.unina.android.ripper_vs_intent_executor;

import java.io.File;
import java.util.ListIterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.nofatclips.androidtesting.model.ActivityState;
import com.nofatclips.crawler.model.Comparator;

public class RipperIntentExecutorComparator extends ActivityManager {

	private String folderPath;	//result folder's path
	private String[] activitiesFilesPaths;	//paths list
	private Document report;	//report

	//accepts the 'activities.xml' file path and the 'result' folder's path
	public RipperIntentExecutorComparator(String activitiesFilePath, String folderPath) {
		super(activitiesFilePath);
		this.folderPath = folderPath;
		this.activitiesFilesPaths = findFiles(folderPath);
		this.report = null;
	}

	//accepts the 'activities.xml' file path and a vector of Strings with each path
	public RipperIntentExecutorComparator(String activitiesFilePath, String[] activitiesFilesPaths) {
		super(activitiesFilePath);
		this.folderPath = null;
		this.activitiesFilesPaths = activitiesFilesPaths;
		this.report = null;
	}

	@Override
	public void run() {
		super.run();
		this.compare();
		System.out.println(this.folderPath);
		this.PrintActivitiesOnXmlFile(this.report, this.folderPath+File.separator+"report.xml");
	}	

	//returns a vector of strings that contains each xml file's path
	public String[] findFiles(String folder){

		File[] f = getFoldersList(folder);

		String[] files = new String[f.length];

		for(int i=0; i<files.length; i++){
			files[i] = f[i].getAbsolutePath();
		}

		return files;
	}

	//returns a vector of file that contains each xml file
	private File[] getFoldersList(String path)
	{
		File file = new File(path);

		Vector<File> vettore = new Vector<File>(0);

		File[] tempList = file.listFiles();

		for(int i=0; i<tempList.length;i++)
		{
			if(tempList[i].isDirectory()){
				File[] fileTempList = tempList[i].listFiles();

				for(int j=0; j<fileTempList.length;j++){
					if(fileTempList[j].getName().contains(".xml")){
						vettore.add(fileTempList[j]);						
					}
				}
			}
		}

		tempList = new File[vettore.size()];
		for(int k=0; k<vettore.size();k++)
			tempList[k]=vettore.get(k);
		return tempList;

	}

	//compares each activity with each one in 'activities.xml'
	private void compare(){
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

		this.report = builder.newDocument();

		this.report.appendChild(this.report.createElement("root"));
		
		for(int i=0; i<this.activitiesFilesPaths.length;i++){
			Element e = this.report.createElement("entry"+i);
			e.setAttribute("name", this.activitiesFilesPaths[i]);
			//assuming that each file has ONLY ONE Activity to be compared.
			ActivityState activity = ActivityExtractor(activitiesFilesPaths[i]).get(0);
			boolean found = Comparation(activity);
			String value = found? "found" : "not found";			 
			e.setAttribute("value", value);
			this.report.getDocumentElement().appendChild(e);
		}

	}

	//compares an activity with each one in 'activities.xml'
	private boolean Comparation(ActivityState b)
	{
	
		Comparator comparator = selectComparator();
		ListIterator<ActivityState> iterator = this.getActivities().listIterator();

		while(iterator.hasNext())
		{
			ActivityState a = iterator.next();
			
			if(comparator.compare(a,b)==true){
				return true;
			}
		}
		return false;
	}


}