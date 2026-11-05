package com.example.enigma.ui;

import com.example.enigma.AppInfo;
import com.example.enigma.controller.EnigmaController;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;

public final class MainWindow extends JFrame {

    public MainWindow(EnigmaController controller) {
        super("Enigma M3-M4 " + AppInfo.VERSION);
        // Una única ruta de cierre: X de la ventana y «Archivo > Salir» terminan la JVM.
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exit();
            }
        });
        setLayout(new BorderLayout());
        setIconImage(AppIcon.create(64));
        getContentPane().setBackground(Theme.FRAME_BACKGROUND);

        Runnable openSettings = () -> new SettingsDialog(this, controller).setVisible(true);
        Runnable openAbout = () -> new AboutDialog(this, "Simulador Máquina Enigma M3-M4").setVisible(true);
        Runnable openHelp = () -> new HelpDialog(this).setVisible(true);

        setJMenuBar(buildMenuBar(controller, openAbout, openHelp));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Theme.FRAME_BACKGROUND);
        top.add(buildSettingsBar(openSettings, openAbout), BorderLayout.NORTH);
        top.add(new RotorWindowPanel(controller, openSettings), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        TextPanel textPanel = new TextPanel(controller, () -> new StepsDialog(this, controller).setVisible(true));

        LampboardPanel lampboardPanel = new LampboardPanel();
        controller.addListener(lampboardPanel);

        KeyboardPanel keyboardPanel = new KeyboardPanel(controller, textPanel::appendTypedLetter);
        PlugboardPanel plugboardPanel = new PlugboardPanel(controller);

        JPanel middle = new JPanel();
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        middle.setBackground(Theme.FRAME_BACKGROUND);
        middle.add(sectionDivider("LÁMPARAS · salida"));
        middle.add(lampboardPanel);
        middle.add(sectionDivider("TECLADO · entrada"));
        middle.add(keyboardPanel);
        middle.add(plugboardPanel);
        middle.add(textPanel);

        JScrollPane scrollPane = new JScrollPane(middle,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        setMinimumSize(new Dimension(720, 500));
        setSize(1050, 950);
        setLocationRelativeTo(null);
    }

    /** Cierra la ventana y termina la JVM (con dispose() a secas puede quedar algo vivo en segundo plano). */
    private void exit() {
        dispose();
        System.exit(0);
    }

    private JMenuBar buildMenuBar(EnigmaController controller, Runnable openAbout, Runnable openHelp) {
        JMenuBar menuBar = new JMenuBar();

        JMenu archivo = new JMenu("Archivo");
        archivo.setMnemonic(KeyEvent.VK_A);

        JMenuItem guardar = new JMenuItem("Guardar configuración...");
        guardar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        guardar.addActionListener(e -> ConfigurationIO.save(this, controller));
        archivo.add(guardar);

        JMenuItem cargar = new JMenuItem("Cargar configuración...");
        cargar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        cargar.addActionListener(e -> ConfigurationIO.load(this, controller));
        archivo.add(cargar);

        archivo.addSeparator();

        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> exit());
        archivo.add(salir);

        JMenu ayuda = new JMenu("Ayuda");
        ayuda.setMnemonic(KeyEvent.VK_Y);

        JMenuItem acercaDe = new JMenuItem("Acerca de");
        acercaDe.addActionListener(e -> openAbout.run());
        ayuda.add(acercaDe);

        JMenuItem comoFunciona = new JMenuItem("Cómo funciona Enigma");
        comoFunciona.addActionListener(e -> openHelp.run());
        ayuda.add(comoFunciona);

        menuBar.add(archivo);
        menuBar.add(ayuda);
        return menuBar;
    }

    private static JPanel sectionDivider(String title) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Theme.FRAME_BACKGROUND);
        panel.setBorder(new EmptyBorder(10, 16, 6, 16));

        JLabel label = new JLabel(title);
        label.setFont(Theme.TITLE_FONT);
        label.setForeground(Theme.TEXT_LIGHT);
        panel.add(label, BorderLayout.WEST);

        JPanel line = new JPanel(new BorderLayout());
        line.setOpaque(false);
        JSeparator separator = new JSeparator();
        separator.setForeground(Theme.TEXT_LIGHT);
        line.add(separator, BorderLayout.CENTER);
        panel.add(line, BorderLayout.CENTER);

        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }

    private static JPanel buildSettingsBar(Runnable openSettings, Runnable openAbout) {
        JPanel bar = new JPanel();
        bar.setBorder(new EmptyBorder(8, 8, 0, 8));
        bar.setBackground(Theme.FRAME_BACKGROUND);

        JButton settingsButton = new JButton("⚙ Rotores y reflector...");
        settingsButton.setFont(settingsButton.getFont().deriveFont(Font.BOLD, 14f));
        settingsButton.setToolTipText("Elige rotores, anillos, posiciones y reflector");
        settingsButton.addActionListener(e -> openSettings.run());
        bar.add(settingsButton);

        JButton aboutButton = new JButton("ℹ Acerca de");
        aboutButton.setFont(aboutButton.getFont().deriveFont(Font.BOLD, 14f));
        aboutButton.addActionListener(e -> openAbout.run());
        bar.add(aboutButton);

        return bar;
    }
}
