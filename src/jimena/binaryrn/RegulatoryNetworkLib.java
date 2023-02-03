package jimena.binaryrn;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import jimena.binarybf.BinaryBooleanFunction;
import jimena.binarybf.actinhibitf.ActivatorInhibitorFunction;
import jimena.binarybf.treebf.ConstantNode;
import jimena.binarybf.treebf.InputNode;
import jimena.binarybf.treebf.NotNode;
import jimena.binarybf.treebf.TreeBooleanFunction;
import jimena.binarybf.treebf.TreeNode;
import jimena.binarybf.treebf.TreeNodeLib;
import jimena.libs.DoubleValue;
import jimena.libs.PatternLib;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A library with functions to create and manage binary regulatory networks.
 * 
 * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
 * 
 */
public class RegulatoryNetworkLib {


    /**
     * Extracts networks nodes and their functions from a yED graphML-file.
     * 
     * @param file
     *            The file to open
     * @param timeIndex
     *            The time index where the simulation starts, needed for the perturbations.
     * @return An array of network nodes
     * @throws Exception
     *             Throws different exception if the graphML file does not specify a valid network
     */
    public static NetworkNode[] parseYEdFile(File file, DoubleValue timeIndex) throws Exception {
        if (!file.exists()) {
            throw new IOException("The specified graphML file does not exist.");
        }

        if (timeIndex == null) {
            throw new NullPointerException();
        }

        // Further IOException are thrown by the XML parsing of the DocumentBuilder, NullPointerExceptions may be thrown if the graphML
        // structure is faulty (e.g. missing elements)

        // Open the XML file get its root
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();
        Element root = doc.getDocumentElement();

        // 1. Search for the graph.
        Node graph = getNamedChildNode(root, "graph");

        // 2. Search for nodes, distinguish between real nodes and logic gates and extract relevant informations
        // (e.g. position)

        LinkedList<TempNode> tempNodesLinked = new LinkedList<TempNode>();

        for (int i = 0; i < graph.getChildNodes().getLength(); i++) {
            // Search for 'node's, those are real nodes or logic gates
            Node graphelement = graph.getChildNodes().item(i);

            if (!graphelement.getNodeName().equals("node")) {
                continue;
            }

            // Descend to the real data
            Node graphelementData = getKeyedDataNode(graphelement, "d6").getChildNodes().item(1);
            if (graphelementData == null) {
                graphelementData = getKeyedDataNode(graphelement, "d7").getChildNodes().item(1);
            }

            // Extract data and store them in TempNodes
            String id = graphelement.getAttributes().getNamedItem("id").getNodeValue();
            String label = getNamedChildNode(graphelementData, "y:NodeLabel").getFirstChild().getTextContent();
            if (label.matches("\\s*")) {
                label = id;
            }

            Node positionNode = getNamedChildNode(graphelementData, "y:Geometry");
            double x = Double.valueOf(positionNode.getAttributes().getNamedItem("x").getNodeValue());
            double y = Double.valueOf(positionNode.getAttributes().getNamedItem("y").getNodeValue());
            double h = Double.valueOf(positionNode.getAttributes().getNamedItem("height").getNodeValue());
            double w = Double.valueOf(positionNode.getAttributes().getNamedItem("width").getNodeValue());
            Rectangle2D.Double position = new Rectangle2D.Double(x, y, w, h);

            boolean isRealNode = getGateType(label) == null;

            // Extract an initial value
            double initialValue = 0;
            // Try to parse it
            try {
                String initialValueString = getKeyedDataNode(graphelement, "d5").getTextContent();
                initialValue = Double.valueOf(initialValueString);
            } catch (NumberFormatException e) {
                // Do nothing and keep the 0.
            } catch (NullPointerException e) {
                // Do nothing and keep the 0.
            }

            tempNodesLinked.add(new TempNode(label, id, position, isRealNode, initialValue));
        }

        // LinkedList is finished, create Arrays now (for faster indexed access)
        ArrayList<TempNode> tempNodes = new ArrayList<TempNode>(tempNodesLinked);

        // Check for duplicate nodes (more exactly: duplicate node labels)
        for (int i = 0; i < tempNodes.size(); i++) {
            for (int j = 0; i < tempNodes.size(); i++) {
                if (i != j && tempNodes.get(i).isRealNode() && tempNodes.get(i).getLabel().equals(tempNodes.get(j).getLabel())) {
                    throw new IllegalArgumentException("Duplicate node found.");
                }
            }
        }

        // Create a mapping of labels to node indexes for faster recognition of the nodes in the xml graph
        HashMap<String, Integer> mapIdToNodeIndex = new HashMap<String, Integer>();
        int pos = 0;
        for (TempNode node : tempNodes) {
            mapIdToNodeIndex.put(node.getID(), pos);
            pos++;
        }

        // 3. Extract information regarding the connection between the nodes
        // Prepare target arrays

        // Array index is the target, sources are in the linkedList
        @SuppressWarnings("unchecked")
        LinkedList<Connection>[] linesLinked = new LinkedList[tempNodes.size()];
        @SuppressWarnings("unchecked")
        LinkedList<Boolean>[] linesIsNegatedLinked = new LinkedList[tempNodes.size()];

        for (int i = 0; i < linesLinked.length; i++) {
            linesIsNegatedLinked[i] = new LinkedList<Boolean>();
            linesLinked[i] = new LinkedList<Connection>();
        }

        // Search for the lines
        for (int i = 0; i < graph.getChildNodes().getLength(); i++) {
            Node graphelement = graph.getChildNodes().item(i);

            if (!graphelement.getNodeName().equals("edge")) {
                continue;
            }

            // Descend to the real data
            Node graphelementData = getKeyedDataNode(graphelement, "d10");
            if(graphelementData != null) {
            	graphelementData = graphelementData.getChildNodes().item(1);
            } 		
        		
            if (graphelementData == null) {
                graphelementData = getKeyedDataNode(graphelement, "d11").getChildNodes().item(1);
            }

            // Extract target and source of the line
            int target = mapIdToNodeIndex.get(graphelement.getAttributes().getNamedItem("target").getNodeValue());
            int source = mapIdToNodeIndex.get(graphelement.getAttributes().getNamedItem("source").getNodeValue());

            // Extract intermediate points of the path
            LinkedList<Point2D.Double> pointsLinked = new LinkedList<Point2D.Double>();

            Node pointsNode = getNamedChildNode(graphelementData, "y:Path");

            for (int j = 0; j < pointsNode.getChildNodes().getLength(); j++) {
                if (pointsNode.getChildNodes().item(j).getNodeName().equals("y:Point")) {
                    double x = Double.valueOf(pointsNode.getChildNodes().item(j).getAttributes().getNamedItem("x").getNodeValue());
                    double y = Double.valueOf(pointsNode.getChildNodes().item(j).getAttributes().getNamedItem("y").getNodeValue());
                    pointsLinked.add(new Point2D.Double(x, y));
                }
            }

            // Create input and store it
            Point2D.Double[] points = new Point2D.Double[pointsLinked.size()];
            pointsLinked.toArray(points);

            double pathOriginX = Double.valueOf(pointsNode.getAttributes().getNamedItem("sx").getNodeValue());
            double pathOriginY = Double.valueOf(pointsNode.getAttributes().getNamedItem("sy").getNodeValue());
            Point2D.Double pathOrigin = new Point2D.Double(pathOriginX, pathOriginY);

            double pathTargetX = Double.valueOf(pointsNode.getAttributes().getNamedItem("tx").getNodeValue());
            double pathTargetY = Double.valueOf(pointsNode.getAttributes().getNamedItem("ty").getNodeValue());
            Point2D.Double pathTarget = new Point2D.Double(pathTargetX, pathTargetY);

            linesLinked[target].add(new Connection(source, points, pathOrigin, pathTarget));

            // Determine whether the line is negated or not
            Node arrowsNode = getNamedChildNode(graphelementData, "y:Arrows");
            if (arrowsNode.getAttributes().getNamedItem("target").getNodeValue().equals("standard")) {
                linesIsNegatedLinked[target].add(false);
            } else {
                linesIsNegatedLinked[target].add(true);
            }
        }

        // Create real arrays for faster indexed access
        Connection[][] lines = new Connection[tempNodes.size()][];
        Boolean[][] linesIsNegated = new Boolean[tempNodes.size()][];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = new Connection[linesLinked[i].size()];
            linesLinked[i].toArray(lines[i]);
            linesIsNegated[i] = new Boolean[linesIsNegatedLinked[i].size()];
            linesIsNegatedLinked[i].toArray(linesIsNegated[i]);
        }

