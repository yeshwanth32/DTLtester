import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.*;

@SuppressWarnings("unused")

public class Main {
    public static ArrayList<AggregateFile> AgFilesA=new ArrayList<>();
    public static ArrayList<AggregateFile> AgFilesB=new ArrayList<>();
    public static int NumberOfAggregateFiles;
    public static void main(String[] args) throws IOException, InterruptedException,FileNotFoundException{
        try {
            String Location1, Location2;
            String TestFilesLocation;
            int Threshold;
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter Location of first program");
            Location1 = sc.nextLine();
            System.out.println("Enter Location of second program");
            Location2 = sc.nextLine();
            System.out.println("Enter Location of the gene trees");
            TestFilesLocation = sc.nextLine();
            System.out.println("Enter number of gene trees");
            NumberOfAggregateFiles = Integer.parseInt(sc.nextLine());
            System.out.println("Enter the difference threshold for event classification checking");
            Threshold = Integer.parseInt(sc.nextLine());
            GenerateAggregateFiles(Location1,Location2,TestFilesLocation);
            UpdateList();
            SaveConsistencies();
            SaveEventClassificationConsistencies(Threshold);
            System.out.println("Done");
        }catch (Exception e){
            FileWriter fw=new FileWriter("error.txt");
            fw.write(e.toString());
            System.out.println("Input wrong (Error in main)" + e.toString());
        }
    }
    private static void GenerateAggregateFiles(String Location1, String Location2, String TestFilesLocation) throws IOException, InterruptedException{
        try {
            long startTime, endTime, durationTime;
            ArrayList<Long> RunTimesForProgram1 = new ArrayList<Long>();
            ArrayList<Long> RunTimesForProgram2 = new ArrayList<Long>();
            int x = 1;
            for (int j = 1; j < NumberOfAggregateFiles+1; j++) {
                x = 1;
                startTime = System.nanoTime();
                for (int k = 0; k < 100; k++) {
                    FileWriter fw = new FileWriter("Program1.bat");
                    fw.append("call " + Location1 + " -i " + TestFilesLocation + j + ".tree -o outputPA" + x + " --seed " + x + "\n");// --seed "+(int)Rand(1,30000)+
                    x++;
                    fw.close();
                    Process P1 = Runtime.getRuntime().exec("cmd /c Program1.bat");
                    P1.waitFor();
                }
                endTime = System.nanoTime();
                durationTime = endTime - startTime;
                System.out.println("RunNumber " + j + " execution time for program 1 (" + (x-1) + "files) : " + (durationTime));
                RunTimesForProgram1.add(durationTime);
                x = 1;
                startTime = System.nanoTime();
                for (int k = 0; k < 100; k++) {
                    FileWriter fw2 = new FileWriter("Program2.bat");
                    fw2.append("call " + Location2 + " -i " + TestFilesLocation + j + ".tree -o outputPB" + x + " --seed " + x + "\n");
                    x++;
                    fw2.close();
                    Process P2 = Runtime.getRuntime().exec("cmd /c Program2.bat");
                    P2.waitFor();
                }
                endTime = System.nanoTime();
                durationTime = endTime - startTime;
                System.out.println("RunNumber " + j + " execution time for program 2 (" + (x-1) + "files) : " + (durationTime));
                RunTimesForProgram2.add(durationTime);
                FileWriter fw3 = new FileWriter("Program3.bat");
                fw3.append("cmd /c AggregateRangerFast.exe outputPA > AggregateOutputA" + j + ".txt\n");
                fw3.append("cmd /c AggregateRangerFast.exe outputPB > AggregateOutputB" + j + ".txt\n");
                fw3.close();
                Process P3 = Runtime.getRuntime().exec("cmd /c Program3.bat");
                P3.waitFor();
            }
            SaveRunTimes(RunTimesForProgram1, RunTimesForProgram2, x-1);
        }catch (Exception e){
            System.out.println("Error in generating Aggregate files : " + e.toString());
        }
    }
    private static void SaveEventClassificationConsistencies(int Threshold) {
        try {
            FileWriter fw=new FileWriter("EventClassification.csv");
            fw.write("Threshold:,"+Threshold+"\n");
            fw.write("Number of files processed:,"+AgFilesA.get(0).NumberOfFilesProcessed+"\n");
            fw.write("FileNumber,Speciation,Duplication,Transfer,Different most frequent mapping, Number of mappings"+"\n");
            AggregateFile A,B;
            for (int i = 0; i < AgFilesA.size(); i++) {
                double Scounter = 0, Dcounter = 0, Tcounter = 0, Mcounter = 0;
                A = AgFilesA.get(i);
                B = AgFilesB.get(i);
                for (int j = 0; j < A.mappings.size(); j++) {
                    Mapping MA = A.mappings.get(j);
                    Mapping MB = B.mappings.get(j);
                    if (Math.abs(MA.SpeciationCount - MB.SpeciationCount) > Threshold){ Scounter++;}
                    if (Math.abs(MA.DuplicationCount - MB.DuplicationCount) > Threshold){ Dcounter++;}
                    if (Math.abs(MA.TransferCount - MB.TransferCount) > Threshold){ Tcounter++;}
                    if (!MA.MostFrequentMapping.equals(MB.MostFrequentMapping)){Mcounter++;}
                }
                String FileNumber = "AggregateOutput" + A.FileName.replaceAll("[^0-9]", "");
                fw.write(FileNumber + "," + Scounter + "," + Dcounter + "," + Tcounter + ","+ Mcounter  +"," + A.mappings.size() +"\n");
            }
            fw.close();
        }catch (Exception e){
            System.out.println("Error :" + e.toString());
        }

    }
    private static void SaveRunTimes(ArrayList<Long> P1, ArrayList<Long> P2, int NumberOfFiles){
        try {
            FileWriter fw=new FileWriter("RunTime.csv");
            fw.write("NumberOfFiles: " + NumberOfFiles + "\n");
            fw.write("Run Number,Program1,Program2\n");
            for (int i = 0; i < P1.size(); i++){
                fw.write(i +","+(P1.get(i)/1000000000d) + "," + (P2.get(i)/1000000000d) +"\n"); // converts the runtime from nanoseconds to seconds
            }
            fw.close();
        }catch (Exception e){ System.out.println("Can't create runtime save file"); }
    }
    private static void SaveConsistencies(){
        try {
            FileWriter fw=new FileWriter("Consistencies.csv");
            fw.write("FileNumber,Program1,,Program2\n");
            fw.write(",Mappings,Events,Mappings,Events\n");
            System.out.println(AgFilesA.size() + " " + AgFilesB.size());
            for (int i = 0; i < AgFilesA.size(); i++){
                String FileNumber = "AggregateOutput" + AgFilesA.get(i).FileName.replaceAll("[^0-9]", "");
                fw.write( FileNumber+","+AgFilesA.get(i).MappingConsistency +"," + AgFilesA.get(i).EventConsistency
                        +","+AgFilesB.get(i).MappingConsistency +"," + AgFilesB.get(i).EventConsistency +"\n");
            }
            fw.close();
        }catch (Exception e){ System.out.println("Can't create Consistency save file " + e.toString()); }
    }
    private static void UpdateList() throws FileNotFoundException{
        int i = 1;
        File file1 = new File("AggregateOutputA"+i+".txt");
        File file2 = new File("AggregateOutputB"+i+".txt");
        do {
            Scanner sc1 = new Scanner(file1);
            Scanner sc2 = new Scanner(file2);
            AggregateFile A = new AggregateFile();
            AggregateFile B = new AggregateFile();
            A.FileName = "AggregateOutputA" + i;
            B.FileName = "AggregateOutputB" + i;
            while (sc1.hasNextLine()) {
                String line = sc1.nextLine();
                UpdateAggregateFile(line,A);
            }
            while (sc2.hasNextLine()) {
                String line = sc2.nextLine();
                UpdateAggregateFile(line,B);
            }
            A.AggregateDisplay();
            B.AggregateDisplay();
            AgFilesA.add(A);
            AgFilesB.add(B);
            i++;
            file1 = new File("AggregateOutputA"+i+".txt");
            file2 = new File("AggregateOutputB"+i+".txt");
            //try { Thread.sleep(100000); }catch (Exception e){ System.out.println("Sleep Error"); }
        }while (file1.exists() && file2.exists());
    }
    private static void UpdateAggregateFile(String line, AggregateFile F){
        if (line.contains("Processed")){
            F.NumberOfFilesProcessed = Integer.parseInt(line.substring(line.indexOf(' ')+1,line.indexOf('f')-1));
        }
        if (line.contains("LCA")){
            Mapping mapping = new Mapping();
            mapping.name = line.substring(0,line.indexOf(' '));
            String speciation = line.substring(nthOccurrenceOf(line,'=',2)+2, nthOccurrenceOf(line,',',2));
            String duplication = line.substring(nthOccurrenceOf(line,'=',3)+2, nthOccurrenceOf(line,',',3));
            String transfer = line.substring(nthOccurrenceOf(line,'=',4)+2, nthOccurrenceOf(line,',',4)-1);
            mapping.SpeciationCount = Integer.parseInt(speciation);
            mapping.DuplicationCount = Integer.parseInt(duplication);
            mapping.TransferCount = Integer.parseInt(transfer);
            mapping.MostFrequentMapping = line.substring(line.indexOf('>')+1,line.lastIndexOf(','));
            mapping.MostFrequentMappingCount = Integer.parseInt(line.substring((line.lastIndexOf(',')+2),(line.lastIndexOf('t')-1)));
            F.mappings.add(mapping);
        }
        if (line.contains("events with 100% consistency")){
            F.EventConsistency = Double.parseDouble(line.substring(line.indexOf('=')+2));
        }
        if (line.contains("mappings with 100% consistency")){
            F.MappingConsistency = Double.parseDouble(line.substring(line.indexOf('=')+2));
        }
    }
    private static double Rand(double i, double j) {
        return ((j - i) * Math.random()) + i ;
    }
    private static void runProgram(String location) throws  IOException{
        Runtime run  = Runtime.getRuntime();
        Process proc = run.exec(location);
    }
    public static int nthOccurrenceOf(String str, char ch, int n)
    {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                n--;
                if (n == 0) { return i; }
            }
        }
        return -1;
    }
}
