package com.shuking.rpccore.server.tcp;

import com.shuking.rpccore.constant.ProtocolConstants;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;


/**
 * 使用装饰器模式对bufferHandler进行增强
 */
public class TcpBufferHandlerWrapper implements Handler<Buffer> {

    private final RecordParser recordParser;

    /**
     * 构造函数
     *
     * @param bufferHandler handler
     */
    public TcpBufferHandlerWrapper(Handler<Buffer> bufferHandler) {
        recordParser = initRecordParser(bufferHandler);
    }

    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }

    private RecordParser initRecordParser(Handler<Buffer> bufferHandler) {
        // 消息头parser
        RecordParser parser = RecordParser.newFixed(ProtocolConstants.MESSAGE_HEADER_LENGTH);

        parser.setOutput(new Handler<>() {
            int size = -1;
            Buffer resultBuffer = Buffer.buffer();

            @Override
            public void handle(Buffer buffer) {
                if (size == -1) {
                    // 读取消息体长度
                    size = buffer.getInt(13);
                    // 将长度设置为消息体长度
                    parser.fixedSizeMode(size);
                    // 将头信息写入到结果
                    resultBuffer.appendBuffer(buffer);
                }
                // 接收到的是消息体
                else {
                    resultBuffer.appendBuffer(buffer);
                    // 已接收到完整buffer 执行处理
                    bufferHandler.handle(resultBuffer);

                    // 重置
                    parser.fixedSizeMode(ProtocolConstants.MESSAGE_HEADER_LENGTH);
                    size = -1;
                    resultBuffer = Buffer.buffer();
                }
            }
        });

        return parser;
    }
}
