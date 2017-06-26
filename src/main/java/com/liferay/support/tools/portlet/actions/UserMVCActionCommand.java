package com.liferay.support.tools.portlet.actions;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.support.tools.constants.LDFPortletKeys;
import com.liferay.support.tools.utils.CommonUtil;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Create Users
 * 
 * @author Yasuyuki Takeo
 */
@Component(
    immediate = true, 
    property = { 
        "javax.portlet.name=" + LDFPortletKeys.LIFERAY_DUMMY_FACTORY,
        "mvc.command.name=" + LDFPortletKeys.USERS
    }, 
    service = MVCActionCommand.class
)
public class UserMVCActionCommand extends BaseMVCActionCommand {

	/**
	 * Create Users
	 * 
	 * @param actionRequest
	 * @param actionResponse
	 * @throws PortalException
	 */
	private void createUsers(ActionRequest actionRequest, ActionResponse actionResponse) throws PortalException {
		long numberOfusers = 0;
		String baseScreenName = "";
		long[] organizationIds = null;
		long[] groupIds = null;
		long[] roleIds = null;
		long[] userGroupIds = null;
		boolean male;
		String password;

		// Fetch data
		numberOfusers = ParamUtil.getLong(actionRequest, "numberOfusers", 0);
		baseScreenName = ParamUtil.getString(actionRequest, "baseScreenName", "");
		male = ParamUtil.getBoolean(actionRequest, "male", true);
		password = ParamUtil.getString(actionRequest, "password", "test");

		// Organization
		String[] organizations = ParamUtil.getStringValues(actionRequest, "organizations", null);
		organizationIds = _commonUtil.convertStringToLongArray(organizations);

		// Sites
		String[] groups = ParamUtil.getStringValues(actionRequest, "groups", null);
		groupIds = _commonUtil.convertStringToLongArray(groups);

		// Roles
		String[] roles = ParamUtil.getStringValues(actionRequest, "roles", null);
		roleIds = _commonUtil.convertStringToLongArray(roles);

		// User Group
		String[] userGroups = ParamUtil.getStringValues(actionRequest, "userGroups", null);
		userGroupIds = _commonUtil.convertStringToLongArray(userGroups);

		double loader = 10;

		ServiceContext serviceContext = ServiceContextFactory.getInstance(Group.class.getName(), actionRequest);

		System.out.println("Starting to create " + numberOfusers + " users");

		for (long i = 1; i <= numberOfusers; i++) {
			if (numberOfusers >= 100) {
				if (i == (int) (numberOfusers * (loader / 100))) {
					System.out.println("Creating users..." + (int) loader + "% done");
					loader = loader + 10;
				}
			}

			StringBundler screenName = new StringBundler(2);
			screenName.append(baseScreenName);
			screenName.append(i);

			StringBundler emailAddress = new StringBundler(2);
			emailAddress.append(screenName);
			emailAddress.append("@liferay.com");

			// Create user and apply roles
			_userDataService.createUserData(serviceContext, organizationIds, groupIds, roleIds, userGroupIds, male,
					password, screenName.toString(), emailAddress.toString(), baseScreenName, i);
		}

		SessionMessages.add(actionRequest, "success");

		System.out.println("Finished creating " + numberOfusers + " users");
	}

	@Override
	protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) {
		try {
			// Create users
			createUsers(actionRequest, actionResponse);

		} catch (Exception e) {
			hideDefaultSuccessMessage(actionRequest);
			_log.error(e, e);
		}

		actionResponse.setRenderParameter("mvcRenderCommandName", LDFPortletKeys.COMMON);
	}

	@Reference
	private UserDataService _userDataService;

	@Reference
	private CommonUtil _commonUtil;

	private static final Log _log = LogFactoryUtil.getLog(UserMVCActionCommand.class);
}
