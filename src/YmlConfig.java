import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by matias <matiaspalomec@gmail.co> on 02/03/15.
 */
public class YmlConfig {

    private static String configPath = System.getProperty("user.home");
    private static String fileName   = ".bancorio_fetcher.yml";
                // Yes, yes, I know, but I still buying leader jackets at Cagnin 910.
    private static Map<String, Object> config = null;
    Yaml yml = new Yaml();
    public static HashMap<String,String> configHelper = null;

    private static String             user         = null;
    private static String             pass         = null;
    private static String             dni          = null;
    private static String             proxy        = null;
    private static String             browserPath  = null;
    private static Map<String,Object> outputFormat = null;
    private static Boolean            debug        = false;

    public YmlConfig(Boolean d){
        this(configPath + File.separator + fileName,d);
    }


    public YmlConfig(String configFile, Boolean myDebug){
        File file = new File(configFile);
        debug = myDebug;
        try{
           if(!file.exists() || (file.isDirectory())){
               file = new File(configPath + "/" + fileName);
           }
        }catch(Exception e){
            e.printStackTrace();
        }
        this.ReadConfig(file);

    }

    public void ReadConfig(File file){
        //File file = new File(configPath + "/" + fileName);
        InputStream io = null;
        if (!file.exists() || file.isDirectory()){
            // I generate the default configuration for the given file
            this.GenDefaultConfig(file);
            return;
        }
        try {
            io = new FileInputStream(file);

        }catch (Exception e){
            e.printStackTrace();
        }
        this.config = (Map<String,Object>)yml.load(io);
        Object myTmpAssig = null;
        proxy       = askSafely(((Map) config.get("Connection")).get("Proxy"));
        browserPath = askSafely(((Map) config.get("General")).get("Browser path"));
        user        = askSafely(((Map) config.get("Bank")).get("User"));
        dni         = askSafely(((Map) config.get("Bank")).get("DNI"));
        pass        = askSafely(((Map) config.get("Bank")).get("Pass"));
        try{
            outputFormat= (Map<String, Object>) config.get("Output");
        }catch(NullPointerException e){
            outputFormat = null;
        }

    }

    public String askSafely(Object input) {
        String ret = null;
        if (input != null) {
            try {
                ret = (String) input.toString();
            } catch (NullPointerException e) {
                ret = null;
            }
        }
        return ret;
    }


    public void GenDefaultConfig(String file){
        File mfile = new File(file);
        this.GenDefaultConfig(mfile);
    }
    /**
     * @param file
     *
     * Use this function to generate the default configuration (to the given file)
     */
    public void GenDefaultConfig(File file){
        String content =
                "General:\n    Browser path: /path/to/phantomjs\n\n"  +
                        "Connection:\n" +
                "    Proxy: none\n\n" +
                "Bank:\n    User: <username>\n" +
                "    Pass: <pass>\n    DNI: <dni>\n\nOutput:\n    Format: JSON\n" +
                        "   Bank: Rio\n" +
                        "   Method: HTTP_POST\n" +
                        "     UR: http://www.whatever.info:333/\n" +
                        "     Auth header: Auth header\n\n";
        try {
            //File file = new File(configPath + "/" + fileName);
            BufferedWriter output = null;
            output = new BufferedWriter(new FileWriter(file));
            output.write(content);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("File generated was: " + file.getName());

    }

    public String             getUser()        { return user;         }
    public String             getDni()         { return dni;          }
    public String             getPass()        { return pass;         }
    public String             getProxy()       { return proxy;        }
    public String             getBrowserPath() { return browserPath;  }
    public Map<String,Object> getOutputFormat(){ return outputFormat; }
    public Object             get(Object key)  { return config.get(key); }
    //public Object             get(Object key)


}
