package integration.config.common;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;


@Configuration
public class RestConfig {


    public static final String REST_USER = "restadmin";
    public static final String REST_PASSWORD = "restadmin";

    @Bean
    @Inject
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        return restTemplate;
    }



    @Bean
    @Inject
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }

    @Bean
    public HttpClient httpClient(){
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000).setSocketTimeout(10000).build();
        return HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
//        DefaultHttpClient httpClient = new DefaultHttpClient();
//        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(REST_USER,REST_PASSWORD);
//        BasicCredentialsProvider bcp = new BasicCredentialsProvider();
//        bcp.setCredentials(AuthScope.ANY, credentials);
//        httpClient.setCredentialsProvider(bcp);
//        return httpClient;
    }

}
