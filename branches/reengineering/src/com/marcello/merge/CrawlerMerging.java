package com.marcello.merge;

import java.io.File;

public class CrawlerMerging {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		TotalMerging tMerging = new TotalMerging();

		tMerging.setXmlFilePath(args[0].replace(".zip", File.separator + "guitree.xml" ));

		if(args.length==0||args[0].equals("-help")){
			System.out.println("Usage: TotalMerging.jar FileInput.zip" + System.getProperty("line.separator")+"(FileInput.zip contains random experiment's folders)");
			return;
		}
		if (args[0].endsWith(".zip")==false){
			System.out.println("file .zip not provided, the program will terminate.");
			return;
		}

		//Scompatta il file di input qualora non sia stato giˆ fatto in precedenza
		tMerging.unZipIt(args[0]);

		//Fa una lista delle cartelle che contengono i file utili per l'algoritmo di merging (activities.xml e guitree.xml)
		tMerging.setList(tMerging.getFoldersList(args[0].replace(".zip", "")));
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
