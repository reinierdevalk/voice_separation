package machineLearning;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.Dataset;
import data.Dataset.DatasetID;
import junit.framework.TestCase;
import representations.Tablature;
import representations.Transcription;
import tools.ToolBox;
import ui.Runner;
import ui.Runner.Model;
import ui.Runner.ProcessingMode;
import ui.UI;
import utility.DataConverter;
import de.uos.fmt.musitech.data.score.NotationStaff;
import de.uos.fmt.musitech.data.score.NotationSystem;
import de.uos.fmt.musitech.data.score.NotationVoice;
import de.uos.fmt.musitech.data.score.ScoreNote;
import de.uos.fmt.musitech.data.score.ScorePitch;
import de.uos.fmt.musitech.data.structure.Note;
import de.uos.fmt.musitech.data.structure.Piece;
import de.uos.fmt.musitech.utility.math.Rational;
import featureExtraction.FeatureGenerator;

public class TestManagerTest extends TestCase {

	private File encodingTestpiece1; // = new File(Runner.encodingsPathTest+ "testpiece.txt");
	private File midiTestpiece1; // = new File(Runner.midiPathTest + "testpiece.mid");	
//	DataConverter dataConverter = new DataConverterTab();
	String[] testPaths;

	protected void setUp() throws Exception {
		super.setUp();
		Runner.setPathsToCodeAndData(UI.getRootPath(), false);
		testPaths = new String[]{Runner.encodingsPath + "test/", Runner.midiPath + "test/", 
			Runner.midiPath + "test/"};
		encodingTestpiece1 = new File(Runner.encodingsPath + "test/" + "testpiece.tbp");
		midiTestpiece1 = new File(Runner.midiPath + "test/" + "testpiece.mid");	
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}


