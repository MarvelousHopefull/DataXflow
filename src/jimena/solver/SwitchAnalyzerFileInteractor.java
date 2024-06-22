package jimena.solver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.table.TableModel;

import jimena.binaryrn.RegulatoryNetwork;
import jimena.weightsCalculator.D2DMapping;

public class SwitchAnalyzerFileInteractor {
	
	/**
	 * Creates the file for the Switch Analyzer.
	 * Removes existing files with the same Name.
	 * @param path The String Path where the new File should be saved to.
	 * @param model The TableModel where the steady states are to be found.
	 * @param initRow The row in the TableModel where the initial steady state is to be found.
	 * @param targetRow The row in the TableModel where the targeted steady state is to be found.
	 * @param parametersPath The String Path where the File, with the (by D2D) calculated values for the Parameters, is to be found.
	 * @param network The Network in question. 
	 * @throws Exception
	 */
	public static void createSwitchAnalyzerFile(String path, HashMap<Integer, Node> nodeMap, SwitchAnalyzerDataContainer data, int controlsAmount) throws Exception {
		if (!path.endsWith(".m")) {
			path = path +".m";
		}
		File file = new File(path);
		if(file.exists()) { 
			file.delete();
		}
		String fileName = file.getName().substring(0, file.getName().lastIndexOf('.'));
		
		String text = "";
		text += getHeaderText(fileName);
		text += getOCPText(nodeMap.size(), controlsAmount, data);
		text += getODEsText(nodeMap,data);
		text += getTailText();
		text += getNodeList(nodeMap, data);
		BufferedWriter fw = new BufferedWriter(new FileWriter(path));
		
		fw.write(text);
		fw.close();
	}
	
