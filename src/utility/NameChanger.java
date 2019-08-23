package utility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tools.ToolBox;

public class NameChanger {

	public static void main(String[] args) {
		List<String> lambdas = new ArrayList<String>();

		String learningApproach = "NOTE_TO_NOTE";
//		String learningApproach = "CHORD_TO_CHORD";
		
		boolean useCrossValidation = true;
		
//		lambdas.add("0.1"); 
//		lambdas.add("0.03");
//		lambdas.add("0.01"); 
//		lambdas.add("0.0");
//		lambdas.add("0.001");
//		lambdas.add("0.003");
//		lambdas.add("1.0E-4");
		lambdas.add("1.0E-5");
//		lambdas.add("3.0E-4"); 
//		lambdas.add("3.0E-5");  
		
		List<String> hiddenNeuronsFactors = new ArrayList<String>();
//		hiddenNeuronsFactors.add("HL=-8");
		hiddenNeuronsFactors.add("HL=-4");
//		hiddenNeuronsFactors.add("HL=-2");
//		hiddenNeuronsFactors.add("HL=1");
//		hiddenNeuronsFactors.add("HL=2");
//		hiddenNeuronsFactors.add("HL=4");

		// 1. Determine the experiment
//		String experimentFolderName = "gridSearch/gridSearch NOTE_TO_NOTE featureSet A";
//		String experimentFolderName = "gridSearch/gridSearch NOTE_TO_NOTE featureSet B";
//		String experimentFolderName = "gridSearch/gridSearch NOTE_TO_NOTE featureSet C";
//		String experimentFolderName = "gridSearch/gridSearch NOTE_TO_NOTE featureSet D";
//		String experimentFolderName = "imitativeOchsenkun";
//		String experimentFolderName = "free";
//		String experimentFolderName = "imitativeOtherIntabulators";
//		String experimentFolderName = "ochsenkun";
		String experimentFolderName = "otherIntabulators";
//		String experimentFolderName = "fourVoicePieces";

		int numberOfFolds = 1;
		int num = 0;
		for (int i = 0; i < lambdas.size(); i++) {
			String currentLambda = lambdas.get(i);
//			System.out.println("currentLambda = " + currentLambda);

			for (int j = 0; j < hiddenNeuronsFactors.size(); j++) {
				String currentHiddenNodes = hiddenNeuronsFactors.get(j);

				for (int k = 0; k < numberOfFolds; k++) {
					int currentFold = k + 1;
//					System.out.println("currentFold " + currentFold);

					String currentFolderName = "";
					// With cross-validation
					if (useCrossValidation) {
//						currentFolderName = ExperimentRunner.pathPrefix + "files_out/NN/" + 
//							experimentFolderName + "/lambda = " + currentLambda + "/" + currentHiddenNodes + "/Fold " +
//							currentFold + "/";
						currentFolderName = "F/PhD/data" + "results/NN/" + 
							experimentFolderName + "/lmbd=" + currentLambda + "/" + currentHiddenNodes + "/fold=" +
							currentFold + "/";
					}
					// Without cross-validation
					if (!useCrossValidation) {
//						currentFolderName = ExperimentRunner.pathPrefix + "files_out/NN/" + 
//							experimentFolderName + "/lambda = " + currentLambda + "/" + currentHiddenNodes + "/No CV/";
						currentFolderName = "F/PhD/data" + "results/NN/" + 
							experimentFolderName + "/lmbd=" + currentLambda + "/" + currentHiddenNodes + "/no_CV/";
					}
					System.out.println(currentFolderName);
					File currentFolder = new File(currentFolderName); 
//					System.out.println("currentFolder = " + currentFolder);
					String[] currentContentsList = currentFolder.list();
//					System.out.println("currentContentsList = ");
//					for (String s : currentContentsList) {
//						System.out.println(s);
//					}
					
					String testPieceName = null;
					for (String s : currentContentsList) {
//						if (s.startsWith("Test process record")) {
						if (s.startsWith("02-test_rec")) { // TODO --> Runner.testRec
//							int startIndex = "Test process record NOTE_TO_NOTE ".length();
//							int startIndex = ("Test process record " + learningApproach + " ").length();
							int startIndex = ("02-test_rec").length(); // TODO --> Runner.testRec
							testPieceName = s.substring(startIndex);
						}
					}

					for (int l = 0; l < currentContentsList.length; l++) {
						String currentFileName = currentContentsList[l]; 
						File currentFile = new File(currentFolderName + currentFileName);
//						System.out.println("currentFile name = " + currentFile.getAbsolutePath());

						// a. To change content of .txt files
						if (currentFile.getAbsolutePath().endsWith("Training results " + learningApproach + ".txt")) {
							System.out.println("currentFileName = " + currentFileName);
							String contents = ToolBox.readTextFile(currentFile);
							if (contents.contains("training error")) {
								num++;
								System.out.println("Fold = " + currentFold);
								System.out.println(currentFile.getAbsolutePath());
								// a. Short text
								contents = contents.replace("training error", "network error");
//								contents = contents.replace("\n", "\r\n");
//								// b. Longer text
//			      		  		String toReplace = 
//			      		  			contents.substring(contents.indexOf("soundness:"), contents.indexOf("GROUND TRUTH VOICES"));
//			      		  		contents = contents.replace(toReplace, "\r\n");
								ToolBox.storeTextFile(contents, currentFile);
							}
						}
						if (currentFile.getAbsolutePath().contains("01-train_rec") || 
							currentFile.getAbsolutePath().contains("02-test_rec") || 
							currentFile.getAbsolutePath().contains("03-appl_rec")) { // TODO --> Runner.train-, test- and applRec
//						if (currentFile.getAbsolutePath().endsWith("Training results " + learningApproach + ".txt") ||
//								currentFile.getAbsolutePath().contains("Training process record " + learningApproach) ||
//								currentFile.getAbsolutePath().contains("Test process record " + learningApproach) ||
//								currentFile.getAbsolutePath().contains("Application process record " + learningApproach)) {
							System.out.println("currentFileName = " + currentFileName);
							String contents = ToolBox.readTextFile(currentFile);
							
							num++;
							System.out.println("Fold = " + currentFold);
							System.out.println(currentFile.getAbsolutePath());
							// a. Short text
							contents = contents.replace("number of onsets assigned to the incorrect voice: ",
								"number of notes assigned to the incorrect voice: ");
							contents = contents.replace("number of onsets assigned to a voice already containing a note: ",
								"number of notes assigned to a voice already containing a note: ");
							contents = contents.replace("number of onsets where a CoD was overlooked (predictedVoices contains one element; actualVoices two): ",
								"number of notes with an overlooked voice assignment (one voice predicted where there are actually two): ");
							contents = contents.replace("number of onsets with a superfluous CoD assignment (predictedVoices contains two elements; actualVoices one): ",
								"number of notes with a superfluous voice assignment (two voices predicted where there is actually one): ");
							contents = contents.replace("number of onsets with a wrong CoD assignment (both predictedVoices and actualVoices contain two elements, but only one is assigned correctly): ",
								"number of notes with a half voice assignment (two voices predicted where there are actually two, but only one of the predicted voices is correct): ");
							contents = contents.replace("F1-score", "F1 score");
//								contents = contents.replace("\n", "\r\n");
//								// b. Longer text
//			      		  		String toReplace = 
//			      		  			contents.substring(contents.indexOf("soundness:"), contents.indexOf("GROUND TRUTH VOICES"));
//			      		  		contents = contents.replace(toReplace, "\r\n");
							ToolBox.storeTextFile(contents, currentFile);
							
						}
						
						 
						// b. To delete a file
//						if (currentFileName.startsWith(EncogNeuralNetworkManager.applRec)) {
//							if (!currentFileName.endsWith(testPieceName)) {
//								num++;
//								currentFile.delete();
//							}
//						}

						// c. To change the name of a file
//						if (currentFileName.contains("è")) {
//							File currentFile = new File(currentFolderName + currentFileName);
//							System.out.println("currentFile name = " + currentFile.getAbsolutePath());
////							System.out.println("currentFileName = " + currentFileName);
//							num++;
////							String newName = currentFileName.replace("è", "e");
//							String newName = currentFileName.replace("ü", "u");
//
////							System.out.println("newFileName =     " + newName);
////							System.out.println("currentFolderName = " + currentFolderName);
//							File newFile = new File(currentFolderName + newName); 
//							System.out.println("newFile name =     " + newFile.getAbsolutePath());
//							System.out.println(currentFile.renameTo(newFile));
//						}
					}
				}
			}
		}
		System.out.println("num = " + num);
	}
	
}
