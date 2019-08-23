// This is the name that is put in front of all
// clauses if the program has to look for a corresponding
// class and method
prefix machineLearning.;  // package name

// Definition of the operators
operator SIG = fuzzy.operators.SigmoidOperator();

// some facts (= input nodes with feature values = X )
TabTrainingExample.getBias <-; //bias
TabTrainingExample.getPitch <- ;
TabTrainingExample.getGivenDuration <- ;
TabTrainingExample.getCourse <- ;
TabTrainingExample.getPitchDistToFirst <- ;
TabTrainingExample.getPitchDistToSecond <- ;
TabTrainingExample.getPitchDistToThird <- ;
TabTrainingExample.getPitchDistToFourth <- ;
TabTrainingExample.getPitchDistToFifth <- ;
TabTrainingExample.getMaximumPhysicalDuration <- ;

// some simple clauses (= hidden & output nodes )
HiddenNode1 <- TabTrainingExample.getBias, TabTrainingExample.getPitch, TabTrainingExample.getGivenDuration, 
  TabTrainingExample.getCourse, TabTrainingExample.getPitchDistToFirst, TabTrainingExample.getPitchDistToSecond, 
  TabTrainingExample.getPitchDistToThird, TabTrainingExample.getPitchDistToFourth, TabTrainingExample.getPitchDistToFifth,
  TabTrainingExample.getMaximumPhysicalDuration | SIG;
HiddenNode2 <- TabTrainingExample.getBias, TabTrainingExample.getPitch, TabTrainingExample.getGivenDuration,
  TabTrainingExample.getCourse, TabTrainingExample.getPitchDistToFirst, TabTrainingExample.getPitchDistToSecond, 
  TabTrainingExample.getPitchDistToThird, TabTrainingExample.getPitchDistToFourth, TabTrainingExample.getPitchDistToFifth,
  TabTrainingExample.getMaximumPhysicalDuration | SIG;

// some simple clauses (= hidden & output nodes )
OutputNode1 <- TabTrainingExample.getBias, TabTrainingExample.getPitch, TabTrainingExample.getGivenDuration, 
  TabTrainingExample.getCourse, TabTrainingExample.getPitchDistToFirst, TabTrainingExample.getPitchDistToSecond, 
  TabTrainingExample.getPitchDistToThird, TabTrainingExample.getPitchDistToFourth, TabTrainingExample.getPitchDistToFifth,
  TabTrainingExample.getMaximumPhysicalDuration, HiddenNode1, HiddenNode2 | SIG;
OutputNode2 <- TabTrainingExample.getBias, TabTrainingExample.getPitch, TabTrainingExample.getGivenDuration, 
  TabTrainingExample.getCourse, TabTrainingExample.getPitchDistToFirst, TabTrainingExample.getPitchDistToSecond, 
  TabTrainingExample.getPitchDistToThird, TabTrainingExample.getPitchDistToFourth, TabTrainingExample.getPitchDistToFifth,
  TabTrainingExample.getMaximumPhysicalDuration, HiddenNode1, HiddenNode2 | SIG;
OutputNode3 <- TabTrainingExample.getBias, TabTrainingExample.getPitch, TabTrainingExample.getGivenDuration, 
  TabTrainingExample.getCourse, TabTrainingExample.getPitchDistToFirst, TabTrainingExample.getPitchDistToSecond, 
  TabTrainingExample.getPitchDistToThird, TabTrainingExample.getPitchDistToFourth, TabTrainingExample.getPitchDistToFifth,
  TabTrainingExample.getMaximumPhysicalDuration, HiddenNode1, HiddenNode2 | SIG;
OutputNode4 <- TabTrainingExample.getBias, TabTrainingExample.getPitch, TabTrainingExample.getGivenDuration, 
  TabTrainingExample.getCourse, TabTrainingExample.getPitchDistToFirst, TabTrainingExample.getPitchDistToSecond, 
  TabTrainingExample.getPitchDistToThird, TabTrainingExample.getPitchDistToFourth, TabTrainingExample.getPitchDistToFifth,
  TabTrainingExample.getMaximumPhysicalDuration, HiddenNode1, HiddenNode2 | SIG;
OutputNode5 <- TabTrainingExample.getBias, TabTrainingExample.getPitch, TabTrainingExample.getGivenDuration, 
  TabTrainingExample.getCourse, TabTrainingExample.getPitchDistToFirst, TabTrainingExample.getPitchDistToSecond, 
  TabTrainingExample.getPitchDistToThird, TabTrainingExample.getPitchDistToFourth, TabTrainingExample.getPitchDistToFifth,
  TabTrainingExample.getMaximumPhysicalDuration, HiddenNode1, HiddenNode2 | SIG;