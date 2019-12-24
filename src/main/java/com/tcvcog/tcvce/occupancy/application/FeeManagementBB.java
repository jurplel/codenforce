/*
 * Copyright (C) 2018 Adam Gutonski
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
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Fee;
import com.tcvcog.tcvce.entities.FeeAssigned;
import com.tcvcog.tcvce.entities.MoneyCECaseFeeAssigned;
import com.tcvcog.tcvce.entities.MoneyOccPeriodFeeAssigned;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import com.tcvcog.tcvce.occupancy.integration.PaymentIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.*;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Adam Gutonski
 */
@ViewScoped
public class FeeManagementBB extends BackingBeanUtils implements Serializable {

    //feeTypeManage.xhtml fields
    private ArrayList<Fee> existingFeeTypeList;
    private Fee selectedFeeType;
    private Municipality formMuni;

    //feeManage.xhtml fields
    private OccPeriod currentOccPeriod;
    private CECase currentCase;
    private Fee formFee;
    private Fee selectedFee;
    private ArrayList<Fee> feeList;
    private ArrayList<Fee> filteredFeeList;
    private ArrayList<CodeViolation> violationList;
    private CodeViolation selectedViolation;

    private FeeAssigned assignedFormFee;

    private FeeAssigned selectedAssignedFee;
    private ArrayList<FeeAssigned> feeAssignedList;
    private ArrayList<FeeAssigned> filteredFeeAssignedList;

    //feePermissions.xhtml fields
    private ArrayList<OccPeriodType> typeList;
    private ArrayList<OccPeriodType> filteredTypeList;
    private OccPeriodType selectedPeriodType;
    private OccPeriodType lockedPeriodType;
    private List<Fee> existingFeeList;
    private ArrayList<Fee> workingFeeList;
    private Fee selectedWorkingFee;
    private List<Fee> allFees;

    private EventDomainEnum currentDomain;
    private boolean editing;
    private String redirTo;
    private boolean waived;

    /**
     * Creates a new instance of NewJSFManagedBean
     */
    public FeeManagementBB() {
    }

    @PostConstruct
    public void initBean() {
        formFee = new Fee();
        formFee.setEffectiveDate(LocalDateTime.now());
        formFee.setExpiryDate(LocalDateTime.now());

        PaymentIntegrator pi = getPaymentIntegrator();

        assignedFormFee = new FeeAssigned();

        if (getSessionBean().getFeeRedirTo() != null) {

            refreshFeeAssignedList();

            if (allFees == null) {
                try {
                    allFees = pi.getFeeTypeList(getSessionBean().getSessionMuni());
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Oops! We encountered a problem trying to fetch the fee list!", ""));
                }
            }

            if (feeList == null) {
                feeList = (ArrayList<Fee>) allFees;
            }

            if (typeList == null) {
                OccupancyIntegrator oi = getOccupancyIntegrator();
                try {
                    typeList = (ArrayList<OccPeriodType>) oi.getOccPeriodTypeList(getSessionBean().getSessionMuni().getProfile().getProfileID());
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Oops! We encountered a problem trying to fetch the OccPeriodType List!", ""));
                }
            }

        }
    }

    public String finishAndRedir() {
        getSessionBean().setFeeRedirTo(null);

        return redirTo;
    }

    public String goToFeePermissions() {
        return "feePermissions";
    }

    public String deleteSelectedOccPeriodFee(ActionEvent e) {

        PaymentIntegrator pi = getPaymentIntegrator();

        if (selectedAssignedFee != null) {
            try {
                pi.deleteOccPeriodFee((MoneyOccPeriodFeeAssigned) selectedAssignedFee);
                refreshFeeAssignedList();
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Fee deleted forever!", ""));
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
            }

        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a fee from the table to delete", ""));
        }
        return "";
    }

