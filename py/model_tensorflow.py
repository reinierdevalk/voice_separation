import os
from sys import argv
import tensorflow as tf
import numpy as np
from numpy import genfromtxt
import matplotlib.pyplot as plt
from functools import partial

train = 0
test = 1
appl = 2
verbose = True
plot_or_not = True

# NOTES
# - https://stackoverflow.com/questions/49041001/why-is-this-tensorflow-code-so-slow
# - eval() is alternative for sess.run(), but is done for each variable individually. Compare (1) with (2), (3) 	
#   (1) acc_val, sm_val = sess.run([accuracy_val, softm_val], feed_dict={x: x_val, y: y_val, keep_prob: 1.0})
#	(2) acc_val = accuracy_val.eval({x: x_val, y: y_val, keep_prob: 1.0})
#	(3) sm_val = softm_val.eval({x: x_val, keep_prob: 1.0})


def create_neural_network(mode, layer_sizes, use_stored_weights, weights_path):
	"""
	Creates the neural network.

	Arguments:
		mode 					the evaluation mode: train, test, or application
		layer_sizes				the sizes of the input, hidden, and output layers
		use_stored_weights		whether or not to initialise the network with stored weights
		weights_path			the path where the weights are stored at or retrieved from (depending on
			 					whether use_stored_weights is False or True, respectively)
	"""

	if verbose:
		print('model_tensorflow.create_neural_network() called')

	num_layers = len(layer_sizes) - 1
	weights = {}
	biases = {}

	if verbose:
		print('Creating a DNN with', str(num_layers-1), 'hidden layers of size', str(layer_sizes[1:len(layer_sizes)-1]))

	# Initialise the weights
	# (a) Create new weights and biases
	if not use_stored_weights:
		for i in range(num_layers):            
			# Layer l has dimensions (|l-1|, |l|) for weights and (|l|) for biases
			w_name = 'W' + str(i+1)
			weights[w_name] = tf.get_variable(w_name, [layer_sizes[i], layer_sizes[i+1]], 
				initializer = tf.contrib.layers.xavier_initializer(), dtype=tf.float32)
			b_name = 'b' + str(i+1)
			biases[b_name] = tf.get_variable(b_name, [layer_sizes[i+1]], 
				initializer = tf.zeros_initializer(), dtype=tf.float32)

		# Initialise all existing global variables 
		sess.run(tf.global_variables_initializer())

		# Save weights and biases
		saver = tf.train.Saver()
		save_path = saver.save(sess, weights_path + 'weights/' + 'init.ckpt') 
	# (b) Restore existing weights and biases
	else:
		for i in range(num_layers):
			# Prepare variable
			w_name = 'W' + str(i+1)
			b_name = 'b' + str(i+1)
			weights[w_name] = tf.get_variable(w_name, [layer_sizes[i], layer_sizes[i+1]], 
				initializer = tf.zeros_initializer(), dtype=tf.float32)
			biases[b_name] = tf.get_variable(b_name, [layer_sizes[i+1]], 
				initializer = tf.zeros_initializer(), dtype=tf.float32)

		# Initialise all existing global variables 
		sess.run(tf.global_variables_initializer())

		# Restore weights and biases
		saver = tf.train.Saver()
		if mode == train:
			saver.restore(sess, weights_path + 'weights/' + 'init.ckpt')            
		elif mode == test or mode == appl:
			saver.restore(sess, weights_path + 'weights/' + 'trained.ckpt')

	wb = {'weights': weights, 'biases': biases}
	return wb


def evaluate_neural_network(data, keep_prob, num_layers, seed, weights, biases):
	
	"""
	Evaluates the neural network.

	Arguments:
		data					the features
		keep_prob				the dropout keep probability placeholder
		num_layers				the number of hidden and output layers
		weights 				the weights
		biases 					the biases
	"""

	if verbose:
		print('model_tensorflow.evaluate_neural_network() called')

	# Calculate linear and ReLU outputs for the hidden layers
	a_prev = data
	for i in range(num_layers-1):
		z = tf.add(tf.matmul(a_prev, weights['W' + str(i+1)]), biases['b' + str(i+1)])
		a = tf.nn.relu(z)
		a_r = tf.nn.dropout(a, keep_prob, seed=seed)
		a_prev = a_r
	# Calculate linear output for the output layer
	z_o = tf.add(tf.matmul(a_prev, weights['W' + str(num_layers)]), biases['b' + str(num_layers)])

	return z_o