	public void testGetMaximumDuration() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);
		
		List<Rational> nextMetricTimes = new ArrayList<Rational>();
		// Chord 0
		nextMetricTimes.add(new Rational(4, 4)); nextMetricTimes.add(new Rational(4, 4));
		nextMetricTimes.add(new Rational(4, 4)); nextMetricTimes.add(new Rational(4, 4));
		// Chord 1
		nextMetricTimes.add(new Rational(19, 16)); nextMetricTimes.add(new Rational(5, 4));
		nextMetricTimes.add(new Rational(5, 4)); nextMetricTimes.add(new Rational(5, 4));
		// Chord 2
		nextMetricTimes.add(new Rational(5, 4));
		// Chord 3
		nextMetricTimes.add(new Rational(11, 8)); nextMetricTimes.add(new Rational(6, 4));
		nextMetricTimes.add(new Rational(6, 4)); nextMetricTimes.add(new Rational(6, 4));
		// Chord 4
		nextMetricTimes.add(new Rational(6, 4));
		// Chord 5
		nextMetricTimes.add(new Rational(7, 4)); nextMetricTimes.add(new Rational(8, 4));
		nextMetricTimes.add(new Rational(7, 4)); nextMetricTimes.add(new Rational(7, 4));
		nextMetricTimes.add(new Rational(7, 4));
		// Chord 6 (value for first note fictional)
		nextMetricTimes.add(new Rational(8, 4)); nextMetricTimes.add(new Rational(15, 8));
		nextMetricTimes.add(new Rational(15, 8)); nextMetricTimes.add(new Rational(8, 4));
		// Chord 7
		nextMetricTimes.add(new Rational(8, 4)); nextMetricTimes.add(new Rational(8, 4));
		// Chord 8
		nextMetricTimes.add(new Rational(11, 4)); nextMetricTimes.add(new Rational(11, 4));
		nextMetricTimes.add(new Rational(11, 4)); nextMetricTimes.add(new Rational(33, 16));
		// Chord 9-14
		nextMetricTimes.add(new Rational(17, 8)); nextMetricTimes.add(new Rational(69, 32));
		nextMetricTimes.add(new Rational(35, 16)); nextMetricTimes.add(new Rational(71, 32));
		nextMetricTimes.add(new Rational(9, 4)); nextMetricTimes.add(new Rational(11, 4));
		// Chord 15 (all values fictional)
		nextMetricTimes.add(new Rational(12, 4)); nextMetricTimes.add(new Rational(12, 4));
		nextMetricTimes.add(new Rational(12, 4)); nextMetricTimes.add(new Rational(12, 4));

		// Determine expected
		List<Rational> expected = new ArrayList<Rational>();
		// a. For type (i) and (ii) conflicts
		List<Rational> expectedTypeOneAndTwo = new ArrayList<Rational>();
		// Chord 0
		expectedTypeOneAndTwo.add(new Rational(1, 4)); expectedTypeOneAndTwo.add(new Rational(1, 4));
		expectedTypeOneAndTwo.add(new Rational(1, 4)); expectedTypeOneAndTwo.add(new Rational(1, 4));
		// Chord 1
		expectedTypeOneAndTwo.add(new Rational(3, 16)); expectedTypeOneAndTwo.add(new Rational(1, 4));
		expectedTypeOneAndTwo.add(new Rational(1, 4)); expectedTypeOneAndTwo.add(new Rational(1, 4));
		// Chord 2
		expectedTypeOneAndTwo.add(new Rational(1, 16));
		// Chord 3
		expectedTypeOneAndTwo.add(new Rational(1, 8)); expectedTypeOneAndTwo.add(new Rational(1, 4));
		expectedTypeOneAndTwo.add(new Rational(1, 4)); expectedTypeOneAndTwo.add(new Rational(1, 4));
		// Chord 4
		expectedTypeOneAndTwo.add(new Rational(1, 8));
		// Chord 5
		expectedTypeOneAndTwo.add(new Rational(1, 4)); expectedTypeOneAndTwo.add(new Rational(1, 2)); 
		expectedTypeOneAndTwo.add(new Rational(1, 4)); expectedTypeOneAndTwo.add(new Rational(1, 4)); 
		expectedTypeOneAndTwo.add(new Rational(1, 4));
		// Chord 6
		expectedTypeOneAndTwo.add(new Rational(1, 4)); expectedTypeOneAndTwo.add(new Rational(1, 8));
		expectedTypeOneAndTwo.add(new Rational(1, 8)); expectedTypeOneAndTwo.add(new Rational(1, 4));
		// Chord 7
		expectedTypeOneAndTwo.add(new Rational(1, 8)); expectedTypeOneAndTwo.add(new Rational(1, 8));
		// Chord 8
		expectedTypeOneAndTwo.add(new Rational(3, 4)); expectedTypeOneAndTwo.add(new Rational(3, 4));
		expectedTypeOneAndTwo.add(new Rational(3, 4)); expectedTypeOneAndTwo.add(new Rational(1, 16));
		// Chord 9-14
		expectedTypeOneAndTwo.add(new Rational(1, 16)); expectedTypeOneAndTwo.add(new Rational(1, 32)); 
		expectedTypeOneAndTwo.add(new Rational(1, 32)); expectedTypeOneAndTwo.add(new Rational(1, 32)); 
		expectedTypeOneAndTwo.add(new Rational(1, 32)); expectedTypeOneAndTwo.add(new Rational(1, 2));
		// Chord 15
		expectedTypeOneAndTwo.add(new Rational(1, 4)); expectedTypeOneAndTwo.add(new Rational(1, 4));
		expectedTypeOneAndTwo.add(new Rational(1, 4)); expectedTypeOneAndTwo.add(new Rational(1, 4));
		expected.addAll(expectedTypeOneAndTwo);

		// b. For type (iv) conflicts
		List<Rational> expectedTypeFour = new ArrayList<Rational>(expectedTypeOneAndTwo);
		expectedTypeFour.set(19, null);
		expectedTypeFour.set(35, null);
		expectedTypeFour.set(36, null);
		expectedTypeFour.set(37, null);
		expectedTypeFour.set(38, null);
		expected.addAll(expectedTypeFour);

		// Calculate actual
		List<Rational> actual = new ArrayList<Rational>();
		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
		List<List<Double>> voiceLabels = transcription.getVoiceLabels();
		// a. For type (i) and (ii) conflicts 
		for (int i = 0; i < basicTabSymbolProperties.length; i++) {
			Rational currentMetricTime = 
				new Rational(basicTabSymbolProperties[i][Tablature.ONSET_TIME],	Tablature.SRV_DEN);
			List<Integer> currentPredictedVoices = DataConverter.convertIntoListOfVoices(voiceLabels.get(i));
			actual.add(TestManager.getMaximumDuration(currentMetricTime, nextMetricTimes.get(i), transcription, 
				currentPredictedVoices));
		}
		// b. For type (iv) conflicts
		for (int i = 0; i < basicTabSymbolProperties.length; i++) {
			Rational currentMetricTime = 
				new Rational(basicTabSymbolProperties[i][Tablature.ONSET_TIME],	Tablature.SRV_DEN);
			List<Integer> currentPredictedVoices = DataConverter.convertIntoListOfVoices(voiceLabels.get(i));
			actual.add(TestManager.getMaximumDuration(currentMetricTime, null, transcription, currentPredictedVoices));
		}

		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
		assertEquals(expected.get(i), actual.get(i)); 
		} 
	}


	public void testResolveConflicts() { 
		// Conflicts:
		// Chord 0, notes 0-3: type (i) conflict: 
		// (i)   note 2 has the same first predicted voice as note 0 (voice 3)
		//
		// Chord 1, notes 4-7: type (ii) conflict: 
		// (ii)  note 7 has the same first predicted voice as the second predicted voice for note 5 (voice 0)
		//
		// Chord 2, notes 8-11: type (iii) conflict: 
		// (iii) note 11 has the same second predicted voice as note 9 (voice 2)
		//
		// Chord 3, notes 12-13: type (iii) and (ii) conflicts:
		// (iii) note 12 has the same second predicted voice as the second predicted voice for sustained note 9 (voice 2)
		// (ii)  note 13 has the same first predicted voice as the second predicted voice for sustained note 9 (voice 2)
		//
		// Chord 4, note 14: type (v) conflict:
		// (v)   the offset time of note 8 exceeds the onset time of the full chord starting on note 15
		//
		// Chord 5, notes 15-19: type (i), (ii), and (iii) conflict:
		// (i)   note 18 has the same first predicted voice as note 17 (voice 1)
		// (ii)  note 18, after its predicted label has been adapted, now has the same first predicted voice as the
		//       second predicted voice for note 15 (voice 2)
		// (iii) note 19 has the same second predicted voice as the first predicted voice for note 15 (voice 4)

		TestManager testManager = new TestManager();		
//		testManager.setIsTablatureCase(true); // TODO some of these are superfluous here
//		testManager.setUseTablatureInformation(true);
//		testManager.setModelDuration(true);
		testManager.allPredictedVoices = new ArrayList<List<Integer>>();
		testManager.allPredictedDurations = new ArrayList<Rational[]>();
		testManager.allDurationLabels = new ArrayList<List<Double>>();
		testManager.allVoicesCoDNotes = new ArrayList<Integer[]>();
		testManager.allVoiceLabels = new ArrayList<List<Double>>();
//		testManager.outputEvaluator = new OutputEvaluator();
//		testManager.dataConverter = new DataConverterTab();
		testManager.conflictsRecord = "";
		testManager.conflictIndices = new ArrayList<List<Integer>>();
		testManager.conflictIndices.add(new ArrayList<Integer>());
		testManager.conflictIndices.add(new ArrayList<Integer>());
//		testManager.setAllowNonMonophonic(false);
//		testManager.setIsNewModel(true);//2016
//		testManager.setReversePiece(false);
//		testManager.setModelBackward(false);
//		testManager.featureGenerator = new FeatureGenerator();		
		
//		Map<String, Double> mp = new LinkedHashMap<String, Double>();
//		mp.put(Runner.APPL_TO_NEW_DATA, 0.0);
//		Runner.setModelParams(mp);
//		Dataset ds = new Dataset(DatasetID.TESTPIECE_SET);
//		ds.populateDataset(mp, null, Runner.testPaths);
//		Runner.setDataset(ds);

		// Determine the variables needed for resolveConflicts()		
		// modelParameters
		Map<String, Double> modelParameters = new HashMap<String, Double>();
//		modelParameters.put(Dataset.DATASET_ID, (double) DatasetID.TESTPIECE_SET.getIntRep());
		modelParameters.put(Runner.MODEL, (double) Model.N_PRIME.getIntRep());
		modelParameters.put(Runner.PROC_MODE, (double) ProcessingMode.FWD.getIntRep());
//		modelParameters.put(Runner.IMPLEMENTATION, (double) Implementation.PHD.getIntRep());
		modelParameters.put(Runner.SNU, 1.0);
		modelParameters.put(Runner.DEV_THRESHOLD, 0.05);
//		modelParameters.put(Runner.HIGHEST_NUM_VOICES, 5.0);
//		modelParameters.put(Runner.MODEL_BACKWARD, 0.0);
//		modelParameters.put(Runner.MODEL_DURATION, 1.0);
//		modelParameters.put(Runner.TAB_DATA, 1.0);
//		modelParameters.put(Runner.MODEL_BIDIR, 0.0);
		modelParameters.put(Runner.MODEL_DURATION_AGAIN, 0.0);
//		modelParameters.put(Runner.GIVE_FIRST, 0.0);
//		modelParameters.put(Runner.DEPLOY_TRAINED_USER_MODEL, 0.0);
		Runner.setModelParams(modelParameters);
		
		// ds
		Dataset ds = new Dataset(Dataset.TEST_TAB);
		ds.populateDataset(null, testPaths, false);
		Runner.setDataset(ds);
		
		// basicTabSymbolProperties and meterInfo
		testManager.tablature = 
			new Tablature(new File(Runner.encodingsPath + "test/" + "test_resolve_conflicts.tbp"), false);
		testManager.basicTabSymbolProperties = testManager.tablature.getBasicTabSymbolProperties();
		testManager.meterInfo = testManager.tablature.getTimeline().getMeterInfo();
//		testManager.meterInfo = testManager.tablature.getTimeline().getMeterInfoOBS();

		// Pre-set the necessary lists with dummy values
		for (int i = 0; i < testManager.basicTabSymbolProperties.length; i++) {
			testManager.allVoiceLabels.add(null);
			testManager.allDurationLabels.add(null);
			testManager.allVoicesCoDNotes.add(null);
		}

//		// allMetricPositions
//		testManager.allMetricPositions = new ArrayList<Rational[]>();
//		for (int i = 0; i < testManager.basicTabSymbolProperties.length; i++) {
//			Rational currMetricTime = 
//				new Rational(testManager.basicTabSymbolProperties[i][Tablature.ONSET_TIME],
//				Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//			testManager.allMetricPositions.add(Tablature.getMetricPosition(currMetricTime, testManager.tablature.getMeterInfo()));
//		}

		// allNetworkOutputs
		double[] eighth = new double[Transcription.DURATION_LABEL_SIZE];
		Arrays.fill(eighth, 0.0); eighth[3] = 0.8; eighth[4] = 0.1; eighth[21] = 0.3; eighth[6] = 0.7;
		double[] quarter = new double[Transcription.DURATION_LABEL_SIZE];
		Arrays.fill(quarter, 0.0); quarter[7] = 0.8; quarter[3] = 0.1; quarter[21] = 0.3; quarter[6] = 0.7;
		double[] half = new double[Transcription.DURATION_LABEL_SIZE];
		Arrays.fill(half, 0.0);	half[15] = 0.8; half[0] = 0.1; half[16] = 0.3; half[31] = 0.7; 
		double[] whole = new double[Transcription.DURATION_LABEL_SIZE];
		Arrays.fill(whole, 0.0); whole[31] = 0.8; whole[5] = 0.1; whole[16] = 0.3; whole[30] = 0.7;

		// Chord 0
		List<double[]> allNetworkOutputs = new ArrayList<double[]>();
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.1, 0.6, 0.84, 0.8}, half}))); // voice 3 and 4
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.2, 0.8, 0.1, 0.6}, half}))); // voice 2
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.6, 0.1, 0.8, 0.0}, half}))); // voice 3 --> voice 1
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.8, 0.1, 0.1, 0.1, 0.6}, half}))); // voice 0
		// Chord 1
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.1, 0.1, 0.8, 0.6}, half}))); // voice 3
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.76, 0.8, 0.1, 0.1, 0.1}, half}))); // voices 1 and 0 --> voice 1
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.1, 0.8, 0.1, 0.6}, half}))); // voice 2
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.8, 0.1, 0.1, 0.1, 0.6}, half}))); // voice 0
		// Chord 2
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.1, 0.1, 0.7, 0.8}, whole}))); // voice 4
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
		  new double[]{0.6, 0.1, 0.76, 0.8, 0.1}, half}))); // voices 3 and 2
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.8, 0.1, 0.1, 0.1, 0.6}, quarter}))); // voice 0
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
		  new double[]{0.1, 0.8, 0.76, 0.1, 0.6}, quarter}))); // voices 1 and 2 --> voice 1
		// Chord 3
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.8, 0.76, 0.1, 0.1}, quarter}))); // voice 1 and 2 --> voice 1
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
		  new double[]{0.2, 0.1, 0.8, 0.6, 0.0}, quarter}))); // voice 2
		// Chord 4
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
		  new double[]{0.8, 0.1, 0.6, 0.2, 0.0}, eighth}))); // voice 0
		// Chord 5
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.1, 0.8, 0.76, 0.84}, half}))); // voices 4 and 2 --> voice 4
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
		  new double[]{0.1, 0.1, 0.6, 0.8, 0.2}, half}))); // voice 3
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.8, 0.1, 0.1, 0.6}, half}))); // voice 1
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.8, 0.6, 0.1, 0.0}, half}))); // voice 1 --> voice 2
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.8, 0.1, 0.1, 0.2, 0.76}, half}))); // voices 0 and 4 --> voice 0
		testManager.allNetworkOutputs = new ArrayList<double[]>(allNetworkOutputs);

		// allNetworkOutputsAdapted (in its initial state, in which it is the same as allNetworkOutputs)
		testManager.allNetworkOutputsAdapted = new ArrayList<double[]>(allNetworkOutputs);

		// piece
		Piece piece = new Piece();
		NotationSystem system = piece.createNotationSystem();
		// Voice 0
		NotationStaff staff0 = new NotationStaff(system); system.add(staff0);
		NotationVoice voice0 = new NotationVoice(staff0); staff0.add(voice0);
		Note voice0n0 = Transcription.createNote(67, new Rational(0, 4), new Rational(1, 2));
		Note voice0n1a = Transcription.createNote(53, new Rational(2, 4), new Rational(1, 2)); // to be removed 
		Note voice0n1b = Transcription.createNote(67, new Rational(2, 4), new Rational(1, 2));
		Note voice0n2 = Transcription.createNote(57, new Rational(4, 4), new Rational(1, 4));
		Note voice0n3 = Transcription.createNote(67, new Rational(11, 8), new Rational(1, 8));
		Note voice0n4 = Transcription.createNote(67, new Rational(6, 4), new Rational(1, 2));
		voice0.add(voice0n0); voice0.add(voice0n1a); voice0.add(voice0n1b); voice0.add(voice0n2); voice0.add(voice0n3); 
		voice0.add(voice0n4);
		// Voice 1
		NotationStaff staff1 = new NotationStaff(system); system.add(staff1); 
		NotationVoice voice1 = new NotationVoice(staff1); staff1.add(voice1); 
		Note voice1n0 = Transcription.createNote(57, new Rational(0, 4), new Rational(1, 2));
		Note voice1n1 = Transcription.createNote(53, new Rational(2, 4), new Rational(1, 2));
		Note voice1n2 = Transcription.createNote(67, new Rational(4, 4), new Rational(1, 4));
		Note voice1n3 = Transcription.createNote(57, new Rational(5, 4), new Rational(1, 4));
		Note voice1n4 = Transcription.createNote(53, new Rational(6, 4), new Rational(1, 2));
		voice1.add(voice1n0); voice1.add(voice1n1); voice1.add(voice1n2); voice1.add(voice1n3); voice1.add(voice1n4);
		// Voice 2
		NotationStaff staff2 = new NotationStaff(system); system.add(staff2); 
		NotationVoice voice2 = new NotationVoice(staff2); staff2.add(voice2);
		Note voice2n0 = Transcription.createNote(53, new Rational(0, 4), new Rational(1, 2));
		Note voice2n1 = Transcription.createNote(57, new Rational(2, 4), new Rational(1, 2));
		Note voice2n2 = Transcription.createNote(53, new Rational(4, 4), new Rational(1, 2)); // to be adapted
		Note voice2n3 = Transcription.createNote(67, new Rational(5, 4), new Rational(1, 4));
		Note voice2n4a = Transcription.createNote(43, new Rational(6, 4), new Rational(1, 2)); // to be removed
		Note voice2n4b = Transcription.createNote(57, new Rational(6, 4), new Rational(1, 2));
		voice2.add(voice2n0); voice2.add(voice2n1); voice2.add(voice2n2); voice2.add(voice2n3); 
		voice2.add(voice2n4a); voice2.add(voice2n4b);
		// Voice 3
		NotationStaff staff3 = new NotationStaff(system); system.add(staff3); 
		NotationVoice voice3 = new NotationVoice(staff3); staff3.add(voice3); 
		Note voice3n0 = Transcription.createNote(43, new Rational(0, 4), new Rational(1, 2));
		Note voice3n1 = Transcription.createNote(43, new Rational(2, 4), new Rational(1, 2));
		Note voice3n2 = Transcription.createNote(53, new Rational(4, 4), new Rational(1, 2)); // to be adapted
		Note voice3n3 = Transcription.createNote(48, new Rational(6, 4), new Rational(1, 2));
		voice3.add(voice3n0); voice3.add(voice3n1); voice3.add(voice3n2); voice3.add(voice3n3); 
		// Voice 4
		NotationStaff staff4 = new NotationStaff(system); system.add(staff4); 
		NotationVoice voice4 = new NotationVoice(staff4); staff4.add(voice4);
		Note voice4n0 = Transcription.createNote(43, new Rational(0, 4), new Rational(1, 2));
		Note voice4n1 = Transcription.createNote(43, new Rational(4, 4), new Rational(1, 1)); // to be adapted
		Note voice4n2 = Transcription.createNote(43, new Rational(6, 4), new Rational(1, 2));
		voice4.add(voice4n0); voice4.add(voice4n1); voice4.add(voice4n2);
		testManager.newTranscription = new Transcription();
		testManager.newTranscription.setPiece(piece);

		// allNotes
		List<List<Note>> allNotes = new ArrayList<List<Note>>();
		// Chord 0
		allNotes.add(Arrays.asList(new Note[]{voice3n0})); 
		allNotes.add(Arrays.asList(new Note[]{voice2n0}));
		allNotes.add(Arrays.asList(new Note[]{voice1n0})); 
		allNotes.add(Arrays.asList(new Note[]{voice0n0}));
		// Chord 1
		allNotes.add(Arrays.asList(new Note[]{voice3n1})); 
		allNotes.add(Arrays.asList(new Note[]{voice1n1}));
		allNotes.add(Arrays.asList(new Note[]{voice2n1}));
		allNotes.add(Arrays.asList(new Note[]{voice0n1b}));
		// Chord 2
		allNotes.add(Arrays.asList(new Note[]{voice4n1}));
		allNotes.add(Arrays.asList(new Note[]{voice3n2})); 
		allNotes.add(Arrays.asList(new Note[]{voice0n2})); 
		allNotes.add(Arrays.asList(new Note[]{voice1n2}));
		// Chord 3
		allNotes.add(Arrays.asList(new Note[]{voice1n3}));
		allNotes.add(Arrays.asList(new Note[]{voice2n3}));
		// Chord 4
		allNotes.add(Arrays.asList(new Note[]{voice0n3}));
		// Chord 5
		allNotes.add(Arrays.asList(new Note[]{voice4n2}));
		allNotes.add(Arrays.asList(new Note[]{voice3n3}));
		allNotes.add(Arrays.asList(new Note[]{voice1n4}));
		allNotes.add(Arrays.asList(new Note[]{voice2n4b})); 
		allNotes.add(Arrays.asList(new Note[]{voice0n4}));
		testManager.allNotes = new ArrayList<List<Note>>(allNotes);

		// Determine the variables created and/or adapted by resolveConflicts()
		// conflictIndicesExpected
		List<List<Integer>> conflictIndicesExpected = new ArrayList<List<Integer>>();
		conflictIndicesExpected.add(Arrays.asList(new Integer[]{2, 5, 11, 12, 18, 15, 19}));
		conflictIndicesExpected.add(Arrays.asList(new Integer[]{9, 8}));

		// allNetworkOutputsAdaptedExpected
		List<double[]> allNetworkOutputsAdaptedExpected = new ArrayList<double[]>(allNetworkOutputs);
		allNetworkOutputsAdaptedExpected.set(2, 
			ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{0.1, 0.6, 0.1, 0.0, 0.0}, half})));	  
		allNetworkOutputsAdaptedExpected.set(11, 
		  ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{0.1, 0.8, 0.0, 0.1, 0.6}, quarter})));	  
		allNetworkOutputsAdaptedExpected.set(12, 
		  ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{0.1, 0.8, 0.0, 0.1, 0.1}, quarter})));
		allNetworkOutputsAdaptedExpected.set(18, 
		   ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{0.1, 0.0, 0.6, 0.1, 0.0}, half})));
		allNetworkOutputsAdaptedExpected.set(19, 
		  ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{0.8, 0.1, 0.1, 0.2, 0.0}, half})));

		// allPredictedVoicesExpected
		List<List<Integer>> allPredictedVoices = new ArrayList<List<Integer>>();
		// Chord 0
		allPredictedVoices.add(Arrays.asList(new Integer[]{3, 4}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{2}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{3}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{0}));
		// Chord 1
		allPredictedVoices.add(Arrays.asList(new Integer[]{3}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1, 0}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{2}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{0}));
		// Chord 2
		allPredictedVoices.add(Arrays.asList(new Integer[]{4}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{3, 2}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{0}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1, 2}));
		// Chord 3
		allPredictedVoices.add(Arrays.asList(new Integer[]{1, 2}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{2}));
		// Chord 4
		allPredictedVoices.add(Arrays.asList(new Integer[]{0}));
		// Chord 5
		allPredictedVoices.add(Arrays.asList(new Integer[]{4, 2}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{3}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{0, 4}));

		List<List<Integer>> allPredictedVoicesExpected = new ArrayList<List<Integer>>(allPredictedVoices);
		allPredictedVoicesExpected.set(2, Arrays.asList(new Integer[]{1}));
		allPredictedVoicesExpected.set(5, Arrays.asList(new Integer[]{1}));
		allPredictedVoicesExpected.set(11, Arrays.asList(new Integer[]{1}));
		allPredictedVoicesExpected.set(12, Arrays.asList(new Integer[]{1}));
		allPredictedVoicesExpected.set(15, Arrays.asList(new Integer[]{4}));
		allPredictedVoicesExpected.set(18, Arrays.asList(new Integer[]{2}));
		allPredictedVoicesExpected.set(19, Arrays.asList(new Integer[]{0}));

		// allVoiceLabelsExpected
		List<List<Double>> allVoiceLabels = new ArrayList<List<Double>>();
		// Chord 0
		allVoiceLabels.add(Transcription.combineLabels(Transcription.VOICE_3, Transcription.VOICE_4));
		allVoiceLabels.add(Transcription.VOICE_2);
		allVoiceLabels.add(Transcription.VOICE_3);
		allVoiceLabels.add(Transcription.VOICE_0);
		// Chord 1
		allVoiceLabels.add(Transcription.VOICE_3);
		allVoiceLabels.add(Transcription.combineLabels(Transcription.VOICE_1, Transcription.VOICE_0));
		allVoiceLabels.add(Transcription.VOICE_2);
		allVoiceLabels.add(Transcription.VOICE_0);
		// Chord 2
		allVoiceLabels.add(Transcription.VOICE_4);
		allVoiceLabels.add(Transcription.combineLabels(Transcription.VOICE_3, Transcription.VOICE_2));
		allVoiceLabels.add(Transcription.VOICE_0);
		allVoiceLabels.add(Transcription.combineLabels(Transcription.VOICE_1, Transcription.VOICE_2));
		// Chord 3
		allVoiceLabels.add(Transcription.combineLabels(Transcription.VOICE_1, Transcription.VOICE_2));
		allVoiceLabels.add(Transcription.VOICE_2);
		// Chord 4
		allVoiceLabels.add(Transcription.VOICE_0);
		// Chord 5
		allVoiceLabels.add(Transcription.combineLabels(Transcription.VOICE_4, Transcription.VOICE_2));
		allVoiceLabels.add(Transcription.VOICE_3);
		allVoiceLabels.add(Transcription.VOICE_1);
		allVoiceLabels.add(Transcription.VOICE_1);
		allVoiceLabels.add(Transcription.combineLabels(Transcription.VOICE_0, Transcription.VOICE_4));

		List<List<Double>> allVoiceLabelsExpected = new ArrayList<List<Double>>(allVoiceLabels);
		allVoiceLabelsExpected.set(2, Transcription.VOICE_1);
		allVoiceLabelsExpected.set(5, Transcription.VOICE_1);
		allVoiceLabelsExpected.set(11, Transcription.VOICE_1);
		allVoiceLabelsExpected.set(12, Transcription.VOICE_1);
		allVoiceLabelsExpected.set(15, Transcription.VOICE_4);
		allVoiceLabelsExpected.set(18, Transcription.VOICE_2);
		allVoiceLabelsExpected.set(19, Transcription.VOICE_0);

		// allPredictedDurationsExpected
		List<Rational[]> allPredictedDurationsExpected = new ArrayList<Rational[]>();
		// Chord 0
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		// Chord 1
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		// Chord 2
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 4)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 4)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 4)});
		// Chord 3
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 4)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 4)});
		// Chord 4
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 8)});
		// Chord 5
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});

		// allDurationLabelsExpected
		List<List<Double>> allDurationLabelsExpected = new ArrayList<List<Double>>();
		
		List<Double> eighthLabel = Transcription.EIGHTH; // trp dur
//		List<Double> eighthLabel = Transcription.createDurationLabel(4*3); // trp dur
		List<Double> quarterLabel = Transcription.QUARTER; // trp dur
//		List<Double> quarterLabel = Transcription.createDurationLabel(8*3); // trp dur
		List<Double> halfLabel = Transcription.HALF; // trp dur
