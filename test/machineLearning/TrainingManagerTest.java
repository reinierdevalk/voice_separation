package machineLearning;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import representations.Tablature;
import representations.Transcription;
import ui.Runner;
import ui.Runner.FeatureVector;
import ui.Runner.ModellingApproach;
import ui.UI;

public class TrainingManagerTest extends TestCase {
	
	private File midiTestpiece1;
	private File encodingTestpiece1;

	TrainingManager tm = new TrainingManager();

	protected void setUp() throws Exception {
		super.setUp();
		Runner.setPathsToCodeAndData(UI.getRootPath(), false);
		encodingTestpiece1 = new File(Runner.encodingsPath + "test/" + "testpiece.tbp");
		midiTestpiece1 = new File(Runner.midiPath + "test/" + "testpiece.mid");	
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}


	public void testDeduplicate() {
		// Toy example: five pieces of 4, 3, 5, 5, and 3 notes
		List<Integer[]> indivPieceInds = new ArrayList<>();
		indivPieceInds.add(new Integer[]{0, 3}); // 4
		indivPieceInds.add(new Integer[]{4, 6}); // 3
		indivPieceInds.add(new Integer[]{7, 11}); // 5
		indivPieceInds.add(new Integer[]{12, 16}); // 5
		indivPieceInds.add(new Integer[]{17, 19}); // 3

		// Make random features, labels, and EDUinfo
		List<List<Double>> features = new ArrayList<>();
		features.add(Arrays.asList(new Double[]{0.0, 0.0})); // 4x
		features.add(Arrays.asList(new Double[]{1.0, 1.0})); // 2x
		features.add(Arrays.asList(new Double[]{0.0, 0.0}));
		features.add(Arrays.asList(new Double[]{3.0, 3.0})); // 3x
		//
		features.add(Arrays.asList(new Double[]{4.0, 4.0})); // 1x
		features.add(Arrays.asList(new Double[]{5.0, 5.0})); // 1x
		features.add(Arrays.asList(new Double[]{0.0, 0.0})); 
		//
		features.add(Arrays.asList(new Double[]{7.0, 7.0})); // 1x
		features.add(Arrays.asList(new Double[]{8.0, 8.0})); // 1x
		features.add(Arrays.asList(new Double[]{9.0, 9.0})); // 3x
		features.add(Arrays.asList(new Double[]{3.0, 3.0}));
		features.add(Arrays.asList(new Double[]{11.0, 11.0})); // 1x
		//
		features.add(Arrays.asList(new Double[]{12.0, 12.0})); // 1x
		features.add(Arrays.asList(new Double[]{13.0, 13.0})); // 1x
		features.add(Arrays.asList(new Double[]{0.0, 0.0}));
		features.add(Arrays.asList(new Double[]{9.0, 9.0}));
		features.add(Arrays.asList(new Double[]{9.0, 9.0}));
		//
		features.add(Arrays.asList(new Double[]{17.0, 17.0})); // 1x
		features.add(Arrays.asList(new Double[]{1.0, 1.0}));
		features.add(Arrays.asList(new Double[]{3.0, 3.0}));

		List<List<Double>> labels = new ArrayList<>();
		List<Integer[]> eduInfo = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			labels.add(Transcription.VOICE_0);
			labels.add(Transcription.VOICE_1);
			labels.add(Transcription.VOICE_2);
			labels.add(Transcription.VOICE_3);
			labels.add(Transcription.VOICE_4);
			eduInfo.add(new Integer[]{0, 0});
			eduInfo.add(new Integer[]{1, 1});
			eduInfo.add(new Integer[]{2, 2});
			eduInfo.add(new Integer[]{3, 3});
			eduInfo.add(new Integer[]{4, 4});
		}
		List<List<Double>> featuresUnadapted = new ArrayList<>(features);
		List<List<Double>> labelsUnadapted = new ArrayList<>(labels);
		List<Integer[]> eduInfoUnadapted = new ArrayList<>(eduInfo);

		// Add unadapted lists to toAdapt
		List<Object> toAdapt = new ArrayList<>();
		toAdapt.add(featuresUnadapted);
		toAdapt.add(labelsUnadapted);
		toAdapt.add(null);
		toAdapt.add(eduInfoUnadapted);
		toAdapt.add(null);

		// Add adapted lists to ExpectedToAdapt
		List<Object> expectedToAdapt = new ArrayList<>();
		List<Integer> indsOfDupls = Arrays.asList(new Integer[]{2, 6, 10, 14, 15, 16, 18, 19});
		Collections.reverse(indsOfDupls);
		for (int i : indsOfDupls) {
			features.remove(i);
			labels.remove(i);
			eduInfo.remove(i);
		}
		expectedToAdapt.add(features);
		expectedToAdapt.add(labels);
		expectedToAdapt.add(null);
		expectedToAdapt.add(eduInfo);
		expectedToAdapt.add(null);

		List<Integer[]> expectedIndivPieceInds = new ArrayList<>();
		expectedIndivPieceInds.add(new Integer[]{0, 2}); // 4-1=3
		expectedIndivPieceInds.add(new Integer[]{3, 4}); // 3-1=2
		expectedIndivPieceInds.add(new Integer[]{5, 8}); // 5-1=4
		expectedIndivPieceInds.add(new Integer[]{9, 10}); // 5-3=2
		expectedIndivPieceInds.add(new Integer[]{11, 11}); // 3-2=1

		Integer[] expectedCounts = new Integer[30];
		Arrays.fill(expectedCounts, 0);
		expectedCounts[0] = 0;
		expectedCounts[1] = 8;
		expectedCounts[2] = 2;
		expectedCounts[3] = 6;
		expectedCounts[4] = 4;