def run_neural_network(mode, arg_placeholders, arg_data, arg_hyperparams, arg_paths_extensions, **kwargs):
	"""
	Runs the neural network.

	Arguments:
		mode 					the evaluation mode: train, test, or application
		arg_placeholders		the placeholders dictionary
		arg_data				the data dictionary
		arg_hyperparams 		the hyperparams dictionary
		arg_paths_extensions 	the paths and extensions dictionary, with at keys
								- 'store_path': the store path, i.e., the path where 
								  - the weights are stored at or retrieved from
								  - the outputs and additional information (.txt files, figures) are stored at
								  - the stored features are retrieved from (only when mode is application)
								- 'path_trained_user_model': the path to the trained model, i.e., ...  
		*kwargs 				the kwargs dictionary (only when mode is application), with at keys 
		                        - 'weights_biases': the weights and biases dictionary, with at keys
		                           - 'weights': the weights dictionary, with at keys 'W1', 'W2', ... the weights 
		                                        Variables (see create_neural_network()) 
		                           - 'biases':  the biases dictionary, with at keys 'b1', 'b2', ... the biases 
		                                        Variables (see create_neural_network()) 
		                        - 'feature_vector': the feature vector, a list of floats 
	"""

	if verbose:
		print('model_tensorflow.run_neural_network() called')

	# Placeholders
	x, y = arg_placeholders['x'], arg_placeholders['y'] 
	keep_prob = arg_placeholders['keep_prob']
	# Data
	x_train, y_train, x_val, y_val = (arg_data['x_train'], arg_data['y_train'], 
									  arg_data['x_val'], arg_data['y_val'])
	x_test, y_test = arg_data['x_test'], arg_data['y_test']
	# Hyperparameters
	use_stored_weights, user_model = (arg_hyperparams['use_stored_weights'], 
									  arg_hyperparams['user_model'])
	layer_sizes, val_perc, mini_batch_size, epochs, seed = (arg_hyperparams['layer_sizes'], 
															arg_hyperparams['val_perc'],
															arg_hyperparams['mini_batch_size'], 
															arg_hyperparams['epochs'], 
															arg_hyperparams['seed'])
	lrn_rate, kp = arg_hyperparams['lrn_rate'], arg_hyperparams['kp']
	# Paths and extensions 
	store_path, out_ext, fv_ext = (arg_paths_extensions['store_path'], 
								   arg_paths_extensions['out_ext'], 
								   arg_paths_extensions['fv_ext'])

	weights_biases = {}
	if mode == train or mode == test:
		weights_biases = create_neural_network(mode, layer_sizes, use_stored_weights, store_path)
	elif mode == appl:
#		weights_biases = args[0]
		weights_biases = kwargs['weights_biases']

#	print('(1) initial weights W1:')
#	print('W1', sess.run(weights_biases['weights']['W1'][0]))

	# To calculate predictions (linear output from the network's output layer) and softmaxes (trn, tst, app)
	prediction = evaluate_neural_network(x, keep_prob, len(layer_sizes) - 1, seed,
										 weights_biases['weights'], weights_biases['biases']) # Tensor
	softm = tf.nn.softmax(prediction) # Tensor
	pred_class = tf.argmax(softm) # Tensor

	# To calculate accuracy (trn, tst, app)
	correct = tf.equal(tf.argmax(prediction, 1), tf.argmax(y, 1)) # Tensor
	accuracy = tf.reduce_mean(tf.cast(correct, 'float')) # Tensor

	if mode == train or mode == test:
