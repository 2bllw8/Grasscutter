package emu.grasscutter.info;

import emu.grasscutter.Grasscutter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class InfoServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final InfoRequestHandler[] handlers = {
            new HandbookRequestHandler(),
            new GachaMappingRequestHandler(),
    };

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        final InfoRequestResponseContents responseContents = handle(msg);
        final FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                responseContents.status(), responseContents.content());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, responseContents.contentType())
                .set(HttpHeaderNames.CONTENT_LENGTH, responseContents.length());
        ctx.write(response);
        ctx.flush();
    }

    private InfoRequestResponseContents handle(FullHttpRequest msg) {
        Grasscutter.getLogger().debug("Incoming request: " + msg.toString());
        for (final InfoRequestHandler handler : handlers) {
            if (handler.canHandle(msg.method(), msg.uri())) {
                return handler.handle(msg.method(), msg.uri(), msg.headers());
            }
        }
        return new InfoRequestResponseContents(Unpooled.buffer(0, 0),
                "text/plain",
                HttpResponseStatus.NOT_FOUND,
                0);
    }
}
