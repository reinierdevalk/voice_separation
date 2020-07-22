% This function is run from a dataset folder that contains the following files 
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


function [voiceSequence] = HMM_evaluate(fold)
% e.g. voiceSequence = HMM_evaluate(1)

% These file names must be the same as in the Java code
TPM = 'TPM.csv'
OPM = 'OPM.csv'
ISM = 'ISM.csv'
obs = 'observations.csv'
chD = 'chord_dictionary.csv'
maD = 'mapping_dictionary.csv'
outM = 'model_output-mappings.csv'
outI = 'model_output.csv'

foldStr = ''
if fold < 10 
	foldStr = strcat('fold_0', num2str(fold), '/');
else
    foldStr = strcat('fold_', num2str(fold), '/');
end;

% Load transition matrix and normalize
%transition = load(['Data/Transition probability matrix fold ' num2str(fold) '.txt']);
transition = load([foldStr TPM]);
for i=1:size(transition,1) 
    transition(i,:) = transition(i,:)/(sum(transition(i,:))+eps);
end;
transition = 1/size(transition,1)*ones(size(transition,1),size(transition,1)); % uniform transitions

% Load observation matrix and normalize
%observation = load(['Data/Observation probability matrix fold ' num2str(fold) '.txt']);
observation = load([foldStr OPM]);
%observation = observation-1; % keeping ones or setting to zero
for i=1:size(observation,1) 
    observation(i,:) = observation(i,:)/(sum(observation(i,:))+eps);
end;
%figure; imagesc(observation);

% Load prior probability matrix and normalize
%priors = load(['Data/Prior probability matrix fold ' num2str(fold) '.txt']);
priors = load([foldStr ISM]);
priors = priors/(sum(priors)+eps);
priors = (1/size(transition,1))*ones(1,size(transition,1)); % uniform prior

% Load test data
%testData = readTestData(['Data/Test data fold ' num2str(fold) '.txt']);
testData = readTestData([foldStr obs]);

% Convert test data into sequence indexed by chord dictionary
chordDictionary = readChordDictionary(chD);
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

% Save voice assignment configuration
voiceDictionary = readMappingDictionary(maD);
%fid = fopen(['Voice assignment configuration fold ' num2str(fold) '.txt'],'w');
fid = fopen([foldStr outM],'w');
for i=1:length(voiceSequence)
    fprintf(fid,'%s\n',voiceDictionary{voiceSequence(i)});  % print voice assignment configurations
end;
fclose(fid);

% Save voice assignment sequence
%fid = fopen(['Voice assignment index fold ' num2str(fold) '.txt'],'w');
fid = fopen([foldStr outI],'w');
for i=1:length(voiceSequence)
%    fprintf(fid,'%d\n',voiceSequence(i));                  % print voice assignment index (1-based)
    fprintf(fid,'%d\n',(voiceSequence(i)-1));               % print voice assignment index (0-based)
end;
fclose(fid);
