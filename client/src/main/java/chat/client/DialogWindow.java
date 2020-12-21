package chat.client;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class DialogWindow extends JFrame
{
    private final JTextArea messageArea = new JTextArea();
    private final JTextArea inputMessage = new JTextArea("");
    private final JPanel inputPanel = new JPanel(new BorderLayout());
    private final JPanel userList =new JPanel(new GridLayout(0,1,5,5));
    private final JButton send = new JButton("Send");
    private final Action enter = new SendListener();
    private Socket socket;
    private DataOutputStream toServer;
    private DataInputStream fromServer;



    public DialogWindow()
    {
        ServerConnection();
    }

    private void showDialogWindow() throws IOException {
        windowSetting();
        messageAreaSetting();
        inputPanelSetting();

/*        toServer.writeUTF("/getClientList");

        String[] clientList = (fromServer.readUTF()).split(" ");
        for(String user: clientList)
        {
            userList.add(new Button(user));
        }
        add(userList,BorderLayout.WEST);*/

        setVisible(true);
    }

    private void showLoginWindow() throws IOException {

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
            toServer.writeUTF("/userChecking " + loginField.getText() + " " + passwordField.getText());
        }
        else if(option == JOptionPane.NO_OPTION)
        {
            toServer.writeUTF("/userRegistration " + loginField.getText() + " " + passwordField.getText());
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
                socket = new Socket("localhost", 8189);
                toServer = new DataOutputStream(socket.getOutputStream());
                fromServer = new DataInputStream(socket.getInputStream());
                showLoginWindow();

                new Thread(() ->
                {
                    try {
                        while (true)
                        {
                            String msgFromServer = fromServer.readUTF();
                            if (msgFromServer.equals("/UserIsExist"))
                            {
                                showDialogWindow();



                                break;
                            }
                                showLoginWindow();
                        }
                        while (true)
                        {
                            try{
                                String msgFromServer = fromServer.readUTF();
                                if (!messageArea.getText().equals(""))
                                    messageArea.append(System.lineSeparator());
                                messageArea.append(msgFromServer);

                            } catch (IOException e) {
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

    private void closeAllStreams() {
        try {
            fromServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            toServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
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
                    toServer.writeUTF(inputMessage.getText());
                    inputMessage.setText("");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
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
        messageArea.setFont(new Font("Calibri Light", Font.PLAIN, 25));
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        messageArea.setBackground(Color.getHSBColor(0,0,0.9f));

        JScrollPane scroll = new JScrollPane(messageArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }
    private void windowSetting()
    {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                try {
                    toServer.writeUTF("/end");
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
        setTitle("chat");
    }

}
