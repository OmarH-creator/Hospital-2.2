module com.example.hospitalsystemsimpletesting {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens com.example.hospitalsystemsimpletesting to javafx.fxml;
    exports com.example.hospitalsystemsimpletesting;
    
    opens com.example.hospitalsystemsimpletesting.model to javafx.fxml;
    exports com.example.hospitalsystemsimpletesting.model;
    
    opens com.example.hospitalsystemsimpletesting.service to javafx.fxml;
    exports com.example.hospitalsystemsimpletesting.service;
    
    opens com.example.hospitalsystemsimpletesting.controller to javafx.fxml;
    exports com.example.hospitalsystemsimpletesting.controller;
    
    opens com.example.hospitalsystemsimpletesting.ui to javafx.fxml;
    exports com.example.hospitalsystemsimpletesting.ui;
}