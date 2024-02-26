import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class CPU {
	private Memory mainMemory;
	private Register registerFile;
	private boolean canRun;
	private int cycle;
	
	private List<String> inst;
	private List<int[]> phase;
	private int instNum;
	private boolean showLast;
	
	private int[] ifToId;//All the length of the arrays are 8
	private int[] idToOne;
	private int[] oneToTwo;
	private int[] twoToThree;
	private int[] threeToFour;
	private int[] fourToMem;
	private int[] memToWb;
	//0 is the operation
	//1 is the register
	//2 is the second register
	//3 is the destination
	//4 is stall
	//5 is for forwarding
	//6 is if there is a halt and no more lines are needed
	//7 is the index to save the cycles
	
	public CPU(){
		mainMemory = new Memory();
		registerFile = new Register();
		canRun = true;
		cycle = 0;
		showLast = false;
		
		inst = new ArrayList<>();
		phase = new ArrayList<>();
		instNum = 0;
		
		ifToId = new int[8];
		idToOne = new int[8];
		oneToTwo = new int[8];
		twoToThree = new int[8];
		threeToFour = new int[8];
		fourToMem = new int[8];
		memToWb = new int[8];
		
		memToWb[0] = -1;
		fourToMem[0] = -1;
		threeToFour[0] = -1;
		twoToThree[0] = -1;
		oneToTwo[0] = -1;
		idToOne[0] = -1;
		ifToId[0] = -1;
	}
	
	public void readData(String fileInstruction, String fileData){//Read data and instructions to memory
		mainMemory.readFile(fileInstruction, fileData);
	}
	
	public void run(){//Run system
		while(canRun){//Runs the program
			cycle++;
			writeBack();
			memoryRun();
			exectutionFour();
			exectutionThree();
			exectutionTwo();
			exectutionOne();
			instructionDecode();
			instructionFetch();
			showLast = false;
			//System.out.println("Cycle number " + cycle + ": " + ifToId[0] + ", " + idToOne[0] + ", " + oneToTwo[0] + ", " + twoToThree[0] + ", " + threeToFour[0] + ", " + fourToMem[0] + ", " + memToWb[0]);
		}
	}
	
	public void outputFile() throws IOException{//Return the output file
		File file = new File("output.txt");
		FileWriter fw = new FileWriter(file);
		PrintWriter pw = new PrintWriter(fw);
		
		
		pw.println("Cycle Number for Each Stage: " + "IF" + " " + "ID"+ " " + "EX4"+ " " + "MEM"+ " " + "WB");
		for(int i = 0; i < inst.size(); i++) {
			pw.println(inst.get(i) + " \t  " + phase.get(i)[0]+ " " + phase.get(i)[1]+ " " + phase.get(i)[2]+ " " + phase.get(i)[3]+ " " + phase.get(i)[4]);
		}
		pw.println("Total number of access requests for instruction cache: " + mainMemory.totalIC);
		pw.println("Number of instruction cache hits: " + mainMemory.hitIC);
		pw.println("");
		pw.println("Total number of access requests for data cache: " + mainMemory.totalDC);
		pw.println("Number of data cache hits: " + mainMemory.hitDC);
		
		pw.close();
	}
	
	private void instructionFetch(){
		boolean canFill = mainMemory.canRunICMiss();
		if(ifToId[6] == 0 && ifToId[4] < 2 && (ifToId[4] == 0 || ifToId[0] == -1 || canFill)) {
			String next = mainMemory.instructionFetch();
			if(!canFill) {
				if(next.equals("Fail")) {
					ifToId[0] = -1;
				}
				else if(next.equals("Stop")) {
					ifToId[0] = -1;
				}
				else {
					inst.add(next);
					phase.add(new int[5]);
					
					int[] change = phase.get(instNum);
					change[0] = cycle;
					phase.set(instNum, change);
					
					int[] set = mainMemory.convertInstruction(next);
					
					ifToId[0]= set[0];
					ifToId[1]= set[2];
					ifToId[2] = set[3];
					ifToId[3] = set[1];
					ifToId[5] = 0;
					ifToId[7] = instNum;
					
					instNum++;
					
				}
			}
		}
		else if(ifToId[6] == 1) {
			if(showLast && !canFill) {
				String next = mainMemory.instructionFetch();
				inst.add(next);
				phase.add(new int[5]);
				
				int[] change = phase.get(instNum);
				change[0] = cycle;
				phase.set(instNum, change);
				
				int[] set = mainMemory.convertInstruction(next);
				
				ifToId[0]= set[0];
				ifToId[1]= set[2];
				ifToId[2] = set[3];
				ifToId[3] = set[1];
				ifToId[5] = 0;
				ifToId[7] = instNum;
				
				instNum++;
			}
			ifToId[0] = -1;
		}
	}
	
	private void instructionDecode(){	
		//If any forwarding is needed
		
		if(ifToId[5] == 0 && idToOne[0] == -1) {
			
			if(ifToId[0] == 0 || ifToId[0] == 1 || (ifToId[0] >= 3 && ifToId[0] <= 14)) {
				if(needForward(ifToId[1])) {
					ifToId[5] += 1;
				}
				else {
					ifToId[1] = registerFile.getRegister(ifToId[1]);
				}
			}
			if(ifToId[0] == 14 || ifToId[0] == 13 || ifToId[0] == 13 || ifToId[0] == 11 || ifToId[0] == 9 || ifToId[0] == 7 || ifToId[0] == 5 || ifToId[0] == 3) {
				if(needForward(ifToId[2])) {
					ifToId[5] += 2;
				}
				else {
					ifToId[2] = registerFile.getRegister(ifToId[2]);
				}
			}
			if(ifToId[0] == 1) {
				if(needForward(ifToId[3])) {
					ifToId[5] += 4;
				}
				else {
					ifToId[3] = registerFile.getRegister(ifToId[3]);
				}
			}
		}
		
		if(ifToId[5] == 0 && idToOne[0] == -1) {
			ifToId[5] = -1;
		}
		
		if(ifToId[5] > 0) {//For checking and setting possible forwarding
			if(ifToId[5] > 3) {
				int check = canForward(ifToId[3], true);
				if(check > -1) {
					ifToId[3] = forward(check, true);
					ifToId[5] -= 4;
				}
			}
			
			if(ifToId[5] > 1 && ifToId[5] != 4 && ifToId[5] != 5) {
				int check = canForward(ifToId[2], true);
				if(check > -1) {
					ifToId[2] = forward(check, true);
					ifToId[5] -= 2;
				}
			}
			
			if(ifToId[5] > 0 && (ifToId[5] != 4 && ifToId[5] != 2 && ifToId[5] != 6)) {
				int check = canForward(ifToId[1], true);
				if(check > -1) {
					ifToId[1] = forward(check, true);
					ifToId[5] -= 1;
				}
			}
		}
		
		if((idToOne[4] == 0 && idToOne[6] == 0) || (idToOne[0] == -1)) {//FIX
			boolean rOne = true;
			boolean rTwo = true;
			boolean des = true;
			int codeFor = ifToId[5];
			
			
			
			if(codeFor > 3) {
				des = predictStall(ifToId[3]);
				codeFor -= 4;
			}
			
			if(codeFor > 1) {
				rTwo = predictStall(ifToId[2]);
				codeFor -= 2;
			}
			
			if(codeFor > 0) {
				rOne = predictStall(ifToId[1]);
				codeFor -= 1;
			}
			
			if(ifToId[5] > 0 && (!(rOne && rTwo && des) || ifToId[0] > 12)) {//If forwarding is still needed for BNE or BEQ or can't be done in EX1
				idToOne[0] = -1;
				ifToId[4] = 1;
				return;
			}
			
			if(ifToId[0] == 13) {//BEQ
				
				int[] change = phase.get(ifToId[7]);
				change[1] = cycle;
				phase.set(ifToId[7], change);
				
				if(ifToId[1] == ifToId[2]) {
					int jump = ifToId[3];
					ifToId[0] = -1;
					
					instructionFetch();
					mainMemory.jump(jump);
					idToOne[0] = -1;
					ifToId[0] = -1;
					ifToId[4] = 2;			
					return;
				}
				
			}
			else if(ifToId[0] == 14) {//BNE
				
				int[] change = phase.get(ifToId[7]);
				change[1] = cycle;
				phase.set(ifToId[7], change);
				
				
				if(ifToId[1] != ifToId[2]) {
					int jump = ifToId[3];
					ifToId[0] = -1;
					
					instructionFetch();
					mainMemory.jump(jump);
					idToOne[0] = -1;
					ifToId[0] = -1;
					ifToId[4] = 2;
					
					return;
					
				}
			}
			else if(ifToId[0] == 15) {//JUMP
				
				int[] change = phase.get(ifToId[7]);
				change[1] = cycle;
				phase.set(ifToId[7], change);
				
				int jump = ifToId[3];
				ifToId[0] = -1;
				
				instructionFetch();
				mainMemory.jump(jump);
				idToOne[0] = -1;
				ifToId[0] = -1;
				ifToId[4] = 2;
				
				return;
			}
			else if(ifToId[0] == 16) {
				int[] change = phase.get(ifToId[7]);
				change[1] = cycle;
				phase.set(ifToId[7], change);
				
				showLast = true;
				ifToId[6] = 1;
			}
			
			
			for(int i = 0; i < 8; i++) {
				idToOne[i] = ifToId[i];
			}
			
			if(ifToId[0] != -1 && ifToId[6] != 1) {
				int[] change = phase.get(ifToId[7]);
				change[1] = cycle;
				phase.set(ifToId[7], change);
			}
			
			
			ifToId[0] = -1;
			
			ifToId[4] = 0;
			
		}
		
	}
	
	private void exectutionOne(){
		if(idToOne[5] > 0) {//For checking and setting possible forwarding
			if(idToOne[5] > 3) {
				int check = canForward(idToOne[3], false);
				if(check > -1) {
					idToOne[3] = forward(check, false);
					idToOne[5] -= 4;
				}
			}
			
			if(idToOne[5] > 1 && ifToId[5] != 4 && ifToId[5] != 5) {
				int check = canForward(idToOne[2], false);
				if(check > -1) {
					idToOne[2] = forward(check, false);
					idToOne[5] -= 2;
				}
			}
			
			if(idToOne[5] > 0 && (ifToId[5] != 4 && ifToId[5] != 2 && ifToId[5] != 6)) {
				int check = canForward(idToOne[1], false);
				if(check > -1) {
					idToOne[1] = forward(check, false);
					idToOne[5] -= 1;
				}
			}
		}
		
		if(oneToTwo[4] == 0 || (oneToTwo[0] == -1)) {	
			
			if(idToOne[0] == 9 || idToOne[0] == 10) {//ADD
				String reg = decToBinary(idToOne[1]);
				String regTwo = decToBinary(idToOne[2]);
				
				for(int i = 0; i < 32; i++) {
					if(reg.charAt(i) == '1' && regTwo.charAt(i) == '1') {
						reg = reg.substring(0, i) + '1' + reg.substring(i+1);
					}
					else {
						reg = reg.substring(0, i) + '0' + reg.substring(i+1);
					}
				}
				
				idToOne[1] = binaryToDec(reg);
			}
			else if(idToOne[0] == 11 || idToOne[0] == 12) {//OR
				String reg = decToBinary(idToOne[1]);
				String regTwo = decToBinary(idToOne[2]);
				
				for(int i = 0; i < 32; i++) {
					if(reg.charAt(i) == '1' || regTwo.charAt(i) == '1') {
						reg = reg.substring(0, i) + '1' + reg.substring(i+1);
					}
					else {
						reg = reg.substring(0, i) + '0' + reg.substring(i+1);
					}
				}
				
				idToOne[1] = binaryToDec(reg);
			}
			
			for(int i = 0; i < 8; i++) {
				oneToTwo[i] = idToOne[i];
			}
			
			idToOne[0] = -1;
			
			idToOne[4] = 0;
			ifToId[4] = 0;
		}
		
	}
	
	private void exectutionTwo(){
		if(twoToThree[4] == 0 || (twoToThree[0] == -1)) {
			
			if(oneToTwo[0] == 3 || oneToTwo[0] == 4) {//ADD
				oneToTwo[1] += oneToTwo[2];
			}
			else if(oneToTwo[0] == 7 || oneToTwo[0] == 8) {//SUBSTRACT
				oneToTwo[1] -= oneToTwo[2];
			}
			
			for(int i = 0; i < 8; i++) {
				twoToThree[i] = oneToTwo[i];
			}
			
			oneToTwo[0] = -1;

			oneToTwo[4] = 0;
			idToOne[4] = 0;
			ifToId[4] = 0;
		}
		
	}
	
	private void exectutionThree(){
		if(threeToFour[4] == 0 || (threeToFour[0] == -1)) {
			
			for(int i = 0; i < 8; i++) {
				threeToFour[i] = twoToThree[i];
			}
			
			twoToThree[0] = -1;

			twoToThree[4] = 0;
			oneToTwo[4] = 0;
			idToOne[4] = 0;
			ifToId[4] = 0;
		}
		
	}
	
	private void exectutionFour(){
		if(fourToMem[4] == 0) {
			
			if(threeToFour[0] == 5 || threeToFour[0] == 6) {//MULTIPLY
				threeToFour[1] *= threeToFour[2];
			}
			
			for(int i = 0; i < 8; i++) {
				fourToMem[i] = threeToFour[i];
			}
			if(threeToFour[0] != -1 && threeToFour[0] < 13) {
				int[] change = phase.get(threeToFour[7]);
				change[2] = cycle;
				phase.set(threeToFour[7], change);
			}
			
			threeToFour[0] = -1;
			
			threeToFour[4] = 0;
			twoToThree[4] = 0;
			oneToTwo[4] = 0;
			idToOne[4] = 0;
			ifToId[4] = 0;
			
		}
		
	}
	
	private void memoryRun(){
		if(memToWb[4] == 0) {
			
			if(fourToMem[0] == 0) {
				String see = mainMemory.dataGet((int)(fourToMem[1] + fourToMem[2])/8, false);
				
				if(see.equals("Fail")) {
					memToWb[0] = -1;
					fourToMem[4] = 1;
					threeToFour[4] = 1;
					twoToThree[4] = 1;
					oneToTwo[4] = 1;
					idToOne[4] = 1;
					ifToId[4] = 1;
					return;
				}

				else {
					fourToMem[1] = binaryToDec(see);
				}
			}
			else if(fourToMem[0] == 1) {
				String see = mainMemory.dataSet((int)(fourToMem[1] + fourToMem[2])/8, decToBinary(fourToMem[3]));
				if(see.equals("Fail")) {
					memToWb[0] = -1;
					fourToMem[4] = 1;
					threeToFour[4] = 1;
					twoToThree[4] = 1;
					oneToTwo[4] = 1;
					idToOne[4] = 1;
					ifToId[4] = 1;
					return;
				}
			}
			
			for(int i = 0; i < 8; i++) {
				memToWb[i] = fourToMem[i];
			}
			if(fourToMem[0] != -1 && fourToMem[0] < 13) {
				int[] change = phase.get(fourToMem[7]);
				change[3] = cycle;
				phase.set(fourToMem[7], change);
			}
			
			fourToMem[0] = -1;
			
			fourToMem[4] = 0;
			threeToFour[4] = 0;
			twoToThree[4] = 0;
			oneToTwo[4] = 0;
			idToOne[4] = 0;
			ifToId[4] = 0;
		}
		
	}
	
	private void writeBack(){
		if(memToWb[0] != -1 && memToWb[0] < 13) {
			
			if(memToWb[0] != 1) {
				registerFile.setRegister(memToWb[3],memToWb[1]);
			}
			
			int[] change = phase.get(memToWb[7]);
			change[4] = cycle;
			phase.set(memToWb[7], change);
		}
		else if(memToWb[0] == 16) {
			canRun = false;
		}
		
		memToWb[0] = -1;
		
		memToWb[4] = 0;
		fourToMem[4] = 0;
		threeToFour[4] = 0;
		twoToThree[4] = 0;
		oneToTwo[4] = 0;
		idToOne[4] = 0;
		ifToId[4] = 0;
	}
	
	private boolean predictStall(int reg) {//Used to predict if the system needs to stall
		if(oneToTwo[3] == reg && oneToTwo[0] != 1 && oneToTwo[0] != 13 && oneToTwo[0] != 14 && oneToTwo[0] != 15 && oneToTwo[0] != 16 && oneToTwo[0] > -1) {
			if(oneToTwo[0] == 2 || (oneToTwo[0] >= 9 && oneToTwo[0] <= 12)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if(twoToThree[3] == reg && twoToThree[0] != 1 && twoToThree[0]!= 13 && twoToThree[0] != 14 && twoToThree[0] != 15 && twoToThree[0] > -1) {
			if(twoToThree[0] >= 2 && twoToThree[0] <= 12 && twoToThree[0] != 5 && twoToThree[0] != 6) {
				return true;
			}
			else {
				return false;
			}
		}
		else if(threeToFour[3] == reg && threeToFour[0] != 1 && threeToFour[0] != 13 && threeToFour[0] != 14 && threeToFour[0]!= 15 && threeToFour[0] > -1) {
			if(threeToFour[0] >= 2 && threeToFour[0] <= 12 && threeToFour[0] != 5 && threeToFour[0] != 6) {
				return true;
			}
			else {
				return false;
			}
		}
		else if(fourToMem[3] == reg && fourToMem[0] != 1 && fourToMem[0] != 13 && fourToMem[0] != 14 && fourToMem[0] != 15 && fourToMem[0] > -1) {
			if(fourToMem[0] >= 0 && fourToMem[0] <= 12) {
				return true;
			}
			else {
				return false;
			}
		}
		else if(memToWb[3] == reg && memToWb[0] != 1 && memToWb[0] != 13 && memToWb[0] != 14 && memToWb[0] != 15 && memToWb[0] > -1) {
			if(memToWb[0] >= 0 && memToWb[0] <= 12) {
				return true;
			}
			else {
				return false;
			}
		}
		
		return false;
	}
	
	private int canForward(int reg, boolean decode) {//Returns location of forward
		if(oneToTwo[3] == reg && decode && oneToTwo[0] != 1 && oneToTwo[0] != 13 && oneToTwo[0] != 14 && oneToTwo[0] != 15 && oneToTwo[0] != 16 && oneToTwo[0] > -1) {
			if(oneToTwo[0] == 2) {
				return 1;
			}
			else {
				return -1;
			}
		}
		else if(twoToThree[3] == reg && twoToThree[0] != 1 && twoToThree[0]!= 13 && twoToThree[0] != 14 && twoToThree[0] != 15 && twoToThree[0] > -1) {
			if((twoToThree[0] >= 9 && twoToThree[0] <= 12) || twoToThree[0] == 2) {
				return 2;
			}
			else {
				return -1;
			}
		}
		else if(threeToFour[3] == reg && threeToFour[0] != 1 && threeToFour[0] != 13 && threeToFour[0] != 14 && threeToFour[0]!= 15 && threeToFour[0] > -1) {
			if(threeToFour[0] >= 2 && threeToFour[0] <= 12 && threeToFour[0] != 5 && threeToFour[0] != 6) {
				return 3;
			}
			else {
				return -1;
			}
		}
		else if(fourToMem[3] == reg && fourToMem[0] != 1 && fourToMem[0] != 13 && fourToMem[0] != 14 && fourToMem[0] != 15 && fourToMem[0] > -1) {
			if(fourToMem[0] >= 2 && fourToMem[0] <= 12 && fourToMem[0] != 5 && fourToMem[0] != 6) {
				return 4;
			}
			else {
				return -1;
			}
		}
		else if(memToWb[3] == reg && memToWb[0] != 1 && memToWb[0] != 13 && memToWb[0] != 14 && memToWb[0] != 15 && memToWb[0] > -1) {
			if(memToWb[0] >= 0 && memToWb[0] <= 12) {
				return 5;
			}
			else {
				return -1;
			}
		}
		return -1;
	}
	
	private int forward(int place, boolean decode) {//Sets the forward
		if(place == 1 && decode) {
			return oneToTwo[1];
		}
		else if(place == 2) {
			return twoToThree[1];
		}
		else if(place == 3) {
			return threeToFour[1];
		}
		else if(place == 4) {
			return fourToMem[1];
		}
		else if(place == 5) {
			return memToWb[1];
		}
		return -1;
	}
	
	private boolean needForward(int reg) {//Detects if a variable needs forwarding
		if(idToOne[3] == reg && idToOne[0] > -1 && idToOne[0] < 13 && idToOne[0] != 1) {
			return true;
		}
		else if(oneToTwo[3] == reg && oneToTwo[0] > -1 && oneToTwo[0] < 13 && oneToTwo[0] != 1) {
			return true;
		}
		else if(twoToThree[3] == reg && twoToThree[0] > -1 && twoToThree[0] < 13 && twoToThree[0] != 1) {
			return true;
		}
		else if(threeToFour[3] == reg && threeToFour[0] > -1 && threeToFour[0] < 13 && threeToFour[0] != 1) {
			return true;
		}
		else if(fourToMem[3] == reg && fourToMem[0] > -1 && fourToMem[0] < 13 && fourToMem[0] != 1) {
			return true;
		}
		else if(memToWb[3] == reg && memToWb[0] > -1 && memToWb[0] < 13 && memToWb[0] != 1) {
			return true;
		}
		return false;
	}
	
	private int binaryToDec(String word) {
		int number = 0;
		
		if(word.charAt(0) == '0') {
			int factor = 1;	
			for(int i = 31; i >= 0; i--){//Convert binary into decimal int
				number += (((int)word.charAt(i) - 48) * factor);
				factor *= 2;
			}
			
		}
		else {
			int index = 0;
			while(word.charAt(index) == '0' || index < 32) {//Subtract one in binary
				word = word.substring(0, index) + '1' + word.substring(index+1);
				index++;
			}
			
			word = word.substring(0, index) + '0' + word.substring(index+1);
			
			
			for(int i = 0; i > 32; i++) {//Flip it
				if(word.charAt(i) == 0) {
					word = word.substring(0, i) + '1' + word.substring(i+1);
				}
				else {
					word = word.substring(0, i) + '0' + word.substring(i+1);
				}
			}
			
			int factor = 1;	
			for(int i = 31; i >= 0; i--){//Convert binary into decimal int
				number += (((int)word.charAt(i) - 48) * factor);
				factor *= 2;
			}
			
			number *= -1;//Times by -1
		}
		
		return number;
	}
	
	private String decToBinary(int value) {
		String word = new String();
		
		if(value >= 0) {
			long factor = 4294967296L;	
			for(int i = 0; i > 32; i++){//Convert decimal into binary string
				if(value - factor >= 0) {
					word += '1';
				}
				else {
					word += '0';
				}
				
				factor /= 2;
			}
		}
		else {
			value *= -1;
			long factor = 4294967296L;	
			for(int i = 0; i > 32; i++){//Convert decimal into binary string
				if(value - factor >= 0) {
					word += '1';
				}
				else {
					word += '0';
				}
				
				factor /= 2;
			}
			
			for(int i = 0; i > 32; i++) {//Flip it
				if(word.charAt(i) == 0) {
					word = word.substring(0, i) + '1' + word.substring(i+1);
				}
				else {
					word = word.substring(0, i) + '0' + word.substring(i+1);
				}
			}
			
			int i = 0;
			while(word.charAt(i) == '1' || i < 32) {//Add one in binary
				word = word.substring(0, i) + '0' + word.substring(i+1);
				i++;
			}
			
			word = word.substring(0, i) + '1' + word.substring(i+1);
		}
		
		return word;
	}
}