    public void editSelectedAssignedFee(ActionEvent e) {

        if (currentDomain == EventDomainEnum.OCCUPANCY) {

            MoneyOccPeriodFeeAssigned skeleton = (MoneyOccPeriodFeeAssigned) selectedAssignedFee;

            if (selectedAssignedFee != null) {
                skeleton.setOccPeriodID(currentOccPeriod.getPeriodID());
                assignedFormFee = skeleton;
                editing = true;
            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Please select an assigned fee to update", ""));
            }
        } else {

            MoneyCECaseFeeAssigned skeleton = (MoneyCECaseFeeAssigned) selectedAssignedFee;

            if (selectedAssignedFee != null) {
                skeleton.setCaseID(currentCase.getCaseID());
                assignedFormFee = skeleton;
                editing = true;
            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Please select an assigned fee to update", ""));
            }

        }

    }

    public void intializeNewAssignedFee(ActionEvent e) {

        editing = false;
        assignedFormFee = new FeeAssigned();

    }

    public String whatDomainFees() {
        if (currentDomain == EventDomainEnum.OCCUPANCY) {

            return "occupancy periods";

        } else {

            return "code enforcement elements";
        }

    }

    public String addAssignedFee() {
        FeeAssigned firstSkeleton = new FeeAssigned();
        PaymentIntegrator pi = getPaymentIntegrator();

        firstSkeleton.setPaymentList(assignedFormFee.getPaymentList());
        firstSkeleton.setMoneyFeeAssigned(assignedFormFee.getMoneyFeeAssigned());
        firstSkeleton.setAssignedBy(getSessionBean().getSessionUser());
        firstSkeleton.setAssigned(LocalDateTime.now());
        firstSkeleton.setLastModified(LocalDateTime.now());
        firstSkeleton.setNotes(assignedFormFee.getNotes());
        firstSkeleton.setFee(assignedFormFee.getFee());

        if (waived == true) {
            firstSkeleton.setWaivedBy(getSessionBean().getSessionUser());
        } else {
            firstSkeleton.setWaivedBy(new User());
        }

        if (assignedFormFee.getReducedBy() != 0) {

            firstSkeleton.setReducedBy(assignedFormFee.getReducedBy());
            firstSkeleton.setReducedByUser(getSessionBean().getSessionUser());

        } else {
            firstSkeleton.setReducedByUser(new User());
        }

        if (currentDomain == EventDomainEnum.OCCUPANCY) {
            MoneyOccPeriodFeeAssigned secondSkeleton = new MoneyOccPeriodFeeAssigned(firstSkeleton);
            MoneyOccPeriodFeeAssigned occPeriodFormFee = (MoneyOccPeriodFeeAssigned) assignedFormFee;

            secondSkeleton.setOccPerAssignedFeeID(occPeriodFormFee.getOccPerAssignedFeeID());
            secondSkeleton.setOccPeriodID(currentOccPeriod.getPeriodID());
            secondSkeleton.setOccPeriodTypeID(currentOccPeriod.getType().getTypeID());

            try {
                pi.insertOccPeriodFee(secondSkeleton);
                refreshFeeAssignedList();
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to add fee to database, sorry!", "Check server print out..."));
            }

        } else {
            
            MoneyCECaseFeeAssigned secondSkeleton = new MoneyCECaseFeeAssigned(firstSkeleton);
            MoneyCECaseFeeAssigned caseFormFee = (MoneyCECaseFeeAssigned) assignedFormFee;

            secondSkeleton.setCeCaseAssignedFeeID(caseFormFee.getCeCaseAssignedFeeID());
            secondSkeleton.setCaseID(currentCase.getCaseID());
            secondSkeleton.setCodeSetElement(selectedViolation.getCodeViolated().getCodeSetElementID());

            try {
                pi.insertCECaseFee(secondSkeleton);
                refreshFeeAssignedList();
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to add fee to database, sorry!", "Check server print out..."));
            }
            
        }

        return "";

    }

