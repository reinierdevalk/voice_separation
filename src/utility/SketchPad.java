package utility;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import data.Dataset;
import data.Dataset.DatasetID;
import de.uos.fmt.musitech.data.performance.MidiNote;
import de.uos.fmt.musitech.data.performance.PerformanceNote;
import de.uos.fmt.musitech.data.rendering.RenderingHints;
import de.uos.fmt.musitech.data.score.NotationChord;
import de.uos.fmt.musitech.data.score.NotationStaff;
import de.uos.fmt.musitech.data.score.NotationStaffConnector;
import de.uos.fmt.musitech.data.score.NotationStaffConnector.CType;
import de.uos.fmt.musitech.data.score.NotationSystem;
import de.uos.fmt.musitech.data.score.NotationVoice;
import de.uos.fmt.musitech.data.score.ScoreNote;
import de.uos.fmt.musitech.data.score.ScorePitch;
import de.uos.fmt.musitech.data.structure.Note;
import de.uos.fmt.musitech.data.structure.Piece;
import de.uos.fmt.musitech.data.structure.container.Containable;
import de.uos.fmt.musitech.data.structure.harmony.KeyMarker;
import de.uos.fmt.musitech.data.structure.harmony.KeyMarker.Mode;
import de.uos.fmt.musitech.data.time.TimeSignature;
import de.uos.fmt.musitech.performance.midi.MidiReader;
import de.uos.fmt.musitech.performance.midi.MidiWriter;
import de.uos.fmt.musitech.utility.math.Rational;
import exports.MIDIExport;
import featureExtraction.FeatureGenerator;
import featureExtraction.FeatureGeneratorChord;
import machineLearning.ErrorCalculator;
import machineLearning.ErrorFraction;
import machineLearning.OutputEvaluator;
import python.PythonInterface;
import representations.Tablature;
import representations.Transcription;
import tbp.TabSymbol;
import tools.ToolBox;
import ui.Runner;
import ui.Runner.Model;
import ui.Runner.ProcessingMode;



public class SketchPad {
		
	private final static int SUPERIUS = 0;
	private final static int ALTUS = 1;
	private final static int TENOR = 2;
	private final static int BASSUS = 3;
	

	
	public static boolean addSthToList(List<Integer> arg, int i) {
		arg.add(i);
		if (i == 4) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private static final int ACC = 0;
	private static final int ACC_DUR = 1;
	private static final int SND = 2;
	private static final int CMP = 3;
	private static final int PRC = 4;
	private static final int RCL = 5;
	private static final int AVCO = 6;
	public static final List<String> hoi = Arrays.asList(new String[]{"a", "b", "c"});

	public static String experiment;
	public static DatasetID datasetID;
	public static Model model;
	public static ProcessingMode pm;
	private static void initialize() {
		JFrame jf = new JFrame();
		jf.setSize(717, 354);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// getEncodingWindowMenubar() -->
		JMenuBar mb = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		mb.add(fileMenu);   
		JMenuItem openFile = new JMenuItem("Open"); 
		fileMenu.add(openFile); 
		openFile.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
//				openFileAction();
			}
		});
		JMenuItem saveFile = new JMenuItem("Save");
		fileMenu.add(saveFile);
		saveFile.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
//				saveFileAction();
			}
		});
		// <-- getEncodingWindowMenubar()
		
		// --> getEncodingWindowPanel()
		JPanel encodingWindowPanel = new JPanel();
		encodingWindowPanel.setLayout(null);
		encodingWindowPanel.setSize(new Dimension(586, 413));
		// <-- getEncodingWindowPanel()
		
		jf.setJMenuBar(mb);
		jf.setContentPane(encodingWindowPanel);
				
		JRadioButton btnFwd = new JRadioButton("FWD");
		
		btnFwd.setSelected(true);
		btnFwd.setBounds(569, 55, 109, 23);
		btnFwd.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			// TODO Auto-generated Event stub actionPerformed()
    			pm = ProcessingMode.FWD;
    			System.out.println(pm);
    		}
    	});
		encodingWindowPanel.add(btnFwd);
		
		JRadioButton btnBwd = new JRadioButton("BWD");
		btnBwd.setBounds(569, 81, 109, 23);
		btnBwd.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			// TODO Auto-generated Event stub actionPerformed()
    			pm = ProcessingMode.BWD;
    			System.out.println(pm);
    		}
    	});
		encodingWindowPanel.add(btnBwd);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(btnFwd);
		bg.add(btnBwd);
				
		JLabel lblProcMode = new JLabel("Processing mode");
		lblProcMode.setBounds(454, 50, 109, 32);
		encodingWindowPanel.add(lblProcMode);
		
		JLabel lblModel = new JLabel("Model");
		lblModel.setBounds(454, 16, 74, 23);
		encodingWindowPanel.add(lblModel);
		
		JLabel lblDataset = new JLabel("Dataset");
		lblDataset.setBounds(234, 16, 99, 23);
		encodingWindowPanel.add(lblDataset);
		
		JLabel lblExperiment = new JLabel("Experiment");
		lblExperiment.setBounds(10, 16, 200, 23);
		encodingWindowPanel.add(lblExperiment);
		
		JComboBox<Model> cbMod = new JComboBox<Model>();
		
//		cbMod.setSelectedItem((Model) Model.N);
//		model = (Model) cbMod.getSelectedItem();
//
		model = Model.B_PRIME_STAR;
//		cbMod.setSelectedItem(model);
		
		System.out.println("1. model = " + model);
		List<Model> selMod = new ArrayList<Model>();
		for (Model m : Model.values()) {
			if (!excludeModels().contains(m)) {
				selMod.add(m);
			}
		}
//		JComboBox<Model> cbMod = new JComboBox<Model>(selMod.toArray(new Model[0]));
		cbMod.setModel(new DefaultComboBoxModel(selMod.toArray(new Model[0])));
		cbMod.setBounds(567, 16, 124, 23);
		cbMod.setSelectedIndex(4);
		cbMod.setSelectedItem(model);
		cbMod.addActionListener(new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 Model m = (Model) cbMod.getSelectedItem();
				 model = m;
				 System.out.println(m);
			 }
		});
		System.out.println("2. model = " + model);
		encodingWindowPanel.add(cbMod);
		
		JComboBox<DatasetID> cbDataset = new JComboBox<DatasetID>();
		List<DatasetID> selID = new ArrayList<DatasetID>();
		for (DatasetID id : DatasetID.values()) {
			if (!excludeDatasetIDs().contains(id)) {
				selID.add(id);
			}
		}
