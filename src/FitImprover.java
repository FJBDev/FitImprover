import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FitImprover {

   private static File fitCSVTool = new File(FitImprover.class.getResource("FitCSVTool.jar").getFile());

   public static void main(final String[] args) {

      if (args.length != 1) {
         System.out.println("No file supplied. Aborting");
         return;
      }

      if (!fitCSVTool.exists()) {
         System.out.println("FitCSVTool.jar could not be found. Aborting");
         return;
      }

      final String fileName = args[0];
      //Convert the FIT file to CSV
      convertFitToCsv(fileName);

      final String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));

      //Remove the data that will prevent from converting back to FIT format
      final List<String> cleanLines = removeErroneousData(fileNameWithoutExtension);

      //Add the power values based on the speed values (and the power average values)
      //  Add avg power for laps and for totals
      // i.e.: Whenever I find avg_speed ?
      //final List<String> dataWithPower = addPowerData(cleanLines);

      //Writing the modified lines to a new CSV file
      writeDataWithPower(fileNameWithoutExtension, cleanLines); // dataWithPower );
      
      //Convert the CSV file to FIT file
      //file-withpower.fit"

      convertCsvToFit(fileNameWithoutExtension + "-withPower");
   }

   private static void writeDataWithPower(String fileNameWithoutExtension, List<String> dataWithPower) {
	   
      final String currentDirectory = getCurrentDirectory();
	   final File fixedCsvFile = new File(currentDirectory, fileNameWithoutExtension + "-withPower" + ".csv");

	      try {
	         fixedCsvFile.createNewFile();
	      } catch (final IOException e1) {
	         e1.printStackTrace();
	      }
	      
	   try (FileWriter writer = new FileWriter(fixedCsvFile)) {


	         //Write the modified lines in the new csv file
	         for (final String line : dataWithPower) {

	             writer.write(line + System.lineSeparator());
	         }
	      } catch (IOException e) {
	         e.printStackTrace();
	      }
	
}

private static List<String> removeErroneousData(final String fileName) {

      final List<String> cleanLines = new ArrayList<>();

      try {

      final String currentDirectory = getCurrentDirectory();
         final File csvFile = new File(currentDirectory, fileName + ".csv");
         final List<String> lines = Files.readAllLines(Paths.get(csvFile.toURI()));

         //Write the modified lines in the new csv file
         for (final String line : lines) {

            if (line.contains("unknown") ||
                  line.startsWith("Data,0,developer_data_id,developer_data_index,\"") ||
                  line.startsWith("Definition,0,field_description,developer_data_index,") ||
                  line.startsWith("Data,0,field_description,developer_data_index,\"") ) {
               continue;
            }
            
            //This is where it gets trickier
            if(line.contains(",charge,"))
            {
            	//We remove the data after 2 ','
            	// Example : ,null,charge,"76",%,,,,
            	// =>        ,null,,,,,
            	
            	int baseIndex = line.indexOf(",charge");
            	String chargeString = line.substring(baseIndex);
            	int index = chargeString.indexOf(',', 8);
            	index = chargeString.indexOf(',', index+1);
            	
            	String cleanedLine =line.replace(line.substring(baseIndex, baseIndex + index),"");
            cleanLines.add(cleanedLine);
            continue;
            }

            cleanLines.add(line);
         }
      } catch (IOException e) {
         e.printStackTrace();
      }

      return cleanLines;
   }

   private static void convertCsvToFit(final String fileNameWithoutExtension) {

      System.out.println("--STARTING THE CSV TO FIT CONVERSION--");
      System.out.println("");

      final String currentDirectory = getCurrentDirectory();
      final File csvFileToConvert = new File(currentDirectory, fileNameWithoutExtension + ".csv");
      final File convertedFitFile = new File(currentDirectory, fileNameWithoutExtension + ".fit");

      final ProcessBuilder processBuilder = new ProcessBuilder(
            "java",
            "-jar",
            fitCSVTool.getAbsolutePath(),
            "-c",
            csvFileToConvert.getAbsolutePath(),
            convertedFitFile.getAbsolutePath());
      try {
         final Process process = processBuilder.start();
         process.waitFor();

         System.out.println(getProcessOutput(process));
         System.out.println("--CSV TO FIT CONVERSION DONE--");

      } catch (final IOException | InterruptedException e) {
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

      System.out.println("--STARTING THE FIT TO CSV CONVERSION--");
      System.out.println("");

      final File fileToConvert = new File(FitImprover.class.getResource(fileName).getFile());

      final ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", fitCSVTool.getAbsolutePath(), fileToConvert.getAbsolutePath());
      try {
         final Process process = processBuilder.start();
         process.waitFor();

         System.out.println(getProcessOutput(process));
         System.out.println("--FIT TO CSV CONVERSION DONE--");

      } catch (final IOException | InterruptedException e) {
         e.printStackTrace();
      }
   }

   private static String getProcessOutput(final Process process) throws IOException {

      BufferedReader reader =
            new BufferedReader(new InputStreamReader(process.getErrorStream()));
      StringBuilder builder = new StringBuilder();
      String line = null;
      while ((line = reader.readLine()) != null) {
         builder.append(line);
         builder.append(System.getProperty("line.separator"));
      }
      if (!builder.toString().trim().equals("")) {
         return builder.toString();
      }

      reader =
            new BufferedReader(new InputStreamReader(process.getInputStream()));
      builder = new StringBuilder();
      while ((line = reader.readLine()) != null) {
         builder.append(line);
         builder.append(System.getProperty("line.separator"));
      }
      return builder.toString();
   }
}
