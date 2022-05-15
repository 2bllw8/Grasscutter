package emu.grasscutter.info;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.tools.Tools;
import emu.grasscutter.tools.ToolsWithLanguageOption;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import java.util.regex.Pattern;

final class GachaMappingRequestHandler implements InfoRequestHandler {
    private static final Pattern PATH_PATTERN = Pattern.compile("/gacha(/.*)*");

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
            final String mapping = ToolsWithLanguageOption.createGachaMappingJson(Tools.getLanguageOption());
            final ByteBuf content = Unpooled.copiedBuffer(mapping, CharsetUtil.UTF_8);
            return new InfoRequestResponseContents(content, "application/json",
                    HttpResponseStatus.OK, content.readableBytes());
        } catch (Exception e) {
            final String error = "Failed to build gacha mapping";
            Grasscutter.getLogger().error(error, e);
            return new InfoRequestResponseContents(Unpooled.copiedBuffer(error, CharsetUtil.UTF_8),
                    "text/plain", HttpResponseStatus.INTERNAL_SERVER_ERROR, error.length());
        }
    }
}
