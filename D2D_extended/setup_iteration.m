close all; clc;

% load models & data
arInit;
arLoadModel('name_model');
arLoadData('name_data');

arCompileAll;

% create new 'updatedSetPars.tsv' with best fit parameters 
% from previous run. Change directories!
arBestFit_SetPars;

% load parameters from best fit 
arUpdateSetPars;

% fitting of the model. The number of repetitions can be defined 
% in the brackets.
arFit(5);

% hypothesis testing. A statistical test to assess the association between 
% two categorical variables by comparing observed and expected frequencies
arChi2Test;

% plot the results
arPlot;

% save results
arExportPEtab;
arSave;

%%%%% END