import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by matias on 02/03/15.
 */
public class YmlConfig {

    private static String configPath = System.getProperty("user.home");
    private static String fileName   = ".bancorio_fetcher.yml"; // Yes, yes, I know, but I still buying leader jackets
                                                               // at Cagning 910.
    private static Map<String, Object> config = null;
    Yaml yml = new Yaml();
    public static HashMap<String,String> configHelper = null;

    public YmlConfig(String configFile){
        File file = new File(configFile);
        try{
           if(!file.exists() || (file.isDirectory())){
               file = new File(configPath + "/" + fileName);
           }
        }catch(Exception e){
            e.printStackTrace();
        }
        this.ReadConfig();

    }

    public void ReadConfig(){
        File file = new File(configPath + "/" + fileName);
        InputStream io = null;
        if (!file.exists() || file.isDirectory()){
            this.GenDefaultConfig();
            return;
        }
        try {
            io = new FileInputStream(file);

        }catch (Exception e){
            e.printStackTrace();
        }
        this.config = (Map<String,Object>)yml.load(io);
        this.configHelper = ((Map) config.get("Connection")).get("Proxy").toString());
        this.configHelper = ((Map) config.get(("Bank")).get("User").toString();)
    }

    /**
     * @param none
     *
     * Use this function to generate the default configuration (
     */
    public void GenDefaultConfig(){
        String content = "\n\nConnection\n\tProxy: none\n\nBank\n\tUser: <username>\n\tPass: <pass>\n\tDNI: <dni>\n\n";
        try {
            File file = new File(configPath + "/" + fileName);
            BufferedWriter output = null;
            output = new BufferedWriter(new FileWriter(file));
            output.write(content);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