#		# To calculate accuracy (trn, tst)
#		correct = tf.equal(tf.argmax(prediction, 1), tf.argmax(y, 1)) # Tensor
#		accuracy = tf.reduce_mean(tf.cast(correct, 'float')) # Tensor
		
		if mode == train:
			# Declare cost and optimizer here: optimizer has global variables that must be initialised (see below)
			cost = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(logits=prediction, labels=y)) # Tensor
			optimizer = tf.train.AdamOptimizer(learning_rate=lrn_rate).minimize(cost) # Operation

		# Initialise all global variables that have not been initialised yet (e.g., variables for Adam). See 
		# https://stackoverflow.com/questions/35164529/in-tensorflow-is-there-any-way-to-just-initialize-uninitialised-variables
		# (answer by Salvador Dali) 
		global_vars = tf.global_variables()
		is_not_initialized = sess.run([tf.is_variable_initialized(var) for var in global_vars])
		not_initialized_vars = [v for (v, f) in zip(global_vars, is_not_initialized) if not f]
		if verbose:
			print('uninitialised variables:', [str(i.name) for i in not_initialized_vars])
		if len(not_initialized_vars):
			sess.run(tf.variables_initializer(not_initialized_vars))
		if verbose:
			print('uninitialised variables:', sess.run(tf.report_uninitialized_variables()))

		saver = tf.train.Saver()

	if mode == train:
		if val_perc != 0:
			# To calculate predictions (linear output from the network's output layer) and softmaxes (vld)
			prediction_val = evaluate_neural_network(x, keep_prob, len(layer_sizes) - 1, seed,
													 weights_biases['weights'], weights_biases['biases']) # Tensor
			softm_val = tf.nn.softmax(prediction_val) # Tensor
			pred_class_val = tf.argmax(softm_val) # Tensor

			# To calculate accuracy (vld)
			correct_val = tf.equal(tf.argmax(prediction_val, 1), tf.argmax(y, 1)) # Tensor
			accuracy_val = tf.reduce_mean(tf.cast(correct_val, 'float')) # Tensor

		total_cost = []
		accs_trn = []
		accs_val = []
		best_acc = 0.0
#		print('(2) weights W1 before training (should be the same as (1))')
#		print('W1', sess.run(weights_biases['weights']['W1'][0]))
		
		for epoch in range(epochs): # one epoch is one fwd-bwd propagation over the complete dataset
			epoch_loss = 0
			for _ in range(int(len(x_train)/mini_batch_size)):
				epoch_x, epoch_y = x_train, y_train # np.ndarray
				_, c, acc_trn, sm_trn = sess.run([optimizer, cost, accuracy, softm], 
												 feed_dict = {x: epoch_x, y: epoch_y, keep_prob: kp})
				epoch_loss += c

				if epoch == 10 or epoch == 20:
					print('MANUAL CHECK TRAIN')
					print('acc_trn   :', acc_trn)
					check_accuracy(epoch_x, epoch_y, sm_trn)

			# In case of mini-batch gradient descent, accumulate the results from the mini batches
			# acc_trn = ...
			# sm_trn_comb = ...
			# sm_trn = sm_trn_comb 

			print('epoch', str(epoch) + '/' + str(epochs), 'completed; loss =', epoch_loss)		
