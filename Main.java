import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {
		CPU theCPU = new CPU();
		theCPU.readData("inst.txt", "data.txt");
		theCPU.run();
		theCPU.outputFile();
	}
}