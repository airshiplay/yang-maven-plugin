package com.airlenet.yang.netconf;

import com.airlenet.play.repo.domain.Result;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.tailf.jnc.Element;

import java.io.IOException;

/**
 * Created by airshiplay on 2017/9/2.
 */
public class ElementSerializer extends JsonSerializer<Element>  {

    @Override
    public void serialize(Element element, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        if(element==null){
            jsonGenerator.writeNull();
        }else {
            jsonGenerator.writeRaw(element.toJSONString());
//            jsonGenerator.writeStartObject();
//            jsonGenerator.writeString(element.toXMLString());
//            jsonGenerator.writeEndObject();
        }
    }
}
