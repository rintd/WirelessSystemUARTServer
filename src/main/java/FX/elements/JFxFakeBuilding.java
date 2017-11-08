package FX.elements;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;

/**
 * Created by jiro on 02.08.17.
 */
public class JFxFakeBuilding extends Group {
    private double width;
    private double height;

    private final int size = 26;
    private final int sizeH = 16;

    private double cellSize = 0;

    public JFxFakeBuilding(double iWidth, double iHeight){
        super();
        width = iWidth;
        height = iHeight;
        cellSize = width / size;

        drawFakeBuilding();
    }

    private void drawFakeBuilding(){

        Rectangle room = new Rectangle(getPositionWithCell(9),getPositionWithCell(6),Color.ANTIQUEWHITE);
        room.setStroke(Color.BLACK);
        this.getChildren().add(room);

        room = new Rectangle(getPositionWithCell(4),getPositionWithCell(6),Color.ANTIQUEWHITE);
        room.setStroke(Color.BLACK);
        room.setTranslateX(getPositionWithCell(9));
        this.getChildren().add(room);
    }

    private double getPositionWithCell(int cellIndex){
        return cellIndex * cellSize;
    }
}