//		List<Double> halfLabel = Transcription.createDurationLabel(16*3); // trp dur
		// Chord 0
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		// Chord 1
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		// Chord 2
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(quarterLabel);
		allDurationLabelsExpected.add(quarterLabel);
		allDurationLabelsExpected.add(quarterLabel);
		// Chord 3
		allDurationLabelsExpected.add(quarterLabel);
		allDurationLabelsExpected.add(quarterLabel);
		// Chord 4
		allDurationLabelsExpected.add(eighthLabel);
		// Chord 5
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);

		// allVoicesCoDNotesExpected
		List<Integer[]> allVoicesCoDNotesExpected = new ArrayList<Integer[]>();
		// Chord 0
		allVoicesCoDNotesExpected.add(new Integer[]{4, 3});
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		// Chord 1
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		// Chord 2
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(new Integer[]{3, 2});
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		// Chord 3
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		// Chord 4
		allVoicesCoDNotesExpected.add(null);
		// Chord 5
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);

		// pieceExpected
		Piece pieceExpected = new Piece();
		NotationSystem adaptedSystem = pieceExpected.createNotationSystem();
		// Voice 0
		NotationStaff adaptedStaff0 = new NotationStaff(adaptedSystem); adaptedSystem.add(adaptedStaff0);
		NotationVoice adaptedVoice0 = new NotationVoice(adaptedStaff0); adaptedStaff0.add(adaptedVoice0);
		adaptedVoice0.add(voice0n0); /*adaptedVoice0.add(voice0n1a);*/ adaptedVoice0.add(voice0n1b); 
		adaptedVoice0.add(voice0n2); adaptedVoice0.add(voice0n3); adaptedVoice0.add(voice0n4);
		// Voice 1
		NotationStaff adaptedStaff1 = new NotationStaff(adaptedSystem); adaptedSystem.add(adaptedStaff1); 
		NotationVoice adaptedVoice1 = new NotationVoice(adaptedStaff1); adaptedStaff1.add(adaptedVoice1);
		adaptedVoice1.add(voice1n0); adaptedVoice1.add(voice1n1); adaptedVoice1.add(voice1n2); 
		adaptedVoice1.add(voice1n3); adaptedVoice1.add(voice1n4); 
		// Voice 2
		Note voice2n2Adapted = Transcription.createNote(53, new Rational(4, 4), new Rational(1, 4));
		NotationStaff adaptedStaff2 = new NotationStaff(adaptedSystem); adaptedSystem.add(adaptedStaff2); 
		NotationVoice adaptedVoice2 = new NotationVoice(adaptedStaff2); adaptedStaff2.add(adaptedVoice2);
		adaptedVoice2.add(voice2n0); adaptedVoice2.add(voice2n1); adaptedVoice2.add(voice2n2Adapted); 
		adaptedVoice2.add(voice2n3); /*adaptedVoice2.add(voice2n4a);*/ adaptedVoice2.add(voice2n4b);
		// Voice 3
		Note voice3n2Adapted = Transcription.createNote(53, new Rational(4, 4), new Rational(1, 4));
		NotationStaff adaptedStaff3 = new NotationStaff(adaptedSystem); adaptedSystem.add(adaptedStaff3); 
		NotationVoice adaptedVoice3 = new NotationVoice(adaptedStaff3); adaptedStaff3.add(adaptedVoice3);
		adaptedVoice3.add(voice3n0); adaptedVoice3.add(voice3n1); adaptedVoice3.add(voice3n2Adapted); 
		adaptedVoice3.add(voice3n3);  
		// Voice 4
		Note voice4n1Adapted = Transcription.createNote(43, new Rational(4, 4), new Rational(1, 2));
		NotationStaff adaptedStaff4 = new NotationStaff(adaptedSystem); adaptedSystem.add(adaptedStaff4); 
		NotationVoice adaptedVoice4 = new NotationVoice(adaptedStaff4); adaptedStaff4.add(adaptedVoice4);
		adaptedVoice4.add(voice4n0); adaptedVoice4.add(voice4n1Adapted); adaptedVoice4.add(voice4n2);

		// allNotesExpected
		List<List<Note>> allNotesExpected = new ArrayList<List<Note>>(allNotes);
		allNotesExpected.set(8, Arrays.asList(new Note[]{voice4n1Adapted}));
		allNotesExpected.set(9, Arrays.asList(new Note[]{voice3n2Adapted}));

		// Calculate actual
		for (int i = 0; i < allNetworkOutputs.size(); i++) {
			testManager.resolveConflicts(allNetworkOutputs.get(i), i);
		}		
		List<List<Integer>> conflictIndicesActual = testManager.conflictIndices; 
		List<double[]> allNetworkOutputsAdaptedActual = testManager.allNetworkOutputsAdapted;
		List<List<Integer>> allPredictedVoicesActual = testManager.allPredictedVoices;
		List<List<Double>> allVoiceLabelsActual = testManager.allVoiceLabels;
		List<Rational[]> allPredictedDurationsActual = testManager.allPredictedDurations;
		List<List<Double>> allDurationLabelsActual = testManager.allDurationLabels;
		List<Integer[]> allVoicesCoDNotesActual = testManager.allVoicesCoDNotes;
		Piece pieceActual = testManager.newTranscription.getPiece();
		List<List<Note>> allNotesActual = testManager.allNotes;
		System.out.println(testManager.conflictsRecord);

		// Assert equality
 		// conflictIndices
		assertEquals(conflictIndicesExpected.size(), conflictIndicesActual.size());
		for (int i = 0; i < conflictIndicesExpected.size(); i++) { 
			assertEquals(conflictIndicesExpected.get(i).size(), conflictIndicesActual.get(i).size());
			for (int j = 0; j < conflictIndicesExpected.get(i).size(); j++) {
				assertEquals(conflictIndicesExpected.get(i).get(j), conflictIndicesActual.get(i).get(j));
			}
		}

		// allNetworkOutputsAdapted
		assertEquals(allNetworkOutputsAdaptedExpected.size(), allNetworkOutputsAdaptedActual.size());
		for (int i = 0; i < allNetworkOutputsAdaptedExpected.size(); i++) {
			assertEquals(allNetworkOutputsAdaptedExpected.get(i).length, allNetworkOutputsAdaptedActual.get(i).length);
			for (int j = 0; j < allNetworkOutputsAdaptedExpected.get(i).length; j++) {
				assertEquals(allNetworkOutputsAdaptedExpected.get(i)[j], allNetworkOutputsAdaptedActual.get(i)[j]);
			}
		}

		// allPredictedVoices
		assertEquals(allPredictedVoicesExpected.size(), allPredictedVoicesActual.size());
		for (int i = 0; i < allPredictedVoicesExpected.size(); i++) {
			assertEquals(allPredictedVoicesExpected.get(i).size(), allPredictedVoicesActual.get(i).size());
			for (int j = 0; j < allPredictedVoicesExpected.get(i).size(); j++) {
				assertEquals(allPredictedVoicesExpected.get(i).get(j), allPredictedVoicesActual.get(i).get(j));
			}
		}

		// allVoiceLabels
		assertEquals(allVoiceLabelsExpected.size(), allVoiceLabelsActual.size());
		for (int i = 0; i < allVoiceLabelsExpected.size(); i++) {
			assertEquals(allVoiceLabelsExpected.get(i).size(),allVoiceLabelsActual.get(i).size());
			for (int j = 0; j < allVoiceLabelsExpected.get(i).size(); j++) {
				assertEquals(allVoiceLabelsExpected.get(i).get(j), allVoiceLabelsActual.get(i).get(j));
			}
		}

		// allPredictedDurations
		assertEquals(allPredictedDurationsExpected.size(), allPredictedDurationsActual.size());
		for (int i = 0; i < allPredictedDurationsExpected.size(); i++) {
			assertEquals(allPredictedDurationsExpected.get(i).length, allPredictedDurationsActual.get(i).length);
			for (int j = 0; j < allPredictedDurationsExpected.get(i).length; j++) {
				assertEquals(allPredictedDurationsExpected.get(i)[j], allPredictedDurationsActual.get(i)[j]);
			}
		}

		// allDurationLabels
		assertEquals(allDurationLabelsExpected.size(), allDurationLabelsActual.size());
		for (int i = 0; i < allDurationLabelsExpected.size(); i++) {
			assertEquals(allDurationLabelsExpected.get(i).size(), allDurationLabelsActual.get(i).size());
			for (int j = 0; j < allDurationLabelsExpected.get(i).size(); j++) {
				assertEquals(allDurationLabelsExpected.get(i).get(j), allDurationLabelsActual.get(i).get(j));
			}
		}

		// allVoicesCoDNotes
		assertEquals(allVoicesCoDNotesExpected.size(), allVoicesCoDNotesActual.size());
		for (int i = 0; i < allVoicesCoDNotesExpected.size(); i++) {
			if (allVoicesCoDNotesExpected.get(i) == null) {
				assertEquals(allVoicesCoDNotesExpected.get(i), allVoicesCoDNotesActual.get(i));
			}
			else {
				assertEquals(allVoicesCoDNotesExpected.get(i).length,allVoicesCoDNotesActual.get(i).length);
				for (int j = 0; j < allVoicesCoDNotesExpected.get(i).length; j++) {
					assertEquals(allVoicesCoDNotesExpected.get(i)[j], allVoicesCoDNotesActual.get(i)[j]);
				}
			}
		}

		// piece
		assertEquals(pieceExpected.getScore().size(), pieceActual.getScore().size());
		// For each NotationStaff at index i
		for (int i = 0; i < pieceExpected.getScore().size(); i++) {
			assertEquals(pieceExpected.getScore().get(i).size(), pieceActual.getScore().get(i).size());
			// For each NotationVoice at index j
			for (int j = 0; j < pieceExpected.getScore().get(i).size(); j++) {
				assertEquals(pieceExpected.getScore().get(i).get(j).size(), pieceActual.getScore().get(i).get(j).size());
				// For each NotationChord at index k
				for (int k = 0; k < pieceExpected.getScore().get(i).get(j).size(); k++) {	
					assertEquals(pieceExpected.getScore().get(i).get(j).get(k).size(), pieceActual.getScore().get(i).get(j).get(k).size());
					// For each Note at index l
					for (int l = 0; l < pieceExpected.getScore().get(i).get(j).get(k).size(); l++) {
//						assertEquals(pieceExpected.getScore().get(i).get(j).get(k).get(l), pieceActual.getScore().get(i).get(j).get(k).get(l));
						// OR if assertEquals(expected.get(i).get(j), actual.get(i).get(j).get(k) does not work because the Notes 
						// are not the same objects: check that pitch, metricTime, and metricDuration are the same
						assertEquals(pieceExpected.getScore().get(i).get(j).get(k).get(l).getMidiPitch(), 
							pieceActual.getScore().get(i).get(j).get(k).get(l).getMidiPitch());
						assertEquals(pieceExpected.getScore().get(i).get(j).get(k).get(l).getMetricTime(), 
							pieceActual.getScore().get(i).get(j).get(k).get(l).getMetricTime());
						assertEquals(pieceExpected.getScore().get(i).get(j).get(k).get(l).getMetricDuration(), 
							pieceActual.getScore().get(i).get(j).get(k).get(l).getMetricDuration());		
					}
				}		
			}
		}

		// allNotes
		assertEquals(allNotesExpected.size(), allNotesActual.size());
		for (int i = 0; i < allNotesExpected.size(); i++) {
			assertEquals(allNotesExpected.get(i).size(), allNotesActual.get(i).size());
			for (int j = 0; j < allNotesExpected.get(i).size(); j++) {
				assertEquals(allNotesExpected.get(i).get(j).getMidiPitch(), allNotesActual.get(i).get(j).getMidiPitch());
				assertEquals(allNotesExpected.get(i).get(j).getMetricTime(), allNotesActual.get(i).get(j).getMetricTime());
				assertEquals(allNotesExpected.get(i).get(j).getMetricDuration(), allNotesActual.get(i).get(j).getMetricDuration());
			}
		}
	}


	public void testResolveConflictsBwd() {
		// Conflicts:
		// Chord 0, notes 0-4 (15-19 fwd): type (i), (ii), and (iii) conflict:
		// (i)   note 3 has the same first predicted voice as note 2 (voice 1)
		// (ii)  note 3, after its predicted label has been adapted, now has the same first predicted voice as the
		//       second predicted voice for note 0 (voice 2)
		// (iii) note 4 has the same second predicted voice as the first predicted voice for note 0 (voice 4)
		//
		// Chord 3, notes 8-11 (8-11 fwd): type (i) and (iii) conflict:
		// (i)   note 8 has an offset that exceeds the onset of the closest next note in its first predicted voice, 
		//       note 0 (voice 4)
		// (iii) note 9 has an offset that exceeds the onset of the closest next note in its second predicted voice,
		//       note 7 (voice 2)
		// (iii) note 11 has the same second predicted voice as the first predicted voice of note 9 (voice 3)      
		//
		// Chord 4, notes 12-15 (4-7 fwd): type (ii) conflict: 
		// (ii)  note 15 has the same first predicted voice as the second predicted voice for note 13 (voice 0)
		//
		// Chord 5, notes 16-19 (0-3 fwd): type (i) conflict: 
		// (i)   note 18 has the same first predicted voice as note 16 (voice 3)

		TestManager testManager = new TestManager();		
//		testManager.setIsTablatureCase(true); // TODO some of these are superfluous here
//		testManager.setUseTablatureInformation(true);
//		testManager.setModelDuration(true);
		testManager.allPredictedVoices = new ArrayList<List<Integer>>();
		testManager.allPredictedDurations = new ArrayList<Rational[]>();
		testManager.allDurationLabels = new ArrayList<List<Double>>();
		testManager.allVoicesCoDNotes = new ArrayList<Integer[]>();
		testManager.allVoiceLabels = new ArrayList<List<Double>>();
//		testManager.outputEvaluator = new OutputEvaluator();
//		testManager.dataConverter = new DataConverterTab();
		testManager.conflictsRecord = "";
		testManager.conflictIndices = new ArrayList<List<Integer>>();
		testManager.conflictIndices.add(new ArrayList<Integer>());
		testManager.conflictIndices.add(new ArrayList<Integer>());
//		testManager.setAllowNonMonophonic(false);
//		testManager.setIsNewModel(true);//2016
//		testManager.setModelBackward(true);
		
//		Map<String, Double> mp = new LinkedHashMap<String, Double>();
//		mp.put(Runner.APPL_TO_NEW_DATA, 0.0);
//		Runner.setModelParams(mp);
//		Dataset ds = new Dataset(DatasetID.TESTPIECE_SET);
//		ds.populateDataset(mp, null, Runner.testPaths);
//		Runner.setDataset(ds);

		// Determine the variables needed for resolveConflicts()		
		// modelParameters
		Map<String, Double> modelParameters = new HashMap<String, Double>();
//		modelParameters.put(Dataset.DATASET_ID, (double) DatasetID.TESTPIECE_SET.getIntRep());
		modelParameters.put(Runner.MODEL, (double) Model.N_PRIME.getIntRep());
		modelParameters.put(Runner.PROC_MODE, (double) ProcessingMode.BWD.getIntRep());
//		modelParameters.put(Runner.IMPLEMENTATION, (double) Implementation.PHD.getIntRep());
		modelParameters.put(Runner.SNU, 1.0);
		modelParameters.put(Runner.DEV_THRESHOLD, 0.05);
//		modelParameters.put(Runner.HIGHEST_NUM_VOICES, 5.0);
//		modelParameters.put(Runner.TAB_DATA, 1.0);
//		modelParameters.put(Runner.MODEL_DURATION, 1.0);
//		modelParameters.put(Runner.MODEL_BACKWARD, 1.0);
//		modelParameters.put(Runner.MODEL_BIDIR, 0.0);
		modelParameters.put(Runner.MODEL_DURATION_AGAIN, 0.0);
//		modelParameters.put(Runner.GIVE_FIRST, 0.0);
//		modelParameters.put(Runner.DEPLOY_TRAINED_USER_MODEL, 0.0);
		Runner.setModelParams(modelParameters);

		// ds
		Dataset ds = new Dataset(Dataset.TEST_TAB);
		ds.populateDataset(null, testPaths, false);
		Runner.setDataset(ds);
		
		// basicTabSymbolProperties (fwd) and meterInfo
		testManager.tablature = 
			new Tablature(new File(Runner.encodingsPath + "test/" + "test_resolve_conflicts.tbp"), false);
		testManager.basicTabSymbolProperties = testManager.tablature.getBasicTabSymbolProperties();
		testManager.meterInfo = testManager.tablature.getTimeline().getMeterInfo();
//		testManager.meterInfo = testManager.tablature.getTimeline().getMeterInfoOBS();

		// Pre-set the necessary lists with dummy values
		for (int i = 0; i < testManager.basicTabSymbolProperties.length; i++) {
			testManager.allVoiceLabels.add(null);
			testManager.allDurationLabels.add(null);
			testManager.allVoicesCoDNotes.add(null);
		}

//		// allMetricPositions (fwd)
//		testManager.allMetricPositions = new ArrayList<Rational[]>();
//		for (int i = 0; i < testManager.basicTabSymbolProperties.length; i++) {
//			Rational currMetricTime = 
//				new Rational(testManager.basicTabSymbolProperties[i][Tablature.ONSET_TIME],
//				Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//			testManager.allMetricPositions.add(Tablature.getMetricPosition(currMetricTime, testManager.tablature.getMeterInfo()));
//		}

		// allNetworkOutputs (bwd)
		double[] eighth = new double[Transcription.DURATION_LABEL_SIZE];
		Arrays.fill(eighth, 0.0); eighth[3] = 0.8; eighth[4] = 0.1; eighth[21] = 0.3; eighth[6] = 0.7;
		double[] quarter = new double[Transcription.DURATION_LABEL_SIZE];
		Arrays.fill(quarter, 0.0); quarter[7] = 0.8; quarter[3] = 0.1; quarter[21] = 0.3; quarter[6] = 0.7;
		double[] half = new double[Transcription.DURATION_LABEL_SIZE];
		Arrays.fill(half, 0.0); half[15] = 0.8; half[0] = 0.1; half[16] = 0.3; half[31] = 0.7; 
		double[] whole = new double[Transcription.DURATION_LABEL_SIZE];
		Arrays.fill(whole, 0.0); whole[31] = 0.8; whole[5] = 0.1; whole[16] = 0.3; whole[30] = 0.7;

		List<double[]> allNetworkOutputs = new ArrayList<double[]>(); 
		// Chord 0
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.1, 0.76, 0.6, 0.8}, half}))); // voices 4 and 2 --> voice 4
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.1, 0.6, 0.8, 0.2}, half}))); // voice 3
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.8, 0.1, 0.1, 0.6}, half}))); // voice 1
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.8, 0.6, 0.1, 0.0}, half}))); // voice 1 --> voice 2
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.8, 0.1, 0.1, 0.1, 0.76}, half}))); // voices 0 and 4 --> voice 0
		// Chord 1
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.8, 0.1, 0.6, 0.2, 0.0}, eighth}))); // voice 0
		// Chord 2
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.8, 0.1, 0.1, 0.1}, quarter}))); // voice 1
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.2, 0.1, 0.8, 0.6, 0.0}, quarter}))); // voice 2
		// Chord 3
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.1, 0.1, 0.7, 0.8}, whole}))); // voice 4
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.6, 0.1, 0.76, 0.8, 0.1}, half}))); // voices 3 and 2 --> voice 3
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.8, 0.1, 0.1, 0.1, 0.6}, quarter}))); // voice 0
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.8, 0.1, 0.76, 0.6}, quarter}))); // voices 1 and 3 --> voice 1
		// Chord 4
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.1, 0.1, 0.8, 0.6}, half}))); // voice 3
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.76, 0.8, 0.1, 0.1, 0.1}, half}))); // voices 1 and 0 --> voice 1
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.1, 0.8, 0.1, 0.6}, half}))); // voice 2
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.8, 0.1, 0.1, 0.1, 0.6}, half}))); // voice 0
		// Chord 5
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.1, 0.6, 0.84, 0.8}, half}))); // voice 3 and 4
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.2, 0.8, 0.1, 0.6}, half}))); // voices 2
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.6, 0.1, 0.8, 0.0}, half}))); // voice 3 --> voice 1
		allNetworkOutputs.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.8, 0.1, 0.1, 0.1, 0.6}, half}))); // voice 0
		testManager.allNetworkOutputs = new ArrayList<double[]>(allNetworkOutputs);

		// allNetworkOutputsAdapted (in its initial state, in which it is the same as allNetworkOutputs) (bwd)
		testManager.allNetworkOutputsAdapted = new ArrayList<double[]>(allNetworkOutputs);

		// piece
		Piece piece = new Piece();
		NotationSystem system = piece.createNotationSystem();
		// Voice 0
		NotationStaff staff0 = new NotationStaff(system); system.add(staff0);
		NotationVoice voice0 = new NotationVoice(staff0); staff0.add(voice0);
		Note voice0n0 = Transcription.createNote(67, new Rational(0, 4), new Rational(1, 2));
		Note voice0n1a = Transcription.createNote(53, new Rational(2, 4), new Rational(1, 2)); // to be removed (type (ii)) 
		Note voice0n1b = Transcription.createNote(67, new Rational(2, 4), new Rational(1, 2));
		Note voice0n2 = Transcription.createNote(57, new Rational(4, 4), new Rational(1, 4));
		Note voice0n3 = Transcription.createNote(67, new Rational(11, 8), new Rational(1, 8));
		Note voice0n4 = Transcription.createNote(67, new Rational(6, 4), new Rational(1, 2));
		voice0.add(voice0n0); voice0.add(voice0n1a); voice0.add(voice0n1b); voice0.add(voice0n2); voice0.add(voice0n3); 
		voice0.add(voice0n4);
		// Voice 1
		NotationStaff staff1 = new NotationStaff(system); system.add(staff1); 
		NotationVoice voice1 = new NotationVoice(staff1); staff1.add(voice1); 
		Note voice1n0 = Transcription.createNote(57, new Rational(0, 4), new Rational(1, 2));
		Note voice1n1 = Transcription.createNote(53, new Rational(2, 4), new Rational(1, 2));
		Note voice1n2 = Transcription.createNote(67, new Rational(4, 4), new Rational(1, 4));
		Note voice1n3 = Transcription.createNote(57, new Rational(5, 4), new Rational(1, 4));
		Note voice1n4 = Transcription.createNote(53, new Rational(6, 4), new Rational(1, 2));
		voice1.add(voice1n0); voice1.add(voice1n1); voice1.add(voice1n2); voice1.add(voice1n3); voice1.add(voice1n4);
		// Voice 2
		NotationStaff staff2 = new NotationStaff(system); system.add(staff2); 
		NotationVoice voice2 = new NotationVoice(staff2); staff2.add(voice2);
		Note voice2n0 = Transcription.createNote(53, new Rational(0, 4), new Rational(1, 2));
		Note voice2n1 = Transcription.createNote(57, new Rational(2, 4), new Rational(1, 2));
