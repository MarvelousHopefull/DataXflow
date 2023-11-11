%%%%read txt file and execute.... into the setup
% Open the text file in read mode
fileID = fopen('updated_arsetpars.txt', 'r');

% Check if the file was opened successfully
if fileID == -1
    error('The file could not be opened.');
end

% Read the contents of the file line by line
fileContent = {};
while ~feof(fileID)
    line = fgetl(fileID);
    fileContent = [fileContent, line];
end

% Close the file
fclose(fileID);

% Execute the read code
for i = 1:length(fileContent)
    try
        eval(fileContent{i});
    catch
        fprintf('Error when executing line %d\n', i);
    end
end