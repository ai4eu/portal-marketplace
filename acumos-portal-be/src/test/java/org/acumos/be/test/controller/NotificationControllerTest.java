/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 * ===================================================================================
 * This Acumos software file is distributed by AT&T and Tech Mahindra
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END=========================================================
 */
package org.acumos.be.test.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.domain.MLPNotifUserMap;
import org.acumos.cds.domain.MLPNotification;
import org.acumos.cds.domain.MLPUserNotification;
import org.acumos.cds.transport.RestPageRequest;
import org.acumos.portal.be.common.JsonRequest;
import org.acumos.portal.be.common.JsonResponse;
import org.acumos.portal.be.controller.NotificationController;
import org.acumos.portal.be.service.NotificationService;
import org.acumos.portal.be.service.impl.NotificationServiceImpl;
import org.acumos.portal.be.transport.MLNotification;
import org.acumos.portal.be.util.EELFLoggerDelegate;
import org.acumos.portal.be.util.PortalUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.mockito.Mockito.*;

public class NotificationControllerTest {

	private static final EELFLoggerDelegate logger = EELFLoggerDelegate.getLogger(NotificationControllerTest.class);

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();
	@InjectMocks
	NotificationController notificationController;
	@Mock
	NotificationServiceImpl notificationService;
	@Mock
	NotificationService notificationServiceImpl;
	
	final HttpServletResponse response = new MockHttpServletResponse();
	final HttpServletRequest request = new MockHttpServletRequest();

	@Test
	public void createNotificationTest(){

		MLPNotification mlpNotification = new MLPNotification();
		JsonResponse<MLNotification> data = new JsonResponse<>();
		Date created = new Date();
		mlpNotification.setCreated(created);
		mlpNotification.setMessage("notification created");
		Date modified = new Date();
		mlpNotification.setModified(modified);
		mlpNotification.setNotificationId("037ad773-3ae2-472b-89d3-9e185a2cbfc9");
		mlpNotification.setTitle("Notification");
		mlpNotification.setUrl("http://notify.com");
		JsonRequest<MLPNotification> notificationReq = new JsonRequest<>();
		notificationReq.setBody(mlpNotification);
		MLNotification value = PortalUtils.convertToMLNotification(mlpNotification);
		when(notificationService.createNotification(mlpNotification)).thenReturn(value);
		data = notificationController.createNotification(request, notificationReq, response);
		data.setResponseBody(value);
		if(data != null){
			logger.debug(EELFLoggerDelegate.debugLogger, "Notification created Successfully :  "+data.getResponseBody());
		}else {
			logger.error(EELFLoggerDelegate.errorLogger, "Error Occurred createNotification :");
		}
	}
	
	@Test
	public void getNotifications(){
		MLNotification mlNotification = new MLNotification();
		mlNotification.setNotificationId("037ad773-3ae2-472b-89d3-9e185a2cbfc9");
		mlNotification.setCount(1);
		mlNotification.setMessage("notification");
		mlNotification.setTitle("Notification");
		mlNotification.setUrl("http://notify.com");
		Assert.assertNotNull(mlNotification);
		List<MLNotification> mlNotificationList = new ArrayList<MLNotification>();
		mlNotificationList.add(mlNotification);
		Assert.assertNotNull(mlNotificationList);
		JsonResponse<List<MLNotification>> notificationres = new JsonResponse<>();
		notificationres.setResponseBody(mlNotificationList);
		when(notificationService.getNotifications()).thenReturn(mlNotificationList);
		notificationres = notificationController.getNotifications();
		if(notificationres != null){
			logger.debug(EELFLoggerDelegate.debugLogger, "getNotifications: size is {} ", mlNotificationList.size());
		}else {
			logger.error(EELFLoggerDelegate.errorLogger, "Error Occurred while fetching Notification :");
		}
	}
	