//		Note voice2n2 = Transcription.createNote(53, new Rational(4, 4), new Rational(1, 2));
		Note voice2n3 = Transcription.createNote(67, new Rational(5, 4), new Rational(1, 4));
		Note voice2n4a = Transcription.createNote(43, new Rational(6, 4), new Rational(1, 2)); // to be removed (type (ii))
		Note voice2n4b = Transcription.createNote(57, new Rational(6, 4), new Rational(1, 2));
		voice2.add(voice2n0); voice2.add(voice2n1); /*voice2.add(voice2n2);*/ voice2.add(voice2n3); 
		voice2.add(voice2n4a); voice2.add(voice2n4b);
		// Voice 3
		NotationStaff staff3 = new NotationStaff(system); system.add(staff3); 
		NotationVoice voice3 = new NotationVoice(staff3); staff3.add(voice3); 
		Note voice3n0 = Transcription.createNote(43, new Rational(0, 4), new Rational(1, 2));
		Note voice3n1 = Transcription.createNote(43, new Rational(2, 4), new Rational(1, 2));
		Note voice3n2 = Transcription.createNote(53, new Rational(4, 4), new Rational(1, 2));
		Note voice3n3 = Transcription.createNote(48, new Rational(6, 4), new Rational(1, 2));
		voice3.add(voice3n0); voice3.add(voice3n1); voice3.add(voice3n2); voice3.add(voice3n3); 
		// Voice 4
		NotationStaff staff4 = new NotationStaff(system); system.add(staff4); 
		NotationVoice voice4 = new NotationVoice(staff4); staff4.add(voice4);
		Note voice4n0 = Transcription.createNote(43, new Rational(0, 4), new Rational(1, 2));
		Note voice4n1 = Transcription.createNote(43, new Rational(4, 4), new Rational(1, 1)); // to be adapted
		Note voice4n2 = Transcription.createNote(43, new Rational(6, 4), new Rational(1, 2));
		voice4.add(voice4n0); voice4.add(voice4n1); voice4.add(voice4n2);
		testManager.newTranscription = new Transcription();
		testManager.newTranscription.setPiece(piece);

		// allNotes (bwd)
		List<List<Note>> allNotes = new ArrayList<List<Note>>();
		// Chord 0
		allNotes.add(Arrays.asList(new Note[]{voice4n2}));
		allNotes.add(Arrays.asList(new Note[]{voice3n3}));
		allNotes.add(Arrays.asList(new Note[]{voice1n4}));
		allNotes.add(Arrays.asList(new Note[]{voice2n4b})); 
		allNotes.add(Arrays.asList(new Note[]{voice0n4}));
		// Chord 1
		allNotes.add(Arrays.asList(new Note[]{voice0n3}));
		// Chord 2
		allNotes.add(Arrays.asList(new Note[]{voice1n3}));
		allNotes.add(Arrays.asList(new Note[]{voice2n3}));
		// Chord 3
		allNotes.add(Arrays.asList(new Note[]{voice4n1}));
		allNotes.add(Arrays.asList(new Note[]{voice3n2}));
		allNotes.add(Arrays.asList(new Note[]{voice0n2})); 
		allNotes.add(Arrays.asList(new Note[]{voice1n2}));
		// Chord 4
		allNotes.add(Arrays.asList(new Note[]{voice3n1})); 
		allNotes.add(Arrays.asList(new Note[]{voice1n1}));
		allNotes.add(Arrays.asList(new Note[]{voice2n1}));
		allNotes.add(Arrays.asList(new Note[]{voice0n1b}));
		// Chord 5
		allNotes.add(Arrays.asList(new Note[]{voice3n0})); 
		allNotes.add(Arrays.asList(new Note[]{voice2n0}));
		allNotes.add(Arrays.asList(new Note[]{voice1n0})); 
		allNotes.add(Arrays.asList(new Note[]{voice0n0}));
		testManager.allNotes = new ArrayList<List<Note>>(allNotes);

		// Determine the variables created and/or adapted by resolveConflicts()
		// conflictIndicesExpected (bwd)
		List<List<Integer>> conflictIndicesExpected = new ArrayList<List<Integer>>(); 
		conflictIndicesExpected.add(Arrays.asList(new Integer[]{3, 0, 4, 9, 11, 13, 18}));
		conflictIndicesExpected.add(Arrays.asList(new Integer[]{8 /*, 9*/}));

		// allNetworkOutputsAdaptedExpected (bwd) 
		List<double[]> allNetworkOutputsAdaptedExpected = new ArrayList<double[]>(allNetworkOutputs);
		allNetworkOutputsAdaptedExpected.set(3, ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.0, 0.6, 0.1, 0.0}, half}))); 
		allNetworkOutputsAdaptedExpected.set(4, ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.8, 0.1, 0.1, 0.1, 0.0}, half})));   
		allNetworkOutputsAdaptedExpected.set(9, ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.6, 0.1, 0.0, 0.8, 0.1}, half})));   
		allNetworkOutputsAdaptedExpected.set(11, ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.8, 0.1, 0.0, 0.6}, quarter})));
		allNetworkOutputsAdaptedExpected.set(18, ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{0.1, 0.6, 0.1, 0.0, 0.0}, half})));

		// allPredictedVoicesExpected (bwd)
		List<List<Integer>> allPredictedVoices = new ArrayList<List<Integer>>();
		// Chord 0
		allPredictedVoices.add(Arrays.asList(new Integer[]{4, 2}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{3}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{0, 4}));
		// Chord 1
		allPredictedVoices.add(Arrays.asList(new Integer[]{0}));
		// Chord 2
		allPredictedVoices.add(Arrays.asList(new Integer[]{1}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{2}));
		// Chord 3
		allPredictedVoices.add(Arrays.asList(new Integer[]{4}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{3, 2}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{0}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1, 3}));
		// Chord 4
		allPredictedVoices.add(Arrays.asList(new Integer[]{3}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1, 0}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{2}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{0})); 
		// Chord 5
		allPredictedVoices.add(Arrays.asList(new Integer[]{3, 4}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{2}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{3}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{0}));

		List<List<Integer>> allPredictedVoicesExpected = new ArrayList<List<Integer>>(allPredictedVoices); 
		allPredictedVoicesExpected.set(0, Arrays.asList(new Integer[]{4}));
		allPredictedVoicesExpected.set(3, Arrays.asList(new Integer[]{2}));
		allPredictedVoicesExpected.set(4, Arrays.asList(new Integer[]{0}));    
		allPredictedVoicesExpected.set(9, Arrays.asList(new Integer[]{3}));    
		allPredictedVoicesExpected.set(11, Arrays.asList(new Integer[]{1}));
		allPredictedVoicesExpected.set(13, Arrays.asList(new Integer[]{1}));
		allPredictedVoicesExpected.set(18, Arrays.asList(new Integer[]{1}));

		// allVoiceLabelsExpected (fwd)
		List<List<Double>> allVoiceLabels = new ArrayList<List<Double>>();
		// Chord 5
		allVoiceLabels.add(Transcription.combineLabels(Transcription.VOICE_3, Transcription.VOICE_4));
		allVoiceLabels.add(Transcription.VOICE_2);
		allVoiceLabels.add(Transcription.VOICE_3);
		allVoiceLabels.add(Transcription.VOICE_0);
		// Chord 4
		allVoiceLabels.add(Transcription.VOICE_3);
		allVoiceLabels.add(Transcription.combineLabels(Transcription.VOICE_1, Transcription.VOICE_0)); 
		allVoiceLabels.add(Transcription.VOICE_2);
		allVoiceLabels.add(Transcription.VOICE_0);
		// Chord 3
		allVoiceLabels.add(Transcription.VOICE_4);
		allVoiceLabels.add(Transcription.combineLabels(Transcription.VOICE_3, Transcription.VOICE_2));
		allVoiceLabels.add(Transcription.VOICE_0);
		allVoiceLabels.add(Transcription.combineLabels(Transcription.VOICE_1, Transcription.VOICE_3)); 
		// Chord 2
		allVoiceLabels.add(Transcription.VOICE_1);
		allVoiceLabels.add(Transcription.VOICE_2);
		// Chord 1
		allVoiceLabels.add(Transcription.VOICE_0);
		// Chord 0
		allVoiceLabels.add(Transcription.combineLabels(Transcription.VOICE_4, Transcription.VOICE_2)); 
		allVoiceLabels.add(Transcription.VOICE_3);
		allVoiceLabels.add(Transcription.VOICE_1);
		allVoiceLabels.add(Transcription.VOICE_1);
		allVoiceLabels.add(Transcription.combineLabels(Transcription.VOICE_0, Transcription.VOICE_4)); 

		List<List<Double>> allVoiceLabelsExpected = new ArrayList<List<Double>>(allVoiceLabels);
		allVoiceLabelsExpected.set(15, Transcription.VOICE_4);
		allVoiceLabelsExpected.set(18, Transcription.VOICE_2); 
		allVoiceLabelsExpected.set(19, Transcription.VOICE_0);   
		allVoiceLabelsExpected.set(9, Transcription.VOICE_3);    
		allVoiceLabelsExpected.set(11, Transcription.VOICE_1); 
		allVoiceLabelsExpected.set(5, Transcription.VOICE_1);  
		allVoiceLabelsExpected.set(2, Transcription.VOICE_1);

		// allPredictedDurationsExpected (bwd)
		List<Rational[]> allPredictedDurationsExpected = new ArrayList<Rational[]>(); 
		// Chord 0
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		// Chord 1
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 8)});
		// Chord 2
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 4)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 4)});
		// Chord 3
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 4)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 4)});
		// Chord 4
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		// Chord 5
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});
		allPredictedDurationsExpected.add(new Rational[]{new Rational(1, 2)});

		// allDurationLabelsExpected (fwd)
		List<List<Double>> allDurationLabelsExpected = new ArrayList<List<Double>>();
		List<Double> eighthLabel = Transcription.EIGHTH; // trp dur
