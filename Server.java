import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {
    private ServerSocket serverSocket;
    static final ArrayList<ServerThread> activeClients = new ArrayList<>();

    public Server() throws IOException {
        try {
            // Socket dùng để xử lý các yêu cầu đăng nhập/đăng ký từ user
            serverSocket = new ServerSocket(Const.PORT);
            System.out.println("Server is ready to access now !");
            while (true) {
                // Đợi request đăng nhập/đăng xuất từ client
                Socket socket = serverSocket.accept();

                DataInputStream dis = new DataInputStream(socket.getInputStream());

                String username = dis.readUTF();

                System.out.println(username + " is connected !");

                ServerThread client = new ServerThread(username);

                // Thêm người dùng vừa truy cập vào danh sách active users
                activeClients.add(client);

                // Tạo Handler mới để giải quyết các request từ user này
                client.setSocket(socket);
                // Tạo một Thread để giao tiếp với user này
                Thread t = new Thread(client);
                t.start();
            }
        } catch (Exception ex) {
            System.err.println(ex);
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }
}

/**
 * Luồng riêng dùng để giao tiếp với mỗi user
 */
class ServerThread implements Runnable {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private final String username;

    public ServerThread(String username) {
        this.username = username;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Yêu cầu gửi tin nhắn dạng văn bản
                String content = dis.readUTF();

                for (ServerThread client : Server.activeClients) {
                    synchronized (new Object()) {
                        client.getDos().writeUTF(this.username);
                        client.getDos().writeUTF(content);
                        client.getDos().flush();
                    }
                }
            } catch (IOException e) {
                closeSocket();
                System.out.println(this.username + " is disconnected !");
                break;
            }
        }
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
        try {
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSocket() {
        if (socket != null) {
            try {
                Server.activeClients.removeIf(client -> this.username.equals(client.getUsername()));
                socket.close();
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getUsername() {
        return this.username;
    }

    public DataOutputStream getDos() {
        return this.dos;
    }
}
