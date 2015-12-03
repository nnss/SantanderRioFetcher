import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by mlo on 11/27/15.
 */
public class FormatedOutput {
    private static Map <String,Object> cfg = null;

    public FormatedOutput(Map<String,Object> myConf){

    }

    public String simpleArrayOutput(Hashtable<String,String> myInput){
        System.out.println("Printing the Output (joined result): " + Arrays.toString(myInput.entrySet().toArray()));
        return Arrays.toString(myInput.entrySet().toArray());
    }
}
