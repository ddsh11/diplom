package ru.spbu.test.interceptor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Asus on 06.05.2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:context.xml"})
@WebAppConfiguration
public class BrokerApiVersionInterceptorIntegrationTest {

    MockMvc mockMvc;

    protected Object brokerApiVersionInterceptor;
    protected Object catalogService;
    protected Object controller;
    protected Class CatalogController;
    protected Class CatalogService;

    public BrokerApiVersionInterceptorIntegrationTest(Object brokerApiVersionInterceptor, Object catalogService,Object controller){
        this.brokerApiVersionInterceptor=brokerApiVersionInterceptor;
        this.catalogService=catalogService;
        this.controller=controller;
        this.CatalogController=controller.getClass();
        this.CatalogService=catalogService.getClass();
    }

    @Test
    public void noHeaderSent(String CatalogController_BASE_PATH_field) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors((org.springframework.web.servlet.HandlerInterceptor) brokerApiVersionInterceptor) //new BrokerApiVersionInterceptor(new BrokerApiVersion("header","version"))
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();
        Field field = CatalogController.getField(CatalogController_BASE_PATH_field);
        this.mockMvc.perform(get((String)field.get(controller))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.description.", containsString("Expected Version")));
    }

    @Test
    public void incorrectHeaderSent(String CatalogController_BASE_PATH_field) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors((org.springframework.web.servlet.HandlerInterceptor) brokerApiVersionInterceptor) //new BrokerApiVersionInterceptor(new BrokerApiVersion("header","version"))
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();
        Field field = CatalogController.getField(CatalogController_BASE_PATH_field);
        this.mockMvc.perform(get((String)field.get(controller))
                .header("header", "wrong-version")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.description", containsString("Expected Version")));
    }

    @Test
    public void correctHeaderSent(String CatalogController_BASE_PATH_field) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors((org.springframework.web.servlet.HandlerInterceptor) brokerApiVersionInterceptor) //new BrokerApiVersionInterceptor(new BrokerApiVersion("header","version"))
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();
        Field field = CatalogController.getField(CatalogController_BASE_PATH_field);
        this.mockMvc.perform(get((String)field.get(controller))
                .header("header", "version")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    public void all(String CatalogController_BASE_PATH_field) throws Exception {
        this.noHeaderSent(CatalogController_BASE_PATH_field);
        this.incorrectHeaderSent(CatalogController_BASE_PATH_field);
        this.correctHeaderSent(CatalogController_BASE_PATH_field);
    }

}
