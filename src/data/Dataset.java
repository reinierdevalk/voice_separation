package data;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import conversion.imports.MIDIImport;
import conversion.imports.TabImport;
import external.Tablature;
import external.Transcription;
import internal.core.Encoding;
import internal.core.Encoding.Stage;
import tools.ToolBox;
import tools.text.StringTools;
import ui.Runner;
import ui.Runner.ModellingApproach;

public class Dataset implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String datasetID;
	private String name;
	private List<String> piecenames;
	private List<String> subDatasetIDs;
	private int numVoices;
	private boolean isTablatureSet;
	private boolean isTabAsNonTabSet;
	private int largestChordSize;
	private int highestNumVoices;
	private List<File> allEncodingFiles;
	private List<File> allMidiFiles;
	private List<Tablature> allTablatures;
	private List<Transcription> allTranscriptions;

	// Dataset naming
	// Each Dataset has a datasetID that consists of three elements, separated by dashes: <a>-<b>-<n>vv. 
	// - tablature datasets have an element of COMPOSITIONS (surrounded by dashes, e.g., 
	//   "-int-") or "-tab" in their datasetID
	// - tab-as-non-tab datasets have "_ANT" in their datasetID
	// - non-tablature datasets have neither in their datasetID
	// - the datasetID must end with "-<n>vv"
	// The Dataset name is the datasetID without the "-<n>vv"
	// a. Tablature datasets
	public static final String THESIS_INT_3VV = "thesis-int-3vv"; // TODO thesis-int
	public static final String THESIS_INT_4VV = "thesis-int-4vv";
	public static final String THESIS_INT_5VV = "thesis-int-5vv";
	public static final String JOSQUIN_INT_3VV = "josquin-int-3vv";
	public static final String JOSQUIN_INT_4VV = "josquin-int-4vv";	
	public static final String BYRD_INT_4VV = "byrd-int-4vv";
	public static final String ADRIAENSSEN_INT_4VV = "adriaenssen-int-4vv";
	public static final String D_MBS_1512_INT_3VV = "d_mbs_1512-int-3vv";
	public static final String THESIS_INT_IMI_4VV = "thesis-int_imi-4vv";
	public static final String THESIS_INT_IMI_SHORT_4VV = "thesis-int_imi_short-4vv";
	public static final String THESIS_INT_SEMI_4VV = "thesis-int_semi-4vv";
	public static final String THESIS_INT_FREE_4VV = "thesis-int_free-4vv";
	public static final String THESIS_INT_FREE_MORE_4VV = "thesis-int_free_more-4vv";
	public static final String ALL_INT_3VV = "all-int-3vv";
	public static final String ALL_INT_4VV = "all-int-4vv";
	public static final String USER_TAB = "user-tab";
	// b. Tab-as-non-tab datasets
	public static final String THESIS_INT_ANT_3VV = "thesis-int_ANT-3vv";
	public static final String THESIS_INT_ANT_4VV = "thesis";
	public static final String THESIS_INT_ANT_5VV = "thesis-int_ANT-5vv";
	public static final String BYRD_INT_ANT_4VV = "byrd-int_ANT-4vv";
	public static final String THESIS_INT_IMI_ANT_4VV = "thesis-int_imi_ANT-4vv";
	public static final String THESIS_INT_IMI_SHORT_ANT_4VV = "thesis-int_imi_short_ANT-4vv";
	public static final String THESIS_INT_SEMI_ANT_4VV = "thesis-int_semi_ANT-4vv";
	public static final String THESIS_INT_FREE_ANT_4VV = "thesis-int_free_ANT-4vv";
	public static final String THESIS_INT_FREE_MORE_ANT_4VV = "thesis-int_free_more_ANT-4vv";
	// c. Non-tablature datasets
	public static final String BACH_INV_2VV = "bach-inv-2vv";
	public static final String BACH_INV_3VV = "bach-inv-3vv";
	public static final String BACH_WTC_2VV = "bach-WTC-2vv";
	public static final String BACH_WTC_3VV = "bach-WTC-3vv";
	public static final String BACH_WTC_4VV = "bach-WTC-4vv";
	public static final String BACH_WTC_5VV = "bach-WTC-5vv";
	public static final String USER_MIDI = "user-MIDI";
	// d. Test and user
	public static final String TEST = "test";
	public static final String USER = "user";
	public static final String TEST_TAB = "test-tab";
	public static final String TEST_MIDI = "test-MIDI";
	
	private static final List<String> TAB_COMPOSITIONS = Arrays.asList(new String[]{
		"int", "fnt", "rcr"}); // intabulation, fantasia, ricercare
	private static final List<String> COMPOSITIONS = Arrays.asList(new String[]{
		"inv"}); // invention

	private static final Map<String, List<List<String>>> ALL_PIECENAMES = new LinkedHashMap<String, List<List<String>>>();
	static {		
		ALL_PIECENAMES.put(THESIS_INT_3VV, Arrays.asList(getThesisInt3vv(), null));
		ALL_PIECENAMES.put(THESIS_INT_4VV, Arrays.asList(getThesisInt4vv(), null));
		ALL_PIECENAMES.put(THESIS_INT_5VV, Arrays.asList(getThesisInt5vv(), null));
		ALL_PIECENAMES.put(JOSQUIN_INT_3VV, Arrays.asList(getJosquinInt3vv(), null));
		ALL_PIECENAMES.put(JOSQUIN_INT_4VV, Arrays.asList(getJosquinInt4vv(), null));
		ALL_PIECENAMES.put(BYRD_INT_4VV, Arrays.asList(getByrdInt4vv(), null));
		ALL_PIECENAMES.put(ADRIAENSSEN_INT_4VV, Arrays.asList(getAdriaenssenInt4vv(), null));
		ALL_PIECENAMES.put(D_MBS_1512_INT_3VV, Arrays.asList(getDMbs1512Int3vv(), null));
//		ALL_PIECENAMES.put(TEST_TAB, getTest());
		ALL_PIECENAMES.put(THESIS_INT_ANT_3VV, Arrays.asList(getThesisInt3vv(), null));
		ALL_PIECENAMES.put(THESIS_INT_ANT_4VV, Arrays.asList(getThesisInt4vv(), null));
		ALL_PIECENAMES.put(THESIS_INT_ANT_5VV, Arrays.asList(getThesisInt5vv(), null));
		ALL_PIECENAMES.put(BYRD_INT_ANT_4VV, Arrays.asList(getByrdInt4vv(), null));
		ALL_PIECENAMES.put(ALL_INT_3VV, Arrays.asList(getAllInt3vv(), Arrays.asList(THESIS_INT_3VV, JOSQUIN_INT_3VV, D_MBS_1512_INT_3VV)));
		ALL_PIECENAMES.put(ALL_INT_4VV, Arrays.asList(getAllInt4vv(), Arrays.asList(THESIS_INT_4VV, JOSQUIN_INT_4VV, BYRD_INT_4VV, ADRIAENSSEN_INT_4VV)));
		ALL_PIECENAMES.put(BACH_WTC_2VV, Arrays.asList(getBachWTC2vv(), null));
		ALL_PIECENAMES.put(BACH_WTC_3VV, Arrays.asList(getBachWTC3vv(), null));
		ALL_PIECENAMES.put(BACH_WTC_4VV, Arrays.asList(getBachWTC4vv(), null));
		ALL_PIECENAMES.put(BACH_WTC_5VV, Arrays.asList(getBachWTC5vv(), null));
		ALL_PIECENAMES.put(BACH_INV_2VV, Arrays.asList(getBachInv2vv(), null));
		ALL_PIECENAMES.put(BACH_INV_3VV, Arrays.asList(getBachInv3vv(), null));
		ALL_PIECENAMES.put(TEST_MIDI, Arrays.asList(getTest(), null));
		ALL_PIECENAMES.put(USER_TAB, Arrays.asList(null, null));
		ALL_PIECENAMES.put(USER_MIDI, Arrays.asList(null, null));
		ALL_PIECENAMES.put(TEST_TAB, Arrays.asList(null, null));
	}


	public static void setUserPiecenames(String key, List<String> argPiecenames) {
		ALL_PIECENAMES.put(key, Arrays.asList(argPiecenames, null));
	}


	public static void main(String[] args) {
	}


	public Dataset() {		
	}


	public Dataset(String id, boolean argIsTablatureSet) {
		// Variables initialised and set here
		this.datasetID = id;
		this.name = id.substring(0, id.lastIndexOf("-")); // id w/o voice information
		this.numVoices = Integer.parseInt(
			id.substring(id.lastIndexOf("-") + 1, id.lastIndexOf("-") + 2)
		);
		List<List<String>> pieceNamesSubsets = ALL_PIECENAMES.get(
			id.startsWith("user") || id.startsWith("test") ? name : id
		);
		this.piecenames = pieceNamesSubsets.get(0);
		this.subDatasetIDs = pieceNamesSubsets.get(1);
		if (argIsTablatureSet) {
			this.isTablatureSet = true;
			this.isTabAsNonTabSet = false;
		}
		else if (id.contains("_ANT")) {
			this.isTablatureSet = false;
			this.isTabAsNonTabSet = true;
		}
		else {
			this.isTablatureSet = false;
			this.isTabAsNonTabSet = false;
		}

		// Variables initialised here and set in populateDataset()
		largestChordSize = -1;
		highestNumVoices = -1;
		if (argIsTablatureSet) {
			allEncodingFiles = new ArrayList<>();
			allTablatures = new ArrayList<>();
		}
		allMidiFiles = new ArrayList<>();
		allTranscriptions = new ArrayList<>();
	}


	public void populateDataset(Map<String, String> paths, boolean deployTrainedUserModel) {
		boolean isTablatureCase = isTablatureSet();
		boolean isTabAsNonTab = isTabAsNonTabSet();

		List<String> subdatasetIDs = getSubDatasetIDs();
		List<Dataset> subdatasets = new ArrayList<>(); // only the main Dataset if not a combined Dataset
		if (subdatasetIDs == null) {
			subdatasets.add(this);
		}
		else {
			for (String id : subdatasetIDs) {
				subdatasets.add(new Dataset(id, isTablatureCase));
			}
		}

		// Set paths
		List<String> argEncodingsPaths = new ArrayList<>(); // only tablature case (if !deployTrainedUserModel and if deployTrainedUserModel)
		List<String> argMidiPaths = new ArrayList<>(); // only non-tablature case (if !deployTrainedUserModel and if deployTrainedUserModel)
		List<String> argTabMidiPaths = new ArrayList<>(); // tablature case; non-tablature case if isTabAsNonTab (if !deployTrainedUserModel; else ?)
//		String argEncodingsPath = null; 
//		String argMidiPath = null;
//		String argTabMidiPath = null;
		if (!deployTrainedUserModel) {
			String numVoices = getNumVoices() + Runner.voices;
			String ep = paths.get("ENCODINGS_PATH");
			String mp = paths.get("MIDI_PATH");
			for (Dataset sds : subdatasets) {
				String n = sds.getName();
				if (isTablatureCase) {
					argEncodingsPaths.add(StringTools.getPathString(Arrays.asList(ep.replace("{dataset}", n), numVoices)));
					argTabMidiPaths.add(StringTools.getPathString(Arrays.asList(mp.replace("{dataset}", n), numVoices)));
				}
				else {
					argMidiPaths.add(StringTools.getPathString(Arrays.asList(mp.replace("{dataset}", n), numVoices)));
					if (isTabAsNonTab) {
						argTabMidiPaths.add(StringTools.getPathString(Arrays.asList(mp.replace("{dataset}", n), numVoices)));
					}
				}
			}
//			argEncodingsPath = StringTools.getPathString(
//				Arrays.asList(paths.get("ENCODINGS_PATH").replace("{dataset}", name), numVoices)
//			);
//			if (name.equals("bach-inv") || name.equals("bach-WTC")) {
//				name += "/thesis";
//			}
//			argMidiPath = StringTools.getPathString(
//				Arrays.asList(paths.get("MIDI_PATH").replace("{dataset}", name), numVoices)
//			);
//			argTabMidiPath = StringTools.getPathString(
//				Arrays.asList(paths.get("MIDI_PATH").replace("{dataset}", name), numVoices)
//			);
		}
		else {
			argEncodingsPaths.add(StringTools.getPathString(
				Arrays.asList(paths.get("POLYPHONIST_PATH"), paths.get("IN_DIR"))
			));
			argMidiPaths.add(StringTools.getPathString(
				Arrays.asList(paths.get("POLYPHONIST_PATH"), paths.get("IN_DIR"))
			));
//			argEncodingsPath = StringTools.getPathString(
//				Arrays.asList(paths.get("POLYPHONIST_PATH"), paths.get("IN_DIR"))
//			);
//			argMidiPath = StringTools.getPathString(
//				Arrays.asList(paths.get("POLYPHONIST_PATH"), paths.get("IN_DIR"))
//			);
		}

//		System.out.println(argEncodingsPaths);
//		System.out.println(argTabMidiPaths);
//		System.out.println(argMidiPaths);
//		System.exit(0);

		// Set remaining class variables
		for (int i = 0; i < subdatasets.size(); i++) {
			Dataset sds = subdatasets.get(i);
			List<String> piecenames = sds.getPiecenames();
			String argEncodingsPath = null;
			String argMidiPath = null;
			String argTabMidiPath = null;
			if (isTablatureCase) {
				argEncodingsPath = argEncodingsPaths.get(i);
				if (!deployTrainedUserModel) {
					argTabMidiPath = argTabMidiPaths.get(i);
				}
			}
			else {
				argMidiPath = argMidiPaths.get(i);
				if (isTabAsNonTab) {
					argTabMidiPath = argTabMidiPaths.get(i);
				}
			}

			for (String currPiecename : piecenames) {
//			for (String currPiecename : getPiecenames()) {
				System.out.println("... creating " + currPiecename + " ...");
				// Get Tablature and Transcription
				String currPiecenameNoExt = ToolBox.splitExt(currPiecename)[0];
				File currEncodingFile = null; // only used if !deployTrainedUserModel
				File currMidiFile = null;
				Tablature currTablature = null;
				Transcription currTranscription = null;
				if (isTablatureCase) {
					// In the model dev case, currEncodingFile always exists as .tbp, and currTablature 
					// can be created from it
					if (!deployTrainedUserModel) {
						currEncodingFile = new File(argEncodingsPath + currPiecename);
						currTablature = new Tablature(currEncodingFile, true);
					}
					// In the real-world case, currEncodingFile may not exist as .tbp, and currTablature must
					// be created from the raw encoding obtained from converting another format into .tbp 
					else {
						String rawEncoding = TabImport.convertToTbp(argEncodingsPath, currPiecename, paths);
						Encoding e = new Encoding(rawEncoding, currPiecenameNoExt, Stage.RULES_CHECKED);
						currTablature = new Tablature(e, true);
					}
					if (!deployTrainedUserModel) {
						currMidiFile = new File(argTabMidiPath + currPiecenameNoExt + MIDIImport.MID_EXT);
					}
				}
				else {
					currMidiFile = new File(argMidiPath + currPiecenameNoExt + MIDIImport.MID_EXT);
					if (isTabAsNonTab) {
						currMidiFile = new File(argTabMidiPath + currPiecenameNoExt + MIDIImport.MID_EXT);
					}
				}
				currTranscription = 
					!deployTrainedUserModel ? new Transcription(true, currMidiFile, currEncodingFile) : null;

//				if (isTabAsNonTab) {
//					int currInterval = 0;
//					if (currentPieceName.equals("ochsenkun-1558-absolon_fili-shorter")) {
//						currInterval = 7;
//					}
//					else if (currentPieceName.equals("rotta-1546-bramo_morir")) {
//						currInterval = -2;
//					}
//					else if (currentPieceName.equals("phalese-1563-las_on")) {
//						currInterval = -2;
//					}
//					currTranscription.transposeNonTab(currInterval);
//				}
	
				// Get largest chord size and number of voices
				int currLargestChordSize =
					isTablatureCase ? currTablature.getLargestTablatureChord() :
					currTranscription.getLargestTranscriptionChord();
				int currNumVoices =
					!deployTrainedUserModel ? currTranscription.getNumberOfVoices() : 
					currLargestChordSize; // TODO this assumption might not always hold true
	
				// Add to lists
				if (isTablatureCase) {
					allEncodingFiles.add(currEncodingFile);
					allTablatures.add(currTablature);
				}
			 	allMidiFiles.add(currMidiFile);
				allTranscriptions.add(currTranscription);
				if (currLargestChordSize > largestChordSize) {
					largestChordSize = currLargestChordSize;
				}
				if (currNumVoices > highestNumVoices) {
					highestNumVoices = currNumVoices;
				}
			}
		}
	}


	public String getDatasetID() {
		return datasetID;
	}


	public String getName() {
		return name;
	}

	public void setName(String s) {
		this.name = s;
	}

	public List<String> getPiecenames() {
		return piecenames;
	}

	public List<String> getSubDatasetIDs() {
		return subDatasetIDs;
	}

