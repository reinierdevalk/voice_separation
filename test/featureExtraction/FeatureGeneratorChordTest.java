package featureExtraction;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.uos.fmt.musitech.data.structure.Note;
import external.Tablature;
import external.Transcription;
import interfaces.CLInterface;
import tbp.symbols.TabSymbol;
import tools.ToolBox;
import tools.labels.LabelTools;
import tools.text.StringTools;

public class FeatureGeneratorChordTest {

	private File encodingTestpiece1;
	private File midiTestpiece1;
	private FeatureGeneratorChord featureGeneratorChord = new FeatureGeneratorChord();
	
	private List<Double> v0;
	private List<Double> v1;
	private List<Double> v2;
	private List<Double> v3;
	private List<Double> v4; 

	private double delta;
	private int mnv;
	
	@Before
	public void setUp() throws Exception {
		delta = 1e-9;
		mnv = Transcription.MAX_NUM_VOICES;

		v0 = LabelTools.createVoiceLabel(new Integer[]{0}, mnv);
		v1 = LabelTools.createVoiceLabel(new Integer[]{1}, mnv);
		v2 = LabelTools.createVoiceLabel(new Integer[]{2}, mnv);
		v3 = LabelTools.createVoiceLabel(new Integer[]{3}, mnv);
		v4 = LabelTools.createVoiceLabel(new Integer[]{4}, mnv);
		
		Map<String, String> paths = CLInterface.getPaths(true);
		encodingTestpiece1 = new File(
			StringTools.getPathString(Arrays.asList(paths.get("ENCODINGS_PATH"), 
			"test/5vv/")) + "testpiece.tbp"
		);
		midiTestpiece1 = new File(
			StringTools.getPathString(Arrays.asList(paths.get("MIDI_PATH"), 
			"test/5vv/")) + "testpiece.mid"
		);

	}

	@After
	public void tearDown() throws Exception {
	}


