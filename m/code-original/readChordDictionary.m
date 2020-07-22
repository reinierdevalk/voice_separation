function chordDictionary = readChordDictionary(filename)

% Open raw file
%fid = fopen('Data/chordDictionary.txt','r+');
fid = fopen(filename,'r+');

% Read 1st line
tline = fgetl(fid);
chordDictionary{1} = char(sscanf(tline,'%s'));

% Read rest of the lines
i=1;
while ischar(tline)
    i = i+1;
    tline = fgetl(fid);
    if (ischar(tline))
        chordDictionary{i} = char(sscanf(tline, '%s'));
    end;
end;

% Close file
fclose(fid);