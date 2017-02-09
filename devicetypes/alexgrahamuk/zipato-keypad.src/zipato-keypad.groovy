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
		capability "Actuator"
		capability "Switch"
		capability "Switch Level"
		capability "Refresh"
		capability "Configuration"
		capability "Sensor"
		capability "Zw Multichannel"
		capability "Battery"

		fingerprint inClusters: "0x60"
		fingerprint inClusters: "0x60, 0x25"
		fingerprint inClusters: "0x60, 0x26"
		fingerprint inClusters: "0x5E, 0x59, 0x60, 0x8E"
        
        command "getUsers"
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
		standardTile("switchOn", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "on", label:'on', action:"switch.on", icon:"st.switches.switch.on"
		}
		standardTile("switchOff", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "off", label:'off', action:"switch.off", icon:"st.switches.switch.off"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}

		main "switch"
		details (["switch", "switchOn", "switchOff", "levelSliderControl", "refresh"])
	}
}

import physicalgraph.zwave.commands.usercodev1.*

def getUsers() {
	log.debug("Asked for users")
    def event = createEvent(descriptionText: "${device.displayName} getting user numbers")
    def cmds = []
    cmds << zwave.userCodeV1.usersNumberGet().format()
    cmds << zwave.userCodeV1.usersNumberReport().format()
 //   response(cmds)
    [event, response(cmds)]    
    return []
}

def zwaveEvent(physicalgraph.zwave.commands.usercodev1.UserCodeGet cmd) {
	log.debug("Mega Banana!")
}

def zwaveEvent(physicalgraph.zwave.commands.usercodev1.UsersNumberGet cmd) {
	log.debug("Mega Sandwich!")
}

def zwaveEvent(physicalgraph.zwave.commands.usercodev1.UsersNumberReport cmd) {
	log.debug("Mega Tooth: $cmd")
}



def parse(String description) {
	def result = null
	if (description.startsWith("Err")) {
	    result = createEvent(descriptionText:description, isStateChange:true)
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x71: 2, 0x63: 1, 0x20: 1, 0x84: 1, 0x98: 1, 0x56: 1, 0x60: 3])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	log.debug("'$description' parsed to $result")
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.alarmv2.AlarmReport cmd) {
	log.debug("Yeah!!!! $cmd")
}

def zwaveEvent(physicalgraph.zwave.commands.usercodev1.UserCodeReport cmd) {
	log.debug("Smecker: $cmd")
    
    /*
	String	code
    Object	user
    Short	userIdStatus
    Short	userIdentifier
    List<Short>	payload

    Short	USER_ID_STATUS_AVAILABLE_NOT_SET	= 0
    Short	USER_ID_STATUS_OCCUPIED	= 1
    Short	USER_ID_STATUS_RESERVED_BY_ADMINISTRATOR	= 2
    Short	USER_ID_STATUS_STATUS_NOT_AVAILABLE	= 255

	String format()
	*/
    
    def event = createEvent(descriptionText: "${device.displayName} trying to register", displayed: false)
    def cmds = []
    //cmds << zwave.userCodeV1.userCodeSet(userIdentifier:cmd.userIdentifier, userIdStatus:1, user: "1").format()
    cmds << zwave.userCodeV1.userCodeGet(userIdentifier: 1).format()
    //cmds << zwave.userCodeV1.usersNumberReport().format()
    
    [event, response(cmds)]    
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	/*def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "$device.displayName has a low battery"
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbatt = now()
	createEvent(map)*/
    debug.log("Battery: $cmd")
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	[ createEvent(descriptionText: "${device.displayName} woke up", isStateChange:true),
	  response(["delay 2000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()]) ]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	createEvent(descriptionText: "$device.displayName: $cmd", isStateChange: true)
}

def on() {
	commands([zwave.basicV1.basicSet(value: 0xFF), zwave.basicV1.basicGet()])
}

def off() {
	commands([zwave.basicV1.basicSet(value: 0x00), zwave.basicV1.basicGet()])
}

def refresh() {
	command(zwave.basicV1.basicGet())
}

def configure() {
}


private command(physicalgraph.zwave.Command cmd) {
	if (state.sec) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=200) {
	delayBetween(commands.collect{ command(it) }, delay)
}
