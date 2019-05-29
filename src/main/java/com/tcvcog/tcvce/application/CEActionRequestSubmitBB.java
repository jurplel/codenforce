/*
 * Copyright (C) 2017 cedba
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

import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.BlobTypeException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobType;
import java.util.Date;
import java.io.Serializable;
import org.primefaces.component.tabview.TabView;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
//import com.tcvcog.tcvce.entities.Photograph;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseId;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
/**
 *
 * @author cedba
 */

public class CEActionRequestSubmitBB extends BackingBeanUtils implements Serializable{
    
    // for request lookup
    
    private CEActionRequest currentRequest;
    
    private Person currentPerson;
    private int actionRequestorAssignmentMethod;
    private List<Person> personCandidateList;
    private boolean disabledPersonFormFields;
    private Person skeleton;
    
    private TabView tabView;
    private int currentTabIndex;

    private ArrayList<Property> propList;
    
    private String houseNum;
    private String streetName;
    
    private Map<String, Integer> violationTypeMap;
    private int violationTypeID;
    private String violationTypeName;
    
    private Municipality selectedMuni;
    
    private Property selectedProperty;
    
    private boolean form_atSpecificAddress;
    private String form_nonPropertyLocation;
    
    private String form_requestDescription;
    private boolean form_isUrgent;
    private Date form_dateOfRecord;
    
    private boolean form_anonymous;
    
    private int submittedRequestPACC;
    
    // located address
        
    private java.util.Date currentDate;
    
    private List<Blob> blobList;
    //private Photograph selectedPhoto;

    /**
     * Creates a new instance of ActionRequestBean
     */
    public CEActionRequestSubmitBB(){
    }
    
    @PostConstruct
    public void initBean(){
        CEActionRequest req = getSessionBean().getCeactionRequestForSubmission();
        PropertyIntegrator pi = getPropertyIntegrator();
        User facesUser = getSessionBean().getFacesUser();
        currentRequest = req;
        
        // set date of record to current date
        form_dateOfRecord = java.util.Date.from(java.time.LocalDateTime.now()
                .atZone(ZoneId.systemDefault()).toInstant());
        // init new, empty photo list
        this.blobList = new ArrayList<>();    
    }
    
    
    public void changePropertyPersonsDropDown(){
        
    }
    
    public String assignSelectedRequestorPersonAndContinue(){
        currentRequest.setRequestor(currentPerson);
        getSessionBean().setCeactionRequestForSubmission(currentRequest);
        return "reviewAndSubmit";
    }
   
    
    public String validateActionRequestorNewPersonAndContinue(){
        getSessionBean().setCeactionRequestForSubmission(currentRequest);
        return "reviewAndSubmit";
    }
    
