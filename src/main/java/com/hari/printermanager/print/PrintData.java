package com.hari.printermanager.print;

import net.spy.memcached.MemcachedClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//https://docs.oracle.com/javase/7/docs/technotes/guides/jps/spec/appendix_printPS.fm.html
// https://docs.oracle.com/javase/7/docs/api/javax/print/attribute/standard/ColorSupported.html

@Component
public class PrintData {

    private static final Logger LOGGER = LogManager.getLogger(PrintData.class);

    private String textFileName;
    private String imageFileName;
    private Boolean printTextFile;
    private final MemcachedClient memcachedClient;
    private final String printerName;

    @Autowired
    public PrintData(@Value("${printer.name}") String printerName,
                     @Value("${text.file.name}") String textFileName,
                     @Value("${image.file.name}") String imageFileName,
                     @Value("${print.text.file}") String printTextFile,
                     MemcachedClient memcachedClient) {
        this.textFileName      = textFileName;
        this.imageFileName     = imageFileName;
        this.printTextFile     = Boolean.valueOf(printTextFile);
        this.memcachedClient   = memcachedClient;
        this.printerName       = printerName;
    }

    //10:30 am on 15th day of every month
    //@Scheduled(cron = "0 30 10 15 * ?")
    @Scheduled(cron = "*/10 * * * * *") //For testing purposes only
    public void print() {

        PrintImage printImage = new PrintImage();
        printImage.printImage();

        boolean printerCheck = false;

        // Read from file and print - alternate weeks print scenic image.
        byte[] fileBytes = null;
        String fileName = printTextFile ? textFileName : imageFileName;

        // Print image using color ink and text using black ink
        PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
        attributeSet.add(Chromaticity.COLOR);

        PrintRequestAttributeSet  finalAttributeSet = null;// printTextFile ? null : attributeSet;

        try {
            Path path = Paths.get(getClass().getClassLoader().getResource(fileName).toURI());
            fileBytes = Files.readAllBytes(path);
        }
        catch (URISyntaxException e) {
            LOGGER.error("Error when reading from file :" + e.getMessage());
        }
        catch (IOException ie) {
            LOGGER.error("Error when reading from file :" + ie.getMessage());
        }

        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, finalAttributeSet);
        LOGGER.info("Number of printers available : " + printServices.length);

        for (PrintService printService : printServices) {
            LOGGER.info("Printer: " + printService.getName());

            if (printService.getName().toLowerCase().equals(printerName)) {

                InputStream inputStream = new ByteArrayInputStream(fileBytes);
                DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
                Doc doc = new SimpleDoc(inputStream, flavor, null);
                DocPrintJob job = printService.createPrintJob();

                try {
                    job.print(doc, finalAttributeSet);
                }
                catch (PrintException pe) {
                    LOGGER.error("Error when printing : " + pe.getMessage());
                }
                printerCheck = true;
            }
        }
        if (printerCheck == false) {
            LOGGER.info("The printer you were searching for could not be found.");
        }
    }
}