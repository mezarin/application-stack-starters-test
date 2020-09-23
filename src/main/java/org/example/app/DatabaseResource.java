/*
 * Copyright (c) 2020 IBM Corporation and others
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
package org.example.app;

import javax.annotation.PostConstruct;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import javax.enterprise.context.RequestScoped;
import javax.inject.*;

@Path("/")
@RequestScoped
@Named
public class DatabaseResource {

    @Inject
    private DatabaseDAO dbDAO;
    
    @PostConstruct
    public void init() {
    }

    @GET
    public Response getDBEntry(){
        dbDAO.read(null);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    public Response createDBEntry(){
        dbDAO.create(null);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @PUT
    public Response updataDBEntry() {
       dbDAO.update(null);
       return Response.status(Response.Status.NO_CONTENT).build();
    }   

    @DELETE
    public Response removeDBEntry() {
        dbDAO.delete(null);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}