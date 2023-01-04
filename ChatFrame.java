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
    private JPanel contentPane;
    private JTextField txtMessage;
    private final JList messageList = new JList();
    private final String username;
    private final DataInputStream dis;
    private final DataOutputStream dos;

    Thread receiver;

    private void autoScroll() {
        chatPanel.getVerticalScrollBar().setValue(chatPanel.getVerticalScrollBar().getMaximum());
    }

    /**
     * Render 1 đoạn chat trong app.
     */
    private void newMessage(String username, String message, Boolean yourMessage) {
        String name = yourMessage ? "You" : username;

        JLabel label = new JLabel(name + ": " + message.trim());
        label.setFont(new Font("Serif", Font.BOLD, 12));

        if(yourMessage) label.setForeground(Color.BLUE);
        else label.setForeground(Color.RED);

        messageList.add(label);
        autoScroll();
    }

    private void sendMessage() {
        try {
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
        txtMessage.setColumns(10);

        btnSend = new JButton("Send");
        btnSend.setEnabled(false);

        messageList.setVisible(true);
        chatPanel = new JScrollPane(messageList);
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
        GroupLayout gl_leftPanel = new GroupLayout(leftPanel);
        gl_leftPanel.setHorizontalGroup(
                gl_leftPanel.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_leftPanel.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(panel, GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                                .addContainerGap()
                        ));

        gl_leftPanel.setVerticalGroup(
                gl_leftPanel.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_leftPanel.createSequentialGroup()
                                .addGap(5)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(panel, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
                        ));

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

        contentPane.setLayout(gl_contentPane);

        txtMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                btnSend.setEnabled(!txtMessage.getText().isBlank());
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
                    // Nhận một tin nhắn văn bản
                    String sender = dis.readUTF();
                    String message = dis.readUTF();

                    // In tin nhắn lên màn hình chat với người gửi
                    newMessage(sender, message, false);
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