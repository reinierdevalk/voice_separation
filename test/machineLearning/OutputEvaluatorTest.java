package machineLearning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import data.Dataset;
import data.Dataset.DatasetID;
import machineLearning.OutputEvaluator;
import junit.framework.TestCase;
import tools.ToolBox;
import ui.Runner;
import ui.Runner.ModellingApproach;
import de.uos.fmt.musitech.utility.math.Rational;

public class OutputEvaluatorTest extends TestCase {

	public OutputEvaluatorTest(String name) {
		super(name);
	}


	protected void setUp() throws Exception {
		super.setUp();
	}


	protected void tearDown() throws Exception {
		super.tearDown();
	}


	public void testDeterminePredictedVoices() {
		OutputEvaluator.ignoreExceptionForTest = true;
		Map<String, Double> modelParams = new LinkedHashMap<String, Double>();
		modelParams.put(Runner.DEPLOY_TRAINED_USER_MODEL, 0.0);
		Runner.setModelParams(modelParams);

		List<List<Integer>> expected = new ArrayList<List<Integer>>();
		// a. N2N
		// Results (taken from getTestOutputs())
		// CoD not allowed
		expected.add(Arrays.asList(new Integer[]{3}));
		expected.add(Arrays.asList(new Integer[]{0}));
		expected.add(Arrays.asList(new Integer[]{0}));
		expected.add(Arrays.asList(new Integer[]{3}));
		expected.add(Arrays.asList(new Integer[]{0}));
		expected.add(Arrays.asList(new Integer[]{4}));
		expected.add(Arrays.asList(new Integer[]{0}));

		// CoD allowed
		expected.add(Arrays.asList(new Integer[]{3}));
		expected.add(Arrays.asList(new Integer[]{0, 4}));
		expected.add(Arrays.asList(new Integer[]{0, 1}));
		expected.add(Arrays.asList(new Integer[]{3, 0}));
		expected.add(Arrays.asList(new Integer[]{0, 3}));
		expected.add(Arrays.asList(new Integer[]{4, 3}));
		expected.add(Arrays.asList(new Integer[]{0, 1}));

		// b. C2C
		// Create mappings (based on outputs from getTestOutputs()))
		// CoD not allowed
		List<List<Integer>> testMappingsNoCoD = new ArrayList<List<Integer>>();
		// 0. Testoutputs 0 and 1 combined --> note 0 = v3 (outp0); note 1 = v0 (outp1)
		testMappingsNoCoD.add(Arrays.asList(new Integer[]{1, -1, -1, 0, -1}));
		// 1. Testoutput 2 changed into a chord occupying all voices
		testMappingsNoCoD.add(Arrays.asList(new Integer[]{4, 3, 1, 2, 0}));
		// 2. Testoutput 3a --> note 0 = v3 
		testMappingsNoCoD.add(Arrays.asList(new Integer[]{-1, -1, -1, 0, -1}));
		// 3. Testoutput 3b --> note 0 = v0
		testMappingsNoCoD.add(Arrays.asList(new Integer[]{0, -1, -1, -1, -1}));
		// 4. Testoutputs 4 and 5 combined --> note 0 = v4 (outp4); note 1 = v0 (outp5) 
		testMappingsNoCoD.add(Arrays.asList(new Integer[]{1, -1, -1, -1, 0}));
		
		// CoD allowed
		List<List<Integer>> testMappingsCoD = new ArrayList<List<Integer>>();
		// 0. Testoutputs 0 and 1 combined --> note 0 = v3 (outp0); note 1 = v0+v4 (outp1)
		testMappingsCoD.add(Arrays.asList(new Integer[]{1, -1, -1, 0, 1}));
		// 1. Testoutput 2 changed into a chord occupying all voices
		testMappingsCoD.add(Arrays.asList(new Integer[]{3, 3, 1, 2, 0}));
		// 2. Testoutput 3a --> note 0 = v3+v0 
		testMappingsCoD.add(Arrays.asList(new Integer[]{0, -1, -1, 0, -1}));
		// 3. Testoutput 3b --> note 0 = v0+v3
		testMappingsCoD.add(Arrays.asList(new Integer[]{0, -1, -1, 0, -1}));
		// 4. Testoutputs 4 and 5 combined --> note 0 = v4+v3 (outp4); note 1 = v0+v1 (outp5) 
		testMappingsCoD.add(Arrays.asList(new Integer[]{1, 1, -1, 0, 0}));

		// Results
		// CoD not allowed
		expected.add(Arrays.asList(new Integer[]{3}));
		expected.add(Arrays.asList(new Integer[]{0}));
		//
		expected.add(Arrays.asList(new Integer[]{4}));
		expected.add(Arrays.asList(new Integer[]{2}));
		expected.add(Arrays.asList(new Integer[]{3}));
		expected.add(Arrays.asList(new Integer[]{1}));
		expected.add(Arrays.asList(new Integer[]{0}));
		//
		expected.add(Arrays.asList(new Integer[]{3}));
		//
		expected.add(Arrays.asList(new Integer[]{0}));
		//
		expected.add(Arrays.asList(new Integer[]{4}));
		expected.add(Arrays.asList(new Integer[]{0}));

		// CoD allowed
		expected.add(Arrays.asList(new Integer[]{3}));
		expected.add(Arrays.asList(new Integer[]{0, 4}));
		//
		expected.add(Arrays.asList(new Integer[]{4}));
		expected.add(Arrays.asList(new Integer[]{2}));
		expected.add(Arrays.asList(new Integer[]{3}));
		expected.add(Arrays.asList(new Integer[]{0, 1}));
		//
		expected.add(Arrays.asList(new Integer[]{0, 3}));
		//
		expected.add(Arrays.asList(new Integer[]{0, 3}));
		//
		expected.add(Arrays.asList(new Integer[]{3, 4}));
		expected.add(Arrays.asList(new Integer[]{0, 1}));

		List<List<Integer>> actual = new ArrayList<List<Integer>>();
		Map<String, Double> modelParameters = new HashMap<String, Double>();
		// N2N
		modelParameters.put(Runner.MODELLING_APPROACH, (double) ModellingApproach.N2N.getIntRep());
		modelParameters.put(Runner.SNU, 0.0);
		Runner.setDataset(new Dataset(Dataset.BACH_WTC_4VV)); // to keep deviationThreshold at default -1
		modelParameters.put(Runner.DEV_THRESHOLD, 0.05);
		actual.addAll(OutputEvaluator.determinePredictedVoices(modelParameters, 
			getTestOutputs(), null));
		modelParameters.put(Runner.SNU, 1.0);
		Runner.setDataset(new Dataset(Dataset.TAB_INT_4VV)); // to set deviationThreshold
		actual.addAll(OutputEvaluator.determinePredictedVoices(modelParameters, 
			getTestOutputs(), null));
		// C2C
		modelParameters.put(Runner.MODELLING_APPROACH, 
			(double) ModellingApproach.C2C.getIntRep());
		modelParameters.put(Runner.SNU, 0.0);
		Runner.setDataset(new Dataset(Dataset.BACH_WTC_4VV)); // to keep deviationThreshold at default -1
		actual.addAll(OutputEvaluator.determinePredictedVoices(modelParameters, null, 
			testMappingsNoCoD));
		modelParameters.put(Runner.SNU, 1.0);
		Runner.setDataset(new Dataset(Dataset.TAB_INT_4VV)); // to set deviationThreshold
		actual.addAll(OutputEvaluator.determinePredictedVoices(modelParameters, null, 
			testMappingsCoD));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}


