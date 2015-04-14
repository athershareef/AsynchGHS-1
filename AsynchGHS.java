/**
 * Program to implement Asynchronous GHS algorithm.
 * Team:
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

class ProcessNode {
	int processIndex;
	int processId;
	float processDistance;

	ProcessNode() {

	}

	ProcessNode(int processIndex, float processDistance) {
		this.processIndex = processIndex;
		this.processDistance = processDistance;
	}

	float getProcessDistance() {
		return this.processDistance;
	}

	int getProcessIndex() {
		return this.processIndex;
	}

	int getProcessId() {
		return this.processId;
	}

	void setProcessId(int processId) {
		this.processId = processId;
	}

}

class MessageGenerator {

	int delay;
	String messageType;
	String message;
	int level;
	int componentId;
	int sender;
	int senderId;
	int mwoeFoundProcess;
	boolean mwoeFound;
	Float mwoeLength;
	int connectReadyTargetId;
	int coreEdgeProcess1ID;
	Float coreEdgeLength;
	int coreEdgeProcess2ID;
	int messageNumber;

	MessageGenerator(int delay, String messageType, int sender, int senderId) {
		this.delay = delay;
		this.messageType = messageType;
		this.message = null;
		this.level = 0;
		this.componentId = 0;
		this.sender = sender;
		this.senderId = senderId;
		this.mwoeLength = null;
		this.connectReadyTargetId = 0;
	}

	int getDelay() {
		return this.delay;
	}

	void decDelay() {
		this.delay--;
	}

	void setDelay(int delay) {
		this.delay = delay;
	}

	String getMessageType() {
		return this.messageType;
	}

	String getMessage() {
		return this.message;
	}

	void setMessage(String message) {
		this.message = message;
	}

	int getLevel() {
		return this.level;
	}

	void setLevel(int level) {
		this.level = level;
	}

	int getComponentId() {
		return this.componentId;
	}

	void setComponentId(int componentId) {
		this.componentId = componentId;
	}

	int getSender() {
		return this.sender;
	}

	int getSenderId() {
		return this.senderId;
	}

	void setSenderId(int senderId) {
		this.senderId = senderId;
	}

	void setMwoeLFoundProcess(int mwoeFoundProcess) {
		this.mwoeFoundProcess = mwoeFoundProcess;
	}

	int getMwoeFoundProcess() {
		return this.mwoeFoundProcess;
	}

	void setMwoeFound(boolean mwoeFound) {
		this.mwoeFound = mwoeFound;
	}

	boolean getMwoeFound() {
		return this.mwoeFound;
	}

	void setMwoeLength(Float mwoeLength) {
		this.mwoeLength = mwoeLength;
	}

	Float getMwoeLength() {
		return this.mwoeLength;
	}

	void setConnectReadyTargetId(int targetId) {
		this.connectReadyTargetId = targetId;
	}

	int getConnectReadyTargetId() {
		return this.connectReadyTargetId;
	}

	int getCoreEdgeProcess1Id() {
		return this.coreEdgeProcess1ID;
	}

	int getCoreEdgeProcess2Id() {
		return this.coreEdgeProcess2ID;
	}

	float getCoreEdgeLength() {
		return this.coreEdgeLength;
	}

	void setCoreEdgeProcess1Id(int coreEdgeProcess1Id) {
		this.coreEdgeProcess1ID = coreEdgeProcess1Id;
	}

	void setCoreEdgeProcess2Id(int coreEdgeProcess2Id) {
		this.coreEdgeProcess2ID = coreEdgeProcess2Id;
	}

	void setCoreEdgeLength(float coreEdgeLength) {
		this.coreEdgeLength = coreEdgeLength;
	}

	void setMessageNumber(int messageNumber) {
		this.messageNumber = messageNumber;
	}

	int getMessageNumber() {
		return this.messageNumber;
	}

}

public class AsynchGHS implements Runnable {

	private int tName;
	private int[] tNeighbours;
	ArrayList<ArrayBlockingQueue<Integer>> queue;
	ArrayList<ArrayList<ArrayBlockingQueue<MessageGenerator>>> tdque;
	ArrayBlockingQueue<MessageGenerator> masterQueue;
	int numOfNeighbours = 0;
	int parentId, newParent, newParentIndex;
	int myIndex, parentIndex;
	ArrayList<Integer> children = new ArrayList<Integer>();
	// ArrayList<processNode> children = new ArrayList<processNode>();
	int noOfChildren;

	int level;
	int coreEdgeProcess1Id;
	float coreEdgeLength;
	int coreEdgeProcess2Id;

	boolean connectSent;
	boolean initiateSent, initiateRecieved, initiateProcessed,
			initiateForwarded;
	boolean reportSent, acceptSent, rejectSent;
	boolean waitingForMwoeReport, waitingForMyMwoeReport, waitingForAccept,
			childrenMwoeReportReady, myMwoeReportReady;
	boolean previousStepCompleted, processConnectMessages, mwoeFound,
			selfMwoeReportDone;
	boolean waitingForMergeCompletion, pendingTestMessage;
	boolean leader;
	int connectSentNode;
	int mwoeFoundProcess;
	float weightOfMWOE;
	int messageNumber;

	PriorityQueue<MessageGenerator> internalQ = new PriorityQueue<MessageGenerator>(
			20, idComparator);

	Queue<ProcessNode> basicNeighbours = null;
	List<ProcessNode> branchNeighbours = new LinkedList<ProcessNode>();
	List<ProcessNode> rejectNeighbours = new LinkedList<ProcessNode>();

	List<MessageGenerator> pendingConnectMessages = new LinkedList<MessageGenerator>(); // Connect
																						// Messages
																						// which
																						// were
																						// received
																						// during
																						// the
																						// step
																						// processing
	List<MessageGenerator> pendingTestMessageList = new LinkedList<MessageGenerator>();
	HashMap<Integer, Integer> mwoeReportReceived = new HashMap<Integer, Integer>(); // To
																					// store
																					// the
																					// mwoe
																					// confirmation
																					// received
																					// message

	// Constructor
	public AsynchGHS(int myIndex, int Name, float[] Neighbours,
			ArrayList<ArrayBlockingQueue<Integer>> bqueue,
			ArrayList<ArrayList<ArrayBlockingQueue<MessageGenerator>>> tdque,
			ArrayBlockingQueue<MessageGenerator> mqueue) {
		this.myIndex = myIndex;
		this.tName = Name;
		this.queue = bqueue;
		this.tdque = tdque;
		this.masterQueue = mqueue;
		this.parentId = tName;
		this.parentIndex = myIndex;
		this.noOfChildren = 0;
		this.level = 0;
		this.connectSent = false;
		this.previousStepCompleted = true;
		this.waitingForMergeCompletion = false;
		this.waitingForMwoeReport = false;
		this.waitingForMyMwoeReport = false;
		// Initialization of Component Triplet Identifier
		this.coreEdgeProcess1Id = tName;
		this.coreEdgeLength = 0;
		this.coreEdgeProcess2Id = tName;
		this.messageNumber = 0;
		this.mwoeFound = false;

		basicNeighbours = new PriorityQueue<ProcessNode>(Neighbours.length,
				processComparator);

		for (int i = 0; i < Neighbours.length; i++) {
			if (Neighbours[i] != 0) {
				this.numOfNeighbours++;
			}
		}

		this.tNeighbours = new int[numOfNeighbours];
		int j = 0;
		for (int i = 0; i < Neighbours.length; i++) {
			if (Neighbours[i] != 0) {
				this.tNeighbours[j] = i;
				j++;
				ProcessNode neighbourNode = new ProcessNode(i, Neighbours[i]);
				basicNeighbours.add(neighbourNode);
			}
		}
	}

	public static Comparator<MessageGenerator> idComparator = new Comparator<MessageGenerator>() {
		public int compare(MessageGenerator c1, MessageGenerator c2) {
			if (c1.getSender() == c2.getSender()) {
				return (int) (c1.getMessageNumber() - c2.getMessageNumber());
			} else {
				return (int) (c1.getDelay() - c2.getDelay());
			}
		}
	};

	public static Comparator<ProcessNode> processComparator = new Comparator<ProcessNode>() {
		public int compare(ProcessNode c1, ProcessNode c2) {
			return (int) (c1.getProcessDistance() - c2.getProcessDistance());
		}
	};

	// Thread Implementation
	public void run() {

		// Display the neighbours
		// System.out.println("This is the child thread: " + this.tName);
		for (ProcessNode node : basicNeighbours) {
			// System.out.println("The Neighbours of thread " + this.tName +
			// " are: " + node.processIndex + ", at a distance of " +
			// node.processDistance );
		}

		while (true) {
			// Poll the master blocking queue. Wait till a message is received
			// from master.
			MessageGenerator messageFromMain = null;
			while (messageFromMain == null) {
				messageFromMain = tdque.get(myIndex).get(myIndex).poll();
				if (messageFromMain != null) {
					// System.out.println("The message from main in thread " +
					// this.myIndex + " is: " +
					// messageFromMain.getMessageType());
					break;
				} else {
					// System.out.println("Received null from Master");
				}
			}

			// Once the master sends a message, check if its a tick message. If
			// yes, execute.
			if (messageFromMain.getMessageType().equals("tick")) {

				// Check for messages from any of the Neighbours. If there is a
				// message copy that into the internal priority queue
				for (int i = 0; i < numOfNeighbours; i++) {
					MessageGenerator rec_message = tdque.get(tNeighbours[i])
							.get(myIndex).poll();
					if (rec_message != null) {
						internalQ.add(rec_message);

						System.out.println("Message Received at " + this.tName
								+ " from Sender " + rec_message.getSenderId()
								+ " is: " + rec_message.getMessage());
						// + " with a delay: " + rec_message.getDelay() +
						// " with message number: " +
						// rec_message.getMessageNumber());
					}
				}

				// Decrement the delay of the messages in the Priority Queue
				for (MessageGenerator eachMessage : internalQ) {
					if (eachMessage.getDelay() != 0) {
						eachMessage.decDelay();
					}
				}

				// Check whether priority queue has any message
				MessageGenerator frontMessage = internalQ.peek();
				if (frontMessage != null) {

					// If there is a message, check whether its delay is zero.
					// If yes, remove it from the queue and process it.
					if (frontMessage.getDelay() == 0) {

						frontMessage = internalQ.remove();
						// System.out.println("Message read at " + this.tName +
						// " from the queue is: " + frontMessage.getMessage() +
						// ", from Sender" + frontMessage.getSenderId() +
						// ", Message Left in Queue: " + internalQ.size());

						// If the received message is a connect message
						if (frontMessage.getMessageType().equals("connect")) {

							pendingConnectMessages.add(frontMessage);
							processConnectMessages = true;

							// System.out.println("Process" + tName +
							// " inserting connect message in pendingConnectMessages list ");

						} else if (frontMessage.getMessageType().equals(
								"absorb")) {
							// If received message is a absorb message

							// Update the Component identifier of this Process
							newParent = frontMessage.getSenderId();
							newParentIndex = frontMessage.getSender();

							System.out.println("Component: ( "
									+ coreEdgeProcess1Id + ", "
									+ coreEdgeLength + ", "
									+ coreEdgeProcess2Id
									+ " )  gets absorbed into component: ( "
									+ frontMessage.getCoreEdgeProcess1Id()
									+ ", " + frontMessage.getCoreEdgeLength()
									+ ", "
									+ frontMessage.getCoreEdgeProcess2Id()
									+ " )");

							coreEdgeProcess1Id = frontMessage
									.getCoreEdgeProcess1Id();
							coreEdgeProcess2Id = frontMessage
									.getCoreEdgeProcess2Id();
							coreEdgeLength = frontMessage.getCoreEdgeLength();
							level = frontMessage.getLevel();

							leader = false;

							// Forward the propagate message to Parent if any
							if (parentId != tName) {
								Random rand = new Random();
								int delay = rand.nextInt((20 - 0) + 1) + 0;
								MessageGenerator propagateMessage = new MessageGenerator(
										delay, "propagate", myIndex, tName);
								propagateMessage
										.setCoreEdgeProcess1Id(coreEdgeProcess1Id);
								propagateMessage
										.setCoreEdgeProcess2Id(coreEdgeProcess2Id);
								propagateMessage
										.setCoreEdgeLength(coreEdgeLength);
								propagateMessage.setLevel(level);
								propagateMessage
										.setMessageNumber(messageNumber++);
								propagateMessage
										.setMessage("This is a propagate message sent after merging");
								try {
									tdque.get(myIndex).get(parentIndex)
											.put(propagateMessage);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								// System.out.println("Process" + tName +
								// " Sending propagate message to " + parentId);
							}

							// Send Update Component & level Message to its
							// children

							String message = "This is a updateComponent Message";
							Random rand = new Random();
							int delay = rand.nextInt((20 - 0) + 1) + 0;
							MessageGenerator updateComponentMessage = new MessageGenerator(
									delay, "updateComponent", myIndex, tName);
							updateComponentMessage.setMessage(message);
							updateComponentMessage
									.setCoreEdgeProcess1Id(coreEdgeProcess1Id);
							updateComponentMessage
									.setCoreEdgeProcess2Id(coreEdgeProcess2Id);
							updateComponentMessage
									.setCoreEdgeLength(coreEdgeLength);
							updateComponentMessage.setLevel(level);
							updateComponentMessage
									.setMessageNumber(messageNumber++);
							for (Integer child : children) {
								try {
									tdque.get(myIndex).get(child)
											.put(updateComponentMessage);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								// System.out.println("Process" + tName +
								// " Sending updateComponent message to " +
								// child);
							}

							// Make the parentId as the child and add it to
							// Children and mwoeReportReceived
							if (parentId != tName) {
								noOfChildren++;
								// processNode newChildNode = new
								// processNode(parentIndex,0);
								// newChildNode.setProcessId(parentId);
								children.add(parentIndex);
								mwoeReportReceived.put(parentIndex, 0);
							}

							// Update the parent pointer
							parentId = newParent;
							parentIndex = newParentIndex;

							// Add the removed basic Neighbour to branch
							// neighbour
							ProcessNode node = basicNeighbours.remove();
							branchNeighbours.add(node);
							// processConnectMessages = true;

						} else if (frontMessage.getMessageType().equals(
								"initiate")) {
							// If received message is a initiate message
							// Forward initiate message to children (if any) and
							// send test message

							waitingForMwoeReport = true;
							waitingForMyMwoeReport = true;
							mwoeFound = false;
							myMwoeReportReady = false;
							childrenMwoeReportReady = false;

							if (children.size() != 0) {

								// Set the value for all the keys in the
								// mwoeReportReceived hashmap to zero

								Iterator<Map.Entry<Integer, Integer>> iterator = mwoeReportReceived
										.entrySet().iterator();
								while (iterator.hasNext()) {
									Map.Entry<Integer, Integer> entry = iterator
											.next();
									entry.setValue(0);
								}

								String message = "This is an Initiate Message forwarded from leader";
								Random rand = new Random();
								int delay = rand.nextInt((20 - 0) + 1) + 0;
								for (Integer child : children) {
									MessageGenerator initiateMessage = new MessageGenerator(
											20, "initiate", myIndex, tName);
									initiateMessage.setDelay(20);
									initiateMessage.setMessage(message);
									initiateMessage
											.setMessageNumber(messageNumber++);
									try {
										tdque.get(myIndex).get(child)
												.put(initiateMessage);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									// System.out.println("Process " + tName +
									// " sending initiate message to " + child);
								}
							} else {

								waitingForMwoeReport = false;
								childrenMwoeReportReady = true;
							}

							// send test messages along its first basic edge
							String message1 = "This is a test Message";
							ProcessNode basicNode = basicNeighbours.peek();
							// System.out.println("Process " + tName +
							// " Number of Basic Neighbours " +
							// basicNeighbours.size());
							if (basicNode != null) {
								Random rand1 = new Random();
								int delay1 = rand1.nextInt((20 - 0) + 1) + 0;
								MessageGenerator testMessage = new MessageGenerator(
										delay1, "test", myIndex, tName);
								testMessage.setMessage(message1);
								testMessage.setLevel(level);
								testMessage
										.setCoreEdgeProcess1Id(coreEdgeProcess1Id);
								testMessage
										.setCoreEdgeProcess2Id(coreEdgeProcess2Id);
								testMessage.setCoreEdgeLength(coreEdgeLength);
								testMessage.setMessageNumber(messageNumber++);
								try {
									tdque.get(myIndex)
											.get(basicNode.getProcessIndex())
											.put(testMessage);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								// System.out.println("Process " + tName +
								// " sending test message to index " +
								// basicNode.getProcessIndex());
							} else if (basicNode == null) {
								// Send No MWOE found report to the parent
								myMwoeReportReady = true;
								waitingForMyMwoeReport = false;
								// System.out.println("Process " + tName +
								// " has no basic neighbour to send the test message");
							}

						} else if (frontMessage.getMessageType().equals("test")) {
							// If received message is a test message

							// If the component identifier matches, Send a
							// reject message
							if (frontMessage.getCoreEdgeLength() == coreEdgeLength
									&& frontMessage.getCoreEdgeProcess1Id() == coreEdgeProcess1Id
									&& frontMessage.getCoreEdgeProcess2Id() == coreEdgeProcess2Id
									&& frontMessage.getLevel() == level) {

								// Send Reject Message
								String message = "This is a reject Message";
								Random rand = new Random();
								int delay = rand.nextInt((20 - 0) + 1) + 0;
								MessageGenerator rejectMessage = new MessageGenerator(
										delay, "reject", myIndex, tName);
								rejectMessage.setMessage(message);
								rejectMessage.setMessageNumber(messageNumber++);
								try {
									tdque.get(myIndex)
											.get(frontMessage.getSender())
											.put(rejectMessage);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								// System.out.println("Process " + tName +
								// " sending reject message to " +
								// frontMessage.getSender());

							} else if (frontMessage.getLevel() <= level) { // If
																			// the
																			// level
																			// of
																			// the
																			// test
																			// message
																			// sender
																			// is
																			// less
																			// than
																			// or
																			// equal
																			// to
																			// reciever,
																			// Send
																			// Accept
																			// Message
								// Send Accept Message
								String message = "This is an Accept Message";
								Random rand = new Random();
								int delay = rand.nextInt((20 - 0) + 1) + 0;
								MessageGenerator acceptMessage = new MessageGenerator(
										delay, "accept", myIndex, tName);
								acceptMessage.setMessage(message);
								acceptMessage.setMessageNumber(messageNumber++);
								try {
									tdque.get(myIndex)
											.get(frontMessage.getSender())
											.put(acceptMessage);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								// System.out.println("Process " + tName +
								// " sending accept message to " +
								// frontMessage.getSender());

							} else {

								// If the component identifiers doesnt match and
								// level of the Sender is greater than the
								// receiver
								// Put the test in the pendingTestMessages list
								// and set pendingTestMessage flag to true
								pendingTestMessageList.add(frontMessage); // **********As
																			// suggested
																			// by
																			// Dilip****************
								pendingTestMessage = true;

								// System.out.println("Process " + tName +
								// " pushing the test Message in the pendingTestMessageList & pending Test Message is set true ");
							}

						} else if (frontMessage.getMessageType().equals(
								"accept")) {
							// If received message is a accept message

							// If mwoe is already found by any child, compare my
							// mwoe with it and Set the mwoe parameters
							// accordingly
							ProcessNode mwoeProcess = basicNeighbours.peek();
							if (mwoeFound == true) {
								if (mwoeProcess.getProcessDistance() < weightOfMWOE) {
									weightOfMWOE = mwoeProcess
											.getProcessDistance();
									mwoeFoundProcess = myIndex;
								}
							} else { // If mwoe is not received by any parent,
										// set my mwoe as the mwoe parameters
								mwoeFound = true;
								weightOfMWOE = mwoeProcess.getProcessDistance();
								mwoeFoundProcess = myIndex;
							}

							// Set myMwoeReportReady to true
							myMwoeReportReady = true;
							waitingForMyMwoeReport = false;

						} else if (frontMessage.getMessageType().equals(
								"reject")) {
							// If received message is a reject message
							// Remove the node from the basic Neighbours and put
							// it in reject neighbours
							// Send Test message to next basic neighbour,
							// If no basic neighbour exists, wait for report
							// from children and send report to parent

							ProcessNode tempNode = basicNeighbours.peek();
							if (tempNode != null) {
								if (tempNode.getProcessIndex() == frontMessage
										.getSender()) {
									tempNode = basicNeighbours.remove();
									rejectNeighbours.add(tempNode);
								}
							}

							ProcessNode basicNode = basicNeighbours.peek();
							// System.out.println("Process " + tName +
							// " Number of Basic Neighbours " +
							// basicNeighbours.size());
							if (basicNode != null) {
								Random rand1 = new Random();
								int delay1 = rand1.nextInt((20 - 0) + 1) + 0;
								MessageGenerator testMessage = new MessageGenerator(
										delay1, "test", myIndex, tName);
								testMessage
										.setMessage("This is a test Message");
								testMessage.setLevel(level);
								testMessage
										.setCoreEdgeProcess1Id(coreEdgeProcess1Id);
								testMessage
										.setCoreEdgeProcess2Id(coreEdgeProcess2Id);
								testMessage.setCoreEdgeLength(coreEdgeLength);
								testMessage.setMessageNumber(messageNumber++);
								try {
									tdque.get(myIndex)
											.get(basicNode.getProcessIndex())
											.put(testMessage);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								// System.out.println("Process " + tName +
								// " sending test message to " +
								// basicNode.getProcessIndex());

							} else if (basicNode == null) {
								// Send No MWOE found report to the parent
								myMwoeReportReady = true;
								waitingForMyMwoeReport = false;
								// System.out.println("Process " + tName +
								// "has no basicNeighbour to send the test message");
							}

						} else if (frontMessage.getMessageType().equals(
								"mwoeReport")) {
							// If received message is a mwoe report message

							// Update the received report array for all the
							// children
							mwoeReportReceived.put(frontMessage.getSender(), 1);
							if (frontMessage.getMwoeFound() == true) {
								if (mwoeFound == true) {
									if (frontMessage.getMwoeLength() < weightOfMWOE) {
										weightOfMWOE = frontMessage
												.getMwoeLength();
										mwoeFoundProcess = frontMessage
												.getMwoeFoundProcess();
									}
								} else {
									mwoeFound = true;
									weightOfMWOE = frontMessage.getMwoeLength();
									mwoeFoundProcess = frontMessage
											.getMwoeFoundProcess();
								}
							}

						} else if (frontMessage.getMessageType().equals(
								"connectReady")) {
							// This will be sent by the Leader to the child node
							// through which MWOE has been identified
							// If received message is a connectReady message
							// Check whether the connectReadyTargetId is equal
							// to its own Process Id
							// If No, Forward it to all children
							// If Yes, Don't forward it to Children. Send the
							// connect Message along MWOE

							if (frontMessage.getMwoeFoundProcess() != myIndex) { // If
																					// this
																					// process
																					// has
																					// not
																					// found
																					// the
																					// mwoe,
																					// **
																					// myIndex
																					// needs
																					// to
																					// be
																					// retained
								// Forward connectReady Message to all the
								// children
								String message = "This is an ConnectReady Message from leader";
								Random rand = new Random();
								int delay = rand.nextInt((20 - 0) + 1) + 0;
								MessageGenerator connectReadyMessage = new MessageGenerator(
										delay, "connectReady", myIndex, tName);
								connectReadyMessage.setMessage(message);
								connectReadyMessage.setMwoeLength(frontMessage
										.getMwoeLength());
								connectReadyMessage
										.setMwoeLFoundProcess(frontMessage
												.getMwoeFoundProcess());
								connectReadyMessage
										.setMessageNumber(messageNumber++);
								for (Integer child : children) {
									try {
										tdque.get(myIndex).get(child)
												.put(connectReadyMessage);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									// System.out.println("Process" + tName +
									// " sending connectReady Message to " +
									// child);
								}
							} else {
								// If this process has found the mwoe
								// Send a connect message to the first basic
								// Neighbour
								String message = "This is a Connect Message";
								ProcessNode basicNode = basicNeighbours.peek();
								Random rand = new Random();
								int delay = rand.nextInt((20 - 0) + 1) + 0;
								MessageGenerator connectMessage = new MessageGenerator(
										delay, "connect", myIndex, tName);
								connectMessage.setMessage(message);
								connectMessage.setLevel(level);
								connectMessage
										.setMessageNumber(messageNumber++);
								connectMessage
										.setCoreEdgeLength(coreEdgeLength);
								connectMessage
										.setCoreEdgeProcess1Id(coreEdgeProcess1Id);
								connectMessage
										.setCoreEdgeProcess2Id(coreEdgeProcess2Id);
								try {
									tdque.get(myIndex)
											.get(basicNode.getProcessIndex())
											.put(connectMessage);
									connectSent = true;
									connectSentNode = basicNode
											.getProcessIndex();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								// System.out.println("Process" + tName +
								// " Sending Connect message to " +
								// basicNode.getProcessId());
							}

						} else if (frontMessage.getMessageType().equals(
								"propagate")) {

							// Change the component identifier of this process
							// and forward the
							coreEdgeProcess1Id = frontMessage
									.getCoreEdgeProcess1Id();
							coreEdgeLength = frontMessage.getCoreEdgeLength();
							coreEdgeProcess2Id = frontMessage
									.getCoreEdgeProcess2Id();
							level = frontMessage.getLevel();
							leader = false;

							// Set the new Parent in the temporary variable
							newParent = frontMessage.getSenderId();
							newParentIndex = frontMessage.getSender();

							// Forward the propagate message to Parent if any
							if (parentId != tName) {
								Random rand = new Random();
								int delay = rand.nextInt((20 - 0) + 1) + 0;
								MessageGenerator propagateMessage = new MessageGenerator(
										delay, "propagate", myIndex, tName);
								propagateMessage
										.setCoreEdgeProcess1Id(coreEdgeProcess1Id);
								propagateMessage
										.setCoreEdgeProcess2Id(coreEdgeProcess2Id);
								propagateMessage
										.setCoreEdgeLength(coreEdgeLength);
								propagateMessage.setLevel(level);
								propagateMessage
										.setMessageNumber(messageNumber++);
								propagateMessage
										.setMessage("This is an propagate message sent after merging");
								try {
									tdque.get(myIndex).get(parentIndex)
											.put(propagateMessage);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								// System.out.println("Process" + tName +
								// " Sending propagate message to " + parentId);
							}

							// Remove propagate message sender from the children
							// list
							noOfChildren--;
							children.remove((Integer) newParentIndex);
							mwoeReportReceived.remove(newParentIndex);

							// Send Update Component & level Message to its
							// children

							String message = "This is a updateComponent Message";
							Random rand = new Random();
							int delay = rand.nextInt((20 - 0) + 1) + 0;
							MessageGenerator updateComponentMessage = new MessageGenerator(
									delay, "updateComponent", myIndex, tName);
							updateComponentMessage.setMessage(message);
							updateComponentMessage
									.setCoreEdgeProcess1Id(coreEdgeProcess1Id);
							updateComponentMessage
									.setCoreEdgeProcess2Id(coreEdgeProcess2Id);
							updateComponentMessage
									.setCoreEdgeLength(coreEdgeLength);
							updateComponentMessage.setLevel(level);
							updateComponentMessage
									.setMessageNumber(messageNumber++);
							for (Integer child : children) {
								try {
									tdque.get(myIndex).get(child)
											.put(updateComponentMessage);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								// System.out.println("Process" + tName +
								// " Sending updateComponent message to child index"
								// + child);
							}

							// Make the parentId as the child and add it to
							// Children and mwoeReportReceived
							if (parentId != tName) {
								noOfChildren++;
								children.add(parentIndex);
								mwoeReportReceived.put(parentIndex, 0);
							}

							// Change the parentId to this new parent
							parentId = newParent;
							parentIndex = newParentIndex;

						} else if (frontMessage.getMessageType().equals(
								"updateComponent")) {
							// Change the component identifier of this process
							// and forward the
							coreEdgeProcess1Id = frontMessage
									.getCoreEdgeProcess1Id();
							coreEdgeLength = frontMessage.getCoreEdgeLength();
							coreEdgeProcess2Id = frontMessage
									.getCoreEdgeProcess2Id();
							level = frontMessage.getLevel();
							leader = false;

							// Send Update Component & level Message to its
							// children

							String message = "This is a updateComponent Message";
							Random rand = new Random();
							int delay = rand.nextInt((20 - 0) + 1) + 0;
							MessageGenerator updateComponentMessage = new MessageGenerator(
									delay, "updateComponent", myIndex, tName);
							updateComponentMessage.setMessage(message);
							updateComponentMessage
									.setCoreEdgeProcess1Id(coreEdgeProcess1Id);
							updateComponentMessage
									.setCoreEdgeProcess2Id(coreEdgeProcess2Id);
							updateComponentMessage
									.setCoreEdgeLength(coreEdgeLength);
							updateComponentMessage.setLevel(level);
							updateComponentMessage
									.setMessageNumber(messageNumber++);
							for (Integer child : children) {
								try {
									tdque.get(myIndex).get(child)
											.put(updateComponentMessage);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								// System.out.println("Process" + tName +
								// " Sending updateComponent message to child index "
								// + child);
							}

						}

					}
				}

				if (level == 0 && connectSent == false
				// && previousStepCompleted == true
				) {

					// Send connect message to the first basic Neighbor

					String message = "This is a Connect Message";
					ProcessNode basicNode = basicNeighbours.peek();
					Random rand = new Random();
					int delay = rand.nextInt((20 - 0) + 1) + 0;
					MessageGenerator connectMessage = new MessageGenerator(
							delay, "connect", myIndex, tName);
					connectMessage.setMessage(message);
					connectMessage.setLevel(level);
					connectMessage.setMessageNumber(messageNumber++);
					connectMessage.setCoreEdgeLength(coreEdgeLength);
					connectMessage.setCoreEdgeProcess1Id(coreEdgeProcess1Id);
					connectMessage.setCoreEdgeProcess2Id(coreEdgeProcess2Id);
					try {
						tdque.get(myIndex).get(basicNode.getProcessIndex())
								.put(connectMessage);
						connectSent = true;
						connectSentNode = basicNode.getProcessIndex();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// System.out.println("Process" + tName +
					// " Sending Connect message to index " +
					// basicNode.getProcessIndex());

				}

				if (level != 0 && leader == true && initiateSent == false // &&
																			// waitingForMergeCompletion
																			// ==
																			// false
						// && previousStepCompleted == true
						&& processConnectMessages == false) {

					// If level is not equal to 0 and If I am the leader and
					// If I have not Sent the Initiate Message
					// Send Initiate Message to all the Children and set
					// initiateSent to true

					initiateSent = true; // Should be set to false after the
											// merge or absorb. This step needs
											// to be added in the merge or
											// absorb block

					// previousStepCompleted = false;

					// initiateForwarded = true;

					// TODO Should I include mwoeReportReceived flag?
					waitingForMwoeReport = true;
					waitingForMyMwoeReport = true;
					mwoeFound = false;
					myMwoeReportReady = false;
					childrenMwoeReportReady = false;

					if (children.size() != 0) {

						// Set the value for all the keys in the
						// mwoeReportReceived hashmap to zero

						Iterator<Map.Entry<Integer, Integer>> iterator = mwoeReportReceived
								.entrySet().iterator();
						while (iterator.hasNext()) {
							Map.Entry<Integer, Integer> entry = iterator.next();
							entry.setValue(0);
						}

						String message = "This is an Initiate Message forwarded from leader";
						// Random rand = new Random();
						// int delay = rand.nextInt((20 -0) + 1) + 0;
						MessageGenerator initiateMessage = new MessageGenerator(
								20, "initiate", myIndex, tName);
						initiateMessage.setDelay(20);
						initiateMessage.setMessage(message);
						initiateMessage.setMessageNumber(messageNumber++);
						for (Integer child : children) {
							try {
								tdque.get(myIndex).get(child)
										.put(initiateMessage);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							// System.out.println("Process" + tName +
							// " Sending Initiate message to child index" +
							// child );
						}
					} else {

						waitingForMwoeReport = false;
						childrenMwoeReportReady = true;
					}

					// Send Test Message to the first Basic Neighbour

					String message1 = "This is a test Message";
					ProcessNode basicNode = basicNeighbours.peek();
					if (basicNode != null) { // Send a test message only if
												// there are any basic
												// neighbours
						Random rand1 = new Random();
						int delay1 = rand1.nextInt((20 - 0) + 1) + 0;
						MessageGenerator testMessage = new MessageGenerator(
								delay1, "test", myIndex, tName);
						testMessage.setMessage(message1);
						testMessage.setLevel(level);
						testMessage.setCoreEdgeProcess1Id(coreEdgeProcess1Id);
						testMessage.setCoreEdgeProcess2Id(coreEdgeProcess2Id);
						testMessage.setCoreEdgeLength(coreEdgeLength);
						testMessage.setMessageNumber(messageNumber++);
						try {
							tdque.get(myIndex).get(basicNode.getProcessIndex())
									.put(testMessage);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						waitingForAccept = true;

						// System.out.println("Process" + tName +
						// " Sending Test message to " +
						// basicNode.getProcessId() );

					} else if (basicNode == null) {
						myMwoeReportReady = true;
						waitingForMyMwoeReport = false;
						System.out
								.println("Process"
										+ tName
										+ " has no basic Neighbour to send the test Message");
					}

					// Once the test message is sent or doesnt have any basic
					// neighbours, Change the status to waiting for

				}

				if (waitingForMwoeReport == true) {
					// Check whether you have received report from all the
					// children
					// If yes, set waitingForMwoeReport == false, else keep it
					// true

					waitingForMwoeReport = false;
					childrenMwoeReportReady = true;
					if (mwoeReportReceived.isEmpty()) {

					} else {
						for (Object value : mwoeReportReceived.values()) {
							if (value.equals(0)) {
								waitingForMwoeReport = true;
								childrenMwoeReportReady = false;
								break;
							}
						}
					}
				}

				if (childrenMwoeReportReady == true
						&& myMwoeReportReady == true) {
					// compare the MWOE from the Report of Children with my MWOE
					// report
					// If I am not the leader send the report to the parent
					// If I am the leader broadcast connectReady message to all
					// the processed in the component
					if (leader == true) {
						// Send connectReady Message if there are any mwoes

						if (mwoeFound == true) {
							if (mwoeFoundProcess != myIndex) { // If leader has
																// not found the
																// mwoe,*********need
																// to verify
																// this index
																// and
																// retain**********
								// Send connectReady Message if there are any
								// mwoes
								String message = "This is an ConnectReady Message from leader";
								Random rand = new Random();
								int delay = rand.nextInt((20 - 0) + 1) + 0;
								MessageGenerator connectReadyMessage = new MessageGenerator(
										delay, "connectReady", myIndex, tName);
								connectReadyMessage.setMessage(message);
								connectReadyMessage.setMwoeLength(weightOfMWOE);
								connectReadyMessage
										.setMwoeLFoundProcess(mwoeFoundProcess);
								connectReadyMessage
										.setMessageNumber(messageNumber++);
								for (Integer child : children) {
									try {
										tdque.get(myIndex).get(child)
												.put(connectReadyMessage);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									// System.out.println("Process" + tName +
									// " sending connectReady Message to child index"
									// + child);
								}
							} else {
								// If leader has found the mwoe
								// Send a connect message to the first basic
								// Neighbour
								String message = "This is a Connect Message";
								ProcessNode basicNode = basicNeighbours.peek();
								Random rand = new Random();
								int delay = rand.nextInt((20 - 0) + 1) + 0;
								MessageGenerator connectMessage = new MessageGenerator(
										delay, "connect", myIndex, tName);
								connectMessage.setMessage(message);
								connectMessage.setLevel(level);
								connectMessage
										.setMessageNumber(messageNumber++);
								connectMessage
										.setCoreEdgeLength(coreEdgeLength);
								connectMessage
										.setCoreEdgeProcess1Id(coreEdgeProcess1Id);
								connectMessage
										.setCoreEdgeProcess2Id(coreEdgeProcess2Id);
								try {
									tdque.get(myIndex)
											.get(basicNode.getProcessIndex())
											.put(connectMessage);
									connectSent = true;
									connectSentNode = basicNode
											.getProcessIndex();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								// System.out.println("Process" + tName +
								// " Sending Connect message to " +
								// basicNode.getProcessId());
							}

							myMwoeReportReady = false;
							childrenMwoeReportReady = false;
							waitingForMwoeReport = false;
							waitingForMyMwoeReport = false;

						} else {
							// Otherwise terminate the MST process
							System.out.println("MST process terminated");

							// TODO Send a message to the Master saying MST is
							// completed
							String message = "This is a Terminate Message";
							MessageGenerator terminateMessage = new MessageGenerator(
									0, "terminate", myIndex, tName);
							terminateMessage.setMessage(message);
							terminateMessage.setLevel(level);
							terminateMessage.setMessageNumber(messageNumber++);
							try {
								masterQueue.put(terminateMessage);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							System.out
									.println("Process"
											+ tName
											+ " Sending Terminate message to the master");
							myMwoeReportReady = false;
							childrenMwoeReportReady = false;
							waitingForMwoeReport = false;
							waitingForMyMwoeReport = false;

						}
					} else {
						// Forward the mwoe report to the parent
						Random rand = new Random();
						int delay = rand.nextInt((20 - 0) + 1) + 0;
						MessageGenerator reportMessage = new MessageGenerator(
								delay, "mwoeReport", myIndex, tName);
						reportMessage.setMwoeFound(mwoeFound);
						reportMessage.setMwoeLength(weightOfMWOE);
						reportMessage.setMwoeLFoundProcess(mwoeFoundProcess);
						reportMessage.setLevel(level);
						reportMessage.setMessageNumber(messageNumber++);
						reportMessage
								.setMessage("This is a MWOE Report Message forwarded to parent");
						try {
							tdque.get(myIndex).get(parentIndex)
									.put(reportMessage);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						// System.out.println("Process" + tName +
						// " sending MWOE report to the parent " + parentId);

						waitingForMwoeReport = false;
						waitingForMyMwoeReport = false;
						myMwoeReportReady = false;
						childrenMwoeReportReady = false;

						// mwoeReportReceived HashMap must be cleared after
						// sending the mwoeReport message to the paren

						Iterator<Map.Entry<Integer, Integer>> iterator = mwoeReportReceived
								.entrySet().iterator();
						while (iterator.hasNext()) {
							Map.Entry<Integer, Integer> entry = iterator.next();
							entry.setValue(0);
						}

					}
				}

				if (processConnectMessages == true) {

					for (MessageGenerator connectMessage : pendingConnectMessages) {

						if (connectMessage.getLevel() < level) { // If the
																	// process
																	// sending
																	// the
																	// connect
																	// message
																	// has a
																	// level
																	// lesser
																	// than
																	// mine,
																	// Send
																	// absorb
																	// message
																	// to that
																	// process

							// Make the process to which absorb is sent as the
							// child
							children.add(connectMessage.getSender());
							noOfChildren++;
							mwoeReportReceived.put(connectMessage.getSender(),
									0);
							// if(waitingForMwoeReport == false){
							// mwoeReportReceived.put(connectMessage.getSender(),
							// 0); //****Very Important
							// }else{
							// mwoeReportReceived.put(connectMessage.getSender(),
							// 1); //****Very Important
							// }

							// Remove the sender from the basicNeighbour list
							ProcessNode tempNode = null;
							for (ProcessNode node : basicNeighbours) {
								if (node.processIndex == connectMessage
										.getSender()) {
									// System.out.println("Found Matching Neighbour");
									tempNode = node;
									// break;
								}
							}

							if (tempNode != null) {
								basicNeighbours.remove(tempNode);
								branchNeighbours.add(tempNode);
							}

							// Send the absorb Message to the process which send
							// the connect message
							Random rand = new Random();
							int delay = rand.nextInt((20 - 0) + 1) + 0;
							MessageGenerator absorbMessage = new MessageGenerator(
									delay, "absorb", myIndex, tName);
							absorbMessage
									.setCoreEdgeProcess1Id(coreEdgeProcess1Id);
							absorbMessage
									.setCoreEdgeProcess2Id(coreEdgeProcess2Id);
							absorbMessage.setCoreEdgeLength(coreEdgeLength);
							absorbMessage.setLevel(level);
							absorbMessage.setMessageNumber(messageNumber++);
							absorbMessage
									.setMessage("This is an absorb message sent after receiving the connect Message");
							try {
								tdque.get(myIndex)
										.get(connectMessage.getSender())
										.put(absorbMessage);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							// System.out.println("Process " + tName +
							// " sending absorb message to " +
							// connectMessage.getSenderId() + ", with delay" +
							// delay);

							// TODO If waitingForMwoeReport == true send
							// initiate message to the new child
							if (waitingForMwoeReport == true
									|| waitingForMyMwoeReport == true) {
								String message = "This is an Initiate Message forwarded from leader";
								// Random rand2 = new Random();
								// int delay2 = rand2.nextInt((20 -0) + 1) + 0;
								MessageGenerator initiateMessage = new MessageGenerator(
										21, "initiate", myIndex, tName);
								initiateMessage.setDelay(21);
								initiateMessage.setMessage(message);
								initiateMessage
										.setMessageNumber(messageNumber++);
								try {
									tdque.get(myIndex)
											.get(connectMessage.getSender())
											.put(initiateMessage);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								waitingForMwoeReport = true;
								childrenMwoeReportReady = false;
								// System.out.println("Process" + tName +
								// " Sending Initiate message to " +
								// connectMessage.getSenderId() + ", with delay"
								// + delay );
							}

							pendingConnectMessages.remove(connectMessage);

						} else if (connectMessage.getSender() == connectSentNode
								&& connectSent == true) {

							level = level + 1;
							// Have to set the connectSent flag to false after
							// merging operation

							// Remove the first basic neighbor and add it to
							// branch neighbor
							ProcessNode tempNode = basicNeighbours.remove();
							branchNeighbours.add(tempNode);

							if (connectMessage.getSenderId() < tName) {
								leader = true;

								System.out.println("Component ID: ( "
										+ coreEdgeProcess1Id
										+ ", "
										+ coreEdgeLength
										+ ", "
										+ coreEdgeProcess2Id
										+ " )"
										+ ", Merging with Component ID: ( "
										+ connectMessage
												.getCoreEdgeProcess1Id()
										+ ", "
										+ connectMessage.getCoreEdgeLength()
										+ ", "
										+ connectMessage
												.getCoreEdgeProcess2Id() + " )"
										+ ", New Component ID: ( " + tName
										+ ", " + tempNode.getProcessDistance()
										+ ", " + connectMessage.getSenderId()
										+ " )");

								// Set the new Component Identifier
								coreEdgeLength = tempNode.getProcessDistance();
								coreEdgeProcess1Id = tName;
								coreEdgeProcess2Id = connectMessage
										.getSenderId();

								// Store the new parent in the temporary
								// variable
								newParent = tName;
								newParentIndex = myIndex;

								// Send Update Component & level Message to its
								// children

								String message = "This is a updateComponent Message";
								Random rand = new Random();
								int delay = rand.nextInt((20 - 0) + 1) + 0;
								MessageGenerator updateComponentMessage = new MessageGenerator(
										delay, "updateComponent", myIndex,
										tName);
								updateComponentMessage.setMessage(message);
								updateComponentMessage
										.setCoreEdgeProcess1Id(coreEdgeProcess1Id);
								updateComponentMessage
										.setCoreEdgeProcess2Id(coreEdgeProcess2Id);
								updateComponentMessage
										.setCoreEdgeLength(coreEdgeLength);
								updateComponentMessage.setLevel(level);
								updateComponentMessage
										.setMessageNumber(messageNumber++);
								for (Integer child : children) {
									try {
										tdque.get(myIndex).get(child)
												.put(updateComponentMessage);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}

									// System.out.println("Process" + tName +
									// " Sending updateComponent message to child index "
									// + child);
								}

								noOfChildren++;
								children.add(connectMessage.getSender());
								mwoeReportReceived.put(
										connectMessage.getSender(), 0); // ****Very
																		// Important****

							} else {
								leader = false;

								// Set the new Component Identifier
								coreEdgeLength = tempNode.getProcessDistance();
								coreEdgeProcess1Id = connectMessage
										.getSenderId();
								coreEdgeProcess2Id = tName;

								// Store the new parent in the temporary
								// variable
								newParent = connectMessage.getSenderId();
								newParentIndex = connectMessage.getSender();

								// System.out.println("Process " + tName +
								// ", Merging with Node: " +
								// connectMessage.getSender() +
								// ", Compenent ID: ( " + coreEdgeProcess1Id +
								// ", " + coreEdgeLength + ", " +
								// coreEdgeProcess2Id + " ),  leader: " +
								// leader);

								// Send Update Component & level Message to its
								// children

								String message = "This is a updateComponent Message";
								Random rand = new Random();
								int delay = rand.nextInt((20 - 0) + 1) + 0;
								MessageGenerator updateComponentMessage = new MessageGenerator(
										delay, "updateComponent", myIndex,
										tName);
								updateComponentMessage.setMessage(message);
								updateComponentMessage
										.setCoreEdgeProcess1Id(coreEdgeProcess1Id);
								updateComponentMessage
										.setCoreEdgeProcess2Id(coreEdgeProcess2Id);
								updateComponentMessage
										.setCoreEdgeLength(coreEdgeLength);
								updateComponentMessage.setLevel(level);
								updateComponentMessage
										.setMessageNumber(messageNumber++);
								for (Integer child : children) {
									try {
										tdque.get(myIndex).get(child)
												.put(updateComponentMessage);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}

									// System.out.println("Process" + tName +
									// " Sending updateComponent message to child index"
									// + child);
								}

							}

							pendingConnectMessages.remove(connectMessage);
							// previousStepCompleted = true;

							// Send propagate message to the parent of the
							// previous component(if it has any parent)
							if (parentId != tName) {
								Random rand = new Random();
								int delay = rand.nextInt((20 - 0) + 1) + 0;
								MessageGenerator propagateMessage = new MessageGenerator(
										delay, "propagate", myIndex, tName);
								propagateMessage
										.setCoreEdgeProcess1Id(coreEdgeProcess1Id);
								propagateMessage
										.setCoreEdgeProcess2Id(coreEdgeProcess2Id);
								propagateMessage
										.setCoreEdgeLength(coreEdgeLength);
								propagateMessage.setLevel(level);
								propagateMessage
										.setMessageNumber(messageNumber++);
								propagateMessage
										.setMessage("This is an propagate message sent after merging");
								try {
									tdque.get(myIndex).get(parentIndex)
											.put(propagateMessage);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}

								// System.out.println("Process" + tName +
								// " Sending propagate message to " + parentId);

								noOfChildren++;
								children.add(parentIndex);
								mwoeReportReceived.put(parentIndex, 0); // ****Very
																		// Important****
							}

							// change the parentId to this new parent
							parentId = newParent;
							parentIndex = newParentIndex;
							initiateSent = false;
							connectSent = false;

							connectSent = false;

						}
					}

					if (pendingConnectMessages.size() == 0) {
						processConnectMessages = false;
					}
				}

				if (pendingTestMessage == true) {

					if (pendingTestMessageList.size() != 0) {
						MessageGenerator tempQueueObject = null;

						for (MessageGenerator queueObject : pendingTestMessageList) {

							if (queueObject.getLevel() <= level) { // Reply to
																	// the test
																	// Message
																	// if the
																	// level of
																	// the
																	// Sender is
																	// lesser
																	// than or
																	// equal to
																	// mine

								// Send Accept or Reject based on the component
								// identifier
								if (queueObject.getCoreEdgeLength() == coreEdgeLength
										&& queueObject.getCoreEdgeProcess1Id() == coreEdgeProcess1Id
										&& queueObject.getCoreEdgeProcess2Id() == coreEdgeProcess2Id
										&& queueObject.getLevel() == level) {

									// Send Reject Message
									String message = "This is a reject Message";
									Random rand = new Random();
									int delay = rand.nextInt((20 - 0) + 1) + 0;
									MessageGenerator rejectMessage = new MessageGenerator(
											delay, "reject", myIndex, tName);
									rejectMessage.setMessage(message);
									rejectMessage
											.setMessageNumber(messageNumber++);
									try {
										tdque.get(myIndex)
												.get(queueObject.getSender())
												.put(rejectMessage);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}

									// System.out.println("Process" + tName +
									// " sending reject message to " +
									// queueObject.getSenderId());

								} else {

									// Send Accept Message
									String message = "This is an Accept Message";
									Random rand = new Random();
									int delay = rand.nextInt((20 - 0) + 1) + 0;
									MessageGenerator acceptMessage = new MessageGenerator(
											delay, "accept", myIndex, tName);
									acceptMessage.setMessage(message);
									acceptMessage
											.setMessageNumber(messageNumber++);
									try {
										tdque.get(myIndex)
												.get(queueObject.getSender())
												.put(acceptMessage);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}

									// System.out.println("Process" + tName +
									// " sending accept message to " +
									// queueObject.getSender());
								}

								// Remove the element from the queue
								// pendingTestMessageList.remove(queueObject);
								tempQueueObject = queueObject;
							}
						}
						if (tempQueueObject != null) {
							pendingTestMessageList.remove(tempQueueObject);
						}

					} else {
						pendingTestMessage = false;
					}

				}

			} else {
				// System.out.println("Thread: " + tName +
				// ", Read Stop Message from Master ");
				break;
			}

		}

		System.out.println("Thread: " + tName + ", Parent: " + parentId
				+ ", Component ID: (" + coreEdgeProcess1Id + ", "
				+ coreEdgeLength + ", " + coreEdgeProcess2Id + "), Level: "
				+ level);
		// System.out.println("Thread Terminating");

	}

	public static void main(String[] args) {

		String line = "";

		int numOfThreads = 0;
		int[] threadIds = null;// { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
		float[][] conn = null;/*
							 * { {0,3,0,0,0,2,0,0,0,0,0,0,0},
							 * {3,0,17,16,0,0,0,0,0,0,0,0,0},
							 * {0,17,0,8,0,0,0,0,18,0,0,0,0},
							 * {0,16,8,0,11,0,0,0,4,0,0,0,0},
							 * {0,0,0,11,0,1,6,5,10,0,0,0,0},
							 * {2,0,0,0,1,0,7,0,0,0,0,0,0},
							 * {0,0,0,0,6,7,0,15,0,0,0,0,0},
							 * {0,0,0,0,5,0,15,0,12,13,0,0,0},
							 * {0,0,18,4,10,0,0,12,0,9,0,0,0},
							 * {0,0,0,0,0,0,0,13,9,0,10,0,0},
							 * {0,0,0,0,0,0,0,0,0,10,0,11,16},
							 * {0,0,0,0,0,0,0,0,0,0,11,0,15},
							 * {0,0,0,0,0,0,0,0,0,0,16,15,0}};
							 */

		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(args[0]);
			DataInputStream in = new DataInputStream(inputStream);
			BufferedReader bf = new BufferedReader(new InputStreamReader(in));

			int lineCount = 0;
			String[] numbers;
			int tempIdx = 0;

			while ((line = bf.readLine()) != null) {
				if (lineCount == 0) {
					numOfThreads = Integer.parseInt(line.trim());
					conn = new float[numOfThreads][numOfThreads];
					lineCount++;
					threadIds = new int[numOfThreads];
				} else if (lineCount == 1) {
					String[] strThreadIds = line.split(",");
					int ct = 0;
					for (String s : strThreadIds) {
						threadIds[ct++] = Integer.parseInt(s.trim());
					}
					lineCount++;
				} else {
					numbers = line.trim().split(",");
					int i = 0;
					for (String part : numbers) {
						conn[tempIdx][i++] = Float.parseFloat(part.trim());
					}
					tempIdx++;
				}
			}
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		ArrayBlockingQueue<MessageGenerator> mqueue = new ArrayBlockingQueue<MessageGenerator>(
				numOfThreads);

		ArrayList<ArrayBlockingQueue<Integer>> queue = new ArrayList<ArrayBlockingQueue<Integer>>(
				numOfThreads);

		ArrayList<ArrayList<ArrayBlockingQueue<MessageGenerator>>> tdque = new ArrayList<ArrayList<ArrayBlockingQueue<MessageGenerator>>>(
				numOfThreads);

		ArrayList<ArrayBlockingQueue<MessageGenerator>> stdque = null;

		for (int j = 0; j < numOfThreads; j++) {
			stdque = new ArrayList<ArrayBlockingQueue<MessageGenerator>>(
					numOfThreads);
			for (int i = 0; i < numOfThreads; i++) {
				stdque.add(new ArrayBlockingQueue<MessageGenerator>(
						numOfThreads));
			}
			tdque.add(stdque);
			stdque = null;
		}

		// Initializing the array of Queues
		for (int i = 0; i < numOfThreads; i++) {
			queue.add(new ArrayBlockingQueue<Integer>(1));
		}

		// Array of Threads
		Thread[] arrayThread = new Thread[numOfThreads];

		// Thread initialization.
		/*
		 * For each thread, the following details are passed 1. Their thread ID
		 * 2. Their Neighbors 3. queue details of its own and the other threads
		 * 4. Master Thread queue
		 */
		for (int i = 0; i < numOfThreads; i++) {
			AsynchGHS temp = new AsynchGHS(i, threadIds[i], conn[i], queue,
					tdque, mqueue);
			arrayThread[i] = new Thread(temp);
		}

		// Thread Start
		for (int i = 0; i < numOfThreads; i++) {
			arrayThread[i].start();
			// System.out.println("Starting Thread ID: " +
			// arrayThread[i].getId() + ", Name: " + arrayThread[i].getName());
		}

		System.out.println("MST in Process");

		// Main thread sends a message to all the threads to start the
		// process.
		// Here the start message is encoded as '1'

		MessageGenerator messageToMaster;
		// for(int j = 0; j < 75 ; j++){
		while (true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			for (int i = 0; i < numOfThreads; i++) {
				try {
					MessageGenerator messageFromMaster = new MessageGenerator(
							0, "tick", 1000, 0);
					tdque.get(i).get(i).put(messageFromMaster);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			messageToMaster = mqueue.poll();
			if (messageToMaster != null) {
				break;
			}

		}

		for (int i = 0; i < numOfThreads; i++) {
			try {
				MessageGenerator messageFromMaster = new MessageGenerator(0,
						"stop", 1000, 0);
				tdque.get(i).get(i).put(messageFromMaster);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// System.out.println("Waiting for Child Threads to Terminate");
		// Main thread waits for all threads to terminate through join
		try {
			for (int i = 0; i < numOfThreads; i++) {
				arrayThread[i].join();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Main Thread Terminates");
	}

}