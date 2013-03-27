package it.unina.android.ripper_vs_intent_executor;

import java.io.File;

public class Main
{
	public static final String PATH = "/Volumes/Magazzino/File Personali/Laboratorio Uni/RipperXMLvsIntentExecutor";
	
	public static void main(String[] args)
	{
		RipperIntentExecutorComparator comparator = new RipperIntentExecutorComparator(PATH + File.separator + "activities_tomdroid.xml", PATH + File.separator + "Activities_tomdroid");
		comparator.start();
	}

}
