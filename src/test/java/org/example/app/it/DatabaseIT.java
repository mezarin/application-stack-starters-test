/*
 * Copyright (c) 2019 IBM Corporation and others
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.example.app.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jaxrs.RESTClient;
import org.microshed.testing.jupiter.MicroShedTest;
import org.example.app.*;
import javax.ws.rs.core.Response;

@MicroShedTest
@SharedContainerConfig(AppContainerConfig.class)
public class DatabaseIT {

    @RESTClient
    public static DatabaseResource dbService;

    @Test
    public void testDBGET() {
        Response r = dbService.getDBEntry();
        assertNotNull(r);
    }

    @Test
    public void testDatabasePOST() {
        Response r = dbService.createDBEntry();
        assertNotNull(r);
    }

    @Test
    public void testDatabasePUT() {
        Response r = dbService.updataDBEntry();
        assertNotNull(r);
    }

    @Test
    public void testDatabaseDELETE() {
        Response r = dbService.removeDBEntry();
        assertNotNull(r);
    }
}
