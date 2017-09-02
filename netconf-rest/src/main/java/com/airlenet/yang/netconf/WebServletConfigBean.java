package com.airlenet.yang.netconf;

/**
 * Created by airshiplay on 2017/9/2.
 */
import com.airlenet.play.integration.webapp.FullBeanNameGenerator;
import com.airlenet.play.web.ServletSupport;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@Configuration
@ComponentScan(basePackages = { "com.airlenet.yang" }, useDefaultFilters = false, includeFilters = { @Filter({ Controller.class, RestController.class}),
        @Filter({ ServletSupport.class }) }, nameGenerator = FullBeanNameGenerator.class)
@EnableWebMvc
public class WebServletConfigBean {

}