		List<Object> dedup = TrainingManager.deduplicate(toAdapt, featuresUnadapted, indivPieceInds);
		// a. lists in toAdapt
		List<Object> actualToAdapt = (List<Object>) dedup.get(0);
		//
		List<List<Double>> expectedFeatures = (List<List<Double>>) expectedToAdapt.get(0);
		List<List<Double>> actualFeatures = (List<List<Double>>) actualToAdapt.get(0);
		assertEquals(expectedFeatures.size(), actualFeatures.size());
		for (int i = 0; i < expectedFeatures.size(); i++) {
			assertEquals(expectedFeatures.get(i).size(), actualFeatures.get(i).size());
			for (int j = 0; j < expectedFeatures.get(i).size(); j++) {
				assertEquals(expectedFeatures.get(i).get(j), actualFeatures.get(i).get(j));
			}
		}
		assertEquals(expectedFeatures, actualFeatures);
		//
		List<List<Double>> expectedVoiceLabels = (List<List<Double>>) expectedToAdapt.get(1);
		List<List<Double>> actualVoiceLabels = (List<List<Double>>) actualToAdapt.get(1);
		assertEquals(expectedVoiceLabels.size(), actualVoiceLabels.size());
		for (int i = 0; i < expectedVoiceLabels.size(); i++) {
			assertEquals(expectedVoiceLabels.get(i).size(), actualVoiceLabels.get(i).size());
			for (int j = 0; j < expectedVoiceLabels.get(i).size(); j++) {
				assertEquals(expectedVoiceLabels.get(i).get(j), actualVoiceLabels.get(i).get(j));
			}
		}
		assertEquals(expectedVoiceLabels, actualVoiceLabels);
		//
		List<List<Object>> expectedIsNull = (List<List<Object>>) expectedToAdapt.get(2);;
		List<List<Object>> actualIsNull = (List<List<Object>>) actualToAdapt.get(2);
		assertEquals(expectedIsNull, actualIsNull);
		//
		List<Integer[]> expectedEduInfo = (List<Integer[]>) expectedToAdapt.get(3);
		List<Integer[]> actualEduInfo = (List<Integer[]>) actualToAdapt.get(3);
		assertEquals(expectedEduInfo.size(), actualEduInfo.size());
		for (int i = 0; i < expectedEduInfo.size(); i++) {
			assertEquals(expectedEduInfo.get(i).length, actualEduInfo.get(i).length);
			for (int j = 0; j < expectedEduInfo.get(i).length; j++) {
				assertEquals(expectedEduInfo.get(i)[j], actualEduInfo.get(i)[j]);
			}
		}
		assertEquals(expectedEduInfo, actualEduInfo);
		//
		List<List<Object>> expectedIsNull2 = (List<List<Object>>) expectedToAdapt.get(4);;
		List<List<Object>> actualIsNull2 = (List<List<Object>>) actualToAdapt.get(4);
		assertEquals(expectedIsNull2, actualIsNull2);

		// b. indivPieceInds
		List<Integer[]> actualIndivPieceInds = (List<Integer[]>) dedup.get(1);
		assertEquals(expectedIndivPieceInds.size(), actualIndivPieceInds.size());
		for (int i = 0; i < expectedIndivPieceInds.size(); i++) {
			assertEquals(expectedIndivPieceInds.get(i).length, actualIndivPieceInds.get(i).length);
			for (int j = 0; j < expectedIndivPieceInds.get(i).length; j++) {
				assertEquals(expectedIndivPieceInds.get(i)[j], actualIndivPieceInds.get(i)[j]);
			}
		}

