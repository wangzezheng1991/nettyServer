/*
 * Copyright (c) 2015 The Jupiter Project
 *
 * Licensed under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ut.netty.server.product.common.message;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 发布订阅信息的包装类.
 * <p>
 * jupiter
 * org.jupiter.registry
 *
 * @author jiachun.fjc
 */
public class Message {

    public static final AtomicLong sequenceGenerator = new AtomicLong(0);

    private long sequence;
    private int sign;
    private Object data;

    public Message() {
    }

    public Message(int sign) {
        this.sign = sign;
    }

    public Message(int sign, Object data) {
        this.sign = sign;
        this.data = data;
    }

    public Message(long sequence, int sign, Object data) {
        this.sequence = sequence;
        this.sign = sign;
        this.data = data;
    }

    public long sequence() {
        return sequence;
    }

    public int sign() {
        return sign;
    }

    public void sign(int sign) {
        this.sign = sign;
    }

    public Object data() {
        return data;
    }

    public void data(Object data) {
        this.data = data;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sequence=" + sequence +
                ", sign=" + sign +
                ", data=" + data +
                '}';
    }
}
