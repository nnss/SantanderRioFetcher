/**
 * 
 */
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.JavascriptExecutor;

/**
 * @author matias
 *
 */
public class SRFetcher {

	static String homeUrl = "https://www.personas.santanderrio.com.ar/hb/html/login/principal.jsp";
	static String insideUrl = "https://www.personas.santanderrio.com.ar/hb/html/common/fInicio.jsp";
	static String quitXPath = "/html/body/table[1]/tbody/tr[2]/td/a[6]";
	static String quitFunc = "windowExit();";
	
	
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
		
		
		this.driver = new PhantomJSDriver();
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
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("u",true,"user");
		options.addOption("p",true,"pass");
		options.addOption("d",true,"dni");
		options.addOption("m",false,"movements");
		options.addOption("f",false,"fondos");
		
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		
		try {
			cmd = parser.parse( options, args);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		SRFetcher fetcher = new SRFetcher(cmd.getOptionValue("d"),cmd.getOptionValue("p"),cmd.getOptionValue("u") );
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
		
		if(flag == 0){
			//fetcher.doFetchInvest();
			fetcher.doFetchMoves();
		}
		
		System.out.println("About to quit");
		fetcher.doQuit();
		System.out.println("About to exit");
		System.exit(0);

	}

	/**
	 * @params
	 * 
	 * By now, this function doesn't return anything, just prints in screen the
	 * invest name and the amount of money in it.
	 */
	public void doFetchInvest(){
		System.out.println("inside doFetchInvest");
		this.driver.get(urlInvest);
		List<WebElement> totals = driver.findElements(By.xpath(tableInvestTotals));
		System.out.println("I got " + totals.size());
		
		List<WebElement> names = driver.findElements(By.xpath(tableInvestNames));
		System.out.println("I got " + names.size());
		Hashtable<String,String> finalList = new Hashtable<String,String>();
		for ( WebElement total :  totals ){
			for(WebElement name : names ) {
				finalList.put(name.getText(),total.getText() );
			}
		}
		
		System.out.println("Now, the final act: " + Arrays.toString(finalList.entrySet().toArray()));
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