    public int insertActionRequestorNewPerson(Person p){
        PersonIntegrator personIntegrator = getPersonIntegrator();
        
        int insertedPersonID = 0;
        
        try {
            insertedPersonID = personIntegrator.insertPerson(p);
            System.out.println("CEActionReqeustSubmitBB.storeActionRequestorPerson | PersonID " + insertedPersonID);
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "INTEGRATION ERROR: Sorry, the system was unable to store your contact information and as a result, your request has not been recorded.", 
                    "You might call your municipal office to report this error and make a request over the phone. "
                    + "You can also phone the Turtle Creek COG's technical support specialist, Eric Darsow, at 412.840.3020 and leave a message"));
        } catch (NullPointerException ex){
             System.out.println(ex.toString());
        }
        return insertedPersonID;
    } // close storePerson 
    
    /**
     * Entry mechanism to the Code Enforcement Action Request creation process
     * We grab the muni the user selected, set it in the new Action Request
     * and store it in the session bean which we'll access and manipulate
     * over the next few pages and finally submit on the last page. This is
     * a poor person's flow system
     * @return String pointer to the next step in the process: choose property
     */
    public String storeSelectedMuni(){
        CEActionRequest cear;
        CaseCoordinator cc = getCaseCoordinator();
        cear = cc.getInititalizedCEActionRequest();
        cear.setDateOfRecordUtilDate(form_dateOfRecord);
        cear.setMuni(selectedMuni);
        getSessionBean().setCeactionRequestForSubmission(cear);
        return "chooseProperty";
    }
    
    public String storePropertyInfo(){
        if(currentRequest.getRequestProperty() == null){
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                       "Please select a property from the list of search results to continue.", ""));
            return "";
        }
        return "describeConcern";
    }
    
    public String saveConcernDescriptions(){
//        User u = getSessionBean().getFacesUser();
//        if(u == null){
        if(getSessionBean().getCeactionRequestForSubmission().getBlobIDList() == null){
            getSessionBean().getCeactionRequestForSubmission().setBlobIDList(new ArrayList<Integer>());
        }
        getSessionBean().setBlobList(new ArrayList<Blob>());
        
        return "photoUpload";
//            
//        } else {
//            
//            return "reviewAndSubmit";
//        }
        
    }
    
    public String savePhotos(){
        BlobCoordinator blobc = getBlobCoordinator();
        SessionBean sb = getSessionBean();
        sb.getCeactionRequestForSubmission().setBlobIDList(new ArrayList<Integer>());
        
        // before moving onto the person page, get a person's skeleton from the coordinator, put it
        // in the session for use on the next page
        setupPersonEntry();
        return "requestorDetails";
    }
    
    private void setupPersonEntry(){
        UserCoordinator uc = getUserCoordinator();
        PersonCoordinator pc = getPersonCoordinator();
        Municipality m = currentRequest.getMuni();
        Person skel = pc.getNewPersonSkeleton(m);
        try {
            skel.setCreatorUserID(uc.getRobotUser().getUserID());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        skel.setSourceID(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("actionRequestPublicUserPersonSourceID")));
        currentRequest.setRequestor(skel);
        getSessionBean().setCeactionRequestForSubmission(currentRequest);
    }
    
    public void handlePhotoUpload(FileUploadEvent ev){
        if(ev == null){
            System.out.println("CEActionRequestBB.handlePhotoUpload | event: null");
            return;
        }
        int newPhotoID = 0;
        
        // verify blob types here. Post a FacesMessage if file type is not an image
        String fileType = ev.getFile().getContentType();
        System.out.println("CEActionRequestSubmitBB.handlePhotoUpload | File: " + ev.getFile().getFileName() + " Type: " + fileType);
        
        if(!fileType.contains("jpg") && !fileType.contains("gif") && !fileType.contains("png")){
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Incompatible file type. ",
                        "Please upload image files only (jpg, gif, or png)."));
        }
        
        BlobCoordinator blobc = getBlobCoordinator();
        Blob blob = blobc.getNewBlob();  //init new blob
        blob.setBytes(ev.getFile().getContents());  // set bytes  
        blob.setType(BlobType.PHOTO);
        try {
            blob.setBlobID(getBlobIntegrator().storeBlob(blob));
        } catch (BlobException | IntegrationException ex) {
            System.out.println("CEActionRequestSubmitBB.handleFileUpload | " + ex);
        }
        getSessionBean().getCeactionRequestForSubmission().getBlobIDList().add((Integer)blob.getBlobID());
        getSessionBean().getBlobList().add(blob);  // store blob on session bean
    }
    
    
    /**
     * This action method is called when the request code enforcement
     * action request is submitted online (submit button in submitCERequest
     * @return the page ID for navigation
     */
    public String submitActionRequest() {
        
        CEActionRequest req = currentRequest;
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        BlobIntegrator blobI = getBlobIntegrator();
        PersonIntegrator pi = getPersonIntegrator();
        SessionBean sb = getSessionBean();
        BlobIntegrator blobi = getBlobIntegrator();
        
        int submittedActionRequestID;
        int personID;
        // start by pulling the person fields and sending them to be entered
        // into db as a person. The ID of this person is returned, and used in our
        // insertion of the action request as a whole. 
        
        // LT goal: bundle these into a transaction that is rolled back if either 
        // the person or the request bounces
        
        if(currentRequest.getRequestor().getPersonID() == 0){
             personID = insertActionRequestorNewPerson(req.getRequestor());
            try {
                currentRequest.setRequestor(pi.getPerson(personID));
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }
        
        int controlCode = getControlCodeFromTime();
        req.setRequestPublicCC(controlCode);
        
        // all requests now are required to be at a known address
        req.setIsAtKnownAddress(true);
        
//        if (form_atSpecificAddress){
//            req.setRequestProperty(selectedProperty);
//        } else {
//            req.setAddressOfConcern(form_nonPropertyLocation);
//        }
//        
//        req.setIssueType_issueTypeID(violationTypeID);
//        req.setRequestDescription(form_requestDescription);
//        req.setDateOfRecord(form_dateOfRecord
//                .toInstant()
//                .atZone(ZoneId.systemDefault())
//                .toLocalDateTime());
//        req.setIsUrgent(form_isUrgent);
//        
        // note that the time stamp is applied by the integration layer
        // with a simple call to the backing bean getTimeStamp method

        try { 
            // send the request into the DB
            submittedActionRequestID = ceari.submitCEActionRequest(req);
            getSessionBean().setSessionCEAR(ceari.getActionRequestByRequestID(submittedActionRequestID));
            
            // insert photos to db and link to request
            
            for(Blob blob : sb.getBlobList()){
                try {
                    blobi.storeBlob(blob);
                    sb.getCeactionRequestForSubmission().getBlobIDList().add(blob.getBlobID());
                    blobI.linkBlobToActionRequest(blob.getBlobID(), sb.getActiveRequest().getRequestID());
                } catch (BlobException ex) {
                    System.out.println(ex);
                }
            }
                    
            // Now go right back to the DB and get the request we just submitted to verify before displaying the PACC
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO, 
                       "Success! Your request has been submitted and passed to our code enforcement team.", ""));
            return "success";

        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
               getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "INTEGRATION ERROR: Unable write request into the database, our apologies!", 
                            "Please call your municipal office and report your concern by phone."));
            return "";
        }
    }
    
  
    
    public void storePropertyLocationInfo(ActionEvent event){
        
    }
    
    public void storeNoPropertyInfo(ActionEvent event){
        System.out.println("ActionRequestBean.storeNoPropertyInfo | request location: " + form_nonPropertyLocation);
    }
    
    public void incrementalFormContinue(ActionEvent event){
        System.out.println("ActionRequestBean.incrementalFormContinue | tabview: " + currentTabIndex);
    }

      
    public void searchForPropertiesSingleMuni(ActionEvent ev){
        PropertyIntegrator pi = getPropertyIntegrator();
        
        try {
            propList = pi.searchForProperties(houseNum, streetName, getSessionBean().getCeactionRequestForSubmission().getMuni().getMuniCode());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Your search completed with " + getPropList().size() + " results", ""));
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to complete a property search! Sorry!", ""));
            
        }
    }
    
    
    /**
     * @return the form_nonPropertyLocation
     */
    public String getForm_nonPropertyLocation() {
        return form_nonPropertyLocation;
    }

    /**
     * @param form_nonPropertyLocation the form_nonPropertyLocation to set
     */
    public void setForm_nonPropertyLocation(String form_nonPropertyLocation) {
        this.form_nonPropertyLocation = form_nonPropertyLocation;
    }

    /**
     * @return the form_atSpecificAddress
     */
    public boolean isForm_atSpecificAddress() {
        form_atSpecificAddress = true;
        return form_atSpecificAddress;
    }

    /**
     * @param form_atSpecificAddress the form_atSpecificAddress to set
     */
    public void setForm_atSpecificAddress(boolean form_atSpecificAddress) {
        this.form_atSpecificAddress = form_atSpecificAddress;
    }

    /**
     * @return the form_requestDescription
     */
    public String getForm_requestDescription() {
        return form_requestDescription;
    }

    /**
     * @param form_requestDescription the form_requestDescription to set
     */
    public void setForm_requestDescription(String form_requestDescription) {
        this.form_requestDescription = form_requestDescription;
    }

    /**
     * @return the form_isUrgent
     */
    public boolean isForm_isUrgent() {
        return form_isUrgent;
    }

    /**
     * @param form_isUrgent the form_isUrgent to set
     */
    public void setForm_isUrgent(boolean form_isUrgent) {
        this.form_isUrgent = form_isUrgent;
    }

    /**
     * @return the form_dateOfRecord
     */
    public Date getForm_dateOfRecord() {
        return form_dateOfRecord;
    }

    /**
     * @param form_dateOfRecord the form_dateOfRecord to set
     */
    public void setForm_dateOfRecord(Date form_dateOfRecord) {
        this.form_dateOfRecord = form_dateOfRecord;
    }

   

    /**
     * @return the form_anonymous
     */
    public boolean isForm_anonymous() {
        return form_anonymous;
    }

    /**
     * @param form_anonymous the form_anonymous to set
     */
    public void setForm_anonymous(boolean form_anonymous) {
        this.form_anonymous = form_anonymous;
    }

   

    /**
     * @return the violationTypeID
     */
    public int getViolationTypeID() {
        return violationTypeID;
    }

    /**
     * @param violationTypeID the violationTypeID to set
     */
    public void setViolationTypeID(int violationTypeID) {
        this.violationTypeID = violationTypeID;
    }

    /**
     * @return the violationTypeName
     */
    public String getViolationTypeName() {
        return violationTypeName;
    }

    /**
     * @param violationTypeName the violationTypeName to set
     */
    public void setViolationTypeName(String violationTypeName) {
        this.violationTypeName = violationTypeName;
    }

    

    /**
     * @return the currentPerson
     */
    public Person getCurrentPerson() {
        return currentPerson;
    }

    /**
     * @param currentPerson the currentPerson to set
     */
    public void setCurrentPerson(Person currentPerson) {
        this.currentPerson = currentPerson;
    }

   
    

    /**
     * @return the submittingPersonTypes
     */
    public PersonType[] getSubmittingPersonTypes() {
        
        return PersonType.values();
    }

   
    /**
     * @return the selectedProperty
     */
    public Property getSelectedProperty() {
        return selectedProperty;
    }

    /**
     * @param selectedProperty the selectedProperty to set
     */
    public void setSelectedProperty(Property selectedProperty) {
        this.selectedProperty = selectedProperty;
    }

    /**
    
  
    /**
     * @return the propList
     */
    public ArrayList getPropList() {
        return propList;
    }

    /**
     * @return the houseNum
     */
    public String getHouseNum() {
        return houseNum;
    }

    /**
     * @param propList the propList to set
     */
    public void setPropList(ArrayList propList) {
        this.propList = propList;
    }

    /**
     * @param houseNum the houseNum to set
     */
    public void setHouseNum(String houseNum) {
        System.out.println("ActionRequestBean.setHouseNum");
        this.houseNum = houseNum;
    }

    /**
     * @return the tabView
     */
    public TabView getTabView() {
        return tabView;
    }

    /**
     * @param tabView the tabView to set
     */
    public void setTabView(TabView tabView) {
        this.tabView = tabView;
    }

    /**
     * @return the streetName
     */
    public String getStreetName() {
        return streetName;
    }

    /**
     * @param streetName the streetName to set
     */
    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    /**
     * @return the submittedRequestPACC
     */
    public int getSubmittedRequestPACC() {
        return submittedRequestPACC;
    }

    /**
     * @param submittedRequestPACC the submittedRequestPACC to set
     */
    public void setSubmittedRequestPACC(int submittedRequestPACC) {
        this.submittedRequestPACC = submittedRequestPACC;
    }

    /**
     * @return the selectedMuni
     */
    public Municipality getSelectedMuni() {
        return selectedMuni;
    }

    /**
     * @param selectedMuni the selectedMuni to set
     */
    public void setSelectedMuni(Municipality selectedMuni) {
        this.selectedMuni = selectedMuni;
    }

    

   
    /**
     * @return the violationTypeMap
     */
    public Map<String, Integer> getViolationTypeMap() {
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        violationTypeMap = ceari.getViolationMap();
        return violationTypeMap;
    }

    /**
     * @param violationTypeMap the violationTypeMap to set
     */
    public void setViolationTypeMap(Map<String, Integer> violationTypeMap) {
        this.violationTypeMap = violationTypeMap;
    }

    /**
     * @return the currentDate
     */
    public java.util.Date getCurrentDate() {
        currentDate = java.util.Date.from(java.time.LocalDateTime.now()
                .atZone(ZoneId.systemDefault()).toInstant());
        return currentDate;
    }

    /**
     * @param currentDate the currentDate to set
     */
    public void setCurrentDate(java.util.Date currentDate) {
        this.currentDate = currentDate;
    }

    /**
     * @return the blobList
     */
    public List<Blob> getBlobList() {
        return blobList;
    }

    /**
     * @param blobList the blobList to set
     */
    public void setBlobListList(List<Blob> blobList) {
        this.blobList = blobList;
    }

    /**
     * @return the currentRequest
     */
    public CEActionRequest getCurrentRequest() {
        return currentRequest;
    }

    /**
     * @param currentRequest the currentRequest to set
     */
    public void setCurrentRequest(CEActionRequest currentRequest) {
        this.currentRequest = currentRequest;
    }

    /**
     * @return the actionRequestorAssignmentMethod
     */
    public int getActionRequestorAssignmentMethod() {
        return actionRequestorAssignmentMethod;
    }

    /**
     * @param actionRequestorAssignmentMethod the actionRequestorAssignmentMethod to set
     */
    public void setActionRequestorAssignmentMethod(int actionRequestorAssignmentMethod) {
        this.actionRequestorAssignmentMethod = actionRequestorAssignmentMethod;
    }

    /**
     * @return the personCandidateList
     */
    public List<Person> getPersonCandidateList() {
        return personCandidateList;
    }

    /**
     * @param personCandidateList the personCandidateList to set
     */
    public void setPersonCandidateList(List<Person> personCandidateList) {
        this.personCandidateList = personCandidateList;
    }

    /**
     * @return the disabledPersonFormFields
     */
    public boolean isDisabledPersonFormFields() {
        return disabledPersonFormFields;
    }

    /**
     * @param disabledPersonFormFields the disabledPersonFormFields to set
     */
    public void setDisabledPersonFormFields(boolean disabledPersonFormFields) {
        this.disabledPersonFormFields = disabledPersonFormFields;
    }

    /**
     * @return the skeleton
     */
    public Person getSkeleton() {
        return skeleton;
    }

    /**
     * @param skeleton the skeleton to set
     */
    public void setSkeleton(Person skeleton) {
        this.skeleton = skeleton;
    }
}