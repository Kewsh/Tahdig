package elements.actions;

import javafx.scene.Group;
import javafx.scene.shape.Line;

public abstract class ConnectionBuilder {

    private static Line line;
    private static Point[] srcPoints;
    private static Point[] destPoints;

    public static void drawConnectionLine(Group root, char type1, char type2, String connectionType, double srcX, double srcY, double destX, double destY){

        if (type1 == 'c')                   //class
            srcPoints = new Point[]{new Point(srcX, srcY+60), new Point(srcX+50, srcY), new Point(srcX+100, srcY+60), new Point(srcX+50, srcY+120)};
        else if (type1 == 'i')              //interface
            srcPoints = new Point[]{new Point(srcX, srcY), new Point(srcX+75, srcY-60), new Point(srcX+150, srcY), new Point(srcX+75, srcY+60)};
        else                                //package
            srcPoints = new Point[]{new Point(srcX, srcY), new Point(srcX+75, srcY-50), new Point(srcX+150, srcY), new Point(srcX+75, srcY+50)};
        if (type2 == 'c')
            destPoints = new Point[]{new Point(destX, destY+60), new Point(destX+50, destY), new Point(destX+100, destY+60), new Point(destX+50, destY+120)};
        else if (type2 == 'i')
            destPoints = new Point[]{new Point(destX, destY), new Point(destX+75, destY-60), new Point(destX+150, destY), new Point(destX+75, destY+60)};
        else
            destPoints = new Point[]{new Point(destX, destY), new Point(destX+75, destY-50), new Point(destX+150, destY), new Point(destX+75, destY+50)};

        if (destY < srcY)
            line = new Line(srcPoints[1].x, srcPoints[1].y, destPoints[3].x, destPoints[3].y);
        else if (destY > srcY+120)
            line = new Line(srcPoints[3].x, srcPoints[3].y, destPoints[1].x, destPoints[1].y);
        else if (destX >= srcX)
            line = new Line(srcPoints[2].x, srcPoints[2].y, destPoints[0].x, destPoints[0].y);
        else
            line = new Line(srcPoints[0].x, srcPoints[0].y, destPoints[2].x, destPoints[2].y);

        line.setStrokeWidth(2.2);
        root.getChildren().add(line);

        //TODO: add the lines to canvas contents
        //TODO: insert the connectionType string somewhere on the line
        //TODO: work more on the connections algorithm

    }

    private static final class Point{
        public double x, y;
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
