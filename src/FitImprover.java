import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FitImprover {


		 private static File fitCSVTool = new File(FitImprover.class.getResource("FitCSVTool.jar").getFile());


	 public static void main(String []args) {

		 if(args.length == 0)
		 {
			 System.out.println("No file supplied. Aborting");
			 return;
		 }

		 if(!fitCSVTool.exists())
		 {
			 System.out.println("FitCSVTool.jar could not be found. Aborting");
			 return;
		 }

		 String fileName = args[0];
		 //Convert the FIT file to CSV
		 convertFitToCsv(fileName);

		 //Remove the data that will prevent from converting back to FIT format

		 //Add the power values based on the speed values

		 //Add the power average values

		 //Convert the CSV file to FIT file
		 //file-withpower.fit"

		 String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
		 convertCsvToFit(fileNameWithoutExtension+ "-withPower");
	   }

	private static void convertCsvToFit(String fileNameWithoutExtension) {

		 File csvFileToConvert = new File(FitImprover.class.getResource(fileNameWithoutExtension+".csv").getFile());

		 Path path = Paths.get(csvFileToConvert.getAbsolutePath());
		 String directory = path.getParent().toString();
		 File convertedFitFile = new File(directory , fileNameWithoutExtension + ".fit");
		 
		 ProcessBuilder processBuilder = new ProcessBuilder(
				 "java", 
				 "-jar", 
				 fitCSVTool.getAbsolutePath(), 
				 "-c",
				 csvFileToConvert.getAbsolutePath(), 
				 convertedFitFile.getAbsolutePath());
		 try {
		Process process=	processBuilder.start();

		    InputStream in = process.getInputStream();
		    InputStream err = process.getErrorStream();

		    byte b[]=new byte[in.available()];
		    in.read(b,0,b.length);
		    System.out.println(new String(b));

		    byte c[]=new byte[err.available()];
		    err.read(c,0,c.length);
		    System.out.println(new String(c));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void convertFitToCsv(String fileName) {

		 File fileToConvert = new File(FitImprover.class.getResource(fileName).getFile());

		 ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", fitCSVTool.getAbsolutePath(),fileToConvert.getAbsolutePath());
		 try {
			processBuilder.start();

//		    InputStream in = process.getInputStream();
//		    InputStream err = process.getErrorStream();
//
//		    byte b[]=new byte[in.available()];
//		    in.read(b,0,b.length);
//		    System.out.println(new String(b));
//
//		    byte c[]=new byte[err.available()];
//		    err.read(c,0,c.length);
//		    System.out.println(new String(c));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
