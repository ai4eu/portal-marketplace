﻿.. ===============LICENSE_START=======================================================
.. Acumos CC-BY-4.0
.. ===================================================================================
.. Copyright (C) 2017-2018 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
.. ===================================================================================
.. This Acumos documentation file is distributed by AT&T and Tech Mahindra
.. under the Creative Commons Attribution 4.0 International License (the "License");
.. you may not use this file except in compliance with the License.
.. You may obtain a copy of the License at
..
.. http://creativecommons.org/licenses/by/4.0
..
.. This file is distributed on an "AS IS" BASIS,
.. WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
.. See the License for the specific language governing permissions and
.. limitations under the License.
.. ===============LICENSE_END=========================================================

.. _intro:

============
Introduction
============

This user guide  describes how to use the Admin Interface for Acumos portals.

What is Covered by the Admin Interface
--------------------------------------

The Admin Interface is a web-based tool for a site admin to:

1. Monitor the site activity

2. Manage the roles that restrict use of various portal features and assign catalogs to roles.

3. Manage users and change their roles.

4. Update and edit the site content.

5. Configure the site.

6. Manatained Backup Logs.

7. Manage Federation relationships: configure peer gateways and set up
   subscriptions to that peer’s public marketplace.

Admin Access to the Acumos Portal
---------------------------------

When a new Acumos Portal is deployed, a default admin user will be
created in the process of deployment by common-dataservice database
setup scripts. A default username (“***TBD***\ ”) *and* password
(“***TBD***\ ”) will be assigned to the admin and must be changed upon
first login, as the password will be set to “expired” when created.

Users can be assigned the role of Admin via the Portal UI at “Site
Admin” / “User Management”. Select the user and pick “admin” under the
“Change Role To” dropdown. The user will need to logout and back in for
the “Site Admin” tools to be visible.

The Site Admin Dashboard provides the toolset that admins use to manage
the Acumos portal. The Site Admin Dashboard can be accessed by site
admins only. Admins will have the “SITE ADMIN” option on the left of the
portal UI when they login.
