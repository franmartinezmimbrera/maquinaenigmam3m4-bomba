package com.example.enigma.ui;

import com.example.enigma.bombe.BombeSearch;
import com.example.enigma.bombe.Menu;
import com.example.enigma.bombe.Stop;
import com.example.enigma.AppInfo;
import com.example.enigma.model.Alphabet;
import com.example.enigma.model.ReflectorType;
import com.example.enigma.model.RotorType;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BombeWindow extends JFrame {

    private final JTextArea cipherTextArea = new JTextArea(5, 60);
    private final JTextField cribField = new JTextField(20);
    private final JComboBox<Integer> offsetCombo = new JComboBox<>();
    private final Map<RotorType, JCheckBox> rotorCheckboxes = new java.util.LinkedHashMap<>();
    private final JComboBox<ReflectorType> reflectorCombo = new JComboBox<>(ReflectorType.values());
    private final JComboBox<Character>[] ringCombos = buildLetterCombos();
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JLabel statusLabel = new JLabel(" ");
    private final DefaultTableModel resultsModel = new DefaultTableModel(
            new Object[] {"Orden de rotores", "Anillos", "Posición inicial", "Pares de clavijero deducidos"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    /** Búsqueda en curso (null si no hay ninguna); se cancela al cerrar la ventana. */
    private SwingWorker<List<Stop>, Integer> searchWorker;

    public BombeWindow() {
        super("Bombe de Turing — criptoanálisis de Enigma M3-M4 " + AppInfo.VERSION);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (searchWorker != null) {
                    searchWorker.cancel(true);
                }
            }
        });
        setIconImage(AppIcon.create(64));
        setLayout(new BorderLayout(8, 8));

        add(buildInputPanel(), BorderLayout.NORTH);
        add(buildResultsPanel(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        setMinimumSize(new Dimension(820, 600));
        setSize(1000, 750);
        setLocationRelativeTo(null);
    }

    private JPanel buildAboutBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton aboutButton = new JButton("ℹ Acerca de");
        aboutButton.setFont(aboutButton.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        aboutButton.addActionListener(e -> new AboutDialog(this, "Bombe de Turing").setVisible(true));
        bar.add(aboutButton);
        return bar;
    }

    @SuppressWarnings("unchecked")
    private static JComboBox<Character>[] buildLetterCombos() {
        JComboBox<Character>[] combos = new JComboBox[3];
        for (int i = 0; i < 3; i++) {
            combos[i] = new JComboBox<>(letters());
        }
        return combos;
    }

    private static Character[] letters() {
        Character[] result = new Character[Alphabet.SIZE];
        for (int i = 0; i < Alphabet.SIZE; i++) {
            result[i] = Alphabet.letterAt(i);
        }
        return result;
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        panel.add(buildAboutBar());

        JPanel cipherPanel = new JPanel(new BorderLayout(6, 6));
        cipherPanel.setBorder(new TitledBorder("Texto cifrado completo"));
        cipherTextArea.setLineWrap(true);
        cipherTextArea.setWrapStyleWord(true);
        cipherPanel.add(new JScrollPane(cipherTextArea), BorderLayout.CENTER);
        panel.add(cipherPanel);

        JPanel cribPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        cribPanel.setBorder(new TitledBorder("Crib (fragmento de texto plano que crees reconocer)"));
        cribPanel.add(new JLabel("Crib:"));
        cribPanel.add(cribField);
        JButton findOffsets = new JButton("Buscar alineaciones válidas");
        findOffsets.setToolTipText("Enigma nunca cifra una letra como sí misma: descarta así las posiciones imposibles");
        findOffsets.addActionListener(e -> findValidOffsets());
        cribPanel.add(findOffsets);
        cribPanel.add(new JLabel("Posición (offset):"));
        cribPanel.add(offsetCombo);
        offsetCombo.addActionListener(e -> previewLoopCount());
        panel.add(cribPanel);

        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        configPanel.setBorder(new TitledBorder("Rotores posibles, reflector y ring settings (conocidos, no se buscan)"));
        for (RotorType type : RotorType.values()) {
            if (type.isFourthRotorOnly()) {
                // Beta/Gamma solo existen como 4º rotor (M4) y la Bombe trabaja con tres rotores.
                continue;
            }
            // Por defecto I-V (los cinco rotores de la M3 histórica y de la práctica); VI-VIII se pueden añadir,
            // pero multiplican el tiempo de búsqueda (336 órdenes posibles en vez de 60).
            JCheckBox checkBox = new JCheckBox(type.name(), type.ordinal() <= RotorType.V.ordinal());
            rotorCheckboxes.put(type, checkBox);
            configPanel.add(checkBox);
        }
        configPanel.add(new JLabel("  Reflector:"));
        configPanel.add(reflectorCombo);
        configPanel.add(new JLabel("  Anillos:"));
        for (JComboBox<Character> combo : ringCombos) {
            configPanel.add(combo);
        }
        panel.add(configPanel);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        JButton searchButton = new JButton("Buscar con la Bombe");
        searchButton.addActionListener(e -> runSearch());
        searchPanel.add(searchButton);
        progressBar.setPreferredSize(new Dimension(300, 20));
        progressBar.setStringPainted(true);
        searchPanel.add(progressBar);
        panel.add(searchPanel);

        return panel;
    }

    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Stops encontrados"));
        JTable table = new JTable(resultsModel);
        table.setRowHeight(22);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JTextArea notice = new JTextArea(
                "Cada fila es un \"stop\": una hipótesis que no produjo ninguna contradicción al recorrer "
                        + "los bucles del menu. Como en la máquina real, puede haber stops falsos — "
                        + "compruébalos descifrando con esa configuración y viendo si el texto tiene sentido.");
        notice.setEditable(false);
        notice.setLineWrap(true);
        notice.setWrapStyleWord(true);
        notice.setOpaque(false);
        notice.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.add(notice, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 10));
        panel.add(statusLabel, BorderLayout.WEST);
        return panel;
    }

    private void findValidOffsets() {
        String cipherText = cipherTextArea.getText();
        String crib = cribField.getText();
        if (cipherText.trim().isEmpty() || crib.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribe el texto cifrado y el crib primero.",
                    "Faltan datos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Integer> offsets = Menu.findValidOffsets(stripNonLetters(cipherText), stripNonLetters(crib));
        offsetCombo.removeAllItems();
        for (int offset : offsets) {
            offsetCombo.addItem(offset);
        }
        statusLabel.setText(offsets.size() + " alineación(es) válida(s) encontradas de "
                + (cipherText.replaceAll("[^A-Za-z]", "").length() - crib.replaceAll("[^A-Za-z]", "").length() + 1)
                + " posibles.");
    }

    private static String stripNonLetters(String text) {
        return text.toUpperCase().replaceAll("[^A-Z]", "");
    }

    private void previewLoopCount() {
        if (offsetCombo.getSelectedItem() == null) {
            return;
        }
        String cipherText = stripNonLetters(cipherTextArea.getText());
        String crib = stripNonLetters(cribField.getText());
        int offset = (Integer) offsetCombo.getSelectedItem();
        try {
            Menu menu = Menu.build(crib, cipherText.substring(offset, offset + crib.length()));
            String quality = menu.loopCount() >= 2 ? "buen menu, debería dar pocos stops"
                    : menu.loopCount() == 1 ? "menu débil, esperables muchos stops falsos"
                    : "sin bucles: la Bombe no podrá usar este crib";
            statusLabel.setText("Offset " + offset + ": " + menu.loopCount() + " bucle(s) — " + quality);
        } catch (IllegalArgumentException | StringIndexOutOfBoundsException ex) {
            statusLabel.setText("Offset " + offset + ": alineación no válida");
        }
    }

    private void runSearch() {
        if (offsetCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Primero busca una alineación válida para el crib.",
                    "Falta la alineación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<RotorType> availableRotors = new ArrayList<>();
        for (Map.Entry<RotorType, JCheckBox> entry : rotorCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                availableRotors.add(entry.getKey());
            }
        }
        if (availableRotors.size() < 3) {
            JOptionPane.showMessageDialog(this, "Selecciona al menos 3 rotores posibles.",
                    "Faltan rotores", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String cipherText = stripNonLetters(cipherTextArea.getText());
        String crib = stripNonLetters(cribField.getText());
        int offset = (Integer) offsetCombo.getSelectedItem();
        String cipherSegment = cipherText.substring(offset, offset + crib.length());

        Menu menu;
        try {
            menu = Menu.build(crib, cipherSegment);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Crib no válido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!menu.hasLoop()) {
            JOptionPane.showMessageDialog(this,
                    "Este crib no forma ningún bucle en el menu, así que la Bombe no puede "
                            + "descartar hipótesis del clavijero con él. Prueba un crib más largo "
                            + "o en otra posición.",
                    "Sin bucles", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (menu.loopCount() < 2) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Este menu solo tiene " + menu.loopCount() + " bucle. Cada bucle de más divide "
                            + "aproximadamente entre 26 el número de \"stops\" falsos, así que con uno solo "
                            + "es casi seguro que salgan miles de stops inútiles (así pasaba en Bletchley "
                            + "Park: con cribs cortos no merecía la pena ni arrancar la Bombe).\n\n"
                            + "¿Buscar igualmente? Prueba un crib más largo, u otra alineación, para "
                            + "conseguir 2 o más bucles.",
                    "Menu débil", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        ReflectorType reflector = (ReflectorType) reflectorCombo.getSelectedItem();
        int[] ringSettings = new int[3];
        for (int i = 0; i < 3; i++) {
            ringSettings[i] = Alphabet.indexOf((Character) ringCombos[i].getSelectedItem());
        }

        resultsModel.setRowCount(0);
        progressBar.setValue(0);
        statusLabel.setText("Buscando...");

        searchWorker = new SwingWorker<List<Stop>, Integer>() {
            @Override
            protected List<Stop> doInBackground() {
                return BombeSearch.search(menu, availableRotors, reflector, ringSettings,
                        (tried, total) -> publish((int) (100.0 * tried / total)));
            }

            @Override
            protected void process(List<Integer> chunks) {
                progressBar.setValue(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                try {
                    List<Stop> stops = get();
                    showResults(stops);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BombeWindow.this,
                            "Error durante la búsqueda: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        searchWorker.execute();
    }

    private void showResults(List<Stop> stops) {
        progressBar.setValue(100);
        statusLabel.setText(stops.size() + " stop(s) encontrados.");
        for (Stop stop : stops) {
            StringBuilder rotorOrder = new StringBuilder();
            for (int i = 0; i < stop.rotorOrder().size(); i++) {
                if (i > 0) {
                    rotorOrder.append("-");
                }
                rotorOrder.append(stop.rotorOrder().get(i));
            }

            StringBuilder pairs = new StringBuilder();
            stop.plugboardPairs().forEach((a, b) -> {
                if (pairs.length() > 0) {
                    pairs.append(" ");
                }
                pairs.append(a).append(b);
            });

            resultsModel.addRow(new Object[] {
                    rotorOrder.toString(),
                    stop.ringSettingsAsText(),
                    stop.startPositionsAsText(),
                    pairs.length() == 0 ? "(ninguno deducido)" : pairs.toString()
            });
        }
    }
}
