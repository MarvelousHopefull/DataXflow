# DataXflow
DataXflow is a software pipeline for data-driven modelling and optimal external stimuli calculation developed by the Bioinformatics chair of the Julius-Maximilians-University-Wuerzburg. 
For an extensive ivesitigation and modelling of experimental data, the pipeline integrates different tools ranging from best parameter fitting (D2D) to optimal external stimuli calculation (optimal control) into a graphical user interface (Jimena), which makes data analysis esier since specific programming skills are reduced. 

You can find the original Jimena software, which allows also other analysis, like dynamic simulations and network stability. These networks are represented as graphs. A more detailed explanation of Jimena can be found here:
https://www.biozentrum.uni-wuerzburg.de/bioinfo/computing/jimena/

DataXflow is bridges the usage of Jimena and D2D.
D2D allows to translate a graph modelling the interaction of agents of a network used by Jimena into model equations and fit their paramters to experimental data.
The added graphical user inferfaces help with the generation of files needed for the usage of D2D. 
You can find the D2D software here: https://github.com/Data2Dynamics/d2d

To further analyze an optimal intervention for the network to steer it from an inital state to a desired state, the external stimuli framework is intergrated into DataXflow in order to exploit the information encoded into the fitted parameters puposefully. The original work about external stimuli can be found here https://opus.bibliothek.uni-wuerzburg.de/frontdoor/index/index/docId/17436.

For detailed explanations, please see the corresponding paper, availaber under ..., in course of which this pipeline was develped. If you use DataXflow or inspires you for further research, we are happy if you cite our paper.

## Installation
Downloading this respository provided in the master branch gives you access to the complete Jimena software 
including the additions needed for working with D2D. The JimenaE.jar file can be used like any other executable file, openenig the Jimena GUI.
