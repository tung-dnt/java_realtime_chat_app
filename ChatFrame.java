import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;

public class ChatFrame extends JFrame {
    private JButton btnSend;
    private JScrollPane chatPanel;
    private JLabel lbReceiver = new JLabel(" ");
    private JPanel contentPane;
    private JTextField txtMessage;
    private JTextPane chatWindow;
    JComboBox<String> onlineUsers = new JComboBox<String>();

    private final String username;
    private final DataInputStream dis;
    private final DataOutputStream dos;

    private final HashMap<String, JTextPane> chatWindows = new HashMap<String, JTextPane>();

    Thread receiver;

    private void autoScroll() {
        chatPanel.getVerticalScrollBar().setValue(chatPanel.getVerticalScrollBar().getMaximum());
    }

    /**
     * Render 1 đoạn chat trong app.
     */
    private void newMessage(String username, String message, Boolean yourMessage) {
        StyledDocument doc;
        if (username.equals(this.username)) {
            doc = chatWindows.get(lbReceiver.getText()).getStyledDocument();
        } else {
            doc = chatWindows.get(username).getStyledDocument();
        }

        Style userStyle = doc.getStyle("User style");
        if (userStyle == null) {
            userStyle = doc.addStyle("User style", null);
            StyleConstants.setBold(userStyle, true);
        }

        if (yourMessage) {
            StyleConstants.setForeground(userStyle, Color.red);
        } else {
            StyleConstants.setForeground(userStyle, Color.BLUE);
        }

        // In ra tên người gửi
        try {
            String name = yourMessage ? "You" : username;
            doc.insertString(doc.getLength(), name + ": ", userStyle);
        } catch (BadLocationException ignored) {
        }

        Style messageStyle = doc.getStyle("Message style");
        if (messageStyle == null) {
            messageStyle = doc.addStyle("Message style", null);
            StyleConstants.setForeground(messageStyle, Color.BLACK);
            StyleConstants.setBold(messageStyle, false);
        }

        // In ra nội dung tin nhắn
        try {
            doc.insertString(doc.getLength(), message + "\n", messageStyle);
        } catch (BadLocationException ignored) {
        }

        autoScroll();
    }

    private void setActiveClients(String activeClients) {
        String[] users = activeClients.split(",");
        onlineUsers.removeAllItems();

        for (String user : users) {
            if (!user.equals(username)) {
                // Cập nhật danh sách các người dùng trực tuyến vào ComboBox onlineUsers (trừ bản thân)
                onlineUsers.addItem(user);
                if (chatWindows.get(user) == null) {
                    JTextPane temp = new JTextPane();
                    temp.setFont(new Font("Arial", Font.PLAIN, 14));
                    temp.setEditable(false);
                    chatWindows.put(user, temp);
                }
            }
        }
        onlineUsers.validate();
    }

    private void sendMessage(){
        try {
            dos.writeUTF("Text");
            dos.writeUTF(lbReceiver.getText());
            dos.writeUTF(txtMessage.getText());
            dos.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
            newMessage("ERROR", "Network error!", true);
        }
    }

    public ChatFrame(String username, Socket clientSocket) throws IOException {
        setTitle("NPR CHAT");
        this.username = username;
        this.dis = new DataInputStream(clientSocket.getInputStream());
        ;
        this.dos = new DataOutputStream(clientSocket.getOutputStream());

        this.dos.writeUTF(username);

        receiver = new Thread(new Receiver(dis));
        receiver.start();

        setDefaultLookAndFeelDecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 586, 450);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setBackground(new Color(230, 240, 247));
        setContentPane(contentPane);

        JPanel header = new JPanel();
        header.setBackground(new Color(160, 190, 223));

        txtMessage = new JTextField();
        txtMessage.setEnabled(false);
        txtMessage.setColumns(10);

        btnSend = new JButton("Send");
        btnSend.setEnabled(false);

