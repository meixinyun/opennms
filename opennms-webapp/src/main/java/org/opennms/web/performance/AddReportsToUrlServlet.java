//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.performance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.resource.Vault;
import org.opennms.web.MissingParameterException;
import org.opennms.web.Util;
import org.opennms.web.graph.PrefabGraph;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class AddReportsToUrlServlet extends HttpServlet {
    protected PerformanceModel m_model;

    public void init() throws ServletException {
        WebApplicationContext m_webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        m_model = (PerformanceModel) m_webAppContext.getBean("performanceModel", PerformanceModel.class);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] requiredParameters = new String[] {
                "node",
                "resourceType"
        };
        
        // required parameter node
        String nodeIdString = request.getParameter("node");
        if (nodeIdString == null) {;
            throw new MissingParameterException("node", requiredParameters);
        }
        
        String resourceTypeName = request.getParameter("resourceType");
        if (resourceTypeName == null) {
            throw new MissingParameterException("resourceType",
                                                requiredParameters);
        }
        
        GraphResourceType resourceType = m_model.getResourceTypeByName(resourceTypeName);

        // optional parameter resource
        String resourceName = request.getParameter("resource");
        if (resourceName == null) {
            resourceName = "";
        }
        
        int nodeId = Integer.parseInt(nodeIdString);
        
        GraphResource resource = m_model.getResourceForNodeResourceResourceType(nodeId, resourceName, resourceTypeName);
        Set<GraphAttribute> attributes = resource.getAttributes();

        // In this block of code, it is possible to end up with an empty
        // list of queries. This will result in a somewhat cryptic
        // "Missing parameter" message on the results.jsp page and will
        // probably be changed soon to a nicer error message.


        /*
        PrefabGraph[] queries = null;
        queries = m_model.getQueriesByResourceTypeAttributes(resourceTypeName, attributes);
        String[] queryNames = new String[queries.length];

        for (int i = 0; i < queries.length; i++) {
            queryNames[i] = queries[i].getName();
        }
        */
        
        List<PrefabGraph> queries =
            resourceType.getAvailablePrefabGraphs(attributes);
        List<String> queryNames = new ArrayList<String>(queries.size());
        for (PrefabGraph query : queries) {
            queryNames.add(query.getName());
        }


        Map additions = new HashMap();
        additions.put("reports", queryNames.toArray(new String[queryNames.size()]));
        additions.put("type", "performance");
        additions.put("resourceType", resourceTypeName);
        String queryString = Util.makeQueryString(request, additions);

        response.sendRedirect(Util.calculateUrlBase(request) + "graph/results?"
			      + queryString);
    }
}
