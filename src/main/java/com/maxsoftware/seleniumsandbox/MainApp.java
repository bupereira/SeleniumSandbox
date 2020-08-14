package com.maxsoftware.seleniumsandbox;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private FXMLController myController;
    
    @Override
    public void start(Stage stage) throws Exception {
        //THIS WORKS, but I need the controller
        //Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));
        // Solution to get controller: https://stackoverflow.com/questions/10751271/accessing-fxml-controller-class
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Scene.fxml"));
        Parent root = loader.load();
        //Now we have access to getController() through the instance... don't forget the type cast
        this.myController = (FXMLController)loader.getController();
        // End solution
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        stage.setTitle("JavaFX and Maven");
        stage.setScene(scene);
        stage.show();
    }
    
    @Override
    public void stop(){
        System.out.println("Stop in main app - Stage is closing");
        myController.closeInstances();
        // Save file
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public void setController(FXMLController controller) {
        this.myController = controller;
    }
    
}
