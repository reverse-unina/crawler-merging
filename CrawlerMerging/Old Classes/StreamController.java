package com.marcello.merge;

import java.io.*;

public class StreamController {

  private FileInputStream input;
  private File file;

  public FileInputStream getStream() {
	  return input;
  }
  
  public void setStream(String path){
	  try {
		input.close();
	} catch (IOException e) {
		System.out.println("An error occurred trying to close the previous stream: " + e + " Probably there wasn't a previous stram to close.");
	}
	  try {
		System.out.println("Opening a new Stream");
		input = new FileInputStream(path);
	} catch (FileNotFoundException e) {
		System.out.println("An error occurred: " + e);
	}	  
  }
  
  public File getFile(){
	  return file;
  }

  public StreamController() {
	  System.out.println("The stream is not connected with a file. Use the constructor StreamController(String file)");
  }

  public StreamController(String stringa) {
	  
	  try {
		file = new File(stringa);
		input = new FileInputStream(file);
	} catch (FileNotFoundException e) {
		System.out.println("A problem occurred: " + e);
		input = null;
		}
	  catch(NullPointerException e){
		System.out.println("A problem occurred: " + e);
		file = null;
	  }
	  
  }
  
  public void closeStream(){
	  
		try {
			input.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	  }

}