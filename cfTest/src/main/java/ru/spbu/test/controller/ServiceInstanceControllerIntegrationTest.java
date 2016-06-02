package ru.spbu.test.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Asus on 05.05.2016.
 */

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:context.xml"})
//@TransactionConfiguration(transactionManager="transactionManagerModule", defaultRollback=true)
@WebAppConfiguration
public class ServiceInstanceControllerIntegrationTest {
    MockMvc mockMvc;

    protected Class ServiceInstanceController;
    protected Class ServiceInstanceService;
    protected Class CatalogService;
    protected Class ServiceInstance;
    protected Class ServiceInstanceFixture;
    protected Class CreateServiceInstanceRequest;
    protected Class ServiceFixture;
    protected Class CreateServiceInstanceResponse;
    protected Class DeleteServiceInstanceRequest;
    protected Class UpdateServiceInstanceRequest;
    protected Object serviceFixture;
    protected Object serviceInstanceFixture;
    protected Object controller;
    protected Object serviceInstanceService;
    protected Object catalogService;

    public ServiceInstanceControllerIntegrationTest( Object serviceInstanceController, Object serviceInstanceService,
                                                     Object catalogService, Class ServiceInstance, Class ServiceInstanceFixture,
                                                     Class CreateServiceInstanceRequest, Class ServiceFixture,
                                                     Class CreateServiceInstanceResponse, Class DeleteServiceInstanceRequest,
                                                     Class UpdateServiceInstanceRequest){
        this.catalogService=catalogService;
        this.controller = serviceInstanceController;
        this.serviceInstanceService = serviceInstanceService;
        this.ServiceInstanceController=serviceInstanceController.getClass();
        this.ServiceInstanceService=serviceInstanceService.getClass();
        this.CatalogService=catalogService.getClass();
        this.ServiceInstance=ServiceInstance;
        this.ServiceInstanceFixture=ServiceInstanceFixture;
        this.CreateServiceInstanceRequest=CreateServiceInstanceRequest;
        this.ServiceFixture=ServiceFixture;
        this.CreateServiceInstanceResponse=CreateServiceInstanceResponse;
        this.DeleteServiceInstanceRequest=DeleteServiceInstanceRequest;
        this.UpdateServiceInstanceRequest=UpdateServiceInstanceRequest;
        try {
            this.serviceFixture=ServiceFixture.newInstance();
            this.serviceInstanceFixture=ServiceInstanceFixture.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void serviceInstanceIsCreatedCorrectly(String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstance_getServiceInstanceId_method,String ServiceInstanceService_createServiceInstance_method
            ,String CatalogService_getServiceDefinition_method,String ServiceFixture_getService_method
            ,String ServiceInstanceFixture_getCreateServiceInstanceResponse_method
            ,String CreateServiceInstanceResponse_getDashboardUrl_method,String ServiceInstanceController_BASE_PATH_field
            ,String ServiceInstanceFixture_getCreateServiceInstanceRequestJson_method) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        Object instance = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getServiceInstance_method)
                .invoke(serviceInstanceFixture); //ServiceInstance

        Method mgetServiceInstanceId = ServiceInstance.getMethod(ServiceInstance_getServiceInstanceId_method);
        Method mcreateServiceInstance = ServiceInstanceService.getMethod(ServiceInstanceService_createServiceInstance_method
                ,CreateServiceInstanceRequest);
        when(mcreateServiceInstance.invoke(serviceInstanceService, new Object[]{any(CreateServiceInstanceRequest)}))
                .thenReturn(instance);

        Method mgetServiceDefinition = CatalogService.getMethod(CatalogService_getServiceDefinition_method,String.class);
        Method mfgetService = ServiceFixture.getMethod(ServiceFixture_getService_method);
        when(mgetServiceDefinition.invoke(catalogService,new Object[]{any(String.class)}))
                .thenReturn(mfgetService.invoke(serviceFixture));

        Method mfgetCreateServiceInstanceResponse = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getCreateServiceInstanceResponse_method);
        Method mgetDashboardUrl = CreateServiceInstanceResponse.getMethod(CreateServiceInstanceResponse_getDashboardUrl_method);
        String dashboardUrl = (String) mgetDashboardUrl.invoke(mfgetCreateServiceInstanceResponse.invoke(serviceInstanceFixture));

