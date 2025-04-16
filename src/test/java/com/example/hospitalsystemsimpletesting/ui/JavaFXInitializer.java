package com.example.hospitalsystemsimpletesting.ui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class to initialize the JavaFX toolkit for UI tests.
 */
public class JavaFXInitializer {
    
    private static final AtomicBoolean jfxIsSetup = new AtomicBoolean(false);
    
    /**
     * Initializes the JavaFX environment for testing.
     * This method must be called before any JavaFX component is created.
     */
    public static synchronized void setupJavaFX() throws InterruptedException {
        if (jfxIsSetup.get()) {
            return;
        }
        
        // Set headless properties
        System.setProperty("java.awt.headless", "false");
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        
        // Create a latch to wait for toolkit initialization
        final CountDownLatch latch = new CountDownLatch(1);
        
        // Use SwingUtilities to ensure JavaFX gets initialized on the AWT thread
        SwingUtilities.invokeLater(() -> {
            // Create a JFXPanel to initialize the JavaFX toolkit
            new JFXPanel();
            
            Platform.runLater(() -> {
                // JavaFX is now initialized
                latch.countDown();
            });
        });
        
        // Wait for initialization to complete
        if (!latch.await(10, TimeUnit.SECONDS)) {
            throw new ExceptionInInitializerError("Could not initialize JavaFX Toolkit");
        }
        
        Platform.setImplicitExit(false);
        jfxIsSetup.set(true);
    }
} 