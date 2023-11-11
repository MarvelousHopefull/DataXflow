% Delete workspace
close all; clc;

% Read in files
tsv_file_path = 'path\name_parameters.tsv';
txt_file_path = 'path\name_initValues.txt';
output_file_path = 'path\updated_arsetpars.txt';

% Read the TSV file into a table
data = readtable(tsv_file_path, 'Delimiter', '\t', 'FileType', 'text');

fileID = fopen(txt_file_path, 'r');
if fileID == -1
    error('Could not open file.');
end

lines = cell(0, 1);
line = fgetl(fileID);
while ischar(line)
    lines{end+1} = line;
    line = fgetl(fileID);
end

fclose(fileID);

% Read in the existing TXT file
lines = cell(0, 1);
fileID = fopen(txt_file_path, 'r');
line = fgetl(fileID);
while ischar(line)
    lines{end+1} = line;
    line = fgetl(fileID);
end
fclose(fileID);

% Create a new file and write updated lines
output_fileID = fopen(output_file_path, 'w');
for i = 1:length(lines)
    parts = strsplit(lines{i}, ',');
    funktions_name = parts{1}(12:end-1);
    id_index = find(strcmp(data.parameterName, funktions_name));
    if ~isempty(id_index)
        neuer_wert = data.nominalValue(id_index);
        parts{2} = num2str(neuer_wert);
        updated_line = strjoin(parts, ',');
        fprintf(output_fileID, '%s\n', updated_line);
    else
        fprintf(output_fileID, '%s\n', lines{i});
    end
end
fclose(output_fileID);