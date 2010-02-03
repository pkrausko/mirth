/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.manager;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.ImageIcon;

/**
 * Creates the heading panel that is used for wizards, login, etc.
 */
public class MirthHeadingPanel extends javax.swing.JPanel {

    /** Creates new form MirthHeadingPanel */
    public MirthHeadingPanel() {
        initComponents();
        setOpaque(false);
        setPreferredSize(new Dimension(138, 22));
    }

    protected void paintComponent(Graphics g) {
        g.drawImage(new ImageIcon(this.getClass().getResource("images/header_nologo.png")).getImage(), 0, 0, null);
        super.paintComponent(g);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 192, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 31, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
