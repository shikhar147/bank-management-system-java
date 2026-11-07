package com.bank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalTime;
import java.util.List;

public class BankGUI extends JFrame {

    // ---------------- Settings ----------------
    /** Passcode needed for the admin links on the login screen. Change it here. */
    static final String ADMIN_PASSCODE = "admin123";
    /** Automatic logout after this many milliseconds of no activity. */
    static final int IDLE_MS = 120_000;

    // ---------------- Colour palette ----------------
    static final Color BG_TOP    = new Color(0x0F172A);
    static final Color BG_BOTTOM = new Color(0x312E81);
    static final Color CARD      = new Color(0x1E293B);
    static final Color FIELD     = new Color(0x0F172A);
    static final Color BORDER    = new Color(0x334155);
    static final Color ACCENT    = new Color(0x6366F1);
    static final Color ACCENT_HI = new Color(0x818CF8);
    static final Color PURPLE    = new Color(0x8B5CF6);
    static final Color TEXT      = new Color(0xF1F5F9);
    static final Color MUTED     = new Color(0x94A3B8);
    static final Color SUCCESS   = new Color(0x10B981);
    static final Color AMBER     = new Color(0xF59E0B);
    static final Color DANGER    = new Color(0xEF4444);
    static final Color SLATE     = new Color(0x475569);
    static final Color SLATE_HI  = new Color(0x64748B);
    static final Color CREDIT    = new Color(0x34D399);
    static final Color DEBIT     = new Color(0xF87171);
    static final Color DIVIDER   = new Color(0x2B3A52);

    static final String FONT = "Segoe UI";

    // message kinds
    static final int INFO = 0, ERROR = 1, OK = 2;

    // ---------------- State ----------------
    private final Bank bank = new Bank();
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final JTextField loginAcc = new JTextField();
    private final JPasswordField loginPin = new JPasswordField();

    private Account current;
    private final Avatar avatar = new Avatar();
    private final JLabel header = label("", 24, Font.BOLD, TEXT);
    private final JLabel subHeader = label("", 13, Font.PLAIN, MUTED);
    private final JLabel balanceLbl = label("", 34, Font.BOLD, Color.WHITE);
    private final JLabel infoLbl = label("", 12, Font.PLAIN, new Color(0xE0E7FF));
    private final JLabel moneyIn = label("", 15, Font.BOLD, CREDIT);
    private final JLabel moneyOut = label("", 15, Font.BOLD, DEBIT);
    private final JPanel txList = new JPanel();
    private JScrollPane txScroll;
    private final JPanel actions = new JPanel(new GridLayout(0, 2, 12, 12));
    private final Timer idleTimer = new Timer(IDLE_MS, null);

