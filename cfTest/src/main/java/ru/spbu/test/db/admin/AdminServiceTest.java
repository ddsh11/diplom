package ru.spbu.test.db.admin;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by Asus on 06.05.2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:context.xml"})
@WebAppConfiguration
public class AdminServiceTest {

    protected Object brokerApiVersion;
    protected Class BrokerApiVersion;

    public AdminServiceTest(Object brokerApiVersion){
        this.brokerApiVersion=brokerApiVersion;
        this.BrokerApiVersion=brokerApiVersion.getClass();
    }

    @Test
    public void BrokerApiVersionIsCorrectly(String bversion)throws Exception{
        Assert.assertEquals("vgsabas", bversion,  BrokerApiVersion.getMethod("getApiVersion").invoke(brokerApiVersion));
    }

}
