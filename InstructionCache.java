public class InstructionCache {
	private String[][] instructionCache;
	private int[][] ICPlace;

	public InstructionCache(){
		instructionCache = new String[4][4];
		ICPlace = new int[4][4];
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				ICPlace[i][j] = -1;
			}
		}
	}

	public String getICache(int place){
		int bA = (int)(place/4);
		if(ICPlace[bA % 4][3] != (place - (place%4) + 3)) {
			return "Fail";
		}	
		
		else {
			return instructionCache[bA % 4][place%4];
		}
	}

	public void setICache(int place, String value){
		int bA = (int)(place/4);
		ICPlace[bA % 4][place%4] = place;
		instructionCache[bA % 4][place%4] = value;
	}
}