//	public void addPiecenames(List<String> l) {
//		for (String s : l) {
//			piecenames.add(s);
//		}
//	}

	public int getNumPieces() {
		return piecenames.size();
	}

	public int getNumVoices() {
		return numVoices;
	}

	public void setNumVoices(int i) {
		this.numVoices= i;
	}

	public static boolean isTablatureSet(String id) {
		// If id contains "-tab" or any of the elements of dashesAdded
		List<String> dashesAdded = new ArrayList<>();
		TAB_COMPOSITIONS.forEach(s -> dashesAdded.add("-" + s + "-"));
		if (id.contains("-tab") || dashesAdded.stream().anyMatch(id::contains)) {
			return true; 
		}
		else {
			return false;
		}
	}

	public boolean isTablatureSet() {
		return isTablatureSet;
	}


	public boolean isTabAsNonTabSet() {
		return isTabAsNonTabSet;
	}


	public int getLargestChordSize() {
		return largestChordSize;
	}

	
	public int getHighestNumVoices() {
		return highestNumVoices;
	}

	public List<File> getAllEncodingFiles() {
		return allEncodingFiles;
	}

	public List<File> getAllMidiFiles() {
		return allMidiFiles; 
	}

	public List<Tablature> getAllTablatures() {
		return allTablatures; 
	}
	
	public List<Transcription> getAllTranscriptions() {
		return allTranscriptions; 
	}


	// TESTED
	public int getNumDataExamples(ModellingApproach ma) {
		return ToolBox.sumListInteger(getIndividualPieceSizes(ma));
	}


	/**
	 * Gets the size of the individual pieces in the dataset. Depending on the given 
	 * ModellingApproach, size is measured either as the number of notes or as the 
	 * number of chords.
	 * 
	 * @param ma
	 * @return
	 */
	// TESTED
	public List<Integer> getIndividualPieceSizes(ModellingApproach ma) {
		List<Integer> sizes = new ArrayList<Integer>();
		// Tablature case
		if (isTablatureSet()) {
			List<Tablature> allTabs = getAllTablatures();
			for (Tablature t : allTabs) {
				if (ma == ModellingApproach.N2N) {
					sizes.add(t.getNumberOfNotes());
				}
				else if (ma == ModellingApproach.C2C || ma == ModellingApproach.HMM) {
					sizes.add(t.getChords().size());
				}
			}
		}
		// Non-tablature case
		else {
			List<Transcription> allTrans = getAllTranscriptions();
			for (Transcription t : allTrans) {
				if (ma == ModellingApproach.N2N) {
					sizes.add(t.getNumberOfNotes());
				}
				else if (ma == ModellingApproach.C2C || ma == ModellingApproach.HMM) {
					sizes.add(t.getChords().size());
				}
			}
		}	
		return sizes;
	}


	private List<Integer> getBeginIndices(ModellingApproach ma) {
		List<Integer> beginIndices = new ArrayList<Integer>();

		int totalSize = 0;
		for (int i : getIndividualPieceSizes(ma)) {
			int currSize = i;
			totalSize += currSize;
			beginIndices.add(totalSize - currSize);
		}
		return beginIndices;
	}

	
	private static List<String> getAdriaenssenInt4vv() { // CHECKED
		return Arrays.asList(new String[]{
			"abran-tant_vous_allez.tbp",
			"berchem-o_s_io.tbp",
			"costeley-la_terre_les.tbp",
			"ferabosco-io_mi_son.tbp",
			"lasso-appariran_per_me.tbp",
			"lasso-avecque_vous.tbp",
			"lasso-madonna_mia_pieta.tbp",
			"lasso-poi_che_l.tbp",
			"rore-anchor_che_col.tbp"
		});
	}


	private static List<String> getByrdInt4vv() { // CHECKED
		return Arrays.asList(new String[]{
			"byrd-ah_golden_hairs.tbp",
			"byrd-an_aged_dame.tbp",
			"byrd-as_caesar_wept.tbp",
			"byrd-blame_i_confess.tbp",
//			"byrd-delight_is_dead.tbp", // skipped for some reason
			"byrd-in_angels_weed.tbp",
//			"byrd-in_tower_most.tbp", // inference piece
			"byrd-o_lord_bow.tbp",
			"byrd-o_that_we.tbp",
			"byrd-quis_me_statim.tbp",
			"byrd-rejoyce_unto_the.tbp",
			"byrd-sith_death.tbp",
			"byrd-the_lord_is.tbp",
			"byrd-the_man_is.tbp",
			"byrd-while_phoebus.tbp"
		});
	}


	private static List<String> getDMbs1512Int3vv() { // CHECKED
		return Arrays.asList(new String[]{
			"D-Mbs_Mus.ms._1512_17r.tbp",
			"D-Mbs_Mus.ms._1512_18r.tbp",
			"D-Mbs_Mus.ms._1512_20v.tbp",
			"D-Mbs_Mus.ms._1512_21r.tbp",
			"D-Mbs_Mus.ms._1512_21v-22r.tbp",
			"D-Mbs_Mus.ms._1512_22v-23r.tbp",
			"D-Mbs_Mus.ms._1512_24v-25r.tbp",
			"D-Mbs_Mus.ms._1512_25v-26r.tbp",
			"D-Mbs_Mus.ms._1512_26v.tbp",
//			"D-Mbs_Mus.ms._1512_29r.tbp", // A kept in tab where it has duo w/ S; omitted elsewhere
//			"D-Mbs_Mus.ms._1512_29v-30r.tbp", // A kept in tab where it has duo w/ S; omitted elsewhere
			"D-Mbs_Mus.ms._1512_38v-39r.tbp",
			"D-Mbs_Mus.ms._1512_39v-40r.tbp"
		});
	}


	private static List<String> getJosquinInt2vv() { // CHECKED
		return Arrays.asList(new String[]{
			"3584_001_pleni_missa_hercules_josquin.tbp",
			"3585_002_benedictus_de_missa_pange_lingua_josquin.tbp",
			"3591_008_fecit_potentiam_josquin.tbp",
			"4965_01b_per_illud_ave_josquin.tbp",
			"5254_03_benedicta_es_coelorum_desprez-2.tbp",
			"5702_benedicta-2.tbp"
		});
	}


	private static List<String> getJosquinInt3vv() { // CHECKED
		return Arrays.asList(new String[]{
			"4438_07_la_plus_des_plus.tbp",
			"5107_11_misa_de_faysan_regres_pleni.tbp",
		});
	}


	private static List<String> getJosquinInt4vv() { // CHECKED
		return Arrays.asList(new String[]{
			"5265_14_absalon_fili_me_desprez.tbp",
			"5263_12_in_exitu_israel_de_egipto_desprez-1.tbp",
			"5263_12_in_exitu_israel_de_egipto_desprez-2.tbp",
			"5263_12_in_exitu_israel_de_egipto_desprez-3.tbp", // NB: rounding problems if not using epsilon in Transcription.checkAlignment()!
			"4465_33-34_memor_esto-1.tbp",
			"4465_33-34_memor_esto-2.tbp", // NB: rounding problems if not using epsilon in Transcription.checkAlignment()!
			"1274_12_qui_habitat_in_adjutorio-1.tbp",
			"1274_12_qui_habitat_in_adjutorio-2.tbp",
			"5264_13_qui_habitat_in_adjutorio_desprez-1.tbp",
			"5264_13_qui_habitat_in_adjutorio_desprez-2.tbp",
			//
			"4471_40_cum_sancto_spiritu.tbp",
			"5266_15_cum_sancto_spiritu_desprez.tbp",
			"5106_10_misa_de_faysan_regres_2_gloria.tbp",
			"5189_16_sanctus_and_hosanna_from_missa_faisant_regrets-1.tbp",
			"5189_16_sanctus_and_hosanna_from_missa_faisant_regrets-2.tbp",		
			"5188_15_sanctus_and_hosanna_from_missa_hercules-1.tbp",
			"5188_15_sanctus_and_hosanna_from_missa_hercules-2.tbp",
			"5190_17_cum_spiritu_sanctu_from_missa_sine_nomine.tbp",
			//
			"4400_45_ach_unfall_was.tbp",
			"4481_49_ach_unfal_wes_zeigst_du_mich.tbp",
			"4406_51_adieu_mes_amours.tbp",
			"4467_37_adieu_mes_amours.tbp", // m is too low
			"1025_adieu_mes_amours.tbp",
			"1030_coment_peult_avoir_joye.tbp",		
			"5191_18_mille_regres.tbp",
			"4482_50_mille_regrets_P.tbp",
//			"4469_39_plus_nulz_regrets_P.tbp", // EXCLUDED: chord at bar 57 has 3* MIDI pitch 60 --> remove from Altus
//			"922_milano_098_que_voulez_vous_dire_de_moi.tbp" // EXCLUDED: m_a < 0.80
		});
	}


	private static List<String> getJosquinInt5vv() { // CHECKED
		return Arrays.asList(new String[]{
			"933_milano_109_stabat_mater_dolorosa_josquin.tbp",
			"1275_13_faulte_d_argent.tbp",
			"3638_061_lauda_sion_gombert_T.tbp",
			"3643_066_credo_de_beata_virgine_jospuin_T-1.tbp",
			"3643_066_credo_de_beata_virgine_jospuin_T-2.tbp",
			"5148_51_respice_in_me_deus._F#_lute_T.tbp",
			"5255_04_stabat_mater_dolorosa_desprez-1.tbp",
			"5255_04_stabat_mater_dolorosa_desprez-2.tbp",
			"5256_05_inviolata_integra_desprez-1.tbp",
			"5256_05_inviolata_integra_desprez-2.tbp",
			"5256_05_inviolata_integra_desprez-3.tbp",
			"5260_09_date_siceram_morentibus_sermisy.tbp"
		});
	}


	private static List<String> getJosquinInt6vv() { // CHECKED
		return Arrays.asList(new String[]{
			"932_milano_108_pater_noster_josquin-1.tbp",
			"932_milano_108_pater_noster_josquin-2.tbp",
			"3647_070_benedicta_est_coelorum_josquin_T.tbp",
			"3649_072_praeter_rerum_seriem_josquin_T.tbp",
			"4964_01a_benedictum_es_coelorum_josquin.tbp",
			"4966_01c_nunc_mater_josquin.tbp",
			"5252_01_pater_noster_desprez-1.tbp",
			"5252_01_pater_noster_desprez-2.tbp",
			"5253_02_praeter_rerum_seriem_desprez-1.tbp",
			"5253_02_praeter_rerum_seriem_desprez-2.tbp",
			"5254_03_benedicta_es_coelorum_desprez-1.tbp",
			"5254_03_benedicta_es_coelorum_desprez-3.tbp",
			"5694_03_motet_praeter_rerum_seriem_josquin-1.tbp",
			"5694_03_motet_praeter_rerum_seriem_josquin-2.tbp",
			"5702_benedicta-1.tbp",
			"5702_benedicta-3.tbp"
		});
	}


	private static List<String> getThesisInt3vv() { // CHECKED
		return Arrays.asList(new String[]{
			"newsidler-1536_7-disant_adiu.tbp",
			"newsidler-1536_7-mess_pensees.tbp",
			"pisador-1552_7-pleni_de.tbp",
			"judenkuenig-1523_2-elslein_liebes.tbp",
			"newsidler-1544_2-nun_volget.tbp",
			"phalese-1547_7-tant_que-3vv.tbp"
		});
	}


	private static List<String> getThesisInt4vv() { // CHECKED
		return Arrays.asList(new String[]{
			"ochsenkun-1558_5-absolon_fili.tbp",
			"ochsenkun-1558_5-in_exitu.tbp",
			"ochsenkun-1558_5-qui_habitat.tbp",
			"rotta-1546_15-bramo_morir.tbp",
			"phalese-1547_7-tant_que-4vv.tbp",
			"ochsenkun-1558_5-herr_gott.tbp",
			"abondante-1548_1-mais_mamignone.tbp",
			"phalese-1563_12-las_on.tbp",
			"barbetta-1582_1-il_nest.tbp",
//			"barbetta-1582_1-il_nest-corrected.tbp" // incorrect version used for thesis
		});
	}


	private static List<String> getThesisInt5vv() { // CHECKED
		return Arrays.asList(new String[]{
			"adriansen-1584_6-d_vn_si.tbp",
			"ochsenkun-1558_5-inuiolata_integra.tbp"
		});
	}


	private static List<String> getAllInt3vv() {
		List<String> comb = new ArrayList<String>(getThesisInt3vv());
		comb.addAll(getJosquinInt3vv());
		comb.addAll(getDMbs1512Int3vv());
		return comb;
	}


	private static List<String> getAllInt4vv() {
		List<String> comb = new ArrayList<String>(getThesisInt4vv());
		comb.addAll(getJosquinInt4vv());
		comb.addAll(getByrdInt4vv());
		comb.addAll(getAdriaenssenInt4vv());
		return comb;
	}


	private static List<String> getBachInv2vv() { 
		return Arrays.asList(new String[]{
			"bach-inv-inventio_1-BWV_772.mid",
			"bach-inv-inventio_2-BWV_773.mid",
			"bach-inv-inventio_3-BWV_774.mid",
			"bach-inv-inventio_4-BWV_775.mid",
			"bach-inv-inventio_5-BWV_776.mid",
			"bach-inv-inventio_6-BWV_777.mid",
			"bach-inv-inventio_7-BWV_778.mid",
			"bach-inv-inventio_8-BWV_779.mid",
			"bach-inv-inventio_9-BWV_780.mid",
			"bach-inv-inventio_10-BWV_781.mid",
			"bach-inv-inventio_11-BWV_782.mid",
			"bach-inv-inventio_12-BWV_783.mid",
			"bach-inv-inventio_13-BWV_784.mid",
			"bach-inv-inventio_14-BWV_785.mid",
			"bach-inv-inventio_15-BWV_786.mid"
		});
	}


	private static List<String> getBachInv3vv() { 
		return Arrays.asList(new String[]{
			"bach-inv-inventio_1-BWV_787",
			"bach-inv-inventio_2-BWV_788",
			"bach-inv-inventio_3-BWV_789",
			"bach-inv-inventio_4-BWV_790",
			"bach-inv-inventio_5-BWV_791",
			"bach-inv-inventio_6-BWV_792",
			"bach-inv-inventio_7-BWV_793",
			"bach-inv-inventio_8-BWV_794",
			"bach-inv-inventio_9-BWV_795",
			"bach-inv-inventio_10-BWV_796",
			"bach-inv-inventio_11-BWV_797",
			"bach-inv-inventio_12-BWV_798",
			"bach-inv-inventio_13-BWV_799",
			"bach-inv-inventio_14-BWV_800",
			"bach-inv-inventio_15-BWV_801"	
		});
	}


	private static List<String> getBachWTC2vv() { 
		return Arrays.asList(new String[]{	
			"bach-WTC1-fuga_10-BWV_855_1",
			"bach-WTC1-fuga_10-BWV_855_2"	
		});
	}


	private static List<String> getBachWTC3vv() { 
		return Arrays.asList(new String[]{
			"bach-WTC1-fuga_2-BWV_847",
			"bach-WTC1-fuga_3-BWV_848",
			"bach-WTC1-fuga_6-BWV_851",
			"bach-WTC1-fuga_7-BWV_852",
			"bach-WTC1-fuga_8-BWV_853",
			"bach-WTC1-fuga_9-BWV_854",
			"bach-WTC1-fuga_11-BWV_856",
			"bach-WTC1-fuga_13-BWV_858",
			"bach-WTC1-fuga_15-BWV_860",
			"bach-WTC1-fuga_19-BWV_864",
			"bach-WTC1-fuga_21-BWV_866",
			"bach-WTC2-fuga_1-BWV_870",
			"bach-WTC2-fuga_3-BWV_872",
			"bach-WTC2-fuga_4-BWV_873",
			"bach-WTC2-fuga_6-BWV_875",
			"bach-WTC2-fuga_10-BWV_879",
			"bach-WTC2-fuga_11-BWV_880",
			"bach-WTC2-fuga_12-BWV_881",
			"bach-WTC2-fuga_13-BWV_882",
			"bach-WTC2-fuga_14-BWV_883",
			"bach-WTC2-fuga_15-BWV_884",
			"bach-WTC2-fuga_18-BWV_887",
			"bach-WTC2-fuga_19-BWV_888",
			"bach-WTC2-fuga_20-BWV_889",
			"bach-WTC2-fuga_21-BWV_890",
			"bach-WTC2-fuga_24-BWV_893"
		});
	}


	public static List<String> getBachWTC4vv() { 
		return Arrays.asList(new String[]{
			"bach-WTC1-fuga_1-BWV_846",
			"bach-WTC1-fuga_5-BWV_850",
			"bach-WTC1-fuga_12-BWV_857",
			"bach-WTC1-fuga_14-BWV_859",
			"bach-WTC1-fuga_16-BWV_861",
			"bach-WTC1-fuga_17-BWV_862",
			"bach-WTC1-fuga_18-BWV_863",
			"bach-WTC1-fuga_20-BWV_865",
			"bach-WTC1-fuga_23-BWV_868",
			"bach-WTC1-fuga_24-BWV_869",
			"bach-WTC2-fuga_2-BWV_871",
			"bach-WTC2-fuga_5-BWV_874",
			"bach-WTC2-fuga_7-BWV_876",
			"bach-WTC2-fuga_8-BWV_877",
			"bach-WTC2-fuga_9-BWV_878",
			"bach-WTC2-fuga_16-BWV_885",
			"bach-WTC2-fuga_17-BWV_886",
			"bach-WTC2-fuga_22-BWV_891",
			"bach-WTC2-fuga_23-BWV_892"
		});
	}


	public static List<String> getBachWTC5vv() { 
		return Arrays.asList(new String[]{
			// Thesis: 1-57, 58-115
//			"bach-WTC1-fuga_4-BWV_849_1",
//			"bach-WTC1-fuga_4-BWV_849_2",
			// Thesis: 1-37, 38-75 
//			"bach-WTC1-fuga_22-BWV_867_1",
//			"bach-WTC1-fuga_22-BWV_867_2",

			// ISMIR 2018
			// Initial split in four equal parts: 0-330, 331-660 (from b. 41 3/4), 661-991 
			// (from b. 65 1/2), 992-1321 (from b. 88 3/4); final split at closest points
			// where theme or motif enters in 5vv texture: from bb. 44, 65, 86 
			"bach-WTC1-fuga_4-BWV_849-split_at_44-65-86_1", 
			"bach-WTC1-fuga_4-BWV_849-split_at_44-65-86_2",
			"bach-WTC1-fuga_4-BWV_849-split_at_44-65-86_3",
			"bach-WTC1-fuga_4-BWV_849-split_at_44-65-86_4",
			// Initial split in two equal parts: 0-376, 377-753 (from b. 42), final split at 
			// closest points where theme or motif enters in 5vv texture: b. 37 
			"bach-WTC1-fuga_22-BWV_867-split_at_37_1",
			"bach-WTC1-fuga_22-BWV_867-split_at_37_2",
			// Initial split in three equal parts: 0-251, 252-502 (from b. 28 3/4), 503-753 
			// (from b. 52 1/2); final split at closest points where theme or motif enters 
			// in 5vv texture: b. 32, 53 (experimented with but not used)
//			"bach-WTC1-fuga_22-BWV_867-split_at_32-53_1",
//			"bach-WTC1-fuga_22-BWV_867-split_at_32-53_2",
//			"bach-WTC1-fuga_22-BWV_867-split_at_32-53_3",		
			// Other splits (not used)
//			"bach-WTC1-fuga_4-BWV_849_1-exact",
//			"bach-WTC1-fuga_4-BWV_849_2-exact",	
//			"bach-WTC1-fuga_4-BWV_849_3-exact",
//			"bach-WTC1-fuga_4-BWV_849-split_at_65_1",       // two same-size parts, beginning of bar 
//			"bach-WTC1-fuga_4-BWV_849-split_at_65_2",       // two same-size parts, beginning of bar 
//			"bach-WTC1-fuga_4-BWV_849-split_at_41-65-88_1", // four same size parts, beginning of bar
//			"bach-WTC1-fuga_4-BWV_849-split_at_41-65-88_2", // four same size parts, beginning of bar 
//			"bach-WTC1-fuga_4-BWV_849-split_at_41-65-88_3", // four same size parts, beginning of bar
//			"bach-WTC1-fuga_4-BWV_849-split_at_41-65-88_4", // four same size parts, beginning of bar
//			"bach-WTC1-fuga_22-BWV_867_1-exact",
//			"bach-WTC1-fuga_22-BWV_867_2-exact"
//			"bach-WTC1-fuga_22-BWV_867-split_at_42_1",      // two same-size parts, beginning of bar 
//			"bach-WTC1-fuga_22-BWV_867-split_at_42_2",      // two same-size parts, beginning of bar
//			"bach-WTC1-fuga_22-BWV_867-split_at_28-52_1",   // three same-size parts, beginning of bar
//			"bach-WTC1-fuga_22-BWV_867-split_at_28-52_2",   // three same-size parts, beginning of bar
//			"bach-WTC1-fuga_22-BWV_867-split_at_28-52_3",   // three same-size parts, beginning of bar
		});
	}


	private static List<String> getTest() { 
		return Arrays.asList(new String[]{
			"testpiece", 
			"testpiece", 
			"testpiece"	
		});
	}


	// TODO check if the obsolete naming in the methods below is used in experiments that have not
	// yet been made reproducable (ISMIR 2013, Early Music, ...)
	private static List<String> getBachTwoVoiceFugues() {
		List<String> bachFuguesTwo = new ArrayList<String>();
		bachFuguesTwo.add("Bach - WTC1, Fuga 10 in e minor (BWV 855) (1)");
		bachFuguesTwo.add("Bach - WTC1, Fuga 10 in e minor (BWV 855) (2)");
		return bachFuguesTwo;
	}

	private static List<String> getBachThreeVoiceFugues() {
		List<String> bachFuguesThree = new ArrayList<String>();
		bachFuguesThree.add("Bach - WTC1, Fuga 2 in c minor (BWV 847)");
		bachFuguesThree.add("Bach - WTC1, Fuga 3 in C# major (BWV 848)");
		bachFuguesThree.add("Bach - WTC1, Fuga 6 in d minor (BWV 851)");
		bachFuguesThree.add("Bach - WTC1, Fuga 7 in Eb major (BWV 852)");
		bachFuguesThree.add("Bach - WTC1, Fuga 8 in d# minor (BWV 853)");
		bachFuguesThree.add("Bach - WTC1, Fuga 9 in E major (BWV 854)");
		bachFuguesThree.add("Bach - WTC1, Fuga 11 in F major (BWV 856)");
		bachFuguesThree.add("Bach - WTC1, Fuga 13 in F# major (BWV 858)");
		bachFuguesThree.add("Bach - WTC1, Fuga 15 in G major (BWV 860)");
		bachFuguesThree.add("Bach - WTC1, Fuga 19 in A major (BWV 864)");
		bachFuguesThree.add("Bach - WTC1, Fuga 21 in Bb major (BWV 866)");
		bachFuguesThree.add("Bach - WTC2, Fuga 1 in C major (BWV 870)");
		bachFuguesThree.add("Bach - WTC2, Fuga 3 in C# major (BWV 872)");
		bachFuguesThree.add("Bach - WTC2, Fuga 4 in c# minor (BWV 873)");
		bachFuguesThree.add("Bach - WTC2, Fuga 6 in d minor (BWV 875)");
		bachFuguesThree.add("Bach - WTC2, Fuga 10 in e minor (BWV 879)");
		bachFuguesThree.add("Bach - WTC2, Fuga 11 in F major (BWV 880)");
		bachFuguesThree.add("Bach - WTC2, Fuga 12 in f minor (BWV 881)");
		bachFuguesThree.add("Bach - WTC2, Fuga 13 in F# major (BWV 882)");
		bachFuguesThree.add("Bach - WTC2, Fuga 14 in f# minor (BWV 883)");
		bachFuguesThree.add("Bach - WTC2, Fuga 15 in G major (BWV 884)");
		bachFuguesThree.add("Bach - WTC2, Fuga 18 in g# minor (BWV 887)");
		bachFuguesThree.add("Bach - WTC2, Fuga 19 in A major (BWV 888)");
		bachFuguesThree.add("Bach - WTC2, Fuga 20 in a minor (BWV 889)");
		bachFuguesThree.add("Bach - WTC2, Fuga 21 in Bb major (BWV 890)");
		bachFuguesThree.add("Bach - WTC2, Fuga 24 in b minor (BWV 893)");
		return bachFuguesThree;
	}


	private static List<String> getBachFourVoiceFugues() {
		List<String> bachFuguesFour = new ArrayList<String>();
		bachFuguesFour.add("Bach - WTC1, Fuga 1 in C major (BWV_846)");	
		bachFuguesFour.add("Bach - WTC1, Fuga 5 in D major (BWV 850)");
		bachFuguesFour.add("Bach - WTC1, Fuga 12 in f minor (BWV 857)");
		bachFuguesFour.add("Bach - WTC1, Fuga 14 in f# minor (BWV 859)");
		bachFuguesFour.add("Bach - WTC1, Fuga 16 in g minor (BWV 861)");
		bachFuguesFour.add("Bach - WTC1, Fuga 17 in Ab major (BWV 862)");
		bachFuguesFour.add("Bach - WTC1, Fuga 18 in g# minor (BWV 863)");
		bachFuguesFour.add("Bach - WTC1, Fuga 20 in a minor (BWV 865)");
		bachFuguesFour.add("Bach - WTC1, Fuga 23 in B major (BWV 868)");
		bachFuguesFour.add("Bach - WTC1, Fuga 24 in b minor (BWV 869)");	
		bachFuguesFour.add("Bach - WTC2, Fuga 2 in c minor (BWV 871)");
		bachFuguesFour.add("Bach - WTC2, Fuga 5 in D major (BWV 874)");
		bachFuguesFour.add("Bach - WTC2, Fuga 7 in Eb major (BWV 876)");
		bachFuguesFour.add("Bach - WTC2, Fuga 8 in d# minor (BWV 877)");
		bachFuguesFour.add("Bach - WTC2, Fuga 9 in E major (BWV 878)");
		bachFuguesFour.add("Bach - WTC2, Fuga 16 in g minor (BWV 885)");
		bachFuguesFour.add("Bach - WTC2, Fuga 17 in Ab major (BWV 886)");
		bachFuguesFour.add("Bach - WTC2, Fuga 22 in bb minor (BWV 891)");
		bachFuguesFour.add("Bach - WTC2, Fuga 23 in B major (BWV 892)");
		return bachFuguesFour;
	}

	private static List<String> getBachFiveVoiceFugues() {
		List<String> bachFuguesFive = new ArrayList<String>();
		bachFuguesFive.add("Bach - WTC1, Fuga 4 in c# minor (BWV 849) (1)");
		bachFuguesFive.add("Bach - WTC1, Fuga 4 in c# minor (BWV 849) (2)");
		bachFuguesFive.add("Bach - WTC1, Fuga 22 in bb minor (BWV 867) (1)");
		bachFuguesFive.add("Bach - WTC1, Fuga 22 in bb minor (BWV 867) (2)");
		return bachFuguesFive;
	}

	private static List<String> getBachTwoVoiceInventions() {
		List<String> bachInventionsTwo = new ArrayList<String>();
		bachInventionsTwo.add("Bach - Inventio 1 in C major (BWV 772)");
		bachInventionsTwo.add("Bach - Inventio 2 in c minor (BWV 773)");
		bachInventionsTwo.add("Bach - Inventio 3 in D major (BWV 774)");
		bachInventionsTwo.add("Bach - Inventio 4 in d minor (BWV 775)");
		bachInventionsTwo.add("Bach - Inventio 5 in Eb major (BWV 776)");
		bachInventionsTwo.add("Bach - Inventio 6 in E major (BWV 777)");
		bachInventionsTwo.add("Bach - Inventio 7 in e minor (BWV 778)");
		bachInventionsTwo.add("Bach - Inventio 8 in F major (BWV 779)");
		bachInventionsTwo.add("Bach - Inventio 9 in f minor (BWV 780)");
		bachInventionsTwo.add("Bach - Inventio 10 in G major (BWV 781)");
		bachInventionsTwo.add("Bach - Inventio 11 in g minor (BWV 782)");
		bachInventionsTwo.add("Bach - Inventio 12 in A major (BWV 783)");
		bachInventionsTwo.add("Bach - Inventio 13 in a minor (BWV 784)");
		bachInventionsTwo.add("Bach - Inventio 14 in Bb major (BWV 785)");
		bachInventionsTwo.add("Bach - Inventio 15 in b minor (BWV 786)");
		return bachInventionsTwo;
	}

	private static List<String> getBachThreeVoiceInventions() {
		List<String> bachInventionsThree = new ArrayList<String>();
		bachInventionsThree.add("Bach - Sinfonia 1 in C Major (BWV 787)");
		bachInventionsThree.add("Bach - Sinfonia 2 in c minor (BWV 788)");
		bachInventionsThree.add("Bach - Sinfonia 3 in D Major (BWV 789)");
		bachInventionsThree.add("Bach - Sinfonia 4 in d minor (BWV 790)");
		bachInventionsThree.add("Bach - Sinfonia 5 in Eb Major (BWV 791)");
		bachInventionsThree.add("Bach - Sinfonia 6 in E Major (BWV 792)");
		bachInventionsThree.add("Bach - Sinfonia 7 in e minor (BWV 793)");
		bachInventionsThree.add("Bach - Sinfonia 8 in F Major (BWV 794)");
		bachInventionsThree.add("Bach - Sinfonia 9 in f minor (BWV 795)");
		bachInventionsThree.add("Bach - Sinfonia 10 in G Major (BWV 796)");
		bachInventionsThree.add("Bach - Sinfonia 11 in g minor (BWV 797)");
		bachInventionsThree.add("Bach - Sinfonia 12 in A Major (BWV 798)");
		bachInventionsThree.add("Bach - Sinfonia 13 in a minor (BWV 799)");
		bachInventionsThree.add("Bach - Sinfonia 14 in Bb Major (BWV 800)");
		bachInventionsThree.add("Bach - Sinfonia 15 in b minor (BWV 801)");
		return bachInventionsThree;
	}
	
	
	private static final String DATASET_ID = "dataset ID";
	private static DatasetID[] ALL_DATASET_IDS = new DatasetID[50];
	public enum DatasetID {
		TAB_INT_3VV("thesis-int-3vv", 0, true, false, getThesisInt3vv()),
		TAB_INT_4VV("thesis-int-4vv", 1, true, false, getThesisInt4vv()),  
		TAB_INT_5VV("thesis-int-5vv", 2, true, false, getThesisInt5vv()), 
		
		TAB_INT_ANT_3VV("thesis-int_ANT-3vv", 3, false, true, getThesisInt3vv()),
		TAB_INT_ANT_4VV("thesis-int_ANT-4vv", 4, false, true, getThesisInt4vv()),
		TAB_INT_ANT_5VV("thesis-int_ANT-5vv", 5, false, true, getThesisInt5vv()),
		
		BACH_WTC_2VV("bach-WTC-2vv", 6, false, false, getBachWTC2vv()), 
		BACH_WTC_3VV("bach-WTC-3vv", 7, false, false, getBachWTC3vv()), 
		BACH_WTC_4VV("bach-WTC-4vv", 8, false, false, getBachWTC4vv()), 
		BACH_WTC_5VV("bach-WTC-5vv", 9, false, false, getBachWTC5vv()), 
		BACH_INV_2VV("bach-inv-2vv", 10, false, false, getBachInv2vv()), 
		BACH_INV_3VV("bach-inv-3vv", 11, false, false, getBachInv3vv()),
		
		TAB_INT_IMI_4VV("thesis-int-imi-4vv", 12, true, false, null), 
		TAB_INT_IMI_SHORT_4VV("thesis-int-imi_short-4vv", 13, true, false, null), 
		TAB_INT_SEMI_4VV("thesis-int-semi-4vv", 14, true, false, null), 
		TAB_INT_FREE_4VV("thesis-int-free-4vv", 15, true, false, null), 
		TAB_INT_FREE_MORE_4VV("thesis-int-free_more-4vv", 16, true, false, null), 
		
		TAB_INT_IMI_ANT_4VV("thesis-int-imi_ANT-4vv", 17, false, true, null), 
		TAB_INT_IMI_SHORT_ANT_4VV("thesis-int-imi_short_ANT-4vv", 18, false, true, null),
		TAB_INT_SEMI_ANT_4VV("thesis-int-semi_ANT-4vv", 19, false, true, null),
		TAB_INT_FREE_ANT_4VV("thesis-int-free_ANT-4vv", 20, false, true, null),
		TAB_INT_FREE_MORE_ANT_4VV("thesis-int-free_more_ANT-4vv", 21, false, true, null),
		
		TAB_TEST("tab-test", 22, true, false, getTest()),
		TEST("test", 23, false, false, getTest()),
		
		TAB_USER("tab-user", 24, true, false, null),
		USER("user", 25, false, false, null),
		
		JOSQUIN_INT_3VV("josquin-int-3vv", 26, true, false, getJosquinInt3vv()),
		JOSQUIN_INT_4VV("josquin-int-4vv", 27, true, false, getJosquinInt4vv()),
		
		BYRD_INT_4VV("byrd-int-4vv", 28, true, false, getByrdInt4vv()),
		BYRD_INT_ANT_4VV("byrd-int_ANT-4vv", 29, false, true, getByrdInt4vv()),
		
		ALL_INT_3VV("all-int-3vv", 30, true, false, getAllInt3vv()),
		ALL_INT_4VV("all-int-4vv", 31, true, false, getAllInt4vv());

		private int intRep;
		private String stringRep;
		private boolean isTablatureSet;
		private boolean isTabAsNonTabSet;
		private int numVoices ;
		private String name;
		private String nameWithVv;
		private List<String> pieceNames;
		DatasetID(String s, int i, boolean isTab, boolean isTabAsNonTab, List<String> argPieceNames) {
			this.stringRep = s;
			this.intRep = i;
			this.isTablatureSet = isTab;
			this.isTabAsNonTabSet = isTabAsNonTab;
			this.pieceNames = argPieceNames;
			if (s.endsWith(Runner.voices)) {
				this.numVoices = Integer.valueOf(s.substring(s.lastIndexOf("-") + 1, s.lastIndexOf(Runner.voices)));
			}
			else {
				this.numVoices = 0;
			}
			if (s.endsWith(Runner.voices)) {
				this.name = s.substring(0, s.lastIndexOf("-"));
			}
			else {
				this.name = s;
			}
			this.nameWithVv = s;
			ALL_DATASET_IDS[i] = this;
		}
		
		private String getStringRep() {
			return stringRep;
		}
		
		@Override
	    public String toString() {
	        return getStringRep();
	    }
		
		private boolean isTablatureSet() {
			return isTablatureSet;
		}
		
		private boolean isTabAsNonTabSet() {
			return isTabAsNonTabSet;
		}
				
		private String getName() {
			return name;
		}
		
		private void setName(String arg) {
			name = arg;
		}
		
		private String getNameWithVv() {
			return nameWithVv;
		}
		
		private int getIntRep() {
			return intRep;
		}
		
		private int getNumVoices() {
			return numVoices;
		}
		
		private void setNumVoices(int arg) {
			numVoices = arg;
		}

		private List<String> getPieceNames() {
			return pieceNames;
		}
		
		private void setPieceNames(List<String> arg) {
			pieceNames = arg;
		}
		
		private static DatasetID getDatasetID(String name) {
			for (DatasetID d : ALL_DATASET_IDS) {
				if (d.getNameWithVv().equals(name)) {
					return d;
				}
			}
			return null;
		}
	}
	
	private DatasetID datasetIDOrig;
	private Dataset(DatasetID id) {
		// Set high-level information
		this.datasetIDOrig = id;
		this.piecenames = id.getPieceNames();
		this.numVoices = id.getNumVoices();
		this.isTablatureSet = id.isTablatureSet();
		this.isTabAsNonTabSet = id.isTabAsNonTabSet();	
		this.name = id.getName();
	}


	private DatasetID getDatasetIDOrig() {
		return datasetIDOrig;
	}
}
