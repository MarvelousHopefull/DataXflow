main.m 			Needs to be generated with the JimenaE framework and exected in the same folder where the 
			helper functions below are saved.

forward.m		Solves for given time curves of external stimuli u the model equations for
			the  state variable x to obtain the corresponding time curve

get_xd.m 		Sets the desired steady state

combinatorial_method.m	Is the implementation of Algorithm 5.4; a heuristic method to select external
			stimuli that have a lower target functional value than the unperturbed time 
			evolution of the model

projected_gradient_method.m	Implementation of the projected gradient method; Algorithm 5.1

SQH_method.m		Implementation of the sequential quadratic Hamiltonian method; Algorithm 5.2

createJacobian.m	Creates the Jacobian of the right hand side of the model; Derivatives of f(x,u)
			with respect to x and u

projection.m		Projects each component of u into [0,1]

setControls.m		Sets the value of the external stimuli to 1 for a certain period of time, 
			else zero

backward.m		Solves the adjoint equation for the projected gradient method

backward_SQH.m		Solves the adjoint equation for the sequential quadratic Hamiltonian method

get_J.m			Calculates the target functional value for the projected gradient method

get_J_SQH.m		Calculates the target functional value for the sequential quadratic Hamiltonian 
			method

get_gradient.m		Assembles the gradient for the projected gradient method

drawStimuli.m		Draws the time curves of the resulting external stimuli u

drawStates.m		Draws the time curves of the resulting states x

In order to execute the Matlab-file, one needs a Matlab version with a symbolic math toolbox. 
Additionally the parallel computing toolbox is recommended. If this toolbox is not available, 
then just put "for" instead of "parfor" in the function createJacobian.m.