	public void testDeterminePredictedDurations() {
		OutputEvaluator.ignoreExceptionForTest = true;

		Rational eighth = new Rational(1, 8);
		Rational dottedQuarter = new Rational(3, 8);
		Rational quarter = new Rational(1, 4);

		List<Rational[]> expected = new ArrayList<Rational[]>();
		expected.add(new Rational[]{quarter});
		expected.add(new Rational[]{quarter});
		expected.add(new Rational[]{quarter});
		expected.add(new Rational[]{eighth});
		expected.add(new Rational[]{dottedQuarter});
		
		Map<String, Double> modelParameters = new HashMap<String, Double>();
		modelParameters.put(Runner.MODELLING_APPROACH, 
			(double) ModellingApproach.N2N.getIntRep());
		modelParameters.put(Runner.SNU, 1.0);
		modelParameters.put(Runner.DEV_THRESHOLD, 0.05);
		List<double[]> testOutputsWithDur = getTestOutputs();
		testOutputsWithDur.remove(1);
		testOutputsWithDur.remove(1);

		List<Rational[]> actual = 
			OutputEvaluator.determinePredictedDurations(modelParameters, 
			testOutputsWithDur, null);
		
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j]);
			}
		}
	}


	public void testDetermineBestVoiceAssignment(){
		// Make a fictional List of network outputs, in which the highest value occurs multiple times  
		List<Double> networkOutputs = 
			new ArrayList<Double>(Arrays.asList(
			new Double[]{0.1, 0.2, 0.1, 0.2, 0.1, 0.2, 0.1, 0.2, 0.1, 0.2, 0.1, 0.2}));

		// Make a fictional List of all possible voice assignment, in which every other 
		// element gets a highest network output (i.e., is a best voice assignment)
		List<List<Integer>> allPossVAs = new ArrayList<List<Integer>>();
		// Add two voice assignments with voice crossings
		allPossVAs.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{-1, -1, -1, -1, 0})));
		allPossVAs.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{2, -1, 0, 1, -1})));
		allPossVAs.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{-1, -1, -1, -1, 0})));
		allPossVAs.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{1, 2, -1, 0, -1})));
		// Add two voice assignments with CoDs
		allPossVAs.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{-1, -1, -1, -1, 0})));
		allPossVAs.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 2, 1, 0, -1})));
		allPossVAs.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{-1, -1, -1, -1, 0})));
		allPossVAs.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 1, 0, -1, 0})));
		// Add two voice assignments with no voice crossings or CoDs: one with a pitch distribution does not fall
		// within the voice averages, and one with one that does
		allPossVAs.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{-1, -1, -1, -1, 0})));
		allPossVAs.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 1, 0, -1, -1}))); // penalty = 2 
		allPossVAs.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{-1, -1, -1, -1, 0})));
		allPossVAs.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{-1, 2, 1, 0, -1})));//penalty = 0

