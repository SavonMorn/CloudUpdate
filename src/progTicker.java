import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.swing.JLabel;

public class progTicker implements Runnable {
    private JLabel message;
    private webFileCheck WFC;
    private CompletableFuture<Map<String, String>> localFut;
    private CompletableFuture<Set<String>> localFut2;
    private CompletableFuture<Set<String>> cloudFut2;
    private CompletableFuture<Boolean> transTest = new CompletableFuture<Boolean>();

    public progTicker( JLabel label, webFileCheck WFC,
    CompletableFuture<Map<String, String>> localFut, 
    CompletableFuture<Set<String>> localFolderFut, 
    CompletableFuture<Set<String>> cloudFolderFut){
        this.message = label;
        this.localFut = localFut;
        this.localFut2 = localFolderFut;
        this.cloudFut2 = cloudFolderFut;
        this.WFC = WFC;
    }
    public void run(){
        try{
            while (!localFut2.isDone()||!cloudFut2.isDone()) {
                Thread.sleep(500);
                if(message.getText().length()<22){
                    message.setText(message.getText() + ".");
                }else{
                    message.setText("Retriving Files");
                }
            }
            Thread Transfer = new Thread(new fileTransfer(transTest, WFC, localFut, localFut2));
            message.setText("Checking for Changes");
            Transfer.start();
            while (!transTest.isDone()){
                Thread.sleep(500);
                if(message.getText().length()<26){
                    message.setText(message.getText() + ".");
                }else{
                    message.setText("Checking for Changes");
                }
            }
            if(transTest.get()==true){
                message.setText("Done");
            }else{
                message.setText("Error Commiting Changes\n Please Restart Application and Check Connection ");
            }
            

        }catch (InterruptedException e) {
            System.out.println("Thread error: " + e.toString());
        } catch (ExecutionException e) {
            System.out.println("Error reading Future: " + e.toString());
        }
    }
}