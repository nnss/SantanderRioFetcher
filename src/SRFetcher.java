/**
 * 
 */

import org.apache.commons.cli.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @author matias
 *
 */
public class SRFetcher {
    /*
     * There are a lot of statics but as this program is not ment to be something protable and adaptable,
     * I have to be able to configure it in case of something changes, and some parts could be implemented
     * as statics. In a more stable version, this should be organized in (at least) two parts, the real pats
     * that can be configured as statics (direct access to some parts of the program), and parts of the
     * "path" to a control/page/button/whatever should be implemented directly in the code, not here as an
     * static.
     */

	static String homeUrl = "https://www.personas.santanderrio.com.ar/hb/html/login/principal.jsp";
	static String insideUrl = "https://www.personas.santanderrio.com.ar/hb/html/common/fInicio.jsp";
	static String quitXPath = "/html/body/table[1]/tbody/tr[2]/td/a[6]";
	static String quitFunc = "windowExit();";
    static String configFile = System.getProperty("user.home") + File.separator + ".bancorio_fetcher.yml";
	
	
	// path:: urlInvest + each td2++ and td7 summed
	static String urlInvest = "https://www.personas.santanderrio.com.ar/hb/html/inversiones/invRes.jsp";
	//static String lastMoves = "/html/body/div[2]/table/tbody/tr[2]/td[2]/table/tbody/tr[2]/td[2]/map[35]/table[4]/tbody/tr[2]/td[4]/a[1]";
	static String tableInvestTotals = "//tbody//tbody//td[5]";
	static String tableInvestNames = "//table[2]/tbody//table/tbody//td[1]";
	static String tdExample = "/html/body/div[2]/table/tbody/tr[2]/td[2]/table[2]/tbody/tr[2]/td[7]";
    static String acceptButton = "//*[@id=\"btn1\"]/i";

    // path:: initPage -> lastMovesA or execScript lastMoves (try both)
	static String initPage = "https://www.personas.santanderrio.com.ar/hb/html/bienvenida/fBienvenida.jsp";
	static String lastMovesA = "/html/body/div[2]/table/tbody/tr[2]/td[2]/table/tbody/tr[2]/td[2]/map[35]/table[4]/tbody/tr[2]/td[4]/a[1]";
	static String lastMoves = "javascript:preesCtaExt ('CU','014-189278/9');";
	static String last7XLS = "/html/body/div[2]/table/tbody/tr[2]/td[2]/form[1]/table[2]/tbody/tr[5]/td/input[2]";
	
	// relogin message
	static String loginErrorXpath = "/html/body/center/table/tbody//th/p";

	private static String user = null;
	private static String pass = null;
	private static String dni = null;
    private static String phantomExec = "phantomjs";
    private static Boolean debug = false;


	private WebDriver driver = null;


	
	
	// /html/body/div[2]/table[2]/tbody/tr/td[2]/table/tbody/tr[1]/td[4]/table/tbody/tr[2]/td/center/table[1]/tbody/tr[4]/td/input
	/*
			CN=S
			DIRECCIONACA=A
			NROCTA=014-189278/9
			TIPOCTA=CU
	*/
	
	public SRFetcher(String dni, String pass, String user) {
        this.setUser(user);
        this.setPass(pass);
        this.setDni(dni);

    }

	/**
	 * @param
	 */
	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption("u",true,"user");
		options.addOption("p",true,"pass");
		options.addOption("d",true,"dni");
		options.addOption("m",false,"movements");
		options.addOption("f",false,"investment");
        options.addOption("a",false,"all checks");
        options.addOption("C",false,"check fonfig");
        options.addOption("c",true,"given configfile");
        options.addOption("g",false,"generate config");
        options.addOption("w",true,"path to phantomJS");
        options.addOption("j",false,"generate JSON output");
        options.addOption("D",false,"print debug information");

        CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		
		try {
			cmd = parser.parse( options, args);
		} catch (ParseException e) {
			e.printStackTrace();
		}

        if(cmd.hasOption("c"))
            configFile = cmd.getOptionValue("c");

        YmlConfig cfgReal = new YmlConfig(configFile,debug);
        dni = cmd.getOptionValue("d");
        pass = cmd.getOptionValue("p");
        user = cmd.getOptionValue("u");
        if(dni == null)
            dni = cfgReal.getDni();
        if(pass == null)
            pass = cfgReal.getPass();
        if(user == null)
            user = cfgReal.getUser();


        if((user == null) || (pass == null) || (dni == null)){
            throw new Exception("Missing user|pass|dni");
        }
		SRFetcher fetcher = new SRFetcher(dni,pass,user );


        if (cmd.hasOption("w"))
            phantomExec = cmd.getOptionValue("w");
        else if (cfgReal.getBrowserPath() != null) {
            phantomExec = cfgReal.getBrowserPath();
        } else {
            phantomExec = fetcher.findExecInPath(phantomExec);
        }


        if(cmd.hasOption("C")){
            SRFetcher.checkConfig();
            System.exit(0);
        }
        if(debug)
            System.out.println("I got phantom as: '" + phantomExec + "'");
        fetcher.initConnection();

        if(cmd.hasOption("g")){
            cfgReal.GenDefaultConfig(configFile);
        }

        if(cmd.hasOption("D"))
            debug = true;

		int flag = 0;
        FormatedOutput formater = new FormatedOutput( (Map<String,Object>) cfgReal.getOutputFormat());
		if(cmd.hasOption("m")){
			if(debug)
                System.out.println("about to call doFetchMoves");
			fetcher.doFetchMoves();
			flag = 1;
		}
		
