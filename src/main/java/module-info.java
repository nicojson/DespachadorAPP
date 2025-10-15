module tecnm.celaya.edu.mx.despachadorapp {
    // Módulos de JavaFX requeridos por la aplicación
    requires javafx.controls;
    requires javafx.fxml;

    // Módulos de dependencias externas
    requires org.controlsfx.controls;
    //requires formsfx.core; // Nombre de módulo corregido
    requires org.kordamp.ikonli.javafx;
    //requires bootstrapfx.core; // Nombre de módulo corregido

    // Permite que JavaFX acceda a los controladores y al modelo de datos mediante reflexión.
    opens tecnm.celaya.edu.mx.despachadorapp to javafx.fxml;

    // Exporta el paquete principal para que la aplicación pueda ser lanzada.
    exports tecnm.celaya.edu.mx.despachadorapp;
}
