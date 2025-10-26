package com.budgee.service;

import com.budgee.model.Group;
import com.budgee.model.GroupSharing;
import com.budgee.model.User;

public interface GroupSharingService {

    void createGroupSharing(User user, Group group, String sharingToken);

    GroupSharing getGroupSharingByUserAndGroup(User user, Group group);
}