		if(cmd.hasOption("f")){
			if(debug)
                System.out.println("about to call doFetchInvest");
			fetcher.doFetchInvest();
            formater.simpleArrayOutput(fetcher.doFetchInvest());
			flag = 1;
		}

        if(cmd.hasOption("a")){
            fetcher.doFetchInvest();
            fetcher.doFetchMoves();
        } else if (flag == 0) {
            //fetcher.doFetchInvest();
			fetcher.doFetchMoves();
		}

		if(debug)
            System.out.println("About to quit");
		fetcher.doQuit();
		if(debug)
            System.out.println("About to exit");
		System.exit(0);

    }

    public String findExecInPath(String cmd) {
        List<String> myPaths = Arrays.asList(System.getenv("PATH").split(":"));
        for (String myPath : myPaths) {
            if (debug)
                System.err.println("myPath::" + myPath);
            if (new File(myPath + "/" + phantomExec).exists()) {
                return myPath + "/" + phantomExec;
            }
        }
        return phantomExec;
    }

    /**
     *
     */
    public void initConnection(){


        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true); // enabled by default

        String[] phantomArgs = new String[] { "--webdriver-loglevel=NONE" };

        caps.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomExec);
        caps.setCapability(
                PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs
        );

        this.driver = new PhantomJSDriver(caps);
        this.driver.get(homeUrl);

        try{
            if(this.driver.findElement(By.xpath(loginErrorXpath)).getText().
                    matches("Para iniciar esta nueva ses.* de Online Banking debe finalizar la sesión anterior")){
                if(debug)
                    System.out.println("RELOGIN:: a session was still opened and I'm trying to close and open it again");
                this.doQuit();
                this.driver = new PhantomJSDriver();
                this.driver.get(homeUrl);
            }
        }catch (NoSuchElementException e){
            //e.printStackTrace();
            if(debug)
                System.out.println("It's OK, this should be seen instead of the relogin message");
        }

        WebElement theID = driver.findElement(By.id("dni"));
        theID.sendKeys(this.getDni() );
        WebElement thePass = driver.findElement(By.id("clave"));
        thePass.sendKeys(this.getPass());
        WebElement theUser = driver.findElement(By.id("usuario"));
        theUser.sendKeys(this.getUser());
        if(debug)
            System.err.println(driver.getPageSource());
        // click the accept button
        driver.findElement(By.xpath(acceptButton)).click();

		/*
		List<WebElement> frameList = driver.findElements(By.tagName("frame"));
		for (WebElement frame : frameList){
			System.out.println("I got frame named: " + frame.getAttribute("name"));
		}
		*/

    }


	/**
	 * @params
	 * 
	 * By now, this function doesn't return anything, just prints in screen the
	 * invest name and the amount of money in it.
	 */
    public Hashtable<String, String> doFetchInvest() {
        this.driver.get(urlInvest);
		List<WebElement> totals = driver.findElements(By.xpath(tableInvestTotals));

		List<WebElement> names = driver.findElements(By.xpath(tableInvestNames));
		Hashtable<String,String> finalList = new Hashtable<String,String>();
		for ( WebElement total :  totals ){
			for(WebElement name : names ) {
				finalList.put(name.getText(),total.getText() );
			}
		}
		
		if (debug)
            System.out.println("Now, the final act: " + Arrays.toString(finalList.entrySet().toArray()));
        return finalList;
    }

    public static void checkConfig(){
        if(debug)
            System.out.println("About to check the configuration");
        File cfg = new File(configFile);
        if(cfg.exists()){
            YmlConfig cfgReal = new YmlConfig(configFile,debug);
            if(debug) {
                System.out.println("For config file '" + configFile.toString() + "'");
                System.out.println("Given user: '" + cfgReal.getUser() + "'");
                System.out.println("Given pass: '" + cfgReal.getPass() + "'");
                System.out.println("Given dni: '" + cfgReal.getDni() + "'");
                System.out.println("Given proxy: '" + cfgReal.getProxy() + "'");
                System.out.println("Given phantomjs: '" + cfgReal.getBrowserPath() + "'");
            }
        }else{
            System.err.println("check file: '" + configFile + "");
        }
    }
	
	public void doFetchMoves(){
		if(debug)
            System.out.println("inside doFetchMoves");
		this.driver.get(initPage);
		//driver.findElement(By.xpath(lastMoves)
	}
	
	 public void doQuit(){
		 // close the session
		 driver.get(insideUrl);
		 driver.switchTo().frame("frame1");
         /**
          * I really do not know why I added this, but after executing this
          * the program hangs and doesn go outside the if
          if (driver instanceof JavascriptExecutor) {
          System.out.println("ended 3.1");
          ((JavascriptExecutor) driver)
			 .executeAsyncScript(quitFunc);
		 }
          */

		 this.driver.findElement(By.xpath(quitXPath)).click();
		 this.driver.get("about:blank");
		 this.driver.close();
         this.driver.quit();
         System.exit(0);
     }
	
	public void lastMovements(WebDriver driver){
		driver.get(insideUrl);;
		driver.switchTo().frame("frame2");
		if(debug)
            System.out.println("attribute href " + driver.findElement(By.xpath(lastMovesA)).getAttribute("href").toString());
		// ((JavascriptExecutor) driver).executeScript(lastMoves);
	}

	public  String getUser() {
		return user;
	}

	public  void setUser(String user) {
		SRFetcher.user = user;
	}

	public  String getPass() {
		return pass;
	}

	public  void setPass(String pass) {
		SRFetcher.pass = pass;
	}

	public  String getDni() {
		return dni;
	}

	public  void setDni(String dni) {
		SRFetcher.dni = dni;
	}



}
