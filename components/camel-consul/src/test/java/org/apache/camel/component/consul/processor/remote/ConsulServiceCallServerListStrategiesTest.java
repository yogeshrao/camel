/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.component.consul.processor.remote;

import java.util.ArrayList;
import java.util.List;

import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import org.apache.camel.component.consul.ConsulConfiguration;
import org.apache.camel.spi.ServiceCallServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Ignore("A consul server is needed for this test ")
public class ConsulServiceCallServerListStrategiesTest {
    private AgentClient client;
    private List<Registration> registrations;

    @Before
    public void setUp() throws Exception {
        client = Consul.builder().build().agentClient();
        registrations = new ArrayList<>(3);

        for (int i = 0; i < 3; i++) {
            Registration r = ImmutableRegistration.builder()
                .id("service-" + i)
                .name("my-service")
                .address("127.0.0.1")
                .addTags("a-tag")
                .addTags("key1=value1")
                .addTags("key2=value2")
                .port(9000 + i)
                .build();

            client.register(r);
            registrations.add(r);
        }
    }

    @After
    public void tearDown() throws Exception {
        registrations.forEach(r -> client.deregister(r.getId()));
    }

    // *************************************************************************
    // Test
    // *************************************************************************

    @Test
    public void testOnDemand() throws Exception {
        ConsulConfiguration configuration = new ConsulConfiguration(null);
        ConsulServiceCallServerListStrategy strategy = ConsulServiceCallServerListStrategies.onDemand(configuration);

        List<ServiceCallServer> servers = strategy.getUpdatedListOfServers("my-service");
        assertNotNull(servers);
        assertEquals(3, servers.size());

        for (ServiceCallServer server : servers) {
            assertFalse(server.getMetadata().isEmpty());
            assertTrue(server.getMetadata().containsKey("service_name"));
            assertTrue(server.getMetadata().containsKey("service_id"));
            assertTrue(server.getMetadata().containsKey("a-tag"));
            assertTrue(server.getMetadata().containsKey("key1"));
            assertTrue(server.getMetadata().containsKey("key2"));
        }
    }
}
