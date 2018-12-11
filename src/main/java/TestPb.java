import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestPb {
	public static void main(String[] args) {
		List<String> script = new ArrayList<String>();
		script.add("java");
		script.add("-jar");
		script.add("./cifi4-1.0.0.jar");
		try {
			System.out.println("executing " + script);
			System.out.println(execute(script));
		} catch (IOException e) {
			System.out.println("ioe:" + e.getMessage());
		} catch (InterruptedException e) {
			System.out.println("ie :" + e.getMessage());
		}
	}
	private static String execute(List<String> script) throws IOException, InterruptedException {
		new ProcessBuilder().command(script).start();
		return "executed" + script;
	}
}
