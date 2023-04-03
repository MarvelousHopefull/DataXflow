package jimena.binaryrn;

import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * Implements a connection to a node in a regulatory network.
 * 
 * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
 * 
 */
public class Connection implements Serializable {
    private static final long serialVersionUID = -9117385978236315486L;
    private int source;
    private Point2D.Double pathOrigin;
    private Point2D.Double pathTarget;
    private Point2D.Double[] path;

    /**
     * Creates a new connection.
     * 
     * @param source
     *            Specifies the number of the node where the connection originates
     * @param path
     *            Intermediate points of the connection line
     * @param pathOrigin
     *            The origin of the path relative to the center of the source node
     * @param pathTarget
     *            The target of the path relative to the center of the target node
     */
    public Connection(int source, Point2D.Double[] path, Point2D.Double pathOrigin, Point2D.Double pathTarget) {
        if (path == null || pathOrigin == null || pathTarget == null) {
            throw new NullPointerException();
        }

        for (Point2D.Double p : path) {
            if (p == null) {
                throw new NullPointerException();
            }
        }

        if (source < 0) {
            throw new IllegalArgumentException();
        }

        this.source = source;
        this.path = path;
        this.pathOrigin = pathOrigin;
        this.pathTarget = pathTarget;
    }

    /**
     * Creates a new connection line. The path origin and target will be set to (0, 0)
     * 
     * @param source
     *            Specifies the number of the node where the connection originates
     * @param path
     *            Intermediate points of the connection
     */
    public Connection(int source, Point2D.Double[] path) {
        this(source, path, new Point2D.Double(), new Point2D.Double());
    }

    /**
     * Creates a new connection line. The path origin and target will be set to (0, 0)
     * 
     * @param source
     *            Specifies the number of the node where the connection originates
     */
    public Connection(int source) {
        this(source, new Point2D.Double[0]);
    }
    
    /**
     * Returns the source of this connection.
     * 
     * @return The source of the connection
     */
    public int getSource() {
        return source;
    }
   

    /**
     * Return the path of this connection .
     * 
     * @return Waypoints
     */
    public Point2D.Double[] getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "Inp: " + source;
    }

    /**
     * Returns the origin of the path relative to the center of the source node.
     * 
     * @return The origin of the path
     */
    public Point2D.Double getPathOrigin() {
        return pathOrigin;
    }

    /**
     * Sets the origin of the path relative to the center of the source node.
     * 
     * @param pathOrigin
     *            The origin of the path to set
     */
    public void setPathOrigin(Point2D.Double pathOrigin) {
        this.pathOrigin = pathOrigin;
    }

    /**
     * Returns the target of the path relative to the center of the target node.
     * 
     * @return Target of the path
     */
    public Point2D.Double getPathTarget() {
        return pathTarget;
    }

    /**
     * Sets the target of the path relative to the center of the target node.
     * 
     * @param pathTarget
     *            The target of the path to set
     */
    public void setPathTarget(Point2D.Double pathTarget) {
        this.pathTarget = pathTarget;
    }

    /**
     * Sets the source of this node;
     * 
     * @param source New source
     */
    public void setSource(int source) {
        this.source = source;

    }

    /**
     * Creates a deep copy of this name.
     */
    @Override
    public Connection clone() {
        Point2D.Double[] copiedPath = new Point2D.Double[path.length];
        for (int i = 0; i < copiedPath.length; i++) {
            copiedPath[i] = (Point2D.Double) path[i].clone();
        }
        return new Connection(source, copiedPath, (Point2D.Double) pathOrigin.clone(), (Point2D.Double) pathTarget.clone());
    }
}
