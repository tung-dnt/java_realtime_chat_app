import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {
    private ServerSocket serverSocket;

    static final String[] clientNameList = {"Tung", "Thanh", "Dong", "Khanh", "Chien"};
    static final ArrayList<ServerThread> activeClients = new ArrayList<>();

    public Server() throws IOException {
        try {
            // Socket dùng để xử lý các yêu cầu đăng nhập/đăng ký từ user
            serverSocket = new ServerSocket(9999);
            System.out.println("Server is ready to access now !");
            while (true) {
                // Đợi request đăng nhập/đăng xuất từ client
                Socket socket = serverSocket.accept();

                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                String username = dis.readUTF();

                System.out.println(username + " is connected !");
                // Kiểm tra tên đăng nhập có tồn tại hay không
                if (isExisted(username)) {
                    // Thêm người dùng vừa truy cập vào danh sách active users
                    activeClients.add(new ServerThread(username));

                    for (ServerThread client : activeClients) {
                        if (client.getUsername().equals(username)) {
                            // Tạo Handler mới để giải quyết các request từ user này
                            client.setSocket(socket);
                            // Tạo một Thread để giao tiếp với user này
                            Thread t = new Thread(client);
                            t.start();
                            break;
                        }
                    }
                    sendActiveClients();
                } else {
                    dos.writeUTF("This username is not exist");
                    dos.flush();
                }
            }
        } catch (Exception ex) {
            System.err.println(ex);
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }

    /**
     * Kiểm tra username đã tồn tại hay chưa
     */
    public boolean isExisted(String name) {
        for (String client : clientNameList) {
            if (client.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private void sendActiveClients() {
        String message = "";
        for (ServerThread client : activeClients) {
            message += ",";
            message += client.getUsername();
        }
        // Làm mới danh sách bạn bè online cho tất cả user mỗi khi có người login
        for (ServerThread client : activeClients) {
            try {
                client.getDos().writeUTF("Online users");
                client.getDos().writeUTF(message);
                client.getDos().flush();
            } catch (IOException e) {
                e.printStackTrace();
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
                // Đọc yêu cầu từ user
                String message = dis.readUTF();

                // Yêu cầu gửi tin nhắn dạng văn bản
                if (message.equals("Text")) {
                    String receiver = dis.readUTF();
                    String content = dis.readUTF();

                    for (ServerThread client : Server.activeClients) {
                        if (client.getUsername().equals(receiver)) {
                            synchronized (new Object()) {
                                client.getDos().writeUTF("Text");
                                client.getDos().writeUTF(this.username);
                                client.getDos().writeUTF(content);
                                client.getDos().flush();
                                break;
                            }
                        }
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
