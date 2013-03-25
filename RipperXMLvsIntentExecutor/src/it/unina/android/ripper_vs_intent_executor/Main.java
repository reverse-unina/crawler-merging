package it.unina.android.ripper_vs_intent_executor;

import java.io.File;

public class Main
{
	public static final String PATH = "/Volumes/MARCELLO/eclipse/Workspace/RipperXMLvsIntentExecutor/xml";
	
	public static void main(String[] args)
	{
		RipperIntentExecutorComparator comparator = new RipperIntentExecutorComparator(PATH + File.separator + "activities.xml", PATH + File.separator + "Nuova Cartella");
		comparator.start();
	}

}
