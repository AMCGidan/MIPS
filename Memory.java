import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
public class Memory {
	private String[] memory;
	private String[] function;
	private int functionLength;
	
	private InstructionCache IC;
	private DataCache DC;
	
	public boolean ICOn;
	public boolean DCOn;
	
	public int totalIC;
	public int hitIC;
	
	public int totalDC;
	public int hitDC;
	
	private int line;
	
	private int fill;
	private int stall;
	
	public Memory(){
		memory = new String[64];
		function = new String[64];
		for(int i = 0; i < 64; i++) {
			function[i] = "";
		}
		functionLength = 0;
		
		IC = new InstructionCache();
		DC = new DataCache();
		
		ICOn = false;
		DCOn = false;
		
		totalIC = 0;
		hitIC = 0;
		
		totalDC = 0;
		hitDC = 0;
		
		line = 0;
		
		fill = 0;
		stall = 0;
	}
	
	public void readFile(String fileInstruction, String fileData){//Reads the file data into the memory at start
		try{
			File myObj = new File(fileInstruction);
			Scanner myReader = new Scanner(myObj);
			int index = 0;

			while(myReader.hasNextLine()){
				String word = myReader.nextLine();
				int place = word.length();
				
				if(word.indexOf("#") != -1) {
					place = word.indexOf("#");
				}
				
				memory[index] = word.substring(0, place);

				if((int)memory[index].charAt(0) != 9){//If there is a function start
					int i = 0;
					while((int)memory[index].charAt(i) != 9){
						function[functionLength] += memory[index].charAt(i);
						i++;
					}

					function[functionLength] = function[functionLength].toUpperCase();
					function[functionLength + 1] = String.valueOf(index);
					functionLength += 2;
				}
				
				index++;
			}
			
			myReader.close();
		}
		catch(FileNotFoundException e){
			System.out.println("An error has occurred trying to read instruction file.");
      e.printStackTrace();
		}

		try{
			File myObj = new File(fileData);
			Scanner myReader = new Scanner(myObj);
			int index = 32;
			
			while(myReader.hasNextLine()){
				String word = myReader.nextLine();
				memory[index] = word;
				
				index++;
			}
			myReader.close();
		}
		catch(FileNotFoundException e){
			System.out.println("An error has occurred trying to read data file.");
      e.printStackTrace();
		}
	}
	
