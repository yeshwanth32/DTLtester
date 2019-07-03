import java.time.Duration;

@SuppressWarnings("unused")
public class Mapping {
    public String name;
    public int SpeciationCount;
    public int DuplicationCount;
    public int TransferCount;
    public String MostFrequentMapping;
    public int MostFrequentMappingCount;
    public void Display(){
        System.out.println(name + " [" + SpeciationCount + "," + DuplicationCount + "," + TransferCount + "]"
                + MostFrequentMapping + "-->" + MostFrequentMappingCount);
    }

}
