package com.example.web3j;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.concurrent.TimeUnit;

@Configuration
public class AppConfig {

    @Value("${wallet.pvt.key}")
    private String privateKey;

    @Value("${ganache.url}")
    private String blockChainUrl;


    @Value("${timeout.value}")
    private String timeout;

    @Bean
    public Credentials getWalletCredentials() throws Exception {
        Credentials credentials = Credentials.create(privateKey);
        return credentials;
    }


    @Bean
    public Web3j getWeb3JInstance() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Long.valueOf(timeout), TimeUnit.SECONDS)
                .writeTimeout(Long.valueOf(timeout), TimeUnit.SECONDS)
                .readTimeout(Long.valueOf(timeout), TimeUnit.SECONDS)
                .build();
        return Web3j.build(new HttpService(blockChainUrl, client));
    }

}
