package machineLearning;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import machineLearning.ErrorCalculator;
import representations.Transcription;
import structure.ScorePiece;
import ui.Runner;
import ui.UI;
import utility.DataConverterTest;
import junit.framework.TestCase;
import de.uos.fmt.musitech.data.structure.Note;
import de.uos.fmt.musitech.utility.math.Rational;

public class ErrorCalculatorTest extends TestCase {

	private File midiTestpiece1;
	private File encodingTestpiece1;
	
	private static final List<Double> V_0 = Transcription.createVoiceLabel(new Integer[]{0});
	private static final List<Double> V_0_1 = Transcription.createVoiceLabel(new Integer[]{0, 1});
	private static final List<Double> V_1 = Transcription.createVoiceLabel(new Integer[]{1});
	private static final List<Double> V_2 = Transcription.createVoiceLabel(new Integer[]{2});
	private static final List<Double> V_3 = Transcription.createVoiceLabel(new Integer[]{3});
	private static final List<Double> V_4 = Transcription.createVoiceLabel(new Integer[]{4});
	private static final List<Double> QUARTER = Transcription.createDurationLabel(new Integer[]{8*3});
	private static final List<Double> HALF = Transcription.createDurationLabel(new Integer[]{16*3});


	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		Runner.setPathsToCodeAndData(UI.getRootPath(), false);
		midiTestpiece1 = new File(Runner.midiPath + "test/" + "testpiece.mid");
		encodingTestpiece1 = new File(Runner.encodingsPath + "test/" + "testpiece.tbp");
	}


	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	
	private List<List<List<Integer>>> getAssignmentErrors() {
		List<List<Integer>> assigErrsTab = new ArrayList<List<Integer>>();
		int numNotes = 124;
		int incorr = 24;
		int overl = 16;
		int superfl = 17;
		int half = 22;
		assigErrsTab.add(Arrays.asList(new Integer[]{numNotes, incorr, overl, superfl, half}));
		assigErrsTab.add(new ArrayList<Integer>());
		assigErrsTab.add(new ArrayList<Integer>());
		assigErrsTab.add(new ArrayList<Integer>());
		assigErrsTab.add(new ArrayList<Integer>());
		int incorrDur = 34;
		int overlDur = 26;
		assigErrsTab.add(Arrays.asList(new Integer[]{numNotes, incorrDur, overlDur}));
		assigErrsTab.add(new ArrayList<Integer>());
		assigErrsTab.add(new ArrayList<Integer>());

		List<List<Integer>> assigErrsNonTab = 
			new ArrayList<List<Integer>>(assigErrsTab.subList(0, ErrorCalculator.GENERAL_DUR));
		assigErrsNonTab.set(0, Arrays.asList(new Integer[]{numNotes, incorr, 0, 0, 0}));

		List<List<List<Integer>>> assigErrs = new ArrayList<List<List<Integer>>>();
		assigErrs.add(assigErrsTab);
		assigErrs.add(assigErrsNonTab);
		
		return assigErrs;
		
	}
	
	
	private List<List<List<Integer>>> getPredictedVoicesToyExamples() {
		List<List<List<Integer>>> allPredictedVoices = new ArrayList<List<List<Integer>>>();

		List<Integer> predVoice0 = Arrays.asList(new Integer[]{0});
		List<Integer> predVoice1 = Arrays.asList(new Integer[]{1});
		List<Integer> predVoice2 = Arrays.asList(new Integer[]{2});
		List<Integer> predVoice3 = Arrays.asList(new Integer[]{3});
		List<Integer> predVoice1And0 = Arrays.asList(new Integer[]{0, 1});
//		List<Integer> predVoice2And1 = Arrays.asList(new Integer[]{1, 2});
		List<Integer> predVoice3And1 = Arrays.asList(new Integer[]{1, 3});
		List<Integer> predVoice3And2 = Arrays.asList(new Integer[]{3, 2});

		// Toy example 1
		List<List<Integer>> allPredictedVoices1 = new ArrayList<List<Integer>>();
		allPredictedVoices1.add(predVoice3); allPredictedVoices1.add(predVoice1); allPredictedVoices1.add(predVoice0);
		allPredictedVoices1.add(predVoice1And0); // correct
		allPredictedVoices1.add(predVoice3); allPredictedVoices1.add(predVoice1); allPredictedVoices1.add(predVoice0);
		allPredictedVoices1.add(predVoice3); allPredictedVoices1.add(predVoice1); allPredictedVoices1.add(predVoice0);
		allPredictedVoices1.add(predVoice0); // overlooked
		allPredictedVoices1.add(predVoice3And1); // superfluous
		allPredictedVoices1.add(predVoice1And0); // half
		allPredictedVoices1.add(predVoice3); allPredictedVoices1.add(predVoice1); allPredictedVoices1.add(predVoice0);
		allPredictedVoices1.add(predVoice3And2); // incorrect
		allPredictedVoices1.add(predVoice2); allPredictedVoices1.add(predVoice3); allPredictedVoices1.add(predVoice0);
		// Toy example 2
		List<List<Integer>> allPredictedVoices2 = new ArrayList<List<Integer>>();
		allPredictedVoices2.add(predVoice3); allPredictedVoices2.add(predVoice0); allPredictedVoices2.add(predVoice1);
		allPredictedVoices2.add(predVoice1And0);
		allPredictedVoices2.add(predVoice3); allPredictedVoices2.add(predVoice0); allPredictedVoices2.add(predVoice1);
		allPredictedVoices2.add(predVoice1); allPredictedVoices2.add(predVoice3); allPredictedVoices2.add(predVoice0);
		allPredictedVoices2.add(predVoice1);
		allPredictedVoices2.add(predVoice3And1); 
		allPredictedVoices2.add(predVoice1And0);
		allPredictedVoices2.add(predVoice3); allPredictedVoices2.add(predVoice1); allPredictedVoices2.add(predVoice0);
		allPredictedVoices2.add(predVoice3And2);
		allPredictedVoices2.add(predVoice3); allPredictedVoices2.add(predVoice0); allPredictedVoices2.add(predVoice2);
		// Toy example 3
		List<List<Integer>> allPredictedVoices3 = new ArrayList<List<Integer>>();
		allPredictedVoices3.add(predVoice3); allPredictedVoices3.add(predVoice1); allPredictedVoices3.add(predVoice0);
		allPredictedVoices3.add(predVoice0); // overlooked
		allPredictedVoices3.add(predVoice3); allPredictedVoices3.add(predVoice1); allPredictedVoices3.add(predVoice0);
		allPredictedVoices3.add(predVoice3); allPredictedVoices3.add(predVoice1); allPredictedVoices3.add(predVoice0);
		allPredictedVoices3.add(predVoice1And0);
		allPredictedVoices3.add(predVoice3); 
		allPredictedVoices3.add(predVoice3And1); 
		allPredictedVoices3.add(predVoice3); allPredictedVoices3.add(predVoice1); allPredictedVoices3.add(predVoice0);
		allPredictedVoices3.add(predVoice1And0); 
		allPredictedVoices3.add(predVoice3); allPredictedVoices3.add(predVoice1); allPredictedVoices3.add(predVoice0);			

		allPredictedVoices.add(allPredictedVoices1); allPredictedVoices.add(allPredictedVoices2); 
		allPredictedVoices.add(allPredictedVoices3);

		return allPredictedVoices;
	}


	private List<List<List<Integer>>> getPredictedVoicesToyExamplesNonTab() {
		List<List<List<Integer>>> allPredictedVoices = new ArrayList<List<List<Integer>>>();

		List<Integer> predVoice0 = Arrays.asList(new Integer[]{0});
		List<Integer> predVoice1 = Arrays.asList(new Integer[]{1});
		List<Integer> predVoice2 = Arrays.asList(new Integer[]{2});

		// Toy example 1
		List<List<Integer>> allPredictedVoices1 = new ArrayList<List<Integer>>();
		allPredictedVoices1.add(predVoice2); allPredictedVoices1.add(predVoice1); allPredictedVoices1.add(predVoice0);
		allPredictedVoices1.add(predVoice1);
		allPredictedVoices1.add(predVoice2); allPredictedVoices1.add(predVoice1); allPredictedVoices1.add(predVoice0);
		allPredictedVoices1.add(predVoice2); allPredictedVoices1.add(predVoice1); allPredictedVoices1.add(predVoice0);
		allPredictedVoices1.add(predVoice0);
		allPredictedVoices1.add(predVoice2); 
		allPredictedVoices1.add(predVoice1);
		allPredictedVoices1.add(predVoice2); allPredictedVoices1.add(predVoice1); allPredictedVoices1.add(predVoice0);
		allPredictedVoices1.add(predVoice0);
		allPredictedVoices1.add(predVoice1); allPredictedVoices1.add(predVoice2); allPredictedVoices1.add(predVoice0);			
		// Toy example 2
		List<List<Integer>> allPredictedVoices2 = new ArrayList<List<Integer>>();
		allPredictedVoices2.add(predVoice2); allPredictedVoices2.add(predVoice0); allPredictedVoices2.add(predVoice1);
		allPredictedVoices2.add(predVoice0);
		allPredictedVoices2.add(predVoice2); allPredictedVoices2.add(predVoice0); allPredictedVoices2.add(predVoice1);
		allPredictedVoices2.add(predVoice1); allPredictedVoices2.add(predVoice2); allPredictedVoices2.add(predVoice0);
		allPredictedVoices2.add(predVoice1);
		allPredictedVoices2.add(predVoice2); 
		allPredictedVoices2.add(predVoice1);
		allPredictedVoices2.add(predVoice2); allPredictedVoices2.add(predVoice1); allPredictedVoices2.add(predVoice0);
		allPredictedVoices2.add(predVoice1);
		allPredictedVoices2.add(predVoice2); allPredictedVoices2.add(predVoice0); allPredictedVoices2.add(predVoice1);
		// Toy example 3
		// Is the same as toy example 2, but now indices 1-2, 8-9, 14-15, and 17-18 are EDU 
		List<List<Integer>> allPredictedVoices3 = new ArrayList<List<Integer>>(allPredictedVoices2);

		allPredictedVoices.add(allPredictedVoices1); allPredictedVoices.add(allPredictedVoices2);
		allPredictedVoices.add(allPredictedVoices3);

		return allPredictedVoices;
	}


	private List<List<Double>> getActualVoicesToyExamples() {				
		// Toy examples 1-3
		List<List<Double>> actualVoiceLabels = new ArrayList<List<Double>>();
		actualVoiceLabels.add(V_3); actualVoiceLabels.add(V_1); actualVoiceLabels.add(V_0);
		actualVoiceLabels.add(DataConverterTest.combineLabels(V_1, V_0));
		actualVoiceLabels.add(V_3); actualVoiceLabels.add(V_1); actualVoiceLabels.add(V_0);
		actualVoiceLabels.add(V_3); actualVoiceLabels.add(V_1); actualVoiceLabels.add(V_0);
		actualVoiceLabels.add(DataConverterTest.combineLabels(V_1, V_0));
		actualVoiceLabels.add(V_3);
		actualVoiceLabels.add(DataConverterTest.combineLabels(V_3, V_1));
		actualVoiceLabels.add(V_3); actualVoiceLabels.add(V_1); actualVoiceLabels.add(V_0);
		actualVoiceLabels.add(DataConverterTest.combineLabels(V_1, V_0));
		actualVoiceLabels.add(V_3); actualVoiceLabels.add(V_1); actualVoiceLabels.add(V_0);

		return actualVoiceLabels;
	}


	private List<List<Double>> getActualVoicesToyExamplesNonTab() {				
		// Toy examples 1-3
		List<List<Double>> actualVoiceLabels = new ArrayList<List<Double>>();
		actualVoiceLabels.add(V_2); actualVoiceLabels.add(V_1); actualVoiceLabels.add(V_0);
		actualVoiceLabels.add(V_0);
		actualVoiceLabels.add(V_2); actualVoiceLabels.add(V_1); actualVoiceLabels.add(V_0);
		actualVoiceLabels.add(V_2); actualVoiceLabels.add(V_1); actualVoiceLabels.add(V_0);
		actualVoiceLabels.add(V_0);
		actualVoiceLabels.add(V_2);
		actualVoiceLabels.add(V_2);
		actualVoiceLabels.add(V_2); actualVoiceLabels.add(V_1); actualVoiceLabels.add(V_0);
		actualVoiceLabels.add(V_0);
		actualVoiceLabels.add(V_2); actualVoiceLabels.add(V_1); actualVoiceLabels.add(V_0);

		return actualVoiceLabels;
	}


	private List<List<Integer[]>> getEDUInfoToyExamples() {
		List<List<Integer[]>> allEDUInfo = new ArrayList<List<Integer[]>>();

		// Toy example 1-2
		List<Integer[]> eDUInfo1 = null;
		List<Integer[]> eDUInfo2 = null;
		// Toy example 3
		List<Integer[]> eDUInfo3 = new ArrayList<Integer[]>();
		for (int i = 0; i < 20; i++) {
			eDUInfo3.add(null);
		} 
		eDUInfo3.set(1, new Integer[]{1, 0, 2}); 
		eDUInfo3.set(2, new Integer[]{1, 0, 1});
		eDUInfo3.set(8, new Integer[]{1, 0, 9});
		eDUInfo3.set(9, new Integer[]{1, 0, 8});
		eDUInfo3.set(14, new Integer[]{1, 0, 15});
		eDUInfo3.set(15, new Integer[]{1, 0, 14});
		eDUInfo3.set(17, new Integer[]{2, 1, 18});
		eDUInfo3.set(18, new Integer[]{2, 1, 17});

		allEDUInfo.add(eDUInfo1); allEDUInfo.add(eDUInfo2);
		allEDUInfo.add(eDUInfo3);

		return allEDUInfo;
	}


	public void testCalculateAssignmentErrors() {
		List<List<Integer>> allPredictedVoices = new ArrayList<List<Integer>>();
		List<List<Double>> groundTruthVoiceLabels = new ArrayList<List<Double>>();
		List<Rational[]> allPredictedDurations = new ArrayList<Rational[]>();
		List<List<Double>> groundTruthDurationLabels = new ArrayList<List<Double>>();
		List<Double> quarter = QUARTER;
//		List<Double> quarter = Transcription.createDurationLabel(8);
		List<Double> half = HALF;
//		List<Double> half = Transcription.createDurationLabel(16);

		// 1. predictedVoices/-Durations contain one element
		// a. actualVoices/-Durations contain one element
		allPredictedVoices.add(Arrays.asList(new Integer[]{1})); // correct
		allPredictedVoices.add(Arrays.asList(new Integer[]{1})); // incorrect
		allPredictedDurations.add(new Rational[]{new Rational(1, 2)}); // correct
		allPredictedDurations.add(new Rational[]{new Rational(1, 2)}); // incorrect
		groundTruthVoiceLabels.add(V_1); 
		groundTruthVoiceLabels.add(V_2);
		groundTruthDurationLabels.add(half);
		groundTruthDurationLabels.add(quarter);
		// b. actualVoices/-Durations contain two elements
		allPredictedVoices.add(Arrays.asList(new Integer[]{1})); // overlooked
		allPredictedVoices.add(Arrays.asList(new Integer[]{1})); // overlooked
		allPredictedVoices.add(Arrays.asList(new Integer[]{1})); // incorrect
		allPredictedDurations.add(new Rational[]{new Rational(1, 2)}); // correct
		allPredictedDurations.add(new Rational[]{new Rational(1, 2)}); // half
		allPredictedDurations.add(new Rational[]{new Rational(1, 8)}); // incorrect
		groundTruthVoiceLabels.add(DataConverterTest.combineLabels(V_0, V_1));
		groundTruthVoiceLabels.add(DataConverterTest.combineLabels(V_0, V_1)); 
		groundTruthVoiceLabels.add(DataConverterTest.combineLabels(V_0, V_2));
		groundTruthDurationLabels.add(half); // CoD with two same durations
		groundTruthDurationLabels.add(DataConverterTest.combineLabels(quarter, half));
		groundTruthDurationLabels.add(DataConverterTest.combineLabels(quarter, half));

		// 2. predictedVoices contains two elements
		// NB: predictedDurations containing two elements currently not implemented
		// a. actualVoices contains one element
		allPredictedVoices.add(Arrays.asList(new Integer[]{1, 2})); // superfluous
		allPredictedVoices.add(Arrays.asList(new Integer[]{1, 2})); // incorrect
		allPredictedDurations.add(new Rational[]{new Rational(1, 2)}); // correct
		allPredictedDurations.add(new Rational[]{new Rational(1, 4)}); // incorrect 
		groundTruthVoiceLabels.add(V_1); 
		groundTruthVoiceLabels.add(V_0);
		groundTruthDurationLabels.add(half);
		groundTruthDurationLabels.add(half);
		// b. actualVoices contains two elements
		allPredictedVoices.add(Arrays.asList(new Integer[]{1, 2})); // correct
		allPredictedVoices.add(Arrays.asList(new Integer[]{1, 2})); // half
		allPredictedVoices.add(Arrays.asList(new Integer[]{1, 2})); // incorrect
		allPredictedDurations.add(new Rational[]{new Rational(1, 2)}); // correct 
		allPredictedDurations.add(new Rational[]{new Rational(1, 2)}); // half
		allPredictedDurations.add(new Rational[]{new Rational(1, 8)}); // incorrect
		groundTruthVoiceLabels.add(DataConverterTest.combineLabels(V_1, V_2));
		groundTruthVoiceLabels.add(DataConverterTest.combineLabels(V_0, V_2));
		groundTruthVoiceLabels.add(DataConverterTest.combineLabels(V_0, V_3));
		groundTruthDurationLabels.add(half); // CoD with two same durations
		groundTruthDurationLabels.add(DataConverterTest.combineLabels(quarter, half));
		groundTruthDurationLabels.add(DataConverterTest.combineLabels(quarter, half));

		// 1. Voices
		List<List<Integer>> expected = new ArrayList<List<Integer>>();
		List<Integer> general = Arrays.asList(new Integer[]{10, 4, 2, 1, 1});
		List<Integer> indicesOfIncorrectAssignments = Arrays.asList(new Integer[]{1, 4, 6, 9});
		List<Integer> indicesOfOverlookedCoDAssignments = Arrays.asList(new Integer[]{2, 3});
		List<Integer> indicesOfSuperfluousCoDAssignments = Arrays.asList(new Integer[]{5});
		List<Integer> indicesOfHalfCoDAssignments = Arrays.asList(new Integer[]{8});
		expected.add(general); expected.add(indicesOfIncorrectAssignments); expected.add(indicesOfOverlookedCoDAssignments);
		expected.add(indicesOfSuperfluousCoDAssignments); expected.add(indicesOfHalfCoDAssignments);
		// 2. Durations
		List<Integer> generalDur = Arrays.asList(new Integer[]{10, 4, 2});
		List<Integer> indicesOfIncorrectDurationAssignments = Arrays.asList(new Integer[]{1, 4, 6, 9});
		List<Integer> indicesOfOverlookedCoDDurationAssignments = Arrays.asList(new Integer[]{3, 8});
		expected.add(generalDur); expected.add(indicesOfIncorrectDurationAssignments); 
		expected.add(indicesOfOverlookedCoDDurationAssignments);

		List<List<Integer>> actual = 
			ErrorCalculator.calculateAssignmentErrors(allPredictedVoices,
			groundTruthVoiceLabels, allPredictedDurations, groundTruthDurationLabels, 
			null);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}


	public void testCalculateAssignmentErrorsNonTab() {
		// Make test values
		List<Integer[]> equalDurationUnisonsInfo = new ArrayList<Integer[]>();
		equalDurationUnisonsInfo.add(null); 
		equalDurationUnisonsInfo.add(null); 
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 1}); // voiceA == 2; voiceB == 3
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 0}); // voiceA == 2; voiceB == 3
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 1}); // voiceA == 2; voiceB == 3
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 0}); // voiceA == 2; voiceB == 3
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 1}); // voiceA == 2; voiceB == 3
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 0}); // voiceA == 2; voiceB == 3
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 1}); // voiceA == 2; voiceB == 3
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 0}); // voiceA == 2; voiceB == 3
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 1}); // voiceA == 2; voiceB == 3
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 0}); // voiceA == 2; voiceB == 3
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 1}); // voiceA == 2; voiceB == 3
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 0}); // voiceA == 2; voiceB == 3
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 1}); // voiceA == 2; voiceB == 3
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 0}); // voiceA == 2; voiceB == 3
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 1}); // voiceA == 2; voiceB == 3
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 0}); // voiceA == 2; voiceB == 3
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 1}); // voiceA == 2; voiceB == 3
		equalDurationUnisonsInfo.add(new Integer[]{3, 2, 0}); // voiceA == 2; voiceB == 3

		List<List<Integer>> allPredictedVoices = new ArrayList<List<Integer>>();
		allPredictedVoices.add(Arrays.asList(new Integer[]{1})); // correct
		allPredictedVoices.add(Arrays.asList(new Integer[]{1})); // incorrect
		// (1) predicted lower correct (voiceA)
		// predicted upper incorrect
		allPredictedVoices.add(Arrays.asList(new Integer[]{2})); // correct
		allPredictedVoices.add(Arrays.asList(new Integer[]{2})); // incorrect
		// predicted upper correct
		allPredictedVoices.add(Arrays.asList(new Integer[]{2})); // correct
		allPredictedVoices.add(Arrays.asList(new Integer[]{3})); // correct
		// predicted upper incorrect
		allPredictedVoices.add(Arrays.asList(new Integer[]{2})); // correct
		allPredictedVoices.add(Arrays.asList(new Integer[]{4})); // incorrect
		// (2) predicted lower correct (voiceB)
		// predicted upper correct
		allPredictedVoices.add(Arrays.asList(new Integer[]{3})); // correct
		allPredictedVoices.add(Arrays.asList(new Integer[]{2})); // correct
		// predicted upper incorrect
		allPredictedVoices.add(Arrays.asList(new Integer[]{3})); // correct
		allPredictedVoices.add(Arrays.asList(new Integer[]{3})); // incorrect
		// predicted upper incorrect
		allPredictedVoices.add(Arrays.asList(new Integer[]{3})); // correct
		allPredictedVoices.add(Arrays.asList(new Integer[]{4})); // incorrect
		// (3) predicted lower incorrect   
		// predicted upper correct
		allPredictedVoices.add(Arrays.asList(new Integer[]{4})); // incorrect
		allPredictedVoices.add(Arrays.asList(new Integer[]{2})); // correct
		// predicted upper correct
		allPredictedVoices.add(Arrays.asList(new Integer[]{4})); // incorrect
		allPredictedVoices.add(Arrays.asList(new Integer[]{3})); // correct
		// predicted upper incorrect
		allPredictedVoices.add(Arrays.asList(new Integer[]{4})); // incorrect
		allPredictedVoices.add(Arrays.asList(new Integer[]{4})); // incorrect

		List<List<Double>> groundTruthVoiceLabels = new ArrayList<List<Double>>();
		groundTruthVoiceLabels.add(V_1);
		groundTruthVoiceLabels.add(V_2);
		// The voice labels for the equal duration unison notes can remain empty as they are not used  
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{})); 
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{}));
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{}));
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{}));
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{}));
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{}));
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{}));
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{}));
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{}));
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{}));
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{}));
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{}));
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{}));
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{}));
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{}));
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{}));
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{}));
		groundTruthVoiceLabels.add(Arrays.asList(new Double[]{}));

		List<List<Integer>> expected = new ArrayList<List<Integer>>();
		expected.add(Arrays.asList(new Integer[]{20, 9, 0, 0, 0}));
		expected.add(Arrays.asList(new Integer[]{1, 3, 7, 11, 13, 14, 16, 18, 19}));
		expected.add(Arrays.asList(new Integer[]{}));
		expected.add(Arrays.asList(new Integer[]{}));
		expected.add(Arrays.asList(new Integer[]{}));

		// Calculate actual
		List<List<Integer>> actual = 
			ErrorCalculator.calculateAssignmentErrors(allPredictedVoices, 
			groundTruthVoiceLabels, null, null, equalDurationUnisonsInfo);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}


	public void testCalculateAccuracy() {
		// Tablature case
		List<List<Integer>> assigErrsTab = getAssignmentErrors().get(0);
		// Non-tablature case
		List<List<Integer>> assigErrsNonTab = getAssignmentErrors().get(1);
		
		// voice tab: (2*124 - 2*(24 + 16/2 + 17/2 + 22/2)) / 2*124 = 145/248
		// duration tab: (2*124 - 2*(34 + 26/2)) / 2*124 = 154/248
		// voice non-tab: 124 - 24 / 124 = 100/124
		List<ErrorFraction> expected = Arrays.asList(
			new ErrorFraction(145, 248), 
			new ErrorFraction(154, 248),
			new ErrorFraction(100, 124)
		);

		List<ErrorFraction> actual = new ArrayList<ErrorFraction>();
		actual.add(ErrorCalculator.calculateAccuracy(assigErrsTab, true, false));
		actual.add(ErrorCalculator.calculateAccuracy(assigErrsTab, true, true));
		actual.add(ErrorCalculator.calculateAccuracy(assigErrsNonTab, false, false));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}


	public void testCalculateClassificationError() {
		// Only the first element of assignmentErrors is needed
		List<List<Integer>> assignmentErrors = new ArrayList<List<Integer>>();
		assignmentErrors.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{500, 20, 30, 40, 50})));

		double expected = 0.16;
		double actual = ErrorCalculator.calculateClassificationError(assignmentErrors);

		assertEquals(expected, actual);
	}


	public void testGetMisassignments() {
		List<double[]> expected = new ArrayList<double[]>();
		
		// Tablature case
		// Voice: 24 + 0.5*(16+17+22) = 51.5
		// Duration: 34 + 0.5*26 = 47
		expected.add(new double[]{51.5, 47});
		
		// Non-tablature case
		// Voice: 24 + 0.5*(0) = 24
		expected.add(new double[]{24, -1});
		
		List<double[]> actual = new ArrayList<double[]>();
		actual.add(ErrorCalculator.getMisassignments(getAssignmentErrors().get(0)));
		actual.add(ErrorCalculator.getMisassignments(getAssignmentErrors().get(1)));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j]);
			}
		}	
	}


	public void testCalculateAvgPrecisionRecallF1() {	  	
		List<ErrorFraction[][]> expected = new ArrayList<ErrorFraction[][]>();

		// Toy example 1
		ErrorFraction[][] exp1 = new ErrorFraction[2][3];
		// a. Wtd avg
		ErrorFraction wtdAvgPrecision1 = new ErrorFraction(7+4+5, 7+7+6);
		ErrorFraction wtdAvgRecall1 = new ErrorFraction(7+4+5, 8+5+7);
		exp1[0] = new ErrorFraction[]{wtdAvgPrecision1, wtdAvgRecall1, null};
		// b. Avg
		// prc = (7/7 + 4/7 + 5/6) / 3 = 101/42 * 1/3 = 101/126 (reduced)
		ErrorFraction avgPrecision1 = new ErrorFraction(101, 126);
		// rcl = (7/8 + 4/5 + 5/7) / 3 = 669/280 * 1/3 = 223/280 (reduced)
		ErrorFraction avgRecall1 = new ErrorFraction(223, 280);
		// f1-score
		// voice 0: (7/7 * 7/8 *2) / (7/7 + 7/8) = (14/8) / (15/8) = 14/15 (reduced) 
		// voice 1: (4/7 * 4/5 *2) / (4/7 + 4/5) = (32/35) / (48/35) = 2/3 (reduced)
		// voice 2: (5/6 * 5/7 *2) / (5/6 + 5/7) = (50/42) / (65/42) = 10/13 (reduced)
		// sum = 154/65
		Rational avgF11 = (new Rational(154, 65).div(3));
		exp1[1] = new ErrorFraction[]{avgPrecision1, avgRecall1, 
			new ErrorFraction(avgF11.getNumer(), avgF11.getDenom())};
		expected.add(exp1);

		// Toy example 2
		ErrorFraction[][] exp2 = new ErrorFraction[2][3];
		// a. Wtd avg
		ErrorFraction wtdAvgPrecision2 = new ErrorFraction(3+1+5, 6+8+6);
		ErrorFraction wtdAvgRecall2 = new ErrorFraction(3+1+5, 8+5+7);
		exp2[0] = new ErrorFraction[]{wtdAvgPrecision2, wtdAvgRecall2, null};
		// b. Avg
		// prc = (3/6 + 1/8 + 5/6) / 3 = 35/24 * 1/3 = 35/72 (reduced)
		ErrorFraction avgPrecision2 = new ErrorFraction(35, 72);
		// rcl = (3/8 + 1/5 + 5/7) / 3 = 361/280 * 1v/3 = 361/840 (reduced)
		ErrorFraction avgRecall2 = new ErrorFraction(361, 840);
		// f1-score
		// voice 0: (3/6 * 3/8 *2) / (3/6 + 3/8) = (6/16) / (14/16) = 3/7 (reduced) 
		// voice 1: (1/8 * 1/5 *2) / (1/8 + 1/5) = (2/40) / (13/40) = 2/13 (reduced)
		// voice 2: (5/6 * 5/7 *2) / (5/6 + 5/7) = (50/42) / (65/42) = 10/13 (reduced)
		// sum = 123/91
		Rational avgF12 = (new Rational(123, 91).div(3));
		exp2[1] = new ErrorFraction[]{avgPrecision2, avgRecall2, 
			new ErrorFraction(avgF12.getNumer(), avgF12.getDenom())};
		expected.add(exp2);

		// Toy example 3
		// voice0 TP/FP/FN = [1, 3, 9, 15], [5, 18], [6, 10, 16, 19]
		// voice1 TP/FP/FN = [2, 14], [6, 7, 10, 12, 16, 19], [5, 8, 18]
		// voice2 TP/FP/FN = [0, 4, 11, 13, 17], [8], [7, 12]
		ErrorFraction[][] exp3 = new ErrorFraction[2][3];
		// a. Wtd avg
		ErrorFraction wtdAvgPrecision3 = new ErrorFraction(4+2+5, 6+8+6);
		ErrorFraction wtdAvgRecall3 = new ErrorFraction(4+2+5, 8+5+7);
		exp3[0] = new ErrorFraction[]{wtdAvgPrecision3, wtdAvgRecall3, null};
		// b. Avg
		// prc = (4/6 + 2/8 + 5/6) / 3 = 7/4 * 1/3 = 7/12 (reduced)
		ErrorFraction avgPrecision3 = new ErrorFraction(7, 12);
		// rcl = (4/8 + 2/5 + 5/7) / 3 = 113/70 * 1/3 = 113/210 (reduced)
		ErrorFraction avgRecall3 = new ErrorFraction(113, 210);
		// f1-score
		// voice 0: (4/6 * 4/8 *2) / (4/6 + 4/8) = (4/6) / (7/6) = 4/7 (reduced) 
		// voice 1: (2/8 * 2/5 *2) / (2/8 + 2/5) = (4/20) / (13/20) = 4/13 (reduced) 
		// voice 2: (5/6 * 5/7 *2) / (5/6 + 5/7) = (50/42) / (65/42) = 10/13 (reduced) 
		// sum = 150/91
		Rational avgF13 = (new Rational(150, 91).div(3));
		exp3[1] = new ErrorFraction[]{avgPrecision3, avgRecall3, 
			new ErrorFraction(avgF13.getNumer(), avgF13.getDenom())};
		expected.add(exp3);

		List<ErrorFraction[][]> actual = new ArrayList<ErrorFraction[][]>();
		actual.add(ErrorCalculator.calculateAvgPrecisionRecallF1(getPredictedVoicesToyExamplesNonTab().get(0), 
			getActualVoicesToyExamplesNonTab(), getEDUInfoToyExamples().get(0), 3));
		actual.add(ErrorCalculator.calculateAvgPrecisionRecallF1(getPredictedVoicesToyExamplesNonTab().get(1), 
			getActualVoicesToyExamplesNonTab(), getEDUInfoToyExamples().get(1), 3));
		actual.add(ErrorCalculator.calculateAvgPrecisionRecallF1(getPredictedVoicesToyExamplesNonTab().get(2), 
			getActualVoicesToyExamplesNonTab(), getEDUInfoToyExamples().get(2), 3));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j].length, actual.get(i)[j].length);
				for (int k = 0; k < expected.get(i)[j].length; k++) {
					assertEquals(expected.get(i)[j][k], actual.get(i)[j][k]);
				}
			}
		}
	}
	
	
	public void testGetPositivesAndNegativesPerVoice() {
		Transcription transcription = 
			new Transcription(midiTestpiece1, encodingTestpiece1);

		List<List<Integer>> allPredictedVoices = new ArrayList<List<Integer>>();
		List<Integer> voice0 = new ArrayList<Integer>(Arrays.asList(new Integer[]{0}));
		List<Integer> voice1 = new ArrayList<Integer>(Arrays.asList(new Integer[]{1}));
		List<Integer> voice2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{2}));
		List<Integer> voice3 = new ArrayList<Integer>(Arrays.asList(new Integer[]{3}));
		List<Integer> voice4 = new ArrayList<Integer>(Arrays.asList(new Integer[]{4}));
		List<Integer> voice0And2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{0, 2}));
		List<Integer> voice1And2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{1, 2}));
		// Chord 0 (should be 3, 2, 1, 0)
		allPredictedVoices.add(voice3); // (0): TP-v3 
		allPredictedVoices.add(voice4); // (1): FP-v4 & FN-v2 
		allPredictedVoices.add(voice0); // (2): FP-v0 & FN-v1
		allPredictedVoices.add(voice1); // (3): FP-v1 & FN-v0
		// Chord 1 (should be 3, 2, 0, 1)
		allPredictedVoices.add(voice3); // (4): TP-v3
		allPredictedVoices.add(voice1And2); // (5): TP-v2 / FP-v1  
		allPredictedVoices.add(voice0); // (6): TP-v0
		allPredictedVoices.add(voice4); // (7): FP-v4 & FN-v1
		// Chord 2 (should be 3)
		allPredictedVoices.add(voice2); // (8): FP-v2 & FN-v3
		// Chord 3 (should be 4, 3, 2, 0/1)
		allPredictedVoices.add(voice4); // (9): TP-v4
		allPredictedVoices.add(voice3); // (10): TP-v3
		allPredictedVoices.add(voice1); // (11): FP-v1 & FN-v2
		allPredictedVoices.add(voice0And2); // (12): TP-v0 / FP-v2 & FN-v1
		// Chord 4 (should be 4)
		allPredictedVoices.add(voice4); // (13): TP-v4
		// Chord 5 (should be 4, 3, 2, 1, 0)
		allPredictedVoices.add(voice3); // (14): FP-v3 & FN-v4 
		allPredictedVoices.add(voice0); // (15): FP-v0 & FN-v3
		allPredictedVoices.add(voice2); // (16): TP-v2
		allPredictedVoices.add(voice1); // (17): TP-v1
		allPredictedVoices.add(voice4); // (18): FP-v4 & FN-v0
		// Chord 6 (should be 4, 2, 0, 1)
		allPredictedVoices.add(voice4); // (19): TP-v4
		allPredictedVoices.add(voice3); // (20): FP-v3 & FN-v2
		allPredictedVoices.add(voice1); // (21): FP-v1 & FN-v0
		allPredictedVoices.add(voice2); // (22): FP-v2 & FN-v1
		// Chord 7 (should be 2, 0)
		allPredictedVoices.add(voice3); // (23): FP-v3 & FN-v2 
		allPredictedVoices.add(voice0); // (24): TP-v0
		// Chord 8 (should be 3, 2, 1, 0)
		allPredictedVoices.add(voice3); // (25): TP-v3
		allPredictedVoices.add(voice4); // (26): FP-v4 & FN-v2
		allPredictedVoices.add(voice1); // (27): TP-v1
		allPredictedVoices.add(voice0); // (28): TP-v0
		// Chord 9-14 (should all be 0)
		allPredictedVoices.add(voice0); // (29): TP-v0
		allPredictedVoices.add(voice0); // (30): TP-v0
		allPredictedVoices.add(voice0); // (31): TP-v0
		allPredictedVoices.add(voice0); // (32): TP-v0
		allPredictedVoices.add(voice0); // (33): TP-v0
		allPredictedVoices.add(voice0); // (34): TP-v0
		// Chord 15 (should be 3, 2, 1, 0)
		allPredictedVoices.add(voice3); // (35): TP-v3
		allPredictedVoices.add(voice2); // (36): TP-v2
		allPredictedVoices.add(voice1); // (37): TP-v1
		allPredictedVoices.add(voice0); // (38): TP-v0

		List<List<List<Integer>>> expected = new ArrayList<List<List<Integer>>>();
		List<List<Integer>> expectedVoice0 = new ArrayList<List<Integer>>();
		List<Integer> tP0 = new ArrayList<Integer>(Arrays.asList(new Integer[]{6, 12, 24, 28, 29, 30, 31, 32, 33, 34, 38}));
		List<Integer> fP0 = new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 15}));
		List<Integer> fN0 = new ArrayList<Integer>(Arrays.asList(new Integer[]{3, 18, 21}));
		expectedVoice0.add(tP0); expectedVoice0.add(fP0); expectedVoice0.add(fN0);
		List<List<Integer>> expectedVoice1 = new ArrayList<List<Integer>>();
		List<Integer> tP1 = new ArrayList<Integer>(Arrays.asList(new Integer[]{17, 27, 37}));
		List<Integer> fP1 = new ArrayList<Integer>(Arrays.asList(new Integer[]{3, 5, 11, 21}));
		List<Integer> fN1 = new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 7, 12, 22}));
		expectedVoice1.add(tP1); expectedVoice1.add(fP1); expectedVoice1.add(fN1);
		List<List<Integer>> expectedVoice2 = new ArrayList<List<Integer>>();
		List<Integer> tP2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{5, 16, 36}));
		List<Integer> fP2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{8, 12, 22}));
		List<Integer> fN2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{1, 11, 20, 23, 26}));
		expectedVoice2.add(tP2); expectedVoice2.add(fP2); expectedVoice2.add(fN2);
		List<List<Integer>> expectedVoice3 = new ArrayList<List<Integer>>();
		List<Integer> tP3 = new ArrayList<Integer>(Arrays.asList(new Integer[]{0, 4, 10, 25, 35}));
		List<Integer> fP3 = new ArrayList<Integer>(Arrays.asList(new Integer[]{14, 20, 23}));
		List<Integer> fN3 = new ArrayList<Integer>(Arrays.asList(new Integer[]{8, 15}));
		expectedVoice3.add(tP3); expectedVoice3.add(fP3); expectedVoice3.add(fN3);
		List<List<Integer>> expectedVoice4 = new ArrayList<List<Integer>>();
		List<Integer> tP4 = new ArrayList<Integer>(Arrays.asList(new Integer[]{9, 13, 19}));
		List<Integer> fP4 = new ArrayList<Integer>(Arrays.asList(new Integer[]{1, 7, 18, 26}));
		List<Integer> fN4 = new ArrayList<Integer>(Arrays.asList(new Integer[]{14}));
		expectedVoice4.add(tP4); expectedVoice4.add(fP4); expectedVoice4.add(fN4);

		expected.add(expectedVoice0); expected.add(expectedVoice1); expected.add(expectedVoice2); expected.add(expectedVoice3);
		expected.add(expectedVoice4);

		List<List<Double>> groundTruthVoiceLabels = transcription.getVoiceLabels();
		int highestNumberOfVoices = transcription.getNumberOfVoices();
		List<List<List<Integer>>> actual = 
			ErrorCalculator.getPositivesAndNegativesPerVoice(allPredictedVoices, 
			groundTruthVoiceLabels,	null, highestNumberOfVoices);

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
		assertEquals(expected, actual);

		// Added as extra: check precision/recall/F1Score for this piece
		ErrorFraction[][] precRecF1Expected = new ErrorFraction[5][3];
		// Voice 0
		ErrorFraction prec0 = new ErrorFraction(11, 13); 
		ErrorFraction rec0 = new ErrorFraction(11, 14);
		// f1-score: (242/182) / (297/182) = 22/27 (reduced) 
		precRecF1Expected[0] = new ErrorFraction[]{prec0, rec0,	new ErrorFraction(22, 27)};
		// Voice 1
		ErrorFraction prec1 = new ErrorFraction(3, 7);
		ErrorFraction rec1 = new ErrorFraction(3, 7);
		// f1-score: (18/49) / (42/49) = 3/7 (reduced)
		precRecF1Expected[1] = new ErrorFraction[]{prec1, rec1,	new ErrorFraction(3, 7)};
		// Voice 2
		ErrorFraction prec2 = new ErrorFraction(3, 6);
		ErrorFraction rec2 = new ErrorFraction(3, 8); 
		// f1-score: (3/8) / (7/8) = 3/7 (reduced)
		precRecF1Expected[2] = new ErrorFraction[]{prec2, rec2,	new ErrorFraction(3, 7)};
		// Voice 3  
		ErrorFraction prec3 = new ErrorFraction(5, 8); 
		ErrorFraction rec3 = new ErrorFraction(5, 7);
		// f1-score: (50/56) / (75/56) = 2/3 (reduced)
		precRecF1Expected[3] = new ErrorFraction[]{prec3, rec3,	new ErrorFraction(2, 3)};
		// Voice 4
		ErrorFraction prec4 = new ErrorFraction(3, 7);
		ErrorFraction rec4 = new ErrorFraction(3, 4);
		// f1-score: (18/28) / (33/28) = 6/11 (reduced)
		precRecF1Expected[4] = new ErrorFraction[]{prec4, rec4,	new ErrorFraction(6, 11)};

		ErrorFraction[][] precRecF1Actual = new ErrorFraction[5][3];
		// For each element of expected (representing a voice)
		for (int i = 0; i < expected.size(); i++) {
			int currTruePositives = expected.get(i).get(0).size();
			int currFalsePositives = expected.get(i).get(1).size();
			int currFalseNegatives = expected.get(i).get(2).size();
			precRecF1Actual[i] = 
				ErrorCalculator.calculatePrecisionRecallF1(currTruePositives,
				currFalsePositives,	currFalseNegatives);
		}

		assertEquals(precRecF1Expected.length, precRecF1Actual.length);
		for (int i = 0; i < precRecF1Expected.length; i++) {
			assertEquals(precRecF1Expected[i].length, precRecF1Actual[i].length);
			for (int j = 0; j < precRecF1Expected[i].length; j++) {
				assertEquals(precRecF1Expected[i][j], precRecF1Actual[i][j]);
			}
		}	  
	}


	public void testGetPositivesAndNegativesPerVoiceNonTab() {
		Transcription transcription0 = new Transcription(midiTestpiece1);
		Transcription transcription1 = new Transcription(midiTestpiece1);
		Transcription transcription2 = new Transcription(midiTestpiece1);
		Transcription transcription3 = new Transcription(midiTestpiece1);

		// Adapt transcription1-3 so that all unisons become EDUs
		List<Transcription> transcriptions = Arrays.asList(new Transcription[]{transcription1, transcription2, transcription3});
		for (Transcription t : transcriptions) {
			// 1. Adapt the appropriate notes in the NoteSequence
			Note n13 = t.getNotes().get(13);
//			Note n13 = t.getNoteSequence().getNoteAt(13);
			t.getNotes().set(13, ScorePiece.createNote(n13.getMidiPitch(), n13.getMetricTime(), 
				new Rational(1, 4), -1, t.getScorePiece().getMetricalTimeLine()));
//			t.getNoteSequence().replaceNoteAt(13, Transcription.createNote(n13.getMidiPitch(), n13.getMetricTime(), 
//				new Rational(1, 4)));
			Note n16 = t.getNotes().get(16);
//			Note n16 = t.getNoteSequence().getNoteAt(16);
			t.getNotes().set(16, ScorePiece.createNote(n16.getMidiPitch(), n16.getMetricTime(),
				new Rational(1, 4), -1, t.getScorePiece().getMetricalTimeLine()));
//			t.getNoteSequence().replaceNoteAt(16, Transcription.createNote(n16.getMidiPitch(), n16.getMetricTime(),
//				new Rational(1, 4)));
			// 2. For the notes at index 12 and 13: swap the voice labels so that the note with the lower index gets
			// the lower voice as must be the case with EDUnotes (it currently has the higher voice because in transcription0,
			// the note with the longer duration, which is in voice 0, is listed first) 
			List<List<Double>> voiceLabels = t.getVoiceLabels();
			Collections.swap(voiceLabels, 12, 13);      
			// 3. Set the equalDurationUnisonsInfo for notes 12-13 and 16-17
			t.getVoicesEDU().set(12, new Integer[]{1, 0, 13});
			t.getVoicesEDU().set(13, new Integer[]{1, 0, 12});
			t.getVoicesEDU().set(16, new Integer[]{3, 2, 17});
			t.getVoicesEDU().set(17, new Integer[]{3, 2, 16});
		}

		List<Integer> voice0 = new ArrayList<Integer>(Arrays.asList(new Integer[]{0}));
		List<Integer> voice1 = new ArrayList<Integer>(Arrays.asList(new Integer[]{1}));
		List<Integer> voice2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{2}));
		List<Integer> voice3 = new ArrayList<Integer>(Arrays.asList(new Integer[]{3}));
		List<Integer> voice4 = new ArrayList<Integer>(Arrays.asList(new Integer[]{4}));

		// Transcription 0: no EDUs
		List<List<List<Integer>>> allPredictedVoices = new ArrayList<List<List<Integer>>>();
		List<List<Integer>> allPredictedVoices0 = new ArrayList<List<Integer>>();
		// Chord 0 (should be 3, 2, 1, 0)
		allPredictedVoices0.add(voice3); // (0): TP-v3
		allPredictedVoices0.add(voice4); // (1): FP-v4 & FN-v2
		allPredictedVoices0.add(voice0); // (2): FP-v0 & FN-v1
		allPredictedVoices0.add(voice1); // (3): FP-v1 & FN-v0
		// Chord 1 (should be 3, 2, 1, 0)
		allPredictedVoices0.add(voice3); // (4): TP-v3
		allPredictedVoices0.add(voice1); // (5): FP-v1 & FN-v2
		allPredictedVoices0.add(voice0); // (6): FP-v0 & FN-v1
		allPredictedVoices0.add(voice4); // (7): FP-v4 & FN-v0
		// Chord 2 (should be 3)
		allPredictedVoices0.add(voice2); // (8): FP-v2 & FN-v3
		// Chord 3 (should be 4, 3, 2, 0, 1)
		allPredictedVoices0.add(voice4); // (9): TP-v4
		allPredictedVoices0.add(voice3); // (10): TP-v3
		allPredictedVoices0.add(voice1); // (11): FP-v1 & FN-v2
		allPredictedVoices0.add(voice0); // (12): TP-v0
		allPredictedVoices0.add(voice2); // (13): FP-v2 & FN-v1
		// Chord 4 (should be 4)
		allPredictedVoices0.add(voice4); // (14): TP-v4
		// Chord 5 (should be 4, 3, 2, 1, 0)
		allPredictedVoices0.add(voice3); // (15): FP-v3 & FN-v4
		allPredictedVoices0.add(voice0); // (16): FP-v0 & FN-v3
		allPredictedVoices0.add(voice2); // (17): TP-v2
		allPredictedVoices0.add(voice1); // (18): TP-v1
		allPredictedVoices0.add(voice4); // (19): FP-v4 & FN-v0
		// Chord 6 (should be 4, 2, 0, 1)
		allPredictedVoices0.add(voice4); // (20): TP-v4
		allPredictedVoices0.add(voice3); // (21): FP-v3 & FN-v2
		allPredictedVoices0.add(voice1); // (22): FP-v1 & FN-v0
		allPredictedVoices0.add(voice2); // (23): FP-v2 & FN-v1
		// Chord 7 (should be 2, 0)
		allPredictedVoices0.add(voice3); // (24): FP-v3 & FN-v2
		allPredictedVoices0.add(voice0); // (25): TP-v0
		// Chord 8 (should be 3, 2, 1, 0)
		allPredictedVoices0.add(voice3); // (26): TP-v3
		allPredictedVoices0.add(voice4); // (27): FP-v4 & FN-v2
		allPredictedVoices0.add(voice1); // (28): TP-v1
		allPredictedVoices0.add(voice0); // (29): TP-v0
		// Chord 9-14 (should all be 0)
		allPredictedVoices0.add(voice0); // (30): TP-v0
		allPredictedVoices0.add(voice0); // (31): TP-v0
		allPredictedVoices0.add(voice0); // (32): TP-v0
		allPredictedVoices0.add(voice0); // (33): TP-v0
		allPredictedVoices0.add(voice0); // (34): TP-v0
		allPredictedVoices0.add(voice0); // (35): TP-v0
		// Chord 15 (should be 3, 2, 1, 0)
		allPredictedVoices0.add(voice3); // (36): TP-v3
		allPredictedVoices0.add(voice2); // (37): TP-v2
		allPredictedVoices0.add(voice1); // (38): TP-v1
		allPredictedVoices0.add(voice0); // (39): TP-v0
		allPredictedVoices.add(allPredictedVoices0);

		// Transcription 1: only one EDUnote assigned correctly
		List<List<Integer>> allPredictedVoices1 = new ArrayList<List<Integer>>(allPredictedVoices0);
		// Chord 3
		// Lower EDU note (index 12) correctly assigned to voice 0; upper (index 13) misassigned to voice 2
		// --> 12 is TP in voice 0; 13 is FP in voice 2 and FN in voice 1
		allPredictedVoices1.set(9, voice4); allPredictedVoices1.set(10, voice3); allPredictedVoices1.set(11, voice1);
		allPredictedVoices1.set(12, voice0); allPredictedVoices1.set(13, voice2); // should be 4, 3, 2, 1(0), 0(1)
		// Chord 5
		// Lower EDU note (index 16) misassigned to voice 0; upper (index 17) correctly assigned to voice 2
		// --> 16 is FP in voice 0 and FN in voice 3; 17 is TP in voice 2
		allPredictedVoices1.set(15, voice3); allPredictedVoices1.set(16, voice0); allPredictedVoices1.set(17, voice2);
		allPredictedVoices1.set(18, voice1); allPredictedVoices1.set(19, voice4); // should be 4, 3(2), 2(3), 1, 0
		allPredictedVoices.add(allPredictedVoices1);

		// Transcription 2: both EDUnotes assigned correctly
		List<List<Integer>> allPredictedVoices2 = new ArrayList<List<Integer>>(allPredictedVoices1);
		// Chord 3
		// Lower EDU note (index 12) correctly assigned to voice 0; upper (index 13) correctly assigned to voice 1
		// --> 12 is TP in voice 0; 13 is TP in voice 1
		allPredictedVoices2.set(9, voice4); allPredictedVoices2.set(10, voice3); allPredictedVoices2.set(11, voice2);
		allPredictedVoices2.set(12, voice0); allPredictedVoices2.set(13, voice1); // should be 4, 3, 2, 1(0), 0(1)
		// Chord 5
		// Lower EDU note (index 16) correctly assigned to voice 3; upper (index 17) correctly assigned to voice 2
		// --> 16 is TP in voice 3; 17 is TP in voice 2
		allPredictedVoices2.set(15, voice0); allPredictedVoices2.set(16, voice3); allPredictedVoices2.set(17, voice2);
		allPredictedVoices2.set(18, voice1); allPredictedVoices2.set(19, voice4); // should be 4, 3(2), 2(3), 1, 0
		allPredictedVoices.add(allPredictedVoices2);

		// Transcription 3: both EDUnotes assigned incorrectly
		List<List<Integer>> allPredictedVoices3 = new ArrayList<List<Integer>>(allPredictedVoices1);
		// Chord 3
		// Lower EDU note (index 12) misassigned to voice 3; upper (index 13) misassigned to voice 2
		// --> 12 is FP in voice 3 and FN in voice 1; 13 is FP in voice 2 and FN in voice 0
		allPredictedVoices3.set(9, voice4); allPredictedVoices3.set(10, voice0); allPredictedVoices3.set(11, voice1);
		allPredictedVoices3.set(12, voice3); allPredictedVoices3.set(13, voice2); // should be 4, 3, 2, 1(0), 0(1)
		// Chord 5
		// Lower EDU note (index 16) misassigned to voice 0; upper (index 17) misassigned to voice 1
		// --> 16 is FP in voice 0 and FN in voice 3; 17 is FP in voice 1 and FN in voice 2
		allPredictedVoices3.set(15, voice2); allPredictedVoices3.set(16, voice0); allPredictedVoices3.set(17, voice1);
		allPredictedVoices3.set(18, voice3); allPredictedVoices3.set(19, voice4); // should be 4, 3(2), 2(3), 1, 0
		allPredictedVoices.add(allPredictedVoices3);

		List<List<List<Integer>>> expected = new ArrayList<List<List<Integer>>>();
		// Transcription 0
		List<List<List<Integer>>> expected0 = new ArrayList<List<List<Integer>>>();
		List<List<Integer>> expectedVoice0 = new ArrayList<List<Integer>>();
		List<Integer> tP0 = new ArrayList<Integer>(Arrays.asList(new Integer[]{12, 25, 29, 30, 31, 32, 33, 34, 35, 39}));
		List<Integer> fP0 = new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 6, 16}));
		List<Integer> fN0 = new ArrayList<Integer>(Arrays.asList(new Integer[]{3, 7, 19, 22}));
		expectedVoice0.add(tP0); expectedVoice0.add(fP0); expectedVoice0.add(fN0);
		List<List<Integer>> expectedVoice1 = new ArrayList<List<Integer>>();
		List<Integer> tP1 = new ArrayList<Integer>(Arrays.asList(new Integer[]{18, 28, 38}));
		List<Integer> fP1 = new ArrayList<Integer>(Arrays.asList(new Integer[]{3, 5, 11, 22}));
		List<Integer> fN1 = new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 6, 13, 23}));
		expectedVoice1.add(tP1); expectedVoice1.add(fP1); expectedVoice1.add(fN1);
		List<List<Integer>> expectedVoice2 = new ArrayList<List<Integer>>();
		List<Integer> tP2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{17, 37}));
		List<Integer> fP2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{8, 13, 23}));
		List<Integer> fN2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{1, 5, 11, 21, 24, 27}));
		expectedVoice2.add(tP2); expectedVoice2.add(fP2); expectedVoice2.add(fN2);
		List<List<Integer>> expectedVoice3 = new ArrayList<List<Integer>>();
		List<Integer> tP3 = new ArrayList<Integer>(Arrays.asList(new Integer[]{0, 4, 10, 26, 36}));
		List<Integer> fP3 = new ArrayList<Integer>(Arrays.asList(new Integer[]{15, 21, 24}));
		List<Integer> fN3 = new ArrayList<Integer>(Arrays.asList(new Integer[]{8, 16}));
		expectedVoice3.add(tP3); expectedVoice3.add(fP3); expectedVoice3.add(fN3);
		List<List<Integer>> expectedVoice4 = new ArrayList<List<Integer>>();
		List<Integer> tP4 = new ArrayList<Integer>(Arrays.asList(new Integer[]{9, 14, 20}));
		List<Integer> fP4 = new ArrayList<Integer>(Arrays.asList(new Integer[]{1, 7, 19, 27}));
		List<Integer> fN4 = new ArrayList<Integer>(Arrays.asList(new Integer[]{15}));
		expectedVoice4.add(tP4); expectedVoice4.add(fP4); expectedVoice4.add(fN4);
		expected0.add(expectedVoice0); expected0.add(expectedVoice1); expected0.add(expectedVoice2); 
		expected0.add(expectedVoice3); expected0.add(expectedVoice4);
		expected.addAll(expected0);

		// Transcription 1
		List<List<List<Integer>>> expected1 = new ArrayList<List<List<Integer>>>();
		expectedVoice0 = new ArrayList<List<Integer>>();
		tP0 = new ArrayList<Integer>(Arrays.asList(new Integer[]{12, 25, 29, 30, 31, 32, 33, 34, 35, 39}));
		fP0 = new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 6, 16}));
		fN0 = new ArrayList<Integer>(Arrays.asList(new Integer[]{3, 7, 19, 22}));
		expectedVoice0.add(tP0); expectedVoice0.add(fP0); expectedVoice0.add(fN0);
		expectedVoice1 = new ArrayList<List<Integer>>();
		tP1 = new ArrayList<Integer>(Arrays.asList(new Integer[]{18, 28, 38}));
		fP1 = new ArrayList<Integer>(Arrays.asList(new Integer[]{3, 5, 11, 22}));
		fN1 = new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 6, 13, 23}));
		expectedVoice1.add(tP1); expectedVoice1.add(fP1); expectedVoice1.add(fN1);
		expectedVoice2 = new ArrayList<List<Integer>>();
		tP2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{17, 37}));
		fP2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{8, 13, 23}));
		fN2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{1, 5, 11, 21, 24, 27}));
		expectedVoice2.add(tP2); expectedVoice2.add(fP2); expectedVoice2.add(fN2);
		expectedVoice3 = new ArrayList<List<Integer>>();
		tP3 = new ArrayList<Integer>(Arrays.asList(new Integer[]{0, 4, 10, 26, 36}));
		fP3 = new ArrayList<Integer>(Arrays.asList(new Integer[]{15, 21, 24}));
		fN3 = new ArrayList<Integer>(Arrays.asList(new Integer[]{8, 16}));
		expectedVoice3.add(tP3); expectedVoice3.add(fP3); expectedVoice3.add(fN3);
		expectedVoice4 = new ArrayList<List<Integer>>();
		tP4 = new ArrayList<Integer>(Arrays.asList(new Integer[]{9, 14, 20}));
		fP4 = new ArrayList<Integer>(Arrays.asList(new Integer[]{1, 7, 19, 27}));
		fN4 = new ArrayList<Integer>(Arrays.asList(new Integer[]{15}));
		expectedVoice4.add(tP4); expectedVoice4.add(fP4); expectedVoice4.add(fN4);
		expected1.add(expectedVoice0); expected1.add(expectedVoice1); expected1.add(expectedVoice2); 
		expected1.add(expectedVoice3); expected1.add(expectedVoice4);
		expected.addAll(expected1);

		// Transcription 2
		List<List<List<Integer>>> expected2 = new ArrayList<List<List<Integer>>>();
		expectedVoice0 = new ArrayList<List<Integer>>();
		tP0 = new ArrayList<Integer>(Arrays.asList(new Integer[]{12, 25, 29, 30, 31, 32, 33, 34, 35, 39}));
		fP0 = new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 6, 15}));
		fN0 = new ArrayList<Integer>(Arrays.asList(new Integer[]{3, 7, 19, 22}));
		expectedVoice0.add(tP0); expectedVoice0.add(fP0); expectedVoice0.add(fN0);
		expectedVoice1 = new ArrayList<List<Integer>>();
		tP1 = new ArrayList<Integer>(Arrays.asList(new Integer[]{13, 18, 28, 38}));
		fP1 = new ArrayList<Integer>(Arrays.asList(new Integer[]{3, 5, 22}));
		fN1 = new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 6, 23}));
		expectedVoice1.add(tP1); expectedVoice1.add(fP1); expectedVoice1.add(fN1);
		expectedVoice2 = new ArrayList<List<Integer>>();
		tP2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{11, 17, 37}));
		fP2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{8, 23}));
		fN2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{1, 5, 21, 24, 27}));
		expectedVoice2.add(tP2); expectedVoice2.add(fP2); expectedVoice2.add(fN2);
		expectedVoice3 = new ArrayList<List<Integer>>();
		tP3 = new ArrayList<Integer>(Arrays.asList(new Integer[]{0, 4, 10, 16, 26, 36}));
		fP3 = new ArrayList<Integer>(Arrays.asList(new Integer[]{21, 24}));
		fN3 = new ArrayList<Integer>(Arrays.asList(new Integer[]{8}));
		expectedVoice3.add(tP3); expectedVoice3.add(fP3); expectedVoice3.add(fN3);
		expectedVoice4 = new ArrayList<List<Integer>>();
		tP4 = new ArrayList<Integer>(Arrays.asList(new Integer[]{9, 14, 20}));
		fP4 = new ArrayList<Integer>(Arrays.asList(new Integer[]{1, 7, 19, 27}));
		fN4 = new ArrayList<Integer>(Arrays.asList(new Integer[]{15}));
		expectedVoice4.add(tP4); expectedVoice4.add(fP4); expectedVoice4.add(fN4);	
		expected2.add(expectedVoice0); expected2.add(expectedVoice1); expected2.add(expectedVoice2); 
		expected2.add(expectedVoice3); expected2.add(expectedVoice4);
		expected.addAll(expected2);

		// Transcription 3
		List<List<List<Integer>>> expected3 = new ArrayList<List<List<Integer>>>();
		expectedVoice0 = new ArrayList<List<Integer>>();
		tP0 = new ArrayList<Integer>(Arrays.asList(new Integer[]{25, 29, 30, 31, 32, 33, 34, 35, 39}));
		fP0 = new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 6, 10, 16}));
		fN0 = new ArrayList<Integer>(Arrays.asList(new Integer[]{3, 7, 13, 19, 22}));
		expectedVoice0.add(tP0); expectedVoice0.add(fP0); expectedVoice0.add(fN0);
		expectedVoice1 = new ArrayList<List<Integer>>();
		tP1 = new ArrayList<Integer>(Arrays.asList(new Integer[]{28, 38}));
		fP1 = new ArrayList<Integer>(Arrays.asList(new Integer[]{3, 5, 11, 17, 22}));
		fN1 = new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 6, 12, 18, 23}));
		expectedVoice1.add(tP1); expectedVoice1.add(fP1); expectedVoice1.add(fN1);
		expectedVoice2 = new ArrayList<List<Integer>>();
		tP2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{37}));
		fP2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{8, 13, 15, 23}));
		fN2 = new ArrayList<Integer>(Arrays.asList(new Integer[]{1, 5, 11, 17, 21, 24, 27}));
		expectedVoice2.add(tP2); expectedVoice2.add(fP2); expectedVoice2.add(fN2);
		expectedVoice3 = new ArrayList<List<Integer>>();
		tP3 = new ArrayList<Integer>(Arrays.asList(new Integer[]{0, 4, 26, 36}));
		fP3 = new ArrayList<Integer>(Arrays.asList(new Integer[]{12, 18, 21, 24}));
		fN3 = new ArrayList<Integer>(Arrays.asList(new Integer[]{8, 10, 16}));
		expectedVoice3.add(tP3); expectedVoice3.add(fP3); expectedVoice3.add(fN3);
		expectedVoice4 = new ArrayList<List<Integer>>();
		tP4 = new ArrayList<Integer>(Arrays.asList(new Integer[]{9, 14, 20}));
		fP4 = new ArrayList<Integer>(Arrays.asList(new Integer[]{1, 7, 19, 27}));
		fN4 = new ArrayList<Integer>(Arrays.asList(new Integer[]{15}));
		expectedVoice4.add(tP4); expectedVoice4.add(fP4); expectedVoice4.add(fN4);	
		expected3.add(expectedVoice0); expected3.add(expectedVoice1); expected3.add(expectedVoice2); 
		expected3.add(expectedVoice3); expected3.add(expectedVoice4);
		expected.addAll(expected3);

		List<List<List<Integer>>> actual = new ArrayList<List<List<Integer>>>();
		transcriptions = Arrays.asList(new Transcription[]{transcription0, transcription1, transcription2, transcription3});
		for (int i = 0; i < transcriptions.size(); i++) {
			Transcription t = transcriptions.get(i);
			actual.addAll(ErrorCalculator.getPositivesAndNegativesPerVoice(allPredictedVoices.get(i),
				t.getVoiceLabels(), t.getVoicesEDU(), t.getNumberOfVoices()));
		}

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
		assertEquals(expected, actual);

		// Added as extra: check precision/recall/F1Score for version 1
		// Determine expected
		ErrorFraction[][] precRecF1Expected = new ErrorFraction[5][3];
		// Voice 0
		ErrorFraction prec0 = new ErrorFraction(10, 13); 
		ErrorFraction rec0 = new ErrorFraction(10, 14);
		// f1-score: (100/91) / (135/91) = 20/27 (reduced)
		precRecF1Expected[0] = new ErrorFraction[]{prec0, rec0,	new ErrorFraction(20, 27)};
		// Voice 1
		ErrorFraction prec1 = new ErrorFraction(3, 7);
		ErrorFraction rec1 = new ErrorFraction(3, 7);
		// f1-score: (18/49) / (42/49) = 3/7 (reduced)
		precRecF1Expected[1] = new ErrorFraction[]{prec1, rec1,	new ErrorFraction(3, 7)};
		// Voice 2
		ErrorFraction prec2 = new ErrorFraction(2, 5);
		ErrorFraction rec2 = new ErrorFraction(2, 8); 
		// f1-score: (4/20) / (13/20) = 4/13 (reduced)
		precRecF1Expected[2] = new ErrorFraction[]{prec2, rec2,	new ErrorFraction(4, 13)};
		// Voice 3  
		ErrorFraction prec3 = new ErrorFraction(5, 8); 
		ErrorFraction rec3 = new ErrorFraction(5, 7);
		// f1-score: (50/56) / (75/56) = 2/3 (reduced)
		precRecF1Expected[3] = new ErrorFraction[]{prec3, rec3,	new ErrorFraction(2, 3)};
		// Voice 4
		ErrorFraction prec4 = new ErrorFraction(3, 7);
		ErrorFraction rec4 = new ErrorFraction(3, 4);
		// f1-score: (18/28) / (33/28) = 6/11 (reduced)
		precRecF1Expected[4] = new ErrorFraction[]{prec4, rec4,	new ErrorFraction(6, 11)};

		ErrorFraction[][] precRecF1Actual = new ErrorFraction[5][3];
		// For each element of expected1 (representing a voice)
		for (int i = 0; i < expected1.size(); i++) {
			int currTruePositives = expected.get(i).get(0).size();
			int currFalsePositives = expected.get(i).get(1).size();
			int currFalseNegatives = expected.get(i).get(2).size();
			precRecF1Actual[i] = 
				ErrorCalculator.calculatePrecisionRecallF1(currTruePositives,
				currFalsePositives,	currFalseNegatives);
		}

		assertEquals(precRecF1Expected.length, precRecF1Actual.length);
		for (int i = 0; i < precRecF1Expected.length; i++) {
			assertEquals(precRecF1Expected[i].length, precRecF1Actual[i].length);
			for (int j = 0; j < precRecF1Expected[i].length; j++) {
				assertEquals(precRecF1Expected[i][j], precRecF1Actual[i][j]);
			}
		}
	}


	public void testCalculatePrecisionRecallF1() {
		Integer[] truePos = new Integer[]{45, 92};
		Integer[] falsePos = new Integer[]{15, 253};
		Integer[] falseNeg = new Integer[]{35, 94};

		List<ErrorFraction[]> expected = new ArrayList<ErrorFraction[]>();
		ErrorFraction[] expected0 = new ErrorFraction[3];
		expected0[0] = new ErrorFraction(45, 60); // prc
		expected0[1] = new ErrorFraction(45, 80); // rcl
		expected0[2] = new ErrorFraction(9, 14); // F1
		ErrorFraction[] expected1 = new ErrorFraction[3];
		expected1[0] = new ErrorFraction(92, 345); // prc
		expected1[1] = new ErrorFraction(92, 186); // rcl
		expected1[2] = new ErrorFraction(184, 531); // F1
		expected.add(expected0); expected.add(expected1); 

		List<ErrorFraction[]> actual = new ArrayList<ErrorFraction[]>();
		actual.add(ErrorCalculator.calculatePrecisionRecallF1(truePos[0], falsePos[0], falseNeg[0]));
		actual.add(ErrorCalculator.calculatePrecisionRecallF1(truePos[1], falsePos[1], falseNeg[1]));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j]);		
			}
		}
	}


	public void testCalculateAvgSoundnessAndCompleteness() { 		
		List<ErrorFraction[][]> expected = new ArrayList<ErrorFraction[][]>();
		// Toy example 1
		ErrorFraction[][] expected1 = new ErrorFraction[2][2];
		// a. Wtd avg
		expected1[0] = new ErrorFraction[]{
			new ErrorFraction(6+5+0+5, 7+6+1+6), 
			new ErrorFraction(5+6+0+4, 7+8+0+6)};
		// b. Avg
		// snd = (6/7 + 5/6 + 0/1 + 5/6) / 4 = 53/21 * 1/4 = 53/84 (reduced)
		// cmp = (5/7 + 6/8 + 0/0 + 4/6) / 4 = 179/84 * 1/4 = 179/336 (reduced)
		expected1[1] = new ErrorFraction[]{
			new ErrorFraction(53, 84), 
			new ErrorFraction(179, 336)};

		// Toy example 2
		ErrorFraction[][] expected2 = new ErrorFraction[2][2];
		// a. Wtd avg
		expected2[0] = new ErrorFraction[]{
			new ErrorFraction(2+4+1+2, 6+7+1+6), 
			new ErrorFraction(3+4+0+4, 7+8+0+6)};
		// b. Avg
		// snd = (2/6 + 4/7 + 1/1 + 2/6) / 4 = 47/21 * 1/4 = 47/84 (reduced)
		// cmp = (3/7 + 4/8 + 0/0 + 4/6) / 4 = 67/42 * 1/4 = 67/168 (reduced)
		expected2[1] = new ErrorFraction[]{
			new ErrorFraction(47, 84), 
			new ErrorFraction(67, 168)};

		// Toy example 3
		ErrorFraction[][] expected3 = new ErrorFraction[2][2];
		// a. Wtd avg
		expected3[0] = new ErrorFraction[]{
			new ErrorFraction(7+7+0+6, 7+7+0+6), 
			new ErrorFraction(7+6+0+6, 7+8+0+6)};
		// b. Avg
		// snd = (7/7 + 7/7 + 0/0 + 6/6) / 4 = 3/1 * 1/4 = 3/4 (reduced)
		// cmp = (7/7 + 6/8 + 0/0 + 6/6) / 4 = 11/4 * 1/4 = 11/16 (reduced)
		expected3[1] = new ErrorFraction[]{
			new ErrorFraction(3, 4), 
			new ErrorFraction(11, 16)};

		expected.add(expected1);
 		expected.add(expected2);  
 		expected.add(expected3);

		List<ErrorFraction[][]> actual = new ArrayList<ErrorFraction[][]>();
		actual.add(ErrorCalculator.calculateAvgSoundnessAndCompleteness(getPredictedVoicesToyExamples().get(0), 
			getActualVoicesToyExamples(), null, 4));
		actual.add(ErrorCalculator.calculateAvgSoundnessAndCompleteness(getPredictedVoicesToyExamples().get(1), 
			getActualVoicesToyExamples(), null, 4));
		actual.add(ErrorCalculator.calculateAvgSoundnessAndCompleteness(getPredictedVoicesToyExamples().get(2), 
			getActualVoicesToyExamples(), null, 4));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j].length, actual.get(i)[j].length);
				for (int k = 0; k < expected.get(i)[j].length; k++) {
					assertEquals(expected.get(i)[j][k], actual.get(i)[j][k]);
				}
			}
		}
	}


	public void testCalculateAvgSoundnessAndCompletenessNonTab() { 		
		// Determine expected
		List<ErrorFraction[][]> expected = new ArrayList<ErrorFraction[][]>();
		// Toy example 1
		ErrorFraction[][] expected1 = new ErrorFraction[2][2];
		// a. Wtd avg
		expected1[0] = new ErrorFraction[]{
			new ErrorFraction(6+1+4, 6+6+5), 
			new ErrorFraction(5+3+3, 7+4+6)};
		// b. Avg
		// snd = (6/6 + 1/6 + 4/5) / 3 = 59/30 * 1/3 = 59/90 (reduced)
		// cmp = (5/7 + 3/4 + 3/6) / 3 = 55/28 * 1/3 = 55/84 (reduced)
		expected1[1] = new ErrorFraction[]{
			new ErrorFraction(59, 90), 
			new ErrorFraction(55, 84)};

		// Toy example 2
		ErrorFraction[][] expected2 = new ErrorFraction[2][2];
		// a. Wtd avg
		expected2[0] = new ErrorFraction[]{
			new ErrorFraction(1+2+3, 5+7+5), 
			new ErrorFraction(1+1+2, 7+4+6)};
		// b. Avg
		// snd = (1/5 + 2/7 + 3/5) / 3 = 38/35 * 1/3 = 38/105 (reduced)
		// cmp = (1/7 + 1/4 + 2/6) / 3 = 61/84 * 1/3 = 61/252 (reduced)
		expected2[1] = new ErrorFraction[]{
			new ErrorFraction(38, 105), 
			new ErrorFraction(61, 252)};

		// Toy example 3
		ErrorFraction[][] expected3 = new ErrorFraction[2][2];
		// a. Wtd avg
		expected3[0] = new ErrorFraction[]{
			new ErrorFraction(2+1+3, 5+7+5), 
			new ErrorFraction(2+0+2, 7+4+6)};
		// b. Avg
		// snd = (2/5 + 1/7 + 3/5) / 3 = 8/7 * 1/3 = 8/21 (reduced)
		// cmp = (2/7 + 0/4 + 2/6) / 3 = 31/21 * 1/3 = 13/63 (reduced);
		expected3[1] = new ErrorFraction[]{
			new ErrorFraction(8, 21), 
			new ErrorFraction(13, 63)};

		expected.add(expected1); 
		expected.add(expected2); 
		expected.add(expected3); 

		List<ErrorFraction[][]> actual = new ArrayList<ErrorFraction[][]>();
		actual.add(ErrorCalculator.calculateAvgSoundnessAndCompleteness(getPredictedVoicesToyExamplesNonTab().get(0), 
			getActualVoicesToyExamplesNonTab(), getEDUInfoToyExamples().get(0), 3));
		actual.add(ErrorCalculator.calculateAvgSoundnessAndCompleteness(getPredictedVoicesToyExamplesNonTab().get(1), 
			getActualVoicesToyExamplesNonTab(), getEDUInfoToyExamples().get(1), 3));
		actual.add(ErrorCalculator.calculateAvgSoundnessAndCompleteness(getPredictedVoicesToyExamplesNonTab().get(2), 
		 	getActualVoicesToyExamplesNonTab(), getEDUInfoToyExamples().get(2), 3));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j].length, actual.get(i)[j].length);
				for (int k = 0; k < expected.get(i)[j].length; k++) {
					assertEquals(expected.get(i)[j][k], actual.get(i)[j][k]);
				}
			}
		}
	}


	public void testCalculateAVC() {
		List<Double> expected = new ArrayList<Double>();
		// Toy example 1
		// For each voice: vc = (100/number of notes predicted for current voice) * max
		// voice 0, u:max = 0:7; 1:0; 2:0; voice 1, u:max = 0:1; 1:4; 2:2; voice 2, u:max = 0:0; 1:1; 2:5    
		int notesPred1Voice0 = 7; int max1Voice0 = 7; 
//		Rational vc1Voice0 = new Rational(max1Voice0*1, notesPred1Voice0);
		double vc1Voice0 = (max1Voice0*100.0) / notesPred1Voice0;
		int notesPred1Voice1 = 7; int max1Voice1 = 4; 
//		Rational vc1Voice1 = new Rational(max1Voice1*1, notesPred1Voice1);
		double vc1Voice1 = (max1Voice1*100.0) / notesPred1Voice1;
		int notesPred1Voice2 = 6; int max1Voice2 = 5; 
//		Rational vc1Voice2 = new Rational(max1Voice2*1, notesPred1Voice2);
		double vc1Voice2 = (max1Voice2*100.0) / notesPred1Voice2;
		// AVC = (1/number of voices) * sum of all vc
//		Rational sum1 = vc1Voice0.add(vc1Voice1).add(vc1Voice2);
//		Rational avc1 = new Rational(1, 3).mul(sum1); 
		double avc1 = (1.0/3) * (vc1Voice0 + vc1Voice1 + vc1Voice2);
		expected.add(avc1);

		// Toy example 2
		// For each voice: vc = (100/number of notes predicted for current voice) * max 
		// voice 0, u:max = 0:3; 1:3; 2:0; voice 1, u:max = 0:5; 1:1; 2:2; voice 2, u:max = 0:0; 1:1; 2:5
		int notesPred2Voice0 = 6; int max2Voice0 = 3; 
//		Rational vc2Voice0 = new Rational(max2Voice0*1, notesPred2Voice0);
		double vc2Voice0 = (max2Voice0*100.0) / notesPred2Voice0;
		int notesPred2Voice1 = 8; int max2Voice1 = 5; 
//		Rational vc2Voice1 = new Rational(max2Voice1*1, notesPred2Voice1);
		double vc2Voice1 = (max2Voice1*100.0) / notesPred2Voice1;
		int notesPred2Voice2 = 6; int max2Voice2 = 5; 
//		Rational vc2Voice2 = new Rational(max2Voice2*1, notesPred2Voice2);
		double vc2Voice2 = (max2Voice2*100.0) / notesPred2Voice2;
		// AVC = (1/number of voices) * sum of all vc
//		Rational sum2 = (vc2Voice0).add(vc2Voice1).add(vc2Voice2);
//		Rational avc2 = new Rational(1, 3).mul(sum2);
		double avc2 = (1.0/3) * (vc2Voice0 + vc2Voice1 + vc2Voice2);
		expected.add(avc2);

		// Toy example 3
		// For each voice: vc = (100/number of notes predicted for current voice) * max
		// voice 0, u:max = 0:4; 1:2; 2:0; voice 1, u:max = 0:4; 1:2; 2:2; voice 2, u:max = 0:0; 1:1; 2:5
		int notesPred3Voice0 = 6; int max3Voice0 = 4; 
//		Rational vc3Voice0 = new Rational(max3Voice0*1, notesPred3Voice0);
		double vc3Voice0 = (max3Voice0*100.0) / notesPred3Voice0;
		int notesPred3Voice1 = 8; int max3Voice1 = 4; 
//		Rational vc3Voice1 = new Rational(max3Voice1*1, notesPred3Voice1);
		double vc3Voice1 = (max3Voice1*100.0) / notesPred3Voice1;
		int notesPred3Voice2 = 6; int max3Voice2 = 5; 
//		Rational vc3Voice2 = new Rational(max3Voice2*1, notesPred3Voice2);
		double vc3Voice2 = (max3Voice2*100.0) / notesPred3Voice2;
		// AVC = (1/number of voices) * sum of all vc
//		Rational sum3 = vc3Voice0.add(vc3Voice1).add(vc3Voice2);
//		Rational avc3 = new Rational(1, 3).mul(sum3);
		double avc3 = (1.0/3) * (vc3Voice0 + vc3Voice1 + vc3Voice2);
		expected.add(avc3);

		List<Double> actual = new ArrayList<Double>();
		actual.add(ErrorCalculator.calculateAVC(getPredictedVoicesToyExamplesNonTab().get(0), 
			getActualVoicesToyExamplesNonTab(), getEDUInfoToyExamples().get(0), 3));
		actual.add(ErrorCalculator.calculateAVC(getPredictedVoicesToyExamplesNonTab().get(1), 
			getActualVoicesToyExamplesNonTab(), getEDUInfoToyExamples().get(1), 3));
		actual.add(ErrorCalculator.calculateAVC(getPredictedVoicesToyExamplesNonTab().get(2), 
			getActualVoicesToyExamplesNonTab(), getEDUInfoToyExamples().get(2), 3));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}


	public void testCalculateCrossEntropy() {	
		// Outputs without and with CoDs
		List<double[]> outputs = new ArrayList<double[]>();
//		outputs.add(new double[]{0.3, 0.2, 8.0, 0.1}); // ln(8)/ln(2) = 3
//		outputs.add(new double[]{0.1, 0.2, 4.0, 0.4}); // ln(4)/ln(2) = 2
//		outputs.add(new double[]{0.4, 0.1, 2.0, 0.1}); // ln(2)/ln(2) = 1
		//
//		outputs.add(new double[]{0.3, 8.0, 8.0, 0.1}); // ln(4)/ln(2) + ln(4)/ln(2) = 4
//		outputs.add(new double[]{0.1, 4.0, 4.0, 0.4}); // ln(2)/ln(2) + ln(2)/ln(2) = 2
//		outputs.add(new double[]{0.4, 2.0, 2.0, 0.1}); // ln(1)/ln(2) + ln(1)/ln(2) = 0
	
		outputs.add(new double[]{0.3, 0.2, 1.0, 0.1}); // log_e(1)
		outputs.add(new double[]{0.1, 0.2, 2.0, 0.4}); // log_e(2)
		outputs.add(new double[]{0.4, 0.1, 4.0, 0.1}); // log_e(4)
		//
		outputs.add(new double[]{0.3, 2.0, 2.0, 0.1}); // log_e(1) + log_e(1) = log_e(1*1)
		outputs.add(new double[]{0.1, 4.0, 4.0, 0.4}); // log_e(2) + log_e(2) = log_e(2*2)
		outputs.add(new double[]{0.4, 8.0, 8.0, 0.1}); // log_e(4) + log_e(4) = log_e(4*4)
		
		// Labels without and with CoDs
		List<List<Double>> labels = new ArrayList<List<Double>>();
		labels.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 0.0}));
		labels.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 0.0}));
		labels.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 0.0}));
		//
		labels.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 0.0}));
		labels.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 0.0}));
		labels.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 0.0}));
		
		// log_e(a) + log_e(b) = log_e(a*b) --> sum = log_e(1*2*4*1*4*16) = log_e(512)
		// j = 6; sum = log_e(512) --> H = (-1/6) * 9 = -1.5
		double[] expected = new double[]{-1.5, Math.log(512), Math.log(2), 6.0};