		// c. counts
		Integer[] actualCounts = (Integer[]) dedup.get(2);
		assertEquals(expectedCounts.length, actualCounts.length);
		for (int i = 0; i < expectedCounts.length; i++) {
			assertEquals(expectedCounts[i], actualCounts[i]);
		}
	}


	public void testGetDeduplicationInfo() {
		List<Integer[]> indivPieceInds = new ArrayList<>();
		indivPieceInds.add(new Integer[]{0, 3});
		indivPieceInds.add(new Integer[]{4, 6});
		indivPieceInds.add(new Integer[]{7, 11});
		indivPieceInds.add(new Integer[]{12, 16});
		indivPieceInds.add(new Integer[]{17, 19});

		List<List<Double>> features = new ArrayList<>();
		features.add(Arrays.asList(new Double[]{0.0, 0.0})); // 4x
		features.add(Arrays.asList(new Double[]{1.0, 1.0})); // 2x
		features.add(Arrays.asList(new Double[]{0.0, 0.0}));
		features.add(Arrays.asList(new Double[]{3.0, 3.0})); // 3x
		//
		features.add(Arrays.asList(new Double[]{4.0, 4.0})); // 1x
		features.add(Arrays.asList(new Double[]{5.0, 5.0})); // 1x
		features.add(Arrays.asList(new Double[]{0.0, 0.0})); 
		//
		features.add(Arrays.asList(new Double[]{7.0, 7.0})); // 1x
		features.add(Arrays.asList(new Double[]{8.0, 8.0})); // 1x
		features.add(Arrays.asList(new Double[]{9.0, 9.0})); // 3x
		features.add(Arrays.asList(new Double[]{3.0, 3.0}));
		features.add(Arrays.asList(new Double[]{11.0, 11.0})); // 1x
		//
		features.add(Arrays.asList(new Double[]{12.0, 12.0})); // 1x
		features.add(Arrays.asList(new Double[]{13.0, 13.0})); // 1x
		features.add(Arrays.asList(new Double[]{0.0, 0.0}));
		features.add(Arrays.asList(new Double[]{9.0, 9.0}));
		features.add(Arrays.asList(new Double[]{9.0, 9.0}));
		//
		features.add(Arrays.asList(new Double[]{17.0, 17.0})); // 1x
		features.add(Arrays.asList(new Double[]{1.0, 1.0}));
		features.add(Arrays.asList(new Double[]{3.0, 3.0}));

		Integer[] expectedCounts = new Integer[30];
		Arrays.fill(expectedCounts, 0);
		expectedCounts[0] = 0;
		expectedCounts[1] = 8;
		expectedCounts[2] = 2;
		expectedCounts[3] = 6;
		expectedCounts[4] = 4;
		List<Integer> expectedIndsOfDupls = Arrays.asList(new Integer[]{2, 6, 10, 14, 15, 16, 18, 19});
		Integer[] expectedToRemovePerPiece = new Integer[]{1, 1, 1, 3, 2};
		
		List<Object> ddi = TrainingManager.getDeduplicationInfo(features, indivPieceInds);
		Integer[] actualCounts = (Integer[]) ddi.get(0);
		List<Integer> actualIndsOfDupls = (List<Integer>) ddi.get(1);
		Integer[] actualToRemovePerPiece = (Integer[]) ddi.get(2);
		
		assertEquals(expectedCounts.length, actualCounts.length);
		for (int i = 0; i < expectedCounts.length; i++) {
			assertEquals(expectedCounts[i], actualCounts[i]);
		}
		//
		assertEquals(expectedIndsOfDupls.size(), actualIndsOfDupls.size());
		for (int i = 0; i < expectedIndsOfDupls.size(); i++) {
			assertEquals(expectedIndsOfDupls.get(i), actualIndsOfDupls.get(i));
		}
		assertEquals(expectedIndsOfDupls, actualIndsOfDupls);
		//
		assertEquals(expectedToRemovePerPiece.length, actualToRemovePerPiece.length);
		for (int i = 0; i < expectedToRemovePerPiece.length; i++) {
			assertEquals(expectedToRemovePerPiece[i], actualToRemovePerPiece[i]);
		}
	}


	public void testCreateTrainAndValSet() {
		// 1. Without augmentation
		List<List<List<List<Double>>>> expectedsDbl = new ArrayList<List<List<List<Double>>>>();
		List<List<List<Integer[]>>> expectedsIntArr = new ArrayList<List<List<Integer[]>>>();
		// a. perc = 20%: 0, 5, 10 go to validation set
		List<List<Double>> expectedPrunedDbl = new ArrayList<List<Double>>();
		List<Integer[]> expectedPrunedIntArr = new ArrayList<Integer[]>();
		for (int i : Arrays.asList(new Integer[]{1, 2, 3, 4, 6, 7, 8, 9, 11, 12, 13, 14})) {
			expectedPrunedDbl.add(Arrays.asList(new Double[]{(double)i, (double)i}));
			expectedPrunedIntArr.add(new Integer[]{i, i});
		}
		List<List<Double>> expectedValDbl = new ArrayList<List<Double>>();
		List<Integer[]> expectedValIntArr = new ArrayList<Integer[]>();
		for (int i : Arrays.asList(new Integer[]{0, 5, 10})) {
			expectedValDbl.add(Arrays.asList(new Double[]{(double)i, (double)i}));
			expectedValIntArr.add(new Integer[]{i, i});
		}
		expectedsDbl.add(Arrays.asList(expectedPrunedDbl, expectedValDbl));
		expectedsIntArr.add(Arrays.asList(expectedPrunedIntArr, expectedValIntArr));

		// b. perc = 15%: 0, 7, 14 go to validation set
		expectedPrunedDbl = new ArrayList<List<Double>>();
		expectedPrunedIntArr = new ArrayList<Integer[]>();
		for (int i : Arrays.asList(new Integer[]{1, 2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13})) {
			expectedPrunedDbl.add(Arrays.asList(new Double[]{(double)i, (double)i}));
			expectedPrunedIntArr.add(new Integer[]{i, i});
		}
		expectedValDbl = new ArrayList<List<Double>>();
		expectedValIntArr = new ArrayList<Integer[]>();
		for (int i : Arrays.asList(new Integer[]{0, 7, 14})) {
			expectedValDbl.add(Arrays.asList(new Double[]{(double)i, (double)i}));
			expectedValIntArr.add(new Integer[]{i, i});
		}
		expectedsDbl.add(Arrays.asList(expectedPrunedDbl, expectedValDbl));
		expectedsIntArr.add(Arrays.asList(expectedPrunedIntArr, expectedValIntArr));

		// 2. With augmentation (3 pieces with 6, 4, 6 elements)
		List<List<List<List<Integer>>>> expectedsInt = new ArrayList<>();
		// a. One augmented piece; perc = 20%: 0, 5, 20, 25 (starred) go to validation set
		// 1           a           2       a       3           a
		// * o o o o * o o o o o o o o o o o o o o * o o o o * o o o o o o
		List<List<Integer>> expectedPrunedInt = new ArrayList<>();
		List<List<Integer>> expectedValInt = new ArrayList<>();
		for (int i = 0; i < 32; i++) {
			if (i != 0 && i != 5 && i != 20 && i != 25 ) {
				expectedPrunedInt.add(Arrays.asList(new Integer[]{i, i}));
			}
			else {
				expectedValInt.add(Arrays.asList(new Integer[]{i, i}));
			}
		}
		expectedsInt.add(Arrays.asList(expectedPrunedInt, expectedValInt));
		
		// b. Two augmented pieces; perc = 20%: 0, 5, 30, 35 (starred) go to validation set
		// 1           a           b           2       a       b       3           a           b          
		// * o o o o * o o o o o o o o o o o o o o o o o o o o o o o o * o o o o * o o o o o o o o o o o o
		TrainingManager.augmentFactor = 3;
		expectedPrunedInt = new ArrayList<>();
		expectedValInt = new ArrayList<>();
		for (int i = 0; i < 48; i++) { 
			if (i != 0 && i != 5 && i != 30 && i != 35 ) {
				expectedPrunedInt.add(Arrays.asList(new Integer[]{i, i}));
			}
			else {
				expectedValInt.add(Arrays.asList(new Integer[]{i, i}));
			}
		}
		expectedsInt.add(Arrays.asList(expectedPrunedInt, expectedValInt));
		
		// Actual
		// 1. Without augmentation
		TrainingManager.augment = false;
		List<List<List<List<Double>>>> actualsDbl = new ArrayList<List<List<List<Double>>>>();
		List<List<List<Integer[]>>> actualsIntArr = new ArrayList<List<List<Integer[]>>>();
		List<List<Double>> dataDbl = new ArrayList<List<Double>>();
		List<Integer[]> dataIntArr = new ArrayList<Integer[]>();
		for (int i = 0; i < 15; i++) {
			dataDbl.add(Arrays.asList(new Double[]{(double)i, (double)i}));
			dataIntArr.add(new Integer[]{i, i});
		}
		List<Integer[]> inds = new ArrayList<>();
		inds.add(new Integer[]{0, 4});
		inds.add(new Integer[]{5, 9});
		inds.add(new Integer[]{10, 14});
		// a. perc = 20
		actualsDbl.add(TrainingManager.createTrainAndValSet(dataDbl, 20, inds));
		actualsDbl.add(TrainingManager.createTrainAndValSet(dataDbl, 15, inds));
		// b. perc = 15
		actualsIntArr.add(TrainingManager.createTrainAndValSet(dataIntArr, 20, inds));
		actualsIntArr.add(TrainingManager.createTrainAndValSet(dataIntArr, 15, inds));
		
		// 2. With augmentation
		TrainingManager.augment = true;
		List<List<List<List<Integer>>>> actualsInt = new ArrayList<List<List<List<Integer>>>>();
		List<List<Integer>> dataIntTwoPieces = new ArrayList<>();
		List<List<Integer>> dataIntThreePieces = new ArrayList<>();
		for (int i = 0; i < 3*(6+4+6); i++) {
			if (i < 2*(6+4+6)) {
				dataIntTwoPieces.add(Arrays.asList(new Integer[]{i, i}));
			}
			dataIntThreePieces.add(Arrays.asList(new Integer[]{i, i}));
		}
		// a. One augmented piece
		TrainingManager.augmentFactor = 2;
		inds = new ArrayList<>();
		inds.add(new Integer[]{0, 5}); inds.add(new Integer[]{6, 11});
		inds.add(new Integer[]{12, 15}); inds.add(new Integer[]{16, 19});
		inds.add(new Integer[]{20, 25}); inds.add(new Integer[]{26, 31});
		actualsInt.add(TrainingManager.createTrainAndValSet(dataIntTwoPieces, 20, inds));
		// b. Two augmented pieces
		TrainingManager.augmentFactor = 3;
		inds = new ArrayList<>();
		inds.add(new Integer[]{0, 5}); inds.add(new Integer[]{6, 11}); inds.add(new Integer[]{12, 17});
		inds.add(new Integer[]{18, 21}); inds.add(new Integer[]{22, 25}); inds.add(new Integer[]{26, 29});
		inds.add(new Integer[]{30, 35}); inds.add(new Integer[]{36, 41}); inds.add(new Integer[]{42, 47});
		actualsInt.add(TrainingManager.createTrainAndValSet(dataIntThreePieces, 20, inds));
		
		assertEquals(expectedsDbl.size(), actualsDbl.size());
		for (int l = 0; l < expectedsDbl.size(); l++) {
			List<List<List<Double>>> expected = expectedsDbl.get(l);
			List<List<List<Double>>> actual = actualsDbl.get(l);
			assertEquals(expected.size(), actual.size());
			for (int i = 0; i < expected.size(); i++) {
				assertEquals(expected.get(i).size(), actual.get(i).size());
				for (int j = 0; j < expected.get(i).size(); j++) {
					assertEquals(expected.get(i).get(j).size(), actual.get(i).get(j).size());
					for (int k = 0; k < expected.get(i).get(j).size(); k++) {
						assertEquals(expected.get(i).get(j).get(k), actual.get(i).get(j).get(k));
					}
				}
			}
		}
		
		assertEquals(expectedsIntArr.size(), actualsIntArr.size());
		for (int l = 0; l < expectedsIntArr.size(); l++) {
			List<List<Integer[]>> expected = expectedsIntArr.get(l);
			List<List<Integer[]>> actual = actualsIntArr.get(l);
			assertEquals(expected.size(), actual.size());
			for (int i = 0; i < expected.size(); i++) {
				assertEquals(expected.get(i).size(), actual.get(i).size());
				for (int j = 0; j < expected.get(i).size(); j++) {
					assertEquals(expected.get(i).get(j).length, actual.get(i).get(j).length);
					for (int k = 0; k < expected.get(i).get(j).length; k++) {
						assertEquals(expected.get(i).get(j)[k], actual.get(i).get(j)[k]);
					}
				}
			}
		}
		
		assertEquals(expectedsInt.size(), actualsInt.size());
		for (int l = 0; l < expectedsInt.size(); l++) {
			List<List<List<Integer>>> expected = expectedsInt.get(l);
			List<List<List<Integer>>> actual = actualsInt.get(l);
			assertEquals(expected.size(), actual.size());
			for (int i = 0; i < expected.size(); i++) {
				assertEquals(expected.get(i).size(), actual.get(i).size());
				for (int j = 0; j < expected.get(i).size(); j++) {
					assertEquals(expected.get(i).get(j).size(), actual.get(i).get(j).size());
					for (int k = 0; k < expected.get(i).get(j).size(); k++) {
						assertEquals(expected.get(i).get(j).get(k), actual.get(i).get(j).get(k));
					}
				}
			}
		}
	}


	public void testSmoothenOutputs() {
		List<double[]> outputs = new ArrayList<double[]>();
		outputs.add(new double[]{0.1, 0.2, 0.0, 0.4, 0.5});
		outputs.add(new double[]{0.1, 0.2, 0.3, 0.4, 0.0});
		outputs.add(new double[]{0.1, 0.2, 0.3, 0.4, 0.00001});
		
		List<double[]> expected = new ArrayList<double[]>();
		expected.add(new double[]{0.1, 0.2, 0.00001, 0.4, 0.5});
		expected.add(new double[]{0.1, 0.2, 0.3, 0.4, 0.00001});
		expected.add(new double[]{0.1, 0.2, 0.3, 0.4, 0.00001});

		List<double[]> actual = TrainingManager.smoothenOutput(outputs, 0.00001);
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j]);
			}
		}
	}


	public void testGenerateAllRelativeTrainingExamples() {	
		// Make allCompleteChordFeatureVectors, a List of three chords each with three fictional chord feature vectors 
		List<List<List<Double>>> allCompleteChordFeatureVectors = new ArrayList<List<List<Double>>>();
		// Chord 0
		List<List<Double>> completeChordFeatureVectorsChord0 = new ArrayList<List<Double>>();
		List<Double> groundTruthChordFeatureVectorChord0 = Arrays.asList(new Double[]{0.00, 0.01, 0.02});
		completeChordFeatureVectorsChord0.add(groundTruthChordFeatureVectorChord0);
		List<Double> chordFeatureVectorChord01 = Arrays.asList(new Double[]{0.10, 0.11, 0.12});
		completeChordFeatureVectorsChord0.add(chordFeatureVectorChord01);
		List<Double> chordFeatureVectorChord02 = Arrays.asList(new Double[]{0.20, 0.21, 0.22});
		completeChordFeatureVectorsChord0.add(chordFeatureVectorChord02);
		// Chord 1
		List<List<Double>> completeChordFeatureVectorsChord1 = new ArrayList<List<Double>>();
		List<Double> groundTruthChordFeatureVectorChord1 = Arrays.asList(new Double[]{1.00, 1.01, 1.02});
		completeChordFeatureVectorsChord1.add(groundTruthChordFeatureVectorChord1);
		List<Double> chordFeatureVectorChord11 = Arrays.asList(new Double[]{1.10, 1.11, 1.12});
		completeChordFeatureVectorsChord1.add(chordFeatureVectorChord11);
		List<Double> chordFeatureVectorChord12 = Arrays.asList(new Double[]{1.20, 1.21, 1.22});
		completeChordFeatureVectorsChord1.add(chordFeatureVectorChord12);
		// Chord 2
		List<List<Double>> completeChordFeatureVectorsChord2 = new ArrayList<List<Double>>();
		List<Double> groundTruthChordFeatureVectorChord2 = Arrays.asList(new Double[]{2.00, 2.01, 2.02});
		completeChordFeatureVectorsChord2.add(groundTruthChordFeatureVectorChord2);
		List<Double> chordFeatureVectorChord21 = Arrays.asList(new Double[]{2.10, 2.11, 2.12});
		completeChordFeatureVectorsChord2.add(chordFeatureVectorChord21);
		List<Double> chordFeatureVectorChord22 = Arrays.asList(new Double[]{2.20, 2.21, 2.22});
		completeChordFeatureVectorsChord2.add(chordFeatureVectorChord22);

		allCompleteChordFeatureVectors.add(completeChordFeatureVectorsChord0); 
		allCompleteChordFeatureVectors.add(completeChordFeatureVectorsChord1);
		allCompleteChordFeatureVectors.add(completeChordFeatureVectorsChord2);

		// For each chord: determine the expected RelativeTrainingExamples and add them to expected
		List<RelativeTrainingExample> expected = new ArrayList<RelativeTrainingExample>();
		RelativeTrainingExample exp0 = new RelativeTrainingExample(groundTruthChordFeatureVectorChord0, chordFeatureVectorChord01);
		RelativeTrainingExample exp1 = new RelativeTrainingExample(groundTruthChordFeatureVectorChord0, chordFeatureVectorChord02);
		RelativeTrainingExample exp2 = new RelativeTrainingExample(groundTruthChordFeatureVectorChord1, chordFeatureVectorChord11);
		RelativeTrainingExample exp3 = new RelativeTrainingExample(groundTruthChordFeatureVectorChord1, chordFeatureVectorChord12);
		RelativeTrainingExample exp4 = new RelativeTrainingExample(groundTruthChordFeatureVectorChord2, chordFeatureVectorChord21);
		RelativeTrainingExample exp5 = new RelativeTrainingExample(groundTruthChordFeatureVectorChord2, chordFeatureVectorChord22);
		expected.add(exp0); expected.add(exp1); expected.add(exp2); expected.add(exp3); expected.add(exp4); expected.add(exp5);

		// For each chord: calculate the actual RelativeTrainingExamples and add them to actual
		List<RelativeTrainingExample> actual = 
			TrainingManager.generateAllRelativeTrainingExamples(allCompleteChordFeatureVectors);  

		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).getBetterVal().size(), actual.get(i).getBetterVal().size());
			for (int j = 0; j < expected.get(i).getBetterVal().size(); j++) {
				assertEquals(expected.get(i).getBetterVal().get(j), actual.get(i).getBetterVal().get(j));
			}
			assertEquals(expected.get(i).getWorseVal().size(), actual.get(i).getWorseVal().size());
			for (int j = 0; j < expected.get(i).getWorseVal().size(); j++) {
				assertEquals(expected.get(i).getWorseVal().get(j), actual.get(i).getWorseVal().get(j));
			}
		}
	}


	public void testGetNumberOfHiddenNeurons() {
		List<Double> HLFactors = 
			new ArrayList<Double>(Arrays.asList(new Double[]{0.2, 0.25, 1/3.0, 0.5, 
			1.0, 2.0, 3.0, 4.0, 5.0}));
		int numFeatures = 75;

		List<Integer> expected = 
			new ArrayList<Integer>(Arrays.asList(new Integer[]{15, 19, 25, 38, 75, 150, 225, 300, 375}));

		List<Integer> actual = new ArrayList<Integer>();
		Map<String, Double> modelParameters = new LinkedHashMap<String, Double>();
		modelParameters.put(Runner.MODELLING_APPROACH, (double) ModellingApproach.N2N.getIntRep());
//		modelParameters.put(Runner.FEAT_VEC, (double) FeatureGenerator.FEATURE_SET_D);
		modelParameters.put(Runner.FEAT_VEC, (double) FeatureVector.PHD_D.getIntRep());	
		
		for (double d : HLFactors) {
			modelParameters.put(Runner.HIDDEN_LAYER_FACTOR, (double) d);
			actual.add(TrainingManager.getNumberOfHiddenNeurons(modelParameters, numFeatures, false));
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
		assertEquals(expected, actual);
	}
	
	
	public void testGenerateChordDictionary() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		List<List<Integer>> expected = new ArrayList<List<Integer>>();
		expected.add(Arrays.asList(new Integer[]{48, 55, 63, 67})); 
		expected.add(Arrays.asList(new Integer[]{43, 55, 67, 70})); 
		expected.add(Arrays.asList(new Integer[]{46})); 
		expected.add(Arrays.asList(new Integer[]{45, 48, 57, 63})); 
		expected.add(Arrays.asList(new Integer[]{43})); 
		expected.add(Arrays.asList(new Integer[]{43, 55, 55, 58, 67})); 
		expected.add(Arrays.asList(new Integer[]{43, 58, 62, 67})); 
		expected.add(Arrays.asList(new Integer[]{57, 66})); 
		expected.add(Arrays.asList(new Integer[]{43, 55, 62, 67}));
		expected.add(Arrays.asList(new Integer[]{66}));
		expected.add(Arrays.asList(new Integer[]{67}));
		expected.add(Arrays.asList(new Integer[]{64}));

		List<TablatureTranscriptionPair> tablatureTranscriptionPairs = new ArrayList<TablatureTranscriptionPair>();
		tablatureTranscriptionPairs.add(new TablatureTranscriptionPair(tablature, transcription));
		tablatureTranscriptionPairs.add(new TablatureTranscriptionPair(tablature, transcription));
		List<List<Integer>> actual = new TrainingManager().generateChordDictionary(tablatureTranscriptionPairs);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
		assertEquals(expected, actual);
	}


	public void testGenerateChordDictionaryNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		List<List<Integer>> expected = new ArrayList<List<Integer>>();
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{50, 57, 65, 69}))); 
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{45, 57, 69, 72}))); 
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{48}))); 
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{47, 50, 59, 65, 65})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{45})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{45, 57, 57, 60, 69}))); 
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{45, 60, 64, 69}))); 
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{59, 68}))); 
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{45, 57, 64, 69})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{68})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{69})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{66})));
		 		  
		List<TablatureTranscriptionPair> tablatureTranscriptionPairs = new ArrayList<TablatureTranscriptionPair>();
		tablatureTranscriptionPairs.add(new TablatureTranscriptionPair(null, transcription));
		tablatureTranscriptionPairs.add(new TablatureTranscriptionPair(null, transcription));
		List<List<Integer>> actual = new TrainingManager().generateChordDictionary(tablatureTranscriptionPairs);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
		assertEquals(expected, actual);
	}


	public void testGenerateMappingDictionary() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		List<List<Integer>> expected = new ArrayList<List<Integer>>();
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{3, 2, 1, 0, -1}))); 
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 3, 1, 0, -1}))); 
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{-1, -1, -1, 0 , -1}))); 
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{3, 3, 2, 1, 0})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{-1, -1, -1, -1 , 0})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{4, 3, 2, 1, 0}))); 
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 3, 1, -1, 0}))); 
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{1, -1, 0, -1, -1})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{0, -1, -1, -1, -1})));

		int highestNumberOfVoices = transcription.getNumberOfVoices();
		List<TablatureTranscriptionPair> tablatureTranscriptionPairs = new ArrayList<TablatureTranscriptionPair>();
		tablatureTranscriptionPairs.add(new TablatureTranscriptionPair(tablature, transcription));
		tablatureTranscriptionPairs.add(new TablatureTranscriptionPair(tablature, transcription));
		List<List<Integer>> actual =
			new TrainingManager().generateMappingDictionary(tablatureTranscriptionPairs, highestNumberOfVoices);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
		assertEquals(expected, actual);
	}
	
	
	public void testGenerateMappingDictionaryNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		List<List<Integer>> expected = new ArrayList<List<Integer>>();
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{3, 2, 1, 0, -1}))); 
//		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 3, 1, 0, -1}))); 
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{-1, -1, -1, 0 , -1}))); 
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{3, 4, 2, 1, 0})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{-1, -1, -1, -1 , 0})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{4, 3, 2, 1, 0}))); 
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 3, 1, -1, 0}))); 
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{1, -1, 0, -1, -1})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{0, -1, -1, -1, -1})));

		int highestNumberOfVoices = transcription.getNumberOfVoices();
		List<TablatureTranscriptionPair> tablatureTranscriptionPairs = new ArrayList<TablatureTranscriptionPair>();
		tablatureTranscriptionPairs.add(new TablatureTranscriptionPair(null, transcription));
		tablatureTranscriptionPairs.add(new TablatureTranscriptionPair(null, transcription));
		List<List<Integer>> actual =
			new TrainingManager().generateMappingDictionary(tablatureTranscriptionPairs, highestNumberOfVoices);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
		assertEquals(expected, actual);
	}


	public void testGenerateInitialStateMatrix() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		Integer[] expected = new Integer[]{3, 1, 1, 1, 1, 1, 1, 1, 1};

		int highestNumberOfVoices = transcription.getNumberOfVoices();
		List<TablatureTranscriptionPair> pieces = new ArrayList<TablatureTranscriptionPair>();
		pieces.add(new TablatureTranscriptionPair(tablature, transcription));
		pieces.add(new TablatureTranscriptionPair(tablature, transcription));
		TrainingManager tm = new TrainingManager();
		List<List<Integer>> voiceAssignmentDictionary = 
			tm.generateMappingDictionary(pieces, highestNumberOfVoices);
		Integer[] actual = tm.generateInitialStateMatrix(pieces, voiceAssignmentDictionary,
			highestNumberOfVoices);

		assertEquals(expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], actual[i]);
		}
	}


	public void testGenerateInitialStateMatrixNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		Integer[] expected = new Integer[]{3, 1, 1, 1, 1, 1, 1, 1};

		int highestNumberOfVoices = transcription.getNumberOfVoices();
		List<TablatureTranscriptionPair> pieces = new ArrayList<TablatureTranscriptionPair>();
		pieces.add(new TablatureTranscriptionPair(null, transcription));
		pieces.add(new TablatureTranscriptionPair(null, transcription));
		TrainingManager tm = new TrainingManager();
		List<List<Integer>> voiceAssignmentDictionary = 
			tm.generateMappingDictionary(pieces, highestNumberOfVoices);
		Integer[] actual = tm.generateInitialStateMatrix(pieces, voiceAssignmentDictionary,
			highestNumberOfVoices);

		assertEquals(expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], actual[i]);
		}
	}


	public void testGenerateObservationProbabilityMatrix() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);
		
		// For each TablatureTranscriptionPair (which are the same) there are
		// 12 unique chords (rows) and 9 unique voice assignments (columns), where
		// chord 0 has voice assignment 0 (1 time), and no others
		// chord 1 has voice assignment 1 (1 time), and no others
		// chord 2 has voice assignment 2 (1 time), and no others
		// chord 3 has voice assignment 3 (1 time), and no others
		// chord 4 has voice assignment 4 (1 time), and no others
		// chord 5 has voice assignment 5 (1 time), and no others
		// chord 6 has voice assignment 6 (1 time), and no others
		// chord 7 has voice assignment 7 (1 time), and no others
		// chord 8 has voice assignment 0 (2 times), and no others   
		// chord 9 has voice assignment 8 (3 times), and no others
		// chord 10 has voice assignment 8 (2 times), and no others
		// chord 11 has voice assignment 8 (1 time), and no others	
