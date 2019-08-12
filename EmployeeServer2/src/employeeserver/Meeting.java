/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package employeeserver;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Juan Javier
 */
public class Meeting implements Serializable{
    private String topic;
    private List<String> nameOfInvitees;
    private String nameOfOrganizer;
    private String setting;
    private Date dateBeginning;
    private Date dateEnd;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setNameOfInvitees(List<String> nameOfInvitees) {
        this.nameOfInvitees = nameOfInvitees;
    }

    public void setNameOfOrganizer(String nameOfOrganizer) {
        this.nameOfOrganizer = nameOfOrganizer;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public void setDateBeginning(Date dateBeginning) {
        this.dateBeginning = dateBeginning;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getTopic() {
        return topic;
    }

    public List<String> getNameOfInvitees() {
        return nameOfInvitees;
    }

    public String getNameOfOrganizer() {
        return nameOfOrganizer;
    }

    public String getSetting() {
        return setting;
    }

    public Date getDateBeginning() {
        return dateBeginning;
    }

    public Date getDateEnd() {
        return dateEnd;
    }
    
    @Override
    public String toString(){
        return topic + ":\t" + dateBeginning;
    }
    
    public boolean myEquals(Meeting o){
        
        Meeting comparedMeeting = (Meeting)o;
        if(topic == null ? comparedMeeting.getTopic() != null : !topic.equals(comparedMeeting.getTopic())){
            return false;
        }
        if(setting == null ? comparedMeeting.getSetting() != null : !setting.equals(comparedMeeting.getSetting())){
            return false;
        }
        if(dateBeginning.compareTo(comparedMeeting.getDateBeginning()) != 0 ){
            return false;
        }
        if(dateEnd.compareTo(comparedMeeting.getDateEnd()) != 0 ){
            return false;
        }
        if(nameOfInvitees.size() != comparedMeeting.getNameOfInvitees().size()){
            return false;
        }
        if(nameOfOrganizer == null ? comparedMeeting.getNameOfOrganizer() != null : !nameOfOrganizer.equals(comparedMeeting.getNameOfOrganizer())){
            return false;
        }
        for(int i=0 ; i<nameOfInvitees.size() ; i++){
            if(nameOfInvitees.get(i) == null ? comparedMeeting.getNameOfInvitees().get(i) != null : !nameOfInvitees.get(i).equals(comparedMeeting.getNameOfInvitees().get(i)))
                return false;
        }
        return true;
    }
}
