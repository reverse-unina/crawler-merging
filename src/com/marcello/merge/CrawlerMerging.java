package com.marcello.merge;

import java.io.File;
import java.util.Observable;

public class CrawlerMerging extends Observable implements Runnable {

	private String arg;

	public CrawlerMerging(String arg){
		this.arg = arg;
	}


	public CrawlerMerging(){
		arg = null;
	}

	@Override
	public void run() {

		if (arg.endsWith(".zip")==false){
			System.out.println("file .zip not provided, the program will terminate.");
			if(this.countObservers()!=0){
				this.setChanged();
				this.notifyObservers("Please, provide a .zip file");
			}				
			return;
		}

		/*if(arg.length==0||arg[0].equals("-help")){
			System.out.println("Usage: TotalMerging.jar FileInput.zip" + System.getProperty("line.separator")+"(FileInput.zip contains random experiment's folders)");
			return;
		}*/

		TotalMerging tMerging = new TotalMerging();

		tMerging.setXmlFilePath(arg.replace(".zip", File.separator + "guitree.xml" ));

		//Scompatta il file di input qualora non sia stato giˆ fatto in precedenza
		tMerging.unZipIt(arg);

		//Fa una lista delle cartelle che contengono i file utili per l'algoritmo di merging (activities.xml e guitree.xml)
		tMerging.setList(tMerging.getFoldersList(arg.replace(".zip", "")));
		System.out.println("Trovate "+tMerging.getList().length + " cartelle adatte.");

		//Stampa il nome delle cartelle a cui sarˆ applicato l'algoritmo
		//for(int i=0;i<list.length;i++)		
		//System.out.println(list[i].getName());

		//definisci l'insieme di thread che gestirˆ il processo di merging
		Thread[] threads = new Thread[tMerging.getList().length];

		//istanzia un monitor per supervisionare i thread
		ThreadMonitor monitor = new ThreadMonitor(threads);
		monitor.addObserver(tMerging);
		Thread mon = new Thread(monitor);

		//Per ogni CARTELLA  applica l'algoritmo di Merging.
		for(int i=0; i<threads.length; i++){
			GuiTreeManager GTManager = new GuiTreeManager(tMerging.getList()[i].getAbsolutePath()+File.separator+"guitree.xml");
			GTManager.addObserver(tMerging);
			threads[i] = new Thread(GTManager);
			threads[i].start();
		}

		mon.start();

	}

}