//		JComboBox<DatasetID> cbDataset = new JComboBox<DatasetID>(selMod.toArray(new DatasetID[0]));
		Dataset di = null;
		
		cbDataset.setModel(new DefaultComboBoxModel(selID.toArray(new DatasetID[0])));
		cbDataset.setBounds(290, 17, 124, 23);
		cbDataset.addActionListener(new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 DatasetID di = (DatasetID) cbDataset.getSelectedItem();
				 datasetID = di;
				 System.out.println(di);
				 System.out.println(datasetID);
			 }
		});
		encodingWindowPanel.add(cbDataset);
		System.out.println("datasetId = " + datasetID);
		
		JComboBox<String> cbExp = new JComboBox<String>();
		cbExp.setModel(new DefaultComboBoxModel(new String[] {"1", "2.1", "2.2", "3.1", "3.2", "3.3.1", "3.3.2", "4", "other"}));
		cbExp.setBounds(82, 17, 137, 22);
		ActionListener alExp = new ActionListener() {
			 public void actionPerformed(ActionEvent e) {
				 String exp = (String) cbExp.getSelectedItem();
				 experiment = exp;
			 }
		};
		cbExp.addActionListener(alExp);
		encodingWindowPanel.add(cbExp);
		
		JButton btnGo = new JButton("GO!");
		btnGo.setBounds(244, 155, 124, 53);
		encodingWindowPanel.add(btnGo);
		btnGo.addActionListener(new ActionListener()  {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				jf.setVisible(false);
				test();
			}
		});
		
		jf.setTitle("SketchPad");
		jf.setVisible(true);
	}
	
	
	public static void test() {
		System.out.println("Henk zegt OK!");
		System.out.println(model);
		System.out.println(datasetID);
		System.out.println(experiment);
	};
	
	
	public static List<DatasetID> excludeDatasetIDs() {
		List<DatasetID> l = Arrays.asList(new DatasetID[]{
			DatasetID.INT_5vv,
			DatasetID.INT_3vv,
			DatasetID.tINT_4vv,
			DatasetID.tINT_5vv,	
			DatasetID.IMI, 
			DatasetID.IMI_SHORTER, 
			DatasetID.SEMI, 
			DatasetID.FREE, 
			DatasetID.FREE_MORE, 
			DatasetID.IMI_NON_TAB, 
			DatasetID.IMI_SHORTER_NON_TAB, 
			DatasetID.SEMI_NON_TAB,
			DatasetID.FREE_NON_TAB,	
			DatasetID.FREE_MORE_NON_TAB,
			DatasetID.TESTPIECE_SET,
			DatasetID.TESTPIECE_SET_NON_TAB
		});	
		return l;
	}
	
	
	public static List<Model> excludeModels() {
		List<Model> l = Arrays.asList(new Model[]{
				Model.STM,
				Model.LTM,
				Model.MIX,
				Model.PROD,	
				Model.ENS_STM, 
				Model.ENS_LTM 
		});	
		return l;
	}


	public static void main(String[] args) throws IllegalArgumentException, IllegalStateException, InterruptedException, RejectedExecutionException, ExecutionException { 
		String pre = "C:/Users/Reinier/Desktop/tab_reconstr-hector/MIDI/"; 
		pre = "F:/research/data/MIDI/bach-WTC/thesis/4vv/";
		for (String s : Dataset.FUGUES_4VV_NAMES) {
			Transcription tt = new Transcription(new File(pre + s + ".mid"), null);
			System.out.println(tt.getVoiceCrossingInformation(null));
		}
		System.exit(0);
		
		List<Integer[]> mi = new ArrayList<>();
		mi.add(new Integer[]{2, 2, 1, 3});
		mi.add(new Integer[]{3, 4, 4, 5});
		mi.add(new Integer[]{2, 2, 6, 8});
		int factor = 3;
		
		String stretchedMeterInfo = "";
		for (int i = 0; i < mi.size(); i++) {
			Integer[] in = mi.get(i);
			if (i > 0) {
				in[2] = mi.get(i-1)[3] + 1;
			}
			in[3] = in[3] * factor;
			stretchedMeterInfo += in[0] + "/" + in[1] + " (" + in[2] + "-" + in[3] + ")";
			if (i < mi.size()-1) {
				stretchedMeterInfo += "; ";
			}
		}
		System.out.println(stretchedMeterInfo);
		
		System.exit(0);
		
		List<Integer> fromTrans = Arrays.asList(new Integer[]{4, 5, 6, 7, 8, 16, 17, 18, 19, 20, 21, 22, 26, 27, 28, 29, 30, 31, 37, 38, 39, 40, 41, 43, 44, 45, 46, 48, 49, 67, 68, 70, 71, 72, 73, 78, 79, 85, 86, 87, 88, 89, 90, 95, 96, 97, 98, 109, 111, 112, 113, 116, 124, 125, 126, 127, 130, 149, 150, 152, 153, 155, 157, 158, 168, 169, 170, 171, 172, 173, 174, 175, 179, 184, 185, 186, 187, 188, 189, 191, 192, 195, 196, 197, 198, 202, 203, 204, 205, 207, 208, 209, 211, 212, 213, 215, 216, 217, 218, 219, 220, 222, 222, 223, 224, 225, 230, 231, 232, 233, 236, 237, 238, 239, 240, 241, 242, 243, 253, 254, 256, 257, 258, 259, 260, 261, 262, 263, 264, 265, 269, 270, 272, 273, 275, 276, 278, 279, 280, 281, 282, 283, 285, 286, 287, 288, 289, 292, 293, 294, 295, 300, 301, 302, 309, 310, 311, 312, 313, 314, 318, 323, 324, 325, 326, 327, 330, 331, 332, 333, 335, 336, 337, 338, 339, 340, 341, 342, 343, 344, 348, 349, 350, 351, 358, 359, 360, 361, 362, 363, 365, 366, 367, 368, 369, 371, 372, 373, 374, 378, 379, 380, 381, 382, 400, 401, 402, 403, 404, 405, 406, 407, 413, 414, 415, 425, 426, 427, 429, 430, 431, 432, 433, 435, 436, 437, 438, 439, 440, 442, 443, 444, 445, 446, 451, 452, 454, 455, 456, 457, 458, 459, 464, 465, 466, 467, 469, 470, 476, 477, 478, 480, 485, 486, 487, 488, 492, 493, 495, 496, 504, 505, 517, 518, 520, 521, 522, 523, 525, 526, 527, 528, 529, 530, 546, 547, 548, 549, 554, 555, 561, 562, 563, 564, 565, 571, 572, 573, 574, 575, 576, 581, 582, 583, 584, 590, 591, 592, 593, 594, 598, 599, 600, 601, 605, 606, 607, 608, 609, 610, 619, 620, 621, 622, 633, 634, 635, 636, 644, 645, 646, 647, 655, 656, 657, 658, 659, 660, 661, 662, 666, 667, 669, 670, 674, 675, 679, 680, 681, 682, 683, 684, 685, 686, 692, 693, 694, 698, 699, 700, 701, 710, 711, 712, 713, 714, 718, 719, 725, 726, 727, 728, 729, 730, 731, 733, 734, 735, 736, 737, 738, 748, 749, 750, 751, 752, 753, 754, 755, 756, 757, 761, 762, 763, 764, 765, 766, 773, 781, 782, 783, 784, 785, 789, 790, 791, 792, 793, 794, 796, 797, 798, 799, 800, 805, 806, 807, 808, 809, 810, 822, 823, 824, 825, 826, 827, 841, 842, 843, 844, 845, 846, 847, 848, 849, 851, 852, 853, 854, 855, 856, 858, 859, 860, 861, 862, 863, 864, 865, 867, 868, 869, 870, 871, 872, 874, 875, 879, 880, 881, 882, 883, 884, 886, 887, 888, 889, 890, 891, 893, 894, 895, 896, 897, 898, 900, 901, 902, 903, 907, 908, 909, 910, 911, 912, 913, 914, 919, 920, 921, 922, 926, 927, 928, 929, 930, 931, 932, 933, 936, 937, 938, 939, 940, 941, 943, 944, 945, 946, 947, 948, 949, 950, 952, 956, 957, 958, 959, 960, 962, 963, 964, 965, 966, 967, 969, 975, 976, 977, 978, 982, 983, 984, 990, 991, 997, 998, 999, 1000, 1001, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1012, 1013, 1017, 1018, 1019, 1020, 1021, 1022, 1023, 1024, 1026, 1027, 1031, 1032, 1040, 1041, 1042, 1043, 1044, 1045, 1046, 1047, 1049, 1050, 1051, 1052, 1053, 1055, 1056, 1057, 1058, 1059, 1060, 1065, 1066, 1068, 1069, 1070, 1071, 1075, 1076, 1082, 1083, 1085, 1086, 1087, 1088, 1089, 1090, 1092, 1093, 1094, 1095, 1096, 1098, 1099, 1100, 1101, 1102, 1103, 1105, 1106, 1107, 1108, 1109, 1110, 1111, 1112, 1115, 1116, 1118, 1119, 1121, 1122, 1124, 1125, 1126, 1127, 1128, 1129, 1130, 1132, 1133, 1134, 1136, 1137, 1138, 1144, 1145, 1146, 1147, 1148, 1149, 1150, 1152, 1153, 1154, 1155, 1162, 1163, 1164, 1165, 1167, 1174, 1175, 1176, 1177, 1178, 1179, 1187, 1188, 1189, 1190, 1191, 1192, 1194, 1195, 1196, 1197, 1205, 1206, 1207, 1208, 1210, 1211, 1212, 1213, 1214, 1215, 1216, 1222, 1223, 1224, 1225, 1232, 1233, 1234, 1235, 1236, 1240, 1241, 1242, 1243, 1245, 1246, 1247, 1267, 1268, 1268, 1269, 1270, 1271, 1277, 1278, 1279, 1280, 1281, 1282, 1284, 1285, 1296, 1297, 1298, 1299, 1305, 1306, 1307, 1308, 1309, 1310, 1322, 1323, 1324, 1325, 1326, 1328, 1329, 1330, 1331, 1335, 1336, 1337, 1338, 1362, 1363, 1364, 1365, 1379, 1380, 1381, 1382, 1383, 1387, 1395, 1397, 1398, 1399, 1400, 1401, 1408, 1409, 1410, 1411, 1412, 1416, 1417, 1418, 1419, 1420, 1430, 1431, 1432, 1433, 1434, 1435, 1436, 1437, 1438, 1439, 1440, 1441});
		List<Integer> fromTab = Arrays.asList(new Integer[]{4, 5, 6, 7, 8, 16, 17, 18, 19, 20, 21, 22, 26, 27, 28, 29, 30, 31, 37, 38, 39, 40, 41, 43, 44, 45, 46, 48, 49, 67, 68, 70, 71, 72, 73, 78, 79, 85, 86, 87, 88, 89, 90, 95, 96, 97, 98, 109, 111, 112, 113, 116, 124, 125, 126, 127, 130, 149, 150, 152, 153, 155, 157, 158, 168, 169, 170, 171, 172, 173, 174, 175, 179, 184, 185, 186, 187, 188, 189, 191, 192, 195, 196, 197, 198, 202, 203, 204, 205, 207, 208, 209, 211, 212, 213, 215, 216, 217, 218, 219, 220, 222, 223, 224, 225, 230, 231, 232, 233, 236, 237, 238, 239, 240, 241, 242, 243, 253, 254, 256, 257, 258, 259, 260, 261, 262, 263, 264, 265, 269, 270, 272, 273, 275, 276, 278, 279, 280, 281, 282, 283, 285, 286, 287, 288, 289, 292, 293, 294, 295, 300, 301, 302, 309, 310, 311, 312, 313, 314, 318, 323, 324, 325, 326, 327, 330, 331, 332, 333, 335, 336, 337, 338, 339, 340, 341, 342, 343, 344, 348, 349, 350, 351, 358, 359, 360, 361, 362, 363, 365, 366, 367, 368, 369, 371, 372, 373, 374, 378, 379, 380, 381, 382, 400, 401, 402, 403, 404, 405, 406, 407, 413, 414, 415, 425, 426, 427, 429, 430, 431, 432, 433, 435, 436, 437, 438, 439, 440, 442, 443, 444, 445, 446, 451, 452, 454, 455, 456, 457, 458, 459, 464, 465, 466, 467, 469, 470, 476, 477, 478, 480, 485, 486, 487, 488, 492, 493, 495, 496, 504, 505, 517, 518, 520, 521, 522, 523, 525, 526, 527, 528, 529, 530, 546, 547, 548, 549, 554, 555, 561, 562, 563, 564, 565, 571, 572, 573, 574, 575, 576, 581, 582, 583, 584, 590, 591, 592, 593, 594, 598, 599, 600, 601, 605, 606, 607, 608, 609, 610, 619, 620, 621, 622, 633, 634, 635, 636, 644, 645, 646, 647, 655, 656, 657, 658, 659, 660, 661, 662, 666, 667, 669, 670, 674, 675, 679, 680, 681, 682, 683, 684, 685, 686, 692, 693, 694, 698, 699, 700, 701, 710, 711, 712, 713, 714, 718, 719, 725, 726, 727, 728, 729, 730, 731, 733, 734, 735, 736, 737, 738, 748, 749, 750, 751, 752, 753, 754, 755, 756, 757, 761, 762, 763, 764, 765, 766, 773, 781, 782, 783, 784, 785, 789, 790, 791, 792, 793, 794, 796, 797, 798, 799, 800, 805, 806, 807, 808, 809, 810, 822, 823, 824, 825, 826, 827, 841, 842, 843, 844, 845, 846, 847, 848, 849, 851, 852, 853, 854, 855, 856, 858, 859, 860, 861, 862, 863, 864, 865, 867, 868, 869, 870, 871, 872, 874, 875, 879, 880, 881, 882, 883, 884, 886, 887, 888, 889, 890, 891, 893, 894, 895, 896, 897, 898, 900, 901, 902, 903, 907, 908, 909, 910, 911, 912, 913, 914, 919, 920, 921, 922, 926, 927, 928, 929, 930, 931, 932, 933, 936, 937, 938, 939, 940, 941, 943, 944, 945, 946, 947, 948, 949, 950, 952, 956, 957, 958, 959, 960, 962, 963, 964, 965, 966, 967, 969, 975, 976, 977, 978, 982, 983, 984, 990, 991, 997, 998, 999, 1000, 1001, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1012, 1013, 1017, 1018, 1019, 1020, 1021, 1022, 1023, 1024, 1026, 1027, 1031, 1032, 1040, 1041, 1042, 1043, 1044, 1045, 1046, 1047, 1049, 1050, 1051, 1052, 1053, 1055, 1056, 1057, 1058, 1059, 1060, 1065, 1066, 1068, 1069, 1070, 1071, 1075, 1076, 1082, 1083, 1085, 1086, 1087, 1088, 1089, 1090, 1092, 1093, 1094, 1095, 1096, 1098, 1099, 1100, 1101, 1102, 1103, 1105, 1106, 1107, 1108, 1109, 1110, 1111, 1112, 1115, 1116, 1118, 1119, 1121, 1122, 1124, 1125, 1126, 1127, 1128, 1129, 1130, 1132, 1133, 1134, 1136, 1137, 1138, 1144, 1145, 1146, 1147, 1148, 1149, 1150, 1152, 1153, 1154, 1155, 1162, 1163, 1164, 1165, 1167, 1174, 1175, 1176, 1177, 1178, 1179, 1187, 1188, 1189, 1190, 1191, 1192, 1194, 1195, 1196, 1197, 1205, 1206, 1207, 1208, 1210, 1211, 1212, 1213, 1214, 1215, 1216, 1222, 1223, 1224, 1225, 1232, 1233, 1234, 1235, 1236, 1240, 1241, 1242, 1243, 1245, 1246, 1247, 1267, 1268, 1269, 1270, 1271, 1277, 1278, 1279, 1280, 1281, 1282, 1284, 1285, 1296, 1297, 1298, 1299, 1305, 1306, 1307, 1308, 1309, 1310, 1322, 1323, 1324, 1325, 1326, 1328, 1329, 1330, 1331, 1335, 1336, 1337, 1338, 1362, 1363, 1364, 1365, 1379, 1380, 1381, 1382, 1383, 1387, 1395, 1397, 1398, 1399, 1400, 1401, 1408, 1409, 1410, 1411, 1412, 1416, 1417, 1418, 1419, 1420, 1430, 1431, 1432, 1433, 1434, 1435, 1436, 1437, 1438, 1439, 1440, 1441});
		System.out.println(fromTrans.size());
		System.out.println(fromTab.size());
		List<Integer> transNotInTab = new ArrayList<>();
		for (int i : fromTrans) {
//			if (!fromTab.contains(i)) {
//				transNotInTab.add(i);
//			}
			if (Collections.frequency(fromTrans, i) > 1) {
				System.out.println(i);
			}
		}
		System.out.println(transNotInTab);
		List<Integer> tabNotInTrans = new ArrayList<>();
		for (int i : fromTab) {
			if (!fromTrans.contains(i)) {
				tabNotInTrans.add(i);
			}
		}
		System.out.println(tabNotInTrans);
		System.exit(0);
		


//		System.out.println(revX.getBasicNoteProperties().length);
		List<List<Integer>> mmi = new ArrayList<>();
		mmi.add(new ArrayList<>()); mmi.add(new ArrayList<>()); mmi.add(new ArrayList<>());
		mmi.add(new ArrayList<>()); mmi.add(new ArrayList<>());
//		MEIExport.exportMEIFile(revX, revY, mmi , true, "C:/Users/Reinier/Desktop/blabla.xml");


		
		
		System.exit(0);
		
		
		Transcription tar = new Transcription(
			new File("F:/research/data/experiments/thesis/exp_1/tab-int/3vv/N/fwd/out/fold_01-phalese-1547_7-tant_que-3vv.mid"),
//				new File("C:/Users/Reinier/Desktop/MEI/newsidler-1544_2-nun_volget-test.mid"), 			
//				enc);
				null);
		System.out.println(tar.getBasicNoteProperties().length);
		System.exit(0);
		
		String[] curr = new String[6];
		System.out.println(Arrays.asList(curr));
		System.exit(0);
		
		List<Integer> bla = Arrays.asList(new Integer[]{0, 1, 2});
		bla = bla.subList(0, bla.indexOf(null)); 
		System.out.println(bla);
		System.exit(0);
		
		String ger = ToolBox.readTextFile(new File(
			"C:/Users/Reinier/Desktop/ISMIR-2019/Gerbode-all/Gerbode_tc_to_Gerbode_paths.txt"));
//		System.out.println(ger);
		String[] gerSplit = ger.split("\r\n");
//		System.out.println(gerSplit[50]);
//		System.exit(0);
		String resu = "";
		for (String line : gerSplit) {
			String[] tokens = line.split(" -> ");	
//			System.out.println(Arrays.asList(tokens));
			String newLine = "";
			String name = tokens[0];
			for (int j = name.length(); j < 70; j++) {
				name += " ";
			}
			newLine += name;
			
			String first = tokens[1].substring(0, tokens[1].indexOf("/"));
			for (int j = first.length(); j < 15; j++) {
				first += " ";
			}
			newLine += first;
			
			String middle = tokens[1].substring(tokens[1].indexOf("/")+1, tokens[1].lastIndexOf("/"));
			for (int j = middle.length(); j < 60; j++) {
				middle += " ";
			}
			newLine += middle;
			
			String last = ""; tokens[1].substring(tokens[1].lastIndexOf("/")+1, tokens[1].length()); 			
			newLine += last + "\r\n";
			
//			String metadata[] = tokens[1].split("/");
//			String md = "";
//			for (String s : metadata) {
//				md += s;
//				int len = 60;
//				if (s.equals("composer") || s.equals("source")) {
//					len = 15;
//				}
//				for (int j = s.length(); j < len; j++) {
//					md += " ";
//				}
//				if (s.startsWith("attaingnant_") || s.startsWith("besard_") || 
//					s.startsWith("francisque_") || s.startsWith("fuenllana_") || 
//					s.startsWith("fuhrmann_") || s.startsWith("gardano_") 
//					|| s.startsWith("kargel_") || s.startsWith("mertel_") 
//					|| s.startsWith("mudarra_") || s.startsWith("narvaez_")
//					|| s.startsWith("negri_"))
//			}
//			newLine += md + "\r\n";
			
//			for (int i = 0; i < tokens.length; i++) {				
//				String token = tokens[i].trim();
//				if (i == (tokens.length - 1)) {
//					token += "\r\n";
//				}
//				else {
////					token += ",";
//					if (token.length() < 70) {
//						for (int j = token.length(); j < 70; j++) {
//							token += " ";
//						}
//					}
//				}
//				newLine += token;
//			}
			resu += newLine;
//				System.out.println(thing);			
		}
		ToolBox.storeTextFile(resu, new File("C:/Users/Reinier/Desktop/ISMIR-2019/Gerbode-all/Gerbode_tc_to_Gerbode_paths-rev.txt"));

		System.exit(0);
		
//		String path = "C:/Users/Reinier/Desktop/tab_reconstr-hector/Reinier-Scores/";
		String ppath = "F:/research/data/encodings/tab-int/3vv/";

//		String ppiece = "judenkuenig-1523_2-elslein_liebes";
//		String ppiece = "newsidler-1536_7-disant_adiu";
//		String ppiece = "newsidler-1536_7-mess_pensees";
		String ppiece = "newsidler-1544_2-nun_volget";
//		String ppiece = "phalese-1547_7-tant_que-3vv";
//		String ppiece = "pisador-1552_7-pleni_de";
		File encodingFile = new File(ppath + ppiece + ".txt");
			
		Tablature tab = new Tablature(encodingFile, false);
		
		System.exit(0);
		Runtime rt = Runtime.getRuntime();
		String cmd = "matlab -wait -nosplash -nodesktop -r  \"disp('hoi'); exit\"";
		try {
			System.out.println("OPEN");
			Process proc = rt.exec(cmd);
			BufferedReader bfr = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = "";
			while((line = bfr.readLine()) != null) {
				System.out.println(line);
			}
			proc.waitFor();
			System.out.println("CLOSE");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.exit(0);
				
		List<List<Integer>> endList = new ArrayList<List<Integer>>();
		List<List<Integer>> midList = new ArrayList<List<Integer>>();
		for (int i = 0; i < 3; i++) {
//			endList.add(Arrays.asList(new Integer[]{i, i, i}));
			midList.add(Arrays.asList(new Integer[]{i, i, i}));
		}
		endList.addAll(midList);
		System.out.println(endList);
		System.exit(0);
		
		List<String> hlfs = Arrays.asList(new String[]{"0.5", "0.25", "0.125", "1", "2", "4"});
		List<String> lmbs = 
			Arrays.asList(new String[]{"0.0", "0.00001", "0.0001", "0.001", "0.01", "0.1",
									          "0.00003", "0.0003", "0.003", "0.03"});
		List<String> epss = 
			Arrays.asList(new String[]{"0.0", "0.0005", "0.005", "0.05", "0.5", "0.00016",
										          "0.0016", "0.016", "0.16"});
//		for (String hlf : hlfs) {
//			for (String lmb : lmbs) {
//				xmlToSer(hlf, lmb);
//				combineWeightLists(hlf, lmb);
//			}
//		}
		for (String eps : epss) {
//			xmlToSer("", "", eps);
			combineWeightLists("", "", eps);
		}

		System.exit(0);
		
		List<String> pcs = Dataset.FUGUES_5VV_NAMES; //Dataset.INV_3VV_NAMES;
		int numVv = 5;
		for (String s : pcs) {
			System.out.println(s);
			Transcription trn = new Transcription(new File("F:/research/" +
				"MIDI/bach-WTC/thesis/" + numVv + "vv/" + s + ".mid"), null);
			List<List<Integer>> ve = 
				trn.determineVoiceEntriesHIGHLEVEL(trn.getBasicNoteProperties(), numVv, 3);
			for (List<Integer> l : ve) {
				System.out.println(l);
			}
			System.out.println("notes: " + trn.getBasicNoteProperties().length);
			FeatureGenerator fg = new FeatureGenerator();
			Integer[][] bnp = trn.getBasicNoteProperties();
			for (int i = 0; i < bnp.length; i++) {
//				if (fg.getSizeOfChordInclusive(null, null, bnp, i) == 5) {
//					System.out.println("index  = " + i);
//					System.out.println("metpos = " + 
//						Arrays.toString(trn.getAllMetricPositions().get(i)));
//				}
//				if (i == 252 || i == 503) {
//					System.out.println("*** " + i);
//					System.out.println("*** " + Arrays.toString(trn.getAllMetricPositions().get(i)));
//				}
//				if (i == 661) {
//					System.out.println(i);
//					System.out.println(Arrays.toString(trn.getAllMetricPositions().get(i)));
//				}
			}
		}
		System.exit(0);
		
				
//		String pc = "52nd_Street.mid";
		String pc = "A_Day_In_The_Life.mid";
		File fff = new File("C:/Users/Reinier/Desktop/MelodyShape/test/data/rock/" + pc);
		String fileName = fff.getName();
		URL urll = null;
		if (!fileName.endsWith(".mid")) {
			throw new RuntimeException("ERROR: the file is not a MIDI file.");
		}
		try {
			urll = fff.toURI().toURL();
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
//			return null;
		}
		
		Sequence sequence = null;
		try {
			sequence = MidiSystem.getSequence(urll);
		} catch (InvalidMidiDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Track[] tracks = sequence.getTracks();
		System.out.println(tracks.length);
//		System.exit(0);
		byte key = -1;
		byte mode = -1;
		for (int i = 0; i < tracks.length; i++) {
			Track t = tracks[i];
			for (int j = 0; j < t.size(); j++) {
				MidiEvent me = t.get(j);
				byte[] b = me.getMessage().getMessage(); 
				if (i == 3) {
					String listHex = "";
					for (byte by : b) {
//						listHex += Integer.toHexString(by) + ", ";
						// See https://stackoverflow.com/questions/7401550/how-to-convert-int-to-unsigned-byte-and-back
						listHex += Integer.toHexString(by & 0xFF) + ", ";
					}
					System.out.println(Arrays.toString(b));
					System.out.println(listHex);
					System.out.println(me.getTick());
				}
//				if (b[1] == 0x59) { // key signature
//					System.out.println("found in track " + i);
//					key = b[b.length-2];
//					mode = b[b.length-1];
//					break;
//				}
			}
		}
		System.exit(0);
		

		File script = new File(Runner.scriptPathPython);
		List<String> ls = PythonInterface.scriptToString(script, "def create_neural_network");
		for (String s : ls) {
			System.out.println(s);
		}
		System.exit(0);

//		File fil = new File(Runner.scriptPath);
//		String fils = ToolBox.readTextFile(fil);
//		String marker = "# --> init\r\n";
//		String top = fils.substring(fils.indexOf(marker) + marker.length(), fils.indexOf("# <-- init"));
////		System.out.println(top);
//		String[] topSplit = top.split("\r\n");
//		for (String s : topSplit) {
//			System.out.println(s);
//		}
//		System.out.println("BBBBB");
		
		System.exit(0);
//		List<String> meters = new ArrayList<String>(); 
//		for (String s : Dataset.FUGUES_4VV_NAMES) {
//			System.out.println(s);
////			Tablature t = new Tablature(new File("F:/research/data/encodings/tab-int/3vv/" + s + ".txt"), false);
//			Transcription tr = new Transcription(
//				new File("F:/research/data/MIDI/bach-WTC/thesis/4vv/" + s + ".mid"), 
////				new File("F:/research/data/encodings/tab-int/3vv/" + s + ".txt"),
//				null
//				);
////				"F:/research/data/experiments/thesis/exp_3.2/tab-int/3vv/N_prime/fwd/out/" +
////				"fold_01-phalese-1547_7-tant_que-3vv.mid"), null);
//			for (Integer[] in : tr.getMeterInfo()) {
//				System.out.println(in[0] + "/" + in[1]); 
//			}
//		}
//		System.out.println(meters);
//		System.exit(0);
				
		Transcription tr = new Transcription(
			new File("F:/research/data/MIDI/tab-int/4vv/barbetta-1582-il_nest.mid"), 
//			new File("F:/research/data/encodings/tab-int/4vv/ochsenkun-1558_5-herr_gott.txt"));
			null);
//		Transcription tr = new Transcription(
//			new File("F:/research/data/MIDI/bach-WTC/thesis/3vv/bach-WTC1-fuga_2-BWV_847.mid"), 
////			new File("F:/research/data/encodings/tab-int/4vv/ochsenkun-1558_5-herr_gott.txt"));
//			null);
		Tablature t = new Tablature(new File("F:/research/data/encodings/tab-int/4vv/ochsenkun-1558_5-herr_gott.txt"), false);
		for (Integer[] in : tr.getMeterInfo()) {
			System.out.println(Arrays.toString(in));
		}
//		System.out.println(tr.getBasicNoteProperties()[0][Transcription.ONSET_TIME_DENOM]);
//		System.out.println(tr.getBasicNoteProperties()[0][Transcription.ONSET_TIME_DENOM]);
//		MEIExport.getData(tr, "");
		System.exit(0);
		
		List<Rational> all = new ArrayList<Rational>();
		for (int i = 1; i <= 128; i++) {
			all.add(new Rational(i, 128));
		}
		
			
		for (int i = 0; i < all.size(); i++) {
			List<Boolean> blah = new ArrayList<Boolean>();
			Rational durRat = all.get(i);
//			System.out.println(durRat);
			// Triple dotted = 1.875 * basic note value
			Rational basicNoteValTrpD = durRat.div(new Rational(15, 8));
			basicNoteValTrpD.reduce();
			if (basicNoteValTrpD.getNumer() == 1 && all.contains(basicNoteValTrpD)) {
				blah.add(Boolean.TRUE);
			}
			else {
				blah.add(Boolean.FALSE);
			}
			// Double dotted = 1.75 * basic note value
			Rational basicNoteValDblD = durRat.div(new Rational(7, 4));
			basicNoteValDblD.reduce();
			if (basicNoteValDblD.getNumer() == 1 && all.contains(basicNoteValDblD)) {
				blah.add(Boolean.TRUE);
			}
			else {
				blah.add(Boolean.FALSE);
			}
			// Dotted = 1.5 * basic note value 
			Rational basicNoteValD = durRat.div(new Rational(3, 2));
			basicNoteValD.reduce();
			if (basicNoteValD.getNumer() == 1 && all.contains(basicNoteValD)) {
				blah.add(Boolean.TRUE);
			}
			else {
				blah.add(Boolean.FALSE);
			}
			// Undotted
			Rational basicNoteVal = durRat.div(new Rational(1, 1));
//			blah.add(all.contains(basicNoteVal));
			if (Collections.frequency(blah, Boolean.TRUE) == 0) {
				System.out.println(durRat);
				System.out.println(blah);
			}
		}
		System.exit(0);
				
		double[][] bb = new double[2][4];
		System.out.println(Arrays.toString(bb[0]));
		System.exit(0);
		List<List<double[]>> joop = ToolBox.getStoredObjectBinary(
			new ArrayList<List<double[]>>(), new File(
			"F:/research/data/experiments/thesis/exp_1/tab-int/4vv/N/fwd/fold_01/model_outputs.ser"));
		System.out.println(Arrays.toString(joop.get(2).get(0)));
		System.out.println(Arrays.toString(joop.get(2).get(1)));
		System.out.println(Arrays.toString(joop.get(2).get(2)));
		System.exit(0);
		
//		xmlToSer();
//		combineWeightLists();
		System.exit(0);
		
		List<List<Integer>> startConfig = new ArrayList<List<Integer>>();
		List<Integer> hoii = new ArrayList<Integer>();
		hoii.add(3); hoii.add(2); hoii.add(1); hoii.add(null); hoii.add(null);
		startConfig.add(hoii);
		for (List<Integer> l : startConfig) {
			//int last = l.get(l.size() - 1);
			l.add(0, l.get(l.size() - 1));
			//last = l.get(l.size() - 1);
			l.remove(l.size()-1);
		}
		System.out.println(startConfig);
		System.exit(0);
		
		List<List<Integer>> partialLeft = new ArrayList<List<Integer>>();
		List<List<Integer>> partialRight = new ArrayList<List<Integer>>();
		partialLeft.add(Arrays.asList(new Integer[]{1, 2, 3, null, null}));
		partialLeft.add(Arrays.asList(new Integer[]{null, 76, 60}));
		partialLeft.add(Arrays.asList(new Integer[]{null, 78, 57}));
		partialRight.add(Arrays.asList(new Integer[]{79, 64, 64}));
		partialRight.add(Arrays.asList(new Integer[]{78, 64, 54}));
		partialRight.add(Arrays.asList(new Integer[]{79, 64, 52}));
		
		
		
		List<Integer> joe = new ArrayList<Integer>(partialLeft.get(0));
//		joe.add(1); joe.add(2); joe.add(3); joe.add(null); joe.add(null); 
		System.out.println(joe);
		int si = 2;
		for (int j = 0; j < si; j++) {
			joe.add(0, null);
			joe.remove(joe.size()-1);
		}
		System.out.println(joe);
		System.exit(0);
		
		
		int cost = 0;
		for (List<Integer> pl : partialLeft) {
			for (List<Integer> pr : partialRight) {
				for (int j = 0; j < pl.size(); j++) {
					if (pl.get(j) != null) {
						cost += Math.abs(pr.get(j) - pl.get(j));	
					}
				}
			}
		}
		System.out.println(cost);
		System.exit(0);
		
		
		List<Integer> wan = new ArrayList<Integer>();
		wan.add(0); wan.add(1); wan.add(2);
		List<Integer> toe = new ArrayList<Integer>(wan);
		System.out.println(wan);
		System.out.println(toe);
		wan.add(3);
		System.out.println(wan);
		System.out.println(toe);
		System.exit(0);

		
		List<Integer> nums = new ArrayList<Integer>();
		nums.add(0); nums.add(1); nums.add(2);
		
		nums.add(nums.size(), 3);
		System.out.println(nums);
		System.exit(0);
		
		List<Double> mvmtHeadMotif = Arrays.asList(new Double[]{1.0, -1.0});
		List<List<Double>> leewayCandidates = new ArrayList<List<Double>>();
		System.out.println(mvmtHeadMotif);
		for (int k = 0; k < mvmtHeadMotif.size(); k++) {
			List<Double> leewayCand = new ArrayList<Double>(mvmtHeadMotif); 
			leewayCand.set(k, 0.0);
			leewayCandidates.add(leewayCand);
		}
		System.out.println(mvmtHeadMotif);
		System.out.println(leewayCandidates);
		
		
		System.exit(0);
		
		List<Integer> ah = Arrays.asList(new Integer[]{48, 20, 30, 1});
		List<Integer> bh = Arrays.asList(new Integer[]{48, 20, 30, 1});
		System.out.println(ah.equals(bh));
		System.exit(0);
		Transcription ff = 
			new Transcription(new 
			File("F:/research/data/MIDI/bach-WTC/thesis/3vv/bach-WTC1-fuga_2-BWV_847.mid"), null);
		Integer[][] bnp = ff.getBasicNoteProperties();

//		System.out.println(Arrays.toString(ff.getVoiceEntries(3)));
		System.exit(0);
		
		System.out.println(ff.getNoteDensity());
		FeatureGenerator fg = new FeatureGenerator();
		
		List<Integer> notedens = ff.getNoteDensity();
				
		int highestNumVoices = 3; 
		int numVoices = 1;
		List<Integer> lowestCostConfigs = new ArrayList<Integer>();
		for (int i = 0; i < notedens.size(); i++) {
			int dens = notedens.get(i);
			// When another voice enters
			if (dens > numVoices) {
				numVoices++;
				// Find each note with current onset time; start from i (the first note
				// at which the number of voices is one higher)
				Rational currOn = new Rational(bnp[i][Transcription.ONSET_TIME_NUMER],
					bnp[i][Transcription.ONSET_TIME_DENOM]);
				List<Integer> currPitches = new ArrayList<Integer>();
				// Add pitches of any sustained previous notes
				List<Integer> currIndSus = 
					Transcription.getIndicesOfSustainedPreviousNotes(null, null, bnp, i);
				for (int ind : currIndSus) {
					currPitches.add(bnp[ind][Transcription.PITCH]);
				}
				// Add pitches of all notes in the current chord
				for (int j = i; j < notedens.size(); j++) {
					Rational on = new Rational(bnp[j][Transcription.ONSET_TIME_NUMER],
						bnp[j][Transcription.ONSET_TIME_DENOM]);
					if (on.equals(currOn)) {
						currPitches.add(bnp[j][Transcription.PITCH]);
					}
					else {
						break;
					}
				}

				// Find each note with previous onset time
				Rational prevOn = new Rational(bnp[i-1][Transcription.ONSET_TIME_NUMER],
					bnp[i-1][Transcription.ONSET_TIME_DENOM]);
				List<Integer> prevPitches = new ArrayList<Integer>();
				// Add pitches of any sustained previous notes
				List<Integer> prevIndSus = 
					Transcription.getIndicesOfSustainedPreviousNotes(null, null, bnp, i-1);
				for (int ind : prevIndSus) {
					prevPitches.add(bnp[ind][Transcription.PITCH]);
				}
				// Add pitches of all notes in the previous chord
				for (int j = i-1; j >= 0; j--) {
					Rational on = new Rational(bnp[j][Transcription.ONSET_TIME_NUMER],
						bnp[j][Transcription.ONSET_TIME_DENOM]);
					if (on.equals(prevOn)) {
						prevPitches.add(bnp[j][Transcription.PITCH]);
					}
					else {
						break;
					}
				}
				Collections.sort(currPitches);
				Collections.sort(prevPitches);
				Collections.reverse(currPitches);
				Collections.reverse(prevPitches);
				prevPitches.add(null);
				System.out.println(i);
				System.out.println(currPitches);
				System.out.println(prevPitches);
				int sizeCurrChord = currPitches.size();
				
				// Compare previous (p) pitches with current (c) pitches and search for 
				// lowest-cost configuration. Example for transition from 2vv to 3vv:
				// configuration 0: p1, p2, null <-> c1, c2, c3 
				// configuration 1: p1, null, p2 <-> c1, c2, c3
				// configuration 2: null, p1, p2 <-> c1, c2, c3
				// Configuration 0 has null at the end of the list; configuration 1 has 
				// null at the penultimate position; etc.
				List<Integer> costPerConfig = new ArrayList<Integer>();
				int config = 0;
				while (config < sizeCurrChord) {
					int currCost = 0;
					for (int j = 0; j < sizeCurrChord; j++) {
						if (prevPitches.get(j) != null) {
							currCost += Math.abs(currPitches.get(j)-prevPitches.get(j)); 
						}
					}
					costPerConfig.add(currCost);
					// Last configuration? Break
					if (config == sizeCurrChord-1) {
						break;
					}
					// Else, shift null one position back and increment config 
					else {
						int swapInd = prevPitches.indexOf(null);
						Collections.swap(prevPitches, swapInd, swapInd-1);
						config++;
					}
				}
				System.out.println("cpc = " + costPerConfig);
				int lowestCostConfig = Collections.min(costPerConfig);
				System.out.println(lowestCostConfig);
				System.out.println(costPerConfig.indexOf(lowestCostConfig));
				lowestCostConfigs.add(costPerConfig.indexOf(lowestCostConfig));

				if (dens == highestNumVoices) {
					break;
				}
			}
		}
		System.out.println(lowestCostConfigs);
//		// List the configs per transition
//		List<List<String>> configsPerTransition = new ArrayList<List<String>>();
//		// 1. Transition to 2vv, config : entering voice 
//		// [0, 1] : [lower, higher]
//		configsPerTransition.add(Arrays.asList(new String[]{"L", "H"}));
//		// 2. Transition to 3vv, config : entering voice 
//		// [0, 1, 2] : [lower, middle, higher]
//		configsPerTransition.add(Arrays.asList(new String[]{"L", "M", "H"}));
//		// 3. Transition to 4vv, config : entering voice  
//		// [0, 1, 2, 3] : [lower, lower middle, upper middle, higher]
//		configsPerTransition.add(Arrays.asList(new String[]{"L", "LM", "UM", "H"}));
//		// 3. Transition to 5vv, config : entering voice 
//		// [0, 1, 2, 3, 4] : [lower, lower middle, middle, upper middle, higher]
//		configsPerTransition.add(Arrays.asList(new String[]{"L", "LM", "M", "UM", "H"}));
				
		// Map the combined configs to voicings
		Map<List<Integer>, Integer[]> configMap = new LinkedHashMap<List<Integer>,
			Integer[]>();
		// 3vv (L=lower, M=middle, H=higher, l=lower, u=upper)
		configMap.put(Arrays.asList(new Integer[]{0, 0}), new Integer[]{0, 1, 2}); // SAB (LL)
		configMap.put(Arrays.asList(new Integer[]{0, 1}), new Integer[]{0, 2, 1}); // SBA (LM)
		configMap.put(Arrays.asList(new Integer[]{1, 0}), new Integer[]{1, 0, 2}); // ASB (HL)
		configMap.put(Arrays.asList(new Integer[]{0, 2}), new Integer[]{1, 2, 0}); // ABS (LH)
		configMap.put(Arrays.asList(new Integer[]{1, 1}), new Integer[]{2, 0, 1}); // BSA (HM)
		configMap.put(Arrays.asList(new Integer[]{1, 2}), new Integer[]{2, 1, 0}); // BAS (HH)

//		// Get the configstring
//		String configStr = "";  
//		for (int i = 0; i < lowestCostConfigs.size(); i++) {
//			int transition = i;
//			int lowestCostConfig = lowestCostConfigs.get(i);
//			configStr += configsPerTransition.get(transition).get(lowestCostConfig);
//		}
//		System.out.println(configStr);
		System.out.println(Arrays.toString(configMap.get(lowestCostConfigs)));
		
		
		
//		xmlToSer();		
//		combineWeightLists();
		System.exit(0);
		
		
			
//		String[] paths = new String[]{"een", "twee", "drie"};
		
//		Transcription oold = ToolBox.getStoredObjectBinary(new Transcription(), 
//			new File("F:/research/data/experiments/thesis/exp_3.2/intabulations/3vv/N_prime/bwd - Copy/out/01-phalese-1547_7-tant_que-a3.ser"));
//		Transcription njew = ToolBox.getStoredObjectBinary(new Transcription(), 
//			new File("F:/research/data/experiments/thesis/exp_3.2/intabulations/3vv/N_prime/bwd/out/01-phalese-1547_7-tant_que-a3.ser"));
		
		
//		List<List<Double>> vlold = oold.getVoiceLabels();
//		List<List<Double>> dlold = oold.getDurationLabels();
//		List<List<Double>> vlnjew = njew.getVoiceLabels();
//		List<List<Double>> dlnjew = njew.getDurationLabels();
		
//		System.out.println(vlold.size());
//		System.out.println(vlnjew.size());
//		System.out.println(dlold.size());
//		System.out.println(dlnjew.size());
		
//		ToolBox.storeObjectBinary(vlold, new File("C:/Users/Reinier/Desktop/kwaak/vlold.ser"));
//		ToolBox.storeObjectBinary(dlold, new File("C:/Users/Reinier/Desktop/kwaak/dlold.ser"));
		
		
//		System.out.println("voice labels NEW");
//		for (int i = 0; i < vlnjew.size(); i++) {
//			System.out.println(i + ": " + DataConverter.convertIntoListOfVoices(vlnjew.get(i)));
//		}
//		System.out.println();	
//		System.out.println("dur labels NEW");
//		for (int i = 0; i < dlnjew.size(); i++) {
//			System.out.println(i + ": " +  Arrays.toString(DataConverter.convertIntoDuration(dlnjew.get(i))));
//		}
		
		List<List<Double>> vlold = ToolBox.getStoredObjectBinary(new ArrayList<List<Double>>(), 
			new File("C:/Users/Reinier/Desktop/kwaak/vlold.ser"));
		List<List<Double>> vlnew = ToolBox.getStoredObjectBinary(new ArrayList<List<Double>>(), 
			new File("C:/Users/Reinier/Desktop/kwaak/vlnjew.ser"));
		List<List<Double>> dlold = ToolBox.getStoredObjectBinary(new ArrayList<List<Double>>(), 
			new File("C:/Users/Reinier/Desktop/kwaak/dlold.ser"));
		List<List<Double>> dlnew = ToolBox.getStoredObjectBinary(new ArrayList<List<Double>>(), 
			new File("C:/Users/Reinier/Desktop/kwaak/dlnjew.ser"));
		
		for (int i = 0; i < vlold.size(); i++) {
			if (vlold.get(i).equals(vlnew.get(i)) == false) {
				System.out.println("mis jongen voice");
			}
		}
		for (int i = 0; i < dlold.size(); i++) {
			if (dlold.get(i).equals(dlnew.get(i)) == false) {
				System.out.println("mis jongen duration");
			}
		}
		System.exit(0);
		

		

//		
//		double sum = Math.log(0.9999999999) + Math.log(0.000000) + Math.log(0.0000001) + Math.log(0.0000001);
//		
//		double cre = (-1.0/4) * (sum/ErrorCalculator.CRE_SUM_DIV);
//		System.out.println(cre);
		System.exit(0);
		
		List<List<Double>> labels = new ArrayList<List<Double>>();
//		labels.add(Arrays.asList(new Double[]{1.0, 0.0, 0.0, 0.0, 0.0}));
		labels.add(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 0.0, 0.0}));
//		labels.add(Arrays.asList(new Double[]{0.0, 0.0, 1.0, 0.0, 0.0}));
		List<double[]> outputs = new ArrayList<double[]>();
//		outputs.add(new double[]{1.0, 0.0, 0.0, 0.0, 0.0});
		outputs.add(new double[]{1.0, 0.0, 0.0, 0.0, 0.0});
//		outputs.add(new double[]{0.0, 0.0, 1.0, 0.0, 0.0});
		
		double[] actual = ErrorCalculator.calculateCrossEntropy(outputs, labels);
		System.out.println(Arrays.toString(actual));
		System.exit(0);
		
		List<Double> sdfdfs = Arrays.asList(new Double[]{0.123, 0.234, 0.345, 0.456,
			0.123, 0.234, 0.345, 0.456, 0.123, 0.234, 0.345, 0.456,
			0.123, 0.234, 0.345, 0.456, 0.123, 0.234, 0.345, 0.456});
		System.out.println("first");
		System.out.println(ToolBox.getTimeStamp());
		for (int i = 0; i < 1000000; i++) {
			String ixxi = sdfdfs.toString();
			String oxxo = ixxi.substring(1, ixxi.length()-1);
		}
		System.out.println(ToolBox.getTimeStamp());
		System.out.println("second");
		System.out.println(ToolBox.getTimeStamp());
		for (int i = 0; i < 1000000; i++) {
			String ixxi = "";
			for (double d : sdfdfs) {
				ixxi = ixxi.concat(Double.toString(d)).concat(","); 
			}
		}
		System.out.println(ToolBox.getTimeStamp());
		System.exit(0);
		
		String csvStrOld = 	
			ToolBox.readTextFile(new File("C:/Users/Reinier/Desktop/ISMIR2017/fv_fold_1.csv"));
		String[][] dlo = ToolBox.retrieveCSVTable(csvStrOld);
		String csvStrNew = 	
			ToolBox.readTextFile(new File("F:/PhD/data/experiments/ISMIR-2017/bach-WTC/4vv/LR/fwd/fold_01/fv_fold_1.csv"));
		String[][] wen = ToolBox.retrieveCSVTable(csvStrNew);
		
		System.out.println(dlo.length);
		System.out.println(wen.length);
		System.out.println(dlo[0].length);
		System.out.println(wen[0].length);
		
		
		for (int i = 0; i < dlo.length; i++) {
			String[] currOld = dlo[i];
			String[] currNew = wen[i];
			for (int j = 0; j < currOld.length; j++) {
				if (!(currOld[j].equals(currNew[j]))) {
					System.out.println("not same at " + i + ", " + j);
				}
			}
		}
		System.exit(0);
		
		
		String argRootDir = new File(".").getAbsolutePath();
		System.out.println(argRootDir);
		System.exit(0);
		
//		String path = "F:/PhD/data/experiments/thesis/exp_3.3/tab/3vv/B/out/";
//		String path = "F:/PhD/data/experiments/thesis/exp_1/intabulations/3vv/N/fwd/out/";
		String path = "C:/Users/Reinier/Desktop/MIDI-test/";
		String piece = "phalese-1547_7-tant_que-a3";
		
		Transcription trr = new Transcription(new File(path + piece + ".mid"), 
			new File("F:/PhD/data/encodings/intabulations/3vv/phalese-1547_7-tant_que-a3.txt"));
		ToolBox.storeObjectBinary(trr, new File(path + piece + ".ser"));
		
		Transcription stored =	
			ToolBox.getStoredObjectBinary(new Transcription(), new File(path + piece + ".ser"));
		System.out.println(stored.getPiece().getScore().get(0).get(0).size());
		System.out.println(stored.getPiece().getScore().get(1).get(0).size());
		System.out.println(stored.getPiece().getScore().get(2).get(0).size());
		
//		System.exit(0);

//		System.exit(0);
//		Transcription predTranscr = 
//			new Transcription(new File("F:/PhD/data/MIDI/thesis/bach/4vv/bach-WTC1-fuga_1-BWV_846.mid"), null);

		Piece pp = stored.getPiece();
		
		MidiReader mr = new MidiReader();
		mr.setPiece(pp);
		System.out.println(mr.getNumberOfTracks());
//		System.exit(0);
		
		NotationSystem pns = pp.getScore();
		int count = 0;
		for (int i = 0; i < pns.size(); i++) {
			NotationVoice nv = pns.get(i).get(0);
			System.out.println("voice = " + i);
			for (int j = 0; j < nv.size(); j++) {
				count++;
				Note n = nv.get(j).get(0); 
//				System.out.println(n);
				PerformanceNote pn = n.getPerformanceNote();
				MidiNote mn = MidiNote.convert(pn);
				mn.setChannel(i);
				
				ScoreNote sn = n.getScoreNote();
				long onTick = (long) sn.getMetricTime().mul(1024).toDouble();
//				long offTick = onTick + ((long) sn.getMetricDuration().mul(1024).toDouble());
				long durTick = (long) sn.getMetricDuration().mul(1024).toDouble();
//				pn.setTime(onTick);
//				pn.setDuration(durTick);
				mn.setTime(onTick);
				mn.setDuration(durTick);
				System.out.println(mn);
				System.out.println(n);
			}
		}
		System.out.println(count);
		System.out.println(path);

//		System.exit(0);
				
		MidiWriter midiWriter = new MidiWriter(path + "pieceToMidi.mid", pp); 
////		midiWriter.setContainer(container);
		midiWriter.write();
		
		System.exit(0);
		
//		File dd = new File("F:/PhD/data/experiments/thesis/exp_1/tab/3vv/N/fwd/fold_01/weights.ser");
//		ArrayList<List<Double>> bl =
//			ToolBox.getStoredObjectBinary(new ArrayList<List<Double>>(), dd);
//		System.out.println(bl.get(0).size());
//		System.out.println(bl.get(1).size());
//		
//		System.exit(0);
		
		
////	String pn = "abondante-1548_1-mais_mamignone";
////	String pn = "barbetta-1582-il_nest";
////	String pn = "ochsenkun-1558_5-absolon_fili";
////	String pn = "ochsenkun-1558_5-herr_gott";
////	String pn = "ochsenkun-1558_5-in_exitu";
////	String pn = "ochsenkun-1558_5-qui_habitat";
////	String pn = "phalese-1547_7-tant_que-a4";
////	String pn = "phalese-1563_12-las_on";
//		String pn = "rotta-1546_15-bramo_morir";
		
		String fs = "C:/Users/Reinier/Desktop/test/";
		
		List<String> pieceNames = Arrays.asList(new String[]{
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
		
//		for (String pn : pieceNames) {
////			String one = "F:/PhD/data/encodings/thesis/4vv/";
//			String two = "F:/PhD/data/MIDI/thesis/bach/4vv/";
//			File enc = null; // new File(one + pn + ".txt");
////			File mid = new File(two + pn);
//			File mid = new File(two + pn + ".mid");
//			
//			Transcription trr = new Transcription(mid, enc);
//			ToolBox.storeObjectBinary(trr, new File(fs + pn + "-NEW.ser"));
//		}
		
		for (String pn : pieceNames) {
			System.out.println(pn);
			Transcription oldt = 
				ToolBox.getStoredObjectBinary(new Transcription(), new File(fs + pn + "-OLD.ser"));
			Transcription newt = 
				ToolBox.getStoredObjectBinary(new Transcription(), new File(fs + pn + "-NEW.ser"));
			
			Integer[][] btpOld = oldt.getBasicNoteProperties();
			Integer[][] btpNew = newt.getBasicNoteProperties();
			System.out.println(btpOld.length - btpNew.length);
			for (int i = 0; i < btpOld.length; i++) {
				Integer[] old = btpOld[i];
				Integer[] nw = btpNew[i];
				if (old.length != nw.length) {
					System.out.println("length not the same");
				}
				for (int j = 0; j < old.length; j++) {
//					System.out.println("old = " + old[j]);
//					System.out.println("new = " + nw[j]);
					if (!(old[j].equals(nw[j]))) {
						System.out.println(i + ": elements not the same");
					}
				}
			}
		}
		System.exit(0);
		
		
		String pref = "";
		File ttaf = 
			new File(pref + "F:/PhD/data/experiments/thesis/exp_1/bach/4vv/N/fwd/out/03-ntw_outputs.ser");
		System.out.println(ttaf.getParent());
		System.exit(0);
		List<List<double[]>> tta = ToolBox.getStoredObjectBinary(new ArrayList<List<double[]>>(), ttaf);
		for (double[] d : tta.get(2)) {
			System.out.println(Arrays.toString(d));
		}
		System.exit(0);
		
		List<double[]> tta0 = tta.get(0);
		List<double[]> tta1 = tta.get(1);
		List<double[]> tta2 = tta.get(2);
		File treenf = new File(pref + "fold_01/NN-outputs-train.ser");
		List<double[]> treen = ToolBox.getStoredObjectBinary(new ArrayList<double[]>(), treenf);
		File testf = new File(pref + "fold_01/NN-outputs-test.ser");
		List<double[]> test = ToolBox.getStoredObjectBinary(new ArrayList<double[]>(), testf);
		System.out.println(tta1.size());
		System.out.println(test.size());
		for (int i = 0; i < tta1.size(); i++) {
			double[] a = tta1.get(i);
			double[] b = test.get(i);
			for (int j = 0; j < a.length; j++) {
				if (i == 5) {
					System.out.println(a[j]);
					System.out.println(b[j]);
				}
				if (a[j] != b[j]) {
					System.out.println("FOUT");
				}
			}
		}
		System.exit(0);
		
		File fi = new File("C:/Users/Reinier/Desktop/MIDI-test/tant-test-staff_only-three-1-4-serpent.mid");
		URL url = null;
		try {
			url = fi.toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Piece p = new MidiReader().getPiece(url);
		NotationSystem ns = p.getScore(); 
		NotationStaff X = ns.get(0);
		System.out.println("==========");
		System.out.println(ns.size()); // yields 4
		System.out.println("==========");
		List<Integer> toRemove = new ArrayList<Integer>();
		for (int i = 0; i < ns.size(); i++) {
			NotationStaff nv = ns.get(i); 
			if (nv.get(0).size() == 0) {
				toRemove.add(i);
			}
		}
		System.out.println(toRemove);
		for (int i : toRemove) {
			ns.remove(i);
		}
		System.out.println(ns.size());
		System.out.println("NotationStaff X = " + X);
		System.out.println("  size of X: " + X.size());
		System.out.println("  contains NotationVoice: " + X.get(0));
		System.out.println("  size of Notationvoice: " + X.get(0).size());
		NotationStaff S = p.getScore().get(0); // upper voice
		System.out.println("NotationStaff S = " + S);
		System.out.println("  size of S: " + S.size());
		System.out.println("  contains NotationVoice: " + S.get(0)); // empty NotationVoice
		System.out.println("  size of Notationvoice: " + S.get(0).size());
		NotationStaff A = p.getScore().get(1); // middle voice
		System.out.println("NotationStaff A = " + A);
		System.out.println("  size of A: " + A.size());
		System.out.println("  contains NotationVoice: " + A.get(0)); // NotationVoice
		System.out.println("  size of Notationvoice: " + A.get(0).size());
		NotationStaff B = p.getScore().get(2); // lower voice
		System.out.println("NotationStaff B = " + B);
		System.out.println("  size of B: " + B.size());
		System.out.println("  contains NotationVoice: " + B.get(0)); // NotationVoice
		System.out.println("  size of Notationvoice: " + B.get(0).size());
		
		System.out.println("LA LA LA");
		for (NotationChord nc : B.get(0)) {
			if (nc.size() > 1) {
				System.exit(0);
			}
			Note n = nc.get(0);
			System.out.println(nc);
		}
			
		
//		System.out.println(part.getScore().get(0).get(0).get(0).get(0)); // A
//		System.out.println(part.getScore().get(1).get(0).get(0).get(0)); // A
//		System.out.println(part.getScore().get(2).get(0).get(0).get(0)); // B
		System.exit(0);
		Collection<Containable> notesInPart = p.getScore().getContentsRecursiveList(null);
//		Collection<Containable> notesInPart = part.getScore().getContentsRecursiveList(null);
		for (Containable c : notesInPart) {
			if (c instanceof Note) {	
				System.out.println(c);				
//				notationVoice.add((Note) c); 	
			}	
		}
		System.exit(0);
		
//		System.out.println("voor");
//		initialize();
		File f = new File("F:/PhD/data/MIDI/user/4vv/798_milano_012");
		System.out.println(f.getName());
		f = new File(f.getName() + ".mid");
		System.out.println(f.getName());
		System.exit(0);
		
		System.out.println("****");
		System.out.println(DatasetID.valueOf("TAB_3VV").getPieceNames());

//		System.out.println("na");
	}
	
	
	public static void main2(String[] args) {
		
		System.out.println("HET WERKT!");
		System.exit(0);
		
//		DecimalFormatSymbols otherSymb = new DecimalFormatSymbols(Locale.UK);
//		DecimalFormat decF = new DecimalFormat("0.0000", otherSymb);
//		decF.setRoundingMode(RoundingMode.DOWN);
//		System.out.println(decF.format(0.234567));
//		System.exit(0);
		
//		File fail = new File("thesis/exp_3.1/tab/3vv/N/fold_01/");
//		System.out.println(fail.getParent());

		List<double[]> orig1 = ToolBox.getStoredObjectBinary(new ArrayList<double[]>(), 
			new File("F:/PhD/data/results/thesis/exp_3.1/tab/3vv/N/bwd/fold_05/NN-outputs-train.ser")); 
		List<double[]> orig2 = ToolBox.getStoredObjectBinary(new ArrayList<List<double[]>>(), 
			new File("F:/PhD/data/results/thesis/exp_3.1/tab/3vv/N/bwd/out/04-ntw_outputs.ser")).get(Runner.APPL);
	
		for (double[] d : orig2) {
			System.out.println(Arrays.toString(d));
		}
		
//		System.out.println(orig1.size() == (orig2.size()));
//		for (int i = 0; i < orig1.size(); i++) {
//			if ( Arrays.equals(orig1.get(i), orig2.get(i)) == false) {
//				System.out.println("false");
//			}
//		}
		System.exit(0);
		
//		xmlToSer();
//		combineWeightLists();
		System.exit(0);
		String pth = "F:/PhD/data/results/thesis/exp_1/tab/3vv/N/fwd/fold_01/";
		String name = "phalese-1547_7-tant_que-a3";
		
		List<List<Double>> both = new ArrayList<List<Double>>();
		both.add(ToolBox.getStoredObjectBinary(new ArrayList<Double>(), 
			new File(pth + "init_weights.ser")));
		both.add(ToolBox.getStoredObjectBinary(new ArrayList<Double>(), 
			new File(pth + "best_weights.ser")));
		ToolBox.storeObjectBinary(both, new File(pth + "weights.ser"));
		
		System.exit(0);
		
		Transcription ttt = 
			ToolBox.getStoredObjectBinary(new Transcription(), new File(pth + name + ".ser"));
		List<List<Double>> vl_ttt = ttt.getVoiceLabels();
		List<List<Double>> vl_org =
			ToolBox.getStoredObjectBinary(new ArrayList<List<Double>>(), 
			new File(pth + "voice_lab-" + name + ".ser"));
		System.out.println(vl_org.size() == vl_ttt.size());
		System.out.println(vl_org.get(0));
		System.out.println(vl_ttt.get(0));
		for (int i = 0; i < vl_org.size(); i++) {
			if (! ( vl_ttt.get(i).equals(vl_org.get(i)) ) ) {
				System.out.println("FALSE");
			}
		}
		
		System.exit(0);
		
		List<String> metricsUsed = new ArrayList<String>(
			Arrays.asList(new String[]{"A", "B", "C"}));
		metricsUsed.add("D");
		System.out.println(metricsUsed);
		System.exit(0);
				
		List<Integer> predictedVoices = 
			OutputEvaluator.interpretNetworkOutput(new double[]{0.0, 1.0, 1.0, 0.0, 0.0}, true, 
			0.05).get(0);
		System.out.println(predictedVoices);
		System.exit(0);
		
		String[] formaatted = 
			new String[]{
			"a", "a", "a", "a", 
			"b", "b", "b", "b", 
			"c", "c", "c", "c",
			"d", "d", "d", "d", 
			"e", "e", "e", "e", 
			"f", "f", "f", "f",
			"g", "g", "g", "g", 
			"h", "h", "h", "h"};	
		String[][] broken = new String[8][4];
		int len = 4;
		for (int i = 0; i < 8; i++) {
			int start = len*i;
			broken[i] = Arrays.copyOfRange(formaatted, start, start + len);
		}
		System.out.println(Arrays.toString(broken[0]));
		System.out.println(Arrays.toString(broken[1]));
		System.out.println(Arrays.toString(broken[2]));
		System.out.println(Arrays.toString(broken[3]));
		System.out.println(Arrays.toString(broken[4]));
		System.out.println(Arrays.toString(broken[5]));
		System.out.println(Arrays.toString(broken[6]));
		System.out.println(Arrays.toString(broken[7]));
		System.exit(0);
		
		List<Integer> aap = Arrays.asList(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
			13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28});
		String[] adaptations = new String[]{
			"correct to correct", "correct to incorrect",
			"incorrect to correct", "incorrect to incorrect"
		};
//		System.out.println("\t\t\t\tx");
//		System.out.println(ToolBox.tabify(adaptations[3], 4) + "x");
		System.exit(0);
		
		StringBuffer ert = new StringBuffer().append("23234234234");
		System.out.println(ert.indexOf("x"));
				
		System.exit(0);
		
		String hoie = "";
		hoie += ToolBox.tabify("dit", 1);
		hoie += ToolBox.tabify("is", 1);
		hoie += ToolBox.tabify("een", 1);
		hoie += ToolBox.tabify("I", 1);
		int nummTabs = 0;
		String alph = "abcdefghijklmnopqrstuvwxyz";
		for (int i = 0; i < hoie.length(); i++) {
			char c = hoie.charAt(i);
			if (!alph.contains(String.valueOf(c).toLowerCase())) {
				nummTabs++;
			}
		}
		System.out.println(hoie);
		System.out.println(nummTabs);
		System.out.println(hoie.length());
			
		System.exit(0);
		
		StringBuffer txt = new StringBuffer();
		txt.append("LEGEND");
		StringBuffer txt2 = new StringBuffer(txt).replace(0, 1, "Z");
//		txt2.append("flfl");
//		txt2.replace(0, 1, "Z");
		
		System.out.println(txt);
		System.out.println(txt2);
		
		
		
		StringBuffer legendStr = new StringBuffer();
		legendStr.append("--------------------------------\r\n");
		legendStr.append(ToolBox.tabify("acc", 1));
		legendStr.append(ToolBox.tabify("snd", 1));
		legendStr.append(ToolBox.tabify("I", 1));
		legendStr.append(ToolBox.tabify("O", 1));
		legendStr.append("\r\n--------------------------------\r\n");
		System.out.println(legendStr);
		
		txt.append(legendStr);
		System.out.println(txt.toString());
		
		List<String> strs = Arrays.asList(new String[]{"acc", "I", "O"});
		for (String s : strs) {
			String r = ToolBox.tabify(s, 1);
			int start = legendStr.indexOf(r);
			legendStr.replace(start, start + r.length(), ToolBox.tabify(s + "*", 1));  
		}
//		System.out.println(legendStr);
		System.out.println(txt.toString());
		System.exit(0);
		
		double[] train = new double[6];
//		List<Double> dfg = Arrays.asList(new Double[]{50.0, 12.5, 12.5});
//		System.out.println(ToolBox.stDev(dfg));
//		System.out.println((-1.0/80) * (Math.log(2) / Math.log(2)));
//		System.out.println(Math.log(2*4*8)/Math.log(2));
		System.exit(0);
		
//		String[][] csvTable = new String[4][EvaluationManager.CSV_LEGEND.size()];
//		csvTable[0] = 
//		EvaluationManager.Metric[] sdf = EvaluationManager.CSV_LEGEND.toArray(new EvaluationManager.Metric[0]);
//		System.out.println(Arrays.toString(sdf));
		System.exit(0);
		
		Integer[] summedEFs = new Integer[4];
		System.out.println(Arrays.toString(summedEFs));
		System.exit(0);
		
		String val1 = "4";
		String val2 = "8";
		double bob = (4.0 / 8);
		System.out.println(bob);
		double ed = ((double) Integer.parseInt(val1) / Integer.parseInt(val2)) * 100.0;
		System.out.println(ed);
		System.exit(0);
		
		String q = 
			ToolBox.tabify("acc", 1) + ToolBox.tabify("I", 1) + ToolBox.tabify("O", 1);
		System.out.println(q);
		q = q.replace(ToolBox.tabify("acc", 1), ToolBox.tabify("acc*", 1));
		q = q.replace("I" + " ", "R" + "*");
		q = q.replace("O" + " ", "R" + "*");
		System.out.println(q);
		System.exit(0);
//		System.out.println(Math.log(0.5) / Math.log(2));
//		System.out.println(Math.log(2.0) / Math.log(2));
//		(Math.log(p) / Math.log(2));
//		System.out.println(
//			Math.log(8)/Math.log(2) +
//			Math.log(4)/Math.log(2) +
//			Math.log(2)/Math.log(2));
		
		String[] currValsDur = new String[]{"a", "b", "c"};  
		System.out.println(Arrays.toString(currValsDur));
		String[] currValsDurAlt = currValsDur.clone();
		currValsDurAlt[0] = "z";
		System.out.println(Arrays.toString(currValsDurAlt));
		System.out.println(Arrays.toString(currValsDur));
		
		System.exit(0);
		
		
		List<Integer> stil = Arrays.asList(new Integer[]{0, 1, 2, 3, 4});
		System.out.println(stil.get(5));
		int ind = 4;
		int nextInd = ind + 1;
		boolean dit = nextInd < stil.size() && stil.get(nextInd) == 6; 
		if (dit) {
			System.out.println("hoihoi");
		}
		else {
		 System.out.println("dit werkt");	
		}
		System.exit(0);
		String ess = "0.123456";
		ess = ess.substring(ess.indexOf("."));
		System.out.println(ess);
				
		System.exit(0);
//		System.out.println(Arrays.asList(EvaluationManager.CSV_LEGEND).indexOf("fold"));
		
		System.exit(0);
		
		List<String> used = Arrays.asList(new String[]{
				"mode", "fold", "ntw_err", 
				"acc", "snd", "cmp", "I", "O", "S", "H", "cnf"}); 
			String[] leggend = new String[11];
			int pos = 0;
//			for (String s : EvaluationManager.CSV_LEGEND) {
//				if (used.contains(s)) {
//					leggend[pos] = s;
//					pos++;
//				}
//			}
			
//			System.out.println(Arrays.toString(leggend));
			
			String legendString = "";
			for (int i = 0; i < leggend.length; i++) {
				String s = leggend[i];
				int numTabs = 1;
				if (s.equals("ntw_err") || s.equals("AVC") || 
					(i != 0 && leggend[i-1].equals("AVC"))) { // TODO AVC length OK?
					numTabs = 2;
				}
				legendString = legendString.concat(ToolBox.tabify(s, numTabs));
			}
			System.out.println(legendString);
		
		System.exit(0);
		
		
		System.exit(0);
		
//		List<double[]> outpt = 
//			ToolBox.getStoredObjectBinary(new ArrayList<double[]>(), new File(
//			"F:/PhD/data/stored/MM/ICASSP/bach/4vv/LTM/n=2/fold_05/MM_ioi-outputs-test.ser"));
		List<double[]> outpt = new ArrayList<double[]>();
		outpt.add(new double[]{1.0, 0.0, 0.0, 0.0, 0.0});
		outpt.add(new double[]{0.0, 1.0, 0.0, 0.0, 0.0});
		outpt.add(new double[]{1.0, 0.0, 0.0, 0.0, 0.0});
		outpt.add(new double[]{0.0, 1.0, 0.0, 0.0, 0.0});
		outpt.add(new double[]{1.0, 0.0, 0.0, 0.0, 0.0});
		outpt.add(new double[]{0.0, 1.0, 0.0, 0.0, 0.0});
		outpt.add(new double[]{1.0, 0.0, 0.0, 0.0, 0.0});
		outpt.add(new double[]{0.0, 1.0, 0.0, 0.0, 0.0});
		outpt.add(new double[]{1.0, 0.0, 0.0, 0.0, 0.0});
		outpt.add(new double[]{0.0, 1.0, 0.0, 0.0, 0.0});
//		Transcription ttt = new Transcription(
//			new File("F:/PhD/data/MIDI/bach/4vv/bach-WTC2-fuga_9-BWV_878"), null);
//		List<List<Double>> labls = ttt.getVoiceLabels();
		List<List<Double>> labls = new ArrayList<List<Double>>();
		labls.add(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 0.0, 0.0}));
		labls.add(Arrays.asList(new Double[]{1.0, 0.0, 0.0, 0.0, 0.0}));
		labls.add(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 0.0, 0.0}));
		labls.add(Arrays.asList(new Double[]{1.0, 0.0, 0.0, 0.0, 0.0}));
		labls.add(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 0.0, 0.0}));
		labls.add(Arrays.asList(new Double[]{1.0, 0.0, 0.0, 0.0, 0.0}));
		labls.add(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 0.0, 0.0}));
		labls.add(Arrays.asList(new Double[]{1.0, 0.0, 0.0, 0.0, 0.0}));
		labls.add(Arrays.asList(new Double[]{0.0, 1.0, 0.0, 0.0, 0.0}));
		labls.add(Arrays.asList(new Double[]{1.0, 0.0, 0.0, 0.0, 0.0}));
		System.out.println(outpt.size());
		System.out.println(labls.size());
		for (double[]d : outpt) {
			System.out.println(Arrays.toString(d));
		}
		for (List<Double> l : labls) {
			System.out.println(l);
		}
