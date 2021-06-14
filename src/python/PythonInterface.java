package python;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import representations.Transcription;
import tools.ToolBox;
import ui.Runner;

/**
 * See https://norwied.wordpress.com/2012/03/28/call-python-script-from-java-app/
 * and https://norwied.wordpress.com/2012/07/23/pass-arguments-from-java-to-python-app/
 * 
 * @author Reinier
 *
 */
public class PythonInterface {
	
	public static void main(String[] args) throws IOException {
//		String[] cmd = new String[4];
//		String pythonScriptPath = "C:/Users/Reinier/Desktop/ISMIR2017/test.py";
//		cmd[0] = "python"; // check version of installed python: python -V
//		cmd[1] = pythonScriptPath;
//		cmd[2] = "hoi";
//		cmd[3] = "hallo";
//		applyModel(cmd);
				
		System.exit(0);
		test();
//		initPython(null);
//		callPython(null);
//		exitPython();
	}
	
	public static void test() { // throws IOException {
		try {
			// Create runtime to execute external command
			Runtime rt = Runtime.getRuntime();		
			pr = rt.exec("ipython");

			bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			// Stop reading after 'In [1]: ' 
			int c = -1;
			char prePrev = '#';
			char prev = '#';
			while ((c = bfr.read()) != -1) {
				System.out.print((char)c);
				if (c == ' ' && prev == ':' && prePrev == ']') {
					break;
				}
				prePrev = prev;
				prev = (char)c;
			}
			// until here: prints iPython start thing and terminates
			
			// Writer writes to terminal (i.e., to process)
			bfw = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));
			
			// Import everything
			String[] cmds = new String[]{ 
				"import numpy as np",
				"import pandas as pd",
				"x = [1.01, 1.02, 1.03]",
				"y = [2.01, 2.02, 2.03]",
				"for i in x: print(i)",
				"print(type(x))",
				"print(type(y))",
				"z = np.array([x, y])",
				"df = pd.DataFrame(z)",
				"df.to_csv('C:/Users/Reinier/Desktop/fooddel.csv', header=False, index=False)",
				"print('stuff saved')",
//				"print('#')" // for enabling bfr to stop
			};
			for (String s : cmds) {
				bfw.write(s + cr);
			}
			bfw.flush(); // for processing the stuff added to bfw
			
			while ((c = bfr.read()) != '#') {
				System.out.print((char)c);
			}
			System.out.println("bla");			
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	

	public static Process pr;
	public static Reader bfr;
	public static Writer bfw;
	private static String cr = "\r\n";
	
	public static void init(String[] cmd) { // throws IOException {
		// For scikit
		String path = cmd[0];
		String model = cmd[1];
		String ext = cmd[2];
		// For TensorFlow
		String argPath = cmd[0];
		String argModel = cmd[1];
		String argParams = cmd[2];
		String mode = Runner.application;
		try {
			// Create runtime to execute external command
			Runtime rt = Runtime.getRuntime();		
			pr = rt.exec("ipython");

			// Reader reads from terminal answer
			// problem with reader: we don't know when it stops
			bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			// Stop reading after 'In [1]: ' 
			int c = -1;
			char prePrev = '#';
			char prev = '#';
			while ((c = bfr.read()) != -1) {
				System.out.print((char)c);
				if (c == ' ' && prev == ':' && prePrev == ']') {
					break;
				}
				prePrev = prev;
				prev = (char)c;
			}
			
			// Writer writes to terminal (i.e., to process)
			bfw = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));
			
			String cmdStr = "";
			boolean isScikit = false;
			// For scikit
			if (isScikit) {
				// Import modules
				cmdStr =
//					"print('importing modules')" +	
					"import os" + cr +
					"from stat import *" + cr +
					"from sys import argv" + cr +
					"from sklearn.linear_model import LogisticRegression" + cr +
					"from sklearn.svm import LinearSVC" + cr +
					"from sklearn.ensemble import RandomForestClassifier" + cr +
					"from sklearn.neighbors import KNeighborsClassifier" + cr +
					"from sklearn.externals import joblib" + cr +
					"import numpy as np" + cr +
					"import pandas as pd" + cr
//					"print('loading the model')",
					;
				// Initialise model
				cmdStr += "m = joblib.load('" + path + model + ".pkl')" + cr;
			}
			
