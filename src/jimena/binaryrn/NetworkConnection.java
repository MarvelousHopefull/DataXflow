package jimena.binaryrn;

/**
 * Represents a connection in a regulatory network.
 * 
 * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
 * 
 */
public class NetworkConnection {
    private int node, position;

    /**
     * Creates a new tupel representing a connection in a regulatory network.
     * 
     * @param node
     *            The node of the connection.
     * @param position
     *            The position of the connection
     */
    public NetworkConnection(int node, int position) {
        this.node = node;
        this.position = position;
    }

    /**
     * Returns the node of the connection.
     * 
     * @return Node of the connection
     */
    public int getNode() {
        return node;
    }

    /**
     * Returns the position of the connection.
     * 
     * @return Position of the connection.
     */
    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "[" + node + ": " + position + "]";
    }

    /**
     * Compares this Connection to another.
     * 
     * @param connection
     *            The connection to compare.
     * @return True if they are equal.
     */
    public boolean equals(NetworkConnection connection) {
        return (connection.getPosition() == this.position && connection.getNode() == this.node);
    }
}
