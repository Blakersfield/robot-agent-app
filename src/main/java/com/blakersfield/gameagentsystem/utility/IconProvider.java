package com.blakersfield.gameagentsystem.utility;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class IconProvider {
    private static final String ICONS_PATH = "icons/"; 
    private static final int ICON_SIZE = 150;
    private static final Map<String, Icon> iconCache = new HashMap<>();

    public static Icon get(String name) {
        return iconCache.computeIfAbsent(name, IconProvider::loadIcon);
    }

    private static Icon loadIcon(String name) {
        String path = ICONS_PATH + name + "_icon.png";
        java.net.URL resource = IconProvider.class.getClassLoader().getResource(path);
        if (resource == null) {
            System.err.println("⚠ Icon not found: " + path);
            return null;
        }

        ImageIcon original = new ImageIcon(resource);
        Image scaled = original.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
