/**
 * 
 */
import org.apache.commons.cli.*;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * @author matias
 *
 */
public class SRFetcher {

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

        CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		
		try {
			cmd = parser.parse( options, args);
		} catch (ParseException e) {
			e.printStackTrace();
		}

        if(cmd.hasOption("c"))
            configFile = cmd.getOptionValue("c");

        YmlConfig cfgReal = new YmlConfig(configFile);
        dni = cmd.getOptionValue("d");
        pass = cmd.getOptionValue("p");
        user = cmd.getOptionValue("u");
        if(dni == null)
            dni = cfgReal.getDni();
        if(pass == null)
            pass = cfgReal.getPass();
        if(user == null)
            user = cfgReal.getUser();

		if(cmd.hasOption("w"))
			phantomExec = cmd.getOptionValue("w");
		else if (cfgReal.getBrowserPath() != null){
			phantomExec = cfgReal.getBrowserPath();
		}else{
			System.out.println("3rd part, went south");

		}

        if((user == null) || (pass == null) || (dni == null)){
            throw new Exception("Missing user|pass|dni");
        }
		SRFetcher fetcher = new SRFetcher(dni,pass,user );



        if(cmd.hasOption("C")){
            SRFetcher.checkConfig();
            System.exit(0);
        }
        System.out.println("I got phantom as: '" + phantomExec + "'");
        fetcher.initConnection();

        if(cmd.hasOption("g")){
            cfgReal.GenDefaultConfig(configFile);
        }

		int flag = 0;
		if(cmd.hasOption("m")){
			System.out.println("about to call doFetchMoves");
			fetcher.doFetchMoves();
			flag = 1;
		}
		
		if(cmd.hasOption("f")){
			System.out.println("about to call doFetchInvest");
			fetcher.doFetchInvest();
			flag = 1;
		}

        if(cmd.hasOption("a")){
            fetcher.doFetchInvest();
            fetcher.doFetchMoves();
        }else if (flag == 0){
			//fetcher.doFetchInvest();
			fetcher.doFetchMoves();
		}
		
		System.out.println("About to quit");
		fetcher.doQuit();
		System.out.println("About to exit");
		System.exit(0);

	}

    /**
     *
     */
    public void initConnection(){


        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true); // enabled by default

        caps.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                phantomExec
        );

        this.driver = new PhantomJSDriver(caps);
        this.driver.get(homeUrl);

        try{
            if(this.driver.findElement(By.xpath(loginErrorXpath)).getText().
                    matches("Para iniciar esta nueva ses.* de Online Banking debe finalizar la sesión anterior")){
                System.err.println("RELOGIN:: a session was still opened and I'm trying to close and open it again");
                this.doQuit();
                this.driver = new PhantomJSDriver();
                this.driver.get(homeUrl);
            }
        }catch (NoSuchElementException e){
            //e.printStackTrace();
            System.err.println("It's OK, this should be seen instead of the relogin message");
        }

        WebElement theID = driver.findElement(By.id("dni"));
        theID.sendKeys(this.getDni() );
        WebElement thePass = driver.findElement(By.id("clave"));
        thePass.sendKeys(this.getPass());
        WebElement theUser = driver.findElement(By.id("usuario"));
        theUser.sendKeys(this.getUser());
        // click the accept button
        driver.findElement(By.xpath("/html/body/div[2]/table[2]/tbody" +
                "/tr/td[2]/table/tbody/tr[1]/td[4]/table/tbody/tr[2]/td" +
                "/center/table[1]/tbody/tr[4]/td/input")).click();

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
	public void doFetchInvest(){
		this.driver.get(urlInvest);
		List<WebElement> totals = driver.findElements(By.xpath(tableInvestTotals));

		List<WebElement> names = driver.findElements(By.xpath(tableInvestNames));
		Hashtable<String,String> finalList = new Hashtable<String,String>();
		for ( WebElement total :  totals ){
			for(WebElement name : names ) {
				finalList.put(name.getText(),total.getText() );
			}
		}
		
		System.out.println("Now, the final act: " + Arrays.toString(finalList.entrySet().toArray()));
	}

    public static void checkConfig(){
        System.out.println("About to check the configuration");
        File cfg = new File(configFile);
        if(cfg.exists()){
            YmlConfig cfgReal = new YmlConfig(configFile);
            System.out.println("For config file '" + configFile.toString() + "'");
            System.out.println("Given user: '" + cfgReal.getUser() + "'");
            System.out.println("Given pass: '" + cfgReal.getPass() + "'");
            System.out.println("Given dni: '" + cfgReal.getDni() + "'");
            System.out.println("Given proxy: '" + cfgReal.getProxy() + "'");
        }else{
            System.out.println("check file: '" + configFile + "");
        }
    }
	
	public void doFetchMoves(){
		System.out.println("inside doFetchMoves");
		this.driver.get(initPage);
		//driver.findElement(By.xpath(lastMoves)
	}
	
	 public void doQuit(){
		 // close the session
		 driver.get(insideUrl);
		 driver.switchTo().frame("frame1");
		 if (driver instanceof JavascriptExecutor) {
			 ((JavascriptExecutor) driver)
			 .executeAsyncScript(quitFunc);
		 }
		 
		 this.driver.findElement(By.xpath(quitXPath)).click();
		 this.driver.get("about:blank");
		 this.driver.close();
		 //System.exit(0);
	}
	
	public void lastMovements(WebDriver driver){
		driver.get(insideUrl);;
		driver.switchTo().frame("frame2");
		System.out.println("attribute href " + driver.findElement(By.xpath(lastMovesA)).getAttribute("href").toString());
		// ((JavascriptExecutor) driver).executeScript(lastMoves);
	}

    public void generateOutput(){

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
