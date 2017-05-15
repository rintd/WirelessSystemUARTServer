package FX;

import UART.UARTConnector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Created by Jiro on 23.01.17.
 */
public class JFxUIMain extends Application  {
    private UARTConnector uartConnector = null;

    @Override
    public void start(Stage primaryStage) throws Exception {
        uartConnector = new UARTConnector();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxinterface.fxml"));

        VBox vbox = loader.load();

        JFxMainUIController controller = loader.getController();
        controller.setUartConnector(uartConnector);

        Scene scene = new Scene(vbox, 1280, 800);

        primaryStage.setTitle("RINTD Wireless System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}