	private static String getHeaderText(String fileName) {
		String text = "";
		text += "function [  ] = " + fileName + "()" + "\r\n";
		text += "%Main-file for calculating external stimuli for a regulatory network" + "\r\n"
				+ "%causing a switch between two steady states of the regulatory network" + "\r\n"
				+ "%For details see corresponding paper" + "\r\n"
				+ "%Date Dec 21, 2017" + "\r\n"
				+ "\r\n"
				+ "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%" + "\r\n"
				+ "%Parameters determined by the network" + "\r\n"
				+ "%numNodes:     	Number of nodes of the network" + "\r\n"
				+ "%numControls:  	Number of controls of the network" + "\r\n"
				+ "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%" + "\r\n"
				+ "%Parameters determined by the user" + "\r\n"
				+ "%timeInterval:  Number, time discretization for the ordinary differential equations\r\n"
				+ "%timeHorizon:   Number, time duration, [0, timeHorizon], in which network is simulated\r\n"
				+ "%alpha:         Number, Weights the contribution of the controls to the target functional\r\n"
				+ "%initialState:  1 x numNodes row vector, The steady state the network is in at the beginning, that is time t=0\r\n"
				+ "%finalState:    1 x numNodes row vector, The steady state in which the network is expected to be at the latest at the final time, t=timeHorizon\r\n"
				+ "\r\n"
				+ "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\r\n"
				+ "%tol1=10^-1;         %Tolerance for |x(i)-xd(i)|<tol1 for i=1,...,numNodes at the final time timeHorizon, input for the function combinatorial_method\r\n"
				+ "%tol2=10^-6;         %Tolerance for the stopping criterion for the sequential quadratic Hamiltonian method, input in function SQH_method\r\n"
				+ "%tol3=10^-4;         %Tolerance for the stopping criterion for the projected gradient method, input in function projected_gradient_method\r\n"
				+ "%T_int=10^-1;        %Tolerance to determine smallest duration of application of external stimuli causing a switch, combinatorial_method         \r\n"
				+ "%max_Num=4;          %Input in function combinatorial_method, Maximum number of external stimuli applied at once to the network in order to find a set of external stimuli that cause the desired switch, max_Num = 1,...,numControls\r\n"
				+ "%max_iter=10000;     %Maximum number of updates on the control for the sequential quadratic Hamiltonian method or maximum number of iterations of the projected gradient method\r\n"
				+ "\r\n"
				+ "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\r\n"
				+ "%flags\r\n"
				+ "%combi_method=1;                 %If flag equals 1, a combinatorial search with the function combinatorial_method by trial and error search is performed in ordert to determine external stimuli causing the desired switch, if flag equals 0 it is not performed\r\n"
				+ "%local_optimization_method=1;    %If flag equals 0, no local optimization method is performed, if local_optimization_method equals 1, then the sequential quadratic Hamiltonian (SQH) method is performed, if local_optimization_method equals 2, then a projected gradient method is performed\r\n"
				+ "\r\n"
				+ "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\r\n"
				+ "%Variables\r\n"
				+ "%x:         numNodes x ((timeHorizon/timeInterval)+1)-matrix, state of the network, row i corresponds \r\n"
				+ "%           to the  node i, i=1,...,numNodes, column j corresponds to\r\n"
				+ "%           the time t=(j-1)*timeInterval, j=1,...,((timeHorizon/timeInterval)+1), entry (i,j) corresponds to value x(i) of the i-th\r\n"
				+ "%           node at time t=(j-1)*timeInterval\r\n"
				+ "%xd:        numNodes x ((timeHorizon/timeInterval)+1)-matrix, Desired state\r\n"
				+ "%           of the netword at final time timeHorizon, row i corresponds to the desired\r\n"
				+ "%           state of node i, i=1,...,numNodes, column j corresponds to the time \r\n"
				+ "%           t=(j-1)*timeInterval, j=1,...,((timeHorizon/timeInterval)+1), entry (i,j) value of the i-th\r\n"
				+ "%           desired state xd(i) of node i at time t=(j-1)*timeInterval\r\n"
				+ "%u:         numControls x (timeHorizon/timeInterval)-matrix, external stimuli, row\r\n"
				+ "%           i corresponds to the external stimulus u(i), i=1,...,numControls, column j\r\n"
				+ "%           corresponds to the time t=(j-1)*timeInterval,\r\n"
				+ "%           j=1,...,(timeHorizon/timeInterval), entry (i,j) value of the i-th\r\n"
				+ "%           external stimulus u(i) at time t=(j-1)*timeInterval\r\n"
				+ "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"
				+ "\r\n";
		
		text += "tol1=10^-1;\r\n"
				+ "tol2=10^-6;\r\n"
				+ "tol3=10^-4;\r\n"
				+ "T_int=10^-1;\r\n"
				+ "max_Num=4;\r\n"
				+ "max_iter=10000;\r\n"
				+ "combi_method=1;\r\n"
				+ "local_optimization_method=1;" + "\r\n"
				+ "\r\n";
		
		return text;
	}
	
	private static String getOCPText(int numNodes, int numControls, SwitchAnalyzerDataContainer data){
		String text = "";
		text += "OCP=struct(\'numNodes\'," + numNodes + ",\'numControls\',"
				+ numControls + ",\'timeInterval\'," + data.getTimeInterval() + ",\'timeHorizon\'," 
				+ data.getTimeHorizon() + ",\'alpha\',"
				+ data.getAlphaW() + ",\'initialState\',[" + getStateStringRepresentation(data.getInitState()) + "]);" + "\r\n"
				+ "\r\n";
		text += "xd=get_xd([" + getStateStringRepresentation(data.getTargetState()) + "], OCP);" + "\r\n";

		return text;
	}
	
