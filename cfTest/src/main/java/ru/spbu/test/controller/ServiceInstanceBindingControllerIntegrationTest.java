package ru.spbu.test.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Asus on 12.04.2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:context.xml"})
@WebAppConfiguration
public class ServiceInstanceBindingControllerIntegrationTest {

    MockMvc mockMvc;

    protected Class ServiceInstanceBindingController;
    protected Class ServiceInstanceBindingService;
    protected Class ServiceInstanceService;
    protected Class CreateServiceInstanceBindingRequest;
    protected Class DeleteServiceInstanceBindingRequest;
    protected Class ServiceInstanceFixture;
    protected Class ServiceInstanceBindingFixture;
    protected Class ServiceInstanceBinding;
    protected Class ServiceInstance;
    protected Object controller; //ServiceInstanceBindingController
    protected Object serviceInstanceBindingService;
    protected Object serviceInstanceService;
    protected Object serviceInstanceFixture;
    protected Object serviceInstanceBindingFixture;
    //protected Object serviceInstanceBinding;
    //protected Object serviceInstance;

    public ServiceInstanceBindingControllerIntegrationTest( Object serviceInstanceBindingController, Object serviceInstanceBindingService,
                                                            Object serviceInstanceService, Class CreateServiceInstanceBindingRequest,
                                                            Class ServiceInstanceFixture, Class ServiceInstanceBindingFixture,
                                                            Class ServiceInstanceBinding, Class ServiceInstance,
                                                            Class DeleteServiceInstanceBindingRequest) throws Exception {
        this.controller=serviceInstanceBindingController;
        this.serviceInstanceBindingService=serviceInstanceBindingService;
        this.serviceInstanceService=serviceInstanceService;
        this.CreateServiceInstanceBindingRequest=CreateServiceInstanceBindingRequest;
        this.ServiceInstanceFixture=ServiceInstanceFixture;
        this.ServiceInstanceBindingFixture=ServiceInstanceBindingFixture;
        this.ServiceInstanceBinding=ServiceInstanceBinding;
        this.ServiceInstance=ServiceInstance;
        this.ServiceInstanceBindingController=serviceInstanceBindingController.getClass();
        this.ServiceInstanceBindingService=serviceInstanceBindingService.getClass();
        this.ServiceInstanceService=serviceInstanceService.getClass();
        this.serviceInstanceFixture=ServiceInstanceFixture.newInstance();
        this.serviceInstanceBindingFixture=ServiceInstanceBindingFixture.newInstance();
        //this.serviceInstanceBinding=ServiceInstanceBinding.newInstance();
        //this.serviceInstance=ServiceInstance.newInstance();
        this.DeleteServiceInstanceBindingRequest = DeleteServiceInstanceBindingRequest;
    }

    @Test
    public void serviceInstanceBindingIsCreatedCorrectly(String ServiceInstanceBindingService_createServiceInstanceBinding_method
            ,String ServiceInstanceService_getServiceInstance_method,String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstanceBindingFixture_getServiceInstanceBinding_method, String ServiceInstance_getServiceInstanceId_method
            ,String ServiceInstanceBindingFixture_getServiceInstanceBindingRequestJson_method
            ,String ServiceInstanceBinding_getId_method) throws Exception {

        Method mcreateServiceInstanceBinding = ServiceInstanceBindingService.getMethod(ServiceInstanceBindingService_createServiceInstanceBinding_method
                ,new Class[] {CreateServiceInstanceBindingRequest});
        Method mgetServiceInstance = ServiceInstanceService.getMethod(ServiceInstanceService_getServiceInstance_method
                ,new Class[] { String.class });

        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        Object instance = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getServiceInstance_method).invoke(serviceInstanceFixture);
        Object binding = ServiceInstanceBindingFixture.getMethod(ServiceInstanceBindingFixture_getServiceInstanceBinding_method)
                .invoke(serviceInstanceBindingFixture);

        when(mgetServiceInstance.invoke(serviceInstanceService,new Object[] { any(String.class)}))
                .thenReturn(instance);

        when(mcreateServiceInstanceBinding.invoke(serviceInstanceBindingService,new Object[] {any(CreateServiceInstanceBindingRequest)}))
                .thenReturn(binding);

