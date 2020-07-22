function voiceDictionary = readMappingDictionary(filename)

% Open raw file
%fid = fopen('Data/voiceAssignmentDictionary.txt','r+');
fid = fopen(filename,'r+');

% Read 1st line
tline = fgetl(fid);
voiceDictionary{1} = char(sscanf(tline,'%s'));

% Read rest of the lines
i=1;
while ischar(tline)
    i = i+1;
    tline = fgetl(fid);
    if (ischar(tline))
        voiceDictionary{i} = char(sscanf(tline, '%s'));
    end;
end;

% Close file
fclose(fid);