	protected static String getODEsText(HashMap<Integer, Node> nodeMap, SwitchAnalyzerDataContainer data) {
		String text = "";
		float alpha = data.getValueAlpha();
		float beta = data.getValueBeta();
		float h = data.getValueH();
		float gamma = data.getValueGamma();
		HashMap<String, Integer> regP = data.getPosetiveRegulations();
		HashMap<String, Integer> regN = data.getNegativeRegulations();
		String content = "";
		for (Integer i : nodeMap.keySet()) {
			String output_j = "";
			System.out.println("Node " + i + " : " + nodeMap.get(i).getName());
			String nodeX = "x(" + i + ")";
			String omega = "";
			if (nodeMap.get(i).getActBy().size() > 0) {
				float alphasum = 0;
				String actsum = "";
				boolean alreadyinput = false;
				for (Integer j : nodeMap.get(i).getActBy()) {
					System.out.println("\tActivated by " + j + ":" + nodeMap.get(j).getName());
					alphasum += alpha;
					if (alreadyinput)
						actsum += '+';
					actsum += (alpha == 1 ? "" : Float.toString(alpha) + "*") + "x(" + Integer.toString(j) + ")";
					alreadyinput = true;
				}
				omega = "(" + Float.toString(1 + alphasum) + "/" + Float.toString(alphasum) + ")*((" + actsum + ")/(1+"
						+ actsum + "))";
			}

			if (nodeMap.get(i).getInhBy().size() > 0) {
				boolean alreadyinput = false;
				float betasum = 0;
				String inhsum = "";
				for (Integer j : nodeMap.get(i).getInhBy()) {
					System.out.println("\tInhed by " + j + ":" + nodeMap.get(j).getName());
					betasum += beta;
					if (alreadyinput)
						inhsum += '+';
					inhsum += (beta == 1 ? "" : Float.toString(beta) + "*") + "x(" + Integer.toString(j) + ")";
					alreadyinput = true;
				}
				if (!omega.isEmpty()) omega += "*";
				
				omega += "(1-"+"(" + Float.toString(1 + betasum) + "/" + Float.toString(betasum) + ")*((" + inhsum + ")/(1+"
						+ inhsum + "))"+")";
			}

			if (nodeMap.get(i).getActBy().size() > 0 || nodeMap.get(i).getInhBy().size() > 0) {
				output_j += "@(x,u)((-exp(" + (0.5 * h) + ")+exp(-" + h + "*((" + omega + ")-0.5)))/((1-exp("
						 + (0.5 * h) + "))*(1+exp(-" + h + "*((" + omega + ")-0.5)))))-"
						 + (gamma == 1 ? nodeX : gamma + "*" + nodeX);				
				// output_j .=
				// "@(x,u)((-exp(".0.5*$h.")+exp(-$h*(($omega)-0.5)))/((1-exp(".0.5*$h."))*(1+exp(-$h*(($omega)-0.5)))))-$gamma*$nodeX";
			} else {
				output_j += "@(x,u)-" + gamma + "*" + nodeX;     // $output_j .="@(x,u)-$gamma*$nodeX";
			}

			/*System.out.print("Positive regulation: ");
			for (int i1 : posCtlT.getSelectedRows()) {
				System.out.print(posCtlT.getValueAt(i1, 0) + "\t");
			}
			System.out.println();

			System.out.print("Negative regulation: ");
			for (int i1 : negCtlT.getSelectedRows()) {
				System.out.print(negCtlT.getValueAt(i1, 0) + "\t");
			}
			System.out.println();*/
			
			if (regP.containsKey(nodeMap.get(i).getName())) {
				//System.out.println("positive Control added for node " + nodeMap.get(i).toString());
				output_j += "+u(" + (regP.get(nodeMap.get(i).getName())) + ")*(1-x(" + i + "))";				
			}
			if (regN.containsKey(nodeMap.get(i).getName())) {
				//System.out.println("negative Control added for node " + nodeMap.get(i).toString());
				output_j += "-u(" + (regN.get(nodeMap.get(i).getName())) + ")*x(" + i + ")";				
			}

			if (!content.isEmpty())	content += ",...\n";
			content += output_j;
		} // node loop
		text = "f= {" + content + "};\n";
		return text;
	}
	
