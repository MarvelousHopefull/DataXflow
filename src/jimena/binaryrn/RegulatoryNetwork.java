package jimena.binaryrn;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import jimena.binarybf.actinhibitf.ActivatorInhibitorFunction;
import jimena.calculationparameters.ConvergenceParameters;
import jimena.calculationparameters.SimulationParameters;
import jimena.libs.BDDLib;
import jimena.libs.DoubleValue;
import jimena.libs.MathLib;
import jimena.libs.StandardNumberFormat;
import jimena.libs.StringLib;
import jimena.simulation.CalculationController;
import jimena.simulation.Simulator;
import jimena.simulation.StableSteadyState;
import jimena.simulationmethods.SimulationMethod;
import jimena.sssearcher.SSSearchResult;
import jimena.sssearcher.SSSearcher;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

/**
 * Implements a binary regulatory network.
 *
 * Please note that this class is NOT THREADSAFE. For multithreaded computation just copy the network with the clone[...]() function.
 *
 * @author Stefan Karl, Department of Bioinformatics, University of Würzburg, stefan[dot]karl[at]uni-wuerzburg[dot]de
 *
 */
public class RegulatoryNetwork implements Serializable {
    /**
     * A private function that checks the plausibility of some often used parameters.
     *
     * @param stabilityMaxDiff
     *            The epsilon neighborhood around a former value which must not be left to assume stability has the diameter
     *            stabilityMaxDiff*2
     * @param stabilityMinTime
     *            Time the node has to spend in an epsilon neighborhood around a former value to assume stability
     * @param simulationsPerConnection
     *            Number of simulations per connection
     * @param method
     *            The method to apply for the simulation
     * @param dt
     *            The time step in the simulation
     * @param maxt
     *            The maximum simulated time
     */
    private static void checkParameters(double stabilityMaxDiff, double stabilityMinTime, int simulationsPerConnection,
            SimulationMethod method, double dt, double maxt) {
        if (stabilityMaxDiff < 0 || stabilityMinTime <= 0 || simulationsPerConnection <= 0 || dt <= 0 || maxt <= 0) {
            throw new IllegalArgumentException();
        }

        if (method == null) {
            throw new NullPointerException();
        }
    }

    private static final long serialVersionUID = -3637940236128103506L;

    private NetworkNode[] networkNodes = new NetworkNode[0];

    private transient HashSet<RegulatoryNetworkObserver> observers = new HashSet<RegulatoryNetworkObserver>();

    private DoubleValue timeIndex = new DoubleValue(0); // The same time index is held by the nodes.

    /**
     * Creates a new empty regulatory network.
     */
    public RegulatoryNetwork() {
    }

    /**
     * Creates a regulatory network from an array of network nodes. The observers are empty and the time index is set to 0. The
     *
     * @param networkNodes
     *            The network nodes to use
     * @param timeIndex
     */
    public RegulatoryNetwork(NetworkNode[] networkNodes, DoubleValue timeIndex) {
        if (networkNodes == null || timeIndex == null) {
            throw new NullPointerException();
        }

        this.networkNodes = networkNodes;
        this.observers = new HashSet<RegulatoryNetworkObserver>();
        this.timeIndex = timeIndex;
    }

    /**
     * Disables the given connection.
     *
     * @param node
     *            The node whose line is to be removed.
     * @param connection
     *            The number of the connection which is to be removed.
     */
    public void addNullMutation(int node, int connection) {
        try {
            networkNodes[node].getConnections()[connection].toString();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("The specified connection does not exist in the network.");
        }
        networkNodes[node].getFunction().disableConnection(connection);
    }

    /**
     * Deletes several connections from the network.
     *
     * @param connections
     *            The connections to remove.
     */
    public void addNullMutation(List<NetworkConnection> connections) {
        for (NetworkConnection connection : connections) {
            addNullMutation(connection);
        }
    }

    /**
     * Disables the given connection.
     *
     * @param connection
     *            The connection which is to be removed.
     */
    public void addNullMutation(NetworkConnection connection) {
        addNullMutation(connection.getNode(), connection.getPosition());
    }

    /**
     * Adds an observer to this binary regulatory network. If the observer already exists it will be ignored.
     *
     * @param observer
     *            The observer to add
     */
    public void addObserver(RegulatoryNetworkObserver observer) {
        if (observer == null) {
            throw new NullPointerException();
        }

        observers.add(observer);
    }

    /**
     * Creates a deep copy of this regulatory network with a new time index, a new log and no observers.
     *
     * @return The copied network.
     */
    public RegulatoryNetwork cloneClean() {
        DoubleValue timeIndex = new DoubleValue(0);
        NetworkNode[] copiedNetworkNodes = new NetworkNode[size()];

        for (int i = 0; i < copiedNetworkNodes.length; i++) {
            copiedNetworkNodes[i] = networkNodes[i].clone(timeIndex, false);
        }

        return new RegulatoryNetwork(copiedNetworkNodes, timeIndex);
    }

