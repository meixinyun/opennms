/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 10th 2010 jonathan@opennms.org
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="reportCatalog")

/**
 * ReportStoreCatalog contains details of reports that have already been run
 *
 *  @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 */

public class ReportCatalogEntry implements Serializable {

    private static final long serialVersionUID = -5351014623584691820L;
    
    private Integer m_id;
    
    private String m_reportId;
    
    private String m_title;
    
    private Date m_date;
    
    private String m_location;

    @Id
    @Column(name="id")
    @SequenceGenerator(name="reportCatalogSequence", sequenceName="reportCatalogNxtId")
    @GeneratedValue(generator="reportCatalogSequence")
    public Integer getId() {
        return m_id;
    }

    public void setId(Integer id) {
        m_id = id;
    }

    @Column(name="reportId", length=256)
    public String getReportId() {
        return m_reportId;
    }

    public void setReportId(String reportId) {
        m_reportId = reportId;
    }

    @Column(name="title", length=256)
    public String getTitle() {
        return m_title;
    }

    public void setTitle(String title) {
        m_title = title;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="date", nullable=false)
    public Date getDate() {
        return m_date;
    }

    public void setDate(Date date) {
        m_date = date;
    }

    @Column(name="location", length=256)
    public String getLocation() {
        return m_location;
    }

    public void setLocation(String location) {
        m_location = location;
    }

}
