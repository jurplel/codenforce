/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.SearchParams;
import com.tcvcog.tcvce.entities.search.SearchParamsCEActionRequests;
import com.tcvcog.tcvce.entities.search.SearchParamsCECases;
import com.tcvcog.tcvce.entities.search.SearchParamsCEEvents;
import com.tcvcog.tcvce.entities.search.SearchParamsPersons;
import com.tcvcog.tcvce.entities.search.SearchParamsProperties;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author sylvia
 */
public class SearchCoordinator extends BackingBeanUtils implements Serializable{

    /**
     * Creates a new instance of SearchCoordinator
     */
    public SearchCoordinator() {
    }
    
    
    
    
   
    
    protected SearchParamsProperties getSearchParamsSkeletonProperties(){
        SearchParamsProperties propParams = new SearchParamsProperties();
        // superclass
        propParams.setFilterByStartEndDate(false);
        propParams.setFilterByObjectID(false);
        propParams.setLimitResultCountTo100(true);
        
        // subclass SearchParamsProperties
        propParams.setFilterByLotAndBlock(false);
        propParams.setFilterByParcelID(false);
        propParams.setFilterByAddressPart(true);
        propParams.setFilterByStreetPart(true);
        propParams.setFilterByCECaseStartEndDate(false);
        propParams.setFilterByRental(false);
        propParams.setFilterByVacant(false);
        propParams.setFilterByUnits(false);
        propParams.setFilterBySource(false);
        propParams.setFilterByPropertyUseType(false);
        propParams.setFilterByPerson(false);
        
        return propParams;
    }
    
    public SearchParamsCEEvents getSearchParamsEventsRequiringView(User u, Municipality muni){
        EventCoordinator ec = getEventCoordinator();
        
        // event types are always bundled in an EventCategory
        // so in this case of this query, we don't care about the Category title,
        // only the type
        EventCategory timelineEventTypeCategory = ec.getInitializedEventCateogry();
        timelineEventTypeCategory.setEventType(EventType.Timeline);
        
        SearchParamsCEEvents eventParams = new SearchParamsCEEvents();
        
        eventParams.setFilterByMuni(true);
        eventParams.setMuni(muni);
        eventParams.setFilterByStartEndDate(false);
        eventParams.setFilterByObjectID(false);
        eventParams.setLimitResultCountTo100(true);
        
        eventParams.setFilterByEventCategory(false);
        eventParams.setFilterByEventType(false);
        
        eventParams.setFilterByCaseID(false);
        
        eventParams.setFilterByEventOwner(false);
        eventParams.setOwnerUserID(u);
        
        eventParams.setFilterByActive(true);
        eventParams.setIsActive(true);
        
        eventParams.setFilterByRequiresViewConfirmation(true);
        eventParams.setIsViewConfirmationRequired(true);
        
        eventParams.setFilterByViewed(true);
        eventParams.setIsViewed(false);
        
        eventParams.setFilterByPerson(false);
        eventParams.setUseViewConfirmedAtDateRange(false);
        
        eventParams.setFilterByHidden(false);
        
        return eventParams;
    }
    
    public SearchParamsCEEvents getSearchParamsOfficerActivity(User u, Municipality m){
        // event types are always bundled in an EventCategory
        // so in this case of this query, we don't care about the Category title,
        // only the type
        
        SearchParamsCEEvents eventParams = new SearchParamsCEEvents();
        
        eventParams.setFilterByMuni(true);
        eventParams.setMuni(m);
        
        eventParams.setFilterByStartEndDate(true);
        eventParams.setUseRelativeDates(true);
        // query from a week ago to now
        eventParams.setStartDateRelativeDays(-7);
        eventParams.setEndDateRelativeDays(0);
        
        eventParams.setFilterByObjectID(false);
        eventParams.setLimitResultCountTo100(true);
        
        eventParams.setFilterByEventCategory(false);
        eventParams.setFilterByEventType(false);
        
        eventParams.setFilterByCaseID(false);
        
        eventParams.setFilterByEventOwner(true);
        eventParams.setOwnerUserID(u);
        
        eventParams.setFilterByActive(true);
        eventParams.setIsActive(true);
        
        eventParams.setFilterByRequiresViewConfirmation(false);
        
        eventParams.setFilterByViewed(false);
        eventParams.setIsViewed(false);
        
        eventParams.setFilterByPerson(false);
        eventParams.setUseViewConfirmedAtDateRange(false);
        
        eventParams.setFilterByHidden(false);
        
        return eventParams;
    }
    
    
    public SearchParamsCEEvents getSearchParamsComplianceEvPastMonth(Municipality m){
        EventCoordinator ec = getEventCoordinator();
        
        // event types are always bundled in an EventCategory
        // so in this case of this query, we don't care about the Category title,
        // only the type
        EventCategory complianceEventCategory = ec.getInitializedEventCateogry();
        complianceEventCategory.setEventType(EventType.Compliance);
        
        
        SearchParamsCEEvents eventParams = new SearchParamsCEEvents();
        
        eventParams.setFilterByMuni(true);
        eventParams.setMuni(m);
        eventParams.setFilterByStartEndDate(true);
        eventParams.setUseRelativeDates(true);
        // query from a week ago to now
        eventParams.setStartDateRelativeDays(-30);
        eventParams.setEndDateRelativeDays(0);
        
        eventParams.setFilterByObjectID(false);
        eventParams.setLimitResultCountTo100(true);
        
        eventParams.setFilterByEventCategory(false);
        eventParams.setFilterByEventType(false);
        eventParams.setEvtType(EventType.Compliance);
        
        eventParams.setFilterByCaseID(false);
        
        eventParams.setFilterByEventOwner(false);
        
        eventParams.setFilterByActive(true);
        eventParams.setIsActive(true);
        
        eventParams.setFilterByRequiresViewConfirmation(false);
        
        eventParams.setFilterByViewed(false);
        eventParams.setIsViewed(false);
        
        eventParams.setFilterByPerson(false);
        eventParams.setUseViewConfirmedAtDateRange(false);
        
        eventParams.setFilterByHidden(false);
        
        return eventParams;
    }
    
    
}