	private List<List<Integer>> getVoiceAssignments() {
		List<List<Integer>> voiceAssignments = new ArrayList<List<Integer>>();
		voiceAssignments.add(Arrays.asList(new Integer[]{3, 2, 1, 0, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{2, 3, 1, 0, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{-1, -1, -1, 0, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{3, 3, 2, 1, 0}));
		voiceAssignments.add(Arrays.asList(new Integer[]{-1, -1, -1, -1, 0}));
		voiceAssignments.add(Arrays.asList(new Integer[]{4, 3, 2, 1, 0}));
		voiceAssignments.add(Arrays.asList(new Integer[]{2, 3, 1, -1, 0}));
		voiceAssignments.add(Arrays.asList(new Integer[]{1, -1, 0, -1, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{3, 2, 1, 0, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{0, -1, -1, -1, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{0, -1, -1, -1, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{0, -1, -1, -1, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{0, -1, -1, -1, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{0, -1, -1, -1, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{0, -1, -1, -1, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{3, 2, 1, 0, -1}));	 
		return voiceAssignments;
	}


	private List<List<Integer>> getVoiceAssignmentsNonTab() {		
		List<List<Integer>> voiceAssignments = new ArrayList<List<Integer>>();
		voiceAssignments.add(Arrays.asList(new Integer[]{3, 2, 1, 0, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{3, 2, 1, 0, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{-1, -1, -1, 0, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{3, 4, 2, 1, 0}));
		voiceAssignments.add(Arrays.asList(new Integer[]{-1, -1, -1, -1, 0}));
		voiceAssignments.add(Arrays.asList(new Integer[]{4, 3, 2, 1, 0}));
		voiceAssignments.add(Arrays.asList(new Integer[]{2, 3, 1, -1, 0}));
		voiceAssignments.add(Arrays.asList(new Integer[]{1, -1, 0, -1, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{3, 2, 1, 0, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{0, -1, -1, -1, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{0, -1, -1, -1, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{0, -1, -1, -1, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{0, -1, -1, -1, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{0, -1, -1, -1, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{0, -1, -1, -1, -1}));
		voiceAssignments.add(Arrays.asList(new Integer[]{3, 2, 1, 0, -1}));	 
		return voiceAssignments;
	}


	@Test
	public void testGetProximitiesAndCourseInfoAheadChord() {
		Tablature tablature = new Tablature(encodingTestpiece1);

		List<double[]> expected = new ArrayList<double[]>();
		// Chord 0
		expected.add(new double[]{1/((1/4.0) + 1), 45, 6, 57, 4, 72, 2, 69, 1,  -1, -1, 
			1/((7/16.0) + 1), 48, 6, -1, -1, -1, -1, -1, -1, -1, -1,
			1/((1/2.0) + 1), 47, 6, 50, 5, 59, 4, 65, 2, -1, -1});
		// Chord 1
		expected.add(new double[]{1/((3/16.0) + 1), 48, 6, -1, -1, -1, -1, -1, -1, -1, -1,
			1/((1/4.0) + 1), 47, 6, 50, 5, 59, 4, 65, 2, -1, -1,
			1/((3/8.0) + 1), 45, 6, -1, -1, -1, -1, -1, -1, -1, -1});
		// Chord 2
		expected.add(new double[]{1/((1/16.0) + 1), 47, 6, 50, 5, 59, 4, 65, 2, -1, -1,
			1/((3/16.0) + 1), 45, 6, -1, -1, -1, -1, -1, -1, -1, -1,
			1/((5/16.0) + 1), 45, 6, 57, 5, 57, 4, 60, 3, 69, 2});
		// Chord 3
		expected.add(new double[]{1/((1/8.0) + 1), 45, 6, -1, -1, -1, -1, -1, -1, -1, -1,
			1/((1/4.0) + 1), 45, 6, 57, 5, 57, 4, 60, 3, 69, 2,
			1/((1/2.0) + 1), 45, 6, 60, 3, 64, 2, 69, 1, -1, -1});
		// Chord 4
		expected.add(new double[]{1/((1/8.0) + 1), 45, 6, 57, 5, 57, 4, 60, 3, 69, 2,
			1/((3/8.0) + 1), 45, 6, 60, 3, 64, 2, 69, 1, -1, -1,
			1/((1/2.0) + 1), 59, 3, 68, 2, -1, -1, -1, -1, -1, -1});
		// Chord 5
		expected.add(new double[]{1/((1/4.0) + 1), 45, 6, 60, 3, 64, 2, 69, 1, -1, -1,
			1/((3/8.0) + 1), 59, 3, 68, 2, -1, -1, -1, -1, -1, -1,
			1/((1/2.0) + 1), 45, 6, 57, 4, 64, 2, 69, 1, -1, -1});
		// Chord 6
		expected.add(new double[]{1/((1/8.0) + 1), 59, 3, 68, 2, -1, -1, -1, -1, -1, -1,
			1/((1/4.0) + 1), 45, 6, 57, 4, 64, 2, 69, 1, -1, -1,
			1/((5/16.0) + 1), 68, 2, -1, -1, -1, -1, -1, -1, -1, -1});
		// Chord 7
		expected.add(new double[]{1/((1/8.0) + 1), 45, 6, 57, 4, 64, 2, 69, 1, -1, -1,
			1/((3/16.0) + 1), 68, 2, -1, -1, -1, -1, -1, -1, -1, -1,
			1/((1/4.0) + 1), 69, 1, -1, -1, -1, -1, -1, -1, -1, -1});
		// Chord 8
		expected.add(new double[]{1/((1/16.0) + 1), 68, 2, -1, -1, -1, -1, -1, -1, -1, -1,
			1/((1/8.0) + 1), 69, 1, -1, -1, -1, -1, -1, -1, -1, -1,
			1/((5/32.0) + 1), 68, 2, -1, -1, -1, -1, -1, -1, -1, -1});
		// Chord 9-14
		expected.add(new double[]{1/((1/16.0) + 1), 69, 1, -1, -1, -1, -1, -1, -1, -1, -1,
			1/((3/32.0) + 1), 68, 2, -1, -1, -1, -1, -1, -1, -1, -1,
			1/((1/8.0) + 1), 66, 2, -1, -1, -1, -1, -1, -1, -1, -1});
		expected.add(new double[]{1/((1/32.0) + 1), 68, 2, -1, -1, -1, -1, -1, -1, -1, -1,
			1/((1/16.0) + 1), 66, 2, -1, -1, -1, -1, -1, -1, -1, -1,
			1/((3/32.0) + 1), 68, 2, -1, -1, -1, -1, -1, -1, -1, -1});
		expected.add(new double[]{1/((1/32.0) + 1), 66, 2, -1, -1, -1, -1, -1, -1, -1, -1,
			1/((1/16.0) + 1), 68, 2, -1, -1, -1, -1, -1, -1, -1, -1,
			1/((3/32.0) + 1), 69, 1, -1, -1, -1, -1, -1, -1, -1, -1});
		expected.add(new double[]{1/((1/32.0) + 1), 68, 2, -1, -1, -1, -1, -1, -1, -1, -1,
			1/((1/16.0) + 1), 69, 1, -1, -1, -1, -1, -1, -1, -1, -1,
			1/((9/16.0) + 1), 45, 6, 57, 4, 64, 2, 69, 1, -1, -1});
		expected.add(new double[]{1/((1/32.0) + 1), 69, 1, -1, -1, -1, -1, -1, -1, -1, -1,
			1/((17/32.0) + 1), 45, 6, 57, 4, 64, 2, 69, 1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
		expected.add(new double[]{1/((1/2.0) + 1), 45, 6, 57, 4, 64, 2, 69, 1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1 ,-1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});
		// Chord 15		
		expected.add(new double[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});

		List<double[]> actual = new ArrayList<double[]>();
		Integer[][] btp = tablature.getBasicTabSymbolProperties();
		int lowestNoteIndex = 0;
		for (int i = 0; i < tablature.getChords().size(); i++) {
			actual.add(featureGeneratorChord.getProximitiesAndCourseInfoAheadChord(btp, null, lowestNoteIndex, 3));
			lowestNoteIndex += tablature.getChords().get(i).size();
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j], delta);
			} 
		}
	}


	@Test
	public void testGetProximitiesAndCourseInfoAheadChordNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1);

		List<double[]> expected = new ArrayList<double[]>();
		// Chord 0
		expected.add(new double[]{1/((1/4.0) + 1), 45, 57, 69, 72, -1, 
			1/((7/16.0) + 1), 48, -1, -1, -1, -1,
			1/((1/2.0) + 1), 47, 50, 59, 65, 65});
		// Chord 1
		expected.add(new double[]{1/((3/16.0) + 1), 48, -1, -1, -1, -1,
			1/((1/4.0) + 1), 47, 50, 59, 65, 65,
			1/((3/8.0) + 1), 45, -1, -1, -1, -1});
		// Chord 2
		expected.add(new double[]{1/((1/16.0) + 1), 47, 50, 59, 65, 65,
			1/((3/16.0) + 1), 45, -1, -1, -1, -1,
			1/((5/16.0) + 1), 45, 57, 57, 60, 69});
		// Chord 3
		expected.add(new double[]{1/((1/8.0) + 1), 45, -1, -1, -1, -1,
			1/((1/4.0) + 1), 45, 57, 57, 60, 69,
			1/((1/2.0) + 1), 45, 60, 64, 69, -1});
		// Chord 4
		expected.add(new double[]{1/((1/8.0) + 1), 45, 57, 57, 60, 69,
			1/((3/8.0) + 1), 45, 60, 64, 69, -1,
			1/((1/2.0) + 1), 59, 68, -1, -1, -1});
		// Chord 5
		expected.add(new double[]{1/((1/4.0) + 1), 45, 60, 64, 69, -1,
			1/((3/8.0) + 1), 59, 68, -1, -1, -1,
			1/((1/2.0) + 1), 45, 57, 64, 69, -1});
		// Chord 6
		expected.add(new double[]{1/((1/8.0) + 1), 59, 68, -1, -1, -1,
			1/((1/4.0) + 1), 45, 57, 64, 69, -1,
			1/((5/16.0) + 1), 68, -1, -1, -1, -1});
		// Chord 7
		expected.add(new double[]{1/((1/8.0) + 1), 45, 57, 64, 69, -1,
			1/((3/16.0) + 1), 68, -1, -1, -1, -1,
			1/((1/4.0) + 1), 69, -1, -1, -1, -1});
		// Chord 8
		expected.add(new double[]{1/((1/16.0) + 1), 68, -1, -1, -1, -1,
			1/((1/8.0) + 1), 69, -1, -1, -1, -1,
			1/((5/32.0) + 1), 68, -1, -1, -1, -1});
		// Chord 9-14
		expected.add(new double[]{1/((1/16.0) + 1), 69, -1, -1, -1, -1,
			1/((3/32.0) + 1), 68, -1, -1, -1, -1,
			1/((1/8.0) + 1), 66, -1, -1, -1, -1});
		expected.add(new double[]{1/((1/32.0) + 1), 68, -1, -1, -1, -1,
			1/((1/16.0) + 1), 66, -1, -1, -1, -1,
			1/((3/32.0) + 1), 68, -1, -1, -1, -1});
		expected.add(new double[]{1/((1/32.0) + 1), 66,  -1, -1, -1, -1,
			1/((1/16.0) + 1), 68,  -1, -1, -1, -1,
			1/((3/32.0) + 1), 69, -1, -1, -1, -1});
		expected.add(new double[]{1/((1/32.0) + 1), 68, -1, -1, -1, -1,
			1/((1/16.0) + 1), 69,  -1, -1, -1, -1,
			1/((9/16.0) + 1), 45, 57, 64, 69, -1});
		expected.add(new double[]{1/((1/32.0) + 1), 69, -1, -1, -1, -1,
			1/((17/32.0) + 1), 45, 57, 64, 69, -1, 
			-1, -1, -1, -1, -1, -1});
		expected.add(new double[]{1/((1/2.0) + 1), 45, 57, 64, 69, -1, 
			-1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1,});
		// Chord 15		
		expected.add(new double[]{-1, -1, -1, -1, -1, -1, 
			-1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1,});

		List<double[]> actual = new ArrayList<double[]>();
		Integer[][] bnp = transcription.getBasicNoteProperties();
		int lowestNoteIndex = 0;
		for (int i = 0; i < transcription.getChords().size(); i++) {
			actual.add(featureGeneratorChord.getProximitiesAndCourseInfoAheadChord(null, bnp, lowestNoteIndex, 3));
			lowestNoteIndex += transcription.getChords().get(i).size();
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
		assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j], delta);
			} 
		}
	}


	@Test
	public void testGetProximitiesAndMovementsOfChord() {
		Tablature tablature = new Tablature(encodingTestpiece1);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);
 
		List<double[]> expected = new ArrayList<double[]>();
		// Chord 0
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0,
			-1.0, -1.0, -1.0, -1.0, -1.0, /**/ 0.0, 0.0, 0.0, 0.0, 0.0});
		// Chord 1
		expected.add(new double[]{3.0, 4.0, 0.0, 5.0, -1.0, /**/ 1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0,
			0.0, 0.0, 0.0, 0.0, -1.0, /**/ 3.0, 4.0, 0.0, -5.0, 0.0});
		// Chord 2
		expected.add(new double[]{-1.0, -1.0, -1.0, 3.0, -1.0, /**/	-1.0, -1.0, -1.0, 3/16.0, -1.0,
			-1.0, -1.0, -1.0, 0.0, -1.0, /**/ 0.0, 0.0, 0.0, 3.0, 0.0});
		// Chord 3
		expected.add(new double[]{7.0, 4.0, 2.0, 2.0, -1.0, /**/ 1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0,
			1/16.0, 1/16.0, 1/16.0, 0.0, -1.0, /**/	-7.0, -4.0, 2.0, 2.0, 0.0});
		// Chord 4
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 2.0, /**/ -1.0, -1.0, -1.0, -1.0, 1/8.0,
			-1.0, -1.0, -1.0, -1.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, -2.0});
		// Chord 5
		expected.add(new double[]{4.0, 5.0, 2.0, 7.0, 0.0, /**/ 1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0,
			1/8.0, 1/8.0, 1/8.0, 1/8.0, 0.0, /**/ 4.0, -5.0, -2.0, 7.0, 0.0});
		// Chord 6
		expected.add(new double[]{5.0, 9.0, 3.0, -1.0, 0.0, /**/ 1/4.0, 1/4.0, 1/4.0, -1.0, 1/4.0,
			0.0, 0.0, 0.0, -1.0, 0.0, /**/ -5.0, 9.0, 3.0, 0.0, 0.0});
		// Chord 7
		expected.add(new double[]{4.0, -1.0, 1.0, -1.0, -1.0, /**/ 1/8.0, -1.0, 1/8.0, -1.0, -1.0,
			0.0, -1.0, 0.0, -1.0, -1.0, /**/ 4.0, 0.0, -1.0, 0.0, 0.0});
		// Chord 8
		expected.add(new double[]{1.0, 5.0, 2.0, 12.0, -1.0, /**/ 1/8.0, 1/4.0, 1/8.0, 1/2.0, -1.0,
			0.0, 1/8.0, 0.0, 1/4.0, -1.0, /**/ 1.0, -5.0, -2.0, -12.0, 0.0});
		// Chords 9-14
		expected.add(new double[]{1.0, -1.0, -1.0, -1.0, -1.0, /**/ 1/16.0, -1.0, -1.0, -1.0, -1.0,
			0.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, 0.0, 0.0, 0.0, 0.0});
		expected.add(new double[]{1.0, -1.0, -1.0, -1.0, -1.0, /**/ 1/16.0, -1.0, -1.0, -1.0, -1.0,
			0.0, -1.0, -1.0, -1.0, -1.0, /**/ 1.0, 0.0, 0.0, 0.0, 0.0});
		expected.add(new double[]{1.0, -1.0, -1.0, -1.0, -1.0, /**/ 1/32.0, -1.0, -1.0, -1.0, -1.0,
			0.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, 0.0, 0.0, 0.0, 0.0});
		expected.add(new double[]{2.0, -1.0, -1.0, -1.0, -1.0, /**/ 1/32.0, -1.0, -1.0, -1.0, -1.0,
			0.0, -1.0, -1.0, -1.0, -1.0, /**/ -2.0, 0.0, 0.0, 0.0, 0.0});
		expected.add(new double[]{2.0, -1.0, -1.0, -1.0, -1.0, /**/ 1/32.0, -1.0, -1.0, -1.0, -1.0,
			0.0, -1.0, -1.0, -1.0, -1.0, /**/ 2.0, 0.0, 0.0, 0.0, 0.0});
		expected.add(new double[]{1.0, -1.0, -1.0, -1.0, -1.0, /**/ 1/32.0, -1.0, -1.0, -1.0, -1.0,
			0.0, -1.0, -1.0, -1.0, -1.0, /**/ 1.0, 0.0, 0.0, 0.0, 0.0});
		// Chord 15
		expected.add(new double[]{0.0, 0.0, 0.0, 0.0, -1.0, /**/ 2/4.0, 3/4.0, 3/4.0, 3/4.0, -1.0,
			1/4.0, 11/16.0, 11/16.0, 11/16.0, -1.0, /**/ 0.0, 0.0, 0.0, 0.0, 0.0});

		// For each element of expected: turn the elements in the proximities arrays from distances into proximities
		for (int i = 0; i < expected.size(); i++) {
			double[] currentExpected = expected.get(i);
			for (int j = 0; j < currentExpected.length - mnv; j++) {
				double currentValue = currentExpected[j]; 
				// Do only if currentValue is not -1.0, i.e., if the voice is active
				if (currentValue != -1.0) {
					currentExpected[j] = 1.0/(currentValue + 1);
				}
			}
		}

		List<double[]> actual = new ArrayList<double[]>();
		Integer[][] btp = tablature.getBasicTabSymbolProperties();
		List<List<Integer>> voiceAssignments = getVoiceAssignments();
		int lowestOnsetIndex = 0;
		for (int i = 0; i < tablature.getChords().size(); i++) {
			actual.add(FeatureGeneratorChord.getProximitiesAndMovementsOfChord(btp, null, transcription, 
				lowestOnsetIndex, voiceAssignments.get(i), mnv));
			lowestOnsetIndex += tablature.getChords().get(i).size(); 	
		}
		
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j], delta);
			}
		} 
	}


	@Test
	public void testGetProximitiesAndMovementsOfChordNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1);

		List<double[]> expected = new ArrayList<double[]>();
		// Chord 0
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0,
			-1.0, -1.0, -1.0, -1.0, -1.0, /**/ 0.0, 0.0, 0.0, 0.0, 0.0});
		// Chord 1
		expected.add(new double[]{3.0, 4.0, 0.0, 5.0, -1.0, /**/ 1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0,
			0.0, 0.0, 0.0, 0.0, -1.0, /**/ 3.0, 4.0, 0.0, -5.0, 0.0});
		// Chord 2
		expected.add(new double[]{-1.0, -1.0, -1.0, 3.0, -1.0, /**/	-1.0, -1.0, -1.0, 3/16.0, -1.0,
			-1.0, -1.0, -1.0, 0.0, -1.0, /**/ 0.0, 0.0, 0.0, 3.0, 0.0});
		// Chord 3
		expected.add(new double[]{7.0, 4.0, 2.0, 2.0, -1.0, /**/ 1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0,
			0.0, 1/8.0, 0.0, 0.0, -1.0, /**/	-7.0, -4.0, 2.0, 2.0, 0.0});
		// Chord 4
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 2.0, /**/ -1.0, -1.0, -1.0, -1.0, 1/8.0,
			-1.0, -1.0, -1.0, -1.0, 0.0, /**/ 0.0, 0.0, 0.0, 0.0, -2.0});
		// Chord 5
		expected.add(new double[]{4.0, 5.0, 2.0, 7.0, 0.0, /**/ 1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/8.0,
			0.0, 1/8.0, 0.0, 0.0, 0.0, /**/ 4.0, -5.0, -2.0, 7.0, 0.0});
		// Chord 6
		expected.add(new double[]{5.0, 9.0, 3.0, -1.0, 0.0, /**/ 1/4.0, 1/4.0, 1/4.0, -1.0, 1/4.0,
			0.0, 0.0, 0.0, -1.0, 0.0, /**/ -5.0, 9.0, 3.0, 0.0, 0.0});
		// Chord 7
		expected.add(new double[]{4.0, -1.0, 1.0, -1.0, -1.0, /**/ 1/8.0, -1.0, 1/8.0, -1.0, -1.0,
			0.0, -1.0, 0.0, -1.0, -1.0, /**/ 4.0, 0.0, -1.0, 0.0, 0.0});
		// Chord 8
		expected.add(new double[]{1.0, 5.0, 2.0, 12.0, -1.0, /**/ 1/8.0, 1/4.0, 1/8.0, 1/2.0, -1.0,
			0.0, 0.0, 0.0, 0.0, -1.0, /**/ 1.0, -5.0, -2.0, -12.0, 0.0});
		// Chords 9-14
		expected.add(new double[]{1.0, -1.0, -1.0, -1.0, -1.0, /**/ 1/16.0, -1.0, -1.0, -1.0, -1.0,
			0.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, 0.0, 0.0, 0.0, 0.0});
		expected.add(new double[]{1.0, -1.0, -1.0, -1.0, -1.0, /**/ 1/16.0, -1.0, -1.0, -1.0, -1.0,
			0.0, -1.0, -1.0, -1.0, -1.0, /**/ 1.0, 0.0, 0.0, 0.0, 0.0});
		expected.add(new double[]{1.0, -1.0, -1.0, -1.0, -1.0, /**/ 1/32.0, -1.0, -1.0, -1.0, -1.0,
			0.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, 0.0, 0.0, 0.0, 0.0});
		expected.add(new double[]{2.0, -1.0, -1.0, -1.0, -1.0, /**/ 1/32.0, -1.0, -1.0, -1.0, -1.0,
			0.0, -1.0, -1.0, -1.0, -1.0, /**/ -2.0, 0.0, 0.0, 0.0, 0.0});
		expected.add(new double[]{2.0, -1.0, -1.0, -1.0, -1.0, /**/ 1/32.0, -1.0, -1.0, -1.0, -1.0,
			0.0, -1.0, -1.0, -1.0, -1.0, /**/ 2.0, 0.0, 0.0, 0.0, 0.0});
		expected.add(new double[]{1.0, -1.0, -1.0, -1.0, -1.0, /**/ 1/32.0, -1.0, -1.0, -1.0, -1.0,
			0.0, -1.0, -1.0, -1.0, -1.0, /**/ 1.0, 0.0, 0.0, 0.0, 0.0});
		// Chord 15
		expected.add(new double[]{0.0, 0.0, 0.0, 0.0, -1.0, /**/ 2/4.0, 3/4.0, 3/4.0, 3/4.0, -1.0,
			1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0, /**/ 0.0, 0.0, 0.0, 0.0, 0.0});
    
		// For each element of expected: turn the elements in the proximities arrays from distances into proximities
		for (int i = 0; i < expected.size(); i++) {
			double[] currentExpected = expected.get(i);
			for (int j = 0; j < currentExpected.length - mnv; j++) {
				double currentValue = currentExpected[j]; 
				// Do only if currentValue is not -1.0, i.e., if the voice is active
				if (currentValue != -1.0) {
					currentExpected[j] = 1.0/(currentValue + 1);
				}
			}
		}

		List<double[]> actual = new ArrayList<double[]>();
		Integer[][] bnp = transcription.getBasicNoteProperties();
		List<List<Integer>> voiceAssignments = getVoiceAssignmentsNonTab();
		int lowestOnsetIndex = 0;
			for (int i = 0; i < transcription.getChords().size(); i++) {
				actual.add(FeatureGeneratorChord.getProximitiesAndMovementsOfChord(null, bnp, transcription, 
					lowestOnsetIndex, voiceAssignments.get(i), mnv));
				lowestOnsetIndex += transcription.getChords().get(i).size(); 	
			}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j], delta);
			}
		}
	}


	@Test
	public void testGetPitchVoiceRelationInChord() {
    Tablature tablature = new Tablature(encodingTestpiece1);
    Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

    // Determine expected
  	List<Double> expected = new ArrayList<Double>();
  	// Chord 0
  	List<Double> pitches0 = Arrays.asList(new Double[]{48.0, 55.0, 63.0, 67.0});
  	List<Double> voices0 = Arrays.asList(new Double[]{3.0, 2.0, 1.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches0, voices0));
  	List<Double> pitches1 = Arrays.asList(new Double[]{43.0, 55.0, 70.0, 67.0});
  	List<Double> voices1 = Arrays.asList(new Double[]{3.0, 2.0, 0.0, 1.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches1, voices1));
  	// Chord 2 contains only one element, in which case pitchVoiceRelation is 0.0 
  	expected.add(0.0);
  	// Chord 3
  	List<Double> pitches3 = Arrays.asList(new Double[]{45.0, 48.0, 57.0, 63.0, 63.0});
  	List<Double> voices3 = Arrays.asList(new Double[]{4.0, 3.0, 2.0, 0.0, 1.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches3, voices3));
  	// Chord 4 contains only one element, in which case pitchVoiceRelation is 0.0
  	expected.add(0.0);
  	// Chord 5
  	List<Double> pitches5 = Arrays.asList(new Double[]{43.0, 55.0, 55.0, 58.0, 67.0});
  	List<Double> voices5 = Arrays.asList(new Double[]{4.0, 3.0, 2.0, 1.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches5, voices5));
  	// Chord 6
  	List<Double> pitches6 = Arrays.asList(new Double[]{43.0, 58.0, 62.0, 67.0});
  	List<Double> voices6 = Arrays.asList(new Double[]{4.0, 2.0, 0.0, 1.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches6, voices6));
  	// Chord 7
  	List<Double> pitches7 = Arrays.asList(new Double[]{57.0, 66.0});
  	List<Double> voices7 = Arrays.asList(new Double[]{2.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches7, voices7));
  	// Chord 8
  	List<Double> pitches8 = Arrays.asList(new Double[]{43.0, 55.0, 62.0, 67.0});
  	List<Double> voices8 = Arrays.asList(new Double[]{3.0, 2.0, 1.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches8, voices8));
  	// Chords 9-14 contain only one element, in which case pitchVoiceRelation is 0.0
    expected.add(0.0);
    expected.add(0.0);
    expected.add(0.0);
    expected.add(0.0);
    expected.add(0.0);
    expected.add(0.0);
  	// Chord 15
  	List<Double> pitches15 = Arrays.asList(new Double[]{43.0, 55.0, 62.0, 67.0});
  	List<Double> voices15 = Arrays.asList(new Double[]{3.0, 2.0, 1.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches15, voices15));
  	
  	// Calculate actual
  	List<Double> actual = new ArrayList<Double>();
  	List<List<Double>> voiceLabels = transcription.getVoiceLabels();
    Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
    int highestNumberOfVoices = transcription.getNumberOfVoices();
    int lowestNoteIndex = 0;
    for (int i = 0; i < tablature.getChords().size(); i++) {
    	List<TabSymbol> currentChord = tablature.getChords().get(i);
    	List<List<Double>> currentChordVoiceLabels = 
    		voiceLabels.subList(lowestNoteIndex, lowestNoteIndex + currentChord.size());
    	List<Integer> currentVoiceAssignment = 
    		LabelTools.getVoiceAssignment(currentChordVoiceLabels, highestNumberOfVoices);
    	actual.add(FeatureGeneratorChord.getPitchVoiceRelationInChord(basicTabSymbolProperties, null, null, 
    		lowestNoteIndex, currentVoiceAssignment, mnv));
    	lowestNoteIndex += currentChord.size();
    }
    
    // Assert equality
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
    	assertEquals(expected.get(i), actual.get(i));
    }
    assertEquals(expected, actual); 
	}


	@Test
	public void testGetPitchVoiceRelationInChordNonTab() {		
	 	Transcription transcription = new Transcription(midiTestpiece1);

    // Determine expected
  	List<Double> expected=  new ArrayList<Double>();
  	// Chord 0
  	List<Double> pitches0 = Arrays.asList(new Double[]{50.0, 57.0, 65.0, 69.0});
  	List<Double> voices0 = Arrays.asList(new Double[]{3.0, 2.0, 1.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches0, voices0));
    // Chord 1
  	List<Double> pitches1 = Arrays.asList(new Double[]{45.0, 57.0, 69.0, 72.0});
  	List<Double> voices1 = Arrays.asList(new Double[]{3.0, 2.0, 1.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches1, voices1));
    // Chord 2
  	List<Double> pitches2 = Arrays.asList(new Double[]{48.0, 57.0, 72.0});
  	List<Double> voices2 = Arrays.asList(new Double[]{3.0, 2.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches2, voices2));
    // Chord 3
  	List<Double> pitches3 = Arrays.asList(new Double[]{47.0, 50.0, 59.0, 65.0, 65.0});
  	List<Double> voices3 = Arrays.asList(new Double[]{4.0, 3.0, 2.0, 0.0, 1.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches3, voices3));
    // Chord 4
  	List<Double> pitches4 = Arrays.asList(new Double[]{45.0, 50.0, 59.0, 65.0});
  	List<Double> voices4 = Arrays.asList(new Double[]{4.0, 3.0, 2.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches4, voices4));
  	// Chord 5
  	List<Double> pitches5 = Arrays.asList(new Double[]{45.0, 57.0, 57.0, 60.0, 69.0});
  	List<Double> voices5 = Arrays.asList(new Double[]{4.0, 3.0, 2.0, 1.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches5, voices5));
    // Chord 6
  	List<Double> pitches6 = Arrays.asList(new Double[]{45.0, 57.0, 60.0, 64.0, 69.0});
  	List<Double> voices6 = Arrays.asList(new Double[]{4.0, 3.0, 2.0, 0.0, 1.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches6, voices6));
    // Chord 7
  	List<Double> pitches7 = Arrays.asList(new Double[]{45.0, 57.0, 59.0, 68.0, 69.0});
  	List<Double> voices7 = Arrays.asList(new Double[]{4.0, 3.0, 2.0, 0.0, 1.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches7, voices7));
    // Chord 8
  	List<Double> pitches8 = Arrays.asList(new Double[]{45.0, 57.0, 64.0, 69.0});
  	List<Double> voices8 = Arrays.asList(new Double[]{3.0, 2.0, 1.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches8, voices8));
  	// Chords 9-14
  	List<Double> pitches9 = Arrays.asList(new Double[]{45.0, 57.0, 64.0, 68.0});
  	List<Double> voices9 = Arrays.asList(new Double[]{3.0, 2.0, 1.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches9, voices9));
  	List<Double> pitches10 = Arrays.asList(new Double[]{45.0, 57.0, 64.0, 69.0});
  	List<Double> voices10 = Arrays.asList(new Double[]{3.0, 2.0, 1.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches10, voices10));
  	List<Double> pitches11 = Arrays.asList(new Double[]{45.0, 57.0, 64.0, 68.0});
  	List<Double> voices11 = Arrays.asList(new Double[]{3.0, 2.0, 1.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches11, voices11));
  	List<Double> pitches12 = Arrays.asList(new Double[]{45.0, 57.0, 64.0, 66.0});
  	List<Double> voices12 = Arrays.asList(new Double[]{3.0, 2.0, 1.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches12, voices12));
  	List<Double> pitches13 = Arrays.asList(new Double[]{45.0, 57.0, 64.0, 68.0});
  	List<Double> voices13 = Arrays.asList(new Double[]{3.0, 2.0, 1.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches13, voices13));
  	List<Double> pitches14 = Arrays.asList(new Double[]{45.0, 57.0, 64.0, 69.0});
  	List<Double> voices14 = Arrays.asList(new Double[]{3.0, 2.0, 1.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches14, voices14));
    // Chord 15
  	List<Double> pitches15 = Arrays.asList(new Double[]{45.0, 57.0, 64.0, 69.0});
  	List<Double> voices15 = Arrays.asList(new Double[]{3.0, 2.0, 1.0, 0.0});
  	expected.add(ToolBox.calculateCorrelationCoefficient(pitches15, voices15));
  	
  	// Calculate actual
  	List<Double> actual = new ArrayList<Double>();
  	List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
    Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
    int highestNumberOfVoices = transcription.getNumberOfVoices();
    int lowestNoteIndex = 0;
    for (int i = 0; i < transcription.getChords().size(); i++) {
    	List<Note> currentChord = transcription.getChords().get(i);
    	List<List<Double>> currentChordVoiceLabels = 
    		allVoiceLabels.subList(lowestNoteIndex, lowestNoteIndex + currentChord.size());
    	List<Integer> currentVoiceAssignment = 
    		LabelTools.getVoiceAssignment(currentChordVoiceLabels, highestNumberOfVoices);
    	actual.add(FeatureGeneratorChord.getPitchVoiceRelationInChord(null, basicNoteProperties, allVoiceLabels,
    		lowestNoteIndex, currentVoiceAssignment, mnv));
    	lowestNoteIndex += currentChord.size();
    }
    
    // Assert equality
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
    	assertEquals(expected.get(i), actual.get(i));
    }
    assertEquals(expected, actual); 
	}


	@Test
	public void testEnumerateVoiceAssignmentPossibilitiesForChord() {
		// Determine expected
		List<List<Integer>> expected = new ArrayList<List<Integer>>();	
		// 0. For a chord of two notes where the maximum voice number is three (which gives 12 musically possible
		// voice assignments) 
		int maxVoiceNumber0 = 3;
		Integer[][] basicTabSymbolPropertiesChord0 = new Integer[2][10];
		// In basicTabSymbolProperties only pitch and chord size are important; the other properties can be dummy values
		basicTabSymbolPropertiesChord0[0] = new Integer[]{10, -1, -1, -1, -1, -1, -1, 2, -1, -1}; 
		basicTabSymbolPropertiesChord0[1] = new Integer[]{20, -1, -1, -1, -1, -1, -1, 2, -1, -1};	
		// Determine the expected voice assignments and add them to expected0
		List<List<Integer>> expected0 = new ArrayList<List<Integer>>();
		List<Integer> exp00 = Arrays.asList(new Integer[]{-1, 0, 1 /**/, -1, -1});
		List<Integer> exp01 = Arrays.asList(new Integer[]{-1, 1, 0 /**/, -1, -1});
		List<Integer> exp02 = Arrays.asList(new Integer[]{0, -1, 1 /**/, -1, -1});
		List<Integer> exp03 = Arrays.asList(new Integer[]{0, 0, 1 /**/, -1, -1});
		List<Integer> exp04 = Arrays.asList(new Integer[]{0, 1, -1 /**/, -1, -1});
		List<Integer> exp05 = Arrays.asList(new Integer[]{0, 1, 0 /**/, -1, -1});
		List<Integer> exp06 = Arrays.asList(new Integer[]{0, 1, 1 /**/, -1, -1});
		List<Integer> exp07 = Arrays.asList(new Integer[]{1, -1, 0 /**/, -1, -1});
		List<Integer> exp08 = Arrays.asList(new Integer[]{1, 0, -1 /**/, -1, -1});
		List<Integer> exp09 = Arrays.asList(new Integer[]{1, 0, 0 /**/, -1, -1});
		List<Integer> exp010 = Arrays.asList(new Integer[]{1, 0, 1 /**/, -1, -1});
		List<Integer> exp011 = Arrays.asList(new Integer[]{1, 1, 0 /**/, -1, -1});	
		expected0.add(exp00); expected0.add(exp01); expected0.add(exp02); expected0.add(exp03);	expected0.add(exp04); 
		expected0.add(exp05); expected0.add(exp06); expected0.add(exp07); expected0.add(exp08); expected0.add(exp09); 
		expected0.add(exp010); expected0.add(exp011);
		expected.addAll(expected0);

		// 1. For a chord of three notes where the maximum voice number is three (which gives 6 musically possible 
		// voice assignments) 
		int maxVoiceNumber1 = 3;
		Integer[][] basicTabSymbolPropertiesChord1 = new Integer[3][10];
		// In basicTabSymbolProperties only pitch and chord size are important; the other properties can be dummy values
		basicTabSymbolPropertiesChord1[0] = new Integer[]{10, -1, -1, -1, -1, -1, -1, 3, -1, -1}; 
		basicTabSymbolPropertiesChord1[1] = new Integer[]{20, -1, -1, -1, -1, -1, -1, 3, -1, -1};
		basicTabSymbolPropertiesChord1[2] = new Integer[]{30, -1, -1, -1, -1, -1, -1, 3, -1, -1};
		// Determine the expected voice assignments and add them to expected1
		List<List<Integer>> expected1 = new ArrayList<List<Integer>>();
//		List<Integer> exp10 = Arrays.asList(new Integer[]{0, 1, 2}); // --> has >2 voice crossing pairs; do not add
		List<Integer> exp11 = Arrays.asList(new Integer[]{0, 2, 1 /**/, -1, -1});
		List<Integer> exp12 = Arrays.asList(new Integer[]{1, 0, 2 /**/, -1, -1});
		List<Integer> exp13 = Arrays.asList(new Integer[]{1, 2, 0 /**/, -1, -1});
		List<Integer> exp14 = Arrays.asList(new Integer[]{2,  0, 1 /**/, -1, -1});
		List<Integer> exp15 = Arrays.asList(new Integer[]{2, 1, 0 /**/, -1, -1});	
		/*expected1.add(exp10);*/ expected1.add(exp11); expected1.add(exp12); expected1.add(exp13);	expected1.add(exp14); 
		expected1.add(exp15);
		expected.addAll(expected1);

		// 2. For a chord of two notes where the maximum voice number is five (which gives 110 musically possible
		// voice assignments) 
		int maxVoiceNumber2 = 5;
		Integer[][] basicTabSymbolPropertiesChord2 = new Integer[2][10];
		// In basicTabSymbolProperties only pitch and chord size are important; the other properties can be dummy values
		basicTabSymbolPropertiesChord2[0] = new Integer[]{10, -1, -1, -1, -1, -1, -1, 2, -1, -1};
		basicTabSymbolPropertiesChord2[1] = new Integer[]{20, -1, -1, -1, -1, -1, -1, 2, -1, -1};
		// Determine the expected voice assignments and add them to expected2
		List<List<Integer>> expected2 = new ArrayList<List<Integer>>();
		List<Integer> exp20 = Arrays.asList(new Integer[]{-1, -1, -1, 0, 1});
		List<Integer> exp21 = Arrays.asList(new Integer[]{-1, -1, -1, 1, 0});
		List<Integer> exp22 = Arrays.asList(new Integer[]{-1, -1, 0, -1, 1});
		List<Integer> exp23 = Arrays.asList(new Integer[]{-1, -1, 0, 0, 1});
		List<Integer> exp24 = Arrays.asList(new Integer[]{-1, -1, 0, 1, -1});
		List<Integer> exp25 = Arrays.asList(new Integer[]{-1, -1, 0, 1, 0});
		List<Integer> exp26 = Arrays.asList(new Integer[]{-1, -1, 0, 1, 1});
		List<Integer> exp27 = Arrays.asList(new Integer[]{-1, -1, 1, -1, 0});
		List<Integer> exp28 = Arrays.asList(new Integer[]{-1, -1, 1, 0, -1});
		List<Integer> exp29 = Arrays.asList(new Integer[]{-1, -1, 1, 0, 0});
		List<Integer> exp210 = Arrays.asList(new Integer[]{-1, -1, 1, 0, 1});
		List<Integer> exp211 = Arrays.asList(new Integer[]{-1, -1, 1, 1, 0});
		List<Integer> exp212 = Arrays.asList(new Integer[]{-1, 0, -1, -1, 1});
		List<Integer> exp213 = Arrays.asList(new Integer[]{-1, 0, -1, 0, 1});		
		List<Integer> exp214 = Arrays.asList(new Integer[]{-1, 0, -1, 1, -1});
		List<Integer> exp215 = Arrays.asList(new Integer[]{-1, 0, -1, 1, 0});
		List<Integer> exp216 = Arrays.asList(new Integer[]{-1, 0, -1, 1, 1});
		List<Integer> exp217 = Arrays.asList(new Integer[]{-1, 0, 0, -1, 1});
		List<Integer> exp218 = Arrays.asList(new Integer[]{-1, 0, 0, 1, -1});
//		List<Integer> exp219 = Arrays.asList(new Integer[]{-1, 0, 0, 1, 1}); // --> has >2 voice crossing pairs; do not add
		List<Integer> exp220 = Arrays.asList(new Integer[]{-1, 0, 1, -1, -1});
		List<Integer> exp221 = Arrays.asList(new Integer[]{-1, 0, 1, -1, 0});
		List<Integer> exp222 = Arrays.asList(new Integer[]{-1, 0, 1, -1, 1});		
		List<Integer> exp223 = Arrays.asList(new Integer[]{-1, 0, 1, 0, -1});
//		List<Integer> exp224 = Arrays.asList(new Integer[]{-1, 0, 1, 0, 1}); // --> has >2 voice crossing pairs; do not add
		List<Integer> exp225 = Arrays.asList(new Integer[]{-1, 0, 1, 1, -1});
		List<Integer> exp226 = Arrays.asList(new Integer[]{-1, 0, 1, 1, 0});
		List<Integer> exp227 = Arrays.asList(new Integer[]{-1, 1, -1, -1, 0});
		List<Integer> exp228 = Arrays.asList(new Integer[]{-1, 1, -1, 0, -1});
		List<Integer> exp229 = Arrays.asList(new Integer[]{-1, 1, -1, 0, 0});
		List<Integer> exp230 = Arrays.asList(new Integer[]{-1, 1, -1, 0, 1});
		List<Integer> exp231 = Arrays.asList(new Integer[]{-1, 1, -1, 1, 0});		
		List<Integer> exp232 = Arrays.asList(new Integer[]{-1, 1, 0, -1, -1});
		List<Integer> exp233 = Arrays.asList(new Integer[]{-1, 1, 0, -1, 0});
		List<Integer> exp234 = Arrays.asList(new Integer[]{-1, 1, 0, -1, 1});
		List<Integer> exp235 = Arrays.asList(new Integer[]{-1, 1, 0, 0, -1});
		List<Integer> exp236 = Arrays.asList(new Integer[]{-1, 1, 0, 0, 1});
		List<Integer> exp237 = Arrays.asList(new Integer[]{-1, 1, 0, 1, -1});
		List<Integer> exp238 = Arrays.asList(new Integer[]{-1, 1, 0, 1, 0});
		List<Integer> exp239 = Arrays.asList(new Integer[]{-1, 1, 1, -1, 0});
		List<Integer> exp240 = Arrays.asList(new Integer[]{-1, 1, 1, 0, -1});
		List<Integer> exp241 = Arrays.asList(new Integer[]{-1, 1, 1, 0, 0});			
		List<Integer> exp242 = Arrays.asList(new Integer[]{0, -1, -1, -1, 1});
		List<Integer> exp243 = Arrays.asList(new Integer[]{0, -1, -1, 0, 1});
		List<Integer> exp244 = Arrays.asList(new Integer[]{0, -1, -1, 1, -1});
		List<Integer> exp245 = Arrays.asList(new Integer[]{0, -1, -1, 1, 0});
		List<Integer> exp246 = Arrays.asList(new Integer[]{0, -1, -1, 1, 1});
		List<Integer> exp247 = Arrays.asList(new Integer[]{0, -1, 0, -1, 1});
		List<Integer> exp248 = Arrays.asList(new Integer[]{0, -1, 0, 1, -1});
//		List<Integer> exp249 = Arrays.asList(new Integer[]{0, -1, 0, 1, 1}); // --> has >2 voice crossing pairs; do not add
		List<Integer> exp250 = Arrays.asList(new Integer[]{0, -1, 1, -1, -1});
		List<Integer> exp251 = Arrays.asList(new Integer[]{0, -1, 1, -1, 0});		
		List<Integer> exp252 = Arrays.asList(new Integer[]{0, -1, 1, -1, 1});
		List<Integer> exp253 = Arrays.asList(new Integer[]{0, -1, 1, 0, -1});
//		List<Integer> exp254 = Arrays.asList(new Integer[]{0, -1, 1, 0, 1}); // --> has >2 voice crossing pairs; do not add
		List<Integer> exp255 = Arrays.asList(new Integer[]{0, -1, 1, 1, -1});
		List<Integer> exp256 = Arrays.asList(new Integer[]{0, -1, 1, 1, 0});
		List<Integer> exp257 = Arrays.asList(new Integer[]{0, 0, -1, -1, 1});
		List<Integer> exp258 = Arrays.asList(new Integer[]{0, 0, -1, 1, -1});
//		List<Integer> exp259 = Arrays.asList(new Integer[]{0, 0, -1, 1, 1}); // --> has >2 voice crossing pairs; do not add
		List<Integer> exp260 = Arrays.asList(new Integer[]{0, 0, 1, -1, -1});
//		List<Integer> exp261 = Arrays.asList(new Integer[]{0, 0, 1, -1, 1}); // --> has >2 voice crossing pairs; do not add		
//		List<Integer> exp262 = Arrays.asList(new Integer[]{0, 0, 1, 1, -1}); // --> has >2 voice crossing pairs; do not add
		List<Integer> exp263 = Arrays.asList(new Integer[]{0, 1, -1, -1, -1});
		List<Integer> exp264 = Arrays.asList(new Integer[]{0, 1, -1, -1, 0});
		List<Integer> exp265 = Arrays.asList(new Integer[]{0, 1, -1, -1, 1});
		List<Integer> exp266 = Arrays.asList(new Integer[]{0, 1, -1, 0, -1});
//		List<Integer> exp267 = Arrays.asList(new Integer[]{0, 1, -1, 0, 1}); // --> has >2 voice crossing pairs; do not add
		List<Integer> exp268 = Arrays.asList(new Integer[]{0, 1, -1, 1, -1});
		List<Integer> exp269 = Arrays.asList(new Integer[]{0, 1, -1, 1, 0});
		List<Integer> exp270 = Arrays.asList(new Integer[]{0, 1, 0, -1, -1});
//		List<Integer> exp271 = Arrays.asList(new Integer[]{0, 1, 0, -1, 1}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp272 = Arrays.asList(new Integer[]{0, 1, 0, 1, -1}); // --> has >2 voice crossing pairs; do not add
		List<Integer> exp273 = Arrays.asList(new Integer[]{0, 1, 1, -1, -1});
		List<Integer> exp274 = Arrays.asList(new Integer[]{0, 1, 1, -1, 0});
		List<Integer> exp275 = Arrays.asList(new Integer[]{0, 1, 1, 0, -1});
		List<Integer> exp276 = Arrays.asList(new Integer[]{1, -1, -1, -1, 0});
		List<Integer> exp277 = Arrays.asList(new Integer[]{1, -1, -1, 0, -1});
		List<Integer> exp278 = Arrays.asList(new Integer[]{1, -1, -1, 0, 0});
		List<Integer> exp279 = Arrays.asList(new Integer[]{1, -1, -1, 0, 1});
		List<Integer> exp280 = Arrays.asList(new Integer[]{1, -1, -1, 1, 0});
		List<Integer> exp281 = Arrays.asList(new Integer[]{1, -1, 0, -1, -1});		
		List<Integer> exp282 = Arrays.asList(new Integer[]{1, -1, 0, -1, 0});
		List<Integer> exp283 = Arrays.asList(new Integer[]{1, -1, 0, -1, 1});
		List<Integer> exp284 = Arrays.asList(new Integer[]{1, -1, 0, 0, -1});
		List<Integer> exp285 = Arrays.asList(new Integer[]{1, -1, 0, 0, 1});
		List<Integer> exp286 = Arrays.asList(new Integer[]{1, -1, 0, 1, -1});
		List<Integer> exp287 = Arrays.asList(new Integer[]{1, -1, 0, 1, 0});
		List<Integer> exp288 = Arrays.asList(new Integer[]{1, -1, 1, -1, 0});
		List<Integer> exp289 = Arrays.asList(new Integer[]{1, -1, 1, 0, -1});
		List<Integer> exp290 = Arrays.asList(new Integer[]{1, -1, 1, 0, 0});
		List<Integer> exp291 = Arrays.asList(new Integer[]{1, 0, -1, -1, -1});		
		List<Integer> exp292 = Arrays.asList(new Integer[]{1, 0, -1, -1, 0});
		List<Integer> exp293 = Arrays.asList(new Integer[]{1, 0, -1, -1, 1});
		List<Integer> exp294 = Arrays.asList(new Integer[]{1, 0, -1, 0, -1});
		List<Integer> exp295 = Arrays.asList(new Integer[]{1, 0, -1, 0, 1});
		List<Integer> exp296 = Arrays.asList(new Integer[]{1, 0, -1, 1, -1});
		List<Integer> exp297 = Arrays.asList(new Integer[]{1, 0, -1, 1, 0});
		List<Integer> exp298 = Arrays.asList(new Integer[]{1, 0, 0, -1, -1});
		List<Integer> exp299 = Arrays.asList(new Integer[]{1, 0, 0, -1, 1});
		List<Integer> exp2100 = Arrays.asList(new Integer[]{1, 0, 0, 1, -1});
		List<Integer> exp2101 = Arrays.asList(new Integer[]{1, 0, 1, -1, -1});		
		List<Integer> exp2102 = Arrays.asList(new Integer[]{1, 0, 1, -1, 0});
		List<Integer> exp2103 = Arrays.asList(new Integer[]{1, 0, 1, 0, -1});
		List<Integer> exp2104 = Arrays.asList(new Integer[]{1, 1, -1, -1, 0});
		List<Integer> exp2105 = Arrays.asList(new Integer[]{1, 1, -1, 0, -1});
		List<Integer> exp2106 = Arrays.asList(new Integer[]{1, 1, -1, 0, 0});
		List<Integer> exp2107 = Arrays.asList(new Integer[]{1, 1, 0, -1, -1});
		List<Integer> exp2108 = Arrays.asList(new Integer[]{1, 1, 0, -1, 0});
		List<Integer> exp2109 = Arrays.asList(new Integer[]{1, 1, 0, 0, -1});			
		expected2.add(exp20); expected2.add(exp21); expected2.add(exp22); expected2.add(exp23);	expected2.add(exp24); 
		expected2.add(exp25); expected2.add(exp26); expected2.add(exp27); expected2.add(exp28); expected2.add(exp29);
		expected2.add(exp210); expected2.add(exp211); expected2.add(exp212); expected2.add(exp213);	expected2.add(exp214); 
		expected2.add(exp215); expected2.add(exp216); expected2.add(exp217); expected2.add(exp218); /*expected2.add(exp219);*/
		expected2.add(exp220); expected2.add(exp221); expected2.add(exp222); expected2.add(exp223); /*expected2.add(exp224);*/ 
		expected2.add(exp225); expected2.add(exp226); expected2.add(exp227); expected2.add(exp228); expected2.add(exp229);
		expected2.add(exp230); expected2.add(exp231); expected2.add(exp232); expected2.add(exp233);	expected2.add(exp234); 
		expected2.add(exp235); expected2.add(exp236); expected2.add(exp237); expected2.add(exp238); expected2.add(exp239);
		expected2.add(exp240); expected2.add(exp241); expected2.add(exp242); expected2.add(exp243);	expected2.add(exp244); 
		expected2.add(exp245); expected2.add(exp246); expected2.add(exp247); expected2.add(exp248); /*expected2.add(exp249);*/
		expected2.add(exp250); expected2.add(exp251); expected2.add(exp252); expected2.add(exp253); /*expected2.add(exp254);*/
		expected2.add(exp255); expected2.add(exp256); expected2.add(exp257); expected2.add(exp258); /*expected2.add(exp259);*/
		expected2.add(exp260); /*expected2.add(exp261); expected2.add(exp262);*/ expected2.add(exp263);	expected2.add(exp264); 
		expected2.add(exp265); expected2.add(exp266); /*expected2.add(exp267);*/ expected2.add(exp268); expected2.add(exp269);
		expected2.add(exp270); /*expected2.add(exp271); expected2.add(exp272);*/ expected2.add(exp273);	expected2.add(exp274); 
		expected2.add(exp275); expected2.add(exp276); expected2.add(exp277); expected2.add(exp278); expected2.add(exp279);
		expected2.add(exp280); expected2.add(exp281); expected2.add(exp282); expected2.add(exp283);	expected2.add(exp284); 
		expected2.add(exp285); expected2.add(exp286); expected2.add(exp287); expected2.add(exp288); expected2.add(exp289);
		expected2.add(exp290); expected2.add(exp291); expected2.add(exp292); expected2.add(exp293);	expected2.add(exp294); 
		expected2.add(exp295); expected2.add(exp296); expected2.add(exp297); expected2.add(exp298); expected2.add(exp299);
		expected2.add(exp2100); expected2.add(exp2101); expected2.add(exp2102); expected2.add(exp2103);	expected2.add(exp2104); 
		expected2.add(exp2105); expected2.add(exp2106); expected2.add(exp2107); expected2.add(exp2108); expected2.add(exp2109);
		expected.addAll(expected2);

		// 3. For a chord of four notes where the maximum voice number is four (which gives 24 musically possible 
		// voice assignments) 
		int maxVoiceNumber3 = 4;
		Integer[][] basicTabSymbolPropertiesChord3 = new Integer[4][10];
		// In basicTabSymbolProperties only pitch and chord size are important; the other properties can be dummy values
		basicTabSymbolPropertiesChord3[0] = new Integer[]{10, -1, -1, -1, -1, -1, -1, 4, -1, -1}; 
		basicTabSymbolPropertiesChord3[1] = new Integer[]{20, -1, -1, -1, -1, -1, -1, 4, -1, -1}; 
		basicTabSymbolPropertiesChord3[2] = new Integer[]{30, -1, -1, -1, -1, -1, -1, 4, -1, -1};  
		basicTabSymbolPropertiesChord3[3] = new Integer[]{40, -1, -1, -1, -1, -1, -1, 4, -1, -1}; 		
		// Determine the expected voice assignments and add them to expected3
		List<List<Integer>> expected3 = new ArrayList<List<Integer>>();
//		List<Integer> exp30 = Arrays.asList(new Integer[]{0, 1, 2, 3}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp31 = Arrays.asList(new Integer[]{0, 1, 3, 2}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp32 = Arrays.asList(new Integer[]{0, 2, 1, 3}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp33 = Arrays.asList(new Integer[]{0, 2, 3, 1}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp34 = Arrays.asList(new Integer[]{0, 3, 1, 2}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp35 = Arrays.asList(new Integer[]{0, 3, 2, 1}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp36 = Arrays.asList(new Integer[]{1, 0, 2, 3}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp37 = Arrays.asList(new Integer[]{1, 0, 3, 2}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp38 = Arrays.asList(new Integer[]{1, 2, 0, 3}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp39 = Arrays.asList(new Integer[]{1, 2, 3, 0}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp310 = Arrays.asList(new Integer[]{1, 3, 0, 2}); // --> has >2 voice crossing pairs; do not add
		List<Integer> exp311 = Arrays.asList(new Integer[]{1, 3, 2, 0 /**/, -1});
//		List<Integer> exp312 = Arrays.asList(new Integer[]{2, 0, 1, 3}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp313 = Arrays.asList(new Integer[]{2, 0, 3, 1}); // --> has >2 voice crossing pairs; do not add
//		List<Integer> exp314 = Arrays.asList(new Integer[]{2, 1, 0, 3}); // --> has >2 voice crossing pairs; do not add
		List<Integer> exp315 = Arrays.asList(new Integer[]{2, 1, 3, 0 /**/, -1});
		List<Integer> exp316 = Arrays.asList(new Integer[]{2, 3, 0, 1 /**/, -1});
		List<Integer> exp317 = Arrays.asList(new Integer[]{2, 3, 1, 0 /**/, -1});
//		List<Integer> exp318 = Arrays.asList(new Integer[]{3, 0, 1, 2}); // --> has >2 voice crossing pairs; do not add
		List<Integer> exp319 = Arrays.asList(new Integer[]{3, 0, 2, 1 /**/, -1}); 
		List<Integer> exp320 = Arrays.asList(new Integer[]{3, 1, 0, 2 /**/, -1});
		List<Integer> exp321 = Arrays.asList(new Integer[]{3, 1, 2, 0 /**/, -1});
		List<Integer> exp322 = Arrays.asList(new Integer[]{3, 2, 0, 1 /**/, -1});		
		List<Integer> exp323 = Arrays.asList(new Integer[]{3, 2, 1, 0 /**/, -1});	
		/*expected3.add(exp30); expected3.add(exp31); expected3.add(exp32); expected3.add(exp33); expected3.add(exp34);
		expected3.add(exp35); expected3.add(exp36); expected3.add(exp37); expected3.add(exp38); expected3.add(exp39);
		expected3.add(exp310);*/ expected3.add(exp311); /*expected3.add(exp312); expected3.add(exp313); expected3.add(exp314);*/
		expected3.add(exp315); expected3.add(exp316); expected3.add(exp317); /*expected3.add(exp318);*/ expected3.add(exp319);
		expected3.add(exp320); expected3.add(exp321); expected3.add(exp322); expected3.add(exp323);
		expected.addAll(expected3);

		// Calculate actual
		List<List<Integer>> actual = new ArrayList <List<Integer>>();
		actual.addAll(FeatureGeneratorChord.enumerateVoiceAssignmentPossibilitiesForChord(basicTabSymbolPropertiesChord0, 
			null, null, 0, maxVoiceNumber0, mnv)); 
		actual.addAll(FeatureGeneratorChord.enumerateVoiceAssignmentPossibilitiesForChord(basicTabSymbolPropertiesChord1,
			null, null, 0, maxVoiceNumber1, mnv)); 
		actual.addAll(FeatureGeneratorChord.enumerateVoiceAssignmentPossibilitiesForChord(basicTabSymbolPropertiesChord2,
			null, null, 0, maxVoiceNumber2, mnv)); 
		actual.addAll(FeatureGeneratorChord.enumerateVoiceAssignmentPossibilitiesForChord(basicTabSymbolPropertiesChord3,
			null, null, 0, maxVoiceNumber3, mnv)); 

		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			System.out.println(i);
			System.out.println("expected");
			System.out.println(expected.get(i));
			System.out.println("actual");
			System.out.println(actual.get(i));
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
		assertEquals(expected, actual);
	}


	@Test
	public void testEnumerateVoiceAssignmentPossibilitiesForChordNonTab() {
		// For all chords: enumerate first all mathematical possibilities (by putting a printout and a System.exit(0) 
		// in enumerateVoiceAssignmentPossibilitiesForChord()), and then remove the musically impossible. Only the
		// remaining musically possible are given below; their numbering is the numbering in the list of mathematical 
		// possibilities

		List<List<Integer>> expected = new ArrayList<List<Integer>>();	
		// 0. For a chord of three notes where the maximum voice number is three (which gives 6 musically possible 
		// vas) and no sustained voices
		int maxVoiceNumber0 = 3;
		int lowestNoteIndex0 = 1;
		// Make the previous chord
		Integer[][] basicNotePropertiesPreviousChord0 = new Integer[1][8];
		basicNotePropertiesPreviousChord0[0] = new Integer[]{30, 0, 4, 1, 4, 0, 1, 0}; 
		// Make the current chord
		Integer[][] basicNotePropertiesChord0 = new Integer[3][8];
		basicNotePropertiesChord0[0] = new Integer[]{10, 1, 4, 1, 4, 1, 3, 0}; 
		basicNotePropertiesChord0[1] = new Integer[]{20, 1, 4, 1, 4, 1, 3, 1};
		basicNotePropertiesChord0[2] = new Integer[]{30, 1, 4, 1, 4, 1, 3, 2};
		// Make the complete basicNoteProperties
		Integer[][] basicNoteProperties0 = new Integer[4][8];
		basicNoteProperties0[0] = basicNotePropertiesPreviousChord0[0];
		basicNoteProperties0[1] = basicNotePropertiesChord0[0];
		basicNoteProperties0[2] = basicNotePropertiesChord0[1];
		basicNoteProperties0[3] = basicNotePropertiesChord0[2];
		// Make the voice labels (only those for the previous note are necessary)
		List<List<Double>> voiceLabels0 = new ArrayList<List<Double>>();
		voiceLabels0.add(Arrays.asList(new Double[]{0.0, 1.0, 0.0}));	
		// Determine all musically possible voice assignments
		List<List<Integer>> expected0 = new ArrayList<List<Integer>>();
		List<Integer> exp00 = Arrays.asList(new Integer[]{0, 1, 2 /**/, -1, -1});
		List<Integer> exp01 = Arrays.asList(new Integer[]{0, 2, 1 /**/, -1, -1});
		List<Integer> exp02 = Arrays.asList(new Integer[]{1, 0, 2 /**/, -1, -1});
		List<Integer> exp03 = Arrays.asList(new Integer[]{1, 2, 0 /**/, -1, -1});
		List<Integer> exp04 = Arrays.asList(new Integer[]{2, 0, 1 /**/, -1, -1});
		List<Integer> exp05 = Arrays.asList(new Integer[]{2, 1, 0 /**/, -1, -1});	
		// Add only those voice assignments with two or fewer voice crossing pairs to expected0
//		expected0.add(exp00); // voices: 0, 1, 2 / pitches: 10, 20, 30 / --> 3 vcp (0-1, 0-2, 1-2) 
//							// note 0 (pitch 10) has voice 0; note 1 (pitch 20) has voice 1; note 2 (pitch 30) has voice 2 
		expected0.add(exp01); // voices: 0, 2, 1 / pitches: 10, 20, 30 / --> 2 vcp (0-2, 0-1) 
							// note 0 (pitch 10) has voice 0; note 1 (pitch 20) has voice 2; note 2 (pitch 30) has voice 1 
		expected0.add(exp02); // voices: 1, 0, 2 / pitches: 10, 20, 30 / --> 2 vcp (1-2, 0-2) 
							// note 0 (pitch 10) has voice 1; note 1 (pitch 20) has voice 0; note 2 (pitch 30) has voice 2 
		expected0.add(exp03); // voices: 2, 0, 1 / pitches: 10, 20, 30 / --> 1 vcp (0-1) 
							// note 0 (pitch 10) has voice 2; note 1 (pitch 20) has voice 0; note 2 (pitch 30) has voice 1 
		expected0.add(exp04); // voices: 1, 2, 0 / pitches: 10, 20, 30 / --> 1 vcp (1-2) 
							// note 0 (pitch 10) has voice 1; note 1 (pitch 20) has voice 2; note 2 (pitch 30) has voice 0 
		expected0.add(exp05); // voices: 2, 1, 0 / pitches: 10, 20, 30 / --> 0 vcp 
							// note 0 (pitch 10) has voice 2; note 1 (pitch 20) has voice 1; note 2 (pitch 30) has voice 0 
		expected.addAll(expected0);

		// 1. For a chord of two notes where the maximum voice number is three (which gives 6 musically possible vas)
		// and one sustained voice (which gives 2 musically possible vas): 2 (pitch 30)
		int maxVoiceNumber1 = 3;
		int lowestNoteIndex1 = 1;
		// Make the previous chord with the sustained note
		Integer[][] basicNotePropertiesPreviousChord1 = new Integer[1][8];
		basicNotePropertiesPreviousChord1[0] = new Integer[]{30, 0, 4, 1, 2, 0, 1, 0}; 
		// Make the current chord
		Integer[][] basicNotePropertiesChord1 = new Integer[2][8];
		basicNotePropertiesChord1[0] = new Integer[]{10, 1, 4, 1, 4, 1, 2, 0}; 
		basicNotePropertiesChord1[1] = new Integer[]{20, 1, 4, 1, 4, 1, 2, 1};
		// Make the complete basicNoteProperties
		Integer[][] basicNoteProperties1 = new Integer[3][8];
		basicNoteProperties1[0] = basicNotePropertiesPreviousChord1[0];
		basicNoteProperties1[1] = basicNotePropertiesChord1[0];
		basicNoteProperties1[2] = basicNotePropertiesChord1[1];
		// Make the voice labels (only those for the previous note are necessary)
		List<List<Double>> voiceLabels1 = new ArrayList<List<Double>>();
		voiceLabels1.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0}));			
		// Determine all musically possible voice assignments
		List<List<Integer>> expected1 = new ArrayList<List<Integer>>();
