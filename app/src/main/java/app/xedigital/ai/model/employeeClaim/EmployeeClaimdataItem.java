package app.xedigital.ai.model.employeeClaim;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EmployeeClaimdataItem implements Serializable {

    @SerializedName("shared")
    private String shared;

    @SerializedName("distance")
    private int distance;

    @SerializedName("statusHr")
    private String statusHr;

    @SerializedName("docFileURLTwo")
    private String docFileURLTwo;

    @SerializedName("docFileURLThree")
    private String docFileURLThree;

    @SerializedName("docFileURL")
    private String docFileURL;

    @SerializedName("docFileURLSeven")
    private String docFileURLSeven;

    @SerializedName("project")
    private String project;

    @SerializedName("perposeofmeet")
    private String perposeofmeet;

    @SerializedName("remark")
    private String remark;

    @SerializedName("docFileURLEight")
    private String docFileURLEight;

    @SerializedName("employee")
    private Employee employee;

    @SerializedName("empTransport")
    private String empTransport;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("statusRm")
    private String statusRm;

    @SerializedName("currency")
    private String currency;

    @SerializedName("docFileURLKeyOne")
    private String docFileURLKeyOne;

    @SerializedName("meeting")
    private String meeting;

    @SerializedName("docFileURLKeySix")
    private String docFileURLKeySix;

    @SerializedName("dedicated")
    private String dedicated;

    @SerializedName("reportingManager")
    private String reportingManager;

    @SerializedName("toaddress")
    private String toaddress;

    @SerializedName("docFileURLKeyThree")
    private String docFileURLKeyThree;

    @SerializedName("modeoftransport")
    private String modeoftransport;

    @SerializedName("docFileURLKeySeven")
    private String docFileURLKeySeven;

    @SerializedName("docFileURLKeyEight")
    private String docFileURLKeyEight;

    @SerializedName("confbutton")
    private boolean confbutton;

    @SerializedName("claimId")
    private String claimId;

    @SerializedName("docFileURLKeyFour")
    private String docFileURLKeyFour;

    @SerializedName("approvedDate")
    private String approvedDate;

    @SerializedName("docFileURLKey")
    private String docFileURLKey;

    @SerializedName("docFileURLFour")
    private String docFileURLFour;

    @SerializedName("docFileURLSix")
    private String docFileURLSix;

    @SerializedName("docFileURLKeyNine")
    private String docFileURLKeyNine;

    @SerializedName("totalamount")
    private int totalamount;

    @SerializedName("claimDate")
    private String claimDate;

    @SerializedName("docFileURLKeyTwo")
    private String docFileURLKeyTwo;

    @SerializedName("docFileURLKeyFive")
    private String docFileURLKeyFive;

    @SerializedName("individually")
    private String individually;

    @SerializedName("comment")
    private String comment;

    @SerializedName("_id")
    private String id;

    @SerializedName("docFileURLNine")
    private String docFileURLNine;

    @SerializedName("fromaddress")
    private String fromaddress;

    @SerializedName("docFileURLOne")
    private String docFileURLOne;

    @SerializedName("docFileURLFive")
    private String docFileURLFive;

    @SerializedName("travelCategory")
    private String travelCategory;

    public List<String> getDocumentUrls() {
        List<String> documentUrls = new ArrayList<>();
        if (docFileURL != null && !docFileURL.isEmpty()) {
            documentUrls.add(docFileURL);
        }
        if (docFileURLOne != null && !docFileURLOne.isEmpty()) {
            documentUrls.add(docFileURLOne);
        }
        if (docFileURLTwo != null && !docFileURLTwo.isEmpty()) {
            documentUrls.add(docFileURLTwo);
        }
        if (docFileURLThree != null && !docFileURLThree.isEmpty()) {
            documentUrls.add(docFileURLThree);
        }
        if (docFileURLFour != null && !docFileURLFour.isEmpty()) {
            documentUrls.add(docFileURLFour);
        }
        if (docFileURLFive != null && !docFileURLFive.isEmpty()) {
            documentUrls.add(docFileURLFive);
        }
        if (docFileURLSix != null && !docFileURLSix.isEmpty()) {
            documentUrls.add(docFileURLSix);
        }
        if (docFileURLSeven != null && !docFileURLSeven.isEmpty()) {
            documentUrls.add(docFileURLSeven);
        }
        if (docFileURLEight != null && !docFileURLEight.isEmpty()) {
            documentUrls.add(docFileURLEight);
        }
        if (docFileURLNine != null && !docFileURLNine.isEmpty()) {
            documentUrls.add(docFileURLNine);
        }
        return documentUrls;
    }

    public String getShared() {
        return shared;
    }

    public int getDistance() {
        return distance;
    }

    public String getStatusHr() {
        return statusHr;
    }

    public String getDocFileURLTwo() {
        return docFileURLTwo;
    }

    public String getDocFileURLThree() {
        return docFileURLThree;
    }

    public String getDocFileURL() {
        return docFileURL;
    }

    public String getDocFileURLSeven() {
        return docFileURLSeven;
    }

    public String getProject() {
        return project;
    }

    public String getPerposeofmeet() {
        return perposeofmeet;
    }

    public String getRemark() {
        return remark;
    }

    public String getDocFileURLEight() {
        return docFileURLEight;
    }

    public Employee getEmployee() {
        return employee;
    }

    public String getEmpTransport() {
        return empTransport;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getStatusRm() {
        return statusRm;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDocFileURLKeyOne() {
        return docFileURLKeyOne;
    }

    public String getMeeting() {
        return meeting;
    }

    public String getDocFileURLKeySix() {
        return docFileURLKeySix;
    }

    public String getDedicated() {
        return dedicated;
    }

    public String getReportingManager() {
        return reportingManager;
    }

    public String getToaddress() {
        return toaddress;
    }

    public String getDocFileURLKeyThree() {
        return docFileURLKeyThree;
    }

    public String getModeoftransport() {
        return modeoftransport;
    }

    public String getDocFileURLKeySeven() {
        return docFileURLKeySeven;
    }

    public String getDocFileURLKeyEight() {
        return docFileURLKeyEight;
    }

    public boolean isConfbutton() {
        return confbutton;
    }

    public String getClaimId() {
        return claimId;
    }

    public String getDocFileURLKeyFour() {
        return docFileURLKeyFour;
    }

    public String getApprovedDate() {
        return approvedDate;
    }

    public String getDocFileURLKey() {
        return docFileURLKey;
    }

    public String getDocFileURLFour() {
        return docFileURLFour;
    }

    public String getDocFileURLSix() {
        return docFileURLSix;
    }

    public String getDocFileURLKeyNine() {
        return docFileURLKeyNine;
    }

    public int getTotalamount() {
        return totalamount;
    }

    public String getClaimDate() {
        return claimDate;
    }

    public String getDocFileURLKeyTwo() {
        return docFileURLKeyTwo;
    }

    public String getDocFileURLKeyFive() {
        return docFileURLKeyFive;
    }

    public String getIndividually() {
        return individually;
    }

    public String getComment() {
        return comment;
    }

    public String getId() {
        return id;
    }

    public String getDocFileURLNine() {
        return docFileURLNine;
    }

    public String getFromaddress() {
        return fromaddress;
    }

    public String getDocFileURLOne() {
        return docFileURLOne;
    }

    public String getDocFileURLFive() {
        return docFileURLFive;
    }

    public String getTravelCategory() {
        return travelCategory;
    }
}