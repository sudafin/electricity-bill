package com.electricitybill.config.swagger;

import cn.hutool.core.convert.ConverterRegistry;
import com.electricitybill.entity.R;
import com.electricitybill.utils.TjTemporalConverter;
import com.fasterxml.classmate.TypeResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 核心配置类
 */
@Configuration
@ConditionalOnProperty(prefix = "eb.swagger", name = "enable",havingValue = "true")
@EnableConfigurationProperties(SwaggerConfigProperties.class)
public class Knife4jConfiguration {

    @Resource
    private SwaggerConfigProperties swaggerConfigProperties;

    //swagger最核心的配置
    @Bean(value = "defaultApi2")
    public Docket defaultApi2(TypeResolver typeResolver) {
        // 1.初始化Docket
        Docket docket = new Docket(DocumentationType.SWAGGER_2);
        // 2.是否需要包装R
        if(swaggerConfigProperties.getEnableResponseWrap()){
            docket.additionalModels(typeResolver.resolve(R.class));
        }
        return docket.apiInfo(new ApiInfoBuilder()
                .title(this.swaggerConfigProperties.getTitle())
                .description(this.swaggerConfigProperties.getDescription())
                .contact(new Contact(
                        this.swaggerConfigProperties.getContactName(),
                        this.swaggerConfigProperties.getContactUrl(),
                        this.swaggerConfigProperties.getContactEmail()))
                .version(this.swaggerConfigProperties.getVersion())
                .build())
                .select()
                //这里指定Controller扫描包路径
                .apis(RequestHandlerSelectors.basePackage(swaggerConfigProperties.getPackagePath()))
                .paths(PathSelectors.any())
                .build();

    }
    //以下都是处理响应的
    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "tj.swagger", name = "enableResponseWrap",havingValue = "true")
    public BaseSwaggerResponseModelPlugin baseSwaggerResponseModelPlugin(){
        return new BaseSwaggerResponseModelPlugin();
    }
    @Bean
    @Primary
    @ConditionalOnProperty(prefix = "tj.swagger", name = "enableResponseWrap",havingValue = "true")
    public BaseSwaggerResponseBuilderPlugin baseSwaggerResponseBuilderPlugin(){
        return new BaseSwaggerResponseBuilderPlugin();
    }
    {
        // hutool的日期转换器加载
        ConverterRegistry converterRegistry = ConverterRegistry.getInstance();
        converterRegistry.putCustom(LocalDateTime.class, new TjTemporalConverter(LocalDateTime.class));
    }
}