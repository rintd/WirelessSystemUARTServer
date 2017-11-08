package FX.elements;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

/**
 * Created by jiro on 01.08.17.
 */

public class JFxNode extends Circle {

    public JFxNode (double centerX, double centerY, double radius, Paint fill) {
        super(centerX,centerY, radius, fill);
    }
}