#			print('(3) updated weights W1 after one training epoch (should be different from (2))')
#			print('W1', sess.run(weights_biases['weights']['W1'][0]))
			total_cost.append(epoch_loss)
			accs_trn.append(acc_trn)

			# Non-user model (model selection) case: save weights and softmaxes for the current epoch  
			# if its acc_val is the highest so far. Check acc_val every tenth epoch
			if not user_model and epoch % 10 == 0:
				if val_perc != 0:
					if arg_hyperparams['ismir_2018']:
						# This is incorrect: sess.run() should not be run again (see loop over the mini 
						# batches) on accuracy and softm, which are for calculating trn results, but on 
						# accuracy_val and softm_val. Rerunning leads to unwanted changes in tensor calculations
						# NB: for the ISMIR paper, sm_val is not calculated
						acc_val, sm_val = sess.run([accuracy, softm],
											   		feed_dict={x: x_val, y: y_val, keep_prob: 1.0})
					else:
						acc_val, sm_val = sess.run([accuracy_val, softm_val],
											   		feed_dict={x: x_val, y: y_val, keep_prob: 1.0})
					accs_val.append(acc_val)

					if epoch == 10 or epoch == 20:
						print('MANUAL CHECK VALIDATION')
						print('acc_val   :', acc_val)
						check_accuracy(x_val, y_val, sm_val)

					if acc_val > best_acc:
						best_acc = acc_val
						
						# Save weights
						save_path = saver.save(sess, store_path + 'weights/' + 'trained.ckpt')

						# Save softmaxes
						if arg_hyperparams['ismir_2018']:
							# This is incorrect: sess.run() should not be run again (see loop over the mini 
							# batches) on softm. Rerunning leads to unwanted changes in tensor calculations 
							softmaxes_trn = sess.run([softm, pred_class], 
													 feed_dict={x: x_train, keep_prob: kp})[0]
							np.savetxt(store_path + out_ext, softmaxes_trn, delimiter=',')
						else:
							np.savetxt(store_path + out_ext, sm_trn, delimiter=',')
						np.savetxt(store_path + out_ext.replace('trn', 'vld'), sm_val, delimiter=',')

						# Save epoch info
						with open(store_path + 'best_epoch.txt', 'w') as text_file:
							text_file.write('highest accuracy on the validation set (' + 
											str(best_acc) + ') in epoch ' + str(epoch))
						np.savetxt(store_path + 'best_epoch.csv', [[int(epoch), acc_val]], delimiter=',')

		# User model case: save weights and softmaxes for the final epoch   
		if user_model:
			# Save weights
			save_path = saver.save(sess, store_path + 'weights/' + 'trained.ckpt')
			# Save softmaxes
			np.savetxt(store_path + out_ext, sm_trn, delimiter=',')

		# Plot the trn and vld accuracy
		if plot_or_not:
			plt.plot(np.squeeze(accs_trn))
			plt.plot(np.squeeze(accs_val))
			plt.ylabel('acc')
			plt.xlabel('epochs (/10)')
			ax = plt.subplot(111)
			ax.set_prop_cycle('color', ['red', 'green'])
#			plt.gca().set_prop_cycle(['red', 'green'])
			plt.title('accuracy on training and validation set')
			plt.legend(['trn', 'vld'], loc='lower right')
			plt.savefig(store_path + 'trn_and_val_acc.png')

	if mode == test:
		acc_tst, sm_tst = sess.run([accuracy, softm], feed_dict={x: x_test, y: y_test, keep_prob: 1.0})
		# Save softmaxes
		np.savetxt(store_path + out_ext, sm_tst, delimiter=',')
 
		print('MANUAL CHECK TEST')
		print('acc_tst   :', acc_tst)
		check_accuracy(x_test, y_test, sm_tst)

	if mode == appl:
		load_save_files = False
		if load_save_files:
			# Features loaded from file
			x_appl = genfromtxt(store_path + fv_ext, delimiter=',')
			# Features given as argument		
#			list_from_string = [float(s.strip()) for s in feature_vector.split(',')]
#			x_appl = np.array(list_from_string)
			# Reshape necessary to get required shape (1, 33)
			x_appl = x_appl.reshape(1, -1)
			print(x_appl.shape)
			sm_app = sess.run([softm, pred_class], feed_dict={x: x_appl, keep_prob: 1.0})[0]
#			acc_app, sm_app2 = sess.run([accuracy, softm], feed_dict={x: x_app, y: y_app, keep_prob: 1.0})
			np.savetxt(store_path + out_ext, sm_app, delimiter=',')
		else:
			x_app = np.array(kwargs['feature_vector'])
			# Reshape necessary to get required shape (1, 33)
			x_app = x_app.reshape(1, -1)
			sm_app = sess.run(softm, feed_dict={x: x_app, keep_prob: 1.0}) # np.ndarray
#			np.savetxt(store_path + out_ext, sm_app, delimiter=',')
			print(sm_app)
			return sm_app[0]


