/*
 * Copyright (C) 2020 Technology Rediscovery LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitWithProp;
import com.tcvcog.tcvce.integration.PropertyIntegrator;

/**
 *
 * @author sylvia
 */
public class OccPeriodPropertyUnitHeavy 
        extends OccPeriod{
    
    protected PropertyUnitWithProp propUnitProp;

    public OccPeriodPropertyUnitHeavy() {
    }

    public OccPeriodPropertyUnitHeavy(OccPeriod opLight) {
        this.periodID = opLight.periodID;
        this.propertyUnitID = opLight.propertyUnitID;

        this.type = opLight.type;
        
        this.governingInspection = opLight.governingInspection;

        this.manager = opLight.manager;
        
        this.periodTypeCertifiedBy = opLight.periodTypeCertifiedBy;
        this.periodTypeCertifiedTS = opLight.periodTypeCertifiedTS;
        this.eventList = opLight.eventList;
        
        this.source = opLight.source;
        this.createdBy = opLight.createdBy;
        this.createdTS = opLight.createdTS;
        
        this.startDate = opLight.startDate;
        this.startDateCertifiedTS = opLight.startDateCertifiedTS;
        this.startDateCertifiedBy = opLight.startDateCertifiedBy;
        
        this.endDate = opLight.endDate;
        this.endDateCertifiedTS = opLight.endDateCertifiedTS;
        this.endDateCertifiedBy = opLight.endDateCertifiedBy;
        
        this.authorizedTS = opLight.authorizedTS;
        this.authorizedBy = opLight.authorizedBy;
        
        this.overrideTypeConfig = opLight.overrideTypeConfig;

        this.notes = opLight.notes;
        
        this.active = opLight.active;
    }
//
//    /**
//     * Uses provided property integrator object to set the member variable containing the PropertyUnitWithProp object
//     *
//     * @param pi Property integrator object, required for this operation
//     */
//    public void updatePropUnitProp(PropertyIntegrator pi) throws IntegrationException {
//        setPropUnitProp(pi.getPropertyUnitWithProp(propertyUnitID));
//    }

    /**
     * @return the propUnitProp
     */
    public PropertyUnitWithProp getPropUnitProp() {
        return propUnitProp;
    }

    /**
     * @param propUnitProp the propUnitProp to set
     */
    public void setPropUnitProp(PropertyUnitWithProp propUnitProp) {
        this.propUnitProp = propUnitProp;
    }

    /**
     * @param propertyUnitID the propertyUnitID to set
     */
}