//		List<Integer> exp10 = Arrays.asList(new Integer[]{-1, 0, 1}); 
//		List<Integer> exp11 = Arrays.asList(new Integer[]{-1, 1, 0}); 
//		List<Integer> exp12 = Arrays.asList(new Integer[]{0, -1, 1}); 
		List<Integer> exp14 = Arrays.asList(new Integer[]{0, 1, -1 /**/, -1, -1}); // leaves voice 2 free
//		List<Integer> exp17 = Arrays.asList(new Integer[]{1, -1, 0});
		List<Integer> exp18 = Arrays.asList(new Integer[]{1, 0, -1 /**/, -1, -1}); // leaves voice 2 free
		// Add only those voice assignments that leave voice 2 free to expected1; do not add voice assignments with 
		// more than two voice crossing pairs 
//		expected1.add(exp14); // voices: 0, 1, (2) / pitches: 10, 20, 30 / --> 3 vcp (0-1, 0-2, 1-2) 
//							// note 0 (pitch 10) has voice 0; note 1 (pitch 20) has voice 1; sustained note (pitch 30) has voice 2
		expected1.add(exp18); // voices: 1, 0, (2) / pitches: 10, 20, 30 / --> 2 vcp (1-2, 0-2) 
							// note 0 (pitch 10) has voice 1; note 1 (pitch 20) has voice 0; sustained note (pitch 30) has voice 2
		expected.addAll(expected1); 

		// 2. For a chord of two notes where the maximum voice number is five (which gives 20 musically possible vas)
		// and two sustained voices (which gives 6 musically possible vas): 4 (pitch 5) and 2 (pitch 40) 
		int maxVoiceNumber2 = 5;
		int lowestNoteIndex2 = 2;
		// Make the previous chord with the sustained notes
		Integer[][] basicNotePropertiesPreviousChord2 = new Integer[2][8];
		basicNotePropertiesPreviousChord2[0] = new Integer[]{5, 0, 4, 1, 2, 0, 2, 0}; 
		basicNotePropertiesPreviousChord2[1] = new Integer[]{40, 0, 4, 1, 2, 0, 2, 1}; 
		// Make the current chord
		Integer[][] basicNotePropertiesChord2 = new Integer[2][8];
		basicNotePropertiesChord2[0] = new Integer[]{10, 1, 4, 1, 4, 1, 2, 0};
		basicNotePropertiesChord2[1] = new Integer[]{20, 1, 4, 1, 4, 1, 2, 1};		
		// Make the complete basicNoteProperties
		Integer[][] basicNoteProperties2 = new Integer[4][8];
		basicNoteProperties2[0] = basicNotePropertiesPreviousChord2[0];
		basicNoteProperties2[1] = basicNotePropertiesPreviousChord2[1];
		basicNoteProperties2[2] = basicNotePropertiesChord2[0];
		basicNoteProperties2[3] = basicNotePropertiesChord2[1];
		// Make the voice labels (only those for the previous note are necessary)
		List<List<Double>> voiceLabels2 = new ArrayList<List<Double>>();
		voiceLabels2.add(v4);
		voiceLabels2.add(v2);	
		// Determine all musically possible voice assignments
		List<List<Integer>> expected2 = new ArrayList<List<Integer>>();
//		List<Integer> exp20 = Arrays.asList(new Integer[]{-1, -1, -1, 0, 1});
//		List<Integer> exp21 = Arrays.asList(new Integer[]{-1, -1, -1, 1, 0});
//		List<Integer> exp22 = Arrays.asList(new Integer[]{-1, -1, 0, -1, 1});
//		List<Integer> exp24 = Arrays.asList(new Integer[]{-1, -1, 0, 1, -1}); 
//		List<Integer> exp27 = Arrays.asList(new Integer[]{-1, -1, 1, -1, 0});
//		List<Integer> exp28 = Arrays.asList(new Integer[]{-1, -1, 1, 0, -1});
//		List<Integer> exp212 = Arrays.asList(new Integer[]{-1, 0, -1, -1, 1});		
		List<Integer> exp214 = Arrays.asList(new Integer[]{-1, 0, -1, 1, -1}); // leaves voices 4 and 2 free
//		List<Integer> exp220 = Arrays.asList(new Integer[]{-1, 0, 1, -1, -1});
//		List<Integer> exp227 = Arrays.asList(new Integer[]{-1, 1, -1, -1, 0});
		List<Integer> exp228 = Arrays.asList(new Integer[]{-1, 1, -1, 0, -1}); // leaves voices 4 and 2 free
//		List<Integer> exp232 = Arrays.asList(new Integer[]{-1, 1, 0, -1, -1});
//		List<Integer> exp242 = Arrays.asList(new Integer[]{0, -1, -1, -1, 1}); 
		List<Integer> exp244 = Arrays.asList(new Integer[]{0, -1, -1, 1, -1}); // leaves voices 4 and 2 free
//		List<Integer> exp250 = Arrays.asList(new Integer[]{0, -1, 1, -1, -1});
		List<Integer> exp263 = Arrays.asList(new Integer[]{0, 1, -1, -1, -1}); // leaves voices 4 and 2 free		
//		List<Integer> exp276 = Arrays.asList(new Integer[]{1, -1, -1, -1, 0});
		List<Integer> exp277 = Arrays.asList(new Integer[]{1, -1, -1, 0, -1}); // leaves voices 4 and 2 free
//		List<Integer> exp281 = Arrays.asList(new Integer[]{1, -1, 0, -1, -1});		
		List<Integer> exp291 = Arrays.asList(new Integer[]{1, 0, -1, -1, -1}); // leaves voices 4 and 2 free		 
		// Add only those voice assignments that leave voice 4 and 2 free to expected2; do not add voice assignments 
		// with more than two voice crossing pairs 
		expected2.add(exp214); // voices: (4), 1, 3, (2) / pitches: 5, 10, 20, 40 / --> 2 vcp (1-3, 1-2) 
							// sustained note (pitch 5) has voice 4, note 0 (pitch 10) has voice 1; note 1 (pitch 20) has voice 3; sustained note (pitch 40) has voice 2
		expected2.add(exp228); // voices: (4), 3, 1, (2) / pitches: 5, 10, 20, 40 / --> 1 vcp (1-2) 
							// sustained note (pitch 5) has voice 4, note 0 (pitch 10) has voice 3; note 1 (pitch 20) has voice 1; sustained note (pitch 40) has voice 2
		expected2.add(exp244); // voices: (4), 0, 3, (2) / pitches: 5, 10, 20, 40 / --> 2 vcp (0-3, 0-2) 
							// sustained note (pitch 5) has voice 4, note 0 (pitch 10) has voice 0; note 1 (pitch 20) has voice 3; sustained note (pitch 40) has voice 2
//		expected2.add(exp263); // voices: (4), 0, 1, (2) / pitches: 5, 10, 20, 40 / --> 3 vcp (0-1, 0-2, 1-2) 
//							// sustained note (pitch 5) has voice 4, note 0 (pitch 10) has voice 0; note 1 (pitch 20) has voice 1; sustained note (pitch 40) has voice 2
		expected2.add(exp277); // voices: (4), 3, 0, (2) / pitches: 5, 10, 20, 40 / --> 1 vcp (0-2) 
							// sustained note (pitch 5) has voice 4, note 0 (pitch 10) has voice 3; note 1 (pitch 20) has voice 0; sustained note (pitch 40) has voice 2
		expected2.add(exp291); // voices: (4), 1, 0, (2) / pitches: 5, 10, 20, 40 / --> 2 vcp (1-2, 0-2) 
							// sustained note (pitch 5) has voice 4, note 0 (pitch 10) has voice 1; note 1 (pitch 20) has voice 0; sustained note (pitch 40) has voice 2
		expected.addAll(expected2); 	

		// 3. For a chord of two notes where the maximum voice number is four (which gives 12 musically possible vas) 
		// and two sustained voices (which gives 2 musically possible vas): 1 (pitch 10) and 2 (pitch 20)
		// In this case, among the remaining musically possible voice assignments (two) there are voice assignments 
		// (one) with fewer than two voice crossing pairs--thus, only that one must be returned by the method
		int maxVoiceNumber3 = 4;
		int lowestNoteIndex3 = 2;
		// Make the previous chord with the sustained notes
		Integer[][] basicNotePropertiesPreviousChord3 = new Integer[2][8];
		basicNotePropertiesPreviousChord3[0] = new Integer[]{10, 0, 4, 1, 2, 0, 2, 0}; 
		basicNotePropertiesPreviousChord3[1] = new Integer[]{20, 0, 4, 1, 2, 0, 2, 1}; 
		// Make the current chord
		Integer[][] basicNotePropertiesChord3 = new Integer[2][8];
		basicNotePropertiesChord3[0] = new Integer[]{5, 1, 4, 1, 4, 1, 2, 0};
		basicNotePropertiesChord3[1] = new Integer[]{40, 1, 4, 1, 4, 1, 2, 1};
		// Make the complete basicNoteProperties
		Integer[][] basicNoteProperties3 = new Integer[4][8];
		basicNoteProperties3[0] = basicNotePropertiesPreviousChord3[0];
		basicNoteProperties3[1] = basicNotePropertiesPreviousChord3[1];
		basicNoteProperties3[2] = basicNotePropertiesChord3[0];
		basicNoteProperties3[3] = basicNotePropertiesChord3[1];
		// Make the voice labels (only those for the previous note are necessary)
		List<List<Double>> voiceLabels3 = new ArrayList<List<Double>>();
		voiceLabels3.add(v1);
		voiceLabels3.add(v2);
		// Determine the expected voice assignments and add them to expected3
		List<List<Integer>> expected3 = new ArrayList<List<Integer>>();
//		List<Integer> exp35 = Arrays.asList(new Integer[]{-1, -1, 0, 1});  
//		List<Integer> exp37 = Arrays.asList(new Integer[]{-1, -1, 1, 0}); 
//		List<Integer> exp311 = Arrays.asList(new Integer[]{-1, 0, -1, 1}); 
//		List<Integer> exp315 = Arrays.asList(new Integer[]{-1, 0, 1, -1}); 
//		List<Integer> exp319 = Arrays.asList(new Integer[]{-1, 1, -1, 0}); 
//		List<Integer> exp321 = Arrays.asList(new Integer[]{-1, 1, 0, -1}); 
		List<Integer> exp329 = Arrays.asList(new Integer[]{0, -1, -1, 1 /**/, -1}); // leaves voices 1 and 2 free
//		List<Integer> exp333 = Arrays.asList(new Integer[]{0, -1, 1, -1});  
//		List<Integer> exp345 = Arrays.asList(new Integer[]{0, 1, -1, -1}); 
		List<Integer> exp355 = Arrays.asList(new Integer[]{1, -1, -1, 0 /**/, -1}); // leaves voices 1 and 2 free
//		List<Integer> exp357 = Arrays.asList(new Integer[]{1, -1, 0, -1});
//		List<Integer> exp363 = Arrays.asList(new Integer[]{1, 0, -1, -1}); 
		// Add only those voice assignments that leave voices 1 and 2 free to expected3; do not add voice assignments 
		// with more than two voice crossing pairs
//		expected3.add(exp329); // voices: 0, (1), (2), 3 / pitches 5, 10, 20, 40 / --> 6 vcp (0-1, 0-2, 0-3, 1-2, 1-3, 2-3) 
//							// note 0 (pitch 5) has voice 0, sustained note (pitch 10) has voice 1, sustained note (pitch 20) has voice 2, note 1 (pitch 40) has voice 3
		expected3.add(exp355); // voices: 3, (1), (2), 0 / pitches 5, 10, 20, 40 / --> 1 vcp (1-2) 
							// note 0 (pitch 5) has voice 3, sustained note (pitch 10) has voice 1, sustained note (pitch 20) has voice 2, note 1 (pitch 40) has voice 0 
		expected.addAll(expected3);

		// 4. For a chord of one note where the maximum voice number is four (which gives 4 musically possible vas)
		// and three sustained voices (which gives 1 musically possible va): 2 (pitch 5), 1 (pitch 10), and 0 (pitch 20).
		// In this case, among the remaining musically possible voice assignments (one) there are no voice assignments 
		// with fewer than two voice crossing pairs--thus, all must be returned by the method
		int maxVoiceNumber4 = 4;
		int lowestNoteIndex4 = 3;
		// Make the previous chord with the sustained notes
		Integer[][] basicNotePropertiesPreviousChord4 = new Integer[3][8];
		basicNotePropertiesPreviousChord4[0] = new Integer[]{5, 0, 4, 1, 2, 0, 3, 0}; 
		basicNotePropertiesPreviousChord4[1] = new Integer[]{10, 0, 4, 1, 2, 0, 3, 1}; 
		basicNotePropertiesPreviousChord4[2] = new Integer[]{20, 0, 4, 1, 2, 0, 3, 2};
		// Make the current chord
		Integer[][] basicNotePropertiesChord4 = new Integer[1][8];
		basicNotePropertiesChord4[0] = new Integer[]{40, 1, 4, 1, 4, 1, 1, 0};		
		// Make the complete basicNoteProperties
		Integer[][] basicNoteProperties4 = new Integer[4][8];
		basicNoteProperties4[0] = basicNotePropertiesPreviousChord4[0];
		basicNoteProperties4[1] = basicNotePropertiesPreviousChord4[1];
		basicNoteProperties4[2] = basicNotePropertiesPreviousChord4[2];
		basicNoteProperties4[3] = basicNotePropertiesChord4[0];
		// Make the voice labels (only those for the previous note are necessary)
		List<List<Double>> voiceLabels4 = new ArrayList<List<Double>>();
		voiceLabels4.add(v2);
		voiceLabels4.add(v1);
		voiceLabels4.add(v0);
		// Determine the expected voice assignments and add them to expected4
		List<List<Integer>> expected4 = new ArrayList<List<Integer>>(); 
		List<Integer> exp41 = Arrays.asList(new Integer[]{-1, -1, -1, 0 /**/, -1}); // leaves voices 2, 1, and 0 free
