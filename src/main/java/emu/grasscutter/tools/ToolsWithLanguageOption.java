package emu.grasscutter.tools;

import com.google.gson.reflect.TypeToken;
import emu.grasscutter.GameConstants;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandMap;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.ResourceLoader;
import emu.grasscutter.data.def.AvatarData;
import emu.grasscutter.data.def.ItemData;
import emu.grasscutter.data.def.MonsterData;
import emu.grasscutter.data.def.SceneData;
import emu.grasscutter.utils.Utils;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class ToolsWithLanguageOption {

    @SuppressWarnings("deprecation")
    public static void createGmHandbook(String language) throws Exception {
        ResourceLoader.loadResources();

        Map<Long, String> map;
        try (InputStreamReader fileReader = new InputStreamReader(
                new FileInputStream(Utils.toFilePath(
                        Grasscutter.getConfig().RESOURCE_FOLDER + "TextMap/TextMap" + language
                                + ".json")), StandardCharsets.UTF_8)) {
            map = Grasscutter.getGsonFactory()
                    .fromJson(fileReader, new TypeToken<Map<Long, String>>() {
                    }.getType());
        }

        List<Integer> list;
        String fileName = "./GM Handbook.txt";
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8),
                false)) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();

            writer.println("// Grasscutter " + GameConstants.VERSION + " GM Handbook");
            writer.println("// Created " + dtf.format(now) + System.lineSeparator()
                    + System.lineSeparator());

            CommandMap cmdMap = new CommandMap(true);
            List<Command> cmdList = new ArrayList<>(cmdMap.getAnnotationsAsList());

            writer.println("// Commands");
            for (Command cmd : cmdList) {
                String cmdName = cmd.label();
                while (cmdName.length() <= 15) {
                    cmdName = " " + cmdName;
                }
                writer.println(cmdName + " : " + cmd.description());
            }

            writer.println();

            list = new ArrayList<>(GameData.getAvatarDataMap().keySet());
            Collections.sort(list);

            writer.println("// Avatars");
            for (Integer id : list) {
                AvatarData data = GameData.getAvatarDataMap().get(id);
                writer.println(data.getId() + " : " + map.get(data.getNameTextMapHash()));
            }

            writer.println();

            list = new ArrayList<>(GameData.getItemDataMap().keySet());
            Collections.sort(list);

            writer.println("// Items");
            for (Integer id : list) {
                ItemData data = GameData.getItemDataMap().get(id);
                writer.println(data.getId() + " : " + map.get(data.getNameTextMapHash()));
            }

            writer.println();

            writer.println("// Scenes");
            list = new ArrayList<>(GameData.getSceneDataMap().keySet());
            Collections.sort(list);

            for (Integer id : list) {
                SceneData data = GameData.getSceneDataMap().get(id);
                writer.println(data.getId() + " : " + data.getScriptData());
            }

            writer.println();

            writer.println("// Monsters");
            list = new ArrayList<>(GameData.getMonsterDataMap().keySet());
            Collections.sort(list);

            for (Integer id : list) {
                MonsterData data = GameData.getMonsterDataMap().get(id);
                writer.println(data.getId() + " : " + map.get(data.getNameTextMapHash()));
            }
        }

        Grasscutter.getLogger().info("GM Handbook generated!");
    }

    @SuppressWarnings("deprecation")
    public static void createGachaMapping(String location, String language) throws Exception {
        ResourceLoader.loadResources();

        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(location), StandardCharsets.UTF_8),
                false)) {
            // if the user made choices for language, I assume it's okay to assign his/her selected language to "en-us"
            // since it's the fallback language and there will be no difference in the gacha record page.
            // The enduser can still modify the `gacha_mappings.js` directly to enable multilingual for the gacha record system.
            writer.print("mappings = {\"en-us\": ");
            writer.println(createGachaMappingJson(language));
            writer.println("\n}");
        }

        Grasscutter.getLogger().info("Mappings generated to " + location + " !");
    }


    public static String createGachaMappingJson(String language) {
        ResourceLoader.loadResources();

        Map<Long, String> map;
        try (InputStreamReader fileReader = new InputStreamReader(
                new FileInputStream(Utils.toFilePath(Grasscutter.getConfig().RESOURCE_FOLDER +
                        "TextMap/TextMap" + language + ".json")),
                StandardCharsets.UTF_8)) {
            map = Grasscutter.getGsonFactory()
                    .fromJson(fileReader, new TypeToken<Map<Long, String>>() {
                    }.getType());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Integer> list;

        final StringBuilder sb = new StringBuilder();
        list = new ArrayList<>(GameData.getAvatarDataMap().keySet());
        Collections.sort(list);

        final String newLine = System.lineSeparator();

        // if the user made choices for language, I assume it's okay to assign his/her selected language to "en-us"
        // since it's the fallback language and there will be no difference in the gacha record page.
        // The enduser can still modify the `gacha_mappings.js` directly to enable multilingual for the gacha record system.
        sb.append("{").append(newLine);

        // Avatars
        boolean first = true;
        for (Integer id : list) {
            AvatarData data = GameData.getAvatarDataMap().get(id);
            int avatarID = data.getId();
            if (avatarID >= 11000000) { // skip test avatar
                continue;
            }
            if (first) { // skip adding comma for the first element
                first = false;
            } else {
                sb.append(",");
            }
            String color;
            switch (data.getQualityType()) {
                case "QUALITY_PURPLE":
                    color = "purple";
                    break;
                case "QUALITY_ORANGE":
                    color = "yellow";
                    break;
                case "QUALITY_BLUE":
                default:
                    color = "blue";
            }
            // Got the magic number 4233146695 from manually search in the json file
            sb.append("\"")
                    .append(avatarID % 1000 + 1000)
                    .append("\" : [\"")
                    .append(map.get(data.getNameTextMapHash()))
                    .append("(")
                    .append(map.get(4233146695L))
                    .append(")\", \"")
                    .append(color)
                    .append("\"]")
                    .append(newLine);
        }

        list = new ArrayList<>(GameData.getItemDataMap().keySet());
        Collections.sort(list);

        // Weapons
        for (Integer id : list) {
            ItemData data = GameData.getItemDataMap().get(id);
            if (data.getId() <= 11101 || data.getId() >= 20000) {
                continue; //skip non weapon items
            }
            String color;

            switch (data.getRankLevel()) {
                case 3:
                    color = "blue";
                    break;
                case 4:
                    color = "purple";
                    break;
                case 5:
                    color = "yellow";
                    break;
                default:
                    continue; // skip unnecessary entries
            }

            // Got the magic number 4231343903 from manually search in the json file

            sb.append(",\"")
                    .append(data.getId())
                    .append("\" : [\"")
                    .append(map.get(data.getNameTextMapHash()).replaceAll("\"", ""))
                    .append("(")
                    .append(map.get(4231343903L))
                    .append(")\",\"")
                    .append(color)
                    .append("\"]")
                    .append(newLine);
        }
        sb.append(",\"200\": \"")
                .append(map.get(332935371L))
                .append("\", \"301\": \"")
                .append(map.get(2272170627L))
                .append("\", \"302\": \"")
                .append(map.get(2864268523L))
                .append("\"")
                .append("}\n}")
                .append(newLine);
        return sb.toString();
    }
}
