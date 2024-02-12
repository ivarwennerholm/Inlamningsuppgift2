import Models.Shoe;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Gui {
    final static int extraWindowWidth = 30;
    final static int extraWindowHeight = 20;
    int lastOrderID;
    final Repository repo;
    final Report rprt;
    Font defaultFont = new Font("Arial", Font.PLAIN, 16);
    final List<Shoe> shoesInStock;

    public Gui(Repository repo, Report rprt) {
        this.repo = repo;
        this.rprt = rprt;
        shoesInStock = this.rprt.shoes.stream().filter(s -> s.getInventory() > 0).toList();
        List<JCheckBox> checkBoxes = new ArrayList<>();
        List<JSpinner> spinners = new ArrayList<>();

        JFrame frame = new JFrame("Skobutik");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        UIManager.put("Button.font", defaultFont);
        UIManager.put("CheckBox.font", defaultFont);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("Panel.font", defaultFont);
        UIManager.put("TabbedPane.font", defaultFont);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("PasswordField.font", defaultFont);
        UIManager.put("TextArea.font", defaultFont);
        UIManager.put("TextPane.font", defaultFont);
        UIManager.put("ComboBox.font", defaultFont);
        UIManager.put("Spinner.font", defaultFont);

        JTabbedPane tabbedPane = new JTabbedPane();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Card "Login"
        JPanel cardLogin = new JPanel() {
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                size.width += extraWindowWidth;
                size.height += extraWindowHeight;
                return size;
            }
        };
        cardLogin.setLayout(new GridBagLayout());

        // Rad 1
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        cardLogin.add(new JLabel("Användarnamn:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        JTextField userNameTextField = new JTextField("", 10);
        cardLogin.add(userNameTextField, gbc);

        // Rad 2
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        cardLogin.add(new JLabel("Lösenord:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        JPasswordField passwordField = new JPasswordField("", 10);
        passwordField.setEchoChar('*');
        cardLogin.add(passwordField, gbc);

        // Rad 3
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton loginButton = new JButton("Logga in");
        cardLogin.add(loginButton, gbc);

        // Rad 4
        gbc.gridy = 3;
        JLabel loginMessageLabel = new JLabel();
        cardLogin.add(loginMessageLabel, gbc);

        loginButton.addActionListener(e -> {
            String username = userNameTextField.getText();
            String password = new String(passwordField.getPassword());
            try {
                repo.thisCustomer = repo.loginCustomer(username, password);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            if (repo.thisCustomer == null) {
                loginMessageLabel.setText("Felaktigt login");
            } else {
                userNameTextField.setText("");
                passwordField.setText("");
                loginMessageLabel.setText("Välkommen tillbaka " + repo.thisCustomer.getFirstName() + "!");
                tabbedPane.setEnabledAt(1, true);
                tabbedPane.setEnabledAt(2, true);
            }
        });

        // Card "Order"
        JPanel cardOrder = new JPanel() {
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                size.width += extraWindowWidth;
                size.height += extraWindowHeight;
                return size;
            }
        };
        int numberOfShoes = shoesInStock.size();
        String[] headings = {"Val", "Antal", "Märke", "Färg", "Storlek", "Pris", "Lagersaldo"};
        cardOrder.setLayout(new GridBagLayout());

        // Rad 1
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        for (String s : headings) {
            cardOrder.add(new JLabel("<html><b>" + s + "</b></html>"), gbc);
            gbc.gridx++;
        }

        // Rad 2
        gbc.gridx = 0;
        gbc.gridy = 1;
        for (Shoe shoe : shoesInStock) {
            checkBoxes.add(new JCheckBox());
            cardOrder.add(checkBoxes.getLast(), gbc);

            gbc.gridx++;
            SpinnerNumberModel model = new SpinnerNumberModel(1, 1, shoe.getInventory(), 1);
            spinners.add(new JSpinner(model));
            JComponent editor = spinners.getLast().getEditor();
            JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setColumns(3);
            cardOrder.add(spinners.getLast(), gbc);

            gbc.gridx++;
            cardOrder.add(new JLabel(shoe.getBrand()), gbc);

            gbc.gridx++;
            cardOrder.add(new JLabel(shoe.getColor()), gbc);

            gbc.gridx++;
            cardOrder.add(new JLabel("" + shoe.getSize()), gbc);

            gbc.gridx++;
            cardOrder.add(new JLabel(shoe.getPrice() + "kr"), gbc);

            gbc.gridx++;
            cardOrder.add(new JLabel("" + shoe.getInventory()), gbc);

            gbc.gridx = 0;
            gbc.gridy++;
        }

        gbc.gridx = 3;
        JButton orderButton = new JButton("Beställ");
        cardOrder.add(orderButton, gbc);

        gbc.gridx = 2;
        gbc.gridy++;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel orderMessageLabel = new JLabel();
        cardOrder.add(orderMessageLabel, gbc);
        orderButton.addActionListener(e -> {
            try {
                lastOrderID = repo.getLastOrderID();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            for (int i = 0; i < numberOfShoes; i++) {
                if (checkBoxes.get(i).isSelected()) {
                    try {
                        repo.placeOrder(lastOrderID + 1, repo.thisCustomer.getId(), rprt.shoes.get(i).getId(), (Integer) spinners.get(i).getValue());
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            orderMessageLabel.setText("Beställningen genomförd!");
            for (JCheckBox box : checkBoxes)
                box.setSelected(false);
            for (JSpinner spinner : spinners)
                spinner.setValue(1);
        });

        // Card "Reports"
        JPanel cardReports = new JPanel() {
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                size.width += extraWindowWidth;
                size.height += extraWindowHeight;
                return size;
            }
        };
        cardReports.setLayout(new GridBagLayout());

        // Skapa beskrivningar för de olika rapporterna
        final String QUERYONE = "Visa de kunder som beställt skor av ett visst märke, en viss färg eller storlek";
        final String QUERYTWO = "Visa alla kunder och ordrar per kund";
        final String QUERYTHREE = "Visa alla kunder och det totala ordervärdet per kund";
        final String QUERYFOUR = "Visa beställningsvärdet per ort";
        final String QUERYFIVE = "Visa topplista över de mest sålda skorna";

        // Skapa och fyll i JComboBoxes
        JComboBox brandsBox, colorsBox, sizesBox;
        try {
            brandsBox = new JComboBox(repo.getOrderedTypes("brands"));
            colorsBox = new JComboBox(repo.getOrderedTypes("colors"));
            sizesBox = new JComboBox(repo.getOrderedTypes("sizes"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Skapa JRadioButtons
        JRadioButton queryOneButton = new JRadioButton();
        JRadioButton queryTwoButton = new JRadioButton();
        JRadioButton queryThreeButton = new JRadioButton();
        JRadioButton queryFourButton = new JRadioButton();
        JRadioButton queryFiveButton = new JRadioButton();
        JRadioButton typeBrandsButton = new JRadioButton();
        JRadioButton typeColorsButton = new JRadioButton();
        JRadioButton typeSizesButton = new JRadioButton();
        queryOneButton.setSelected(true);
        typeBrandsButton.setSelected(true);

        // Skapa och lägg till i ButtonGroups
        ButtonGroup mainChoiceGroup = new ButtonGroup();
        ButtonGroup typesChoiceGroup = new ButtonGroup();
        mainChoiceGroup.add(queryOneButton);
        mainChoiceGroup.add(queryTwoButton);
        mainChoiceGroup.add(queryThreeButton);
        mainChoiceGroup.add(queryFourButton);
        mainChoiceGroup.add(queryFiveButton);
        typesChoiceGroup.add(typeBrandsButton);
        typesChoiceGroup.add(typeColorsButton);
        typesChoiceGroup.add(typeSizesButton);

        JButton showReportButton = new JButton("Visa rapport");

        // Rad 1
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.gridwidth = 1;
        cardReports.add(queryOneButton, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.LINE_START;
        cardReports.add(new JLabel(QUERYONE), gbc);

        // Rad 2
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        cardReports.add(typeBrandsButton, gbc);

        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        cardReports.add(new JLabel("Märke"), gbc);

        gbc.gridx = 3;
        cardReports.add(brandsBox, gbc);

        // Rad 3
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        cardReports.add(typeColorsButton, gbc);

        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        cardReports.add(new JLabel("Färg"), gbc);

        gbc.gridx = 3;
        cardReports.add(colorsBox, gbc);

        // Rad 4
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_END;
        cardReports.add(typeSizesButton, gbc);

        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        cardReports.add(new JLabel("Storlek"), gbc);

        gbc.gridx = 3;
        cardReports.add(sizesBox, gbc);

        // Rad 5
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.LINE_END;
        cardReports.add(queryTwoButton, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.LINE_START;
        cardReports.add(new JLabel(QUERYTWO), gbc);

        // Rad 6
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        cardReports.add(queryThreeButton, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.LINE_START;
        cardReports.add(new JLabel(QUERYTHREE), gbc);

        // Rad 7
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        cardReports.add(queryFourButton, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.LINE_START;
        cardReports.add(new JLabel(QUERYFOUR), gbc);

        // Rad 8
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        cardReports.add(queryFiveButton, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.LINE_START;
        cardReports.add(new JLabel(QUERYFIVE), gbc);

        // Rad 9
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        cardReports.add(showReportButton, gbc);
        showReportButton.addActionListener(e -> {
            List<String[]> report;
            String queryOne;
            if (queryOneButton.isSelected()) {
                if (typeBrandsButton.isSelected()) {
                    report = rprt.getListQueryOne("brand", brandsBox.getSelectedItem().toString());
                    queryOne = "Visa de kunder som beställt skor av märket " + brandsBox.getSelectedItem().toString();

                } else if (typeColorsButton.isSelected()) {
                    report = rprt.getListQueryOne("color", colorsBox.getSelectedItem().toString());
                    queryOne = "Visa de kunder som beställt skor med " + colorsBox.getSelectedItem().toString().toLowerCase() + " färg";
                } else {
                    report = rprt.getListQueryOne("size", sizesBox.getSelectedItem().toString());
                    queryOne = "Visa de kunder som beställt skor av storleken " + sizesBox.getSelectedItem().toString();
                }
                new ReportWindow(queryOne, report);
            } else if (queryTwoButton.isSelected()) {
                report = rprt.getListQueryTwo();
                new ReportWindow(QUERYTWO, report);
            } else if (queryThreeButton.isSelected()) {
                report = rprt.getListQueryThree();
                new ReportWindow(QUERYTHREE, report);
            } else if (queryFourButton.isSelected()) {
                report = rprt.getListQueryFour();
                new ReportWindow(QUERYFOUR, report);
            } else if (queryFiveButton.isSelected()) {
                report = rprt.getListQueryFive();
                new ReportWindow(QUERYFIVE, report);
            }
        });

        // TabbedPane högst upp
        tabbedPane.addTab("Login", cardLogin);
        tabbedPane.addTab("Beställ", cardOrder);
        tabbedPane.addTab("Rapporter", cardReports);
        tabbedPane.setEnabledAt(1, false);
        tabbedPane.setEnabledAt(2, false);
        frame.add(tabbedPane, BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static class ReportWindow extends JFrame {
        ReportWindow(String title, List<String[]> report) {
            JPanel panel = new JPanel() {
                public Dimension getPreferredSize() {
                    Dimension size = super.getPreferredSize();
                    size.width += extraWindowWidth;
                    size.height += extraWindowHeight;
                    return size;
                }
            };
            this.add(panel);
            GridBagConstraints gbc = new GridBagConstraints();
            panel.setLayout(new GridBagLayout());
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = report.getFirst().length;
            gbc.anchor = GridBagConstraints.CENTER;
            panel.add(new JLabel("<html><b>" + title + "</b></html>"), gbc);
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.LINE_START;
            for (String[] array : report) {
                gbc.gridy++;
                for (int i = 0; i < array.length; i++) {
                    gbc.gridx = i;
                    panel.add(new JLabel(array[i]), gbc);
                }
            }
            this.pack();
            this.setLocationRelativeTo(null);
            this.setVisible(true);
        }
    }

}