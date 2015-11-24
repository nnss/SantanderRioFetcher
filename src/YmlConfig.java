import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by matias <matiaspalomec@gmail.co> on 02/03/15.
 */
public class YmlConfig {

    private static String configPath = System.getProperty("user.home");
    private static String fileName   = ".bancorio_fetcher.yml"; // Yes, yes, I know, but I still buying leader jackets
                                                               // at Cagning 910.
    private static Map<String, Object> config = null;
    Yaml yml = new Yaml();
    public static HashMap<String,String> configHelper = null;

    private static String user = null;
    private static String pass = null;
    private static String dni = null;
    private static String proxy = null;
    private static String browserPath = null;

    public YmlConfig(String configFile){
        File file = new File(configFile);
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
        // this.configHelper = ((Map) config.get("Connection")).get("Proxy").toString());
        // this.configHelper = ((Map) config.get(("Bank")).get("User").toString();)
        proxy = (String) ((Map) config.get("Connection")).get("Proxy").toString();
        browserPath = (String) ((Map) config.get("General")).get("Browser path").toString();
        user = (String) ((Map) config.get("Bank")).get("User").toString();
        dni = (String) ((Map) config.get("Bank")).get("DNI").toString();
        pass = (String) ((Map) config.get("Bank")).get("Pass").toString();

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
                "\n\nConnection:\n" +
                "    Proxy: none\n\n" +
                "Bank:\n    User: <username>\n" +
                "    Pass: <pass>\n    DNI: <dni>\n\n";
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

    public String getUser(){ return user; }
    public String getDni(){ return dni;}
    public String getPass(){ return pass; }
    public String getProxy(){ return proxy; }
    public String getBrowserPath(){ return browserPath; }


}
