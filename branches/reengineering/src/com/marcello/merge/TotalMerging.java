package com.marcello.merge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nofatclips.androidtesting.model.ActivityState;
import com.nofatclips.crawler.model.Comparator;
import com.unina.tata.filemanager.FileManagerFSM;

public class TotalMerging implements Observer{

	private Document guitree_temp;
	private List<ActivityState> activity_temp;
	private String xmlFilePath;
	private File[] list;
	private Hashtable<GuiTreeManager, activityGuitree> stateTable;
	class activityGuitree{
		boolean activityCome = false;
		boolean activityMerged = false;
		boolean guitreeCome = false;
		boolean guitreeMerged = false;
		List<ActivityState> activity = null;

		/**
		 * @return the activityCome
		 */
		public boolean isActivityCome() {
			return activityCome;
		}
		/**
		 * @param activityCome the activityCome to set
		 */
		public void setActivityCome(boolean activityCome) {
			this.activityCome = activityCome;
		}
		/**
		 * @return the activityMerged
		 */
		public boolean isActivityMerged() {
			return activityMerged;
		}
		/**
		 * @param activityMerged the activityMerged to set
		 */
		public void setActivityMerged(boolean activityMerged) {
			this.activityMerged = activityMerged;
		}
		/**
		 * @return the guitreeCome
		 */
		public boolean isGuitreeCome() {
			return guitreeCome;
		}
		/**
		 * @param guitreeCome the guitreeCome to set
		 */
		public void setGuitreeCome(boolean guitreeCome) {
			this.guitreeCome = guitreeCome;
		}
		/**
		 * @return the guitreeMerged
		 */
		public boolean isGuitreeMerged() {
			return guitreeMerged;
		}
		/**
		 * @param guitreeMerged the guitreeMerged to set
		 */
		public void setGuitreeMerged(boolean guitreeMerged) {
			this.guitreeMerged = guitreeMerged;
		}
		/**
		 * @return the activity
		 */
		public List<ActivityState> getActivity() {
			return activity;
		}
		/**
		 * @param activity the activity to set
		 */
		public void setActivity(List<ActivityState> activity) {
			this.activity = activity;
		}

	}

	public TotalMerging() {
		super();
		this.guitree_temp = null;
		this.activity_temp = null;
		this.xmlFilePath = null;
		this.list = null;
		this.stateTable = new Hashtable<GuiTreeManager, activityGuitree>();
	}

	public void mergeTransitions(){

		System.out.println("\nAlgorithm applicated to all folders...\n");

		GuiTreeManager GTManager = new GuiTreeManager(this.guitree_temp);

		System.out.println("Merging Events...");

		GTManager.TransitionMerging();

		GTManager.PrintGuiTreeOnXmlFile(GTManager.getDoc(),this.xmlFilePath);

		this.GetDotFile(this.xmlFilePath);


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

	public File[] getFoldersList(String path)
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

	public synchronized List<ActivityState> compareActivities(List<ActivityState> temp, List<ActivityState> current){

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

				//se il risultato  positivo si cambia l'id dell'activity in esame in quello
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
						b.setId(b.getId()+"+");
					}
				}
			}
		}
		return current;
	}

	public void unZipIt(String zipFile){

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

	public void mergeActivities(GuiTreeManager obj){
		if(this.activity_temp == null){
			this.activity_temp = new Vector<ActivityState>();
			this.activity_temp.addAll(obj.getaManager().getActivities());
			this.stateTable.get(obj).setActivity(obj.getaManager().getActivities());
		}
		else{
			this.stateTable.get(obj).setActivity(compareActivities(activity_temp, obj.getaManager().getActivities()));
			this.activity_temp.addAll(stateTable.get(obj).getActivity());
		}
		this.stateTable.get(obj).setActivityMerged(true);
	}

	public void updateGuitree(GuiTreeManager obj){
		if(this.guitree_temp == null){
			try {
				guitree_temp = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(obj.getXmlFilePath().replace(".xml", "_merged.xml"));
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
		else{
			obj.ReplaceActivitiesOnGuitree(obj.getDoc().getDocumentElement(), this.stateTable.get(obj).getActivity());

			NodeList trace = obj.getDoc().getDocumentElement().getElementsByTagName("TRACE");
			for(int j=0; j<trace.getLength(); j++){
				guitree_temp.getDocumentElement().appendChild(guitree_temp.importNode(trace.item(j), true));	
			}

		}
		this.stateTable.get(obj).setGuitreeMerged(true);
	}

	@Override
	public void update(Observable o, Object arg) {
		//System.out.println("notification received from " + o.toString());
		if(o.getClass()==ThreadMonitor.class){
			this.mergeTransitions();
		}
		else{

			GuiTreeManager obj = (GuiTreeManager) o;			

			if(((Integer)arg).intValue() == GuiTreeManager.ACTIVITIES){
				if (stateTable.containsKey(obj)==false)
					stateTable.put(obj, new activityGuitree());
				stateTable.get(obj).setActivityCome(true);
				mergeActivities(obj);
			}


			else if(((Integer)arg).intValue() == GuiTreeManager.GUITREE){
				if (stateTable.containsKey(obj)==false)
					stateTable.put(obj, new activityGuitree());
				stateTable.get(obj).setGuitreeCome(true);
				if(stateTable.get(obj).isActivityCome()&&stateTable.get(obj).isActivityMerged())
					updateGuitree(obj);
			}
		}
	}

	/**
	 * @return the xmlFilePath
	 */
	public String getXmlFilePath() {
		return xmlFilePath;
	}

	/**
	 * @return the list
	 */
	public File[] getList() {
		return list;
	}

	/**
	 * @param xmlFilePath the xmlFilePath to set
	 */
	public void setXmlFilePath(String xmlFilePath) {
		this.xmlFilePath = xmlFilePath;
	}

	/**
	 * @param list the list to set
	 */
	public void setList(File[] list) {
		this.list = list;
	}
}
