package com.airlenet.yang.netconf;

import com.airlenet.play.web.ServletSupport;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Created by airshiplay on 2017/9/2.
 */
@Configuration
@ComponentScan(basePackages = { " com.airlenet.yang" }, excludeFilters = { @ComponentScan.Filter(value = Controller.class, type = FilterType.ANNOTATION),
        @ComponentScan.Filter(value = RestController.class,type = FilterType.ANNOTATION),
        @ComponentScan.Filter(value = EnableWebMvc.class, type = FilterType.ANNOTATION),
        @ComponentScan.Filter(value = ServletSupport.class, type = FilterType.ANNOTATION) })
public class WebRootConfigBean {
}
