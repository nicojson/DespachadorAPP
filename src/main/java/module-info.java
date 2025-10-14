module tecnm.celaya.edu.mx.despachadorapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens tecnm.celaya.edu.mx.despachadorapp to javafx.fxml;
    exports tecnm.celaya.edu.mx.despachadorapp;
}