	private static String getTailText() {
		String text = "";
		text += "\r\n"
				+ "u=zeros(OCP.numControls,round(OCP.timeHorizon/OCP.timeInterval));  %Initial guess for the controls if no combinatorial search is performed before the local optimization framework, any control can be set to any value between 0 and 1 for an initial guess for example ones() instead of zeros()\r\n"
				+ "\r\n"
				+ "if(combi_method==1)                                         %Block for the combinatorial method\r\n"
				+ "    fprintf('\\n');\r\n"
				+ "    fprintf('Starting combinatorial method...\\n');\r\n"
				+ "    A=combinatorial_method(f,xd,tol1,max_Num,T_int,OCP);    %Trys to get external stimuli causing desired switch by trial an error for different combinations of external stimuli, Output: array, left column number of stimuli, right column duration of its application from t=0\r\n"
				+ "    if(isempty(A)==0)                                       %Check if any external stimuli have been found\r\n"
				+ "        u=setControls(A,OCP);                               %Sets u according to the external stimuli returned by function combinatorial_method if the set of external stimuli returned by function combinatorial_method is not empty\r\n"
				+ "        fprintf('\\n');                                        \r\n"
				+ "        fprintf('External stimuli from combinatorial method causing desired switch at a tolerance |x-xd|<%d:\\n',tol1)       \r\n"
				+ "        [rowA,~]=size(A);                                   %Number of the set of external stimuli which have been found causing the desired switch                                                                                     %Determine rows of A     \r\n"
				+ "        for i=1:rowA                                                                              %Read out each row\r\n"
				+ "            fprintf('External stimulus %i applied at t=0 for %d time units\\n', A(i,1),A(i,2));    %Output results of combinatorial_method, that means read out matrix A if not empty\r\n"
				+ "        end\r\n"
				+ "    end\r\n"
				+ "end\r\n"
				+ "\r\n"
				+ "if(local_optimization_method==1 || local_optimization_method==2)                             %Block for the local optimization method                                                         \r\n"
				+ "    [df_x,cmx,df_u,cmu]=createJacobian(f,OCP);                                               %Creates derivatives of f with respect to x and u, Jacobian of the right hand side f\r\n"
				+ "    if(local_optimization_method==1)\r\n"
				+ "        u=SQH_method( @get_J_SQH,f,df_x,cmx,df_u,cmu,tol2,u,xd,max_iter,OCP); %Sequential quadratic Hamiltonian method as a local optimization scheme, returns u, u the external stimuli optimizing the target functional\r\n"
				+ "                                                                              %Input: @get_J function handle for the target functional, f right hand-side of the ordinary differential equation corresponding to the network with dx/dt=f(x(t),u(t))\r\n"
				+ "                                                                              %df_x function handle for the derivative of f with respect to x, cmx notes the nonzero elements of df_x, df_u function handle for the derivative of f with respect to u, cmu notes the nonzero elements of df_u, see output function createJacobian\r\n"
				+ "                                                                              %u inital guess for the external stimuli, can be taken from the combinatorial method \r\n"
				+ "                                                                              %xd desired state for the values x of the corresponding nodes, tol2 stopping criterion, max_iter maximum number of updates on the control u of the sequential quadratic Hamiltonian\r\n"
				+ "    end                                                              \r\n"
				+ "                                                                                                                                                            \r\n"
				+ "    if (local_optimization_method==2)\r\n"
				+ "        [u,~,~]=projected_gradient_method( @get_gradient, @projection, @get_J,f,df_x,cmx,df_u,cmu, u, xd, tol3, max_iter, OCP );    %Prjected gradient method as a local optimization scheme, returns [u,J,count], u the external stimuli optimizing the target functional\r\n"
				+ "                                                                                                                                    %Input:@get_gradient function handle for the gradient of the reduced target funcitonal, @projection function handel of the projection, projects u into [0,1] \r\n"
				+ "                                                                                                                                    %@get_J function handle for the target functional, f right hand-side of the ordinary differential equation corresponding to the network with dx/dt=f(x(t),u(t))\r\n"
				+ "                                                                                                                                    %df_x function handle for the derivative of f with respect to x, cmx notes the nonzero elements of df_x, df_u function handle for the derivative of f with respect to u, cmu notes the nonzero elements of df_u, see output function createJacobian\r\n"
				+ "                                                                                                                                    %u inital guess for the external stimuli, can be taken from the combinatorial method \r\n"
				+ "                                                                                                                                    %xd desired state for the values x of the corresponding nodes, tol2 stopping criterion, max_iter maximum iteration number of the projected gradient method  \r\n"
				+ "    end\r\n"
				+ "end\r\n"
				+ "\r\n"
				+ "if(max(max(u))~=0)                          %Print the numbers of active external stimuli if there is an external stimulus being different from a constant zero function\r\n"
				+ "    fprintf('\\n');\r\n"
				+ "    fprintf('Active external stimuli, that means being different from constant zero function:\\n');                      \r\n"
				+ "    for i=1:OCP.numControls                 %Prints out the numbers which correspond to the active external stimuli, can be copied for the function drawStimuli  \r\n"
				+ "        if(max(u(i,:))~=0)\r\n"
				+ "            fprintf('%i,',i);\r\n"
				+ "        end\r\n"
				+ "    end\r\n"
				+ "    fprintf('\\n');\r\n"
				+ "else\r\n"
				+ "    fprintf('No active external stimulus\\n');\r\n"
				+ "end\r\n"
				+ "\r\n"
				+ "x=forward(f,u,OCP);                           %Calculates the state of the network corresponding to the external stimuli u calculated with the schemes above\r\n"
				+ "\r\n"
				+ "fprintf('\\n');\r\n"
				+ "fprintf('Final values of the state x at final time T\\n');         \r\n"
				+ "fprintf('Number node\\t Value node\\n')\r\n"
				+ "for i=1:OCP.numNodes                          %Output of the value of the state x at final time T\r\n"
				+ "    fprintf('%i\\t\\t %d\\n',i,x(i,end));\r\n"
				+ "end\r\n"
				+ "\r\n"
				+ "if(min((abs(x(:,end)-xd(:,end))<tol1)))       %Checks if final external stimuli cause the desired switch up to tolerance tol1\r\n"
				+ "    fprintf('\\n');\r\n"
				+ "    fprintf('Each node equals its desired value at final time within its tolerance, that means |x_i(T)-x_d_i|<%d\\n',tol1);\r\n"
				+ "else\r\n"
				+ "    fprintf('\\n');\r\n"
				+ "    fprintf('There is a node that does not have its desired value at the end, that means |x_i(T)-x_d_i|>=%d\\n',tol1);\r\n"
				+ "end\r\n"
				+ "\r\n"
				+ "fprintf('\\n');\r\n"
				+ "fprintf('Save data to file...\\n');\r\n"
				+ "dlmwrite('x.txt',[0:OCP.timeInterval:round(OCP.timeHorizon/OCP.timeInterval)*OCP.timeInterval;x]);               %Writes the state x in a text-file \"x.txt\" where the first row corresponds to the discrete time steps, separated by commas, row i=2,...,numNode+1 corresponds to state x(i-1), values in the colums, separated by commas, value of x(i) at the corresponding time step  \r\n"
				+ "dlmwrite('u.txt',[0:OCP.timeInterval:(round(OCP.timeHorizon/OCP.timeInterval)-1)*OCP.timeInterval;u]);           %Writes the external stimuli u in a text-file \"u.txt\" where the first row corresponds to the discrete time steps, separated by commas, row i=2,...,numControls+1 corresponds to external stimlus u(i), values in the colums, separated by commas, value of u(i) at the corresponding time step\r\n"
				+ "fprintf('Done!\\n')\r\n"
				+ "end\r\n"
				+ "\r\n";
		return text;
	}
	
	private static String getNodeList(HashMap<Integer, Node> nodeMap, SwitchAnalyzerDataContainer data) {
		HashMap<String, Integer> regP = data.getPosetiveRegulations();
		HashMap<String, Integer> regN = data.getNegativeRegulations();
		String text = "% Node_list \n";
		for (Integer i : nodeMap.keySet()) {
			text += "% Node " + i + " : " + nodeMap.get(i).getName() + "\n";			
		}
		
		text += "%\n% Regulation_list\n";	
		for (String n: regP.keySet())
			text += "% Pos "+regP.get(n).toString()+" : "+n+"\n";
	
		for (String n: regN.keySet()) 
			text += "% Neg "+regN.get(n).toString()+" : "+n+"\n";		
		return text;
	}
	
	private static String getStateStringRepresentation(ArrayList<Double> state) {
		String text = "";
		if (state.size() < 1) return text;
		text += state.get(0);
		for(int i = 1; i < state.size(); i++) {
			text += "," + state.get(i);
		}
		return text;
	}
}
