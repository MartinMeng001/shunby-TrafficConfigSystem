package com.traffic.config.cardetector.parser;

import com.traffic.config.cardetector.model.ProtocolMessage;
import com.traffic.config.exception.DataParseException;

public interface DataParser<T> {
    T parse(ProtocolMessage message) throws DataParseException;
    boolean canParse(ProtocolMessage message);
}