    /**
     * Returns a stable state searcher for the given initial values.
     *
     * @param startValues
     *            initial values.
     * @param parameters
     *            Parameters for the convergence function.
     * @return A stable state searcher.
     */
    public StableSteadyState stableStateSearcher(double[] startValues, ConvergenceParameters parameters) {
        // Checks done by the functions

        RegulatoryNetwork searchingNetwork = cloneClean();
        searchingNetwork.setValues(startValues);

        return new StableSteadyState(searchingNetwork, parameters);
    }

    /**
     * Returns a index which increases with the number of cycles the given network node is a member of.
     *
     * If favorSmallCycles is set, small cycles increase the index more than large cycles (the increase is 1/(2^n) where n is the length of
     * the cycle). If it is not set, large and small cycles increase the index by 1.
     *
     * @param node
     *            The node whose cycle membership is to be determined.
     * @param favorSmallCycles
     *            Whether small cycles increase the index more than large cycles (see above).
     * @return An index which increases with the number of cycles the given network node is a member of.
     */
    public double cycleIndex(int node, boolean favorSmallCycles) {
        if (node < 0 || node > networkNodes.length) {
            throw new IllegalArgumentException("The specified node does not exist in the network.");
        }
        LinkedList<Integer> visitedNodes = new LinkedList<Integer>();
        visitedNodes.add(node);
        return cycleIndex(visitedNodes, node, favorSmallCycles);
    }

    private double cycleIndex(LinkedList<Integer> visitedNodes, int node, boolean favorSmallCycles) {
        double result = 0;

        for (Integer source : networkNodes[node].getConnectionSources()) {
            if (visitedNodes.peekFirst() == source) {
                if (favorSmallCycles) {
                    result += 1.0 / Math.pow(2, visitedNodes.size());
                } else {
                    result += 1.0;
                }
                continue;
            }
            if (visitedNodes.contains(source)) {
                continue;
            }
            visitedNodes.add(source);
            result += cycleIndex(visitedNodes, source, favorSmallCycles);
            visitedNodes.removeLast();
        }

        return result;
    }

    /**
     * Returns the stable steady states of the network if a discrete model is assumed.
     *
     * @return An ArrayList of byte arrays with the stable steady states.
     */
    @SuppressWarnings("unchecked")
    public synchronized ArrayList<byte[]> discreteStableSteadyStates() {
        /*
         * We construct a BDD for the boolean expression that is true iff the results of all transition functions match the values of the
         * nodes they affect: (x1 <-> f1(x1, x2, ...) ^ x2 <-> f2(...)^ ...) where xi are the nodes and fi the transition functions.
         */
        BDDFactory bddFactory = BDDLib.getBDDFactory(); // A shortcut to the BDD factory

        // Create one BDD for each node
        bddFactory.setVarNum(size());
        BDD[] nodes = new BDD[size()];
        for (int i = 0; i < size(); i++) {
            nodes[i] = bddFactory.ithVar(i);
        }

        // Construct the expression by ANDing the subexpressions (xi <-> fi(x1, x2, ...) to the "true" literal
        BDD network = bddFactory.one();

        for (int i = 0; i < size(); i++) {
            // Create an array of input BDDs for the function fi
            BDD[] inputs = new BDD[networkNodes[i].getConnections().length];
            for (int j = 0; j < networkNodes[i].getConnections().length; j++) {
                inputs[j] = nodes[networkNodes[i].getConnections()[j].getSource()];
            }

            // Create a BDD for the function fi
            BDD functionBDD = networkNodes[i].getFunction().createBDD(inputs, bddFactory);

            // Create a BDD for the subexpression (xi <-> fi(x1, x2, ...)
            functionBDD = nodes[i].biimp(functionBDD);

            // Concatenate the function BDDs
            network.andWith(functionBDD);
        }

        // The arrays contain -1 (= arbitrary value) components, we expand those to full arrays.
        // E.g. (-1,1,0) => (1,1,0),(0,1,0)
        return MathLib.bddAllsatResultToArray(network.allsat());
    }

    /**
     * Creates a string representation of the network ready to import in SQUAD. Throws an exception if network is not compatible with SQUAD.
     *
     * @return A SQUAD compatible representation of the network..
     */
    public String exportSQUAD() {
        String result = new String();
        for (int i = 0; i < networkNodes.length; i++) {
            result = result + exportSQUAD(i);
        }
        return result;
    }

    /**
     * Creates a string representation of the specified node ready to import in SQUAD. Throws an exception if the node is not compatible
     * with SQUAD.
     *
     * @param node
     *            The node to export.
     * @return A SQUAD compatible representation of the node.
     */
    public String exportSQUAD(int node) {
        if (node < 0 || node >= networkNodes.length) {
            throw new IllegalArgumentException("The specified node does not exist.");
        }

        String result = new String();
        if (networkNodes[node].getFunction() instanceof ActivatorInhibitorFunction) {
            boolean[] activators = ((ActivatorInhibitorFunction) networkNodes[node].getFunction()).getActivators();
            for (int i = 0; i < networkNodes[node].getConnections().length; i++) {
                result = result + networkNodes[networkNodes[node].getConnections()[i].getSource()].getName()
                        + (activators[i] ? " -> " : " -| ") + networkNodes[node].getName() + "\n";
            }
        } else {
            throw new IllegalArgumentException("The specified node is not an activator-inhibitor-node.");
        }
        return result;
    }