//		List<Integer> exp42 = Arrays.asList(new Integer[]{-1, -1, 0, -1});  
//		List<Integer> exp44 = Arrays.asList(new Integer[]{-1, 0, -1, -1}); 
//		List<Integer> exp48 = Arrays.asList(new Integer[]{0, -1, -1, -1}); 
		// Add only those voice assignments that leave voices 2, 1, and 0 free to expected4; add them all
		expected4.add(exp41); // voices: (2), (1), (0), 3 / pitches 5, 10, 20, 40 / --> 3 vcp (2-3, 1-3, 0-3) 
							// sustained note (pitch 5) has voice 2, sustained note (pitch 10) has voice 1, sustained note (pitch 20) has voice 0, note 0 (pitch 40) has voice 3 
		expected.addAll(expected4);

		// 5. For a chord of two notes where the maximum voice number is four (which gives 12 musically possible vas)
		// and two sustained voices (which gives 2 musically possible vas): 2 (pitch 20), and 3 (pitch 40)
		// In this case, among the remaining musically possible voice assignments (two) there are no voice assignments 
		// with fewer than two voice crossing pairs--thus, all must be returned by the method
		int maxVoiceNumber5 = 4;
		int lowestNoteIndex5 = 2;
		// Make the previous chord with the sustained notes
		Integer[][] basicNotePropertiesPreviousChord5 = new Integer[2][8];
		basicNotePropertiesPreviousChord5[0] = new Integer[]{20, 0, 4, 1, 2, 0, 2, 0}; 
		basicNotePropertiesPreviousChord5[1] = new Integer[]{40, 0, 4, 1, 2, 0, 2, 1}; 
		// Make the current chord
		Integer[][] basicNotePropertiesChord5 = new Integer[2][8];
		basicNotePropertiesChord5[0] = new Integer[]{5, 1, 4, 1, 4, 1, 2, 0};
		basicNotePropertiesChord5[1] = new Integer[]{10, 1, 4, 1, 4, 1, 2, 1};
		// Make the complete basicNoteProperties
		Integer[][] basicNoteProperties5 = new Integer[4][8];
		basicNoteProperties5[0] = basicNotePropertiesPreviousChord5[0];
		basicNoteProperties5[1] = basicNotePropertiesPreviousChord5[1];
		basicNoteProperties5[2] = basicNotePropertiesChord5[0];
		basicNoteProperties5[3] = basicNotePropertiesChord5[1];
		// Make the voice labels (only those for the previous note are necessary)
		List<List<Double>> voiceLabels5 = new ArrayList<List<Double>>();
		voiceLabels5.add(v2);
		voiceLabels5.add(v3);
		// Determine the expected voice assignments and add them to expected5
		List<List<Integer>> expected5 = new ArrayList<List<Integer>>();
//		List<Integer> exp55 = Arrays.asList(new Integer[]{-1, -1, 0, 1});  
//		List<Integer> exp57 = Arrays.asList(new Integer[]{-1, -1, 1, 0}); 
//		List<Integer> exp511 = Arrays.asList(new Integer[]{-1, 0, -1, 1}); 
//		List<Integer> exp515 = Arrays.asList(new Integer[]{-1, 0, 1, -1});  
//		List<Integer> exp519 = Arrays.asList(new Integer[]{-1, 1, -1, 0}); 
//		List<Integer> exp521 = Arrays.asList(new Integer[]{-1, 1, 0, -1}); 
//		List<Integer> exp529 = Arrays.asList(new Integer[]{0, -1, -1, 1}); 
//		List<Integer> exp533 = Arrays.asList(new Integer[]{0, -1, 1, -1});  
		List<Integer> exp545 = Arrays.asList(new Integer[]{0, 1, -1, -1 /**/, -1}); // leaves voices 2 and 3 free
//		List<Integer> exp555 = Arrays.asList(new Integer[]{1, -1, -1, 0});
//		List<Integer> exp557 = Arrays.asList(new Integer[]{1, -1, 0, -1});
		List<Integer> exp563 = Arrays.asList(new Integer[]{1, 0, -1, -1 /**/, -1}); // leaves voices 2 and 3 free
		// Add only those voice assignments that leave voices 2 and 3 free to expected5; add them all
		expected5.add(exp545); // voices: 0, 1, (2), (3) / pitches 5, 10, 20, 40 / --> 6 vcp (0-1, 0-2, 0-3, 1-2, 1-3, 2-3) 
							// note 0 (pitch 5) has voice 0, note 1 (pitch 10) has voice 1; sustained note (pitch 20) has voice 2, sustained note (pitch 40) has voice 3
		expected5.add(exp563); // voices: 1, 0, (2), (3) / pitches 5, 10, 20, 40 / --> 5 vcp (1-2, 1-3, 0-2, 0-3, 2-3) 
							// note 0 (pitch 5) has voice 1, note 1 (pitch 10) has voice 0; sustained note (pitch 20) has voice 2, sustained note (pitch 40) has voice 3
		expected.addAll(expected5);

		// Calculate actual
		List<List<Integer>> actual = new ArrayList<List<Integer>>();
		actual.addAll(FeatureGeneratorChord.enumerateVoiceAssignmentPossibilitiesForChord(null, basicNoteProperties0,
			voiceLabels0, lowestNoteIndex0, maxVoiceNumber0, mnv)); 
		actual.addAll(FeatureGeneratorChord.enumerateVoiceAssignmentPossibilitiesForChord(null, basicNoteProperties1,
			voiceLabels1, lowestNoteIndex1, maxVoiceNumber1, mnv)); 
		actual.addAll(FeatureGeneratorChord.enumerateVoiceAssignmentPossibilitiesForChord(null, basicNoteProperties2,
			voiceLabels2, lowestNoteIndex2, maxVoiceNumber2, mnv)); 
		actual.addAll(FeatureGeneratorChord.enumerateVoiceAssignmentPossibilitiesForChord(null, basicNoteProperties3,
			voiceLabels3, lowestNoteIndex3, maxVoiceNumber3, mnv));
		actual.addAll(FeatureGeneratorChord.enumerateVoiceAssignmentPossibilitiesForChord(null, basicNoteProperties4,
			voiceLabels4, lowestNoteIndex4, maxVoiceNumber4, mnv));
		actual.addAll(FeatureGeneratorChord.enumerateVoiceAssignmentPossibilitiesForChord(null, basicNoteProperties5,
			voiceLabels5, lowestNoteIndex5, maxVoiceNumber5, mnv));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
		assertEquals(expected, actual);
	}


	@Test
	public void testGetOrderedVoiceAssignments() {
    Tablature tablature = new Tablature(encodingTestpiece1);
    Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);
    
  	// Determine expected
 	  List<List<List<Integer>>> expected = new ArrayList<List<List<Integer>>>();
 	  List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
 	  int highestNumberOfVoices = transcription.getNumberOfVoices();
    List<List<Integer>> groundTruthVoiceAssignments = 
//  		transcription.getVoiceAssignments(tablature, highestNumberOfVoices);
      transcription.getVoiceAssignments(/*tablature,*/ mnv);
 	  Integer[][] btp = tablature.getBasicTabSymbolProperties();
  	int lowestNoteIndex = 0;
 	  for (int i = 0; i < groundTruthVoiceAssignments.size(); i++) {
  		int chordSize = btp[lowestNoteIndex][Tablature.CHORD_SIZE_AS_NUM_ONSETS];
 	  	// Get the ground truth voice assignment
  	  List<Integer> currentGroundTruthVoiceAssignment = groundTruthVoiceAssignments.get(i);
  	  // Enumerate all the voice assignment possibilities
  		List<List<Integer>> currentVoiceAssignmentPossibilities = 
//  		  featureGenerator.enumerateVoiceAssignmentPossibilitiesForChord(basicTabSymbolProperties, 
//  		  null, allVoiceLabels, lowestNoteIndex, highestNumberOfVoices);
  	    FeatureGeneratorChord.enumerateVoiceAssignmentPossibilitiesForChord(btp, null, allVoiceLabels,
  	    lowestNoteIndex, highestNumberOfVoices, mnv);
  	  // Remove the ground truth voice assignment possibility
  		currentVoiceAssignmentPossibilities.remove(currentGroundTruthVoiceAssignment);
  	  // Create currentOrderedVoiceAssignmentPossibilities, add currentGroundTruthVoiceAssignment as its first
  		// element, and then add all elements of currentVoiceAssignmentPossibilities
  		List<List<Integer>> currentOrderedVoiceAssignmentPossibilities = new ArrayList<List<Integer>>();
  	  currentOrderedVoiceAssignmentPossibilities.add(currentGroundTruthVoiceAssignment); 
  	  for (List<Integer> c : currentVoiceAssignmentPossibilities) {
  	  	currentOrderedVoiceAssignmentPossibilities.add(c);
  	  }
  	  // Add currentOrderedVoiceAssignmentPossibilities to expected
  	  expected.add(currentOrderedVoiceAssignmentPossibilities);
  	  lowestNoteIndex += chordSize;
  	}
  	
  	// Calculate actual
 	  List<List<List<Integer>>> actual = FeatureGeneratorChord.getOrderedVoiceAssignments(btp, null,
 	  	allVoiceLabels, highestNumberOfVoices, mnv);
  	
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


	@Test
	public void testGetOrderedVoiceAssignmentsNonTab() {    
 	  Transcription transcription = new Transcription(midiTestpiece1);

   	List<List<Integer>> groundTruthVoiceAssignments = getVoiceAssignmentsNonTab();
   	  
  	// Determine expected
 	  List<List<List<Integer>>> expected = new ArrayList<List<List<Integer>>>();
 	  int highestNumberOfVoices = transcription.getNumberOfVoices();
 	  Integer[][] bnp = transcription.getBasicNoteProperties();
 	  List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
 	  int lowestNoteIndex = 0;
 	  for (int i = 0; i < groundTruthVoiceAssignments.size(); i++) {
  		int chordSize = bnp[lowestNoteIndex][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
  		// Get the ground truth voice assignment
  	  List<Integer> currentGroundTruthVoiceAssignment = groundTruthVoiceAssignments.get(i);
  	  // Enumerate all the voice assignment possibilities
  		List<List<Integer>> currentVoiceAssignmentPossibilities = 
//  		  featureGenerator.enumerateVoiceAssignmentPossibilitiesForChord(null, basicNoteProperties, 
//  		  allVoiceLabels, lowestNoteIndex, highestNumberOfVoices);
  		  FeatureGeneratorChord.enumerateVoiceAssignmentPossibilitiesForChord(null, bnp, 
    		allVoiceLabels, lowestNoteIndex, highestNumberOfVoices, mnv);
  	  // Remove the ground truth voice assignment possibility
  		currentVoiceAssignmentPossibilities.remove(currentGroundTruthVoiceAssignment);
  	  // Create currentOrderedVoiceAssignmentPossibilities, add currentGroundTruthVoiceAssignment as its first
  		// element, and then add all elements of currentVoiceAssignmentPossibilities
  		List<List<Integer>> currentOrderedVoiceAssignmentPossibilities = new ArrayList<List<Integer>>();
  	  currentOrderedVoiceAssignmentPossibilities.add(currentGroundTruthVoiceAssignment); 
  	  for (List<Integer> c : currentVoiceAssignmentPossibilities) {
  	  	currentOrderedVoiceAssignmentPossibilities.add(c);
  	  }
  	  // Add currentOrderedVoiceAssignmentPossibilities to expected
  	  expected.add(currentOrderedVoiceAssignmentPossibilities);
  	  lowestNoteIndex += chordSize;
  	}
  	
  	// For each chord: calculate the actual ordered voice assignment possibilities
  	List<List<List<Integer>>> actual = FeatureGeneratorChord.getOrderedVoiceAssignments(null, bnp, 
  		allVoiceLabels, highestNumberOfVoices, mnv);
  	
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


	@Test
	public void testGetVoicesWithAdjacentNoteOnSameCourse() {
		Tablature tablature = new Tablature(encodingTestpiece1);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);
		
		// Determine expected
		List<double[]> expected = new ArrayList<double[]>();
    // Chord 0
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, -1.0});
	  // Chord 1
		expected.add(new double[]{3.0, 2.0, 1.0, -1.0, -1.0});
		// Chord 2
		expected.add(new double[]{-1.0, -1.0, -1.0, 0.0, -1.0});
		// Chord 3
		expected.add(new double[]{3.0, -1.0, 2.0, 0.0, -1.0});
		// Chord 4
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, 0.0});
		// Chord 5
		expected.add(new double[]{4.0, 4.0, 2.0, 1.0, 0.0});
		// Chord 6
		expected.add(new double[]{2.0, 1.0, -1.0, -1.0, 0.0});
		// Chord 7
		expected.add(new double[]{1.0, -1.0, 0.0, -1.0, -1.0});
		// Chord 8
		expected.add(new double[]{2.0, 3.0, -1.0, -1.0, 0.0});
	  // Chord 9-14
		expected.add(new double[]{-1.0, 0.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{-1.0, 0.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{0.0, 0.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{0.0, 0.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0, -1.0});
		// Chord 15
		expected.add(new double[]{3.0, 2.0, 1.0, 0.0, 0.0});
		
		// Calculate actual
		List<double[]> actual = new ArrayList<double[]>();
    Integer[][] btp = tablature.getBasicTabSymbolProperties();
//    List<List<Double>> voiceLabels = transcription.getVoiceLabels();
//    List<List<Double>> durationLabels = transcription.getDurationLabels();
//    List<Integer[]> voicesCoDNotes=  transcription.getVoicesCoDNotes();
    int lowestNoteIndex = 0;
    for (int i = 0; i < tablature.getChords().size(); i++) {
   	  actual.add(featureGeneratorChord.getVoicesWithAdjacentNoteOnSameCourse(btp, transcription, lowestNoteIndex, mnv)); 
   	  lowestNoteIndex += tablature.getChords().get(i).size();
    }
    

		// Assert equality
  	assertEquals(expected.size(), actual.size());
  	for (int i = 0; i < expected.size(); i++) {
  		assertEquals(expected.get(i).length, actual.get(i).length);
  		for (int j = 0; j < expected.get(i).length; j++) {
  			assertEquals(expected.get(i)[j], actual.get(i)[j], delta);
  		}
  	} 
	}


	@Test
	public void testGetNoteSpecificFeaturesChord() {
		Tablature tablature = new Tablature(encodingTestpiece1);
		
	  // Determine expected 
		List<List<Double>> expected = new ArrayList<List<Double>>();
	  // Chord 0
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 50.0, 5.0, 0.0, 1/2.0, /**/ 1.0, 1.0, 57.0, 4.0, 2.0, 1/4.0, /**/
	  	2.0, 2.0, 65.0, 2.0, 1.0, 1/4.0, /**/	3.0, 3.0, 69.0, 1.0, 0.0, 1/4.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0}));
	  // Chord 1
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 6.0, 0.0, 3/16.0, /**/ 1.0, 1.0, 57.0, 4.0, 2.0, 1/4.0, /**/ 
	    2.0, 3.0, 72.0, 2.0, 8.0, 1/4.0, /**/ 3.0, 2.0, 69.0, 1.0, 0.0, 3/4.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0})); 
	  // Chord 2
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 48.0, 6.0, 3.0, 1/16.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ 
	  	-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,}));
	  // Chord 3
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 47.0, 6.0, 2.0, 1/8.0, /**/ 1.0, 1.0, 50.0, 5.0, 0.0, 1/4.0, /**/ 
	  	2.0, 2.0, 59.0, 4.0, 4.0, 1/4.0, /**/ 3.0, 3.0, 65.0, 2.0, 1.0, 1/4.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0}));
	  // Chord 4
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 6.0, 0.0, 1/8.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/
	  	-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,}));
	  // Chord 5
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 6.0, 0.0, 1/4.0, /**/ 1.0, 1.0, 57.0, 5.0, 7.0, 3/2.0, /**/ 
	  	2.0, 1.0, 57.0, 4.0, 2.0, 1/2.0, /**/ 3.0, 2.0, 60.0, 3.0, 1.0, 1/4.0, /**/ 4.0, 3.0, 69.0, 2.0, 5.0, 1/4.0}));
	  // Chord 6
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 6.0, 0.0, 1/4.0, /**/ 1.0, 1.0, 60.0, 3.0, 1.0, 1/8.0, /**/ 
	    2.0, 2.0, 64.0, 2.0, 0.0, 1/8.0, /**/ 3.0, 3.0, 69.0, 1.0, 0.0, 1/4.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,}));
	  // Chord 7
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 59.0, 3.0, 0.0, 9/8.0, /**/ 1.0, 1.0, 68.0, 2.0, 4.0, 1/8.0, /**/ 
	  	-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,}));
	  // Chord 8
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 6.0, 0.0, 3/4.0, /**/ 1.0, 1.0, 57.0, 4.0, 2.0, 3/4.0, /**/ 
	  	2.0, 2.0, 64.0, 2.0, 0.0, 1/16.0, /**/ 3.0, 3.0, 69.0, 1.0, 0.0, 1/8.0, /**/  -1.0, -1.0, -1.0, -1.0, -1.0, -1.0})); 
	  // Chords 9-14
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 68.0, 2.0, 4.0, 3/32.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ 
	  	-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0 }));
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 69.0, 1.0, 0.0, 1/8.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ 
		  -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0 }));
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 68.0, 2.0, 4.0, 1/32.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ 
		  -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0 }));
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 66.0, 2.0, 2.0, 1/32.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ 
		  -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0 })); 
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 68.0, 2.0, 4.0, 17/32.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ 
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0 })); 
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 69.0, 1.0, 0.0, 1/2.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/
	  	-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0})); 	  
	  // Chord 15
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 6.0, 0.0, 1/4.0, /**/ 1.0, 1.0, 57.0, 4.0, 2.0, 1/4.0, /**/ 
	    2.0, 2.0, 64.0, 2.0, 0.0, 1/4.0, /**/ 3.0, 3.0, 69.0, 1.0, 0.0, 1/4.0, /**/ -1.0, -1.0, -1.0, -1.0, -1.0, -1.0}));
	  	    		  
	  // Calculate actual 
	  List<List<Double>> actual = new ArrayList<List<Double>>();
	  Integer[][] btp = tablature.getBasicTabSymbolProperties();
	  List<Integer[]> meterInfo = tablature.getMeterInfo();
