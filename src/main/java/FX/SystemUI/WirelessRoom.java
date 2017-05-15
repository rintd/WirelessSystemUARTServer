package FX.SystemUI;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

/**
 * Created by jiro on 09.04.17.
 */
public class WirelessRoom extends Rectangle {

    public WirelessSensor sensor;
    public WirelessRoom(double width, double height, Paint color) {
        super(width,height,color);
    }
}
