function dictionary = readDictionary(filename)

% Open raw file
fid = fopen(filename, 'r+');

% Read first line
tline = fgetl(fid);
dictionary{1} = char(sscanf(tline,'%s'));

% Read remaining lines
i=1;
while ischar(tline)
    i = i+1;
    tline = fgetl(fid);
    if (ischar(tline))
        dictionary{i} = char(sscanf(tline, '%s'));
    end;
end;

% Close file
fclose(fid);