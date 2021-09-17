package featureExtraction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.uos.fmt.musitech.data.score.NotationSystem;
import de.uos.fmt.musitech.data.score.NotationVoice;
import de.uos.fmt.musitech.data.structure.Note;
import de.uos.fmt.musitech.data.structure.Piece;
import de.uos.fmt.musitech.data.structure.container.NoteSequence;
import featureExtraction.FeatureGenerator.Direction;
import junit.framework.TestCase;
import representations.Tablature;
import representations.Transcription;
import tools.ToolBox;
import ui.Runner;
import ui.Runner.ModellingApproach;
import ui.Runner.ProcessingMode;
import ui.UI;
import utility.DataConverter;

public class FeatureGeneratorTest extends TestCase {

	private File midiTestpiece1; // = new File(Runner.midiPathTest + "testpiece.mid");
	private File encodingTestpiece1; // = new File(Runner.encodingsPathTest + "testpiece.txt");

	private static final int NUM_FEATURES_SET_A = 8;
	private static final int NUM_FEATURES_SET_A_NO_TAB_INFO = 4;
	private static final int NUM_FEATURES_SET_B = 12;
	private static final int NUM_FEATURES_SET_C = 27;
	private static final int NUM_FEATURES_SET_D = 32;

	private static final int NUM_FEATURES_SET_A_NON_TAB = 4;
	private static final int NUM_FEATURES_SET_B_NON_TAB = 4 + 3 + 3;
	private static final int NUM_FEATURES_SET_C_NON_TAB = 4 + 3 + 3 + 15;
	private static final int NUM_FEATURES_SET_D_NON_TAB = 4 + 3 + 3 + 15 + 5 ;


	protected void setUp() throws Exception {
		super.setUp();
		Runner.setPathsToCodeAndData(UI.getRootDir(), false);
		midiTestpiece1 = new File(Runner.midiPathTest + "testpiece.mid");
		encodingTestpiece1 = new File(Runner.encodingsPathTest + "testpiece.tbp");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}


	public void bla() { // TODO wat is dit? Test gen voice labels B model?
		String pieceName = "Barbetta 1582 - Il nest plaisir";
		File midiFile = new File(Runner.midiPath + "4vv/" + pieceName);
		File encodingFile = new File(Runner.encodingsPath + "4vv/" + pieceName + ".tbp");
		
		Tablature tab = new Tablature(encodingFile, true); 
		Transcription gtTrans = new Transcription(midiFile, encodingFile);
		
		List<List<Double>> predVoiceLabels = ToolBox.getStoredObject(new ArrayList<List<Double>>(),
			new File("F/PhD/data" + "results/gen_labels/tab/4vv/" + 
//			"Predicted voice labels " +	pieceName + ".xml"));
			"voice_lab-" +	pieceName + ".ser"));
		List<List<Double>> predDurationLabels = ToolBox.getStoredObject(new ArrayList<List<Double>>(), 
			new File("F/PhD/data" + "results/gen_labels/tab/4vv/" +  
//			"Predicted duration labels " + pieceName + ".xml"));
			"dur_lab-" + pieceName + ".ser"));
		
		Piece argPiece = Transcription.createPiece(tab.getBasicTabSymbolProperties(), null, predVoiceLabels,
			predDurationLabels, 4, null, null);
			
		Transcription predTrans = null;
//			new Transcription(midiFile, encodingFile, argPiece, 
//			predVoiceLabels, predDurationLabels/*, null*/);
		
		List<List<Double>> list1 = predTrans.getDurationLabels();
		List<List<Double>> list2 = gtTrans.getDurationLabels();
		
		NoteSequence nos1 = predTrans.getNoteSequence();
		List<Note> ns1 = new ArrayList<Note>();
	    for (Note n : nos1) {
	    	ns1.add(n);
	    }
		NoteSequence nos2 = gtTrans.getNoteSequence();
		List<Note> ns2 = new ArrayList<Note>();
	    for (Note n : nos2) {
	    	ns2.add(n);
	    }
		
		assertEquals(ns1.size(), ns2.size());
	    for (int i = 0; i < ns1.size(); i++) {
	    	// assertEquals(expected.get(i), actual.get(i)) does not work because the Notes are not the same
	    	// objects: therefore check that pitch, metricTime, and metricDuration are the same
	    	assertEquals(ns1.get(i).getMidiPitch(), ns2.get(i).getMidiPitch());
	    	assertEquals(ns1.get(i).getMetricTime(), ns2.get(i).getMetricTime());
	    	assertEquals(ns1.get(i).getMetricDuration(), ns2.get(i).getMetricDuration());
	    }

		assertEquals(list1.size(), list2.size());
		for (int i = 0; i < list1.size(); i++) {
			assertEquals(list1.get(i).size(), list2.get(i).size());
			for (int j = 0; j < list1.get(i).size(); j++) {
				assertEquals(list1.get(i).get(j), list2.get(i).get(j));
			}
		}
	}


	private List<List<Double>> getSetOfFeatureVectors() {
		List<List<Double>> setOfFeatureVectors = new ArrayList<List<Double>>();
		setOfFeatureVectors.add(Arrays.asList(new Double[]{0.0,  0.0, -1.0, -1.0, 1.0, -1.0, 0.0, -2.0, -1.0}));
		setOfFeatureVectors.add(Arrays.asList(new Double[]{1.0, -1.0, -1.0,  0.0, 2.0,  2.0, 0.0,  1.0, -1.0}));
		setOfFeatureVectors.add(Arrays.asList(new Double[]{2.0,  1.0, -1.0,  0.0, 3.0, -1.0, 0.0,  0.0, -1.0}));
		return setOfFeatureVectors;		
	}
	
	
	private List<List<Double>> getSecondSetOfFeatureVectors() {
		List<List<Double>> setOfFeatureVectors = new ArrayList<List<Double>>();
		setOfFeatureVectors.add(Arrays.asList(new Double[]{1.0,  1.0, -1.0, -1.0, 1.0,  -1.0, 0.0, -2.0, -1.0}));
		setOfFeatureVectors.add(Arrays.asList(new Double[]{2.0, -1.0,  0.0,  1.0, 2.0,   2.0, 0.0,  2.0, -1.0}));
		setOfFeatureVectors.add(Arrays.asList(new Double[]{3.0,  2.0, -1.0,  2.0, -3.0, -1.0, 0.0,  3.0, -1.0}));
		return setOfFeatureVectors;
	}
	
	
	public void testGetBasicNoteFeaturesMUSCI() {
		Tablature tablature = new Tablature(encodingTestpiece1, false);

	List<double[]> expected = new ArrayList<double[]>();
    // Chord 0
    expected.add(new double[]{50.0, 5.0, 0.0, 1/4.0, 1/2.0, 4.0, 0.0, 1.0});
    expected.add(new double[]{57.0, 4.0, 2.0, 1/4.0, 1/4.0, 4.0, 0.0, 0.0});
    expected.add(new double[]{65.0, 2.0, 1.0, 1/4.0, 1/4.0, 4.0, 0.0, 0.0});
    expected.add(new double[]{69.0, 1.0, 0.0, 1/4.0, 1/4.0, 4.0, 0.0, 1.0});
    // Chord 1
    expected.add(new double[]{45.0, 6.0, 0.0, 3/16.0, 3/16.0, 4.0, 0.0, 1.0});
    expected.add(new double[]{57.0, 4.0, 2.0, 3/16.0, 1/4.0, 4.0, 0.0, 0.0});
    expected.add(new double[]{72.0, 2.0, 8.0, 3/16.0, 1/4.0, 4.0, 0.0, 0.0});
    expected.add(new double[]{69.0, 1.0, 0.0, 3/16.0, 3/4.0, 4.0, 0.0, 1.0});
    // Chord 2
    expected.add(new double[]{48.0, 6.0, 3.0, 1/16.0, 1/16.0, 1.0, 1.0, 0.0});
    // Chord 3
    expected.add(new double[]{47.0, 6.0, 2.0, 1/8.0, 1/8.0, 4.0, 0.0, 0.0});
    expected.add(new double[]{50.0, 5.0, 0.0, 1/8.0, 1/4.0, 4.0, 0.0, 1.0});
    expected.add(new double[]{59.0, 4.0, 4.0, 1/8.0, 1/4.0, 4.0, 0.0, 0.0});
    expected.add(new double[]{65.0, 2.0, 1.0, 1/8.0, 1/4.0, 4.0, 0.0, 0.0});
    // Chord 4
    expected.add(new double[]{45.0, 6.0, 0.0, 1/8.0, 1/8.0, 1.0, 0.0, 1.0});
    // Chord 5
    expected.add(new double[]{45.0, 6.0, 0.0, 1/4.0, 1/4.0, 5.0, 0.0, 1.0});
    expected.add(new double[]{57.0, 5.0, 7.0, 1/4.0, 3/2.0, 5.0, 0.0, 0.0});
    expected.add(new double[]{57.0, 4.0, 2.0, 1/4.0, 1/2.0, 5.0, 0.0, 0.0});
    expected.add(new double[]{60.0, 3.0, 1.0, 1/4.0, 1/4.0, 5.0, 0.0, 0.0});
    expected.add(new double[]{69.0, 2.0, 5.0, 1/4.0, 1/4.0, 5.0, 0.0, 0.0});
    // Chord 6
    expected.add(new double[]{45.0, 6.0, 0.0, 1/8.0, 1/4.0, 4.0, 0.0, 1.0});
    expected.add(new double[]{60.0, 3.0, 1.0, 1/8.0, 1/8.0, 4.0, 0.0, 0.0});
    expected.add(new double[]{64.0, 2.0, 0.0, 1/8.0, 1/8.0, 4.0, 0.0, 1.0});
    expected.add(new double[]{69.0, 1.0, 0.0, 1/8.0, 1/4.0, 4.0, 0.0, 1.0});
    // Chord 7
    expected.add(new double[]{59.0, 3.0, 0.0, 1/8.0, 9/8.0, 2.0, 0.0, 1.0});
    expected.add(new double[]{68.0, 2.0, 4.0, 1/8.0, 1/8.0, 2.0, 0.0, 0.0});
    // Chord 8
    expected.add(new double[]{45.0, 6.0, 0.0, 1/16.0, 3/4.0, 4.0, 0.0, 1.0});
    expected.add(new double[]{57.0, 4.0, 2.0, 1/16.0, 3/4.0, 4.0, 0.0, 0.0});
    expected.add(new double[]{64.0, 2.0, 0.0, 1/16.0, 1/16.0, 4.0, 0.0, 1.0});
    expected.add(new double[]{69.0, 1.0, 0.0, 1/16.0, 1/8.0, 4.0, 0.0, 1.0});
    // Chords 9-14
    expected.add(new double[]{68.0, 2.0, 4.0, 1/16.0, 3/32.0, 1.0, 1.0, 0.0});
    expected.add(new double[]{69.0, 1.0, 0.0, 1/32.0, 1/8.0, 1.0, 1.0, 1.0});
    expected.add(new double[]{68.0, 2.0, 4.0, 1/32.0, 1/32.0, 1.0, 1.0, 0.0});
    expected.add(new double[]{66.0, 2.0, 2.0, 1/32.0, 1/32.0, 1.0, 1.0, 0.0});
    expected.add(new double[]{68.0, 2.0, 4.0, 1/32.0, 17/32.0, 1.0, 1.0, 0.0});
    expected.add(new double[]{69.0, 1.0, 0.0, 1/4.0, 1/2.0, 1.0, 0.0, 1.0});
    // Chord 15
    expected.add(new double[]{45.0, 6.0, 0.0, 1/4.0, 1/4.0, 4.0, 0.0, 1.0});
    expected.add(new double[]{57.0, 4.0, 2.0, 1/4.0, 1/4.0, 4.0, 0.0, 0.0});
    expected.add(new double[]{64.0, 2.0, 0.0, 1/4.0, 1/4.0, 4.0, 0.0, 1.0});
    expected.add(new double[]{69.0, 1.0, 0.0, 1/4.0, 1/4.0, 4.0, 0.0, 1.0});
      	
    List<double[]> actual = new ArrayList<double[]>();
    Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
    for (int i = 0; i < basicTabSymbolProperties.length; i++) {
    	actual.add(FeatureGenerator.getBasicNoteFeaturesMUSCI(basicTabSymbolProperties, null, i));
    }
    	   
    assertEquals(expected.size(), actual.size());
	  for (int i = 0; i < expected.size(); i++) {
	  	assertEquals(expected.get(i).length, actual.get(i).length);
	  	for (int j = 0; j < expected.get(i).length; j++) {
	  		assertEquals(expected.get(i)[j], actual.get(i)[j]);
	  	}
	  }
	}
  
  
  public void testGetBasicNoteFeaturesNonTabMUSCI() {
  	Transcription transcription = new Transcription(midiTestpiece1, null);

    List<double[]> expected = new ArrayList<double[]>();
    // Chord 0
    expected.add(new double[]{50.0, 1/4.0, 4.0, 0.0});
    expected.add(new double[]{57.0, 1/4.0, 4.0, 0.0});
    expected.add(new double[]{65.0, 1/4.0, 4.0, 0.0});
    expected.add(new double[]{69.0, 1/4.0, 4.0, 0.0});
    // Chord 1
    expected.add(new double[]{45.0, 3/16.0, 4.0, 0.0});
    expected.add(new double[]{57.0, 1/4.0, 4.0, 0.0});
    expected.add(new double[]{69.0, 1/8.0, 4.0, 0.0});
    expected.add(new double[]{72.0, 1/4.0, 4.0, 0.0});
    // Chord 2
    expected.add(new double[]{48.0, 1/16.0, 3.0, 1.0});
    // Chord 3
    expected.add(new double[]{47.0, 1/8.0, 5.0, 0.0});
    expected.add(new double[]{50.0, 1/4.0, 5.0, 0.0});
    expected.add(new double[]{59.0, 1/4.0, 5.0, 0.0});
    expected.add(new double[]{65.0, 1/4.0, 5.0, 0.0});
    expected.add(new double[]{65.0, 1/8.0, 5.0, 0.0});
    // Chord 4
    expected.add(new double[]{45.0, 1/8.0, 4.0, 0.0});
    // Chord 5
    expected.add(new double[]{45.0, 1/4.0, 5.0, 0.0});
    expected.add(new double[]{57.0, 1/2.0, 5.0, 0.0});
    expected.add(new double[]{57.0, 1/4.0, 5.0, 0.0});
    expected.add(new double[]{60.0, 1/4.0, 5.0, 0.0});
    expected.add(new double[]{69.0, 1/4.0, 5.0, 0.0});
    // Chord 6
    expected.add(new double[]{45.0, 1/4.0, 5.0, 0.0});
    expected.add(new double[]{60.0, 1/8.0, 5.0, 0.0});
    expected.add(new double[]{64.0, 1/8.0, 5.0, 0.0});
    expected.add(new double[]{69.0, 1/4.0, 5.0, 0.0});
    // Chord 7
    expected.add(new double[]{59.0, 1/8.0, 5.0, 0.0});
    expected.add(new double[]{68.0, 1/8.0, 5.0, 0.0});
    // Chord 8
    expected.add(new double[]{45.0, 1/2.0, 4.0, 0.0});
    expected.add(new double[]{57.0, 1/2.0, 4.0, 0.0});
    expected.add(new double[]{64.0, 1/2.0, 4.0, 0.0});
    expected.add(new double[]{69.0, 1/16.0, 4.0, 0.0});
    // Chords 9-14
    expected.add(new double[]{68.0, 1/16.0, 4.0, 1.0});
    expected.add(new double[]{69.0, 1/32.0, 4.0, 1.0});
    expected.add(new double[]{68.0, 1/32.0, 4.0, 1.0});
    expected.add(new double[]{66.0, 1/32.0, 4.0, 1.0});
    expected.add(new double[]{68.0, 1/32.0, 4.0, 1.0});
    expected.add(new double[]{69.0, 1/4.0, 4.0, 0.0});
    // Chord 15
    expected.add(new double[]{45.0, 1/4.0, 4.0, 0.0});
    expected.add(new double[]{57.0, 1/4.0, 4.0, 0.0});
    expected.add(new double[]{64.0, 1/4.0, 4.0, 0.0});
    expected.add(new double[]{69.0, 1/4.0, 4.0, 0.0});
  	
    List<double[]> actual = new ArrayList<double[]>();
    Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
    for (int i = 0; i < basicNoteProperties.length; i++) {
    	actual.add(FeatureGenerator.getBasicNoteFeaturesMUSCI(null, basicNoteProperties, i));
    }

    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
   	  assertEquals(expected.get(i).length, actual.get(i).length);
    	for (int j = 0; j < expected.get(i).length; j++) {
    		assertEquals(expected.get(i)[j], actual.get(i)[j]);
    	}
    }
	}
  
  
  public void testGetIndicesOfSustainedPreviousNotesMUSCI() {
  	Transcription transcription = new Transcription(midiTestpiece1, null);

		List<List<Integer>> expected = new ArrayList<List<Integer>>();
		List<Integer> emptyList = Arrays.asList(new Integer[]{});
		// Chord 0
		expected.add(emptyList); expected.add(emptyList); expected.add(emptyList); expected.add(emptyList);
		// Chord 1
		expected.add(emptyList); expected.add(emptyList); expected.add(emptyList); expected.add(emptyList);
		// Chord 2
		expected.add(Arrays.asList(new Integer[]{5, 7}));
	  // Chord 3
		expected.add(emptyList); expected.add(emptyList); expected.add(emptyList); expected.add(emptyList);
		expected.add(emptyList);
		// Chord 4
		expected.add(Arrays.asList(new Integer[]{10, 11, 12}));
		// Chord 5
		expected.add(emptyList); expected.add(emptyList); expected.add(emptyList); expected.add(emptyList);
		expected.add(emptyList);
  	// Chord 6
		expected.add(Arrays.asList(new Integer[]{16}));
		expected.add(Arrays.asList(new Integer[]{16}));
		expected.add(Arrays.asList(new Integer[]{16}));
		expected.add(Arrays.asList(new Integer[]{16}));
	  // Chord 7
		expected.add(Arrays.asList(new Integer[]{16, 20, 23}));
		expected.add(Arrays.asList(new Integer[]{16, 20, 23}));
		// Chord 8
		expected.add(emptyList); expected.add(emptyList); expected.add(emptyList); expected.add(emptyList);
		// Chords 9-14
		expected.add(Arrays.asList(new Integer[]{26, 27, 28}));
		expected.add(Arrays.asList(new Integer[]{26, 27, 28}));
		expected.add(Arrays.asList(new Integer[]{26, 27, 28}));
		expected.add(Arrays.asList(new Integer[]{26, 27, 28}));
		expected.add(Arrays.asList(new Integer[]{26, 27, 28}));
		expected.add(Arrays.asList(new Integer[]{26, 27, 28}));
		// Chord 15
		expected.add(emptyList); expected.add(emptyList); expected.add(emptyList); expected.add(emptyList);
				
		List<List<Integer>> actual = new ArrayList<List<Integer>>();
		Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
		for (int i = 0; i < basicNoteProperties.length; i++) {
			actual.add(FeatureGenerator.getIndicesOfSustainedPreviousNotesMUSCI(basicNoteProperties, i));
		}
	
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}
  
  
  public void testGetPositionWithinChordMUSCI() {
		Tablature tablature = new Tablature(encodingTestpiece1, false);
			    
	  // Determine expected	  
	  List<double[]> expected = new ArrayList<double[]>();
	  // Chord 0
	  expected.add(new double[]{0.0, 3.0});
	  expected.add(new double[]{1.0, 2.0});
	  expected.add(new double[]{2.0, 1.0});
	  expected.add(new double[]{3.0, 0.0});
	  // Chord 1
	  expected.add(new double[]{0.0, 3.0});
	  expected.add(new double[]{1.0, 2.0});
	  expected.add(new double[]{2.0, 1.0});
	  expected.add(new double[]{3.0, 0.0});
	  // Chord 2
	  expected.add(new double[]{0.0, 0.0});
	  // Chord 3
	  expected.add(new double[]{0.0, 3.0});
	  expected.add(new double[]{1.0, 2.0});
	  expected.add(new double[]{2.0, 1.0});
	  expected.add(new double[]{3.0, 0.0});
	  // Chord 4
	  expected.add(new double[]{0.0, 0.0});
	  // Chord 5
	  expected.add(new double[]{0.0, 4.0});
	  expected.add(new double[]{1.0, 3.0});
	  expected.add(new double[]{2.0, 2.0});
	  expected.add(new double[]{3.0, 1.0});
	  expected.add(new double[]{4.0, 0.0});
	  // Chord 6
	  expected.add(new double[]{0.0, 3.0});
	  expected.add(new double[]{1.0, 2.0});
	  expected.add(new double[]{2.0, 1.0});
	  expected.add(new double[]{3.0, 0.0});
	  // Chord 7
	  expected.add(new double[]{0.0, 1.0});
	  expected.add(new double[]{1.0, 0.0});
	  // Chord 8
	  expected.add(new double[]{0.0, 3.0});
	  expected.add(new double[]{1.0, 2.0});
	  expected.add(new double[]{2.0, 1.0});
	  expected.add(new double[]{3.0, 0.0});
	  // Chords 9-14
	  expected.add(new double[]{0.0, 0.0});
	  expected.add(new double[]{0.0, 0.0});
	  expected.add(new double[]{0.0, 0.0});
	  expected.add(new double[]{0.0, 0.0});
	  expected.add(new double[]{0.0, 0.0});
	  expected.add(new double[]{0.0, 0.0});
	  // Chord 15
	  expected.add(new double[]{0.0, 3.0});
	  expected.add(new double[]{1.0, 2.0});
	  expected.add(new double[]{2.0, 1.0});
	  expected.add(new double[]{3.0, 0.0});
	  	  	    	
	  // Calculate actual 
	  List<double[]> actual = new ArrayList<double[]>();
	  Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
	  for (int i = 0; i < basicTabSymbolProperties.length; i++) {
	  	actual.add(FeatureGenerator.getPositionWithinChordMUSCI(basicTabSymbolProperties, null, i));
	  }
	    	   
		// Assert equality  
	  assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
		 	for (int j = 0; j < expected.get(i).length; j++) {
		 		assertEquals(expected.get(i)[j], actual.get(i)[j]);
		 	}
		}
	}
	
	
	public void testGetPositionWithinChordNonTabMUSCI() {
  	Transcription transcription = new Transcription(midiTestpiece1, null);

	  // Determine expected 
	  List<double[]> expected = new ArrayList<double[]>();
	  // Chord 0
	  expected.add(new double[]{0.0, 0.0, 3.0});
	  expected.add(new double[]{1.0, 0.0, 2.0});
	  expected.add(new double[]{2.0, 0.0, 1.0});
	  expected.add(new double[]{3.0, 0.0, 0.0});
	  // Chord 1
	  expected.add(new double[]{0.0, 0.0, 3.0});
	  expected.add(new double[]{1.0, 0.0, 2.0});
	  expected.add(new double[]{2.0, 0.0, 1.0});
	  expected.add(new double[]{3.0, 0.0, 0.0});
	  // Chord 2
	  expected.add(new double[]{0.0, 0.0, 2.0});
	  // Chord 3
	  expected.add(new double[]{0.0, 0.0, 4.0});
	  expected.add(new double[]{1.0, 0.0, 3.0});
	  expected.add(new double[]{2.0, 0.0, 2.0});
	  expected.add(new double[]{3.0, 1.0, 0.0});
	  expected.add(new double[]{3.0, 1.0, 0.0});
	  // Chord 4
	  expected.add(new double[]{0.0, 0.0, 3.0});
	  // Chord 5
	  expected.add(new double[]{0.0, 0.0, 4.0});
	  expected.add(new double[]{1.0, 1.0, 2.0});
	  expected.add(new double[]{1.0, 1.0, 2.0});
	  expected.add(new double[]{3.0, 0.0, 1.0});
	  expected.add(new double[]{4.0, 0.0, 0.0});
	  // Chord 6
	  expected.add(new double[]{0.0, 0.0, 4.0});
	  expected.add(new double[]{2.0, 0.0, 2.0});
	  expected.add(new double[]{3.0, 0.0, 1.0});
	  expected.add(new double[]{4.0, 0.0, 0.0});
	  // Chord 7
	  expected.add(new double[]{2.0, 0.0, 2.0});
	  expected.add(new double[]{3.0, 0.0, 1.0});
	  // Chord 8
	  expected.add(new double[]{0.0, 0.0, 3.0});
	  expected.add(new double[]{1.0, 0.0, 2.0});
	  expected.add(new double[]{2.0, 0.0, 1.0});
	  expected.add(new double[]{3.0, 0.0, 0.0});
	  // Chords 9-14
	  expected.add(new double[]{3.0, 0.0, 0.0});
	  expected.add(new double[]{3.0, 0.0, 0.0});
	  expected.add(new double[]{3.0, 0.0, 0.0});
	  expected.add(new double[]{3.0, 0.0, 0.0});
	  expected.add(new double[]{3.0, 0.0, 0.0});
	  expected.add(new double[]{3.0, 0.0, 0.0});
	  // Chord 15
	  expected.add(new double[]{0.0, 0.0, 3.0});
	  expected.add(new double[]{1.0, 0.0, 2.0});
	  expected.add(new double[]{2.0, 0.0, 1.0});
	  expected.add(new double[]{3.0, 0.0, 0.0});
	  
	  // Calculate actual
	  List<double[]> actual = new ArrayList<double[]>();
	  Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
	  for (int i = 0; i < basicNoteProperties.length; i++) {
	  	actual.add(FeatureGenerator.getPositionWithinChordMUSCI(null, basicNoteProperties, i));
	  }
    	   
	  // Assert equality  
    assertEquals(expected.size(), actual.size());
  	for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);  
  	 	for (int j = 0; j < expected.get(i).length; j++) {
		 		assertEquals(expected.get(i)[j], actual.get(i)[j]);
		 	}
		}
	}
	
	
	public void testGetPitchDistancesWithinChordMUSCI() {
		Tablature tablature = new Tablature(encodingTestpiece1, false);
			    	  
	  // Determine expected
	  List<double[]> expected = new ArrayList<double[]>();
		// Chord 0
	  expected.add(new double[]{-1.0, 7.0}); 
	  expected.add(new double[]{7.0, 8.0});
	  expected.add(new double[]{8.0, 4.0}); 
	  expected.add(new double[]{4.0, -1.0});
	  // Chord 1
	  expected.add(new double[]{-1.0, 12.0}); 
	  expected.add(new double[]{12.0, 15.0});
	  expected.add(new double[]{15.0, 3.0}); 
	  expected.add(new double[]{3.0, -1.0}); 
	  // Chord 2
	  expected.add(new double[]{-1.0, -1.0});
	  // Chord  3
	  expected.add(new double[]{-1.0, 3.0}); 
	  expected.add(new double[]{3.0, 9.0});
	  expected.add(new double[]{9.0, 6.0}); 
	  expected.add(new double[]{6.0, -1.0}); 
	  // Chord 4
	  expected.add(new double[]{-1.0, -1.0});
	  // Chord 5
	  expected.add(new double[]{-1.0, 12.0}); 
	  expected.add(new double[]{12.0, 0.0});
	  expected.add(new double[]{0.0, 3.0}); 
	  expected.add(new double[]{3.0, 9.0});
	  expected.add(new double[]{9.0, -1.0});
	  // Chord 6
	  expected.add(new double[]{-1.0, 15.0}); 
	  expected.add(new double[]{15.0, 4.0});
	  expected.add(new double[]{4.0, 5.0}); 
	  expected.add(new double[]{5.0, -1.0});
	  // Chord 7
	  expected.add(new double[]{-1.0, 9.0}); 
	  expected.add(new double[]{9.0, -1.0});
	  // Chord 8
	  expected.add(new double[]{-1.0, 12.0}); 
	  expected.add(new double[]{12.0, 7.0});
	  expected.add(new double[]{7.0, 5.0}); 
	  expected.add(new double[]{5.0, -1.0});
	  // Chords 9-14
	  expected.add(new double[]{-1.0, -1.0}); 
	  expected.add(new double[]{-1.0, -1.0});
	  expected.add(new double[]{-1.0, -1.0}); 
	  expected.add(new double[]{-1.0, -1.0});
	  expected.add(new double[]{-1.0, -1.0}); 
	  expected.add(new double[]{-1.0, -1.0});
	  // Chord 15
	  expected.add(new double[]{-1.0, 12.0}); 
	  expected.add(new double[]{12.0, 7.0});
	  expected.add(new double[]{7.0, 5.0}); 
	  expected.add(new double[]{5.0, -1.0});
	  	    	
	  // Calculate actual
	  List<double[]> actual = new ArrayList<double[]>();
	  Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
	  for (int i = 0; i < basicTabSymbolProperties.length; i++) {
	   	actual.add(FeatureGenerator.getPitchDistancesWithinChordMUSCI(basicTabSymbolProperties, null, i));
	  }
	    	   
		// Assert equality  
	  assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
		 	for (int j = 0; j < expected.get(i).length; j++) {
		 		assertEquals(expected.get(i)[j], actual.get(i)[j]);
		 	}
		}
	}
	
	
	public void testGetPitchDistancesWithinChordNonTabMUSCI() {
  	Transcription transcription = new Transcription(midiTestpiece1, null);

	  // Determine expected 
	  List<double[]> expected = new ArrayList<double[]>();
		// Chord 0
	  expected.add(new double[]{-1.0, -1.0, 7.0}); 
	  expected.add(new double[]{7.0, -1.0, 8.0});
	  expected.add(new double[]{8.0, -1.0, 4.0}); 
	  expected.add(new double[]{4.0,-1.0, -1.0});
	  // Chord 1
	  expected.add(new double[]{-1.0, -1.0, 12.0});
	  expected.add(new double[]{12.0, -1.0, 12.0});
	  expected.add(new double[]{12.0, -1.0, 3.0});
	  expected.add(new double[]{3.0, -1.0, -1.0}); 
	  // Chord 2
	  expected.add(new double[]{-1.0, -1.0, 9.0});
	  // Chord  3
	  expected.add(new double[]{-1.0, -1.0, 3.0});
	  expected.add(new double[]{3.0, -1.0, 9.0});
	  expected.add(new double[]{9.0, -1.0, 6.0}); 
	  expected.add(new double[]{6.0, 0.0, -1.0});
	  expected.add(new double[]{6.0, 0.0, -1.0});
	  // Chord 4
	  expected.add(new double[]{-1.0, -1.0, 5.0});
	  // Chord 5
	  expected.add(new double[]{-1.0, -1.0, 12.0}); 
	  expected.add(new double[]{12.0, 0.0, 3.0});
	  expected.add(new double[]{12.0, 0.0, 3.0}); 
	  expected.add(new double[]{3.0, -1.0, 9.0});
	  expected.add(new double[]{9.0, -1.0, -1.0});
	  // Chord 6
	  expected.add(new double[]{-1.0, -1.0, 12.0}); 
	  expected.add(new double[]{3.0, -1.0, 4.0});
	  expected.add(new double[]{4.0, -1.0, 5.0}); 
	  expected.add(new double[]{5.0, -1.0, -1.0});
	  // Chord 7
	  expected.add(new double[]{2.0, -1.0, 9.0}); 
	  expected.add(new double[]{9.0, -1.0, 1.0});
	  // Chord 8
	  expected.add(new double[]{-1.0, -1.0, 12.0}); 
	  expected.add(new double[]{12.0, -1.0, 7.0});
	  expected.add(new double[]{7.0, -1.0, 5.0}); 
	  expected.add(new double[]{5.0, -1.0, -1.0});
	  // Chords 9-14
	  expected.add(new double[]{4.0, -1.0, -1.0}); 
	  expected.add(new double[]{5.0, -1.0, -1.0});
	  expected.add(new double[]{4.0, -1.0, -1.0}); 
	  expected.add(new double[]{2.0, -1.0, -1.0});
	  expected.add(new double[]{4.0, -1.0, -1.0}); 
	  expected.add(new double[]{5.0, -1.0, -1.0});
	  // Chord 15
	  expected.add(new double[]{-1.0, -1.0, 12.0}); 
	  expected.add(new double[]{12.0, -1.0, 7.0});
	  expected.add(new double[]{7.0, -1.0, 5.0}); 
	  expected.add(new double[]{5.0, -1.0, -1.0});
	  	    	
	  // Calculate actual
	  List<double[]> actual = new ArrayList<double[]>();
	  Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
	  for (int i = 0; i < basicNoteProperties.length; i++) {
	   	actual.add(FeatureGenerator.getPitchDistancesWithinChordMUSCI(null, basicNoteProperties, i));
	  }
    	   
		// Assert equality  
	  assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
		 	for (int j = 0; j < expected.get(i).length; j++) {
		 		assertEquals(expected.get(i)[j], actual.get(i)[j]);
		 	}
		}
	}
	
	
	public void testGetPitchAndTimeProximitiesToAllVoicesMUSCI() {
    Tablature tablature = new Tablature(encodingTestpiece1, true);
  	Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

  	// Determine expected
  	List<double[][]> expected = new ArrayList<double[][]>();
  	// Chord 0
	  expected.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},
	  	{-1.0, -1.0, -1.0, -1.0, -1.0}});
	  expected.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},
	  	{-1.0, -1.0, -1.0, -1.0, -1.0}});
	  expected.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
	  	{-1.0, -1.0, -1.0, -1.0, -1.0}});
	  expected.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},
	  	{-1.0, -1.0, -1.0, -1.0, -1.0}});
  	// Chord 1
	  expected.add(new double[][]{{24.0, 20.0, 12.0, 5.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
	  	{0.0, 0.0, 0.0, 0.0, -1.0}});
  	expected.add(new double[][]{{12.0, 8.0, 0.0, 7.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
  		{0.0, 0.0, 0.0, 0.0, -1.0}});
  	expected.add(new double[][]{{3.0, 7.0, 15.0, 22.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
  		{0.0, 0.0, 0.0, 0.0, -1.0}});
  	expected.add(new double[][]{{0.0, 4.0, 12.0, 19.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
  		{0.0, 0.0, 0.0, 0.0, -1.0}});
  	// Chord 2
  	expected.add(new double[][]{{24.0, 21.0, 9.0, 3.0, -1.0}, {3/16.0, 3/16.0, 3/16.0, 3/16.0, -1.0}, 
  		{0.0, 0.0, 0.0, 0.0, -1.0}});
  	// Chord 3
  	expected.add(new double[][]{{25.0, 22.0, 10.0, 1.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
  		{1/16.0, 1/16.0, 1/16.0, 0.0, -1.0}});
  	expected.add(new double[][]{{22.0, 19.0, 7.0, 2.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0},
  		{1/16.0, 1/16.0, 1/16.0, 0.0, -1.0}});
  	expected.add(new double[][]{{13.0, 10.0, 2.0, 11.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
  		{1/16.0, 1/16.0, 1/16.0, 0.0, -1.0}});
  	expected.add(new double[][]{{7.0, 4.0, 8.0, 17.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
  		{1/16.0, 1/16.0, 1/16.0, 0.0, -1.0}});
  	// Chord 4
  	expected.add(new double[][]{{20.0, 20.0, 14.0, 5.0, 2.0}, {1/8.0, 1/8.0, 1/8.0, 1/8.0, 1/8.0}, 
	  	{0.0, 0.0, 0.0, 0.0, 0.0}});
  	// Chord 5
  	expected.add(new double[][]{{20.0, 20.0, 14.0, 5.0, 0.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0},
  		{1/8.0, 1/8.0, 1/8.0, 1/8.0, 0.0}});
  	expected.add(new double[][]{{8.0, 8.0, 2.0, 7.0, 12.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0},
  		{1/8.0, 1/8.0, 1/8.0, 1/8.0, 0.0}});
  	expected.add(new double[][]{{8.0, 8.0, 2.0, 7.0, 12.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0},
  		{1/8.0, 1/8.0, 1/8.0, 1/8.0, 0.0}});
  	expected.add(new double[][]{{5.0, 5.0, 1.0, 10.0, 15.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
  		{1/8.0, 1/8.0, 1/8.0, 1/8.0, 0.0}});
  	expected.add(new double[][]{{4.0, 4.0, 10.0, 19.0, 24.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
  		{1/8.0, 1/8.0, 1/8.0, 1/8.0, 0.0}});
  	// Chord 6
  	expected.add(new double[][]{{24.0, 15.0, 12.0, 12.0, 0.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0},
  		{0.0, 0.0, 0.0, 0.0, 0.0}});
  	expected.add(new double[][]{{9.0, 0.0, 3.0, 3.0, 15.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0},
  		{0.0, 0.0, 0.0, 0.0, 0.0}});
  	expected.add(new double[][]{{5.0, 4.0, 7.0, 7.0, 19.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
  		{0.0, 0.0, 0.0, 0.0, 0.0}});
  	expected.add(new double[][]{{0.0, 9.0, 12.0, 12.0, 24.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
  		{0.0, 0.0, 0.0, 0.0, 0.0}});
  	// Chord 7
  	expected.add(new double[][]{{5.0, 10.0, 1.0, 2.0, 14.0}, {1/8.0, 1/8.0, 1/8.0, 3/8.0, 1/8.0}, 
  		{0.0, 0.0, 0.0, 1/8.0, 0.0}});
  	expected.add(new double[][]{{4.0, 1.0, 8.0, 11.0, 23.0}, {1/8.0, 1/8.0, 1/8.0, 3/8.0, 1/8.0}, 
  		{0.0, 0.0, 0.0, 1/8.0, 0.0}});
  	// Chord 8
  	expected.add(new double[][]{{23.0, 24.0, 14.0, 12.0, 0.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0}, 
  		{0.0, 1/8.0, 0.0, 1/4.0, 1/8.0}});
  	expected.add(new double[][]{{11.0, 12.0, 2.0, 0.0, 12.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0},
  		{0.0, 1/8.0, 0.0, 1/4.0, 1/8.0}});
  	expected.add(new double[][]{{4.0, 5.0, 5.0, 7.0, 19.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0}, 
  		{0.0, 1/8.0, 0.0, 1/4.0, 1/8.0}});
  	expected.add(new double[][]{{1.0, 0.0, 10.0, 12.0, 24.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0},
  		{0.0, 1/8.0, 0.0, 1/4.0, 1/8.0}});
  	// Chords 9-14
  	expected.add(new double[][]{{1.0, 4.0, 11.0, 23.0, 23.0}, {1/16.0, 1/16.0, 1/16.0, 1/16.0, 5/16.0}, 
  		{0.0, 0.0, 0.0, 0.0, 3/16.0}});
  	expected.add(new double[][]{{1.0, 5.0, 12.0, 24.0, 24.0}, {1/16.0, 1/8.0, 1/8.0, 1/8.0, 3/8.0},
  		{0.0, 1/16.0, 1/16.0, 1/16.0, 1/4.0}});
  	expected.add(new double[][]{{1.0, 4.0, 11.0, 23.0, 23.0}, {1/32.0, 5/32.0, 5/32.0, 5/32.0, 13/32.0}, 
  		{0.0, 3/32.0, 3/32.0, 3/32.0, 9/32.0}});
  	expected.add(new double[][]{{2.0, 2.0, 9.0, 21.0, 21.0}, {1/32.0, 3/16.0, 3/16.0, 3/16.0, 7/16.0}, 
  		{0.0, 1/8.0, 1/8.0, 1/8.0, 5./16.0}});
  	expected.add(new double[][]{{2.0, 4.0, 11.0, 23.0, 23.0}, {1/32.0, 7/32.0, 7/32.0, 7/32.0, 15/32.0}, 
  		{0.0, 5/32.0, 5/32.0, 5/32.0, 11/32.0}});
  	expected.add(new double[][]{{1.0, 5.0, 12.0, 24.0, 24.0}, {1/32.0, 1/4.0, 1/4.0, 1/4.0, 1/2.0}, 
  		{0.0, 3/16.0, 3/16.0, 3/16.0, 3/8.0}});
  	// Chord 15
  	expected.add(new double[][]{{24.0, 19.0, 12.0, 0.0, 0.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 1.0}, 
  		{1/4.0, 11/16.0, 11/16.0, 11/16.0, 7/8.0}});
  	expected.add(new double[][]{{12.0, 7.0, 0.0, 12.0, 12.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 1.0}, 
  		{1/4.0, 11/16.0, 11/16.0, 11/16.0, 7/8.0}});
  	expected.add(new double[][]{{5.0, 0.0, 7.0, 19.0, 19.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 1.0},
  		{1/4.0, 11/16.0, 11/16.0, 11/16.0, 7/8.0}});
  	expected.add(new double[][]{{0.0, 5.0, 12.0, 24.0, 24.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 1.0}, 
  		{1/4.0, 11/16.0, 11/16.0, 11/16.0, 7/8.0}});
  	
    // For all elements of expected: turn the elements in all three Arrays from distances into proximities
  	for (int i = 0; i < expected.size(); i++) {
  		double[][] currentExpected = expected.get(i);
  		for (int j = 0; j < currentExpected.length; j++) {
  			double[] currentArray = currentExpected[j];
  			for (int k = 0; k < currentArray.length; k++) {
  				double oldValue = currentArray[k];
  				// Do only if oldValue is not -1.0, i.e., if the voice is active
  			  if (oldValue != -1.0) {
  				  double newValue = 1.0/(oldValue + 1);
  				  // If oldValue is negative
  				  if (oldValue < 0) {
  				  	newValue = -(1.0/(-oldValue + 1));
  				  }
  			    currentArray[k] = newValue;
  			  }
  			}
  		}
  	}
  	
    // Calculate actual
  	List<double[][]> actual = new ArrayList<double[][]>();
  	Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
  	for (int i = 0; i < basicTabSymbolProperties.length; i++) {
  		Note currentNote = Tablature.convertTabSymbolToNote(basicTabSymbolProperties, i);
  		actual.add(FeatureGenerator.getPitchAndTimeProximitiesToAllVoicesMUSCI(basicTabSymbolProperties, 
  			transcription, currentNote));
  	}
  	
  	assertEquals(expected.size(), actual.size());
  	for (int i = 0; i < expected.size(); i++) { // i = noteIndex
  		assertEquals(expected.get(i).length, actual.get(i).length);
  		for (int j = 0; j < expected.get(i).length; j++) { // j = one of the three proximities Arrays
  			assertEquals(expected.get(i)[j].length, actual.get(i)[j].length);
  			for (int k = 0; k < expected.get(i)[j].length; k++) { // k = the value of element k in the current proximities Array
  				assertEquals(expected.get(i)[j][k], actual.get(i)[j][k]);
  			}
  		}
  	}
	}
	
	
	public void testGetPitchAndTimeProximitiesToAllVoicesNonTabMUSCI() {
  	Transcription transcription = new Transcription(midiTestpiece1, null);

	  // Determine expected
	  List<double[][]> expected = new ArrayList<double[][]>();
	  // Chord 0
	  expected.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},
	  	{-1.0, -1.0, -1.0, -1.0, -1.0}});
	  expected.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},
	  	{-1.0, -1.0, -1.0, -1.0, -1.0}});
	  expected.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
	  	{-1.0, -1.0, -1.0, -1.0, -1.0}});
	  expected.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},
	  	{-1.0, -1.0, -1.0, -1.0, -1.0}});
  	// Chord 1
	  expected.add(new double[][]{{24.0, 20.0, 12.0, 5.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
	  	{0.0, 0.0, 0.0, 0.0, -1.0}});
  	expected.add(new double[][]{{12.0, 8.0, 0.0, 7.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
  		{0.0, 0.0, 0.0, 0.0, -1.0}});
  	expected.add(new double[][]{{0.0, 4.0, 12.0, 19.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
  		{0.0, 0.0, 0.0, 0.0, -1.0}});
  	expected.add(new double[][]{{3.0, 7.0, 15.0, 22.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
  		{0.0, 0.0, 0.0, 0.0, -1.0}});
  	// Chord 2
  	expected.add(new double[][]{{24.0, 21.0, 9.0, 3.0, -1.0}, {3/16.0, 3/16.0, 3/16.0, 3/16.0, -1.0}, 
  		{-1/16.0, 1/16.0, -1/16.0, 0.0, -1.0}});
  	// Chord 3
  	expected.add(new double[][]{{25.0, 22.0, 10.0, 1.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
  		{0.0, 1/8.0, 0.0, 0.0, -1.0}});
  	expected.add(new double[][]{{22.0, 19.0, 7.0, 2.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0},
  		{0.0, 1/8.0, 0.0, 0.0, -1.0}});
  	expected.add(new double[][]{{13.0, 10.0, 2.0, 11.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
  		{0.0, 1/8.0, 0.0, 0.0, -1.0}});
  	expected.add(new double[][]{{7.0, 4.0, 8.0, 17.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
  		{0.0, 1/8.0, 0.0, 0.0, -1.0}});
  	expected.add(new double[][]{{7.0, 4.0, 8.0, 17.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
	  	{0.0, 1/8.0, 0.0, 0.0, -1.0}});
  	// Chord 4
  	expected.add(new double[][]{{20.0, 20.0, 14.0, 5.0, 2.0}, {1/8.0, 1/8.0, 1/8.0, 1/8.0, 1/8.0}, 
	  	{-1/8.0, 0.0, -1/8.0, -1/8.0, 0.0}});
  	// Chord 5
  	expected.add(new double[][]{{20.0, 20.0, 14.0, 5.0, 0.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0},
  		{0.0, 1/8.0, 0.0, 0.0, 0.0}});
  	expected.add(new double[][]{{8.0, 8.0, 2.0, 7.0, 12.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0},
  		{0.0, 1/8.0, 0.0, 0.0, 0.0}});
  	expected.add(new double[][]{{8.0, 8.0, 2.0, 7.0, 12.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0},
  		{0.0, 1/8.0, 0.0, 0.0, 0.0}});
  	expected.add(new double[][]{{5.0, 5.0, 1.0, 10.0, 15.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
  		{0.0, 1/8.0, 0.0, 0.0, 0.0}});
  	expected.add(new double[][]{{4.0, 4.0, 10.0, 19.0, 24.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
  		{0.0, 1/8.0, 0.0, 0.0, 0.0}});
  	// Chord 6
  	expected.add(new double[][]{{24.0, 15.0, 12.0, 12.0, 0.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0},
  		{0.0, 0.0, 0.0, -1/4.0, 0.0}});
  	expected.add(new double[][]{{9.0, 0.0, 3.0, 3.0, 15.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0},
  		{0.0, 0.0, 0.0, -1/4.0, 0.0}});
  	expected.add(new double[][]{{5.0, 4.0, 7.0, 7.0, 19.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
  		{0.0, 0.0, 0.0, -1/4.0, 0.0}});
  	expected.add(new double[][]{{0.0, 9.0, 12.0, 12.0, 24.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
  		{0.0, 0.0, 0.0, -1/4.0, 0.0}});
  	// Chord 7
  	expected.add(new double[][]{{5.0, 10.0, 1.0, 2.0, 14.0}, {1/8.0, 1/8.0, 1/8.0, 3/8.0, 1/8.0}, 
  		{0.0, -1/8.0, 0.0, -1/8.0, -1/8.0}});
  	expected.add(new double[][]{{4.0, 1.0, 8.0, 11.0, 23.0}, {1/8.0, 1/8.0, 1/8.0, 3/8.0, 1/8.0}, 
  		{0.0, -1/8.0, 0.0, -1/8.0, -1/8.0}});
  	// Chord 8
  	expected.add(new double[][]{{23.0, 24.0, 14.0, 12.0, 0.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0}, 
  		{0.0, 0.0, 0.0, 0.0, 0.0}});
  	expected.add(new double[][]{{11.0, 12.0, 2.0, 0.0, 12.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0},
  		{0.0, 0.0, 0.0, 0.0, 0.0}});
  	expected.add(new double[][]{{4.0, 5.0, 5.0, 7.0, 19.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0}, 
  		{0.0, 0.0, 0.0, 0.0, 0.0}});
  	expected.add(new double[][]{{1.0, 0.0, 10.0, 12.0, 24.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0},
  		{0.0, 0.0, 0.0, 0.0, 0.0}});
  	// Chords 9-14
  	expected.add(new double[][]{{1.0, 4.0, 11.0, 23.0, 23.0}, {1/16.0, 1/16.0, 1/16.0, 1/16.0, 5/16.0}, 
  		{0.0, -7/16.0, -7/16.0, -7/16.0, 1/16.0}});
  	expected.add(new double[][]{{1.0, 5.0, 12.0, 24.0, 24.0}, {1/16.0, 1/8.0, 1/8.0, 1/8.0, 3/8.0},
  		{0.0, -3/8.0, -3/8.0, -3/8.0, 1/8.0}});
  	expected.add(new double[][]{{1.0, 4.0, 11.0, 23.0, 23.0}, {1/32.0, 5/32.0, 5/32.0, 5/32.0, 13/32.0}, 
  		{0.0, -11/32.0, -11/32.0, -11/32.0, 5/32.0}});
  	expected.add(new double[][]{{2.0, 2.0, 9.0, 21.0, 21.0}, {1/32.0, 3/16.0, 3/16.0, 3/16.0, 7/16.0}, 
  		{0.0, -5/16.0, -5/16.0, -5/16.0, 3/16.0}});
  	expected.add(new double[][]{{2.0, 4.0, 11.0, 23.0, 23.0}, {1/32.0, 7/32.0, 7/32.0, 7/32.0, 15/32.0}, 
  		{0.0, -9/32.0, -9/32.0, -9/32.0, 7/32.0}});
  	expected.add(new double[][]{{1.0, 5.0, 12.0, 24.0, 24.0}, {1/32.0, 1/4.0, 1/4.0, 1/4.0, 1/2.0}, 
  		{0.0, -1/4.0, -1/4.0, -1/4.0, 1/4.0}});
  	// Chord 15
  	expected.add(new double[][]{{24.0, 19.0, 12.0, 0.0, 0.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 1.0}, 
  		{1/4.0, 1/4.0, 1/4.0, 1/4.0, 3/4.0}});
  	expected.add(new double[][]{{12.0, 7.0, 0.0, 12.0, 12.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 1.0}, 
  		{1/4.0, 1/4.0, 1/4.0, 1/4.0, 3/4.0}});
  	expected.add(new double[][]{{5.0, 0.0, 7.0, 19.0, 19.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 1.0},
  		{1/4.0, 1/4.0, 1/4.0, 1/4.0, 3/4.0}});
  	expected.add(new double[][]{{0.0, 5.0, 12.0, 24.0, 24.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 1.0}, 
  		{1/4.0, 1/4.0, 1/4.0, 1/4.0, 3/4.0}});
  	
 	  // For all elements of expected: turn the elements in all three Arrays from distances into proximities
    for (int i = 0; i < expected.size(); i++) {
    	double[][] currentExpected = expected.get(i);
    	for (int j = 0; j < currentExpected.length; j++) {
    		double[] currentArray = currentExpected[j];
    		for (int k = 0; k < currentArray.length; k++) {
    			double oldValue = currentArray[k];
    			// Do only if oldValue is not -1.0, i.e., if the voice is active
    		  if (oldValue != -1.0) {
  				  double newValue = 1.0/(oldValue + 1);
  				  // If oldValue is negative 
  				  if (oldValue < 0) {
  				  	newValue = -(1.0/(-oldValue + 1));
  				  }
    		    currentArray[k] = newValue;
    		  }
    		}
    	}
    }
  	
    // Calculate actual
	  List<double[][]> actual = new ArrayList<double[][]>();
	  NoteSequence noteSeq = transcription.getNoteSequence();
    for (int i = 0; i < noteSeq.size(); i++) {
    	Note currentNote = noteSeq.get(i);
    	actual.add(FeatureGenerator.getPitchAndTimeProximitiesToAllVoicesMUSCI(null, transcription, currentNote));
    }
  
	  // Assert equality
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) { // i = onsetindex
    	assertEquals(expected.get(i).length, actual.get(i).length);
    	for (int j = 0; j < expected.get(i).length; j++) { // j = one of the three proximities Arrays
    		assertEquals(expected.get(i)[j].length, actual.get(i)[j].length);
    		for (int k = 0; k < expected.get(i)[j].length; k++) { // k = the value of element k in the current proximities Array
    			assertEquals(expected.get(i)[j][k], actual.get(i)[j][k]);
    		}
    	}
    }
	}
  
	
	public void testGetProximitiesAndMovementToVoiceMUSCI() {
    Tablature tablature = new Tablature(encodingTestpiece1, true);
  	Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);
  	
    // Determine expected
  	List<double[]> expected = new ArrayList<double[]>();
  	// Chord 0
  	expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
  	expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
  	expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
  	expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
    // Chord 1
  	expected.add(new double[]{5.0, 1/4.0, 0.0, -5.0});
  	expected.add(new double[]{0.0, 1/4.0, 0.0, 0.0});
  	expected.add(new double[]{3.0, 1/4.0, 0.0, 3.0});
  	expected.add(new double[]{4.0, 1/4.0, 0.0, 4.0});
    // Chord 2
  	expected.add(new double[]{3.0, 3/16.0, 0.0, 3.0});
    // Chord 3
    expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
    expected.add(new double[]{2.0, 1/16.0, 0.0, 2.0});
    expected.add(new double[]{2.0, 1/4.0, 1/16.0, 2.0});
    expected.add(new double[]{7.0, 1/4.0, 1/16.0, -7.0});
    expected.add(new double[]{4.0, 1/4.0, 1/16.0, -4.0});
    // Chord 4
    expected.add(new double[]{2.0, 1/8.0, 0.0, -2.0});
    // Chord 5
  	expected.add(new double[]{0.0, 1/8.0, 0.0, 0.0});
  	expected.add(new double[]{7.0, 1/4.0, 1/8.0, 7.0});
  	expected.add(new double[]{2.0, 1/4.0, 1/8.0, -2.0});
  	expected.add(new double[]{5.0, 1/4.0, 1/8.0, -5.0});
  	expected.add(new double[]{4.0, 1/4.0, 1/8.0, 4.0});
    // Chord 6
  	expected.add(new double[]{0.0, 1/4.0, 0.0, 0.0});
  	expected.add(new double[]{3.0, 1/4.0, 0.0, 3.0});
  	expected.add(new double[]{5.0, 1/4.0, 0.0, -5.0});
  	expected.add(new double[]{9.0, 1/4.0, 0.0, 9.0});
    // Chord 7
  	expected.add(new double[]{1.0, 1/8.0, 0.0, -1.0});
  	expected.add(new double[]{4.0, 1/8.0, 0.0, 4.0});
    // Chord 8
  	expected.add(new double[]{12.0, 1/2.0, 1/4.0, -12.0});
  	expected.add(new double[]{2.0, 1/8.0, 0.0, -2.0});
  	expected.add(new double[]{5.0, 1/4.0, 1/8.0, -5.0});
  	expected.add(new double[]{1.0, 1/8.0, 0.0, 1.0});
  	// Chords 9-14
  	expected.add(new double[]{1.0, 1/16.0, 0.0, -1.0});
  	expected.add(new double[]{1.0, 1/16.0, 0.0, 1.0});
  	expected.add(new double[]{1.0, 1/32.0, 0.0, -1.0});
  	expected.add(new double[]{2.0, 1/32.0, 0.0, -2.0});
  	expected.add(new double[]{2.0, 1/32.0, 0.0, 2.0});
  	expected.add(new double[]{1.0, 1/32.0, 0.0, 1.0});
    // Chord 15
	  expected.add(new double[]{0.0, 3/4.0, 11/16.0, 0.0});
 	  expected.add(new double[]{0.0, 3/4.0, 11/16.0, 0.0});
 	  expected.add(new double[]{0.0, 3/4.0, 11/16.0, 0.0});
 	  expected.add(new double[]{0.0, 2/4.0, 1/4.0, 0.0});
  	
    // For each element of expected: turn the first three elements from distances into proximities
  	for (int i = 0; i < expected.size(); i++) {
  		double[] currentArray = expected.get(i);
  		for (int j = 0; j < currentArray.length - 1; j++) {
  			double oldValue = currentArray[j]; 
  		  // Do only if oldValue is not -1.0, i.e., if the voice is active
			  if (oldValue != -1.0) {
			    double newValue = 1.0/(oldValue + 1);
				  // If oldValue is negative
				  if (oldValue < 0) {
				  	newValue = -(1.0/(-oldValue + 1));
				  }
			    currentArray[j] = newValue;
			  }
  		}
  	}
  	
    // Calculate actual
  	List<double[]> actual = new ArrayList<double[]>();
  	Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
  	List<List<Double>> voiceLabels = transcription.getVoiceLabels();
  	for (int i = 0; i < basicTabSymbolProperties.length; i++) {
  		Note currentNote = Tablature.convertTabSymbolToNote(basicTabSymbolProperties, i);
  		List<Double> currentLabel = voiceLabels.get(i);
  		List<Integer> currentVoices = 
  			DataConverter.convertIntoListOfVoices(currentLabel);
  		// For each voice (in case of a CoD, the highest, i.e., the voice with the lowest number, will be dealt with first)
  		for (int j = 0; j < currentVoices.size(); j++) {
  	    int currentVoice = currentVoices.get(j);
  	    NotationVoice currentVoiceToCompareTo = transcription.getPiece().getScore().get(currentVoice).get(0);
  	    actual.add(FeatureGenerator.getProximitiesAndMovementToVoiceMUSCI(basicTabSymbolProperties, 
  				currentVoiceToCompareTo, currentNote));
  		}
  	}
  	
  	// Assert equality
  	assertEquals(expected.size(), actual.size());
  	for (int i = 0; i < expected.size(); i++) {
  		assertEquals(expected.get(i).length, actual.get(i).length);
  	 	for (int j = 0; j < expected.get(i).length; j++) {
  	 		assertEquals(expected.get(i)[j], actual.get(i)[j]);
  	 	}
  	}
	}
	
	
	public void testGetProximitiesAndMovementToVoiceNonTabMUSCI() {    
    Transcription transcription = new Transcription(midiTestpiece1, null);

    // Determine expected
  	List<double[]> expected = new ArrayList<double[]>();
  	// Chord 0
  	expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
  	expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
  	expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
  	expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
    // Chord 1
  	expected.add(new double[]{5.0, 1/4.0, 0.0, -5.0});
  	expected.add(new double[]{0.0, 1/4.0, 0.0, 0.0});
  	expected.add(new double[]{4.0, 1/4.0, 0.0, 4.0});
  	expected.add(new double[]{3.0, 1/4.0, 0.0, 3.0});
    // Chord 2
  	expected.add(new double[]{3.0, 3/16.0, 0.0, 3.0});
    // Chord 3
  	expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
  	expected.add(new double[]{2.0, 1/16.0, 0.0, 2.0});
  	expected.add(new double[]{2.0, 1/4.0, 0.0, 2.0});
  	expected.add(new double[]{7.0, 1/4.0, 0.0, -7.0});
  	expected.add(new double[]{4.0, 1/4.0, 1/8.0, -4.0});
    // Chord 4
  	expected.add(new double[]{2.0, 1/8.0, 0.0, -2.0});
  	// Chord 5
  	expected.add(new double[]{0.0, 1/8.0, 0.0, 0.0});
  	expected.add(new double[]{7.0, 1/4.0, 0.0, 7.0});
  	expected.add(new double[]{2.0, 1/4.0, 0.0, -2.0});
  	expected.add(new double[]{5.0, 1/4.0, 1/8.0, -5.0});
  	expected.add(new double[]{4.0, 1/4.0, 0.0, 4.0});
    // Chord 6
  	expected.add(new double[]{0.0, 1/4.0, 0.0, 0.0});
  	expected.add(new double[]{3.0, 1/4.0, 0.0, 3.0});
  	expected.add(new double[]{5.0, 1/4.0, 0.0, -5.0});
  	expected.add(new double[]{9.0, 1/4.0, 0.0, 9.0});
    // Chord 7
  	expected.add(new double[]{1.0, 1/8.0, 0.0, -1.0});
  	expected.add(new double[]{4.0, 1/8.0, 0.0, 4.0});
    // Chord 8
  	expected.add(new double[]{12.0, 1/2.0, 0.0, -12.0});
  	expected.add(new double[]{2.0, 1/8.0, 0.0, -2.0});
  	expected.add(new double[]{5.0, 1/4.0, 0.0, -5.0});
  	expected.add(new double[]{1.0, 1/8.0, 0.0, 1.0});
    // Chords 9-14
  	expected.add(new double[]{1.0, 1/16.0, 0.0, -1.0});
  	expected.add(new double[]{1.0, 1/16.0, 0.0, 1.0});
  	expected.add(new double[]{1.0, 1/32.0, 0.0, -1.0});
  	expected.add(new double[]{2.0, 1/32.0, 0.0, -2.0});
  	expected.add(new double[]{2.0, 1/32.0, 0.0, 2.0});
  	expected.add(new double[]{1.0, 1/32.0, 0.0, 1.0});
  	// Chord 15
  	expected.add(new double[]{0.0, 3/4.0, 1/4.0, 0.0});
  	expected.add(new double[]{0.0, 3/4.0, 1/4.0, 0.0});
  	expected.add(new double[]{0.0, 3/4.0, 1/4.0, 0.0});
  	expected.add(new double[]{0.0, 2/4.0, 1/4.0, 0.0});
  	
    // For each element of expected: turn the first three elements from distances into proximities
  	for (int i = 0; i < expected.size(); i++) {
  		double[] currentArray = expected.get(i);
  		for (int j = 0; j < currentArray.length - 1; j++) {
  			double oldValue = currentArray[j]; 
  		  // Do only if oldValue is not -1.0, i.e., if the voice is active
			  if (oldValue != -1.0) {
				  double newValue = 1.0/(oldValue + 1);
				  // If oldValue is negative
				  if (oldValue < 0) {
				  	newValue = -(1.0/(-oldValue + 1));
				  }
			    currentArray[j] = newValue;
			  }
  		}
  	}
  	
    // Calculate actual
  	List<double[]> actual = new ArrayList<double[]>();
  	Integer[][] basicTabSymbolProperties = null;
  	Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
  	List<List<Double>> voiceLabels = transcription.getVoiceLabels();
  	NoteSequence noteSeq = transcription.getNoteSequence();
  	for (int i = 0; i < basicNoteProperties.length; i++) {
  		Note currentNote = noteSeq.getNoteAt(i);
  		List<Double> currentLabel = voiceLabels.get(i);
  		List<Integer> currentVoices = 
  			DataConverter.convertIntoListOfVoices(currentLabel);
  		// For each voice assigned to the Note
  		// NB: currentVoices.size() will always be 1, as CoDs don't occur in the non-tablature case
  		for (int j = 0; j < currentVoices.size(); j++) {
  	    int currentVoice = currentVoices.get(j);
  	    NotationVoice currentVoiceToCompareTo = transcription.getPiece().getScore().get(currentVoice).get(0);
  	    actual.add(FeatureGenerator.getProximitiesAndMovementToVoiceMUSCI(basicTabSymbolProperties, 
  				currentVoiceToCompareTo, currentNote));
  		}
  	}
  	
  	// Assert equality
  	assertEquals(expected.size(), actual.size());
  	for (int i = 0; i < expected.size(); i++) {
  		assertEquals(expected.get(i).length, actual.get(i).length);
  	 	for (int j = 0; j < expected.get(i).length; j++) {
  	 		assertEquals(expected.get(i)[j], actual.get(i)[j]);
  	 	}
  	}
	}
	
	
	public void testGetVoicesAlreadyOccupiedMUSCI() {
    Tablature tablature = new Tablature(encodingTestpiece1, true);
  	Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

    // Determine expected
    List<List<Double>> expected = new ArrayList<List<Double>>();
    // Chord 0
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
    // Chord 1
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{1.0, 0.0, 1.0, 1.0, 0.0}));
    // Chord 2
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    // Chord 3
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 1.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 1.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 1.0}));
    // Chord 4
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    // Chord 5
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 1.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 1.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 1.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 1.0}));
    // Chord 6
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 1.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 0.0, 1.0}));
    expected.add(Arrays.asList(new Double[]{1.0, 0.0, 1.0, 0.0, 1.0}));
    // Chord 7
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 0.0, 0.0}));
    // Chord 8
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
    // Chords 9-14
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    // Chord 15
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
    	
    // Calculate actual
  	List<List<Double>> actual = new ArrayList<List<Double>>();
  	Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
  	for (int i = 0; i < basicTabSymbolProperties.length; i++) {
  		actual.add(FeatureGenerator.getVoicesAlreadyOccupiedMUSCI(basicTabSymbolProperties, null, i, transcription));
  	}
  	
  	// Assert equality
  	assertEquals(expected.size(), actual.size());
  	for (int i = 0; i < expected.size(); i++) {
  		assertEquals(expected.get(i).size(), actual.get(i).size());
  		for (int j = 0; j < expected.get(i).size(); j++) {
  			assertEquals(expected.get(i).get(j), actual.get(i).get(j));
  		}
  	}
  	assertEquals(expected, actual);
	}
	
	
	public void testGetVoicesAlreadyOccupiedNonTabMUSCI() {
  	Transcription transcription = new Transcription(midiTestpiece1, null);
	
    // Determine expected
    List<List<Double>> expected = new ArrayList<List<Double>>();
    // Chord 0
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
    // Chord 1
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
    // Chord 2
    expected.add(Arrays.asList(new Double[]{1.0, 0.0, 1.0, 0.0, 0.0}));
    // Chord 3
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 1.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 1.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 1.0}));
    expected.add(Arrays.asList(new Double[]{1.0, 0.0, 1.0, 1.0, 1.0}));
    // Chord 4
    expected.add(Arrays.asList(new Double[]{1.0, 0.0, 1.0, 1.0, 0.0}));
    // Chord 5
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 1.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 1.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 1.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 1.0}));
    // Chord 6
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 1.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 1.0}));
    expected.add(Arrays.asList(new Double[]{1.0, 0.0, 1.0, 1.0, 1.0}));
    // Chord 7 
    expected.add(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 1.0, 1.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 1.0}));
    // Chord 8
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
    // Chords 9-14
    expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
    // Chord 15
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0}));
    expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
  
    // Calculate actual
    List<List<Double>> actual = new ArrayList<List<Double>>();
    Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
    for (int i = 0; i < basicNoteProperties.length; i++) {
    	actual.add(FeatureGenerator.getVoicesAlreadyOccupiedMUSCI(null, basicNoteProperties, i, transcription));
    }
  
    // Assert equality
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
    	assertEquals(expected.get(i).size(), actual.get(i).size());
    	for (int j = 0; j < expected.get(i).size(); j++) {
    		assertEquals(expected.get(i).get(j), actual.get(i).get(j));
    	}
    }
    assertEquals(expected, actual);
	}
	
	
	public void testGenerateAllNoteFeatureVectorsMUSCI() {
    Tablature tablature = new Tablature(encodingTestpiece1, true);
    Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

  	Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
  	
    // For each featureSet: determine the expected note feature vectors and add them to expected
  	List<List<List<Double>>> expected = new ArrayList<List<List<Double>>>();
  	List<List<Double>> expectedFeatureSetA = new ArrayList<List<Double>>();
	  List<List<Double>> expectedFeatureSetB = new ArrayList<List<Double>>();
	  List<List<Double>> expectedFeatureSetC = new ArrayList<List<Double>>();
	  List<List<Double>> expectedFeatureSetD = new ArrayList<List<Double>>();
  	for (int i = 0; i < basicTabSymbolProperties.length; i++) {
  	  // 1. Determine the expected note features for featureSet D
  		List<Double> currentExpectedFeatureSetD = new ArrayList<Double>(); 
  	  double[] currentBasicOnsetFeatures = 
  	  	FeatureGenerator.getBasicNoteFeaturesMUSCI(basicTabSymbolProperties, null, i);
  	  for (int j = 0; j < currentBasicOnsetFeatures.length; j++) {
  	  	currentExpectedFeatureSetD.add(currentBasicOnsetFeatures[j]);
  	  }
  	  double[] currentPositionWithinChord = 
  	  	FeatureGenerator.getPositionWithinChordMUSCI(basicTabSymbolProperties, null, i);
  	  for (int j = 0; j < currentPositionWithinChord.length; j++) {
  	  	currentExpectedFeatureSetD.add(currentPositionWithinChord[j]);
  	  }
  	  double[] currentPitchDistancesWithinChord = 
  	  	FeatureGenerator.getPitchDistancesWithinChordMUSCI(basicTabSymbolProperties, null, i);
  	  for (int j = 0; j < currentPitchDistancesWithinChord.length; j++) {
  	  	currentExpectedFeatureSetD.add(currentPitchDistancesWithinChord[j]);
  	  }
  	  Note currentNote = Tablature.convertTabSymbolToNote(basicTabSymbolProperties, i);
  	  double[][] currentPitchAndTimeProximities = 
  	  	FeatureGenerator.getPitchAndTimeProximitiesToAllVoicesMUSCI(basicTabSymbolProperties, transcription, currentNote);	
  	  for (int j = 0; j < currentPitchAndTimeProximities.length; j++) {
  	  	for (int k = 0; k < currentPitchAndTimeProximities[j].length; k++) {
  	  		currentExpectedFeatureSetD.add(currentPitchAndTimeProximities[j][k]);
  	  	}
  	  }
  	  List<Double> currentVoicesAlreadyOccupied = 
  	  	FeatureGenerator.getVoicesAlreadyOccupiedMUSCI(basicTabSymbolProperties, null, i, transcription);
  	  currentExpectedFeatureSetD.addAll(currentVoicesAlreadyOccupied);
  	  
  	  // 2. Use currentExpectedFeatureSetD to determine the expected onset features for featureSets A-C; then add 
  	  // everything to the appropriate Lists and add those to expected
  	  List<Double> currentExpectedFeatureSetA = 
  	  	new ArrayList<Double>(currentExpectedFeatureSetD.subList(0, NUM_FEATURES_SET_A));
  	  List<Double> currentExpectedFeatureSetB = 
    	  	new ArrayList<Double>(currentExpectedFeatureSetD.subList(0, NUM_FEATURES_SET_B));
  	  List<Double> currentExpectedFeatureSetC = 
    	  	new ArrayList<Double>(currentExpectedFeatureSetD.subList(0, NUM_FEATURES_SET_C));
  	  
  	  expectedFeatureSetA.add(currentExpectedFeatureSetA); 
  	  expectedFeatureSetB.add(currentExpectedFeatureSetB);
  	  expectedFeatureSetC.add(currentExpectedFeatureSetC); 
  	  expectedFeatureSetD.add(currentExpectedFeatureSetD);
  	}
  	expected.add(expectedFeatureSetA); expected.add(expectedFeatureSetB);
  	expected.add(expectedFeatureSetC); expected.add(expectedFeatureSetD);
  	  
    // For each featureSet: determine the actual note feature vectors and add them to actual
  	List<List<List<Double>>> actual = new ArrayList<List<List<Double>>>();
    for (int i = FeatureGenerator.FEATURE_SET_A; i <= FeatureGenerator.FEATURE_SET_D; i++) {
	  	int currentFeatureSet = i;  	  		
    	actual.add(FeatureGenerator.generateAllNoteFeatureVectorsMUSCI(basicTabSymbolProperties,
    		null, transcription, currentFeatureSet, true));	  
    }
    
    // Assert equality
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
	
	
	public void testGenerateAllNoteFeatureVectorsNonTabMUSCI() {
    Transcription transcription = new Transcription(midiTestpiece1, null);

  	Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
  	NoteSequence noteSeq = transcription.getNoteSequence();
  	
    // For each featureSet: determine the expected note feature vectors and add them to expected
  	List<List<List<Double>>> expected = new ArrayList<List<List<Double>>>();
  	List<List<Double>> expectedFeatureSetA = new ArrayList<List<Double>>();
	  List<List<Double>> expectedFeatureSetB = new ArrayList<List<Double>>();
	  List<List<Double>> expectedFeatureSetC = new ArrayList<List<Double>>();
	  List<List<Double>> expectedFeatureSetD = new ArrayList<List<Double>>();
  	for (int i = 0; i < basicNoteProperties.length; i++) {
  	  // 1. Determine the expected onset features for featureSet D
  		List<Double> currentExpectedFeatureSetD = new ArrayList<Double>(); 
  	  double[] currentBasicNoteFeatures = 
  	  	FeatureGenerator.getBasicNoteFeaturesMUSCI(null, basicNoteProperties, i);
  	  for (int j = 0; j < currentBasicNoteFeatures.length; j++) {
  	  	currentExpectedFeatureSetD.add(currentBasicNoteFeatures[j]);
  	  }
  	  double[] currentPositionWithinChord = 
  	  	FeatureGenerator.getPositionWithinChordMUSCI(null, basicNoteProperties, i);
  	  for (int j = 0; j < currentPositionWithinChord.length; j++) {
  	  	currentExpectedFeatureSetD.add(currentPositionWithinChord[j]);
  	  }
  	  double[] currentPitchDistancesWithinChord = 
  	  	FeatureGenerator.getPitchDistancesWithinChordMUSCI(null, basicNoteProperties, i);
  	  for (int j = 0; j < currentPitchDistancesWithinChord.length; j++) {
  	  	currentExpectedFeatureSetD.add(currentPitchDistancesWithinChord[j]);
  	  }
  	  Note currentNote = noteSeq.getNoteAt(i);
  	  double[][] currentPitchAndTimeProximities = 
  	  	FeatureGenerator.getPitchAndTimeProximitiesToAllVoicesMUSCI(null, transcription, currentNote);	
  	  for (int j = 0; j < currentPitchAndTimeProximities.length; j++) {
  	  	for (int k = 0; k < currentPitchAndTimeProximities[j].length; k++) {
  	  		currentExpectedFeatureSetD.add(currentPitchAndTimeProximities[j][k]);
  	  	}
  	  }
  	  List<Double> currentVoicesAlreadyOccupied = 
  	  	FeatureGenerator.getVoicesAlreadyOccupiedMUSCI(null, basicNoteProperties, i, transcription);
  	  currentExpectedFeatureSetD.addAll(currentVoicesAlreadyOccupied);
  	  
  	  // 2. Use currentExpectedFeatureSetD to determine the expected note features for featureSets A-C; then 
  	  // add everything to the appropriate Lists and add those to expected
  	  List<Double> currentExpectedFeatureSetA = 
  	  	new ArrayList<Double>(currentExpectedFeatureSetD.subList(0, NUM_FEATURES_SET_A_NON_TAB));
  	  List<Double> currentExpectedFeatureSetB = 
    	  	new ArrayList<Double>(currentExpectedFeatureSetD.subList(0, NUM_FEATURES_SET_B_NON_TAB));
  	  List<Double> currentExpectedFeatureSetC = 
    	  	new ArrayList<Double>(currentExpectedFeatureSetD.subList(0, NUM_FEATURES_SET_C_NON_TAB));
  	  
  	  expectedFeatureSetA.add(currentExpectedFeatureSetA); 
  	  expectedFeatureSetB.add(currentExpectedFeatureSetB);
  	  expectedFeatureSetC.add(currentExpectedFeatureSetC); 
  	  expectedFeatureSetD.add(currentExpectedFeatureSetD);
  	}
  	expected.add(expectedFeatureSetA); expected.add(expectedFeatureSetB);
  	expected.add(expectedFeatureSetC); expected.add(expectedFeatureSetD);
  	  
    // For each featureSet: determine the actual note feature vectors and add them to actual
  	List<List<List<Double>>> actual = new ArrayList<List<List<Double>>>();
    for (int i = FeatureGenerator.FEATURE_SET_A; i <= FeatureGenerator.FEATURE_SET_D; i++) {
	  	int currentFeatureSet = i;
    	actual.add(FeatureGenerator.generateAllNoteFeatureVectorsMUSCI(null, basicNoteProperties, 
    		transcription, currentFeatureSet, true));	  
    }
    
    // Assert equality
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
	
	
	public void testGetBasicNoteFeatures() {
		Tablature tablature = new Tablature(encodingTestpiece1, false);

		List<double[]> allInfoAhead = new ArrayList<double[]>();
		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
		for (int i = 0; i < basicTabSymbolProperties.length; i++) {
			allInfoAhead.add(FeatureGenerator.getProximitiesAndCourseInfoAhead(basicTabSymbolProperties, null, i, 
				FeatureGenerator.NUM_NEXT_CHORDS));
		}

		List<double[]> expected = new ArrayList<double[]>();
		// Chord 0
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{50.0, 5.0, 0.0, 1/4.0, 1/2.0, 0.0, 3/4.0}, allInfoAhead.get(0), new double[]{4.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{57.0, 4.0, 2.0, 1/4.0, 1/4.0, 0.0, 3/4.0}, allInfoAhead.get(1), new double[]{4.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{65.0, 2.0, 1.0, 1/4.0, 1/4.0, 0.0, 3/4.0}, allInfoAhead.get(2), new double[]{4.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{69.0, 1.0, 0.0, 1/4.0, 1/4.0, 0.0, 3/4.0}, allInfoAhead.get(3), new double[]{4.0}})));
		// Chord 1
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{45.0, 6.0, 0.0, 3/16.0, 3/16.0, 0.0, 0/32.0}, allInfoAhead.get(4), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{57.0, 4.0, 2.0, 3/16.0, 1/4.0, 0.0, 0/32.0}, allInfoAhead.get(5), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{72.0, 2.0, 8.0, 3/16.0, 1/4.0, 0.0, 0/32.0}, allInfoAhead.get(6), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{69.0, 1.0, 0.0, 3/16.0, 3/4.0, 0.0, 0/32.0}, allInfoAhead.get(7), new double[]{1.0}})));
		// Chord 2
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{48.0, 6.0, 3.0, 1/16.0, 1/16.0, 1.0, 3/16.0}, allInfoAhead.get(8), new double[]{4.0}})));
		// Chord 3
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{47.0, 6.0, 2.0, 1/8.0, 1/8.0, 0.0, 1/4.0}, allInfoAhead.get(9), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{50.0, 5.0, 0.0, 1/8.0, 1/4.0, 0.0, 1/4.0}, allInfoAhead.get(10), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{59.0, 4.0, 4.0, 1/8.0, 1/4.0, 0.0, 1/4.0}, allInfoAhead.get(11), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{65.0, 2.0, 1.0, 1/8.0, 1/4.0, 0.0, 1/4.0}, allInfoAhead.get(12), new double[]{1.0}})));
		// Chord 4
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{45.0, 6.0, 0.0, 1/8.0, 1/8.0, 0.0, 3/8.0}, allInfoAhead.get(13), new double[]{5.0}})));
		// Chord 5
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{45.0, 6.0, 0.0, 1/4.0, 1/4.0, 0.0, 1/2.0}, allInfoAhead.get(14), new double[]{4.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{57.0, 5.0, 7.0, 1/4.0, 3/2.0, 0.0, 1/2.0}, allInfoAhead.get(15), new double[]{4.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{57.0, 4.0, 2.0, 1/4.0, 1/2.0, 0.0, 1/2.0}, allInfoAhead.get(16), new double[]{4.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{60.0, 3.0, 1.0, 1/4.0, 1/4.0, 0.0, 1/2.0}, allInfoAhead.get(17), new double[]{4.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{69.0, 2.0, 5.0, 1/4.0, 1/4.0, 0.0, 1/2.0}, allInfoAhead.get(18), new double[]{4.0}})));
		// Chord 6
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{45.0, 6.0, 0.0, 1/8.0, 1/4.0, 0.0, 3/4.0}, allInfoAhead.get(19), new double[]{2.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{60.0, 3.0, 1.0, 1/8.0, 1/8.0, 0.0, 3/4.0}, allInfoAhead.get(20), new double[]{2.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{64.0, 2.0, 0.0, 1/8.0, 1/8.0, 0.0, 3/4.0}, allInfoAhead.get(21), new double[]{2.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{69.0, 1.0, 0.0, 1/8.0, 1/4.0, 0.0, 3/4.0}, allInfoAhead.get(22), new double[]{2.0}})));
		// Chord 7
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{59.0, 3.0, 0.0, 1/8.0, 9/8.0, 0.0, 7/8.0}, allInfoAhead.get(23), new double[]{4.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{68.0, 2.0, 4.0, 1/8.0, 1/8.0, 0.0, 7/8.0}, allInfoAhead.get(24), new double[]{4.0}})));
		// Chord 8
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{45.0, 6.0, 0.0, 1/16.0, 3/4.0, 0.0, 0/32.0}, allInfoAhead.get(25), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{57.0, 4.0, 2.0, 1/16.0, 3/4.0, 0.0, 0/32.0}, allInfoAhead.get(26), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{64.0, 2.0, 0.0, 1/16.0, 1/16.0, 0.0, 0/32.0}, allInfoAhead.get(27), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{69.0, 1.0, 0.0, 1/16.0, 1/8.0, 0.0, 0/32.0}, allInfoAhead.get(28), new double[]{1.0}})));
		// Chords 9-14
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{68.0, 2.0, 4.0, 1/16.0, 3/32.0, 1.0, 1/16.0}, allInfoAhead.get(29), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{69.0, 1.0, 0.0, 1/32.0, 1/8.0, 1.0, 1/8.0}, allInfoAhead.get(30), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{68.0, 2.0, 4.0, 1/32.0, 1/32.0, 1.0, 5/32.0}, allInfoAhead.get(31), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{66.0, 2.0, 2.0, 1/32.0, 1/32.0, 1.0, 3/16.0}, allInfoAhead.get(32), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{68.0, 2.0, 4.0, 1/32.0, 17/32.0, 1.0, 7/32.0}, allInfoAhead.get(33), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{69.0, 1.0, 0.0, 1/4.0, 1/2.0, 0.0, 1/4.0}, allInfoAhead.get(34), new double[]{4.0}})));
		// Chord 15
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{45.0, 6.0, 0.0, 1/4.0, 1/4.0, 0.0, 3/4.0}, allInfoAhead.get(35), new double[]{-1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{57.0, 4.0, 2.0, 1/4.0, 1/4.0, 0.0, 3/4.0}, allInfoAhead.get(36), new double[]{-1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{64.0, 2.0, 0.0, 1/4.0, 1/4.0, 0.0, 3/4.0}, allInfoAhead.get(37), new double[]{-1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{
			new double[]{69.0, 1.0, 0.0, 1/4.0, 1/4.0, 0.0, 3/4.0}, allInfoAhead.get(38), new double[]{-1.0}})));

		List<double[]> actual = new ArrayList<double[]>();
		List<Integer[]> meterInfo = tablature.getMeterInfo();
		for (int i = 0; i < basicTabSymbolProperties.length; i++) {
			actual.add(FeatureGenerator.getBasicNoteFeatures(basicTabSymbolProperties, null, meterInfo, i));
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j]);
			}
		}
	}
	
	
	public void testGetBasicNoteFeaturesNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		List<double[]> allInfoAhead = new ArrayList<double[]>();
		Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
		for (int i = 0; i < basicNoteProperties.length; i++) {
			allInfoAhead.add(FeatureGenerator.getProximitiesAndCourseInfoAhead(null, basicNoteProperties, i, 
		 		FeatureGenerator.NUM_NEXT_CHORDS));
		}

		List<double[]> expected = new ArrayList<double[]>();
		// Chord 0
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{50.0, 1/4.0, 0.0, 3/4.0},
			allInfoAhead.get(0), new double[]{4.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{57.0, 1/4.0, 0.0, 3/4.0}, 
			allInfoAhead.get(1), new double[]{4.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{65.0, 1/4.0, 0.0, 3/4.0},
			allInfoAhead.get(2), new double[]{4.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{69.0, 1/4.0, 0.0, 3/4.0},
			allInfoAhead.get(3), new double[]{4.0}})));
		// Chord 1
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{45.0, 3/16.0, 0.0, 0.0},
			allInfoAhead.get(4), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{57.0, 1/4.0, 0.0, 0.0},
			allInfoAhead.get(5), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{69.0, 1/8.0, 0.0, 0.0},
			allInfoAhead.get(6), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{72.0, 1/4.0, 0.0, 0.0},
			allInfoAhead.get(7), new double[]{1.0}})));
		// Chord 2
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{48.0, 1/16.0, 1.0, 3/16.0},
			allInfoAhead.get(8), new double[]{5.0}})));
		// Chord 3
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{47.0, 1/8.0, 0.0, 1/4.0},
			allInfoAhead.get(9), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{50.0, 1/4.0, 0.0, 1/4.0},
			allInfoAhead.get(10), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{59.0, 1/4.0, 0.0, 1/4.0},
			allInfoAhead.get(11), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{65.0, 1/4.0, 0.0, 1/4.0},
			allInfoAhead.get(12), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{65.0, 1/8.0, 0.0, 1/4.0},
			allInfoAhead.get(13), new double[]{1.0}})));
		// Chord 4
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{45.0, 1/8.0, 0.0, 3/8.0},
			allInfoAhead.get(14), new double[]{5.0}})));
		// Chord 5
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{45.0, 1/4.0, 0.0, 1/2.0},
			allInfoAhead.get(15), new double[]{4.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{57.0, 1/2.0, 0.0, 1/2.0},
			allInfoAhead.get(16), new double[]{4.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{57.0, 1/4.0, 0.0, 1/2.0},
			allInfoAhead.get(17), new double[]{4.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{60.0, 1/4.0, 0.0, 1/2.0},
			allInfoAhead.get(18), new double[]{4.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{69.0, 1/4.0, 0.0, 1/2.0},
			allInfoAhead.get(19), new double[]{4.0}})));
		// Chord 6
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{45.0, 1/4.0, 0.0, 3/4.0},
			allInfoAhead.get(20), new double[]{2.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{60.0, 1/8.0, 0.0, 3/4.0},
			allInfoAhead.get(21), new double[]{2.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{64.0, 1/8.0, 0.0, 3/4.0},
			allInfoAhead.get(22), new double[]{2.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{69.0, 1/4.0, 0.0, 3/4.0},
			allInfoAhead.get(23), new double[]{2.0}})));
		// Chord 7
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{59.0, 1/8.0, 0.0, 7/8.0},
			allInfoAhead.get(24), new double[]{4.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{68.0, 1/8.0, 0.0, 7/8.0},
			allInfoAhead.get(25), new double[]{4.0}})));
		// Chord 8
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{45.0, 1/2.0, 0.0, 0.0},
			allInfoAhead.get(26), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{57.0, 1/2.0, 0.0, 0.0},
			allInfoAhead.get(27), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{64.0, 1/2.0, 0.0, 0.0},
			allInfoAhead.get(28), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{69.0, 1/16.0, 0.0, 0.0},
			allInfoAhead.get(29), new double[]{1.0}})));
		// Chords 9-14
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{68.0, 1/16.0, 1.0, 1/16.0},
			allInfoAhead.get(30), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{69.0, 1/32.0, 1.0, 1/8.0},
			allInfoAhead.get(31), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{68.0, 1/32.0, 1.0, 5/32.0},
			allInfoAhead.get(32), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{66.0, 1/32.0, 1.0, 3/16.0},
			allInfoAhead.get(33), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{68.0, 1/32.0, 1.0, 7/32.0},
			allInfoAhead.get(34), new double[]{1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{69.0, 1/4.0, 0.0, 1/4.0},
			allInfoAhead.get(35), new double[]{4.0}})));
		// Chord 15
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{45.0, 1/4.0, 0.0, 3/4.0}, 
			allInfoAhead.get(36), new double[]{-1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{57.0, 1/4.0, 0.0, 3/4.0},
			allInfoAhead.get(37), new double[]{-1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{64.0, 1/4.0, 0.0, 3/4.0},
			allInfoAhead.get(38), new double[]{-1.0}})));
		expected.add(ToolBox.concatDoubleArrays(Arrays.asList(new double[][]{new double[]{69.0, 1/4.0, 0.0, 3/4.0},
			allInfoAhead.get(39), new double[]{-1.0}})));
		
		List<double[]> actual = new ArrayList<double[]>();
		List<Integer[]> meterInfo = transcription.getMeterInfo();
		for (int i = 0; i < basicNoteProperties.length; i++) {
			actual.add(FeatureGenerator.getBasicNoteFeatures(null, basicNoteProperties, meterInfo, i));
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j]);
			}
		}
	}

	
	public void testGetProximitiesAndCourseInfoAhead() {
		Tablature tablature = new Tablature(encodingTestpiece1, false);
			
		// Determine expected
		List<double[]> expected = new ArrayList<double[]>();
		// Chord 0
		expected.add(new double[]{1.0/((1/4.0) + 1), 1.0/(5+1), 6.0, -1.0, 1.0/(7+1), 4.0,
			1.0/((7/16.0) + 1), 1.0/(2+1), 6.0, -1.0, -1.0, -1.0,
			1.0/((1/2.0) + 1), 1.0/(3+1), 6.0, 5.0, 1.0/(9+1), 4.0,
			1.0/((5/8.0) + 1), 1.0/(5+1), 6.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{1.0/((1/4.0) + 1), 1.0/(12+1), 6.0, 4.0, 1.0/(12+1), 1.0,
			1.0/((7/16.0) + 1), 1.0/(9+1), 6.0, -1.0, -1.0, -1.0,
			1.0/((1/2.0) + 1), 1.0/(7+1), 5.0, -1.0, 1.0/(2+1), 4.0,
			1.0/((5/8.0) + 1), 1.0/(12+1), 6.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{1.0/((1/4.0) + 1), 1.0/(8+1), 4.0, -1.0, 1.0/(4+1), 1.0,
			1.0/((7/16.0) + 1), 1.0/(17+1), 6.0, -1.0, -1.0, -1.0,
			1.0/((1/2.0) + 1), 1.0/(6+1), 4.0, 2.0, -1.0, -1.0,
			1.0/((5/8.0) + 1), 1.0/(20+1), 6.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{1.0/((1/4.0) + 1), 1.0/(12+1), 4.0, 1.0, 1.0/(3+1), 2.0,
			1.0/((7/16.0) + 1), 1.0/(21+1), 6.0, -1.0, -1.0, -1.0,
			1.0/((1/2.0) + 1), 1.0/(4+1), 2.0, -1.0, -1.0, -1.0,
			1.0/((5/8.0) + 1), 1.0/(24+1), 6.0, -1.0, -1.0, -1.0});
		// Chord 1
		expected.add(new double[]{1.0/((3/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(3+1), 6.0,
			1.0/((1/4.0) + 1), -1.0, -1.0, -1.0, 1.0/(2+1), 6.0,
			1.0/((3/8.0) + 1), -1.0, -1.0, 6.0, -1.0, -1.0,
			1.0/((1/2.0) + 1), -1.0, -1.0, 6.0, 1.0/(12+1), 5.0});
		expected.add(new double[]{1.0/((3/16.0) + 1), 1.0/(9+1), 6.0, -1.0, -1.0, -1.0,
			1.0/((1/4.0) + 1), 1.0/(7+1), 5.0, -1.0, 1.0/(2+1), 4.0,
			1.0/((3/8.0) + 1), 1.0/(12+1), 6.0, -1.0, -1.0, -1.0,
			1.0/((1/2.0) + 1), 1.0/(12+1), 6.0, 4.0, 1.0/(3+1), 3.0});
		expected.add(new double[]{1.0/((3/16.0) + 1), 1.0/(24+1), 6.0, -1.0, -1.0, -1.0,
			1.0/((1/4.0) + 1), 1.0/(7+1), 2.0, -1.0, -1.0, -1.0,
			1.0/((3/8.0) + 1), 1.0/(27+1), 6.0, -1.0, -1.0, -1.0,
			1.0/((1/2.0) + 1), 1.0/(3+1), 2.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{1.0/((3/16.0) + 1), 1.0/(21+1), 6.0, -1.0, -1.0, -1.0,
			1.0/((1/4.0) + 1), 1.0/(4+1), 2.0, -1.0, -1.0, -1.0,
			1.0/((3/8.0) + 1), 1.0/(24+1), 6.0, -1.0, -1.0, -1.0,
			1.0/((1/2.0) + 1), 1.0/(9+1), 3.0, 2.0, -1.0, -1.0});
		// Chord 2
		expected.add(new double[]{1.0/((1/16.0) + 1), 1.0/(1+1), 6.0, -1.0, 1.0/(2+1), 5.0,
			1.0/((3/16.0) + 1), 1.0/(3+1), 6.0, -1.0, -1.0, -1.0,
			1.0/((5/16.0) + 1), 1.0/(3+1), 6.0, -1.0, 1.0/(9+1), 5.0, 
			1.0/((9/16.0) + 1), 1.0/(3+1), 6.0, -1.0, 1.0/(12+1), 3.0});
		// Chord 3
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(2+1), 6.0, -1.0, -1.0, -1.0,
			1.0/((1/4.0) + 1), 1.0/(2+1), 6.0, -1.0, 1.0/(10+1), 5.0,
			1.0/((1/2.0) + 1), 1.0/(2+1), 6.0, -1.0, 1.0/(13+1), 3.0, 
			1.0/((5/8.0) + 1), -1.0, -1.0, -1.0, 1.0/(12+1), 3.0});
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(5+1), 6.0, -1.0, -1.0, -1.0,
			1.0/((1/4.0) + 1), 1.0/(5+1), 6.0, -1.0, 1.0/(7+1), 5.0,
			1.0/((1/2.0) + 1), 1.0/(5+1), 6.0, -1.0, 1.0/(10+1), 3.0, 
			1.0/((5/8.0) + 1), -1.0, -1.0, -1.0, 1.0/(9+1), 3.0});
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(14+1), 6.0, -1.0, -1.0, -1.0,
			1.0/((1/4.0) + 1), 1.0/(2+1), 4.0, -1.0, 1.0/(1+1), 3.0,
			1.0/((1/2.0) + 1), 1.0/(14+1), 6.0, -1.0, 1.0/(1+1), 3.0, 
			1.0/((5/8.0) + 1), -1.0, -1.0, 3.0, 1.0/(9+1), 2.0});
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(20+1), 6.0, -1.0, -1.0, -1.0,
			1.0/((1/4.0) + 1), 1.0/(5+1), 3.0, -1.0, 1.0/(4+1), 2.0,
			1.0/((1/2.0) + 1), 1.0/(1+1), 2.0, -1.0, 1.0/(4+1), 1.0, 
			1.0/((5/8.0) + 1), 1.0/(6+1), 3.0, -1.0, 1.0/(3+1), 2.0});
		// Chord 4
		expected.add(new double[]{1.0/((1/8.0) + 1), -1.0, -1.0, 6.0, 1.0/(12+1), 5.0,
			1.0/((3/8.0) + 1), -1.0, -1.0, 6.0, 1.0/(15+1), 3.0,
			1.0/((1/2.0) + 1), -1.0, -1.0, -1.0, 1.0/(14+1), 3.0,
			1.0/((5/8.0) + 1), -1.0, -1.0, 6.0, 1.0/(12+1), 4.0});
		// Chord 5
		expected.add(new double[]{1.0/((1/4.0) + 1), -1.0, -1.0, 6.0, 1.0/(15+1), 3.0,
			1.0/((3/8.0) + 1), -1.0, -1.0, -1.0, 1.0/(14+1), 3.0,
			1.0/((1/2.0) + 1), -1.0, -1.0, 6.0, 1.0/(12+1), 4.0,
			1.0/((9/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(23+1), 2.0});
		expected.add(new double[]{1.0/((1/4.0) + 1), 1.0/(12+1), 6.0, -1.0, 1.0/(3+1), 3.0,
			1.0/((3/8.0) + 1), -1.0, -1.0, -1.0, 1.0/(2+1), 3.0,
			1.0/((1/2.0) + 1), 1.0/(12+1), 6.0, 4.0, 1.0/(7+1), 2.0,
			1.0/((9/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(11+1), 2.0});
		expected.add(new double[]{1.0/((1/4.0) + 1), 1.0/(12+1), 6.0, -1.0, 1.0/(3+1), 3.0,
			1.0/((3/8.0) + 1), -1.0, -1.0, -1.0, 1.0/(2+1), 3.0,
			1.0/((1/2.0) + 1), 1.0/(12+1), 6.0, 4.0, 1.0/(7+1), 2.0,
			1.0/((9/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(11+1), 2.0});
		expected.add(new double[]{1.0/((1/4.0) + 1), 1.0/(15+1), 6.0, 3.0, 1.0/(4+1), 2.0,
			1.0/((3/8.0) + 1), 1.0/(1+1), 3.0, -1.0, 1.0/(8+1), 2.0,
			1.0/((1/2.0) + 1), 1.0/(3+1), 4.0, -1.0, 1.0/(4+1), 2.0,
			1.0/((9/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(8+1), 2.0});
		expected.add(new double[]{1.0/((1/4.0) + 1), 1.0/(5+1), 2.0, 1.0, -1.0, -1.0,
			1.0/((3/8.0) + 1), 1.0/(1+1), 2.0, -1.0, -1.0, -1.0,
			1.0/((1/2.0) + 1), 1.0/(5+1), 2.0, 1.0, -1.0, -1.0,
			1.0/((9/16.0) + 1), 1.0/(1+1), 2.0, -1.0, -1.0, -1.0});
		// Chord 6
		expected.add(new double[]{1.0/((1/8.0) + 1), -1.0, -1.0, -1.0, 1.0/(14+1), 3.0, 
			1.0/((1/4.0) + 1), -1.0, -1.0, 6.0, 1.0/(12+1), 4.0, 
			1.0/((5/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(23+1), 2.0,
			1.0/((3/8.0) + 1), -1.0, -1.0, -1.0, 1.0/(24+1), 1.0});
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(1+1), 3.0, -1.0, 1.0/(8+1), 2.0, 
			1.0/((1/4.0) + 1), 1.0/(3+1), 4.0, -1.0, 1.0/(4+1), 2.0, 
			1.0/((5/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(8+1), 2.0,
			1.0/((3/8.0) + 1), -1.0, -1.0, -1.0, 1.0/(9+1), 1.0});
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(5+1), 3.0, -1.0, 1.0/(4+1), 2.0, 
			1.0/((1/4.0) + 1), 1.0/(7+1), 4.0, 2.0, 1.0/(5+1), 1.0, 
			1.0/((5/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(4+1), 2.0,
			1.0/((3/8.0) + 1), -1.0, -1.0, -1.0, 1.0/(5+1), 1.0});
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(1+1), 2.0, -1.0, -1.0, -1.0, 
			1.0/((1/4.0) + 1), 1.0/(5+1), 2.0, 1.0, -1.0, -1.0, 
			1.0/((5/16.0) + 1), 1.0/(1+1), 2.0, -1.0, -1.0, -1.0,
			1.0/((3/8.0) + 1), -1.0, -1.0, 1.0, -1.0, -1.0});
		// Chord 7
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(2+1), 4.0, -1.0, 1.0/(5+1), 2.0, 
			1.0/((3/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(9+1), 2.0, 
			1.0/((1/4.0) + 1), -1.0, -1.0, -1.0, 1.0/(10+1), 1.0,
			1.0/((9/32.0) + 1), -1.0, -1.0, -1.0, 1.0/(9+1), 2.0});
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(4+1), 2.0, -1.0, 1.0/(1+1), 1.0, 
			1.0/((3/16.0) + 1), -1.0, -1.0, 2.0, -1.0, -1.0, 
			1.0/((1/4.0) + 1), -1.0, -1.0, -1.0, 1.0/(1+1), 1.0,
			1.0/((9/32.0) + 1), -1.0, -1.0, 2.0, -1.0, -1.0});
		// Chord 8
		expected.add(new double[]{1.0/((1/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(23+1), 2.0, 
			1.0/((1/8.0) + 1), -1.0, -1.0, -1.0, 1.0/(24+1), 1.0,
			1.0/((5/32.0) + 1), -1.0, -1.0, -1.0, 1.0/(23+1), 2.0,
			1.0/((3/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(21+1), 2.0});
		expected.add(new double[]{1.0/((1/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(11+1), 2.0, 
			1.0/((1/8.0) + 1), -1.0, -1.0, -1.0, 1.0/(12+1), 1.0,
			1.0/((5/32.0) + 1), -1.0, -1.0, -1.0, 1.0/(11+1), 2.0,
			1.0/((3/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(9+1), 2.0});
		expected.add(new double[]{1.0/((1/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(4+1), 2.0, 
			1.0/((1/8.0) + 1), -1.0, -1.0, -1.0, 1.0/(5+1), 1.0,
			1.0/((5/32.0) + 1), -1.0, -1.0, -1.0, 1.0/(4+1), 2.0,
			1.0/((3/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(2+1), 2.0});
		expected.add(new double[]{1.0/((1/16.0) + 1), 1.0/(1+1), 2.0, -1.0, -1.0, -1.0, 
			1.0/((1/8.0) + 1), -1.0, -1.0, 1.0, -1.0, -1.0,
			1.0/((5/32.0) + 1), 1.0/(1+1), 2.0, -1.0, -1.0, -1.0,
			1.0/((3/16.0) + 1), 1.0/(3+1), 2.0, -1.0, -1.0, -1.0});
		// Chords 9-14
		expected.add(new double[]{1.0/((1/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(1+1), 1.0,
			1.0/((3/32.0) + 1), -1.0, -1.0, 2.0, -1.0, -1.0, 
			1.0/((1/8.0) + 1), 1.0/(2+1), 2.0, -1.0, -1.0, -1.0,
			1.0/((5/32.0) + 1), -1.0, -1.0, 2.0, -1.0, -1.0});
		expected.add(new double[]{1.0/((1/32.0) + 1), 1.0/(1+1), 2.0, -1.0, -1.0, -1.0,
			1.0/((1/16.0) + 1), 1.0/(3+1), 2.0, -1.0, -1.0, -1.0, 
			1.0/((3/32.0) + 1), 1.0/(1+1), 2.0, -1.0, -1.0, -1.0,
			1.0/((1/8.0) + 1), -1.0, -1.0, 1.0, -1.0, -1.0});
		expected.add(new double[]{1.0/((1/32.0) + 1), 1.0/(2+1), 2.0, -1.0, -1.0, -1.0,
			1.0/((1/16.0) + 1), -1.0, -1.0, 2.0, -1.0, -1.0, 
			1.0/((3/32.0) + 1), -1.0, -1.0, -1.0, 1.0/(1+1), 1.0,
			1.0/((19/32.0) + 1), 1.0/(4+1), 2.0, -1.0, 1.0/(1+1), 1.0});
		expected.add(new double[]{1.0/((1/32.0) + 1), -1.0, -1.0, -1.0, 1.0/(2+1), 2.0,
			1.0/((1/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(3+1), 1.0, 
			1.0/((9/16.0) + 1), 1.0/(2+1), 2.0, -1.0, 1.0/(3+1), 1.0,
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{1.0/((1/32.0) + 1), -1.0, -1.0, -1.0, 1.0/(1+1), 1.0,
			1.0/((17/32.0) + 1), 1.0/(4+1), 2.0, -1.0, 1.0/(1+1), 1.0,
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0,
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{1.0/((1/2.0) + 1), 1.0/(5+1), 2.0, 1.0, -1.0, -1.0,
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0,
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0});
		// Chord 15
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,});
	    
		// Calculate actual
		List<double[]> actual = new ArrayList<double[]>();
		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
		for (int i = 0; i < basicTabSymbolProperties.length; i++) {
			actual.add(FeatureGenerator.getProximitiesAndCourseInfoAhead(basicTabSymbolProperties, null, i, 4));
		}
		
		// Assert equality  
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
		 	for (int j = 0; j < expected.get(i).length; j++) {
		 		assertEquals(expected.get(i)[j], actual.get(i)[j]);
		 	}
		}
	}


	public void testGetProximitiesAndCourseInfoAheadNonTab() {  	
		Transcription transcription = new Transcription(midiTestpiece1, null);

		// Determine expected
		List<double[]> expected = new ArrayList<double[]>();
		// Chord 0
		expected.add(new double[]{1.0/((1/4.0) + 1), 1.0/(5+1), -1.0, 1.0/(7+1),
			1.0/((7/16.0) + 1), 1.0/(2+1), -1.0, -1.0,
			1.0/((1/2.0) + 1), 1.0/(3+1), 1.0, 1.0/(9+1),
			1.0/((5/8.0) + 1), 1.0/(5+1), -1.0, -1.0});
		expected.add(new double[]{1.0/((1/4.0) + 1), 1.0/(12+1), 1.0, 1.0/(12+1),
			1.0/((7/16.0) + 1), 1.0/(9+1), -1.0, -1.0,
			1.0/((1/2.0) + 1), 1.0/(7+1), -1.0, 1.0/(2+1),
			1.0/((5/8.0) + 1), 1.0/(12+1), -1.0, -1.0});
		expected.add(new double[]{1.0/((1/4.0) + 1), 1.0/(8+1), -1.0, 1.0/(4+1),
			1.0/((7/16.0) + 1), 1.0/(17+1), -1.0, -1.0,
			1.0/((1/2.0) + 1), 1.0/(6+1), 1.0, -1.0,
			1.0/((5/8.0) + 1), 1.0/(20+1), -1.0, -1.0});
		expected.add(new double[]{1.0/((1/4.0) + 1), 1.0/(12+1), 1.0, 1.0/(3+1),
			1.0/((7/16.0) + 1), 1.0/(21+1), -1.0, -1.0,
			1.0/((1/2.0) + 1), 1.0/(4+1), -1.0, -1.0,
			1.0/((5/8.0) + 1), 1.0/(24+1), -1.0, -1.0});
		// Chord 1
		expected.add(new double[]{1.0/((3/16.0) + 1), -1.0, -1.0, 1.0/(3+1),
			1.0/((1/4.0) + 1), -1.0, -1.0, 1.0/(2+1),
			1.0/((3/8.0) + 1), -1.0, 1.0, -1.0,
			1.0/((1/2.0) + 1), -1.0, 1.0, 1.0/(12+1)});
		expected.add(new double[]{1.0/((3/16.0) + 1), 1.0/(9+1), -1.0, -1.0,
			1.0/((1/4.0) + 1), 1.0/(7+1), -1.0, 1.0/(2+1),
			1.0/((3/8.0) + 1), 1.0/(12+1), -1.0, -1.0,
			1.0/((1/2.0) + 1), 1.0/(12+1), 1.0, 1.0/(3+1)});
		expected.add(new double[]{1.0/((3/16.0) + 1), 1.0/(21+1), -1.0, -1.0,
			1.0/((1/4.0) + 1), 1.0/(4+1), -1.0, -1.0,
			1.0/((3/8.0) + 1), 1.0/(24+1), -1.0, -1.0,
			1.0/((1/2.0) + 1), 1.0/(9+1), 1.0, -1.0});
		expected.add(new double[]{1.0/((3/16.0) + 1), 1.0/(24+1), -1.0, -1.0,
			1.0/((1/4.0) + 1), 1.0/(7+1), -1.0, -1.0,
			1.0/((3/8.0) + 1), 1.0/(27+1), -1.0, -1.0,
			1.0/((1/2.0) + 1), 1.0/(3+1), -1.0, -1.0});
		// Chord 2
		expected.add(new double[]{1.0/((1/16.0) + 1), 1.0/(1+1), -1.0, 1.0/(2+1),
			1.0/((3/16.0) + 1), 1.0/(3+1), -1.0, -1.0,
			1.0/((5/16.0) + 1), 1.0/(3+1), -1.0, 1.0/(9+1), 
			1.0/((9/16.0) + 1), 1.0/(3+1), -1.0, 1.0/(12+1)});
		// Chord 3
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(2+1), -1.0, -1.0,
			1.0/((1/4.0) + 1), 1.0/(2+1), -1.0, 1.0/(10+1),
			1.0/((1/2.0) + 1), 1.0/(2+1), -1.0, 1.0/(13+1), 
			1.0/((5/8.0) + 1), -1.0, -1.0, 1.0/(12+1)});
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(5+1), -1.0, -1.0,
			1.0/((1/4.0) + 1), 1.0/(5+1), -1.0, 1.0/(7+1),
			1.0/((1/2.0) + 1), 1.0/(5+1), -1.0, 1.0/(10+1), 
			1.0/((5/8.0) + 1), -1.0, -1.0, 1.0/(9+1)});
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(14+1), -1.0, -1.0,
			1.0/((1/4.0) + 1), 1.0/(2+1), -1.0, 1.0/(1+1),
			1.0/((1/2.0) + 1), 1.0/(14+1), -1.0, 1.0/(1+1), 
			1.0/((5/8.0) + 1), -1.0, 1.0, 1.0/(9+1)});
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(20+1), -1.0, -1.0,
			1.0/((1/4.0) + 1), 1.0/(5+1), -1.0, 1.0/(4+1),
			1.0/((1/2.0) + 1), 1.0/(1+1), -1.0, 1.0/(4+1), 
			1.0/((5/8.0) + 1), 1.0/(6+1), -1.0, 1.0/(3+1)});
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(20+1), -1.0, -1.0,
			1.0/((1/4.0) + 1), 1.0/(5+1), -1.0, 1.0/(4+1),
			1.0/((1/2.0) + 1), 1.0/(1+1), -1.0, 1.0/(4+1), 
			1.0/((5/8.0) + 1), 1.0/(6+1), -1.0, 1.0/(3+1)});
		// Chord 4
		expected.add(new double[]{1.0/((1/8.0) + 1), -1.0, 1.0, 1.0/(12+1),
			1.0/((3/8.0) + 1), -1.0, 1.0, 1.0/(15+1),
			1.0/((1/2.0) + 1), -1.0, -1.0, 1.0/(14+1),
			1.0/((5/8.0) + 1), -1.0, 1.0, 1.0/(12+1)});
		// Chord 5
		expected.add(new double[]{1.0/((1/4.0) + 1), -1.0, 1.0, 1.0/(15+1),
			1.0/((3/8.0) + 1), -1.0, -1.0, 1.0/(14+1),
			1.0/((1/2.0) + 1), -1.0, 1.0, 1.0/(12+1),
			1.0/((9/16.0) + 1), -1.0, -1.0, 1.0/(23+1)});
		expected.add(new double[]{1.0/((1/4.0) + 1), 1.0/(12+1), -1.0, 1.0/(3+1),
			1.0/((3/8.0) + 1), -1.0, -1.0, 1.0/(2+1),
			1.0/((1/2.0) + 1), 1.0/(12+1), 1.0, 1.0/(7+1),
			1.0/((9/16.0) + 1), -1.0, -1.0, 1.0/(11+1)});
		expected.add(new double[]{1.0/((1/4.0) + 1), 1.0/(12+1), -1.0, 1.0/(3+1),
			1.0/((3/8.0) + 1), -1.0, -1.0, 1.0/(2+1),
			1.0/((1/2.0) + 1), 1.0/(12+1), 1.0, 1.0/(7+1),
			1.0/((9/16.0) + 1), -1.0, -1.0, 1.0/(11+1)});
		expected.add(new double[]{1.0/((1/4.0) + 1), 1.0/(15+1), 1.0, 1.0/(4+1),
			1.0/((3/8.0) + 1), 1.0/(1+1), -1.0, 1.0/(8+1),
			1.0/((1/2.0) + 1), 1.0/(3+1), -1.0, 1.0/(4+1),
			1.0/((9/16.0) + 1), -1.0, -1.0, 1.0/(8+1)});
		expected.add(new double[]{1.0/((1/4.0) + 1), 1.0/(5+1), 1.0, -1.0,
			1.0/((3/8.0) + 1), 1.0/(1+1), -1.0, -1.0,
			1.0/((1/2.0) + 1), 1.0/(5+1), 1.0, -1.0,
			1.0/((9/16.0) + 1), 1.0/(1+1), -1.0, -1.0});
		// Chord 6
		expected.add(new double[]{1.0/((1/8.0) + 1), -1.0, -1.0, 1.0/(14+1), 
			1.0/((1/4.0) + 1), -1.0, 1.0, 1.0/(12+1), 
			1.0/((5/16.0) + 1), -1.0, -1.0, 1.0/(23+1),
			1.0/((3/8.0) + 1), -1.0, -1.0, 1.0/(24+1)});
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(1+1), -1.0, 1.0/(8+1), 
			1.0/((1/4.0) + 1), 1.0/(3+1), -1.0, 1.0/(4+1), 
			1.0/((5/16.0) + 1), -1.0, -1.0, 1.0/(8+1),
			1.0/((3/8.0) + 1), -1.0, -1.0, 1.0/(9+1)});
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(5+1), -1.0, 1.0/(4+1), 
			1.0/((1/4.0) + 1), 1.0/(7+1), 1.0, 1.0/(5+1), 
			1.0/((5/16.0) + 1), -1.0, -1.0, 1.0/(4+1),
			1.0/((3/8.0) + 1), -1.0, -1.0, 1.0/(5+1)});
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(1+1), -1.0, -1.0, 
			1.0/((1/4.0) + 1), 1.0/(5+1), 1.0, -1.0, 
			1.0/((5/16.0) + 1), 1.0/(1+1), -1.0, -1.0,
			1.0/((3/8.0) + 1), -1.0, 1.0, -1.0});
		// Chord 7
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(2+1), -1.0, 1.0/(5+1), 
			1.0/((3/16.0) + 1), -1.0, -1.0, 1.0/(9+1), 
			1.0/((1/4.0) + 1), -1.0, -1.0, 1.0/(10+1),
			1.0/((9/32.0) + 1), -1.0, -1.0, 1.0/(9+1)});
		expected.add(new double[]{1.0/((1/8.0) + 1), 1.0/(4+1), -1.0, 1.0/(1+1), 
			1.0/((3/16.0) + 1), -1.0, 1.0, -1.0, 
			1.0/((1/4.0) + 1), -1.0, -1.0, 1.0/(1+1),
			1.0/((9/32.0) + 1), -1.0, 1.0, -1.0});
		// Chord 8
		expected.add(new double[]{1.0/((1/16.0) + 1), -1.0, -1.0, 1.0/(23+1), 
			1.0/((1/8.0) + 1), -1.0, -1.0, 1.0/(24+1),
			1.0/((5/32.0) + 1), -1.0, -1.0, 1.0/(23+1),
			1.0/((3/16.0) + 1), -1.0, -1.0, 1.0/(21+1)});
		expected.add(new double[]{1.0/((1/16.0) + 1), -1.0, -1.0, 1.0/(11+1), 
			1.0/((1/8.0) + 1), -1.0, -1.0, 1.0/(12+1),
			1.0/((5/32.0) + 1), -1.0, -1.0, 1.0/(11+1),
			1.0/((3/16.0) + 1), -1.0, -1.0, 1.0/(9+1)});
		expected.add(new double[]{1.0/((1/16.0) + 1), -1.0, -1.0, 1.0/(4+1), 
			1.0/((1/8.0) + 1), -1.0, -1.0, 1.0/(5+1),
			1.0/((5/32.0) + 1), -1.0, -1.0, 1.0/(4+1),
			1.0/((3/16.0) + 1), -1.0, -1.0, 1.0/(2+1)});
		expected.add(new double[]{1.0/((1/16.0) + 1), 1.0/(1+1), -1.0, -1.0, 
			1.0/((1/8.0) + 1), -1.0, 1.0, -1.0,
			1.0/((5/32.0) + 1), 1.0/(1+1), -1.0, -1.0,
			1.0/((3/16.0) + 1), 1.0/(3+1), -1.0, -1.0});
		// Chords 9-14
		expected.add(new double[]{1.0/((1/16.0) + 1), -1.0, -1.0, 1.0/(1+1),
			1.0/((3/32.0) + 1), -1.0, 1.0, -1.0, 
			1.0/((1/8.0) + 1), 1.0/(2+1), -1.0, -1.0,
			1.0/((5/32.0) + 1), -1.0, 1.0, -1.0});
		expected.add(new double[]{1.0/((1/32.0) + 1), 1.0/(1+1), -1.0, -1.0,
			1.0/((1/16.0) + 1), 1.0/(3+1), -1.0, -1.0, 
			1.0/((3/32.0) + 1), 1.0/(1+1), -1.0, -1.0,
			1.0/((1/8.0) + 1), -1.0, 1.0, -1.0});
		expected.add(new double[]{1.0/((1/32.0) + 1), 1.0/(2+1), -1.0, -1.0,
			1.0/((1/16.0) + 1), -1.0, 1.0, -1.0, 
			1.0/((3/32.0) + 1), -1.0, -1.0, 1.0/(1+1),
			1.0/((19/32.0) + 1), 1.0/(4+1), -1.0, 1.0/(1+1)});
		expected.add(new double[]{1.0/((1/32.0) + 1), -1.0, -1.0, 1.0/(2+1),
			1.0/((1/16.0) + 1), -1.0, -1.0, 1.0/(3+1), 
			1.0/((9/16.0) + 1), 1.0/(2+1), -1.0, 1.0/(3+1),
			-1.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{1.0/((1/32.0) + 1), -1.0, -1.0, 1.0/(1+1),
			1.0/((17/32.0) + 1), 1.0/(4+1), -1.0, 1.0/(1+1),
			-1.0, -1.0, -1.0, -1.0,
			-1.0, -1.0, -1.0, -1.0,});
		expected.add(new double[]{1.0/((1/2.0) + 1), 1.0/(5+1), 1.0, -1.0,
			-1.0, -1.0, -1.0, -1.0,
			-1.0, -1.0, -1.0, -1.0, 
			-1.0, -1.0, -1.0, -1.0,});
		// Chord 15
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,});
		
//    // b. Backwards approach
//    List<double[]> expectedBackwards = new ArrayList<double[]>();
//   	// Chord 0
//    expectedBackwards.add(new double[]{1.0/((1/2.0) + 1), -1.0, -1.0, 1.0/(24+1),
//     		                              1.0/((17/32.0) + 1), -1.0, -1.0, 1.0/(23+1),
//     		                              1.0/((9/16.0) + 1), -1.0, -1.0, 1.0/(21+1),
//     		                              1.0/((19/32.0) + 1), -1.0, -1.0, 1.0/(23+1)});
//    expectedBackwards.add(new double[]{1.0/((1/2.0) + 1), -1.0, -1.0, 1.0/(12+1),
//     					                        1.0/((17/32.0) + 1), -1.0, -1.0, 1.0/(11+1),
//                                      1.0/((9/16.0) + 1), -1.0, -1.0, 1.0/(9+1),
//                                      1.0/((19/32.0) + 1), -1.0, -1.0, 1.0/(11+1)});
//    expectedBackwards.add(new double[]{1.0/((1/2.0) + 1), -1.0, -1.0, 1.0/(5+1),
//                                      1.0/((17/32.0) + 1), -1.0, -1.0, 1.0/(4+1),
//                                      1.0/((9/16.0) + 1), -1.0, -1.0, 1.0/(2+1),
//                                      1.0/((19/32.0) + 1), -1.0, -1.0, 1.0/(4+1)});
//    expectedBackwards.add(new double[]{1.0/((1/2.0) + 1), -1.0, 1.0, -1.0,
//                                      1.0/((17/32.0) + 1), 1.0/(1+1), -1.0, -1.0,
//                                      1.0/((9/16.0) + 1), 1.0/(3+1), -1.0, -1.0,
//                                      1.0/((19/32.0) + 1), 1.0/(1+1), -1.0, -1.0});
//  	// Chord 1
//    expectedBackwards.add(new double[]{1.0/((1/32.0) + 1), 1.0/(1+1), -1.0, -1.0,
//    		                              1.0/((1/16.0) + 1), -1.0, -1.0, 1.0/(2+1),
//    		                              1.0/((3/32.0) + 1), -1.0, 1.0, -1.0,
//    		                              1.0/((1/8.0) + 1), -1.0, 1.0, 1.0/(12+1)});
//    expectedBackwards.add(new double[]{1.0/((3/16.0) + 1), 1.0/(9+1), -1.0, -1.0,
//                                      1.0/((1/4.0) + 1), 1.0/(7+1), -1.0, 1.0/(2+1),
//                                      1.0/((3/8.0) + 1), 1.0/(12+1), -1.0, -1.0,
//                                      1.0/((1/2.0) + 1), 1.0/(12+1), 1.0, 1.0/(3+1)});
//    expectedBackwards.add(new double[]{1.0/((3/16.0) + 1), 1.0/(21+1), -1.0, -1.0,
//                                      1.0/((1/4.0) + 1), 1.0/(4+1), -1.0, -1.0,
//                                      1.0/((3/8.0) + 1), 1.0/(24+1), -1.0, -1.0,
//                                      1.0/((1/2.0) + 1), 1.0/(9+1), 1.0, -1.0});
//    expectedBackwards.add(new double[]{1.0/((3/16.0) + 1), 1.0/(24+1), -1.0, -1.0,
//                                      1.0/((1/4.0) + 1), 1.0/(7+1), -1.0, -1.0,
//                                      1.0/((3/8.0) + 1), 1.0/(27+1), -1.0, -1.0,
//                                      1.0/((1/2.0) + 1), 1.0/(3+1), -1.0, -1.0});
//    // Chord 2
//    expectedBackwards.add(new double[]{1.0/((1/16.0) + 1), 1.0/(1+1), -1.0, 1.0/(2+1),
//    		                              1.0/((3/16.0) + 1), 1.0/(3+1), -1.0, -1.0,
//                                      1.0/((5/16.0) + 1), 1.0/(3+1), -1.0, 1.0/(9+1), 
//                                      1.0/((9/16.0) + 1), 1.0/(3+1), -1.0, 1.0/(12+1)});
//    // Chord 3
//    expectedBackwards.add(new double[]{1.0/((1/8.0) + 1), 1.0/(2+1), -1.0, -1.0,
//    		                              1.0/((1/4.0) + 1), 1.0/(2+1), -1.0, 1.0/(10+1),
//    		                              1.0/((1/2.0) + 1), 1.0/(2+1), -1.0, 1.0/(13+1), 
//    		                              1.0/((5/8.0) + 1), -1.0, -1.0, 1.0/(12+1)});
//    expectedBackwards.add(new double[]{1.0/((1/8.0) + 1), 1.0/(5+1), -1.0, -1.0,
//    		                              1.0/((1/4.0) + 1), 1.0/(5+1), -1.0, 1.0/(7+1),
//                                      1.0/((1/2.0) + 1), 1.0/(5+1), -1.0, 1.0/(10+1), 
//                                      1.0/((5/8.0) + 1), -1.0, -1.0, 1.0/(9+1)});
//    expectedBackwards.add(new double[]{1.0/((1/8.0) + 1), 1.0/(14+1), -1.0, -1.0,
//    		                              1.0/((1/4.0) + 1), 1.0/(2+1), -1.0, 1.0/(1+1),
//                                      1.0/((1/2.0) + 1), 1.0/(14+1), -1.0, 1.0/(1+1), 
//                                      1.0/((5/8.0) + 1), -1.0, 1.0, 1.0/(9+1)});
//    expectedBackwards.add(new double[]{1.0/((1/8.0) + 1), 1.0/(20+1), -1.0, -1.0,
//    		                              1.0/((1/4.0) + 1), 1.0/(5+1), -1.0, 1.0/(4+1),
//                                      1.0/((1/2.0) + 1), 1.0/(1+1), -1.0, 1.0/(4+1), 
//                                      1.0/((5/8.0) + 1), 1.0/(6+1), -1.0, 1.0/(3+1)});
//    expectedBackwards.add(new double[]{1.0/((1/8.0) + 1), 1.0/(20+1), -1.0, -1.0,
//                                      1.0/((1/4.0) + 1), 1.0/(5+1), -1.0, 1.0/(4+1),
//                                      1.0/((1/2.0) + 1), 1.0/(1+1), -1.0, 1.0/(4+1), 
//                                      1.0/((5/8.0) + 1), 1.0/(6+1), -1.0, 1.0/(3+1)});
//    // Chord 4
//    expectedBackwards.add(new double[]{1.0/((1/8.0) + 1), -1.0, 1.0, 1.0/(12+1),
//    		                              1.0/((3/8.0) + 1), -1.0, 1.0, 1.0/(15+1),
//    		                              1.0/((1/2.0) + 1), -1.0, -1.0, 1.0/(14+1),
//                                      1.0/((5/8.0) + 1), -1.0, 1.0, 1.0/(12+1)});
//    // Chord 5
//    expectedBackwards.add(new double[]{1.0/((1/4.0) + 1), -1.0, 1.0, 1.0/(15+1),
//    		                              1.0/((3/8.0) + 1), -1.0, -1.0, 1.0/(14+1),
//    		                              1.0/((1/2.0) + 1), -1.0, 1.0, 1.0/(12+1),
//    		                              1.0/((9/16.0) + 1), -1.0, -1.0, 1.0/(23+1)});
//    expectedBackwards.add(new double[]{1.0/((1/4.0) + 1), 1.0/(12+1), -1.0, 1.0/(3+1),
//                                      1.0/((3/8.0) + 1), -1.0, -1.0, 1.0/(2+1),
//                                      1.0/((1/2.0) + 1), 1.0/(12+1), 1.0, 1.0/(7+1),
//                                      1.0/((9/16.0) + 1), -1.0, -1.0, 1.0/(11+1)});
//    expectedBackwards.add(new double[]{1.0/((1/4.0) + 1), 1.0/(12+1), -1.0, 1.0/(3+1),
//                                      1.0/((3/8.0) + 1), -1.0, -1.0, 1.0/(2+1),
//                                      1.0/((1/2.0) + 1), 1.0/(12+1), 1.0, 1.0/(7+1),
//                                      1.0/((9/16.0) + 1), -1.0, -1.0, 1.0/(11+1)});
//    expectedBackwards.add(new double[]{1.0/((1/4.0) + 1), 1.0/(15+1), 1.0, 1.0/(4+1),
//                                      1.0/((3/8.0) + 1), 1.0/(1+1), -1.0, 1.0/(8+1),
//                                      1.0/((1/2.0) + 1), 1.0/(3+1), -1.0, 1.0/(4+1),
//                                      1.0/((9/16.0) + 1), -1.0, -1.0, 1.0/(8+1)});
//    expectedBackwards.add(new double[]{1.0/((1/4.0) + 1), 1.0/(5+1), 1.0, -1.0,
//                                      1.0/((3/8.0) + 1), 1.0/(1+1), -1.0, -1.0,
//                                      1.0/((1/2.0) + 1), 1.0/(5+1), 1.0, -1.0,
//                                      1.0/((9/16.0) + 1), 1.0/(1+1), -1.0, -1.0});
//    // Chord 6
//    expectedBackwards.add(new double[]{1.0/((1/8.0) + 1), -1.0, -1.0, 1.0/(14+1), 
//    		                              1.0/((1/4.0) + 1), -1.0, 1.0, 1.0/(12+1), 
//    		                              1.0/((5/16.0) + 1), -1.0, -1.0, 1.0/(23+1),
//    		                              1.0/((3/8.0) + 1), -1.0, -1.0, 1.0/(24+1)});
//    expectedBackwards.add(new double[]{1.0/((1/8.0) + 1), 1.0/(1+1), -1.0, 1.0/(8+1), 
//                                      1.0/((1/4.0) + 1), 1.0/(3+1), -1.0, 1.0/(4+1), 
//                                      1.0/((5/16.0) + 1), -1.0, -1.0, 1.0/(8+1),
//                                      1.0/((3/8.0) + 1), -1.0, -1.0, 1.0/(9+1)});
//    expectedBackwards.add(new double[]{1.0/((1/8.0) + 1), 1.0/(5+1), -1.0, 1.0/(4+1), 
//                                      1.0/((1/4.0) + 1), 1.0/(7+1), 1.0, 1.0/(5+1), 
//                                      1.0/((5/16.0) + 1), -1.0, -1.0, 1.0/(4+1),
//                                      1.0/((3/8.0) + 1), -1.0, -1.0, 1.0/(5+1)});
//    expectedBackwards.add(new double[]{1.0/((1/8.0) + 1), 1.0/(1+1), -1.0, -1.0, 
//                                      1.0/((1/4.0) + 1), 1.0/(5+1), 1.0, -1.0, 
//                                      1.0/((5/16.0) + 1), 1.0/(1+1), -1.0, -1.0,
//                                      1.0/((3/8.0) + 1), -1.0, 1.0, -1.0});
//    // Chord 7
//    expectedBackwards.add(new double[]{1.0/((1/8.0) + 1), 1.0/(2+1), -1.0, 1.0/(5+1), 
//    		                              1.0/((3/16.0) + 1), -1.0, -1.0, 1.0/(9+1), 
//    		                              1.0/((1/4.0) + 1), -1.0, -1.0, 1.0/(10+1),
//    		                              1.0/((9/32.0) + 1), -1.0, -1.0, 1.0/(9+1)});
//    expectedBackwards.add(new double[]{1.0/((1/8.0) + 1), 1.0/(4+1), -1.0, 1.0/(1+1), 
//                                      1.0/((3/16.0) + 1), -1.0, 1.0, -1.0, 
//                                      1.0/((1/4.0) + 1), -1.0, -1.0, 1.0/(1+1),
//                                      1.0/((9/32.0) + 1), -1.0, 1.0, -1.0});
//    // Chord 8
//    expectedBackwards.add(new double[]{1.0/((1/16.0) + 1), -1.0, -1.0, 1.0/(23+1), 
//    		                              1.0/((1/8.0) + 1), -1.0, -1.0, 1.0/(24+1),
//    		                              1.0/((5/32.0) + 1), -1.0, -1.0, 1.0/(23+1),
//    		                              1.0/((3/16.0) + 1), -1.0, -1.0, 1.0/(21+1)});
//    expectedBackwards.add(new double[]{1.0/((1/16.0) + 1), -1.0, -1.0, 1.0/(11+1), 
//                                      1.0/((1/8.0) + 1), -1.0, -1.0, 1.0/(12+1),
//                                      1.0/((5/32.0) + 1), -1.0, -1.0, 1.0/(11+1),
//                                      1.0/((3/16.0) + 1), -1.0, -1.0, 1.0/(9+1)});
//    expectedBackwards.add(new double[]{1.0/((1/16.0) + 1), -1.0, -1.0, 1.0/(4+1), 
//                                      1.0/((1/8.0) + 1), -1.0, -1.0, 1.0/(5+1),
//                                      1.0/((5/32.0) + 1), -1.0, -1.0, 1.0/(4+1),
//                                      1.0/((3/16.0) + 1), -1.0, -1.0, 1.0/(2+1)});
//    expectedBackwards.add(new double[]{1.0/((1/16.0) + 1), 1.0/(1+1), -1.0, -1.0, 
//                                      1.0/((1/8.0) + 1), -1.0, 1.0, -1.0,
//                                      1.0/((5/32.0) + 1), 1.0/(1+1), -1.0, -1.0,
//                                      1.0/((3/16.0) + 1), 1.0/(3+1), -1.0, -1.0});
//    // Chords 9-14
//    expectedBackwards.add(new double[]{1.0/((1/16.0) + 1), -1.0, -1.0, 1.0/(1+1),
//    		                              1.0/((3/32.0) + 1), -1.0, 1.0, -1.0, 
//    		                              1.0/((1/8.0) + 1), 1.0/(2+1), -1.0, -1.0,
//    		                              1.0/((5/32.0) + 1), -1.0, 1.0, -1.0});
//    expectedBackwards.add(new double[]{1.0/((1/32.0) + 1), 1.0/(1+1), -1.0, -1.0,
//                                      1.0/((1/16.0) + 1), 1.0/(3+1), -1.0, -1.0, 
//                                      1.0/((3/32.0) + 1), 1.0/(1+1), -1.0, -1.0,
//                                      1.0/((1/8.0) + 1), -1.0, 1.0, -1.0});
//    expectedBackwards.add(new double[]{1.0/((1/32.0) + 1), 1.0/(2+1), -1.0, -1.0,
//                                      1.0/((1/16.0) + 1), -1.0, 1.0, -1.0, 
//                                      1.0/((3/32.0) + 1), -1.0, -1.0, 1.0/(1+1),
//                                      1.0/((19/32.0) + 1), 1.0/(4+1), -1.0, 1.0/(1+1)});
//    expectedBackwards.add(new double[]{1.0/((1/32.0) + 1), -1.0, -1.0, 1.0/(2+1),
//                                      1.0/((1/16.0) + 1), -1.0, -1.0, 1.0/(3+1), 
//                                      1.0/((9/16.0) + 1), 1.0/(2+1), -1.0, 1.0/(3+1),
//                                      -1.0, -1.0, -1.0, -1.0});
//    expectedBackwards.add(new double[]{1.0/((1/32.0) + 1), -1.0, -1.0, 1.0/(1+1),
//                                      1.0/((17/32.0) + 1), 1.0/(4+1), -1.0, 1.0/(1+1),
//                                      -1.0, -1.0, -1.0, -1.0,
//                                      -1.0, -1.0, -1.0, -1.0,});
//    expectedBackwards.add(new double[]{1.0/((1/2.0) + 1), 1.0/(5+1), 1.0, -1.0,
//    		                              -1.0, -1.0, -1.0, -1.0,
//    		                              -1.0, -1.0, -1.0, -1.0, 
//                                      -1.0, -1.0, -1.0, -1.0,});
//    // Chord 15
//    expectedBackwards.add(new double[]{-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 
//    		                              -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,});
//    expectedBackwards.add(new double[]{-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 
//                                      -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,});
//    expectedBackwards.add(new double[]{-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 
//                                      -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,});
//    expectedBackwards.add(new double[]{-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 
//                                      -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,});

		// Calculate actual
		List<double[]> actual = new ArrayList<double[]>();
		Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
		for (int i = 0; i < basicNoteProperties.length; i++) {
			actual.add(FeatureGenerator.getProximitiesAndCourseInfoAhead(null, basicNoteProperties, i, 4));
		}
		
		// Assert equality  
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {  
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {  
				assertEquals(expected.get(i)[j], actual.get(i)[j]);
			}
		}
	}
	
	
	public void testGetNumberOfNewOnsetsInNextChord() {
		Tablature tablature = new Tablature(encodingTestpiece1, false);

		// Determine expected
		List<Double> expected = new ArrayList<Double>();
		// Chord 0
		expected.addAll(Arrays.asList(new Double[]{4.0, 4.0, 4.0, 4.0}));
		// Chord 1
		expected.addAll(Arrays.asList(new Double[]{1.0, 1.0, 1.0, 1.0}));
		// Chord 2
		expected.addAll(Arrays.asList(new Double[]{4.0}));
		// Chord 3
		expected.addAll(Arrays.asList(new Double[]{1.0, 1.0, 1.0, 1.0}));
		// Chord 4
		expected.addAll(Arrays.asList(new Double[]{5.0}));
		// Chord 5
		expected.addAll(Arrays.asList(new Double[]{4.0, 4.0, 4.0, 4.0, 4.0}));
		// Chord 6
		expected.addAll(Arrays.asList(new Double[]{2.0, 2.0, 2.0, 2.0}));
		// Chord 7
		expected.addAll(Arrays.asList(new Double[]{4.0, 4.0}));
		// Chord 8
		expected.addAll(Arrays.asList(new Double[]{1.0, 1.0, 1.0, 1.0}));
		// Chord 9-14
		expected.addAll(Arrays.asList(new Double[]{1.0, 1.0, 1.0, 1.0, 1.0, 4.0}));
		// Chord 15
		expected.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0}));
   	
//    // b. Backwards model
//   	List<Double> expectedBackwards = new ArrayList<Double>();
//   	// Chord 15
//   	expectedBackwards.addAll(Arrays.asList(new Double[]{1.0, 1.0, 1.0, 1.0}));
//   	// Chord 14-9
//   	expectedBackwards.addAll(Arrays.asList(new Double[]{1.0, 1.0, 1.0, 1.0, 1.0, 4.0}));
//   	// Chord 8
//   	expectedBackwards.addAll(Arrays.asList(new Double[]{2.0, 2.0, 2.0, 2.0}));
//   	// Chord 7
//   	expectedBackwards.addAll(Arrays.asList(new Double[]{4.0, 4.0}));
//   	// Chord 6
//   	expectedBackwards.addAll(Arrays.asList(new Double[]{5.0, 5.0, 5.0, 5.0}));
//   	// Chord 5
//   	expectedBackwards.addAll(Arrays.asList(new Double[]{1.0, 1.0, 1.0, 1.0, 1.0}));
//   	// Chord 4
//   	expectedBackwards.addAll(Arrays.asList(new Double[]{4.0}));
//   	// Chord 3
//   	expectedBackwards.addAll(Arrays.asList(new Double[]{1.0, 1.0, 1.0, 1.0}));
//    // Chord 2
//    expectedBackwards.addAll(Arrays.asList(new Double[]{4.0}));
//    // Chord 1
//    expectedBackwards.addAll(Arrays.asList(new Double[]{4.0, 4.0, 4.0, 4.0}));
//    // Chord 0
//    expectedBackwards.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0}));
//    expected.addAll(expectedBackwards);
    
		// Calculate actual
		List<Double> actual = new ArrayList<Double>();
		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//		featureGenerator.setModelBackward(false);
		for (int i = 0; i < basicTabSymbolProperties.length; i++) {
			actual.add(FeatureGenerator.getNumberOfNewOnsetsInNextChord(basicTabSymbolProperties, null, i));
		}
		
//    // b. Backwards model
//  	featureGenerator.setModelBackward(true);
//  	List<Integer> backwardsMapping = Transcription.getBackwardsMapping(tablature.getNumberOfNotesPerChord());
////    for (int i = 0; i < basicTabSymbolProperties.length; i++) {
//    for (int i : backwardsMapping) {
//  		actual.add(featureGenerator.getNumberOfNewOnsetsInNextChord(basicTabSymbolProperties, null, i));
//  	}

		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
		assertEquals(expected, actual);
	}
	
	
	public void testGetNumberOfNewOnsetsInNextChordNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		// Determine expected
		List<Double> expected = new ArrayList<Double>();
		// Chord 0
		expected.addAll(Arrays.asList(new Double[]{4.0, 4.0, 4.0, 4.0}));
		// Chord 1
		expected.addAll(Arrays.asList(new Double[]{1.0, 1.0, 1.0, 1.0}));
		// Chord 2
		expected.addAll(Arrays.asList(new Double[]{5.0}));
		// Chord 3
		expected.addAll(Arrays.asList(new Double[]{1.0, 1.0, 1.0, 1.0, 1.0}));
		// Chord 4
		expected.addAll(Arrays.asList(new Double[]{5.0}));
		// Chord 5
		expected.addAll(Arrays.asList(new Double[]{4.0, 4.0, 4.0, 4.0, 4.0}));
		// Chord 6
		expected.addAll(Arrays.asList(new Double[]{2.0, 2.0, 2.0, 2.0}));
		// Chord 7
		expected.addAll(Arrays.asList(new Double[]{4.0, 4.0}));
		// Chord 8
		expected.addAll(Arrays.asList(new Double[]{1.0, 1.0, 1.0, 1.0}));
		// Chord 9-14
		expected.addAll(Arrays.asList(new Double[]{1.0, 1.0, 1.0, 1.0, 1.0, 4.0}));
		// Chord 15
		expected.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0}));

		// Calculate actual
		List<Double> actual = new ArrayList<Double>();
		Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
		for (int i = 0; i < basicNoteProperties.length; i++) {
			actual.add(FeatureGenerator.getNumberOfNewOnsetsInNextChord(null, basicNoteProperties, i));
		}

		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
		assertEquals(expected, actual);
	}
	
	
	public void testGetPositionWithinChord() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);
		
		// Determine expected 
		List<double[]> expected = new ArrayList<double[]>();
		// a. Not modelling duration
		List<double[]> expNoDur = new ArrayList<double[]>();
		// Chord 0
		expNoDur.add(new double[]{4.0, 0.0, -1.0, 7.0, 7.0, 8.0, 4.0, -1.0});
		expNoDur.add(new double[]{4.0, 1.0, 7.0, 8.0, 7.0, 8.0, 4.0, -1.0});
		expNoDur.add(new double[]{4.0, 2.0, 8.0, 4.0, 7.0, 8.0, 4.0, -1.0});
		expNoDur.add(new double[]{4.0, 3.0, 4.0, -1.0, 7.0, 8.0, 4.0, -1.0});
		// Chord 1
		expNoDur.add(new double[]{4.0, 0.0, -1.0, 12.0, 12.0, 12.0, 3.0, -1.0});
		expNoDur.add(new double[]{4.0, 1.0, 12.0, 12.0, 12.0, 12.0, 3.0, -1.0});
		expNoDur.add(new double[]{4.0, 3.0, 3.0, -1.0, 12.0, 12.0, 3.0, -1.0});
		expNoDur.add(new double[]{4.0, 2.0, 12.0, 3.0, 12.0, 12.0, 3.0, -1.0});
		// Chord 2
		expNoDur.add(new double[]{1.0, 0.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0});
		// Chord 3
		expNoDur.add(new double[]{4.0, 0.0, -1.0, 3.0, 3.0, 9.0, 6.0, -1.0});
		expNoDur.add(new double[]{4.0, 1.0, 3.0, 9.0, 3.0, 9.0, 6.0, -1.0});
		expNoDur.add(new double[]{4.0, 2.0, 9.0, 6.0, 3.0, 9.0, 6.0, -1.0});
		expNoDur.add(new double[]{4.0, 3.0, 6.0, -1.0, 3.0, 9.0, 6.0, -1.0});
		// Chord 4
		expNoDur.add(new double[]{1.0, 0.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0});
		// Chord 5
		expNoDur.add(new double[]{5.0, 0.0, -1.0, 12.0, 12.0, 0.0, 3.0, 9.0});
		expNoDur.add(new double[]{5.0, 1.0, 12.0, 3.0, 12.0, 0.0, 3.0, 9.0});
		expNoDur.add(new double[]{5.0, 1.0, 12.0, 3.0, 12.0, 0.0, 3.0, 9.0});
		expNoDur.add(new double[]{5.0, 2.0, 3.0, 9.0, 12.0, 0.0, 3.0, 9.0});
		expNoDur.add(new double[]{5.0, 3.0, 9.0, -1.0, 12.0, 0.0, 3.0, 9.0});
		// Chord 6
		expNoDur.add(new double[]{4.0, 0.0, -1.0, 15.0, 15.0, 4.0, 5.0, -1.0});
		expNoDur.add(new double[]{4.0, 1.0, 15.0, 4.0, 15.0, 4.0, 5.0, -1.0});
		expNoDur.add(new double[]{4.0, 2.0, 4.0, 5.0, 15.0, 4.0, 5.0, -1.0});
		expNoDur.add(new double[]{4.0, 3.0, 5.0, -1.0, 15.0, 4.0, 5.0, -1.0});
		// Chord 7
		expNoDur.add(new double[]{2.0, 0.0, -1.0, 9.0, 9.0, -1.0, -1.0, -1.0});
		expNoDur.add(new double[]{2.0, 1.0, 9.0, -1.0, 9.0, -1.0, -1.0, -1.0});
		// Chord 8
		expNoDur.add(new double[]{4.0, 0.0, -1.0, 12.0, 12.0, 7.0, 5.0, -1.0});
		expNoDur.add(new double[]{4.0, 1.0, 12.0, 7.0, 12.0, 7.0, 5.0, -1.0});
		expNoDur.add(new double[]{4.0, 2.0, 7.0, 5.0, 12.0, 7.0, 5.0, -1.0});
		expNoDur.add(new double[]{4.0, 3.0, 5.0, -1.0, 12.0, 7.0, 5.0, -1.0});
		// Chords 9-14
		expNoDur.add(new double[]{1.0, 0.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0});
		expNoDur.add(new double[]{1.0, 0.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0});
		expNoDur.add(new double[]{1.0, 0.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0});
		expNoDur.add(new double[]{1.0, 0.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0});
		expNoDur.add(new double[]{1.0, 0.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0});
		expNoDur.add(new double[]{1.0, 0.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0});
		// Chord 15
		expNoDur.add(new double[]{4.0, 0.0, -1.0, 12.0, 12.0, 7.0, 5.0, -1.0});
		expNoDur.add(new double[]{4.0, 1.0, 12.0, 7.0, 12.0, 7.0, 5.0, -1.0});
		expNoDur.add(new double[]{4.0, 2.0, 7.0, 5.0, 12.0, 7.0, 5.0, -1.0});
		expNoDur.add(new double[]{4.0, 3.0, 5.0, -1.0, 12.0, 7.0, 5.0, -1.0});
		expected.addAll(expNoDur);

		// b. Modelling duration
		List<double[]> expDur = new ArrayList<double[]>();
		// Chord 0
		expDur.add(new double[]{4.0, 0.0, -1.0, 7.0, 7.0, 8.0, 4.0, -1.0});
		expDur.add(new double[]{4.0, 1.0, 7.0, 8.0, 7.0, 8.0, 4.0, -1.0});
		expDur.add(new double[]{4.0, 2.0, 8.0, 4.0, 7.0, 8.0, 4.0, -1.0});
		expDur.add(new double[]{4.0, 3.0, 4.0, -1.0, 7.0, 8.0, 4.0, -1.0});
		// Chord 1
		expDur.add(new double[]{4.0, 0.0, -1.0, 12.0, 12.0, 12.0, 3.0, -1.0});
		expDur.add(new double[]{4.0, 1.0, 12.0, 12.0, 12.0, 12.0, 3.0, -1.0});
		expDur.add(new double[]{4.0, 3.0, 3.0, -1.0, 12.0, 12.0, 3.0, -1.0});
		expDur.add(new double[]{4.0, 2.0, 12.0, 3.0, 12.0, 12.0, 3.0, -1.0});
		// Chord 2
		expDur.add(new double[]{3.0, 0.0, -1.0, 9.0, 9.0, 15.0, -1.0, -1.0});
		// Chord 3
		expDur.add(new double[]{4.0, 0.0, -1.0, 3.0, 3.0, 9.0, 6.0, -1.0});
		expDur.add(new double[]{4.0, 1.0, 3.0, 9.0, 3.0, 9.0, 6.0, -1.0});
		expDur.add(new double[]{4.0, 2.0, 9.0, 6.0, 3.0, 9.0, 6.0, -1.0});
		expDur.add(new double[]{4.0, 3.0, 6.0, -1.0, 3.0, 9.0, 6.0, -1.0});
		// Chord 4
		expDur.add(new double[]{4.0, 0.0, -1.0, 5.0, 5.0, 9.0, 6.0, -1.0});
		// Chord 5
		expDur.add(new double[]{5.0, 0.0, -1.0, 12.0, 12.0, 0.0, 3.0, 9.0});
		expDur.add(new double[]{5.0, 1.0, 12.0, 3.0, 12.0, 0.0, 3.0, 9.0});
		expDur.add(new double[]{5.0, 1.0, 12.0, 3.0, 12.0, 0.0, 3.0, 9.0});
		expDur.add(new double[]{5.0, 2.0, 3.0, 9.0, 12.0, 0.0, 3.0, 9.0});
		expDur.add(new double[]{5.0, 3.0, 9.0, -1.0, 12.0, 0.0, 3.0, 9.0});
		// Chord 6
		expDur.add(new double[]{5.0, 0.0, -1.0, 12.0, 12.0, 3.0, 4.0, 5.0});
		expDur.add(new double[]{5.0, 2.0, 3.0, 4.0, 12.0, 3.0, 4.0, 5.0});
		expDur.add(new double[]{5.0, 3.0, 4.0, 5.0, 12.0, 3.0, 4.0, 5.0});
		expDur.add(new double[]{5.0, 4.0, 5.0, -1.0, 12.0, 3.0, 4.0, 5.0});
		// Chord 7
		expDur.add(new double[]{5.0, 2.0, 2.0, 9.0, 12.0, 2.0, 9.0, 1.0});
		expDur.add(new double[]{5.0, 3.0, 9.0, 1.0, 12.0, 2.0, 9.0, 1.0});
		// Chord 8
		expDur.add(new double[]{4.0, 0.0, -1.0, 12.0, 12.0, 7.0, 5.0, -1.0});
		expDur.add(new double[]{4.0, 1.0, 12.0, 7.0, 12.0, 7.0, 5.0, -1.0});
		expDur.add(new double[]{4.0, 2.0, 7.0, 5.0, 12.0, 7.0, 5.0, -1.0});
		expDur.add(new double[]{4.0, 3.0, 5.0, -1.0, 12.0, 7.0, 5.0, -1.0});
		// Chords 9-14
		expDur.add(new double[]{4.0, 3.0, 4.0, -1.0, 12.0, 7.0, 4.0, -1.0});
		expDur.add(new double[]{4.0, 3.0, 5.0, -1.0, 12.0, 7.0, 5.0, -1.0});
		expDur.add(new double[]{4.0, 3.0, 4.0, -1.0, 12.0, 7.0, 4.0, -1.0});
		expDur.add(new double[]{4.0, 3.0, 2.0, -1.0, 12.0, 7.0, 2.0, -1.0});
		expDur.add(new double[]{4.0, 3.0, 4.0, -1.0, 12.0, 7.0, 4.0, -1.0});
		expDur.add(new double[]{4.0, 3.0, 5.0, -1.0, 12.0, 7.0, 5.0, -1.0});
		// Chord 15
		expDur.add(new double[]{4.0, 0.0, -1.0, 12.0, 12.0, 7.0, 5.0, -1.0});
		expDur.add(new double[]{4.0, 1.0, 12.0, 7.0, 12.0, 7.0, 5.0, -1.0});
		expDur.add(new double[]{4.0, 2.0, 7.0, 5.0, 12.0, 7.0, 5.0, -1.0});
		expDur.add(new double[]{4.0, 3.0, 5.0, -1.0, 12.0, 7.0, 5.0, -1.0});
		expected.addAll(expDur);

		// Calculate actual
		List<double[]> actual = new ArrayList<double[]>();
		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
		List<List<Double>> durationLabels = transcription.getDurationLabels();
		// a. Not modelling duration
//		FeatureGenerator.setModelDuration(false);
		for (int i = 0; i < basicTabSymbolProperties.length; i++) {
			actual.add(FeatureGenerator.getPositionWithinChord(basicTabSymbolProperties, durationLabels, null, 
				Direction.LEFT, i, false, false));
		}
		// b. Modelling duration
//		FeatureGenerator.setModelDuration(true);
		for (int i = 0; i < basicTabSymbolProperties.length; i++) {
			actual.add(FeatureGenerator.getPositionWithinChord(basicTabSymbolProperties, durationLabels, null,
				Direction.LEFT,	i, true, false));
		}
			   
		// Assert equality  
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
		 	for (int j = 0; j < expected.get(i).length; j++) {
		 		assertEquals(expected.get(i)[j], actual.get(i)[j]);
		 	}
		}
	}
  
  
	public void testGetPositionWithinChordNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		// Determine expected 
		List<double[]> expected = new ArrayList<double[]>();	  
		// Chord 0
		expected.add(new double[]{4.0, 0.0, -1.0, 7.0, 7.0, 8.0, 4.0, -1.0});
		expected.add(new double[]{4.0, 1.0, 7.0, 8.0, 7.0, 8.0, 4.0, -1.0});
		expected.add(new double[]{4.0, 2.0, 8.0, 4.0, 7.0, 8.0, 4.0, -1.0});
		expected.add(new double[]{4.0, 3.0, 4.0, -1.0, 7.0, 8.0, 4.0, -1.0});
		// Chord 1
		expected.add(new double[]{4.0, 0.0, -1.0, 12.0, 12.0, 12.0, 3.0, -1.0});
		expected.add(new double[]{4.0, 1.0, 12.0, 12.0, 12.0, 12.0, 3.0, -1.0});
		expected.add(new double[]{4.0, 2.0, 12.0, 3.0, 12.0, 12.0, 3.0, -1.0});
		expected.add(new double[]{4.0, 3.0, 3.0, -1.0, 12.0, 12.0, 3.0, -1.0});
		// Chord 2
		expected.add(new double[]{3.0, 0.0, -1.0, 9.0, 9.0, 15.0, -1.0, -1.0});
		// Chord 3
		expected.add(new double[]{5.0, 0.0, -1.0, 3.0, 3.0, 9.0, 6.0, 0.0});
		expected.add(new double[]{5.0, 1.0, 3.0, 9.0, 3.0, 9.0, 6.0, 0.0});
		expected.add(new double[]{5.0, 2.0, 9.0, 6.0, 3.0, 9.0, 6.0, 0.0});
		expected.add(new double[]{5.0, 3.0, 6.0, -1.0, 3.0, 9.0, 6.0, 0.0});
		expected.add(new double[]{5.0, 3.0, 6.0, -1.0, 3.0, 9.0, 6.0, 0.0});
		// Chord 4
		expected.add(new double[]{4.0, 0.0, -1.0, 5.0, 5.0, 9.0, 6.0, -1.0});
		// Chord 5
		expected.add(new double[]{5.0, 0.0, -1.0, 12.0, 12.0, 0.0, 3.0, 9.0});
		expected.add(new double[]{5.0, 1.0, 12.0, 3.0, 12.0, 0.0, 3.0, 9.0});
		expected.add(new double[]{5.0, 1.0, 12.0, 3.0, 12.0, 0.0, 3.0, 9.0});
		expected.add(new double[]{5.0, 2.0, 3.0, 9.0, 12.0, 0.0, 3.0, 9.0});
		expected.add(new double[]{5.0, 3.0, 9.0, -1.0, 12.0, 0.0, 3.0, 9.0});
		// Chord 6
		expected.add(new double[]{5.0, 0.0, -1.0, 12.0, 12.0, 3.0, 4.0, 5.0});
		expected.add(new double[]{5.0, 2.0, 3.0, 4.0, 12.0, 3.0, 4.0, 5.0});
		expected.add(new double[]{5.0, 3.0, 4.0, 5.0, 12.0, 3.0, 4.0, 5.0});
		expected.add(new double[]{5.0, 4.0, 5.0, -1.0, 12.0, 3.0, 4.0, 5.0});
		// Chord 7
		expected.add(new double[]{5.0, 2.0, 2.0, 9.0, 12.0, 2.0, 9.0, 1.0});
		expected.add(new double[]{5.0, 3.0, 9.0, 1.0, 12.0, 2.0, 9.0, 1.0});
		// Chord 8
		expected.add(new double[]{4.0, 0.0, -1.0, 12.0, 12.0, 7.0, 5.0, -1.0});
		expected.add(new double[]{4.0, 1.0, 12.0, 7.0, 12.0, 7.0, 5.0, -1.0});
		expected.add(new double[]{4.0, 2.0, 7.0, 5.0, 12.0, 7.0, 5.0, -1.0});
		expected.add(new double[]{4.0, 3.0, 5.0, -1.0, 12.0, 7.0, 5.0, -1.0});
		// Chords 9-14
		expected.add(new double[]{4.0, 3.0, 4.0, -1.0, 12.0, 7.0, 4.0, -1.0});
		expected.add(new double[]{4.0, 3.0, 5.0, -1.0, 12.0, 7.0, 5.0, -1.0});
		expected.add(new double[]{4.0, 3.0, 4.0, -1.0, 12.0, 7.0, 4.0, -1.0});
		expected.add(new double[]{4.0, 3.0, 2.0, -1.0, 12.0, 7.0, 2.0, -1.0});
		expected.add(new double[]{4.0, 3.0, 4.0, -1.0, 12.0, 7.0, 4.0, -1.0});
		expected.add(new double[]{4.0, 3.0, 5.0, -1.0, 12.0, 7.0, 5.0, -1.0});
		// Chord 15
		expected.add(new double[]{4.0, 0.0, -1.0, 12.0, 12.0, 7.0, 5.0, -1.0});
		expected.add(new double[]{4.0, 1.0, 12.0, 7.0, 12.0, 7.0, 5.0, -1.0});
		expected.add(new double[]{4.0, 2.0, 7.0, 5.0, 12.0, 7.0, 5.0, -1.0});
		expected.add(new double[]{4.0, 3.0, 5.0, -1.0, 12.0, 7.0, 5.0, -1.0});

		// Calculate actual 
		List<double[]> actual = new ArrayList<double[]>();
		Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
		for (int i = 0; i < basicNoteProperties.length; i++) {
			actual.add(FeatureGenerator.getPositionWithinChord(null, null, 
				basicNoteProperties, null, i, false, false));
		}

		// Assert equality  
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j]);
			}
		}
	}
  
  
	public void testGetSizeOfChordInclusive() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		// Determine expected
		List<Integer> expected = new ArrayList<Integer>();
		// Chord 0
		expected.add(4); expected.add(4); expected.add(4); expected.add(4);
		// Chord 1
		expected.add(4); expected.add(4); expected.add(4); expected.add(4); 
		// Chord 2
		expected.add(3);
		// Chord 3
		expected.add(4); expected.add(4); expected.add(4); expected.add(4); 
		// Chord 4
		expected.add(4);
		// Chord 5
		expected.add(5); expected.add(5); expected.add(5); expected.add(5); expected.add(5);
		// Chord 6
		expected.add(5); expected.add(5); expected.add(5); expected.add(5);
		// Chord 7
		expected.add(5); expected.add(5);
		// Chord 8
		expected.add(4); expected.add(4); expected.add(4); expected.add(4);
		// Chord 9-14
		expected.add(4); expected.add(4); expected.add(4); expected.add(4); expected.add(4); expected.add(4);
		// Chord 15
		expected.add(4); expected.add(4); expected.add(4); expected.add(4);
		
		// Calculate actual
		List<Integer> actual = new ArrayList<Integer>();
		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
		List<List<Double>> durationLabels = transcription.getDurationLabels();
//		FeatureGenerator.setModelDuration(true);
		for (int i = 0; i < basicTabSymbolProperties.length; i++) {
		  	actual.add(FeatureGenerator.getSizeOfChordInclusive(basicTabSymbolProperties, durationLabels, null, i));
		}
		
		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
		 	assertEquals(expected.get(i), actual.get(i));
		}
	}
  
  
	public void testGetSizeOfChordInclusiveNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		// Determine expected
		List<Integer> expected = new ArrayList<Integer>();
		// Chord 0
		expected.add(4); expected.add(4); expected.add(4); expected.add(4);
		// Chord 1
		expected.add(4); expected.add(4); expected.add(4); expected.add(4); 
		// Chord 2
		expected.add(3);
		// Chord 3
		expected.add(5); expected.add(5); expected.add(5); expected.add(5); expected.add(5); 
		// Chord 4
		expected.add(4);
		// Chord 5
		expected.add(5); expected.add(5); expected.add(5); expected.add(5); expected.add(5);
		// Chord 6
		expected.add(5); expected.add(5); expected.add(5); expected.add(5);
		// Chord 7
		expected.add(5); expected.add(5);
		// Chord 8
		expected.add(4); expected.add(4); expected.add(4); expected.add(4);
		// Chord 9-14
		expected.add(4); expected.add(4); expected.add(4); expected.add(4); expected.add(4); expected.add(4);
		// Chord 15
		expected.add(4); expected.add(4); expected.add(4); expected.add(4);
		  
		// Calculate actual
		List<Integer> actual = new ArrayList<Integer>();
		Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
//		List<List<Double>> durationLabels = transcription.getDurationLabels();
		for (int i = 0; i < basicNoteProperties.length; i++) {
			actual.add(FeatureGenerator.getSizeOfChordInclusive(null, null, basicNoteProperties, i));
		}
		    
		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}


	public void testGetIndexExclusive() {
		Tablature tablature = new Tablature(encodingTestpiece1, false);

		// Determine expected
		List<Integer> expected = new ArrayList<Integer>();
		// Chord 0
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		// Chord 1
		expected.add(0); expected.add(1); expected.add(3); expected.add(2);
		// Chord 2
		expected.add(0);
		// Chord 3
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		// Chord 4
		expected.add(0);
		// Chord 5
		expected.add(0); expected.add(1); expected.add(1); expected.add(2); expected.add(3);
		// Chord 6
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		// Chord 7
		expected.add(0); expected.add(1);
		// Chord 8
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		// Chord 9-14
		expected.add(0); expected.add(0); expected.add(0); expected.add(0); expected.add(0); expected.add(0); 
		// Chord 15
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		
		// Calculate actual	
		List<Integer> actual = new ArrayList<Integer>();
		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//		FeatureGenerator.setModelDuration(false);
		for (int i = 0; i < basicTabSymbolProperties.length; i++) {
		 	actual.add(FeatureGenerator.getIndexExclusive(basicTabSymbolProperties,	null, i));
		}
		
		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}
  
  
	public void testGetIndexExclusiveNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		// Determine expected
		List<Integer> expected = new ArrayList<Integer>();
		// Chord 0
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		// Chord 1
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		// Chord 2
		expected.add(0);
		// Chord 3
		expected.add(0); expected.add(1); expected.add(2); expected.add(3); expected.add(3);
		// Chord 4
		expected.add(0);
		// Chord 5
		expected.add(0); expected.add(1); expected.add(1); expected.add(2); expected.add(3);
		// Chord 6
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		// Chord 7
		expected.add(0); expected.add(1);
		// Chord 8
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		// Chord 9-14
		expected.add(0); expected.add(0); expected.add(0); expected.add(0); expected.add(0); expected.add(0); 
		// Chord 15
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		  
		// Calculate actual	
		List<Integer> actual = new ArrayList<Integer>();
		Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
		for (int i = 0; i < basicNoteProperties.length; i++) {
		 	actual.add(FeatureGenerator.getIndexExclusive(null, basicNoteProperties, i));
		}
		
		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
		 	assertEquals(expected.get(i), actual.get(i));
		}
	}
  
  
	public void testGetIndexInclusive() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		// Determine expected
		List<Integer> expected = new ArrayList<Integer>();
		// Chord 0
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		// Chord 1
		expected.add(0); expected.add(1); expected.add(3); expected.add(2);
		// Chord 2
		expected.add(0);
		// Chord 3
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		// Chord 4
		expected.add(0);
		// Chord 5
		expected.add(0); expected.add(1); expected.add(1); expected.add(2); expected.add(3);
		// Chord 6
		expected.add(0); expected.add(2); expected.add(3); expected.add(4);
		// Chord 7
		expected.add(2); expected.add(3);
		// Chord 8
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		// Chord 9-14
		expected.add(3); expected.add(3); expected.add(3); expected.add(3); expected.add(3); expected.add(3);  
		// Chord 15
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		
		// Calculate actual	
		List<Integer> actual = new ArrayList<Integer>();
		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
		List<List<Double>> durationLabels = transcription.getDurationLabels();
//		FeatureGenerator.setModelDuration(true);
		for (int i = 0; i < basicTabSymbolProperties.length; i++) {
			actual.add(FeatureGenerator.getIndexInclusive(basicTabSymbolProperties,	durationLabels, null, i));
		}
		
		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
		  	assertEquals(expected.get(i), actual.get(i));
		}
	}


	public void testGetIndexInclusiveNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		// Determine expected
		List<Integer> expected = new ArrayList<Integer>();
		// Chord 0
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		// Chord 1
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		// Chord 2
		expected.add(0);
		// Chord 3
		expected.add(0); expected.add(1); expected.add(2); expected.add(3); expected.add(3);
		// Chord 4
		expected.add(0);
		// Chord 5
		expected.add(0); expected.add(1); expected.add(1); expected.add(2); expected.add(3);
		// Chord 6
		expected.add(0); expected.add(2); expected.add(3); expected.add(4);
		// Chord 7
		expected.add(2); expected.add(3);
		// Chord 8
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		// Chord 9-14
		expected.add(3); expected.add(3); expected.add(3); expected.add(3); expected.add(3); expected.add(3);  
		// Chord 15
		expected.add(0); expected.add(1); expected.add(2); expected.add(3);
		
		// Calculate actual	
		List<Integer> actual = new ArrayList<Integer>();
		Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
		for (int i = 0; i < basicNoteProperties.length; i++) {
			actual.add(FeatureGenerator.getIndexInclusive(null,	null, basicNoteProperties, i));
		}
		 
		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}
  
  
//	public void testGetPitchesInChord() {    
//		Tablature tablature = new Tablature(encodingTestpiece1, false);
//		 
//		List<List<Integer>> expected = new ArrayList<List<Integer>>();
//		expected.add(Arrays.asList(new Integer[]{50, 57, 65, 69})); 
//		expected.add(Arrays.asList(new Integer[]{45, 57, 72, 69})); 
//		expected.add(Arrays.asList(new Integer[]{48})); 
//		expected.add(Arrays.asList(new Integer[]{47, 50, 59, 65}));
//		expected.add(Arrays.asList(new Integer[]{45}));
//		expected.add(Arrays.asList(new Integer[]{45, 57, 57, 60, 69})); 
//		expected.add(Arrays.asList(new Integer[]{45, 60, 64, 69})); 
//		expected.add(Arrays.asList(new Integer[]{59, 68})); 
//		expected.add(Arrays.asList(new Integer[]{45, 57, 64, 69}));
//		expected.add(Arrays.asList(new Integer[]{68}));
//		expected.add(Arrays.asList(new Integer[]{69}));
//		expected.add(Arrays.asList(new Integer[]{68}));
//		expected.add(Arrays.asList(new Integer[]{66}));
//		expected.add(Arrays.asList(new Integer[]{68}));
//		expected.add(Arrays.asList(new Integer[]{69}));
//		expected.add(Arrays.asList(new Integer[]{45, 57, 64, 69})); 
//
//		List<List<Integer>> actual = new ArrayList<List<Integer>>();
//		List<List<TabSymbol>> tablatureChords = tablature.getTablatureChords();
//		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//		int lowestNoteIndex = 0;
//		for (int i = 0; i < tablatureChords.size(); i++) {
//			actual.add(FeatureGenerator.getPitchesInChord(basicTabSymbolProperties, null, lowestNoteIndex));
//			lowestNoteIndex += tablatureChords.get(i).size();
//		}
//
//		assertEquals(expected.size(), actual.size());
//		for (int i = 0; i < expected.size(); i++) {
//			assertEquals(expected.get(i).size(), actual.get(i).size());
//			for (int j = 0; j < expected.get(i).size(); j++) {
//				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
//			}
//		}
//	}


//	public void testGetPitchesInChordNonTab() {	  	  
//		Transcription transcription = new Transcription(midiTestpiece1, null);
//
//		List<List<Integer>> expected = new ArrayList<List<Integer>>();
//		expected.add(Arrays.asList(new Integer[]{50, 57, 65, 69})); 
//		expected.add(Arrays.asList(new Integer[]{45, 57, 69, 72})); 
//		expected.add(Arrays.asList(new Integer[]{48})); 
//		expected.add(Arrays.asList(new Integer[]{47, 50, 59, 65, 65}));
//		expected.add(Arrays.asList(new Integer[]{45}));
//		expected.add(Arrays.asList(new Integer[]{45, 57, 57, 60, 69})); 
//		expected.add(Arrays.asList(new Integer[]{45, 60, 64, 69})); 
//		expected.add(Arrays.asList(new Integer[]{59, 68})); 
//		expected.add(Arrays.asList(new Integer[]{45, 57, 64, 69}));
//		expected.add(Arrays.asList(new Integer[]{68}));
//		expected.add(Arrays.asList(new Integer[]{69}));
//		expected.add(Arrays.asList(new Integer[]{68}));
//		expected.add(Arrays.asList(new Integer[]{66}));
//		expected.add(Arrays.asList(new Integer[]{68}));
//		expected.add(Arrays.asList(new Integer[]{69}));
//		expected.add(Arrays.asList(new Integer[]{45, 57, 64, 69})); 
//
//		List<List<Integer>> actual = new ArrayList<List<Integer>>();
//		List<List<Note>> transcriptionChords = transcription.getTranscriptionChords();
//		Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
//		int lowestNoteIndex = 0;
//		for (int i = 0; i < transcriptionChords.size(); i++) {
//			actual.add(FeatureGenerator.getPitchesInChord(null, basicNoteProperties, lowestNoteIndex));
//			lowestNoteIndex += transcriptionChords.get(i).size();
//		}
//
//		assertEquals(expected.size(), actual.size());
//		for (int i = 0; i < expected.size(); i++) {
//			assertEquals(expected.get(i).size(), actual.get(i).size());
//			for (int j = 0; j < expected.get(i).size(); j++) {
//				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
//			}
//		}
//	}


	public void testGetIntervalsInChord() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		List<double[]> expected = new ArrayList<double[]>();
		expected.add(new double[]{7.0, 8.0, 4.0, -1.0});
		expected.add(new double[]{12.0, 12.0, 3.0, -1.0});
		expected.add(new double[]{9.0, 15.0, -1.0, -1.0});
		expected.add(new double[]{3.0, 9.0, 6.0, -1.0});
		expected.add(new double[]{5.0, 9.0, 6.0, -1.0});
		expected.add(new double[]{12.0, 0.0, 3.0, 9.0});
		expected.add(new double[]{12.0, 3.0, 4.0, 5.0});
		expected.add(new double[]{12.0, 2.0, 9.0, 1.0});
		expected.add(new double[]{12.0, 7.0, 5.0, -1.0});
		expected.add(new double[]{12.0, 7.0, 4.0, -1.0});
		expected.add(new double[]{12.0, 7.0, 5.0, -1.0});
		expected.add(new double[]{12.0, 7.0, 4.0, -1.0});
		expected.add(new double[]{12.0, 7.0, 2.0, -1.0});
		expected.add(new double[]{12.0, 7.0, 4.0, -1.0});
		expected.add(new double[]{12.0, 7.0, 5.0, -1.0});
		expected.add(new double[]{12.0, 7.0, 5.0, -1.0});

		List<double[]> actual = new ArrayList<double[]>();
		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
		List<List<Double>> durationLabels = transcription.getDurationLabels();
		int lowestNoteIndex = 0;
		for (int i = 0; i < tablature.getTablatureChords().size(); i++) {
			actual.add(FeatureGenerator.getIntervalsInChord(basicTabSymbolProperties, durationLabels, null,	lowestNoteIndex));
			lowestNoteIndex += tablature.getTablatureChords().get(i).size();
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j]);
			} 
		}
	}


	public void testGetIntervalsInChordNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);
		
		// Determine expected
		List<double[]> expected = new ArrayList<double[]>();
		expected.add(new double[]{7.0, 8.0, 4.0, -1.0});
		expected.add(new double[]{12.0, 12.0, 3.0, -1.0});
		expected.add(new double[]{9.0, 15.0, -1.0, -1.0});
		expected.add(new double[]{3.0, 9.0, 6.0, 0.0});
		expected.add(new double[]{5.0, 9.0, 6.0, -1.0});
		expected.add(new double[]{12.0, 0.0, 3.0, 9.0});
		expected.add(new double[]{12.0, 3.0, 4.0, 5.0});
		expected.add(new double[]{12.0, 2.0, 9.0, 1.0});
		expected.add(new double[]{12.0, 7.0, 5.0, -1.0});
		expected.add(new double[]{12.0, 7.0, 4.0, -1.0});
		expected.add(new double[]{12.0, 7.0, 5.0, -1.0});
		expected.add(new double[]{12.0, 7.0, 4.0, -1.0});
		expected.add(new double[]{12.0, 7.0, 2.0, -1.0});
		expected.add(new double[]{12.0, 7.0, 4.0, -1.0});
		expected.add(new double[]{12.0, 7.0, 5.0, -1.0});
		expected.add(new double[]{12.0, 7.0, 5.0, -1.0});

		// Calculate actual
		List<double[]> actual = new ArrayList<double[]>();
		Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
		int lowestNoteIndex = 0;
		for (int i = 0; i < transcription.getTranscriptionChords().size(); i++) {
			actual.add(FeatureGenerator.getIntervalsInChord(null, null, basicNoteProperties, lowestNoteIndex));
			lowestNoteIndex += transcription.getTranscriptionChords().get(i).size();
		}

		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j]);
			} 
		}
	}
  
	
//  public void testGetSizeOfChord() {
//    Tablature tablature = new Tablature(encodingTestpiece1);
//  	Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);
//
//	  // Determine expected
//	  List<double[]> expected = new ArrayList<double[]>();
//	  // Chord 0
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  // Chord 1
//	  expected.add(new double[]{4.0, 4.0}); 	    
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  // Chord 2
//	  expected.add(new double[]{1.0, 3.0});
//	  // Chord 3
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  // Chord 4
//	  expected.add(new double[]{1.0, 4.0});
//	  // Chord 5
//	  expected.add(new double[]{5.0, 5.0});
//	  expected.add(new double[]{5.0, 5.0});
//	  expected.add(new double[]{5.0, 5.0});
//	  expected.add(new double[]{5.0, 5.0});
//	  expected.add(new double[]{5.0, 5.0});
//	  // Chord 6
//	  expected.add(new double[]{4.0, 5.0});
//	  expected.add(new double[]{4.0, 5.0});
//	  expected.add(new double[]{4.0, 5.0});
//	  expected.add(new double[]{4.0, 5.0});
//	  // Chord 7
//	  expected.add(new double[]{2.0, 5.0});
//	  expected.add(new double[]{2.0, 5.0});
//	  // Chord 8
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  // Chord 9-14
//	  expected.add(new double[]{1.0, 4.0});
//	  expected.add(new double[]{1.0, 4.0});
//	  expected.add(new double[]{1.0, 4.0});
//	  expected.add(new double[]{1.0, 4.0});
//	  expected.add(new double[]{1.0, 4.0});
//	  expected.add(new double[]{1.0, 4.0});
//	  // Chord 15
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	    
//	  // Calculate actual
//	  List<double[]> actual = new ArrayList<double[]>();
//	  Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//	  List<List<Double>> durationLabels = transcription.getDurationLabels();
//	  featureGenerator.setModelDuration(true);
//	  for (int i = 0; i < basicTabSymbolProperties.length; i++) {
//	  	actual.add(featureGenerator.getSizeOfChord(basicTabSymbolProperties, durationLabels, null, i));
//	  }
//	    
//	  // Assert equality
//	  assertEquals(expected.size(), actual.size());
//	  for (int i = 0; i < expected.size(); i++) {
//	  	assertEquals(expected.get(i).length, actual.get(i).length);
//	  	for (int j = 0; j < expected.get(i).length; j++) {
//	  		assertEquals(expected.get(i)[j], actual.get(i)[j]);
//	  	}
//	  }
//  }
   
  
//  public void testGetSizeOfChordNonTab() {
//  	Transcription transcription = new Transcription(midiTestpiece1, null);
//
//	 	// Determine expected
//	 	List<double[]> expected = new ArrayList<double[]>();
//	 	// Chord 0
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	 	// Chord 1
//	  expected.add(new double[]{4.0, 4.0}); 	    
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	 	// Chord 2
//	  expected.add(new double[]{1.0, 3.0});
//		// Chord 3
//	  expected.add(new double[]{5.0, 5.0});
//	  expected.add(new double[]{5.0, 5.0});
//	  expected.add(new double[]{5.0, 5.0});
//	  expected.add(new double[]{5.0, 5.0});
//	  expected.add(new double[]{5.0, 5.0});
//	 	// Chord 4
//	  expected.add(new double[]{1.0, 4.0});
//		// Chord 5
//	  expected.add(new double[]{5.0, 5.0});
//	  expected.add(new double[]{5.0, 5.0});
//	  expected.add(new double[]{5.0, 5.0});
//	  expected.add(new double[]{5.0, 5.0});
//	  expected.add(new double[]{5.0, 5.0});
//		// Chord 6
//	  expected.add(new double[]{4.0, 5.0});
//	  expected.add(new double[]{4.0, 5.0});
//	  expected.add(new double[]{4.0, 5.0});
//	  expected.add(new double[]{4.0, 5.0});
//		// Chord 7
//	  expected.add(new double[]{2.0, 5.0});
//	  expected.add(new double[]{2.0, 5.0});
//		// Chord 8
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//		// Chord 9-14
//	  expected.add(new double[]{1.0, 4.0});
//	  expected.add(new double[]{1.0, 4.0});
//	  expected.add(new double[]{1.0, 4.0});
//	  expected.add(new double[]{1.0, 4.0});
//	  expected.add(new double[]{1.0, 4.0});
//	  expected.add(new double[]{1.0, 4.0});
//	  // Chord 15
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	  expected.add(new double[]{4.0, 4.0});
//	    
//	  // Calculate actual
//	  List<double[]> actual = new ArrayList<double[]>();
//	  Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
//	  for (int i = 0; i < basicNoteProperties.length; i++) {
//	  	actual.add(featureGenerator.getSizeOfChord(null, null, basicNoteProperties, i));
//	  }
//	    
//	  // Assert equality
//	  assertEquals(expected.size(), actual.size());
//	  for (int i = 0; i < expected.size(); i++) {
//	  	assertEquals(expected.get(i).length, actual.get(i).length);
//	  	for (int j = 0; j < expected.get(i).length; j++) {
//	  		assertEquals(expected.get(i)[j], actual.get(i)[j]);
//	  	}
//	  }
//  }
	
  
//  public void testGetIndexExclusiveAndInclusive() {
//    Tablature tablature = new Tablature(encodingTestpiece1);
//  	Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);
//
// 	  // Determine expected
// 	  List<Integer[]> expected = new ArrayList<Integer[]>();
// 	  // Chord 0
// 	  expected.add(new Integer[]{0, 0}); expected.add(new Integer[]{1, 1});
// 	  expected.add(new Integer[]{2, 2}); expected.add(new Integer[]{3, 3});
// 	  // Chord 1
// 	  expected.add(new Integer[]{0, 0}); expected.add(new Integer[]{1, 1});
// 	  expected.add(new Integer[]{3, 3}); expected.add(new Integer[]{2, 2});
// 	  // Chord 2
// 	  expected.add(new Integer[]{0, 0});
// 	  // Chord 3
// 	  expected.add(new Integer[]{0, 0}); expected.add(new Integer[]{1, 1});
// 	  expected.add(new Integer[]{2, 2}); expected.add(new Integer[]{3, 3});
// 	  // Chord 4
// 	  expected.add(new Integer[]{0, 0});
// 	  // Chord 5
// 	  expected.add(new Integer[]{0, 0}); expected.add(new Integer[]{1, 1});
// 	  expected.add(new Integer[]{1, 1}); expected.add(new Integer[]{2, 2});
// 	  expected.add(new Integer[]{3, 3});
// 	  // Chord 6
//	  expected.add(new Integer[]{0, 0}); expected.add(new Integer[]{1, 2});
//	  expected.add(new Integer[]{2, 3}); expected.add(new Integer[]{3, 4});
// 	  // Chord 7
//	  expected.add(new Integer[]{0, 2}); expected.add(new Integer[]{1, 3});
// 	  // Chord 8
//	  expected.add(new Integer[]{0, 0}); expected.add(new Integer[]{1, 1});
//	  expected.add(new Integer[]{2, 2}); expected.add(new Integer[]{3, 3});
// 	  // Chord 9-14
//	  expected.add(new Integer[]{0, 3}); expected.add(new Integer[]{0, 3});
//	  expected.add(new Integer[]{0, 3}); expected.add(new Integer[]{0, 3});
//	  expected.add(new Integer[]{0, 3}); expected.add(new Integer[]{0, 3});
// 	  // Chord 15
//	  expected.add(new Integer[]{0, 0}); expected.add(new Integer[]{1, 1});
//	  expected.add(new Integer[]{2, 2}); expected.add(new Integer[]{3, 3});
// 	  
// 	  // Calculate actual	
// 	  List<Integer[]> actual = new ArrayList<Integer[]>();
// 	  Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
// 	  List<List<Double>> durationLabels = transcription.getDurationLabels();
// 	  for (int i = 0; i < basicTabSymbolProperties.length; i++) {
// 	  	actual.add(featureGenerator.getIndexExclusiveAndInclusive(basicTabSymbolProperties, 
// 	  		durationLabels, null, i));
// 	  }
// 	   
// 	  // Assert equality
// 	  assertEquals(expected.size(), actual.size());
// 	  for (int i = 0; i < expected.size(); i++) {
// 	  	assertEquals(expected.get(i).length, actual.get(i).length);
// 	  	for (int j = 0; j < expected.get(i).length; j++) {
// 	  		assertEquals(expected.get(i)[j], actual.get(i)[j]);
// 	   	}
// 	  }
//  }
  
  
//  public void testGetIndexExclusiveAndInclusiveNonTab() {
//  	Transcription transcription = new Transcription(midiTestpiece1, null);
//
// 	  // Determine expected
// 	  List<Integer[]> expected = new ArrayList<Integer[]>();
// 	  // Chord 0
// 	  expected.add(new Integer[]{0, 0}); expected.add(new Integer[]{1, 1});
// 	  expected.add(new Integer[]{2, 2}); expected.add(new Integer[]{3, 3});
// 	  // Chord 1
// 	  expected.add(new Integer[]{0, 0}); expected.add(new Integer[]{1, 1});
// 	  expected.add(new Integer[]{2, 2}); expected.add(new Integer[]{3, 3});
// 	  // Chord 2
// 	  expected.add(new Integer[]{0, 0});
// 	  // Chord 3
// 	  expected.add(new Integer[]{0, 0}); expected.add(new Integer[]{1, 1});
// 	  expected.add(new Integer[]{2, 2}); expected.add(new Integer[]{3, 3});
// 	  expected.add(new Integer[]{3, 3});
// 	  // Chord 4
// 	  expected.add(new Integer[]{0, 0});
// 	  // Chord 5
// 	  expected.add(new Integer[]{0, 0}); expected.add(new Integer[]{1, 1});
// 	  expected.add(new Integer[]{1, 1}); expected.add(new Integer[]{2, 2});
// 	  expected.add(new Integer[]{3, 3}); 
// 	  // Chord 6
//	  expected.add(new Integer[]{0, 0}); expected.add(new Integer[]{1, 2});
//	  expected.add(new Integer[]{2, 3}); expected.add(new Integer[]{3, 4});
// 	  // Chord 7
//	  expected.add(new Integer[]{0, 2}); expected.add(new Integer[]{1, 3});
// 	  // Chord 8
//	  expected.add(new Integer[]{0, 0}); expected.add(new Integer[]{1, 1});
//	  expected.add(new Integer[]{2, 2}); expected.add(new Integer[]{3, 3});
// 	  // Chord 9-14
//	  expected.add(new Integer[]{0, 3}); expected.add(new Integer[]{0, 3});
//	  expected.add(new Integer[]{0, 3}); expected.add(new Integer[]{0, 3});
//	  expected.add(new Integer[]{0, 3}); expected.add(new Integer[]{0, 3});
// 	  // Chord 15
//	  expected.add(new Integer[]{0, 0}); expected.add(new Integer[]{1, 1});
//	  expected.add(new Integer[]{2, 2}); expected.add(new Integer[]{3, 3});
// 	    
// 	  // Calculate actual	
// 	  List<Integer[]> actual = new ArrayList<Integer[]>();
// 	  Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
// 	  for (int i = 0; i < basicNoteProperties.length; i++) {
// 	  	actual.add(featureGenerator.getIndexExclusiveAndInclusive(null, null, basicNoteProperties, i));
// 	  }
// 	   
// 	  // Assert equality
// 	  assertEquals(expected.size(), actual.size());
// 	  for (int i = 0; i < expected.size(); i++) {
// 	  	assertEquals(expected.get(i).length, actual.get(i).length);
// 	  	for (int j = 0; j < expected.get(i).length; j++) {
// 	  		assertEquals(expected.get(i)[j], actual.get(i)[j]);
// 	  	}
// 	  }
//  }
	
	
	public void testGetVoicesWithAdjacentNoteOnSameCourse() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		double[] empty = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		double[] voice0 = new double[]{1.0, 0.0, 0.0, 0.0, 0.0};
		double[] voice1 = new double[]{0.0, 1.0, 0.0, 0.0, 0.0};
		double[] voice2 = new double[]{0.0, 0.0, 1.0, 0.0, 0.0};
		double[] voice3 = new double[]{0.0, 0.0, 0.0, 1.0, 0.0};
		double[] voice4 = new double[]{0.0, 0.0, 0.0, 0.0, 1.0};
		double[] voice0And1 = new double[]{1.0, 1.0, 0.0, 0.0, 0.0};
		double[] voice4And3 = new double[]{0.0, 0.0, 0.0, 1.0, 1.0};

		// Determine expected
		List<double[]> expected = new ArrayList<double[]>();
		// a. Direction.LEFT
		// Chord 0
		expected.add(empty); expected.add(empty); expected.add(empty); expected.add(empty);
		// Chord 1
		expected.add(empty); expected.add(voice2); expected.add(voice1); expected.add(voice0);
		// Chord 2
		expected.add(voice3);
		// Chord 3
		expected.add(voice3); expected.add(empty); expected.add(voice2); expected.add(voice0);
		// Chord 4
		expected.add(voice4); 
		// Chord 5
		expected.add(voice4);	expected.add(voice3);	expected.add(voice2);	expected.add(empty); expected.add(voice0And1); 	
		// Chord 6
		expected.add(voice4); expected.add(voice1);	expected.add(voice0);	expected.add(empty);
		// Chord 7
		expected.add(voice2);	expected.add(voice0);
		// Chord 8
		expected.add(voice4); expected.add(empty); expected.add(voice0); expected.add(voice1);
		// Chords 9-14
		expected.add(voice1);
		expected.add(empty);
		expected.add(voice1);
		expected.add(voice0And1);
		expected.add(voice0And1);
		expected.add(empty);
		// Chord 15
		expected.add(voice4And3);	expected.add(voice2);	expected.add(voice1);	expected.add(voice0);

		// b. Direction.RIGHT
		List<double[]> expRight =  new ArrayList<double[]>();
		// Chord 15
		expRight.add(empty); expRight.add(empty); expRight.add(empty); expRight.add(empty);
		// Chord 14-9
		expRight.add(voice0); expRight.add(voice1); expRight.add(voice0And1); expRight.add(voice0And1); 
		expRight.add(empty); expRight.add(voice1);
		// Chord 8
		expRight.add(voice3); expRight.add(voice2); expRight.add(voice0And1); expRight.add(empty);
		// Chord 7
		expRight.add(empty); expRight.add(voice1);	
		// Chord 6
		expRight.add(voice3); expRight.add(voice2); expRight.add(voice0And1); expRight.add(empty);
		// Chord 5
		expRight.add(voice4And3); expRight.add(empty); expRight.add(empty); expRight.add(voice2);
		expRight.add(voice0);
		// Chord 4
		expRight.add(voice4);
		// Chord 3
		expRight.add(voice4); expRight.add(voice3); expRight.add(voice2); expRight.add(voice0);
		// Chord 2
		expRight.add(voice4);
		// Chord 1
		expRight.add(voice4And3); expRight.add(voice2); expRight.add(voice0And1); expRight.add(empty);
		// Chord 0
		expRight.add(empty); expRight.add(voice2); expRight.add(voice0); expRight.add(voice1);
		expected.addAll(expRight);

		// Calculate actual
		List<double[]> actual = new ArrayList<double[]>();
		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
		// a. Direction.LEFT
		for (int i = 0; i < basicTabSymbolProperties.length; i++) {
			actual.add(FeatureGenerator.getVoicesWithAdjacentNoteOnSameCourse(basicTabSymbolProperties,	transcription,
				Direction.LEFT, i)); 
		}
		// b. Direction.RIGHT
		List<Integer> backwardsMapping = FeatureGenerator.getBackwardsMapping(tablature.getNumberOfNotesPerChord());
		for (int i : backwardsMapping) {
			actual.add(FeatureGenerator.getVoicesWithAdjacentNoteOnSameCourse(basicTabSymbolProperties,	transcription, 
				Direction.RIGHT, i));
		}   	

		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j]);
			}
		}
	}


	// TWEE gedaan
	public void testGetPitchAndTimeProximitiesToAllVoices() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		List<double[][]> expected = new ArrayList<double[][]>();
		// a. Direction.LEFT (decisionContextSize = 1) 	
		// Not modelling duration
		List<double[][]> expLeftNoDur = new ArrayList<double[][]>();
		// Chord 0
		expLeftNoDur.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftNoDur.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},
			{-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftNoDur.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},	
			{-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftNoDur.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},	
			{-1.0, -1.0, -1.0, -1.0, -1.0}});
		// Chord 1
		expLeftNoDur.add(new double[][]{{24.0, 20.0, 12.0, 5.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
			{0.0, 0.0, 0.0, 0.0, -1.0}});
		expLeftNoDur.add(new double[][]{{12.0, 8.0, 0.0, 7.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0},
			{0.0, 0.0, 0.0, 0.0, -1.0}});
		expLeftNoDur.add(new double[][]{{3.0, 7.0, 15.0, 22.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
			{0.0, 0.0, 0.0, 0.0, -1.0}});
		expLeftNoDur.add(new double[][]{{0.0, 4.0, 12.0, 19.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
			{0.0, 0.0, 0.0, 0.0, -1.0}});
		// Chord 2
		expLeftNoDur.add(new double[][]{{24.0, 21.0, 9.0, 3.0, -1.0}, {3/16.0, 3/16.0, 3/16.0, 3/16.0, -1.0},	
			{0.0, 0.0, 0.0, 0.0, -1.0}});
		// Chord 3
		expLeftNoDur.add(new double[][]{{25.0, 22.0, 10.0, 1.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
			{1/16.0, 1/16.0, 1/16.0, 0.0, -1.0}});
		expLeftNoDur.add(new double[][]{{22.0, 19.0, 7.0, 2.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
			{1/16.0, 1/16.0, 1/16.0, 0.0, -1.0}});
		expLeftNoDur.add(new double[][]{{13.0, 10.0, 2.0, 11.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0},
			{1/16.0, 1/16.0, 1/16.0, 0.0, -1.0}});
		expLeftNoDur.add(new double[][]{{7.0, 4.0, 8.0, 17.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
			{1/16.0, 1/16.0, 1/16.0, 0.0, -1.0}});
		// Chord 4
		expLeftNoDur.add(new double[][]{{20.0, 20.0, 14.0, 5.0, 2.0}, {1/8.0, 1/8.0, 1/8.0, 1/8.0, 1/8.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}});
		// Chord 5
		expLeftNoDur.add(new double[][]{{20.0, 20.0, 14.0, 5.0, 0.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
			{1/8.0, 1/8.0, 1/8.0, 1/8.0, 0.0}});
		expLeftNoDur.add(new double[][]{{8.0, 8.0, 2.0, 7.0, 12.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
			{1/8.0, 1/8.0, 1/8.0, 1/8.0, 0.0}});
		expLeftNoDur.add(new double[][]{{8.0, 8.0, 2.0, 7.0, 12.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0},
			{1/8.0, 1/8.0, 1/8.0, 1/8.0, 0.0}});
		expLeftNoDur.add(new double[][]{{5.0, 5.0, 1.0, 10.0, 15.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
			{1/8.0, 1/8.0, 1/8.0, 1/8.0, 0.0}});
		expLeftNoDur.add(new double[][]{{4.0, 4.0, 10.0, 19.0, 24.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
			{1/8.0, 1/8.0, 1/8.0, 1/8.0, 0.0}});
		// Chord 6
		expLeftNoDur.add(new double[][]{{24.0, 15.0, 12.0, 12.0, 0.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}});
		expLeftNoDur.add(new double[][]{{9.0, 0.0, 3.0, 3.0, 15.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}});
		expLeftNoDur.add(new double[][]{{5.0, 4.0, 7.0, 7.0, 19.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}});
		expLeftNoDur.add(new double[][]{{0.0, 9.0, 12.0, 12.0, 24.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}});
		// Chord 7
		expLeftNoDur.add(new double[][]{{5.0, 10.0, 1.0, 2.0, 14.0}, {1/8.0, 1/8.0, 1/8.0, 3/8.0, 1/8.0}, 
			{0.0, 0.0, 0.0, 1/8.0, 0.0}});
		expLeftNoDur.add(new double[][]{{4.0, 1.0, 8.0, 11.0, 23.0}, {1/8.0, 1/8.0, 1/8.0, 3/8.0, 1/8.0}, 
			{0.0, 0.0, 0.0, 1/8.0, 0.0}});
		// Chord 8
		expLeftNoDur.add(new double[][]{{23.0, 24.0, 14.0, 12.0, 0.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0},
			{0.0, 1/8.0, 0.0, 1/4.0, 1/8.0}});
		expLeftNoDur.add(new double[][]{{11.0, 12.0, 2.0, 0.0, 12.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0},
			{0.0, 1/8.0, 0.0, 1/4.0, 1/8.0}});
		expLeftNoDur.add(new double[][]{{4.0, 5.0, 5.0, 7.0, 19.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0}, 
			{0.0, 1/8.0, 0.0, 1/4.0, 1/8.0}});
		expLeftNoDur.add(new double[][]{{1.0, 0.0, 10.0, 12.0, 24.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0}, 
			{0.0, 1/8.0, 0.0, 1/4.0, 1/8.0}});
		// Chords 9-14
		expLeftNoDur.add(new double[][]{{1.0, 4.0, 11.0, 23.0, 23.0}, {1/16.0, 1/16.0, 1/16.0, 1/16.0, 5/16.0},	
			{0.0, 0.0, 0.0, 0.0, 3/16.0}});
		expLeftNoDur.add(new double[][]{{1.0, 5.0, 12.0, 24.0, 24.0}, {1/16.0, 1/8.0, 1/8.0, 1/8.0, 3/8.0},	
			{0.0, 1/16.0, 1/16.0, 1/16.0, 1/4.0}});
		expLeftNoDur.add(new double[][]{{1.0, 4.0, 11.0, 23.0, 23.0}, {1/32.0, 5/32.0, 5/32.0, 5/32.0, 13/32.0},
			{0.0, 3/32.0, 3/32.0, 3/32.0, 9/32.0}});
		expLeftNoDur.add(new double[][]{{2.0, 2.0, 9.0, 21.0, 21.0}, {1/32.0, 3/16.0, 3/16.0, 3/16.0, 7/16.0}, 
			{0.0, 1/8.0, 1/8.0, 1/8.0, 5./16.0}});
		expLeftNoDur.add(new double[][]{{2.0, 4.0, 11.0, 23.0, 23.0}, {1/32.0, 7/32.0, 7/32.0, 7/32.0, 15/32.0},
			{0.0, 5/32.0, 5/32.0, 5/32.0, 11/32.0}});
		expLeftNoDur.add(new double[][]{{1.0, 5.0, 12.0, 24.0, 24.0}, {1/32.0, 1/4.0, 1/4.0, 1/4.0, 1/2.0}, 
			{0.0, 3/16.0, 3/16.0, 3/16.0, 3/8.0}});
		// Chord 15
		expLeftNoDur.add(new double[][]{{24.0, 19.0, 12.0, 0.0, 0.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 1.0}, 
			{1/4.0, 11/16.0, 11/16.0, 11/16.0, 7/8.0}});
		expLeftNoDur.add(new double[][]{{12.0, 7.0, 0.0, 12.0, 12.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 1.0},
			{1/4.0, 11/16.0, 11/16.0, 11/16.0, 7/8.0}});
		expLeftNoDur.add(new double[][]{{5.0, 0.0, 7.0, 19.0, 19.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 1.0},
			{1/4.0, 11/16.0, 11/16.0, 11/16.0, 7/8.0}});
		expLeftNoDur.add(new double[][]{{0.0, 5.0, 12.0, 24.0, 24.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 1.0},
			{1/4.0, 11/16.0, 11/16.0, 11/16.0, 7/8.0}});
		expected.addAll(expLeftNoDur);

		// Modelling duration
		List<double[][]> expLeftDur = new ArrayList<double[][]>();
		for (int i = 0; i < expLeftNoDur.size(); i++) {
			double[][] allProxCurrentNote = expLeftNoDur.get(i); 
			double[][] newAllProxCurrentNote = new double[3][5];
			for (int j = 0; j < allProxCurrentNote.length; j++) {
				newAllProxCurrentNote[j] = Arrays.copyOf(allProxCurrentNote[j], allProxCurrentNote[j].length);
			}
			expLeftDur.add(newAllProxCurrentNote);
		}
		// Chord 0
		expLeftDur.get(0)[2] = new double[]{-1.0, -1.0, -1.0, -1.0, -1.0};
		expLeftDur.get(1)[2] = new double[]{-1.0, -1.0, -1.0, -1.0, -1.0};
		expLeftDur.get(2)[2] = new double[]{-1.0, -1.0, -1.0, -1.0, -1.0};
		expLeftDur.get(3)[2] = new double[]{-1.0, -1.0, -1.0, -1.0, -1.0};
		// Chord 1
		expLeftDur.get(4)[2] = new double[]{0.0, 0.0, 0.0, 0.0, -1.0};
		expLeftDur.get(5)[2] = new double[]{0.0, 0.0, 0.0, 0.0, -1.0};
		expLeftDur.get(6)[2] = new double[]{0.0, 0.0, 0.0, 0.0, -1.0};
		expLeftDur.get(7)[2] = new double[]{0.0, 0.0, 0.0, 0.0, -1.0};
		// Chord 2
		expLeftDur.get(8)[2] = new double[]{-1/16.0, 1/16.0, -1/16.0, 0.0, -1.0};
		// Chord 3
		expLeftDur.get(9)[2] = new double[]{0.0, 1/8.0, 0.0, 0.0, -1.0};
		expLeftDur.get(10)[2] = new double[]{0.0, 1/8.0, 0.0, 0.0, -1.0};
		expLeftDur.get(11)[2] = new double[]{0.0, 1/8.0, 0.0, 0.0, -1.0};
		expLeftDur.get(12)[2] = new double[]{0.0, 1/8.0, 0.0, 0.0, -1.0};
		// Chord 4
		expLeftDur.get(13)[2] = new double[]{-1/8.0, 0.0, -1/8.0, -1/8.0, 0.0};
		// Chord 5
		expLeftDur.get(14)[2] = new double[]{0.0, 1/8.0, 0.0, 0.0, 0.0};
		expLeftDur.get(15)[2] = new double[]{0.0, 1/8.0, 0.0, 0.0, 0.0};
		expLeftDur.get(16)[2] = new double[]{0.0, 1/8.0, 0.0, 0.0, 0.0};
		expLeftDur.get(17)[2] = new double[]{0.0, 1/8.0, 0.0, 0.0, 0.0};
		expLeftDur.get(18)[2] = new double[]{0.0, 1/8.0, 0.0, 0.0, 0.0};
		// Chord 6
		expLeftDur.get(19)[2] = new double[]{0.0, 0.0, 0.0, -1/4.0, 0.0};
		expLeftDur.get(20)[2] = new double[]{0.0, 0.0, 0.0, -1/4.0, 0.0};
		expLeftDur.get(21)[2] = new double[]{0.0, 0.0, 0.0, -1/4.0, 0.0};
		expLeftDur.get(22)[2] = new double[]{0.0, 0.0, 0.0, -1/4.0, 0.0};
		// Chord 7
		expLeftDur.get(23)[2] = new double[]{0.0, -1/8.0, 0.0, -1/8.0, -1/8.0};
		expLeftDur.get(24)[2] = new double[]{0.0, -1/8.0, 0.0, -1/8.0, -1/8.0};
		// Chord 8
		expLeftDur.get(25)[2] = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		expLeftDur.get(26)[2] = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		expLeftDur.get(27)[2] = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		expLeftDur.get(28)[2] = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		// Chord 9-14
		expLeftDur.get(29)[2] = new double[]{0.0, -7/16.0, -7/16.0, -7/16.0, 1/16.0};
		expLeftDur.get(30)[2] = new double[]{0.0, -3/8.0, -3/8.0, -3/8.0, 1/8.0};
		expLeftDur.get(31)[2] = new double[]{0.0, -11/32.0, -11/32.0, -11/32.0, 5/32.0};
		expLeftDur.get(32)[2] = new double[]{0.0, -5/16.0, -5/16.0, -5/16.0, 3/16.0};
		expLeftDur.get(33)[2] = new double[]{0.0, -9/32.0, -9/32.0, -9/32.0, 7/32.0};
		expLeftDur.get(34)[2] = new double[]{0.0, -1/4.0, -1/4.0, -1/4.0, 1/4.0};
		// Chord 15
		expLeftDur.get(35)[2] = new double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0, 3/4.0};
		expLeftDur.get(36)[2] = new double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0, 3/4.0};
		expLeftDur.get(37)[2] = new double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0, 3/4.0};
		expLeftDur.get(38)[2] = new double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0, 3/4.0};
		expected.addAll(expLeftDur);
		
		// b. Direction.LEFT (decisionContextSize = 3) 	
		// Not modelling duration
		List<double[][]> expLeftThree = new ArrayList<double[][]>();
		// Chord 0
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},	
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},	
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		// Chord 1
		expLeftThree.add(new double[][]{{24.0, 20.0, 12.0, 5.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
			{0.0, 0.0, 0.0, 0.0, -1.0}, {0.0, 0.0, 0.0, 0.0, -1.0}}); 
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{12.0, 8.0, 0.0, 7.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0},
			{0.0, 0.0, 0.0, 0.0, -1.0}, {0.0, 0.0, 0.0, 1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{3.0, 7.0, 15.0, 22.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
			{0.0, 0.0, 0.0, 0.0, -1.0}, {1.0, 1.0, 1.0, 1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{0.0, 4.0, 12.0, 19.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
			{0.0, 0.0, 0.0, 0.0, -1.0}, {0.0, 1.0, 1.0, 1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		// Chord 2
		expLeftThree.add(new double[][]{{24.0, 21.0, 9.0, 3.0, -1.0}, {3/16.0, 3/16.0, 3/16.0, 3/16.0, -1.0},	
			{0.0, 0.0, 0.0, 0.0, -1.0}, {0.0, 0.0, 0.0, 1.0, -1.0}}); 
		expLeftThree.add(new double[][]{{21.0, 17.0, 9.0, 2.0, -1.0}, {7/16.0, 7/16.0, 7/16.0, 7/16.0, -1.0}, 
			{3/16.0, 3/16.0, 3/16.0, 3/16.0, -1.0}, {0.0, 0.0, 0.0, 0.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		// Chord 3
		expLeftThree.add(new double[][]{{25.0, 22.0, 10.0, 1.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
			{1/16.0, 1/16.0, 1/16.0, 0.0, -1.0}, {0.0, 0.0, 0.0, 0.0, -1.0}});
		expLeftThree.add(new double[][]{{22.0, 18.0, 10.0, 2.0, -1.0}, {2/4.0, 2/4.0, 2/4.0, 1/4.0, -1.0}, 
			{1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, {0.0, 0.0, 0.0, 1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, 3.0, -1.0}, {-1.0, -1.0, -1.0, 2/4.0, -1.0}, 
			{-1.0, -1.0, -1.0, 1/4.0, -1.0}, {-1.0, -1.0, -1.0, 0.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{22.0, 19.0, 7.0, 2.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
			{1/16.0, 1/16.0, 1/16.0, 0.0, -1.0}, {0.0, 0.0, 0.0, 1.0, -1.0}}); 
		expLeftThree.add(new double[][]{{19.0, 15.0, 7.0, 5.0, -1.0}, {2/4.0, 2/4.0, 2/4.0, 1/4.0, -1.0}, 
			{1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, {0.0, 0.0, 0.0, 1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, 0.0, -1.0}, {-1.0, -1.0, -1.0, 2/4.0, -1.0}, 
			{-1.0, -1.0, -1.0, 1/4.0, -1.0}, {-1.0, -1.0, -1.0, 0.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{13.0, 10.0, 2.0, 11.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0},
			{1/16.0, 1/16.0, 1/16.0, 0.0, -1.0}, {0.0, 0.0, 1.0, 1.0, -1.0}}); 
		expLeftThree.add(new double[][]{{10.0, 6.0, 2.0, 14.0, -1.0}, {2/4.0, 2/4.0, 2/4.0, 1/4.0, -1.0}, 
			{1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, {0.0, 0.0, 1.0, 1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, 9.0, -1.0}, {-1.0, -1.0, -1.0, 2/4.0, -1.0}, 
			{-1.0, -1.0, -1.0, 1/4.0, -1.0}, {-1.0, -1.0, -1.0, 1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{7.0, 4.0, 8.0, 17.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
			{1/16.0, 1/16.0, 1/16.0, 0.0, -1.0}, {0.0, 0.0, 1.0, 1.0, -1.0}}); 
		expLeftThree.add(new double[][]{{4.0, 0.0, 8.0, 20.0, -1.0}, {2/4.0, 2/4.0, 2/4.0, 1/4.0, -1.0}, 
			{1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, {0.0, 0.0, 1.0, 1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, 15.0, -1.0}, {-1.0, -1.0, -1.0, 2/4.0, -1.0}, 
			{-1.0, -1.0, -1.0, 1/4.0, -1.0}, {-1.0, -1.0, -1.0, 1.0, -1.0}});
		// Chord 4
		expLeftThree.add(new double[][]{{20.0, 20.0, 14.0, 5.0, 2.0}, {1/8.0, 1/8.0, 1/8.0, 1/8.0, 1/8.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 0.0, 0.0, 0.0}}); 
		expLeftThree.add(new double[][]{{27.0, 24.0, 12.0, 3.0, -1.0}, {3/8.0, 3/8.0, 3/8.0, 3/16.0, -1.0}, 
			{3/16.0, 3/16.0, 3/16.0, 1/8.0, -1.0}, {0.0, 0.0, 0.0, 0.0, -1.0}});
		expLeftThree.add(new double[][]{{24.0, 20.0, 12.0, 0.0, -1.0}, {5/8.0, 5/8.0, 5/8.0, 3/8.0, -1.0}, 
			{3/8.0, 3/8.0, 3/8.0, 3/16.0, -1.0}, {0.0, 0.0, 0.0, 0.0, -1.0}});
		// Chord 5
		expLeftThree.add(new double[][]{{20.0, 20.0, 14.0, 5.0, 0.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
			{1/8.0, 1/8.0, 1/8.0, 1/8.0, 0.0}, {0.0, 0.0, 0.0, 0.0, 0.0}}); 
		expLeftThree.add(new double[][]{{27.0, 24.0, 12.0, 3.0, 2.0}, {2/4.0, 2/4.0, 2/4.0, 5/16.0, 1/4.0}, 
			{5/16.0, 5/16.0, 5/16.0, 1/4.0, 1/8.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		expLeftThree.add(new double[][]{{24.0, 20.0, 12.0, 0.0, -1.0}, {3/4.0, 3/4.0, 3/4.0, 2/4.0, -1.0}, 
			{2/4.0, 2/4.0, 2/4.0, 5/16.0, -1.0}, {0.0, 0.0, 0.0, 0.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{8.0, 8.0, 2.0, 7.0, 12.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
			{1/8.0, 1/8.0, 1/8.0, 1/8.0, 0.0}, {0.0, 0.0, 0.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{15.0, 12.0, 0.0, 9.0, 10.0}, {2/4.0, 2/4.0, 2/4.0, 5/16.0, 1/4.0}, 
			{5/16.0, 5/16.0, 5/16.0, 1/4.0, 1/8.0}, {0.0, 0.0, 0.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{12.0, 8.0, 0.0, 12.0, -1.0}, {3/4.0, 3/4.0, 3/4.0, 2/4.0, -1.0}, 
			{2/4.0, 2/4.0, 2/4.0, 5/16.0, -1.0}, {0.0, 0.0, 0.0, 1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{8.0, 8.0, 2.0, 7.0, 12.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0},
			{1/8.0, 1/8.0, 1/8.0, 1/8.0, 0.0}, {0.0, 0.0, 0.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{15.0, 12.0, 0.0, 9.0, 10.0}, {2/4.0, 2/4.0, 2/4.0, 5/16.0, 1/4.0}, 
			{5/16.0, 5/16.0, 5/16.0, 1/4.0, 1/8.0}, {0.0, 0.0, 0.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{12.0, 8.0, 0.0, 12.0, -1.0}, {3/4.0, 3/4.0, 3/4.0, 2/4.0, -1.0}, 
			{2/4.0, 2/4.0, 2/4.0, 5/16.0, -1.0}, {0.0, 0.0, 0.0, 1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{5.0, 5.0, 1.0, 10.0, 15.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
			{1/8.0, 1/8.0, 1/8.0, 1/8.0, 0.0}, {0.0, 0.0, 1.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{12.0, 9.0, 3.0, 12.0, 13.0}, {2/4.0, 2/4.0, 2/4.0, 5/16.0, 1/4.0}, 
			{5/16.0, 5/16.0, 5/16.0, 1/4.0, 1/8.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{9.0, 5.0, 3.0, 15.0, -1.0}, {3/4.0, 3/4.0, 3/4.0, 2/4.0, -1.0}, 
			{2/4.0, 2/4.0, 2/4.0, 5/16.0, -1.0}, {0.0, 0.0, 1.0, 1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{4.0, 4.0, 10.0, 19.0, 24.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
			{1/8.0, 1/8.0, 1/8.0, 1/8.0, 0.0}, {1.0, 1.0, 1.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{3.0, 0.0, 12.0, 21.0, 22.0}, {2/4.0, 2/4.0, 2/4.0, 5/16.0, 1/4.0}, 
			{5/16.0, 5/16.0, 5/16.0, 1/4.0, 1/8.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{0.0, 4.0, 12.0, 24.0, -1.0}, {3/4.0, 3/4.0, 3/4.0, 2/4.0, -1.0}, 
			{2/4.0, 2/4.0, 2/4.0, 5/16.0, -1.0}, {0.0, 1.0, 1.0, 1.0, -1.0}}); 
		// Chord 6
		expLeftThree.add(new double[][]{{24.0, 15.0, 12.0, 12.0, 0.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		expLeftThree.add(new double[][]{{20.0, 20.0, 14.0, 5.0, 0.0}, {2/4.0, 2/4.0, 2/4.0, 2/4.0, 3/8.0}, 
			{3/8.0, 3/8.0, 3/8.0, 3/8.0, 1/4.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		expLeftThree.add(new double[][]{{27.0, 24.0, 12.0, 3.0, 2.0}, {3/4.0, 3/4.0, 3/4.0, 9/16.0, 2/4.0}, 
			{9/16.0, 9/16.0, 9/16.0, 2/4.0, 3/8.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		//
		expLeftThree.add(new double[][]{{9.0, 0.0, 3.0, 3.0, 15.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{5.0, 5.0, 1.0, 10.0, 15.0}, {2/4.0, 2/4.0, 2/4.0, 2/4.0, 3/8.0}, 
			{3/8.0, 3/8.0, 3/8.0, 3/8.0, 1/4.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{12.0, 9.0, 3.0, 12.0, 13.0}, {3/4.0, 3/4.0, 3/4.0, 9/16.0, 2/4.0}, 
			{9/16.0, 9/16.0, 9/16.0, 2/4.0, 3/8.0}, {0.0, 0.0, 1.0, 1.0, 1.0}}); 
		//
		expLeftThree.add(new double[][]{{5.0, 4.0, 7.0, 7.0, 19.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{1.0, 1.0, 5.0, 14.0, 19.0}, {2/4.0, 2/4.0, 2/4.0, 2/4.0, 3/8.0}, 
			{3/8.0, 3/8.0, 3/8.0, 3/8.0, 1/4.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{8.0, 5.0, 7.0, 16.0, 17.0}, {3/4.0, 3/4.0, 3/4.0, 9/16.0, 2/4.0}, 
			{9/16.0, 9/16.0, 9/16.0, 2/4.0, 3/8.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{0.0, 9.0, 12.0, 12.0, 24.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{4.0, 4.0, 10.0, 19.0, 24.0}, {2/4.0, 2/4.0, 2/4.0, 2/4.0, 3/8.0}, 
			{3/8.0, 3/8.0, 3/8.0, 3/8.0, 1/4.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{3.0, 0.0, 12.0, 21.0, 22.0}, {3/4.0, 3/4.0, 3/4.0, 9/16.0, 2/4.0}, 
			{9/16.0, 9/16.0, 9/16.0, 2/4.0, 3/8.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		// Chord 7
		expLeftThree.add(new double[][]{{5.0, 10.0, 1.0, 2.0, 14.0}, {1/8.0, 1/8.0, 1/8.0, 3/8.0, 1/8.0}, 
			{0.0, 0.0, 0.0, 1/8.0, 0.0}, {0.0, 0.0, 0.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{10.0, 1.0, 2.0, 9.0, 14.0}, {3/8.0, 3/8.0, 3/8.0, 5/8.0, 3/8.0}, 
			{1/8.0, 1/8.0, 1/8.0, 1/2.0, 1/8.0}, {0.0, 0.0, 1.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{6.0, 6.0, 0.0, 11.0, 14.0}, {5/8.0, 5/8.0, 5/8.0, 11/16.0, 2/4.0}, 
			{2/4.0, 2/4.0, 2/4.0, 5/8.0, 3/8.0}, {0.0, 0.0, 0.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{4.0, 1.0, 8.0, 11.0, 23.0}, {1/8.0, 1/8.0, 1/8.0, 3/8.0, 1/8.0}, 
			{0.0, 0.0, 0.0, 1/8.0, 0.0}, {1.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{1.0, 8.0, 11.0, 18.0, 23.0}, {3/8.0, 3/8.0, 3/8.0, 5/8.0, 3/8.0}, 
			{1/8.0, 1/8.0, 1/8.0, 1/2.0, 1/8.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{3.0, 3.0, 9.0, 20.0, 23.0}, {5/8.0, 5/8.0, 5/8.0, 11/16.0, 2/4.0}, 
			{2/4.0, 2/4.0, 2/4.0, 5/8.0, 3/8.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		// Chord 8
		expLeftThree.add(new double[][]{{23.0, 24.0, 14.0, 12.0, 0.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0},
			{0.0, 1/8.0, 0.0, 1/4.0, 1/8.0}, {0.0, 0.0, 0.0, 0.0, 0.0}}); 
		expLeftThree.add(new double[][]{{19.0, 15.0, 15.0, 5.0, 0.0}, {1/4.0, 2/4.0, 1/4.0, 3/4.0, 2/4.0}, 
			{1/8.0, 1/4.0, 1/8.0, 5/8.0, 1/4.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		expLeftThree.add(new double[][]{{24.0, 20.0, 12.0, 3.0, 0.0}, {2/4.0, 3/4.0, 2/4.0, 13/16.0, 5/8.0}, 
			{1/4.0, 5/8.0, 1/4.0, 3/4.0, 2/4.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		//
		expLeftThree.add(new double[][]{{11.0, 12.0, 2.0, 0.0, 12.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0},
			{0.0, 1/8.0, 0.0, 1/4.0, 1/8.0}, {0.0, 0.0, 0.0, 0.0, 1.0}});
		expLeftThree.add(new double[][]{{7.0, 3.0, 3.0, 7.0, 12.0}, {1/4.0, 2/4.0, 1/4.0, 3/4.0, 2/4.0}, 
			{1/8.0, 1/4.0, 1/8.0, 5/8.0, 1/4.0}, {0.0, 0.0, 0.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{12.0, 8.0, 0.0, 9.0, 12.0}, {2/4.0, 3/4.0, 2/4.0, 13/16.0, 5/8.0}, 
			{1/4.0, 5/8.0, 1/4.0, 3/4.0, 2/4.0}, {0.0, 0.0, 0.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{4.0, 5.0, 5.0, 7.0, 19.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0}, 
			{0.0, 1/8.0, 0.0, 1/4.0, 1/8.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{0.0, 4.0, 4.0, 14.0, 19.0}, {1/4.0, 2/4.0, 1/4.0, 3/4.0, 2/4.0}, 
			{1/8.0, 1/4.0, 1/8.0, 5/8.0, 1/4.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{5.0, 1.0, 7.0, 16.0, 19.0}, {2/4.0, 3/4.0, 2/4.0, 13/16.0, 5/8.0}, 
			{1/4.0, 5/8.0, 1/4.0, 3/4.0, 2/4.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{1.0, 0.0, 10.0, 12.0, 24.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0}, 
			{0.0, 1/8.0, 0.0, 1/4.0, 1/8.0}, {1.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{5.0, 9.0, 9.0, 19.0, 24.0}, {1/4.0, 2/4.0, 1/4.0, 3/4.0, 2/4.0}, 
			{1/8.0, 1/4.0, 1/8.0, 5/8.0, 1/4.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{0.0, 4.0, 12.0, 21.0, 24.0}, {2/4.0, 3/4.0, 2/4.0, 13/16.0, 5/8.0}, 
			{1/4.0, 5/8.0, 1/4.0, 3/4.0, 2/4.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		// Chords 9-14
		expLeftThree.add(new double[][]{{1.0, 4.0, 11.0, 23.0, 23.0}, {1/16.0, 1/16.0, 1/16.0, 1/16.0, 5/16.0},	
			{0.0, 0.0, 0.0, 0.0, 3/16.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{0.0, 1.0, 9.0, 11.0, 23.0}, {3/16.0, 5/16.0, 3/16.0, 9/16.0, 9/16.0}, 
			{1/16.0, 3/16.0, 1/16.0, 5/16.0, 5/16.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{4.0, 8.0, 8.0, 18.0, 23.0}, {5/16.0, 9/16.0, 5/16.0, 13/16.0, 11/16.0}, 
			{3/16.0, 5/16.0, 3/16.0, 11/16.0, 9/16.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{1.0, 5.0, 12.0, 24.0, 24.0}, {1/16.0, 1/8.0, 1/8.0, 1/8.0, 3/8.0},	
			{0.0, 1/16.0, 1/16.0, 1/16.0, 1/4.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{0.0, 0.0, 10.0, 12.0, 24.0}, {1/8.0, 3/8.0, 2/8.0, 5/8.0, 5/8.0}, 
			{1/16.0, 2/8.0, 1/8.0, 3/8.0, 3/8.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{1.0, 9.0, 9.0, 19.0, 24.0}, {2/8.0, 5/8.0, 3/8.0, 7/8.0, 6/8.0}, 
			{1/8.0, 3/8.0, 2/8.0, 6/8.0, 5/8.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{1.0, 4.0, 11.0, 23.0, 23.0}, {1/32.0, 5/32.0, 5/32.0, 5/32.0, 13/32.0},
			{0.0, 3/32.0, 3/32.0, 3/32.0, 9/32.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{0.0, 1.0, 9.0, 11.0, 23.0}, {3/32.0, 13/32.0, 9/32.0, 21/32.0, 21/32.0}, 
			{1/32.0, 9/32.0, 5/32.0, 13/32.0, 13/32.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{1.0, 8.0, 8.0, 18.0, 23.0}, {5/32.0, 21/32.0, 13/32.0, 29/32.0, 25/32.0}, 
			{3/32.0, 13/32.0, 9/32.0, 25/32.0, 21/32.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{2.0, 2.0, 9.0, 21.0, 21.0}, {1/32.0, 3/16.0, 3/16.0, 3/16.0, 7/16.0}, 
			{0.0, 1/8.0, 1/8.0, 1/8.0, 5/16.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{3.0, 3.0, 7.0, 9.0, 21.0}, {1/16.0, 7/16.0, 5/16.0, 11/16.0, 11/16.0}, 
			{1/32.0, 5/16.0, 3/16.0, 7/16.0, 7/16.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{2.0, 6.0, 6.0, 16.0, 21.0}, {2/16.0, 11/16.0, 7/16.0, 15/16.0, 13/16.0}, 
			{1/16.0, 7/16.0, 5/16.0, 13/16.0, 11/16.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{2.0, 4.0, 11.0, 23.0, 23.0}, {1/32.0, 7/32.0, 7/32.0, 7/32.0, 15/32.0},
			{0.0, 5/32.0, 5/32.0, 5/32.0, 11/32.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{0.0, 1.0, 9.0, 11.0, 23.0}, {2/32.0, 15/32.0, 11/32.0, 23/32.0, 23/32.0}, 
			{1/32.0, 11/32.0, 7/32.0, 15/32.0, 15/32.0}, {0.0, 0.0, 1.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{1.0, 8.0, 8.0, 18.0, 23.0}, {3/32.0, 23/32.0, 15/32.0, 31/32.0, 27/32.0}, 
			{2/32.0, 15/32.0, 11/32.0, 27/32.0, 23/32.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{1.0, 5.0, 12.0, 24.0, 24.0}, {1/32.0, 1/4.0, 1/4.0, 1/4.0, 1/2.0}, 
			{0.0, 3/16.0, 3/16.0, 3/16.0, 3/8.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{3.0, 0.0, 10.0, 12.0, 24.0}, {1/16.0, 2/4.0, 3/8.0, 3/4.0, 3/4.0}, 
			{1/32.0, 3/8.0, 1/4.0, 2/4.0, 2/4.0}, {1.0, 0.0, 1.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{1.0, 9.0, 9.0, 19.0, 24.0}, {3/32.0, 3/4.0, 2/4.0, 4/4.0, 7/8.0}, 
			{2/32.0, 2/4.0, 3/8.0, 7/8.0, 3/4.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		// Chord 15
		expLeftThree.add(new double[][]{{24.0, 19.0, 12.0, 0.0, 0.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 4/4.0}, 
			{1/4.0, 11/16.0, 11/16.0, 11/16.0, 7/8.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		expLeftThree.add(new double[][]{{23.0, 24.0, 14.0, 12.0, 0.0}, {17/32.0, 4/4.0, 7/8.0, 5/4.0, 5/4.0}, 
			{2/4.0, 7/8.0, 3/4.0, 4/4.0, 4/4.0}, {0.0, 0.0, 0.0, 0.0, 0.0}}); 
		expLeftThree.add(new double[][]{{21.0, 15.0, 15.0, 5.0, 0.0}, {9/16.0, 5/4.0, 4/4.0, 6/4.0, 11/8.0}, 
			{17/32.0, 4/4.0, 7/8.0, 11/8.0, 5/4.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		//
		expLeftThree.add(new double[][]{{12.0, 7.0, 0.0, 12.0, 12.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 4/4.0},
			{1/4.0, 11/16.0, 11/16.0, 11/16.0, 7/8.0}, {0.0, 0.0, 0.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{11.0, 12.0, 2.0, 0.0, 12.0}, {17/32.0, 4/4.0, 7/8.0, 5/4.0, 5/4.0}, 
			{2/4.0, 7/8.0, 3/4.0, 4/4.0, 4/4.0}, {0.0, 0.0, 0.0, 0.0, 1.0}});
		expLeftThree.add(new double[][]{{9.0, 3.0, 3.0, 7.0, 12.0}, {9/16.0, 5/4.0, 4/4.0, 6/4.0, 11/8.0}, 
			{17/32.0, 4/4.0, 7/8.0, 11/8.0, 5/4.0}, {0.0, 0.0, 0.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{5.0, 0.0, 7.0, 19.0, 19.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 4/4.0},
			{1/4.0, 11/16.0, 11/16.0, 11/16.0, 7/8.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{4.0, 5.0, 5.0, 7.0, 19.0}, {17/32.0, 4/4.0, 7/8.0, 5/4.0, 5/4.0}, 
			{2/4.0, 7/8.0, 3/4.0, 4/4.0, 4/4.0}, {0.0, 0.0, 1.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{2.0, 4.0, 4.0, 14.0, 19.0}, {9/16.0, 5/4.0, 4/4.0, 6/4.0, 11/8.0}, 
			{17/32.0, 4/4.0, 7/8.0, 11/8.0, 5/4.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{0.0, 5.0, 12.0, 24.0, 24.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 4/4.0},
			{1/4.0, 11/16.0, 11/16.0, 11/16.0, 7/8.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{1.0, 0.0, 10.0, 12.0, 24.0}, {17/32.0, 4/4.0, 7/8.0, 5/4.0, 5/4.0}, 
			{2/4.0, 7/8.0, 3/4.0, 4/4.0, 4/4.0}, {1.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{3.0, 9.0, 9.0, 19.0, 24.0}, {9/16.0, 5/4.0, 4/4.0, 6/4.0, 11/8.0}, 
			{17/32.0, 4/4.0, 7/8.0, 11/8.0, 5/4.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		expected.addAll(expLeftThree);

		// c. Direction.RIGHT (decisionContextSize = 1)  
		// Using the bwd model (where only the minimum duration of the current note is known (= not modelling duration))
		List<double[][]> expRightBwd = new ArrayList<double[][]>();
		// Chord 15
		expRightBwd.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}});
		expRightBwd.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}});
		expRightBwd.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}});
		expRightBwd.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}});
		// Chords 14-9
		expRightBwd.add(new double[][]{{0.0, 5.0, 12.0, 24.0, -1.0}, {-1/2.0, -1/2.0, -1/2.0, -1/2.0, -1.0},	
			{-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1.0}});
		expRightBwd.add(new double[][]{{1.0, 4.0, 11.0, 23.0, -1.0}, {-1/32.0, -17/32.0, -17/32.0, -17/32.0, -1.0},	
			{0.0, -1/2.0, -1/2.0, -1/2.0, -1.0}});
		expRightBwd.add(new double[][]{{2.0, 2.0, 9.0, 21.0, -1.0}, {-1/32.0, -9/16.0, -9/16.0, -9/16.0, -1.0},	
			{0.0, -17/32.0, -17/32.0, -17/32.0, -1.0}});
		expRightBwd.add(new double[][]{{2.0, 4.0, 11.0, 23.0, -1.0}, {-1/32.0, -19/32.0, -19/32.0, -19/32.0, -1.0},	
			{0.0, -9/16.0, -9/16.0, -9/16.0, -1.0}});
		expRightBwd.add(new double[][]{{1.0, 5.0, 12.0, 24.0, -1.0}, {-1/32.0, -5/8.0, -5/8.0, -5/8.0, -1.0},	
			{0.0, -19/32.0, -19/32.0, -19/32.0, -1.0}});
		expRightBwd.add(new double[][]{{1.0, 4.0, 11.0, 23.0, -1.0}, {-1/16.0, -11/16.0, -11/16.0, -11/16.0, -1.0},	
			{0.0, -5/8.0, -5/8.0, -5/8.0, -1.0}});
		// Chord 8
		expRightBwd.add(new double[][]{{23.0, 19.0, 12.0, 0.0, -1.0}, {-1/16.0, -3/4.0, -3/4.0, -3/4.0, -1.0},
			{0.0, -11/16.0, -11/16.0, -11/16.0, -1.0}});
		expRightBwd.add(new double[][]{{11.0, 7.0, 0.0, 12.0, -1.0}, {-1/16.0, -3/4.0, -3/4.0, -3/4.0, -1.0},
			{0.0, -11/16.0, -11/16.0, -11/16.0, -1.0}});
		expRightBwd.add(new double[][]{{4.0, 0.0, 7.0, 19.0, -1.0}, {-1/16.0, -3/4.0, -3/4.0, -3/4.0, -1.0},
			{0.0, -11/16.0, -11/16.0, -11/16.0, -1.0}});
		expRightBwd.add(new double[][]{{1.0, 5.0, 12.0, 24.0, -1.0}, {-1/16.0, -3/4.0, -3/4.0, -3/4.0, -1.0},
			{0.0, -11/16.0, -11/16.0, -11/16.0, -1.0}});
		// Chord 7
		expRightBwd.add(new double[][]{{10.0, 5.0, 2.0, 14.0, -1.0}, {-1/8.0, -1/8.0, -1/8.0, -1/8.0, -1.0}, 
			{0.0, 0.0, 0.0, 0.0, -1.0}});
		expRightBwd.add(new double[][]{{1.0, 4.0, 11.0, 23.0, -1.0}, {-1/8.0, -1/8.0, -1/8.0, -1/8.0, -1.0}, 
			{0.0, 0.0, 0.0, 0.0, -1.0}});
		// Chord 6
		expRightBwd.add(new double[][]{{23.0, 19.0, 14.0, 0.0, -1.0}, {-1/8.0, -1/4.0, -1/8.0, -1/4.0, -1.0}, 
			{0.0, -1/8.0, 0.0, -1/8.0, -1.0}});
		expRightBwd.add(new double[][]{{8.0, 4.0, 1.0, 15.0, -1.0}, {-1/8.0, -1/4.0, -1/8.0, -1/4.0, -1.0}, 
			{0.0, -1/8.0, 0.0, -1/8.0, -1.0}});
		expRightBwd.add(new double[][]{{4.0, 0.0, 5.0, 19.0, -1.0}, {-1/8.0, -1/4.0, -1/8.0, -1/4.0, -1.0}, 
			{0.0, -1/8.0, 0.0, -1/8.0, -1.0}});
		expRightBwd.add(new double[][]{{1.0, 5.0, 10.0, 24.0, -1.0}, {-1/8.0, -1/4.0, -1/8.0, -1/4.0, -1.0}, 
			{0.0, -1/8.0, 0.0, -1/8.0, -1.0}});
		// Chord 5
		expRightBwd.add(new double[][]{{19.0, 24.0, 15.0, 0.0, 0.0}, {-1/4.0, -1/4.0, -1/4.0, -1/2.0, -1/4.0}, 
			{0.0, 0.0, 0.0, -1/4.0, 0.0}});
		expRightBwd.add(new double[][]{{7.0, 12.0, 3.0, 12.0, 12.0}, {-1/4.0, -1/4.0, -1/4.0, -1/2.0, -1/4.0}, 
			{0.0, 0.0, 0.0, -1/4.0, 0.0}});
		expRightBwd.add(new double[][]{{7.0, 12.0, 3.0, 12.0, 12.0}, {-1/4.0, -1/4.0, -1/4.0, -1/2.0, -1/4.0}, 
			{0.0, 0.0, 0.0, -1/4.0, 0.0}});
		expRightBwd.add(new double[][]{{4.0, 9.0, 0.0, 15.0, 15.0}, {-1/4.0, -1/4.0, -1/4.0, -1/2.0, -1/4.0}, 
			{0.0, 0.0, 0.0, -1/4.0, 0.0}});
		expRightBwd.add(new double[][]{{5.0, 0.0, 9.0, 24.0, 24.0}, {-1/4.0, -1/4.0, -1/4.0, -1/2.0, -1/4.0}, 
			{0.0, 0.0, 0.0, -1/4.0, 0.0}});
		// Chord 4
		expRightBwd.add(new double[][]{{24.0, 15.0, 12.0, 12.0, 0.0}, {-1/8.0, -1/8.0, -1/8.0, -1/8.0, -1/8.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}});
		// Chord 3
		expRightBwd.add(new double[][]{{22.0, 13.0, 10.0, 10.0, 2.0}, {-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1/8.0}, 
			{-1/8.0, -1/8.0, -1/8.0, -1/8.0, 0.0}});
		expRightBwd.add(new double[][]{{19.0, 10.0, 7.0, 7.0, 5.0}, {-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1/8.0}, 
			{-1/8.0, -1/8.0, -1/8.0, -1/8.0, 0.0}});
		expRightBwd.add(new double[][]{{10.0, 1.0, 2.0, 2.0, 14.0}, {-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1/8.0}, 
			{-1/8.0, -1/8.0, -1/8.0, -1/8.0, 0.0}});
		expRightBwd.add(new double[][]{{4.0, 5.0, 8.0, 8.0, 20.0}, {-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1/8.0}, 
			{-1/8.0, -1/8.0, -1/8.0, -1/8.0, 0.0}});
		// Chord 2
		expRightBwd.add(new double[][]{{17.0, 17.0, 11.0, 2.0, 1.0}, {-1/16.0, -1/16.0, -1/16.0, -1/16.0, -1/16.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}});
		// Chord 1
		expRightBwd.add(new double[][]{{20.0, 20.0, 14.0, 3.0, 2.0}, {-1/4.0, -1/4.0, -1/4.0, -3/16.0, -1/4.0}, 
			{-1/16.0, -1/16.0, -1/16.0, 0.0, -1/16.0}});
		expRightBwd.add(new double[][]{{8.0, 8.0, 2.0, 9.0, 10.0}, {-1/4.0, -1/4.0, -1/4.0, -3/16.0, -1/4.0}, 
			{-1/16.0, -1/16.0, -1/16.0, 0.0, -1/16.0}});
		expRightBwd.add(new double[][]{{7.0, 7.0, 13.0, 24.0, 25.0}, {-1/4.0, -1/4.0, -1/4.0, -3/16.0, -1/4.0}, 
			{-1/16.0, -1/16.0, -1/16.0, 0.0, -1/16.0}});
		expRightBwd.add(new double[][]{{4.0, 4.0, 10.0, 21.0, 22.0}, {-1/4.0, -1/4.0, -1/4.0, -3/16.0, -1/4.0}, 
			{-1/16.0, -1/16.0, -1/16.0, 0.0, -1/16.0}});
		// Chord 0
		expRightBwd.add(new double[][]{{22.0, 19.0, 7.0, 5.0, 3.0}, {-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1/2.0}, 
			{0.0, 0.0, 0.0, 0.0, -1/4.0}});
		expRightBwd.add(new double[][]{{15.0, 12.0, 0.0, 12.0, 10.0}, {-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1/2.0}, 
			{0.0, 0.0, 0.0, 0.0, -1/4.0}});
		expRightBwd.add(new double[][]{{7.0, 4.0, 8.0, 20.0, 18.0}, {-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1/2.0}, 
			{0.0, 0.0, 0.0, 0.0, -1/4.0}});
		expRightBwd.add(new double[][]{{3.0, 0.0, 12.0, 24.0, 22.0}, {-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1/2.0}, 
			{0.0, 0.0, 0.0, 0.0, -1/4.0}});
		expected.addAll(expRightBwd);

		// Using the bi-directional model (where the full duration of the current note is known (= modelling duration))
		List<double[][]> expRightBiDir = new ArrayList<double[][]>();
		for (int i = 0; i < expRightBwd.size(); i++) {
			double[][] allProxCurrentNote = expRightBwd.get(i); 
			double[][] newAllProxCurrentNote = new double[3][5];
			for (int j = 0; j < allProxCurrentNote.length; j++) {
				newAllProxCurrentNote[j] = Arrays.copyOf(allProxCurrentNote[j], allProxCurrentNote[j].length);
			}
			expRightBiDir.add(newAllProxCurrentNote);
		}
		// Chord 15
		expRightBiDir.get(0)[2] = new double[]{-1.0, -1.0, -1.0, -1.0, -1.0};
		expRightBiDir.get(1)[2] = new double[]{-1.0, -1.0, -1.0, -1.0, -1.0};
		expRightBiDir.get(2)[2] = new double[]{-1.0, -1.0, -1.0, -1.0, -1.0};
		expRightBiDir.get(3)[2] = new double[]{-1.0, -1.0, -1.0, -1.0, -1.0};
		// Chords 14-9
		expRightBiDir.get(4)[2] = new double[]{-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1.0};
		expRightBiDir.get(5)[2] = new double[]{0.0, -1/2.0, -1/2.0, -1/2.0, -1.0};
		expRightBiDir.get(6)[2] = new double[]{0.0, -17/32.0, -17/32.0, -17/32.0, -1.0};
		expRightBiDir.get(7)[2] = new double[]{0.0, -9/16.0, -9/16.0, -9/16.0, -1.0};
		expRightBiDir.get(8)[2] = new double[]{0.0, -19/32.0, -19/32.0, -19/32.0, -1.0};
		expRightBiDir.get(9)[2] = new double[]{0.0, -5/8.0, -5/8.0, -5/8.0, -1.0};
		// Chord 8
		expRightBiDir.get(10)[2] = new double[]{7/16.0, -1/4.0, -1/4.0, -1/4.0, -1.0};
		expRightBiDir.get(11)[2] = new double[]{7/16.0, -1/4.0, -1/4.0, -1/4.0, -1.0};
		expRightBiDir.get(12)[2] = new double[]{7/16.0, -1/4.0, -1/4.0, -1/4.0, -1.0};
		expRightBiDir.get(13)[2] = new double[]{0.0, -11/16.0, -11/16.0, -11/16.0, -1.0};
		// Chord 7
		expRightBiDir.get(14)[2] = new double[]{0.0, 0.0, 0.0, 0.0, -1.0};
		expRightBiDir.get(15)[2] = new double[]{0.0, 0.0, 0.0, 0.0, -1.0};
		// Chord 6
		expRightBiDir.get(16)[2] = new double[]{1/8.0, 0.0, 1/8.0, 0.0, -1.0};
		expRightBiDir.get(17)[2] = new double[]{0.0, -1/8.0, 0.0, -1/8.0, -1.0};
		expRightBiDir.get(18)[2] = new double[]{0.0, -1/8.0, 0.0, -1/8.0, -1.0};
		expRightBiDir.get(19)[2] = new double[]{1/8.0, 0.0, 1/8.0, 0.0, -1.0};
		// Chord 5
		expRightBiDir.get(20)[2] = new double[]{0.0, 0.0, 0.0, -1/4.0, 0.0};
		expRightBiDir.get(21)[2] = new double[]{1/4.0, 1/4.0, 1/4.0, 0.0, 1/4.0};
		expRightBiDir.get(22)[2] = new double[]{0.0, 0.0, 0.0, -1/4.0, 0.0};
		expRightBiDir.get(23)[2] = new double[]{0.0, 0.0, 0.0, -1/4.0, 0.0};
		expRightBiDir.get(24)[2] = new double[]{0.0, 0.0, 0.0, -1/4.0, 0.0};
		// Chord 4
		expRightBiDir.get(25)[2] = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		// Chord 3
		expRightBiDir.get(26)[2] = new double[]{-1/8.0, -1/8.0, -1/8.0, -1/8.0, 0.0};
		expRightBiDir.get(27)[2] = new double[]{0.0, 0.0, 0.0, 0.0, 1/8.0};
		expRightBiDir.get(28)[2] = new double[]{0.0, 0.0, 0.0, 0.0, 1/8.0};
		expRightBiDir.get(29)[2] = new double[]{0.0, 0.0, 0.0, 0.0, 1/8.0}; // full duration of currentNote = 1/4 because in the NoteSequence, the CoDNote with the longest duration is retained
		// Chord 2
		expRightBiDir.get(30)[2] = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		// Chord 1
		expRightBiDir.get(31)[2] = new double[]{-1/16.0, -1/16.0, -1/16.0, 0.0, -1/16.0};
		expRightBiDir.get(32)[2] = new double[]{0.0, 0.0, 0.0, 1/16.0, 0.0};
		expRightBiDir.get(33)[2] = new double[]{0.0, 0.0, 0.0, 1/16.0, 0.0};
		expRightBiDir.get(34)[2] = new double[]{-1/8.0, -1/8.0, -1/8.0, -1/16.0, -1/8.0};
		// Chord 0
		expRightBiDir.get(35)[2] = new double[]{0.0, 0.0, 0.0, 0.0, -1/4.0};
		expRightBiDir.get(36)[2] = new double[]{0.0, 0.0, 0.0, 0.0, -1/4.0};
		expRightBiDir.get(37)[2] = new double[]{0.0, 0.0, 0.0, 0.0, -1/4.0};
		expRightBiDir.get(38)[2] = new double[]{0.0, 0.0, 0.0, 0.0, -1/4.0};
		expected.addAll(expRightBiDir);

		// d. Direction.LEFT (decisionContextSize = 3, averaged) 	
		List<double[][]> expectedAvg = new ArrayList<double[][]>();
		// Not modelling duration
		// Chord 0
		expectedAvg.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {0.0, 0.0, 0.0, 0.0, 0.0}}); 
		expectedAvg.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		expectedAvg.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},	
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		expectedAvg.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		// Chord 1
		expectedAvg.add(new double[][]{
			{1.0/(24+1), 1.0/(20+1), 1.0/(12+1), 1.0/(5+1), -1.0}, 
			{1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), -1.0}, 
			{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0}, 
			{-24.0, -20.0, -12.0, -5.0, 0.0}
		}); 
		expectedAvg.add(new double[][]{
			{1.0/(12+1), 1.0/(8+1), 1.0/(0+1), 1.0/(7+1), -1.0},
			{1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), -1.0},
			{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0},
			{-12.0, -8.0, 0.0, 7.0, 0.0}
		}); 
		expectedAvg.add(new double[][]{
			{1.0/(3+1), 1.0/(7+1), 1.0/(15+1), 1.0/(22+1), -1.0},
			{1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), -1.0},
			{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0},
			{3.0, 7.0, 15.0, 22.0, 0.0}
		}); 	
		expectedAvg.add(new double[][]{
			{1.0/(0+1), 1.0/(4+1), 1.0/(12+1), 1.0/(19+1), -1.0},
			{1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), -1.0},
			{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0},
			{0.0, 4.0, 12.0, 19.0, 0.0}
		});
		// Chord 5
		expectedAvg.add(new double[][]{
			{(1.0/(20+1) + 1.0/(27+1) + 1.0/(24+1)) / 3.0, // pitchProx 
			 (1.0/(20+1) + 1.0/(24+1) + 1.0/(20+1)) / 3.0, 
			 (1.0/(14+1) + 1.0/(12+1) + 1.0/(12+1)) / 3.0, 
			 (1.0/(5+1)  + 1.0/(3+1)  + 1.0/(0+1))  / 3.0, 
			 (1.0/(0+1) + 1.0/(2+1)) / 2.0},
			{(1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, // ioProx 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1))  / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((1/4.0)+1)) / 2.0},
			{(1.0/((1/8.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1)) / 3.0, // ooProx
			 (1.0/((1/8.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((1/4.0)+1) + 1.0/((5/16.0)+1))  / 3.0, 
			 (1.0/(0+1) + 1.0/((1/8.0)+1)) / 2.0},
			{(-20 + -27 + -24) / 3.0, // mvmts
			 (-20 + -24 + -20) / 3.0, 
			 (-14 + -12 + -12) / 3.0, 
			 (-5 + -3 + 0) / 3.0, 
			 -0.99999 /*(0 + -2) / 2.0*/}
		});
		expectedAvg.add(new double[][]{
			{(1.0/(8+1) + 1.0/(15+1) + 1.0/(12+1)) / 3.0, // pitchProx 
			 (1.0/(8+1) + 1.0/(12+1) + 1.0/(8+1)) / 3.0, 
			 (1.0/(2+1) + 1.0/(0+1) + 1.0/(0+1)) / 3.0, 
			 (1.0/(7+1)  + 1.0/(9+1)  + 1.0/(12+1))  / 3.0, 
			 (1.0/(12+1) + 1.0/(10+1)) / 2.0},
			{(1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, // ioProx 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1))  / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((1/4.0)+1)) / 2.0},
			{(1.0/((1/8.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1)) / 3.0, // ooProx
			 (1.0/((1/8.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((1/4.0)+1) + 1.0/((5/16.0)+1))  / 3.0, 
			 (1.0/(0+1) + 1.0/((1/8.0)+1)) / 2.0},
			{(-8 + -15 + -12) / 3.0, // mvmts
			 (-8 + -12 + -8) / 3.0, 
			 (-2 + -0 + -0) / 3.0, 
			 (7 + 9 + 12) / 3.0, 
			 (12 + 10) / 2.0}
		});
		expectedAvg.add(new double[][]{
			{(1.0/(8+1) + 1.0/(15+1) + 1.0/(12+1)) / 3.0, // pitchProx 
			 (1.0/(8+1) + 1.0/(12+1) + 1.0/(8+1)) / 3.0, 
			 (1.0/(2+1) + 1.0/(0+1) + 1.0/(0+1)) / 3.0, 
			 (1.0/(7+1)  + 1.0/(9+1)  + 1.0/(12+1))  / 3.0, 
			 (1.0/(12+1) + 1.0/(10+1)) / 2.0},
			{(1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, // ioProx 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1))  / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((1/4.0)+1)) / 2.0},
			{(1.0/((1/8.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1)) / 3.0, // ooProx
			 (1.0/((1/8.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((1/4.0)+1) + 1.0/((5/16.0)+1))  / 3.0, 
			 (1.0/(0+1) + 1.0/((1/8.0)+1)) / 2.0},
			{(-8 + -15 + -12) / 3.0, // mvmts
			 (-8 + -12 + -8) / 3.0, 
			 (-2 + -0 + -0) / 3.0, 
			 (7 + 9 + 12) / 3.0, 
			 (12 + 10) / 2.0}
		});
		expectedAvg.add(new double[][]{
			{(1.0/(5+1) + 1.0/(12+1) + 1.0/(9+1)) / 3.0, // pitchProx 
			 (1.0/(5+1) + 1.0/(9+1) + 1.0/(5+1)) / 3.0, 
			 (1.0/(1+1) + 1.0/(3+1) + 1.0/(3+1)) / 3.0, 
			 (1.0/(10+1) + 1.0/(12+1)  + 1.0/(15+1))  / 3.0, 
			 (1.0/(15+1) + 1.0/(13+1)) / 2.0},
			{(1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, // ioProx 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1))  / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((1/4.0)+1)) / 2.0},
			{(1.0/((1/8.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1)) / 3.0, // ooProx
			 (1.0/((1/8.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((1/4.0)+1) + 1.0/((5/16.0)+1))  / 3.0, 
			 (1.0/(0+1) + 1.0/((1/8.0)+1)) / 2.0},
			{(-5 + -12 + -9) / 3.0, // mvmts
			 (-5 + -9 + -5) / 3.0, 
			 (1 + 3 + 3) / 3.0, 
			 (10 + 12 + 15) / 3.0, 
			 (15 + 13) / 2.0}
		});
		expectedAvg.add(new double[][]{
			{(1.0/(4+1) + 1.0/(3+1) + 1.0/(0+1)) / 3.0, // pitchProx 
			 (1.0/(4+1) + 1.0/(0+1) + 1.0/(4+1)) / 3.0, 
			 (1.0/(10+1) + 1.0/(12+1) + 1.0/(12+1)) / 3.0, 
			 (1.0/(19+1) + 1.0/(21+1)  + 1.0/(24+1))  / 3.0, 
			 (1.0/(24+1) + 1.0/(22+1)) / 2.0},
			{(1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, // ioProx 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1))  / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((1/4.0)+1)) / 2.0},
			{(1.0/((1/8.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1)) / 3.0, // ooProx
			 (1.0/((1/8.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((1/4.0)+1) + 1.0/((5/16.0)+1))  / 3.0, 
			 (1.0/(0+1) + 1.0/((1/8.0)+1)) / 2.0},
			{(4 + -3 + 0) / 3.0, // mvmts
			 (4 + 0 + 4) / 3.0, 
			 (10 + 12 + 12) / 3.0, 
			 (19 + 21 + 24) / 3.0, 
			 (24 + 22) / 2.0}
		});
		expected.addAll(expectedAvg);

		// For all non-averaged elements of expected: turn the elements in all Arrays from distances into proximities
		for (int i = 0; i < (expected.size() - expectedAvg.size()); i++) {
			double[][] currentExpected = expected.get(i);
			for (int j = 0; j < currentExpected.length; j++) {
				// Do only if not movements 
				if (j != 3) {
					double[] currentArray = currentExpected[j];
					for (int k = 0; k < currentArray.length; k++) {
						double oldValue = currentArray[k];
						// Do only if oldValue is not -1.0, i.e., if the voice is active
						if (oldValue != -1.0) {
							double newValue = 1.0/(oldValue + 1);
							// If oldValue is negative
							if (oldValue < 0) {
								newValue = -(1.0/(-oldValue + 1));
							}
							currentArray[k] = newValue;
						}
					}
				}
			}
		}

		List<double[][]> actual = new ArrayList<double[][]>();
		Integer[][] btp = tablature.getBasicTabSymbolProperties();
		NoteSequence noteSeq = transcription.getNoteSequence();
		// a. Direction.LEFT (decisionContextSize = 1)
		// Not modelling duration
		for (int i = 0; i < btp.length; i++) {
			actual.addAll(FeatureGenerator.getPitchAndTimeProximitiesToAllVoices(btp, 
				transcription, noteSeq.get(i), Direction.LEFT, false, false, 1, false));
		}
		// Modelling duration
		for (int i = 0; i < btp.length; i++) {
			actual.addAll(FeatureGenerator.getPitchAndTimeProximitiesToAllVoices(btp, 
				transcription, noteSeq.get(i), Direction.LEFT, true, false, 1, false));
		}
		// b. Direction.LEFT (decisionContextSize = 3)
		// Not modelling duration
		for (int i = 0; i < btp.length; i++) {
			actual.addAll(FeatureGenerator.getPitchAndTimeProximitiesToAllVoices(btp, 
				transcription, noteSeq.get(i), Direction.LEFT, false, false, 3, false));
		}
		// c. Direction.RIGHT (decisionContextSize = 1)
		List<Integer> backwardsMapping = 
			FeatureGenerator.getBackwardsMapping(tablature.getNumberOfNotesPerChord());
		// Using the bwd model
		for (int i : backwardsMapping) {
			actual.addAll(FeatureGenerator.getPitchAndTimeProximitiesToAllVoices(btp, 
				transcription, noteSeq.get(i), Direction.RIGHT, false, false, 1, false));
		}
		// Using the bi-directional model
		for (int i : backwardsMapping) {
			actual.addAll(FeatureGenerator.getPitchAndTimeProximitiesToAllVoices(btp, 
				transcription, noteSeq.get(i), Direction.RIGHT, true, true, 1, false));
		}
		// d. Direction.LEFT (decisionContextSize = 3, averaged)		
		// Not modelling duration
		for (int i : Arrays.asList(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 14, 15, 16, 17, 18})) {
			actual.addAll(FeatureGenerator.getPitchAndTimeProximitiesToAllVoices(btp, 
				transcription, noteSeq.get(i), Direction.LEFT, false, false, 3, true));
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) { // i = noteIndex
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) { // j = one of the three proximities Arrays
				assertEquals(expected.get(i)[j].length, actual.get(i)[j].length);
				for (int k = 0; k < expected.get(i)[j].length; k++) { // k = the value of element k in the current proximities Array
					assertEquals(expected.get(i)[j][k], actual.get(i)[j][k]);
				}
			}
		}
	}


	public void testGetPitchAndTimeProximitiesToAllVoicesNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		List<double[][]> expected = new ArrayList<double[][]>();
		// a. Direction.LEFT (decisionContextSize = 1)
		// Chord 0
		expected.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},
			{-1.0, -1.0, -1.0, -1.0, -1.0}});
		expected.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},
			{-1.0, -1.0, -1.0, -1.0, -1.0}});
		expected.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}});
		expected.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},
			{-1.0, -1.0, -1.0, -1.0, -1.0}});
		// Chord 1
		expected.add(new double[][]{{24.0, 20.0, 12.0, 5.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
			{0.0, 0.0, 0.0, 0.0, -1.0}});
		expected.add(new double[][]{{12.0, 8.0, 0.0, 7.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
			{0.0, 0.0, 0.0, 0.0, -1.0}});
		expected.add(new double[][]{{0.0, 4.0, 12.0, 19.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
			{0.0, 0.0, 0.0, 0.0, -1.0}});
		expected.add(new double[][]{{3.0, 7.0, 15.0, 22.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
			{0.0, 0.0, 0.0, 0.0, -1.0}});
		// Chord 2
		expected.add(new double[][]{{24.0, 21.0, 9.0, 3.0, -1.0}, {3/16.0, 3/16.0, 3/16.0, 3/16.0, -1.0}, 
			{-1/16.0, 1/16.0, -1/16.0, 0.0, -1.0}});
		// Chord 3
		expected.add(new double[][]{{25.0, 22.0, 10.0, 1.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
			{0.0, 1/8.0, 0.0, 0.0, -1.0}});
		expected.add(new double[][]{{22.0, 19.0, 7.0, 2.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0},
			{0.0, 1/8.0, 0.0, 0.0, -1.0}});
		expected.add(new double[][]{{13.0, 10.0, 2.0, 11.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
			{0.0, 1/8.0, 0.0, 0.0, -1.0}});
		expected.add(new double[][]{{7.0, 4.0, 8.0, 17.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
			{0.0, 1/8.0, 0.0, 0.0, -1.0}});
		expected.add(new double[][]{{7.0, 4.0, 8.0, 17.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
			{0.0, 1/8.0, 0.0, 0.0, -1.0}});
		// Chord 4
		expected.add(new double[][]{{20.0, 20.0, 14.0, 5.0, 2.0}, {1/8.0, 1/8.0, 1/8.0, 1/8.0, 1/8.0}, 
			{-1/8.0, 0.0, -1/8.0, -1/8.0, 0.0}});
		// Chord 5
		expected.add(new double[][]{{20.0, 20.0, 14.0, 5.0, 0.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0},
			{0.0, 1/8.0, 0.0, 0.0, 0.0}});
		expected.add(new double[][]{{8.0, 8.0, 2.0, 7.0, 12.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0},
			{0.0, 1/8.0, 0.0, 0.0, 0.0}});
		expected.add(new double[][]{{8.0, 8.0, 2.0, 7.0, 12.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0},
			{0.0, 1/8.0, 0.0, 0.0, 0.0}});
		expected.add(new double[][]{{5.0, 5.0, 1.0, 10.0, 15.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
			{0.0, 1/8.0, 0.0, 0.0, 0.0}});
		expected.add(new double[][]{{4.0, 4.0, 10.0, 19.0, 24.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
			{0.0, 1/8.0, 0.0, 0.0, 0.0}});
		// Chord 6
		expected.add(new double[][]{{24.0, 15.0, 12.0, 12.0, 0.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0},
			{0.0, 0.0, 0.0, -1/4.0, 0.0}});
		expected.add(new double[][]{{9.0, 0.0, 3.0, 3.0, 15.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0},
			{0.0, 0.0, 0.0, -1/4.0, 0.0}});
		expected.add(new double[][]{{5.0, 4.0, 7.0, 7.0, 19.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
			{0.0, 0.0, 0.0, -1/4.0, 0.0}});
		expected.add(new double[][]{{0.0, 9.0, 12.0, 12.0, 24.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
			{0.0, 0.0, 0.0, -1/4.0, 0.0}});
		// Chord 7
		expected.add(new double[][]{{5.0, 10.0, 1.0, 2.0, 14.0}, {1/8.0, 1/8.0, 1/8.0, 3/8.0, 1/8.0}, 
			{0.0, -1/8.0, 0.0, -1/8.0, -1/8.0}});
		expected.add(new double[][]{{4.0, 1.0, 8.0, 11.0, 23.0}, {1/8.0, 1/8.0, 1/8.0, 3/8.0, 1/8.0}, 
			{0.0, -1/8.0, 0.0, -1/8.0, -1/8.0}});
		// Chord 8
		expected.add(new double[][]{{23.0, 24.0, 14.0, 12.0, 0.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}});
		expected.add(new double[][]{{11.0, 12.0, 2.0, 0.0, 12.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0},
			{0.0, 0.0, 0.0, 0.0, 0.0}});
		expected.add(new double[][]{{4.0, 5.0, 5.0, 7.0, 19.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}});
		expected.add(new double[][]{{1.0, 0.0, 10.0, 12.0, 24.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0},
			{0.0, 0.0, 0.0, 0.0, 0.0}});
		// Chords 9-14
		expected.add(new double[][]{{1.0, 4.0, 11.0, 23.0, 23.0}, {1/16.0, 1/16.0, 1/16.0, 1/16.0, 5/16.0}, 
			{0.0, -7/16.0, -7/16.0, -7/16.0, 1/16.0}});
		expected.add(new double[][]{{1.0, 5.0, 12.0, 24.0, 24.0}, {1/16.0, 1/8.0, 1/8.0, 1/8.0, 3/8.0},
			{0.0, -3/8.0, -3/8.0, -3/8.0, 1/8.0}});
		expected.add(new double[][]{{1.0, 4.0, 11.0, 23.0, 23.0}, {1/32.0, 5/32.0, 5/32.0, 5/32.0, 13/32.0}, 
			{0.0, -11/32.0, -11/32.0, -11/32.0, 5/32.0}});
		expected.add(new double[][]{{2.0, 2.0, 9.0, 21.0, 21.0}, {1/32.0, 3/16.0, 3/16.0, 3/16.0, 7/16.0}, 
			{0.0, -5/16.0, -5/16.0, -5/16.0, 3/16.0}});
		expected.add(new double[][]{{2.0, 4.0, 11.0, 23.0, 23.0}, {1/32.0, 7/32.0, 7/32.0, 7/32.0, 15/32.0}, 
			{0.0, -9/32.0, -9/32.0, -9/32.0, 7/32.0}});
		expected.add(new double[][]{{1.0, 5.0, 12.0, 24.0, 24.0}, {1/32.0, 1/4.0, 1/4.0, 1/4.0, 1/2.0}, 
			{0.0, -1/4.0, -1/4.0, -1/4.0, 1/4.0}});
		// Chord 15
		expected.add(new double[][]{{24.0, 19.0, 12.0, 0.0, 0.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 1.0}, 
			{1/4.0, 1/4.0, 1/4.0, 1/4.0, 3/4.0}});
		expected.add(new double[][]{{12.0, 7.0, 0.0, 12.0, 12.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 1.0}, 
			{1/4.0, 1/4.0, 1/4.0, 1/4.0, 3/4.0}});
		expected.add(new double[][]{{5.0, 0.0, 7.0, 19.0, 19.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 1.0},
			{1/4.0, 1/4.0, 1/4.0, 1/4.0, 3/4.0}});
		expected.add(new double[][]{{0.0, 5.0, 12.0, 24.0, 24.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 1.0}, 
			{1/4.0, 1/4.0, 1/4.0, 1/4.0, 3/4.0}});

		// b. Direction.LEFT (decisionContextSize = 3)
		List<double[][]> expLeftThree = new ArrayList<double[][]>();
		// Chord 0
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		// Chord 1
		expLeftThree.add(new double[][]{{24.0, 20.0, 12.0, 5.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
			{0.0, 0.0, 0.0, 0.0, -1.0}, {0.0, 0.0, 0.0, 0.0, -1.0}}); 
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{12.0, 8.0, 0.0, 7.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0},
			{0.0, 0.0, 0.0, 0.0, -1.0}, {0.0, 0.0, 0.0, 1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{0.0, 4.0, 12.0, 19.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
			{0.0, 0.0, 0.0, 0.0, -1.0}, {0.0, 1.0, 1.0, 1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{3.0, 7.0, 15.0, 22.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}, 
			{0.0, 0.0, 0.0, 0.0, -1.0}, {1.0, 1.0, 1.0, 1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		// Chord 2
		expLeftThree.add(new double[][]{{24.0, 21.0, 9.0, 3.0, -1.0}, {3/16.0, 3/16.0, 3/16.0, 3/16.0, -1.0},	
			{-1/16.0, 1/16.0, -1/16.0, 0.0, -1.0}, {0.0, 0.0, 0.0, 1.0, -1.0}});
		expLeftThree.add(new double[][]{{21.0, 17.0, 9.0, 2.0, -1.0}, {7/16.0, 7/16.0, 7/16.0, 7/16.0, -1.0}, 
			{3/16.0, 3/16.0, 3/16.0, 3/16.0, -1.0}, {0.0, 0.0, 0.0, 0.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}});
		// Chord 3
		expLeftThree.add(new double[][]{{25.0, 22.0, 10.0, 1.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
			{0.0, 1/8.0, 0.0, 0.0, -1.0}, {0.0, 0.0, 0.0, 0.0, -1.0}}); 
		expLeftThree.add(new double[][]{{22.0, 18.0, 10.0, 2.0, -1.0}, {2/4.0, 2/4.0, 2/4.0, 1/4.0, -1.0}, 
			{1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, {0.0, 0.0, 0.0, 1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, 3.0, -1.0}, {-1.0, -1.0, -1.0, 2/4.0, -1.0}, 
			{-1.0, -1.0, -1.0, 1/4.0, -1.0}, {-1.0, -1.0, -1.0, 0.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{22.0, 19.0, 7.0, 2.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
			{0.0, 1/8.0, 0.0, 0.0, -1.0}, {0.0, 0.0, 0.0, 1.0, -1.0}}); 
		expLeftThree.add(new double[][]{{19.0, 15.0, 7.0, 5.0, -1.0}, {2/4.0, 2/4.0, 2/4.0, 1/4.0, -1.0}, 
			{1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, {0.0, 0.0, 0.0, 1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, 0.0, -1.0}, {-1.0, -1.0, -1.0, 2/4.0, -1.0}, 
			{-1.0, -1.0, -1.0, 1/4.0, -1.0}, {-1.0, -1.0, -1.0, 0.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{13.0, 10.0, 2.0, 11.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0},
			{0.0, 1/8.0, 0.0, 0.0, -1.0}, {0.0, 0.0, 1.0, 1.0, -1.0}}); 
		expLeftThree.add(new double[][]{{10.0, 6.0, 2.0, 14.0, -1.0}, {2/4.0, 2/4.0, 2/4.0, 1/4.0, -1.0}, 
			{1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, {0.0, 0.0, 1.0, 1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, 9.0, -1.0}, {-1.0, -1.0, -1.0, 2/4.0, -1.0}, 
			{-1.0, -1.0, -1.0, 1/4.0, -1.0}, {-1.0, -1.0, -1.0, 1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{7.0, 4.0, 8.0, 17.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
			{0.0, 1/8.0, 0.0, 0.0, -1.0}, {0.0, 0.0, 1.0, 1.0, -1.0}}); 
		expLeftThree.add(new double[][]{{4.0, 0.0, 8.0, 20.0, -1.0}, {2/4.0, 2/4.0, 2/4.0, 1/4.0, -1.0}, 
			{1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, {0.0, 0.0, 1.0, 1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, 15.0, -1.0}, {-1.0, -1.0, -1.0, 2/4.0, -1.0}, 
			{-1.0, -1.0, -1.0, 1/4.0, -1.0}, {-1.0, -1.0, -1.0, 1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{7.0, 4.0, 8.0, 17.0, -1.0}, {1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, 
			{0.0, 1/8.0, 0.0, 0.0, -1.0}, {0.0, 0.0, 1.0, 1.0, -1.0}}); 
		expLeftThree.add(new double[][]{{4.0, 0.0, 8.0, 20.0, -1.0}, {2/4.0, 2/4.0, 2/4.0, 1/4.0, -1.0}, 
			{1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}, {0.0, 0.0, 1.0, 1.0, -1.0}});
		expLeftThree.add(new double[][]{{-1.0, -1.0, -1.0, 15.0, -1.0}, {-1.0, -1.0, -1.0, 2/4.0, -1.0}, 
			{-1.0, -1.0, -1.0, 1/4.0, -1.0}, {-1.0, -1.0, -1.0, 1.0, -1.0}});
		// Chord 4
		expLeftThree.add(new double[][]{{20.0, 20.0, 14.0, 5.0, 2.0}, {1/8.0, 1/8.0, 1/8.0, 1/8.0, 1/8.0}, 
			{-1/8.0, 0.0, -1/8.0, -1/8.0, 0.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});  
		expLeftThree.add(new double[][]{{27.0, 24.0, 12.0, 3.0, -1.0}, {3/8.0, 3/8.0, 3/8.0, 3/16.0, -1.0}, 
			{1/8.0, 1/4.0, 1/8.0, 1/8.0, -1.0}, {0.0, 0.0, 0.0, 0.0, -1.0}});
		expLeftThree.add(new double[][]{{24.0, 20.0, 12.0, 0.0, -1.0}, {5/8.0, 5/8.0, 5/8.0, 3/8.0, -1.0}, 
			{3/8.0, 3/8.0, 3/8.0, 3/16.0, -1.0}, {0.0, 0.0, 0.0, 0.0, -1.0}});
		// Chord 5
		expLeftThree.add(new double[][]{{20.0, 20.0, 14.0, 5.0, 0.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
			{0.0, 1/8.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 0.0, 0.0, 0.0}}); 
		expLeftThree.add(new double[][]{{27.0, 24.0, 12.0, 3.0, 2.0}, {2/4.0, 2/4.0, 2/4.0, 5/16.0, 1/4.0}, 
			{1/4.0, 3/8.0, 1/4.0, 1/4.0, 1/8.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		expLeftThree.add(new double[][]{{24.0, 20.0, 12.0, 0.0, -1.0}, {3/4.0, 3/4.0, 3/4.0, 2/4.0, -1.0}, 
			{2/4.0, 2/4.0, 2/4.0, 5/16.0, -1.0}, {0.0, 0.0, 0.0, 0.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{8.0, 8.0, 2.0, 7.0, 12.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
			{0.0, 1/8.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 0.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{15.0, 12.0, 0.0, 9.0, 10.0}, {2/4.0, 2/4.0, 2/4.0, 5/16.0, 1/4.0}, 
			{1/4.0, 3/8.0, 1/4.0, 1/4.0, 1/8.0}, {0.0, 0.0, 0.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{12.0, 8.0, 0.0, 12.0, -1.0}, {3/4.0, 3/4.0, 3/4.0, 2/4.0, -1.0}, 
			{2/4.0, 2/4.0, 2/4.0, 5/16.0, -1.0}, {0.0, 0.0, 0.0, 1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{8.0, 8.0, 2.0, 7.0, 12.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0},
			{0.0, 1/8.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 0.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{15.0, 12.0, 0.0, 9.0, 10.0}, {2/4.0, 2/4.0, 2/4.0, 5/16.0, 1/4.0}, 
			{1/4.0, 3/8.0, 1/4.0, 1/4.0, 1/8.0}, {0.0, 0.0, 0.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{12.0, 8.0, 0.0, 12.0, -1.0}, {3/4.0, 3/4.0, 3/4.0, 2/4.0, -1.0}, 
			{2/4.0, 2/4.0, 2/4.0, 5/16.0, -1.0}, {0.0, 0.0, 0.0, 1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{5.0, 5.0, 1.0, 10.0, 15.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
			{0.0, 1/8.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 1.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{12.0, 9.0, 3.0, 12.0, 13.0}, {2/4.0, 2/4.0, 2/4.0, 5/16.0, 1/4.0}, 
			{1/4.0, 3/8.0, 1/4.0, 1/4.0, 1/8.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{9.0, 5.0, 3.0, 15.0, -1.0}, {3/4.0, 3/4.0, 3/4.0, 2/4.0, -1.0}, 
			{2/4.0, 2/4.0, 2/4.0, 5/16.0, -1.0}, {0.0, 0.0, 1.0, 1.0, -1.0}});
		//
		expLeftThree.add(new double[][]{{4.0, 4.0, 10.0, 19.0, 24.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0}, 
			{0.0, 1/8.0, 0.0, 0.0, 0.0}, {1.0, 1.0, 1.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{3.0, 0.0, 12.0, 21.0, 22.0}, {2/4.0, 2/4.0, 2/4.0, 5/16.0, 1/4.0}, 
			{1/4.0, 3/8.0, 1/4.0, 1/4.0, 1/8.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{0.0, 4.0, 12.0, 24.0, -1.0}, {3/4.0, 3/4.0, 3/4.0, 2/4.0, -1.0}, 
			{2/4.0, 2/4.0, 2/4.0, 5/16.0, -1.0}, {0.0, 1.0, 1.0, 1.0, -1.0}}); 
		// Chord 6
		expLeftThree.add(new double[][]{{24.0, 15.0, 12.0, 12.0, 0.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
			{0.0, 0.0, 0.0, -1/4.0, 0.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		expLeftThree.add(new double[][]{{20.0, 20.0, 14.0, 5.0, 0.0}, {2/4.0, 2/4.0, 2/4.0, 2/4.0, 3/8.0}, 
			{1/4.0, 3/8.0, 1/4.0, 1/4.0, 1/4.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		expLeftThree.add(new double[][]{{27.0, 24.0, 12.0, 3.0, 2.0}, {3/4.0, 3/4.0, 3/4.0, 9/16.0, 2/4.0}, 
			{2/4.0, 5/8.0, 2/4.0, 2/4.0, 3/8.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		//
		expLeftThree.add(new double[][]{{9.0, 0.0, 3.0, 3.0, 15.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
			{0.0, 0.0, 0.0, -1/4.0, 0.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{5.0, 5.0, 1.0, 10.0, 15.0}, {2/4.0, 2/4.0, 2/4.0, 2/4.0, 3/8.0}, 
			{1/4.0, 3/8.0, 1/4.0, 1/4.0, 1/4.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{12.0, 9.0, 3.0, 12.0, 13.0}, {3/4.0, 3/4.0, 3/4.0, 9/16.0, 2/4.0}, 
			{2/4.0, 5/8.0, 2/4.0, 2/4.0, 3/8.0}, {0.0, 0.0, 1.0, 1.0, 1.0}}); 
		//
		expLeftThree.add(new double[][]{{5.0, 4.0, 7.0, 7.0, 19.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
			{0.0, 0.0, 0.0, -1/4.0, 0.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{1.0, 1.0, 5.0, 14.0, 19.0}, {2/4.0, 2/4.0, 2/4.0, 2/4.0, 3/8.0}, 
			{1/4.0, 3/8.0, 1/4.0, 1/4.0, 1/4.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{8.0, 5.0, 7.0, 16.0, 17.0}, {3/4.0, 3/4.0, 3/4.0, 9/16.0, 2/4.0}, 
			{2/4.0, 5/8.0, 2/4.0, 2/4.0, 3/8.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{0.0, 9.0, 12.0, 12.0, 24.0}, {1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}, 
			{0.0, 0.0, 0.0, -1/4.0, 0.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{4.0, 4.0, 10.0, 19.0, 24.0}, {2/4.0, 2/4.0, 2/4.0, 2/4.0, 3/8.0}, 
			{1/4.0, 3/8.0, 1/4.0, 1/4.0, 1/4.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{3.0, 0.0, 12.0, 21.0, 22.0}, {3/4.0, 3/4.0, 3/4.0, 9/16.0, 2/4.0}, 
			{2/4.0, 5/8.0, 2/4.0, 2/4.0, 3/8.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		// Chord 7
		expLeftThree.add(new double[][]{{5.0, 10.0, 1.0, 2.0, 14.0}, {1/8.0, 1/8.0, 1/8.0, 3/8.0, 1/8.0}, 
			{0.0, -1/8.0, 0.0, -1/8.0, -1/8.0}, {0.0, 0.0, 0.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{10.0, 1.0, 2.0, 9.0, 14.0}, {3/8.0, 3/8.0, 3/8.0, 5/8.0, 3/8.0}, 
			{1/8.0, 1/8.0, 1/8.0, 3/8.0, 1/8.0}, {0.0, 0.0, 1.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{6.0, 6.0, 0.0, 11.0, 14.0}, {5/8.0, 5/8.0, 5/8.0, 11/16.0, 2/4.0}, 
			{3/8.0, 2/4.0, 3/8.0, 5/8.0, 3/8.0}, {0.0, 0.0, 0.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{4.0, 1.0, 8.0, 11.0, 23.0}, {1/8.0, 1/8.0, 1/8.0, 3/8.0, 1/8.0}, 
			{0.0, -1/8.0, 0.0, -1/8.0, -1/8.0}, {1.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{1.0, 8.0, 11.0, 18.0, 23.0}, {3/8.0, 3/8.0, 3/8.0, 5/8.0, 3/8.0}, 
			{1/8.0, 1/8.0, 1/8.0, 3/8.0, 1/8.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{3.0, 3.0, 9.0, 20.0, 23.0}, {5/8.0, 5/8.0, 5/8.0, 11/16.0, 2/4.0}, 
			{3/8.0, 2/4.0, 3/8.0, 5/8.0, 3/8.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		// Chord 8
		expLeftThree.add(new double[][]{{23.0, 24.0, 14.0, 12.0, 0.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0},
			{0.0, 0.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});  
		expLeftThree.add(new double[][]{{19.0, 15.0, 15.0, 5.0, 0.0}, {1/4.0, 2/4.0, 1/4.0, 3/4.0, 2/4.0}, 
			{1/8.0, 1/4.0, 1/8.0, 2/4.0, 1/4.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		expLeftThree.add(new double[][]{{24.0, 20.0, 12.0, 3.0, 0.0}, {2/4.0, 3/4.0, 2/4.0, 13/16.0, 5/8.0}, 
			{1/4.0, 5/8.0, 1/4.0, 3/4.0, 2/4.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		//
		expLeftThree.add(new double[][]{{11.0, 12.0, 2.0, 0.0, 12.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0},
			{0.0, 0.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 0.0, 0.0, 1.0}});
		expLeftThree.add(new double[][]{{7.0, 3.0, 3.0, 7.0, 12.0}, {1/4.0, 2/4.0, 1/4.0, 3/4.0, 2/4.0}, 
			{1/8.0, 1/4.0, 1/8.0, 2/4.0, 1/4.0}, {0.0, 0.0, 0.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{12.0, 8.0, 0.0, 9.0, 12.0}, {2/4.0, 3/4.0, 2/4.0, 13/16.0, 5/8.0}, 
			{1/4.0, 5/8.0, 1/4.0, 3/4.0, 2/4.0}, {0.0, 0.0, 0.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{4.0, 5.0, 5.0, 7.0, 19.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{0.0, 4.0, 4.0, 14.0, 19.0}, {1/4.0, 2/4.0, 1/4.0, 3/4.0, 2/4.0}, 
			{1/8.0, 1/4.0, 1/8.0, 2/4.0, 1/4.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{5.0, 1.0, 7.0, 16.0, 19.0}, {2/4.0, 3/4.0, 2/4.0, 13/16.0, 5/8.0}, 
			{1/4.0, 5/8.0, 1/4.0, 3/4.0, 2/4.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{1.0, 0.0, 10.0, 12.0, 24.0}, {1/8.0, 1/4.0, 1/8.0, 1/2.0, 1/4.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}, {1.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{5.0, 9.0, 9.0, 19.0, 24.0}, {1/4.0, 2/4.0, 1/4.0, 3/4.0, 2/4.0}, 
			{1/8.0, 1/4.0, 1/8.0, 2/4.0, 1/4.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{0.0, 4.0, 12.0, 21.0, 24.0}, {2/4.0, 3/4.0, 2/4.0, 13/16.0, 5/8.0}, 
			{1/4.0, 5/8.0, 1/4.0, 3/4.0, 2/4.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		// Chords 9-14
		expLeftThree.add(new double[][]{{1.0, 4.0, 11.0, 23.0, 23.0}, {1/16.0, 1/16.0, 1/16.0, 1/16.0, 5/16.0},	
			{0.0, -7/16.0, -7/16.0, -7/16.0, 1/16.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{0.0, 1.0, 9.0, 11.0, 23.0}, {3/16.0, 5/16.0, 3/16.0, 9/16.0, 9/16.0}, 
			{1/16.0, 1/16.0, 1/16.0, 1/16.0, 5/16.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{4.0, 8.0, 8.0, 18.0, 23.0}, {5/16.0, 9/16.0, 5/16.0, 13/16.0, 11/16.0}, 
			{3/16.0, 5/16.0, 3/16.0, 9/16.0, 9/16.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{1.0, 5.0, 12.0, 24.0, 24.0}, {1/16.0, 1/8.0, 1/8.0, 1/8.0, 3/8.0},	
			{0.0, -3/8.0, -3/8.0, -3/8.0, 1/8.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{0.0, 0.0, 10.0, 12.0, 24.0}, {1/8.0, 3/8.0, 2/8.0, 5/8.0, 5/8.0}, 
			{1/16.0, 1/8.0, 1/8.0, 1/8.0, 3/8.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{1.0, 9.0, 9.0, 19.0, 24.0}, {2/8.0, 5/8.0, 3/8.0, 7/8.0, 6/8.0}, 
			{1/8.0, 3/8.0, 2/8.0, 5/8.0, 5/8.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{1.0, 4.0, 11.0, 23.0, 23.0}, {1/32.0, 5/32.0, 5/32.0, 5/32.0, 13/32.0},
			{0.0, -11/32.0, -11/32.0, -11/32.0, 5/32.0}, {0.0, 1.0, 1.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{0.0, 1.0, 9.0, 11.0, 23.0}, {3/32.0, 13/32.0, 9/32.0, 21/32.0, 21/32.0}, 
			{1/32.0, 5/32.0, 5/32.0, 5/32.0, 13/32.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{1.0, 8.0, 8.0, 18.0, 23.0}, {5/32.0, 21/32.0, 13/32.0, 29/32.0, 25/32.0}, 
			{3/32.0, 13/32.0, 9/32.0, 21/32.0, 21/32.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{2.0, 2.0, 9.0, 21.0, 21.0}, {1/32.0, 3/16.0, 3/16.0, 3/16.0, 7/16.0}, 
			{0.0, -5/16.0, -5/16.0, -5/16.0, 3/16.0}, {0.0, 1.0, 1.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{3.0, 3.0, 7.0, 9.0, 21.0}, {1/16.0, 7/16.0, 5/16.0, 11/16.0, 11/16.0}, 
			{1/32.0, 3/16.0, 3/16.0, 3/16.0, 7/16.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{2.0, 6.0, 6.0, 16.0, 21.0}, {2/16.0, 11/16.0, 7/16.0, 15/16.0, 13/16.0}, 
			{1/16.0, 7/16.0, 5/16.0, 11/16.0, 11/16.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{2.0, 4.0, 11.0, 23.0, 23.0}, {1/32.0, 7/32.0, 7/32.0, 7/32.0, 15/32.0},
			{0.0, -9/32.0, -9/32.0, -9/32.0, 7/32.0}, {1.0, 1.0, 1.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{0.0, 1.0, 9.0, 11.0, 23.0}, {2/32.0, 15/32.0, 11/32.0, 23/32.0, 23/32.0}, 
			{1/32.0, 7/32.0, 7/32.0, 7/32.0, 15/32.0}, {0.0, 0.0, 1.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{1.0, 8.0, 8.0, 18.0, 23.0}, {3/32.0, 23/32.0, 15/32.0, 31/32.0, 27/32.0}, 
			{2/32.0, 15/32.0, 11/32.0, 23/32.0, 23/32.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{1.0, 5.0, 12.0, 24.0, 24.0}, {1/32.0, 1/4.0, 1/4.0, 1/4.0, 2/4.0}, 
			{0.0, -1/4.0, -1/4.0, -1/4.0, 1/4.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{3.0, 0.0, 10.0, 12.0, 24.0}, {1/16.0, 2/4.0, 3/8.0, 3/4.0, 3/4.0}, 
			{1/32.0, 1/4.0, 1/4.0, 1/4.0, 2/4.0}, {1.0, 0.0, 1.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{1.0, 9.0, 9.0, 19.0, 24.0}, {3/32.0, 3/4.0, 2/4.0, 4/4.0, 7/8.0}, 
			{2/32.0, 2/4.0, 3/8.0, 3/4.0, 3/4.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		// Chord 15
		expLeftThree.add(new double[][]{{24.0, 19.0, 12.0, 0.0, 0.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 4/4.0}, 
			{1/4.0, 1/4.0, 1/4.0, 1/4.0, 3/4.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		expLeftThree.add(new double[][]{{23.0, 24.0, 14.0, 12.0, 0.0}, {17/32.0, 4/4.0, 7/8.0, 5/4.0, 5/4.0}, 
			{2/4.0, 3/4.0, 3/4.0, 3/4.0, 4/4.0}, {0.0, 0.0, 0.0, 0.0, 0.0}}); 
		expLeftThree.add(new double[][]{{21.0, 15.0, 15.0, 5.0, 0.0}, {9/16.0, 5/4.0, 4/4.0, 6/4.0, 11/8.0}, 
			{17/32.0, 4/4.0, 7/8.0, 5/4.0, 5/4.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		//
		expLeftThree.add(new double[][]{{12.0, 7.0, 0.0, 12.0, 12.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 4/4.0},
			{1/4.0, 1/4.0, 1/4.0, 1/4.0, 3/4.0}, {0.0, 0.0, 0.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{11.0, 12.0, 2.0, 0.0, 12.0}, {17/32.0, 4/4.0, 7/8.0, 5/4.0, 5/4.0}, 
			{2/4.0, 3/4.0, 3/4.0, 3/4.0, 4/4.0}, {0.0, 0.0, 0.0, 0.0, 1.0}});
		expLeftThree.add(new double[][]{{9.0, 3.0, 3.0, 7.0, 12.0}, {9/16.0, 5/4.0, 4/4.0, 6/4.0, 11/8.0}, 
			{17/32.0, 4/4.0, 7/8.0, 5/4.0, 5/4.0}, {0.0, 0.0, 0.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{5.0, 0.0, 7.0, 19.0, 19.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 4/4.0},
			{1/4.0, 1/4.0, 1/4.0, 1/4.0, 3/4.0}, {0.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{4.0, 5.0, 5.0, 7.0, 19.0}, {17/32.0, 4/4.0, 7/8.0, 5/4.0, 5/4.0}, 
			{2/4.0, 3/4.0, 3/4.0, 3/4.0, 4/4.0}, {0.0, 0.0, 1.0, 1.0, 1.0}}); 
		expLeftThree.add(new double[][]{{2.0, 4.0, 4.0, 14.0, 19.0}, {9/16.0, 5/4.0, 4/4.0, 6/4.0, 11/8.0}, 
			{17/32.0, 4/4.0, 7/8.0, 5/4.0, 5/4.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		//
		expLeftThree.add(new double[][]{{0.0, 5.0, 12.0, 24.0, 24.0}, {1/2.0, 3/4.0, 3/4.0, 3/4.0, 4/4.0},
			{1/4.0, 1/4.0, 1/4.0, 1/4.0, 3/4.0}, {0.0, 1.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{1.0, 0.0, 10.0, 12.0, 24.0}, {17/32.0, 4/4.0, 7/8.0, 5/4.0, 5/4.0}, 
			{2/4.0, 3/4.0, 3/4.0, 3/4.0, 4/4.0}, {1.0, 0.0, 1.0, 1.0, 1.0}});
		expLeftThree.add(new double[][]{{3.0, 9.0, 9.0, 19.0, 24.0}, {9/16.0, 5/4.0, 4/4.0, 6/4.0, 11/8.0}, 
			{17/32.0, 4/4.0, 7/8.0, 5/4.0, 5/4.0}, {1.0, 1.0, 1.0, 1.0, 1.0}});
		expected.addAll(expLeftThree);

		// c. Direction.RIGHT (decisionContextSize = 1)
		List<double[][]> expectedRight = new ArrayList<double[][]>();
		// Chord 15
		expectedRight.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}});
		expectedRight.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}});
		expectedRight.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}});
		expectedRight.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}});
		// Chords 14-9
		expectedRight.add(new double[][]{{0.0, 5.0, 12.0, 24.0, -1.0}, {-1/2.0, -1/2.0, -1/2.0, -1/2.0, -1.0},	
			{-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1.0}});
		expectedRight.add(new double[][]{{1.0, 4.0, 11.0, 23.0, -1.0}, {-1/32.0, -17/32.0, -17/32.0, -17/32.0, -1.0},	
			{0.0, -1/2.0, -1/2.0, -1/2.0, -1.0}});
		expectedRight.add(new double[][]{{2.0, 2.0, 9.0, 21.0, -1.0}, {-1/32.0, -9/16.0, -9/16.0, -9/16.0, -1.0},	
			{0.0, -17/32.0, -17/32.0, -17/32.0, -1.0}});
		expectedRight.add(new double[][]{{2.0, 4.0, 11.0, 23.0, -1.0}, {-1/32.0, -19/32.0, -19/32.0, -19/32.0, -1.0},	
			{0.0, -9/16.0, -9/16.0, -9/16.0, -1.0}});
		expectedRight.add(new double[][]{{1.0, 5.0, 12.0, 24.0, -1.0}, {-1/32.0, -5/8.0, -5/8.0, -5/8.0, -1.0},	
			{0.0, -19/32.0, -19/32.0, -19/32.0, -1.0}});
		expectedRight.add(new double[][]{{1.0, 4.0, 11.0, 23.0, -1.0}, {-1/16.0, -11/16.0, -11/16.0, -11/16.0, -1.0},	
			{0.0, -5/8.0, -5/8.0, -5/8.0, -1.0}});
		// Chord 8
		expectedRight.add(new double[][]{{23.0, 19.0, 12.0, 0.0, -1.0}, {-1/16.0, -3/4.0, -3/4.0, -3/4.0, -1.0},
			{7/16.0, -1/4.0, -1/4.0, -1/4.0, -1.0}});
		expectedRight.add(new double[][]{{11.0, 7.0, 0.0, 12.0, -1.0}, {-1/16.0, -3/4.0, -3/4.0, -3/4.0, -1.0},
			{7/16.0, -1/4.0, -1/4.0, -1/4.0, -1.0}});
		expectedRight.add(new double[][]{{4.0, 0.0, 7.0, 19.0, -1.0}, {-1/16.0, -3/4.0, -3/4.0, -3/4.0, -1.0},
			{7/16.0, -1/4.0, -1/4.0, -1/4.0, -1.0}});
		expectedRight.add(new double[][]{{1.0, 5.0, 12.0, 24.0, -1.0}, {-1/16.0, -3/4.0, -3/4.0, -3/4.0, -1.0},
			{0.0, -11/16.0, -11/16.0, -11/16.0, -1.0}});
		// Chord 7
		expectedRight.add(new double[][]{{10.0, 5.0, 2.0, 14.0, -1.0}, {-1/8.0, -1/8.0, -1/8.0, -1/8.0, -1.0}, 
			{0.0, 0.0, 0.0, 0.0, -1.0}});
		expectedRight.add(new double[][]{{1.0, 4.0, 11.0, 23.0, -1.0}, {-1/8.0, -1/8.0, -1/8.0, -1/8.0, -1.0}, 
			{0.0, 0.0, 0.0, 0.0, -1.0}});
		// Chord 6
		expectedRight.add(new double[][]{{23.0, 19.0, 14.0, 0.0, -1.0}, {-1/8.0, -1/4.0, -1/8.0, -1/4.0, -1.0}, 
			{1/8.0, 0.0, 1/8.0, 0.0, -1.0}});
		expectedRight.add(new double[][]{{8.0, 4.0, 1.0, 15.0, -1.0}, {-1/8.0, -1/4.0, -1/8.0, -1/4.0, -1.0}, 
			{0.0, -1/8.0, 0.0, -1/8.0, -1.0}});
		expectedRight.add(new double[][]{{4.0, 0.0, 5.0, 19.0, -1.0}, {-1/8.0, -1/4.0, -1/8.0, -1/4.0, -1.0}, 
			{0.0, -1/8.0, 0.0, -1/8.0, -1.0}});
		expectedRight.add(new double[][]{{1.0, 5.0, 10.0, 24.0, -1.0}, {-1/8.0, -1/4.0, -1/8.0, -1/4.0, -1.0}, 
			{1/8.0, 0.0, 1/8.0, 0.0, -1.0}});
		// Chord 5
		expectedRight.add(new double[][]{{19.0, 24.0, 15.0, 0.0, 0.0}, {-1/4.0, -1/4.0, -1/4.0, -1/2.0, -1/4.0}, 
			{0.0, 0.0, 0.0, -1/4.0, 0.0}});
		expectedRight.add(new double[][]{{7.0, 12.0, 3.0, 12.0, 12.0}, {-1/4.0, -1/4.0, -1/4.0, -1/2.0, -1/4.0}, 
			{1/4.0, 1/4.0, 1/4.0, 0.0, 1/4.0}});
		expectedRight.add(new double[][]{{7.0, 12.0, 3.0, 12.0, 12.0}, {-1/4.0, -1/4.0, -1/4.0, -1/2.0, -1/4.0}, 
			{0.0, 0.0, 0.0, -1/4.0, 0.0}});
		expectedRight.add(new double[][]{{4.0, 9.0, 0.0, 15.0, 15.0}, {-1/4.0, -1/4.0, -1/4.0, -1/2.0, -1/4.0}, 
			{0.0, 0.0, 0.0, -1/4.0, 0.0}});
		expectedRight.add(new double[][]{{5.0, 0.0, 9.0, 24.0, 24.0}, {-1/4.0, -1/4.0, -1/4.0, -1/2.0, -1/4.0}, 
			{0.0, 0.0, 0.0, -1/4.0, 0.0}});
		// Chord 4
		expectedRight.add(new double[][]{{24.0, 15.0, 12.0, 12.0, 0.0}, {-1/8.0, -1/8.0, -1/8.0, -1/8.0, -1/8.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}});
		// Chord 3
		expectedRight.add(new double[][]{{22.0, 13.0, 10.0, 10.0, 2.0}, {-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1/8.0}, 
			{-1/8.0, -1/8.0, -1/8.0, -1/8.0, 0.0}});
		expectedRight.add(new double[][]{{19.0, 10.0, 7.0, 7.0, 5.0}, {-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1/8.0}, 
			{0.0, 0.0, 0.0, 0.0, 1/8.0}});
		expectedRight.add(new double[][]{{10.0, 1.0, 2.0, 2.0, 14.0}, {-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1/8.0}, 
			{0.0, 0.0, 0.0, 0.0, 1/8.0}});
		expectedRight.add(new double[][]{{4.0, 5.0, 8.0, 8.0, 20.0}, {-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1/8.0}, 
			{0.0, 0.0, 0.0, 0.0, 1/8.0}});
		expectedRight.add(new double[][]{{4.0, 5.0, 8.0, 8.0, 20.0}, {-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1/8.0}, 
			{-1/8.0, -1/8.0, -1/8.0, -1/8.0, 0.0}});
		// Chord 2
		expectedRight.add(new double[][]{{17.0, 17.0, 11.0, 2.0, 1.0}, {-1/16.0, -1/16.0, -1/16.0, -1/16.0, -1/16.0}, 
			{0.0, 0.0, 0.0, 0.0, 0.0}});
		// Chord 1
		expectedRight.add(new double[][]{{20.0, 20.0, 14.0, 3.0, 2.0}, {-1/4.0, -1/4.0, -1/4.0, -3/16.0, -1/4.0}, 
			{-1/16.0, -1/16.0, -1/16.0, 0.0, -1/16.0}});
		expectedRight.add(new double[][]{{8.0, 8.0, 2.0, 9.0, 10.0}, {-1/4.0, -1/4.0, -1/4.0, -3/16.0, -1/4.0}, 
			{0.0, 0.0, 0.0, 1/16.0, 0.0}});
		expectedRight.add(new double[][]{{4.0, 4.0, 10.0, 21.0, 22.0}, {-1/4.0, -1/4.0, -1/4.0, -3/16.0, -1/4.0}, 
			{-1/8.0, -1/8.0, -1/8.0, -1/16.0, -1/8.0}});
		expectedRight.add(new double[][]{{7.0, 7.0, 13.0, 24.0, 25.0}, {-1/4.0, -1/4.0, -1/4.0, -3/16.0, -1/4.0}, 
			{0.0, 0.0, 0.0, 1/16.0, 0.0}});
		// Chord 0
		expectedRight.add(new double[][]{{22.0, 19.0, 7.0, 5.0, 3.0}, {-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1/2.0}, 
			{0.0, 0.0, 0.0, 0.0, -1/4.0}});
		expectedRight.add(new double[][]{{15.0, 12.0, 0.0, 12.0, 10.0}, {-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1/2.0}, 
			{0.0, 0.0, 0.0, 0.0, -1/4.0}});
		expectedRight.add(new double[][]{{7.0, 4.0, 8.0, 20.0, 18.0}, {-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1/2.0}, 
			{0.0, 0.0, 0.0, 0.0, -1/4.0}});
		expectedRight.add(new double[][]{{3.0, 0.0, 12.0, 24.0, 22.0}, {-1/4.0, -1/4.0, -1/4.0, -1/4.0, -1/2.0}, 
			{0.0, 0.0, 0.0, 0.0, -1/4.0}});
		expected.addAll(expectedRight);

		// d. Direction.LEFT (decisionContextSize = 3, averaged)
		List<double[][]> expectedAvg = new ArrayList<double[][]>();
		// Chord 0
		expectedAvg.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {0.0, 0.0, 0.0, 0.0, 0.0}}); 
		expectedAvg.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		expectedAvg.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0},	
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		expectedAvg.add(new double[][]{{-1.0, -1.0, -1.0, -1.0, -1.0}, {-1.0, -1.0, -1.0, -1.0, -1.0}, 
			{-1.0, -1.0, -1.0, -1.0, -1.0}, {0.0, 0.0, 0.0, 0.0, 0.0}});
		// Chord 1
		expectedAvg.add(new double[][]{
			{1.0/(24+1), 1.0/(20+1), 1.0/(12+1), 1.0/(5+1), -1.0}, 
			{1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), -1.0}, 
			{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0}, 
			{-24.0, -20.0, -12.0, -5.0, 0.0}
		}); 
		expectedAvg.add(new double[][]{
			{1.0/(12+1), 1.0/(8+1), 1.0/(0+1), 1.0/(7+1), -1.0},
			{1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), -1.0},
			{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0},
			{-12.0, -8.0, 0.0, 7.0, 0.0}
		});
		expectedAvg.add(new double[][]{
			{1.0/(0+1), 1.0/(4+1), 1.0/(12+1), 1.0/(19+1), -1.0},
			{1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), -1.0},
			{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0},
			{0.0, 4.0, 12.0, 19.0, 0.0}
		});
		expectedAvg.add(new double[][]{
			{1.0/(3+1), 1.0/(7+1), 1.0/(15+1), 1.0/(22+1), -1.0},
			{1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), -1.0},
			{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0},
			{3.0, 7.0, 15.0, 22.0, 0.0}
		});
		// Chord 5
		expectedAvg.add(new double[][]{
			{(1.0/(20+1) + 1.0/(27+1) + 1.0/(24+1)) / 3.0, // pitchProx 
			 (1.0/(20+1) + 1.0/(24+1) + 1.0/(20+1)) / 3.0, 
			 (1.0/(14+1) + 1.0/(12+1) + 1.0/(12+1)) / 3.0, 
			 (1.0/(5+1)  + 1.0/(3+1)  + 1.0/(0+1))  / 3.0, 
			 (1.0/(0+1) + 1.0/(2+1)) / 2.0},
			{(1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, // ioProx 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1))  / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((1/4.0)+1)) / 2.0},
			{(1.0/(0+1) + 1.0/((1/4.0)+1) + 1.0/((2/4.0)+1)) / 3.0, // ooProx
			 (1.0/((1/8.0)+1) + 1.0/((3/8.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/(0+1) + 1.0/((1/4.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/(0+1) + 1.0/((1/4.0)+1) + 1.0/((5/16.0)+1))  / 3.0, 
			 (1.0/(0+1) + 1.0/((1/8.0)+1)) / 2.0},
			{(-20 + -27 + -24) / 3.0, // mvmts
			 (-20 + -24 + -20) / 3.0, 
			 (-14 + -12 + -12) / 3.0, 
			 (-5 + -3 + 0) / 3.0, 
			 -0.99999 /*(0 + -2) / 2.0*/}
		});
		expectedAvg.add(new double[][]{
			{(1.0/(8+1) + 1.0/(15+1) + 1.0/(12+1)) / 3.0, // pitchProx 
			 (1.0/(8+1) + 1.0/(12+1) + 1.0/(8+1)) / 3.0, 
			 (1.0/(2+1) + 1.0/(0+1) + 1.0/(0+1)) / 3.0, 
			 (1.0/(7+1)  + 1.0/(9+1)  + 1.0/(12+1))  / 3.0, 
			 (1.0/(12+1) + 1.0/(10+1)) / 2.0},
			{(1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, // ioProx 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1))  / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((1/4.0)+1)) / 2.0},
			{(1.0/(0+1) + 1.0/((1/4.0)+1) + 1.0/((2/4.0)+1)) / 3.0, // ooProx
			 (1.0/((1/8.0)+1) + 1.0/((3/8.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/(0+1) + 1.0/((1/4.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/(0+1) + 1.0/((1/4.0)+1) + 1.0/((5/16.0)+1))  / 3.0, 
			 (1.0/(0+1) + 1.0/((1/8.0)+1)) / 2.0},
			{(-8 + -15 + -12) / 3.0, // mvmts
			 (-8 + -12 + -8) / 3.0, 
			 (-2 + -0 + -0) / 3.0, 
			 (7 + 9 + 12) / 3.0, 
			 (12 + 10) / 2.0}
		});
		expectedAvg.add(new double[][]{
			{(1.0/(8+1) + 1.0/(15+1) + 1.0/(12+1)) / 3.0, // pitchProx 
			 (1.0/(8+1) + 1.0/(12+1) + 1.0/(8+1)) / 3.0, 
			 (1.0/(2+1) + 1.0/(0+1) + 1.0/(0+1)) / 3.0, 
			 (1.0/(7+1)  + 1.0/(9+1)  + 1.0/(12+1))  / 3.0, 
			 (1.0/(12+1) + 1.0/(10+1)) / 2.0},
			{(1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, // ioProx 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1))  / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((1/4.0)+1)) / 2.0},
			{(1.0/(0+1) + 1.0/((1/4.0)+1) + 1.0/((2/4.0)+1)) / 3.0, // ooProx
			 (1.0/((1/8.0)+1) + 1.0/((3/8.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/(0+1) + 1.0/((1/4.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/(0+1) + 1.0/((1/4.0)+1) + 1.0/((5/16.0)+1))  / 3.0, 
			 (1.0/(0+1) + 1.0/((1/8.0)+1)) / 2.0},
			{(-8 + -15 + -12) / 3.0, // mvmts
			 (-8 + -12 + -8) / 3.0, 
			 (-2 + -0 + -0) / 3.0, 
			 (7 + 9 + 12) / 3.0, 
			 (12 + 10) / 2.0}
		});
		expectedAvg.add(new double[][]{
			{(1.0/(5+1) + 1.0/(12+1) + 1.0/(9+1)) / 3.0, // pitchProx 
			 (1.0/(5+1) + 1.0/(9+1) + 1.0/(5+1)) / 3.0, 
			 (1.0/(1+1) + 1.0/(3+1) + 1.0/(3+1)) / 3.0, 
			 (1.0/(10+1) + 1.0/(12+1)  + 1.0/(15+1))  / 3.0, 
			 (1.0/(15+1) + 1.0/(13+1)) / 2.0},
			{(1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, // ioProx 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1))  / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((1/4.0)+1)) / 2.0},
			{(1.0/(0+1) + 1.0/((1/4.0)+1) + 1.0/((2/4.0)+1)) / 3.0, // ooProx
			 (1.0/((1/8.0)+1) + 1.0/((3/8.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/(0+1) + 1.0/((1/4.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/(0+1) + 1.0/((1/4.0)+1) + 1.0/((5/16.0)+1))  / 3.0, 
			 (1.0/(0+1) + 1.0/((1/8.0)+1)) / 2.0},
			{(-5 + -12 + -9) / 3.0, // mvmts
			 (-5 + -9 + -5) / 3.0, 
			 (1 + 3 + 3) / 3.0, 
			 (10 + 12 + 15) / 3.0, 
			 (15 + 13) / 2.0}
		});
		expectedAvg.add(new double[][]{
			{(1.0/(4+1) + 1.0/(3+1) + 1.0/(0+1)) / 3.0, // pitchProx 
			 (1.0/(4+1) + 1.0/(0+1) + 1.0/(4+1)) / 3.0, 
			 (1.0/(10+1) + 1.0/(12+1) + 1.0/(12+1)) / 3.0, 
			 (1.0/(19+1) + 1.0/(21+1)  + 1.0/(24+1))  / 3.0, 
			 (1.0/(24+1) + 1.0/(22+1)) / 2.0},
			{(1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, // ioProx 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((2/4.0)+1) + 1.0/((3/4.0)+1)) / 3.0, 
			 (1.0/((1/4.0)+1) + 1.0/((5/16.0)+1) + 1.0/((2/4.0)+1))  / 3.0, 
			 (1.0/((1/8.0)+1) + 1.0/((1/4.0)+1)) / 2.0},
			{(1.0/(0+1) + 1.0/((1/4.0)+1) + 1.0/((2/4.0)+1)) / 3.0, // ooProx
			 (1.0/((1/8.0)+1) + 1.0/((3/8.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/(0+1) + 1.0/((1/4.0)+1) + 1.0/((2/4.0)+1)) / 3.0, 
			 (1.0/(0+1) + 1.0/((1/4.0)+1) + 1.0/((5/16.0)+1))  / 3.0, 
			 (1.0/(0+1) + 1.0/((1/8.0)+1)) / 2.0},
			{(4 + -3 + 0) / 3.0, // mvmts
			 (4 + 0 + 4) / 3.0, 
			 (10 + 12 + 12) / 3.0, 
			 (19 + 21 + 24) / 3.0, 
			 (24 + 22) / 2.0}
		});
		expected.addAll(expectedAvg);
		
		// For all (non-averaged) elements of expected: turn the elements in all three Arrays 
		// from distances into proximities
		for (int i = 0; i < (expected.size() - expectedAvg.size()); i++) {
			double[][] currentExpected = expected.get(i);
			for (int j = 0; j < currentExpected.length; j++) {
				// Do only if not movements 
				if (j != 3) {
					double[] currentArray = currentExpected[j];
					for (int k = 0; k < currentArray.length; k++) {
						double oldValue = currentArray[k];
						// Do only if oldValue is not -1.0, i.e., if the voice is active
						if (oldValue != -1.0) {
							double newValue = 1.0/(oldValue + 1);
							// If oldValue is negative 
							if (oldValue < 0) {
								newValue = -(1.0/(-oldValue + 1));
							}
							currentArray[k] = newValue;
						}
					}
				}
			}
		}

		List<double[][]> actual = new ArrayList<double[][]>();
		NoteSequence noteSeq = transcription.getNoteSequence();
		// a. Direction.LEFT (decisionContextSize = 1)
		for (int i = 0; i < noteSeq.size(); i++) {
			Note currentNote = noteSeq.get(i);
			actual.addAll(FeatureGenerator.getPitchAndTimeProximitiesToAllVoices(null, 
				transcription, currentNote, Direction.LEFT, false, false, 1, false));
		}
		// b. Direction.LEFT (decisionContextSize = 3)
		for (int i = 0; i < noteSeq.size(); i++) {
			Note currentNote = noteSeq.get(i);
			actual.addAll(FeatureGenerator.getPitchAndTimeProximitiesToAllVoices(null, 
				transcription, currentNote, Direction.LEFT, false, false, 3, false));
		}
		// c. Direction.RIGHT (decisionContextSize = 1)
		List<Integer> backwardsMapping = FeatureGenerator.getBackwardsMapping(transcription.getNumberOfNewNotesPerChord());
		for (int i : backwardsMapping) {
			Note currentNote = noteSeq.get(i);
			actual.addAll(FeatureGenerator.getPitchAndTimeProximitiesToAllVoices(null, 
				transcription, currentNote, Direction.RIGHT, false, false, 1, false));
		}
		// d. Direction.LEFT (decisionContextSize = 3, averaged)
		for (int i : Arrays.asList(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 15, 16, 17, 18, 19})) {
			actual.addAll(FeatureGenerator.getPitchAndTimeProximitiesToAllVoices(null, 
				transcription, noteSeq.get(i), Direction.LEFT, false, false, 3, true));
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) { // i = onsetindex
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) { // j = one of the three proximities Arrays
				assertEquals(expected.get(i)[j].length, actual.get(i)[j].length);
				for (int k = 0; k < expected.get(i)[j].length; k++) { // k = the value of element k in the current proximities Array
					assertEquals(expected.get(i)[j][k], actual.get(i)[j][k]);
				}
			}
		}
	}
	
	
//	public void testGetProximitiesAndMovementToVoiceAvg() {
//		Tablature tablature = new Tablature(encodingTestpiece1, true);
//		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);
//
//		List<double[]> expected = new ArrayList<double[]>();
//		// a. Direction.LEFT (decisionContextSize = 1)
//		// Chord 0
//		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
//		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
//		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
//		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
//		 // Chord 1
//		expected.add(new double[]{5.0, 1/4.0, 0.0, 0.0, -5.0});
//		expected.add(new double[]{0.0, 1/4.0, 0.0, 0.0, 0.0});
//		expected.add(new double[]{3.0, 1/4.0, 0.0, 0.0, 3.0});
//		expected.add(new double[]{4.0, 1/4.0, 0.0, 0.0, 4.0});
//		// Chord 2
//		expected.add(new double[]{3.0, 3/16.0, 0.0, 0.0, 3.0});
//		// Chord 3
//		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
//		expected.add(new double[]{2.0, 1/16.0, 0.0, 0.0, 2.0});
//		expected.add(new double[]{2.0, 1/4.0, 1/16.0, 0.0, 2.0});
//		expected.add(new double[]{7.0, 1/4.0, 1/16.0, 0.0, -7.0});
//		expected.add(new double[]{4.0, 1/4.0, 1/16.0, 1/8.0, -4.0});
//		// Chord 4
//		expected.add(new double[]{2.0, 1/8.0, 0.0, 0.0, -2.0});
//		// Chord 5
//		expected.add(new double[]{0.0, 1/8.0, 0.0, 0.0, 0.0});
//		expected.add(new double[]{7.0, 1/4.0, 1/8.0, 0.0, 7.0});
//		expected.add(new double[]{2.0, 1/4.0, 1/8.0, 0.0, -2.0});
//		expected.add(new double[]{5.0, 1/4.0, 1/8.0, 1/8.0, -5.0});
//		expected.add(new double[]{4.0, 1/4.0, 1/8.0, 0.0, 4.0});
//		// Chord 6
//		expected.add(new double[]{0.0, 1/4.0, 0.0, 0.0, 0.0});
//		expected.add(new double[]{3.0, 1/4.0, 0.0, 0.0, 3.0});
//		expected.add(new double[]{5.0, 1/4.0, 0.0, 0.0, -5.0});
//		expected.add(new double[]{9.0, 1/4.0, 0.0, 0.0, 9.0});
//		// Chord 7
//		expected.add(new double[]{1.0, 1/8.0, 0.0, 0.0, -1.0});
//		expected.add(new double[]{4.0, 1/8.0, 0.0, 0.0, 4.0});
//		// Chord 8
//		expected.add(new double[]{12.0, 1/2.0, 1/4.0, 0.0, -12.0});
//		expected.add(new double[]{2.0, 1/8.0, 0.0, 0.0, -2.0});
//		expected.add(new double[]{5.0, 1/4.0, 1/8.0, 0.0, -5.0});
//		expected.add(new double[]{1.0, 1/8.0, 0.0, 0.0, 1.0});
//		// Chords 9-14
//		expected.add(new double[]{1.0, 1/16.0, 0.0, 0.0, -1.0});
//		expected.add(new double[]{1.0, 1/16.0, 0.0, 0.0, 1.0});
//		expected.add(new double[]{1.0, 1/32.0, 0.0, 0.0, -1.0});
//		expected.add(new double[]{2.0, 1/32.0, 0.0, 0.0, -2.0});
//		expected.add(new double[]{2.0, 1/32.0, 0.0, 0.0, 2.0});
//		expected.add(new double[]{1.0, 1/32.0, 0.0, 0.0, 1.0});
//		 // Chord 15
//		expected.add(new double[]{0.0, 3/4.0, 11/16.0, 1/4.0, 0.0});
//		expected.add(new double[]{0.0, 3/4.0, 11/16.0, 1/4.0, 0.0});
//		expected.add(new double[]{0.0, 3/4.0, 11/16.0, 1/4.0,0.0});
//		expected.add(new double[]{0.0, 2/4.0, 1/4.0, 1/4.0, 0.0});
//
//		// b. Direction.LEFT (decisionContextSize = 3)
//		// Chord 0
//		// Note 0
////		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
////		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
////		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
////		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
////		// Chord 1
////		expected.add(new double[]{5.0, 1/4.0, 0.0, 0.0, -5.0});
////		expected.add(new double[]{0.0, 1/4.0, 0.0, 0.0, 0.0});
////		expected.add(new double[]{3.0, 1/4.0, 0.0, 0.0, 3.0});
////		expected.add(new double[]{4.0, 1/4.0, 0.0, 0.0, 4.0});
////		// Chord 2
////		// Note 8
////		expected.add(new double[]{(3+5)/2.0, ((3/16.0)+(1/4.0))/2.0, (0+0)/2.0, (0+0)/2.0, (3+-5)/2.0}); // hoer 8
////		// Chord 3
////		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
////		expected.add(new double[]{(2+5+0)/3.0, ((1/16.0)+(1/4.0)+(1/2.0))/3.0, (0+(1/16.0)+(1/4.0))/3.0, (0+(1/16.0)+(1/4.0))/3.0, (2+5+0)/3.0});
////		expected.add(new double[]{(2+2)/2.0, ((1/4.0)+(1/2.0))/2.0, ((1/16.0)+(1/4.0))/2.0, (0+(1/4.0))/2.0, (2+2)/2.0});
////		expected.add(new double[]{(7+4)/2.0, ((1/4.0)+(1/2.0))/2.0, ((1/16.0)+(1/4.0))/2.0, (0+(1/4.0))/2.0, (-7+-4)/2.0});
////		expected.add(new double[]{(4+0)/2.0, ((1/4.0)+(1/2.0))/2.0, ((1/16.0)+(1/4.0))/2.0, ((1/8.0)+(1/4.0))/2.0, (-4+0)/2.0});
////		// Chord 4
////		expected.add(new double[]{2.0, 1/8.0, 0.0, 0.0, -2.0});
////		// Chord 5
////		expected.add(new double[]{(0+2)/2.0, ((1/8.0)+(1/4.0))/2.0, (0+(1/8.0))/2.0, (0+(1/8.0))/2.0, (0+-2)/2.0});
////		expected.add(new double[]{(7+9+12)/3.0, ((1/4.0)+(5/16.0)+(1/2.0))/3.0, ((1/8.0)+(1/4.0)+(5/16.0))/3.0, (0+(1/4.0)+(5/16.0))/3.0, (7+9+12)/3.0});
////		expected.add(new double[]{(2+0+0)/3.0, ((1/4.0)+(1/2.0)+(3/4.0))/3.0, ((1/8.0)+(5/16.0)+(1/2.0))/3.0, (0+(1/4.0)+(1/2.0))/3.0, (-2+0+0)/3.0});
////		expected.add(new double[]{(5+9+5)/3.0, ((1/4.0)+(1/2.0)+(3/4.0))/3.0, ((1/8.0)+(5/16.0)+(1/2.0))/3.0, ((1/8.0)+(3/8.0)+(1/2.0))/3.0, (-5+-9+-5)/3.0});
////		expected.add(new double[]{(4+3+0)/3.0, ((1/4.0)+(1/2.0)+(3/4.0))/3.0, ((1/8.0)+(5/16.0)+(1/2.0))/3.0, (0+(1/4.0)+(1/2.0))/3.0, (4+-3+0)/3.0});
////		// Chord 6
////		// Note 19
////		expected.add(new double[]{0.0, 1/4.0, 0.0, 0.0, 0.0}); // hoer 20
////		expected.add(new double[]{0.0, 3/8.0, 1/4.0, 1/4.0, 0.0});
////		expected.add(new double[]{2.0, 1/2.0, 3/8.0, 3/8.0, -2.0});
////		// Note 20
////		expected.add(new double[]{3.0, 1/4.0, 0.0, 0.0, 3.0});
////		expected.add(new double[]{1.0, 1/2.0, 3/8.0, 1/4.0, 1.0});
////		expected.add(new double[]{3.0, 3/4.0, 9/16.0, 1/2.0, 3.0});
////		// Note 21
////		expected.add(new double[]{5.0, 1/4.0, 0.0, 0.0, -5.0});
////		expected.add(new double[]{1.0, 1/2.0, 3/8.0, 1/4.0, -1.0});
////		expected.add(new double[]{8.0, 3/4.0, 9/16.0, 1/2.0, -8.0});
////		// Note 22
////		expected.add(new double[]{9.0, 1/4.0, 0.0, 0.0, 9.0});
////		expected.add(new double[]{4.0, 1/2.0, 3/8.0, 3/8.0, 4.0});
////		expected.add(new double[]{0.0, 3/4.0, 9/16.0, 5/8.0, 0.0});
////		// Chord 7
////		// Note 23
////		expected.add(new double[]{1.0, 1/8.0, 0.0, 0.0, -1.0});
////		expected.add(new double[]{2.0, 3/8.0, 1/8.0, 1/8.0, 2.0});
////		expected.add(new double[]{0.0, 5/8.0, 1/2.0, 3/8.0, 0.0});
////		// Note 24
////		expected.add(new double[]{4.0, 1/8.0, 0.0, 0.0, 4.0});
////		expected.add(new double[]{1.0, 3/8.0, 1/8.0, 1/8.0, -1.0});
////		expected.add(new double[]{3.0, 5/8.0, 1/2.0, 3/8.0, 3.0});
////		// Chord 8
////		// Note 25
////		expected.add(new double[]{12.0, 1/2.0, 1/4.0, 0.0, -12.0});
////		expected.add(new double[]{5.0, 3/4.0, 5/8.0, 1/2.0, -5.0});
////		expected.add(new double[]{3.0, 13/16.0, 3/4.0, 3/4.0, -3.0});
////		// Note 26
////		expected.add(new double[]{2.0, 1/8.0, 0.0, 0.0, -2.0});
////		expected.add(new double[]{3.0, 1/4.0, 1/8.0, 1/8.0, -3.0});
////		expected.add(new double[]{0.0, 1/2.0, 1/4.0, 1/4.0, 0.0});
////		// Note 27
////		expected.add(new double[]{5.0, 1/4.0, 1/8.0, 0.0, -5.0});
////		expected.add(new double[]{4.0, 1/2.0, 1/4.0, 1/4.0, 4.0});
////		expected.add(new double[]{1.0, 3/4.0, 5/8.0, 5/8.0, -1.0});
////		// Note 28
////		expected.add(new double[]{1.0, 1/8.0, 0.0, 0.0, 1.0});
////		expected.add(new double[]{5.0, 1/4.0, 1/8.0, 1/8.0, 5.0});
////		expected.add(new double[]{0.0, 1/2.0, 1/4.0, 1/4.0, 0.0});
////		// Chords 9-14
////		// Note 29
////		expected.add(new double[]{1.0, 1/16.0, 0.0, 0.0, -1.0});
////		expected.add(new double[]{0.0, 3/16.0, 1/16.0, 1/16.0, 0.0});
////		expected.add(new double[]{4.0, 5/16.0, 3/16.0, 3/16.0, 4.0});
////		// Note 30
////		expected.add(new double[]{1.0, 1/16.0, 0.0, 0.0, 1.0});
////		expected.add(new double[]{0.0, 1/8.0, 1/16.0, 1/16.0, 0.0});
////		expected.add(new double[]{1.0, 1/4.0, 1/8.0, 1/8.0, 1.0});
////		// Note 31
////		expected.add(new double[]{1.0, 1/32.0, 0.0, 0.0, -1.0});
////		expected.add(new double[]{0.0, 3/32.0, 1/32.0, 1/32.0, 0.0});
////		expected.add(new double[]{1.0, 5/32.0, 3/32.0, 3/32.0, -1.0});
////		// Note 32
////		expected.add(new double[]{2.0, 1/32.0, 0.0, 0.0, -2.0});
////		expected.add(new double[]{3.0, 1/16.0, 1/32.0, 1/32.0, -3.0});
////		expected.add(new double[]{2.0, 1/8.0, 1/16.0, 1/16.0, -2.0});
////		// Note 33
////		expected.add(new double[]{2.0, 1/32.0, 0.0, 0.0, 2.0});
////		expected.add(new double[]{0.0, 1/16.0, 1/32.0, 1/32.0, 0.0});
////		expected.add(new double[]{1.0, 3/32.0, 1/16.0, 1/16.0, -1.0});
////		// Note 34
////		expected.add(new double[]{1.0, 1/32.0, 0.0, 0.0, 1.0});
////		expected.add(new double[]{3.0, 1/16.0, 1/32.0, 1/32.0, 3.0});
////		expected.add(new double[]{1.0, 3/32.0, 1/16.0, 1/16.0, 1.0}); 
////		// Chord 15
////		// Note 35
////		expected.add(new double[]{0.0, 3/4.0, 11/16.0, 1/4.0, 0.0});
////		expected.add(new double[]{12.0, 5/4.0, 4/4.0, 3/4.0, -12.0});
////		expected.add(new double[]{5.0, 6/4.0, 11/8.0, 5/4.0, -5.0});
////		// Note 36
////		expected.add(new double[]{0.0, 3/4.0, 11/16.0, 1/4.0, 0.0});
////		expected.add(new double[]{2.0, 7/8.0, 3/4.0, 3/4.0, -2.0});
////		expected.add(new double[]{3.0, 4/4.0, 7/8.0, 7/8.0, -3.0});
////		// Note 37
////		expected.add(new double[]{0.0, 3/4.0, 11/16.0, 1/4.0,0.0});
////		expected.add(new double[]{5.0, 4/4.0, 7/8.0, 3/4.0, -5.0});
////		expected.add(new double[]{4.0, 5/4.0, 4/4.0, 4/4.0, 4.0});
////		// Note 38
////		expected.add(new double[]{0.0, 1/2.0, 1/4.0, 1/4.0, 0.0});
////		expected.add(new double[]{1.0, 17/32.0, 1/2.0, 1/2.0, 1.0});
////		expected.add(new double[]{3.0, 9/16.0, 17/32.0, 17/32.0, 3.0});
//
//		// c. Direction.RIGHT (decisionContextSize = 1)
//		List<double[]> expectedRight = new ArrayList<double[]>();	
//		// Chord 15
//		expectedRight.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
//		expectedRight.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
//		expectedRight.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
//		expectedRight.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
//		// Chords 14-9
//		expectedRight.add(new double[]{0.0, -1/2.0, -1/4.0, -1/4.0, 0.0});
//		expectedRight.add(new double[]{1.0, -1/32.0, 0.0, 0.0, -1.0});
//		expectedRight.add(new double[]{2.0, -1/32.0, 0.0, 0.0, -2.0});
//		expectedRight.add(new double[]{2.0, -1/32.0, 0.0, 0.0, 2.0});
//		expectedRight.add(new double[]{1.0, -1/32.0, 0.0, 0.0, 1.0});
//		expectedRight.add(new double[]{1.0, -1/16.0, 0.0, 0.0, -1.0});
//		// Chord 8
//		expectedRight.add(new double[]{0.0, -3/4.0, -11/16.0, -1/4.0, 0.0});
//		expectedRight.add(new double[]{0.0, -3/4.0, -11/16.0, -1/4.0, 0.0});
//		expectedRight.add(new double[]{0.0, -3/4.0, -11/16.0, -1/4.0, 0.0});
//		expectedRight.add(new double[]{1.0, -1/16.0, 0.0, 0.0, 1.0});
//		// Chord 7
//		expectedRight.add(new double[]{2.0, -1/8.0, 0.0, 0.0, 2.0});
//		expectedRight.add(new double[]{1.0, -1/8.0, 0.0, 0.0, -1.0});
//		// Chord 6
//		expectedRight.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
//		expectedRight.add(new double[]{1.0, -1/8.0, 0.0, 0.0, 1.0});
//		expectedRight.add(new double[]{4.0, -1/8.0, 0.0, 0.0, -4.0});
//		expectedRight.add(new double[]{5.0, -1/4.0, -1/8.0, 0.0, 5.0});
//		// Chord 5
//		expectedRight.add(new double[]{0.0, -1/4.0, 0.0, 0.0, 0.0});
//		expectedRight.add(new double[]{12.0, -1/2.0, -1/4.0, 0.0, 12.0});
//		expectedRight.add(new double[]{3.0, -1/4.0, 0.0, 0.0, -3.0});
//		expectedRight.add(new double[]{9.0, -1/4.0, 0.0, 0.0, -9.0});
//		expectedRight.add(new double[]{5.0, -1/4.0, 0.0, 0.0, 5.0});
//		// Chord 4
//		expectedRight.add(new double[]{0.0, -1/8.0, 0.0, 0.0, 0.0});
//		// Chord 3
//		expectedRight.add(new double[]{2.0, -1/8.0, 0.0, 0.0, 2.0});
//		expectedRight.add(new double[]{7.0, -1/4.0, -1/8.0, 0.0, -7.0});
//		expectedRight.add(new double[]{2.0, -1/4.0, -1/8.0, 0.0, 2.0});
//		expectedRight.add(new double[]{4.0, -1/4.0, -1/8.0, 0.0, -4.0});
//		expectedRight.add(new double[]{5.0, -1/4.0, -1/8.0, 0.0, 5.0}); // full duration of currentNote = 1/4 because in the NoteSequence, the CoDNote with the longest duration is retained 
//		// Chord 2                                                        
//		expectedRight.add(new double[]{2.0, -1/16.0, 0.0, 0.0, -2.0});
//		// Chord 1
//		expectedRight.add(new double[]{3.0, -3/16.0, 0.0, 0.0, -3.0});
//		expectedRight.add(new double[]{2.0, -1/4.0, -1/16.0, 0.0, -2.0});
//		expectedRight.add(new double[]{7.0, -1/4.0, -1/16.0, 0.0, 7.0});
//		expectedRight.add(new double[]{4.0, -1/4.0, -1/16.0, -1/8.0, 4.0});
//		// Chord 0
//		expectedRight.add(new double[]{5.0, -1/4.0, 0.0, 0.0, 5.0});
//		expectedRight.add(new double[]{0.0, -1/4.0, 0.0, 0.0, 0.0});
//		expectedRight.add(new double[]{4.0, -1/4.0, 0.0, 0.0, -4.0});
//		expectedRight.add(new double[]{3.0, -1/4.0, 0.0, 0.0, -3.0});
//		expected.addAll(expectedRight);
//
//		// For each element of expected: turn the first four elements from distances into proximities
//		for (int i = 0; i < expected.size(); i++) {
//			double[] currentArray = expected.get(i);
//			for (int j = 0; j < currentArray.length - 1; j++) {
//				double oldValue = currentArray[j]; 
//				// Do only if oldValue is not -1.0, i.e., if the voice is active
//				if (oldValue != -1.0) {
//					double newValue = 1.0/(oldValue + 1);
//					// If oldValue is negative
//					if (oldValue < 0) {
//						newValue = -(1.0/(-oldValue + 1));
//					}
//					currentArray[j] = newValue;
//				}
//			}
//		}
//
//		List<double[]> actual = new ArrayList<double[]>();
//		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//		List<List<Double>> voiceLabels = transcription.getVoiceLabels();
//		NoteSequence noteSeq = transcription.getNoteSequence();
//		// a. Direction.LEFT (decisionContextSize = 1)
//		for (int i = 0; i < basicTabSymbolProperties.length; i++) {
//			Note currentNote = noteSeq.get(i);
//			List<Double> currentLabel = voiceLabels.get(i);
//			List<Integer> currentVoices = 
//				DataConverter.convertIntoListOfVoices(currentLabel);
//			// For each voice (in case of a CoD, the highest, i.e., the voice with the lowest number, will be dealt with first)
//			for (int j = 0; j < currentVoices.size(); j++) {
//				int currentVoice = currentVoices.get(j);
//				NotationVoice currentVoiceToCompareTo = transcription.getPiece().getScore().get(currentVoice).get(0);
//				actual.add(FeatureGenerator.getProximitiesAndMovementToVoiceAvg(
//					basicTabSymbolProperties, currentVoiceToCompareTo, currentNote, 
//					Direction.LEFT, 1));
//			}
//		}
//		// b. Direction.LEFT (decisionContextSize = 3)
//		for (int i = 0; i < basicTabSymbolProperties.length; i++) {
//			Note currentNote = noteSeq.get(i);
//			List<Double> currentLabel = voiceLabels.get(i);
//			List<Integer> currentVoices = 
//				DataConverter.convertIntoListOfVoices(currentLabel);
//			// For each voice (in case of a CoD, the highest, i.e., the voice with the lowest number, will be dealt with first)
//			for (int j = 0; j < currentVoices.size(); j++) {
//				int currentVoice = currentVoices.get(j);
//				NotationVoice currentVoiceToCompareTo = transcription.getPiece().getScore().get(currentVoice).get(0);
////				actual.add(FeatureGenerator.getProximitiesAndMovementToVoiceAvg(
////					basicTabSymbolProperties, currentVoiceToCompareTo, currentNote, 
////					Direction.LEFT, 3));
//			}
//		}
//		// c. Direction.RIGHT (decisionContextSize = 1)
//		List<Integer> backwardsMapping = FeatureGenerator.getBackwardsMapping(tablature.getNumberOfNotesPerChord());
//		for (int i : backwardsMapping) {
//			Note currentNote = noteSeq.get(i);
//			List<Double> currentLabel = voiceLabels.get(i);
//			List<Integer> currentVoices = DataConverter.convertIntoListOfVoices(currentLabel);
//			// For each voice (in case of a CoD, the highest, i.e., the voice with the lowest number, will be dealt with first)
//			for (int j = 0; j < currentVoices.size(); j++) {
//				int currentVoice = currentVoices.get(j);
//				NotationVoice currentVoiceToCompareTo = 
//					transcription.getPiece().getScore().get(currentVoice).get(0);
//				actual.add(FeatureGenerator.getProximitiesAndMovementToVoiceAvg(
//					basicTabSymbolProperties, currentVoiceToCompareTo, 
//					currentNote, Direction.RIGHT, 1));
//			}
//		}
//
//		assertEquals(expected.size(), actual.size());
//		for (int i = 0; i < expected.size(); i++) {
//			System.out.println("i = " + i);
//			assertEquals(expected.get(i).length, actual.get(i).length);
//			for (int j = 0; j < expected.get(i).length; j++) {
//				System.out.println("j = " + j);
//				assertEquals(expected.get(i)[j], actual.get(i)[j]);
//			}
//		}
//	}


	// EEN gedaan
	public void testGetProximitiesAndMovementToVoiceAll() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		List<double[]> expected = new ArrayList<double[]>();
		// a. Direction.LEFT (decisionContextSize = 1)
		// Chord 0
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		 // Chord 1
		expected.add(new double[]{5.0, 1/4.0, 0.0, 0.0, -5.0});
		expected.add(new double[]{0.0, 1/4.0, 0.0, 0.0, 0.0});
		expected.add(new double[]{3.0, 1/4.0, 0.0, 0.0, 3.0});
		expected.add(new double[]{4.0, 1/4.0, 0.0, 0.0, 4.0});
		// Chord 2
		expected.add(new double[]{3.0, 3/16.0, 0.0, 0.0, 3.0});
		// Chord 3
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{2.0, 1/16.0, 0.0, 0.0, 2.0});
		expected.add(new double[]{2.0, 1/4.0, 1/16.0, 0.0, 2.0});
		expected.add(new double[]{7.0, 1/4.0, 1/16.0, 0.0, -7.0});
		expected.add(new double[]{4.0, 1/4.0, 1/16.0, 1/8.0, -4.0});
		// Chord 4
		expected.add(new double[]{2.0, 1/8.0, 0.0, 0.0, -2.0});
		// Chord 5
		expected.add(new double[]{0.0, 1/8.0, 0.0, 0.0, 0.0});
		expected.add(new double[]{7.0, 1/4.0, 1/8.0, 0.0, 7.0});
		expected.add(new double[]{2.0, 1/4.0, 1/8.0, 0.0, -2.0});
		expected.add(new double[]{5.0, 1/4.0, 1/8.0, 1/8.0, -5.0});
		expected.add(new double[]{4.0, 1/4.0, 1/8.0, 0.0, 4.0});
		// Chord 6
		expected.add(new double[]{0.0, 1/4.0, 0.0, 0.0, 0.0});
		expected.add(new double[]{3.0, 1/4.0, 0.0, 0.0, 3.0});
		expected.add(new double[]{5.0, 1/4.0, 0.0, 0.0, -5.0});
		expected.add(new double[]{9.0, 1/4.0, 0.0, 0.0, 9.0});
		// Chord 7
		expected.add(new double[]{1.0, 1/8.0, 0.0, 0.0, -1.0});
		expected.add(new double[]{4.0, 1/8.0, 0.0, 0.0, 4.0});
		// Chord 8
		expected.add(new double[]{12.0, 1/2.0, 1/4.0, 0.0, -12.0});
		expected.add(new double[]{2.0, 1/8.0, 0.0, 0.0, -2.0});
		expected.add(new double[]{5.0, 1/4.0, 1/8.0, 0.0, -5.0});
		expected.add(new double[]{1.0, 1/8.0, 0.0, 0.0, 1.0});
		// Chords 9-14
		expected.add(new double[]{1.0, 1/16.0, 0.0, 0.0, -1.0});
		expected.add(new double[]{1.0, 1/16.0, 0.0, 0.0, 1.0});
		expected.add(new double[]{1.0, 1/32.0, 0.0, 0.0, -1.0});
		expected.add(new double[]{2.0, 1/32.0, 0.0, 0.0, -2.0});
		expected.add(new double[]{2.0, 1/32.0, 0.0, 0.0, 2.0});
		expected.add(new double[]{1.0, 1/32.0, 0.0, 0.0, 1.0});
		 // Chord 15
		expected.add(new double[]{0.0, 3/4.0, 11/16.0, 1/4.0, 0.0});
		expected.add(new double[]{0.0, 3/4.0, 11/16.0, 1/4.0, 0.0});
		expected.add(new double[]{0.0, 3/4.0, 11/16.0, 1/4.0,0.0});
		expected.add(new double[]{0.0, 2/4.0, 1/4.0, 1/4.0, 0.0});

		// b. Direction.LEFT (decisionContextSize = 3)
		// Chord 0
		// Note 0
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		// Note 1
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		// Note 2
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		// Note 3
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		// Chord 1
		// Note 4
		expected.add(new double[]{5.0, 1/4.0, 0.0, 0.0, -5.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		// Note 5
		expected.add(new double[]{0.0, 1/4.0, 0.0, 0.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		// Note 6
		expected.add(new double[]{3.0, 1/4.0, 0.0, 0.0, 3.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		// Note 7
		expected.add(new double[]{4.0, 1/4.0, 0.0, 0.0, 4.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		// Chord 2
		// Note 8
		expected.add(new double[]{3.0, 3/16.0, 0.0, 0.0, 3.0});
		expected.add(new double[]{2.0, 7/16.0, 3/16.0, 3/16.0, -2.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		// Chord 3
		// Note 9
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		// Note 10
		expected.add(new double[]{2.0, 1/16.0, 0.0, 0.0, 2.0});
		expected.add(new double[]{5.0, 1/4.0, 1/16.0, 1/16.0, 5.0});
		expected.add(new double[]{0.0, 1/2.0, 1/4.0, 1/4.0, 0.0});
		// Note 11
		expected.add(new double[]{2.0, 1/4.0, 1/16.0, 0.0, 2.0});
		expected.add(new double[]{2.0, 1/2.0, 1/4.0, 1/4.0, 2.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		// Note 12
		expected.add(new double[]{7.0, 1/4.0, 1/16.0, 0.0, -7.0});
		expected.add(new double[]{4.0, 1/2.0, 1/4.0, 1/4.0, -4.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{4.0, 1/4.0, 1/16.0, 1/8.0, -4.0});
		expected.add(new double[]{0.0, 1/2.0, 1/4.0, 1/4.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		// Chord 4
		// Note 13
		expected.add(new double[]{2.0, 1/8.0, 0.0, 0.0, -2.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		// Chord 5
		// Note 14
		expected.add(new double[]{0.0, 1/8.0, 0.0, 0.0, 0.0});
		expected.add(new double[]{2.0, 1/4.0, 1/8.0, 1/8.0, -2.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		// Note 15
		expected.add(new double[]{7.0, 1/4.0, 1/8.0, 0.0, 7.0});
		expected.add(new double[]{9.0, 5/16.0, 1/4.0, 1/4.0, 9.0});
		expected.add(new double[]{12.0, 1/2.0, 5/16.0, 5/16.0, 12.0});
		// Note 16
		expected.add(new double[]{2.0, 1/4.0, 1/8.0, 0.0, -2.0});
		expected.add(new double[]{0.0, 1/2.0, 5/16.0, 1/4.0, 0.0});
		expected.add(new double[]{0.0, 3/4.0, 1/2.0, 1/2.0, 0.0});
		// Note 17
		expected.add(new double[]{5.0, 1/4.0, 1/8.0, 1/8.0, -5.0});
		expected.add(new double[]{9.0, 1/2.0, 5/16.0, 3/8.0, -9.0});
		expected.add(new double[]{5.0, 3/4.0, 1/2.0, 1/2.0, -5.0});
		// Note 18
		expected.add(new double[]{4.0, 1/4.0, 1/8.0, 0.0, 4.0});
		expected.add(new double[]{3.0, 1/2.0, 5/16.0, 1/4.0, -3.0});
		expected.add(new double[]{0.0, 3/4.0, 1/2.0, 1/2.0, 0.0});
		// Chord 6
		// Note 19
		expected.add(new double[]{0.0, 1/4.0, 0.0, 0.0, 0.0});
		expected.add(new double[]{0.0, 3/8.0, 1/4.0, 1/4.0, 0.0});
		expected.add(new double[]{2.0, 1/2.0, 3/8.0, 3/8.0, -2.0});
		// Note 20
		expected.add(new double[]{3.0, 1/4.0, 0.0, 0.0, 3.0});
		expected.add(new double[]{1.0, 1/2.0, 3/8.0, 1/4.0, 1.0});
		expected.add(new double[]{3.0, 3/4.0, 9/16.0, 1/2.0, 3.0});
		// Note 21
		expected.add(new double[]{5.0, 1/4.0, 0.0, 0.0, -5.0});
		expected.add(new double[]{1.0, 1/2.0, 3/8.0, 1/4.0, -1.0});
		expected.add(new double[]{8.0, 3/4.0, 9/16.0, 1/2.0, -8.0});
		// Note 22
		expected.add(new double[]{9.0, 1/4.0, 0.0, 0.0, 9.0});
		expected.add(new double[]{4.0, 1/2.0, 3/8.0, 3/8.0, 4.0});
		expected.add(new double[]{0.0, 3/4.0, 9/16.0, 5/8.0, 0.0});
		// Chord 7
		// Note 23
		expected.add(new double[]{1.0, 1/8.0, 0.0, 0.0, -1.0});
		expected.add(new double[]{2.0, 3/8.0, 1/8.0, 1/8.0, 2.0});
		expected.add(new double[]{0.0, 5/8.0, 1/2.0, 3/8.0, 0.0});
		// Note 24
		expected.add(new double[]{4.0, 1/8.0, 0.0, 0.0, 4.0});
		expected.add(new double[]{1.0, 3/8.0, 1/8.0, 1/8.0, -1.0});
		expected.add(new double[]{3.0, 5/8.0, 1/2.0, 3/8.0, 3.0});
		// Chord 8
		// Note 25
		expected.add(new double[]{12.0, 1/2.0, 1/4.0, 0.0, -12.0});
		expected.add(new double[]{5.0, 3/4.0, 5/8.0, 1/2.0, -5.0});
		expected.add(new double[]{3.0, 13/16.0, 3/4.0, 3/4.0, -3.0});
		// Note 26
		expected.add(new double[]{2.0, 1/8.0, 0.0, 0.0, -2.0});
		expected.add(new double[]{3.0, 1/4.0, 1/8.0, 1/8.0, -3.0});
		expected.add(new double[]{0.0, 1/2.0, 1/4.0, 1/4.0, 0.0});
		// Note 27
		expected.add(new double[]{5.0, 1/4.0, 1/8.0, 0.0, -5.0});
		expected.add(new double[]{4.0, 1/2.0, 1/4.0, 1/4.0, 4.0});
		expected.add(new double[]{1.0, 3/4.0, 5/8.0, 5/8.0, -1.0});
		// Note 28
		expected.add(new double[]{1.0, 1/8.0, 0.0, 0.0, 1.0});
		expected.add(new double[]{5.0, 1/4.0, 1/8.0, 1/8.0, 5.0});
		expected.add(new double[]{0.0, 1/2.0, 1/4.0, 1/4.0, 0.0});
		// Chords 9-14
		// Note 29
		expected.add(new double[]{1.0, 1/16.0, 0.0, 0.0, -1.0});
		expected.add(new double[]{0.0, 3/16.0, 1/16.0, 1/16.0, 0.0});
		expected.add(new double[]{4.0, 5/16.0, 3/16.0, 3/16.0, 4.0});
		// Note 30
		expected.add(new double[]{1.0, 1/16.0, 0.0, 0.0, 1.0});
		expected.add(new double[]{0.0, 1/8.0, 1/16.0, 1/16.0, 0.0});
		expected.add(new double[]{1.0, 1/4.0, 1/8.0, 1/8.0, 1.0});
		// Note 31
		expected.add(new double[]{1.0, 1/32.0, 0.0, 0.0, -1.0});
		expected.add(new double[]{0.0, 3/32.0, 1/32.0, 1/32.0, 0.0});
		expected.add(new double[]{1.0, 5/32.0, 3/32.0, 3/32.0, -1.0});
		// Note 32
		expected.add(new double[]{2.0, 1/32.0, 0.0, 0.0, -2.0});
		expected.add(new double[]{3.0, 1/16.0, 1/32.0, 1/32.0, -3.0});
		expected.add(new double[]{2.0, 1/8.0, 1/16.0, 1/16.0, -2.0});
		// Note 33
		expected.add(new double[]{2.0, 1/32.0, 0.0, 0.0, 2.0});
		expected.add(new double[]{0.0, 1/16.0, 1/32.0, 1/32.0, 0.0});
		expected.add(new double[]{1.0, 3/32.0, 1/16.0, 1/16.0, -1.0});
		// Note 34
		expected.add(new double[]{1.0, 1/32.0, 0.0, 0.0, 1.0});
		expected.add(new double[]{3.0, 1/16.0, 1/32.0, 1/32.0, 3.0});
		expected.add(new double[]{1.0, 3/32.0, 1/16.0, 1/16.0, 1.0}); 
		// Chord 15
		// Note 35
		expected.add(new double[]{0.0, 3/4.0, 11/16.0, 1/4.0, 0.0});
		expected.add(new double[]{12.0, 5/4.0, 4/4.0, 3/4.0, -12.0});
		expected.add(new double[]{5.0, 6/4.0, 11/8.0, 5/4.0, -5.0});
		// Note 36
		expected.add(new double[]{0.0, 3/4.0, 11/16.0, 1/4.0, 0.0});
		expected.add(new double[]{2.0, 7/8.0, 3/4.0, 3/4.0, -2.0});
		expected.add(new double[]{3.0, 4/4.0, 7/8.0, 7/8.0, -3.0});
		// Note 37
		expected.add(new double[]{0.0, 3/4.0, 11/16.0, 1/4.0,0.0});
		expected.add(new double[]{5.0, 4/4.0, 7/8.0, 3/4.0, -5.0});
		expected.add(new double[]{4.0, 5/4.0, 4/4.0, 4/4.0, 4.0});
		// Note 38
		expected.add(new double[]{0.0, 1/2.0, 1/4.0, 1/4.0, 0.0});
		expected.add(new double[]{1.0, 17/32.0, 1/2.0, 1/2.0, 1.0});
		expected.add(new double[]{3.0, 9/16.0, 17/32.0, 17/32.0, 3.0});

		// c. Direction.RIGHT (decisionContextSize = 1)
		List<double[]> expectedRight = new ArrayList<double[]>();	
		// Chord 15
		expectedRight.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expectedRight.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expectedRight.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expectedRight.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		// Chords 14-9
		expectedRight.add(new double[]{0.0, -1/2.0, -1/4.0, -1/4.0, 0.0});
		expectedRight.add(new double[]{1.0, -1/32.0, 0.0, 0.0, -1.0});
		expectedRight.add(new double[]{2.0, -1/32.0, 0.0, 0.0, -2.0});
		expectedRight.add(new double[]{2.0, -1/32.0, 0.0, 0.0, 2.0});
		expectedRight.add(new double[]{1.0, -1/32.0, 0.0, 0.0, 1.0});
		expectedRight.add(new double[]{1.0, -1/16.0, 0.0, 0.0, -1.0});
		// Chord 8
		expectedRight.add(new double[]{0.0, -3/4.0, -11/16.0, -1/4.0, 0.0});
		expectedRight.add(new double[]{0.0, -3/4.0, -11/16.0, -1/4.0, 0.0});
		expectedRight.add(new double[]{0.0, -3/4.0, -11/16.0, -1/4.0, 0.0});
		expectedRight.add(new double[]{1.0, -1/16.0, 0.0, 0.0, 1.0});
		// Chord 7
		expectedRight.add(new double[]{2.0, -1/8.0, 0.0, 0.0, 2.0});
		expectedRight.add(new double[]{1.0, -1/8.0, 0.0, 0.0, -1.0});
		// Chord 6
		expectedRight.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expectedRight.add(new double[]{1.0, -1/8.0, 0.0, 0.0, 1.0});
		expectedRight.add(new double[]{4.0, -1/8.0, 0.0, 0.0, -4.0});
		expectedRight.add(new double[]{5.0, -1/4.0, -1/8.0, 0.0, 5.0});
		// Chord 5
		expectedRight.add(new double[]{0.0, -1/4.0, 0.0, 0.0, 0.0});
		expectedRight.add(new double[]{12.0, -1/2.0, -1/4.0, 0.0, 12.0});
		expectedRight.add(new double[]{3.0, -1/4.0, 0.0, 0.0, -3.0});
		expectedRight.add(new double[]{9.0, -1/4.0, 0.0, 0.0, -9.0});
		expectedRight.add(new double[]{5.0, -1/4.0, 0.0, 0.0, 5.0});
		// Chord 4
		expectedRight.add(new double[]{0.0, -1/8.0, 0.0, 0.0, 0.0});
		// Chord 3
		expectedRight.add(new double[]{2.0, -1/8.0, 0.0, 0.0, 2.0});
		expectedRight.add(new double[]{7.0, -1/4.0, -1/8.0, 0.0, -7.0});
		expectedRight.add(new double[]{2.0, -1/4.0, -1/8.0, 0.0, 2.0});
		expectedRight.add(new double[]{4.0, -1/4.0, -1/8.0, 0.0, -4.0});
		expectedRight.add(new double[]{5.0, -1/4.0, -1/8.0, 0.0, 5.0}); // full duration of currentNote = 1/4 because in the NoteSequence, the CoDNote with the longest duration is retained 
		// Chord 2                                                        
		expectedRight.add(new double[]{2.0, -1/16.0, 0.0, 0.0, -2.0});
		// Chord 1
		expectedRight.add(new double[]{3.0, -3/16.0, 0.0, 0.0, -3.0});
		expectedRight.add(new double[]{2.0, -1/4.0, -1/16.0, 0.0, -2.0});
		expectedRight.add(new double[]{7.0, -1/4.0, -1/16.0, 0.0, 7.0});
		expectedRight.add(new double[]{4.0, -1/4.0, -1/16.0, -1/8.0, 4.0});
		// Chord 0
		expectedRight.add(new double[]{5.0, -1/4.0, 0.0, 0.0, 5.0});
		expectedRight.add(new double[]{0.0, -1/4.0, 0.0, 0.0, 0.0});
		expectedRight.add(new double[]{4.0, -1/4.0, 0.0, 0.0, -4.0});
		expectedRight.add(new double[]{3.0, -1/4.0, 0.0, 0.0, -3.0});
		expected.addAll(expectedRight);
		
		// d. Direction.LEFT (decisionContextSize = 3, averaged)
		List<double[]> expectedAvg = new ArrayList<double[]>();	
		// Chord 0
		expectedAvg.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expectedAvg.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expectedAvg.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		expectedAvg.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		// Chord 1
		expectedAvg.add(new double[]{1.0/(5+1), 1.0/((1/4.0)+1), 1.0/(0+1), 1.0/(0+1), -5.0});
		expectedAvg.add(new double[]{1.0/(0+1), 1.0/((1/4.0)+1), 1.0/(0+1), 1.0/(0+1), 0.0});
		expectedAvg.add(new double[]{1.0/(3+1), 1.0/((1/4.0)+1), 1.0/(0+1), 1.0/(0+1), 3.0});
		expectedAvg.add(new double[]{1.0/(4+1), 1.0/((1/4.0)+1), 1.0/(0+1), 1.0/(0+1), 4.0});
		// Chord 5
		expectedAvg.add(new double[]{
			( (1.0/(0+1)) + (1.0/(2+1)) ) / 2.0, 
			( (1.0/((1/8.0)+1)) + (1.0/((1/4.0)+1)) ) / 2.0,
			( (1.0/(0+1)) + (1.0/((1/8.0)+1)) ) / 2.0,
			( (1.0/(0+1)) + (1.0/((1/8.0)+1)) ) / 2.0,
			( (0 + -2) ) / 2.0
		});
		expectedAvg.add(new double[]{ 
			( (1.0/(7+1)) + (1.0/(9+1)) + (1.0/(12+1)) ) / 3.0, 
			( (1.0/((1/4.0)+1)) + (1.0/((5/16.0)+1)) + (1.0/((2/4.0)+1)) ) / 3.0,
			( (1.0/((1/8.0)+1)) + (1.0/((1/4.0)+1)) + (1.0/((5/16.0)+1)) ) / 3.0,
			( (1.0/(0+1)) + (1.0/((1/4.0)+1)) + (1.0/((5/16.0)+1)) ) / 3.0,
			( (7 + 9 + 12) ) / 3.0
		});
		expectedAvg.add(new double[]{ 
			( (1.0/(2+1)) + (1.0/(0+1)) + (1.0/(0+1)) ) / 3.0, 
			( (1.0/((1/4.0)+1)) + (1.0/((2/4.0)+1)) + (1.0/((3/4.0)+1)) ) / 3.0,
			( (1.0/((1/8.0)+1)) + (1.0/((5/16.0)+1)) + (1.0/((2/4.0)+1)) ) / 3.0,
			( (1.0/(0+1)) + (1.0/((1/4.0)+1)) + (1.0/((2/4.0)+1)) ) / 3.0,
			( (-2 + 0 + 0) ) / 3.0
		});
		expectedAvg.add(new double[]{ 
			( (1.0/(5+1)) + (1.0/(9+1)) + (1.0/(5+1)) ) / 3.0, 
			( (1.0/((1/4.0)+1)) + (1.0/((2/4.0)+1)) + (1.0/((3/4.0)+1)) ) / 3.0,
			( (1.0/((1/8.0)+1)) + (1.0/((5/16.0)+1)) + (1.0/((2/4.0)+1)) ) / 3.0,
			( (1.0/((1/8.0)+1)) + (1.0/((3/8.0)+1)) + (1.0/((2/4.0)+1)) ) / 3.0,
			( (-5 + -9 + -5) ) / 3.0
		});
		expectedAvg.add(new double[]{
			( (1.0/(4+1)) + (1.0/(3+1)) + (1.0/(0+1)) ) / 3.0, 
			( (1.0/((1/4.0)+1)) + (1.0/((2/4.0)+1)) + (1.0/((3/4.0)+1)) ) / 3.0,
			( (1.0/((1/8.0)+1)) + (1.0/((5/16.0)+1)) + (1.0/((2/4.0)+1)) ) / 3.0,
			( (1.0/(0+1)) + (1.0/((1/4.0)+1)) + (1.0/((2/4.0)+1)) ) / 3.0,
			( (4 + -3 + 0) ) / 3.0
		});
		expected.addAll(expectedAvg);

		// For each element of non-averaged expected: turn the first four elements from 
		// distances into proximities
		for (int i = 0; i < (expected.size() - expectedAvg.size()); i++) {
			double[] currentArray = expected.get(i);
			for (int j = 0; j < currentArray.length - 1; j++) {
				double oldValue = currentArray[j]; 
				// Do only if oldValue is not -1.0, i.e., if the voice is active
				if (oldValue != -1.0) {
					double newValue = 1.0/(oldValue + 1);
					// If oldValue is negative
					if (oldValue < 0) {
						newValue = -(1.0/(-oldValue + 1));
					}
					currentArray[j] = newValue;
				}
			}
		}

		List<double[]> actual = new ArrayList<double[]>();
		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
		List<List<Double>> voiceLabels = transcription.getVoiceLabels();
		NoteSequence noteSeq = transcription.getNoteSequence();
		// a. Direction.LEFT (decisionContextSize = 1)
		for (int i = 0; i < basicTabSymbolProperties.length; i++) {
			Note currentNote = noteSeq.get(i);
			List<Double> currentLabel = voiceLabels.get(i);
			List<Integer> currentVoices = 
				DataConverter.convertIntoListOfVoices(currentLabel);
			// For each voice (in case of a CoD, the highest, i.e., the voice with the lowest number, will be dealt with first)
			for (int j = 0; j < currentVoices.size(); j++) {
				int currentVoice = currentVoices.get(j);
				NotationVoice currentVoiceToCompareTo = transcription.getPiece().getScore().get(currentVoice).get(0);
				actual.addAll(FeatureGenerator.getProximitiesAndMovementToVoiceAll(
					basicTabSymbolProperties, currentVoiceToCompareTo, currentNote, 
					Direction.LEFT, 1, false));
			}
		}
		// b. Direction.LEFT (decisionContextSize = 3)
		for (int i = 0; i < basicTabSymbolProperties.length; i++) {
			Note currentNote = noteSeq.get(i);
			List<Double> currentLabel = voiceLabels.get(i);
			List<Integer> currentVoices = 
				DataConverter.convertIntoListOfVoices(currentLabel);
			// For each voice (in case of a CoD, the highest, i.e., the voice with the lowest number, will be dealt with first)
			for (int j = 0; j < currentVoices.size(); j++) {
				int currentVoice = currentVoices.get(j);
				NotationVoice currentVoiceToCompareTo = transcription.getPiece().getScore().get(currentVoice).get(0);
				actual.addAll(FeatureGenerator.getProximitiesAndMovementToVoiceAll(
					basicTabSymbolProperties, currentVoiceToCompareTo, currentNote, 
					Direction.LEFT, 3, false));
			}
		}
		// c. Direction.RIGHT (decisionContextSize = 1)
		List<Integer> backwardsMapping = FeatureGenerator.getBackwardsMapping(tablature.getNumberOfNotesPerChord());
		for (int i : backwardsMapping) {
			Note currentNote = noteSeq.get(i);
			List<Double> currentLabel = voiceLabels.get(i);
			List<Integer> currentVoices = DataConverter.convertIntoListOfVoices(currentLabel);
			// For each voice (in case of a CoD, the highest, i.e., the voice with the lowest number, will be dealt with first)
			for (int j = 0; j < currentVoices.size(); j++) {
				int currentVoice = currentVoices.get(j);
				NotationVoice currentVoiceToCompareTo = 
					transcription.getPiece().getScore().get(currentVoice).get(0);
				actual.addAll(FeatureGenerator.getProximitiesAndMovementToVoiceAll(
					basicTabSymbolProperties, currentVoiceToCompareTo, 
					currentNote, Direction.RIGHT, 1, false));
			}
		}
		// d. Direction.LEFT (decisionContextSize = 3, averaged)
		for (int i : Arrays.asList(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 14, 15, 16, 17, 18})) {
			Note currentNote = noteSeq.getNoteAt(i);
			List<Double> currentLabel = voiceLabels.get(i);
			List<Integer> currentVoices = 
				DataConverter.convertIntoListOfVoices(currentLabel);
			// For each voice (in case of a CoD, the highest, i.e., the voice with the lowest number, will be dealt with first)
			for (int j = 0; j < currentVoices.size(); j++) {
				int currentVoice = currentVoices.get(j);
				NotationVoice currentVoiceToCompareTo = transcription.getPiece().getScore().get(currentVoice).get(0);
				actual.addAll(FeatureGenerator.getProximitiesAndMovementToVoiceAll(basicTabSymbolProperties, 
					currentVoiceToCompareTo, currentNote, Direction.LEFT, 3, true));
			}
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j]);
			}
		}
	}


	public void testGetProximitiesAndMovementToVoiceAllNonTab() {    
		Transcription transcription = new Transcription(midiTestpiece1, null);
		
		List<double[]> expected = new ArrayList<double[]>();
		// a. Direction.LEFT (decisionContextSize = 1)
		// Chord 0
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Chord 1
		expected.add(new double[]{5.0, 1/4.0, 0.0, -5.0});
		expected.add(new double[]{0.0, 1/4.0, 0.0, 0.0});
		expected.add(new double[]{4.0, 1/4.0, 0.0, 4.0});
		expected.add(new double[]{3.0, 1/4.0, 0.0, 3.0});
		// Chord 2
		expected.add(new double[]{3.0, 3/16.0, 0.0, 3.0});
		// Chord 3
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{2.0, 1/16.0, 0.0, 2.0});
		expected.add(new double[]{2.0, 1/4.0, 0.0, 2.0});
		expected.add(new double[]{7.0, 1/4.0, 0.0, -7.0});
		expected.add(new double[]{4.0, 1/4.0, 1/8.0, -4.0});
		// Chord 4
		expected.add(new double[]{2.0, 1/8.0, 0.0, -2.0});
		// Chord 5
		expected.add(new double[]{0.0, 1/8.0, 0.0, 0.0});
		expected.add(new double[]{7.0, 1/4.0, 0.0, 7.0});
		expected.add(new double[]{2.0, 1/4.0, 0.0, -2.0});
		expected.add(new double[]{5.0, 1/4.0, 1/8.0, -5.0});
		expected.add(new double[]{4.0, 1/4.0, 0.0, 4.0});
		// Chord 6
		expected.add(new double[]{0.0, 1/4.0, 0.0, 0.0});
		expected.add(new double[]{3.0, 1/4.0, 0.0, 3.0});
		expected.add(new double[]{5.0, 1/4.0, 0.0, -5.0});
		expected.add(new double[]{9.0, 1/4.0, 0.0, 9.0});
		// Chord 7
		expected.add(new double[]{1.0, 1/8.0, 0.0, -1.0});
		expected.add(new double[]{4.0, 1/8.0, 0.0, 4.0});
		// Chord 8
		expected.add(new double[]{12.0, 1/2.0, 0.0, -12.0});
		expected.add(new double[]{2.0, 1/8.0, 0.0, -2.0});
		expected.add(new double[]{5.0, 1/4.0, 0.0, -5.0});
		expected.add(new double[]{1.0, 1/8.0, 0.0, 1.0});
		// Chords 9-14
		expected.add(new double[]{1.0, 1/16.0, 0.0, -1.0});
		expected.add(new double[]{1.0, 1/16.0, 0.0, 1.0});
		expected.add(new double[]{1.0, 1/32.0, 0.0, -1.0});
		expected.add(new double[]{2.0, 1/32.0, 0.0, -2.0});
		expected.add(new double[]{2.0, 1/32.0, 0.0, 2.0});
		expected.add(new double[]{1.0, 1/32.0, 0.0, 1.0});
		// Chord 15
		expected.add(new double[]{0.0, 3/4.0, 1/4.0, 0.0});
		expected.add(new double[]{0.0, 3/4.0, 1/4.0, 0.0});
		expected.add(new double[]{0.0, 3/4.0, 1/4.0, 0.0});
		expected.add(new double[]{0.0, 2/4.0, 1/4.0, 0.0});

		// b. Direction.LEFT (decisionContextSize = 3)
		// Chord 0
		// Note 0
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Note 1
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Note 2
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Note 3
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Chord 1
		// Note 4
		expected.add(new double[]{5.0, 1/4.0, 0.0, -5.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Note 5
		expected.add(new double[]{0.0, 1/4.0, 0.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Note 6
		expected.add(new double[]{4.0, 1/4.0, 0.0, 4.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Note 7
		expected.add(new double[]{3.0, 1/4.0, 0.0, 3.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Chord 2
		// Note 8
		expected.add(new double[]{3.0, 3/16.0, 0.0, 3.0}); 
		expected.add(new double[]{2.0, 7/16.0, 3/16.0, -2.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Chord 3
		// Note 9
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0}); 
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Note 10
		expected.add(new double[]{2.0, 1/16.0, 0.0, 2.0});
		expected.add(new double[]{5.0, 1/4.0, 1/16.0, 5.0});
		expected.add(new double[]{0.0, 1/2.0, 1/4.0, 0.0});
		// Note 11
		expected.add(new double[]{2.0, 1/4.0, 0.0, 2.0});
		expected.add(new double[]{2.0, 1/2.0, 1/4.0, 2.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Note 12
		expected.add(new double[]{7.0, 1/4.0, 0.0, -7.0});
		expected.add(new double[]{4.0, 1/2.0, 1/4.0, -4.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Note 13
		expected.add(new double[]{4.0, 1/4.0, 1/8.0, -4.0});
		expected.add(new double[]{0.0, 1/2.0, 1/4.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Chord 4
		// Note 14
		expected.add(new double[]{2.0, 1/8.0, 0.0, -2.0}); 
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Chord 5
		// Note 15
		expected.add(new double[]{0.0, 1/8.0, 0.0, 0.0}); 
		expected.add(new double[]{2.0, 1/4.0, 1/8.0, -2.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Note 16
		expected.add(new double[]{7.0, 1/4.0, 0.0, 7.0});
		expected.add(new double[]{9.0, 5/16.0, 1/4.0, 9.0});
		expected.add(new double[]{12.0, 1/2.0, 5/16.0, 12.0});
		// Note 17
		expected.add(new double[]{2.0, 1/4.0, 0.0, -2.0});
		expected.add(new double[]{0.0, 1/2.0, 1/4.0, 0.0});
		expected.add(new double[]{0.0, 3/4.0, 1/2.0, 0.0});
		// Note 18
		expected.add(new double[]{5.0, 1/4.0, 1/8.0, -5.0});
		expected.add(new double[]{9.0, 1/2.0, 3/8.0, -9.0});
		expected.add(new double[]{5.0, 3/4.0, 1/2.0, -5.0});
		// Note 19
		expected.add(new double[]{4.0, 1/4.0, 0.0, 4.0});
		expected.add(new double[]{3.0, 1/2.0, 1/4.0, -3.0});
		expected.add(new double[]{0.0, 3/4.0, 1/2.0, 0.0});
		// Chord 6
		// Note 20
		expected.add(new double[]{0.0, 1/4.0, 0.0, 0.0}); 
		expected.add(new double[]{0.0, 3/8.0, 1/4.0, 0.0});
		expected.add(new double[]{2.0, 1/2.0, 3/8.0, -2.0});
		// Note 21
		expected.add(new double[]{3.0, 1/4.0, 0.0, 3.0});
		expected.add(new double[]{1.0, 1/2.0, 1/4.0, 1.0});
		expected.add(new double[]{3.0, 3/4.0, 2/4.0, 3.0});
		// Note 22
		expected.add(new double[]{5.0, 1/4.0, 0.0, -5.0});
		expected.add(new double[]{1.0, 1/2.0, 1/4.0, -1.0});
		expected.add(new double[]{8.0, 3/4.0, 1/2.0, -8.0});
		// Note 23
		expected.add(new double[]{9.0, 1/4.0, 0.0, 9.0});
		expected.add(new double[]{4.0, 1/2.0, 3/8.0, 4.0});
		expected.add(new double[]{0.0, 3/4.0, 5/8.0, 0.0});
		// Chord 7
		// Note 24
		expected.add(new double[]{1.0, 1/8.0, 0.0, -1.0}); 
		expected.add(new double[]{2.0, 3/8.0, 1/8.0, 2.0});
		expected.add(new double[]{0.0, 5/8.0, 3/8.0, 0.0});
		// Note 25
		expected.add(new double[]{4.0, 1/8.0, 0.0, 4.0});
		expected.add(new double[]{1.0, 3/8.0, 1/8.0, -1.0});
		expected.add(new double[]{3.0, 5/8.0, 3/8.0, 3.0});
		// Chord 8
		// Note 26
		expected.add(new double[]{12.0, 1/2.0, 0.0, -12.0}); 
		expected.add(new double[]{5.0, 3/4.0, 1/2.0, -5.0});
		expected.add(new double[]{3.0, 13/16.0, 3/4.0, -3.0});
		// Note 27
		expected.add(new double[]{2.0, 1/8.0, 0.0, -2.0});
		expected.add(new double[]{3.0, 1/4.0, 1/8.0, -3.0});
		expected.add(new double[]{0.0, 1/2.0, 1/4.0, 0.0});
		// Note 28
		expected.add(new double[]{5.0, 1/4.0, 0.0, -5.0});
		expected.add(new double[]{4.0, 1/2.0, 1/4.0, 4.0});
		expected.add(new double[]{1.0, 3/4.0, 5/8.0, -1.0});
		// Note 29
		expected.add(new double[]{1.0, 1/8.0, 0.0, 1.0});
		expected.add(new double[]{5.0, 1/4.0, 1/8.0, 5.0});
		expected.add(new double[]{0.0, 1/2.0, 1/4.0, 0.0});
		// Chords 9-14
		// Note 30-35
		expected.add(new double[]{1.0, 1/16.0, 0.0, -1.0}); 
		expected.add(new double[]{0.0, 3/16.0, 1/16.0, 0.0});
		expected.add(new double[]{4.0, 5/16.0, 3/16.0, 4.0});
		//
		expected.add(new double[]{1.0, 1/16.0, 0.0, 1.0});
		expected.add(new double[]{0.0, 1/8.0, 1/16.0, 0.0});
		expected.add(new double[]{1.0, 1/4.0, 1/8.0, 1.0});
		// 
		expected.add(new double[]{1.0, 1/32.0, 0.0, -1.0});
		expected.add(new double[]{0.0, 3/32.0, 1/32.0, 0.0});
		expected.add(new double[]{1.0, 5/32.0, 3/32.0, -1.0});
		// 
		expected.add(new double[]{2.0, 1/32.0, 0.0, -2.0});
		expected.add(new double[]{3.0, 1/16.0, 1/32.0, -3.0});
		expected.add(new double[]{2.0, 2/16.0, 1/16.0, -2.0});
		// 
		expected.add(new double[]{2.0, 1/32.0, 0.0, 2.0});
		expected.add(new double[]{0.0, 1/16.0, 1/32.0, 0.0});
		expected.add(new double[]{1.0, 3/32.0, 2/32.0, -1.0});
		// 
		expected.add(new double[]{1.0, 1/32.0, 0.0, 1.0});
		expected.add(new double[]{3.0, 1/16.0, 1/32.0, 3.0});
		expected.add(new double[]{1.0, 3/32.0, 2/32.0, 1.0});
		// Chord 15
		// Note 36
		expected.add(new double[]{0.0, 3/4.0, 1/4.0, 0.0});
		expected.add(new double[]{12.0, 5/4.0, 3/4.0, -12.0});
		expected.add(new double[]{5.0, 6/4.0, 5/4.0, -5.0});
		// Note 37
		expected.add(new double[]{0.0, 3/4.0, 1/4.0, 0.0});
		expected.add(new double[]{2.0, 7/8.0, 3/4.0, -2.0});
		expected.add(new double[]{3.0, 4/4.0, 7/8.0, -3.0});
		// Note 38
		expected.add(new double[]{0.0, 3/4.0, 1/4.0, 0.0});
		expected.add(new double[]{5.0, 4/4.0, 3/4.0, -5.0});
		expected.add(new double[]{4.0, 5/4.0, 4/4.0, 4.0});
		// Note 39
		expected.add(new double[]{0.0, 2/4.0, 1/4.0, 0.0});
		expected.add(new double[]{1.0, 17/32.0, 1/2.0, 1.0});
		expected.add(new double[]{3.0, 9/16.0, 17/32.0, 3.0});
				
		// c. Direction.RIGHT (decisionContextSize = 1)
		List<double[]> expectedRight = new ArrayList<double[]>();	
		// Chord 15
		expectedRight.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expectedRight.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expectedRight.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expectedRight.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Chords 14-9
		expectedRight.add(new double[]{0.0, -1/2.0, -1/4.0, 0.0});
		expectedRight.add(new double[]{1.0, -1/32.0, 0.0, -1.0});
		expectedRight.add(new double[]{2.0, -1/32.0, 0.0, -2.0});
		expectedRight.add(new double[]{2.0, -1/32.0, 0.0, 2.0});
		expectedRight.add(new double[]{1.0, -1/32.0, 0.0, 1.0});
		expectedRight.add(new double[]{1.0, -1/16.0, 0.0, -1.0});
		// Chord 8
		expectedRight.add(new double[]{0.0, -3/4.0, -1/4.0, 0.0});
		expectedRight.add(new double[]{0.0, -3/4.0, -1/4.0, 0.0});
		expectedRight.add(new double[]{0.0, -3/4.0, -1/4.0, 0.0});
		expectedRight.add(new double[]{1.0, -1/16.0, 0.0, 1.0});
		// Chord 7
		expectedRight.add(new double[]{2.0, -1/8.0, 0.0, 2.0});
		expectedRight.add(new double[]{1.0, -1/8.0, 0.0, -1.0});
		// Chord 6
		expectedRight.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expectedRight.add(new double[]{1.0, -1/8.0, 0.0, 1.0});
		expectedRight.add(new double[]{4.0, -1/8.0, 0.0, -4.0});
		expectedRight.add(new double[]{5.0, -1/4.0, 0.0, 5.0});
		// Chord 5
		expectedRight.add(new double[]{0.0, -1/4.0, 0.0, 0.0});
		expectedRight.add(new double[]{12.0, -1/2.0, 0.0, 12.0});
		expectedRight.add(new double[]{3.0, -1/4.0, 0.0, -3.0});
		expectedRight.add(new double[]{9.0, -1/4.0, 0.0, -9.0});
		expectedRight.add(new double[]{5.0, -1/4.0, 0.0, 5.0});
		// Chord 4
		expectedRight.add(new double[]{0.0, -1/8.0, 0.0, 0.0});
		// Chord 3
		expectedRight.add(new double[]{2.0, -1/8.0, 0.0, 2.0});
		expectedRight.add(new double[]{7.0, -1/4.0, 0.0, -7.0});
		expectedRight.add(new double[]{2.0, -1/4.0, 0.0, 2.0});
		expectedRight.add(new double[]{4.0, -1/4.0, 0.0, -4.0}); // in the NoteSequence, the Note with the longest duration comes first
		expectedRight.add(new double[]{5.0, -1/4.0, -1/8.0, 5.0}); 
		// Chord 2                                                        
		expectedRight.add(new double[]{2.0, -1/16.0, 0.0, -2.0});
		// Chord 1
		expectedRight.add(new double[]{3.0, -3/16.0, 0.0, -3.0});
		expectedRight.add(new double[]{2.0, -1/4.0, 0.0, -2.0});
		expectedRight.add(new double[]{4.0, -1/4.0, -1/8.0, 4.0});
		expectedRight.add(new double[]{7.0, -1/4.0, 0.0, 7.0});
		// Chord 0
		expectedRight.add(new double[]{5.0, -1/4.0, 0.0, 5.0});
		expectedRight.add(new double[]{0.0, -1/4.0, 0.0, 0.0});
		expectedRight.add(new double[]{4.0, -1/4.0, 0.0, -4.0});
		expectedRight.add(new double[]{3.0, -1/4.0, 0.0, -3.0});
		expected.addAll(expectedRight);

		// d. Direction.LEFT (decisionContextSize = 3, averaged)
		List<double[]> expectedAvg = new ArrayList<double[]>();	
		// Chord 0
		expectedAvg.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expectedAvg.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expectedAvg.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		expectedAvg.add(new double[]{-1.0, -1.0, -1.0, 0.0});
		// Chord 1
		expectedAvg.add(new double[]{1.0/(5+1), 1.0/((1/4.0)+1), 1.0/(0+1), -5.0});
		expectedAvg.add(new double[]{1.0/(0+1), 1.0/((1/4.0)+1), 1.0/(0+1), 0.0});
		expectedAvg.add(new double[]{1.0/(4+1), 1.0/((1/4.0)+1), 1.0/(0+1), 4.0});
		expectedAvg.add(new double[]{1.0/(3+1), 1.0/((1/4.0)+1), 1.0/(0+1), 3.0});
		// Chord 5
		expectedAvg.add(new double[]{
			( (1.0/(0+1)) + (1.0/(2+1)) ) / 2.0, 
			( (1.0/((1/8.0)+1)) + (1.0/((1/4.0)+1)) ) / 2.0,
			( (1.0/(0+1)) + (1.0/((1/8.0)+1)) ) / 2.0,
			( (0 + -2) ) / 2.0
		});
		expectedAvg.add(new double[]{ 
			( (1.0/(7+1)) + (1.0/(9+1)) + (1.0/(12+1)) ) / 3.0, 
			( (1.0/((1/4.0)+1)) + (1.0/((5/16.0)+1)) + (1.0/((2/4.0)+1)) ) / 3.0,
			( (1.0/(0+1)) + (1.0/((1/4.0)+1)) + (1.0/((5/16.0)+1)) ) / 3.0,
			( (7 + 9 + 12) ) / 3.0
		});
		expectedAvg.add(new double[]{ 
			( (1.0/(2+1)) + (1.0/(0+1)) + (1.0/(0+1)) ) / 3.0, 
			( (1.0/((1/4.0)+1)) + (1.0/((2/4.0)+1)) + (1.0/((3/4.0)+1)) ) / 3.0,
			( (1.0/(0+1)) + (1.0/((1/4.0)+1)) + (1.0/((2/4.0)+1)) ) / 3.0,
			( (-2 + 0 + 0) ) / 3.0
		});
		expectedAvg.add(new double[]{ 
			( (1.0/(5+1)) + (1.0/(9+1)) + (1.0/(5+1)) ) / 3.0, 
			( (1.0/((1/4.0)+1)) + (1.0/((2/4.0)+1)) + (1.0/((3/4.0)+1)) ) / 3.0,
			( (1.0/((1/8.0)+1)) + (1.0/((3/8.0)+1)) + (1.0/((2/4.0)+1)) ) / 3.0,
			( (-5 + -9 + -5) ) / 3.0
		});
		expectedAvg.add(new double[]{
			( (1.0/(4+1)) + (1.0/(3+1)) + (1.0/(0+1)) ) / 3.0, 
			( (1.0/((1/4.0)+1)) + (1.0/((2/4.0)+1)) + (1.0/((3/4.0)+1)) ) / 3.0,
			( (1.0/(0+1)) + (1.0/((1/4.0)+1)) + (1.0/((2/4.0)+1)) ) / 3.0,
			( (4 + -3 + 0) ) / 3.0
		});
		expected.addAll(expectedAvg);

		// For each element of non-averaged expected: turn the first three elements from 
		// distances into proximities
		for (int i = 0; i < (expected.size() - expectedAvg.size()); i++) {
			double[] currentArray = expected.get(i);
			for (int j = 0; j < currentArray.length - 1; j++) {
				double oldValue = currentArray[j]; 
				// Do only if oldValue is not -1.0, i.e., if the voice is active
				if (oldValue != -1.0) {
					double newValue = 1.0/(oldValue + 1);
					// If oldValue is negative
					if (oldValue < 0) {
						newValue = -(1.0/(-oldValue + 1));
					}
					currentArray[j] = newValue;
				}
			}
		}

		List<double[]> actual = new ArrayList<double[]>();
		Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
		List<List<Double>> voiceLabels = transcription.getVoiceLabels();
		NoteSequence noteSeq = transcription.getNoteSequence();
		// a. Direction.LEFT (decisionContextSize = 1)
		for (int i = 0; i < basicNoteProperties.length; i++) {
			Note currentNote = noteSeq.getNoteAt(i);
			List<Double> currentLabel = voiceLabels.get(i);
			List<Integer> currentVoices = 
				DataConverter.convertIntoListOfVoices(currentLabel);
			// For each voice assigned to the Note
			// NB: currentVoices.size() will always be 1, as CoDs do not occur in the non-tablature case
			for (int j = 0; j < currentVoices.size(); j++) {
				int currentVoice = currentVoices.get(j);
				NotationVoice currentVoiceToCompareTo = transcription.getPiece().getScore().get(currentVoice).get(0);
				actual.addAll(FeatureGenerator.getProximitiesAndMovementToVoiceAll(null, 
					currentVoiceToCompareTo, currentNote, Direction.LEFT, 1, false));
			}
		}
		// b. Direction.LEFT (decisionContextSize = 3)
		for (int i = 0; i < basicNoteProperties.length; i++) {
			Note currentNote = noteSeq.getNoteAt(i);
			List<Double> currentLabel = voiceLabels.get(i);
			List<Integer> currentVoices = 
				DataConverter.convertIntoListOfVoices(currentLabel);
			// For each voice assigned to the Note
			// NB: currentVoices.size() will always be 1, as CoDs do not occur in the non-tablature case
			for (int j = 0; j < currentVoices.size(); j++) {
				int currentVoice = currentVoices.get(j);
				NotationVoice currentVoiceToCompareTo = transcription.getPiece().getScore().get(currentVoice).get(0);
				actual.addAll(FeatureGenerator.getProximitiesAndMovementToVoiceAll(null, 
					currentVoiceToCompareTo, currentNote, Direction.LEFT, 3, false));
			}
		}
		// c. Direction.RIGHT (decisionContextSize = 1)
		List<Integer> backwardsMapping = FeatureGenerator.getBackwardsMapping(transcription.getNumberOfNewNotesPerChord());
		for (int i : backwardsMapping) {
			Note currentNote = noteSeq.getNoteAt(i);
			List<Double> currentLabel = voiceLabels.get(i);
			List<Integer> currentVoices = 
				DataConverter.convertIntoListOfVoices(currentLabel);
			// For each voice assigned to the Note
			// NB: currentVoices.size() will always be 1, as CoDs do not occur in the non-tablature case
			for (int j = 0; j < currentVoices.size(); j++) {
				int currentVoice = currentVoices.get(j);
				NotationVoice currentVoiceToCompareTo = transcription.getPiece().getScore().get(currentVoice).get(0);
				actual.addAll(FeatureGenerator.getProximitiesAndMovementToVoiceAll(null, 
					currentVoiceToCompareTo, currentNote, Direction.RIGHT, 1, false));
			}
		}
		// d. Direction.LEFT (decisionContextSize = 3, averaged)
		for (int i : Arrays.asList(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 15, 16, 17, 18, 19})) {
			Note currentNote = noteSeq.getNoteAt(i);
			List<Double> currentLabel = voiceLabels.get(i);
			List<Integer> currentVoices = 
				DataConverter.convertIntoListOfVoices(currentLabel);
			// For each voice assigned to the Note
			// NB: currentVoices.size() will always be 1, as CoDs do not occur in the non-tablature case
			for (int j = 0; j < currentVoices.size(); j++) {
				int currentVoice = currentVoices.get(j);
				NotationVoice currentVoiceToCompareTo = transcription.getPiece().getScore().get(currentVoice).get(0);
				actual.addAll(FeatureGenerator.getProximitiesAndMovementToVoiceAll(null, 
					currentVoiceToCompareTo, currentNote, Direction.LEFT, 3, true));
			}
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j]);
			}
		}
	}


	public void testCalculateProximity() {
	  List<Double> distances = Arrays.asList(new Double[]{499.0, 4.0, 2.0, 1.0, 3/4.0, 1/2.0, 1/4.0,	1/8.0, 
	  	1/16.0, 1/32.0, 0.0, -1/32.0, -1/16.0, -1/8.0, -1/4.0, -1/2.0, -3/4.0, -1.0, -2.0, -4.0, -499.0});
		
		// Determine expected
	  List<Double> expected = Arrays.asList(new Double[]{0.002, 0.2, 1/3.0, 0.5, 4/7.0, 2/3.0, 0.8,	8/9.0, 
		  16/17.0, 32/33.0, 1.0, -32/33.0, -16/17.0, -8/9.0, -0.8, -2/3.0, -4/7.0, -0.5, -1/3.0, -0.2, -0.002});

	  // Calculate actual
	  List<Double> actual = new ArrayList<Double>();
	  for (double d : distances) {
	  	actual.add(FeatureGenerator.calculateProximity(d));
	  }
	  
	  // Assert equality
	  assertEquals(expected.size(), actual.size());
	  for (int i = 0; i < expected.size(); i++) {
	  	assertEquals(expected.get(i), actual.get(i));
	  }
	}
	
	
	public void testGetVoicesAlreadyOccupied() {
		// a. Unadapted (longer CoDnote in the upper voice)
		Tablature tablatureA = new Tablature(encodingTestpiece1, true);
  	Transcription transcriptionA = new Transcription(midiTestpiece1, encodingTestpiece1);
  	List<Integer[]> voicesCoDNotesA = transcriptionA.getVoicesCoDNotes();
  	List<List<Double>> durationLabelsA = transcriptionA.getDurationLabels();
    // b. Longer CoDNote in the lower voice
  	Tablature tablatureB = new Tablature(encodingTestpiece1, true);
  	Transcription transcriptionB = new Transcription(midiTestpiece1, encodingTestpiece1);
  	List<Integer[]> voicesCoDNotesB = transcriptionB.getVoicesCoDNotes();
  	voicesCoDNotesB.set(12, new Integer[]{1, 0});
   	List<List<Double>> durationLabelsB = transcriptionB.getDurationLabels();
   	// c. CoDNotes of equal duration (quarter)
  	Tablature tablatureC = new Tablature(encodingTestpiece1, true);
  	Transcription transcriptionC = new Transcription(midiTestpiece1, encodingTestpiece1);
  	List<Integer[]> voicesCoDNotesC = transcriptionC.getVoicesCoDNotes();
   	List<List<Double>> durationLabelsC = transcriptionC.getDurationLabels();
   	List<Double> durLab12C = Transcription.QUARTER;
   	durationLabelsC.set(12, durLab12C);
    // d. CoDNotes of equal duration (eighth)
  	Tablature tablatureD = new Tablature(encodingTestpiece1, true);
  	Transcription transcriptionD = new Transcription(midiTestpiece1, encodingTestpiece1);
  	List<Integer[]> voicesCoDNotesD = transcriptionD.getVoicesCoDNotes();
   	List<List<Double>> durationLabelsD = transcriptionD.getDurationLabels();
   	List<Double> durLab12D = Transcription.EIGHTH;
   	durationLabelsD.set(12, durLab12D);
   		
   	List<Tablature> allTablatures = Arrays.asList(new Tablature[]{tablatureA, tablatureB,	tablatureC,	tablatureD});
  	List<Transcription> allTranscriptions = Arrays.asList(new Transcription[]{transcriptionA, transcriptionB,	
  		transcriptionC, transcriptionD});
  	List<List<Integer[]>> allVoicesCoDNotes = new ArrayList<List<Integer[]>>();
  	allVoicesCoDNotes.add(voicesCoDNotesA); allVoicesCoDNotes.add(voicesCoDNotesB); 
  	allVoicesCoDNotes.add(voicesCoDNotesC); allVoicesCoDNotes.add(voicesCoDNotesD); 
  	List<List<List<Double>>> allDurationLabels = new ArrayList<List<List<Double>>>();
  	allDurationLabels.add(durationLabelsA); allDurationLabels.add(durationLabelsB); 
  	allDurationLabels.add(durationLabelsC); allDurationLabels.add(durationLabelsD);
  	
    // Determine expected
  	List<double[]> expected = new ArrayList<double[]>();
  	// a. Direction.LEFT
  	// Not modelling duration 
  	// a. Unadapted (longer CoDnote in the upper voice)
  	List<double[]> expectedANoDur = new ArrayList<double[]>();
  	// Chord 0
  	expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    // Chord 1
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expectedANoDur.add(new double[]{1.0, 0.0, 1.0, 1.0, 0.0});
    // Chord 2
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    // Chord 3
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 1.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 1.0, 1.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 1.0, 1.0, 1.0});
    // Chord 4
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    // Chord 5
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 1.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 1.0, 1.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 1.0, 1.0, 1.0});
    expectedANoDur.add(new double[]{0.0, 1.0, 1.0, 1.0, 1.0});
    // Chord 6
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 1.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 1.0, 0.0, 1.0});
    expectedANoDur.add(new double[]{1.0, 0.0, 1.0, 0.0, 1.0});
    // Chord 7
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 1.0, 0.0, 0.0});
    // Chord 8
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    // Chords 9-14
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    // Chord 15
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expectedANoDur.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    // b. Longer CoDNote in the lower voice
  	List<double[]> expectedBNoDur = new ArrayList<double[]>(expectedANoDur);
  	expectedBNoDur.set(13, new double[]{0.0, 0.0, 0.0, 0.0, 0.0}); 	
    // c. CoDNotes of equal duration (quarter)
   	List<double[]> expectedCNoDur = new ArrayList<double[]>(expectedANoDur);
   	expectedCNoDur.set(13, new double[]{0.0, 0.0, 0.0, 0.0, 0.0}); 	   	
   	// d. CoDNotes of equal duration (eighth)
   	List<double[]> expectedDNoDur = new ArrayList<double[]>(expectedANoDur);
   	expectedDNoDur.set(13, new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
   	expected.addAll(expectedANoDur); expected.addAll(expectedBNoDur); expected.addAll(expectedCNoDur); 
   	expected.addAll(expectedDNoDur);
       		
   	// Modelling duration
    // a. Unadapted (longer CoDnote in the upper voice)
   	List<double[]> expectedADur = new ArrayList<double[]>();
   	// Chord 0
   	expectedADur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
   	expectedADur.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
   	expectedADur.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expectedADur.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    // Chord 1
    expectedADur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedADur.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expectedADur.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expectedADur.add(new double[]{1.0, 0.0, 1.0, 1.0, 0.0});
    // Chord 2
    expectedADur.add(new double[]{1.0, 0.0, 1.0, 0.0, 0.0});
    // Chord 3
    expectedADur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedADur.add(new double[]{0.0, 0.0, 0.0, 0.0, 1.0});
    expectedADur.add(new double[]{0.0, 0.0, 0.0, 1.0, 1.0});
    expectedADur.add(new double[]{0.0, 0.0, 1.0, 1.0, 1.0});
    // Chord 4
    expectedADur.add(new double[]{1.0, 0.0, 1.0, 1.0, 0.0});
    // Chord 5
    expectedADur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedADur.add(new double[]{0.0, 0.0, 0.0, 0.0, 1.0});
    expectedADur.add(new double[]{0.0, 0.0, 0.0, 1.0, 1.0});
    expectedADur.add(new double[]{0.0, 0.0, 1.0, 1.0, 1.0});
    expectedADur.add(new double[]{0.0, 1.0, 1.0, 1.0, 1.0});
    // Chord 6
    expectedADur.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expectedADur.add(new double[]{0.0, 0.0, 0.0, 1.0, 1.0});
    expectedADur.add(new double[]{0.0, 0.0, 1.0, 1.0, 1.0});
    expectedADur.add(new double[]{1.0, 0.0, 1.0, 1.0, 1.0});
    // Chord 7
    expectedADur.add(new double[]{0.0, 1.0, 0.0, 1.0, 1.0});
    expectedADur.add(new double[]{0.0, 1.0, 1.0, 1.0, 1.0});
    // Chord 8
    expectedADur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedADur.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expectedADur.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expectedADur.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    // Chords 9-14
    expectedADur.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    expectedADur.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    expectedADur.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    expectedADur.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    expectedADur.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    expectedADur.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    // Chord 15
    expectedADur.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedADur.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expectedADur.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expectedADur.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    // b. Longer CoDNote in the lower voice
   	List<double[]> expectedBDur = new ArrayList<double[]>(expectedADur);
   	expectedBDur.set(13, new double[]{0.0, 1.0, 1.0, 1.0, 0.0}); 	
    // c. CoDNotes of equal duration (quarter)
    List<double[]> expectedCDur = new ArrayList<double[]>(expectedADur);
    expectedCDur.set(13, new double[]{1.0, 1.0, 1.0, 1.0, 0.0}); 	   	
    // d. CoDNotes of equal duration (eighth)
    List<double[]> expectedDDur = new ArrayList<double[]>(expectedADur);
    expectedDDur.set(13, new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expected.addAll(expectedADur); expected.addAll(expectedBDur); expected.addAll(expectedCDur); 
    expected.addAll(expectedDDur);
   	
    // b. Direction.RIGHT
    List<double[]> expectedRight = new ArrayList<double[]>();
    // Chord 15
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expectedRight.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    // Chords 9-14
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    // Chord 8
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expectedRight.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    // Chord 7
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 1.0, 0.0, 0.0});
    // Chord 6
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 1.0});
    expectedRight.add(new double[]{0.0, 0.0, 1.0, 0.0, 1.0});
    expectedRight.add(new double[]{1.0, 0.0, 1.0, 0.0, 1.0});
    // Chord 5
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 1.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 1.0, 1.0});
    expectedRight.add(new double[]{0.0, 0.0, 1.0, 1.0, 1.0});
    expectedRight.add(new double[]{0.0, 1.0, 1.0, 1.0, 1.0});
    // Chord 4
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    // Chord 3
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 1.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 1.0, 1.0});
    expectedRight.add(new double[]{0.0, 0.0, 1.0, 1.0, 1.0});
    // Chord 2
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    // Chord 1
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expectedRight.add(new double[]{1.0, 0.0, 1.0, 1.0, 0.0});
    // Chord 0
   	expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expectedRight.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    expected.addAll(expectedRight);
    
    // Calculate actual
   	List<double[]> actual = new ArrayList<double[]>();
   	// a. Direction.LEFT
   	// Not modelling duration
//   	FeatureGenerator.setModelDuration(false);
   	for (int i = 0; i < allTablatures.size(); i++) {
    	Integer[][] currentBasicTabSymbolProperties = allTablatures.get(i).getBasicTabSymbolProperties();
    	List<List<Double>> currentVoiceLabels = allTranscriptions.get(i).getVoiceLabels();
    	List<List<Double>> currentDurationLabels = allDurationLabels.get(i);
    	List<Integer[]> currentVoicesCoDNotes = allVoicesCoDNotes.get(i); 	
  	  for (int j = 0; j < currentBasicTabSymbolProperties.length; j++) {
   	  	actual.add(FeatureGenerator.getVoicesAlreadyOccupied(currentBasicTabSymbolProperties, 
     	    currentDurationLabels, currentVoicesCoDNotes, null, currentVoiceLabels, 
     	    Direction.LEFT, j, false, false));
   	  }
   	}
    // Modelling duration
//   	FeatureGenerator.setModelDuration(true);
   	for (int i = 0; i < allTablatures.size(); i++) {
    	Integer[][] currentBasicTabSymbolProperties = allTablatures.get(i).getBasicTabSymbolProperties();
    	List<List<Double>> currentVoiceLabels = allTranscriptions.get(i).getVoiceLabels();
    	List<List<Double>> currentDurationLabels = allDurationLabels.get(i);
    	List<Integer[]> currentVoicesCoDNotes = allVoicesCoDNotes.get(i); 	
  	  for (int j = 0; j < currentBasicTabSymbolProperties.length; j++) {
   	  	actual.add(FeatureGenerator.getVoicesAlreadyOccupied(currentBasicTabSymbolProperties, 
     	    currentDurationLabels, currentVoicesCoDNotes, null, currentVoiceLabels, 
     	    Direction.LEFT, j, true, false));
   	  }
   	}
    // b. Direction.RIGHT (tablatureA only; note durations 'from the left' only make a difference in the fwd 
   	// model and not in the bwd model, where they are unknown)
//   	FeatureGenerator.setModelDuration(false);
   	List<Integer> backwardsMapping = FeatureGenerator.getBackwardsMapping(tablatureA.getNumberOfNotesPerChord());
   	for (int i : backwardsMapping) {
   		actual.add(FeatureGenerator.getVoicesAlreadyOccupied(tablatureA.getBasicTabSymbolProperties(), 
     	  durationLabelsA, voicesCoDNotesA, null, transcriptionA.getVoiceLabels(), 
     	  Direction.RIGHT, i, false, false));
   	}
   	 	  	
    // Assert equality
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) { 
    	assertEquals(expected.get(i).length, actual.get(i).length);
    	for (int j = 0; j < expected.get(i).length; j++) {
    		assertEquals(expected.get(i)[j], actual.get(i)[j]);
    	}
    }
	}
	
	
	public void testGetVoicesAlreadyOccupiedNonTab() {
  	Transcription transcription = new Transcription(midiTestpiece1, null);

    // Determine expected
    List<double[]> expected = new ArrayList<double[]>();
    // a. Direction.LEFT
    // Chord 0    
    expected.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expected.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expected.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expected.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    // Chord 1
    expected.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expected.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expected.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expected.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    // Chord 2
    expected.add(new double[]{1.0, 0.0, 1.0, 0.0, 0.0});
    // Chord 3
    expected.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expected.add(new double[]{0.0, 0.0, 0.0, 0.0, 1.0});
    expected.add(new double[]{0.0, 0.0, 0.0, 1.0, 1.0});
    expected.add(new double[]{0.0, 0.0, 1.0, 1.0, 1.0});
    expected.add(new double[]{1.0, 0.0, 1.0, 1.0, 1.0});
    // Chord 4
    expected.add(new double[]{1.0, 0.0, 1.0, 1.0, 0.0});
    // Chord 5
    expected.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expected.add(new double[]{0.0, 0.0, 0.0, 0.0, 1.0});
    expected.add(new double[]{0.0, 0.0, 0.0, 1.0, 1.0});
    expected.add(new double[]{0.0, 0.0, 1.0, 1.0, 1.0});
    expected.add(new double[]{0.0, 1.0, 1.0, 1.0, 1.0});
    // Chord 6
    expected.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expected.add(new double[]{0.0, 0.0, 0.0, 1.0, 1.0});
    expected.add(new double[]{0.0, 0.0, 1.0, 1.0, 1.0});
    expected.add(new double[]{1.0, 0.0, 1.0, 1.0, 1.0});
    // Chord 7
    expected.add(new double[]{0.0, 1.0, 0.0, 1.0, 1.0});
    expected.add(new double[]{0.0, 1.0, 1.0, 1.0, 1.0});
    // Chord 8
    expected.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expected.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expected.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expected.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    // Chords 9-14
    expected.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    expected.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    expected.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    expected.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    expected.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    expected.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    // Chord 15
    expected.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expected.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expected.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expected.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    
    // b. Direction.RIGHT
    List<double[]> expectedRight = new ArrayList<double[]>();
    // Chord 15
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expectedRight.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    // Chords 9-14
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    // Chord 8
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expectedRight.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    // Chord 7
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 1.0, 0.0, 0.0});
    // Chord 6
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 1.0});
    expectedRight.add(new double[]{0.0, 0.0, 1.0, 0.0, 1.0});
    expectedRight.add(new double[]{1.0, 0.0, 1.0, 0.0, 1.0});
    // Chord 5
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 1.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 1.0, 1.0});
    expectedRight.add(new double[]{0.0, 0.0, 1.0, 1.0, 1.0});
    expectedRight.add(new double[]{0.0, 1.0, 1.0, 1.0, 1.0});
    // Chord 4
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    // Chord 3
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 1.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 1.0, 1.0});
    expectedRight.add(new double[]{0.0, 0.0, 1.0, 1.0, 1.0});
    expectedRight.add(new double[]{1.0, 0.0, 1.0, 1.0, 1.0});
    // Chord 2
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    // Chord 1
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expectedRight.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    // Chord 0
   	expectedRight.add(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 0.0, 1.0, 0.0});
    expectedRight.add(new double[]{0.0, 0.0, 1.0, 1.0, 0.0});
    expectedRight.add(new double[]{0.0, 1.0, 1.0, 1.0, 0.0});
    expected.addAll(expectedRight);
  	
    // Calculate actual
  	List<double[]> actual = new ArrayList<double[]>();
  	Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
  	List<List<Double>> voiceLabels = transcription.getVoiceLabels();
   	// a. Direction.LEFT
  	for (int i = 0; i < basicNoteProperties.length; i++) {
  		actual.add(FeatureGenerator.getVoicesAlreadyOccupied(null, null, null, basicNoteProperties, voiceLabels,
  			Direction.LEFT,	i, false, false));
  	}
  	// b. Direction.RIGHT
   	List<Integer> backwardsMapping = FeatureGenerator.getBackwardsMapping(transcription.getNumberOfNewNotesPerChord());
   	for (int i : backwardsMapping) {
   		actual.add(FeatureGenerator.getVoicesAlreadyOccupied(null, null, null, basicNoteProperties, voiceLabels,
    		Direction.RIGHT, i, false, false));
   	}
   		
    // Assert equality
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
    	assertEquals(expected.get(i).length, actual.get(i).length);
    	for (int j = 0; j < expected.get(i).length; j++) {
    		assertEquals(expected.get(i)[j], actual.get(i)[j]);
    	}
    }
	}
	
	
//  public void testGetNextNoteInVoice() {
//    Transcription transcription = new Transcription(midiTestpiece1, null);
//  	
//  	NotationSystem ns = transcription.getPiece().getScore();
//  	//
//  	NotationVoice nv0 = ns.get(0).get(0);
//  	Note nv0n0 = nv0.get(0).get(0); Note nv0n1 = nv0.get(1).get(0); Note nv0n2 = nv0.get(2).get(0);
//  	Note nv0n3 = nv0.get(3).get(0); Note nv0n4 = nv0.get(4).get(0); Note nv0n5 = nv0.get(5).get(0);
//  	Note nv0n6 = nv0.get(6).get(0); Note nv0n7 = nv0.get(7).get(0); Note nv0n8 = nv0.get(8).get(0);
//  	Note nv0n9 = nv0.get(9).get(0); Note nv0n10 = nv0.get(10).get(0); Note nv0n11 = nv0.get(11).get(0);
//  	Note nv0n12 = nv0.get(12).get(0); Note nv0n13 = nv0.get(13).get(0);   		
//  	//
//  	NotationVoice nv1 = ns.get(1).get(0);
//  	Note nv1n0 = nv1.get(0).get(0); Note nv1n1 = nv1.get(1).get(0); Note nv1n2 = nv1.get(2).get(0);
//  	Note nv1n3 = nv1.get(3).get(0); Note nv1n4 = nv1.get(4).get(0); Note nv1n5 = nv1.get(5).get(0);
//  	Note nv1n6 = nv1.get(6).get(0);
//  	//
//  	NotationVoice nv2 = ns.get(2).get(0);
//  	Note nv2n0 = nv2.get(0).get(0); Note nv2n1 = nv2.get(1).get(0); Note nv2n2 = nv2.get(2).get(0);
//  	Note nv2n3 = nv2.get(3).get(0); Note nv2n4 = nv2.get(4).get(0); Note nv2n5 = nv2.get(5).get(0);
//  	Note nv2n6 = nv2.get(6).get(0); Note nv2n7 = nv2.get(7).get(0);
//  	//
//  	NotationVoice nv3 = ns.get(3).get(0);
//  	Note nv3n0 = nv3.get(0).get(0); Note nv3n1 = nv3.get(1).get(0); Note nv3n2 = nv3.get(2).get(0);
//  	Note nv3n3 = nv3.get(3).get(0); Note nv3n4 = nv3.get(4).get(0); Note nv3n5 = nv3.get(5).get(0);
//  	Note nv3n6 = nv3.get(6).get(0);
//  	//
//  	NotationVoice nv4 = ns.get(4).get(0);
//  	Note nv4n0 = nv4.get(0).get(0); Note nv4n1 = nv4.get(1).get(0); Note nv4n2 = nv4.get(2).get(0);
//  	Note nv4n3 = nv4.get(3).get(0);
//  	
//  	// Determine expected
//  	List<Note> expected = new ArrayList<Note>();  
//    // Chord 0
//   	expected.addAll(Arrays.asList(new Note[]{nv3n1, nv2n1, nv1n1, nv0n1}));
//    // Chord 1
//   	expected.addAll(Arrays.asList(new Note[]{nv3n2, nv2n2, nv1n2, nv0n2}));
//    // Chord 2
//   	expected.addAll(Arrays.asList(new Note[]{nv3n3}));
//    // Chord 3
//   	expected.addAll(Arrays.asList(new Note[]{nv4n1, nv3n4, nv2n3, nv0n3, nv1n3}));
//    // Chord 4
//   	expected.addAll(Arrays.asList(new Note[]{nv4n2}));
//    // Chord 5
//   	expected.addAll(Arrays.asList(new Note[]{nv4n3, nv3n5, nv2n4, nv1n4, nv0n4}));
//    // Chord 6
//   	expected.addAll(Arrays.asList(new Note[]{null, nv2n5, nv0n5, nv1n5}));
//    // Chord 7
//   	expected.addAll(Arrays.asList(new Note[]{nv2n6, nv0n6}));
//    // Chord 8
//   	expected.addAll(Arrays.asList(new Note[]{nv3n6, nv2n7, nv1n6, nv0n7}));
//    // Chord 14-9
//   	expected.addAll(Arrays.asList(new Note[]{nv0n8, nv0n9, nv0n10, nv0n11, nv0n12, nv0n13}));
//    // Chord 15
//   	expected.addAll(Arrays.asList(new Note[]{null, null, null, null}));
//   	
//   	// Calculate actual
//   	List<Note> actual = new ArrayList<Note>();
//   	NoteSequence noteSeq = transcription.getNoteSequence();
//   	for (int i = 0; i < noteSeq.size(); i++) {
//  		Note n = noteSeq.getNoteAt(i);
//  		int voice = transcription.findVoice(n, ns);
//  		NotationVoice nv = ns.get(voice).get(0);
//  		actual.add(featureGenerator.getNextNoteInVoice(nv, n));
//  	}
//  	
//  	// Assert equality
//  	assertEquals(expected.size(), actual.size());
//  	for (int i = 0; i < expected.size(); i++) {
//  		assertEquals(expected.get(i), actual.get(i));
//  	}
//  }
  
  
//  public void testGetIndexOfPreviousNoteInVoice() {
//  	Tablature tablature = new Tablature(encodingTestpiece1);
//  	Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);
//  	
//  	NotationSystem ns = transcription.getPiece().getScore();
//  	List<NotationVoice> nv0 = Arrays.asList(new NotationVoice[]{ns.get(0).get(0)});
//  	List<NotationVoice> nv1 = Arrays.asList(new NotationVoice[]{ns.get(1).get(0)});
//  	List<NotationVoice> nv1And0 = Arrays.asList(new NotationVoice[]{ns.get(1).get(0), ns.get(0).get(0)});
//  	List<NotationVoice> nv2 = Arrays.asList(new NotationVoice[]{ns.get(2).get(0)});
//  	List<NotationVoice> nv3 = Arrays.asList(new NotationVoice[]{ns.get(3).get(0)});
//  	List<NotationVoice> nv4 = Arrays.asList(new NotationVoice[]{ns.get(4).get(0)});
//  	List<List<NotationVoice>> allNotVoices = new ArrayList<List<NotationVoice>>();
//    // Chord 0
//  	allNotVoices.add(nv3); allNotVoices.add(nv2); allNotVoices.add(nv1); allNotVoices.add(nv0);
//    // Chord 1
//  	allNotVoices.add(nv3); allNotVoices.add(nv2); allNotVoices.add(nv0); allNotVoices.add(nv1);
//    // Chord 2
//  	allNotVoices.add(nv3);
//    // Chord 3
//  	allNotVoices.add(nv4); allNotVoices.add(nv3); allNotVoices.add(nv2); allNotVoices.add(nv1And0);
//    // Chord 4
//  	allNotVoices.add(nv4);
//    // Chord 5
//  	allNotVoices.add(nv4); allNotVoices.add(nv3); allNotVoices.add(nv2); allNotVoices.add(nv1); allNotVoices.add(nv0);
//    // Chord 6
//  	allNotVoices.add(nv4); allNotVoices.add(nv2); allNotVoices.add(nv0); allNotVoices.add(nv1);
//    // Chord 7
//  	allNotVoices.add(nv2); allNotVoices.add(nv0);
//    // Chord 8
//  	allNotVoices.add(nv3); allNotVoices.add(nv2); allNotVoices.add(nv1); allNotVoices.add(nv0);
//    // Chord 9-14
//  	allNotVoices.add(nv0); allNotVoices.add(nv0); allNotVoices.add(nv0); allNotVoices.add(nv0); allNotVoices.add(nv0); allNotVoices.add(nv0);
//    // Chord 15
//  	allNotVoices.add(nv3); allNotVoices.add(nv2); allNotVoices.add(nv1); allNotVoices.add(nv0);
// 
//  	// Determine expected
//  	List<Integer> expected = new ArrayList<Integer>();
//  	// a. Forward model
//  	// Chord 0
//  	expected.addAll(Arrays.asList(new Integer[]{-1, -1, -1, -1}));
//    // Chord 1
//  	expected.addAll(Arrays.asList(new Integer[]{0, 0, 0, 0}));
//    // Chord 2
//  	expected.addAll(Arrays.asList(new Integer[]{1}));
//    // Chord 3
//  	expected.addAll(Arrays.asList(new Integer[]{-1, 2, 1, 1, 1}));
//    // Chord 4
//  	expected.addAll(Arrays.asList(new Integer[]{0}));
//    // Chord 5
//  	expected.addAll(Arrays.asList(new Integer[]{1, 3, 2, 2, 2}));
//    // Chord 6
//  	expected.addAll(Arrays.asList(new Integer[]{2, 3, 3, 3}));
//    // Chord 7
//  	expected.addAll(Arrays.asList(new Integer[]{4, 4}));
//    // Chord 8
//  	expected.addAll(Arrays.asList(new Integer[]{4, 5, 4, 5}));
//    // Chord 9-14
//  	expected.addAll(Arrays.asList(new Integer[]{6, 7, 8, 9, 10, 11}));
//    // Chord 15
//  	expected.addAll(Arrays.asList(new Integer[]{5, 6, 5, 12}));
//  	
//    // b. Backward model
//  	List<Integer> expectedBackwards = new ArrayList<Integer>();
//   	// Chord 15
//  	expectedBackwards.addAll(Arrays.asList(new Integer[]{-1, -1, -1, -1}));
//     // Chord 14-9
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{13, 12, 11, 10, 9, 8}));
//     // Chord 8
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{6, 7, 6, 7}));
//     // Chord 7
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{6, 6}));
//     // Chord 6
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{-1, 5, 5, 5}));
//     // Chord 5
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{3, 5, 4, 4, 4}));
//     // Chord 4
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{2}));
//     // Chord 3
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{1, 4, 3, 3, 3}));
//     // Chord 2
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{3}));
//     // Chord 1
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{2, 2, 2, 2}));
//     // Chord 0
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{1, 1, 1, 1}));
//  	expected.addAll(expectedBackwards);
//  	
//  	// Calculate actual
//   	List<Integer> actual = new ArrayList<Integer>();
//   	Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//   	// a. Forwards model
//  	featureGenerator.setModelBackward(false);
//  	for (int i = 0; i < basicTabSymbolProperties.length; i++) {
//  		int pitch = basicTabSymbolProperties[i][Tablature.PITCH];
//  		Rational mt = new Rational(basicTabSymbolProperties[i][Tablature.ONSET_TIME], Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//  		Rational md = new Rational(basicTabSymbolProperties[i][Tablature.MIN_DURATION], Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//  		Note n = Transcription.createNote(pitch, mt, md);
//  		List<NotationVoice> currentNotationVoices = allNotVoices.get(i);
//  		for (NotationVoice nv : currentNotationVoices) {
//  		  actual.add(featureGenerator.getIndexOfPreviousNoteInVoice(nv, n));
//  		}
//  	}
//    // b. Backwards model
//   	featureGenerator.setModelBackward(true);
//   	List<Integer> backwardsMapping = Transcription.getBackwardsMapping(tablature.getNumberOfNotesPerChord());
//   	for (int i : backwardsMapping) {
//   		int pitch = basicTabSymbolProperties[i][Tablature.PITCH];
//   		Rational mt = new Rational(basicTabSymbolProperties[i][Tablature.ONSET_TIME], Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//   		Rational md = new Rational(basicTabSymbolProperties[i][Tablature.MIN_DURATION], Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//   		Note n = Transcription.createNote(pitch, mt, md);
//   		List<NotationVoice> currentNotationVoices = allNotVoices.get(i);
//   		for (NotationVoice nv : currentNotationVoices) {
//   		  actual.add(featureGenerator.getIndexOfPreviousNoteInVoice(nv, n));
//   		}
//   	}
//  	
//  	// Assert equality
//  	assertEquals(expected.size(), actual.size());
//  	for (int i = 0; i < expected.size(); i++) {
//  		assertEquals(expected.get(i), actual.get(i));
//  	}	
//  }
  
  
//  public void testGetIndexOfPreviousNoteInVoiceNonTab() {
//  	Transcription transcription = new Transcription(midiTestpiece1, null);
//  	
//  	NotationSystem ns = transcription.getPiece().getScore();
//  	NotationVoice nv0 = ns.get(0).get(0);
//  	NotationVoice nv1 = ns.get(1).get(0);
//  	NotationVoice nv2 = ns.get(2).get(0);
//  	NotationVoice nv3 = ns.get(3).get(0);
//  	NotationVoice nv4 = ns.get(4).get(0);
//  	List<NotationVoice> allNotVoices = new ArrayList<NotationVoice>();
//    // Chord 0
//  	allNotVoices.add(nv3); allNotVoices.add(nv2); allNotVoices.add(nv1); allNotVoices.add(nv0);
//    // Chord 1
//  	allNotVoices.add(nv3); allNotVoices.add(nv2); allNotVoices.add(nv1); allNotVoices.add(nv0);
//    // Chord 2
//  	allNotVoices.add(nv3);
//    // Chord 3
//  	allNotVoices.add(nv4); allNotVoices.add(nv3); allNotVoices.add(nv2); allNotVoices.add(nv0); allNotVoices.add(nv1);
//    // Chord 4
//  	allNotVoices.add(nv4);
//    // Chord 5
//  	allNotVoices.add(nv4); allNotVoices.add(nv3); allNotVoices.add(nv2); allNotVoices.add(nv1); allNotVoices.add(nv0);
//    // Chord 6
//  	allNotVoices.add(nv4); allNotVoices.add(nv2); allNotVoices.add(nv0); allNotVoices.add(nv1);
//    // Chord 7
//  	allNotVoices.add(nv2); allNotVoices.add(nv0);
//    // Chord 8
//  	allNotVoices.add(nv3); allNotVoices.add(nv2); allNotVoices.add(nv1); allNotVoices.add(nv0);
//    // Chord 9-14
//  	allNotVoices.add(nv0); allNotVoices.add(nv0); allNotVoices.add(nv0); allNotVoices.add(nv0); allNotVoices.add(nv0); allNotVoices.add(nv0);
//    // Chord 15
//  	allNotVoices.add(nv3); allNotVoices.add(nv2); allNotVoices.add(nv1); allNotVoices.add(nv0);
// 
//  	// Determine expected
//  	List<Integer> expected = new ArrayList<Integer>();
//  	// a. Forward model
//  	// Chord 0
//  	expected.addAll(Arrays.asList(new Integer[]{-1, -1, -1, -1}));
//    // Chord 1
//  	expected.addAll(Arrays.asList(new Integer[]{0, 0, 0, 0}));
//    // Chord 2
//  	expected.addAll(Arrays.asList(new Integer[]{1}));
//    // Chord 3
//  	expected.addAll(Arrays.asList(new Integer[]{-1, 2, 1, 1, 1}));
//    // Chord 4
//  	expected.addAll(Arrays.asList(new Integer[]{0}));
//    // Chord 5
//  	expected.addAll(Arrays.asList(new Integer[]{1, 3, 2, 2, 2}));
//    // Chord 6
//  	expected.addAll(Arrays.asList(new Integer[]{2, 3, 3, 3}));
//    // Chord 7
//  	expected.addAll(Arrays.asList(new Integer[]{4, 4}));
//    // Chord 8
//  	expected.addAll(Arrays.asList(new Integer[]{4, 5, 4, 5}));
//    // Chord 9-14
//  	expected.addAll(Arrays.asList(new Integer[]{6, 7, 8, 9, 10, 11}));
//    // Chord 15
//  	expected.addAll(Arrays.asList(new Integer[]{5, 6, 5, 12}));
//  	
//    // b. Backward model
//  	List<Integer> expectedBackwards = new ArrayList<Integer>();
//   	// Chord 15
//  	expectedBackwards.addAll(Arrays.asList(new Integer[]{-1, -1, -1, -1}));
//     // Chord 14-9
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{13, 12, 11, 10, 9, 8}));
//     // Chord 8
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{6, 7, 6, 7}));
//     // Chord 7
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{6, 6}));
//     // Chord 6
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{-1, 5, 5, 5}));
//     // Chord 5
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{3, 5, 4, 4, 4}));
//     // Chord 4
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{2}));
//     // Chord 3
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{1, 4, 3, 3, 3}));
//     // Chord 2
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{3}));
//     // Chord 1
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{2, 2, 2, 2}));
//     // Chord 0
//   	expectedBackwards.addAll(Arrays.asList(new Integer[]{1, 1, 1, 1}));
//  	expected.addAll(expectedBackwards);
//  	
//  	// Calculate actual
//   	List<Integer> actual = new ArrayList<Integer>();
//   	Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
//   	// a. Forwards model
//  	featureGenerator.setModelBackward(false);
//  	for (int i = 0; i < basicNoteProperties.length; i++) {
//  		int pitch = basicNoteProperties[i][Transcription.PITCH];
//  		Rational mt = new Rational(basicNoteProperties[i][Transcription.ONSET_TIME_NUMER], 
//  			basicNoteProperties[i][Transcription.ONSET_TIME_DENOM]);
//  		Rational md = new Rational(basicNoteProperties[i][Transcription.DURATION_NUMER], 
//  		  basicNoteProperties[i][Transcription.DURATION_DENOM]);
//  		Note n = Transcription.createNote(pitch, mt, md);
//  		actual.add(featureGenerator.getIndexOfPreviousNoteInVoice(allNotVoices.get(i), n));
//  	}
//    // b. Backwards model
//   	featureGenerator.setModelBackward(true);
//   	List<Integer> backwardsMapping = Transcription.getBackwardsMapping(transcription.getNumberOfNewNotesPerChord());
//   	for (int i : backwardsMapping) {
//   		int pitch = basicNoteProperties[i][Transcription.PITCH];
//  		Rational mt = new Rational(basicNoteProperties[i][Transcription.ONSET_TIME_NUMER], 
//  			basicNoteProperties[i][Transcription.ONSET_TIME_DENOM]);
//  		Rational md = new Rational(basicNoteProperties[i][Transcription.DURATION_NUMER], 
//  		  basicNoteProperties[i][Transcription.DURATION_DENOM]);
//   		Note n = Transcription.createNote(pitch, mt, md);
//   		actual.add(featureGenerator.getIndexOfPreviousNoteInVoice(allNotVoices.get(i), n));
//   	}
//  	
//  	// Assert equality
//  	assertEquals(expected.size(), actual.size());
//  	for (int i = 0; i < expected.size(); i++) {
//  		assertEquals(expected.get(i), actual.get(i));
//  	}	
//  }
	
	
	public void testGenerateNoteFeatureVector() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
		Note note6 = transcription.getNoteSequence().getNoteAt(6);
		Note note23 = transcription.getNoteSequence().getNoteAt(23);

		List<List<Double>> expected = new ArrayList<List<Double>>();
		// a. Fwd model
		// Not modelling duration
		// Chord 1, note at index 6
		List<Double> expFwdNoDur6 = new ArrayList<Double>(); 
		expFwdNoDur6.addAll(Arrays.asList(new Double[]{
			70.0, 2.0, 8.0, 3/16.0, 1/4.0, 0.0, 0/4.0, // basicNoteFeatures
			1.0/((3/16.0) + 1), 1.0/(24+1), 6.0, -1.0, -1.0, -1.0, // proximitiesAndCourseInfoAhead
			1.0/((1/4.0) + 1), 1.0/(7+1), 2.0, -1.0, -1.0, -1.0, 
			1.0/((3/8.0) + 1), 1.0/(27+1), 6.0, -1.0, -1.0, -1.0, 
			1.0, // numberOfNewOnsetsNextChord  
			4.0, 3.0, 3.0, -1.0, 12.0, 12.0, 3.0, -1.0, // positionWithinChord
			0.0, 1.0, 0.0, 0.0, 0.0 // voicesWithPreviousNoteOnSameCourse
		}));
		expFwdNoDur6.addAll(Arrays.asList(new Double[]{1.0/(3+1), 1.0/(7+1), 1.0/(15+1), 1.0/(22+1), -1.0})); // pitchProximities 
		expFwdNoDur6.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), -1.0})); // interOnsetTimeProximities 
		expFwdNoDur6.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0})); // offsetOnsetTimeProximities
		expFwdNoDur6.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expFwdNoDur6);
		// Chord 7, note at index 23
		List<Double> expFwdNoDur23 = new ArrayList<Double>(); 
		expFwdNoDur23.addAll(Arrays.asList(new Double[]{
			57.0, 3.0, 0.0, 1/8.0, 9/8.0, 0.0, 7/8.0, // basicNoteFeatures
			1.0/((1/8.0) + 1), 1.0/(2+1), 4.0, -1.0, 1.0/(5+1), 2.0, // proximitiesAndCourseInfoAhead
			1.0/((3/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(9+1), 2.0, 
			1.0/((1/4.0) + 1), -1.0, -1.0, -1.0, 1.0/(10+1), 1.0,  
			4.0, // numberOfNewOnsetsNextChord
			2.0, 0.0, -1.0, 9.0, 9.0, -1.0, -1.0, -1.0, // positionWithinChord
			0.0, 0.0, 1.0, 0.0, 0.0 // voicesWithPreviousNoteOnSameCourse
		}));
		expFwdNoDur23.addAll(Arrays.asList(new Double[]{1.0/(5+1), 1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(14+1)})); // pitchProximities 
		expFwdNoDur23.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((3/8.0) + 1), 1/((1/8.0) + 1)})); // interOnsetTimeProximities
		expFwdNoDur23.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), 1/((1/8.0) + 1), 1/((0.0) + 1)})); // offsetOnsetTimeProximities  
		expFwdNoDur23.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expFwdNoDur23);

		// Modelling duration
		// Chord 1, note at index 6
		List<Double> expFwdDur6 = new ArrayList<Double>(); 
		expFwdDur6.addAll(Arrays.asList(new Double[]{
			70.0, 2.0, 8.0, 3/16.0, 1/4.0, 0.0, 0/4.0, // basicNoteFeatures
			1.0/((3/16.0) + 1), 1.0/(24+1), 6.0, -1.0, -1.0, -1.0, // proximitiesAndCourseInfoAhead
			1.0/((1/4.0) + 1), 1.0/(7+1), 2.0, -1.0, -1.0, -1.0, 
			1.0/((3/8.0) + 1), 1.0/(27+1), 6.0, -1.0, -1.0, -1.0, 
			1.0, // numberOfNewOnsetsNextChord  
			4.0, 3.0, 3.0, -1.0, 12.0, 12.0, 3.0, -1.0, // positionWithinChord
			0.0, 1.0, 0.0, 0.0, 0.0 // voicesWithPreviousNoteOnSameCourse
		}));
		expFwdDur6.addAll(Arrays.asList(new Double[]{1.0/(3+1), 1.0/(7+1), 1.0/(15+1), 1.0/(22+1), -1.0})); // pitchProximities
		expFwdDur6.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), -1.0})); // interOnsetTimeProximities
		expFwdDur6.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0})); // offsetOnsetTimeProximities
		expFwdDur6.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expFwdDur6);
		// Chord 7, note at index 23
		List<Double> expFwdDur23 = new ArrayList<Double>();
		expFwdDur23.addAll(Arrays.asList(new Double[]{
			57.0, 3.0, 0.0, 1/8.0, 9/8.0, 0.0, 7/8.0, // basicNoteFeatures
			1.0/((1/8.0) + 1), 1.0/(2+1), 4.0, -1.0, 1.0/(5+1), 2.0, // proximitiesAndCourseInfoAhead
			1.0/((3/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(9+1), 2.0,
			1.0/((1/4.0) + 1), -1.0, -1.0, -1.0, 1.0/(10+1), 1.0, 
			4.0, // numberOfNewOnsetsNextChord   
			5.0, 2.0, 2.0, 9.0, 12.0, 2.0, 9.0, 1.0, // positionWithinChord
			0.0, 0.0, 1.0, 0.0, 0.0 // voicesWithPreviousNoteOnSameCourse
		}));
		expFwdDur23.addAll(Arrays.asList(new Double[]{1.0/(5+1), 1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(14+1)})); // pitchProximities
		expFwdDur23.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((3/8.0) + 1), 1/((1/8.0) + 1)})); // interOnsetTimeProximities
		expFwdDur23.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), -1/((1/8.0) + 1), 1/((0.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1)})); // offsetOnsetTimeProximities
		expFwdDur23.addAll(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 1.0, 1.0})); // voicesAlreadyOccupied
		expected.add(expFwdDur23);

		// b. Bwd model 			
		// Not modelling duration
		// Chord 1, note at index 6
		List<Double> expBwdNoDur6 = new ArrayList<Double>(); 
		expBwdNoDur6.addAll(Arrays.asList(new Double[]{
			70.0, 2.0, 8.0, 3/16.0, 1/4.0, 0.0, 0/4.0, // basicNoteFeatures
			1.0/((3/16.0) + 1), 1.0/(24+1), 6.0, -1.0, -1.0, -1.0, // proximitiesAndCourseInfoAhead
			1.0/((1/4.0) + 1), 1.0/(7+1), 2.0, -1.0, -1.0, -1.0, 
			1.0/((3/8.0) + 1), 1.0/(27+1), 6.0, -1.0, -1.0, -1.0, 
			1.0, // numberOfNewOnsetsNextChord  
			4.0, 3.0, 3.0, -1.0, 12.0, 12.0, 3.0, -1.0, // positionWithinChord
			1.0, 1.0, 0.0, 0.0, 0.0 // voicesWithPreviousNoteOnSameCourse
		})); 
		expBwdNoDur6.addAll(Arrays.asList(new Double[]{1.0/(7+1), 1.0/(7+1), 1.0/(13+1), 1.0/(24+1), 1.0/(25+1)})); // pitchProximities	
 		expBwdNoDur6.addAll(Arrays.asList(new Double[]{-1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((3/16.0) + 1), -1/((1/4.0) + 1)})); // interOnsetTimeProximities
		expBwdNoDur6.addAll(Arrays.asList(new Double[]{-1/((1/16.0) + 1), -1/((1/16.0) + 1), -1/((1/16.0) + 1), 1.0/(0+1), -1/((1/16.0) + 1)})); // offsetOnsetTimeProximities
		expBwdNoDur6.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expBwdNoDur6);

		// Chord 7, note at index 23
		List<Double> expBwdNoDur23 = new ArrayList<Double>(); 
		expBwdNoDur23.addAll(Arrays.asList(new Double[]{
			57.0, 3.0, 0.0, 1/8.0, 9/8.0, 0.0, 7/8.0, // basicNoteFeatures
			1.0/((1/8.0) + 1), 1.0/(2+1), 4.0, -1.0, 1.0/(5+1), 2.0, // proximitiesAndCourseInfoAhead
			1.0/((3/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(9+1), 2.0,
			1.0/((1/4.0) + 1), -1.0, -1.0, -1.0, 1.0/(10+1), 1.0,
			4.0, // numberOfNewOnsetsNextChord
			2.0, 0.0, -1.0, 9.0, 9.0, -1.0, -1.0, -1.0, // positionWithinChord
			0.0, 0.0, 0.0, 0.0, 0.0 // voicesWithPreviousNoteOnSameCourse
		}));
		expBwdNoDur23.addAll(Arrays.asList(new Double[]{1.0/(10+1), 1.0/(5+1), 1.0/(2+1), 1.0/(14+1), -1.0})); // pitchProximities 
		expBwdNoDur23.addAll(Arrays.asList(new Double[]{-1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1.0})); // interOnsetTimeProximities
		expBwdNoDur23.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), -1.0})); // offsetOnsetTimeProximities  
		expBwdNoDur23.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expBwdNoDur23);

		// Modelling duration (same as expBwdNoDur because the full duration of the current note is not known)
		List<Double> expBwdDur6 = new ArrayList<Double>(expBwdNoDur6);
		expected.add(expBwdDur6);
		List<Double> expBwdDur23 = new ArrayList<Double>(expBwdNoDur23);
		expected.add(expBwdDur23);

		List<List<Double>> actual = new ArrayList<List<Double>>();
		List<List<Double>> durationLabels = transcription.getDurationLabels();
		List<Integer[]> voicesCoDNotes = transcription.getVoicesCoDNotes();
		List<List<Double>> voiceLabels = transcription.getVoiceLabels();
		List<Integer[]> meterInfo = tablature.getMeterInfo();
		// a. Fwd model	  
		// Not modelling duration
		actual.add(FeatureGenerator.generateNoteFeatureVector(basicTabSymbolProperties,	
			durationLabels, voicesCoDNotes, null, transcription, note6, voiceLabels, 
			meterInfo, 6, false, false, 1));
		actual.add(FeatureGenerator.generateNoteFeatureVector(basicTabSymbolProperties,	
			durationLabels, voicesCoDNotes, null, transcription, note23, voiceLabels, 
			meterInfo, 23, false, false, 1));
		// Modelling duration
		actual.add(FeatureGenerator.generateNoteFeatureVector(basicTabSymbolProperties, 
			durationLabels, voicesCoDNotes, null, transcription, note6, voiceLabels, 
			meterInfo, 6, true, false, 1));
		actual.add(FeatureGenerator.generateNoteFeatureVector(basicTabSymbolProperties, 
			durationLabels, voicesCoDNotes, null, transcription, note23, voiceLabels, 
			meterInfo, 23, true, false, 1));
		// b. Bwd model
		// Not modelling duration
		actual.add(FeatureGenerator.generateNoteFeatureVector(basicTabSymbolProperties, 
			durationLabels, voicesCoDNotes, null, transcription, note6, voiceLabels, 
			meterInfo, 6, false, true, 1));
		actual.add(FeatureGenerator.generateNoteFeatureVector(basicTabSymbolProperties, 
			durationLabels, voicesCoDNotes, null, transcription, note23, voiceLabels, 
			meterInfo, 23, false, true, 1));
		// Modelling duration
		actual.add(FeatureGenerator.generateNoteFeatureVector(basicTabSymbolProperties, 
			durationLabels, voicesCoDNotes, null, transcription, note6, voiceLabels, 
			meterInfo, 6, true, true, 1));
		actual.add(FeatureGenerator.generateNoteFeatureVector(basicTabSymbolProperties, 
			durationLabels,	voicesCoDNotes, null, transcription, note23, voiceLabels, 
			meterInfo, 23, true, true, 1));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}


	public void testGenerateNoteFeatureVectorNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		Note note7 = transcription.getNoteSequence().getNoteAt(7);
		Note note24 = transcription.getNoteSequence().getNoteAt(24);

		List<List<Double>> expected = new ArrayList<List<Double>>();
		// a. Fwd model
		// Chord 1, note at index 7
		List<Double> expFwd7 = new ArrayList<Double>(); 
		expFwd7.addAll(Arrays.asList(new Double[]{
			72.0, 1/4.0, 0.0, 0/4.0, // basicNoteFeatures
			1.0/((3/16.0) + 1), 1.0/(24+1), -1.0, -1.0, // proximitiesAndCourseInfoAhead
			1.0/((1/4.0) + 1), 1.0/(7+1), -1.0, -1.0, 
			1.0/((3/8.0) + 1), 1.0/(27+1), -1.0, -1.0, 
			1.0, // numberOfNewOnsetsNextChord  
			4.0, 3.0, 3.0, -1.0, 12.0, 12.0, 3.0, -1.0 // positionWithinChord
		}));
		expFwd7.addAll(Arrays.asList(new Double[]{1.0/(3+1), 1.0/(7+1), 1.0/(15+1), 1.0/(22+1), -1.0})); // pitchProximities
		expFwd7.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), -1.0})); // interOnsetTimeProximities
		expFwd7.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0})); // offsetOnsetTimeProximities
		expFwd7.addAll(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied 
		expected.add(expFwd7);	
		// Chord 7, note at index 24 
		List<Double> expFwd24 = new ArrayList<Double>();
		expFwd24.addAll(Arrays.asList(new Double[]{
			59.0, 1/8.0, 0.0, 7/8.0, // basicNoteFeatures
			1.0/((1/8.0) + 1), 1.0/(2+1), -1.0, 1.0/(5+1), // proximitiesAndCourseInfoAhead
	    1.0/((3/16.0) + 1), -1.0, -1.0, 1.0/(9+1),
	    1.0/((1/4.0) + 1), -1.0, -1.0, 1.0/(10+1), 
	    4.0, // numberOfNewOnsetsNextChord
			5.0, 2.0, 2.0, 9.0, 12.0, 2.0, 9.0, 1.0 // positionWithinChord
		}));
		expFwd24.addAll(Arrays.asList(new Double[]{1.0/(5+1), 1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(14+1)})); // pitchProximities
		expFwd24.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((3/8.0) + 1), 1/((1/8.0) + 1)})); // interOnsetTimeProximities
		expFwd24.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), -1/((1/8.0) + 1), 1/((0.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1)})); // offsetOnsetTimeProximities
		expFwd24.addAll(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 1.0, 1.0})); // voicesAlreadyOccupied
		expected.add(expFwd24);

		// b. Bwd model
		// Chord 1, note at index 7
		List<Double> expBwd7 = new ArrayList<Double>(); 
		expBwd7.addAll(Arrays.asList(new Double[]{
			72.0, 1/4.0, 0.0, 0/4.0, // basicNoteFeatures
			1.0/((3/16.0) + 1), 1.0/(24+1), -1.0, -1.0, // proximitiesAndCourseInfoAhead
			1.0/((1/4.0) + 1), 1.0/(7+1), -1.0, -1.0, 
			1.0/((3/8.0) + 1), 1.0/(27+1), -1.0, -1.0, 
			1.0, // numberOfNewOnsetsNextChord  
			4.0, 3.0, 3.0, -1.0, 12.0, 12.0, 3.0, -1.0, // positionWithinChord
		}));
		expBwd7.addAll(Arrays.asList(new Double[]{1.0/(7+1), 1.0/(7+1), 1.0/(13+1), 1.0/(24+1), 1.0/(25+1)})); // pitchProximities
		expBwd7.addAll(Arrays.asList(new Double[]{-1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((3/16.0) + 1), -1/((1/4.0) + 1)})); // interOnsetTimeProximities 
		expBwd7.addAll(Arrays.asList(new Double[]{1/(0.0+1), 1/(0.0+1), 1/(0.0+1), 1/((1/16.0) + 1), 1/(0.0+1)})); // offsetOnsetTimeProximities
		expBwd7.addAll(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expBwd7);
		// Chord 7, note at index 24 
		List<Double> expBwd24 = new ArrayList<Double>();
		expBwd24.addAll(Arrays.asList(new Double[]{
			59.0, 1/8.0, 0.0, 7/8.0, // basicNoteFeatures
			1.0/((1/8.0) + 1), 1.0/(2+1), -1.0, 1.0/(5+1), // proximitiesAndCourseInfoAhead
		    1.0/((3/16.0) + 1), -1.0, -1.0, 1.0/(9+1),
		    1.0/((1/4.0) + 1), -1.0, -1.0, 1.0/(10+1), 
		    4.0, // numberOfNewOnsetsNextChord
			5.0, 2.0, 2.0, 9.0, 12.0, 2.0, 9.0, 1.0 // positionWithinChord
		}));
		expBwd24.addAll(Arrays.asList(new Double[]{1.0/(10+1), 1.0/(5+1), 1.0/(2+1), 1.0/(14+1), -1.0})); // pitchProximities
		expBwd24.addAll(Arrays.asList(new Double[]{-1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1.0})); // interOnsetTimeProximities
		expBwd24.addAll(Arrays.asList(new Double[]{1/(0.0 + 1), 1/(0.0 + 1), 1/(0.0 + 1), 1/(0.0 + 1), -1.0})); // offsetOnsetTimeProximities
		expBwd24.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expBwd24);

		Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
		List<List<Double>> voiceLabels = transcription.getVoiceLabels();
		List<Integer[]> meterInfo = transcription.getMeterInfo();

		List<List<Double>> actual = new ArrayList<List<Double>>();
		// a. Fwd model	  
		actual.add(FeatureGenerator.generateNoteFeatureVector(null,	null, null, basicNoteProperties, 
			transcription, note7, voiceLabels, meterInfo, 7, true, false, 1)); // value of argModelDuration irrelevant
		actual.add(FeatureGenerator.generateNoteFeatureVector(null,	null, null, basicNoteProperties,
			transcription, note24, voiceLabels, meterInfo, 24, true, false, 1)); // value of argModelDuration irrelevant
		// b. Bwd model
		actual.add(FeatureGenerator.generateNoteFeatureVector(null,	null, null, basicNoteProperties, 
			transcription, note7, voiceLabels, meterInfo, 7, true, true, 1)); // value of argModelDuration irrelevant
		actual.add(FeatureGenerator.generateNoteFeatureVector(null,	null, null, basicNoteProperties,
			transcription, note24, voiceLabels, meterInfo, 24, true, true, 1)); // value of argModelDuration irrelevant

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}
	
	
	public void testGenerateNoteFeatureVectorDISSFirst() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
		Note note6 = transcription.getNoteSequence().getNoteAt(6);
		Note note23 = transcription.getNoteSequence().getNoteAt(23);

		List<List<Double>> expected = new ArrayList<List<Double>>();
		// a. Fwd model
		// Not modelling duration
		// Chord 1, note at index 6
		List<Double> expFwdNoDur6 = new ArrayList<Double>(); 
		expFwdNoDur6.addAll(Arrays.asList(new Double[]{
			70.0, 2.0, 8.0, 3/16.0, 1/4.0, 0.0, // note-level
			3.0, 3.0, -1.0, // note-chord
			4.0, 0/4.0, 1.0, 12.0, 12.0, 3.0, -1.0, // chord-level
			0.0, 1.0, 0.0, 0.0, 0.0 // polyphonic embedding
		}));
		expFwdNoDur6.addAll(Arrays.asList(new Double[]{1.0/(3+1), 1.0/(7+1), 1.0/(15+1), 1.0/(22+1), -1.0})); // pitchProximities 
		expFwdNoDur6.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), -1.0})); // interOnsetTimeProximities 
		expFwdNoDur6.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0})); // offsetOnsetTimeProximities
		expFwdNoDur6.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expFwdNoDur6);
		// Chord 7, note at index 23
		List<Double> expFwdNoDur23 = new ArrayList<Double>(); 
		expFwdNoDur23.addAll(Arrays.asList(new Double[]{
			57.0, 3.0, 0.0, 1/8.0, 9/8.0, 0.0, // note-level
			0.0, -1.0, 9.0, // note-chord
			2.0, 7/8.0, 4.0, 9.0, -1.0, -1.0, -1.0, // chord-level
			0.0, 0.0, 1.0, 0.0, 0.0 // polyphonic embedding
		}));
		expFwdNoDur23.addAll(Arrays.asList(new Double[]{1.0/(5+1), 1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(14+1)})); // pitchProximities 
		expFwdNoDur23.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((3/8.0) + 1), 1/((1/8.0) + 1)})); // interOnsetTimeProximities
		expFwdNoDur23.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), 1/((1/8.0) + 1), 1/((0.0) + 1)})); // offsetOnsetTimeProximities  
		expFwdNoDur23.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expFwdNoDur23);

		// Modelling duration
		// Chord 1, note at index 6
		List<Double> expFwdDur6 = new ArrayList<Double>(); 
		expFwdDur6.addAll(Arrays.asList(new Double[]{
			70.0, 2.0, 8.0, 3/16.0, 1/4.0, 0.0, // note-level
			3.0, 3.0, -1.0, // note-chord
			4.0, 0/4.0, 1.0, 12.0, 12.0, 3.0, -1.0, // chord-level
			0.0, 1.0, 0.0, 0.0, 0.0 // polyphonic embedding
		}));
		expFwdDur6.addAll(Arrays.asList(new Double[]{1.0/(3+1), 1.0/(7+1), 1.0/(15+1), 1.0/(22+1), -1.0})); // pitchProximities
		expFwdDur6.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), -1.0})); // interOnsetTimeProximities
		expFwdDur6.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0})); // offsetOnsetTimeProximities
		expFwdDur6.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expFwdDur6);
		// Chord 7, note at index 23
		List<Double> expFwdDur23 = new ArrayList<Double>();
		expFwdDur23.addAll(Arrays.asList(new Double[]{
			57.0, 3.0, 0.0, 1/8.0, 9/8.0, 0.0, // note-level
			2.0, 2.0, 9.0, // note-chord
			5.0, 7/8.0, 4.0, 12.0, 2.0, 9.0, 1.0, // chord-level
			0.0, 0.0, 1.0, 0.0, 0.0 // polyphonic embedding
		}));
		expFwdDur23.addAll(Arrays.asList(new Double[]{1.0/(5+1), 1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(14+1)})); // pitchProximities
		expFwdDur23.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((3/8.0) + 1), 1/((1/8.0) + 1)})); // interOnsetTimeProximities
		expFwdDur23.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), -1/((1/8.0) + 1), 1/((0.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1)})); // offsetOnsetTimeProximities
		expFwdDur23.addAll(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 1.0, 1.0})); // voicesAlreadyOccupied
		expected.add(expFwdDur23);

		// b. Bwd model 			
		// Not modelling duration
		// Chord 1, note at index 6
		List<Double> expBwdNoDur6 = new ArrayList<Double>(); 
		expBwdNoDur6.addAll(Arrays.asList(new Double[]{
			70.0, 2.0, 8.0, 3/16.0, 1/4.0, 0.0, // note-level
			3.0, 3.0, -1.0, // note-chord
			4.0, 0/4.0, 1.0, 12.0, 12.0, 3.0, -1.0, // chord-level
			1.0, 1.0, 0.0, 0.0, 0.0 // polyphonic embedding
		})); 
		expBwdNoDur6.addAll(Arrays.asList(new Double[]{1.0/(7+1), 1.0/(7+1), 1.0/(13+1), 1.0/(24+1), 1.0/(25+1)})); // pitchProximities	
 		expBwdNoDur6.addAll(Arrays.asList(new Double[]{-1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((3/16.0) + 1), -1/((1/4.0) + 1)})); // interOnsetTimeProximities
		expBwdNoDur6.addAll(Arrays.asList(new Double[]{-1/((1/16.0) + 1), -1/((1/16.0) + 1), -1/((1/16.0) + 1), 1.0/(0+1), -1/((1/16.0) + 1)})); // offsetOnsetTimeProximities
		expBwdNoDur6.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expBwdNoDur6);

		// Chord 7, note at index 23
		List<Double> expBwdNoDur23 = new ArrayList<Double>(); 
		expBwdNoDur23.addAll(Arrays.asList(new Double[]{
			57.0, 3.0, 0.0, 1/8.0, 9/8.0, 0.0, // note-level
			0.0, -1.0, 9.0, // note-chord
			2.0, 7/8.0, 4.0, 9.0, -1.0, -1.0, -1.0,// chord-level
			0.0, 0.0, 0.0, 0.0, 0.0 // polyphonic embedding
		}));
		expBwdNoDur23.addAll(Arrays.asList(new Double[]{1.0/(10+1), 1.0/(5+1), 1.0/(2+1), 1.0/(14+1), -1.0})); // pitchProximities 
		expBwdNoDur23.addAll(Arrays.asList(new Double[]{-1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1.0})); // interOnsetTimeProximities
		expBwdNoDur23.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), -1.0})); // offsetOnsetTimeProximities  
		expBwdNoDur23.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expBwdNoDur23);

		// Modelling duration (same as expBwdNoDur because the full duration of the current note is not known)
		List<Double> expBwdDur6 = new ArrayList<Double>(expBwdNoDur6);
		expected.add(expBwdDur6);
		List<Double> expBwdDur23 = new ArrayList<Double>(expBwdNoDur23);
		expected.add(expBwdDur23);

		List<List<Double>> actual = new ArrayList<List<Double>>();
		List<List<Double>> durationLabels = transcription.getDurationLabels();
		List<Integer[]> voicesCoDNotes = transcription.getVoicesCoDNotes();
		List<List<Double>> voiceLabels = transcription.getVoiceLabels();
		List<Integer[]> meterInfo = tablature.getMeterInfo();
		// a. Fwd model	  
		// Not modelling duration
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISSFirst(basicTabSymbolProperties,
			durationLabels, voicesCoDNotes, null, transcription, note6, voiceLabels, 
			meterInfo, 6, false, false, 1));		
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISSFirst(basicTabSymbolProperties,	
			durationLabels, voicesCoDNotes, null, transcription, note23, voiceLabels, 
			meterInfo, 23, false, false, 1));
		// Modelling duration
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISSFirst(basicTabSymbolProperties,
			durationLabels, voicesCoDNotes, null, transcription, note6, voiceLabels, 
			meterInfo, 6, true, false, 1));
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISSFirst(basicTabSymbolProperties, 
			durationLabels, voicesCoDNotes, null, transcription, note23, voiceLabels, 
			meterInfo, 23, true, false, 1));
		// b. Bwd model
		// Not modelling duration
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISSFirst(basicTabSymbolProperties,
			durationLabels, voicesCoDNotes, null, transcription, note6, voiceLabels, 
			meterInfo, 6, false, true, 1));
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISSFirst(basicTabSymbolProperties,
			durationLabels, voicesCoDNotes, null, transcription, note23, voiceLabels, 
			meterInfo, 23, false, true, 1));
		// Modelling duration
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISSFirst(basicTabSymbolProperties, 
			durationLabels, voicesCoDNotes, null, transcription, note6, voiceLabels, 
			meterInfo, 6, true, true, 1));
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISSFirst(basicTabSymbolProperties, 
			durationLabels, voicesCoDNotes, null, transcription, note23, voiceLabels, 
			meterInfo, 23, true, true, 1));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}

	
	public void testGenerateNoteFeatureVectorDISSFirstNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		Note note7 = transcription.getNoteSequence().getNoteAt(7);
		Note note24 = transcription.getNoteSequence().getNoteAt(24);

		List<List<Double>> expected = new ArrayList<List<Double>>();
		// a. Fwd model
		// Chord 1, note at index 7
		List<Double> expFwd7 = new ArrayList<Double>(); 
		expFwd7.addAll(Arrays.asList(new Double[]{
			72.0, 1/4.0, 0.0, // note-level
			3.0, 3.0, -1.0, // note-chord
			4.0, 0/4.0, 1.0, 12.0, 12.0, 3.0, -1.0 // chord-level
		}));
		expFwd7.addAll(Arrays.asList(new Double[]{1.0/(3+1), 1.0/(7+1), 1.0/(15+1), 1.0/(22+1), -1.0})); // pitchProximities
		expFwd7.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), -1.0})); // interOnsetTimeProximities
		expFwd7.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0})); // offsetOnsetTimeProximities
		expFwd7.addAll(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied 
		expected.add(expFwd7);	
		// Chord 7, note at index 24 
		List<Double> expFwd24 = new ArrayList<Double>();
		expFwd24.addAll(Arrays.asList(new Double[]{
			59.0, 1/8.0, 0.0, // note-level
			2.0, 2.0, 9.0, // note-chord
			5.0, 7/8.0, 4.0, 12.0, 2.0, 9.0, 1.0 // chord-level
		}));
		expFwd24.addAll(Arrays.asList(new Double[]{1.0/(5+1), 1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(14+1)})); // pitchProximities
		expFwd24.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((3/8.0) + 1), 1/((1/8.0) + 1)})); // interOnsetTimeProximities
		expFwd24.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), -1/((1/8.0) + 1), 1/((0.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1)})); // offsetOnsetTimeProximities
		expFwd24.addAll(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 1.0, 1.0})); // voicesAlreadyOccupied
		expected.add(expFwd24);

		// b. Bwd model
		// Chord 1, note at index 7
		List<Double> expBwd7 = new ArrayList<Double>(); 
		expBwd7.addAll(Arrays.asList(new Double[]{
			72.0, 1/4.0, 0.0, // note-level
			3.0, 3.0, -1.0, // note-chord
			4.0, 0/4.0, 1.0, 12.0, 12.0, 3.0, -1.0 // chord-level
		}));
		expBwd7.addAll(Arrays.asList(new Double[]{1.0/(7+1), 1.0/(7+1), 1.0/(13+1), 1.0/(24+1), 1.0/(25+1)})); // pitchProximities
		expBwd7.addAll(Arrays.asList(new Double[]{-1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((3/16.0) + 1), -1/((1/4.0) + 1)})); // interOnsetTimeProximities 
		expBwd7.addAll(Arrays.asList(new Double[]{1/(0.0+1), 1/(0.0+1), 1/(0.0+1), 1/((1/16.0) + 1), 1/(0.0+1)})); // offsetOnsetTimeProximities
		expBwd7.addAll(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expBwd7);
		// Chord 7, note at index 24 
		List<Double> expBwd24 = new ArrayList<Double>();
		expBwd24.addAll(Arrays.asList(new Double[]{
			59.0, 1/8.0, 0.0,  // note-level
			2.0, 2.0, 9.0, // note-chord
			5.0, 7/8.0, 4.0, 12.0, 2.0, 9.0, 1.0 // chord-level
		}));
		expBwd24.addAll(Arrays.asList(new Double[]{1.0/(10+1), 1.0/(5+1), 1.0/(2+1), 1.0/(14+1), -1.0})); // pitchProximities
		expBwd24.addAll(Arrays.asList(new Double[]{-1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1.0})); // interOnsetTimeProximities
		expBwd24.addAll(Arrays.asList(new Double[]{1/(0.0 + 1), 1/(0.0 + 1), 1/(0.0 + 1), 1/(0.0 + 1), -1.0})); // offsetOnsetTimeProximities
		expBwd24.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expBwd24);

		Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
		List<List<Double>> voiceLabels = transcription.getVoiceLabels();
		List<Integer[]> meterInfo = transcription.getMeterInfo();

		List<List<Double>> actual = new ArrayList<List<Double>>();
		// a. Fwd model	  
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISSFirst(null, null, null, 
			basicNoteProperties, transcription, note7, voiceLabels, meterInfo, 7, true, 
			false, 1)); // value of argModelDuration irrelevant
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISSFirst(null, null, null, 
			basicNoteProperties, transcription, note24, voiceLabels, meterInfo, 24, true, 
			false, 1)); // value of argModelDuration irrelevant
		// b. Bwd model
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISSFirst(null, null, null, 
			basicNoteProperties, transcription, note7, voiceLabels, meterInfo, 7, true, 
			true, 1)); // value of argModelDuration irrelevant
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISSFirst(null, null, null, 
			basicNoteProperties, transcription, note24, voiceLabels, meterInfo, 24, true, 
			true, 1)); // value of argModelDuration irrelevant

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}
	
	// DRIE gedaan
	public void testGenerateNoteFeatureVectorDISS() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
		Note note6 = transcription.getNoteSequence().getNoteAt(6);
		Note note23 = transcription.getNoteSequence().getNoteAt(23);

		List<List<Double>> expected = new ArrayList<List<Double>>();
		// a. Fwd model
		// Not modelling duration (decisionContextSize = 1)
		// Chord 1, note at index 6
		List<Double> expFwdNoDur6 = new ArrayList<Double>(); 
		expFwdNoDur6.addAll(Arrays.asList(new Double[]{
			70.0, 2.0, 8.0, 1/4.0, 0.0, // note-level
			3.0, 3.0, -1.0, // note-chord
			4.0, 3/16.0, 0/4.0, 1.0, 12.0, 12.0, 3.0, -1.0, // chord-level
			0.0, 1.0, 0.0, 0.0, 0.0 // polyphonic embedding
		}));
		expFwdNoDur6.addAll(Arrays.asList(new Double[]{1.0/(3+1), 1.0/(7+1), 1.0/(15+1), 1.0/(22+1), -1.0})); // pitchProximities 
		expFwdNoDur6.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), -1.0})); // interOnsetTimeProximities 
		expFwdNoDur6.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0})); // offsetOnsetTimeProximities
		expFwdNoDur6.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expFwdNoDur6);
		// Chord 7, note at index 23
		List<Double> expFwdNoDur23 = new ArrayList<Double>(); 
		expFwdNoDur23.addAll(Arrays.asList(new Double[]{
			57.0, 3.0, 0.0, 9/8.0, 0.0, // note-level
			0.0, -1.0, 9.0, // note-chord
			2.0, 1/8.0, 7/8.0, 4.0, 9.0, -1.0, -1.0, -1.0, // chord-level
			0.0, 0.0, 1.0, 0.0, 0.0 // polyphonic embedding
		}));
		expFwdNoDur23.addAll(Arrays.asList(new Double[]{1.0/(5+1), 1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(14+1)})); // pitchProximities 
		expFwdNoDur23.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((3/8.0) + 1), 1/((1/8.0) + 1)})); // interOnsetTimeProximities
		expFwdNoDur23.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), 1/((1/8.0) + 1), 1/((0.0) + 1)})); // offsetOnsetTimeProximities  
		expFwdNoDur23.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expFwdNoDur23);
		
		// Not modelling duration (decisionContextSize = 3)
		// Chord 1, note at index 6
		List<Double> expFwdNoDurThree6 = new ArrayList<Double>(); 
		expFwdNoDurThree6.addAll(Arrays.asList(new Double[]{
			70.0, 2.0, 8.0, 1/4.0, 0.0, // note-level
			3.0, 3.0, -1.0, // note-chord
			4.0, 3/16.0, 0/4.0, 1.0, 12.0, 12.0, 3.0, -1.0, // chord-level
			0.0, 1.0, 0.0, 0.0, 0.0 // polyphonic embedding
		}));
		// Note n-1
		expFwdNoDurThree6.addAll(Arrays.asList(new Double[]{1.0/(3+1), 1.0/(7+1), 1.0/(15+1), 1.0/(22+1), -1.0})); // pitchProximities 
		expFwdNoDurThree6.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), -1.0})); // interOnsetTimeProximities 
		expFwdNoDurThree6.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0})); // offsetOnsetTimeProximities
		expFwdNoDurThree6.addAll(Arrays.asList(new Double[]{1.0, 1.0, 1.0, 1.0, -1.0})); // pitchMovements
		// Note n-2
		expFwdNoDurThree6.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0})); // pitchProximities 
		expFwdNoDurThree6.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0})); // interOnsetTimeProximities 
		expFwdNoDurThree6.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0})); // offsetOnsetTimeProximities
		expFwdNoDurThree6.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0})); // pitchMovements
		// Note n-3
		expFwdNoDurThree6.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0})); // pitchProximities 
		expFwdNoDurThree6.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0})); // interOnsetTimeProximities 
		expFwdNoDurThree6.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0})); // offsetOnsetTimeProximities
		expFwdNoDurThree6.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0})); // pitchMovements
		//
		expFwdNoDurThree6.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expFwdNoDurThree6);
		// Chord 7, note at index 23
		List<Double> expFwdNoDurThree23 = new ArrayList<Double>(); 
		expFwdNoDurThree23.addAll(Arrays.asList(new Double[]{
			57.0, 3.0, 0.0, 9/8.0, 0.0, // note-level
			0.0, -1.0, 9.0, // note-chord
			2.0, 1/8.0, 7/8.0, 4.0, 9.0, -1.0, -1.0, -1.0, // chord-level
			0.0, 0.0, 1.0, 0.0, 0.0 // polyphonic embedding
		}));
		// Note n-1
		expFwdNoDurThree23.addAll(Arrays.asList(new Double[]{1.0/(5+1), 1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(14+1)})); // pitchProximities 
		expFwdNoDurThree23.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((3/8.0) + 1), 1/((1/8.0) + 1)})); // interOnsetTimeProximities		
		expFwdNoDurThree23.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), 1/((1/8.0) + 1), 1/((0.0) + 1)})); // offsetOnsetTimeProximities  
		expFwdNoDurThree23.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 1.0})); // pitchMovements
		// Note n-2
		expFwdNoDurThree23.addAll(Arrays.asList(new Double[]{1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(9+1), 1.0/(14+1)})); // pitchProximities 
		expFwdNoDurThree23.addAll(Arrays.asList(new Double[]{1/((3/8.0) + 1), 1/((3/8.0) + 1), 1/((3/8.0) + 1), 1/((5/8.0) + 1), 1/((3/8.0) + 1)})); // interOnsetTimeProximities		
		expFwdNoDurThree23.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/2.0) + 1), 1/((1/8.0) + 1)})); // offsetOnsetTimeProximities  		
		expFwdNoDurThree23.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 1.0})); // pitchMovements
		// Note n-3
		expFwdNoDurThree23.addAll(Arrays.asList(new Double[]{1.0/(6+1), 1.0/(6+1), 1.0/(0+1), 1.0/(11+1), 1.0/(14+1)})); // pitchProximities 
		expFwdNoDurThree23.addAll(Arrays.asList(new Double[]{1/((5/8.0) + 1), 1/((5/8.0) + 1), 1/((5/8.0) + 1), 1/((11/16.0) + 1), 1/((1/2.0) + 1)})); // interOnsetTimeProximities		
		expFwdNoDurThree23.addAll(Arrays.asList(new Double[]{1/((1/2.0) + 1), 1/((1/2.0) + 1), 1/((1/2.0) + 1), 1/((5/8.0) + 1), 1/((3/8.0) + 1)})); // offsetOnsetTimeProximities  
		expFwdNoDurThree23.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 1.0})); // pitchMovements
		//
		expFwdNoDurThree23.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expFwdNoDurThree23);

		// Not modelling duration (decisionContextSize = 3, averaged)
		// Chord 1, note at index 6
		List<Double> expFwdNoDurThreeAvg6 = new ArrayList<Double>(); 
		expFwdNoDurThreeAvg6.addAll(Arrays.asList(new Double[]{
			70.0, 2.0, 8.0, 1/4.0, 0.0, // note-level
			3.0, 3.0, -1.0, // note-chord
			4.0, 3/16.0, 0/4.0, 1.0, 12.0, 12.0, 3.0, -1.0, // chord-level
			0.0, 1.0, 0.0, 0.0, 0.0 // polyphonic embedding
		}));
		expFwdNoDurThreeAvg6.addAll(Arrays.asList(new Double[]{1.0/(3+1), 1.0/(7+1), 1.0/(15+1), 1.0/(22+1), -1.0})); // pitchProximities 
		expFwdNoDurThreeAvg6.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), -1.0})); // interOnsetTimeProximities 
		expFwdNoDurThreeAvg6.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0})); // offsetOnsetTimeProximities
		expFwdNoDurThreeAvg6.addAll(Arrays.asList(new Double[]{3.0, 7.0, 15.0, 22.0, 0.0})); // pitchMovements
		expFwdNoDurThreeAvg6.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expFwdNoDurThreeAvg6);
		// Chord 7, note at index 23
		List<Double> expFwdNoDurThreeAvg23 = new ArrayList<Double>(); 
		expFwdNoDurThreeAvg23.addAll(Arrays.asList(new Double[]{
			57.0, 3.0, 0.0, 9/8.0, 0.0, // note-level
			0.0, -1.0, 9.0, // note-chord
			2.0, 1/8.0, 7/8.0, 4.0, 9.0, -1.0, -1.0, -1.0, // chord-level
			0.0, 0.0, 1.0, 0.0, 0.0 // polyphonic embedding
		}));
		expFwdNoDurThreeAvg23.addAll(Arrays.asList(new Double[]{
			( 1.0/(5+1) + 1.0/(10+1) + 1.0/(6+1) ) / 3.0,
			( 1.0/(10+1) + 1.0/(1+1) + 1.0/(6+1) ) / 3.0,
			( 1.0/(1+1) + 1.0/(2+1) + 1.0/(0+1) ) / 3.0,
			( 1.0/(2+1) + 1.0/(9+1) + 1.0/(11+1) ) / 3.0,
			( 1.0/(14+1) + 1.0/(14+1) + 1.0/(14+1) ) / 3.0
		})); // pitchProximities 
		expFwdNoDurThreeAvg23.addAll(Arrays.asList(new Double[]{
			( 1/((1/8.0)+1) + 1/((3/8.0)+1) + 1/((5/8.0)+1) ) / 3.0,
			( 1/((1/8.0)+1) + 1/((3/8.0)+1) + 1/((5/8.0)+1) ) / 3.0,
			( 1/((1/8.0)+1) + 1/((3/8.0)+1) + 1/((5/8.0)+1) ) / 3.0,
			( 1/((3/8.0)+1) + 1/((5/8.0)+1) + 1/((11/16.0)+1) ) / 3.0,
			( 1/((1/8.0)+1) + 1/((3/8.0)+1) + 1/((4/8.0)+1) ) / 3.0
		})); // interOnsetTimeProximities		
		expFwdNoDurThreeAvg23.addAll(Arrays.asList(new Double[]{
			( 1/(0+1) + 1/((1/8.0)+1) + 1/((4/8.0)+1) ) / 3.0,
			( 1/(0+1) + 1/((1/8.0)+1) + 1/((4/8.0)+1) ) / 3.0,
			( 1/(0+1) + 1/((1/8.0)+1) + 1/((4/8.0)+1) ) / 3.0,
			( 1/((1/8.0)+1) + 1/((4/8.0)+1) + 1/((5/8.0)+1) ) / 3.0,
			( 1/(0+1) + 1/((1/8.0)+1) + 1/((3/8.0)+1) ) / 3.0
		})); // offsetOnsetTimeProximities  
		expFwdNoDurThreeAvg23.addAll(Arrays.asList(new Double[]{
			(-5 + -10 + -6) / 3.0,
			(-10 + -1 + -6) / 3.0,
			(-1 + 2 + 0) / 3.0,
			(2 + 9 + 11) / 3.0,
			(14 + 14 + 14) / 3.0
		})); // pitchMovements
		expFwdNoDurThreeAvg23.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expFwdNoDurThreeAvg23);
		
		// Modelling duration (decisionContextSize = 1)
		// Chord 1, note at index 6
		List<Double> expFwdDur6 = new ArrayList<Double>(); 
		expFwdDur6.addAll(Arrays.asList(new Double[]{
			70.0, 2.0, 8.0, 1/4.0, 0.0, // note-level
			3.0, 3.0, -1.0, // note-chord
			4.0, 3/16.0, 0/4.0, 1.0, 12.0, 12.0, 3.0, -1.0, // chord-level
			0.0, 1.0, 0.0, 0.0, 0.0 // polyphonic embedding
		}));
		expFwdDur6.addAll(Arrays.asList(new Double[]{1.0/(3+1), 1.0/(7+1), 1.0/(15+1), 1.0/(22+1), -1.0})); // pitchProximities
		expFwdDur6.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), -1.0})); // interOnsetTimeProximities
		expFwdDur6.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0})); // offsetOnsetTimeProximities
		expFwdDur6.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expFwdDur6);
		// Chord 7, note at index 23
		List<Double> expFwdDur23 = new ArrayList<Double>();
		expFwdDur23.addAll(Arrays.asList(new Double[]{
			57.0, 3.0, 0.0, 9/8.0, 0.0, // note-level
			2.0, 2.0, 9.0, // note-chord
			5.0, 1/8.0, 7/8.0, 4.0, 12.0, 2.0, 9.0, 1.0, // chord-level
			0.0, 0.0, 1.0, 0.0, 0.0 // polyphonic embedding
		}));
		expFwdDur23.addAll(Arrays.asList(new Double[]{1.0/(5+1), 1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(14+1)})); // pitchProximities
		expFwdDur23.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((3/8.0) + 1), 1/((1/8.0) + 1)})); // interOnsetTimeProximities
		expFwdDur23.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), -1/((1/8.0) + 1), 1/((0.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1)})); // offsetOnsetTimeProximities
		expFwdDur23.addAll(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 1.0, 1.0})); // voicesAlreadyOccupied
		expected.add(expFwdDur23);

		// b. Bwd model 			
		// Not modelling duration (decisionContextSize = 1)
		// Chord 1, note at index 6
		List<Double> expBwdNoDur6 = new ArrayList<Double>(); 
		expBwdNoDur6.addAll(Arrays.asList(new Double[]{
			70.0, 2.0, 8.0, 1/4.0, 0.0, // note-level
			3.0, 3.0, -1.0, // note-chord
			4.0, 3/16.0, 0/4.0, 1.0, 12.0, 12.0, 3.0, -1.0, // chord-level
			1.0, 1.0, 0.0, 0.0, 0.0 // polyphonic embedding
		})); 
		expBwdNoDur6.addAll(Arrays.asList(new Double[]{1.0/(7+1), 1.0/(7+1), 1.0/(13+1), 1.0/(24+1), 1.0/(25+1)})); // pitchProximities	
 		expBwdNoDur6.addAll(Arrays.asList(new Double[]{-1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((3/16.0) + 1), -1/((1/4.0) + 1)})); // interOnsetTimeProximities
		expBwdNoDur6.addAll(Arrays.asList(new Double[]{-1/((1/16.0) + 1), -1/((1/16.0) + 1), -1/((1/16.0) + 1), 1.0/(0+1), -1/((1/16.0) + 1)})); // offsetOnsetTimeProximities
		expBwdNoDur6.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expBwdNoDur6);
		// Chord 7, note at index 23
		List<Double> expBwdNoDur23 = new ArrayList<Double>(); 
		expBwdNoDur23.addAll(Arrays.asList(new Double[]{
			57.0, 3.0, 0.0, 9/8.0, 0.0, // note-level
			0.0, -1.0, 9.0, // note-chord
			2.0, 1/8.0, 7/8.0, 4.0, 9.0, -1.0, -1.0, -1.0,// chord-level
			0.0, 0.0, 0.0, 0.0, 0.0 // polyphonic embedding
		}));
		expBwdNoDur23.addAll(Arrays.asList(new Double[]{1.0/(10+1), 1.0/(5+1), 1.0/(2+1), 1.0/(14+1), -1.0})); // pitchProximities 
		expBwdNoDur23.addAll(Arrays.asList(new Double[]{-1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1.0})); // interOnsetTimeProximities
		expBwdNoDur23.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), -1.0})); // offsetOnsetTimeProximities  
		expBwdNoDur23.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expBwdNoDur23);

		// Modelling duration (decisionContextSize = 1)
		// (same as expBwdNoDur because the full duration of the current note is not known)
		List<Double> expBwdDur6 = new ArrayList<Double>(expBwdNoDur6);
		expected.add(expBwdDur6);
		List<Double> expBwdDur23 = new ArrayList<Double>(expBwdNoDur23);
		expected.add(expBwdDur23);

		List<List<Double>> actual = new ArrayList<List<Double>>();
		List<List<Double>> durationLabels = transcription.getDurationLabels();
		List<Integer[]> voicesCoDNotes = transcription.getVoicesCoDNotes();
		List<List<Double>> voiceLabels = transcription.getVoiceLabels();
		List<Integer[]> meterInfo = tablature.getMeterInfo();
		// a. Fwd model	  
		// Not modelling duration (decisionContextSize = 1)
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(basicTabSymbolProperties,	
			durationLabels, voicesCoDNotes, null, transcription, note6, voiceLabels, meterInfo, 
			6, false, ProcessingMode.FWD, Runner.FeatureVector.PHD_D, 1, false));		
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(basicTabSymbolProperties,	
			durationLabels, voicesCoDNotes, null, transcription, note23, voiceLabels, meterInfo,
			23, false, ProcessingMode.FWD, Runner.FeatureVector.PHD_D, 1, false));
		// Not modelling duration (decisionContextSize = 3)
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(basicTabSymbolProperties,	
			durationLabels, voicesCoDNotes, null, transcription, note6, voiceLabels, meterInfo, 
			6, false, ProcessingMode.FWD, Runner.FeatureVector.PHD_D, 3, false));		
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(basicTabSymbolProperties,	
			durationLabels, voicesCoDNotes, null, transcription, note23, voiceLabels, meterInfo,
			23, false, ProcessingMode.FWD, Runner.FeatureVector.PHD_D, 3, false));
		// Not modelling duration (decisionContextSize = 3, averaged)
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(basicTabSymbolProperties,	
			durationLabels, voicesCoDNotes, null, transcription, note6, voiceLabels, meterInfo,
			6, false, ProcessingMode.FWD, Runner.FeatureVector.PHD_D, 3, true));
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(basicTabSymbolProperties,	
			durationLabels, voicesCoDNotes, null, transcription, note23, voiceLabels, meterInfo,
			23, false, ProcessingMode.FWD, Runner.FeatureVector.PHD_D, 3, true));
		// Modelling duration (decisionContextSize = 1)
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(basicTabSymbolProperties, 
			durationLabels, voicesCoDNotes, null, transcription, note6, voiceLabels, meterInfo,
			6, true, ProcessingMode.FWD, Runner.FeatureVector.PHD_D, 1, false));
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(basicTabSymbolProperties, 
			durationLabels, voicesCoDNotes, null, transcription, note23, voiceLabels, meterInfo,
			23, true, ProcessingMode.FWD, Runner.FeatureVector.PHD_D, 1, false));
		// b. Bwd model
		// Not modelling duration (decisionContextSize = 1)
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(basicTabSymbolProperties, 
			durationLabels, voicesCoDNotes, null, transcription, note6, voiceLabels, meterInfo,
			6, false, ProcessingMode.BWD, Runner.FeatureVector.PHD_D, 1, false));
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(basicTabSymbolProperties, 
			durationLabels, voicesCoDNotes, null, transcription, note23, voiceLabels, meterInfo,
			23, false, ProcessingMode.BWD, Runner.FeatureVector.PHD_D, 1, false));
		// Modelling duration (decisionContextSize = 1)
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(basicTabSymbolProperties, 
			durationLabels, voicesCoDNotes, null, transcription, note6, voiceLabels, meterInfo,
			6, true, ProcessingMode.BWD, Runner.FeatureVector.PHD_D, 1, false));
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(basicTabSymbolProperties, 
			durationLabels, voicesCoDNotes, null, transcription, note23, voiceLabels, meterInfo,
			23, true, ProcessingMode.BWD, Runner.FeatureVector.PHD_D, 1, false));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}


	public void testGenerateNoteFeatureVectorDISSNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		Note note7 = transcription.getNoteSequence().getNoteAt(7);
		Note note24 = transcription.getNoteSequence().getNoteAt(24);

		List<List<Double>> expected = new ArrayList<List<Double>>();
		// a. Fwd model
		// (decisionContextSize = 1)
		// Chord 1, note at index 7
		List<Double> expFwd7 = new ArrayList<Double>(); 
		expFwd7.addAll(Arrays.asList(new Double[]{
			72.0, 1/4.0, 0.0, // note-level
			3.0, 3.0, -1.0, // note-chord
			4.0, 0/4.0, 1.0, 12.0, 12.0, 3.0, -1.0 // chord-level
		}));
		expFwd7.addAll(Arrays.asList(new Double[]{1.0/(3+1), 1.0/(7+1), 1.0/(15+1), 1.0/(22+1), -1.0})); // pitchProximities
		expFwd7.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), -1.0})); // interOnsetTimeProximities
		expFwd7.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0})); // offsetOnsetTimeProximities
		expFwd7.addAll(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied 
		expected.add(expFwd7);	
		// Chord 7, note at index 24 
		List<Double> expFwd24 = new ArrayList<Double>();
		expFwd24.addAll(Arrays.asList(new Double[]{
			59.0, 1/8.0, 0.0, // note-level
			2.0, 2.0, 9.0, // note-chord
			5.0, 7/8.0, 4.0, 12.0, 2.0, 9.0, 1.0 // chord-level
		}));
		expFwd24.addAll(Arrays.asList(new Double[]{1.0/(5+1), 1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(14+1)})); // pitchProximities
		expFwd24.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((3/8.0) + 1), 1/((1/8.0) + 1)})); // interOnsetTimeProximities
		expFwd24.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), -1/((1/8.0) + 1), 1/((0.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1)})); // offsetOnsetTimeProximities
		expFwd24.addAll(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 1.0, 1.0})); // voicesAlreadyOccupied
		expected.add(expFwd24);

		// (decisionContextSize = 3)
		// Chord 1, note at index 7
		List<Double> expFwdThree7 = new ArrayList<Double>(); 
		expFwdThree7.addAll(Arrays.asList(new Double[]{
			72.0, 1/4.0, 0.0, // note-level
			3.0, 3.0, -1.0, // note-chord
			4.0, 0/4.0, 1.0, 12.0, 12.0, 3.0, -1.0 // chord-level
		}));
		// Note n-1
		expFwdThree7.addAll(Arrays.asList(new Double[]{1.0/(3+1), 1.0/(7+1), 1.0/(15+1), 1.0/(22+1), -1.0})); // pitchProximities
		expFwdThree7.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), -1.0})); // interOnsetTimeProximities
		expFwdThree7.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0})); // offsetOnsetTimeProximities
		expFwdThree7.addAll(Arrays.asList(new Double[]{1.0, 1.0, 1.0, 1.0, -1.0})); // pitchMovements
		// Note n-2
		expFwdThree7.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0})); // pitchProximities
		expFwdThree7.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0})); // interOnsetTimeProximities
		expFwdThree7.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0})); // offsetOnsetTimeProximities
		expFwdThree7.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0})); // pitchMovements
		// Note n-3
		expFwdThree7.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0})); // pitchProximities
		expFwdThree7.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0})); // interOnsetTimeProximities
		expFwdThree7.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0})); // offsetOnsetTimeProximities
		expFwdThree7.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0})); // pitchMovements
		//
		expFwdThree7.addAll(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied 
		expected.add(expFwdThree7);	
		// Chord 7, note at index 24 
		List<Double> expFwdThree24 = new ArrayList<Double>();
		expFwdThree24.addAll(Arrays.asList(new Double[]{
			59.0, 1/8.0, 0.0, // note-level
			2.0, 2.0, 9.0, // note-chord
			5.0, 7/8.0, 4.0, 12.0, 2.0, 9.0, 1.0 // chord-level
		}));
		// Note n-1
		expFwdThree24.addAll(Arrays.asList(new Double[]{1.0/(5+1), 1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(14+1)})); // pitchProximities
		expFwdThree24.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((3/8.0) + 1), 1/((1/8.0) + 1)})); // interOnsetTimeProximities
		expFwdThree24.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), -1/((1/8.0) + 1), 1/((0.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1)})); // offsetOnsetTimeProximities
		expFwdThree24.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 1.0})); // pitchMovements
		// Note n-2
		expFwdThree24.addAll(Arrays.asList(new Double[]{1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(9+1), 1.0/(14+1)})); // pitchProximities
		expFwdThree24.addAll(Arrays.asList(new Double[]{1/((3/8.0) + 1), 1/((3/8.0) + 1), 1/((3/8.0) + 1), 1/((5/8.0) + 1), 1/((3/8.0) + 1)})); // interOnsetTimeProximities
		expFwdThree24.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((3/8.0) + 1), 1/((1/8.0) + 1)})); // offsetOnsetTimeProximities
		expFwdThree24.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 1.0})); // pitchMovements
		// Note n-3
		expFwdThree24.addAll(Arrays.asList(new Double[]{1.0/(6+1), 1.0/(6+1), 1.0/(0+1), 1.0/(11+1), 1.0/(14+1)})); // pitchProximities
		expFwdThree24.addAll(Arrays.asList(new Double[]{1/((5/8.0) + 1), 1/((5/8.0) + 1), 1/((5/8.0) + 1), 1/((11/16.0) + 1), 1/((1/2.0) + 1)})); // interOnsetTimeProximities
		expFwdThree24.addAll(Arrays.asList(new Double[]{1/((3/8.0) + 1), 1/((1/2.0) + 1), 1/((3/8.0) + 1), 1/((5/8.0) + 1), 1/((3/8.0) + 1)})); // offsetOnsetTimeProximities
		expFwdThree24.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 1.0})); // pitchMovements
		//
		expFwdThree24.addAll(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 1.0, 1.0})); // voicesAlreadyOccupied
		expected.add(expFwdThree24);
		
		// (decisionContextSize = 3, averaged)
		// Chord 1, note at index 7
		List<Double> expFwdThreeAvg7 = new ArrayList<Double>(); 
		expFwdThreeAvg7.addAll(Arrays.asList(new Double[]{
			72.0, 1/4.0, 0.0, // note-level
			3.0, 3.0, -1.0, // note-chord
			4.0, 0/4.0, 1.0, 12.0, 12.0, 3.0, -1.0 // chord-level
		}));
		expFwdThreeAvg7.addAll(Arrays.asList(new Double[]{1.0/(3+1), 1.0/(7+1), 1.0/(15+1), 1.0/(22+1), -1.0})); // pitchProximities 
		expFwdThreeAvg7.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), -1.0})); // interOnsetTimeProximities 
		expFwdThreeAvg7.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0})); // offsetOnsetTimeProximities
		expFwdThreeAvg7.addAll(Arrays.asList(new Double[]{3.0, 7.0, 15.0, 22.0, 0.0})); // pitchMovements
		expFwdThreeAvg7.addAll(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expFwdThreeAvg7);
		// Chord 7, note at index 24
		List<Double> expFwdThreeAvg24 = new ArrayList<Double>(); 
		expFwdThreeAvg24.addAll(Arrays.asList(new Double[]{
			59.0, 1/8.0, 0.0, // note-level
			2.0, 2.0, 9.0, // note-chord
			5.0, 7/8.0, 4.0, 12.0, 2.0, 9.0, 1.0 // chord-level
		}));
		expFwdThreeAvg24.addAll(Arrays.asList(new Double[]{
			( 1.0/(5+1) + 1.0/(10+1) + 1.0/(6+1) ) / 3.0,
			( 1.0/(10+1) + 1.0/(1+1) + 1.0/(6+1) ) / 3.0,
			( 1.0/(1+1) + 1.0/(2+1) + 1.0/(0+1) ) / 3.0,
			( 1.0/(2+1) + 1.0/(9+1) + 1.0/(11+1) ) / 3.0,
			( 1.0/(14+1) + 1.0/(14+1) + 1.0/(14+1) ) / 3.0
		})); // pitchProximities 
		expFwdThreeAvg24.addAll(Arrays.asList(new Double[]{
			( 1/((1/8.0)+1) + 1/((3/8.0)+1) + 1/((5/8.0)+1) ) / 3.0,
			( 1/((1/8.0)+1) + 1/((3/8.0)+1) + 1/((5/8.0)+1) ) / 3.0,
			( 1/((1/8.0)+1) + 1/((3/8.0)+1) + 1/((5/8.0)+1) ) / 3.0,
			( 1/((3/8.0)+1) + 1/((5/8.0)+1) + 1/((11/16.0)+1) ) / 3.0,
			( 1/((1/8.0)+1) + 1/((3/8.0)+1) + 1/((4/8.0)+1) ) / 3.0
		})); // interOnsetTimeProximities		
		expFwdThreeAvg24.addAll(Arrays.asList(new Double[]{
			( 1/(0+1) + 1/((1/8.0)+1) + 1/((3/8.0)+1) ) / 3.0,
			( -1/((1/8.0)+1) + 1/((1/8.0)+1) + 1/((4/8.0)+1) ) / 3.0,
			( 1/(0+1) + 1/((1/8.0)+1) + 1/((3/8.0)+1) ) / 3.0,
			( -1/((1/8.0)+1) + 1/((3/8.0)+1) + 1/((5/8.0)+1) ) / 3.0,
			( -1/((1/8.0)+1) + 1/((1/8.0)+1) + 1/((3/8.0)+1) ) / 3.0
		})); // offsetOnsetTimeProximities  
		expFwdThreeAvg24.addAll(Arrays.asList(new Double[]{
			(-5 + -10 + -6) / 3.0,
			(-10 + -1 + -6) / 3.0,
			(-1 + 2 + 0) / 3.0,
			(2 + 9 + 11) / 3.0,
			(14 + 14 + 14) / 3.0
		})); // pitchMovements
		expFwdThreeAvg24.addAll(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 1.0, 1.0})); // voicesAlreadyOccupied
		expected.add(expFwdThreeAvg24);

		// b. Bwd model
		// (decisionContextSize = 1)
		// Chord 1, note at index 7
		List<Double> expBwd7 = new ArrayList<Double>(); 
		expBwd7.addAll(Arrays.asList(new Double[]{
			72.0, 1/4.0, 0.0, // note-level
			3.0, 3.0, -1.0, // note-chord
			4.0, 0/4.0, 1.0, 12.0, 12.0, 3.0, -1.0 // chord-level
		}));
		expBwd7.addAll(Arrays.asList(new Double[]{1.0/(7+1), 1.0/(7+1), 1.0/(13+1), 1.0/(24+1), 1.0/(25+1)})); // pitchProximities
		expBwd7.addAll(Arrays.asList(new Double[]{-1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((3/16.0) + 1), -1/((1/4.0) + 1)})); // interOnsetTimeProximities 
		expBwd7.addAll(Arrays.asList(new Double[]{1/(0.0+1), 1/(0.0+1), 1/(0.0+1), 1/((1/16.0) + 1), 1/(0.0+1)})); // offsetOnsetTimeProximities
		expBwd7.addAll(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expBwd7);
		// Chord 7, note at index 24 
		List<Double> expBwd24 = new ArrayList<Double>();
		expBwd24.addAll(Arrays.asList(new Double[]{
			59.0, 1/8.0, 0.0,  // note-level
			2.0, 2.0, 9.0, // note-chord
			5.0, 7/8.0, 4.0, 12.0, 2.0, 9.0, 1.0 // chord-level
		}));
		expBwd24.addAll(Arrays.asList(new Double[]{1.0/(10+1), 1.0/(5+1), 1.0/(2+1), 1.0/(14+1), -1.0})); // pitchProximities
		expBwd24.addAll(Arrays.asList(new Double[]{-1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1.0})); // interOnsetTimeProximities
		expBwd24.addAll(Arrays.asList(new Double[]{1/(0.0 + 1), 1/(0.0 + 1), 1/(0.0 + 1), 1/(0.0 + 1), -1.0})); // offsetOnsetTimeProximities
		expBwd24.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expBwd24);

		List<List<Double>> actual = new ArrayList<List<Double>>();
		Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
		List<List<Double>> voiceLabels = transcription.getVoiceLabels();
		List<Integer[]> meterInfo = transcription.getMeterInfo();
		// a. Fwd model
		// (decisionContextSize = 1)
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(null, null, null, 
			basicNoteProperties, transcription, note7, voiceLabels, meterInfo, 7, true, 
			ProcessingMode.FWD, Runner.FeatureVector.PHD_D, 1, false)); // value of argModelDuration irrelevant
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(null, null, null, 
			basicNoteProperties, transcription, note24, voiceLabels, meterInfo, 24, true, 
			ProcessingMode.FWD, Runner.FeatureVector.PHD_D, 1, false)); // value of argModelDuration irrelevant
		// (decisionContextSize = 3)
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(null, null, null, 
			basicNoteProperties, transcription, note7, voiceLabels, meterInfo, 7, true, 
			ProcessingMode.FWD, Runner.FeatureVector.PHD_D, 3, false)); // value of argModelDuration irrelevant
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(null, null, null, 
			basicNoteProperties, transcription, note24, voiceLabels, meterInfo, 24, true, 
			ProcessingMode.FWD, Runner.FeatureVector.PHD_D, 3, false)); // value of argModelDuration irrelevant
		// (decisionContextSize = 3, averaged)
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(null, null, null, 
			basicNoteProperties, transcription, note7, voiceLabels, meterInfo, 7, true, 
			ProcessingMode.FWD, Runner.FeatureVector.PHD_D, 3, true)); // value of argModelDuration irrelevant
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(null, null, null, 
			basicNoteProperties, transcription, note24, voiceLabels, meterInfo, 24, true, 
			ProcessingMode.FWD, Runner.FeatureVector.PHD_D, 3, true)); // value of argModelDuration irrelevant		
		
		// b. Bwd model
		// (decisionContextSize = 1)
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(null, null, null, 
			basicNoteProperties, transcription, note7, voiceLabels, meterInfo, 7, true, 
			ProcessingMode.BWD, Runner.FeatureVector.PHD_D, 1, false)); // value of argModelDuration irrelevant
		actual.add(FeatureGenerator.generateNoteFeatureVectorDISS(null, null, null, 
			basicNoteProperties, transcription, note24, voiceLabels, meterInfo, 24, true, 
			ProcessingMode.BWD, Runner.FeatureVector.PHD_D, 1, false)); // value of argModelDuration irrelevant

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}
	
	
	public void testGenerateMelodyFeatureVector() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		List<List<Double>> expected = new ArrayList<List<Double>>();
		expected.add(Arrays.asList(new Double[]{67.0, 1/4.0, 0.0, -1.0}));
		//
		expected.add(Arrays.asList(new Double[]{70.0, 3/16.0, 3.0, 1/4.0}));
		expected.add(Arrays.asList(new Double[]{63.0, 1/8.0, -7.0, 1/4.0}));
		expected.add(Arrays.asList(new Double[]{67.0, 1/4.0, 4.0, 1/4.0}));
		expected.add(Arrays.asList(new Double[]{62.0, 1/8.0, -5.0, 1/4.0}));
		expected.add(Arrays.asList(new Double[]{66.0, 1/8.0, 4.0, 1/8.0}));
		//
		expected.add(Arrays.asList(new Double[]{67.0, 1/16.0, 1.0, 1/8.0}));
		expected.add(Arrays.asList(new Double[]{66.0, 1/16.0, -1.0, 1/16.0}));
		expected.add(Arrays.asList(new Double[]{67.0, 1/32.0, 1.0, 1/16.0}));
		expected.add(Arrays.asList(new Double[]{66.0, 1/32.0, -1.0, 1/32.0}));
		expected.add(Arrays.asList(new Double[]{64.0, 1/32.0, -2.0, 1/32.0}));
		expected.add(Arrays.asList(new Double[]{66.0, 1/32.0, 2.0, 1/32.0}));
		expected.add(Arrays.asList(new Double[]{67.0, 1/4.0, 1.0, 1/32.0}));
		expected.add(Arrays.asList(new Double[]{67.0, 1/4.0, 0.0, 1/2.0}));

		// Calculate actual
		List<List<Double>> actual = new ArrayList<List<Double>>();
		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
		NotationVoice nv0 = transcription.getPiece().getScore().get(0).get(0);
		for (int i = 0; i < nv0.size(); i++) {
			Note n = nv0.get(i).get(0);
			actual.add(FeatureGenerator.generateMelodyFeatureVector(basicTabSymbolProperties, nv0, n));
		}
	  	
	  	// Assert equality
	  	assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}	
	}
		
		
	public void testGetMelodyFeatureVectorNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		// Determine expected
		List<List<Double>> expected = new ArrayList<List<Double>>();
		expected.add(Arrays.asList(new Double[]{69.0, 1/4.0, 0.0, -1.0}));
		//
		expected.add(Arrays.asList(new Double[]{72.0, 1/4.0, 3.0, 1/4.0}));
		expected.add(Arrays.asList(new Double[]{65.0, 1/4.0, -7.0, 1/4.0}));
		expected.add(Arrays.asList(new Double[]{69.0, 1/4.0, 4.0, 1/4.0}));
		expected.add(Arrays.asList(new Double[]{64.0, 1/8.0, -5.0, 1/4.0}));
		expected.add(Arrays.asList(new Double[]{68.0, 1/8.0, 4.0, 1/8.0}));
		//
		expected.add(Arrays.asList(new Double[]{69.0, 1/16.0, 1.0, 1/8.0}));
		expected.add(Arrays.asList(new Double[]{68.0, 1/16.0, -1.0, 1/16.0}));
		expected.add(Arrays.asList(new Double[]{69.0, 1/32.0, 1.0, 1/16.0}));
		expected.add(Arrays.asList(new Double[]{68.0, 1/32.0, -1.0, 1/32.0}));
		expected.add(Arrays.asList(new Double[]{66.0, 1/32.0, -2.0, 1/32.0}));
		expected.add(Arrays.asList(new Double[]{68.0, 1/32.0, 2.0, 1/32.0}));
		expected.add(Arrays.asList(new Double[]{69.0, 1/4.0, 1.0, 1/32.0}));
		expected.add(Arrays.asList(new Double[]{69.0, 1/4.0, 0.0, 1/2.0}));

		// Calculate actual
		List<List<Double>> actual = new ArrayList<List<Double>>();
		NotationVoice nv0 = transcription.getPiece().getScore().get(0).get(0);
		for (int i = 0; i < nv0.size(); i++) {
			Note n = nv0.get(i).get(0);
			actual.add(FeatureGenerator.generateMelodyFeatureVector(null, nv0, n));
		}

		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}	
	}


	public void testGenerateMelodyFeatureVectors() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);
	 
		// Determine expected
		List<List<Double>> expected = new ArrayList<List<Double>>();
		// a. If note == null
		// Complete voice 0
		List<List<Double>> expected0 = new ArrayList<List<Double>>();
		expected0.add(Arrays.asList(new Double[]{67.0, 1/4.0, 0.0, -1.0}));
		//
		expected0.add(Arrays.asList(new Double[]{70.0, 3/16.0, 3.0, 1/4.0}));
		expected0.add(Arrays.asList(new Double[]{63.0, 1/8.0, -7.0, 1/4.0}));
		expected0.add(Arrays.asList(new Double[]{67.0, 1/4.0, 4.0, 1/4.0}));
		expected0.add(Arrays.asList(new Double[]{62.0, 1/8.0, -5.0, 1/4.0}));
		expected0.add(Arrays.asList(new Double[]{66.0, 1/8.0, 4.0, 1/8.0}));
		//
		expected0.add(Arrays.asList(new Double[]{67.0, 1/16.0, 1.0, 1/8.0}));
		expected0.add(Arrays.asList(new Double[]{66.0, 1/16.0, -1.0, 1/16.0}));
		expected0.add(Arrays.asList(new Double[]{67.0, 1/32.0, 1.0, 1/16.0}));
		expected0.add(Arrays.asList(new Double[]{66.0, 1/32.0, -1.0, 1/32.0}));
		expected0.add(Arrays.asList(new Double[]{64.0, 1/32.0, -2.0, 1/32.0}));
		expected0.add(Arrays.asList(new Double[]{66.0, 1/32.0, 2.0, 1/32.0}));
		expected0.add(Arrays.asList(new Double[]{67.0, 1/4.0, 1.0, 1/32.0}));
		expected0.add(Arrays.asList(new Double[]{67.0, 1/4.0, 0.0, 1/2.0}));

		// b. If note != null
		// Voice 1, up to note 2
		List<List<Double>> expected1 = new ArrayList<List<Double>>();
		expected1.add(Arrays.asList(new Double[]{63.0, 1/4.0, 0.0, -1.0}));	  
		// Voice 2, up to note 25
		List<List<Double>> expected2 = new ArrayList<List<Double>>();
		expected2.add(Arrays.asList(new Double[]{55.0, 1/4.0, 0.0, -1.0}));
		expected2.add(Arrays.asList(new Double[]{55.0, 3/16.0, 0.0, 1/4.0}));
		expected2.add(Arrays.asList(new Double[]{57.0, 1/8.0, 2.0, 1/4.0}));
		expected2.add(Arrays.asList(new Double[]{55.0, 1/4.0, -2.0, 1/4.0}));
		expected2.add(Arrays.asList(new Double[]{58.0, 1/8.0, 3.0, 1/4.0}));
		expected2.add(Arrays.asList(new Double[]{57.0, 1/8.0, -1.0, 1/8.0}));
		expected2.add(Arrays.asList(new Double[]{43.0, 1/16.0, -14.0, 1/8.0}));

		expected.addAll(expected0); expected.addAll(expected1); expected.addAll(expected2);

		// Calculate actual
		List<List<Double>> actual = new ArrayList<List<Double>>();
		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
		NotationSystem system = transcription.getPiece().getScore(); 
		NotationVoice nv0 = system.get(0).get(0);
		NotationVoice nv1 = system.get(1).get(0);
		NotationVoice nv2 = system.get(2).get(0);
		NotationVoice nv3 = system.get(3).get(0);
		// a. If note == null
		actual.addAll(FeatureGenerator.generateMelodyFeatureVectors(basicTabSymbolProperties, nv0, null));
		// b. If note != null
		Note note2 = nv1.get(0).get(0);
		actual.addAll(FeatureGenerator.generateMelodyFeatureVectors(basicTabSymbolProperties, nv0, note2));
		Note note25 = nv3.get(5).get(0); 
		actual.addAll(FeatureGenerator.generateMelodyFeatureVectors(basicTabSymbolProperties, nv2, note25));

		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}


	public void testGenerateMelodyFeatureVectorsNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		// Determine expected
		List<List<Double>> expected = new ArrayList<List<Double>>();
		// a. If note == null
		// Complete voice 0
		List<List<Double>> expected0 = new ArrayList<List<Double>>();
		expected0.add(Arrays.asList(new Double[]{69.0, 1/4.0, 0.0, -1.0}));
		//
		expected0.add(Arrays.asList(new Double[]{72.0, 1/4.0, 3.0, 1/4.0}));
		expected0.add(Arrays.asList(new Double[]{65.0, 1/4.0, -7.0, 1/4.0}));
		expected0.add(Arrays.asList(new Double[]{69.0, 1/4.0, 4.0, 1/4.0}));
		expected0.add(Arrays.asList(new Double[]{64.0, 1/8.0, -5.0, 1/4.0}));
		expected0.add(Arrays.asList(new Double[]{68.0, 1/8.0, 4.0, 1/8.0}));
		//
		expected0.add(Arrays.asList(new Double[]{69.0, 1/16.0, 1.0, 1/8.0}));
		expected0.add(Arrays.asList(new Double[]{68.0, 1/16.0, -1.0, 1/16.0}));
		expected0.add(Arrays.asList(new Double[]{69.0, 1/32.0, 1.0, 1/16.0}));
		expected0.add(Arrays.asList(new Double[]{68.0, 1/32.0, -1.0, 1/32.0}));
		expected0.add(Arrays.asList(new Double[]{66.0, 1/32.0, -2.0, 1/32.0}));
		expected0.add(Arrays.asList(new Double[]{68.0, 1/32.0, 2.0, 1/32.0}));
		expected0.add(Arrays.asList(new Double[]{69.0, 1/4.0, 1.0, 1/32.0}));
		expected0.add(Arrays.asList(new Double[]{69.0, 1/4.0, 0.0, 1/2.0}));
	
		// b. If note != null
		// Voice 1, up to note 2
		List<List<Double>> expected1 = new ArrayList<List<Double>>();
		expected1.add(Arrays.asList(new Double[]{65.0, 1/4.0, 0.0, -1.0}));	  
		// Voice 2, up to note 26
		List<List<Double>> expected2 = new ArrayList<List<Double>>();
		expected2.add(Arrays.asList(new Double[]{57.0, 1/4.0, 0.0, -1.0}));
		expected2.add(Arrays.asList(new Double[]{57.0, 1/4.0, 0.0, 1/4.0}));
		expected2.add(Arrays.asList(new Double[]{59.0, 1/4.0, 2.0, 1/4.0}));
		expected2.add(Arrays.asList(new Double[]{57.0, 1/4.0, -2.0, 1/4.0}));
		expected2.add(Arrays.asList(new Double[]{60.0, 1/8.0, 3.0, 1/4.0}));
		expected2.add(Arrays.asList(new Double[]{59.0, 1/8.0, -1.0, 1/8.0}));
		expected2.add(Arrays.asList(new Double[]{45.0, 1/2.0, -14.0, 1/8.0}));

		expected.addAll(expected0); expected.addAll(expected1); expected.addAll(expected2);

		// Calculate actual
		List<List<Double>> actual = new ArrayList<List<Double>>();
		NotationSystem system = transcription.getPiece().getScore(); 
		NotationVoice nv0 = system.get(0).get(0);
		NotationVoice nv1 = system.get(1).get(0);
		NotationVoice nv2 = system.get(2).get(0);
		NotationVoice nv3 = system.get(3).get(0);
		// a. If note == null
		actual.addAll(FeatureGenerator.generateMelodyFeatureVectors(null, nv0, null));
		// b. If note != null
		Note note2 = nv1.get(0).get(0);
		actual.addAll(FeatureGenerator.generateMelodyFeatureVectors(null, nv0, note2));
		Note note26 = nv3.get(5).get(0); 
		actual.addAll(FeatureGenerator.generateMelodyFeatureVectors(null, nv2, note26));
		
		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}


	public void testGenerateBidirectionalNoteFeatureVector() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription gtTranscription = new Transcription(midiTestpiece1, encodingTestpiece1);

		// Create predicted Lists
		List<List<Double>> predVoiceLabels = gtTranscription.getVoiceLabels();
		List<List<Double>> predDurationLabels = gtTranscription.getDurationLabels();
//		List<Integer[]> predVoicesCoDNotes = gtTranscription.getVoicesCoDNotes();
		// Chord 0: note 0 becomes CoD (voices 3 and 4) of length 1/8; notes 2 and 3 swap voice 
		predVoiceLabels.set(0, Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 1.0}));
		predDurationLabels.set(0, Transcription.EIGHTH);
//		predDurationLabels.set(0, Transcription.createDurationLabel(4));
		Collections.swap(predVoiceLabels, 2, 3);
		// Chord 1: note 6 gets length 1/8
		predDurationLabels.set(6, Transcription.EIGHTH);
//		predDurationLabels.set(6, Transcription.createDurationLabel(4));
		// Chord 3: notes 9 and 10 swap voice and note 10 gets length 1/8
		Collections.swap(predVoiceLabels, 9, 10);
		predDurationLabels.set(10, Transcription.EIGHTH);
//		predDurationLabels.set(10, Transcription.createDurationLabel(4));

		// Create predicted Transcription
		Integer[][] btp = tablature.getBasicTabSymbolProperties();
//		Piece predictedPiece = tablature.createPiece(predVoiceLabels, predDurationLabels, 5);
//		Piece predictedPiece = Transcription.createPiece(btp, null, predVoiceLabels, predDurationLabels, 5);
//		Transcription predictedTranscription = new Transcription(midiTestpiece1, encodingTestpiece1, predictedPiece,
//			predVoiceLabels, predDurationLabels/*, null*/);

		Transcription predTranscription = 
			new Transcription(midiTestpiece1.getName(), encodingTestpiece1, btp, null, 5,
			predVoiceLabels, predDurationLabels, 
			gtTranscription.getPiece().getMetricalTimeLine(), 
			gtTranscription.getPiece().getHarmonyTrack());

//		MIDIExport.exportMidiFile(predTranscription.getPiece(), 
//			Arrays.asList(new Integer[]{56}), "C:/Users/Reinier/Desktop/bla.mid");
//		MIDIExport.exportMidiFile(predictedTranscription.getUnadaptedGTPiece(), 
//			Arrays.asList(new Integer[]{56}), "C:/Users/Reinier/Desktop/blaUn.mid");

		Note note6 = predTranscription.getNoteSequence().getNoteAt(6);
		Note note23 = predTranscription.getNoteSequence().getNoteAt(23);

		List<List<Double>> expected = new ArrayList<List<Double>>(); 
		// a. Not modelling duration
		// Chord 1, note at index 6
		// 1. Note-level, note-chord, and chord-level features
		List<Double> expected6NonDur = new ArrayList<Double>(); 
		expected6NonDur.addAll(Arrays.asList(new Double[]{
			70.0, 2.0, 8.0, 1/4.0, 0.0, // note-level
			3.0, 3.0, -1.0, // note-chord
			4.0, 3/16.0, 0/4.0, 1.0, 12.0, 12.0, 3.0, -1.0 // chord-level
		}));
		// 2. Polyphonic embedding features
		// To previous notes
		expected6NonDur.addAll(Arrays.asList(new Double[]{1.0, 0.0, 0.0, 0.0, 0.0})); // voicesWithAdjacentNoteOnSameCourse
		expected6NonDur.addAll(Arrays.asList(new Double[]{1.0/(7+1), 1.0/(3+1), 1.0/(15+1), 1.0/(22+1), 1.0/(22+1)})); // pitchProximities 
		expected6NonDur.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1)})); // interOnsetTimeProximities
		expected6NonDur.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/((0+1))})); // offsetOnsetTimeProximities  
		// To next notes
		expected6NonDur.addAll(Arrays.asList(new Double[]{1.0, 1.0, 0.0, 0.0, 0.0})); // voicesWithAdjacentNoteOnSameCourse
		expected6NonDur.addAll(Arrays.asList(new Double[]{1.0/(7+1), 1.0/(7+1), 1.0/(13+1), 1.0/(24+1), 1.0/(22+1)})); // pitchProximities 
		expected6NonDur.addAll(Arrays.asList(new Double[]{-1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((3/16.0) + 1), -1/((1/4.0) + 1)})); // interOnsetTimeProximities
		expected6NonDur.addAll(Arrays.asList(new Double[]{-1/((1/16.0) +1), -1/((1/16.0) +1), -1/((1/16.0) +1), 1.0/(0+1), -1/((1/16.0) +1)})); // offsetOnsetTimeProximities  
		// voicesAlreadyOccupied
		expected6NonDur.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0})); 
		expected.add(expected6NonDur);

		// Chord 7, note at index 23
		// 1. Note-level, note-chord, and chord-level features
		List<Double> expected23NonDur = new ArrayList<Double>(); 
		expected23NonDur.addAll(Arrays.asList(new Double[]{
			57.0, 3.0, 0.0, 9/8.0, 0.0, // note-level
			0.0, -1.0, 9.0, // note-chord
			2.0, 1/8.0, 7/8.0, 4.0, 9.0, -1.0, -1.0, -1.0 // chord-level
		}));
		// 2. Polyphonic embedding features
		// To previous notes
		expected23NonDur.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 0.0, 0.0})); // voicesWithAdjacentNoteOnSameCourse
		expected23NonDur.addAll(Arrays.asList(new Double[]{1.0/(5+1), 1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(14+1)})); // pitchProximities 
		expected23NonDur.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((3/8.0) + 1), 1/((1/8.0) + 1)})); // interOnsetTimeProximities
		expected23NonDur.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1/((1/8.0) + 1), 1.0/(0+1)})); // offsetOnsetTimeProximities
		// To next notes
		expected23NonDur.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0})); // voicesWithAdjacentNoteOnSameCourse
		expected23NonDur.addAll(Arrays.asList(new Double[]{1.0/(10+1), 1.0/(5+1), 1.0/(2+1), 1.0/(14+1), -1.0})); // pitchProximities 
		expected23NonDur.addAll(Arrays.asList(new Double[]{-1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1.0})); // interOnsetTimeProximities 
		expected23NonDur.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), -1.0})); // offsetOnsetTimeProximities  
		expected23NonDur.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expected23NonDur);
		
		// b. Modelling duration
		// Chord 1, note at index 6
		// 1. Note-level, note-chord, and chord-level features
		List<Double> expected6Dur = new ArrayList<Double>(); 
		expected6Dur.addAll(Arrays.asList(new Double[]{
			70.0, 2.0, 8.0, 1/4.0, 0.0, // note-level
			3.0, 3.0, -1.0, // note-chord
			4.0, 3/16.0, 0/4.0, 1.0, 12.0, 12.0, 3.0, -1.0 // chord-level
		}));
		// 2. Polyphonic embedding features
		// To previous notes
		expected6Dur.addAll(Arrays.asList(new Double[]{1.0, 0.0, 0.0, 0.0, 0.0})); // voicesWithAdjacentNoteOnSameCourse
		expected6Dur.addAll(Arrays.asList(new Double[]{1.0/(7+1), 1.0/(3+1), 1.0/(15+1), 1.0/(22+1), 1.0/(22+1)})); // pitchProximities 
		expected6Dur.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1)})); // interOnsetTimeProximities
		expected6Dur.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/((1/8.0) + 1), 1.0/((1/8.0) + 1)})); // offsetOnsetTimeProximities  
		// To next notes
		expected6Dur.addAll(Arrays.asList(new Double[]{1.0, 1.0, 0.0, 0.0, 0.0})); // voicesWithAdjacentNoteOnSameCourse
		expected6Dur.addAll(Arrays.asList(new Double[]{1.0/(7+1), 1.0/(7+1), 1.0/(13+1), 1.0/(24+1), 1.0/(22+1)})); // pitchProximities 
		expected6Dur.addAll(Arrays.asList(new Double[]{-1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((3/16.0) + 1), -1/((1/4.0) + 1)})); // interOnsetTimeProximities
		expected6Dur.addAll(Arrays.asList(new Double[]{-1/((1/8.0) +1), -1/((1/8.0) +1), -1/((1/8.0) +1), -1/((1/16.0) + 1), -1/((1/8.0) +1)})); // offsetOnsetTimeProximities  
		expected6Dur.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expected6Dur);

		// Chord 7, note at index 23
		// 1. Note-level, note-chord, and chord-level features
		List<Double> expected23Dur = new ArrayList<Double>(); 
		expected23Dur.addAll(Arrays.asList(new Double[]{
			57.0, 3.0, 0.0, 9/8.0, 0.0, // note-level
			2.0, 2.0, 9.0, // note-chord
			5.0, 1/8.0, 7/8.0, 4.0, 12.0, 2.0, 9.0, 1.0 // chord-level
		}));
		// 2. Polyphonic embedding features
		// To previous notes
		expected23Dur.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 0.0, 0.0})); // voicesWithAdjacentNoteOnSameCourse
		expected23Dur.addAll(Arrays.asList(new Double[]{1.0/(5+1), 1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(14+1)})); // pitchProximities 
		expected23Dur.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((3/8.0) + 1), 1/((1/8.0) + 1)})); // interOnsetTimeProximities
		expected23Dur.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), -1/((1/8.0) + 1), 1/((0.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1)})); // offsetOnsetTimeProximities
		// To next notes
		expected23Dur.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0})); // voicesWithAdjacentNoteOnSameCourse
		expected23Dur.addAll(Arrays.asList(new Double[]{1.0/(10+1), 1.0/(5+1), 1.0/(2+1), 1.0/(14+1), -1.0})); // pitchProximities 
		expected23Dur.addAll(Arrays.asList(new Double[]{-1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1.0})); // interOnsetTimeProximities 
		expected23Dur.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), -1.0})); // offsetOnsetTimeProximities  
		expected23Dur.addAll(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 1.0, 1.0})); // voicesAlreadyOccupied
		expected.add(expected23Dur);

		List<List<Double>> actual = new ArrayList<List<Double>>();
		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
		predDurationLabels = predTranscription.getDurationLabels();
		List<Integer[]> predVoicesCoDNotes = predTranscription.getVoicesCoDNotes();
		predVoiceLabels = predTranscription.getVoiceLabels();
		List<Integer[]> meterInfo = tablature.getMeterInfo();
		boolean modelDuration = false;
		actual.add(FeatureGenerator.generateBidirectionalNoteFeatureVector(
			basicTabSymbolProperties, predDurationLabels, predVoicesCoDNotes, null, 
			predTranscription, note6, predVoiceLabels, meterInfo, 6, modelDuration, 1, false));
		actual.add(FeatureGenerator.generateBidirectionalNoteFeatureVector(
			basicTabSymbolProperties, predDurationLabels, predVoicesCoDNotes, null, 
			predTranscription, note23, predVoiceLabels, meterInfo, 23, modelDuration, 1, false));
		modelDuration = true;
		actual.add(FeatureGenerator.generateBidirectionalNoteFeatureVector(
			basicTabSymbolProperties, predDurationLabels, predVoicesCoDNotes, null, 
			predTranscription, note6, predVoiceLabels, meterInfo, 6, modelDuration, 1, false));
		actual.add(FeatureGenerator.generateBidirectionalNoteFeatureVector(
			basicTabSymbolProperties, predDurationLabels, predVoicesCoDNotes, null, 
			predTranscription, note23, predVoiceLabels, meterInfo, 23, modelDuration, 1, false));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}


	public void testGenerateBidirectionalNoteFeatureVectorNonTab() {
		Transcription gtTranscription = new Transcription(midiTestpiece1, null);

		// Create predicted Lists
		List<List<Double>> predVoiceLabels = gtTranscription.getVoiceLabels();
		List<Integer[]> predEDUInfo = gtTranscription.getEqualDurationUnisonsInfo();
		// Chord 0: note 0 goes to voice 4; notes 2 and 3 swap voice 
		predVoiceLabels.set(0, Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 1.0}));
		Collections.swap(predVoiceLabels, 2, 3);
		// Chord 3: notes 9 and 10 swap voice
		Collections.swap(predVoiceLabels, 9, 10);

		// Create predicted Transcription
		Integer[][] bnp = gtTranscription.getBasicNoteProperties();
//		Piece predictedPiece = Transcription.createPiece(null, bnp, predVoiceLabels, null, 5);
//		Piece predictedPiece = gtTranscription.createPiece(predVoiceLabels, 5);
//		Transcription predictedTranscription = new Transcription(midiTestpiece1, null, predictedPiece,
//			predVoiceLabels, null/*, predEDUInfo*/);
		Transcription predictedTranscription = 
			new Transcription(midiTestpiece1.getName(), null, null, bnp, 5, predVoiceLabels, null, 
			gtTranscription.getPiece().getMetricalTimeLine(), 
			gtTranscription.getPiece().getHarmonyTrack());

		Note note7 = predictedTranscription.getNoteSequence().getNoteAt(7);
		Note note24 = predictedTranscription.getNoteSequence().getNoteAt(24);

		List<List<Double>> expected = new ArrayList<List<Double>>(); 
		// Chord 1, note at index 7
		// 1. Note-level, note-chord, and chord-level features
		List<Double> expected7 = new ArrayList<Double>(); 
		expected7.addAll(Arrays.asList(new Double[]{
			72.0, 1/4.0, 0.0, // note-level
			3.0, 3.0, -1.0, // note-chord
			4.0, 0/4.0, 1.0, 12.0, 12.0, 3.0, -1.0 // chord-level
		}));
		// 2. Polyphonic embedding features
		// To previous notes
		expected7.addAll(Arrays.asList(new Double[]{1.0/(7+1), 1.0/(3+1), 1.0/(15+1), -1.0, 1.0/(22+1)})); // pitchProximities 
		expected7.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), -1.0, 1/((1/4.0) + 1)})); // interOnsetTimeProximities
		expected7.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0, 1.0/(0+1)})); // offsetOnsetTimeProximities  
		// To next notes
		expected7.addAll(Arrays.asList(new Double[]{1.0/(7+1), 1.0/(7+1), 1.0/(13+1), 1.0/(24+1), 1.0/(22+1)})); // pitchProximities 
		expected7.addAll(Arrays.asList(new Double[]{-1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((3/16.0) + 1), -1/((1/4.0) + 1)})); // interOnsetTimeProximities
		expected7.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1/((1/16.0) + 1), 1.0/(0+1)})); // offsetOnsetTimeProximities  
		expected7.addAll(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expected7);

		// Chord 7, note at index 24
		// 1. Note-level, note-chord, and chord-level
		List<Double> expected24 = new ArrayList<Double>(); 
		expected24.addAll(Arrays.asList(new Double[]{
			59.0, 1/8.0, 0.0, // note-level
			2.0, 2.0, 9.0, // note-chord
			5.0, 7/8.0, 4.0, 12.0, 2.0, 9.0, 1.0// chord-level
		}));
		// 2. Polyphonic embedding features
		// To previous notes
		expected24.addAll(Arrays.asList(new Double[]{1.0/(5+1), 1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(14+1)})); // pitchProximities 
		expected24.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((3/8.0) + 1), 1/((1/8.0) + 1)})); // interOnsetTimeProximities
		expected24.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), -1/((1/8.0) + 1), 1/((0.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1)})); // offsetOnsetTimeProximities
		// To next notes
		expected24.addAll(Arrays.asList(new Double[]{1.0/(10+1), 1.0/(5+1), 1.0/(2+1), 1.0/(14+1), -1.0})); // pitchProximities 
		expected24.addAll(Arrays.asList(new Double[]{-1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1.0})); // interOnsetTimeProximities 
		expected24.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), -1.0})); // offsetOnsetTimeProximities  
		expected24.addAll(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 1.0, 1.0})); // voicesAlreadyOccupied
		expected.add(expected24);

		List<List<Double>> actual = new ArrayList<List<Double>>();
		Integer[][] basicNoteProperties = gtTranscription.getBasicNoteProperties();
		predVoiceLabels = predictedTranscription.getVoiceLabels();
		List<Integer[]> meterInfo = gtTranscription.getMeterInfo();
		boolean modelDuration = false;
		actual.add(FeatureGenerator.generateBidirectionalNoteFeatureVector(null, null, null,
			basicNoteProperties, predictedTranscription, note7, predVoiceLabels, meterInfo,
			7, modelDuration, 1, false));
		actual.add(FeatureGenerator.generateBidirectionalNoteFeatureVector(null, null, null, 
			basicNoteProperties, predictedTranscription, note24, predVoiceLabels, meterInfo, 
			24, modelDuration, 1, false));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}
	
	
	public void testGenerateBidirectionalNoteFeatureVectorOLD() {
		Tablature tablature = new Tablature(encodingTestpiece1, true);
		Transcription gtTranscription = new Transcription(midiTestpiece1, encodingTestpiece1);

		// Create predicted Lists
		List<List<Double>> predVoiceLabels = gtTranscription.getVoiceLabels();
		List<List<Double>> predDurationLabels = gtTranscription.getDurationLabels();
//		List<Integer[]> predVoicesCoDNotes = gtTranscription.getVoicesCoDNotes();
		// Chord 0: note 0 becomes CoD (voices 3 and 4) of length 1/8; notes 2 and 3 swap voice 
		predVoiceLabels.set(0, Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 1.0}));
		predDurationLabels.set(0, Transcription.EIGHTH);
//		predDurationLabels.set(0, Transcription.createDurationLabel(4));
		Collections.swap(predVoiceLabels, 2, 3);
		// Chord 1: note 6 gets length 1/8
		predDurationLabels.set(6, Transcription.EIGHTH);
//		predDurationLabels.set(6, Transcription.createDurationLabel(4));
		// Chord 3: notes 9 and 10 swap voice and note 10 gets length 1/8
		Collections.swap(predVoiceLabels, 9, 10);
		predDurationLabels.set(10, Transcription.EIGHTH);
//		predDurationLabels.set(10, Transcription.createDurationLabel(4));

		// Create predicted Transcription
		Integer[][] btp = tablature.getBasicTabSymbolProperties();
//		Piece predictedPiece = Transcription.createPiece(btp, null, predVoiceLabels, predDurationLabels, 5);
//		Transcription predictedTranscription = new Transcription(midiTestpiece1, encodingTestpiece1, predictedPiece,
//			predVoiceLabels, predDurationLabels/*, null*/);		
		Transcription predictedTranscription = 
			new Transcription(midiTestpiece1.getName(), encodingTestpiece1, btp, null, 5, 
			predVoiceLabels, predDurationLabels,
			gtTranscription.getPiece().getMetricalTimeLine(), 
			gtTranscription.getPiece().getHarmonyTrack()
		);		

		Note note6 = predictedTranscription.getNoteSequence().getNoteAt(6);
		Note note23 = predictedTranscription.getNoteSequence().getNoteAt(23);

		List<List<Double>> expected = new ArrayList<List<Double>>(); 
		// Chord 1, note at index 6
		// 1. Tablature features
		List<Double> expected6 = new ArrayList<Double>(); 
		expected6.addAll(Arrays.asList(new Double[]{
			70.0, 2.0, 8.0, 3/16.0, 1/4.0, 0.0, 0/4.0, // basicNoteFeatures
			1.0/((3/16.0) + 1), 1.0/(24+1), 6.0, -1.0, -1.0, -1.0, // proximitiesAndCourseInfoAhead
			1.0/((1/4.0) + 1), 1.0/(7+1), 2.0, -1.0, -1.0, -1.0, 
			1.0/((3/8.0) + 1), 1.0/(27+1), 6.0, -1.0, -1.0, -1.0,
			1.0 // numberOfNewOnsetsNextChord
		}));
		// 2. Tablature + duration features
		expected6.addAll(Arrays.asList(new Double[]{4.0, 3.0, 3.0, -1.0, 12.0, 12.0, 3.0, -1.0})); // positionWithinChord
		// 3. Tablature + duration + voice features
		// To previous notes
		expected6.addAll(Arrays.asList(new Double[]{1.0, 0.0, 0.0, 0.0, 0.0})); // voicesWithAdjacentNoteOnSameCourse
		expected6.addAll(Arrays.asList(new Double[]{1.0/(7+1), 1.0/(3+1), 1.0/(15+1), 1.0/(22+1), 1.0/(22+1)})); // pitchProximities 
		expected6.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1)})); // interOnsetTimeProximities
		expected6.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/((1/8.0) + 1), 1.0/((1/8.0) + 1)})); // offsetOnsetTimeProximities  
		// To next notes
		expected6.addAll(Arrays.asList(new Double[]{1.0, 1.0, 0.0, 0.0, 0.0})); // voicesWithAdjacentNoteOnSameCourse
		expected6.addAll(Arrays.asList(new Double[]{1.0/(7+1), 1.0/(7+1), 1.0/(13+1), 1.0/(24+1), 1.0/(22+1)})); // pitchProximities 
		expected6.addAll(Arrays.asList(new Double[]{-1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((3/16.0) + 1), -1/((1/4.0) + 1)})); // interOnsetTimeProximities
		expected6.addAll(Arrays.asList(new Double[]{-1/((1/8.0) +1), -1/((1/8.0) +1), -1/((1/8.0) +1), -1/((1/16.0) + 1), -1/((1/8.0) +1)})); // offsetOnsetTimeProximities  
		expected6.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expected6);

		// Chord 7, note at index 23
		// 1. Tablature features
		List<Double> expected23 = new ArrayList<Double>(); 
		expected23.addAll(Arrays.asList(new Double[]{
			57.0, 3.0, 0.0, 1/8.0, 9/8.0, 0.0, 7/8.0, // basicNoteFeatures
			1.0/((1/8.0) + 1), 1.0/(2+1), 4.0, -1.0, 1.0/(5+1), 2.0, // proximitiesAndCourseInfoAhead
			1.0/((3/16.0) + 1), -1.0, -1.0, -1.0, 1.0/(9+1), 2.0, 
			1.0/((1/4.0) + 1), -1.0, -1.0, -1.0, 1.0/(10+1), 1.0,  
			4.0, // numberOfNewOnsetsNextChord
		}));
		// 2. Tablature + duration features
		expected23.addAll(Arrays.asList(new Double[]{5.0, 2.0, 2.0, 9.0, 12.0, 2.0, 9.0, 1.0})); // positionWithinChord		
		// 3. Tablature + duration + voice features
		// To previous notes
		expected23.addAll(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 0.0, 0.0})); // voicesWithAdjacentNoteOnSameCourse
		expected23.addAll(Arrays.asList(new Double[]{1.0/(5+1), 1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(14+1)})); // pitchProximities 
		expected23.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((3/8.0) + 1), 1/((1/8.0) + 1)})); // interOnsetTimeProximities
		expected23.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), -1/((1/8.0) + 1), 1/((0.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1)})); // offsetOnsetTimeProximities
		// To next notes
		expected23.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0})); // voicesWithAdjacentNoteOnSameCourse
		expected23.addAll(Arrays.asList(new Double[]{1.0/(10+1), 1.0/(5+1), 1.0/(2+1), 1.0/(14+1), -1.0})); // pitchProximities 
		expected23.addAll(Arrays.asList(new Double[]{-1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1.0})); // interOnsetTimeProximities 
		expected23.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), -1.0})); // offsetOnsetTimeProximities  
		expected23.addAll(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 1.0, 1.0})); // voicesAlreadyOccupied
		expected.add(expected23);

		List<List<Double>> actual = new ArrayList<List<Double>>();
		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
		predDurationLabels = predictedTranscription.getDurationLabels();
		List<Integer[]> predVoicesCoDNotes = predictedTranscription.getVoicesCoDNotes();
		predVoiceLabels = predictedTranscription.getVoiceLabels();
		List<Integer[]> meterInfo = tablature.getMeterInfo();
		actual.add(FeatureGenerator.generateBidirectionalNoteFeatureVectorOLD(
			basicTabSymbolProperties, predDurationLabels, predVoicesCoDNotes, null, 
			predictedTranscription, note6, predVoiceLabels, meterInfo, 6, 1, false));
		actual.add(FeatureGenerator.generateBidirectionalNoteFeatureVectorOLD(
			basicTabSymbolProperties, predDurationLabels, predVoicesCoDNotes, null, 
			predictedTranscription, note23, predVoiceLabels, meterInfo, 23, 1, false));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}


	public void testGenerateBidirectionalNoteFeatureVectorOLDNonTab() {
		Transcription gtTranscription = new Transcription(midiTestpiece1, null);

		// Create predicted Lists
		List<List<Double>> predVoiceLabels = gtTranscription.getVoiceLabels();
		List<Integer[]> predEDUInfo = gtTranscription.getEqualDurationUnisonsInfo();
		// Chord 0: note 0 goes to voice 4; notes 2 and 3 swap voice 
		predVoiceLabels.set(0, Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 1.0}));
		Collections.swap(predVoiceLabels, 2, 3);
		// Chord 3: notes 9 and 10 swap voice
		Collections.swap(predVoiceLabels, 9, 10);

		// Create predicted Transcription
		Integer[][] bnp = gtTranscription.getBasicNoteProperties();
//		Piece predictedPiece = Transcription.createPiece(null, bnp, predVoiceLabels, null, 5);
//		Transcription predictedTranscription = new Transcription(midiTestpiece1, null, predictedPiece,
//			predVoiceLabels, null/*, predEDUInfo*/);		
		Transcription predictedTranscription = 
			new Transcription(midiTestpiece1.getName(), null, null, bnp, 5, predVoiceLabels, null,
			gtTranscription.getPiece().getMetricalTimeLine(), 
			gtTranscription.getPiece().getHarmonyTrack());		

		Note note7 = predictedTranscription.getNoteSequence().getNoteAt(7);
		Note note24 = predictedTranscription.getNoteSequence().getNoteAt(24);

		List<List<Double>> expected = new ArrayList<List<Double>>(); 
		// Chord 1, note at index 7
		// 1. Note features
		List<Double> expected7 = new ArrayList<Double>(); 
		expected7.addAll(Arrays.asList(new Double[]{
			72.0, 1/4.0, 0.0, 0/4.0, // basicNoteFeatures
			1.0/((3/16.0) + 1), 1.0/(24+1), -1.0, -1.0, // proximitiesAndCourseInfoAhead
			1.0/((1/4.0) + 1), 1.0/(7+1), -1.0, -1.0, 
			1.0/((3/8.0) + 1), 1.0/(27+1), -1.0, -1.0, 
			1.0, // numberOfNewOnsetsNextChord  
		}));
		// 2. Note + duration features
		expected7.addAll(Arrays.asList(new Double[]{4.0, 3.0, 3.0, -1.0, 12.0, 12.0, 3.0, -1.0})); // positionWithinChord
		// 3. Note + duration + voice features
		// To previous notes
		expected7.addAll(Arrays.asList(new Double[]{1.0/(7+1), 1.0/(3+1), 1.0/(15+1), -1.0, 1.0/(22+1)})); // pitchProximities 
		expected7.addAll(Arrays.asList(new Double[]{1/((1/4.0) + 1), 1/((1/4.0) + 1), 1/((1/4.0) + 1), -1.0, 1/((1/4.0) + 1)})); // interOnsetTimeProximities
		expected7.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0, 1.0/(0+1)})); // offsetOnsetTimeProximities  
		// To next notes
		expected7.addAll(Arrays.asList(new Double[]{1.0/(7+1), 1.0/(7+1), 1.0/(13+1), 1.0/(24+1), 1.0/(22+1)})); // pitchProximities 
		expected7.addAll(Arrays.asList(new Double[]{-1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((1/4.0) + 1), -1/((3/16.0) + 1), -1/((1/4.0) + 1)})); // interOnsetTimeProximities
		expected7.addAll(Arrays.asList(new Double[]{1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1/((1/16.0) + 1), 1.0/(0+1)})); // offsetOnsetTimeProximities  
		expected7.addAll(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0})); // voicesAlreadyOccupied
		expected.add(expected7);

		// Chord 7, note at index 24
		// 1. Note features
		List<Double> expected24 = new ArrayList<Double>(); 
		expected24.addAll(Arrays.asList(new Double[]{
			59.0, 1/8.0, 0.0, 7/8.0, // basicNoteFeatures
			1.0/((1/8.0) + 1), 1.0/(2+1), -1.0, 1.0/(5+1), // proximitiesAndCourseInfoAhead
			1.0/((3/16.0) + 1), -1.0, -1.0, 1.0/(9+1),
			1.0/((1/4.0) + 1), -1.0, -1.0, 1.0/(10+1), 
			4.0, // numberOfNewOnsetsNextChord
		}));
		// 2. Note + duration features
		expected24.addAll(Arrays.asList(new Double[]{5.0, 2.0, 2.0, 9.0, 12.0, 2.0, 9.0, 1.0})); // positionWithinChord		
		// 3. Note + duration + voice features
		// To previous notes
		expected24.addAll(Arrays.asList(new Double[]{1.0/(5+1), 1.0/(10+1), 1.0/(1+1), 1.0/(2+1), 1.0/(14+1)})); // pitchProximities 
		expected24.addAll(Arrays.asList(new Double[]{1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((1/8.0) + 1), 1/((3/8.0) + 1), 1/((1/8.0) + 1)})); // interOnsetTimeProximities
		expected24.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), -1/((1/8.0) + 1), 1/((0.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1)})); // offsetOnsetTimeProximities
		// To next notes
		expected24.addAll(Arrays.asList(new Double[]{1.0/(10+1), 1.0/(5+1), 1.0/(2+1), 1.0/(14+1), -1.0})); // pitchProximities 
		expected24.addAll(Arrays.asList(new Double[]{-1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1/((1/8.0) + 1), -1.0})); // interOnsetTimeProximities 
		expected24.addAll(Arrays.asList(new Double[]{1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), 1/((0.0) + 1), -1.0})); // offsetOnsetTimeProximities  
		expected24.addAll(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 1.0, 1.0})); // voicesAlreadyOccupied
		expected.add(expected24);

		List<List<Double>> actual = new ArrayList<List<Double>>();
		Integer[][] basicNoteProperties = gtTranscription.getBasicNoteProperties();
		predVoiceLabels = predictedTranscription.getVoiceLabels();
		List<Integer[]> meterInfo = gtTranscription.getMeterInfo();
		actual.add(FeatureGenerator.generateBidirectionalNoteFeatureVectorOLD(null, null, 
			null, basicNoteProperties, predictedTranscription, note7, predVoiceLabels, 
			meterInfo, 7, 1, false));
		actual.add(FeatureGenerator.generateBidirectionalNoteFeatureVectorOLD(null, null, 
			null, basicNoteProperties, predictedTranscription, note24, predVoiceLabels, 
			meterInfo, 24, 1, false));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}
	
	
	public void testScaleFeatureVector() {		
		List<Double> fvN2N = getSetOfFeatureVectors().get(1);
		List<Double> fvC2C = getSetOfFeatureVectors().get(1);

		// Determine expected
		List<List<Double>> expected = new ArrayList<List<Double>>();
		// a. N2N
		// min = [0.0, 0.0,  Double.MAX_VALUE, 0.0, 1.0, 2.0, 0.0, -2.0,  Double.MAX_VALUE]
		// max = [2.0, 1.0, -Double.MAX_VALUE, 0.0, 3.0, 2.0, 0.0,  1.0, -Double.MAX_VALUE]
		double[][] minMaxN2N = 
			FeatureGenerator.getMinAndMaxValuesSetOfFeatureVectors(getSetOfFeatureVectors(),
			ModellingApproach.N2N);
		expected.add(Arrays.asList(new Double[]{1/2.0, -1.0, -1.0, 0.0, 1/2.0, 0.0, 0.0, 3/3.0, -1.0}));

		
		// b. C2C
		// Make fictional indicesOfPitchMvmt and indexOfPitchVoiceRel
		FeatureGenerator.indicesOfPitchMvmt = Arrays.asList(new Integer[]{4, 5, 6});
		FeatureGenerator.indexOfPitchVoiceRel = 3;
		// min = new double[]{0.0,  0.0,  Double.MAX_VALUE, -1.0, 1.0, -1.0, 0.0, -2.0,  Double.MAX_VALUE};
		// max = new double[]{2.0,  1.0, -Double.MAX_VALUE,  0.0, 3.0,  2.0, 0.0,  1.0, -Double.MAX_VALUE};
		double[][] minMaxC2C = 
			FeatureGenerator.getMinAndMaxValuesSetOfFeatureVectors(getSetOfFeatureVectors(),
			ModellingApproach.C2C);
		expected.add(Arrays.asList(new Double[]{1/2.0, -1.0, -1.0, 1.0, 1/2.0, 1.0, -1.0, 1.0, -1.0}));	
		
		// Calculate actual
		List<List<Double>> actual = new ArrayList<List<Double>>();
		actual.add(FeatureGenerator.scaleFeatureVector(fvN2N, minMaxN2N, 
			ModellingApproach.N2N));
		actual.add(FeatureGenerator.scaleFeatureVector(fvC2C, minMaxC2C, 
			ModellingApproach.C2C));
		
		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
	}
	
	
	public void testScaleSetOfFeatureVectors() {				
		List<List<Double>> setN2N = getSetOfFeatureVectors();
		List<List<Double>> setC2C = getSetOfFeatureVectors();

		// Determine expected
		List<List<List<Double>>> expected = new ArrayList<List<List<Double>>>();
		// a. N2N
		// min = [0.0, 0.0,  Double.MAX_VALUE, 0.0, 1.0, 2.0, 0.0, -2.0,  Double.MAX_VALUE]
		// max = [2.0, 1.0, -Double.MAX_VALUE, 0.0, 3.0, 2.0, 0.0,  1.0, -Double.MAX_VALUE]
		double[][] minMaxN2N = 
			FeatureGenerator.getMinAndMaxValuesSetOfFeatureVectors(setN2N,
			ModellingApproach.N2N);
		List<List<Double>> expectedN2N = new ArrayList<List<Double>>();
		expectedN2N.add(Arrays.asList(new Double[]{0/2.0, 0/1.0, -1.0, -1.0, 0/2.0, -1.0, 0.0, 0/3.0, -1.0}));
		expectedN2N.add(Arrays.asList(new Double[]{1/2.0,  -1.0, -1.0,  0.0, 1/2.0,  0.0, 0.0, 3/3.0, -1.0}));
		expectedN2N.add(Arrays.asList(new Double[]{2/2.0, 1/1.0, -1.0,  0.0, 2/2.0, -1.0, 0.0, 2/3.0, -1.0}));
		expected.add(expectedN2N);
				
		// b. C2C
		// Make fictional indicesOfPitchMvmt and indexOfPitchVoiceRel
		FeatureGenerator.indicesOfPitchMvmt = Arrays.asList(new Integer[]{4, 5, 6});
		FeatureGenerator.indexOfPitchVoiceRel = 3;
		// min = new double[]{0.0,  0.0,  Double.MAX_VALUE, -1.0, 1.0, -1.0, 0.0, -2.0,  Double.MAX_VALUE};
		// max = new double[]{2.0,  1.0, -Double.MAX_VALUE,  0.0, 3.0,  2.0, 0.0,  1.0, -Double.MAX_VALUE};
		double[][] minMaxC2C = 
			FeatureGenerator.getMinAndMaxValuesSetOfFeatureVectors(setC2C, 
			ModellingApproach.C2C);
		List<List<Double>> expectedC2C = new ArrayList<List<Double>>();
		expectedC2C.add(Arrays.asList(new Double[]{0/2.0, 0/1.0, -1.0, 0/1.0, 0/2.0, 0/3.0, -1.0, 0/3.0, -1.0}));
		expectedC2C.add(Arrays.asList(new Double[]{1/2.0,  -1.0, -1.0, 1.0,   1/2.0, 3/3.0, -1.0, 3/3.0, -1.0}));
		expectedC2C.add(Arrays.asList(new Double[]{2/2.0, 1/1.0, -1.0,  1.0,  2/2.0, 0/3.0, -1.0, 2/3.0, -1.0}));
		expected.add(expectedC2C);
		
//		List<List<Double>> set = new ArrayList<List<Double>>();
//		set.add(Arrays.asList(new Double[]{0.0,  0.0, -1.0, -1.0, 1.0, -1.0, 0.0, -2.0, -1.0}));
//		set.add(Arrays.asList(new Double[]{1.0, -1.0, -1.0,  0.0, 2.0,  2.0, 0.0,  1.0, -1.0}));
//		set.add(Arrays.asList(new Double[]{2.0,  1.0, -1.0,  0.0, 3.0, -1.0, 0.0,  0.0, -1.0}));
		
		// Calculate actual
		List<List<List<Double>>> actual = new ArrayList<List<List<Double>>>();
//		FeatureGenerator.scaleSetOfFeatureVectors(setN2N, minMaxN2N, ExperimentRunner.NOTE_TO_NOTE);
//		actual.add(setN2N);
		actual.add(FeatureGenerator.scaleSetOfFeatureVectors(setN2N, minMaxN2N, 
			ModellingApproach.N2N));
//		FeatureGenerator.scaleSetOfFeatureVectors(setC2C, minMaxC2C, ExperimentRunner.CHORD_TO_CHORD);
//		actual.add(setC2C);
		actual.add(FeatureGenerator.scaleSetOfFeatureVectors(setC2C, minMaxC2C, 
			ModellingApproach.C2C));

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
	
	
	public void testScaleSetOfSetsOfFeatureVectors() {
		List<List<List<Double>>> setOfSetsN2N = new ArrayList<List<List<Double>>>();
		setOfSetsN2N.add(getSetOfFeatureVectors());
		setOfSetsN2N.add(getSecondSetOfFeatureVectors());
		
		List<List<List<Double>>> setOfSetsC2C = new ArrayList<List<List<Double>>>();
		setOfSetsC2C.add(getSetOfFeatureVectors());
		setOfSetsC2C.add(getSecondSetOfFeatureVectors());

		// Determine expected
		List<List<List<List<Double>>>> expected = new ArrayList<List<List<List<Double>>>>();
		// a. N2N	
		// min = [0.0, 0.0, 0.0, 0.0, -3.0, 2.0, 0.0, -2.0,  Double.MAX_VALUE]
		// max = [3.0, 2.0, 0.0, 2.0,  3.0, 2.0, 0.0,  3.0, -Double.MAX_VALUE]
		double[][] minMaxN2N = 
			FeatureGenerator.getMinAndMaxValuesSetOfSetsOfFeatureVectors(setOfSetsN2N, 
			ModellingApproach.N2N);				
		
		List<List<List<Double>>> expectedN2N = new ArrayList<List<List<Double>>>();
		List<List<Double>> expected0N2N = new ArrayList<List<Double>>();
		expected0N2N.add(Arrays.asList(new Double[]{0/3.0, 0/2.0, -1.0,  -1.0, 4/6.0, -1.0, 0.0, 0/5.0, -1.0}));
		expected0N2N.add(Arrays.asList(new Double[]{1/3.0,  -1.0, -1.0, 0/2.0, 5/6.0,  0.0, 0.0, 3/5.0, -1.0}));
		expected0N2N.add(Arrays.asList(new Double[]{2/3.0, 1/2.0, -1.0, 0/2.0, 6/6.0, -1.0, 0.0, 2/5.0, -1.0}));
		expectedN2N.add(expected0N2N);
		List<List<Double>> expected1N2N = new ArrayList<List<Double>>();
		expected1N2N.add(Arrays.asList(new Double[]{1/3.0, 1/2.0, -1.0,  -1.0, 4/6.0, -1.0, 0.0, 0/5.0, -1.0}));
		expected1N2N.add(Arrays.asList(new Double[]{2/3.0,  -1.0,  0.0, 1/2.0, 5/6.0,  0.0, 0.0, 4/5.0, -1.0}));
		expected1N2N.add(Arrays.asList(new Double[]{3/3.0, 2/2.0, -1.0, 2/2.0, 0/6.0, -1.0, 0.0, 5/5.0, -1.0}));
		expectedN2N.add(expected1N2N);
		expected.add(expectedN2N);
		
		// b. C2C
		// Make fictional indicesOfPitchMvmt and indexOfPitchVoiceRel
		FeatureGenerator.indicesOfPitchMvmt = Arrays.asList(new Integer[]{4, 5, 6});
		FeatureGenerator.indexOfPitchVoiceRel = 3;
		// min = new double[]{0.0, 0.0, 0.0, -1.0, -3.0, -1.0, 0.0, -2.0,  Double.MAX_VALUE};
		// max = new double[]{3.0, 2.0, 0.0,  2.0,  3.0,  2.0, 0.0,  3.0, -Double.MAX_VALUE};
		double[][] minMaxC2C = 
			FeatureGenerator.getMinAndMaxValuesSetOfSetsOfFeatureVectors(setOfSetsC2C, 
			ModellingApproach.C2C);
		
//		List<List<Double>> set2 = new ArrayList<List<Double>>();
//		set2.add(Arrays.asList(new Double[]{1.0,  1.0, -1.0, -1.0, 1.0,  -1.0, 0.0, -2.0, -1.0}));
//		set2.add(Arrays.asList(new Double[]{2.0, -1.0,  0.0,  1.0, 2.0,   2.0, 0.0,  2.0, -1.0}));
//		set2.add(Arrays.asList(new Double[]{3.0,  2.0, -1.0,  2.0, -3.0, -1.0, 0.0,  3.0, -1.0}));
		
		List<List<List<Double>>> expectedC2C = new ArrayList<List<List<Double>>>();
		List<List<Double>> expected0C2C = new ArrayList<List<Double>>();
		expected0C2C.add(Arrays.asList(new Double[]{0/3.0, 0/2.0, -1.0, 0/3.0, 4/6.0, 0/3.0, -1.0, 0/5.0, -1.0}));
		expected0C2C.add(Arrays.asList(new Double[]{1/3.0,  -1.0, -1.0, 1/3.0, 5/6.0, 3/3.0, -1.0, 3/5.0, -1.0}));
		expected0C2C.add(Arrays.asList(new Double[]{2/3.0, 1/2.0, -1.0, 1/3.0, 6/6.0, 0/3.0, -1.0, 2/5.0, -1.0}));
		expectedC2C.add(expected0C2C);
		List<List<Double>> expected1C2C = new ArrayList<List<Double>>();
		expected1C2C.add(Arrays.asList(new Double[]{1/3.0, 1/2.0, -1.0, 0/3.0, 4/6.0, 0/3.0, -1.0, 0/5.0, -1.0}));
		expected1C2C.add(Arrays.asList(new Double[]{2/3.0,  -1.0,  0.0, 2/3.0, 5/6.0, 3/3.0, -1.0, 4/5.0, -1.0}));
		expected1C2C.add(Arrays.asList(new Double[]{3/3.0, 2/2.0, -1.0, 3/3.0, 0/6.0, 0/3.0, -1.0, 5/5.0, -1.0}));
		expectedC2C.add(expected1C2C);
		expected.add(expectedC2C);
			
		// Calculate actual
		List<List<List<List<Double>>>> actual = new ArrayList<List<List<List<Double>>>>();
//		FeatureGenerator.scaleSetOfSetsOfFeatureVectors(setOfSetsN2N, minMaxN2N, ExperimentRunner.NOTE_TO_NOTE);
//		actual.add(setOfSetsN2N);
		actual.add(FeatureGenerator.scaleSetOfSetsOfFeatureVectors(setOfSetsN2N, 
			minMaxN2N, ModellingApproach.N2N));
//		FeatureGenerator.scaleSetOfSetsOfFeatureVectors(setOfSetsC2C, minMaxC2C, ExperimentRunner.CHORD_TO_CHORD);
//		actual.add(setOfSetsC2C);
		actual.add(FeatureGenerator.scaleSetOfSetsOfFeatureVectors(setOfSetsC2C, 
			minMaxC2C, ModellingApproach.C2C));
		
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j).size(), actual.get(i).get(j).size());
				for (int k = 0; k < expected.get(i).get(j).size(); k++) {
					assertEquals(expected.get(i).get(j).get(k).size(), actual.get(i).get(j).get(k).size());
					for (int l = 0; l < expected.get(i).get(j).get(k).size(); l++) {
						assertEquals(expected.get(i).get(j).get(k).get(l), actual.get(i).get(j).get(k).get(l));
					}
				}
			}
		}
	}
			
			
	public void testGetMinAndMaxValuesSetOfFeatureVectors() {		
		List<List<Double>> set = getSetOfFeatureVectors();

		// Determine expected
		List<double[][]> expected = new ArrayList<double[][]>();
		// a. N2N
		double[][] expectedN2N = new double[2][set.get(0).size()];
		expectedN2N[0] = new double[]{0.0,  0.0,  Double.MAX_VALUE, 0.0, 1.0, 2.0, 0.0, -2.0,  Double.MAX_VALUE};
		expectedN2N[1] = new double[]{2.0,  1.0, -Double.MAX_VALUE, 0.0, 3.0, 2.0, 0.0,  1.0, -Double.MAX_VALUE};
		expected.add(expectedN2N);
		// b. C2C
		// Make fictional indicesOfPitchMvmt and indexOfPitchVoiceRel
		FeatureGenerator.indicesOfPitchMvmt = Arrays.asList(new Integer[]{4, 5, 6});
		FeatureGenerator.indexOfPitchVoiceRel = 3;
		double[][] expectedC2C = new double[2][set.get(0).size()];
		expectedC2C[0] = new double[]{0.0,  0.0,  Double.MAX_VALUE, -1.0, 1.0, -1.0, 0.0, -2.0,  Double.MAX_VALUE};
		expectedC2C[1] = new double[]{2.0,  1.0, -Double.MAX_VALUE,  0.0, 3.0,  2.0, 0.0,  1.0, -Double.MAX_VALUE};
		expected.add(expectedC2C);
		
		// Calculate actual
		List<double[][]> actual = new ArrayList<double[][]>();
		actual.add(FeatureGenerator.getMinAndMaxValuesSetOfFeatureVectors(set, 
			ModellingApproach.N2N));
		actual.add(FeatureGenerator.getMinAndMaxValuesSetOfFeatureVectors(set, 
			ModellingApproach.C2C));
		
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
	
	
	public void testGetMinAndMaxValuesSetOfSetsOfFeatureVectors() {	
		List<List<List<Double>>> setOfSets = new ArrayList<List<List<Double>>>();
		setOfSets.add(getSetOfFeatureVectors());
		setOfSets.add(getSecondSetOfFeatureVectors());
		
		// Determine expected
		List<double[][]> expected = new ArrayList<double[][]>();
		// a. N2N
		double[][] expectedN2N = new double[2][setOfSets.get(0).get(0).size()];
		expectedN2N[0] = new double[]{0.0, 0.0, 0.0, 0.0, -3.0, 2.0, 0.0, -2.0,  Double.MAX_VALUE};
		expectedN2N[1] = new double[]{3.0, 2.0, 0.0, 2.0,  3.0, 2.0, 0.0,  3.0, -Double.MAX_VALUE};
		expected.add(expectedN2N);
		
		// b. C2C
		// Make fictional indicesOfPitchMvmt and indexOfPitchVoiceRel
		FeatureGenerator.indicesOfPitchMvmt = Arrays.asList(new Integer[]{4, 5, 6});
		FeatureGenerator.indexOfPitchVoiceRel = 3;
		double[][] expectedC2C = new double[2][setOfSets.get(0).get(0).size()];
		expectedC2C[0] = new double[]{0.0, 0.0, 0.0, -1.0, -3.0, -1.0, 0.0, -2.0,  Double.MAX_VALUE};
		expectedC2C[1] = new double[]{3.0, 2.0, 0.0,  2.0,  3.0,  2.0, 0.0,  3.0, -Double.MAX_VALUE};
		expected.add(expectedC2C);
		
		// Calculate actual
		List<double[][]> actual = new ArrayList<double[][]>();
		actual.add(FeatureGenerator.getMinAndMaxValuesSetOfSetsOfFeatureVectors(setOfSets, 
			ModellingApproach.N2N));
		actual.add(FeatureGenerator.getMinAndMaxValuesSetOfSetsOfFeatureVectors(setOfSets, 
			ModellingApproach.C2C));
		
//		assertEquals(expected.length, actual.length);
//		for (int i = 0; i < expected.length; i++) {
//			assertEquals(expected[i].length, actual[i].length);
//			for (int j = 0; j < expected[i].length; j++) {
//				assertEquals(expected[i][j], actual[i][j]);
//			}
//		}
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


	public void testGetBackwardsMapping() {
		Tablature tablature = new Tablature(encodingTestpiece1, false);
			
		List<Integer> expected = Arrays.asList(new Integer[]{35, 36, 37, 38, 34, 33, 32, 31, 30, 29, 25, 26, 27, 28, 
			23, 24, 19, 20, 21, 22, 14, 15, 16, 17, 18, 13, 9, 10, 11, 12, 8, 4, 5, 6, 7, 0, 1, 2, 3});

		List<Integer> chordSizes = tablature.getNumberOfNotesPerChord();
		List<Integer> actual = FeatureGenerator.getBackwardsMapping(chordSizes);
			
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}


	public void testGetBackwardsMappingNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1, null);

		List<Integer> expected = Arrays.asList(new Integer[]{36, 37, 38, 39, 35, 34, 33, 32, 31, 30, 26, 27, 28, 29, 
			24, 25, 20, 21, 22, 23, 15, 16, 17, 18, 19, 14, 9, 10, 11, 12, 13, 8, 4, 5, 6, 7, 0, 1, 2, 3});

		List<Integer> chordSizes = transcription.getNumberOfNewNotesPerChord();
		List<Integer> actual = FeatureGenerator.getBackwardsMapping(chordSizes);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}
	
	
	public void testScaleFeatureVectorCOMPLICATED() {	
//		// Non-tab only; !useFixedValues
//		
//		// Determine expected
//		List<List<Double>> expected = new ArrayList<List<Double>>();
//		// 1. N2N
//		// a. Feature vector for note 4
//		List<Double> fvN2N = Arrays.asList(new Double[]{
//			45.0, 3/16.0, 0.0, 0/4.0,
//			4.0, 0.0, -1.0, 12.0, 12.0, 12.0, 3.0, -1.0,
//			1/25.0, 1/19.0, 1/13.0, 1/6.0, -1.0,
//			1/1.25, 1/1.25, 1/1.25, 1/1.25, -1.0,
//			1.0, 1.0, 1.0, 1.0, -1.0,
//			0.0, 0.0, 0.0, 0.0, 0.0
//		});
//		
//		double[][] minMaxN2N = new double[2][32];
//		Arrays.fill(minMaxN2N[0], 0.0);
//		Arrays.fill(minMaxN2N[1], 10.0);
//		
//		List<Double> expectedN2N = Arrays.asList(new Double[]{
//			8.0, -77/80.0, -1.0, -1.0,
//			-0.2, -1.0, -1.0, 1.4, 1.4, 1.4, -0.4, -1.0,
//			1/25.0, 1/19.0, 1/13.0, 1/6.0, -1.0,
//			1/1.25, 1/1.25, 1/1.25, 1/1.25, -1.0,
//			1.0, 1.0, 1.0, 1.0, -1.0,
//			-1.0, -1.0, -1.0, -1.0, -1.0
//		});
//		expected.add(expectedN2N);
//		
//		// 2. C2C
//		// a. Feature vector for chord 1; config 1
//		List<Double> fvC2C4vvMax4vv = Arrays.asList(new Double[]{ 
//			0.0, 0.0, 45.0, 3/16.0, // note-specific 
//			1.0, 1.0, 57.0, 1/4.0,
//			2.0, 2.0, 69.0, 1/8.0,
//			-1.0, -1.0, -1.0, -1.0,
//			3.0, 0.0, 0/4.0, 12.0, 12.0, -1.0, // chord-level
//			1/5.0, 1.0, 1/6.0, -1.0, // proximities and movements
//			1/1.25, 1/1.25, 1/1.25, -1.0,
//			1.0, 1.0, 1.0, -1.0,
//			4.0, 0.0, -5.0, 0.0, 
//			0.0, 0.0, 0.0, 0.0, // voices already occupied
//			-0.666, 0.0, 0.0, 0.0, // pitch-voice relation and voice crossing info
//			-1.0, 2.0, 1.0, 0.0 // voice assignment vector
//		});
//				
//		double[][] minMaxC2C4vvMax4vv = new double[2][50];
//		Arrays.fill(minMaxC2C4vvMax4vv[0], 0.0);
//		Arrays.fill(minMaxC2C4vvMax4vv[1], 10.0);
//		// Equalise min and max for last pitchMvmt
//		minMaxC2C4vvMax4vv[1][37] = 0.0; 
//				
//		List<Double> expectedC2C4vvMax4vv = Arrays.asList(new Double[]{ 
//			0.0, 0.0, 4.5, 3/160.0, // note-specific 
//			1/3.0, 0.1, 5.7, 1/40.0,
//			2/3.0, 0.2, 6.9, 1/80.0,
//			-1.0, -1.0, -1.0, -1.0,
//			0.3, 0.0, 0/4.0, 1.2, 1.2,-1.0, // chord-level
//			1/5.0, 1.0, 1/6.0, -1.0, // proximities and movements
//			1/1.25, 1/1.25, 1/1.25, -1.0,
//			1.0, 1.0, 1.0, -1.0,
//			0.4, 0.0, -0.5, 0.0, 
//			0.0, 0.0, 0.0, 0.0, // voices already occupied
//			-0.666, 0.0, 0.0, 0.0, // pitch-voice relation and voice crossing info
//			-1.0, 0.2, 0.1, 0.0 // voice assignment vector
//		});
//		expected.add(expectedC2C4vvMax4vv);
//		
//		// b. Feature vector for chord 1; config 2
//		List<Double> fvC2C4vvMax5vv = Arrays.asList(new Double[]{ 
//			0.0, 0.0, 45.0, 3/16.0, // note-specific 
//			1.0, 1.0, 57.0, 1/4.0,
//			2.0, 2.0, 69.0, 1/8.0,
//			3.0, 3.0, 72.0, 1/4.0,
//			-1.0, -1.0, -1.0, -1.0,
//			4.0, 0.0, 0/4.0, 12.0, 12.0, 3.0, -1.0, // chord-level
//			1/4.0, 1/5.0, 1.0, 1/6.0, -1.0, // proximities and movements
//			1/1.25, 1/1.25, 1/1.25, 1/1.25, -1.0,
//			1.0, 1.0, 1.0, 1.0, -1.0,
//			3.0, 4.0, 0.0, -5.0, 0.0, 
//			0.0, 0.0, 0.0, 0.0, 0.0, // voices already occupied
//			-0.666, 0.0, 0.0, 0.0, // pitch-voice relation and voice crossing info
//			3.0, 2.0, 1.0, 0.0, -1.0 // voice assignment vector
//		});
//		
//		double[][] minMaxC2C4vvMax5vv = new double[2][61];
//		Arrays.fill(minMaxC2C4vvMax5vv[0], 0.0);
//		Arrays.fill(minMaxC2C4vvMax5vv[1], 10.0);
//		// Equalise min and max for last pitchMvmt
//		minMaxC2C4vvMax5vv[1][46] = 0.0; 
//		
//		List<Double> expectedC2C4vvMax5vv = Arrays.asList(new Double[]{ 
//			0.0, 0.0, 4.5, 3/160.0, // note-specific 
//			1/3.0, 0.1, 5.7, 1/40.0,
//			2/3.0, 0.2, 6.9, 1/80.0,
//			3/3.0, 0.3, 7.2, 1/40.0,
//			-1.0, -1.0, -1.0, -1.0,
//			0.4, 0.0, 0/4.0, 1.2, 1.2, 0.3, -1.0, // chord-level
//			1/4.0, 1/5.0, 1.0, 1/6.0, -1.0, // proximities and movements
//			1/1.25, 1/1.25, 1/1.25, 1/1.25, -1.0,
//			1.0, 1.0, 1.0, 1.0, -1.0,
//			0.3, 0.4, 0.0, -0.5, 0.0, 
//			0.0, 0.0, 0.0, 0.0, 0.0, // voices already occupied
//			-0.666, 0.0, 0.0, 0.0, // pitch-voice relation and voice crossing info
//			0.3, 0.2, 0.1, 0.0, -1.0 // voice assignment vector
//		});
//		expected.add(expectedC2C4vvMax5vv);
//		
////		List<Double> fvC2C4vvMax5vv = Arrays.asList(new Double[]{ 
////			0.0, 0.0, 45.0, 3/16.0, // note-specific 
////			1.0, 1.0, 57.0, 1/4.0,
////			2.0, 2.0, 69.0, 1/8.0,
////			3.0, 3.0, 72.0, 1/4.0,
////			-1.0, -1.0, -1.0, -1.0,
////			4.0, 0.0, 0/4.0, 12.0, 12.0, 3.0, -1.0, // chord-level
////			1/4.0, 1/5.0, 1.0, 1/6.0, -1.0, // proximities and movements
////			1/1.25, 1/1.25, 1/1.25, 1/1.25, -1.0,
////			1.0, 1.0, 1.0, 1.0, -1.0,
////			3.0, 4.0, 0.0, -5.0, 0.0, 
////			0.0, 0.0, 0.0, 0.0, 0.0, // voices already occupied
////			-0.666, 0.0, 0.0, 0.0, // pitch-voice relation and voice crossing info
////			3.0, 2.0, 1.0, 0.0, -1.0 // voice assignment vector
////		});
////		
////		double[][] minMaxC2C4vvMax5vv = new double[2][61];
////		Arrays.fill(minMaxC2C4vvMax5vv[0], 0.0);
////		Arrays.fill(minMaxC2C4vvMax5vv[1], 10.0);
////		// Equalise min and max for last pitchMvmt and last alreadyOccupied
////		minMaxC2C4vvMax5vv[1][46] = 0.0;
////		minMaxC2C4vvMax5vv[1][51] = 0.0;
////		
////		List<Double> expectedC2C4vvMax5vv = Arrays.asList(new Double[]{ 
////			-1.0, -1.0, 8.0, -77/80.0, // note-specific 
////			-1/3.0, -0.8, 10.4, -19/20.0,
////			1/3.0, -0.6, 12.8, -39/40.0,
////			1.0, -0.4, 13.4, -19/20.0,
////			-10.0, -10.0, -10.0, -10.0,
////			-0.2, -1.0, -1.0, 1.4, 1.4, -0.4, -10.0, // chord-level
////			-19/20.0, -24/25.0, -0.8, -29/30.0, -10.0, // proximities and movements
////			-0.84, -0.84, -0.84, -0.84, -10.0,
////			-0.8, -0.8, -0.8, -0.8, -10.0,
////			0.3, 0.4, 0.0, -0.5, 0.0, 
////			-1.0, -1.0, -1.0, -1.0, -10.0, // voices already occupied
////			-1.1332, -1.0, -1.0, -1.0, // pitch-voice relation and voice crossing info
////			-0.4, -0.6, -0.8, -1.0, -10.0 // voice assignment vector
////		});
////		expected.add(expectedC2C4vvMax5vv);
//		
//		// c. Feature vector for chord 1; config 3
//		List<Double> fvC2C4vvMax4vvAvg = Arrays.asList(new Double[]{ 
//			0.0, 0.0, 45.0, 3/16.0, // note-specific 
//			1.0, 1.0, 57.0, 1/4.0,
//			2.0, 2.0, 69.0, 1/8.0,
//			-1.0, -1.0, -1.0, -1.0,
//			3.0, 0.0, 0/4.0, 12.0, 12.0, -1.0, // chord-level
//			1/5.0,  // proximities and movements
//			1/1.25, 
//			1.0,
//			4.0, 0.0, -5.0, 0.0, 
//			0.0, 0.0, 0.0, 0.0, // voices already occupied
//			-0.666, 0.0, 0.0, 0.0, // pitch-voice relation and voice crossing info
//			-1.0, 2.0, 1.0, 0.0 // voice assignment vector
//		});
//				
//		double[][] minMaxC2C4vvMax4vvAvg = new double[2][41];
//		Arrays.fill(minMaxC2C4vvMax4vvAvg[0], 0.0);
//		Arrays.fill(minMaxC2C4vvMax4vvAvg[1], 10.0);
//		// Equalise min and max for last pitchMvmt
//		minMaxC2C4vvMax4vvAvg[1][28] = 0.0; 
//				
//		List<Double> expectedC2C4vvMax4vvAvg = Arrays.asList(new Double[]{ 
//			0.0, 0.0, 4.5, 3/160.0, // note-specific 
//			1/3.0, 0.1, 5.7, 1/40.0,
//			2/3.0, 0.2, 6.9, 1/80.0,
//			-1.0, -1.0, -1.0, -1.0,
//			0.3, 0.0, 0/4.0, 1.2, 1.2,-1.0, // chord-level
//			1/5.0, // proximities and movements
//			1/1.25, 
//			1.0, 
//			0.4, 0.0, -0.5, 0.0, 
//			0.0, 0.0, 0.0, 0.0, // voices already occupied
//			-0.666, 0.0, 0.0, 0.0, // pitch-voice relation and voice crossing info
//			-1.0, 0.2, 0.1, 0.0 // voice assignment vector
//		});
//		expected.add(expectedC2C4vvMax4vvAvg);
//		
//		// d. Feature vector for chord 1; config 4
//		List<Double> fvC2C4vvMax5vvAvg = Arrays.asList(new Double[]{ 
//			0.0, 0.0, 45.0, 3/16.0, // note-specific 
//			1.0, 1.0, 57.0, 1/4.0,
//			2.0, 2.0, 69.0, 1/8.0,
//			3.0, 3.0, 72.0, 1/4.0,
//			-1.0, -1.0, -1.0, -1.0,
//			4.0, 0.0, 0/4.0, 12.0, 12.0, 3.0, -1.0, // chord-level
//			1/4.0,  // proximities and movements
//			1/1.25, 
//			1.0, 
//			3.0, 4.0, 0.0, -5.0, 0.0, 
//			0.0, 0.0, 0.0, 0.0, 0.0, // voices already occupied
//			-0.666, 0.0, 0.0, 0.0, // pitch-voice relation and voice crossing info
//			3.0, 2.0, 1.0, 0.0, -1.0 // voice assignment vector
//		});
//		
//		double[][] minMaxC2C4vvMax5vvAvg = new double[2][49];
//		Arrays.fill(minMaxC2C4vvMax5vvAvg[0], 0.0);
//		Arrays.fill(minMaxC2C4vvMax5vvAvg[1], 10.0);
//		// Equalise min and max for last pitchMvmt
//		minMaxC2C4vvMax5vvAvg[1][34] = 0.0; 
//		
//		List<Double> expectedC2C4vvMax5vvAvg = Arrays.asList(new Double[]{ 
//			0.0, 0.0, 4.5, 3/160.0, // note-specific 
//			1/3.0, 0.1, 5.7, 1/40.0,
//			2/3.0, 0.2, 6.9, 1/80.0,
//			3/3.0, 0.3, 7.2, 1/40.0,
//			-1.0, -1.0, -1.0, -1.0,
//			0.4, 0.0, 0/4.0, 1.2, 1.2, 0.3, -1.0, // chord-level
//			1/4.0, // proximities and movements
//			1/1.25,
//			1.0, 
//			0.3, 0.4, 0.0, -0.5, 0.0, 
//			0.0, 0.0, 0.0, 0.0, 0.0, // voices already occupied
//			-0.666, 0.0, 0.0, 0.0, // pitch-voice relation and voice crossing info
//			0.3, 0.2, 0.1, 0.0, -1.0 // voice assignment vector
//		});
//		expected.add(expectedC2C4vvMax5vvAvg);
//		
//		
////		List<Double> fvNote4 = Arrays.asList(new Double[]{
////				45.0, 6.0, 0.0, 3/16.0, 3/16.0, 0.0, 0/4.0, 1.0,
////				4.0, 0.0, -1.0, 12.0, 12.0, 12.0, 3.0, -1.0,
////				0.0, 0.0, 0.0, 0.0, 0.0,
////				1/25.0, 1/19.0, 1/13.0, 1/6.0, -1.0,
////				1/1.25, 1/1.25, 1/1.25, 1/1.25, -1.0,
////				1.0, 1.0, 1.0, 1.0, -1.0,
////				0.0, 0.0, 0.0, 0.0, 0.0
////			});
////		
////		// b. !isTablatureCase, N2N, useFixedValues; note 4
////		List<Double> expectedB = Arrays.asList(new Double[]{
////			-5/12.0, -61/64.0, -1.0, -1.0, 
////			1/2.0, -1.0, -1.0, -2/3.0, -2/3.0, -2/3.0, -11/12.0, -1.0,
////			-23/25.0, -17/19.0, -11/13.0, -2/3.0, -1.0,
////			0.6, 0.6, 0.6, 0.6, -1.0,
////			1.0, 1.0, 1.0, 1.0, -1.0,
////			-1.0, -1.0, -1.0, -1.0, -1.0,
////		});
//////		expected.add(expectedB);
////		
////		// isTablatureCase, N2N, useFixedValues; note 4
////		List<Double> expectedB1 = Arrays.asList(new Double[]{
////			-5/12.0, 1/9.0, -1.0, -61/64.0, -61/64.0, -1.0, -1.0, -1.0, 
////			1/2.0, -1.0, -1.0, -2/3.0, -2/3.0, -2/3.0, -11/12.0, -1.0,
////			-1.0, -1.0, -1.0, -1.0, -1.0,  
////			-23/25.0, -17/19.0, -11/13.0, -2/3.0, -1.0,
////			0.6, 0.6, 0.6, 0.6, -1.0,
//////			1/25.0, 1/19.0, 1/13.0, 1/6.0, -1.0,
//////			1/1.25, 1/1.25, 1/1.25, 1/1.25, -1.0,
////			1.0, 1.0, 1.0, 1.0, -1.0,
////			-1.0, -1.0, -1.0, -1.0, -1.0,
////		});
//////		expected.add(expectedB1);
////			
////		// c. !isTablatureCase, C2C, !useFixedValues; chord 1
////		
////		// d. !isTablatureCase, C2C, useFixedValues; chord 1
////		List<Double> expectedD = Arrays.asList(new Double[]{
////			-1.0, -1.0, -5/12.0, -61/64.0, 
////			-1/2.0, -1/2.0, -1/12.0, -15/16.0,
////			0.0, 0.0, 1/4.0, -31/32.0,
////			1/2.0, 1/2.0, 1/3.0, -15/16.0,
////			-1.0, -1.0, -1.0, -1.0,
////			1/2.0, -1.0, -1.0, -2/3.0, -2/3.0, -11/12.0, -1.0, 
////			-1/2.0, -3/5.0, 1.0, -2/3.0, -1.0,
////			0.6, 0.6, 0.6, 0.6, -1.0,
////			1.0, 1.0, 1.0, 1.0, -1.0,
////			1/24.0, 1/18.0, 0.0, -5/72.0, 0.0,
////			-1.0, -1.0, -1.0, -1.0, -1.0,
////			-0.666, -1.0, -1.0, -1.0,
////			1/2.0, 0.0, -1/2.0, -1.0, -1.0,
////		});
//////		expected.add(expectedD);
//				
//	
//		// Calculate actual
//		List<List<Double>> actual = new ArrayList<List<Double>>();
//		// 1.
//		FeatureGenerator.determineScalingSettings(ExperimentRunner.NOTE_TO_NOTE, false, -1);
//		List<List<Double>> setOfFeatureVectors = new ArrayList<List<Double>>();
//		setOfFeatureVectors.add(fvN2N);
//		FeatureGenerator.scaleFeatureVector(fvN2N, minMaxN2N);
//		actual.add(fvN2N);
//		// 2.
//		// a.
//		FeatureGenerator.determineScalingSettings(ExperimentRunner.CHORD_TO_CHORD, false, 1);
//		setOfFeatureVectors = new ArrayList<List<Double>>();
//		setOfFeatureVectors.add(fvC2C4vvMax4vv);
//		FeatureGenerator.scaleFeatureVector(fvC2C4vvMax4vv, minMaxC2C4vvMax4vv);
//		actual.add(fvC2C4vvMax4vv);
//		// b.
//		FeatureGenerator.determineScalingSettings(ExperimentRunner.CHORD_TO_CHORD, false, 2);
//		setOfFeatureVectors = new ArrayList<List<Double>>();
//		setOfFeatureVectors.add(fvC2C4vvMax5vv);
//		FeatureGenerator.scaleFeatureVector(fvC2C4vvMax5vv, minMaxC2C4vvMax5vv);
//		actual.add(fvC2C4vvMax5vv);	
//		// c.
//		FeatureGenerator.determineScalingSettings(ExperimentRunner.CHORD_TO_CHORD, false, 3);
//		setOfFeatureVectors = new ArrayList<List<Double>>();
//		setOfFeatureVectors.add(fvC2C4vvMax4vvAvg);
//		FeatureGenerator.scaleFeatureVector(fvC2C4vvMax4vvAvg, minMaxC2C4vvMax4vvAvg);
//		actual.add(fvC2C4vvMax4vvAvg);	
//		// d. 
//		FeatureGenerator.determineScalingSettings(ExperimentRunner.CHORD_TO_CHORD, false, 4);
//		setOfFeatureVectors = new ArrayList<List<Double>>();
//		setOfFeatureVectors.add(fvC2C4vvMax5vvAvg);
//		FeatureGenerator.scaleFeatureVector(fvC2C4vvMax5vvAvg, minMaxC2C4vvMax5vvAvg);
//		actual.add(fvC2C4vvMax5vvAvg);	
//		
//		// Assert equality
//		assertEquals(expected.size(), actual.size());
//		for (int i = 0; i < expected.size(); i++) {
//			assertEquals(expected.get(i).size(), actual.get(i).size());
//			for (int j = 0; j < expected.get(i).size(); j++) {
//				System.out.println(i + " " + j);
//				assertEquals(expected.get(i).get(j), actual.get(i).get(j), Math.abs(expected.get(i).get(j)) / 1e6);		
//			}
//		}
	}
	
	
	public void testGetMinAndMaxValuesSetOfFeatureVectorsCOMPLICATED() {
//		 
//		// Non-tab only; !useFixedValues
//		
//		// Determine expected
//		List<double[][]> expected = new ArrayList<double[][]>();
//		// 1. N2N
//		List<List<Double>> fvN2N = new ArrayList<List<Double>>();
//		fvN2N.add(Arrays.asList(new Double[]{
//			0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, -1.0, 0.0, /**/ 0.0, 1.0, 2.0, -1.0, /**/
//			0.0, 0.0, 0.0, 0.0, -1.0, /**/ 0.0, 0.0, 0.0, 0.0, -1.0, /**/ 0.0, 0.0, 0.0, 0.0, -1.0, /**/
//			0.0, 0.0, 0.0, 0.0, 0.0
//		}));
//		fvN2N.add(Arrays.asList(new Double[]{
//			1.0, 1.0, 1.0, 1.0, /**/ 1.0, 1.0, 1.0, 1.0, /**/ 1.0, 2.0, 3.0, -1.0, /**/
//			1.0, 1.0, 1.0, 1.0, -1.0, /**/ 1.0, 1.0, 1.0, 1.0, -1.0, /**/ 1.0, 1.0, 1.0, 1.0, -1.0, /**/
//			1.0, 1.0, 1.0, 1.0, 1.0
//			}));
//		fvN2N.add(Arrays.asList(new Double[]{
//			2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, -1.0, /**/ 2.0, 3.0, 4.0, -1.0, /**/
//			2.0, 2.0, 2.0, 2.0, -1.0, /**/ 2.0, 2.0, 2.0, 2.0, -1.0, /**/ 2.0, 2.0, 2.0, 2.0, -1.0, /**/
//			2.0, 2.0, 2.0, 2.0, 2.0
//			}));
//			
//		double[][] expectedN2N = new double[2][32];
//		expectedN2N[0] = new double[]{
//			0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 1.0, 0.0, /**/ 0.0, 1.0, 2.0, Double.MAX_VALUE, /**/
//			0.0, 0.0, 0.0, 0.0, Double.MAX_VALUE, /**/ 0.0, 0.0, 0.0, 0.0, Double.MAX_VALUE, /**/ 0.0, 0.0, 0.0, 0.0, Double.MAX_VALUE, /**/
//			0.0, 0.0, 0.0, 0.0, 0.0
//		};
//		expectedN2N[1] = new double[]{
//			2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 1.0, /**/ 2.0, 3.0, 4.0, -Double.MAX_VALUE, /**/
//			2.0, 2.0, 2.0, 2.0, -Double.MAX_VALUE, /**/ 2.0, 2.0, 2.0, 2.0, -Double.MAX_VALUE, /**/ 2.0, 2.0, 2.0, 2.0, -Double.MAX_VALUE, /**/
//			2.0, 2.0, 2.0, 2.0, 2.0
//		};
//		expected.add(expectedN2N);
//		
//		// 2. C2C
//		// a. Config 1
//		List<List<Double>> fvC2C4vvMax4vv = new ArrayList<List<Double>>();
//		fvC2C4vvMax4vv.add(Arrays.asList(new Double[]{
//			0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ -1.0, -1.0, -1.0, -1.0, /**/ 
//			0.0, 0.0, 0.0, /**/ 0.0, 1.0, -1.0, /**/
//			0.0, 0.0, 0.0, -1.0, /**/ 0.0, 0.0, 0.0, -1.0, /**/ 0.0, 0.0, 0.0, -1.0, /**/
//			1.0, -2.0, 3.0, 0.0,
//			0.0, 0.0, 0.0, 0.0,
//			0.0,
//			0.0, 0.0, 0.0,
//			-1.0, 0.0, 0.0, 0.0
//		}));
//		fvC2C4vvMax4vv.add(Arrays.asList(new Double[]{
//			1.0, 1.0, 1.0, 1.0, /**/ 1.0, 1.0, 1.0, 1.0, /**/ 1.0, 1.0, 1.0, 1.0, /**/ -1.0, -1.0, -1.0, -1.0, /**/ 
//			1.0, 1.0, 1.0, /**/ 1.0, 2.0, -1.0, /**/
//			1.0, 1.0, 1.0, -1.0, /**/ 1.0, 1.0, 1.0, -1.0, /**/ 1.0, 1.0, 1.0, -1.0, /**/
//			2.0, 3.0, -5.0, 0.0,
//			1.0, 1.0, 1.0, 1.0,
//			1.0,
//			1.0, 1.0, 1.0,
//			-1.0, 1.0, 1.0, 1.0,
//		}));
//		fvC2C4vvMax4vv.add(Arrays.asList(new Double[]{
//			2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ -1.0, -1.0, -1.0, -1.0, /**/ 
//			2.0, 2.0, 2.0, /**/ 2.0, 3.0, -1.0, /**/
//			2.0, 2.0, 2.0, -1.0, /**/ 2.0, 2.0, 2.0, -1.0, /**/ 2.0, 2.0, 2.0, -1.0, /**/
//			3.0, -4.0, 4.0, 0.0,
//			2.0, 2.0, 2.0, 2.0,
//			2.0,
//			2.0, 2.0, 2.0,
//			-1.0, 2.0, 2.0, 2.0
//		}));
//			
//		double[][] expectedC2C4vvMax4vv = new double[2][];
//		expectedC2C4vvMax4vv[0] = new double[]{
//			0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, /**/ 
//			0.0, 0.0, 0.0, /**/ 0.0, 1.0, Double.MAX_VALUE, /**/
//			0.0, 0.0, 0.0, Double.MAX_VALUE, /**/ 0.0, 0.0, 0.0, Double.MAX_VALUE, /**/ 0.0, 0.0, 0.0, Double.MAX_VALUE, /**/
//			1.0, 2.0, 3.0, 0.0,
//			0.0, 0.0, 0.0, 0.0,
//			0.0,
//			0.0, 0.0, 0.0,
//			Double.MAX_VALUE, 0.0, 0.0, 0.0 
//		};
//		expectedC2C4vvMax4vv[1] = new double[]{
//			2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, /**/ 
//			2.0, 2.0, 2.0, /**/ 2.0, 3.0, -Double.MAX_VALUE, /**/
//			2.0, 2.0, 2.0, -Double.MAX_VALUE, /**/ 2.0, 2.0, 2.0, -Double.MAX_VALUE, /**/ 2.0, 2.0, 2.0, -Double.MAX_VALUE, /**/
//			3.0, 4.0, 5.0, 0.0,
//			2.0, 2.0, 2.0, 2.0,
//			2.0,
//			2.0, 2.0, 2.0,
//			-Double.MAX_VALUE, 2.0, 2.0, 2.0 
//		};
//		expected.add(expectedC2C4vvMax4vv);
//		
//		// b. config 2
//		List<List<Double>> fvC2C4vvMax5vv = new ArrayList<List<Double>>();
//		fvC2C4vvMax5vv.add(Arrays.asList(new Double[]{
//			0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ -1.0, -1.0, -1.0, -1.0, /**/ 
//			0.0, 0.0, 0.0, /**/ 0.0, 1.0, 2.0, -1.0, /**/
//			0.0, 0.0, 0.0, 0.0, -1.0, /**/ 0.0, 0.0, 0.0, 0.0, -1.0, /**/ 0.0, 0.0, 0.0, 0.0, -1.0, /**/
//			0.0, 1.0, -2.0, 3.0, 0.0,
//			0.0, 0.0, 0.0, 0.0, 0.0,
//			0.0,
//			0.0, 0.0, 0.0,
//			0.0, 0.0, 0.0, 0.0, -1.0
//		}));
//		fvC2C4vvMax5vv.add(Arrays.asList(new Double[]{
//			1.0, 1.0, 1.0, 1.0, /**/ 1.0, 1.0, 1.0, 1.0, /**/ 1.0, 1.0, 1.0, 1.0, /**/ 1.0, 1.0, 1.0, 1.0, /**/ -1.0, -1.0, -1.0, -1.0, /**/ 
//			1.0, 1.0, 1.0, /**/ 1.0, 2.0, 3.0, -1.0, /**/
//			1.0, 1.0, 1.0, 1.0, -1.0, /**/ 1.0, 1.0, 1.0, 1.0, -1.0, /**/ 1.0, 1.0, 1.0, 1.0, -1.0, /**/
//			1.0, 2.0, 3.0, -5.0, 0.0,
//			1.0, 1.0, 1.0, 1.0, 1.0,
//			1.0,
//			1.0, 1.0, 1.0,
//			1.0, 1.0, 1.0, 1.0, -1.0
//		}));
//		fvC2C4vvMax5vv.add(Arrays.asList(new Double[]{
//			2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ -1.0, -1.0, -1.0, -1.0, /**/ 
//			2.0, 2.0, 2.0, /**/ 2.0, 3.0, 4.0, -1.0, /**/
//			2.0, 2.0, 2.0, 2.0, -1.0, /**/ 2.0, 2.0, 2.0, 2.0, -1.0, /**/ 2.0, 2.0, 2.0, 2.0, -1.0, /**/
//			2.0, 3.0, -4.0, 4.0, 0.0,
//			2.0, 2.0, 2.0, 2.0, 2.0,
//			2.0,
//			2.0, 2.0, 2.0,
//			2.0, 2.0, 2.0, 2.0, -1.0
//		}));
//			
//		double[][] expectedC2C4vvMax5vv = new double[2][];
//		expectedC2C4vvMax5vv[0] = new double[]{
//			0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, /**/ 
//			0.0, 0.0, 0.0, /**/ 0.0, 1.0, 2.0, Double.MAX_VALUE, /**/
//			0.0, 0.0, 0.0, 0.0, Double.MAX_VALUE, /**/ 0.0, 0.0, 0.0, 0.0, Double.MAX_VALUE, /**/ 0.0, 0.0, 0.0, 0.0, Double.MAX_VALUE, /**/
//			0.0, 1.0, 2.0, 3.0, 0.0,
//			0.0, 0.0, 0.0, 0.0, 0.0,
//			0.0,
//			0.0, 0.0, 0.0,
//			0.0, 0.0, 0.0, 0.0, Double.MAX_VALUE
//		};
//		expectedC2C4vvMax5vv[1] = new double[]{
//			2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, /**/ 
//			2.0, 2.0, 2.0, /**/ 2.0, 3.0, 4.0, -Double.MAX_VALUE, /**/
//			2.0, 2.0, 2.0, 2.0, -Double.MAX_VALUE, /**/ 2.0, 2.0, 2.0, 2.0, -Double.MAX_VALUE, /**/ 2.0, 2.0, 2.0, 2.0, -Double.MAX_VALUE, /**/
//			2.0, 3.0, 4.0, 5.0, 0.0,
//			2.0, 2.0, 2.0, 2.0, 2.0,
//			2.0,
//			2.0, 2.0, 2.0,
//			2.0, 2.0, 2.0, 2.0, -Double.MAX_VALUE
//		};
//		expected.add(expectedC2C4vvMax5vv);
//		
//		// c. Config 3
//		List<List<Double>> fvC2C4vvMax4vvAvg = new ArrayList<List<Double>>();
//		fvC2C4vvMax4vvAvg.add(Arrays.asList(new Double[]{
//			0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ -1.0, -1.0, -1.0, -1.0, /**/ 
//			0.0, 0.0, 0.0, /**/ 0.0, 1.0, -1.0, /**/
//			0.0, 0.0, 0.0,
//			1.0, -2.0, 3.0, 0.0,
//			0.0, 0.0, 0.0, 0.0,
//			0.0,
//			0.0, 0.0, 0.0,
//			-1.0, 0.0, 0.0, 0.0
//		}));
//		fvC2C4vvMax4vvAvg.add(Arrays.asList(new Double[]{
//			1.0, 1.0, 1.0, 1.0, /**/ 1.0, 1.0, 1.0, 1.0, /**/ 1.0, 1.0, 1.0, 1.0, /**/ -1.0, -1.0, -1.0, -1.0, /**/ 
//			1.0, 1.0, 1.0, /**/ 1.0, 2.0, -1.0, /**/
//			1.0, 1.0, 1.0,
//			2.0, 3.0, -5.0, 0.0,
//			1.0, 1.0, 1.0, 1.0,
//			1.0,
//			1.0, 1.0, 1.0,
//			-1.0, 1.0, 1.0, 1.0,
//		}));
//		fvC2C4vvMax4vvAvg.add(Arrays.asList(new Double[]{
//			2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ -1.0, -1.0, -1.0, -1.0, /**/ 
//			2.0, 2.0, 2.0, /**/ 2.0, 3.0, -1.0, /**/
//			2.0, 2.0, 2.0,
//			3.0, -4.0, 4.0, 0.0,
//			2.0, 2.0, 2.0, 2.0,
//			2.0,
//			2.0, 2.0, 2.0,
//			-1.0, 2.0, 2.0, 2.0
//		}));
//			
//		double[][] expectedC2C4vvMax4vvAvg = new double[2][];
//		expectedC2C4vvMax4vvAvg[0] = new double[]{
//			0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, /**/ 
//			0.0, 0.0, 0.0, /**/ 0.0, 1.0, Double.MAX_VALUE, /**/
//			0.0, 0.0, 0.0,
//			1.0, 2.0, 3.0, 0.0,
//			0.0, 0.0, 0.0, 0.0,
//			0.0,
//			0.0, 0.0, 0.0,
//			Double.MAX_VALUE, 0.0, 0.0, 0.0 
//		};
//		expectedC2C4vvMax4vvAvg[1] = new double[]{
//			2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, /**/ 
//			2.0, 2.0, 2.0, /**/ 2.0, 3.0, -Double.MAX_VALUE, /**/
//			2.0, 2.0, 2.0,
//			3.0, 4.0, 5.0, 0.0,
//			2.0, 2.0, 2.0, 2.0,
//			2.0,
//			2.0, 2.0, 2.0,
//			-Double.MAX_VALUE, 2.0, 2.0, 2.0 
//		};
//		expected.add(expectedC2C4vvMax4vvAvg);
//		
//		// d. Config 4
//		List<List<Double>> fvC2C4vvMax5vvAvg = new ArrayList<List<Double>>();
//		fvC2C4vvMax5vvAvg.add(Arrays.asList(new Double[]{
//			0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ -1.0, -1.0, -1.0, -1.0, /**/ 
//			0.0, 0.0, 0.0, /**/ 0.0, 1.0, 2.0, -1.0, /**/
//			0.0, 0.0, 0.0, 
//			0.0, 1.0, -2.0, 3.0, 0.0,
//			0.0, 0.0, 0.0, 0.0, 0.0,
//			0.0,
//			0.0, 0.0, 0.0,
//			0.0, 0.0, 0.0, 0.0, -1.0
//		}));
//		fvC2C4vvMax5vvAvg.add(Arrays.asList(new Double[]{
//			1.0, 1.0, 1.0, 1.0, /**/ 1.0, 1.0, 1.0, 1.0, /**/ 1.0, 1.0, 1.0, 1.0, /**/ 1.0, 1.0, 1.0, 1.0, /**/ -1.0, -1.0, -1.0, -1.0, /**/ 
//			1.0, 1.0, 1.0, /**/ 1.0, 2.0, 3.0, -1.0, /**/
//			1.0, 1.0, 1.0, 
//			1.0, 2.0, 3.0, -5.0, 0.0,
//			1.0, 1.0, 1.0, 1.0, 1.0,
//			1.0,
//			1.0, 1.0, 1.0,
//			1.0, 1.0, 1.0, 1.0, -1.0
//		}));
//		fvC2C4vvMax5vvAvg.add(Arrays.asList(new Double[]{
//			2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ -1.0, -1.0, -1.0, -1.0, /**/ 
//			2.0, 2.0, 2.0, /**/ 2.0, 3.0, 4.0, -1.0, /**/
//			2.0, 2.0, 2.0, 
//			2.0, 3.0, -4.0, 4.0, 0.0,
//			2.0, 2.0, 2.0, 2.0, 2.0,
//			2.0,
//			2.0, 2.0, 2.0,
//			2.0, 2.0, 2.0, 2.0, -1.0
//		}));
//			
//		double[][] expectedC2C4vvMax5vvAvg = new double[2][];
//		expectedC2C4vvMax5vvAvg[0] = new double[]{
//			0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, /**/ Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, /**/ 
//			0.0, 0.0, 0.0, /**/ 0.0, 1.0, 2.0, Double.MAX_VALUE, /**/
//			0.0, 0.0, 0.0, 
//			0.0, 1.0, 2.0, 3.0, 0.0,
//			0.0, 0.0, 0.0, 0.0, 0.0,
//			0.0,
//			0.0, 0.0, 0.0,
//			0.0, 0.0, 0.0, 0.0, Double.MAX_VALUE
//		};
//		expectedC2C4vvMax5vvAvg[1] = new double[]{
//			2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ 2.0, 2.0, 2.0, 2.0, /**/ -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, /**/ 
//			2.0, 2.0, 2.0, /**/ 2.0, 3.0, 4.0, -Double.MAX_VALUE, /**/
//			2.0, 2.0, 2.0, 
//			2.0, 3.0, 4.0, 5.0, 0.0,
//			2.0, 2.0, 2.0, 2.0, 2.0,
//			2.0,
//			2.0, 2.0, 2.0,
//			2.0, 2.0, 2.0, 2.0, -Double.MAX_VALUE
//		};
//		expected.add(expectedC2C4vvMax5vvAvg);
//		
//		
////		// b. !isTablatureCase, N2N, useFixedValues
////		double[][] expectedB = new double[2][32];
////		expectedB[0] = new double[]{24.0, 0.0, 0.0, 0.0, /**/ 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
////			0.0, 0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, 0.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, /**/ 
////			0.0, 0.0, 0.0, 0.0, 0.0};
////		expectedB[1] = new double[]{96.0, 8.0, 1.0, 1.0, /**/ 5.0, 4.0, 72.0, 72.0, 72.0, 72.0, 72.0, 72.0,
////			1.0, 1.0, 1.0, 1.0, 1.0, /**/ 1.0, 1.0, 1.0, 1.0, 1.0, /**/ 1.0, 1.0, 1.0, 1.0, 1.0, /**/ 
////			1.0, 1.0, 1.0, 1.0, 1.0};
//////		expected.add(expectedB);
////		
////		// isTablatureCase, N2N, useFixedValues
////		double[][] expectedB1 = new double[2][41];
////		expectedB1[0] = new double[]{24.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0,  /**/ 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
////			0.0, 0.0, 0.0, 0.0, 0.0,
////			0.0, 0.0, 0.0, 0.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, 0.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, /**/ 
////			0.0, 0.0, 0.0, 0.0, 0.0};
////		expectedB1[1] = new double[]{96.0, 10.0, 12.0, 8.0, 8.0, 1.0, 1.0, 5.0, /**/ 5.0, 4.0, 72.0, 72.0, 72.0, 72.0, 72.0, 72.0,
////			1.0, 1.0, 1.0, 1.0, 1.0,
////			1.0, 1.0, 1.0, 1.0, 1.0, /**/ 1.0, 1.0, 1.0, 1.0, 1.0, /**/ 1.0, 1.0, 1.0, 1.0, 1.0, /**/ 
////			1.0, 1.0, 1.0, 1.0, 1.0};
//////		expected.add(expectedB1);
////			
////		// c. !isTablatureCase, C2C, !useFixedValues
////		List<List<Double>> setOfFeatureVectorsC2C = new ArrayList<List<Double>>();
////		Double[] ones = new Double[61];
////		Arrays.fill(ones, 0.1); 
////		setOfFeatureVectorsC2C.add(Arrays.asList(ones));
////		Double[] twos = new Double[61];
////		Arrays.fill(twos, 0.2); 
////		setOfFeatureVectorsC2C.add(Arrays.asList(twos));
////		setOfFeatureVectorsC2C.add(Arrays.asList(new Double[]{
////			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,
////			0.3, 0.3, 0.3, -1.0, -1.0, -1.0, -1.0, 
////			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,
////			-0.3, -0.3, -0.3, -0.3, -0.3,
////			0.3, 0.3, 0.3, 0.3, 0.3, 
////			-1.0, 
////			0.3, 0.3, 0.3, 
////			-1.0, -1.0, -1.0, -1.0, -1.0}));
////		
////		double[][] expectedC = new double[2][61];
////		expectedC[0] = new double[]{
////			0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 
////			0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1,
////			0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1,
////			0.1, 0.1, 0.1, 0.1, 0.1,
////			0.1, 0.1, 0.1, 0.1, 0.1,
////			-1.0,
////			0.1, 0.1, 0.1,
////			0.1, 0.1, 0.1, 0.1, 0.1
////		};
////		expectedC[1] = new double[]{
////			0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 
////			0.3, 0.3, 0.3, 0.2, 0.2, 0.2, 0.2,
////			0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2,
////			0.3, 0.3, 0.3, 0.3, 0.3,
////			0.3, 0.3, 0.3, 0.3, 0.3,
////			0.2,
////			0.3, 0.3, 0.3,
////			0.2, 0.2, 0.2, 0.2, 0.2
////		};
//////		expected.add(expectedC);
////		
////		// d. !isTablatureCase, C2C, !useFixedValues
////		double[][] expectedD = new double[2][61];
////		expectedD[0] = new double[]{
////			0.0, 0.0, 24.0, 0.0, 0.0, 0.0, 24.0, 0.0, 0.0, 0.0, 24.0, 0.0, 0.0, 0.0, 24.0, 0.0, 0.0, 0.0, 24.0, 0.0, 
////			1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
////			0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, -1.0, -1.0, -1.0, -1.0,  
////			0.0, 0.0, 0.0, 0.0, 0.0,
////			0.0, 0.0, 0.0, 0.0, 0.0,
////			-1.0,
////			0.0, 0.0, 0.0,
////			0.0, 0.0, 0.0, 0.0, 0.0
////		};
////		expectedD[1] = new double[]{
////			4.0, 4.0, 96.0, 8.0, 4.0, 4.0, 96.0, 8.0, 4.0, 4.0, 96.0, 8.0, 4.0, 4.0, 96.0, 8.0, 4.0, 4.0, 96.0, 8.0, 
////			5.0, 1.0, 1.0, 72.0, 72.0, 72.0, 72.0,
////			1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
////			72.0, 72.0, 72.0, 72.0, 72.0,
////			1.0, 1.0, 1.0, 1.0, 1.0,
////			1.0,
////			10.0, 720.0, 72.0,
////			4.0, 4.0, 4.0, 4.0, 4.0
////		};
//////		expected.add(expectedD);
//		
//		// Calculate actual
//		List<double[][]> actual = new ArrayList<double[][]>();
//		// 1.
//		// a.
//		FeatureGenerator.determineScalingSettings(ExperimentRunner.NOTE_TO_NOTE, false, -1);
//		actual.add(FeatureGenerator.getMinAndMaxValuesSetOfFeatureVectors(fvN2N));
//		// 2.
//		// a.
//		FeatureGenerator.determineScalingSettings(ExperimentRunner.CHORD_TO_CHORD, false, 1);
//		actual.add(FeatureGenerator.getMinAndMaxValuesSetOfFeatureVectors(fvC2C4vvMax4vv));
//		// b.
//		FeatureGenerator.determineScalingSettings(ExperimentRunner.CHORD_TO_CHORD, false, 2);
//		actual.add(FeatureGenerator.getMinAndMaxValuesSetOfFeatureVectors(fvC2C4vvMax5vv));
//		// c.
//		FeatureGenerator.determineScalingSettings(ExperimentRunner.CHORD_TO_CHORD, false, 3);
//		actual.add(FeatureGenerator.getMinAndMaxValuesSetOfFeatureVectors(fvC2C4vvMax4vvAvg));
//		// d. 
//		FeatureGenerator.determineScalingSettings(ExperimentRunner.CHORD_TO_CHORD, false, 4);
//		actual.add(FeatureGenerator.getMinAndMaxValuesSetOfFeatureVectors(fvC2C4vvMax5vvAvg));
//				
//		// Assert equality
//		assertEquals(expected.size(), actual.size());
//		for (int i = 0; i < expected.size(); i++) {
//			assertEquals(expected.get(i).length, actual.get(i).length);
//			for (int j = 0; j < expected.get(i).length; j++) {
//				assertEquals(expected.get(i)[j].length, actual.get(i)[j].length);
//				for (int k = 0; k < expected.get(i)[j].length; k++) {
//					System.out.println(i + " " + j + " " + k);
//					assertEquals(expected.get(i)[j][k], actual.get(i)[j][k]);
//				}
//			}
//		}
//		
	}
	
	
//	public void testGetIndividualNoteFeaturesChordOLD() {
//	  // Make a Tablature and preprocess it
//		File encodingFile = new File(TrainingManager.prefix + "Encodings/Tests/Testpiece 1.txt");
//		Tablature tablature = new Tablature(encodingFile);
//		preprocessor.prepareInitialInformation(tablature, null, true);
//		
//    // For each chord: determine the expected individual onset features
//    List<List<List<Double>>> expected = new ArrayList<List<List<Double>>>();
//    List<Double> missingOnsetFeatures = Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0}); 	
//    // Chord 0
//    List<List<Double>> individualOnsetFeaturesChord0 = new ArrayList<List<Double>>();
//    List<Double> featuresOnset00 = Arrays.asList(new Double[]{48.0, 5.0, 0.0, 1/2.0, 0.0, 3.0, -1.0, 7.0});
//    List<Double> featuresOnset01 = Arrays.asList(new Double[]{55.0, 4.0, 2.0, 1/4.0, 1.0, 2.0, 7.0, 8.0});
//    List<Double> featuresOnset02 = Arrays.asList(new Double[]{63.0, 2.0, 1.0, 1/4.0, 2.0, 1.0, 8.0, 4.0});
//    List<Double> featuresOnset03 = Arrays.asList(new Double[]{67.0, 1.0, 0.0, 1/4.0, 3.0, 0.0, 4.0, -1.0}); 
//    individualOnsetFeaturesChord0.add(featuresOnset00); individualOnsetFeaturesChord0.add(featuresOnset01);
//    individualOnsetFeaturesChord0.add(featuresOnset02); individualOnsetFeaturesChord0.add(featuresOnset03);
//    individualOnsetFeaturesChord0.add(missingOnsetFeatures);
//   	// Chord 1
//    List<List<Double>> individualOnsetFeaturesChord1 = new ArrayList<List<Double>>();
//   	List<Double> featuresOnset10 = Arrays.asList(new Double[]{43.0, 6.0, 0.0, 1/8.0, 0.0, 3.0, -1.0, 12.0});
//   	List<Double> featuresOnset11 = Arrays.asList(new Double[]{55.0, 4.0, 2.0, 1/4.0, 1.0, 2.0, 12.0, 15.0});
//   	List<Double> featuresOnset12 = Arrays.asList(new Double[]{70.0, 2.0, 8.0, 1/4.0, 2.0, 1.0, 15.0, 3.0});
//   	List<Double> featuresOnset13 = Arrays.asList(new Double[]{67.0, 1.0, 0.0, 1/2.0, 3.0, 0.0, 3.0, -1.0}); 
//   	individualOnsetFeaturesChord1.add(featuresOnset10); individualOnsetFeaturesChord1.add(featuresOnset11);
//   	individualOnsetFeaturesChord1.add(featuresOnset12);	individualOnsetFeaturesChord1.add(featuresOnset13);
//   	individualOnsetFeaturesChord1.add(missingOnsetFeatures);
//   	// Chord 2
//    List<List<Double>> individualOnsetFeaturesChord2 = new ArrayList<List<Double>>();
//    List<Double> featuresOnset20 = Arrays.asList(new Double[]{46.0, 6.0, 3.0, 1/8.0, 0.0, 0.0, -1.0, -1.0});
//    individualOnsetFeaturesChord2.add(featuresOnset20); individualOnsetFeaturesChord2.add(missingOnsetFeatures);
//    individualOnsetFeaturesChord2.add(missingOnsetFeatures); individualOnsetFeaturesChord2.add(missingOnsetFeatures);
//    individualOnsetFeaturesChord2.add(missingOnsetFeatures);
//    // Chord 3
//    List<List<Double>> individualOnsetFeaturesChord3 = new ArrayList<List<Double>>();
//    List<Double> featuresOnset30 = Arrays.asList(new Double[]{45.0, 6.0, 2.0, 1/4.0, 0.0, 3.0, -1.0, 3.0});
//    List<Double> featuresOnset31 = Arrays.asList(new Double[]{48.0, 5.0, 0.0, 1/4.0, 1.0, 2.0, 3.0, 9.0});
//    List<Double> featuresOnset32 = Arrays.asList(new Double[]{57.0, 4.0, 4.0, 1/4.0, 2.0, 1.0, 9.0, 6.0});
//    List<Double> featuresOnset33 = Arrays.asList(new Double[]{63.0, 2.0, 1.0, 1/2.0, 3.0, 0.0, 6.0, -1.0});
//    individualOnsetFeaturesChord3.add(featuresOnset30); individualOnsetFeaturesChord3.add(featuresOnset31);
//    individualOnsetFeaturesChord3.add(featuresOnset32);	individualOnsetFeaturesChord3.add(featuresOnset33);
//    individualOnsetFeaturesChord3.add(missingOnsetFeatures); 
//    // Chord 4
//    List<List<Double>> individualOnsetFeaturesChord4 = new ArrayList<List<Double>>();
//    List<Double> featuresOnset40 = Arrays.asList(new Double[]{43.0, 6.0, 0.0, 1/4.0, 0.0, 4.0, -1.0, 12.0});
//    List<Double> featuresOnset41 = Arrays.asList(new Double[]{55.0, 5.0, 7.0, 3/2.0, 1.0, 3.0, 12.0, 0.0});
//    List<Double> featuresOnset42 = Arrays.asList(new Double[]{55.0, 4.0, 2.0, 1/2.0, 2.0, 2.0, 0.0, 3.0});
//    List<Double> featuresOnset43 = Arrays.asList(new Double[]{58.0, 3.0, 1.0, 1/4.0, 3.0, 1.0, 3.0, 9.0});
//    List<Double> featuresOnset44 = Arrays.asList(new Double[]{67.0, 1.0, 0.0, 1/4.0, 4.0, 0.0, 9.0, -1.0});
//    individualOnsetFeaturesChord4.add(featuresOnset40); individualOnsetFeaturesChord4.add(featuresOnset41);
//    individualOnsetFeaturesChord4.add(featuresOnset42);	individualOnsetFeaturesChord4.add(featuresOnset43);
//    individualOnsetFeaturesChord4.add(featuresOnset44); 	
//    // Chord 5
//   	List<List<Double>> individualOnsetFeaturesChord5 = new ArrayList<List<Double>>();
//   	List<Double> featuresOnset50 = Arrays.asList(new Double[]{43.0, 6.0, 0.0, 1/4.0, 0.0, 3.0, -1.0, 15.0});
//   	List<Double> featuresOnset51 = Arrays.asList(new Double[]{58.0, 3.0, 1.0, 1/8.0, 1.0, 2.0, 15.0, 4.0});
//   	List<Double> featuresOnset52 = Arrays.asList(new Double[]{62.0, 2.0, 0.0, 1/8.0, 2.0, 1.0, 4.0, 5.0});
//   	List<Double> featuresOnset53 = Arrays.asList(new Double[]{67.0, 1.0, 0.0, 1/4.0, 3.0, 0.0, 5.0, -1.0});
//   	individualOnsetFeaturesChord5.add(featuresOnset50); individualOnsetFeaturesChord5.add(featuresOnset51);
//   	individualOnsetFeaturesChord5.add(featuresOnset52); individualOnsetFeaturesChord5.add(featuresOnset53);
//   	individualOnsetFeaturesChord5.add(missingOnsetFeatures); 
//   	// Chord 6
//    List<List<Double>> individualOnsetFeaturesChord6 = new ArrayList<List<Double>>();
//    List<Double> featuresOnset60 = Arrays.asList(new Double[]{57.0, 3.0, 0.0, 9/8.0, 0.0, 1.0, -1.0, 9.0});
//    List<Double> featuresOnset61 = Arrays.asList(new Double[]{66.0, 2.0, 4.0, 1/8.0, 1.0, 0.0, 9.0, -1.0});
//    individualOnsetFeaturesChord6.add(featuresOnset60); individualOnsetFeaturesChord6.add(featuresOnset61);
//    individualOnsetFeaturesChord6.add(missingOnsetFeatures); individualOnsetFeaturesChord6.add(missingOnsetFeatures);
//    individualOnsetFeaturesChord6.add(missingOnsetFeatures);    
//    // Chord 7
//    List<List<Double>> individualOnsetFeaturesChord7 = new ArrayList<List<Double>>();
//    List<Double> featuresOnset70 = Arrays.asList(new Double[]{43.0, 6.0, 0.0, 3/4.0, 0.0, 3.0, -1.0, 12.0});
//    List<Double> featuresOnset71 = Arrays.asList(new Double[]{55.0, 4.0, 2.0, 3/4.0, 1.0, 2.0, 12.0, 7.0});
//    List<Double> featuresOnset72 = Arrays.asList(new Double[]{62.0, 2.0, 0.0, 3/4.0, 2.0, 1.0, 7.0, 5.0});
//    List<Double> featuresOnset73 = Arrays.asList(new Double[]{67.0, 1.0, 0.0, 3/4.0, 3.0, 0.0, 5.0, -1.0});
//    individualOnsetFeaturesChord7.add(featuresOnset70);	individualOnsetFeaturesChord7.add(featuresOnset71);
//    individualOnsetFeaturesChord7.add(featuresOnset72);	individualOnsetFeaturesChord7.add(featuresOnset73);
//    individualOnsetFeaturesChord7.add(missingOnsetFeatures);
//    // Chord 8
//    List<List<Double>> individualOnsetFeaturesChord8 = new ArrayList<List<Double>>();
//    List<Double> featuresOnset80 = Arrays.asList(new Double[]{43.0, 6.0, 0.0, 1/4.0, 0.0, 3.0, -1.0, 12.0});
//    List<Double> featuresOnset81 = Arrays.asList(new Double[]{55.0, 4.0, 2.0, 1/4.0, 1.0, 2.0, 12.0, 7.0});
//    List<Double> featuresOnset82 = Arrays.asList(new Double[]{62.0, 2.0, 0.0, 1/4.0, 2.0, 1.0, 7.0, 5.0});
//    List<Double> featuresOnset83 = Arrays.asList(new Double[]{67.0, 1.0, 0.0, 1/4.0, 3.0, 0.0, 5.0, -1.0});
//    individualOnsetFeaturesChord8.add(featuresOnset80); individualOnsetFeaturesChord8.add(featuresOnset81);
//    individualOnsetFeaturesChord8.add(featuresOnset82);	individualOnsetFeaturesChord8.add(featuresOnset83);
//    individualOnsetFeaturesChord8.add(missingOnsetFeatures);
//    	
//    // Add the expected individual onset features for each chord to expected
//    expected.add(individualOnsetFeaturesChord0); expected.add(individualOnsetFeaturesChord1); 
//    expected.add(individualOnsetFeaturesChord2); expected.add(individualOnsetFeaturesChord3);
//    expected.add(individualOnsetFeaturesChord4); expected.add(individualOnsetFeaturesChord5); 
//    expected.add(individualOnsetFeaturesChord6); expected.add(individualOnsetFeaturesChord7);
//    expected.add(individualOnsetFeaturesChord8);
//    	
//    // For each chord: calculate the actual individual onset features
//    List<List<List<Double>>> actual = new ArrayList<List<List<Double>>>();
//    List<List<TabSymbol>> tablatureChords = tablature.getTablatureChords();
//    Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//    int lowestOnsetIndex = 0;
//	  for (int i = 0; i < tablatureChords.size(); i++) {
//	   	List<TabSymbol> currentChord = tablatureChords.get(i);
////	   	List<List<Integer>> currentChordOnsetProperties = tablature.getChordOnsetProperties(i);
//	   	List<List<Double>> currentActual = featureGenerator.getIndividualNoteFeaturesChordOLD(tablature,
//	   		basicTabSymbolProperties, lowestOnsetIndex, true);
//	   	actual.add(currentActual);
//	   	lowestOnsetIndex += currentChord.size();
//	  }
//	    
//	  // Assert equality
//	  assertEquals(expected.size(), actual.size());
//	  for (int i = 0; i < expected.size(); i++) {
//	  	assertEquals(expected.get(i).size(), actual.get(i).size()); 
//	  	for (int j = 0; j < expected.get(i).size(); j++) {
//	  		assertEquals(expected.get(i).get(j).size(), actual.get(i).get(j).size());
//	  		for (int k = 0; k < expected.get(i).get(j).size(); k++) {
//    			assertEquals(expected.get(i).get(j).get(k), actual.get(i).get(j).get(k));
//	  		}
//	  	}
//	  }
//	  assertEquals(expected, actual);
//	}
	
	
//	public void testGetSharedNoteFeaturesChordOLD() {
//	  // Make a Tablature and preprocess it
//		File encodingFile = new File(TrainingManager.prefix + "Encodings/Tests/Testpiece 1.txt");
//		Tablature tablature = new Tablature(encodingFile);
//		preprocessor.prepareInitialInformation(tablature, null, true);
//		
//    // For each chord: determine the expected shared onset features
//    List<List<Double>> expected = new ArrayList<List<Double>>();
//    // Chord 0
//    Integer[][] basicTabSymbolPropertiesChord0 = tablature.getBasicTabSymbolPropertiesChord(0);
//    double rangeChord0 = featureGenerator.getRangeOfChord(basicTabSymbolPropertiesChord0);
//    double skewnessChord0 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord0);
//    List<Double> sharedOnsetFeaturesChord0 = new ArrayList<Double>(Arrays.asList(new Double[]{1/4.0, 4.0, 0.0, rangeChord0, skewnessChord0}));
//    double[] intervalsInChord0 = featureGenerator.getIntervalsInChordOLD(basicTabSymbolPropertiesChord0);
//    for (int i = 0; i < intervalsInChord0.length; i++) {
//    	sharedOnsetFeaturesChord0.add(intervalsInChord0[i]);
//    }
//    // Chord 1
//    Integer[][] basicTabSymbolPropertiesChord1 = tablature.getBasicTabSymbolPropertiesChord(1);
//    double rangeChord1 = featureGenerator.getRangeOfChord(basicTabSymbolPropertiesChord1);
//    double skewnessChord1 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord1);
//    List<Double> sharedOnsetFeaturesChord1 = new ArrayList<Double>(Arrays.asList(new Double[]{1/8.0, 4.0, 0.0, rangeChord1, skewnessChord1}));
//    double[] intervalsInChord1 = featureGenerator.getIntervalsInChordOLD(basicTabSymbolPropertiesChord1);
//    for (int i = 0; i < intervalsInChord0.length; i++) {
//    	sharedOnsetFeaturesChord1.add(intervalsInChord1[i]);
//    }
//    // Chord 2
//    Integer[][] basicTabSymbolPropertiesChord2 = tablature.getBasicTabSymbolPropertiesChord(2);
//    double rangeChord2 = featureGenerator.getRangeOfChord(basicTabSymbolPropertiesChord2);
//    double skewnessChord2 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord2);
//    List<Double> sharedOnsetFeaturesChord2 = new ArrayList<Double>(Arrays.asList(new Double[]{1/8.0, 1.0, 0.0, rangeChord2, skewnessChord2}));
//    double[] intervalsInChord2 = featureGenerator.getIntervalsInChordOLD(basicTabSymbolPropertiesChord2);
//    for (int i = 0; i < intervalsInChord0.length; i++) {
//    	sharedOnsetFeaturesChord2.add(intervalsInChord2[i]);
//    }
//    // Chord 3
//    Integer[][] basicTabSymbolPropertiesChord3 = tablature.getBasicTabSymbolPropertiesChord(3);
//    double rangeChord3 = featureGenerator.getRangeOfChord(basicTabSymbolPropertiesChord3);
//    double skewnessChord3 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord3);
//    List<Double> sharedOnsetFeaturesChord3 = new ArrayList<Double>(Arrays.asList(new Double[]{1/4.0, 4.0, 0.0, rangeChord3, skewnessChord3}));
//    double[] intervalsInChord3 = featureGenerator.getIntervalsInChordOLD(basicTabSymbolPropertiesChord3);
//    for (int i = 0; i < intervalsInChord0.length; i++) {
//    	sharedOnsetFeaturesChord3.add(intervalsInChord3[i]);
//    }
//    // Chord 4
//    Integer[][] basicTabSymbolPropertiesChord4 = tablature.getBasicTabSymbolPropertiesChord(4);
//    double rangeChord4 = featureGenerator.getRangeOfChord(basicTabSymbolPropertiesChord4);
//    double skewnessChord4 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord4);
//    List<Double> sharedOnsetFeaturesChord4 = new ArrayList<Double>(Arrays.asList(new Double[]{1/4.0, 5.0, 0.0, rangeChord4, skewnessChord4}));
//    double[] intervalsInChord4 = featureGenerator.getIntervalsInChordOLD(basicTabSymbolPropertiesChord4);
//    for (int i = 0; i < intervalsInChord0.length; i++) {
//    	sharedOnsetFeaturesChord4.add(intervalsInChord4[i]);
//    }
//    // Chord 5
//    Integer[][] basicTabSymbolPropertiesChord5 = tablature.getBasicTabSymbolPropertiesChord(5);
//    double rangeChord5 = featureGenerator.getRangeOfChord(basicTabSymbolPropertiesChord5);
//    double skewnessChord5 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord5);
//    List<Double> sharedOnsetFeaturesChord5 = new ArrayList<Double>(Arrays.asList(new Double[]{1/8.0, 4.0, 0.0, rangeChord5, skewnessChord5}));
//    double[] intervalsInChord5 = featureGenerator.getIntervalsInChordOLD(basicTabSymbolPropertiesChord5);
//    for (int i = 0; i < intervalsInChord5.length; i++) {
//    	sharedOnsetFeaturesChord5.add(intervalsInChord5[i]);
//    }
//    // Chord 6
//    Integer[][] basicTabSymbolPropertiesChord6 = tablature.getBasicTabSymbolPropertiesChord(6);
//    double rangeChord6 = featureGenerator.getRangeOfChord(basicTabSymbolPropertiesChord6);
//    double skewnessChord6 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord6);
//    List<Double> sharedOnsetFeaturesChord6 = new ArrayList<Double>(Arrays.asList(new Double[]{1/8.0, 2.0, 0.0, rangeChord6, skewnessChord6}));
//    double[] intervalsInChord6 = featureGenerator.getIntervalsInChordOLD(basicTabSymbolPropertiesChord6);
//    for (int i = 0; i < intervalsInChord0.length; i++) {
//    	sharedOnsetFeaturesChord6.add(intervalsInChord6[i]);
//    }
//    // Chord 7
//    Integer[][] basicTabSymbolPropertiesChord7 = tablature.getBasicTabSymbolPropertiesChord(7);
//    double rangeChord7 = featureGenerator.getRangeOfChord(basicTabSymbolPropertiesChord7);
//    double skewnessChord7 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord7);
//    List<Double> sharedOnsetFeaturesChord7 = new ArrayList<Double>(Arrays.asList(new Double[]{1/2.0, 4.0, 0.0, rangeChord7, skewnessChord7}));
//    double[] intervalsInChord7 = featureGenerator.getIntervalsInChordOLD(basicTabSymbolPropertiesChord7);
//    for (int i = 0; i < intervalsInChord0.length; i++) {
//    	sharedOnsetFeaturesChord7.add(intervalsInChord7[i]);
//    }
//    // Chord 8
//    Integer[][] basicTabSymbolPropertiesChord8 = tablature.getBasicTabSymbolPropertiesChord(8);
//    double rangeChord8 = featureGenerator.getRangeOfChord(basicTabSymbolPropertiesChord8);
//    double skewnessChord8 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord8);
//    List<Double> sharedOnsetFeaturesChord8 = new ArrayList<Double>(Arrays.asList(new Double[]{1/4.0, 4.0, 0.0, rangeChord8, skewnessChord8}));
//    double[] intervalsInChord8 = featureGenerator.getIntervalsInChordOLD(basicTabSymbolPropertiesChord8);
//    for (int i = 0; i < intervalsInChord0.length; i++) {
//    	sharedOnsetFeaturesChord8.add(intervalsInChord8[i]);
//    }
//    
//    // Add the expected shared onset features for each chord to expected
//    expected.add(sharedOnsetFeaturesChord0); expected.add(sharedOnsetFeaturesChord1);
//    expected.add(sharedOnsetFeaturesChord2); expected.add(sharedOnsetFeaturesChord3);
//    expected.add(sharedOnsetFeaturesChord4); expected.add(sharedOnsetFeaturesChord5); 
//    expected.add(sharedOnsetFeaturesChord6); expected.add(sharedOnsetFeaturesChord7);
//    expected.add(sharedOnsetFeaturesChord8);
//    	
//    // For each chord: calculate the actual shared onset features
//    List<List<Double>> actual = new ArrayList<List<Double>>();
//    List<List<TabSymbol>> tablatureChords = tablature.getTablatureChords();
//    Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//	  int lowestOnsetIndex = 0;
//	  for (int i = 0; i < tablatureChords.size(); i++) {
//	  	List<TabSymbol> currentChord = tablatureChords.get(i);
////	   	List<List<Integer>> currentChordOnsetProperties = tablature.getChordOnsetProperties(i);
////	  	int chordIndex = basicTabSymbolProperties[lowestOnsetIndex][Tablature.CHORD_SEQ_NUM];
////		 	Integer[][] currentBasicTabSymbolPropertiesChord = tablature.getBasicTabSymbolPropertiesChord(i);
//	  	List<Double> currentActual = featureGenerator.getSharedNoteFeaturesChordOLD(tablature,
//	  		basicTabSymbolProperties, lowestOnsetIndex, true);
//	   	actual.add(currentActual);
//		 	lowestOnsetIndex += currentChord.size();
//	  }
//	    
//	  // Assert equality
//	  assertEquals(expected.size(), actual.size());
//	  for (int i = 0; i < expected.size(); i++) {
//	  	assertEquals(expected.get(i).size(), actual.get(i).size()); 
//	  	for (int j = 0; j < expected.get(i).size(); j++) {
//	  		assertEquals(expected.get(i).get(j), actual.get(i).get(j));
//	  	}
//	  }
//	  assertEquals(expected, actual);
//	}
	
	
//	public void testGetIntervalsInChordOLD() {
//	  // Make a Tablature and preprocess it
//		File encodingFile = new File(TrainingManager.prefix + "Encodings/Tests/Testpiece 1.txt");
//		Tablature tablature = new Tablature(encodingFile);
//		preprocessor.prepareInitialInformation(tablature, null, true);
//				  
//		// For each chord: determine the expected intervals and add them to expected
//		List<double[]> expected = new ArrayList<double[]>();
//		double[] intervalsChord0 = new double[]{7.0, 8.0, 4.0, -1.0};
//		double[] intervalsChord1 = new double[]{12.0, 15.0, 3.0, -1.0};
//		double[] intervalsChord2 = new double[]{-1.0, -1.0, -1.0, -1.0};
//		double[] intervalsChord3 = new double[]{3.0, 9.0, 6.0, -1.0};
//		double[] intervalsChord4 = new double[]{12.0, 0.0, 3.0, 9.0};
//		double[] intervalsChord5 = new double[]{15.0, 4.0, 5.0, -1.0};
//		double[] intervalsChord6 = new double[]{9.0, -1.0, -1.0, -1.0};
//		double[] intervalsChord7 = new double[]{12.0, 7.0, 5.0, -1.0};
//		double[] intervalsChord8 = new double[]{12.0, 7.0, 5.0, -1.0};
//		
//		expected.add(intervalsChord0); expected.add(intervalsChord1); expected.add(intervalsChord2);
//		expected.add(intervalsChord3); expected.add(intervalsChord4); expected.add(intervalsChord5);
//		expected.add(intervalsChord6); expected.add(intervalsChord7); expected.add(intervalsChord8);
//		  
//	  // For each chord: determine the actual intervals and add them to expected
//		List<double[]> actual = new ArrayList<double[]>();
//		List<List<TabSymbol>> tablatureChords = tablature.getTablatureChords();
//		for (int i = 0; i < tablatureChords.size(); i++) {
////			List<List<Integer>> currentChordOnsetProperties = tablature.getChordOnsetProperties(i);
//			Integer[][] currentTabSymbolPropertiesChord = tablature.getBasicTabSymbolPropertiesChord(i);
//		 	double[] currentActual = featureGenerator.getIntervalsInChordOLD(currentTabSymbolPropertiesChord);
//		 	actual.add(currentActual);
//		}
//			
//		// Assert equality
//		assertEquals(expected.size(), actual.size());
//	   for (int i = 0; i < expected.size(); i++) {
//	   	assertEquals(expected.get(i).length, actual.get(i).length);
//	   	for (int j = 0; j < expected.get(i).length; j++) {
//	   		assertEquals(expected.get(i)[j], actual.get(i)[j]);
//	   	} 
//	  }
//	}
	
	
//	public void testGetAverageProximitiesAndMovementsOfChordOLD() {
//		// Make a Tablature and Transcription and preprocess them
//    File encodingFile = new File(TrainingManager.prefix + "Encodings/Tests/Testpiece 1.txt");
//    File midiFile = new File(TrainingManager.prefix + "MIDI/Tests/Testpiece 1");
//    Tablature tablature = new Tablature(encodingFile);
//    Transcription transcription = midiImport.importMidiFiles(midiFile);
//    boolean preprocessingSuccessful = preprocessor.preprocess(tablature, transcription, true, null);
//    
//    // Continue only if preprocessing is successful
//    if (preprocessingSuccessful == true) {
//      // For each chord: determine the voice assignment and add it to voiceAssignments
//    	List<List<Integer>> voiceAssignments = new ArrayList<List<Integer>>();
//      // Chord 0: voice 0 has onset 3; voice 1 has onset 2; voice 2 has onset 1; voice 3 has onset 0; voice 4 is not in use 
//    	List<Integer> voiceAssignment0 = Arrays.asList(new Integer[]{3, 2, 1, 0, -1}); 
//      // Chord 1: voice 0 has onset 2; voice 1 has onset 3; voice 2 has onset 1; voice 3 has onset 0; voice 4 is not in use
//    	List<Integer> voiceAssignment1 = Arrays.asList(new Integer[]{2, 3, 1, 0, -1});
//      // Chord 2: voice 0 is not in use; voice 1 is not in use; voice 2 is not in use; voice 3 has onset 0; voice 4 is not in use
//    	List<Integer> voiceAssignment2 = Arrays.asList(new Integer[]{-1, -1, -1, 0, -1});
//      // Chord 3: voice 0 has onset 3; voice 1 has onset 3; voice 2 has onset 2; voice 3 has onset 1; voice 4 has onset 0
//    	List<Integer> voiceAssignment3 = Arrays.asList(new Integer[]{3, 3, 2, 1, 0});
//      // Chord 4: voice 0 has onset 4; voice 1 has onset 3; voice 2 has onset 2; voice 3 has onset 1; voice 4 has onset 0
//    	List<Integer> voiceAssignment4 = Arrays.asList(new Integer[]{4, 3, 2, 1, 0});
//      // Chord 5: voice 0 has onset 2; voice 1 has onset 3; voice 2 has onset 1; voice 3 is not in use; voice 4 has onset 0
//    	List<Integer> voiceAssignment5 = Arrays.asList(new Integer[]{2, 3, 1, -1, 0});
//      // Chord 6: voice 0 has onset 1; voice 1 is not in use; voice 2 has onset 0; voice 3 is not in use; voice 4 is not in use 
//    	List<Integer> voiceAssignment6 = Arrays.asList(new Integer[]{1, -1, 0, -1, -1});
//      // Chord 7: voice 0 has onset 3; voice 1 has onset 2; voice 2 has onset 1; voice 3 has onset 0; voice 4 is not in use 
//    	List<Integer> voiceAssignment7 = Arrays.asList(new Integer[]{3, 2, 1, 0, -1});
//      // Chord 8: voice 0 has onset 3; voice 1 has onset 2; voice 2 has onset 1; voice 3 has onset 0; voice 4 is not in use
//    	List<Integer> voiceAssignment8 = Arrays.asList(new Integer[]{3, 2, 1, 0, -1});
//    	voiceAssignments.add(voiceAssignment0); voiceAssignments.add(voiceAssignment1); voiceAssignments.add(voiceAssignment2); 
//    	voiceAssignments.add(voiceAssignment3); voiceAssignments.add(voiceAssignment4); voiceAssignments.add(voiceAssignment5); 
//    	voiceAssignments.add(voiceAssignment6); voiceAssignments.add(voiceAssignment7); voiceAssignments.add(voiceAssignment8); 
//       	
//    	// For each chord: determine the distances and the pitch movements and add them to expectedAsList
//    	List<double[]> expected = new ArrayList<double[]>();
//    	List<List<List<Double>>> expectedAsList = new ArrayList<List<List<Double>>>();
//    	// Chord 0
//    	List<List<Double>> expected0 = new ArrayList<List<Double>>();
//    	List<Double> pitchProximities0 = Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0});
//    	List<Double> interOnsetTimeProximities0 = Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0});
//    	List<Double> offsetOnsetTimeProximities0 = Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0});
//    	List<Double> pitchMovement0 = Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0});
//      expected0.add(pitchProximities0); expected0.add(interOnsetTimeProximities0); 
//      expected0.add(offsetOnsetTimeProximities0); expected0.add(pitchMovement0);
//    	// Chord 1
//      List<List<Double>> expected1 = new ArrayList<List<Double>>();
//    	List<Double> pitchProximities1 = Arrays.asList(new Double[]{5.0, 0.0, 3.0, 4.0});
//    	List<Double> interOnsetTimeProximities1 = Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0});
//    	List<Double> offsetOnsetTimeProximities1 = Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0});
//    	List<Double> pitchMovement1 = Arrays.asList(new Double[]{3.0, 4.0, 0.0, -5.0, 0.0});
//    	expected1.add(pitchProximities1); expected1.add(interOnsetTimeProximities1); 
//      expected1.add(offsetOnsetTimeProximities1); expected1.add(pitchMovement1);
//    	// Chord 2
//      List<List<Double>> expected2 = new ArrayList<List<Double>>();
//    	List<Double> pitchProximities2 = Arrays.asList(new Double[]{3.0});
//    	List<Double> interOnsetTimeProximities2 = Arrays.asList(new Double[]{1/8.0});
//    	List<Double> offsetOnsetTimeProximities2 = Arrays.asList(new Double[]{0.0});
//    	List<Double> pitchMovement2 = Arrays.asList(new Double[]{0.0, 0.0, 0.0, 3.0, 0.0});
//    	expected2.add(pitchProximities2); expected2.add(interOnsetTimeProximities2); 
//      expected2.add(offsetOnsetTimeProximities2); expected2.add(pitchMovement2);
//    	// Chord 3
//    	List<List<Double>> expected3 = new ArrayList<List<Double>>();
//    	List<Double> pitchProximities3 = Arrays.asList(new Double[]{-1.0, 2.0, 2.0, 7.0, 4.0});
//     	List<Double> interOnsetTimeProximities3 = Arrays.asList(new Double[]{-1.0, 1/8.0, 1/4.0, 1/4.0, 1/4.0});
//     	List<Double> offsetOnsetTimeProximities3 = Arrays.asList(new Double[]{-1.0, 0.0, 1/8.0, 1/8.0, 1/8.0});
//     	List<Double> pitchMovement3 = Arrays.asList(new Double[]{-7.0, -4.0, 2.0, 2.0, 0.0});
//     	expected3.add(pitchProximities3); expected3.add(interOnsetTimeProximities3); 
//      expected3.add(offsetOnsetTimeProximities3); expected3.add(pitchMovement3);
//    	// Chord 4
//    	List<List<Double>> expected4 = new ArrayList<List<Double>>();
//    	List<Double> pitchProximities4 = Arrays.asList(new Double[]{2.0, 7.0, 2.0, 5.0, 4.0});
//     	List<Double> interOnsetTimeProximities4 = Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0});
//     	List<Double> offsetOnsetTimeProximities4 = Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0});
//    	List<Double> pitchMovement4 = Arrays.asList(new Double[]{4.0, -5.0, -2.0, 7.0, -2.0});
//    	expected4.add(pitchProximities4); expected4.add(interOnsetTimeProximities4); 
//      expected4.add(offsetOnsetTimeProximities4); expected4.add(pitchMovement4);
//    	// Chord 5
//    	List<List<Double>> expected5 = new ArrayList<List<Double>>();
//    	List<Double> pitchProximities5 = Arrays.asList(new Double[]{0.0, 3.0, 5.0, 9.0});
//    	List<Double> interOnsetTimeProximities5 = Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0});
//    	List<Double> offsetOnsetTimeProximities5 = Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0});
//    	List<Double> pitchMovement5 = Arrays.asList(new Double[]{-5.0, 9.0, 3.0, 0.0, 0.0});
//    	expected5.add(pitchProximities5); expected5.add(interOnsetTimeProximities5); 
//      expected5.add(offsetOnsetTimeProximities5); expected5.add(pitchMovement5);
//    	// Chord 6
//    	List<List<Double>> expected6 = new ArrayList<List<Double>>();
//    	List<Double> pitchProximities6 = Arrays.asList(new Double[]{1.0, 4.0});
//    	List<Double> interOnsetTimeProximities6 = Arrays.asList(new Double[]{1/8.0, 1/8.0});
//    	List<Double> offsetOnsetTimeProximities6 = Arrays.asList(new Double[]{0.0, 0.0});
//    	List<Double> pitchMovement6 = Arrays.asList(new Double[]{4.0, 0.0, -1.0, 0.0, 0.0});
//    	expected6.add(pitchProximities6); expected6.add(interOnsetTimeProximities6); 
//      expected6.add(offsetOnsetTimeProximities6); expected6.add(pitchMovement6);
//    	// Chord 7
//    	List<List<Double>> expected7 = new ArrayList<List<Double>>();
//    	List<Double> pitchProximities7 = Arrays.asList(new Double[]{12.0, 2.0, 5.0, 1.0});
//    	List<Double> interOnsetTimeProximities7 = Arrays.asList(new Double[]{1/2.0, 1/8.0, 1/4.0, 1/8.0});
//    	List<Double> offsetOnsetTimeProximities7 = Arrays.asList(new Double[]{1/4.0, 0.0, 1/8.0, 0.0});
//    	List<Double> pitchMovement7 = Arrays.asList(new Double[]{1.0, -5.0, -2.0, -12.0, 0.0});
//    	expected7.add(pitchProximities7); expected7.add(interOnsetTimeProximities7); 
//      expected7.add(offsetOnsetTimeProximities7); expected7.add(pitchMovement7);
//    	// Chord 8
//    	List<List<Double>> expected8 = new ArrayList<List<Double>>();
//    	List<Double> pitchProximities8 = Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0});
//    	List<Double> interOnsetTimeProximities8 = Arrays.asList(new Double[]{3/4.0, 3/4.0, 3/4.0, 3/4.0});
//    	List<Double> offsetOnsetTimeProximities8 = Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0});
//    	List<Double> pitchMovement8 = Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0});
//    	expected8.add(pitchProximities8); expected8.add(interOnsetTimeProximities8); 
//      expected8.add(offsetOnsetTimeProximities8); expected8.add(pitchMovement8);
//    	
//      expectedAsList.add(expected0); expectedAsList.add(expected1); expectedAsList.add(expected2); 
//      expectedAsList.add(expected3); expectedAsList.add(expected4); expectedAsList.add(expected5); 
//      expectedAsList.add(expected6); expectedAsList.add(expected7); expectedAsList.add(expected8);
//      
//      // For each element of expectedAsList: turn the elements of the first three Lists from distances into proximities
//   	  for (int i = 0; i < expectedAsList.size(); i++) {
//     		List<List<Double>> currentExpected = expectedAsList.get(i);
//   	  	for (int j = 0; j < currentExpected.size() - 1; j++) {
//   	  		List<Double> currentProximities = currentExpected.get(j);
//   	  		for (int k = 0; k < currentProximities.size(); k++) {
//   	  			double oldValue = currentProximities.get(k);
//   	  			// Do only if oldValue is not -1.0, i.e., if the voice is active
//   	  		  if (oldValue != -1.0) {
//   	  			  double newValue = 1.0/(oldValue + 1);
//   	  		    currentProximities.set(k, newValue);
//   	  		  }
//   	  		}
//   	  	}
//   	  }
//   	  
//   	  // For each element of expectedAsList: average the elements of the first three Lists (the proximities) and add the
//   	  // respective numbers to currentExpected; add all elements of the last List (the pitch movements) to
//   	  // currentExpected; then add currentExpected to expected
//   	  for (int i = 0; i < expectedAsList.size(); i++) {
//    		List<List<Double>> currentExpectedAsList = expectedAsList.get(i);
//     		double[] currentExpected = new double[8];
//  	  	// Add the averages of the proximities to currentExpected
//    		for (int j = 0; j < currentExpectedAsList.size() - 1; j++) {
//  	  		List<Double> currentList = currentExpectedAsList.get(j);
//    		  currentExpected[j] = AuxiliaryTool.getAverage(currentList);
//  	  	}
//  	  	// Add the pitch movements to currentExpected
//    		List<Double> currentPitchMovements = currentExpectedAsList.get(currentExpectedAsList.size() - 1);
//    		for (int j = 0; j < currentPitchMovements.size(); j++) {
//    		  currentExpected[j + 3] = currentPitchMovements.get(j);
//    		}
//    		// Add currentExpected to expected
//  	  	expected.add(currentExpected);
//  	  }
//   	  
//    	// For each chord: calculate the actual average proximities and the pitch movements and add them to actual
//   	  List<double[]> actual = new ArrayList<double[]>();
//    	Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//    	List<List<TabSymbol>> tablatureEvents = tablature.getTablatureChords();
//  		int lowestOnsetIndex = 0;
//  		for (int i = 0; i < tablatureEvents.size(); i++) {
//  			int chordSize = tablatureEvents.get(i).size();
//  	  	double[] currentActual = featureGenerator.getAverageProximitiesAndMovementsOfChordOLD(tablature, basicTabSymbolProperties, 
//      		transcription, chordSize, lowestOnsetIndex, voiceAssignments.get(i));
//  	  	actual.add(currentActual);
//  	  	lowestOnsetIndex += chordSize; 	
//  		}
//    	
//    	// Assert equality
//    	assertEquals(expected.size(), actual.size());
//    	for (int i = 0; i < expected.size(); i++) {
//    		assertEquals(expected.get(i).length, actual.get(i).length);
//    		for (int j = 0; j < expected.get(i).length; j++) {
//    			assertEquals(expected.get(i)[j], actual.get(i)[j]);
//    		}
//    	} 
//    }
//    else {
//    	System.out.println("ERROR: Preprocessing failed." + "\n");
//    	throw new RuntimeException("Preprocessing unsuccessful (see console for details)");
//    }
//	}
	
	
//	public void testGetProximitiesAndMovementsOfChordOLD() {
//		// Make a Tablature and Transcription and preprocess them
//    File encodingFile = new File(TrainingManager.prefix + "Encodings/Tests/Testpiece 1.txt");
//    File midiFile = new File(TrainingManager.prefix + "MIDI/Tests/Testpiece 1");
//    Tablature tablature = new Tablature(encodingFile);
//    Transcription transcription = midiImport.importMidiFiles(midiFile);
//    boolean preprocessingSuccessful = preprocessor.preprocess(tablature, transcription, true, null);
//   
//    // Continue only if preprocessing is successful
//    if (preprocessingSuccessful == true) {
//      // For each chord: determine the voice assignment and add it to voiceAssignments
//   	  List<List<Integer>> voiceAssignments = new ArrayList<List<Integer>>();
//      // Chord 0: voice 0 has onset 3; voice 1 has onset 2; voice 2 has onset 1; voice 3 has onset 0; voice 4 is not in use 
//   	  List<Integer> voiceAssignment0 = Arrays.asList(new Integer[]{3, 2, 1, 0, -1}); 
//      // Chord 1: voice 0 has onset 2; voice 1 has onset 3; voice 2 has onset 1; voice 3 has onset 0; voice 4 is not in use
//   	  List<Integer> voiceAssignment1 = Arrays.asList(new Integer[]{2, 3, 1, 0, -1});
//      // Chord 2: voice 0 is not in use; voice 1 is not in use; voice 2 is not in use; voice 3 has onset 0; voice 4 is not in use
//   	  List<Integer> voiceAssignment2 = Arrays.asList(new Integer[]{-1, -1, -1, 0, -1});
//      // Chord 3: voice 0 has onset 3; voice 1 has onset 3; voice 2 has onset 2; voice 3 has onset 1; voice 4 has onset 0
//   	  List<Integer> voiceAssignment3 = Arrays.asList(new Integer[]{3, 3, 2, 1, 0});
//      // Chord 4: voice 0 has onset 4; voice 1 has onset 3; voice 2 has onset 2; voice 3 has onset 1; voice 4 has onset 0
//   	  List<Integer> voiceAssignment4 = Arrays.asList(new Integer[]{4, 3, 2, 1, 0});
//      // Chord 5: voice 0 has onset 2; voice 1 has onset 3; voice 2 has onset 1; voice 3 is not in use; voice 4 has onset 0
//   	  List<Integer> voiceAssignment5 = Arrays.asList(new Integer[]{2, 3, 1, -1, 0});
//      // Chord 6: voice 0 has onset 1; voice 1 is not in use; voice 2 has onset 0; voice 3 is not in use; voice 4 is not in use 
//   	  List<Integer> voiceAssignment6 = Arrays.asList(new Integer[]{1, -1, 0, -1, -1});
//      // Chord 7: voice 0 has onset 3; voice 1 has onset 2; voice 2 has onset 1; voice 3 has onset 0; voice 4 is not in use 
//   	  List<Integer> voiceAssignment7 = Arrays.asList(new Integer[]{3, 2, 1, 0, -1});
//      // Chord 8: voice 0 has onset 3; voice 1 has onset 2; voice 2 has onset 1; voice 3 has onset 0; voice 4 is not in use
//   	  List<Integer> voiceAssignment8 = Arrays.asList(new Integer[]{3, 2, 1, 0, -1});
//   	  voiceAssignments.add(voiceAssignment0); voiceAssignments.add(voiceAssignment1); voiceAssignments.add(voiceAssignment2); 
//   	  voiceAssignments.add(voiceAssignment3); voiceAssignments.add(voiceAssignment4); voiceAssignments.add(voiceAssignment5); 
//   	  voiceAssignments.add(voiceAssignment6); voiceAssignments.add(voiceAssignment7); voiceAssignments.add(voiceAssignment8); 
//      	
//   	  // For each chord: determine the expected distances and the pitch movements and add them to expected
//   	  List<List<List<Double>>> expected = new ArrayList<List<List<Double>>>();
//   	  // Chord 0
//   	  List<List<Double>> expected0 = new ArrayList<List<Double>>();
//   	  List<Double> pitchProximities0 = Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0});
//   	  List<Double> interOnsetTimeProximities0 = Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0});
//   	  List<Double> offsetOnsetTimeProximities0 = Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0});
//   	  List<Double> pitchMovement0 = Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0});
//   	  expected0.add(pitchProximities0); expected0.add(interOnsetTimeProximities0); 
//   	  expected0.add(offsetOnsetTimeProximities0); expected0.add(pitchMovement0); 	
//      // Chord 1
//   	  List<List<Double>> expected1 = new ArrayList<List<Double>>();
//   	  List<Double> pitchProximities1 = Arrays.asList(new Double[]{3.0, 4.0, 0.0, 5.0, -1.0}); 
//   	  List<Double> interOnsetTimeProximities1 = Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0});
//   	  List<Double> offsetOnsetTimeProximities1 = Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, -1.0});
//   	  List<Double> pitchMovement1 = Arrays.asList(new Double[]{3.0, 4.0, 0.0, -5.0, 0.0});
//   	  expected1.add(pitchProximities1); expected1.add(interOnsetTimeProximities1); 
//   	  expected1.add(offsetOnsetTimeProximities1); expected1.add(pitchMovement1);  	
//    	// Chord 2
//   	  List<List<Double>> expected2 = new ArrayList<List<Double>>();
//   	  List<Double> pitchProximities2 = Arrays.asList(new Double[]{-1.0, -1.0, -1.0, 3.0, -1.0});
//   	  List<Double> interOnsetTimeProximities2 = Arrays.asList(new Double[]{-1.0, -1.0, -1.0, 1/8.0, -1.0});
//   	  List<Double> offsetOnsetTimeProximities2 = Arrays.asList(new Double[]{-1.0, -1.0, -1.0, 0.0, -1.0});
//   	  List<Double> pitchMovement2 = Arrays.asList(new Double[]{0.0, 0.0, 0.0, 3.0, 0.0});
//   	  expected2.add(pitchProximities2); expected2.add(interOnsetTimeProximities2); 
//   	  expected2.add(offsetOnsetTimeProximities2); expected2.add(pitchMovement2);
//   	  // Chord 3
//   	  List<List<Double>> expected3 = new ArrayList<List<Double>>();
//   	  List<Double> pitchProximities3 = Arrays.asList(new Double[]{7.0, 4.0, 2.0, 2.0, -1.0});
//   	  List<Double> interOnsetTimeProximities3 = Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/8.0, -1.0});
//   	  List<Double> offsetOnsetTimeProximities3 = Arrays.asList(new Double[]{1/8.0, 1/8.0, 1/8.0, 0.0, -1.0});
//   	  List<Double> pitchMovement3 = Arrays.asList(new Double[]{-7.0, -4.0, 2.0, 2.0, 0.0});
//   	  expected3.add(pitchProximities3); expected3.add(interOnsetTimeProximities3); 
//   	  expected3.add(offsetOnsetTimeProximities3); expected3.add(pitchMovement3);  	
//   	  // Chord 4
//   	  List<List<Double>> expected4 = new ArrayList<List<Double>>();
//   	  List<Double> pitchProximities4 = Arrays.asList(new Double[]{4.0, 5.0, 2.0, 7.0, 2.0});
//   	  List<Double> interOnsetTimeProximities4 = Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0});
//   	  List<Double> offsetOnsetTimeProximities4 = Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0});
//   	  List<Double> pitchMovement4 = Arrays.asList(new Double[]{4.0, -5.0, -2.0, 7.0, -2.0});
//   	  expected4.add(pitchProximities4); expected4.add(interOnsetTimeProximities4); 
//   	  expected4.add(offsetOnsetTimeProximities4); expected4.add(pitchMovement4); 	
//   	  // Chord 5
//   	  List<List<Double>> expected5 = new ArrayList<List<Double>>();
//   	  List<Double> pitchProximities5 = Arrays.asList(new Double[]{5.0, 9.0, 3.0, -1.0, 0.0});
//   	  List<Double> interOnsetTimeProximities5 = Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, -1.0, 1/4.0});
//   	  List<Double> offsetOnsetTimeProximities5 = Arrays.asList(new Double[]{0.0, 0.0, 0.0, -1.0, 0.0});
//   	  List<Double> pitchMovement5 = Arrays.asList(new Double[]{-5.0, 9.0, 3.0, 0.0, 0.0});
//   	  expected5.add(pitchProximities5); expected5.add(interOnsetTimeProximities5); 
//   	  expected5.add(offsetOnsetTimeProximities5); expected5.add(pitchMovement5);
//   	  // Chord 6
//   	  List<List<Double>> expected6 = new ArrayList<List<Double>>();
//   	  List<Double> pitchProximities6 = Arrays.asList(new Double[]{4.0, -1.0, 1.0, -1.0, -1.0});
//   	  List<Double> interOnsetTimeProximities6 = Arrays.asList(new Double[]{1/8.0, -1.0, 1/8.0, -1.0, -1.0});
//   	  List<Double> offsetOnsetTimeProximities6 = Arrays.asList(new Double[]{0.0, -1.0, 0.0, -1.0, -1.0});
//   	  List<Double> pitchMovement6 = Arrays.asList(new Double[]{4.0, 0.0, -1.0, 0.0, 0.0});
//   	  expected6.add(pitchProximities6); expected6.add(interOnsetTimeProximities6); 
//   	  expected6.add(offsetOnsetTimeProximities6); expected6.add(pitchMovement6);
//   	  // Chord 7
//   	  List<List<Double>> expected7 = new ArrayList<List<Double>>();
//   	  List<Double> pitchProximities7 = Arrays.asList(new Double[]{1.0, 5.0, 2.0, 12.0, -1.0});
//   	  List<Double> interOnsetTimeProximities7 = Arrays.asList(new Double[]{1/8.0, 1/4.0, 1/8.0, 1/2.0, -1.0});
//   	  List<Double> offsetOnsetTimeProximities7 = Arrays.asList(new Double[]{0.0, 1/8.0, 0.0, 1/4.0, -1.0});
//   	  List<Double> pitchMovement7 = Arrays.asList(new Double[]{1.0, -5.0, -2.0, -12.0, 0.0});
//   	  expected7.add(pitchProximities7); expected7.add(interOnsetTimeProximities7); 
//   	  expected7.add(offsetOnsetTimeProximities7); expected7.add(pitchMovement7);  	
//   	  // Chord 8
//   	  List<List<Double>> expected8 = new ArrayList<List<Double>>();
//   	  List<Double> pitchProximities8 = Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, -1.0});
//   	  List<Double> interOnsetTimeProximities8 = Arrays.asList(new Double[]{3/4.0, 3/4.0, 3/4.0, 3/4.0, -1.0});
//   	  List<Double> offsetOnsetTimeProximities8 = Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0});
//   	  List<Double> pitchMovement8 = Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0});
//   	  expected8.add(pitchProximities8); expected8.add(interOnsetTimeProximities8); 
//   	  expected8.add(offsetOnsetTimeProximities8); expected8.add(pitchMovement8);
//   	
//   	  expected.add(expected0); expected.add(expected1); expected.add(expected2); expected.add(expected3);
//   	  expected.add(expected4); expected.add(expected5); expected.add(expected6); expected.add(expected7);
//   	  expected.add(expected8);
//   	
//   	  // For each element of expected: turn the elements of the first three Lists from distances into proximities
//   	  for (int i = 0; i < expected.size(); i++) {
//     		List<List<Double>> currentExpected = expected.get(i);
//   	  	for (int j = 0; j < currentExpected.size() - 1; j++) {
//   	  		List<Double> currentList = currentExpected.get(j);
//   	  		for (int k = 0; k < currentList.size(); k++) {
//   	  			double oldValue = currentList.get(k);
//   	  			// Do only if oldValue is not -1.0, i.e., if the voice is active
//   	  		  if (oldValue != -1.0) {
//   	  			  double newValue = 1.0/(oldValue + 1);
//   	  		    currentList.set(k, newValue);
//   	  		  }
//   	  		}
//   	  	}
//   	  }
//   	 	
//   	  // For each chord: calculate the actual proximities and the pitch movements and add them to actual      	
//   	  List<List<List<Double>>> actual = new ArrayList<List<List<Double>>>();
//   	  Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//   	  List<List<TabSymbol>> tablatureEvents = tablature.getTablatureChords();
// 		  int lowestOnsetIndex = 0;
// 		  for (int i = 0; i < tablatureEvents.size(); i++) {
// 	  		int chordSize = tablatureEvents.get(i).size();
// 	    	List<List<Double>> currentActual = featureGenerator.getProximitiesAndMovementsOfChordOLD(tablature,
// 	    		basicTabSymbolProperties, transcription, chordSize, lowestOnsetIndex, voiceAssignments.get(i));
// 	    	actual.add(currentActual);
// 	    	lowestOnsetIndex += chordSize; 	
//   		}
//   	
//     	// Assert equality
//   	  assertEquals(expected.size(), actual.size());
//   	  for (int i = 0; i < expected.size(); i++) {
//   	  	assertEquals(expected.get(i).size(), actual.get(i).size());
//     		for (int j = 0; j < expected.get(i).size(); j++) {
//     	    assertEquals(expected.get(i).get(j).size(), actual.get(i).get(j).size());
//     	    for (int k = 0; k < expected.get(i).get(j).size(); k++) {
//     	    	assertEquals(expected.get(i).get(j).get(k), actual.get(i).get(j).get(k));
//     	    }
//   	  	}
//    	} 
//     	assertEquals(expected, actual);
//    } 
//    else {
//    	System.out.println("ERROR: Preprocessing failed." + "\n");
//    	throw new RuntimeException("Preprocessing unsuccessful (see console for details)");
//    }
//	}
	
	
//	public void testEnumerateVoiceAssignmentPossibilitiesForChordOLD() {		
//		// Make the basicTabSymbolProperties for the chords. Only pitch is important here; the other properties 
//		// can be dummy values
//		Integer[] onsetProperties0 = new Integer[]{10, -1, -1, -1, -1, -1, -1, -1, -1, -1};
//		Integer[] onsetProperties1 = new Integer[]{20, -1, -1, -1, -1, -1, -1, -1, -1, -1};
//		Integer[] onsetProperties2 = new Integer[]{30, -1, -1, -1, -1, -1, -1, -1, -1, -1};
//		Integer[] onsetProperties3 = new Integer[]{40, -1, -1, -1, -1, -1, -1, -1, -1, -1};
//		
//		// For each chord: determine the expected voice assignments and add them to expected
//		List<List<List<Integer>>> expected = new ArrayList<List<List<Integer>>>();
//		// 1. For a chord of two onsets where the maximum voice number is three (which gives 12 musically possible voice
//		// assignments) 
//		int maxVoiceNumber0 = 3;
//		Integer[][] basicTabSymbolPropertiesChord0 = new Integer[2][10];
//		basicTabSymbolPropertiesChord0[0] = onsetProperties0; basicTabSymbolPropertiesChord0[1] = onsetProperties1;
//		
//		// Determine the expected voice assignments and add them to expected0
//		List<List<Integer>> expected0 = new ArrayList<List<Integer>>();
//		List<Integer> exp00 = Arrays.asList(new Integer[]{-1, 0, 1, -1,-1});
//		List<Integer> exp01 = Arrays.asList(new Integer[]{-1, 1, 0, -1,-1});
//		List<Integer> exp02 = Arrays.asList(new Integer[]{0, -1, 1, -1,-1});
//		List<Integer> exp03 = Arrays.asList(new Integer[]{0, 0, 1, -1,-1});
//		List<Integer> exp04 = Arrays.asList(new Integer[]{0, 1, -1, -1,-1});
//		List<Integer> exp05 = Arrays.asList(new Integer[]{0, 1, 0, -1,-1});
//		List<Integer> exp06 = Arrays.asList(new Integer[]{0, 1, 1, -1,-1});
//		List<Integer> exp07 = Arrays.asList(new Integer[]{1, -1, 0, -1,-1});
//		List<Integer> exp08 = Arrays.asList(new Integer[]{1, 0, -1, -1,-1});
//		List<Integer> exp09 = Arrays.asList(new Integer[]{1, 0, 0, -1,-1});
//		List<Integer> exp010 = Arrays.asList(new Integer[]{1, 0, 1, -1,-1});
//		List<Integer> exp011 = Arrays.asList(new Integer[]{1, 1, 0, -1,-1});
//		
//		expected0.add(exp00); expected0.add(exp01); expected0.add(exp02); expected0.add(exp03);	expected0.add(exp04); 
//		expected0.add(exp05); expected0.add(exp06); expected0.add(exp07); expected0.add(exp08); expected0.add(exp09); 
//		expected0.add(exp010); expected0.add(exp011);
//		
//	  // 2. For a chord of three onsets where the maximum voice number is three (which gives 6  musically possible 
//		// voice assignments) 
//		int maxVoiceNumber1 = 3;
//		Integer[][] basicTabSymbolPropertiesChord1 = new Integer[3][10];
//		basicTabSymbolPropertiesChord1[0] = onsetProperties0; basicTabSymbolPropertiesChord1[1] = onsetProperties1;
//		basicTabSymbolPropertiesChord1[2] = onsetProperties2;
//		
//	  // Determine the expected voice assignments and add them to expected1
//		List<List<Integer>> expected1 = new ArrayList<List<Integer>>();
////		List<Integer> exp10 = Arrays.asList(new Integer[]{0, 1, 2, -1,-1}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp11 = Arrays.asList(new Integer[]{0, 2, 1, -1,-1});
//		List<Integer> exp12 = Arrays.asList(new Integer[]{1, 0, 2, -1,-1});
//		List<Integer> exp13 = Arrays.asList(new Integer[]{1, 2, 0, -1,-1});
//		List<Integer> exp14 = Arrays.asList(new Integer[]{2,  0, 1, -1,-1});
//		List<Integer> exp15 = Arrays.asList(new Integer[]{2, 1, 0, -1,-1});
//		
//		/*expected1.add(exp10);*/ expected1.add(exp11); expected1.add(exp12); expected1.add(exp13);	expected1.add(exp14); 
//		expected1.add(exp15);
//		
//	  // 3. For a chord of two onsets where the maximum voice number is five (which gives 110 musically possible voice
//		// assignments) 
//		int maxVoiceNumber2 = 5;
//		Integer[][] basicTabSymbolPropertiesChord2 = new Integer[2][10];
//		basicTabSymbolPropertiesChord2[0] = onsetProperties0; basicTabSymbolPropertiesChord2[1] = onsetProperties1;
//			
//	  // Determine the expected voice assignments and add them to expected2
//		List<List<Integer>> expected2 = new ArrayList<List<Integer>>();
//		List<Integer> exp20 = Arrays.asList(new Integer[]{-1, -1, -1, 0, 1});
//		List<Integer> exp21 = Arrays.asList(new Integer[]{-1, -1, -1, 1, 0});
//		List<Integer> exp22 = Arrays.asList(new Integer[]{-1, -1, 0, -1, 1});
//		List<Integer> exp23 = Arrays.asList(new Integer[]{-1, -1, 0, 0, 1});
//		List<Integer> exp24 = Arrays.asList(new Integer[]{-1, -1, 0, 1, -1});
//		List<Integer> exp25 = Arrays.asList(new Integer[]{-1, -1, 0, 1, 0});
//		List<Integer> exp26 = Arrays.asList(new Integer[]{-1, -1, 0, 1, 1});
//		List<Integer> exp27 = Arrays.asList(new Integer[]{-1, -1, 1, -1, 0});
//		List<Integer> exp28 = Arrays.asList(new Integer[]{-1, -1, 1, 0, -1});
//		List<Integer> exp29 = Arrays.asList(new Integer[]{-1, -1, 1, 0, 0});
//		List<Integer> exp210 = Arrays.asList(new Integer[]{-1, -1, 1, 0, 1});
//		List<Integer> exp211 = Arrays.asList(new Integer[]{-1, -1, 1, 1, 0});
//		List<Integer> exp212 = Arrays.asList(new Integer[]{-1, 0, -1, -1, 1});
//	  List<Integer> exp213 = Arrays.asList(new Integer[]{-1, 0, -1, 0, 1});		
//		List<Integer> exp214 = Arrays.asList(new Integer[]{-1, 0, -1, 1, -1});
//		List<Integer> exp215 = Arrays.asList(new Integer[]{-1, 0, -1, 1, 0});
//		List<Integer> exp216 = Arrays.asList(new Integer[]{-1, 0, -1, 1, 1});
//		List<Integer> exp217 = Arrays.asList(new Integer[]{-1, 0, 0, -1, 1});
//		List<Integer> exp218 = Arrays.asList(new Integer[]{-1, 0, 0, 1, -1});
////		List<Integer> exp219 = Arrays.asList(new Integer[]{-1, 0, 0, 1, 1}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp220 = Arrays.asList(new Integer[]{-1, 0, 1, -1, -1});
//		List<Integer> exp221 = Arrays.asList(new Integer[]{-1, 0, 1, -1, 0});
//		List<Integer> exp222 = Arrays.asList(new Integer[]{-1, 0, 1, -1, 1});		
//		List<Integer> exp223 = Arrays.asList(new Integer[]{-1, 0, 1, 0, -1});
////		List<Integer> exp224 = Arrays.asList(new Integer[]{-1, 0, 1, 0, 1}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp225 = Arrays.asList(new Integer[]{-1, 0, 1, 1, -1});
//		List<Integer> exp226 = Arrays.asList(new Integer[]{-1, 0, 1, 1, 0});
//		List<Integer> exp227 = Arrays.asList(new Integer[]{-1, 1, -1, -1, 0});
//		List<Integer> exp228 = Arrays.asList(new Integer[]{-1, 1, -1, 0, -1});
//		List<Integer> exp229 = Arrays.asList(new Integer[]{-1, 1, -1, 0, 0});
//		List<Integer> exp230 = Arrays.asList(new Integer[]{-1, 1, -1, 0, 1});
//		List<Integer> exp231 = Arrays.asList(new Integer[]{-1, 1, -1, 1, 0});		
//		List<Integer> exp232 = Arrays.asList(new Integer[]{-1, 1, 0, -1, -1});
//		List<Integer> exp233 = Arrays.asList(new Integer[]{-1, 1, 0, -1, 0});
//		List<Integer> exp234 = Arrays.asList(new Integer[]{-1, 1, 0, -1, 1});
//		List<Integer> exp235 = Arrays.asList(new Integer[]{-1, 1, 0, 0, -1});
//		List<Integer> exp236 = Arrays.asList(new Integer[]{-1, 1, 0, 0, 1});
//		List<Integer> exp237 = Arrays.asList(new Integer[]{-1, 1, 0, 1, -1});
//		List<Integer> exp238 = Arrays.asList(new Integer[]{-1, 1, 0, 1, 0});
//		List<Integer> exp239 = Arrays.asList(new Integer[]{-1, 1, 1, -1, 0});
//		List<Integer> exp240 = Arrays.asList(new Integer[]{-1, 1, 1, 0, -1});
//		List<Integer> exp241 = Arrays.asList(new Integer[]{-1, 1, 1, 0, 0});		
//		
//		List<Integer> exp242 = Arrays.asList(new Integer[]{0, -1, -1, -1, 1});
//		List<Integer> exp243 = Arrays.asList(new Integer[]{0, -1, -1, 0, 1});
//		List<Integer> exp244 = Arrays.asList(new Integer[]{0, -1, -1, 1, -1});
//		List<Integer> exp245 = Arrays.asList(new Integer[]{0, -1, -1, 1, 0});
//		List<Integer> exp246 = Arrays.asList(new Integer[]{0, -1, -1, 1, 1});
//		List<Integer> exp247 = Arrays.asList(new Integer[]{0, -1, 0, -1, 1});
//		List<Integer> exp248 = Arrays.asList(new Integer[]{0, -1, 0, 1, -1});
////		List<Integer> exp249 = Arrays.asList(new Integer[]{0, -1, 0, 1, 1}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp250 = Arrays.asList(new Integer[]{0, -1, 1, -1, -1});
//		List<Integer> exp251 = Arrays.asList(new Integer[]{0, -1, 1, -1, 0});		
//		List<Integer> exp252 = Arrays.asList(new Integer[]{0, -1, 1, -1, 1});
//		List<Integer> exp253 = Arrays.asList(new Integer[]{0, -1, 1, 0, -1});
////		List<Integer> exp254 = Arrays.asList(new Integer[]{0, -1, 1, 0, 1}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp255 = Arrays.asList(new Integer[]{0, -1, 1, 1, -1});
//		List<Integer> exp256 = Arrays.asList(new Integer[]{0, -1, 1, 1, 0});
//		List<Integer> exp257 = Arrays.asList(new Integer[]{0, 0, -1, -1, 1});
//		List<Integer> exp258 = Arrays.asList(new Integer[]{0, 0, -1, 1, -1});
////		List<Integer> exp259 = Arrays.asList(new Integer[]{0, 0, -1, 1, 1}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp260 = Arrays.asList(new Integer[]{0, 0, 1, -1, -1});
////		List<Integer> exp261 = Arrays.asList(new Integer[]{0, 0, 1, -1, 1}); // --> has >2 voice crossing pairs; do not add		
////		List<Integer> exp262 = Arrays.asList(new Integer[]{0, 0, 1, 1, -1}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp263 = Arrays.asList(new Integer[]{0, 1, -1, -1, -1});
//		List<Integer> exp264 = Arrays.asList(new Integer[]{0, 1, -1, -1, 0});
//		List<Integer> exp265 = Arrays.asList(new Integer[]{0, 1, -1, -1, 1});
//		List<Integer> exp266 = Arrays.asList(new Integer[]{0, 1, -1, 0, -1});
////		List<Integer> exp267 = Arrays.asList(new Integer[]{0, 1, -1, 0, 1}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp268 = Arrays.asList(new Integer[]{0, 1, -1, 1, -1});
//		List<Integer> exp269 = Arrays.asList(new Integer[]{0, 1, -1, 1, 0});
//		List<Integer> exp270 = Arrays.asList(new Integer[]{0, 1, 0, -1, -1});
////		List<Integer> exp271 = Arrays.asList(new Integer[]{0, 1, 0, -1, 1}); // --> has >2 voice crossing pairs; do not add
////		List<Integer> exp272 = Arrays.asList(new Integer[]{0, 1, 0, 1, -1}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp273 = Arrays.asList(new Integer[]{0, 1, 1, -1, -1});
//		List<Integer> exp274 = Arrays.asList(new Integer[]{0, 1, 1, -1, 0});
//		List<Integer> exp275 = Arrays.asList(new Integer[]{0, 1, 1, 0, -1});
//		
//		List<Integer> exp276 = Arrays.asList(new Integer[]{1, -1, -1, -1, 0});
//		List<Integer> exp277 = Arrays.asList(new Integer[]{1, -1, -1, 0, -1});
//		List<Integer> exp278 = Arrays.asList(new Integer[]{1, -1, -1, 0, 0});
//		List<Integer> exp279 = Arrays.asList(new Integer[]{1, -1, -1, 0, 1});
//		List<Integer> exp280 = Arrays.asList(new Integer[]{1, -1, -1, 1, 0});
//		List<Integer> exp281 = Arrays.asList(new Integer[]{1, -1, 0, -1, -1});		
//		List<Integer> exp282 = Arrays.asList(new Integer[]{1, -1, 0, -1, 0});
//		List<Integer> exp283 = Arrays.asList(new Integer[]{1, -1, 0, -1, 1});
//		List<Integer> exp284 = Arrays.asList(new Integer[]{1, -1, 0, 0, -1});
//		List<Integer> exp285 = Arrays.asList(new Integer[]{1, -1, 0, 0, 1});
//		List<Integer> exp286 = Arrays.asList(new Integer[]{1, -1, 0, 1, -1});
//		List<Integer> exp287 = Arrays.asList(new Integer[]{1, -1, 0, 1, 0});
//		List<Integer> exp288 = Arrays.asList(new Integer[]{1, -1, 1, -1, 0});
//		List<Integer> exp289 = Arrays.asList(new Integer[]{1, -1, 1, 0, -1});
//		List<Integer> exp290 = Arrays.asList(new Integer[]{1, -1, 1, 0, 0});
//		List<Integer> exp291 = Arrays.asList(new Integer[]{1, 0, -1, -1, -1});		
//		List<Integer> exp292 = Arrays.asList(new Integer[]{1, 0, -1, -1, 0});
//		List<Integer> exp293 = Arrays.asList(new Integer[]{1, 0, -1, -1, 1});
//		List<Integer> exp294 = Arrays.asList(new Integer[]{1, 0, -1, 0, -1});
//		List<Integer> exp295 = Arrays.asList(new Integer[]{1, 0, -1, 0, 1});
//		List<Integer> exp296 = Arrays.asList(new Integer[]{1, 0, -1, 1, -1});
//		List<Integer> exp297 = Arrays.asList(new Integer[]{1, 0, -1, 1, 0});
//		List<Integer> exp298 = Arrays.asList(new Integer[]{1, 0, 0, -1, -1});
//		List<Integer> exp299 = Arrays.asList(new Integer[]{1, 0, 0, -1, 1});
//		List<Integer> exp2100 = Arrays.asList(new Integer[]{1, 0, 0, 1, -1});
//		List<Integer> exp2101 = Arrays.asList(new Integer[]{1, 0, 1, -1, -1});		
//		List<Integer> exp2102 = Arrays.asList(new Integer[]{1, 0, 1, -1, 0});
//		List<Integer> exp2103 = Arrays.asList(new Integer[]{1, 0, 1, 0, -1});
//		List<Integer> exp2104 = Arrays.asList(new Integer[]{1, 1, -1, -1, 0});
//		List<Integer> exp2105 = Arrays.asList(new Integer[]{1, 1, -1, 0, -1});
//		List<Integer> exp2106 = Arrays.asList(new Integer[]{1, 1, -1, 0, 0});
//		List<Integer> exp2107 = Arrays.asList(new Integer[]{1, 1, 0, -1, -1});
//		List<Integer> exp2108 = Arrays.asList(new Integer[]{1, 1, 0, -1, 0});
//		List<Integer> exp2109 = Arrays.asList(new Integer[]{1, 1, 0, 0, -1});
//				
//		expected2.add(exp20); expected2.add(exp21); expected2.add(exp22); expected2.add(exp23);	expected2.add(exp24); 
//		expected2.add(exp25); expected2.add(exp26); expected2.add(exp27); expected2.add(exp28); expected2.add(exp29);
//		expected2.add(exp210); expected2.add(exp211); expected2.add(exp212); expected2.add(exp213);	expected2.add(exp214); 
//		expected2.add(exp215); expected2.add(exp216); expected2.add(exp217); expected2.add(exp218); /*expected2.add(exp219);*/
//		expected2.add(exp220); expected2.add(exp221); expected2.add(exp222); expected2.add(exp223); /*expected2.add(exp224);*/ 
//		expected2.add(exp225); expected2.add(exp226); expected2.add(exp227); expected2.add(exp228); expected2.add(exp229);
//		expected2.add(exp230); expected2.add(exp231); expected2.add(exp232); expected2.add(exp233);	expected2.add(exp234); 
//		expected2.add(exp235); expected2.add(exp236); expected2.add(exp237); expected2.add(exp238); expected2.add(exp239);
//		expected2.add(exp240); expected2.add(exp241); expected2.add(exp242); expected2.add(exp243);	expected2.add(exp244); 
//		expected2.add(exp245); expected2.add(exp246); expected2.add(exp247); expected2.add(exp248); /*expected2.add(exp249);*/
//		expected2.add(exp250); expected2.add(exp251); expected2.add(exp252); expected2.add(exp253); /*expected2.add(exp254);*/
//		expected2.add(exp255); expected2.add(exp256); expected2.add(exp257); expected2.add(exp258); /*expected2.add(exp259);*/
//		expected2.add(exp260); /*expected2.add(exp261); expected2.add(exp262);*/ expected2.add(exp263);	expected2.add(exp264); 
//		expected2.add(exp265); expected2.add(exp266); /*expected2.add(exp267);*/ expected2.add(exp268); expected2.add(exp269);
//		expected2.add(exp270); /*expected2.add(exp271); expected2.add(exp272);*/ expected2.add(exp273);	expected2.add(exp274); 
//		expected2.add(exp275); expected2.add(exp276); expected2.add(exp277); expected2.add(exp278); expected2.add(exp279);
//		expected2.add(exp280); expected2.add(exp281); expected2.add(exp282); expected2.add(exp283);	expected2.add(exp284); 
//		expected2.add(exp285); expected2.add(exp286); expected2.add(exp287); expected2.add(exp288); expected2.add(exp289);
//		expected2.add(exp290); expected2.add(exp291); expected2.add(exp292); expected2.add(exp293);	expected2.add(exp294); 
//		expected2.add(exp295); expected2.add(exp296); expected2.add(exp297); expected2.add(exp298); expected2.add(exp299);
//		expected2.add(exp2100); expected2.add(exp2101); expected2.add(exp2102); expected2.add(exp2103);	expected2.add(exp2104); 
//		expected2.add(exp2105); expected2.add(exp2106); expected2.add(exp2107); expected2.add(exp2108); expected2.add(exp2109);
//		
//	  // 4. For a chord of four onsets where the maximum voice number is four (which gives 24 musically possible voice 
//		// assignments) 
//		int maxVoiceNumber3 = 4;
//		Integer[][] basicTabSymbolPropertiesChord3 = new Integer[4][10];
//		basicTabSymbolPropertiesChord3[0] = onsetProperties0; basicTabSymbolPropertiesChord3[1] = onsetProperties1;
//		basicTabSymbolPropertiesChord3[2] = onsetProperties2; basicTabSymbolPropertiesChord3[3] = onsetProperties3;		
//		
//	  // Determine the expected voice assignments and add them to expected3
//		List<List<Integer>> expected3 = new ArrayList<List<Integer>>();
////		List<Integer> exp30 = Arrays.asList(new Integer[]{0, 1, 2, 3, -1}); // --> has >2 voice crossing pairs; do not add
////		List<Integer> exp31 = Arrays.asList(new Integer[]{0, 1, 3, 2, -1}); // --> has >2 voice crossing pairs; do not add
////		List<Integer> exp32 = Arrays.asList(new Integer[]{0, 2, 1, 3, -1}); // --> has >2 voice crossing pairs; do not add
////		List<Integer> exp33 = Arrays.asList(new Integer[]{0, 2, 3, 1, -1}); // --> has >2 voice crossing pairs; do not add
////		List<Integer> exp34 = Arrays.asList(new Integer[]{0, 3, 1, 2, -1}); // --> has >2 voice crossing pairs; do not add
////		List<Integer> exp35 = Arrays.asList(new Integer[]{0, 3, 2, 1, -1}); // --> has >2 voice crossing pairs; do not add
////		List<Integer> exp36 = Arrays.asList(new Integer[]{1, 0, 2, 3, -1}); // --> has >2 voice crossing pairs; do not add
////		List<Integer> exp37 = Arrays.asList(new Integer[]{1, 0, 3, 2, -1}); // --> has >2 voice crossing pairs; do not add
////		List<Integer> exp38 = Arrays.asList(new Integer[]{1, 2, 0, 3, -1}); // --> has >2 voice crossing pairs; do not add
////		List<Integer> exp39 = Arrays.asList(new Integer[]{1, 2, 3, 0, -1}); // --> has >2 voice crossing pairs; do not add
////		List<Integer> exp310 = Arrays.asList(new Integer[]{1, 3, 0, 2, -1}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp311 = Arrays.asList(new Integer[]{1, 3, 2, 0, -1});
////		List<Integer> exp312 = Arrays.asList(new Integer[]{2, 0, 1, 3, -1}); // --> has >2 voice crossing pairs; do not add
////		List<Integer> exp313 = Arrays.asList(new Integer[]{2, 0, 3, 1, -1}); // --> has >2 voice crossing pairs; do not add
////		List<Integer> exp314 = Arrays.asList(new Integer[]{2, 1, 0, 3, -1}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp315 = Arrays.asList(new Integer[]{2, 1, 3, 0, -1});
//		List<Integer> exp316 = Arrays.asList(new Integer[]{2, 3, 0, 1, -1});
//		List<Integer> exp317 = Arrays.asList(new Integer[]{2, 3, 1, 0, -1});
////		List<Integer> exp318 = Arrays.asList(new Integer[]{3, 0, 1, 2, -1}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp319 = Arrays.asList(new Integer[]{3, 0, 2, 1, -1}); 
//		List<Integer> exp320 = Arrays.asList(new Integer[]{3, 1, 0, 2, -1});
//		List<Integer> exp321 = Arrays.asList(new Integer[]{3, 1, 2, 0, -1});
//		List<Integer> exp322 = Arrays.asList(new Integer[]{3, 2, 0, 1, -1});		
//		List<Integer> exp323 = Arrays.asList(new Integer[]{3, 2, 1, 0, -1});
//		
//		/*expected3.add(exp30); expected3.add(exp31); expected3.add(exp32); expected3.add(exp33); expected3.add(exp34);
//		expected3.add(exp35); expected3.add(exp36); expected3.add(exp37); expected3.add(exp38); expected3.add(exp39);
//		expected3.add(exp310);*/ expected3.add(exp311); /*expected3.add(exp312); expected3.add(exp313); expected3.add(exp314);*/
//		expected3.add(exp315); expected3.add(exp316); expected3.add(exp317); /*expected3.add(exp318);*/ expected3.add(exp319);
//		expected3.add(exp320); expected3.add(exp321); expected3.add(exp322); expected3.add(exp323);
//
//	  // Add expected0-3 to expected
//	  expected.add(expected0); expected.add(expected1); expected.add(expected2); expected.add(expected3); 
//	    
//		// For each chord: calculate the actual voice assignments and add them to actual
//	  List<List<List<Integer>>> actual = new ArrayList <List<List<Integer>>>();
//		List<List<Integer>> actual0 = 
//			featureGenerator.enumerateVoiceAssignmentPossibilitiesForChordOLD(maxVoiceNumber0, basicTabSymbolPropertiesChord0); // chordSize0);
//		List<List<Integer>> actual1 = 
//			featureGenerator.enumerateVoiceAssignmentPossibilitiesForChordOLD(maxVoiceNumber1, basicTabSymbolPropertiesChord1); // chordSize1);
//		List<List<Integer>> actual2 = 
//			featureGenerator.enumerateVoiceAssignmentPossibilitiesForChordOLD(maxVoiceNumber2, basicTabSymbolPropertiesChord2); // chordSize2);
//		List<List<Integer>> actual3 = 
//			featureGenerator.enumerateVoiceAssignmentPossibilitiesForChordOLD(maxVoiceNumber3, basicTabSymbolPropertiesChord3); // chordSize2);
//		actual.add(actual0); actual.add(actual1); actual.add(actual2); actual.add(actual3);
//		
//		// Assert equality
//		assertEquals(expected.size(), actual.size());
//		for (int i = 0; i < expected.size(); i++) {
//		  assertEquals(expected.get(i).size(), actual.get(i).size());
//		  for (int j = 0; j < expected.get(i).size(); j++) {
//		  	assertEquals(expected.get(i).get(j).size(), actual.get(i).get(j).size());
//		  	for (int k = 0; k < expected.get(i).get(j).size(); k++) {
//		  		assertEquals(expected.get(i).get(j).get(k), actual.get(i).get(j).get(k));
//		  	}
//		  }
//		}
//    assertEquals(expected, actual);
//	}
	
	
//	public void testGenerateConstantChordFeatureVectorOLD() {
//	  // Make a Tablature and Transcription and preprocess them
//    File encodingFile = new File(TrainingManager.prefix + "Encodings/Tests/Testpiece 1.txt");
//    File midiFile = new File(TrainingManager.prefix + "MIDI/Tests/Testpiece 1");
//    Tablature tablature = new Tablature(encodingFile);
//    Transcription transcription = midiImport.importMidiFiles(midiFile);
//    boolean preprocessingSuccessful = preprocessor.preprocess(tablature, transcription, true, null);
//    
//    // Continue only if preprocessing is successful
//    if (preprocessingSuccessful == true) {
//    	// For each chord: determine the expected constant chord feature vectors and add them to expected
//    	List<List<Double>> expected = new ArrayList<List<Double>>();
//    	List<Double> missingOnsetFeatures = Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0}); 		
//    	// Chord 0
//    	List<Double> constantChordFeatureVector0 = new ArrayList<Double>();
//      // a. Add the features that are different for each onset in the chord
//    	List<Double> individualOnsetFeatures0 = new ArrayList<Double>();
//    	List<Double> featuresOnset00 = Arrays.asList(new Double[]{48.0, 5.0, 0.0, 1/2.0, 0.0, 3.0, -1.0, 7.0});
//    	List<Double> featuresOnset01 = Arrays.asList(new Double[]{55.0, 4.0, 2.0, 1/4.0, 1.0, 2.0, 7.0, 8.0});
//    	List<Double> featuresOnset02 = Arrays.asList(new Double[]{63.0, 2.0, 1.0, 1/4.0, 2.0, 1.0, 8.0, 4.0});
//    	List<Double> featuresOnset03 = Arrays.asList(new Double[]{67.0, 1.0, 0.0, 1/4.0, 3.0, 0.0, 4.0, -1.0}); 
//    	individualOnsetFeatures0.addAll(featuresOnset00); individualOnsetFeatures0.addAll(featuresOnset01);
//    	individualOnsetFeatures0.addAll(featuresOnset02); individualOnsetFeatures0.addAll(featuresOnset03);
//    	individualOnsetFeatures0.addAll(missingOnsetFeatures);
//      // b. Add the features that are the same for (every onset in) the complete chord
//    	Integer[][] basicTabSymbolPropertiesChord0 = tablature.getBasicTabSymbolPropertiesChord(0);
//    	double skewnessChord0 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord0);
//    	List<Double> sharedOnsetFeatures0 = 
//    		Arrays.asList(new Double[]{1/4.0, 4.0, 0.0, 19.0, skewnessChord0, 7.0, 8.0, 4.0, -1.0});
//   		// c. Construct the chord's constant chord feature vector 
//    	constantChordFeatureVector0.addAll(individualOnsetFeatures0);
//    	constantChordFeatureVector0.addAll(sharedOnsetFeatures0);
//   		
//   		// Chord 1
//   		List<Double> constantChordFeatureVector1 = new ArrayList<Double>();
//      // a. Add the features that are different for each onset in the chord
//   		List<Double> individualOnsetFeatures1 = new ArrayList<Double>();
//   		List<Double> featuresOnset10 = Arrays.asList(new Double[]{43.0, 6.0, 0.0, 1/8.0, 0.0, 3.0, -1.0, 12.0});
//   		List<Double> featuresOnset11 = Arrays.asList(new Double[]{55.0, 4.0, 2.0, 1/4.0, 1.0, 2.0, 12.0, 15.0});
//   		List<Double> featuresOnset12 = Arrays.asList(new Double[]{70.0, 2.0, 8.0, 1/4.0, 2.0, 1.0, 15.0, 3.0});
//   		List<Double> featuresOnset13 = Arrays.asList(new Double[]{67.0, 1.0, 0.0, 1/2.0, 3.0, 0.0, 3.0, -1.0}); 
//   		individualOnsetFeatures1.addAll(featuresOnset10); individualOnsetFeatures1.addAll(featuresOnset11);
//   		individualOnsetFeatures1.addAll(featuresOnset12);	individualOnsetFeatures1.addAll(featuresOnset13);
//   		individualOnsetFeatures1.addAll(missingOnsetFeatures);
//      // b. Add the features that are the same for (every onset in) the complete chord
//   		Integer[][] basicTabSymbolPropertiesChord1 = tablature.getBasicTabSymbolPropertiesChord(1);
//   		double skewnessChord1 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord1);
//   		List<Double> sharedOnsetFeatures1 = 
//   			Arrays.asList(new Double[]{1/8.0, 4.0, 0.0, 27.0, skewnessChord1, 12.0, 15.0, 3.0, -1.0});
//      // c. Construct the chord's constant chord feature vector
//   		constantChordFeatureVector1.addAll(individualOnsetFeatures1);
//   		constantChordFeatureVector1.addAll(sharedOnsetFeatures1);
//
//   		// Chord 2
//    	List<Double> constantChordFeatureVector2 = new ArrayList<Double>();
//      // a. Add the features that are different for each onset in the chord
//    	List<Double> individualOnsetFeatures2 = new ArrayList<Double>();
//    	List<Double> featuresOnset20 = Arrays.asList(new Double[]{46.0, 6.0, 3.0, 1/8.0, 0.0, 0.0, -1.0, -1.0});
//    	individualOnsetFeatures2.addAll(featuresOnset20); individualOnsetFeatures2.addAll(missingOnsetFeatures);
//    	individualOnsetFeatures2.addAll(missingOnsetFeatures); individualOnsetFeatures2.addAll(missingOnsetFeatures);
//    	individualOnsetFeatures2.addAll(missingOnsetFeatures);
//      // b. Add the features that are the same for (every onset in) the complete chord
//    	Integer[][] basicTabSymbolPropertiesChord2 = tablature.getBasicTabSymbolPropertiesChord(2);
//    	double skewnessChord2 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord2);
//    	List<Double> sharedOnsetFeatures2 = 
//    		Arrays.asList(new Double[]{1/8.0, 1.0, 0.0, 0.0, skewnessChord2, -1.0, -1.0, -1.0, -1.0});
//      // c. Construct the chord's constant chord feature vector
//    	constantChordFeatureVector2.addAll(individualOnsetFeatures2);
//    	constantChordFeatureVector2.addAll(sharedOnsetFeatures2);
//
//    	// Chord 3
//    	List<Double> constantChordFeatureVector3 = new ArrayList<Double>();
//      // a. Add the features that are different for each onset in the chord
//    	List<Double> individualOnsetFeatures3 = new ArrayList<Double>();
//    	List<Double> featuresOnset30 = Arrays.asList(new Double[]{45.0, 6.0, 2.0, 1/4.0, 0.0, 3.0, -1.0, 3.0});
//    	List<Double> featuresOnset31 = Arrays.asList(new Double[]{48.0, 5.0, 0.0, 1/4.0, 1.0, 2.0, 3.0, 9.0});
//    	List<Double> featuresOnset32 = Arrays.asList(new Double[]{57.0, 4.0, 4.0, 1/4.0, 2.0, 1.0, 9.0, 6.0});
//    	List<Double> featuresOnset33 = Arrays.asList(new Double[]{63.0, 2.0, 1.0, 1/2.0, 3.0, 0.0, 6.0, -1.0});
//    	individualOnsetFeatures3.addAll(featuresOnset30); individualOnsetFeatures3.addAll(featuresOnset31);
//    	individualOnsetFeatures3.addAll(featuresOnset32);	individualOnsetFeatures3.addAll(featuresOnset33);
//    	individualOnsetFeatures3.addAll(missingOnsetFeatures);
//      // b. Add the features that are the same for (every onset in) the complete chord
//    	Integer[][] basicTabSymbolPropertiesChord3 = tablature.getBasicTabSymbolPropertiesChord(3);
//    	double skewnessChord3 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord3);
//    	List<Double> sharedOnsetFeatures3 = 
//    		Arrays.asList(new Double[]{1/4.0, 4.0, 0.0, 18.0, skewnessChord3, 3.0, 9.0, 6.0, -1.0});
//      // c. Construct the chord's constant chord feature vector
//    	constantChordFeatureVector3.addAll(individualOnsetFeatures3);
//    	constantChordFeatureVector3.addAll(sharedOnsetFeatures3);
//
//    	// Chord 4
//    	List<Double> constantChordFeatureVector4 = new ArrayList<Double>();
//      // a. Add the features that are different for each onset in the chord
//    	List<Double> individualOnsetFeatures4 = new ArrayList<Double>();
//    	List<Double> featuresOnset40 = Arrays.asList(new Double[]{43.0, 6.0, 0.0, 1/4.0, 0.0, 4.0, -1.0, 12.0});
//    	List<Double> featuresOnset41 = Arrays.asList(new Double[]{55.0, 5.0, 7.0, 3/2.0, 1.0, 3.0, 12.0, 0.0});
//    	List<Double> featuresOnset42 = Arrays.asList(new Double[]{55.0, 4.0, 2.0, 1/2.0, 2.0, 2.0, 0.0, 3.0});
//    	List<Double> featuresOnset43 = Arrays.asList(new Double[]{58.0, 3.0, 1.0, 1/4.0, 3.0, 1.0, 3.0, 9.0});
//    	List<Double> featuresOnset44 = Arrays.asList(new Double[]{67.0, 1.0, 0.0, 1/4.0, 4.0, 0.0, 9.0, -1.0});
//    	individualOnsetFeatures4.addAll(featuresOnset40); individualOnsetFeatures4.addAll(featuresOnset41);
//    	individualOnsetFeatures4.addAll(featuresOnset42);	individualOnsetFeatures4.addAll(featuresOnset43);
//    	individualOnsetFeatures4.addAll(featuresOnset44);
//    	// b. Add the features that are the same for (every onset in) the complete chord
//    	Integer[][] basicTabSymbolPropertiesChord4 = tablature.getBasicTabSymbolPropertiesChord(4);
//    	double skewnessChord4 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord4);
//    	List<Double> sharedOnsetFeatures4 = 
//    		Arrays.asList(new Double[]{1/4.0, 5.0, 0.0, 24.0, skewnessChord4, 12.0, 0.0, 3.0, 9.0});
//      // c. Construct the chord's constant chord feature vector
//    	constantChordFeatureVector4.addAll(individualOnsetFeatures4);
//    	constantChordFeatureVector4.addAll(sharedOnsetFeatures4);
//
//    	// Chord 5
//   		List<Double> constantChordFeatureVector5 = new ArrayList<Double>();
//      // a. Add the features that are different for each onset in the chord
//   		List<Double> individualOnsetFeatures5 = new ArrayList<Double>();
//   		List<Double> featuresOnset50 = Arrays.asList(new Double[]{43.0, 6.0, 0.0, 1/4.0, 0.0, 3.0, -1.0, 15.0});
//   		List<Double> featuresOnset51 = Arrays.asList(new Double[]{58.0, 3.0, 1.0, 1/8.0, 1.0, 2.0, 15.0, 4.0});
//   		List<Double> featuresOnset52 = Arrays.asList(new Double[]{62.0, 2.0, 0.0, 1/8.0, 2.0, 1.0, 4.0, 5.0});
//   		List<Double> featuresOnset53 = Arrays.asList(new Double[]{67.0, 1.0, 0.0, 1/4.0, 3.0, 0.0, 5.0, -1.0});
//   		individualOnsetFeatures5.addAll(featuresOnset50); individualOnsetFeatures5.addAll(featuresOnset51);
//   		individualOnsetFeatures5.addAll(featuresOnset52); individualOnsetFeatures5.addAll(featuresOnset53);
//   		individualOnsetFeatures5.addAll(missingOnsetFeatures);
//      // b. Add the features that are the same for (every onset in) the complete chord
//   		Integer[][] basicTabSymbolPropertiesChord5 = tablature.getBasicTabSymbolPropertiesChord(5);
//   		double skewnessChord5 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord5);
//   		List<Double> sharedOnsetFeatures5 = 
//   			Arrays.asList(new Double[]{1/8.0, 4.0, 0.0, 24.0, skewnessChord5, 15.0, 4.0, 5.0, -1.0});
//      // c. Construct the chord's constant chord feature vector
//   		constantChordFeatureVector5.addAll(individualOnsetFeatures5);
//   		constantChordFeatureVector5.addAll(sharedOnsetFeatures5);
//
//   		// Chord 6
//    	List<Double> constantChordFeatureVector6 = new ArrayList<Double>();
//      // a. Add the features that are different for each onset in the chord
//    	List<Double> individualOnsetFeatures6 = new ArrayList<Double>();
//    	List<Double> featuresOnset60 = Arrays.asList(new Double[]{57.0, 3.0, 0.0, 9/8.0, 0.0, 1.0, -1.0, 9.0});
//    	List<Double> featuresOnset61 = Arrays.asList(new Double[]{66.0, 2.0, 4.0, 1/8.0, 1.0, 0.0, 9.0, -1.0});
//    	individualOnsetFeatures6.addAll(featuresOnset60); individualOnsetFeatures6.addAll(featuresOnset61);
//    	individualOnsetFeatures6.addAll(missingOnsetFeatures); individualOnsetFeatures6.addAll(missingOnsetFeatures);
//    	individualOnsetFeatures6.addAll(missingOnsetFeatures);
//      // b. Add the features that are the same for (every onset in) the complete chord
//    	Integer[][] basicTabSymbolPropertiesChord6 = tablature.getBasicTabSymbolPropertiesChord(6);
//    	double skewnessChord6 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord6);
//    	List<Double> sharedOnsetFeatures6 = 
//    		Arrays.asList(new Double[]{1/8.0, 2.0, 0.0, 9.0, skewnessChord6, 9.0, -1.0, -1.0, -1.0});
//      // c. Construct the chord's constant chord feature vector
//    	constantChordFeatureVector6.addAll(individualOnsetFeatures6);
//    	constantChordFeatureVector6.addAll(sharedOnsetFeatures6);
//
//    	// Chord 7
//    	List<Double> constantChordFeatureVector7 = new ArrayList<Double>();
//      // a. Add the features that are different for each onset in the chord
//    	List<Double> individualOnsetFeatures7 = new ArrayList<Double>();
//    	List<Double> featuresOnset70 = Arrays.asList(new Double[]{43.0, 6.0, 0.0, 3/4.0, 0.0, 3.0, -1.0, 12.0});
//    	List<Double> featuresOnset71 = Arrays.asList(new Double[]{55.0, 4.0, 2.0, 3/4.0, 1.0, 2.0, 12.0, 7.0});
//    	List<Double> featuresOnset72 = Arrays.asList(new Double[]{62.0, 2.0, 0.0, 3/4.0, 2.0, 1.0, 7.0, 5.0});
//    	List<Double> featuresOnset73 = Arrays.asList(new Double[]{67.0, 1.0, 0.0, 3/4.0, 3.0, 0.0, 5.0, -1.0});
//    	individualOnsetFeatures7.addAll(featuresOnset70);	individualOnsetFeatures7.addAll(featuresOnset71);
//    	individualOnsetFeatures7.addAll(featuresOnset72);	individualOnsetFeatures7.addAll(featuresOnset73);
//    	individualOnsetFeatures7.addAll(missingOnsetFeatures);
//      // b. Add the features that are the same for (every onset in) the complete chord
//    	Integer[][] basicTabSymbolPropertiesChord7 = tablature.getBasicTabSymbolPropertiesChord(7);
//    	double skewnessChord7 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord7);
//    	List<Double> sharedOnsetFeatures7 = 
//    		Arrays.asList(new Double[]{1/2.0, 4.0, 0.0, 24.0, skewnessChord7, 12.0, 7.0, 5.0, -1.0});
//      // c. Construct the chord's constant chord feature vector
//    	constantChordFeatureVector7.addAll(individualOnsetFeatures7);
//    	constantChordFeatureVector7.addAll(sharedOnsetFeatures7);
//
//    	// Chord 8
//    	List<Double> constantChordFeatureVector8 = new ArrayList<Double>();
//      // a. Add the features that are different for each onset in the chord
//    	List<Double> individualOnsetFeatures8 = new ArrayList<Double>();
//    	List<Double> featuresOnset80 = Arrays.asList(new Double[]{43.0, 6.0, 0.0, 1/4.0, 0.0, 3.0, -1.0, 12.0});
//    	List<Double> featuresOnset81 = Arrays.asList(new Double[]{55.0, 4.0, 2.0, 1/4.0, 1.0, 2.0, 12.0, 7.0});
//    	List<Double> featuresOnset82 = Arrays.asList(new Double[]{62.0, 2.0, 0.0, 1/4.0, 2.0, 1.0, 7.0, 5.0});
//    	List<Double> featuresOnset83 = Arrays.asList(new Double[]{67.0, 1.0, 0.0, 1/4.0, 3.0, 0.0, 5.0, -1.0});
//    	individualOnsetFeatures8.addAll(featuresOnset80); individualOnsetFeatures8.addAll(featuresOnset81);
//    	individualOnsetFeatures8.addAll(featuresOnset82);	individualOnsetFeatures8.addAll(featuresOnset83);
//    	individualOnsetFeatures8.addAll(missingOnsetFeatures);
//      // b. Add the features that are the same for (every onset in) the complete chord
//    	Integer[][] basicTabSymbolPropertiesChord8 = tablature.getBasicTabSymbolPropertiesChord(8);
//    	double skewnessChord8 = featureGenerator.getSkewnessOfChord(basicTabSymbolPropertiesChord8);
//    	List<Double> sharedOnsetFeatures8 = 
//    		Arrays.asList(new Double[]{1/4.0, 4.0, 0.0, 24.0, skewnessChord8, 12.0, 7.0, 5.0, -1.0});
//      // c. Construct the chord's constant chord feature vector
//    	constantChordFeatureVector8.addAll(individualOnsetFeatures8);
//    	constantChordFeatureVector8.addAll(sharedOnsetFeatures8);
//    	
//    	expected.add(constantChordFeatureVector0); expected.add(constantChordFeatureVector1); 
//    	expected.add(constantChordFeatureVector2); expected.add(constantChordFeatureVector3);
//    	expected.add(constantChordFeatureVector4); expected.add(constantChordFeatureVector5);
//    	expected.add(constantChordFeatureVector6); expected.add(constantChordFeatureVector7);
//    	expected.add(constantChordFeatureVector8);
//    	
//      // For each chord: calculate the actual constant chord feature vectors and add them to actual
//    	List<List<Double>> actual = new ArrayList<List<Double>>();
//    	List<List<TabSymbol>> tablatureChords = tablature.getTablatureChords();
//    	Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//    	int lowestOnsetIndex = 0;
//	    for (int i = 0; i < tablatureChords.size(); i++) {
//	    	List<TabSymbol> currentChord = tablatureChords.get(i);
////	    	List<List<Integer>> currentChordOnsetProperties = tablature.getChordOnsetProperties(i);
////	    	Integer[][] currentBasicTabSymbolPropertiesChord = tablature.getBasicTabSymbolPropertiesChord(i); 
//	    	List<Double> currentActual = featureGenerator.generateConstantChordFeatureVectorOLD(tablature, 
//	    		basicTabSymbolProperties, lowestOnsetIndex, true);
//	    	actual.add(currentActual);
//	    	lowestOnsetIndex += currentChord.size();
//	    }
//	    
//	    // Assert equality
//	    assertEquals(expected.size(), actual.size());
//	    for (int i = 0; i < expected.size(); i++) {
//	    	assertEquals(expected.get(i).size(), actual.get(i).size());
//	    	for (int j= 0; j < expected.get(i).size(); j++) {
//	    		assertEquals(expected.get(i).get(j), actual.get(i).get(j));
//	    	}
//	    }
//	    assertEquals(expected, actual);
//    }
//    else {
//    	System.out.println("ERROR: Preprocessing failed." + "\n");
//    	throw new RuntimeException("Preprocessing unsuccessful (see console for details)");
//    } 
//	}
	
	
//	public void testGenerateConstantChordFeatureVectorWithAveragesOLD() {
//	  // Make a Tablature and Transcription and preprocess them
//    File encodingFile = new File(TrainingManager.prefix + "Encodings/Tests/Testpiece 1.txt");
//    File midiFile = new File(TrainingManager.prefix + "MIDI/Tests/Testpiece 1");
//    Tablature tablature = new Tablature(encodingFile);
//    Transcription transcription = midiImport.importMidiFiles(midiFile);
//    boolean preprocessingSuccessful = preprocessor.preprocess(tablature, transcription, true, null);
//    
//    // Continue only if preprocessing is successful
//    if (preprocessingSuccessful == true) {
//    	// For each chord: determine the expected constant chord feature vectors and add them to expected
//    	List<List<Double>> expected = new ArrayList<List<Double>>();
//    	double skewness0 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(0)); 
//    	List<Double> expected0 = Arrays.asList(new Double[]{48.0, 67.0, 58.25, 1.0, 5.0, 3.0, 0.0, 2.0, 0.75, 1/4.0, 
//    		1/4.0, 1/2.0, 5/16.0, 4.0, 0.0, 2.0, 19.0, skewness0, 7.0, 8.0, 4.0, -1.0}); 
//    	double skewness1 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(1)); 
//    	List<Double> expected1 = Arrays.asList(new Double[]{43.0, 70.0, 58.75, 1.0, 6.0, 3.25, 0.0, 8.0, 2.5, 1/8.0,
//    		1/8.0, 1/2.0, 9/32.0, 4.0, 0.0, 2.0, 27.0, skewness1, 12.0, 15.0, 3.0, -1.0});
//    	double skewness2 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(2)); 
//    	List<Double> expected2 = Arrays.asList(new Double[]{46.0, 46.0, 46.0, 6.0, 6.0, 6.0, 3.0, 3.0, 3.0, 1/8.0, 1/8.0,
//    		1/8.0, 1/8.0, 1.0, 0.0, 0.0, 0.0, skewness2, -1.0, -1.0, -1.0, -1.0});
//    	double skewness3 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(3)); 
//    	List<Double> expected3 = Arrays.asList(new Double[]{45.0, 63.0, 53.25, 2.0, 6.0, 4.25, 0.0, 4.0, 1.75, 1/4.0,
//    		1/4.0, 1/2.0, 5/16.0, 4.0, 0.0, 1.0, 18.0, skewness3, 3.0, 9.0, 6.0, -1.0});
//    	double skewness4 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(4)); 
//    	List<Double> expected4 = Arrays.asList(new Double[]{43.0, 67.0, 55.6, 1.0, 6.0, 3.8, 0.0, 7.0, 2.0, 1/4.0, 1/4.0,
//    		3/2.0, 0.55, 5.0, 0.0, 2.0, 24.0, skewness4, 12.0, 0.0, 3.0, 9.0});
//    	double skewness5 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(5)); 
//    	List<Double> expected5 = Arrays.asList(new Double[]{43.0, 67.0, 57.5, 1.0, 6.0, 3.0, 0.0, 1.0, 0.25, 1/8.0, 1/8.0,
//    		1/4.0, 3/16.0, 4.0, 0.0, 3.0, 24.0, skewness5, 15.0, 4.0, 5.0, -1.0});
//    	double skewness6 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(6)); 
//    	List<Double> expected6 = Arrays.asList(new Double[]{57.0, 66.0, 61.5, 2.0, 3.0, 2.5, 0.0, 4.0, 2.0, 1/8.0, 1/8.0,
//    		9/8.0, 5/8.0, 2.0, 0.0, 1.0, 9.0, skewness6, 9.0, -1.0, -1.0, -1.0});
//    	double skewness7 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(7)); 
//    	List<Double> expected7 = Arrays.asList(new Double[]{43.0, 67.0, 56.75, 1.0, 6.0, 3.25, 0.0, 2.0, 0.5, 1/2.0, 3/4.0,
//    		3/4.0, 3/4.0, 4.0, 0.0, 3.0, 24.0, skewness7, 12.0, 7.0, 5.0, -1.0});
//    	double skewness8 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(8)); 
//    	List<Double> expected8 = Arrays.asList(new Double[]{43.0, 67.0, 56.75, 1.0, 6.0, 3.25, 0.0, 2.0, 0.5, 1/4.0, 1/4.0,
//    		1/4.0, 1/4.0, 4.0, 0.0, 3.0, 24.0, skewness8, 12.0, 7.0, 5.0, -1.0});
//    	
//    	expected.add(expected0); expected.add(expected1); expected.add(expected2); expected.add(expected3); 
//    	expected.add(expected4); expected.add(expected5);	expected.add(expected6); expected.add(expected7); 
//    	expected.add(expected8);
//      
//    	// For each chord: calculate the actual constant chord feature vectors and add them to actual
//    	List<List<Double>> actual = new ArrayList<List<Double>>();
//    	List<List<TabSymbol>> tablatureChords = tablature.getTablatureChords();
//	    Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//    	int lowestOnsetIndex = 0;
//    	for (int i = 0; i < tablatureChords.size(); i++) {
//    		 List<TabSymbol> currentChord = tablatureChords.get(i);
////	    	List<List<Integer>> currentChordOnsetProperties = tablature.getChordOnsetProperties(i);
//	    	Integer[][] currentBasicTabSymbolPropertiesChord = tablature.getBasicTabSymbolPropertiesChord(i);
//	    	List<Double> currentActual = featureGenerator.generateConstantChordFeatureVectorWithAveragesOLD(tablature,
//	    		basicTabSymbolProperties,	lowestOnsetIndex, true);
//	    	actual.add(currentActual);
//	    	lowestOnsetIndex += currentChord.size();
//	    }
//	    
//	    // Assert equality
//	    assertEquals(expected.size(), actual.size());
//	    for (int i = 0; i < expected.size(); i++) {
//	    	assertEquals(expected.get(i).size(), actual.get(i).size());
//	    	for (int j= 0; j < expected.get(i).size(); j++) {
//	    		assertEquals(expected.get(i).get(j), actual.get(i).get(j));
//	    	}
//	    }
//	    assertEquals(expected, actual);
//    }
//    else {
//    	System.out.println("ERROR: Preprocessing failed." + "\n");
//    	throw new RuntimeException("Preprocessing unsuccessful (see console for details)");
//    }
//	}
	
	
//	public void testGenerateConstantChordFeatureVectorWithAverages() {
//		// Make a Tablature and Transcription and preprocess them
//	  File encodingFile = new File(TrainingManager.prefix + "Encodings/Tests/Testpiece 1.txt");
//	  File midiFile = new File(TrainingManager.prefix + "MIDI/Tests/Testpiece 1");
//	  Tablature tablature = new Tablature(encodingFile);
//	  Transcription transcription = midiImport.importMidiFiles(midiFile);
//	  boolean preprocessingSuccessful = preprocessor.preprocess(tablature, transcription, true, null);
//	    
//	  // Continue only if preprocessing is successful
//	  if (preprocessingSuccessful == true) {
//	  	// For each chord: determine the expected constant chord feature vectors and add them to expected
//	  	List<List<Double>> expected = new ArrayList<List<Double>>();
//	  	double skewness0 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(0)); 
//	  	List<Double> expected0 = Arrays.asList(new Double[]{48.0, 67.0, 58.25, 1.0, 5.0, 3.0, 0.0, 2.0, 0.75, 1/4.0, 
//	  		1/4.0, 1/2.0, 5/16.0, 4.0, 0.0, 2.0, 19.0, skewness0, 7.0, 8.0, 4.0, -1.0}); 
//	  	double skewness1 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(1)); 
//	  	List<Double> expected1 = Arrays.asList(new Double[]{43.0, 70.0, 58.75, 1.0, 6.0, 3.25, 0.0, 8.0, 2.5, 1/8.0,
//	  		1/8.0, 1/2.0, 9/32.0, 4.0, 0.0, 2.0, 27.0, skewness1, 12.0, 15.0, 3.0, -1.0});
//	  	double skewness2 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(2)); 
//	  	List<Double> expected2 = Arrays.asList(new Double[]{46.0, 46.0, 46.0, 6.0, 6.0, 6.0, 3.0, 3.0, 3.0, 1/8.0, 1/8.0,
//	  		1/8.0, 1/8.0, 1.0, 0.0, 0.0, 0.0, skewness2, -1.0, -1.0, -1.0, -1.0});
//	  	double skewness3 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(3)); 
//	  	List<Double> expected3 = Arrays.asList(new Double[]{45.0, 63.0, 53.25, 2.0, 6.0, 4.25, 0.0, 4.0, 1.75, 1/4.0,
//	  		1/4.0, 1/2.0, 5/16.0, 4.0, 0.0, 1.0, 18.0, skewness3, 3.0, 9.0, 6.0, -1.0});
//	  	double skewness4 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(4)); 
//	  	List<Double> expected4 = Arrays.asList(new Double[]{43.0, 67.0, 55.6, 1.0, 6.0, 3.8, 0.0, 7.0, 2.0, 1/4.0, 1/4.0,
//	  		3/2.0, 0.55, 5.0, 0.0, 2.0, 24.0, skewness4, 12.0, 0.0, 3.0, 9.0});
//	  	double skewness5 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(5)); 
//	  	List<Double> expected5 = Arrays.asList(new Double[]{43.0, 67.0, 57.5, 1.0, 6.0, 3.0, 0.0, 1.0, 0.25, 1/8.0, 1/8.0,
//	  		1/4.0, 3/16.0, 4.0, 0.0, 3.0, 24.0, skewness5, 15.0, 4.0, 5.0, -1.0});
//	  	double skewness6 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(6)); 
//	  	List<Double> expected6 = Arrays.asList(new Double[]{57.0, 66.0, 61.5, 2.0, 3.0, 2.5, 0.0, 4.0, 2.0, 1/8.0, 1/8.0,
//	  		9/8.0, 5/8.0, 2.0, 0.0, 1.0, 9.0, skewness6, 9.0, -1.0, -1.0, -1.0});
//	  	double skewness7 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(7)); 
//	    List<Double> expected7 = Arrays.asList(new Double[]{43.0, 67.0, 56.75, 1.0, 6.0, 3.25, 0.0, 2.0, 0.5, 1/2.0, 3/4.0,
//	   		3/4.0, 3/4.0, 4.0, 0.0, 3.0, 24.0, skewness7, 12.0, 7.0, 5.0, -1.0});
//	    double skewness8 = featureGenerator.getSkewnessOfChord(tablature.getBasicTabSymbolPropertiesChord(8)); 
//	    List<Double> expected8 = Arrays.asList(new Double[]{43.0, 67.0, 56.75, 1.0, 6.0, 3.25, 0.0, 2.0, 0.5, 1/4.0, 1/4.0,
//	    	1/4.0, 1/4.0, 4.0, 0.0, 3.0, 24.0, skewness8, 12.0, 7.0, 5.0, -1.0});
//	    	
//	    expected.add(expected0); expected.add(expected1); expected.add(expected2); expected.add(expected3); 
//	    expected.add(expected4); expected.add(expected5);	expected.add(expected6); expected.add(expected7); 
//	    expected.add(expected8);
//	      
//	    // For each chord: calculate the actual constant chord feature vectors and add them to actual
//	    List<List<Double>> actual = new ArrayList<List<Double>>();
//	    List<List<TabSymbol>> tablatureChords = tablature.getTablatureChords();
//	    Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//	    int largestChordSizeTraining = tablature.getLargestTablatureChord();
//		  int lowestOnsetIndex = 0;
//	    for (int i = 0; i < tablatureChords.size(); i++) {
//	    	List<TabSymbol> currentChord = tablatureChords.get(i);
//		   	Integer[][] currentBasicTabSymbolPropertiesChord = tablature.getBasicTabSymbolPropertiesChord(i);
//		   	List<Double> currentActual = featureGenerator.generateConstantChordFeatureVectorWithAverages(tablature, 
//		   		basicTabSymbolProperties, largestChordSizeTraining, lowestOnsetIndex, true);
//		   	actual.add(currentActual);
//		   	lowestOnsetIndex += currentChord.size();
//		  }
//		    
//		  // Assert equality
//		  assertEquals(expected.size(), actual.size());
//		  for (int i = 0; i < expected.size(); i++) {
//		  	assertEquals(expected.get(i).size(), actual.get(i).size());
//		   	for (int j= 0; j < expected.get(i).size(); j++) {
//		   		assertEquals(expected.get(i).get(j), actual.get(i).get(j));
//		   	}
//		  }
//		  assertEquals(expected, actual);
//	  }
//	  else {
//	   	System.out.println("ERROR: Preprocessing failed." + "\n");
//	   	throw new RuntimeException("Preprocessing unsuccessful (see console for details)");
//	  }
//	}
	
	
//	public void testGenerateVariableChordFeatureVectorOLDEST() {
//	  // Make a Tablature and Transcription and preprocess them
//    File encodingFile = new File(TrainingManager.prefix + "Encodings/Tests/Testpiece 1.txt");
//    File midiFile = new File(TrainingManager.prefix + "MIDI/Tests/Testpiece 1");
//    Tablature tablature = new Tablature(encodingFile);
//    Transcription transcription = midiImport.importMidiFiles(midiFile);
//    boolean preprocessingSuccessful = preprocessor.preprocess(tablature, transcription, true, null);
//    
//    // Continue only if preprocessing is successful
//    if (preprocessingSuccessful == true) {
//      // For each chord: determine the voice assignment and add it to voiceAssignments
//   	  List<List<Integer>> voiceAssignments = new ArrayList<List<Integer>>();
//      // Chord 0: voice 0 has onset 3; voice 1 has onset 2; voice 2 has onset 1; voice 3 has onset 0; voice 4 is not in use 
//   	  List<Integer> voiceAssignment0 = Arrays.asList(new Integer[]{3, 2, 1, 0, -1}); 
//      // Chord 1: voice 0 has onset 2; voice 1 has onset 3; voice 2 has onset 1; voice 3 has onset 0; voice 4 is not in use
//   	  List<Integer> voiceAssignment1 = Arrays.asList(new Integer[]{2, 3, 1, 0, -1});
//      // Chord 2: voice 0 is not in use; voice 1 is not in use; voice 2 is not in use; voice 3 has onset 0; voice 4 is not in use
//   	  List<Integer> voiceAssignment2 = Arrays.asList(new Integer[]{-1, -1, -1, 0, -1});
//      // Chord 3: voice 0 has onset 3; voice 1 has onset 3; voice 2 has onset 2; voice 3 has onset 1; voice 4 has onset 0
//   	  List<Integer> voiceAssignment3 = Arrays.asList(new Integer[]{3, 3, 2, 1, 0});
//      // Chord 4: voice 0 has onset 4; voice 1 has onset 3; voice 2 has onset 2; voice 3 has onset 1; voice 4 has onset 0
//   	  List<Integer> voiceAssignment4 = Arrays.asList(new Integer[]{4, 3, 2, 1, 0});
//      // Chord 5: voice 0 has onset 2; voice 1 has onset 3; voice 2 has onset 1; voice 3 is not in use; voice 4 has onset 0
//   	  List<Integer> voiceAssignment5 = Arrays.asList(new Integer[]{2, 3, 1, -1, 0});
//      // Chord 6: voice 0 has onset 1; voice 1 is not in use; voice 2 has onset 0; voice 3 is not in use; voice 4 is not in use 
//   	  List<Integer> voiceAssignment6 = Arrays.asList(new Integer[]{1, -1, 0, -1, -1});
//      // Chord 7: voice 0 has onset 3; voice 1 has onset 2; voice 2 has onset 1; voice 3 has onset 0; voice 4 is not in use 
//   	  List<Integer> voiceAssignment7 = Arrays.asList(new Integer[]{3, 2, 1, 0, -1});
//      // Chord 8: voice 0 has onset 3; voice 1 has onset 2; voice 2 has onset 1; voice 3 has onset 0; voice 4 is not in use
//   	  List<Integer> voiceAssignment8 = Arrays.asList(new Integer[]{3, 2, 1, 0, -1});
//   	  voiceAssignments.add(voiceAssignment0); voiceAssignments.add(voiceAssignment1); voiceAssignments.add(voiceAssignment2); 
//   	  voiceAssignments.add(voiceAssignment3); voiceAssignments.add(voiceAssignment4); voiceAssignments.add(voiceAssignment5); 
//   	  voiceAssignments.add(voiceAssignment6); voiceAssignments.add(voiceAssignment7); voiceAssignments.add(voiceAssignment8);
//    	
//    	// For each chord: determine the expected variable chord feature vector and add it to expected
//    	List<List<Double>> expected = new ArrayList<List<Double>>(); 		
//    	// Chord 0
//    	List<Double> variableChordFeatureVector0 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector0.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0}));
//      variableChordFeatureVector0.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0}));
//      variableChordFeatureVector0.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0}));
//      variableChordFeatureVector0.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord0 = tablature.getBasicTabSymbolPropertiesChord(0);
//      variableChordFeatureVector0.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord0, voiceAssignment0));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector0.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}));
//      // Add voice assignment
//   		variableChordFeatureVector0.addAll(AuxiliaryTool.convertToListDouble(voiceAssignments.get(0)));
//   		// Chord 1
//    	List<Double> variableChordFeatureVector1 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector1.addAll(Arrays.asList(new Double[]{3.0, 4.0, 0.0, 5.0, -1.0}));
//      variableChordFeatureVector1.addAll(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}));
//      variableChordFeatureVector1.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, -1.0}));
//      variableChordFeatureVector1.addAll(Arrays.asList(new Double[]{3.0, 4.0, 0.0, -5.0, 0.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord1 = tablature.getBasicTabSymbolPropertiesChord(1);
//      variableChordFeatureVector1.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord1, voiceAssignment1));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector1.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}));
//      // Add voice assignment
//   		variableChordFeatureVector1.addAll(AuxiliaryTool.convertToListDouble(voiceAssignments.get(1)));
//   		// Chord 2
//    	List<Double> variableChordFeatureVector2 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector2.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, 3.0, -1.0}));
//      variableChordFeatureVector2.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, 1/8.0, -1.0}));
//      variableChordFeatureVector2.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, 0.0, -1.0}));
//      variableChordFeatureVector2.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 3.0, 0.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord2 = tablature.getBasicTabSymbolPropertiesChord(2);
//      variableChordFeatureVector2.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord2, voiceAssignment2));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector2.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}));
//      // Add voice assignment
//   		variableChordFeatureVector2.addAll(AuxiliaryTool.convertToListDouble(voiceAssignments.get(2)));
//   		// Chord 3
//    	List<Double> variableChordFeatureVector3 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector3.addAll(Arrays.asList(new Double[]{7.0, 4.0, 2.0, 2.0, -1.0}));
//      variableChordFeatureVector3.addAll(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/8.0, -1.0}));
//      variableChordFeatureVector3.addAll(Arrays.asList(new Double[]{1/8.0, 1/8.0, 1/8.0, 0.0, -1.0}));
//      variableChordFeatureVector3.addAll(Arrays.asList(new Double[]{-7.0, -4.0, 2.0, 2.0, 0.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord3 = tablature.getBasicTabSymbolPropertiesChord(3);
//      variableChordFeatureVector3.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord3, voiceAssignment3));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector3.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}));
//      // Add voice assignment
//   		variableChordFeatureVector3.addAll(AuxiliaryTool.convertToListDouble(voiceAssignments.get(3)));
//   		// Chord 4
//    	List<Double> variableChordFeatureVector4 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector4.addAll(Arrays.asList(new Double[]{4.0, 5.0, 2.0, 7.0, 2.0}));
//      variableChordFeatureVector4.addAll(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}));
//      variableChordFeatureVector4.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
//      variableChordFeatureVector4.addAll(Arrays.asList(new Double[]{4.0, -5.0, -2.0, 7.0, -2.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord4 = tablature.getBasicTabSymbolPropertiesChord(4);
//      variableChordFeatureVector4.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord4, voiceAssignment4));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector4.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}));
//      // Add voice assignment
//   		variableChordFeatureVector4.addAll(AuxiliaryTool.convertToListDouble(voiceAssignments.get(4)));
//   		// Chord 5
//    	List<Double> variableChordFeatureVector5 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector5.addAll(Arrays.asList(new Double[]{5.0, 9.0, 3.0, -1.0, 0.0}));
//      variableChordFeatureVector5.addAll(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, -1.0, 1/4.0}));
//      variableChordFeatureVector5.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, -1.0, 0.0}));
//      variableChordFeatureVector5.addAll(Arrays.asList(new Double[]{-5.0, 9.0, 3.0, 0.0, 0.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord5 = tablature.getBasicTabSymbolPropertiesChord(5);
//      variableChordFeatureVector5.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord5, voiceAssignment5));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector5.addAll(Arrays.asList(new Double[]{1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 5.0, 5.0}));
//      // Add voice assignment
//   		variableChordFeatureVector5.addAll(AuxiliaryTool.convertToListDouble(voiceAssignments.get(5)));
//   		// Chord 6
//    	List<Double> variableChordFeatureVector6 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector6.addAll(Arrays.asList(new Double[]{4.0, -1.0, 1.0, -1.0, -1.0}));
//      variableChordFeatureVector6.addAll(Arrays.asList(new Double[]{1/8.0, -1.0, 1/8.0, -1.0, -1.0}));
//      variableChordFeatureVector6.addAll(Arrays.asList(new Double[]{0.0, -1.0, 0.0, -1.0, -1.0}));
//      variableChordFeatureVector6.addAll(Arrays.asList(new Double[]{4.0, 0.0, -1.0, 0.0, 0.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord6 = tablature.getBasicTabSymbolPropertiesChord(6);
//      variableChordFeatureVector6.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord6, voiceAssignment6));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector6.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}));
//      // Add voice assignment
//   		variableChordFeatureVector6.addAll(AuxiliaryTool.convertToListDouble(voiceAssignments.get(6)));
//   		// Chord 7
//    	List<Double> variableChordFeatureVector7 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector7.addAll(Arrays.asList(new Double[]{1.0, 5.0, 2.0, 12.0, -1.0}));
//      variableChordFeatureVector7.addAll(Arrays.asList(new Double[]{1/8.0, 1/4.0, 1/8.0, 1/2.0, -1.0}));
//      variableChordFeatureVector7.addAll(Arrays.asList(new Double[]{0.0, 1/8.0, 0.0, 1/4.0, -1.0}));
//      variableChordFeatureVector7.addAll(Arrays.asList(new Double[]{1.0, -5.0, -2.0, -12.0, 0.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord7 = tablature.getBasicTabSymbolPropertiesChord(7);
//      variableChordFeatureVector7.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord7, voiceAssignment7));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector7.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}));
//      // Add voice assignment
//   		variableChordFeatureVector7.addAll(AuxiliaryTool.convertToListDouble(voiceAssignments.get(7)));
//   		// Chord 8
//    	List<Double> variableChordFeatureVector8 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector8.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, -1.0}));
//      variableChordFeatureVector8.addAll(Arrays.asList(new Double[]{3/4.0, 3/4.0, 3/4.0, 3/4.0, -1.0}));
//      variableChordFeatureVector8.addAll(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}));
//      variableChordFeatureVector8.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord8 = tablature.getBasicTabSymbolPropertiesChord(8);
//      variableChordFeatureVector8.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord8, voiceAssignment8));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector8.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}));
//      // Add voice assignment
//   		variableChordFeatureVector8.addAll(AuxiliaryTool.convertToListDouble(voiceAssignments.get(8)));
//   		
//    	expected.add(variableChordFeatureVector0); expected.add(variableChordFeatureVector1); 
//    	expected.add(variableChordFeatureVector2); expected.add(variableChordFeatureVector3);
//    	expected.add(variableChordFeatureVector4); expected.add(variableChordFeatureVector5);
//    	expected.add(variableChordFeatureVector6); expected.add(variableChordFeatureVector7);
//    	expected.add(variableChordFeatureVector8);
//    	
//      // For each element of expected: turn the first 15 elements from distances into proximities
//   	  for (int i = 0; i < expected.size(); i++) {
//     		List<Double> currentExpected = expected.get(i);
//   	  	for (int j = 0; j < Transcription.MAXIMUM_NUMBER_OF_VOICES * 3; j++) {
//   	  		double oldValue = currentExpected.get(j);
//   	  		// Do only if oldValue is not -1.0, i.e., if the voice is active
//   	  		if (oldValue != -1.0) {
//   	  		  double newValue = 1.0/(oldValue + 1);
//   	  		  currentExpected.set(j, newValue);
//   	  		}
//   	  	}
//   	  }
//    	
//      // For each chord: calculate the actual variable chord feature vector and add it to actual
//    	List<List<Double>> actual = new ArrayList<List<Double>>();
//    	List<List<TabSymbol>> tablatureChords = tablature.getTablatureChords();
//    	Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//    	int lowestOnsetIndex = 0;
//	    for (int i = 0; i < tablatureChords.size(); i++) {
//	    	List<TabSymbol> currentChord = tablatureChords.get(i);
//	    	List<Integer> currentVoiceAssignment = voiceAssignments.get(i);
////	    	List<List<Integer>> currentChordOnsetProperties = tablature.getChordOnsetProperties(i);
//	    	List<Double> currentActual = featureGenerator.generateVariableChordFeatureVectorOLDEST(tablature, 
//	    		basicTabSymbolProperties, transcription, lowestOnsetIndex, currentVoiceAssignment);
//	    	actual.add(currentActual);
//	    	lowestOnsetIndex += currentChord.size();
//	    }
//	    
//	    // Assert equality
//	    assertEquals(expected.size(), actual.size());
//	    for (int i = 0; i < expected.size(); i++) {
//	    	assertEquals(expected.get(i).size(), actual.get(i).size());
//	    	for (int j= 0; j < expected.get(i).size(); j++) {
//	    		assertEquals(expected.get(i).get(j), actual.get(i).get(j));
//	    	}
//	    }
//	    assertEquals(expected, actual);
//    }
//    else {
//    	System.out.println("ERROR: Preprocessing failed." + "\n");
//    	throw new RuntimeException("Preprocessing unsuccessful (see console for details)");
//    }
//	}
	
	
//	public void testGenerateVariableChordFeatureVectorOLD() {
//	  // Make a Tablature and Transcription and preprocess them
//    File encodingFile = new File(TrainingManager.prefix + "Encodings/Tests/Testpiece 1.txt");
//    File midiFile = new File(TrainingManager.prefix + "MIDI/Tests/Testpiece 1");
//    Tablature tablature = new Tablature(encodingFile);
//    Transcription transcription = midiImport.importMidiFiles(midiFile);
//    boolean preprocessingSuccessful = preprocessor.preprocess(tablature, transcription, true, null);
//    
//    // Continue only if preprocessing is successful
//    if (preprocessingSuccessful == true) {
//      // For each chord: determine the voice assignment and add it to voiceAssignments
//   	  List<List<Integer>> voiceAssignments = new ArrayList<List<Integer>>(); 
//  	  List<Integer> voiceAssignment0 = Arrays.asList(new Integer[]{3, 2, 1, 0, -1}); 
// 	    List<Integer> voiceAssignment1 = Arrays.asList(new Integer[]{2, 3, 1, 0, -1});
// 	    List<Integer> voiceAssignment2 = Arrays.asList(new Integer[]{-1, -1, -1, 0, -1});
// 	    List<Integer> voiceAssignment3 = Arrays.asList(new Integer[]{3, 3, 2, 1, 0});
// 	    List<Integer> voiceAssignment4 = Arrays.asList(new Integer[]{4, 3, 2, 1, 0});
// 	    List<Integer> voiceAssignment5 = Arrays.asList(new Integer[]{2, 3, 1, -1, 0});
// 	    List<Integer> voiceAssignment6 = Arrays.asList(new Integer[]{1, -1, 0, -1, -1});
// 	    List<Integer> voiceAssignment7 = Arrays.asList(new Integer[]{3, 2, 1, 0, -1});
// 	    List<Integer> voiceAssignment8 = Arrays.asList(new Integer[]{3, 2, 1, 0, -1});
//   	  voiceAssignments.add(voiceAssignment0); voiceAssignments.add(voiceAssignment1); voiceAssignments.add(voiceAssignment2); 
//   	  voiceAssignments.add(voiceAssignment3); voiceAssignments.add(voiceAssignment4); voiceAssignments.add(voiceAssignment5); 
//   	  voiceAssignments.add(voiceAssignment6); voiceAssignments.add(voiceAssignment7); voiceAssignments.add(voiceAssignment8);
//    	
//   	  List<List<Double>> voiceAssignmentsAsDoubles = new ArrayList<List<Double>>();
//   	  for (List<Integer> l : voiceAssignments) {
//   	  	List<Double>  currentVoiceAssignmentAsDoubles = AuxiliaryTool.convertToListDouble(l);
//   	  	voiceAssignmentsAsDoubles.add(currentVoiceAssignmentAsDoubles);
//   	  }
//   	  
//    	// For each chord: determine the expected variable chord feature vector and add it to expected
//    	List<List<Double>> expected = new ArrayList<List<Double>>(); 		
//    	// Chord 0
//    	List<Double> variableChordFeatureVector0 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector0.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0})); 
//      variableChordFeatureVector0.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0}));
//      variableChordFeatureVector0.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0}));
//      variableChordFeatureVector0.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord0 = tablature.getBasicTabSymbolPropertiesChord(0);
//      variableChordFeatureVector0.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord0, voiceAssignment0));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector0.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}));
//   		// Add voiceAssignment
//   		variableChordFeatureVector0.addAll(voiceAssignmentsAsDoubles.get(0));
//      // Chord 1
//    	List<Double> variableChordFeatureVector1 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector1.addAll(Arrays.asList(new Double[]{3.0, 4.0, 0.0, 5.0, -1.0}));
//      variableChordFeatureVector1.addAll(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}));
//      variableChordFeatureVector1.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, -1.0}));
//      variableChordFeatureVector1.addAll(Arrays.asList(new Double[]{3.0, 4.0, 0.0, -5.0, 0.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord1 = tablature.getBasicTabSymbolPropertiesChord(1);
//      variableChordFeatureVector1.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord1, voiceAssignment1));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector1.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}));
//      // Add voiceAssignment
//   		variableChordFeatureVector1.addAll(voiceAssignmentsAsDoubles.get(1));
//      // Chord 2
//    	List<Double> variableChordFeatureVector2 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector2.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, 3.0, -1.0}));
//      variableChordFeatureVector2.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, 1/8.0, -1.0}));
//      variableChordFeatureVector2.addAll(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, 0.0, -1.0}));
//      variableChordFeatureVector2.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 3.0, 0.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord2 = tablature.getBasicTabSymbolPropertiesChord(2);
//      variableChordFeatureVector2.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord2, voiceAssignment2));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector2.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}));
//      // Add voiceAssignment
//   		variableChordFeatureVector2.addAll(voiceAssignmentsAsDoubles.get(2));
//   		// Chord 3
//    	List<Double> variableChordFeatureVector3 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector3.addAll(Arrays.asList(new Double[]{7.0, 4.0, 2.0, 2.0, -1.0}));
//      variableChordFeatureVector3.addAll(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/8.0, -1.0}));
//      variableChordFeatureVector3.addAll(Arrays.asList(new Double[]{1/8.0, 1/8.0, 1/8.0, 0.0, -1.0}));
//      variableChordFeatureVector3.addAll(Arrays.asList(new Double[]{-7.0, -4.0, 2.0, 2.0, 0.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord3 = tablature.getBasicTabSymbolPropertiesChord(3);
//      variableChordFeatureVector3.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord3, voiceAssignment3));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector3.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}));
//      // Add voiceAssignment
//   		variableChordFeatureVector3.addAll(voiceAssignmentsAsDoubles.get(3));
//   		// Chord 4
//    	List<Double> variableChordFeatureVector4 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector4.addAll(Arrays.asList(new Double[]{4.0, 5.0, 2.0, 7.0, 2.0}));
//      variableChordFeatureVector4.addAll(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}));
//      variableChordFeatureVector4.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
//      variableChordFeatureVector4.addAll(Arrays.asList(new Double[]{4.0, -5.0, -2.0, 7.0, -2.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord4 = tablature.getBasicTabSymbolPropertiesChord(4);
//      variableChordFeatureVector4.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord4, voiceAssignment4));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector4.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}));
//      // Add voiceAssignment
//   		variableChordFeatureVector4.addAll(voiceAssignmentsAsDoubles.get(4));
//   		// Chord 5
//    	List<Double> variableChordFeatureVector5 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector5.addAll(Arrays.asList(new Double[]{5.0, 9.0, 3.0, -1.0, 0.0}));
//      variableChordFeatureVector5.addAll(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, -1.0, 1/4.0}));
//      variableChordFeatureVector5.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, -1.0, 0.0}));
//      variableChordFeatureVector5.addAll(Arrays.asList(new Double[]{-5.0, 9.0, 3.0, 0.0, 0.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord5 = tablature.getBasicTabSymbolPropertiesChord(5);
//      variableChordFeatureVector5.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord5, voiceAssignment5));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector5.addAll(Arrays.asList(new Double[]{1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 5.0, 5.0}));
//      // Add voiceAssignment
//   		variableChordFeatureVector5.addAll(voiceAssignmentsAsDoubles.get(5));
//   		// Chord 6
//    	List<Double> variableChordFeatureVector6 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector6.addAll(Arrays.asList(new Double[]{4.0, -1.0, 1.0, -1.0, -1.0}));
//      variableChordFeatureVector6.addAll(Arrays.asList(new Double[]{1/8.0, -1.0, 1/8.0, -1.0, -1.0}));
//      variableChordFeatureVector6.addAll(Arrays.asList(new Double[]{0.0, -1.0, 0.0, -1.0, -1.0}));
//      variableChordFeatureVector6.addAll(Arrays.asList(new Double[]{4.0, 0.0, -1.0, 0.0, 0.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord6 = tablature.getBasicTabSymbolPropertiesChord(6);
//      variableChordFeatureVector6.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord6, voiceAssignment6));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector6.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}));
//      // Add voiceAssignment
//   		variableChordFeatureVector6.addAll(voiceAssignmentsAsDoubles.get(6));
//   		// Chord 7
//    	List<Double> variableChordFeatureVector7 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector7.addAll(Arrays.asList(new Double[]{1.0, 5.0, 2.0, 12.0, -1.0}));
//      variableChordFeatureVector7.addAll(Arrays.asList(new Double[]{1/8.0, 1/4.0, 1/8.0, 1/2.0, -1.0}));
//      variableChordFeatureVector7.addAll(Arrays.asList(new Double[]{0.0, 1/8.0, 0.0, 1/4.0, -1.0}));
//      variableChordFeatureVector7.addAll(Arrays.asList(new Double[]{1.0, -5.0, -2.0, -12.0, 0.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord7 = tablature.getBasicTabSymbolPropertiesChord(7);
//      variableChordFeatureVector7.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord7, voiceAssignment7));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector7.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}));
//      // Add voiceAssignment
//   		variableChordFeatureVector7.addAll(voiceAssignmentsAsDoubles.get(7));
//   		// Chord 8
//    	List<Double> variableChordFeatureVector8 = new ArrayList<Double>();
//      // Add pitch- and time proximities and pitch movement information
//      variableChordFeatureVector8.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, -1.0}));
//      variableChordFeatureVector8.addAll(Arrays.asList(new Double[]{3/4.0, 3/4.0, 3/4.0, 3/4.0, -1.0}));
//      variableChordFeatureVector8.addAll(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}));
//      variableChordFeatureVector8.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
//   		// Add pitch-voice relation information
//      Integer[][] basicTabSymbolPropertiesChord8 = tablature.getBasicTabSymbolPropertiesChord(8);
//      variableChordFeatureVector8.add(featureGenerator.getPitchVoiceRelationInChord(basicTabSymbolPropertiesChord8, voiceAssignment8));
//   		// Add voice crossing information: voices involved in crossing; number of voice crossing pairs, total and average
//      // pitch distances
//   		variableChordFeatureVector8.addAll(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}));
//      // Add voiceAssignment
//   		variableChordFeatureVector8.addAll(voiceAssignmentsAsDoubles.get(8));
//   		
//    	expected.add(variableChordFeatureVector0); expected.add(variableChordFeatureVector1); 
//    	expected.add(variableChordFeatureVector2); expected.add(variableChordFeatureVector3);
//    	expected.add(variableChordFeatureVector4); expected.add(variableChordFeatureVector5);
//    	expected.add(variableChordFeatureVector6); expected.add(variableChordFeatureVector7);
//    	expected.add(variableChordFeatureVector8);
//    	
//      // For each element of expected: turn the first largestNumberOfVoicesTraining * 3 elements from
//    	// distances into proximities
//    	int highestNumberOfVoicesTraining = transcription.getNumberOfVoices();
//    	for (int i = 0; i < expected.size(); i++) {
//     		List<Double> currentExpected = expected.get(i);
//   	  	for (int j = 0; j < highestNumberOfVoicesTraining * 3; j++) {
//   	  		double oldValue = currentExpected.get(j);
//   	  		// Do only if oldValue is not -1.0, i.e., if the voice is active
//   	  		if (oldValue != -1.0) {
//   	  		  double newValue = 1.0/(oldValue + 1);
//   	  		  currentExpected.set(j, newValue);
//   	  		}
//   	  	}
//   	  }
//    	
//      // For each chord: calculate the actual variable chord feature vector and add it to actual
//    	List<List<Double>> actual = new ArrayList<List<Double>>();
//    	List<List<TabSymbol>> tablatureChords = tablature.getTablatureChords();
//    	Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//    	
//    	int lowestOnsetIndex = 0;
//	    for (int i = 0; i < tablatureChords.size(); i++) {
//	    	List<TabSymbol> currentChord = tablatureChords.get(i);
//	    	List<Integer> currentVoiceAssignment = voiceAssignments.get(i);
////	    	List<List<Integer>> currentChordOnsetProperties = tablature.getChordOnsetProperties(i);
////	    	Integer[][] currentBasicTabSymbolPropertiesChord = tablature.getBasicTabSymbolPropertiesChord(i);
//	    	List<Double> currentActual = featureGenerator.generateVariableChordFeatureVectorOLD(tablature, 
//	    		basicTabSymbolProperties, transcription, lowestOnsetIndex, highestNumberOfVoicesTraining, 
//	    		currentVoiceAssignment);
//	    	actual.add(currentActual);
//	    	lowestOnsetIndex += currentChord.size();
//	    }
//	    
//	    // Assert equality
//	    assertEquals(expected.size(), actual.size());
//	    for (int i = 0; i < expected.size(); i++) {
//	    	assertEquals(expected.get(i).size(), actual.get(i).size());
//	    	for (int j = 0; j < expected.get(i).size(); j++) {
//	    		assertEquals(expected.get(i).get(j), actual.get(i).get(j));
//	    	}
//	    }
//	    assertEquals(expected, actual);
//    }
//    else {
//    	System.out.println("ERROR: Preprocessing failed." + "\n");
//    	throw new RuntimeException("Preprocessing unsuccessful (see console for details)");
//    }
//	}
	
	
//	public void testGenerateVariableChordFeatureVectorWithAveragesOLD() {
//	  // Make a Tablature and Transcription and preprocess them
//    File encodingFile = new File(TrainingManager.prefix + "Encodings/Tests/Testpiece 1.txt");
//    File midiFile = new File(TrainingManager.prefix + "MIDI/Tests/Testpiece 1");
//    Tablature tablature = new Tablature(encodingFile);
//    Transcription transcription = midiImport.importMidiFiles(midiFile);
//    boolean preprocessingSuccessful = preprocessor.preprocess(tablature, transcription, true, null);
//    
//    // Continue only if preprocessing is successful
//    if (preprocessingSuccessful == true) {
//      // For each chord: determine the voice assignment and add it to voiceAssignments
//   	  List<List<Integer>> voiceAssignments = new ArrayList<List<Integer>>();
//      // Chord 0: voice 0 has onset 3; voice 1 has onset 2; voice 2 has onset 1; voice 3 has onset 0; voice 4 is not in use 
//   	  List<Integer> voiceAssignment0 = Arrays.asList(new Integer[]{3, 2, 1, 0, -1}); 
//      // Chord 1: voice 0 has onset 2; voice 1 has onset 3; voice 2 has onset 1; voice 3 has onset 0; voice 4 is not in use
//   	  List<Integer> voiceAssignment1 = Arrays.asList(new Integer[]{2, 3, 1, 0, -1});
//      // Chord 2: voice 0 is not in use; voice 1 is not in use; voice 2 is not in use; voice 3 has onset 0; voice 4 is not in use
//   	  List<Integer> voiceAssignment2 = Arrays.asList(new Integer[]{-1, -1, -1, 0, -1});
//      // Chord 3: voice 0 has onset 3; voice 1 has onset 3; voice 2 has onset 2; voice 3 has onset 1; voice 4 has onset 0
//   	  List<Integer> voiceAssignment3 = Arrays.asList(new Integer[]{3, 3, 2, 1, 0});
//      // Chord 4: voice 0 has onset 4; voice 1 has onset 3; voice 2 has onset 2; voice 3 has onset 1; voice 4 has onset 0
//   	  List<Integer> voiceAssignment4 = Arrays.asList(new Integer[]{4, 3, 2, 1, 0});
//      // Chord 5: voice 0 has onset 2; voice 1 has onset 3; voice 2 has onset 1; voice 3 is not in use; voice 4 has onset 0
//   	  List<Integer> voiceAssignment5 = Arrays.asList(new Integer[]{2, 3, 1, -1, 0});
//      // Chord 6: voice 0 has onset 1; voice 1 is not in use; voice 2 has onset 0; voice 3 is not in use; voice 4 is not in use 
//   	  List<Integer> voiceAssignment6 = Arrays.asList(new Integer[]{1, -1, 0, -1, -1});
//      // Chord 7: voice 0 has onset 3; voice 1 has onset 2; voice 2 has onset 1; voice 3 has onset 0; voice 4 is not in use 
//   	  List<Integer> voiceAssignment7 = Arrays.asList(new Integer[]{3, 2, 1, 0, -1});
//      // Chord 8: voice 0 has onset 3; voice 1 has onset 2; voice 2 has onset 1; voice 3 has onset 0; voice 4 is not in use
//   	  List<Integer> voiceAssignment8 = Arrays.asList(new Integer[]{3, 2, 1, 0, -1});
//   	  voiceAssignments.add(voiceAssignment0); voiceAssignments.add(voiceAssignment1); voiceAssignments.add(voiceAssignment2); 
//   	  voiceAssignments.add(voiceAssignment3); voiceAssignments.add(voiceAssignment4); voiceAssignments.add(voiceAssignment5); 
//   	  voiceAssignments.add(voiceAssignment6); voiceAssignments.add(voiceAssignment7); voiceAssignments.add(voiceAssignment8);
//    	
//   	  List<List<Double>> voiceAssignmentsAsDoubles= new ArrayList<List<Double>>();
//   	  voiceAssignmentsAsDoubles.add(new ArrayList<Double>(Arrays.asList(new Double[]{3.0, 2.0, 1.0, 0.0, -1.0})));
//   	  voiceAssignmentsAsDoubles.add(new ArrayList<Double>(Arrays.asList(new Double[]{2.0, 3.0, 1.0, 0.0, -1.0})));
//   	  voiceAssignmentsAsDoubles.add(new ArrayList<Double>(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, 0.0, -1.0})));
//   	  voiceAssignmentsAsDoubles.add(new ArrayList<Double>(Arrays.asList(new Double[]{3.0, 3.0, 2.0, 1.0, 0.0})));
//   	  voiceAssignmentsAsDoubles.add(new ArrayList<Double>(Arrays.asList(new Double[]{4.0, 3.0, 2.0, 1.0, 0.0})));
//   	  voiceAssignmentsAsDoubles.add(new ArrayList<Double>(Arrays.asList(new Double[]{2.0, 3.0, 1.0, -1.0, 0.0})));
//   	  voiceAssignmentsAsDoubles.add(new ArrayList<Double>(Arrays.asList(new Double[]{1.0, -1.0, 0.0, -1.0, -1.0})));
//   	  voiceAssignmentsAsDoubles.add(new ArrayList<Double>(Arrays.asList(new Double[]{3.0, 2.0, 1.0, 0.0, -1.0})));
//   	  voiceAssignmentsAsDoubles.add(new ArrayList<Double>(Arrays.asList(new Double[]{3.0, 2.0, 1.0, 0.0, -1.0}))); 
//   	  
//   	  List<Integer> IDs = 
//   	  	new ArrayList<Integer>(Arrays.asList(new Integer[]{5910, 4830, 6, 6169, 7465, 4825, 2628, 5910, 5910}));
//   	  
//    	// For each chord: determine the expected variable chord feature vector and add it to expected
//   	  Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//  	  List<List<TabSymbol>> tablatureChords = tablature.getTablatureChords();
//  	  List<double[]> allAverageProximities = new ArrayList<double[]>();
//  	  int lowestOnsetIndex = 0;
//  	  for (int i = 0; i < tablatureChords.size(); i++) {
//  	  	List<TabSymbol> currentChord = tablatureChords.get(i);
//	    	double[] currentAverageProximities = featureGenerator.getAverageProximitiesAndMovementsOfChordOLD(tablature,
//	    		basicTabSymbolProperties, transcription, currentChord.size(), lowestOnsetIndex, voiceAssignments.get(i));
//	    	allAverageProximities.add(currentAverageProximities);
//	    	lowestOnsetIndex += currentChord.size();
//  	  }
//   	  List<List<Double>> expected = new ArrayList<List<Double>>(); 		
//   	  for (int i = 0; i < tablatureChords.size(); i++) {
//   	    List<Double> currentExpected = new ArrayList<Double>();
//   	    for (int j = 0; j < allAverageProximities.get(i).length; j++) {
//   	  	  currentExpected.add(allAverageProximities.get(i)[j]);
//   	    }
//    	  currentExpected.add(featureGenerator.getNumberOfActiveVoicesInChord(voiceAssignments.get(i)));
//    	  currentExpected.add(featureGenerator.getPitchVoiceRelationInChord(tablature.getBasicTabSymbolPropertiesChord(i),
//    	  	voiceAssignments.get(i)));
//  		  List<List<Integer>> voiceCrossingInformation = 
//  			  featureGenerator.getVoiceCrossingInformationInChord(tablature.getBasicTabSymbolPropertiesChord(i), 
//  			  voiceAssignments.get(i));
//   	    currentExpected.add((voiceCrossingInformation.get(1).size() / 2.0));
//   	    currentExpected.add((double)AuxiliaryTool.sumListInteger(voiceCrossingInformation.get(2)));
//   	    // NEW -->
//   	    if (voiceCrossingInformation.get(2).size() == 0) {
//   	      currentExpected.add(0.0);
//   	    }
//   	    else {
//   	  	  currentExpected.add((double)AuxiliaryTool.sumListInteger(voiceCrossingInformation.get(2)) /
//   	  	  	voiceCrossingInformation.get(2).size());
//   	    }
//   	    // <-- NEW
//   	    List<Double> currentVoiceAssignmentAsDoubles = voiceAssignmentsAsDoubles.get(i);
//   	    currentExpected.addAll(currentVoiceAssignmentAsDoubles);
////   	    double currentID = IDs.get(i) / 1000.0;
////   	    currentExpected.add(currentID);
//   	    expected.add(currentExpected);
//   	  }
//   	  
//      // For each chord: calculate the actual variable chord feature vector and add it to actual
//    	List<List<Double>> actual = new ArrayList<List<Double>>();
//    	
//    	lowestOnsetIndex = 0;
//	    for (int i = 0; i < tablatureChords.size(); i++) {
//	    	List<TabSymbol> currentChord = tablatureChords.get(i);
//	    	List<Integer> currentVoiceAssignment = voiceAssignments.get(i);
////	    	List<List<Integer>> currentChordOnsetProperties = tablature.getChordOnsetProperties(i);
////	    	Integer[][] currentBasicTabSymbolPropertiesChord = tablature.getBasicTabSymbolPropertiesChord(i);
//	    	List<Double> currentActual = featureGenerator.generateVariableChordFeatureVectorWithAveragesOLD(tablature,
//	    		basicTabSymbolProperties, transcription, lowestOnsetIndex, currentVoiceAssignment);
//	    	actual.add(currentActual);
//	    	lowestOnsetIndex += currentChord.size();
//	    }
//	    
//	    // Assert equality
//	    assertEquals(expected.size(), actual.size());
//	    for (int i = 0; i < expected.size(); i++) {
//	    	assertEquals(expected.get(i).size(), actual.get(i).size());
//	    	for (int j = 0; j < expected.get(i).size(); j++) {
//	    		assertEquals(expected.get(i).get(j), actual.get(i).get(j));
//	    	}
//	    }
//	    assertEquals(expected, actual);
//    }
//    else {
//    	System.out.println("ERROR: Preprocessing failed." + "\n");
//    	throw new RuntimeException("Preprocessing unsuccessful (see console for details)");
//    }
//	}
	
	
//  public void testGenerateAllCompleteChordFeatureVectorsWithAveragesOLD() {
//    // Make a Tablature and Transcription and preprocess them
//    File encodingFile = new File(TrainingManager.prefix + "Encodings/Tests/Testpiece 1.txt");
//    File midiFile = new File(TrainingManager.prefix + "MIDI/Tests/Testpiece 1");
//    Tablature tablature = new Tablature(encodingFile);
//    Transcription transcription = midiImport.importMidiFiles(midiFile);
//    boolean preprocessingSuccessful = preprocessor.preprocess(tablature, transcription, true, null);
//    
//    // Continue only if preprocessing is successful
//    if (preprocessingSuccessful == true) {    	
//    	int highestNumberOfVoicesTraining = transcription.getNumberOfVoices();
//    	Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//    	List<List<TabSymbol>> tablatureChords = tablature.getTablatureChords();
//    	List<List<List<Double>>> groundTruthChordVoiceLabels = transcription.getMostRecentChordVoiceLabels(tablature);
//    	List<List<List<Integer>>> allOrderedVoiceAssignments = 
//   	  	featureGenerator.getOrderedVoiceAssignmentPossibilitiesAllChords(tablature, groundTruthChordVoiceLabels,
//   	  	highestNumberOfVoicesTraining);
//   	  
//    	// For each chord: determine the expected complete chord feature vectors and add them to expected
//   	  List<List<List<Double>>> expected = new ArrayList<List<List<Double>>>();
////   	 int largestChordSizeTraining = tablature.getLargestTablatureChord();
//   	  int lowestOnsetIndex = 0;
//    	// For each chord
//   	  for (int i = 0; i < tablatureChords.size(); i++) {
//   	  	List<TabSymbol> currentChord = tablatureChords.get(i);
////    		List<List<Integer>> currentChordOnsetProperties = tablature.getChordOnsetProperties(i);
//    		Integer[][] currentBasicTabSymbolPropertiesChord = tablature.getBasicTabSymbolPropertiesChord(i);
//    		List<List<Integer>> currentOrderedVoiceAssignments = allOrderedVoiceAssignments.get(i);
//    		List<List<Double>> currentExpectedChord = new ArrayList<List<Double>>();
//    		// For each voice assignment
//    		for (int j = 0; j < currentOrderedVoiceAssignments.size(); j++) {
//    			List<Double> currentExpected = new ArrayList<Double>();
//    			List<Integer> currentVoiceAssignment = currentOrderedVoiceAssignments.get(j);
//    	    // 1. Determine the constant chord feature vector
//    			List<Double> constantChordFeatureVector =
//    				featureGenerator.generateConstantChordFeatureVectorWithAveragesOLD(tablature, basicTabSymbolProperties,
//    				lowestOnsetIndex, true);
////    			List<Double> constantChordFeatureVector = new ArrayList<Double>();
////    			List<List<Double>> individualOnsetFeaturesChord = 
////    	    	featureGenerator.getIndividualOnsetFeaturesChordNEW(currentChordOnsetProperties, lowestOnsetIndex,
////    	    		largestChordSizeTraining);
////    			for (int k = 0; k < individualOnsetFeaturesChord.size(); k++) {
////    				constantChordFeatureVector.addAll(individualOnsetFeaturesChord.get(k));
////    			}
////    			List<Double> sharedOnsetFeaturesChord = 
////    				featureGenerator.getSharedOnsetFeaturesChordNEW(currentChordOnsetProperties, largestChordSizeTraining);
////    			constantChordFeatureVector.addAll(sharedOnsetFeaturesChord);
//    		  // 2. Determine the variable chord feature vector
//    			List<Double> variableChordFeatureVector = 
//    				featureGenerator.generateVariableChordFeatureVectorWithAveragesOLD(tablature, basicTabSymbolProperties,
//    				transcription, lowestOnsetIndex, currentVoiceAssignment);
//    			// 3. Construct the complete chord feature vector and add it to currentExpectedChord
//    			currentExpected.addAll(constantChordFeatureVector);
//    			currentExpected.addAll(variableChordFeatureVector);			
//    			currentExpectedChord.add(currentExpected);
//    		}
//        expected.add(currentExpectedChord);
//        lowestOnsetIndex += currentChord.size();
//    	}
//    	
//    	// For each chord: calculate the actual complete chord feature vectors 
//    	List<List<List<Double>>> actual = 
//    		featureGenerator.generateAllCompleteChordFeatureVectorsWithAveragesOLD(tablature, transcription, 
//    		highestNumberOfVoicesTraining, true);
//	    
//	    // Assert equality
//		  assertEquals(expected.size(), actual.size());
//		  for (int i = 0; i < expected.size(); i++) {
//		  	assertEquals(expected.get(i).size(), actual.get(i).size()); 
//		  	for (int j = 0; j < expected.get(i).size(); j++) {
//		  		assertEquals(expected.get(i).get(j).size(), actual.get(i).get(j).size());
//		  		for (int k = 0; k < expected.get(i).get(j).size(); k++) {
//	    			assertEquals(expected.get(i).get(j).get(k), actual.get(i).get(j).get(k));
//		  		}
//		  	}
//		  }
//		  assertEquals(expected, actual);
//    }
//    else {
//    	System.out.println("ERROR: Preprocessing failed." + "\n");
//    	throw new RuntimeException("Preprocessing unsuccessful (see console for details)");
//    }
//	}
	
	
//  public void testGenerateAllCompleteChordFeatureVectorsWithAverages() {
//    // Make a Tablature and Transcription and preprocess them
//    File encodingFile = new File(TrainingManager.prefix + "Encodings/Tests/Testpiece 1.txt");
//    File midiFile = new File(TrainingManager.prefix + "MIDI/Tests/Testpiece 1");
//    Tablature tablature = new Tablature(encodingFile);
//    Transcription transcription = midiImport.importMidiFiles(midiFile);
//    boolean preprocessingSuccessful = preprocessor.preprocess(tablature, transcription, true, null);
//    
//    // Continue only if preprocessing is successful
//    if (preprocessingSuccessful == true) {    	
//    	int highestNumberOfVoicesTraining = transcription.getNumberOfVoices();
//    	Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
//    	List<List<TabSymbol>> tablatureChords = tablature.getTablatureChords();
//    	List<List<List<Double>>> groundTruthChordVoiceLabels = transcription.getMostRecentChordVoiceLabels(tablature);
//    	List<List<List<Integer>>> allOrderedVoiceAssignments = 
//   	  	featureGenerator.getOrderedVoiceAssignmentPossibilitiesAllChords(tablature, groundTruthChordVoiceLabels,
//   	  	highestNumberOfVoicesTraining);
//   	  
//    	// For each chord: determine the expected complete chord feature vectors and add them to expected
//   	  List<List<List<Double>>> expected = new ArrayList<List<List<Double>>>();
//   	  int largestChordSizeTraining = tablature.getLargestTablatureChord();
//   	  int lowestOnsetIndex = 0;
//    	// For each chord
//   	  for (int i = 0; i < tablatureChords.size(); i++) {
//   	  	List<TabSymbol> currentChord = tablatureChords.get(i);
////    		List<List<Integer>> currentChordOnsetProperties = tablature.getChordOnsetProperties(i);
//   	  	Integer[][] currentBasicTabSymbolPropertiesChord = tablature.getBasicTabSymbolPropertiesChord(i);
//    		List<List<Integer>> currentOrderedVoiceAssignments = allOrderedVoiceAssignments.get(i);
//    		List<List<Double>> currentExpectedChord = new ArrayList<List<Double>>();
//    		// For each voice assignment
//    		for (int j = 0; j < currentOrderedVoiceAssignments.size(); j++) {
//    			List<Double> currentExpected = new ArrayList<Double>();
//    			List<Integer> currentVoiceAssignment = currentOrderedVoiceAssignments.get(j);
//    	    // 1. Determine the constant chord feature vector
//    			List<Double> constantChordFeatureVector = 
//    				featureGenerator.generateConstantChordFeatureVectorWithAverages(tablature,
//    				basicTabSymbolProperties,	largestChordSizeTraining, lowestOnsetIndex, true);
////    			List<Double> constantChordFeatureVector = new ArrayList<Double>();
////    			List<List<Double>> individualOnsetFeaturesChord = 
////    	    	featureGenerator.getIndividualOnsetFeaturesChordNEW(currentChordOnsetProperties, lowestOnsetIndex,
////    	    		largestChordSizeTraining);
////    			for (int k = 0; k < individualOnsetFeaturesChord.size(); k++) {
////    				constantChordFeatureVector.addAll(individualOnsetFeaturesChord.get(k));
////    			}
////    			List<Double> sharedOnsetFeaturesChord = 
////    				featureGenerator.getSharedOnsetFeaturesChordNEW(currentChordOnsetProperties, largestChordSizeTraining);
////    			constantChordFeatureVector.addAll(sharedOnsetFeaturesChord);
//    		  // 2. Determine the variable chord feature vector
//    			List<Double> variableChordFeatureVector = 
//    				featureGenerator.generateVariableChordFeatureVector(tablature, basicTabSymbolProperties,
//    				transcription, lowestOnsetIndex, highestNumberOfVoicesTraining, currentVoiceAssignment);
//    			// 3. Construct the complete chord feature vector and add it to currentExpectedChord
//    			currentExpected.addAll(constantChordFeatureVector);
//    			currentExpected.addAll(variableChordFeatureVector);			
//    			currentExpectedChord.add(currentExpected);
//    		}
//        expected.add(currentExpectedChord);
//        lowestOnsetIndex += currentChord.size();
//    	}
//    	
//    	// For each chord: calculate the actual complete chord feature vectors 
//    	List<List<List<Double>>> actual = 
//    		featureGenerator.generateAllCompleteChordFeatureVectorsWithAverages(tablature, transcription, 
//    		largestChordSizeTraining, highestNumberOfVoicesTraining, true);
//	    
//	    // Assert equality
//		  assertEquals(expected.size(), actual.size());
//		  for (int i = 0; i < expected.size(); i++) {
//		  	assertEquals(expected.get(i).size(), actual.get(i).size()); 
//		  	for (int j = 0; j < expected.get(i).size(); j++) {
//		  		assertEquals(expected.get(i).get(j).size(), actual.get(i).get(j).size());
//		  		for (int k = 0; k < expected.get(i).get(j).size(); k++) {
//	    			assertEquals(expected.get(i).get(j).get(k), actual.get(i).get(j).get(k));
//		  		}
//		  	}
//		  }
//		  assertEquals(expected, actual);
//    }
//    else {
//    	System.out.println("ERROR: Preprocessing failed." + "\n");
//    	throw new RuntimeException("Preprocessing unsuccessful (see console for details)");
//    }
//  }
	
}