    public String commitOccPeriodFeeUpdates(ActionEvent e) {
        FeeAssigned firstSkeleton = new FeeAssigned();
        PaymentIntegrator pi = getPaymentIntegrator();

        firstSkeleton.setAssignedFeeID(assignedFormFee.getAssignedFeeID());
        firstSkeleton.setPaymentList(assignedFormFee.getPaymentList());
        firstSkeleton.setMoneyFeeAssigned(assignedFormFee.getMoneyFeeAssigned());
        firstSkeleton.setAssignedBy(getSessionBean().getSessionUser());
        firstSkeleton.setAssigned(LocalDateTime.now());
        firstSkeleton.setLastModified(LocalDateTime.now());
        firstSkeleton.setNotes(assignedFormFee.getNotes());
        firstSkeleton.setFee(assignedFormFee.getFee());

        if (waived == true) {
            firstSkeleton.setWaivedBy(getSessionBean().getSessionUser());
        } else {
            firstSkeleton.setWaivedBy(new User());
        }

        if (assignedFormFee.getReducedBy() != 0) {

            firstSkeleton.setReducedBy(assignedFormFee.getReducedBy());
            firstSkeleton.setReducedByUser(getSessionBean().getSessionUser());

        } else {
            firstSkeleton.setReducedByUser(new User());
        }

        if (currentDomain == EventDomainEnum.OCCUPANCY) {
            MoneyOccPeriodFeeAssigned secondSkeleton = new MoneyOccPeriodFeeAssigned(firstSkeleton);
            MoneyOccPeriodFeeAssigned occPeriodFormFee = (MoneyOccPeriodFeeAssigned) assignedFormFee;

            secondSkeleton.setOccPerAssignedFeeID(occPeriodFormFee.getOccPerAssignedFeeID());
            secondSkeleton.setOccPeriodID(currentOccPeriod.getPeriodID());
            secondSkeleton.setOccPeriodTypeID(currentOccPeriod.getType().getTypeID());

            try {
                pi.updateOccPeriodFee(secondSkeleton);
                refreshFeeAssignedList();
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to update fee in database, sorry!", "Check server print out..."));
            }

        } else {
            
            MoneyCECaseFeeAssigned secondSkeleton = new MoneyCECaseFeeAssigned(firstSkeleton);
            MoneyCECaseFeeAssigned caseFormFee = (MoneyCECaseFeeAssigned) assignedFormFee;

            secondSkeleton.setCeCaseAssignedFeeID(caseFormFee.getCeCaseAssignedFeeID());
            secondSkeleton.setCaseID(currentCase.getCaseID());
            secondSkeleton.setCodeSetElement(selectedViolation.getCodeViolated().getCodeSetElementID());

            try {
                pi.updateCECaseFee(secondSkeleton);
                refreshFeeAssignedList();
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to update fee in database, sorry!", "Check server print out..."));
            }
            
        }

        return "";
    }

    public void editFeeType(ActionEvent e) {
        if (getSelectedFeeType() != null) {
            editing = true;
            formFee.setOccupancyInspectionFeeID(selectedFeeType.getOccupancyInspectionFeeID());
            formFee.setMuni(selectedFeeType.getMuni());
            formFee.setName(selectedFeeType.getName());
            formFee.setAmount(selectedFeeType.getAmount());
            formFee.setNotes(selectedFeeType.getNotes());
            /*
            Have to figure out what to do w/ setting dates...
            setFormOccupancyInspectionFeeEffDate(formFeeEffDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
             */
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select an occupancy inspection fee to update", ""));
        }
    }

    public void commitFeeUpdates(ActionEvent e) {
        OccupancyIntegrator oifi = getOccupancyIntegrator();
        PaymentIntegrator pi = getPaymentIntegrator();
        Fee oif = selectedFeeType;

        oif.setMuni(formFee.getMuni());
        oif.setName(formFee.getName());
        oif.setAmount(formFee.getAmount());
        oif.setEffectiveDate(formFee.getEffectiveDate());
        oif.setExpiryDate(formFee.getExpiryDate());
        oif.setNotes(formFee.getNotes());
        try {
            pi.updateOccupancyInspectionFee(oif);
            editing = false;
        } catch (IntegrationException ex) {
        }
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Occupancy Inspection Fee updated!", ""));
    }

