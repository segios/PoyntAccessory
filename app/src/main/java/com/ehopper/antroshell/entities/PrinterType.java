package com.ehopper.antroshell.entities;

/**
 *  Created by SStarikov on 4/29/2016.
 */

public enum PrinterType {
    Network(1.0, 30),
    Bluetooth(1.0, 30),
    USB(1.0, 30),
    POWA(1.7, 30),
    POYNT(2.44, 30),
    ELO(10, 32),
    SNBC(1.5, 42);

    private double scale;
    private int textLength;

    private PrinterType(double scale, int textLength) {
        this.scale = scale;
        this.textLength = textLength;
    }

    public double scale() {
        return this.scale;
    }

    public int textLength() {
        return this.textLength;
    }
};