        chatPanel = new JScrollPane();
        chatPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(230, 240, 247));

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(
                gl_contentPane.createParallelGroup(Alignment.LEADING)
                        .addComponent(header, GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                        .addGroup(gl_contentPane.createSequentialGroup()
                                .addComponent(leftPanel, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addComponent(txtMessage, GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(btnSend, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
                                        .addComponent(chatPanel, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)))
        );
        gl_contentPane.setVerticalGroup(
                gl_contentPane.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_contentPane.createSequentialGroup()
                                .addComponent(header, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addComponent(chatPanel, GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
                                                        .addComponent(btnSend, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtMessage, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(leftPanel, GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)))
        );

        JPanel panel = new JPanel();
        panel.setBackground(new Color(230, 240, 247));
        JLabel lblNewLabel_1 = new JLabel("CHAT WITH");
        lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
        GroupLayout gl_leftPanel = new GroupLayout(leftPanel);
        gl_leftPanel.setHorizontalGroup(
                gl_leftPanel.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_leftPanel.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(panel, GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                                .addContainerGap())
                        .addGroup(gl_leftPanel.createSequentialGroup()
                                .addGap(28)
                                .addComponent(lblNewLabel_1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(29))
                        .addGroup(Alignment.TRAILING, gl_leftPanel.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(onlineUsers, 0, 101, Short.MAX_VALUE)
                                .addContainerGap())
        );
        onlineUsers.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                lbReceiver.setText((String) onlineUsers.getSelectedItem());
                if (chatWindow != chatWindows.get(lbReceiver.getText())) {
                    txtMessage.setText("");
                    chatWindow = chatWindows.get(lbReceiver.getText());
                    chatPanel.setViewportView(chatWindow);
                    chatPanel.validate();
                }

                if (lbReceiver.getText().isBlank()) {
                    btnSend.setEnabled(false);
                    txtMessage.setEnabled(false);
                } else {
                    btnSend.setEnabled(true);
                    txtMessage.setEnabled(true);
                }
            }

        });

        gl_leftPanel.setVerticalGroup(
                gl_leftPanel.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_leftPanel.createSequentialGroup()
                                .addGap(5)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(panel, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
                                .addGap(41)
                                .addComponent(lblNewLabel_1)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(onlineUsers, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(104, Short.MAX_VALUE))
        );

        JLabel lbUsername = new JLabel(this.username);
        lbUsername.setFont(new Font("Arial", Font.BOLD, 15));
        panel.add(lbUsername);
        leftPanel.setLayout(gl_leftPanel);

        JLabel headerContent = new JLabel("NPR CHAT");
        headerContent.setFont(new Font("Poor Richard", Font.BOLD, 24));
        header.add(headerContent);

        JPanel usernamePanel = new JPanel();
        usernamePanel.setBackground(new Color(230, 240, 247));
        chatPanel.setColumnHeaderView(usernamePanel);

        lbReceiver.setFont(new Font("Arial", Font.BOLD, 16));
        usernamePanel.add(lbReceiver);

        chatWindows.put(" ", new JTextPane());
        chatWindow = chatWindows.get(" ");
        chatWindow.setFont(new Font("Arial", Font.PLAIN, 14));
        chatWindow.setEditable(false);

        chatPanel.setViewportView(chatWindow);
        contentPane.setLayout(gl_contentPane);

        txtMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                btnSend.setEnabled(!txtMessage.getText().isBlank() && !lbReceiver.getText().isBlank());
            }
        });

        btnSend.addActionListener(e -> {
            sendMessage();
            // In ra tin nhắn lên màn hình chat với người nhận
            newMessage(username, txtMessage.getText(), true);
            txtMessage.setText("");
        });

        this.getRootPane().setDefaultButton(btnSend);

        setVisible(true);
    }

    /**
     * Luồng nhận tin nhắn từ server của mỗi client
     */
    class Receiver implements Runnable {
        private final DataInputStream dis;
        public Receiver(DataInputStream dis) {
            this.dis = dis;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    // Chờ tin nhắn từ server
                    String method = dis.readUTF();

                    if (method.equals("Text")) {
                        // Nhận một tin nhắn văn bản
                        String sender = dis.readUTF();
                        String message = dis.readUTF();

                        // In tin nhắn lên màn hình chat với người gửi
                        newMessage(sender, message, false);
                    } else if (method.equals("Online users")) {
                        // Nhận yêu cầu cập nhật danh sách người dùng trực tuyến
                        String dirtyActiveUsers = dis.readUTF();
                        setActiveClients(dirtyActiveUsers);
                    }
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