
public class DataCache {
	private String[][][] dataCache;
	private int[][][] DCPlace;
	private int LLRU;
	private int RLRU;
	public int lock;

	public DataCache(){
		dataCache = new String[2][2][4];
		DCPlace = new int[2][2][4];
		for(int i = 0; i < 2; i++) {
			for(int j = 0; j < 2; j++) {
				for(int k = 0; k < 4; k++) {
					DCPlace[i][j][k] = -1;
					dataCache[i][j][k] = "";
				}
			}
		}
		LLRU = 0;
		RLRU = 0;
		lock = -1;
	}

	public String getDCache(int place){
		int bA = (int)(place/4);
		if(DCPlace[bA % 2][0][3] != (place - (place%4)) + 3 && DCPlace[bA % 2][1][3] != (place - (place%4)) + 3) {
			return "Fail";
		}
		else {
			if(DCPlace[bA % 2][0][0] == (place - (place%4))) {
				if(bA%2 == 0) {
					LLRU = 1;
				}
				else {
					RLRU = 1;
				}
				lock = 0;
				return dataCache[bA % 2][0][place%4];
			}
			else {
				if(bA%2 == 0) {
					LLRU = 0;
				}
				else {
					RLRU = 0;
				}
				lock = 1;
				return dataCache[bA % 2][1][place%4];
			}
			
		}
	}

	public void setDCache(int place, String value, String[] memory){
		int bA = (int)(place/4);
		int space = 0;
		if(lock > -1) {//For any insert of a new block so that every time after the first the lock is set to the LRU
			if(bA%2 == 0) {
				space = LLRU;
				if(LLRU == 1) {
					LLRU = 0;
					lock = 1;
				}
				else {
					LLRU = 1;
					lock = 0;
				}
			}
			else {
				space = RLRU;
				if(RLRU == 1) {
					RLRU = 0;
					lock = 1;
				}
				else {
					RLRU = 1;
					lock = 0;
				}
			}
		}
		else {
			if(bA%2 == 0) {
				space = LLRU;
				if(LLRU == 1) {
					LLRU = 0;
				}
				else {
					LLRU = 1;
				}
			}
			else {
				space = RLRU;
				if(RLRU == 1) {
					RLRU = 0;
				}
				else {
					RLRU = 1;
				}
			}
		}
		
		//System.out.println(space);
		
		if(DCPlace[bA % 2][space][place%4] != -1) {
			memory[DCPlace[bA % 2][space][place%4]] = dataCache[bA % 2][space][place%4];
		}
		DCPlace[bA % 2][space][place%4] = place;
		dataCache[bA % 2][space][place%4] = value;
	}
}
