package com.airlenet.yang.netconf;

import com.airlenet.play.integration.ApplicationInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tailf.jnc.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Created by airshiplay on 2017/9/2.
 */

@Component
@Order(0)
public class NetconfApplicationInitializer extends ApplicationInitializer {

 @Autowired
    ObjectMapper objectMapper;

    @Override
    public void onRootContextRefreshed() {
        super.onRootContextRefreshed();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Element.class,new ElementSerializer());
        objectMapper.registerModule(simpleModule);

    }
}