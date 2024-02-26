public class Register {
	private int[] registers;

	public Register(){
		registers = new int[32];
	}
	
	public int getRegister(int index){
		return registers[index];
	}

	public void setRegister(int index, int save){
		registers[index] = save;
	}
}