	@Test
	public void getUserNotifications(){
		MLPUserNotification mlpUserNotification = new MLPUserNotification();
		Date created = new Date();
		mlpUserNotification.setCreated(created);
		mlpUserNotification.setMessage("notification created");
		Date modified = new Date();
		mlpUserNotification.setModified(modified);
		mlpUserNotification.setNotificationId("037ad773-3ae2-472b-89d3-9e185a2cbfc9");
		mlpUserNotification.setTitle("Notification");
		mlpUserNotification.setUrl("http://notify.com");
		Date viewed = new Date();
		mlpUserNotification.setViewed(viewed);
		Assert.assertNotNull(mlpUserNotification);
		MLPNotifUserMap mlpNotificationUserMap = new MLPNotifUserMap();
		mlpNotificationUserMap.setNotificationId(mlpUserNotification.getNotificationId());
		mlpNotificationUserMap.setUserId("41058105-67f4-4461-a192-f4cb7fdafd34");
		Assert.assertNotNull(mlpNotificationUserMap);
		List<MLPUserNotification> mlpUserNotificationList = new ArrayList<>();
		mlpUserNotificationList.add(mlpUserNotification);
		Assert.assertNotNull(mlpUserNotificationList);
		String userId = mlpNotificationUserMap.getUserId();
		JsonRequest<RestPageRequest> restPageReq = new JsonRequest<>();
		RestPageRequest restPageRequest = new RestPageRequest();
		restPageRequest.setPage(0);
		restPageRequest.setSize(9);
		JsonResponse<List<MLPUserNotification>> notifires = new JsonResponse<>();
		if (restPageRequest.getPage() != null || restPageRequest.getSize() != null) {
			notifires.setResponseBody(mlpUserNotificationList);
		}
		
		when(notificationService.getUserNotifications(userId, restPageRequest)).thenReturn(mlpUserNotificationList);
		notifires = notificationController.getUserNotifications(request, userId, restPageReq, response);
		if(notifires != null){
			logger.debug(EELFLoggerDelegate.debugLogger, "No notifications exist for user : "+userId);
		}else {
			logger.error(EELFLoggerDelegate.errorLogger, "Exception Occurred while getUserNotifications");
		}
	}
	
	@Test
	public void addNotificationUserTest() {
		try {

			MLNotification mlNotification = new MLNotification();
			mlNotification.setNotificationId("037ad773-3ae2-472b-89d3-9e185a2cbfc9");
			mlNotification.setCount(1);
			mlNotification.setMessage("notification");
			mlNotification.setTitle("Notification");
			mlNotification.setUrl("http://notify.com");
			Assert.assertNotNull(mlNotification);
			List<MLNotification> mlNotificationList = new ArrayList<MLNotification>();
			mlNotificationList.add(mlNotification);
			Assert.assertNotNull(mlNotificationList);
			JsonResponse<List<MLNotification>> notificationres = new JsonResponse<>();
			notificationres.setResponseBody(mlNotificationList);

			MLPNotifUserMap mlpNotificationUserMap = new MLPNotifUserMap();
			mlpNotificationUserMap.setNotificationId(mlNotification.getNotificationId());
			mlpNotificationUserMap.setUserId("41058105-67f4-4461-a192-f4cb7fdafd34");
			Assert.assertNotNull(mlpNotificationUserMap);
			String userId = mlpNotificationUserMap.getUserId();
			String notificationId = mlpNotificationUserMap.getNotificationId();
			Assert.assertNotNull(userId);
			Assert.assertNotNull(notificationId);
			NotificationServiceImpl mockImpl = mock(NotificationServiceImpl.class);
			mockImpl.addNotificationUser(notificationId, userId);
			notificationController.addNotificationUser(request, notificationId, userId, response);
			logger.info("Successfully  added notifiaction for particular user : " + notificationres.getResponseBody());
			Assert.assertNotNull(notificationres);
		} catch (Exception e) {
			logger.info("failed tot execute the above test case");
		}
	}
	@Test
	public void dropNotificationUserTest() {
		try {

			MLNotification mlNotification = new MLNotification();
			mlNotification.setNotificationId("037ad773-3ae2-472b-89d3-9e185a2cbfc9");
			mlNotification.setCount(1);
			mlNotification.setMessage("notification");
			mlNotification.setTitle("Notification");
			mlNotification.setUrl("http://notify.com");
			Assert.assertNotNull(mlNotification);
			List<MLNotification> mlNotificationList = new ArrayList<MLNotification>();
			mlNotificationList.add(mlNotification);
			Assert.assertNotNull(mlNotificationList);
			JsonResponse<List<MLNotification>> notificationres = new JsonResponse<>();
			notificationres.setResponseBody(mlNotificationList);

			MLPNotifUserMap mlpNotificationUserMap = new MLPNotifUserMap();
			mlpNotificationUserMap.setNotificationId(mlNotification.getNotificationId());
			mlpNotificationUserMap.setUserId("41058105-67f4-4461-a192-f4cb7fdafd34");
			Assert.assertNotNull(mlpNotificationUserMap);
			String userId = mlpNotificationUserMap.getUserId();
			String notificationId = mlpNotificationUserMap.getNotificationId();
			Assert.assertNotNull(userId);
			Assert.assertNotNull(notificationId);
			NotificationServiceImpl mockImpl = mock(NotificationServiceImpl.class);
			mockImpl.dropNotificationUser(notificationId, userId);
			notificationController.dropNotificationUser(request, notificationId, userId, response);
			logger.info("Successfully  droped notifiaction for particular user : " + notificationres.getResponseBody());
			Assert.assertNotNull(notificationres);
		} catch (Exception e) {
			logger.info("failed tot execute the above test case");
		}
	}

