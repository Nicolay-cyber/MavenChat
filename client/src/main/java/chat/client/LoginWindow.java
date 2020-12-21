package chat.client;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginWindow extends JFrame
{
    private final JPanel fieldPanel = new JPanel(new GridLayout(2,0,15,15));
    private final JPanel labelPanel = new JPanel(new GridLayout(2,0,15,15));
    private final JLabel passLabel = new JLabel(" Password ");
    private final JLabel loginLabel = new JLabel("Login ");
    private final JLabel title = new JLabel("Enter your login and password");
    private final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    private final JButton ok = new JButton("Ok");
    private final JButton register = new JButton("Register");
    private final JPasswordField passwordField = new JPasswordField();
    private final JTextField loginField = new JTextField();
    public LoginWindow()
    {
        windowSetting();
        UISetting();
        setVisible(true);
    }
    private void UISetting()
    {
        titleSetting();
        labelPanelSetting();
        fieldPanelSetting();
        buttonPanelSetting();
    }
    private void fieldPanelSetting()
    {
        fieldPanel.add(loginField);
        fieldPanel.add(passwordField);
        add(fieldPanel,BorderLayout.CENTER);
    }
    private void labelPanelSetting()
    {
        loginLabel.setHorizontalAlignment(JLabel.RIGHT);
        passLabel.setHorizontalAlignment(JLabel.RIGHT);
        labelPanel.add(loginLabel);
        labelPanel.add(passLabel);
        add(labelPanel,BorderLayout.WEST);
    }
    private void titleSetting()
    {
        title.setHorizontalAlignment(JLabel.CENTER);
        add(title,BorderLayout.NORTH);
    }
    private void buttonPanelSetting()
    {
        register.addActionListener(new registerUsers());
//        if(isFieldCorrect())
        ok.addActionListener(new okListener());
        buttonPanel.add(register);
        buttonPanel.add(ok);
        add(buttonPanel,BorderLayout.SOUTH);
    }

/*
    private boolean isFieldCorrect() {

    }
*/

    private class okListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {


        }
    }
    private class registerUsers implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {

        }
    }
    private void windowSetting()
    {
        setResizable(false);
        setBounds(500, 200, 400, 200);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("login");
    }

}
