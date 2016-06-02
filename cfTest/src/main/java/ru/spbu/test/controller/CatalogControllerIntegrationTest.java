package ru.spbu.test.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Asus on 12.04.2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:context.xml"})
@WebAppConfiguration
public class CatalogControllerIntegrationTest {

    MockMvc mockMvc;

    protected Class CatalogService;
    protected Class CatalogController;
    protected Class CatalogFixture;
    protected Object controller;
    protected Object catalogService;
    protected Object catalogFixture;

    public CatalogControllerIntegrationTest(Object controller,Object catalogService,Class CatalogFixture){
        try {
            this.controller=controller;
            this.catalogService=catalogService;
            this.CatalogFixture=CatalogFixture;
            this.catalogFixture = CatalogFixture.newInstance();
            this.CatalogController=controller.getClass();
            this.CatalogService=catalogService.getClass();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void catalogIsRetrievedCorrectly(String CatalogService_getCatalog_method
            ,String CatalogController_BASE_PATH_field) throws Exception {

        Method mGetCatalog = CatalogService.getMethod(CatalogService_getCatalog_method);

        Field field = CatalogController.getField(CatalogController_BASE_PATH_field);

        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        when(mGetCatalog.invoke(catalogService)).thenReturn(mGetCatalog.invoke(catalogFixture));
        this.mockMvc.perform(get((String)field.get(controller))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

}