//		double[] CE = ErrorCalculator.calculateCrossEntropy(outpt, labls);
//		System.out.println(CE[0]);
//		System.out.println(CE[1]);
//		System.out.println(CE[2]);
		System.exit(0);
		
		System.out.println(3 + Math.log(0));
		System.exit(0);
		
		List<Double> wghts = Arrays.asList(new Double[]{25.640509807544944, 2.592890742386053, 1.3861076277876652, 2.4900749984779114, 1.3194960872824257});
		List<Double> outputsAtIndex0 = Arrays.asList(new Double[]{1.531505821765811E-14, 0.06177479691800602, 0.08118084197938906, 0.06978400843350603, 0.2838282828871432}); 
		List<Double> outputsAtIndex2 = Arrays.asList(new Double[]{0.999749174923275, 0.7961742262067772, 0.37495735221368903, 0.7533699892420679, 0.5385614873663986});
		System.out.println(ToolBox.weightedGeometricMean(outputsAtIndex0, wghts));
		System.out.println(ToolBox.weightedGeometricMean(outputsAtIndex2, wghts));
		System.exit(0);
		
		List<Double> weights = 
			ToolBox.getStoredObjectBinary(new ArrayList<Double>(), 
			new File("F:/PhD/data/results/ICASSP/bach/4vv/ENS_LTM-all-b=1/fold_01/" + 
			Runner.modelWeighting + ".ser"));
		System.out.println(weights);
		List<double[]> NN = ToolBox.getStoredObjectBinary(new ArrayList<double[]>(), new File(
			"F:/PhD/data/stored/NN/ICASSP/bach/4vv/N/fwd/fold_01/NN-outputs-train.ser"));
		double[] NN_1 = NN.get(5339);
		System.out.println(Arrays.toString(NN_1));
		List<double[]> MMp = ToolBox.getStoredObjectBinary(new ArrayList<double[]>(), new File(
			"F:/PhD/data/stored/MM/ICASSP/bach/4vv/LTM/n=2/fold_01/MM_p-outputs-train.ser"));
		double[] MMp_1 = MMp.get(5339);
		System.out.println(Arrays.toString(MMp_1));
		List<double[]> MMd = ToolBox.getStoredObjectBinary(new ArrayList<double[]>(), new File(
			"F:/PhD/data/stored/MM/ICASSP/bach/4vv/LTM/n=2/fold_01/MM_d-outputs-train.ser"));
		double[] MMd_1 = MMd.get(5339);
		System.out.println(Arrays.toString(MMd_1));
		List<double[]> MMrp = ToolBox.getStoredObjectBinary(new ArrayList<double[]>(), new File(
			"F:/PhD/data/stored/MM/ICASSP/bach/4vv/LTM/n=2/fold_01/MM_rp-outputs-train.ser"));
		double[] MMrp_1 = MMrp.get(5339);
		System.out.println(Arrays.toString(MMrp_1));
		List<double[]> MMioi = ToolBox.getStoredObjectBinary(new ArrayList<double[]>(), new File(
			"F:/PhD/data/stored/MM/ICASSP/bach/4vv/LTM/n=2/fold_01/MM_ioi-outputs-train.ser"));
		double[] Mioi_1 = MMioi.get(5339);
		System.out.println(Arrays.toString(Mioi_1));
		
		System.exit(0);
		List<Double> bIs1 = 
			ToolBox.getStoredObjectBinary(new ArrayList<Double>(), 
			new File("F:/PhD/data/results/ICASSP/bach/4vv/ENS_LTM-all-b=1/fold_01/" + 
			Runner.modelWeighting + ".ser"));
		List<Double> bIs12 = 
				ToolBox.getStoredObjectBinary(new ArrayList<Double>(), 
				new File("F:/PhD/data/results/ICASSP/bach/4vv/ENS_LTM-all-b=1/fold_02/" + 
				Runner.modelWeighting + ".ser"));
		List<Double> bIs5 = 
			ToolBox.getStoredObjectBinary(new ArrayList<Double>(), 
			new File("F:/PhD/data/results/ICASSP/bach/4vv/ENS_LTM-all-b=5/fold_01/" + 
			Runner.modelWeighting + ".ser"));
		List<Double> bIs52 = 
				ToolBox.getStoredObjectBinary(new ArrayList<Double>(), 
				new File("F:/PhD/data/results/ICASSP/bach/4vv/ENS_LTM-all-b=5/fold_02/" + 
				Runner.modelWeighting + ".ser"));
		
		System.out.println(bIs1);
//		System.out.println(bIs12);
		System.out.println(bIs5);
//		System.out.println(bIs52);
//		getPValue();
		System.exit(0);
//		String str = Integer.toString(j);
//		str = new StringBuilder(str).insert(str.length()-2, ".").toString();
		
//		System.out.println(Dataset.FUGUES_4VV_NAMES);
//		Dataset de = new Dataset();
//		System.out.println(Dataset.bla.get(DatasetID.TAB_3VV));
		System.out.println(DatasetID.INT_3vv.getPieceNames());
		System.exit(0);
		
		Dataset des = new Dataset(DatasetID.INT_3vv);
		System.out.println(des.getPieceNames());
		System.out.println(des.getNumPieces());
		System.exit(0);
		
		List<Integer> sddfs = Arrays.asList(new Integer[]{0, 1, 2, 3, 4});
		System.out.println(sddfs.subList(1, sddfs.size()));
		
		System.out.println(ToolBox.tabify("", 2) + "bla");		
		System.exit(0);
		
		String ds = "een lange String\r\nbla";
		System.out.println(ds);
		ds = new StringBuilder(ds).insert(ds.lastIndexOf("\r\n"), " iets erbij").toString();
		System.out.println(ds);
		
//		ToolBox.tabify(ds, 1);
//		System.out.println(ds + "dfd");
		System.exit(0);
		
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.UK);
		otherSymbols.setDecimalSeparator('.'); 
		DecimalFormat decForm = new DecimalFormat("0.0   ", otherSymbols);
		
		double[] output = new double[]{0.0, 1.0, 1.0, 0.0, 0.0};
		String[] formatted = new String[output.length]; 
		for (int j = 0; j < output.length; j++) {
			formatted[j] = decForm.format(output[j]); 
		}
		System.out.println(Arrays.toString(formatted));
		System.exit(0);
		
		String sth = "Dit is GODVER een string";
		System.out.println(sth);
		sth = sth.replace("GODVER", "NONDEDJU");
		System.out.println(sth);
		System.exit(0);
		
//		File tf = new File("F/PhD/data/" + "Encodings/Dataset/3vv/" + s + ".txt");
		File mff = new File("F:/PhD/data/MIDI/test/testpiece");
					
//		Tablature tabb = new Tablature(tf, true);
//		Transcription transs = new Transcription(mf, tf);
		Transcription transs = new Transcription(mff, null);
				
		FeatureGeneratorChord fgcc = new FeatureGeneratorChord();
		
//		List<List<Integer>> voices = new ArrayList<List<Integer>>();
//		voices.add(Arrays.asList(new Integer[]{2}));
//		voices.add(Arrays.asList(new Integer[]{1}));
//		voices.add(Arrays.asList(new Integer[]{0}));
//		voices.add(Arrays.asList(new Integer[]{3}));
//		List<List<Integer>> ktj = fgcc.getVoiceCrossingInformationInChord(pitches, voices);
//		System.out.println(ktj.get(0));
//		System.out.println(ktj.get(1));
//		System.out.println(ktj.get(2));
//		System.exit(0);		
		Integer[][] btpp = null;
//		Integer[][] btp = tab.getBasicTabSymbolProperties();
//		Integer[][] btp = null;
//		Integer[][] bnpp = transs.getBasicNoteProperties();

		
		List<List<Double>> vll = transs.getVoiceLabels();
		
		int size = 4;
		int numV = 5;
//		List<Integer> pitches = Arrays.asList(new Integer[]{10});
//		List<Integer> pitches = Arrays.asList(new Integer[]{10, 20});
//		List<Integer> pitches = Arrays.asList(new Integer[]{10, 20, 30});
		List<Integer> pitches = Arrays.asList(new Integer[]{10, 20, 30, 40});
//		List<Integer> pitches = Arrays.asList(new Integer[]{10, 20, 30, 40, 50});
		Integer[][] bnpp = new Integer[size][8];
		bnpp[0] = new Integer[]{10, 0, 4, 1, 4, 0, size, 0};
		bnpp[1] = new Integer[]{20, 0, 4, 1, 4, 0, size, 1};
		bnpp[2] = new Integer[]{30, 0, 4, 1, 4, 0, size, 2};
		bnpp[3] = new Integer[]{40, 0, 4, 1, 4, 0, size, 3};
//		bnpp[4] = new Integer[]{50, 0, 4, 1, 4, 0, size, 4};
		
		List<List<Integer>> alll = 
			fgcc.enumerateVoiceAssignmentPossibilitiesForChord(null, bnpp, null, 0, numV);
		for (List<Integer> l : alll) {
			System.out.println(l);
		}
		System.out.println(alll.size());
		
		
		int withMore = 0;
		for (List<Integer> l : alll) {
			List<List<Double>> cvl = DataConverter.getChordVoiceLabels(l);
			List<List<Integer>> vcs = new ArrayList<List<Integer>>();
			for (List<Double> ll : cvl) {
				vcs.add(DataConverter.convertIntoListOfVoices(ll));
			}
			List<List<Integer>> io = Transcription.getVoiceCrossingInformationInChord(pitches, vcs);
			if ((io.get(1).size() / 2) > 2) {
				withMore++;
			}
		}
//		System.out.println(withMore);
		
		System.exit(0);
		
		
//		List<Integer> liest = Arrays.asList(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 19, 132});
//		System.out.println(ToolBox.breakList(liest, 5, "\t") + "hoi");
		
//		String result = "00,01,,03,04,";
//		System.out.println(Arrays.toString(result.split(",")));
			
		String qwe = "00.123456789";
		double dubl = Double.parseDouble(qwe);
		System.out.println(dubl);
		System.exit(0);
		
		
		
		String os = new String(
				"00,01,02,03" + "\r\n" +
				"10,11,12,13" + "\r\n" +
				"20,21,22,23"
		);
		
		String[] oss = os.split("\r\n");
		System.out.println("rows = " + oss.length);
		System.out.println("cols = " + oss[0].split(",").length);
		System.exit(0);
		
		
		
		
		ErrorFraction eef1 = new ErrorFraction(1, 79);
		ErrorFraction eef2 = new ErrorFraction(1, 79);	
		junit.framework.TestCase.assertEquals(eef1, eef2);
		
		
		System.exit(0);
		
		String[][] ta = new String[3][3];
		for (String[] ss : ta) {	
			Arrays.fill(ss, "");
		}
//		Arrays.fill(ta, "d");
//		for (int i = 0; i < ta.length; i++) {
		for (String[] ss : ta) {	
//			String[] tal = ta[i];
			for (String s : ss) {
				System.out.println(s);
			}
		}
		System.exit(0);
		
		ta[0] = new String[]{"name1", "", ""};
		ta[1] = new String[]{"33", "34.1", "0.1234"};
		ta[2] = new String[]{"0.234", "34.1", "0.0002"};
		
		System.out.println(ToolBox.createCSVTableString(ta));
		System.exit(0);
		
		
		
		String sr = ToolBox.tabify("1", 1).concat(ToolBox.tabify("2", 1).concat(ToolBox.tabify("3", 1)));
		String sr2 = ToolBox.tabify("", 1).concat(ToolBox.tabify("", 1).concat(ToolBox.tabify("", 1)));
		System.out.println(sr);
		System.out.println(sr2);
		System.exit(0);
		BigDecimal nu = new BigDecimal(BigInteger.valueOf(5000));
		BigDecimal den = new BigDecimal(BigInteger.valueOf(10000));
		
		BigDecimal bd = nu.divide(den);
		System.out.println(bd);
		
//		System.out.println(Integer.MAX_VALUE);
//		System.out.println(Math.pow(2, 31));
		
		
//		ErrorFraction ef1 = new ErrorFraction(1, 2);
//		ErrorFraction ef2 = new ErrorFraction(12, 12);
//		ErrorFraction ef3 = new ErrorFraction(123, 123);
///		ErrorFraction ef4 = new ErrorFraction(123, 1234);
//		ErrorFraction ef5 = new ErrorFraction(12345, 12345);
//		ErrorFraction ef6 = new ErrorFraction(123456, 123456);
		
		
		
		String ef1 = "1";
		String ef2 = "12";
		String ef3 = "123";
		String ef4 = "1234";
		String ef5 = "12345";
		String ef6 = "123456";
		String ef7 = "1234567";
		String ef8 = "12345678";
		String ef9 = "123456789";
		String ef10 = "123456789012345678901234";
		
		String t1 = "\t";
		String t2 = "\t\t";
		String t3 = "\t\t\t"; 
		String t4 = "\t\t\t\t";
		
		System.out.println("x\t\t\t\tx\t\t\t\tx");
		System.out.println(ef10 + t1 + ef10 + t1 + ef8);
		
		
		
//		String joep = stringify(ef, 16);
//		System.out.println(joep + "x");

		System.exit(0);
		
		String bla = "";
		bla = bla.concat("TRAINING\r\n");
		bla = bla.concat("\t\t" + "ntw_err" + "\t\t" + "acc" + "\t\t" + "acc_dur" + "\t\t" +
		"prc" + "\t\t" + "rcl" + "\t\t" + "snd" + "\t\t" + "cmp" + "\t\t" + "AVC\r\n");
		bla = bla.concat("fold_01\t\tbla");
		bla = bla.concat("");
		System.out.println(bla);
		System.exit(0);
		
		ToolBox aut = new ToolBox();
		ErrorFraction[] st = ToolBox.getStoredObjectBinary(new ErrorFraction[]{}, 
			new File("F:/PhD/data/stored/NN/SMC/bach/4vv/N/lmbd=1.0E-5/HL=1/fold_01/eval-train.ser"));
		System.out.println("acc     = " + st[ACC]);
		System.out.println("acc dur = " + st[ACC_DUR]);
		System.out.println("prc = " + st[PRC]);
		System.out.println("rcl = " + st[RCL]);
		System.out.println("snd = " + st[SND]);
		System.out.println("cmp = " + st[CMP]);
		System.out.println("AVC = " + st[AVCO]);
		System.exit(0);
		
		
		List<Integer> rty = 
			Arrays.asList(new Integer[]{1, 5, 8, 9, 23, 25, 26, 27, 28, 29, 27, 28, 67, 68, 69, 71, 73, 75, 77, 79, 80, 81, 82, 83, 84, 93, 94, 95, 96, 97, 100, 250, 300, 350, 400, 450, 500, 550, 600, 601, 602, 603, 604, 605, 606, 607, 608, 610, 620, 630, 640, 650, 660, 670, 680, 690, 700, 743, 768, 865, 915, 1011, 1204, 1283, 1387, 1509, 1523, 1527, 1530, 1537, 1539, 1541, 1551, 1555, 1558, 1565, 1579, 1583, 1586, 1593, 1595, 1597, 1615, 2198, 2574, 2811, 2825, 2828, 2831, 2834, 2837, 3046, 3189, 3214, 3825, 3848, 3967, 4715, 4722, 4725, 4727, 4733, 4744, 5025, 5315, 5332, 5335, 5445, 5456, 5847, 6178, 6211, 6213, 6373, 6402, 6624, 6797, 6825, 7051, 7139, 7275, 7341, 7347, 7352, 7382, 7533, 7545, 7553, 7558, 7559, 7969, 8521, 8525, 8543, 8547, 8565, 8569, 9056, 9058, 9226, 9270, 9411, 9414, 9724, 9861, 10107, 10155, 10159, 10160, 10166, 10170, 10173, 10176, 10179, 10182, 10185, 10188, 10191, 10194, 10197, 10228, 10271, 10324, 10649, 10651, 10906, 10962, 11045, 11063, 11311, 11401, 11402, 11419, 11437, 11612, 11649, 11667, 11876, 12770, 12794, 12804, 12858, 12876, 12942, 12946, 12954, 12970, 12998, 13003, 13007, 13020, 13058, 13061, 13066, 13073, 13086, 13098, 13105, 13137, 13148, 13232, 13237, 13242, 13258, 13337, 13351, 13354, 13363, 13366, 13458, 13478, 13492, 13775, 13793, 14196, 14463, 14571, 14578, 14580, 14798, 14882, 14885, 15245, 15445, 15533, 15651, 15737, 15773, 15947, 15991, 16078, 16180, 16216, 16261, 16269, 16351, 16388, 16493, 16594, 16684, 16699, 16720, 16730, 16733, 16736, 16740, 16748, 16751, 16754, 16758, 16846, 17059, 17081, 17143, 17320, 17368, 18144, 18184, 18201, 18339, 18426, 19025, 19275, 19303, 19324, 19437, 19490, 19529, 19827, 19845, 19899, 19917, 19936, 19955, 20019, 20021, 20099, 20322, 20388, 20442, 20460, 20514, 20569, 20698, 20722, 20764, 20845, 20889, 20925, 20940, 21018, 21046, 21246, 22236, 22365, 22378, 22379, 22438, 22553, 22733, 22926, 22930, 22996, 22997, 23028, 23037, 23046, 23110, 23249, 23301, 23701, 23882, 24126, 24163, 24472, 24634, 24736, 24822, 25037, 25368, 25476, 25486, 25531, 25537, 25543, 25549, 25621, 25860, 25866, 25872, 26000, 26046, 26050, 26051, 26173, 26184, 26258, 26274, 26275, 26333, 26470, 26599, 26629, 26630, 26631, 26632, 27240  });
		System.out.println(ToolBox.breakList(rty, 15, "  "));
		System.exit(0);
		
		List<Integer> considerSolved = new ArrayList<Integer>();
		boolean resolved = false;
		while (resolved == false) {	
			for (int i = 0; i < 5; i++) {
				resolved = addSthToList(considerSolved, i);
			}
		}
		
		System.out.println(considerSolved);
		System.exit(0);
		
		
		double w = (Math.log(0.4) / Math.log(2)) + (Math.log(0.4) / Math.log(2)) +
			(Math.log(0.4) / Math.log(2)) + (Math.log(0.4) / Math.log(2)) +
			(Math.log(0.1) / Math.log(2)) + (Math.log(0.1) / Math.log(2));
		System.out.println(w);
		
		List<List<Double>> lbls = new ArrayList<List<Double>>();
		for (int i = 0; i < 100000; i++) {
			lbls.add(Transcription.VOICE_0);
			lbls.add(Transcription.VOICE_1);
			lbls.add(Transcription.VOICE_2);
		}
		List<double[]> outps = new ArrayList<double[]>();
		for (int i = 0; i < 100000; i++) {
			outps.add(new double[]{0.1, 0.2, 0.1, 0.3, 0.0});
			outps.add(new double[]{0.1, 0.2, 0.4, 0.3, 0.0});
			outps.add(new double[]{0.1, 0.4, 0.1, 0.3, 0.0});
		}
		
		System.out.println(ToolBox.getTimeStamp());
		System.out.println(lbls.size());
		ErrorCalculator ec = new ErrorCalculator();
//		double[] ce = ec.calculateCrossEntropy(outps, lbls);
//		System.out.println(ce[0]);
//		System.out.println(-ce[1]/ce[2]);
		System.out.println(ToolBox.getTimeStamp());
		
		
		
//		getPValue();
		System.exit(0);
		

		ToolBox at = new ToolBox();
		List<List<Double>> fold1tr = at.getStoredObject(new ArrayList<List<Double>>(), 
			new File("F:/PhD/data/stored/MM/SMC/bach/4vv/LTM/n=1/fold_01/labels-train.xml"));
		List<List<Double>> fold3te = at.getStoredObject(new ArrayList<List<Double>>(), 
			new File("F:/PhD/data/stored/MM/SMC/bach/4vv/LTM/n=1/fold_03/labels-test.xml"));
		
		for (List<Double> l : fold3te) {
			System.out.println(l);
		}
		System.exit(0);
		
//		System.out.println(fold1.size());
//		System.out.println(fold2.size());
//		
//		for (int i = 0; i < fold1.size(); i++) {
////			List<Double> asList1 = AuxiliaryTool.convertArrayToList(fold1.get(i));
////			List<Double> asList2 = AuxiliaryTool.convertArrayToList(fold2.get(i));
//			
//			double[] asList1 = fold1.get(i);
//			double[] asList2 = fold2.get(i);
////			System.out.println(i);
////			System.out.println(Arrays.toString(asList1));
////			System.out.println(Arrays.toString(asList2));
//			for (int j = 0; j < asList1.length; j++) {
//				if (asList1[j] != asList2[j]) {
//					System.out.println(i);
//				}
//			}
//		}
		
		System.exit(0);
		
		
		List<Integer> lul = new ArrayList<Integer>();
		lul.add(1); lul.add(1); lul.add(1); lul.add(8); lul.add(1);
		System.out.println(lul.indexOf(8));
		System.exit(0);
		
		String[] expPaths = new String[]{"eerste", "tweede"};
		System.out.println(expPaths[0]);
		System.out.println(expPaths[1]);
		for (int i = 0; i < expPaths.length; i++) {
//			expPaths[0] = expPaths[0].concat("no_CV/");
			String s = expPaths[i];
			if (s != null) {
				expPaths[i] = s.concat("no_CV/");
			}
		}
		System.out.println(expPaths[0]);
		System.out.println(expPaths[1]);
		
//		getPValue();
		System.exit(0);
		
		double[] dbl = new double[]{1.0, 2.0, 3.0};
		System.out.println(Arrays.toString(dbl));
		System.exit(0);
		
		System.out.println(Math.log(Math.E));
		System.out.println(Math.log(Math.E*Math.E));
		System.out.println(Math.log(Math.E*Math.E*Math.E));
		
		double one = Math.log(Math.E);
		double two = Math.log(Math.pow(Math.E, 2));
		double thre = Math.log(Math.pow(Math.E, 3));
		double four = Math.log(Math.pow(Math.E, 4));
		System.out.println(one + "-" + two + "-" + thre + "-" + four);
		
//		List<double[]> allOutputs = new ArrayList<double[]>();
////		double[] currNNOutp = networkOutputs.get(i);
//		allOutputs.add(new double[]{0.0, 0.0, 0.0});
//		List<double[]> currMMOutps = new ArrayList<double[]>();
//		currMMOutps.add(new double[]{1.0, 1.0, 1.0});
//		currMMOutps.add(new double[]{2.0, 2.0, 2.0});
//		allOutputs.addAll(currMMOutps);
//		for (double[]d : allOutputs) {
//			System.out.println(Arrays.toString(d));
//		}
		System.exit(0);
		
		File midiTestpiece1 = new File(Runner.midiPathTest + "testpiece");
		File encodingTestpiece1 = new File(Runner.encodingsPathTest + "testpiece.txt");
		
		Tablature tba = new Tablature(encodingTestpiece1, false);
		Transcription tar = new Transcription(midiTestpiece1, encodingTestpiece1);
		System.out.println(tar.getNoteSequence().size());
		System.out.println(tba.getBasicTabSymbolProperties().length);
		System.out.println(tar.getVoiceLabels().size());
		
		tar = new Transcription(midiTestpiece1, null);
		System.out.println(tar.getNoteSequence().size());
		System.out.println(tar.getBasicNoteProperties().length);
		System.out.println(tar.getVoiceLabels().size());
		
		System.exit(0);
		
		
		
		List<List<String>> sff = new ArrayList<List<String>>();
		sff.add(new ArrayList<String>());
		sff.add(new ArrayList<String>());
		System.out.println(sff.get(1));
		sff.get(1).addAll(Arrays.asList(new String[]{"ja", "ja", "ja"}));
		sff.get(1).set(0, "dit");
		System.out.println(sff.get(1));
		System.exit(0);
		
		
		System.out.println(sff.size());
		sff.add(Arrays.asList(new String[]{null, null, null}));
		sff.add(Arrays.asList(new String[]{null, null, null}));
		System.out.println(sff.get(1).get(2));
		for (List<String> l : sff) {
			l.set(0, "one");
			l.set(1, "two");
			l.set(2, "three");
		}
		
		System.out.println(sff.get(1).get(2));
		System.exit(0);
		
		System.out.println(Math.pow(0.9717, 2));
		System.out.println(Math.pow(0.8481, 2));
		System.exit(0);
		
		Rational total = Rational.ZERO;
		total = total.add(new Rational(1, 2));
		total =  total.add(new Rational(1, 2));
		total = total.add(new Rational(1, 2));
		System.out.println(total);
//		Rational currTotal = 
//			total.add(new Rational(1, 2).add(new Rational(1, 2).add(new Rational(1, 2))));
//		System.out.println(currTotal);
		System.exit(0);
		
		String[] finalDataPaths = new String[]{Runner.encodingsPath + "intabulations/4vv/", 
			Runner.midiPath + "/4vv/", Runner.midiPath + "/4vv/"};
		File datasetFile = new File("F/Phd/data" + "stored_datasets/" + DatasetID.WTC_4vv.toString() + ".xml");
		if (!datasetFile.exists()) {
			System.out.println("deddaschih");
			
			System.out.println();
//			Dataset trainingSet = new Dataset(DatasetID.FUGUES_4VV, finalDataPaths);
//			at.storeObject(trainingSet, datasetFile);
		}
		
		double[] zo = new double[]{1.0, 1.1, 1.2};
		System.out.println(Arrays.toString(zo));
		System.exit(0);
		
		int fuk = 4;
		Rational rtn = new Rational(7, 3);
		Rational comb = new Rational(fuk, 1).add(rtn);
		System.out.println(comb.toDouble());
		
		Rational rati = Rational.ZERO;
		rati = rati.add(new Rational(1, 2));
		System.out.println(rati);
		System.exit(0);
			
		String path = "F:/PhD/data/ISMIR-2016/bach/4vv/N_STM-n=1/lmbd=1.0E-5/HL=1/";
		OutputEvaluator oe = new OutputEvaluator();
		List<Integer> incorrPerFold = new ArrayList<Integer>();
		List<Integer> totalPerFold = new ArrayList<Integer>();
		for (int fold = 1; fold <= 10; fold++) {
			String s = "fold_0";
			if (fold == 10) {
				s = "fold_";
			}
			List<List<Double>> outputs = at.getStoredObject(new ArrayList<List<Double>>(), 
				new File(path + s + fold + "/mm-outputs.xml"));
			List<List<Double>> labels = at.getStoredObject(new ArrayList<List<Double>>(), 
				new File(path + s + fold + "/labels.xml"));
			if (outputs.size() != labels.size()) {
				System.out.println("ERROR");
				System.exit(0);
			}
			totalPerFold.add(outputs.size());
			
			for (List<Double> outp : outputs) {
				double[] outpArr = new double[outp.size()];
				for (int i = 0; i < outp.size(); i++) {
					outpArr[i] = outp.get(i);
				}
				System.out.println(outp);
				System.out.println("[" + outpArr[0] + ", " + outpArr[1] + ", " + 
					outpArr[2] + ", " + outpArr[3] + ", " + outpArr[4] + "]");
				
				int voice = OutputEvaluator.interpretNetworkOutput(outpArr, false, -1).get(0).get(0);
				double max = Collections.max(outp);
				System.out.println(voice);
			}	
			System.exit(0);
		}
		System.exit(0);
		
		List<Double> ld = Arrays.asList(new Double[]{0.123, 5/6.0, 1/2.0, 3.0000/1});
		List<List<List<Double>>> llld = new ArrayList<List<List<Double>>>();
		
		
		List<String> asStr = new ArrayList<String>();
		for (Double d: ld) {
			asStr.add(d.toString());
		}
		System.out.println(asStr);
		System.exit(0);
		
		List<Double[]> probs = 
			at.getStoredObject(new ArrayList<Double[]>(), 
			new File("F:/PhD/data/results/NN-mld/exp_1/bach/4vv/probs/" + 
		  "mm_outp_testBach - WTC1, Fuga 24 in b minor (BWV 869).xml"));
		
		for (Double[] dou : probs) {
			System.out.println("[" + dou[0] + "\t" + dou[1] + "\t" + dou[2] + "\t" + dou[3] + "\t"  + dou[4]);
		}
		System.exit(0);
		
		String begin = ToolBox.getTimeStamp();
		for (int i = 0; i < 1000000; i++) {
			System.out.println(i);
		}
		String eind = ToolBox.getTimeStamp();
		System.out.println(begin);
		System.out.println(eind);
		System.exit(0);
		
		double[] currentResultsMatrix = new double[]{0.1111111, 0.2222222, -1, 
				0.444444, 0.5555555, 0.666666, 0.777777, 0.888888, 0.999999, 0.111111, 0.222222,
				0.333333, 0.444444};
		
		DecimalFormat decimalFormat = new DecimalFormat("0.0000");
		DecimalFormat alternativeDecimalFormat = new DecimalFormat("0.000000");
		List<Integer> columnsToSkip = new ArrayList<Integer>(); 
//		columnsToSkip.add(0);
		List<Integer> columnsWithAlternativeDecimalFormat = new ArrayList<Integer>();
//		columnsWithAlternativeDecimalFormat.add(1);
		columnsWithAlternativeDecimalFormat.add(0);
		RoundingMode roundingMode = RoundingMode.DOWN; 
		double naValue = -1.0;
//		// Do not include lambda
//		double[] withoutLambda = Arrays.copyOfRange(currentResultsMatrix[0], 1, currentResultsMatrix[0].length);
//			
//		double[][] wrapped = new double[][]{withoutLambda};
				
//		int roww = lambdas.indexOf(currentLambda);
//		double[][] resultsMatrixCurrentLambda = new double[1][currentResultsMatrix[0].length];
//		resultsMatrixCurrentLambda[0] = currentResultsMatrix[roww];
		double[][] currentResultsMatrixWrapped = new double[1][currentResultsMatrix.length];
		currentResultsMatrixWrapped[0] = currentResultsMatrix;
		
		String[] legend = null;
		boolean isTablatureCase=  true;
		if (isTablatureCase) {
			legend = new String[]{
				"netw_err", "acc", "acc_dur", "snd", "cmp", 
				"acc", "acc_dur", "snd", "cmp", 
				"acc", "acc_dur", "snd", "cmp"
			};
		}
		else {
			legend = new String[]{
				"netw_err", "acc", "prc", "rcl", "snd", "cmp", "AVC",
				"acc", "prc", "rcl", "snd", "cmp", "AVC", 
				"acc", "prc", "rcl", "snd", "cmp", "AVC"
			};
		}
		
		String matrixAsTable = ToolBox.writeMatrixAsTable(legend, currentResultsMatrixWrapped, 
			/*resultsMatrixCurrentLambda,*/ 
			decimalFormat, alternativeDecimalFormat, roundingMode, columnsToSkip, 
			columnsWithAlternativeDecimalFormat, naValue);
		System.out.println(matrixAsTable);
		System.exit(0);
		
		List<String> aList = new ArrayList<String>();
		aList.add("a");
		aList.add("b");
		aList.add("c");
		List<String> argPieceNames = new ArrayList<String>();
		System.out.println(aList);
		for (String s: aList) {
			argPieceNames.add(s.concat("-shorter"));
		}
		System.out.println(argPieceNames);
		System.exit(0);
		
		
		
		
		
		
//		Collection<Collection> c = new Collection();
		
		HashMap<String, String> mep = new HashMap<String, String>();
		mep.put("firstName", "reinier");
		mep.put("secondName", "valk");
		
		String hoi = mep.get("secondName");
		System.out.println(hoi);
		System.exit(0);
		
		List<Double> actualDurationLabel = new ArrayList<Double>();
		actualDurationLabel.add(0.0); actualDurationLabel.add(0.0); 
		actualDurationLabel.add(0.0); actualDurationLabel.add(0.0);
		actualDurationLabel.add(0.0); actualDurationLabel.add(0.0);
		actualDurationLabel.add(0.0); actualDurationLabel.add(1.0);
		actualDurationLabel.add(0.0); actualDurationLabel.add(0.0); 
		actualDurationLabel.add(0.0); actualDurationLabel.add(0.0);
		actualDurationLabel.add(0.0); actualDurationLabel.add(0.0);
		actualDurationLabel.add(0.0); actualDurationLabel.add(1.0);
		actualDurationLabel.add(0.0); actualDurationLabel.add(0.0); 
		actualDurationLabel.add(0.0); actualDurationLabel.add(0.0);
		actualDurationLabel.add(0.0); actualDurationLabel.add(0.0);
		actualDurationLabel.add(0.0); actualDurationLabel.add(0.0);
		actualDurationLabel.add(0.0); actualDurationLabel.add(0.0); 
		actualDurationLabel.add(0.0); actualDurationLabel.add(0.0);
		actualDurationLabel.add(0.0); actualDurationLabel.add(0.0);
		actualDurationLabel.add(0.0); actualDurationLabel.add(0.0);
		Rational[] actualDurations = DataConverter.convertIntoDuration(actualDurationLabel);
		System.out.println(Arrays.toString(actualDurations));
		
		List<Rational> actualDurationsAsList = 
			Arrays.asList(new Rational[]{actualDurations[0], actualDurations[1]});
		System.out.println(actualDurationsAsList);
		
		System.out.println(Arrays.toString(actualDurations));
		System.out.println("FUK");
		System.out.println(Arrays.asList(actualDurations));
		System.exit(0);
		
		List<List<Integer>> someList = new ArrayList<List<Integer>>();
		List<Integer> listA = new ArrayList<Integer>();
		listA.add(0); listA.add(1); listA.add(2);
		List<Integer> listB = new ArrayList<Integer>();
		listB.add(3); listB.add(4); listB.add(5);
		someList.add(listA);
		someList.add(listB);
		
		List<Integer> listC = someList.get(0);
		listC.add(99);
		
		System.out.println(someList.get(0));
		System.exit(0);
		
		String trSetContents = 
				ToolBox.readTextFile(new File("F:/PhD/data/results/NN/exp_1/tab/3vv/N/lmbd=0.001/HL=1/fold=1/training_rec OLD.txt"));
//			String s = AuxiliaryTool.getAllowedCharactersAfterMarker(contents,
//				("number of training examples " + nOrC + " = "));
//			numTrainEx = Integer.parseInt(s);
		String trainingSettings = 
				trSetContents.substring(trSetContents.indexOf("bla"),
						trSetContents.indexOf("ERROR SPEC"));
			
			System.out.println(trainingSettings + "x");
			System.exit(0);
			
		File fly = new File("F:/PhD/data/files_out/NN/Thesis FINAL CHECKS/N2N/tab/4vv/lambda = 0.001/Hidden nodes = 1/Fold 1/Training process record (N).txt");
		String cnt = ToolBox.readTextFile(fly);
		String nOrC = "(notes)";
//		if (learningApproach == CHORD_TO_CHORD) {
//			nOrC = "(chords)";
//		}
		String es = ToolBox.getAllowedCharactersAfterMarker(cnt,
			("number of training examples " + nOrC + " = "));
		int numTrainEx = Integer.parseInt(es);
		System.out.println("x"+es+"x");
		System.out.println(numTrainEx);
		System.exit(0);
		
		Integer in = null;
		int num = at.getStoredObject(in, new File("F:/PhD/data/files_out/NN/Thesis FINAL CHECKS/N2N/tab/4vv/lambda = 0.001/Hidden nodes = 1/Fold 1/numTrainingExamples (N).xml"));
		System.out.println(num);
		System.exit(0);
		
		String argExpDir = "F:/PhD/data/results/NN/pr_exp_1.1/tab/4vv/N";
		int inn = argExpDir.indexOf("vb/");
		System.out.println(inn);
		String numVoicesDir = argExpDir.charAt(inn-1) + "vv/";
		System.out.println(numVoicesDir);
		
		System.exit(0);
		
		searchPiece();
		
		String start =  ToolBox.getTimeStamp();
		
		String startTraining = ToolBox.getTimeStamp();
		String endTraining = ToolBox.getTimeStamp();
		
		// 2. Test
		String startTesting = ToolBox.getTimeStamp();
		String endTesting = ToolBox.getTimeStamp();
		
		// 3. Evaluate
		String startEvaluation = ToolBox.getTimeStamp();
		String endEvaluation = ToolBox.getTimeStamp();
		
		String end = ToolBox.getTimeStamp();
		
		String runningTimes = "start time:       " + start + "\r\n" + "end time:         " + end + "\r\n" + "\r\n";;
		String specified = "start training:   " + startTraining + "\r\n" + "end training:     " + endTraining + "\r\n" +
		"start test:       " + startTesting + "\r\n" + "end test:         " + endTesting + "\r\n" +
		"start evaluation: " + startEvaluation + "\r\n" + "end evaluation:   " + endEvaluation;
		
		runningTimes = runningTimes.concat(specified); 
		
		System.out.println(runningTimes);
		System.exit(0);
		
		
