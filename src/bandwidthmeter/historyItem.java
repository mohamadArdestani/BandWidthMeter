/*
 * Copyright 2018 ma.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bwm;

/**
 *
 * @author ma
 * mohamadardestani@gmail.com
 */
public class historyItem {
    private String date;
    private String send;
    private String recive;

    public historyItem() {
    }
    
    public historyItem(String date,String send,String recive) {
        this.date=date;
        this.send=send;
        this.recive=recive;
    }
    
    public void setDate(String date){
        this.date=date;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @return the send
     */
    public String getSend() {
        return send;
    }

    /**
     * @param send the send to set
     */
    public void setSend(String send) {
        this.send = send;
    }

    /**
     * @return the recive
     */
    public String getRecive() {
        return recive;
    }

    /**
     * @param recive the recive to set
     */
    public void setRecive(String recive) {
        this.recive = recive;
    }
    
}
