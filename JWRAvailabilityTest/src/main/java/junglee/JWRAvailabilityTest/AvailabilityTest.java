package junglee.JWRAvailabilityTest;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;


public class AvailabilityTest 
{
    public static void main( String[] args )
    {
    	Properties prop = new Properties();
    	InputStream in = null;

		try {
			in = new FileInputStream("./config/phantom.properties");
			prop.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	TestLivelyness(prop);
    }
    
    private static void TestLivelyness(Properties prop){
    	PhantomJSDriver driver;

		DesiredCapabilities caps = new DesiredCapabilities();
		caps.setJavascriptEnabled(true);                
		caps.setCapability("takesScreenshot", true);  
        caps.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, prop.getProperty("phantomjs.binarypath")
            );
		driver = new PhantomJSDriver(caps);	
		
	    driver.get(prop.getProperty("login.weburl"));
	    
	    String loginPageTitle = driver.getTitle();
	    
		driver.manage().deleteAllCookies();
		driver.findElement(By.id("username")).sendKeys(prop.getProperty("login.username"));
		driver.findElement(By.id("loginPassword")).sendKeys(prop.getProperty("login.password"));

		System.out.println("click on login button");
		driver.findElement(By.id("loginForm")).submit();
		System.out.println("clicked on login button");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		String AR= driver.getTitle();
		String ER = prop.getProperty("login.title");

		if(AR.equals(ER)){
			System.out.println("login success");
		}
		else if (driver.getTitle().equals(loginPageTitle))
		{
			driver.navigate().refresh();
			System.out.println("login fail");
		}
    }
}
