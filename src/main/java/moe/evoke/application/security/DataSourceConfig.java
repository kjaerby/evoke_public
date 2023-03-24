package moe.evoke.application.security;

import moe.evoke.application.backend.Config;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource getDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url("jdbc:mysql://" + Config.getSQLServer() + ":" + Config.getSQLPort() + "/" + Config.getSQLDatabse() + "?serverTimezone=Europe/Berlin");
        dataSourceBuilder.username(Config.getSQLUser());
        dataSourceBuilder.password(Config.getSQLPassword());
        return dataSourceBuilder.build();
    }
}