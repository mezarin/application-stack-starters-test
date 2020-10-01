/*******************************************************************************
Copyright (c) 2020 IBM Corporation and others

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 *******************************************************************************/
package com.samples.jpa.it;

import static io.restassured.RestAssured.*;

import org.junit.jupiter.api.Test;
import org.microshed.testing.jupiter.MicroShedTest;
import org.microshed.testing.testcontainers.ApplicationContainer;
import org.testcontainers.junit.jupiter.Container;

import io.restassured.http.ContentType;

@MicroShedTest
public class EndpointIT {

	@Container
	public static ApplicationContainer app = new ApplicationContainer()
	.withAppContextRoot("/")
	.withReadinessPath("/health/ready");

	/**
	 * Ping readiness health check.  See samples for more detail.
	 */
	@Test 
	public void testReassured() {
		expect()
		.statusCode(200)
		.contentType(ContentType.JSON)
		.when().get("/health/ready");
	}
}