    /**
     * Returns the given network connection if it exists.
     *
     * @param conection
     *            The connection to return.
     * @return The requested connection.
     */
    public Connection getConnection(NetworkConnection conection) {
        return networkNodes[conection.getNode()].getConnections()[conection.getPosition()];
    }

    /**
     * Returns the set of connection indexes to a node whose source nodes have a given name
     *
     * @param node
     *            The node whose connections are to be searched
     * @param name
     *            The connection name to search for
     * @return The set of connection indexes to a node whose source nodes have a given name
     */
    public TreeSet<Integer> getConnectionPositionsByName(int node, String name) {
        if (node < 0 || node >= networkNodes.length) {
            throw new IllegalArgumentException("The specified network node was not found in the network.");
        }

        if (name == null) {
            throw new NullPointerException();
        }

        TreeSet<Integer> result = new TreeSet<Integer>();

        for (int i = 0; i < networkNodes[node].getConnections().length; i++) {
            if (StringLib.equalsTrimmed(getConnectionSourceName(node, i), name)) {
                result.add(i);
            }
        }

        return result;
    }

    /**
     * Returns the set of connection indexes to a node whose source nodes have a given name
     *
     * @param target
     *            The node whose inputs are to be searched
     * @param source
     *            The source node name to search for
     * @return A set of the matching inputs' indices
     */
    public TreeSet<Integer> getConnectionPositionsByName(String target, String source) {
        return getConnectionPositionsByName(getNodeIndexByName(target), source);
    }

    /**
     * Returns all connections in the network.
     *
     * @return All connections of the network.
     */
    public ArrayList<NetworkConnection> getConnections() {
        ArrayList<NetworkConnection> result = new ArrayList<NetworkConnection>();
        if (getFirstConnection() == null) {
            return result;
        }
        NetworkConnection nextConnection = getFirstConnection();
        while (nextConnection != null) {
            result.add(nextConnection);
            nextConnection = getNextConnection(nextConnection);
        }
        return result;
    }

    /**
     * Returns all connections in the network.
     *
     * @return All connections of the network.
     */
    public String[] getConnectionsStrings() {
        if (getFirstConnection() == null) {
            return new String[0];
        }
        NetworkConnection nextConnection = getFirstConnection();
        String[] result = new String[getConnections().size()];
        int index = 0;
        while (nextConnection != null) {
            result[index] = (getConnectionString(nextConnection));
            nextConnection = getNextConnection(nextConnection);
            index++;
        }
        return result;
    }

    /**
     * Returns the connections from a given node in the network.
     *
     * @param fromNode
     *            Connections originating in this node are returned.
     *
     * @return All connections from the given node.
     */
    public ArrayList<NetworkConnection> getConnectionsBySource(int fromNode) {
        if (fromNode < 0 || fromNode >= networkNodes.length) {
            throw new IllegalArgumentException("The specified network node " + fromNode + " was not found in the network.");
        }
        ArrayList<NetworkConnection> result = new ArrayList<NetworkConnection>();
        if (getFirstConnection() == null) {
            return result;
        }
        NetworkConnection nextConnection = getFirstConnection();
        while (nextConnection != null) {
            if (networkNodes[nextConnection.getNode()].getConnections()[nextConnection.getPosition()].getSource() != fromNode) {
                nextConnection = getNextConnection(nextConnection);
                continue;
            }
            result.add(nextConnection);
            nextConnection = getNextConnection(nextConnection);
        }
        return result;
    }

