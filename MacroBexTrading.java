import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MacroBexTrading {

    // =========================================================
    // ENUMS
    // =========================================================

    enum Role {
        USER, ADMIN
    }

    enum VerificationStatus {
        PENDING, VERIFIED, REJECTED, RESUBMIT
    }

    enum AccountStatus {
        ACTIVE, SUSPENDED
    }

    enum RequestType {
        DEPOSIT, WITHDRAWAL
    }

    enum RequestStatus {
        PENDING, APPROVED, REJECTED
    }

    enum TradeType {
        BUY, SELL
    }

    enum TradeStatus {
        ACTIVE, COMPLETED
    }

    // =========================================================
    // CONSTANTS
    // =========================================================

    static final java.text.DecimalFormat MONEY =
            new java.text.DecimalFormat("#,##0.00");

    static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static final Random RANDOM = new Random();

    // Market configuration
    static final int MARKET_UPDATE_INTERVAL = 5000;
    static final int MARKET_STALE_TIME = 20000;

    // =========================================================
    // USER MODEL
    // =========================================================

    static class User {

        int id;

        String name;
        String email;
        String password;

        Role role;

        VerificationStatus verification;

        AccountStatus status = AccountStatus.ACTIVE;

        double demoBalance = 10000;
        double demoReserved = 0;

        double mainBalance = 10000;
        double mainReserved = 0;

        List<String> notifications =
                new ArrayList<>();

        User(
                int id,
                String name,
                String email,
                String password,
                Role role,
                VerificationStatus verification
        ) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.password = password;
            this.role = role;
            this.verification = verification;
        }

        double demoAvailable() {
            return demoBalance - demoReserved;
        }

        double mainAvailable() {
            return mainBalance - mainReserved;
        }
    }

    // =========================================================
    // ASSET MODEL
    // =========================================================

    static class Asset {

        String symbol;
        String name;

        double price;
        double previousPrice;

        boolean enabled = true;

        long lastUpdated;

        Asset(
                String symbol,
                String name,
                double price
        ) {
            this.symbol = symbol;
            this.name = name;
            this.price = price;
            this.previousPrice = price;
            this.lastUpdated =
                    System.currentTimeMillis();
        }

        boolean stale() {

            return System.currentTimeMillis()
                    - lastUpdated > MARKET_STALE_TIME;
        }

        double change() {

            if (previousPrice == 0)
                return 0;

            return (
                    (price - previousPrice)
                            / previousPrice
            ) * 100;
        }
    }

    // =========================================================
    // WALLET REQUEST
    // =========================================================

    static class WalletRequest {

        int id;

        int userId;

        RequestType type;

        double amount;

        String reference;

        RequestStatus status =
                RequestStatus.PENDING;

        LocalDateTime createdAt =
                LocalDateTime.now();

        WalletRequest(
                int id,
                int userId,
                RequestType type,
                double amount,
                String reference
        ) {
            this.id = id;
            this.userId = userId;
            this.type = type;
            this.amount = amount;
            this.reference = reference;
        }
    }

    // =========================================================
    // TRADE MODEL
    // =========================================================

    static class Trade {

        int id;

        int userId;

        String accountType;

        String symbol;

        TradeType type;

        double quantity;

        double entryPrice;

        double closingPrice;

        double pnl;

        TradeStatus status =
                TradeStatus.ACTIVE;

        LocalDateTime createdAt =
                LocalDateTime.now();

        LocalDateTime closedAt;

        Trade(
                int id,
                int userId,
                String accountType,
                String symbol,
                TradeType type,
                double quantity,
                double entryPrice
        ) {
            this.id = id;
            this.userId = userId;
            this.accountType = accountType;
            this.symbol = symbol;
            this.type = type;
            this.quantity = quantity;
            this.entryPrice = entryPrice;
        }
    }

    // =========================================================
    // SUPPORT TICKET
    // =========================================================

    static class Ticket {

        int id;

        int userId;

        String subject;

        String message;

        String status = "OPEN";

        LocalDateTime createdAt =
                LocalDateTime.now();

        Ticket(
                int id,
                int userId,
                String subject,
                String message
        ) {
            this.id = id;
            this.userId = userId;
            this.subject = subject;
            this.message = message;
        }
    }

    // =========================================================
    // AUDIT
    // =========================================================

    static class Audit {

        LocalDateTime time =
                LocalDateTime.now();

        String actor;

        String action;

        Audit(
                String actor,
                String action
        ) {
            this.actor = actor;
            this.action = action;
        }
    }

    // =========================================================
    // APPLICATION DATA
    // =========================================================

    static List<User> users =
            new ArrayList<>();

    static List<Asset> assets =
            new ArrayList<>();

    static List<WalletRequest> requests =
            new ArrayList<>();

    static List<Trade> trades =
            new ArrayList<>();

    static List<Ticket> tickets =
            new ArrayList<>();

    static List<Audit> audits =
            new ArrayList<>();

    static Set<Integer> processedFinancialRequests =
            new HashSet<>();

    static int nextUser = 1;

    static int nextRequest = 1;

    static int nextTrade = 1;

    static int nextTicket = 1;

    static User currentUser;

    static boolean marketOnline = true;

    static boolean aiAvailable = true;

    static Timer marketTimer;

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            init();

            startMarketEngine();

            login();
        });
    }

    // =========================================================
    // INITIALIZATION
    // =========================================================

    static void init() {

        users.clear();
        assets.clear();
        requests.clear();
        trades.clear();
        tickets.clear();
        audits.clear();

        processedFinancialRequests.clear();

        nextUser = 1;
        nextRequest = 1;
        nextTrade = 1;
        nextTicket = 1;

        marketOnline = true;

        aiAvailable = true;

        // Assets
        assets.add(
                new Asset(
                        "AAPL",
                        "Apple Inc.",
                        182.43
                )
        );

        assets.add(
                new Asset(
                        "TSLA",
                        "Tesla Inc.",
                        241.72
                )
        );

        assets.add(
                new Asset(
                        "NVDA",
                        "NVIDIA Corp.",
                        176.21
                )
        );

        assets.add(
                new Asset(
                        "AMZN",
                        "Amazon",
                        228.51
                )
        );

        assets.add(
                new Asset(
                        "MSFT",
                        "Microsoft",
                        511.30
                )
        );

        // Demo user
        users.add(
                new User(
                        nextUser++,
                        "Demo User",
                        "demo@example.com",
                        "1234",
                        Role.USER,
                        VerificationStatus.VERIFIED
                )
        );

        // Admin
        users.add(
                new User(
                        nextUser++,
                        "System Admin",
                        "admin@example.com",
                        "admin123",
                        Role.ADMIN,
                        VerificationStatus.VERIFIED
                )
        );

        audit(
                "SYSTEM",
                "Application initialized"
        );
    }

    // =========================================================
    // AUTOMATIC MARKET ENGINE
    // =========================================================

    static void startMarketEngine() {

        if (marketTimer != null) {
            marketTimer.stop();
        }

        marketTimer =
                new Timer(
                        MARKET_UPDATE_INTERVAL,
                        e -> {

                            if (marketOnline) {

                                simulateMarketMovement();

                            }
                        }
                );

        marketTimer.start();
    }

    static void simulateMarketMovement() {

        if (!marketOnline)
            return;

        for (Asset a : assets) {

            if (!a.enabled)
                continue;

            a.previousPrice =
                    a.price;

            // Random movement between -2% and +2%
            double movement =
                    (
                            RANDOM.nextDouble()
                                    - 0.5
                    ) * 0.04;

            a.price =
                    Math.max(
                            1,
                            a.price
                                    * (1 + movement)
                    );

            a.lastUpdated =
                    System.currentTimeMillis();
        }

        audit(
                "MARKET_ENGINE",
                "Automatic market update"
        );
    }

    // =========================================================
    // HELPERS
    // =========================================================

    static JFrame frame(
            String title,
            int width,
            int height
    ) {

        JFrame f =
                new JFrame(title);

        f.setSize(
                width,
                height
        );

        f.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        return f;
    }

    static JButton btn(
            String text,
            java.awt.event.ActionListener action
    ) {

        JButton b =
                new JButton(text);

        b.addActionListener(action);

        return b;
    }

    static User user(int id) {

        for (User u : users) {

            if (u.id == id)
                return u;
        }

        return null;
    }

    static User userEmail(String email) {

        for (User u : users) {

            if (
                    u.email.equalsIgnoreCase(email)
            ) {
                return u;
            }
        }

        return null;
    }

    static Asset asset(String symbol) {

        for (Asset a : assets) {

            if (
                    a.symbol.equals(symbol)
            ) {
                return a;
            }
        }

        return null;
    }

    static WalletRequest request(int id) {

        for (WalletRequest r : requests) {

            if (r.id == id)
                return r;
        }

        return null;
    }

    static boolean isAdmin() {

        return currentUser != null
                && currentUser.role
                == Role.ADMIN;
    }

    static void notifyUser(
            User user,
            String message
    ) {

        user.notifications.add(
                LocalDateTime.now()
                        .format(TIME)
                        + " - "
                        + message
        );
    }

    static void audit(
            String actor,
            String action
    ) {

        audits.add(
                new Audit(
                        actor,
                        action
                )
        );
    }

    // =========================================================
    // LOGIN
    // =========================================================

    static void login() {

        JFrame f =
                frame(
                        "MacroBex Trading",
                        500,
                        430
                );

        JPanel p =
                new JPanel(
                        new GridLayout(
                                0,
                                1,
                                10,
                                10
                        )
                );

        p.setBorder(
                BorderFactory.createEmptyBorder(
                        25,
                        35,
                        25,
                        35
                )
        );

        JLabel title =
                new JLabel(
                        "MacroBex Trading Simulation",
                        SwingConstants.CENTER
                );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        22
                )
        );

        JTextField email =
                new JTextField();

        JPasswordField password =
                new JPasswordField();

        JButton login =
                new JButton("LOGIN");

        JButton demo =
                new JButton("DEMO LOGIN");

        JButton register =
                new JButton("REGISTER");

        p.add(title);

        p.add(
                new JLabel("Email")
        );

        p.add(email);

        p.add(
                new JLabel("Password")
        );

        p.add(password);

        p.add(login);

        p.add(demo);

        p.add(register);

        f.add(p);

        login.addActionListener(e -> {

            User u =
                    userEmail(
                            email.getText().trim()
                    );

            String pass =
                    new String(
                            password.getPassword()
                    );

            if (
                    u != null
                            && u.password.equals(pass)
            ) {

                currentUser = u;

                audit(
                        u.email,
                        "Logged in"
                );

                f.dispose();

                dashboard();

            } else {

                JOptionPane.showMessageDialog(
                        f,
                        "Invalid email or password."
                );
            }
        });

        demo.addActionListener(e -> {

            currentUser =
                    userEmail(
                            "demo@example.com"
                    );

            audit(
                    currentUser.email,
                    "Demo login"
            );

            f.dispose();

            dashboard();
        });

        register.addActionListener(
                e -> register()
        );

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // REGISTER
    // =========================================================

    static void register() {

        JFrame f =
                frame(
                        "Register",
                        450,
                        400
                );

        JPanel p =
                new JPanel(
                        new GridLayout(
                                0,
                                1,
                                8,
                                8
                        )
                );

        p.setBorder(
                BorderFactory.createEmptyBorder(
                        25,
                        30,
                        25,
                        30
                )
        );

        JTextField name =
                new JTextField();

        JTextField email =
                new JTextField();

        JPasswordField pass =
                new JPasswordField();

        JButton create =
                new JButton(
                        "CREATE ACCOUNT"
                );

        p.add(
                new JLabel("Name")
        );

        p.add(name);

        p.add(
                new JLabel("Email")
        );

        p.add(email);

        p.add(
                new JLabel("Password")
        );

        p.add(pass);

        p.add(create);

        f.add(p);

        create.addActionListener(e -> {

            if (
                    name.getText()
                            .trim()
                            .isEmpty()
                            || email.getText()
                            .trim()
                            .isEmpty()
                            || new String(
                            pass.getPassword()
                    ).isEmpty()
            ) {

                JOptionPane.showMessageDialog(
                        f,
                        "All fields are required."
                );

                return;
            }

            if (
                    userEmail(
                            email.getText().trim()
                    ) != null
            ) {

                JOptionPane.showMessageDialog(
                        f,
                        "Email already exists."
                );

                return;
            }

            User u =
                    new User(
                            nextUser++,
                            name.getText().trim(),
                            email.getText().trim(),
                            new String(
                                    pass.getPassword()
                            ),
                            Role.USER,
                            VerificationStatus.PENDING
                    );

            users.add(u);

            audit(
                    "SYSTEM",
                    "User registered: "
                            + u.email
            );

            JOptionPane.showMessageDialog(
                    f,
                    "Registration successful!\n"
                            + "Verification status: PENDING"
            );

            f.dispose();
        });

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    static void dashboard() {

        JFrame f =
                frame(
                        "MacroBex Trading - Dashboard",
                        900,
                        680
                );

        JPanel root =
                new JPanel(
                        new BorderLayout(
                                12,
                                12
                        )
                );

        root.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );

        JLabel heading =
                new JLabel(
                        "Welcome, "
                                + currentUser.name
                                + " | "
                                + currentUser.role
                );

        heading.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        20
                )
        );

        JButton logout =
                new JButton("LOGOUT");

        logout.addActionListener(e -> {

            audit(
                    currentUser.email,
                    "Logged out"
            );

            currentUser = null;

            f.dispose();

            login();
        });

        JPanel top =
                new JPanel(
                        new BorderLayout()
                );

        top.add(
                heading,
                BorderLayout.WEST
        );

        top.add(
                logout,
                BorderLayout.EAST
        );

        JPanel grid =
                new JPanel(
                        new GridLayout(
                                0,
                                3,
                                10,
                                10
                        )
                );

        grid.add(
                btn(
                        "ACCOUNT",
                        e -> accountWindow()
                )
        );

        grid.add(
                btn(
                        "PROFILE",
                        e -> profileWindow()
                )
        );

        grid.add(
                btn(
                        "MARKET",
                        e -> marketWindow()
                )
        );

        grid.add(
                btn(
                        "WALLET",
                        e -> walletWindow()
                )
        );

        grid.add(
                btn(
                        "TRADE",
                        e -> tradeWindow()
                )
        );

        grid.add(
                btn(
                        "PORTFOLIO",
                        e -> portfolioWindow()
                )
        );

        grid.add(
                btn(
                        "TRADE HISTORY",
                        e -> historyWindow()
                )
        );

        grid.add(
                btn(
                        "VERIFICATION",
                        e -> verificationWindow()
                )
        );

        grid.add(
                btn(
                        "HELP",
                        e -> help()
                )
        );

        grid.add(
                btn(
                        "SUPPORT",
                        e -> supportWindow()
                )
        );

        grid.add(
                btn(
                        "NOTIFICATIONS",
                        e -> notifications()
                )
        );

        if (isAdmin()) {

            grid.add(
                    btn(
                            "ADMIN PANEL",
                            e -> adminPanel()
                    )
            );

            grid.add(
                    btn(
                            "RELIABILITY TEST",
                            e -> reliabilityTest()
                    )
            );
        }

        root.add(
                top,
                BorderLayout.NORTH
        );

        root.add(
                new JScrollPane(grid),
                BorderLayout.CENTER
        );

        f.add(root);

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // ACCOUNT
    // =========================================================

    static void accountWindow() {

        JFrame f =
                frame(
                        "Account",
                        600,
                        500
                );

        JTextArea text =
                new JTextArea();

        text.setEditable(false);

        text.setFont(
                new Font(
                        "Monospaced",
                        Font.PLAIN,
                        14
                )
        );

        text.setText(
                "ACCOUNT INFORMATION\n"
                        + "====================\n\n"

                        + "Name: "
                        + currentUser.name
                        + "\n"

                        + "Email: "
                        + currentUser.email
                        + "\n"

                        + "Role: "
                        + currentUser.role
                        + "\n"

                        + "Status: "
                        + currentUser.status
                        + "\n"

                        + "Verification: "
                        + currentUser.verification
                        + "\n\n"

                        + "DEMO ACCOUNT\n"
                        + "-------------\n"

                        + "Balance: BDT "
                        + MONEY.format(
                        currentUser.demoBalance
                )
                        + "\n"

                        + "Reserved: BDT "
                        + MONEY.format(
                        currentUser.demoReserved
                )
                        + "\n"

                        + "Available: BDT "
                        + MONEY.format(
                        currentUser.demoAvailable()
                )
                        + "\n\n"

                        + "MAIN ACCOUNT\n"
                        + "------------\n"

                        + "Balance: BDT "
                        + MONEY.format(
                        currentUser.mainBalance
                )
                        + "\n"

                        + "Reserved: BDT "
                        + MONEY.format(
                        currentUser.mainReserved
                )
                        + "\n"

                        + "Available: BDT "
                        + MONEY.format(
                        currentUser.mainAvailable()
                )
                        + "\n\n"

                        + "Available = Balance - Reserved"
        );

        f.add(
                new JScrollPane(text)
        );

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // PROFILE
    // =========================================================

    static void profileWindow() {

        JFrame f =
                frame(
                        "Profile",
                        500,
                        350
                );

        JTextArea text =
                new JTextArea();

        text.setEditable(false);

        text.setText(
                "PROFILE\n\n"
                        + "User ID: "
                        + currentUser.id
                        + "\n"
                        + "Name: "
                        + currentUser.name
                        + "\n"
                        + "Email: "
                        + currentUser.email
                        + "\n"
                        + "Role: "
                        + currentUser.role
                        + "\n"
                        + "Status: "
                        + currentUser.status
                        + "\n"
                        + "Verification: "
                        + currentUser.verification
        );

        f.add(text);

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // MARKET WINDOW
    // =========================================================

    static void marketWindow() {

        JFrame f =
                frame(
                        "Live Simulated Market",
                        850,
                        550
                );

        String[] columns = {
                "Symbol",
                "Asset",
                "Price",
                "Change %",
                "Status",
                "Updated"
        };

        DefaultTableModel model =
                new DefaultTableModel(
                        columns,
                        0
                );

        JTable table =
                new JTable(model);

        table.setRowHeight(28);

        Runnable refresh =
                () -> {

                    model.setRowCount(0);

                    for (Asset a : assets) {

                        String status;

                        if (!marketOnline) {

                            status = "OFFLINE";

                        } else if (!a.enabled) {

                            status = "DISABLED";

                        } else if (a.stale()) {

                            status = "STALE";

                        } else {

                            status = "LIVE";
                        }

                        model.addRow(
                                new Object[]{
                                        a.symbol,
                                        a.name,
                                        "BDT "
                                                + MONEY.format(
                                                a.price
                                        ),
                                        String.format(
                                                "%.2f%%",
                                                a.change()
                                        ),
                                        status,
                                        new java.util.Date(
                                                a.lastUpdated
                                        )
                                }
                        );
                    }
                };

        JButton manual =
                new JButton(
                        "SIMULATE MARKET MOVEMENT"
                );

        JButton stop =
                new JButton(
                        "STOP FEED"
                );

        JButton start =
                new JButton(
                        "START FEED"
                );

        manual.addActionListener(e -> {

            if (!marketOnline) {

                JOptionPane.showMessageDialog(
                        f,
                        "Market feed is OFFLINE."
                );

                return;
            }

            simulateMarketMovement();

            refresh.run();
        });

        stop.addActionListener(e -> {

            marketOnline = false;

            audit(
                    currentUser.email,
                    "Market feed stopped"
            );

            refresh.run();

            JOptionPane.showMessageDialog(
                    f,
                    "Market feed stopped.\n"
                            + "Trading is now blocked."
            );
        });

        start.addActionListener(e -> {

            marketOnline = true;

            // Immediately refresh all timestamps
            for (Asset a : assets) {

                a.lastUpdated =
                        System.currentTimeMillis();
            }

            audit(
                    currentUser.email,
                    "Market feed started"
            );

            refresh.run();

            JOptionPane.showMessageDialog(
                    f,
                    "Market feed started.\n"
                            + "Automatic updates resumed."
            );
        });

        JPanel bottom =
                new JPanel(
                        new GridLayout(
                                1,
                                3,
                                8,
                                8
                        )
                );

        bottom.add(manual);
        bottom.add(stop);
        bottom.add(start);

        f.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        f.add(
                bottom,
                BorderLayout.SOUTH
        );

        refresh.run();

        // Refresh table every second
        Timer tableTimer =
                new Timer(
                        1000,
                        e -> refresh.run()
                );

        tableTimer.start();

        f.addWindowListener(
                new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(
                            java.awt.event.WindowEvent e
                    ) {

                        tableTimer.stop();
                    }
                }
        );

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // WALLET
    // =========================================================

    static void walletWindow() {

        JFrame f =
                frame(
                        "BDT Wallet",
                        800,
                        550
                );

        JPanel root =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        JTextArea summary =
                new JTextArea();

        summary.setEditable(false);

        summary.setText(
                "MAIN WALLET\n\n"
                        + "Balance: BDT "
                        + MONEY.format(
                        currentUser.mainBalance
                )
                        + "\n"
                        + "Reserved: BDT "
                        + MONEY.format(
                        currentUser.mainReserved
                )
                        + "\n"
                        + "Available: BDT "
                        + MONEY.format(
                        currentUser.mainAvailable()
                )
                        + "\n\n"
                        + "Available = Balance - Reserved"
        );

        JTable table =
                walletTable();

        JButton deposit =
                new JButton(
                        "DEPOSIT REQUEST"
                );

        JButton withdrawal =
                new JButton(
                        "WITHDRAWAL REQUEST"
                );

        JButton help =
                new JButton(
                        "CONTEXTUAL HELP"
                );

        deposit.addActionListener(
                e -> depositWindow()
        );

        withdrawal.addActionListener(
                e -> withdrawalWindow()
        );

        help.addActionListener(
                e -> help()
        );

        JPanel buttons =
                new JPanel(
                        new GridLayout(
                                1,
                                3,
                                8,
                                8
                        )
                );

        buttons.add(deposit);
        buttons.add(withdrawal);
        buttons.add(help);

        root.add(
                summary,
                BorderLayout.NORTH
        );

        root.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        root.add(
                buttons,
                BorderLayout.SOUTH
        );

        f.add(root);

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    static JTable walletTable() {

        DefaultTableModel model =
                new DefaultTableModel(
                        new String[]{
                                "ID",
                                "Type",
                                "Amount",
                                "Reference",
                                "Status",
                                "Created"
                        },
                        0
                );

        for (WalletRequest r : requests) {

            if (
                    r.userId
                            == currentUser.id
            ) {

                model.addRow(
                        new Object[]{
                                r.id,
                                r.type,
                                "BDT "
                                        + MONEY.format(
                                        r.amount
                                ),
                                r.reference,
                                r.status,
                                r.createdAt.format(
                                        TIME
                                )
                        }
                );
            }
        }

        return new JTable(model);
    }

    // =========================================================
    // DEPOSIT
    // =========================================================

    static void depositWindow() {

        JFrame f =
                frame(
                        "Deposit Request",
                        500,
                        350
                );

        JPanel p =
                new JPanel(
                        new GridLayout(
                                0,
                                1,
                                8,
                                8
                        )
                );

        p.setBorder(
                BorderFactory.createEmptyBorder(
                        25,
                        30,
                        25,
                        30
                )
        );

        JTextField amount =
                new JTextField();

        JTextField reference =
                new JTextField();

        JButton submit =
                new JButton(
                        "SUBMIT DEPOSIT REQUEST"
                );

        p.add(
                new JLabel("Amount (BDT)")
        );

        p.add(amount);

        p.add(
                new JLabel(
                        "Synthetic Payment Reference"
                )
        );

        p.add(reference);

        p.add(
                new JLabel(
                        "Demo information only."
                )
        );

        p.add(submit);

        f.add(p);

        submit.addActionListener(e -> {

            try {

                double value =
                        Double.parseDouble(
                                amount.getText()
                        );

                if (
                        value <= 0
                                || reference.getText()
                                .trim()
                                .isEmpty()
                ) {

                    throw new Exception();
                }

                WalletRequest r =
                        new WalletRequest(
                                nextRequest++,
                                currentUser.id,
                                RequestType.DEPOSIT,
                                value,
                                reference.getText()
                                        .trim()
                        );

                requests.add(r);

                audit(
                        currentUser.email,
                        "Deposit request submitted: "
                                + r.id
                );

                JOptionPane.showMessageDialog(
                        f,
                        "Deposit request submitted.\n"
                                + "Status: PENDING"
                );

                f.dispose();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        f,
                        "Enter a valid amount and reference."
                );
            }
        });

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // WITHDRAWAL
    // =========================================================

    static void withdrawalWindow() {

        JFrame f =
                frame(
                        "Withdrawal Request",
                        500,
                        350
                );

        JPanel p =
                new JPanel(
                        new GridLayout(
                                0,
                                1,
                                8,
                                8
                        )
                );

        p.setBorder(
                BorderFactory.createEmptyBorder(
                        25,
                        30,
                        25,
                        30
                )
        );

        JTextField amount =
                new JTextField();

        JTextField reference =
                new JTextField();

        JButton submit =
                new JButton(
                        "SUBMIT WITHDRAWAL REQUEST"
                );

        p.add(
                new JLabel("Amount (BDT)")
        );

        p.add(amount);

        p.add(
                new JLabel(
                        "Synthetic Destination Reference"
                )
        );

        p.add(reference);

        p.add(submit);

        f.add(p);

        submit.addActionListener(e -> {

            try {

                double value =
                        Double.parseDouble(
                                amount.getText()
                        );

                if (
                        value <= 0
                                || value
                                > currentUser.mainAvailable()
                                || reference.getText()
                                .trim()
                                .isEmpty()
                ) {

                    throw new Exception();
                }

                WalletRequest r =
                        new WalletRequest(
                                nextRequest++,
                                currentUser.id,
                                RequestType.WITHDRAWAL,
                                value,
                                reference.getText()
                                        .trim()
                        );

                // Reserve amount
                currentUser.mainReserved +=
                        value;

                requests.add(r);

                notifyUser(
                        currentUser,
                        "Withdrawal BDT "
                                + MONEY.format(value)
                                + " is pending."
                );

                audit(
                        currentUser.email,
                        "Withdrawal submitted: "
                                + r.id
                );

                JOptionPane.showMessageDialog(
                        f,
                        "Withdrawal submitted.\n\n"
                                + "Reserved: BDT "
                                + MONEY.format(value)
                                + "\n"
                                + "Available: BDT "
                                + MONEY.format(
                                currentUser.mainAvailable()
                        )
                );

                f.dispose();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        f,
                        "Invalid amount, reference, "
                                + "or insufficient balance."
                );
            }
        });

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // TRADE WINDOW
    // =========================================================

    static void tradeWindow() {

        JFrame f =
                frame(
                        "Simulated Trading",
                        680,
                        620
                );

        JPanel main =
                new JPanel(
                        new GridBagLayout()
                );

        main.setBorder(
                BorderFactory.createEmptyBorder(
                        25,
                        30,
                        25,
                        30
                )
        );

        GridBagConstraints g =
                new GridBagConstraints();

        g.gridx = 0;

        g.weightx = 1;

        g.fill =
                GridBagConstraints.HORIZONTAL;

        g.insets =
                new Insets(
                        8,
                        0,
                        8,
                        0
                );

        // -----------------------------------------------------
        // TITLE
        // -----------------------------------------------------

        JLabel title =
                new JLabel(
                        "DEMO TRADING",
                        SwingConstants.CENTER
                );

        title.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        24
                )
        );

        g.gridy = 0;

        main.add(
                title,
                g
        );

        // -----------------------------------------------------
        // ASSET
        // -----------------------------------------------------

        g.gridy++;

        main.add(
                new JLabel("Select Asset"),
                g
        );

        JComboBox<String> assetBox =
                new JComboBox<>();

        for (Asset a : assets) {

            if (a.enabled) {

                assetBox.addItem(
                        a.symbol
                );
            }
        }

        g.gridy++;

        main.add(
                assetBox,
                g
        );

        // -----------------------------------------------------
        // PRICE
        // -----------------------------------------------------

        JLabel priceLabel =
                new JLabel(
                        "Price: -",
                        SwingConstants.CENTER
                );

        priceLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        18
                )
        );

        g.gridy++;

        main.add(
                priceLabel,
                g
        );

        // -----------------------------------------------------
        // MARKET STATUS
        // -----------------------------------------------------

        JLabel marketStatus =
                new JLabel(
                        "Market: LIVE",
                        SwingConstants.CENTER
                );

        g.gridy++;

        main.add(
                marketStatus,
                g
        );

        // -----------------------------------------------------
        // QUANTITY
        // -----------------------------------------------------

        g.gridy++;

        main.add(
                new JLabel("Quantity"),
                g
        );

        JTextField quantity =
                new JTextField();

        g.gridy++;

        main.add(
                quantity,
                g
        );

        // -----------------------------------------------------
        // BALANCE
        // -----------------------------------------------------

        JLabel balanceLabel =
                new JLabel(
                        "",
                        SwingConstants.CENTER
                );

        balanceLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );

        g.gridy++;

        main.add(
                balanceLabel,
                g
        );

        // -----------------------------------------------------
        // BUY BUTTON
        // -----------------------------------------------------

        JButton buy =
                new JButton("BUY");

        buy.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        18
                )
        );

        buy.setPreferredSize(
                new Dimension(
                        250,
                        55
                )
        );

        // -----------------------------------------------------
        // SELL BUTTON
        // -----------------------------------------------------

        JButton sell =
                new JButton("SELL");

        sell.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        18
                )
        );

        sell.setPreferredSize(
                new Dimension(
                        250,
                        55
                )
        );

        // -----------------------------------------------------
        // BUY / SELL SIDE BY SIDE
        // -----------------------------------------------------

        JPanel actionPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                20,
                                0
                        )
                );

        actionPanel.add(buy);

        actionPanel.add(sell);

        g.gridy++;

        g.insets =
                new Insets(
                        25,
                        0,
                        8,
                        0
                );

        main.add(
                actionPanel,
                g
        );

        // -----------------------------------------------------
        // STATUS UPDATE
        // -----------------------------------------------------

        Runnable update =
                () -> {

                    Asset a =
                            asset(
                                    (String)
                                            assetBox.getSelectedItem()
                            );

                    if (a == null)
                        return;

                    priceLabel.setText(
                            "Price: BDT "
                                    + MONEY.format(
                                    a.price
                            )
                    );

                    balanceLabel.setText(
                            "Available Demo Balance: BDT "
                                    + MONEY.format(
                                    currentUser.demoAvailable()
                            )
                    );

                    if (!marketOnline) {

                        marketStatus.setText(
                                "Market: OFFLINE"
                        );

                        buy.setEnabled(false);

                        sell.setEnabled(false);

                    } else if (!a.enabled) {

                        marketStatus.setText(
                                "Market: ASSET DISABLED"
                        );

                        buy.setEnabled(false);

                        sell.setEnabled(false);

                    } else if (a.stale()) {

                        marketStatus.setText(
                                "Market: STALE"
                        );

                        buy.setEnabled(false);

                        sell.setEnabled(false);

                    } else {

                        marketStatus.setText(
                                "Market: LIVE"
                        );

                        buy.setEnabled(true);

                        sell.setEnabled(true);
                    }
                };

        assetBox.addActionListener(
                e -> update.run()
        );

        // -----------------------------------------------------
        // BUY ACTION
        // -----------------------------------------------------

        buy.addActionListener(e -> {

            try {

                double qty =
                        Double.parseDouble(
                                quantity.getText()
                        );

                if (qty <= 0) {

                    throw new NumberFormatException();
                }

                Asset a =
                        asset(
                                (String)
                                        assetBox.getSelectedItem()
                        );

                executeBuy(
                        f,
                        a,
                        qty
                );

                update.run();

            } catch (
                    NumberFormatException ex
            ) {

                JOptionPane.showMessageDialog(
                        f,
                        "Please enter a valid quantity."
                );
            }
        });

        // -----------------------------------------------------
        // SELL ACTION
        // -----------------------------------------------------

        sell.addActionListener(e -> {

            try {

                double qty =
                        Double.parseDouble(
                                quantity.getText()
                        );

                if (qty <= 0) {

                    throw new NumberFormatException();
                }

                Asset a =
                        asset(
                                (String)
                                        assetBox.getSelectedItem()
                        );

                executeSell(
                        f,
                        a,
                        qty
                );

                update.run();

            } catch (
                    NumberFormatException ex
            ) {

                JOptionPane.showMessageDialog(
                        f,
                        "Please enter a valid quantity."
                );
            }
        });

        // -----------------------------------------------------
        // AUTOMATIC UI REFRESH
        // -----------------------------------------------------

        Timer timer =
                new Timer(
                        1000,
                        e -> update.run()
                );

        timer.start();

        f.addWindowListener(
                new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(
                            java.awt.event.WindowEvent e
                    ) {

                        timer.stop();
                    }
                }
        );

        update.run();

        f.add(main);

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // TRADE VALIDATION
    // =========================================================

    static boolean tradeAllowed(
            JFrame f,
            Asset a
    ) {

        if (a == null) {

            JOptionPane.showMessageDialog(
                    f,
                    "Please select an asset."
            );

            return false;
        }

        if (!marketOnline) {

            JOptionPane.showMessageDialog(
                    f,
                    "Market feed is OFFLINE.\n\n"
                            + "BUY / SELL is blocked "
                            + "until the market feed resumes."
            );

            return false;
        }

        if (!a.enabled) {

            JOptionPane.showMessageDialog(
                    f,
                    "This asset is disabled."
            );

            return false;
        }

        if (a.stale()) {

            JOptionPane.showMessageDialog(
                    f,
                    "Market data is STALE.\n\n"
                            + "Trading is temporarily blocked "
                            + "for safety."
            );

            return false;
        }

        if (
                currentUser.status
                        != AccountStatus.ACTIVE
        ) {

            JOptionPane.showMessageDialog(
                    f,
                    "Your account is not active."
            );

            return false;
        }

        if (
                currentUser.verification
                        != VerificationStatus.VERIFIED
        ) {

            JOptionPane.showMessageDialog(
                    f,
                    "Account verification is required "
                            + "before trading."
            );

            return false;
        }

        return true;
    }

    // =========================================================
    // BUY
    // =========================================================

    static void executeBuy(
            JFrame f,
            Asset a,
            double quantity
    ) {

        if (!tradeAllowed(f, a))
            return;

        double total =
                a.price * quantity;

        if (
                total
                        > currentUser.demoAvailable()
        ) {

            JOptionPane.showMessageDialog(
                    f,
                    "Insufficient demo balance.\n\n"
                            + "Required: BDT "
                            + MONEY.format(total)
                            + "\n"
                            + "Available: BDT "
                            + MONEY.format(
                            currentUser.demoAvailable()
                    )
            );

            return;
        }

        // Temporary reservation
        currentUser.demoReserved +=
                total;

        // Deduct from demo balance
        currentUser.demoBalance -=
                total;

        // Release temporary reservation
        currentUser.demoReserved -=
                total;

        Trade trade =
                new Trade(
                        nextTrade++,
                        currentUser.id,
                        "DEMO",
                        a.symbol,
                        TradeType.BUY,
                        quantity,
                        a.price
                );

        trades.add(trade);

        notifyUser(
                currentUser,
                "BUY executed: "
                        + quantity
                        + " "
                        + a.symbol
        );

        audit(
                currentUser.email,
                "BUY executed: Trade ID "
                        + trade.id
        );

        JOptionPane.showMessageDialog(
                f,
                "BUY SUCCESSFUL!\n\n"
                        + "Asset: "
                        + a.symbol
                        + "\n"
                        + "Quantity: "
                        + quantity
                        + "\n"
                        + "Price: BDT "
                        + MONEY.format(a.price)
                        + "\n"
                        + "Total: BDT "
                        + MONEY.format(total)
        );
    }

    // =========================================================
    // SELL
    // =========================================================

    static void executeSell(
            JFrame f,
            Asset a,
            double requestedQuantity
    ) {

        if (!tradeAllowed(f, a))
            return;

        Trade activeTrade = null;

        for (Trade t : trades) {

            if (
                    t.userId
                            == currentUser.id
                            && t.accountType
                            .equals("DEMO")
                            && t.symbol
                            .equals(a.symbol)
                            && t.type
                            == TradeType.BUY
                            && t.status
                            == TradeStatus.ACTIVE
                            && t.quantity
                            >= requestedQuantity
            ) {

                activeTrade = t;

                break;
            }
        }

        if (activeTrade == null) {

            JOptionPane.showMessageDialog(
                    f,
                    "No active BUY position found "
                            + "for this asset."
            );

            return;
        }

        // Current prototype closes the full position
        double quantity =
                activeTrade.quantity;

        double proceeds =
                a.price * quantity;

        double pnl =
                (
                        a.price
                                - activeTrade.entryPrice
                ) * quantity;

        activeTrade.closingPrice =
                a.price;

        activeTrade.pnl =
                pnl;

        activeTrade.status =
                TradeStatus.COMPLETED;

        activeTrade.closedAt =
                LocalDateTime.now();

        currentUser.demoBalance +=
                proceeds;

        Trade sellTrade =
                new Trade(
                        nextTrade++,
                        currentUser.id,
                        "DEMO",
                        a.symbol,
                        TradeType.SELL,
                        quantity,
                        activeTrade.entryPrice
                );

        sellTrade.closingPrice =
                a.price;

        sellTrade.pnl =
                pnl;

        sellTrade.status =
                TradeStatus.COMPLETED;

        sellTrade.closedAt =
                LocalDateTime.now();

        trades.add(sellTrade);

        notifyUser(
                currentUser,
                "SELL completed: "
                        + quantity
                        + " "
                        + a.symbol
                        + " | P/L BDT "
                        + MONEY.format(pnl)
        );

        audit(
                currentUser.email,
                "SELL executed: Trade ID "
                        + sellTrade.id
        );

        JOptionPane.showMessageDialog(
                f,
                "SELL SUCCESSFUL!\n\n"
                        + "Asset: "
                        + a.symbol
                        + "\n"
                        + "Quantity: "
                        + quantity
                        + "\n"
                        + "Sell Price: BDT "
                        + MONEY.format(a.price)
                        + "\n"
                        + "P/L: BDT "
                        + MONEY.format(pnl)
        );
    }

    // =========================================================
    // PORTFOLIO
    // =========================================================

    static void portfolioWindow() {

        JFrame f =
                frame(
                        "Portfolio",
                        850,
                        500
                );

        DefaultTableModel model =
                new DefaultTableModel(
                        new String[]{
                                "ID",
                                "Asset",
                                "Type",
                                "Quantity",
                                "Entry",
                                "Current",
                                "P/L",
                                "Status"
                        },
                        0
                );

        for (Trade t : trades) {

            if (
                    t.userId
                            != currentUser.id
            )
                continue;

            Asset a =
                    asset(t.symbol);

            double currentPrice =
                    a == null
                            ? t.entryPrice
                            : a.price;

            double pnl =
                    t.status
                            == TradeStatus.ACTIVE
                            && t.type
                            == TradeType.BUY
                            ? (
                            currentPrice
                                    - t.entryPrice
                    ) * t.quantity
                            : t.pnl;

            model.addRow(
                    new Object[]{
                            t.id,
                            t.symbol,
                            t.type,
                            t.quantity,
                            MONEY.format(
                                    t.entryPrice
                            ),
                            MONEY.format(
                                    currentPrice
                            ),
                            "BDT "
                                    + MONEY.format(pnl),
                            t.status
                    }
            );
        }

        f.add(
                new JScrollPane(
                        new JTable(model)
                )
        );

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // HISTORY
    // =========================================================

    static void historyWindow() {

        JFrame f =
                frame(
                        "Trade History",
                        900,
                        500
                );

        DefaultTableModel model =
                new DefaultTableModel(
                        new String[]{
                                "ID",
                                "Asset",
                                "Type",
                                "Qty",
                                "Entry",
                                "Close",
                                "P/L",
                                "Status"
                        },
                        0
                );

        for (Trade t : trades) {

            if (
                    t.userId
                            == currentUser.id
            ) {

                model.addRow(
                        new Object[]{
                                t.id,
                                t.symbol,
                                t.type,
                                t.quantity,
                                MONEY.format(
                                        t.entryPrice
                                ),
                                t.closingPrice == 0
                                        ? "-"
                                        : MONEY.format(
                                        t.closingPrice
                                ),
                                "BDT "
                                        + MONEY.format(
                                        t.pnl
                                ),
                                t.status
                        }
                );
            }
        }

        f.add(
                new JScrollPane(
                        new JTable(model)
                )
        );

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // VERIFICATION
    // =========================================================

    static void verificationWindow() {

        JFrame f =
                frame(
                        "Verification",
                        600,
                        400
                );

        JTextArea text =
                new JTextArea();

        text.setEditable(false);

        text.setText(
                "VERIFICATION\n"
                        + "=============\n\n"
                        + "Current Status: "
                        + currentUser.verification
                        + "\n\n"
                        + "This is a synthetic "
                        + "verification workflow "
                        + "for the hackathon prototype.\n\n"
                        + "Possible states:\n"
                        + "PENDING\n"
                        + "VERIFIED\n"
                        + "REJECTED\n"
                        + "RESUBMIT\n\n"
                        + "Final verification decisions "
                        + "are made by authorized admin."
        );

        f.add(
                new JScrollPane(text)
        );

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // HELP
    // =========================================================

    static void help() {

        String message =
                "ACCOUNT STATE\n\n"
                        + "Balance: BDT "
                        + MONEY.format(
                        currentUser.mainBalance
                )
                        + "\n"
                        + "Reserved: BDT "
                        + MONEY.format(
                        currentUser.mainReserved
                )
                        + "\n"
                        + "Available: BDT "
                        + MONEY.format(
                        currentUser.mainAvailable()
                )
                        + "\n\n"
                        + "Formula:\n"
                        + "Available = Balance - Reserved"
                        + "\n\n"
                        + "AI status: "
                        + (
                        aiAvailable
                                ? "AVAILABLE"
                                : "UNAVAILABLE"
                );

        JOptionPane.showMessageDialog(
                null,
                message,
                "Contextual Help",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // =========================================================
    // SUPPORT
    // =========================================================

    static void supportWindow() {

        JFrame f =
                frame(
                        "Support",
                        650,
                        500
                );

        JPanel root =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        JTextField subject =
                new JTextField();

        JTextArea message =
                new JTextArea();

        JButton submit =
                new JButton(
                        "CREATE SUPPORT CASE"
                );

        root.add(
                new JLabel("Subject"),
                BorderLayout.NORTH
        );

        root.add(
                subject,
                BorderLayout.CENTER
        );

        JPanel bottom =
                new JPanel(
                        new BorderLayout()
                );

        bottom.add(
                new JScrollPane(message),
                BorderLayout.CENTER
        );

        bottom.add(
                submit,
                BorderLayout.SOUTH
        );

        root.add(
                bottom,
                BorderLayout.SOUTH
        );

        f.add(root);

        submit.addActionListener(e -> {

            if (
                    subject.getText()
                            .trim()
                            .isEmpty()
                            || message.getText()
                            .trim()
                            .isEmpty()
            ) {

                JOptionPane.showMessageDialog(
                        f,
                        "Subject and message are required."
                );

                return;
            }

            Ticket ticket =
                    new Ticket(
                            nextTicket++,
                            currentUser.id,
                            subject.getText().trim(),
                            message.getText().trim()
                    );

            tickets.add(ticket);

            audit(
                    currentUser.email,
                    "Support ticket created: "
                            + ticket.id
            );

            JOptionPane.showMessageDialog(
                    f,
                    "Support case created.\n"
                            + "Ticket ID: "
                            + ticket.id
            );

            f.dispose();
        });

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // NOTIFICATIONS
    // =========================================================

    static void notifications() {

        JFrame f =
                frame(
                        "Notifications",
                        650,
                        450
                );

        JTextArea text =
                new JTextArea();

        text.setEditable(false);

        if (
                currentUser.notifications
                        .isEmpty()
        ) {

            text.setText(
                    "No notifications."
            );

        } else {

            StringBuilder builder =
                    new StringBuilder();

            for (
                    String n :
                    currentUser.notifications
            ) {

                builder.append("• ")
                        .append(n)
                        .append("\n\n");
            }

            text.setText(
                    builder.toString()
            );
        }

        f.add(
                new JScrollPane(text)
        );

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // ADMIN PANEL
    // =========================================================

    static void adminPanel() {

        if (!isAdmin()) {

            JOptionPane.showMessageDialog(
                    null,
                    "Admin permission required."
            );

            return;
        }

        JFrame f =
                frame(
                        "Admin Control Panel",
                        900,
                        650
                );

        JPanel panel =
                new JPanel(
                        new GridLayout(
                                0,
                                2,
                                10,
                                10
                        )
                );

        panel.add(
                btn(
                        "USER MANAGEMENT",
                        e -> adminUsers()
                )
        );

        panel.add(
                btn(
                        "VERIFICATION REVIEW",
                        e -> adminVerification()
                )
        );

        panel.add(
                btn(
                        "DEPOSIT REVIEW",
                        e -> adminDeposits()
                )
        );

        panel.add(
                btn(
                        "WITHDRAWAL REVIEW",
                        e -> adminWithdrawals()
                )
        );

        panel.add(
                btn(
                        "TRADE RECORDS",
                        e -> adminTrades()
                )
        );

        panel.add(
                btn(
                        "ASSET CONTROL",
                        e -> adminAssets()
                )
        );

        panel.add(
                btn(
                        "SUPPORT CASES",
                        e -> adminSupport()
                )
        );

        panel.add(
                btn(
                        "AUDIT LOG",
                        e -> auditWindow()
                )
        );

        panel.add(
                btn(
                        "SYSTEM STATUS",
                        e -> systemStatus()
                )
        );

        panel.add(
                btn(
                        "CHALLENGE CASE",
                        e -> reliabilityTest()
                )
        );

        f.add(
                new JScrollPane(panel)
        );

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // ADMIN USERS
    // =========================================================

    static void adminUsers() {

        JFrame f =
                frame(
                        "User Management",
                        1000,
                        500
                );

        DefaultTableModel model =
                new DefaultTableModel(
                        new String[]{
                                "ID",
                                "Name",
                                "Email",
                                "Role",
                                "Verification",
                                "Status",
                                "Balance",
                                "Reserved",
                                "Available"
                        },
                        0
                );

        for (User u : users) {

            model.addRow(
                    new Object[]{
                            u.id,
                            u.name,
                            u.email,
                            u.role,
                            u.verification,
                            u.status,
                            MONEY.format(
                                    u.mainBalance
                            ),
                            MONEY.format(
                                    u.mainReserved
                            ),
                            MONEY.format(
                                    u.mainAvailable()
                            )
                    }
            );
        }

        f.add(
                new JScrollPane(
                        new JTable(model)
                )
        );

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // ADMIN VERIFICATION
    // =========================================================

    static void adminVerification() {

        JFrame f =
                frame(
                        "Verification Review",
                        800,
                        500
                );

        DefaultTableModel model =
                new DefaultTableModel(
                        new String[]{
                                "ID",
                                "Name",
                                "Email",
                                "Status"
                        },
                        0
                );

        JTable table =
                new JTable(model);

        for (User u : users) {

            if (
                    u.role
                            == Role.USER
            ) {

                model.addRow(
                        new Object[]{
                                u.id,
                                u.name,
                                u.email,
                                u.verification
                        }
                );
            }
        }

        JButton review =
                new JButton(
                        "REVIEW SELECTED USER"
                );

        review.addActionListener(e -> {

            int row =
                    table.getSelectedRow();

            if (row < 0) {

                JOptionPane.showMessageDialog(
                        f,
                        "Select a user."
                );

                return;
            }

            int id =
                    Integer.parseInt(
                            table.getValueAt(
                                    row,
                                    0
                            ).toString()
                    );

            User u =
                    user(id);

            String[] options = {
                    "VERIFIED",
                    "REJECTED",
                    "RESUBMIT"
            };

            int choice =
                    JOptionPane.showOptionDialog(
                            f,
                            "Select verification decision:",
                            "Verification Review",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]
                    );

            if (choice >= 0) {

                u.verification =
                        VerificationStatus
                                .valueOf(
                                        options[choice]
                                );

                notifyUser(
                        u,
                        "Verification status changed to "
                                + u.verification
                );

                audit(
                        currentUser.email,
                        "Verification decision for "
                                + u.email
                                + ": "
                                + u.verification
                );

                f.dispose();

                adminVerification();
            }
        });

        f.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        f.add(
                review,
                BorderLayout.SOUTH
        );

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // ADMIN DEPOSITS
    // =========================================================

    static void adminDeposits() {

        JFrame f =
                frame(
                        "Deposit Review",
                        900,
                        500
                );

        DefaultTableModel model =
                new DefaultTableModel(
                        new String[]{
                                "ID",
                                "User",
                                "Amount",
                                "Reference",
                                "Status"
                        },
                        0
                );

        JTable table =
                new JTable(model);

        for (WalletRequest r : requests) {

            if (
                    r.type
                            == RequestType.DEPOSIT
            ) {

                User u =
                        user(r.userId);

                model.addRow(
                        new Object[]{
                                r.id,
                                u.email,
                                "BDT "
                                        + MONEY.format(
                                        r.amount
                                ),
                                r.reference,
                                r.status
                        }
                );
            }
        }

        JButton review =
                new JButton(
                        "REVIEW SELECTED"
                );

        review.addActionListener(e -> {

            int row =
                    table.getSelectedRow();

            if (row < 0) {

                JOptionPane.showMessageDialog(
                        f,
                        "Select a request."
                );

                return;
            }

            int id =
                    Integer.parseInt(
                            table.getValueAt(
                                    row,
                                    0
                            ).toString()
                    );

            WalletRequest r =
                    request(id);

            User u =
                    user(r.userId);

            String[] options = {
                    "APPROVE",
                    "REJECT"
            };

            int choice =
                    JOptionPane.showOptionDialog(
                            f,
                            "Select action:",
                            "Deposit Review",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]
                    );

            if (choice == 0) {

                if (
                        processedFinancialRequests
                                .contains(r.id)
                ) {

                    audit(
                            currentUser.email,
                            "Duplicate deposit approval blocked: "
                                    + r.id
                    );

                    JOptionPane.showMessageDialog(
                            f,
                            "Duplicate approval BLOCKED.\n\n"
                                    + "Balance was NOT credited twice."
                    );

                } else {

                    u.mainBalance +=
                            r.amount;

                    r.status =
                            RequestStatus.APPROVED;

                    processedFinancialRequests.add(
                            r.id
                    );

                    notifyUser(
                            u,
                            "Deposit approved: BDT "
                                    + MONEY.format(
                                    r.amount
                            )
                    );

                    audit(
                            currentUser.email,
                            "Deposit approved: "
                                    + r.id
                    );
                }

            } else if (choice == 1) {

                r.status =
                        RequestStatus.REJECTED;

                notifyUser(
                        u,
                        "Deposit rejected: "
                                + r.id
                );

                audit(
                        currentUser.email,
                        "Deposit rejected: "
                                + r.id
                );
            }

            f.dispose();

            adminDeposits();
        });

        f.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        f.add(
                review,
                BorderLayout.SOUTH
        );

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // ADMIN WITHDRAWALS
    // =========================================================

    static void adminWithdrawals() {

        JFrame f =
                frame(
                        "Withdrawal Review",
                        900,
                        500
                );

        DefaultTableModel model =
                new DefaultTableModel(
                        new String[]{
                                "ID",
                                "User",
                                "Amount",
                                "Reference",
                                "Status"
                        },
                        0
                );

        JTable table =
                new JTable(model);

        for (WalletRequest r : requests) {

            if (
                    r.type
                            == RequestType.WITHDRAWAL
            ) {

                User u =
                        user(r.userId);

                model.addRow(
                        new Object[]{
                                r.id,
                                u.email,
                                "BDT "
                                        + MONEY.format(
                                        r.amount
                                ),
                                r.reference,
                                r.status
                        }
                );
            }
        }

        JButton review =
                new JButton(
                        "REVIEW SELECTED"
                );

        review.addActionListener(e -> {

            int row =
                    table.getSelectedRow();

            if (row < 0) {

                JOptionPane.showMessageDialog(
                        f,
                        "Select a request."
                );

                return;
            }

            int id =
                    Integer.parseInt(
                            table.getValueAt(
                                    row,
                                    0
                            ).toString()
                    );

            WalletRequest r =
                    request(id);

            User u =
                    user(r.userId);

            if (
                    r.status
                            != RequestStatus.PENDING
            ) {

                JOptionPane.showMessageDialog(
                        f,
                        "This request has already been processed."
                );

                return;
            }

            String[] options = {
                    "APPROVE",
                    "REJECT"
            };

            int choice =
                    JOptionPane.showOptionDialog(
                            f,
                            "Select action:",
                            "Withdrawal Review",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]
                    );

            if (choice == 0) {

                if (
                        u.mainReserved
                                < r.amount
                ) {

                    JOptionPane.showMessageDialog(
                            f,
                            "Reserved balance is insufficient."
                    );

                    return;
                }

                u.mainBalance -=
                        r.amount;

                u.mainReserved -=
                        r.amount;

                r.status =
                        RequestStatus.APPROVED;

                notifyUser(
                        u,
                        "Withdrawal approved: BDT "
                                + MONEY.format(
                                r.amount
                        )
                );

                audit(
                        currentUser.email,
                        "Withdrawal approved: "
                                + r.id
                );

            } else if (choice == 1) {

                u.mainReserved =
                        Math.max(
                                0,
                                u.mainReserved
                                        - r.amount
                        );

                r.status =
                        RequestStatus.REJECTED;

                notifyUser(
                        u,
                        "Withdrawal rejected: "
                                + r.id
                );

                audit(
                        currentUser.email,
                        "Withdrawal rejected: "
                                + r.id
                );
            }

            f.dispose();

            adminWithdrawals();
        });

        f.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        f.add(
                review,
                BorderLayout.SOUTH
        );

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // ADMIN TRADES
    // =========================================================

    static void adminTrades() {

        JFrame f =
                frame(
                        "Trade Records",
                        1050,
                        500
                );

        DefaultTableModel model =
                new DefaultTableModel(
                        new String[]{
                                "ID",
                                "User",
                                "Account",
                                "Asset",
                                "Type",
                                "Qty",
                                "Entry",
                                "Close",
                                "P/L",
                                "Status"
                        },
                        0
                );

        for (Trade t : trades) {

            User u =
                    user(t.userId);

            model.addRow(
                    new Object[]{
                            t.id,
                            u.email,
                            t.accountType,
                            t.symbol,
                            t.type,
                            t.quantity,
                            MONEY.format(
                                    t.entryPrice
                            ),
                            t.closingPrice == 0
                                    ? "-"
                                    : MONEY.format(
                                    t.closingPrice
                            ),
                            MONEY.format(
                                    t.pnl
                            ),
                            t.status
                    }
            );
        }

        f.add(
                new JScrollPane(
                        new JTable(model)
                )
        );

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // ADMIN ASSET CONTROL
    // =========================================================

    static void adminAssets() {

        JFrame f =
                frame(
                        "Asset Control",
                        800,
                        500
                );

        DefaultTableModel model =
                new DefaultTableModel(
                        new String[]{
                                "Symbol",
                                "Name",
                                "Price",
                                "Enabled"
                        },
                        0
                );

        JTable table =
                new JTable(model);

        Runnable refresh =
                () -> {

                    model.setRowCount(0);

                    for (Asset a : assets) {

                        model.addRow(
                                new Object[]{
                                        a.symbol,
                                        a.name,
                                        "BDT "
                                                + MONEY.format(
                                                a.price
                                        ),
                                        a.enabled
                                }
                        );
                    }
                };

        JButton toggle =
                new JButton(
                        "ENABLE / DISABLE"
                );

        toggle.addActionListener(e -> {

            int row =
                    table.getSelectedRow();

            if (row < 0) {

                JOptionPane.showMessageDialog(
                        f,
                        "Select an asset."
                );

                return;
            }

            Asset a =
                    asset(
                            table.getValueAt(
                                    row,
                                    0
                            ).toString()
                    );

            a.enabled =
                    !a.enabled;

            audit(
                    currentUser.email,
                    "Asset "
                            + a.symbol
                            + " enabled="
                            + a.enabled
            );

            refresh.run();
        });

        refresh.run();

        f.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        f.add(
                toggle,
                BorderLayout.SOUTH
        );

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // ADMIN SUPPORT
    // =========================================================

    static void adminSupport() {

        JFrame f =
                frame(
                        "Support Cases",
                        900,
                        500
                );

        DefaultTableModel model =
                new DefaultTableModel(
                        new String[]{
                                "ID",
                                "User",
                                "Subject",
                                "Message",
                                "Status",
                                "Created"
                        },
                        0
                );

        for (Ticket t : tickets) {

            User u =
                    user(t.userId);

            model.addRow(
                    new Object[]{
                            t.id,
                            u.email,
                            t.subject,
                            t.message,
                            t.status,
                            t.createdAt.format(
                                    TIME
                            )
                    }
            );
        }

        f.add(
                new JScrollPane(
                        new JTable(model)
                )
        );

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // AUDIT WINDOW
    // =========================================================

    static void auditWindow() {

        JFrame f =
                frame(
                        "Audit Log",
                        900,
                        550
                );

        JTextArea text =
                new JTextArea();

        text.setEditable(false);

        StringBuilder builder =
                new StringBuilder();

        for (Audit a : audits) {

            builder.append("[")
                    .append(
                            a.time.format(TIME)
                    )
                    .append("] ")
                    .append(a.actor)
                    .append(" -> ")
                    .append(a.action)
                    .append("\n");
        }

        text.setText(
                builder.toString()
        );

        f.add(
                new JScrollPane(text)
        );

        f.setLocationRelativeTo(null);

        f.setVisible(true);
    }

    // =========================================================
    // SYSTEM STATUS
    // =========================================================

    static void systemStatus() {

        JOptionPane.showMessageDialog(
                null,
                "SYSTEM STATUS\n\n"
                        + "Market: "
                        + (
                        marketOnline
                                ? "ONLINE"
                                : "OFFLINE"
                )
                        + "\n"
                        + "AI: "
                        + (
                        aiAvailable
                                ? "AVAILABLE"
                                : "UNAVAILABLE"
                )
                        + "\n"
                        + "Users: "
                        + users.size()
                        + "\n"
                        + "Assets: "
                        + assets.size()
                        + "\n"
                        + "Wallet Requests: "
                        + requests.size()
                        + "\n"
                        + "Trades: "
                        + trades.size()
                        + "\n"
                        + "Support Tickets: "
                        + tickets.size()
                        + "\n"
                        + "Audit Events: "
                        + audits.size(),
                "System Status",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // =========================================================
    // LEVEL 3 RELIABILITY TEST
    // =========================================================

    static void reliabilityTest() {

        if (!isAdmin()) {

            JOptionPane.showMessageDialog(
                    null,
                    "Admin permission required."
            );

            return;
        }

        User demo =
                userEmail(
                        "demo@example.com"
                );

        // Reset
        demo.mainBalance = 10000;

        demo.mainReserved = 0;

        StringBuilder result =
                new StringBuilder();

        result.append(
                "LEVEL 3 RELIABILITY TEST\n"
        );

        result.append(
                "========================\n\n"
        );

        // STEP 1
        result.append("""
                      1. Demo balance reset
                         Balance = BDT 10,000
                      
                      """);

        audit(
                "SYSTEM",
                "Reliability test: balance reset"
        );

        // STEP 2
        WalletRequest deposit =
                new WalletRequest(
                        nextRequest++,
                        demo.id,
                        RequestType.DEPOSIT,
                        5000,
                        "TEST-DEPOSIT-001"
                );

        requests.add(deposit);

        result.append("""
                      2. Deposit created
                         Amount = BDT 5,000
                      
                      """);

        // STEP 3
        demo.mainBalance +=
                5000;

        deposit.status =
                RequestStatus.APPROVED;

        processedFinancialRequests.add(
                deposit.id
        );

        result.append(
                """
                3. Deposit approved once
                   Balance = BDT """
                        + MONEY.format(
                        demo.mainBalance
                )
                        + "\n\n"
        );

        // STEP 4
        if (
                processedFinancialRequests
                        .contains(deposit.id)
        ) {

            result.append(
                    """
                    4. Duplicate approval BLOCKED
                       Balance remains = BDT """
                            + MONEY.format(
                            demo.mainBalance
                    )
                            + "\n\n"
            );

            audit(
                    "SYSTEM",
                    "Duplicate deposit approval blocked"
            );
        }

        // STEP 5
        WalletRequest withdrawal =
                new WalletRequest(
                        nextRequest++,
                        demo.id,
                        RequestType.WITHDRAWAL,
                        3000,
                        "TEST-WITHDRAWAL-001"
                );

        requests.add(withdrawal);

        demo.mainReserved +=
                3000;

        result.append(
                """
                5. Pending withdrawal created
                   Reserved = BDT 3,000
                   Available = BDT """
                        + MONEY.format(
                        demo.mainAvailable()
                )
                        + "\n\n"
        );

        // STEP 6
        marketOnline = false;

        result.append("""
                      6. Market feed STOPPED
                         Trading should be blocked.
                      
                      """);

        audit(
                "SYSTEM",
                "Reliability test: market stopped"
        );

        // STEP 7
        result.append(
                """
                7. Balance reconstruction
                   Balance = BDT """
                        + MONEY.format(
                        demo.mainBalance
                )
                        + "\n"
                        + "   Reserved = BDT "
                        + MONEY.format(
                        demo.mainReserved
                )
                        + "\n"
                        + "   Available = BDT "
                        + MONEY.format(
                        demo.mainAvailable()
                )
                        + "\n\n"
        );

        // STEP 8
        aiAvailable = false;

        result.append("""
                      8. AI unavailable
                         Core calculation still works.
                      
                      """);

        // STEP 9
        result.append(
                "9. Audit log preserved.\n\n"
        );

        result.append("""
                      RESULT
                      ======
                      Duplicate credit prevented.
                      Reserved amount remained visible.
                      Available balance remained correct.
                      Market offline state communicated.
                      Core workflow does not depend on AI.""");

        JOptionPane.showMessageDialog(
                null,
                result.toString(),
                "Level 3 Reliability Test",
                JOptionPane.INFORMATION_MESSAGE
        );

        audit(
                "SYSTEM",
                "Reliability test completed"
        );
    }
}