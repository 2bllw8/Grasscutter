package emu.grasscutter.info;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;

interface InfoRequestHandler {

    boolean canHandle(HttpMethod method, String path);

    InfoRequestResponseContents handle(HttpMethod method, String path, HttpHeaders headers);
}