//		Integer[][] expected = new Integer[12][9];
//		expected[0] = new Integer[]{1+2+10, 1+10,   1,      1+10,   1,      1,      1+10,    1,      1}; 
//		expected[1] = new Integer[]{1+10,   1+2+10, 1,      1+10,   1,      1,      1+10,    1,      1};
//		expected[2] = new Integer[]{1,      1,      1+2+10, 1,      1+10,   1,      1,       1,      1+10};
//		expected[3] = new Integer[]{1+10,   1+10,   1,      1+2+10, 1,      1,      1+10,    1,      1};
//		expected[4] = new Integer[]{1,      1,      1+10,   1,      1+2+10, 1,      1,       1,      1+10};
//		expected[5] = new Integer[]{1,      1,      1,      1,      1,      1+2+10, 1,       1,      1};
//		expected[6] = new Integer[]{1+10,   1+10,   1,      1+10,   1,      1,      1+2+10,  1,      1}; 
//		expected[7] = new Integer[]{1,      1,      1,      1,      1,      1,      1,       1+2+10, 1};
//		expected[8] = new Integer[]{1+4+10, 1+10,   1,      1+10,   1,      1,      1+10,    1,      1};
//		expected[9] = new Integer[]{1,      1,      1+10,   1,      1+10,   1,      1,       1,      1+6+10};
//		expected[10] = new Integer[]{1,     1,      1+10,   1,      1+10,   1,      1,       1,      1+4+10};
//		expected[11] = new Integer[]{1,     1,      1+10,   1,      1+10,   1,      1,       1,      1+2+10};

		Integer[][] expected = new Integer[12][9];
		expected[0] = new Integer[]{ 1+2, 1,   0,   1,   0,   0,   1,   0,   0}; 
		expected[1] = new Integer[]{ 1,   1+2, 0,   1,   0,   0,   1,   0,   0};
		expected[2] = new Integer[]{ 0,   0,   1+2, 0,   1,   0,   0,   0,   1};
		expected[3] = new Integer[]{ 1,   1,   0,   1+2, 0,   0,   1,   0,   0};
		expected[4] = new Integer[]{ 0,   0,   1,   0,   1+2, 0,   0,   0,   1};
		expected[5] = new Integer[]{ 0,   0,   0,   0,   0,   1+2, 0,   0,   0};
		expected[6] = new Integer[]{ 1,   1,   0,   1,   0,   0,   1+2, 0,   0}; 
		expected[7] = new Integer[]{ 0,   0,   0,   0,   0,   0,   0,   1+2, 0};
		expected[8] = new Integer[]{ 1+4, 1,   0,   1,   0,   0,   1,   0,   0};
		expected[9] = new Integer[]{ 0,   0,   1,   0,   1,   0,   0,   0,   1+6};
		expected[10] = new Integer[]{0,   0,   1,   0,   1,   0,   0,   0,   1+4};
		expected[11] = new Integer[]{0,   0,   1,   0,   1,   0,   0,   0,   1+2};

		int highestNumberOfVoices = transcription.getNumberOfVoices();
		List<TablatureTranscriptionPair> tablatureTranscriptionPairs = new ArrayList<TablatureTranscriptionPair>();
		tablatureTranscriptionPairs.add(new TablatureTranscriptionPair(tablature, transcription));
		tablatureTranscriptionPairs.add(new TablatureTranscriptionPair(tablature, transcription));
		TrainingManager tm = new TrainingManager();
		List<List<Integer>> chordDictionary = 
			tm.generateChordDictionary(tablatureTranscriptionPairs);
		List<List<Integer>> voiceAssignmentDictionary =
			tm.generateMappingDictionary(tablatureTranscriptionPairs, highestNumberOfVoices);	

		Integer[][] actual = tm.generateObservationProbabilityMatrix(tablatureTranscriptionPairs,
			chordDictionary, voiceAssignmentDictionary, highestNumberOfVoices);
		assertEquals(expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i].length, actual[i].length);
			for (int j = 0; j < expected[i].length; j++) {
				assertEquals(expected[i][j], actual[i][j]);
			}
		}
	}


	public void testGenerateObservationProbabilityMatrixNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		// For each TablatureTranscriptionPair (which are the same) there are
		// 12 unique chords (rows) and 8 unique voice assignments (columns), where
		// chord 0 has voice assignment 0 (1 time), and no others
		// chord 1 has voice assignment 0 (1 time), and no others
		// chord 2 has voice assignment 1 (1 time), and no others
		// chord 3 has voice assignment 2 (1 time), and no others
		// chord 4 has voice assignment 3 (1 time), and no others
		// chord 5 has voice assignment 4 (1 time), and no others
		// chord 6 has voice assignment 5 (1 time), and no others
		// chord 7 has voice assignment 6 (1 time), and no others
		// chord 8 has voice assignment 0 (2 times), and no others   
		// chord 9 has voice assignment 7 (3 times), and no others
		// chord 10 has voice assignment 7 (2 times), and no others
		// chord 11 has voice assignment 7 (1 time), and no others