//		String path = "one";
//		String currentPath = path;
//		System.out.println(path);
//		System.out.println(currentPath);
//		currentPath = currentPath.concat("STH");
//		System.out.println(path);
//		System.out.println(currentPath);
		System.exit(0);
		
		
		File fgg = new File("F:/PhD/data/results/Neural network/Testje/N2N/tab/3vv/lambda = 0.001/" + 
			"Hidden nodes = 1/Fold 4/Initial weights (N).xml");
		List<Double> dgfr = at.getStoredObject(new ArrayList<Double>(), fgg);
		System.out.println(dgfr.size());
		System.exit(0);
		
		
//		String pie = "Ochsenkun 1558 - Absolon fili mi";
//		String pie = "Ochsenkun 1558 - In exitu Israel de Egipto";
//		String pie = "Ochsenkun 1558 - Qui habitat";
//		String pie = "Rotta 1546 - Bramo morir per non patir piu morte";
//		String pie = "Phalese 1547 - Tant que uiuray [a4]";
//		String pie = "Ochsenkun 1558 - Herr Gott lass dich erbarmen";
		String pie = "Abondante 1548 - mais mamignone";
//		String pie = "Phalese 1563 - LAs on peult";
//		String pie = "Barbetta 1582 - Il nest plaisir";
		
		
		
		File en = new File("F/PhD/data/" + "Encodings/Dataset/4vv/" + pie + ".txt");
		File mi = new File("F/PhD/data/" + "MIDI/Tablature/4vv/" + pie);
		
		Tablature tbl = new Tablature(en, true);
		Transcription trn = new Transcription(mi, null);
		
//		System.out.println(AuxiliaryTool.getTimeStamp());
////		trn.getTranscriptionChordsInternal();
//		System.out.println(AuxiliaryTool.getTimeStamp());
//		System.out.println("--------------------");
//		System.out.println(AuxiliaryTool.getTimeStamp());
//		trn.getTranscriptionChords();
//		System.out.println(AuxiliaryTool.getTimeStamp());
		
		System.exit(0);
		
		
		List<List<Double>> old = at.getStoredObject(new ArrayList<List<Double>>(), 
			new File("F:/PhD/data/Files out/Neural network/Running times/N2N/tab/3vv/Oude versie/Fold 1.xml"));
		System.out.println(old.size());
		
		List<List<Double>> neew = at.getStoredObject(new ArrayList<List<Double>>(), 
			new File("F:/PhD/data/Files out/Neural network/Running times/N2N/tab/3vv/Nieuwe versie/Fold 1.xml"));
		System.out.println(neew.size());
		
		System.out.println(old.get(1700));
		System.out.println(neew.get(1700));
		
		System.exit(0);
		
		int count = 0;
		for (int i = 0; i < old.size(); i++) {
			if (old.get(i).equals(neew.get(i)) == false) {
//				System.out.println("HOEREZOOI");
				System.out.println(i);
//				System.out.println(old.get(i));
//				System.out.println(neew.get(i));
				count++;
			}
		}
		System.out.println("--> " + count);
		System.out.println("DONE");
		
//		List<List<Integer>> ll = AuxiliaryTool.getStoredObject(new ArrayList<List<Integer>>(), 
//			new File("F:/PhD/data/Files out/HMM/Thesis/tab/3vv/optimisation/Data/chordDictionary.xml"));
//		System.out.println(ll.size());
		
//		getPValue();
		System.exit(0);
		
//		String pi = "Ochsenkun 1558 - Absolon fili mi";
//		String pi = "Ochsenkun 1558 - In exitu Israel de Egipto";
//		String pi = "Ochsenkun 1558 - Qui habitat";
//		String pi = "Rotta 1546 - Bramo morir per non patir piu morte";
//		String pi = "Phalese 1547 - Tant que uiuray [a4]";
//		String pi = "Ochsenkun 1558 - Herr Gott lass dich erbarmen";
		String pi = "Abondante 1548 - mais mamignone";
//		String pi = "Phalese 1563 - LAs on peult";
//		String pi = "Barbetta 1582 - Il nest plaisir";
		
		List<Integer> allInd = new ArrayList<Integer>();
		
		File cne = new File("F/PhD/data" + "Encodings/Dataset/4vv/" + pi + ".txt");
		File idim = new File("F/PhD/data" + "MIDI/Tablature/4vv/" + pi);
		
		Tablature bat = new Tablature(cne, true);
		Transcription snart = new Transcription(idim, cne);
		List<Integer> bwm = null; //Transcription.getBackwardsMapping(bat.getNumberOfNotesPerChord());
		for (int i = 0; i < bwm.size(); i++) {
			System.out.println(i + " " + bwm.get(i));
		}
		System.exit(0);
				
		
		List<List<TabSymbol>> tac = bat.getTablatureChords();
		List<List<Note>> trc = snart.getTranscriptionChords();
		for (int i = 0; i < tac.size(); i++) {
			if (tac.get(i).size() != trc.get(i).size()) {
				allInd.add(i);
			}
		}
		
		System.out.println(allInd);
		System.exit(0);

		// CoDs
		// N2N
//		String pref = "F:/PhD/Data/Files out/Neural network/Thesis/N2N/tab/4vv/";	
//		String suf = "gridSearch/N1/lambda = 0.001/Hidden nodes = 1/Fold "; // fwd
//		String suf = "extended/bwd/lambda = 0.001/Hidden nodes = 1/Fold "; // bwd
//		String suf = "gridSearch/N2/lambda = 3.0E-5/Hidden nodes = -2/Fold "; // fwd dur
//		String suf = "extended/bwd dur/lambda = 3.0E-5/Hidden nodes = -2/Fold "; // fwd dur
//		String suf = "extended/bidir/lambda = 0.001/Hidden nodes = 1/Fold "; // bidir
//		String suf = "extended/bidir dur/lambda = 0.001/Hidden nodes = 1/Fold "; // bidir dur
		
		// C2C
		String pref = "F:/PhD/data/Files out/Neural network/Thesis/C2C/tab/4vv/";
		String suf = "gridSearch/max = 4vv/epsilon = 0.05/lambda = 0.001/Hidden nodes = -4/Fold "; // C2C
		
		int overlooked = 0;
		int superfl = 0;
		int half = 0;
		for (int i = 1; i < 10; i++) {
			File folderr = new File(pref + suf + i);
			String[] all = folderr.list();
			for (String s : all) {
				if (s.startsWith("Application process")) {
					File f = new File(folderr + "/" + s);
					String contents = ToolBox.readTextFile(f);
					// overlooked
					String ov = "where there are actually two): ";
					int intOv = contents.indexOf(ov);
					String substr = contents.substring(intOv + ov.length());
					String over = substr.substring(0, substr.indexOf("\r\n"));
					overlooked += Integer.parseInt(over);
					// superfluous
					String sup = "where there is actually one): ";
					int intSup = contents.indexOf(sup);
					substr = contents.substring(intSup + sup.length());
					String supe = substr.substring(0, substr.indexOf("\r\n"));
					superfl += Integer.parseInt(supe);
					// half
					String ha = "voices is correct): ";
					int intHa = contents.indexOf(ha);
					substr = contents.substring(intHa + ha.length());
					String hal = substr.substring(0, substr.indexOf("\r\n"));
					half += Integer.parseInt(hal);
					
//					System.exit(0);
				}
			}
		}
		System.out.println("overlooked = " + overlooked);
		System.out.println("superfl    = " + superfl);
		System.out.println("half       = " + half);
		System.out.println(overlooked + "\t" + superfl + "\t" + half);
		System.exit(0);
		
//		String contentss = AuxiliaryTool.readTextFile(folderr);
//		int appl = contentss.indexOf("TEST");
//		String relevantt = contentss.substring(appl);
////		System.out.println(relevant);
//		for (int i = 0; i < relevantt.length(); i++) {
//			if (relevantt.charAt(i) == '[' && relevantt.charAt(i + 1) == '(') {
//				int end = relevantt.indexOf(']', i);
//				String avc = relevantt.substring(i + 2, end);
////				System.out.println(avc);
//				String first = avc.substring(0, avc.indexOf(' '));
//				String second = avc.substring(avc.indexOf("* ") + 2, avc.indexOf(')'));
////				System.out.println("BOOO " + "X" +  first + "X" + " " + "X" + second + "X");
//				System.out.println(first + "\t" + second);
//			}
//		}
		
		
		// AVCs
//		String pref = "F:/PhD/Data/Files out/Neural network/Thesis/N2N/Bach/";
//		String suf = "Sinfonias/extended/bidir/lambda = 1.0E-5/Hidden nodes = 1/Summary over all folds.txt";
//		File folderr = new File(pref + suf);
//		String contentss = AuxiliaryTool.readTextFile(folderr);
//		int appl = contentss.indexOf("TEST");
//		String relevantt = contentss.substring(appl);
////		System.out.println(relevant);
//		for (int i = 0; i < relevantt.length(); i++) {
//			if (relevantt.charAt(i) == '[' && relevantt.charAt(i + 1) == '(') {
//				int end = relevantt.indexOf(']', i);
//				String avc = relevantt.substring(i + 2, end);
////				System.out.println(avc);
//				String first = avc.substring(0, avc.indexOf(' '));
//				String second = avc.substring(avc.indexOf("* ") + 2, avc.indexOf(')'));
////				System.out.println("BOOO " + "X" +  first + "X" + " " + "X" + second + "X");
//				System.out.println(first + "\t" + second);
//			}
//		}
		System.exit(0);
		
		
		String pre = "F:/PhD/Data/Files out/Neural network/Thesis/N2N/";
		// unidir
//		String su = "tab/4vv/gridSearch/N1//lambda = 0.001/Hidden nodes = 1/Fold "; // fwd
//		String su = "tab/4vv/extended/bwd/lambda = 0.001/Hidden nodes = 1/Fold "; //bwd
//		String su = "tab/4vv/gridSearch/N2/lambda = 3.0E-5/Hidden nodes = -2/Fold "; // fwd dur
//		String su = "tab/4vv/extended/bwd dur/lambda = 3.0E-5/Hidden nodes = -2/Fold "; // bwd dur
//		String su = "Bach/4vv/gridSearch/lambda = 1.0E-5/Hidden nodes = 1/Fold "; // fwd
//		String su = "Bach/4vv/extended/bwd/lambda = 1.0E-5/Hidden nodes = 1/Fold "; // bwd
		
		// bidir
//		String su = "tab/4vv/extended/bidir/lambda = 0.001/Hidden nodes = 1/Fold "; // bidir
//		String su = "tab/4vv/extended/bidir dur/lambda = 0.001/Hidden nodes = 1/Fold "; // bidir dur
		String su = "Bach/4vv/extended/bidir/lambda = 1.0E-5/Hidden nodes = 1/Fold "; // bidir dur
		
		int allAs = 0;
		int allBs = 0;
		int allCs = 0;
		int allDs = 0;
		
		String vOrD = "number of notes";
//		String vOrD = "number of durations"; 
		
		int numConfl = 0;
		double numCorr = 0;
		
		boolean bidir = true;
		
		String testOrAppl = "Application";
		if (bidir == true) {
			testOrAppl = "Test";
		}
		
		for (int i = 1; i < 20; i++) {
			System.out.println("FOLD = " + i);
			File folder = new File(pre + su + i);
//			System.out.println(folder.getAbsolutePath());
			String[] x = folder.list();
			for (String s : x) {
				if (s.startsWith(testOrAppl + " process")) {
					File f = new File(folder + "/" + s);
					String contents = ToolBox.readTextFile(f);
					if (bidir) {
						if (contents.contains("Voices at noteIndex")) {
							contents = contents.substring(contents.indexOf("CONFLICTS"));
							for (int j = 0; j < contents.length(); j++) {
								if (contents.charAt(j) == 'V') {
									numConfl++;
								}
								if (contents.charAt(j) == '-') {
									if (contents.charAt(j+ 2) == '>' && contents.charAt(j+4) == 'c') {
									numCorr++;
									}
								}
								if (contents.charAt(j) == '-') {
									if (contents.charAt(j+ 2) == '>' && contents.charAt(j+4) == 's') {
									numCorr+= 0.5;
									}
								}
								
								
//								if (j == contents.indexOf("Voices at noteIndex")) {
//									numConfl++;
//								}
//								if (j == contents.indexOf("--> correct adaptation")) {
//									numCorr++;
//								}
//								if (j == contents.indexOf("--> semi-correct adaptation")) {
//									numCorr += 0.5;
//								}
							}
						}
					}
					else if (!bidir) {
						int confl = contents.indexOf("CONFLICT REASSIGNMENTS");
						int confl2 = contents.indexOf("CONFLICTS\r\n");
//						System.out.println(confl);
//						System.out.println(confl2);
						String relevant = contents.substring(confl, confl2);
						if (relevant.contains(vOrD)) {
							String a = vOrD + " initially predicted correctly and reassigned correctly: ";
							int intA = relevant.indexOf(a);
							int intAEnd = relevant.indexOf("  at indices [", intA);
							String numA = relevant.substring(intA, intAEnd);
							numA = numA.substring(0, numA.indexOf("\r\n"));
							numA = numA.substring(numA.indexOf(": ") + 2, numA.length());
							allAs += Integer.parseInt(numA);
	//						System.out.println(numA);
	
							String b = vOrD + " initially predicted correctly and reassigned incorrectly: ";
							int intB = relevant.indexOf(b);
							int intBEnd = relevant.indexOf("  at indices [", intB);
							String numB = relevant.substring(intB, intBEnd);
							numB = numB.substring(0, numB.indexOf("\r\n"));
							numB = numB.substring(numB.indexOf(": ") + 2, numB.length());
							allBs += Integer.parseInt(numB);
							
							String c = vOrD + " initially predicted incorrectly and reassigned correctly: ";
							int intC = relevant.indexOf(c);
							int intCEnd = relevant.indexOf("  at indices [", intC);
							String numC = relevant.substring(intC, intCEnd);
							numC = numC.substring(0, numC.indexOf("\r\n"));
							numC = numC.substring(numC.indexOf(": ") + 2, numC.length());
							allCs += Integer.parseInt(numC);
							
							String d = vOrD + " initially predicted incorrectly and reassigned incorrectly: ";
							int intD = relevant.indexOf(d);
							int intDEnd = relevant.indexOf("  at indices [", intD);
							String numD = relevant.substring(intD, intDEnd);
							numD = numD.substring(0, numD.indexOf("\r\n"));
							numD = numD.substring(numD.indexOf(": ") + 2, numD.length());
							allDs += Integer.parseInt(numD);
						}
					}
				}			
			}		
		}
		System.out.println("a\t" + allAs);
		System.out.println("b\t" + allBs);
		System.out.println("c\t" + allCs);
		System.out.println("d\t" + allDs);
		
		System.out.println("total\t" + numConfl);
		System.out.println("corr\t" + numCorr);
		System.exit(0);
		
//		System.out.println(relevant);
//		for (int i = 0; i < relevant.length(); i++) {
		
		
		Piece p = new Piece();    
	    NotationSystem notSystem = p.createNotationSystem();
	    NotationStaff staff = new NotationStaff(notSystem);
	    notSystem.add(staff);
	    NotationVoice notationVoice = new NotationVoice(staff);
	    staff.add(notationVoice);
	    NotationChord noc = new NotationChord();
	    
	    NotationChord chord = new NotationChord();
		chord.add(new Note(new ScorePitch(60), new Rational(1, 2), new Rational(1, 2)));
		chord.setMetricTime(new Rational(1, 2));
		notationVoice.add(chord);
		
		NotationChord chord2 = new NotationChord();
		chord2.add(new Note(new ScorePitch(65), new Rational(1, 2), new Rational(1, 4)));
		chord2.setMetricTime(new Rational(1, 2));
		notationVoice.add(chord2);
		
//		NotationChord chord3 = new NotationChord();
//		chord3.add(new Note(new ScorePitch(67), new Rational(1, 2), new Rational(1, 2)));
//		chord3.setMetricTime(new Rational(1, 2));
//		notationVoice.add(chord3);
		
		System.out.println(notationVoice.get(0));
		System.out.println(notationVoice.get(1));
		
		System.out.println(notationVoice.size());
//		System.out.println(notationVoice.get(0).size());
//		System.out.println(notationVoice.get(1).size());
//		System.out.println(notationVoice.get(2).size());
	    
//	    noc.add(new Note(new ScorePitch(65), new Rational(1, 2), new Rational(1, 2)));
//	    noc.add(new Note(new ScorePitch(60), new Rational(1, 2), new Rational(1, 2)));
//	    notationVoice.add(noc);
	    
	    System.out.println(p.getScore().get(0).get(0).get(0).size());
	    System.out.println(p.getScore().get(0).get(0).get(1).size());
	    System.out.println(p.getScore().get(0).get(0).get(2).size());
	    
	    System.exit(0);
	    
		
//		double a = 99.55983653522212 * 1668;
//		double b = 99.60896076287534 * 1730;
//		double c = 100.0 * 1308;
//		double d = 99.69107230370395 * 1522;
//		
//		double sum = (a/1668) + (b/1730) + (c/1308) + (d/1522);
//		
//		System.out.println(sum);
//		System.out.println(sum/4);
////		System.out.println(sum/(1668 + 1730 + 1308 + 1522));
		
		
		
		

//		File midi = new File("C:/Users/Reinier/Desktop/Sinfonias/Bach - Sinfonia 1 in C major (BWV 787)");
//		File midi = new File("C:/Users/Reinier/Desktop/Sinfonias/Bach - Sinfonia 2 in c minor (BWV 788)");
		
//		Dataset datasetMaker = new Dataset(DatasetID.WTC_FUGUES_2VV); 	  
		List<String> pieceNames = Dataset.getBachTwoVoiceFugues();
		
//		pieceNames.add("Bach - Sinfonia 1 in C major (BWV 787)");
//		pieceNames.add("Bach - Sinfonia 2 in c minor (BWV 788)");
//		pieceNames.add("Bach - Sinfonia 3 in D major (BWV 789)");
//		pieceNames.add("Bach - Sinfonia 4 in d minor (BWV 790)");
//		pieceNames.add("Bach - Sinfonia 5 in Eb major (BWV 791)");
//		pieceNames.add("Bach - Sinfonia 6 in E major (BWV 792)");
//		pieceNames.add("Bach - Sinfonia 7 in e minor (BWV 793)");
//		pieceNames.add("Bach - Sinfonia 8 in F major (BWV 794)");
//		pieceNames.add("Bach - Sinfonia 9 in f minor (BWV 795)");
//		pieceNames.add("Bach - Sinfonia 10 in G major (BWV 796)");
//		pieceNames.add("Bach - Sinfonia 11 in g minor (BWV 797)");
//		pieceNames.add("Bach - Sinfonia 12 in A major (BWV 798)");
//		pieceNames.add("Bach - Sinfonia 13 in a minor (BWV 799)");
//		pieceNames.add("Bach - Sinfonia 14 in Bb major (BWV 800)");
//		pieceNames.add("Bach - Sinfonia 15 in b minor (BWV 801)");
		
//		pieceNames.add("Bach - Inventio 1 in C major (BWV 772)");
//		pieceNames.add("Bach - Inventio 2 in c minor (BWV 773)");
//		pieceNames.add("Bach - Inventio 3 in D major (BWV 774)");
//		pieceNames.add("Bach - Inventio 4 in d minor (BWV 775)");
//		pieceNames.add("Bach - Inventio 5 in Eb major (BWV 776)");
//		pieceNames.add("Bach - Inventio 6 in E major (BWV 777)");
//		pieceNames.add("Bach - Inventio 7 in e minor (BWV 778)");
//		pieceNames.add("Bach - Inventio 8 in F major (BWV 779)");
//		pieceNames.add("Bach - Inventio 9 in f minor (BWV 780)");
//		pieceNames.add("Bach - Inventio 10 in G major (BWV 781)");
//		pieceNames.add("Bach - Inventio 11 in g minor (BWV 782)");
//		pieceNames.add("Bach - Inventio 12 in A major (BWV 783)");
//		pieceNames.add("Bach - Inventio 13 in a minor (BWV 784)");
//		pieceNames.add("Bach - Inventio 14 in Bb major (BWV 785)");
//		pieceNames.add("Bach - Inventio 15 in b minor (BWV 786)");
		
//		pieceNames.add("Bach - WTC1, Fuga 10 in e minor (BWV 855)");
//		pieceNames.add("Bach - WTC1, Fuga 10 in e minor (BWV 855) (1)");
//		pieceNames.add("Bach - WTC1, Fuga 10 in e minor (BWV 855) (2)");
//		pieceNames.add("Bach - WTC1, Fuga 4 in c# minor (BWV 849)");
//		pieceNames.add("Bach - WTC1, Fuga 22 in bb minor (BWV 867)");
		
		int totalNum = 0;
		for (String s : pieceNames) {
			System.out.println(s);
			File midi = new File("F/PhD/data" + "MIDI/Bach/WTC/2vv/" + s);
//			File midi = new File(ExperimentRunner.pathPrefix + "MIDI/Bach/Inventions/3vv/" + s);
//			File midi = new File("C:/Users/Reinier/Desktop/Sinfonias/" + s);
			Transcription tra = new Transcription(midi, null);
		
			for (int i = 0; i < 2; i++) {
				NotationVoice nv = tra.getPiece().getScore().get(i).get(0);
	
				System.out.println("voice = " + i);
				int numNotes = nv.size();
				totalNum += numNotes;
				System.out.println("numNotes = " + numNotes);
				for (int j = 0; j < numNotes; j++) {
					// Not for the last note
					if (j != numNotes - 1) {
						NotationChord left = nv.get(j);
						NotationChord right = nv.get(j + 1);
						if (left.size() > 1 || right.size() > 1) {
							System.out.println(i + " " + left.getMetricTime());
							System.out.println("MORE THAN ONE");
							System.exit(0);
						}
						Note leftN = left.get(0);
						Note rightN = right.get(0);
						Rational onsetLeftN = leftN.getMetricTime();
						Rational durLeftN = leftN.getMetricDuration();
						Rational offsetLeftN = onsetLeftN.add(durLeftN);
						Rational onsetRightN = rightN.getMetricTime();
	//					System.out.println(onsetLeftN);
	//					System.out.println(durLeftN);
	//					System.out.println(offsetLeftN);
	//					System.out.println(onsetRightN);
	//					System.exit(0);
						
						if (offsetLeftN.isGreater(onsetRightN)) {
							System.out.println(j);
							System.out.println("  OVERLAP in voice " + i + " between note \n  " + rightN + 
								"\nand \n" + leftN);
							System.out.println(offsetLeftN);
							System.out.println(onsetRightN);
//							System.exit(0);
						}
					}
				}			
			}
			System.out.println("done");
			System.out.println(totalNum);
		}
		System.exit(0);
		
		
//		getPValue();
//		getRunTimes();
//		searchPiece();
//		getNumberOfWeights();
//		getSignificanceGridSearch();
		
		System.exit(0);
		
				
		
		FeatureGenerator fg = new FeatureGenerator();
//		DatasetMaker datasetMaker = new DatasetMaker(); 	  
//		List<String> pieceNames = new ArrayList<String>();
//		pieceNames.addAll(datasetMaker.makeDataset(DatasetMaker.Dataset.WTC_FOUR_VOICE_FUGUES));
		int numVV = 4;
		int totalNotes = 0;
		int totalChords = 0;
		for (String s : pieceNames) {
			System.out.println(s);
//			File t = new File(ExperimentRunner.pathPrefix + ExperimentRunner.encodingsDir + "/" + numVV + "vv/" + s + ".txt");
//			File m = new File(ExperimentRunner.pathPrefix + ExperimentRunner.tabMidiDir +  "/" + numVV + "vv/" + s);
			File m = new File("F/PhD/data" + "MIDI/bach/WTC/" + "/" + numVV + "vv/" + s);
//			Tablature tab = new Tablature(t, true);
			Transcription tr = new Transcription(m, null);
//			Integer[][] btp = tab.getBasicTabSymbolProperties();
			Integer[][] bnp = tr.getBasicNoteProperties();
			int numNotes = bnp.length;
			System.out.println("  num notes = " + numNotes);
			int numChords = (bnp[bnp.length - 1][Transcription.CHORD_SEQ_NUM] + 1);
			System.out.println("  num chords = " + numChords);
			totalNotes += numNotes;
			totalChords += numChords;
		}
		System.out.println(totalNotes);
		System.out.println(totalChords);
		System.exit(0);
		
//		private File midiTestpiece1 = new File(ExperimentRunner.pathPrefix + "MIDI/Tests/Testpiece 1");
//	  private File encodingTestpiece1 = new File(ExperimentRunner.pathPrefix + "Encodings/Tests/Testpiece 1.txt");
		
//		File t = new File(ExperimentRunner.pathPrefix + "Encodings/Tests/Testpiece 1.txt");
//		Tablature tabl = new Tablature(t, true);
//	  File m = new File(ExperimentRunner.pathPrefix + "MIDI/Tests/Testpiece 1");
//		Transcription transc = new Transcription(m, t);
//		
//		for (Note n : transc.getNoteSequence()) {
//			System.out.println(n);
//		}
//		System.exit(0);
		
//		double[][] minMax = AuxiliaryTool.getStoredObject(new double[][]{}, 
//			new File("F:/PhD/Data/Files out/Neural network/Quick test/lambda = 1.0E-5/Hidden nodes = -4/Fold 1/Min and max feature values CHORD_TO_CHORD.xml"));
//		for (double[] d : minMax) {
//	    	System.out.println(Arrays.toString(d));
////	    	for (double d : l) {
////	    		if (d < -1 || d > 1) {
////	    			System.out.println("  GODVERDOMME " + d);
////	    		}
////	    	}
//	    }
//	    System.exit(0);
		
		
		
		System.out.println(ToolBox.getTimeStamp());
//		DatasetMaker dama = new DatasetMaker();
//		List<String> threeVV = dama.makeDataset(Dataset.WTC_THREE_VOICE_FUGUES);
		List<String> threeVV = null; // dama.makePieceNames(Dataset.INTABULATIONS_3VV);
//		List<String> fourVV = dama.makeDataset(Dataset.WTC_FOUR_VOICE_FUGUES);
//		List<String> fourVV = dama.makePieceNames(Dataset.INTABULATIONS_4VV);
		
//		FeatureGenerator fg = new FeatureGenerator();
		FeatureGeneratorChord fgch = new FeatureGeneratorChord();
		double minDistBelow = Double.MAX_VALUE;
		double maxDistBelow = Double.MIN_VALUE;
		double minDistAbove = Double.MAX_VALUE;
		double maxDistAbove = Double.MIN_VALUE;
		double minPitch = Integer.MAX_VALUE;
		double maxPitch  = Integer.MIN_VALUE;
		double minDur = Double.MAX_VALUE;
		double maxDur = Double.MIN_VALUE;
		Double minMetPos = Double.MAX_VALUE;
		Double maxMetPos = Double.MIN_VALUE;
		double minInterv = Double.MAX_VALUE;
		double maxInterv = Double.MIN_VALUE;
		double minPitchProx = Double.MAX_VALUE;
		double maxPitchProx = Double.MIN_VALUE;
		double minIOProx = Double.MAX_VALUE;
		double maxIOProx = Double.MIN_VALUE;
		double minOOProx = Double.MAX_VALUE;
		double maxOOProx = Double.MIN_VALUE;
		double minMov = Double.MAX_VALUE;
		double maxMov = Double.MIN_VALUE;
		double minCC = Double.MAX_VALUE;
		double maxCC = Double.MIN_VALUE;
		
		double minNumVCPairs = Double.MAX_VALUE;
		double maxNumVCPairs = Double.MIN_VALUE;
		
		double minSummedDistVCPairs = Double.MAX_VALUE;
		double maxSummedDistVCPairs = Double.MIN_VALUE;
		
		double minAvgDistVCPairs = Double.MAX_VALUE;
		double maxAvgDistVCPairs = Double.MIN_VALUE;
		
		
		numVV = 3;
		FeatureGenerator fege = new FeatureGenerator();
		Double[] mins = new Double[41];
		Arrays.fill(mins, Double.MAX_VALUE);
		Double[] maxs = new Double[41];
		Arrays.fill(maxs, Double.MIN_VALUE);
		
		for (String s : threeVV) {
			System.out.println(s);
//			File m = new File(ExperimentRunner.pathPrefix + ExperimentRunner.bachMidiPath + "/" + numVV + "vv/" + s);
			File t = new File(Runner.encodingsPath + "/" + numVV + "vv/" + s + ".txt");
			File m = new File(Runner.midiPath +  "/" + numVV + "vv/" + s);
			Tablature tab = new Tablature(t, true);
			Transcription tr = new Transcription(m, t);
			Integer[][] btp = tab.getBasicTabSymbolProperties();
			Integer[][] bnp = tr.getBasicNoteProperties();
			List<Integer[]> meterInfo = tab.getMeterInfo();
			List<List<Note>> chords = tr.getTranscriptionChords();
			List<List<Double>> durLabels = tr.getDurationLabels();
			List<Integer[]> voicesCoDNotes = tr.getVoicesCoDNotes();
					
			for (int i = 0; i < btp.length; i++) {
				Note currentNote = tab.convertTabSymbolToNote(i);
				List<Double> fv = null; 
				//fege.generateNoteFeatureVector(btp, durLabels, voicesCoDNotes, null, tr, currentNote, tr.getVoiceLabels(), meterInfo, i, false, false);
				for (int j = 0; j < fv.size(); j++) {
					double f = fv.get(j);
					if (f != -1) {
						if (f < mins[j]) {
							mins[j] = f;
						}
						if (f > maxs[j]) {
							maxs[j] = f;
						}
					}
				}
			}
					
//			for (int i = 0; i < bnp.length; i++) {
//				Note currentNote = tr.getNoteSequence().get(i);
//				List<Double> fv = fege.generateNoteFeatureVector(null, null, null, bnp, tr, currentNote, tr.getVoiceLabels(),
//					meterInfo, i, false, false);
//				
//				double currPitch = fv.get(0);
//				if (currPitch < minPitch) {
//					minPitch = currPitch;
//				}
//				if (currPitch > maxPitch) {
//					maxPitch = currPitch;
//				}
//				double currDur = fv.get(1);
//				if (currDur < minDur) {
//					minDur = currDur;
//				}
//				if (currDur > maxDur) {
//					maxDur = currDur;
//				}
//				double currMetPos = fv.get(3);
//				if (currMetPos < minMetPos) {
//					minMetPos = currMetPos;
//				}
//				if (currMetPos > maxMetPos) {
//					maxMetPos = currMetPos;
//				}
//				//
//				double currDistBelow = fv.get(6);
//				if (currDistBelow != -1) {
//					if (currDistBelow < minDistBelow) {
//						minDistBelow = currDistBelow;
//					}
//					if (currDistBelow > maxDistBelow) {
//						maxDistBelow = currDistBelow;
//					}
//				}
//				double currDistAbove = fv.get(7);
//				if (currDistAbove != -1) {
//					if (currDistAbove < minDistAbove) {
//						minDistAbove = currDistAbove;
//					}
//					if (currDistAbove > maxDistAbove) {
//						maxDistAbove = currDistAbove;
//					}
//				}
//				List<Double> interv = fv.subList(8, 12);
//				for (Double d : interv) {
//					if (d != -1) {
//						if (d < minInterv) {
//							minInterv = d;
//						}
//						if (d > maxInterv) {
//							maxInterv = d;
//						}
//					}
//				}
//				//
//				List<Double> pitchProx = fv.subList(12, 17);
//				for (Double d : pitchProx) {
//					if (d != -1) {	
//						double pp = (1/d) - 1;
//						if (pp < minPitchProx) {
//							minPitchProx = pp;
//						}
//						if (pp > maxPitchProx) {
//							maxPitchProx = pp;
//						}
//					}
//				}
//				List<Double> ioProx = fv.subList(17, 22);
//				for (Double d : ioProx) {
//					if (d != -1) {
//						double io = (1/d) - 1;
//						if (io < minIOProx) {
//							minIOProx = io;
//						}
//						if (io > maxIOProx) {
//							maxIOProx = io;
//						}
//					}
//				}
//				List<Double> ooProx = fv.subList(22, 27);
//				for (Double d : ooProx) {
//					if (d != -1) {
//						if (d > 0) { 
//							double oo = (1/d) - 1; 
//							if (oo < minOOProx) {
//								minOOProx = oo;
//							}
//							if (oo > maxOOProx) {
//								maxOOProx = oo;
//							}
//						}
//						if (d < 0) { 
//							double oo = -((1/d) + 1);
//							if (oo < minOOProx) {
//								minOOProx = oo;
//							}
//							if (oo > maxOOProx) {
//								maxOOProx = oo;
//							}
//						}		
//					}
//				}
//			}
			
			// C2C
//			for (Integer[] i : bnp) {
//				int currPitch = i[Transcription.PITCH];
//				if (currPitch < minPitch) {
//					minPitch = currPitch;
//				}
//				if (currPitch > maxPitch) {
//					maxPitch = currPitch;
//				}
//				
//				Rational currDur = new Rational(i[Transcription.DURATION_NUMER], i[Transcription.DURATION_DENOM]);
////				if (currDur.isLess(minDur)) {
////					minDur = currDur;
////				}
////				if (currDur.isGreater(maxDur)) {
////					maxDur = currDur;
////				}
//			}
//			int lni = 0;
//			for (List<Note> l : chords) {
//				Rational metricTime = new Rational(bnp[lni][Transcription.ONSET_TIME_NUMER], bnp[lni][Transcription.ONSET_TIME_DENOM]);	
//				Rational[] metricPosition = Tablature.getMetricPosition(metricTime, meterInfo);
//				Rational metPos = new Rational(metricPosition[1].getNumer(), metricPosition[1].getDenom());
////				if (metPos.isLess(minMetPos)) {
////					minMetPos = metPos;
////				}
////				if (metPos.isGreater(maxMetPos)) {
////					maxMetPos = metPos;
////				}
//			
//				double[] interv = fg.getIntervalsInChord(null, null, bnp, lni);
//				for (double d : interv) {
//					if (d != -1) {
//						if (d < minInterv) {
//							minInterv = d;
//						}
//						if (d > maxInterv) {
//							maxInterv = d;
//						}
//					}
//				}
//				
//				List<List<Integer>> allMappings = fgch.enumerateVoiceAssignmentPossibilitiesForChord(null, bnp,
//					tr.getVoiceLabels(), lni, numVV);
//				for (List<Integer> map : allMappings) {
//					double[] prox = fgch.getProximitiesAndMovementsOfChord(null, bnp, tr, lni, map);
////					for (int i = 0; i < 5; i++) {
////						if (prox[i] != -1.0) {
////							if (prox[i] < minPitchProx) {
////								minPitchProx = prox[i];
////							}
////							if (prox[i] > maxPitchProx) {
////								maxPitchProx = prox[i];
////							}
////						}
////					}
////					for (int i = 5; i < 10; i++) {
////						if (prox[i] != -1.0) {
////							if (prox[i] < minIOProx) {
////								minIOProx = prox[i];
////							}
////							if (prox[i] > maxIOProx) {
////								maxIOProx = prox[i];
////							}
////						}
////					}
////					for (int i = 10; i < 15; i++) {
////						if (prox[i] != -1.0) {
////							if (prox[i] < minOOProx) {
////								minOOProx = prox[i];
////							}
////							if (prox[i] > maxOOProx) {
////								maxOOProx = prox[i];
////							}
////						}
////					}
//					for (int i = 15; i < 20; i++) {
//						if (prox[i] < minMov) {
//							minMov = prox[i];
//						}
//						if (prox[i] > maxMov) {
//							maxMov = prox[i];
//						}
//					}
//					
//					List<Double> fv = fgch.generateChordFeatureVector(null, bnp, tr, meterInfo, lni, map);
//					double cc = fv.get(52);
//					if (cc < minCC) {
//						minCC = cc;
//					}
//					if (cc > maxCC) {
//						maxCC = cc;
//					}
//					double numVCPairs = fv.get(53);
//					if (numVCPairs < minNumVCPairs) {
//						minNumVCPairs = numVCPairs;
//					}
//					if (numVCPairs > maxNumVCPairs) {
//						maxNumVCPairs = numVCPairs;
//					}
//					double summedDistVCPairs = fv.get(54);
//					if (summedDistVCPairs < minSummedDistVCPairs) {
//						minSummedDistVCPairs = summedDistVCPairs;
//					}
//					if (summedDistVCPairs > maxSummedDistVCPairs) {
//						maxSummedDistVCPairs = summedDistVCPairs;
//					}
//					double avgDistVCPairs = fv.get(55);
//					if (avgDistVCPairs < minAvgDistVCPairs) {
//						minAvgDistVCPairs = avgDistVCPairs;
//					}
//					if (avgDistVCPairs > maxAvgDistVCPairs) {
//						maxAvgDistVCPairs = avgDistVCPairs;
//					}
//				}
//				 
//				
//				lni += l.size();
//			}
		}
		
		System.out.println("pitch:           " + mins[0] + " - " + maxs[0]);
		System.out.println("course:          " + mins[1] + " - " + maxs[1]);
		System.out.println("fret:            " + mins[2] + " - " + maxs[2]);
		System.out.println("minDur:          " + mins[3] + " - " + maxs[3]);
		System.out.println("maxDur:          " + mins[4] + " - " + maxs[4]);
		System.out.println("isOrn:           " + mins[5] + " - " + maxs[5]);
		System.out.println("metPos:          " + mins[6] + " - " + maxs[6]);
		System.out.println("numNewNext:      " + mins[7] + " - " + maxs[7]);
		
		System.out.println("chordSize:       " + mins[8] + " - " + maxs[8]);
		System.out.println("indexInChord:    " + mins[9] + " - " + maxs[9]);
		System.out.println("distBelow:       " + mins[10] + " - " + maxs[10]);
		System.out.println("distAbove:       " + mins[11] + " - " + maxs[11]);
		List<Double> intervMins = new ArrayList<Double>();
		List<Double> intervMaxs = new ArrayList<Double>();
		for (int i = 12; i < 16; i++) {
			intervMins.add(mins[i]);
			intervMaxs.add(maxs[i]);
		}
		System.out.println("intervals:       " + Collections.min(intervMins) + " - " + Collections.max(intervMaxs));
		List<Double> ppMins = new ArrayList<Double>();
		List<Double> ppMaxs = new ArrayList<Double>();
		for (int i = 21; i < 26; i++) {
			ppMins.add(mins[i]);
			ppMaxs.add(maxs[i]);
		}
		System.out.println("pitchProx:       " + Collections.min(ppMins) + " - " + Collections.max(ppMaxs));
		List<Double> ioMins = new ArrayList<Double>();
		List<Double> ioMaxs = new ArrayList<Double>();
		for (int i = 26; i < 31; i++) {
			ioMins.add(mins[i]);
			ioMaxs.add(maxs[i]);
		}
		System.out.println("ioProx:          " + Collections.min(ioMins) + " - " + Collections.max(ioMaxs));
		List<Double> ooMins = new ArrayList<Double>();
		List<Double> ooMaxs = new ArrayList<Double>();
		for (int i = 31; i < 36; i++) {
			ooMins.add(mins[i]);
			ooMaxs.add(maxs[i]);
		}
		System.out.println("ooProx:          " + Collections.min(ooMins) + " - " + Collections.max(ooMaxs));
		System.exit(0);
		
		System.out.println("pitch:           " + minPitch + " - " + maxPitch);
		System.out.println("duration:        " + minDur + " - " + maxDur);
		System.out.println("metric position: " + minMetPos + " - " + maxMetPos);
		System.out.println("distBelow:       " + minDistBelow + " - " + maxDistBelow);
		System.out.println("distAbove:       " + minDistAbove + " - " + maxDistAbove);
		System.out.println("intervals:       " + minInterv + " - " + maxInterv);
		System.out.println("pitchProx:       " + minPitchProx + " - " + maxPitchProx);
		System.out.println("interOnsetProx:  " + minIOProx + " - " + maxIOProx);
		System.out.println("offsetOnsetProx: " + minOOProx + " - " + maxOOProx);
		System.exit(0);
		
		System.out.println("pitch:           " + minPitch + " - " + maxPitch);
		System.out.println("duration:        " + minDur + " - " + maxDur);
		System.out.println("metric position: " + minMetPos + " - " + maxMetPos);
		System.out.println("intervals:       " + minInterv + " - " + maxInterv);
		System.out.println("pitchProx:       " + minPitchProx + " - " + maxPitchProx);
		System.out.println("interOnsetProx:  " + minIOProx + " - " + maxIOProx);
		System.out.println("offsetOnsetProx: " + minOOProx + " - " + maxOOProx);
		System.out.println("movements:       " + minMov + " - " + maxMov);
		System.out.println("corr. coeff.:    " + minCC + " - " + maxCC);
		System.out.println("num vcp:         " + minNumVCPairs + " - " + maxNumVCPairs);
		System.out.println("summed dist vcp: " + minSummedDistVCPairs + " - " + maxSummedDistVCPairs);
		System.out.println("avg dist vcp:    " + minAvgDistVCPairs + " - " + maxAvgDistVCPairs);
		System.out.println(ToolBox.getTimeStamp());
		System.exit(0);
		
		
		for (String s : threeVV) {
			File m = new File("F/PhD/data" + "MIDI/bach/WTC/" + "/3vv/" + s);
//			File m = new File(ExperimentRunner.pathPrefix + ExperimentRunner.tabMidiPath + "4vv/" + s);
			Transcription tr = new Transcription(m, null);
			Integer[][] bnp = tr.getBasicNoteProperties();
			
			int sizeLastChord = bnp[bnp.length - 1][Transcription.CHORD_SIZE_AS_NUM_ONSETS];
			int lowestNoteIndexLastChord = bnp.length - sizeLastChord;
//			FeatureGenerator fg = new FeatureGenerator();
			List<Integer> pitchesInChord = Transcription.getPitchesInChord(bnp, lowestNoteIndexLastChord);
				
			List<List<List<Double>>> chordVoiceLabels = tr.getChordVoiceLabels();
			int indexLastChord = bnp[bnp.length - 1][Transcription.CHORD_SEQ_NUM];
			List<List<Double>> currentChordVoiceLabels = chordVoiceLabels.get(indexLastChord);
//			DataConverter dc = new DataConverterTab();
			List<List<Integer>> voicesInChord = DataConverter.getVoicesInChord(currentChordVoiceLabels);
			FeatureGeneratorChord fgc = new FeatureGeneratorChord();
  	  List<List<Integer>> pAndV = Transcription.getAllPitchesAndVoicesInChord(bnp, pitchesInChord, voicesInChord, 
  	  	tr.getVoiceLabels(), lowestNoteIndexLastChord);
  	  List<Integer> currentPitchesInChord = pAndV.get(0);
  	  // currentVoicesInChord must be a List<List>>
			List<List<Integer>> currentVoicesInChord = new ArrayList<List<Integer>>();
			for (int j : pAndV.get(1)) {
				int currentVoice = j;
				List<Integer> voiceWrapped = Arrays.asList(new Integer[]{currentVoice});
				currentVoicesInChord.add(voiceWrapped);
			}
  	  List<List<Integer>> voiceCrossingInfo = 
  		Transcription.getVoiceCrossingInformationInChord(currentPitchesInChord, currentVoicesInChord);
  	  System.out.println(s);
  	  System.out.println("  " + voiceCrossingInfo.get(1));
				
//			int numSus = fg.getIndicesOfSustainedPreviousNotes(null, null, bnp, bnp.length - 1).size();
//			if (sizeLastChord != 4) {
//				System.out.println(s);
//				System.out.println("  sizeLastChord = " + sizeLastChord);
//				System.out.println("  numSus = " + numSus);
//				
//			}
		}
		System.exit(0);
		
		
    File midf = new File("F/PhD/data" + "MIDI/Bach/WTC/3vv/Bach - WTC1, Fuga 21 in Bb major (BWV 866)");
    Transcription trns = new Transcription(midf, null);
    System.out.println(trns.getVoiceLabels().get(37));
    System.out.println(trns.getVoiceLabels().get(38));
    System.out.println(trns.getVoiceLabels().get(39));
    System.out.println(trns.getVoiceLabels().get(40));
    
    System.exit(0);
		
    List<String> three = null; //Dataset.getTabThreeVoices();
    String s = three.get(1);
    
		File tf = new File("F/PhD/data" + "Encodings/Dataset/3vv/" + s + ".txt");
		File mf = new File("F/PhD/data" + "MIDI/Tablature/3vv/" + s);
			
		Tablature tab = new Tablature(tf, true);
		Transcription trans = new Transcription(mf, tf);

		System.out.println(trans.getPieceName());
		
		// CoDs
		System.out.println("... counting CoDs ...");
		int counter = 0;
		for (int i = 0; i < trans.getTranscriptionChords().size(); i++) {
			Integer[][] coDInfo = trans.getCoDInfo(tab.getTablatureChords(), i);
			if (coDInfo != null) {
			  counter++;
				System.out.println("i = " + i + "; CoDInfo:");
				System.out.println("  " + Arrays.toString(coDInfo[0]));
				if (coDInfo.length > 1) {
					for (int j = 1; j < coDInfo.length; j++) {
				    System.out.println("  " + Arrays.toString(coDInfo[j]));
					}
				}
			}
		}
		System.out.println(s + " contains " + counter + " chords with a CoD");
		counter = 0;
		
		// Unisons
		System.out.println("... counting unisons ...");
		for (int i = 0; i < tab.getTablatureChords().size(); i++) {
			Integer[][] unisonInfo = tab.getUnisonInfo(i);
			if (unisonInfo != null) {
				counter++;
				System.out.println("i = " + i + "; unisonInfo:");
				System.out.println("  " + Arrays.toString(unisonInfo[0]));
				if (unisonInfo.length > 1) {
					for (int j = 1; j < unisonInfo.length; j++) {
				    System.out.println("  " + Arrays.toString(unisonInfo[j]));
					}
				}
			}
		}
		System.out.println(s + " contains " + counter + " chords with a unison");
		counter = 0;
		
		// Course crossings
		System.out.println("... counting course crossings ...");
		for (int i = 0; i < tab.getTablatureChords().size(); i++) {
			Integer[][] courseCrossingInfo = tab.getCourseCrossingInfo(i);
			if (courseCrossingInfo != null) {
				counter++;
				System.out.println("i = " + i + "; unisonInfo:");
				System.out.println("  " + Arrays.toString(courseCrossingInfo[0]));
				if (courseCrossingInfo.length > 1) {
					for (int j = 1; j < courseCrossingInfo.length; j++) {
				    System.out.println("  " + Arrays.toString(courseCrossingInfo[j]));
					}
				}
			}
		}
		System.out.println(s + " contains " + counter + " chords with a course crossing");
		counter = 0;
		
		// Voice crossing pairs
		trans = new Transcription(mf, tf);