def check_accuracy(x, y, sm):
	if len(x) != len(sm):
		exit()
	num_ex = len(x)
	incorr = 0
	for i in range(len(sm)):
		if np.argmax(sm[i]) != np.argmax(y[i]):
			incorr += 1
	print('acc manual:', (num_ex - incorr)/num_ex)
	print('incorr, num_ex:', incorr, num_ex)


def parse_argument_strings(mode_str, hyperparams_str, paths_extensions_str):
	# Mode
	mode = {'trn': train, 'tst': test, 'app': appl}[mode_str]

	# Paths and extensions (keys are defined in Java code)
	paths_extensions = {item[:item.index('=')].strip():item[item.index('=')+1:].strip() 
						for item in paths_extensions_str.split(',')}

	# Hyperparameters (keys are defined in Java code)
	hyperparams = {}
	for item in [s.strip() for s in hyperparams_str.split(',')]:
		key = item.strip().split('=')[0]
		val = item.strip().split('=')[1]
		# Bools
		if val in ['True', 'true', 'False', 'false']:
			hyperparams[key] = True if val.casefold() == 'true' else False
		# Lists (space-separated)
		elif '[' in val:
			hyperparams[key] = [float(s) if '.' in s else int(s) for s in val[1:-1].split(' ')]
		# Floats and ints
		else:
			hyperparams[key] = float(val) if '.' in val else int(val) 

	# Data
	data = {key : None for key in ['x_train', 'y_train', 'x_val', 'y_val', 'x_test', 'y_test']}
	sp, fv_ext, lbl_ext = [paths_extensions['store_path'], paths_extensions['fv_ext'], paths_extensions['lbl_ext']]  
	if mode == train:
		# See https://stackoverflow.com/questions/13499824/using-map-function-with-keyword-arguments
		# and https://www.geeksforgeeks.org/python-map-function/ (Code 2)
#		mapfunc = partial(genfromtxt, delimiter=',')
#		data['x_train'], data['y_train'] = map(mapfunc, [store_path + fv_ext, store_path + lbl_ext])
		data['x_train'], data['y_train'] = map(lambda path: genfromtxt(path, delimiter=','), 
											   [sp + fv_ext, sp + lbl_ext])
		if hyperparams['val_perc'] != 0:
			data['x_val'], data['y_val'] = map(lambda path: genfromtxt(path, delimiter=','), 
											   [sp + fv_ext.replace('trn', 'vld'), 
											    sp + lbl_ext.replace('trn', 'vld')])
	elif mode == test:
		data['x_test'], data['y_test'] = map(lambda path: genfromtxt(path, delimiter=','), 
											 [sp + fv_ext, sp + lbl_ext])  
#		data['x_test'] = genfromtxt(sp + fv_ext, delimiter=',')
#		if not hyperparams['user_model']:
#			data['y_test'] = genfromtxt(sp + lbl_ext, delimiter=',')

	# Placeholders
	placeholders = {key : None for key in ['x', 'y', 'keep_prob']}
	placeholders['x'] = tf.placeholder('float', [None, hyperparams['layer_sizes'][0]])
	placeholders['y'] = tf.placeholder('float')
	placeholders['keep_prob'] = tf.placeholder('float')

#	sess = tf.InteractiveSession()

	return mode, paths_extensions, hyperparams, data, placeholders


def start_sess():
	# Interactive session needed to be able to share variables between functions; 'with tf.Session() as sess:' does not work.
	# See https://www.tensorflow.org/api_docs/python/tf/compat/v1/InteractiveSession
	# Example of passing around sessions at  
	# https://stackoverflow.com/questions/44660572/function-scopes-with-tensorflow-sessions
	global sess
	sess = tf.InteractiveSession()