    public BankGUI() {
        super("Bank Management System");
        bank.load();

        styleField(loginAcc);
        styleField(loginPin);
        actions.setOpaque(false);
        txList.setOpaque(false);
        txList.setLayout(new BoxLayout(txList, BoxLayout.Y_AXIS));

        root.add(buildHomePanel(), "home");
        root.add(buildAccountPanel(), "account");
        add(root);

        // auto-logout after inactivity
        idleTimer.setRepeats(false);
        idleTimer.addActionListener(e -> onIdle());
        Toolkit.getDefaultToolkit().addAWTEventListener(ev -> {
            if (current != null) idleTimer.restart();
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);

        setSize(1000, 640);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    // =====================================================
    //  HOME SCREEN
    // =====================================================
    private JPanel buildHomePanel() {
        GradientPanel bg = new GradientPanel(new GridLayout(1, 2));
        bg.add(buildHero());

        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        right.add(buildLoginCard());
        bg.add(right);
        return bg;
    }

    private JPanel buildHero() {
        JPanel hero = new JPanel(new GridBagLayout());
        hero.setOpaque(false);

        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBorder(new EmptyBorder(0, 56, 0, 16));

        col.add(left(label("BANK MANAGEMENT SYSTEM", 12, Font.BOLD, ACCENT_HI)));
        col.add(Box.createVerticalStrut(14));
        col.add(left(label("<html><body style='width:340px'>Banking that feels effortless.</body></html>",
                36, Font.BOLD, TEXT)));
        col.add(Box.createVerticalStrut(14));
        col.add(left(label("<html><body style='width:320px'>Manage your money, send transfers and "
                + "watch your savings grow, all in one place.</body></html>", 15, Font.PLAIN, MUTED)));
        col.add(Box.createVerticalStrut(30));
        col.add(featureRow(CREDIT, "Instant transfers between accounts"));
        col.add(Box.createVerticalStrut(10));
        col.add(featureRow(AMBER, "4% yearly interest on savings"));
        col.add(Box.createVerticalStrut(10));
        col.add(featureRow(ACCENT_HI, "PIN protected with auto-logout"));

        hero.add(col);
        return hero;
    }

    private JPanel featureRow(Color dot, String text) {
        JPanel r = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        r.setOpaque(false);
        r.setAlignmentX(LEFT_ALIGNMENT);
        r.add(new Dot(dot));
        r.add(label(text, 14, Font.PLAIN, TEXT));
        return r;
    }

    private static <T extends JComponent> T left(T c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        return c;
    }

    private RoundedPanel buildLoginCard() {
        RoundedPanel card = new RoundedPanel(new GridBagLayout(), 28, CARD).outline(BORDER);
        card.setBorder(new EmptyBorder(30, 34, 26, 34));
        card.setPreferredSize(new Dimension(400, 520));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        int row = 0;
        c.gridy = row++; c.insets = new Insets(0, 0, 4, 0);
        card.add(label("SECURE LOGIN", 11, Font.BOLD, ACCENT_HI), c);

        c.gridy = row++; c.insets = new Insets(0, 0, 4, 0);
        card.add(label("Welcome back", 28, Font.BOLD, TEXT), c);

        c.gridy = row++; c.insets = new Insets(0, 0, 22, 0);
        card.add(label("Sign in to manage your account", 13, Font.PLAIN, MUTED), c);

        c.gridy = row++; c.insets = new Insets(0, 0, 6, 0);
        card.add(label("Account number", 12, Font.BOLD, MUTED), c);
        c.gridy = row++; c.insets = new Insets(0, 0, 14, 0);
        card.add(loginAcc, c);

        c.gridy = row++; c.insets = new Insets(0, 0, 6, 0);
        card.add(label("PIN", 12, Font.BOLD, MUTED), c);
        c.gridy = row++; c.insets = new Insets(0, 0, 22, 0);
        card.add(loginPin, c);

        ModernButton loginBtn = new ModernButton("Login", ACCENT, ACCENT_HI);
        loginBtn.addActionListener(e -> doLogin());
        loginAcc.addActionListener(e -> loginPin.requestFocusInWindow()); // Enter -> PIN
        loginPin.addActionListener(e -> doLogin());                       // Enter -> login
        c.gridy = row++; c.insets = new Insets(0, 0, 10, 0);
        card.add(loginBtn, c);

        ModernButton openBtn = new ModernButton("Open New Account", SLATE, SLATE_HI);
        openBtn.addActionListener(e -> doOpenAccount());
        c.gridy = row++; c.insets = new Insets(0, 0, 18, 0);
        card.add(openBtn, c);

        JPanel admin = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        admin.setOpaque(false);
        admin.add(linkButton("View all accounts", this::doViewAll));
        admin.add(label("|", 13, Font.PLAIN, BORDER));
        admin.add(linkButton("Close an account", this::doCloseAccount));
        c.gridy = row; c.insets = new Insets(0, 0, 0, 0);
        card.add(admin, c);
        return card;
    }

    private void doLogin() {
        try {
            int no = Integer.parseInt(loginAcc.getText().trim());
            Account acc = bank.login(no, new String(loginPin.getPassword()).trim());
            if (acc == null) {
                loginPin.setText("");
                error("Invalid account number or PIN.");
                return;
            }
            current = acc;
            loginPin.setText("");
            refreshAccountView();
            cards.show(root, "account");
            idleTimer.restart();
        } catch (NumberFormatException ex) {
            error("Account number must be a number.");
        }
    }

    private void logout() {
        idleTimer.stop();
        current = null;
        loginAcc.setText("");
        loginPin.setText("");
        cards.show(root, "home");
    }

    private boolean dialogOpen() {
        for (Window w : Window.getWindows()) {
            if (w instanceof ModernDialog && w.isVisible()) return true;
        }
        return false;
    }

    private void onIdle() {
        if (current == null) return;
        if (dialogOpen()) {
            idleTimer.restart();
            return;
        }
        logout();
        message("Session expired", "You were logged out after 2 minutes of inactivity.",
                INFO, "OK", false);
    }

    private boolean adminAuth() {
        String p = prompt("Admin access", "Enter the admin passcode", "Passcode", true,
                "Unlock", ACCENT);
        if (p == null) return false;
        if (!ADMIN_PASSCODE.equals(p)) {
            error("Incorrect admin passcode.");
            return false;
        }
        return true;
    }

    private void doOpenAccount() {
        JTextField name = new JTextField();
        JTextField phone = new JTextField();
        JPasswordField pin = new JPasswordField();
        JTextField opening = new JTextField();
        styleField(name);
        styleField(phone);
        styleField(pin);
        styleField(opening);

        // Savings / Current segmented switch
        boolean[] savings = {true};
        ModernButton sav = new ModernButton("Savings", ACCENT, ACCENT_HI);
        ModernButton cur = new ModernButton("Current", BORDER, SLATE);
        sav.setBorder(new EmptyBorder(9, 14, 9, 14));
        cur.setBorder(new EmptyBorder(9, 14, 9, 14));
        Runnable sync = () -> {
            sav.setColors(savings[0] ? ACCENT : BORDER, savings[0] ? ACCENT_HI : SLATE);
            cur.setColors(!savings[0] ? ACCENT : BORDER, !savings[0] ? ACCENT_HI : SLATE);
        };
        sav.addActionListener(e -> { savings[0] = true; sync.run(); });
        cur.addActionListener(e -> { savings[0] = false; sync.run(); });
        JPanel typeRow = new JPanel(new GridLayout(1, 2, 8, 0));
        typeRow.setOpaque(false);
        typeRow.add(sav);
        typeRow.add(cur);

        ModernDialog d = new ModernDialog(this, "Open New Account", "Takes less than a minute");
        d.addField("Full name", name);
        d.addField("Phone (10 digits)", phone);
        d.addField("4-digit PIN", pin);
        d.addField("Account type", typeRow);
        d.addField("Opening balance (\u20B9)", opening);
        d.setButtons("Create Account", ACCENT, ACCENT_HI, true);

        while (d.showDialog()) {
            String pinText = new String(pin.getPassword()).trim();
            if (name.getText().trim().isEmpty() || !phone.getText().trim().matches("\\d{10}")) {
                error("Name is required and phone must be 10 digits.");
                continue;
            }
            if (!pinText.matches("\\d{4}")) {
                error("PIN must be exactly 4 digits.");
                continue;
            }
            try {
                double amt = Double.parseDouble(opening.getText().trim());
                Account acc = bank.openAccount(savings[0], name.getText().trim(),
                        phone.getText().trim(), pinText, amt);
                bank.save();
                success("Account created",
                        "Your account number is " + acc.getAccountNumber()
                                + ". Use it with your PIN to log in.");
                break;
            } catch (NumberFormatException ex) {
                error("Please enter a valid opening balance.");
            } catch (IllegalArgumentException ex) {
                error(ex.getMessage());
            }
        }
        d.dispose();
    }

    private void doViewAll() {
        if (!adminAuth()) return;
        if (bank.getAllAccounts().isEmpty()) {
            message("All Accounts", "No accounts yet.", INFO, "OK", false);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Account a : bank.getAllAccounts()) sb.append(a).append("\n");
        sb.append(String.format("%nTotal deposits held: %.2f", bank.totalDeposits()));

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setBackground(FIELD);
        area.setForeground(TEXT);
        area.setBorder(new EmptyBorder(12, 14, 12, 14));
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane sp = styledScroll(area, FIELD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        sp.setPreferredSize(new Dimension(820, 280));

        ModernDialog d = new ModernDialog(this, "All Accounts", "Admin overview");
        d.addComponent(sp);
        d.setButtons("Close", ACCENT, ACCENT_HI, false);
        d.showDialog();
        d.dispose();
    }

    private void doCloseAccount() {
        if (!adminAuth()) return;
        String s = prompt("Close Account", "Admin action", "Account number", false,
                "Continue", DANGER);
        if (s == null) return;
        try {
            int no = Integer.parseInt(s);
            Account a = bank.find(no);
            if (a == null) {
                error("Account " + no + " does not exist.");
                return;
            }
            if (!message("Close account?", "Account " + no + " (" + a.getHolderName()
                    + ") will be permanently removed.", ERROR, "Close account", true)) return;
            bank.closeAccount(no);
            bank.save();
            success("Account closed", "Account " + no + " has been removed.");
        } catch (NumberFormatException ex) {
            error("Please enter a valid account number.");
        }
    }

    // =====================================================
    //  ACCOUNT SCREEN
    // =====================================================
    private JPanel buildAccountPanel() {
        GradientPanel p = new GradientPanel(new BorderLayout(0, 20));
        p.setBorder(new EmptyBorder(24, 28, 24, 28));

        // ----- header -----
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(header);
        titles.add(subHeader);

        JPanel avatarWrap = new JPanel(new GridBagLayout());
        avatarWrap.setOpaque(false);
        avatarWrap.add(avatar);

        JPanel who = new JPanel(new BorderLayout(14, 0));
        who.setOpaque(false);
        who.add(avatarWrap, BorderLayout.WEST);
        who.add(titles, BorderLayout.CENTER);

        ModernButton logout = new ModernButton("Logout", SLATE, SLATE_HI);
        logout.addActionListener(e -> logout());
        JPanel logoutWrap = new JPanel(new GridBagLayout());
        logoutWrap.setOpaque(false);
        logoutWrap.add(logout);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(who, BorderLayout.WEST);
        top.add(logoutWrap, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        // ----- left column: balance card, money in/out, action tiles -----
        RoundedPanel balCard = new RoundedPanel(new GridLayout(0, 1, 0, 4), 24, ACCENT, PURPLE);
        balCard.setBorder(new EmptyBorder(20, 24, 20, 24));
        balCard.add(label("Available Balance", 13, Font.PLAIN, new Color(0xE0E7FF)));
        balCard.add(balanceLbl);
        balCard.add(infoLbl);

        JPanel stats = new JPanel(new GridLayout(1, 2, 12, 0));
        stats.setOpaque(false);
        stats.add(statCard("Money in", moneyIn));
        stats.add(statCard("Money out", moneyOut));

        JPanel topLeft = new JPanel(new BorderLayout(0, 14));
        topLeft.setOpaque(false);
        topLeft.add(balCard, BorderLayout.NORTH);
        topLeft.add(stats, BorderLayout.SOUTH);

        JPanel leftCol = new JPanel(new BorderLayout(0, 14));
        leftCol.setOpaque(false);
        leftCol.setPreferredSize(new Dimension(340, 0));
        leftCol.add(topLeft, BorderLayout.NORTH);
        leftCol.add(actions, BorderLayout.CENTER);

        // ----- right column: transactions -----
        JPanel txWrap = new JPanel(new BorderLayout());
        txWrap.setOpaque(false);
        txWrap.add(txList, BorderLayout.NORTH);
        txScroll = styledScroll(txWrap, CARD);
        txScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        RoundedPanel right = new RoundedPanel(new BorderLayout(0, 10), 24, CARD);
        right.setBorder(new EmptyBorder(18, 20, 18, 20));
        right.add(label("Recent Transactions", 16, Font.BOLD, TEXT), BorderLayout.NORTH);
        right.add(txScroll, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(18, 0));
        center.setOpaque(false);
        center.add(leftCol, BorderLayout.WEST);
        center.add(right, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);

        return p;
    }

    private RoundedPanel statCard(String title, JLabel value) {
        RoundedPanel p = new RoundedPanel(new GridLayout(2, 1, 0, 2), 18, CARD);
        p.setBorder(new EmptyBorder(12, 16, 12, 16));
        p.add(label(title, 12, Font.PLAIN, MUTED));
        p.add(value);
        return p;
    }

    private void rebuildActions() {
        actions.removeAll();
        actions.add(actionBtn("Deposit", SUCCESS, new Color(0x34D399), this::doDeposit));
        actions.add(actionBtn("Withdraw", AMBER, new Color(0xFBBF24), this::doWithdraw));
        actions.add(actionBtn("Transfer", ACCENT, ACCENT_HI, this::doTransfer));
        actions.add(actionBtn("Change PIN", SLATE, SLATE_HI, this::doChangePin));
        if (current instanceof SavingsAccount) {
            actions.add(actionBtn("Credit Interest", PURPLE, new Color(0xA78BFA), this::doInterest));
        }
        actions.revalidate();
        actions.repaint();
    }

    private ModernButton actionBtn(String text, Color base, Color hover, Runnable r) {
        ModernButton b = new ModernButton(text, base, hover);
        b.setBorder(new EmptyBorder(18, 14, 18, 14));
        b.setFont(new Font(FONT, Font.BOLD, 15));
        b.addActionListener(e -> r.run());
        return b;
    }

    private String greeting() {
        int h = LocalTime.now().getHour();
        return h < 12 ? "Good morning" : h < 17 ? "Good afternoon" : "Good evening";
    }

    private void refreshAccountView() {
        avatar.setHolder(current.getHolderName());
        header.setText(greeting() + ", " + current.getHolderName());
        subHeader.setText(current.getType() + "  \u2022  A/C " + current.getAccountNumber());
        balanceLbl.setText(String.format("\u20B9 %,.2f", current.getBalance()));
        balanceLbl.setForeground(current.getBalance() < 0 ? new Color(0xFECACA) : Color.WHITE);
        infoLbl.setText("Phone: " + current.getPhone());

        double in = 0, out = 0;
        txList.removeAll();
        List<Transaction> list = current.getTransactions();
        for (int i = list.size() - 1; i >= 0; i--) {          // newest first
            Txn t = new Txn(list.get(i));
            if (t.credit) in += t.amount; else out += t.amount;
            txList.add(txnRow(t));
        }
        moneyIn.setText(String.format("\u20B9 %,.2f", in));
        moneyOut.setText(String.format("\u20B9 %,.2f", out));
        txList.revalidate();
        txList.repaint();
        SwingUtilities.invokeLater(() -> txScroll.getVerticalScrollBar().setValue(0));

        rebuildActions();
    }

    private JPanel txnRow(Txn t) {
        Color c = t.credit ? CREDIT : DEBIT;
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER),
                new EmptyBorder(10, 2, 10, 2)));
        row.add(new TxnIcon(c, t.credit), BorderLayout.WEST);

        JPanel mid = new JPanel(new GridLayout(2, 1, 0, 2));
        mid.setOpaque(false);
        mid.add(label(t.title, 14, Font.BOLD, TEXT));
        mid.add(label(t.time, 12, Font.PLAIN, MUTED));
        row.add(mid, BorderLayout.CENTER);

        JPanel end = new JPanel(new GridLayout(2, 1, 0, 2));
        end.setOpaque(false);
        JLabel amt = label((t.credit ? "+ " : "- ") + String.format("\u20B9 %,.2f", t.amount),
                14, Font.BOLD, c);
        amt.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel bal = label(String.format("Bal \u20B9 %,.2f", t.balance), 12, Font.PLAIN, MUTED);
        bal.setHorizontalAlignment(SwingConstants.RIGHT);
        end.add(amt);
        end.add(bal);
        row.add(end, BorderLayout.EAST);
        return row;
    }

    // ---------------- Account actions ----------------
    /** Amount dialog with quick-pick chips. Returns null if cancelled. */
    private Double askAmount(String title, String subtitle, Color color) {
        JTextField f = new JTextField();
        styleField(f);

        JPanel chips = new JPanel(new GridLayout(1, 4, 8, 0));
        chips.setOpaque(false);
        for (int v : new int[]{500, 1000, 2000, 5000}) {
            ModernButton b = new ModernButton("\u20B9" + v, BORDER, SLATE);
            b.setBorder(new EmptyBorder(8, 6, 8, 6));
            b.setFont(new Font(FONT, Font.BOLD, 13));
            b.addActionListener(e -> f.setText(String.valueOf(v)));
            chips.add(b);
        }

        ModernDialog d = new ModernDialog(this, title, subtitle);
        d.addField("Amount (\u20B9)", f);
        d.addComponent(chips);
        d.setButtons(title, color, color.brighter(), true);

        while (d.showDialog()) {
            try {
                double a = Double.parseDouble(f.getText().trim());
                d.dispose();
                return a;
            } catch (NumberFormatException ex) {
                error("Please enter a valid amount.");
            }
        }
        d.dispose();
        return null;
    }

    private void doDeposit() {
        Double amt = askAmount("Deposit", "Add money to your account", SUCCESS);
        if (amt == null) return;
        try {
            current.deposit(amt);
            afterChange();
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private void doWithdraw() {
        Double amt = askAmount("Withdraw", "Take money out of your account", AMBER);
        if (amt == null) return;
        try {
            current.withdraw(amt);
            afterChange();
        } catch (InsufficientFundsException | IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private void doTransfer() {
        JTextField to = new JTextField();
        JTextField amt = new JTextField();
        styleField(to);
        styleField(amt);

        ModernDialog d = new ModernDialog(this, "Transfer Money", "Send funds to another account");
        d.addField("Destination account number", to);
        d.addField("Amount (\u20B9)", amt);
        d.setButtons("Send", ACCENT, ACCENT_HI, true);

        while (d.showDialog()) {
            try {
                int no = Integer.parseInt(to.getText().trim());
                double a = Double.parseDouble(amt.getText().trim());
                bank.transfer(current, no, a);
                afterChange();
                success("Transfer successful",
                        String.format("\u20B9 %,.2f sent to account %d.", a, no));
                break;
            } catch (NumberFormatException ex) {
                error("Please enter a valid account number and amount.");
            } catch (InsufficientFundsException | IllegalArgumentException ex) {
                error(ex.getMessage());
            }
        }
        d.dispose();
    }

    private void doChangePin() {
        String pin = prompt("Change PIN", "Choose a new 4-digit PIN", "New PIN", true,
                "Update PIN", ACCENT);
        if (pin == null) return;
        if (!pin.matches("\\d{4}")) {
            error("PIN must be exactly 4 digits.");
            return;
        }
        current.changePin(pin);
        bank.save();
        success("PIN changed", "Your new PIN is active.");
    }

    private void doInterest() {
        if (current instanceof SavingsAccount) {
            double i = ((SavingsAccount) current).addInterest();
            afterChange();
            success("Interest credited", String.format("\u20B9 %,.2f added to your balance.", i));
        } else {
            error("Interest is only for savings accounts.");
        }
    }

    private void afterChange() {
        bank.save();
        refreshAccountView();
    }

    // =====================================================
    //  DIALOG HELPERS
    // =====================================================
    private void error(String m) {
        message("Something went wrong", m, ERROR, "OK", false);
    }

    private void success(String title, String m) {
        message(title, m, OK, "OK", false);
    }

    /** Shows a styled message. Returns true if the main button was pressed. */
    private boolean message(String title, String msg, int kind, String okText, boolean withCancel) {
        Color c = kind == ERROR ? DANGER : kind == OK ? SUCCESS : ACCENT_HI;
        ModernDialog d = new ModernDialog(this, title, "");

        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.add(new Badge(c, kind), BorderLayout.WEST);
        JLabel text = label("<html><body style='width:260px'>" + esc(msg) + "</body></html>",
                14, Font.PLAIN, TEXT);
        row.add(text, BorderLayout.CENTER);
        d.addComponent(row);

        d.setButtons(okText, c, c.brighter(), withCancel);
        boolean ok = d.showDialog();
        d.dispose();
        return ok;
    }

    /** One-field input dialog. Returns the trimmed text, or null if cancelled. */
    private String prompt(String title, String subtitle, String labelText, boolean secret,
                          String okText, Color color) {
        JTextField f = secret ? new JPasswordField() : new JTextField();
        styleField(f);
        ModernDialog d = new ModernDialog(this, title, subtitle);
        d.addField(labelText, f);
        d.setButtons(okText, color, color.brighter(), true);
        boolean ok = d.showDialog();
        d.dispose();
        if (!ok) return null;
        return (secret ? new String(((JPasswordField) f).getPassword()) : f.getText()).trim();
    }

    private static String esc(String s) {
        return String.valueOf(s).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // =====================================================
    //  GENERAL HELPERS
    // =====================================================
    static JLabel label(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(FONT, style, size));
        l.setForeground(color);
        return l;
    }

    static void styleField(JTextField f) {
        f.setBackground(FIELD);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setFont(new Font(FONT, Font.PLAIN, 15));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(10, 12, 10, 12)));
    }

    private static JButton linkButton(String text, Runnable r) {
        JButton b = new JButton(text);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setForeground(MUTED);
        b.setFont(new Font(FONT, Font.PLAIN, 13));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setForeground(TEXT); }
            @Override public void mouseExited(MouseEvent e) { b.setForeground(MUTED); }
        });
        b.addActionListener(e -> r.run());
        return b;
    }