//		System.out.println(trans.getVoiceCrossingInformation(tab));
//		for (int i = 0; i < trans.getTranscriptionChords().size(); i++) {
//		  System.out.println(trans.chordContainsVoiceCrossing(i));
//		}
		
		System.exit(0);
		
//		Tablature tab = new Tablature(tf, true);
//		Transcription trans = new Transcription(mf, tf);
//	  Transcription trans = new Transcription(mf, null);
				
		FeatureGeneratorChord fgc = new FeatureGeneratorChord();
		Integer[][] btp = tab.getBasicTabSymbolProperties();
//		Integer[][] btp = null;
		Integer[][] bnp = trans.getBasicNoteProperties();
		System.out.println(btp);
		System.out.println(bnp);
		List<List<Double>> vl = trans.getVoiceLabels();
				
		List<List<Integer>> all = fgc.enumerateVoiceAssignmentPossibilitiesForChord(btp, bnp, vl, 26, 3);
	  for (List<Integer> l : all) {
      System.out.println(l);
    }
    System.exit(0);
		
		
		
//		System.out.println(va.get(0));
//		List<Double> fv = fgc.generateChordFeatureVector(btp, bnp, trans, tab.getMeterInfo(), 
//			6, va.get(0).get(0));
//		System.out.println(fv.size());
//		System.out.println(fv);
//		System.out.println(fg.generateChordFeatureVector(btp, bnp, trans, tab.getMeterInfo(), 
//			0, va.get(0).get(1)));
//		System.out.println(fg.generateChordFeatureVector(btp, bnp, trans, tab.getMeterInfo(), 
//			0, va.get(0).get(2)));
//		System.out.println(fg.generateChordFeatureVector(btp, bnp, trans, tab.getMeterInfo(), 
//			0, va.get(0).get(3)));
//		System.out.println(fg.generateChordFeatureVector(btp, bnp, trans, tab.getMeterInfo(), 
//			0, va.get(0).get(4)));
		
		
		System.exit(0);
		
		
//		List<Rational> errors = new ArrayList<Rational>();
//		errors.add(new Rational(89, 478));
//		errors.add(new Rational(91, 777));
//		errors.add(new Rational(61, 705));
//		errors.add(new Rational(46, 371));
//		errors.add(new Rational(33, 457));
//		errors.add(new Rational(46, 708));
//		errors.add(new Rational(289, 2238));
//		errors.add(new Rational(231, 1974));
//		errors.add(new Rational(139, 1184));
//		
//		Rational sum = Rational.ZERO;
//		Rational mu = new Rational(1025, 8892);
//		for (Rational e : errors) {
//			Rational r = e.sub(mu);
//			Rational rSquared = r.mul(r);
//			sum = sum.add(rSquared);
//		}
//		Rational sumDivided = sum.div(errors.size());
//		double root = Math.sqrt(sumDivided.toDouble());
//		System.out.println(root);
//		System.out.println("OF");  
//		
//		System.out.println(AuxiliaryTool.calculateStandardDeviation(errors, mu));
//		
//    System.exit(0);
//		
//
//		
//		
//		
//		
//			
//		File enc = new File(ExperimentRunner.pathPrefix + ExperimentRunner.encodingsPath + 
//			"/3vv/Newsidler 1536 - Mess pensees.txt");
//		File mid = new File(ExperimentRunner.pathPrefix + ExperimentRunner.tabMidiPath + 
//			"/3vv/Newsidler 1536 - Mess pensees");
//		Tablature mesp = new Tablature(enc);
//		Transcription mespTr = new Transcription(mid, null);
//		List<List<TabSymbol>> tabCh = mesp.getTablatureChords();
//		System.out.println("num chords = " + tabCh.size());
//		int numCoDs = 0;
//		List<Integer> codIndices = new ArrayList<Integer>();
//		for (int i = 0; i < tabCh.size(); i++) {
//		  if (mespTr.getCoDInfo(tabCh, i) != null) {
//		  	codIndices.add(i);
//		  	numCoDs++;
//		  }
//		}
//		System.out.println(codIndices);
//		System.out.println(numCoDs);
//		System.exit(0);
//		
//		
////		String pieceN = "Ochsenkun 1558 - Herr Gott lass dich erbarmen";
////		String pieceN = "Phalese 1547 - Tant que uiuray [a4]";
//		String pieceN = "Phalese 1547 - Tant que uiuray [a3]";
//  	String tabN = "F:/PhD/Data/Encodings/Dataset/" + pieceN + ".txt";
//  	String transN = "F:/PhD/Data/MIDI/Dataset/" + pieceN;
////  	String transN = "F:/PhD/Data/MIDI/Dataset/Newsidler 1536 - Mess pensees";
//
//  	Tablature tab = new Tablature(new File(tabN));
//
//		Transcription tra = new Transcription(new File(transN), new File(tabN));
//		
//		System.out.println(tra.getPiece().getScore().size());
//		System.out.println(tra.getNumberOfVoices());
////		System.out.println(tra.getBasicNoteProperties().length);
//		System.out.println(tra.getPiece().getScore().get(0).get(0).size());
//		System.out.println(tra.getPiece().getScore().get(1).get(0).size());
//		System.out.println(tra.getPiece().getScore().get(2).get(0).size());
//		System.out.println(tra.getPiece().getScore().get(3).get(0).size());
//		System.out.println(tra.getPiece().getScore().get(4).get(0).size());
//		
//		
//		System.exit(0);
//		
//		
//		List<List<TabSymbol>> tc = tab.getTablatureChords();
//		int total = 0;
//		for (List<TabSymbol> l : tc) {
//			total += l.size();
//		}
//		System.out.println(total);
//		System.out.println(tra.getNoteSequence().size());
////		System.exit(0);
//		
//    for (int i = 0; i < tra.getTranscriptionChords().size(); i++) {
////    	System.out.println(i + " " + tra.getCoDInfo(tab.getTablatureChords(), i));
//		  if (tra.getCoDInfo(tab.getTablatureChords(), i) != null) {
//		  	System.out.println(i);
////		  	System.out.println(Arrays.toString(tra.getCoDInfo(tab.getTablatureChords(), i)[0]));
//		  }
//    }
    

		
//		RenderingHints test = new RenderingHints();
////	  test.registerHint("stem direction", "up");
//  	test.registerHint("hidden", true);
//  	Piece pie = new Piece();
//    NotationSystem notSys = pie.createNotationSystem();
//    NotationStaff notStaff= new NotationStaff(notSys);
//    NotationVoice notVoice = new NotationVoice(notStaff);
//    List<Note> notes = new ArrayList<Note>();
//    notes.add(Transcription.createNote(72, new Rational(0, 8), new Rational(1, 16)));
//    notes.add(Transcription.createNote(72, new Rational(1, 8), new Rational(1, 16)));
//    notes.add(Transcription.createNote(72, new Rational(2, 8), new Rational(1, 16)));
//    notes.add(Transcription.createNote(72, new Rational(3, 8), new Rational(1, 16)));
//    for (int i = 0; i < notes.size(); i++) {
//    	Note n = notes.get(i);
//    	if (i == 0 || i == 2) {
//  //  		n.getScoreNote().setDiatonic('r');
//  //  		n.getScoreNote().setOctave((byte) 2);
//  //  		n.setRenderingHints(test);
//    		
//    	}
//    	notVoice.add(n);
//    }
//    for (int i = 0; i < notVoice.size(); i++) {
//    	NotationChord nc = notVoice.get(i); 	
//    	if (i == 1) {
//    		nc.setBeamType((byte) 1);
//    	}
//    }
//    
//    notSys.createBeams();
//      
//    Transcription toPrintPie = new Transcription(pie, null, null);
//  	toPrintPie.visualise("TEST");
		
		
		
    Piece argPiece = new Piece();
    NotationSystem argNotationSystem = argPiece.createNotationSystem();
    RenderingHints rhDown = new RenderingHints();
  	rhDown.registerHint("stem direction", "down");
  	RenderingHints rhUp = new RenderingHints();
  	rhUp.registerHint("stem direction", "up");
    
  	NotationStaff staff0 = new NotationStaff(argNotationSystem);
    NotationVoice voice0 = new NotationVoice(staff0);  
    voice0.add(Transcription.createNote(72, new Rational(0, 4), new Rational(3, 8)));
//    voice0.add(Transcription.createNote(72, new Rational(0, 4), new Rational(3, 16)));
//    Note r00 = Transcription.createNote(72, new Rational(3, 16), new Rational(1, 16));
//    r00.getScoreNote().setDiatonic('r');
//    voice0.add(r00);
//    Note r01 = Transcription.createNote(72, new Rational(1, 4), new Rational(1, 8));
//    r01.getScoreNote().setDiatonic('r');
//    voice0.add(r01);
    voice0.add(Transcription.createNote(74, new Rational(3, 8), new Rational(1, 8)));
    voice0.add(Transcription.createNote(72, new Rational(1, 2), new Rational(1, 4)));
    voice0.add(Transcription.createNote(70, new Rational(3, 4), new Rational(1, 16)));
    voice0.add(Transcription.createNote(64, new Rational(13, 16), new Rational(1, 16)));
    voice0.add(Transcription.createNote(65, new Rational(7, 8), new Rational(1, 16)));
    voice0.add(Transcription.createNote(67, new Rational(15, 16), new Rational(1, 16)));
    voice0.add(Transcription.createNote(69, new Rational(1, 1), new Rational(1, 8)));
    voice0.add(Transcription.createNote(67, new Rational(9, 8), new Rational(1, 8)));
    voice0.add(Transcription.createNote(69, new Rational(5, 4), new Rational(1, 8)));
    voice0.add(Transcription.createNote(70, new Rational(11, 8), new Rational(1, 8)));
    voice0.add(Transcription.createNote(69, new Rational(6, 4), new Rational(1, 2)));
    voice0.add(Transcription.createNote(67, new Rational(2, 1), new Rational(1, 4)));
    Note r02 = Transcription.createNote(67, new Rational(9, 4), new Rational(3, 4));
    r02.getScoreNote().setDiatonic('r');
    voice0.add(r02);
    
//    voice0.add(Transcription.createNote(64, new Rational(0, 4), new Rational(1, 2)));
//    voice0.add(Transcription.createNote(71, new Rational(5, 4), new Rational(1, 4)));
//    voice0.add(Transcription.createNote(71, new Rational(6, 4), new Rational(1, 4)));
//    voice0.add(Transcription.createNote(71, new Rational(7, 4), new Rational(1, 4)));
//    voice0.add(Transcription.createNote(69, new Rational(8, 4), new Rational(1, 8)));
//    voice0.add(Transcription.createNote(67, new Rational(17, 8), new Rational(1, 8)));
//    voice0.add(Transcription.createNote(69, new Rational(9, 4), new Rational(1, 8)));
//    voice0.add(Transcription.createNote(71, new Rational(19, 8), new Rational(1, 8)));
//    voice0.add(Transcription.createNote(72, new Rational(10, 4), new Rational(1, 4)));
//    voice0.add(Transcription.createNote(72, new Rational(11, 4), new Rational(1, 4)));
//    voice0.add(Transcription.createNote(71, new Rational(12, 4), new Rational(1, 8)));
//    voice0.add(Transcription.createNote(69, new Rational(13, 4), new Rational(1, 4)));
//    Note r00 = Transcription.createNote(69, new Rational(14, 4), new Rational(1, 2));
//    r00.getScoreNote().setDiatonic('r');
//    voice0.add(r00);
    
    NotationStaff staff1 = new NotationStaff(argNotationSystem);
    NotationVoice voice1 = new NotationVoice(staff1);
    voice1.add(Transcription.createNote(65, new Rational(0, 4), new Rational(1, 2)));
    voice1.add(Transcription.createNote(65, new Rational(1, 2), new Rational(1, 4)));
    voice1.add(Transcription.createNote(62, new Rational(3, 4), new Rational(1, 4)));
    voice1.add(Transcription.createNote(65, new Rational(1, 1), new Rational(1, 2)));
    voice1.add(Transcription.createNote(65, new Rational(6, 4), new Rational(1, 2)));
    Note r10 = Transcription.createNote(67, new Rational(2, 1), new Rational(1, 4));
//    r10.getScoreNote().setDiatonic('r');
    r10.setRenderingHints(rhUp);
    voice1.add(r10);
    voice1.add(Transcription.createNote(65, new Rational(9, 4), new Rational(3, 4)));
     
//    voice1.add(Transcription.createNote(60, new Rational(0, 4), new Rational(1, 2)));
//    voice1.add(Transcription.createNote(64, new Rational(2, 4), new Rational(1, 4)));
//    voice1.add(Transcription.createNote(67, new Rational(4, 4), new Rational(1, 2)));
//    voice1.add(Transcription.createNote(67, new Rational(6, 4), new Rational(1, 4)));
//    voice1.add(Transcription.createNote(67, new Rational(7, 4), new Rational(1, 8)));
//    voice1.add(Transcription.createNote(64, new Rational(15, 8), new Rational(1, 8)));
//    voice1.add(Transcription.createNote(65, new Rational(8, 4), new Rational(1, 2)));
//    voice1.add(Transcription.createNote(65, new Rational(10, 4), new Rational(1, 4)));
//    voice1.add(Transcription.createNote(67, new Rational(11, 4), new Rational(1, 4)));
//    voice1.add(Transcription.createNote(65, new Rational(25, 8), new Rational(1, 16)));
//    voice1.add(Transcription.createNote(64, new Rational(51, 16), new Rational(1, 16)));
//    voice1.add(Transcription.createNote(65, new Rational(13, 4), new Rational(1, 8)));
//    voice1.add(Transcription.createNote(60, new Rational(27, 8), new Rational(1, 8)));
//    Note r10 = Transcription.createNote(60, new Rational(14, 4), new Rational(1, 2));
//    r10.getScoreNote().setDiatonic('r');
//    voice1.add(r10);
    
    NotationStaff staff2 = new NotationStaff(argNotationSystem);
    NotationVoice voice2 = new NotationVoice(staff2);
    voice2.add(Transcription.createNote(57, new Rational(0, 4), new Rational(1, 2)));
    voice2.add(Transcription.createNote(57, new Rational(1, 2), new Rational(1, 4)));
    voice2.add(Transcription.createNote(58, new Rational(3, 4), new Rational(1, 4)));
//    voice2.add(Transcription.createNote(58, new Rational(3, 4), new Rational(1, 8)));
    voice2.add(Transcription.createNote(60, new Rational(1, 1), new Rational(1, 2)));
    voice2.add(Transcription.createNote(60, new Rational(6, 4), new Rational(3, 8)));
//    voice2.add(Transcription.createNote(60, new Rational(6, 4), new Rational(1, 4)));
    voice2.add(Transcription.createNote(62, new Rational(15, 8), new Rational(1, 8)));
    voice2.add(Transcription.createNote(64, new Rational(2, 1), new Rational(1, 4)));  
    Note r20 = Transcription.createNote(64, new Rational(9, 4), new Rational(3, 4));
    r20.getScoreNote().setDiatonic('r');
    voice2.add(r20);
    
//    voice2.add(Transcription.createNote(55, new Rational(0, 4), new Rational(1, 4)));
//    voice2.add(Transcription.createNote(55, new Rational(2, 4), new Rational(1, 4)));
//    voice2.add(Transcription.createNote(64, new Rational(3, 4), new Rational(1, 4)));
//    voice2.add(Transcription.createNote(64, new Rational(4, 4), new Rational(1, 4)));
//    voice2.add(Transcription.createNote(64, new Rational(5, 4), new Rational(1, 4)));
//    voice2.add(Transcription.createNote(62, new Rational(6, 4), new Rational(1, 8)));
//    voice2.add(Transcription.createNote(61, new Rational(13, 8), new Rational(1, 8)));
//    voice2.add(Transcription.createNote(62, new Rational(7, 4), new Rational(1, 4)));
//    voice2.add(Transcription.createNote(60, new Rational(8, 4), new Rational(1, 2)));
//    voice2.add(Transcription.createNote(60, new Rational(10, 4), new Rational(1, 4)));
//    voice2.add(Transcription.createNote(64, new Rational(11, 4), new Rational(1, 4)));
//    voice2.add(Transcription.createNote(62, new Rational(12, 4), new Rational(1, 4)));
//    Note r20 = Transcription.createNote(62, new Rational(13, 4), new Rational(1, 4));
//    r20.getScoreNote().setDiatonic('r');
//    voice2.add(r20);
//    voice2.add(Transcription.createNote(62, new Rational(14, 4), new Rational(1, 2)));
    
    NotationStaff staff3 = new NotationStaff(argNotationSystem);
    NotationVoice voice3 = new NotationVoice(staff3);
		voice3.add(Transcription.createNote(53, new Rational(0, 4), new Rational(1, 2)));
		voice3.add(Transcription.createNote(53, new Rational(1, 2), new Rational(1, 4)));
		voice3.add(Transcription.createNote(55, new Rational(3, 4), new Rational(1, 4)));
		voice3.add(Transcription.createNote(53, new Rational(1, 1), new Rational(1, 2)));
		voice3.add(Transcription.createNote(53, new Rational(6, 4), new Rational(1, 8)));
		voice3.add(Transcription.createNote(55, new Rational(13, 8), new Rational(1, 8)));
		voice3.add(Transcription.createNote(57, new Rational(7, 4), new Rational(1, 8)));
		voice3.add(Transcription.createNote(59, new Rational(15, 8), new Rational(1, 8)));
		voice3.add(Transcription.createNote(60, new Rational(8, 4), new Rational(1, 4)));
		Note r30 = Transcription.createNote(60, new Rational(9, 4), new Rational(3, 4));
    r30.getScoreNote().setDiatonic('r');
    voice3.add(r30);
    
//    voice3.add(Transcription.createNote(48, new Rational(0, 4), new Rational(1, 2)));
//    voice3.add(Transcription.createNote(48, new Rational(2, 4), new Rational(1, 2)));
//    voice3.add(Transcription.createNote(48, new Rational(4, 4), new Rational(1, 8)));
//    voice3.add(Transcription.createNote(50, new Rational(9, 8), new Rational(1, 8)));
//    voice3.add(Transcription.createNote(52, new Rational(5, 4), new Rational(1, 8)));
//    voice3.add(Transcription.createNote(54, new Rational(11, 8), new Rational(1,8)));
//    voice3.add(Transcription.createNote(55, new Rational(6, 4), new Rational(1, 4)));
//    voice3.add(Transcription.createNote(55, new Rational(7, 4), new Rational(1, 4)));
//    voice3.add(Transcription.createNote(53, new Rational(8, 4), new Rational(1, 2)));
//    voice3.add(Transcription.createNote(53, new Rational(10, 4), new Rational(1, 4)));
//    voice3.add(Transcription.createNote(48, new Rational(11, 4), new Rational(1, 4)));
//    voice3.add(Transcription.createNote(55, new Rational(12, 4), new Rational(1, 4)));
//    voice3.add(Transcription.createNote(50, new Rational(13, 4), new Rational(1, 4)));
//    voice3.add(Transcription.createNote(50, new Rational(14, 4), new Rational(1, 2)));
    
    Transcription toPrint = new Transcription(argPiece, null, null);
//    toPrint.setPieceName("Pierre PHALÈSE, Tant que uiuray [a4]");
    toPrint.setPieceName("PHALESE_1547_Tant_que_uiuray_[a4].txt");
  	toPrint.visualise(new TimeSignature(), true, 4);
  	toPrint.visualise(new TimeSignature(), false, 4);
   
  	
  	
//  	String pieceName = "Rotta 1546 - Bramo morir per non patir piu morte";
  	String pieceName = "Ochsenkun 1558 - Herr Gott lass dich erbarmen";
  	String tabName = "F:/PhD/Data/Encodings/Dataset/" + pieceName + ".txt";
  	String transName = "F:/PhD/Data/MIDI/Dataset/" + pieceName;
    
  	
//		Tablature t = new Tablature(new File(tabName)); 
//		Transcription tr = new Transcription(new File(transName), new File(tabName));  
		Transcription tr = new Transcription(new File(transName), null); 
  	tr.setPieceName("HUERESIECH");
//		tr.visualise(false);
		
		boolean isTwoStaff = false;
		    
	  // TWO-STAFF
		Piece piece = new Piece(); 
		NotationSystem notationSystem = piece.createNotationSystem();
		
		RenderingHints rhUpper = new RenderingHints();
    rhUpper.registerHint("stem direction", "up");
    RenderingHints rhLower = new RenderingHints();
    rhLower.registerHint("stem direction", "down");
		
    // Upper staff
		NotationStaff upperStaff = new NotationStaff(notationSystem);
		NotationStaff lowerStaff = new NotationStaff(notationSystem);
		lowerStaff.setClefType('f', 1, 0);
		
		for (int j = SUPERIUS; j <= BASSUS; j++) {
			NotationStaff ns = null;
			if (j == SUPERIUS || j == ALTUS) {
				ns = notationSystem.get(0);
			}
			else {
				notationSystem.get(1).setClefType('f', 1, 0);
				ns = notationSystem.get(1);
			}
    	NotationVoice nv = new NotationVoice(ns);
    	NotationVoice argNV = argPiece.getScore().get(j).get(0);
    	for (NotationChord nc : argNV) {
    		Note n = nc.get(0);	  
//    		if (j == SUPERIUS || j == TENOR) {
//		      n.setRenderingHints(rhUpper);
//    		}
//    		else {
//		      n.setRenderingHints(rhLower);
//    		}
    		nv.add(n);
  		}
  	}
		        
    notationSystem.createBeams();
    NotationStaffConnector nsc = new NotationStaffConnector(CType.BRACKET);
    nsc.add(notationSystem.get(0));
    nsc.add(notationSystem.get(notationSystem.size() - 1));
    notationSystem.addStaffConnector(nsc);
    toPrint = new Transcription(piece, null, null);
    toPrint.setPieceName("Phalèse 1547, Tant que uiuray [a4], bwd_dur");
//  	toPrint.visualise(false);
		
		
		// SCORE
		piece = new Piece(); 
		notationSystem = piece.createNotationSystem();
    
		for (int j = SUPERIUS; j <= BASSUS; j++) {
			NotationStaff ns = new NotationStaff(notationSystem);
    	if (j == TENOR || j == BASSUS) {
    		ns.setClefType('f', 1, 0);
    	}
			NotationVoice nv = new NotationVoice(ns);
    	NotationVoice argNV = argPiece.getScore().get(j).get(0);
    	for (NotationChord nc : argNV) {
    		Note n = nc.get(0);	  
    		nv.add(n);
  		}
  	}
		    
    notationSystem.createBeams();
    nsc = new NotationStaffConnector(CType.BRACKET);
    nsc.add(notationSystem.get(0));
    nsc.add(notationSystem.get(notationSystem.size() - 1));
    notationSystem.addStaffConnector(nsc);
	  toPrint = new Transcription(piece, null, null);
	  toPrint.setPieceName("Phalèse 1547, Tant que uiuray [a4], bwd_dur");
//		toPrint.visualise(true);
   
   
		
//		List<List<Double>> oei = AuxiliaryTool.getStoredObject(new ArrayList<List<Double>>(), 
//			new File("F:/PhD/Data/Files out/Neural network/Quick test/lambda = 3.0E-5/Hidden nodes = -4/Fold 1/" +
//		  "Predicted duration labels Ochsenkun 1558 - Herr Gott lass dich erbarmen.xml"));
//		System.out.println(oei.size());
//		
//		List<Integer> chordSizes = new Tablature(new File("F:/PhD/Data/Encodings/Dataset/Ochsenkun 1558 - Herr Gott lass dich erbarmen.txt")).getNumberOfNotesPerChord();
//		List<Integer> backwardsMapping = Transcription.getBackwardsMapping(chordSizes);
//		
//		for (int i = 0; i < oei.size(); i++) {
//			System.out.println("fwd index = " + i + "; bwd index = " + backwardsMapping.indexOf(i));
//			System.out.println("  " + oei.get(i));
//		}
//		System.exit(0);
		
		
		
