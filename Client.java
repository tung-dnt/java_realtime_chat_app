import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client {
    private final DataInputStream dis;
    private final DataOutputStream dos;

    public Client(String username, Socket clientSocket) throws IOException {
        this.dis = new DataInputStream(clientSocket.getInputStream());
        ;
        this.dos = new DataOutputStream(clientSocket.getOutputStream());

        this.dos.writeUTF(username);

        Thread receiver = new Thread(new Receiver(dis));
        receiver.start();
        Thread sender = new Thread(new Sender());
        sender.start();
    }

    class Sender implements Runnable {
        @Override
        public void run() {
            while (true) {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                try {
                    String message = br.readLine();
                    dos.writeUTF(message.trim());
                } catch (IOException e1) {
                    e1.printStackTrace();
                    System.out.println("Sending message unsuccessfully !");
                }
            }
        }
    }


    class Receiver implements Runnable {
        private final DataInputStream dis;
        public Receiver(DataInputStream dis) {
            this.dis = dis;
        }

        @Override
        public void run() {
            try {
                String notification = dis.readUTF();
                System.out.println(notification);
                while (true) {
                    String sender = dis.readUTF();
                    String message = dis.readUTF();
                    System.out.println(sender + ": " + message.trim());
                }
            } catch (IOException ex) {
                System.err.println(ex);
            } finally {
                try {
                    if (dis != null) {
                        dis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
