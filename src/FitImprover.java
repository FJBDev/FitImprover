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

      //Convert the CSV file to FIT file
      //file-withpower.fit"

      convertCsvToFit(fileNameWithoutExtension + "-withPower");
   }

   private static List<String> removeErroneousData(final String fileName) {

      final String currentDirectory = getCurrentDirectory();
      final File fixedCsvFile = new File(currentDirectory, fileName + "-modified" + ".csv");

      final List<String> cleanLines = new ArrayList<>();

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
         for (final String line : lines) {

            if (line.contains("unknown") ||
                  line.contains("developer_data_id")) {
               continue;
            }

            // writer.write(str + System.lineSeparator());
            cleanLines.add(line);
         }
      } catch (IOException | InterruptedException e) {
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
