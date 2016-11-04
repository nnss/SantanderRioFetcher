import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by mlo on 11/27/15.
 */
public class FormatedOutput {
    private static YmlConfig cfg;
    private static Boolean debug = false;

    public FormatedOutput(YmlConfig myConf, Boolean debug) {
        cfg = (YmlConfig) myConf;
        this.debug = debug;


    }

    public void doTheOutput(ArrayList<Hashtable<String, String>> myInputs) {
        if (debug)
            System.out.println("ddd:: about to process Output");

        for (Hashtable<String, String> myInput : myInputs) {
            if (((Map) cfg.get("Output")).get("Method").toString().matches("HTTP_POST")) {
                this.httpPostMethod(myInput);
            }
        }
    }

    /*
    * The configuration file should have a section for the output, something like:
    * Output:
    *   Format: JSON
    *   Bank: Rio
    *   Method: HTTP_POST
    *     UR: http://www.whatever.info:333/
    *     Auth header: Auth header
    *
    * With this ifnormation, I think that would be enough for now as
    * the only method that I would use by now is HTTP_POST/PUT
    */
    public String simpleArrayOutput(Hashtable<String, String> myInput) {
        // System.out.println("Printing the Output (joined result): " + Arrays.toString(myInput.entrySet().toArray()));
        Enumeration<String> tmp = myInput.keys();
        System.out.println("My output now is: " + tmp.toString());
        return Arrays.toString(myInput.entrySet().toArray());
    }

    public void httpPostMethod(Hashtable<String, String> myInput) {
        JSONObject myJson = new JSONObject();
        String tmp = "";
        Enumeration<String> k = myInput.keys();
        String v = null;

        //(k,v) = myInput.elements().nextElement();
        while (k.hasMoreElements()) {
            v = k.nextElement();
            // myJson["invests"] = "{" + v + ":" + myInput.get(v) +"}";
            try {
                myJson.append("Invests", new JSONObject().append(v, myInput.get(v)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead
        try {
            cfg = new YmlConfig(debug);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (debug)
                System.out.println("DEBUG::OUT I got: " + ((Map) cfg.get("Output")).get("URL").toString());
            HttpPost req = new HttpPost(((Map) cfg.get("Output")).get("URL").toString());

            // {"invest": { "bank": "santander","name" : "Super Ahorro PLUS A", "amount" : "90146.22]"}}
            String myStringJson = "{\"invest\": {";
            for (Object key : myInput.keySet()) {
                tmp = new String(myInput.get(key));
                String safe = key.toString().replaceAll("\\xF3", "o");
                //tmp = new String(myInput.get(key).getBytes(), UTF_8);
                //tmp = tmp.replaceAll("\\.","").replaceAll(",",".");
                //tmp = tmp.replaceAll(" ","").replaceAll("\\$","");
                System.out.println("DEBUG::AMOUNT::\"" + tmp.toString() + "\"");
                TestEncoding(safe);
                myStringJson += "\"bank\" : \"" + ((Map) cfg.get("Output")).get("Bank") + "\", " +
                        "\"name\": \"" + safe + "\", \"amount\": \"" + tmp + "\",";
            }
            myStringJson = myStringJson.substring(0, myStringJson.length() - 1);
            myStringJson += "}}";
            StringEntity params = new StringEntity(myStringJson);
            req.addHeader("content-type", "application/json; charset=UTF-8");
            req.addHeader("Accept", "application/json");
            String[] myTmpStr = null;
            for (String header : (List<String>) ((Map) cfg.get("Output")).get("Headers")) {
                myTmpStr = header.split(":", 2);
                req.addHeader(myTmpStr[0], myTmpStr[1]);
            }

            req.setEntity(params);
            HttpResponse res = httpClient.execute(req);

            if (debug)
                System.out.println("response " + res.getStatusLine().toString());

            if (res.getStatusLine().getStatusCode() != 201) {
                throw new Exception("Return status different from 200: " + res.getStatusLine());
            }

            // handle response here...
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void TestEncoding(String origin) {
        byte[] myBytes = null;

        try {
            myBytes = origin.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.exit(-1);
        }


    }
}
