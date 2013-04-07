import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

/**
 * @author Olle
 */
public class Main {

	private static final int REQUIRED_FILES = 7;
	// only lower case.
	private static final List<String> FILE_ENDINGS_TO_REMOVE = Arrays.asList(
			"mkv", "avi", "mp4");
	private static final Scanner scanner = new Scanner(System.in);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String startDir = getStartDirectory();

		File start = new File(startDir);

		// Gets all files and dirs recursively
		System.out.println("Detta kan ta lite tid..");
		Collection<File> files = FileUtils.listFilesAndDirs(start,
				new NoneFilteringFilter(), new NoneFilteringFilter());

		Collection<File> filesToRemove = new ArrayList<File>();
		for (File dir : files) {
			if (dir.isDirectory()) {
				if (getExtractedRar(dir) != null)
					filesToRemove.add(getExtractedRar(dir));
			}
		}

		talkToUser(filesToRemove);
	}

	private static String getStartDirectory() {
		System.out
				.println("Skriv in path till start, te x 'E:\\\\'(sökning sker rekursivt)");
		String path = scanner.nextLine();
		return path;
	}

	private static File getExtractedRar(File dir) {
		if (!dir.isDirectory())
			throw new IllegalArgumentException("det där var ju inget dirr!");

		File[] filesInDir = dir.listFiles();
		if (filesInDir == null)
			return null;
		List<File> files = new ArrayList<File>();
		// add the files that might be rars or extracts to files
		for (int i = 0; i < filesInDir.length; i++) {
			File curr = filesInDir[i];
			// rars and extracts will always contains "."
			if (!curr.getName().contains("."))
				continue;
			files.add(curr);
		}

		Map<String, List<File>> filesPerPaths = new HashMap<String, List<File>>();

		for (File f : files)
			addPathToMap(f, filesPerPaths);

		return getExtractedFromMap(filesPerPaths);
	}

	private static void addPathToMap(File file, Map<String, List<File>> paths) {
		String noEndingPath = removeEnding(file.getAbsolutePath());
		if (paths.containsKey(noEndingPath)) {
			paths.get(noEndingPath).add(file);
		} else {
			List<File> fileList = new ArrayList<File>();
			fileList.add(file);
			paths.put(noEndingPath, fileList);
		}
	}

	/**
	 * @param withEnding
	 *            needs to contain "." !!
	 */
	private static String removeEnding(String withEnding) {
		String fileEnding = withEnding.substring(withEnding.lastIndexOf("."));
		return withEnding.substring(0, withEnding.lastIndexOf(fileEnding));
	}

	private static File getExtractedFromMap(Map<String, List<File>> paths) {
		for (Map.Entry<String, List<File>> me : paths.entrySet()) {
			if (me.getValue().size() < REQUIRED_FILES)
				continue;

			// nu har vi hittat en rar-arkiv, vi måste bara hitta extrakten
			long biggestSize = 0;
			File biggestFile = null;
			for (File f : me.getValue()) {
				long mySize = FileUtils.sizeOf(f);
				if (biggestSize < mySize) {
					biggestFile = f;
					biggestSize = mySize;
				}
			}
			if (isFileToRemove(biggestFile))
				return biggestFile;
		}

		return null;
	}

	private static boolean isFileToRemove(File f) {
		String path = f.getAbsolutePath();
		String fileEnding = path.substring(path.lastIndexOf(".") + 1)
				.toLowerCase();
		return FILE_ENDINGS_TO_REMOVE.contains(fileEnding);
	}

	private static void talkToUser(Collection<File> filesToRemove) {
		if (filesToRemove.isEmpty()) {
			System.out.println("Hittade inga filer.");
			closeProgram();
		}

		for (File f : filesToRemove) {
			System.out.println(f.getAbsolutePath());
		}

		System.out
				.println("\n********************************************************************");
		System.out.println("Ta bort ovan listade filer? (ja/nej)");

		String answer = "nej";
		if (scanner.hasNextLine())
			answer = scanner.nextLine();
		if ("ja".equalsIgnoreCase(answer)) {
			for (File removeMe : filesToRemove)
				if (!removeMe.delete())
					System.out.println("Lyckades inte ta bort: "
							+ removeMe.getAbsolutePath());
		}

		closeProgram();
	}

	private static void closeProgram() {
		System.out.println("Avslutar programmet.");
		scanner.close();
		System.exit(0);
	}
}