        Method mgetServiceInstanceId = ServiceInstance.getMethod(ServiceInstance_getServiceInstanceId_method);
        String BASE_PATH = "/v2/service_instances/"
                + (String) mgetServiceInstanceId.invoke(instance)
                + "/service_bindings";
        String url = BASE_PATH + "/{bindingId}";
        String body = (String) ServiceInstanceBindingFixture.getMethod(ServiceInstanceBindingFixture_getServiceInstanceBindingRequestJson_method)
                .invoke(serviceInstanceBindingFixture);

        mockMvc.perform(
                put(url, (String) ServiceInstanceBinding.getMethod(ServiceInstanceBinding_getId_method).invoke(binding))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.credentials.uri", is("uri")))
                .andExpect(jsonPath("$.credentials.username", is("username")))
                .andExpect(jsonPath("$.credentials.password", is("password")));
    }

    @Test
    public void unknownServiceInstanceFailsBinding(String ServiceInstanceBinding_getServiceInstanceId_method
            ,String ServiceInstanceService_getServiceInstance_method,String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstanceBindingFixture_getServiceInstanceBinding_method, String ServiceInstance_getServiceInstanceId_method
            ,String ServiceInstanceBindingFixture_getServiceInstanceBindingRequestJson_method
            ,String ServiceInstanceBinding_getId_method) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        Method mfgetServiceInstanceBinding = ServiceInstanceBindingFixture.getMethod(ServiceInstanceBindingFixture_getServiceInstanceBinding_method);
        Object binding = mfgetServiceInstanceBinding.invoke(serviceInstanceBindingFixture);  //ServiceInstanceBinding

        Method mgetServiceInstance = ServiceInstanceService.getMethod(ServiceInstanceService_getServiceInstance_method,String.class);
        when(mgetServiceInstance.invoke(serviceInstanceService, new Object[]{any(String.class)}))
                .thenReturn(null);