def main():
	script, mode_str, hyperparams_str, paths_extensions_str = argv

	tf.reset_default_graph()
	mode, paths_extensions, hyperparams, data, placeholders = parse_argument_strings(mode_str, 
																					 hyperparams_str, 
																					 paths_extensions_str)

	if verbose:
		print('model_tensorflow.main() called in mode', mode)
		print('paths_extensions:')
		for k, v in paths_extensions.items():
			print(k, v)
		print('hyperparams:')
		for k, v in hyperparams.items():
			print(k, v)
		print('placeholders:')
		for k, v in placeholders.items():
			print(k, v)
		print()

	# Set seed to ensure deterministic dropout
	# see https://stackoverflow.com/questions/49175704/tensorflow-reproducing-results-when-using-dropout
	tf.set_random_seed(hyperparams['seed'])

	# Start session
	start_sess()

	run_neural_network(mode, placeholders, data, hyperparams, paths_extensions)

	sess.close()


def old_main():
	# Set variables
	# a. When this script is used as a script, i.e., run directly (trn, tst)
	if len(argv) > 1:
		script, mdl, arg_mode, arg_store_path, arg_path_trained_user_model, arg_exts, arg_user_model, arg_use_stored_weights, arg_hyperparams = argv

		"""
		This script trains the given model on the data at the given path, and then stores
		both the output and the weights at the same path. 

		Arguments
		mdl							the model used
		arg_mode 					the mode: train, test, or application
		arg_store_path				the path where to store the data and model
		arg_path_trained_user_model	
		arg_exts					the extension used for the feature vectors, labels, and model outputs
		arg_user_model				whether or not a trained user model is employed
		arg_use_stored_weights		whether or not to initialise the network with the stored initial weights
		arg_hyperparams				the hyperparameters, some of them to be tuned
		"""

		if verbose:
			print('model_tensorflow.py called as a script')

		# Extract global variables
#		mode = {'trn': train, 'tst': test, 'app': appl}[arg_mode]
#		fold_path = arg_store_path # TODO rename. fold_path is the path where the stored features and labels are retrieved from
#		path_trained_user_model = arg_path_trained_user_model # TODO work into code
#		exts = [s.strip() for s in arg_exts.split(',')]
#		fv_ext, lbl_ext, out_ext = [e + (arg_mode if not (mode == test and 'B' in mdl) else 
#									 'app') + '.csv' for e in exts] # In case of a B model, test mode is application mode
#		user_model = True if arg_user_model.casefold() == 'true' else False
#		use_stored_weights = True if arg_use_stored_weights.casefold() == 'true' else False
#		hyperparams = [s.strip() for s in arg_hyperparams.split(',')]
#		hyperparams = {item.strip().split('=')[0]:(float(item.strip().split('=')[1]) if '.' in item.strip().split('=')[1] else 
#												  int(item.strip().split('=')[1])) for item in hyperparams}
#		num_HL 		= hyperparams['hidden layers']
#		IL_size 	= hyperparams['input layer size']
#		HL_size 	= hyperparams['hidden layer size']
#		OL_size 	= hyperparams['output layer size']
#		lrn_rate 	= hyperparams['learning rate']
#		kp 			= hyperparams['keep probability']
#		epochs 		= hyperparams['epochs']
#		seed 		= hyperparams['seed']
#		val_perc	= hyperparams['validation percentage']
#		layer_sizes = [IL_size] + [HL_size] * num_HL + [OL_size]

#		if verbose:
#			print('mode:', mode)
#			print('fold_path:', fold_path)
#			print('path_trained_user_model:', path_trained_user_model)
#			print('fv_ext, lbl_ext, out_ex:', fv_ext + ', ' + lbl_ext + ', ' + out_ext)
#			print('user_model:', user_model)
#			print('use_stored_weights:', use_stored_weights)
#			print('num_HL:', num_HL)
#			print('IL_size, HL_size, OL_size:', str(IL_size) + ', ' + str(HL_size) + ', ' + str(OL_size))
#			print('lrn_rate:', lrn_rate)
#			print('kp:', kp)
#			print('epochs:', epochs)
#			print('seed:', seed)
#			print('val_perc:', val_perc)
#			print('layer_sizes:', layer_sizes)
#			print()
	# b. When this script is used as a module, i.e., imported by iPython or another .py file (app)
	else:
		if verbose:
			print('model_tensorflow.py called as a module')