//		// Make a fictional basicTabSymbolPropertiesChord
//		Integer[][] basicTabSymbolPropertiesChord = new Integer[3][10];
//		basicTabSymbolPropertiesChord[0] = new Integer[]{45, -1, -1, -1, -1, -1, -1, -1, -1, -1};
//		basicTabSymbolPropertiesChord[1] = new Integer[]{52, -1, -1, -1, -1, -1, -1, -1, -1, -1};
//		basicTabSymbolPropertiesChord[2] = new Integer[]{60, -1, -1, -1, -1, -1, -1, -1, -1, -1};

		// Simply take the first best voice assignment from the list 
		List<Integer> expected = allPossVAs.get(1);

//		// NB: because determineBestVoiceAssignment() uses only pitch information from basicTabSymbolPropertiesChord
//		// or basicNotePropertiesChord (via FeatureGenerator.getVoiceCrossingInformationInChord()), it is not
//		// necessary to test it for both the tablature and the non-tablature case)
//		List<Integer> actual = outputEvaluator.determineBestVoiceAssignment(basicTabSymbolPropertiesChord, null,
//			networkOutputs, allPossibleVoiceAssignments, false); <-- veranderd voor SysMus
		List<Integer> actual = OutputEvaluator.determineBestVoiceAssignment(networkOutputs, 
			allPossVAs);

		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
		assertEquals(expected, actual);	
	}


	private List<double[]> getTestOutputs() {
		List<double[]> testOutputs = new ArrayList<double[]>();
		// 0. No CoD
		testOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.1, 0.1, 0.8, 0.1}, 
			new double[]{0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.9, 0.9, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1}})));
		// CoD and 
		// 1. || hiVal || == 2
		testOutputs.add(new double[]{0.8, 0.2, 0.1, 0.1, 0.8});
		// 2. || hiVal || > 2  
		testOutputs.add(new double[]{0.8, 0.8, 0.1, 0.8, 0.8});
		// 3a. || hiVal || == 1 && || secHiVal || == 1                
		testOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.76, 0.1, 0.1, 0.8, 0.1}, 
			new double[]{0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.9, 0.9, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1}})));
		// 3b. (= 3a, reversed)
		testOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.8, 0.1, 0.1, 0.76, 0.1},
			new double[]{0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.4, 0.4, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1}})));
		// 4. || hiVal || == 1 && || secHiVal || == 2
		testOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.76, 0.1, 0.76, 0.8},
			new double[]{0.1, 0.1, 0.1, 0.9, 0.9, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1}})));
		// 5. || hiVal || == 1 && || secHiVal || > 2
		testOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.8, 0.76, 0.76, 0.76, 0.1},
			new double[]{0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.5, 0.5, 0.1, 0.1, 0.1}})));			

		return testOutputs;
	}


	public void testInterpretNetworkOutput() {
		OutputEvaluator.ignoreExceptionForTest = true;
		Map<String, Double> modelParams = new LinkedHashMap<String, Double>();
		modelParams.put(Runner.DEPLOY_TRAINED_USER_MODEL, 0.0);
		Runner.setModelParams(modelParams);
		
		List<List<List<Integer>>> expected = new ArrayList<List<List<Integer>>>();
		// 1. When CoDs are not allowed
		List<List<List<Integer>>> expectedNoCoD = new ArrayList<List<List<Integer>>>();
		List<List<Integer>> expectedNoCoD0 = new ArrayList<List<Integer>>(); 
		expectedNoCoD0.add(Arrays.asList(new Integer[]{3}));
		expectedNoCoD0.add(Arrays.asList(new Integer[]{8}));
		expectedNoCoD.add(expectedNoCoD0); expectedNoCoD.add(expectedNoCoD0);
		expectedNoCoD.add(expectedNoCoD0);
		//
		List<List<Integer>> expectedNoCoD1 = new ArrayList<List<Integer>>(); 
		expectedNoCoD1.add(Arrays.asList(new Integer[]{0}));
//		expectedNoCoD1.add(Arrays.asList(new Integer[]{}));
		expectedNoCoD1.add(null);
		expectedNoCoD.add(expectedNoCoD1); expectedNoCoD.add(expectedNoCoD1);
		expectedNoCoD.add(expectedNoCoD1);
		//
		List<List<Integer>> expectedNoCoD2 = new ArrayList<List<Integer>>(); 
		expectedNoCoD2.add(Arrays.asList(new Integer[]{0}));
//		expectedNoCoD2.add(Arrays.asList(new Integer[]{}));
		expectedNoCoD2.add(null);
		expectedNoCoD.add(expectedNoCoD2); expectedNoCoD.add(expectedNoCoD2);
		expectedNoCoD.add(expectedNoCoD2);
		//
		List<List<Integer>> expectedNoCoD3 = new ArrayList<List<Integer>>(); 
		expectedNoCoD3.add(Arrays.asList(new Integer[]{3}));
		expectedNoCoD3.add(Arrays.asList(new Integer[]{8}));
		expectedNoCoD.add(expectedNoCoD3); expectedNoCoD.add(expectedNoCoD3);
		expectedNoCoD.add(expectedNoCoD3);
		//
		List<List<Integer>> expectedNoCoD4 = new ArrayList<List<Integer>>(); 
		expectedNoCoD4.add(Arrays.asList(new Integer[]{0}));
		expectedNoCoD4.add(Arrays.asList(new Integer[]{8}));
		expectedNoCoD.add(expectedNoCoD4); expectedNoCoD.add(expectedNoCoD4);
		expectedNoCoD.add(expectedNoCoD4);
		//
		List<List<Integer>> expectedNoCoD5 = new ArrayList<List<Integer>>(); 
		expectedNoCoD5.add(Arrays.asList(new Integer[]{4}));
		expectedNoCoD5.add(Arrays.asList(new Integer[]{4}));
		expectedNoCoD.add(expectedNoCoD5); expectedNoCoD.add(expectedNoCoD5);
		expectedNoCoD.add(expectedNoCoD5);
		//
		List<List<Integer>> expectedNoCoD6 = new ArrayList<List<Integer>>(); 
		expectedNoCoD6.add(Arrays.asList(new Integer[]{0}));
		expectedNoCoD6.add(Arrays.asList(new Integer[]{12}));
		expectedNoCoD.add(expectedNoCoD6); expectedNoCoD.add(expectedNoCoD6);
		expectedNoCoD.add(expectedNoCoD6);
		
		// 2. When CoDs are allowed
		List<Double> devThresh = Arrays.asList(new Double[]{0.0, 0.05, 0.049});
		// All outputs are tested with three deviation thresholds. hiVal is always 0.8, so
		// when DT = 0.0: CoD if 2ndHiVal == hiVal
		// when DT = 0.05: CoD if 2ndHiVal >= (0.8 - 0.8*0.05) = 0.76
		// when DT = 0.049: CoD if 2ndHiVal >= (0.8 - 0.8*0.049) = 0.7608
		// 0-2. Same result for all DTs
		List<List<List<Integer>>> expectedCoD = new ArrayList<List<List<Integer>>>();
		List<List<Integer>> expected0 = new ArrayList<List<Integer>>(); 
		expected0.add(Arrays.asList(new Integer[]{3}));
		expected0.add(Arrays.asList(new Integer[]{8}));
		expectedCoD.add(expected0); expectedCoD.add(expected0); expectedCoD.add(expected0);
		// 
		List<List<Integer>> expected1 = new ArrayList<List<Integer>>(); 
		expected1.add(Arrays.asList(new Integer[]{0, 4}));
//		expected1.add(Arrays.asList(new Integer[]{}));
		expected1.add(null);
		expectedCoD.add(expected1); expectedCoD.add(expected1); expectedCoD.add(expected1);
		// 
		List<List<Integer>> expected2 = new ArrayList<List<Integer>>(); 
		expected2.add(Arrays.asList(new Integer[]{0, 1}));
//		expected2.add(Arrays.asList(new Integer[]{}));
		expected2.add(null);
		expectedCoD.add(expected2); expectedCoD.add(expected2); expectedCoD.add(expected2);
		// 3-6. No CoD when DT = 0.0 or DT = 0.049
		List<List<Integer>> expected3no = new ArrayList<List<Integer>>(); 
		expected3no.add(Arrays.asList(new Integer[]{3}));
		expected3no.add(Arrays.asList(new Integer[]{8}));
		//
		List<List<Integer>> expected3yes = new ArrayList<List<Integer>>(); 
		expected3yes.add(Arrays.asList(new Integer[]{3, 0}));
		expected3yes.add(Arrays.asList(new Integer[]{8}));
		expectedCoD.add(expected3no); expectedCoD.add(expected3yes); expectedCoD.add(expected3no);
		List<List<Integer>> expected4no = new ArrayList<List<Integer>>(); 
		expected4no.add(Arrays.asList(new Integer[]{0}));
		expected4no.add(Arrays.asList(new Integer[]{8}));
		//
		List<List<Integer>> expected4yes = new ArrayList<List<Integer>>(); 
		expected4yes.add(Arrays.asList(new Integer[]{0, 3}));
		expected4yes.add(Arrays.asList(new Integer[]{8}));
		expectedCoD.add(expected4no); expectedCoD.add(expected4yes); expectedCoD.add(expected4no);
		//
		List<List<Integer>> expected5no = new ArrayList<List<Integer>>(); 
		expected5no.add(Arrays.asList(new Integer[]{4}));
		expected5no.add(Arrays.asList(new Integer[]{4}));
		List<List<Integer>> expected5yes = new ArrayList<List<Integer>>(); 
		expected5yes.add(Arrays.asList(new Integer[]{4, 3}));
		expected5yes.add(Arrays.asList(new Integer[]{4}));
		expectedCoD.add(expected5no); expectedCoD.add(expected5yes) ;expectedCoD.add(expected5no);
		//
		List<List<Integer>> expected6no = new ArrayList<List<Integer>>(); 
		expected6no.add(Arrays.asList(new Integer[]{0}));
		expected6no.add(Arrays.asList(new Integer[]{12}));
		List<List<Integer>> expected6yes = new ArrayList<List<Integer>>(); 
		expected6yes.add(Arrays.asList(new Integer[]{0, 1}));
		expected6yes.add(Arrays.asList(new Integer[]{12}));
		expectedCoD.add(expected6no); expectedCoD.add(expected6yes); expectedCoD.add(expected6no);
		
		expected.addAll(expectedNoCoD); expected.addAll(expectedCoD);
		
		List<double[]> testOutputs = getTestOutputs();
		List<List<List<Integer>>> actual = new ArrayList<List<List<Integer>>>();
		for (double[] output : testOutputs) {
			for (double dt : devThresh) {
				actual.add(OutputEvaluator.interpretNetworkOutput(output, false, dt));
			}
		}
		for (double[] output : testOutputs) {
			for (double dt : devThresh) {
				actual.add(OutputEvaluator.interpretNetworkOutput(output, true, dt));
			}
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				if (expected.get(i).get(j) == null) {
					assertEquals(expected.get(i).get(j), actual.get(i).get(j));
				}
				else {
					assertEquals(expected.get(i).get(j).size(), actual.get(i).get(j).size());
					for (int k = 0; k < expected.get(i).get(j).size(); k++) {
						assertEquals(expected.get(i).get(j).get(k), actual.get(i).get(j).get(k));
					}
				}
			}
		}
	}


