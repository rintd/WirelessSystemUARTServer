package FX.SystemUI;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

/**
 * Created by jiro on 09.04.17.
 */

public class WirelessSensor extends Circle {
    public  int sensor_id;
    public boolean isInited;

    public  WirelessSensor(double centerX, double centerY, double radius, Paint color){
        super(centerX,centerY,radius,color);
    }
}
