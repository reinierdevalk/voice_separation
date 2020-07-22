% This function accesses a directory structure that contains the following files 
% - chord_dictionary.csv
% - mapping_dictionary.csv
% - fold_<n>/ISM.csv
% - fold_<n>/OPM.csv
% - fold_<n>/TPM.csv
% - fold_<n>/observations.csv
%
% The function calculates, for each fold, the most likely sequence of mappings, and
% stores it in the following files
% - fold_<n>/model_output-mappings.csv
% - fold_<n>/model_output.csv
function [voiceSequence] = HMM_evaluate(fold, path, uniform_TPM, uniform_ISM, file_names)

TPM = file_names{1}
OPM = file_names{2}
ISM = file_names{3}
obs = file_names{4}
chD = file_names{5}
%maD = file_names{6}
%outM = file_names{7}
outI = file_names{6}

foldStr = ''
if fold < 10 
	foldStr = strcat(path, 'fold_0', num2str(fold), '/');
else
    foldStr = strcat(path, 'fold_', num2str(fold), '/');
end;

% Load transition matrix and normalize
transition = load([foldStr TPM]);
for i=1:size(transition,1) 
    transition(i,:) = transition(i,:)/(sum(transition(i,:))+eps);
end;
if uniform_TPM
    transition = 1/size(transition,1)*ones(size(transition,1),size(transition,1)); % uniform transitions
end;

% Load observation matrix and normalize
observation = load([foldStr OPM]);
%observation = observation-1; % keeping ones or setting to zero
for i=1:size(observation,1) 
    observation(i,:) = observation(i,:)/(sum(observation(i,:))+eps);
end;
%figure; imagesc(observation);

% Load prior probability matrix and normalize
priors = load([foldStr ISM]);
priors = priors/(sum(priors)+eps);
if uniform_ISM
    priors = (1/size(transition,1))*ones(1,size(transition,1)); % uniform prior
end;

% Load test data
testData = read_CSV_file([foldStr obs]);

% Convert test data into sequence indexed by chord dictionary
chordDictionary = read_CSV_file([path chD]);
testSequence = zeros(1,length(testData));
for i=1:length(testData) 
    for j=1:length(chordDictionary)    
        if( strcmp(testData{i},chordDictionary{j}) )    
            testSequence(i) = j; 
        end;
    end;
end;

% Compute the observation probabilities for the test sequence
B = multinomial_prob(testSequence, observation');

% Use the Viterbi algorithm in order to extract the most likely voice assignment sequence
voiceSequence = viterbi_path(priors, transition, B);

%% Save voice assignment configuration
%voiceDictionary = read_CSV_file([path maD]);
%fid = fopen([foldStr outM],'w');
%for i=1:length(voiceSequence)
%    fprintf(fid,'%s\n',voiceDictionary{voiceSequence(i)});  % print voice assignment configurations
%end;
%fclose(fid);

% Save voice assignment sequence
fid = fopen([foldStr outI],'w');
for i=1:length(voiceSequence)
%    fprintf(fid,'%d\n',voiceSequence(i));                  % print voice assignment index (1-based)
    fprintf(fid,'%d\n',(voiceSequence(i)-1));               % print voice assignment index (0-based)
end;
fclose(fid);