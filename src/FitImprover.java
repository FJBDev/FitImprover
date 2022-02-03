import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

		 String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));

		 //Remove the data that will prevent from converting back to FIT format
		 removeErroneousData(fileNameWithoutExtension);

		 //Add the power values based on the speed values

		 //Add the power average values

		 //Convert the CSV file to FIT file
		 //file-withpower.fit"

		 convertCsvToFit(fileNameWithoutExtension+ "-withPower");
	   }

	private static void removeErroneousData(String fileName) {

		 String currentDirectory = getCurrentDirectory();
		 File fixedCsvFile = new File(currentDirectory , fileName + "-modified"+ ".csv");
		 
	
		 
		 try {
			fixedCsvFile.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		 try (FileWriter writer = new FileWriter(fixedCsvFile)) {
			 
			 Thread.sleep(1000);
			 
		 File csvFile = new File(currentDirectory, fileName + ".csv");
			List<String> lines = Files.readAllLines(Paths.get(csvFile.toURI()));

			//Write the modified lines in the new csv file
			for(String str: lines) {
				
				if(str.contains("unknown") ||
						str.contains("developer_data_id"))
				{
					continue;
				}
				
			  writer.write(str + System.lineSeparator());
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	private static void convertCsvToFit(String fileNameWithoutExtension) {

		 String currentDirectory = getCurrentDirectory();
		 File csvFileToConvert = new File(currentDirectory , fileNameWithoutExtension +  ".csv");
		 File convertedFitFile = new File(currentDirectory , fileNameWithoutExtension + ".fit");

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

	private static String getCurrentDirectory() {
		
		String currentDirectory = "";
		try {
			 currentDirectory = new File(FitImprover.class.getProtectionDomain().getCodeSource().getLocation()
					    .toURI()).getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return currentDirectory;
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
