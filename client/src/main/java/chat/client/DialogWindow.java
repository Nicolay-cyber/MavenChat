package chat.client;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DialogWindow extends JFrame
{
    private final JTextPane chatText = new JTextPane();
    private StyledDocument messageArea ;
    private final SimpleAttributeSet left = new SimpleAttributeSet();
    private final SimpleAttributeSet right = new SimpleAttributeSet();
    private final JTextArea inputMessage = new JTextArea("");
    private final JPanel inputPanel = new JPanel(new BorderLayout());
    private final JButton send = new JButton("Send");
    private final Action enter = new SendListener();
    private Socket socket;
    private DataOutputStream toClientHandler;
    private DataInputStream fromClientHandler;
    private String companion = "";
    private String nickname = "";
    private String login = "";
    private final Map<String, JButton> userList = new ConcurrentHashMap<>();

    public DialogWindow()
    {
        ServerConnection();
    }
    private void showDialogWindow() throws IOException
    {
        windowSetting();
        menuBarSetting();
        messageAreaSetting();
        inputPanelSetting();
        userListArea();
        setVisible(true);
    }
    private void menuBarSetting()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem setting = new JMenuItem("Setting");
        setting.addActionListener(e -> showUserSettingWindow());
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(1));
        file.add(setting);
        file.add(exit);
        menuBar.add(file);
        setJMenuBar(menuBar);
    }

    private void showUserSettingWindow() {
        JTextField newNicknameField = new JTextField(nickname);
        JTextField newLoginField = new JTextField(login);
        JTextField OldPasswordField = new JPasswordField();
        JTextField NewPasswordField = new JPasswordField();
        Object[] message =
            {
            "Nickname:", newNicknameField,
            "Login:", newLoginField,
            "Old password:", OldPasswordField,
            "New password:", NewPasswordField
            };
        int option = JOptionPane.showConfirmDialog(null, message, "User setting", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION)
        {
            try
            {
                if(!newNicknameField.getText().contains(" ") && !newLoginField.getText().contains(" ") &&
                        !newNicknameField.getText().equals("") && !newLoginField.getText().equals(""))
                {
                    toClientHandler.writeUTF("/changeNicknameAndLogin " + newNicknameField.getText() + " " + newLoginField.getText());
                }
                else
                if(!OldPasswordField.getText().contains(" ") && !NewPasswordField.getText().contains(" ") &&
                        !OldPasswordField.getText().equals("") && !NewPasswordField.getText().equals(""))
                {
                    toClientHandler.writeUTF("/changePassword " + OldPasswordField.getText() + " " + NewPasswordField.getText());
                }
                else
                {
                    showUserSettingWindow();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void listeningMsgFromClientHandler() throws IOException, BadLocationException {
        String msgFromServer = fromClientHandler.readUTF();
        String[] s = msgFromServer.split(" ",3);
        switch (s[0])
        {
            case "/reloadUserList":
            {
                (userList.get(s[1])). //Getting button with old user's nickname
                        setText(s[2]); //Setting new button's tittle, which equals new user's nickname
                break;
            }
            case "/newNicknameAndLogin":
            {
                nickname = s[1];
                login = s[2];
                break;
            }
            case "To":
            {
                if (!chatText.getText().equals(""))
                    messageArea.insertString(messageArea.getLength(),"\n",right);
                messageArea.insertString(messageArea.getLength(),msgFromServer,right);
                messageArea.setParagraphAttributes(messageArea.getLength(), 1, right, false);
                break;
            }
            case "From":
            {
                if (!chatText.getText().equals(""))
                    messageArea.insertString(messageArea.getLength(),"\n",left);
                messageArea.insertString(messageArea.getLength(),msgFromServer,left);
                messageArea.setParagraphAttributes(messageArea.getLength(), 1, left, false);
                break;
            }

            default:
            {
                if (!chatText.getText().equals(""))
                    messageArea.insertString(messageArea.getLength(),"\n",left);
                messageArea.insertString(messageArea.getLength(),msgFromServer,left);
                messageArea.setParagraphAttributes(messageArea.getLength(), 1, left, false);
            }
        }
    }
    private void writingMsgToClientHandler() throws IOException
    {
        if(!companion.equals(""))
            toClientHandler.writeUTF("/w " + companion + " " + inputMessage.getText());
        else
            toClientHandler.writeUTF(inputMessage.getText());
        inputMessage.setText("");
    }
    private class ConversationListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            companion = e.getActionCommand();
            setTitle("Dialog " + nickname + " with " + companion);
            if(e.getActionCommand().equals("General chat"))
            {
                companion = "";
                setTitle("General chat");
            }
        }
    }
    private class SendListener extends AbstractAction implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if(!inputMessage.getText().equals(""))
            {
                try {
                    writingMsgToClientHandler();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

    }
    private void showLoginWindow() throws IOException
    {

        JTextField loginField = new JTextField();
        JTextField passwordField = new JPasswordField();
        Object[] message = {
                "Login:", loginField,
                "Password:", passwordField,
        };
        UIManager.put("OptionPane.noButtonText", "Register");
        UIManager.put("OptionPane.yesButtonText", "Log in");
        int option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION)
        {
            if(loginField.getText().equals("") || passwordField.getText().equals(""))
            {
                showLoginWindow();
            }
            login = loginField.getText();
            toClientHandler.writeUTF("/userChecking " + loginField.getText() + " " + passwordField.getText());
        }
        else if(option == JOptionPane.NO_OPTION)
        {
            if(loginField.getText().equals("") || passwordField.getText().equals(""))
            {
                showLoginWindow();
            }
            toClientHandler.writeUTF("/userRegistration " + loginField.getText() + " " + passwordField.getText());
        }
        else
        {
            System.exit(1);
        }
    }
    private void ServerConnection()
    {
        try {
            if (socket == null || socket.isClosed())
            {
                socket = new Socket("ec2-34-251-245-108.eu-west-1.compute.amazonaws.com", 4274);
                toClientHandler = new DataOutputStream(socket.getOutputStream());
                fromClientHandler = new DataInputStream(socket.getInputStream());
                showLoginWindow();

                new Thread(() ->
                {
                    try {
                        while (true)
                        {
                            String msgFromServer = fromClientHandler.readUTF();
                            String[] s = msgFromServer.split(" ", 3);
                            if (s[0].equals("/UserIsExist"))
                            {
                                showDialogWindow();
                                nickname = s[1];
                                break;
                            }
                                showLoginWindow();
                        }
                        while (true)
                        {
                            try{
                                listeningMsgFromClientHandler();
                            } catch (IOException | BadLocationException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        closeAllStreams();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void closeAllStreams()
    {
        try {
            fromClientHandler.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            toClientHandler.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void userListArea() throws IOException
    {
        JPanel userListPanel = new JPanel(new GridLayout(0, 1, 5, 5));

        toClientHandler.writeUTF("/getClientList");
        String[] clientList = (fromClientHandler.readUTF()).split(" ");

        if(clientList[0].equals("/ClientList"))
        {
            JButton generalChat = (new JButton("General chat"));
            generalChat.addActionListener(new ConversationListener());
            userListPanel.add(generalChat);
            for (int i = 1; i < clientList.length; i++)
            {
                JButton otherUser = new JButton(clientList[i]);
                userList.put(clientList[i], otherUser);
                otherUser.addActionListener(new ConversationListener());
                userListPanel.add(otherUser);
            }
            JScrollPane UserScroll = new JScrollPane(userListPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            add(UserScroll, BorderLayout.WEST);

        }

    }
    private void inputPanelSetting()
    {
        sendButtonSetting();
        inputMessageFieldSetting();
        inputPanel.add(send, BorderLayout.EAST);
        inputPanel.add(inputMessage, BorderLayout.CENTER);
        add(inputPanel,BorderLayout.SOUTH);
    }
    private void inputMessageFieldSetting()
    {
        inputMessage.setBackground(Color.getHSBColor(240,0.09f,0.26f));
        inputMessage.setSelectionColor(Color.LIGHT_GRAY);
        inputMessage.setForeground(Color.LIGHT_GRAY);
        inputMessage.setFont(new Font("Calibri Light", Font.PLAIN, 25));
        inputMessage.setLineWrap(true);
        inputMessage.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        inputMessage.getActionMap().put("insert-break", enter);
        inputMessage.setWrapStyleWord(true);
        inputMessage.setCaretColor(Color.getHSBColor(0,0,0.9f));

        inputMessage.getInputMap(JComponent.WHEN_FOCUSED).put
                (KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK, true),
                "Shift+Enter released"
                );
        inputMessage.getActionMap().put("Shift+Enter released", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                inputMessage.append(System.lineSeparator());
            }
        });
    }
    private void sendButtonSetting()
    {
        send.setForeground(Color.getHSBColor(0,0,0.9f));
        send.setBackground(Color.getHSBColor(240,0.09f,0.18f));
        send.addActionListener(enter);
    }
    private void messageAreaSetting()
    {

        StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
        StyleConstants.setForeground(left, Color.getHSBColor(234,0.67f,0.58f)); //Green

        StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setForeground(right, Color.getHSBColor(0.234f,0.67f,0.58f)); //Blue

        chatText.setFont(new Font("Calibri Light", Font.PLAIN, 25));
        chatText.setEditable(false);
        chatText.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        chatText.setBackground(Color.getHSBColor(0,0,0.9f));

        JScrollPane scroll = new JScrollPane(chatText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
        messageArea = chatText.getStyledDocument();
    }
    private void windowSetting()
    {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                try {
                    toClientHandler.writeUTF("/end");
                    System.exit(1);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                finally {
                    closeAllStreams();
                }
            }
        });
        setBackground(Color.DARK_GRAY);
        setBounds(500, 200, 600, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("General chat");

    }

}
