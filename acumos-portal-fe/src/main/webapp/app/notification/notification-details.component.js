/*
===============LICENSE_START=======================================================
Acumos Apache-2.0
===================================================================================
Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
===================================================================================
This Acumos software file is distributed by AT&T and Tech Mahindra
under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
This file is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
===============LICENSE_END=========================================================
*/

'use strict';

app.component('notificationModule',{
	templateUrl : '/app/notification/notification-details.template.html',
	controller : function($scope, $state,$anchorScroll, $timeout, $location, $rootScope, $window, $http, $mdDialog, 
			$sce, apiService, browserStorageService, $filter, DTOptionsBuilder, DTColumnDefBuilder, DTColumnBuilder) {
		$scope.loginUserID='';
		$scope.totalCount = 0;
		$scope.page = 0;
		//$rootScope.notificationCount= 0;
		$scope.notificationManageObj=[];
		$scope.selectAll = false;
		
		$scope.orderByField = 'start';
		$scope.reverseSort = true;

		if (JSON.parse(browserStorageService.getUserDetail())) {
			$scope.userDetails = JSON.parse(browserStorageService
					.getUserDetail());
			$scope.userDetails.userName = $scope.userDetails[0];
			$scope.loginUserID = $scope.userDetails[1];
		}

		if (browserStorageService.getUserDetail()) {
			$scope.auth = browserStorageService
					.getAuthToken();
		}
		
		$scope.dtOptions = DTOptionsBuilder.newOptions()
        .withPaginationType('simple_numbers')
        .withDisplayLength(10)
        .withLanguage({"sLengthMenu": "Show _MENU_ Notifications",
        	"sInfo": "Showing _START_ to _END_ of _TOTAL_ Notifications",
			"sInfoEmpty": "Showing 0 to 0 of 0 Notifications"})
        $scope.dtColumnDefs = [
        	 DTColumnDefBuilder.newColumnDef(0).notVisible(),
        	 DTColumnDefBuilder.newColumnDef(1).notSortable(),
        	 DTColumnDefBuilder.newColumnDef(2)
        ];
		
		$scope.dtInstance = {};
		$scope.reloadData = reloadData;
		
		 function reloadData() {
			 $scope.dtInstance.reloadData(null, true);
	     };  
		
		$scope.totalNotificationMessages=function (userId){
			
			$scope.SetDataLoaded = true;
			$rootScope.setLoader = true;
			
			var req = {
		    	  "request_body": {
						"fieldToDirectionMap": {'start': "DESC"},
			    	    "page": 0,
			    	    "size": 1
			    	 } 
			    	};
				$scope.Loadcheck = true;
				apiService.getNotificationPagination(userId,req)
				.then(
						function successCallback(response) {
							$scope.totalCatalogElements = response.data.response_body.totalElements;
							$scope.getNotificationMessage($scope.loginUserID, 0, $scope.totalCatalogElements);
						}, function errorCallback(response) {
							$scope.Loadcheck = false;
							$scope.SetDataLoaded = false;
							$rootScope.setLoader = false;
						});
		}


		$scope.getNotificationMessage=function (userId,page,size){
			
			var req = {
		    	  "request_body": {
						"fieldToDirectionMap": {'start': "DESC"},
			    	    "page": page,
			    	    "size": size
			    	 } 
			    	};
				$scope.Loadcheck = true;
				apiService.getNotificationPagination(userId,req).then(function(response) {
					$scope.SetDataLoaded = false;
					$rootScope.setLoader = false;
					var totalElements = response.data.response_body.totalElements;
					$scope.totalPages = response.data.response_body.totalPages;
					// var totalPages = (totalElements / page ) + 1;
					$scope.loadpage = $scope.selectedPage;
					$scope.startPageSize = $scope.loadpage *  $scope.defaultSize + 1;
					$scope.endPageSize = (($scope.loadpage + 1) * $scope.defaultSize) < $scope.totalElements ? (($scope.loadpage + 1) * $scope.defaultSize)
							: $scope.totalElements;
					$scope.SetDataLoaded = false;
					$rootScope.setLoader = false;
					if ($scope.notificationManageObj
							&& $scope.notificationManageObj.length !== 0)
						$scope.notificationManageObj = [];
					
				if(response.data != null && response.data.response_body.totalElements > 0 ) {
					angular.forEach(response.data.response_body.content,function( value, key) {
						$scope.notificationManageObj
						.push({
							message : $sce.trustAsHtml($filter('link')(value.message)),
							start : value.start,
							startdateForSorting : $filter('date')(value.start, "MM/dd/yyyy"),
							viewed : value.viewed,
							notificationId : value.notificationId
						});
					});
					/*$scope.totalCount = response.data.response_body.length;
					if($scope.totalCount == 20){
						$scope.page = $scope.page + 1;
						$scope.getNotificationMessage($scope.loginUserID,$scope.page);
					} */
				} else {
					
					/*$rootScope.notificationCount=0;
					$scope.notificationManageObj=[];*/
				}
				$scope.Loadcheck = false;
			});	
		}
		
		// Change pagination Size starts
		$scope.defaultSize = 10;
		//$scope.getNotificationMessage($scope.loginUserID,$scope.page, $scope.defaultSize);
		$scope.totalNotificationMessages($scope.loginUserID);

		$scope.filterChange = function(checkbox, type) {
			$rootScope.setLoader = true;
			$scope.pageNumber = 0;
			$scope.setPageStart = 0;
			$scope.selectedPage = 0;
			if (type == 'paginationSize') {
				$scope.defaultSize = checkbox;
				//$scope.getNotificationMessage($scope.loginUserID, $scope.selectedPage, $scope.defaultSize);
				$scope.totalNotificationMessages($scope.loginUserID)
			}
		}
		$scope.setPageStart = 0;
		$scope.selectedPage = 0;
		
		$scope.setStartCount = function(val) {
			$location.hash('notification-details');
			$anchorScroll();
			if (val == "preBunch") {
				$scope.setPageStart = $scope.setPageStart - 5
			}
			if (val == "nextBunch") {
				$scope.setPageStart = $scope.setPageStart + 5
			}
			if (val == "pre") {
				if ($scope.selectedPage == $scope.setPageStart) {
					$scope.setPageStart = $scope.setPageStart - 1;
					$scope.selectedPage = $scope.selectedPage - 1;
				} else
					$scope.selectedPage = $scope.selectedPage - 1;
			}
			if (val == "next") {
				if ($scope.selectedPage == $scope.setPageStart + 4) {
					$scope.setPageStart = $scope.setPageStart + 1;
					$scope.selectedPage = $scope.selectedPage + 1;
				} else
					$scope.selectedPage = $scope.selectedPage + 1;
			}
		}
		
		$scope.Navigation = function(selectedPage) {
			//$scope.SetDataLoaded = true;
			$rootScope.setLoader = true;
			$location.hash('notification-details');
			$anchorScroll();
			if ($scope.defaultSize <= 10)
				$scope.defaultSize = 10;
			else
				$scope.defaultSize;

			$scope.notificationManageObj = [];
			$scope.pageNumber = selectedPage;
			$scope.selectedPage = selectedPage;
			//$scope.getNotificationMessage($scope.loginUserID,$scope.selectedPage,$scope.defaultSize);
			$scope.totalNotificationMessages($scope.loginUserID)
		}
		
		$scope.refreshNotification=function(){
			$scope.selectedPage = 0;
			$scope.Navigation($scope.selectedPage)
			$scope.notificationManageObj=[];
			$scope.selectAllStatus = false;
			reloadData();
			//$scope.getNotificationMessage($scope.loginUserID, $scope.page);
			$scope.totalNotificationMessages($scope.loginUserID);
		}
		
        $scope.viewNotification=function (notificationId){
			var req = {
				    method: 'PUT',
				    url: '/api/notifications/view/'+notificationId+'/user/'+$scope.loginUserID
				};
			$http(req).success(function(data, status, headers,config) {
				if(data!=null){
					$scope.notificationManageObj=[];
					//$scope.page = 0;
					$scope.selectedPage = 0;
					$scope.defaultSize = 10;
					//$scope.getNotificationMessage($scope.loginUserID,$scope.selectedPage,$scope.defaultSize);
					$scope.totalNotificationMessages($scope.loginUserID);
				 }
			}).error(function(data, status, headers, config) {
				
			});
		}
		
		$scope.markRead = function(){
			$scope.methodCallCounter = 0;
			$scope.methodResponseCounter = 0;
			for (var i = 0; i < $scope.notificationManageObj.length; i++) {
                if ($scope.notificationManageObj[i].Selected){
                	if ($scope.notificationManageObj[i].viewed != null){
                		$location.hash('notification-details');
	                	$anchorScroll(); 							// used to scroll to the id 
						$scope.msg = "Already marked as read."; 
						$scope.icon = 'info_outline';
						$scope.styleclass = 'c-info';
						$scope.showReadAlertMessage = true;
						$timeout(function() {
							$scope.showReadAlertMessage = false;
						}, 2500);
                	}else{
	                    var notificationId = $scope.notificationManageObj[i].notificationId;
	                    var notificationName = $scope.notificationManageObj[i].title;
	                    $scope.methodCallCounter = $scope.methodCallCounter + 1;
						apiService
						.markReadNotifications(notificationId, $scope.loginUserID)
						.then(function(response) {
							$rootScope.notificationCount = $rootScope.notificationCount - 1;
							$scope.methodResponseCounter = $scope.methodResponseCounter + 1;
							if($scope.methodResponseCounter == $scope.methodCallCounter){
								$scope.refreshNotification();
							}
							
							
						});
                	}
					$scope.notificationManageObj[i].Selected = false;
                }
            }
			$scope.removeSelectAll();
			$scope.selectAll= false;
			angular.element(document.querySelector("#checkbox-label")).removeClass("is-checked");
			reloadData();
		
       };
		
		$scope.trashNotification = function(){
			$scope.trashMethodCallCounter = 0;
			$scope.trashMethodResponseCounter = 0;
			
			for (var i = 0; i < $scope.notificationManageObj.length; i++) {
                if ($scope.notificationManageObj[i].Selected) {
                    var notificationId = $scope.notificationManageObj[i].notificationId;
                    var notificationName = $scope.notificationManageObj[i].title;
                    $scope.trashMethodCallCounter = $scope.trashMethodCallCounter + 1;
					apiService
					.deleteNotifications(notificationId, $scope.loginUserID)
					.then(function(response) {
						$scope.notificationManageObj=[];
						$rootScope.notificationCount = $rootScope.notificationCount-1;
						$scope.trashMethodResponseCounter = $scope.trashMethodResponseCounter + 1;
						if($scope.trashMethodResponseCounter == $scope.trashMethodCallCounter){
							$scope.refreshNotification();
						}
					});
                }
            }
			$scope.removeSelectAll();
			$scope.selectAll= false;
			angular.element(document.querySelector("#checkbox-label")).removeClass("is-checked");
			reloadData();
			
       };
       
       $scope.removeSelectAll = function(){
    	   if($scope.selectAll == true){
    		   $scope.selectAll = false;
    		   $scope.selectAllStatus = false;
    	   }
       }
       
       
       $scope.setSelectAll = function(selected){
    	   $scope.selectAll = selected;
    	   $scope.selectAllStatus = true;
    	  /*f($scope.selectAll)
    	  	{$scope.selectAll = false;}
    	  else 
    	    {$scope.selectAll = true;}*/
    	   for (var i = 0; i < $scope.notificationManageObj.length; i++) {
   	        $scope.notificationManageObj[i].Selected = $scope.selectAll;
   	        if($scope.selectAll)
   	        angular.element(document.querySelector("#checkBox_label_" + i)).addClass("is-checked");
   	        else 
   	        angular.element(document.querySelector("#checkBox_label_" + i)).removeClass("is-checked");
   	       // $("checkBox").checked(true);
       	  }
    	  
       };
       
       //get Admin user.
       $scope.userDetailsFetch = function(){
    	   $scope.adminDetails = [];
			apiService
			.getAllUserCount()
			.then(
					function(response) {
						$scope.userDetails = response.data.response_body;
						angular.forEach($scope.userDetails,function(value,key){
							if(value.active == "true"){
								if(value.userAssignedRolesList[0].name == "Admin" || value.userAssignedRolesList[0].name == "admin"){
									if($scope.adminDetails.length < 1){
										$scope.adminDetails.push({
											created : value.created,
											firstName : value.firstName,
											emailId : value.emailId,
											lastName : value.lastName
										});
									}else if($scope.adminDetails[0].created > value.created){
										$scope.adminDetails1 = [];
										$scope.adminDetails1.push({
											created : value.created,
											firstName : value.firstName,
											emailId : value.emailId,
											lastName : value.lastName
										});
										$scope.adminDetails = $scope.adminDetails1;
									}
								}
							}
						});
						
					},
					function(error) {
						console.log(error);
					});
			}
			$scope.userDetailsFetch();
			
			$scope.filterByDateSubject = function(notification) {	
				if(!$scope.search) return true; 
				return ( (angular.lowercase(notification.startdateForSorting).indexOf(angular.lowercase($scope.search)) !== -1) ||
						(angular.lowercase((notification.message.toString())).indexOf(angular.lowercase($scope.search)) !== -1) );  		
		    };
			

	},

});