        /*
         * 4. Iterate over the real nodes and decide whether they can be modeled as activator-inhibitor-node or a tree node has to be used.
         * Then assemble the node.
         */
        // Preparation: Create a mapping of TempNode indexes to their indexes in the regulatory network.
        HashMap<Integer, Integer> mapNodeIndexToRealNodeIndex = new HashMap<Integer, Integer>();

        pos = 0;
        for (int i = 0; i < tempNodes.size(); i++) {
            if (tempNodes.get(i).isRealNode()) {
                mapNodeIndexToRealNodeIndex.put(i, pos);
                pos++;
            }
        }

        LinkedList<NetworkNode> networkNodesLinked = new LinkedList<NetworkNode>();

        for (int i = 0; i < tempNodes.size(); i++) {
            // Abort if it's logic gate
            if (!tempNodes.get(i).isRealNode()) {
                continue;
            }

            // If all inputs are real nodes we can use an activator-inhibitor-node
            boolean actInhibitPossible = true;
            for (Connection input : lines[i]) {
                if (!tempNodes.get(input.getSource()).isRealNode()) {
                    actInhibitPossible = false;
                    break;
                }
            }

            if (actInhibitPossible) {
                // We can create a pure act-inhibit-node

                // Create the function
                boolean[] activators = new boolean[linesIsNegated[i].length];

                for (int j = 0; j < activators.length; j++) {
                    activators[j] = !linesIsNegated[i][j];
                }

                BinaryBooleanFunction function = new ActivatorInhibitorFunction(activators);

                Connection[] inputsArray = new Connection[lines[i].length];
                for (int j = 0; j < inputsArray.length; j++) {
                    Point2D.Double[] path;
                    if (lines[i][j].getSource() == i) {
                        // If the node has itself as an input create a new path for the input
                        path = cyclePath(tempNodes.get(i).getRectangle());
                    } else {
                        // If not, we can reuse the path
                        path = lines[i][j].getPath();
                    }

                    inputsArray[j] = new Connection(mapNodeIndexToRealNodeIndex.get(lines[i][j].getSource()), path, lines[i][j].getPathOrigin(),
                            lines[i][j].getPathTarget());
                }

                // Assemble the NetworkNode
                networkNodesLinked.add(new NetworkNode(tempNodes.get(i).getLabel(), function, inputsArray, tempNodes.get(i).getRectangle(),
                        tempNodes.get(i).getInitialValue(), timeIndex));
            } else {
                // We have to create a full tree node

                // Check for cycles in the boolean parts of the graph
                if (isCyclic(new LinkedList<Integer>(), tempNodes, lines, i)) {
                    throw new IllegalArgumentException("A cycle was found in a boolean subtree of the input graph.");
                }
                // Further check (e.g. for multiple input to an NOT node are done in the getTree function

                // Collect all (real) input nodes to current node
                TreeSet<Integer> inputNodesSet = getAllConnections(tempNodes, lines, i);

                // Maps the index of an input node to the position of the input in the boolean function
                HashMap<Integer, Integer> mapNodeToInputIndex = new HashMap<Integer, Integer>();
                pos = 0;
                for (Integer node : inputNodesSet) {
                    mapNodeToInputIndex.put(node, pos);
                    pos++;
                }

                // Create an array of inputs with the definitive indexes
                Connection[] inputsArray = new Connection[inputNodesSet.size()];

                pos = 0;
                for (Integer j : inputNodesSet) {
                    // If the node has itself as an input create a new path for the input
                    if (i == j) {
                        inputsArray[pos] = new Connection(mapNodeIndexToRealNodeIndex.get(j), cyclePath(tempNodes.get(i).getRectangle()));
                    } else {
                        Connection realNodeInput = null;

                        // Search for the FIRST (of possibly many) inputs from the real node to this target node
                        for (Connection input : lines[i]) {
                            if (input.getSource() == j) {
                                realNodeInput = input;
                            }
                        }

                        if (realNodeInput == null) {
                            // We found a direct input from another node to this target node and can reuse the path of that line
                            inputsArray[pos] = new Connection(mapNodeIndexToRealNodeIndex.get(j), new Point2D.Double[0]);
                        } else {
                            // All input lines lead to boolean nodes and we have to draw an improvised straight line from the source to the
                            // target node
                            inputsArray[pos] = new Connection(mapNodeIndexToRealNodeIndex.get(j), realNodeInput.getPath(),
                                    realNodeInput.getPathOrigin(), realNodeInput.getPathTarget());
                        }
                    }

                    pos++;
                }

                // Assemble the NetworkNode
                TreeNode rootOfBF = getTree(mapNodeToInputIndex, tempNodes, lines, linesIsNegated, i, true);

                networkNodesLinked.add(new NetworkNode(tempNodes.get(i).getLabel(), new TreeBooleanFunction(rootOfBF), inputsArray,
                        tempNodes.get(i).getRectangle(), tempNodes.get(i).getInitialValue(), timeIndex));
            }
        }