//		Tablature taab = new Tablature(new File("F:/PhD/Data/Encodings/Dataset/Rotta 1546 - Bramo morir per non patir piu morte.txt")); 
//		Transcription traas = new Transcription(new File("F:/PhD/Data/MIDI/Dataset/Rotta 1546 - Bramo morir per non patir piu morte"), 
//			new File("F:/PhD/Data/Encodings/Dataset/Rotta 1546 - Bramo morir per non patir piu morte.txt"));
//		
//		traas.visualise("snsjn");
		
		
//		List<Integer[]> voicesCoDNotes = traas.getVoicesCoDNotes();
//		Integer[][] btp = taab.getBasicTabSymbolProperties();
//		for (Integer[] bttp : btp) {
//			System.out.println(bttp[Tablature.PITCH]);
//		}
//		System.exit(0);
////		List<List<Double>> voiceLabels = traas.getVoiceLabels();
////		List<List<Double>> durLabels = traas.getDurationLabels();
//		List<List<Double>> labels = new ArrayList<List<Double>>(traas.getVoiceLabels());
//		for (int i = 0; i < labels.size(); i++) {
//			List<Double> curr = labels.get(i); 
//			curr.addAll(traas.getDurationLabels().get(i));
//			labels.set(i, curr);
//		}
//		
//		
//		FeatureGenerator fege = new FeatureGenerator();
//		List<List<Double>> joo = fege.generateAllNoteFeatureVectors(btp, voicesCoDNotes, null, traas, labels, 
//			taab.getMeterInfo(), traas.getNumberOfNewNotesPerChord(), false, false, true);
//		for (List<Double> l : joo) {
//			System.out.println(l);
//		}
//		System.exit(0);
//		
//
//		List<Integer> bwm = Transcription.getBackwardsMapping(traas.getNumberOfNewNotesPerChord());
//		System.out.println("ROTTA");
//		for (int i = 0; i < voicesCoDNotes.size(); i++) {
//			if (voicesCoDNotes.get(i) != null) {
//				Rational mt = new Rational(btp[i][Tablature.ONSET_TIME], Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//				System.out.println("met pos = " + 
//				  AuxiliaryTool.getMetricPositionAsString(Tablature.getMetricPosition(mt, taab.getMeterInfo())));
//				System.out.println("noteIndex = " + i);
//				int bwdInd = -1;
//				for (int j = 0; j < bwm.size(); j++) {
//					if (bwm.get(j) == i) {
//						bwdInd = j;
//						break;
//					}
//				}
//				System.out.println("bwd index = " + bwdInd);
//				System.out.println(Arrays.toString(voicesCoDNotes.get(i)));
//			}
//		}
//		
//		taab = new Tablature(new File("F:/PhD/Data/Encodings/Dataset/Phalese 1547 - Tant que uiuray [a4].txt")); 
//		traas = new Transcription(new File("F:/PhD/Data/MIDI/Dataset/Phalese 1547 - Tant que uiuray [a4]"), 
//			new File("F:/PhD/Data/Encodings/Dataset/Phalese 1547 - Tant que uiuray [a4].txt"));
//		voicesCoDNotes = traas.getVoicesCoDNotes();
//		btp = taab.getBasicTabSymbolProperties();
//		bwm = Transcription.getBackwardsMapping(traas.getNumberOfNewNotesPerChord());
//		System.out.println("PHALESE");
//		for (int i = 0; i < voicesCoDNotes.size(); i++) {
//			if (voicesCoDNotes.get(i) != null) {
//				Rational mt = new Rational(btp[i][Tablature.ONSET_TIME], Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());
//				System.out.println("met pos = " + 
//				  AuxiliaryTool.getMetricPositionAsString(Tablature.getMetricPosition(mt, taab.getMeterInfo())));
//				System.out.println("noteIndex = " + i);
//				int bwdInd = -1;
//				for (int j = 0; j < bwm.size(); j++) {
//					if (bwm.get(j) == i) {
//						bwdInd = j;
//						break;
//					}
//				}
//				System.out.println("bwd index = " + bwdInd);
//				System.out.println(Arrays.toString(voicesCoDNotes.get(i)));
//			}
//		}
//		
//		System.exit(0);
//		
//		Rational[] cc = new Rational[]{new Rational(1, 3), new Rational(1, 4),};
//		List<Rational> dd = Arrays.asList(new Rational[]{cc[0], cc[1]});
//
//		System.out.println(Arrays.toString(cc));
//		System.out.println(dd);
//		Collections.reverse(dd);
//		System.out.println(Arrays.toString(cc));
//		System.out.println(dd);
//		
//		System.exit(0);
//		
//		List<Double> aLabel = Arrays.asList(new Double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0});
//		String asString = aLabel.toString();
//		System.out.println(asString);
//		
//		double misassignmentsDur = 5 + (0.5 * 5);
//		System.out.println(misassignmentsDur);
//	  double durationError = misassignmentsDur / 50;
//	  System.out.println(durationError);
//	  
//	
//		List<Rational> metricTimesNext = new ArrayList<Rational>();
//		metricTimesNext.add(new Rational(1, 2));
//		metricTimesNext.add(new Rational(1, 2));
//		System.out.println(Collections.min(metricTimesNext));
//		System.exit(0);
//		if (metricTimesNext.contains(null)) {
//			System.out.println("nondepie");
//		}
//		System.out.println(metricTimesNext.indexOf(null));
////		Rational closest = Collections.min(metricTimesNext);
////		System.out.println(closest);
//		System.exit(0);
//		
//		Transcription ts = new Transcription(new File(TrainingManager.prefix + "MIDI/Tests/Testpiece 1"), null);
////		ts.visualise("bla");
//		ScoreNote adaptedScoreNote = new ScoreNote(new ScorePitch(57), new Rational(6, 4), new Rational(1, 32));
//		NotationVoice nov = ts.getPiece().getScore().get(2).get(0);
//  	  for (NotationChord nc : nov) {
//  	  	if (nc.getMetricTime().equals(new Rational(6, 4))) {
//  	  		nc.get(0).setScoreNote(adaptedScoreNote);
//  	  	}
//  	  }
//  	  ts.visualise("bla");
//		
////		System.exit(0);
//		
//		List<Note> previousNote = new ArrayList<Note>();
//		previousNote.add(Transcription.createNote(60, new Rational(1, 2), new Rational(1, 4)));
//		previousNote.add(Transcription.createNote(60, new Rational(1, 2), new Rational(1, 4)));
//     
//    for (Note n : previousNote) {
//      System.out.println(n);
//    }
//    Rational adaptedDur = new Rational(1, 8);
//    for (Note n : previousNote) {
//    	ScoreNote scoreNote = new ScoreNote(new ScorePitch(n.getMidiPitch()), n.getMetricTime(), adaptedDur);
//    	n.setScoreNote(scoreNote);
//    }
//    for (Note n : previousNote) {
//      System.out.println(n);
//    }
//    System.exit(0);
//		
//		List<Integer> listOne = Arrays.asList(new Integer[]{0, 1, 2, 3, 4});
//		List<Integer> listTwo = Arrays.asList(new Integer[]{0, 1, 2, 3, 4});
//		List<List<Integer>> all = new ArrayList<List<Integer>>();
//		all.add(listOne); all.add(listTwo);
//		
//		for (List<Integer> l : all) {
//			if (l.equals(listOne)) {
//				System.out.println("ja one");
//			}
//			if (l.equals(listTwo)) {
//				System.out.println("ja two");
//			}
//		}
//		System.exit(0);
//		
//		Integer[] hoi = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
//		List<Integer> fuck = Arrays.asList(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
//		System.out.println(fuck);
//		Collections.reverse(fuck);
//		System.out.println(fuck);
//		System.exit(0);
//		
//		Integer[] hoi2 = Arrays.copyOf(hoi, 5);
//		System.out.println(Arrays.toString(hoi));
//		System.out.println(Arrays.toString(hoi2));
//		System.exit(0);
//		
//		List<Integer> predictedVoicesCurrentNote = new ArrayList<Integer>();
//		predictedVoicesCurrentNote.add(0); predictedVoicesCurrentNote.add(1);
//		System.out.println(predictedVoicesCurrentNote);
//		int firstPredictedVoiceCurrentNote = predictedVoicesCurrentNote.get(0); 
//		predictedVoicesCurrentNote = Arrays.asList(new Integer[]{firstPredictedVoiceCurrentNote});
//		System.out.println(predictedVoicesCurrentNote);
//		System.exit(0);
//		
//		List<Integer> adaptedPredictedVoicesCurrentNote = new ArrayList<Integer>(predictedVoicesCurrentNote);
//	  adaptedPredictedVoicesCurrentNote.remove(1);
//	  predictedVoicesCurrentNote = adaptedPredictedVoicesCurrentNote; // NEW
//	  System.out.println(predictedVoicesCurrentNote);
//		
//	  
//		List<Double> currentLabel = Arrays.asList(new Double[]{0.0, 1.0, 2.0, 3.0, 4.0});
//		List<List<Double>> newL1 = new ArrayList<List<Double>>();
//		List<List<Double>> newL2 = new ArrayList<List<Double>>();
//		newL1.add(new ArrayList<Double>(currentLabel.subList(0, Transcription.MAXIMUM_NUMBER_OF_VOICES)));
//		newL2.add(new ArrayList<Double>(currentLabel.subList(Transcription.MAXIMUM_NUMBER_OF_VOICES, currentLabel.size())));
//		System.out.println(currentLabel);
//		System.out.println(newL1);
//		System.out.println(newL2);
//		System.exit(0);
//		
//		List<Integer> al = Arrays.asList(new Integer[]{0, 1, 2, 3, 4, 5});
//		System.out.println(al);
//		Collections.reverse(al);
//		
//		
//		int vvv = Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom()/1;
//		System.out.println(vvv);
//		System.exit(0);
//		
//		FeatureGenerator fg = new FeatureGenerator();
//		Integer[][] basicNooteProperties = new Integer[2][8];
//		basicNooteProperties[0] = new Integer[]{-1, -1, -1, -1, -1, -1, 2, -1};
//		basicNooteProperties[1] = new Integer[]{-1, -1, -1, -1, -1, -1, 2, -1};
//		fg.enumerateVoiceAssignmentPossibilitiesForChord(null, basicNooteProperties, null, 0, 4);
//		System.exit(0);
//		
////		Transcription t = new Transcription(new File(TrainingManager.prefix + "MIDI/Tests/Testpiece 1"), null);
////		System.out.println(t.getVoiceCrossingInformation(null));
////		System.exit(0);
//		
//		DataConverter dc = new DataConverterTab();
////		FeatureGenerator fg = new FeatureGenerator();
//		DatasetMaker datasetMaker = new DatasetMaker(); 	  
// 	  List<String> pieceNames = new ArrayList<String>();
//// 	  pieceNames.addAll(datasetMaker.makeDataset(DatasetMaker.Dataset.WTC_THREE_VOICE_FUGUES));
// 	  pieceNames.addAll(datasetMaker.makeDataset(DatasetMaker.Dataset.WTC_FOUR_VOICE_FUGUES));
// 	 
// 	  for (String pieceName : pieceNames) {
// 	  	System.out.println(pieceName);
// 	  	Transcription trans = new Transcription(new File("F:/PhD/Data/MIDI/Bach/WTC/four-voice fugues/" + pieceName), null);
// 	  	List<Integer[]> meterInfo = trans.getMeterInfo();
// 	  	Integer[][] basicNoteProperties = trans.getBasicNoteProperties();
// 	  	List<List<Note>> transChords = trans.getTranscriptionChords();
// 	  	List<List<Double>> allVoiceLabels = trans.getVoiceLabels();
// 	  	List<List<List<Double>>> chordVoiceLabels = trans.getChordVoiceLabels(null);
// 	  	int lowestNoteIndex = 0;
// 	  	for (int i = 0; i < transChords.size(); i++) {
// 	  		List<Integer> pitchesInChord = fg.getPitchesInChord(null, basicNoteProperties, lowestNoteIndex);
// 	  		List<List<Double>> currentChordVoiceLabels = chordVoiceLabels.get(i);
// 				List<List<Integer>> voicesInChord = dc.getVoicesInChord(currentChordVoiceLabels);
// 	  	  List<List<Integer>> pAndV =	fg.getAllPitchesAndVoicesInChord(basicNoteProperties, pitchesInChord, 
// 	  	  	voicesInChord, allVoiceLabels, lowestNoteIndex);
// 	  	  List<Integer> currentPitchesInChord = pAndV.get(0);
// 	  	  // currentVoicesInChord must be a List<List>>
// 				List<List<Integer>> currentVoicesInChord = new ArrayList<List<Integer>>();
// 				for (int j : pAndV.get(1)) {
// 					int currentVoice = j;
// 					List<Integer> voiceWrapped = Arrays.asList(new Integer[]{currentVoice});
// 					currentVoicesInChord.add(voiceWrapped);
// 				}
// 	  	  List<List<Integer>> voiceCrossingInfo = fg.getVoiceCrossingInformationInChord(currentPitchesInChord, 
// 	  	  	currentVoicesInChord);
// 	  	  if ((voiceCrossingInfo.get(1).size() / 2) > 2) {
// 	  	  	int noteIndex = -1;
// 	  	  	for (int j = 0; j < basicNoteProperties.length; j++) {
// 	  	  		if (basicNoteProperties[j][Transcription.CHORD_SEQ_NUM] == i) {
// 	  	  			noteIndex = j;
// 	  	  		}
// 	  	  	}
// 	  	  	Rational onsetTime = new Rational(basicNoteProperties[noteIndex][Transcription.ONSET_TIME_NUMER],
// 	  	  		basicNoteProperties[noteIndex][Transcription.ONSET_TIME_DENOM]);	
// 	  	  	Rational[] metricPos = Tablature.getMetricPosition(onsetTime, meterInfo);
// 	  	    System.out.println("more than 2 vc pairs in chord " + i + " (bar " + metricPos[0].getNumer() + 
// 	  	      " " + metricPos[1] + ")");	
// 	  	  }
// 	  	  lowestNoteIndex += transChords.get(i).size();
// 	  	}
//
//// 	  	System.out.println(trans.getVoiceCrossingInformation(null));
// 	  }
//		
// 	  System.exit(0);
//		
//		
//		List<Rational> lr = new ArrayList<Rational>();
//		lr.add(new Rational(6, 6));
//		lr.add(new Rational(1, 6));
//		lr.add(new Rational(4, 5));
//		Rational res = Rational.ZERO;
//		for (Rational r : lr) {
//			res = res.add(r);
//		}
//		System.out.println(res);
//		System.exit(0);
//		
//		File file2 = new File(TrainingManager.prefix + 
//			"MIDI/Bach/WTC/Three-voice fugues/Bach - WTC2, Fuga 10 in e minor (BWV 879)");
//		Transcription eli = new Transcription(file2, null);
////		System.out.println(eli.getMirrorPoint(null));
////	  NotationSystem notationSyst = eli.getPiece().getScore();
////	  for (NotationStaff s : notationSyst) {
////			// (s will be size 1)	
////			for (NotationVoice nv : s) {
////				Rational end = nv.getEndTime();
////				NotationChord lastNotationChord = nv.get(nv.size() - 1);
////				Rational offsetTimeLastNote = Rational.ZERO;
////				for (Note n : lastNotationChord) {
////					Rational duration = n.getMetricDuration();
////					Rational offsetTime = n.getMetricTime().add(duration);
////					if (offsetTime.isGreater(offsetTimeLastNote)) {
////						offsetTimeLastNote = offsetTime;	
////					}
////				}
////				System.out.println(end + " / " + offsetTimeLastNote);			
////			}
////		}
////		System.exit(0);
//		
//		Integer[][] bnpEli = eli.getBasicNoteProperties();
//		Rational onsetBass = new Rational(bnpEli[1046][Transcription.ONSET_TIME_NUMER], bnpEli[1046][Transcription.ONSET_TIME_DENOM]); 
//		Rational onsetTenor = new Rational(bnpEli[1049][Transcription.ONSET_TIME_NUMER], bnpEli[1049][Transcription.ONSET_TIME_DENOM]); 
//		Rational onsetSup = new Rational(bnpEli[1050][Transcription.ONSET_TIME_NUMER], bnpEli[1050][Transcription.ONSET_TIME_DENOM]);
//		Rational durBass = new Rational(bnpEli[1046][Transcription.DURATION_NUMER], bnpEli[1046][Transcription.DURATION_DENOM]);
//		Rational durTenor = new Rational(bnpEli[1049][Transcription.DURATION_NUMER], bnpEli[1049][Transcription.DURATION_DENOM]);
//		Rational durSup = new Rational(bnpEli[1050][Transcription.DURATION_NUMER], bnpEli[1050][Transcription.DURATION_DENOM]);
//		Rational offsetBass = onsetBass.add(durBass);
//		Rational offsetTenor = onsetTenor.add(durTenor);
//		Rational offsetSup = onsetSup.add(durSup);
//		System.out.println("onsets:");
//		System.out.println(onsetBass + " " + onsetTenor + " " + onsetSup);
//		System.out.println("offsets:");
//		System.out.println(offsetBass + " " + offsetTenor + " " + offsetSup);
//		System.exit(0);
//		
//		File midiTestpiece1 = new File(TrainingManager.prefix + "MIDI/Tests/Testpiece 1");
//		Transcription tra = new Transcription(midiTestpiece1, null);
//		
//		int onsetNum = tra.getBasicNoteProperties()[39][Transcription.ONSET_TIME_NUMER];
//		int onsetDenom = tra.getBasicNoteProperties()[39][Transcription.ONSET_TIME_DENOM];
//		Rational onset = new Rational(onsetNum, onsetDenom);
//		int durNum = tra.getBasicNoteProperties()[39][Transcription.DURATION_NUMER];
//		int durDenom = tra.getBasicNoteProperties()[39][Transcription.DURATION_DENOM];
//		Rational duration = new Rational(durNum, durDenom);
//		Rational offsetLastNote = onset.add(duration).sub(new Rational(1, 128));
//		System.out.println(onset);
//		System.out.println(duration);
//		System.out.println(offsetLastNote);
//		Rational[] metricPositionEnd = Tablature.getMetricPosition(offsetLastNote, tra.getMeterInfo());
//		System.out.println(metricPositionEnd[0]);
//		System.out.println(metricPositionEnd[1]);
//		System.exit(0);
//		
////		DatasetMaker dm = new DatasetMaker();
////		List<String> fugueNames = dm.makeDataset(DatasetMaker.Dataset.WTC_THREE_VOICE_FUGUES);
////		for (String name : fugueNa)mes) {
////		  Transcription trn = 
////		  	new Transcription(new File(TrainingManager.prefix + "MIDI/Bach/WTC/Three-voice fugues/" + name), null);
////		  NotationSystem notationSystem = trn.getPiece().getScore();
////			List<Rational> ends = new ArrayList<Rational>();
////		  for (NotationStaff s : notationSystem) {
////				// (s will be size 1)	
////				for (NotationVoice nv : s) {
////					Rational end = nv.getEndTime();
////					System.out.println(end);
////					ends.add(end);
////				}
////			}
////		  Rational first = ends.get(0);
////		  for (Rational r : ends) {
////		    if (!r.equals(first)) {
////		    	System.out.println("UNEVEN END TIME IN " + name);
////		    }
////		  }
////		  
////		}
//		
//	  Transcription trn = new Transcription(new File(TrainingManager.prefix + "MIDI/Bach/WTC/Two-voice fugues/" 
//	    + "Bach - WTC1, Fuga 10 in e minor (BWV 855)"), null);
////	  Transcription trn = new Transcription(new File(TrainingManager.prefix + "MIDI/Bach/WTC/Three-voice fugues/" 
////		  + "Bach - WTC2, Fuga 4 in c# minor (BWV 873)"), null);
////	  Transcription trn = new Transcription(new File(TrainingManager.prefix + "MIDI/Bach/WTC/Three-voice fugues/" 
////			+ "Bach - WTC2, Fuga 11 in F major (BWV 880)"), null);
////	  Transcription trn = new Transcription(new File(TrainingManager.prefix + "MIDI/Bach/WTC/Three-voice fugues/" 
////			+ "Bach - WTC2, Fuga 16 in g minor (BWV 885)"), null);
//    Integer[][] bnp = trn.getBasicNoteProperties();
//    int lastNoteIndex = bnp.length - 1;
//    Rational mt = new Rational(bnp[lastNoteIndex][Transcription.ONSET_TIME_NUMER], 
//	    bnp[lastNoteIndex][Transcription.ONSET_TIME_DENOM]);
//    System.out.println(mt);
//    Rational[] metPos = Tablature.getMetricPosition(mt, trn.getMeterInfo());
//    System.out.println(metPos[0].getNumer() + " " + metPos[1]);
//    NotationSystem notationSystem = trn.getPiece().getScore();
//    // 1. Get the latest end time of all voices
//  	Rational latestEndTime = Rational.ZERO;
//		// For each voice
//  	int counter = 0;
//  	for (NotationStaff notationStaff : notationSystem) {	
//			System.out.println("voice " + counter);
//			counter++;
//  		for (NotationVoice notationVoice : notationStaff) {
//  			// 1. Get metric position in bar of note with latest end time
//  			// 2. Find onset time of "next" bar by adding (total bar time in current meter - onset time of note with
//  			//    latest end time) to onset time of note with latest end time
//  			// 3. Use onset time of "next" bar as mirrorpoint: onset time of first backwards note = mirrorPoint - 
//  			//    offset time of that note, etc.
//  			// 
//				Rational currEndTime = notationVoice.getEndTime();
//				System.out.println("end time " + currEndTime);
//				Rational[] metPosEndtime = Tablature.getMetricPosition(currEndTime, trn.getMeterInfo());
//		    System.out.println(metPosEndtime[0].getNumer() + " " + metPosEndtime[1]);
//				if (currEndTime.isGreater(latestEndTime)){
//			  	latestEndTime = currEndTime;
//			  }
//			}
//		}
//    
//    
//    System.exit(0);
//		
//		
////		List<Integer> listOne = Arrays.asList(new Integer[]{0, 1, 2, 3, 4});
////		List<Integer> listTwo = Arrays.asList(new Integer[]{5, 6, 7, 8, 9});
////		List<Integer> combined = new ArrayList<Integer>(Arrays.asList(new Integer[]{0, 1, 2, 3, 4}));
////		System.out.println(listOne);
////		System.out.println(listTwo);
////		System.out.println(combined);
////		combined.addAll(Arrays.asList(new Integer[]{5, 6, 7, 8, 9}));
////		System.out.println(listOne);
////		System.out.println(listTwo);
////		System.out.println(combined);
//		
//		List<Double> vlOne = Arrays.asList(new Double[]{1.0, 0.0, 0.0, 0.0, 0.0});
//		List<Double> vlTwo = Arrays.asList(new Double[]{0.0, 0.0, 1.0, 0.0, 0.0});
//		List<Double> comb = null;
//		System.out.println(vlOne);
//		System.out.println(vlTwo);
//		comb = Transcription.combineLabels(vlOne, vlTwo);
//		System.out.println(vlOne);
//		System.out.println(vlTwo);
//		System.out.println(comb);
//		
//		System.exit(0);
//		
//		Rational[] rn = new Rational[]{new Rational(1, 2), new Rational(3, 4)};
////		System.out.println(Arrays.asList(rn));
//		
//
//		
//		Integer[] as = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
//		System.out.println(Arrays.toString(as));
//		
//		
//				
//		int[] bla = new int[]{0, 1, 2, 3};
//		for (int i : bla) {
//		  System.out.println(i);
//		}
//		int[] bla2 = Arrays.copyOfRange(bla, 1, 3);
//		for (int i : bla) {
//		  System.out.println(i);
//		}
//		for (int i : bla2) {
//		  System.out.println(i);
//		}
//		System.exit(0);
//		
//		
////		String testP = "Phalese 1547 - Tant que uiuray [a3]";
////		String testP = "Pisador 1552 - Pleni de la missa misma";
////		String testP = "Phalese 1547 - Tant que uiuray [a4]";
////		String testP = "Phalese 1563 - LAs on peult";
////		String testP = "Rotta 1546 - Bramo morir per non patir piu morte";
////		String testP = "Adriansen 1584 - D'Vn si bel foco";
////		
////		File en = new File(TrainingManager.prefix + "Encodings/Dataset/" + testP + ".txt");
////		File mi = new File(TrainingManager.prefix + "MIDI/Dataset/" + testP);
////		Tablature tab = new Tablature(en);
////		Transcription tra = new Transcription(mi, en);
////		Integer[][] btp = tab.getBasicTabSymbolProperties();
////		int lastNoteIndex = (btp.length) - 1;
////		Rational mtLastChord =  new Rational(btp[lastNoteIndex][Tablature.ONSET_TIME], 
////			Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom());	
////		Rational[] meterInf = Tablature.getMetricPosition(mtLastChord, tab.getMeterInfo());
////		System.out.println(meterInf[0].getNumer() + " " + meterInf[1]);
////		System.exit(0);
//		
//		
//		
////		Transcription trannie = new Transcription(new File(TrainingManager.prefix + "MIDI/Bach/WTC/Four-voice fugues/" +
////			"Bach - WTC1, Fuga 17 in Ab major (BWV 862)"), null);
////		Integer[][] bnp = trannie.getBasicNoteProperties();
////		Rational mt = 
////			new Rational(bnp[684][Transcription.ONSET_TIME_NUMER], bnp[684][Transcription.ONSET_TIME_DENOM]); 
////		Rational[] metPos = Tablature.getMetricPosition(mt, trannie.getMeterInfo());
////		System.out.println(metPos[0].getNumer() + " " + metPos[1]);
////		System.exit(0);
//		
//		List<Double> ld = AuxiliaryTool.getStoredObject(new ArrayList<Double>(), new File(TrainingManager.prefix + 
//			"Files out/Neural network/fourVoiceFugues/lambda = 3.0E-5/Hidden nodes = -4/Fold 16/Best weights NOTE_TO_NOTE.xml"));
//		System.out.println(ld.size());
//		System.exit(0);
//		
//		
//		
//		File encoding2 = new File(TrainingManager.prefix + "Encodings/Dataset/Phalese 1563 - LAs on peult.txt");
//		File midi2 = new File(TrainingManager.prefix + "MIDI/Dataset/Phalese 1563 - LAs on peult");
//		Tablature tablature2 = new Tablature(encoding2);
//		Transcription transcription2 = new Transcription(midi2, encoding2);
//		System.out.println(transcription2.getChordsSpecification());
//		System.exit(0);
//				
//		Transcription transcription = new Transcription(new File("F:/PhD/Data/MIDI/Bach/WTC/Four-voice fugues/" +
//	  	"Bach - WTC1, Fuga 5 in D major (BWV 850)"), null);
//	  List<Integer[]> sdf = transcription.getEqualDurationUnisonsInfo();
//	  
//	  for (int i = 0; i < sdf.size(); i++) {
//	  	
//	  	if (sdf.get(i) != null) {
//	  		System.out.println("i = " + i + " " + sdf.get(i)); 
//	  	}
//	  }
//	  System.exit(0);
//		
//	  File encod = new File("F:/PhD/Data/Encodings/Dataset/Abondante 1548 - mais mamignone.txt"); 	
//
//	  for (int i = 0; i < 10; i++) {
//	  	System.out.println(i);
//	  	System.out.println("bla bla");
//	  	if (i == 1 || i == 4 || i == 7 || i == 9) {
//	  	  i++;
//	  	}
//	  }
//	  System.exit(0);
//	  
//	  
//	  Tablature ta = new Tablature(encod);
//
//    List<List<TabSymbol>> tc = ta.getTablatureChords();
//    List<String> listOfTabSymbols = ta.getListsOfSymbols().get(Tablature.TAB_SYMBOLS_INDEX);
//    System.out.println(listOfTabSymbols.size());
//    
//    List<Integer> gridXOfTabSymbols = ta.getListsOfStatistics().get(Tablature.GRID_X_INDEX);
//    List<Integer> gridYOfTabSymbols = ta.getListsOfStatistics().get(Tablature.GRID_Y_INDEX);
//    List<Integer> durationOfTabSymbols = ta.getListsOfStatistics().get(Tablature.DURATION_INDEX);
//    List<Integer> horizontal = ta.getListsOfStatistics().get(Tablature.HORIZONTAL_POSITION_INDEX);
//    List<Integer> vertical = ta.getListsOfStatistics().get(Tablature.VERTICAL_POSITION_INDEX);
//    List<Integer> durEvents = ta.getListsOfStatistics().get(Tablature.DURATION_OF_EVENTS_INDEX);
//    List<Integer> hp = ta.getListsOfStatistics().get(Tablature.HORIZONTAL_POSITION_TAB_SYMBOLS_ONLY_INDEX);
//    
//    System.out.println(hp);
//    System.exit(0);
////    
////    Integer[][] btp = tabl.getBasicTabSymbolProperties();
////    for (int i = 0; i < btp.length; i++) {
////    	Integer[] curr = btp[i];
////    	System.out.println(i + "\t" + curr[0] + "\t" + curr[1] + "\t" + curr[2] + "\t" + curr[3] + "\t" + curr[4] +
////    		"\t" + curr[5] + "\t" + curr[6] + "\t" + curr[7] + "\t" + curr[8] + "\t" + curr[9]);
////    }
////    
////    int previousSeqNum = -1;
////    int numChords = 0;
////    for (int i = 0; i < btp.length; i++) {
////    	Integer[] cbtp = btp[i];
////    	int seqNum = cbtp[Tablature.ONSET_TIME];
////    	if (seqNum != previousSeqNum) {
////    		previousSeqNum = seqNum;
////    		numChords++;
////    		
////    	}
////    }
////    System.out.println("numChords = " + numChords);
//    
////    Integer[][] btp = tabl.getBasicTabSymbolProperties();
////    System.out.println(tc.size());
////    System.out.println(btp.length);
//    
////    System.out.println(tabl.getFootnotes());
////    List<Integer> ls = tabl.getListsOfStatistics().get(Tablature.IS_TAB_SYMBOL_EVENT_INDEX);
////    int count  = 0;
////    for (Integer st : ls) {
////    	System.out.println(st);
////    	if (st == 1) {
////    		count++;
////    	}
////    }
////    System.out.println(count);
////    System.exit(0);
//    
//	  File encoding = new File("F:/PhD/Data/Encodings/Dataset/Abondante 1548 - mais mamignone.txt"); 	
//    Tablature tabl = new Tablature(encoding);
//	  Transcription transc = new Transcription(new File("F:/PhD/Data/MIDI/Dataset/Abondante 1548 - mais mamignone"), encoding);
//    int transpositionInterv = tabl.normaliseTuning(); 
//    transc.transpose(transpositionInterv); 
//    
//    System.out.println(tabl.getTablatureChords().size()); 
//    System.out.println(transc.getTranscriptionChords().size());
//    
//    for (int i = 0; i < tabl.getTablatureChords().size(); i++) {
//    	System.out.println(i + ": " 
//        + tabl.getTablatureChords().get(i).get(0).getPitch(tabl.getTunings()[Tablature.NEW_TUNING_INDEX], 
//    		tabl.getTuningSeventhCourse()));
//    }
//    System.exit(0);
//		
////		String ft = "aaaa\r\nbbbb\r\n";
////		AuxiliaryTool.storeTextFile(ft, new File("F:/PhD/Data/Teringzooi.txt"));
//
//		String stri = AuxiliaryTool.readTextFile(new File("F:/PhD/Data/Encodings/Tests/Testpiece 1.txt"));
//		System.out.println(stri.indexOf(DataContainer.END_BREAK_INDICATOR));
////		System.out.println(stri.contains("\r\n"));
////		System.out.println(stri.contains("\r\n\r\n"));
////		System.out.println(stri.contains("\r\n\r\n\r\n"));
////		System.out.println(stri.length());
////		AuxiliaryTool.storeTextFile(stri, new File("F:/PhD/Data/Teringzooi.txt"));
////		
////	  String striStoredAndRead = AuxiliaryTool.readTextFile(new File("F:/PhD/Data/Teringzooi.txt"));
////	  System.out.println(striStoredAndRead.contains("\r\n"));
////		System.out.println(striStoredAndRead.contains("\r\n\r\n"));
////		System.out.println(striStoredAndRead.contains("\r\n\r\n\r\n"));
////		System.out.println(striStoredAndRead.length());
//	  System.exit(0);
//
//		//		System.out.println(ftRead.contains("\r\n\r\n"));
////		String ftRead = AuxiliaryTool.readTextFile(new File("F:/PhD/Data/Teringzooi.txt"));
////		System.out.println(ftRead.contains("\r\n\r\n"));
////		AuxiliaryTool.storeTextFile(ftRead, new File("F:/PhD/Data/Teringzooi.txt"));
////		System.out.println(ftRead.length());
//		
//		System.exit(0);
//		
//		
//    List<File> encodingFiles = new ArrayList<File>();
//	  encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Dataset/Abondante 1548 - Mais mamignone.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Dataset/Adriansen 1584 - D'Vn si bel foco.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Dataset/Barbetta 1582 - Il nest plaisir.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Dataset/Judenkunig 1523 - Elslein liebes Elslein.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Dataset/Newsidler 1544 - Nun volget Lalafete.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Dataset/Newsidler 1536 - Disant adiu madame.txt"));
//	  encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Dataset/Ochsenkun 1558 - Absolon fili mi.txt"));
//	  encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Dataset/Ochsenkun 1558 - Cum Sancto spiritu.txt"));  	
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Dataset/Ochsenkun 1558 - Herr Gott lass dich erbarmen.txt"));
//	  encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Dataset/Ochsenkun 1558 - In exitu Israel de Egipto.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Dataset/Ochsenkun 1558 - Inuiolata integra.txt"));
//	  encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Dataset/Ochsenkun 1558 - Qui habitat.txt")); 	
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Dataset/Phalese 1547 - Tant que uiuray [a3].txt"));    
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Dataset/Phalese 1547 - Tant que uiuray [a4].txt"));    
//	  encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Dataset/Phalese 1563 - LAs on peult.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Dataset/Pisador 1552 - Pleni de la missa misma.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Dataset/Rotta 1546 - Bramo morir per non patir piu morte.txt"));
//    
////    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Tests/Testpiece 1.txt"));
////    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Tests/Testpiece 1a.txt"));
//
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Barbetta 1582 - Martin menoit.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Da Crema 1546 - Il nest plaisir.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Heckel 1562 - Il est vne Fillete. [Tenor].txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Heckel 1562 - Il estoit vne fillete. Discant.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Morlaye 1552 - LAs on peut iuger.txt"));  	  	
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Newsidler 1544 - Der hupff auf.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Newsidler 1544 - Hie volget die Schlacht vor Bafia. Der Erst Teyl.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Newsidler 1544 - Hie volget die Schlacht vor Bafia. Der ander Teyl der schlacht.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Newsidler 1544 - Sula Bataglia.txt"));
//	  encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Ochsenkun 1558 - Benedicta es coelorum, Prima pars.txt"));
//	  encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Ochsenkun 1558 - Gott alls in allem wesentlich.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Ochsenkun 1558 - Pater Noster, Prima pars.txt")); 
//	  encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Ochsenkun 1558 - Praeter rerum seriem, Prima pars.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Ochsenkun 1558 - Stabat mater dolorosa, Prima pars.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Phalese 1546 - Martin menuyt de Iennequin.txt"));
//    encodingFiles.add(new File(TrainingManager.prefix + "Encodings/Spinacino 1507 - LA Bernardina de Iosquin.txt"));
//
//
//
//	  for (File f : encodingFiles) {
//	  	String s = AuxiliaryTool.readTextFile(f);
//	  	if (s.contains("\r\n")) {
//	  		System.out.println(f.getName());
//	  	}
//	  }
//		System.exit(0);
//	  
//    String str = "regel1\nregel2\nregel3";
//    str = str.replace("\n", "\r\n");
//		char[] ac = str.toCharArray();
//    for (int i = 0; i < ac.length - 1; i++) {
//    	char currentChar = ac[i];
//    	char nextChar = ac[i + 1];
//    	if (Character.toString(currentChar).equals("\r") && Character.toString(nextChar).equals("\n") ) {
//    		System.out.println("GODVERDOMME NOG NEET");
//    		
//    	}
//    }
////		str = str.replace("\n", "\r\n");
////		if (str.contains("\r\n")) {
////  		System.out.println("JOO");
//////  		str = str.replace("\n", "\r\n");
////  	}
////    for (int i = 0; i < str.length(); i++) {
////    	
////    	if (str.contains("\n\r")) {
////    		System.out.println("JOO");
//////    		str = str.replace("\n", "\r\n");
////    	}
////    }
////    System.out.println(str);
////		File filee = new File("F:/PhD/Data/Testje.txt");
////		AuxiliaryTool.storeTextFile("Nieuwe tekst.", filee);
//				
//	  System.exit(0);
//
//			
////		Integer[] ii = new Integer[2];
////		System.out.println(ii[0]);
////		System.out.println(ii[1]);
//////		String st = "a..b.";
//////		System.out.println(st.indexOf(".", 1 + 1));
//
//		
//		String sf = "a.b.c.d.e.";
//		List<String> alc = new ArrayList<String>();
//		for (int j = 0; j < 10; j++) {
//			alc.add(Character.toString(sf.charAt(j)));
//		}
//    if (Collections.frequency(alc, "}") != 1 || Collections.frequency(alc, "{") != 0) {
//    	System.out.println("JOEEEE");
//    }
//		
//		
//		System.exit(0);
//		
//		List<Integer> aListA = Arrays.asList(new Integer[]{4, 3, 1, 2});
//		List<Integer> aListC = Arrays.asList(new Integer[]{4, 3, 1, 2});
//		System.out.println(aListA.equals(aListC));
//		List<Integer> aListB = new ArrayList<Integer>(aListA);
//		Collections.reverse(aListB);
//		System.out.println(aListA);
//		System.out.println(aListB);
//		System.exit(0);
//		
//		String event = "a.b.c.d.";
//  	String[] allSymbols = event.replace(DataContainer.SYMBOL_SEPARATOR, " ").split(" ");
//		System.out.println(event);
////		System.exit(0);
//  	
//	  List<Integer> aList = new ArrayList<Integer>();
//	  if (aList.contains(7)) {
//	  	System.out.println("joo");
//	  }
//		Integer[] anArr = new Integer[]{1, 2, 1, 4};
//		for (int i : anArr) {
//			if (aList.contains(i)) {
//				System.out.println("ja");
//				break;
//			}
//			else {
//				aList.add(i);
//			}
//		}
//		System.out.println(aList);
//		
////		String sss = "a.b.c.d.";
////		sss = sss.replace(DataContainer.SYMBOL_SEPARATOR, " ");
////    String[] ssss = sss.split(" ");
////    for (String s : ssss) {
////      System.out.println(s);
////    }
////    System.out.println(ssss.length);
//    
//
//		
//		System.exit(0);
//		
//		Transcription trans = new Transcription(new File(TrainingManager.prefix + 
//			"MIDI/WTC/Book I/Three-voice fugues/Bach - WTC1, Fuga 6 in d minor (BWV 851)"), null);
////		preprocessor.preprocess(null, trans, false, new Integer(0));
//		
//		List<Integer[]> meterInfo = trans.getMeterInfo(); 
//		for (Integer[] inte : meterInfo) {
//		  System.out.println(Arrays.asList(inte));
//	  }
//		System.exit(0);
//		
//		MetricalTimeLine mtl = trans.getPiece().getMetricalTimeLine();
//		TimeSignatureMarker timeSig = mtl.getTimeSignatureMarker(new Rational(0, 1));
//		System.out.println(timeSig);
//		
//			
//		String fileName = "F:/PhD/Data/Files out/Neural network/gridSearch/gridSearch NOTE_TO_NOTE featureSet D/" + 
//      "lambda = 0.0/Hidden nodes = -4/Fold 1/Training process record NOTE_TO_NOTE run 0.txt";
//    File file = new File(fileName);
//    String content = AuxiliaryTool.readTextFile(file);
//    System.out.println(content.length());
//    System.exit(0);
//    
////    String fileName = "F:/PhD/Data/Files out/Neural network/Quick test/lambda = 3.0E-5/" + 
////      "Hidden nodes = -4/No CV/Training results NOTE_TO_NOTE - kopie.txt";
////    File file = new File(fileName);
////    String content = AuxiliaryTool.readTextFile(file);
//////    System.out.println(content);
////    String target = "number of onsets assigned to the incorrect voice";
////    String replacement = "GODNONDEDJU";
////    content = content.replace(target, replacement);
////    System.out.println(content);
////    AuxiliaryTool.storeTextFile(content, new File(
////    	"F:/PhD/Data/Files out/Neural network/Quick test/lambda = 3.0E-5/Hidden nodes = -4/No CV/SHIT.txt"));
//    
//		System.exit(0);
//		
//		String experimentFolderName = "gridSearch/gridSearch NOTE_TO_NOTE featureSet D";
//    
//    List<Double> lambdas = new ArrayList<Double>();
//    lambdas.add(0.1);
//    lambdas.add(0.03);
//	  lambdas.add(0.01);
//	  lambdas.add(0.003); 
//	  lambdas.add(0.001);
//	  lambdas.add(0.0003);
//	  lambdas.add(0.0001);
//	  lambdas.add(0.00003);
//  	lambdas.add(0.00001);
//    lambdas.add(0.0); 
//    
//    List<Integer> hiddenNeuronsFactors = new ArrayList<Integer>();
//  	hiddenNeuronsFactors.add(-8);
//  	hiddenNeuronsFactors.add(-4);
//   	hiddenNeuronsFactors.add(-2); 
//  	hiddenNeuronsFactors.add(1);
//  	hiddenNeuronsFactors.add(2); 
//  	hiddenNeuronsFactors.add(4);
//    
//    int numberOfFolds = 9;
//  	
//    int outputsCounted = 0;
//    int casesSolvedArbitrarily = 0;
//    int numOfHighestTwice = 0;
//    int numOfCoDWithHighestThrice = 0;
//    int numOfCoDWithHighestFourTimes = 0;
//    int numOfCoDWithHighestFiveTimes = 0;
//    int numOfCoDWithHighestOnceSecondHighestTwice = 0;
//    int numOfCoDWithHighestOnceSecondHighestThrice = 0;
//    int numOfCoDWithHighestOnceAndSecondHighestFourTimes = 0;
//    for (double lambda : lambdas) {
//      // Turn lambda into a String and remove any trailing 0s from it (only if lambda != 0.0)
//  	  String lambdaAsString = Double.toString(lambda);
//  	  if (lambda != 0.0) {
//  	    boolean endsWithZero = lambdaAsString.endsWith("0");
//  	    while (endsWithZero == true) {
//  	  	  lambdaAsString = lambdaAsString.substring(0, lambdaAsString.length() - 1);
//  	  	  endsWithZero = lambdaAsString.endsWith("0");
//  	    }
//  	  }
//  	  for (int hiddenNeuronsFactor : hiddenNeuronsFactors) {
//        for (int i = 0; i < numberOfFolds; i++) {
//        	int currentFold = i + 1;
//        	String currentFolderName = TrainingManager.prefix + "Files out/Neural network/" + experimentFolderName +
//        	 	"/lambda = " + lambdaAsString + "/Hidden nodes = " + hiddenNeuronsFactor + "/Fold " + currentFold + "/";
//        	File f = new File(currentFolderName + "Training process record NOTE_TO_NOTE run 0.txt");
//        	String contents = AuxiliaryTool.readTextFile(f);
//        	
//        	int preStartIndex = contents.indexOf("output" + "\n" + "[");
//        	while (preStartIndex != -1) {
//        		int startIndex = preStartIndex + 8;
//        		int endIndex = contents.indexOf("]", startIndex);
//        		String output = contents.substring(startIndex, endIndex);
//        		outputsCounted++;
//        		
//       		  String[] numbers = output.split("\t");        		
//       		  List<Double> labelList = new ArrayList<Double>();
//       		  for (String s : numbers) {
//        		  double d = Double.parseDouble(s);
//        		  labelList.add(d);
//          	}
//         		double highest = Collections.max(labelList);
//       	  	int frequencyOfHighest = Collections.frequency(labelList, highest);
//         		if (frequencyOfHighest > 1) {
//         			if (frequencyOfHighest == 2) {
//         				numOfHighestTwice++;
////           		  OutputEvaluator oe = new OutputEvaluatorTab();
////         				System.out.println(oe.determinePresenceOfCoD(labelList, 0.05));
////         				System.out.println("freq of highest = 2");
////         				System.out.println("output = " + labelList);
//         			}
//         			else if (frequencyOfHighest == 3) {
//         				numOfCoDWithHighestThrice++;
//         				casesSolvedArbitrarily++;
//         				System.out.println("CoD with freq of highest = 3; case solved arbitrarily");
//         				System.out.println("output = " + labelList);
//         			}
//         			else if (frequencyOfHighest == 4) {
//         				numOfCoDWithHighestFourTimes++;
//         				casesSolvedArbitrarily++;
//         				System.out.println("CoD with freq of highest = 4; case solved arbitrarily");
//         				System.out.println("output = " + labelList);
//         			}
//         			else if (frequencyOfHighest == 5) {
//         				numOfCoDWithHighestFiveTimes++;
//         				casesSolvedArbitrarily++;
//         				System.out.println("CoD with freq of highest = 5; case solved arbitrarily");
//         				System.out.println("output = " + labelList);
//         			}
//         		}
//         		List<Double> labelListWithoutHighest = new ArrayList<Double>();
//         		for (double d : labelList) {
//         			if (d != highest) {
//         				labelListWithoutHighest.add(d);
//         			}
//         		}
//       	  	double secondHighest = Collections.max(labelListWithoutHighest);
//       	  	int frequencyOfSecondHighest = Collections.frequency(labelListWithoutHighest, secondHighest);
//       	  	
//       	  	boolean containsCoD = false;
//       	  	double deviationThreshold = 0.05;
//       	  	double maximumDeviation = deviationThreshold * highest;
//       			if (secondHighest >= (highest - maximumDeviation)) {
//       		   containsCoD = true;
//       		  }
//       			// Cases that are solved arbitrarily (see OutputEvaluatorTab.interpretPredictedLabel())
//       			if (frequencyOfHighest == 1 && containsCoD) {
//           		if (frequencyOfSecondHighest == 2) {
//           			numOfCoDWithHighestOnceSecondHighestTwice++; 
//           			casesSolvedArbitrarily++;
//          			System.out.println("CoD  with freq of highest = 1 and freq of second highest = 2; case solved arbitrarily");
//           			System.out.println("output = " + labelList);
//           		}
//           		else if (frequencyOfSecondHighest == 3) {
//           			numOfCoDWithHighestOnceSecondHighestThrice++;
//           			casesSolvedArbitrarily++;
//           			System.out.println("CoD with freq of highest = 1 and freq of second highest = 3; case solved arbitrarily");
//          			System.out.println("output = " + labelList);
//           		}
//           		else if (frequencyOfSecondHighest == 4) {
//           			numOfCoDWithHighestOnceAndSecondHighestFourTimes++;
//           			casesSolvedArbitrarily++;
//          		  System.out.println("CoD with freq of highest = 1 and freq of second highest = 4; case solved arbitrarily");
//          		  System.out.println("output = " + labelList);
//           		}
//       			}
//       	  	
//       	  	preStartIndex = contents.indexOf("output" + "\n" + "[", endIndex);
//       	  	
////       	  	System.out.println("output = " + labelList);
////       	  	System.out.println("highest = " + max);
////         		System.out.println("frequency of highest = " + frequencyOfMax);
////         		System.out.println("second highest = " + secondMax);
////         		System.out.println("frequency of second highest = " + frequencyOfSecondMax);
//          }
//        }
//      }
//    }
//    
//    System.out.println("outputs counted = " + outputsCounted);
//    System.out.println("cases solved arbitrarily = " + casesSolvedArbitrarily);
//    System.out.println("specifications of cases solved arbitrarily:");
////    System.out.println("  numOfHighestTwice = " + numOfHighestTwice);
//    System.out.println("  CoD with highest output three times = " + numOfCoDWithHighestThrice);
//    System.out.println("  CoD with highest output four times = " + numOfCoDWithHighestFourTimes);
//    System.out.println("  CoD with highest output five times = " + numOfCoDWithHighestFiveTimes);
//    System.out.println("  CoD with highest output once and second highest two times = " + numOfCoDWithHighestOnceSecondHighestTwice);
//    System.out.println("  CoD with highest output once and second highest three times = " + numOfCoDWithHighestOnceSecondHighestThrice);
//    System.out.println("  CoD with highest output once and second highest four times = " + numOfCoDWithHighestOnceAndSecondHighestFourTimes);
//    
//		System.exit(0);
//		
////		List<Integer> aList = Arrays.asList(new Integer[]{});
////		List<Integer> distances = new ArrayList<Integer>();
////		for (int i = 0; i < aList.size(); i++) {
////			int currentInt = aList.get(i);
////			if (i != aList.size() - 1) {
////				int nextInt = aList.get(i + 1);
////				distances.add(nextInt - currentInt);
////			}
////		}
////		int sum = AuxiliaryTool.sumListInteger(distances);
////		double avg = (double) sum / distances.size();
////		System.out.println(aList.size());
////		System.out.println(avg); 
//		
////		List<Integer> aList = Arrays.asList(new Integer[]{0, 1, 3, 4, 25, 26, 27, 31, 32, 35, 36, 38, 39, 42, 56, 61, 77, 81, 82, 84, 87, 88, 91, 94, 95, 97, 98, 99, 101, 102, 104, 106, 107, 111, 112, 114, 115, 120, 122, 127, 140, 141, 144, 145, 146, 147, 149, 150, 154, 157, 171, 174, 176, 178, 179, 181, 182, 193, 201, 217, 229, 250, 276, 288, 308, 325, 326, 328, 329, 330, 331, 334, 343, 352, 353, 355, 356, 358, 359, 361, 362, 364, 365, 367, 368, 370, 371, 373, 374, 376, 377, 379, 380, 382, 383, 401, 404, 407, 410, 435, 436, 452, 487, 490, 525, 540, 542, 546, 549, 552, 559, 597, 600, 635, 648, 652, 655, 662, 679});
////		int numberOfOnes = 0;
////		for (int i = 0; i < aList.size(); i++) {
////			int currentInt = aList.get(i);
////			if (i != aList.size() - 1) {
////				int nextInt = aList.get(i + 1);
////				if (nextInt - currentInt == 1) {
////					numberOfOnes++;
////				}
////			}
////		}
////		int numTransitions = aList.size() - 1; 
////		System.out.println(aList.size());
////		System.out.println(numberOfOnes + "/" + numTransitions + " = " + (double) numberOfOnes / numTransitions); 
////		System.exit(0);
//		
//			  	  
//	  System.exit(0);
//		
//		for (int i = 0; i < 1; i++) {
//			if (i == 0) {
//				System.out.println("  x x       x x");
//				System.out.println("x x x x   x x x x");
//				System.out.println("x x x x x x x x x");
//				System.out.println("x x x x x x x x x");
//				System.out.println("  x x x x x x x");
//				System.out.println("    x x x x x");
//				System.out.println("      x x x");
//				System.out.println("       x x");
//				System.out.println("        x");
//				System.out.println("");
//			}
//			i--;
//		}
//		System.exit(0);
//		
//		List<Double> weights = AuxiliaryTool.getStoredObject(new ArrayList<Double>(), 
//			new File("F:/PhD/Data/Files out/Neural network/gridSearch/gridSearch NOTE_TO_NOTE featureSet C/"
//			+ "lambda = 3.0E-5/Hidden nodes = -4/Fold 1/Best weights NOTE_TO_NOTE.xml"));
//		System.out.println(weights.size());
//		System.exit(0);
//		
////		List<Double> weights = AuxiliaryTool.getStoredObject(new ArrayList<Double>(), 
////				new File("F:/PhD/Data/Files out/Neural network/fourVoicePieces/lambda = 1.0E-5/Hidden nodes = -4/Fold 1/" + 
////			  "Best weights CHORD_TO_CHORD.xml"));
////			System.out.println(weights.size());
//		
////		List<Double> weights = AuxiliaryTool.getStoredObject(new ArrayList<Double>(), 
////			new File("F:/PhD/Data/Files out/Neural network/free/lambda = 3.0E-5/Hidden nodes = -4/No CV/" + 
////		  "Best weights NOTE_TO_NOTE.xml"));
////		System.out.println(weights.size());
////		
////		weights = AuxiliaryTool.getStoredObject(new ArrayList<Double>(), 
////			new File("F:/PhD/Data/Files out/Neural network/imitativeOchsenkun/lambda = 3.0E-5/Hidden nodes = -4/No CV/" + 
////		  "Best weights NOTE_TO_NOTE.xml"));
////		System.out.println(weights.size());
////		
////		weights = AuxiliaryTool.getStoredObject(new ArrayList<Double>(), 
////			new File("F:/PhD/Data/Files out/Neural network/imitativeOtherIntabulators/lambda = 3.0E-5/Hidden nodes = -4/No CV/" + 
////		  "Best weights NOTE_TO_NOTE.xml"));
////		System.out.println(weights.size());
////		
////		weights = AuxiliaryTool.getStoredObject(new ArrayList<Double>(), 
////			new File("F:/PhD/Data/Files out/Neural network/ochsenkun/lambda = 3.0E-5/Hidden nodes = -4/No CV/" + 
////		  "Best weights NOTE_TO_NOTE.xml"));
////		System.out.println(weights.size());
////		
////		weights = AuxiliaryTool.getStoredObject(new ArrayList<Double>(), 
////				new File("F:/PhD/Data/Files out/Neural network/otherIntabulators/lambda = 3.0E-5/Hidden nodes = -4/No CV/" + 
////			  "Best weights NOTE_TO_NOTE.xml"));
////		System.out.println(weights.size());
//		
//		System.exit(0);
//		
//		List<Integer> transInt = Arrays.asList(new Integer[]{null, null, null});
//		Integer tA = transInt.get(0);
//		System.out.println(tA);
//		System.exit(0);
//		
//		int highestNumberOfVoicesTraining = 3; 
//		List<Integer> predictedVoices = new ArrayList<Integer>();
//		predictedVoices.add(2);
//		predictedVoices.add(2);
//		
//		System.out.println(predictedVoices);
//		for (int i = 0; i < predictedVoices.size(); i++) {
//  	 	int currentPredictedVoice = predictedVoices.get(i);
//  	 	if (currentPredictedVoice >= highestNumberOfVoicesTraining) {
//
//  	 		predictedVoices.remove(i);
//  	  	i--;
//  	  }
//  	}	  	
//		System.out.println(predictedVoices);
//		System.exit(0);
//		
//		int pro = 1;
//				
//			if (pro != 3) {
//			  System.out.println("1e gelezen");
//			}
//			else if (pro == 3) {
//				System.out.println("2e gelezen");
//			}
//			else if (pro > 0) {
//				System.out.println("3e gelezen");
//			}
//			System.exit(0);
//		
//		
//		List<List<Integer>> pitchesAndVoices = new ArrayList<List<Integer>>();
//		pitchesAndVoices.add(Arrays.asList(new Integer[]{5, 1}));
//		pitchesAndVoices.add(Arrays.asList(new Integer[]{1, 2}));
//		pitchesAndVoices.add(Arrays.asList(new Integer[]{1, 3}));
//		pitchesAndVoices.add(Arrays.asList(new Integer[]{6, 4}));
//		for (int j = 0; j < pitchesAndVoices.size(); j++) {
//      for (int i = 1; i < pitchesAndVoices.size() - j; i++) {
//        int previousPitch = pitchesAndVoices.get(i-1).get(0);
//        int currentPitch = pitchesAndVoices.get(i).get(0);
//      	if (previousPitch > currentPitch) {
//          Collections.swap(pitchesAndVoices, i, i-1);
////        	tijdelijk = invoer[i];
////          invoer[i] = invoer[i-1];
////          invoer[i-1] = tijdelijk;
//        }
//      }
//    }
//		
//		System.out.println(pitchesAndVoices);
//		System.exit(0);
//		
//		List<Integer> pitchesOfSustainedPreviousNotes = Arrays.asList(new Integer[]{20, 10, 30, 40});
//		List<Integer> voicesOfSustainedPreviousNotes = Arrays.asList(new Integer[]{2, 1, 3, 4});
//		
//		List<List<Integer>> ll = new ArrayList<List<Integer>>();
//		ll.add(Arrays.asList(new Integer[]{20, 2}));
//		ll.add(Arrays.asList(new Integer[]{10, 1}));
//		ll.add(Arrays.asList(new Integer[]{30, 3}));
//		ll.add(Arrays.asList(new Integer[]{40, 4}));
//				
//		Collections.swap(ll, 0, 2);
//		System.out.println(ll);
//		System.exit(0);
//		
//		Collections.sort(pitchesOfSustainedPreviousNotes);
//		List<Integer> pitchesReordered = new ArrayList<Integer>();
//		List<Integer> voicesReordered = new ArrayList<Integer>();
//		while (pitchesOfSustainedPreviousNotes.size() != 0) {
//			int min = Collections.min(pitchesOfSustainedPreviousNotes);
//			int indexToRemove = pitchesOfSustainedPreviousNotes.indexOf(min);
//			pitchesReordered.add(min);
//			voicesReordered.add(voicesOfSustainedPreviousNotes.get(indexToRemove));
//			pitchesOfSustainedPreviousNotes.remove(indexToRemove);
//			voicesOfSustainedPreviousNotes.remove(indexToRemove);
//		}
//		
//		System.out.println(pitchesOfSustainedPreviousNotes);
//		System.out.println(voicesOfSustainedPreviousNotes);
//		System.out.println(pitchesReordered);
//		System.out.println(voicesReordered);
//		System.exit(0);
//		
//		
//		
//		List<Integer> l1 = Arrays.asList(new Integer[]{1, 0, 2, 0});
//		int min = Collections.min(l1);
//		System.out.println(l1.indexOf(min));
//		System.exit(0);
//		
//		List<Integer> l2 = Arrays.asList(new Integer[]{2, 3, 4});
//		List<Integer> newl = new ArrayList<Integer>(l1);
//		newl.addAll(l2);
////		System.out.println(newl);
//		
//		l1 = Arrays.asList(new Integer[]{0, 1, 1, 2});
//		l2 = new ArrayList<Integer>();
//		for (int i : l1) {
//			if (!l2.contains(i)) {
//				l2.add(i);
//			}
//		}
//		System.out.println(l2);
//		System.exit(0);
//		
//		
//		ErrorFraction e = new ErrorFraction(6, 8);
//		Rational rat0 = e.toRational();
//		System.out.println(rat0); //.mul(new Rational(1, 1)));
//		
//		Rational rat1 = new Rational(1, 2001);
//		Rational rat2 = new Rational(1, 201);
//		Rational rat3 = new Rational(1, 202);
//		Rational rat4 = new Rational(1, 203);
//		List<Rational> rlist = new ArrayList<Rational>();
//		rlist.add(rat1); rlist.add(rat2); rlist.add(rat3); rlist.add(rat4);
//		
//		Rational newr = new Rational();
//		double newd = 0.0;
//		for (Rational r : rlist) {
//			double d = (double) r.getNumer() / r.getDenom();
//			newr = newr.add(r);
////			System.out.println(newr.getNumer());
////			System.out.println(newr.getDenom());
//			System.out.println(newr);
//			System.out.println(newr.toDouble());
//			newd += d;
//			System.out.println(newd);
//		}
//		System.out.println(newr);
//		System.out.println(newd);
//		System.out.println(newr.toDouble() == newd);
//		System.exit(0);
//		
//		int a = 2147483647;
//		int b = 2;
//		double d = (double) a * b; 
//		if (d > Integer.MAX_VALUE) {
//			System.out.println("yes");
//		}
//		else {
//			System.out.println("no");
//		}
//		
////		System.out.println(inn);
////		long maxRange = 9223372036854775807L;
////		if (in > maxRange) {
////			System.out.println("yes");
////		}
////		else {
////			System.out.println("no");
////		}
////		long lo = in;
//		
//		System.exit(0);
//		
//		Rational precision = new Rational(10, 20);
//	  Rational recall = new Rational(10, 20);
//	  Rational numF1Score = precision.mul(recall).mul(2);
//	  System.out.println(numF1Score);
//	  System.out.println(precision);
//	  System.out.println(precision.getNumer());
//	  System.out.println(precision.getDenom());
//	  System.out.println(recall);
//	  System.out.println(recall.getNumer());
//	  System.out.println(recall.getDenom());
//	  System.exit(0);
////	int a = 10; int b = 20; int c = 20;
////	int d = 20; int e = 20; int f = 20;
////	Rational ra = new Rational(a+b+c, d+e+f);
////	System.out.println(ra);
////	System.out.println(ra.getNumer());
////	System.out.println(ra.getDenom());
////	System.out.println(new Rational(30, 60).isEqual(new Rational(1, 2)));
////	ra = ra.mul(new Rational(30, 50));
////	System.out.println(ra);
//	
//	Rational rationalsSummed = new Rational(0, 1);
//	List<Rational> listOfRationals = new ArrayList<Rational>();
//	listOfRationals.add(new Rational(4, 8)); listOfRationals.add(new Rational(3, 8));
//	Rational ra2 = listOfRationals.get(0);
//	System.out.println(ra2);
//  System.out.println(ra2.getNumer());
//  System.out.println(ra2.getDenom());
//  Rational ra3 = new Rational(ra2.getNumer(), ra2.getDenom());
//  System.out.println(ra3.getNumer());
//  System.out.println(ra3.getDenom());
////	for (Rational r: listOfRationals) {
////		rationalsSummed = rationalsSummed.add(r);
////	}
////	System.out.println(rationalsSummed);
//	
//	System.exit(0);
//	
//	
//	
//	
//
////		int fold = 1;
////	  String pieceName = null;
////	  switch (fold) {
////	    case 1: pieceName = "Barbetta 1582 - Il nest plaisir";
////	    break;
////	    case 2: pieceName = "Phalese 1563 - Las on peult";
////	    break;
////	    case 3: pieceName = "Abondante 1548 - mais mamignone";
////	    break;
////	    case 4: pieceName = "Ochsenkun 1558 - Herr Gott lass dich erbarmen";
////	    break;
////	    case 5: pieceName = "Phalese 1547 - Tant que uiuray [a4]";
////	    break;
////	    case 6: pieceName = "Rotta 1546 - Bramo morir per non patir piu morte";
////	    break;
////	    case 7: pieceName = "Ochsenkun 1558 - Qui habitat";
////	    break;
////	    case 8: pieceName = "Ochsenkun 1558 - In exitu Israel de Egipto";
////	    break;
////	    case 9: pieceName = "Ochsenkun 1558 - Absolon fili mi";
////	  }
//		
////	  List<String> pieceNames = new ArrayList<String>();
////	  pieceNames.add("Barbetta 1582 - Il nest plaisir");
////	  pieceNames.add("Phalese 1563 - Las on peult");
////	  pieceNames.add("Abondante 1548 - mais mamignone");
////	  pieceNames.add("Ochsenkun 1558 - Herr Gott lass dich erbarmen");
////	  pieceNames.add("Phalese 1547 - Tant que uiuray [a4]");
////	  pieceNames.add("Rotta 1546 - Bramo morir per non patir piu morte");
////	  pieceNames.add("Ochsenkun 1558 - Qui habitat");
////	  pieceNames.add("Ochsenkun 1558 - In exitu Israel de Egipto");
////	  pieceNames.add("Ochsenkun 1558 - Absolon fili mi");
//	  
//	  
////	  for (int i = 0; i < pieceNames.size(); i++) {
////	    int fold = i + 1;
////	    String pieceName = pieceNames.get(i);
////	  	
////	    String dir = "F:/PhD/Data/Files out/HMM/Reinier-HMM/Boosted Observation Probabilities/"; 
////	    String subfolder = "Outputs (keeping ones)/";
//////	  	File outputFile =	new File(dir + subfolder + 
//////	  		"1. Output (with uniform priors and transitions)/Voice assignment index fold "+ fold + ".txt");
//////  	  File outputFile = new File(dir + subfolder +
//////  		  "2. Output (with prior probability matrix and uniform transitions)/Voice assignment index fold "+ fold + ".txt");
//////	    File outputFile = new File(dir + subfolder +
//////	    	"3. Output (with uniform priors)/Voice assignment index fold "+ fold + ".txt"); // NOT OK
////  	  File outputFile = new File(dir + subfolder +
////  	  	"4. Output (with prior probability matrix)/Voice assignment index fold "+ fold + ".txt"); // NOT OK
////	     subfolder = "Outputs (setting to zero)/";
//////      File outputFile = new File(dir + subfolder +
//////	      "1. Output (with uniform priors and transitions)/Voice assignment index fold "+ fold + ".txt");
//////      File outputFile = new File(dir + subfolder +
//////        "2. Output (with prior probability matrix and uniform transitions)/Voice assignment index fold "+ fold + ".txt");
//////      File outputFile = new File(dir + subfolder +
//////	      "3. Output (with uniform priors)/Voice assignment index fold "+ fold + ".txt"); 
//////      File outputFile = new File(dir + subfolder +
//////  	    "4. Output (with prior probability matrix)/Voice assignment index fold "+ fold + ".txt"); 
////	    
////  	  String contents = AuxiliaryTool.readTextFile(outputFile); 
////  	  List<Integer> predictedIndices = AuxiliaryTool.parseStringOfIntegers(contents, "\n");
////  	  
////  	  // Subtract 1 from all predicted indices
////  	  for (int j = 0; j < predictedIndices.size(); j++) {
////  	  	int currentValue = predictedIndices.get(j);
////  	  	predictedIndices.set(j, currentValue - 1);
////  	  }
//////  	  System.out.println(predictedIndices);
////	  
////  	  // Get ground truth voice labels and voice assignment dictionary
////  	  HMMManager hMMManager = new HMMManager();
////  	  List<List<Double>> groundTruthVoiceLabels = AuxiliaryTool.getStoredObject(new ArrayList<List<Double>>(),
////  	  	new File("F:/PhD/Data/Files out/Ground truth/Ground truth voice labels " + pieceName + ".xml")); 
////  	  List<List<Integer>> voiceAssignmentDictionary = AuxiliaryTool.getStoredObject(new ArrayList<List<Integer>>(),
////  		  new File("F:/PhD/Data/Files out/HMM/voiceAssignmentDictionary.xml")); 
////
////  	  
////  	  int highestNumberOfVoices = 4;  
////  	  String evaluation = hMMManager.evaluate(predictedIndices, groundTruthVoiceLabels, 
////  	  	voiceAssignmentDictionary, highestNumberOfVoices); //, pieceName);
////  	  String tolErr = ErrorCalculatorTab.getAllowedCharactersAfterMarker(evaluation, "(tolerant) on the test set: ");
////  	  String strictErr = ErrorCalculatorTab.getAllowedCharactersAfterMarker(evaluation, "(strict) on the test set: ");
////  	  System.out.println("Fold = " + fold);
////  	  System.out.println(tolErr);
////  	  System.out.println(strictErr);
////  	  
////  	  if (fold == 1) {
////  	  	if (!tolErr.equals("146/478") || !strictErr.equals("150.5/478")) {
////  	  		System.out.println("Fold " + fold + " incorrect");
////  	  		System.exit(0);
////  	  	}
////  	  }
////  	  else if (fold == 2) {
////  	  	if (!tolErr.equals("187/777") || !strictErr.equals("193.5/777")) {
////  	  		System.out.println("Fold " + fold + " incorrect");
////  	  		System.exit(0);
////  	  	}
////  	  }
////  	  else if (fold == 3) {
////  	  	if (!tolErr.equals("178/705") || !strictErr.equals("185.5/705")) {
////  	  		System.out.println("Fold " + fold + " incorrect");
////  	  		System.exit(0);
////  	  	}
////  	  }
////  	  else if (fold == 4) {
////  	  	if (!tolErr.equals("68/371") || !strictErr.equals("70.5/371")) {
////  	  		System.out.println("Fold " + fold + " incorrect");
////  	  		System.exit(0);
////  	  	}
////  	  }
////  	  else if (fold == 5) {
////  	  	if (!tolErr.equals("66/457") || !strictErr.equals("68.0/457")) {
////  	  		System.out.println("Fold " + fold + " incorrect");
////  	  		System.exit(0);
////  	  	}
////  	  }
////  	  else if (fold == 6) {
////  	  	if (!tolErr.equals("89/708") || !strictErr.equals("93.5/708")) {
////  	  		System.out.println("Fold " + fold + " incorrect");
////  	  		System.exit(0);
////  	  	}
////  	  }
////  	  else if (fold == 7) {
////  	  	if (!tolErr.equals("651/2238") || !strictErr.equals("664.0/2238")) {
////  	  		System.out.println("Fold " + fold + " incorrect");
////  	  		System.exit(0);
////  	  	}
////  	  }
////  	  else if (fold == 8) {
////  	  	if (!tolErr.equals("470/1974") || !strictErr.equals("480.5/1974")) {
////  	  		System.out.println("Fold " + fold + " incorrect");
////  	  		System.exit(0);
////  	  	}
////  	  }
////  	  else if (fold == 9) {
////  	  	if (!tolErr.equals("364/1184") || !strictErr.equals("374.0/1184")) {
////  	  		System.out.println("Fold " + fold + " incorrect");
////  	  		System.exit(0);
////  	  	}
////  	  }
////  	  
////  	  
//////      System.out.println(evaluation);
//////      String s = 
//////      	outputFile.getAbsolutePath() + "\r\n\r\n" + "Fold " + fold + "\r\n" + pieceName + "\r\n\r\n" +
//////        "predictedIndices = " + predictedIndices + "\r\n\r\n" + evaluation;
//////     AuxiliaryTool.storeTextFile(s, new File("F:/PhD/Data/" + pieceName + ".txt"));
////	  }
////	  System.exit(0);
//
//		
//    // Test errors tolerant NN
////    List<Rational> errors = new ArrayList<Rational>();
////    errors.add(new Rational(89, 478));
////    errors.add(new Rational(91, 777));
////    errors.add(new Rational(61, 705));
////    errors.add(new Rational(46, 371));
////    errors.add(new Rational(33, 457));
////    errors.add(new Rational(46, 708));
////    errors.add(new Rational(289, 2238));
////    errors.add(new Rational(231, 1974));
////    errors.add(new Rational(139, 1184));
//    
//    // Test errors strict NN
////    List<Rational> errors = new ArrayList<Rational>();
////    errors.add(new Rational(195, 956));
////    errors.add(new Rational(204, 1554));
////    errors.add(new Rational(160, 1410));
////    errors.add(new Rational(101, 742));
////    errors.add(new Rational(72, 914));
////    errors.add(new Rational(101, 1416));
////    errors.add(new Rational(630, 4476));
////    errors.add(new Rational(513, 3948));
////    errors.add(new Rational(314, 2368));
//
//   
//    // Application errors tolerant NN
////    List<Rational> errors = new ArrayList<Rational>();
////    errors.add(new Rational(102, 478));
////    errors.add(new Rational(141, 777));
////    errors.add(new Rational(109, 705));
////    errors.add(new Rational(61, 371));
////    errors.add(new Rational(41, 457));
////    errors.add(new Rational(65, 708));
////    errors.add(new Rational(507, 2238));
////    errors.add(new Rational(441, 1974));
////    errors.add(new Rational(256, 1184));
//    
//    
//    // Application errors strict NN
////    List<Rational> errors = new ArrayList<Rational>();
////    errors.add(new Rational(220, 956));
////    errors.add(new Rational(299, 1554));
////    errors.add(new Rational(252, 1410));
////    errors.add(new Rational(132, 742));
////    errors.add(new Rational(89, 914));
////    errors.add(new Rational(140, 1416));
////    errors.add(new Rational(1071, 4476));
////    errors.add(new Rational(932, 3948));
////    errors.add(new Rational(542, 2368));
//    
//    // Application errors tolerant HMM
////    List<Rational> errors = new ArrayList<Rational>();
////    errors.add(new Rational(146, 478));
////    errors.add(new Rational(187, 777));
////    errors.add(new Rational(178, 705));
////    errors.add(new Rational(68, 371));
////    errors.add(new Rational(66, 457));
////    errors.add(new Rational(89, 708));
////    errors.add(new Rational(651, 2238));
////    errors.add(new Rational(470, 1974));
////    errors.add(new Rational(364, 1184));
//
//    // Application errors strict HMM
////    List<Rational> errors = new ArrayList<Rational>();
////    errors.add(new Rational(301, 956));
////    errors.add(new Rational(387, 1554));
////    errors.add(new Rational(371, 1410));
////    errors.add(new Rational(141, 742));
////    errors.add(new Rational(136, 914));
////    errors.add(new Rational(187, 1416));
////    errors.add(new Rational(1328, 4476));
////    errors.add(new Rational(961, 3948));
////    errors.add(new Rational(748, 2368));
//    
////    int numers = 0;
////    int denomis = 0;
////    for (Rational r: errors) {
////    	numers += r.getNumer();
////    	denomis += r.getDenom();
////    }
////    System.out.println("--> " + numers/2 + "/" + denomis/2 + " = " + (double) numers/denomis );
////    System.exit(0);
//    
////		int nums = 0;
////    int denoms = 0;
////    for (Rational r : errors) {
////    	nums += r.getNumer();
////	    denoms += r.getDenom(); 
////    }
////    Rational mu = new Rational(nums, denoms);
////    System.out.println("mu = " + mu);
////
////    Rational sum = new Rational(0, 1);
////    for (Rational x : errors) {
////	    Rational value = x.sub(mu);
////	    value = value.mul(value);
////	    sum = sum.add(value);
////    }
////    System.out.println("sum = " + sum);
////    double sumAsDouble = sum.toDouble();
////    sumAsDouble = sumAsDouble/errors.size();
////    double outcome = Math.sqrt(sumAsDouble);
////    outcome *= 100;
////    System.out.println("stdev = " + outcome);
////    System.exit(0);
//
////for (Rational r : applicationErrorsStrict) {
////	nums += r.getNumer();
////	denoms += r.getDenom();
////}
////System.out.println(nums + "/" + denoms);
////System.exit(0);
//		
//		
//		
////		Rational rr1 = new Rational(0, 1);
////		Rational rr2 = new Rational(1, 2);
////		Rational rr3 = r1.add(rr2);
////		System.out.println(rr3);
////		System.exit(0);
//		
////		String name = "Bach - WTC1, Fuga 1 in C major (BWV 846)";
////		String name = "Bach - WTC1, Fuga 5 in D major (BWV 850)";
////		String name = "Bach - WTC1, Fuga 12 in f minor (BWV 857)";
////		String name = "Bach - WTC1, Fuga 14 in f# minor (BWV 859)";
//		String name = "Bach - WTC1, Fuga 16 in g minor (BWV 861)";
//		
//		File midiFile = new File(TrainingManager.prefix + "MIDI/WTC/Four-voice fugues/" + name);	
////		File encodingFile = new File(TrainingManager.prefix + "Encodings/Tests/Testpiece 1.txt");
//		MidiImport mid = new MidiImport();
//		Transcription tr = new Transcription(midiFile, null); 
////		Tablature tablature = new Tablature(encodingFile);
//		Preprocessor pp = new Preprocessor();
////		pp.preprocess(null, tr, false, 0);
//		
//		NotationSystem system = tr.getPiece().getScore();
//		for (int i = 0; i < 4; i++) {
//			System.out.println("Checking voice " + i);
//			NotationStaff staff = system.get(i);
//	    NotationVoice voice = staff.get(0);  
//	    for (int j = 0; j < voice.size(); j++) {
//	    	NotationChord notationChord = voice.get(j);
//	    	Note firstNote = notationChord.get(0);
//        if (notationChord.size() > 1) {
//        	System.out.println("--> two notes at at onset time " + firstNote.getMetricTime() + "in voice " + i);
//        	System.exit(0);
//	      }
//	    }
//      System.out.println("voice OK");
//		}
//		System.exit(0);
//		
////		Integer[][] bOP = tablature.getBasicTabSymbolProperties();
////		for(Integer[] i : bOP) {
////			System.out.println("pitch = " + i[Tablature.PITCH]);
////			System.out.println("vert pos = " + i[Tablature.NOTE_SEQ_NUM]);
////		}
//		
//		
////		tr.visualise("");
//		
//		
////		tr.setInitialNoteSequence();
////		tr.setInitialVoiceLabels();
////		List<List<Double>> labels = tr.getMostRecentVoiceLabels();
////		for (List<Double> label : labels) {
////			System.out.println(label);
////		}
//    
//		Note currentNote = tr.getPiece().getScore().get(1).get(0).get(0).get(0);
//		
//		NotationSystem ns = tr.getPiece().getScore();
//		for (int currVoice = 0; currVoice < 5; currVoice++) {
//		  NotationStaff nst = ns.get(currVoice);
//		  NotationVoice nv = nst.get(0);	
//		  for (NotationChord noCh : nv) {
//			  if (noCh.contains(currentNote)) {
//				  System.out.println("YES");  
//			  }
//		  }
//		}
//		
////		System.out.println(nv.size());
////		NotationChord nch = nv.get(3);
////		System.out.println("nc.size() = " + nch.size());
////		Note no = nch.get(0);
////		System.out.println("pitch = " + no.getMidiPitch());
////		System.out.println("metricTime = " + no.getMetricTime());
////		System.out.println("metricDuration = " + no.getMetricDuration());
//		
//		System.exit(0);
//		
//		
//
////		Transcription tr = mid.importMidiFile(new 
////			File("F:/PhD/Papers, posters, presentations/Paper Musicae Scientiae/MIDI/Bach/bach-0846/midi1/02.mid"));
////				File("F:/PhD/Data/MIDI/Dataset/Ochsenkun 1558 - Herr Gott lass dich erbarmen/Ochsenkun 1558 - Herr Gott SA mm. 15-17.mid"));
////		tr.visualise("");
//
//    NotationVoice voice = tr.getPiece().getScore().get(0).get(0);
//    List<Note> noteList = new ArrayList<Note>();
//    for (NotationChord nc : voice) {
//    	for (Note n : nc) {
//    		noteList.add(n);
//    	}
//    }
//    
//    // List the onset times
//    List<Rational> metricTimes = new ArrayList<Rational>();
//    for (Note n : noteList) {
//    	Rational currentMetricTiime = n.getMetricTime();
//    	if (!metricTimes.contains(currentMetricTiime)) {
//    		metricTimes.add(currentMetricTiime);
//    	}
//    }
//    
//    // Group the Notes per onset time (i.e., per chord)
//    List<List<Note>> allChords = new ArrayList<List<Note>>();
//    for (Rational r : metricTimes) {
//    	List<Note> currentChord = new ArrayList<Note>();
//    	for (Note n : noteList) {
//    		if (n.getMetricTime().equals(r)) {
//    			currentChord.add(n);
//    		}
//    	}
//    	allChords.add(currentChord);
//    }
//    System.out.println("number of chords:" + allChords.size());
//    for (List<Note> l : allChords) {
//    	for (Note n : l) {
//    		System.out.println(n.getMidiPitch());
//    	}
//    	System.out.println();
//    }
//    
//    
//    
////    List<List<Note>> allChords = new ArrayList<List<Note>>();
////    for (int i = 0; i < noteList.size(); i++) {
////    	List<Rational> metricTimesAlreadySeen = new ArrayList<Rational>();
////    	Note currentNote = noteList.get(i);
////    	List<Note> currentChord = new ArrayList<Note>();
////    	currentChord.add(currentNote);
////    	Rational currentMetricTime = currentNote.getMetricTime();
////    	// Add all other notes with the same MetricTime to currentChord, but only if the Note's MetricTime has not been
////    	// checked before
////    	for (int j = 0; j < noteList.size(); j++) {
////    		if (j != i) {
////    			Note otherNote = noteList.get(j);
////    			Rational otherMetricTime = otherNote.getMetricTime();
////    			if (!metricTimesAlreadySeen.contains(currentMetricTime)) {
////    				
////    				if (otherMetricTime.equals(currentMetricTime)) {
////    				  currentChord.add(otherNote);
////    			  }
////    			}
////    			else {
////    				System.out.println("IF");
////    			}
////    		}
////    		metricTimesAlreadySeen.add(currentMetricTime);
//////    		// Sort numerically
//////    		if (currentChord.size() > 1) {
//////    			int firstMidiPitch = currentChord.get(0).getMidiPitch();
//////    		  for (int k = 1; k < currentChord.size(); k++) {
//////    			  Note currNote = currentChord.get(k);
//////    		  	if (currNote.getMidiPitch() < firstMidiPitch) {
//////    			    currentChord.set(0, currNote);	
//////    			  }
////////    		  	break;
//////    		  }
//////    		}
////    		
////    	}
////    	allChords.add(currentChord);
////    }
////    
////    System.out.println("allChords.size() = " + allChords.size());
////  	
////  	for (List<Note> l : allChords) {
////  	  System.out.println(l.size());
////  	  for (Note n : l) {
////  	  	System.out.println(n.getMidiPitch());
////  	  }
////  	}
//    
//    
//    
////    List<List<Note>> chordList = new ArrayList<List<Note>>();
////    for (Note n : noteList) {
////    	List<Note> currentChord = new ArrayList<Note>();
////    	currentChord.
////    }
//    
//    
////    for (int i = 0; i < currentVoice.size(); i++) {
////      NotationChord nc = currentVoice.get(i);
////      Rational mt = nc.getMetricTime();
////      double mtDouble = mt.toDouble() + 1;
////      System.out.println("i = " + i);
////      System.out.println("    metricTime = " + mtDouble);
////      System.out.println("    notationchord size = " + nc.size());
////      for (int j = 0; j < nc.size(); j++) {
////      	Note n = nc.get(j);
////      	System.out.println("        note no. " + j + "; pitch = " + n.getMidiPitch() + "; duration = " + n.getMetricDuration());
////      }
////    }
//		
//		
//		TiedChord tic = null;
//		
////		System.exit(0);
//		
////		double dd = 0.12307;
////		DecimalFormat df = new DecimalFormat("0.0000");
////		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.UK);
////		otherSymbols.setDecimalSeparator('.'); 
////		df.setDecimalFormatSymbols(otherSymbols);
////		
////		df.setRoundingMode(RoundingMode.HALF_UP);
////		
////		
////		String dds = df.format(dd);
////		System.out.println(dds);
////		System.exit(0);
////		
////		double[][] matrix = new double[3][3];
////		double num1 = Math.sqrt(2.0);
////		double num2 = Math.sqrt(3.0);
////		double num3 = Math.sqrt(5.0);
////		Arrays.fill(matrix[0], num1);
////		Arrays.fill(matrix[1], num2);
////		Arrays.fill(matrix[2], num3);
////		
////		
////		
////		
////		
////		for (double[] row : matrix) {
////      for (double d : row) {
////      	
////      }
////		}
////		
////	
////		
////		System.exit(0);
////		
////		
////		
////		
////		List<Double> weights = AuxiliaryTool.getStoredObject(new ArrayList<Double>(), 
////			new File("F:/PhD/Data/Files out/regularisation/regularisation CHORD_TO_CHORD/" + 
////		  "lambda = 0.03/Hidden nodes = -4/Fold 6/Best weights CHORD_TO_CHORD.xml"));
////		System.out.println("lengte = " + weights.size());
////		
////		
////		
////		System.exit(0);
////		
////		boolean useCV = true; 
////		boolean isTP = true;
////		System.out.println(!(useCV == true && isTP == true));
////		System.out.println(!(useCV == true && isTP == false));
////		System.out.println(!(useCV == false && isTP == true));
////		System.out.println(!(useCV == false && isTP == false));
////				
////		int a = 6;
////		int b = 5;
////		int c = a/b; 
////		System.out.println(c);
////		
////		
////		System.exit(0);
////		
////		MidiImport mi = new MidiImport();
////	  Preprocessor pp = new Preprocessor(); 
////	  FeatureGenerator fg = new FeatureGenerator();
////	  DataConverter dc = new DataConverterTab();
////	  ErrorCalculator ec = new ErrorCalculatorTab();
////	  
////	  MidiImport midiImport = new MidiImport(); 
////		Preprocessor preprocessor = new Preprocessor();
////		
////		List<String> pieceNames = new ArrayList<String>();
//////		pieceNames.add("Abondante 1548 - mais mamignone");
//////		pieceNames.add("Adriansen 1584 - D'Vn si bel foco");
//////		pieceNames.add("Barbetta 1582 - Il nest plaisir");
//////		pieceNames.add("Judenkunig 1523 - Elslein liebes Elslein");
//////		pieceNames.add("Newsidler 1536 - Disant adiu madame");
//////		pieceNames.add("Newsidler 1544 - Nun volget Lalafete");
//////		pieceNames.add("Ochsenkun 1558 - Absolon fili mi");
//////		pieceNames.add("Ochsenkun 1558 - Cum Sancto spiritu");
////		pieceNames.add("Ochsenkun 1558 - Herr Gott lass dich erbarmen");
//////		pieceNames.add("Ochsenkun 1558 - In exitu Israel de Egipto");
//////		pieceNames.add("Ochsenkun 1558 - Inuiolata integra");
//////		pieceNames.add("Ochsenkun 1558 - Qui habitat");
//////		pieceNames.add("Phalese 1547 - Tant que uiuray [a3]");
//////		pieceNames.add("Phalese 1547 - Tant que uiuray [a4]");
//////		pieceNames.add("Phalese 1563 - Las on peult");
//////		pieceNames.add("Pisador 1552 - Pleni de la missa misma");
//////		pieceNames.add("Rotta 1546 - Bramo morir per non patir piu morte");
//////		pieceNames.add("Testpiece 1");
////		
////    for (int i = 0; i < pieceNames.size(); i++) {    
////      String currentPieceName = pieceNames.get(i);
////    	File encodingFile = new File(TrainingManager.prefix + "Encodings/Dataset/" + currentPieceName + ".txt");
////      File midiFile = new File(TrainingManager.prefix + "MIDI/Dataset/" + currentPieceName);
////      File voiceLabelsFile = new File(TrainingManager.prefix + "Files out/Ground truth/Ground truth voice labels " + currentPieceName + ".xml");
////      File chordVoiceLabelsFile = new File(TrainingManager.prefix + "Files out/Ground truth/Ground truth chord voice labels " + 
////        currentPieceName + ".xml");
////    
////      // Make a Tablature and Transcription and preprocess them
////	    Tablature tablature = new Tablature(encodingFile);
////      Transcription transcription = midiImport.importMidiFiles(midiFile);
////      preprocessor.preprocess(tablature, transcription, true);
////      
////      List<List<TabSymbol>> tabChords = tablature.getTablatureChords();
////      List<List<Integer>> onsetIndices = new ArrayList<List<Integer>>();
////      int first = 0;
////      int last = 0;
////      for (List<TabSymbol> chord : tabChords) {
////      	last = first + chord.size() - 1;
////      	onsetIndices.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{first, last})));
////      	first = last + 1;
////      }
////      for (int j = 0; j < onsetIndices.size(); j++) {
////      	System.out.println("chord = " + j + "; indices = " + onsetIndices.get(j));
////      }
////      
////      List<List<Integer>> voiceAssignmentDictionary = AuxiliaryTool.getStoredObject(new ArrayList<List<Integer>>(), 
////      	new File("F:/PhD/Data/Files out/HMM/voiceAssignmentDictionary.xml"));
////      List<List<Integer>> voiceAssignments = transcription.getVoiceAssignments(tablature, 4);
////      List<Integer> indices = new ArrayList<Integer>();
////      for (List<Integer> voiceAss : voiceAssignments) {
////      	indices.add(voiceAssignmentDictionary.indexOf(voiceAss));
////      }
////      System.out.println(indices);
////    }
////    System.exit(0);
////    
////	  // Classification errors tolerant NN
//////	  List<Rational> errors = new ArrayList<Rational>();
//////	  errors.add(new Rational(86, 478));
//////	  errors.add(new Rational(85, 777));
//////	  errors.add(new Rational(67, 705));
//////	  errors.add(new Rational(39, 371));
//////	  errors.add(new Rational(35, 457));
//////	  errors.add(new Rational(36, 708));
//////	  errors.add(new Rational(289, 2238));
//////	  errors.add(new Rational(219, 1974));
//////	  errors.add(new Rational(133, 1184));
////	  
//////	  errors.add(new Rational(3, 8));
//////	  errors.add(new Rational(2, 7));
//////	  errors.add(new Rational(6, 9));
////	  
////	  // Classification errors strict NN
//////	  List<Rational> errors = new ArrayList<Rational>();
//////	  errors.add(new Rational(101, 478));
//////	  errors.add(new Rational(109, 777));
//////	  errors.add(new Rational(103, 705));
//////	  errors.add(new Rational(49, 371));
//////	  errors.add(new Rational(42, 457));
//////	  errors.add(new Rational(50, 708));
//////	  errors.add(new Rational(339, 2238));
//////	  errors.add(new Rational(269, 1974));
//////	  errors.add(new Rational(166, 1184));
//////	  
////	  // Application errors tolerant NN
//////	  List<Rational> errors = new ArrayList<Rational>();
//////	  errors.add(new Rational(93, 478));
//////	  errors.add(new Rational(140, 777));
//////	  errors.add(new Rational(127, 705));
//////	  errors.add(new Rational(60, 371));
//////	  errors.add(new Rational(48, 457));
//////	  errors.add(new Rational(57, 708));
//////	  errors.add(new Rational(577, 2238));
//////	  errors.add(new Rational(410, 1974));
//////	  errors.add(new Rational(216, 1184));
//////	  
////	  // Application errors strict NN
//////	  List<Rational> errors = new ArrayList<Rational>();
//////	  errors.add(new Rational(114, 478));
//////	  errors.add(new Rational(159, 777));
//////	  errors.add(new Rational(161, 705));
//////	  errors.add(new Rational(64, 371));
//////	  errors.add(new Rational(54, 457));
//////	  errors.add(new Rational(73, 708));
//////	  errors.add(new Rational(627, 2238));
//////	  errors.add(new Rational(447, 1974));
//////	  errors.add(new Rational(251, 1184));
////	  
////	  // Test errors tolerant HMM
//////	  List<Rational> errors = new ArrayList<Rational>();
//////	  errors.add(new Rational(146, 478));
//////	  errors.add(new Rational(187, 777));
//////	  errors.add(new Rational(178, 705));
//////	  errors.add(new Rational(68, 371));
//////	  errors.add(new Rational(66, 457));
//////	  errors.add(new Rational(89, 708));
//////	  errors.add(new Rational(651, 2238));
//////	  errors.add(new Rational(470, 1974));
//////	  errors.add(new Rational(364, 1184));
////	  
////	  // Test errors strict HMM
////		List<Rational> errors = new ArrayList<Rational>();
////		errors.add(new Rational(155, 478));
////		errors.add(new Rational(200, 777));
////		errors.add(new Rational(193, 705));
////		errors.add(new Rational(73, 371));
////		errors.add(new Rational(70, 457));
////		errors.add(new Rational(98, 708));
////		errors.add(new Rational(677, 2238));
////		errors.add(new Rational(491, 1974));
////		errors.add(new Rational(384, 1184));
////	  
////	  int nums = 0;
////	  int denoms = 0;
////	  for (Rational r : errors) {
////	  	nums += r.getNumer();
////	  	denoms += r.getDenom();
////	  }
////	  Rational mu = new Rational(nums, denoms);
////	  System.out.println("mu = " + mu);
////	  
////	  Rational sum = new Rational(0, 1);
////	  for (Rational x : errors) {
////	  	Rational value = x.sub(mu);
////	  	value = value.mul(value);
////	  	sum = sum.add(value);
////	  }
////	  System.out.println("sum = " + sum);
////	  double sumAsDouble = sum.toDouble();
////	  sumAsDouble = sumAsDouble/errors.size();
////	  double outcome = Math.sqrt(sumAsDouble);
////	  System.out.println("stdev = " + outcome);
////	  System.exit(0);
////	  
//////	  for (Rational r : applicationErrorsStrict) {
//////	  	nums += r.getNumer();
//////	  	denoms += r.getDenom();
//////	  }
////	  System.out.println(nums + "/" + denoms);
////    System.exit(0);
////    
////    
////    
//// HIER GEKNIPT	  
////	  
////	  // Problem: the HMM often predicts voice assignments in which fewer/more onsets are assigned than 
////		// there are actually onsets in the chord.
////	  
////	      
////	  System.out.println("Maximum number of voices = 4");
////    List<List<Integer>> chordOnsetProperties = new ArrayList<List<Integer>>();
////    chordOnsetProperties.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{10})));
////    
////    List<List<Integer>> allVaPoss = new ArrayList<List<Integer>>();
////    List<List<Integer>> vaPoss = fg.enumerateVoiceAssignmentPossibilitiesForChordNEW(4, chordOnsetProperties);
////    allVaPoss.addAll(vaPoss);
////    System.out.println("All possible voice assignments for a 1-note chord (" + vaPoss.size() + "):");
////	  for(List<Integer> l : vaPoss) {
////	  	System.out.println(l);
////	  }
////    
////	  chordOnsetProperties.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{20})));
////	  vaPoss = fg.enumerateVoiceAssignmentPossibilitiesForChordNEW(4, chordOnsetProperties);
////	  allVaPoss.addAll(vaPoss);
////	  System.out.println("All possible voice assignments for a 2-note chord (" + vaPoss.size() + "):");
////	  for(List<Integer> l : vaPoss) {
////	  	System.out.println(l);
////	  }
////	  
////	  chordOnsetProperties.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{30})));
////	  vaPoss = fg.enumerateVoiceAssignmentPossibilitiesForChordNEW(4, chordOnsetProperties);
////	  allVaPoss.addAll(vaPoss);
////	  System.out.println("All possible voice assignments for a 3-note chord (" + vaPoss.size() + "):");
////	  for(List<Integer> l : vaPoss) {
////	  	System.out.println(l);
////	  }
////	  
////	  chordOnsetProperties.add(new ArrayList<Integer>(Arrays.asList(new Integer[]{30})));
////	  vaPoss = fg.enumerateVoiceAssignmentPossibilitiesForChordNEW(4, chordOnsetProperties);
////	  allVaPoss.addAll(vaPoss);
////	  System.out.println("All possible voice assignments for a 4-note chord (" + vaPoss.size() + "):");
////	  for(List<Integer> l : vaPoss) {
////	  	System.out.println(l);
////	  }
////	  System.out.println(allVaPoss.size());
	 
	}
	
	
	private static void xmlToSer(String hlf, String lmb, String eps) {
				
		String fldr = 
			"F:/research/data/experiments/thesis/prl_1.2/tab-int/4vv/C/eps=" + eps + 
			 "/fold_";
		
//		String nOrC = "NOTE_TO_NOTE";
		String nOrC = "CHORD_TO_CHORD";
		int numPieces = 9;
		
//		String a = "/Initial weights " + nOrC + " run 0.xml";
		String a = "/Best weights " + nOrC + ".xml";
//		String b = "/init_weights";
		String b = "/best_weights";
		
		for (int i = 1; i <= numPieces; i++) {
			File f;
			String istr = "" + i;
			if (i < 10) {
				istr = "0" + i;
			}
			// Rename
			f = new File(fldr + istr + a);
			Path source = f.toPath();
			try {
			     Files.move(source, source.resolveSibling(fldr + istr + b + ".xml"));
			} catch (IOException e) {
			     e.printStackTrace();
			}
			// Serialise differently
			f = new File(fldr + istr + b + ".xml");
			List<Double> iw = ToolBox.getStoredObject(new ArrayList<Double>(), f);
			File f2 = new File(fldr + istr + b + ".ser");
			ToolBox.storeObjectBinary(iw, f2);
		}
	}
	
	private static void combineWeightLists(String hlf, String lmb, String eps) {
		
//		String path = "F:/research/data/experiments/thesis/exp_3.3.2/tab-int/4vv/B_prime_star/fold_";
		
		String path = 
			"F:/research/data/experiments/thesis/prl_1.2/tab-int/4vv/C/eps=" + eps + 
			"/fold_";
		int numPieces = 9;
		
		for (int i = 1; i <= numPieces; i++) {
			String istr = "" + i;
			if (i < 10) {
				istr = "0" + i;
			}
			List<List<Double>> both = new ArrayList<List<Double>>();
			both.add(ToolBox.getStoredObjectBinary(new ArrayList<Double>(), 
				new File(path + istr +"/" + "init_weights.ser")));
			both.add(ToolBox.getStoredObjectBinary(new ArrayList<Double>(), 
				new File(path + istr +"/" + "best_weights.ser")));
			ToolBox.storeObjectBinary(both, new File(path + istr + "/" + Runner.weights + ".ser"));
		}
	}
	
	private static void getPValue() {
				
		double[] x = new double[]{
				0.985250737,
				0.980692788,
				0.983000739,
				0.983183183,
				0.982142857,
				0.993326978,
				0.987270156,
				0.969866071,
				0.983408748,
				0.97578426,
				0.987736901,
				0.977102415,
				0.98880597,
				0.983660131,
				0.977220957,
				0.997674419,
				0.978021978,
				0.98225602,
				0.986413043
		};
		double[] y = new double[]{
				0.985988201,
				0.971607041,
				0.977827051,
				0.966366366,
				0.977040816,
				0.987607245,
				0.984441301,
				0.962053571,
				0.978883861,
				0.967528894,
				0.987736901,
				0.970024979,
				0.983830846,
				0.980392157,
				0.972665148,
				0.994186047,
				0.973214286,
				0.969581749,
				0.975543478
		};
		System.out.println(x.length);
		System.out.println(y.length);
	    System.out.println(ToolBox.sig(x, y));
	    System.out.println(ToolBox.sig(y, x));
	}
	
	
	private static void getRunTimes() {
		String pre = "F:/PhD/data/results/NN/EuroMAC 2014/";
//		String lap = "NOTE_TO_NOTE";
		String lap = "CHORD_TO_CHORD";
//		String folderName = pre + "N2N/Bach/4vv met alrOcc = -1/lambda = 3.0E-5/Hidden nodes = -2/";
		String folderName = pre + "C2C/Bach/C2C4 - 4vv (max 4vv) met alrOcc = -1/";
//		String folderName = pre + "C2C/Bach/C2C5 - 4vv (max 5vv) met alrOcc = -1/";
//		String folderName = pre + "C2C/Bach/C2CA4 - 4vv (max 4vv) (avg prox) met alrOcc = -1/";
//		String folderName = pre + "C2C/Bach/C2CA5 - 4vv (max 5vv) (avg prox) met alrOcc = -1/";
		
		int trainStart = -1;
		int trainEnd = -1;
		int totals = 0;
		for (int i = 4; i <= 19; i++) {
			System.out.println("Fold = " + i);
			String str = "fold=" + i + "/";
			if (lap.equals("CHORD_TO_CHORD")) {
				str = "eps=0.05/lmbd=1.0E-5/HL=-4/fold=" + i + "/";
			}
			String contents = null;
			File f =  new File(folderName + str);
			String[] list = f.list();
			for (String s : list) {
//				if (s.startsWith("Test")) {
				if (s.startsWith("Training results")) {
					contents = ToolBox.readTextFile(new File(folderName + str + s));
				}
			}
			
//			File f = new File(folderName + str + "Training results " + lap + ".txt");
//			contents = AuxiliaryTool.readTextFile(f);
			int begin = contents.indexOf("Training started on ") + "Training started on ".length();
			int end = contents.indexOf("\r\n", begin);
			String beginTime = contents.substring(begin, end);
			System.out.println(beginTime);
			
			int begin2 = contents.indexOf("Training completed on ") + "Training completed on ".length();
			int end2 = contents.indexOf("\r\n", begin2);
			String endTime = contents.substring(begin2, end2);
			System.out.println(endTime);
			if (i == 19) {
//				System.exit(0);
			}
			String first = beginTime.split(", ")[1];
			String last = endTime.split(", ")[1];
			System.out.println(first);
			System.out.println(last);
			int hourB = Integer.parseInt(first.split(":")[0]);
			int minB = Integer.parseInt(first.split(":")[1]);
			int secB = Integer.parseInt(first.split(":")[2]);
//			System.out.println(hourB);
//			System.out.println(minB);
//			System.out.println(secB);
			int beginFinal = (hourB * 3600) + (minB * 60) + secB;
			
			int hourE = Integer.parseInt(last.split(":")[0]);
			int minE = Integer.parseInt(last.split(":")[1]);
			int secE = Integer.parseInt(last.split(":")[2]);
//			System.out.println(hourE);
//			System.out.println(minE);
//			System.out.println(secE);
			int endFinal = (hourE * 3600) + (minE * 60) + secE;
			
			int total = endFinal - beginFinal;
			totals += total;
			
//			System.out.println(endFinal - beginFinal);
//			System.exit(0);
			
			if (i == 1) {
				trainStart = beginFinal;
			}
			if (i == 19) {
				trainEnd = endFinal;
			}
			
		}
		System.out.println(trainStart);
		System.out.println(trainEnd);
		System.out.println("TOTAL = " + (trainEnd - trainStart));
		System.out.println("totals = " + totals);
	}
	
	
	private static void searchPiece() {
		ToolBox at = new ToolBox();
//		String currentPieceName = "Newsidler 1536 - Disant adiu madame"; 
//		String currentPieceName = "Newsidler 1536 - Mess pensees"; 
//		String currentPieceName = "Pisador 1552 - Pleni de la missa misma";
//		String currentPieceName = "Judenkunig 1523 - Elslein liebes Elslein";
//		String currentPieceName = "Newsidler 1544 - Nun volget Lalafete"; 
//		String currentPieceName = "Phalese 1547 - Tant que uiuray [a3]";
		
		String currentPieceName = "Ochsenkun 1558 - Absolon fili mi (shortened)";
//		String currentPieceName = "Ochsenkun 1558 - In exitu Israel de Egipto (shortened)";
//		String currentPieceName = "Ochsenkun 1558 - Qui habitat (shortened)";
//		String currentPieceName = "Rotta 1546 - Bramo morir per non patir piu morte";
//		String currentPieceName = "Phalese 1547 - Tant que uiuray [a4]"; 
//		String currentPieceName = "Ochsenkun 1558 - Herr Gott lass dich erbarmen";
//		String currentPieceName = "Abondante 1548 - mais mamignone"; 
//		String currentPieceName = "Phalese 1563 - LAs on peult";
//		String currentPieceName = "Barbetta 1582 - Il nest plaisir";
//		String currentPieceName = "Phalese 1563 - Il estoit vne filette";
		
		File argMidiFile = new File("F/PhD/data/" + "MIDI/tab/4vv/" + currentPieceName);
		File argEncodingFile = new File("F/PhD/data/" + "encodings/dataset/4vv/" + currentPieceName + ".txt");
		
		Tablature tab = new Tablature(argEncodingFile, true); 
		Transcription trans = new Transcription(argMidiFile, null);
//		trans.transposeNonTab(-2);
		
		Integer[][] bnp = trans.getBasicNoteProperties();
		int numB = 0; // 35, 47, 59, 71, 83
		int numBb = 0; // 34, 46, 58, 70, 82
		int numE = 0; // 40, 52, 64, 76, 88
		int numEb = 0; // 39, 51, 63, 75, 87
		for (Integer[] in : bnp) {
			int p = in[Transcription.PITCH];
			if (p == 35 || p == 47 || p == 59 || p == 71 || p == 83) {
				numB++;
			}
			if (p == 34 || p == 46 || p == 58 || p == 70 || p == 82) {
				numBb++;
			}
			if (p == 40 || p == 52 || p == 64 || p == 76 || p == 88) {
				numE++;
			}
			if (p == 39 || p == 51 || p == 63 || p == 75 || p == 87) {
				numEb++;
			}
		}
		System.out.println("Number of B  = " + numB);
		System.out.println("Number of Bb = " + numBb);
		System.out.println("Number of E  = " + numE);
		System.out.println("Number of Eb = " + numEb);
				
		System.exit(0);
		
		
		
		FeatureGenerator fg = new FeatureGenerator();
		double[][] d = null; 
//			fg.getPitchAndTimeProximitiesToAllVoices(tab.getBasicTabSymbolProperties(), trans, 
//			trans.getNoteSequence().get(0), FeatureGenerator.Direction.LEFT);
		System.out.println(trans.getPiece().getScore().size());
		double[] dd = fg.getProximitiesAndMovementToVoice(tab.getBasicTabSymbolProperties(), trans.getPiece().getScore().get(2).get(0), 
			trans.getNoteSequence().get(15), FeatureGenerator.Direction.LEFT);
//		System.out.println(Arrays.toString(dd));
		FeatureGeneratorChord fvc = new FeatureGeneratorChord();
		List<Double> chordFV = fvc.generateChordFeatureVectorDISS(tab.getBasicTabSymbolProperties(), null,
			trans, tab.getMeterInfo(), 2, Arrays.asList(new Integer[]{2, 1, 0, -1}));
		System.out.println(chordFV);
		
		System.exit(0);
		
		// Check for duration longer than 1/2
//		for (Integer[] in : trans.getBasicNoteProperties()) {
//			Rational dur = new Rational(in[Transcription.DURATION_NUMER], in[Transcription.DURATION_DENOM]);
//			if (dur.isGreater(new Rational(1, 2))) {
//				System.out.println(new Rational(in[Transcription.ONSET_TIME_NUMER], in[Transcription.ONSET_TIME_DENOM]).add(new Rational(1,1)));
//				System.out.println(dur);
//			}
//		}
//		System.exit(0);
		
		List<List<Double>> dl = trans.getDurationLabels();
		
		List<List<Double>> predDurationLabels = at.getStoredObject(new ArrayList<List<Double>>(), 
			new File("F/PhD/data" + "results/Predicted duration labels/Predicted duration labels " + 
			currentPieceName + ".xml"));
		
		List<Integer> bwdMap = null; //Transcription.getBackwardsMapping(trans.getNumberOfNewNotesPerChord());
//		System.out.println(bwdMap);
		
		Integer[][] btp = tab.getBasicTabSymbolProperties();
		int lastChordIndex = btp[btp.length - 1][Tablature.CHORD_SEQ_NUM];
	
		List<Integer> list = new ArrayList<Integer>();
		int count = 0;
		for (int i = 0; i < bwdMap.size(); i++) {
			int fwdIndex = bwdMap.get(i);
			List<Double> gtDl = dl.get(fwdIndex);
			List<Double> predDl = predDurationLabels.get(fwdIndex);
			Rational[] gtDur = DataConverter.convertIntoDuration(gtDl);
			Rational[] predDur = DataConverter.convertIntoDuration(predDl);
			
			if (!gtDl.equals(predDl)) {
				count++;
				// Get smallest GT duration
				Rational smallestGT = gtDur[0];
				if (gtDur.length == 2) {
					if (gtDur[1].isLess(gtDur[0])) {
						smallestGT = gtDur[1];
					}
				}
				
				// Compare
				if (predDur[0].isGreater(smallestGT)) {
					// Add only if it is not a note in the final chord
					if (btp[fwdIndex][Tablature.CHORD_SEQ_NUM] != lastChordIndex) {
						list.add(i);
					}
				}
				
			}
		}
		System.out.println(currentPieceName);
		System.out.println("  " + list.size() + " of " + count + " incorrectly predicted durs (from a total of " +
			btp.length + " notes) have a pred dur that is longer than the GT dur");
		System.exit(0);
		
		for (int i = 0; i < dl.size(); i++) {
			List<Double> gtDl = dl.get(i);
			List<Double> predDl = predDurationLabels.get(i);
//			
			
			
			
//			if (Collections.frequency(d, 1.0) > 1) {
//				System.out.println(i);
//			}
			Rational[] durations = DataConverter.convertIntoDuration(gtDl); 
			String toPrint = "" + durations[0];
			if (durations.length > 1) {
				toPrint = toPrint.concat(" " + durations[1]);
			}
			System.out.println(toPrint);
			
		}
		System.exit(0);
		
		List<List<Double>> predVoiceLabels = ToolBox.getStoredObject(new ArrayList<List<Double>>(),
			new File("F/PhD/data" + "results/Predicted voice labels/Predicted voice labels " + 
			currentPieceName + ".xml"));
//		List<List<Double>> predDurationLabels = AuxiliaryTool.getStoredObject(new ArrayList<List<Double>>(), 
//			new File(ExperimentRunner.pathPrefix + "Files out/Predicted duration labels/Predicted duration labels " + 
//			currentPieceName + ".xml"));
		List<Integer[]> argEDUInfo = null;
		Integer[][] currBtp = tab.getBasicTabSymbolProperties();
		Piece argPiece = 
			Transcription.createPiece(currBtp, null, predVoiceLabels, predDurationLabels, 3, null, null);

		Transcription predictedTranscription = null; 
//			new Transcription(argMidiFile, argEncodingFile, argPiece, 
//			predVoiceLabels, predDurationLabels); // HIERO

		List<List<Double>> actualVoiceLabels = trans.getVoiceLabels();
		List<List<Double>> actualDurationLabels = trans.getDurationLabels();
		List<Integer[]> actualVoicesCoD = trans.getVoicesCoDNotes();
		
		List<List<Double>> predictedVoiceLabels = predictedTranscription.getVoiceLabels();
		List<List<Double>> predictedDurationLabels = predictedTranscription.getDurationLabels();
		List<Integer[]> predictedVoicesCoD = predictedTranscription.getVoicesCoDNotes();

//		System.out.println(actualVoicesCoD.get(130)[0]);
//		System.out.println(actualVoicesCoD.get(130)[1]);
//		System.out.println(predictedVoicesCoD.get(130));
//		System.out.println(predictedVoicesCoD.get(63)[1]);
//		System.exit(0);
		
		for (int i = 0; i < actualVoiceLabels.size(); i++) {
//			if (!actualDurationLabels.get(i).equals(predictedDurationLabels.get(i))) {
//				System.out.println(i);
//			}
			if ((actualVoicesCoD.get(i) == null && predictedVoicesCoD.get(i) != null) ||
				(actualVoicesCoD.get(i) != null && predictedVoicesCoD.get(i) == null)) {
				System.out.println(i);
//				System.out.println("actual " + (actualDurationLabels.get(i).indexOf(1.0) + 1) / 32.0) ;
//				System.out.println("pred   " + (predictedDurationLabels.get(i).indexOf(1.0) + 1) / 32.0) ;
//				System.out.println("actual " + actualVoicesCoD.get(i)[0] + "; " + actualVoicesCoD.get(i)[1]);
//				System.out.println("pred   " + predictedVoicesCoD.get(i)[0] + "; " + predictedVoicesCoD.get(i)[1]);
			}
		}
		
	
		TimeSignature timeSignature = new TimeSignature(3, 4); // TODO
		KeyMarker keyMarker = new KeyMarker();
		keyMarker.setMode(Mode.MODE_MINOR); // TODO
		keyMarker.setAlterationNum(-1); // TODO
		
		predictedTranscription.visualise(timeSignature, true, 3);
	}
	
	
	private static void getNumberOfWeights() {
		ToolBox at = new ToolBox();
		List<Double> weights = at.getStoredObject(new ArrayList<Double>(),
			new File("F/PhD/data/" + "results/NN/Thesis/N2N/tab/3vv/features/noTabInfo/" + 
			"lmbd=0.001/HL=1/fold=1/Best weights NOTE_TO_NOTE.xml"));
	
		System.out.println(weights.size());
		System.out.println(weights);
	}
	
	
	private static void getSignificanceGridSearch() {
		String lambda0 = "0.001";
		int hnf0 = 2;
//		String la = "N2N/";
		String la = "C2C/";
//		String model = "N2/";
//		String model = "N1/";
		String model = "max=4vv/eps=0.05/";
//		String set = "tab/"; 
		String set = "Bach/";
		
		int tOrT = 0; // 0 for training; 1 for test; 2 for appl
		
		// For tab
//		String linesNum0Text1 = "accuracy (strict), numerators";
//		String linesNum0Text2 = "accuracy (strict), denominators";
//		String linesDen0Text1 = "accuracy (strict), denominators";
//		String linesDen0Text2 = "soundness, numerators";
		// Or, modelling duration
//		String linesDen0Text2 = "duration accuracy";
		
		// For Bach
		String linesNum0Text1 = "accuracy (strict) + additional metrics, numerators";
		String linesNum0Text2 = "accuracy (strict) + additional metrics, denominators";
		String linesDen0Text1 = "accuracy (strict) + additional metrics, denominators";
		String linesDen0Text2 = "accuracy percentages for all folds";
			
		int numFolds = 9;
		if (set.equals("Bach/")) {
			numFolds = 19;
		}
		String dir = "results/NN/Thesis/" + la + set + "4vv/gridSearch/" + model + "lmbd=";
//		String fileName = "Summary over all folds.txt";
		String fileName = "summary_folds.txt";
		
		System.out.println("lmbd = " + lambda0);
		System.out.println("hnf  = " + hnf0);
		String st0 = ToolBox.readTextFile(new File("F/PhD/data/" + dir + lambda0 + "/HL=" + hnf0 + "/" + fileName));
//		System.out.println(ExperimentRunner.pathPrefix + dir + lambda0 + "/Hidden nodes = " + hnf0 + "/" + fileName);
				
		String[] linesNum0 = st0.substring(st0.indexOf(linesNum0Text1), st0.indexOf(linesNum0Text2)).split("\r\n");
		String[] linesDen0 = st0.substring(st0.indexOf(linesDen0Text1), st0.indexOf(linesDen0Text2)).split("\r\n");
				
		List<Integer> numerators0 = new ArrayList<Integer>();
		List<Integer> denominators0 = new ArrayList<Integer>();
		for (int i = 2; i < (numFolds + 2); i++) {
			String[] splitNum = linesNum0[i].split("\t");
			numerators0.add(Integer.parseInt(splitNum[tOrT].trim()));
			String[] splitDen = linesDen0[i].split("\t");
			denominators0.add(Integer.parseInt(splitDen[tOrT].trim()));
		}				
		
//		System.out.println(numerators0);
//		System.out.println(denominators0);
		
		List<Double> accuracies0 = new ArrayList<Double>();
		double[] acc0 = new double[numerators0.size()];
		for (int i = 0; i < numerators0.size(); i++) {
			double a = numerators0.get(i) / (1.0 * denominators0.get(i));
			accuracies0.add(a);
			acc0[i] = a;
		}
				
//		for (Double d: accuracies) {
//			System.out.println(d);
//		}
		int numsSummed0 = ToolBox.sumListInteger(numerators0);
		int denomsSummed0 = ToolBox.sumListInteger(denominators0);
		double avg0 = (1.0 * numsSummed0) / denomsSummed0;
		double stdev0 = ToolBox.stDev(accuracies0);

		System.out.println("AVG =   " + avg0);		
		System.out.println("STDEV = " + stdev0);
		System.out.println("**********************\r\n");
//		System.exit(0);
		
		List<Integer> hnfs = Arrays.asList(new Integer[]{-8, -4, -2, 1, 2}); //, 4});
		List<String> lambdas = Arrays.asList(new String[]{"0.1", "0.03", "0.01", "0.003", "0.001", 
			"3.0E-4", "1.0E-4", "3.0E-5", "1.0E-5", "0.0"});
		
		String sigs = "SIGNIFICANCE\r\n \t";
		String stdevs = "STDEV\r\n \t";
		for (String s : lambdas) {
			sigs = sigs.concat(s + "\t\t");
			stdevs = stdevs.concat(s + "\t\t\t");
		}
		sigs = sigs.concat("\r\n");
		stdevs = stdevs.concat("\r\n");
		for (int h : hnfs) {
			sigs = sigs.concat(h + "\t");
			stdevs = stdevs.concat(h + "\t");
			for (String l : lambdas) {
//				if (h != hnf0 && l != lambda0) {			
					System.out.println("lmbd = " + l);
					System.out.println("hnf  = " + h);
					String st = ToolBox.readTextFile(new File("F/PhD/data" + dir + l + "/HL=" + h + "/" + fileName));
							
//					String[] linesNum = st.substring(st.indexOf("accuracy (strict), numerators"), st.indexOf("accuracy (strict), denominators")).split("\r\n");
//					String[] linesDen = st.substring(st.indexOf("accuracy (strict), denominators"), st.indexOf("soundness, numerators")).split("\r\n");
//					String[] linesDen = st.substring(st.indexOf("accuracy (strict), denominators"), st.indexOf("duration accuracy")).split("\r\n");
					
					String[] linesNum = st.substring(st.indexOf(linesNum0Text1), st.indexOf(linesNum0Text2)).split("\r\n");
					String[] linesDen = st.substring(st.indexOf(linesDen0Text1), st.indexOf(linesDen0Text2)).split("\r\n");
					
					List<Integer> numerators = new ArrayList<Integer>();
					List<Integer> denominators = new ArrayList<Integer>();
					for (int i = 2; i < (numFolds + 2); i++) {
						String[] splitNum = linesNum[i].split("\t");
						numerators.add(Integer.parseInt(splitNum[tOrT].trim()));
						String[] splitDen = linesDen[i].split("\t");
						denominators.add(Integer.parseInt(splitDen[tOrT].trim()));
					}				
					
//					System.out.println(numers);
//					System.out.println(denomers);
					
					List<Double> accuracies = new ArrayList<Double>();
					double[] acc = new double[numerators.size()];
					for (int i = 0; i < numerators.size(); i++) {
						double a = numerators.get(i) / (1.0 * denominators.get(i));
						accuracies.add(a);
						acc[i] = a;
					}
							
//					for (Double d: accuracies) {
//						System.out.println(d);
//					}
					int numsSummed = ToolBox.sumListInteger(numerators);
					int denomsSummed = ToolBox.sumListInteger(denominators);
					double avg = (1.0 * numsSummed) / denomsSummed;
					double stdev = ToolBox.stDev(accuracies);

					System.out.println("AVG =   " + avg);		
					System.out.println("STDEV = " + stdev);
	
					double sig = ToolBox.sig(acc0, acc);
					System.out.println("SIG =  " + sig);
					if (sig != ToolBox.sig(acc, acc0)) {
						System.out.println("--> HAS TIE");
						System.out.println("  (x, y) = " + sig);
						System.out.println("  (y, x) = " + ToolBox.sig(acc, acc0));
					}
					System.out.println();
					
					sigs = sigs.concat(sig + "\t");
					stdevs = stdevs.concat(stdev + "\t");
					
//				}
			}
			sigs = sigs.concat("\r\n");
			stdevs = stdevs.concat("\r\n");
		}
		System.out.println();
		System.out.println(sigs);
		System.out.println();
		System.out.println(stdevs);
	}
}