	public boolean canRunICMiss() {
		String item = IC.getICache(line);
		if(item == null) {
			return false;
		}
		else if(item.equals("Fail") && !DCOn && fill != 3 && stall != 2) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public String instructionFetch() {
		String item = IC.getICache(line);
		if(item == null) {
			return "Stop";
		}
		if(item.equals("Fail") && !DCOn) {
			ICOn = true;
			stall++;
			if(stall == 3) {
				IC.setICache((line - (line % 4)) + fill, memory[line - (line % 4) + fill]);
				fill++;
				stall = 0;
			}
			if(fill == 4) {
				item = IC.getICache(line);
				fill = 0;
				stall = 0;
				totalIC++;
				line++;
				ICOn = false;
				
				if(item == null) {
					return "Stop";
				}
				
				return item;
			}
			
		}
		else if(!item.equals("Fail")) {
			totalIC++;
			hitIC++;
			line++;
			if(line > 31) {//Fail safe
				line = 0;
			}
			return item;
		}
		
		return "Fail";
	}
	
	public String dataGet(int place, boolean setBlock){
		String item = DC.getDCache(place);
		if(item.equals("Fail") && !ICOn) {
			DCOn = true;
			stall++;
			if(stall == 3) {
				DC.setDCache((place - (place%4)) + fill, memory[(place - (place%4)) + fill], memory);
				fill++;
				stall = 0;
			}
			if(fill == 4) {
				item = DC.getDCache(place);
				stall = 0;
				fill = 0;
				totalDC++;
				DCOn = false;
				if(!setBlock) {
					DC.lock = -1;
				}
				return item;
			}
			
		}
		else if(!item.equals("Fail")) {
			totalDC++;
			hitDC++;
			return item;
		}
		
		return item;
	}
	
	public String dataSet(int place, String value) {
		String theItem = dataGet(place, true);
		
		if(!theItem.equals("Fail")) {
			DC.setDCache(place, value, memory);
			DC.lock = -1;
		}
		
		return theItem;
	}
	
	public void jump(int value) {
		line = value;
	}
	
	public int[] convertInstruction(String theWord){//Convert a instruction in memory to cache
		String look = theWord;
		String[] word = {"", "", "", ""};
		int[] code = new int[4];
		boolean stop = false;
		int current = 0;
		
		//System.out.println(look);
		
		if((int)look.charAt(current) != 9 && (int)look.charAt(current) != 32){//Cut function off
			current = 1;
			while((int)look.charAt(current) != 58){
				current++;
			}
			look = look.substring(current+1);
			current = 0;
		}
		
		while(current < look.length() && !stop){
			if((int)look.charAt(current) != 9 && (int)look.charAt(current) != 32 && (int)look.charAt(current) != 44){
				stop = true;
			}
			else {
				current++;
			}
		}
		stop = false;	
		
		while(current < look.length() && !stop){//Get first line
			if((int)look.charAt(current) != 9 && (int)look.charAt(current) != 32 && (int)look.charAt(current) != 44){
				word[0] += look.charAt(current);
				current++;
			}
			else{
				stop = true;
				word[0] = word[0].toUpperCase();
			}
		}

		stop = false;
		while(current < look.length() && !stop){
			if((int)look.charAt(current) != 9 && (int)look.charAt(current) != 32 && (int)look.charAt(current) != 44){
				stop = true;
			}
			else {
				current++;
			}
		}
		stop = false;	

		while(current < look.length() && !stop){//Get second line
			if((int)look.charAt(current) != 9 && (int)look.charAt(current) != 32 && (int)look.charAt(current) != 44){
				word[1] += look.charAt(current);
				current++;
			}
			else{
				stop = true;
			}
		}

		stop = false;
		while(current < look.length() && !stop){
			if((int)look.charAt(current) != 9 && (int)look.charAt(current) != 32 && (int)look.charAt(current) != 44){
				stop = true;
			}
			else {
				current++;
			}
		}
		stop = false;	

		while(current < look.length() && !stop){//Get third line
			if((int)look.charAt(current) != 9 && (int)look.charAt(current) != 32 && (int)look.charAt(current) != 44){
				word[2] += look.charAt(current);
				current++;
			}
			else{
				stop = true;
			}
		}

		stop = false;
		while(current < look.length() && !stop){
			if((int)look.charAt(current) != 9 && (int)look.charAt(current) != 32 && (int)look.charAt(current) != 44){
				stop = true;
			}
			else {
				current++;
			}
		}
		stop = false;	

		while(current < look.length() && !stop){//Get fourth line
			if((int)look.charAt(current) != 9 && (int)look.charAt(current) != 32 && (int)look.charAt(current) != 44 && (int)look.charAt(current) != 35){
				word[3] += look.charAt(current);
				current++;
			}
			else{
				stop = true;
			}
		}
		
//		for(int i = 0; i < 4; i++) {
//			System.out.println(word[i]);
//		}
		
		if(word[0].contains("ADDI") || word[0].contains("MULI") || word[0].contains("SUBI") || word[0].contains("ANDI") || word[0].contains("ORI")){
			if(word[0].contains("ADDI")){
				code[0] = 4;
			}
			else if(word[0].contains("MULTI")){
				code[0] = 6;
			}
			else if(word[0].contains("SUBI")){
				code[0] = 8;
			}
			else if(word[0].contains("ANDI")){
				code[0] = 10;
			}
			else{
				code[0] = 12;
			}
			
			code[1] = Integer.parseInt(word[1].substring(1));
			code[2] =  Integer.parseInt(word[2].substring(1));
			code[3] = Integer.parseInt(word[3]);
		}
		else if(word[0].contains("ADD") || word[0].contains("MUL") || word[0].contains("SUB") || word[0].contains("AND") || word[0].contains("OR")){
			if(word[0].contains("ADD")){
				code[0] = 3;
			}
			else if(word[0].contains("MULT")){
				code[0] = 5;
			}
			else if(word[0].contains("SUB")){
				code[0] = 7;
			}
			else if(word[0].contains("AND")){
				code[0] = 9;
			}
			else{
				code[0] = 11;
			}
			code[1] =  Integer.parseInt(word[1].substring(1));
			code[2] = Integer.parseInt(word[2].substring(1));
			code[3] = Integer.parseInt(word[3].substring(1));
		}
		else if(word[0].contains("BEQ") || word[0].contains("BNE")){
			if(word[0].contains("BEQ")){
				code[0] = 13;
			}
			else{
				code[0] = 14;
			}
			code[2] = Integer.parseInt(word[1].substring(1));
			code[3] = Integer.parseInt(word[2].substring(1));
			for(int i = 0; i < functionLength; i += 2){
				if(word[3].equals(function[i].substring(0, function[i].length() - 1))){
					code[1] = Integer.parseInt(function[i + 1]);
				}
			}
		}
		else if(word[0].contains("LW") || word[0].contains("SW") || word[0].contains("LI")){
			if(word[0].contains("LW")){
				code[0] = 0;
				code[1] = Integer.parseInt(word[1].substring(1));
				code[2] = Integer.parseInt(word[2].substring(word[2].indexOf("R") + 1, word[2].indexOf(")")));
				code[3] = Integer.parseInt(word[2].substring(0, word[2].indexOf("(")));
			}
			else if(word[0].contains("SW")){
				code[0] = 1;
				code[1] = Integer.parseInt(word[1].substring(1));
				code[2] = Integer.parseInt(word[2].substring(word[2].indexOf("R") + 1, word[2].indexOf(")")));
				code[3] = Integer.parseInt(word[2].substring(0, word[2].indexOf("(")));
			}
			else{
				code[0] = 2;
				code[1] = Integer.parseInt(word[1].substring(1));
				code[2] = Integer.parseInt(word[2]);
			}
		}
		else if(word[0].contains("J")){
			code[0] = 15;
			for(int i = 0; i < functionLength; i += 2){
				if(word[1].equals(function[i].substring(0, function[i].length() - 1))){
					code[1] = Integer.parseInt(function[i + 1]);
				}
			}
		}
		else{
			code[0] = 16;
		}
		
		return code;
	}
	
	public void lineBack() {
		line--;
	}
}
