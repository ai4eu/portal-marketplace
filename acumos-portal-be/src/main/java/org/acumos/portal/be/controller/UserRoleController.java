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

package org.acumos.portal.be.controller;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.acumos.cds.domain.MLPCatalog;
import org.acumos.cds.domain.MLPRole;
import org.acumos.cds.domain.MLPRoleFunction;
import org.acumos.cds.domain.MLPUser;
import org.acumos.portal.be.APINames;
import org.acumos.portal.be.common.JSONTags;
import org.acumos.portal.be.common.JsonRequest;
import org.acumos.portal.be.common.JsonResponse;
import org.acumos.portal.be.common.exception.AcumosServiceException;
import org.acumos.portal.be.common.exception.UserServiceException;
import org.acumos.portal.be.service.UserRoleService;
import org.acumos.portal.be.transport.MLRole;
import org.acumos.portal.be.transport.MLRoleFunction;
import org.acumos.portal.be.transport.User;
import org.acumos.portal.be.util.PortalUtils;
import org.acumos.portal.be.util.SanitizeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;

@Controller
@RequestMapping("/")
public class UserRoleController extends AbstractController {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());	

	@Autowired
	private UserRoleService userRoleService;


	public UserRoleController() {

	}

	/**
	 * @return List roles in JSON format.
	 */
	@ApiOperation(value = "Gets a list of roles for user.", response = MLRole.class, responseContainer = "List")
	@RequestMapping(value = { APINames.ROLES }, method = RequestMethod.GET, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<List<MLRole>> getRolesList() {
		List<MLRole> mlRoles = null;
		JsonResponse<List<MLRole>> data = new JsonResponse<>();
		try {
			List<MLRole> mlRoleList = userRoleService.getAllRoles();
			if (!PortalUtils.isEmptyList(mlRoleList)) {
				mlRoles = new ArrayList<>();
				for (MLRole role : mlRoleList) {
					List<MLPRoleFunction> mlpRoleFunctionList=userRoleService.getRoleFunctions(role.getRoleId());
					List<String> roleFunctionName=mlpRoleFunctionList.stream().map(MLPRoleFunction::getName).collect(Collectors.toList());
					MLRole mlRole = role;
					mlRole.setPermissionList(roleFunctionName);
					mlRoles.add(mlRole);
				}
			}
			if (mlRoles != null) {
				data.setResponseBody(mlRoles);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Roles fetched Successfully");
				log.debug("getRolesList: size is {} ", mlRoles.size());
			} else {
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
				data.setResponseDetail("Error Occurred while getRolesList()");
			}
		} catch (UserServiceException e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
			data.setResponseDetail("Error Occurred while getRolesList()");
			log.error("Error Occurred while getRolesList()", e.getMessage());
		} catch (Exception e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_EXCEPTION);
			data.setResponseDetail("Exception Occurred Fetching roles for Market Place user");
			log.error("Exception Occurred while getRolesList()", e.getMessage());
		}
		return data;
	}

	/**
	 * @param request
	 *            HttpServletRequest
	 * @param roleId
	 *            role ID
	 * @param response
	 *            HttpServletResponse
	 * @return object in JSON format.
	 */
	@ApiOperation(value = "Gets a role Detail for the given RoleId.", response = MLRole.class)
	@RequestMapping(value = { APINames.ROLES_DEATAILS }, method = RequestMethod.GET, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLRole> getRoleDetails(HttpServletRequest request, @PathVariable("roleId") String roleId,
			HttpServletResponse response) {
		
		roleId = SanitizeUtils.sanitize(roleId);
		JsonResponse<MLRole> data = new JsonResponse<>();
		try {
				MLRole mlRole = PortalUtils.convertToMLRole(userRoleService.getRole(roleId));
				if(mlRole !=null) {
					List<MLPRoleFunction> roleFunctions=userRoleService.getRoleFunctions(roleId);
					List<MLPCatalog> catalogs=userRoleService.getRoleCatalogs(roleId);
					List<String> roleFunctionList=roleFunctions.stream().map(MLPRoleFunction::getName).collect(Collectors.toList());
					List<String> catalogList=catalogs.stream().map(MLPCatalog::getCatalogId).collect(Collectors.toList());
					mlRole.setPermissionList(roleFunctionList);
					mlRole.setCatalogIds(catalogList);
					data.setResponseBody(mlRole);
					data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
					data.setResponseDetail("Role fetched Successfully");
					log.debug("getRoleDetails :  ", mlRole);
				}
				else {
					data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
					data.setResponseDetail("Error Occurred while getRoleDetails()");
				}
			
		}catch (UserServiceException e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail("Exception occured while fetching role details");
			log.error("Exception Occurred Fetching role Detail for roleId :" + roleId, e.getMessage());
		}catch (Exception e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail("Exception occured while fetching role details");
			log.error("Exception Occurred Fetching role Detail for roleId :" + roleId, e.getMessage());
		}
		return data;
	}

	/**
	 * @param request
	 *            HttpServletRequest
	 * @param role
	 *            Role
	 * @param response
	 *            HttpServletResponse
	 * @return object in JSON format.
	 */
	@ApiOperation(value = "Creates a new role.", response = MLPRole.class)
	@RequestMapping(value = { APINames.CREATE_ROLE }, method = RequestMethod.POST, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLPRole> postRole(HttpServletRequest request, @RequestBody JsonRequest<MLRole> role,
			HttpServletResponse response) {
		MLPRole mlpRole = null;
		JsonResponse<MLPRole> data = new JsonResponse<>();
		MLPRoleFunction mlpFunction = null;
		try {
			if (role.getBody() != null) {
				mlpRole = userRoleService.createRole(role.getBody());
			}
			if (mlpRole.getRoleId() != null && role.getBody().getPermissionList() != null) {
				for (String permission : role.getBody().getPermissionList()) {
					MLPRoleFunction roleFunction = new MLPRoleFunction();
					roleFunction.setRoleId(mlpRole.getRoleId());
					roleFunction.setName(permission);
					mlpFunction = userRoleService.createRoleFunction(roleFunction);
				}
			}
			if(!PortalUtils.isEmptyList(role.getBody().getCatalogIds())) {
				userRoleService.addCatalogsInRole(role.getBody().getCatalogIds(),mlpRole.getRoleId());
			}
			
			if (mlpFunction != null) {
				data.setResponseBody(mlpRole);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Role created Successfully");
				log.debug("postRole :  ", role);
			} else {
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
				data.setResponseDetail("Error Occurred while postRole()");
			}
		} catch (UserServiceException e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
			data.setResponseDetail("Exception Occurred while creating role");
			log.error("Error Occurred while getRoleDetails() :", e.getMessage());
		} catch (Exception e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail("Error occured while creating role");
			log.error("Exception Occurred while creating role :", e.getMessage());
		}
		return data;
	}

	/**
	 * @param role
	 *            Role
	 * @return success message
	 */
	@ApiOperation(value = "Update a role.", response = MLPRole.class)
	@RequestMapping(value = { APINames.UPDATE_ROLE }, method = RequestMethod.PUT, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<Object> updateRole(@RequestParam(value = "roleId", required = true) String roleId,@RequestBody JsonRequest<MLRole> role) {
		JsonResponse<Object> response = new JsonResponse<>();
		try {
			roleId=SanitizeUtils.sanitize(roleId);
			if (role != null && role.getBody() != null) {
				userRoleService.updateRole(roleId,role.getBody().getName());
				if(!PortalUtils.isEmptyList(role.getBody().getPermissionList()))
					userRoleService.updateModulePermission(roleId,role.getBody().getPermissionList());
				if(!PortalUtils.isEmptyList(role.getBody().getCatalogIds()))
					userRoleService.updateCatalogsInRole(role.getBody().getCatalogIds(), roleId);
				response.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				response.setResponseDetail("Role updated Successfuly");
				log.debug("updateRole :  ", role.getBody());
			}
			else {
				response.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
				response.setResponseDetail("Role updation Failed");
				log.debug("updateRole Failed :  ", role.getBody());
			}
		} catch (UserServiceException e) {
			response.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
			response.setResponseDetail("Exception Occurred while updateRole()");
			log.error("Error Occurred while updateRole() :", e.getMessage());
		} catch (Exception e) {
			response.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
			response.setResponseDetail("Exception Occurred while updateRole()");
			log.error("Exception Occurred while updateRole() :", e.getMessage());
		}
		return response;
	}

	/**
	 * @param roleId
	 *            role ID
	 * @return Success or error message
	 */
	@ApiOperation(value = "Delete a role.")
	@RequestMapping(value = { APINames.DELETE_ROLE }, method = RequestMethod.POST, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<Object> deleteRole(@PathVariable("roleId") String roleId) {
		JsonResponse<Object> response = new JsonResponse<>();
		roleId=SanitizeUtils.sanitize(roleId);
		try {
				List<MLPUser> userList=userRoleService.getRoleUsers(roleId);
				List<String> roleUserList=userList.stream().map(MLPUser::getUserId).collect(Collectors.toList());
				if(!PortalUtils.isEmptyList(roleUserList)) {
					throw new AcumosServiceException(AcumosServiceException.ErrorCode.IO_EXCEPTION,
							"Role can't be deleted. It's already assigned to some user");
				}
				List<MLPRoleFunction> mlpRoleFunctionList=userRoleService.getRoleFunctions(roleId);
				List<MLPCatalog> catalogs=userRoleService.getRoleCatalogs(roleId);
				List<String> catalogList=catalogs.stream().map(MLPCatalog::getCatalogId).collect(Collectors.toList());
				if(!PortalUtils.isEmptyList(catalogList))
					userRoleService.dropCatalogsInRole(catalogList, roleId);
				for(MLPRoleFunction roleFunction:mlpRoleFunctionList) {
					userRoleService.deleteRoleFunction(roleId, roleFunction.getRoleFunctionId());
				}
				userRoleService.deleteRole(roleId);
				response.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				response.setResponseDetail("Role deleted Successfuly");
		}catch (AcumosServiceException ae) {
			response.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
			response.setResponseDetail(ae.getMessage());
			log.error("Error Occurred while deleteRole() :", ae.getMessage());
		}catch (UserServiceException e) {
			response.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
			response.setResponseDetail("Exception Occurred while deleteRole()");
			log.error("Error Occurred while deleteRole() :", e.getMessage());
		} catch (Exception e) {
			response.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
			response.setResponseDetail("Exception Occurred while deleteRole()");
			log.error("Exception Occurred while deleteRole() :", e.getMessage());
		}
		return response;
	}

	/**
	 * @param roleFunction
	 *            Role function
	 * @return object in JSON format.
	 */
	@ApiOperation(value = "Gets a rolefunction Detail for the given RoleId.", response = MLRoleFunction.class)
	@RequestMapping(value = { APINames.ROLE_FUNCTION_DETAILS }, method = RequestMethod.GET, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLRoleFunction> getRoleFunction(@RequestBody JsonRequest<MLRoleFunction> roleFunction) {
		MLRoleFunction mlRoleFunction = null;
		JsonResponse<MLRoleFunction> data = new JsonResponse<>();
		try {
			if (!PortalUtils.isEmptyOrNullString(roleFunction.getBody().getMlRole().getRoleId())
					&& !PortalUtils.isEmptyOrNullString(roleFunction.getBody().getRoleFunctionId())) {
				mlRoleFunction = userRoleService.getRoleFunction(roleFunction.getBody().getMlRole().getRoleId(),
						roleFunction.getBody().getRoleFunctionId());
			}
			if (mlRoleFunction != null) {
				data.setResponseBody(mlRoleFunction);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Role fetched Successfully");
				log.debug("getRoleDetails :  ", mlRoleFunction);
			} else {
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
			}
		} catch (UserServiceException e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
			data.setResponseDetail("Error Occurred while getRoleFunction()");
			log.error("Error Occurred while getRoleFunction() :", e.getMessage());
		} catch (Exception e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail("Error occured while getRoleFunction()");
			log.error("Exception Occurred while getRoleFunction()", e.getMessage());
		}
		return data;
	}

	/**
	 * @param mlpRoleFunction
	 *            Role function
	 * @return object in JSON format.
	 */
	@ApiOperation(value = "Creates a new rolefunction.", response = MLPRole.class)
	@RequestMapping(value = { APINames.CREATE_ROLE_FUNCTION }, method = RequestMethod.POST, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLPRoleFunction> createRoleFunction(@RequestBody JsonRequest<MLPRoleFunction> mlpRoleFunction) {
		MLPRoleFunction roleFunction = null;
		JsonResponse<MLPRoleFunction> data = new JsonResponse<>();
		try {
			if (mlpRoleFunction.getBody() != null) {
				roleFunction = userRoleService.createRoleFunction(mlpRoleFunction.getBody());
				data.setResponseBody(roleFunction);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("RoleFunction created Successfully");
				log.debug("createRoleFunction() :");
			}
		} catch (UserServiceException e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
			data.setResponseDetail("Exception Occurred while creating role");
			log.error("Error Occurred while createRoleFunction() :", e.getMessage());
		} catch (Exception e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail("Error occured while creating roles");
			log.error("Exception Occurred while createRoleFunction() :", e.getMessage());
		}
		return data;
	}

	/**
	 * @param mlpRoleFunction
	 *            Role function
	 * @return Success or error message
	 */
	@ApiOperation(value = "Update a rolefunction.", response = MLPRole.class)
	@RequestMapping(value = { APINames.UPDATE_ROLE_FUNCTION }, method = RequestMethod.PUT, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<Object> updateRoleFunction(@RequestBody JsonRequest<MLPRoleFunction> mlpRoleFunction) {
		JsonResponse<Object> response = new JsonResponse<>();
		try {
			if (mlpRoleFunction != null && mlpRoleFunction.getBody() != null) {
				userRoleService.updateRoleFunction(mlpRoleFunction.getBody());
				response.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				response.setResponseDetail("RoleFunction updated Successfuly");
				log.debug("updateRoleFunction() :");
			}
		} catch (UserServiceException e) {
			response.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
			response.setResponseDetail("Exception Occurred while updateRoleFunction()");
			log.error("Error Occurred while updateRoleFunction() :", e.getMessage());
		} catch (Exception e) {
			response.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
			response.setResponseDetail("Exception Occurred while updateRoleFunction()");
			log.error("Exception Occurred while updateRoleFunction :", e.getMessage());
		}
		return response;
	}

	/**
	 * @param roleFunction
	 *            Role function
	 * @return Success or error message
	 */
	@ApiOperation(value = "Delete role function.", response = MLPRole.class)
	@RequestMapping(value = {
			APINames.DELETE_ROLE_FUNCTION }, method = RequestMethod.DELETE, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<Object> deleteRoleFunction(@RequestBody JsonRequest<MLRoleFunction> roleFunction) {
		JsonResponse<Object> response = new JsonResponse<>();
		try {
			if (!PortalUtils.isEmptyOrNullString(roleFunction.getBody().getMlRole().getRoleId())
					&& !PortalUtils.isEmptyOrNullString(roleFunction.getBody().getRoleFunctionId())) {
				userRoleService.deleteRoleFunction(roleFunction.getBody().getMlRole().getRoleId(),
						roleFunction.getBody().getRoleFunctionId());
				response.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				response.setResponseDetail("RoleFunction deleted Successfuly");
				log.debug("RoleFunction deleted successfuly");
			} else {
				response.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
				response.setResponseDetail("Exception Occurred while deleteRoleFunction()");
				log.error("Error Occurred while deleteRoleFunction() :");
			}
		} catch (UserServiceException e) {
			response.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
			response.setResponseDetail("Exception Occurred while deleteRoleFunction()");
			log.error("Error Occurred while deleteRoleFunction() :", e.getMessage());
		} catch (Exception e) {
			response.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
			response.setResponseDetail("Exception Occurred while deleteRoleFunction()");
			log.error("Exception Occurred while deleteRoleFunction() :", e.getMessage());
		}
		return response;
	}

	@ApiOperation(value = "Update role for user", response = MLPRole.class)
	@RequestMapping(value = { APINames.UPDATE_ROLES_USER }, method = RequestMethod.POST, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLPRole> updateUserRole(@RequestBody JsonRequest<List<User>> userList) {
		JsonResponse<MLPRole> data = new JsonResponse<>();
		try {

			for (User user : userList.getBody()) {
				userRoleService.updateUserRole(user);
			}

			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Role updated Successfully");
		} catch (Exception e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail("Error occured while creating role");
			log.error("Exception Occurred while creating role :", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Gets a list of roles for user.", response = MLRole.class, responseContainer = "List")
	@RequestMapping(value = { APINames.USER_ROLES }, method = RequestMethod.GET, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<List<MLRole>> getRolesForUser(HttpServletRequest request, @PathVariable("userId") String userId,
			HttpServletResponse response) {
		List<MLRole> mlRoles = null;
		JsonResponse<List<MLRole>> data = new JsonResponse<>();
		try {
			mlRoles = userRoleService.getRolesForUser(userId);
			if (mlRoles != null) {
				data.setResponseBody(mlRoles);
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
				data.setResponseDetail("Roles for user fetched Successfully");
				log.debug("getRolesList: size is {} ", mlRoles.size());
			} else {
				data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
				data.setResponseDetail("Error Occurred while getRolesList()");
			}
		} catch (Exception e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_EXCEPTION);
			data.setResponseDetail("Exception Occurred Fetching roles for Market Place user");
			log.error("Exception Occurred while getRolesList()", e.getMessage());
		}
		return data;
	}

	/*
	 * @ApiOperation(value = "Gets a list of role count for user.", response =
	 * MLRole.class, responseContainer = "List")
	 * 
	 * @RequestMapping(value = { APINames.USER_ROLE_COUNT}, method =
	 * RequestMethod.GET, produces = APPLICATION_JSON)
	 * 
	 * @ResponseBody public JsonResponse<MLRole>
	 * getRoleCountForUser(HttpServletRequest request, HttpServletResponse response)
	 * { MLRole mlRoles = null; JsonResponse<MLRole> data = new JsonResponse<>();
	 * //@PathVariable("pageRequest") RestPageRequest pageRequest, RestPageRequest
	 * pageRequest = new RestPageRequest();
	 * 
	 * try { mlRoles = userRoleService.getRoleCountForUser(pageRequest); if (mlRoles
	 * != null) { data.setResponseBody(mlRoles);
	 * data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
	 * data.setResponseDetail("Role Count for user fetched Successfully");
	 * log.debug("getRoleCountForUser: size is {} ",
	 * mlRoles); } else { data.setErrorCode(JSONTags.TAG_ERROR_CODE_FAILURE);
	 * data.setResponseDetail("Error Occurred while getRoleCountForUser()"); } }
	 * catch (Exception e) { data.setErrorCode(JSONTags.TAG_ERROR_CODE_EXCEPTION);
	 * data.
	 * setResponseDetail("Exception Occurred Fetching role count for Market Place user"
	 * ); log.error("Exception Occurred while getRoleCountForUser()", e); 
	 * } return data; 
	 * }
	 */

	@ApiOperation(value = "Change user roles", response = MLPRole.class)
	@RequestMapping(value = { APINames.CHANGE_ROLES_USER }, method = RequestMethod.PUT, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<MLPRole> changeUserRoles(@RequestBody JsonRequest<User> user) {
		JsonResponse<MLPRole> data = new JsonResponse<>();
		try {

			if (user.getBody() != null) {

				userRoleService.updateUserRoles(user.getBody());
			}

			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Role updated Successfully");
		} catch (Exception e) {
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail("Error occured while updating role");
			log.error("Exception Occurred while updating role :", e.getMessage());
		}
		return data;
	}

	@ApiOperation(value = "Get user role count", response = MLPRole.class)
	@RequestMapping(value = { APINames.ROLES_COUNT }, method = RequestMethod.GET, produces = APPLICATION_JSON)
	@ResponseBody
	public JsonResponse<List<MLRole>> getRoleUsersCount() {
		JsonResponse<List<MLRole>> data = new JsonResponse<List<MLRole>>();
		List<MLRole> rolesCountMap = null;
		try {
			// String json = new ObjectMapper().writeValueAsString(map);
			rolesCountMap = userRoleService.getRoleUsersCount();
			// MLRole role = new MLRole();
			// role.setRoleIdUserCount(rolesCountMap);

			data.setResponseBody(rolesCountMap);
			data.setErrorCode(JSONTags.TAG_ERROR_CODE_SUCCESS);
			data.setResponseDetail("Role count fetched Successfully");
		} catch (Exception e) {
			e.printStackTrace();
			data.setErrorCode(JSONTags.TAG_ERROR_CODE);
			data.setResponseDetail("Error occured while getRoleUsersCount");
			log.error("Exception Occurred while getRoleUsersCount :", e.getMessage());
		}
		return data;
	}
}