//	  List<Integer[]> meterInfo = tablature.getTimeline().getMeterInfoOBS();
	  int lowestNoteIndex = 0;
	  for (int i = 0; i < tablature.getChords().size(); i++) {
		 	actual.add(FeatureGeneratorChord.getNoteSpecificFeaturesChord(btp, null, 
		 		/*transcription,*/ meterInfo, lowestNoteIndex, mnv));
		 	lowestNoteIndex += tablature.getChords().get(i).size();
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


	@Test
	public void testGetNoteSpecificFeaturesChordNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1);
			
	  // Determine expected 
		List<List<Double>> expected = new ArrayList<List<Double>>();
	  // Chord 0
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 50.0, 1/4.0, /**/1.0, 1.0, 57.0, 1/4.0,
	  	2.0, 2.0, 65.0, 1/4.0, /**/ 3.0, 3.0, 69.0, 1/4.0, /**/ -1.0, -1.0, -1.0, -1.0}));
	  // Chord 1
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 3/16.0, /**/1.0, 1.0, 57.0, 1/4.0,
	    2.0, 2.0, 69.0, 1/8.0, /**/ 3.0, 3.0, 72.0, 1/4.0, /**/-1.0, -1.0, -1.0, -1.0})); 
	  // Chord 2
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 48.0, 1/16.0, /**/ -1.0, -1.0, -1.0, -1.0,
	  	-1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0, /**/ -1.0, -1.0, -1.0, -1.0}));
	  // Chord 3
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 47.0, 1/8.0, /**/ 1.0, 1.0, 50.0, 1/4.0,
	    2.0, 2.0, 59.0, 1/4.0,/**/ 3.0, 3.0, 65.0, 1/4.0, /**/ 4.0, 3.0, 65.0, 1/8.0}));
	  // Chord 4
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 1/8.0, /**/ -1.0, -1.0, -1.0, -1.0,
		  -1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0, /**/ -1.0, -1.0, -1.0, -1.0}));
	  // Chord 5
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 1/4.0, /**/ 1.0, 1.0, 57.0, 1/2.0,
	    2.0, 1.0, 57.0, 1/4.0, /**/ 3.0, 2.0, 60.0, 1/4.0, /**/ 4.0, 3.0, 69.0, 1/4.0}));
	  // Chord 6
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 1/4.0, /**/ 1.0, 2.0, 60.0, 1/8.0,
	    2.0, 3.0, 64.0, 1/8.0, /**/ 3.0, 4.0, 69.0, 1/4.0, /**/ -1.0, -1.0, -1.0, -1.0}));
	  // Chord 7
	  expected.add(Arrays.asList(new Double[]{0.0, 2.0, 59.0, 1/8.0, /**/ 1.0, 3.0, 68.0, 1/8.0,
	  	-1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0, /**/ -1.0, -1.0, -1.0, -1.0}));
	  // Chord 8
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 1/2.0, /**/ 1.0, 1.0, 57.0, 1/2.0,
	    2.0, 2.0, 64.0, 1/2.0, /**/ 3.0, 3.0, 69.0, 1/16.0, /**/ -1.0, -1.0, -1.0, -1.0})); 
	  // Chords 9-14
	  expected.add(Arrays.asList(new Double[]{0.0, 3.0, 68.0, 1/16.0, /**/ -1.0, -1.0, -1.0, -1.0,
  		-1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0}));
	  expected.add(Arrays.asList(new Double[]{0.0, 3.0, 69.0, 1/32.0, /**/ -1.0, -1.0, -1.0, -1.0,
	  	-1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0}));
	  expected.add(Arrays.asList(new Double[]{0.0, 3.0, 68.0, 1/32.0, /**/ -1.0, -1.0, -1.0, -1.0,
	  	-1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0}));
	  expected.add(Arrays.asList(new Double[]{0.0, 3.0, 66.0, 1/32.0, /**/ -1.0, -1.0, -1.0, -1.0,
	  	-1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0}));
	  expected.add(Arrays.asList(new Double[]{0.0, 3.0, 68.0, 1/32.0, /**/ -1.0, -1.0, -1.0, -1.0,
	  	-1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0}));
	  expected.add(Arrays.asList(new Double[]{0.0, 3.0, 69.0, 1/4.0, /**/ -1.0, -1.0, -1.0, -1.0,
	  	-1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0,	/**/ -1.0, -1.0, -1.0, -1.0}));	  
	  // Chord 15
	  expected.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 1/4.0, /**/ 1.0, 1.0, 57.0, 1/4.0,
	    2.0, 2.0, 64.0, 1/4.0, /**/ 3.0, 3.0, 69.0, 1/4.0, /**/ -1.0, -1.0, -1.0, -1.0}));  
	  	    		  
	  // Calculate actual 
	  List<List<Double>> actual = new ArrayList<List<Double>>();
	  Integer[][] bnp = transcription.getBasicNoteProperties();
	  List<Integer[]> meterInfo = transcription.getMeterInfo();
	  int lowestNoteIndex = 0;
	  for (int i = 0; i < transcription.getChords().size(); i++) {
		 	actual.add(FeatureGeneratorChord.getNoteSpecificFeaturesChord(null, bnp, 
		 		/*transcription,*/ meterInfo, lowestNoteIndex, mnv));
		 	lowestNoteIndex += transcription.getChords().get(i).size();
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


	@Test
	public void testGetChordLevelFeaturesChord() { 		
		Tablature tablature = new Tablature(encodingTestpiece1);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		// Determine expected
		List<List<Double>> expected = new ArrayList<List<Double>>();
		// Chord 0
		expected.add(Arrays.asList(new Double[]{4.0, 1/4.0, 0.0, 3/4.0, 7.0, 8.0, 4.0, -1.0}));
		// Chord 1
		expected.add(Arrays.asList(new Double[]{4.0, 3/16.0, 0.0, 0/4.0, 12.0, 12.0, 3.0, -1.0}));
		// Chord 2
		expected.add(Arrays.asList(new Double[]{1.0, 1/16.0, 1.0, 3/16.0, -1.0, -1.0, -1.0, -1.0}));
		// Chord 3
		expected.add(Arrays.asList(new Double[]{4.0, 1/8.0, 0.0, 1/4.0, 3.0, 9.0, 6.0, -1.0}));
		// Chord 4
		expected.add(Arrays.asList(new Double[]{1.0, 1/8.0, 0.0, 3/8.0, -1.0, -1.0, -1.0, -1.0}));
		// Chord 5
		expected.add(Arrays.asList(new Double[]{5.0, 1/4.0, 0.0, 1/2.0, 12.0, 0.0, 3.0, 9.0}));
		// Chord 6
		expected.add(Arrays.asList(new Double[]{4.0, 1/8.0, 0.0, 3/4.0, 15.0, 4.0, 5.0, -1.0}));
		// Chord 7
		expected.add(Arrays.asList(new Double[]{2.0, 1/8.0, 0.0, 7/8.0, 9.0, -1.0, -1.0, -1.0}));
		// Chord 8
		expected.add(Arrays.asList(new Double[]{4.0, 1/16.0, 0.0, 0/4.0, 12.0, 7.0, 5.0, -1.0}));
		// Chords 9-14
		expected.add(Arrays.asList(new Double[]{1.0, 1/16.0, 1.0, 1/16.0, -1.0, -1.0, -1.0, -1.0}));
		expected.add(Arrays.asList(new Double[]{1.0, 1/32.0, 1.0, 1/8.0, -1.0, -1.0, -1.0, -1.0}));
		expected.add(Arrays.asList(new Double[]{1.0, 1/32.0, 1.0, 5/32.0, -1.0, -1.0, -1.0, -1.0}));
		expected.add(Arrays.asList(new Double[]{1.0, 1/32.0, 1.0, 3/16.0, -1.0, -1.0, -1.0, -1.0}));
		expected.add(Arrays.asList(new Double[]{1.0, 1/32.0, 1.0, 7/32.0, -1.0, -1.0, -1.0, -1.0}));
		expected.add(Arrays.asList(new Double[]{1.0, 1/4.0, 0.0, 1/4.0, -1.0, -1.0, -1.0, -1.0}));
		// Chord 15
		expected.add(Arrays.asList(new Double[]{4.0, 1/4.0, 0.0, 3/4.0, 12.0, 7.0, 5.0, -1.0}));

		// Calculate actual
		List<List<Double>> actual = new ArrayList<List<Double>>();
		Integer[][] btp = tablature.getBasicTabSymbolProperties();
		List<Integer[]> meterInfo = tablature.getMeterInfo();
//		List<Integer[]> meterInfo = tablature.getTimeline().getMeterInfoOBS();
		int lowestNoteIndex = 0;
		for (int i = 0; i < tablature.getChords().size(); i++) {
			actual.add(FeatureGeneratorChord.getChordLevelFeaturesChord(btp, null,
				/*transcription,*/ meterInfo, lowestNoteIndex, mnv));
			lowestNoteIndex += tablature.getChords().get(i).size();
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


	@Test
	public void testGetChordLevelFeaturesChordNonTab() { 		
		Transcription transcription = new Transcription(midiTestpiece1);

		// Determine expected
		List<List<Double>> expected = new ArrayList<List<Double>>();
		// Chord 0
		expected.add(Arrays.asList(new Double[]{4.0, 0.0, 3/4.0, 7.0, 8.0, 4.0, -1.0}));
		// Chord 1
		expected.add(Arrays.asList(new Double[]{4.0, 0.0, 0/4.0, 12.0, 12.0, 3.0, -1.0}));
		// Chord 2
		expected.add(Arrays.asList(new Double[]{3.0, 1.0, 3/16.0, 9.0, 15.0, -1.0, -1.0}));
		// Chord 3
		expected.add(Arrays.asList(new Double[]{5.0, 0.0, 1/4.0, 3.0, 9.0, 6.0, 0.0}));
		// Chord 4
		expected.add(Arrays.asList(new Double[]{4.0, 0.0, 3/8.0, 5.0, 9.0, 6.0, -1.0}));
		// Chord 5
		expected.add(Arrays.asList(new Double[]{5.0, 0.0, 1/2.0, 12.0, 0.0, 3.0, 9.0}));
		// Chord 6
		expected.add(Arrays.asList(new Double[]{5.0, 0.0, 3/4.0, 12.0, 3.0, 4.0, 5.0}));
		// Chord 7
		expected.add(Arrays.asList(new Double[]{5.0, 0.0, 7/8.0, 12.0, 2.0, 9.0, 1.0}));
		// Chord 8
		expected.add(Arrays.asList(new Double[]{4.0, 0.0, 0/4.0, 12.0, 7.0, 5.0, -1.0}));
		// Chords 9-14
		expected.add(Arrays.asList(new Double[]{4.0, 1.0, 1/16.0, 12.0, 7.0, 4.0, -1.0}));
		expected.add(Arrays.asList(new Double[]{4.0, 1.0, 1/8.0, 12.0, 7.0, 5.0, -1.0}));
		expected.add(Arrays.asList(new Double[]{4.0, 1.0, 5/32.0, 12.0, 7.0, 4.0, -1.0}));
		expected.add(Arrays.asList(new Double[]{4.0, 1.0, 3/16.0, 12.0, 7.0, 2.0, -1.0}));
		expected.add(Arrays.asList(new Double[]{4.0, 1.0, 7/32.0, 12.0, 7.0, 4.0, -1.0}));
		expected.add(Arrays.asList(new Double[]{4.0, 0.0, 1/4.0, 12.0, 7.0, 5.0, -1.0}));
		// Chord 15
		expected.add(Arrays.asList(new Double[]{4.0, 0.0, 3/4.0, 12.0, 7.0, 5.0, -1.0}));

		// Calculate actual
		List<List<Double>> actual = new ArrayList<List<Double>>();
		Integer[][] bnp = transcription.getBasicNoteProperties();
		List<Integer[]> meterInfo = transcription.getMeterInfo();
		int lowestNoteIndex = 0;
		for (int i = 0; i < transcription.getChords().size(); i++) {
			actual.add(FeatureGeneratorChord.getChordLevelFeaturesChord(null, bnp, 
				/*transcription,*/ meterInfo, lowestNoteIndex, mnv));
			lowestNoteIndex += transcription.getChords().get(i).size();
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


	@Test
	public void testGetVoicesAlreadyOccupied() {
		Tablature tablature = new Tablature(encodingTestpiece1);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);
		
		// Determine expected
		List<List<Double>> expected = new ArrayList<List<Double>>();
		// Chord 0
		expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
	  // Chord 1
		expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
		// Chord 2
		expected.add(Arrays.asList(new Double[]{1.0, 0.0, 1.0, 0.0, 0.0}));
		// Chord 3
		expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
		// Chord 4
		expected.add(Arrays.asList(new Double[]{1.0, 0.0, 1.0, 1.0, 0.0}));
		// Chord 5
		expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
		// Chord 6
		expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 0.0}));
		// Chord 7
		expected.add(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 1.0, 1.0}));
		// Chord 8
		expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
		// Chord 9-14
		expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
		expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
		expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
		expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
		expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
		expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
		// Chord 15
		expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
		
		// Calculate actual
		List<List<Double>> actual = new ArrayList<List<Double>>();
		Integer[][] btp = tablature.getBasicTabSymbolProperties();
		List<List<Double>> durationLabels = transcription.getDurationLabels();
    List<Integer[]> voicesCoDNotes=  transcription.getVoicesSNU();
    int lowestNoteIndex = 0;
    for (int i = 0; i < tablature.getChords().size(); i++) {
 	    actual.add(FeatureGeneratorChord.getVoicesAlreadyOccupied(btp, durationLabels, voicesCoDNotes, null, 
 	    	transcription, lowestNoteIndex, mnv)); 
 	    lowestNoteIndex += tablature.getChords().get(i).size();  
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


	@Test
	public void testGetVoicesAlreadyOccupiedNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1);
		
		// Determine expected
		List<List<Double>> expected = new ArrayList<List<Double>>();
		// Chord 0
		expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
	  // Chord 1
		expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
		// Chord 2
		expected.add(Arrays.asList(new Double[]{1.0, 0.0, 1.0, 0.0, 0.0}));
		// Chord 3
		expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
		// Chord 4
		expected.add(Arrays.asList(new Double[]{1.0, 0.0, 1.0, 1.0, 0.0}));
		// Chord 5
		expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
		// Chord 6
		expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 0.0}));
		// Chord 7
		expected.add(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 1.0, 1.0}));
		// Chord 8
		expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
		// Chord 9-14
		expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
		expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
		expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
		expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
		expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
		expected.add(Arrays.asList(new Double[]{0.0, 1.0, 1.0, 1.0, 0.0}));
		// Chord 15
		expected.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
		
		// Calculate actual
		List<List<Double>> actual = new ArrayList<List<Double>>();
		Integer[][] bnp = transcription.getBasicNoteProperties();
    int lowestNoteIndex = 0;
    for (int i = 0; i < transcription.getChords().size(); i++) {
 	    actual.add(FeatureGeneratorChord.getVoicesAlreadyOccupied(null, null, null, bnp, transcription, lowestNoteIndex, mnv)); 
 	    lowestNoteIndex += transcription.getChords().get(i).size();  
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


	@Test
	public void testGenerateChordFeatureVector() {
		Tablature tablature = new Tablature(encodingTestpiece1);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
		Integer[][] btp = tablature.getBasicTabSymbolProperties();

		List<List<Double>> expected = new ArrayList<List<Double>>();
		// Chord 1
		expected.add(Arrays.asList(new Double[]{
			0.0, 0.0, 45.0, 6.0, 0.0, 3/16.0, // note-specific 
			1.0, 1.0, 57.0, 4.0, 2.0, 1/4.0,
			2.0, 3.0, 72.0, 2.0, 8.0, 1/4.0, 
			3.0, 2.0, 69.0, 1.0, 0.0, 3/4.0,
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0,
			4.0, 3/16.0, 0.0, 0/4.0, 12.0, 12.0, 3.0, -1.0, // chord-level
			3.0, 2.0, 1.0, -1.0, -1.0, // voices with adjacent note on same course
			1.0/(3+1), 1.0/(4+1), 1.0/(0+1), 1.0/(5+1), -1.0,   // proximities and movements
			1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), -1.0,
			1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0,
			3.0, 4.0, 0.0, -5.0, 0.0,
			0.0, 0.0, 0.0, 0.0, 0.0, // voices already occupied
			FeatureGeneratorChord.getPitchVoiceRelationInChord(btp, null, allVoiceLabels, 4, 
				getVoiceAssignments().get(1), mnv), // pitch-voice relation
			0.0, 0.0, 0.0, // voice crossing info
			2.0, 3.0, 1.0, 0.0, -1.0 // voice assignment vector
		}));
		// Chord 7
		expected.add(Arrays.asList(new Double[]{
			0.0, 0.0, 59.0, 3.0, 0.0, 9/8.0, // note-specific 
			1.0, 1.0, 68.0, 2.0, 4.0, 1/8.0,
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0,
			-1.0, -1.0, -1.0, -1.0, -1.0, -1.0,
			2.0, 1/8.0, 0.0, 7/8.0, 9.0, -1.0, -1.0, -1.0, // chord-level
			1.0, -1.0, 0.0, -1.0, -1.0, // voices with adjacent note on same course
			1.0/(4+1), -1.0, 1.0/(1+1), -1.0, -1.0,   // proximities and movements
			1.0/((1/8.0)+1), -1.0, 1.0/((1/8.0)+1), -1.0, -1.0,
			1.0/(0+1), -1.0, 1.0/(0+1), -1.0, -1.0,
			4.0, 0.0, -1.0, 0.0, 0.0,
			0.0, 0.0, 0.0, 0.0, 0.0, // voices already occupied
			FeatureGeneratorChord.getPitchVoiceRelationInChord(btp, null, allVoiceLabels, 23, 
				getVoiceAssignments().get(7), mnv), // pitch-voice relation
			0.0, 0.0, 0.0, // voice crossing info
			1.0, -1.0, 0.0, -1.0, -1.0 // voice assignment vector
		}));

		List<List<Double>> actual = new ArrayList<List<Double>>();
		List<Integer[]> meterInfo = tablature.getMeterInfo(); 
		actual.add(featureGeneratorChord.generateChordFeatureVector(btp, null, transcription, meterInfo, 4,
			getVoiceAssignments().get(1), mnv));
		actual.add(featureGeneratorChord.generateChordFeatureVector(btp, null, transcription, meterInfo, 23,
			getVoiceAssignments().get(7), mnv));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j= 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
		assertEquals(expected, actual);
	}


	@Test
	public void testGenerateChordFeatureVectorNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1);
		
		List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
		Integer[][] bnp = transcription.getBasicNoteProperties();
		
		// Determine expected
		List<List<Double>> expected = new ArrayList<List<Double>>();
		// Chord 1
		expected.add(Arrays.asList(new Double[]{
			0.0, 0.0, 45.0, 3/16.0, // note-specific 
			1.0, 1.0, 57.0, 1/4.0,
			2.0, 2.0, 69.0, 1/8.0,
			3.0, 3.0, 72.0, 1/4.0,
			-1.0, -1.0, -1.0, -1.0,
			4.0, 0.0, 0/4.0, 12.0, 12.0, 3.0, -1.0, // chord-level
			1.0/(3+1), 1.0/(4+1), 1.0/(0+1), 1.0/(5+1), -1.0, // proximities and movements
			1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), -1.0,
			1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0,
			3.0, 4.0, 0.0, -5.0, 0.0, 
			0.0, 0.0, 0.0, 0.0, 0.0, // voices already occupied
			FeatureGeneratorChord.getPitchVoiceRelationInChord(null, bnp, allVoiceLabels, 4, 
				getVoiceAssignmentsNonTab().get(1), mnv), // pitch-voice relation
			0.0, 0.0, 0.0, // voice crossing info
			3.0, 2.0, 1.0, 0.0, -1.0 // voice assignment vector
		}));
		// Chord 7
		expected.add(Arrays.asList(new Double[]{
			0.0, 2.0, 59.0, 1/8.0, // note-specific 
			1.0, 3.0, 68.0, 1/8.0, 
			-1.0, -1.0, -1.0, -1.0, 
			-1.0, -1.0, -1.0, -1.0,
			-1.0, -1.0, -1.0, -1.0,
			5.0, 0.0, 7/8.0, 12.0, 2.0, 9.0, 1.0, // chord-level
			1.0/(4+1), -1.0, 1.0/(1+1), -1.0, -1.0, // proximities and movements
			1.0/((1/8.0)+1), -1.0, 1.0/((1/8.0)+1), -1.0, -1.0,
			1.0/(0+1), -1.0, 1.0/(0+1), -1.0, -1.0,
			4.0, 0.0, -1.0, 0.0, 0.0, 
			0.0, 1.0, 0.0, 1.0, 1.0, // voices already occupied
			FeatureGeneratorChord.getPitchVoiceRelationInChord(null, bnp, allVoiceLabels, 24, 
				getVoiceAssignments().get(7), mnv), // pitch-voice relation
			1.0, 1.0, 1.0, // voice crossing info
			1.0, -1.0, 0.0, -1.0, -1.0 // voice assignment vector
		}));
		
		// Calculate actual
		List<List<Double>> actual = new ArrayList<List<Double>>();
		List<Integer[]> meterInfo = transcription.getMeterInfo(); 
		actual.add(featureGeneratorChord.generateChordFeatureVector(null, bnp, transcription, meterInfo, 4,
			getVoiceAssignmentsNonTab().get(1), mnv));
		actual.add(featureGeneratorChord.generateChordFeatureVector(null, bnp, transcription, meterInfo, 24,
			getVoiceAssignmentsNonTab().get(7), mnv));
		
		// Assert equality
		assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
    	assertEquals(expected.get(i).size(), actual.get(i).size());
    	for (int j= 0; j < expected.get(i).size(); j++) {
    		assertEquals(expected.get(i).get(j), actual.get(i).get(j));
    	}
    }
    assertEquals(expected, actual);
	}


	@Test
	public void testGenerateChordFeatureVectorDISS() {
		Tablature tablature = new Tablature(encodingTestpiece1);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
		Integer[][] btp = tablature.getBasicTabSymbolProperties();

		// Determine expected
		List<List<Double>> expected = new ArrayList<List<Double>>();
		// Chord 1
		expected.add(Arrays.asList(new Double[]{
			0.0, 45.0, 6.0, 0.0, 3/16.0, // note-level 
			1.0, 57.0, 4.0, 2.0, 1/4.0,
			3.0, 72.0, 2.0, 8.0, 1/4.0, 
			2.0, 69.0, 1.0, 0.0, 3/4.0,
			-1.0, -1.0, -1.0, -1.0, -1.0,
			4.0, 3/16.0, 0/4.0, 12.0, 12.0, 3.0, -1.0, // chord-level
//			3.0, 2.0, 1.0, -1.0, -1.0, // voices with adjacent note on same course
			1.0/(3+1), 1.0/(4+1), 1.0/(0+1), 1.0/(5+1), -1.0,   // proximities and movements
			1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), -1.0,
			1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0,
			3.0, 4.0, 0.0, -5.0, 0.0,
			0.0, 0.0, 0.0, 0.0, 0.0, // voices already occupied
			FeatureGeneratorChord.getPitchVoiceRelationInChord(btp, null, allVoiceLabels, 4, 
				getVoiceAssignments().get(1), mnv), // pitch-voice relation
			0.0, 0.0, 0.0, // voice crossing info
			2.0, 3.0, 1.0, 0.0, -1.0 // voice assignment vector
		}));
		// Chord 7
		expected.add(Arrays.asList(new Double[]{
			0.0, 59.0, 3.0, 0.0, 9/8.0, // note-level 
			1.0, 68.0, 2.0, 4.0, 1/8.0,
			-1.0, -1.0, -1.0, -1.0, -1.0, 
			-1.0, -1.0, -1.0, -1.0, -1.0,
			-1.0, -1.0, -1.0, -1.0, -1.0,
			2.0, 1/8.0, 7/8.0, 9.0, -1.0, -1.0, -1.0, // chord-level
//			1.0, -1.0, 0.0, -1.0, -1.0, // voices with adjacent note on same course
			1.0/(4+1), -1.0, 1.0/(1+1), -1.0, -1.0,   // proximities and movements
			1.0/((1/8.0)+1), -1.0, 1.0/((1/8.0)+1), -1.0, -1.0,
			1.0/(0+1), -1.0, 1.0/(0+1), -1.0, -1.0,
			4.0, 0.0, -1.0, 0.0, 0.0,
			0.0, 0.0, 0.0, 0.0, 0.0, // voices already occupied
			FeatureGeneratorChord.getPitchVoiceRelationInChord(btp, null, allVoiceLabels, 23, 
				getVoiceAssignments().get(7), mnv), // pitch-voice relation
			0.0, 0.0, 0.0, // voice crossing info
			1.0, -1.0, 0.0, -1.0, -1.0 // voice assignment vector
		}));

		// Calculate actual
		List<List<Double>> actual = new ArrayList<List<Double>>();
		List<Integer[]> meterInfo = tablature.getMeterInfo(); 