        Method mgetServiceInstanceBindingRequestJson = ServiceInstanceBindingFixture.getMethod(ServiceInstanceBindingFixture_getServiceInstanceBindingRequestJson_method);
        Object instance = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getServiceInstance_method).invoke(serviceInstanceFixture);
        Method msgetServiceInstanceId = ServiceInstance.getMethod(ServiceInstance_getServiceInstanceId_method);
        String BASE_PATH = "/v2/service_instances/"
                + (String) msgetServiceInstanceId.invoke(instance)
                + "/service_bindings";
        String url = BASE_PATH + "/{bindingId}";
        String body =(String) mgetServiceInstanceBindingRequestJson.invoke(serviceInstanceBindingFixture);

        Method mgetId = ServiceInstanceBinding.getMethod(ServiceInstanceBinding_getId_method);
        Method mgetServiceInstanceId = ServiceInstanceBinding.getMethod(ServiceInstanceBinding_getServiceInstanceId_method);
        mockMvc.perform(
                put(url, (String) mgetId.invoke(binding))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
                .andDo(print())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.description", containsString((String) mgetServiceInstanceId.invoke(binding))));
    }

    @Test
    public void duplicateBindingRequestFailsBinding(Exception exception,String ServiceInstanceBinding_getServiceInstanceId_method
            ,String ServiceInstanceService_getServiceInstance_method,String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstanceBindingFixture_getServiceInstanceBinding_method, String ServiceInstance_getServiceInstanceId_method
            ,String ServiceInstanceBindingFixture_getServiceInstanceBindingRequestJson_method,String ServiceInstanceBindingService_createServiceInstanceBinding_method
            ,String ServiceInstanceBinding_getId_method) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        Method mfgetServiceInstance = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getServiceInstance_method);
        Object instance = mfgetServiceInstance.invoke(serviceInstanceFixture);  //ServiceInstance
        Method mfgetServiceInstanceBinding = ServiceInstanceBindingFixture.getMethod(ServiceInstanceBindingFixture_getServiceInstanceBinding_method);
        Object binding = mfgetServiceInstanceBinding.invoke(serviceInstanceBindingFixture); //ServiceInstanceBinding

        Method mgetServiceInstance = ServiceInstanceService.getMethod(ServiceInstanceService_getServiceInstance_method,String.class);
        when(mgetServiceInstance.invoke(serviceInstanceService, new Object[]{any(String.class)}))
                .thenReturn(instance);

        Method mgetId = ServiceInstanceBinding.getMethod(ServiceInstanceBinding_getId_method);
        Method mgetServiceInstanceId = ServiceInstanceBinding.getMethod(ServiceInstanceBinding_getServiceInstanceId_method);
        Method mcreateServiceInstanceBinding =
                ServiceInstanceBindingService.getMethod(ServiceInstanceBindingService_createServiceInstanceBinding_method,CreateServiceInstanceBindingRequest);
        when(mcreateServiceInstanceBinding.invoke(serviceInstanceBindingService, new Object[]{any(CreateServiceInstanceBindingRequest)}))
                .thenThrow(exception); //new ServiceInstanceBindingExistsException(binding)

        Method mgetServiceInstanceBindingRequestJson = ServiceInstanceBindingFixture.getMethod(ServiceInstanceBindingFixture_getServiceInstanceBindingRequestJson_method);
        String body =(String) mgetServiceInstanceBindingRequestJson.invoke(serviceInstanceBindingFixture);
        Method msgetServiceInstanceId = ServiceInstance.getMethod(ServiceInstance_getServiceInstanceId_method);
        String BASE_PATH = "/v2/service_instances/"
                + (String) msgetServiceInstanceId.invoke(instance)
                + "/service_bindings";
        String url = BASE_PATH + "/{bindingId}";

        mockMvc.perform(
                put(url, (String) mgetId.invoke(binding))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.description", containsString((String) mgetId.invoke(binding))));
    }

    @Test
    public void invalidBindingRequestJson(String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstanceBindingFixture_getServiceInstanceBinding_method, String ServiceInstance_getServiceInstanceId_method
            ,String ServiceInstanceBindingFixture_getServiceInstanceBindingRequestJson_method
            ,String ServiceInstanceBinding_getId_method) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        Method mfgetServiceInstanceBinding = ServiceInstanceBindingFixture.getMethod(ServiceInstanceBindingFixture_getServiceInstanceBinding_method);
        Object binding = mfgetServiceInstanceBinding.invoke(serviceInstanceBindingFixture); //ServiceInstanceBinding
        Method mfgetServiceInstance = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getServiceInstance_method);
        Object instance = mfgetServiceInstance.invoke(serviceInstanceFixture);  //ServiceInstance

        Method msgetServiceInstanceId = ServiceInstance.getMethod(ServiceInstance_getServiceInstanceId_method);
        String BASE_PATH = "/v2/service_instances/"
                + (String) msgetServiceInstanceId.invoke(instance)
                + "/service_bindings";
        String url = BASE_PATH + "/{bindingId}";
        Method mgetServiceInstanceBindingRequestJson = ServiceInstanceBindingFixture.getMethod(ServiceInstanceBindingFixture_getServiceInstanceBindingRequestJson_method);
        String body =(String) mgetServiceInstanceBindingRequestJson.invoke(serviceInstanceBindingFixture);
        body = body.replace("service_id", "foo");
        Method mgetId = ServiceInstanceBinding.getMethod(ServiceInstanceBinding_getId_method);
        mockMvc.perform(
                put(url, (String) mgetId.invoke(binding))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.description", containsString("Missing required fields")));
    }

    @Test
    public void invalidBindingRequestMissingFields(String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstanceBindingFixture_getServiceInstanceBinding_method, String ServiceInstance_getServiceInstanceId_method
            ,String ServiceInstanceBinding_getId_method) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        Method mfgetServiceInstanceBinding = ServiceInstanceBindingFixture.getMethod(ServiceInstanceBindingFixture_getServiceInstanceBinding_method);
        Object binding = mfgetServiceInstanceBinding.invoke(serviceInstanceBindingFixture); //ServiceInstanceBinding
        Method mfgetServiceInstance = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getServiceInstance_method);
        Object instance = mfgetServiceInstance.invoke(serviceInstanceFixture);  //ServiceInstance

        Method msgetServiceInstanceId = ServiceInstance.getMethod(ServiceInstance_getServiceInstanceId_method);
        String BASE_PATH = "/v2/service_instances/"
                + (String) msgetServiceInstanceId.invoke(instance)
                + "/service_bindings";
        String url = BASE_PATH + "/{bindingId}";
        String body = "{}";

        Method mgetId = ServiceInstanceBinding.getMethod(ServiceInstanceBinding_getId_method);
        mockMvc.perform(
                put(url, (String) mgetId.invoke(binding))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.description", containsString("serviceDefinitionId")))
                .andExpect(jsonPath("$.description", containsString("planId")));
    }

    @Test
    public void serviceInstanceBindingIsDeletedSuccessfully(String ServiceInstance_getServiceDefinitionId_method
            ,String ServiceInstanceService_getServiceInstance_method,String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstanceBindingFixture_getServiceInstanceBinding_method, String ServiceInstance_getServiceInstanceId_method
            ,String ServiceInstance_getPlanId_method,String ServiceInstanceBindingService_deleteServiceInstanceBinding_method
            ,String ServiceInstanceBinding_getId_method) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        Method mfgetServiceInstance = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getServiceInstance_method);
        Object instance = mfgetServiceInstance.invoke(serviceInstanceFixture);  //ServiceInstance
        Method mfgetServiceInstanceBinding = ServiceInstanceBindingFixture.getMethod(ServiceInstanceBindingFixture_getServiceInstanceBinding_method);
        Object binding = mfgetServiceInstanceBinding.invoke(serviceInstanceBindingFixture); //ServiceInstanceBinding

        Method mgetServiceInstance = ServiceInstanceService.getMethod(ServiceInstanceService_getServiceInstance_method,String.class);
        when(mgetServiceInstance.invoke(serviceInstanceService, new Object[]{any(String.class)}))
                .thenReturn(instance);

        Method mdeleteServiceInstanceBinding =ServiceInstanceBindingService.getMethod(ServiceInstanceBindingService_deleteServiceInstanceBinding_method,DeleteServiceInstanceBindingRequest);
        when(mdeleteServiceInstanceBinding.invoke(serviceInstanceBindingService,new Object[] {any(DeleteServiceInstanceBindingRequest)}))
                .thenReturn(binding);

        Method mgetId = ServiceInstanceBinding.getMethod(ServiceInstanceBinding_getId_method);
        Method mgetServiceDefinitionId = ServiceInstance.getMethod(ServiceInstance_getServiceDefinitionId_method);
        Method mgetPlanId = ServiceInstance.getMethod(ServiceInstance_getPlanId_method);
        Method msgetServiceInstanceId = ServiceInstance.getMethod(ServiceInstance_getServiceInstanceId_method);
        String BASE_PATH = "/v2/service_instances/"
                + (String) msgetServiceInstanceId.invoke(instance)
                + "/service_bindings";
        String url = BASE_PATH + "/" + (String) mgetId.invoke(binding)
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
    public void unknownServiceInstanceBindingNotDeletedAndA410IsReturned(String ServiceInstance_getServiceDefinitionId_method
            ,String ServiceInstanceService_getServiceInstance_method,String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstanceBindingFixture_getServiceInstanceBinding_method, String ServiceInstance_getServiceInstanceId_method
            ,String ServiceInstance_getPlanId_method,String ServiceInstanceBindingService_deleteServiceInstanceBinding_method
            ,String ServiceInstanceBinding_getId_method) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        Method mfgetServiceInstance = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getServiceInstance_method);
        Object instance = mfgetServiceInstance.invoke(serviceInstanceFixture);  //ServiceInstance
        Method mfgetServiceInstanceBinding = ServiceInstanceBindingFixture.getMethod(ServiceInstanceBindingFixture_getServiceInstanceBinding_method);
        Object binding = mfgetServiceInstanceBinding.invoke(serviceInstanceBindingFixture); //ServiceInstanceBinding

        Method mgetServiceInstance = ServiceInstanceService.getMethod(ServiceInstanceService_getServiceInstance_method,String.class);
        when(mgetServiceInstance.invoke(serviceInstanceService, new Object[]{any(String.class)}))
                .thenReturn(instance);
        Method mdeleteServiceInstanceBinding =ServiceInstanceBindingService.getMethod(ServiceInstanceBindingService_deleteServiceInstanceBinding_method,DeleteServiceInstanceBindingRequest);
        when(mdeleteServiceInstanceBinding.invoke(serviceInstanceBindingService,new Object[] {any(DeleteServiceInstanceBindingRequest)}))
                .thenReturn(null);

        Method mgetId = ServiceInstanceBinding.getMethod(ServiceInstanceBinding_getId_method);
        Method mgetServiceDefinitionId = ServiceInstance.getMethod(ServiceInstance_getServiceDefinitionId_method);
        Method mgetPlanId = ServiceInstance.getMethod(ServiceInstance_getPlanId_method);
        Method msgetServiceInstanceId = ServiceInstance.getMethod(ServiceInstance_getServiceInstanceId_method);
        String BASE_PATH = "/v2/service_instances/"
                + (String) msgetServiceInstanceId.invoke(instance)
                + "/service_bindings";
        String url = BASE_PATH + "/" + (String) mgetId.invoke(binding)
                + "?service_id=" + (String) mgetServiceDefinitionId.invoke(instance)
                + "&plan_id=" + (String) mgetPlanId.invoke(instance);

        mockMvc.perform(delete(url)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isGone())
                .andExpect(jsonPath("$", is("{}")));
    }

    @Test
    public void whenAnUnknownServiceInstanceIsProvidedOnABindingDeleteAnHttp422IsReturned(String ServiceInstance_getServiceDefinitionId_method
            ,String ServiceInstanceService_getServiceInstance_method,String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstanceBindingFixture_getServiceInstanceBinding_method, String ServiceInstance_getServiceInstanceId_method
            ,String ServiceInstance_getPlanId_method,String ServiceInstanceBinding_getId_method) throws Exception {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()).build();

        Method mfgetServiceInstance = ServiceInstanceFixture.getMethod(ServiceInstanceFixture_getServiceInstance_method);
        Object instance = mfgetServiceInstance.invoke(serviceInstanceFixture);  //ServiceInstance
        Method mfgetServiceInstanceBinding = ServiceInstanceBindingFixture.getMethod(ServiceInstanceBindingFixture_getServiceInstanceBinding_method);
        Object binding = mfgetServiceInstanceBinding.invoke(serviceInstanceBindingFixture); //ServiceInstanceBinding

        Method mgetServiceInstance = ServiceInstanceService.getMethod(ServiceInstanceService_getServiceInstance_method,String.class);
        when(mgetServiceInstance.invoke(serviceInstanceService, new Object[]{any(String.class)}))
                .thenReturn(null);

        Method mgetId = ServiceInstanceBinding.getMethod(ServiceInstanceBinding_getId_method);
        Method mgetServiceDefinitionId = ServiceInstance.getMethod(ServiceInstance_getServiceDefinitionId_method);
        Method mgetPlanId = ServiceInstance.getMethod(ServiceInstance_getPlanId_method);
        Method msgetServiceInstanceId = ServiceInstance.getMethod(ServiceInstance_getServiceInstanceId_method);
        String BASE_PATH = "/v2/service_instances/"
                + (String) msgetServiceInstanceId.invoke(instance)
                + "/service_bindings";
        String url = BASE_PATH + "/" + (String) mgetId.invoke(binding)
                + "?service_id=" + (String) mgetServiceDefinitionId.invoke(instance)
                + "&plan_id=" + (String) mgetPlanId.invoke(instance);

        mockMvc.perform(delete(url)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isGone());
    }

    public void all(Exception exception,String ServiceInstance_getServiceDefinitionId_method,String ServiceInstanceBinding_getServiceInstanceId_method
            ,String ServiceInstanceService_getServiceInstance_method,String ServiceInstanceFixture_getServiceInstance_method
            ,String ServiceInstanceBindingFixture_getServiceInstanceBinding_method, String ServiceInstance_getServiceInstanceId_method
            ,String ServiceInstanceBindingFixture_getServiceInstanceBindingRequestJson_method,String ServiceInstanceBindingService_createServiceInstanceBinding_method
            ,String ServiceInstance_getPlanId_method,String ServiceInstanceBindingService_deleteServiceInstanceBinding_method
            ,String ServiceInstanceBinding_getId_method) throws Exception {
        this.serviceInstanceBindingIsCreatedCorrectly( ServiceInstanceBindingService_createServiceInstanceBinding_method
                , ServiceInstanceService_getServiceInstance_method, ServiceInstanceFixture_getServiceInstance_method
                , ServiceInstanceBindingFixture_getServiceInstanceBinding_method,  ServiceInstance_getServiceInstanceId_method
                , ServiceInstanceBindingFixture_getServiceInstanceBindingRequestJson_method
                , ServiceInstanceBinding_getId_method);
        this.unknownServiceInstanceFailsBinding( ServiceInstanceBinding_getServiceInstanceId_method
                , ServiceInstanceService_getServiceInstance_method, ServiceInstanceFixture_getServiceInstance_method
                , ServiceInstanceBindingFixture_getServiceInstanceBinding_method,  ServiceInstance_getServiceInstanceId_method
                , ServiceInstanceBindingFixture_getServiceInstanceBindingRequestJson_method
                , ServiceInstanceBinding_getId_method);
        this.duplicateBindingRequestFailsBinding(exception, ServiceInstanceBinding_getServiceInstanceId_method
                , ServiceInstanceService_getServiceInstance_method, ServiceInstanceFixture_getServiceInstance_method
                , ServiceInstanceBindingFixture_getServiceInstanceBinding_method,  ServiceInstance_getServiceInstanceId_method
                , ServiceInstanceBindingFixture_getServiceInstanceBindingRequestJson_method, ServiceInstanceBindingService_createServiceInstanceBinding_method
                , ServiceInstanceBinding_getId_method);
        this.invalidBindingRequestJson( ServiceInstanceFixture_getServiceInstance_method
                , ServiceInstanceBindingFixture_getServiceInstanceBinding_method,  ServiceInstance_getServiceInstanceId_method
                , ServiceInstanceBindingFixture_getServiceInstanceBindingRequestJson_method
                , ServiceInstanceBinding_getId_method);
        this.invalidBindingRequestMissingFields( ServiceInstanceFixture_getServiceInstance_method
                , ServiceInstanceBindingFixture_getServiceInstanceBinding_method,  ServiceInstance_getServiceInstanceId_method
                , ServiceInstanceBinding_getId_method);
        this.serviceInstanceBindingIsDeletedSuccessfully( ServiceInstance_getServiceDefinitionId_method
                , ServiceInstanceService_getServiceInstance_method, ServiceInstanceFixture_getServiceInstance_method
                , ServiceInstanceBindingFixture_getServiceInstanceBinding_method,  ServiceInstance_getServiceInstanceId_method
                , ServiceInstance_getPlanId_method, ServiceInstanceBindingService_deleteServiceInstanceBinding_method
                , ServiceInstanceBinding_getId_method);
        this.unknownServiceInstanceBindingNotDeletedAndA410IsReturned( ServiceInstance_getServiceDefinitionId_method
                , ServiceInstanceService_getServiceInstance_method, ServiceInstanceFixture_getServiceInstance_method
                , ServiceInstanceBindingFixture_getServiceInstanceBinding_method,  ServiceInstance_getServiceInstanceId_method
                , ServiceInstance_getPlanId_method, ServiceInstanceBindingService_deleteServiceInstanceBinding_method
                , ServiceInstanceBinding_getId_method);
        this.whenAnUnknownServiceInstanceIsProvidedOnABindingDeleteAnHttp422IsReturned( ServiceInstance_getServiceDefinitionId_method
                , ServiceInstanceService_getServiceInstance_method, ServiceInstanceFixture_getServiceInstance_method
                , ServiceInstanceBindingFixture_getServiceInstanceBinding_method,  ServiceInstance_getServiceInstanceId_method
                , ServiceInstance_getPlanId_method, ServiceInstanceBinding_getId_method);
    }

}