    public void initializeNewFee(ActionEvent e) {

        editing = false;
        formFee = new Fee();
        formFee.setEffectiveDate(LocalDateTime.now());
        formFee.setExpiryDate(LocalDateTime.now());
    }

    public String saveNewFeeType() {
        PaymentIntegrator pi = getPaymentIntegrator();
        Fee oif = new Fee();
        oif.setOccupancyInspectionFeeID(formFee.getOccupancyInspectionFeeID());
        oif.setMuni(getFormMuni());
        oif.setName(formFee.getName());
        oif.setAmount(formFee.getAmount());
        oif.setEffectiveDate(formFee.getEffectiveDate());
        oif.setExpiryDate(formFee.getExpiryDate());
        oif.setNotes(formFee.getNotes());
        try {
            pi.insertOccupancyInspectionFee(oif);
        } catch (IntegrationException ex) {
        }
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Successfully added occupancy inspection fee to database!", ""));

        return "occupancyInspectionFeeManage";

    }

    public void deleteSelectedFee(ActionEvent e) {
        PaymentIntegrator pi = getPaymentIntegrator();

        if (getSelectedFeeType() != null) {
            try {
                pi.deleteOccupancyInspectionFee(getSelectedFeeType());
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
            }
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Occupancy inspection fee deleted forever!", ""));

        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select an occupancy inspection fee from the table to delete", ""));
        }
    }

