import javax.swing.JFrame;
import java.io.File;
import java.io.FileWriter;
public class Main {
    public static void main(String[] args) {
        try{     
        //Confirming the existence of a save data file, or creating one and passing it the main window when launched.
        File saveFile = new File(System.getProperty("user.home") + "\\AppData\\CloudUpdate_settings.txt");
        if(saveFile.createNewFile()){
            FileWriter writeFile = new FileWriter(saveFile);
            writeFile.write("-1");
            writeFile.close();
        }
        mWindow mainWin = new mWindow(saveFile);
        mainWin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWin.setVisible(true);
        }catch(Exception e){System.out.println("Error on Main: " + e.toString());}

    }
}