//		double[] expected = new double[]{-2.0, 12.0, 6.0, 4096.0};
		
		double[] actual = ErrorCalculator.calculateCrossEntropy(outputs, labels);
				
		assertEquals(expected.length, actual.length);
		for (int i = 0; i < actual.length; i++) {
			assertEquals(expected[i], actual[i]);
		}		
	}


	public void testAssertCorrectness() {
		List<List<Integer>> allPredictedVoices = new ArrayList<List<Integer>>();
		List<List<Integer>> actualVoices = new ArrayList<List<Integer>>();

		// 1. predictedVoices contains one element
		// a. actualVoices contains one element
		allPredictedVoices.add(Arrays.asList(new Integer[]{1})); // correct (T)
		actualVoices.add(Arrays.asList(new Integer[]{1}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1})); // incorrect (F)
		actualVoices.add(Arrays.asList(new Integer[]{2}));
		// b. actualVoices contains two elements
		allPredictedVoices.add(Arrays.asList(new Integer[]{1})); // overlooked (F)
		actualVoices.add(Arrays.asList(new Integer[]{0, 1}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1})); // incorrect (F)
		actualVoices.add(Arrays.asList(new Integer[]{0, 2}));
		// 2. predictedVoices contains two elements
		// a. actualVoices contains one element
		allPredictedVoices.add(Arrays.asList(new Integer[]{1, 2})); // superfluous (F)
		actualVoices.add(Arrays.asList(new Integer[]{1}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1, 2})); // incorrect (F)
		actualVoices.add(Arrays.asList(new Integer[]{0}));
		// b. actualVoices contains two elements
		allPredictedVoices.add(Arrays.asList(new Integer[]{1, 2})); // correct (T)
		actualVoices.add(Arrays.asList(new Integer[]{1, 2}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{2, 1})); // correct (T)
		actualVoices.add(Arrays.asList(new Integer[]{1, 2}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1, 2})); // half (F)
		actualVoices.add(Arrays.asList(new Integer[]{0, 2}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1, 2})); // incorrect (F)
		actualVoices.add(Arrays.asList(new Integer[]{0, 3}));

		List<Boolean> expected = Arrays.asList(new Boolean[]{
			true, false, false, false,
			false, false, true, true, false, false
		});

		List<Boolean> actual = new ArrayList<Boolean>();
		for (int i = 0; i < allPredictedVoices.size(); i++) {
			actual.add(ErrorCalculator.assertCorrectness(allPredictedVoices.get(i), 
				actualVoices.get(i)));
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
		assertEquals(expected, actual);
	}


	public void testAssertCorrectnessEDUNotes() {
		List<Integer> allowedVoices = Arrays.asList(new Integer[]{1, 2});
		List<List<Integer[]>> predAndAdVoices = new ArrayList<List<Integer[]>>();
		// 1. Both voices predicted correctly
		// a. Both adapted correctly
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{1, 2}, new Integer[]{1, 2}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{1, 2}, new Integer[]{2, 1}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{2, 1}, new Integer[]{1, 2}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{2, 1}, new Integer[]{2, 1}}));
		// b. Only lower voice adapted correctly
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{1, 2}, new Integer[]{1, 3}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{1, 2}, new Integer[]{2, 3}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{2, 1}, new Integer[]{1, 3}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{2, 1}, new Integer[]{2, 3}}));
		// c. Only upper voice adapted correctly
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{1, 2}, new Integer[]{3, 1}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{1, 2}, new Integer[]{3, 2}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{2, 1}, new Integer[]{3, 1}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{2, 1}, new Integer[]{3, 2}}));
		// d. Both adapted incorrectly
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{1, 2}, new Integer[]{3, 4}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{1, 2}, new Integer[]{4, 3}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{2, 1}, new Integer[]{3, 4}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{2, 1}, new Integer[]{4, 3}}));
		// 2. Only lower voice predicted correctly
		// a. Both adapted correctly
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{1, 3}, new Integer[]{1, 2}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{1, 3}, new Integer[]{2, 1}}));
		// b. Only lower voice adapted correctly
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{1, 3}, new Integer[]{1, 3}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{1, 3}, new Integer[]{2, 3}}));
		// c. Only upper voice adapted correctly
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{1, 3}, new Integer[]{3, 1}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{1, 3}, new Integer[]{3, 2}}));
		// d. Both adapted incorrectly
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{1, 3}, new Integer[]{3, 4}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{1, 3}, new Integer[]{4, 3}}));
		// 3. Only upper voice predicted correctly
		// a. Both adapted correctly
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{3, 1}, new Integer[]{1, 2}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{3, 1}, new Integer[]{2, 1}}));
		// b. Only lower voice adapted correctly
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{3, 1}, new Integer[]{1, 3}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{3, 1}, new Integer[]{2, 3}}));
		// c. Only upper voice adapted correctly
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{3, 1}, new Integer[]{3, 1}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{3, 1}, new Integer[]{3, 2}}));
		// d. Both adapted incorrectly
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{3, 1}, new Integer[]{3, 4}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{3, 1}, new Integer[]{4, 3}}));
		// 4. Both voice predicted incorrectly
		// a. Both adapted correctly
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{3, 4}, new Integer[]{1, 2}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{3, 4}, new Integer[]{2, 1}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{4, 3}, new Integer[]{1, 2}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{4, 3}, new Integer[]{2, 1}}));
		// b. Only lower voice adapted correctly
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{3, 4}, new Integer[]{1, 4}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{3, 4}, new Integer[]{2, 4}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{4, 3}, new Integer[]{1, 4}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{4, 3}, new Integer[]{2, 4}}));
		// c. Only upper voice adapted correctly
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{3, 4}, new Integer[]{4, 1}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{3, 4}, new Integer[]{4, 2}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{4, 3}, new Integer[]{4, 1}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{4, 3}, new Integer[]{4, 2}}));
		// d. Both adapted incorrectly
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{3, 4}, new Integer[]{3, 4}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{3, 4}, new Integer[]{4, 3}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{4, 3}, new Integer[]{3, 4}}));
		predAndAdVoices.add(Arrays.asList(new Integer[][]{new Integer[]{4, 3}, new Integer[]{4, 3}}));

		List<boolean[][]> expected = new ArrayList<boolean[][]>();
		// 1.
		expected.add(new boolean[][]{{true, true}, {true, true}}); expected.add(new boolean[][]{{true, true}, {true, true}});
		expected.add(new boolean[][]{{true, true}, {true, true}}); expected.add(new boolean[][]{{true, true}, {true, true}});
		//
		expected.add(new boolean[][]{{true, true}, {true, false}});	expected.add(new boolean[][]{{true, true}, {true, false}});
		expected.add(new boolean[][]{{true, true}, {true, false}});	expected.add(new boolean[][]{{true, true}, {true, false}});
		// 
		expected.add(new boolean[][]{{true, true}, {false, true}}); expected.add(new boolean[][]{{true, true}, {false, true}});
		expected.add(new boolean[][]{{true, true}, {false, true}}); expected.add(new boolean[][]{{true, true}, {false, true}});
		//
		expected.add(new boolean[][]{{true, true}, {false, false}}); expected.add(new boolean[][]{{true, true}, {false, false}});
		expected.add(new boolean[][]{{true, true}, {false, false}}); expected.add(new boolean[][]{{true, true}, {false, false}});
		// 2.
		expected.add(new boolean[][]{{true, false}, {true, true}}); expected.add(new boolean[][]{{true, false}, {true, true}});
		//
		expected.add(new boolean[][]{{true, false}, {true, false}});	expected.add(new boolean[][]{{true, false}, {true, false}});
		// 
		expected.add(new boolean[][]{{true, false}, {false, true}}); expected.add(new boolean[][]{{true, false}, {false, true}});
		//
		expected.add(new boolean[][]{{true, false}, {false, false}}); expected.add(new boolean[][]{{true, false}, {false, false}});
		// 3.
		expected.add(new boolean[][]{{false, true}, {true, true}}); expected.add(new boolean[][]{{false, true}, {true, true}});
		//
		expected.add(new boolean[][]{{false, true}, {true, false}});	expected.add(new boolean[][]{{false, true}, {true, false}});
		// 
		expected.add(new boolean[][]{{false, true}, {false, true}}); expected.add(new boolean[][]{{false, true}, {false, true}});
		//
		expected.add(new boolean[][]{{false, true}, {false, false}}); expected.add(new boolean[][]{{false, true}, {false, false}});
		// 4. 
		expected.add(new boolean[][]{{false, false}, {true, true}}); expected.add(new boolean[][]{{false, false}, {true, true}});
		expected.add(new boolean[][]{{false, false}, {true, true}}); expected.add(new boolean[][]{{false, false}, {true, true}});
		//
		expected.add(new boolean[][]{{false, false}, {true, false}});	expected.add(new boolean[][]{{false, false}, {true, false}});
		expected.add(new boolean[][]{{false, false}, {true, false}});	expected.add(new boolean[][]{{false, false}, {true, false}});
		// 
		expected.add(new boolean[][]{{false, false}, {false, true}}); expected.add(new boolean[][]{{false, false}, {false, true}});
		expected.add(new boolean[][]{{false, false}, {false, true}}); expected.add(new boolean[][]{{false, false}, {false, true}});
		//
		expected.add(new boolean[][]{{false, false}, {false, false}}); expected.add(new boolean[][]{{false, false}, {false, false}});
		expected.add(new boolean[][]{{false, false}, {false, false}}); expected.add(new boolean[][]{{false, false}, {false, false}});

		List<boolean[][]> actual = new ArrayList<boolean[][]>();
		for (int i = 0; i < predAndAdVoices.size(); i++) {
			actual.add(ErrorCalculator.assertCorrectnessEDUNotes(predAndAdVoices.get(i),
				allowedVoices));
		}
 
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j].length, actual.get(i)[j].length);
				for (int k = 0; k < expected.get(i)[j].length; k++) {
					assertEquals(expected.get(i)[j][k], actual.get(i)[j][k]);
				}
			}
		}
	}


	public void testSwapEDUnotes() {
		List<List<Integer>> notesPerVoicePredicted = new ArrayList<List<Integer>>();
		notesPerVoicePredicted.add(Arrays.asList(new Integer[]{1, 3, 6, 8, 10, 15, 16, 18}));
		notesPerVoicePredicted.add(Arrays.asList(new Integer[]{2, 4, 9, 13, 19}));
		notesPerVoicePredicted.add(Arrays.asList(new Integer[]{0, 5, 7, 11, 12, 14, 17}));

		List<Integer[]> equalDurationUnisonsInfo = new ArrayList<Integer[]>(); 
		equalDurationUnisonsInfo.add(null);
		equalDurationUnisonsInfo.add(1, new Integer[]{1, 0, 2}); 
		equalDurationUnisonsInfo.add(2, new Integer[]{1, 0, 1});
		equalDurationUnisonsInfo.add(null);
		equalDurationUnisonsInfo.add(4, new Integer[]{2, 1, 5}); 
		equalDurationUnisonsInfo.add(5, new Integer[]{2, 1, 4});
		equalDurationUnisonsInfo.add(null);
		equalDurationUnisonsInfo.add(null);
		equalDurationUnisonsInfo.add(8, new Integer[]{1, 0, 9}); 
		equalDurationUnisonsInfo.add(9, new Integer[]{1, 0, 8});
		equalDurationUnisonsInfo.add(null);
		equalDurationUnisonsInfo.add(null);
		equalDurationUnisonsInfo.add(null);
		equalDurationUnisonsInfo.add(13, new Integer[]{2, 1, 14}); 
		equalDurationUnisonsInfo.add(14, new Integer[]{2, 1, 13});
		equalDurationUnisonsInfo.add(null);
		equalDurationUnisonsInfo.add(null);
		equalDurationUnisonsInfo.add(null);
		equalDurationUnisonsInfo.add(18, new Integer[]{1, 0, 19}); 
		equalDurationUnisonsInfo.add(19, new Integer[]{1, 0, 18});

		// Determine expected
		List<List<Integer>> expected = new ArrayList<List<Integer>>();
		expected.add(Arrays.asList(new Integer[]{2, 3, 6, 9, 10, 15, 16, 19}));
		expected.add(Arrays.asList(new Integer[]{1, 5, 8, 14, 18}));
		expected.add(Arrays.asList(new Integer[]{0, 4, 7, 11, 12, 13, 17}));

		// Calculate actual
		List<List<Integer>> actual = ErrorCalculator.swapEDUnotes(notesPerVoicePredicted, 
			getActualVoicesToyExamplesNonTab(), equalDurationUnisonsInfo);

		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}  
	}

}
