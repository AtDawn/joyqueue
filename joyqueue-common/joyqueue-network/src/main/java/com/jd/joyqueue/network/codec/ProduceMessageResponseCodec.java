/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.joyqueue.network.codec;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.joyqueue.exception.JoyQueueCode;
import com.jd.joyqueue.network.command.JoyQueueCommandType;
import com.jd.joyqueue.network.command.ProduceMessageAckData;
import com.jd.joyqueue.network.command.ProduceMessageAckItemData;
import com.jd.joyqueue.network.command.ProduceMessageResponse;
import com.jd.joyqueue.network.serializer.Serializer;
import com.jd.joyqueue.network.transport.codec.JoyQueueHeader;
import com.jd.joyqueue.network.transport.codec.PayloadCodec;
import com.jd.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * ProduceMessageResponseCodec
 * author: gaohaoxiang
 * email: gaohaoxiang@jd.com
 * date: 2018/12/19
 */
public class ProduceMessageResponseCodec implements PayloadCodec<JoyQueueHeader, ProduceMessageResponse>, Type {

    @Override
    public ProduceMessageResponse decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        short dataSize = buffer.readShort();
        Map<String, ProduceMessageAckData> data = Maps.newHashMap();
        for (int i = 0; i < dataSize; i++) {
            String topic = Serializer.readString(buffer, Serializer.SHORT_SIZE);
            JoyQueueCode code = JoyQueueCode.valueOf(buffer.readInt());
            short itemSize = buffer.readShort();
            List<ProduceMessageAckItemData> item = Lists.newArrayListWithCapacity(itemSize);

            for (int j = 0; j < itemSize; j++) {
                short partition = buffer.readShort();
                long index = buffer.readLong();
                long startTime = buffer.readLong();
                item.add(new ProduceMessageAckItemData(partition, index, startTime));
            }
            data.put(topic, new ProduceMessageAckData(item, code));
        }

        ProduceMessageResponse produceMessageResponse = new ProduceMessageResponse();
        produceMessageResponse.setData(data);
        return produceMessageResponse;
    }

    @Override
    public void encode(ProduceMessageResponse payload, ByteBuf buffer) throws Exception {
        buffer.writeShort(payload.getData().size());
        for (Map.Entry<String, ProduceMessageAckData> entry : payload.getData().entrySet()) {
            Serializer.write(entry.getKey(), buffer, Serializer.SHORT_SIZE);
            buffer.writeInt(entry.getValue().getCode().getCode());
            buffer.writeShort(entry.getValue().getItem().size());
            for (ProduceMessageAckItemData data : entry.getValue().getItem()) {
                buffer.writeShort(data.getPartition());
                buffer.writeLong(data.getIndex());
                buffer.writeLong(data.getStartTime());
            }
        }
    }

    @Override
    public int type() {
        return JoyQueueCommandType.PRODUCE_MESSAGE_RESPONSE.getCode();
    }
}