    /**
     * @return the existingFeeTypeList
     */
    public ArrayList<Fee> getFeeTypeList() {
        PaymentIntegrator pi = getPaymentIntegrator();
        try {
            existingFeeTypeList = pi.getOccupancyInspectionFeeList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        if (existingFeeTypeList != null) {
            return existingFeeTypeList;
        } else {
            existingFeeTypeList = new ArrayList();
            return existingFeeTypeList;
        }
    }

    public void editOccPeriodFees() {

        try {
            lockedPeriodType = (OccPeriodType) selectedPeriodType.clone();
        } catch (CloneNotSupportedException ex) {
            System.out.println("OccPeriodType had a problem cloning. Oops!");
        }

        try {
            existingFeeList = lockedPeriodType.getPermittedFees();
            workingFeeList = new ArrayList<>(existingFeeList);
        } catch (NullPointerException e) {
            System.out.println("OccPeriodType has no existing permitted fee list, making new ArrayList...");
            workingFeeList = new ArrayList<>();
        }

    }

    public void removeOccPeriodFee(Fee selectedFee) {
        workingFeeList.remove(selectedFee);
    }

    public void addFeeToPermittedFees() {

        Iterator itr = workingFeeList.iterator();
        boolean duplicate = false;
        Fee test = null;
        while (itr.hasNext()) {
            test = (Fee) itr.next();
            duplicate = test.getOccupancyInspectionFeeID() == selectedFee.getOccupancyInspectionFeeID();
            if (duplicate) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "You cannot permit the same fee twice.", ""));
                break;
            }

        }
        if (!duplicate) {
            workingFeeList.add(selectedFee);
        }
    }

    public void commitPermissionUpdates() {

        PaymentIntegrator pi = getPaymentIntegrator();

        boolean failed = false;

        if (existingFeeList == null) {

            for (Fee workingFee : workingFeeList) {

                failed = false;

                try {
                    pi.insertFeePeriodTypeJoin(workingFee, lockedPeriodType);
                } catch (IntegrationException ex) {
                    System.out.println("Failed inserting occperiod fee join, trying to reactivate.");
                    failed = true;
                }

                if (failed) {

                    try {
                        pi.reactivateFeePeriodTypeJoin(workingFee, lockedPeriodType);
                    } catch (IntegrationException ex) {
                        System.out.println("FeeManagementBB.commitPermissionUpdates() | Error: " + ex.toString());
                    }

                }

            }

        } else {

            int notThisFee = 0;

            for (Fee existingFee : existingFeeList) {
                notThisFee = 0;
                for (Fee workingFee : workingFeeList) {
                    if (existingFee.getOccupancyInspectionFeeID() != workingFee.getOccupancyInspectionFeeID()) {
                        notThisFee++;
                    }

                }

                if (notThisFee == workingFeeList.size()) {

                    try {
                        pi.deactivateFeePeriodTypeJoin(existingFee, lockedPeriodType);
                    } catch (IntegrationException ex) {
                        System.out.println("FeeManagementBB.commitPermissionUpdates() | Error: " + ex.toString());
                    }

                }

            }

            for (Fee workingFee : workingFeeList) {
                notThisFee = 0;
                failed = false;
                for (Fee existingFee : existingFeeList) {

                    if (existingFee.getOccupancyInspectionFeeID() == workingFee.getOccupancyInspectionFeeID()) {
                        try {
                            pi.updateFeePeriodTypeJoin(workingFee, lockedPeriodType);
                        } catch (IntegrationException ex) {
                            System.out.println("FeeManagementBB.commitPermissionUpdates() | Error: " + ex.toString());
                        }
                        break;
                    } else {
                        notThisFee++;
                    }

                }

                if (notThisFee == existingFeeList.size()) {
                    try {
                        pi.insertFeePeriodTypeJoin(workingFee, lockedPeriodType);
                    } catch (IntegrationException ex) {
                        System.out.println("Failed inserting occperiod fee join, trying to reactivate.");
                        failed = true;
                    }

                }

                if (failed) {

                    try {
                        pi.reactivateFeePeriodTypeJoin(workingFee, lockedPeriodType);
                    } catch (IntegrationException ex) {
                        System.out.println("FeeManagementBB.commitPermissionUpdates() | Error: " + ex.toString());
                    }

                }

            }
        }
        OccupancyIntegrator oi = getOccupancyIntegrator();
        try {
            typeList = (ArrayList<OccPeriodType>) oi.getOccPeriodTypeList(getSessionBean().getSessionMuni().getProfile().getProfileID());
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Oops! We encountered a problem trying to fetch the OccPeriodType List!", ""));
        }
    }

    public void refreshFeeAssignedList() {

        feeAssignedList = new ArrayList<>();

        PaymentIntegrator pi = getPaymentIntegrator();

        redirTo = getSessionBean().getFeeRedirTo();

        currentDomain = getSessionBean().getFeeManagementDomain();

        if (currentDomain.equals(EventDomainEnum.OCCUPANCY)) {

            currentOccPeriod = getSessionBean().getFeeManagementOccPeriod();

            if (currentOccPeriod != null) {

                try {
                    feeAssignedList.addAll(pi.getFeeAssigned(currentOccPeriod));
                    feeList = (ArrayList<Fee>) currentOccPeriod.getType().getPermittedFees();
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Oops! We encountered a problem trying to refresh the fee assigned list!", ""));
                }

            }

        } else if (currentDomain.equals(EventDomainEnum.CODE_ENFORCEMENT)) {

            currentCase = getSessionBean().getFeeManagementCeCase();

            if (currentCase != null) {

                try {
                    feeAssignedList.addAll(pi.getFeeAssigned(currentCase));
//                    feeList = (ArrayList<Fee>) currentCase.getType().getPermittedFees(); pop this back in when we find out how to get permitted fees
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Oops! We encountered a problem trying to refresh the fee assigned list!", ""));
                }

            }

        }

    }

    /**
     * @param existingFeeTypeList the existingFeeTypeList to set
     */
    public void setExistingFeeTypeList(ArrayList<Fee> existingFeeTypeList) {
        this.existingFeeTypeList = existingFeeTypeList;
    }

    /**
     * @return the selectedFeeType
     */
    public Fee getSelectedFeeType() {
        return selectedFeeType;
    }

    /**
     * @param selectedFeeType the selectedFeeType to set
     */
    public void setSelectedFeeType(Fee selectedFeeType) {
        this.selectedFeeType = selectedFeeType;
    }

    /**
     * @return the formFeeID
     */
    public int getFormFeeID() {
        return formFee.getOccupancyInspectionFeeID();
    }

    /**
     * @param formFeeID the formFeeID to set
     */
    public void setFormFeeID(int formFeeID) {
        this.formFee.setOccupancyInspectionFeeID(formFeeID);
    }

    /**
     * @return the formMuni
     */
    public Municipality getFormMuni() {
        return formMuni;
    }

    /**
     * @param formMuni the formMuni to set
     */
    public void setFormMuni(Municipality formMuni) {
        this.formMuni = formMuni;
    }

    /**
     * @return the formFeeName
     */
    public String getFormFeeName() {
        return formFee.getName();
    }

    /**
     * @param formFeeName the formFeeName to set
     */
    public void setFormFeeName(String formFeeName) {
        this.formFee.setName(formFeeName);
    }

    /**
     * @return the formFeeAmount
     */
    public double getFormFeeAmount() {
        return formFee.getAmount();
    }

    /**
     * @param formFeeAmount the formFeeAmount to set
     */
    public void setFormFeeAmount(double formFeeAmount) {
        this.formFee.setAmount(formFeeAmount);
    }

    /**
     * @return the formFeeEffDate
     */
    public java.util.Date getFormFeeEffDate() {
        return java.util.Date.from(formFee.getEffectiveDate()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    /**
     * @param formFeeEffDate the formFeeEffDate to set
     */
    public void setFormFeeEffDate(java.util.Date formFeeEffDate) {
        this.formFee.setEffectiveDate(
                LocalDateTime.ofInstant(formFeeEffDate.toInstant(),
                        ZoneId.systemDefault()));
    }

    /**
     * @return the formFeeExpDate
     */
    public java.util.Date getFormFeeExpDate() {
        return java.util.Date.from(formFee.getExpiryDate()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    /**
     * @param formFeeExpDate the formFeeExpDate to set
     */
    public void setFormFeeExpDate(java.util.Date formFeeExpDate) {
        this.formFee.setExpiryDate(
                LocalDateTime.ofInstant(formFeeExpDate.toInstant(),
                        ZoneId.systemDefault()));
    }

    /**
     * @return the formFeeNotes
     */
    public String getFormFeeNotes() {
        return formFee.getNotes();
    }

    /**
     * @param formFeeNotes the formFeeNotes to set
     */
    public void setFormFeeNotes(String formFeeNotes) {
        this.formFee.setNotes(formFeeNotes);
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public Fee getFormFee() {
        return formFee;
    }

    public void setFormFee(Fee formFee) {
        this.formFee = formFee;
    }

    public Fee getSelectedFee() {
        return selectedFee;
    }

    public void setSelectedFee(Fee selectedFee) {
        this.selectedFee = selectedFee;
    }

    public ArrayList<Fee> getFeeList() {
        return feeList;
    }

    public void setFeeList(ArrayList<Fee> feeList) {
        this.feeList = feeList;
    }

    public ArrayList<CodeViolation> getViolationList() {
        return violationList;
    }

    public void setViolationList(ArrayList<CodeViolation> violationList) {
        this.violationList = violationList;
    }

    public FeeAssigned getSelectedAssignedFee() {
        return selectedAssignedFee;
    }

    public void setSelectedAssignedFee(FeeAssigned selectedAssignedFee) {
        this.selectedAssignedFee = selectedAssignedFee;
    }

    public ArrayList<FeeAssigned> getFeeAssignedList() {
        return feeAssignedList;
    }

    public void setFeeAssignedList(ArrayList<FeeAssigned> feeAssignedList) {
        this.feeAssignedList = feeAssignedList;
    }

    public String getRedirTo() {
        return redirTo;
    }

    public void setRedirTo(String redirTo) {
        this.redirTo = redirTo;
    }

    public ArrayList<FeeAssigned> getFilteredFeeAssignedList() {
        return filteredFeeAssignedList;
    }

    public void setFilteredFeeAssignedList(ArrayList<FeeAssigned> filteredFeeAssignedList) {
        this.filteredFeeAssignedList = filteredFeeAssignedList;
    }

    public boolean isWaived() {
        return waived;
    }

    public void setWaived(boolean waived) {
        this.waived = waived;
    }

    public ArrayList<Fee> getFilteredFeeList() {
        return filteredFeeList;
    }

    public void setFilteredFeeList(ArrayList<Fee> filteredFeeList) {
        this.filteredFeeList = filteredFeeList;
    }

    public OccPeriod getCurrentOccPeriod() {
        return currentOccPeriod;
    }

    public void setCurrentOccPeriod(OccPeriod currentOccPeriod) {
        this.currentOccPeriod = currentOccPeriod;
    }

    public boolean editingOccPeriod() {
        return (redirTo != null && currentOccPeriod != null && currentDomain == EventDomainEnum.OCCUPANCY);
    }

    public boolean editingCECase() {
        return (redirTo != null && currentCase != null && currentDomain == EventDomainEnum.CODE_ENFORCEMENT);
    }

    public ArrayList<OccPeriodType> getTypeList() {
        return typeList;
    }

    public void setTypeList(ArrayList<OccPeriodType> typeList) {
        this.typeList = typeList;
    }

    public OccPeriodType getSelectedPeriodType() {
        return selectedPeriodType;
    }

    public void setSelectedPeriodType(OccPeriodType selectedPeriodType) {
        this.selectedPeriodType = selectedPeriodType;
    }

    public ArrayList<OccPeriodType> getFilteredTypeList() {
        return filteredTypeList;
    }

    public void setFilteredTypeList(ArrayList<OccPeriodType> filteredTypeList) {
        this.filteredTypeList = filteredTypeList;
    }

    public List<Fee> getExistingFeeList() {
        return existingFeeList;
    }

    public void setExistingFeeList(List<Fee> existingFeeList) {
        this.existingFeeList = existingFeeList;
    }

    public ArrayList<Fee> getWorkingFeeList() {
        return workingFeeList;
    }

    public void setWorkingFeeList(ArrayList<Fee> workingFeeList) {
        this.workingFeeList = workingFeeList;
    }

    public Fee getSelectedWorkingFee() {
        return selectedWorkingFee;
    }

    public void setSelectedWorkingFee(Fee selectedWorkingFee) {
        this.selectedWorkingFee = selectedWorkingFee;
    }

    public List<Fee> getAllFees() {
        return allFees;
    }

    public void setAllFees(List<Fee> allFees) {
        this.allFees = allFees;
    }

    public Property getOccPeriodProperty() {

        PropertyIntegrator pi = getPropertyIntegrator();

        PropertyUnit unit;
        Property prop = new Property();
        try {
            unit = pi.getPropertyUnitByPropertyUnitID(currentOccPeriod.getPropertyUnitID());
            prop = pi.getProperty(unit.getPropertyID());
        } catch (IntegrationException ex) {
            System.out.println("PaymentBB had problems getting the OccPeriodProperty");
        }

        return prop;

    }

    public String getOccPeriodAddress() {

        return getOccPeriodProperty().getAddress();

    }

    public String getCECaseAddress() {

        return currentCase.getProperty().getAddress();
    }

    public OccPeriodType getLockedPeriodType() {
        return lockedPeriodType;
    }

    public void setLockedPeriodType(OccPeriodType lockedPeriodType) {
        this.lockedPeriodType = lockedPeriodType;
    }

    public EventDomainEnum getCurrentDomain() {
        return currentDomain;
    }

    public void setCurrentDomain(EventDomainEnum currentDomain) {
        this.currentDomain = currentDomain;
    }

    public CECase getCurrentCase() {
        return currentCase;
    }

    public void setCurrentCase(CECase currentCase) {
        this.currentCase = currentCase;
    }

    public FeeAssigned getAssignedFormFee() {
        return assignedFormFee;
    }

    public void setAssignedFormFee(FeeAssigned assignedFormFee) {
        this.assignedFormFee = assignedFormFee;
    }

    public CodeViolation getSelectedViolation() {
        return selectedViolation;
    }

    public void setSelectedViolation(CodeViolation selectedViolation) {
        this.selectedViolation = selectedViolation;
    }

}