//		List<Double> eighthLabel = Transcription.createDurationLabel(4*3); // trp dur
		List<Double> quarterLabel = Transcription.QUARTER; // trp dur
//		List<Double> quarterLabel = Transcription.createDurationLabel(8*3); // trp dur
		List<Double> halfLabel = Transcription.HALF; // trp dur
//		List<Double> halfLabel = Transcription.createDurationLabel(16*3); // trp dur
		// Chord 5
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		// Chord 4
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		// Chord 3
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(quarterLabel);
		allDurationLabelsExpected.add(quarterLabel);
		// Chord 2
		allDurationLabelsExpected.add(quarterLabel);
		allDurationLabelsExpected.add(quarterLabel);
		// Chord 1
		allDurationLabelsExpected.add(eighthLabel);
		// Chord 0
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);
		allDurationLabelsExpected.add(halfLabel);

		// allVoicesCoDNotesExpected (fwd)
		List<Integer[]> allVoicesCoDNotesExpected = new ArrayList<Integer[]>();
		// Chord 5
		allVoicesCoDNotesExpected.add(new Integer[]{4, 3});
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		// Chord 4
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		// Chord 3
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		// Chord 2
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		// Chord 1
		allVoicesCoDNotesExpected.add(null);
		// Chord 0
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);
		allVoicesCoDNotesExpected.add(null);

		// pieceExpected
		Piece pieceExpected = new Piece();
		NotationSystem adaptedSystem = pieceExpected.createNotationSystem();
		// Voice 0
		NotationStaff adaptedStaff0 = new NotationStaff(adaptedSystem); adaptedSystem.add(adaptedStaff0);
		NotationVoice adaptedVoice0 = new NotationVoice(adaptedStaff0); adaptedStaff0.add(adaptedVoice0);
		adaptedVoice0.add(voice0n0); /*adaptedVoice0.add(voice0n1a);*/ adaptedVoice0.add(voice0n1b); 
		adaptedVoice0.add(voice0n2); adaptedVoice0.add(voice0n3); adaptedVoice0.add(voice0n4);
		// Voice 1
		NotationStaff adaptedStaff1 = new NotationStaff(adaptedSystem); adaptedSystem.add(adaptedStaff1); 
		NotationVoice adaptedVoice1 = new NotationVoice(adaptedStaff1); adaptedStaff1.add(adaptedVoice1);
		adaptedVoice1.add(voice1n0); adaptedVoice1.add(voice1n1); adaptedVoice1.add(voice1n2); 
		adaptedVoice1.add(voice1n3); adaptedVoice1.add(voice1n4); 
		// Voice 2
//		Note voice2n2Adapted = Transcription.createNote(53, new Rational(4, 4), new Rational(1, 4));
		NotationStaff adaptedStaff2 = new NotationStaff(adaptedSystem); adaptedSystem.add(adaptedStaff2); 
		NotationVoice adaptedVoice2 = new NotationVoice(adaptedStaff2); adaptedStaff2.add(adaptedVoice2);
		adaptedVoice2.add(voice2n0); adaptedVoice2.add(voice2n1); /*adaptedVoice2.add(voice2n2Adapted);*/ 
		adaptedVoice2.add(voice2n3); /*adaptedVoice2.add(voice2n4a);*/ adaptedVoice2.add(voice2n4b);
		// Voice 3
//		Note voice3n2Adapted = Transcription.createNote(53, new Rational(4, 4), new Rational(1, 4));
		NotationStaff adaptedStaff3 = new NotationStaff(adaptedSystem); adaptedSystem.add(adaptedStaff3); 
		NotationVoice adaptedVoice3 = new NotationVoice(adaptedStaff3); adaptedStaff3.add(adaptedVoice3);
		adaptedVoice3.add(voice3n0); adaptedVoice3.add(voice3n1); adaptedVoice3.add(voice3n2/*Adapted*/); 
		adaptedVoice3.add(voice3n3);  
		// Voice 4
		Note voice4n1Adapted = Transcription.createNote(43, new Rational(4, 4), new Rational(1, 2));
		NotationStaff adaptedStaff4 = new NotationStaff(adaptedSystem); adaptedSystem.add(adaptedStaff4); 
		NotationVoice adaptedVoice4 = new NotationVoice(adaptedStaff4); adaptedStaff4.add(adaptedVoice4);
		adaptedVoice4.add(voice4n0); adaptedVoice4.add(voice4n1Adapted); adaptedVoice4.add(voice4n2);

		// allNotesExpected (bwd)
		List<List<Note>> allNotesExpected = new ArrayList<List<Note>>(allNotes);
		allNotesExpected.set(8, Arrays.asList(new Note[]{voice4n1Adapted}));
//		allNotesExpected.set(9, Arrays.asList(new Note[]{voice3n2Adapted}));

		// Calculate actual
		testManager.backwardsMapping = FeatureGenerator.getBackwardsMapping(testManager.tablature.getNumberOfNotesPerChord());
		for (int i = 0; i < testManager.backwardsMapping.size(); i++) {
			int noteIndex = testManager.backwardsMapping.get(i);
				testManager.resolveConflicts(allNetworkOutputs.get(i), noteIndex);
		}		
		List<List<Integer>> conflictIndicesActual = testManager.conflictIndices; 		
		List<double[]> allNetworkOutputsAdaptedActual = testManager.allNetworkOutputsAdapted;
		List<List<Integer>> allPredictedVoicesActual = testManager.allPredictedVoices;
		List<List<Double>> allVoiceLabelsActual = testManager.allVoiceLabels;
		List<Rational[]> allPredictedDurationsActual = testManager.allPredictedDurations;
		List<List<Double>> allDurationLabelsActual = testManager.allDurationLabels;
		List<Integer[]> allVoicesCoDNotesActual = testManager.allVoicesCoDNotes;
		Piece pieceActual = testManager.newTranscription.getPiece();
		List<List<Note>> allNotesActual = testManager.allNotes;
		System.out.println(testManager.conflictsRecord);

		// Assert equality
		// conflictIndices
		assertEquals(conflictIndicesExpected.size(), conflictIndicesActual.size());
		for (int i = 0; i < conflictIndicesExpected.size(); i++) { 
			assertEquals(conflictIndicesExpected.get(i).size(), conflictIndicesActual.get(i).size());
			for (int j = 0; j < conflictIndicesExpected.get(i).size(); j++) {
				assertEquals(conflictIndicesExpected.get(i).get(j), conflictIndicesActual.get(i).get(j));
			}
		}

		// allNetworkOutputsAdapted
		assertEquals(allNetworkOutputsAdaptedExpected.size(), allNetworkOutputsAdaptedActual.size());
		for (int i = 0; i < allNetworkOutputsAdaptedExpected.size(); i++) {
			assertEquals(allNetworkOutputsAdaptedExpected.get(i).length, allNetworkOutputsAdaptedActual.get(i).length);
			for (int j = 0; j < allNetworkOutputsAdaptedExpected.get(i).length; j++) {
				assertEquals(allNetworkOutputsAdaptedExpected.get(i)[j], allNetworkOutputsAdaptedActual.get(i)[j]);
			}
		}

		// allPredictedVoices
		assertEquals(allPredictedVoicesExpected.size(), allPredictedVoicesActual.size());
		for (int i = 0; i < allPredictedVoicesExpected.size(); i++) {
			assertEquals(allPredictedVoicesExpected.get(i).size(), allPredictedVoicesActual.get(i).size());
			for (int j = 0; j < allPredictedVoicesExpected.get(i).size(); j++) {
				assertEquals(allPredictedVoicesExpected.get(i).get(j), allPredictedVoicesActual.get(i).get(j));
			}
		}

		// allVoiceLabels
		assertEquals(allVoiceLabelsExpected.size(), allVoiceLabelsActual.size());
		for (int i = 0; i < allVoiceLabelsExpected.size(); i++) {
			assertEquals(allVoiceLabelsExpected.get(i).size(),allVoiceLabelsActual.get(i).size());
			for (int j = 0; j < allVoiceLabelsExpected.get(i).size(); j++) {
				assertEquals(allVoiceLabelsExpected.get(i).get(j), allVoiceLabelsActual.get(i).get(j));
			}
		}

		// allPredictedDurations
		assertEquals(allPredictedDurationsExpected.size(), allPredictedDurationsActual.size());
		for (int i = 0; i < allPredictedDurationsExpected.size(); i++) {
			assertEquals(allPredictedDurationsExpected.get(i).length, allPredictedDurationsActual.get(i).length);
			for (int j = 0; j < allPredictedDurationsExpected.get(i).length; j++) {
				assertEquals(allPredictedDurationsExpected.get(i)[j], allPredictedDurationsActual.get(i)[j]);
			}
		}

		// allDurationLabels
		assertEquals(allDurationLabelsExpected.size(), allDurationLabelsActual.size());
		for (int i = 0; i < allDurationLabelsExpected.size(); i++) {
			assertEquals(allDurationLabelsExpected.get(i).size(),allDurationLabelsActual.get(i).size());
			for (int j = 0; j < allDurationLabelsExpected.get(i).size(); j++) {
				assertEquals(allDurationLabelsExpected.get(i).get(j), allDurationLabelsActual.get(i).get(j));
			}
		}

		// allVoicesCoDNotes
		assertEquals(allVoicesCoDNotesExpected.size(), allVoicesCoDNotesActual.size());
		for (int i = 0; i < allVoicesCoDNotesExpected.size(); i++) {
			if (allVoicesCoDNotesExpected.get(i) == null) {
				assertEquals(allVoicesCoDNotesExpected.get(i), allVoicesCoDNotesActual.get(i));
			}
			else {
				assertEquals(allVoicesCoDNotesExpected.get(i).length,allVoicesCoDNotesActual.get(i).length);
				for (int j = 0; j < allVoicesCoDNotesExpected.get(i).length; j++) {
					assertEquals(allVoicesCoDNotesExpected.get(i)[j], allVoicesCoDNotesActual.get(i)[j]);
				}
			}
		}

		// piece