//		List<Integer[]> meterInfo = tablature.getTimeline().getMeterInfoOBS(); 
		actual.add(FeatureGeneratorChord.generateChordFeatureVectorDISS(btp, null, transcription, meterInfo, 4,
			getVoiceAssignments().get(1), mnv));
		actual.add(FeatureGeneratorChord.generateChordFeatureVectorDISS(btp, null, transcription, meterInfo, 23,
			getVoiceAssignments().get(7), mnv));
		
		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
				for (int j= 0; j < expected.get(i).size(); j++) {
					assertEquals(expected.get(i).get(j), actual.get(i).get(j));
				}
		}
		assertEquals(expected, actual);
	}


	@Test
	public void testGenerateChordFeatureVectorDISSNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1);

		List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
		Integer[][] bnp = transcription.getBasicNoteProperties();

		// Determine expected
		List<List<Double>> expected = new ArrayList<List<Double>>();
		// Chord 1
		expected.add(Arrays.asList(new Double[]{
			0.0, 45.0, 3/16.0, // note-level 
			1.0, 57.0, 1/4.0,
			2.0, 69.0, 1/8.0,
			3.0, 72.0, 1/4.0,
			-1.0, -1.0, -1.0,
			4.0, 0/4.0, 12.0, 12.0, 3.0, -1.0, // chord-level
			1.0/(3+1), 1.0/(4+1), 1.0/(0+1), 1.0/(5+1), -1.0, // proximities and movements
			1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), 1.0/((1/4.0)+1), -1.0,
			1.0/(0+1), 1.0/(0+1), 1.0/(0+1), 1.0/(0+1), -1.0,
			3.0, 4.0, 0.0, -5.0, 0.0, 
			0.0, 0.0, 0.0, 0.0, 0.0, // voices already occupied
			FeatureGeneratorChord.getPitchVoiceRelationInChord(null, bnp, allVoiceLabels, 4, 
				getVoiceAssignmentsNonTab().get(1), mnv), // pitch-voice relation
			0.0, 0.0, 0.0, // voice crossing info
			3.0, 2.0, 1.0, 0.0, -1.0 // voice assignment vector
		}));
		// Chord 7
		expected.add(Arrays.asList(new Double[]{
			2.0, 59.0, 1/8.0, // note-specific 
			3.0, 68.0, 1/8.0, 
			-1.0, -1.0, -1.0, 
			-1.0, -1.0, -1.0,
			-1.0, -1.0, -1.0,
			5.0, 7/8.0, 12.0, 2.0, 9.0, 1.0, // chord-level
			1.0/(4+1), -1.0, 1.0/(1+1), -1.0, -1.0, // proximities and movements
			1.0/((1/8.0)+1), -1.0, 1.0/((1/8.0)+1), -1.0, -1.0,
			1.0/(0+1), -1.0, 1.0/(0+1), -1.0, -1.0,
			4.0, 0.0, -1.0, 0.0, 0.0, 
			0.0, 1.0, 0.0, 1.0, 1.0, // voices already occupied
			FeatureGeneratorChord.getPitchVoiceRelationInChord(null, bnp, allVoiceLabels, 24, 
				getVoiceAssignments().get(7), mnv), // pitch-voice relation
			1.0, 1.0, 1.0, // voice crossing info
			1.0, -1.0, 0.0, -1.0, -1.0 // voice assignment vector
		}));

		// Calculate actual
		List<List<Double>> actual = new ArrayList<List<Double>>();
		List<Integer[]> meterInfo = transcription.getMeterInfo(); 
		actual.add(FeatureGeneratorChord.generateChordFeatureVectorDISS(null, bnp, transcription, meterInfo, 4,
			getVoiceAssignmentsNonTab().get(1), mnv));
		actual.add(FeatureGeneratorChord.generateChordFeatureVectorDISS(null, bnp, transcription, meterInfo, 24,
			getVoiceAssignmentsNonTab().get(7), mnv));
		
		// Assert equality
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j= 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
		assertEquals(expected, actual);
	}


	// MuSci
	@Test
	public void testGetIndividualNoteFeaturesChord() {
		Tablature tablature = new Tablature(encodingTestpiece1);
			
	  // Determine expected 
		// a. Including tablature information
		List<List<List<Double>>> expected = new ArrayList<List<List<Double>>>();
		List<List<List<Double>>> expectedWithTabInfo = new ArrayList<List<List<Double>>>();
	  List<Double> missingOnsetFeatures = Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0});
	  // Chord 0
	  List<List<Double>> withTabInfo0 = new ArrayList<List<Double>>();
	  withTabInfo0.add(Arrays.asList(new Double[]{0.0, 50.0, 5.0, 0.0, 1/2.0}));
	  withTabInfo0.add(Arrays.asList(new Double[]{1.0, 57.0, 4.0, 2.0, 1/4.0}));
	  withTabInfo0.add(Arrays.asList(new Double[]{2.0, 65.0, 2.0, 1.0, 1/4.0}));
	  withTabInfo0.add(Arrays.asList(new Double[]{3.0, 69.0, 1.0, 0.0, 1/4.0}));
	  withTabInfo0.add(missingOnsetFeatures);
	  // Chord 1
	  List<List<Double>> withTabInfo1 = new ArrayList<List<Double>>();
	  withTabInfo1.add(Arrays.asList(new Double[]{0.0, 45.0, 6.0, 0.0, 3/16.0}));
	  withTabInfo1.add(Arrays.asList(new Double[]{1.0, 57.0, 4.0, 2.0, 1/4.0}));
	  withTabInfo1.add(Arrays.asList(new Double[]{2.0, 72.0, 2.0, 8.0, 1/4.0}));
	  withTabInfo1.add(Arrays.asList(new Double[]{3.0, 69.0, 1.0, 0.0, 3/4.0})); 
	  withTabInfo1.add(missingOnsetFeatures);
	  // Chord 2
	  List<List<Double>> withTabInfo2 = new ArrayList<List<Double>>();
	  withTabInfo2.add(Arrays.asList(new Double[]{0.0, 48.0, 6.0, 3.0, 1/16.0}));
	  withTabInfo2.add(missingOnsetFeatures); withTabInfo2.add(missingOnsetFeatures);
	  withTabInfo2.add(missingOnsetFeatures); withTabInfo2.add(missingOnsetFeatures);
	  // Chord 3
	  List<List<Double>> withTabInfo3 = new ArrayList<List<Double>>();
	  withTabInfo3.add(Arrays.asList(new Double[]{0.0, 47.0, 6.0, 2.0, 1/8.0}));
	  withTabInfo3.add(Arrays.asList(new Double[]{1.0, 50.0, 5.0, 0.0, 1/4.0}));
	  withTabInfo3.add(Arrays.asList(new Double[]{2.0, 59.0, 4.0, 4.0, 1/4.0}));
	  withTabInfo3.add(Arrays.asList(new Double[]{3.0, 65.0, 2.0, 1.0, 1/4.0}));
	  withTabInfo3.add(missingOnsetFeatures);
	  // Chord 4
	  List<List<Double>> withTabInfo4 = new ArrayList<List<Double>>();
	  withTabInfo4.add(Arrays.asList(new Double[]{0.0, 45.0, 6.0, 0.0, 1/8.0}));
	  withTabInfo4.add(missingOnsetFeatures); withTabInfo4.add(missingOnsetFeatures);
	  withTabInfo4.add(missingOnsetFeatures); withTabInfo4.add(missingOnsetFeatures);
	  // Chord 5
	  List<List<Double>> withTabInfo5 = new ArrayList<List<Double>>();
	  withTabInfo5.add(Arrays.asList(new Double[]{0.0, 45.0, 6.0, 0.0, 1/4.0}));
	  withTabInfo5.add(Arrays.asList(new Double[]{1.0, 57.0, 5.0, 7.0, 3/2.0}));
	  withTabInfo5.add(Arrays.asList(new Double[]{2.0, 57.0, 4.0, 2.0, 1/2.0}));
	  withTabInfo5.add(Arrays.asList(new Double[]{3.0, 60.0, 3.0, 1.0, 1/4.0}));
	  withTabInfo5.add(Arrays.asList(new Double[]{4.0, 69.0, 2.0, 5.0, 1/4.0}));
	  // Chord 6
	  List<List<Double>> withTabInfo6 = new ArrayList<List<Double>>();
	  withTabInfo6.add(Arrays.asList(new Double[]{0.0, 45.0, 6.0, 0.0, 1/4.0}));
	  withTabInfo6.add(Arrays.asList(new Double[]{1.0, 60.0, 3.0, 1.0, 1/8.0}));
	  withTabInfo6.add(Arrays.asList(new Double[]{2.0, 64.0, 2.0, 0.0, 1/8.0}));
	  withTabInfo6.add(Arrays.asList(new Double[]{3.0, 69.0, 1.0, 0.0, 1/4.0}));
	  withTabInfo6.add(missingOnsetFeatures);
	  // Chord 7
	  List<List<Double>> withTabInfo7 = new ArrayList<List<Double>>();
	  withTabInfo7.add(Arrays.asList(new Double[]{0.0, 59.0, 3.0, 0.0, 9/8.0}));
	  withTabInfo7.add(Arrays.asList(new Double[]{1.0, 68.0, 2.0, 4.0, 1/8.0}));
	  withTabInfo7.add(missingOnsetFeatures); withTabInfo7.add(missingOnsetFeatures);
	  withTabInfo7.add(missingOnsetFeatures);
	  // Chord 8
	  List<List<Double>> withTabInfo8 = new ArrayList<List<Double>>();
	  withTabInfo8.add(Arrays.asList(new Double[]{0.0, 45.0, 6.0, 0.0, 3/4.0}));
	  withTabInfo8.add(Arrays.asList(new Double[]{1.0, 57.0, 4.0, 2.0, 3/4.0}));
	  withTabInfo8.add(Arrays.asList(new Double[]{2.0, 64.0, 2.0, 0.0, 1/16.0}));
	  withTabInfo8.add(Arrays.asList(new Double[]{3.0, 69.0, 1.0, 0.0, 1/8.0}));
	  withTabInfo8.add(missingOnsetFeatures);
	  // Chords 9-14
	  List<List<Double>> withTabInfo9 = new ArrayList<List<Double>>();
	  withTabInfo9.add(Arrays.asList(new Double[]{0.0, 68.0, 2.0, 4.0, 3/32.0}));
	  withTabInfo9.add(missingOnsetFeatures); withTabInfo9.add(missingOnsetFeatures);
	  withTabInfo9.add(missingOnsetFeatures); withTabInfo9.add(missingOnsetFeatures);
	  List<List<Double>> withTabInfo10 = new ArrayList<List<Double>>();
	  withTabInfo10.add(Arrays.asList(new Double[]{0.0, 69.0, 1.0, 0.0, 1/8.0}));
	  withTabInfo10.add(missingOnsetFeatures); withTabInfo10.add(missingOnsetFeatures);
	  withTabInfo10.add(missingOnsetFeatures); withTabInfo10.add(missingOnsetFeatures);
	  List<List<Double>> withTabInfo11 = new ArrayList<List<Double>>();
	  withTabInfo11.add(Arrays.asList(new Double[]{0.0, 68.0, 2.0, 4.0, 1/32.0}));
	  withTabInfo11.add(missingOnsetFeatures); withTabInfo11.add(missingOnsetFeatures);
	  withTabInfo11.add(missingOnsetFeatures); withTabInfo11.add(missingOnsetFeatures);
	  List<List<Double>> withTabInfo12 = new ArrayList<List<Double>>();
	  withTabInfo12.add(Arrays.asList(new Double[]{0.0, 66.0, 2.0, 2.0, 1/32.0}));
	  withTabInfo12.add(missingOnsetFeatures); withTabInfo12.add(missingOnsetFeatures);
	  withTabInfo12.add(missingOnsetFeatures); withTabInfo12.add(missingOnsetFeatures);
	  List<List<Double>> withTabInfo13 = new ArrayList<List<Double>>();
	  withTabInfo13.add(Arrays.asList(new Double[]{0.0, 68.0, 2.0, 4.0, 17/32.0}));
	  withTabInfo13.add(missingOnsetFeatures); withTabInfo13.add(missingOnsetFeatures);
	  withTabInfo13.add(missingOnsetFeatures); withTabInfo13.add(missingOnsetFeatures);
	  List<List<Double>> withTabInfo14 = new ArrayList<List<Double>>();
	  withTabInfo14.add(Arrays.asList(new Double[]{0.0, 69.0, 1.0, 0.0, 1/2.0}));
	  withTabInfo14.add(missingOnsetFeatures); withTabInfo14.add(missingOnsetFeatures);
	  withTabInfo14.add(missingOnsetFeatures); withTabInfo14.add(missingOnsetFeatures);
	  // Chord 15
	  List<List<Double>> withTabInfo15 = new ArrayList<List<Double>>();
	  withTabInfo15.add(Arrays.asList(new Double[]{0.0, 45.0, 6.0, 0.0, 1/4.0}));
	  withTabInfo15.add(Arrays.asList(new Double[]{1.0, 57.0, 4.0, 2.0, 1/4.0}));
	  withTabInfo15.add(Arrays.asList(new Double[]{2.0, 64.0, 2.0, 0.0, 1/4.0}));
	  withTabInfo15.add(Arrays.asList(new Double[]{3.0, 69.0, 1.0, 0.0, 1/4.0}));
	  withTabInfo15.add(missingOnsetFeatures);
	  	    	
	  expectedWithTabInfo.add(withTabInfo0); expectedWithTabInfo.add(withTabInfo1); expectedWithTabInfo.add(withTabInfo2); 
	  expectedWithTabInfo.add(withTabInfo3); expectedWithTabInfo.add(withTabInfo4); expectedWithTabInfo.add(withTabInfo5); 
	  expectedWithTabInfo.add(withTabInfo6); expectedWithTabInfo.add(withTabInfo7); expectedWithTabInfo.add(withTabInfo8);
	  expectedWithTabInfo.add(withTabInfo9); expectedWithTabInfo.add(withTabInfo10); expectedWithTabInfo.add(withTabInfo11);
	  expectedWithTabInfo.add(withTabInfo12); expectedWithTabInfo.add(withTabInfo13); expectedWithTabInfo.add(withTabInfo14);
	  expectedWithTabInfo.add(withTabInfo15);
	    	
	  // b. Not including tablature information
	  List<List<List<Double>>> expectedNoTabInfo = new ArrayList<List<List<Double>>>();
	  for (List<List<Double>> currentChordWithTabInfo : expectedWithTabInfo) {
	  	List<List<Double>> currentChordNoTabInfo = new ArrayList<List<Double>>();
	  	for (List<Double> currentNoteWithTabInfo : currentChordWithTabInfo) {
	  	  currentChordNoTabInfo.add(new ArrayList<Double>(currentNoteWithTabInfo.subList(0, 2)));
	  	}
	  	expectedNoTabInfo.add(currentChordNoTabInfo);
	  }
	  
	  expected.addAll(expectedWithTabInfo); expected.addAll(expectedNoTabInfo);
	  
	  // Calculate actual 
	  List<List<List<Double>>> actual = new ArrayList<List<List<Double>>>();
	  int largestChordSizeTraining = tablature.getLargestTablatureChord();
	  Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
	  int lowestNoteIndex = 0;
		// a. Including tablature information
	  for (int i = 0; i < tablature.getChords().size(); i++) {
		 	actual.add(featureGeneratorChord.getIndividualNoteFeaturesChord(basicTabSymbolProperties, null,
		 		largestChordSizeTraining, lowestNoteIndex, true));
		 	lowestNoteIndex += tablature.getChords().get(i).size();
		}
	  // b. Not including tablature information
	  lowestNoteIndex = 0;
		for (int i = 0; i < tablature.getChords().size(); i++) {
			 actual.add(featureGeneratorChord.getIndividualNoteFeaturesChord(basicTabSymbolProperties, null, 
					largestChordSizeTraining, lowestNoteIndex, false));
		 	lowestNoteIndex += tablature.getChords().get(i).size();
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
		assertEquals(expected, actual);
	}


	@Test
	public void testGetIndividualNoteFeaturesChordNonTab() {		
	  Transcription transcription = new Transcription(midiTestpiece1);

	  // Determine expected
		List<List<List<Double>>> expected = new ArrayList<List<List<Double>>>();
	  List<Double> missingOnsetFeatures = Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0});
	  // Chord 0
	  List<List<Double>> chord0 = new ArrayList<List<Double>>();
	  chord0.add(Arrays.asList(new Double[]{0.0, 0.0, 50.0, 1/4.0}));
	  chord0.add(Arrays.asList(new Double[]{1.0, 1.0, 57.0, 1/4.0}));
	  chord0.add(Arrays.asList(new Double[]{2.0, 2.0, 65.0, 1/4.0}));
	  chord0.add(Arrays.asList(new Double[]{3.0, 3.0, 69.0, 1/4.0})); 
	  chord0.add(missingOnsetFeatures);
	  // Chord 1
	  List<List<Double>> chord1 = new ArrayList<List<Double>>();
	  chord1.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 3/16.0}));
	  chord1.add(Arrays.asList(new Double[]{1.0, 1.0, 57.0, 1/4.0}));
	  chord1.add(Arrays.asList(new Double[]{2.0, 2.0, 69.0, 1/8.0}));
	  chord1.add(Arrays.asList(new Double[]{3.0, 3.0, 72.0, 1/4.0})); 
	  chord1.add(missingOnsetFeatures);
	  // Chord 2
	  List<List<Double>> chord2 = new ArrayList<List<Double>>();
	  chord2.add(Arrays.asList(new Double[]{0.0, 0.0, 48.0, 1/16.0}));
	  chord2.add(missingOnsetFeatures); chord2.add(missingOnsetFeatures);
	  chord2.add(missingOnsetFeatures); chord2.add(missingOnsetFeatures);
	  // Chord 3
	  List<List<Double>> chord3 = new ArrayList<List<Double>>();
	  chord3.add(Arrays.asList(new Double[]{0.0, 0.0, 47.0, 1/8.0}));
	  chord3.add(Arrays.asList(new Double[]{1.0, 1.0, 50.0, 1/4.0}));
	  chord3.add(Arrays.asList(new Double[]{2.0, 2.0, 59.0, 1/4.0}));
	  chord3.add(Arrays.asList(new Double[]{3.0, 3.0, 65.0, 1/4.0}));
	  chord3.add(Arrays.asList(new Double[]{4.0, 4.0, 65.0, 1/8.0}));
	  // Chord 4
	  List<List<Double>> chord4 = new ArrayList<List<Double>>();
	  chord4.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 1/8.0}));
	  chord4.add(missingOnsetFeatures); chord4.add(missingOnsetFeatures);
	  chord4.add(missingOnsetFeatures); chord4.add(missingOnsetFeatures);
	  // Chord 5
	  List<List<Double>> chord5 = new ArrayList<List<Double>>();
	  chord5.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 1/4.0}));
	  chord5.add(Arrays.asList(new Double[]{1.0, 1.0, 57.0, 1/2.0}));
	  chord5.add(Arrays.asList(new Double[]{2.0, 2.0, 57.0, 1/4.0}));
	  chord5.add(Arrays.asList(new Double[]{3.0, 3.0, 60.0, 1/4.0}));
	  chord5.add(Arrays.asList(new Double[]{4.0, 4.0, 69.0, 1/4.0}));
	  // Chord 6
	  List<List<Double>> chord6 = new ArrayList<List<Double>>();
	  chord6.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 1/4.0}));
	  chord6.add(Arrays.asList(new Double[]{1.0, 2.0, 60.0, 1/8.0}));
	  chord6.add(Arrays.asList(new Double[]{2.0, 3.0, 64.0, 1/8.0}));
	  chord6.add(Arrays.asList(new Double[]{3.0, 4.0, 69.0, 1/4.0}));
	  chord6.add(missingOnsetFeatures);
	  // Chord 7
	  List<List<Double>> chord7 = new ArrayList<List<Double>>();
	  chord7.add(Arrays.asList(new Double[]{0.0, 2.0, 59.0, 1/8.0}));
	  chord7.add(Arrays.asList(new Double[]{1.0, 3.0, 68.0, 1/8.0}));
	  chord7.add(missingOnsetFeatures);
	  chord7.add(missingOnsetFeatures);
	  chord7.add(missingOnsetFeatures);
	  // Chord 8
	  List<List<Double>> chord8 = new ArrayList<List<Double>>();
	  chord8.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 1/2.0}));
	  chord8.add(Arrays.asList(new Double[]{1.0, 1.0, 57.0, 1/2.0}));
	  chord8.add(Arrays.asList(new Double[]{2.0, 2.0, 64.0, 1/2.0}));
	  chord8.add(Arrays.asList(new Double[]{3.0, 3.0, 69.0, 1/16.0}));
    chord8.add(missingOnsetFeatures);
    // Chords 9-14
    List<List<Double>> chord9 = new ArrayList<List<Double>>();
    chord9.add(Arrays.asList(new Double[]{0.0, 3.0, 68.0, 1/16.0}));
    chord9.add(missingOnsetFeatures); chord9.add(missingOnsetFeatures);
    chord9.add(missingOnsetFeatures); chord9.add(missingOnsetFeatures);
    List<List<Double>> chord10 = new ArrayList<List<Double>>();
    chord10.add(Arrays.asList(new Double[]{0.0, 3.0, 69.0, 1/32.0}));
    chord10.add(missingOnsetFeatures); chord10.add(missingOnsetFeatures);
    chord10.add(missingOnsetFeatures); chord10.add(missingOnsetFeatures);
    List<List<Double>> chord11 = new ArrayList<List<Double>>();
    chord11.add(Arrays.asList(new Double[]{0.0, 3.0, 68.0, 1/32.0}));
    chord11.add(missingOnsetFeatures); chord11.add(missingOnsetFeatures);
    chord11.add(missingOnsetFeatures); chord11.add(missingOnsetFeatures);
    List<List<Double>> chord12 = new ArrayList<List<Double>>();
    chord12.add(Arrays.asList(new Double[]{0.0, 3.0, 66.0, 1/32.0}));
    chord12.add(missingOnsetFeatures); chord12.add(missingOnsetFeatures);
    chord12.add(missingOnsetFeatures); chord12.add(missingOnsetFeatures);
    List<List<Double>> chord13 = new ArrayList<List<Double>>();
    chord13.add(Arrays.asList(new Double[]{0.0, 3.0, 68.0, 1/32.0}));
    chord13.add(missingOnsetFeatures); chord13.add(missingOnsetFeatures);
    chord13.add(missingOnsetFeatures); chord13.add(missingOnsetFeatures);
    List<List<Double>> chord14 = new ArrayList<List<Double>>();
    chord14.add(Arrays.asList(new Double[]{0.0, 3.0, 69.0, 1/4.0}));
    chord14.add(missingOnsetFeatures); chord14.add(missingOnsetFeatures);
    chord14.add(missingOnsetFeatures); chord14.add(missingOnsetFeatures);
    // Chord 15
    List<List<Double>> chord15 = new ArrayList<List<Double>>();
    chord15.add(Arrays.asList(new Double[]{0.0, 0.0, 45.0, 1/4.0}));
    chord15.add(Arrays.asList(new Double[]{1.0, 1.0, 57.0, 1/4.0}));
    chord15.add(Arrays.asList(new Double[]{2.0, 2.0, 64.0, 1/4.0}));
    chord15.add(Arrays.asList(new Double[]{3.0, 3.0, 69.0, 1/4.0}));
	  chord15.add(missingOnsetFeatures);
    	
	  expected.add(chord0); expected.add(chord1); expected.add(chord2); expected.add(chord3); expected.add(chord4);
	  expected.add(chord5); expected.add(chord6); expected.add(chord7); expected.add(chord8); expected.add(chord9);
	  expected.add(chord10); expected.add(chord11); expected.add(chord12); expected.add(chord13); expected.add(chord14);
	  expected.add(chord15);
    	
 	  // For each chord: calculate the actual individual note features
    List<List<List<Double>>> actual = new ArrayList<List<List<Double>>>();
	  int largestChordSizeTraining = transcription.getLargestTranscriptionChord();
	  Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
	  int lowestNoteIndex = 0;
		for (int i = 0; i < transcription.getChords().size(); i++) {
  	 	actual.add(featureGeneratorChord.getIndividualNoteFeaturesChord(null, basicNoteProperties, 
  	 		largestChordSizeTraining, lowestNoteIndex, false));
  	 	lowestNoteIndex += transcription.getChords().get(i).size();
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
		assertEquals(expected, actual);
 	}


	@Test
	public void testGetSharedNoteFeaturesChord() { 		
    Tablature tablature = new Tablature(encodingTestpiece1);
    Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

    // Determine expected
    List<List<Double>> expected = new ArrayList<List<Double>>();
    // Chord 0
    expected.add(Arrays.asList(new Double[]{4.0, 1/4.0, 19.0, 7.0, 8.0, 4.0, -1.0}));
    // Chord 1
    expected.add(Arrays.asList(new Double[]{4.0, 3/16.0, 27.0, 12.0, 15.0, 3.0, -1.0}));
    // Chord 2
    expected.add(Arrays.asList(new Double[]{1.0, 1/16.0, 0.0, -1.0, -1.0, -1.0, -1.0}));
    // Chord 3
    expected.add(Arrays.asList(new Double[]{4.0, 1/8.0, 18.0, 3.0, 9.0, 6.0, -1.0}));
    // Chord 4
    expected.add(Arrays.asList(new Double[]{1.0, 1/8.0, 0.0, -1.0, -1.0, -1.0, -1.0}));
    // Chord 5
    expected.add(Arrays.asList(new Double[]{5.0, 1/4.0, 24.0, 12.0, 0.0, 3.0, 9.0}));
    // Chord 6
    expected.add(Arrays.asList(new Double[]{4.0, 1/8.0, 24.0, 15.0, 4.0, 5.0, -1.0}));
    // Chord 7
    expected.add(Arrays.asList(new Double[]{2.0, 1/8.0, 9.0, 9.0, -1.0, -1.0, -1.0}));
    // Chord 8
    expected.add(Arrays.asList(new Double[]{4.0, 1/16.0, 24.0, 12.0, 7.0, 5.0, -1.0}));
    // Chords 9-14
    expected.add(Arrays.asList(new Double[]{1.0, 1/16.0, 0.0, -1.0, -1.0, -1.0, -1.0}));
    expected.add(Arrays.asList(new Double[]{1.0, 1/32.0, 0.0, -1.0, -1.0, -1.0, -1.0}));
    expected.add(Arrays.asList(new Double[]{1.0, 1/32.0, 0.0, -1.0, -1.0, -1.0, -1.0}));
    expected.add(Arrays.asList(new Double[]{1.0, 1/32.0, 0.0, -1.0, -1.0, -1.0, -1.0}));
    expected.add(Arrays.asList(new Double[]{1.0, 1/32.0, 0.0, -1.0, -1.0, -1.0, -1.0}));
    expected.add(Arrays.asList(new Double[]{1.0, 1/4.0, 0.0, -1.0, -1.0, -1.0, -1.0}));
    // Chord 15
    expected.add(Arrays.asList(new Double[]{4.0, 1/4.0, 24.0, 12.0, 7.0, 5.0, -1.0}));
   
    // Calculate actual
    List<List<Double>> actual = new ArrayList<List<Double>>();
    Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
    int largestChordSizeTraining = tablature.getLargestTablatureChord();
	  int highestNumberOfVoicesTraining = transcription.getNumberOfVoices(); 
	  List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
    int lowestOnsetIndex = 0;
    for (int i = 0; i < tablature.getChords().size(); i++) {
   	  actual.add(featureGeneratorChord.getSharedNoteFeaturesChord(basicTabSymbolProperties, null, allVoiceLabels,
   	  	largestChordSizeTraining, highestNumberOfVoicesTraining, lowestOnsetIndex));
      lowestOnsetIndex += tablature.getChords().get(i).size();
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


	@Test
	public void testGetSharedNoteFeaturesChordNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1);

    // Determine expected
    List<List<Double>> expected = new ArrayList<List<Double>>();
    // Chord 0
    expected.add(Arrays.asList(new Double[]{4.0, 4.0, 0.0, 0.0, 0.0, 0.0, 0.0, 19.0, 7.0, 8.0, 4.0, -1.0}));
    // Chord 1
    expected.add(Arrays.asList(new Double[]{4.0, 4.0, 0.0, 0.0, 0.0, 0.0, 0.0, 27.0, 12.0, 12.0, 3.0, -1.0}));
    // Chord 2
    expected.add(Arrays.asList(new Double[]{3.0, 1.0, 1.0, 0.0, 1.0, 0.0, 0.0, 24.0, 9.0, 15.0, -1.0, -1.0}));
    // Chord 3
    expected.add(Arrays.asList(new Double[]{5.0, 5.0, 0.0, 0.0, 0.0, 0.0, 0.0, 18.0, 3.0, 9.0, 6.0, 0.0}));
    // Chord 4
    expected.add(Arrays.asList(new Double[]{4.0, 1.0, 1.0, 0.0, 1.0, 1.0, 0.0, 20.0, 5.0, 9.0, 6.0, -1.0}));
    // Chord 5
    expected.add(Arrays.asList(new Double[]{5.0, 5.0, 0.0, 0.0, 0.0, 0.0, 0.0, 24.0, 12.0, 0.0, 3.0, 9.0}));
    // Chord 6
    expected.add(Arrays.asList(new Double[]{5.0, 4.0, 0.0, 0.0, 0.0, 1.0, 0.0, 24.0, 12.0, 3.0, 4.0, 5.0}));
    // Chord 7
    expected.add(Arrays.asList(new Double[]{5.0, 2.0, 0.0, 1.0, 0.0, 1.0, 1.0, 24.0, 12.0, 2.0, 9.0, 1.0}));
    // Chord 8
    expected.add(Arrays.asList(new Double[]{4.0, 4.0, 0.0, 0.0, 0.0, 0.0, 0.0, 24.0, 12.0, 7.0, 5.0, -1.0}));
    // Chords 9-14
    expected.add(Arrays.asList(new Double[]{4.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 23.0, 12.0, 7.0, 4.0, -1.0}));
    expected.add(Arrays.asList(new Double[]{4.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 24.0, 12.0, 7.0, 5.0, -1.0}));
    expected.add(Arrays.asList(new Double[]{4.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 23.0, 12.0, 7.0, 4.0, -1.0}));
    expected.add(Arrays.asList(new Double[]{4.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 21.0, 12.0, 7.0, 2.0, -1.0}));
    expected.add(Arrays.asList(new Double[]{4.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 23.0, 12.0, 7.0, 4.0, -1.0}));
    expected.add(Arrays.asList(new Double[]{4.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 24.0, 12.0, 7.0, 5.0, -1.0}));
    // Chord 15
    expected.add(Arrays.asList(new Double[]{4.0, 4.0, 0.0, 0.0, 0.0, 0.0, 0.0, 24.0, 12.0, 7.0, 5.0, -1.0}));
   
    // Calculate actual
    List<List<Double>> actual = new ArrayList<List<Double>>();
    Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
    int largestChordSizeTraining = transcription.getLargestTranscriptionChord();
	  int highestNumberOfVoicesTraining = transcription.getNumberOfVoices(); 
	  List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
    int lowestOnsetIndex = 0;
    for (int i = 0; i < transcription.getChords().size(); i++) {
 	   	actual.add(featureGeneratorChord.getSharedNoteFeaturesChord(null, basicNoteProperties,	allVoiceLabels,
 	   		largestChordSizeTraining, highestNumberOfVoicesTraining, lowestOnsetIndex));
 	    lowestOnsetIndex += transcription.getChords().get(i).size();
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


	@Test
	public void testGetAverageProximitiesAndMovementsOfChord() {
		Tablature tablature = new Tablature(encodingTestpiece1);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);
 	
		List<double[]> expected = new ArrayList<double[]>();
		List<List<List<Double>>> expectedAsList = new ArrayList<List<List<Double>>>();
		// Chord 0
		List<List<Double>> expected0 = new ArrayList<List<Double>>();
		expected0.add(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0}));
		expected0.add(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0}));
		expected0.add(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0}));
		expected0.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
		// Chord 1
		List<List<Double>> expected1 = new ArrayList<List<Double>>();
		expected1.add(Arrays.asList(new Double[]{5.0, 0.0, 3.0, 4.0}));
		expected1.add(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0}));
		expected1.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0}));
		expected1.add(Arrays.asList(new Double[]{3.0, 4.0, 0.0, -5.0, 0.0}));
		// Chord 2
		List<List<Double>> expected2 = new ArrayList<List<Double>>();
		expected2.add(Arrays.asList(new Double[]{3.0}));
		expected2.add(Arrays.asList(new Double[]{3/16.0}));
		expected2.add(Arrays.asList(new Double[]{0.0}));
		expected2.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 3.0, 0.0}));
		// Chord 3
		List<List<Double>> expected3 = new ArrayList<List<Double>>();
		expected3.add(Arrays.asList(new Double[]{-1.0, 2.0, 2.0, 7.0, 4.0}));
		expected3.add(Arrays.asList(new Double[]{-1.0, 1/16.0, 1/4.0, 1/4.0, 1/4.0}));
		expected3.add(Arrays.asList(new Double[]{-1.0, 0.0, 1/16.0, 1/16.0, 1/16.0}));
		expected3.add(Arrays.asList(new Double[]{-7.0, -4.0, 2.0, 2.0, 0.0}));
		// Chord 4
		List<List<Double>> expected4 = new ArrayList<List<Double>>();
		expected4.add(Arrays.asList(new Double[]{2.0}));
		expected4.add(Arrays.asList(new Double[]{1/8.0}));
		expected4.add(Arrays.asList(new Double[]{0.0}));
		expected4.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, -2.0}));
		// Chord 5
		List<List<Double>> expected5 = new ArrayList<List<Double>>();
		expected5.add(Arrays.asList(new Double[]{0.0, 7.0, 2.0, 5.0, 4.0}));
		expected5.add(Arrays.asList(new Double[]{1/8.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}));
		expected5.add(Arrays.asList(new Double[]{0.0, 1/8.0, 1/8.0, 1/8.0, 1/8.0}));
		expected5.add(Arrays.asList(new Double[]{4.0, -5.0, -2.0, 7.0, 0.0}));
		// Chord 6
		List<List<Double>> expected6 = new ArrayList<List<Double>>();
		expected6.add(Arrays.asList(new Double[]{0.0, 3.0, 5.0, 9.0}));
		expected6.add(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0}));
		expected6.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0}));
		expected6.add(Arrays.asList(new Double[]{-5.0, 9.0, 3.0, 0.0, 0.0}));
		// Chord 7
		List<List<Double>> expected7 = new ArrayList<List<Double>>();
		expected7.add(Arrays.asList(new Double[]{1.0, 4.0}));
		expected7.add(Arrays.asList(new Double[]{1/8.0, 1/8.0}));
		expected7.add(Arrays.asList(new Double[]{0.0, 0.0}));
		expected7.add(Arrays.asList(new Double[]{4.0, 0.0, -1.0, 0.0, 0.0}));
		// Chord 8
		List<List<Double>> expected8 = new ArrayList<List<Double>>();
		expected8.add(Arrays.asList(new Double[]{12.0, 2.0, 5.0, 1.0}));
		expected8.add(Arrays.asList(new Double[]{1/2.0, 1/8.0, 1/4.0, 1/8.0}));
		expected8.add(Arrays.asList(new Double[]{1/4.0, 0.0, 1/8.0, 0.0}));
		expected8.add(Arrays.asList(new Double[]{1.0, -5.0, -2.0, -12.0, 0.0}));
		// Chords 9-14
		List<List<Double>> expected9 = new ArrayList<List<Double>>();
		expected9.add(Arrays.asList(new Double[]{1.0}));
		expected9.add(Arrays.asList(new Double[]{1/16.0}));
		expected9.add(Arrays.asList(new Double[]{0.0}));
		expected9.add(Arrays.asList(new Double[]{-1.0, 0.0, 0.0, 0.0, 0.0}));
		List<List<Double>> expected10 = new ArrayList<List<Double>>();
		expected10.add(Arrays.asList(new Double[]{1.0}));
		expected10.add(Arrays.asList(new Double[]{1/16.0}));
		expected10.add(Arrays.asList(new Double[]{0.0}));
		expected10.add(Arrays.asList(new Double[]{1.0, 0.0, 0.0, 0.0, 0.0}));
		List<List<Double>> expected11 = new ArrayList<List<Double>>();
		expected11.add(Arrays.asList(new Double[]{1.0}));
		expected11.add(Arrays.asList(new Double[]{1/32.0}));
		expected11.add(Arrays.asList(new Double[]{0.0}));
		expected11.add(Arrays.asList(new Double[]{-1.0, 0.0, 0.0, 0.0, 0.0}));
		List<List<Double>> expected12 = new ArrayList<List<Double>>();
		expected12.add(Arrays.asList(new Double[]{2.0}));
		expected12.add(Arrays.asList(new Double[]{1/32.0}));
		expected12.add(Arrays.asList(new Double[]{0.0}));
		expected12.add(Arrays.asList(new Double[]{-2.0, 0.0, 0.0, 0.0, 0.0}));
		List<List<Double>> expected13 = new ArrayList<List<Double>>();
		expected13.add(Arrays.asList(new Double[]{2.0}));
		expected13.add(Arrays.asList(new Double[]{1/32.0}));
		expected13.add(Arrays.asList(new Double[]{0.0}));
		expected13.add(Arrays.asList(new Double[]{2.0, 0.0, 0.0, 0.0, 0.0}));
		List<List<Double>> expected14 = new ArrayList<List<Double>>();
		expected14.add(Arrays.asList(new Double[]{1.0}));
		expected14.add(Arrays.asList(new Double[]{1/32.0}));
		expected14.add(Arrays.asList(new Double[]{0.0}));
		expected14.add(Arrays.asList(new Double[]{1.0, 0.0, 0.0, 0.0, 0.0}));
		// Chord 15
		List<List<Double>> expected15 = new ArrayList<List<Double>>();
		expected15.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0}));
		expected15.add(Arrays.asList(new Double[]{3/4.0, 3/4.0, 3/4.0, 2/4.0}));
		expected15.add(Arrays.asList(new Double[]{11/16.0, 11/16.0, 11/16.0, 1/4.0}));
		expected15.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));

		expectedAsList.add(expected0); expectedAsList.add(expected1); expectedAsList.add(expected2); 
		expectedAsList.add(expected3); expectedAsList.add(expected4); expectedAsList.add(expected5); 
		expectedAsList.add(expected6); expectedAsList.add(expected7); expectedAsList.add(expected8);
		expectedAsList.add(expected9); expectedAsList.add(expected10); expectedAsList.add(expected11); 
		expectedAsList.add(expected12); expectedAsList.add(expected13); expectedAsList.add(expected14);
		expectedAsList.add(expected15);
    
		// For each element of expectedAsList: turn the elements of the first three Lists from distances into proximities
		for (int i = 0; i < expectedAsList.size(); i++) {
			List<List<Double>> currentExpected = expectedAsList.get(i);
			for (int j = 0; j < currentExpected.size() - 1; j++) {
				List<Double> currentProximities = currentExpected.get(j);
				for (int k = 0; k < currentProximities.size(); k++) {
					double oldValue = currentProximities.get(k);
					// Do only if oldValue is not -1.0, i.e., if the voice is active
					if (oldValue != -1.0) {
						double newValue = 1.0/(oldValue + 1);
						currentProximities.set(k, newValue);
					}
				}
			}
		}

		// For each element of expectedAsList: average the elements of the first three Lists (the proximities) and add the
		// respective numbers to currentExpected; add all elements of the last List (the pitch movements) to
		// currentExpected; then add currentExpected to expected