//	public void testInterpretNetworkOutputOLD() {		
//		List<double[]> testOutputs = getTestOutputs();
//		// Create testLabels
//		// 0-1. Highest value appears once; second highest value appears once; duration is quarter 
//		double[] output0a = testOutputs.get(0);
//		double[] output0b = testOutputs.get(1);
//		double[] output1 = testOutputs.get(2);
//		// 2. Highest value appears once; second highest value appears twice; duration is eighth
//		double[] output2 = testOutputs.get(3);
//		// 3. Highest value appears once; second highest value appears more than twice; duration is dotted quarter
//		double[] output3 = testOutputs.get(4);
//		// 4. Highest value appears twice; no duration
//		double[] output4 = testOutputs.get(5);
//		// 5. Highest value appears more than twice; no duration
//		double[] output5 = testOutputs.get(6);
//
//		List<List<List<Integer>>> expected = new ArrayList<List<List<Integer>>>();
//		// 0. Assume that the deviationThreshold is met, and test the sequence of predicted 
//		// voices--they should appear with the voice corresponding to the position of the 
//		// element with highest activation value first
//		List<List<Integer>> expected0a = new ArrayList<List<Integer>>(); 
//		expected0a.add(Arrays.asList(new Integer[]{3, 0}));
//		expected0a.add(Arrays.asList(new Integer[]{8}));
//		List<List<Integer>> expected0b = new ArrayList<List<Integer>>();
//		expected0b.add(Arrays.asList(new Integer[]{0, 3}));
//		expected0b.add(Arrays.asList(new Integer[]{8}));
//
//		// 1-3. When highestValue appears only once: test each label for both the case 
//		// (a) when the deviationThreshold is not met (contains no CoD), and 
//		// (b) when it is met (contains CoD) 
//		List<List<Integer>> expected1a = new ArrayList<List<Integer>>(); 
//		expected1a.add(Arrays.asList(new Integer[]{4}));
//		expected1a.add(Arrays.asList(new Integer[]{8}));
//		List<List<Integer>> expected1b = new ArrayList<List<Integer>>();
//		expected1b.add(Arrays.asList(new Integer[]{4, 1}));
//		expected1b.add(Arrays.asList(new Integer[]{8}));
//		List<List<Integer>> expected2a = new ArrayList<List<Integer>>();
//		expected2a.add(Arrays.asList(new Integer[]{4}));
//		expected2a.add(Arrays.asList(new Integer[]{4}));
//		List<List<Integer>> expected2b = new ArrayList<List<Integer>>();
//		expected2b.add(Arrays.asList(new Integer[]{4, 3}));
//		expected2b.add(Arrays.asList(new Integer[]{4}));
//		List<List<Integer>> expected3a = new ArrayList<List<Integer>>();
//		expected3a.add(Arrays.asList(new Integer[]{0}));
//		expected3a.add(Arrays.asList(new Integer[]{12}));
//		List<List<Integer>> expected3b = new ArrayList<List<Integer>>();
//		expected3b.add(Arrays.asList(new Integer[]{0, 1}));
//		expected3b.add(Arrays.asList(new Integer[]{12}));
//
//		// 4-5. When highestValue appears twice or more: there should always be a CoD, regardless of the value of 
//		// deviationThreshold 
//		List<List<Integer>> expected4a = new ArrayList<List<Integer>>();
//		expected4a.add(Arrays.asList(new Integer[]{0, 4}));
//		expected4a.add(Arrays.asList(new Integer[]{}));
//		List<List<Integer>> expected4b = new ArrayList<List<Integer>>();
//		expected4b.add(Arrays.asList(new Integer[]{0, 4}));
//		expected4b.add(Arrays.asList(new Integer[]{}));
//		List<List<Integer>> expected5a = new ArrayList<List<Integer>>();
//		expected5a.add(Arrays.asList(new Integer[]{0, 1}));
//		expected5a.add(Arrays.asList(new Integer[]{}));
//		List<List<Integer>> expected5b = new ArrayList<List<Integer>>();
//		expected5b.add(Arrays.asList(new Integer[]{0, 1}));
//		expected5b.add(Arrays.asList(new Integer[]{}));
//
//		expected.add(expected0a); expected.add(expected0b); expected.add(expected1a); expected.add(expected1b);
//		expected.add(expected2a); expected.add(expected2b); expected.add(expected3a); expected.add(expected3b);
//		expected.add(expected4a); expected.add(expected4b); expected.add(expected5a); expected.add(expected5b);
//
//		List<Double> devThresh = Arrays.asList(new Double[]{
//			0.05, 0.05, 0.049, 0.05, 0.049, 0.05, 0.049, 0.05, 0.0, 0.5, 0.0, 0.5});
//		List<List<List<Integer>>> actual = new ArrayList<List<List<Integer>>>();
//		actual.add(outputEvaluator.interpretNetworkOutput(output0a, true, devThresh.get(0)));
//		actual.add(outputEvaluator.interpretNetworkOutput(output0b, true, devThresh.get(1)));
//
//		actual.add(outputEvaluator.interpretNetworkOutput(output1, true, devThresh.get(2)));
//		actual.add(outputEvaluator.interpretNetworkOutput(output1, true, devThresh.get(3)));
//
//		actual.add(outputEvaluator.interpretNetworkOutput(output2, true, devThresh.get(4)));  
//		actual.add(outputEvaluator.interpretNetworkOutput(output2, true, devThresh.get(5)));
//
//		actual.add(outputEvaluator.interpretNetworkOutput(output3, true, devThresh.get(6)));
//		actual.add(outputEvaluator.interpretNetworkOutput(output3, true, devThresh.get(7)));
//
//		actual.add(outputEvaluator.interpretNetworkOutput(output4, true, devThresh.get(8)));
//		actual.add(outputEvaluator.interpretNetworkOutput(output4, true, devThresh.get(9)));
//
//		actual.add(outputEvaluator.interpretNetworkOutput(output5, true, devThresh.get(10)));
//		actual.add(outputEvaluator.interpretNetworkOutput(output5, true, devThresh.get(11)));
//
//		// Assert equality
//		assertEquals(expected.size(), actual.size());
//		for (int i = 0; i < expected.size(); i++) {
//			assertEquals(expected.get(i).size(), actual.get(i).size());
//			for (int j = 0; j < expected.get(i).size(); j++) {
//				assertEquals(expected.get(i).get(j).size(), actual.get(i).get(j).size());
//				for (int k = 0; k < expected.get(i).get(j).size(); k++) {
//					assertEquals(expected.get(i).get(j).get(k), actual.get(i).get(j).get(k));
//				}
//			}
//		}
//	}


	public void testGetTwoHighestValuesInformation() {
		// Create test labels 
		List<Double> output1 = Arrays.asList(new Double[]{0.4, 0.5, 0.6, 0.4, 0.4}); 
		List<Double> output2 = Arrays.asList(new Double[]{0.4, 0.5, 0.6, 0.6, 0.4}); 
		List<Double> output3 = Arrays.asList(new Double[]{0.6, 0.5, 0.6, 0.5, 0.1}); 
		List<Double> output4 = Arrays.asList(new Double[]{0.6, 0.6, 0.6, 0.6, 0.6});

		double[][] expected1 = new double[2][2];
		expected1[0] = new double[]{0.6, 1.0};
		expected1[1] = new double[]{0.5, 1.0};
		double[][] expected2 = new double[2][2];
		expected2[0] = new double[]{0.6, 2.0};
		expected2[1] = new double[]{0.5, 1.0};
		double[][] expected3 = new double[2][2];
		expected3[0] = new double[]{0.6, 2.0};
		expected3[1] = new double[]{0.5, 2.0};
		double[][] expected4 = new double[2][2];
		expected4[0] = new double[]{0.6, 5.0};
		expected4[1] = new double[]{-1.0, 0.0};
		List<double[][]> expected = new ArrayList<double[][]>();
		expected.add(expected1); expected.add(expected2); 
		expected.add(expected3); expected.add(expected4);

		List<double[][]> actual = new ArrayList<double[][]>();
		actual.add(OutputEvaluator.getTwoHighestValuesInformation(output1));
		actual.add(OutputEvaluator.getTwoHighestValuesInformation(output2));
		actual.add(OutputEvaluator.getTwoHighestValuesInformation(output3));
		actual.add(OutputEvaluator.getTwoHighestValuesInformation(output4));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j].length, actual.get(i)[j].length);
				for (int k = 0; k < expected.get(i)[j].length; k++) {
					assertEquals(expected.get(i)[j][k],  actual.get(i)[j][k]);
				}
			}
		}
	}


	public void testDeterminePresenceOfCoD() {		
		// Create testLabels
		// 1. No CoDs
		List<Double> label1 = Arrays.asList(new Double[]{0.1, 0.2, 0.1, 0.1, 1.0}); 
		// 2. CoD: highest value appears once; second highest value appears once
		List<Double> label2 = Arrays.asList(new Double[]{0.1, 0.78, 0.1, 0.1, 1.0}); 
		// 3. CoD: highest value appears once; second highest value appears twice
		List<Double> label3 = Arrays.asList(new Double[]{0.1, 0.78, 0.1, 0.78, 1.0}); 
		// 4. CoD: highest value appears once; second highest value appears more than twice
		List<Double> label4 = Arrays.asList(new Double[]{0.1, 0.78, 0.78, 0.78, 1.0}); 
		// 5. CoD: highest value appears twice
		List<Double> label5 = Arrays.asList(new Double[]{1.0, 0.2, 0.1, 0.1, 1.0}); 
		// 6. CoD: highest value appears more than twice
		List<Double> label6 = Arrays.asList(new Double[]{1.0, 1.0, 0.1, 1.0, 1.0});

		// Create twoHighestValueInfos
		double[][] thvi1 = new double[2][2];
		thvi1[0] = new double[]{0.2, 1.0};
		thvi1[1] = new double[]{0.1, 4.0};
		//
		double[][] thvi2 = new double[2][2];
		thvi2[0] = new double[]{1.0, 1.0};
		thvi2[1] = new double[]{0.78, 1.0};
		//
		double[][] thvi3 = new double[2][2];
		thvi3[0] = new double[]{1.0, 1.0};
		thvi3[1] = new double[]{0.78, 2.0};
		//
		double[][] thvi4 = new double[2][2];
		thvi4[0] = new double[]{1.0, 1.0};
		thvi4[1] = new double[]{0.78, 3.0};
		//
		double[][] thvi5 = new double[2][2];
		thvi5[0] = new double[]{1.0, 2.0};
		thvi5[1] = new double[]{0.2, 1.0};
		//
		double[][] thvi6 = new double[2][2];
		thvi6[0] = new double[]{1.0, 4.0};
		thvi6[1] = new double[]{0.1, 1.0};
		
		// Create deviationThresholds
		double deviationThreshold0 = 0.0;
		double deviationThreshold1 = 0.05;
		double deviationThreshold2 = 0.22;
		double deviationThreshold3 = 0.23;

		// Label1: always false
		assertEquals(false, OutputEvaluator.determinePresenceOfCoD(label1, thvi1, deviationThreshold0));
		assertEquals(false, OutputEvaluator.determinePresenceOfCoD(label1, thvi1, deviationThreshold1));
		assertEquals(false, OutputEvaluator.determinePresenceOfCoD(label1, thvi1, deviationThreshold2));
		assertEquals(false, OutputEvaluator.determinePresenceOfCoD(label1, thvi1, deviationThreshold3));
		// Label2: false if deviationThreshold < 0.22
		assertEquals(false, OutputEvaluator.determinePresenceOfCoD(label2, thvi2, deviationThreshold0));
		assertEquals(false, OutputEvaluator.determinePresenceOfCoD(label2, thvi2, deviationThreshold1));
		assertEquals(true, OutputEvaluator.determinePresenceOfCoD(label2, thvi2, deviationThreshold2));
		assertEquals(true, OutputEvaluator.determinePresenceOfCoD(label2, thvi2, deviationThreshold3));
		// Label3: false if deviationThreshold < 0.22
		assertEquals(false, OutputEvaluator.determinePresenceOfCoD(label3, thvi3, deviationThreshold0));
		assertEquals(false, OutputEvaluator.determinePresenceOfCoD(label3, thvi3, deviationThreshold1));
		assertEquals(true, OutputEvaluator.determinePresenceOfCoD(label3, thvi3, deviationThreshold2));
		assertEquals(true, OutputEvaluator.determinePresenceOfCoD(label3, thvi3, deviationThreshold3));
		// Label4: false if deviationThreshold < 0.22
		assertEquals(false, OutputEvaluator.determinePresenceOfCoD(label4, thvi4, deviationThreshold0));
		assertEquals(false, OutputEvaluator.determinePresenceOfCoD(label4, thvi4, deviationThreshold1));
		assertEquals(true, OutputEvaluator.determinePresenceOfCoD(label4, thvi4, deviationThreshold2));
		assertEquals(true, OutputEvaluator.determinePresenceOfCoD(label4, thvi4, deviationThreshold3));
		// Label5: always true, even when deviationThreshold == 0.0
		assertEquals(true, OutputEvaluator.determinePresenceOfCoD(label5, thvi5, deviationThreshold0));
		assertEquals(true, OutputEvaluator.determinePresenceOfCoD(label5, thvi5, deviationThreshold1));
		assertEquals(true, OutputEvaluator.determinePresenceOfCoD(label5, thvi5, deviationThreshold2));
		assertEquals(true, OutputEvaluator.determinePresenceOfCoD(label5, thvi5, deviationThreshold3));
		// Label6: always true, even when deviationThreshold == 0.0
		assertEquals(true, OutputEvaluator.determinePresenceOfCoD(label6, thvi6, deviationThreshold0));
		assertEquals(true, OutputEvaluator.determinePresenceOfCoD(label6, thvi6, deviationThreshold1));
		assertEquals(true, OutputEvaluator.determinePresenceOfCoD(label6, thvi6, deviationThreshold2));
		assertEquals(true, OutputEvaluator.determinePresenceOfCoD(label6, thvi6, deviationThreshold3));
	}


	double one = Math.E; // ln = 1.0
	double two = Math.pow(Math.E, 2); // ln = 2.0
	double three = Math.pow(Math.E, 3); // ln = 3.0
	double four = Math.pow(Math.E, 4); // ln = 4.0
	double wNN = 0.25;
	double wMM1 = 0.5;
	double wMM2 = 0.4;
	public void testCreateCombinedNetworkOutputs() {
		// Create, for a single note, fictional netw output and two MM outputs
		double[] netwOutp = new double[]{one, two, three};
		double[] outpMM1 = new double[]{two, three, four};
		double[] outpMM2 = new double[]{two, two, two};

		// Make the lists with all outputs for the NN and the two MMs
		List<double[]> allNetwOutp = new ArrayList<double[]>();
		List<double[]> allOutpMM1 = new ArrayList<double[]>();
		List<double[]> allOutpMM2 = new ArrayList<double[]>();
		for (int i = 0; i < 3; i++) {
			allNetwOutp.add(netwOutp);
			allOutpMM1.add(outpMM1);
			allOutpMM2.add(outpMM2);
		}
		// Make the list with MM outputs per model
		List<List<double[]>> allMMOutps = new ArrayList<List<double[]>>();
		allMMOutps.add(allOutpMM1);
		allMMOutps.add(allOutpMM2);

		// Expected contains three times the same double[]
		List<double[]> expected = new ArrayList<double[]>();
		double[] exp = new double[3];
		exp[0] = ( Math.exp( (0.25*1 + 0.5*2 + 0.4*2) / (1.15) ) );
		exp[1] = ( Math.exp( (0.25*2 + 0.5*3 + 0.4*2) / (1.15) ) );
		exp[2] = ( Math.exp( (0.25*3 + 0.5*4 + 0.4*2) / (1.15) ) );
		for (int i = 0; i < 3; i++) {
			expected.add(exp);
		}

		List<Double> weights = Arrays.asList(new Double[]{wNN, wMM1, wMM2});
		List<double[]> actual = 
			OutputEvaluator.createCombinedNetworkOutputs(allNetwOutp, allMMOutps, weights);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j]);
			}
		}
	}


	public void testCombineNetworkOutputs() {	
		List<double[]> outputs = new ArrayList<double[]>();
		outputs.add(new double[]{one, two, three}); // output 0 (= netw outp)
		outputs.add(new double[]{two, three, four}); // output 1 (= MM outp 1)
		outputs.add(new double[]{two, two, two}); // output 2 (= MM outp 2)

		double[] expected = new double[3];
		expected[0] = ( Math.exp( (0.25*1 + 0.5*2 + 0.4*2) / (1.15) ) );
		expected[1] = ( Math.exp( (0.25*2 + 0.5*3 + 0.4*2) / (1.15) ) );
		expected[2] = ( Math.exp( (0.25*3 + 0.5*4 + 0.4*2) / (1.15) ) );

		List<Double> weights = Arrays.asList(new Double[]{wNN, wMM1, wMM2});
		double[] actual = OutputEvaluator.combineNetworkOutputs(outputs, weights);

		assertEquals(expected.length,  actual.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], actual[i]);
		}
	}

}
