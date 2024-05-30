package br.com.ccs.uuidvslong.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("com.ccs.uuidvslong")
@Getter
@Setter
public class ConfigBean {
    private int qtd_produtos;
    private int batch_size;
}
