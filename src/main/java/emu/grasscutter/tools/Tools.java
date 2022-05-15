package emu.grasscutter.tools;

import emu.grasscutter.Grasscutter;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public final class Tools {
	public static void createGmHandbook() throws Exception {
		ToolsWithLanguageOption.createGmHandbook(getLanguageOption());
	}

	public static void createGachaMapping(String location) throws Exception {
		ToolsWithLanguageOption.createGachaMapping(location, getLanguageOption());
	}

	public static List<String> getAvailableLanguage() throws Exception {
		File textMapFolder = new File(Grasscutter.getConfig().RESOURCE_FOLDER + "TextMap");
		List<String> availableLangList = new ArrayList<String>();
		for (String textMapFileName : textMapFolder.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.startsWith("TextMap") && name.endsWith(".json")){
					return true;
				}
				return false;
			}
		})) {
			availableLangList.add(textMapFileName.replace("TextMap","").replace(".json","").toLowerCase());
		}
		return availableLangList;
	}

	public static String getLanguageOption() throws Exception {
		List<String> availableLangList = getAvailableLanguage();
	
		// Use system out for better format
		if (availableLangList.size() == 1) {
			return availableLangList.get(0).toUpperCase();
		}
		String stagedMessage = "";
		stagedMessage += "The following languages mappings are available, please select one: [default: EN]\n";
		String groupedLangList = ">\t";
		int groupedLangCount = 0;
		String input = "";
		for (String availableLanguage: availableLangList){
			groupedLangCount++;
			groupedLangList = groupedLangList + "" + availableLanguage + "\t";
			if (groupedLangCount == 6) {
				stagedMessage += groupedLangList + "\n";
				groupedLangCount = 0;
				groupedLangList = ">\t";
			}
		}
		if (groupedLangCount > 0) {
			stagedMessage += groupedLangList + "\n";
		}
		stagedMessage += "\nYour choice:[EN] ";
		
		input = Grasscutter.getConsole().readLine(stagedMessage);
		if (availableLangList.contains(input.toLowerCase())) {
			return input.toUpperCase();
		}
		Grasscutter.getLogger().info("Invalid option. Will use EN(English) as fallback");

		return "EN";
	}
}