    /**
     * Returns all connections starting from a given node and leading to a given node.
     *
     * @param fromNode
     *            The node the connections start from.
     * @param toNode
     *            The node the connections lead to.
     * @return All connections between the two nodes.
     */
    public ArrayList<NetworkConnection> getConnectionsBySourceAndTarget(int fromNode, int toNode) {
        ArrayList<NetworkConnection> result = new ArrayList<NetworkConnection>();

        ArrayList<NetworkConnection> from = getConnectionsBySource(fromNode);
        ArrayList<NetworkConnection> to = getConnectionsByTarget(toNode);

        for (NetworkConnection connection : from) {
            for (NetworkConnection connection2 : to) {
                if (connection.getNode() == connection2.getNode() && connection.getPosition() == connection2.getPosition()) {
                    result.add(connection);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns the connections to a given node in the network.
     *
     * @param toNode
     *            Connections targeting this node are returned.
     * @return All connections to the given node.
     */
    public ArrayList<NetworkConnection> getConnectionsByTarget(int toNode) {
        if (toNode < 0 || toNode >= networkNodes.length) {
            throw new IllegalArgumentException("The specified network node was not found in the network.");
        }
        ArrayList<NetworkConnection> result = new ArrayList<NetworkConnection>();

        for (int i = 0; i < networkNodes[toNode].getConnections().length; i++) {
            result.add(new NetworkConnection(toNode, i));
        }

        return result;
    }

    /**
     * Return the index of the node who is the index-th connection to the specified node.
     *
     * @param node
     *            The node whose connections are considered
     * @param position
     *            Index of the connection
     * @return Index of the source of the connection
     */
    public int getConnectionSourceIndex(int node, int position) {
        if (node < 0 || node >= networkNodes.length) {
            throw new IllegalArgumentException("The specified network node was not found in the network.");
        }

        if (node < 0 || position >= networkNodes[node].getConnections().length) {
            throw new IllegalArgumentException("The specified input was not found in the node.");
        }

        return networkNodes[node].getConnections()[position].getSource();
    }

    /**
     * Return the index of the source to the specified connection.
     *
     * @param connection
     *            The connection to consider
     * @return Index of the source of the connection
     */
    public int getConnectionSourceIndex(NetworkConnection connection) {
        return getConnectionSourceIndex(connection.getNode(), connection.getPosition());
    }

    /**
     * Return the name of the node who is the index-th input to the specified node.
     *
     * @param node
     *            The node whose connections are considered
     * @param position
     *            Index of the connection
     * @return Name of the source of the connection
     */
    public String getConnectionSourceName(int node, int position) {
        if (node < 0 || node >= networkNodes.length) {
            throw new IllegalArgumentException("The specified network node was not found in the network.");
        }

        if (node < 0 || position >= networkNodes[node].getConnections().length) {
            throw new IllegalArgumentException("The specified connection was not found in the node.");
        }

        return networkNodes[getConnectionSourceIndex(node, position)].getName();
    }

    /**
     * Return the name of the source to the specified connection.
     *
     * @param connection
     *            The connection to consider
     * @return Name of the source of the connection
     */
    public String getConnectionSourceName(NetworkConnection connection) {
        return getConnectionSourceName(connection.getNode(), connection.getPosition());
    }

    /**
     * Returns a String representation of a list of given connections.
     *
     * @param connections
     *            The connections.
     * @return Their string representation
     */
    public String getConnectionString(ArrayList<NetworkConnection> connections) {
        String result = "{ ";
        for (int i = 0; i < connections.size() - 1; i++) {
            result = result + getConnectionString(connections.get(i)) + ", ";
        }
        if (connections.size() != 0) {
            result = result + getConnectionString(connections.get(connections.size() - 1));
        }
        return result + " }";
    }

    /**
     * Returns a String representation of a given connection.
     *
     * @param connection
     *            The connection.
     * @return Its string representation
     */
    public String getConnectionString(NetworkConnection connection) {
        return getConnectionString(connection, "->");
    }

    /**
     * Returns a String representation of a given connection.
     *
     * @param connection
     *            The connection.
     * @param sep
     *            The seperator between the connections.
     * @return Its string representation
     */
    public String getConnectionString(NetworkConnection connection, String sep) {
        return "\"" + networkNodes[networkNodes[connection.getNode()].getConnections()[connection.getPosition()].getSource()].getName()
                + "\"" + sep + "\"" + networkNodes[connection.getNode()].getName() + "\"";
    }

    /**
     * Returns the first element in a list of all connections in the network.
     *
     * @return The first connection in the network or null if none exist.
     */
    public NetworkConnection getFirstConnection() {
        for (int i = 0; i < networkNodes.length; i++) {
            if (networkNodes[i].getConnections().length != 0) {
                return new NetworkConnection(i, 0);
            }
        }
        return null;
    }

    /**
     * Returns a string representation of the function of a node.
     *
     * @param node
     *            The node to consider.
     * @return String representation of the function.
     */
    public String getFunctionString(int node) {
        if (node < 0 || node >= networkNodes.length) {
            throw new IllegalArgumentException("The specified node is not valid.");
        }

        String[] nodeNames = getNodeNames();

        String[] connectionNodeNames = new String[networkNodes[node].getConnections().length];
        for (int j = 0; j < networkNodes[node].getConnections().length; j++) {
            connectionNodeNames[j] = nodeNames[networkNodes[node].getConnections()[j].getSource()];
        }

        return networkNodes[node].getName() + " = " + networkNodes[node].getFunction().getFunctionString(connectionNodeNames, false);
    }

    /**
     * Returns a Odefy compatible string representation of the function of a node.
     *
     * @param node
     *            The node to consider.
     * @return String representation of the function.
     */
    public String getFunctionStringOdefyCompatible(int node) {
        if (node < 0 || node >= networkNodes.length) {
            throw new IllegalArgumentException("The specified node is not valid.");
        }

        String[] nodeNames = getNodeNames();

        // Create an array of input BDDs for the function fi
        String[] connectionNodeNames = new String[networkNodes[node].getConnections().length];
        for (int j = 0; j < networkNodes[node].getConnections().length; j++) {
            connectionNodeNames[j] = nodeNames[networkNodes[node].getConnections()[j].getSource()];
        }

        return "'" + networkNodes[node].getName() + " = " + networkNodes[node].getFunction().getFunctionString(connectionNodeNames, true)
                + "'";
    }

    /**
     * Return the network nodes of this networks.
     *
     * @return Network nodes
     */
    public NetworkNode[] getNetworkNodes() {
        return networkNodes;
    }

    /**
     * Returns a string representation of network.
     *
     * @return A string representation of network.
     */
    public String getNetworkString() {
        String result = new String();

        for (int i = 0; i < networkNodes.length; i++) {
            result = result + "\n" + getFunctionString(i);
        }

        return result;
    }

    /**
     * Returns a Odefy compatible string representation of network.
     *
     * @return A string representation of network.
     */
    public String getNetworkStringOdefyCompatible() {
        String result = "expr = {";

        for (int i = 0; i < networkNodes.length; i++) {
            result = result + "\n" + getFunctionStringOdefyCompatible(i);
            if (i != networkNodes.length - 1) {
                result = result + ", ";
            }
        }
        result = result + "}";

        return result;
    }

    /**
     * Returns the next connection in a list of all connections in the network.
     *
     * @param node
     *            The node of the preceding connection
     * @param position
     *            The position of the preceding connection
     *
     * @return The next connection or null if the end if the list was reached.
     * */
    public NetworkConnection getNextConnection(int node, int position) {
        if (!isValidConnection(node, position)) {
            throw new IllegalArgumentException("The specified connection does not exist." + "(" + node + ", " + position + ")");
        }
        if ((position + 1) >= networkNodes[node].getConnections().length) {
            for (int i = node + 1; i < networkNodes.length; i++) {
                if (networkNodes[i].getConnections().length != 0) {
                    return new NetworkConnection(i, 0);
                }
            }
            return null;
        } else {
            return new NetworkConnection(node, position + 1);
        }
    }

    /**
     * Returns the next connection in a list of all connections in the network.
     *
     * @param connections
     *            The preceding connection
     *
     * @return The next connection or null if the end if the list was reached.
     * */
    public NetworkConnection getNextConnection(NetworkConnection connections) {
        return (getNextConnection(connections.getNode(), connections.getPosition()));
    }

    /**
     * Returns the next connection in a list of all connections in the network.
     *
     * @param node
     *            The node of the preceding connection
     * @param position
     *            The position of the preceding connection
     *
     * @return The next connection or the first one if the end if the list was reached.
     * */
    public NetworkConnection getNextConnectionWrap(int node, int position) {
        if (!isValidConnection(node, position)) {
            throw new IllegalArgumentException("The specified connection does not exist." + "(" + node + ", " + position + ")");
        }
        if ((position + 1) >= networkNodes[node].getConnections().length) {
            for (int i = node + 1; i < networkNodes.length; i++) {
                if (networkNodes[i].getConnections().length != 0) {
                    return new NetworkConnection(i, 0);
                }
            }
            return getFirstConnection();
        } else {
            return new NetworkConnection(node, position + 1);
        }
    }

    /**
     * Returns the next connection in a list of all connections in the network.
     *
     * @param connection
     *            The preceding connection
     *
     * @return The next connection or the first one if the end if the list was reached.
     * */
    public NetworkConnection getNextConnectionWrap(NetworkConnection connection) {
        return (getNextConnectionWrap(connection.getNode(), connection.getPosition()));
    }

    /**
     * Returns the network node with a given name.
     *
     * @param name
     *            The name to search for
     * @return The network node with that name or an exception if no node was found
     */
    public NetworkNode getNodeByName(String name) {
        return networkNodes[getNodeIndexByName(name)];
    }

    /**
     * Returns the index of the network node with a given name.
     *
     * @param name
     *            The name to search for
     * @return The index of the network node with that name or an exception if no node was found
     */
    public int getNodeIndexByName(String name) {
        for (int i = 0; i < networkNodes.length; i++) {
            if (StringLib.equalsTrimmed(networkNodes[i].getName(), name)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Node " + name + " not found.");
    }

    /**
     * Gets the names of the nodes.
     *
     * @return An array of the names of the nodes.
     */
    public String[] getNodeNames() {
        String[] names = new String[size()];
        for (int i = 0; i < size(); i++) {
            names[i] = networkNodes[i].getName();
        }
        return names;
    }

    /**
     * Returns the observers of the network.
     *
     * @return The observers of the network in a HashSet.
     */
    public HashSet<RegulatoryNetworkObserver> getObservers() {
        return observers;
    }

    /**
     * Returns the time index of this network.
     *
     * @return The time index
     */
    public DoubleValue getTimeIndex() {
        return timeIndex;
    }

    /**
     * Returns the values of the nodes. You should not use this method to get a single value, but query the node instead.
     *
     * @return An arrays of the values of the nodes.
     */
    public double[] getValues() {
        double[] result = new double[size()];
        for (int i = 0; i < size(); i++) {
            result[i] = networkNodes[i].getValue();
        }
        return result;
    }

    /**
     * Returns the values of the nodes. You should not use this method to get a single value, but query the node instead.
     *
     * @return An arrays of the values of the nodes.
     */
    public double[] getValues(double[] target) {
        for (int i = 0; i < size(); i++) {
            target[i] = networkNodes[i].getValue();
        }
        return target;
    }

    public boolean hasLoop(int node) {
        return getConnectionsBySourceAndTarget(node, node).size() != 0;
    }

    /**
     * Returns whether the network is empty, i.e. it has no nodes.
     *
     * @return True is the network is empty
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns whether the given connection is a loop.
     *
     * @param connection
     *            The connection to analyze.
     * @return True, if the connection is a loop
     */
    public boolean isLoop(NetworkConnection connection) {
        return connection.getNode() == getConnectionSourceIndex(connection);
    }

    /**
     * Checks whether the given connection exists in the network.
     *
     * @param node
     *            The node of the connection to check for.
     * @param position
     *            The position of the connection to check for.
     * @return True, if the connection exists.
     */
    public boolean isValidConnection(int node, int position) {
        if (node < 0 || node >= networkNodes.length || position < 0 || position >= networkNodes[node].getConnections().length) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Checks whether the given connection exists in the network.
     *
     * @param connection
     *            The connection to check for.
     * @return True, if the connection exists.
     */
    public boolean isValidConnection(NetworkConnection connection) {
        return isValidConnection(connection.getNode(), connection.getPosition());
    }

    /**
     * Takes the nodes and the time index from a loaded network, keeping the current observers.
     *
     * @param storedNetwork
     *            A network loaded from a file.
     */
    public void loadNetwork(RegulatoryNetwork storedNetwork) {
        // This automatically checks for storedNetwork = null
        networkNodes = storedNetwork.getNetworkNodes();
        timeIndex = storedNetwork.getTimeIndex();
        notifyObserversOfChangedNetwork();
    }

    /**
     * Parses a yED file into this network. The current content will be replaced. The time index is set to 0 and the observer are preserved.
     *
     * @param file
     *            yED file
     */
    public void loadYEdFile(File file) {
        // Checks done by the parseYEdFile function
        timeIndex = new DoubleValue(0);
        try {
            networkNodes = RegulatoryNetworkLib.parseYEdFile(file, timeIndex);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        notifyObserversOfChangedNetwork();
    }

    /**
     * Creates a log entries with the current values in all the nodes.
     */
    public void log() {
        for (NetworkNode node : networkNodes) {
            node.log();
        }
    }

    /**
     * The value of all nodes is set to 0 if it is < 0.5 and to 1 otherwise.
     */
    public void makeDiscrete() {
        for (NetworkNode node : networkNodes) {
            node.makeDiscrete();
        }
    }

    /**
     * Notifies all observers that a change of the network structure has taken place.
     */
    public void notifyObserversOfChangedNetwork() {
        for (RegulatoryNetworkObserver observer : observers) {
            observer.notifyNetworkChanged();
        }
    }

    /**
     * Notifies all observers that a change of the network values has taken place.
     */
    public void notifyObserversOfChangedValues() {
        for (RegulatoryNetworkObserver observer : observers) {
            observer.notifyValuesChanged();
        }
    }

    /**
     * Returns the number of input nodes in the network.
     *
     * @return Number of inputs.
     */
    public int numberOfInputs() {
        int counter = 0;

        for (int i = 0; i < size(); i++) {
            if (getConnectionsByTarget(i).size() == 0) {
                counter++;
            }
        }
        return counter;
    }

    /**
     * Returns the number of loops in the network.
     *
     * @return Number of loops.
     */
    public int numberOfLoops() {
        int counter = 0;
        for (int i = 0; i < networkNodes.length; i++) {
            for (int j = 0; j < networkNodes[i].getConnections().length; j++) {
                if (networkNodes[i].getConnections()[j].getSource() == i) {
                    counter++;
                }
            }
        }
        return counter;
    }

    /**
     * Prints a string representation of the network.
     */
    public void printNetwork() {
        System.out.println(getNetworkStringOdefyCompatible());
    }

    /**
     * Prints the current values of the network nodes.
     */
    public void printNetworkState() {
        int padding = StringLib.maxStringLength(getNodeNames());

        for (NetworkNode node : networkNodes) {
            System.out.println(StringLib.padLeft(node.getName(), padding) + ": " + node.getValue());
        }
    }

    /**
     * Removes all perturbation from the network.
     *
     */
    public void removeAllPerturbations() {
        for (NetworkNode node : networkNodes) {
            node.getPerturbations().clear();
        }
    }

    /**
     * Removes an observer to this binary regulatory network. If the observer is not registered the command will be ignored.
     *
     * @param observer
     *            The observer to remove
     */
    public void removeObserver(RegulatoryNetworkObserver observer) {
        if (observer == null) {
            throw new NullPointerException();
        }

        observers.remove(observer);
    }

    /**
     * Resets all values to the initial values, resets the logs and sets the time index to 0.
     */
    public void reset() {
        for (NetworkNode node : networkNodes) {
            node.reset();
            node.resetLog();
        }
        timeIndex.setValue(0D);
        notifyObserversOfChangedValues();
    }

    /**
     * Removes all mutations from the network.
     */
    public void restore() {
        for (NetworkNode node : networkNodes) {
            node.getFunction().restore();
        }
    }

    /**
     * Sets the names of all nodes in the network.
     *
     * @param names
     *            The names to set.
     */
    public void setNames(ArrayList<String> names) {
        setNames(names.toArray(new String[names.size()]));
    }

    /**
     * Sets the names of all nodes in the network.
     *
     * @param names
     *            The names to set.
     */
    public void setNames(String[] names) {
        if (names.length != networkNodes.length) {
            throw new IllegalArgumentException("The arrays must be of equal size.");
        }
        for (int i = 0; i < names.length; i++) {
            if (names[i] == null) {
                throw new IllegalArgumentException("A node name may not be null.");
            }
            for (int j = i + 1; j < names.length; j++) {
                if (names[i].equals(names[j])) {
                    throw new IllegalArgumentException("All node names must be different.");
                }
            }
            networkNodes[i].setName(names[i]);
        }
    }

    /**
     * Sets the network nodes of this networks.
     *
     * @param networkNodes
     *            The network nodes to set.
     */
    public void setNetworkNodes(NetworkNode[] networkNodes) {
        this.networkNodes = networkNodes;
    }

    /**
     * Sets the observers of the network.
     *
     * @param observers
     *            The observers of the network in a HashSet.
     */
    public void setObservers(HashSet<RegulatoryNetworkObserver> observers) {
        if (observers == null) {
            throw new NullPointerException();
        }

        this.observers = observers;
    }

    /**
     * Constructs a set of all the node indices.
     *
     * @return A set of all the node indices.
     */
    public TreeSet<Integer> setOfAllNodeIndices() {
        TreeSet<Integer> allIndices = new TreeSet<Integer>();
        for (int i = 0; i < size(); i++) {
            allIndices.add(i);
        }
        return allIndices;
    }

    /**
     * Sets the values of the nodes.
     *
     * @param values
     *            An arrays of the values of the nodes to set.
     */
    public void setValues(double[] values) {
        // This automatically checks for values = null, the values themselves are checked by the setValue function
        for (int i = 0; i < values.length; i++) {
            networkNodes[i].setValue(values[i]);
        }
    }

    /**
     * Simulate the the network for a given amount of time.
     *
     * @param method
     *            The method to use
     * @param dt
     *            The time step
     * @param maxT
     *            The duration of the simulation (If the simulation uses discrete steps (maxt/dt) steps are done)
     * @param maxSpeed
     *            Maximum speed of the simulation
     * @param minSimulationTimeBetweenLogs
     *            Minimum (simulation) time between two log entries during the simulation.
     * @param calculationController
     *            A calculation controller or null for an uncontrolled calculation
     */
    public void simulate(SimulationMethod method, double dt, double maxT, double maxSpeed, double minSimulationTimeBetweenLogs,
            CalculationController calculationController) {
        simulate(new SimulationParameters(method, dt, maxT, calculationController), maxSpeed, minSimulationTimeBetweenLogs);
    }

    /**
     * Simulate the the network for a given amount of time.
     *
     * @param parameters
     *            Parameters of the simulation
     * @param maxSpeed
     *            Maximum speed of the simulation
     * @param minSimulationTimeBetweenLogs
     *            Minimum (simulation) time between two log entries during the simulation.
     */
    public void simulate(SimulationParameters parameters, double maxSpeed, double minSimulationTimeBetweenLogs) {
        Simulator simulator = new Simulator(parameters, this, maxSpeed, minSimulationTimeBetweenLogs);
        simulator.run(); // No new thread here
    }

    /**
     * Returns the number of node in the network.
     *
     * @return Number of nodes
     */
    public int size() {
        return networkNodes.length;
    }

    /**
     * Sorts the network nodes by their label.
     */
    public void sortNetworkNodes() {
        // Bubble sort
        boolean change = true;
        while (change) {
            change = false;
            for (int i = 0; i < this.size() - 1; i++) {
                if (getNetworkNodes()[i + 1].getName().compareTo(getNetworkNodes()[i].getName()) < 0) {
                    change = true;

                    NetworkNode tempNode = getNetworkNodes()[i + 1];
                    getNetworkNodes()[i + 1] = getNetworkNodes()[i];
                    getNetworkNodes()[i] = tempNode;

                    for (int j = 0; j < size(); j++) {
                        for (int k = 0; k < getNetworkNodes()[j].getConnections().length; k++) {
                            if (getNetworkNodes()[j].getConnections()[k].getSource() == i) {
                                getNetworkNodes()[j].getConnections()[k].setSource(i + 1);
                                continue;
                            }
                            if (getNetworkNodes()[j].getConnections()[k].getSource() == i + 1) {
                                getNetworkNodes()[j].getConnections()[k].setSource(i);
                                continue;
                            }
                        }
                    }
                }
            }
        }
        notifyObserversOfChangedNetwork();
    }

    /**
     * Calculates the stable state that is assumed starting from a set of given values.
     *
     * @param startValues
     *            The values to start from
     * @param parameters
     *            Parameters of the search
     * @return An array with the results or null of the simulation did not converge in time.
     * @throws Exception
     */
    public double[] stableSteadyState(double[] startValues, ConvergenceParameters parameters) throws Exception {
        // Checks done by the called functions.

        StableSteadyState stableSteadyState = stableStateSearcher(startValues, parameters);

        try {
            return stableSteadyState.call().getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Calculates a subset of the stable steady states of this node by randomly choosing a set a set of start values and simulation from
     * there.
     *
     * @param parameters
     *            The search parameters.
     * @param maxTime
     *            The maximum time to search in ms
     * @param ssSearcher
     *            The stable steady state searcher to use
     *
     * @return An ArrayList with the SSS found by the calculation.
     */
    public ArrayList<double[]> stableSteadyStates(ConvergenceParameters parameters, long maxTime, SSSearcher ssSearcher) {
        return stableSteadyStatesWithBasins(parameters, maxTime, ssSearcher).getResults();
    }

    /**
     * Calculates a subset of the stable steady states of this node by randomly choosing a set a set of start values and simulation from
     * there.
     *
     * @param numberOfStarts
     *            The number of start vectors to simulate
     * @param parameters
     *            The search parameters.
     * @param ssSearcher
     *            The stable steady state searcher to use
     *
     * @return An ArrayList with the SSS found by the calculation.
     */
    public ArrayList<double[]> stableSteadyStates(long numberOfStarts, ConvergenceParameters parameters, SSSearcher ssSearcher) {
        return ssSearcher.searchSSStates(this, numberOfStarts, parameters).getResults();
    }

    /**
     * Calculates a subset of the stable steady states of this node by randomly choosing a set a set of start values and simulation from
     * there. Also returns the basins of attraction.
     *
     * @param parameters
     *            The search parameters.
     * @param maxTime
     *            The maximum time to search in ms
     * @param ssSearcher
     *            The stable steady state searcher to use
     *
     * @return An ArrayList with the SSS found by the calculation.
     */
    public SSSearchResult stableSteadyStatesWithBasins(ConvergenceParameters parameters, long maxTime, SSSearcher ssSearcher) {
        return ssSearcher.searchSSStates(this, parameters, maxTime);
    }

    /**
     * Calculates a subset of the stable steady states of this node by randomly choosing a set a set of start values and simulation from
     * there.
     *
     * @param numberOfStarts
     *            The number of start vectors to simulate
     * @param parameters
     *            The search parameters.
     * @param ssSearcher
     *            The stable steady state searcher to use
     *
     * @return An ArrayList with the SSS found by the calculation.
     */
    public SSSearchResult stableSteadyStatesWithBasins(long numberOfStarts, ConvergenceParameters parameters, SSSearcher ssSearcher) {
        return ssSearcher.searchSSStates(this, numberOfStarts, parameters);
    }

    /**
     * Returns the sum of the number of all connections to all network nodes.
     *
     * @return Number of connections
     */
    public int sumOfConnections() {
        int numberOfConnections = 0;
        for (NetworkNode node : networkNodes) {
            numberOfConnections += node.getConnections().length;
        }
        return numberOfConnections;
    }

    /**
     * Switches the sources of two connections.
     *
     * @param connection1
     *            First connection of the pair.
     * @param connection3
     *            Second connection of the pair.
     */
    public void switchConnections(NetworkConnection connection1, NetworkConnection connection3) {
        int temp = networkNodes[connection1.getNode()].getConnections()[connection1.getPosition()].getSource();
        networkNodes[connection1.getNode()].getConnections()[connection1.getPosition()].setSource(networkNodes[connection3.getNode()]
                .getConnections()[connection3.getPosition()].getSource());
        networkNodes[connection3.getNode()].getConnections()[connection3.getPosition()].setSource(temp);
    }

    /**
     * Switches two random connection in the network.
     */
    public void switchRandomConnections(boolean preserveLoops) {
        ArrayList<NetworkConnection> connections = getConnections();
        int numberOfLoops = numberOfLoops();
        while (true) {
            int rnd1 = (int) (Math.random() * (connections.size() - 1));

            int rnd2 = (int) (Math.random() * (connections.size() - 1));

            switchConnections(connections.get(rnd1), connections.get(rnd2));
            if (numberOfLoops == numberOfLoops() || !preserveLoops) {
                break;
            }
            switchConnections(connections.get(rnd1), connections.get(rnd2));
        }
    }

    /**
     * Switches two random connections in the network for given number of times.
     *
     * @param repetitions
     *            Number of switches
     */
    public void switchRandomConnections(int repetitions, boolean preserveLoops) {
        for (int i = 0; i < repetitions; i++) {
            switchRandomConnections(preserveLoops);
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (double o : getValues()) {
            result.append(StandardNumberFormat.SHORTFIXEDNUMBERFORMAT.format(o) + " | ");
        }
        return result.toString();
    }
}
