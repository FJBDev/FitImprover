import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FitImprover {


		 private static File fitCSVTool = new File(FitImprover.class.getResource("FitCSVTool.jar").getFile());


	 public static void main(final String []args) {

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

		 final String fileName = args[0];
		 //Convert the FIT file to CSV
		 convertFitToCsv(fileName);

		 final String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));

		 //Remove the data that will prevent from converting back to FIT format
		 removeErroneousData(fileNameWithoutExtension);

		 //Add the power values based on the speed values (and the power average values)
		 //	 Add avg power for laps and for totals
		// i.e.: Whenever I find avg_speed ?


		 //Convert the CSV file to FIT file
		 //file-withpower.fit"

		 convertCsvToFit(fileNameWithoutExtension+ "-withPower");
	   }

	private static void removeErroneousData(final String fileName) {

		 final String currentDirectory = getCurrentDirectory();
		 final File fixedCsvFile = new File(currentDirectory , fileName + "-modified"+ ".csv");



		 try {
			fixedCsvFile.createNewFile();
		} catch (final IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		 try (FileWriter writer = new FileWriter(fixedCsvFile)) {

			 Thread.sleep(1000);

		 final File csvFile = new File(currentDirectory, fileName + ".csv");
			final List<String> lines = Files.readAllLines(Paths.get(csvFile.toURI()));

			//Write the modified lines in the new csv file
			for(final String str: lines) {

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

	private static void convertCsvToFit(final String fileNameWithoutExtension) {

		 final String currentDirectory = getCurrentDirectory();
		 final File csvFileToConvert = new File(currentDirectory , fileNameWithoutExtension +  ".csv");
		 final File convertedFitFile = new File(currentDirectory , fileNameWithoutExtension + ".fit");

		 final ProcessBuilder processBuilder = new ProcessBuilder(
				 "java",
				 "-jar",
				 fitCSVTool.getAbsolutePath(),
				 "-c",
				 csvFileToConvert.getAbsolutePath(),
				 convertedFitFile.getAbsolutePath());
		 try {
		final Process process=	processBuilder.start();

		    final InputStream in = process.getInputStream();
		    final InputStream err = process.getErrorStream();

		    final byte b[]=new byte[in.available()];
		    in.read(b,0,b.length);
		    System.out.println(new String(b));

		    final byte c[]=new byte[err.available()];
		    err.read(c,0,c.length);
		    System.out.println(new String(c));

		} catch (final IOException e) {
			e.printStackTrace();
		}

	}

	private static String getCurrentDirectory() {

		String currentDirectory = "";
		try {
			 currentDirectory = new File(FitImprover.class.getProtectionDomain().getCodeSource().getLocation()
					    .toURI()).getPath();
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		}

		return currentDirectory;
	}

	private static void convertFitToCsv(final String fileName) {

		 final File fileToConvert = new File(FitImprover.class.getResource(fileName).getFile());

		 final ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", fitCSVTool.getAbsolutePath(),fileToConvert.getAbsolutePath());
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

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
