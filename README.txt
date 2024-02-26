If there are troubles with the main trying to read the text, 
input the full location of the file(pwd on linux + /inst.txt or /data.txt) with the inst.txt first 
and the data.txt second into the readData method in Main.java.
EX: "C:\\Users\\hggbc\\eclipse-workspace\\MIPS\\src\\inst.txt" for my inst.txt
EX: "C:\\Users\\hggbc\\eclipse-workspace\\MIPS\\src\\data.txt" for my data.txt
This is for the 6th line in the Main.java: 
theCPU.readData("C:\\Users\\hggbc\\eclipse-workspace\\MIPS\\src\\inst.txt", "C:\\Users\\hggbc\\eclipse-workspace\\MIPS\\src\\data.txt");

If the the code is having trouble reading the inst.txt,
make sure they have proper spacing similar to the given tests,
as the instruction converter can only read in a few specific ways, including
seeing that if there is a first charcter for whether a line is a function or not.

To run the program in linux make sure a inst.txt and data.txt are in the same folder
or the path is in the main for readData. First do "make" or "make simulator"
to compile the appropriate files. Then do "make run" to run the program, 
there will be no console output, but a text file called output.txt should appear.
Finally to clean or remove all the compiled files do "make clean".

If there are any other problems with compiling contact me at cw49136@umbc.edu.
This project was mainly done in java eclipse.