//		int highestNumberOfVoicesTraining = transcription.getNumberOfVoices();
		for (int i = 0; i < expectedAsList.size(); i++) {
			List<List<Double>> currentExpectedAsList = expectedAsList.get(i);
			double[] currentExpected = new double[3 + mnv];
//			double[] currentExpected = new double[3 + highestNumberOfVoicesTraining];
			// Add the averages of the proximities to currentExpected
			for (int j = 0; j < currentExpectedAsList.size() - 1; j++) {
				List<Double> currentList = currentExpectedAsList.get(j);
				currentExpected[j] = ToolBox.getAverage(currentList);
			}
			// Add the pitch movements to currentExpected
			List<Double> currentPitchMovements = currentExpectedAsList.get(currentExpectedAsList.size() - 1);
			for (int j = 0; j < currentPitchMovements.size(); j++) {
				currentExpected[3 + j] = currentPitchMovements.get(j);
			}
			// Add currentExpected to expected
			expected.add(currentExpected);
		}

		List<double[]> actual = new ArrayList<double[]>();
		Integer[][] btp = tablature.getBasicTabSymbolProperties();
		List<List<Integer>> voiceAssignments = getVoiceAssignments();
		int lowestOnsetIndex = 0;
		for (int i = 0; i < tablature.getChords().size(); i++) {
			actual.add(featureGeneratorChord.getAverageProximitiesAndMovementsOfChord(btp, null,
				transcription, lowestOnsetIndex, voiceAssignments.get(i), mnv));
			lowestOnsetIndex += tablature.getChords().get(i).size(); 	
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j], delta);
			}
		} 
	}


	@Test
	public void testGetAverageProximitiesAndMovementsOfChordNonTab() {    
 		Transcription transcription = new Transcription(midiTestpiece1);
 	
  	// Determine expected
  	List<double[]> expected = new ArrayList<double[]>();
  	List<List<List<Double>>> expectedAsList = new ArrayList<List<List<Double>>>();
    // Chord 0
  	List<List<Double>> expected0 = new ArrayList<List<Double>>();
  	expected0.add(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0}));
  	expected0.add(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0}));
  	expected0.add(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0}));
  	expected0.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
  	// Chord 1
    List<List<Double>> expected1 = new ArrayList<List<Double>>();
    expected1.add(Arrays.asList(new Double[]{5.0, 0.0, 4.0, 3.0}));
    expected1.add(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0}));
    expected1.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0}));
    expected1.add(Arrays.asList(new Double[]{3.0, 4.0, 0.0, -5.0, 0.0}));
  	// Chord 2
    List<List<Double>> expected2 = new ArrayList<List<Double>>();
    expected2.add(Arrays.asList(new Double[]{3.0}));
    expected2.add(Arrays.asList(new Double[]{3/16.0}));
    expected2.add(Arrays.asList(new Double[]{0.0}));
    expected2.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 3.0, 0.0}));
  	// Chord 3
  	List<List<Double>> expected3 = new ArrayList<List<Double>>();
  	expected3.add(Arrays.asList(new Double[]{-1.0, 2.0, 2.0, 7.0, 4.0}));
  	expected3.add(Arrays.asList(new Double[]{-1.0, 1/16.0, 1/4.0, 1/4.0, 1/4.0}));
  	expected3.add(Arrays.asList(new Double[]{-1.0, 0.0, 0.0, 0.0, 1/8.0}));
  	expected3.add(Arrays.asList(new Double[]{-7.0, -4.0, 2.0, 2.0, 0.0}));
  	// Chord 4
  	List<List<Double>> expected4 = new ArrayList<List<Double>>();
    expected4.add(Arrays.asList(new Double[]{2.0}));
    expected4.add(Arrays.asList(new Double[]{1/8.0}));
    expected4.add(Arrays.asList(new Double[]{0.0}));
    expected4.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, -2.0}));
  	// Chord 5
  	List<List<Double>> expected5 = new ArrayList<List<Double>>();
  	expected5.add(Arrays.asList(new Double[]{0.0, 7.0, 2.0, 5.0, 4.0}));
  	expected5.add(Arrays.asList(new Double[]{1/8.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}));
  	expected5.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1/8.0, 0.0}));
  	expected5.add(Arrays.asList(new Double[]{4.0, -5.0, -2.0, 7.0, 0.0}));
  	// Chord 6
  	List<List<Double>> expected6 = new ArrayList<List<Double>>();
  	expected6.add(Arrays.asList(new Double[]{0.0, 3.0, 5.0, 9.0}));
  	expected6.add(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0}));
  	expected6.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0}));
  	expected6.add(Arrays.asList(new Double[]{-5.0, 9.0, 3.0, 0.0, 0.0}));
  	// Chord 7
  	List<List<Double>> expected7 = new ArrayList<List<Double>>();
  	expected7.add(Arrays.asList(new Double[]{1.0, 4.0}));
  	expected7.add(Arrays.asList(new Double[]{1/8.0, 1/8.0}));
  	expected7.add(Arrays.asList(new Double[]{0.0, 0.0}));
  	expected7.add(Arrays.asList(new Double[]{4.0, 0.0, -1.0, 0.0, 0.0}));
  	// Chord 8
  	List<List<Double>> expected8 = new ArrayList<List<Double>>();
  	expected8.add(Arrays.asList(new Double[]{12.0, 2.0, 5.0, 1.0}));
  	expected8.add(Arrays.asList(new Double[]{1/2.0, 1/8.0, 1/4.0, 1/8.0}));
  	expected8.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0}));
  	expected8.add(Arrays.asList(new Double[]{1.0, -5.0, -2.0, -12.0, 0.0}));
  	// Chords 9-14
  	List<List<Double>> expected9 = new ArrayList<List<Double>>();
    expected9.add(Arrays.asList(new Double[]{1.0}));
    expected9.add(Arrays.asList(new Double[]{1/16.0}));
    expected9.add(Arrays.asList(new Double[]{0.0}));
    expected9.add(Arrays.asList(new Double[]{-1.0, 0.0, 0.0, 0.0, 0.0}));
    List<List<Double>> expected10 = new ArrayList<List<Double>>();
    expected10.add(Arrays.asList(new Double[]{1.0}));
    expected10.add(Arrays.asList(new Double[]{1/16.0}));
    expected10.add(Arrays.asList(new Double[]{0.0}));
    expected10.add(Arrays.asList(new Double[]{1.0, 0.0, 0.0, 0.0, 0.0}));
    List<List<Double>> expected11 = new ArrayList<List<Double>>();
    expected11.add(Arrays.asList(new Double[]{1.0}));
    expected11.add(Arrays.asList(new Double[]{1/32.0}));
    expected11.add(Arrays.asList(new Double[]{0.0}));
    expected11.add(Arrays.asList(new Double[]{-1.0, 0.0, 0.0, 0.0, 0.0}));
    List<List<Double>> expected12 = new ArrayList<List<Double>>();
    expected12.add(Arrays.asList(new Double[]{2.0}));
    expected12.add(Arrays.asList(new Double[]{1/32.0}));
    expected12.add(Arrays.asList(new Double[]{0.0}));
    expected12.add(Arrays.asList(new Double[]{-2.0, 0.0, 0.0, 0.0, 0.0}));
    List<List<Double>> expected13 = new ArrayList<List<Double>>();
    expected13.add(Arrays.asList(new Double[]{2.0}));
    expected13.add(Arrays.asList(new Double[]{1/32.0}));
    expected13.add(Arrays.asList(new Double[]{0.0}));
    expected13.add(Arrays.asList(new Double[]{2.0, 0.0, 0.0, 0.0, 0.0}));
    List<List<Double>> expected14 = new ArrayList<List<Double>>();
    expected14.add(Arrays.asList(new Double[]{1.0}));
    expected14.add(Arrays.asList(new Double[]{1/32.0}));
    expected14.add(Arrays.asList(new Double[]{0.0}));
    expected14.add(Arrays.asList(new Double[]{1.0, 0.0, 0.0, 0.0, 0.0}));
  	// Chord 15
  	List<List<Double>> expected15 = new ArrayList<List<Double>>();
  	expected15.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0}));
  	expected15.add(Arrays.asList(new Double[]{3/4.0, 3/4.0, 3/4.0, 2/4.0}));
  	expected15.add(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0}));
  	expected15.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
  	
    expectedAsList.add(expected0); expectedAsList.add(expected1); expectedAsList.add(expected2); 
    expectedAsList.add(expected3); expectedAsList.add(expected4); expectedAsList.add(expected5); 
    expectedAsList.add(expected6); expectedAsList.add(expected7); expectedAsList.add(expected8); 
    expectedAsList.add(expected9); expectedAsList.add(expected10); expectedAsList.add(expected11);
    expectedAsList.add(expected12); expectedAsList.add(expected13); expectedAsList.add(expected14);
    expectedAsList.add(expected15);
    
    // For each element of expectedAsList: turn the elements of the first three Lists from distances into proximities
 	  for (int i = 0; i < expectedAsList.size(); i++) {
   		List<List<Double>> currentExpected = expectedAsList.get(i);
 	  	for (int j = 0; j < currentExpected.size() - 1; j++) {
 	  		List<Double> currentProximities = currentExpected.get(j);
 	  		for (int k = 0; k < currentProximities.size(); k++) {
 	  			double oldValue = currentProximities.get(k);
 	  			// Do only if oldValue is not -1.0, i.e., if the voice is active
 	  		  if (oldValue != -1.0) {
 	  			  double newValue = 1.0/(oldValue + 1);
 	  		    currentProximities.set(k, newValue);
 	  		  }
 	  		}
 	  	}
 	  }
 	  
 	  // For each element of expectedAsList: average the elements of the first three Lists (the proximities) and add the
 	  // respective numbers to currentExpected; add all elements of the last List (the pitch movements) to
 	  // currentExpected; then add currentExpected to expected