			// For TensorFlow
			// Set imports and variables
			String cdDir = Runner.scriptPathPython;
//			String cdDir = "C:Users/Reinier/Dropbox/";
			String imports =
				"cd " + cdDir  + cr +
				"from sys import argv" + cr +
				"import tensorflow as tf" + cr +
				"import numpy as np" + cr +
				"from numpy import genfromtxt" + cr +
				"import " + Runner.script.substring(0, Runner.script.indexOf(".py")) + cr
			;
			String variables =
				"mode = " + Runner.APPL + cr + 
//				"fv_ext = '" + Runner.fvExt + mode + ".csv'" + cr + 
//				"lbl_ext = '" + Runner.lblExt + mode + ".csv'" + cr +
//				"out_ext = '" + Runner.outpExt + mode + ".csv'" + cr +
				"use_stored_weights = True" + cr +
				"fold_path = '" + argPath + "'" + cr +
//				"num_features = len(genfromtxt(fold_path + fv_ext, delimiter=','))" + cr + 
//				"num_classes = len(genfromtxt(fold_path + lbl_ext, delimiter=','))" + cr +	
				"params = [s.strip() for s in '" + argParams + "'.split(',')]" + cr +
				"param_dict = {}" + cr +
				// This can be a one-liner because there are no double parameters
				"for item in params: param_dict[item.strip().split('=')[0]] = int(item.strip().split('=')[1]) " + cr +				
				"num_HL = param_dict['hidden layers']" + cr +
				"IL_size = param_dict['input layer size']" + cr +
				"HL_size = param_dict['hidden layer size']" + cr +
				"OL_size = param_dict['output layer size']" + cr +			
//				"num_HL =  int(float(" + arg_param + "))" + cr + 
//				"num_nodes_HL = [num_features, num_features, num_features, num_features]" + cr +
//				"layer_sizes = [num_features]" + cr + 
//				"for i in range(num_HL): layer_sizes.append(num_nodes_HL[i])" + cr +
//				"layer_sizes.append(num_classes)" + cr
				"layer_sizes = [IL_size]" + cr +
				"for i in range(num_HL): layer_sizes.append(HL_size)" + cr +
				"layer_sizes.append(OL_size)" + cr
			;
			// Create and call the model
//			String defCreateNeuralNetwork = "";
//			for (String s : scriptToString(new File(Runner.scriptPath), "def create_neural_network")) {
//				defCreateNeuralNetwork += s + cr;
//			}
			String call = 
				"weights_biases = train_test_tensorflow.create_neural_network(layer_sizes, " + 
				"use_stored_weights, mode, fold_path)" + cr
//				"print(type(weights_biases['weights']['W1']))" + cr +
//				"print(type(weights_biases['biases']['b1']))" + cr
			;
			cmdStr += imports; 
			cmdStr += variables;
//			cmdStr += defCreateNeuralNetwork;
			cmdStr += call;
			
