/**
 *  Copyright 2017 Alex Graham
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Zipato Keypad", namespace: "alexgrahamuk", author: "Alex Graham") {
    
        preferences {
        	input ("feedbackTimeout", "number", title: "Feedback Timeout (seconds)", description: "Number of seconds to beep and (optionally wait) before sending the mode change", default: 5, displayDuringSetup: true)
    	}
    
		capability "Actuator"
        capability "switch"
		capability "Refresh"
		capability "Configuration"
		capability "Sensor"
		capability "Zw Multichannel"
        capability "Polling"
		capability "Battery"
        capability "lockCodes"

        command "getUsers"
        command "armAlarm"

        //Raw Description
        //zw:S type:4000 mfr:0097 prod:6131 model:4501 ver:0.28 zwv:3.67 lib:03 cc:85,80,84,86,72,71,70,25,63
        fingerprint mfr: "0097", prod: "6131", model: "4501"
        
       
	}

	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		reply "600902": "command: 600A, payload: 210031"
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.unknown.zwave.device", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.unknown.zwave.device", backgroundColor: "#ffffff"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details (["switch", "switchOn", "switchOff", "levelSliderControl", "refresh"])
	}
}

import physicalgraph.zwave.commands.usercodev1.*
import physicalgraph.zwave.commands.switchbinaryv1.*

def updateCodes() {
}

def setCode(id, code) {

    def cmds = []
    //cmds << zwave.userCodeV1.userCodeSet(userIdentifier: $id, userIdStatus: 1, code: "$code").format()
    //cmds << zwave.userCodeV1.userCodeGet(userIdentifier: $id).format()
	return response(cmds)
}

def deleteCode(id) {
    //cmds << zwave.userCodeV1.userCodeSet(userIdentifier: $id, userIdStatus: 0, code: "").format()
}

def requestCode(id) {
	//cmds << zwave.userCodeV1.userCodeGet(userIdentifier: $id).format()
}

def armAlarm() {
    sendEvent(name: "alarm.reallyArmed", descriptionText: "${device.displayName} ARMED!!!!!!!!", displayed: false, isStateChange: true)
    def cmds = []
    cmds << zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00).format()
	return response(cmds)
}

def reloadAllCodes() {
	def cmds = []
    def event = createEvent(descriptionText: "${device.displayName} user report", displayed: false, isStateChange: true)
    cmds << zwave.userCodeV1.usersNumberGet().format()
    def result = response(cmds)
    log.debug(result)
    return result
}

def on(evt) {
	log.debug("ONNONONONNONO: $evt")
    sendEvent(name: "switch.on", descriptionText: "${device.displayName} is on", displayed: false, isStateChange: true)
}

def poll() {
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	log.debug("Battery: $cmd.batteryLevel")
	def event = createEvent(descriptionText: "${device.displayName} battery report: $cmd", displayed: false)
    def cmds = []
    [event, response(cmds)]
}

def zwaveEvent(physicalgraph.zwave.commands.usercodev1.UsersNumberReport cmd) {
    def event = createEvent(descriptionText: "${device.displayName} user report: $cmd", displayed: false)
    def cmds = []
   
   	//Reset all codes
	//cmds << zwave.userCodeV1.userCodeSet(userIdentifier:0, userIdStatus: 0, code: "").format()
   
    for (i in 1..2) {
    	cmds << zwave.userCodeV1.userCodeGet(userIdentifier: i).format()
    }

    [event, response(cmds)]  
}

def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
	    result = createEvent(descriptionText:description, isStateChange:true)
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x80: 1, 0x71: 2, 0x25: 1, 0x70: 1, 0x63: 1, 0x20: 1, 0x84: 2, 0x98: 1, 0x56: 1, 0x60: 3])
		if (cmd) {
            result = zwaveEvent(cmd)
		}
	}
	log.debug("'$description' parsed to $result")
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd) {
    def event = createEvent(name: "alarm.armed", value: feedbackTimeout, descriptionText: "${device.displayName} alarm went: $cmd", displayed: false, isStateChange: true)
    def cmds = []
    //Endless notification, we will turn it off ourselves
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0xFF], parameterNumber: 2, size: 1).format()
    //Sensible timout for getting back to the keypad whether we accepted the code
    cmds << zwave.configurationV1.configurationSet(configurationValue: [60], parameterNumber: 3, size: 1).format()
    //Turn on the beeps
    cmds << zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF).format()
    [event, response(cmds)]
}


def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
    def event = createEvent(descriptionText: "***********************${device.displayName} binary went off: $cmd", displayed: false)
    def cmds = []
    //cmds << zwave.wakeUpV2.wakeUpNoMoreInformation().format()
    [event, response(cmds)]	
}

def zwaveEvent(physicalgraph.zwave.commands.usercodev1.UserCodeReport cmd) {
    def event = createEvent(descriptionText: "${device.displayName} trying to register: $cmd", displayed: false)
    def cmds = []
   
    //cmds << zwave.userCodeV1.userCodeSet(userIdentifier:1, userIdStatus: 1, code: "1111").format()
    //cmds << zwave.userCodeV1.userCodeGet(userIdentifier:1).format()
    //cmds << zwave.wakeUpV2.wakeUpNoMoreInformation().format()
 
 	//Convert those pesky RFID tags
    def meh = []
    for (a in cmd.user) {
    	def va = new Short(a)
        if (va <= 48)
        	va = -48 + (-va)
		meh.add(va)
    }

	//cmds << zwave.userCodeV1.userCodeSet(userIdentifier:2, userIdStatus: 1, user: meh).format()
    //cmds << zwave.userCodeV1.userCodeGet(userIdentifier:2).format()
    //cmds << zwave.wakeUpV2.wakeUpNoMoreInformation().format()
    
    if (cmd.userIdStatus == UserCodeReport.USER_ID_STATUS_AVAILABLE_NOT_SET)
    	cmds << zwave.userCodeV1.usersNumberGet().format()

	[event, response(cmds)]    
}


def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def event = createEvent(descriptionText: "${device.displayName} woke up v2", isStateChange:true)
    def cmds = []
	cmds << zwave.batteryV1.batteryGet().format()  
    [event, response(cmds)]    
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", isStateChange: true)
}

def refresh() {
	poll()
}

def configure() {
}


def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x62: 1, 0x71: 2, 0x80: 1, 0x85: 2, 0x63: 1, 0x98: 1, 0x86: 1])
	// log.debug "encapsulated: $encapsulatedCommand"
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

private command(physicalgraph.zwave.Command cmd) {
    zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private commands(commands, delay=200) {
	delayBetween(commands.collect{ command(it) }, delay)
}
