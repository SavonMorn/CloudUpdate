import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class fileTransfer extends webFileCheck {
    private CompletableFuture<Map<String, String>> localFileFut;
    private CompletableFuture<Set<String>> localFolderFut;
    private CompletableFuture<Boolean> transTest;
    private Map<String, String> localFileStorage;
    private Set<String> localFolderStorage;
    private Map<String, String> cloudFileStorage;
    private Set<String> cloudFolderStorage;
    private Set<String> tempStorage = new HashSet<String>();
    //Final variables
    private final String optionsXpt1 = "/html/body/main/app-root/app-content/div/app-file-manager/div/app-smart-file-viewer/div/app-file-viewer/virtual-scroller/div[2]/div/div[";
    private final String optionsXpt2 = "]/div/div/div/div[3]/div/img";

    public fileTransfer(CompletableFuture<Boolean> Test, webFileCheck old,
        CompletableFuture<Map<String, String>> localFile, CompletableFuture<Set<String>> localFolder) 
        {
        super(old);
        localFileFut = localFile;
        localFolderFut = localFolder;
        transTest = Test;
    }

    public void deleteElements(Set<String> paths, Boolean isFile) {
        String parentPath = "";
        Map<String, String> nextFolders = new HashMap<String, String>();
        Map.Entry<String, String> nextFolder;
        Boolean parseFiles = false;
        Boolean repeat = false;
        do {
            // Ensure page is loaded
            webWait.until(ExpectedConditions.or(ExpectedConditions.visibilityOfElementLocated(By.xpath(hasDataDivX)),
                    ExpectedConditions.visibilityOfElementLocated(By.xpath(noDataDivX))));
            if (isFile) {
                for (String x : paths) {
                    if (x.replaceFirst("[\\\\][^\\\\]+$", "").equals(parentPath)) {
                        parseFiles = true;
                    }
                }
            } else {
                parseFiles = false;
            }
            // test if there are files in this location
            if (driver.findElements(By.xpath(hasDataDivX)).size() != 0) {
                // Check if theres anymore files on page by scrolling the bottom of the page
                js.executeScript("window.scrollTo(0,document.body.scrollHeight)");
                js.executeScript("window.scrollTo(0,document.body.scrollHeight)");
                // Count how many files and sort to folders and files
                WebElement oldFirst = null;
                do {
                    List<WebElement> temp = driver.findElements(By.xpath(mainDivXpath));
                    int deleted = 0;
                    int change = 0;
                    for (int i = 1; i < temp.size() + 1 - deleted; i++) {
                        do {
                            change = deleted;
                            if (oldFirst == null || !oldFirst
                                    .equals(driver.findElement(By.xpath(elementImgXPt1 + i + elementImgXPt2)))) {
                                webWait.until(ExpectedConditions.or(
                                        ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath(elementImgXPt1 + i + elementImgXPt2)),
                                        ExpectedConditions.visibilityOfElementLocated(By.xpath(
                                                elementImgXPt1 + i + "]/div/div/div/div[1]/app-thumbnail/img[2]"))));
                                if (driver.findElement(By.xpath(elementImgXPt1 + i + elementImgXPt2))
                                        .getAttribute("src").equals(folderImg)) {
                                    // If folder check if its in the path
                                    String newPath = parentPath + "\\"
                                            + driver.findElement(By.xpath(nameXPt1 + i + nameXPt2)).getText();
                                    for (String x : paths) {
                                        if (x.contains(newPath) && !x.equals(newPath)) {
                                            if (!nextFolders.containsValue(
                                                    driver.findElement(By.xpath(nameXPt1 + i + "]/div/div"))
                                                            .getAttribute("item-id"))) {
                                                nextFolders.put(newPath,
                                                        driver.findElement(By.xpath(nameXPt1 + i + "]/div/div"))
                                                                .getAttribute("item-id"));
                                            }
                                        } else if (x.equals(newPath)) {
                                            // Delete Folder
                                            driver.findElement(By.xpath(optionsXpt1 + i + optionsXpt2)).click();
                                            webWait.until(ExpectedConditions.visibilityOfElementLocated(
                                                    By.xpath("//*[@id=\"context-menu\"]/app-context-menu-item[6]")));
                                            driver.findElement(
                                                    By.xpath("//*[@id=\"context-menu\"]/app-context-menu-item[6]"))
                                                    .click();
                                            deleted += 1;
                                        }
                                    }
                                } else if (parseFiles) {
                                    // If file and we're looking to delete files in this directory
                                    for (String x : paths) {
                                        if (x.equals(parentPath + "\\"
                                                + driver.findElement(By.xpath(nameXPt1 + i + nameXPt2)).getText())) {
                                            // Delete current file
                                            driver.findElement(By.xpath(optionsXpt1 + i + optionsXpt2)).click();
                                            webWait.until(ExpectedConditions.visibilityOfElementLocated(
                                                    By.xpath("//*[@id=\"context-menu\"]/app-context-menu-item[7]")));
                                            driver.findElement(
                                                    By.xpath("//*[@id=\"context-menu\"]/app-context-menu-item[7]"))
                                                    .click();
                                            deleted += 1;
                                        }
                                    }
                                }
                            } else {
                                break;
                            }
                        } while (change != deleted);
                    }

                    // Try scrolling up to see if ther are more elements, and if their are repeat
                    // and continue scrolling
                    oldFirst = driver.findElement(By.xpath(elementImgXPt1 + 1 + elementImgXPt2));
                    js.executeScript("window.scrollBy(0,-745)");
                    if (oldFirst.equals(driver.findElement(By.xpath(elementImgXPt1 + 1 + elementImgXPt2)))) {
                        oldFirst = null;
                    }
                } while (oldFirst != null);
            }
            // Setting parentPath, navigating page, and removing from map
            Iterator<Map.Entry<String, String>> to = nextFolders.entrySet().iterator();
            if (to.hasNext()) {
                repeat = true;
                nextFolder = to.next();
                driver.navigate().to("https://app.degoo.com/files/" + nextFolder.getValue());
                parentPath = nextFolder.getKey();
                to.remove();
            } else {
                repeat = false;
            }

        } while (repeat);
    }

    public void createElements(Set<String> paths, Function<String, ?> toDo) {
        String parentPath = "";
        Map<String, String> nextFolders = new HashMap<String, String>();
        Map.Entry<String, String> nextFolder;
        Boolean repeat = false;
        do {
            // Ensure page is loaded
            webWait.until(ExpectedConditions.or(ExpectedConditions.visibilityOfElementLocated(By.xpath(hasDataDivX)),
                    ExpectedConditions.visibilityOfElementLocated(By.xpath(noDataDivX))));
            // Test if there are files in this location
            if (driver.findElements(By.xpath(hasDataDivX)).size() != 0) {
                // Check if this directory needs a folder
                for (String x : paths) {
                    if (parentPath.equals(x.replaceFirst("[\\\\][^\\\\]+$", ""))) {
                        // Execute passed function when in correct directory
                        toDo.apply(x);
                    }
                }

                // Check if theres anymore files on page by scrolling the bottom of the page
                js.executeScript("window.scrollTo(0,document.body.scrollHeight)");
                js.executeScript("window.scrollTo(0,document.body.scrollHeight)");
                // Count how many files and sort to folders and files
                WebElement oldFirst = null;
                do {
                    List<WebElement> temp = driver.findElements(By.xpath(mainDivXpath));
                    for (int i = 1; i < temp.size() + 1; i++) {
                        if (oldFirst == null || !oldFirst
                                .equals(driver.findElement(By.xpath(elementImgXPt1 + i + elementImgXPt2)))) {
                            webWait.until(ExpectedConditions.or(
                                    ExpectedConditions
                                            .visibilityOfElementLocated(By.xpath(elementImgXPt1 + i + elementImgXPt2)),
                                    ExpectedConditions.visibilityOfElementLocated(By
                                            .xpath(elementImgXPt1 + i + "]/div/div/div/div[1]/app-thumbnail/img[2]"))));
                            if (driver.findElement(By.xpath(elementImgXPt1 + i + elementImgXPt2)).getAttribute("src")
                                    .equals(folderImg)) {
                                // If folder check if its in the path
                                String newPath = parentPath + "\\"
                                        + driver.findElement(By.xpath(nameXPt1 + i + nameXPt2)).getText();
                                for (String x : paths) {
                                    if (x.contains(newPath)) {
                                        String next = driver.findElement(By.xpath(nameXPt1 + i + "]/div/div"))
                                                .getAttribute("item-id");
                                        if (!nextFolders.containsValue(next)) {
                                            nextFolders.put(newPath, next);
                                        }
                                    }
                                }
                            }
                        } else {
                            break;
                        }
                    }

                    // Try scrolling up to see if ther are more elements, and if their are repeat
                    // and continue scrolling
                    oldFirst = driver.findElement(By.xpath(elementImgXPt1 + 1 + elementImgXPt2));
                    js.executeScript("window.scrollBy(0,-745)");
                    if (oldFirst.equals(driver.findElement(By.xpath(elementImgXPt1 + 1 + elementImgXPt2)))) {
                        oldFirst = null;
                    }
                } while (oldFirst != null);
            }
            // Setting parentPath, navigating page, and removing from map
            Iterator<Map.Entry<String, String>> to = nextFolders.entrySet().iterator();
            if (to.hasNext()) {
                repeat = true;
                nextFolder = to.next();
                driver.navigate().to("https://app.degoo.com/files/" + nextFolder.getValue());
                parentPath = nextFolder.getKey();
                to.remove();
            } else {
                repeat = false;
            }

        } while (repeat);
    }

    public void deleteFolders(Set<String> paths) {
        this.deleteElements(paths, false);
    }

    public void deleteFiles(Set<String> paths) {
        this.deleteElements(paths, true);
    }

    public void createFolders(Set<String> paths) {
        createElements(paths, p -> {// Find right directory
            // Create Folder in current directory
            webWait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"create-folder-btn\"]")));
            driver.findElement(By.xpath("//*[@id=\"create-folder-btn\"]")).click();
            driver.findElement(By.xpath("//*[@id=\"input-field\"]"))
                    .sendKeys(p.replaceFirst("[\\\\].+[\\\\]", "").replaceFirst("[.][^.]+$", ""));
            driver.findElement(By.xpath("/html/body/app-dialog/div/div/app-input-dialog/div/button[2]")).click();
            webWait.until(ExpectedConditions.or(ExpectedConditions.visibilityOfElementLocated(By.xpath(hasDataDivX)),
                    ExpectedConditions.visibilityOfElementLocated(By.xpath(noDataDivX))));
            return "";
        });
    }

    public void uploadFiles(Set<String> paths) {
        createElements(paths, p -> {// Find right directory
            // Upload file in current directory
            webWait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"upload-btn\"]")));
            driver.findElement(By.xpath("//*[@id=\"upload-btn\"]")).click();
            webWait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div/div[2]/div/div[2]/div[2]/div/button")));
            driver.findElement(By.xpath("/html/body/div/div[2]/div/div[2]/div[2]/div/button")).click();
            driver.switchTo().activeElement();
            webWait.until(ExpectedConditions.visibilityOfElementLocated(By.name("FileInput")));
            driver.findElement(By.name("FileInput")).sendKeys(p);
            
        return"";
        });
    }

    public void run() {
        try {

            localFileStorage = localFileFut.get();
            localFolderStorage = localFolderFut.get();
            cloudFileStorage = cloudFileFut.get();
            cloudFolderStorage = cloudFolderFut.get();

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


            //Whats in the cloud thats not local
                //Deleting folders and their files
                
                for (String a :cloudFolderStorage) {
                    if(!localFolderStorage.contains(a)){
                        //Confirm files related to this folder are deleted
                        for(Iterator<String> x = cloudFileStorage.keySet().iterator(); x.hasNext();){
                            String y = x.next();
                            if(y.contains(a.toString())){x.remove();}
                        }
                        tempStorage.add(a);
                    }
                }
                if(tempStorage.size()>0){
                    deleteFolders(tempStorage);
                    tempStorage.clear();
                }

                //Deleting lone files
                for (Entry<String, String> a : cloudFileStorage.entrySet()) {
                    if(localFileStorage.get(a.getKey())==null||!a.getValue().equals(localFileStorage.get(a.getKey()))){
                        tempStorage.add(a.getKey());
                    }
                }
                if(tempStorage.size()>0){
                    driver.navigate().to(startPage);
                    deleteFiles(tempStorage);
                    tempStorage.clear();
                }

            //Whats local thats not in the cloud

                //Folders that need to be created
                for (String a : localFolderStorage){
                    if(!cloudFolderStorage.contains(a)){
                        tempStorage.add(a);
                    }
                }
                if(tempStorage.size()>0){
                    driver.navigate().to(startPage);
                    createFolders(tempStorage);
                    tempStorage.clear();
                }

                //Files that need to be uploaded
                for (Entry<String, String> a : localFileStorage.entrySet()) {
                    if(cloudFileStorage.get(a.getKey())==null||!a.getValue().equals(cloudFileStorage.get(a.getKey()))){
                        tempStorage.add(a.getKey());
                    }
                }
                if(tempStorage.size()>0){
                    driver.navigate().to(startPage);
                    //uploadFiles(tempStorage);
                    System.out.println(tempStorage);
                }
                driver.quit();
            transTest.complete(true);

        }catch (InterruptedException e) {
            System.out.println("Error retriving Future: " + e.toString());
        }catch (ExecutionException e) {
            System.out.println("Error retriving Future: " + e.toString());
        }
    }
    
}