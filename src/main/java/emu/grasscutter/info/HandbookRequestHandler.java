package emu.grasscutter.info;

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
import emu.grasscutter.tools.Tools;
import emu.grasscutter.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

final class HandbookRequestHandler implements InfoRequestHandler {
    private static final Pattern PATH_PATTERN = Pattern.compile("/handbook(/.*)*");

    @Override
    public boolean canHandle(HttpMethod method, String path) {
        if (!HttpMethod.GET.equals(method)) {
            return false;
        }
        return PATH_PATTERN.matcher(path).matches();
    }

    @Override
    public InfoRequestResponseContents handle(HttpMethod method, String path, HttpHeaders headers) {
        try {
            final String htmlPage = buildHtmlGmHandbook(Tools.getLanguageOption());
            final ByteBuf content = Unpooled.copiedBuffer(htmlPage, CharsetUtil.UTF_8);
            return new InfoRequestResponseContents(content, "text/html",
                    HttpResponseStatus.OK, content.readableBytes());
        } catch (Exception e) {
            final String error = "Failed to build gm handbook";
            Grasscutter.getLogger().error(error, e);
            return new InfoRequestResponseContents(Unpooled.copiedBuffer(error, CharsetUtil.UTF_8),
                    "text/plain", HttpResponseStatus.INTERNAL_SERVER_ERROR, error.length());
        }
    }
    
    @SuppressWarnings("deprecation")
    private String buildHtmlGmHandbook(String language) throws Exception {
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
        final StringBuilder sb = new StringBuilder();

        final String title = "Grasscutter " + GameConstants.VERSION + " GM Handbook";
        
        sb.append("<html lang=\"").append(language).append("\">\n")
                .append("<header>\n")
                .append("<title>").append(title).append("</title>\n")
                .append("<style>")
                .append("table {border-collapse: collapse; width: 70%; margin: 0 auto;}\n")
                .append("table thead tr {height: 60px; background: black;}\n")
                .append("table thead tr th {font-size: 18px; color: white;}\n")
                .append("table tbody tr {height: 50px; background-color: #f5f5f5;}\n")
                .append("tbody tr:nth-child(even) {background-color: #fdfdfd;}\n")
                .append("table th, table td {text-align: left; padding: 0px 8px;}\n")
                .append("</style>")
                .append("</header>\n")
                .append("<body>");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        sb.append("<h1>").append(title).append("</h1>");
        sb.append("<p>").append("Created ").append(dtf.format(now)).append("</p>");

        CommandMap cmdMap = new CommandMap(true);
        List<Command> cmdList = new ArrayList<>(cmdMap.getAnnotationsAsList());

        sb.append("<h2>Commands</h2>");
        sb.append("<table><thead><tr><th>Command</th><th>Description</th></tr></thead>");
        for (Command cmd : cmdList) {
            String cmdName = cmd.label();
            sb.append("<tr><td><code>").append(cmdName).append("</code></td><td>")
                    .append(cmd.description()).append("</td></tr>");
        }
        sb.append("</table>");

        list = new ArrayList<>(GameData.getAvatarDataMap().keySet());
        Collections.sort(list);
        
        sb.append("<h2>Avatars</h2>");
        sb.append("<table><thead><tr><th>Id</th><th>Avatar</th></tr></thead>");
        for (Integer id : list) {
            AvatarData data = GameData.getAvatarDataMap().get(id);
            sb.append("<tr><td><code>").append(data.getId()).append("</code></td><td>")
                    .append(map.get(data.getNameTextMapHash())).append("</td></tr>");
        }
        sb.append("</table>");


        list = new ArrayList<>(GameData.getItemDataMap().keySet());
        Collections.sort(list);

        sb.append("<h2>Items</h2>");
        sb.append("<table><thead><tr><th>Id</th><th>Item</th></tr></thead>");
        for (Integer id : list) {
            ItemData data = GameData.getItemDataMap().get(id);
            sb.append("<tr><td><code>").append(data.getId()).append("</code></td><td>")
                    .append(map.get(data.getNameTextMapHash())).append("</td></tr>");
        }
        sb.append("</table>");
        
        list = new ArrayList<>(GameData.getSceneDataMap().keySet());
        Collections.sort(list);

        sb.append("<h2>Scenes</h2>");
        sb.append("<table><thead><tr><th>Id</th><th>Scene</th></tr></thead>");
        for (Integer id : list) {
            SceneData data = GameData.getSceneDataMap().get(id);
            sb.append("<tr><td><code>").append(data.getId()).append("</code></td><td>")
                    .append(data.getScriptData()).append("</td></tr>");
        }
        sb.append("</table>");
        
        list = new ArrayList<>(GameData.getMonsterDataMap().keySet());
        Collections.sort(list);

        sb.append("<h2>Monsters</h2>");
        sb.append("<table><thead><tr><th>Id</th><th>Monster</th></tr></thead>");
        for (Integer id : list) {
            MonsterData data = GameData.getMonsterDataMap().get(id);
            sb.append("<tr><td><code>").append(data.getId()).append("</code></td><td>")
                    .append(map.get(data.getNameTextMapHash())).append("</td></tr>");
        }
        sb.append("</table>");
        sb.append("</body></html>");
        return sb.toString();
    }
}
