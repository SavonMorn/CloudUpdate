
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class webFileCheck implements Runnable {
    //Passed Data
    protected CompletableFuture<Map<String, String>> cloudFileFut;
    protected CompletableFuture<Set<String>> cloudFolderFut;
    private Map<String, String> fileStorage = new HashMap<String, String>();
    private Map<String, String> folderStorage = new ConcurrentHashMap<String, String>();
    private Map<String, String> nonExplFolderID = new HashMap<String, String>();
    protected String logins[] = {"",""};
    protected String startPage;
    protected JavascriptExecutor js;
    protected WebDriverWait webWait;
    protected ChromeDriver driver;
    //Final Data
    protected final String mainDivXpath = "//*[@id=\"file-viewer-scroller\"]/div[2]/div/div";
    protected final String changeLayoutX = "/html/body/main/app-root/app-content/div/app-file-manager/app-toolbar/div/div[2]/div[3]/img[2]";
    protected final String nameXPt1 = "//*[@id=\"file-viewer-scroller\"]/div[2]/div/div[";
    protected final String nameXPt2 = "]/div/div/div/div[2]/p";
    protected final String dateXPt1 = "//*[@id=\"file-viewer-scroller\"]/div[2]/div/div[";
    protected final String dateXPt2 = "]/div/div/div/div[2]/div/app-text-date/h3";
    protected final String elementImgXPt1 = "//*[@id=\"file-viewer-scroller\"]/div[2]/div/div[";
    protected final String elementImgXPt2 = "]/div/div/div/div[1]/app-thumbnail/img";
    protected final String hasDataDivX = "//*[@id=\"file-viewer-scroller\"]";
    protected final String noDataDivX = "/html/body/main/app-root/app-content/div/app-file-manager/div/div";
    protected final String folderImg = "https://app.degoo.com/assets/icons/folder.svg";


    public webFileCheck(String startLoc, String logins[], 
        CompletableFuture<Map<String, String>> fileFut, 
        CompletableFuture<Set<String>> folderFut){ //Constructor
        this.startPage = startLoc;
        this.cloudFileFut = fileFut;
        this.cloudFolderFut = folderFut;
        this.logins = logins;
    }
        //Copy constructor
    public webFileCheck(webFileCheck old){
        cloudFileFut = old.cloudFileFut;
        cloudFolderFut = old.cloudFolderFut;
        logins = old.logins;
        startPage = old.startPage;
        js = old.js;
        webWait = old.webWait;
        driver = old.driver;
    }

    private void populateMaps(String parentPath, Boolean inLocation){
        if(nonExplFolderID.get(parentPath)!=null||inLocation==true){
            //If not in location
            if(inLocation!=true){
                driver.navigate().to("https://app.degoo.com/files/"+nonExplFolderID.get(parentPath));
                nonExplFolderID.remove(parentPath);
            }
            //Confirm location is loaded
            webWait.until(ExpectedConditions.or(ExpectedConditions.visibilityOfElementLocated(By.xpath(hasDataDivX)),
                ExpectedConditions.visibilityOfElementLocated(By.xpath(noDataDivX)))
            );
            //test if there are files in this location
            if(driver.findElements(By.xpath(hasDataDivX)).size()!=0){ 
                //Check if theres anymore files on page by scrolling the bottom of the page
                js.executeScript("window.scrollTo(0,document.body.scrollHeight)");
                js.executeScript("window.scrollTo(0,document.body.scrollHeight)");
                //Count how many files and sort to folders and files
                WebElement oldFirst = null;
                do{
                    List<WebElement> temp = driver.findElements(By.xpath(mainDivXpath));
                    for(int i = 1; i<temp.size()+1; i++){
                        if(oldFirst==null||!oldFirst.equals(driver.findElement(By.xpath(elementImgXPt1+i+elementImgXPt2)))){
                            webWait.until(ExpectedConditions.or(ExpectedConditions.visibilityOfElementLocated(By.xpath(elementImgXPt1+i+elementImgXPt2)),
                                ExpectedConditions.visibilityOfElementLocated(By.xpath(elementImgXPt1+i+"]/div/div/div/div[1]/app-thumbnail/img[2]")))
                            );
                            if(driver.findElement(By.xpath(elementImgXPt1+i+elementImgXPt2)).getAttribute("src").equals(folderImg)){
                                //If folder, format and insert into folder map while storing the id and parent path
                                String newPath = parentPath +"\\"+ driver.findElement(By.xpath(nameXPt1+i+nameXPt2)).getText();
                                nonExplFolderID.put(newPath, driver.findElement(By.xpath(nameXPt1+i+"]/div/div")).getAttribute("item-id"));
                                folderStorage.put( newPath, "");
                            }else{
                                //If file, format and insert into file map
                                fileStorage.put(
                                    parentPath +"\\"+ driver.findElement(By.xpath(nameXPt1+i+nameXPt2)).getText(),
                                    driver.findElement(By.xpath(dateXPt1+i+dateXPt2)).getText()
                                );
                            }  
                        }else{ break;}
                    }

                    //Try scrolling up to see if ther are more elements, and if their are repeat and continue scrolling
                    oldFirst = driver.findElement(By.xpath(elementImgXPt1+1+elementImgXPt2));
                    js.executeScript("window.scrollBy(0,-745)");
                    if(oldFirst.equals(driver.findElement(By.xpath(elementImgXPt1+1+elementImgXPt2)))){
                        oldFirst = null;
                    }
                }while(oldFirst!=null);

                //After data is sorted repeat for each folder in current location 
                for(String a : folderStorage.keySet()){
                    if(a.contains(parentPath)&&!a.equals(parentPath)){
                        populateMaps(a, false);
                    }
                }
            
            }
        }
    }

    private void populateMaps(ChromeDriver driver, WebDriverWait webWait){
        this.populateMaps("", true);
    }

    public void run() {
        System.setProperty("webdriver.chrome.driver", "drivers\\chromedriver.exe");
        driver = new ChromeDriver();
        webWait = new WebDriverWait(driver, 30);
        js = (JavascriptExecutor) driver;
        driver.navigate().to(startPage);

        //Login to site
        webWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"username\"]")));
        driver.findElement(By.xpath("//*[@id=\"username\"]")).sendKeys(logins[0]);
        driver.findElement(By.xpath("//*[@id=\"password\"]")).sendKeys(logins[1]);
        driver.findElement(By.xpath("/html/body/main/app-root/app-login/div/app-login-form/div/form/div/button[2]")).click();

        //Change layout to show date
        webWait.until(ExpectedConditions.elementToBeClickable(By.xpath(changeLayoutX)));
        driver.findElement(By.xpath(changeLayoutX)).click();

        //Begin data collection
        populateMaps(driver, webWait);
        driver.close();
        
        
        cloudFileFut.complete(fileStorage);
        cloudFolderFut.complete(folderStorage.keySet());
        
    }

}