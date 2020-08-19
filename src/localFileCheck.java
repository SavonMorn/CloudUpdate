
import java.nio.file.Path;
import java.nio.file.Paths;
//import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.util.stream.Stream;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;


public class localFileCheck implements Runnable{
    private String Dir[] = {"", "", "", "", "", "", "", "", "" };
    private String Months[] = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
    private Map<String, String> fileStorage = new HashMap<String, String>();
    private Set<String> folderStorage = new HashSet<String>();
    private CompletableFuture<Map<String,String>> futFile;
    private CompletableFuture<Set<String>> futFolder;

    public localFileCheck(int dirNum, String dir[],
     CompletableFuture<Map<String,String>> fileFut, 
     CompletableFuture<Set<String>> folderFut){
         //Take an array of monitored directories
         futFile = fileFut;
         futFolder = folderFut;
        for(int i = 0; i <= dirNum; i++)
            this.Dir[i] = dir[i];
    }/*
    public localFileCheck(CompletableFuture<Map<String,String>> fut){ //Find monitored directories though files
        this.fut = fut;
        Scanner settIn = new Scanner(System.getProperty("user.home") + "\\AppData\\CloudUpdate_settings.txt");
        int test = settIn.nextInt();
        if (test != -1) {
            int dirNum = settIn.nextInt();
            settIn.next();
            settIn.next();
            for (int i = 0; i <= dirNum; i++)
                Dir[i] = settIn.next();
        }
        settIn.close();
    }*/

    private String formatDate(String x) {
        // Remove the time
        String y[] = x.replaceFirst("[T][^-]+[-][^-]+$", "").split("-");
        // Convert the month number to name
        String month = Months[Integer.parseInt(y[1]) - 1];
        // Add all of the above, casting the day to a proper int
        String z = month + " " + (Integer.parseInt(y[2])) + " " + y[0];
        return z;
    }


    public void run() {
        for(String path : Dir){ //iterate through monitored directories 
            if(path.contains("\\")){//Ensure the string is a path
                    try(Stream<Path> x = Files.walk(Paths.get(path))){//iterate through files 
                        x.forEach((currFile)->{
                            String fileName = "\\"+path.replaceFirst("^[A-Z]:.+[\\\\]", "");
                            try{
                                if(Files.isDirectory(currFile)){
                                    folderStorage.add(currFile.toString().replace(path,fileName));
                                }else{
                                    BasicFileAttributes attr = Files.readAttributes(currFile, BasicFileAttributes.class);
                                    fileStorage.put(currFile.toString().replace(path, fileName),
                                        formatDate(attr.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault()).toString())
                                    ); 
                                }
                            }catch(Exception e){System.out.println("Error retriving file attributes.");}
                        });
                    }catch(Exception e){ 
                        System.out.println("Error streaming local files, Ensure all Directories exist.");
                        
                    }
            }
        }
        futFile.complete(fileStorage);
        futFolder.complete(folderStorage);
    } 
    public static void main(String[] args){
        String path = "\\Main\\AppData\\CloudUpdate_settings.txt";
        System.out.println(path);
        System.out.println(path.replaceFirst("[\\\\].+[\\\\]", "").replaceFirst("[.][^.]+$", ""));
   
    }
}