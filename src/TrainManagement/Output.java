/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TrainManagement;

/**
 *
 * @author abhijeet
 */
import java.io.*;
public class Output{

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

		File dir = new File(".");
		File fout = new File(dir.getCanonicalPath() + File.separator + "hello.txt");
		
		FileOutputStream fos = new FileOutputStream("hello.txt");
		fos.write("hello".getBytes());

	}

}
