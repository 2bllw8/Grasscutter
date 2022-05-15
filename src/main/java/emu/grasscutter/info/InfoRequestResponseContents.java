package emu.grasscutter.info;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;

public record InfoRequestResponseContents(ByteBuf content,
                                          String contentType,
                                          HttpResponseStatus status,
                                          int length) {
}
