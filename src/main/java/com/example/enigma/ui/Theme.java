package com.example.enigma.ui;

import java.awt.Color;
import java.awt.Font;

public final class Theme {

    private Theme() {
    }

    public static final Color FRAME_BACKGROUND = new Color(40, 37, 34);
    public static final Color PANEL_BACKGROUND = new Color(26, 24, 22);
    public static final Color TEXT_LIGHT = new Color(222, 216, 205);

    public static final Color LAMP_ON = new Color(255, 205, 60);
    public static final Color LAMP_OFF = new Color(55, 50, 45);
    public static final Color LAMP_RING = new Color(15, 13, 12);

    public static final Color KEY_IDLE = new Color(52, 47, 43);
    public static final Color KEY_PRESSED = new Color(100, 92, 82);
    public static final Color KEY_RING = new Color(15, 13, 12);

    public static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 13);
}