    private static JScrollPane styledScroll(Component view, Color bg) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(null);
        sp.getViewport().setBackground(bg);
        sp.getVerticalScrollBar().setUI(new SlimScrollBarUI(bg));
        sp.getHorizontalScrollBar().setUI(new SlimScrollBarUI(bg));
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    // =====================================================
    //  TRANSACTION MODEL (parsed from Transaction.toString())
    // =====================================================
    static class Txn {
        final String time;
        final String title;
        final double amount;
        final double balance;
        final boolean credit;

        Txn(Transaction t) {
            String[] p = t.toString().split("\\|");
            String type = p.length > 1 ? p[1].trim() : "";
            time = p.length > 0 ? p[0].trim() : "";
            amount = p.length > 2 ? num(p[2]) : 0;
            balance = p.length > 3 ? num(p[3].replace("Balance:", "")) : 0;
            credit = type.startsWith("DEPOSIT") || type.startsWith("TRANSFER IN")
                    || type.startsWith("INTEREST") || type.startsWith("ACCOUNT");
            title = pretty(type);
        }

        private static double num(String s) {
            try {
                return NumberFormat.getNumberInstance().parse(s.trim()).doubleValue();
            } catch (ParseException e) {
                return 0;
            }
        }

        private static String pretty(String type) {
            if (type.startsWith("TRANSFER OUT >")) return "Transfer to " + type.substring(14).trim();
            if (type.startsWith("TRANSFER IN <")) return "Transfer from " + type.substring(13).trim();
            if (type.isEmpty()) return "Transaction";
            return type.charAt(0) + type.substring(1).toLowerCase();
        }
    }