        // Network nodes are finished, create arrays now
        NetworkNode[] networkNodes = new NetworkNode[networkNodesLinked.size()];
        networkNodesLinked.toArray(networkNodes);

        return networkNodes;
    }

    /**
     * Creates a path for input lines that lead from one node to the same node
     * 
     * This improvisation is used only if no path is provided by the file.
     * 
     * @param rectangle
     *            The rectangle of the node
     * @return An improvised path
     */
    public static Point2D.Double[] cyclePath(Rectangle2D.Double rectangle) {
        Point2D.Double[] path = new Point2D.Double[2];
        path[0] = new Point2D.Double(rectangle.getX() - 30, rectangle.getY() + rectangle.getHeight() / 2 - 20);
        path[1] = new Point2D.Double(rectangle.getX() - 30, rectangle.getY() + rectangle.getHeight() / 2 + 20);
        return path;
    }

    /**
     * Represent the type of the logic gate during the creation of the network.
     */
    private enum LogicGateType {
        AND, OR, NOT
    }

    /**
     * Return the LogicGateType of the current label or null if no LogicGateType is recognized. E.g. "||" => LogicGateType.OR, "node" =>
     * null.
     * 
     * @param label
     *            The label of the node that is to be inspected
     * @return The LogicGateType or null if none was recognized
     */
    private static LogicGateType getGateType(String label) {
        if (PatternLib.PADDEDENTIREANDPATTERN.matcher(label).matches()) {
            return LogicGateType.AND;
        }
        if (PatternLib.PADDEDENTIREORPATTERN.matcher(label).matches()) {
            return LogicGateType.OR;
        }
        if (PatternLib.PADDEDENTIRENOTPATTERN.matcher(label).matches()) {
            return LogicGateType.NOT;
        }

        return null;
    }

    /**
     * Recursively creates a tree of TreeNode nodes for the subtree of the graph given by the node index.
     * 
     * @param mapNodeToInputIndex
     *            Maps the index of a node to its index in the input to the boolean function
     * @param tempNodes
     *            The nodes of the yED graph.
     * @param lines
     *            The lines of the yED graph.
     * @param linesIsNegated
     *            Whether the lines of the yED graph are negated.
     * @param node
     *            The current node during the recursion
     * @param firstStep
     *            Whether it's the first step of the recursion or not
     * @return The root of the subtree
     */
    private static TreeNode getTree(HashMap<Integer, Integer> mapNodeToInputIndex, ArrayList<TempNode> tempNodes, Connection[][] lines,
            Boolean[][] linesIsNegated, int node, boolean firstStep) {
        if (firstStep) {
            // The current node is the start node: create act inhibit tree
            LinkedList<TreeNode> activators = new LinkedList<TreeNode>();
            LinkedList<TreeNode> inhibitor = new LinkedList<TreeNode>();

            for (int i = 0; i < lines[node].length; i++) {
                if (!linesIsNegated[node][i]) {
                    activators.add(getTree(mapNodeToInputIndex, tempNodes, lines, linesIsNegated, lines[node][i].getSource(), false));
                } else {
                    inhibitor.add(getTree(mapNodeToInputIndex, tempNodes, lines, linesIsNegated, lines[node][i].getSource(), false));
                }
            }

            return TreeNodeLib.getActInhibitTree(activators, inhibitor);
        } else if (!tempNodes.get(node).isRealNode()) {
            // The current node is a logic node: create an AND, OR or NOT node
            LinkedList<TreeNode> inputTrees = new LinkedList<TreeNode>();

            for (int i = 0; i < lines[node].length; i++) {
                if (!linesIsNegated[node][i]) {
                    inputTrees.add(getTree(mapNodeToInputIndex, tempNodes, lines, linesIsNegated, lines[node][i].getSource(), false));
                } else {
                    inputTrees.add(new NotNode(getTree(mapNodeToInputIndex, tempNodes, lines, linesIsNegated, lines[node][i].getSource(),
                            false)));
                }
            }

            switch (getGateType(tempNodes.get(node).getLabel())) {
            case AND:
                return TreeNodeLib.getAndTree(inputTrees);
            case OR:
                return TreeNodeLib.getOrTree(inputTrees);
            case NOT:
                if (inputTrees.size() == 0) {
                    return new ConstantNode(false);
                }

                if (inputTrees.size() > 1) {
                    throw new IllegalArgumentException("A NOT node with multiple inputs was found.");
                }

                return new NotNode(inputTrees.get(0));
            default:
                throw new IllegalArgumentException("The type of a logic gate could not be determined.");
            }

        } else {
            // The current node is an input node to the boolean function
            return new InputNode(mapNodeToInputIndex.get(node));
        }
    }

    /**
     * Determines whether the boolean tree of a real node is cyclic.
     * 
     * @param predecessors
     *            Predecessors that may not appear in the current subtree
     * @param tempNodes
     *            Necessary network information from the parsing function
     * @param lines
     *            Necessary network information from the parsing function
     * @param node
     *            The index of the node whose subtree is to be searched
     * @return true, iff the tree is cyclic
     */
    private static boolean isCyclic(LinkedList<Integer> predecessors, ArrayList<TempNode> tempNodes, Connection[][] lines, int node) {
        if (predecessors.contains(node)) {
            return true;
        }

        boolean result = false;
        predecessors.add(node);

        for (Connection input : lines[node]) {
            if (!tempNodes.get(input.getSource()).isRealNode()) {
                result = result || isCyclic(predecessors, tempNodes, lines, input.getSource());
            }
        }

        predecessors.removeLast();

        return result;
    }

    /**
     * Returns all indexes of input nodes of the subtree of given node.
     * 
     * @param tempNodes
     *            Necessary network information from the parsing function
     * @param lines
     *            Necessary network information from the parsing function
     * @param node
     *            The node whose subtree is to be searched
     * @return All indexes of input nodes of the subtree of given node
     */
    private static TreeSet<Integer> getAllConnections(ArrayList<TempNode> tempNodes, Connection[][] lines, int node) {
        TreeSet<Integer> result = new TreeSet<Integer>();

        for (Connection connection : lines[node]) {
            if (tempNodes.get(connection.getSource()).isRealNode()) {
                result.add(connection.getSource());
            } else {
                result.addAll(getAllConnections(tempNodes, lines, connection.getSource()));
            }
        }

        return result;
    }

    /**
     * Returns the first child node with the given name.
     * 
     * @param node
     *            The node whose children are to be searched
     * @param name
     *            The name to search for
     * @return The first matching child or null
     */
    private static Node getNamedChildNode(Node node, String name) {
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeName().equals(name)) {
                return node.getChildNodes().item(i);
            }
        }

        return null;
    }

    /**
     * Returns the a 'data' childnode with the given key
     * 
     * @param node
     *            The node whose children are to be searched
     * @param key
     *            The key to search for
     * @return The first matching child or null
     */
    private static Node getKeyedDataNode(Node node, String key) {
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeName().equals("data")
                    && node.getChildNodes().item(i).getAttributes().getNamedItem("key").getNodeValue().equals(key)) {
                return node.getChildNodes().item(i);
            }
        }

        return null;
    }
}
