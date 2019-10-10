package com.hari.printermanager.print;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public class PrintImage {

    public void printImage() {

        final Image img = new ImageIcon("classpath:scenic-pic.jpg").getImage();
        PrinterJob printJob = PrinterJob.getPrinterJob();

        printJob.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex != 0) {
                    return NO_SUCH_PAGE;
                }
                graphics.drawImage(img, 0, 0, img.getWidth(null), img.getHeight(null),   null);
                return PAGE_EXISTS;
            }
        });

        try {
            printJob.print();
        } catch (Exception prt) {
            System.err.println(prt.getMessage());
        }

    }

}
