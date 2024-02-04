import Models.Shoe;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Gui {
    final static String LOGIN = "Login";
    final static String ORDER = "Beställ";
    final static String REPORTS = "Rapporter";
    final static int extraWindowWidth = 100;
    List<JCheckBox> checkBoxes = new ArrayList<>();
    List<JSpinner> spinners = new ArrayList<>();
    Repository repo;
    Main main;
    int loginCounter = 0;
    int lastOrderID;
    File file;

    public Gui(Main main) {
        this.main = main;
        this.repo = main.repo;
        JFrame frame = new JFrame("Skobutik");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addComponentToPane(frame.getContentPane());

        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException |
                 ClassNotFoundException e) {
            e.printStackTrace();
        }

        /* Set window icon */
        //ImageIcon img = new ImageIcon("./img/shoe16.png");
        //frame.setIconImage(img.getImage());
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void addComponentToPane(Container pane) throws RuntimeException {
        JTabbedPane tabbedPane = new JTabbedPane();
        GridBagConstraints gbc = new GridBagConstraints();

        // Card "Login"
        JPanel cardLogin = new JPanel() {
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                size.width += extraWindowWidth;
                return size;
            }
        };
        cardLogin.setLayout(new GridBagLayout(3, 2));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        cardLogin.add(new JLabel("Användarnamn:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        JTextField userNameTextField = new JTextField("", 10);
        cardLogin.add(userNameTextField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        cardLogin.add(new JLabel("Lösenord:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        JPasswordField passwordField = new JPasswordField("", 10);
        passwordField.setEchoChar('*');
        cardLogin.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton loginButton = new JButton("Logga in");
        cardLogin.add(loginButton, gbc);

        gbc.gridy = 3;
        JLabel messageLabel = new JLabel();
        cardLogin.add(messageLabel, gbc);

        loginButton.addActionListener(e -> {
            String username = userNameTextField.getText();
            String password = new String(passwordField.getPassword());
            try {
                main.thisCustomer = repo.loginCustomer(username, password);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            if (main.thisCustomer == null) {
                loginCounter++;
                messageLabel.setText("Felaktigt login");
                if (loginCounter == 3) {
                    try {
                        // TODO: setText funkar inte ↓
                        messageLabel.setText("För många fösök");
                        java.util.concurrent.TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException("Error executin Timeout.SECONDS.sleep", ex);
                    }
                    System.exit(0);
                }
            } else {
                userNameTextField.setText("");
                passwordField.setText("");
                messageLabel.setText("Välkommen tillbaka " + main.thisCustomer.getFirstName());
                tabbedPane.setEnabledAt(1, true);
                tabbedPane.setEnabledAt(2, true);
            }
        });

        // Card "Order"
        // TODO: Lägg till kategorier
        JPanel cardOrder = new JPanel();
        int numberOfShoes = main.shoes.size();
        cardOrder.setLayout(new GridLayout(numberOfShoes + 2, 8));
        String[] headings = {"Val", "Antal", "Märke", "Färg", "Storlek", "Pris", "Lagersaldo"};
        for (String s : headings)
            cardOrder.add(new JLabel(s));
        for (Shoe shoe : main.shoes) {
            checkBoxes.add(new JCheckBox());
            cardOrder.add(checkBoxes.getLast());
            SpinnerNumberModel model = new SpinnerNumberModel(1, 1, shoe.getInventory(), 1);
            spinners.add(new JSpinner(model));
            cardOrder.add(spinners.getLast());
            cardOrder.add(new JLabel(shoe.getBrand()));
            cardOrder.add(new JLabel(shoe.getColor()));
            cardOrder.add(new JLabel("" + shoe.getSize()));
            cardOrder.add(new JLabel("" + shoe.getPrice()));
            cardOrder.add(new JLabel("" + shoe.getInventory()));
        }
        JButton orderButton = new JButton("Beställ");
        cardOrder.add(orderButton);
        orderButton.addActionListener(e -> {
            try {
                lastOrderID = main.repo.getLastOrderID();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            for (int i = 0; i < numberOfShoes; i++) {
                if (checkBoxes.get(i).isSelected()) {
                    try {
                        main.repo.placeOrder(lastOrderID + 1, main.thisCustomer.getId(), main.shoes.get(i).getId(), (Integer) spinners.get(i).getValue());
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        // Card "Reports"
        JPanel cardReports = new JPanel();
        cardReports.add(new JTextField("TextField", 20));

        // TabbedPane
        tabbedPane.addTab(LOGIN, cardLogin);
        tabbedPane.addTab(ORDER, cardOrder);
        tabbedPane.addTab(REPORTS, cardReports);
        //tabbedPane.setEnabledAt(1, false);
        tabbedPane.setSelectedIndex(1); // FOR TESTING
        tabbedPane.setEnabledAt(2, false);
        pane.add(tabbedPane, BorderLayout.CENTER);
    }

}
