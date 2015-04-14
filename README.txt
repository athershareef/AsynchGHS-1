CS 6380.001 - Distributed Computing.
  
Implementation of AsynchGHS algorithm.

======================================================================

Note:

1. AsynchGHS.java is the source code. input.txt is the sample test input.
2. We ran the code on cs1.utdallas.edu.
3. Compiler used: Java compiler.

Running instructions:

1. After unzipping, copy AsynchGHS.java and input.txt to the same folder.
2. To compile, run the command "javac AsynchGHS.java"
3. To execute the program, run the command "java AsynchGHS input.txt". [java AsynchGHS <path to the input file>].
4. Kindly note that the input file has a comma separated adjacency matrix.

Interpretation of results:
1. When we ran the code on cs1.utdallas.edu for the attached input we get the following results:
	Thread: 1, Parent: 6, Component ID: (9, 10.0, 5), Level: 2
	Thread: 2, Parent: 1, Component ID: (9, 10.0, 5), Level: 2
	Thread: 3, Parent: 4, Component ID: (9, 10.0, 5), Level: 2
	Thread: 13, Parent: 12, Component ID: (9, 10.0, 5), Level: 2
	Thread: 6, Parent: 5, Component ID: (9, 10.0, 5), Level: 2
	Thread: 12, Parent: 11, Component ID: (9, 10.0, 5), Level: 2
	Thread: 4, Parent: 9, Component ID: (9, 10.0, 5), Level: 2
	Thread: 9, Parent: 9, Component ID: (9, 10.0, 5), Level: 2
	Thread: 7, Parent: 5, Component ID: (9, 10.0, 5), Level: 2
	Thread: 10, Parent: 9, Component ID: (9, 10.0, 5), Level: 2
	Thread: 5, Parent: 9, Component ID: (9, 10.0, 5), Level: 2
	Thread: 8, Parent: 5, Component ID: (9, 10.0, 5), Level: 2
	Thread: 11, Parent: 10, Component ID: (9, 10.0, 5), Level: 2
	Main Thread Terminates
