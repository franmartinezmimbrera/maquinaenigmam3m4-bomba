package com.example.enigma.ui;

import com.example.enigma.model.EncryptionStep;
import com.example.enigma.model.EncryptionTrace;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

public final class StepsPanel extends JPanel {

    private static final String[] COLUMNS = {"Letra pulsada", "Etapa", "Entrada", "Salida"};

    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JLabel emptyLabel = new JLabel(
            "Todavía no se ha cifrado ni descifrado ningún texto.", SwingConstants.CENTER);

    public StepsPanel() {
        super(new BorderLayout());
        table.setRowHeight(22);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(emptyLabel, BorderLayout.NORTH);
    }

    public void showTraces(List<EncryptionTrace> traces) {
        model.setRowCount(0);
        for (EncryptionTrace trace : traces) {
            for (EncryptionStep step : trace.steps()) {
                model.addRow(new Object[]{
                        String.valueOf(trace.keyPressed()),
                        step.stage(),
                        String.valueOf(step.input()),
                        String.valueOf(step.output())
                });
            }
        }
        emptyLabel.setVisible(traces.isEmpty());
    }
}