// 	  int highestNumberOfVoicesTraining = transcription.getNumberOfVoices();
 	  for (int i = 0; i < expectedAsList.size(); i++) {
  		List<List<Double>> currentExpectedAsList = expectedAsList.get(i);
   		double[] currentExpected = new double[3 + mnv];
//   		double[] currentExpected = new double[3 + highestNumberOfVoicesTraining];
	  	// Add the averages of the proximities to currentExpected
  		for (int j = 0; j < currentExpectedAsList.size() - 1; j++) {
	  		List<Double> currentList = currentExpectedAsList.get(j);
  		  currentExpected[j] = ToolBox.getAverage(currentList);
	  	}
	  	// Add the pitch movements to currentExpected
  		List<Double> currentPitchMovements = currentExpectedAsList.get(currentExpectedAsList.size() - 1);
  		for (int j = 0; j < currentPitchMovements.size(); j++) {
  		  currentExpected[3 + j] = currentPitchMovements.get(j);
  		}
  		// Add currentExpected to expected
	  	expected.add(currentExpected);
	  }
 	  
  	// Calculate actual
 	  List<double[]> actual = new ArrayList<double[]>();
  	Integer[][] bnp = transcription.getBasicNoteProperties();
  	List<List<Integer>> voiceAssignments = getVoiceAssignmentsNonTab();
  	int lowestOnsetIndex = 0;
		for (int i = 0; i < transcription.getChords().size(); i++) {
	  	actual.add(featureGeneratorChord.getAverageProximitiesAndMovementsOfChord(null, bnp, transcription, 
	  		lowestOnsetIndex, voiceAssignments.get(i), mnv));
	  	lowestOnsetIndex += transcription.getChords().get(i).size(); 	
		}
  		
  	// Assert equality
  	assertEquals(expected.size(), actual.size());
  	for (int i = 0; i < expected.size(); i++) {
  		assertEquals(expected.get(i).length, actual.get(i).length);
  		for (int j = 0; j < expected.get(i).length; j++) {
  			assertEquals(expected.get(i)[j], actual.get(i)[j], delta);
  		}
  	} 
	}


	@Test
	public void testGetRangeOfChord() {
    Tablature tablature = new Tablature(encodingTestpiece1);
    
    // Determine expected 
    List<Double> expected = Arrays.asList(new Double[]{19.0, 27.0, 0.0, 18.0, 0.0, 24.0, 24.0, 9.0, 24.0,
    	0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 24.0});
   
    // Calculate actual
    List<Double> actual = new ArrayList<Double>(); 
    Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
    int lowestNoteIndex = 0;
    for (int i = 0; i < tablature.getChords().size(); i++) {
   	  actual.add(featureGeneratorChord.getRangeOfChord(basicTabSymbolProperties, null, lowestNoteIndex));
   	  lowestNoteIndex += tablature.getChords().get(i).size();
    }
    
    // Assert equality
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
    	assertEquals(expected.get(i), actual.get(i));
    }
    assertEquals(expected, actual); 
	}


	@Test
	public void testGetRangeOfChordNonTab() {
 		Transcription transcription = new Transcription(midiTestpiece1);

    // Determine expected 
    List<Double> expected = Arrays.asList(new Double[]{19.0, 27.0, 24.0, 18.0, 20.0, 24.0, 24.0, 24.0, 24.0,
      23.0, 24.0, 23.0, 21.0, 23.0, 24.0, 24.0});
 
    // Calculate actual 
    List<Double> actual = new ArrayList<Double>(); 
    Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
    int lowestNoteIndex = 0;
    for (int i = 0; i < transcription.getChords().size(); i++) {
 	    actual.add(featureGeneratorChord.getRangeOfChord(null, basicNoteProperties, lowestNoteIndex));
 	    lowestNoteIndex += transcription.getChords().get(i).size();
    }
  
   // Assert equality
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
  	  assertEquals(expected.get(i), actual.get(i));
    }
    assertEquals(expected, actual); 
	}


	@Test
	public void testGetIntervalsInChordMUSCI() {
		Tablature tablature = new Tablature(encodingTestpiece1);
				  
		// Determine expected
		List<double[]> expected = new ArrayList<double[]>();
		expected.add(new double[]{7.0, 8.0, 4.0, -1.0});
		expected.add(new double[]{12.0, 15.0, 3.0, -1.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{3.0, 9.0, 6.0, -1.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{12.0, 0.0, 3.0, 9.0});
		expected.add(new double[]{15.0, 4.0, 5.0, -1.0});
		expected.add(new double[]{9.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{12.0, 7.0, 5.0, -1.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{-1.0, -1.0, -1.0, -1.0});
		expected.add(new double[]{12.0, 7.0, 5.0, -1.0});
				  
	  // Calculate actual
		List<double[]> actual = new ArrayList<double[]>();
		Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
		int largestChordSizeTraining = tablature.getLargestTablatureChord();
		int lowestNoteIndex = 0;
		for (int i = 0; i < tablature.getChords().size(); i++) {
		 	actual.add(featureGeneratorChord.getIntervalsInChordMUSCI(basicTabSymbolProperties, null,
			 	largestChordSizeTraining, lowestNoteIndex));
		 	lowestNoteIndex += tablature.getChords().get(i).size();
		}

		// Assert equality
		assertEquals(expected.size(), actual.size());
	   for (int i = 0; i < expected.size(); i++) {
	   	assertEquals(expected.get(i).length, actual.get(i).length);
	   	for (int j = 0; j < expected.get(i).length; j++) {
	   		assertEquals(expected.get(i)[j], actual.get(i)[j], delta);
	   	} 
	  }
	}


	@Test
	public void testGetIntervalsInChordNonTabMUSCI() {		
  	Transcription transcription = new Transcription(midiTestpiece1);

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
		int largestChordSizeTraining = transcription.getLargestTranscriptionChord();
		int lowestNoteIndex = 0;
		for (int i = 0; i < transcription.getChords().size(); i++) {
  	 	actual.add(featureGeneratorChord.getIntervalsInChordMUSCI(null, basicNoteProperties, largestChordSizeTraining, 
  	 		lowestNoteIndex));
  	 	lowestNoteIndex += transcription.getChords().get(i).size();
  	}

		// Assert equality
		assertEquals(expected.size(), actual.size());
	   for (int i = 0; i < expected.size(); i++) {
	   	assertEquals(expected.get(i).length, actual.get(i).length);
	   	for (int j = 0; j < expected.get(i).length; j++) {
	   		assertEquals(expected.get(i)[j], actual.get(i)[j], delta);
	   	} 
	  }
	}


	@Test
	public void testGetProximitiesAndMovementsOfChordOLD() {
//    Tablature tablature = new Tablature(encodingTestpiece1);
//    Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);
//
//    List<List<Integer>> voiceAssignments = getVoiceAssignmentsNonTab();
//    	
// 	  // Determine expected
// 	  List<List<List<Double>>> expected = new ArrayList<List<List<Double>>>();
// 	  // Chord 0
// 	  List<List<Double>> expected0 = new ArrayList<List<Double>>();
// 	  expected0.add(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0}));
// 	  expected0.add(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0}));
// 	  expected0.add(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, -1.0}));
// 	  expected0.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0})); 	
//    // Chord 1
// 	  List<List<Double>> expected1 = new ArrayList<List<Double>>();
// 	  expected1.add(Arrays.asList(new Double[]{3.0, 4.0, 0.0, 5.0, -1.0})); 
// 	  expected1.add(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0, -1.0}));
// 	  expected1.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, -1.0}));
// 	  expected1.add(Arrays.asList(new Double[]{3.0, 4.0, 0.0, -5.0, 0.0}));
//  	// Chord 2
// 	  List<List<Double>> expected2 = new ArrayList<List<Double>>();
// 	  expected2.add(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, 3.0, -1.0}));
// 	  expected2.add(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, 3/16.0, -1.0}));
// 	  expected2.add(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, 0.0, -1.0}));
// 	  expected2.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 3.0, 0.0}));
// 	  // Chord 3
// 	  List<List<Double>> expected3 = new ArrayList<List<Double>>();
// 	  expected3.add(Arrays.asList(new Double[]{7.0, 4.0, 2.0, 2.0, -1.0}));
// 	  expected3.add(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/16.0, -1.0}));
// 	  expected3.add(Arrays.asList(new Double[]{1/16.0, 1/16.0, 1/16.0, 0.0, -1.0}));
// 	  expected3.add(Arrays.asList(new Double[]{-7.0, -4.0, 2.0, 2.0, 0.0}));
// 	  // Chord 4
// 	  List<List<Double>> expected4 = new ArrayList<List<Double>>();
//	  expected4.add(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, 2.0}));
//	  expected4.add(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, 1/8.0}));
//	  expected4.add(Arrays.asList(new Double[]{-1.0, -1.0, -1.0, -1.0, 0.0}));
//	  expected4.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, -2.0}));
// 	  // Chord 5
// 	  List<List<Double>> expected5 = new ArrayList<List<Double>>();
// 	  expected5.add(Arrays.asList(new Double[]{4.0, 5.0, 2.0, 7.0, 0.0}));
// 	  expected5.add(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, 1/4.0, 1/4.0}));
// 	  expected5.add(Arrays.asList(new Double[]{1/8.0, 1/8.0, 1/8.0, 1/8.0, 0.0}));
// 	  expected5.add(Arrays.asList(new Double[]{4.0, -5.0, -2.0, 7.0, 0.0}));
// 	  // Chord 6
// 	  List<List<Double>> expected6 = new ArrayList<List<Double>>();
// 	  expected6.add(Arrays.asList(new Double[]{5.0, 9.0, 3.0, -1.0, 0.0}));
// 	  expected6.add(Arrays.asList(new Double[]{1/4.0, 1/4.0, 1/4.0, -1.0, 1/4.0}));
// 	  expected6.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, -1.0, 0.0}));
// 	  expected6.add(Arrays.asList(new Double[]{-5.0, 9.0, 3.0, 0.0, 0.0}));
// 	  // Chord 7
// 	  List<List<Double>> expected7 = new ArrayList<List<Double>>();
// 	  expected7.add(Arrays.asList(new Double[]{4.0, -1.0, 1.0, -1.0, -1.0}));
// 	  expected7.add(Arrays.asList(new Double[]{1/8.0, -1.0, 1/8.0, -1.0, -1.0}));
// 	  expected7.add(Arrays.asList(new Double[]{0.0, -1.0, 0.0, -1.0, -1.0}));
// 	  expected7.add(Arrays.asList(new Double[]{4.0, 0.0, -1.0, 0.0, 0.0}));
// 	  // Chord 8
// 	  List<List<Double>> expected8 = new ArrayList<List<Double>>();
// 	  expected8.add(Arrays.asList(new Double[]{1.0, 5.0, 2.0, 12.0, -1.0}));
// 	  expected8.add(Arrays.asList(new Double[]{1/8.0, 1/4.0, 1/8.0, 1/2.0, -1.0}));
// 	  expected8.add(Arrays.asList(new Double[]{0.0, 1/8.0, 0.0, 1/4.0, -1.0}));
// 	  expected8.add(Arrays.asList(new Double[]{1.0, -5.0, -2.0, -12.0, 0.0}));
// 	  // Chords 9-14
// 	  List<List<Double>> expected9 = new ArrayList<List<Double>>();
//	  expected9.add(Arrays.asList(new Double[]{1.0, -1.0, -1.0, -1.0, -1.0}));
//	  expected9.add(Arrays.asList(new Double[]{1/16.0, -1.0, -1.0, -1.0, -1.0}));
//	  expected9.add(Arrays.asList(new Double[]{0.0, -1.0, -1.0, -1.0, -1.0}));
//	  expected9.add(Arrays.asList(new Double[]{-1.0, 0.0, 0.0, 0.0, 0.0}));
//	  List<List<Double>> expected10 = new ArrayList<List<Double>>();
// 	  expected10.add(Arrays.asList(new Double[]{1.0, -1.0, -1.0, -1.0, -1.0}));
// 	  expected10.add(Arrays.asList(new Double[]{1/16.0, -1.0, -1.0, -1.0, -1.0}));
// 	  expected10.add(Arrays.asList(new Double[]{0.0, -1.0, -1.0, -1.0, -1.0}));
// 	  expected10.add(Arrays.asList(new Double[]{1.0, 0.0, 0.0, 0.0, 0.0}));
// 	  List<List<Double>> expected11 = new ArrayList<List<Double>>();
//	  expected11.add(Arrays.asList(new Double[]{1.0, -1.0, -1.0, -1.0, -1.0}));
//	  expected11.add(Arrays.asList(new Double[]{1/32.0, -1.0, -1.0, -1.0, -1.0}));
//	  expected11.add(Arrays.asList(new Double[]{0.0, -1.0, -1.0, -1.0, -1.0}));
//	  expected11.add(Arrays.asList(new Double[]{-1.0, 0.0, 0.0, 0.0, 0.0}));
//	  List<List<Double>> expected12 = new ArrayList<List<Double>>();
// 	  expected12.add(Arrays.asList(new Double[]{2.0, -1.0, -1.0, -1.0, -1.0}));
// 	  expected12.add(Arrays.asList(new Double[]{1/32.0, 1/4.0, 1/8.0, 1/2.0, -1.0}));
// 	  expected12.add(Arrays.asList(new Double[]{0.0, -1.0, -1.0, -1.0, -1.0}));
// 	  expected12.add(Arrays.asList(new Double[]{-2.0, 0.0, 0.0, 0.0, 0.0}));
// 	  List<List<Double>> expected13 = new ArrayList<List<Double>>();
//	  expected13.add(Arrays.asList(new Double[]{2.0, -1.0, -1.0, -1.0, -1.0}));
//	  expected13.add(Arrays.asList(new Double[]{1/32.0, 1/4.0, 1/8.0, 1/2.0, -1.0}));
//	  expected13.add(Arrays.asList(new Double[]{0.0, -1.0, -1.0, -1.0, -1.0}));
//	  expected13.add(Arrays.asList(new Double[]{2.0, 0.0, 0.0, 0.0, 0.0}));
//	  List<List<Double>> expected14 = new ArrayList<List<Double>>();
// 	  expected14.add(Arrays.asList(new Double[]{1.0, -1.0, -1.0, -1.0, -1.0}));
// 	  expected14.add(Arrays.asList(new Double[]{1/32.0, 1/4.0, 1/8.0, 1/2.0, -1.0}));
// 	  expected14.add(Arrays.asList(new Double[]{0.0, -1.0, -1.0, -1.0, -1.0}));
// 	  expected14.add(Arrays.asList(new Double[]{1.0, -5.0, -2.0, -12.0, 0.0}));
// 	  // Chord 15
// 	  List<List<Double>> expected15 = new ArrayList<List<Double>>();
// 	  expected15.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, -1.0}));
// 	  expected15.add(Arrays.asList(new Double[]{3/4.0, 3/4.0, 3/4.0, 1/2.0, -1.0}));
// 	  expected15.add(Arrays.asList(new Double[]{1/4.0, 11/16.0, 11/16.0, 11/16.0, -1.0}));
// 	  expected15.add(Arrays.asList(new Double[]{0.0, 0.0, 0.0, 0.0, 0.0}));
// 	
// 	  expected.add(expected0); expected.add(expected1); expected.add(expected2); expected.add(expected3);
// 	  expected.add(expected4); expected.add(expected5); expected.add(expected6); expected.add(expected9);
// 	  expected.add(expected15); expected.add(expected9); expected.add(expected10); expected.add(expected11);
// 	  expected.add(expected12); expected.add(expected13); expected.add(expected14); expected.add(expected15);
// 	
// 	  // For each element of expected: turn the elements of the first three Lists from distances into proximities
// 	  for (int i = 0; i < expected.size(); i++) {
//   		List<List<Double>> currentExpected = expected.get(i);
// 	  	for (int j = 0; j < currentExpected.size() - 1; j++) {
// 	  		List<Double> currentList = currentExpected.get(j);
// 	  		for (int k = 0; k < currentList.size(); k++) {
// 	  			double oldValue = currentList.get(k);
// 	  			// Do only if oldValue is not -1.0, i.e., if the voice is active
// 	  		  if (oldValue != -1.0) {
// 	  			  double newValue = 1.0/(oldValue + 1);
// 	  		    currentList.set(k, newValue);
// 	  		  }
// 	  		}
// 	  	}
// 	  }
// 	 	
// 	  // Calculate actual      	
// 	  List<List<List<Double>>> actual = new ArrayList<List<List<Double>>>();
// 	  Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
// 	  int largestNumberOfVoicesTraining = transcription.getNumberOfVoices();
//	  int lowestOnsetIndex = 0;
//	  for (int i = 0; i < tablature.getTablatureChords().size(); i++) {
//  		int chordSize = tablature.getTablatureChords().get(i).size();
//    	actual.add(featureGenerator.getProximitiesAndMovementsOfChord(basicTabSymbolProperties,	transcription,
//    		chordSize, lowestOnsetIndex, largestNumberOfVoicesTraining, voiceAssignments.get(i)));
//    	lowestOnsetIndex += chordSize; 	
// 		}
// 	
//   	// Assert equality
// 	  assertEquals(expected.size(), actual.size());
// 	  for (int i = 0; i < expected.size(); i++) {
// 	  	assertEquals(expected.get(i).size(), actual.get(i).size());
//   		for (int j = 0; j < expected.get(i).size(); j++) {
//   	    assertEquals(expected.get(i).get(j).size(), actual.get(i).get(j).size());
//   	    for (int k = 0; k < expected.get(i).get(j).size(); k++) {
//   	    	System.out.println(i + " " + j + " " + k);
//   	    	assertEquals(expected.get(i).get(j).get(k), actual.get(i).get(j).get(k));
//   	    }
// 	  	}
//  	} 
//   	assertEquals(expected, actual);
	}


	@Test
	public void testGetNumberOfActiveVoicesInChord() {
//    Tablature tablature = new Tablature(encodingTestpiece1);
//    Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);
//    
//    // Determine expected
//    List<Double> expected = Arrays.asList(new Double[]{4.0, 4.0, 1.0, 5.0, 1.0, 5.0, 4.0, 2.0, 4.0, 1.0,
//    	1.0, 1.0, 1.0, 1.0, 1.0, 4.0});
//    	
//  	// Calculate actual
//  	List<Double> actual = new ArrayList<Double>();
//    int highestNumberOfVoices = transcription.getNumberOfVoices();
//    List<List<Integer>> voiceAssignments = transcription.getVoiceAssignments(tablature, highestNumberOfVoices);
//    for (int i = 0; i < tablature.getTablatureChords().size(); i++) {
//    	List<Integer> currentVoiceAssignment = voiceAssignments.get(i);
//    	actual.add(featureGenerator.getNumberOfActiveVoicesInChord(currentVoiceAssignment));
//    }
//    
//    // Assert equality
//    assertEquals(expected.size(), actual.size());
//    for (int i = 0; i < expected.size(); i++) {
//    	assertEquals(expected.get(i), actual.get(i));
//    }
//    assertEquals(expected, actual);
	}


	@Test
	public void testGetPitchesOfSustainedPreviousNotesInChordMUSCI() {
		Transcription transcription = new Transcription(midiTestpiece1);
		
    // Determine expected
    List<List<Integer>> expected = new ArrayList<List<Integer>>();
    expected.add(Arrays.asList(new Integer[]{})); 
    expected.add(Arrays.asList(new Integer[]{})); 
    expected.add(Arrays.asList(new Integer[]{57, 72})); 
    expected.add(Arrays.asList(new Integer[]{})); 
    expected.add(Arrays.asList(new Integer[]{50, 59, 65})); 
    expected.add(Arrays.asList(new Integer[]{})); 
    expected.add(Arrays.asList(new Integer[]{57})); 
    expected.add(Arrays.asList(new Integer[]{57, 45, 69})); 
    expected.add(Arrays.asList(new Integer[]{}));
    expected.add(Arrays.asList(new Integer[]{45, 57, 64})); 
    expected.add(Arrays.asList(new Integer[]{45, 57, 64})); 
    expected.add(Arrays.asList(new Integer[]{45, 57, 64})); 
    expected.add(Arrays.asList(new Integer[]{45, 57, 64})); 
    expected.add(Arrays.asList(new Integer[]{45, 57, 64})); 
    expected.add(Arrays.asList(new Integer[]{45, 57, 64}));
    expected.add(Arrays.asList(new Integer[]{}));
    		  
    // Calculate actual
    List<List<Integer>> actual = new ArrayList<List<Integer>>();
    List<List<Note>> transcriptionChords = transcription.getChords();
    Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
    int lowestNoteIndex = 0;
    for (int i = 0; i < transcriptionChords.size(); i++) {
   	  actual.add(featureGeneratorChord.getPitchesOfSustainedPreviousNotesInChordMUSCI(basicNoteProperties,
   	  	lowestNoteIndex));
   	  lowestNoteIndex += transcriptionChords.get(i).size();
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


	@Test
	public void testGetVoicesOfSustainedPreviousNotesInChordMUSCI() {
		Transcription transcription = new Transcription(midiTestpiece1);

    // Determine expected
    List<List<Integer>> expected = new ArrayList<List<Integer>>();
    // Chord 0
    expected.add(Arrays.asList(new Integer[]{})); 
    // Chord 1
    expected.add(Arrays.asList(new Integer[]{})); 
    // Chord 2
    expected.add(Arrays.asList(new Integer[]{2, 0})); 
    // Chord 3
    expected.add(Arrays.asList(new Integer[]{})); 
    // Chord 4
    expected.add(Arrays.asList(new Integer[]{3, 2, 0}));
    // Chord 5
    expected.add(Arrays.asList(new Integer[]{})); 
    // chord 6
    expected.add(Arrays.asList(new Integer[]{3})); 
    // Chord 7
    expected.add(Arrays.asList(new Integer[]{3, 4, 1})); 
    // Chord 8
    expected.add(Arrays.asList(new Integer[]{}));
    // Chords 9-14
    expected.add(Arrays.asList(new Integer[]{3, 2, 1}));
    expected.add(Arrays.asList(new Integer[]{3, 2, 1}));
    expected.add(Arrays.asList(new Integer[]{3, 2, 1}));
    expected.add(Arrays.asList(new Integer[]{3, 2, 1}));
    expected.add(Arrays.asList(new Integer[]{3, 2, 1}));
    expected.add(Arrays.asList(new Integer[]{3, 2, 1}));
    // Chord 15
    expected.add(Arrays.asList(new Integer[]{}));
	  
    // For each chord: calculate the actual sustained pitches and add them to actual
    List<List<Integer>> actual = new ArrayList<List<Integer>>();
    List<List<Note>> transcriptionChords = transcription.getChords();
    Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
    List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
    int lowestNoteIndex = 0;
    for (int i = 0; i < transcriptionChords.size(); i++) {
   	  List<Note> currentTranscriptionChord = transcriptionChords.get(i);
   	  actual.add(featureGeneratorChord.getVoicesOfSustainedPreviousNotesInChordMUSCI(basicNoteProperties, 
   	  	allVoiceLabels, lowestNoteIndex));
   	  lowestNoteIndex += currentTranscriptionChord.size();
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


	@Test
	public void testGenerateConstantChordFeatureVector() {
    Tablature tablature = new Tablature(encodingTestpiece1);
    Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);
    
    // Determine expected
    List<List<Double>> expected = new ArrayList<List<Double>>();
    Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
    int largestChordSizeTraining = tablature.getLargestTablatureChord();
    int highestNumberOfVoicesTraining = transcription.getNumberOfVoices();
  	int lowestNoteIndex = 0;
    for (int i = 0; i < transcription.getChords().size(); i++) {
    	List<Double> currentExpected = new ArrayList<Double>();
    	List<List<Double>> individual = featureGeneratorChord.getIndividualNoteFeaturesChord(basicTabSymbolProperties,
    		null,	largestChordSizeTraining, lowestNoteIndex, true);
    	for (List<Double> l : individual) {
    		currentExpected.addAll(l);
    	}
      List<Double> shared = featureGeneratorChord.getSharedNoteFeaturesChord(basicTabSymbolProperties, null, 
      	null, largestChordSizeTraining, highestNumberOfVoicesTraining, lowestNoteIndex);
    	currentExpected.addAll(shared);
    	expected.add(currentExpected);
    	lowestNoteIndex += transcription.getChords().get(i).size();		
    }
      	
    // Calculate actual
  	List<List<Double>> actual = new ArrayList<List<Double>>();
  	lowestNoteIndex = 0;
    for (int i = 0; i < tablature.getChords().size(); i++) {
    	actual.add(featureGeneratorChord.generateConstantChordFeatureVector(basicTabSymbolProperties, null, null,
    		largestChordSizeTraining, highestNumberOfVoicesTraining, lowestNoteIndex, true));
    	lowestNoteIndex += tablature.getChords().get(i).size();
    }

    // Assert equality
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
    	assertEquals(expected.get(i).size(), actual.get(i).size());
    	for (int j= 0; j < expected.get(i).size(); j++) {
    		assertEquals(expected.get(i).get(j), actual.get(i).get(j));
    	}
    }
    assertEquals(expected, actual); 
	}


	@Test
	public void testGenerateConstantChordFeatureVectorNonTab() {    
    Transcription transcription = new Transcription(midiTestpiece1);

    // Determine expected
    List<List<Double>> expected = new ArrayList<List<Double>>();
    Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
    int largestChordSizeTraining = transcription.getLargestTranscriptionChord();
    List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
    int highestNumberOfVoicesTraining = transcription.getNumberOfVoices();
  	int lowestNoteIndex = 0;
    for (int i = 0; i < transcription.getChords().size(); i++) {
    	List<Double> currentExpected = new ArrayList<Double>();
    	List<List<Double>> individual = featureGeneratorChord.getIndividualNoteFeaturesChord(null, basicNoteProperties, 
    		largestChordSizeTraining, lowestNoteIndex, false);
    	for (List<Double> l : individual) {
    		currentExpected.addAll(l);
    	}
      List<Double> shared = featureGeneratorChord.getSharedNoteFeaturesChord(null, basicNoteProperties,  
      	allVoiceLabels, largestChordSizeTraining, highestNumberOfVoicesTraining, lowestNoteIndex);
    	currentExpected.addAll(shared);
    	expected.add(currentExpected);
    	lowestNoteIndex += transcription.getChords().get(i).size();		
    }
      	
    // Calculate actual
  	List<List<Double>> actual = new ArrayList<List<Double>>();
  	lowestNoteIndex = 0;
    for (int i = 0; i < transcription.getChords().size(); i++) {
    	actual.add(featureGeneratorChord.generateConstantChordFeatureVector(null, basicNoteProperties,
      		allVoiceLabels, largestChordSizeTraining, highestNumberOfVoicesTraining, lowestNoteIndex, false));
    	lowestNoteIndex += transcription.getChords().get(i).size();
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


	@Test
	public void testGenerateVariableChordFeatureVector() {
		Tablature tablature = new Tablature(encodingTestpiece1);
		Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

		List<List<Double>> expected = new ArrayList<List<Double>>();
		for (int i = 0; i < tablature.getChords().size(); i++) {
			expected.add(new ArrayList<Double>());
		}
		Integer[][] btp = tablature.getBasicTabSymbolProperties();
		int highestNumberOfVoices = transcription.getNumberOfVoices();
		List<List<Integer>> voiceAssignments = transcription.getVoiceAssignments(/*tablature,*/ highestNumberOfVoices);
		List<List<Double>> voiceLabels = transcription.getVoiceLabels();
		int lowestNoteIndex = 0;
		for (int i = 0; i < tablature.getChords().size(); i++) {
			List<Integer> currentVoiceAssignment = voiceAssignments.get(i);
			List<Integer> currentPitchesInChord = 
				Tablature.getPitchesInChord(btp, lowestNoteIndex); 
			List<List<Double>> currentVoiceLabelsChord = 
				LabelTools.getChordVoiceLabels(currentVoiceAssignment, mnv); 	  			
			List<List<Integer>> currentVoicesInChord = 
				LabelTools.getVoicesInChord(currentVoiceLabelsChord);

			List<Double> currentExpected = new ArrayList<Double>();
			// 1. Add proximities and movements
			double[] currentAverageProximitiesAndMovements = 
				featureGeneratorChord.getAverageProximitiesAndMovementsOfChord(btp, null, transcription,
				/*highestNumberOfVoices,*/ lowestNoteIndex, currentVoiceAssignment, mnv);
			for (double d : currentAverageProximitiesAndMovements) {
				currentExpected.add(d);
			}
			// 2. Add pitch-voice relation
			double currentPitchVoiceRelation = FeatureGeneratorChord.getPitchVoiceRelationInChord(btp,
				null, voiceLabels, lowestNoteIndex, currentVoiceAssignment, mnv);
			currentExpected.add(currentPitchVoiceRelation);
			// 3. Add voice crossing information
			List<List<Integer>> currentVoiceCrossingInformation = 
				Transcription.getVoiceCrossingInformationInChord(currentPitchesInChord, currentVoicesInChord);
			// a. Number of voice crossing pairs
			currentExpected.add((currentVoiceCrossingInformation.get(1).size() / 2.0));
			// b. The sum of the pitch distances between the voice crossing pairs
			currentExpected.add((double)ToolBox.sumListInteger(currentVoiceCrossingInformation.get(2)));
			// c. The average distance between the voice crossing pairs
			if (currentVoiceCrossingInformation.get(2).size() == 0) {
				currentExpected.add(0.0);
			}
			else {
				currentExpected.add((double)ToolBox.sumListInteger(currentVoiceCrossingInformation.get(2)) /
					currentVoiceCrossingInformation.get(2).size());
			}	
			// 4. Add voice assignment
			for (int v : currentVoiceAssignment) {
				currentExpected.add((double) v);
			}
			expected.set(i, currentExpected);
			lowestNoteIndex += tablature.getChords().get(i).size();
		}

		// For each chord: calculate the actual variable chord feature vector and add it to actual
		List<List<Double>> actual = new ArrayList<List<Double>>();
		lowestNoteIndex = 0;
		for (int i = 0; i < tablature.getChords().size(); i++) {
			List<Integer> currentVoiceAssignment = voiceAssignments.get(i);
			actual.add(featureGeneratorChord.generateVariableChordFeatureVector(btp, null, 
				transcription, lowestNoteIndex, highestNumberOfVoices, currentVoiceAssignment, mnv));
			lowestNoteIndex += tablature.getChords().get(i).size();
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
		assertEquals(expected, actual);
	}


	@Test
	public void testGenerateVariableChordFeatureVectorNonTab() {
		Transcription transcription = new Transcription(midiTestpiece1);

		List<List<Integer>> voiceAssignments = getVoiceAssignmentsNonTab();

		// For each chord: determine expected
		List<List<Double>> expected = new ArrayList<List<Double>>(); 
		for (int i = 0; i < transcription.getChords().size(); i++) {
			expected.add(new ArrayList<Double>());
		}
		Integer[][] bnp = transcription.getBasicNoteProperties();
		List<List<Double>> voiceLabels = transcription.getVoiceLabels();
		int highestNumberOfVoicesTraining = transcription.getNumberOfVoices();
		int lowestNoteIndex = 0;
		for (int i = 0; i < transcription.getChords().size(); i++) {
			List<Integer> currentVoiceAssignment = voiceAssignments.get(i);
			List<Integer> currentNewPitchesInChord = Transcription.getPitchesInChord(bnp, 
				lowestNoteIndex); 
			List<List<Double>> currentVoiceLabelsChord = 
				LabelTools.getChordVoiceLabels(currentVoiceAssignment, mnv); 	  			
			List<List<Integer>> currentNewVoicesInChord = 
				LabelTools.getVoicesInChord(currentVoiceLabelsChord);
			List<List<Integer>> currentAllPitchesAndVoicesInChord = 
				Transcription.getAllPitchesAndVoicesInChord(bnp, currentNewPitchesInChord,
				currentNewVoicesInChord, voiceLabels, lowestNoteIndex);
			// voicesInChord must be a List<List>>
			List<Integer> currentPitchesInChord = currentAllPitchesAndVoicesInChord.get(0);
			List<List<Integer>> currentVoicesInChord = new ArrayList<List<Integer>>();
			for (int j : currentAllPitchesAndVoicesInChord.get(1)) {
				int currentVoice = j;
				List<Integer> voiceWrapped = Arrays.asList(new Integer[]{currentVoice});
				currentVoicesInChord.add(voiceWrapped);
			}

			List<Double> currentExpected = new ArrayList<Double>();
			// 1. Add proximities and movements
			double[] currentAverageProximitiesAndMovements = 
				featureGeneratorChord.getAverageProximitiesAndMovementsOfChord(null, bnp, transcription, 
				/*highestNumberOfVoicesTraining,*/ lowestNoteIndex, currentVoiceAssignment, mnv);
			for (double d : currentAverageProximitiesAndMovements) {
				currentExpected.add(d);
			}
			// 2. Add pitch-voice relation
			double currentPitchVoiceRelation = FeatureGeneratorChord.getPitchVoiceRelationInChord(null, bnp,
				voiceLabels, lowestNoteIndex, currentVoiceAssignment, mnv);
			currentExpected.add(currentPitchVoiceRelation);
			// 3. Add voice crossing information
			List<List<Integer>> currentVoiceCrossingInformation = 
				Transcription.getVoiceCrossingInformationInChord(currentPitchesInChord, currentVoicesInChord);
			// a. Number of voice crossing pairs
			currentExpected.add((currentVoiceCrossingInformation.get(1).size() / 2.0));
			// b. The sum of the pitch distances between the voice crossing pairs
			currentExpected.add((double)ToolBox.sumListInteger(currentVoiceCrossingInformation.get(2)));
			// c. The average distance between the voice crossing pairs
			if (currentVoiceCrossingInformation.get(2).size() == 0) {
				currentExpected.add(0.0);
			}
			else {
				currentExpected.add((double)ToolBox.sumListInteger(currentVoiceCrossingInformation.get(2)) /
					currentVoiceCrossingInformation.get(2).size());
			}	
			// 4. Add voice assignment
			for (int v : currentVoiceAssignment) {
				currentExpected.add((double) v);
			}
			expected.set(i, currentExpected);
			lowestNoteIndex += transcription.getChords().get(i).size();
		}

		// For each chord: calculate the actual variable chord feature vector and add it to actual
		List<List<Double>> actual = new ArrayList<List<Double>>();
		lowestNoteIndex = 0;
		for (int i = 0; i < transcription.getChords().size(); i++) {
			List<Integer> currentVoiceAssignment = voiceAssignments.get(i);
			actual.add(featureGeneratorChord.generateVariableChordFeatureVector(null, bnp, transcription,
				lowestNoteIndex, highestNumberOfVoicesTraining,	currentVoiceAssignment, mnv));
			lowestNoteIndex += transcription.getChords().get(i).size();
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j), actual.get(i).get(j));
			}
		}
		assertEquals(expected, actual);
	}


	@Test
	public void testGenerateAllCompleteChordFeatureVectors() {
    Tablature tablature = new Tablature(encodingTestpiece1);
    Transcription transcription = new Transcription(midiTestpiece1, encodingTestpiece1);

  	int highestNumberOfVoicesTraining = transcription.getNumberOfVoices();
  	Integer[][] basicTabSymbolProperties = tablature.getBasicTabSymbolProperties();
  	List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
  	List<List<List<Integer>>> allOrderedVoiceAssignments = 
  		FeatureGeneratorChord.getOrderedVoiceAssignments(basicTabSymbolProperties, null, allVoiceLabels,
  		highestNumberOfVoicesTraining, mnv);
 	  
  	// Determine expected
 	  List<List<List<Double>>> expected = new ArrayList<List<List<Double>>>();
 	  int largestChordSizeTraining = tablature.getLargestTablatureChord();
 	  int lowestNoteIndex = 0;
  	// For each chord
 	  for (int i = 0; i < tablature.getChords().size(); i++) {
  		List<List<Integer>> currentOrderedVoiceAssignments = allOrderedVoiceAssignments.get(i);
  		List<List<Double>> currentExpectedChord = new ArrayList<List<Double>>();
  		// For each voice assignment
  		for (int j = 0; j < currentOrderedVoiceAssignments.size(); j++) {
  			List<Double> currentExpected = new ArrayList<Double>();
  			List<Integer> currentVoiceAssignment = currentOrderedVoiceAssignments.get(j);
  	    // 1. Determine the constant chord feature vector
  			List<Double> constantChordFeatureVector = new ArrayList<Double>();
  			List<List<Double>> individualOnsetFeaturesChord = 
  	    	featureGeneratorChord.getIndividualNoteFeaturesChord(basicTabSymbolProperties, null, largestChordSizeTraining,
  	    	lowestNoteIndex, true);
  			for (int k = 0; k < individualOnsetFeaturesChord.size(); k++) {
  				constantChordFeatureVector.addAll(individualOnsetFeaturesChord.get(k));
  			}
  			List<Double> sharedOnsetFeaturesChord = featureGeneratorChord.getSharedNoteFeaturesChord(basicTabSymbolProperties,
  				null,	null, largestChordSizeTraining, highestNumberOfVoicesTraining, lowestNoteIndex);
  			constantChordFeatureVector.addAll(sharedOnsetFeaturesChord);
  		  // 2. Determine the variable chord feature vector
  			List<Double> variableChordFeatureVector =	featureGeneratorChord.generateVariableChordFeatureVector(basicTabSymbolProperties,
  				null, transcription, lowestNoteIndex, highestNumberOfVoicesTraining, currentVoiceAssignment, mnv);
  			// 3. Construct the complete chord feature vector and add it to currentExpectedChord
  			currentExpected.addAll(constantChordFeatureVector);
  			currentExpected.addAll(variableChordFeatureVector);			
  			currentExpectedChord.add(currentExpected);
  		}
      expected.add(currentExpectedChord);
      lowestNoteIndex += tablature.getChords().get(i).size();
  	}
  	
  	// Calculate actual
 	  List<List<List<Double>>> actual = featureGeneratorChord.generateAllCompleteChordFeatureVectorsMUSCI(basicTabSymbolProperties, 
  		null, transcription, largestChordSizeTraining, highestNumberOfVoicesTraining, true, mnv);
    
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
	  assertEquals(expected, actual);
	}


	@Test
	public void testGenerateAllCompleteChordFeatureVectorsNonTab() {
 	 Transcription transcription = new Transcription(midiTestpiece1);

  	int highestNumberOfVoicesTraining = transcription.getNumberOfVoices();
  	Integer[][] basicNoteProperties = transcription.getBasicNoteProperties();
  	List<List<Double>> allVoiceLabels = transcription.getVoiceLabels();
  	List<List<List<Integer>>> allOrderedVoiceAssignments = 
 	  	FeatureGeneratorChord.getOrderedVoiceAssignments(null, basicNoteProperties, allVoiceLabels,
 	  	highestNumberOfVoicesTraining, mnv);
 	  
  	// For each chord: determine the expected complete chord feature vectors and add them to expected
 	  List<List<List<Double>>> expected = new ArrayList<List<List<Double>>>();
 	  int largestChordSizeTraining = transcription.getLargestTranscriptionChord();
 	  int lowestOnsetIndex = 0;
  	// For each chord
 	  for (int i = 0; i < transcription.getChords().size(); i++) {
  		List<List<Integer>> currentOrderedVoiceAssignments = allOrderedVoiceAssignments.get(i);
  		List<List<Double>> currentExpectedChord = new ArrayList<List<Double>>();
  		// For each voice assignment
  		for (int j = 0; j < currentOrderedVoiceAssignments.size(); j++) {
  			List<Double> currentExpected = new ArrayList<Double>();
  			List<Integer> currentVoiceAssignment = currentOrderedVoiceAssignments.get(j);
  	    // 1. Determine the constant chord feature vector
  			List<Double> constantChordFeatureVector = new ArrayList<Double>();
  			List<List<Double>> individualOnsetFeaturesChord = 
  	    	featureGeneratorChord.getIndividualNoteFeaturesChord(null, basicNoteProperties, largestChordSizeTraining,
  	    	lowestOnsetIndex, true);
  			for (int k = 0; k < individualOnsetFeaturesChord.size(); k++) {
  				constantChordFeatureVector.addAll(individualOnsetFeaturesChord.get(k));
  			}
  			List<Double> sharedOnsetFeaturesChord = featureGeneratorChord.getSharedNoteFeaturesChord(null, 
  				basicNoteProperties, allVoiceLabels, largestChordSizeTraining, highestNumberOfVoicesTraining, 
  				lowestOnsetIndex);
  			constantChordFeatureVector.addAll(sharedOnsetFeaturesChord);
  		  // 2. Determine the variable chord feature vector
  			List<Double> variableChordFeatureVector =	featureGeneratorChord.generateVariableChordFeatureVector(null,
  				basicNoteProperties, transcription, lowestOnsetIndex, highestNumberOfVoicesTraining, 
  				currentVoiceAssignment, mnv);
  			// 3. Construct the complete chord feature vector and add it to currentExpectedChord
  			currentExpected.addAll(constantChordFeatureVector);
  			currentExpected.addAll(variableChordFeatureVector);			
  			currentExpectedChord.add(currentExpected);
  		}
      expected.add(currentExpectedChord);
      lowestOnsetIndex += transcription.getChords().get(i).size();
  	}
  	
  	// For each chord: calculate the actual complete chord feature vectors 
  	List<List<List<Double>>> actual = featureGeneratorChord.generateAllCompleteChordFeatureVectorsMUSCI(null, 
  		basicNoteProperties, transcription, largestChordSizeTraining, highestNumberOfVoicesTraining, true, mnv);
    
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
		assertEquals(expected, actual);
	}

}
