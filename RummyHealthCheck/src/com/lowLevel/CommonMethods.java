package com.lowLevel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.csvreader.CsvWriter;
import com.gargoylesoftware.htmlunit.javascript.host.fetch.Response;
import com.google.gson.JsonObject;
import com.pageObjectRepo.HomePage;
import com.pageObjectRepo.LobbyPage;

import io.restassured.RestAssured;
import jxl.Sheet;
import jxl.Workbook;

public class CommonMethods {
	public static Logger logger = Logger.getLogger(CommonMethods.class);
	private final static String USER_AGENT = "Mozilla/5.0";

	private static enum pingResponse {
		SUCCESS, ERROR, CONNECTION_REFUSED, NO_INTERNET
	};

	public static CsvWriter csvOutput;
	public static Workbook wb;
	public static Sheet sheet;
	public static FileInputStream fs;
	public static WebDriver driver;
	static int rows;
	static int columns;
	public static int temp = 0;
	public static Properties prop = new Properties();
	// Select the browser
	public static boolean flag = false;
	public static String appServer;
	public static int statusCode;

	public static WebDriver loadPropertiesFile() {
		try {
			FileReader reader = new FileReader("src/config/config.properties");
			// FileReader reader = new FileReader("config.properties");
			prop.load(reader);
			appServer = prop.getProperty("url"); // Get url from properties file
			logger.info("loading  " + appServer);
		} catch (IOException e) {
			logger.error("Configuration file not loaded properly", e);
		}

		String browser = prop.getProperty("browserName"); // Assigning String
		// value form
		// configuraion file

		if (browser.equalsIgnoreCase("firefox")) {
			System.setProperty("webdriver.firefox.marionette", "geckodriver.exe");
			driver = new FirefoxDriver();
		} else if (browser.equalsIgnoreCase("chrome")) {
			// String path=System.getProperty("user.dir") +File.separator +
			// "Browsers"+ File.separator +"chromedriver.exe";
			// System.out.println(path);
			System.setProperty("webdriver.chrome.driver", "drivers/chromedriver.exe"); // setting
			// path
			// of
			// the
			// ChromeDriver
			driver = new ChromeDriver();
		} else if (browser.equalsIgnoreCase("PhantomJS")) {
			DesiredCapabilities caps = new DesiredCapabilities();
			caps.setJavascriptEnabled(true);
			caps.setCapability("takesScreenshot", true);
			caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "drivers/phantomjs.exe");
			// caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
			// "phantomjs");
			driver = new PhantomJSDriver(caps);
		}
		return driver;
	}

	public static void openURL() {
		try {
			driver.get(appServer);
		} catch (Exception e) {
			logger.error("Page not loaded properly", e);
		}
	}

	public static int getResponseCode(String urlString) throws Exception {
		return RestAssured.get(urlString).statusCode();
	}

	// Home page object
	public static void InitilazeObjectsOfHomePage() {

		HomePage pageObject = PageFactory.initElements(CommonMethods.driver, HomePage.class);
	}

	// Lobby page objects
	public static void InitilazeObjectsOfLobbyPage() {

		LobbyPage LobbypageObject = PageFactory.initElements(CommonMethods.driver, LobbyPage.class);
	}

	// Maximize window
	public static void windowMax() {
		driver.manage().window().maximize();

	}

	// Delete cookies
	public static void deletecookies() {
		driver.manage().deleteAllCookies();
	}

	// Implicit wait
	public static void callingImplicitSleep() {
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

	}

	public static void openBrowser() {

		loadPropertiesFile();
		windowMax();
		deletecookies();
		pingResponse status = getHTTPStatusCodeResponse();
		if (status.toString() == "SUCCESS") {
			openURL();
		} else if (status.toString() == "CONNECTION_REFUSED") {
			int responseOfPassOrFail = getFinalStatusOfPassOrFailure();
			try {
				CommonMethods.httpCall("" + responseOfPassOrFail);
			} catch (Exception e) {
				logger.error("received error message", e);
			}
		}
	}

	public static pingResponse getHTTPStatusCodeResponse() {
		pingResponse response = pingResponse.ERROR;

		getStatusCode: for (int i = 0; i < 3; i++) {
			try {
				statusCode = getResponseCode(appServer);
			} catch (UnknownHostException e1) {
				response = pingResponse.NO_INTERNET;
				logger.error("UnknownHostException ", e1);
			} catch (ConnectException e2) {
				response = pingResponse.CONNECTION_REFUSED;
				logger.error("ConnectException ", e2);
			} catch (Exception e3) {
				response = pingResponse.ERROR;
				logger.error("IOException ", e3);
			}
			if (200 != statusCode) {
				logger.info(response);
				System.out.println(" gave a response code of " + statusCode);
				logger.info("login tried" + ++CommonMethods.temp + " time");
				CommonMethods.driver.navigate().refresh();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
				// response = pingResponse.ERROR;
			} else {
				response = pingResponse.SUCCESS;
				System.out.println(response);
				break getStatusCode;
			}
		}
		return response;

	}

	public static void openBroswerAndInitialzePageObjectRepo() throws IOException {
		openBrowser();
		InitilazeObjectsOfHomePage();
		InitilazeObjectsOfLobbyPage();
	}

	public static int getFinalStatusOfPassOrFailure() {
		int retrunedValue = temp;
		if (retrunedValue == 3 && flag == false) {
			logger.error("Test case failed");
			System.out.println("Test case failed");
			retrunedValue = 0;
			logger.error("Returned value is " + retrunedValue);
			System.out.println("Returned value status " + retrunedValue);
			return retrunedValue;
		} else if (retrunedValue == 2 && flag == false) {
			logger.error("Test case failed");
			System.out.println("Test case failed");
			retrunedValue = 0;
			logger.error("Returned value is " + retrunedValue);
			System.out.println("Returned value status " + retrunedValue);
			return retrunedValue;
		} else if (retrunedValue == 1 && flag == false) {
			logger.error("Test case failed");
			System.out.println("Test case failed");
			retrunedValue = 0;
			logger.error("Returned value status " + retrunedValue);
			System.out.println("Returned value status " + retrunedValue);
			return retrunedValue;
		} else if (retrunedValue == 0 && flag == false) {
			logger.error("Test case failed");
			System.out.println("Test case failed");
			retrunedValue = 0;
			logger.error("Returned value status " + retrunedValue);
			System.out.println("Returned value status " + retrunedValue);
			return retrunedValue;
		} else {
			logger.info("Test cases pass successfully");
			retrunedValue = 1;
			logger.info("Returned status is " + retrunedValue);
			return retrunedValue;

		}
	}

	public static String currentTimeMenthod() {

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		System.out.println(sdf.format(cal.getTime()));
		return (sdf.format(cal.getTime())).replaceAll("[^0-9]+", "");
	}

	public static String appendTodayDateAndCurrentTime() {

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		CsvWritter.appendDateinCsvFile(sdf.format(cal.getTime()));
		return (sdf.format(cal.getTime()));
	}

	public static void httpCall(String status) throws ClientProtocolException, IOException {

		String url = "https://hooks.slack.com/services/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";
		// String url =
		// "https://hooks.slack.com/services/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";

		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);

		// add header
		post.setHeader("Content-type", "application/json");
		post.setHeader("status", status);

		JsonObject jsonObj = new JsonObject();
		jsonObj.addProperty("text", "Seems, A valid user is not able to login");
		StringEntity jsonString = new StringEntity(jsonObj.toString(), ContentType.APPLICATION_JSON);
		post.setEntity(jsonString);

		HttpResponse response = httpClient.execute(post);
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + post.getEntity());
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		System.out.println(result.toString());

	}

}
