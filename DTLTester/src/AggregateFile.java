import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.util.ArrayList;

@SuppressWarnings("unused")

public class AggregateFile {
    public String FileName;
    public int NumberOfFilesProcessed;
    public double EventConsistency;
    public double MappingConsistency;
    public boolean Error;
    public ArrayList<Mapping> mappings;
    public AggregateFile(){
        super();
        mappings = new ArrayList<Mapping>();
    }
    public void AggregateDisplay(){
        System.out.println("FileName " + FileName);
        System.out.println("Number of Files processed " + NumberOfFilesProcessed);
        /*for (Mapping i: mappings){
            i.Display();
        }*/
        System.out.println("Number of Mappings "+ mappings.size());
        System.out.println("Event consistency " +EventConsistency);
        System.out.println("Mapping consistency " +MappingConsistency);
        System.out.println();
    }
}