			System.out.println();
//			System.out.println(cmdStr);
			bfw.write(cmdStr);		
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}


	public static List<String> scriptToString(File script, String marker) {
		String content = ToolBox.readTextFile(script);
		
		// Get function
		int startFunction = content.indexOf(marker);
		int endFunction = content.indexOf("# fed", startFunction);
		String function = content.substring(startFunction, endFunction);
		
		// Cut out docstring
		String ds = "\"\"\"";
		int startDocstring = function.indexOf(ds);
		int endDocstring = function.indexOf(ds, startDocstring + 1) + ds.length();
		String toReplace = function.substring(startDocstring, endDocstring);
		function = function.replace(toReplace, "");
		
		// Remove all comments and empty lines
		String[] functionSplit = function.split("\r\n");
		List<String> functionClean = new ArrayList<String>();
		for (String s : functionSplit) {
			String sTrimmed = s.trim();
			if ((!sTrimmed.startsWith("#")) && (!sTrimmed.isEmpty())) {
				functionClean.add(s);
			}
		}
		return functionClean;
	}
	
	/**
	 * Writes the given function from the given master script as a separate script.
	 *  
	 * @param masterScript
	 * @param functionName
	 */
	public static void functionToScript(File masterScript, String functionName, String path) {
		String content = ToolBox.readTextFile(masterScript);
		
		// Get function
		int startFunction = content.indexOf(functionName);
		int endFunction = content.indexOf("# fed", startFunction);
		String function = content.substring(startFunction, endFunction);
		
		// Get args
		String def = function.substring(0, function.indexOf(":"));
		String allArgs = def.substring(def.indexOf("(")+1, def.indexOf(")"));
		// https://stackoverflow.com/questions/41953388/java-split-and-trim-in-one-shot
		String[] args = allArgs.trim().split("\\s*,\\s*");	
		String argv = "";
		
		for (String s : args) {
			argv += s;
			if (s.equals(args[args.length-1])) {
				argv += " = argv" + "\r\n" + "\r\n";
			}
			else {
				argv += ", ";
			}
		}
		
		ToolBox.storeTextFile(argv + function, new File(path));
		
//		// Cut out docstring
//		String ds = "\"\"\"";
//		int startDocstring = function.indexOf(ds);
//		int endDocstring = function.indexOf(ds, startDocstring + 1) + ds.length();
//		String toReplace = function.substring(startDocstring, endDocstring);
//		function = function.replace(toReplace, "");
		
//		// Remove all comments and empty lines
//		String[] functionSplit = function.split("\r\n");
//		List<String> functionClean = new ArrayList<String>();
//		for (String s : functionSplit) {
//			String sTrimmed = s.trim();
//			if ((!sTrimmed.startsWith("#")) && (!sTrimmed.isEmpty())) {
//				functionClean.add(s);
//			}
//		}
	}


	public static double[] predictNoLoading(String argFv) { // throws IOException {
		double[] output = new double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
		
//		String path = cmd[0]; // path
//		String argFv = cmd[1]; // String rep of fv
//		String outpExt = cmd[2]; // Runner.outpExt + "appl.csv"
		try {					
			String cmdStr = 
//				"print('loading features and applying model')" + cr +
//				"X = np.loadtxt('" + path + fvExt + "', delimiter=\",\")" + cr +
				"arg_fv = '" + argFv + "'" + cr + 
				"X_list = [float(s.strip()) for s in arg_fv.split(',')]" + cr +
//				"print(X_list)" + cr +
				"X = np.array(X_list)" + cr +
				"X = X.reshape(1, -1)" + cr +
				"classes = m.predict(X)" + cr +
				"probs = m.predict_proba(X)" + cr +
				"max_num_voices = 5" + cr +
				"num_cols_to_add = max_num_voices - len(probs[0])" + cr + 
				"num_ex = len(probs)" + cr +
				"z = np.zeros((num_ex, num_cols_to_add), dtype=probs.dtype)" + cr +
				"probs = np.append(probs, z, axis=1)" + cr +
//				"print(X)" + cr +
//				"print(classes)" + cr +
//				"print(probs)" + cr +
//				"print('saving model output')" + cr +
				"output = ','.join([str(p) for p in probs[0]])" + cr +
				"print('@' + output + '@')" + cr +
//				"df_probs = pd.DataFrame(probs)" + cr +
//				"df_probs.to_csv('" + path + outpExt + "', header=False, index=False)" + cr +			
				"print('#')" + cr
			;
			bfw.write(cmdStr);
			bfw.flush(); // needed to execute the above commands
							
			int c = -1;
			String outp = "";
			char prev = '#';
			boolean addToOutp = false;
			while((c = PythonInterface.bfr.read()) != '#') {
//				System.out.print((char)c);
				// If previous char is begin marker: start adding
				if (prev == '@') {
					addToOutp = true;
				}
				// If char is end marker: stop adding
				if (c == '@' && addToOutp) {
					addToOutp = false;
					// Prevent further adding by resetting prev
					prev = '#';
				}
				else {
					prev = (char) c;
				}
				if (addToOutp) {
					outp += (char)c;
				}
			}
			
			String[] indiv = outp.split(",");
			for (int i = 0; i < indiv.length; i++) {
				output[i] = Double.parseDouble(indiv[i]);
			}
//			System.out.println(cmdStr);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}


	public static void predict(String[] cmd) { // throws IOException {
		// For scikit
		String path = cmd[0]; // path
		String mdl = cmd[1]; 
		String fvExt = cmd[2]; // Runner.fvExt + "appl.csv"
		String outpExt = cmd[3]; // Runner.outpExt + "appl.csv"
		// For TensorFlow
		String argPath = cmd[0];
		String argMdl = cmd[1];
		String argFv = cmd[2];
		String mode = cmd[3];
		try {
			String cmdStr = "";
			boolean isScikit = false;
			// For scikit
			if (isScikit) {
				cmdStr = 
//					"print('loading features and applying model')" + cr +
					"X = np.loadtxt('" + path + fvExt + "', delimiter=\",\")" + cr +
					"X = X.reshape(1, -1)" + cr +
					"classes = m.predict(X)" + cr +
					"max_num_voices = 5" + cr +
					"num_ex = len(classes)" + cr
				;
				if (!mdl.endsWith("CL")) {
					cmdStr +=
						"probs = m.predict_proba(X)" + cr +		
						"num_cols_to_add = max_num_voices - len(probs[0])" + cr +
//						"num_ex = len(probs)" + cr +
						"z = np.zeros((num_ex, num_cols_to_add), dtype=probs.dtype)" + cr +
						"probs = np.append(probs, z, axis=1)" + cr
					;	
				}
				else {
					cmdStr +=
						"probs = np.zeros((num_ex, max_num_voices))" + cr +
						"for i in range (0, num_ex): probs[i][int(classes[i])] = 1.0" + cr
					;
				}	
				cmdStr +=
//					"print(X)" + cr +
//					"print(classes)" + cr +
//					"print(probs)" + cr +
//					"print('saving model output')" + cr +
					"df_probs = pd.DataFrame(probs)" + cr +
					"df_probs.to_csv('" + path + outpExt + "', header=False, index=False)" + cr +
//					"df_classes = pd.DataFrame(classes)" + cr + 
//					"df_classes.to_csv('" + path + "outp_cl_appl.csv', header=False, index=False)" + cr +
					"print('#')" + cr
				;
			}
			// For TensorFlow
			String placeholders =
//				"tf.reset_default_graph()" + cr + 
				// Load features from file
//				"x_appl = genfromtxt('" + argPath + Runner.fvExt + mode +  ".csv', delimiter=',')" + cr +
//				"x_appl = x_appl.reshape(1, -1)" + cr +
//				"print(x_appl.shape)" + cr +
//				"print(x_appl)" + cr +
//				// Features string is argument to method
//				"arg_fv = '" + argFv + "'" + cr + 
//				"print(arg_fv)" + cr +
//				"list_from_string = [float(s.strip()) for s in arg_fv.split(',')]" + cr +
//				"x_appl = np.array(list_from_string)" + cr +
				"x = tf.placeholder('float', [None, IL_size])" + cr +
	//			"print(x)" + cr + 
				"y = tf.placeholder('float')" + cr +
				"keep_prob = tf.placeholder('float')" + cr
			; 
//			String defEvaluateNeuralNetwork = "";
//			for (String s : scriptToString(new File(Runner.scriptPath), "def evaluate_neural_network")) {
//				defEvaluateNeuralNetwork += s + cr;
//			}
//			String defRunNeuralNetwork = "";
//			for (String s : scriptToString(new File(Runner.scriptPath), "def run_neural_network")) {
//				defRunNeuralNetwork += s + cr;
//			}
			//

			String call =
				"sess = tf.InteractiveSession()" + cr +
				"tf.set_random_seed(0)" + cr +
				"lrn_rate = 0" + cr +
				"kp = 0.0" + cr +
				"epochs = 0" + cr +
				"train_test_tensorflow.run_neural_network(x, keep_prob, lrn_rate, kp, epochs, layer_sizes, use_stored_weights, mode, fold_path, weights_biases)" + cr +
				"sess.close()" + cr
			;
			cmdStr += placeholders; 
//			cmdStr += defEvaluateNeuralNetwork;
//			cmdStr += defRunNeuralNetwork;
			cmdStr += call;
			cmdStr += "print('#')" + cr;
			
			bfw.write(cmdStr);
			bfw.flush(); // needed to execute the above commands
			int c = -1;
			while((c = PythonInterface.bfr.read()) != '#') {
//				System.out.print((char)c); // To print out iPython terminal output
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static void initPython(String[] cmd) { // throws IOException {
		String path = cmd[0];
		String model = cmd[1];
		String ext = cmd[2];
		try {
			// Create runtime to execute external command
			Runtime rt = Runtime.getRuntime();		
			pr = rt.exec("ipython");
			
			// Writer writes to terminal (i.e., to process)
			// Reader reads from terminal answer
			// problem w/ reader: we don't know when it stops
			bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			// Stop reading after 'In [1]: ' 
			int c = -1;
			char prePrev = '#';
			char prev = '#';
			while ((c = bfr.read()) != -1) {
				System.out.print((char)c);
				if (c == ' ' && prev == ':' && prePrev == ']') {
					break;
				}
				prePrev = prev;
				prev = (char)c;
			}
			
			bfw = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));

//			bfw.write("%cd C:/Users/Reinier/Desktop/ISMIR2017/" + cr);
//			bfw.write("pwd\r\n");
//			bfw.write("import init\r\n");

//			bfw.write("import numpy as np\r\n");
			
			// Import everything
			String[] imports = new String[]{ 
				"import os" + "\r\n",
				"from stat import *",
				"from sys import argv",
				"from sklearn.linear_model import LogisticRegression",
				"from sklearn.externals import joblib",
				"import numpy as np",
				"import pandas as pd"
			};
			for (String s : imports) {
				bfw.write(s + cr);
			}
			
			// Init model
			bfw.write("m = joblib.load('" + path + model + ".pkl')" + cr);
							
			String[] cmds = new String[]{
//				"m = LogisticRegression()",
//				"m = joblib.load('F:/research/data/experiments/ISMIR-2017/intabulations/3vv/LR/fwd/LR.pkl')",	
//				"m = joblib.load('" + path + model + ".pkl')",	
				
				"X = np.loadtxt('" + path + ext + "', delimiter=\",\")",
				
//				"X = np.loadtxt('F:/research/data/experiments/ISMIR-2017/intabulations/3vv/LR/fwd/fv_train.csv', delimiter=\",\")",
//				"y = np.loadtxt('F:/research/data/experiments/ISMIR-2017/intabulations/3vv/LR/fwd/cl_train.csv', delimiter=\",\")",
//				"m.fit(X[:120],y[:120])",

//				"classes = m.predict(X)",
//				"probs = m.predict_proba(X)",
				
			};
//			for (String s : cmds) {
//				bfw.write(s + cr);
//			}
			
//			bfw.write("probs = m.predict_proba(X[120:])" + cr);
			
			
//			bfw.write("\r\n");
//			bfw.write("\r\n");
//			bfw.write("print('#')\r\n");
//			bfw.flush();
			
					
//			while((c = bfr.read()) != '#') {
//				System.out.print((char)c);
//			}
			
			// Retrieve output from Python script
//			bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
//			while((c = bfr.read()) != -1) {
//				System.out.print((char)c);
//			}
			
//			// Retrieve error (if any) from Python script. See Listing 4.3 at 
//			// http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
////			InputStream stderr = pr.getErrorStream();
////			InputStreamReader isr = new InputStreamReader(stderr);
//			BufferedReader br = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
//			line = null;
////			System.out.println("<ERROR>");
//			boolean errorFound = false;
//			while ( (line = br.readLine()) != null) {
//				errorFound = true;
//				System.out.println(line);
//			}
////			System.out.println("</ERROR>");
//			if (errorFound) { 
//				int exitVal = pr.waitFor();
//				System.out.println("Process exitValue: " + exitVal);
//			}
			System.out.println("Python started.");
			
			bfw.write("exit()\r\n");
			bfw.flush();
			
			
			while((c = bfr.read()) != '#') {
				System.out.print((char)c);
			}
//			System.out.println("Python exited.");
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}


	private static void callPython(String s) {
		try {
//			bfw.write("x = np.array([1.1, 2.2, 3.3, 4.4])\r\n");
//			bfw.write("for n in x:\r\n");
//			bfw.write("\tprint(n)\r\n");
			bfw.write("print('en nog eens')\r\n");
			bfw.write("print(x)\r\n");
			bfw.flush();
			int c = -1;
			while((c = bfr.read()) != '#') {
				System.out.print((char)c);
			}
			
			System.out.println("Call ended.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static void exitPython() {
		try {
			bfw.write("'#'");
//			bfw.write("exit()\r\n");
			bfw.flush();
			
			int c = -1;
			while((c = bfr.read()) != '#') {
				System.out.print((char)c);
			}
			System.out.println("Python exited.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	* @param args
	* @throws IOException
	*/
	public static double[] applyModel(String[] cmd) { // throws IOException {
		double[] outp = new double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
		try {
			// Create runtime to execute external command
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec(cmd);
			
			// Retrieve output from Python script
			BufferedReader bfr = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = "";
//			double[] outp = new double[Transcription.MAXIMUM_NUMBER_OF_VOICES];
			while((line = bfr.readLine()) != null) {
				System.out.println(line);
				String s = "model output = ";
				if (line.startsWith(s)) {
					System.out.println(line);
					String output = line.substring(s.length());
					String[] indiv = output.split(",");
					for (int i = 0; i < indiv.length; i++) {
						outp[i] = Double.parseDouble(indiv[i]);
					}
				}
			}
			String arrAsStr = "               ";
			for (double d : outp) {
				arrAsStr += d + ",";
			}
//			System.out.println(arrAsStr);
			
			// Retrieve error (if any) from Python script. See Listing 4.3 at 
			// http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
//			InputStream stderr = pr.getErrorStream();
//			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
			line = null;
//			System.out.println("<ERROR>");
			boolean errorFound = false;
			while ( (line = br.readLine()) != null) {
				errorFound = true;
				System.out.println(line);
			}
//			System.out.println("</ERROR>");
			if (errorFound) { 
				int exitVal = pr.waitFor();
				System.out.println("Process exitValue: " + exitVal);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return outp;
	}
	
}