#		seed=-1 #'set_me'
#		script = argv
#		mode = appl
#		fv_ext = 'x-app.csv'
#		lbl_ext = 'y-app.csv' # currently not used in Python code
#		out_ext = 'out-app.csv'
#		user_model = True # in app mode only used when it is True 

	tf.reset_default_graph()

#	if mode == train:
#		x_train = genfromtxt(store_path + fv_ext, delimiter=',')
#		y_train = genfromtxt(store_path + lbl_ext, delimiter=',')
#		if val_perc != 0:
#			x_val = genfromtxt(store_path + fv_ext.replace('trn', 'vld'), delimiter=',')
#			y_val = genfromtxt(store_path + lbl_ext.replace('trn', 'vld'), delimiter=',')
#		mini_batch_size = len(x_train)  
#	elif mode == test:
#		x_train, y_train = None, None
#		x_val, y_val = None, None
#		x_test = genfromtxt(store_path + fv_ext, delimiter=',')
#		if not user_model:
#			y_test = genfromtxt(store_path + lbl_ext, delimiter=',') # currently not used in Python code
#		mini_batch_size = None
#		use_stored_weights = True
##	elif mode == appl:
##		x_appl  = genfromtxt(fold_path + fv_ext, delimiter=',')
##		y_appl  = genfromtxt(fold_path + lbl_ext, delimiter=',')
##		use_stored_weights = True

#	if mode == train or mode == test:
#		x = tf.placeholder('float', [None, IL_size])
#		y = tf.placeholder('float')
#		keep_prob = tf.placeholder('float')
		
	# Interactive session needed to be able to share variables between functions; 'with tf.Session() as sess:' does not work.
	# See https://www.tensorflow.org/api_docs/python/tf/InteractiveSession
	# Example of passing around sessions at  
	# https://stackoverflow.com/questions/44660572/function-scopes-with-tensorflow-sessions
	sess = tf.InteractiveSession()

	# Set seed to ensure deterministic dropout
	# see https://stackoverflow.com/questions/49175704/tensorflow-reproducing-results-when-using-dropout
	tf.set_random_seed(seed)

	if mode == train or mode == test:
		print('==========>> start trn/tst')
		run_neural_network(x, y, x_train, y_train, x_val, y_val, x_test, y_test, val_perc, keep_prob, lrn_rate, kp, epochs, layer_sizes, mini_batch_size, seed, user_model, use_stored_weights, mode, store_path, out_ext, sess)
#	elif mode == appl:
#		weights_biases = create_neural_network(layer_sizes, use_stored_weights, mode, fold_path)
#		# https://pythontips.com/2013/08/04/args-and-kwargs-in-python-explained/
#		run_neural_network(x, keep_prob, lrn_rate, kp, epochs, layer_sizes, use_stored_weights, mode, fold_path, weights_biases)
	
	if mode == appl:
		weights_biases = create_neural_network(mode, layer_sizes, use_stored_weights, store_path, sess)
		run_neural_network(x, y, x_train, y_train, x_val, y_val, x_test, y_test, val_perc, keep_prob, lrn_rate, kp, epochs, layer_sizes, mini_batch_size, seed, user_model, use_stored_weights, mode, store_path, out_ext, sess, weights_biases)

	sess.close()


def make_validation_set(x_tr, y_tr, perc):
	"""
	Takes every nth example from the training set and puts it into a validation set. n is determined by the given 
	percentage: n = 100/perc  

	Arguments:
		x_tr					the features
		y_tr					the labels
		perc					the percentage of training examples that go into the validation set
	"""

	x_val, y_val, x_tr_new, y_tr_new = [], [], [], []
	n = 100/perc
	for i in range(len(x_tr)):
		if i % n == 0:
			x_val.append(x_tr[i])
			y_val.append(y_tr[i])
		else:
			x_tr_new.append(x_tr[i])
			y_tr_new.append(y_tr[i])
 
	arrs = {'x-val': np.array(x_val), 
			'y-val': np.array(y_val), 
			'x-tr-new': np.array(x_tr_new), 
			'y-tr-new': np.array(y_tr_new)}
	return arrs


if __name__ == '__main__':
	main()