//		// NB: type (ii) and (iv) conflicts imply changes to a Note. In the case of type (ii) conflicts, that Note has
//		// already been added to the Transcription at the moment of calling resolveConflicts(); in the case of type (iv)
//		// conflicts, however, it still must be added (this happens inside TestManager.addNote()). The duration of Notes 
//		// /*voice2n2, voice3n2, and*/ voice4n1 (all involved in type (i) conflicts) in actualPiece must thus be adapted 
//		// manually
//		// voice2n2
//		ScoreNote newScoreNote1 = new ScoreNote(new ScorePitch(voice2n2.getMidiPitch()), voice2n2.getMetricTime(), 
//			new Rational(1, 4));
//		pieceActual.getScore().get(2).get(0).get(2).get(0).setScoreNote(newScoreNote1);
//		// voice3n2
//		ScoreNote newScoreNote2 = new ScoreNote(new ScorePitch(voice3n2.getMidiPitch()), voice3n2.getMetricTime(), 
//			new Rational(1, 4));
//		pieceActual.getScore().get(3).get(0).get(2).get(0).setScoreNote(newScoreNote2);
//		// voice4n1

		// NB: In the case of type (i) or (iii) conflicts, at the moment of calling resolveconflicts(), the note 
		// must still  be added to the Transcription (this happens inside TestManager.addNote()). The duration of  
		// note voice4n1 (involved in a type (i) duration conflict) must thus be adapted manually
		ScoreNote newScoreNote = new ScoreNote(new ScorePitch(voice4n1.getMidiPitch()), voice4n1.getMetricTime(), 
			new Rational(1, 2));
		pieceActual.getScore().get(4).get(0).get(1).get(0).setScoreNote(newScoreNote);
		assertEquals(pieceExpected.getScore().size(), pieceActual.getScore().size());
		// For each NotationStaff at index i
		for (int i = 0; i < pieceExpected.getScore().size(); i++) {
			assertEquals(pieceExpected.getScore().get(i).size(), pieceActual.getScore().get(i).size());
			// For each NotationVoice at index j
			for (int j = 0; j < pieceExpected.getScore().get(i).size(); j++) {
				assertEquals(pieceExpected.getScore().get(i).get(j).size(), pieceActual.getScore().get(i).get(j).size());
				// For each NotationChord at index k
				for (int k = 0; k < pieceExpected.getScore().get(i).get(j).size(); k++) {	
					assertEquals(pieceExpected.getScore().get(i).get(j).get(k).size(), pieceActual.getScore().get(i).get(j).get(k).size());
					// For each Note at index l
					for (int l = 0; l < pieceExpected.getScore().get(i).get(j).get(k).size(); l++) {
//						assertEquals(pieceExpected.getScore().get(i).get(j).get(k).get(l), pieceActual.getScore().get(i).get(j).get(k).get(l));
						// OR if assertEquals(expected.get(i).get(j), actual.get(i).get(j).get(k) does not work because the Notes 
						// are not the same objects: check that pitch, metricTime, and metricDuration are the same
						assertEquals(pieceExpected.getScore().get(i).get(j).get(k).get(l).getMidiPitch(), 
							pieceActual.getScore().get(i).get(j).get(k).get(l).getMidiPitch());
						assertEquals(pieceExpected.getScore().get(i).get(j).get(k).get(l).getMetricTime(), 
							pieceActual.getScore().get(i).get(j).get(k).get(l).getMetricTime());
						assertEquals(pieceExpected.getScore().get(i).get(j).get(k).get(l).getMetricDuration(), 
							pieceActual.getScore().get(i).get(j).get(k).get(l).getMetricDuration());		
					}
				}		
			}
		}

		// allNotes
		assertEquals(allNotesExpected.size(), allNotesActual.size());
		for (int i = 0; i < allNotesExpected.size(); i++) {
			assertEquals(allNotesExpected.get(i).size(), allNotesActual.get(i).size());
			for (int j = 0; j < allNotesExpected.get(i).size(); j++) {
				assertEquals(allNotesExpected.get(i).get(j).getMidiPitch(), allNotesActual.get(i).get(j).getMidiPitch());
				assertEquals(allNotesExpected.get(i).get(j).getMetricTime(), allNotesActual.get(i).get(j).getMetricTime());
				assertEquals(allNotesExpected.get(i).get(j).getMetricDuration(), allNotesActual.get(i).get(j).getMetricDuration());
			}
		}
	}


	public void testResolveConflictsNonTab() {
		// Conflicts:
		// Chord 1, notes 4-5: type (i) conflict: 
		// (i)  note 5 has the same predicted voice as (sustained) note 0 (voice 3)
		//
		// Chord 1, notes 4-5: type (i) conflict: 
		// (i)  note 5, after its predicted label has been adapted, now has the same predicted voice as (sustained) 
		//      note 2 (voice 1)
		//
		// Chord 3, notes 10-12: type (i) conflict: 
		// (i)  note 12 has the same predicted voice as (sustained) note 7 (voice 2)
		//
		// Chord 3, notes 10-12: type (i) conflict: 
		// (i)  note 12, after its predicted label has been adapted, now has the same predicted voice as note 10 
		//      (voice 3)
		//
		// Chord 3, notes 10-12: type (i) conflict: 
		// (i)  note 12, after its predicted label has been adapted, now has the same predicted voice as note 11 
		//      (voice 1)

		TestManager testManager = new TestManager();		
//		testManager.setIsTablatureCase(false); // TODO some of these are superfluous here
//		testManager.setUseTablatureInformation(false);
//		testManager.setModelDuration(false);
//		testManager.setAllowNonMonophonic(false);
//		testManager.setIsNewModel(true);//2016
		testManager.allVoiceLabels = new ArrayList<List<Double>>();
		testManager.allPredictedVoices = new ArrayList<List<Integer>>();
		testManager.conflictsRecord = "";
		testManager.conflictIndices = new ArrayList<List<Integer>>();
		testManager.conflictIndices.add(new ArrayList<Integer>());
		testManager.conflictIndices.add(new ArrayList<Integer>());
//		testManager.outputEvaluator = new OutputEvaluator();
//		testManager.dataConverter = new DataConverterTab();
//		testManager.setReversePiece(false);
//		testManager.setModelBackward(false);
		
//		Map<String, Double> mp = new LinkedHashMap<String, Double>();
//		mp.put(Runner.APPL_TO_NEW_DATA, 0.0);
//		Runner.setModelParams(mp);
//		Dataset ds = new Dataset(DatasetID.TESTPIECE_SET_NON_TAB);
//		ds.populateDataset(mp, null, Runner.testPaths);
//		Runner.setDataset(ds);

		// Determine the variables needed for resolveConflicts()		
		// modelParameters
		Map<String, Double> modelParameters = new HashMap<String, Double>();
//		modelParameters.put(Dataset.DATASET_ID, (double) DatasetID.TESTPIECE_SET_NON_TAB.getIntRep());
		modelParameters.put(Runner.MODEL, (double) Model.N.getIntRep());
		modelParameters.put(Runner.PROC_MODE, (double) ProcessingMode.FWD.getIntRep());
//		modelParameters.put(Runner.IMPLEMENTATION, (double) Implementation.PHD.getIntRep());
		modelParameters.put(Runner.SNU, 0.0);
		modelParameters.put(Runner.DEV_THRESHOLD, 0.05);	
//		modelParameters.put(Runner.HIGHEST_NUM_VOICES, 5.0);
//		modelParameters.put(Runner.TAB_DATA, 0.0);
//		modelParameters.put(Runner.MODEL_DURATION, 0.0);
//		modelParameters.put(Runner.MODEL_BACKWARD, 0.0);
//		modelParameters.put(Runner.MODEL_BIDIR, 0.0);
		modelParameters.put(Runner.MODEL_DURATION_AGAIN, 0.0);
//		modelParameters.put(Runner.GIVE_FIRST, 0.0);
//		modelParameters.put(Runner.DEPLOY_TRAINED_USER_MODEL, 0.0);
		Runner.setModelParams(modelParameters);

		// ds
		Dataset ds = new Dataset(Dataset.TEST);
		ds.populateDataset(null, testPaths, false);
		Runner.setDataset(ds);
		
		// basicNoteProperties
		testManager.groundTruthTranscription = 
			new Transcription(new File(Runner.midiPath + "test/" + "test_resolve_conflicts_non_tab.mid"), null);
		testManager.basicNoteProperties = testManager.groundTruthTranscription.getBasicNoteProperties();
		testManager.meterInfo = testManager.groundTruthTranscription.getMeterInfo();

		// Pre-set the necessary lists with dummy values
		for (int i = 0; i < testManager.basicNoteProperties.length; i++) {
			testManager.allVoiceLabels.add(null);
		}

//		// allMetricPositions
//		testManager.allMetricPositions = new ArrayList<Rational[]>();
//		for (int i = 0; i < testManager.basicNoteProperties.length; i++) {
//			Rational currMetricTime = 
//				new Rational(testManager.basicNoteProperties[i][Transcription.ONSET_TIME_NUMER],
//				testManager.basicNoteProperties[i][Transcription.ONSET_TIME_DENOM]);
////			metricTimes.add(currMetricTime);
//			testManager.allMetricPositions.add(Tablature.getMetricPosition(currMetricTime, 
//				testManager.groundTruthTranscription.getMeterInfo()));
//		}

		// allNetworkOutputs
		List<double[]> allNetworkOutputs = new ArrayList<double[]>();
		allNetworkOutputs.add(new double[]{0.1, 0.1, 0.6, 0.8, 0.0}); // voice 3
		allNetworkOutputs.add(new double[]{0.1, 0.2, 0.8, 0.1, 0.6}); // voice 2
		allNetworkOutputs.add(new double[]{0.1, 0.8, 0.1, 0.6, 0.0}); // voice 1
		allNetworkOutputs.add(new double[]{0.8, 0.1, 0.1, 0.1, 0.6}); // voice 0
		//
		allNetworkOutputs.add(new double[]{0.1, 0.2, 0.8, 0.1, 0.6}); // voice 2
		allNetworkOutputs.add(new double[]{0.4, 0.6, 0.1, 0.8, 0.1}); // voices 3 --> voice 1 --> voice 0
		//
		allNetworkOutputs.add(new double[]{0.1, 0.1, 0.6, 0.8, 0.0}); // voice 3
		allNetworkOutputs.add(new double[]{0.1, 0.2, 0.8, 0.1, 0.6}); // voice 2
		allNetworkOutputs.add(new double[]{0.1, 0.8, 0.1, 0.6, 0.0}); // voice 1
		allNetworkOutputs.add(new double[]{0.8, 0.1, 0.1, 0.1, 0.6}); // voice 0
		//
		allNetworkOutputs.add(new double[]{0.1, 0.1, 0.6, 0.8, 0.0}); // voice 3
		allNetworkOutputs.add(new double[]{0.1, 0.8, 0.1, 0.6, 0.0}); // voice 1 
		allNetworkOutputs.add(new double[]{0.2, 0.4, 0.8, 0.6, 0.0}); // voice 2 --> voice 3 --> voice 1 --> voice 0
		//
		allNetworkOutputs.add(new double[]{0.1, 0.1, 0.6, 0.8, 0.0}); // voice 3
		//
		allNetworkOutputs.add(new double[]{0.8, 0.1, 0.1, 0.6, 0.0}); // voice 0
		testManager.allNetworkOutputs = new ArrayList<double[]>(allNetworkOutputs);

		// allNetworkOutputsAdapted (in its initial state, in which it is the same as allNetworkOutputs)
		testManager.allNetworkOutputsAdapted = new ArrayList<double[]>(allNetworkOutputs);

		// piece
		Piece piece = new Piece();
		NotationSystem system = piece.createNotationSystem();
		// Voice 0
		NotationStaff staff0 = new NotationStaff(system); system.add(staff0);
		NotationVoice voice0 = new NotationVoice(staff0); staff0.add(voice0);
		Note voice0n0 = Transcription.createNote(67, new Rational(0, 4), new Rational(1, 2));
		Note voice0n1 = Transcription.createNote(67, new Rational(2, 4), new Rational(1, 2));
		Note voice0n2 = Transcription.createNote(67, new Rational(4, 4), new Rational(1, 2));
		Note voice0n3 = Transcription.createNote(67, new Rational(6, 4), new Rational(3, 8));
		Note voice0n4 = Transcription.createNote(67, new Rational(15, 8), new Rational(1, 8));
		voice0.add(voice0n0); voice0.add(voice0n1); voice0.add(voice0n2); voice0.add(voice0n3); voice0.add(voice0n4);
		// Voice 1
		NotationStaff staff1 = new NotationStaff(system); system.add(staff1); 
		NotationVoice voice1 = new NotationVoice(staff1); staff1.add(voice1); 
		Note voice1n0 = Transcription.createNote(57, new Rational(0, 4), new Rational(1, 1));
		Note voice1n1 = Transcription.createNote(57, new Rational(4, 4), new Rational(1, 2));
		Note voice1n2 = Transcription.createNote(57, new Rational(6, 4), new Rational(1, 2));
		voice1.add(voice1n0); voice1.add(voice1n1); voice1.add(voice1n2);
		// Voice 2
		NotationStaff staff2 = new NotationStaff(system); system.add(staff2); 
		NotationVoice voice2 = new NotationVoice(staff2); staff2.add(voice2);
		Note voice2n0 = Transcription.createNote(53, new Rational(0, 4), new Rational(1, 2));
		Note voice2n1 = Transcription.createNote(53, new Rational(2, 4), new Rational(1, 2));
		Note voice2n2 = Transcription.createNote(53, new Rational(4, 4), new Rational(1, 1));
		voice2.add(voice2n0); voice2.add(voice2n1); voice2.add(voice2n2);
		// Voice 3
		NotationStaff staff3 = new NotationStaff(system); system.add(staff3); 
		NotationVoice voice3 = new NotationVoice(staff3); staff3.add(voice3); 
		Note voice3n0 = Transcription.createNote(43, new Rational(0, 4), new Rational(1, 1));
		Note voice3n1 = Transcription.createNote(43, new Rational(4, 4), new Rational(1, 2));
		Note voice3n2 = Transcription.createNote(43, new Rational(6, 4), new Rational(1, 4));
		Note voice3n3 = Transcription.createNote(43, new Rational(7, 4), new Rational(1, 4));
		voice3.add(voice3n0); voice3.add(voice3n1); voice3.add(voice3n2); voice3.add(voice3n3);
		testManager.newTranscription = new Transcription();
		testManager.newTranscription.setPiece(piece);

		// allNotes
		List<List<Note>> allNotes = new ArrayList<List<Note>>();
		allNotes.add(Arrays.asList(new Note[]{voice3n0})); 
		allNotes.add(Arrays.asList(new Note[]{voice2n0}));
		allNotes.add(Arrays.asList(new Note[]{voice1n0})); 
		allNotes.add(Arrays.asList(new Note[]{voice0n0}));
		//
		allNotes.add(Arrays.asList(new Note[]{voice2n1})); 
		allNotes.add(Arrays.asList(new Note[]{voice0n1}));
		//
		allNotes.add(Arrays.asList(new Note[]{voice3n1}));
		allNotes.add(Arrays.asList(new Note[]{voice2n2})); 
		allNotes.add(Arrays.asList(new Note[]{voice1n1})); 
		allNotes.add(Arrays.asList(new Note[]{voice0n2}));
		// 
		allNotes.add(Arrays.asList(new Note[]{voice3n2}));
		allNotes.add(Arrays.asList(new Note[]{voice1n2})); 
		allNotes.add(Arrays.asList(new Note[]{voice0n3}));
		//
		allNotes.add(Arrays.asList(new Note[]{voice3n3}));
		//
		allNotes.add(Arrays.asList(new Note[]{voice0n4}));
		testManager.allNotes = new ArrayList<List<Note>>(allNotes);

		// Determine the variables created and/or adapted by resolveConflicts()
		// conflictIndicesExpected
		List<List<Integer>> conflictIndicesExpected = new ArrayList<List<Integer>>();
		conflictIndicesExpected.add(Arrays.asList(new Integer[]{5, 12}));
		conflictIndicesExpected.add(new ArrayList<Integer>());

		// allNetworkOutputsAdaptedExpected (network outputs are adapted only in case of type (i) conflicts)
		List<double[]> allNetworkOutputsAdaptedExpected = new ArrayList<double[]>(allNetworkOutputs);
		allNetworkOutputsAdaptedExpected.set(5, new double[]{0.4, 0.0, 0.1, 0.0, 0.1});
		allNetworkOutputsAdaptedExpected.set(12, new double[]{0.2, 0.0, 0.0, 0.0, 0.0});

		// allPredictedVoicesExpected (in case of each conflict, a voice is adapted)
		List<List<Integer>> allPredictedVoices = new ArrayList<List<Integer>>();
		allPredictedVoices.add(Arrays.asList(new Integer[]{3}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{2}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{0}));
		//
		allPredictedVoices.add(Arrays.asList(new Integer[]{2}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{3}));
		//
		allPredictedVoices.add(Arrays.asList(new Integer[]{3}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{2}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{0}));
		//
		allPredictedVoices.add(Arrays.asList(new Integer[]{3}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{2}));
		//
		allPredictedVoices.add(Arrays.asList(new Integer[]{3}));
		//
		allPredictedVoices.add(Arrays.asList(new Integer[]{0}));

		List<List<Integer>> allPredictedVoicesExpected = new ArrayList<List<Integer>>(allPredictedVoices);
		allPredictedVoicesExpected.set(5, Arrays.asList(new Integer[]{0}));
		allPredictedVoicesExpected.set(12, Arrays.asList(new Integer[]{0}));

		// allVoiceLabelsExpected (in case of each conflict, a voice label is adapted)
		List<List<Double>> allVoiceLabels = new ArrayList<List<Double>>();
		allVoiceLabels.add(Transcription.VOICE_3); allVoiceLabels.add(Transcription.VOICE_2);
		allVoiceLabels.add(Transcription.VOICE_1); allVoiceLabels.add(Transcription.VOICE_0);
		//
		allVoiceLabels.add(Transcription.VOICE_2); allVoiceLabels.add(Transcription.VOICE_3);
		//
		allVoiceLabels.add(Transcription.VOICE_3); allVoiceLabels.add(Transcription.VOICE_2);
		allVoiceLabels.add(Transcription.VOICE_1); allVoiceLabels.add(Transcription.VOICE_0);
		//
		allVoiceLabels.add(Transcription.VOICE_3); allVoiceLabels.add(Transcription.VOICE_1);
		allVoiceLabels.add(Transcription.VOICE_2);
		//
		allVoiceLabels.add(Transcription.VOICE_3);
		//
		allVoiceLabels.add(Transcription.VOICE_0);

		List<List<Double>> allVoiceLabelsExpected = new ArrayList<List<Double>>(allVoiceLabels);
		allVoiceLabelsExpected.set(5, Transcription.VOICE_0);
		allVoiceLabelsExpected.set(12, Transcription.VOICE_0);

		// pieceExpected (notes are removed from the piece only in case of type (ii) conflicts)
		Piece pieceExpected = new Piece();
		pieceExpected.setScore(system);

		// allNotesExpected
		List<List<Note>> allNotesExpected = new ArrayList<List<Note>>(allNotes);

		// Calculate actual
		for (int i = 0; i < allNetworkOutputs.size(); i++) {
			testManager.resolveConflicts(allNetworkOutputs.get(i), i);
		}		
		List<List<Integer>> conflictIndicesActual = testManager.conflictIndices; 
		List<double[]> allNetworkOutputsAdaptedActual = testManager.allNetworkOutputsAdapted;
		List<List<Integer>> allPredictedVoicesActual = testManager.allPredictedVoices;
		List<List<Double>> allVoiceLabelsActual = testManager.allVoiceLabels;
		Piece pieceActual = testManager.newTranscription.getPiece();
		List<List<Note>> allNotesActual = testManager.allNotes;
		System.out.println(testManager.conflictsRecord);

		// Assert equality
		// conflictIndices
		assertEquals(conflictIndicesExpected.size(), conflictIndicesActual.size());
		for (int i = 0; i < conflictIndicesExpected.size(); i++) { 
			assertEquals(conflictIndicesExpected.get(i).size(), conflictIndicesActual.get(i).size());
			for (int j = 0; j < conflictIndicesExpected.get(i).size(); j++) {
				assertEquals(conflictIndicesExpected.get(i).get(j), conflictIndicesActual.get(i).get(j));
			}
		}

		// allNetworkOutputsAdapted
		assertEquals(allNetworkOutputsAdaptedExpected.size(), allNetworkOutputsAdaptedActual.size());
		for (int i = 0; i < allNetworkOutputsAdaptedExpected.size(); i++) {
			assertEquals(allNetworkOutputsAdaptedExpected.get(i).length, allNetworkOutputsAdaptedActual.get(i).length);
			for (int j = 0; j < allNetworkOutputsAdaptedExpected.get(i).length; j++) {
				assertEquals(allNetworkOutputsAdaptedExpected.get(i)[j], allNetworkOutputsAdaptedActual.get(i)[j]);
			}
		}

		// allPredictedVoices
		assertEquals(allPredictedVoicesExpected.size(), allPredictedVoicesActual.size());
		for (int i = 0; i < allPredictedVoicesExpected.size(); i++) {
			assertEquals(allPredictedVoicesExpected.get(i).size(), allPredictedVoicesActual.get(i).size());
			for (int j = 0; j < allPredictedVoicesExpected.get(i).size(); j++) {
				assertEquals(allPredictedVoicesExpected.get(i).get(j), allPredictedVoicesActual.get(i).get(j));
			}
		}

		// allVoiceLabels
		assertEquals(allVoiceLabelsExpected.size(), allVoiceLabelsActual.size());
		for (int i = 0; i < allVoiceLabelsExpected.size(); i++) {
			assertEquals(allVoiceLabelsExpected.get(i).size(),allVoiceLabelsActual.get(i).size());
			for (int j = 0; j < allVoiceLabelsExpected.get(i).size(); j++) {
				assertEquals(allVoiceLabelsExpected.get(i).get(j), allVoiceLabelsActual.get(i).get(j));
			}
		}

		// piece
		assertEquals(pieceExpected.getScore().size(), pieceActual.getScore().size());
		// For each NotationStaff at index i
		for (int i = 0; i < pieceExpected.getScore().size(); i++) {
			assertEquals(pieceExpected.getScore().get(i).size(), pieceActual.getScore().get(i).size());
			// For each NotationVoice at index j
			for (int j = 0; j < pieceExpected.getScore().get(i).size(); j++) {
				assertEquals(pieceExpected.getScore().get(i).get(j).size(), pieceActual.getScore().get(i).get(j).size());
				// For each NotationChord at index k
				for (int k = 0; k < pieceExpected.getScore().get(i).get(j).size(); k++) {	
					assertEquals(pieceExpected.getScore().get(i).get(j).get(k).size(), pieceActual.getScore().get(i).get(j).get(k).size());
					// For each Note at index l
					for (int l = 0; l < pieceExpected.getScore().get(i).get(j).get(k).size(); l++) {
						assertEquals(pieceExpected.getScore().get(i).get(j).get(k).get(l), pieceActual.getScore().get(i).get(j).get(k).get(l));
						// OR if assertEquals(expected.get(i).get(j), actual.get(i).get(j).get(k) does not work because the Notes 
						// are not the same objects: check that pitch, metricTime, and metricDuration are the same
//						assertEquals(adaptedPiece.getScore().get(i).get(j).get(k).get(l).getMidiPitch(), 
//							pieceActual.getScore().get(i).get(j).get(k).get(l).getMidiPitch());
//						assertEquals(adaptedPiece.getScore().get(i).get(j).get(k).get(l).getMetricTime(), 
//							pieceActual.getScore().get(i).get(j).get(k).get(l).getMetricTime());
//						assertEquals(adaptedPiece.getScore().get(i).get(j).get(k).get(l).getMetricDuration(), 
//							pieceActual.getScore().get(i).get(j).get(k).get(l).getMetricDuration());		
					}
				}		
			}
		}

		// allNotes
		assertEquals(allNotesExpected.size(), allNotesActual.size());
		for (int i = 0; i < allNotesExpected.size(); i++) {
			assertEquals(allNotesExpected.get(i).size(), allNotesActual.get(i).size());
			for (int j = 0; j < allNotesExpected.get(i).size(); j++) {
				assertEquals(allNotesExpected.get(i).get(j).getMidiPitch(), allNotesActual.get(i).get(j).getMidiPitch());
				assertEquals(allNotesExpected.get(i).get(j).getMetricTime(), allNotesActual.get(i).get(j).getMetricTime());
				assertEquals(allNotesExpected.get(i).get(j).getMetricDuration(), allNotesActual.get(i).get(j).getMetricDuration());
			}
		}
	}


	public void testResolveConflictsNonTabBwd() {
		// Conflicts:
		// Chord 2, notes 2-4 (10-12 fwd): type (i) conflict: 
		// (i)  note 4 has the same predicted voice as note 2 (voice 3)
		//
		// Chord 2, notes 2-4 (10-12 fwd): type (i) conflict: 
		// (i)  note 4, after its predicted label has been adapted, now has the same predicted voice as 
		//      note 3 (voice 1)
		//
		// Chord 3, notes 5-8 (6-9 fwd): type (i) conflict: 
		// (i)  note 6 has the same predicted voice as (interrupting) note 4 (voice 0)
		//
		// Chord 3, notes 5-8 (6-9 fwd): type (i) conflict: 
		// (i)  note 6, after its predicted label has been adapted, now has the same predicted voice as 
		//      (interrupting) note 3 (voice 1) 
		//
		// Chord 3, notes 5-8 (6-9 fwd): type (i) conflict: 
		// (i)  note 6, after its predicted label has been adapted, now has the same predicted voice as 
		//      (interrupting) note 2 (voice 3) 
		//
		// Chord 0, notes 11-14 (0-3 fwd): type (i) conflict: 
		// (i)  note 11 has the same predicted voice as (interrupting) note 10 (voice 0)
		//
		// Chord 0, notes 11-14 (0-3 fwd): type (i) conflict: 
		// (i)  note 13 has the same predicted voice as (interrupting) note 9 (voice 2)
		//
		// Chord 0, notes 11-14 (0-3 fwd): type (i) conflict: 
		// (i)  note 13, after its predicted label has been adapted, now has the same predicted voice as
		//      note 11 (voice 3)

		TestManager testManager = new TestManager();		
//		testManager.setIsTablatureCase(false); // TODO some of these are superfluous here
//		testManager.setUseTablatureInformation(false);
//		testManager.setModelDuration(false);
//		testManager.setAllowNonMonophonic(false);
//		testManager.setIsNewModel(true);//2016
		testManager.allVoiceLabels = new ArrayList<List<Double>>();
		testManager.allPredictedVoices = new ArrayList<List<Integer>>();
		testManager.conflictsRecord = "";
		testManager.conflictIndices = new ArrayList<List<Integer>>();
		testManager.conflictIndices.add(new ArrayList<Integer>());
		testManager.conflictIndices.add(new ArrayList<Integer>());
//		testManager.outputEvaluator = new OutputEvaluator();
//		testManager.dataConverter = new DataConverterTab();
//		testManager.setModelBackward(true);
		
//		Map<String, Double> mp = new LinkedHashMap<String, Double>();
//		mp.put(Runner.APPL_TO_NEW_DATA, 0.0);
//		Runner.setModelParams(mp);
//		Dataset ds = new Dataset(DatasetID.TESTPIECE_SET_NON_TAB);
//		ds.populateDataset(mp, null, Runner.testPaths);
//		Runner.setDataset(ds);

		// Determine the variables needed for resolveConflicts()		
 		// modelParameters
		Map<String, Double> modelParameters = new HashMap<String, Double>();
//		modelParameters.put(Dataset.DATASET_ID, (double) DatasetID.TESTPIECE_SET_NON_TAB.getIntRep());
		modelParameters.put(Runner.MODEL, (double) Model.N.getIntRep());
		modelParameters.put(Runner.PROC_MODE, (double) ProcessingMode. BWD.getIntRep());
//		modelParameters.put(Runner.IMPLEMENTATION, (double) Implementation.PHD.getIntRep());
		modelParameters.put(Runner.SNU, 0.0);
		modelParameters.put(Runner.DEV_THRESHOLD, 0.05);	
//		modelParameters.put(Runner.HIGHEST_NUM_VOICES, 5.0);
//		modelParameters.put(Runner.TAB_DATA, 0.0);
//		modelParameters.put(Runner.MODEL_DURATION, 0.0);
//		modelParameters.put(Runner.MODEL_BACKWARD, 1.0);
//		modelParameters.put(Runner.MODEL_BIDIR, 0.0);
		modelParameters.put(Runner.MODEL_DURATION_AGAIN, 0.0);
//		modelParameters.put(Runner.GIVE_FIRST, 0.0);
//		modelParameters.put(Runner.DEPLOY_TRAINED_USER_MODEL, 0.0);
		Runner.setModelParams(modelParameters);

		// ds
		Dataset ds = new Dataset(Dataset.TEST);
		ds.populateDataset(null, testPaths, false);
		Runner.setDataset(ds);
		
		// basicNoteProperties (fwd)
		testManager.groundTruthTranscription = 
			new Transcription(new File(Runner.midiPath+ "test/" + "test_resolve_conflicts_non_tab.mid"), null);
		testManager.basicNoteProperties = testManager.groundTruthTranscription.getBasicNoteProperties();
		testManager.meterInfo = testManager.groundTruthTranscription.getMeterInfo();
		
		// Pre-set the necessary lists with dummy values
		for (int i = 0; i < testManager.basicNoteProperties.length; i++) {
			testManager.allVoiceLabels.add(null);
		}

//		// allMetricPositions (fwd)
////		List<Rational> metricTimes = new ArrayList<Rational>();
//		testManager.allMetricPositions = new ArrayList<Rational[]>();
//		for (int i = 0; i < testManager.basicNoteProperties.length; i++) {
//			Rational currMetricTime = 
//				new Rational(testManager.basicNoteProperties[i][Transcription.ONSET_TIME_NUMER],
//				testManager.basicNoteProperties[i][Transcription.ONSET_TIME_DENOM]);
////			metricTimes.add(currMetricTime);
//			testManager.allMetricPositions.add(Tablature.getMetricPosition(currMetricTime, 
//				testManager.groundTruthTranscription.getMeterInfo()));
//		}

		// allNetworkOutputs (bwd)
		List<double[]> allNetworkOutputs = new ArrayList<double[]>();
		allNetworkOutputs.add(new double[]{0.8, 0.1, 0.6, 0.1, 0.0}); // voice 0
		//
		allNetworkOutputs.add(new double[]{0.1, 0.1, 0.6, 0.8, 0.0}); // voice 3
		//
		allNetworkOutputs.add(new double[]{0.1, 0.1, 0.6, 0.8, 0.0}); // voice 3
		allNetworkOutputs.add(new double[]{0.1, 0.8, 0.2, 0.1, 0.6}); // voice 1
		allNetworkOutputs.add(new double[]{0.2, 0.6, 0.1, 0.8, 0.0}); // voice 3 --> voice 1 --> voice 0
		//
		allNetworkOutputs.add(new double[]{0.1, 0.2, 0.6, 0.8, 0.6}); // voice 3
		allNetworkOutputs.add(new double[]{0.8, 0.6, 0.4, 0.5, 0.1}); // voice 0 --> voice 1 -> voice 3 --> voice 2
		allNetworkOutputs.add(new double[]{0.1, 0.8, 0.2, 0.1, 0.6}); // voice 1
		allNetworkOutputs.add(new double[]{0.8, 0.6, 0.1, 0.4, 0.1}); // voices 0
		//
		allNetworkOutputs.add(new double[]{0.1, 0.2, 0.8, 0.1, 0.6}); // voice 2
		allNetworkOutputs.add(new double[]{0.8, 0.1, 0.6, 0.1, 0.0}); // voice 0
		//
		allNetworkOutputs.add(new double[]{0.8, 0.1, 0.4, 0.6, 0.0}); // voice 0 --> voice 3
		allNetworkOutputs.add(new double[]{0.1, 0.2, 0.8, 0.1, 0.6}); // voice 2
		allNetworkOutputs.add(new double[]{0.1, 0.4, 0.8, 0.6, 0.0}); // voice 2 --> voice 3 --> voice 1
		allNetworkOutputs.add(new double[]{0.8, 0.1, 0.1, 0.1, 0.6}); // voice 0

		testManager.allNetworkOutputs = new ArrayList<double[]>(allNetworkOutputs);

		// allNetworkOutputsAdapted (in its initial state, in which it is the same as allNetworkOutputs) (bwd)
		testManager.allNetworkOutputsAdapted = new ArrayList<double[]>(allNetworkOutputs);

		// piece
		Piece piece = new Piece();
		NotationSystem system = piece.createNotationSystem();
		// Voice 0
		NotationStaff staff0 = new NotationStaff(system); system.add(staff0);
		NotationVoice voice0 = new NotationVoice(staff0); staff0.add(voice0);
		Note voice0n0 = Transcription.createNote(67, new Rational(0, 4), new Rational(1, 2));
		Note voice0n1 = Transcription.createNote(67, new Rational(2, 4), new Rational(1, 2));
		Note voice0n2 = Transcription.createNote(67, new Rational(4, 4), new Rational(1, 2));
		Note voice0n3 = Transcription.createNote(67, new Rational(6, 4), new Rational(3, 8));
		Note voice0n4 = Transcription.createNote(67, new Rational(15, 8), new Rational(1, 8));
		voice0.add(voice0n0); voice0.add(voice0n1); voice0.add(voice0n2); voice0.add(voice0n3); voice0.add(voice0n4);
		// Voice 1
		NotationStaff staff1 = new NotationStaff(system); system.add(staff1); 
		NotationVoice voice1 = new NotationVoice(staff1); staff1.add(voice1); 
		Note voice1n0 = Transcription.createNote(57, new Rational(0, 4), new Rational(1, 1));
		Note voice1n1 = Transcription.createNote(57, new Rational(4, 4), new Rational(1, 2));
		Note voice1n2 = Transcription.createNote(57, new Rational(6, 4), new Rational(1, 2));
		voice1.add(voice1n0); voice1.add(voice1n1); voice1.add(voice1n2);
		// Voice 2
		NotationStaff staff2 = new NotationStaff(system); system.add(staff2); 
		NotationVoice voice2 = new NotationVoice(staff2); staff2.add(voice2);
		Note voice2n0 = Transcription.createNote(53, new Rational(0, 4), new Rational(1, 2));
		Note voice2n1 = Transcription.createNote(53, new Rational(2, 4), new Rational(1, 2));
		Note voice2n2 = Transcription.createNote(53, new Rational(4, 4), new Rational(1, 1));
		voice2.add(voice2n0); voice2.add(voice2n1); voice2.add(voice2n2);
		// Voice 3
		NotationStaff staff3 = new NotationStaff(system); system.add(staff3); 
		NotationVoice voice3 = new NotationVoice(staff3); staff3.add(voice3); 
		Note voice3n0 = Transcription.createNote(43, new Rational(0, 4), new Rational(1, 1));
		Note voice3n1 = Transcription.createNote(43, new Rational(4, 4), new Rational(1, 2));
		Note voice3n2 = Transcription.createNote(43, new Rational(6, 4), new Rational(1, 4));
		Note voice3n3 = Transcription.createNote(43, new Rational(7, 4), new Rational(1, 4));
		voice3.add(voice3n0); voice3.add(voice3n1); voice3.add(voice3n2); voice3.add(voice3n3);
		testManager.newTranscription = new Transcription();
		testManager.newTranscription.setPiece(piece);

		// allNotes (bwd)
		List<List<Note>> allNotes = new ArrayList<List<Note>>();
		allNotes.add(Arrays.asList(new Note[]{voice0n4}));
		//
		allNotes.add(Arrays.asList(new Note[]{voice3n3}));
		//
		allNotes.add(Arrays.asList(new Note[]{voice3n2})); 
		allNotes.add(Arrays.asList(new Note[]{voice1n2}));
		allNotes.add(Arrays.asList(new Note[]{voice0n3}));
		//
		allNotes.add(Arrays.asList(new Note[]{voice3n1})); 
		allNotes.add(Arrays.asList(new Note[]{voice2n2}));
		allNotes.add(Arrays.asList(new Note[]{voice1n1}));
		allNotes.add(Arrays.asList(new Note[]{voice0n2})); 
		// 
		allNotes.add(Arrays.asList(new Note[]{voice2n1}));
		allNotes.add(Arrays.asList(new Note[]{voice0n1})); 
		//
		allNotes.add(Arrays.asList(new Note[]{voice3n0}));
		allNotes.add(Arrays.asList(new Note[]{voice2n0}));
		allNotes.add(Arrays.asList(new Note[]{voice1n0}));
		allNotes.add(Arrays.asList(new Note[]{voice0n0}));
		//
		testManager.allNotes = new ArrayList<List<Note>>(allNotes);

		// Determine the variables created and/or adapted by resolveConflicts()
		// conflictIndicesExpected (bwd)
		List<List<Integer>> conflictIndicesExpected = new ArrayList<List<Integer>>(); 
		conflictIndicesExpected.add(Arrays.asList(new Integer[]{4, 6, 11, 13}));
		conflictIndicesExpected.add(new ArrayList<Integer>());

		// allNetworkOutputsAdaptedExpected (network outputs are adapted only in case of type (i) conflicts) (bwd)
		List<double[]> allNetworkOutputsAdaptedExpected = new ArrayList<double[]>(allNetworkOutputs);
		allNetworkOutputsAdaptedExpected.set(4, new double[]{0.2, 0.0, 0.1, 0.0, 0.0});
		allNetworkOutputsAdaptedExpected.set(6, new double[]{0.0, 0.0, 0.4, 0.0, 0.1});
		allNetworkOutputsAdaptedExpected.set(11, new double[]{0.0, 0.1, 0.4, 0.6, 0.0});
		allNetworkOutputsAdaptedExpected.set(13, new double[]{0.1, 0.4, 0.0, 0.0, 0.0});

		// allPredictedVoicesExpected (in case of each conflict, a voice is adapted) (bwd)
		List<List<Integer>> allPredictedVoices = new ArrayList<List<Integer>>();
		allPredictedVoices.add(Arrays.asList(new Integer[]{0}));
		//
		allPredictedVoices.add(Arrays.asList(new Integer[]{3}));
		//
		allPredictedVoices.add(Arrays.asList(new Integer[]{3}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{3}));
		//
		allPredictedVoices.add(Arrays.asList(new Integer[]{3}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{0}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{1}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{0}));
		//
		allPredictedVoices.add(Arrays.asList(new Integer[]{2}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{0}));
		//
		allPredictedVoices.add(Arrays.asList(new Integer[]{0}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{2}));
		allPredictedVoices.add(Arrays.asList(new Integer[]{2}));    
		allPredictedVoices.add(Arrays.asList(new Integer[]{0}));

		List<List<Integer>> allPredictedVoicesExpected = new ArrayList<List<Integer>>(allPredictedVoices);
		allPredictedVoicesExpected.set(4, Arrays.asList(new Integer[]{0}));
		allPredictedVoicesExpected.set(6, Arrays.asList(new Integer[]{2}));
		allPredictedVoicesExpected.set(11, Arrays.asList(new Integer[]{3}));
		allPredictedVoicesExpected.set(13, Arrays.asList(new Integer[]{1}));

		// allVoiceLabelsExpected (in case of each conflict, a voice label is adapted) (fwd)
		List<List<Double>> allVoiceLabels = new ArrayList<List<Double>>();
		allVoiceLabels.add(Transcription.VOICE_0); allVoiceLabels.add(Transcription.VOICE_2);
		allVoiceLabels.add(Transcription.VOICE_2); allVoiceLabels.add(Transcription.VOICE_0);
		//
		allVoiceLabels.add(Transcription.VOICE_2); allVoiceLabels.add(Transcription.VOICE_0);
		//
		allVoiceLabels.add(Transcription.VOICE_3); allVoiceLabels.add(Transcription.VOICE_0);
		allVoiceLabels.add(Transcription.VOICE_1); allVoiceLabels.add(Transcription.VOICE_0);
		//
		allVoiceLabels.add(Transcription.VOICE_3); allVoiceLabels.add(Transcription.VOICE_1);
		allVoiceLabels.add(Transcription.VOICE_3);
		//
		allVoiceLabels.add(Transcription.VOICE_3);
		//
		allVoiceLabels.add(Transcription.VOICE_0);

		List<List<Double>> allVoiceLabelsExpected = new ArrayList<List<Double>>(allVoiceLabels);
		allVoiceLabelsExpected.set(12, Transcription.VOICE_0);
		allVoiceLabelsExpected.set(7, Transcription.VOICE_2);
		allVoiceLabelsExpected.set(2, Transcription.VOICE_1);
		allVoiceLabelsExpected.set(0, Transcription.VOICE_3);

		// pieceExpected (notes are removed from the piece only in case of type (ii) conflicts) 
		Piece pieceExpected = new Piece();
		pieceExpected.setScore(system);

		// allNotesExpected (bwd)
		List<List<Note>> allNotesExpected = new ArrayList<List<Note>>(allNotes);

		// Calculate actual
		testManager.backwardsMapping = 
			FeatureGenerator.getBackwardsMapping(testManager.groundTruthTranscription.getNumberOfNewNotesPerChord());
		for (int i = 0; i < testManager.backwardsMapping.size(); i++) {
			int noteIndex = testManager.backwardsMapping.get(i);
			testManager.resolveConflicts(allNetworkOutputs.get(i), noteIndex);
		}
		List<List<Integer>> conflictIndicesActual = testManager.conflictIndices; 
		List<double[]> allNetworkOutputsAdaptedActual = testManager.allNetworkOutputsAdapted;
		List<List<Integer>> allPredictedVoicesActual = testManager.allPredictedVoices;
		List<List<Double>> allVoiceLabelsActual = testManager.allVoiceLabels;
		Piece pieceActual = testManager.newTranscription.getPiece();
		List<List<Note>> allNotesActual = testManager.allNotes;
		System.out.println(testManager.conflictsRecord);

		// Assert equality
		// conflictIndices
		assertEquals(conflictIndicesExpected.size(), conflictIndicesActual.size());
		for (int i = 0; i < conflictIndicesExpected.size(); i++) { 
			assertEquals(conflictIndicesExpected.get(i).size(), conflictIndicesActual.get(i).size());
			for (int j = 0; j < conflictIndicesExpected.get(i).size(); j++) {
				assertEquals(conflictIndicesExpected.get(i).get(j), conflictIndicesActual.get(i).get(j));
			}
		}

		// allNetworkOutputsAdapted
		assertEquals(allNetworkOutputsAdaptedExpected.size(), allNetworkOutputsAdaptedActual.size());
		for (int i = 0; i < allNetworkOutputsAdaptedExpected.size(); i++) {
			assertEquals(allNetworkOutputsAdaptedExpected.get(i).length, allNetworkOutputsAdaptedActual.get(i).length);
			for (int j = 0; j < allNetworkOutputsAdaptedExpected.get(i).length; j++) {
				assertEquals(allNetworkOutputsAdaptedExpected.get(i)[j], allNetworkOutputsAdaptedActual.get(i)[j]);
			}
		}

		// allPredictedVoices
		assertEquals(allPredictedVoicesExpected.size(), allPredictedVoicesActual.size());
		for (int i = 0; i < allPredictedVoicesExpected.size(); i++) {
			assertEquals(allPredictedVoicesExpected.get(i).size(), allPredictedVoicesActual.get(i).size());
			for (int j = 0; j < allPredictedVoicesExpected.get(i).size(); j++) {
				assertEquals(allPredictedVoicesExpected.get(i).get(j), allPredictedVoicesActual.get(i).get(j));
			}
		}

		// allVoiceLabels
		assertEquals(allVoiceLabelsExpected.size(), allVoiceLabelsActual.size());
		for (int i = 0; i < allVoiceLabelsExpected.size(); i++) {
			assertEquals(allVoiceLabelsExpected.get(i).size(),allVoiceLabelsActual.get(i).size());
			for (int j = 0; j < allVoiceLabelsExpected.get(i).size(); j++) {
				assertEquals(allVoiceLabelsExpected.get(i).get(j), allVoiceLabelsActual.get(i).get(j));
			}
		}

		// piece
		assertEquals(pieceExpected.getScore().size(), pieceActual.getScore().size());
		// For each NotationStaff at index i
		for (int i = 0; i < pieceExpected.getScore().size(); i++) {
			assertEquals(pieceExpected.getScore().get(i).size(), pieceActual.getScore().get(i).size());
			// For each NotationVoice at index j
			for (int j = 0; j < pieceExpected.getScore().get(i).size(); j++) {
				assertEquals(pieceExpected.getScore().get(i).get(j).size(), pieceActual.getScore().get(i).get(j).size());
				// For each NotationChord at index k
				for (int k = 0; k < pieceExpected.getScore().get(i).get(j).size(); k++) {	
					assertEquals(pieceExpected.getScore().get(i).get(j).get(k).size(), pieceActual.getScore().get(i).get(j).get(k).size());
					// For each Note at index l
					for (int l = 0; l < pieceExpected.getScore().get(i).get(j).get(k).size(); l++) {
						assertEquals(pieceExpected.getScore().get(i).get(j).get(k).get(l), pieceActual.getScore().get(i).get(j).get(k).get(l));
						// OR if assertEquals(expected.get(i).get(j), actual.get(i).get(j).get(k) does not work because the Notes 
						// are not the same objects: check that pitch, metricTime, and metricDuration are the same
//						assertEquals(adaptedPiece.getScore().get(i).get(j).get(k).get(l).getMidiPitch(), 
//							pieceActual.getScore().get(i).get(j).get(k).get(l).getMidiPitch());
//						assertEquals(adaptedPiece.getScore().get(i).get(j).get(k).get(l).getMetricTime(), 
//							pieceActual.getScore().get(i).get(j).get(k).get(l).getMetricTime());
//						assertEquals(adaptedPiece.getScore().get(i).get(j).get(k).get(l).getMetricDuration(), 
//							pieceActual.getScore().get(i).get(j).get(k).get(l).getMetricDuration());		
					}
				}		
			}
		}

		// allNotes
		assertEquals(allNotesExpected.size(), allNotesActual.size());
		for (int i = 0; i < allNotesExpected.size(); i++) {
			assertEquals(allNotesExpected.get(i).size(), allNotesActual.get(i).size());
			for (int j = 0; j < allNotesExpected.get(i).size(); j++) {
				assertEquals(allNotesExpected.get(i).get(j).getMidiPitch(), allNotesActual.get(i).get(j).getMidiPitch());
				assertEquals(allNotesExpected.get(i).get(j).getMetricTime(), allNotesActual.get(i).get(j).getMetricTime());
				assertEquals(allNotesExpected.get(i).get(j).getMetricDuration(), allNotesActual.get(i).get(j).getMetricDuration());
			}
		} 
	}
	
	
	public void testGenerateObservations() {
		TestManager tm = new TestManager();
		tm.tablature = new Tablature(encodingTestpiece1, true);
		tm.groundTruthTranscription = new Transcription(midiTestpiece1, encodingTestpiece1);
		
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
		expected.add(Arrays.asList(new Integer[]{66}));
		expected.add(Arrays.asList(new Integer[]{64}));
		expected.add(Arrays.asList(new Integer[]{66}));
		expected.add(Arrays.asList(new Integer[]{67}));
		expected.add(Arrays.asList(new Integer[]{43, 55, 62, 67}));
		
		List<List<Integer>> actual = tm.generateObservations();
		
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j) );
			}
		}
	}
	
	
	public void testGenerateObservationsNonTab() {
		TestManager tm = new TestManager();
		tm.tablature = null;
		tm.groundTruthTranscription = new Transcription(midiTestpiece1, null);
		
		List<List<Integer>> expected = new ArrayList<List<Integer>>();
		expected.add(Arrays.asList(new Integer[]{50, 57, 65, 69}));
		expected.add(Arrays.asList(new Integer[]{45, 57, 69, 72}));
		expected.add(Arrays.asList(new Integer[]{48}));
		expected.add(Arrays.asList(new Integer[]{47, 50, 59, 65, 65}));
		expected.add(Arrays.asList(new Integer[]{45}));
		expected.add(Arrays.asList(new Integer[]{45, 57, 57, 60, 69}));
		expected.add(Arrays.asList(new Integer[]{45, 60, 64, 69}));
		expected.add(Arrays.asList(new Integer[]{59, 68}));
		expected.add(Arrays.asList(new Integer[]{45, 57, 64, 69}));
		expected.add(Arrays.asList(new Integer[]{68}));
		expected.add(Arrays.asList(new Integer[]{69}));
		expected.add(Arrays.asList(new Integer[]{68}));
		expected.add(Arrays.asList(new Integer[]{66}));
		expected.add(Arrays.asList(new Integer[]{68}));
		expected.add(Arrays.asList(new Integer[]{69}));
		expected.add(Arrays.asList(new Integer[]{45, 57, 64, 69}));
		
		List<List<Integer>> actual = tm.generateObservations();
		
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j) );
			}
		}
	}
	
	public void testGetVoicesFromMappingIndices() {
		TestManager tm = new TestManager();
		
		List<List<Integer>> mappingDict = new ArrayList<List<Integer>>();
		mappingDict.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{3, 2, 1, 0})));
		mappingDict.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{1, 2, -1, 0})));
		mappingDict.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{-1, -1, 1, 0})));
		mappingDict.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{2, 2, 1, 0})));
		mappingDict.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{0, -1, 1, 0})));
		tm.mappingDictionary = mappingDict;
		
		List<Integer> mappingIndices = new ArrayList<Integer>(Arrays.asList(new Integer[]{0, 1, 2, 3, 4}));

		// Mapping 0
		List<List<Integer>> expected = new ArrayList<List<Integer>>();
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{3})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{2})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{1})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{0})));
		// Mapping 1
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{3})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{0})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{1})));
		// Mapping 2
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{3})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{2})));
		// Mapping 3
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{3})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{2})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{0, 1})));
		// Mapping 4
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{0, 3})));
		expected.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{2})));

		List<List<Integer>> actual = tm.getVoicesFromMappingIndices(mappingIndices);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
		assertEquals(expected, actual);
	}

}