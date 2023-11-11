%%%%Script opend the initValue.txt output from JimenaE
% open initValue.txt 
fileID = fopen('name_initValues.txt', 'r');

% Test:
if fileID == -1
    error('problem: name of input not correct');
end

% Read in the data
fileContent = {};
while ~feof(fileID)
    line = fgetl(fileID);
    fileContent = [fileContent, line];
end

% Close file
fclose(fileID);

% Execute the read code
for i = 1:length(fileContent)
    try
        eval(fileContent{i});
    catch
        fprintf('Error when executing line %d\n', i);
    end
end