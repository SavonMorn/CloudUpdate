import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import java.io.File;
import java.util.Scanner;
import java.util.Set;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Thread;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
 /* Window contructor */
public class mWindow extends JFrame {
   //Window elements
    private static final long serialVersionUID = 1L;
    private JLabel title = new JLabel("CLOUD UPDATE");
    private JLabel UN = new JLabel("Username:");
    private JLabel PW = new JLabel("Password:");
    private JLabel Dir = new JLabel("Monitored Directories:");
    private JLabel Date = new JLabel("Check for changes:");
    private JLabel Url = new JLabel("Cloud directory url:");
    private JTextField UrlField = new JTextField(20);
    private JTextField UNField = new JTextField(20);
    private JPasswordField PWField = new JPasswordField(20);
    private JTextField dir[] = { new JTextField(25), new JTextField(25), new JTextField(25), new JTextField(25),
            new JTextField(25), new JTextField(25), new JTextField(25), new JTextField(25) };
    private JButton save = new JButton("Save changes");
    private JButton start = new JButton("Start upload");
    private Integer x[] = { 1, 2, 3, 4, 5, 6, 7, 8 };
    private JComboBox<Integer> DirNum = new JComboBox<Integer>(x);
    private String y[] = { "Every Day", "Every Other Day", "Once a Week", "Once a Month" };
    private JComboBox<String> DateButt = new JComboBox<String>(y);
    //Variables
    private int dirSelection;
    private int dateSelection;
    private String[] loginInfo = { "", "" };
    private String[] currentDirs = { "", "", "", "", "", "", "", "", "" };
    private String startPage;
    private File saveFile;
    private Scanner fileParse;
    private mWindow This = this;
    private GridBagConstraints gbc = new GridBagConstraints();

    public mWindow(File SF) {   //Constructor
        super(" CloudUpdate");
        saveFile = SF;
        Populate();
        setLayout(new GridBagLayout());
        evtHandler evt = new evtHandler();
        gbc.gridx = 0;
        gbc.gridy = 0;
        title.setFont(new Font("Serif", Font.BOLD, 14));
        add(title, gbc);
        gbc.gridy = 1;
        add(UN, gbc);
        gbc.gridx = 1;
        add(UNField, gbc);
        gbc.gridy = 2;
        add(PWField, gbc);
        gbc.gridx = 0;
        add(PW, gbc);
        gbc.weighty = 0.3;
        gbc.gridy = 3;
        add(Dir, gbc);
        gbc.gridx = 1;
        add(DirNum, gbc);
        SetDirectoryBox();
        gbc.weighty = 0.2;
        gbc.gridy = 12;
        gbc.gridx = 0;
        add(Url, gbc);
        gbc.gridx = 1;
        add(UrlField, gbc);
        gbc.gridy = 13;
        gbc.gridx = 0;
        add(Date, gbc);
        gbc.gridx = 1;
        add(DateButt, gbc);
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 14;
        add(save, gbc);
        gbc.gridx = 1;
        add(start, gbc);

        UNField.setText(loginInfo[0]);
        PWField.setText(loginInfo[1]);
        UrlField.setText(startPage);
        DirNum.setSelectedIndex(dirSelection);
        DateButt.setSelectedIndex(dateSelection);
        DirNum.addActionListener(evt);
        DateButt.addActionListener(evt);
        save.addActionListener(evt);
        start.addActionListener(evt);
    }

    private void Populate()  { //Fill all GUI boxes with saved data if any
        try{
        fileParse = new Scanner(saveFile);
        dateSelection = fileParse.nextInt();
        if (dateSelection != -1) {
            dirSelection = fileParse.nextInt(); fileParse.nextLine();
            loginInfo[0] = fileParse.nextLine();
            loginInfo[1] = fileParse.nextLine();
            startPage = fileParse.nextLine();
            for (int i = 0; i <= dirSelection; i++)
                currentDirs[i] = fileParse.nextLine();
        } else {
            dateSelection = 0;
            dirSelection = 0;
        }
        fileParse.close();
        }catch(IOException e){
            System.out.println("Error populating mWindow data from file, \n delete Settings file in the users \\Appdata");
        }
    }

    private void SetDirectoryBox() { //Add or remove directory boxes from the GUI depending on the users selection
        gbc.weighty = 0.1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        for (int i = 0; i <= dirSelection; i++) {

            gbc.gridy = i + 4;
            add(dir[i], gbc);
            dir[i].setText(currentDirs[i]);
        }
        for (int i = dirSelection + 1; i < 8; i++) {
            remove(dir[i]);
            dir[i].setText(currentDirs[i]);
        }
        setSize(370, 250 + (25 * dirSelection));
        revalidate();
        repaint();
        gbc.gridwidth = 1;
    }

    private Boolean setInfoForm() throws IOException { //Save current data to .txt file
        if (dir[dirSelection].getText().contains("\\") && UNField.getText().length() > 4
                && PWField.getPassword().toString().length() > 4
                && UrlField.getText().length()>30) {
            FileWriter toFile = new FileWriter(saveFile, false);
            toFile.write(dateSelection + System.lineSeparator());
            toFile.write(dirSelection + System.lineSeparator());
            loginInfo[0] = UNField.getText();
            loginInfo[1] = PWField.getText();
            toFile.write(loginInfo[0] + System.lineSeparator());
            toFile.write(loginInfo[1] + System.lineSeparator());
            startPage = UrlField.getText();
            toFile.write(startPage + System.lineSeparator());
            for (int i = 0; i <= dirSelection; i++) {
                currentDirs[i] =dir[i].getText();

                toFile.write(currentDirs[i] + System.lineSeparator());
            }
            toFile.close();
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "Please ensure all boxes are filled correctly.");
            return false;
        }

    }

    private void startWait(){ //Launge the Progress window while file checks begin in a new thread
        
        CompletableFuture<Map<String, String>> localFileFut = new CompletableFuture<Map<String, String>>();
        CompletableFuture<Map<String,String>> cloudFileFut = new CompletableFuture<Map<String,String>>();
        CompletableFuture<Set<String>> localFolderFut = new CompletableFuture<Set<String>>();
        CompletableFuture<Set<String>> cloudFolderFut = new CompletableFuture<Set<String>>();
        Thread localWorker = new Thread(new localFileCheck(dirSelection, currentDirs, localFileFut, localFolderFut),"local");
        webFileCheck WFC = new webFileCheck(startPage, loginInfo, cloudFileFut, cloudFolderFut);
        Thread cloudWorker = new Thread(WFC ,"cloud");
        cloudWorker.start();
        localWorker.start();
        JLabel message = new JLabel("Retriving Files");
        Thread Ticker = new Thread(new progTicker(message, WFC, localFileFut, localFolderFut, cloudFolderFut),"ticker");
        Ticker.start();
        JOptionPane.showMessageDialog(null, message);

    }

    private class evtHandler implements ActionListener {
        // window event handler
        public void actionPerformed(ActionEvent evt) {
            try {
                if (evt.getSource() == DirNum) {
                    dirSelection = DirNum.getSelectedIndex();
                    This.SetDirectoryBox();
                } else if (evt.getSource() == DateButt) {
                    dateSelection = DateButt.getSelectedIndex();
                } else if (evt.getSource() == save) {
                    if (setInfoForm()) {
                        JOptionPane.showMessageDialog(null, "Changes Saved.");
                    }
                } else if (evt.getSource() == start) {
                    if (setInfoForm()) {
                       startWait();
                    }
                }
            } catch (IOException e) {
                System.out.println("Error Writing saves to option file: " + e.toString());
            } 
        }
    }
}