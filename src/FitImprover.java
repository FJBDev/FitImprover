import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FitImprover {

   private static File fitCSVTool = new File(FitImprover.class.getProtectionDomain().getCodeSource().getLocation().getPath());

   public static void main(final String[] args) {

   System.out.println(fitCSVTool.getAbsolutePath());
      if (args.length != 1) {
         System.out.println("No file supplied. Aborting");
         return;
      }

      if (!fitCSVTool.exists()) {
         System.out.println("FitCSVTool.jar could not be found at this location"+
      fitCSVTool.getAbsolutePath()+
      ". Aborting");
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
      final List<String> dataWithPower = addPowerData(cleanLines);

      //Writing the modified lines to a new CSV file
      writeDataWithPower(fileNameWithoutExtension, dataWithPower);

      //Convert the CSV file to FIT file
      convertCsvToFit(fileNameWithoutExtension + "-withPower");
   }

   private static List<String> addPowerData(final List<String> cleanLines) {

      final List<String> linesWithPowerData = new ArrayList<>();

      for (final String line : cleanLines) {

         final int index = line.indexOf("speed");
         if (index == -1) {
            linesWithPowerData.add(line);
            continue;
         }

         //In m/s
         final String speedContent = line.substring(index);
         //We need to look for this format only
         // ,speed,"3.218",m/s

         if (!speedContent.contains("\"")) {
            linesWithPowerData.add(line);
            continue;
         }

         String speedValue = speedContent.substring(speedContent.indexOf('"') + 1);
         speedValue = speedValue.substring(0, speedValue.indexOf('"') - 1);

         //Convert the value from m/s to mph
         final double speedMph = 3600 * Double.valueOf(speedValue) / 1609;

         //Compute the power value with the equation for the Travel Trac Fluid
         // found here 
         //https://github.com/mechgt/trainer-power/blob/0281a9fd3b02bd29e59faa8fd8c48c273e9c98e2/TrainerPower/TrainerData.xml#L1083
         //The generic equation:
         //https://github.com/mechgt/trainer-power/blob/0281a9fd3b02bd29e59faa8fd8c48c273e9c98e2/TrainerPower/Data/Trainer.cs#L301

         final double powerValue = -1.69361180854322 +
               4.8316011670131 * speedMph -
               0.104979886788859 * Math.pow(speedMph, 2) +
               0.0169930755283735 * Math.pow(speedMph, 3);

         final String powerDataContent = getPowerDataContent((int) Math.round(powerValue), line.contains("max_speed"));
         final String lineWithPower = line.replace("m/s", powerDataContent);

         linesWithPowerData.add(lineWithPower);

      }

      return linesWithPowerData;
   }

   private static String getPowerDataContent(final int roundedPowerValue, final boolean averagePower) {

      final StringBuilder powerDataContent = new StringBuilder("m/s,");

      if (averagePower) {
         powerDataContent.append("avg_");
      }

      powerDataContent.append("power,\"" + roundedPowerValue + "\",watts");

      return powerDataContent.toString();
   }

   private static void writeDataWithPower(final String fileNameWithoutExtension, final List<String> dataWithPower) {

      final String currentDirectory = getCurrentDirectory();
      final File fixedCsvFile = new File(currentDirectory, fileNameWithoutExtension + "-withPower" + ".csv");

      try {
         fixedCsvFile.createNewFile();
      } catch (final IOException e1) {
         e1.printStackTrace();
      }

      //UTF8 is VERY important and REQUIRED
      try (FileWriter writer = new FileWriter(fixedCsvFile, StandardCharsets.UTF_8)) {

         //Write the modified lines in the new csv file
         for (final String line : dataWithPower) {

            writer.write(line + "\n");
         }
         writer.flush();
      } catch (final IOException e) {
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
                  line.startsWith("Data,0,field_description,developer_data_index,\"")) {
               continue;
            }

            //This is where it gets trickier
            if (line.contains(",charge,") || line.contains("workout_type")) {
               //We remove the data after 2 ','
               // Example : ,null,charge,"76",%,,,,
               // =>        ,null,,,,,

               String keyword = "";

               if (line.contains(",charge,")) {
                  keyword = "charge";
               }
               if (line.contains(",workout_type,")) {
                  keyword = "workout_type";
               }

               final String cleanedLine = removeSpecificFieldData(line, keyword);
               cleanLines.add(cleanedLine);
               continue;
            }

            cleanLines.add(line);
         }
      } catch (final IOException e) {
         e.printStackTrace();
      }

      return cleanLines;
   }

   private static String removeSpecificFieldData(final String line, final String keyword) {

      final int baseIndex = line.indexOf(keyword);
      final String chargeString = line.substring(baseIndex);

      final int index = nthOccurrence(chargeString, ",", 3);

      final String substringToReplace = line.substring(baseIndex, baseIndex + index);

      final String cleanedLine = line.replace(substringToReplace, "");
      return cleanedLine;
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

   public static int nthOccurrence(final String str1, final String str2, final int n) {

      String tempStr = str1;
      int tempIndex = -1;
      int finalIndex = 0;
      for (int occurrence = 0; occurrence < n; ++occurrence) {
         tempIndex = tempStr.indexOf(str2);
         if (tempIndex == -1) {
            finalIndex = 0;
            break;
         }
         tempStr = tempStr.substring(++tempIndex);
         finalIndex += tempIndex;
      }
      return --finalIndex;
   }
}
