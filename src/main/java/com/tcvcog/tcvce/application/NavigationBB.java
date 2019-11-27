/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.NavigationItem;
import com.tcvcog.tcvce.entities.NavigationSubItem;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 *
 * @author Eric C. Darsow
 */
public class NavigationBB extends BackingBeanUtils implements Serializable {

    private boolean noActiveUser;
    private boolean noActiveCase;
    private boolean noActiveProperty;
    private boolean noActiveInspection;
    private boolean noActiveSource;
    private boolean noActivePerson;

    /**
     * Creates a new instance of NavigationBB
     */
    public NavigationBB() {
    }

    public String gotoPropertyProfile() {
        if (getSessionBean().getSessionProperty() != null) {
            return "propertyProfile";
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "No active property to profile! Please search for and "
                            + "select a property and re-attempt navigation",
                            ""));
            return "propertySearch";
        }
    }

    public String gotoCaseProfile() {
        if (getSessionBean().getSessionCECase() != null) {
            return "caseProfile";
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "No active case! Please select a case from the list below and re-attempt navigation",
                            ""));
            return "ceCases";
        }

    }

    public String gotoPersonProfile() {
        if (getSessionBean().getSessionPerson() != null) {
            return "personProfile";
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "No active case! Please select a case from the list below and re-attempt navigation",
                            ""));
            return "personSearch";
        }
    }

    /**
     * @return the noActiveCase
     */
    public boolean isNoActiveCase() {
        CECase c = getSessionBean().getSessionCECase();
        noActiveCase = (c == null);
        return noActiveCase;
    }

    /**
     * @return the noActiveProperty
     */
    public boolean isNoActiveProperty() {
        Property p = getSessionBean().getSessionProperty();
        noActiveProperty = (p == null);
        return noActiveProperty;
    }

    /**
     * @return the noActiveInspection
     */
    public boolean isNoActiveInspection() {
        return noActiveInspection;
    }

    /**
     * @param noActiveCase the noActiveCase to set
     */
    public void setNoActiveCase(boolean noActiveCase) {
        this.noActiveCase = noActiveCase;
    }

    /**
     * @param noActiveProperty the noActiveProperty to set
     */
    public void setNoActiveProperty(boolean noActiveProperty) {
        this.noActiveProperty = noActiveProperty;
    }

    /**
     * @param noActiveInspection the noActiveInspection to set
     */
    public void setNoActiveInspection(boolean noActiveInspection) {
        this.noActiveInspection = noActiveInspection;
    }

    /**
     * @return the noActiveSource
     */
    public boolean isNoActiveSource() {
        CodeSource cs = getSessionBean().getActiveCodeSource();
        noActiveSource = (cs == null);
        return noActiveSource;
    }

    /**
     * @param noActiveSource the noActiveSource to set
     */
    public void setNoActiveSource(boolean noActiveSource) {
        this.noActiveSource = noActiveSource;
    }

    /**
     * @return the noActivePerson
     */
    public boolean isNoActivePerson() {
        Person p = getSessionBean().getSessionPerson();
        noActivePerson = (p == null);
        return noActivePerson;
    }

    /**
     * @param noActivePerson the noActivePerson to set
     */
    public void setNoActivePerson(boolean noActivePerson) {
        this.noActivePerson = noActivePerson;
    }

    /**
     * @return the noActiveUser
     */
    public boolean isNoActiveUser() {
        noActiveUser = (getSessionBean().getSessionUser() == null);
        return noActiveUser;
    }

    /**
     * @param noActiveUser the noActiveUser to set
     */
    public void setNoActiveUser(boolean noActiveUser) {
        this.noActiveUser = noActiveUser;
    }

    //xiaohong add
    //Store SubNav Items into List: Dashboard
    public List<NavigationSubItem> getDashboardNavList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        return navList;
    }

    //Nav Bar
    //Sub NavItem: Property
    private final NavigationSubItem propertyUnits = getNavSubItem("Units", "", "fa fa-sign-in", false);
    private final NavigationSubItem propertyEvents = getNavSubItem("Events", "", "fa fa-sign-in", false);
    private final NavigationSubItem propertyPersons = getNavSubItem("Persons", "", "fa fa-sign-in", false);
    private final NavigationSubItem propertyCases = getNavSubItem("Cases", "", "fa fa-sign-in", false);
    private final NavigationSubItem propertyPeriods = getNavSubItem("Periods", "", "fa fa-sign-in", false);
    private final NavigationSubItem propertyDocuments = getNavSubItem("Documents", "", "fa fa-sign-in", false);

    //Store SubNav Items into List: Property
    public List<NavigationSubItem> getPropertyNavList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(propertyUnits);
        navList.add(propertyEvents);
        navList.add(propertyPersons);
        navList.add(propertyCases);
        navList.add(propertyPeriods);
        navList.add(propertyDocuments);
        return navList;
    }

    //Sub NavItem: Code Enf
    private final NavigationSubItem CEViolations = getNavSubItem("Violations", "", "fa fa-sign-in", false);
    private final NavigationSubItem CEEvents = getNavSubItem("Events", "", "fa fa-sign-in", false);
    private final NavigationSubItem CECitations = getNavSubItem("Citations", "", "fa fa-sign-in", false);
    private final NavigationSubItem CENotices = getNavSubItem("Notices", "", "fa fa-sign-in", false);
    private final NavigationSubItem CERequests = getNavSubItem("Requests", "", "fa fa-sign-in", false);
    private final NavigationSubItem CEPayments = getNavSubItem("Payments", "", "fa fa-sign-in", false);

    //Store SubNav Items into List: Code Enf
    public List<NavigationSubItem> getCENavList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(CEViolations);
        navList.add(CEEvents);
        navList.add(CECitations);
        navList.add(CENotices);
        navList.add(CERequests);
        navList.add(CEPayments);
        return navList;
    }

    //Sub NavItem: Occupancy
    private final NavigationSubItem occPeriodStatus = getNavSubItem("Period Status", "", "fa fa-sign-in", true);
    private final NavigationSubItem occPermits = getNavSubItem("Permits", "", "fa fa-sign-in", true);
    private final NavigationSubItem occEvents = getNavSubItem("Events", "", "fa fa-sign-in", false);
    private final NavigationSubItem occInspections = getNavSubItem("Inspections", "", "fa fa-sign-in", false);
    private final NavigationSubItem occDocuments = getNavSubItem("Documents", "", "fa fa-sign-in", false);
    private final NavigationSubItem occPayments = getNavSubItem("Payments", "", "fa fa-sign-in", false);

    //Store SubNav Items into List: Occupancy
    public List<NavigationSubItem> getOccNavList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(occPeriodStatus);
        navList.add(occPermits);
        navList.add(occEvents);
        navList.add(occInspections);
        navList.add(occDocuments);
        navList.add(occPayments);
        return navList;
    }

    //Sub NavItem: Persons
    private final NavigationSubItem personParcels = getNavSubItem("Parcels", "", "fa fa-sign-in", false);
    private final NavigationSubItem personCases = getNavSubItem("Cases", "", "fa fa-sign-in", false);
    private final NavigationSubItem personEvents = getNavSubItem("Events", "", "fa fa-sign-in", false);
    private final NavigationSubItem personDocuments = getNavSubItem("Documents", "", "fa fa-sign-in", false);

    //Store SubNav Items into List: Person
    public List<NavigationSubItem> getPersonNavList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(personParcels);
        navList.add(personCases);
        navList.add(personEvents);
        navList.add(personDocuments);
        return navList;
    }

    //Sub NavItem: Code
    private final NavigationSubItem codeSources = getNavSubItem("Sources", "/restricted/cogstaff/code/codeSourceManage.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem codeDetails = getNavSubItem("Details", "", "fa fa-sign-in", false);

    //Store SubNav Items into List: Code
    public List<NavigationSubItem> getCodeNavList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(codeSources);
        navList.add(codeDetails);
        return navList;
    }

    public List<NavigationItem> navList() {

        ArrayList<NavigationItem> navList;
        navList = new ArrayList<>();
        try {
            //NavItem: Dashboard
            NavigationItem dashboardItem = getNavItem(getCurrentDashBoardInfo(), "/restricted/missionControl.xhtml", "Dashboard", "fa fa-dashboard", getDashboardNavList());
            //NavItem: Property
            NavigationItem propertyItem = getNavItem(getCurrentPropertyInfo(), "/restricted/cogview/properties.xhtml", "Property", "fa fa-home", getPropertyNavList());
            //NavItem: Code Enf
            NavigationItem CEItem = getNavItem(getCurrentCEInfo(), "/restricted/cogview/cECases.xhtml", "Code Enf", "fa fa-balance-scale", getCENavList());
            //NavItem: Occupancy
            NavigationItem occItem = getNavItem(getCurrentPeriodInfo(), "/restricted/cogstaff/occ/inspection.xhtml", "Occupancy", "fa fa-list-alt", getOccNavList());
            //NavItem: Persons
            NavigationItem personItem = getNavItem(getCurrentPersonInfo(), "/restricted/cogview/persons.xhtml", "Person", "fa fa-female", getPersonNavList());
            //NavItem: Code
            NavigationItem codeItem = getNavItem("Current Code: ", "/restricted/cogstaff/code/codeSourceManage.xhtml", "Code", "fa fa-book", getCodeNavList());

            navList.add(dashboardItem);
            navList.add(propertyItem);
            navList.add(CEItem);
            navList.add(occItem);
            navList.add(codeItem);
            navList.add(personItem);
        } catch (Exception e) {

        }
        return navList;

    }

    //Side Tool Bar
    //Sidebar Sub Nav Item: Municipal Code
    private final NavigationSubItem codeSource = getNavSubItem("Code Sources", "/restricted/cogstaff/code/codeSourceManage.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem codeBook = getNavSubItem("Code Book", "/restricted/cogstaff/code/codeSetManage.xhtml", "fa fa-sign-in", false);

    //Store SubNav Items into List:Code
    public List<NavigationSubItem> getSidebarCodeConfigList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(codeSource);
        navList.add(codeBook);
        return navList;
    }

    //Sidebar Sub Nav Item: CE
    private final NavigationSubItem caseEvent = getNavSubItem("Case Event", "/restricted/cogstaff/ce/eventConfiguration.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem courtEntity = getNavSubItem("Court Entity", "/restricted/cogstaff/ce/courtEntityManage.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem notice = getNavSubItem("Notice", "/restricted/cogstaff/ce/textBlockManage.xhtml", "fa fa-sign-in", false);

    //Store SubNav Items into List:Code
    public List<NavigationSubItem> getSidebarCEConfigList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(caseEvent);
        navList.add(courtEntity);
        navList.add(notice);
        return navList;
    }

    //Sidebar Sub Nav Item: Occ
    private final NavigationSubItem checklist = getNavSubItem("Checklist", "/restricted/cogstaff/occ/checklists.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem payment = getNavSubItem("Payment", "/restricted/cogstaff/occ/checklists.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem feeType = getNavSubItem("Fee Type", "/restricted/cogstaff/occ/paymentManage.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem permitType = getNavSubItem("Permit Type", "/restricted/cogstaff/occ/occPermitTypeManage.xhtml", "fa fa-sign-in", false);

    //Store SubNav Items into List:Code
    public List<NavigationSubItem> getSidebarOccConfigList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(checklist);
        navList.add(payment);
        navList.add(feeType);
        navList.add(permitType);
        return navList;
    }

    //Sidebar Sub Nav Item: Reports
    private final NavigationSubItem municipalityActivity = getNavSubItem("Municipality Activity", "", "fa fa-sign-in", false);
    private final NavigationSubItem activeCases = getNavSubItem("Active Cases", "", "fa fa-sign-in", false);

    //Store SubNav Items into List: Reports
    public List<NavigationSubItem> getSidebarReportList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(municipalityActivity);
        navList.add(activeCases);
        return navList;
    }

    //Sidebar Sub Nav Item: System
    private final NavigationSubItem users = getNavSubItem("Users", "/restricted/cogadmin/userAuthMuniManage.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem icons = getNavSubItem("Icons", "/restricted/cogadmin/iconManage.xhtml", "fa fa-sign-in", false);
    private final NavigationSubItem tickets = getNavSubItem("Tickets", "", "fa fa-sign-in", false);

    //Store SubNav Items into List: Reports
    public List<NavigationSubItem> getSidebarSystemList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(users);
        navList.add(icons);
        navList.add(tickets);
        return navList;
    }

    //Sidebar Sub Nav Item: Help
    private final NavigationSubItem howto = getNavSubItem("How-To", "/public/system/documentation/howtos/howtos.xhtml", "fa fa-sign-in", false);

    //Store SubNav Items into List: Help
    public List<NavigationSubItem> getSidebarHelpList() {
        ArrayList<NavigationSubItem> navList;
        navList = new ArrayList<>();
        navList.add(howto);
        return navList;
    }

    public List<NavigationItem> sideBarNavList() {

        ArrayList<NavigationItem> navList;
        navList = new ArrayList<>();
        try {
            //NavItem: CE
            NavigationItem CEconfigItem = getNavItem("", "", "Code Enforcement", "fa fa-balance-scale", getSidebarCEConfigList());
            //NavItem: CE
            NavigationItem OccconfigItem = getNavItem("", "", "Occupancy", "fa fa-list-alt", getSidebarOccConfigList());
            //NavItem: Code
            NavigationItem codeconfigItem = getNavItem("", "", "Municipal Code", "fa fa-book", getSidebarCodeConfigList());
            //NavItem: System
            NavigationItem reportItem = getNavItem("", "", "Report", "fa fa-bullhorn", getSidebarReportList());
            //NavItem: Reports
            NavigationItem systemItem = getNavItem("", "", "System", "fa fa-cogs", getSidebarSystemList());
            //NavItem: Help
            NavigationItem helpItem = getNavItem("", "", "Help", "fa fa-question-circle", getSidebarHelpList());

            navList.add(CEconfigItem);
            navList.add(OccconfigItem);
            navList.add(codeconfigItem);
            navList.add(reportItem);
            navList.add(systemItem);
            navList.add(helpItem);

        } catch (Exception e) {

        }
        return navList;

    }

    public NavigationSubItem getNavSubItem(String value, String path, String icon, boolean disable) {
        NavigationSubItem mn = new NavigationSubItem();
        mn.setValue(value);
        mn.setPagePath(path);
        mn.setIcon(icon);
        mn.setDisable(disable);
        return mn;
    }

    public NavigationItem getNavItem(String currentInfo, String searchPageUrl, String value, String icon, List navSubList) {
        NavigationItem ni = new NavigationItem();
        ni.setValue(value);
        ni.setIcon(icon);
        ni.setSubNavitem(navSubList);
        ni.setSearchpageurl(searchPageUrl);
        ni.setCurrentInfo(currentInfo);
        return ni;
    }

    public Map<String, String> navCategoryMap() {

        HashMap<String, String> categoryMap;
        categoryMap = new HashMap<>();

        List navList = navList();
        for (int i = 0; i < navList.size(); i++) {
            NavigationItem navitem = (NavigationItem) navList.get(i);
            List subnavList = navitem.getSubNavitem();
            String categoryName = navitem.getValue();
            for (int m = 0; m < subnavList.size(); m++) {
                NavigationSubItem subnavitem = (NavigationSubItem) subnavList.get(m);
                String pagePath = subnavitem.getPagePath();
                categoryMap.put(pagePath, categoryName);
            }
        }
        return categoryMap;
    }

    public String getCurrentPageNavItemValue() {
        String currentViewPagePath = getviewPagePath();
        return navCategoryMap().get(currentViewPagePath);
    }

    public String getviewPagePath() {
        FacesContext fc = FacesContext.getCurrentInstance();
        String viewID = fc.getViewRoot().getViewId();
        return viewID;
    }

    public String getCurrentDashBoardInfo() {
        SessionBean s = getSessionBean();
        try {
            String info = s.getSessionMuni().getMuniName();
            return "Current Municipality: " + info;
        } catch (Exception ex) {
            return "Current Municipality: ";
        }

    }

    public String getCurrentPropertyInfo() {
        SessionBean s = getSessionBean();
        try {
            String propertyAddress = s.getSessionProperty().getAddress();
            String propertyId = String.valueOf(s.getSessionProperty().getPropertyID());
            return "Current Property: " + propertyAddress + " | ID: " + propertyId;
        } catch (Exception ex) {
            return "Current Property: " + " | ID: ";
        }

    }

    public String getCurrentCEInfo() {
        SessionBean s = getSessionBean();
        try {
            String caseName = s.getSessionCECase().getCaseName();
            String caseId = String.valueOf(s.getSessionCECase().getCaseID());
            return "Current Case: " + caseName + " | ID: " + caseId;
        } catch (Exception ex) {
            return "Current Case: " + " | ID: ";
        }

    }

    public String getCurrentPersonInfo() {
        SessionBean s = getSessionBean();
        try {
            String personName = s.getSessionPerson().getFirstName();
            String personId = String.valueOf(s.getSessionPerson().getPersonID());
            return "Current Person: " + personName + " | ID: " + personId;
        } catch (Exception ex) {
            return "Current Person: " + " | ID: ";
        }

    }

    public String getCurrentPeriodInfo() {
        SessionBean s = getSessionBean();
        try {
            String periodId = String.valueOf(s.getSessionOccPeriod().getPeriodID());
            String periodType = s.getSessionOccPeriod().getType().getTitle();
            return "Current Person: " + periodType + " | ID: " + periodId;
        } catch (Exception ex) {
            return "Current Person: " + " | ID: ";
        }

    }

}
