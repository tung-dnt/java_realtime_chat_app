import java.net.Socket;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter username");
        String username = sc.nextLine();

        Socket clientSocket = new Socket("localhost", 9999);

        new ChatFrame(username, clientSocket);
    }
}