        Field fBASE_PATH = ServiceInstanceController.getField(ServiceInstanceController_BASE_PATH_field);
        String url = (String) fBASE_PATH.get(controller) + "/" + (String)mgetServiceInstanceId.invoke(instance);
        Method mgetCreateServiceInstanceRequestJson = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getCreateServiceInstanceRequestJson_method);
        String body = (String) mgetCreateServiceInstanceRequestJson.invoke(serviceInstanceFixture);

        mockMvc.perform(
                put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.dashboard_url", is(dashboardUrl)));
    }

    @Test
    public void unknownServiceDefinitionInstanceCreationFails(String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstance_getServiceInstanceId_method,String ServiceInstance_getServiceDefinitionId_method
            ,String CatalogService_getServiceDefinition_method,String ServiceInstanceController_BASE_PATH_field
            ,String ServiceInstanceFixture_getCreateServiceInstanceRequestJson_method) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        Object instance = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getServiceInstance_method)
                .invoke(serviceInstanceFixture); //ServiceInstance

        Method mgetServiceDefinition = CatalogService.getMethod(CatalogService_getServiceDefinition_method,String.class);
        when(mgetServiceDefinition.invoke(catalogService,new Object[]{any(String.class)}))
                .thenReturn(null);

        Method mgetServiceInstanceId = ServiceInstance.getMethod(ServiceInstance_getServiceInstanceId_method);
        Field fBASE_PATH = ServiceInstanceController.getField(ServiceInstanceController_BASE_PATH_field);
        String url = (String) fBASE_PATH.get(controller) + "/" + (String)mgetServiceInstanceId.invoke(instance);
        Method mgetCreateServiceInstanceRequestJson = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getCreateServiceInstanceRequestJson_method);
        String body = (String) mgetCreateServiceInstanceRequestJson.invoke(serviceInstanceFixture);

        Method mgetServiceDefinitionId = ServiceInstance.getMethod(ServiceInstance_getServiceDefinitionId_method);
        mockMvc.perform(
                put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.description", containsString((String) mgetServiceDefinitionId.invoke(instance))));
    }

    @Test
    public void duplicateServiceInstanceCreationFails(Exception exceptionDel,String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstance_getServiceInstanceId_method,String ServiceInstanceService_createServiceInstance_method
            ,String CatalogService_getServiceDefinition_method,String ServiceFixture_getService_method
            ,String ServiceInstance_getServiceDefinitionId_method,String ServiceInstanceController_BASE_PATH_field
            ,String ServiceInstanceFixture_getCreateServiceInstanceRequestJson_method) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        Object instance = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getServiceInstance_method)
                .invoke(serviceInstanceFixture); //ServiceInstance

        Method mfgetService = ServiceFixture.getMethod(ServiceFixture_getService_method);
        Method mgetServiceDefinition = CatalogService.getMethod(CatalogService_getServiceDefinition_method,String.class);
        when(mgetServiceDefinition.invoke(catalogService, new Object[]{any(String.class)}))
                .thenReturn(mfgetService.invoke(serviceFixture));

        Method mgetServiceInstanceId = ServiceInstance.getMethod(ServiceInstance_getServiceInstanceId_method);
        Method mgetServiceDefinitionId = ServiceInstance.getMethod(ServiceInstance_getServiceDefinitionId_method);
        Method mcreateServiceInstance = ServiceInstanceService.getMethod(ServiceInstanceService_createServiceInstance_method,CreateServiceInstanceRequest);
        when(mcreateServiceInstance.invoke(serviceInstanceService, new Object[]{any(CreateServiceInstanceRequest)}))
                .thenThrow(exceptionDel);

        Field fBASE_PATH = ServiceInstanceController.getField(ServiceInstanceController_BASE_PATH_field);
        String url = (String) fBASE_PATH.get(controller) + "/" + (String)mgetServiceInstanceId.invoke(instance);
        Method mgetCreateServiceInstanceRequestJson = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getCreateServiceInstanceRequestJson_method);
        String body = (String) mgetCreateServiceInstanceRequestJson.invoke(serviceInstanceFixture);

        mockMvc.perform(
                put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.description", containsString((String) mgetServiceDefinitionId.invoke(instance))));
    }

    @Test
    public void badJsonServiceInstanceCreationFails(String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstance_getServiceInstanceId_method,String ServiceInstanceService_createServiceInstance_method
            ,String CatalogService_getServiceDefinition_method,String ServiceFixture_getService_method
            ,String ServiceInstanceController_BASE_PATH_field
            ,String ServiceInstanceFixture_getCreateServiceInstanceRequestJson_method) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        Object instance = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getServiceInstance_method).invoke(serviceInstanceFixture); //ServiceInstance

        Method mcreateServiceInstance = ServiceInstanceService.getMethod(ServiceInstanceService_createServiceInstance_method,CreateServiceInstanceRequest);
        when(mcreateServiceInstance.invoke(serviceInstanceService, new Object[]{any(CreateServiceInstanceRequest)}))
                .thenReturn(instance);

        Method mfgetService = ServiceFixture.getMethod(ServiceFixture_getService_method);
        Method mgetServiceDefinition = CatalogService.getMethod(CatalogService_getServiceDefinition_method,String.class);
        when(mgetServiceDefinition.invoke(catalogService, new Object[]{any(String.class)}))
                .thenReturn(mfgetService.invoke(serviceFixture));

        Method mgetServiceInstanceId = ServiceInstance.getMethod(ServiceInstance_getServiceInstanceId_method);
        Field fBASE_PATH = ServiceInstanceController.getField(ServiceInstanceController_BASE_PATH_field);
        String url = (String) fBASE_PATH.get(controller) + "/" + (String)mgetServiceInstanceId.invoke(instance);
        Method mgetCreateServiceInstanceRequestJson = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getCreateServiceInstanceRequestJson_method);
        String body = (String) mgetCreateServiceInstanceRequestJson.invoke(serviceInstanceFixture);
        body = body.replace("service_id", "foo");

        mockMvc.perform(
                put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.description", containsString("Missing required fields")));
    }

    @Test
    public void badJsonServiceInstanceCreationFailsMissingFields(String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstance_getServiceInstanceId_method,String ServiceInstanceService_createServiceInstance_method
            ,String CatalogService_getServiceDefinition_method,String ServiceFixture_getService_method
            ,String ServiceInstanceController_BASE_PATH_field) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        Object instance = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getServiceInstance_method).invoke(serviceInstanceFixture); //ServiceInstance

        Method mcreateServiceInstance = ServiceInstanceService.getMethod(ServiceInstanceService_createServiceInstance_method,CreateServiceInstanceRequest);
        when(mcreateServiceInstance.invoke(serviceInstanceService, new Object[]{any(CreateServiceInstanceRequest)}))
                .thenReturn(instance);

        Method mfgetService = ServiceFixture.getMethod(ServiceFixture_getService_method);
        Method mgetServiceDefinition = CatalogService.getMethod(CatalogService_getServiceDefinition_method,String.class);
        when(mgetServiceDefinition.invoke(catalogService, new Object[]{any(String.class)}))
                .thenReturn(mfgetService.invoke(serviceFixture));

        Method mgetServiceInstanceId = ServiceInstance.getMethod(ServiceInstance_getServiceInstanceId_method);
        Field fBASE_PATH = ServiceInstanceController.getField(ServiceInstanceController_BASE_PATH_field);
        String url = (String) fBASE_PATH.get(controller) + "/" + (String)mgetServiceInstanceId.invoke(instance);
        String body = "{}";

        mockMvc.perform(
                put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.description", containsString("serviceDefinitionId")))
                .andExpect(jsonPath("$.description", containsString("planId")))
                .andExpect(jsonPath("$.description", containsString("organizationGuid")))
                .andExpect(jsonPath("$.description", containsString("spaceGuid")));
    }

    @Test
    public void serviceInstanceIsDeletedSuccessfully(String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstance_getServiceInstanceId_method,String ServiceInstanceService_deleteServiceInstance_method
            ,String ServiceInstance_getServiceDefinitionId_method,String ServiceInstance_getPlanId_method
            ,String ServiceInstanceController_BASE_PATH_field) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        Object instance = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getServiceInstance_method).invoke(serviceInstanceFixture); //ServiceInstance

        Method mdeleteServiceInstance = ServiceInstanceService.getMethod(ServiceInstanceService_deleteServiceInstance_method,DeleteServiceInstanceRequest);
        when(mdeleteServiceInstance.invoke(serviceInstanceService, new Object[]{any(DeleteServiceInstanceRequest)}))
                .thenReturn(instance);

        Method mgetServiceDefinitionId = ServiceInstance.getMethod(ServiceInstance_getServiceDefinitionId_method);
        Method mgetPlanId = ServiceInstance.getMethod(ServiceInstance_getPlanId_method);
        Method mgetServiceInstanceId = ServiceInstance.getMethod(ServiceInstance_getServiceInstanceId_method);
        Field fBASE_PATH = ServiceInstanceController.getField(ServiceInstanceController_BASE_PATH_field);
        String url = (String) fBASE_PATH.get(controller) + "/" + (String)mgetServiceInstanceId.invoke(instance)
                + "?service_id=" + (String) mgetServiceDefinitionId.invoke(instance)
                + "&plan_id=" + (String) mgetPlanId.invoke(instance);

        mockMvc.perform(delete(url)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is("{}"))
                );
    }

    @Test
    public void deleteUnknownServiceInstanceFailsWithA410(String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstance_getServiceInstanceId_method,String ServiceInstanceService_deleteServiceInstance_method
            ,String ServiceInstance_getServiceDefinitionId_method,String ServiceInstance_getPlanId_method
            ,String ServiceInstanceController_BASE_PATH_field) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        Object instance = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getServiceInstance_method).invoke(serviceInstanceFixture); //ServiceInstance

        Method mdeleteServiceInstance = ServiceInstanceService.getMethod(ServiceInstanceService_deleteServiceInstance_method,DeleteServiceInstanceRequest);
        when(mdeleteServiceInstance.invoke(serviceInstanceService, new Object[]{any(DeleteServiceInstanceRequest)}))
                .thenReturn(null);

        Method mgetServiceDefinitionId = ServiceInstance.getMethod(ServiceInstance_getServiceDefinitionId_method);
        Method mgetPlanId = ServiceInstance.getMethod(ServiceInstance_getPlanId_method);
        Method mgetServiceInstanceId = ServiceInstance.getMethod(ServiceInstance_getServiceInstanceId_method);
        Field fBASE_PATH = ServiceInstanceController.getField(ServiceInstanceController_BASE_PATH_field);
        String url = (String) fBASE_PATH.get(controller) + "/" + (String)mgetServiceInstanceId.invoke(instance)
                + "?service_id=" + (String) mgetServiceDefinitionId.invoke(instance)
                + "&plan_id=" + (String) mgetPlanId.invoke(instance);

        mockMvc.perform(delete(url)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("{}")));
    }

    @Test
    public void serviceInstanceIsUpdatedSuccessfully(String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstance_getServiceInstanceId_method,String ServiceInstanceService_updateServiceInstance_method
            ,String ServiceInstanceFixture_getUpdateServiceInstanceRequestJson_method
            ,String ServiceInstanceController_BASE_PATH_field) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        Object instance = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getServiceInstance_method).invoke(serviceInstanceFixture); //ServiceInstance

        Method mupdateServiceInstance = ServiceInstanceService.getMethod(ServiceInstanceService_updateServiceInstance_method,UpdateServiceInstanceRequest);
        when(mupdateServiceInstance.invoke(serviceInstanceService, new Object[]{any(UpdateServiceInstanceRequest)}))
                .thenReturn(instance);

        Method mgetServiceInstanceId = ServiceInstance.getMethod(ServiceInstance_getServiceInstanceId_method);
        Field fBASE_PATH = ServiceInstanceController.getField(ServiceInstanceController_BASE_PATH_field);
        String url = (String) fBASE_PATH.get(controller) + "/" + (String)mgetServiceInstanceId.invoke(instance);

        Method mfgetUpdateServiceInstanceRequestJson = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getUpdateServiceInstanceRequestJson_method);
        String body = (String) mfgetUpdateServiceInstanceRequestJson.invoke(serviceInstanceFixture);

        mockMvc.perform(
                patch(url).contentType(MediaType.APPLICATION_JSON).content(body)
                        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is("{}")));
    }

    @Test
    public void updateUnsupportedPlanFailsWithA422(Exception exceptionUp,String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstance_getServiceInstanceId_method,String ServiceInstanceService_updateServiceInstance_method
            ,String ServiceInstanceFixture_getUpdateServiceInstanceRequestJson_method
            ,String ServiceInstance_getServiceDefinitionId_method,String ServiceInstance_getPlanId_method
            ,String ServiceInstanceController_BASE_PATH_field) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        Object instance = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getServiceInstance_method).invoke(serviceInstanceFixture); //ServiceInstance

        Method mupdateServiceInstance = ServiceInstanceService.getMethod(ServiceInstanceService_updateServiceInstance_method,UpdateServiceInstanceRequest);
        when(mupdateServiceInstance.invoke(serviceInstanceService, new Object[]{any(UpdateServiceInstanceRequest)}))
                .thenThrow(exceptionUp);

        Method mgetServiceDefinitionId = ServiceInstance.getMethod(ServiceInstance_getServiceDefinitionId_method);
        Method mgetPlanId = ServiceInstance.getMethod(ServiceInstance_getPlanId_method);
        Method mgetServiceInstanceId = ServiceInstance.getMethod(ServiceInstance_getServiceInstanceId_method);
        Field fBASE_PATH = ServiceInstanceController.getField(ServiceInstanceController_BASE_PATH_field);
        String url = (String) fBASE_PATH.get(controller) + "/" + (String)mgetServiceInstanceId.invoke(instance)
                + "?service_id=" + (String) mgetServiceDefinitionId.invoke(instance)
                + "&plan_id=" + (String) mgetPlanId.invoke(instance);
        Method mfgetUpdateServiceInstanceRequestJson = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getUpdateServiceInstanceRequestJson_method);
        String body = (String) mfgetUpdateServiceInstanceRequestJson.invoke(serviceInstanceFixture);

        mockMvc.perform(
                patch(url).contentType(MediaType.APPLICATION_JSON).content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.description", containsString("description")));
    }

    public void all(Exception exceptionDel,Exception exceptionUp,String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstance_getServiceInstanceId_method,String ServiceInstanceService_createServiceInstance_method
            ,String CatalogService_getServiceDefinition_method,String ServiceFixture_getService_method
            ,String ServiceInstanceFixture_getCreateServiceInstanceResponse_method,String ServiceInstanceService_deleteServiceInstance_method
            ,String ServiceInstance_getServiceDefinitionId_method,String ServiceInstance_getPlanId_method
            ,String CreateServiceInstanceResponse_getDashboardUrl_method,String ServiceInstanceController_BASE_PATH_field
            ,String ServiceInstanceFixture_getCreateServiceInstanceRequestJson_method,String ServiceInstanceService_updateServiceInstance_method
            ,String ServiceInstanceFixture_getUpdateServiceInstanceRequestJson_method) throws Exception{
        this.serviceInstanceIsCreatedCorrectly( ServiceInstanceFixture_getServiceInstance_method
                , ServiceInstance_getServiceInstanceId_method, ServiceInstanceService_createServiceInstance_method
                , CatalogService_getServiceDefinition_method, ServiceFixture_getService_method
                , ServiceInstanceFixture_getCreateServiceInstanceResponse_method
                , CreateServiceInstanceResponse_getDashboardUrl_method, ServiceInstanceController_BASE_PATH_field
                , ServiceInstanceFixture_getCreateServiceInstanceRequestJson_method);
        this.unknownServiceDefinitionInstanceCreationFails( ServiceInstanceFixture_getServiceInstance_method
                , ServiceInstance_getServiceInstanceId_method, ServiceInstance_getServiceDefinitionId_method
                , CatalogService_getServiceDefinition_method, ServiceInstanceController_BASE_PATH_field
                , ServiceInstanceFixture_getCreateServiceInstanceRequestJson_method);
        this.duplicateServiceInstanceCreationFails( exceptionDel,ServiceInstanceFixture_getServiceInstance_method
                , ServiceInstance_getServiceInstanceId_method, ServiceInstanceService_createServiceInstance_method
                , CatalogService_getServiceDefinition_method, ServiceFixture_getService_method
                , ServiceInstance_getServiceDefinitionId_method, ServiceInstanceController_BASE_PATH_field
                , ServiceInstanceFixture_getCreateServiceInstanceRequestJson_method);
        this.badJsonServiceInstanceCreationFails( ServiceInstanceFixture_getServiceInstance_method
                , ServiceInstance_getServiceInstanceId_method, ServiceInstanceService_createServiceInstance_method
                , CatalogService_getServiceDefinition_method, ServiceFixture_getService_method
                , ServiceInstanceController_BASE_PATH_field, ServiceInstanceFixture_getCreateServiceInstanceRequestJson_method);
        this.badJsonServiceInstanceCreationFailsMissingFields( ServiceInstanceFixture_getServiceInstance_method
                , ServiceInstance_getServiceInstanceId_method, ServiceInstanceService_createServiceInstance_method
                , CatalogService_getServiceDefinition_method, ServiceFixture_getService_method
                , ServiceInstanceController_BASE_PATH_field);
        this.serviceInstanceIsDeletedSuccessfully( ServiceInstanceFixture_getServiceInstance_method
                , ServiceInstance_getServiceInstanceId_method, ServiceInstanceService_deleteServiceInstance_method
                , ServiceInstance_getServiceDefinitionId_method, ServiceInstance_getPlanId_method
                , ServiceInstanceController_BASE_PATH_field);
        this.deleteUnknownServiceInstanceFailsWithA410( ServiceInstanceFixture_getServiceInstance_method
                , ServiceInstance_getServiceInstanceId_method, ServiceInstanceService_deleteServiceInstance_method
                , ServiceInstance_getServiceDefinitionId_method, ServiceInstance_getPlanId_method
                , ServiceInstanceController_BASE_PATH_field);
        this.serviceInstanceIsUpdatedSuccessfully( ServiceInstanceFixture_getServiceInstance_method
                , ServiceInstance_getServiceInstanceId_method, ServiceInstanceService_updateServiceInstance_method
                , ServiceInstanceFixture_getUpdateServiceInstanceRequestJson_method
                , ServiceInstanceController_BASE_PATH_field);
        this.updateUnsupportedPlanFailsWithA422( exceptionUp,ServiceInstanceFixture_getServiceInstance_method, ServiceInstance_getServiceInstanceId_method, ServiceInstanceService_updateServiceInstance_method
                , ServiceInstanceFixture_getUpdateServiceInstanceRequestJson_method, ServiceInstance_getServiceDefinitionId_method, ServiceInstance_getPlanId_method
                , ServiceInstanceController_BASE_PATH_field);
    }

}