    // =====================================================
    //  CUSTOM COMPONENTS
    // =====================================================
    static class GradientPanel extends JPanel {
        GradientPanel(LayoutManager lm) { super(lm); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, BG_TOP, getWidth(), getHeight(), BG_BOTTOM));
            g2.fillRect(0, 0, getWidth(), getHeight());
            // soft decorative circles
            g2.setColor(new Color(99, 102, 241, 38));
            g2.fillOval(-140, -140, 400, 400);
            g2.setColor(new Color(139, 92, 246, 34));
            g2.fillOval(getWidth() - 280, getHeight() - 240, 440, 440);
            g2.dispose();
        }
    }

    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fill;
        private final Color fill2;
        private Color outline;

        RoundedPanel(LayoutManager lm, int radius, Color fill) {
            this(lm, radius, fill, null);
        }

        RoundedPanel(LayoutManager lm, int radius, Color fill, Color fill2) {
            super(lm);
            this.radius = radius;
            this.fill = fill;
            this.fill2 = fill2;
            setOpaque(false);
        }

        RoundedPanel outline(Color c) {
            this.outline = c;
            return this;
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (fill2 != null) {
                g2.setPaint(new GradientPaint(0, 0, fill, getWidth(), getHeight(), fill2));
            } else {
                g2.setColor(fill);
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            if (outline != null) {
                g2.setColor(outline);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class ModernButton extends JButton {
        private Color base;
        private Color hover;
        private boolean over;

        ModernButton(String text, Color base, Color hover) {
            super(text);
            this.base = base;
            this.hover = hover;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(new Font(FONT, Font.BOLD, 14));
            setBorder(new EmptyBorder(12, 20, 12, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { over = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { over = false; repaint(); }
            });
        }

        void setColors(Color base, Color hover) {
            this.base = base;
            this.hover = hover;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(over ? hover : base);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Small coloured dot used in the hero feature list. */
    static class Dot extends JComponent {
        private final Color color;

        Dot(Color color) {
            this.color = color;
            setPreferredSize(new Dimension(10, 10));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(0, 0, 10, 10);
            g2.dispose();
        }
    }

    /** Round gradient avatar showing the first letter of the holder's name. */
    static class Avatar extends JComponent {
        private String initial = "?";

        Avatar() { setPreferredSize(new Dimension(52, 52)); }

        void setHolder(String n) {
            initial = (n == null || n.trim().isEmpty()) ? "?" : n.trim().substring(0, 1).toUpperCase();
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, ACCENT, 52, 52, PURPLE));
            g2.fillOval(0, 0, 52, 52);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(FONT, Font.BOLD, 22));
            FontMetrics fm = g2.getFontMetrics();
            int x = (52 - fm.stringWidth(initial)) / 2;
            int y = (52 + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(initial, x, y);
            g2.dispose();
        }
    }

    /** Round arrow icon for transaction rows (down = money in, up = money out). */
    static class TxnIcon extends JComponent {
        private final Color color;
        private final boolean credit;

        TxnIcon(Color color, boolean credit) {
            this.color = color;
            this.credit = credit;
            setPreferredSize(new Dimension(36, 36));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
            g2.fillOval(0, 0, 36, 36);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(18, 10, 18, 26);
            if (credit) {
                g2.drawPolyline(new int[]{12, 18, 24}, new int[]{20, 26, 20}, 3);
            } else {
                g2.drawPolyline(new int[]{12, 18, 24}, new int[]{16, 10, 16}, 3);
            }
            g2.dispose();
        }
    }

    /** Round icon used in message dialogs (info / error / success). */
    static class Badge extends JComponent {
        private final Color color;
        private final int kind;

        Badge(Color color, int kind) {
            this.color = color;
            this.kind = kind;
            setPreferredSize(new Dimension(44, 44));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 45));
            g2.fillOval(0, 0, 44, 44);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (kind == OK) {                       // check mark
                g2.drawPolyline(new int[]{13, 20, 32}, new int[]{23, 30, 15}, 3);
            } else if (kind == ERROR) {             // exclamation mark
                g2.drawLine(22, 12, 22, 25);
                g2.fillOval(20, 30, 4, 4);
            } else {                                // "i"
                g2.fillOval(20, 11, 4, 4);
                g2.drawLine(22, 20, 22, 33);
            }
            g2.dispose();
        }
    }

    static class SlimScrollBarUI extends BasicScrollBarUI {
        private final Color trackBg;

        SlimScrollBarUI(Color trackBg) { this.trackBg = trackBg; }

        @Override protected JButton createDecreaseButton(int o) { return zero(); }
        @Override protected JButton createIncreaseButton(int o) { return zero(); }

        private JButton zero() {
            JButton b = new JButton();
            Dimension d = new Dimension(0, 0);
            b.setPreferredSize(d);
            b.setMinimumSize(d);
            b.setMaximumSize(d);
            return b;
        }

        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackBg);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(SLATE);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }

    /** Borderless, rounded, draggable dark dialog (replaces the default pop-ups). */
    static class ModernDialog extends JDialog {
        private boolean confirmed;
        private final JPanel content = new JPanel();
        private final JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        private JComponent firstFocus;
        private Point dragStart;

        ModernDialog(Window owner, String title, String subtitle) {
            super(owner, ModalityType.APPLICATION_MODAL);
            setUndecorated(true);
            try {
                setBackground(new Color(0, 0, 0, 0));
            } catch (UnsupportedOperationException ignored) { }
            getRootPane().setOpaque(false);

            RoundedPanel card = new RoundedPanel(new BorderLayout(0, 18), 24, CARD).outline(BORDER);
            card.setBorder(new EmptyBorder(24, 28, 22, 28));

            JPanel head = new JPanel();
            head.setOpaque(false);
            head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
            JLabel t = label(title, 20, Font.BOLD, TEXT);
            t.setAlignmentX(LEFT_ALIGNMENT);
            head.add(t);
            if (subtitle != null && !subtitle.isEmpty()) {
                head.add(Box.createVerticalStrut(4));
                JLabel s = label(subtitle, 13, Font.PLAIN, MUTED);
                s.setAlignmentX(LEFT_ALIGNMENT);
                head.add(s);
            }

            content.setOpaque(false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            buttonRow.setOpaque(false);

            card.add(head, BorderLayout.NORTH);
            card.add(content, BorderLayout.CENTER);
            card.add(buttonRow, BorderLayout.SOUTH);

            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);
            wrap.setBorder(new EmptyBorder(6, 6, 6, 6));
            wrap.add(card, BorderLayout.CENTER);
            setContentPane(wrap);

            // drag the dialog by its card
            MouseAdapter drag = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { dragStart = e.getPoint(); }
                @Override public void mouseDragged(MouseEvent e) {
                    if (dragStart == null) return;
                    Point p = getLocation();
                    setLocation(p.x + e.getX() - dragStart.x, p.y + e.getY() - dragStart.y);
                }
            };
            card.addMouseListener(drag);
            card.addMouseMotionListener(drag);

            // ESC = cancel
            getRootPane().registerKeyboardAction(e -> close(false),
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            addWindowListener(new WindowAdapter() {
                @Override public void windowActivated(WindowEvent e) {
                    if (firstFocus != null) firstFocus.requestFocusInWindow();
                }
            });
        }

        void addField(String labelText, JComponent field) {
            JLabel l = label(labelText, 12, Font.BOLD, MUTED);
            l.setAlignmentX(LEFT_ALIGNMENT);
            field.setAlignmentX(LEFT_ALIGNMENT);
            content.add(l);
            content.add(Box.createVerticalStrut(6));
            content.add(field);
            content.add(Box.createVerticalStrut(14));
            if (firstFocus == null && field instanceof JTextField) firstFocus = field;
        }

        void addComponent(JComponent c) {
            c.setAlignmentX(LEFT_ALIGNMENT);
            content.add(c);
            content.add(Box.createVerticalStrut(14));
        }

        void setButtons(String okText, Color okColor, Color okHover, boolean withCancel) {
            buttonRow.removeAll();
            if (withCancel) {
                ModernButton cancel = new ModernButton("Cancel", SLATE, SLATE_HI);
                cancel.addActionListener(e -> close(false));
                buttonRow.add(cancel);
            }
            ModernButton ok = new ModernButton(okText, okColor, okHover);
            ok.addActionListener(e -> close(true));
            buttonRow.add(ok);
            getRootPane().setDefaultButton(ok);
        }

        private void close(boolean ok) {
            confirmed = ok;
            setVisible(false);
        }

        /** Shows the dialog (modal). Returns true if the main button was pressed. */
        boolean showDialog() {
            confirmed = false;
            pack();
            Dimension d = getPreferredSize();
            setSize(Math.max(d.width, 440), d.height);
            setLocationRelativeTo(getOwner());
            setVisible(true);
            return confirmed;
        }
    }

    // =====================================================
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new BankGUI().setVisible(true));
    }
}