//		Integer[][] expected = new Integer[12][8];
//		expected[0] = new Integer[]{1+2+10, 1,      1,      1,      1,      1+10,   1,      1}; 
//		expected[1] = new Integer[]{1+2+10, 1,      1,      1,      1,      1+10,   1,      1};
//		expected[2] = new Integer[]{1,      1+2+10, 1,      1+10,   1,      1,      1,      1+10};
//		expected[3] = new Integer[]{1,      1,      1+2+10, 1,      1+10,   1,      1,      1};
//		expected[4] = new Integer[]{1,      1+10,   1,      1+2+10, 1,      1,      1,      1+10};
//		expected[5] = new Integer[]{1,      1,      1+10,   1,      1+2+10, 1,      1,      1};
//		expected[6] = new Integer[]{1+10,   1,      1,      1,      1,      1+2+10, 1,      1}; 
//		expected[7] = new Integer[]{1,      1,      1,      1,      1,      1,      1+2+10, 1};
//		expected[8] = new Integer[]{1+4+10, 1,      1,      1,      1,      1+10,   1,      1};
//		expected[9] = new Integer[]{1,      1+10,   1,      1+10,   1,      1,      1,      1+6+10};
//		expected[10] = new Integer[]{1,     1+10,   1,      1+10,   1,      1,      1,      1+4+10};
//		expected[11] = new Integer[]{1,     1+10,   1,      1+10,   1,      1,      1,      1+2+10};

		Integer[][] expected = new Integer[12][8];
		expected[0] = new Integer[]{ 1+2, 0,   0,   0,   0,   1,   0,   0}; 
		expected[1] = new Integer[]{ 1+2, 0,   0,   0,   0,   1,   0,   0};
		expected[2] = new Integer[]{ 0,   1+2, 0,   1,   0,   0,   0,   1};
		expected[3] = new Integer[]{ 0,   0,   1+2, 0,   1,   0,   0,   0};
		expected[4] = new Integer[]{ 0,   1,   0,   1+2, 0,   0,   0,   1};
		expected[5] = new Integer[]{ 0,   0,   1,   0,   1+2, 0,   0,   0};
		expected[6] = new Integer[]{ 1,   0,   0,   0,   0,   1+2, 0,   0}; 
		expected[7] = new Integer[]{ 0,   0,   0,   0,   0,   0,   1+2, 0};
		expected[8] = new Integer[]{ 1+4, 0,   0,   0,   0,   1,   0,   0};
		expected[9] = new Integer[]{ 0,   1,   0,   1,   0,   0,   0,   1+6};
		expected[10] = new Integer[]{0,   1,   0,   1,   0,   0,   0,   1+4};
		expected[11] = new Integer[]{0,   1,   0,   1,   0,   0,   0,   1+2};

		int highestNumberOfVoices = transcription.getNumberOfVoices();
		List<TablatureTranscriptionPair> tablatureTranscriptionPairs = new ArrayList<TablatureTranscriptionPair>();
		tablatureTranscriptionPairs.add(new TablatureTranscriptionPair(null, transcription));
		tablatureTranscriptionPairs.add(new TablatureTranscriptionPair(null, transcription));
		TrainingManager tm = new TrainingManager();
		List<List<Integer>> chordDictionary = 
			tm.generateChordDictionary(tablatureTranscriptionPairs);
		List<List<Integer>> voiceAssignmentDictionary =
			tm.generateMappingDictionary(tablatureTranscriptionPairs, highestNumberOfVoices);
		Integer[][] actual = tm.generateObservationProbabilityMatrix(tablatureTranscriptionPairs,
			chordDictionary, voiceAssignmentDictionary, highestNumberOfVoices);

		assertEquals(expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i].length, actual[i].length);
			for (int j = 0; j < expected[i].length; j++) {
				assertEquals(expected[i][j], actual[i][j]);
			}
		}
	}

	
	public void testGenerateTransitionProbabilityMatrix() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		// For each TablatureTranscriptionPair (which are the same) there are 
		// 9 unique voice assignments, where
		// voice assignment 0 is followed by voice assignments 1 (1 time) and 8 (1 time)
		// voice assignment 1 is followed by voice assignment 2 (1 time)
		// voice assignment 2 is followed by voice assignment 3 (1 time)
		// voice assignment 3 is followed by voice assignment 4 (1 time)
		// voice assignment 4 is followed by voice assignment 5 (1 time)
		// voice assignment 5 is followed by voice assignment 6 (1 time)
		// voice assignment 6 is followed by voice assignment 7 (1 time)
		// voice assignment 7 is followed by voice assignment 0 (1 time)
		// voice assignment 8 is followed by voice assignments 0 (1 time) and 8 (5 times)
		Integer[][] expected = new Integer[9][9];
		expected[0] = new Integer[]{1, 1+2, 1, 1, 1, 1, 1, 1, 1+2}; 
		expected[1] = new Integer[]{1, 1, 1+2, 1, 1, 1, 1, 1, 1};
		expected[2] = new Integer[]{1, 1, 1, 1+2, 1, 1, 1, 1, 1};
		expected[3] = new Integer[]{1, 1, 1, 1, 1+2, 1, 1, 1, 1};
		expected[4] = new Integer[]{1, 1, 1, 1, 1, 1+2, 1, 1, 1};
		expected[5] = new Integer[]{1, 1, 1, 1, 1, 1, 1+2, 1, 1};
		expected[6] = new Integer[]{1, 1, 1, 1, 1, 1, 1, 1+2, 1}; 
		expected[7] = new Integer[]{1+2, 1, 1, 1, 1, 1, 1, 1, 1}; 
		expected[8] = new Integer[]{1+2, 1, 1, 1, 1, 1, 1, 1, 1+10}; 

		List<TablatureTranscriptionPair> tablatureTranscriptionPairs = new ArrayList<TablatureTranscriptionPair>();
		tablatureTranscriptionPairs.add(new TablatureTranscriptionPair(tablature, transcription));
		tablatureTranscriptionPairs.add(new TablatureTranscriptionPair(tablature, transcription));
		int highestNumberOfVoices = transcription.getNumberOfVoices();
		TrainingManager tm = new TrainingManager();
		List<List<Integer>> voiceAssignmentDictionary = 
			tm.generateMappingDictionary(tablatureTranscriptionPairs, highestNumberOfVoices);
		Integer[][] actual = tm.generateTransitionProbabilityMatrix(tablatureTranscriptionPairs,
			voiceAssignmentDictionary, highestNumberOfVoices);

		assertEquals(expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i].length, actual[i].length);
			for (int j = 0; j < expected[i].length; j++) {
				assertEquals(expected[i][j], actual[i][j]);
			}
		}
	}
	
	
	public void testGenerateTransitionProbabilityMatrixNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		// For each TablatureTranscriptionPair (which are the same) there are 
		// 8 unique voice assignments, where
		// voice assignment 0 is followed by voice assignments 0 (1 time), 1 (1 time), and 7 (1 time)
		// voice assignment 1 is followed by voice assignment 2 (1 time)
		// voice assignment 2 is followed by voice assignment 3 (1 time)
		// voice assignment 3 is followed by voice assignment 4 (1 time)
		// voice assignment 4 is followed by voice assignment 5 (1 time)
		// voice assignment 5 is followed by voice assignment 6 (1 time)
		// voice assignment 6 is followed by voice assignment 0 (1 time)
		// voice assignment 7 is followed by voice assignment 0 (1 time) and 7 (5 times)

		Integer[][] expected = new Integer[8][8];
		expected[0] = new Integer[]{1+2, 1+2, 1, 1, 1, 1, 1, 1+2}; 
		expected[1] = new Integer[]{1, 1, 1+2, 1, 1, 1, 1, 1};
		expected[2] = new Integer[]{1, 1, 1, 1+2, 1, 1, 1, 1};
		expected[3] = new Integer[]{1, 1, 1, 1, 1+2, 1, 1, 1};
		expected[4] = new Integer[]{1, 1, 1, 1, 1, 1+2, 1, 1};
		expected[5] = new Integer[]{1, 1, 1, 1, 1, 1, 1+2, 1};
		expected[6] = new Integer[]{1+2, 1, 1, 1, 1, 1, 1, 1}; 
		expected[7] = new Integer[]{1+2, 1, 1, 1, 1, 1, 1, 1+10}; 

		List<TablatureTranscriptionPair> tablatureTranscriptionPairs = new ArrayList<TablatureTranscriptionPair>();
		tablatureTranscriptionPairs.add(new TablatureTranscriptionPair(null, transcription));
		tablatureTranscriptionPairs.add(new TablatureTranscriptionPair(null, transcription));
		int highestNumberOfVoices = transcription.getNumberOfVoices();
		TrainingManager tm = new TrainingManager();
		List<List<Integer>> voiceAssignmentDictionary = 
			tm.generateMappingDictionary(tablatureTranscriptionPairs, highestNumberOfVoices);
		Integer[][] actual = 
			tm.generateTransitionProbabilityMatrix(tablatureTranscriptionPairs,
			voiceAssignmentDictionary, highestNumberOfVoices);

		assertEquals(expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i].length, actual[i].length);
			for (int j = 0; j < expected[i].length; j++) {
				assertEquals(expected[i][j], actual[i][j]);
			}
		}
	}
	

}