	@Test
	public void setNotificationUserViewedTest() {

		try {

			MLNotification mlNotification = new MLNotification();
			mlNotification.setNotificationId("037ad773-3ae2-472b-89d3-9e185a2cbfc9");
			mlNotification.setCount(1);
			mlNotification.setMessage("notification");
			mlNotification.setTitle("Notification");
			mlNotification.setUrl("http://notify.com");
			Assert.assertNotNull(mlNotification);
			List<MLNotification> mlNotificationList = new ArrayList<MLNotification>();
			mlNotificationList.add(mlNotification);
			Assert.assertNotNull(mlNotificationList);
			JsonResponse<List<MLNotification>> notificationres = new JsonResponse<>();
			notificationres.setResponseBody(mlNotificationList);

			MLPNotifUserMap mlpNotificationUserMap = new MLPNotifUserMap();
			mlpNotificationUserMap.setNotificationId(mlNotification.getNotificationId());
			mlpNotificationUserMap.setUserId("41058105-67f4-4461-a192-f4cb7fdafd34");
			Assert.assertNotNull(mlpNotificationUserMap);
			String userId = mlpNotificationUserMap.getUserId();
			String notificationId = mlpNotificationUserMap.getNotificationId();
			Assert.assertNotNull(userId);
			Assert.assertNotNull(notificationId);
			NotificationServiceImpl mockImpl = mock(NotificationServiceImpl.class);
			mockImpl.setNotificationUserViewed(notificationId, userId);
			notificationController.setNotificationUserViewed(request, notificationId, userId, response);
			logger.info("Successfully  setNotificationUserViewed: " + notificationres.getResponseBody());
			Assert.assertNotNull(notificationres);
		} catch (Exception e) {
			logger.info("failed tot execute the above test case");
		}

	}

	@Test
	public void deleteNotificationTest() {

		try {

			MLNotification mlNotification = new MLNotification();
			mlNotification.setNotificationId("037ad773-3ae2-472b-89d3-9e185a2cbfc9");
			mlNotification.setCount(1);
			mlNotification.setMessage("notification");
			mlNotification.setTitle("Notification");
			mlNotification.setUrl("http://notify.com");
			Assert.assertNotNull(mlNotification);
			List<MLNotification> mlNotificationList = new ArrayList<MLNotification>();
			mlNotificationList.add(mlNotification);
			Assert.assertNotNull(mlNotificationList);
			JsonResponse<List<MLNotification>> notificationres = new JsonResponse<>();
			notificationres.setResponseBody(mlNotificationList);
			
			MLPNotifUserMap mlpNotificationUserMap = new MLPNotifUserMap();
			mlpNotificationUserMap.setNotificationId(mlNotification.getNotificationId());
			mlpNotificationUserMap.setUserId("41058105-67f4-4461-a192-f4cb7fdafd34");
			Assert.assertNotNull(mlpNotificationUserMap);
			String userId = mlpNotificationUserMap.getUserId();
			Assert.assertNotNull(userId);
			String notificationId = mlpNotificationUserMap.getNotificationId();
			Assert.assertNotNull(notificationId);
			NotificationServiceImpl mockImpl = mock(NotificationServiceImpl.class);
			mockImpl.dropNotificationUser(notificationId, userId);
			notificationController.deleteNotification(request, notificationId, response);
			logger.info("Successfully  setNotificationUserViewed: " + notificationres.getResponseBody());
			Assert.assertNotNull(notificationres);
		} catch (Exception e) {
			logger.info("failed tot execute the above test case");
		}

	}

	@Test
	public void getNotificationCountTest(){
		try {

			MLNotification mlNotification = new MLNotification();
			mlNotification.setNotificationId("037ad773-3ae2-472b-89d3-9e185a2cbfc9");
			mlNotification.setCount(1);
			mlNotification.setMessage("notification");
			mlNotification.setTitle("Notification");
			mlNotification.setUrl("http://notify.com");
			Assert.assertNotNull(mlNotification);
			JsonResponse<MLNotification> notificationres = new JsonResponse<>();
			notificationres.setResponseBody(mlNotification);
			when(notificationServiceImpl.getNotificationCount());
			notificationres = notificationController.getNotificationCount();
			logger.info("Successfully  setNotificationUserViewed: " + notificationres.getResponseBody());
			Assert.assertNotNull(notificationres);
		} catch (Exception e) {
			logger.info("failed tot execute the above test case");
		}

	
	}
}
