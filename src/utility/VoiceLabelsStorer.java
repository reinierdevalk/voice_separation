package utility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import representations.Transcription;
import tools.ToolBox;
import ui.Runner;

public class VoiceLabelsStorer {

	/**
	 * Stores the voice labels of the given Tablature-Transcription pair in a File with the given name.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		List<String> pieceNames = new ArrayList<String>();
		
//		pieceNames = Dataset.getTabFourVoices(); String vv = "4vv/"; boolean isTablatureCase = true;
		pieceNames = null; // Dataset.getTabThreeVoices(); 
		String vv = "3vv/"; boolean isTablatureCase = true;
//		pieceNames = Dataset.getBachFourVoiceFugues(); String vv = "4vv/"; boolean isTablatureCase = false;
//		pieceNames = Dataset.getBachThreeVoiceFugues(); String vv = "3vv/"; boolean isTablatureCase = false;
		
		String dir = "results/Ground truth/";

		for (String s : pieceNames) {	
			List<List<Double>> voiceLabels = null;
			File voiceLabelsFile = null;
			if (isTablatureCase) {       
				File encodingFile = 
					new File(Runner.encodingsPath + vv + s + ".tbp");
				File midiFile = new File(Runner.midiPath + vv + s);
				voiceLabelsFile = new File("F/PhD/data" + dir + "tab/" + vv +
//					"Predicted voice labels " +	currentPieceName + ".xml");
//					TrainingManager.bestVoiceLabelsPrefix +	currentPieceName + ".xml");
					"Ground truth voice labels " + s + ".xml");
				File durationLabelsFile = new File("F/PhD/data" + dir + "tab/" + vv +
//					"Predicted duration labels " + currentPieceName + ".xml");
//					TrainingManager.bestDurationLabelsPrefix + currentPieceName + ".xml");
				    "Ground truth duration labels " + s + ".xml");
				
				Transcription transcription = new Transcription(midiFile, encodingFile);
				voiceLabels = transcription.getVoiceLabels();
				
				List<List<Double>> durationLabels = transcription.getDurationLabels();
				ToolBox.storeObject(durationLabels, durationLabelsFile);
			} 
			else {	
				File midiFile = new File(Runner.midiPath + "WTC/" + vv + s);
				voiceLabelsFile = 
//					new File(ExperimentRunner.pathPrefix + dir + "Bach/" + vv +	"Predicted voice labels " + s + ".xml");	
					new File("F/PhD/data" + dir + "Bach/" + vv + Runner.genVoiceLabels + 
						s + ".xml");	
				File eqDurInfoFile = 
					new File("F/PhD/data" + dir + "Equal duration unisons info " + s + ".xml");
				
				Transcription transcription = new Transcription(midiFile, null);
				voiceLabels = transcription.getVoiceLabels();
				
				List<Integer[]> eqDurInfo = transcription.getEqualDurationUnisonsInfo();
				ToolBox.storeObject(eqDurInfo, eqDurInfoFile);
			}
			ToolBox.storeObject(voiceLabels, voiceLabelsFile);
		}
	}
}
